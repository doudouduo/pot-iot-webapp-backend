package com.pot.iot.webapp;

import com.pot.iot.webapp.Controller.BaseController;
import com.pot.iot.webapp.Entity.ResultVo;
import com.pot.iot.webapp.Entity.User;
import com.pot.iot.webapp.Interface.IMailService;
import com.pot.iot.webapp.Repository.UserRepository;
import com.pot.iot.webapp.Util.RS256Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@Configuration
public class UserController extends BaseController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RS256Util rs256Util;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IMailService mailService;
    @Value("${mail.activate.account}")
    private String activateAccountMail;
    @Value("${mail.reset.password}")
    private String resetPasswordMail;
    @Value("${frontend.host}")
    private String host;
    @Value("${frontend.port}")
    private String port;
    Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/register")
    public ResultVo registerUser(@RequestBody Map<String,String> registerUser){
        String userId=generateUserId();
        String username= registerUser.get("username");
        String email=registerUser.get("email");
        String password= DigestUtils.md5DigestAsHex(registerUser.get("password").getBytes());
        if (userRepository.findUserByEmail(email)!=null){
            logger.error("Email {} has been used.",email);
            return error(ResultVo.ResultCode.EMAIL_DUPLICATE_ERROR);
        }
        User user = new User(userId,username,email,password);
        String token = rs256Util.buildToken(userId,"ACTIVATION");
        String url="http://"+host+":"+port+"/user-services/activation-result?token="+token;
        String registerMail=activateAccountMail.replace("\'username\'",username);
        registerMail=registerMail.replace("\'url\'",url);
        try{
            mailService.sendHtmlMail(user.getEmail(),"Activate Your Account",registerMail);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            logger.error("Registration email cannot be sent to {}",email);
            return error(ResultVo.ResultCode.REGISTRATION_EMAIL_ERROR);
        }
        User save = userRepository.save(user);
        redisTemplate.opsForValue().set(token,email,rs256Util.ACTIVATION_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        logger.info("User {} has been registered.",save.getUserId());
        return success(save.toString());
    }

    @GetMapping("/checkEmail")
    public ResultVo checkEmail(HttpServletRequest request,
                               HttpServletResponse response){
        String email=request.getParameter("email");
        User user=userRepository.findUserByEmailAndAccountStatus(email,true);
        if (user==null){
            user=userRepository.findUserByEmail(email);
            if (user==null){
                logger.error("Email {} is invalid.",email);
                return error(ResultVo.ResultCode.EMAIL_INVALID_ERROR);
            }
            else {
                logger.error("Account {} hasn't been activated.",user.getUserId());
                return error(ResultVo.ResultCode.ACCOUNT_INACTIVE_ERROR);
            }
        };
        return success();
    }

    @PostMapping("/login")
    public ResultVo login(@RequestBody Map<String,String>loginUser){
        BCryptPasswordEncoder bCryptPasswordEncoder=new BCryptPasswordEncoder();
        String email=loginUser.get("email");
        String password= DigestUtils.md5DigestAsHex(loginUser.get("password").getBytes());
        User user=userRepository.findUserByEmailAndAccountStatus(email,true);
        if (user==null){
            user=userRepository.findUserByEmail(email);
            if (user==null){
                logger.error("Email {} is invalid.",email);
                return error(ResultVo.ResultCode.EMAIL_INVALID_ERROR);
            }
            else {
                logger.error("Account {} hasn't been activated.",user.getUserId());
                return error(ResultVo.ResultCode.ACCOUNT_INACTIVE_ERROR);
            }
        };
        if (password.equals(user.getPassword())) {
            String userId=user.getUserId();
            String token = rs256Util.buildToken(userId,"LOGIN");
            Object value = redisTemplate.opsForValue().get(userId);
            if (value!=null) {
                redisTemplate.opsForValue().getOperations().delete(String.format(value.toString(), userId));
            }
            redisTemplate.opsForValue().set(token,user.getUserId() , rs256Util.LOGIN_EXPIRE_TIME, TimeUnit.MILLISECONDS);
            redisTemplate.opsForValue().set(user.getUserId() ,token, rs256Util.LOGIN_EXPIRE_TIME, TimeUnit.MICROSECONDS);

            return success(token);
        }
        else return error(ResultVo.ResultCode.WRONG_PASSWORD_ERROR);
    }

    @GetMapping("/activateAccount")
    public ResultVo activateAccount(HttpServletRequest request,
                                    HttpServletResponse response){
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        if (value==null){
            logger.error("Activation token {} has expired.",token);
            return error(ResultVo.ResultCode.TOKEN_AURHENTICATION_ERROR);
        }
        String email=value.toString();
        User user=userRepository.findUserByEmailAndAccountStatus(email,false);
        if (user==null){
            logger.error("Email {} is invalid.",email);
            return error(ResultVo.ResultCode.EMAIL_INVALID_ERROR);
        }
        user.setAccountStatus(true);
        userRepository.save(user);
        redisTemplate.opsForValue().getOperations().delete(String.format(token,email));
        logger.info("Account {} has been activated.",user.getUserId());
        return success();
    }

    @PostMapping("/reactiveAccount")
    public ResultVo reactiveAccount(@RequestBody Map<String,String> reactiveAccount){
        String email=reactiveAccount.get("email");
        User user=userRepository.findUserByEmailAndAccountStatus(email,false);
        if (user==null){
            user=userRepository.findUserByEmailAndAccountStatus(email,true);
            if (user==null){
                logger.error("Email {} is invalid.",email);
                return error(ResultVo.ResultCode.EMAIL_INVALID_ERROR);
            }
            else {
                logger.error("Email {} has been activated.",email);
                return error(ResultVo.ResultCode.ACCOUNT_ACTIVATED_ERROR);
            }
        }
        String token = rs256Util.buildToken(user.getUserId(),"ACTIVATION");
        String url="http://"+host+":"+port+"/user-services/activation-result?token="+token;
        String registerMail=activateAccountMail.replace("\'username\'",user.getUsername());
        registerMail=registerMail.replace("\'url\'",url);
        try{
            mailService.sendHtmlMail(user.getEmail(),"Activate Your Account",registerMail);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            logger.error("Registration email cannot be sent to {}",email);
            return error(ResultVo.ResultCode.REGISTRATION_EMAIL_ERROR);
        }
        redisTemplate.opsForValue().set(token,email,rs256Util.ACTIVATION_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        logger.info("Reactivation email has been sent to {}.",email);
        return success();
    }

    @PostMapping("/forgetPassword")
    public ResultVo forgetPassword(@RequestBody Map<String,String>userEmail){
        String email=userEmail.get("email");
        User user=userRepository.findUserByEmailAndAccountStatus(email,true);
        if (user==null){
            logger.error("Email {} is invalid.",email);
            return error(ResultVo.ResultCode.EMAIL_INVALID_ERROR);
        }
        String token=rs256Util.buildToken(user.getUserId(),"RESET_PASSWORD");
        redisTemplate.opsForValue().set(token,email,rs256Util.RESET_PASSWORD_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        String url="http://"+host+":"+port+"/user-services/reset-password?token="+token;
        String forgetPasswordMail=resetPasswordMail.replace("\'url\'",url);
        forgetPasswordMail=forgetPasswordMail.replace("\'username\'",user.getUsername());
        try{
            mailService.sendHtmlMail(user.getEmail(),"Reset Your Password",forgetPasswordMail);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            logger.error("Password reset email cannot be sent to {}",email);
            return error(ResultVo.ResultCode.FORGET_PASSWORD_EMAIL_ERROR);
        }
        logger.info("Password reset Email has been sent to {}",email);
        return success();
    }

    @PostMapping("/resetPassword")
    public ResultVo resetPassword(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @RequestBody Map<String,String>newPassword){
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        if (value==null){
            logger.error("Password reset token {} has expired.",token);
            return error(ResultVo.ResultCode.TOKEN_AURHENTICATION_ERROR);
        }
        String email=value.toString();
        User user=userRepository.findUserByEmailAndAccountStatus(email,true);
        if (user==null){
            logger.error("Email {} is invalid.",email);
            return error(ResultVo.ResultCode.EMAIL_INVALID_ERROR);
        }

        String password= DigestUtils.md5DigestAsHex(newPassword.get("password").getBytes());
        user.setPassword(password);
        userRepository.save(user);
        logger.info("User {} has reset password.",user.getUserId());
        return success();
    }

    @PostMapping("/changeUsername")
    public ResultVo changeUsername(HttpServletRequest request,
                                   HttpServletResponse response,
                                   @RequestBody Map<String,String>userName){
        String username=userName.get("username");
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        if (value==null){
            logger.error("Token {} has expired.",token);
            return error(ResultVo.ResultCode.TOKEN_AURHENTICATION_ERROR);
        }
        String userid=value.toString();
        User user=userRepository.findByUserId(userid);
        if (user==null){
            logger.error("Token {} is invalid.",token);
            return error(ResultVo.ResultCode.TOKEN_AURHENTICATION_ERROR);
        }
        user.setUsername(username);
        userRepository.save(user);
        logger.info("User {} has changed username.",user.getUserId());
        return success(user.toString());
    }

    @PostMapping("/logout")
    public ResultVo logout(HttpServletRequest request,
                           HttpServletResponse response){
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        if (value==null){
            logger.error("Token {} has expired.",token);
            return error(ResultVo.ResultCode.TOKEN_AURHENTICATION_ERROR);
        }
        String userid=value.toString();
        redisTemplate.opsForValue().getOperations().delete(String.format(token,userid));
        redisTemplate.opsForValue().getOperations().delete(String.format(userid,token));
        logger.info("User {} has logged out.",userid);
        return success();
    }

    public String generateUserId(){
        String id= UUID.randomUUID().toString();
        id=id.replaceAll("[^\\d]+", "");
        while (id.length()<8){
            id=UUID.randomUUID().toString();
            id=id.replaceAll("[^\\d]+", "");
        }
        id="PIU-"+id.substring(0,8);
        while (userRepository.findByUserId(id)!=null) {
            id = UUID.randomUUID().toString();
            id = id.replaceAll("[^\\d]+", "");
            while (id.length() < 8) {
                id = UUID.randomUUID().toString();
                id = id.replaceAll("[^\\d]+", "");
            }
            id = "PIU-" + id.substring(0, 8);
        }
        return id;
    }

    @Configuration
    public class CORSConfiguration extends WebMvcConfigurerAdapter {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                    .allowedMethods("*")
                    .allowedOrigins("*")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            super.addCorsMappings(registry);
        }
    }
}

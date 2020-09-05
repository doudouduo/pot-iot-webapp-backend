package com.pot.iot.webapp;

import com.pot.iot.webapp.Controller.BaseController;
import com.pot.iot.webapp.Entity.ResultVo;
import com.pot.iot.webapp.Entity.User;
import com.pot.iot.webapp.Interface.IMailService;
import com.pot.iot.webapp.Repository.UserRepository;
import com.pot.iot.webapp.Util.RS256Util;
import com.sun.deploy.net.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/register")
    public ResultVo registerUser(@RequestBody Map<String,String> registerUser){
        String userId=generateUserId();
        String username= registerUser.get("username");
        String email=registerUser.get("email");
        String password= DigestUtils.md5DigestAsHex(registerUser.get("password").getBytes());
        if (userRepository.findUserByEmail(email)!=null){
            return error(ResultVo.ResultCode.EMAIL_DUPLICATE_ERROR);
        }
        User user = new User(userId,username,email,password);
        User save = userRepository.save(user);
        String token = rs256Util.buildToken(userId);
        redisTemplate.opsForValue().set(token,email);
        String url="https://ec2-18-163-118-242.ap-east-1.compute.amazonaws.com:8080/activateAccount?token="+token;
        activateAccountMail=activateAccountMail.replace("\'username\'",user.getUsername());
        activateAccountMail=activateAccountMail.replace("\'url\'",url);
        mailService.sendHtmlMail(user.getEmail(),"Activate Your Account",activateAccountMail);
        return success(save.toString());
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
                return error(ResultVo.ResultCode.EMAIL_INVALID_ERROR);
            }
            else return error(ResultVo.ResultCode.ACCOUNT_INACTIVATE_ERROR);
        };
        if (password.equals(user.getPassword())) {
            String userId=user.getUserId();
            String token = rs256Util.buildToken(userId);
            Object value = redisTemplate.opsForValue().get(userId);
            if (value!=null) {
                redisTemplate.opsForValue().getOperations().delete(String.format(value.toString(), userId));
            }
            redisTemplate.opsForValue().set(token,user.getUserId() , rs256Util.EXPIRE_TIME, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(user.getUserId() ,token, rs256Util.EXPIRE_TIME, TimeUnit.SECONDS);

            return success(token);
        }
        else return error(ResultVo.ResultCode.WRONG_PASSWORD_ERROR);
    }

    @PostMapping("/activateAccount")
    public ResultVo activateAccount(HttpServletRequest request,
                                    HttpServletResponse response){
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        if (value==null){
            return error(ResultVo.ResultCode.TOKEN_AURHENTICATION_ERROR);
        }
        String email=value.toString();
        User user=userRepository.findUserByEmailAndAccountStatus(email,false);
        if (user==null){
            return error(ResultVo.ResultCode.EMAIL_INVALID_ERROR);
        }
        user.setAccountStatus(true);
        redisTemplate.opsForValue().getOperations().delete(String.format(token,email));
        return success();
    }

    @PostMapping("/forgetPassword")
    public ResultVo forgetPassword(@RequestBody Map<String,String>userEmail){
        String email=userEmail.get("email");
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
            return error(ResultVo.ResultCode.TOKEN_AURHENTICATION_ERROR);
        }
        String userid=value.toString();
        redisTemplate.opsForValue().set(token,userid , rs256Util.EXPIRE_TIME, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(userid,token, rs256Util.EXPIRE_TIME, TimeUnit.SECONDS);
        User user=userRepository.findByUserId(userid);
        user.setUsername(username);
        userRepository.save(user);
        return success(user.toString());
    }

    @PostMapping("/logout")
    public ResultVo logout(HttpServletRequest request,
                           HttpServletResponse response){
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        if (value==null){
            return error(ResultVo.ResultCode.TOKEN_AURHENTICATION_ERROR);
        }
        String userid=value.toString();
        redisTemplate.opsForValue().getOperations().delete(String.format(token,userid));
        redisTemplate.opsForValue().getOperations().delete(String.format(userid,token));
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
}

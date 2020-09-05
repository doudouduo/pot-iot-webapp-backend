package com.pot.iot.webapp.Controller;

import com.pot.iot.webapp.Entity.ResultVo;
import com.pot.iot.webapp.Entity.User;
import com.pot.iot.webapp.Interface.IMailService;
import com.pot.iot.webapp.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@Configuration
public class MailController extends BaseController {
    @Autowired
    private IMailService mailService;
    @Value("${mail.activate.account}")
    private String activateAccountMail;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/sendText")
    public void sendTest(){
        mailService.sendSimpleMail("ydoudouduoy@gmail.com","test","test content");
    }

    @PostMapping("/sendHtml")
    public ResultVo sendHtml(HttpServletRequest request,
                         HttpServletResponse response){
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        if (value==null){
            return error(ResultVo.ResultCode.TOKEN_AURHENTICATION_ERROR);
        }
        String userid=value.toString();
        User user=userRepository.findByUserId(userid);
        activateAccountMail=activateAccountMail.replace("\'username\'",user.getUsername());
        mailService.sendHtmlMail(user.getEmail(),"Activate Your Account",activateAccountMail);
        return success();
    }
}

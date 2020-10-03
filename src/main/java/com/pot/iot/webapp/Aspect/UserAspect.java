package com.pot.iot.webapp.Aspect;

import com.pot.iot.webapp.Controller.BaseController;
import com.pot.iot.webapp.Entity.ResultVo;
import com.pot.iot.webapp.Util.RS256Util;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;


@Aspect
@Component
public class UserAspect {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RS256Util rs256Util;
    @Autowired
    private BaseController baseController;


    @Pointcut("execution(public * com.pot.iot.Controller.*.*(..))")
    public void pointCut() {}

    @Around("pointCut()")
    public Object before(ProceedingJoinPoint pjp) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        if (value==null){
            return baseController.error(ResultVo.ResultCode.TOKEN_AURHENTICATION_ERROR);
        }
        redisTemplate.opsForValue().set(token,value.toString() , rs256Util.LOGIN_EXPIRE_TIME, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(value.toString() ,token, rs256Util.LOGIN_EXPIRE_TIME, TimeUnit.SECONDS);
        return pjp.proceed();
    }
}

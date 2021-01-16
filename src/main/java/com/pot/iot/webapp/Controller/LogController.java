package com.pot.iot.webapp.Controller;

import com.pot.iot.webapp.Entity.CommandLog;
import com.pot.iot.webapp.Entity.IotLog;
import com.pot.iot.webapp.Entity.ResultVo;
import com.pot.iot.webapp.Entity.UserDevice;
import com.pot.iot.webapp.Repository.UserDeviceRepository;
import com.pot.iot.webapp.Service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class LogController extends BaseController{
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private LogService logService;
    @Autowired
    private UserDeviceRepository userdeviceRepository;
    Logger logger = LoggerFactory.getLogger(LogController.class);

    @GetMapping(value = "/commandLog")
    public ResultVo getCommandLogByLogName(HttpServletRequest request,
                                           HttpServletResponse response){
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        String userId=value.toString();
        String imei=request.getParameter("imei");
        String logTime=request.getParameter("log_time");
        UserDevice device=userdeviceRepository.findDeviceByUserIdAndImeiAndIsdelete(userId,imei,false);
        if (device==null){
            logger.error("Device {} provided by user {} is invalid.",imei,userId);
            return error(ResultVo.ResultCode.DEVICE_INVALID_ERROR);
        }
        String logName=imei+"-"+logTime;
        CommandLog commandLog=logService.getCommandLogByLogName(logName);
        if (commandLog==null){
            logger.error("Command log {} is null.",logName);
            return error(ResultVo.ResultCode.COMMAND_LOG_NOT_FOUND_ERROR);
        }
        return success(commandLog);
    }

    @GetMapping(value = "/iotLog")
    public ResultVo getIotLogByLogName(HttpServletRequest request,
                                           HttpServletResponse response){
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        String userId=value.toString();
        String imei=request.getParameter("imei");
        String logTime=request.getParameter("log_time");
        UserDevice device=userdeviceRepository.findDeviceByUserIdAndImeiAndIsdelete(userId,imei,false);
        if (device==null){
            logger.error("Device {} provided by user {} is invalid.",imei,userId);
            return error(ResultVo.ResultCode.DEVICE_INVALID_ERROR);
        }
        String logName=imei+"-"+logTime;
        IotLog iotLog=logService.getIotLogByLogName(logName);
        if (iotLog==null){
            logger.error("Command log {} is null.",logName);
            return error(ResultVo.ResultCode.IOT_LOG_NOT_FOUND_ERROR);
        }
        return success(iotLog);
    }
}

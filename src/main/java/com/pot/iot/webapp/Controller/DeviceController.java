package com.pot.iot.webapp.Controller;

import com.amazonaws.services.iot.client.AWSIotException;
import com.pot.iot.webapp.Entity.DeviceInfo;
import com.pot.iot.webapp.Entity.ResultVo;
import com.pot.iot.webapp.Entity.UserDevice;
import com.pot.iot.webapp.Repository.DeviceInfoRepository;
import com.pot.iot.webapp.Repository.UserDeviceRepository;
import com.pot.iot.webapp.UserController;
import com.pot.iot.webapp.Util.AWSIotUtil;
import com.sun.org.apache.regexp.internal.RE;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
public class DeviceController extends BaseController{
    @Autowired
    private UserDeviceRepository userdeviceRepository;
    @Autowired
    private DeviceInfoRepository deviceInfoRepository;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private AWSIotUtil awsIotUtil;
    Logger logger = LoggerFactory.getLogger(DeviceController.class);

    @PostMapping("/addDevice")
    public ResultVo addDevice(HttpServletRequest request,
                              HttpServletResponse response,
                              @RequestBody Map<String,String>addDevice){
        String productId=addDevice.get("product_id");
        String iemi=addDevice.get("iemi");
        String pinCode=addDevice.get("pin_code");
        String name=addDevice.get("name");
        String description=addDevice.get("description");
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        String userId=value.toString();
        DeviceInfo deviceInfo=deviceInfoRepository.findDeviceInfoByProductIdAndIemi(productId,iemi);
        if (deviceInfo==null){
            logger.error("Product {} or iemi {} provided by user {} is invalid.",productId,iemi,userId);
            return error(ResultVo.ResultCode.DEVICE_INVALID_ERROR);
        }
        if (!pinCode.equals(deviceInfo.getPinCode())){
            logger.error("Pin Code {} of device {} provided by user {} is invalid.",pinCode,iemi,userId);
            return error(ResultVo.ResultCode.DEVICE_PIN_CODE_ERROR);
        }
        UserDevice userDevice=userdeviceRepository.findDeviceByIemiAndIsdelete(iemi,false);
        if (userDevice!=null){
            logger.error("Device {} provided by user {} has been already registered.",iemi,userId);
            return error(ResultVo.ResultCode.DEVICE_ALREADY_REGISTERED_ERROR);
        }
        userDevice=new UserDevice(iemi,userId,pinCode,name,description);
        userdeviceRepository.save(userDevice);
        logger.info("User {} has successfully added device {}.",userId,iemi);
        return success();
    }

    @PostMapping("/removeDevice")
    public ResultVo removeDevice(HttpServletRequest request,
                                 HttpServletResponse response,
                                 @RequestBody Map<String,String>removeDevice){
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        String userId=value.toString();
        String pinCode=removeDevice.get("pin_code");
        String iemi=removeDevice.get("iemi");
        UserDevice device=userdeviceRepository.findDeviceByUserIdAndIemiAndIsdelete(userId,iemi,false);
        if (device==null){
            logger.error("Device {} provided by user {} is invalid.",iemi,userId);
            return error(ResultVo.ResultCode.DEVICE_INVALID_ERROR);
        }
        if (!pinCode.equals(device.getPinCode())){
            logger.error("Pin Code {} of device {} provided by user {} is invalid.",pinCode,iemi,userId);
            return error(ResultVo.ResultCode.DEVICE_PIN_CODE_ERROR);
        }
        device.setIsdelete(true);
        userdeviceRepository.save(device);
        logger.info("User {} has successfully removed device {}.",userId,iemi);
        return success();
    }

    @GetMapping("/deviceList")
    public ResultVo getDeviceList(HttpServletRequest request,
                                  HttpServletResponse response){
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        String userId=value.toString();
        List<UserDevice> deviceList=userdeviceRepository.findDevicesByUserIdAndIsdelete(userId,false);
        logger.info("User {} has successfully got his device list.",userId);
        return success(deviceList);
    }

    @GetMapping("/device")
    public ResultVo getDeviceById(HttpServletRequest request,
                                  HttpServletResponse response){
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        String userId=value.toString();
        String iemi=request.getParameter("iemi");
        UserDevice userDevice=userdeviceRepository.findDeviceByUserIdAndIemiAndIsdelete(userId,iemi,false);
        if (userDevice==null){
            logger.error("Device {} provided by user {} is invalid.",iemi,userId);
            return error(ResultVo.ResultCode.DEVICE_INVALID_ERROR);
        }
        else {
            logger.info("User {} has successfully got his device {}.",userId,iemi);
            return success(userDevice);
        }
    }

    @PostMapping("/changeDeviceName")
    public ResultVo changeDeviceName(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestBody Map<String,String>deviceName){
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        String userId=value.toString();
        String iemi=deviceName.get("iemi");
        String name=deviceName.get("name");
        UserDevice userDevice=userdeviceRepository.findDeviceByUserIdAndIemiAndIsdelete(userId,iemi,false);
        if (userDevice==null){
            logger.error("Device {} provided by user {} is invalid.",iemi,userId);
            return error(ResultVo.ResultCode.DEVICE_INVALID_ERROR);
        }
        userDevice.setName(name);
        userdeviceRepository.save(userDevice);
        logger.info("User {} has successfully changed the device name of his device {}.",userId,iemi);
        return success(userDevice);
    }

    @PostMapping("/changeDeviceDescription")
    public ResultVo changeDeviceDescription(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestBody Map<String,String>deviceDescription){
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        String userId=value.toString();
        String iemi=deviceDescription.get("iemi");
        String description=deviceDescription.get("description");
        UserDevice userDevice=userdeviceRepository.findDeviceByUserIdAndIemiAndIsdelete(userId,iemi,false);
        if (userDevice==null){
            logger.error("Device {} provided by user {} is invalid.",iemi,userId);
            return error(ResultVo.ResultCode.DEVICE_INVALID_ERROR);
        }
        userDevice.setDescription(description);
        userdeviceRepository.save(userDevice);
        logger.info("User {} has successfully changed the device description of his device {}.",userId,iemi);
        return success(userDevice);
    }

    @PostMapping("/addDeviceCommand")
    public ResultVo addDeviceCommand(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestBody Map<String,String>deviceDescription){
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        String userId=value.toString();
        String iemi=deviceDescription.get("iemi");
        String command=deviceDescription.get("command");
        UserDevice userDevice=userdeviceRepository.findDeviceByUserIdAndIemiAndIsdelete(userId,iemi,false);
        if (userDevice==null){
            logger.error("Device {} provided by user {} is invalid.",iemi,userId);
            return error(ResultVo.ResultCode.DEVICE_INVALID_ERROR);
        }
        if (userDevice.getDeviceCommand().equals("")){
            userDevice.setDeviceCommand(command);
            userdeviceRepository.save(userDevice);
            logger.info("User {} has successfully added a command {} to his device {}.",userId,command,iemi);
            return success();
        }
        else {
            userDevice.setDeviceCommand(userDevice.getDeviceCommand()+";"+command);
            userdeviceRepository.save(userDevice);
            logger.info("User {} has successfully added a command {} to his device {}.",userId,command,iemi);
            return success();
        }
    }
//    @PostMapping("/changeCommunicationMode")
//    public ResultVo changeCommunicationMode(HttpServletRequest request,
//                                            HttpServletResponse response,
//                                            @RequestBody Map<String,String>communicationMode) throws AWSIotException {
//        String token=request.getParameter("token");
//        Object value = redisTemplate.opsForValue().get(token);
//        String userId=value.toString();
//        String iemi=communicationMode.get("iemi");
//        String mode=communicationMode.get("communication_mode");
//        UserDevice userDevice=userdeviceRepository.findDeviceByUserIdAndIemiAndIsdelete(userId,iemi,false);
//        if (userDevice==null){
//            return error(ResultVo.ResultCode.DEVICE_INVALID_ERROR);
//        }
//        if (mode.equals(CommunicationMode.SUCCESSIVE.getDesc())){
//            JSONObject state=awsIotUtil.getShadow(iemi).getJSONObject("state");
//            JSONObject desired=state.getJSONObject("desired");
//            String setting=desired.getString("settings");
//            String[] settings=setting.split(",");
//            String newSetting=settings[0]+","+mode+","+settings[2];
//            String newState = "{\"state\":{\"desired\":{\"settings\":\""+newSetting+"\"}}}";
//            awsIotUtil.putShadow(iemi,newState);

//            return success();
//        }
//        else if (mode.equals(CommunicationMode.SLEEP.getCode())||mode.equals(CommunicationMode.POWERSAVING.getCode())) {
//            JSONObject state = awsIotUtil.getShadow(deviceId).getJSONObject("state");
//            JSONObject desired = state.getJSONObject("desired");
//            String setting = desired.getString("settings");
//            String[] settings = setting.split(",");
//            if (!settings[2].equals("-1")) {
//                String newSetting = settings[0] + "," + mode + "," + settings[2];
//                String newState = "{\"state\":{\"desired\":{\"settings\":\""+newSetting+"\"}}}";
//                awsIotUtil.putShadow(deviceId,newState);
//                return success();
//            }
//            else return error(ResultVo.ResultCode.COMMUNICATION_INTERVAL_ERROR);
//        }
//        else return error(ResultVo.ResultCode.COMMUNICATION_MODE_ERROR);
//    }

//    @PostMapping("/changeCommunicationInterval")
//    public ResultVo changeCommunicationInterval(HttpServletRequest request,
//                                                HttpServletResponse response,
//                                                @RequestBody Map<String,String>communicationInterval) throws AWSIotException {
//        String token=request.getParameter("token");
//        Object value = redisTemplate.opsForValue().get(token);
//        String userId=value.toString();
//        String iemi=communicationInterval.get("iemi");
//        String interval=communicationInterval.get("communication_interval");
//        UserDevice userDevice=userdeviceRepository.findDeviceByUserIdAndIemiAndIsdelete(userId,iemi,false);
//        if (userDevice==null){
//            return error(ResultVo.ResultCode.DEVICE_INVALID_ERROR);
//        }
//        JSONObject state=awsIotUtil.getShadow(iemi).getJSONObject("state");
//        JSONObject desired=state.getJSONObject("desired");
//        String setting=desired.getString("settings");
//        String[] settings=setting.split(",");
//        if (settings[1].equals(CommunicationMode.SUCCESSIVE.getCode())){
//            return error(ResultVo.ResultCode.COMMUNICATION_INTERVAL_ERROR);
//        }
//        else if (settings[1].equals(CommunicationMode.SLEEP.getCode())
//                || settings[1].equals(CommunicationMode.POWERSAVING.getCode())){
//            String newSetting=settings[0]+","+settings[1]+","+interval;
//            String newState = "{\"state\":{\"desired\":{\"settings\":\""+newSetting+"\"}}}";
//            awsIotUtil.putShadow(iemi,newState);
//            return success();
//        }
//        else return error(ResultVo.ResultCode.COMMUNICATION_MODE_ERROR);
//    }

//    @PostMapping("/turnOnDevice")
//    public ResultVo turnOnDevice(HttpServletRequest request,
//                                 HttpServletResponse response,
//                                 @RequestBody Map<String,String>turnOn) throws AWSIotException {
//        String token=request.getParameter("token");
//        Object value = redisTemplate.opsForValue().get(token);
//        String userId=value.toString();
//        String iemi=turnOn.get("iemi");
//        UserDevice userDevice=userdeviceRepository.findDeviceByUserIdAndIemiAndIsdelete(userId,iemi,false);
//        if (userDevice==null){
//            return error(ResultVo.ResultCode.DEVICE_INVALID_ERROR);
//        }
//        JSONObject state=awsIotUtil.getShadow(iemi).getJSONObject("state");
//        JSONObject desired=state.getJSONObject("desired");
//        String setting=desired.getString("settings");
//        String[] settings=setting.split(",");
//        if (settings[0].equals("1")){
//            return error(ResultVo.ResultCode.DEVICE_ALREADY_ON_ERROR);
//        }
//        else {
//            String newSetting="1"+","+settings[1]+","+settings[2];
//            String newState = "{\"state\":{\"desired\":{\"settings\":\""+newSetting+"\"}}}";
//            awsIotUtil.putShadow(iemi,newState);
//            return success();
//        }
//    }

//    @PostMapping("/turnOffDevice")
//    public ResultVo turnOffDevice(HttpServletRequest request,
//                                 HttpServletResponse response,
//                                 @RequestBody Map<String,String>turnOff) throws AWSIotException {
//        String token=request.getParameter("token");
//        Object value = redisTemplate.opsForValue().get(token);
//        String userId=value.toString();
//        String iemi=turnOff.get("iemi");
//        UserDevice device=userdeviceRepository.findDeviceByUserIdAndIemiAndIsdelete(userId,deviceId,false);
//        if (device==null){
//            return error(ResultVo.ResultCode.DEVICE_INVALID_ERROR);
//        }
//        JSONObject state=awsIotUtil.getShadow(iemi).getJSONObject("state");
//        JSONObject desired=state.getJSONObject("desired");
//        String setting=desired.getString("settings");
//        String[] settings=setting.split(",");
//        if (settings[0].equals("0")){
//            return error(ResultVo.ResultCode.DEVICE_ALREADY_ON_ERROR);
//        }
//        else {
//            String newSetting="0"+","+settings[1]+","+settings[2];
//            String newState = "{\"state\":{\"desired\":{\"settings\":\""+newSetting+"\"}}}";
//            awsIotUtil.putShadow(iemi,newState);
//            return success();
//        }
//    }
}

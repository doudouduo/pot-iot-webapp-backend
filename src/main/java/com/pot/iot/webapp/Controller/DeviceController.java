package com.pot.iot.webapp.Controller;

import com.pot.iot.webapp.Entity.CommunicationMode;
import com.pot.iot.webapp.Entity.Device;
import com.pot.iot.webapp.Entity.ResultVo;
import com.pot.iot.webapp.Repository.DeviceRepository;
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
    private DeviceRepository deviceRepository;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/addDevice")
    public ResultVo addDevice(HttpServletRequest request,
                              HttpServletResponse response,
                              @RequestBody Map<String,String>addDevice){
        String iemi=addDevice.get("iemi");
        String pinCode=addDevice.get("pin_code");
        String name=addDevice.get("name");
        String description=addDevice.get("description");
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        String userId=value.toString();
        Device device=new Device();
        device.setIemi(iemi);
        device.setPinCode(pinCode);
        device.setName(name);
        device.setDescription(description);
        device.setUserId(userId);
        device=deviceRepository.save(device);
        return success(device);
    }

    @PostMapping("/removeDevice")
    public ResultVo removeDevice(HttpServletRequest request,
                                 HttpServletResponse response,
                                 @RequestBody Map<String,String>removeDevice){
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        String userId=value.toString();
        String pinCode=removeDevice.get("pin_code");
        String deviceId=removeDevice.get("device_id");
        Device device=deviceRepository.findDeviceByUserIdAndDeviceIdAndIsdelete(userId,deviceId,false);
        if (device==null){
            return error(ResultVo.ResultCode.DEVICE_INVALID_ERROR);
        }
        if (!pinCode.equals(device.getPinCode())){
            return error(ResultVo.ResultCode.DEVICE_PIN_CODE_ERROR);
        }
        device.setIsdelete(true);
        deviceRepository.save(device);
        return success();
    }

    @GetMapping("/deviceList")
    public ResultVo getDeviceList(HttpServletRequest request,
                                  HttpServletResponse response){
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        String userId=value.toString();
        List<Device> deviceList=deviceRepository.findDevicesByUserIdAndIsdelete(userId,false);
        return success(deviceList);
    }

    @GetMapping("/device")
    public ResultVo getDeviceById(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @RequestBody Map<String,String>devices){
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        String userId=value.toString();
        String deviceId=devices.get("device_id");
        Device device=deviceRepository.findDeviceByUserIdAndDeviceIdAndIsdelete(userId,deviceId,false);
        if (device==null){
            return error(ResultVo.ResultCode.DEVICE_INVALID_ERROR);
        }
        else return success(device);
    }

    @PostMapping("/changeDeviceName")
    public ResultVo changeDeviceName(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestBody Map<String,String>deviceName){
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        String userId=value.toString();
        String deviceId=deviceName.get("device_id");
        String name=deviceName.get("name");
        Device device=deviceRepository.findDeviceByUserIdAndDeviceIdAndIsdelete(userId,deviceId,false);
        if (device==null){
            return error(ResultVo.ResultCode.DEVICE_INVALID_ERROR);
        }
        device.setName(name);
        deviceRepository.save(device);
        return success(device);
    }

    @PostMapping("/changeDeviceDescription")
    public ResultVo changeDeviceDescription(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestBody Map<String,String>deviceDescription){
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        String userId=value.toString();
        String deviceId=deviceDescription.get("device_id");
        String description=deviceDescription.get("description");
        Device device=deviceRepository.findDeviceByUserIdAndDeviceIdAndIsdelete(userId,deviceId,false);
        if (device==null){
            return error(ResultVo.ResultCode.DEVICE_INVALID_ERROR);
        }
        device.setDescription(description);
        deviceRepository.save(device);
        return success(device);
    }

    @PostMapping("/changeCommunicationMode")
    public ResultVo changeCommunicationMode(HttpServletRequest request,
                                            HttpServletResponse response,
                                            @RequestBody Map<String,String>communicationMode){
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        String userId=value.toString();
        String deviceId=communicationMode.get("device_id");
        String mode=communicationMode.get("communication_mode");
        Device device=deviceRepository.findDeviceByUserIdAndDeviceIdAndIsdelete(userId,deviceId,false);
        if (device==null){
            return error(ResultVo.ResultCode.DEVICE_INVALID_ERROR);
        }
        if (mode.equals(CommunicationMode.SUCCESSIVE.getDesc())){
            device.setCommunicationMode(CommunicationMode.SUCCESSIVE.getCode());
            deviceRepository.save(device);
            return success();
        }
        else if (mode.equals(CommunicationMode.SLEEP.getDesc())){
            if (device.getCommunicationInterval()>0){
                device.setCommunicationMode(CommunicationMode.SLEEP.getCode());
                deviceRepository.save(device);
                return success();
            }
            else return error(ResultVo.ResultCode.COMMUNICATION_INTERVAL_ERROR);
        }
        else return error(ResultVo.ResultCode.COMMUNICATION_MODE_ERROR);
    }

    @PostMapping("/changeCommunicationInterval")
    public ResultVo changeCommunicationInterval(HttpServletRequest request,
                                                HttpServletResponse response,
                                                @RequestBody Map<String,String>communicationInterval){
        String token=request.getParameter("token");
        Object value = redisTemplate.opsForValue().get(token);
        String userId=value.toString();
        String deviceId=communicationInterval.get("device_id");
        Integer interval=Integer.parseInt(communicationInterval.get("communication_interval"));
        Device device=deviceRepository.findDeviceByUserIdAndDeviceIdAndIsdelete(userId,deviceId,false);
        if (device.getCommunicationMode()==CommunicationMode.SUCCESSIVE.getCode()){
            return error(ResultVo.ResultCode.COMMUNICATION_INTERVAL_ERROR);
        }
        else if (device.getCommunicationMode()==CommunicationMode.SLEEP.getCode()
                || device.getCommunicationMode()==CommunicationMode.POWERSAVING.getCode()){
            device.setCommunicationInterval(interval);
            deviceRepository.save(device);
            return success();
        }
        else return error(ResultVo.ResultCode.COMMUNICATION_MODE_ERROR);
    }


}

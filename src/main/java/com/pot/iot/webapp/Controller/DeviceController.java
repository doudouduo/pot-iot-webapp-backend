package com.pot.iot.webapp.Controller;

import com.pot.iot.webapp.Entity.Device;
import com.pot.iot.webapp.Entity.ResultVo;
import com.pot.iot.webapp.Repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DeviceController extends BaseController{
    @Autowired
    private DeviceRepository deviceRepository;

    @PostMapping("/addDevice")
    public ResultVo addDevice(@RequestBody Map<String,String>addDevice){
        String iemi=addDevice.get("iemi");
        String pinCode=addDevice.get("pin_code");
        String name=addDevice.get("name");
        String description=addDevice.get("description");
        String userId=addDevice.get("user_id");
        Device device=new Device();
        device=deviceRepository.save(device);
        return success(device);
    }
}

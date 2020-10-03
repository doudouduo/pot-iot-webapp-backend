package com.pot.iot.webapp.Repository;

import com.pot.iot.webapp.Entity.Device;
import com.pot.iot.webapp.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, Integer> {
    List<Device> findDevicesByUserIdAndIsdelete(String userid,boolean isdelete);
    Device findDeviceByUserIdAndDeviceIdAndIsdelete(String userid,String deviceid,boolean isdelete);
}

package com.pot.iot.webapp.Repository;

import com.pot.iot.webapp.Entity.DeviceInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceInfoRepository extends JpaRepository<DeviceInfo, String> {
    DeviceInfo findDeviceInfoByProductIdAndImei(String productid,String imei);
}

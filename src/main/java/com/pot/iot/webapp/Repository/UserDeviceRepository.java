package com.pot.iot.webapp.Repository;

import com.pot.iot.webapp.Entity.UserDevice;
import com.pot.iot.webapp.Entity.UserDevicePK;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDeviceRepository extends JpaRepository<UserDevice, UserDevicePK> {
    List<UserDevice> findDevicesByUserIdAndIsdelete(String userid,boolean isdelete);
    UserDevice findDeviceByUserIdAndImeiAndIsdelete(String userid,String imei,boolean isdelete);
    UserDevice findDeviceByImeiAndIsdelete(String imei,boolean isdelete);
}

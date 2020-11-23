package com.pot.iot.webapp.Repository;

import com.pot.iot.webapp.Entity.UserDevice;
import com.pot.iot.webapp.Entity.UserDevicePK;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDeviceRepository extends JpaRepository<UserDevice, UserDevicePK> {
    List<UserDevice> findDevicesByUserIdAndIsdelete(String userid,boolean isdelete);
    UserDevice findDeviceByUserIdAndIemiAndIsdelete(String userid,String iemi,boolean isdelete);
    UserDevice findDeviceByIemiAndIsdelete(String iemi,boolean isdelete);
}

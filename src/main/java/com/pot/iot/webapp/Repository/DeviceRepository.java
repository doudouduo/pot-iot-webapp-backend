package com.pot.iot.webapp.Repository;

import com.pot.iot.webapp.Entity.Device;
import com.pot.iot.webapp.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Integer> {
}

package com.pot.iot.webapp.Repository;

import com.pot.iot.webapp.Entity.IotLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface IotLogRepository extends MongoRepository<IotLog, String> {
    IotLog findIotLogByLogName(String logname);
    List<IotLog> findIotLogsByLogNameContaining(String imei);
}

package com.pot.iot.webapp.Repository;

import com.pot.iot.webapp.Entity.CommandLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommandLogRepository extends MongoRepository<CommandLog, String> {
    CommandLog findCommandLogByLogName(String logname);
    List<CommandLog> findCommandLogsByLogNameContaining(String imei);
}

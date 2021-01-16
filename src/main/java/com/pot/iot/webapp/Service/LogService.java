package com.pot.iot.webapp.Service;

import com.pot.iot.webapp.Entity.CommandLog;
import com.pot.iot.webapp.Entity.IotLog;
import com.pot.iot.webapp.Repository.CommandLogRepository;
import com.pot.iot.webapp.Repository.IotLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
@Component
public class LogService {
    @Autowired
    private IotLogRepository iotLogRepository;
    @Autowired
    private CommandLogRepository commandLogRepository;

    public void addCommandLog(String imei,String command){
        String logName=getLogname(imei);
        CommandLog commandLog=commandLogRepository.findCommandLogByLogName(logName);
        if (commandLog==null){
            commandLog=new CommandLog();
            LocalDateTime dateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String logTime=dateTime.format(formatter);
            commandLog.setLog(logTime+": "+command+"\n");
            commandLogRepository.save(commandLog);
        }
        else{
            LocalDateTime dateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String logTime=dateTime.format(formatter);
            commandLog.setLog(commandLog.getLog()+logTime+": "+command+"\n");
            commandLogRepository.save(commandLog);
        }
    }

    public List<CommandLog> getCommandLogByImei(String imei){
        return commandLogRepository.findCommandLogsByLogNameContaining(imei);
    }

    public CommandLog getCommandLogByLogName(String logName){
        return commandLogRepository.findCommandLogByLogName(logName);
    }

    public List<IotLog> getIotLogByImei(String imei){
        return iotLogRepository.findIotLogsByLogNameContaining(imei);
    }

    public IotLog getIotLogByLogName(String logName){
        return iotLogRepository.findIotLogByLogName(logName);
    }

    private String getLogname(String imei){
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String logName=imei+"-"+date.format(formatter);
        return logName;
    }
}

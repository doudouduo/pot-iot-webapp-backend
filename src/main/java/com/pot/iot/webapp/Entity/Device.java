package com.pot.iot.webapp.Entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
public class Device {
    @Id
    @JSONField(name="device_id")
    private String deviceId;
    private String iemi;
    @JSONField(name="user_id")
    private String userId;
    @JSONField(name="pin_code")
    private String pinCode;
    private String name;
    private String description;
    @JSONField(name="device_status")
    private String deviceStatus;
    @JSONField(name="communication_mode")
    private int communicationMode;
    @JSONField(name="signal_strength")
    private int signalStrength;
    private String battery;
    private String certificate;
    @JSONField(name="public_key")
    private String publicKey;
    @JSONField(name="private_key")
    private String privateKey;
    private String gps;
    @JSONField(name="led_status")
    private int ledStatus;
    private String acceleration;
    @JSONField(name="communication_interval")
    private int communicationInterval;
    @JSONField(name="on_time")
    private Timestamp onTime;
    @JSONField(name="off_time")
    private Timestamp offTime;
    @JSONField(name="last_communication")
    private Timestamp lastCommunication;
    @JSONField(name="device_command")
    private String deviceCommand;
    @JSONField(name="mode_command")
    private String modeCommand;
    private boolean isdelete;
    @CreatedDate
    @JSONField(name="create_time")
    private Timestamp createTime;
    @LastModifiedDate
    @JSONField(name="update_time")
    private Timestamp updateTime;

    public Device(){
        description="";
        deviceStatus="";
        communicationMode=-1;
        signalStrength=-1;
        battery="";
        certificate="";
        publicKey="";
        privateKey="";
        gps="";
        ledStatus=-1;
        acceleration="";
        communicationInterval=-1;
        onTime=new Timestamp(new Date(0).getTime());
        offTime=new Timestamp(new Date(0).getTime());
        lastCommunication=new Timestamp(new Date(0).getTime());
        deviceCommand="";
        modeCommand="";
        isdelete=false;
    }
}

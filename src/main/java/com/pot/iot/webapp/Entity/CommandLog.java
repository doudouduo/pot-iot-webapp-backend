package com.pot.iot.webapp.Entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.EntityListeners;
import javax.persistence.Id;
import java.sql.Timestamp;

@Document(collection = "CommandLog")
@Data
@EntityListeners(AuditingEntityListener.class)
public class CommandLog {
    @Id
    private String _id;
    @JSONField(name="log_name")
    private String logName;
    private String log;
    @CreatedDate
    @JSONField(name="create_time")
    private Timestamp createTime;
    @LastModifiedDate
    @JSONField(name="update_time")
    private Timestamp updateTime;

    public CommandLog(){
        super();
        this.logName="";
        this.log="";
    }

}

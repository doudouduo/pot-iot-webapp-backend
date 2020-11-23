package com.pot.iot.webapp.Entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
public class DeviceInfo {
    @Id
    private String iemi;
    @JSONField(name="pin_code")
    private String pinCode;
    @JSONField(name="product_id")
    private String productId;
    @JSONField(name="create_time")
    private Timestamp createTime;
    private String version;
    private int status;

    public DeviceInfo(){}
}

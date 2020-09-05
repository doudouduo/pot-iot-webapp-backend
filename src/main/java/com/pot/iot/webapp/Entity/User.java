package com.pot.iot.webapp.Entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by echisan on 2018/6/23
 */
@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @JSONField(name="user_id")
    private String userId;
    private String username;
    private String email;
    private String password;
    @JSONField(name="account_status")
    private boolean accountStatus;
    private boolean isdelete;
    @CreatedDate
    @JSONField(name="create_time")
    private Timestamp createTime;
    @LastModifiedDate
    @JSONField(name="update_time")
    private Timestamp updateTime;

    @Override
    public String toString() {
        return "User{" +
                "id=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    public User(String userId,String username,String email,String password){
        this.userId=userId;
        this.username=username;
        this.email=email;
        this.password=password;
        accountStatus=false;
        isdelete=false;
    }

    public User(){
        accountStatus=false;
        isdelete=false;
    }
}

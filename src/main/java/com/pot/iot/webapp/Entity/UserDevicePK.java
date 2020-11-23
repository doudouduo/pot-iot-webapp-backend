package com.pot.iot.webapp.Entity;

import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
public class UserDevicePK implements Serializable {
    private String iemi;
    private String userId;

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((userId == null) ? 0 : userId.hashCode());
        result = PRIME * result + ((iemi == null) ? 0 : iemi.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(getClass() != obj.getClass()){
            return false;
        }

        final UserDevicePK other = (UserDevicePK) obj;
        if(userId == null){
            if(other.userId != null){
                return false;
            }
        }else if(!userId.equals(other.userId)){
            return false;
        }
        if(iemi == null){
            if(other.iemi != null){
                return false;
            }
        }else if(!iemi.equals(other.iemi)){
            return false;
        }
        return true;
    }
    
    public UserDevicePK(){}
}

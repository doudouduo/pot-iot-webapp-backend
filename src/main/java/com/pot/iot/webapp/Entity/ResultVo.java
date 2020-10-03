package com.pot.iot.webapp.Entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultVo {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ResultCodeVo{
        @JSONField(name ="result_code")
        private Integer resultCode;
        @JSONField(name ="message")
        private String message;
    }

    public enum  ResultCode{

        SUCCESS("SUCCESS",0),
        EMAIL_DUPLICATE_ERROR("EMAIL_DUPLICATE_ERROR",1),
        EMAIL_INVALID_ERROR("EMAIL_INVALID_ERROR",2),
        REGISTRATION_EMAIL_ERROR("REGISTRATION_EMAIL_ERROR",3),
        ACCOUNT_INACTIVATE_ERROR("ACCOUNT_INACTIVATE_ERROR",4),
        WRONG_PASSWORD_ERROR("WRONG_PASSWORD_ERROR",5),
        TOKEN_AURHENTICATION_ERROR("TOKEN_AURHENTICATION_ERROR",6),
        FORGET_PASSWORD_EMAIL_ERROR("FORGET_PASSWORD_EMAIL_ERROR",7),
        DEVICE_INVALID_ERROR("DEVICE_INVALID_ERROR",8),
        DEVICE_PIN_CODE_ERROR("DEVICE_PIN_CODE_ERROR",9),
        COMMUNICATION_INTERVAL_ERROR("COMMUNICATION_INTERVAL_ERROR",10),
        COMMUNICATION_MODE_ERROR("COMMUNICATION_MODE_ERROR",12),
        ;
        ResultCode(String message, Integer resultCode) {
            this.resultCode = resultCode;
            this.message = message;
        }

        private Integer resultCode;
        private String message;

        public Integer getResultCode() {
            return resultCode;
        }

        public ResultCode setErrorCode(Integer resultCode) {
            this.resultCode = resultCode;
            return this;
        }

        public void setResultCode(Integer resultCode) {
            this.resultCode = resultCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public ResultCodeVo toVo() {
            return new ResultCodeVo(this.resultCode, this.message);
        }
    }

    @JSONField(name ="status_code")
    private Integer statusCode;
    private Boolean success;
    private Object data;
    @JSONField(name ="result")
    private ResultCodeVo result;



}

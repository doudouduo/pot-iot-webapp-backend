package com.pot.iot.webapp.Controller;

import com.pot.iot.webapp.Entity.ResultVo;
import org.springframework.stereotype.Component;

@Component
public class BaseController {
    public ResultVo success(Object data){
        return new ResultVo(200, true, data, ResultVo.ResultCode.SUCCESS.toVo());
    }

    public ResultVo success(){
        return new ResultVo(200, true, null, ResultVo.ResultCode.SUCCESS.toVo());
    }

    public ResultVo success(ResultVo.ResultCode resultCode, Object data){
        return new ResultVo(200, true, data, resultCode.toVo());
    }

    public ResultVo error(ResultVo.ResultCode resultCode){
        return new ResultVo(200, false, null, resultCode.toVo());
    }

    public ResultVo error(ResultVo.ResultCode resultCode, Object data){
        return new ResultVo(200, false, data, resultCode.toVo());
    }
}

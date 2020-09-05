package com.pot.iot.webapp.Controller;

import com.pot.iot.webapp.Entity.ResultVo;
import com.pot.iot.webapp.Util.CmdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CompileController extends BaseController {
    @Autowired
    private CmdUtil cmdUtil;

    @PostMapping(value = "/make")
    public ResultVo runMake(@RequestBody Map<String,String> compileArgs) {
        String compilePath=compileArgs.get("compile_path");
        cmdUtil.exeCmd(compilePath);
        return success();
    }
}

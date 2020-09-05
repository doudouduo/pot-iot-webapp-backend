package com.pot.iot.webapp.Util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class CmdUtil {
    public void exeCmd(String path){
        // TODO Auto-generated method stub
        Process proc;
        try {
            String resource_path=System.getProperty("user.dir")+"/src/main/resources/";
            String command="python3 "+resource_path+"compile.py "+path;
            System.out.println(command);
            proc = Runtime.getRuntime().exec(command);// 执行py文件
            //用输入输出流来截取结果
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            proc.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

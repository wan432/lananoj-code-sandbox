package com.lanan.lananojcodesandbox.utils;

import cn.hutool.core.date.StopWatch;
import com.lanan.lananojcodesandbox.model.ExecuteMessage;

import java.io.*;

public class ProccessUtils {
    public static ExecuteMessage runProcessAndGEtMessage(Process runProcess, String opName) throws IOException, InterruptedException {
        ExecuteMessage message = new ExecuteMessage();

           StopWatch stopWatch=new StopWatch();
           stopWatch.start();
        //等待程序执行，获取错误码
        int exitValue = runProcess.waitFor();
        message.setExitValue(exitValue);
        //正常退出
        if (exitValue == 0) {
            System.out.println(opName + "成功");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
            StringBuilder compileOutputStringBuilde = new StringBuilder();
            //逐行读取
            String compileOutputLine;
            while ((compileOutputLine = bufferedReader.readLine()) != null) {
                compileOutputStringBuilde.append(compileOutputLine);
            }
            message.setMessage(compileOutputStringBuilde.toString());
        } else {
            System.out.println("编译失败，错误码:" + exitValue);
            BufferedReader errorbufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
            StringBuilder errorcompileOutputStringBuilde = new StringBuilder();
            //逐行读取
            String errorcompileOutputLine;
            while ((errorcompileOutputLine = errorbufferedReader.readLine()) != null) {
                errorcompileOutputStringBuilde.append(errorcompileOutputLine);
            }
            message.setErrormessage(errorcompileOutputStringBuilde.toString());
        }
        stopWatch.stop();
        message.setTime(stopWatch.getLastTaskTimeMillis());
        return message;
    }

    //交互式
    public static ExecuteMessage runInterProcessAndGEtMessage(Process runProcess,String args) throws IOException, InterruptedException {
        ExecuteMessage message = new ExecuteMessage();
        StopWatch stopWatch=new StopWatch();
        InputStream inputStream = runProcess.getInputStream();
        OutputStream outputStream = runProcess.getOutputStream();
        //向控制台输入数据
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        String[] s=args.split(" ");
        outputStreamWriter.write(String.join("\n", s)+"\r\n");
        outputStreamWriter.flush();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder compileOutputStringBuilde=new StringBuilder();
        //逐行读取
        String compileOutputLine ;
        while ((compileOutputLine= bufferedReader.readLine())!=null)
        {
            compileOutputStringBuilde.append(compileOutputLine);
        }
        message.setMessage(compileOutputStringBuilde.toString());
        stopWatch.stop();
        message.setTime(stopWatch.getLastTaskTimeMillis());
        outputStreamWriter.close();
        outputStream.close();
        inputStream.close();
        runProcess.destroy();
        return message;
    }

}

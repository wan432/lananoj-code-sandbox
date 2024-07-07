package com.lanan.lananojcodesandbox;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import com.lanan.lananojcodesandbox.model.ExecuteCodeRequest;
import com.lanan.lananojcodesandbox.model.ExecuteCodeResponse;
import com.lanan.lananojcodesandbox.model.ExecuteMessage;
import com.lanan.lananojcodesandbox.model.Judgeinfo;
import com.lanan.lananojcodesandbox.utils.ProccessUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class JavaNativeCodeSandbox implements CodeSandbox {

    private static final String GLOBAL_CODE_NAME = "tmpCode";
    private static final String GLOBAL_JAVA_CLASS_NAME= "Main.java";

    public static void main(String[] args) throws IOException, InterruptedException {
        JavaNativeCodeSandbox javaNativeCodeSandbox=new JavaNativeCodeSandbox();
        ExecuteCodeRequest executeCodeRequest=new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2","1 3"));
        String code = ResourceUtil.readStr("testCode.simpleCopuetArgs/Main.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);

    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        String property = System.getProperty("user.dir");
        String globalPathName=property+ File.separator+GLOBAL_CODE_NAME;
        //判断全局代码目录是否存在
        if(!FileUtil.exist(globalPathName)){
            FileUtil.mkdir(globalPathName);
        }
        //将用户的代码隔离存放
        String useCodeParentPath=globalPathName+File.separator+ UUID.randomUUID();
        String useCodePath=useCodeParentPath+File.separator+GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, useCodePath, StandardCharsets.UTF_8);

        //编译代码，得到class文件
        String compileCmd=String.format("javac -encoding utf-8 %s",userCodeFile.getAbsolutePath());
        ExecuteMessage executeMessage = null;
        try {
            Process spocess=Runtime.getRuntime().exec(compileCmd);
            executeMessage = ProccessUtils.runProcessAndGEtMessage(spocess, "编译");
            System.out.println(executeMessage);
        } catch (Exception e) {
           return getErrorResponse(e);
        }
        //等待程序执行，获取错误码

        List<ExecuteMessage> executeMessageList=new ArrayList<>();
        for (String inputArgs : inputList) {
            String runCmd=String.format("java -Dfile.encoding=UTF-8 -cp %s Main %s",useCodeParentPath,inputArgs);
            ExecuteMessage executeMessage1 = null;
            try {
                Process runProcess= Runtime.getRuntime().exec(runCmd);
                executeMessage1 = ProccessUtils.runProcessAndGEtMessage(runProcess, "运行");
                //ExecuteMessage executeMessage1 = ProccessUtils.runInterProcessAndGEtMessage(runProcess,inputArgs);
                System.out.println(executeMessage1);
            } catch (Exception e) {
               return getErrorResponse(e);
            }

            executeMessageList.add(executeMessage1);
        }

        ExecuteCodeResponse executeCodeResponse=new ExecuteCodeResponse();
        List<String> outputList =new ArrayList<>();
        long maxTime=-1;
        for (ExecuteMessage excutemessage : executeMessageList) {
            String errormessage = executeMessage.getErrormessage();
            if(StrUtil.isNotBlank(excutemessage.getErrormessage())){
               executeCodeResponse.setMessage(errormessage);
               //用户提交的代码执行中存在错误
               executeCodeResponse.setStatus(3);
               break;
            }
            if(excutemessage.getTime()!=null)
            {
                maxTime=Math.max(maxTime,excutemessage.getTime());
            }
            outputList.add(excutemessage.getMessage());
        }
        if(outputList.size()==executeMessageList.size()){
            executeCodeResponse.setStatus(1);
        }
        executeCodeResponse.setOutputList(outputList);
/*        executeCodeResponse.setMessage();*/
       // executeCodeResponse.setStatus(1);
        Judgeinfo judgeinfo=new Judgeinfo();
       // judgeinfo.setMessage();
        judgeinfo.setTime(maxTime);
       // judgeinfo.setMemory();

        executeCodeResponse.setJudgeinfo(judgeinfo);
       //文件清理
        if(userCodeFile.getParentFile()!=null)
        {
            boolean del = FileUtil.del(useCodeParentPath);
            System.out.println("删除"+(del ? "成功":"失败"));
        }


        return executeCodeResponse;
    }

    private ExecuteCodeResponse getErrorResponse(Throwable e){
        ExecuteCodeResponse executeCodeResponse=new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeinfo(new Judgeinfo());
        return executeCodeResponse;
    }
}

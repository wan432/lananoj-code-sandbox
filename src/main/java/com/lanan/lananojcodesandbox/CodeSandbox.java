package com.lanan.lananojcodesandbox;


import com.lanan.lananojcodesandbox.model.ExecuteCodeRequest;
import com.lanan.lananojcodesandbox.model.ExecuteCodeResponse;

import java.io.IOException;

/*
  代码沙箱接口定义
 */
public interface CodeSandbox {

    /*
      执行代码
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest excuteCodeRequest) throws IOException, InterruptedException;
}

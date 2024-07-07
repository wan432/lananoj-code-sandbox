package com.lanan.lananojcodesandbox.model;

import lombok.Data;

/*
  进城执行信息
 */
@Data
public class ExecuteMessage {
    private Integer exitValue;
    private String message;
    private String Errormessage;
    private Long time;
}

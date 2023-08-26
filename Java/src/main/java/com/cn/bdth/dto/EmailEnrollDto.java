package com.cn.bdth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 雨纷纷旧故里草木深
 *
 * @author 时间海 @github dulaiduwang003
 * @version 1.0
 */
@Data
@Accessors(chain = true)
public class EmailEnrollDto {


    @Email(message = "邮箱格式错误")
    private String email;

    @Size(min = 1, max = 20, message = "登陆密码格式错误")
    private String password;

    private String code;
}

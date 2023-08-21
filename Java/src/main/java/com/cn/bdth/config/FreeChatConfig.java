package com.cn.bdth.config;


import com.cn.bdth.model.ClaudeModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 雨纷纷旧故里草木深
 *
 * @author 时间海 @github dulaiduwang003
 * @version 1.0
 */
@ConfigurationProperties(prefix = "gpt.free")
@Component
@Data
@EnableConfigurationProperties
public class FreeChatConfig {



    private ClaudeModel claudeModel;
}

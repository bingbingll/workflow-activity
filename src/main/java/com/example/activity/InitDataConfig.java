package com.example.activity;



import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * TODO description：数据初始化，提交状态前端提交需要如下汉字
 *
 * @author 李兵
 * @version v1.0
 * @CreateDate: 2018/10/5 15:44
 */
@Data
@Component
public class InitDataConfig {
    @Value("agree")
    private String agree;

    @Value("rejected")
    private String rejected;
}

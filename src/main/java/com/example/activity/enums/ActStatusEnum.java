package com.example.activity.enums;

import lombok.Getter;

/**
 * TODO description：审批结果类型
 *
 * @author 李兵
 * @version v1.0
 * @CreateDate: 2018/9/28 14:09
 */
@Getter
public enum ActStatusEnum {
    /**创建*/
    found(1, "编制"),
    /** 驳回*/
    rejected(2, "驳回"),
    /**同意*/
    agree(3, "同意"),
    /**待审批 */
    auditing(4, "待审批");

    private Integer code;
    private String value;

    ActStatusEnum(Integer code, String codeValue) {
        this.code = code;
        this.value = codeValue;
    }

}

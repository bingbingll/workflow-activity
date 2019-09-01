package com.example.activity.to;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * TODO description：审核记录表传输对象，用处为 ：流程页面展示流程记录信息。
 *
 * @author 李兵
 * @version v1.0
 * @CreateDate: 2018/10/14 20:37
 */
@Getter
@Setter
@ToString
public class ActRecordTO {
    /**审批状态*/
    private String status;
    /**流转时间*/
    private String operateTime;
    /**审批时间*/
    private String auditTime;
    /**任务节点名称*/
    private String task;
    /**经办人*/
    private String operateRealName;
    /**意见*/
    private String content;
    /**用户所属部门*/
    private String userDepId;
    private String userDepName;

}

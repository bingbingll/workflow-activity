package com.example.activity;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

/**
 * TODO description：流程审核历史记录及待审核记录，用于业务层，activity的历史记录并不能满足业务要求所以要自定义一个历史记录表。
 *
 * @author bing.li
 * @version v1.0
 * @date 2018/8/8 15:07
 */
@Entity
@Table(name = "sys_act_approved_memo")
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ActApprovedMemoPO implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    /**创建时间*/
    private String createTime;
    /**是否删除 0 1*/
    private Integer isDelete=0;
    /**是否展示*/
    protected Integer isHide=0;
    /**流转时间*/
    private String operateTime;
    /**经办者登陆账号*/
    private String operateName;
    /**经办者昵称*/
    private String operateNickName;
    /**经办人真实名称*/
    @Column(length = 300)
    private String operateRealName;
    /**审批状态 同意 驳回 待审批 创建*/
    @Column(name = "auditStatus")
    private String auditStatus;
    private Integer auditStatusCode;
    /**审核时间 默认为空*/
    private String auditTime;
    /**任务id*/
    private String taskId;
    /**任务名称*/
    private String taskName;
    /**任务类型*/
    private String taskType;
    /**标题 存放表单的名称或表单中的编号*/
    private String title;
    /**审批意见*/
    private String content;
    /**是否会签 0 是 1 否*/
    private Integer isCountersign=1;
    /**流程定义id*/
    @Column(length = 500)
    private String processInstanceId;
    /**审批所属表单id 这个存储的业务表单数据的id，不能为空。*/
    private String formId;
    private String formType;
    /**用户id*/
    private String userId;
    /**用户类型 1 企业 2 粮食局*/
    private String userTypeCode;
    private String userTypeValue;
    /**用户所属部门*/
    private String userDepId;
    private String userDepName;

}
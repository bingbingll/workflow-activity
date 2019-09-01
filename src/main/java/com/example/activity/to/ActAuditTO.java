package com.example.activity.to;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * TODO description：审核记录表传输对象，用处为 ：审核模块展示已审核及待审核状态信息
 *
 * @author 李兵
 * @version v1.0
 * @CreateDate: 2018/10/14 20:37
 */
@Getter
@Setter
@ToString
public class ActAuditTO {
    /**
     * 表单id
     */
    private String formId;
    /**
     * 审批状态
     */
    private String status;
    /**
     * 审批结果编码
     */
    private Integer statusCode;
    /**
     * 流转时间
     */
    private String operateTime;
    /**
     * 审批时间
     */
    private String auditTime;
    /**
     * 数据总条数
     */
    private Long totalElements;

    /**
     * 表单类型
     */
    private String formType;

    /**
     * 流程实例id
     */
    private String processInstanceId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 申请人姓名
     */
    private String applyUserName;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ActAuditTO) {
            ActAuditTO actAuditTO = (ActAuditTO) obj;
            return Long.valueOf(formId).equals(Long.valueOf(actAuditTO.getFormId()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(this.formId);
    }
}

package com.example.activity.to;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO description：流程请求封装参数
 * TO(Transfer Object) ，数据传输对象 不同的应用程序之间传输的对象
 * @author 李兵
 * @version v1.0
 * @CreateDate: 2018/10/3 23:43
 */
@Getter
@Setter
public class ActParameterTO {
    /**表单id*/
    private String businessKey;
    /**表单类型，可以是业务模块名称*/
    private String businessType;
    /**当前登录用户id*/
    private String userId;
    /**流程定义id*/
    private String processDefinitionKey;
    private String processDefinitionId;
    /**流程实例id*/
    private String processInstanceId;
    /**下一节点审核人id*/
    private String nextAuditorUserId;
    /**审批类型 同意，驳回*/
    private String isValue;
    /**审批意见*/
    private String message;
    /**流程线上的条件名称与值 例如流程中定义了 money 1000或2000或3000  type 1或2或3 等，必须要和流程定义中的名称一致。*/
    private Map<String,Object> conditionVariable=new HashMap<>(1);

    //------2018/10/14 新增用于查询参数------------
    /**当前页数*/
    private Integer pagination;
    /**页面大小*/
    private Integer size;
    /**标点id集合,多数用于审核页面的模糊查询等*/
//    @Deprecated
    private List<String> buKeys;
    /**审批状态 同意, 待审批, 驳回*/
    private String status;
    /**审批开始时间*/
    private String beginTime;
    /**审批结束时间*/
    private String endTime;
}

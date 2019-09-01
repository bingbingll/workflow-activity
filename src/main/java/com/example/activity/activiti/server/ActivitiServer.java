package com.example.activity.activiti.server;

import com.example.activity.ActApprovedMemoRepository;
import com.example.activity.ActUserPO;
import com.example.activity.ActUserRepository;
import com.example.activity.InitDataConfig;
import lombok.extern.log4j.Log4j2;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.activiti.engine.*;
import org.activiti.engine.task.Task;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;


/**
 * TODO description：activiti 工作流服务层
 *
 * @author bing.li
 * @version v1.0
 * @date 2018-09-27 11:56
 */
@Log4j2
@Service
public class ActivitiServer {
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    RuntimeService runtimeService;
    @Autowired
    TaskService taskService;
    @Autowired
    ProcessEngine processEngine;
    @Autowired
    ActUserRepository actUserRepository;
    @Autowired
    HistoryService historyService;
    @Autowired
    ActApprovedMemoRepository actApprovedMemoRepository;
    @Autowired
    InitDataConfig initDataConfig;
    @Autowired
    SpringProcessEngineConfiguration processEngineConfiguration;

    /**
     * 部署工程文件下的流程定义图
     *
     * @param url
     */
    private void createDeployment(String url) {
        repositoryService.createDeployment().name("这是一个测试").addClasspathResource("/processes/MyProcess.bpmn").addClasspathResource("/processes/MyProcess.png").deploy();
    }

    /**
     * 获取流程实例id
     *
     * @param name     部署的流程名称
     * @param category 归属单位
     * @param key      流程部署中的key
     * @return
     */
    public String getProcessDefinitionId(String name, String category, String key) {
        List<Deployment> deploymentList = repositoryService.createDeploymentQuery()
                .deploymentName(name)
                .deploymentCategory(category)
                .deploymentKey(key)
                .orderByDeploymenTime()
                .desc()
                .list();
        if (deploymentList.size() > 0) {
            String depId = deploymentList.get(0).getId();
            return repositoryService.createProcessDefinitionQuery().deploymentId(depId).singleResult().getId();
        }
        return null;

    }

    /**
     * 流程启动
     * TODO : 注意 当前人员为真实姓名，在用户添加时需要做重复判定
     *
     * @param businessKey
     * @param userId
     * @param processDefinitionKey
     * @return
     */
    @Deprecated
    public String start(String businessKey, String businessType, String userId, String processDefinitionKey) {
        ActUserPO po = actUserRepository.findById(userId).get();
        Map<String, Object> taskVariable = new HashMap<String, Object>(2);
        taskVariable.put("user", po.getRealName());
        taskVariable.put("formType", businessType);
        //获取 流程定义表：act_re_procdef 对象，这一步只会读取最新的版本
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).singleResult();
        //启动流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinition.getKey(), businessKey, taskVariable);
        return processInstance.getId();
    }

    public String startById(String businessKey, String businessType, String userId, String processDefinitionId) {
        ActUserPO po = new ActUserPO();
        try {
            po = actUserRepository.findById(userId).get();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("流程用户表中未发现id为[" + userId + "]的用户，本次业务数据参数为" +
                    "业务表单：" + businessKey + "；业务类型：" + businessType + "；用户id：" + userId + "；流程定义id：" + processDefinitionId + "；请根据提升信息进行数据维护。");
            return "未发现相关用户";
        }
        Map<String, Object> taskVariable = new HashMap<String, Object>(2);
        taskVariable.put("user", po.getRealName());
        taskVariable.put("formType", businessType);
        //获取 流程部署表：act_re_deployment 对象信息并以最新的部署时间为条件。
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinition.getKey(), businessKey, taskVariable);
        return processInstance.getProcessInstanceId();
    }

    /**
     * 提交功能
     * TODO : 注意 当前人员为真实姓名，在用户添加时需要做重复判定
     *
     * @param processInstanceId
     * @param nextAuditorUserId
     * @param userId
     * @param businessType      表单所属类型
     * @param conditionVariable 流程条件变量 ,bpmn 中定义的是什么名称这里就需要传什么名称例如 money，type 等
     */
    public Task submit(String processInstanceId, String nextAuditorUserId, String userId, String businessType, Map<String, Object> conditionVariable) throws Exception {
        ActUserPO po = actUserRepository.findById(userId).get();
        Task task = taskService.createTaskQuery()
                .taskAssignee(po.getRealName())
                .processInstanceId(processInstanceId)
                .singleResult();
        task.setOwner(businessType);
        taskService.saveTask(task);
        //审批人
        Map<String, Object> taskVariable = new HashMap<>(2);
        if (StringUtils.isNotEmpty(nextAuditorUserId)) {
            ActUserPO nextUserPO = actUserRepository.findById(nextAuditorUserId).get();
            taskVariable.put("user", nextUserPO.getRealName());
            taskVariable.put("formType", businessType);
            //设置流程变量为获取下一个审批人经过的网关提供判断条件
            conditionVariable.put("type", "科员");
            taskService.setVariable(task.getId(), "variable", conditionVariable);
        }
        taskService.complete(task.getId(), taskVariable, conditionVariable);
        return task;
    }

    /**
     * @param isValue
     * @param userId
     * @param processInstanceId
     * @param nextAuditorUserId
     * @param message
     * @param transientVariables
     */
    public Task taskAudit(String isValue,
                          String businessType,
                          String userId,
                          String processInstanceId,
                          String nextAuditorUserId,
                          String message,
                          Map<String, Object> transientVariables) throws Exception {
        ActUserPO po = actUserRepository.findById(userId).get();
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(po.getRealName()).processInstanceId(processInstanceId).orderByTaskCreateTime().asc().list();
        for (Task task : tasks) {
            System.out.println(task.getId() + "," + task.getName());
            Map variable = (Map<String, Object>) taskService.getVariable(task.getId(), "variable");
            //是否同意
            if (StringUtils.equals(isValue, initDataConfig.getAgree())) {
                Map<String, Object> taskVariable = new HashMap<>(2);
                //提交至下一个审批人
                if (StringUtils.isBlank(nextAuditorUserId)) {
                    taskVariable.put("formType", businessType);
                    taskService.complete(task.getId(), taskVariable, variable);
                } else {
                    ActUserPO AuditorUser = actUserRepository.findById(nextAuditorUserId).get();
                    //存储所属流程类型，和归属的任务类型
                    taskService.addComment(task.getId(), task.getProcessInstanceId(), isValue, message);
                    taskVariable.put("formType", businessType);
                    taskVariable.put("user", AuditorUser.getRealName());
                    task.setOwner(businessType);
                    taskService.saveTask(task);
                    taskService.complete(task.getId(), taskVariable, variable);
                }
            } else if (StringUtils.equals(isValue, initDataConfig.getRejected())) {
//                runtimeService.deleteProcessInstance(processInstanceId, message);
            } else {
                Assert.notNull(null, "审批异常！未获取到相关的条件。");
            }
        }
        if (tasks.size() > 0) {
            return tasks.get(0);
        } else {
            return null;
        }
    }

    /**
     * 获取待审批任务
     *
     * @param userId
     */
    public List<Map> getTasks(String userId) {
        ActUserPO po = actUserRepository.findById(userId).get();
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(po.getRealName()).orderByTaskCreateTime().desc().list();
        List<Map> list = new ArrayList<>(tasks.size());
        for (Task task : tasks) {
            Map map = new HashMap(5);
            map.put("owner", task.getOwner());
            map.put("taskName", task.getName());
            map.put("assignee", task.getAssignee());
            map.put("taskId", task.getId());
            String businessKey = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult().getBusinessKey();
            map.put("businessKey", businessKey);
            list.add(map);
        }
        return list;
    }

    /**
     * 获取已审批的任务
     *
     * @param userId
     */
    public List<Map> getHiTasks(String userId) {
        ActUserPO po = actUserRepository.findById(userId).get();
        List<HistoricTaskInstance> historicTaskInstanceList = historyService.createHistoricTaskInstanceQuery().taskAssignee(po.getRealName()).orderByHistoricTaskInstanceStartTime().desc().list();
        List<Map> list = new ArrayList<>(historicTaskInstanceList.size());
        for (HistoricTaskInstance historicTaskInstance : historicTaskInstanceList) {
            Map map = new HashMap(5);
            String processInstanceId = historicTaskInstance.getProcessInstanceId();
            map.put("owner", historicTaskInstance.getOwner());
            map.put("taskName", historicTaskInstance.getName());
            map.put("assignee", historicTaskInstance.getAssignee());
            map.put("taskId", historicTaskInstance.getId());
            String businessKey = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult().getBusinessKey();
            map.put("businessKey", businessKey);
            list.add(map);
        }
        return list;
    }


    /**
     * 获取以部署的流程信息
     * java8集合操作 https://www.jianshu.com/p/63d2db850a8d
     */
    public List<Deployment> getDeploymentList(String unitName) {

        List<Deployment> deploymentList = repositoryService.createDeploymentQuery()
                .deploymentCategory(unitName)
                .orderByDeploymenTime()
                .desc()
                .list();
        //临时存放流程的key
        List<String> keys = new ArrayList<>();
        List<Deployment> collect = deploymentList.stream()
                //去除重复的key对象
                .filter(
                        d -> {
                            boolean flag = !keys.contains(d.getKey());
                            if (flag) {
                                keys.add(d.getKey());
                            }
                            return flag;
                        }
                ).collect(Collectors.toList());

        return collect;
    }

    /**
     * @param unitName 单位名称
     * @param name     流程部署名称
     * @return
     */
    public List<Deployment> getDeploymentList(String unitName, String name) {
        return repositoryService.createDeploymentQuery().deploymentCategory(unitName).deploymentNameLike(name).list();
    }


    /**
     * 获取流程定义信息
     *
     * @param name  流程定义名称
     * @param key   流程定义key
     * @param depId 流程部署id
     * @return
     */
    public List<ProcessDefinition> getDefinitionList(String name, String key, String depId) {
        return repositoryService.createProcessDefinitionQuery()
                .processDefinitionName(name)
                .processDefinitionKey(key)
                .deploymentId(depId)
                .list();
    }

    public List<ProcessDefinition> getDefinitionList(Set<String> depIds) {
        return repositoryService.createProcessDefinitionQuery().deploymentIds(depIds).list();
    }

    public String getDefinitionId(String depId) {
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().deploymentId(depId).orderByProcessDefinitionVersion().desc().list();
        if (list.size() > 0) {
            return list.get(0).getId();
        }
        return "";
    }

    /**
     * 获取下一节点审批人
     *
     * @param userId
     * @param processInstanceId
     * @param transientVariables
     * @return
     */
    public List<ActUserPO> getNextAuditUser(String userId, String processInstanceId, Map<String, Object> transientVariables) throws Exception {
        ActUserPO po = actUserRepository.findById(userId).get();
        //获取当前任务
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).taskAssignee(po.getRealName()).orderByTaskCreateTime().asc().singleResult();

        if (ObjectUtils.allNotNull(task)) {
            return this.getNextAuditUnite(userId, task.getProcessDefinitionId(), task.getTaskDefinitionKey(), transientVariables);
        }
        return new ArrayList<ActUserPO>(0);
    }

    /**
     * 获取下一节点审批人,将多个方法合并到一起
     *
     * @param userId
     * @param processDefinitionId
     * @param taskDefKey
     * @param conditionVariable
     * @return
     */
    private List<ActUserPO> getNextAuditUnite(String userId, String processDefinitionId, String taskDefKey, Map<String, Object> conditionVariable) throws Exception {
        String nextId = "";
        // 根据流程定义id获取改流程所有节点信息，并返回流程对象：
        Process process = repositoryService.getBpmnModel(processDefinitionId).getProcesses().get(0);
        //获取所有的FlowElement信息
        Collection<FlowElement> flowElements = process.getFlowElements();
        //获取当前节点信息
        FlowElement flowElement = getFlowElementById(taskDefKey, flowElements);
        String nextNodeDocumentation = getNextNode(flowElement, flowElements, conditionVariable);
        if (StringUtils.isBlank(nextNodeDocumentation)) {
            return new ArrayList<ActUserPO>(0);
        }
        System.out.println(nextNodeDocumentation);
//        if (nextNodeDocumentation.equals("end")){
//            return null;
//        }
        List<ActUserPO> auditors = getAuditors(nextNodeDocumentation, userId);
        System.out.println(auditors);
        return auditors;
    }

    /**
     * 获取符合条件的节点信息
     *
     * @param taskDefKey
     * @param flowElements
     * @return
     */
    private FlowElement getFlowElementById(String taskDefKey, Collection<FlowElement> flowElements) {
        for (FlowElement flowElement : flowElements) {
            if (flowElement.getId().equals(taskDefKey)) {
                return flowElement;
            }
        }
        return null;
    }

    /**
     * 获取下一个节点对象信息
     *
     * @param transientVariables
     * @param flowElements
     * @return
     */
    private String getNextNode(FlowElement currentFlowElement, Collection<FlowElement> flowElements, Map<String, Object> transientVariables) throws Exception {
        //通过当前节点获取出线信息
        List<SequenceFlow> outGoingFlows = null;
        if (currentFlowElement instanceof UserTask) {
            outGoingFlows = ((UserTask) currentFlowElement).getOutgoingFlows();
        } else if (currentFlowElement instanceof ExclusiveGateway) {
            outGoingFlows = ((ExclusiveGateway) currentFlowElement).getOutgoingFlows();
        }
        if (outGoingFlows != null && outGoingFlows.size() > 0) {
            for (SequenceFlow outGoingFlow : outGoingFlows) {

                //首先对这条线有没有表达试做出判断
                String conditionExpression = outGoingFlow.getConditionExpression();
//                System.out.println(conditionExpression);
                if (StringUtils.isNotEmpty(conditionExpression)) {
                    //有表达式-对传入参数和线上表达式进行运算
                    if (!judgeCondition(conditionExpression, transientVariables)) {
                        //不匹配
                        continue;
                    }
                }
                //======匹配的话 通过这条线找出下一个节点
                //此处应该只有一条出线
                //下一个节点的id
                String nextFlowElementID = outGoingFlow.getTargetRef();
                //查询下一节点的信息
                FlowElement nextFlowElement = getFlowElementById(nextFlowElementID, flowElements);
                //如果下一个节点为 任务节点
                if (nextFlowElement instanceof UserTask) {
                    UserTask nextFlowElement1 = (UserTask) nextFlowElement;
                    String documentation = nextFlowElement1.getDocumentation();
                    return documentation;
                } else if (nextFlowElement instanceof ExclusiveGateway) {
                    String nextNode = getNextNode(nextFlowElement, flowElements, transientVariables);
                    return nextNode;
                } else if (nextFlowElement instanceof EndEvent) {
                    return null;
                }
            }
        }
        return null;
    }

    public boolean judgeCondition(String conditionExpression, Map map) throws Exception {
        if (StringUtils.isNotEmpty(conditionExpression)) {
            String temStr = StringUtils.substringAfterLast(conditionExpression, "{");
            String s = StringUtils.substringBeforeLast(temStr, "}");
            String replace = "";
            if (s.contains("money")) {
                replace = s.replace("money", map.get("money").toString());
            } else if (s.contains("type")) {
                replace = s.replace("type", map.get("type").toString());
            } else if (s.contains("their")) {
                replace = s.replace("their", map.get("their").toString());
            } else if (s.contains("result")) {
                replace = s.replace("result", map.get("result").toString());
            } else {
                Assert.isTrue(false, "流程中未定义变量名称");
            }
            //字符串判定 例如："科员=='调研员'" 结果返回true，原因字符串带了“ ' ”  单引号，所以流程中只要是 字符串的应该放在 '' 中间。
            if (replace.indexOf("'") != -1) {
                if (replace.indexOf("==") != -1) {
                    String[] split = replace.split("==");
                    String tag = split[0];
                    String val = split[1].replaceAll("\'", "");
                    return strEq(tag, val);
                }
                if (replace.indexOf("!=") != -1) {
                    String[] split = replace.split("!=");
                    String tag = split[0];
                    String val = split[1].replaceAll("\'", "");
                    return !strEq(tag, val);
                }
            } else {
                ExpressionParser parser = new SpelExpressionParser();
                return parser.parseExpression(replace).getValue(Boolean.class);
            }
        }
        return false;
    }

    private boolean strEq(String tag, String val) {
        // 字符串是否包含指定的字符； 返回所在位置，不包含返回-1
        // abcde.indexOf(abc) return true ;  0
        return val.indexOf(tag) != -1;
    }

    /***
     * 根据流程定义中的任务审批人中的documentation属性获取符合条件的审批人
     * @param documentation
     * @param userId
     * @return
     */
    private List<ActUserPO> getAuditors(String documentation, String userId) {
        Assert.notNull(documentation, "流程定义中的任务类型不能为空");
        Assert.notNull(userId, "当前登陆者id不能为空");
        ActUserPO po = actUserRepository.findById(userId).get();
        List<ActUserPO> list = new ArrayList<ActUserPO>();
        if (documentation.startsWith("personId")) {
            //指定任务处理人
            String userIdStr = documentation.replaceAll("personId:", "");
            ActUserPO userPO = actUserRepository.getOne(userIdStr);
            list.add(userPO);
        } else if (documentation.startsWith("curDepartment")) {
            //本部门的某个岗位
            String position = documentation.replaceAll("curDepartment:", "");
            List<ActUserPO> pos = actUserRepository.findByDepartmentIdAndUnitIdAndPositionAndEnabledOrderByIdDesc(po.getDepartmentId(), po.getUnitId(), position, 0);
            list = pos;
        } else if (documentation.startsWith("appointDepartment")) {
            //指定部门的某个岗位
            String[] strs = documentation.split(",");
            //部门id
            String depId = strs[0].replace("appointDepartment:", "");
            //岗位
            String pos = strs[1];
            list = actUserRepository.findByDepartmentIdAndUnitIdAndPositionAndEnabledOrderByIdDesc(depId, po.getUnitId(), pos, 0);
        }
        return list;
    }

    /**
     * 获取流程审批记录
     *
     * @param processInstanceId
     * @return
     */
    @Deprecated
    public List<Map> getAuditLogging(String processInstanceId) {
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).list();
        List<Map> result = new ArrayList<>(list.size());
        for (HistoricTaskInstance historicTaskInstance : list) {
            Map map = new HashMap(5);

            result.add(map);
        }
        return result;
    }

    /**
     * 获取流程图，带流程跟踪
     *
     * @param processInstanceId
     * @return
     */
    public InputStream getDiagram(String processInstanceId) {
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        String processDefinitionId = historicProcessInstance.getProcessDefinitionId();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

        List<String> activeActivityIds = new ArrayList<>();
//        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).activityType("userTask")
//                .orderByHistoricActivityInstanceStartTime().desc().list();
        List<HistoricActivityInstance> list = historyService.createNativeHistoricActivityInstanceQuery()
                .sql("SELECT * FROM act_hi_actinst WHERE PROC_INST_ID_ = #{piid} AND (ACT_TYPE_ = 'userTask' " +
                        "or ACT_TYPE_ = 'endEvent' ) " +
                        "ORDER BY START_TIME_ DESC").parameter("piid", processInstanceId).list();

        activeActivityIds.add(list.get(0).getActivityId());

        //通过引擎生成png图片，并标记当前节点,并把当前节点用红色边框标记出来，弊端和直接部署流程文件生成的图片问题一样-乱码！。
        DefaultProcessDiagramGenerator dpd = new DefaultProcessDiagramGenerator();
        InputStream imageStream = dpd.generateDiagram(bpmnModel, "宋体", "宋体", "宋体");
        //InputStream imageStream = defaultProcessDiagramGenerator.generateDiagram(bpmnModel, "png", activeActivityIds, activeActivityIds, "宋体", "宋体", "宋体", null, 1.5);
        return imageStream;
    }

    /**
     * 获取流程变量
     *
     * @param processInstanceId
     * @return
     */
    public Map<String, Object> getProcessVariable(String processInstanceId) {
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        Map<String, Object> variable = (Map<String, Object>) taskService.getVariable(task.getId(), "variable");
        return variable;
    }

}


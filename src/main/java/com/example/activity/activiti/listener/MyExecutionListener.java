package com.example.activity.activiti.listener;



/**
 * TODO description：
 *
 * @author 李兵
 * @version v1.0
 * @CreateDate: 2018/10/3 15:42
 */
public class MyExecutionListener implements ExecutionListener, TaskListener {
    /**
     * 执行监听器
     *
     * @param execution
     */
    @Override
    public void notify(DelegateExecution execution) {
        String eventName = execution.getEventName();
        //start
        if ("start".equals(eventName)) {
            System.out.println("start=========");
        } else if ("end".equals(eventName)) {
            System.out.println("end=========");
        } else if ("take".equals(eventName)) {
            System.out.println("take=========");
        }

    }

    /**
     * 任务监听器
     *
     * @param delegateTask
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        String description = delegateTask.getDescription();
        Object variable = delegateTask.getVariable("userId");
        String eventName = delegateTask.getEventName();
        if ("create".endsWith(eventName)) {
            System.out.println("create=========");
        } else if ("assignment".endsWith(eventName)) {
            System.out.println("assignment========");
        } else if ("complete".endsWith(eventName)) {
            System.out.println("complete===========");
        } else if ("delete".endsWith(eventName)) {
            System.out.println("delete=============");
        }
    }
}

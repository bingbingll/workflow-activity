package com.example.activity.activiti.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/***
 * @ClassName: TongZhiListener
 * @Description: TODO: 通知任务监听器。
 * @Auther: 李兵
 * @Date: 2019/4/18 10:48
 * @version : V1.0
 */

public class TongZhiListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        System.out.println("当前任务为通知任务监听器。进行数据转换，需要通知人事处");

    }
}

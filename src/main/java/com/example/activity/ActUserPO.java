package com.example.activity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * TODO description：用户表，用于同步业务库中的用户
 *
 * @author bing.li
 * @version v1.0
 * @date 2018/9/27 12:03
 */
@Table(name = "sys_act_user")
@Entity
@Data
@ToString
public class ActUserPO implements Serializable {
    @Id
    private String id;
    /**登陆者账号*/
    private String userName;
    /**昵称*/
    private String nickName;
    /**真实姓名*/
    private String realName;

    private String email;

    private String photo;
    /**所属部门名称*/
    private String departmentName;
    private String departmentId;
    /**所属单位名称*/
    private String unitName;
    private String unitId;
    /**所属岗位*/
    private String position;

    /**是否可用 0 不可用 1 可用*/
    private Integer enabled ;
}
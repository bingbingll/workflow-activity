package com.example.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

/**
 * TODO description：
 *
 * @author 李兵
 * @version v1.0
 * @date 2018/09/27 12:14
 */
@Repository
public interface ActUserRepository extends JpaRepository<ActUserPO, Serializable> {
    /**
     * 根据单位id及部门id获取符合条件的人员。
     * @param did
     * @param uid
     * @param enabled
     * @param position
     * @return
     */
    List<ActUserPO> findByDepartmentIdAndUnitIdAndPositionAndEnabledOrderByIdDesc(String did, String uid, String position, Integer enabled);

    /**
     * 根据单位id及部门id及岗位获取符合条件的人员
     * @param did
     * @param uid
     * @param enabled
     * @param pos
     * @return
     */
    List<ActUserPO> findByDepartmentNameAndUnitIdAndEnabledAndPositionOrderByIdDesc(String did, String uid, Integer enabled, String pos);

    List<ActUserPO> findByUnitIdAndEnabledAndPositionOrderByIdDesc(String uid, Integer enabled, String pos);

    List<ActUserPO> findByNickName(String realName);

    ActUserPO findByIdAndEnabled(String id, Integer enabled);
}

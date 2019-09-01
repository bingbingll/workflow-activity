package com.example.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

/**
 * TODO description：
 *
 * @author 李兵
 * @version v1.0
 * @date 2018/09/27 17:32
 */
@Repository
public interface ActApprovedMemoRepository extends JpaRepository<ActApprovedMemoPO, Serializable>, JpaSpecificationExecutor<ActApprovedMemoPO> {
    /**
     * 根据条件获取信息，包含result值的数据
     * @param name 用户登陆账号
     * @param delete 是否删除
     * @param hide 是否隐藏
     * @param status 审批结果
     * @return
     */
    List findByOperateNameAndIsDeleteAndIsHideAndStatusOrderByCreateTimeDesc(String name, Integer delete, Integer hide, String status);

    /**
     * 根据条件获取信息，不包含result值的数据。
     * @param name
     * @param delete
     * @param hide
     * @param status
     * @return
     */
    List findByOperateNameAndIsDeleteAndIsHideAndStatusNotOrderByCreateTimeDesc(String name, Integer delete, Integer hide, String status);

    /**
     * 获取当前审批者信息
     * @param userId
     * @param porins
     * @param hide
     * @param delete
     * @return
     */
    List<ActApprovedMemoPO> findByUserIdAndProcessInstanceIdAndIsHideAndIsDelete(String userId, String porins, Integer hide, Integer delete);

    /**
     *
     * @param piid 流程示例id
     * @param fid 表单id
     * @param hide 是否隐藏
     * @param delete 是否删除
     * @return
     */
    List<ActApprovedMemoPO> findByProcessInstanceIdAndFormIdAndIsHideAndIsDeleteOrderByCreateTimeDesc(String piid, String fid, Integer hide, Integer delete);
    List<ActApprovedMemoPO> findByFormIdAndIsHideAndIsDeleteOrderByCreateTimeDesc(String fid, Integer hide, Integer delete);


    /**
     * 获取流程记录
     * @param userid
     * @param piid
     * @param delete
     * @param hide
     * @return
     */
    List<ActApprovedMemoPO> findByUserIdAndProcessInstanceIdAndIsDeleteAndIsHide(String userid, String piid, Integer delete, Integer hide);

    /**
     * 根据formId获取审批记录信息（statusCode为!=）
     * @param formId
     * @param statusCode
     * @param isDeleted
     * @return
     */
    List<ActApprovedMemoPO> findByFormIdAndStatusCodeNotAndIsDelete(String formId, Integer statusCode, Integer isDeleted);

    List<ActApprovedMemoPO> findByFormIdAndIsDeleteAndIsHide(String formId, Integer isDeleted, Integer isHide);

    List<ActApprovedMemoPO> findByFormIdAndStatusCodeAndIsDeleteAndIsHide(String formId, Integer statusCode, Integer isDeleted, Integer isHide);

}

package io.bhex.bhop.common.mapper;

import io.bhex.bhop.common.entity.UserRoleIndex;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.mapper
 * @Author: ming.xu
 * @CreateDate: 06/10/2018 3:56 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Component
@org.apache.ibatis.annotations.Mapper
public interface UserRoleIndexMapper extends Mapper<UserRoleIndex> {

    String TABLE_NAME = " tb_user_role_index ";

    @Update("update " + TABLE_NAME + " set status=#{status} where org_id=#{orgId} and user_id=#{userId} and status=#{oldStatus}")
    int deleteByUserId(@Param("status") Integer status, @Param("oldStatus") Integer oldStatus, @Param("orgId") Long orgId, @Param("userId") Long userId);

    @Update("update " + TABLE_NAME + " set status=#{status} where org_id=#{orgId} and role_id=#{roleId} and status=#{oldStatus}")
    int deleteByRoleId(@Param("status") Integer status, @Param("oldStatus") Integer oldStatus, @Param("orgId") Long orgId, @Param("roleId") Long roleId);

    @Select("select count(*) from " + TABLE_NAME + " where org_id=#{orgId} and role_id=#{roleId} and status=#{status}")
    int countByRoleId(@Param("status") Integer status, @Param("orgId") Long orgId, @Param("roleId") Long roleId);
}

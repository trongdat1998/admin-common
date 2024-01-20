package io.bhex.bhop.common.mapper;

import io.bhex.bhop.common.entity.RoleAuthIndex;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.mapper
 * @Author: ming.xu
 * @CreateDate: 06/10/2018 3:55 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Component
@org.apache.ibatis.annotations.Mapper
public interface RoleAuthIndexMapper extends Mapper<RoleAuthIndex> {

    String TABLE_NAME = " tb_role_auth_index ";

    @Update("update " + TABLE_NAME + " set status=#{status} where org_id=#{orgId} and role_id=#{roleId} and status=#{oldStatus}")
    int deleteConfig(@Param("status") Integer status, @Param("oldStatus") Integer oldStatus, @Param("orgId") Long orgId, @Param("roleId") Long roleId);
}

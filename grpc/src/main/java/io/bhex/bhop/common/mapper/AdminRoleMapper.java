package io.bhex.bhop.common.mapper;

import io.bhex.bhop.common.entity.AdminRole;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.mapper
 * @Author: ming.xu
 * @CreateDate: 06/10/2018 3:54 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Component
@org.apache.ibatis.annotations.Mapper
public interface AdminRoleMapper extends Mapper<AdminRole> {

}

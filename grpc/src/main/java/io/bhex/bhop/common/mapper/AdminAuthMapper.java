package io.bhex.bhop.common.mapper;

import io.bhex.bhop.common.entity.AdminAuth;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.mapper
 * @Author: ming.xu
 * @CreateDate: 06/10/2018 3:53 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Component
@org.apache.ibatis.annotations.Mapper
public interface AdminAuthMapper extends Mapper<AdminAuth> {

}

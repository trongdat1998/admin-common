package io.bhex.bhop.common.mapper;

import io.bhex.bhop.common.entity.UserIpWhitelist;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.mapper
 * @Author: ming.xu
 * @CreateDate: 2019/3/19 11:30 AM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Component
@org.apache.ibatis.annotations.Mapper
public interface UserIpWhitelistMapper extends Mapper<UserIpWhitelist> {

}

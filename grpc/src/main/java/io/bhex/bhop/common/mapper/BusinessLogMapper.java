package io.bhex.bhop.common.mapper;

import io.bhex.bhop.common.entity.BusinessLog;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/**
 * @Description:
 * @Date: 2018/12/19 下午2:53
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@org.apache.ibatis.annotations.Mapper
@Component
public interface BusinessLogMapper   extends Mapper<BusinessLog> {

}

package io.bhex.bhop.common.mapper;


import io.bhex.bhop.common.entity.InitPasswordToken;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

import java.sql.Timestamp;

/**
 * @Description:
 * @Date: 2018/8/20 上午10:27
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@org.apache.ibatis.annotations.Mapper
@Component
public interface InitPasswordTokenMapper extends Mapper<InitPasswordToken> {

    @Select("SELECT * FROM tb_init_password_token WHERE admin_user_id = #{adminUserId}")
    InitPasswordToken getByAdminUserId(@Param("adminUserId") Long adminUserId);

    @Update("update tb_init_password_token set expired_at = #{expiredAt}, validate_result = #{validateResult} where  admin_user_id = #{adminUserId}")
    int updateExpiredAt(@Param("adminUserId") Long adminUserId, @Param("expiredAt") Timestamp expiredAt, @Param("validateResult") int validateResult);
}

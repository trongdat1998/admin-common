package io.bhex.bhop.common.mapper;

import io.bhex.bhop.common.entity.UserBindGACheck;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.mapper
 * @Author: ming.xu
 * @CreateDate: 2019/3/14 12:14 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@org.apache.ibatis.annotations.Mapper
@Component
public interface UserBindGACheckMapper extends Mapper<UserBindGACheck> {

    String USER_BIND_GA_CHECK_TABLE_NAME = "tb_user_bind_ga_check";

    String USER_BIND_GA_CHECK_COLUMNS = "id, org_id, user_id, ga_key, expired, created";

    @InsertProvider(type = UserBindGACheckSqlProvider.class, method = "insertUserBindGACheck")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    int insertUserBindGACheck(UserBindGACheck userBindGACheck);

    @SelectProvider(type = UserBindGACheckSqlProvider.class, method = "getLastBindGaCheck")
    UserBindGACheck getLastBindGaCheck(@Param("orgId") Long orgId, @Param("userId") Long userId);
}

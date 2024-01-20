package io.bhex.bhop.common.mapper;

import io.bhex.bhop.common.entity.UserBindGACheck;
import org.apache.ibatis.jdbc.SQL;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.mapper
 * @Author: ming.xu
 * @CreateDate: 2019/3/14 12:15 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
public class UserBindGACheckSqlProvider {

    public String insertUserBindGACheck(UserBindGACheck bindGACheck) {
        return new SQL() {
            {
                INSERT_INTO(UserBindGACheckMapper.USER_BIND_GA_CHECK_TABLE_NAME);
                VALUES("org_id", "#{orgId}");
                VALUES("user_id", "#{userId}");
                VALUES("ga_key", "#{gaKey}");
                VALUES("expired", "#{expired}");
                VALUES("created", "#{created}");
            }
        }.toString();
    }

    public String getLastBindGaCheck() {
        return "SELECT " + UserBindGACheckMapper.USER_BIND_GA_CHECK_COLUMNS + " FROM " + UserBindGACheckMapper.USER_BIND_GA_CHECK_TABLE_NAME
                + " WHERE org_id = #{orgId} AND user_id = #{userId} ORDER BY id DESC LIMIT 1";
    }
}

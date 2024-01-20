package io.bhex.bhop.common.mapper;

import io.bhex.bhop.common.entity.AdminApiKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminApiKeyMapper {

    @Select("SELECT * FROM tb_admin_api_key WHERE access_key=#{accessKey}")
    AdminApiKey getByAccessKey(@Param("accessKey") String accessKey);

}

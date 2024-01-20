package io.bhex.bhop.common.mapper;

import io.bhex.bhop.common.entity.AdminUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@org.apache.ibatis.annotations.Mapper
@Component(value = "adminUserMapper")
public interface AdminUserMapper  extends Mapper<AdminUser> {

    @Select("SELECT * FROM tb_admin_user WHERE username = #{username} and  password = #{password} limit 1")
    AdminUser selectByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    @Select("select DISTINCT(org_id) from tb_admin_user")
    List<Long> selectOrgIds();

    @Select("<script>"
            + "SELECT * FROM tb_admin_user WHERE username = #{username}  "
            + "<if test='brokerId > 0'> AND org_id = #{brokerId}</if>"
            + "</script>")
    public AdminUser selectByUsername(@Param("brokerId") Long orgId, @Param("username") String username);

    @Select("SELECT count(id) FROM tb_admin_user WHERE username = #{username}")
    public int countByUsername(@Param("username") String username);

    @Select("SELECT * FROM tb_admin_user WHERE id = #{id}")
    AdminUser selectAdminUserById(@Param("id") Long id);

    @Select("SELECT * FROM tb_admin_user WHERE id = #{id} and org_id = #{orgId}")
    AdminUser selectAdminUserByIdAndOrgId(@Param("id") Long id, @Param("orgId") Long orgId);

    @Select("SELECT * FROM tb_admin_user WHERE org_id = #{orgId} and email = #{email}")
    AdminUser selectAdminUserByEmail(@Param("email") String email, @Param("orgId") Long orgId);

    @Select("SELECT * FROM tb_admin_user WHERE org_id = #{orgId} and area_code = #{nationalCode} and telephone = #{phone}")
    AdminUser selectAdminUserByPhone(@Param("nationalCode") String nationalCode, @Param("phone") String phone, @Param("orgId") Long orgId);

    @Select("SELECT * FROM tb_admin_user WHERE org_id = #{orgId}")
    AdminUser selectAdminUserByExchangeId(@Param("orgId") Long orgId);

    @Select("SELECT * FROM tb_admin_user WHERE org_id = #{brokerId} limit 1")
    AdminUser selectAdminUserByOrgId(@Param("brokerId") Long orgId);

    @Update("update tb_admin_user set password = #{password}, status = 1 where id = #{id}")
    public int updatePassword(@Param("id") Long id, @Param("password") String password);

}

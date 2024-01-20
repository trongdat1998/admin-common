package io.bhex.bhop.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_admin_user")
public class AdminUser {

    public final static Integer ROOT_ACCOUNT = 1;
    public final static Integer SUB_ACCOUNT = 2;

    public final static Integer ENABLE_STATUS = 1;
    public final static Integer FORBID_STATUS = 2;

    public final static Integer UN_BIND = 0;
    public final static Integer BIND = 1;

    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;//序号

    private Long orgId;//对应的bluehelix 平台 org_id

    private String orgName; //对应saas的机构简称

    private Long saasOrgId;

    private String email;

    private String areaCode;//手机区号

    private String telephone; //手机号码

    private String username;//用户名

    private String defaultLanguage; //默认语言

    private String password;//密码

    private Integer status;//状态 1 启用 2禁用

    private Timestamp createdAt;//用户注册时间

    private String createdIp;//用户注册IP

    private String position;//职位

    private Integer accountType; //账号类型 1主账户 2子账号

    private Integer deleted;//逻辑删除: 1=删除 0=正常

    private String realName;

    private Integer bindGa;

    private Integer bindPhone;

    private String gaKey;
}

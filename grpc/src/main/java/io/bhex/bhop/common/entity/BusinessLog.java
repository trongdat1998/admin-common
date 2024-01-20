package io.bhex.bhop.common.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Table(name = "tb_business_log")
public class BusinessLog {

    @Id
    private Long id; //序号

    private Long orgId; //对应的bluehelix 平台 org_id

    private String username;

    //操作类型
    private String opType;

    private String subType;

    //返回结果
    private Integer resultCode;

    //返回数据
    private String resultMsg;

    //备注信息
    private String remark;

    /** 唯一标识所使用的值，大部分为id主键的值*/
    private String entityId;

    private String requestInfo;

    private String ip;

    private Timestamp created;

    private Integer visible;

    private String requestUrl;

    private String userAgent;
}

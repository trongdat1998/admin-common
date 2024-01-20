package io.bhex.bhop.common.util;

import io.bhex.base.proto.BaseRequest;

/**
 * @author wangsc
 * @description 产生baseRequest
 * @date 2020-05-31 22:15
 */
public class BaseReqUtil {

    public static BaseRequest getBaseRequest(Long orgId){
        //兼容proxy为false的情况,不做校验避免orgId为null或者为0时失败
        return BaseRequest.newBuilder().setOrganizationId(orgId).build();
    }
}

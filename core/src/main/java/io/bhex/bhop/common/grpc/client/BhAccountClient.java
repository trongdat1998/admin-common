package io.bhex.bhop.common.grpc.client;

import io.bhex.base.account.AccountType;
import io.bhex.base.account.BindAccountReply;

/**
 * @Description:
 * @Date: 2018/10/8 下午7:00
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
public interface BhAccountClient {


    /**
     * 平台账户绑定指定类型的账户
     * @param orgId
     * @param accountId
     * @param accountType
     * @return
     */
    BindAccountReply bindAccount(long orgId, long accountId, AccountType accountType);


    Long bindRelation(long orgId, AccountType accountType);

    Long getAccountBrokerId(Long accountId);

    /**
     * 根据账号获取brokerId（独立部署--大概校验当前brokerId是否包含accountId）
     * @param accountId accountId
     * @param orgId orgId
     * @return
     */
    Long getAccountBrokerId(Long accountId,Long orgId);
}

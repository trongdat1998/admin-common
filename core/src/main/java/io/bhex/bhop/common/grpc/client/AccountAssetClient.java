package io.bhex.bhop.common.grpc.client;

import io.bhex.bhop.common.dto.param.BalanceDetailDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description:
 * @Date: 2018/11/1 下午2:51
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
public interface AccountAssetClient {

    /**
     * 获取accountId下的所有资产(独立部署--代替不含orgId的接口)
     * @param brokerId
     * @param accountId
     * @return
     */
    List<BalanceDetailDTO> getBalances(Long brokerId, Long accountId);

    /**
     * 获取accountId下指定token的资产
     * @param accountId
     * @param tokenId
     * @return
     */
    BalanceDetailDTO getBalance(Long accountId, String tokenId);

    /**
     * 获取accountId下指定token的资产（独立部署--代替不含orgId的接口）
     * @param accountId
     * @param tokenId
     * @param orgId
     * @return
     */
    BalanceDetailDTO getBalance(Long accountId, String tokenId,Long orgId);

    /**
     * 获取accountId下的所有资产
     * @param accountId
     * @return
     */
    List<BalanceDetailDTO> getBalances(Long accountId);

}

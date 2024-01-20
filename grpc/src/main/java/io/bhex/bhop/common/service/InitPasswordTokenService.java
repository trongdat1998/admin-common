package io.bhex.bhop.common.service;

import io.bhex.bhop.common.entity.InitPasswordToken;
import io.bhex.bhop.common.mapper.InitPasswordTokenMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * @ProjectName: broker-server
 * @Package: io.bhex.broker.server.grpc.server.service
 * @Author: ming.xu
 * @CreateDate: 19/08/2018 8:39 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Service
public class InitPasswordTokenService {

    @Autowired
    private InitPasswordTokenMapper initPasswordTokenMapper;

    public InitPasswordToken getByAdminUserId(Long adminUserId) {
        return initPasswordTokenMapper.getByAdminUserId(adminUserId);
    }

    public Boolean saveInitPasswordToken(InitPasswordToken initPasswordToken) {
        InitPasswordToken token = getByAdminUserId(initPasswordToken.getAdminUserId());
        if (null == token) {
            initPasswordTokenMapper.insert(initPasswordToken);
        } else {
            initPasswordTokenMapper.updateByPrimaryKey(initPasswordToken);
        }
        return true;
    }


    public boolean updateExpiredAt( Long adminUserId,Timestamp expiredAt, boolean updatePasswordOk){
        int validateResult = updatePasswordOk ? 1 : 2;
        return initPasswordTokenMapper.updateExpiredAt(adminUserId, expiredAt, validateResult) == 1;
    }
}

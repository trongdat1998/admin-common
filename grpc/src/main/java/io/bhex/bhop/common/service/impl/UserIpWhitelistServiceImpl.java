package io.bhex.bhop.common.service.impl;

import io.bhex.base.admin.IpWhitelistDetail;
import io.bhex.base.admin.ShowIpWhitelistResponse;
import io.bhex.bhop.common.entity.UserIpWhitelist;
import io.bhex.bhop.common.mapper.UserIpWhitelistMapper;
import io.bhex.bhop.common.service.IUserIpWhitelistService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.service.impl
 * @Author: ming.xu
 * @CreateDate: 2019/3/19 11:44 AM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Service
public class UserIpWhitelistServiceImpl implements IUserIpWhitelistService {

    @Autowired
    private UserIpWhitelistMapper userIpWhitelistMapper;

    @Override
    public Boolean addIpWhitelist(Long orgId, Long admingId, String ipAddress) {
        UserIpWhitelist userIpWhitelist = new UserIpWhitelist();
        userIpWhitelist.setOrgId(orgId);
        userIpWhitelist.setAdminId(admingId);
        userIpWhitelist.setIpAddress(ipAddress);
        userIpWhitelist.setStatus(UserIpWhitelist.STATUS_PASS);
        userIpWhitelist.setCreated(System.currentTimeMillis());

        return userIpWhitelistMapper.insert(userIpWhitelist) > 0? true: false;
    }

    @Override
    public Boolean deleteIpWhitelist(Long orgId, Long admingId, Long id) {
        UserIpWhitelist userIpWhitelist = userIpWhitelistMapper.selectByPrimaryKey(id);
        if (userIpWhitelist.getOrgId().equals(orgId) && userIpWhitelist.getStatus().equals(UserIpWhitelist.STATUS_PASS)) {
            userIpWhitelist.setStatus(UserIpWhitelist.STATUS_DELETE);
            userIpWhitelist.setAdminId(admingId);
            return userIpWhitelistMapper.updateByPrimaryKey(userIpWhitelist) > 0 ? true : false;
        }
        return false;
    }

    @Override
    public ShowIpWhitelistResponse showIpWhitelist(Long orgId) {
        Example example = Example.builder(UserIpWhitelist.class).build();
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orgId", orgId);
        criteria.andEqualTo("status", UserIpWhitelist.STATUS_PASS);
        List<UserIpWhitelist> userIpWhitelists = userIpWhitelistMapper.selectByExample(example);
        List<IpWhitelistDetail> details = new ArrayList<>();
        userIpWhitelists.forEach(ip -> {
            IpWhitelistDetail.Builder builder = IpWhitelistDetail.newBuilder();
            BeanUtils.copyProperties(ip, builder);
            details.add(builder.build());
        });
        ShowIpWhitelistResponse response = ShowIpWhitelistResponse.newBuilder()
                .addAllIpWhitelistDetails(details)
                .build();
        return response;
    }
}

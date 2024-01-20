package io.bhex.bhop.common.service;

import io.bhex.base.admin.*;
import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.bhop.common.dto.IpWhitelistDTO;
import io.bhex.bhop.common.dto.param.AddIpWhitelistPO;
import io.bhex.bhop.common.dto.param.DeleteIpWhitelistPO;
import io.bhex.bhop.common.dto.param.ShowIpWhitelistPO;
import io.bhex.bhop.common.grpc.client.AdminUserClient;
import io.bhex.bhop.common.grpc.client.UserIpWhitelistServiceClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.service
 * @Author: ming.xu
 * @CreateDate: 2019/3/20 4:25 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Service
public class UserIpWhitelistService {

    @Autowired
    private UserIpWhitelistServiceClient ipWhitelistServiceClient;

    @Autowired
    private AdminUserClient adminUserClient;

    public Boolean addIpWhitelist(AddIpWhitelistPO param) {
        AddIpWhitelistRequest request = AddIpWhitelistRequest.newBuilder()
                .setOrgId(param.getOrgId())
                .setAdminUserId(param.getAdminId())
                .setIpAddress(param.getIpAddress())
                .build();

        OptionIpWhitelistResponse response = ipWhitelistServiceClient.addIpWhitelist(request);
        return response.getRet();
    }

    public Boolean deleteIpWhitelist(DeleteIpWhitelistPO param) {
        DeleteIpWhitelistRequest request = DeleteIpWhitelistRequest.newBuilder()
                .setId(param.getId())
                .setAdminUserId(param.getAdminId())
                .setOrgId(param.getOrgId())
                .build();

        OptionIpWhitelistResponse response = ipWhitelistServiceClient.deleteIpWhitelist(request);
        return response.getRet();
    }

    public List<IpWhitelistDTO> showIpWhitelist(Long orgId) {
        ShowIpWhitelistRequest request = ShowIpWhitelistRequest.newBuilder()
                .setOrgId(orgId)
                .build();

        ShowIpWhitelistResponse response = ipWhitelistServiceClient.showIpWhitelist(request);

        List<IpWhitelistDetail> details = response.getIpWhitelistDetailsList();

        List<Long> uidList = new ArrayList<>();
        List<IpWhitelistDTO> dtoList = new ArrayList<>();
        details.forEach(d -> {
            uidList.add(d.getAdminId());
        });
        Map<Long, String> userNameMap = new HashMap();
        if (!CollectionUtils.isEmpty(uidList)) {
            List<AdminUserReply> adminUserByUids = adminUserClient.getAdminUserByUids(orgId, uidList);
            if (!CollectionUtils.isEmpty(adminUserByUids)) {
                for (AdminUserReply userReply: adminUserByUids) {
                    userNameMap.put(userReply.getId(), userReply.getEmail());
                }
            }
        }
        details.forEach(d -> {
            IpWhitelistDTO dto = new IpWhitelistDTO();
            BeanUtils.copyProperties(d, dto);
            dto.setAdminName(userNameMap.get(d.getAdminId()));
            dtoList.add(dto);
        });
        return dtoList;
    }

    public List<IpWhitelistDTO> showIpWhitelist(ShowIpWhitelistPO param) {
        return showIpWhitelist(param.getOrgId());
    }
}

package io.bhex.bhop.common.grpc.client;


import io.bhex.base.admin.common.*;
import io.grpc.stub.StreamObserver;

import java.util.List;

public interface AdminUserClient {


    public List<Long> getOrgIds();

    LoginByUsernameAndPasswordReply loginByUsernameAndPassword(Long orgId, String username, String password);

    //AdminUserReply selectByUsername(String username);

    int countByUsername(String username);

    boolean changePassword(Long id, String oldPassword, String newPassword);

    boolean sendChangePasswordEmail(Long id);

    AddAdminUserReply addAdminUser(AddAdminUserRequest request);

    AddAdminUserReply addSubAdminUser(AddSubAdminUserRequest request);

    UpdateSubAdminUserReply updateSubAdminUser(UpdateSubAdminUserRequest request);

    EnableSubAdminUserReply enableSubAdminUser(EnableSubAdminUserRequest request);

    AdminUserReply getAdminUserById(Long id);

    AdminUserReply getAdminUserById(Long id, Long orgId);

    AdminUserReply getAdminRootUserByOrgId(Long orgId);

    List<AdminUserReply> getAdminUserByUids(Long orgId, List<Long> uidList);

    GetInitPasswordTokenReply getInitPasswordToken(Long adminUserId);

    String saveInitPasswordToken(Long adminUserId);

    boolean saveInitPassword(Long adminUserId, String password);

    boolean resetPassword(Long adminUserId, String password);

    AdminUserReply getAdminUserByEmail(String email, Long orgId);

    AdminUserReply getAdminUserByPhone(String nationalCode, String phone, Long orgId);

    Boolean isPhoneExist(String nationalCode, String phone, Long orgId, Long userId);

    ChangeAdminUserReply changeAdminUser(ChangeAdminUserRequest request);

    List<AdminUserReply> listAdminUserByOrgId(Long orgId);
}

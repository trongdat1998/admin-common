package io.bhex.bhop.common.service;

import io.bhex.base.admin.common.ChangeAdminUserReply;
import io.bhex.base.admin.common.ChangeAdminUserRequest;
import io.bhex.bhop.common.entity.AdminUser;
import io.grpc.stub.StreamObserver;

import java.util.List;

public interface AdminUserService {

    AdminUser selectByUsernameAndPassword(String username, String password);

    public boolean checkPassword(String username, String inputPassword, String passwordInDb);

    AdminUser selectByUsername(Long orgId, String username);

    AdminUser getAdminUserById(Long id);

    AdminUser getAdminUserById(Long id, Long orgId);

    AdminUser getAdminUserByEmail(String email, Long orgId);

    AdminUser getAdminUserByPhone(String nationalCode, String phone, Long orgId);

    AdminUser getAdminUserByOrgId(Long orgId);

    List<AdminUser> selectAdminUserByUid(Long orgId, List<Long> uids);

    public int countByUsername(String username);

    public boolean changePassword(Long id, String oldPassword, String newPassword);

    public boolean validateOldPassword(Long id, String oldPassword);

    public boolean sendChangePasswordEmail(Long id);

    boolean updateAdminUserById(AdminUser adminUser);

    public boolean addAdminUser(AdminUser adminUser);

    ChangeAdminUserReply changeAdminUser(ChangeAdminUserRequest request);

    public boolean initPassword(Long id, String password);

    public boolean resetPassword(Long id, String password);

    boolean addSubAdminUser(AdminUser adminUser, List<Long> roleIds);

    boolean updateSubAdminUser(AdminUser adminUser, List<Long> roleIds);

    public List<Long> getOrgIds();

    List<AdminUser> listAdminUserByOrgId(Long orgId);
}

package io.bhex.bhop.common.service.impl;


import io.bhex.base.admin.common.AccountType;
import io.bhex.base.admin.common.ChangeAdminUserReply;
import io.bhex.base.admin.common.ChangeAdminUserRequest;
import io.bhex.bhop.common.entity.AdminUser;
import io.bhex.bhop.common.mapper.AdminUserMapper;
import io.bhex.bhop.common.service.AdminUserAuthService;
import io.bhex.bhop.common.service.AdminUserService;
import io.bhex.bhop.common.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Objects;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    public static final String BASE_SALT = "BHexTradingPlatform";

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private AdminUserAuthService userAuthService;

    @Override
    public List<Long> getOrgIds() {
        return adminUserMapper.selectOrgIds();
    }

    @Override
    public AdminUser selectByUsernameAndPassword(String username, String password) {
        return adminUserMapper.selectByUsernameAndPassword(username, getPasswordWithSalt(password, username));
    }

    @Override
    public AdminUser selectByUsername(Long orgId, String username) {
        return adminUserMapper.selectByUsername(orgId, username);
    }

    @Override
    public boolean checkPassword(String username, String inputPassword, String passwordInDb) {
        return getPasswordWithSalt(inputPassword, username).equals(passwordInDb);
    }

    @Override
    public AdminUser getAdminUserById(Long id){
        return adminUserMapper.selectAdminUserById(id);
    }

    @Override
    public AdminUser getAdminUserById(Long id, Long orgId) {
        return adminUserMapper.selectAdminUserByIdAndOrgId(id, orgId);
    }

    @Override
    public AdminUser getAdminUserByOrgId(Long orgId) {
        return adminUserMapper.selectAdminUserByOrgId(orgId);
    }

    @Override
    public AdminUser getAdminUserByEmail(String email, Long orgId){
        return adminUserMapper.selectAdminUserByEmail(email, orgId);
    }

    @Override
    public List<AdminUser> selectAdminUserByUid(Long orgId, List<Long> uids) {
        Example example = Example.builder(AdminUser.class).build();
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orgId", orgId);
        criteria.andIn("id", uids);
        return adminUserMapper.selectByExample(example);
    }

    @Override
    public AdminUser getAdminUserByPhone(String nationalCode, String phone, Long orgId) {
        return adminUserMapper.selectAdminUserByPhone(nationalCode, phone, orgId);
    }

    @Override
    public int countByUsername(String username) {
        return adminUserMapper.countByUsername(username);
    }



    @Override
    public boolean sendChangePasswordEmail(Long id) {
        return false;
    }

    @Override
    public boolean updateAdminUserById(AdminUser adminUser) {
        return false;
    }

    @Override
    public boolean addAdminUser(AdminUser adminUser) {
        adminUser.setAccountType(AdminUser.ROOT_ACCOUNT);
        int num = adminUserMapper.insertSelective(adminUser);

        return true;
    }

    @Override
    public ChangeAdminUserReply changeAdminUser(ChangeAdminUserRequest request) {
        AdminUser user = adminUserMapper.selectAdminUserById(request.getId());
        if (user.getAccountType() == AccountType.ROOT_ACCOUNT_VALUE) {
            return ChangeAdminUserReply.newBuilder().setResult(false).setMessage("can't unbind for root account").build();
        }
        if (request.getUnbindGa()) {
            user.setBindGa(0);
            user.setGaKey("");
        }
        if (request.getUnbindPhone()) {
            user.setBindPhone(0);
            user.setTelephone("");
        }
        if (request.getLockAdminUser()) {
            user.setStatus(AdminUser.FORBID_STATUS);
        }
        if (request.getUnlockAdminUser()) {
            user.setStatus(AdminUser.ENABLE_STATUS);
        }
        int count = adminUserMapper.updateByPrimaryKeySelective(user);
        return ChangeAdminUserReply.newBuilder().setResult(count == 1).build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addSubAdminUser(AdminUser adminUser, List<Long> roleIds) {
        adminUser.setAccountType(AdminUser.SUB_ACCOUNT);
        adminUser.setPassword(getPasswordWithSalt(adminUser.getPassword(), adminUser.getEmail()));
        Boolean isOk = adminUserMapper.insertSelective(adminUser) > 0 ? true : false;
        if (isOk && !CollectionUtils.isEmpty(roleIds)) {
            userAuthService.saveUserRole(adminUser.getOrgId(), adminUser.getId(), roleIds);
        }
        return isOk;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateSubAdminUser(AdminUser adminUser, List<Long> roleIds) {
        adminUser.setAccountType(AdminUser.SUB_ACCOUNT);
//        if (StringUtils.isNotEmpty(adminUser.getPassword())) {
//            adminUser.setPassword(getPasswordWithSalt(adminUser.getPassword(), adminUser.getEmail()));
//        }
        Boolean isOk = adminUserMapper.updateByPrimaryKeySelective(adminUser) > 0;
        if (isOk && !CollectionUtils.isEmpty(roleIds)) {
            userAuthService.saveUserRole(adminUser.getOrgId(), adminUser.getId(), roleIds);
        }
        return isOk;
    }

    @Override
    public boolean initPassword(Long id, String password) {
        AdminUser user = adminUserMapper.selectAdminUserById(id);

        return adminUserMapper.updatePassword(id, getPasswordWithSalt(password, user.getUsername())) == 1;
    }

    @Override
    public boolean resetPassword(Long id, String password) {
        AdminUser user = adminUserMapper.selectAdminUserById(id);
        return adminUserMapper.updatePassword(id, getPasswordWithSalt(password, user.getUsername())) == 1;
    }

    @Override
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        AdminUser adminUser = getAdminUserById(id);
        if (Objects.isNull(adminUser)) {
            return false;
        }
        Boolean isOk = checkPassword(adminUser.getUsername(), oldPassword, adminUser.getPassword());
        if (isOk) {
            isOk = adminUserMapper.updatePassword(id, getPasswordWithSalt(newPassword, adminUser.getUsername())) > 0? true: false;
        }
        return isOk;
    }

    @Override
    public boolean validateOldPassword(Long id, String oldPassword) {
        AdminUser user = adminUserMapper.selectAdminUserById(id);
        if(user == null){
            return false;
        }
        if(!getPasswordWithSalt(oldPassword, user.getUsername()).equals(user.getPassword())){
            return false;
        }
        return true;
    }

    @Override
    public List<AdminUser> listAdminUserByOrgId(Long orgId){
        Example example = new Example(AdminUser.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orgId", orgId);
        criteria.andEqualTo("deleted", 0);
        criteria.andEqualTo("status", 1);
        example.orderBy("id").asc();
        return adminUserMapper.selectByExampleAndRowBounds(example,new RowBounds(0, 100));
    }

    private String getPasswordWithSalt(String password, String salt){
        return MD5Util.getMD5(salt + password + BASE_SALT);
    }

    public static void main(String[] args) {

        System.out.println(new AdminUserServiceImpl().getPasswordWithSalt("2ea596408cb9352c0567be5bd4c7432a", "abe.liu@ace.io").equals("9d374bd68c2e16707ec91fb03622447e"));

    }


}

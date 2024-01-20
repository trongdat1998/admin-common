package io.bhex.bhop.common.grpc.service;


import io.bhex.base.admin.common.*;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.base.idgen.snowflake.SnowflakeGenerator;
import io.bhex.bhop.common.entity.AdminRole;
import io.bhex.bhop.common.entity.AdminUser;
import io.bhex.bhop.common.entity.InitPasswordToken;
import io.bhex.bhop.common.service.AdminUserAuthService;
import io.bhex.bhop.common.service.AdminUserService;
import io.bhex.bhop.common.service.InitPasswordTokenService;
import io.bhex.bhop.common.service.OpenapiService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@GrpcService
public class AdminUserGrpcImpl extends AdminUserServiceGrpc.AdminUserServiceImplBase {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private AdminUserAuthService adminUserAuthService;

    @Autowired
    private InitPasswordTokenService initPasswordTokenService;

    @Autowired
    SnowflakeGenerator idGenerator;

    @Resource
    private OpenapiService openapiService;

    @Override
    public void loginByUsernameAndPassword(LoginByUsernameAndPasswordRequest request,
                                           StreamObserver<LoginByUsernameAndPasswordReply> responseObserver) {
        LoginByUsernameAndPasswordReply.Builder builder = LoginByUsernameAndPasswordReply.newBuilder();
        AdminUser adminUser = adminUserService.selectByUsername(request.getOrgId(), request.getUsername());
        if (adminUser == null) {
            builder.setResult(LoginByUsernameAndPasswordReply.Result.UsernameNotExisted);
        } else {
            if (adminUserService.checkPassword(request.getUsername().toLowerCase(), request.getPassword(), adminUser.getPassword()) == false) {
                builder.setResult(LoginByUsernameAndPasswordReply.Result.PasswordWrong);
            } else {
                builder.setResult(LoginByUsernameAndPasswordReply.Result.LoginSuccess);
            }
            AdminUserReply.Builder userReplyBuilder = AdminUserReply.newBuilder();
            BeanUtils.copyProperties(adminUser, userReplyBuilder);
            userReplyBuilder.setBindGa(AdminUser.BIND.equals(adminUser.getBindGa()));
            userReplyBuilder.setBindPhone(AdminUser.BIND.equals(adminUser.getBindPhone()));
            userReplyBuilder.setAccountType(adminUser.getAccountType() == AdminUser.ROOT_ACCOUNT ? AccountType.ROOT_ACCOUNT : AccountType.SUB_ACCOUNT);
            builder.setAdminUser(userReplyBuilder.build());
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void openApiAuthenticate(OpenApiAuthenticationRequest request, StreamObserver<OpenApiAuthenticationReply> observer) {
        OpenApiAuthenticationReply reply;
        try {
            OpenapiService.OpenapiAuthenticateResult result = openapiService.openapiAuthenticate(request.getAccessKey(), request.getOriginalStr(), request.getSignature());
            if (result.getResult() != 0) {
                reply = OpenApiAuthenticationReply.newBuilder().setResult(result.getResult()).build();
            } else {
                AdminUser user = result.getUser();
                reply = OpenApiAuthenticationReply.newBuilder().setOrgId(user.getOrgId()).setUserId(user.getId()).build();
            }
            observer.onNext(reply);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(e);
        }
    }

    @Override
    public void getAdminUserByEmail(GetAdminUserByEmailRequest request, StreamObserver<AdminUserReply> responseObserver) {
        AdminUserReply.Builder userReplyBuilder = AdminUserReply.newBuilder();
        AdminUser adminUserByEmail = adminUserService.getAdminUserByEmail(request.getEmail().toLowerCase(), request.getOrgId());
        if (null != adminUserByEmail) {
            BeanUtils.copyProperties(adminUserByEmail, userReplyBuilder);
            userReplyBuilder.setBindGa(AdminUser.BIND.equals(adminUserByEmail.getBindGa()));
            userReplyBuilder.setBindPhone(AdminUser.BIND.equals(adminUserByEmail.getBindPhone()));
            userReplyBuilder.setAccountTypeValue(adminUserByEmail.getAccountType());
        }
        responseObserver.onNext(userReplyBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAdminUserByPhone(GetAdminUserByPhoneRequest request, StreamObserver<AdminUserReply> responseObserver) {
        AdminUserReply.Builder userReplyBuilder = AdminUserReply.newBuilder();
        AdminUser adminUserByPhone = adminUserService.getAdminUserByPhone(request.getNationCode(), request.getPhone(), request.getOrgId());
        if (null != adminUserByPhone) {
            BeanUtils.copyProperties(adminUserByPhone, userReplyBuilder);
            userReplyBuilder.setBindGa(AdminUser.BIND.equals(adminUserByPhone.getBindGa()));
            userReplyBuilder.setBindPhone(AdminUser.BIND.equals(adminUserByPhone.getBindPhone()));
        }
        responseObserver.onNext(userReplyBuilder.build());
        responseObserver.onCompleted();
    }

    /**
     *
     */
    public void countByUsername(CountByUsernameRequest request,
                                StreamObserver<CountByUsernameReply> responseObserver) {
        int count = adminUserService.countByUsername(request.getUsername().toLowerCase());
        CountByUsernameReply reply = CountByUsernameReply.newBuilder().setCount(count).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    /**
     *
     */
    public void changePassword(ChangePasswordRequest request,
                               StreamObserver<ChangePasswordReply> responseObserver) {
        Boolean isOk = adminUserService.changePassword(request.getId(), request.getOldPassword(), request.getNewPassword());
        responseObserver.onNext(ChangePasswordReply.newBuilder().setResult(isOk).build());
        responseObserver.onCompleted();
    }

    /**
     *
     */
    public void addAdminUser(AddAdminUserRequest request,
                             StreamObserver<AddAdminUserReply> responseObserver) {
        Long id = idGenerator.getLong();
        AdminUser adminUser = new AdminUser();
        BeanUtils.copyProperties(request, adminUser);
        adminUser.setStatus(0);
        adminUser.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        adminUser.setDeleted(0);
        adminUser.setId(id);
        adminUser.setBindGa(AdminUser.UN_BIND);
        adminUser.setBindPhone(AdminUser.UN_BIND);
        adminUser.setUsername(request.getUsername().toLowerCase());
        adminUser.setEmail(request.getEmail().toLowerCase());

        boolean result = adminUserService.addAdminUser(adminUser);
        log.info("add user:{} result : {}", id, result);

        AddAdminUserReply reply = AddAdminUserReply.newBuilder()
                .setResult(result)
                .setAdminUserId(id)
                .build();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void changeAdminUser(ChangeAdminUserRequest request, StreamObserver<ChangeAdminUserReply> responseObserver) {
        ChangeAdminUserReply reply = adminUserService.changeAdminUser(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void addSubAdminUser(AddSubAdminUserRequest request, StreamObserver<AddAdminUserReply> responseObserver) {
        Long id = idGenerator.getLong();
        AdminUser adminUser = new AdminUser();
        BeanUtils.copyProperties(request, adminUser);
        adminUser.setStatus(0);
        adminUser.setPosition(request.getPosition());
        adminUser.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        adminUser.setDeleted(0);
        adminUser.setId(id);
        adminUser.setBindGa(AdminUser.UN_BIND);
        adminUser.setBindPhone(AdminUser.UN_BIND);
        adminUser.setUsername(request.getUsername().toLowerCase());
        adminUser.setEmail(request.getEmail().toLowerCase());


        boolean result = adminUserService.addSubAdminUser(adminUser, request.getRoleIdsList());
        log.info("add user:{} result : {}", id, result);

        AddAdminUserReply reply = AddAdminUserReply.newBuilder()
                .setResult(result)
                .setAdminUserId(id)
                .build();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void updateSubAdminUser(UpdateSubAdminUserRequest request, StreamObserver<UpdateSubAdminUserReply> responseObserver) {
        UpdateSubAdminUserReply.Builder builder = UpdateSubAdminUserReply.newBuilder();

        AdminUser adminUser = adminUserService.getAdminUserById(request.getAdminId(), request.getOrgId());

        if (Objects.isNull(adminUser)) {
            builder.setResult(false)
                    .setAdminUserId(request.getAdminId())
                    .build();
        } else {
//            if (StringUtils.isNoneEmpty(request.getPassword())) {
//                adminUser.setPassword(request.getPassword());
//            } else {
//                adminUser.setPassword(null);
//            }
            adminUser.setEmail(request.getEmail().toLowerCase());
            adminUser.setAreaCode(request.getAreaCode());
            adminUser.setTelephone(request.getTelephone());
            adminUser.setUsername(request.getEmail().toLowerCase());
            adminUser.setPosition(request.getPosition());
            adminUser.setRealName(request.getRealName());

            boolean result = adminUserService.updateSubAdminUser(adminUser, request.getRoleIdsList());
            log.info("update user:{} result : {}", request.getAdminId(), result);

            builder.setResult(result)
                    .setAdminUserId(request.getAdminId())
                    .build();
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void enableSubAdminUser(EnableSubAdminUserRequest request, StreamObserver<EnableSubAdminUserReply> responseObserver) {
        EnableSubAdminUserReply.Builder builder = EnableSubAdminUserReply.newBuilder();

        AdminUser adminUser = adminUserService.getAdminUserById(request.getAdminId(), request.getOrgId());

        if (Objects.isNull(adminUser)) {
            builder.setResult(false)
                    .setAdminUserId(request.getAdminId())
                    .build();
        } else {

            AdminUser tmp = new AdminUser();
            tmp.setId(adminUser.getId());
            tmp.setStatus(request.getStatus() == AdminUser.ENABLE_STATUS ? AdminUser.ENABLE_STATUS : AdminUser.FORBID_STATUS);
            boolean result = adminUserService.updateSubAdminUser(tmp, null);
            log.info("update user:{} result : {}", request.getAdminId(), result);

            builder.setResult(result)
                    .setAdminUserId(request.getAdminId())
                    .build();
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAdminUserById(GetAdminUserByIdRequest request,
                                 StreamObserver<AdminUserReply> responseObserver) {
        AdminUser adminUser = adminUserService.getAdminUserById(request.getId());
        AdminUserReply.Builder builder = AdminUserReply.newBuilder();
        if (adminUser != null) {
            BeanUtils.copyProperties(adminUser, builder);
            builder.setAccountType(adminUser.getAccountType() == AdminUser.ROOT_ACCOUNT ? AccountType.ROOT_ACCOUNT : AccountType.SUB_ACCOUNT);
            builder.setBindGa(AdminUser.BIND.equals(adminUser.getBindGa()));
            builder.setBindPhone(AdminUser.BIND.equals(adminUser.getBindPhone()));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAdminUserByIdAndOrgId(GetAdminUserByIdAndOrgIdRequest request, StreamObserver<AdminUserReply> responseObserver) {
        AdminUser adminUser = adminUserService.getAdminUserById(request.getId(), request.getOrgId());
        AdminUserReply.Builder builder = AdminUserReply.newBuilder();
        if (adminUser != null) {
            BeanUtils.copyProperties(adminUser, builder);
            List<AdminRole> roleList = adminUserAuthService.listRoleInfoByUserId(adminUser.getId(), adminUser.getOrgId(), false);
            roleList.forEach(role -> {
                builder.addRoleNameList(role.getName());
                builder.addRoleIds(role.getId());
            });
            builder.setAccountType(adminUser.getAccountType() == AdminUser.ROOT_ACCOUNT ? AccountType.ROOT_ACCOUNT : AccountType.SUB_ACCOUNT);
            builder.setBindGa(AdminUser.BIND.equals(adminUser.getBindGa()));
            builder.setBindPhone(AdminUser.BIND.equals(adminUser.getBindPhone()));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAdminUserByOrgId(GetAdminUserByOrgIdRequest request, StreamObserver<AdminUserReply> responseObserver) {
        AdminUser adminUser = adminUserService.getAdminUserByOrgId(request.getOrgId());
        AdminUserReply.Builder builder = AdminUserReply.newBuilder();
        if (adminUser != null) {
            BeanUtils.copyProperties(adminUser, builder);
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    public void saveInitPassword(SaveInitPasswordRequest request,
                                 StreamObserver<SaveInitPasswordReply> responseObserver) {

        boolean result = adminUserService.initPassword(request.getAdminUserId(), request.getPassword());

        initPasswordTokenService.updateExpiredAt(request.getAdminUserId(), new Timestamp(System.currentTimeMillis()), result);
        SaveInitPasswordReply.Builder builder = SaveInitPasswordReply.newBuilder();
        builder.setResult(result);
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();

    }

    @Override
    public void resetPassword(ResetPasswordRequest request, StreamObserver<ResetPasswordReply> responseObserver) {
        ResetPasswordReply.Builder builder = ResetPasswordReply.newBuilder();

        boolean result = adminUserService.resetPassword(request.getAdminUserId(), request.getPassword());

        builder.setResult(result);
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void saveInitPasswordToken(SaveInitPasswordTokenRequest request, StreamObserver<SaveInitPasswordTokenReply> responseObserver) {
        InitPasswordToken token = new InitPasswordToken();
        BeanUtils.copyProperties(request, token);
        token.setExpiredAt(new Timestamp(request.getExpiredAt()));
        token.setValidateResult(0);
        token.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        Boolean isOk = initPasswordTokenService.saveInitPasswordToken(token);
        SaveInitPasswordTokenReply.Builder builder = SaveInitPasswordTokenReply.newBuilder()
                .setResult(isOk);
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getInitPasswordToken(GetInitPasswordTokenRequest request, StreamObserver<GetInitPasswordTokenReply> responseObserver) {
        InitPasswordToken token = initPasswordTokenService.getByAdminUserId(request.getAdminUserId());
        log.info("init token:{}", token);
        GetInitPasswordTokenReply.Builder builder = GetInitPasswordTokenReply.newBuilder();
        if (null != token) {
            BeanUtils.copyProperties(token, builder);
            builder.setExpiredAt(token.getExpiredAt().getTime());
            builder.setValidateResult(token.getValidateResult());
        }

        log.info("builder:{}", builder.build());
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }


    @Override
    public void getOrgIds(GetOrgIdsRequest request, StreamObserver<GetOrgIdsReply> responseObserver) {
        List<Long> orgIds = adminUserService.getOrgIds();
        GetOrgIdsReply reply = GetOrgIdsReply.newBuilder().addAllOrgId(orgIds).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();

    }

    @Override
    public void getAdminUserByUids(GetAdminUserByUidsRequest request, StreamObserver<GetAdminUserByUidsReply> responseObserver) {
        List<AdminUser> adminUserList = adminUserService.selectAdminUserByUid(request.getOrgId(), request.getUidList());

        List<AdminUserReply> replyList = new ArrayList<>();
        adminUserList.forEach(adminUser -> {
            AdminUserReply.Builder builder = AdminUserReply.newBuilder();
            BeanUtils.copyProperties(adminUser, builder);
            builder.setAccountType(adminUser.getAccountType() == AdminUser.ROOT_ACCOUNT ? AccountType.ROOT_ACCOUNT : AccountType.SUB_ACCOUNT);
            builder.setBindGa(AdminUser.BIND.equals(adminUser.getBindGa()));
            builder.setBindPhone(AdminUser.BIND.equals(adminUser.getBindPhone()));
            replyList.add(builder.build());
        });
        GetAdminUserByUidsReply reply = GetAdminUserByUidsReply.newBuilder().addAllUserList(replyList).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void getAdminUserListByOrgId(GetAdminUserByOrgIdRequest request, StreamObserver<GetAdminUserByOrgIdReply> responseObserver) {
        List<AdminUser> adminUserList = adminUserService.listAdminUserByOrgId(request.getOrgId());

        List<AdminUserReply> replyList = new ArrayList<>();
        adminUserList.forEach(adminUser -> {
            AdminUserReply.Builder builder = AdminUserReply.newBuilder();
            BeanUtils.copyProperties(adminUser, builder);
            builder.setAccountType(adminUser.getAccountType().equals(AdminUser.ROOT_ACCOUNT) ? AccountType.ROOT_ACCOUNT : AccountType.SUB_ACCOUNT);
            builder.setBindGa(AdminUser.BIND.equals(adminUser.getBindGa()));
            builder.setBindPhone(AdminUser.BIND.equals(adminUser.getBindPhone()));
            replyList.add(builder.build());
        });
        GetAdminUserByOrgIdReply reply = GetAdminUserByOrgIdReply.newBuilder().addAllUserList(replyList).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}

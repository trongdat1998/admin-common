package io.bhex.bhop.common.config;

import io.bhex.base.account.AccountServiceGrpc;
import io.bhex.base.account.BalanceServiceGrpc;
import io.bhex.base.admin.AdminRoleAuthServiceGrpc;
import io.bhex.base.admin.AdminUserIpWhitelistServiceGrpc;
import io.bhex.base.admin.SecurityServiceGrpc;
import io.bhex.base.admin.common.AdminUserServiceGrpc;
import io.bhex.base.admin.common.BrokerAccountTradeFeeSettingServiceGrpc;
import io.bhex.base.admin.common.BrokerTradeFeeSettingServiceGrpc;
import io.bhex.base.admin.common.BusinessLogServiceGrpc;
import io.bhex.base.admin.common.CommissionServiceGrpc;
import io.bhex.base.admin.common.CountryServiceGrpc;
import io.bhex.base.common.MessageServiceGrpc;
import io.bhex.base.exadmin.BrokerSmsTemplateServiceGrpc;
import io.bhex.base.grpc.client.channel.IGrpcClientPool;
import io.bhex.base.token.SaasTokenServiceGrpc;
import io.bhex.broker.common.entity.GrpcChannelInfo;
import io.bhex.broker.common.entity.GrpcClientProperties;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 加载的时候会被依赖的工程排除
 */
@Slf4j
@Order(value = 1)
@Component
public class GrpcConfig {

    public static final String COMMON_SERVER_CHANNEL_NAME = "commonServerChannel";
    public static final String SAAS_ADMIN_GRPC_CHANNEL_NAME = "saasAdminGrpcChannel";
    public static final String ADMIN_COMMON_GRPC_CHANNEL_NAME = "adminCommonGrpcChannel";

    @Resource
    private GrpcClientProperties grpcClientProperties;

    @Resource
    private IGrpcClientPool pool;

    Long stubDeadline;

    Long shortStubDeadline;

    Long futureTimeout;

    @PostConstruct
    public void init() {
        stubDeadline = grpcClientProperties.getStubDeadline();
        shortStubDeadline = grpcClientProperties.getShortStubDeadline();
        futureTimeout = grpcClientProperties.getFutureTimeout();
        List<GrpcChannelInfo> channelInfoList = grpcClientProperties.getChannelInfo();
        for (GrpcChannelInfo channelInfo : channelInfoList) {
            pool.setShortcut(channelInfo.getChannelName(), channelInfo.getHost(), channelInfo.getPort());
        }
    }

    public AdminRoleAuthServiceGrpc.AdminRoleAuthServiceBlockingStub adminRoleAuthServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return AdminRoleAuthServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public SecurityServiceGrpc.SecurityServiceBlockingStub securityServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return SecurityServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public CommissionServiceGrpc.CommissionServiceBlockingStub commissionServiceBlockingStub(String channelName){
        Channel channel = pool.borrowChannel(channelName);
        return CommissionServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public AdminUserServiceGrpc.AdminUserServiceBlockingStub adminUserServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return AdminUserServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public BusinessLogServiceGrpc.BusinessLogServiceBlockingStub businessLogServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return BusinessLogServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public AdminUserIpWhitelistServiceGrpc.AdminUserIpWhitelistServiceBlockingStub adminUserIpWhitelistServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return AdminUserIpWhitelistServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public CountryServiceGrpc.CountryServiceBlockingStub countryServiceBlockingStub(String channelName){
        Channel channel = pool.borrowChannel(channelName);
        return CountryServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public SaasTokenServiceGrpc.SaasTokenServiceBlockingStub saasTokenServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return SaasTokenServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public BalanceServiceGrpc.BalanceServiceBlockingStub balanceServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return BalanceServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public AccountServiceGrpc.AccountServiceBlockingStub accountServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return AccountServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public BrokerTradeFeeSettingServiceGrpc.BrokerTradeFeeSettingServiceBlockingStub brokerTradeFeeSettingServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return BrokerTradeFeeSettingServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public BrokerSmsTemplateServiceGrpc.BrokerSmsTemplateServiceBlockingStub brokerSmsTemplateServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return BrokerSmsTemplateServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public io.bhex.base.clear.CommissionServiceGrpc.CommissionServiceBlockingStub clearCommissionServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return io.bhex.base.clear.CommissionServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public MessageServiceGrpc.MessageServiceBlockingStub messageServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return MessageServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public BrokerAccountTradeFeeSettingServiceGrpc.BrokerAccountTradeFeeSettingServiceBlockingStub brokerAccountTradeFeeSettingServiceBlockingStub(String channelName){
        Channel channel = pool.borrowChannel(channelName);
        return BrokerAccountTradeFeeSettingServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

}

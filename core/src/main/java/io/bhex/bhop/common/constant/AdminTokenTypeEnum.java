package io.bhex.bhop.common.constant;


public enum AdminTokenTypeEnum {
    UNKNOWN(0, false, 0, 0, 0, "", "0", ""),
    CHAIN(1, false, 0, 0, 0, "", "0", ""),
    ETH(2, false, 15, 30, 18, "https://etherscan.io/tx/", "0", "ETH"),
    BTC(3, false, 1, 6, 0, "", "0", ""),
    EOS(4, true, 1, 6, 4, "https://eospark.com/tx/", "0.1", "EOS"),
    BH_CARD(5, false, 0, 0, 0, "", "0", ""),
    EXCHANGE_PRIVATE_TOKEN(6, false, 0, 0, 0, "", "0", ""),
    REAL_MONEY(7, false, 0, 0, 0, "", "0", ""),
    OPTION(8, false, 0, 0, 0, "", "0", ""),
    FUTURE(9, false, 0, 0, 0, "", "0", ""),
    TRX(10, false, 12, 30, 8, "https://tronscan.org/#/transaction/", "0.1", "TRX"),
    NEO(11, false, 1, 1, 8, "https://neotracker.io/tx/", "0", "GAS"),
    HECO(12, false, 15, 30, 18, "https://hecoinfo.com/tx/", "0.0088", "HT"),
    BSC(13, false, 15, 30, 18, "https://bscscan.com/tx/", "0.0012", "BNB"),
    SOLANA(14, false, 10, 100, 9, "https://explorer.solana.com/", "0", "SOL"),
    CHZ(15, false, 15, 30, 18, "https://explorer.chiliz.com/tx/", "0", "CHZ"),
    EZCHAIN(16, false, 15, 30, 18, "https://cchain-explorer.ezchain.com/tx/", "0", "EZC");
    private int type;
    private boolean needTag;
    private int confirmCount;
    private int canWithdrawConfirmCount;
    private int minPrecision;
    private String exploreUrl;
    private String platformFee;
    private String gasFeeToken;

    AdminTokenTypeEnum(int type, boolean needTag, int confirmCount, int canWithdrawConfirmCount, int minPrecision, String exploreUrl, String platformFee, String gasFeeToken) {
        this.type = type;
        this.needTag = needTag;
        this.confirmCount = confirmCount;
        this.canWithdrawConfirmCount = canWithdrawConfirmCount;
        this.minPrecision = minPrecision;
        this.exploreUrl = exploreUrl;
        this.platformFee = platformFee;
        this.gasFeeToken = gasFeeToken;
    }

    public String getPlatformFee() {
        return platformFee;
    }

    public int getType() {
        return type;
    }

    public boolean isNeedTag() {
        return needTag;
    }

    public int getConfirmCount() {
        return confirmCount;
    }

    public int getCanWithdrawConfirmCount() {
        return canWithdrawConfirmCount;
    }

    public int getMinPrecision() {
        return minPrecision;
    }

    public String getExploreUrl() {
        return exploreUrl;
    }

    public String getGasFeeToken() {
        return gasFeeToken;
    }

    public static AdminTokenTypeEnum getByType(int type) {
        for (AdminTokenTypeEnum value : values()) {
            if (value.getType() == type) {
                return value;
            }
        }
        throw new IllegalArgumentException("type: " + type);
    }

//    public static void main(String[] args) {
//        //private int type;
//        //    private boolean needTag;
//        //    private int confirmCount;
//        //    private int canWithdrawConfirmCount;
//        //    private int minPrecision;
//        //    private String exploreUrl;
//        //    private String platformFee;
//        for (AdminTokenTypeEnum value : values()) {
//
//            String sql = "INSERT INTO `tb_base_config` (`id`, `broker_id`, `conf_group`, `conf_key`, `conf_value`, `status`, `created`, `updated`)\n" +
//                    "VALUES\n" +
//                    "\t(null, 0, 'token.chain.type', '#type', '#value', 1, '2020-11-26 14:13:33.847', '2020-11-26 14:13:33.847');";
//
//            Map<String, Object> item = Maps.newHashMap();
//            item.put("type", value.getType());
//            item.put("needTag", value.isNeedTag());
//            item.put("confirmCount", value.getConfirmCount());
//            item.put("canWithdrawConfirmCount", value.getCanWithdrawConfirmCount());
//            item.put("minPrecision", value.getMinPrecision());
//            item.put("exploreUrl", value.getExploreUrl());
//            item.put("platformFee", value.getPlatformFee());
//            System.out.println(sql.replace("#type", value.name()).replace("#value",JsonUtil.defaultGson().toJson(item)));
//        }
//    }
}

//package io.bhex.bhop.common.util;
//
//import io.bhex.base.proto.Decimal;
//
//import java.math.BigDecimal;
//
///**
// * @Description:
// * @Date: 2018/10/19 上午11:02
// * @Author: liwei
// * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
// */
//public class BigDecimalUtil {
//
//    /**
//     * 将proto中的Decimal转换成BigDecimal并去除后面没必要的0
//     * @param decimal
//     * @return
//     */
//    public static BigDecimal convertDecimal(Decimal decimal) {
//        BigDecimal num = new BigDecimal(decimal.getStr());
//        return num.setScale(8, BigDecimal.ROUND_DOWN).stripTrailingZeros();
//    }
//
//    public static BigDecimal convert(BigDecimal num){
//        return num.setScale(8, BigDecimal.ROUND_DOWN)
//                .stripTrailingZeros();
//    }
//
//    public static BigDecimal convert(String numStr){
//        BigDecimal num = new BigDecimal(numStr);
//        return num.setScale(8, BigDecimal.ROUND_DOWN).stripTrailingZeros();
//    }
//}

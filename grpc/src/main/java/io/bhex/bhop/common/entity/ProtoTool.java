package io.bhex.bhop.common.entity;

import java.lang.reflect.Field;

public class ProtoTool {

    private static Class transClazz = ExchangeCommissionDetailEntity.class;

    public static void main(String[] args) {
        Field[] fields = transClazz.getDeclaredFields();
        for(int i=0; i<fields.length; i++){
            Field field = fields[i];
            String type = field.getType().getName();
            String realType = "";
            if(type.endsWith(".Long")){
                realType = "int64";
            }
            else if(type.endsWith(".String")){
                realType = "string";
            }
            else if(type.endsWith(".Date")){
                realType = "int64";
            }
            else if(type.endsWith(".Timestamp")){
                realType = "int64";
            }
            else if(type.endsWith(".Integer")){
                realType = "int32";
            }
            else if(type.endsWith(".BigDecimal")){
                realType = "string";
            }


            String name = field.getName();

            String realName = "";
            for(int j=0; j<name.length(); j++){
                char c = name.charAt(j);
                if(c>='A' && c<='Z'){
                    c+=32;
                    realName += "_" + c;
                }
                else {
                    realName += c;
                }
            }
            System.out.println(realType+" "+realName+" = "+(i+1)+";");
        }

    }
}

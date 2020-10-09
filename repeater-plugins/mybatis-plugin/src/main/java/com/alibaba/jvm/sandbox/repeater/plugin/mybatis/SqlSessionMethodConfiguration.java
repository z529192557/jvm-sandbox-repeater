package com.alibaba.jvm.sandbox.repeater.plugin.mybatis;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhuangpeng
 * @since 2020/10/9
 */
public class SqlSessionMethodConfiguration {

    public static Map<String,SqlCommandType> methodName2Type = new HashMap<>();

    static {
        //SELECT
        methodName2Type.put("selectList",SqlCommandType.SELECT);
        methodName2Type.put("selectMap",SqlCommandType.SELECT);
        methodName2Type.put("selectCursor",SqlCommandType.SELECT);
        methodName2Type.put("selectOne",SqlCommandType.SELECT);
        //INSERT
        methodName2Type.put("insert",SqlCommandType.INSERT);
        //UPDATE
        methodName2Type.put("update",SqlCommandType.UPDATE);
        //DELETE
        methodName2Type.put("delete",SqlCommandType.DELETE);
        //FLUSH
        methodName2Type.put("flushStatements",SqlCommandType.FLUSH);

    }

    public static SqlCommandType getSqlCommmandType(String methodName){
        SqlCommandType sqlCommandType = methodName2Type.get(methodName);
        if(null == sqlCommandType){
            sqlCommandType = SqlCommandType.UNKNOWN;
        }
        return sqlCommandType;
    }
}

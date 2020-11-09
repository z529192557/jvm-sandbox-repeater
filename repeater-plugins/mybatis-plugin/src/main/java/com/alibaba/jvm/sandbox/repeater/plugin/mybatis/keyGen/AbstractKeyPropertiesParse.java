package com.alibaba.jvm.sandbox.repeater.plugin.mybatis.keyGen;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.jvm.sandbox.repeater.plugin.mybatis.MybatisInvocationProcessor;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhuangpeng
 * @since 2020/11/6
 */
public abstract class AbstractKeyPropertiesParse implements KeyPropertiesParse{

    protected final static Logger log = LoggerFactory.getLogger(AbstractKeyPropertiesParse.class);

    Map<String,Object> getKeyPropertiesValue(Object param,String[] keyPropertise){
        Map<String,Object> maps = new HashMap<>();
        if(null != keyPropertise && keyPropertise.length > 0){
            Object realParam = param;

            if(param instanceof Object[] && ((Object[])param).length > 1){
                log.debug("unsupport mock insert key generator for multiple param, maybe support for futrue");
                return maps;
            }else if(param instanceof  Object[]){
                realParam = ((Object[])param)[0];
            }

            for(int i = 0; i < keyPropertise.length ; i++){
                if(keyPropertise[i].indexOf('.') != -1){
                    log.debug("unsupport mock insert key generator for {}, maybe support for futrur",keyPropertise[i]);
                    continue;
                }

                Field field = FieldUtils.getDeclaredField(realParam.getClass(),keyPropertise[i],true);
                if(null != field){
                    try {
                        Object value = field.get(realParam);
                        if(null != value){
                            maps.put(keyPropertise[0],value);
                        }
                    } catch (Exception e) {

                    }
                }
            }

        }
        return maps;
    }



    protected void setValueForKeyProperties(Object param,Map<String,Object> key2Value){
       if(null != key2Value && !key2Value.isEmpty()){
           Object realParam = param;
           if(param instanceof Object[] && ((Object[])param).length > 1){
               log.debug("unsupport mock insert key generator for multiple param, maybe support for futrue");
               return;
           }else if(param instanceof  Object[]){
               realParam = ((Object[])param)[0];
           }

           Iterator<Entry<String, Object>> iterable = key2Value.entrySet().iterator();
           while (iterable.hasNext()){
               Entry<String, Object> entry = iterable.next();
               Field field = FieldUtils.getDeclaredField(realParam.getClass(),entry.getKey(),true);
               if(null != field){
                   try {
                       field.set(realParam,entry.getValue());
                   } catch (Exception e) {

                   }
               }
           }
       }
    }
}

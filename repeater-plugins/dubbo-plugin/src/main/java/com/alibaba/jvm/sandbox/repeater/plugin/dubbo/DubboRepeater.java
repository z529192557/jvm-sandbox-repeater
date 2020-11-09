package com.alibaba.jvm.sandbox.repeater.plugin.dubbo;

import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.jvm.sandbox.repeater.plugin.Constants;
import com.alibaba.jvm.sandbox.repeater.plugin.core.bridge.ClassloaderBridge;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.AbstractRepeater;
import com.alibaba.jvm.sandbox.repeater.plugin.core.trace.TraceFactory;
import com.alibaba.jvm.sandbox.repeater.plugin.core.util.ClassUtils;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.ArsmHeader;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.DubboInvocation;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.Invocation;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.InvokeType;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.RepeatContext;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.RepeaterConfig;
import com.alibaba.jvm.sandbox.repeater.plugin.dubbo.DubboMetaConfig.DubboMetaConfigBuilder;
import com.alibaba.jvm.sandbox.repeater.plugin.exception.RepeatException;
import com.alibaba.jvm.sandbox.repeater.plugin.spi.Repeater;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.context.ConfigManager;
import org.apache.dubbo.rpc.service.GenericService;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * {@link DubboRepeater} dubbo回放器
 * <p>
 *
 * @author zhaoyb1990
 */
@MetaInfServices(Repeater.class)
public class DubboRepeater extends AbstractRepeater {


    private static final Cache<String, Object> SERVICE_PROXY = CacheBuilder
        .newBuilder()
        .maximumSize(4096)
        .expireAfterWrite(360, TimeUnit.MINUTES).build();

    @Override
    protected Object executeRepeat(RepeatContext context) throws Exception {
        Invocation invocation = context.getRecordModel().getEntranceInvocation();
        if (!(invocation instanceof DubboInvocation)) {
            throw new RepeatException("type miss match, required DubboInvocation but found " + invocation.getClass().getSimpleName());
        }
        DubboInvocation dubboInvocation = (DubboInvocation)invocation;
        if(dubboInvocation.isAliDubbo()){
            return executeAliDubboRepeat(context,dubboInvocation);
        }
        return executeApacheDubboRepeat(context,dubboInvocation);
    }

    private Object executeAliDubboRepeat(RepeatContext context, DubboInvocation dubboInvocation) throws Exception {
        String serviceKey = getServicekey(getServiceKeyFromExtension(context),getServiceName(dubboInvocation));
        Object service= SERVICE_PROXY.getIfPresent(serviceKey);
        if(null == service){
            com.alibaba.dubbo.config.ReferenceConfig reference = new com.alibaba.dubbo.config.ReferenceConfig<>();
            reference.setApplication(new com.alibaba.dubbo.config.ApplicationConfig("repeater-local-consumer"));
            DubboMetaConfig dubboMetaConfig = getDubboMetaConfig(context,dubboInvocation);
            String url = buildDubboUrl(dubboMetaConfig.getPort(),dubboMetaConfig.getGroup());
            reference.setUrl(url);
            reference.setInterface(dubboInvocation.getInterfaceName());
            reference.setCheck(false);
            reference.setVersion(dubboMetaConfig.getVersion());
            reference.setTimeout(dubboMetaConfig.getTimeOut());
            reference.setCluster("failfast");
            service = reference.get();
            SERVICE_PROXY.put(serviceKey,service);
        }
        Method method = getTargetMethod(service.getClass(),dubboInvocation);
        //将traceId通过dubbo上下文传递给dubbo provider
        RpcContext.getContext().setAttachment(Constants.REPEAT_TRACE_ID, TraceFactory.getTraceId());

        //移除attachments中ARMS相关的traceId
        for(ArsmHeader arsmHeader : ArsmHeader.values()){
            dubboInvocation.getAttachments().remove(arsmHeader.getName());
        }

        //还原Rpc上下文
        RpcContext.getContext().getAttachments().putAll(dubboInvocation.getAttachments());
        return method.invoke(service,dubboInvocation.getRequest());
    }

    private String getServicekey(String serviceKeyFromExtension, String serviceName) {
        StringBuilder sb = new StringBuilder();
        if(StringUtils.isNotBlank(serviceKeyFromExtension)){
            sb.append(serviceKeyFromExtension);
            sb.append("-");
        }
        sb.append(serviceName);
        return sb.toString();
    }

    /**
     * 获取dubbo配置的优先级,从高倒低:
     * 1.传入的回放配置
     * 2.当前机器上已有的配置
     * 3.录制到的配置
     * 4.默认的兜底配置
     */
    private DubboMetaConfig getDubboMetaConfig(RepeatContext context, DubboInvocation dubboInvocation)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        String recordServiceName = getServiceName(dubboInvocation);
        DubboMetaConfigBuilder builder = DubboMetaConfig.getBuilder();
        String group = null;
        String version = null;
        String port = null;

        //1. 先从配置拿
        getDubboMetaFromExtension(context,builder);

        if(builder.hasNull()){
            //2.从本机导出服务拿
            if(DubboRunTimeUtil.iskaolaDubbo()){
                //dubbok的serviceKey: {DUBBO_GROUP}/{DUBBO_INTERFACE}/{DUBB_VERSION}
                Class applicationModel =  getAliDubboApplicationModelObject();
                //先寻找serviceName完全一致的服务
                Collection providerModels = (Collection)MethodUtils.invokeStaticMethod(applicationModel,"getProviderModel",recordServiceName);
                if(null != providerModels && providerModels.size() > 0){
                    log.info("find fit service ：{}",recordServiceName);
                    Iterator iterator = providerModels.iterator();
                    while (iterator.hasNext()){
                        parseMetaConfig(builder, iterator.next(), recordServiceName);
                        break;
                    }
                }else{
                    //如果找不到，则从导出的所有服务中，找到和录制的接口全限定名一致的导出服务，如果有多个，则选择第一个匹配上的
                    List allProviderModels = (List)MethodUtils.invokeStaticMethod(applicationModel,"allProviderModels");
                    boolean find = false;
                    for(Object o : allProviderModels){
                        String serviceName = (String)MethodUtils.invokeMethod(o,"getServiceName");
                        if(StringUtils.isNotBlank(serviceName) && serviceName.contains(dubboInvocation.getInterfaceName())){
                            if(find){
                                log.info("Interface {} export multiple service, use default 1",dubboInvocation.getInterfaceName());
                                break;
                            }
                            parseMetaConfig(builder, o, serviceName);
                            find = true;
                        }
                    }
                }
            }

            //3.从录制的dubboInvocation中拿
            if(null == builder.getGroup()){
                builder.setGroup(dubboInvocation.getGroup());
            }
            if(null == builder.getVersion()){
                builder.setVersion(dubboInvocation.getVersion());
            }
            if(null == builder.getPort()){
                builder.setPort(dubboInvocation.getPort());
            }

            //4.兜底配置
            if(null == builder.getGroup()){
                builder.setGroup("");
            }
            if(null == builder.getVersion()){
                builder.setVersion("1.0.0");
            }
            if(null == builder.getPort()){
                builder.setPort("20880");
            }
        }
        return builder.build();
    }

    private void parseMetaConfig(DubboMetaConfigBuilder builder, Object o, String serviceName)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String version;
        String port;
        String group;
        if(serviceName.contains("/") && null == builder.getGroup()){
            String[] serviceMeta = serviceName.split("/");
            group = serviceMeta[0];
            builder.setGroup(group);
        }
        if(serviceName.contains(":") && null == builder.getVersion()){
            String[] serviceMeta = serviceName.split(":");
            version = serviceMeta[1];
            builder.setVersion(version);
        }

        if(null == builder.getPort()){
            List urls = (List)MethodUtils.invokeMethod(MethodUtils.invokeMethod(o,"getMetadata"),"getExportedUrls");
            if(null != urls && urls.size() > 1){
                log.info("Servce {} has more than one export URl, use default 1");
            }
            if(null != urls && urls.size() > 0){
                com.alibaba.dubbo.common.URL url = (com.alibaba.dubbo.common.URL)urls.get(0);
                port = String.valueOf(url.getPort());
                builder.setPort(port);
            }
        }
    }

    private String getServiceName(DubboInvocation dubboInvocation) {
        StringBuilder sb = new StringBuilder();
        if(null != dubboInvocation.getGroup()){
            sb.append(dubboInvocation.getGroup());
            sb.append("/");
        }
        if(null != dubboInvocation.getInterfaceName()){
            sb.append(dubboInvocation.getInterfaceName());
        }
        if(null != dubboInvocation.getVersion()) {
            sb.append(":");
            sb.append(dubboInvocation.getVersion());
        }
        return sb.toString();
    }

    private void getDubboMetaFromExtension(RepeatContext context,DubboMetaConfigBuilder builder) {
        builder.setGroup(getDubboGroup(context)).setVersion(getDubboVersion(context)).setPort(getDubboPort(context)).setTimeOut(getDubboTimeOut(context));
    }

    private String getServiceKeyFromExtension(RepeatContext context) {
        StringBuilder sb = new StringBuilder();
        String group = getDubboGroup(context);
        if(null != group){
            sb.append(group);
        }
        String version = getDubboVersion(context);
        if(null != version){
            sb.append(version);
        }
        String port = getDubboPort(context);
        if(null != version){
            sb.append(version);
        }
        return sb.toString();
    }

    private Method getTargetMethod(Class<?> clazz, DubboInvocation invocation) throws Exception{
        ClassLoader classLoader = ClassloaderBridge.instance().decode(invocation.getSerializeToken());
        // @see HSFUtils.processIdentity
        String[] parameterTypes = invocation.getParameterTypes();
        Class[] paramsTypes = new Class[parameterTypes.length];
        if (parameterTypes.length > 0) {
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
            }
            paramsTypes = ClassUtils.findClasses(parameterTypes, classLoader);
        }
        return clazz.getDeclaredMethod(invocation.getMethodName(), paramsTypes);
    }

    private String buildDubboUrl(String port,String group) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("dubbo://");
        urlBuilder.append("127.0.0.1:");
        urlBuilder.append(port);
        urlBuilder.append("?");
        urlBuilder.append("group=");
        urlBuilder.append(group);
        return urlBuilder.toString();
    }

    String getDubboGroup(RepeatContext context) {
        return getDubboMetaExtension(context,"dubbo.group");
    }

    String getDubboPort(RepeatContext context){
        return getDubboMetaExtension(context,"dubbo.port");
    }

    String getDubboVersion(RepeatContext context){
        return getDubboMetaExtension(context,"dubbo.version");
    }

    Integer getDubboTimeOut(RepeatContext context){
        String timeOut = getMetaExtension(context, "dubbo.timeOut");
        if(null != timeOut){
            return Integer.valueOf(timeOut);
        }
        return null;
    }

    String getDubboMetaExtension(RepeatContext context,String key){
        return getMetaExtension(context, key);
    }


    private String getMetaExtension(RepeatContext context, String key) {
        if(null != context.getMeta().getExtension()){
            return context.getMeta().getExtension().get(key);
        }
        return null;
    }

    private Class getAliDubboApplicationModelObject(){
        Class aliDubboClass = null;
        try {
            aliDubboClass = this.getClass().getClassLoader().loadClass(Constants.ALI_DUBBO_CLASS);
        } catch (ClassNotFoundException e) {
            //理论上不会发生
            log.warn("can not find dubbo class :{}",Constants.ALI_DUBBO_CLASS,e);
        }
        return aliDubboClass;
    }

    /**
     * TODO dubbo回放时需要指定的group和version，优先从回放机器上export的服务中获取
     */
    private Object executeApacheDubboRepeat(RepeatContext context,DubboInvocation dubboInvocation) {
        ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName("jvm-sandbox-repeater");
        // require address to initialize registry config
        RegistryConfig registryConfig = new RegistryConfig();
        String address = context.getMeta().getExtension().get("dubbo.address");
        // using special address
        if (StringUtils.isNotEmpty(address)) {
            registryConfig.setAddress(address);
        } else {
            registryConfig.setAddress(dubboInvocation.getAddress());
        }
        String group = context.getMeta().getExtension().get("dubbo.group");
        // using special group
        if (StringUtils.isNotEmpty(group)) {
            registryConfig.setGroup(group);
        } else {
            registryConfig.setGroup(dubboInvocation.getGroup());
        }
        reference.setApplication(ConfigManager.getInstance().getApplication().orElse(applicationConfig));
        reference.setRegistry(registryConfig);

        // set protocol / interface / version / timeout
        reference.setProtocol(dubboInvocation.getProtocol());
        reference.setInterface(dubboInvocation.getInterfaceName());
        if (StringUtils.isNotEmpty(dubboInvocation.getVersion())) {
            reference.setVersion(dubboInvocation.getVersion());
        }
        // timeout
        reference.setTimeout(context.getMeta().getTimeout());
        // use generic invoke
        reference.setGeneric(true);
        // fix issue #45
        ClassLoader swap = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(GenericService.class.getClassLoader());
            GenericService genericService = reference.get();
            return genericService.$invoke(dubboInvocation.getMethodName(), dubboInvocation.getParameterTypes(), dubboInvocation.getRequest());
        } finally {
            Thread.currentThread().setContextClassLoader(swap);
        }
    }

    @Override
    public InvokeType getType() {
        return InvokeType.DUBBO;
    }

    @Override
    public String identity() {
        return "dubbo,ali-dubbo";
    }

    @Override
    public boolean enable(RepeaterConfig config) {
       String[] identities = identity().split(",");
       for(int i = 0; i < identities.length ; i++){
           if(config != null && config.getRepeatIdentities().contains(identities[i])){
               return Boolean.TRUE;
           }
       }
        return Boolean.FALSE;
    }

}

package com.alibaba.jvm.sandbox.repeater.plugin.domain;


/**
 * <p>
 *
 * @author zhaoyb1990
 */
public class MockInvocation implements java.io.Serializable {
    private int index;
    private String traceId;
    private String repeatId;
    private boolean success;
    private boolean skip;
    private String skipMsg;
    private long cost;
    private String originUri;
    private String currentUri;
    private Object[] originArgs;
    private Object[] currentArgs;
    /**
     *  currentArgs序列化
     *  要对currentArgs进行及时序列化，因为例如mybatis框架可能会修改currentArgs的属性值
     */
    private String currentArgsSerialized;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getRepeatId() {
        return repeatId;
    }

    public void setRepeatId(String repeatId) {
        this.repeatId = repeatId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public String getOriginUri() {
        return originUri;
    }

    public void setOriginUri(String originUri) {
        this.originUri = originUri;
    }

    public String getCurrentUri() {
        return currentUri;
    }

    public void setCurrentUri(String currentUri) {
        this.currentUri = currentUri;
    }

    public Object[] getOriginArgs() {
        return originArgs;
    }

    public void setOriginArgs(Object[] originArgs) {
        this.originArgs = originArgs;
    }

    public Object[] getCurrentArgs() {
        return currentArgs;
    }

    public void setCurrentArgs(Object[] currentArgs) {
        this.currentArgs = currentArgs;
    }

    public String getCurrentArgsSerialized() {
        return currentArgsSerialized;
    }

    public void setCurrentArgsSerialized(String currentArgsSerialized) {
        this.currentArgsSerialized = currentArgsSerialized;
    }

    public String getSkipMsg() {
        return skipMsg;
    }

    public void setSkipMsg(String skipMsg) {
        this.skipMsg = skipMsg;
    }
}

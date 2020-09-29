package com.alibaba.jvm.sandbox.repeater.plugin.core.trace;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * @author zhuangpeng
 * @since 2020/9/28
 */
public class DefaultTraceGenerator{

    private static AtomicInteger count = new AtomicInteger(10000);

    private static String IP_COMPLETION = getCompletionIp();

    private static String END_FLAG = "ed";

    public String generate() {
        StringBuilder builder = new StringBuilder(32);
        builder.append(IP_COMPLETION)
            .append(System.currentTimeMillis())
            .append(getNext()).append(END_FLAG);
        return builder.toString();
    }

    public boolean isValid(String traceId) {
        if (StringUtils.isBlank(traceId)) {
            return false;
        }
        if (traceId.length() != 32 && !traceId.endsWith(END_FLAG)) {
            return false;
        }
        return NumberUtils.isDigits(traceId.substring(25, 30));
    }

    private Integer getNext() {
        for (; ; ) {
            int current = count.get();
            int next = (current > 90000) ? 10000 : current + 1;
            if (count.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    /**
     * 补全IP为12位数字
     * <p>
     * eg:127.0.0.1 -> 127000000001
     *
     * @return 补全后的IP
     */
    private static String getCompletionIp() {
        String ip;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            ip = "127.0.0.1";
        }
        StringBuilder builder = new StringBuilder();
        String[] bits = ip.split("\\.");
        for (String bit : bits) {
            if (bit.length() == 1) {
                builder.append("00").append(bit);
            } else if (bit.length() == 2) {
                builder.append("0").append(bit);
            } else {
                builder.append(bit);
            }
        }
        return builder.toString();
    }
}

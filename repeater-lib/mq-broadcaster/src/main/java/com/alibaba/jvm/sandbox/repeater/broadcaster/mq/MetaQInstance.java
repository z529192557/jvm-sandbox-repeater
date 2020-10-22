package com.alibaba.jvm.sandbox.repeater.broadcaster.mq;

import com.taobao.metaq.client.MetaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;

import static com.alibaba.jvm.sandbox.repeater.broadcaster.mq.MessageConfig.GROUP_NAME;

/**
 *
 * @author zhuangpeng
 */
public class MetaQInstance {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaQInstance.class);

    /**
     * 设置metaQ producer支持最大消息长度 4M
     */
    private final static int MAX_MESSAGE_SIZE = 1024 * 1024 * 4;
    /**
     * 走http发送消息的阈值 100KB
     */
    private final static int MAX_METAQ_LIMIT_SIZE = 1024 * 100;

    private MetaProducer producer;

    private boolean launch;


    private MetaQInstance() {
        producer = new MetaProducer(GROUP_NAME);
        if (EnvUtil.isPrepare()) {
            producer.setUnitName("pre");
            producer.setInstanceName("pre");
        } else if (EnvUtil.isOnline()) {
            producer.setUnitName("sh");
            producer.setInstanceName("sh");
        }
        try {
            producer.start();
            launch = true;
        } catch (MQClientException e) {
            LOGGER.error("Error occurred when launch metaQ producer.", e);
        }
        // JVM回收资源钩子
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (producer != null) {
                    shutdown();
                }
            }
        });
    }

    public static MetaQInstance getInstance() {
        return MetaQInstance.LazyInstanceHolder.INSTANCE;
    }

    private void shutdown() {
        producer.shutdown();
        launch = false;
    }

    public void send(String topic,String tag, String key, byte[] bytes) {
        if (!launch) {
            LOGGER.error("MetaQ producer have not been started,can not send message.");
            return;
        }
        // reset max message to default
        if (producer.getMaxMessageSize() != MAX_MESSAGE_SIZE) {
            producer.setMaxMessageSize(MAX_MESSAGE_SIZE);
        }
        try {
            Message message = new Message(topic, tag, key, bytes);
            SendResult sendResult = producer.send(message);
            if (sendResult == null) {
                LOGGER.warn("Send message fail,topic={},tag={},key={}", topic, tag, key);
            } else {
                LOGGER.info("Send message success,topic={},tag={},key={},messageId={}", topic, tag, key, sendResult.getMsgId());
            }
        } catch (Throwable t) {
            LOGGER.error("Error occurred when send metaQ message,tag={},key={},content={}", tag, key, new String(bytes, Charset.forName("UTF-8")), t);
        }
    }

    private static class LazyInstanceHolder {
        private static final MetaQInstance INSTANCE = new MetaQInstance();
    }
}

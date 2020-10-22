package com.alibaba.jvm.sandbox.repeater.broadcaster.mq;

/**
 * <p>
 *
 * @author yuebing.zyb@alibaba-inc.com 2018/1/25 09:41.
 */
public class MessageConfig {

    /**
     * message thread name
     */
    public final static String MESSAGE_THREAD_NAME = "REPEATER-CLOUD-NATIVE-MESSAGE-THREAD";
    /**
     * metaQ producer group name
     */
    public final static String GROUP_NAME = "REPEATER-CLOUD-NATIVE-META-PRODUCER";

    /**
     * metaQ global topic name
     */
    public static String DEFAULT_TOPIC_NAME = "JVM_SANDBOX";

    public static String DEFAULT_RECORD_TAG_NAME = "repeater-cloud-native-record-tag";

    public static String DEFAULT_REPLAY_TAG_NAME = "repeater-cloud-native-replay-tag";

    public static String METAQ_RECORD_BROADCASTER_TOPIC = "METAQ_RECORD_BROADCASTER_TOPIC";

    public static String METAQ_RECORD_BROADCASTER_TAG = "METAQ_RECORD_BROADCASTER_TAG";


    public static String METAQ_REPLAY_BROADCASTER_TOPIC = "METAQ_REPLAY_BROADCASTER_TOPIC";

    public static String METAQ_REPLAY_BROADCASTER_TAG = "METAQ_REPLAY_BROADCASTER_TAG";
}

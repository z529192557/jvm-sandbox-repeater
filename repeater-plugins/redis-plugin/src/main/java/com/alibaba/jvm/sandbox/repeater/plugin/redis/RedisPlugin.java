package com.alibaba.jvm.sandbox.repeater.plugin.redis;

import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.alibaba.jvm.sandbox.repeater.plugin.api.InvocationListener;
import com.alibaba.jvm.sandbox.repeater.plugin.api.InvocationProcessor;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.AbstractInvokePluginAdapter;
import com.alibaba.jvm.sandbox.repeater.plugin.core.impl.api.DefaultEventListener;
import com.alibaba.jvm.sandbox.repeater.plugin.core.model.EnhanceModel;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.InvokeType;
import com.alibaba.jvm.sandbox.repeater.plugin.spi.InvokePlugin;
import com.google.common.collect.Lists;

import org.kohsuke.MetaInfServices;
import java.util.*;

/**
 * {@link RedisPlugin} jedis的java插件
 * <p>
 * 拦截{@code redis.clients.jedis.commands}包下面的commands实现类
 *
 * 获取redis常用操作指令，不包括所有命令
 * 详见Jedis类、BinaryJedis类的实现接口
 * </p>
 *
 * @author zhaoyb1990
 */
@MetaInfServices(InvokePlugin.class)
public class RedisPlugin extends AbstractInvokePluginAdapter {

    String[] COMMANDS = {  "zcount",
        "sunionstore",
        "scriptKill",
        "zunionstore",
        "del",
        "echo",
        "hscan",
        "zinterstore",
        "psubscribe",
        "type",
        "sinterstore",
        "xrevrange",
        "setex",
        "xadd",
        "zlexcount",
        "brpoplpush",
        "bitcount",
        "llen",
        "zscan",
        "lpushx",
        "bitpos",
        "setnx",
        "xack",
        "hvals",
        "evalsha",
        "substr",
        "randomKey",
        "geodist",
        "zrangeByLex",
        "geoadd",
        "expire",
        "bitop",
        "zrangeByScore",
        "smove",
        "lset",
        "decrBy",
        "pttl",
        "scan",
        "zrank",
        "xtrim",
        "blpop",
        "zremrangeByLex",
        "rpoplpush",
        "get",
        "lpop",
        "persist",
        "georadius",
        "scriptExists",
        "set",
        "srandmember",
        "incr",
        "setbit",
        "hexists",
        "expireAt",
        "pexpire",
        "zcard",
        "bitfield",
        "zrevrangeByLex",
        "sinter",
        "srem",
        "getrange",
        "rename",
        "watch",
        "zrevrank",
        "exists",
        "setrange",
        "zremrangeByRank",
        "sadd",
        "sdiff",
        "zrevrange",
        "unwatch",
        "getbit",
        "scard",
        "sdiffstore",
        "zrevrangeByScore",
        "zincrby",
        "rpushx",
        "psetex",
        "strlen",
        "zrevrangeWithScores",
        "hdel",
        "zremrangeByScore",
        "geohash",
        "xgroupDestroy",
        "brpop",
        "lrem",
        "hlen",
        "decr",
        "scriptLoad",
        "lpush",
        "lindex",
        "zrange",
        "incrBy",
        "getSet",
        "xlen",
        "ltrim",
        "georadiusReadonly",
        "touch",
        "incrByFloat",
        "rpop",
        "sort",
        "xdel",
        "zrevrangeByScoreWithScores",
        "xreadGroup",
        "xclaim",
        "pfadd",
        "eval",
        "linsert",
        "pfcount",
        "hkeys",
        "hsetnx",
        "hincrBy",
        "xpending",
        "hgetAll",
        "xgroupSetID",
        "georadiusByMemberReadonly",
        "keys",
        "restoreReplace",
        "hset",
        "spop",
        "zrangeWithScores",
        "hincrByFloat",
        "hmset",
        "renamenx",
        "zrem",
        "dump",
        "msetnx",
        "hmget",
        "sunion",
        "hget",
        "xread",
        "zadd",
        "move",
        "restore",
        "geopos",
        "subscribe",
        "mset",
        "zrangeByScoreWithScores",
        "zscore",
        "pexpireAt",
        "georadiusByMember",
        "ttl",
        "lrange",
        "hstrlen",
        "smembers",
        "xgroupCreate",
        "unlink",
        "pfmerge",
        "rpush",
        "publish",
        "sscan",
        "mget",
        "xrange",
        "append",
        "sismember",
        "xgroupDelConsumer",
        "sismember",
        "eval",
        "evalsha",
        "scriptExists",
        "scriptLoad"};


    @Override
    protected List<EnhanceModel> getEnhanceModels() {
        EnhanceModel jedis = EnhanceModel.builder()
                .classPattern("redis.clients.jedis.Jedis")
                .methodPatterns(EnhanceModel.MethodPattern.transform(COMMANDS))
                .watchTypes(Event.Type.BEFORE, Event.Type.RETURN, Event.Type.THROWS)
                .build();

        EnhanceModel jedisCluster = EnhanceModel.builder()
            .classPattern("redis.clients.jedis.JedisCluster")
            .methodPatterns(EnhanceModel.MethodPattern.transform(COMMANDS))
            .watchTypes(Event.Type.BEFORE, Event.Type.RETURN, Event.Type.THROWS)
            .build();

        EnhanceModel binaryJedis = EnhanceModel.builder()
                .classPattern("redis.clients.jedis.BinaryJedis")
                .methodPatterns(EnhanceModel.MethodPattern.transform(COMMANDS))
                .watchTypes(Event.Type.BEFORE, Event.Type.RETURN, Event.Type.THROWS)
                .build();
        return Lists.newArrayList(jedis,jedisCluster,binaryJedis);
    }


    @Override
    protected InvocationProcessor getInvocationProcessor() {
        return new RedisProcessor(getType());
    }

    @Override
    public InvokeType getType() {
        return InvokeType.REDIS;
    }

    @Override
    public String identity() {
        return "redis";
    }

    @Override
    public boolean isEntrance() {
        return false;
    }
}

package com.rblue;

import redis.clients.jedis.Jedis;

public class Test {

    @org.junit.Test
    public void testRedis(){
        //获取连接对象
        Jedis jedis=new Jedis("localhost",6379);
        //执行操作
        jedis.set("city","beijing");
        //关闭连接
        jedis.close();
    }
}

package com.rblue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class testDataRedis {
//    自动注解获取redis模板
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testString(){

        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("city","beijing",100l, TimeUnit.SECONDS);
        System.out.println(valueOperations.get("city"));
    }

    @Test
    public void testHash(){

        HashOperations hashOperations=redisTemplate.opsForHash();

        hashOperations.put("002","name","xiaoming");
        hashOperations.put("002","age","22");
        hashOperations.put("002","add","广州");
        String age= (String) hashOperations.get("002","age");
        System.out.println(age);
        //获得hash中的所有hashkey
      Set keys=  hashOperations.keys("002");
        for (Object key : keys) {
            System.out.println(key);
        }

    }

    @Test
    public void testList(){
        ListOperations listOperations=redisTemplate.opsForList();
        listOperations.leftPushAll("mylist","a","b","c","d","e");
        List<String> mylist = listOperations.range("mylist", 0, -1);
        for (String o : mylist) {
            listOperations.rightPop("mylist");
        }
    }
    @Test
    public void testSet(){
        SetOperations setOperations=redisTemplate.opsForSet();
        setOperations.add("myset","a","b","c","c","a");
        //取值
        Set<String> sets=setOperations.members("myset");
        for (String set : sets) {
            System.out.println(set);
        }
        //删除成员
        setOperations.remove("myset","b");
        sets=setOperations.members("myset");
        for (String set : sets) {
            System.out.println(set);
        }
    }

    @Test
    public void testSortSet(){
        ZSetOperations zsetOperations=redisTemplate.opsForZSet();

        zsetOperations.add("myZset","a",100.5);
        zsetOperations.add("myZset","b",120.5);
        zsetOperations.add("myZset","c",200.5);
        zsetOperations.add("myZset","a",101.5);


        //取值
        Set<String> sets=zsetOperations.range("myZset",0,-1);
        for (String set : sets) {
            System.out.println(set);
        }
        //增加
        zsetOperations.incrementScore("myZset","a",200);

        sets=zsetOperations.range("myZset",0,-1);
        for (String set : sets) {
            System.out.println(set);
        }
    }

    @Test
    public void testCommon(){
        Set<String > keys=redisTemplate.keys("*");
        for (String key : keys) {
            System.out.println(key);
        }
        //判断某个key是否存在
        System.out.println(redisTemplate.hasKey("myset"));
        //删除指定key
        redisTemplate.delete("myZset");

        DataType dataType=redisTemplate.type("mylist");
        System.out.println(dataType);
    }
}

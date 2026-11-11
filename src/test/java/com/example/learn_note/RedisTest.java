package com.example.learn_note;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

@SpringBootTest//如果在测试类上添加了该注解，那么将来单元测试方法执行之前，会先初始化spring容器，也就是说加载了ioc容器
//调用Api完成存取操作
public class RedisTest {

//        在引入redis起步依赖后ioc容器就自动创建了stringRedisTemplate对象
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Test
    public void testset(){
        //        往redis存储一个键值对 调用StringRedisTemplate对象
//        opsForValue 对实体的操作集合
        ValueOperations<String, String> Operations = stringRedisTemplate.opsForValue();
        Operations.set("username","zhangsan");
//        设置过期时间
        Operations.set("id","1",15, TimeUnit.SECONDS);


    }
    @Test
    public void testget(){
//        从redis获取键值对
        ValueOperations<String, String> Operations = stringRedisTemplate.opsForValue();
        System.out.println(Operations.get("username"));

    }
}

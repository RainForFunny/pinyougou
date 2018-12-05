package cn.itcast.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-redis.xml")
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 测试字符串
     */
    @Test
    public void testString(){
        redisTemplate.boundValueOps("string_key").set("yahoooooooooooo");
        Object obj = redisTemplate.boundValueOps("string_key").get();
        System.out.println(obj);
    }


    /**
     * 测试list
     */
    @Test
    public void testList(){
        redisTemplate.boundListOps("list_key").rightPush("a");
        redisTemplate.boundListOps("list_key").rightPush("b");
        redisTemplate.boundListOps("list_key").leftPush("c");
        //起始索引号，结束索引号（如果为-1表示最后一个索引号
        Object obj = redisTemplate.boundListOps("list_key").range(0, -1);
        System.out.println(obj);
    }

    /**
     * 测试hash
     */
    @Test
    public void testHash(){
        redisTemplate.boundHashOps("hash_key").put("a",21);
        redisTemplate.boundHashOps("hash_key").put("b",23);
        redisTemplate.boundHashOps("hash_key").put("c",25);

        Object hash_key = redisTemplate.boundHashOps("hash_key").values();

        System.out.println(hash_key);
    }

    /**
     * 测试sorted set
     */
    @Test
    public void testZset(){
        redisTemplate.boundZSetOps("Zset_key").add("a",10);
        redisTemplate.boundZSetOps("Zset_key").add("b",20);
        redisTemplate.boundZSetOps("Zset_key").add("c",30);

        Object obj = redisTemplate.boundZSetOps("Zset_key").range(0,-1);
        System.out.println(obj);
    }

    /**
     * 测试set
     */
    @Test
    public void testSet(){
        redisTemplate.boundSetOps("set_key").add("a","b","c");

        Object obj = redisTemplate.boundSetOps("set_key").members();
        System.out.println(obj);
    }

}

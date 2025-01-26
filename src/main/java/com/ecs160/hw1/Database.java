package com.ecs160.hw1;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

public class Database {
    private Jedis jedis;

    Database(){
        this.jedis = new Jedis("localhost", 6379);
    }

    public void setValue(String key, String object) {
        jedis.set(key, object.toString());
    }

    public String getValue(String key) {
        return jedis.get(key);
    }
}

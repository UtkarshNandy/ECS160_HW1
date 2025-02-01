package com.ecs160.hw1;
import redis.clients.jedis.Jedis;
import java.util.*;

public class Database {
    private Jedis jedis;
    private static final String POST_KEY = "post:";
    private static final String REPLIES_KEY = "replies:";
    private static final String PARENT_KEY = "parent:";

    public Database() {
        this.jedis = new Jedis("localhost", 6379);
    }

    public void storeThread(BlueskyThread thread, String threadId) {
        // Store main post
        Map<String, String> postData = new HashMap<>();
        postData.put("text", thread.getText());
        postData.put("createdAt", thread.getCreatedAt());
        jedis.hmset(POST_KEY + threadId, postData);

        // Store replies
        if (thread.getReplies() != null) {
            for (int i = 0; i < thread.getReplies().size(); i++) {
                String replyId = threadId + ":reply:" + i;
                BlueskyThread reply = thread.getReplies().get(i);

                Map<String, String> replyData = new HashMap<>();
                replyData.put("text", reply.getText());
                replyData.put("createdAt", reply.getCreatedAt());

                jedis.hmset(POST_KEY + replyId, replyData);
                jedis.set(PARENT_KEY + replyId, threadId);
                jedis.sadd(REPLIES_KEY + threadId, replyId);
            }
        }
    }

    public BlueskyThread reconstructThread(String threadId) {
        Map<String, String> postData = jedis.hgetAll(POST_KEY + threadId);
        if (postData.isEmpty()) {
            return null;
        }

        List<BlueskyThread> replies = new ArrayList<>();
        Set<String> replyIds = jedis.smembers(REPLIES_KEY + threadId);

        for (String replyId : replyIds) {
            Map<String, String> replyData = jedis.hgetAll(POST_KEY + replyId);
            if (!replyData.isEmpty()) {
                replies.add(new BlueskyThread(
                        replyData.get("text"),
                        replyData.get("createdAt"),
                        null,
                        0  // Default replyCount for replies
                ));
            }
        }

        return new BlueskyThread(
                postData.get("text"),
                postData.get("createdAt"),
                replies.isEmpty() ? null : replies,
                0  // Default replyCount for main post
        );
    }

    public void close() {
        if (jedis != null) {
            jedis.close();
        }
    }
}
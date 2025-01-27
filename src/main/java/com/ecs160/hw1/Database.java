package com.ecs160.hw1;
import redis.clients.jedis.Jedis;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import com.google.gson.*;
import java.util.HashMap;
import java.util.Map;

public class Database {
    private Jedis jedis;
    private static final String POST_KEY = "post:";
    private static final String REPLIES_KEY = "replies:";
    private static final String PARENT_KEY = "parent:";

    public Database() {
        this.jedis = new Jedis("localhost", 6379);
    }

    public void storePost(String postId, String postData, List<String> replyIds) {
        jedis.hset(POST_KEY + postId, "data", postData);

        if (replyIds != null && !replyIds.isEmpty()) {
            for (String replyId : replyIds) {
                jedis.sadd(REPLIES_KEY + postId, replyId);
            }
        }
    }

    public void storeThread(BlueskyThread thread, String threadId) {
        // Store the main post
        Gson gson = new Gson();
        jedis.hset(POST_KEY + threadId, "data", gson.toJson(thread.getPost()));

        // Store replies with parent references
        if (thread.getReplies() != null) {
            for (int i = 0; i < thread.getReplies().size(); i++) {
                String replyId = threadId + ":reply:" + i;
                BlueskyThread reply = thread.getReplies().get(i);

                // Store reply data
                jedis.hset(POST_KEY + replyId, "data", gson.toJson(reply.getPost()));

                // Store parent reference
                jedis.set(PARENT_KEY + replyId, threadId);

                // Add to parent's reply set
                jedis.sadd(REPLIES_KEY + threadId, replyId);
            }
        }
    }

    public BlueskyThread reconstructThread(String threadId) {
        // Get the main post
        String postData = jedis.hget(POST_KEY + threadId, "data");
        if (postData == null) {
            return null;
        }

        Gson gson = new Gson();
        BlueskyThread thread = new BlueskyThread();
        thread.setPost(gson.fromJson(postData, Post.class));

        // Get all replies
        Set<String> replyIds = jedis.smembers(REPLIES_KEY + threadId);
        if (!replyIds.isEmpty()) {
            List<BlueskyThread> replies = new ArrayList<>();

            for (String replyId : replyIds) {
                String replyData = jedis.hget(POST_KEY + replyId, "data");
                if (replyData != null) {
                    BlueskyThread replyThread = new BlueskyThread();
                    replyThread.setPost(gson.fromJson(replyData, Post.class));
                    replies.add(replyThread);
                }
            }

            thread.setReplies(replies);
        }

        return thread;
    }

    public void close() {
        if (jedis != null) {
            jedis.close();
        }
    }
}
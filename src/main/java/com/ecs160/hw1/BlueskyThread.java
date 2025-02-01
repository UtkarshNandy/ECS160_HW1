package com.ecs160.hw1;
import java.util.List;

public class BlueskyThread {
    private final String text;
    private final String createdAt;
    private final List<BlueskyThread> replies;
    private final int replyCount;

    public BlueskyThread(String text, String createdAt, List<BlueskyThread> replies, int replyCount) {
        this.text = text;
        this.createdAt = createdAt;
        this.replies = replies;
        this.replyCount = replyCount;
    }

    public String getText() {
        return text;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public List<BlueskyThread> getReplies() {
        return replies;
    }

    public int getReplyCount() {
        return replyCount;
    }
}

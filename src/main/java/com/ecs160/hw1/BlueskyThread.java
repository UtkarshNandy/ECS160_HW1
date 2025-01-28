package com.ecs160.hw1;

import java.util.List;

public class BlueskyThread {
    private Post post;
    private List<BlueskyThread> replies;


    Post getPost() {
        return this.post;
    }

    List<BlueskyThread> getReplies() {
        return this.replies;
    }
    public void setPost(Post post) {
        this.post = post;
    }

    public void setReplies(List<BlueskyThread> replies) {
        this.replies = replies;
    }

}

class Record {
    private String text;
    private String createdAt;

    // Add setters
    public void setText(String text) {
        this.text = text;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    String getText() {
        return this.text;
    }

    String getCreatedAt() {
        return this.createdAt;
    }
}

class Post {
    private Record record;
    private int replyCount;

    // Add setter
    public void setRecord(Record record) {
        this.record = record;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    Record getRecord() {
        return this.record;
    }

    int getReplyCount() {
        return this.replyCount;
    }
}
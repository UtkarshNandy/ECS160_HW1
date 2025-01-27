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

class Post {
    private Record record;
    private int replyCount;

    Record getRecord() {
        return this.record;
    }

    int getReplyCount() {
        return this.replyCount;
    }

}

class Record {
    private String text;
    private String createdAt;  // Add timestamp field

    String getText() {
        return this.text;
    }

    String getCreatedAt() {
        return this.createdAt;
    }
}

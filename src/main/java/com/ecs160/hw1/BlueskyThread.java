package com.ecs160.hw1;
import java.util.List;

public class BlueskyThread {
    private Post post;
    private List<BlueskyThread>replies;
    private int replyCount;

    Post getPost(){
        return this.post;
    }

    List<BlueskyThread> getReplies(){
        return this.replies;
    }

    int getReplyCount(){
        return this.replyCount;
    }

}

class Post{
    private Record record;

    Record getRecord(){
        return this.record;
    }

}

class Record{
    private String text;

    String getText(){
        return this.text;
    }
}

package com.ecs160.hw1;

import java.time.Instant;
import java.util.List;

public class BasicAnalyzer implements Analyzer {
    private final List<BlueskyThread> threads;

    public BasicAnalyzer(List<BlueskyThread> threads) {
        this.threads = threads;
    }

    @Override
    public int getTotalPosts() {
        return threads.size();
    }

    @Override
    public double getAverageReplies() {
        if (threads.isEmpty()) return 0.0;

        double totalReplies = 0;
        for (BlueskyThread thread : threads) {
            if (thread.getReplies() != null) {
                totalReplies += thread.getReplies().size();
            }
        }
        return totalReplies / threads.size();
    }

    @Override
    public String getAverageInterval() {
        long totalSeconds = 0;
        int postsWithReplies = 0;

        for (BlueskyThread thread : threads) {
            if (thread.getReplies() != null && !thread.getReplies().isEmpty()) {
                long threadStart = parseTimestamp(thread.getCreatedAt());
                long lastReplyTime = threadStart;

                for (BlueskyThread reply : thread.getReplies()) {
                    long replyTime = parseTimestamp(reply.getCreatedAt());
                    totalSeconds += (replyTime - lastReplyTime) / 1000;
                    lastReplyTime = replyTime;
                }
                postsWithReplies++;
            }
        }

        if (postsWithReplies == 0) return "00:00:00";

        long avgSeconds = totalSeconds / postsWithReplies;
        return String.format("%02d:%02d:%02d",
                avgSeconds / 3600, (avgSeconds % 3600) / 60, avgSeconds % 60);
    }

    private long parseTimestamp(String timestamp) {
        return Instant.parse(timestamp).toEpochMilli();
    }
}
package com.ecs160.hw1;
import java.util.List;

public class StatisticsCalc {
    private List<BlueskyThread> threads;
    private boolean weighted;

    public StatisticsCalc(List<BlueskyThread> threads, boolean weighted) {
        this.threads = threads;
        this.weighted = weighted;
    }

    // calculates total posts, optionally weighted by word count
    public int getTotalPosts() {
        if (!weighted) {
            return threads.size();
        }

        // weighted calculation based on word count
        int maxWords = findMaxWordCount();
        double weightedTotal = 0;
        for (BlueskyThread thread : threads) {
            int wordCount = thread.getPost().getRecord().getText().split("\\s+").length;
            weightedTotal += 1 + ((double) wordCount / maxWords);
        }
        return (int) Math.round(weightedTotal);
    }

    // calculates average replies per thread, optionally weighted
    public double getAverageReplies() {
        if (!weighted) {
            int totalReplies = 0;
            for (BlueskyThread thread : threads) {
                List<BlueskyThread> replies = thread.getReplies();
                if (replies != null) {
                    totalReplies += replies.size();
                }
            }
            return threads.isEmpty() ? 0 : (double) totalReplies / threads.size();
        }

        // weighted calculation for replies
        int maxWords = findMaxWordCount();
        double weightedReplies = 0;
        for (BlueskyThread thread : threads) {
            List<BlueskyThread> replies = thread.getReplies();
            if (replies != null) {
                for (BlueskyThread reply : replies) {
                    int wordCount = reply.getPost().getRecord().getText().split("\\s+").length;
                    weightedReplies += 1 + ((double) wordCount / maxWords);
                }
            }
        }
        return threads.isEmpty() ? 0 : weightedReplies / threads.size();
    }

    // finds the maximum word count across all posts and replies
    private int findMaxWordCount() {
        int maxWords = 0;
        for (BlueskyThread thread : threads) {
            maxWords = Math.max(maxWords,
                    thread.getPost().getRecord().getText().split("\\s+").length);
            List<BlueskyThread> replies = thread.getReplies();
            if (replies != null) {
                for (BlueskyThread reply : replies) {
                    maxWords = Math.max(maxWords,
                            reply.getPost().getRecord().getText().split("\\s+").length);
                }
            }
        }
        return maxWords;
    }

    // calculates average time between replies in hh:mm:ss format
    public String getAverageInterval() {
        long totalSeconds = 0;
        int postsWithReplies = 0;

        for (BlueskyThread thread : threads) {
            List<BlueskyThread> replies = thread.getReplies();
            if (replies != null && !replies.isEmpty()) {
                long threadStart = parseTimestamp(thread.getPost().getRecord().getCreatedAt());
                long lastReplyTime = threadStart;

                for (BlueskyThread reply : replies) {
                    long replyTime = parseTimestamp(reply.getPost().getRecord().getCreatedAt());
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

    // converts iso timestamp to milliseconds
    private long parseTimestamp(String timestamp) {
        return java.time.Instant.parse(timestamp).toEpochMilli();
    }
}
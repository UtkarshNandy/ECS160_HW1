package com.ecs160.hw1;

import java.time.Instant;
import java.util.List;

public class WeightedAnalyzer implements Analyzer {
    private final List<BlueskyThread> threads;
    private final int maxWords;

    public WeightedAnalyzer(List<BlueskyThread> threads) {
        this.threads = threads;
        this.maxWords = findMaxWordCount();
    }

    private int findMaxWordCount() {
        int maxWords = 0;
        for (BlueskyThread thread : threads) {
            maxWords = Math.max(maxWords, thread.getText().split("\\s+").length);
            if (thread.getReplies() != null) {
                for (BlueskyThread reply : thread.getReplies()) {
                    maxWords = Math.max(maxWords, reply.getText().split("\\s+").length);
                }
            }
        }
        return maxWords;
    }

    private double calculateWeight(String text) {
        if (maxWords == 0) return 1.0;
        int wordCount = text.split("\\s+").length;
        return 1.0 + ((double) wordCount / maxWords);
    }

    @Override
    public int getTotalPosts() {
        double weightedTotal = 0;
        for (BlueskyThread thread : threads) {
            weightedTotal += calculateWeight(thread.getText());
        }
        return (int) Math.round(weightedTotal);
    }

    @Override
    public double getAverageReplies() {
        if (threads.isEmpty()) return 0.0;

        double weightedReplies = 0;
        for (BlueskyThread thread : threads) {
            if (thread.getReplies() != null) {
                for (BlueskyThread reply : thread.getReplies()) {
                    weightedReplies += calculateWeight(reply.getText());
                }
            }
        }
        return weightedReplies / threads.size();
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
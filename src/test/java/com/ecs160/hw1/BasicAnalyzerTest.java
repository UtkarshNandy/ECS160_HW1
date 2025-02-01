package com.ecs160.hw1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class BasicAnalyzerTest {
    private List<BlueskyThread> threads;
    private Analyzer analyzer;

    @BeforeEach
    void setUp() {
        threads = new ArrayList<>();

        // Test thread 1: Standard post with multiple replies
        threads.add(new BlueskyThread(
                "Main post 1",
                "2024-01-01T10:00:00.000Z",
                List.of(
                        new BlueskyThread("Reply 1", "2024-01-01T10:30:00.000Z", null, 0),
                        new BlueskyThread("Reply 2", "2024-01-01T11:00:00.000Z", null, 0)
                ),
                2
        ));

        // Test thread 2: Single post with one reply
        threads.add(new BlueskyThread(
                "Main post 2",
                "2024-01-01T12:00:00.000Z",
                List.of(new BlueskyThread("Reply to post 2", "2024-01-01T12:30:00.000Z", null, 0)),
                1
        ));

        analyzer = new BasicAnalyzer(threads);
    }

    @Test
    void testTotalPosts() {
        assertEquals(2, analyzer.getTotalPosts(), "Should count only main posts");
    }

    @Test
    void testAverageReplies() {
        // (2 replies + 1 reply) / 2 posts = 1.5 average replies
        assertEquals(1.5, analyzer.getAverageReplies(), 0.01);
    }

    @Test
    void testAverageInterval() {
        // First thread: 30 min + 30 min = 60 min avg
        // Second thread: 30 min
        // Overall average = 45 min = 00:45:00
        assertEquals("00:45:00", analyzer.getAverageInterval());
    }

    @Test
    void testEmptyThreadList() {
        analyzer = new BasicAnalyzer(new ArrayList<>());
        assertEquals(0, analyzer.getTotalPosts());
        assertEquals(0.0, analyzer.getAverageReplies());
        assertEquals("00:00:00", analyzer.getAverageInterval());
    }

    @Test
    void testThreadWithNoReplies() {
        List<BlueskyThread> singleThread = List.of(
                new BlueskyThread("Standalone post", "2024-01-01T10:00:00.000Z", null, 0)
        );
        analyzer = new BasicAnalyzer(singleThread);

        assertEquals(1, analyzer.getTotalPosts());
        assertEquals(0.0, analyzer.getAverageReplies());
        assertEquals("00:00:00", analyzer.getAverageInterval());
    }
}
package com.ecs160.hw1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class WeightedAnalyzerTest {
    private List<BlueskyThread> threads;
    private Analyzer analyzer;

    @BeforeEach
    void setUp() {
        threads = new ArrayList<>();

        // Test thread 1: Short post with short replies
        threads.add(new BlueskyThread(
                "Short post",
                "2024-01-01T10:00:00.000Z",
                List.of(
                        new BlueskyThread("Short reply 1", "2024-01-01T10:30:00.000Z", null, 0),
                        new BlueskyThread("Short reply 2", "2024-01-01T11:00:00.000Z", null, 0)
                ),
                2
        ));

        // Test thread 2: Long post with long reply
        threads.add(new BlueskyThread(
                "This is a much longer post that should have significantly more weight in the calculations because it contains many more words than the other posts",
                "2024-01-01T12:00:00.000Z",
                List.of(new BlueskyThread(
                        "This is also a very long reply that should have more weight in the calculations due to its length",
                        "2024-01-01T12:30:00.000Z",
                        null,
                        0
                )),
                1
        ));

        analyzer = new WeightedAnalyzer(threads);
    }

    @Test
    void testWeightedTotalPosts() {
        int weightedTotal = analyzer.getTotalPosts();
        assertTrue(weightedTotal > 2, "Weighted total should be greater than raw count due to long posts");
    }

    @Test
    void testWeightedAverageReplies() {
        double weightedAvg = analyzer.getAverageReplies();
        assertTrue(weightedAvg > 1.5, "Weighted average should be higher than unweighted due to long replies");
    }

    @Test
    void testVeryLongPost() {
        String longText = "This ".repeat(100) + "is a very long post";
        List<BlueskyThread> longThreads = List.of(
                new BlueskyThread(longText, "2024-01-01T10:00:00.000Z", null, 0)
        );

        Analyzer longAnalyzer = new WeightedAnalyzer(longThreads);
        assertTrue(longAnalyzer.getTotalPosts() > 1.5,
                "Very long post should have weight significantly greater than 1");
    }

    @Test
    void testEmptyThreadList() {
        analyzer = new WeightedAnalyzer(new ArrayList<>());
        assertEquals(0, analyzer.getTotalPosts());
        assertEquals(0.0, analyzer.getAverageReplies());
        assertEquals("00:00:00", analyzer.getAverageInterval());
    }

    @Test
    void testIntervalCalculationMatchesBasic() {
        Analyzer basicAnalyzer = new BasicAnalyzer(threads);
        assertEquals(basicAnalyzer.getAverageInterval(), analyzer.getAverageInterval(),
                "Interval calculation should be same for weighted and unweighted");
    }
}
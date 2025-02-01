package com.ecs160.hw1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SocialMediaAnalyzerDriverTest {
    private List<BlueskyThread> threads;
    private Database database;
    private Analyzer basicAnalyzer;
    private Analyzer weightedAnalyzer;

    @BeforeEach
    void setUp() {
        threads = new ArrayList<>();
        database = new Database();

        // Create test data
        BlueskyThread thread1 = createTestThread(
                "Short post",
                "2024-01-01T10:00:00.000Z",
                new String[]{"First reply at 10:30", "Second reply at 11:00"},
                new String[]{"2024-01-01T10:30:00.000Z", "2024-01-01T11:00:00.000Z"}
        );
        threads.add(thread1);

        BlueskyThread thread2 = createTestThread(
                "This is a much longer post that should have more weight in the weighted calculations because it contains more words",
                "2024-01-01T12:00:00.000Z",
                new String[]{"Short reply"},
                new String[]{"2024-01-01T12:30:00.000Z"}
        );
        threads.add(thread2);

        basicAnalyzer = new BasicAnalyzer(threads);
        weightedAnalyzer = new WeightedAnalyzer(threads);
    }

    @AfterEach
    void tearDown() {
        database.close();
    }

    private BlueskyThread createTestThread(String postText, String postTimestamp,
                                           String[] replyTexts, String[] replyTimestamps) {
        List<BlueskyThread> replies = new ArrayList<>();
        if (replyTexts.length > 0) {
            for (int i = 0; i < replyTexts.length; i++) {
                replies.add(new BlueskyThread(
                        replyTexts[i],
                        replyTimestamps[i],
                        null,
                        0
                ));
            }
        }

        return new BlueskyThread(
                postText,
                postTimestamp,
                replies.isEmpty() ? null : replies,
                replies.size()
        );
    }

    @Test
    void testUnweightedTotalPosts() {
        assertEquals(2, basicAnalyzer.getTotalPosts());
    }

    @Test
    void testWeightedTotalPosts() {
        int weightedTotal = weightedAnalyzer.getTotalPosts();
        assertTrue(weightedTotal > 2,
                "Weighted total should be greater than unweighted due to longer posts");
    }

    @Test
    void testUnweightedAverageReplies() {
        assertEquals(1.5, basicAnalyzer.getAverageReplies(), 0.01);
    }

    @Test
    void testWeightedAverageReplies() {
        double weightedAvg = weightedAnalyzer.getAverageReplies();
        assertTrue(weightedAvg > 0, "Weighted average should be positive");
        assertTrue(weightedAvg > basicAnalyzer.getAverageReplies(),
                "Weighted average should be greater than unweighted due to long replies");
    }

    @Test
    void testEmptyThreadList() {
        List<BlueskyThread> emptyList = new ArrayList<>();
        Analyzer emptyAnalyzer = new BasicAnalyzer(emptyList);
        assertEquals(0, emptyAnalyzer.getTotalPosts());
        assertEquals(0.0, emptyAnalyzer.getAverageReplies());
        assertEquals("00:00:00", emptyAnalyzer.getAverageInterval());
    }

    @Test
    void testDatabaseStorage() {
        String threadId = "test-thread";
        BlueskyThread thread = createTestThread(
                "Database test post",
                "2024-01-01T10:00:00.000Z",
                new String[]{"Database test reply"},
                new String[]{"2024-01-01T10:30:00.000Z"}
        );

        database.storeThread(thread, threadId);
        BlueskyThread reconstructed = database.reconstructThread(threadId);

        assertNotNull(reconstructed);
        assertEquals("Database test post", reconstructed.getText());
        assertNotNull(reconstructed.getReplies());
        assertEquals(1, reconstructed.getReplies().size());
        assertEquals("Database test reply", reconstructed.getReplies().get(0).getText());
    }

    @Test
    void testIntervalCalculation() {
        assertEquals("00:45:00", basicAnalyzer.getAverageInterval());
        assertEquals("00:45:00", weightedAnalyzer.getAverageInterval(),
                "Interval should be same for weighted and unweighted");
    }

    @Test
    void testThreadWithNoReplies() {
        BlueskyThread thread = createTestThread(
                "No replies post",
                "2024-01-01T10:00:00.000Z",
                new String[]{},
                new String[]{}
        );

        List<BlueskyThread> singleThread = new ArrayList<>();
        singleThread.add(thread);
        Analyzer analyzer = new BasicAnalyzer(singleThread);

        assertEquals(1, analyzer.getTotalPosts());
        assertEquals(0.0, analyzer.getAverageReplies());
        assertEquals("00:00:00", analyzer.getAverageInterval());
    }

    @Test
    void testVeryLongPost() {
        String longText = "This ".repeat(100) + "is a very long post";
        BlueskyThread thread = createTestThread(
                longText,
                "2024-01-01T10:00:00.000Z",
                new String[]{"Short reply"},
                new String[]{"2024-01-01T10:30:00.000Z"}
        );

        List<BlueskyThread> testThreads = new ArrayList<>();
        testThreads.add(thread);
        Analyzer analyzer = new WeightedAnalyzer(testThreads);

        assertTrue(analyzer.getTotalPosts() > 1.0,
                "Very long post should have weight > 1");
    }

    @Test
    void testCommandLineArguments() {
        String[] args = {"weighted=true", "file=./input.json"};
        Map<String, String> parsed = SocialMediaAnalyzerDriver.parseArguments(args);
        assertEquals("true", parsed.get("weighted"));
        assertEquals("./input.json", parsed.get("file"));
    }

    @Test
    void testFilePathHandling() {
        assertDoesNotThrow(() -> {
            InputStream defaultInput = SocialMediaAnalyzerDriver.class
                    .getClassLoader().getResourceAsStream("input.json");
            assertNotNull(defaultInput);
        });

        assertThrows(FileNotFoundException.class, () -> {
            new FileInputStream("nonexistent.json");
        });
    }
}
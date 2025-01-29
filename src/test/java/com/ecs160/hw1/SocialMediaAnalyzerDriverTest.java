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
import com.google.gson.Gson;


public class SocialMediaAnalyzerDriverTest {
    private List<BlueskyThread> threads;
    private StatisticsCalc statsCalc;
    private Database database;

    @BeforeEach
    void setUp() {
        threads = new ArrayList<>();
        database = new Database();

        // Create test data
        BlueskyThread thread1 = createTestThread(
                "Short post",
                "2024-01-01T10:00:00.000Z",
                new String[]{
                        "First reply at 10:30",
                        "Second reply at 11:00"
                },
                new String[]{
                        "2024-01-01T10:30:00.000Z",
                        "2024-01-01T11:00:00.000Z"
                }
        );
        threads.add(thread1);

        BlueskyThread thread2 = createTestThread(
                "This is a much longer post that should have more weight in the weighted calculations because it contains more words",
                "2024-01-01T12:00:00.000Z",
                new String[]{"Short reply"},
                new String[]{"2024-01-01T12:30:00.000Z"}
        );
        threads.add(thread2);
    }

    @AfterEach
    void tearDown() {
        database.close();
    }

    private BlueskyThread createTestThread(String postText, String postTimestamp,
                                           String[] replyTexts, String[] replyTimestamps) {
        BlueskyThread thread = new BlueskyThread();
        Post post = new Post();
        Record record = new Record();

        record.setText(postText);
        record.setCreatedAt(postTimestamp);
        post.setRecord(record);
        thread.setPost(post);

        if (replyTexts.length > 0) {
            List<BlueskyThread> replies = new ArrayList<>();
            for (int i = 0; i < replyTexts.length; i++) {
                BlueskyThread reply = new BlueskyThread();
                Post replyPost = new Post();
                Record replyRecord = new Record();

                replyRecord.setText(replyTexts[i]);
                replyRecord.setCreatedAt(replyTimestamps[i]);
                replyPost.setRecord(replyRecord);
                reply.setPost(replyPost);
                replies.add(reply);
            }
            thread.setReplies(replies);
        }

        return thread;
    }

    @Test
    void testUnweightedTotalPosts() {
        statsCalc = new StatisticsCalc(threads, false);
        assertEquals(2, statsCalc.getTotalPosts()); // Only counts main posts in unweighted mode
    }

    @Test
    void testWeightedTotalPosts() {
        statsCalc = new StatisticsCalc(threads, true);
        int weightedTotal = statsCalc.getTotalPosts();
        assertTrue(weightedTotal > 2,
                "Weighted total should be greater than unweighted due to longer posts");
    }

    @Test
    void testUnweightedAverageReplies() {
        statsCalc = new StatisticsCalc(threads, false);
        // Thread 1 has 2 replies, Thread 2 has 1 reply
        // Average = (2 + 1) / 2 = 1.5
        assertEquals(1.5, statsCalc.getAverageReplies(), 0.01);
    }

    @Test
    void testWeightedAverageReplies() {
        statsCalc = new StatisticsCalc(threads, true);
        double weightedAvg = statsCalc.getAverageReplies();
        assertTrue(weightedAvg > 0, "Weighted average should be positive");
    }

    @Test
    void testEmptyThreadList() {
        statsCalc = new StatisticsCalc(new ArrayList<>(), false);
        assertEquals(0, statsCalc.getTotalPosts());
        assertEquals(0.0, statsCalc.getAverageReplies());
        assertEquals("00:00:00", statsCalc.getAverageInterval());
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

        // Test storeThread
        database.storeThread(thread, threadId);

        // Test reconstruction
        BlueskyThread reconstructed = database.reconstructThread(threadId);
        assertNotNull(reconstructed);
        assertEquals("Database test post",
                reconstructed.getPost().getRecord().getText());
        assertNotNull(reconstructed.getReplies());
        assertEquals(1, reconstructed.getReplies().size());
        assertEquals("Database test reply",
                reconstructed.getReplies().getFirst().getPost().getRecord().getText());
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

        statsCalc = new StatisticsCalc(singleThread, false);
        assertEquals(1, statsCalc.getTotalPosts());
        assertEquals(0.0, statsCalc.getAverageReplies());
        assertEquals("00:00:00", statsCalc.getAverageInterval());
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

        statsCalc = new StatisticsCalc(testThreads, true);
        assertTrue(statsCalc.getTotalPosts() > 1.0,
                "Very long post should have weight > 1");
    }

    @Test
    void testDatabaseStorePost() {
        String postId = "test-post";
        Post post = new Post();
        Record record = new Record();
        record.setText("Test post");
        post.setRecord(record);

        Gson gson = new Gson();
        String postData = gson.toJson(post);
        List<String> replyIds = List.of("reply1", "reply2");

        database.storePost(postId, postData, replyIds);
        BlueskyThread reconstructed = database.reconstructThread(postId);
        assertNotNull(reconstructed);
        assertEquals("Test post",
                reconstructed.getPost().getRecord().getText());
    }
    @Test
    void testReplyCountVsActualReplies() {
        // Create a thread where replyCount differs from actual replies
        BlueskyThread thread = createTestThread(
                "Main post",
                "2024-01-01T10:00:00.000Z",
                new String[]{"Reply 1", "Reply 2"},
                new String[]{
                        "2024-01-01T10:30:00.000Z",
                        "2024-01-01T11:00:00.000Z"
                }
        );
        // Set replyCount different from actual
        thread.getPost().setReplyCount(5);

        List<BlueskyThread> testThreads = new ArrayList<>();
        testThreads.add(thread);

        statsCalc = new StatisticsCalc(testThreads, false);
        assertEquals(2.0, statsCalc.getAverageReplies()); // Should use actual reply count (2) not replyCount (5)
    }

    @Test
    void testLargeNumberOfPosts() {
        List<BlueskyThread> largeThreadList = new ArrayList<>();
        for(int i = 0; i < 10000; i++) {
            largeThreadList.add(createTestThread(
                    "Post " + i,
                    "2024-01-01T10:00:00.000Z",
                    new String[]{"Reply to " + i},
                    new String[]{"2024-01-01T11:00:00.000Z"}
            ));
        }
        statsCalc = new StatisticsCalc(largeThreadList, false);
        assertEquals(10000, statsCalc.getTotalPosts());
    }

    @Test
    void testCommandLineArguments() {
        String[] args = {"weighted=true", "file=./input.json"};
        // Create a method to parse arguments and test it
        Map<String, String> parsed = SocialMediaAnalyzerDriver.parseArguments(args);
        assertEquals("true", parsed.get("weighted"));
        assertEquals("./input.json", parsed.get("file"));
    }

    @Test
    void testFilePathHandling() {
        // Test default path
        assertDoesNotThrow(() -> {
            InputStream defaultInput = SocialMediaAnalyzerDriver.class
                    .getClassLoader().getResourceAsStream("input.json");
            assertNotNull(defaultInput);
        });

        // Test custom path
        assertThrows(FileNotFoundException.class, () -> {
            new FileInputStream("nonexistent.json");
        });
    }
}


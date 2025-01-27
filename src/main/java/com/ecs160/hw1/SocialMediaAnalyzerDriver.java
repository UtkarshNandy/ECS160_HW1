package com.ecs160.hw1;

import com.google.gson.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class SocialMediaAnalyzerDriver {
    public static void main(String[] args) {
        // Parse command line arguments
        String inputFile = "input.json";
        boolean weighted = false;

        for (String arg : args) {
            if (arg.startsWith("file=")) {
                inputFile = arg.substring(5);
            } else if (arg.startsWith("weighted=")) {
                weighted = Boolean.parseBoolean(arg.substring(9));
            }
        }

        Database database = new Database();
        List<BlueskyThread> threads = new ArrayList<>();

        try {
            // Read JSON file
            JsonElement element;
            if (inputFile.equals("input.json")) {
                element = JsonParser.parseReader(new InputStreamReader(
                        SocialMediaAnalyzerDriver.class.getClassLoader().getResourceAsStream(inputFile)));
            } else {
                element = JsonParser.parseReader(new InputStreamReader(new FileInputStream(inputFile)));
            }

            if (element.isJsonObject()) {
                JsonObject jsonObject = element.getAsJsonObject();
                JsonArray feedArray = jsonObject.get("feed").getAsJsonArray();
                int postId = 0;

                for (JsonElement feedObject : feedArray) {
                    if (feedObject.getAsJsonObject().has("thread")) {
                        JsonObject threadObject = feedObject.getAsJsonObject().getAsJsonObject("thread");
                        Gson gson = new Gson();
                        BlueskyThread thread = gson.fromJson(threadObject, BlueskyThread.class);
                        threads.add(thread);

                        // Store in database
                        List<String> replyIds = new ArrayList<>();
                        if (thread.getReplies() != null) {
                            for (int i = 0; i < thread.getReplies().size(); i++) {
                                replyIds.add(postId + "_" + i);
                            }
                        }
                        database.storePost(String.valueOf(postId), gson.toJson(thread.getPost()), replyIds);
                        postId++;
                    }
                }

                // Calculate and display statistics
                StatisticsCalc stats = new StatisticsCalc(threads, weighted);
                System.out.println("Analysis Results:");
                System.out.println("Total Posts: " + (int)stats.getTotalPosts());
                System.out.printf("Average Replies per Post: %.2f%n", stats.getAverageReplies());
                System.out.println("Average Interval between Comments: " + stats.getAverageInterval());
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: Could not find input file: " + inputFile);
            System.exit(1);
        } finally {
            database.close();
        }
    }
}
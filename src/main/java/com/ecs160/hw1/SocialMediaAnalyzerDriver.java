package com.ecs160.hw1;

import com.google.gson.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class SocialMediaAnalyzerDriver {
    public static Map<String, String> parseArguments(String[] args) {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("file", "input.json");
        arguments.put("weighted", "false");

        for (String arg : args) {
            if (arg.startsWith("file=")) {
                arguments.put("file", arg.substring(5));
            } else if (arg.startsWith("weighted=")) {
                arguments.put("weighted", arg.substring(9));
            }
        }
        return arguments;
    }

    private static BlueskyThread parseThread(JsonObject threadObject) {
        JsonObject postObject = threadObject.getAsJsonObject("post");
        JsonObject recordObject = postObject.getAsJsonObject("record");

        List<BlueskyThread> replies = new ArrayList<>();
        if (threadObject.has("replies")) {
            JsonArray repliesArray = threadObject.getAsJsonArray("replies");
            for (JsonElement replyElement : repliesArray) {
                replies.add(parseThread(replyElement.getAsJsonObject()));
            }
        }

        return new BlueskyThread(
                recordObject.get("text").getAsString(),
                recordObject.get("createdAt").getAsString(),
                replies.isEmpty() ? null : replies,
                postObject.get("replyCount").getAsInt()
        );
    }

    public static void main(String[] args) {
        Map<String, String> arguments = parseArguments(args);
        String inputFile = arguments.get("file");
        boolean weighted = Boolean.parseBoolean(arguments.get("weighted"));

        Database database = new Database();
        List<BlueskyThread> threads = new ArrayList<>();

        try {
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
                        BlueskyThread thread = parseThread(threadObject);
                        threads.add(thread);
                        database.storeThread(thread, String.valueOf(postId));
                        postId++;
                    }
                }

                Analyzer analyzer = weighted ?
                        new WeightedAnalyzer(threads) :
                        new BasicAnalyzer(threads);

                System.out.println("Analysis Results:");
                System.out.println("Total posts: " + analyzer.getTotalPosts());
                System.out.println("Average number of replies: " +
                        String.format("%.2f", analyzer.getAverageReplies()));
                System.out.println("Average duration between replies: " +
                        analyzer.getAverageInterval());
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: Could not find input file: " + inputFile);
            System.exit(1);
        } finally {
            database.close();
        }
    }
}
package com.ecs160.hw1;

import com.google.gson.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class SocialMediaAnalyzerDriver {
    // parses command line arguments into a map with default values
    public static Map<String, String> parseArguments(String[] args) {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("file", "input.json"); // default input file
        arguments.put("weighted", "false");   // default weighting setting

        for (String arg : args) {
            if (arg.startsWith("file=")) {
                arguments.put("file", arg.substring(5));
            } else if (arg.startsWith("weighted=")) {
                arguments.put("weighted", arg.substring(9));
            }
        }
        return arguments;
    }

    public static void main(String[] args) {
        // parse command line arguments
        Map<String, String> arguments = parseArguments(args);
        String inputFile = arguments.get("file");
        boolean weighted = Boolean.parseBoolean(arguments.get("weighted"));

        Database database = new Database();
        List<BlueskyThread> threads = new ArrayList<>();

        try {
            // read and parse json input file
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

                // process each thread in the feed
                for (JsonElement feedObject : feedArray) {
                    if (feedObject.getAsJsonObject().has("thread")) {
                        JsonObject threadObject = feedObject.getAsJsonObject().getAsJsonObject("thread");
                        Gson gson = new Gson();
                        BlueskyThread thread = gson.fromJson(threadObject, BlueskyThread.class);
                        threads.add(thread);

                        // store thread data in database
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

                // calculate and output statistics
                StatisticsCalc stats = new StatisticsCalc(threads, weighted);
                System.out.println("Analysis Results:");
                System.out.println("Total posts: " + stats.getTotalPosts());
                System.out.println("Average number of replies: " + String.format("%.2f", stats.getAverageReplies()));
                System.out.println("Average duration between replies: " + stats.getAverageInterval());
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: Could not find input file: " + inputFile);
            System.exit(1);
        } finally {
            database.close();
        }
    }
}
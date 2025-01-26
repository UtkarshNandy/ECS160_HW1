package com.ecs160.hw1;

import com.google.gson.*;
import java.io.InputStreamReader;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.List;


public class SocialMediaAnalyzerDriver {
    public static void main(String[] args) {
        JsonElement element = JsonParser.parseReader(new InputStreamReader(JsonDeserializer.class.getClassLoader().getResourceAsStream("input.json")));
        Database database = new Database(); // initialize database

        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonArray feedArray = jsonObject.get("feed").getAsJsonArray();
            int key = 0; // initialize key for each object

            for (JsonElement feedObject: feedArray) {
                // Check if you have the thread key
                if (feedObject.getAsJsonObject().has("thread")) {
                    JsonObject threadObject = feedObject.getAsJsonObject().getAsJsonObject("thread");
                    Gson gson = new Gson();
                    BlueskyThread object = gson.fromJson(threadObject, BlueskyThread.class);
                    String stringObject = gson.toJson(object);
                    database.setValue(String.valueOf(key), stringObject);
                    key++;
                }
            }

            String post = database.getValue("400");
            System.out.println(post);
        }
    }
}

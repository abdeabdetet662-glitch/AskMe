package com.askme.ai;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatStorage {

    private static final String PREFS = "askme_chats";
    private final SharedPreferences prefs;

    public ChatStorage(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void save(String personalityId, List<GroqManager.Message> messages) {
        try {
            JSONArray arr = new JSONArray();
            for (GroqManager.Message m : messages) {
                JSONObject o = new JSONObject();
                o.put("role", m.role);
                o.put("content", m.content);
                arr.put(o);
            }
            prefs.edit().putString("chat_" + personalityId, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    public List<GroqManager.Message> load(String personalityId) {
        List<GroqManager.Message> list = new ArrayList<>();
        String data = prefs.getString("chat_" + personalityId, "[]");
        try {
            JSONArray arr = new JSONArray(data);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new GroqManager.Message(o.getString("role"), o.getString("content")));
            }
        } catch (Exception ignored) {}
        return list;
    }

    public void clear(String personalityId) {
        prefs.edit().remove("chat_" + personalityId).apply();
    }
}

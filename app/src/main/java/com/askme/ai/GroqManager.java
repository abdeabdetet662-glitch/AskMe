package com.askme.ai;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroqManager {

    private static final String URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";

    private final OkHttpClient client;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface OnReply {
        void onSuccess(String reply);
        void onError(String error);
    }

    public static class Message {
        public String role;
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public GroqManager() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void sendMessage(String systemPrompt,
                            List<Message> history,
                            String userMessage,
                            OnReply callback) {
        try {
            String apiKey = BuildConfig.GROQ_API_KEY;
            if (apiKey == null || apiKey.isEmpty()) {
                callback.onError("مفتاح API غير مُعد. تأكد من إعداد GROQ_API_KEY");
                return;
            }

            JSONObject body = new JSONObject();
            body.put("model", MODEL);
            body.put("temperature", 0.7);
            body.put("max_tokens", 1024);

            JSONArray messages = new JSONArray();

            JSONObject sys = new JSONObject();
            sys.put("role", "system");
            sys.put("content", systemPrompt);
            messages.put(sys);

            int start = Math.max(0, history.size() - 10);
            for (int i = start; i < history.size(); i++) {
                Message m = history.get(i);
                JSONObject msg = new JSONObject();
                msg.put("role", m.role);
                msg.put("content", m.content);
                messages.put(msg);
            }

            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("content", userMessage);
            messages.put(user);

            body.put("messages", messages);

            RequestBody requestBody = RequestBody.create(
                    body.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mainHandler.post(() -> callback.onError("فشل الاتصال: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resp = response.body() != null ? response.body().string() : "";

                    if (!response.isSuccessful()) {
                        final int code = response.code();
                        mainHandler.post(() -> callback.onError("خطأ " + code + ": " + resp));
                        return;
                    }

                    try {
                        JSONObject json = new JSONObject(resp);
                        JSONArray choices = json.getJSONArray("choices");
                        JSONObject first = choices.getJSONObject(0);
                        JSONObject message = first.getJSONObject("message");
                        String reply = message.getString("content");
                        final String finalReply = reply.trim();
                        mainHandler.post(() -> callback.onSuccess(finalReply));
                    } catch (Exception e) {
                        mainHandler.post(() -> callback.onError("خطأ في التحليل: " + e.getMessage()));
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("خطأ: " + e.getMessage());
        }
    }
}

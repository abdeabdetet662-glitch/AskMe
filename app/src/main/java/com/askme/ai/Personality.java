package com.askme.ai;

public class Personality {
    public String id;
    public String name;
    public String emoji;
    public String tagline;
    public String systemPrompt;
    public int color;

    public Personality(String id, String name, String emoji,
                       String tagline, String systemPrompt, int color) {
        this.id = id;
        this.name = name;
        this.emoji = emoji;
        this.tagline = tagline;
        this.systemPrompt = systemPrompt;
        this.color = color;
    }
}

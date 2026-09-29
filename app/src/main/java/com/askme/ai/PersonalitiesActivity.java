package com.askme.ai;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public class PersonalitiesActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0F"));
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 60, 30, 60);
        scroll.addView(root);

        setContentView(scroll);

        TextView title = new TextView(this);
        title.setText("اختر شخصيتك");
        title.setTextSize(32);
        title.setTextColor(Color.WHITE);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("كل شخصية لها أسلوب مختلف");
        sub.setTextSize(14);
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 10, 0, 50);
        root.addView(sub);

        List<Personality> personalities = PersonalityCatalog.getAll();

        for (Personality p : personalities) {
            root.addView(createCard(p));
        }
    }

    private LinearLayout createCard(Personality p) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(30, 35, 30, 35);
        card.setGravity(Gravity.CENTER_VERTICAL);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#1A1A22"));
        bg.setCornerRadius(24f);
        bg.setStroke(3, p.color);
        card.setBackground(bg);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 25);
        card.setLayoutParams(lp);

        TextView emoji = new TextView(this);
        emoji.setText(p.emoji);
        emoji.setTextSize(42);
        emoji.setPadding(0, 0, 30, 0);
        card.addView(emoji);

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textCol.setLayoutParams(tlp);

        TextView name = new TextView(this);
        name.setText(p.name);
        name.setTextSize(20);
        name.setTextColor(Color.WHITE);
        name.setTypeface(null, Typeface.BOLD);
        textCol.addView(name);

        TextView tag = new TextView(this);
        tag.setText(p.tagline);
        tag.setTextSize(13);
        tag.setTextColor(Color.parseColor("#B0B0B0"));
        tag.setPadding(0, 8, 0, 0);
        textCol.addView(tag);

        card.addView(textCol);

        card.setOnClickListener(v -> {
            Intent i = new Intent(this, ChatActivity.class);
            i.putExtra("personality_id", p.id);
            startActivity(i);
        });

        return card;
    }
}

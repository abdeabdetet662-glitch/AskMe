package com.askme.ai;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.parseColor("#0A0A0F"));

        TextView logo = new TextView(this);
        logo.setText("🤖");
        logo.setTextSize(100);
        logo.setGravity(Gravity.CENTER);
        root.addView(logo);

        TextView name = new TextView(this);
        name.setText("اسألني");
        name.setTextSize(48);
        name.setTextColor(Color.parseColor("#7C4DFF"));
        name.setGravity(Gravity.CENTER);
        name.setPadding(0, 30, 0, 0);
        root.addView(name);

        TextView tag = new TextView(this);
        tag.setText("مساعدك الذكي بشخصيات متعددة");
        tag.setTextSize(14);
        tag.setTextColor(Color.parseColor("#9E9E9E"));
        tag.setGravity(Gravity.CENTER);
        root.addView(tag);

        setContentView(root);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(this, PersonalitiesActivity.class));
            finish();
        }, 2000);
    }
}

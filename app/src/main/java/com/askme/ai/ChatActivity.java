package com.askme.ai;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public class ChatActivity extends Activity {

    private Personality personality;
    private GroqManager groq;
    private ChatStorage storage;
    private List<GroqManager.Message> history;

    private LinearLayout messagesContainer;
    private ScrollView scroll;
    private EditText input;
    private Button sendBtn;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        String id = getIntent().getStringExtra("personality_id");
        personality = PersonalityCatalog.getById(id);

        groq = new GroqManager();
        storage = new ChatStorage(this);
        history = storage.load(personality.id);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0A0A0F"));

        // ===== Header =====
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setBackgroundColor(Color.parseColor("#1A1A22"));
        header.setPadding(30, 40, 30, 40);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView back = new TextView(this);
        back.setText("←");
        back.setTextSize(28);
        back.setTextColor(Color.WHITE);
        back.setPadding(0, 0, 30, 0);
        back.setOnClickListener(v -> finish());
        header.addView(back);

        TextView emoji = new TextView(this);
        emoji.setText(personality.emoji);
        emoji.setTextSize(30);
        emoji.setPadding(0, 0, 20, 0);
        header.addView(emoji);

        LinearLayout titleCol = new LinearLayout(this);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        titleCol.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView name = new TextView(this);
        name.setText(personality.name);
        name.setTextColor(Color.WHITE);
        name.setTextSize(18);
        name.setTypeface(null, Typeface.BOLD);
        titleCol.addView(name);

        TextView status = new TextView(this);
        status.setText("🟢 متصل");
        status.setTextColor(Color.parseColor("#4CAF50"));
        status.setTextSize(11);
        titleCol.addView(status);

        header.addView(titleCol);

        TextView clear = new TextView(this);
        clear.setText("🗑️");
        clear.setTextSize(22);
        clear.setOnClickListener(v -> confirmClear());
        header.addView(clear);

        root.addView(header);

        // ===== Messages area =====
        scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        scroll.setPadding(20, 20, 20, 20);

        messagesContainer = new LinearLayout(this);
        messagesContainer.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(messagesContainer);

        root.addView(scroll);

        // ===== Input bar =====
        LinearLayout inputBar = new LinearLayout(this);
        inputBar.setOrientation(LinearLayout.HORIZONTAL);
        inputBar.setBackgroundColor(Color.parseColor("#1A1A22"));
        inputBar.setPadding(20, 20, 20, 20);
        inputBar.setGravity(Gravity.CENTER_VERTICAL);

        input = new EditText(this);
        input.setHint("اكتب رسالتك...");
        input.setHintTextColor(Color.parseColor("#666666"));
        input.setTextColor(Color.WHITE);
        input.setTextSize(15);
        input.setBackgroundColor(Color.parseColor("#2A2A32"));
        input.setPadding(30, 25, 30, 25);
        input.setMaxLines(4);
        input.setImeOptions(EditorInfo.IME_ACTION_SEND);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        ilp.setMargins(0, 0, 15, 0);
        input.setLayoutParams(ilp);

        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        inputBar.addView(input);

        sendBtn = new Button(this);
        sendBtn.setText("➤");
        sendBtn.setTextSize(18);
        sendBtn.setTextColor(Color.WHITE);
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(personality.color);
        btnBg.setCornerRadius(60f);
        sendBtn.setBackground(btnBg);
        sendBtn.setPadding(30, 0, 30, 0);
        sendBtn.setOnClickListener(v -> sendMessage());
        inputBar.addView(sendBtn);

        root.addView(inputBar);

        setContentView(root);

        // ===== Welcome message =====
        if (history.isEmpty()) {
            addMessage(personality.emoji + " " + getWelcomeMessage(), false);
        } else {
            for (GroqManager.Message m : history) {
                addMessage(m.content, m.role.equals("user"));
            }
        }
    }

    private String getWelcomeMessage() {
        switch (personality.id) {
            case "teacher":   return "مرحبًا! أنا الأستاذ. ماذا تريد أن تتعلم اليوم؟";
            case "therapist": return "أهلًا بك. أنا هنا لأسمعك. كيف تشعر اليوم؟";
            case "coach":     return "هيا بنا! 💪 ما هدفك اليوم؟";
            case "mentor":    return "مرحبًا! اسألني عن أي شيء يخص أموالك.";
            case "writer":    return "أهلًا! ماذا تريد أن أكتب لك اليوم؟";
            case "chef":      return "أهلًا! ما المكونات المتوفرة عندك؟";
            case "friend":    return "يا هلا! 😄 شو الأخبار؟";
            case "historian": return "مرحبًا! أي حقبة تريد أن نستكشف اليوم؟";
            default:          return "مرحبًا! كيف أساعدك؟";
        }
    }

    private void sendMessage() {
        String text = input.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;
        if (isLoading) return;

        input.setText("");
        addMessage(text, true);

        history.add(new GroqManager.Message("user", text));
        storage.save(personality.id, history);

        TextView typing = addTypingIndicator();

        isLoading = true;
        sendBtn.setEnabled(false);

        groq.sendMessage(personality.systemPrompt, history, text,
                new GroqManager.OnReply() {
            @Override
            public void onSuccess(String reply) {
                runOnUiThread(() -> {
                    messagesContainer.removeView(typing);
                    addMessage(reply, false);

                    history.add(new GroqManager.Message("assistant", reply));
                    storage.save(personality.id, history);

                    isLoading = false;
                    sendBtn.setEnabled(true);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    messagesContainer.removeView(typing);
                    addMessage("⚠️ " + error, false);
                    isLoading = false;
                    sendBtn.setEnabled(true);
                });
            }
        });
    }

    private void addMessage(String text, boolean isUser) {
        LinearLayout bubble = new LinearLayout(this);
        bubble.setOrientation(LinearLayout.VERTICAL);
        bubble.setPadding(35, 25, 35, 25);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(30f);

        if (isUser) {
            bg.setColor(personality.color);
        } else {
            bg.setColor(Color.parseColor("#1E1E28"));
        }
        bubble.setBackground(bg);

        TextView msg = new TextView(this);
        msg.setText(text);
        msg.setTextColor(Color.WHITE);
        msg.setTextSize(15);
        msg.setLineSpacing(0, 1.3f);
        bubble.addView(msg);

        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.HORIZONTAL);
        wrapper.setPadding(0, 12, 0, 12);
        wrapper.setGravity(isUser ? Gravity.END : Gravity.START);
        wrapper.addView(bubble);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        wrapper.setLayoutParams(lp);

        messagesContainer.addView(wrapper);

        scroll.post(() -> scroll.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private TextView addTypingIndicator() {
        TextView t = new TextView(this);
        t.setText(personality.emoji + " يكتب...");
        t.setTextColor(Color.parseColor("#9E9E9E"));
        t.setTextSize(14);
        t.setPadding(40, 25, 40, 25);
        messagesContainer.addView(t);
        scroll.post(() -> scroll.fullScroll(ScrollView.FOCUS_DOWN));
        return t;
    }

    private void confirmClear() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("مسح المحادثة؟")
                .setMessage("ستفقد كل الرسائل السابقة.")
                .setPositiveButton("مسح", (d, w) -> {
                    storage.clear(personality.id);
                    history.clear();
                    messagesContainer.removeAllViews();
                    addMessage(personality.emoji + " " + getWelcomeMessage(), false);
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }
}

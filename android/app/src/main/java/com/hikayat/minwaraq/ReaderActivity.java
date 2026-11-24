package com.hikayat.minwaraq;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;
public class ReaderActivity extends AppCompatActivity {
  private final List<String> pages = new ArrayList<>();
  private int idx = 0;
  private String sid = "";
  private SharedPreferences prefs;
  private LinearLayout root;
  private ScrollView scroll;
  private TextView body, info;
  @Override protected void onCreate(Bundle b) {
    super.onCreate(b);
    setContentView(R.layout.activity_reader);
    prefs = getSharedPreferences("hikayat_prefs", MODE_PRIVATE);
    sid = getIntent().getStringExtra("story_id");
    String title = getIntent().getStringExtra("story_title");
    ArrayList<String> inc = getIntent().getStringArrayListExtra("story_pages");
    if (inc != null && !inc.isEmpty()) pages.addAll(inc);
    else pages.add("No pages.");
    root = findViewById(R.id.readerRoot);
    scroll = findViewById(R.id.scrollContent);
    body = findViewById(R.id.tvStoryBody);
    info = findViewById(R.id.tvPageInfo);
    ((TextView) findViewById(R.id.tvReaderTitle)).setText(title != null ? title : "Story");
    findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    findViewById(R.id.btnSettings).setOnClickListener(v -> openSettings());
    findViewById(R.id.btnPrevPage).setOnClickListener(v -> { if (idx > 0) { idx--; render(); } });
    findViewById(R.id.btnNextPage).setOnClickListener(v -> {
      if (idx < pages.size() - 1) { idx++; render(); }
      else if (sid != null && !sid.isEmpty()) {
        Set<String> ar = new HashSet<>(prefs.getStringSet("archive_ids", new HashSet<>()));
        ar.add(sid);
        prefs.edit().putStringSet("archive_ids", ar).apply();
      }
    });
    applyCfg();
    render();
  }
  private void render() {
    body.setText(pages.get(idx));
    info.setText((idx + 1) + " / " + pages.size());
    scroll.scrollTo(0, 0);
  }
  private void applyCfg() {
    float sz = prefs.getFloat("font_size", 18f);
    int mode = prefs.getInt("theme_mode", 0);
    int br = prefs.getInt("brightness", 80);
    body.setTextSize(sz);
    if (mode == 1) { root.setBackgroundColor(Color.parseColor("#F4ECD8")); body.setTextColor(Color.parseColor("#2B2118")); }
    else if (mode == 2) { root.setBackgroundColor(Color.parseColor("#FFFFFF")); body.setTextColor(Color.parseColor("#111111")); }
    else { root.setBackgroundColor(Color.parseColor("#181818")); body.setTextColor(Color.parseColor("#F5F5F5")); }
    WindowManager.LayoutParams lp = getWindow().getAttributes();
    lp.screenBrightness = Math.max(0.1f, Math.min(1.0f, br / 100f));
    getWindow().setAttributes(lp);
  }
  private void openSettings() {
    String[] opts = {"Font +", "Font -", "Night", "Paper", "Day", "Bright 50%", "Bright 95%"};
    new AlertDialog.Builder(this).setTitle("Reader Settings").setItems(opts, (d, w) -> {
      float sz = prefs.getFloat("font_size", 18f);
      if (w == 0) prefs.edit().putFloat("font_size", Math.min(30f, sz + 2f)).apply();
      else if (w == 1) prefs.edit().putFloat("font_size", Math.max(14f, sz - 2f)).apply();
      else if (w == 2) prefs.edit().putInt("theme_mode", 0).apply();
      else if (w == 3) prefs.edit().putInt("theme_mode", 1).apply();
      else if (w == 4) prefs.edit().putInt("theme_mode", 2).apply();
      else if (w == 5) prefs.edit().putInt("brightness", 50).apply();
      else if (w == 6) prefs.edit().putInt("brightness", 95).apply();
      applyCfg();
    }).show();
  }
}

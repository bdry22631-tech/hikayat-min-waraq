package com.hikayat.minwaraq;
import android.content.*;
import android.graphics.Color;
import android.os.Bundle;
import android.text.*;
import android.view.Menu;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
public class MainActivity extends AppCompatActivity {
  private static final int[] PAL = { Color.parseColor("#263238"), Color.parseColor("#1B5E20"), Color.parseColor("#4A148C"), Color.parseColor("#880E4F"), Color.parseColor("#0D47A1"), Color.parseColor("#BF360C") };
  private final List<StoryItem> all = new ArrayList<>();
  private BooksAdapter adapter;
  private String tab = "ALL", q = "";
  private SharedPreferences prefs;
  @Override protected void onCreate(Bundle b) {
    super.onCreate(b);
    setContentView(R.layout.activity_main);
    prefs = getSharedPreferences("hikayat_prefs", MODE_PRIVATE);
    loadBooks();
    RecyclerView rv = findViewById(R.id.rvBooks);
    rv.setLayoutManager(new GridLayoutManager(this, 3));
    adapter = new BooksAdapter(this::openBook);
    rv.setAdapter(adapter);
    findViewById(R.id.btnTabAll).setOnClickListener(v -> { tab = "ALL"; refresh(); });
    findViewById(R.id.btnTabReading).setOnClickListener(v -> { tab = "READING"; refresh(); });
    findViewById(R.id.btnTabArchive).setOnClickListener(v -> { tab = "ARCHIVE"; refresh(); });
    EditText et = findViewById(R.id.etSearch);
    et.addTextChangedListener(new TextWatcher() {
      public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
      public void onTextChanged(CharSequence s, int a, int b, int c) { q = s == null ? "" : s.toString().trim(); refresh(); }
      public void afterTextChanged(Editable s) {}
    });
    BottomNavigationView nav = findViewById(R.id.bottomNav);
    Menu m = nav.getMenu();
    m.add(Menu.NONE, 1, 1, "All");
    m.add(Menu.NONE, 2, 2, "Reading");
    m.add(Menu.NONE, 3, 3, "Archive");
    nav.setOnItemSelectedListener(it -> {
      if (it.getItemId() == 1) tab = "ALL";
      else if (it.getItemId() == 2) tab = "READING";
      else if (it.getItemId() == 3) tab = "ARCHIVE";
      refresh();
      return true;
    });
    refresh();
  }
  @Override protected void onResume() { super.onResume(); refresh(); }
  private void openBook(StoryItem s) {
    Set<String> r = new HashSet<>(prefs.getStringSet("reading_ids", new HashSet<>()));
    r.add(s.id);
    prefs.edit().putStringSet("reading_ids", r).apply();
    Intent it = new Intent(this, ReaderActivity.class);
    it.putExtra("story_id", s.id);
    it.putExtra("story_title", s.title);
    it.putStringArrayListExtra("story_pages", new ArrayList<>(s.pages));
    startActivity(it);
  }
  private void refresh() {
    Set<String> r = prefs.getStringSet("reading_ids", new HashSet<>());
    Set<String> a = prefs.getStringSet("archive_ids", new HashSet<>());
    List<StoryItem> out = new ArrayList<>();
    for (StoryItem s : all) {
      boolean okTab = "ALL".equals(tab) || ("READING".equals(tab) && r.contains(s.id) && !a.contains(s.id)) || ("ARCHIVE".equals(tab) && a.contains(s.id));
      boolean okQ = q.isEmpty() || s.title.contains(q);
      if (okTab && okQ) out.add(s);
    }
    adapter.setItems(out);
  }
  private void loadBooks() {
    all.clear();
    Set<String> seen = new LinkedHashSet<>();
    try {
      InputStream is = getAssets().open("books.json");
      byte[] buf = new byte[is.available()];
      int n = is.read(buf);
      is.close();
      if (n <= 0) return;
      JSONArray arr = new JSONArray(new String(buf, 0, n, StandardCharsets.UTF_8));
      for (int i = 0; i < arr.length(); i++) {
        JSONObject o = arr.optJSONObject(i);
        if (o == null) continue;
        String title = o.optString("title", "").trim();
        if (title.isEmpty()) title = "Story " + (i + 1);
        if (!seen.add(title)) continue;
        String id = o.optString("id", String.valueOf(i + 1));
        StoryItem item = new StoryItem(id, title, PAL[i % PAL.length]);
        Set<String> uniq = new LinkedHashSet<>();
        JSONArray pa = o.optJSONArray("pages");
        if (pa != null) {
          for (int p = 0; p < pa.length(); p++) {
            String pt = clean(pa.optString(p, ""));
            if (!pt.isEmpty() && uniq.add(pt)) item.pages.add(pt);
          }
        } else {
          String txt = clean(o.optString("content", o.optString("text", "")));
          if (!txt.isEmpty()) {
            StringBuilder ch = new StringBuilder();
            for (String ln : txt.split("\n+")) {
              String tr = ln.trim();
              if (tr.isEmpty() || !uniq.add(tr)) continue;
              if (ch.length() + tr.length() > 900 && ch.length() > 0) { item.pages.add(ch.toString().trim()); ch.setLength(0); }
              ch.append(tr).append("

");
            }
            if (ch.length() > 0) item.pages.add(ch.toString().trim());
          }
        }
        if (item.pages.isEmpty()) item.pages.add("No content.");
        all.add(item);
      }
    } catch (Exception ignored) {}
  }
  private String clean(String s) { return s == null ? "" : s.replaceAll("\r\n|\r", "
").trim(); }
}

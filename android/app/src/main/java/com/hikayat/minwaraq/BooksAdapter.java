package com.hikayat.minwaraq;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;
public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.VH> {
  public interface OnClick { void onBook(StoryItem s); }
  private final List<StoryItem> list = new ArrayList<>();
  private final OnClick cb;
  public BooksAdapter(OnClick cb) { this.cb = cb; }
  public void setItems(List<StoryItem> d) { list.clear(); if (d != null) list.addAll(d); notifyDataSetChanged(); }
  @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
    return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_book, p, false));
  }
  @Override public void onBindViewHolder(@NonNull VH h, int pos) {
    StoryItem s = list.get(pos);
    h.title.setText(s.title);
    h.meta.setText(s.pages.size() + " p");
    h.root.setBackgroundColor(s.coverColor);
    h.itemView.setOnClickListener(v -> cb.onBook(s));
  }
  @Override public int getItemCount() { return list.size(); }
  static class VH extends RecyclerView.ViewHolder {
    LinearLayout root; TextView title, meta;
    VH(@NonNull View v) { super(v); root = v.findViewById(R.id.cardRoot); title = v.findViewById(R.id.tvBookTitle); meta = v.findViewById(R.id.tvBookMeta); }
  }
}

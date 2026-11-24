package com.hikayat.minwaraq;
import java.util.*;
public class StoryItem {
  public final String id, title;
  public final int coverColor;
  public final List<String> pages = new ArrayList<>();
  public StoryItem(String id, String title, int coverColor) {
    this.id = id; this.title = title; this.coverColor = coverColor;
  }
}

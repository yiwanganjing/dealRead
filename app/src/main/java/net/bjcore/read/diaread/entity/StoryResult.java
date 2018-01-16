package net.bjcore.read.diaread.entity;

import java.util.ArrayList;
import java.util.List;


public class StoryResult {
     public int novel_id;
     public int chapter_id;
     public Novel novel = new Novel();
     public Chapter chapter = new Chapter();
     public List<Role> roles = new ArrayList<>();
     public List<Content> list = new ArrayList<>();
}

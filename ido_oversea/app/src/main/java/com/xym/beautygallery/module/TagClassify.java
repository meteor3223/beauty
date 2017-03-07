package com.xym.beautygallery.module;

import com.xym.beautygallery.tagview.widget.Tag;

import java.util.List;

/**
 * Created by root on 11/3/16.
 */
public class TagClassify {
    public String classifyName;
    public List<Tag> classifyTagList;

    public TagClassify() {
    }

    public TagClassify(String classifyName, List<Tag> classifyTagList) {
        this.classifyName = classifyName;
        this.classifyTagList = classifyTagList;
    }

    @Override
    public String toString() {
        return "TagClassify{" +
                "classifyName='" + classifyName + '\'' +
                ", classifyTagList=" + classifyTagList +
                '}';
    }
}

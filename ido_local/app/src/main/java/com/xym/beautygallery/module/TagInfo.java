package com.xym.beautygallery.module;

/**
 * Created by root on 11/1/16.
 */
public class TagInfo {
    public String tag_name;
    public String tag_address_root;
    public String classify;
    public String tag_des;
    public int tag_page_num;

    public TagInfo() {
    }

    public TagInfo(String tag_name, String tag_address_root, String classify, String tag_des, int tag_page_num) {
        this.tag_name = tag_name;
        this.tag_address_root = tag_address_root;
        this.classify = classify;
        this.tag_des = tag_des;
        this.tag_page_num = tag_page_num;
    }

    public TagInfo(TagInfo toCopy) {
        this.tag_name = toCopy.tag_name;
        this.tag_address_root = toCopy.tag_address_root;
        this.classify = toCopy.classify;
        this.tag_des = toCopy.tag_des;
        this.tag_page_num = toCopy.tag_page_num;
    }

    @Override
    public String toString() {
        return "TagInfo{" +
                "tag_name='" + tag_name + '\'' +
                ", tag_address_root='" + tag_address_root + '\'' +
                ", classify='" + classify + '\'' +
                ", tag_des='" + tag_des + '\'' +
                ", tag_page_num=" + tag_page_num +
                '}';
    }
}

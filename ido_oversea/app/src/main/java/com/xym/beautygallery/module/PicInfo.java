package com.xym.beautygallery.module;

/**
 * Created by root on 9/26/16.
 */
public class PicInfo {
    public String pic_url_address;
    public String pic_detail;
    public int is_love;
    public long love_time;
    public int pic_width;
    public int pic_height;

    public PicInfo() {
    }

    public PicInfo(PicInfo toCopy) {
        this.pic_url_address = toCopy.pic_url_address;
        this.pic_detail = toCopy.pic_detail;
        this.is_love = toCopy.is_love;
        this.love_time = toCopy.love_time;
        this.pic_width = toCopy.pic_width;
        this.pic_height = toCopy.pic_height;
    }
}

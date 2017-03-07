package com.xym.beautygallery.module;

/**
 * Created by root on 11/1/16.
 */
public class AlbumInfo {
    public String album_address;
    public String album_name;
    public String album_thumb;
    public String album_pics;
    public int album_width;
    public int album_height;
    public int is_love;
    public long love_time;

    public AlbumInfo() {
    }

    public AlbumInfo(AlbumInfo toCopy) {
        this.album_address = toCopy.album_address;
        if (toCopy.album_name != null) {
            this.album_name = toCopy.album_name;
        } else {
            this.album_name = new String();
        }
        this.album_thumb = toCopy.album_thumb;
        this.album_pics = toCopy.album_pics;
        this.album_width = toCopy.album_width;
        this.album_height = toCopy.album_height;
        this.is_love = toCopy.is_love;
        this.love_time = toCopy.love_time;
    }

    @Override
    public String toString() {
        return "AlbumInfo{" +
                "album_address='" + album_address + '\'' +
                ", album_name='" + album_name + '\'' +
                ", album_thumb='" + album_thumb + '\'' +
                ", album_pics=" + album_pics +
                ", album_width=" + album_width +
                ", album_height=" + album_height +
                ", is_love=" + is_love +
                ", love_time=" + love_time +
                '}';
    }
}

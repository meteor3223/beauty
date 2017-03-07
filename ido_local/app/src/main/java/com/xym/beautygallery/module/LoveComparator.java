package com.xym.beautygallery.module;

import java.util.Comparator;

/**
 * Created by root on 7/13/16.
 */
public class LoveComparator implements Comparator<AlbumInfo> {
    @Override
    public int compare(AlbumInfo lhs, AlbumInfo rhs) {
        if (lhs.love_time == rhs.love_time) {
            return 0;
        } else if (lhs.love_time > rhs.love_time) {
            return -1;
        } else {
            return 1;
        }
    }
}

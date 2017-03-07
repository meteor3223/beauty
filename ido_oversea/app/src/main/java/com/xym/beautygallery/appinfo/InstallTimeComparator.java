package com.xym.beautygallery.appinfo;

import java.util.Comparator;

public class InstallTimeComparator implements Comparator<AppInfoSnapshot> {
    private boolean mReverseOrder;

    public InstallTimeComparator() {
        mReverseOrder = false;
    }

    public InstallTimeComparator(boolean reverseOrder) {
        mReverseOrder = reverseOrder;
    }

    @Override
    public int compare(AppInfoSnapshot lhs, AppInfoSnapshot rhs) {
        if (lhs.getInstallTime() == rhs.getInstallTime()) {
            return 0;
        } else if (lhs.getInstallTime() < rhs.getInstallTime()) {
            return mReverseOrder ? 1 : -1;
        } else {
            return mReverseOrder ? -1 : 1;
        }
    }
}

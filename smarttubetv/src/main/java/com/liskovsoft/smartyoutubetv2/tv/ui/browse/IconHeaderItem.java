package com.liskovsoft.smartyoutubetv2.tv.ui.browse;

import androidx.leanback.widget.HeaderItem;

/**
 * MOD: header with a shelf icon (YouTube TV style)
 */
public class IconHeaderItem extends HeaderItem {
    private final int mIconRes;

    public IconHeaderItem(String name, int iconRes) {
        super(name);
        mIconRes = iconRes;
    }

    public int getIconRes() {
        return mIconRes;
    }
}

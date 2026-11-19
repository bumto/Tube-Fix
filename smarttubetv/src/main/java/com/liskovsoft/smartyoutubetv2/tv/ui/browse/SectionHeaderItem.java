package com.liskovsoft.smartyoutubetv2.tv.ui.browse;

import androidx.leanback.widget.HeaderItem;
import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.BrowseSection;

public class SectionHeaderItem extends HeaderItem {
    public static final int GROUP_PRIMARY = 0;
    public static final int GROUP_EXPLORE = 1;
    public static final int GROUP_SYSTEM = 2;
    private final BrowseSection mSection;
    private boolean mGroupStart;

    public SectionHeaderItem(BrowseSection section) {
        super(section.getId(), section.getTitle());
        mSection = section;
    }

    /**
     * Rail grouping: a divider is drawn above the first section of each group.
     */
    public static int groupForType(int type) {
        switch (type) {
            case MediaGroup.TYPE_SETTINGS:
                return GROUP_SYSTEM;
            case MediaGroup.TYPE_TRENDING:
            case MediaGroup.TYPE_MUSIC:
            case MediaGroup.TYPE_MOVIES:
            case MediaGroup.TYPE_GAMING:
            case MediaGroup.TYPE_NEWS:
            case MediaGroup.TYPE_SPORTS:
            case MediaGroup.TYPE_LIVE:
            case MediaGroup.TYPE_SUBSCRIPTIONS:
            case MediaGroup.TYPE_KIDS_HOME:
            case MediaGroup.TYPE_BLOCKED_CHANNELS:
            case MediaGroup.TYPE_NOTIFICATIONS:
                return GROUP_EXPLORE;
            default:
                return GROUP_PRIMARY;
        }
    }

    public boolean isGroupStart() {
        return mGroupStart;
    }

    public void setGroupStart(boolean groupStart) {
        mGroupStart = groupStart;
    }

    public int getType() {
        return mSection.getType();
    }

    public int getResId() {
        return mSection.getResId();
    }

    public String getIconUrl() {
        return mSection.getIconUrl();
    }

    public BrowseSection getSection() {
        return mSection;
    }
}

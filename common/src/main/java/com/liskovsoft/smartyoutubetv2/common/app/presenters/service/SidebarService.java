package com.liskovsoft.smartyoutubetv2.common.app.presenters.service;

import android.annotation.SuppressLint;
import android.content.Context;

import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.prefs.AppPrefs;
import com.liskovsoft.smartyoutubetv2.common.prefs.AppPrefs.ProfileChangeListener;
import com.liskovsoft.smartyoutubetv2.common.prefs.GeneralData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SidebarService implements ProfileChangeListener {
    @SuppressLint("StaticFieldLeak")
    private static SidebarService sInstance;
    private final Context mContext;
    private List<Video> mPinnedItems;
    private final Map<Integer, Integer> mDefaultSections = new LinkedHashMap<>();
    private final AppPrefs mPrefs;
    private boolean mIsSettingsSectionEnabled;
    private int mBootSectionId;
    // MOD: TizenTube-style persisted sidebar config
    private List<Integer> mSidebarContentsOrder;
    private final Set<Integer> mDisabledSidebarContents = new LinkedHashSet<>();
    private final static int RESERVED_ID = 100;
    private final static int[] DEFAULT_SIDEBAR_CONTENTS_ORDER = {
            MediaGroup.TYPE_HOME,
            MediaGroup.TYPE_SHORTS,
            MediaGroup.TYPE_MUSIC,
            MediaGroup.TYPE_LIVE,
            MediaGroup.TYPE_SUBSCRIPTIONS,
            MediaGroup.TYPE_SETTINGS
    };
    private final static int[] DEFAULT_DISABLED_SIDEBAR_CONTENTS = {
            MediaGroup.TYPE_SEARCH,
            MediaGroup.TYPE_RECOMMENDED,
            MediaGroup.TYPE_HISTORY,
            MediaGroup.TYPE_USER_PLAYLISTS,
            MediaGroup.TYPE_MOVIES,
            MediaGroup.TYPE_GAMING,
            MediaGroup.TYPE_NEWS,
            MediaGroup.TYPE_SPORTS,
            MediaGroup.TYPE_NOTIFICATIONS,
            MediaGroup.TYPE_SUGGESTIONS,
            MediaGroup.TYPE_CHANNEL,
            MediaGroup.TYPE_CHANNEL_UPLOADS,
            MediaGroup.TYPE_KIDS_HOME,
            MediaGroup.TYPE_TRENDING,
            MediaGroup.TYPE_MY_VIDEOS,
            MediaGroup.TYPE_PLAYBACK_QUEUE,
            MediaGroup.TYPE_BLOCKED_CHANNELS
    };

    private SidebarService(Context context) {
        mContext = context.getApplicationContext();
        mPrefs = AppPrefs.instance(context);
        mPrefs.addListener(this);
        initSections();
        restoreState();
    }

    public static SidebarService instance(Context context) {
        if (sInstance == null && context != null) {
            sInstance = new SidebarService(context.getApplicationContext());
        }

        return sInstance;
    }

    public Collection<Video> getPinnedItems() {
        return Collections.unmodifiableList(mPinnedItems);
    }

    public int addPinnedItem(Video item) {
        if (mPinnedItems.contains(item)) {
            return -1;
        }

        int idx = Helpers.indexOfFirst(mPinnedItems, obj -> obj.getId() > RESERVED_ID);

        if (idx != -1) {
            mPinnedItems.add(idx, item);
        } else {
            mPinnedItems.add(item);
        }
        persistState();

        return idx;
    }

    public void removePinnedItem(Video item) {
        mPinnedItems.remove(item);
        persistState();
    }

    public void enableSection(int sectionId, boolean enabled) {
        if (enabled) {
            mDisabledSidebarContents.remove(sectionId);

            if (sectionId == MediaGroup.TYPE_SETTINGS) {
                mIsSettingsSectionEnabled = true; // prevent Settings lock
            }

            //Video item = new Video();
            //item.sectionId = sectionId;
            //
            //if (mPinnedItems.contains(item)) { // don't reorder if item already exists
            //    return;
            //}

            Video section = Helpers.findFirst(mPinnedItems, item -> item != null && item.sectionId == sectionId);

            if (section != null) { // don't reorder if item already exists
                return;
            }

            Video item = new Video();
            item.sectionId = sectionId;

            int index = getDefaultSectionIndex(sectionId);

            if (index == -1 || index > mPinnedItems.size()) {
                mPinnedItems.add(item);
            } else {
                mPinnedItems.add(index, item);
            }
        } else {
            mDisabledSidebarContents.add(sectionId);
            Helpers.removeIf(mPinnedItems, item -> item == null || item.sectionId == sectionId);
        }

        persistState();
    }

    public Map<Integer, Integer> getDefaultSections() {
        return mDefaultSections;
    }

    private int getDefaultSectionIndex(int sectionId) {
        int index = -1;

        Collection<Integer> values = mDefaultSections.values();

        for (int item : values) {
            index++;
            if (item == sectionId) {
                break;
            }
        }

        return index;
    }

    /**
     * Contains sections and pinned items!
     */
    public boolean isSectionPinned(int sectionId) {
        if (mDisabledSidebarContents.contains(sectionId)) {
            return false;
        }

        Video section = Helpers.findFirst(mPinnedItems, item -> getSectionId(item) == sectionId);
        return section != null; // by default enable all pinned items
    }

    public List<Integer> getSidebarContentsOrder() {
        return Collections.unmodifiableList(mSidebarContentsOrder);
    }

    public void setSidebarContentsOrder(List<Integer> order) {
        if (order == null) {
            return;
        }

        mSidebarContentsOrder = normalizeOrder(order);
        persistState();
    }

    public Set<Integer> getDisabledSidebarContents() {
        return Collections.unmodifiableSet(mDisabledSidebarContents);
    }

    public void setDisabledSidebarContents(Collection<Integer> sectionIds) {
        mDisabledSidebarContents.clear();

        if (sectionIds != null) {
            mDisabledSidebarContents.addAll(sectionIds);
        } else {
            for (int sectionId : DEFAULT_DISABLED_SIDEBAR_CONTENTS) {
                mDisabledSidebarContents.add(sectionId);
            }
        }

        mDisabledSidebarContents.add(MediaGroup.TYPE_SEARCH);

        syncPinnedSections();
        persistState();
    }

    public int getSectionIndex(int sectionId) {
        int index = mSidebarContentsOrder.indexOf(sectionId);

        if (index >= 0) {
            return index;
        }

        // Unknown sections and user pinned items go after the configured tabs.
        int pinnedIndex = findPinnedItemIndex(sectionId);

        return pinnedIndex < 0 ? mSidebarContentsOrder.size() + RESERVED_ID : mSidebarContentsOrder.size() + pinnedIndex;
    }

    public void renameSection(int sectionId, String newTitle) {
        int index = findPinnedItemIndex(sectionId);
        if (index != -1) {
            Video video = mPinnedItems.get(index);
            video.title = newTitle;
            persistState();
        }
    }

    public void moveSectionUp(int sectionId) {
        shiftSection(sectionId, -1);
    }

    public void moveSectionDown(int sectionId) {
        shiftSection(sectionId, 1);
    }

    public boolean canMoveSectionUp(int sectionId) {
        return canShiftSection(sectionId, -1);
    }

    public boolean canMoveSectionDown(int sectionId) {
        return canShiftSection(sectionId, 1);
    }

    private boolean canShiftSection(int sectionId, int shift) {
        int orderIndex = mSidebarContentsOrder.indexOf(sectionId);

        if (orderIndex >= 0) {
            return orderIndex + shift >= 0 && orderIndex + shift < mSidebarContentsOrder.size();
        }

        int index = findPinnedItemIndex(sectionId);

        if (index != -1) {
            return index + shift >= 0 && index + shift < mPinnedItems.size();
        }

        return false;
    }

    private void shiftSection(int sectionId, int shift) {
        if (!canShiftSection(sectionId, shift)) {
            return;
        }

        int orderIndex = mSidebarContentsOrder.indexOf(sectionId);

        if (orderIndex >= 0) {
            int targetIndex = orderIndex + shift;
            mSidebarContentsOrder.set(orderIndex, mSidebarContentsOrder.get(targetIndex));
            mSidebarContentsOrder.set(targetIndex, sectionId);
            persistState();
            return;
        }

        int index = findPinnedItemIndex(sectionId);

        if (index != -1) {
            Video current = mPinnedItems.get(index);
            mPinnedItems.remove(current);

            mPinnedItems.add(index + shift, current);
            persistState();
        }
    }

    private int findPinnedItemIndex(int sectionId) {
        int index = -1;

        for (Video item : mPinnedItems) {
            // Distinguish pinned items by hashCode or extra field (default section)!
            if (getSectionId(item) == sectionId) {
                index = mPinnedItems.indexOf(item);
                break;
            }
        }

        return index;
    }

    public void setBootSectionId(int sectionId) {
        mBootSectionId = sectionId;

        persistState();
    }

    public int getBootSectionId() {
        return mBootSectionId;
    }

    public void enableSettingsSection(boolean enabled) {
        mIsSettingsSectionEnabled = enabled;

        persistState();
    }

    public boolean isSettingsSectionEnabled() {
        return mIsSettingsSectionEnabled;
    }

    private int getSectionId(Video item) {
        if (item == null) {
            return -1;
        }

        return item.sectionId == -1 ? item.getId() : item.sectionId;
    }

    private void initSections() {
        mDefaultSections.put(R.string.header_notifications, MediaGroup.TYPE_NOTIFICATIONS);
        mDefaultSections.put(R.string.header_home, MediaGroup.TYPE_HOME);
        mDefaultSections.put(R.string.header_shorts, MediaGroup.TYPE_SHORTS);
        mDefaultSections.put(R.string.header_trending, MediaGroup.TYPE_TRENDING);
        mDefaultSections.put(R.string.header_kids_home, MediaGroup.TYPE_KIDS_HOME);
        mDefaultSections.put(R.string.header_sports, MediaGroup.TYPE_SPORTS);
        mDefaultSections.put(R.string.badge_live, MediaGroup.TYPE_LIVE);
        mDefaultSections.put(R.string.header_gaming, MediaGroup.TYPE_GAMING);
        mDefaultSections.put(R.string.header_news, MediaGroup.TYPE_NEWS);
        mDefaultSections.put(R.string.header_music, MediaGroup.TYPE_MUSIC);
        mDefaultSections.put(R.string.header_film, MediaGroup.TYPE_MOVIES);
        mDefaultSections.put(R.string.header_channels, MediaGroup.TYPE_CHANNEL_UPLOADS);
        mDefaultSections.put(R.string.header_subscriptions, MediaGroup.TYPE_SUBSCRIPTIONS);
        mDefaultSections.put(R.string.header_history, MediaGroup.TYPE_HISTORY);
        mDefaultSections.put(R.string.header_blocked_channels, MediaGroup.TYPE_BLOCKED_CHANNELS);
        mDefaultSections.put(R.string.header_playlists, MediaGroup.TYPE_USER_PLAYLISTS);
        mDefaultSections.put(R.string.my_videos, MediaGroup.TYPE_MY_VIDEOS);
        mDefaultSections.put(R.string.playback_queue_category_title, MediaGroup.TYPE_PLAYBACK_QUEUE);
        mDefaultSections.put(R.string.header_settings, MediaGroup.TYPE_SETTINGS);
    }

    private List<Integer> normalizeOrder(List<Integer> order) {
        List<Integer> result = new ArrayList<>();

        if (order != null) {
            for (int sectionId : order) {
                if (sectionId != -1 && !result.contains(sectionId)) {
                    result.add(sectionId);
                }
            }
        }

        for (int sectionId : DEFAULT_SIDEBAR_CONTENTS_ORDER) {
            if (!result.contains(sectionId)) {
                result.add(sectionId);
            }
        }

        return result;
    }

    private void initPinnedItems() {
        for (int sectionId : mDefaultSections.values()) {
            // Hidden sections
            enableSection(sectionId, !Helpers.equalsAny(sectionId, DEFAULT_DISABLED_SIDEBAR_CONTENTS));
        }
    }

    private void cleanupPinnedItems() {
        Helpers.removeDuplicates(mPinnedItems);

        Helpers.removeIf(mPinnedItems, item -> {
            if (item == null) {
                return true;
            }

            item.videoId = null;

            // Fix id collision between pinned and default sections
            if (item.getId() < RESERVED_ID && item.getId() >= -1 && item.sectionId == -1) {
                return true;
            }

            return !item.hasPlaylist() && item.channelId == null && item.sectionId == -1 && item.channelGroupId == null && !item.hasReloadPageKey();
        });
    }

    private void syncPinnedSections() {
        mDisabledSidebarContents.remove(MediaGroup.TYPE_SETTINGS);

        Helpers.removeIf(mPinnedItems, item -> item == null || (item.sectionId != -1
                && mDisabledSidebarContents.contains(item.sectionId)));

        for (int sectionId : DEFAULT_SIDEBAR_CONTENTS_ORDER) {
            if (!mDisabledSidebarContents.contains(sectionId) && findPinnedItemIndex(sectionId) == -1) {
                Video item = new Video();
                item.sectionId = sectionId;
                mPinnedItems.add(item);
            }
        }
    }

    private void restoreState() {
        String data = mPrefs.getSidebarData();

        String[] split = Helpers.splitData(data);

        mPinnedItems = Helpers.parseList(split, 0, Video::fromString);

        if (mPinnedItems == null) {
            mPinnedItems = new ArrayList<>();
        }

        mBootSectionId = Helpers.parseInt(split, 1, MediaGroup.TYPE_HOME);
        mIsSettingsSectionEnabled = true;

        // MOD: restore persisted rail order and hidden tabs while keeping TizenTube defaults.
        List<Integer> savedOrder = split != null && split.length > 3 ? Helpers.parseIntList(split, 3) : null;
        mSidebarContentsOrder = normalizeOrder(savedOrder != null && !savedOrder.isEmpty() ? savedOrder : null);

        mDisabledSidebarContents.clear();
        List<Integer> savedDisabled = split != null && split.length > 4 ? Helpers.parseIntList(split, 4) : null;

        if (savedDisabled == null) {
            for (int sectionId : DEFAULT_DISABLED_SIDEBAR_CONTENTS) {
                mDisabledSidebarContents.add(sectionId);
            }
        } else {
            mDisabledSidebarContents.addAll(savedDisabled);
        }
        mDisabledSidebarContents.add(MediaGroup.TYPE_SEARCH);

        syncPinnedSections();

        cleanupPinnedItems();
        persistState();
    }

    private void transferOldPinnedItems() {
        if (mPinnedItems != null && !mPinnedItems.isEmpty()) {
            return;
        }

        List<Video> oldPinnedItems = GeneralData.instance(mContext).getOldPinnedItems();

        if (oldPinnedItems != null && !oldPinnedItems.isEmpty()) {
            mPinnedItems = oldPinnedItems;
        }
    }

    public void persistState() {
        mPrefs.setSidebarData(Helpers.mergeData(
                mPinnedItems,
                mBootSectionId,
                mIsSettingsSectionEnabled,
                Helpers.mergeList(mSidebarContentsOrder),
                Helpers.mergeList(new ArrayList<>(mDisabledSidebarContents))
        ));
    }

    @Override
    public void onProfileChanged() {
        restoreState();
    }
}

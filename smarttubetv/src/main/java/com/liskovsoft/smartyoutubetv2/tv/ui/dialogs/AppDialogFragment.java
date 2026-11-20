package com.liskovsoft.smartyoutubetv2.tv.ui.dialogs;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.leanback.preference.LeanbackPreferenceFragment;
import androidx.leanback.preference.LeanbackSettingsFragment;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceScreen;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionCategory;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.AppDialogPresenter;
import com.liskovsoft.smartyoutubetv2.common.app.views.AppDialogView;
import com.liskovsoft.smartyoutubetv2.common.misc.MotherActivity;
import com.liskovsoft.smartyoutubetv2.common.utils.Utils;
import com.liskovsoft.smartyoutubetv2.tv.R;
import com.liskovsoft.smartyoutubetv2.tv.ui.dialogs.other.CommentsPreference;
import com.liskovsoft.smartyoutubetv2.tv.ui.dialogs.other.CommentsPreferenceDialogFragment;
import com.liskovsoft.smartyoutubetv2.tv.ui.dialogs.other.RadioListPreferenceDialogFragment;
import com.liskovsoft.smartyoutubetv2.tv.ui.dialogs.twopane.TwoPaneSettingsView;
import com.liskovsoft.smartyoutubetv2.tv.ui.dialogs.other.StringListPreference;
import com.liskovsoft.smartyoutubetv2.tv.ui.dialogs.other.StringListPreferenceDialogFragment;
import com.liskovsoft.smartyoutubetv2.tv.ui.mod.leanback.preference.LeanbackListPreferenceDialogFragment;
import com.liskovsoft.smartyoutubetv2.tv.util.ViewUtil;

import java.util.List;

@SuppressWarnings("deprecation")
public class AppDialogFragment extends LeanbackSettingsFragment implements AppDialogView {
    private static final String TAG = AppDialogFragment.class.getSimpleName();
    private AppDialogPresenter mPresenter;
    private AppPreferenceManager mManager;
    private boolean mIsTransparent;
    private boolean mIsOverlay;
    private boolean mIsPaused;
    private int mId;

    // MOD: two-pane layout for settings dialogs (set by SettingsGridFragment)
    private static boolean sTwoPaneEnabled;
    private static boolean sTwoPaneActive;

    public static void setTwoPaneEnabled(boolean enabled) {
        sTwoPaneEnabled = enabled;
    }

    public static boolean isTwoPaneEnabled() {
        return sTwoPaneEnabled;
    }

    /**
     * True while the two-pane view is actually on screen. Used to allow horizontal
     * navigation inside the panes instead of closing the dialog.
     */
    public static boolean isTwoPaneActive() {
        return sTwoPaneActive;
    }

    private static final String PREFERENCE_FRAGMENT_TAG =
            "androidx.leanback.preference.LeanbackSettingsFragment.PREFERENCE_FRAGMENT";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenter = AppDialogPresenter.instance(getActivity());
        mPresenter.setView(this);
        mManager = new AppPreferenceManager(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        // Workaround for dialog that are destroyed with the delay (e.g. transparent dialogs)
        mIsPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        // Workaround for dialog that are destroyed with the delay (e.g. transparent dialogs)
        mIsPaused = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (mPresenter.getView() == this) {
            mPresenter.onViewDestroyed();
        }
    }

    @Override
    public void onPreferenceStartInitialScreen() {
        // FIX: Can not perform this action after onSaveInstanceState
        // Possible fix: Unable to add window -- token android.os.BinderProxy is not valid; is your activity running?
        if (!Utils.checkActivity(getActivity())) {
            return;
        }

        try {
            // Fix mPresenter in null after init stage.
            // Seems concurrency between dialogs.
            mPresenter.setView(this);

            mPresenter.onViewInitialized();
        } catch (IllegalStateException e) {
            // NOP
        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment preferenceFragment, Preference preference) {
        // Contains only child fragments.
        return false;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragment preferenceFragment, PreferenceScreen preferenceScreen) {
        // Contains only child fragments.
        return false;
    }

    private AppPreferenceFragment buildPreferenceFragment(List<OptionCategory> categories, CharSequence title) {
        AppPreferenceFragment fragment = new AppPreferenceFragment();
        fragment.setCategories(categories);
        fragment.setTitle(title);
        fragment.enableTransparent(mIsTransparent);
        return fragment;
    }

    @Override
    public void show(List<OptionCategory> categories, CharSequence title, boolean isExpandable, boolean isTransparent, boolean isOverlay, int id) {
        if (!Utils.checkActivity(getActivity())) {
            return;
        }

        // Only root fragment could make other fragments in the stack transparent
        boolean stackIsEmpty = getChildFragmentManager() != null && getChildFragmentManager().getBackStackEntryCount() == 0;
        mIsTransparent = stackIsEmpty ? isTransparent : mIsTransparent;
        mIsOverlay = isOverlay;
        mId = id;

        if (isExpandable && categories != null && categories.size() == 1) {
            OptionCategory category = categories.get(0);
            if (category.options != null) {
                onPreferenceDisplayDialog(null, mManager.createPreference(category));
            }
        } else {
            AppPreferenceFragment fragment = buildPreferenceFragment(categories, title);
            startPreferenceFragment(fragment);
        }
    }

    @Override
    public boolean onPreferenceDisplayDialog(@Nullable PreferenceFragment caller, @NonNull Preference pref) {
        // Fix: IllegalStateException: Activity has been destroyed
        // Possible fix: Unable to add window -- token android.os.BinderProxy is not valid; is your activity running?
        if (!Utils.checkActivity(getActivity())) {
            return false;
        }

        if (pref instanceof StringListPreference) {
            StringListPreference listPreference = (StringListPreference) pref;
            StringListPreferenceDialogFragment f = StringListPreferenceDialogFragment.newInstanceStringList(listPreference.getKey());
            f.enableTransparent(mIsTransparent);
            f.setTargetFragment(caller, 0);
            f.setPreference(pref);
            startPreferenceFragment(f);

            return true;
        } else if (pref instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) pref;
            RadioListPreferenceDialogFragment f = RadioListPreferenceDialogFragment.newInstanceSingle(listPreference.getKey());
            f.enableTransparent(mIsTransparent);
            f.setTargetFragment(caller, 0);
            f.setPreference(pref);
            startPreferenceFragment(f);

            return true;
        } else if (pref instanceof CommentsPreference) {
            ((MotherActivity) getActivity()).enableThrottleKeyDown(true);
            CommentsPreference commentsPreference = (CommentsPreference) pref;
            CommentsPreferenceDialogFragment f = CommentsPreferenceDialogFragment.newInstance(commentsPreference.getCommentsReceiver(), commentsPreference.getKey());
            f.enableTransparent(mIsTransparent);
            f.setTargetFragment(caller, 0);
            f.setPreference(pref);
            startPreferenceFragment(f);

            return true;
        } else if (pref instanceof MultiSelectListPreference) {
            MultiSelectListPreference listPreference = (MultiSelectListPreference) pref;
            LeanbackListPreferenceDialogFragment f = LeanbackListPreferenceDialogFragment.newInstanceMulti(listPreference.getKey());
            f.setTargetFragment(caller, 0);
            f.setPreference(pref);
            startPreferenceFragment(f);
        }
        // TODO
        // else if (pref instanceof EditTextPreference) {
        //
        //        }
        else {
            // Single button item. Imitate click on it (expandable = true).
            if (pref.getOnPreferenceClickListener() != null) {
                pref.getOnPreferenceClickListener().onPreferenceClick(pref);
            }

            return false;
        }

        // NOTE: Transparent CheckedList should be placed here (just in case you'll need it).

        //return super.onPreferenceDisplayDialog(caller, pref);
        return true;
    }

    /**
     * Fix possible state loss!!!
     */
    @Override
    public void startPreferenceFragment(@NonNull Fragment fragment) {
        final FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        final Fragment prevFragment =
                getChildFragmentManager().findFragmentByTag(PREFERENCE_FRAGMENT_TAG);
        if (prevFragment != null) {
            transaction
                    .addToBackStack(null)
                    .replace(R.id.settings_preference_fragment_container, fragment,
                            PREFERENCE_FRAGMENT_TAG);
        } else {
            transaction
                    .add(R.id.settings_preference_fragment_container, fragment,
                            PREFERENCE_FRAGMENT_TAG);
        }
        // Fix possible state loss!!!
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void finish() {
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void goBack() {
        if (canGoBack()) {
            getChildFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

    @Override
    public void clearBackstack() {
        // this manager holds entire back stack
        Helpers.setField(this, "mChildFragmentManager", null);
    }

    @Override
    public boolean canGoBack() {
        return getChildFragmentManager() != null && getChildFragmentManager().getBackStackEntryCount() > 0;
    }

    @Override
    public boolean isShown() {
        return isVisible() && getUserVisibleHint();
    }

    @Override
    public boolean isTransparent() {
        return mIsTransparent;
    }

    @Override
    public boolean isOverlay() {
        return mIsOverlay;
    }

    @Override
    public boolean isPaused() {
        return mIsPaused;
    }

    @Override
    public int getViewId() {
        return mId;
    }

    public void onFinish() {
        mPresenter.onFinish();
    }
    
    public static class AppPreferenceFragment extends LeanbackPreferenceFragment {
        private static final String TAG = AppPreferenceFragment.class.getSimpleName();
        private List<OptionCategory> mCategories;
        private Context mExtractedContext;
        private AppPreferenceManager mManager;
        private CharSequence mTitle;
        private boolean mIsTransparent;
        private FrameLayout mExpandedContainer;
        private FrameLayout.LayoutParams mOriginalContainerParams;

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            // Note, place in field with different name to avoid field overlapping
            mExtractedContext = (Context) Helpers.getField(this, "mStyledContext");
            // Note, don't use external manager (probable focus lost and visual bugs)
            mManager = new AppPreferenceManager(mExtractedContext);

            initPrefs();

            Log.d(TAG, "onCreatePreferences");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Initialize preference internals first (keeps lifecycle hooks valid).
            View preferenceView = super.onCreateView(inflater, container, savedInstanceState);

            Context context = getActivity();

            if (sTwoPaneEnabled && context != null) {
                // MOD: YouTube TV style settings — categories left, options right.
                sTwoPaneActive = true;
                expandSettingsContainer();
                return new TwoPaneSettingsView(context, mCategories, mTitle);
            }

            View view = preferenceView;

            if (mIsTransparent && view != null) {
                // Enable transparent shadow outline on parent (R.id.settings_preference_fragment_container)
                ViewUtil.enableTransparentDialog(getActivity(), getParentFragment().getView());
                // Enable transparency on child fragment itself (isn't attached to parent yet)
                ViewUtil.enableTransparentDialog(getActivity(), view);
            }

            return view;
        }

        @Override
        public void onDestroyView() {
            sTwoPaneActive = false;
            restoreSettingsContainer();
            super.onDestroyView();
        }

        private void expandSettingsContainer() {
            // MOD: the leanback settings container occupies only the right half of the screen.
            // A two-pane layout needs the full width, so expand it while the two-pane view is shown.
            View root = getParentFragment() != null ? getParentFragment().getView() : null;
            if (root == null) {
                return;
            }

            View container = root.findViewById(R.id.settings_preference_fragment_container);
            if (!(container instanceof FrameLayout) || container == mExpandedContainer) {
                return;
            }

            ViewGroup.LayoutParams lp = container.getLayoutParams();
            if (!(lp instanceof FrameLayout.LayoutParams)) {
                return;
            }

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) lp;
            mExpandedContainer = (FrameLayout) container;
            mOriginalContainerParams = new FrameLayout.LayoutParams(params.width, params.height);
            mOriginalContainerParams.gravity = params.gravity;
            mOriginalContainerParams.leftMargin = params.leftMargin;
            mOriginalContainerParams.topMargin = params.topMargin;
            mOriginalContainerParams.rightMargin = params.rightMargin;
            mOriginalContainerParams.bottomMargin = params.bottomMargin;

            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            params.leftMargin = 0;
            params.rightMargin = 0;
            params.gravity = Gravity.START | Gravity.TOP;
            container.setLayoutParams(params);

            TypedValue background = new TypedValue();
            int backgroundColor = Color.parseColor("#0F0F0F");

            if (container.getContext().getTheme().resolveAttribute(R.attr.ttTwoPaneBackgroundColor, background, true)) {
                backgroundColor = background.data;
            }

            container.setBackgroundColor(backgroundColor);
            container.bringToFront();
        }

        private void restoreSettingsContainer() {
            if (mExpandedContainer == null || mOriginalContainerParams == null) {
                return;
            }

            mExpandedContainer.setLayoutParams(mOriginalContainerParams);
            mExpandedContainer.setBackgroundColor(Color.TRANSPARENT);
            mExpandedContainer = null;
            mOriginalContainerParams = null;
        }

        private void initPrefs() {
            PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(mExtractedContext);
            setPreferenceScreen(screen);

            screen.setTitle(mTitle);

            addPreferences(screen);
        }

        private void addPreferences(PreferenceScreen screen) {
            if (mCategories != null) {
                for (OptionCategory category : mCategories) {
                    if (category.options != null) {
                        screen.addPreference(mManager.createPreference(category));
                    }
                }
            }
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            super.onDisplayPreferenceDialog(preference);
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            return super.onPreferenceTreeClick(preference);
        }

        public void setCategories(List<OptionCategory> categories) {
            mCategories = categories;
        }

        public void setTitle(CharSequence title) {
            super.setTitle(title);

            mTitle = title;
        }

        public void enableTransparent(boolean enable) {
            mIsTransparent = enable;
        }
    }
}
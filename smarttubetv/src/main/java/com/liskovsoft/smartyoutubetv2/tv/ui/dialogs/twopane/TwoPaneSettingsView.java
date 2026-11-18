package com.liskovsoft.smartyoutubetv2.tv.ui.dialogs.twopane;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.VerticalGridView;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionCategory;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionItem;
import com.liskovsoft.smartyoutubetv2.tv.presenter.TwoPaneCategoryPresenter;
import com.liskovsoft.smartyoutubetv2.tv.presenter.TwoPaneOptionPresenter;

import java.util.ArrayList;
import java.util.List;

/**
 * MOD: YouTube TV style two-pane settings.<br/>
 * Left: option categories. Right: options of the selected category.
 */
public class TwoPaneSettingsView extends LinearLayout {
    public interface Listener {
        void onItem(Object item);
    }

    private final List<OptionCategory> mCategories = new ArrayList<>();
    private final ArrayObjectAdapter mCategoryAdapter;
    private final ArrayObjectAdapter mOptionAdapter;
    private final VerticalGridView mCategoryGrid;
    private final VerticalGridView mOptionGrid;
    private OptionCategory mSelectedCategory;

    public TwoPaneSettingsView(Context context, List<OptionCategory> categories, CharSequence title) {
        super(context);

        setOrientation(VERTICAL);
        setBackgroundColor(Color.parseColor("#0F0F0F"));
        setPadding(0, dp(32), 0, 0);

        if (categories != null) {
            for (OptionCategory category : categories) {
                if (category != null && category.options != null && !category.options.isEmpty()) {
                    mCategories.add(category);
                }
            }
        }

        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(22);
        titleView.setTypeface(titleView.getTypeface(), android.graphics.Typeface.BOLD);
        titleView.setPadding(dp(48), dp(24), dp(48), dp(16));
        addView(titleView);

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(HORIZONTAL);
        row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));
        addView(row);

        mCategoryGrid = new VerticalGridView(context);
        LayoutParams leftParams = new LayoutParams(dp(360), LayoutParams.MATCH_PARENT);
        leftParams.setMarginStart(dp(48));
        mCategoryGrid.setLayoutParams(leftParams);
        mCategoryGrid.setNumColumns(1);
        mCategoryGrid.setVerticalSpacing(dp(4));
        mCategoryGrid.setFocusable(false);
        row.addView(mCategoryGrid);

        mOptionGrid = new VerticalGridView(context);
        LayoutParams rightParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f);
        rightParams.setMarginStart(dp(24));
        rightParams.setMarginEnd(dp(48));
        mOptionGrid.setLayoutParams(rightParams);
        mOptionGrid.setNumColumns(1);
        mOptionGrid.setVerticalSpacing(dp(4));
        mOptionGrid.setFocusable(false);
        row.addView(mOptionGrid);

        mCategoryAdapter = new ArrayObjectAdapter(new TwoPaneCategoryPresenter(context, item -> {
            if (item instanceof OptionCategory) {
                selectCategory((OptionCategory) item);
            }
        }));

        mOptionAdapter = new ArrayObjectAdapter(new TwoPaneOptionPresenter(context, item -> {
            if (item instanceof OptionItem && mSelectedCategory != null) {
                onOptionClicked(mSelectedCategory, (OptionItem) item);
            }
        }));

        mCategoryGrid.setAdapter(new ItemBridgeAdapter(mCategoryAdapter));
        mOptionGrid.setAdapter(new ItemBridgeAdapter(mOptionAdapter));

        mCategoryAdapter.addAll(0, mCategories);

        if (!mCategories.isEmpty()) {
            selectCategory(mCategories.get(0));
        }

        post(() -> {
            if (!mCategories.isEmpty()) {
                mCategoryGrid.requestFocus();
                mCategoryGrid.setSelectedPosition(0);
            }
        });
    }

    public static CharSequence getCategoryTitle(OptionCategory category) {
        if (!TextUtils.isEmpty(category.title)) {
            return category.title;
        }

        if (category.options != null && !category.options.isEmpty()) {
            CharSequence first = category.options.get(0).getTitle();
            if (!TextUtils.isEmpty(first)) {
                return first;
            }
        }

        return "";
    }

    private void selectCategory(OptionCategory category) {
        if (category == mSelectedCategory) {
            return;
        }

        mSelectedCategory = category;
        mOptionAdapter.clear();

        if (category != null && category.options != null) {
            mOptionAdapter.addAll(0, category.options);
        }
    }

    private void focusOptions() {
        post(() -> {
            if (mOptionAdapter.size() > 0) {
                mOptionGrid.requestFocus();
                mOptionGrid.setSelectedPosition(0);
            }
        });
    }

    private void onOptionClicked(OptionCategory category, OptionItem clicked) {
        switch (category.type) {
            case OptionCategory.TYPE_SINGLE_BUTTON:
            case OptionCategory.TYPE_COMMENTS:
                clicked.onSelect(true);
                break;
            case OptionCategory.TYPE_LONG_TEXT:
                CharSequence text = clicked.getTitle();
                MessageHelpers.showMessage(getContext(), text != null ? text.toString() : "");
                return;
            case OptionCategory.TYPE_RADIO_LIST:
                for (OptionItem item : category.options) {
                    if (item != clicked) {
                        item.onSelect(false);
                    }
                }
                clicked.onSelect(true);
                break;
            default:
                boolean isSelected = !clicked.isSelected();
                clicked.onSelect(isSelected);

                if (isSelected) {
                    OptionItem[] required = clicked.getRequired();
                    if (required != null) {
                        for (OptionItem item : required) {
                            if (!item.isSelected()) {
                                item.onSelect(true);
                            }
                        }
                    }

                    OptionItem[] radio = clicked.getRadio();
                    if (radio != null) {
                        for (OptionItem item : radio) {
                            if (item != clicked && item.isSelected()) {
                                item.onSelect(false);
                            }
                        }
                    }
                }
                break;
        }

        mOptionAdapter.notifyArrayItemRangeChanged(0, mOptionAdapter.size());
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}

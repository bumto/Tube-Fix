package com.liskovsoft.smartyoutubetv2.tv.presenter;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.Presenter;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionCategory;
import com.liskovsoft.smartyoutubetv2.tv.R;
import com.liskovsoft.smartyoutubetv2.tv.ui.dialogs.twopane.TwoPaneSettingsView;

/**
 * MOD: two-pane settings — left column category row.
 */
public class TwoPaneCategoryPresenter extends Presenter {
    public interface SelectedProvider {
        boolean isSelected(OptionCategory category);
    }

    private final int mDefaultTextColor;
    private final int mFocusedTextColor;
    private final int mSelectedTextColor;
    private final TwoPaneSettingsView.Listener mListener;
    private final SelectedProvider mSelectedProvider;

    public TwoPaneCategoryPresenter(Context context, TwoPaneSettingsView.Listener listener,
                                    SelectedProvider selectedProvider) {
        mDefaultTextColor = resolveThemeColor(context, R.attr.ttTwoPaneTextColor,
                ContextCompat.getColor(context, R.color.card_default_text));
        mFocusedTextColor = resolveThemeColor(context, R.attr.ttTwoPaneTextFocusedColor, Color.BLACK);
        mSelectedTextColor = resolveThemeColor(context, R.attr.ttTwoPaneTextSelectedColor, Color.WHITE);
        mListener = listener;
        mSelectedProvider = selectedProvider;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.two_pane_category_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        TextView title = (TextView) viewHolder.view;

        OptionCategory category = (OptionCategory) item;

        title.setText(TwoPaneSettingsView.getCategoryTitle(category));

        boolean selected = mSelectedProvider != null && mSelectedProvider.isSelected(category);
        viewHolder.view.setSelected(selected);

        viewHolder.view.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                title.setTextColor(mFocusedTextColor);

                if (mListener != null) {
                    mListener.onItem(category, false);
                }
            } else {
                title.setTextColor(v.isSelected() ? mSelectedTextColor : mDefaultTextColor);
            }
        });

        viewHolder.view.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItem(category, true);
            }
        });

        title.setTextColor(viewHolder.view.isFocused() ? mFocusedTextColor
                : selected ? mSelectedTextColor : mDefaultTextColor);
    }

    private static int resolveThemeColor(Context context, int attr, int fallback) {
        TypedValue value = new TypedValue();

        if (context.getTheme().resolveAttribute(attr, value, true)) {
            return value.data;
        }

        return fallback;
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        // NOP
    }
}

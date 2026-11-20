package com.liskovsoft.smartyoutubetv2.tv.presenter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.Presenter;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionItem;
import com.liskovsoft.smartyoutubetv2.tv.R;
import com.liskovsoft.smartyoutubetv2.tv.ui.dialogs.twopane.TwoPaneSettingsView;

/**
 * MOD: two-pane settings — right column option row.
 */
public class TwoPaneOptionPresenter extends Presenter {
    private final int mDefaultTextColor;
    private final int mFocusedTextColor;
    private final int mMetaTextColor;
    private final int mMetaFocusedTextColor;
    private final TwoPaneSettingsView.Listener mListener;

    public TwoPaneOptionPresenter(Context context, TwoPaneSettingsView.Listener listener) {
        mDefaultTextColor = resolveThemeColor(context, R.attr.ttTwoPaneTextColor,
                ContextCompat.getColor(context, R.color.card_default_text));
        mFocusedTextColor = resolveThemeColor(context, R.attr.ttTwoPaneTextFocusedColor, Color.BLACK);
        mMetaTextColor = ContextCompat.getColor(context, R.color.card_meta_text);
        mMetaFocusedTextColor = resolveThemeColor(context, R.attr.ttTwoPaneTextFocusedColor, Color.BLACK);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.two_pane_option_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        View root = viewHolder.view;
        TextView title = root.findViewById(R.id.two_pane_option_title);
        TextView description = root.findViewById(R.id.two_pane_option_description);
        TextView check = root.findViewById(R.id.two_pane_option_check);

        OptionItem optionItem = (OptionItem) item;

        title.setText(optionItem.getTitle());

        CharSequence desc = optionItem.getDescription();
        if (!TextUtils.isEmpty(desc)) {
            description.setText(desc);
            description.setVisibility(View.VISIBLE);
        } else {
            description.setVisibility(View.GONE);
        }

        boolean selected = optionItem.isSelected();
        check.setVisibility(selected ? View.VISIBLE : View.GONE);

        root.setOnFocusChangeListener((v, hasFocus) -> {
            int color = hasFocus ? mFocusedTextColor : mDefaultTextColor;
            int metaColor = hasFocus ? mMetaFocusedTextColor : mMetaTextColor;
            title.setTextColor(color);
            description.setTextColor(metaColor);
            check.setTextColor(color);
        });

        root.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItem(optionItem, true);
            }
        });

        boolean focused = root.isFocused();
        title.setTextColor(focused ? mFocusedTextColor : mDefaultTextColor);
        description.setTextColor(focused ? mMetaFocusedTextColor : mMetaTextColor);
        check.setTextColor(focused ? mFocusedTextColor : mDefaultTextColor);
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

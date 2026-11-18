package com.liskovsoft.smartyoutubetv2.tv.presenter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
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
    private final int mMetaTextColor;
    private final TwoPaneSettingsView.Listener mListener;

    public TwoPaneOptionPresenter(Context context, TwoPaneSettingsView.Listener listener) {
        mDefaultTextColor = ContextCompat.getColor(context, R.color.card_default_text);
        mMetaTextColor = ContextCompat.getColor(context, R.color.card_meta_text);
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
            int color = hasFocus ? Color.BLACK : mDefaultTextColor;
            int metaColor = hasFocus ? Color.BLACK : mMetaTextColor;
            title.setTextColor(color);
            description.setTextColor(metaColor);
            check.setTextColor(color);
        });

        root.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItem(optionItem);
            }
        });

        boolean focused = root.isFocused();
        title.setTextColor(focused ? Color.BLACK : mDefaultTextColor);
        description.setTextColor(focused ? Color.BLACK : mMetaTextColor);
        check.setTextColor(focused ? Color.BLACK : mDefaultTextColor);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        // NOP
    }
}

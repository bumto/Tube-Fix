package com.liskovsoft.smartyoutubetv2.tv.presenter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.Presenter;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionCategory;
import com.liskovsoft.smartyoutubetv2.tv.R;

/**
 * MOD: two-pane settings — left column category row.
 */
public class TwoPaneCategoryPresenter extends Presenter {
    private final int mDefaultTextColor;

    public TwoPaneCategoryPresenter(Context context) {
        mDefaultTextColor = ContextCompat.getColor(context, R.color.card_default_text);
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

        title.setText(com.liskovsoft.smartyoutubetv2.tv.ui.dialogs.twopane.TwoPaneSettingsView.getCategoryTitle(category));

        viewHolder.view.setOnFocusChangeListener((v, hasFocus) -> {
            title.setTextColor(hasFocus ? Color.BLACK : mDefaultTextColor);
            title.setSelected(hasFocus);
        });
        title.setTextColor(viewHolder.view.isFocused() ? Color.BLACK : mDefaultTextColor);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        // NOP
    }
}

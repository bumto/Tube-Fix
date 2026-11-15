package com.liskovsoft.smartyoutubetv2.tv.presenter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.prefs.MainUIData;
import com.liskovsoft.smartyoutubetv2.tv.R;
import com.liskovsoft.smartyoutubetv2.tv.presenter.base.LongClickPresenter;
import com.liskovsoft.smartyoutubetv2.tv.ui.browse.video.GridFragmentHelper;

/**
 * MOD: YouTube TV style — colored collection tile (History / Playlists / etc.)
 */
public class TileCardPresenter extends LongClickPresenter {
    private static final int[] TILE_COLORS = {
            0xFF7B1FA2, // purple
            0xFF1E88E5, // blue
            0xFF00897B, // teal
            0xFFF4511E, // orange
            0xFFC49018, // mustard
            0xFFAD1457, // magenta
            0xFF2E7D32, // green
            0xFF5C6BC0  // indigo
    };

    private int mWidth;
    private int mHeight;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();

        View tileView = LayoutInflater.from(context).inflate(R.layout.tile_card, null);
        FrameLayout root = (FrameLayout) tileView;

        Pair<Integer, Integer> dimens =
                GridFragmentHelper.getCardDimensPx(context, R.dimen.card_width, R.dimen.card_height, MainUIData.instance(context).getVideoGridScale());
        mWidth = dimens.first;
        mHeight = dimens.second;

        root.setLayoutParams(new FrameLayout.LayoutParams(mWidth, mHeight));
        root.setFocusable(true);
        root.setFocusableInTouchMode(true);

        if (android.os.Build.VERSION.SDK_INT >= 23 && MainUIData.instance(context).isUiTweakEnabled(MainUIData.UI_TWEAK_ROUNDED_CORNERS)) {
            root.setForeground(ContextCompat.getDrawable(context, R.drawable.card_focus_outline));
        }

        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        super.onBindViewHolder(viewHolder, item);

        Video video = (Video) item;

        FrameLayout root = (FrameLayout) viewHolder.view;
        TextView titleText = root.findViewById(R.id.tile_title);

        String title = video.getTitle() != null ? video.getTitle() : "";
        titleText.setText(title);

        int color = TILE_COLORS[Math.abs(title.hashCode()) % TILE_COLORS.length];
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(root.getResources().getDimensionPixelSize(R.dimen.lb_rounded_rect_corner_radius));
        bg.setColor(color);
        root.setBackground(bg);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        super.onUnbindViewHolder(viewHolder);
    }
}

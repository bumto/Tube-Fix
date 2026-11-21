package com.liskovsoft.smartyoutubetv2.tv.presenter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.PageRow;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.RowHeaderPresenter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.BrowseSection;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.BrowsePresenter;
import com.liskovsoft.smartyoutubetv2.tv.R;
import com.liskovsoft.smartyoutubetv2.tv.util.ViewUtil;

public class IconHeaderItemPresenter extends RowHeaderPresenter {
    private static final String TAG = IconHeaderItemPresenter.class.getSimpleName();
    private float mUnselectedAlpha;
    private final int mResId;
    private final String mIconUrl;
    private Drawable mDefaultIcon;

    public IconHeaderItemPresenter(int resId, String iconUrl) {
        mResId = resId;
        mIconUrl = iconUrl;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup) {
        mUnselectedAlpha = viewGroup.getResources()
                .getFraction(R.fraction.lb_browse_header_unselect_alpha, 1, 1);
        LayoutInflater inflater = (LayoutInflater) viewGroup.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDefaultIcon = new ColorDrawable(ContextCompat.getColor(viewGroup.getContext(), R.color.lb_grey));

        // MOD: inflate with parent so the rail item width is honored — grid uses
        // UNSPECIFIED spec for secondary size, so match_parent would collapse to 0
        View view = inflater.inflate(R.layout.icon_header_item, viewGroup, false);
        view.setAlpha(1f);

        view.setOnFocusChangeListener((v, hasFocus) ->
                updateItemState(v, hasFocus, v.isSelected()));
        updateItemState(view, false, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        HeaderItem headerItem;

        if (item instanceof PageRow) {
            headerItem = ((PageRow) item).getHeaderItem();
        } else {
            headerItem = ((ListRow) item).getHeaderItem();
        }

        View rootView = viewHolder.view;
        rootView.setFocusable(true);

        BrowseSection currentSection = BrowsePresenter.instance(rootView.getContext()).getCurrentSection();
        boolean isCurrentSection = currentSection != null && currentSection.getId() == headerItem.getId();
        rootView.setSelected(isCurrentSection);

        ImageView iconView = rootView.findViewById(R.id.header_icon);
        if (iconView != null) {
            if (mIconUrl != null) {
                Glide.with(rootView.getContext())
                        .load(mIconUrl)
                        .apply(ViewUtil.glideOptions().error(mDefaultIcon))
                        .listener(mErrorListener)
                        .into(iconView);

                //ViewUtil.makeMonochrome(iconView);
            } else {
                Drawable icon = mResId > 0 ? ContextCompat.getDrawable(rootView.getContext(), mResId) : mDefaultIcon;
                iconView.setImageDrawable(icon);
            }
        }

        TextView label = rootView.findViewById(R.id.header_label);
        if (label != null) {
            label.setText(headerItem.getName());
        }

        updateItemState(rootView, rootView.isFocused(), isCurrentSection);
    }

    /**
     * MOD: collapsed rail shows icons only (selected icon turns white); expanded rail
     * shows the SmartTube-style pill with a dark label/icon.
     */
    private static void updateItemState(View rootView, boolean focused, boolean selected) {
        Context context = rootView.getContext();
        boolean expanded = BrowseSupportFragment.sPinnedHeadersExpanded;

        int railText = resolveThemeColor(context, R.attr.ttRailTextColor,
                ContextCompat.getColor(context, R.color.tt_rail_text));
        int railTextFocused = resolveThemeColor(context, R.attr.ttRailTextFocusedColor,
                ContextCompat.getColor(context, R.color.tt_rail_text_focused));

        int color;
        if (expanded) {
            color = focused || selected ? railTextFocused : railText;
        } else {
            color = selected ? ContextCompat.getColor(context, R.color.tt_rail_text_collapsed_selected) : railText;
        }

        ImageView icon = rootView.findViewById(R.id.header_icon);
        if (icon != null) {
            icon.setColorFilter(color);

            LinearLayout.LayoutParams iconParams = (LinearLayout.LayoutParams) icon.getLayoutParams();
            iconParams.setMarginStart(0);
            iconParams.setMarginEnd(expanded ? context.getResources().getDimensionPixelSize(R.dimen.rail_icon_margin) : 0);
            icon.setLayoutParams(iconParams);
        }

        TextView label = rootView.findViewById(R.id.header_label);
        if (label != null) {
            label.setVisibility(expanded ? View.VISIBLE : View.GONE);
            label.setTextColor(color);
        }

        LinearLayout pill = rootView.findViewById(R.id.header_pill);
        if (pill != null) {
            int padding = context.getResources().getDimensionPixelSize(R.dimen.rail_item_padding_start);

            if (expanded) {
                pill.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                pill.setPadding(padding, 0, padding, 0);
                pill.setBackgroundResource(R.drawable.header_pill_bg);
            } else {
                pill.setGravity(Gravity.CENTER);
                pill.setPadding(0, 0, 0, 0);
                pill.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    private static int resolveThemeColor(Context context, int attr, int fallback) {
        TypedValue value = new TypedValue();

        if (context.getTheme().resolveAttribute(attr, value, true)) {
            return value.data;
        }

        return fallback;
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        // NOP
    }

    // TODO: This is a temporary fix. Remove me when leanback onCreateViewHolder no longer sets the
    // mUnselectAlpha, and also assumes the xml inflation will return a RowHeaderView.
    @Override
    protected void onSelectLevelChanged(RowHeaderPresenter.ViewHolder holder) {
        holder.view.setAlpha(mUnselectedAlpha + holder.getSelectLevel() *
                (1.0f - mUnselectedAlpha));
    }

    private final RequestListener<Drawable> mErrorListener = new RequestListener<Drawable>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            Log.e(TAG, "Glide load failed: " + e);
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            return false;
        }
    };
}

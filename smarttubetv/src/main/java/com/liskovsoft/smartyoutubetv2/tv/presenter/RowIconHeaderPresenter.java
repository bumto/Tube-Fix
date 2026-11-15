package com.liskovsoft.smartyoutubetv2.tv.presenter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowHeaderPresenter;
import androidx.leanback.widget.RowHeaderView;
import com.liskovsoft.smartyoutubetv2.tv.R;
import com.liskovsoft.smartyoutubetv2.tv.ui.browse.IconHeaderItem;

/**
 * MOD: YouTube TV style — shelf header with an optional icon
 */
public class RowIconHeaderPresenter extends RowHeaderPresenter {
    public RowIconHeaderPresenter() {
        super(R.layout.row_header_item, true);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        super.onBindViewHolder(viewHolder, item);

        HeaderItem headerItem = item == null ? null : ((Row) item).getHeaderItem();

        ImageView icon = viewHolder.view.findViewById(R.id.row_header_icon);
        RowHeaderView title = viewHolder.view.findViewById(R.id.row_header);

        int res = headerItem instanceof IconHeaderItem ? ((IconHeaderItem) headerItem).getIconRes() : 0;

        if (icon != null) {
            if (res > 0) {
                icon.setImageResource(res);
                icon.setVisibility(View.VISIBLE);
            } else {
                icon.setVisibility(View.GONE);
            }
        }

        // MOD: hide whole header when there is no title and no icon
        if (title != null) {
            String name = headerItem != null ? headerItem.getName() : null;
            title.setVisibility(name != null || res > 0 ? View.VISIBLE : View.GONE);
        }
    }
}

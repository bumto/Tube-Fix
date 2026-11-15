package com.liskovsoft.smartyoutubetv2.tv.ui.widgets.search;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.liskovsoft.smartyoutubetv2.tv.R;

/**
 * MOD: YouTube TV style rounded search pill.
 * Focusable, opens the search screen on click.
 */
public class SearchPillView extends FrameLayout {
    public SearchPillView(Context context) {
        super(context);
        init(context, null);
    }

    public SearchPillView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SearchPillView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.search_pill, this, true);
        setBackgroundResource(R.drawable.search_pill_bg);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setClickable(true);
        TextView hintText = findViewById(R.id.search_pill_hint);
        hintText.setText(R.string.action_search);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // prevent dpad clicks from being consumed by children
        return super.dispatchKeyEvent(event);
    }
}

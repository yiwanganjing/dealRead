package net.bjcore.read.diaread.view.behavior;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

/**
 * Created by kevin on 17/12/18.
 */

public class ControlScrollLinearlayoutManager extends LinearLayoutManager {
    private boolean isScrollEnabled = true;

    public ControlScrollLinearlayoutManager(Context context) {
        super(context);
    }

    public void setScrollEnabled(boolean flag) {
        this.isScrollEnabled = flag;
    }

    @Override
    public boolean canScrollVertically() {
        //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
        return isScrollEnabled && super.canScrollVertically();
    }
}

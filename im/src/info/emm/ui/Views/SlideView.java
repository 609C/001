/*
 * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.ui.Views;

import info.emm.messenger.TLRPC;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class SlideView extends LinearLayout {
    public static interface SlideViewDelegate {
        public abstract void onNextAction();
        public abstract void needShowAlert(String text);
        public abstract void needShowProgress();
        public abstract void needHideProgress();
        public abstract void setPage(int page, boolean animated, Bundle params, boolean back);
        public abstract void needFinishActivity();
        public abstract void setPageTranslate(int page);
        
        public abstract void setSelected(TLRPC.User user, boolean bAdd);
        public abstract void setBackParams(Bundle params);
    }

    public SlideView(Context context) {
        super(context);
    }

    public SlideView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlideView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SlideViewDelegate delegate;

    public String getHeaderName() {
        return "";
    }

    public void onNextPressed() {

    }

    public void setParams(Bundle params, boolean back) {

    }

    public void onBackPressed() {

    }

    public void onShow() {

    }

    public void onDestroyActivity() {
        delegate = null;
    }
}

/*
 * This is the source code of Emm for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package info.emm.messenger;

import info.emm.ui.ApplicationLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class ScreenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            FileLog.e("emm", "screen off");
            ApplicationLoader.isScreenOn = false;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            FileLog.e("emm", "screen on");
            ApplicationLoader.isScreenOn = true;
        }
    }
}

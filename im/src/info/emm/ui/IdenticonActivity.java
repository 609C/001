/*
 * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.ui;

import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.TLRPC;
import info.emm.ui.Views.BaseFragment;
import info.emm.ui.Views.IdenticonView;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IdenticonActivity extends BaseFragment {
    private int chat_id;

    @Override
    public boolean onFragmentCreate() {
        chat_id = getArguments().getInt("chat_id");
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.identicon_layout, container, false);
            IdenticonView identiconView = (IdenticonView) fragmentView.findViewById(R.id.identicon_view);
            TextView textView = (TextView)fragmentView.findViewById(R.id.identicon_text);
            TLRPC.EncryptedChat encryptedChat = MessagesController.getInstance().encryptedChats.get(chat_id);
            if (encryptedChat != null) {
                identiconView.setBytes(encryptedChat.auth_key);
                TLRPC.User user = MessagesController.getInstance().users.get(encryptedChat.user_id);
                textView.setText(Html.fromHtml(LocaleController.formatString("EncryptionKeyDescription", R.string.EncryptionKeyDescription, user.first_name, user.first_name)));
            }
        } else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        return fragmentView;
    }

    @Override
    public void applySelfActionBar() {
        if (parentActivity == null) {
            return;
        }
        ActionBar actionBar =  super.applySelfActionBar(true);
        actionBar.setTitle(LocaleController.getString("EncryptionKey", R.string.EncryptionKey));

        TextView title = (TextView)parentActivity.findViewById(R.id.action_bar_title);
        if (title == null) {
            final int subtitleId = parentActivity.getResources().getIdentifier("action_bar_title", "id", "android");
            title = (TextView)parentActivity.findViewById(subtitleId);
        }
        if (title != null) {
            title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white, 0, 0, 0);
            title.setCompoundDrawablePadding(Utilities.dp(4));
        }
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFinish) {
            return;
        }
        if (getActivity() == null) {
            return;
        }
        ((LaunchActivity)parentActivity).showActionBar();
        ((LaunchActivity)parentActivity).updateActionBar();
        fixLayout();
    }

    private void fixLayout() {
        final View v = getView();
        if (v != null) {
            ViewTreeObserver obs = v.getViewTreeObserver();
            obs.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    LinearLayout layout = (LinearLayout)fragmentView;
                    WindowManager manager = (WindowManager)parentActivity.getSystemService(Context.WINDOW_SERVICE);
                    int rotation = manager.getDefaultDisplay().getRotation();

                    if (rotation == Surface.ROTATION_270 || rotation == Surface.ROTATION_90) {
                        layout.setOrientation(LinearLayout.HORIZONTAL);
                    } else {
                        layout.setOrientation(LinearLayout.VERTICAL);
                    }

                    v.setPadding(v.getPaddingLeft(), 0, v.getPaddingRight(), v.getPaddingBottom());
                    v.getViewTreeObserver().removeOnPreDrawListener(this);

                    TextView title = (TextView)parentActivity.findViewById(R.id.action_bar_title);
                    if (title == null) {
                        final int subtitleId = ApplicationLoader.applicationContext.getResources().getIdentifier("action_bar_title", "id", "android");
                        title = (TextView)parentActivity.findViewById(subtitleId);
                    }
                    if (title != null) {
                        title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white, 0, 0, 0);
                        title.setCompoundDrawablePadding(Utilities.dp(4));
                    }

                    return false;
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finishFragment();
                break;
        }
        return true;
    }
}

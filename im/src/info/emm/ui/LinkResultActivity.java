/**
 * @Title        : LinkResultActivity.java
 *
 * @Package      : info.emm.ui
 *
 * @Copyright    : Copyright - All Rights Reserved.
 *
 * @Author       : He,Zhen hezhen@yunboxin.com
 *
 * @Date         : 2014-7-1
 *
 * @Version      : V1.00
 * 
 * @discription  �󶨻�������ʾ���档
 */
package info.emm.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.ui.Views.BaseFragment;
import info.emm.yuanchengcloudb.R;

public class LinkResultActivity extends BaseFragment implements
NotificationCenter.NotificationCenterDelegate {
	
	boolean isLink = false;
	
	@Override
	public void didReceivedNotification(int id, Object... args) {
	}
	@Override
	public boolean onFragmentCreate() {
		super.onFragmentCreate();
		isLink = getArguments().getBoolean("isLink");
		return true;
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (fragmentView == null) {
			fragmentView = inflater.inflate(R.layout.settings_link_result,
					container, false);
			ImageView iv = (ImageView)fragmentView.findViewById(R.id.iv_icon);
			TextView tvInfo = (TextView)fragmentView.findViewById(R.id.tv_detail);
			TextView tvDone = (TextView)fragmentView.findViewById(R.id.tv_done);
			iv.setImageResource(isLink?R.drawable.mobile_binded_icon:R.drawable.mobile_unbind_icon);
			tvInfo.setText(isLink?R.string.BindResult:R.string.UnbindResult);
			tvDone.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
//					((LaunchActivity)parentActivity).presentFragment(new AccountActivity(), "settings_account", false);
					NotificationCenter.getInstance().postNotificationName(MessagesController.contactsDidLoaded);
					fragmentName = AccountActivity.class;
					finishFragment();
				}
			});
		}else {
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
	        actionBar.setTitle(R.string.Done);

	        TextView title = (TextView)parentActivity.findViewById(R.id.action_bar_title);
	        if (title == null) {
	            final int subtitleId = parentActivity.getResources().getIdentifier("action_bar_title", "id", "android");
	            title = (TextView)parentActivity.findViewById(subtitleId);
	        }
	        if (title != null) {
	            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
	            title.setCompoundDrawablePadding(0);
	        }
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

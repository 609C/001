/**
 * @Title        : PhoneLinkActivity.java
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
 */
package info.emm.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.StringUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

public class PhoneLinkActivity extends BaseFragment implements
		NotificationCenter.NotificationCenterDelegate {
	private boolean isLinked = true; // true�Ѿ���
	private String phoneNum;

	@Override
	public void didReceivedNotification(int id, Object... args) {
		if (id == MessagesController.unbind_account_success) {
			Utilities.showToast(getActivity(), LocaleController.getString("",
					R.string.unbindAccountSuccess));
			finishFragment();
		} else if (id == MessagesController.unbind_account_failed) {
			Utilities
					.showToast(getActivity(), LocaleController.getString("",
							R.string.unbindAccountError));
		}
	}

	@Override
	public boolean onFragmentCreate() {
		super.onFragmentCreate();
		phoneNum = getArguments().getString("phoneNum");
		if (StringUtil.isEmpty(phoneNum)) {
			isLinked = false;
		} else {
			isLinked = true;
			if (!phoneNum.startsWith("+"))
				phoneNum = "+" + phoneNum;
		}

		NotificationCenter.getInstance().addObserver(this,
				MessagesController.unbind_account_success);
		NotificationCenter.getInstance().addObserver(this,
				MessagesController.unbind_account_failed);
		return true;
	}

	@Override
	public void onFragmentDestroy() {
		super.onFragmentDestroy();
		NotificationCenter.getInstance().removeObserver(this,
				MessagesController.unbind_account_success);
		NotificationCenter.getInstance().removeObserver(this,
				MessagesController.unbind_account_failed);
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
			fragmentView = inflater.inflate(R.layout.settings_phonelink,
					container, false);
			viewSet(fragmentView);
		} else {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		return fragmentView;
	}

	private void viewSet(View view) {
		ImageView iv = (ImageView) view.findViewById(R.id.iv_icon);
		TextView notice = (TextView) view.findViewById(R.id.tv_notice);
		TextView detail = (TextView) view.findViewById(R.id.tv_detail);
		TextView contact = (TextView) view.findViewById(R.id.tv_contact);
		TextView changePhone = (TextView) view.findViewById(R.id.tv_change);
		TextView linkPhone = (TextView) view.findViewById(R.id.tv_link);
		if (isLinked) {
			iv.setImageResource(R.drawable.mobile_binded_icon);
			notice.setTextColor(Color.GRAY);
			notice.setText(R.string.PhoneLinkNotic1);
			detail.setText(LocaleController.getString("",
					R.string.PhoneLinkNotic2) + phoneNum);
			linkPhone.setVisibility(View.GONE);
		} else {
			iv.setImageResource(R.drawable.mobile_unbind_icon);
			notice.setTextColor(Color.BLACK);
			notice.setText(R.string.PhoneUnlinkNotic1);
			detail.setText(R.string.PhoneUnlinkNotic2);
			contact.setVisibility(View.GONE);
			changePhone.setVisibility(View.GONE);
		}

		contact.setOnClickListener(onClickListener);
		changePhone.setOnClickListener(onClickListener);
		linkPhone.setOnClickListener(onClickListener);
	}

	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int id = v.getId();
			Bundle args = new Bundle();
			if (id == R.id.tv_contact) {
				LookContactsActivity fragment = new LookContactsActivity();
				// fragment.setArguments(args);
				((LaunchActivity) parentActivity).presentFragment(fragment, "",
						false);

			} else if (id == R.id.tv_change) {/*if (MessagesController.getInstance().accounts.size() < 2) {
					new AlertDialog.Builder(parentActivity)
							.setTitle(R.string.Alert)
							.setMessage(R.string.unlink_notice)
							.setPositiveButton(R.string.OK,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
										}
									}).show();
				} else {
					unLinkDialog();
				}*/

				// ChangePhoneLinkActivity fragment1 = new
				// ChangePhoneLinkActivity();
				// // fragment1.setArguments(args);
				// ((LaunchActivity)parentActivity).presentFragment(fragment1,
				// "", false);

			} else if (id == R.id.tv_link) {
				ChangePhoneLinkActivity fragment2 = new ChangePhoneLinkActivity();
				// fragment2.setArguments(args);
				((LaunchActivity) parentActivity).presentFragment(fragment2,
						"", false);

			}
		}
	};

	@Override
	public void applySelfActionBar() {
		if (parentActivity == null) {
			return;
		}
		  ActionBar actionBar =  super.applySelfActionBar(true);
		actionBar.setTitle(R.string.PhoneLink);

		TextView title = (TextView) parentActivity
				.findViewById(R.id.action_bar_title);
		if (title == null) {
			final int subtitleId = parentActivity.getResources().getIdentifier(
					"action_bar_title", "id", "android");
			title = (TextView) parentActivity.findViewById(subtitleId);
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
		((LaunchActivity) parentActivity).showActionBar();
		((LaunchActivity) parentActivity).updateActionBar();
	}

	private void unLinkDialog() {
		new AlertDialog.Builder(parentActivity)
				.setTitle(R.string.Alert)
				.setMessage(R.string.UnlinkPhone)
				.setPositiveButton(R.string.Unlink,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								MessagesController.getInstance().bindAccount(
										phoneNum, 1, "", 0);
								dialog.dismiss();
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}

						}).show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			finishFragment();
			break;
		case 1:
			unLinkDialog();
			break;
		}
		return true;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		// inflater.inflate(android.R.menu, menu);
		// if(!isLinked){
		// return;
		// }
		// String unlinkPhone = ApplicationLoader.applicationContext
		// .getString(R.string.UnlinkPhone);
		// String Tools = ApplicationLoader.applicationContext
		// .getString(R.string.Tools);
		//
		// SubMenu addMenu = menu.addSubMenu(Tools);
		// addMenu.add(0, 1, 0, unlinkPhone);
		// SupportMenuItem addItem = (SupportMenuItem) addMenu.getItem();
		// addItem.setIcon(R.drawable.ic_ab_other);
		// addItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	}

}

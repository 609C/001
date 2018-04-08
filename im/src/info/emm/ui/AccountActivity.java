/**
 * @Title        : AccountActivity.java
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

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import info.emm.messenger.LocaleController;
import info.emm.messenger.NotificationCenter;
import info.emm.ui.EmailLinkActivity.EmailType;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.StringUtil;
import info.emm.yuanchengcloudb.R;

public class AccountActivity extends BaseFragment implements
		NotificationCenter.NotificationCenterDelegate {
	private ListView listView;

	private ListAdapter listAdapter;

	private int rowCount;

	private int accountRow;

	private ArrayList<String> emailList;

	private int newEmailRow;

	@Override
	public boolean onFragmentCreate() {
		super.onFragmentCreate();
		//emailList = MessagesController.getInstance().accounts;
		reSetRows();
		return true;
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
			fragmentView = inflater.inflate(R.layout.settings_layout,
					container, false);
			listAdapter = new ListAdapter(parentActivity);
			listView = (ListView) fragmentView.findViewById(R.id.listView);
			listView.setAdapter(listAdapter);
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					if (position == newEmailRow) {
						EmailLinkActivity fragment = new EmailLinkActivity();
						Bundle args = new Bundle();
						args.putInt("emailType", EmailType.AddEmail.ordinal());
						fragment.setArguments(args);
						((LaunchActivity) parentActivity).presentFragment(
								fragment, "", false);
					} else {
						String str = emailList.get(0);
						if (position == 1
								&& !StringUtil.isEmail(str)) {
							PhoneLinkActivity fragment = new PhoneLinkActivity();
							Bundle args = new Bundle();
							args.putString("phoneNum", str);
							fragment.setArguments(args);
							((LaunchActivity) parentActivity).presentFragment(
									fragment, "", false);
						} else {
							EmailLinkActivity fragment = new EmailLinkActivity();
							Bundle args = new Bundle();
							int index = position - 1;
							args.putString("email", emailList.get(index));
							args.putInt("emailType",
									EmailType.UnlinkEmail.ordinal());
							fragment.setArguments(args);
							((LaunchActivity) parentActivity).presentFragment(
									fragment, "", false);
						}
					}
				}
			});
		} else {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		return fragmentView;
	}

	private void reSetRows() {
		rowCount = 0;
		accountRow = rowCount++;
		rowCount += emailList.size();
		newEmailRow = rowCount++;
	}

	@Override
	public void applySelfActionBar() {
		if (parentActivity == null) {
			return;
		}
		super.applySelfActionBar(true);
		ActionBar actionBar = parentActivity.getSupportActionBar();
		actionBar.setTitle(R.string.myaccount);

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
		if (listAdapter != null) 
		{
			//FileLog.e("emm", "refresh accounts"+MessagesController.getInstance().accounts);
			reSetRows();
			listAdapter.notifyDataSetChanged();
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

	@Override
	public void didReceivedNotification(int id, Object... args) {
			
	}

	private class ListAdapter extends BaseAdapter {
		private Context mContext;

		public ListAdapter(Context context) {
			mContext = context;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int i) {
			return i != accountRow;
		}

		@Override
		public int getCount() {
			return rowCount;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getItemViewType(int i) {
			if (i == accountRow) {
				return 0;
			} else {
				return 1;
			}
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			int type = getItemViewType(i);
			if (type == 0) {
				if (view == null) {
					LayoutInflater li = (LayoutInflater) mContext
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					view = li.inflate(R.layout.settings_section_layout,
							viewGroup, false);
				}
				TextView textView = (TextView) view
						.findViewById(R.id.settings_section_text);
				textView.setText(R.string.Account);
			}
			if (type == 1) {
				if (view == null) {
					LayoutInflater li = (LayoutInflater) mContext
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					view = li.inflate(
							R.layout.user_profile_leftright_row_layout,
							viewGroup, false);
				}
				TextView textView = (TextView) view
						.findViewById(R.id.settings_row_text);
				TextView detailTextView = (TextView) view
						.findViewById(R.id.settings_row_text_detail);
				View divider = view.findViewById(R.id.settings_row_divider);
				detailTextView.setTextColor(Color.GRAY);

				if (i == newEmailRow) 
				{
					textView.setText(R.string.Email);
					detailTextView.setText("");
					detailTextView.setCompoundDrawablesWithIntrinsicBounds(
							R.drawable.email_addicon, 0, 0, 0);
				} else {
					int index = i - 1;
					String str = emailList.get(index);
					if (index == 0) {
						textView.setText(R.string.PhoneNum);
						detailTextView
								.setText(StringUtil.isEmpty(str) ? LocaleController
										.getString("Unknown", R.string.Unknown)
										: str);
					} else if (StringUtil.isEmail(str)) {
						textView.setText(R.string.Email);
						detailTextView.setCompoundDrawablesWithIntrinsicBounds(
								0, 0, 0, 0);
						detailTextView.setText(R.string.Verified);
					}
				}
			} else if (type == 2) {

			}

			return view;
		}
	}
}

/**
 * @Title        : LookContactsActivity.java
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
import info.emm.LocalData.DataAdapter;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.ui.Views.BaseFragment;
import info.emm.yuanchengcloudb.R;

public class LookContactsActivity extends BaseFragment implements
		NotificationCenter.NotificationCenterDelegate {
	private ListView listView;

	private ListAdapter listAdapter;
	
	private ArrayList<DataAdapter> arrayDataAdapter = new ArrayList<DataAdapter>();

	@Override
	public boolean onFragmentCreate() {
		super.onFragmentCreate();
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		MessagesController.getInstance().getContact(arrayDataAdapter);
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

	@Override
	public void applySelfActionBar() {
		if (parentActivity == null) {
			return;
		}
		  ActionBar actionBar =  super.applySelfActionBar(true);
		actionBar.setTitle(R.string.MobileContacts);

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
		if (listAdapter != null) {
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
		public int getCount() {
			return arrayDataAdapter==null?0:arrayDataAdapter.size();
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
		public View getView(int i, View view, ViewGroup viewGroup) { 
			if(view == null){
				view = LayoutInflater.from(mContext).inflate(R.layout.item_contact, null);  
				TextView textV = (TextView)view.findViewById(R.id.tv_name);
				DataAdapter data = arrayDataAdapter.get(i);
				textV.setText(data.dataName);
			}
			 
			return view;
		}
	}
}

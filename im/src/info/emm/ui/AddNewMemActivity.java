/**
 * @Title        : AddNewMemActivity.java
 *
 * @Package      : info.emm.ui
 *
 * @Copyright    : Copyright - All Rights Reserved.
 *
 * @Author       : He,Zhen hezhen@yunboxin.com
 *
 * @Date         : 2014-6-24
 *
 * @Version      : V1.00
 */
package info.emm.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.TLRPC;
import info.emm.messenger.TLRPC.TL_PendingCompanyInfo;
import info.emm.messenger.UserConfig;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

public class AddNewMemActivity extends BaseFragment implements
		NotificationCenter.NotificationCenterDelegate {
	private ListView listView;

	private ListAdapter listViewAdapter;
	
	public ArrayList<TLRPC.TL_PendingCompanyInfo> invitedCompanys;
	
	@Override
	public boolean onFragmentCreate() {
		 NotificationCenter.getInstance().addObserver(this, MessagesController.pending_company_added);
		return true;
	}
	@Override
	public void onFragmentDestroy() {
		NotificationCenter.getInstance().removeObserver(this, MessagesController.pending_company_added);
		super.onFragmentDestroy();
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		invitedCompanys = MessagesController.getInstance().invitedCompanys;
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (fragmentView == null) {
			fragmentView = inflater.inflate(R.layout.chat_profile_layout,
					container, false);
		}
		
		listView = (ListView) fragmentView.findViewById(R.id.listView);
		
		listView.setDivider(getResources().getDrawable(R.color.listitem_gray));
		
		listView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		listViewAdapter = new ListAdapter(parentActivity);
		listView.setAdapter(listViewAdapter);
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				  AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
                  CharSequence[] items = new CharSequence[] {LocaleController.getString("KickFromGroup", R.string.Delete)};
                  final TL_PendingCompanyInfo pendCompany = invitedCompanys.get(position);
                  builder.setItems(items, new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialogInterface, int i) {
                    	  MessagesController.getInstance().DeleteInviteCompany(pendCompany.id); 
                    	  listViewAdapter.notifyDataSetChanged();
                      }
                  });
                  builder.show().setCanceledOnTouchOutside(true);
				return true;
			}
		});
		//���ö���־
		MessagesController.getInstance().readInviteCompany();
		NotificationCenter.getInstance().postNotificationName(MessagesController.pending_company_loaded);
		return fragmentView;
	}

	@Override
	public void applySelfActionBar() {
		if (parentActivity == null) {
			return;
		}
		super.applySelfActionBar(true);
		ActionBar actionBar = parentActivity.getSupportActionBar();
		actionBar.setTitle(R.string.NewInvite);

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
		((LaunchActivity) parentActivity).fixBackButton();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (getActivity() == null) {
			return;
		}
		if (listViewAdapter != null) {
			listViewAdapter.notifyDataSetChanged();
		}
		((LaunchActivity) parentActivity).showActionBar();
		((LaunchActivity) parentActivity).updateActionBar();
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

//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		inflater.inflate(R.menu.group_profile_menu, menu);
//		SupportMenuItem doneItem = (SupportMenuItem) menu
//				.findItem(R.id.block_user);
//		TextView doneTextView = (TextView) doneItem.getActionView()
//				.findViewById(R.id.done_button);
//		doneTextView.setText(R.string.AddFriend);
//		doneTextView.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//			}
//		});
//	}

	@Override
	public void didReceivedNotification(int id, Object... args) 
	{
		if (listViewAdapter != null) 
		{
			listViewAdapter.notifyDataSetChanged();
		}
		Utilities.showToast(parentActivity, getResources().getString(R.string.SuccedNotic));
	}

	private class ListAdapter extends BaseAdapter {
		private Context mContext;

		private Map<Integer, View> viewMap = new HashMap<Integer, View>();

		public ListAdapter(Context context) {
			mContext = context;
		}

		@Override
		public int getCount() {
			return invitedCompanys.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {			
			ViewHolder holder;
			View rowView = viewMap.get(position);
			if (rowView == null) {
				holder = new ViewHolder();
				rowView = LayoutInflater.from(mContext).inflate(
						R.layout.item_new_friend, null);
				holder.userIcon = (ImageView)rowView.findViewById(R.id.iv_head);
				holder.userName = (TextView)rowView.findViewById(R.id.tv_newfriend);
				holder.accpet = (TextView)rowView.findViewById(R.id.tv_accept);
				holder.added = (TextView)rowView.findViewById(R.id.tv_added);
				holder.info = (TextView)rowView.findViewById(R.id.tv_info);
				Rect bounds = new Rect();
				Paint textPaint = holder.accpet.getPaint();
				String str = getResources().getString(R.string.Added)+"++";
				textPaint.getTextBounds(str,0,str.length(),bounds);
				int width = bounds.width();
				holder.accpet.setLayoutParams(new LinearLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT));
			} else {
				holder = (ViewHolder) rowView.getTag();
			}
			
			TL_PendingCompanyInfo pendCompany = invitedCompanys.get(position);
			holder.userName.setText(pendCompany.inviteName);
			String text = getResources().getString(R.string.RequestForAdd)+ pendCompany.name;
			holder.info.setText(text);
			holder.companyid = pendCompany.id;
			String sTip = LocaleController.getString("AddFriend", R.string.AddFriend);
			if(pendCompany.bAccept)
			{
				sTip = LocaleController.getString("AddFriended", R.string.AddFriended);
				holder.accpet.setText(sTip);
				holder.accpet.setOnClickListener(null);
			}
			else
			{
				holder.accpet.setText(sTip);
				final int companyId = holder.companyid;
				holder.accpet.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						TLRPC.TL_CompanyInfo req = new TLRPC.TL_CompanyInfo();
		            	req.act = 6;//��ʾ�����������
		        		req.companyid = companyId;		        		
		        		req.createrid = UserConfig.clientUserId;
		        		MessagesController.getInstance().ControlCompany(req);
					}
				});
			}
			rowView.setTag(holder);
			viewMap.put(position, rowView);
			return rowView;
		}

		public class ViewHolder {
			public boolean isAdded = false; 
			public ImageView userIcon;
			public TextView userName;
			public TextView info;
			public TextView accpet;
			public TextView added;
			public int companyid;
		}
	}
}

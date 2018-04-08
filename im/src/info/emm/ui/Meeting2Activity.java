/**
 * @Title        : Meeting2Activity.java
 *
 * @Package      : info.emm.ui
 *
 * @Copyright    : Copyright - All Rights Reserved.
 *
 * @Author       : He,Zhen hezhen@yunboxin.com
 *
 * @Date         : 2014-11-11
 *
 * @Version      : V1.00
 */
package info.emm.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;
import info.emm.LocalData.DateUnit;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.MessagesStorage;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.TLRPC;
import info.emm.messenger.NotificationCenter.NotificationCenterDelegate;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.StringUtil;
import info.emm.yuanchengcloudb.R;

public class Meeting2Activity extends BaseFragment implements NotificationCenterDelegate {
//	private LinearLayout empty_layout_view;
	private ListView meeting_list_viewListView;
	private MettingListAdapter meeting_list_adapter;
	
	private BaseAdapter searchListViewAdapter;
	@Override
	public boolean onFragmentCreate() {
		super.onFragmentCreate();
		 NotificationCenter.getInstance().addObserver(this, MessagesController.PSTNControl_Notify);
		 NotificationCenter.getInstance().addObserver(this, MessagesController.meeting_list_delete);
		return true;
	}
	@Override
	public void onFragmentDestroy() {
		super.onFragmentDestroy();
		NotificationCenter.getInstance().removeObserver(this, MessagesController.PSTNControl_Notify);
		NotificationCenter.getInstance().removeObserver(this, MessagesController.meeting_list_delete);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) { 
		if (fragmentView == null) 
		{
			fragmentView = inflater.inflate(R.layout.meeting_list_layout, container, false);
//			empty_text_view = (TextView) fragmentView.findViewById(R.id.meetting_list_searchEmptyView);
//			empty_layout_view = (LinearLayout)fragmentView.findViewById(R.id.layout_meetting_notic);
			meeting_list_viewListView = (ListView) fragmentView.findViewById(R.id.meeting_list_view);
//			meeting_list_viewListView.setEmptyView(empty_layout_view);
			meeting_list_adapter = new MettingListAdapter(parentActivity);
			meeting_list_viewListView.setAdapter(meeting_list_adapter);
			meeting_list_viewListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					
					BaseFragment fragment = new Meeting2AddActivity();
					TLRPC.TL_PSTNMeeting meeting = getMeetingInfo(position);
					String timeString = DateUnit.getMMddFormat1(meeting.meettime);
					
					Bundle bundle = new Bundle();
					bundle.putString("meetingID", meeting.conferenceId);
					bundle.putString("startTime", timeString);
					bundle.putBoolean("isFromMeetingDetail", true);//��֪��ȡ�������������
					fragment.setArguments(bundle);
					((LaunchActivity) parentActivity).presentFragment(fragment, "meetingDetail" , false);
				}
			});
			meeting_list_viewListView.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> parent,
						View view, final int position, long id) {
					new AlertDialog.Builder(parentActivity)
		    		.setTitle(R.string.Tips)
		    		.setMessage(R.string.meeting_dialog_delete)
		    		.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) 
						{	
							MessagesStorage.getInstance().deleteMeeting2(getMeetingInfo(position).conferenceId);
							NotificationCenter.getInstance().postNotificationName(MessagesController.meeting_list_delete,getMeetingInfo(position).conferenceId);
							dialog.dismiss();
						}
					})
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
					.show();
					// TODO Auto-generated method stub
					return false;
				}
			});
			fragmentView.findViewById(R.id.tv_create_meeting).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(MessagesController.getInstance().companys.size()>0)
					{
						((LaunchActivity) parentActivity).createNewMeeting();
					}
				}
			});
//			searchListViewAdapter = new SearchAdapter(parentActivity);
		} else {
			ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
		}
		return fragmentView;
	}
	@Override
	public void onResume() {
		super.onResume();
		if (meeting_list_adapter != null) {
			//FileLog.e("emm", "pstn notify" );
			meeting_list_adapter.notifyDataSetChanged();				
		}
	}
	
	/**
	 * ��ʼ��Actionbar �е�title��BackView
	 */
	@Override
	public void applySelfActionBar() {
		if (parentActivity == null) {
			return;
		}
		
		  ActionBar actionBar =  super.applySelfActionBar(false);
        actionBar.setTitle(LocaleController.getString("AppName", R.string.AppName));
        
        TextView title = (TextView)parentActivity.findViewById(R.id.action_bar_title);
        if (title == null) {
            final int subtitleId = parentActivity.getResources().getIdentifier("action_bar_title", "id", "android");
            title = (TextView)parentActivity.findViewById(subtitleId);
        }
        if (title != null) {
            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            title.setCompoundDrawablePadding(0);
        }

        ((LaunchActivity)parentActivity).fixBackButton();
	}

	private TLRPC.TL_PSTNMeeting getMeetingInfo(int position) {
		return MessagesController.getInstance().meeting2List.get(position);
	}
	@Override
	public void didReceivedNotification(int id, Object... args) {
		if (id == MessagesController.PSTNControl_Notify) {
//			int size= MessagesController.getInstance().meeting2List.size();
//			if (size > 0) {
//				meeting_list_viewListView.setEmptyView(null);
//			}else {
//				meeting_list_viewListView.setEmptyView(empty_layout_view);
//			}
		}else if (id == MessagesController.meeting_list_delete) { //ɾ�������¼��Ϣ
			String conferenceID = (String)args[0];
			TLRPC.TL_PSTNMeeting pstnMeeting = MessagesController.getInstance().meeting2Map.get(conferenceID);
			MessagesController.getInstance().meeting2List.remove(pstnMeeting);
			MessagesController.getInstance().meeting2Map.remove(conferenceID);
			
		}
		if (meeting_list_adapter != null) {
			//FileLog.e("emm", "pstn notify" );
			meeting_list_adapter.notifyDataSetChanged();				
		}
	}
	/**
	 * @author Administrator
	 * �����б��ViewAdapter
	 */
	public class MettingListAdapter extends BaseAdapter{
		public MettingListAdapter() {
		}
		private Context mContext;

		public MettingListAdapter(Context context) {
			mContext = context;
		}
		@Override
		public int getCount() {
			int size= MessagesController.getInstance().meeting2List.size();
			//FileLog.e("emm", "pstn count"+size);
			return size;
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public boolean isEnabled(int i) {
			return true;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup arg2) 
		{
			//FileLog.e("emm", "get pstn view"+position);
			TLRPC.TL_PSTNMeeting pstnMeeting = getMeetingInfo(position);
			ViewHolder holder= null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.meeting_list_item , null);
				holder.meeting_name = (TextView)convertView.findViewById(R.id.meeting_name);
				holder.meeting_time = (TextView) convertView.findViewById(R.id.meeting_starttime);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();				
			}
			String meetingName = pstnMeeting.meettitle;
			String timeString = DateUnit.getMMddFormat1(pstnMeeting.meettime);
			
//			String meetingId = "*****" + pstnMeeting.conferenceId;
//			meetingId = "***" + meetingId.substring(meetingId.length()-5, meetingId.length());
			holder.meeting_name.setText(StringUtil.isEmpty(meetingName)?StringUtil.getStringFromRes(R.string.Meeting):meetingName);
			String startTime = String.format(StringUtil.getStringFromRes(R.string.meeting_start_time), timeString);
			holder.meeting_time.setText(startTime);
			return convertView;
		}
		@Override
		public Object getItem(int position) {
			return null;//getMeetingInfo(position);
		}
	}
	class ViewHolder{
		private TextView meeting_name;
		private TextView meeting_time;
		private TextView meeting_new_msg;
	}
}

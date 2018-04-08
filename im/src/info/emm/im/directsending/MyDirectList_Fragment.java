package info.emm.im.directsending;

import java.util.ArrayList;

import com.meeting.ui.RefreshableView;
import com.meeting.ui.RefreshableView.PullToRefreshListener;
import com.utils.Utitlties;
import com.utils.WeiyiMeeting;
import com.utils.WeiyiMeetingNotificationCenter;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import info.emm.LocalData.Config;
import info.emm.LocalData.DateUnit;
import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.MessagesController;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.LaunchActivity;
import info.emm.ui.Views.BaseFragment;
import info.emm.yuanchengcloudb.R;

/**
 * �ҵ�ֱ���б�
 * @author qxm
 */
public class MyDirectList_Fragment extends BaseFragment implements OnClickListener, PullToRefreshListener,
info.emm.messenger.NotificationCenter.NotificationCenterDelegate,
WeiyiMeetingNotificationCenter.NotificationCenterDelegate{

	private RefreshableView refresh_view;
	private ListView myDirectLv;
	private LinearLayout noListLl;
	private ImageView joinDirectIv;;
	private ImageView rangeDirectIv;
	private ArrayList<TLRPC.TL_MeetingInfo> directList ;
	String directTopicId;
	String directPwd;
	DirectAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		info.emm.messenger.NotificationCenter.getInstance().addObserver(this,MessagesController.getall_meeting);
		info.emm.messenger.NotificationCenter.getInstance().addObserver(this,MessagesController.direct_list_delete);
		if(fragmentView == null){
			fragmentView = inflater.inflate(R.layout.mydirect_list_fragment, container, false);
			refresh_view = (RefreshableView) fragmentView.findViewById(R.id.mydirect_refreshable_view);
			myDirectLv = (ListView) fragmentView.findViewById(R.id.mydirect_list_lv);
			noListLl = (LinearLayout) fragmentView.findViewById(R.id.mydirect_list_no_direct_ll);
			joinDirectIv = (ImageView) fragmentView.findViewById(R.id.mydirect_list_join_direct_iv);
			rangeDirectIv = (ImageView) fragmentView.findViewById(R.id.mydirect_list_range_direct_iv);
			joinDirectIv.setOnClickListener(this);
			rangeDirectIv.setOnClickListener(this);

			refresh_view.setOnRefreshListener(this, R.id.refreshable_view);
			directList = MessagesController.getInstance().broadcastMeetingList;
			adapter = new DirectAdapter();
			myDirectLv.setAdapter(adapter);
			adapter.notifyDataSetChanged();
			myDirectLv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					final TLRPC.TL_MeetingInfo mt = MessagesController.getInstance().broadcastMeetingList.get(arg2);					
					if(mt==null)
						return;
					DirectDetails_Fragment directFragment = new DirectDetails_Fragment();
					Bundle bundle = new Bundle();
					bundle.putString("meetingid", Integer.toString(mt.mid));					
					directFragment.setArguments(bundle);
					((LaunchActivity) getActivity()).presentFragment(directFragment, "", false);
				}
			});
		}else{
			ViewGroup group = (ViewGroup) fragmentView.getParent();
			group.removeView(fragmentView);
		}
		refreshDirectList();
		return fragmentView;
	}


	private void refreshDirectList() {
		if (MessagesController.getInstance().broadcastMeetingList.size() == 0) {
			noListLl.setVisibility(View.VISIBLE);
		} else {
			noListLl.setVisibility(View.GONE);
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	public void applySelfActionBar() {
		if (parentActivity == null) {
			return;
		}
		ActionBar actionBar =  super.applySelfActionBar(true);


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
	public void onResume() 
	{
		super.onResume();
		if (isFinish) {
			return;
		}
		((LaunchActivity) parentActivity).showActionBar();
		((LaunchActivity) parentActivity).updateActionBar();
	}

	@Override
	public void onRefresh() {
		ConnectionsManager.getInstance().getUpdate();
	}
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		info.emm.messenger.NotificationCenter.getInstance().removeObserver(this,MessagesController.getall_meeting);
		info.emm.messenger.NotificationCenter.getInstance().removeObserver(this,MessagesController.direct_list_delete);
	}

	@Override
	public void didReceivedNotification(int id, Object... args) {
		if (id == MessagesController.getall_meeting) {
			if (refresh_view.isRefreshing()) {
				refresh_view.finishRefreshing();
			}			
		}
		else if (id == MessagesController.direct_list_delete) 
		{
			int mid = (Integer) args[0];
			if (mid==-1)
			{
				Utitlties.HideProgressDialog(getActivity());
				Toast.makeText(getActivity().getApplicationContext(),
						R.string.delete_direct_faild,Toast.LENGTH_SHORT).show();
			}
			else
			{
				Toast.makeText(getActivity().getApplicationContext(),
						R.string.delete_direct_success,Toast.LENGTH_SHORT).show();

			}
		} 
		if(adapter!=null)
		{
			refreshDirectList();
		}
	}

	@Override
	public void onClick(View arg0) {
		int nId = arg0.getId();
		if(nId == R.id.mydirect_list_join_direct_iv){
			JoinDirect_Fragment m_fragjtm = new JoinDirect_Fragment();
			((LaunchActivity)getActivity()).presentFragment(m_fragjtm, "", false);

		}else if(nId ==  R.id.mydirect_list_range_direct_iv){
			RangeDirect_Fragment m_fragjtm = new RangeDirect_Fragment();
			((LaunchActivity)getActivity()).presentFragment(m_fragjtm, "", false);
		}

	}

	class DirectAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			int nSize = MessagesController.getInstance().broadcastMeetingList.size();
			return nSize;
		}

		@Override
		public Object getItem(int arg0) {
			return arg0;
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View view, ViewGroup arg2) {
			ViewHolder holder = null;
			TLRPC.TL_MeetingInfo mt = MessagesController.getInstance().broadcastMeetingList.get(arg0);
			if (mt == null)
				return view;
			if(view == null){
				holder = new ViewHolder();
				view = LayoutInflater.from(getActivity()).inflate(R.layout.weiyi_meeting_list_item, null);
				holder.itemTopicTv = (TextView) view.findViewById(R.id.Meeting_Topic);
				holder.itemStartTimeTv = (TextView) view.findViewById(R.id.Meeting_Time);
				holder.itemStartDirectTv = (TextView) view.findViewById(R.id.button_start);
				holder.itemId = (TextView) view.findViewById(R.id.Meeting_ID);
				view.setTag(holder);
			}else{
				holder = (ViewHolder) view.getTag();
			}
			int type = mt.meetingType;
			String strTime = DateUnit.getMMddFormat1(mt.startTime);
			holder.itemStartTimeTv.setText(strTime);
			holder.itemTopicTv .setText(mt.topic);
			holder.itemId.setText("ID:"+mt.mid);

			StartDirectListen sbl = new StartDirectListen();
			sbl.setMeeting(mt);
//			sbl.SetMeetingID(Integer.toString(mt.mid));
			holder.itemStartDirectTv.setOnClickListener(sbl);
			return view;
		}

		class ViewHolder{
			TextView itemTopicTv;
			TextView itemStartTimeTv;
			TextView itemStartDirectTv;
			TextView itemId;
		}
	};

	public class StartDirectListen implements OnClickListener {

//		String strdirectTopic = "";
		TLRPC.TL_MeetingInfo myDirect;
		@Override
		public void onClick(View arg0) {
//			directTopicId = strdirectTopic;
//			directPwd = null;
			joinDirect(myDirect);
		}
//		public void SetMeetingID(String strTopic) {
//			strdirectTopic = strTopic;
//		}
		public void setMeeting(TLRPC.TL_MeetingInfo info){
			myDirect = info;
		}
	}
	/**
	 * ��ʼֱ��
	 */
	private void joinDirect(TLRPC.TL_MeetingInfo myDirect) {
		Utitlties.ShowProgressDialog(MyDirectList_Fragment.this.getActivity(),
				getResources().getString(R.string.Loading));
		String Url = "";
		int usertype = 0;
		if(UserConfig.clientUserId==myDirect.createid){
			usertype = 2;
		}
		if(UserConfig.isPublic){
			String ss = Config.publicWebHttp;
			int index = ss.indexOf(6);
			String publicHttp = ss.substring(7);
			Url = "weiyi://start?ip="+publicHttp+"&port="+80+"&meetingid="+myDirect.mid+"&meetingtype="+13+"&title="+myDirect.topic+"&nickname="+UserConfig.currentUser.identification+"&createrid="+myDirect.createid+"&thirdID="+UserConfig.clientUserId;
		}else{
			if(UserConfig.privatePort == 80){
				String privateHttp = UserConfig.privateWebHttp;
				Url = "weiyi://start?ip="+privateHttp+"&port="+80+"&meetingid="+myDirect.mid+"&meetingtype="+13+"&title="+myDirect.topic+"&nickname="+UserConfig.currentUser.identification+"&createrid="+myDirect.createid+"&thirdID="+UserConfig.clientUserId;
			}else{
				String privateHttp = UserConfig.privateWebHttp;
				int privatePort = UserConfig.privatePort;
				Url = "weiyi://start?ip="+privateHttp+"&port="+privatePort+"&meetingid="+myDirect.mid+"&meetingtype="+13+"&title="+myDirect.topic+"&nickname="+UserConfig.currentUser.identification+"&createrid="+myDirect.createid+"&thirdID="+UserConfig.clientUserId;
			}
		}
		Url+="&usertype="+usertype;
		WeiyiMeeting.getInstance().joinBroadcast(getActivity(),Url);				
//		if(UserConfig.clientUserId == myDirect.createid){			
//		}else{
//		}
		Utitlties.HideProgressDialog(getActivity());
	}
}

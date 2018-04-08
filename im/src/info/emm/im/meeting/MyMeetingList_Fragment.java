package info.emm.im.meeting;


import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.meeting.ui.RefreshableView;
import com.meeting.ui.RefreshableView.PullToRefreshListener;
import com.utils.Utitlties;
import com.utils.WeiyiMeetingNotificationCenter;

import java.util.ArrayList;

import info.emm.LocalData.DateUnit;
import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.TLRPC;
import info.emm.ui.LaunchActivity;
import info.emm.ui.Views.BaseFragment;
import info.emm.weiyicloud.meeting.R;
/**
 * 我的会议
 * @author Administrator
 *
 */
public class MyMeetingList_Fragment extends BaseFragment implements
		PullToRefreshListener, info.emm.messenger.NotificationCenter.NotificationCenterDelegate,
		WeiyiMeetingNotificationCenter.NotificationCenterDelegate {

	LayoutInflater m_inflater;
	RefreshableView m_Refreshable;
	ListView m_ListView;
	BaseAdapter m_listMemberAdapter;
	String m_strCurrentMeetingPsd;
	String m_strCurrentMeetingID;
	// xiaoyang
	LinearLayout view_isempty;
	ImageView img_joinmeeting;
	
	ImageView img_rangmeeting;
	ArrayList<TLRPC.TL_MeetingInfo> meetingList ;
	

	// ///////////////////////////////////////////////////////

	public class StartBtnListen implements OnClickListener {

		String strMeetingID = "";

		@Override
		public void onClick(View arg0) {
			m_strCurrentMeetingID = strMeetingID;
			m_strCurrentMeetingPsd = null;
			JointoMeeting();
		}

		public void SetMeetingID(String strID) {
			strMeetingID = strID;
		}
	}

	// /////////////////////////////////////////////////////

	/*
	 * @Override public void onActivityCreated(Bundle savedInstanceState) {
	 * super.onActivityCreated(savedInstanceState); }
	 */

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		info.emm.messenger.NotificationCenter.getInstance().addObserver(this,MessagesController.getall_meeting);
		info.emm.messenger.NotificationCenter.getInstance().addObserver(this,MessagesController.meeting_list_delete);
		if (fragmentView == null) {
			// this.setHasOptionsMenu(true);

			m_inflater = inflater;
			fragmentView = inflater.inflate(R.layout.fragment_my_meeting, null);
			m_Refreshable = (RefreshableView) fragmentView
					.findViewById(R.id.refreshable_view);
			m_ListView = (ListView) fragmentView.findViewById(R.id.list_view);
			// xiaoyang
			view_isempty = (LinearLayout) fragmentView.findViewById(R.id.lin_no_meeting);
			img_joinmeeting = (ImageView) fragmentView.findViewById(R.id.joinmeeting);
			img_rangmeeting = (ImageView) fragmentView.findViewById(R.id.rangmeeting);
			
			img_joinmeeting.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					JointoMeeting_Fragment m_fragjtm = new JointoMeeting_Fragment();
					((LaunchActivity)getActivity()).presentFragment(m_fragjtm, "", false);
				}
			});
			img_rangmeeting.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					RangeMeeting_Fragment m_fragrm = new RangeMeeting_Fragment();
					((LaunchActivity)getActivity()).presentFragment(m_fragrm, "", false);
					
				}
			});
			m_Refreshable.setOnRefreshListener(this, R.id.refreshable_view);
			meetingList = MessagesController.getInstance().meetingList;
			m_listMemberAdapter = new BaseAdapter() {

				@Override
				public int getCount() {
					int nSize = MessagesController.getInstance().meetingList.size();
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
				public View getView(int arg0, View arg1, ViewGroup arg2) {
					View view = arg1;
					if (view == null) {
						view = m_inflater.inflate(
								R.layout.weiyi_meeting_list_item, null);
					}
				
					TLRPC.TL_MeetingInfo mt= MessagesController.getInstance().meetingList.get(arg0);
					if (mt == null)
						return view;

					TextView tvTime = (TextView) view
							.findViewById(R.id.Meeting_Time);
					TextView tvTopic = (TextView) view
							.findViewById(R.id.Meeting_Topic);
					final TextView tvId = (TextView) view
							.findViewById(R.id.Meeting_ID);
					TextView btStart = (TextView) view
							.findViewById(R.id.button_start);
					
					String strTime = DateUnit.getMMddFormat1(mt.startTime);
					
					tvTime.setText(strTime);

					tvTopic.setText(mt.topic);
				
					tvId.setText("ID:" + mt.mid);

					StartBtnListen sbl = new StartBtnListen();
				
					sbl.SetMeetingID( Integer.toString(mt.mid));
					btStart.setOnClickListener(sbl);
				
					return view;
				}
			};

			m_ListView.setAdapter(m_listMemberAdapter);

			m_ListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					final TLRPC.TL_MeetingInfo mt = MessagesController.getInstance().meetingList.get(arg2);					
					if(mt==null)
						return;
					MeetingInfoFragment infoFragment = new MeetingInfoFragment();
					Bundle bundle = new Bundle();
					bundle.putString("meetingid", Integer.toString(mt.mid));					
					infoFragment.setArguments(bundle);
					((LaunchActivity) getActivity()).presentFragment(
							infoFragment, "", false);
				}
			});

		} else {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}

		refreshMeetingList();

	
		Log.e("emm", "onCreateView");
		return fragmentView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		NotificationCenter.getInstance().removeObserver(this, MessagesController.getall_meeting);
		NotificationCenter.getInstance().removeObserver(this, MessagesController.meeting_list_delete);		
	}

	@Override
	public void onRefresh() {
		Log.e("emm", "onRefresh begin**************");
		ConnectionsManager.getInstance().getUpdate();
		//MeetingMgr.getInstance().getAllMeeting();
		Log.e("emm", "onRefresh end*******************");

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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (isFinish) {
			return;
		}
		/*if(!m_Refreshable.isRefreshing()){
			ConnectionsManager.getInstance().getUpdate();
		}*/
		((LaunchActivity) parentActivity).showActionBar();
		((LaunchActivity) parentActivity).updateActionBar();
		m_listMemberAdapter.notifyDataSetChanged();		

	}

	@Override
	public void applySelfActionBar() {

		ActionBar actionBar = super.applySelfActionBar(true);

		// ((LaunchActivity) parentActivity).fixBackButton();
		if(this.getActivity()==null)
			return;
		if (actionBar == null)
			return;

		actionBar.setTitle(getString(R.string.app_name));

		TextView title = (TextView) getActivity().findViewById(
				R.id.action_bar_title);
		if (title == null) {
			final int subtitleId = getResources().getIdentifier(
					"action_bar_title", "id", "android");
			title = (TextView) getActivity().findViewById(subtitleId);
		}
		if (title != null) {
			title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			title.setCompoundDrawablePadding(0);
		}
	}

	@Override
	public void didReceivedNotification(int id, Object... args) {
		if (id == MessagesController.getall_meeting) {
			if (m_Refreshable.isRefreshing()) {
				m_Refreshable.finishRefreshing();
			}			
		}
		else if (id == MessagesController.meeting_list_delete) 
		{
			int mid = (Integer) args[0];
			if (mid==-1)
			{
				Utitlties.HideProgressDialog(getActivity());
				Toast.makeText(getActivity().getApplicationContext(),
						R.string.delete_meeting_faild,
						Toast.LENGTH_SHORT).show();
			}
			else
			{
				Toast.makeText(getActivity().getApplicationContext(),
						R.string.delete_meeting_success,
						Toast.LENGTH_SHORT).show();
				m_listMemberAdapter.notifyDataSetChanged();	
			}
		} 
		if(m_listMemberAdapter!=null)
		{
			refreshMeetingList();
		}
	}

	private void refreshMeetingList()
	{
		if (MessagesController.getInstance().meetingList.size() == 0) {
			view_isempty.setVisibility(View.VISIBLE);
		} else {
			view_isempty.setVisibility(View.GONE);
		}
		m_listMemberAdapter.notifyDataSetChanged();
	}
	
	public void JointoMeeting() {
		Utitlties.ShowProgressDialog(getActivity(),
				getResources().getString(R.string.Loading));
	
//		ApplicationLoader.getInstance().joinMeeting(getActivity(),m_strCurrentMeetingID,m_strCurrentMeetingPsd);
	
	}
}

package info.emm.im.meeting;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.main.mme.view.PinnedHeaderExpandableListView;
import com.main.mme.view.PinnedHeaderExpandableListView.OnHeaderUpdateListener;
import com.main.mme.view.StickyLayout;
import com.main.mme.view.StickyLayout.OnGiveUpTouchEventListener;
import com.meeting.ui.RefreshableView;
import com.meeting.ui.RefreshableView.PullToRefreshListener;
import com.utils.Utitlties;
import com.utils.WeiyiMeeting;
import com.utils.WeiyiMeetingNotificationCenter;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import info.emm.LocalData.Config;
import info.emm.LocalData.DateUnit;
import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.LaunchActivity;
import info.emm.ui.Adapters.CalendarAdapter;
import info.emm.ui.Views.BaseFragment;
import info.emm.ui.Views.SpecialCalendar;
import info.emm.yuanchengcloudb.R;

public class MyCalendarFragment extends BaseFragment implements PullToRefreshListener, OnGestureListener,
info.emm.messenger.NotificationCenter.NotificationCenterDelegate,
WeiyiMeetingNotificationCenter.NotificationCenterDelegate,ExpandableListView.OnChildClickListener,
ExpandableListView.OnGroupClickListener,OnHeaderUpdateListener,OnGiveUpTouchEventListener{

	private ViewFlipper flipper = null;
	private GestureDetector gestureDetector = null;
	private CalendarAdapter calV = null;
	private GridView gridView = null;
	private TextView topText = null;
	private int jumpMonth = 0;    
	private int jumpYear = 0;    
	private int year_c = 0;
	private int month_c = 0;
	private int day_c = 0;
	private String currentDate = "";
	private String currrentGroupDate = "";

	private RefreshableView m_Refreshable;
	private PinnedHeaderExpandableListView m_ListView;
	private StickyLayout stickyLayout;
	private MyMeetingListAdapter adapter;
	private	String m_strCurrentMeetingPsd;
	private String m_strCurrentMeetingID;
	private SpecialCalendar sc = new SpecialCalendar();
	private int intoYear = 0;
	private int intoMonth = 0;

	public MyCalendarFragment() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdfGroup = new SimpleDateFormat("yyyy-MM-dd");
		currentDate = sdf.format(date);  
		currrentGroupDate = sdfGroup.format(date);
		year_c = Integer.parseInt(currentDate.split("-")[0]);
		month_c = Integer.parseInt(currentDate.split("-")[1]);
		day_c = Integer.parseInt(currentDate.split("-")[2]);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	@SuppressLint("UseValueOf")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(fragmentView == null){
			Log.e("TAG", "onCreateView  begin.....");
			fragmentView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_mycalender,null);
			m_Refreshable = (RefreshableView) fragmentView.findViewById(R.id.refreshable_view);
			m_Refreshable.setOnRefreshListener(this, R.id.refreshable_view);
			m_ListView = (PinnedHeaderExpandableListView) fragmentView.findViewById(R.id.list_view);
			gestureDetector = new GestureDetector(this);
			flipper = (ViewFlipper)fragmentView.findViewById(R.id.flipper);
			topText = (TextView)fragmentView.findViewById(R.id.toptext);

			stickyLayout = (StickyLayout)fragmentView.findViewById(R.id.sticky_layout);
			flipper.removeAllViews();
			Log.e("TAG", "calendar adapter begin....");
			calV = new CalendarAdapter(getActivity(), getResources(),jumpMonth,jumpYear,year_c,month_c,day_c);
			Log.e("TAG", "calendar adapter end....");
			addGridView();
			gridView.setAdapter(calV);
			flipper.addView(gridView,0);
			addTextToTopTextView(topText);
			if(MessagesController.getInstance().groupLists.size()>0){
				MessagesController.getInstance().groupLists.clear();
			}
			if(MessagesController.getInstance().meetingMap.size()>0){
				MessagesController.getInstance().meetingMap.clear();
			}
			Log.e("TAG", "GroupAndChildList begin....");
			intoYear = year_c;
			intoMonth = month_c;
			MessagesController.getInstance().GroupAndChildList(intoYear,intoMonth,currrentGroupDate);//yyyyMMddE=2016-05-01 currrentGroupDate 选择的日�?
			Log.e("TAG", "groupAndChild end...");
			adapter = new MyMeetingListAdapter();
			m_ListView.setGroupIndicator(null);
			m_ListView.setAdapter(adapter);
			adapter.notifyDataSetChanged();


			m_ListView.setOnHeaderUpdateListener(this);
			m_ListView.setOnChildClickListener(this);
			m_ListView.setOnGroupClickListener(this);
			stickyLayout.setOnGiveUpTouchEventListener(this);
		}else{
			ViewGroup parent = (ViewGroup)fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		Log.e("TAG", "onCreateView  end.....");
		return fragmentView;
	}
	@Override
	public void onStart() {
		super.onStart();
		info.emm.messenger.NotificationCenter.getInstance().addObserver(this,MessagesController.getall_meeting);
		info.emm.messenger.NotificationCenter.getInstance().addObserver(this,MessagesController.meeting_list_delete);

		Log.i("TAG", "onStart...");
	}
	@Override
	public void onStop() {
		super.onStop();
		NotificationCenter.getInstance().removeObserver(this, MessagesController.getall_meeting);
		NotificationCenter.getInstance().removeObserver(this, MessagesController.meeting_list_delete);	
	}             

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
	@Override
	public void onRefresh() {
		Log.e("TAG", "onRefresh  begin.....");
		ConnectionsManager.getInstance().getUpdate();
		Log.e("emm", "onRefresh end ......");

	}
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) {
		int gvFlag = 0;       
		if (e1.getX() - e2.getX() > 100) {
			
			addGridView();   
			jumpMonth++;     
			calV = new CalendarAdapter(getActivity(), getResources(),jumpMonth,jumpYear,year_c,month_c,day_c);
			gridView.setAdapter(calV);
			gvFlag++;
			addTextToTopTextView(topText);
			flipper.addView(gridView, gvFlag);
			this.flipper.setInAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.push_left_in));
			this.flipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.push_left_out));
			this.flipper.showNext();
			flipper.removeViewAt(0);

			StringBuffer buffer = new StringBuffer();
			String months;
			if(Integer.parseInt(calV.getShowMonth())<10){
				months = "0"+calV.getShowMonth();
			}else{
				months = calV.getShowMonth();
			}
			buffer.append(calV.getShowYear()).append("-").append(months).append("-").append("01");//.append(" ").append(dayOfWeek);
			currrentGroupDate = buffer.toString();
			if(MessagesController.getInstance().groupLists.size()>0){
				MessagesController.getInstance().groupLists.clear();
			}
			if(MessagesController.getInstance().meetingMap.size()>0){
				MessagesController.getInstance().meetingMap.clear();
			}
			intoYear = Integer.parseInt(calV.getShowYear());
			intoMonth = Integer.parseInt(calV.getShowMonth());
			MessagesController.getInstance().GroupAndChildList(intoYear,intoMonth,currrentGroupDate);
			adapter.notifyDataSetChanged();
			return true;
		} else if (e1.getX() - e2.getX() < -100) {
			
			addGridView();   
			jumpMonth--;    

			calV = new CalendarAdapter(getActivity(), getResources(),jumpMonth,jumpYear,year_c,month_c,day_c);
			gridView.setAdapter(calV);
			gvFlag++;
			addTextToTopTextView(topText);
			flipper.addView(gridView,gvFlag);
			this.flipper.setInAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.push_right_in));
			this.flipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.push_right_out));
			this.flipper.showPrevious();
			flipper.removeViewAt(0);
			StringBuffer buffer = new StringBuffer();
			String months;
			if(Integer.parseInt(calV.getShowMonth())<10){
				months = "0"+calV.getShowMonth();
			}else{
				months = calV.getShowMonth();
			}
			buffer.append(calV.getShowYear()).append("-").append(months).append("-").append("01");//.append(" ").append(dayOfWeek);
			currrentGroupDate = buffer.toString();
			if(MessagesController.getInstance().groupLists.size()>0){
				MessagesController.getInstance().groupLists.clear();
			}
			if(MessagesController.getInstance().meetingMap.size()>0){
				MessagesController.getInstance().meetingMap.clear();
			}
			intoYear = Integer.parseInt(calV.getShowYear());
			intoMonth = Integer.parseInt(calV.getShowMonth());
			MessagesController.getInstance().GroupAndChildList(intoYear,intoMonth,currrentGroupDate);
			adapter.notifyDataSetChanged();
			return true;
		}
		return false;
	}
	public boolean onTouchEvent(MotionEvent event) {
		return this.gestureDetector.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public void addTextToTopTextView(TextView view){
		StringBuffer textDate = new StringBuffer();
		textDate.append(calV.getShowYear()).append(getString(R.string.year)).append(calV.getShowMonth()).append(getString(R.string.month)).append("\t");
		view.setText(textDate);
	}

	
	private void addGridView() {
		Log.i("TAG", "gridView begin........");
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		gridView = new GridView(getActivity());
		gridView.setNumColumns(7);
		gridView.setGravity(Gravity.CENTER);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridView.setOnTouchListener(new OnTouchListener() {
		
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return MyCalendarFragment.this.gestureDetector.onTouchEvent(event);
			}
		});
		gridView.setOnItemClickListener(new OnItemClickListener() {
		
			@SuppressLint("UseSparseArrays")
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				int startPosition = calV.getStartPositon();
				int endPosition = calV.getEndPosition();
				if(startPosition <= position  && position <= endPosition){
					String scheduleDay = calV.getDateByClickItem(position).split("\\.")[0];  
					String scheduleYear = calV.getShowYear();
					String scheduleMonth = calV.getShowMonth();
					calV.notifyDataSetChanged();
					calV.setSeclection(position);

					int day = Integer.parseInt(scheduleDay);
					int month =Integer.parseInt(scheduleMonth);
					int year = Integer.parseInt(scheduleYear);
					String days;            
					String months;
					if(day<10){
						days = "0"+String.valueOf(day);
					}else{
						days = String.valueOf(day);
					}
					if(month<10){
						months = "0"+String.valueOf(month);
					}else{
						months = String.valueOf(month);
					}
					StringBuffer buffer1 = new StringBuffer();
					buffer1.append(scheduleYear).append("-").append(months).append("-").append(days);//.append(" ").append(dayOfWeek);
					currrentGroupDate = buffer1.toString();
					m_ListView.setSelection(0);
					m_ListView.smoothScrollToPosition(0);
					if(MessagesController.getInstance().groupLists.size()>0){
						MessagesController.getInstance().groupLists.clear();
					}
					if(MessagesController.getInstance().meetingMap.size()>0){
						MessagesController.getInstance().meetingMap.clear();
					}
					intoYear = year;
					intoMonth = month;
					MessagesController.getInstance().GroupAndChildList(intoYear,intoMonth,currrentGroupDate);//yyyyMMddE=2016-05-01 currrentGroupDate 选择的日�?
					adapter.notifyDataSetChanged();
				}
			}
		});
		Log.i("TAG", "gridView end........");
		gridView.setLayoutParams(params);
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

		//		for(int i =0;i < MessagesController.getInstance().groupLists.size();i++){
		//			if(MessagesController.getInstance().meetingMap.get(MessagesController.getInstance().groupLists)!=null && 
		//					MessagesController.getInstance().meetingMap.get(MessagesController.getInstance().groupLists).size()>0){				
		//				m_ListView.expandGroup(i);
		//			}
		//		}
		adapter.notifyDataSetChanged();
	}
	@Override
	public void didReceivedNotification(int id, Object... args) {
		if (id == MessagesController.getall_meeting) {
			if (m_Refreshable.isRefreshing()) {
				m_Refreshable.finishRefreshing();
			}
			MessagesController.getInstance().GroupAndChildList(intoYear,intoMonth,currrentGroupDate);//yyyyMMddE=2016-05-01 currrentGroupDate 选择的日�?
			adapter.notifyDataSetChanged();
			calV.notifyDataSetChanged();
		}
		else if (id == MessagesController.meeting_list_delete) {
			int mid = (Integer) args[0];
			if (mid==-1){
				Utitlties.HideProgressDialog(getActivity());
				Toast.makeText(getActivity().getApplicationContext(),R.string.delete_meeting_faild,Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(getActivity().getApplicationContext(),R.string.delete_meeting_success,Toast.LENGTH_SHORT).show();
			}
			ConnectionsManager.getInstance().getUpdate();
			adapter.notifyDataSetChanged();
			calV.notifyDataSetChanged();
		}
	}	
	class MyMeetingListAdapter extends BaseExpandableListAdapter{

		@Override
		public Object getChild(int arg0, int arg1) {
			return MessagesController.getInstance().meetingMap.get(MessagesController.getInstance().getListDateByPos(arg0));
		}

		@Override
		public long getChildId(int arg0, int arg1) {
			return arg1;
		}

		@Override
		public int getChildrenCount(int arg0) {
			return MessagesController.getInstance().meetingMap.get(MessagesController.getInstance().getListDateByPos(arg0)).size();
		}

		@SuppressLint("SimpleDateFormat")
		@Override
		public View getChildView(int arg0, int arg1, boolean arg2, View arg3,
				ViewGroup arg4) {
			ChildViewHolder holder = null;
			if(arg3 == null){
				holder = new ChildViewHolder();
				arg3 = LayoutInflater.from(getActivity()).inflate(R.layout.mymeetinglist_item_child, null);
				holder.meetingNameTv = (TextView) arg3.findViewById(R.id.meeting_topic_tv);
				holder.meetingIdTv = (TextView) arg3.findViewById(R.id.meeting_id_tv);
				holder.meetingStartTimeTv = (TextView) arg3.findViewById(R.id.meeting_time_tv);
				holder.startMeetingTv = (TextView) arg3.findViewById(R.id.meeting_start_tv);
				arg3.setTag(holder);
			}else{
				holder = (ChildViewHolder) arg3.getTag();
			}
			TLRPC.TL_MeetingInfo mt= MessagesController.getInstance().meetingMap.get(MessagesController.getInstance().getListDateByPos(arg0)).get(arg1);
			holder.meetingStartTimeTv.setText(DateUnit.getHHmm(mt.startTime));
			holder.meetingIdTv.setText("ID:" + mt.mid);
			holder.meetingNameTv.setText(mt.topic);

			StartBtnListen sbl = new StartBtnListen();
			sbl.SetMeetingID(mt);
			holder.startMeetingTv.setOnClickListener(sbl);
			Log.i("TAG", "getChildView........");
			return arg3;
		}

		@Override
		public Object getGroup(int arg0) {
			return MessagesController.getInstance().getListDateByPos(arg0);
		}

		@Override
		public int getGroupCount() {
			return MessagesController.getInstance().set.size();
		}

		@Override
		public long getGroupId(int arg0) {
			return arg0;
		}

		@Override
		public View getGroupView(int arg0, boolean arg1, View arg2,
				ViewGroup arg3) {

			GroupViewHolder viewHolder = null;
			if(arg2 == null){        
				viewHolder = new GroupViewHolder();
				arg2 = LayoutInflater.from(getActivity()).inflate(R.layout.mymeetinglist_item_group, null);
				viewHolder.startDateTv = (TextView) arg2.findViewById(R.id.item_startdate_tv);
				arg2.setTag(viewHolder);
			}else{
				viewHolder = (GroupViewHolder) arg2.getTag();
			}


			if(MessagesController.getInstance().getListDateByPos(arg0).equals(currentDate)){
				viewHolder.startDateTv.setText(MessagesController.getInstance().getListDateByPos(arg0)+" "+sc.getWeekDayOfMonth(MessagesController.getInstance().getListDateByPos(arg0)));
				viewHolder.startDateTv.setBackgroundResource(R.color.meetinglist_group_blue);
			}else{
				viewHolder.startDateTv.setText(MessagesController.getInstance().getListDateByPos(arg0)+" "+sc.getWeekDayOfMonth(MessagesController.getInstance().getListDateByPos(arg0)));
				viewHolder.startDateTv.setBackgroundResource(R.color.public_black_e0);
			}
			Log.i("TAG", "getGroupView........");
			return arg2;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int arg0, int arg1) {
			return true;
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
			for(int i =0;i < getGroupCount();i++){
				m_ListView.expandGroup(i);
			}
		}
		class GroupViewHolder{
			TextView startDateTv;
		}
		class ChildViewHolder{
			TextView meetingNameTv;
			TextView meetingIdTv;
			TextView startMeetingTv;
			TextView meetingStartTimeTv;
		}
	}
	private class StartBtnListen implements OnClickListener {

		//		String strMeetingID = "";
		TLRPC.TL_MeetingInfo myMeeting;

		@Override
		public void onClick(View arg0) {
			//			m_strCurrentMeetingID = strMeetingID;
			//			m_strCurrentMeetingPsd = null;
			JointoMeeting(myMeeting);
			Log.e("TAG", "joinMeeting......");
		}

		public void SetMeetingID(TLRPC.TL_MeetingInfo info) {
			myMeeting = info;
		}
	}
	public void JointoMeeting(TLRPC.TL_MeetingInfo info) {
		Utitlties.ShowProgressDialog(getActivity(),getResources().getString(R.string.Loading));

		String Url = "";
		if(UserConfig.isPublic){
			String ss = Config.publicWebHttp;
			String publicHttp = ss.substring(7);
			Url = "weiyi://start?ip="+publicHttp+"&port="+80+"&meetingid="+info.mid+"&meetingtype="+info.meetingType+"&title="+info.topic+"&clientidentification="+UserConfig.currentUser.identification+"&createrid="+info.createid+"&userid="+UserConfig.clientUserId+"&nickname="+UserConfig.getNickName();
		}else{
			if(UserConfig.privatePort == 80){
				String privateHttp = UserConfig.privateWebHttp;
				Url = "weiyi://start?ip="+privateHttp+"&port="+80+"&meetingid="+info.mid+"&meetingtype="+info.meetingType+"&title="+info.topic+"&clientidentification="+UserConfig.currentUser.identification+"&createrid="+info.createid+"&userid="+UserConfig.clientUserId+"&nickname="+UserConfig.getNickName();
			}else{
				String privateHttp = UserConfig.privateWebHttp;
				int privatePort = UserConfig.privatePort;
				Url = "weiyi://start?ip="+privateHttp+"&port="+privatePort+"&meetingid="+info.mid+"&meetingtype="+info.meetingType+"&title="+info.topic+"&clientidentification="+UserConfig.currentUser.identification+"&createrid="+info.createid+"&userid="+UserConfig.clientUserId+"&nickname="+UserConfig.getNickName();
			}
		}
		WeiyiMeeting.getInstance().joinMeeting(getActivity(), Url,null);
		//		ApplicationLoader.getInstance().joinMeeting(getActivity(),m_strCurrentMeetingID,m_strCurrentMeetingPsd);

	}

	@Override
	public boolean onGroupClick(ExpandableListView arg0, View arg1, int arg2,
			long arg3) {
		return true;
	}
	
	@Override
	public boolean onChildClick(ExpandableListView arg0, View arg1, int arg2,
			int arg3, long arg4) {
		if(MessagesController.getInstance().meetingMap.get(MessagesController.getInstance().getListDateByPos(arg2)).get(arg3) != null){
			int mId = MessagesController.getInstance().meetingMap.get(MessagesController.getInstance().getListDateByPos(arg2)).get(arg3).mid;
			MeetingInfoFragment infoFragment = new MeetingInfoFragment();
			Bundle bundle = new Bundle();
			bundle.putString("meetingid",mId+"");					
			infoFragment.setArguments(bundle);
			((LaunchActivity) getActivity()).presentFragment(infoFragment, "", false);
		}
		return true;
	}

	@SuppressLint("SimpleDateFormat")
	private Date getDateNext(String currentTime, int day){
		Date d;
		Calendar now = null;
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
		try {
			d = formater.parse(currentTime);
			now = Calendar.getInstance();    
			now.setTime(d);    
			now.set(Calendar.DATE, now.get(Calendar.DATE) + day);    

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return  now.getTime();
	}

	@Override
	public View getPinnedHeader() {
		View headerView = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.mymeetinglist_item_group, null);
		headerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		return headerView;
	}

	@Override
	public void updatePinnedHeader(View headerView, int firstVisibleGroupPos) {
		TextView textView = (TextView) headerView.findViewById(R.id.item_startdate_tv);
		if(MessagesController.getInstance().groupLists != null && !MessagesController.getInstance().groupLists.isEmpty()){
			textView.setText(MessagesController.getInstance().getListDateByPos(firstVisibleGroupPos));
			if(MessagesController.getInstance().getListDateByPos(firstVisibleGroupPos).equals(currentDate)){
				textView.setBackgroundResource(R.color.meetinglist_group_blue);
				textView.setText(MessagesController.getInstance().getListDateByPos(firstVisibleGroupPos)+" "+sc.getWeekDayOfMonth(MessagesController.getInstance().getListDateByPos(firstVisibleGroupPos)));
			}else{
				textView.setBackgroundResource(R.color.public_black_e0);
				textView.setText(MessagesController.getInstance().getListDateByPos(firstVisibleGroupPos)+" "+sc.getWeekDayOfMonth(MessagesController.getInstance().getListDateByPos(firstVisibleGroupPos)));
			}
		}
	}

	@Override
	public boolean giveUpTouchEvent(MotionEvent event) {
		if (m_ListView.getFirstVisiblePosition() == 0) {
			View view = m_ListView.getChildAt(0);
			if (view != null && view.getTop() >= 0) {
				return true;
			}
		}
		return false;
	}

}

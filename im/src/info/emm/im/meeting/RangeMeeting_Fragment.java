package info.emm.im.meeting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.utils.WeiyiMeetingNotificationCenter;

import info.emm.messenger.UserConfig;
import info.emm.ui.ApplicationLoader;
import info.emm.ui.CreateNewGroupActivity;
import info.emm.ui.LaunchActivity;
import info.emm.ui.Views.BaseFragment;
import info.emm.yuanchengcloudb.R;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.TLRPC;

import com.utils.Utitlties;
/**
 *锟斤拷锟脚伙拷锟斤拷
 * @author Administrator
 *
 */
public class RangeMeeting_Fragment extends BaseFragment implements OnClickListener,
info.emm.messenger.NotificationCenter.NotificationCenterDelegate,WeiyiMeetingNotificationCenter.NotificationCenterDelegate {

	LayoutInflater m_inflater;
	EditText mEditTopic; 
	private LinearLayout attendMemberLl;
	private TextView attendNumTv;

	TextView mTextStartDate;
	TextView mTextStartTime;

	TextView mTextEndTime;
	TextView txt_rangmeeting;

	Date mDateStart;
	Date mDateEnd; 

	int mIntTimeSet = 0;
	int type = 0;

	int meetingType = 0;
	static String repreatTime = "";
	String attendPwd = null;
	String livePwd = null;

	private int strNum;
	private ArrayList<Integer> selectedContacts=new ArrayList<Integer>();
	private EditText mChairmanPwd;
	private EditText mAttendPwd;
	private EditText mLivePwd;
	private String chairmanPwd;
	private TextView mRepeatTv;
	private ImageView mIsPublisgIv;
	private LinearLayout startDateLl;
	private LinearLayout rePeateLl;
	private RelativeLayout isPublishRl;
	private boolean ispubliMeeting = true;
	private int visiblemeeting = 1;
	private int meetingTime = 1;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	@Override
	public boolean onFragmentCreate() {
		return true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		info.emm.messenger.NotificationCenter.getInstance().addObserver(this,MessagesController.meeting_list_update);
		info.emm.messenger.NotificationCenter.getInstance().addObserver(this,MessagesController.meeting_create_failed);
		chairmanPwd = getRandom(4);
		//xiaoyang add
		if(ApplicationLoader.edition == 1){
			ispubliMeeting = false;
			visiblemeeting = 0;
		}
		//xiaoyang add
		if(fragmentView == null){

			setHasOptionsMenu(true);
			m_inflater = inflater;
			fragmentView = inflater.inflate(R.layout.activity_range_meeting, null);
			mEditTopic = (EditText) fragmentView
					.findViewById(R.id.editText_meetingtopic);
			mTextStartDate = (TextView) fragmentView
					.findViewById(R.id.editText_startdate);
			mTextStartTime = (TextView) fragmentView
					.findViewById(R.id.editText_starttime);
			mTextEndTime = (TextView) fragmentView
					.findViewById(R.id.editText_endtime);
			txt_rangmeeting = (TextView) fragmentView.findViewById(R.id.btn_rangmeeting);
			attendNumTv = (TextView) fragmentView.findViewById(R.id.rangemeeting_attend_num_tv);
			attendMemberLl = (LinearLayout) fragmentView.findViewById(R.id.rangemeeting_attend_ll);
			mChairmanPwd = (EditText) fragmentView.findViewById(R.id.rangemeeting_chairman_pwd_num_tv);
			mAttendPwd = (EditText) fragmentView.findViewById(R.id.rangemeeting_attend_num_et);
			mLivePwd = (EditText) fragmentView.findViewById(R.id.rangemeeting_live_pwd_num_et);
			mChairmanPwd.setText(chairmanPwd);
			mRepeatTv = (TextView) fragmentView.findViewById(R.id.editText_repeat);
			mIsPublisgIv = (ImageView) fragmentView.findViewById(R.id.rangemeeting_ispublish_iv);
			startDateLl = (LinearLayout) fragmentView.findViewById(R.id.startdate_ll);
			rePeateLl = (LinearLayout) fragmentView.findViewById(R.id.rangemeeting_repeat_ll);
			isPublishRl = (RelativeLayout) fragmentView.findViewById(R.id.rangemeeting_ispublish_ll);
			//			if(UserConfig.isPublic && !UserConfig.isPersonalVersion){
			//				rePeateLl.setVisibility(View.VISIBLE);
			//				isPublishRl.setVisibility(View.VISIBLE);
			//			}else{
			//				rePeateLl.setVisibility(View.GONE);
			//				isPublishRl.setVisibility(View.GONE);
			//			}
			mIsPublisgIv.setImageResource(ispubliMeeting ? R.drawable.btn_check_on : R.drawable.btn_check_off);
			
			attendMemberLl.setOnClickListener(this);
			mTextStartDate.setOnClickListener(this);
			mTextStartTime.setOnClickListener(this);
			txt_rangmeeting.setOnClickListener(this);
			mTextEndTime.setOnClickListener(this);
			mRepeatTv.setOnClickListener(this);
			mIsPublisgIv.setOnClickListener(this);
			mTextEndTime.setText(R.string.Longterm);
			mRepeatTv.setText(R.string.repeat_no);
			selectedContacts.add(0, UserConfig.clientUserId);
			attendNumTv.setText(strNum+1+"");
			mEditTopic.setText(UserConfig.getNickName() + getString(R.string.s_meeting));
			long time = System.currentTimeMillis();
			mDateStart = new Date(time);
			mDateEnd = new Date(0);
			updateTime();
		}else{
			ViewGroup parent = (ViewGroup)fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		return fragmentView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		info.emm.messenger.NotificationCenter.getInstance().removeObserver(this,MessagesController.meeting_list_update);
		info.emm.messenger.NotificationCenter.getInstance().removeObserver(this,MessagesController.meeting_create_failed);

	}

	@Override
	public void onClick(View arg0) {
		int nID = arg0.getId();
		if (nID == R.id.editText_startdate) {
			mIntTimeSet = 0;
			popupTimeSetWindow();
		} else if (nID == R.id.editText_starttime) {
			mIntTimeSet = 1;
			popupTimeSetWindow();
		}
		// else if(nID == R.id.editText_enddate){
		// mIntTimeSet = 2;
		// popupTimeSetWindow();
		// }
		else if (nID == R.id.editText_endtime) {
			mIntTimeSet = 3;
			popupTimeSetWindow();
		}else if(nID == R.id.btn_rangmeeting){
			rangerMeeting();
		}else if(nID == R.id.rangemeeting_attend_ll){
			AttendMeetingFM m_fragrm = new AttendMeetingFM();
			Bundle bundle = new Bundle();
			//			selectedContacts.add(UserConfig.clientUserId);
			bundle.putIntegerArrayList("attendList", selectedContacts);
			m_fragrm.setArguments(bundle);
			((LaunchActivity)getActivity()).presentFragment(m_fragrm, "", false);
		}else if(nID == R.id.editText_repeat){
			mIntTimeSet = 4;
			popupTimeSetWindow();
		}else if(nID ==  R.id.rangemeeting_ispublish_iv){
			mIsPublisgIv.setImageResource(!ispubliMeeting ? R.drawable.btn_check_on : R.drawable.btn_check_off);
			ispubliMeeting = !ispubliMeeting;
			if(ispubliMeeting){
				visiblemeeting = 1;
			}else{
				visiblemeeting = 0;
			}
		}
	}

	public String getRandom(int count){
		StringBuffer sb = new StringBuffer();
		String str = "0123456789";
		Random r = new Random();
		for(int i=0;i<count;i++){
			int num = r.nextInt(str.length());
			sb.append(str.charAt(num));
			str = str.replace((str.charAt(num)+""), "");
		}
		return sb.toString();
	}
	@SuppressLint("SimpleDateFormat")
	private void rangerMeeting() {

		String strTextTopic = mEditTopic.getText().toString();
		attendPwd = mAttendPwd.getText().toString();
		livePwd = mLivePwd.getText().toString();
		chairmanPwd = mChairmanPwd.getText().toString();
		if (strTextTopic.isEmpty()) {

			Toast.makeText(getActivity().getApplicationContext(),
					R.string.topic_is_empty, Toast.LENGTH_SHORT).show();
			return;
		}		
		TLRPC.TL_MeetingInfo mt = new TLRPC.TL_MeetingInfo();

		if(meetingType == 3 || meetingType == 4 || meetingType == 5 || meetingType == 6){
			mt.endTime = 0;
			//			if(type==0){//默锟斤拷1小时
			//				mDateEnd.setTime(mDateStart.getTime() + (1 * 60*60*1000));
			//				mt.endTime = (int)(mDateEnd.getTime()/1000);
			//			}else
			//				mt.endTime = (int)(mDateEnd.getTime()/1000);
		}else{
			if(type==0)
				mt.endTime = 0;
			else
				mt.endTime = (int)(mDateEnd.getTime()/1000);
		}

		SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");
		String StartTime = formatTime.format(mDateStart);
		if(meetingType == 4){//锟斤拷锟斤拷直锟接讹拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
			mt.startTime = (int)(getDateNext(mDateStart, getWeekOfDate(mDateStart)).getTime()/1000);
		}else{
			mt.startTime = (int)(mDateStart.getTime()/1000);
		}
		mt.topic = strTextTopic;
		mt.createid = UserConfig.clientUserId;	
		mt.confuserpwd = attendPwd;
		mt.sidelineuserpwd = livePwd;
		mt.chairmanpwd = chairmanPwd;
		mt.meetingType = meetingType;
		if(meetingType == 0){
			mt.beginTime = ""; 
		}else{
			mt.beginTime = repreatTime+":"+StartTime;//  repreatTime锟斤拷示锟截革拷锟斤拷时锟斤拷    每锟斤拷锟斤拷0, 每锟杰碉拷锟斤拷锟节硷拷锟斤拷每锟斤拷锟铰的硷拷锟斤拷 
		}

		String duration= null;

		if(meetingTime > 0 && meetingTime < 10){
			duration ="0" + meetingTime+":00";
		}else{
			duration = meetingTime+":00";
		}  
		mt.duration = duration; //锟斤拷式 01:00  一小时
		mt.ispublicMeeting = visiblemeeting;
		//		selectedContacts.add(0, UserConfig.clientUserId);
		mt.participants.addAll(this.selectedContacts);
		Utitlties.ShowProgressDialog(this.getActivity(), getResources()
				.getString(R.string.Loading));

		MeetingMgr.getInstance().scheduleMeeting(mt); 
	}
	@SuppressLint("NewApi")
	public void popupTimeSetWindow() {
		if (mIntTimeSet == 0) {
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			View view = inflater.inflate(R.layout.dateselecter, null);

			final PopupWindow pop = new PopupWindow(view,
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, false);

			pop.setBackgroundDrawable(new BitmapDrawable());
			pop.setOutsideTouchable(true);
			pop.setFocusable(true);

			Button btnSet = (Button) view.findViewById(R.id.button_set);
			Button btnCancel = (Button) view.findViewById(R.id.button_cancel);

			final DatePicker dpicker = (DatePicker) view
					.findViewById(R.id.datePicker);
			btnSet.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (mIntTimeSet == 0) {
						mDateStart.setYear(dpicker.getYear() - 1900);
						mDateStart.setMonth(dpicker.getMonth());
						mDateStart.setDate(dpicker.getDayOfMonth());
					} else {
						mDateEnd.setYear(mDateStart.getYear());
						mDateEnd.setMonth(mDateStart.getMonth());
						mDateEnd.setDate(mDateStart.getDate());
					}
					updateTime();
					pop.dismiss();
				}

			});
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					pop.dismiss();
				}

			});
			pop.showAtLocation(mTextStartTime, Gravity.BOTTOM, 0, 0);

		} else if (mIntTimeSet == 1) {
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			View view = inflater.inflate(R.layout.timeselecter, null);

			final PopupWindow pop = new PopupWindow(view,
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, false);

			pop.setBackgroundDrawable(new BitmapDrawable());
			pop.setOutsideTouchable(true);
			pop.setFocusable(true);

			Button btnSet = (Button) view.findViewById(R.id.button_set);
			Button btnCancel = (Button) view.findViewById(R.id.button_cancel);

			final TimePicker tpicker = (TimePicker) view
					.findViewById(R.id.timePicker);
			btnSet.setOnClickListener(new OnClickListener() {

				@Override 
				public void onClick(View arg0) {
					if (mIntTimeSet == 1) {
						mDateStart.setHours(tpicker.getCurrentHour());
						mDateStart.setMinutes(tpicker.getCurrentMinute());
					} else {

						mDateEnd.setHours(tpicker.getCurrentHour());
						mDateEnd.setMinutes(tpicker.getCurrentMinute());
					}
					updateTime();
					pop.dismiss();

				}

			});
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					pop.dismiss();
				}

			});
			pop.showAtLocation(mTextStartTime, Gravity.BOTTOM, 0, 0);
		} else if (mIntTimeSet == 3) {//锟斤拷锟斤拷时锟斤拷
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			View view = inflater.inflate(R.layout.hoursselecter, null);

			final PopupWindow pop = new PopupWindow(view,
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, false);
			final NumberPicker hourpicker = (NumberPicker) view
					.findViewById(R.id.hour);
			String[] hours = null;
			if(meetingType == 3 || meetingType == 4 || meetingType == 5 || meetingType == 6){//锟斤拷锟节伙拷锟斤拷
				hours = new String[]{ "1", "2", "3",
						"4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
						"15", "16", "17", "18", "19", "20", "21", "22", "23" };
			}else{
				hours = new String[]{ getString(R.string.Longterm), "1", "2", "3",
						"4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
						"15", "16", "17", "18", "19", "20", "21", "22", "23" };
			}
			hourpicker.setDisplayedValues(hours);
			hourpicker.setMinValue(0);
			hourpicker.setMaxValue(hours.length - 1);
			for (int i = 0; i < hourpicker.getChildCount(); i++) {
				hourpicker.getChildAt(i).setFocusable(false);
			}

			pop.setBackgroundDrawable(new BitmapDrawable());
			pop.setOutsideTouchable(true);
			pop.setFocusable(true);

			Button btnSet = (Button) view.findViewById(R.id.button_set);
			Button btnCancel = (Button) view.findViewById(R.id.button_cancel);


			btnSet.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if(meetingType == 3 || meetingType == 4 || meetingType == 5 || meetingType == 6){
						type = 1;
						mDateEnd.setTime(mDateStart.getTime() + (hourpicker.getValue()+1) * 60*60*1000);
						mTextEndTime.setText((hourpicker.getValue()+1)
								+ getResources().getString(R.string.hour));
						meetingTime = hourpicker.getValue()+1;
					}else{
						if (hourpicker.getValue() == 0) {
							type = 0;
							mTextEndTime.setText(getResources().getString(
									R.string.Longterm));
							meetingTime = 0 ;
						} else {
							mDateEnd.setTime(mDateStart.getTime() + hourpicker.getValue() * 60*60*1000);
							long dd = mDateEnd.getTime();
							type = 1;
							mTextEndTime.setText(hourpicker.getValue()
									+ getResources().getString(R.string.hour));
							meetingTime = hourpicker.getValue();
						}
					}
					updateTime();
					pop.dismiss();
				}
			});
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					pop.dismiss();
				}

			});
			pop.showAtLocation(mTextStartTime, Gravity.BOTTOM, 0, 0);
		}else if (mIntTimeSet == 4) {//锟角凤拷锟斤拷锟斤拷锟劫匡拷
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			View view = inflater.inflate(R.layout.rangemeeting_repeat, null);

			final PopupWindow pop = new PopupWindow(view,
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, false);
			pop.setBackgroundDrawable(new BitmapDrawable());
			pop.setOutsideTouchable(true);
			pop.setFocusable(true);
			ListView listView = (ListView) view.findViewById(R.id.repeat_listiew);
			final String[] date = { getString(R.string.repeat_no),getString(R.string.repeat_day),
					getString(R.string.repeat_week),//getString(R.string.repeat_two_week),
					getString(R.string.repeat_month)};
			listView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.repeat_item_tv, date));

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					if(arg2 == 0){
						meetingType = 0;
						mRepeatTv.setText(R.string.repeat_no);
						startDateLl.setVisibility(View.VISIBLE);
						mTextEndTime.setText(R.string.Longterm);
					}else if(arg2 == 1){
						repreatTime = 0 + "";
						meetingType = 3;
						mRepeatTv.setText(R.string.every_day);
						startDateLl.setVisibility(View.GONE);
						mTextEndTime.setText("1"+ getResources().getString(R.string.hour));
					}else if(arg2 == 2){
						meetingType = 4;
						popuListView(2);
						startDateLl.setVisibility(View.GONE);
						mTextEndTime.setText("1"+ getResources().getString(R.string.hour));
						//					}else if(arg2 == 3){
						//						meetingType = 5;
						//						popuListView(3);
						//						startDateLl.setVisibility(View.GONE);
						//						mTextEndTime.setText("1"+ getResources().getString(R.string.hour));
					}else if(arg2 == 3){
						meetingType = 6;
						popuMonth();
						startDateLl.setVisibility(View.GONE);
						mTextEndTime.setText("1"+ getResources().getString(R.string.hour));
					}
					pop.dismiss();
				}
			});
			pop.showAtLocation(mTextStartTime, Gravity.BOTTOM, 0, 0);
		}
	}
	@SuppressLint("SimpleDateFormat")
	public void updateTime() {

		//		if (mDateStart.after(mDateEnd)) {
		//			mDateEnd = new Date(mDateStart.getTime() + 1000 * 60 * 60);
		SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd E");
		SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");

		String StartDate = formatDate.format(mDateStart);
		String StartTime = formatTime.format(mDateStart);
		String EndDate = formatDate.format(mDateEnd);
		String EndTIme = formatTime.format(mDateEnd);

		mTextStartDate.setText(StartDate);
		mTextStartTime.setText(StartTime);

		//		 mTextEndDate.setText(EndDate);
		//		String mTextEndDate = EndDate;
		//		String mtextEndtime = EndTIme;
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
		if(parentActivity != null)
		{
			((LaunchActivity) parentActivity).showActionBar();
			((LaunchActivity) parentActivity).updateActionBar();
		}
	}
	@Override
	public void applySelfActionBar() {
		ActionBar actionBar = super.applySelfActionBar(true);

		if(actionBar == null)return;

		if(this.getActivity()==null)
			return;

		actionBar.setTitle(getString(R.string.app_name));

		TextView title = (TextView) getActivity()
				.findViewById(R.id.action_bar_title);
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.main_menu, menu);
		SupportMenuItem donemenu = (SupportMenuItem) menu
				.findItem(R.id.logout_menu_item);
		TextView logout_menu = (TextView) donemenu.getActionView()
				.findViewById(R.id.logout_menu_tv);
		logout_menu.setText(R.string.AddMember);
		logout_menu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addMembers();
			}
		});
	}

	private void addMembers() {
		MessagesController.getInstance().selectedUsers.clear();
		MessagesController.getInstance().ignoreUsers.clear();
		//		if(selectedContacts.size() == 0){
		//			attendMemberLl.setVisibility(View.GONE);
		//			attendNumTv.setText(0+"");
		//		}else{
		//			attendMemberLl.setVisibility(View.VISIBLE);
		attendNumTv.setText(strNum + 1 + "");
		for(int i=0;i<selectedContacts.size();i++){
			int userid = selectedContacts.get(i);
			TLRPC.User user = MessagesController.getInstance().users.get(userid);
			MessagesController.getInstance().ignoreUsers.put(userid, user);
			//			}
		}
		Intent intent = new Intent(this.parentActivity, CreateNewGroupActivity.class);
		Bundle bundle = new Bundle();
		bundle.putBoolean("AddGroupUser", true);
		intent.putExtras(bundle);
		startActivityForResult(intent, 200);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK){
			if(requestCode == 200){
				//				attendMemberLl.setVisibility(View.VISIBLE);
				MessagesController.getInstance().ignoreUsers.clear();
				ArrayList<Integer> selectUsers = (ArrayList<Integer>)NotificationCenter.getInstance().getFromMemCache(2);
				selectedContacts.addAll(selectUsers);
				strNum = selectedContacts.size();
				attendNumTv.setText(strNum + 1 + "");
				if(selectedContacts == null)
					return;				
			}
		}
	}

	@Override
	public void didReceivedNotification(int id, Object... args) 
	{
		if(id == MessagesController.meeting_create_failed)
		{
			Toast.makeText(getActivity().getApplicationContext(),
					R.string.ranger_faild, Toast.LENGTH_SHORT).show();
			Utitlties.HideProgressDialog(getActivity());
		}
		else if (MessagesController.meeting_list_update == id) 
		{
			Integer mid = (Integer) args[0];			
			Bundle bundle = new Bundle();
			bundle.putString("meetingid", mid.toString());			
			MeetingInfoFragment infoFragment = new MeetingInfoFragment();
			infoFragment.setArguments(bundle);
			((LaunchActivity)getActivity()).presentFragment(infoFragment, "", false);			
			((LaunchActivity)getActivity()).removeFromStack(RangeMeeting_Fragment.this);
			Toast.makeText(getActivity().getApplicationContext(),R.string.ranger_success, Toast.LENGTH_SHORT).show();
			Utitlties.HideProgressDialog(getActivity());			
		}
	}
	@SuppressWarnings("deprecation")
	@SuppressLint("InlinedApi")
	public void popuListView(final int position){
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View view = inflater.inflate(R.layout.rangemeeting_repeat, null);

		final PopupWindow pop = new PopupWindow(view,
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, false);
		pop.setBackgroundDrawable(new BitmapDrawable());
		pop.setOutsideTouchable(true);
		pop.setFocusable(true);
		ListView listView = (ListView) view.findViewById(R.id.repeat_listiew);
		final String[] date = { getString(R.string.Monday),getString(R.string.Tuesday),
				getString(R.string.Wednesday),getString(R.string.Thursday),
				getString(R.string.Friday),getString(R.string.Saturday),getString(R.string.Sunday),};
		listView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.repeat_item_tv, date));
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if(position ==2){//每锟斤拷
					if(arg2 == 0){
						repreatTime = 1  + "";
						mRepeatTv.setText(getString(R.string.every_week) + getString(R.string.Monday));
					}else if(arg2 == 1){
						repreatTime = 2  + "";
						mRepeatTv.setText(getString(R.string.every_week) + getString(R.string.Tuesday));
					}else if(arg2 == 2){
						repreatTime = 3  + "";
						mRepeatTv.setText(getString(R.string.every_week) + getString(R.string.Wednesday));
					}else if(arg2 == 3){
						repreatTime = 4  + "";
						mRepeatTv.setText(getString(R.string.every_week) + getString(R.string.Thursday));
					}else if(arg2 == 4){
						repreatTime = 5  + "";
						mRepeatTv.setText(getString(R.string.every_week) + getString(R.string.Friday));
					}else if(arg2 == 5){
						repreatTime = 6  + "";
						mRepeatTv.setText(getString(R.string.every_week) + getString(R.string.Saturday));
					}else if(arg2 == 6){
						repreatTime = 7  + "";
						mRepeatTv.setText(getString(R.string.every_week) + getString(R.string.Sunday));
					}
					//				}else if(position ==3){//每锟斤拷锟斤拷
					//					if(arg2 == 0){
					//						repreatTime = 1  + "";
					//						mRepeatTv.setText(getString(R.string.two_week) + getString(R.string.Monday));
					//					}else if(arg2 == 1){
					//						repreatTime = 2  + "";
					//						mRepeatTv.setText(getString(R.string.two_week) + getString(R.string.Tuesday));
					//					}else if(arg2 == 2){
					//						repreatTime = 3  + "";
					//						mRepeatTv.setText(getString(R.string.two_week) + getString(R.string.Wednesday));
					//					}else if(arg2 == 3){
					//						repreatTime = 4  + "";
					//						mRepeatTv.setText(getString(R.string.two_week) + getString(R.string.Thursday));
					//					}else if(arg2 == 4){
					//						repreatTime = 5  + "";
					//						mRepeatTv.setText(getString(R.string.two_week) + getString(R.string.Friday));
					//					}else if(arg2 == 5){
					//						repreatTime = 6  + "";
					//						mRepeatTv.setText(getString(R.string.two_week) + getString(R.string.Saturday));
					//					}else if(arg2 == 6){
					//						repreatTime = 7  + "";
					//						mRepeatTv.setText(getString(R.string.two_week) + getString(R.string.Sunday));
					//					}
				}
				pop.dismiss();
			}
		});
		pop.showAtLocation(mTextStartTime, Gravity.BOTTOM, 0, 0);
	}
	@SuppressLint("NewApi")
	public void popuMonth(){
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View view = inflater.inflate(R.layout.hoursselecter, null);
		final PopupWindow pop = new PopupWindow(view,LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, false);
		final NumberPicker hourpicker = (NumberPicker) view.findViewById(R.id.hour);
		String[] hours = {"01", "02", "03",
				"04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14",
				"15", "16", "17", "18", "19", "20", "21", "22", "23","24","25","26","27","28","29","30","31" };
		hourpicker.setDisplayedValues(hours);
		hourpicker.setMinValue(0);
		hourpicker.setMaxValue(hours.length - 1);
		for (int i = 0; i < hourpicker.getChildCount(); i++) {
			hourpicker.getChildAt(i).setFocusable(false);
		}

		pop.setBackgroundDrawable(new BitmapDrawable());
		pop.setOutsideTouchable(true);
		pop.setFocusable(true);

		Button btnSet = (Button) view.findViewById(R.id.button_set);
		Button btnCancel = (Button) view.findViewById(R.id.button_cancel);

		btnSet.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mRepeatTv.setText( getString(R.string.every_monyh)+(hourpicker.getValue()+1));
				if(hourpicker.getValue() < 9){
					repreatTime = "0" +(hourpicker.getValue()+1);
				}else{
					repreatTime = (hourpicker.getValue()+1) + "";
				}
				pop.dismiss();
			}
		});
		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				pop.dismiss();
			}
		});
		pop.showAtLocation(mTextStartTime, Gravity.BOTTOM, 0, 0);
	}

	/**
	 * 锟斤拷前锟斤拷锟斤拷锟斤拷锟杰硷拷
	 */
	static int ww;
	public static int getWeekOfDate(Date date) {      
		Calendar calendar = Calendar.getInstance();      
		if(date != null){        
			calendar.setTime(date);      
		}        
		int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;      
		if (w < 0){        
			w = 0;      
		}
		if((Integer.parseInt(repreatTime)-w)>=0){
			ww = (Integer.parseInt(repreatTime)-w);
		}else{
			ww = 7 - Math.abs(Integer.parseInt(repreatTime)-w);
		}
		return ww;       
	}
	/**
	 * 某锟斤拷锟斤拷锟斤拷锟斤拷
	 * @param date
	 * @param day
	 * @return
	 */
	private Date getDateNext(Date date, int day){
		Calendar now = null;
		now = Calendar.getInstance();    
		now.setTime(date);    
		now.set(Calendar.DATE, now.get(Calendar.DATE) + day);    

		return  now.getTime();
	}
}

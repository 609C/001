package info.emm.im.directsending;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.utils.Utitlties;
import com.utils.WeiyiMeetingNotificationCenter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import info.emm.im.meeting.MeetingMgr;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.CreateNewGroupActivity;
import info.emm.ui.LaunchActivity;
import info.emm.ui.Views.BaseFragment;
import info.emm.yuanchengcloudb.R;

/**
 * ����ֱ��
 * @author Administrator
 *
 */
public class RangeDirect_Fragment extends BaseFragment implements OnClickListener,
info.emm.messenger.NotificationCenter.NotificationCenterDelegate,
WeiyiMeetingNotificationCenter.NotificationCenterDelegate{
	private EditText mTopicTv;
	private TextView mStartDateTv;
	private TextView mStartTimeTv;
	private EditText mDirectPwdEt;
	private TextView mDirectTypeTv;
	private TextView mRangeDirectTv;
	private TextView mAttendMemberTv;
	private LinearLayout mAttendMemberLl;
	private Date startDate;
	private int popuState = 0;//0��ʼ���ڣ�1��ʼʱ�䣬2ֱ������
	private int directType = 13; //ֱ������   11����Ƶ+PPT��12����Ƶ+��Ƶ+PPT��13����Ƶ+��Ƶ  
	private ArrayList<Integer> selectedContacts=new ArrayList<Integer>();//����ĳ�Ա
	private int strNum;//����μ�ֱ��������


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		info.emm.messenger.NotificationCenter.getInstance().addObserver(this,MessagesController.meeting_list_update);
		info.emm.messenger.NotificationCenter.getInstance().addObserver(this,MessagesController.meeting_create_failed);
		if(fragmentView == null){
			fragmentView = inflater.inflate(R.layout.range_direct_fragment, container, false);

			mTopicTv = (EditText) fragmentView.findViewById(R.id.rangedirect_topic_et);
			mStartDateTv = (TextView) fragmentView.findViewById(R.id.rangedirect_startdate_tv);
			mStartTimeTv = (TextView) fragmentView.findViewById(R.id.rangedirect_starttime_tv);
			mDirectPwdEt = (EditText) fragmentView.findViewById(R.id.rangedirect_pwd_et);
			mDirectTypeTv = (TextView) fragmentView.findViewById(R.id.rangedirect_type_tv);
			mRangeDirectTv = (TextView) fragmentView.findViewById(R.id.rangedirect_sending_tv);
			mAttendMemberTv = (TextView) fragmentView.findViewById(R.id.rangedirect_attend_num_tv);
			mAttendMemberLl = (LinearLayout) fragmentView.findViewById(R.id.rangedirect_attend_ll);
			mTopicTv.setText(UserConfig.getNickName());
			mDirectTypeTv.setText(R.string.audio_video);
			mStartDateTv.setOnClickListener(this);
			mStartTimeTv.setOnClickListener(this);
			mDirectTypeTv.setOnClickListener(this);
			mRangeDirectTv.setOnClickListener(this);
			long time = System.currentTimeMillis();
			startDate = new Date(time);
			getUpdateTime();
		}else{
			ViewGroup group = (ViewGroup) fragmentView.getParent();
			if(group != null){
				group.removeView(fragmentView);
			}
		}
		return fragmentView;
	}

	/**
	 * ��ȡ����ʾ����ֵ
	 */
	@SuppressLint("SimpleDateFormat")
	private void getUpdateTime() {

		SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd E");
		SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");

		String StartDate = formatDate.format(startDate);
		String StartTime = formatTime.format(startDate);
		mStartDateTv.setText(StartDate);
		mStartTimeTv.setText(StartTime);
	}

	@Override
	public void onClick(View arg0) {
		int nId = arg0.getId();
		if(nId == R.id.rangedirect_startdate_tv){//��ʼ����
			popuState = 0;
			PopuWindows();
		}else if(nId == R.id.rangedirect_starttime_tv){//��ʼʱ��
			popuState = 1;
			PopuWindows();
		}else if(nId == R.id.rangedirect_type_tv){//ֱ������
			popuState = 2;
			PopuWindows();
		}else if(nId == R.id.rangedirect_sending_tv){//����ֱ��
			rangeDirect();
		}
	}
	/**
	 * ����ֱ��
	 */
	private void rangeDirect() {
		String directTopic = mTopicTv.getText().toString();
		String directPwd = mDirectPwdEt.getText().toString();
		if(directTopic.equals("") || directTopic.isEmpty()){
			Toast.makeText(getActivity(), R.string.topic_isempty_toast, Toast.LENGTH_LONG).show();
			return;
		}
		TLRPC.TL_MeetingInfo mt = new TLRPC.TL_MeetingInfo();
		mt.createid = UserConfig.clientUserId;	
		mt.topic = directTopic;
		mt.meetingType = directType;
		mt.startTime= (int)(startDate.getTime()/1000);
		mt.sidelineuserpwd = directPwd;
		mt.participants.addAll(this.selectedContacts);
		Utitlties.ShowProgressDialog(this.getActivity(), getResources().getString(R.string.Loading));
		MeetingMgr.getInstance().scheduleMeeting(mt);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("InlinedApi")
	/**
	 * �Ի���
	 */
	private void PopuWindows() {
		if(popuState == 0){//��ʼ����
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			View view = inflater.inflate(R.layout.dateselecter, null);//����

			final PopupWindow pop = new PopupWindow(view,LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, false);

			pop.setBackgroundDrawable(new BitmapDrawable());
			pop.setOutsideTouchable(true);
			pop.setFocusable(true);

			Button btnSet = (Button) view.findViewById(R.id.button_set);
			Button btnCancel = (Button) view.findViewById(R.id.button_cancel);

			final DatePicker dpicker = (DatePicker) view.findViewById(R.id.datePicker);
			btnSet.setOnClickListener(new OnClickListener() {

				@SuppressWarnings("deprecation")
				@Override
				public void onClick(View arg0) {
					startDate.setYear(dpicker.getYear() - 1900);
					startDate.setMonth(dpicker.getMonth());
					startDate.setDate(dpicker.getDayOfMonth());
					getUpdateTime();
					pop.dismiss();
				}
			});
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					pop.dismiss();
				}
			});
			pop.showAtLocation(mStartDateTv, Gravity.BOTTOM, 0, 0);

		}else if(popuState == 1){//��ʼʱ��
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			View view = inflater.inflate(R.layout.timeselecter, null);

			final PopupWindow pop = new PopupWindow(view,LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, false);

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

					startDate.setHours(tpicker.getCurrentHour());
					startDate.setMinutes(tpicker.getCurrentMinute());

					getUpdateTime();
					pop.dismiss();
				}
			});
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					pop.dismiss();
				}
			});
			pop.showAtLocation(mStartDateTv, Gravity.BOTTOM, 0, 0);

		}else if(popuState == 2){//ֱ������

			LayoutInflater inflater = LayoutInflater.from(getActivity());
			View view = inflater.inflate(R.layout.rangemeeting_repeat, null);

			final PopupWindow pop = new PopupWindow(view,LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, false);
			pop.setBackgroundDrawable(new BitmapDrawable());
			pop.setOutsideTouchable(true);
			pop.setFocusable(true);
			ListView listView = (ListView) view.findViewById(R.id.repeat_listiew);
			final String[] date = {getString(R.string.audio_video),getString(R.string.audio_video_ppt),getString(R.string.audio_ppt)};
			listView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.repeat_item_tv, date));

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					if(arg2 == 0){//��Ƶ+��Ƶ
						directType = 13;
						mDirectTypeTv.setText(R.string.audio_video);
					}else if(arg2 == 1){//��Ƶ+��Ƶ+PPT
						directType = 12;
						mDirectTypeTv.setText(R.string.audio_video_ppt);
					}else if(arg2 == 2){//��Ƶ+PPT
						directType = 11;
						mDirectTypeTv.setText(R.string.audio_ppt);
					}
					pop.dismiss();
				}
			});
			pop.showAtLocation(mStartDateTv, Gravity.BOTTOM, 0, 0);
		}
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
//			finishFragment();
		((LaunchActivity)getActivity()).onBackPressed();
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
	public void onDestroyView() {
		super.onDestroyView();
		info.emm.messenger.NotificationCenter.getInstance().removeObserver(this,MessagesController.meeting_list_update);
		info.emm.messenger.NotificationCenter.getInstance().removeObserver(this,MessagesController.meeting_create_failed);

	}

	@Override
	public void didReceivedNotification(int id, Object... args) {
		if(id == MessagesController.meeting_create_failed)
		{
			Toast.makeText(getActivity().getApplicationContext(),
					R.string.range_direct_faild, Toast.LENGTH_SHORT).show();
			Utitlties.HideProgressDialog(getActivity());
		}
		else if (MessagesController.meeting_list_update == id) 
		{
			Integer mid = (Integer) args[0];			
			Bundle bundle = new Bundle();
			bundle.putString("meetingid", mid.toString());			
			DirectDetails_Fragment infoFragment = new DirectDetails_Fragment();
			infoFragment.setArguments(bundle);
			((LaunchActivity)getActivity()).presentFragment(infoFragment, "", false);			
			((LaunchActivity)getActivity()).removeFromStack(RangeDirect_Fragment.this);
			Toast.makeText(getActivity().getApplicationContext(),R.string.range_direct_success, Toast.LENGTH_SHORT).show();
			Utitlties.HideProgressDialog(getActivity());			
		}
	}
	/**
	 * ���Ͻǵļ����Աmenu
	 */
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
	/**
	 * ��ӳ�Ա
	 */
	private void addMembers() {
		MessagesController.getInstance().selectedUsers.clear();//��ÿ�ΰ����µĻ��飬���
		MessagesController.getInstance().ignoreUsers.clear();
		if(selectedContacts.size() == 0){
			mAttendMemberLl.setVisibility(View.GONE);
			mAttendMemberTv.setText(0+"");
		}else{
			mAttendMemberLl.setVisibility(View.VISIBLE);
			mAttendMemberTv.setText(strNum+"");
			for(int i=0;i<selectedContacts.size();i++){
				int userid = selectedContacts.get(i);//��ȡid
				TLRPC.User user = MessagesController.getInstance().users.get(userid);//��ȡ����
				MessagesController.getInstance().ignoreUsers.put(userid, user);
			}
		}
		Intent intent = new Intent(this.parentActivity, CreateNewGroupActivity.class);//��ӳ�Ա
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
				mAttendMemberLl.setVisibility(View.VISIBLE);
				MessagesController.getInstance().ignoreUsers.clear();
				ArrayList<Integer> selectUsers = (ArrayList<Integer>)NotificationCenter.getInstance().getFromMemCache(2);//֮ǰѡ��ĳ�Ա
				selectedContacts.addAll(selectUsers);
				strNum = selectedContacts.size();
				mAttendMemberTv.setText(strNum+"");
				if(selectedContacts == null)
					return;				
			}
		}
	}
}

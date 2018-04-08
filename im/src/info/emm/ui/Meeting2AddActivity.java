package info.emm.ui;

import info.emm.LocalData.DateUnit;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.MessagesStorage;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.messenger.NotificationCenter.NotificationCenterDelegate;
import info.emm.ui.Views.BackupImageView;
import info.emm.ui.Views.BaseFragment;
import info.emm.ui.Views.PinnedHeaderListView;
import info.emm.ui.Views.SectionedBaseAdapter;
import info.emm.utils.ConstantValues;
import info.emm.utils.StringUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * �������
 * @author Administrator
 */
public class Meeting2AddActivity extends BaseFragment implements NotificationCenterDelegate ,OnClickListener{
	/**
	 * ѡ���û�ID
	 */
	//	private ArrayList<Integer> selectedContacts = new ArrayList<Integer>();
	/**
	 * editTextview������
	 */
	private EditText nameEditText;
	/**
	 * ѡ����ϵ�˵�List�б�
	 */
	private PinnedHeaderListView listView;
	/**
	 * �Ƿ�����ɼ�
	 */
	private boolean donePressed;
	/**
	 * �������
	 */
	private ProgressDialog progressDialog = null;
	/**
	 *  
	 */
	private boolean isFromMeetingDetail;
	private TLRPC.TL_PSTNMeeting pstnMeeting = null;
	private ArrayList<MeetingUser> meetingUsers = null;
	/**
	 * @Fields meetingUsers : key : phoneNum
	 */
	private Map<String, MeetingUser> meetingUsersMap = null;
	private String meetingID;
	private TextView doneTextView;
	private TextView startTimeView,tv_savetime,title2,title3;
	private EditText bubble_input_text;
	private ListAdapter listViewAdapter;
	private Button realTimeView;
	private Button endMeetingView;
	private Timer timer = new Timer();
	private boolean isCreateMeeting = false;
	private String startTimeString = "";
	private boolean bhostonline = false;

	//��ʱ
	private Handler stepTimeHandler;
	private long startTime = 0;
	private Runnable mTicker;
	//��������
	private String meeting_name;
	@Override
	public boolean onFragmentCreate() {
		super.onFragmentCreate();
		NotificationCenter.getInstance().addObserver(this, MessagesController.PSTNControl_Notify_error);
		NotificationCenter.getInstance().addObserver(this, MessagesController.PSTNControl_Notify);

		isFromMeetingDetail = getArguments().getBoolean("isFromMeetingDetail", false);//Ĭ�ϴӴ��������������false   ���������Ҫ����
		meetingID = getArguments().getString("meetingID");
		startTimeString = getArguments().getString("startTime");
		meetingUsersMap = new HashMap();
		meetingUsers = new ArrayList<MeetingUser>();
		System.out.println("=="+startTimeString);
		if (isFromMeetingDetail) 
		{
			loadMeetingInfo();
		} 
		else 
		{
			//        	��������
			newMemberToShow(0);
		}

		return true;
	}
	@Override
	public void onFragmentDestroy() {
		super.onFragmentDestroy();
		NotificationCenter.getInstance().removeObserver(this, MessagesController.PSTNControl_Notify_error);
		NotificationCenter.getInstance().removeObserver(this, MessagesController.PSTNControl_Notify);
		MessagesController.getInstance().ignoreUsers.clear();
		timer.cancel();
	}
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			setMeetingTime();
		}; 
	};
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (fragmentView == null) {
			fragmentView = inflater.inflate(R.layout.meeting2_create_final_layout, container, false);
			nameEditText = (EditText)fragmentView.findViewById(R.id.bubble_input_text);
			title2 = (TextView) fragmentView.findViewById(R.id.title2);
			title3 = (TextView) fragmentView.findViewById(R.id.title3);
			bubble_input_text = (EditText) fragmentView.findViewById(R.id.bubble_input_text);
			startTimeView = (TextView)fragmentView.findViewById(R.id.tv_starttime);
			tv_savetime = (TextView) fragmentView.findViewById(R.id.tv_savetime);
			listView = (PinnedHeaderListView)fragmentView.findViewById(R.id.listView);
			listView.setAdapter(listViewAdapter = new ListAdapter(parentActivity));

			if (isFromMeetingDetail) {
				nameEditText.setText(pstnMeeting.meettitle);
			} else {
				TLRPC.User hostUser = MessagesController.getInstance().users.get(UserConfig.clientUserId);
				String titleDefault = String.format(StringUtil.getStringFromRes(R.string.meeting_title_default), Utilities.formatName(hostUser));
				nameEditText.setText(titleDefault);
			}
			setMeetingTime();
			fragmentView.findViewById(R.id.btn_appointment).setOnClickListener(this);
			realTimeView = (Button)fragmentView.findViewById(R.id.btn_realtime);
			endMeetingView = (Button)fragmentView.findViewById(R.id.btn_endmeeting);		
			realTimeView.setOnClickListener(this);
			endMeetingView.setOnClickListener(this);
			listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> parent,
						View view, int position, long id) {
					int position1 = position-1;
						int userid = meetingUsers.get(position1).userId;
	                	if(userid==UserConfig.clientUserId){
	                		return false;
	                	}
	                	TLRPC.User user = MessagesController.getInstance().users.get(userid);
	                	String nickname = Utilities.formatName(user);
	    				deleteAlertDiaLog(user,position1,nickname);	
					return true;
				}	
			});

		} else {
			ViewGroup parent = (ViewGroup)fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		return fragmentView;
	}
	private void setMeetingTime() {
		if (isFromMeetingDetail) {
			startTimeView.setVisibility(View.VISIBLE);
			tv_savetime.setVisibility(View.VISIBLE);
			String  starttime = startTimeString;
			String callLength = "";
			if (pstnMeeting != null && !StringUtil.isEmpty(pstnMeeting.endtime)) {
				int seconds = DateUnit.getTimeDifferenceSeconds(DateUnit.strToDateLong(pstnMeeting.starttime), DateUnit.strToDateLong(pstnMeeting.endtime));
				String timeLongString = DateUnit.getTimeLengh(seconds);
				callLength = StringUtil.getStringFromRes(R.string.voicetime)+ "��" + timeLongString; 
				starttime = pstnMeeting.starttime;
			}

			startTimeView.setText(String.format(StringUtil.getStringFromRes(R.string.meeting_start_time), starttime));
			tv_savetime.setText(callLength);
			if (StringUtil.isEmpty(starttime)) {
				startTimeView.setVisibility(View.GONE);
				tv_savetime.setVisibility(View.GONE);
			}
		}
		if (donePressed == true) {
			startTimeView.setVisibility(View.VISIBLE);
			tv_savetime.setVisibility(View.VISIBLE);
			Date curDate = new Date(System.currentTimeMillis());//��ȡ��ǰʱ�� 
			startTimeView.setText(String.format(StringUtil.getStringFromRes(R.string.pstmeeting_start_time))+DateUnit.getStringDate(curDate));
			stepTimeHandler = new Handler();
			startTime = System.currentTimeMillis();
			mTicker = new Runnable() {
				@Override
				public void run() {
					String content = showTimeCount(System.currentTimeMillis() - startTime);
					tv_savetime.setText(String.format(StringUtil.getStringFromRes(R.string.voicetime)) + " : " + content);
					long now = SystemClock.uptimeMillis();
					long next = now + (1000 - now % 1000);
					stepTimeHandler.postAtTime(mTicker, next);
				}
			};
		}
	}
	private void deleteAlertDiaLog(final TLRPC.User user ,final int position, String name)
	{
		//wangxm todo..��ʾӦ���޸�
		String Tips = ApplicationLoader.applicationContext.getString(R.string.Tips);
		String msg = LocaleController.formatString( "IsRemoveMemberMeet", R.string.IsRemoveMemberMeet,name );
		String ok = ApplicationLoader.applicationContext.getString(R.string.OK);
		String Cancel = ApplicationLoader.applicationContext.getString(R.string.Cancel);
		new AlertDialog.Builder(parentActivity)
		.setTitle(Tips)
		.setMessage(msg)
		.setPositiveButton(ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{	
				meetingUsersMap.remove(getPhoneNum(user.phone));
				meetingUsers.remove(position);
				refreshAdapter();
				dialog.dismiss();
			}
		})
		.setNegativeButton(Cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.show();
	}

	//	private void muteDialog(final MeetingUser mUser) {
	//		final boolean toMute = ("1".equals(mUser.speakstate));
	//		showControlNiticeDialog(toMute ? "ȷ������" : "ȷ��ȡ������",toMute ? 8 : 5,mUser);
	//	}
	//	private void deleteDialog(final MeetingUser mUser) {
	//		showControlNiticeDialog("ȷ��ɾ��?", 6,mUser);
	//	}
	//	private void showControlNiticeDialog(String msg,final int commandNo,
	//			final MeetingUser mUser) {
	//		new AlertDialog.Builder(parentActivity).setTitle("��ʾ").setMessage(msg)
	//				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
	//					@Override
	//					public void onClick(DialogInterface dialog, int which) {
	//						controlMeetingUser(commandNo, mUser);
	//						dialog.dismiss();
	//					}
	//				})
	//				.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
	//					@Override
	//					public void onClick(DialogInterface dialog, int which) {
	//						dialog.dismiss();
	//					}
	//				}).show();
	//	}

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
		if (getActivity() == null) {
			return;
		}
		if (isCreateMeeting == true) {
			parentActivity.invalidateOptionsMenu();
		}
		((LaunchActivity)parentActivity).showActionBar();
		((LaunchActivity)parentActivity).updateActionBar();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

//		if (isFromMeetingDetail) {
			inflater.inflate(R.menu.group_create_menu, menu);
			SupportMenuItem doneItem = (SupportMenuItem)menu.findItem(R.id.done_menu_item);
			doneTextView = (TextView)doneItem.getActionView().findViewById(R.id.done_button);
			doneTextView.setText(R.string.Add);
//		} else{
//			doneTextView.setVisibility(View.GONE);
//		}
			doneTextView.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					addMemberToMeet();
				}
				});
//			doneTextView.setVisibility(View.GONE);
		super.onCreateOptionsMenu(menu, inflater);
	}
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (donePressed == true || isCreateMeeting == true) {
			 menu.clear();
		}
		super.onPrepareOptionsMenu(menu);
	}
	/**
	 * ��ʾloading��Ϣ
	 */
	private void processDiaLog(){
		progressDialog = new ProgressDialog(parentActivity);
		progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setCancelable(false);

		progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, LocaleController.getString("Cancel", R.string.Cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				donePressed = false;
				try {
					dialog.dismiss();
				} catch (Exception e) {
					FileLog.e("emm", e);
				}
			}
		});
		progressDialog.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int item_id = item.getItemId();
		switch (item_id) {
		case android.R.id.home:
			DeletAlterDialog();
			break;

		default:
			break;
		}
		return true;
	}
	public boolean onBackPressed() {
		if (isCreateMeeting == true) {
			DeletAlterDialog();
			return false;
		}
		return super.onBackPressed();

	}
	@Override
	public void applySelfActionBar() {
		if (parentActivity == null) {
			return;
		}
		ActionBar actionBar =  super.applySelfActionBar(true);

		TextView title = (TextView)parentActivity.findViewById(R.id.action_bar_title);
		if (title == null) {
			final int subtitleId = parentActivity.getResources().getIdentifier("action_bar_title", "id", "android");
			title = (TextView)parentActivity.findViewById(subtitleId);
		}
		if (title != null) {
			title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			title.setCompoundDrawablePadding(0);
		}

		if (isCreateMeeting == true) {
			actionBar.setTitle(meeting_name);
		}else{
			if (isFromMeetingDetail) {
				actionBar.setTitle(LocaleController.getString("meetingDetail", R.string.MeetingDetail));
			} else {
				actionBar.setTitle(LocaleController.getString("meeting_add", R.string.MeetingAdd));
			}
		}
		((LaunchActivity)parentActivity).fixBackButton();
	}
	private void progressDialogDismiss() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		donePressed = false;
	}
	@Override
	public void didReceivedNotification(int id, Object... args) {
		if (id == MessagesController.PSTNControl_Notify) {
			TLRPC.TL_PSTNResponse response = (TLRPC.TL_PSTNResponse)args[0];
			if (response.nAction == 1) { //�����ɹ�
				progressDialogDismiss();
				if (response.nResult == -4) {
					String alertMsg = ApplicationLoader.applicationContext.getString(R.string.meeting_nomoney_notice);
					Utilities.showToast(parentActivity, alertMsg);
				}else {
					meetingID = response.newconferenceId;
					timer.schedule(updateTask, 5000l, 3000l);
					realTimeView.setVisibility(View.GONE);
					endMeetingView.setVisibility(View.VISIBLE);
				}
				mTicker.run();
				isFromMeetingDetail = false;
				isCreateMeeting = true;
				title3.setVisibility(View.GONE);
				bubble_input_text.setVisibility(View.GONE);
				title2.setVisibility(View.VISIBLE);
				title2.setText(LocaleController.getString("MEETING_MEMBERS", R.string.meeting_members)+meetingUsers.size());
				((LaunchActivity)parentActivity).showActionBar();
				((LaunchActivity)parentActivity).updateActionBar();
			}else if (response.nAction == 4) {
				if ("9".equals(response.PSTNm.lastControlCmd)) {
					finishFragment();
				}else	if ("5".equals(response.PSTNm.lastControlCmd)|| "8".equals(response.PSTNm.lastControlCmd)) {
					String phoneNum = response.PSTNm.lastControlString.split(",")[0];
					meetingUsersMap.get(getPhoneNum(phoneNum)).speakstate = "5".equals(response.PSTNm.lastControlCmd)?"1":"2";
				}
			}else if (response.nAction == 6) {
				ArrayList<TLRPC.TL_PSTNUserStatus> statuslist = response.statuslist;
				for (int i = 0; i < statuslist.size(); i++) {
					TLRPC.TL_PSTNUserStatus userStatus = statuslist.get(i);
					meetingUsersMap.get(getPhoneNum(userStatus.phone)).callstatus = userStatus.callstate;
					////��������˵�״̬�ǹҶϣ������ҳ��
					if(getPhoneNum(UserConfig.phone).equals(userStatus.phone)){
						if( "2".equals(userStatus.callstate) || "3".equals(userStatus.callstate)){
							finishFragment();
						}
						else if("1".equals(userStatus.callstate)){
							bhostonline = true;
						}
						else {
							bhostonline = false;
						}
					}
				}

			}else if (response.nAction == 9) {
				if (pstnMeeting != null ) {
					if (!StringUtil.isEmpty(pstnMeeting.endtime)) {
						MessagesStorage.getInstance().updateMeeting2(pstnMeeting);
						isFromMeetingDetail = true;
						Message msg = new Message();
						msg.what = 1;
						handler.handleMessage(msg);
					}
				}

			}
			refreshAdapter();
		}else if (id == MessagesController.PSTNControl_Notify_error) 
		{
			progressDialogDismiss();
			int nAction = (Integer)args[0];
			TLRPC.TL_PSTNResponse response = (TLRPC.TL_PSTNResponse)args[1];
			if (nAction == 1) {
				String alertMsg = ApplicationLoader.applicationContext.getString(R.string.CreateMeetingFaild);
				Utilities.showToast(parentActivity, alertMsg);
			}else if (nAction == 6) {
				timer.cancel();
				stepTimeHandler.removeCallbacks(mTicker);
			}

		}		
		else if (id == MessagesController.meeting_list_update) 
		{
			if (progressDialog != null) 
			{
				try {
					progressDialog.dismiss();
				} catch (Exception e) {
					FileLog.e("emm", e);
				}
			}

			int result = (java.lang.Integer)args[0];
			String alertMsg="";
			if( result == -1)
			{
				alertMsg = LocaleController.getString("WaitingForNetwork", R.string.WaitingForNetwork);
			}
			else if(result == 1)
			{
				alertMsg = LocaleController.getString("AccessServerFaildRetry", R.string.AccessServerFaildRetry);
			}
			if( result == 1 || result == -1)
			{
				Utilities.showAlertDialog(parentActivity, alertMsg);
			}
			else
			{
				refreshAdapter();
				donePressed = false;
				finishFragment(true);
			}	
		}
		else if (id == MessagesController.meet_infos_needreload) 
		{
			loadMeetingInfo();
			refreshAdapter();
		}
		else if (id == MessagesController.meet_member_operator_error) 
		{
			int errorId = (Integer)args[0];
			if (errorId == 1 || errorId == -1) {
				if (parentActivity != null) {
					String msg = LocaleController.getString("MeetOperatorFaild", R.string.MeetOperatorFaild);
					Utilities.showToast(parentActivity, msg);
				}
			}
		}
	}

	private void loadMeetingInfo()
	{
		pstnMeeting = MessagesController.getInstance().meeting2Map.get(meetingID);
		if (pstnMeeting == null) {
			pstnMeeting = new TLRPC.TL_PSTNMeeting();
		}
		String userIDs = UserConfig.clientUserId + "," + pstnMeeting.userIdList;
		String[] userId = userIDs.split(",");
		int size = userId.length;
		for (int i = 0; i < size; i++) {
			TLRPC.User user = MessagesController.getInstance().users.get(Integer.parseInt(userId[i]));
			if (user != null) {
				MeetingUser mUser = new MeetingUser();
				mUser.userId = user.id;
				meetingUsersMap.put(getPhoneNum(user.phone),mUser);
				meetingUsers.add(mUser);
			}
		}
		if (StringUtil.isEmpty(pstnMeeting.endtime)) {
			MessagesController.getInstance().ControlPSTNMeeting(9, pstnMeeting);
		}
		//		for (int i = 0; i < size; i++) {
		//			for (Map.Entry<Integer, TLRPC.User> entry : MessagesController.getInstance().users.entrySet()) {
		//				TLRPC.User user = entry.getValue();
		//				if (phones[i].equals(user.phone)) {
		//					MeetingUser mUser = new MeetingUser();
		//					mUser.userId = user.id;
		//					meetingUsersMap.put(getPhoneNum(user.phone),mUser);
		//					meetingUsers.add(mUser);
		//					break;
		//				}
		//			}
		//		}
	}
	/**
	 * @Title: getPhoneNum
	 *
	 * @Description: ���� 86 ȥ��
	 *
	 * @param phone 
	 * @return 
	 */
	public String getPhoneNum(String phone){
		String phoneNum = phone;
		if(phoneNum.startsWith("+86")){
			phoneNum = phoneNum.replace("+86", "");
		}
		return phoneNum;
	} 
	/**
	 * ��ʼ�ͽ���ʱ��ѡ��ı༭��
	 * @author wangxm
	 */

	private class ListAdapter extends SectionedBaseAdapter {
		private Context mContext;

		public ListAdapter(Context context) {
			mContext = context;
		}

		@Override
		public Object getItem(int section, int position) {
			return null;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int position) 
		{
			return true;
		}

		@Override
		public long getItemId(int section, int position) {
			return 0;
		}

		@Override
		public int getSectionCount() {
			return 1;
		}

		@Override
		public int getCountForSection(int section) {
			if (meetingUsers == null) {
				return 0;
			}
			return meetingUsers.size();
		}
		@Override
		public View getItemView(int section, int position, View convertView, ViewGroup parent) 
		{
			ViewHolder holder = null;
			if (null == convertView) 
			{
				holder = new ViewHolder();
				convertView = LayoutInflater.from(parentActivity).inflate( R.layout.meeting_user_item, null );
				holder.mUserImg = (BackupImageView) convertView
						.findViewById(R.id.settings_avatar_image);
				holder.nameTextView = (TextView)convertView.findViewById(R.id.tv_name);
				holder.phoneTextView = (TextView)convertView.findViewById(R.id.tv_phone);
				holder.statusView = (ImageView)convertView.findViewById(R.id.iv_status);
				holder.userAction = (TextView)convertView.findViewById(R.id.tv_action);
			} else {
				holder = (ViewHolder)convertView.getTag();
			} 
			final MeetingUser mUser = meetingUsers.get(position);
			TLRPC.User user = MessagesController.getInstance().users.get(mUser.userId);
			if (isCreateMeeting) {
				final int commandNo = meetingUserStatus(holder.statusView,holder.userAction ,mUser.callstatus);
				holder.userAction.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (commandNo != -1) {
							controlMeetingUser(commandNo, mUser);
						}
					}
				});
			} 
			SetAvatar(user, holder.mUserImg);

			String nameString = Utilities.formatName(user);
			holder.phoneTextView.setText(user.phone);
			if (user.id == UserConfig.clientUserId) {
				nameString = nameString + "("+StringUtil.getStringFromRes(R.string.meeting_host)+")";
			} 
			holder.nameTextView.setText(nameString);
			convertView.setTag(holder);
			return convertView;
		}
		private void SetAvatar(TLRPC.User user,BackupImageView avatarImage) {
			if (null != user) {
				if (user.photo instanceof TLRPC.TL_userProfilePhotoEmpty) {
					int state = MessagesController.getInstance().getUserState(
							user.id);
					if (state == 0)
						avatarImage.setImageResource(R.drawable.user_gray);
					else
						avatarImage.setImageResource(R.drawable.user_blue);

				} else
					avatarImage.setImage(user.photo.photo_small, "50_50",
							Utilities.getUserAvatarForId(user.id));
			} else {
				avatarImage.setImageResource(R.drawable.user_blue);
			}
		}
		@Override
		public int getItemViewType(int section, int position) {
			return 0;
		}

		@Override
		public int getItemViewTypeCount() {
			return 1;
		}

		@Override
		public int getSectionHeaderViewType(int section) {
			return 0;
		}

		@Override
		public int getSectionHeaderViewTypeCount() {
			return 1;
		}

		@Override
		public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = li.inflate(R.layout.settings_section_layout, parent, false);
				convertView.setBackgroundColor(0xffffffff);
				if (donePressed == true) {
					convertView.setVisibility(View.GONE);
				}
			}
			//wangxm ��ʾ������Ա �»���
			TextView textView = (TextView)convertView.findViewById(R.id.settings_section_text);
			if (meetingUsers.size() == 1) {
				textView.setText(meetingUsers.size() + " " + LocaleController.getString("MEMBER", R.string.MEMBER));
			} else {
				textView.setText(meetingUsers.size() + " " + LocaleController.getString("MEMBERS", R.string.MEMBERS));
			}	

			return convertView;
		}
	}
	public void addMemberToMeet(){
		int size = meetingUsers.size();
		MessagesController.getInstance().ignoreUsers.clear();
		MessagesController.getInstance().selectedUsers.clear();
		for (int i = 0; i < size; i++) {
			TLRPC.User user = MessagesController.getInstance().users.get(meetingUsers.get(i).userId);
			MessagesController.getInstance().ignoreUsers.put(user.id, user);	
		}
		Intent intent = new Intent(this.parentActivity, CreateNewGroupActivity.class);
		Bundle bundle = new Bundle();
		bundle.putBoolean("isMeetingCreate", true);
		bundle.putBoolean("AddGroupUser", true);
		intent.putExtras(bundle);
		startActivityForResult(intent, 101);
	}
	/**
	 * @Title: newMemberToShow
	 *
	 * @Description: TODO
	 *
	 * @param state 0 �������� 1 ��ӳ�Ա
	 * @param selectedContacts
	 */
	private void newMemberToShow(int state) {
		ArrayList<Integer> selectedContacts = (ArrayList<Integer>)NotificationCenter.getInstance().getFromMemCache(2);
		if (state == 0) {
			selectedContacts.add(0,UserConfig.clientUserId);
		}
		for (int i = 0; i < selectedContacts.size(); i++) {
			MeetingUser mUser = new MeetingUser();
			TLRPC.User user = MessagesController.getInstance().users.get(selectedContacts.get(i));
			mUser.userId = user.id;
			meetingUsersMap.put(getPhoneNum(user.phone),mUser);
			meetingUsers.add(mUser);
		}
	}
	private int meetingUserStatus(ImageView statusView,TextView userControl, String status) {
		statusView.setVisibility(View.VISIBLE);

		if(bhostonline)
			userControl.setVisibility(View.VISIBLE);
		else
			userControl.setVisibility(View.INVISIBLE);

		if ("0".equals(status)) { // ������
			statusView.setImageResource(R.drawable.meeting_user_calling);
			userControl.setText(R.string.meeting_user_action_hangup);
			return 6;
		}else if ("1".equals(status)) { //��ͨ
			statusView.setImageResource(R.drawable.meeting_user_call);
			userControl.setText(R.string.meeting_user_action_hangup);
			return 6;
		}else if ("2".equals(status)||"3".equals(status)) { //ֱ�ӹҶ� ��ͨ��Ҷ�
			statusView.setImageResource(R.drawable.meetinguser_hungup);
			userControl.setText(R.string.meeting_user_action_call);
			return 5;
		}else {
			statusView.setVisibility(View.INVISIBLE);
			//			statusView.setImageResource(R.drawable.meetcalldefault);
			userControl.setText("");
			return -1;
		}
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);		
		if (resultCode != Activity.RESULT_OK) {
			return;
		}		
		if (requestCode == 101)  {
			newMemberToShow(1);
			MessagesController.getInstance().ignoreUsers.clear();
			refreshAdapter();
		}else if (requestCode == ConstantValues.OAR_AppointmentMeeting) {
			int appintmentTime = data.getIntExtra("appintmentTime", 0);
			startAppointmentMeeting(appintmentTime);
		}
	}
	/**
	 * @Title: startRealTimeMeeting
	 *
	 * @Description: ����ʱ����
	 *
	 */
	private void startRealTimeMeeting() {
		createMeeting(0,(int)(System.currentTimeMillis()/1000));
	}
	private void startAppointmentMeeting(int appointmentTime) {
		createMeeting(1,appointmentTime);

	}
	/**
	 * @Title: createMeeting
	 *
	 * @Description: �����»���
	 *
	 * @param meetType ��������
	 * @param meetTime ʱ��
	 */
	private void createMeeting(int meetType,int meetTime) {

		if (donePressed) {
			return;
		}
		donePressed = false;
		meeting_name = nameEditText.getText().toString();

		TLRPC.TL_PSTNMeeting PSTNm = new TLRPC.TL_PSTNMeeting();
		PSTNm.meettitle = meeting_name;
		PSTNm.meetcall = getPhoneNum(MessagesController.getInstance().users.get(UserConfig.clientUserId).phone);
		int size = meetingUsers.size();
		for (int i = 0; i < size; i++) {
			TLRPC.User user = MessagesController.getInstance().users.get(meetingUsers.get(i).userId);
			PSTNm.allUserId.add(user.id);
			if (user.id == UserConfig.clientUserId) {
				continue;
			}
			PSTNm.userIdList += user.id;
			if (i < size-1) {
				PSTNm.userIdList += ",";
			}
		}
		PSTNm.meettype = meetType;
		PSTNm.meettime = meetTime;
		PSTNm.meetsmsflag = 0;
		PSTNm.meetrecflag = 0;
		PSTNm.Autocallnum = 0;
		PSTNm.shutupflag = 0;
		MessagesController.getInstance().ControlPSTNMeeting(1, PSTNm);
		processDiaLog();
	}
	/**
	 * @Title: controlMeetingUser
	 *
	 * @Description: �������
	 *
	 * @param commandNo 5=���Լ��룬6=�߳����飬8=�������룬9=�������飬12=׷������
	 * @param userInfo
	 */
	private void controlMeetingUser(int commandNo,MeetingUser mUser) {
		String userInfo = "";
		if (mUser != null) {
			TLRPC.User user = MessagesController.getInstance().users
					.get(mUser.userId);
			if (user != null) {
				String phoneNum = getPhoneNum(user.phone);
				userInfo = phoneNum + "," + Utilities.formatName(user);
			}
		}
		TLRPC.TL_PSTNMeeting PSTNm = new TLRPC.TL_PSTNMeeting();
		PSTNm.conferenceId = meetingID;
		PSTNm.lastControlCmd = ""+commandNo;
		PSTNm.lastControlString = ""+userInfo;
		MessagesController.getInstance().ControlPSTNMeeting(4, PSTNm);
		stepTimeHandler.removeCallbacks(mTicker);
	}
	private void doUpdateMeetingUserState() {
		TLRPC.TL_PSTNMeeting PSTNm = new TLRPC.TL_PSTNMeeting();
		PSTNm.conferenceId = meetingID;
		MessagesController.getInstance().ControlPSTNMeeting(6, PSTNm);
	}
	private void refreshAdapter() {
		if(listViewAdapter!=null)
			listViewAdapter.notifyDataSetChanged();
	}
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_appointment) {
			Intent intent = new Intent(parentActivity,
					CreateNewGroupActivity.class);
			Bundle bundle = new Bundle();
			bundle.putBoolean("appointmentMeeting", true);
			intent.putExtras(bundle);
			startActivityForResult(intent, ConstantValues.OAR_AppointmentMeeting);

		} else if (id == R.id.btn_realtime) {
			if (meetingUsers.size() > 1) {
				startRealTimeMeeting();
				donePressed = true;
				setMeetingTime();
				parentActivity.invalidateOptionsMenu();
			} else {
				String alertMsg = ApplicationLoader.applicationContext.getString(R.string.CreateMeetingPST);
				Utilities.showToast(parentActivity, alertMsg);
				return;
			}

		} else if (id == R.id.btn_endmeeting) {
			controlMeetingUser(9, null);
			stepTimeHandler.removeCallbacks(mTicker);

		} else {
		}
	}
	class ViewHolder{
		public BackupImageView mUserImg = null; 
		TextView nameTextView;
		TextView phoneTextView;
		ImageView statusView;
		TextView userAction;
	}
	TimerTask updateTask = new TimerTask(){
		@Override
		public void run() {
			doUpdateMeetingUserState();
		}  
	};

	//����dialog��������
	public void DeletAlterDialog(){
		if (isCreateMeeting == true) {
			new AlertDialog.Builder(parentActivity)
			.setTitle(ApplicationLoader.applicationContext.getString(R.string.Tips))
			.setMessage(ApplicationLoader.applicationContext.getString(R.string.meeting_end))
			.setPositiveButton(ApplicationLoader.applicationContext.getString(R.string.OK)
					, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					controlMeetingUser(9, null);
					finishFragment();
					refreshAdapter();
					dialog.dismiss();
					stepTimeHandler.removeCallbacks(mTicker);
				}
			})
			.setNegativeButton(ApplicationLoader.applicationContext.getString(R.string.Cancel)
					, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			}).show();

		}else{
			finishFragment();
		}
	}
	public String showTimeCount(long time) {
		if(time >= 360000000){
			return "00:00:00";
		}
		String timeCount = "";
		long hourc = time/3600000;
		String hour = "0" + hourc;
		hour = hour.substring(hour.length()-2, hour.length());

		long minuec = (time-hourc*3600000)/(60000);
		String minue = "0" + minuec;
		minue = minue.substring(minue.length()-2, minue.length());

		long secc = (time-hourc*3600000-minuec*60000)/1000;
		String sec = "0" + secc;
		sec = sec.substring(sec.length()-2, sec.length());
		timeCount = hour + ":" + minue + ":" + sec;
		return timeCount;
	}
	class MeetingUser {
		String callstatus = ""+-1; //0=�����У�1=��ͨ��2=�Ҷ�
		int userId;
		String speakstate = ""+1; // 1=���ԣ�2=���� 
	}
}


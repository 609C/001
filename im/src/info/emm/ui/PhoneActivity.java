package info.emm.ui;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;




import java.util.TimerTask;

import info.emm.messenger.FileLog;
import info.emm.messenger.IMRtmpClientMgr;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.TLRPC;
import info.emm.services.UEngine;
import info.emm.ui.Views.InCallHeadView;
import info.emm.ui.Views.InCallHeadView.HeadStatus;
import info.emm.ui.Views.SquareLayout;
import info.emm.utils.StringUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.media.AudioManager;


public class PhoneActivity extends Activity implements OnClickListener,
		NotificationCenter.NotificationCenterDelegate {
	private LinearLayout call_add_layout;
	private TextView joinTv, have_phoneTV;
	private LinearLayout incallLayout;
	private LinearLayout incomingLayout;
	private GridLayout memberLayout;
	private ImageView backBtn;
	private TextView callTitle;
	private TextView handFree;
	private TextView muteTv;
	private ImageView addMemberBtn;
	private ImageView callUserBtn;
	private SquareLayout handFreeBtn;
	private SquareLayout muteBtn;
	private RelativeLayout hangUpBtn;
	private RelativeLayout acceptBtn;
	private RelativeLayout refuseBtn;

	//private IMRtmpClientMgr meeting = IMRtmpClientMgr.getInstance();
	public String meeting_id;
	public int mid = 0;
	public int userId = 0;
	public int chatId = 0;
	public int callType = 0;
	public int fromid = 0;
	public int dialog_id=0;

	private TLRPC.ChatParticipants chatInfo;
	private ArrayList<Integer> selectedContacts;

	private String nickName;
	private String meetingname;
	private boolean meeting_connection_status = false;
	private boolean wantPlayAudio = true;
	private View fragmentView;
	private boolean bHandFree = true;
	private boolean peerCancel = false;
	private boolean normalQuit = false;
	private boolean bRefuse = false;

	private boolean topCreateGroupCall = false;


	Timer msgTimer;
	public StringBuffer noticeInfo = null;

	private String titleBarMsg = "";
	private long lastShowMsgTime = 0;

	private Map<Integer, InCallHeadView> userInMeetingMap;
	private boolean islogout = false; // ���߳�

	Timer serviceTimer = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		NotificationCenter.getInstance().addObserver(this, 1234);
		NotificationCenter.getInstance().addObserver(this, MessagesController.meeting_call_response);
		
		setContentView(R.layout.outgoing_layout);
		initView();
		//IMRtmpClientMgr.getInstance().phoneIsTop = true;

		//if (!meeting.inMeeting)
			//MediaController.getInstance().stopAudio();

		getWindow().addFlags(
				LayoutParams.FLAG_TURN_SCREEN_ON
						| LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| LayoutParams.FLAG_KEEP_SCREEN_ON
						| LayoutParams.FLAG_DISMISS_KEYGUARD);

	

		setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
		startTimer();

	}

	private void getExtra() {
		Intent intent = this.getIntent();
		meeting_id = intent.getStringExtra("meetingId");// ����ID
		chatId = intent.getIntExtra("chatId", 0);// ��ʾ��ͨ��
		topCreateGroupCall = intent
				.getBooleanExtra("topCreateGroupCall", false);
		if (chatId != 0) {
			// ���chatId!=0��ʾ����У�fromid��ʾ������
			fromid = intent.getIntExtra("userId", 0);
			dialog_id = -chatId;
		} else {
			// ��ʾΪ1��1ͨ����userIdΪ������ID
			userId = intent.getIntExtra("userId", 0);
			fromid = userId;
			dialog_id = userId;
		}

		callType = intent.getIntExtra("callType", 0);// ��ʾΪ0��ʾ��������,Ϊ1��ʾ���������
		if (StringUtil.isNum(meeting_id))
			mid = Integer.parseInt(meeting_id);

		if (userId != 0) {
			// �����е�ʱ����Ҫ��ʾ�����ߵ�ͷ��
			addMeetingUser(fromid, HeadStatus.OnLine);
		} else if (chatId != 0) {
			if (callType == 0) {
				// �����е�ʱ����Ҫ��ʾ�����ߵ�ͷ��
				addMeetingUser(fromid, HeadStatus.OnLine);
			}
		}		
		updateView(0);	
	}



	@Override
	public void onStop() 
	{	
		super.onStop();
	}

	private void release() {
		// TODO Auto-generated method stub		
		NotificationCenter.getInstance().removeObserver(this, 1234);
		NotificationCenter.getInstance().removeObserver(this, MessagesController.meeting_call_response);
		// ���۰�home,back����rufuse button����ִ���������,������ڻ����е�ʱ��
		//if (!meeting.inMeeting)
			//refuseCall();
		// ֹͣ����
		//meeting.stopdial();
	}
	public void stopTimer()
	{
		if(serviceTimer!=null)
		{
			serviceTimer.cancel();
			serviceTimer=null;
		}
		
	}
	public void startTimer() 
	{
		if(serviceTimer==null)
			serviceTimer = new Timer();
		serviceTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {						
						
						String temp = LocaleController.getString("youhavecall", R.string.youhavecall);
						MessagesController.getInstance().sendSystemMsg(userId, chatId, temp, true);
						refuseCall();			
						stopTimer();
						finish();
					}
				});
			}
		}, 60000);
	}

	@Override
	public void onPause() {
		super.onPause();
		//tearDown();
		UEngine.getInstance().getSoundService().stopSound();
		if (!islogout) {
			MessagesController.getInstance().showCallNotification();
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		if (userInMeetingMap == null) {
			userInMeetingMap = new HashMap<Integer, InCallHeadView>();
		}
		if (noticeInfo == null) {
			noticeInfo = new StringBuffer();
		}
		/*if (have_phoneTV != null) {
			have_phoneTV
					.setVisibility(IMRtmpClientMgr.getInstance().obIsCall == 0 ? View.VISIBLE
							: View.GONE);
		}*/
		//if (checkMeetingState())
			//backBtn.setVisibility(View.VISIBLE);
		getExtra();
		
		MessagesController.getInstance().cancelCallNotif();
		//if (IMRtmpClientMgr.getInstance().getCurrentCallStatus() == CallStatus.Called)
		UEngine.getInstance().getSoundService().stopSound();
		UEngine.getInstance().getSoundService().playSound();			
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		release();
		if (userId != 0 || chatId != 0) {
			if (msgTimer != null) {
				msgTimer.cancel();
				msgTimer = null;
			}
			if (memberLayout != null)
				memberLayout.removeAllViews();
		}
		MessagesController.getInstance().cancelCallNotif();
		//disableProximitySensor(false);
		IMRtmpClientMgr.getInstance().setReceiveCall(false);
	}

	private void updateView(int statusType) {
		incallLayout.setVisibility(View.GONE);
		incomingLayout.setVisibility(View.GONE);
		call_add_layout.setVisibility(View.INVISIBLE);
		joinTv.setVisibility(View.GONE);
		switch (statusType) {
		case 0: // ����
			isInComingView();
			break;
		case 1: // ͨ����
			/*if (!meeting.inMeeting) {
				UEngine.getInstance().getSoundService().stopSound();
				toConectMeeting();
			}
			isInCallView();*/
			break;
		default:
			break;
		}
	}

	private void isInCallView() {
		incallLayout.setVisibility(View.VISIBLE);
	}

	private void isInComingView() {
		//setCallTitle(CallStatus.None);
		incomingLayout.setVisibility(View.VISIBLE);
		
		String temp = LocaleController.getString("voicechattip",
				R.string.voicechattip);
		callTitle.setText(temp);
	}

	private void initView() {
		incallLayout = (LinearLayout) findViewById(R.id.linlay_incall);
		incomingLayout = (LinearLayout) findViewById(R.id.linlay_incoming);
		memberLayout = (GridLayout) findViewById(R.id.gridlay_members);
		fragmentView = findViewById(R.id.relativeLayoutMain);
		memberLayout.setVisibility(View.VISIBLE);
		callTitle = (TextView) findViewById(R.id.tv_title);
		backBtn = (ImageView) findViewById(R.id.iv_back);
		handFree = (TextView) findViewById(R.id.tv_handfree);
		muteTv = (TextView) findViewById(R.id.tv_mute);
		addMemberBtn = (ImageView) findViewById(R.id.iv_addmem);
		callUserBtn = (ImageView) findViewById(R.id.iv_call);
		call_add_layout = (LinearLayout) findViewById(R.id.linlay_group);
		joinTv = (TextView) findViewById(R.id.tv_join);
		have_phoneTV = (TextView) findViewById(R.id.tv_have_phone);
		setOnClickListener(R.id.iv_back, R.id.iv_addmem, R.id.iv_call,
				R.id.sqlay_handfree, R.id.sqlay_mute, R.id.relay_accept,
				R.id.relay_hangup, R.id.relay_refuse, R.id.tv_join);

		backBtn.setVisibility(View.INVISIBLE);
	}

	private void setOnClickListener(Integer... arg) {
		for (Integer id : arg) {
			findViewById(id).setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.relay_accept) {// �����˺����ң��ҿ��Խ���
			acceptCall();

		} else if (id == R.id.relay_refuse) {// �����˺����ң��ҿ��Ծܾ�
			FileLog.e("emm", "R.id.relay_refuse");
			//destroyUI(true);
			refuseCall();

		} else if (id == R.id.tv_join) {
		}
	}

	private void acceptCall() {

		UEngine.getInstance().getSoundService().stopSound();
		IMRtmpClientMgr.getInstance().stopTimer();
		stopTimer();		
		ApplicationLoader.getInstance().joinInstMeeting(this, meeting_id,dialog_id);
		finish();
	}

	private void refuseCall() {
		// ����ܾ���ֱ��return,�˺��������ظ�ִ��
		/*meeting.bReceiveCall = false;
		
		FileLog.d("emm", "refuse call");
		if (normalQuit)
			return;
			*/
		
		if (bRefuse)
			return;
		bRefuse = true;
		UEngine.getInstance().getSoundService().stopSound();
		IMRtmpClientMgr.getInstance().stopTimer();
		stopTimer();
		if (callType == 0) 
		{
			if (userId != 0) 
			{
				//ֻ��1��1��ʾ
				String temp = LocaleController.getString("voicerefuse",R.string.voicerefuse);
				MessagesController.getInstance().sendSystemMsg(userId, chatId,temp, true);
				//���;ܾ���Ϣ��OK		
				ArrayList<Integer> users = new ArrayList<Integer>();
				users.add(userId);
				MessagesController.getInstance().meetingCall(meeting_id,chatId, users, 2);
			}
		}		
		
		finish();
	}

	@Override
	public void didReceivedNotification(int id, Object... args) {
		if (id == 1234) {
			islogout = true;
			finish();
		}
		if (id == MessagesController.meeting_call_response) {
			// ��ʾ�Է�æ��ܾ�
			int reason = (Integer) args[0];
			// 2��ʾ�Է��ܾ���3��ʾ�Է�æµ,4��ʾ�Է�����ͨ��
			if (userId != 0) {
				TLRPC.User user = MessagesController.getInstance().users.get(userId);
				if (user != null) 
				{
					if (reason == 1) 
					{
						// ȡ������
						String temp = LocaleController.getString("youhavecall",R.string.youhavecall);
						MessagesController.getInstance().sendSystemMsg(userId,chatId, temp, true);
						FileLog.e("emm", "meeting_call_response 1");
						UEngine.getInstance().getSoundService().stopSound();
						IMRtmpClientMgr.getInstance().stopTimer();
						stopTimer();
						finish();
					}
				}
			}
		}
		/*if (id == MessagesController.meeting_call_end) {
			FileLog.e("emm", "meeting_call_end");
			normalQuit = true;
			destroyUI(true);
		} else if (id == MessagesController.meeting_connected) {
			int rc = (Integer) args[0];
			if (rc == -1) {
				String alertMsg = ApplicationLoader.applicationContext
						.getString(R.string.WaitingForNetwork);
				Utilities.showToast(this, alertMsg);
				return;
			}
			String msg = ApplicationLoader.applicationContext
					.getString(R.string.MeetingServerConnectSuccess);
			if (msg != null)
				Utilities.showToast(this, msg);
			meeting_connection_status = true;
			// callTitle.setText("�Ѿ��ɹ����ӷ�����");
		} else if (id == MessagesController.meeting_disconnected) {
			String msg = ApplicationLoader.applicationContext
					.getString(R.string.WaitingForNetwork);
			if (msg != null)
				Utilities.showToast(this, msg);
			meeting_connection_status = false;
		} else if (id == MessagesController.meeting_enablePresence) {
			// ���Ѿ�������飬���������ҷ�����һ��meetBuddyId
			int meetBuddyId = (Integer) args[0];
			// ���û������ԣ��ָ�UI��ʱ��ʹ�ü�chatActivity�Ƿ���ʾͨ���Ľ���ֻҪ���⼸��ֵ������
			removeMeetingUser(fromid);
			if (chatId != 0)
				addMeetingUser(UserConfig.clientUserId, HeadStatus.OnLine);
			else
				addMeetingUser(userId, HeadStatus.OutLine);
			backBtn.setVisibility(View.VISIBLE);
			// ����ͷ�����Ƶ
			meeting.publishAudio();
		} else if (id == MessagesController.meeting_userin_nobody) {
			// ����״̬��rtmpclientmgr.java��ά��
			// �������û��������������û�
			if (userId != 0) {
				// 1�� 1����ĳ���û�
				ArrayList<Integer> users = new ArrayList<Integer>();
				users.add(userId);
				MessagesController.getInstance().meetingCall(meeting_id,
						chatId, users, 0);
				setCallTitle(CallStatus.Calling);
				IMRtmpClientMgr.getInstance().setCurrentCallStatus(
						CallStatus.Calling);
				meeting.dial(ToneGenerator.TONE_SUP_RINGTONE);
				// ������ʱ��,һ���ӳ�ʱ
				meeting.startCallingTimer();

				// ��ʼ���б���
			} else if (chatId != 0) {
				if (topCreateGroupCall) {
					selectedContacts = (ArrayList<Integer>) NotificationCenter
							.getInstance().getFromMemCache(5);
					if (selectedContacts != null) {
						// selectedContacts.remove(new
						// Integer(UserConfig.clientUserId));
						ArrayList<Integer> users = new ArrayList<Integer>();
						for (int i = 0; i < selectedContacts.size(); i++) {
							int userid = selectedContacts.get(i);
							if (userid != UserConfig.clientUserId)
								users.add(selectedContacts.get(i));
						}
						MessagesController.getInstance().meetingCall(
								meeting_id, chatId, users, 0);
						setCallTitle(CallStatus.Wait);
					} else {
						setCallTitle(CallStatus.InCall);
					}
				} else {
					chatInfo = (TLRPC.ChatParticipants) NotificationCenter
							.getInstance().getFromMemCache(5);
					if (chatInfo != null) {
						// ��Ϊ��Ϊ�������У�Ϊ��Ϊ��������
						ArrayList<Integer> users = new ArrayList<Integer>();
						for (int i = 0; i < chatInfo.participants.size(); i++) {
							int userid = chatInfo.participants.get(i).user_id;
							if (userid != UserConfig.clientUserId)
								users.add(chatInfo.participants.get(i).user_id);
						}
						MessagesController.getInstance().meetingCall(
								meeting_id, chatId, users, 0);
						setCallTitle(CallStatus.Wait);
					} else {
						setCallTitle(CallStatus.InCall);
					}
				}

				meeting.dial(ToneGenerator.TONE_SUP_RINGTONE);

				// ����е�����״̬
			}

			MediaController.getInstance().SetAudioMode(
					AudioManager.MODE_RINGTONE);
		} else if (id == MessagesController.meeting_userin) {
			final int meetBuddyId = (Integer) args[0];
			int peerId = (Integer) args[1];

			TLRPC.User user = MessagesController.getInstance().users
					.get(peerId);
			if (user == null)
				return;
			if (wantPlayAudio) {
				if (meeting != null) {
					FileLog.d("emm", "play audio meetBuddyId=" + meetBuddyId);
				}
			}
			// ֻҪ���˽�������ֹͣ���ź�����Ƶ�����˵�ʱ��Ӧ�õȵ������˽�����ֹͣ������Ƶ��
			addMeetingUser(peerId, HeadStatus.OnLine);
			meeting.stopdial();

			// ���гɹ���
			// MediaController.getInstance().SetAudioMode(AudioManager.MODE_IN_CALL);
			MediaController.getInstance().SetAudioMode(
					AudioManager.MODE_IN_COMMUNICATION);

			UEngine.getInstance().getSoundService().requestSoundFocus();

			if (chatId != 0) {
				String temp = LocaleController.getString("joinmeeting",
						R.string.joinmeeting);
				titleBarMsg = Utilities.formatName(user) + temp;
				lastShowMsgTime = System.currentTimeMillis();
				showMsg();
			} else if (userId != 0) {
				showMsg();
			}
		} else if (id == MessagesController.meeting_userout) {
			int meetBuddyId = (Integer) args[0];
			meeting.localUnplayAudio(meetBuddyId);
			// Boolean b = isNeedPublishAudio();
			// ���������뿪�Զ���������Ƶ
			// if (!b)
			// unPublishAudio();
			if (chatId != 0) {
				int userID = meeting.findUserByMeetingID(meetBuddyId);
				TLRPC.User user = MessagesController.getInstance().users
						.get(userID);
				if (user == null)
					return;
				String temp = LocaleController.getString("exitmeeting",
						R.string.exitmeeting);
				titleBarMsg = Utilities.formatName(user) + temp;
				lastShowMsgTime = System.currentTimeMillis();
				removeMeetingUser(userID);
			} else if (userId != 0) {
				// �����Զ��رգ�Ȼ����chatAcitivity��ʾ��Ϣ������qq������BUTTON�������£�Ȼ������Զ��ر�
				// ��HZ�ϳ�һ������ֱ��destroyUI������
				meeting.dial(ToneGenerator.TONE_SUP_CONGESTION);
				meeting.stopdial();
				normalQuit = true;
				FileLog.e("emm", "meeting_userout");
				destroyUI(true);

				UEngine.getInstance().getSoundService().abandonSoundFocus();
				MediaController.getInstance().SetAudioMode(
						AudioManager.MODE_NORMAL);
			}
		} else if (id == MessagesController.meeting_speaking_status_change) {
			int meetBuddyId = (Integer) args[0];
			int speaking = (Integer) args[1];
			int count = meeting.online_meeting_member_data.size();
			for (int i = 0; i < count; i++) {
				TLRPC.TL_MeetingUser user = meeting.online_meeting_member_data
						.get(i);
				int mBuddyId = user.meetBuddyID;
				if (mBuddyId == meetBuddyId) {
					// speaking == 1�Ƿ��ԣ�0�ǲ�����
					if (user.state)
						userInMeetingMap.get(user.userID).updateUserState(
								HeadStatus.OnLine);
					else
						userInMeetingMap.get(user.userID).updateUserState(
								HeadStatus.Mute);
					break;
				}
			}
		} else if (id == MessagesController.meeting_call_response) {
			// ��ʾ�Է�æ��ܾ�
			int reason = (Integer) args[0];
			// 2��ʾ�Է��ܾ���3��ʾ�Է�æµ,4��ʾ�Է�����ͨ��
			if (userId != 0) {
				TLRPC.User user = MessagesController.getInstance().users
						.get(userId);
				if (user != null) {
					if (reason == 1) {
						// ȡ������
						String temp = LocaleController.getString("youhavecall",
								R.string.youhavecall);
						MessagesController.getInstance().sendSystemMsg(userId,
								chatId, temp, true);
						UEngine.getInstance().getSoundService().stopSound();
						meeting.localLeaveMeeting();
						normalQuit = true;
						FileLog.e("emm", "meeting_call_response 1");
						destroyUI(true);

					}
					if (reason == 2) {
						String nickName = Utilities.formatName(user);
						setCallTitle(CallStatus.Missed, nickName);
						normalQuit = true;
						FileLog.e("emm", "meeting_call_response,reason=2");
						destroyUI(true);
					} else if (reason == 3) {
						String nickName = Utilities.formatName(user);
						setCallTitle(CallStatus.Busy, nickName);
						normalQuit = true;
						FileLog.e("emm", "meeting_call_response,reason=3");
						destroyUI(true);
					} else if (reason == 4) {
						
					}
				}
				UEngine.getInstance().getSoundService().abandonSoundFocus();
				MediaController.getInstance().SetAudioMode(
						AudioManager.MODE_NORMAL);
			}
		} else if (id == MessagesController.other_have_call) {// ��������ʱ���˴�绰������֪ͨ��Ϣ
			int reason = (Integer) args[0];
			if (userId != 0) {
				TLRPC.User user = MessagesController.getInstance().users
						.get(userId);
				if (user != null) {
					if (reason == 1) {
						have_phoneTV.setVisibility(View.GONE);
					} else if (reason == 0) {
						have_phoneTV.setVisibility(View.VISIBLE);
					}
				}
			}
		} else if (id == 1234) {
			islogout = true;
			finish();
		}*/
	}

	

	

	
	private void addMeetingUser(int userId, HeadStatus status) {
		// �������ԤԼ����ֻ���Լ���Ȼ��ͨ��USERIN��ȡ�û���Ϣ
		if (userId == 0) {
			return;
		}
		if (userInMeetingMap.containsKey(userId)) {
			userInMeetingMap.get(userId).updateUserState(status);
			memberLayout.invalidate();
			return;
		}
		InCallHeadView iCallHeadView = new InCallHeadView(this, userId);
		iCallHeadView.updateUserState(status);
		userInMeetingMap.put(userId, iCallHeadView);
		View view = iCallHeadView.getView();
		if (view.getParent() != memberLayout) {
			memberLayout.addView(view);
			memberLayout.invalidate();
		}
	}

	private void removeMeetingUser(int userId) {
		InCallHeadView hv = userInMeetingMap.get(userId);
		if (hv == null)
			return;
		View view = hv.getView();
		if (view.getParent() == memberLayout) {
			userInMeetingMap.remove(userId);
			memberLayout.removeView(view);
			memberLayout.invalidate();
		}
	}


	public void onBackPressed() {		
		refuseCall();
	}

}

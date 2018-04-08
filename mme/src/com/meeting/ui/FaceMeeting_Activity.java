package com.meeting.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.broadcast.BroadCastViewPagerFragment;
import com.utils.BaseFragment;
import com.utils.BaseFragmentContainer;
import com.utils.FileLog;
import com.utils.ProximitySensorManager;
import com.utils.Utitlties;
import com.utils.WeiyiMeetingClient;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.weiyicloud.whitepad.NotificationCenter;
import com.weiyicloud.whitepad.NotificationCenter.NotificationCenterDelegate;
import com.weiyicloud.whitepad.ShareDoc;
import com.weiyicloud.whitepad.SharePadMgr;

import org.json.JSONObject;

import info.emm.meeting.MeetingUser;
import info.emm.meeting.Session;

@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
public class FaceMeeting_Activity extends ActionBarActivity implements
NotificationCenterDelegate {
	boolean m_bDisconcert = false;
	boolean bConnected = false;


	private BroadCastViewPagerFragment BCViewPagerFragment;
	private ViewPagerFragment ViewPagerFragment;
	//	public static int meetingType = 5;
	//public static boolean isreceive = false;

	static int GET_SHARE_FILE = 20;
	static int TAKE_SHARE_PHOTO = 21;
	static int MEETING_MEMBER = 22;
	private boolean bExitMeeting = false;

	public static Point displaySize = new Point();
	private Handler handler = new Handler();
	private	AlertDialog alertDialog;

	BaseFragmentContainer m_bfContainer = new BaseFragmentContainer() {
		@Override
		public void onFragmentReomved(BaseFragment fragment) {
		}

		@Override
		public void applySelfActionBar() {

		}

		@Override
		public void onFragmentChange(BaseFragment Nowfragment,
									 BaseFragment Oldfragment) {
		}
	};

	private static final long PROXIMITY_UNBLANK_DELAY_MILLIS = 1000;
	private static final long PROXIMITY_BLANK_DELAY_MILLIS = 100;
	private ProximitySensorManager mProximitySensorManager;

	NotificationManager notificationManager = null;

	Notification notification = null;

	boolean isOut = false;



	private MyOrientationDetector m_Odetector;


	public class MyOrientationDetector extends OrientationEventListener {
		public MyOrientationDetector(Context context) {
			super(context);
		}

		@Override
		public void onOrientationChanged(int orientation) {
			//			if (orientation > 350 || orientation < 10) {
			//				reSetRotation();
			//			} else if (orientation > 80 && orientation < 100) {
			//				reSetRotation();
			//			} else if (orientation > 170 && orientation < 190) {
			//				reSetRotation();
			//			} else if (orientation > 260 && orientation < 280) {
			//				reSetRotation();
			//			} else {
			//				return;
			//			}
		}
	}





	public void getExtraData()
	{
		ActionBar ab = getSupportActionBar();
		if (ab != null) {
			ab.hide();
		}

		Bundle bdarg = getIntent().getExtras();

		if (bdarg != null)
		{
			Log.e("meeting","facemeeting has extras data");
			String httpServer = bdarg.getString("httpserver","");
			String mid = bdarg.getString("meetingid","");
			String name = bdarg.getString("username","");
			String pwd =  bdarg.getString("password","");
			//my third id
			int thirdUID =  bdarg.getInt("thirduid");
			int chatid =  bdarg.getInt("chatid");
			boolean isInstMeeting = bdarg.getBoolean("isInstMeeting");
			int bAutoExitWeiyi = bdarg.getInt("bAutoExitWeiyi",0);
			String inviteAddress = bdarg.getString("inviteAddress");
			int bSupportSensor = bdarg.getInt("bSupportSensor",1);
			int bSupportRotation = bdarg.getInt("bSupportRotation",0);
			int hideme = bdarg.getInt("hideme",0);
			int meetingType = bdarg.getInt("meetingtype");
			String useridf = bdarg.getString("clientidentIfication");
			String title = bdarg.getString("title");
			int createid = bdarg.getInt("createrid");
			int userid = bdarg.getInt("userid");
			WeiyiMeetingClient.getInstance().setM_strMeetingID(mid);
			WeiyiMeetingClient.getInstance().setM_strUserName(name);
			WeiyiMeetingClient.getInstance().setM_instMeeting(isInstMeeting);
			WeiyiMeetingClient.getInstance().setM_httpServer(httpServer);
			WeiyiMeetingClient.getInstance().setM_pwd(pwd);
			WeiyiMeetingClient.getInstance().setM_chatid(chatid);
			Session.getInstance().getUserMgr().getSelfUser().setThirdID(thirdUID);
			WeiyiMeetingClient.getInstance().setM_autoExitWeiyi(bAutoExitWeiyi);
			WeiyiMeetingClient.getInstance().setM_inviteAddress(inviteAddress);
			WeiyiMeetingClient.getInstance().setM_bSupportSensor(bSupportSensor);

			WeiyiMeetingClient.getInstance().setM_bSupportRotation(bSupportRotation);
			WeiyiMeetingClient.getInstance().setM_hideme(hideme);
			if(meetingType!=0){
				WeiyiMeetingClient.getInstance().setM_userIdfaction(useridf);
				WeiyiMeetingClient.getInstance().setM_strMeetingName(title);
				//				WeiyiMeetingClient.getInstance().setCreaterid(createid);
				Session.getInstance().setMeetingtype(meetingType);
			}
			String photoPath = bdarg.getString("photoname");
			Log.d("emm", "photoname="+photoPath);
			Log.d("emm", "mid="+mid);
			//			if(Session.getInstance().getUserMgr().getSelfUser().getThirdID() == WeiyiMeetingClient.getInstance().getCreaterid()&&Session.getInstance().getUserMgr().getSelfUser().getThirdID()!=0&&WeiyiMeetingClient.getInstance().getCreaterid()!=0){
			//				WeiyiMeetingClient.getInstance().setViewer(false);
			//			}else{
			//				WeiyiMeetingClient.getInstance().setViewer(true);
			//			}

			//			MeetingSession.getInstance().getInvitUsers(mid, new CheckMeetingCallBack() {
			//
			//				@Override
			//				public void onSuccess(String mid, String pwd, int role) {
			//					NotificationCenter.getInstance().postNotificationName(MeetingSession.GETINVITEUSERSLIST, 0);
			//
			//				}
			//
			//				@Override
			//				public void onError(int code) {
			//					NotificationCenter.getInstance().postNotificationName(MeetingSession.GETINVITEUSERSLIST, 1);
			//
			//				}
			//			});
			//			saveHistory(WeiyiMeetingClient.getInstance().getM_strMeetingID());
		}
		else
		{
			Log.e("meeting","facemeeting no has extras data");
		}
	}

	public void initSensers() {
		m_Odetector = new MyOrientationDetector(this);
		//获取系统服务POWER_SERVICE，返回一个PowerManager对象
//		PowerManager pm = (PowerManager) this
//				.getSystemService(Context.POWER_SERVICE);


		if(WeiyiMeetingClient.getInstance().getM_bSupportSensor()==1)
			//距离感应器
//			mProximitySensorManager = new ProximitySensorManager(this,mProximitySensorListener, true);
			if(WeiyiMeetingClient.getInstance().isLiveMeeting()){
				if(Utitlties.isPad(this)){
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				}else{
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				}
			}else{
				if(WeiyiMeetingClient.getInstance().getM_bSupportRotation()==1)
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);
		UZResourcesIDFinder.init(getApplicationContext());
		getWindow().addFlags(
				LayoutParams.FLAG_TURN_SCREEN_ON
						| LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| LayoutParams.FLAG_KEEP_SCREEN_ON
						| LayoutParams.FLAG_DISMISS_KEYGUARD);

		Session.getInstance().getUserMgr().getSelfUser().setWatch(false);
		Log.e("meeting", "facemeeting onCreate**********************");

		Session.getInstance().Init(getApplicationContext(), "weiyi20!%",
				"A95F65A9FC8185F2", false);



		m_bfContainer.setContainerViewID(UZResourcesIDFinder.getResIdID("fragment_container"));
		m_bfContainer.setActivity(this);
		//设置全屏
		getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
				WindowManager.LayoutParams. FLAG_FULLSCREEN);

		setContentView(UZResourcesIDFinder.getResLayoutID("activity_face_meeting"));

		getExtraData();


		Session.getInstance().setActivity(this);

		initNotifycation();

		initSensers();
		//xiaoyang change
		if(WeiyiMeetingClient.getInstance().isLiveMeeting()){
			if(BCViewPagerFragment==null)
			{
				BCViewPagerFragment = new BroadCastViewPagerFragment();
				m_bfContainer.PushFragment(BCViewPagerFragment);
			}
		}else{
			if(ViewPagerFragment==null)
			{
				ViewPagerFragment = new ViewPagerFragment();
				m_bfContainer.PushFragment(ViewPagerFragment);
			}

		}
		//xiaoyang change
	}
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if(hasFocus){
			Session.getInstance().setActivity(this);
		}
		super.onWindowFocusChanged(hasFocus);
	}

	public void initNotifycation() {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				getApplicationContext());
		builder.setSmallIcon(UZResourcesIDFinder.getResDrawableID("ic_launcher"));
		builder.setDefaults(UZResourcesIDFinder.getResStringID("app_name"));
		notification = builder.build();
		notificationManager = (NotificationManager) this
				.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		// notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults = Notification.DEFAULT_LIGHTS;
		notification.ledARGB = Color.BLUE;
		notification.ledOnMS = 5000;
		// notification.contentView

		CharSequence contentTitle = getString(UZResourcesIDFinder.getResStringID("back_run"));
		CharSequence contentText = getString(UZResourcesIDFinder.getResStringID("in_the_meeting"));

		Intent notificationIntent = WeiyiMeetingClient.getInstance().getMeetingNotifyIntent();

		String strUrl = WeiyiMeetingClient.getInstance().getInviteAddress(Session.getInstance().getM_strMeetingID(),WeiyiMeetingClient.getInstance().getM_pwd());

		Log.i("xiao__rebuild", strUrl);

		if (notificationIntent == null) {
			return;
		}
		notificationIntent.putExtra(WeiyiMeetingClient.FROM_TITLE, strUrl);

		notificationIntent.setAction(Intent.ACTION_MAIN);
		notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		PendingIntent contentItent = PendingIntent.getActivity(
				getApplicationContext(), 0, notificationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(getApplicationContext(), contentTitle,
				contentText, contentItent);
		Log.e("meeting", "notification.contentView=" + notification.contentView);
	}

	@Override
	public void onStop() {
		super.onStop();
		NotificationCenter.getInstance().removeObserver(this);
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.NET_CONNECT_SUCCESS);
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.NET_CONNECT_LEAVE);
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.NET_CONNECT_FAILED);
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.NET_CONNECT_BREAK);
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.NET_CONNECT_ENABLE_PRESENCE);
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.NET_CONNECT_USER_IN);
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.NET_CONNECT_USER_OUT);
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_AUDIO_CHANGE);

		NotificationCenter.getInstance().removeObserver(this,
				SharePadMgr.SHAREPAD_STATECHANGE);
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_SHOW_SCREEN_PLAY);

		NotificationCenter.getInstance().removeObserver(this,
				SharePadMgr.UPLOAD_IMAGE_COMPLETE);
		NotificationCenter.getInstance().removeObserver(this,
				SharePadMgr.UPLOAD_IMAGE_PROCESSING);

		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_WHITE_PAD);
		//		NotificationCenter.getInstance().removeObserver(this,
		//				WeiyiMeetingClient.UI_NOTIFY_USER_WHITE_PAD_DOC_CHANGE);
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_KICKOUT);
		NotificationCenter.getInstance().removeObserver(this,WeiyiMeetingClient.UI_NOTIFY_USER_CHAIRMAN_CHANGE);
		NotificationCenter.getInstance().removeObserver(this, WeiyiMeetingClient.UI_NOTIFY_USER_SYNC_VIDEO_MODE_CHANGE);
		NotificationCenter.getInstance().removeObserver(this,WeiyiMeetingClient.UI_NOTIFY_MEETING_MODECHANGE);
		NotificationCenter.getInstance().removeObserver(this,WeiyiMeetingClient.UI_NOTIFY_USER_HOSTSTATUS);
		NotificationCenter.getInstance().removeObserver(this, WeiyiMeetingClient.UI_NOTIFY_USER_HOST_CHANGE);
		//		NotificationCenter.getInstance().removeObserver(this, MeetingSession.VIDEO_NOTIFY_CAMERA_DID_OPEN);
		//		NotificationCenter.getInstance().removeObserver(this, MeetingSession.NET_CONNECT_WARNING);

		NotificationCenter.getInstance().removeObserver(this, WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_TAKE_BACK_HOST_RIGHT);
		NotificationCenter.getInstance().removeObserver(this, WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_TAKE_BACK_FREE_SPEAK_RIGHT);
		NotificationCenter.getInstance().removeObserver(this, WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_TAKE_BACK_FREE_RECORD);

		NotificationCenter.getInstance().removeObserver(this, WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_ALLOW_FREE_HOST_RIGHT);
		NotificationCenter.getInstance().removeObserver(this, WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_ALLOW_FREE_SPEAK_RIGHT);
		NotificationCenter.getInstance().removeObserver(this, WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_ALLOW_FREE_RECORD);
		NotificationCenter.getInstance().removeObserver(this, WeiyiMeetingClient.DIRECT_MEETINGTYPE);
		NotificationCenter.getInstance().removeObserver(this, WeiyiMeetingClient.UI_NOTIFY_HANDSUP_START);
		NotificationCenter.getInstance().removeObserver(this, WeiyiMeetingClient.UI_NOTIFY_HANDSUP_STOP);
		if (isOut) {
			return;
		}
		// ViewPagerFragmentNew.cleanNogifi();
		disableProximitySensor(false);
		if (notification.contentView != null&&Session.getInstance().isM_bInmeeting()) {
			notificationManager.notify(0, notification);
			Log.e("meeting", "face meeting onStop notify*********************");
		}
		Log.e("meeting", "face meeting onStop");
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		disableProximitySensor(false);
		m_Odetector.disable();
		if (notification.contentView != null&&Session.getInstance().isM_bInmeeting()) {
			notificationManager.notify(0, notification);
			Log.e("meeting", "face meeting onPause notify*********************");
		}
		Log.e("meeting", "face meeting onPause*********************");

		// if(pop_Audio.isShowing()){
		// pop_Audio.dismiss();
		// }
		// if(pop_video.isShowing()){
		// pop_video.dismiss();
		// }
		// if(pop_share_doc.isShowing()){
		// pop_share_doc.dismiss();
		// }

	}

	@Override
	public void onResume() {
		super.onResume();
		WeiyiMeetingClient.getInstance().Init(getApplicationContext(), "weiyi20!%",
				"A95F65A9FC8185F2", false);
		enableProximitySensor();
		notificationManager.cancel(0);
		Log.e("meeting", "face meeting onResume cancel notify*********************");
		m_Odetector.enable();
		try {

			Display display = this.getWindowManager().getDefaultDisplay();
			if (display != null) {
				if (android.os.Build.VERSION.SDK_INT < 13) {
					displaySize.set(display.getWidth(), display.getHeight());
				} else {
					display.getSize(displaySize);
				}
			}

		} catch (Exception e) {
			FileLog.e("meeting", e);
		}


		Log.e("meeting", "facemeeting onresume********************");

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		if (isScreenOriatationPortrait(this)) {
			enableProximitySensor();
		} else {
			disableProximitySensor(false);
		}

		try {

			Display display = this.getWindowManager().getDefaultDisplay();
			if (display != null) {
				if (android.os.Build.VERSION.SDK_INT < 13) {
					displaySize.set(display.getWidth(), display.getHeight());
				} else {
					display.getSize(displaySize);
				}
			}

		} catch (Exception e) {
			FileLog.e("meeting", e);
		}
		if(WeiyiMeetingClient.getInstance().isLiveMeeting()){
			if (BCViewPagerFragment != null) {
				if (BCViewPagerFragment.m_fragmentCamera != null)
					BCViewPagerFragment.m_fragmentCamera.doVideoLayout();
				else {
					if (BCViewPagerFragment.m_PadMainFragment != null
							&& BCViewPagerFragment.m_PadMainFragment.m_fragmentCamera != null)
						BCViewPagerFragment.m_PadMainFragment.m_fragmentCamera
								.doVideoLayout();
				}
			}
		}else{
			if (ViewPagerFragment != null) {
				if (ViewPagerFragment.m_fragmentCamera != null)
					ViewPagerFragment.m_fragmentCamera.doVideoLayout();
				else {
					if (ViewPagerFragment.m_PadMainFragment != null
							&& BCViewPagerFragment.m_PadMainFragment.m_fragmentCamera != null)
						ViewPagerFragment.m_PadMainFragment.m_fragmentCamera
								.doVideoLayout();
				}
			}
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if(bExitMeeting)
			notificationManager.cancel(0);
		Log.e("meeting", "face meeting destroyed cancel notify*********************");
		// MeetingSession.getInstance().getSharePadMgr().removeOnDataChangeListener(this);
		Log.e("meeting", "meeting activity destroyed************************************************");
	}

	@SuppressWarnings("unused")
	@Override
	public void didReceivedNotification(int id, Object... args) {
		switch (id) {
			case WeiyiMeetingClient.NET_CONNECT_LEAVE: {
				if(WeiyiMeetingClient.getInstance().isLiveMeeting()){
					return;
				}
				//EXIT_MEETING
				FileLog.e("meeting", "facemeeting receive MeetingSession.NET_CONNECT_LEAVE msg");
				//Toast.makeText(this, "NET_CONNECT_LEAVE is error", Toast.LENGTH_LONG).show();
				finish();
				bExitMeeting = true;

				break;
			}
			case WeiyiMeetingClient.NET_CONNECT_SUCCESS: {
				if(WeiyiMeetingClient.getInstance().isLiveMeeting()){
					BCViewPagerFragment.OnConnectted();
				}else{
					ViewPagerFragment.OnConnectted();
				}
			}
			break;

			case WeiyiMeetingClient.NET_CONNECT_FAILED:
			{
				FileLog.e("emm", "NET_CONNECT_FAILED");
				//Toast.makeText(this, "NET_CONNECT_FAILED is error", Toast.LENGTH_LONG).show();
				onConnectBreak(0);
				break;
			}
			case WeiyiMeetingClient.NET_CONNECT_ENABLE_PRESENCE:
			{

				Log.e("meeting", "NET_CONNECT_ENABLE_PRESENCE arg length="+ args.length);
				if(WeiyiMeetingClient.getInstance().isLiveMeeting()){
					BCViewPagerFragment.OnConnectted();
				}else{
					ViewPagerFragment.OnConnectted();
				}
				long currentTime = System.currentTimeMillis()/1000;
				long residualTime = Session.getInstance().getMeetingEndTime();
				long time = residualTime - currentTime;
				if(WeiyiMeetingClient.getInstance().isM_bAutoQuit() && residualTime != 0){
					new CountDownTimer(time*1000, 1000) {
						@Override
						public void onTick(long totaltime) {
							if(!FaceMeeting_Activity.this.isFinishing()){
								int times = (int) (totaltime/1000);
								if (times == 0){
									Toast.makeText(FaceMeeting_Activity.this, getString(UZResourcesIDFinder.getResStringID("exit_meeting")), Toast.LENGTH_LONG).show();
								}else if(times == 15*60){
									Toast.makeText(FaceMeeting_Activity.this, getString(UZResourcesIDFinder.getResStringID("exit_remind_15")), Toast.LENGTH_LONG).show();
								}else if (times == 5*60){
									Toast.makeText(FaceMeeting_Activity.this, getString(UZResourcesIDFinder.getResStringID("exit_remind_5")), Toast.LENGTH_LONG).show();
								}else if (times == 60){
									Toast.makeText(FaceMeeting_Activity.this,getString(UZResourcesIDFinder.getResStringID("exit_remind_1")), Toast.LENGTH_LONG).show();
								}else if (times == 15){
									Toast.makeText(FaceMeeting_Activity.this, getString(UZResourcesIDFinder.getResStringID("exit_remind_15_second")), Toast.LENGTH_LONG).show();
								}
							}
						}
						@Override
						public void onFinish() {
							WeiyiMeetingClient.getInstance().exitMeeting();
							Toast.makeText(FaceMeeting_Activity.this, getString(UZResourcesIDFinder.getResStringID("exit_meeting")), Toast.LENGTH_LONG).show();
						}
					}.start();
					Log.e("meeting", "NET_CONNECT_ENABLE_PRESENCE");
				}

				break;
			}

			case SharePadMgr.UPLOAD_IMAGE_PROCESSING: {
				break;
			}

			case WeiyiMeetingClient.NET_CONNECT_USER_IN: {
				if(WeiyiMeetingClient.getInstance().isLiveMeeting()){
					return;
				}
				int nPeerID = (Integer) args[0];
				boolean bInlist = (Boolean) args[1];
				if (!bInlist) {
					MeetingUser mu = Session.getInstance().getUserMgr()
							.getUser(nPeerID);
					if (mu != null) {
						String strTip = mu.getName() + " "
								+ getString(UZResourcesIDFinder.getResStringID("user_join_to_meeting"));
						Toast.makeText(this, strTip, Toast.LENGTH_SHORT).show();
					}
				}
				break;
			}
			case WeiyiMeetingClient.NET_CONNECT_USER_OUT: {
				if(WeiyiMeetingClient.getInstance().isLiveMeeting()){
					return;
				}
				String strName = (String) args[1];
				String strTip = strName + " "
						+ getString(UZResourcesIDFinder.getResStringID("user_left_meeting"));
				Toast.makeText(this, strTip, Toast.LENGTH_SHORT).show();
				break;
			}


			case WeiyiMeetingClient.UI_NOTIFY_USER_WHITE_PAD: {
				JSONObject js = (JSONObject) args[0];
				byte[] shapedata = (byte[]) args[1];
				boolean bAdd = (Boolean) args[2];
				Session.getInstance().whitePadChange(js, shapedata, bAdd);
				break;
			}

			//		case WeiyiMeetingClient.UI_NOTIFY_USER_WHITE_PAD_DOC_CHANGE: {
			//			JSONObject js = (JSONObject) args[0];
			//			Session.getInstance().whitePadDocChange(js);
			//			break;
			//		}

			case WeiyiMeetingClient.NET_CONNECT_BREAK: {
				//if (!m_bDisconcert) {
				//m_bDisconcert = true;
				int nRet = (Integer) args[0];
				FileLog.e("emm", "NET_CONNECT_BREAK");
				//Toast.makeText(this, "NET_CONNECT_BREAK is error", Toast.LENGTH_LONG).show();
				onConnectBreak(nRet);
				//}
				break;
			}

			case WeiyiMeetingClient.UI_NOTIFY_USER_KICKOUT: {
				int res = (Integer) args[0];
				if (res == WeiyiMeetingClient.Kickout_ChairmanKickout) {
					Endmeeting();
					if(!WeiyiMeetingClient.getInstance().isLiveMeeting()){
						Toast.makeText(this, UZResourcesIDFinder.getResStringID("lng_mask_kickout_message"), Toast.LENGTH_SHORT).show();
					}else{
						finish();
					}
				} else if (res == WeiyiMeetingClient.Kickout_Repeat) {
					Toast.makeText(this,getString(UZResourcesIDFinder.getResStringID("lng_mask_accountlogined_message")) , Toast.LENGTH_LONG).show();
					Endmeeting();
				}
				break;
			}
			case WeiyiMeetingClient.UI_NOTIFY_USER_CHAIRMAN_CHANGE:
				int fromid = WeiyiMeetingClient.getInstance().getChairManID();
				MeetingUser mu = Session.getInstance().getUserMgr().getUser(fromid);
				if(mu!=null)
				{
					if(fromid == WeiyiMeetingClient.getInstance().getMyPID())
					{
						ShareDoc dc = SharePadMgr.getInstance().getCurrentShareDoc();
						if(dc!=null)
							Session.getInstance().sendShowPage(dc.docID, dc.currentPage);
						if(Session.getInstance().getUserMgr().getSelfUser().getAudioStatus() ==  Session.RequestSpeak_Allow)
						{
							WeiyiMeetingClient.getInstance().requestSpeaking(WeiyiMeetingClient.getInstance().getMyPID());
						}
					}
					String userName = mu.getName();
					String msg =getString(UZResourcesIDFinder.getResStringID("chairman_change"))+userName;
					Session.getInstance().sendSystemMsg(fromid,userName,msg);
					Toast.makeText(FaceMeeting_Activity.this,getString(UZResourcesIDFinder.getResStringID("chairman_change"))+userName, Toast.LENGTH_LONG).show();
				}
				break;
			case WeiyiMeetingClient.UI_NOTIFY_USER_SYNC_VIDEO_MODE_CHANGE:
				if(WeiyiMeetingClient.getInstance().isLiveMeeting()){
					return;
				}
				boolean mode = (Boolean) args[0];
				boolean auto = (Boolean) args[1];
				int fromid1 = WeiyiMeetingClient.getInstance().getChairManID();
				MeetingUser mu1 = Session.getInstance().getUserMgr().getUser(fromid1);
				if (mode && auto)
				{
					if(mu1!=null)
					{
						String userName = mu1.getName();
						String msg =getString(UZResourcesIDFinder.getResStringID("chairman_synchronous_video"));
						Session.getInstance().sendSystemMsg(fromid1,userName,msg);
						Toast.makeText(FaceMeeting_Activity.this,msg, Toast.LENGTH_LONG).show();
					}
				}
				else
				{
					if(mu1!=null)
					{
						String userName = mu1.getName();
						String msg =getString(UZResourcesIDFinder.getResStringID("chairman_cancel_video"));
						Session.getInstance().sendSystemMsg(fromid1,userName,msg);
						Toast.makeText(FaceMeeting_Activity.this,msg, Toast.LENGTH_LONG).show();
					}
				}
				break;
			case WeiyiMeetingClient.UI_NOTIFY_MEETING_MODECHANGE:
				int fromid2 = WeiyiMeetingClient.getInstance().getChairManID();
				MeetingUser mu2 = Session.getInstance().getUserMgr().getUser(fromid2);
				if(mu2!=null)
				{
					String userName = mu2.getName();
					String msg =getString(UZResourcesIDFinder.getResStringID("freedo_mode"));
					Session.getInstance().sendSystemMsg(fromid2,userName,msg);
					Toast.makeText(FaceMeeting_Activity.this,getString(UZResourcesIDFinder.getResStringID("freedo_mode")), Toast.LENGTH_LONG).show();
				}
				break;
			case WeiyiMeetingClient.UI_NOTIFY_USER_HOSTSTATUS:
				int userid = (Integer) args[0];
				int status = (Integer) args[1];


				MeetingUser mu3 = Session.getInstance().getUserMgr().getUser(userid);
				if(mu3!=null)
				{
					String userName = mu3.getName();
					if(status == WeiyiMeetingClient.RequestHost_Allow)
					{
						if(userid == WeiyiMeetingClient.getInstance().getMyPID())
						{
							ShareDoc dc = SharePadMgr.getInstance().getCurrentShareDoc();
							if(dc!=null)
								Session.getInstance().sendShowPage(dc.docID, dc.currentPage);
						}
						if(!WeiyiMeetingClient.getInstance().isLiveMeeting()){
							String msg =userName+getString(UZResourcesIDFinder.getResStringID("speaker_change"));
							Session.getInstance().sendSystemMsg(userid,userName,msg);
							Toast.makeText(FaceMeeting_Activity.this,msg, Toast.LENGTH_LONG).show();
						}
					}
					else if(status==WeiyiMeetingClient.RequestHost_Disable)
					{
						if(mu3.getPeerID()==userid)
						{
							if(!WeiyiMeetingClient.getInstance().isLiveMeeting()){
								String msg =userName+getString(UZResourcesIDFinder.getResStringID("speaker_change_cancel"));
								Session.getInstance().sendSystemMsg(userid,userName,msg);
								Toast.makeText(FaceMeeting_Activity.this,msg, Toast.LENGTH_LONG).show();
							}
						}
					}
					else
					{
						if(!WeiyiMeetingClient.getInstance().isControlFree() && WeiyiMeetingClient.getInstance().getChairManID()==WeiyiMeetingClient.getInstance().getMyPID())
						{
							if(!WeiyiMeetingClient.getInstance().isLiveMeeting()){
								String msg =userName+getString(UZResourcesIDFinder.getResStringID("speaker_change_request"));
								Session.getInstance().sendSystemMsg(userid,userName,msg);
								Toast.makeText(FaceMeeting_Activity.this,msg, Toast.LENGTH_LONG).show();
							}
						}
						else if(WeiyiMeetingClient.getInstance().getMyPID()==userid)
						{
							if(!WeiyiMeetingClient.getInstance().isLiveMeeting()){
								String msg =getString(UZResourcesIDFinder.getResStringID("speaker_change_request_return"));
								Session.getInstance().sendSystemMsg(userid,userName,msg);
								Toast.makeText(FaceMeeting_Activity.this,msg, Toast.LENGTH_LONG).show();
							}
						}
					}
				}
				break;
			case WeiyiMeetingClient.UI_NOTIFY_USER_HOST_CHANGE://鍚岄敓鏂ゆ嫹閿熶茎纰夋嫹
			/*int fromid4 = (Integer) args[0];
			MeetingUser mu4 = MeetingSession.getInstance().getUserMgr().getUser(fromid4);
			if(mu4!=null)
			{
				String userName = mu4.getName();				
				MeetingSession.getInstance().sendSystemMsg(fromid4,userName,msg);
				Toast.makeText(FaceMeeting_Activity.this,msg, Toast.LENGTH_LONG).show();				
			}*/
				break;
			//		case MeetingSession.VIDEO_NOTIFY_CAMERA_DID_OPEN:
			//			Camera cam = (Camera) args[0];
			//			if(cam==null)
			//			{			
			//				//todo.. xiaomei
			//				AlertDialog.Builder builder = new AlertDialog.Builder(
			//						this);
			//						new DialogInterface.OnClickListener() {
			//
			//					@Override
			//					public void onClick(DialogInterface arg0,
			//							int arg1) {
			//						if(MeetingSession.getInstance().isLiveMeeting()){
			//							Endmeeting();
			//							finish();
			//						}else{							
			//							Endmeeting();
			//						}
			//					}
			//				});
			//				builder.show();
			//			}
			//			break;
			//		case MeetingSession.NET_CONNECT_WARNING:
			//			int result = (Integer) args[0];
			//			if(result==11)
			//
			//				//todo.. xiaomei
			//			{	
			//				AlertDialog.Builder builder = new AlertDialog.Builder(
			//						this);
			//						new DialogInterface.OnClickListener() {
			//
			//					@Override
			//					public void onClick(DialogInterface arg0,
			//							int arg1) {
			//						if(MeetingSession.getInstance().isLiveMeeting()){
			//							Endmeeting();
			//							finish();
			//						}else{							
			//							Endmeeting();
			//						}
			//					}
			//				});
			//				builder.show();
			//				//ShowAlertDialog(this, message)
			//			}
			//			break;
			case WeiyiMeetingClient.UI_NOTIFY_USER_AUDIO_CHANGE: {

				int nPeerID = (Integer) args[0];
				int nStatus = (Integer) args[1];
				if(!WeiyiMeetingClient.getInstance().isSpeakFree())
				{
					if(WeiyiMeetingClient.getInstance().getChairManID()==WeiyiMeetingClient.getInstance().getMyPID() && nPeerID!=WeiyiMeetingClient.getInstance().getChairManID())
					{
						MeetingUser user = Session.getInstance().getUserMgr().getUser(nPeerID);
						if(user!=null)
						{
							if(nStatus==Session.RequestSpeak_Pending)
							{
								String msg = user.getName()+getString(UZResourcesIDFinder.getResStringID("apply_speak_and_wait_agree"));
								Toast.makeText(this, msg,Toast.LENGTH_LONG).show();
							}
						}
					}
				}

				MeetingUser mu4 = Session.getInstance().getUserMgr().getSelfUser();
				if (nPeerID != mu4.getPeerID())
					return;


				if(mu4.getAudioStatus() == Session.RequestSpeak_Pending)
					Toast.makeText(this, UZResourcesIDFinder.getResStringID("request_in_queue"),Toast.LENGTH_LONG).show();
				else if(mu4.getAudioStatus() == Session.RequestSpeak_Allow){
					PackageManager pm = getPackageManager();
					boolean permission = (PackageManager.PERMISSION_GRANTED ==
							pm.checkPermission("android.permission.RECORD_AUDIO", "info.emm.weiyicloud"));
					if (permission) {
						Toast.makeText(this,getString(UZResourcesIDFinder.getResStringID("speak_permission")),Toast.LENGTH_LONG).show();		        }else {
					}
				}
				if(WeiyiMeetingClient.getInstance().isLiveMeeting()){
					if(BCViewPagerFragment!=null)
						BCViewPagerFragment.changeAudioImage();
				}else{
					if(ViewPagerFragment!=null)
						ViewPagerFragment.changeAudioImage();
				}
			}
			break;
			case WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_TAKE_BACK_HOST_RIGHT:
				if(!WeiyiMeetingClient.getInstance().isControlFree())
				{
					int fromId = WeiyiMeetingClient.getInstance().getChairManID();
					MeetingUser mu5 = Session.getInstance().getUserMgr().getUser(fromId);
					if(mu5!=null)
					{
						String userName = mu5.getName();
						if(WeiyiMeetingClient.getInstance().getChairManID()==WeiyiMeetingClient.getInstance().getMyPID())
						{
							//xiaomei todo..
							String msg = getString(UZResourcesIDFinder.getResStringID("withdraw_permission"));
							Session.getInstance().sendSystemMsg(fromId,userName,msg);
							Toast.makeText(this, msg,Toast.LENGTH_LONG).show();
						}
						else
						{
							//xiaomei todo..
							String msg = getString(UZResourcesIDFinder.getResStringID("chairman_withdraw_permission"));
							Session.getInstance().sendSystemMsg(fromId,userName,msg);
							Toast.makeText(this, msg,Toast.LENGTH_LONG).show();
						}
					}
				}
				break;
			case WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_TAKE_BACK_FREE_SPEAK_RIGHT:
				if(!WeiyiMeetingClient.getInstance().isSpeakFree())
				{
					int fromchairmanId = WeiyiMeetingClient.getInstance().getChairManID();
					MeetingUser mu5 =Session.getInstance().getUserMgr().getUser(fromchairmanId);
					if(mu5!=null)
					{
						String userName = mu5.getName();
						if(WeiyiMeetingClient.getInstance().getChairManID()==WeiyiMeetingClient.getInstance().getMyPID())
						{
							//xiaomei todo..
							String msg = getString(UZResourcesIDFinder.getResStringID("withdraw_free_speech_right"));
							Session.getInstance().sendSystemMsg(fromchairmanId,userName,msg);
							Toast.makeText(this, msg,Toast.LENGTH_LONG).show();
						}
						else
						{
							//xiaomei todo..
							String msg = getString(UZResourcesIDFinder.getResStringID("chairman_withdraw_free_speech_right"));
							Session.getInstance().sendSystemMsg(fromchairmanId,userName,msg);
							Toast.makeText(this, msg,Toast.LENGTH_LONG).show();
						}
					}
				}
				break;
			case WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_TAKE_BACK_FREE_RECORD:
			/*if(!MeetingSession.getInstance().isAllowRecord())
			{

			}*/
				break;
			case WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_ALLOW_FREE_HOST_RIGHT:
				if(WeiyiMeetingClient.getInstance().getChairManID()==WeiyiMeetingClient.getInstance().getMyPID())
				{
					//xiaomei todo..
					String msg = getString(UZResourcesIDFinder.getResStringID("allow_free_permission_speaker"));
					Toast.makeText(this, msg,Toast.LENGTH_LONG).show();
				}
				else
				{
					//xiaomei todo..
					String msg = getString(UZResourcesIDFinder.getResStringID("chairman_allow_free_permission_speaker"));
					Toast.makeText(this, msg,Toast.LENGTH_LONG).show();
				}
				break;
			case WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_ALLOW_FREE_SPEAK_RIGHT:
				if(WeiyiMeetingClient.getInstance().getChairManID()==WeiyiMeetingClient.getInstance().getMyPID())
				{
					//xiaomei todo..
					String msg = getString(UZResourcesIDFinder.getResStringID("allow_free_speech"));
					Toast.makeText(this, msg,Toast.LENGTH_LONG).show();
				}
				else
				{
					//xiaomei todo..
					String msg = getString(UZResourcesIDFinder.getResStringID("chairman_allow_free_speech"));
					Toast.makeText(this, msg,Toast.LENGTH_LONG).show();
				}


				break;
			case WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_ALLOW_FREE_RECORD:
				break;
			//		case MeetingSession.DIRECT_MEETINGTYPE:
			//			//xiaoyang change
			//			m_bfContainer.cleanStack();
			//			if(MeetingSession.getInstance().isLiveMeeting()){
			//				if(BCViewPagerFragment==null)
			//				{
			//					BCViewPagerFragment = new BroadCastViewPagerFragment();
			//					m_bfContainer.PushFragment(BCViewPagerFragment);
			//				}
			//			}else{
			//				if(ViewPagerFragment==null)
			//				{
			//					ViewPagerFragment = new ViewPagerFragment();
			//					m_bfContainer.PushFragment(ViewPagerFragment);
			//				}
			//
			//			}
			//			//xiaoyang change
			//			break;
			case WeiyiMeetingClient.UI_NOTIFY_HANDSUP_START:
				int toId = (Integer) args[0];
				showHandsUpACK(toId);
				break;
			case WeiyiMeetingClient.UI_NOTIFY_HANDSUP_STOP:
				if (alertDialog != null) {
					alertDialog.dismiss();
				}
		}
	}

	public void Endmeeting() {
		SharedPreferences sp = getSharedPreferences("state", 0);
		SharedPreferences.Editor editor = sp.edit();
		//		editor.putBoolean("loud", true);
		//		editor.putBoolean("m_bisopenCamera", true);
		//		editor.putBoolean("highquality", false);
		//		//		editor.putBoolean("isShow", false);
		//		editor.putBoolean("m_bIsfrontCamera", true);
		//		editor.putBoolean("isStartLive", false);
		//		editor.putBoolean("ishandup", false);
		editor.clear();
		editor.commit();

		SharedPreferences chairsp = getSharedPreferences("chairmanData",
				0);
		Editor chaired = chairsp.edit();
		chaired.putBoolean("isfreemode", true);
		chaired.putBoolean("issyc", false);
		chaired.putBoolean("islock", false);
		chaired.commit();
		notificationManager.cancel(0);
		Log.e("meeting", "EndMeeting cancel notify*************");
		isOut = true;
		if(WeiyiMeetingClient.getInstance().isLiveMeeting()){
			if(BCViewPagerFragment != null){
				BCViewPagerFragment.clean();
			}
		}else{
			if(ViewPagerFragment != null){
				ViewPagerFragment.clean();
			}
		}

		m_bDisconcert = true;
		disableProximitySensor(false);
		WeiyiMeetingClient.getInstance().exitMeeting();
	}

	public static boolean isScreenOriatationPortrait(Context context) {
		return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	}
	@Override
	protected void onStart() {
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_SUCCESS);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_LEAVE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_FAILED);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_BREAK);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_ENABLE_PRESENCE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_USER_IN);
		NotificationCenter.getInstance().addObserver(this,+

				WeiyiMeetingClient.NET_CONNECT_USER_OUT);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_AUDIO_CHANGE);

		NotificationCenter.getInstance().addObserver(this,
				SharePadMgr.SHAREPAD_STATECHANGE);
		// NotificationCenter.getInstance().addObserver(this,
		// MeetingSession.UI_NOTIFY_SHOW_SCREEN_PLAY);

		NotificationCenter.getInstance().addObserver(this,
				SharePadMgr.UPLOAD_IMAGE_COMPLETE);
		NotificationCenter.getInstance().addObserver(this,
				SharePadMgr.UPLOAD_IMAGE_PROCESSING);

		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_WHITE_PAD);
		//		NotificationCenter.getInstance().addObserver(this,
		//				WeiyiMeetingClient.UI_NOTIFY_USER_WHITE_PAD_DOC_CHANGE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_KICKOUT);
		NotificationCenter.getInstance().addObserver(this,WeiyiMeetingClient.UI_NOTIFY_USER_CHAIRMAN_CHANGE);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_USER_SYNC_VIDEO_MODE_CHANGE);
		NotificationCenter.getInstance().addObserver(this,WeiyiMeetingClient.UI_NOTIFY_MEETING_MODECHANGE);
		NotificationCenter.getInstance().addObserver(this,WeiyiMeetingClient.UI_NOTIFY_USER_HOSTSTATUS);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_USER_HOST_CHANGE);
		//		NotificationCenter.getInstance().addObserver(this, MeetingSession.VIDEO_NOTIFY_CAMERA_DID_OPEN);
		//		NotificationCenter.getInstance().addObserver(this, MeetingSession.NET_CONNECT_WARNING);

		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_TAKE_BACK_HOST_RIGHT);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_TAKE_BACK_FREE_SPEAK_RIGHT);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_TAKE_BACK_FREE_RECORD);

		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_ALLOW_FREE_HOST_RIGHT);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_ALLOW_FREE_SPEAK_RIGHT);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_ALLOW_FREE_RECORD);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.DIRECT_MEETINGTYPE);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_HANDSUP_START);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_HANDSUP_STOP);

		super.onStart();
	}
	@Override
	public void onBackPressed() {
		BaseFragment topFragment = m_bfContainer.getTopFragment();

		if (topFragment != null && topFragment.onBackPressed()) {
			m_bfContainer.removeFromStack(topFragment);
		}else if(topFragment != null && !topFragment.onBackPressed()){

		} else {
			if(m_bfContainer.fragmentsStack.size()==1){
				BaseFragment last = m_bfContainer.fragmentsStack.get(0);
				if(!last.onBackPressed()){
					showExitDialog();
				}
			}else{
				showExitDialog();
			}
		}
		if (m_bfContainer.getTopFragment() == null) {
			if(WeiyiMeetingClient.getInstance().isLiveMeeting()){
				if (BCViewPagerFragment != null
						&& BCViewPagerFragment.m_vpMeeting != null)
					BCViewPagerFragment.m_vpMeeting.setVisibility(View.VISIBLE);
			}else{
				if (ViewPagerFragment != null
						&& ViewPagerFragment.m_vpMeeting != null)
					ViewPagerFragment.m_vpMeeting.setVisibility(View.VISIBLE);
			}
			this.enableProximitySensor();
		}
		//		if (ViewPagerFragmentNew != null)
		//			ViewPagerFragmentNew.HideLayouts();
	}

	public void ShowAlertDialog(final Activity activity, final String message) {
		Utitlties.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				if (!isFinishing()) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							activity);
					builder.setTitle(getString(UZResourcesIDFinder.getResStringID("app_name")));
					builder.setMessage(message);
					builder.setPositiveButton(getString(UZResourcesIDFinder.getResStringID("OK")),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
													int arg1) {
									// TODO Auto-generated method stub
									Endmeeting();
									if (WeiyiMeetingClient.getInstance().isLiveMeeting()) {
										finish();
									}
								}
							});
					builder.show().setCanceledOnTouchOutside(true);
				}
			}
		});
	}
	public void showExitDialog() {
		if(WeiyiMeetingClient.getInstance().isNeedShowExitDialog()){
			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle(UZResourcesIDFinder.getResStringID("remind"));
			builder.setMessage(UZResourcesIDFinder.getResStringID("logouts"));
			builder.setPositiveButton(UZResourcesIDFinder.getResStringID("sure"),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(WeiyiMeetingClient.getInstance().isLiveMeeting()){
								notificationManager.cancel(0);
								WeiyiMeetingClient.getInstance().stopBroadCasting(Session.getInstance().getUserMgr().getSelfUser().getPeerID());
								Endmeeting();
								finish();
							}else{
								Endmeeting();
							}


						}
					}).setNegativeButton(UZResourcesIDFinder.getResStringID("cancel"),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			AlertDialog alertDialog = builder.create();
			alertDialog.getWindow().setBackgroundDrawableResource(
					android.R.color.transparent);
			alertDialog.show();
		}else{
			if(WeiyiMeetingClient.getInstance().isLiveMeeting()){
				notificationManager.cancel(0);
				WeiyiMeetingClient.getInstance().stopBroadCasting(Session.getInstance().getUserMgr().getSelfUser().getPeerID());
				Endmeeting();
				finish();
			}else{
				Endmeeting();
			}
		}


	}

	@SuppressLint("NewApi")
	public static String getPath(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider   
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/"
							+ split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				String file = null;
				String wholeID = DocumentsContract.getDocumentId(uri);
				String id = wholeID.split(":")[1];
				String[] column = { MediaStore.Images.Media.DATA };
				String sel = MediaStore.Images.Media._ID + "=?";
				Cursor cursor = context.getContentResolver().query(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column,
						sel, new String[] { id }, null);
				int columnIndex = cursor.getColumnIndex(column[0]);
				if (cursor.moveToFirst()) {
					file = cursor.getString(columnIndex);
				}
				cursor.close();
				return file;
				// return getDataColumn(context, contentUri, selection,
				// selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {

			// Return the remote address
			if (isGooglePhotosUri(uri))
				return uri.getLastPathSegment();

			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}
		return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @param selection
	 *            (Optional) Filter used in the query.
	 * @param selectionArgs
	 *            (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri,
									   String selection, String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection,
					selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri
				.getAuthority());
	}

	public static long getProximityUnblankDelayMillis() {
		return PROXIMITY_UNBLANK_DELAY_MILLIS;
	}

	public static long getProximityBlankDelayMillis() {
		return PROXIMITY_BLANK_DELAY_MILLIS;
	}

	public void enableProximitySensor()
	{
		if(mProximitySensorManager!=null&&!WeiyiMeetingClient.getInstance().isLiveMeeting())
			mProximitySensorManager.enable();
	}

	public void disableProximitySensor(boolean waitForFarState) {
		if(mProximitySensorManager!=null)
			mProximitySensorManager.disable(waitForFarState);
	}

	private final ProximitySensorListener mProximitySensorListener = new ProximitySensorListener() {

	};

	/** Listener to changes in the proximity sensor state. */
	private class ProximitySensorListener implements
			ProximitySensorManager.Listener {
		/** Used to show a blank view and hide the action bar. */
		private final Runnable mBlankRunnable = new Runnable() {
			@Override
			public void run() {
				FileLog.d("meeting", "meeting mBlankRunnable");
				simulateProximitySensorNearby(true);
			}
		};
		/** Used to remove the blank view and show the action bar. */
		private final Runnable mUnblankRunnable = new Runnable() {
			@Override
			public void run() {
				simulateProximitySensorNearby(false);
			}
		};

		@Override
		public synchronized void onNear() {
			FileLog.d("meeting", "meeting onNear");

			postDelayed(mBlankRunnable, PROXIMITY_BLANK_DELAY_MILLIS);
		}

		@Override
		public synchronized void onFar() {
			FileLog.d("meeting", "meeting onFar");

			postDelayed(mUnblankRunnable, PROXIMITY_UNBLANK_DELAY_MILLIS);
		}

		/** Removed any delayed requests that may be pending. */

		/** Post a {@link Runnable} with a delay on the main thread. */
		private synchronized void postDelayed(Runnable runnable,
											  long delayMillis) {
			// Post these instead of executing immediately so that:
			// - They are guaranteed to be executed on the main thread.
			// - If the sensor values changes rapidly for some time, the UI will
			// not be
			// updated immediately.

		}
	}

	public void tearDown() {
		// disableProximitySensor(false);
		mProximitySensorListener.postDelayed(
				mProximitySensorListener.mUnblankRunnable,
				PROXIMITY_UNBLANK_DELAY_MILLIS);
	}

	private void simulateProximitySensorNearby(boolean nearby) {

		final Window window = this.getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		View view = ((ViewGroup) window.getDecorView().findViewById(
				android.R.id.content)).getChildAt(0);
		if (view == null) {
			return;
		}
		if (nearby) {
			params.screenBrightness = 0.1f;
			view.setVisibility(View.INVISIBLE);
		} else {
			params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
			view.setVisibility(View.VISIBLE);
		}
		window.setAttributes(params);
	}

	private void onConnectBreak(int nret)
	{

		FileLog.e("emm", "connect break and reconnect");
		if (nret == 3)
		{
			AlertDialog.Builder build = new AlertDialog.Builder(this);
			build.setTitle(getString(UZResourcesIDFinder.getResStringID("link_tip")));
			if (nret == 3)
				build.setMessage(getString(UZResourcesIDFinder.getResStringID("link_room_lock")));
			else
				build.setMessage(getString(UZResourcesIDFinder.getResStringID("link_break")));
			build.setPositiveButton(getString(UZResourcesIDFinder.getResStringID("OK")),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							finish();
						}
					});
			build.show();
		}
		else
		{
			handler.postDelayed(new Runnable() {
				public void run() {
					bConnected = false;
					if(WeiyiMeetingClient.getInstance().isLiveMeeting()){
						if(BCViewPagerFragment!=null)
							BCViewPagerFragment.connectServer();
					}else{
						if(ViewPagerFragment!=null)
							ViewPagerFragment.connectServer(true);
					}
				}
			}, 1000);
		}
	}

	public void showHandsUpACK(final int toId) {

		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle(UZResourcesIDFinder.getResStringID("handsupack_remind"));
		builder.setMessage(UZResourcesIDFinder.getResStringID("handsupack_content"));
		builder.setPositiveButton(UZResourcesIDFinder.getResStringID("handsupack_yes"),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						WeiyiMeetingClient.getInstance().sendHandupACK(toId, true);
					}
				}).setNegativeButton(UZResourcesIDFinder.getResStringID("handsupack_no"),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						WeiyiMeetingClient.getInstance().sendHandupACK(toId, false);
					}
				});
		alertDialog = builder.create();
		alertDialog.getWindow().setBackgroundDrawableResource(
				android.R.color.transparent);
		alertDialog.show();
	}

}

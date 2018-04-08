package com.broadcast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.main.mme.view.mMediaController;
import com.meeting.ui.MeetingMemberFragment;
import com.meeting.ui.ViewPagerFragment;
import com.utils.BaseFragment;
import com.utils.BaseFragmentContainer;
import com.utils.FileLog;
import com.utils.Utitlties;
import com.utils.WeiyiMeetingClient;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.weiyicloud.whitepad.DocInterface;
import com.weiyicloud.whitepad.Face_Share_Fragment;
import com.weiyicloud.whitepad.Face_Share_Fragment.penClickListener;
import com.weiyicloud.whitepad.NotificationCenter;
import com.weiyicloud.whitepad.NotificationCenter.NotificationCenterDelegate;
import com.weiyicloud.whitepad.TL_PadAction.factoryType;
import com.weiyicloud.whitepad.FaceShareControl;
import com.weiyicloud.whitepad.MResource;
import com.weiyicloud.whitepad.ShareDoc;
import com.weiyicloud.whitepad.SharePadMgr;

import info.emm.meeting.MeetingUser;
import info.emm.meeting.Session;
import info.emm.sdk.VideoView;

import info.emm.weiyicloud.meeting.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


@SuppressLint("ResourceAsColor")
public class Broadcast_Activity extends ActionBarActivity implements OnClickListener,NotificationCenterDelegate,penClickListener,DocInterface {
	private ImageView img_switch_audio;
	private info.emm.sdk.VideoView broadcastViewWatch;
	private TextView txt_chat;
	private TextView txt_whitepad;
	private Face_Share_Fragment m_fragment_share;
	private BroadcastChat_Fragment chat_Fragment;
	private TextView txt_no_live;
	private RelativeLayout video_conter;
	private LinearLayout bottom_layout;
	private FrameLayout broadLayout;
	private FrameLayout broadLayout_top;
	private View lineView;
	private LinearLayout tab;
	private boolean isFullScreen;
	PowerManager.WakeLock m_wl;
	private int senderId;
	private TextView txt_online_num;
	Timer timer = new Timer();
	private TextView txt_meetingid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		UZResourcesIDFinder.init(this.getApplicationContext());
		setContentView(UZResourcesIDFinder.getResLayoutID("activity_broadcast"));

		SharePadMgr.getInstance().setDocInterface(this);

		video_conter = (RelativeLayout) findViewById(UZResourcesIDFinder.getResIdID("video_conter"));
		img_switch_audio = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("img_broad_switch_audio"));
		broadcastViewWatch = (VideoView) findViewById(UZResourcesIDFinder.getResIdID("broadcast_view_watch"));
		txt_chat = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_chat"));
		txt_whitepad = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_whitepad"));
		txt_no_live = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_no_live"));
		bottom_layout = (LinearLayout) findViewById(UZResourcesIDFinder.getResIdID("broad_bottom_layout"));
		broadLayout = (FrameLayout) findViewById(UZResourcesIDFinder.getResIdID("broadcast_content"));
		lineView = findViewById(UZResourcesIDFinder.getResIdID("line"));
		tab = (LinearLayout) findViewById(UZResourcesIDFinder.getResIdID("tab"));
		broadLayout_top = (FrameLayout) findViewById(UZResourcesIDFinder.getResIdID("broadcast_content_top"));
		txt_online_num = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_online_num"));
		txt_meetingid = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_meetingid"));

		img_switch_audio.setOnClickListener(this);
		txt_chat.setOnClickListener(this);
		txt_whitepad.setOnClickListener(this);
		txt_chat.setTextColor(Color.WHITE);
		txt_whitepad.setTextColor(Color.GRAY);
		Session.getInstance().setActivity(this);
		getExtraData();
		txt_meetingid.setText(WeiyiMeetingClient.getInstance().getM_strMeetingID());
		if(Session.getInstance().getMeetingtype() == 11){
			broadcastViewWatch.setVisibility(View.GONE);
			img_switch_audio.setVisibility(View.VISIBLE);
			txt_no_live.setVisibility(View.VISIBLE);
			txt_no_live.setTextColor(Color.BLACK);
			broadLayout_top.setVisibility(View.INVISIBLE);
			txt_whitepad.setVisibility(View.INVISIBLE);
			//			bottom_layout.setVisibility(View.GONE);


			m_fragment_share = new Face_Share_Fragment(this, Session.getInstance());
			m_fragment_share.setPenClickListener(this);
			m_fragment_share.setShareControl(Session.getInstance());
			m_fragment_share.setIsshowArr(false);
			FragmentManager manager = getSupportFragmentManager();
			FragmentTransaction tran = manager.beginTransaction();
			tran.replace(UZResourcesIDFinder.getResIdID("broadcast_content_top"), m_fragment_share);
			tran.commit();

		}else if(Session.getInstance().getMeetingtype()==13||Session.getInstance().getMeetingtype()==14){
			txt_whitepad.setVisibility(View.INVISIBLE);
		}
		//		MeetingSession.getInstance().getMeetingConfig();
		chat_Fragment = new BroadcastChat_Fragment();
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction tran = manager.beginTransaction();
		tran.replace(UZResourcesIDFinder.getResIdID("broadcast_content"), chat_Fragment);
		tran.commit();
		WindowManager wm = this.getWindowManager();
		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();
		LayoutParams params = video_conter.getLayoutParams();
		params.height = width/4*3;
		params.width = width;
		video_conter.setLayoutParams(params);

		initSensers();

	}

	public void initSensers() {

		PowerManager pm = (PowerManager)getSystemService(
				Context.POWER_SERVICE);
		m_wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyTag");
		m_wl.acquire();
	}
	@Override
	public void onBackPressed() {		
		showExitDialog();

		//		else{
		//			finish();
		//		}
		if (m_wl != null) {
			m_wl.release();
			m_wl = null;
		}
	}

	public void showExitDialog() {

		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle(UZResourcesIDFinder.getResStringID("remind"));
		builder.setMessage(UZResourcesIDFinder.getResStringID("logouts"));
		builder.setPositiveButton(UZResourcesIDFinder.getResStringID("sure"),
				new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Endmeeting();
			}
		}).setNegativeButton(UZResourcesIDFinder.getResStringID("cancel"),new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.getWindow().setBackgroundDrawableResource(
				android.R.color.transparent);
		alertDialog.show();

	}

	@Override
	protected void onStart() {
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.GETMEETING_DOC);
		NotificationCenter.getInstance().addObserver(this,
				FaceShareControl.DELMEETING_DOC);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.LIVE_WHITEPAD_JSON_BACK);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.GETMEETING_DOC);
		NotificationCenter.getInstance().addObserver(this,
				FaceShareControl.DELMEETING_DOC);
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
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.VIDEO_NOTIFY_CAMERA_DID_OPEN);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.NET_CONNECT_WARNING);

		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_TAKE_BACK_HOST_RIGHT);		
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_TAKE_BACK_FREE_SPEAK_RIGHT);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_TAKE_BACK_FREE_RECORD);

		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_ALLOW_FREE_HOST_RIGHT);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_ALLOW_FREE_SPEAK_RIGHT);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_CHAIRMAN_ALLOW_FREE_RECORD);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.DIRECT_MEETINGTYPE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_ING);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_SUCCESS);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_FAILED);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_BREAK);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_ENABLE_PRESENCE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_USER_IN);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_USER_OUT);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_AUDIO_CHANGE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_WATCH_VIDEO);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_SELF_VEDIO_WISH_CHANGE);
		NotificationCenter.getInstance().addObserver(this,
				MeetingMemberFragment.ControlVideo);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_USER_INLIST_COMPLETE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_VEDIO_CHANGE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_CHAIRMAN_CHANGE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_SYNC_WATCH_VIDEO);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_SYNC_VIDEO_MODE_CHANGE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_FAILED);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_BREAK);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.LIVECHANGE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_CAMERA_CHANGE);
		NotificationCenter.getInstance().addObserver(this, 
				WeiyiMeetingClient.UI_NOTIFY_SWITCH_MAIN_VIDEO);
		NotificationCenter.getInstance().addObserver(this, 
				WeiyiMeetingClient.START_BROADCAST);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_SHOW_SCREEN_PLAY);
		NotificationCenter.getInstance().addObserver(this,
				SharePadMgr.SHAREPAD_STATECHANGE);

		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_UNREAD_MSG);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_CHAT_RECEIVE_TEXT);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.REQUEST_CHAIRMAN);

		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_START_WEBSHRAE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_STOP_WEBSHRAE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_SHOW_WEBPAGE);

		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_SYNC_VIDEO_MODE_CHANGE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_SHOW_TABPAGE);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_GET_ONLINE_NUM);
		super.onStart();
		FileLog.e("emm", "connectServer");
		int thirduid = Session.getInstance().getUserMgr().getSelfUser()
				.getThirdID();
		String mid = WeiyiMeetingClient.getInstance().getM_strMeetingID();
		String name = WeiyiMeetingClient.getInstance().getM_strUserName();
		WeiyiMeetingClient.getInstance().entermeeting(name,
				mid, thirduid, false, 2,"");
		timer = new Timer();
		timer.schedule(new MyTask(),0, 5000);


	}
	class MyTask extends TimerTask {  

		@Override  
		public void run() {
			WeiyiMeetingClient.getInstance().getLiveOnLineNum();
		}  

	}

	@Override
	protected void onStop() {
		NotificationCenter.getInstance().removeObserver(this);
		if (m_wl != null) {
			m_wl.release();
			m_wl = null;
		}
		timer.cancel();
		super.onStop();
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
			//			int meetingType = bdarg.getInt("meetingtype");
			//			MeetingSession.getInstance().setMeetingtype(meetingType);
			String mid = bdarg.getString("meetingid");
			WeiyiMeetingClient.getInstance().setM_strMeetingID(mid);
			String httpServer = bdarg.getString("httpserver");
			String name = bdarg.getString("username");
			String pwd =  bdarg.getString("password");
			//my third id
			int thirdUID =  bdarg.getInt("thirduid");	
			int chatid =  bdarg.getInt("chatid");						
			boolean isInstMeeting = bdarg.getBoolean("isInstMeeting");
			int bAutoExitWeiyi = bdarg.getInt("bAutoExitWeiyi",0);
			String inviteAddress = bdarg.getString("inviteAddress");
			int bSupportSensor = bdarg.getInt("bSupportSensor",1);
			int bSupportRotation = bdarg.getInt("bSupportRotation",0);			
			int hideme = bdarg.getInt("hideme",0);
			String useridf = bdarg.getString("clientidentIfication");
			String title = bdarg.getString("title");
			int createid = bdarg.getInt("createrid");
			int userid = bdarg.getInt("userid");
			WeiyiMeetingClient.getInstance().setM_strUserName(name);
			WeiyiMeetingClient.getInstance().setM_instMeeting(isInstMeeting);			
			Session.getInstance().setWebHttpServerAddress(httpServer);
			WeiyiMeetingClient.getInstance().setM_pwd(pwd);
			WeiyiMeetingClient.getInstance().setM_chatid(chatid);
			Session.getInstance().getUserMgr().getSelfUser().setThirdID(thirdUID);
			WeiyiMeetingClient.getInstance().setM_autoExitWeiyi(bAutoExitWeiyi);
			WeiyiMeetingClient.getInstance().setM_inviteAddress(inviteAddress);
			WeiyiMeetingClient.getInstance().setM_bSupportSensor(bSupportSensor);

			WeiyiMeetingClient.getInstance().setM_bSupportRotation(bSupportRotation);
			WeiyiMeetingClient.getInstance().setM_hideme(hideme);
			WeiyiMeetingClient.getInstance().setM_userIdfaction(useridf);
			WeiyiMeetingClient.getInstance().setM_strMeetingName(title);
			//			WeiyiMeetingClient.getInstance().setCreaterid(createid);
			String photoPath = bdarg.getString("photoname");
			Log.d("emm", "photoname="+photoPath);
			Log.d("emm", "mid="+mid);

			WeiyiMeetingClient.getInstance().setViewer(true);
			saveHistory(WeiyiMeetingClient.getInstance().getM_strMeetingID());

		}
		else
		{
			Log.e("meeting","facemeeting no has extras data");
		}
	}

	//	public void connectServer() {
	//		MeetingUser muself = MeetingSession.getInstance().getUserMgr().getSelfUser();
	//		FileLog.e("emm", "connectServer");
	//		int thirduid = MeetingSession.getInstance().getUserMgr().getSelfUser()
	//				.getThirdID();
	//		String mid = MeetingSession.getInstance().getM_strMeetingID();
	//		String name = MeetingSession.getInstance().getM_strUserName();
	//			MeetingSession.getInstance().JoinMeeting(
	//					MeetingSession.SIGNAL_SERVER,
	//					Integer.parseInt(MeetingSession.SIGNAL_SERVER_PORT), name,
	//					mid, thirduid, false, 2);
	//	}

	private void playbrocast(int status) {
		if(WeiyiMeetingClient.getInstance().isLiveMeeting()){
			if(WeiyiMeetingClient.getInstance().isViewer()){
				String mediaserver = WeiyiMeetingClient.getInstance().getLIVE_MEDIA_SERVER();;
				String mediaport = WeiyiMeetingClient.getInstance().getLIVE_MEDIA_PORT();
				String path = "rtmp://"+mediaserver+":"+mediaport+"/live/"+Session.getInstance().getMeetingId();
				if(Session.getInstance().getMeetingtype()==14){
					if(status == 1){
						broadcastViewWatch.setVisibility(View.VISIBLE);
						txt_no_live.setVisibility(View.INVISIBLE);
						WeiyiMeetingClient.getInstance().playBroadCasting(path,broadcastViewWatch,0);
					}else if(status == 0){
						broadcastViewWatch.setVisibility(View.INVISIBLE);
						txt_no_live.setText(getString(UZResourcesIDFinder.getResStringID("broadcast_over")));
						txt_no_live.setTextColor(Color.BLACK);
						txt_no_live.setVisibility(View.VISIBLE);
						WeiyiMeetingClient.getInstance().unplayBroadCasting();
					}
				}else if(Session.getInstance().getMeetingtype()==11){
					if(status == 1){
						broadcastViewWatch.setVisibility(View.VISIBLE);
						txt_no_live.setVisibility(View.INVISIBLE);
						WeiyiMeetingClient.getInstance().playBroadCasting(path,null,WeiyiMeetingClient.getInstance().isM_scattype());
					}else if(status == 0){
						broadcastViewWatch.setVisibility(View.INVISIBLE);
						txt_no_live.setText(getString(UZResourcesIDFinder.getResStringID("broadcast_over")));
						txt_no_live.setTextColor(Color.BLACK);
						txt_no_live.setVisibility(View.VISIBLE);
						WeiyiMeetingClient.getInstance().unplayBroadCasting();
					}
				}else{
					if(status == 1){
						broadcastViewWatch.setVisibility(View.VISIBLE);
						txt_no_live.setVisibility(View.INVISIBLE);
						WeiyiMeetingClient.getInstance().playBroadCasting(path,broadcastViewWatch,WeiyiMeetingClient.getInstance().isM_scattype());
					}else if(status == 0){
						broadcastViewWatch.setVisibility(View.INVISIBLE);
						txt_no_live.setText(getString(UZResourcesIDFinder.getResStringID("broadcast_over")));
						txt_no_live.setTextColor(Color.WHITE);
						txt_no_live.setVisibility(View.VISIBLE);
						WeiyiMeetingClient.getInstance().unplayBroadCasting();
					}
				}
			}

		}
	}

	@Override
	public void onClick(View v) {
		int nid = v.getId();
		if(nid == UZResourcesIDFinder.getResIdID("img_broad_switch_audio")){
			boolean loud = WeiyiMeetingClient.getInstance().getLoudSpeaker();
			WeiyiMeetingClient.getInstance().setLoudSpeaker(!loud);
			if (!loud) {
				img_switch_audio.setImageResource(UZResourcesIDFinder.getResDrawableID("voice_out"));
			} else {
				img_switch_audio.setImageResource(UZResourcesIDFinder.getResDrawableID("voice_in"));
			}
		}else if(nid == UZResourcesIDFinder.getResIdID("txt_chat")){
			if(Session.getInstance().getMeetingtype()!= 11){
				if(chat_Fragment == null){
					chat_Fragment = new BroadcastChat_Fragment();
				}
				txt_chat.setTextColor(Color.WHITE);
				txt_whitepad.setTextColor(Color.GRAY);
				FragmentManager manager = getSupportFragmentManager();
				FragmentTransaction tran = manager.beginTransaction();
				tran.replace(UZResourcesIDFinder.getResIdID("broadcast_content"), chat_Fragment);
				tran.commit();
			}
		}else if(nid == UZResourcesIDFinder.getResIdID("txt_whitepad")){
			m_fragment_share = new Face_Share_Fragment(
					this, Session.getInstance());
			m_fragment_share.setPenClickListener(this);
			m_fragment_share.setShareControl(Session
					.getInstance());
			m_fragment_share.setIsshowArr(false);
			FragmentManager manager = getSupportFragmentManager();
			FragmentTransaction tran = manager.beginTransaction();
			tran.replace(UZResourcesIDFinder.getResIdID("broadcast_content"), m_fragment_share);
			tran.commit();
			txt_chat.setTextColor(Color.GRAY);
			txt_whitepad.setTextColor(Color.WHITE);
		}

	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {   
			// land 
			bottom_layout.setVisibility(View.GONE);
			LayoutParams lp = video_conter.getLayoutParams();
			lp.height = LayoutParams.MATCH_PARENT;
			lp.width = LayoutParams.MATCH_PARENT;
			video_conter.setLayoutParams(lp);
		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {   
			// port 
			bottom_layout.setVisibility(View.VISIBLE);
			LayoutParams lp = video_conter.getLayoutParams();
			lp.height = 0;
			lp.width = LayoutParams.MATCH_PARENT;
			video_conter.setLayoutParams(lp);			
			WindowManager wm = this.getWindowManager();
			int width = wm.getDefaultDisplay().getWidth();
			int height = wm.getDefaultDisplay().getHeight();
			LayoutParams params = video_conter.getLayoutParams();
			params.height = width/4*3;
			params.width = width;
			video_conter.setLayoutParams(params);
		}
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
		}
	}

	public void Endmeeting() {
		WeiyiMeetingClient.getInstance().exitMeeting();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}
	public void ShowAlertDialog(final Activity activity, final String message) {
		activity.runOnUiThread(new Runnable() {
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
						}
					});
					builder.show().setCanceledOnTouchOutside(true);
				}
			}
		});
	}

	@Override
	public void didReceivedNotification(int id, final Object... args) {
		switch (id) {
		case WeiyiMeetingClient.NET_CONNECT_LEAVE: {
			finish();
			break;
		}	
		case WeiyiMeetingClient.NET_CONNECT_FAILED:
		{
			FileLog.e("emm", "NET_CONNECT_FAILED");
			onConnectBreak(0);
			break;
		}
		case WeiyiMeetingClient.NET_CONNECT_ENABLE_PRESENCE: 
		{
			WeiyiMeetingClient.getInstance().setCameraQuality(true);
			WeiyiMeetingClient.getInstance().setLoudSpeaker(true);
			Log.e("meeting", "NET_CONNECT_ENABLE_PRESENCE arg length="+ args.length);
			long currentTime = System.currentTimeMillis()/1000;                       
			long residualTime =Session.getInstance().getMeetingEndTime();
			long time = residualTime - currentTime;
			if(WeiyiMeetingClient.getInstance().isM_bAutoQuit() && residualTime != 0){
				new CountDownTimer(time*1000, 1000) {
					@Override
					public void onTick(long totaltime) {
						if(!Broadcast_Activity.this.isFinishing()){
							int times = (int) (totaltime/1000);
							if (times == 0){
								Toast.makeText(Broadcast_Activity.this, getString(UZResourcesIDFinder.getResStringID("exit_meeting")), Toast.LENGTH_LONG).show();
							}else if(times == 15*60){
								Toast.makeText(Broadcast_Activity.this, getString(UZResourcesIDFinder.getResStringID("exit_remind_15")), Toast.LENGTH_LONG).show();
							}else if (times == 5*60){
								Toast.makeText(Broadcast_Activity.this, getString(UZResourcesIDFinder.getResStringID("exit_remind_5")), Toast.LENGTH_LONG).show();
							}else if (times == 60){
								Toast.makeText(Broadcast_Activity.this, getString(UZResourcesIDFinder.getResStringID("exit_remind_1")), Toast.LENGTH_LONG).show();
							}else if (times == 15){
								Toast.makeText(Broadcast_Activity.this, getString(UZResourcesIDFinder.getResStringID("exit_remind_15_second")), Toast.LENGTH_LONG).show();
							}
						}
					}
					@Override
					public void onFinish() {
						WeiyiMeetingClient.getInstance().exitMeeting();
						Toast.makeText(Broadcast_Activity.this, getString(UZResourcesIDFinder.getResStringID("exit_meeting")), Toast.LENGTH_LONG).show();
					}
				}.start();
				Log.e("meeting", "NET_CONNECT_ENABLE_PRESENCE");
			}
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
			int nRet = (Integer) args[0];
			FileLog.e("emm", "NET_CONNECT_BREAK");
			onConnectBreak(nRet);
			break;
		}

		case WeiyiMeetingClient.UI_NOTIFY_USER_KICKOUT: {
			int res = (Integer) args[0];
			if (res == WeiyiMeetingClient.Kickout_ChairmanKickout) {
				Endmeeting();
				Toast.makeText(this, UZResourcesIDFinder.getResStringID("lng_mask_kickout_message"), Toast.LENGTH_SHORT).show();
				//				ShowAlertDialog(this,getString(R.string.lng_mask_kickout_message));
			} else if (res == WeiyiMeetingClient.Kickout_Repeat) {
				ShowAlertDialog(this,getString(UZResourcesIDFinder.getResStringID("lng_mask_accountlogined_message")));
			}
			break;
		}
		case WeiyiMeetingClient.UI_NOTIFY_MEETING_MODECHANGE:
			int fromid2 = WeiyiMeetingClient.getInstance().getChairManID();
			MeetingUser mu2 = Session.getInstance().getUserMgr().getUser(fromid2);
			if(mu2!=null)
			{
				String userName = mu2.getName();				
				String msg =getString(UZResourcesIDFinder.getResStringID("freedo_mode")); 
				Session.getInstance().sendSystemMsg(fromid2,userName,msg);
				Toast.makeText(Broadcast_Activity.this,getString(UZResourcesIDFinder.getResStringID("freedo_mode")), Toast.LENGTH_LONG).show();
			}
			break;
		case WeiyiMeetingClient.VIDEO_NOTIFY_CAMERA_DID_OPEN:
			Camera cam = (Camera) args[0];
			if(cam==null)
			{			
				//todo.. xiaomei
				AlertDialog.Builder builder = new AlertDialog.Builder(
						this);
				builder.setMessage(getString(UZResourcesIDFinder.getResStringID("alertdialog_title_camera")));
				builder.setPositiveButton(getString(UZResourcesIDFinder.getResStringID("alertdialog_ok")),
						new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0,
							int arg1) {
					}
				});
				builder.show();
			}
			break;
		case WeiyiMeetingClient.NET_CONNECT_WARNING:
			int result = (Integer) args[0];
			if(result==11)

				//todo.. xiaomei
			{	
				AlertDialog.Builder builder = new AlertDialog.Builder(
						this);
				builder.setMessage(getString(UZResourcesIDFinder.getResStringID("alertdialog_title_microphone")));
				builder.setPositiveButton(getString(UZResourcesIDFinder.getResStringID("alertdialog_ok")),
						new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0,
							int arg1) {
					}
				});
				builder.show();
				//ShowAlertDialog(this, message)
			}
			break;
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
				Toast.makeText(this,getString(UZResourcesIDFinder.getResStringID("request_in_queue")),Toast.LENGTH_LONG).show();
			else if(mu4.getAudioStatus() == Session.RequestSpeak_Allow)
				Toast.makeText(this,getString(UZResourcesIDFinder.getResStringID("speak_permission")),Toast.LENGTH_LONG).show();
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
				MeetingUser mu5 = Session.getInstance().getUserMgr().getUser(fromchairmanId);
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
		case WeiyiMeetingClient.NET_CONNECT_SUCCESS: {

		}
		break;
		case WeiyiMeetingClient.START_BROADCAST:
			int status1 = (Integer) args[0];
			if(status1 == 1){				
				senderId = (Integer) args[1];
			}
			if(Session.getInstance().getMeetingtype()==11){
				if(status1 == 1){					
					broadLayout_top.setVisibility(View.VISIBLE);
				}else{
					broadLayout_top.setVisibility(View.INVISIBLE);
				}
			}
			playbrocast(status1);
			break;
		case WeiyiMeetingClient.DIRECT_MEETINGTYPE:
			int type = Session.getInstance().getMeetingtype();
			if(type == 12){
				txt_whitepad.setVisibility(View.VISIBLE);
			}else{
				txt_whitepad.setVisibility(View.GONE);
			}
			if(type == 11){
				broadcastViewWatch.setVisibility(View.GONE);
				img_switch_audio.setVisibility(View.VISIBLE);
				txt_no_live.setVisibility(View.VISIBLE);
				txt_no_live.setTextColor(Color.BLACK);
				broadLayout_top.setVisibility(View.INVISIBLE);
				m_fragment_share = new Face_Share_Fragment(
						this,  Session.getInstance());
				m_fragment_share.setPenClickListener(this);
				m_fragment_share.setShareControl(Session
						.getInstance());
				m_fragment_share.setIsshowArr(false);
				FragmentManager manager = getSupportFragmentManager();
				FragmentTransaction tran = manager.beginTransaction();
				tran.replace(UZResourcesIDFinder.getResIdID("broadcast_content_top"), m_fragment_share);
				tran.commit();

			}
			break;
		case WeiyiMeetingClient.GETMEETING_DOC: {
			//			connectServer();
			break;
		}
		case WeiyiMeetingClient.NET_CONNECT_USER_OUT:
			Log.d("emm", "userout");
			int userid = (Integer) args[0];
			if(Session.getInstance().getMeetingtype()==11){
				broadLayout_top.setVisibility(View.INVISIBLE);
			}
			if(userid == senderId){
				playbrocast(0);
			}
			break;
		case WeiyiMeetingClient.NET_CONNECT_USER_IN:
			Log.d("emm", "userout");
			break;

		case WeiyiMeetingClient.UI_NOTIFY_GET_ONLINE_NUM:
			int num = (Integer) args[0];
			int tim = num/10;
			if(tim<5){
				tim = 5;
			}else if(tim>300){
				tim = 300;
			}
			timer.cancel();
			timer = new Timer();
			timer.schedule(new MyTask(),tim*1000, tim*1000);
			txt_online_num.setText(num+"");
			break;
		default:
			break;

		}


	}

	@Override
	public void OnPenClick(boolean bShowPoints) {
		// TODO Auto-generated method stub

	}

	@Override
	public void UploadingFileFinish(int fileid, int pagenum, String filename,
			String swfpath) {
		// TODO Auto-generated method stub

	}

	@Override
	public void UploadingFileFailed(int operationcount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void ChangedUploadProgress(int progress) {
		// TODO Auto-generated method stub

	}


	@Override
	public void DelmeetingFile(int id, int pageid, String filename,
			String fileurl) {


	}
	private void saveHistory(String ID) {

		SharedPreferences sp = WeiyiMeetingClient.getApplicationContext()
				.getSharedPreferences("live_id", 0);

		String longhistory = sp.getString("history", "");

		String newString = longhistory;
		if (longhistory.contains(ID + ",")) {
			newString = longhistory.replace(ID + ",", "");
		}
		StringBuilder sb = new StringBuilder(newString);
		sb.insert(0, ID + ",");
		sp.edit().putString("history", sb.toString()).commit();

	}

}

package com.broadcast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.main.mme.view.mMediaController;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.utils.Utitlties;
import com.utils.VodMsgList;
import com.utils.WeiyiMeetingClient;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.weiyicloud.whitepad.FaceShareControl;
import com.weiyicloud.whitepad.Face_Share_Fragment;
import com.weiyicloud.whitepad.Face_Share_Fragment.penClickListener;
import com.weiyicloud.whitepad.GetMeetingFileCallBack;
import com.weiyicloud.whitepad.NotificationCenter;
import com.weiyicloud.whitepad.NotificationCenter.NotificationCenterDelegate;

import java.util.Timer;
import java.util.TimerTask;

import info.emm.meeting.Session;


@SuppressLint("ResourceAsColor")
public class BroadcastPlayBack_Activity extends ActionBarActivity implements OnClickListener,NotificationCenterDelegate,penClickListener,com.main.mme.view.mMediaController.MediaPlayerControl {
	private ImageView img_switch_audio;
	private android.widget.VideoView video_play_back;
	private Face_Share_Fragment m_fragment_share;
	private BroadcastChat_Fragment chat_Fragment;
	private TextView txt_no_live;
	private String httpUrl;
	private String jsonUrl;
	private String pichttpUrl;
	private RelativeLayout video_conter;
	private LinearLayout bottom_layout;
	private VodMsgList m_pageList;
	private VodMsgList m_shapeList;
	private FrameLayout broadLayout;
	private FrameLayout broadLayout_top;
	private View lineView;
	private LinearLayout tab;
	Timer timer = new Timer();
	//	Timer buftimer = new Timer();
	private boolean isFullScreen;
	PowerManager.WakeLock m_wl;
	mMediaController controller;
	OnClickListener pageclick;
	ImageView img_default;
	ImageView img_play_state;
	private ImageLoader loader=ImageLoader.getInstance();
	private DisplayImageOptions options;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		UZResourcesIDFinder.init(this.getApplicationContext());
		setContentView(UZResourcesIDFinder.getResLayoutID("activity_broadcast_playback"));

		//		Session.getInstance().clearWhitePad();
		video_conter = (RelativeLayout) findViewById(UZResourcesIDFinder.getResIdID("video_conter"));
		img_switch_audio = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("img_broad_switch_audio"));
		txt_no_live = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_no_live"));
		video_play_back = (android.widget.VideoView) findViewById(UZResourcesIDFinder.getResIdID("video_play_back"));
		bottom_layout = (LinearLayout) findViewById(UZResourcesIDFinder.getResIdID("broad_bottom_layout"));
		broadLayout = (FrameLayout) findViewById(UZResourcesIDFinder.getResIdID("broadcast_content"));
		lineView = findViewById(UZResourcesIDFinder.getResIdID("line"));
		tab = (LinearLayout) findViewById(UZResourcesIDFinder.getResIdID("tab"));
		broadLayout_top = (FrameLayout) findViewById(UZResourcesIDFinder.getResIdID("broadcast_content_top"));
		img_play_state = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("img_play_state"));
		img_default = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("img_default"));
		img_switch_audio.setOnClickListener(this);
		img_play_state.setOnClickListener(this);
		Session.getInstance().setActivity(this);
		options = Utitlties.getImgOpt(UZResourcesIDFinder.getResDrawableID("broad_playback_default"), UZResourcesIDFinder.getResDrawableID("broad_playback_default"));
		getExtraData();
		pageclick = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(httpUrl!=null&&!httpUrl.isEmpty()){					
					controller.show(); 
				}

			}
		};
		video_conter.setOnClickListener(pageclick);

		//		MeetingSession.getInstance().getMeetingConfig();
		if(httpUrl==null||httpUrl.isEmpty()){			
			chat_Fragment = new BroadcastChat_Fragment();
			FragmentManager manager = getSupportFragmentManager();
			FragmentTransaction tran = manager.beginTransaction();
			tran.replace(UZResourcesIDFinder.getResIdID("broadcast_content"), chat_Fragment);
			tran.commit();
			video_play_back.setVisibility(View.INVISIBLE);
			WindowManager wm = this.getWindowManager();
			int width = wm.getDefaultDisplay().getWidth();
			int height = wm.getDefaultDisplay().getHeight();
			LayoutParams params = video_conter.getLayoutParams();
			params.height = width/4*3;
			params.width = width;
			video_conter.setLayoutParams(params);

		}else{
			txt_no_live.setVisibility(View.GONE);
			img_switch_audio.setVisibility(View.GONE);
			video_conter.setBackgroundResource(UZResourcesIDFinder.getResColorID("black"));
			video_play_back.setVisibility(View.VISIBLE);
			Uri uri = Uri.parse(httpUrl);
			video_play_back.setVideoURI(uri);
			controller =new mMediaController(this);
			controller.setAnchorView(video_conter);
			controller.setMediaPlayer(this);
			video_play_back.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					video_play_back.resume();
					video_play_back.pause();
					Session.getInstance().clearWhitePad();
					if(m_pageList != null){						
						m_pageList.reSet();
					}
					if(m_shapeList != null){						
						m_shapeList.reSet();
					}
				}
			});
			video_play_back.setOnErrorListener(new OnErrorListener() {

				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					timer.cancel();
					return true;
				}
			});
			video_play_back.setOnPreparedListener(new OnPreparedListener() {

				@Override
				public void onPrepared(MediaPlayer mp) {
					mp.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {

						@Override
						public void onBufferingUpdate(MediaPlayer mp, int percent) {
							Log.d("xiao", "percent="+percent);

						}
					});
					mp.setOnSeekCompleteListener(new OnSeekCompleteListener() {

						@Override
						public void onSeekComplete(MediaPlayer mp) {
							video_play_back.start();

						}
					});

				}
			});


			//		    img_default.setImageBitmap(bitmap);
			img_default.setVisibility(View.VISIBLE);
			loader.displayImage(pichttpUrl, img_default);

			jsonUrl = httpUrl.substring(0, httpUrl.indexOf("playlist.m3u8"))+"live.json";
			int type = Session.getInstance().getMeetingtype();
			if(type == 11){
				broadLayout_top.setVisibility(View.VISIBLE);
				bottom_layout.setVisibility(View.GONE);
				m_fragment_share = new Face_Share_Fragment(
						pageclick, Session.getInstance());
				m_fragment_share.setPenClickListener(this);
				m_fragment_share.setShareControl(Session
						.getInstance());
				m_fragment_share.setIsshowArr(false);
				FragmentManager manager = getSupportFragmentManager();
				FragmentTransaction tran = manager.beginTransaction();
				tran.replace(UZResourcesIDFinder.getResIdID("broadcast_content_top"), m_fragment_share);
				tran.commit();
			}
			if(type == 12){
				m_fragment_share = new Face_Share_Fragment(
						pageclick,Session.getInstance());
				m_fragment_share.setPenClickListener(this);
				m_fragment_share.setShareControl(Session
						.getInstance());
				m_fragment_share.setIsshowArr(false);
				FragmentManager manager = getSupportFragmentManager();
				FragmentTransaction tran = manager.beginTransaction();
				tran.replace(UZResourcesIDFinder.getResIdID("broadcast_content"), m_fragment_share);
				tran.commit();
			}
			if(type == 13||type == 14){
				bottom_layout.setVisibility(View.GONE);
				LayoutParams lp = video_conter.getLayoutParams();
				lp.height = LayoutParams.MATCH_PARENT;
				lp.width = LayoutParams.MATCH_PARENT;
				video_conter.setLayoutParams(lp);
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
			WeiyiMeetingClient.getInstance().getJSON(jsonUrl);
		}
		initSensers();

	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	public void initSensers() {

		PowerManager pm = (PowerManager)getSystemService(
				Context.POWER_SERVICE);
		m_wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyTag");
		m_wl.acquire();
	}
	@Override
	public void onBackPressed() {
		if(Session.getInstance().isM_bInmeeting()){			
			showExitDialog();
		}else{
			timer.cancel();
			//			buftimer.cancel();
			finish();
		}
		if (m_wl != null) {
			m_wl.release();
			m_wl = null;
		}
	}

	public void showExitDialog() {

		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle(UZResourcesIDFinder.getResStringID("remind"));
		builder.setMessage(UZResourcesIDFinder.getResStringID("logouts"));
		builder.setPositiveButton(UZResourcesIDFinder.getResStringID("sure"),new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
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

	}

	@Override
	protected void onStart() {

		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.LIVE_WHITEPAD_JSON_BACK);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.GETMEETING_CONFIG);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.GETMEETING_DOC);
		NotificationCenter.getInstance().addObserver(this,
				FaceShareControl.DELMEETING_DOC);


		//		connectServer();
		if(Session.getInstance().getMeetingtype()==12){			
			WindowManager wm = this.getWindowManager();
			int width = wm.getDefaultDisplay().getWidth();
			int height = wm.getDefaultDisplay().getHeight();
			LayoutParams params = video_conter.getLayoutParams();
			params.height = width/4*3;
			params.width = width;
			video_conter.setLayoutParams(params);
		}
		if(Session.getInstance().getMeetingtype() == 11){
			WindowManager wm = this.getWindowManager();
			int width = wm.getDefaultDisplay().getWidth();
			int height = wm.getDefaultDisplay().getHeight();
			LayoutParams params = video_conter.getLayoutParams();
			params.height = height;
			params.width = width;
			video_conter.setLayoutParams(params);
		}

		super.onStart();
	}

	@Override
	protected void onStop() {
		NotificationCenter.getInstance().removeObserver(this);
		if (m_wl != null) {
			m_wl.release();
			m_wl = null;
		}
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
			httpUrl = bdarg.getString("httpurl");
			int meetingType = bdarg.getInt("meetingtype");
			pichttpUrl = bdarg.getString("pichttpUrl");
			String mid = bdarg.getString("meetingid");
			Session.getInstance().setMeetingtype(meetingType);
			WeiyiMeetingClient.getInstance().setM_strMeetingID(mid);

		}
		else
		{
			Log.e("meeting","facemeeting no has extras data");
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
		}else if(nid == UZResourcesIDFinder.getResIdID("img_play_state")){
			if(isPlaying()){
				pause();

			}else{
				start();
			}
			controller.show();
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
			if(Session.getInstance().getMeetingtype()==12||httpUrl==null||!httpUrl.isEmpty()){				
				WindowManager wm = this.getWindowManager();
				int width = wm.getDefaultDisplay().getWidth();
				int height = wm.getDefaultDisplay().getHeight();
				LayoutParams params = video_conter.getLayoutParams();
				params.height = width/4*3;
				params.width = width;
				video_conter.setLayoutParams(params);
			}
		}
		if(Session.getInstance().getMeetingtype()==11){
			img_switch_audio.setVisibility(View.GONE);
			txt_no_live.setTextColor(Color.BLACK);
			broadLayout_top.setVisibility(View.VISIBLE);
			bottom_layout.setVisibility(View.GONE);

			LayoutParams lp = video_conter.getLayoutParams();
			lp.height = LayoutParams.MATCH_PARENT;
			lp.width = LayoutParams.MATCH_PARENT;
			video_conter.setLayoutParams(lp);
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

		case WeiyiMeetingClient.GETMEETING_DOC: {
			if(httpUrl==null||httpUrl.isEmpty()){
				if(Session.getInstance().getMeetingtype() == 11){
					//					video_play_back.setVisibility(View.INVISIBLE);
					img_switch_audio.setVisibility(View.GONE);
					txt_no_live.setVisibility(View.VISIBLE);
					broadLayout_top.setVisibility(View.VISIBLE);
					bottom_layout.setVisibility(View.GONE);


					m_fragment_share = new Face_Share_Fragment(
							pageclick, Session.getInstance());
					m_fragment_share.setPenClickListener(this);
					m_fragment_share.setShareControl(Session
							.getInstance());
					m_fragment_share.setIsshowArr(false);
					FragmentManager manager = getSupportFragmentManager();
					FragmentTransaction tran = manager.beginTransaction();
					tran.replace(UZResourcesIDFinder.getResIdID("broadcast_content_top"), m_fragment_share);
					tran.commit();

				}
			}
			break;
		}
		case WeiyiMeetingClient.LIVE_WHITEPAD_JSON_BACK:
			Utitlties.RunOnUIThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(args[0]!=null){
						m_pageList = (VodMsgList) args[0];
					}
					if(args[1]!=null){
						m_shapeList = (VodMsgList) args[1];
					}
					WeiyiMeetingClient.getInstance().getMeetingFile_UI(Integer.parseInt(Session.getInstance().getM_strMeetingID()), new GetMeetingFileCallBack() {

						@Override
						public void GetmeetingFile(int code) {
							// TODO Auto-generated method stub							
							timer = new Timer();
							timer.schedule(new MyTask(), 0, 500); 
						}
					});
				}
			});

			break;
		}


	}

	@Override
	public void OnPenClick(boolean bShowPoints) {
		// TODO Auto-generated method stub

	}

	//	private void DoMsg(int tm)
	//	{
	//		try {
	//			if(m_pageList!=null)
	//			{
	//				JSONObject ret = m_pageList.OnTS(tm);
	//				if(ret!=null)
	//				{
	//					for (int i = 0; i < ret.getJSONArray("msgs").length(); i++) {
	//						JSONObject msg = ret.getJSONArray("msgs").getJSONObject(i);
	//						if(!msg.has("body")){
	//							continue;
	//						}
	//						final JSONObject obj = new JSONObject();
	//						obj.put("name", "ShowPage");
	//						obj.put("body", msg.get("body"));
	//						Utitlties.RunOnUIThread(new Runnable() {
	//
	//							@Override
	//							public void run() {
	//								// TODO Auto-generated method stub								
	//								Session.getInstance().whitePadChange(obj, null, null);
	//							}
	//						});
	//					}
	//				}
	//			}
	//
	//			if(m_shapeList != null)
	//			{
	//				JSONObject ret1 = m_shapeList.OnTS(tm);
	//				if(ret1!=null)
	//				{
	//					for (int i = 0; i < ret1.getJSONArray("msgs").length(); i++) {
	//						final JSONObject msg1 = ret1.getJSONArray("msgs").getJSONObject(i);
	//						if(!msg1.has("add") || !msg1.has("msg"))
	//							continue;
	//						if(msg1.has("msg")){
	//							JSONObject msg = msg1.getJSONObject("msg");
	//							String strbody = msg.getString("body");
	//							final byte[] shapedata = Base64.decode(strbody, Base64.DEFAULT);
	//							Utitlties.RunOnUIThread(new Runnable() {
	//
	//								@Override
	//								public void run() {
	//									// TODO Auto-generated method stub									
	//									try {
	//										Session.getInstance().whitePadChange(msg1.getJSONObject("msg"), shapedata, msg1.getInt("add")==1);
	//									} catch (JSONException e) {
	//										// TODO Auto-generated catch block
	//										e.printStackTrace();
	//									}
	//								}
	//							});
	//						}						
	//					}
	//
	//				}
	//			}
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}
	//
	//	}
	class MyTask extends TimerTask {  

		@Override  
		public void run() {
			WeiyiMeetingClient.getInstance().DoMsg(video_play_back.getCurrentPosition());  
			Log.d("emm", "video_play_back.getCurrentPosition()="+video_play_back.getCurrentPosition());
		}  

	}
	class buffTask extends TimerTask {  

		@Override  
		public void run() {
			Log.d("xiao", "BufferPercentage="+video_play_back.getBufferPercentage());
		}  

	}
	@Override
	public void start() {
		video_play_back.start();
		//		buftimer = new Timer();
		//		buftimer.schedule(new buffTask(),0,1000);
	}
	@Override
	public void pause() {
		video_play_back.pause();
		//		buftimer.cancel();

	}
	@Override
	public int getDuration() {
		// TODO Auto-generated method stub
		return video_play_back.getDuration();
	}
	@Override
	public int getCurrentPosition() {
		// TODO Auto-generated method stub
		return video_play_back.getCurrentPosition();
	}
	@Override
	public void seekTo(int pos) {
		video_play_back.seekTo(pos);

	}
	@Override
	public boolean isPlaying() {
		// TODO Auto-generated method stub
		if(video_play_back.isPlaying()){
			img_default.setVisibility(View.INVISIBLE);
		}
		return video_play_back.isPlaying();
	}
	@Override
	public int getBufferPercentage() {
		// TODO Auto-generated method stub
		return video_play_back.getBufferPercentage();
	}
	@Override
	public boolean canPause() {
		// TODO Auto-generated method stub
		return video_play_back.canPause();
	}
	@Override
	public boolean canSeekBackward() {
		// TODO Auto-generated method stub
		return video_play_back.canSeekBackward();
	}
	@Override
	public boolean canSeekForward() {
		// TODO Auto-generated method stub
		return video_play_back.canSeekForward();
	}
	@Override
	public boolean isFullScreen() {
		// TODO Auto-generated method stub
		return isFullScreen;
	}
	@Override
	public void toggleFullScreen() {
		if(isFullScreen){
			bottom_layout.setVisibility(View.VISIBLE);
			LayoutParams lp = video_conter.getLayoutParams();
			lp.height = 0;
			lp.width = LayoutParams.MATCH_PARENT;
			video_conter.setLayoutParams(lp);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			//			img_full_screen.setImageResource(R.drawable.full_screen);
		}else{				
			bottom_layout.setVisibility(View.GONE);
			LayoutParams lp = video_conter.getLayoutParams();
			lp.height = LayoutParams.MATCH_PARENT;
			lp.width = LayoutParams.MATCH_PARENT;
			video_conter.setLayoutParams(lp);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			//			img_full_screen.setImageResource(R.drawable.full_screen_quit);
		}
		isFullScreen = !isFullScreen;

	}
	@Override
	public boolean canFullScreen() {
		int type = Session.getInstance().getMeetingtype();
		if(type==13||type == 11){
			return false;
		}else{
			return true;
		}
	}
	@Override
	public void show(boolean isshow) {
		if(img_play_state==null){
			return;
		}
		img_play_state.setVisibility(View.VISIBLE);
		if (isshow) {
			img_play_state.setImageResource(UZResourcesIDFinder.getResDrawableID("big_pause"));
		} else {
			img_play_state.setImageResource(UZResourcesIDFinder.getResDrawableID("big_play"));
		}

	}

	@Override
	public void hidePlayButton() {
		if(img_play_state==null){
			return;
		}
		img_play_state.setVisibility(View.INVISIBLE);

	}


}

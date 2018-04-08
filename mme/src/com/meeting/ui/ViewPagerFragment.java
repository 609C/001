package com.meeting.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.meeting.ui.adapter.ViewPagerAdapter;
import com.utils.BaseFragment;
import com.utils.FileLog;
import com.utils.Utitlties;
import com.utils.WeiyiMeetingClient;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.weiyicloud.whitepad.DocInterface;
import com.weiyicloud.whitepad.Face_Share_Fragment;
import com.weiyicloud.whitepad.Face_Share_Fragment.penClickListener;
import com.weiyicloud.whitepad.NotificationCenter;
import com.weiyicloud.whitepad.NotificationCenter.NotificationCenterDelegate;
import com.weiyicloud.whitepad.PaintPad;
import com.weiyicloud.whitepad.SharePadMgr;
import com.weiyicloud.whitepad.SharePadMgr.DataChangeListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import info.emm.meeting.ChatData;
import info.emm.meeting.MeetingUser;
import info.emm.meeting.Session;

//import org.apache.http.entity.mime.FormBodyPart;

@SuppressLint("HandlerLeak")
public class ViewPagerFragment extends BaseFragment implements
NotificationCenterDelegate, DataChangeListener, OnClickListener,
penClickListener, DocFragment.DocFragmentDelegate ,DocInterface{
	public ViewPagerControl m_vpMeeting;
	public ViewPagerAdapter mAdapter;
	FragmentManager fm;
	public Face_camera_Fragment m_fragmentCamera;
	public Face_Share_Fragment m_fragment_share;
	public Face_ScreenShare_Fragment m_fragment_screenshare;
	public Face_Movie_Fragment movie_Fragment;
	public PadMainFragment m_PadMainFragment;
	public ChairmainFragment chairmainFragment;
	public WebViewFragment webViewFragment;
	public MeetingDetailsFragment detailsFragment;
	public LinearLayout mLayoutTabPoints;
	public OnClickListener m_PageClickListener;
	public OnClickListener m_tabpointlistener;
	// public boolean mHasShare = false;
	public boolean mBShowPoints;
	public String strShareName;
	private int nLastShowtipUser = 0;

	boolean bConnected = false;

	ImageView img_menu;
	ImageView tv_switch_camera;
	private TextView tab_audio;
	private TextView m_tMeeting_id;
	private RelativeLayout m_lTop_Title;
	private LinearLayout m_lBttom_btns;
	private RelativeLayout m_ly_connecting;
	private TextView m_txt_connecting;
	private RelativeLayout m_vAudio;
	private ImageView img_audio;
	// private RelativeLayout m_vInvent;
	private RelativeLayout m_vMessage;
	private RelativeLayout m_vMember;
	private RelativeLayout m_vShare;
	private RelativeLayout m_vVideo;
	private ImageView img_exitmeeting;






	public MeetingMemberFragment memberFragment = new MeetingMemberFragment();
	public GroupChatFragment chatFragment = new GroupChatFragment();
	public DocFragment docFragment;
	public boolean isShowMember = false;
	public boolean isShowChat = false;

	public static boolean m_bIsfrontCamera = true;

	private boolean m_bisopenCamera = true;

	PowerManager.WakeLock m_wl;
	// xiaoyang
	// TextView btn_switch_cre;
	TextView btn_close_cre;
	TextView btn_switch_zhiliang;
	// private PopupWindow pop_Audio;
	private PopupWindow pop_video;
	private PopupWindow pop_share_doc;
	private PopupWindow pop_menu;
	private View view1;
	// TextView btn_switch_ado;
	// TextView btn_audio_colse;
	RelativeLayout rel_lis_chat;

	// private boolean loud = true;
	private boolean highquality;

	Timer timerhide = new Timer();
	// static String m_strShareFilePath = "";
	boolean m_blayoutsShow = true;
	boolean m_banimationing = false;
	// View view2;
	// View view;
	TimerTask task = null;

	// static int GET_SHARE_FILE = 20;
	// static int TAKE_SHARE_PHOTO = 21;
	static int MEETING_MEMBER = 22;
	static String m_strShareFilePath = "";


	static int TAKE_SHARE_PHOTO = 21;
	static int GET_SHARE_FILE = 20;
	boolean alreadyIn = false;
	SharedPreferences sp = null;
	SharedPreferences spphoto = null;

	MeetingUser muself = null;


	//xiaoyang
	//	public static final boolean isMeeting = false;
	private String tempcontent = "";
	private String temptime = "";
	public static boolean isStartLive = false;
	ChatData data;

	//xiaoyang

	boolean bDocFragmentFinish = false;


	@Override
	public void didFinish() {
		bDocFragmentFinish = true;
		if (bDocFragmentFinish&&m_vpMeeting!=null) {
			bDocFragmentFinish = false;
			m_vpMeeting.setCurrentItem(0);
			Log.e("emm",
					"viewpageFragement onresume show whiteboard******************");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (bDocFragmentFinish) {
			bDocFragmentFinish = false;
			m_vpMeeting.setCurrentItem(0);
			Log.e("emm",
					"viewpageFragement onresume show whiteboard******************");
		}

		Log.e("emm", "viewpageFragement onresume ******************");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("emm", "viewpageFragement onCreateView begin*******************");
		UZResourcesIDFinder.init(getActivity().getApplicationContext());
		//xiaoyang add �ĵ�ҳ����Ҫ����Ϣ
		if(WeiyiMeetingClient.getInstance().isSipMeeting()){
			highquality = WeiyiMeetingClient.getInstance().isHighquality();
		}else{
			highquality = true;
		}
		WeiyiMeetingClient.getInstance().switchCamera(m_bIsfrontCamera);
		SharePadMgr.getInstance().setDocInterface(this);
		m_FragmentContainer.setContainerViewID(UZResourcesIDFinder.getResIdID("viewpager_container"));
		m_FragmentContainer.setFragment(this);
		muself = Session.getInstance().getUserMgr().getSelfUser();

		if (getActivity() != null) {
			initPopWindow(getActivity().getLayoutInflater());
			sp = getActivity().getSharedPreferences("state", 0);
			// loud = sp.getBoolean("loud", true);
			m_bisopenCamera = sp.getBoolean("m_bisopenCamera", true);
			if(WeiyiMeetingClient.getInstance().isSipMeeting()){				
				highquality = WeiyiMeetingClient.getInstance().isHighquality();
			}else{
				highquality = sp.getBoolean("highquality", true);
			}
			m_bIsfrontCamera = sp.getBoolean("m_bIsfrontCamera", true);
		}
		if (fragmentView == null) {
			Log.e("emm", "viewpageFragement view fragmentView is null");

			fragmentView = inflater.inflate(UZResourcesIDFinder.getResLayoutID("fragment_viewpager"), null);
			m_vpMeeting = (ViewPagerControl) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("vPager_meeting"));
			mLayoutTabPoints = (LinearLayout) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("layout_tab_point"));
			m_lTop_Title = (RelativeLayout) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("top_layout"));
			m_tMeeting_id = (TextView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("text_meeting_id"));
			m_lBttom_btns = (LinearLayout) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("bottom_btns"));
			img_menu = (ImageView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("img_menu"));
			tv_switch_camera = (ImageView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("tv_switch_camera"));

			m_ly_connecting = (RelativeLayout) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("rly_connecting"));
			m_txt_connecting = (TextView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("textView_connecting"));

			m_vAudio = (RelativeLayout) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("button_audio"));
			img_audio = (ImageView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("img_audio"));
			m_vMessage = (RelativeLayout) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("button_message"));
			m_vMember = (RelativeLayout) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("button_member"));
			m_vVideo = (RelativeLayout) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("button_video"));
			m_vShare = (RelativeLayout) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("button_share"));
			tab_audio = (TextView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("tab_audio"));
			img_exitmeeting = (ImageView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("img_exitmeeting"));
			rel_lis_chat = (RelativeLayout) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("rel_lis_chat"));

			// m_ly_connecting.setOnClickListener(this);
			img_menu.setOnClickListener(this);
			tv_switch_camera.setOnClickListener(this);
			m_vAudio.setOnClickListener(this);
			// m_vInvent.setOnClickListener(this);
			m_vMessage.setOnClickListener(this);
			m_vMember.setOnClickListener(this);
			// m_ly_connecting.setOnClickListener(this);
			m_vVideo.setOnClickListener(this);
			m_vShare.setOnClickListener(this);
			img_exitmeeting.setOnClickListener(this);
			rel_lis_chat.setOnClickListener(this);
			//xiaoyang sip���鲻���л���Ƶ��������Ҫ���ص�����ǿ�ƺ���
			
			if(WeiyiMeetingClient.getInstance().isSipMeeting()){
				m_vVideo.setVisibility(View.GONE);
				getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}else{
				m_vVideo.setVisibility(View.VISIBLE);
			}
			if (WeiyiMeetingClient.getInstance().isM_bShowUserList()) {
				m_vMember.setVisibility(View.VISIBLE);
			} else {
				m_vMember.setVisibility(View.GONE);
			}
			if (WeiyiMeetingClient.getInstance().isM_bShowDocList()) {
				m_vShare.setVisibility(View.VISIBLE);
			} else {
				m_vShare.setVisibility(View.GONE);
			}
			if (WeiyiMeetingClient.getInstance().isM_bShowTextChat()) {
				m_vMessage.setVisibility(View.VISIBLE);
			} else {
				m_vMessage.setVisibility(View.GONE);
			}
			// m_vShare.setVisibility(View.GONE);
			tab_audio.setText(UZResourcesIDFinder.getResStringID("audio"));
			SharePadMgr.getInstance()
			.addOnDataChangeListener(this);
			m_PageClickListener = new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					//face_camera_fragment arg0==null,xueqiang change
					if (arg0!=null && arg0 instanceof PaintPad)
						onPageClick(true);
					else
						onPageClick(false);
				}
			};
			if (Utitlties.isPad(getActivity())) {
				m_PadMainFragment = new PadMainFragment(m_PageClickListener,
						this);
			} else {
				m_fragmentCamera = new Face_camera_Fragment(m_PageClickListener,m_FragmentContainer);
				if (WeiyiMeetingClient.getInstance().isM_bShowWhite()) {
					m_fragment_share = new Face_Share_Fragment(
							m_PageClickListener,Session.getInstance());
					m_fragment_share = (Face_Share_Fragment) SharePadMgr.getInstance().createWhitePadFragment(m_PageClickListener, Session.getInstance(), this, false,true,1);
//					m_fragment_share.setPenClickListener(this);
//					m_fragment_share.setShareControl(Session.getInstance());
				}else{
					m_vShare.setVisibility(View.GONE);
				}				
			}
			if (muself.getRole() != 2) {
				m_lBttom_btns.setVisibility(View.VISIBLE);
				img_menu.setVisibility(View.VISIBLE);
				rel_lis_chat.setVisibility(View.INVISIBLE);
			} else {
				m_lBttom_btns.setVisibility(View.INVISIBLE);
				img_menu.setVisibility(View.INVISIBLE);
				rel_lis_chat.setVisibility(View.VISIBLE);
			}

			m_fragment_screenshare = new Face_ScreenShare_Fragment(
					m_PageClickListener);
			movie_Fragment = new Face_Movie_Fragment(m_PageClickListener);
			webViewFragment = new WebViewFragment(m_PageClickListener);
			m_tabpointlistener = new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					for (int i = 0; i < mLayoutTabPoints.getChildCount(); i++) {
						View vc = mLayoutTabPoints.getChildAt(i);
						boolean vSelect = (arg0 == vc);
						vc.setSelected(vSelect);
						if (vSelect)
							m_vpMeeting.setCurrentItem(i, true);
					}
				}
			};
			mLayoutTabPoints.setVisibility(View.INVISIBLE);
			m_vpMeeting.setOffscreenPageLimit(3);
			m_vpMeeting.setOnPageChangeListener(new OnPageChangeListener() {

				@Override
				public void onPageScrollStateChanged(int arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onPageSelected(int arg0) {

					for (int i = 0; i < mLayoutTabPoints.getChildCount(); i++) {
						View v = mLayoutTabPoints.getChildAt(i);
						v.setSelected(arg0 == i);
					}
					FileLog.e("emm", "onPageSelected*********************");
					Fragment ft = mAdapter.getItem(arg0);
					//��ҳ����
					if (ft instanceof WebViewFragment)
						WeiyiMeetingClient.getInstance().requestShowTab(2);
					//
					else if (ft instanceof Face_ScreenShare_Fragment)
						WeiyiMeetingClient.getInstance().requestShowTab(1);
					else if (ft instanceof Face_Share_Fragment)
						WeiyiMeetingClient.getInstance().requestShowTab(0);
					else if(ft instanceof Face_Movie_Fragment){
						WeiyiMeetingClient.getInstance().requestShowTab(3);
					}
				}
			});

			task = new TimerTask() {
				public void run() {
					Utitlties.RunOnUIThread(new Runnable() {
						@Override
						public void run() {
							if (m_blayoutsShow)
								HideLayouts();
							ShowTabpoints(true);
						}
					});
				}
			};

			if (getActivity() != null) {
				ActionBar ab = ((FaceMeeting_Activity) getActivity())
						.getSupportActionBar();
				if (ab != null) {
					ab.hide();
				}
			}

//			if(!WeiyiMeetingClient.getInstance().getM_strMeetingName().trim().isEmpty())
//			{
//				m_tMeeting_id.setText(WeiyiMeetingClient.getInstance().getM_strMeetingID()
//						+ "-" + WeiyiMeetingClient.getInstance().getM_strMeetingName());
//			}
//			else
//			{
				m_tMeeting_id.setText(WeiyiMeetingClient.getInstance().getM_strMeetingID());
//			}


			startShareFile();
			initTabs();
			initSensers();
			//			ShowLayouts();
			HideLayouts();
			ShowTabpoints(true);
			//			connectToServer();

		} else {
			Log.e("emm",
					"viewpageFragement onCreateView not null*******************");
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		if (Session.getInstance().isM_bInmeeting()) {

			OnConnectted();
		}

		if (getActivity() != null) {
			((FaceMeeting_Activity) getActivity()).enableProximitySensor();
		}

		return fragmentView;
	}

	public boolean isM_bIsfrontCamera() {
		return m_bIsfrontCamera;
	}

	public void setM_bIsfrontCamera(boolean m_bIsfrontCamera) {
		this.m_bIsfrontCamera = m_bIsfrontCamera;
	}

	@Override
	public void onAttach(Activity activity) {
		Log.i("emm", "onAttach");
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		Log.i("emm", "onDetach");
		super.onDetach();
	}

	@Override
	public void onFragmentDestroy() {
		super.onFragmentDestroy();

	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		if (m_fragment_share.isAdded()){
			getActivity().getSupportFragmentManager().putFragment(outState, "mFragment", m_fragment_share); 
		}
		if (m_fragment_screenshare.isAdded()){
			getActivity().getSupportFragmentManager().putFragment(outState, "mFragment", m_fragment_screenshare); 
		}
		if (m_fragmentCamera.isAdded()){
			getActivity().getSupportFragmentManager().putFragment(outState, "mFragment", m_fragmentCamera); 
		}
		if (webViewFragment.isAdded()){
			getActivity().getSupportFragmentManager().putFragment(outState, "mFragment", webViewFragment); 
		}
		if (movie_Fragment.isAdded()){
			getActivity().getSupportFragmentManager().putFragment(outState, "mFragment", movie_Fragment); 
		}
	}
	public void initTabs() {
		if (getActivity() == null)
			return;

		if (m_PadMainFragment != null)
			mAdapter.addItem(m_PadMainFragment);
		else
			mAdapter.addItem(m_fragmentCamera);

		if (!WeiyiMeetingClient.getLinkName().isEmpty()
				&& !WeiyiMeetingClient.getLinkUrl().isEmpty()) {

			webViewFragment.loadUrl(WeiyiMeetingClient.getLinkUrl());
			mAdapter.addItem(webViewFragment);
		}

		m_vpMeeting.setAdapter(mAdapter);
		m_vpMeeting.setCurrentItem(1);
		mAdapter.notifyDataSetChanged();

		ChecktabCounts();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		// m_fragmentCamera.doVideoLayout();
	}
	@Override
	public void onPause() {
		super.onPause();
		if (pop_video != null) {
			pop_video.dismiss();
		}
		if (pop_share_doc != null)
			pop_share_doc.dismiss();
		if (pop_menu != null)
			pop_menu.dismiss();
	}

	public void ShowTabpoints(boolean bshow) {

		mBShowPoints = bshow;

		ChecktabCounts();

		if (m_PadMainFragment != null) {
			m_PadMainFragment.showCameraName(mBShowPoints);
		}
		if (m_fragmentCamera != null) {
			m_fragmentCamera.showCameraName(mBShowPoints);
		}
		if (m_fragment_share != null) {
			m_fragment_share.showName(mBShowPoints);
		}
		if (m_fragment_screenshare != null) {
			m_fragment_screenshare.showName(mBShowPoints);
		}
	}

	public void ChecktabCounts() {

		if (mLayoutTabPoints.getChildCount() < mAdapter.getCount()) {
			if (getActivity() == null)
				return;
			LinearLayout roundTab = (LinearLayout) View.inflate(getActivity(),
					UZResourcesIDFinder.getResLayoutID("round_tab"), null);
			roundTab.setId(mAdapter.getCount() - 1);
			roundTab.setOnClickListener(m_tabpointlistener);
			mLayoutTabPoints.addView(roundTab);
		} else if (mLayoutTabPoints.getChildCount() > mAdapter.getCount()) {
			mLayoutTabPoints.removeViewAt(mLayoutTabPoints.getChildCount() - 1);
		} else {
			if (mLayoutTabPoints != null) {

				int nCount = mAdapter.getCount();

				boolean bshow = mBShowPoints && (nCount > 1);
				Animation alphaAnimationShow = bshow ? new AlphaAnimation(0.0f,
						1.0f) : new AlphaAnimation(1.0f, 0.0f);

				alphaAnimationShow.setDuration(500);
				if (mLayoutTabPoints.getVisibility() == View.VISIBLE && bshow)
					return;
				if (mLayoutTabPoints.getVisibility() == View.INVISIBLE
						&& !bshow)
					return;

				mLayoutTabPoints.startAnimation(alphaAnimationShow);
				mLayoutTabPoints.setVisibility(bshow ? View.VISIBLE
						: View.INVISIBLE);
			}
			return;
		}
		ChecktabCounts();
	}

	// public boolean isSharingFromhere() {
	// return mHasShare;
	// }

	private boolean findSC() {
		boolean bFound = false;
		int count = mAdapter.getCount();
		for (int i = 0; i < count; i++) {
			Fragment ft = mAdapter.getItem(i);
			if (ft instanceof Face_ScreenShare_Fragment) {
				bFound = true;
				break;
			}
		}
		return bFound;
	}

	public void startShareScreen() {
		if (!findSC()) {
			if (m_fragment_screenshare != null) {
				mAdapter.addItem(m_fragment_screenshare);
				m_fragment_screenshare.start();
			}
			ChecktabCounts();
		}

		if (m_vpMeeting != null) {
			for (int i = 0; i < mAdapter.getCount(); i++) {
				Fragment ft = this.mAdapter.getItem(i);
				if (ft instanceof Face_ScreenShare_Fragment) {
					m_vpMeeting.setCurrentItem(i);
					return;
				}
			}
		}

		MeetingUser mu = Session
				.getInstance()
				.getUserMgr()
				.getUser(WeiyiMeetingClient.getInstance().getM_nScreenSharePeerID());
		if (mu != null) {
			strShareName = mu.getName();
			if (getActivity() != null) {
				String strTip = strShareName + " "
						+ getString(UZResourcesIDFinder.getResStringID("user_sharing_screen"));
				Toast.makeText(getActivity(), strTip, Toast.LENGTH_SHORT)
				.show();
			}
		}
	}

	public void stopShareScreen() {
		if (findSC()) {
			if (strShareName != null) {
				if (getActivity() != null) {
					String strTip = strShareName + " "
							+ getString(UZResourcesIDFinder.getResStringID("user_stop_sharing_screen"));

					Toast.makeText(getActivity(), strTip, Toast.LENGTH_SHORT)
					.show();
				}
			}
			m_fragment_screenshare.stop();
			mAdapter.removeItem(m_fragment_screenshare);
			ChecktabCounts();
		}
	}

	private boolean findPM() {
		boolean bFound = false;
		int count = mAdapter.getCount();
		for (int i = 0; i < count; i++) {
			Fragment ft = mAdapter.getItem(i);
			if (ft instanceof Face_Movie_Fragment) {
				bFound = true;
				break;
			}
		}
		return bFound;
	}

	public void startPlayMovie() {
		if (!findPM()) {
			if (movie_Fragment != null) {
				mAdapter.addItem(movie_Fragment);
				movie_Fragment.start();
			}
			ChecktabCounts();
		}

		if (m_vpMeeting != null) {
			for (int i = 0; i < mAdapter.getCount(); i++) {
				Fragment ft = this.mAdapter.getItem(i);
				if (ft instanceof Face_Movie_Fragment) {
					m_vpMeeting.setCurrentItem(i);
					return;
				}
			}
		}

		MeetingUser mu = Session
				.getInstance()
				.getUserMgr()
				.getUser(WeiyiMeetingClient.getInstance().getMoviePlayerId());
		if (mu != null) {
			strShareName = mu.getName();
			if (getActivity() != null) {
				String strTip = strShareName + " "
						+ getString(UZResourcesIDFinder.getResStringID("user_playing_movie"));
				Toast.makeText(getActivity(), strTip, Toast.LENGTH_SHORT)
				.show();
			}
		}
	}

	public void stopPlayMovie() {
		if (findPM()) {
			if (strShareName != null) {
				if (getActivity() != null) {
					String strTip = strShareName + " "
							+ getString(UZResourcesIDFinder.getResStringID("user_stop_playing_movie"));

					Toast.makeText(getActivity(), strTip, Toast.LENGTH_SHORT)
					.show();
				}
			}
			movie_Fragment.stop();
			mAdapter.removeItem(movie_Fragment);
			//			movie_Fragment = null;
			ChecktabCounts();
		}
	}

	private boolean findSF() {
		boolean bFound = false;
		int count = mAdapter.getCount();
		for (int i = 0; i < count; i++) {
			Fragment ft = mAdapter.getItem(i);
			if (ft instanceof Face_Share_Fragment) {
				bFound = true;
				break;
			}
		}
		return bFound;
	}

	public void startShareFile() {
		fm = getChildFragmentManager();
		mAdapter = new ViewPagerAdapter(fm);
		if (!findSF()&&m_fragment_share!=null) {
			mAdapter.addItem(m_fragment_share);
			LinearLayout roundTab = (LinearLayout) View.inflate(getActivity(),
					UZResourcesIDFinder.getResLayoutID("round_tab"), null);
			roundTab.setId(mAdapter.getCount() - 1);
			roundTab.setSelected(true);
			roundTab.setOnClickListener(m_tabpointlistener);
			mLayoutTabPoints.addView(roundTab);
		}
		MeetingUser mu = Session.getInstance().getUserMgr().getUser(Session.getInstance().nLastShowPageUser);
		if (nLastShowtipUser != Session.getInstance().nLastShowPageUser
				&& mu != null
				&& Session.getInstance().nLastShowPageUser != 0) {
			nLastShowtipUser = Session.getInstance().nLastShowPageUser;
			strShareName = mu.getName();
			if (getActivity() == null)
				return;
			String strTip = strShareName + " "
					+ getString(UZResourcesIDFinder.getResStringID("user_sharing_a_file"));
			Toast.makeText(getActivity(), strTip, Toast.LENGTH_LONG).show();
		}
	}

	public void stopShareFile() {
		if (findSF())
			if (m_fragment_share != null) {
				mAdapter.removeItem(m_fragment_share);
			}
		ChecktabCounts();
	}

	@Override
	public void didReceivedNotification(int id, Object... args) {
		switch (id) {
		case WeiyiMeetingClient.UI_NOTIFY_SHOW_SCREEN_PLAY: {
			boolean bwatch = (Boolean) args[0];
			if (bwatch
					&& WeiyiMeetingClient.getInstance().getM_nScreenSharePeerID() != 0) {
				startShareScreen();
			} else {
				stopShareScreen();
			}
			break;
		}

		case SharePadMgr.SHAREPAD_STATECHANGE: {
			// if (MeetingSession.getInstance().getSharePadMgr().isHasThings())
			// {
			// startShareFile();
			// } else {
			// stopShareFile();
			// }
		}
		break;
		/*
		 * case MeetingSession.NET_CONNECT_ENABLE_PRESENCE: { OnConnectted(); }
		 * break;
		 */

		/*
		 * case MeetingSession.NET_CONNECT_BREAK: {
		 * MeetingSession.getInstance().getSharePadMgr()
		 * .removeOnDataChangeListener(this); } break;
		 */

		/*
		 * case MeetingSession.NET_CONNECT_FAILED: { onConnectFaild(); } break;
		 */
		case WeiyiMeetingClient.UI_NOTIFY_CHAT_RECEIVE_TEXT: {
			NotificationCenter.getInstance().postNotificationName(
					WeiyiMeetingClient.UI_NOTIFY_USER_UNREAD_MSG);
			handler.sendMessage(new Message());

		}
		break;
		case WeiyiMeetingClient.UI_NOTIFY_USER_UNREAD_MSG: {

			handler.sendMessage(new Message());
		}
		break;
		case WeiyiMeetingClient.REQUEST_CHAIRMAN: {
			int result = (Integer) args[0];
			int myid = Session.getInstance().getUserMgr().getSelfUser().getPeerID();
			if (result == 0) {//qxm change 1 change to 0 
				if (getActivity() != null) {
					WeiyiMeetingClient.getInstance().changeChairMan(myid);
					chairmainFragment = new ChairmainFragment();
					m_FragmentContainer.PushFragment(chairmainFragment);
					HideLayouts();
					ShowTabpoints(true);
					// m_vpMeeting.setVisibility(View.GONE);

					((FaceMeeting_Activity) getActivity())
					.disableProximitySensor(false);
				}
			} else {
				errorTipDialog(UZResourcesIDFinder.getResStringID("request_chairman_fail"));
			}
			break;
		}
		case WeiyiMeetingClient.NET_CONNECT_SUCCESS: {
			int result = (Integer) args[0];
			if (result == 0) {
				// Log.e("emm","GETMEETING_CONFIG meeting id="+m_strMeetingID );
				saveHistory(WeiyiMeetingClient.getInstance().getM_strMeetingID());
			}
			else
			{
				if(m_txt_connecting!=null)
				{
					m_txt_connecting.setText(UZResourcesIDFinder.getResStringID("getconfig_failed"));
					handler.postDelayed(new Runnable() {
						public void run() {
							NotificationCenter.getInstance().postNotificationName(
									WeiyiMeetingClient.NET_CONNECT_LEAVE);
						}
					}, 3000);


				}
			}
		}
		break;

		case WeiyiMeetingClient.GETMEETING_DOC: {
			connectServer(false);
			break;
		}
		case WeiyiMeetingClient.UI_NOTIFY_USER_START_WEBSHRAE:
			mAdapter.addItem(webViewFragment);
			this.ChecktabCounts();
			break;
		case WeiyiMeetingClient.UI_NOTIFY_USER_STOP_WEBSHRAE:
			mAdapter.removeItem(webViewFragment);
			this.ChecktabCounts();
			break;
		case WeiyiMeetingClient.UI_NOTIFY_USER_SHOW_WEBPAGE:
			String url = (String) args[0];
			webViewFragment.loadUrl(url);
			break;
		case WeiyiMeetingClient.UI_NOTIFY_USER_SYNC_VIDEO_MODE_CHANGE:
			boolean mode = (Boolean) args[0];
			boolean auto = (Boolean) args[1];
			if (mode && auto)
				m_bisopenCamera = false;

			break;
		case WeiyiMeetingClient.UI_NOTIFY_USER_AUDIO_CHANGE:
			break;
		case WeiyiMeetingClient.UI_NOTIFY_SHOW_TABPAGE:
			int curSel = (Integer) args[0];
			if (curSel == 0) {
				// whiteboard
				if (m_vpMeeting != null)
					m_vpMeeting.setCurrentItem(0);
			} else if (curSel == 1) {
				// desktop share
				if (m_vpMeeting != null) {
					for (int i = 0; i < mAdapter.getCount(); i++) {
						Fragment ft = this.mAdapter.getItem(i);
						if (ft instanceof Face_ScreenShare_Fragment) {
							m_vpMeeting.setCurrentItem(i);
							return;
						}
					}
				} 
			} else if (curSel == 2) {
				// webpage share
				if (m_vpMeeting != null) {
					for (int i = 0; i < mAdapter.getCount(); i++) {
						Fragment ft = this.mAdapter.getItem(i);
						if (ft instanceof WebViewFragment) {
							m_vpMeeting.setCurrentItem(i);
							return;
						}
					}
				}
			}else if(curSel == 3){
				//camera
				if (m_vpMeeting != null) {
					for (int i = 0; i < mAdapter.getCount(); i++) {
						Fragment ft = this.mAdapter.getItem(i);
						if (ft instanceof Face_Movie_Fragment) {
							m_vpMeeting.setCurrentItem(i);
							return;
						}
					}
				}
			}
			break;
		case WeiyiMeetingClient.UI_NOTIFY_PLAY_MOVIE:
			if(WeiyiMeetingClient.getInstance().isMoviestate()){
				startPlayMovie();
			}else{
				stopPlayMovie();
			}
			break;
		case WeiyiMeetingClient.SHOWPAGE:
			m_vpMeeting.setCurrentItem(0);
			break;
		case WeiyiMeetingClient.VIDEO_NOTIFY_CAMERA_DID_OPEN:
			Camera cam = (Camera) args[0];
			if(cam==null)
			{			
				//todo.. xiaomei
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
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
						getActivity());
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
		default:
			break;

		}

	}

	public void connectServer(boolean bNetworkBreak) {
		changeAudioImage();		
		FileLog.e("emm", "connectServer");
		int thirduid = Session.getInstance().getUserMgr().getSelfUser()
				.getThirdID();
		String mid = WeiyiMeetingClient.getInstance().getM_strMeetingID();
		String name = WeiyiMeetingClient.getInstance().getM_strUserName();
		//xiaoyang change		
		WeiyiMeetingClient.getInstance().entermeeting( name, mid,
				thirduid, false, muself.getRole(),null);
		//qxm change  
		if(bNetworkBreak){//true ��ʾ�Ͽ�����
			if(m_txt_connecting!=null){
				m_ly_connecting.setVisibility(View.VISIBLE);
				m_txt_connecting.setText(UZResourcesIDFinder.getResStringID("connect_again"));
			}
		}else{//false 
			if(m_txt_connecting!=null)
				m_txt_connecting.setText(UZResourcesIDFinder.getResStringID("connecting"));
		}
	}

	public void changeAudioImage() {
		if (tab_audio == null)
			return;
		MeetingUser mu = Session.getInstance().getUserMgr()
				.getSelfUser();
		if (mu.getAudioStatus() == Session.RequestSpeak_Disable) {
			tab_audio.setText(UZResourcesIDFinder.getResStringID("audio"));
			// tab_audio.setSelected(false);
			img_audio.setImageResource(UZResourcesIDFinder.getResDrawableID("button_silence"));// button_audio
			// m_vAudio.setSelected(false);
		} else if (mu.getAudioStatus() == Session.RequestSpeak_Allow) {
			tab_audio.setText(UZResourcesIDFinder.getResStringID("audio"));
			// tab_audio.setSelected(true);
			img_audio.setImageResource(UZResourcesIDFinder.getResDrawableID("button_speak"));
			// m_vAudio.setSelected(true);
		} else if (mu.getAudioStatus() == Session.RequestSpeak_Pending) {
			tab_audio.setText(UZResourcesIDFinder.getResStringID("audio"));
			// tab_audio.setSelected(true);
			img_audio.setImageResource(UZResourcesIDFinder.getResDrawableID("button_speak_panding"));
		}

	}

	@Override
	public void onChange() {

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		SharePadMgr.getInstance()
		.removeOnDataChangeListener(this);
		// if (pop_Audio != null) {
		// pop_Audio.dismiss();
		// }
		if (pop_video != null) {
			pop_video.dismiss();
		}
		if (pop_share_doc != null)
			pop_share_doc.dismiss();
		if (pop_menu != null)
			pop_menu.dismiss();

	}

	public void onConnectFaild() {
		SharePadMgr.getInstance()
		.removeOnDataChangeListener(this);
		bConnected = false;
		if (getActivity() == null)
			return;
		AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
		build.setTitle(getString(UZResourcesIDFinder.getResStringID("link_tip")));
		build.setMessage(getString(UZResourcesIDFinder.getResStringID("link_faild")));
		build.setPositiveButton(getString(UZResourcesIDFinder.getResStringID("OK")),
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				getActivity().finish();
			}

		});
		build.show();
	}

	@SuppressWarnings("unused")
	@Override
	public void onClick(View v) {

		int nid = v.getId();
		if (nid == UZResourcesIDFinder.getResIdID("img_menu")) {
			// showExitDialog();
			if (getActivity() == null) {
				return;
			}
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View vie_menu = inflater.inflate(UZResourcesIDFinder.getResLayoutID("pop_chairmain_menu"), null);
			vie_menu.measure(0, 0);
			pop_menu = new PopupWindow(vie_menu, vie_menu.getMeasuredWidth(),
					vie_menu.getMeasuredHeight(), false);
			pop_menu.setBackgroundDrawable(new BitmapDrawable());
			pop_menu.setOutsideTouchable(true);
			pop_menu.setFocusable(true);
			pop_menu.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					timerhide = new Timer();
					timerhide.schedule(new whenHide(), 5000);
				}
			});
			TextView txt_requestchairman = (TextView) vie_menu
					.findViewById(UZResourcesIDFinder.getResIdID("txt_requestchairman"));
			TextView txt_applyspeaker = (TextView) vie_menu
					.findViewById(UZResourcesIDFinder.getResIdID("txt_applyspeaker_tv"));
			TextView txt_invite = (TextView) vie_menu
					.findViewById(UZResourcesIDFinder.getResIdID("txt_invite"));
			TextView txt_exit = (TextView) vie_menu.findViewById(UZResourcesIDFinder.getResIdID("txt_exit"));
			TextView  txt_details = (TextView) vie_menu.findViewById(UZResourcesIDFinder.getResIdID("txt_details"));
			if (WeiyiMeetingClient.getInstance().getMyPID() == WeiyiMeetingClient
					.getInstance().getChairManID()) {
				txt_requestchairman.setText(getString(UZResourcesIDFinder.getResStringID("chairmain_func")));
			} else {
				txt_requestchairman
				.setText(getString(UZResourcesIDFinder.getResStringID("str_request_chairman")));
			}

			if (WeiyiMeetingClient.getInstance().isM_bShowInvite()) {
				txt_invite.setVisibility(View.VISIBLE);
			} else {
				txt_invite.setVisibility(View.GONE);
			}

			if (WeiyiMeetingClient.getInstance().isM_bshowChairman()) {
				txt_requestchairman.setVisibility(View.VISIBLE);
			} else {
				txt_requestchairman.setVisibility(View.GONE);
			}

			if (WeiyiMeetingClient.getInstance().isM_bshowHost()) {
				txt_applyspeaker.setVisibility(View.VISIBLE);
			} else {
				txt_applyspeaker.setVisibility(View.GONE);
			}

			if (WeiyiMeetingClient.RequestHost_Allow == Session
					.getInstance().getUserMgr().getSelfUser().getHostStatus()) {
				txt_applyspeaker
				.setText(getString(UZResourcesIDFinder.getResStringID("viewpager_cancel_speaker")));
			} else if (WeiyiMeetingClient.RequestHost_Pending == Session
					.getInstance().getUserMgr().getSelfUser().getHostStatus()) {

				txt_applyspeaker
				.setText(getString(UZResourcesIDFinder.getResStringID("viewpager_apply_speaker")));
			} else {
				txt_applyspeaker
				.setText(getString(UZResourcesIDFinder.getResStringID("viewpager_apply_speaker")));
			}
			if(WeiyiMeetingClient.getInstance().isLiveMeeting()){
				txt_requestchairman.setVisibility(View.GONE);
				txt_applyspeaker.setVisibility(View.GONE);
				txt_details.setVisibility(View.GONE);
			}
			txt_requestchairman.setOnClickListener(this);
			txt_applyspeaker.setOnClickListener(this);
			txt_invite.setOnClickListener(this);
			txt_exit.setOnClickListener(this);
			txt_details.setOnClickListener(this);
//			int[] xys = calculatePopWindowPos(img_menu, m_lTop_Title);
			pop_menu.showAtLocation(m_lTop_Title, Gravity.TOP|Gravity.LEFT,
					m_lTop_Title.getWidth()-pop_menu.getWidth(), m_lTop_Title.getHeight()
					+m_lTop_Title.getHeight()/2);
			
//			pop_menu.showAtLocation(m_lTop_Title, Gravity.TOP,
//					xys[0], xys[1]);
			timerhide.cancel();
		} else if (nid == UZResourcesIDFinder.getResIdID("tv_switch_camera")) {

			m_bIsfrontCamera = !m_bIsfrontCamera;
			WeiyiMeetingClient.getInstance().switchCamera();
		} else if (nid == UZResourcesIDFinder.getResIdID("button_audio")) {
			// pop_Audio.getContentView().getMeasuredWidth();
			// int xoffset = m_vAudio.getLeft() + m_vAudio.getWidth() / 2
			// - (m_lBttom_btns.getWidth() / 2);
			PackageManager pm = getActivity().getPackageManager();
			boolean permission = (PackageManager.PERMISSION_GRANTED ==   
					pm.checkPermission("android.permission.RECORD_AUDIO", getActivity().getPackageName()));  
			if (permission) {
				MeetingUser mu = Session.getInstance().getUserMgr()
						.getSelfUser();
				if (mu.getAudioStatus() == Session.RequestSpeak_Allow
						|| mu.getAudioStatus() == WeiyiMeetingClient.RequestHost_Pending)
					WeiyiMeetingClient.getInstance().StopSpeaking();
				else
					WeiyiMeetingClient.getInstance().StartSpeaking();
				this.changeAudioImage();
			}else{
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				builder.setMessage(getString(UZResourcesIDFinder.getResStringID("alertdialog_title_microphone")));
				builder.setPositiveButton(getString(UZResourcesIDFinder.getResStringID("alertdialog_ok")),
						new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0,
							int arg1) {

					}
				});
				builder.show();
			}


			// pop_Audio.showAtLocation(m_lBttom_btns, Gravity.BOTTOM, xoffset,
			// m_vAudio.getHeight());
			timerhide.cancel();

		} else if (nid == UZResourcesIDFinder.getResIdID("button_message")) {

			for (int i = 0; i < Session.getInstance().getUserMgr()
					.getCount(); i++) {
				MeetingUser user = Session.getInstance().getUserMgr()
						.getUserByIndex(i);
				if (user != null)
					user.setUnreadMsg(0);
			}
			Session.getInstance().getUserMgr().getSelfUser()
			.setUnreadMsg(0);
			NotificationCenter.getInstance().postNotificationName(
					WeiyiMeetingClient.UI_NOTIFY_USER_UNREAD_MSG, 0);
			m_FragmentContainer.PushFragment(new GroupChatFragment());
		}

		else if (nid == UZResourcesIDFinder.getResIdID("button_member")) {
			ShowMembersActivity();
		} else if (nid == UZResourcesIDFinder.getResIdID("button_video")) {// xiaoyang
			if (pop_video.isShowing()) {
				pop_video.dismiss();

			} else {

				if (highquality) {
					btn_switch_zhiliang.setText(UZResourcesIDFinder.getResStringID("fast"));
				} else {
					btn_switch_zhiliang.setText(UZResourcesIDFinder.getResStringID("quality"));
				}
				if (m_bisopenCamera) {
					btn_close_cre.setText(getString(UZResourcesIDFinder.getResStringID("close_crea")));
					// m_vVideo.setSelected(false);
				} else {
					if (WeiyiMeetingClient.getInstance().isbHasFrontCamera()) {
						// m_vVideo.setSelected(true);
						if (m_bIsfrontCamera) {
							btn_close_cre
							.setText(getString(UZResourcesIDFinder.getResStringID("front_crea")));
						} else {
							btn_close_cre
							.setText(getString(UZResourcesIDFinder.getResStringID("switch_crea")));
						}
					} else {
						btn_close_cre.setText(getString(UZResourcesIDFinder.getResStringID("switch_crea")));
						m_vVideo.setSelected(false);
					}
				}
				pop_video.getContentView().getMeasuredWidth();
				int xoffset = m_vVideo.getLeft() + m_vVideo.getWidth() / 2
						- (m_lBttom_btns.getWidth() / 2);
				pop_video.showAtLocation(m_lBttom_btns, Gravity.BOTTOM,
						xoffset, m_lBttom_btns.getHeight());
				timerhide.cancel();
			}
		}

		else if (nid == UZResourcesIDFinder.getResIdID("btn_close_cre")) {

			m_bisopenCamera = !m_bisopenCamera;
			WeiyiMeetingClient.getInstance().setWatchMeWish(m_bisopenCamera);

			if (m_bisopenCamera) {
				btn_close_cre.setText(getString(UZResourcesIDFinder.getResStringID("close_crea")));
				m_vVideo.setSelected(false);
			} else {
				if (WeiyiMeetingClient.getInstance().isbHasFrontCamera()) {
					m_vVideo.setSelected(true);
					WeiyiMeetingClient.getInstance().switchCamera(m_bIsfrontCamera);
					if (m_bIsfrontCamera) {
						btn_close_cre.setText(getString(UZResourcesIDFinder.getResStringID("front_crea")));
					} else {
						btn_close_cre.setText(getString(UZResourcesIDFinder.getResStringID("switch_crea")));
					}
				} else {
					btn_close_cre.setText(getString(UZResourcesIDFinder.getResStringID("switch_crea")));
					WeiyiMeetingClient.getInstance()
					.setWatchMeWish(m_bisopenCamera);
					m_vVideo.setSelected(false);
				}
			}
			SharedPreferences.Editor editor = sp.edit();
			editor.putBoolean("m_bisopenCamera", m_bisopenCamera);
			editor.commit();
			pop_video.dismiss();
		} else if (nid == UZResourcesIDFinder.getResIdID("button_share")) {

			pop_share_doc.getContentView().getMeasuredWidth();

			int xoffset = m_vShare.getLeft() + m_vShare.getWidth() / 2
					- (m_lBttom_btns.getWidth() / 2);
			pop_share_doc.showAtLocation(m_lBttom_btns, Gravity.BOTTOM,
					xoffset, m_lBttom_btns.getHeight());
			if (pop_share_doc.isShowing()) {
				timerhide.cancel();
			}

		} else if (nid == UZResourcesIDFinder.getResIdID("btn_switch_quality")) {
			highquality = !highquality;
			WeiyiMeetingClient.getInstance().setCameraQuality(highquality);
			if (highquality) {
				btn_switch_zhiliang.setText(UZResourcesIDFinder.getResStringID("fast"));
			} else {
				btn_switch_zhiliang.setText(UZResourcesIDFinder.getResStringID("quality"));
			}
			SharedPreferences.Editor editor = sp.edit();
			editor.putBoolean("highquality", highquality);
			editor.commit();
			pop_video.dismiss();

		} else if (nid == UZResourcesIDFinder.getResIdID("btn_take_photo")) {
			pop_share_doc.dismiss();
			Session.getInstance().pauseLocalVideo();
			try {
				Intent takePictureIntent = new Intent(
						MediaStore.ACTION_IMAGE_CAPTURE);
				File image = Utitlties.generatePicturePath();
				if (image != null) {
					takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
							Uri.fromFile(image));
					m_strShareFilePath = image.getAbsolutePath();
					spphoto = getActivity().getSharedPreferences("spphoto", 0);
					if(spphoto!=null){
						spphoto.edit().putString("photoname",m_strShareFilePath ).commit();
					}

					Bundle bundle = new Bundle();
					bundle.putString("path", m_strShareFilePath);
					takePictureIntent.putExtra("data", bundle);
				}
				takePictureIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(takePictureIntent, TAKE_SHARE_PHOTO);
			} catch (Exception e) {
				FileLog.e("emm", e);
			}

			// Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			// startActivityForResult(intent, TAKE_SHARE_PHOTO);

		} else if (nid == UZResourcesIDFinder.getResIdID("btn_select_form_folder")) {
			pop_share_doc.dismiss();
			try {
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				Log.e("emm",
						"start activity phopo***********************************************");
				photoPickerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(photoPickerIntent, GET_SHARE_FILE);
			} catch (Exception e) {
				FileLog.e("emm", e);
			}

		} else if (nid == UZResourcesIDFinder.getResIdID("btn_share_doc")) {
			pop_share_doc.dismiss();
			// DocumentSelectActivity selectActivity = new
			// DocumentSelectActivity();
			// selectActivity.delegate = this;
			// m_FragmentContainer.PushFragment(selectActivity);

			docFragment = new DocFragment();
			Bundle args = new Bundle();
			docFragment.delegate = this;
			String mid = WeiyiMeetingClient.getInstance().getM_strMeetingID();
			String name = WeiyiMeetingClient.getInstance().getM_strUserName();
			args.putString("m_strMeetingID", mid);
			args.putString("m_strUserName", name);
			docFragment.setArguments(args);
			m_FragmentContainer.PushFragment(docFragment);
			HideLayouts();
			ShowTabpoints(true);
			if (getActivity() != null)
				((FaceMeeting_Activity) getActivity())
				.disableProximitySensor(false);
		} else if (nid == UZResourcesIDFinder.getResIdID("txt_invite")) {
			// showExitDialog();
			String mid = WeiyiMeetingClient.getInstance().getM_strMeetingID();
			String name = WeiyiMeetingClient.getInstance().getM_strUserName();
			String pwd = WeiyiMeetingClient.getInstance().getM_pwd();

			String strShare = WeiyiMeetingClient.getInstance().getM_inviteAddress();
			if (strShare.isEmpty())
				strShare = WeiyiMeetingClient.getInstance().getInviteAddress(mid,
						pwd);

			String strFinal = String.format(
					this.getString(UZResourcesIDFinder.getResStringID("share_string")), strShare.toString());

			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, getString(UZResourcesIDFinder.getResStringID("invite")));
			intent.putExtra(Intent.EXTRA_TEXT, strFinal);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(Intent.createChooser(intent, ""));
			pop_menu.dismiss();
		} else if (nid == UZResourcesIDFinder.getResIdID("txt_requestchairman")) {

			if (WeiyiMeetingClient.getInstance().getChairManID() == Session
					.getInstance().getUserMgr().getSelfUser().getPeerID()) {

				chairmainFragment = new ChairmainFragment();
				m_FragmentContainer.PushFragment(chairmainFragment);
				HideLayouts();
				ShowTabpoints(true);
				// m_vpMeeting.setVisibility(View.GONE);
				if (getActivity() != null)
					((FaceMeeting_Activity) getActivity())
					.disableProximitySensor(false);
			} else {
				if (WeiyiMeetingClient.getInstance().isM_instMeeting()) {
					String mid = WeiyiMeetingClient.getInstance()
							.getM_strMeetingID();
					String name = WeiyiMeetingClient.getInstance()
							.getM_strUserName();
					String pwd = WeiyiMeetingClient.getInstance().getM_pwd();
					WeiyiMeetingClient.getInstance().requestChairman(mid, pwd);
				} else {
					showRequestChairmanDialog(UZResourcesIDFinder.getResStringID("enter_chairman_pwd"));

				}
			}
			pop_menu.dismiss();
		} else if (nid == UZResourcesIDFinder.getResIdID("img_exitmeeting")) {
			showExitDialog();
		} else if (nid == UZResourcesIDFinder.getResIdID("rel_lis_chat")) {
			chatTo(0);
		} else if (nid == UZResourcesIDFinder.getResIdID("txt_exit")) {
			showExitDialog();
		} else if (nid == UZResourcesIDFinder.getResIdID("txt_applyspeaker_tv")) {
			if (Session.getInstance().getUserMgr().getSelfUser()
					.getHostStatus() == WeiyiMeetingClient.RequestHost_Allow) {
				WeiyiMeetingClient.getInstance().cancelHost(
						WeiyiMeetingClient.getInstance().getMyPID());
			} else {
				WeiyiMeetingClient.getInstance().requestHost(
						WeiyiMeetingClient.getInstance().getMyPID());
			}
			pop_menu.dismiss();
		}else if(nid == UZResourcesIDFinder.getResIdID("txt_details")){
			detailsFragment = new MeetingDetailsFragment();
			m_FragmentContainer.PushFragment(detailsFragment);
			HideLayouts();
			ShowTabpoints(true);
			// m_vpMeeting.setVisibility(View.GONE);
			if (getActivity() != null)
				((FaceMeeting_Activity) getActivity())
				.disableProximitySensor(false);	
			pop_menu.dismiss();
		}

	}






	public void chatTo(int mummeid) {
		// MeetingUser user = MeetingSession.getInstance().getUserMgr()
		// .getUser(mummeid);
		// user.setUnreadMsg(0);
		NotificationCenter.getInstance().postNotificationName(
				WeiyiMeetingClient.UI_NOTIFY_USER_UNREAD_MSG, 0);
		GroupChatFragment chatFragment = new GroupChatFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("toid", mummeid);
		// ((FaceMeetingActivity)getActivity()).isShowChat = true;
		// meetingActivity.isShowMember = false;
		chatFragment.setArguments(bundle);
		m_FragmentContainer.PushFragment(chatFragment);
	}

	public void showRequestChairmanDialog(int nTipID) {
		if (getActivity() == null)
			return;

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater layoutInflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = layoutInflater.inflate(UZResourcesIDFinder.getResLayoutID("meeting_password"),
				null);

		// builder.setView(view);
		// builder.setTitle(nTipID);

		final EditText etpsd = (EditText) view.findViewById(UZResourcesIDFinder.getResIdID("et_psd"));
		etpsd.setHint(getString(UZResourcesIDFinder.getResStringID("chairman_pwd")));
		etpsd.requestFocus();

		builder.setPositiveButton(getString(UZResourcesIDFinder.getResStringID("OK")),
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
				String mid = WeiyiMeetingClient.getInstance()
						.getM_strMeetingID();
				String name = WeiyiMeetingClient.getInstance()
						.getM_strUserName();
				String pwd = WeiyiMeetingClient.getInstance().getM_pwd();
				String strPassword = etpsd.getText().toString();
				WeiyiMeetingClient.getInstance().requestChairman(mid,
						strPassword);
			}

		});
		builder.setNegativeButton(getString(UZResourcesIDFinder.getResStringID("cancel")),
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
			}
		});
		AlertDialog adlg = builder.create();
		adlg.setView(view, 0, 0, 0, 0);
		adlg.setTitle(nTipID);
		adlg.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface arg0) {
				Utitlties.showKeyboard(etpsd);
				// InputMethodManager imm = (InputMethodManager)
				// getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				// imm.showSoftInput(etpsd, InputMethodManager.SHOW_IMPLICIT);
			}
		});
		adlg.show();
		adlg.setCanceledOnTouchOutside(false);
	}

	public void errorTipDialog(int errorTipID) {
		if (getActivity() == null)
			return;
		AlertDialog.Builder build = new AlertDialog.Builder(this.getActivity());
		build.setTitle(getString(UZResourcesIDFinder.getResStringID("remind")));
		build.setMessage(getString(errorTipID));
		build.setPositiveButton(getString(UZResourcesIDFinder.getResStringID("OK")),
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
			}

		});
		build.show();
	}

	public void showExitDialog() {
		if (getActivity() == null)
			return;
		if(WeiyiMeetingClient.getInstance().isNeedShowExitDialog()){
			AlertDialog.Builder builder = new Builder(getActivity());
			builder.setTitle(UZResourcesIDFinder.getResStringID("remind"));
			builder.setMessage(UZResourcesIDFinder.getResStringID("logouts"));
			builder.setPositiveButton(UZResourcesIDFinder.getResStringID("sure"),
					new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.e("emm", "endmeeting");

					((FaceMeeting_Activity) getActivity()).Endmeeting();
					

					if (m_wl != null) {
						m_wl.release();
						m_wl = null;
					}
					//yy ��������
					getActivity().finish();
				}
			}).setNegativeButton(UZResourcesIDFinder.getResStringID("cancel"),
					new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			AlertDialog dialog = builder.create();
			dialog.setCanceledOnTouchOutside(true);
			dialog.show();
			this.HideLayouts();
			ShowTabpoints(true);
		}else{
			((FaceMeeting_Activity) getActivity()).Endmeeting();

			if (m_wl != null) {
				m_wl.release();
				m_wl = null;
			}
			this.HideLayouts();
			ShowTabpoints(true);
		}
		
	}

	public void ShowMembersActivity() {
		memberFragment = new MeetingMemberFragment();
		m_FragmentContainer.PushFragment(memberFragment);
		HideLayouts();
		ShowTabpoints(true);
		// m_vpMeeting.setVisibility(View.GONE);
		if (getActivity() != null)
			((FaceMeeting_Activity) getActivity())
			.disableProximitySensor(false);
		return;

	}

	public void HideLayouts() {
		// if (!m_blayoutsShow)
		// return;
		if (m_PadMainFragment != null)
			m_PadMainFragment.hideArrLayout();
		else if (m_fragment_share != null)
			m_fragment_share.hideArrLayout();

		m_blayoutsShow = false;
		if (m_PadMainFragment != null) {
			m_PadMainFragment.setFullButtonVisable(m_blayoutsShow);
		}
		if (m_lTop_Title == null)
			return;
		float ftop = m_lTop_Title.getHeight();
		float fbottom = m_lBttom_btns.getHeight();

		/*AnimationSet animationsetTop = new AnimationSet(true);
		AnimationSet animationsetBottom = new AnimationSet(true);

		Animation translateAnimationtop = new TranslateAnimation(0.0f, 0.0f,
				0.0f, -ftop);
		Animation translateAnimationbottom = new TranslateAnimation(0.0f, 0.0f,
				0.0f, fbottom);
		Animation AlphaAnimation = new AlphaAnimation(1.0f, 0.0f);

		animationsetBottom
		.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationEnd(Animation arg0) {
				// TODO Auto-generated method stub
				m_banimationing = false;
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {

			}

			@Override
			public void onAnimationStart(Animation arg0) {
				m_banimationing = true;
			}

		});

		animationsetTop.setDuration(350);
		animationsetBottom.setDuration(350);
		// translateAnimationcamera.setDuration(200);

		animationsetTop.addAnimation(translateAnimationtop);
		animationsetTop.addAnimation(AlphaAnimation);
		animationsetBottom.addAnimation(translateAnimationbottom);
		animationsetBottom.addAnimation(AlphaAnimation);*/
		m_lTop_Title.setVisibility(View.INVISIBLE);
		m_lBttom_btns.setVisibility(View.INVISIBLE);
		ShowTabpoints(true);
		if (timerhide != null)
			timerhide.cancel();

		Log.e("emm", "HideLayouts");
	}
	@Override
	public void onStart() {
		//		m_FragmentContainer.setFragment(this);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_SUCCESS);
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
				WeiyiMeetingClient.GETMEETING_DOC);

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
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_PLAY_MOVIE);

		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.SHOWPAGE);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.NET_CONNECT_WARNING);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.VIDEO_NOTIFY_CAMERA_DID_OPEN);
		super.onStart();
		connectServer(false);
		

	}

	public void initSensers() {
		if (getActivity() == null)
			return;
		PowerManager pm = (PowerManager) getActivity().getSystemService(
				Context.POWER_SERVICE);
		m_wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyTag");
		m_wl.acquire();
	}

	public void initPopWindow(LayoutInflater inflater) {
		if (getActivity() == null)
			return;

		View view3 = inflater.inflate(UZResourcesIDFinder.getResLayoutID("popup_crema"), null);
		view3.measure(View.MeasureSpec.UNSPECIFIED,
				View.MeasureSpec.UNSPECIFIED);
		// popupWindow.showAsDropDown(view, 5, 5);
		pop_video = new PopupWindow(view3, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, false);
		pop_video.setBackgroundDrawable(new BitmapDrawable()  );
		pop_video.setOutsideTouchable(true);
		pop_video.setFocusable(true);

		pop_video.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				timerhide = new Timer();
				timerhide.schedule(new whenHide(), 5000);

			}
		});
		btn_close_cre = (TextView) view3.findViewById(UZResourcesIDFinder.getResIdID("btn_close_cre"));
		btn_switch_zhiliang = (TextView) view3
				.findViewById(UZResourcesIDFinder.getResIdID("btn_switch_quality"));

		// btn_switch_cre.setOnClickListener(this);
		btn_close_cre.setOnClickListener(this);
		btn_switch_zhiliang.setOnClickListener(this);


		View view4 = inflater.inflate(UZResourcesIDFinder.getResLayoutID("popup_share_pic"), null);
		view4.measure(View.MeasureSpec.UNSPECIFIED,
				View.MeasureSpec.UNSPECIFIED);
		pop_share_doc = new PopupWindow(view4, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, false);
		pop_share_doc.setBackgroundDrawable(new BitmapDrawable());
		pop_share_doc.setOutsideTouchable(true);
		pop_share_doc.setFocusable(true);
		pop_share_doc.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				timerhide = new Timer();
				timerhide.schedule(new whenHide(), 3000);

			}
		});

		TextView bt_take_photo = (TextView) view4
				.findViewById(UZResourcesIDFinder.getResIdID("btn_take_photo"));
		TextView bt_from_folder = (TextView) view4
				.findViewById(UZResourcesIDFinder.getResIdID("btn_select_form_folder"));
		TextView btn_share_doc = (TextView) view4
				.findViewById(UZResourcesIDFinder.getResIdID("btn_share_doc"));
		bt_take_photo.setOnClickListener(this);
		bt_from_folder.setOnClickListener(this);
		btn_share_doc.setOnClickListener(this);


	}

	class whenHide extends TimerTask {

		@Override
		public void run() {
			Utitlties.RunOnUIThread(new Runnable() {
				@Override
				public void run() {
					if (m_blayoutsShow)
						HideLayouts();
					ShowTabpoints(true);
				}
			});
		}

	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		if (m_wl != null) {
			m_wl.release();
			m_wl = null;
		}
		cleanNogifi();
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.NET_CONNECT_SUCCESS);
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_SHOW_SCREEN_PLAY);
		NotificationCenter.getInstance().removeObserver(this,
				SharePadMgr.SHAREPAD_STATECHANGE);

		// NotificationCenter.getInstance().removeObserver(this,
		// MeetingSession.NET_CONNECT_BREAK);
		// NotificationCenter.getInstance().removeObserver(this,
		// MeetingSession.NET_CONNECT_FAILED);

		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.GETMEETING_DOC);

		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_START_WEBSHRAE);
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_STOP_WEBSHRAE);
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_SHOW_WEBPAGE);

		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_SHOW_SCREEN_PLAY);
		NotificationCenter.getInstance().removeObserver(this,
				SharePadMgr.SHAREPAD_STATECHANGE);

		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_AUDIO_CHANGE);

		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_SYNC_VIDEO_MODE_CHANGE);

		NotificationCenter.getInstance().removeObserver(this, WeiyiMeetingClient.NET_CONNECT_WARNING);
		NotificationCenter.getInstance().removeObserver(this, WeiyiMeetingClient.VIDEO_NOTIFY_CAMERA_DID_OPEN);
		super.onStop();
	}

	private void ShowLayouts() {
		m_blayoutsShow = true;
		if (m_PadMainFragment != null) {
			m_PadMainFragment.setFullButtonVisable(m_blayoutsShow);
		}

		float ftop = m_lTop_Title.getHeight();
		float fbottom = m_lBttom_btns.getHeight();

		if(!WeiyiMeetingClient.getInstance().isLiveMeeting()){			
			if (muself != null && muself.getRole() != 2) {

				m_lBttom_btns.setVisibility(View.VISIBLE);
			} else {
				m_lBttom_btns.setVisibility(View.INVISIBLE);
			}
		}else{
			m_lBttom_btns.setVisibility(View.INVISIBLE);
		}

		m_lTop_Title.setVisibility(View.VISIBLE);

		ShowTabpoints(false);
		if (timerhide != null) {
			timerhide.cancel();
			timerhide = null;
		}
		timerhide = new Timer();
		timerhide.schedule(new whenHide(), 5000);

		Log.e("emm", "ShowLayouts");
	}

	/*
	 * public void onConnecting() { if(muself.getRole()!=2){
	 * m_ly_connecting.setVisibility(View.VISIBLE); HideLayouts(); } }
	 */

	public void OnConnectted() {
		Log.e("emm", "connect mediaserver success***********");
		ShowLayouts();
		bConnected = true;
		m_ly_connecting.setVisibility(View.INVISIBLE);

		WeiyiMeetingClient.getInstance().setCameraQuality(highquality);

		WeiyiMeetingClient.getInstance().setLoudSpeaker(true);

		if (!alreadyIn) {
			m_lBttom_btns.post(new Runnable() {

				@Override
				public void run() {
					int xoffset = m_vAudio.getLeft() + m_vAudio.getWidth() / 2
							- (m_lBttom_btns.getWidth() / 2);
					if (getActivity() != null && !getActivity().isFinishing()
							&& muself != null && muself.getRole() != 2) {

						changeAudioImage();
						if (pop_video != null && pop_video.isShowing()) {
							timerhide.cancel();
						}
					}
					if (Session.getInstance().getUserMgr().getCount() == 0) {
						if (getActivity() != null) {
							Toast.makeText(getActivity(),
									UZResourcesIDFinder.getResStringID("invite_people"), Toast.LENGTH_SHORT)
									.show();

						}
					}
				}
			});
			alreadyIn = true;
		}

		if (WeiyiMeetingClient.getInstance().getM_nScreenSharePeerID() == 0)
			stopShareScreen();
		else
			startShareScreen();

		if (m_PadMainFragment != null)
			m_PadMainFragment.showLayout();
		else if (m_fragmentCamera != null) {
			m_fragmentCamera.showLayout();
		}

		//for exception for huawei honor
		if(getActivity()!=null){
			spphoto = getActivity().getSharedPreferences("spphoto", 0);
			String filename = spphoto.getString("photoname", "");
			if(filename != null && !filename.isEmpty())
			{
				uploadFile(filename);
				spphoto.edit().putString("photoname", "").commit();

			}
		}	
	}

	public void onPageClick(boolean isWhiteBoard) {
		if (m_banimationing || m_ly_connecting.getVisibility() == View.VISIBLE)
			return;
		if (m_blayoutsShow) {
			HideLayouts();
			ShowTabpoints(true);
		} else {
			if (isWhiteBoard) {
				if (m_PadMainFragment != null) {
					m_PadMainFragment.showArrLayout();
				} else if (m_fragment_share != null)
					m_fragment_share.showArrLayout();
			}
			ShowLayouts();
			reStartSchedule();
		}
	}

	public void reStartSchedule() {
		// timer.cancel();
		// timer= new Timer();
		// timer.schedule(task, 5000);
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			UnreadMSG1();
		};
	};

	@SuppressLint("HandlerLeak")
	public void UnreadMSG1() {
		int count = 0;
		for (int i = 0; i < Session.getInstance().getUserMgr()
				.getCount(); i++) {
			MeetingUser user = Session.getInstance().getUserMgr()
					.getUserByIndex(i);
			if (user != null)
				count += user.getUnreadMsg();
		}
		TextView unread_tv = (TextView) fragmentView
				.findViewById(UZResourcesIDFinder.getResIdID("unreadtext"));
		if (count == 0) {
			unread_tv.setVisibility(View.GONE);
		} else {
			unread_tv.setVisibility(View.VISIBLE);
			if (count > 99) {
				unread_tv.setText(99 + "+");
			} else {
				unread_tv.setText(count + "");
			}
		}
	}

	public void clean() {
		cleanNogifi();
		if (mAdapter != null)
			mAdapter.removeall();
		// m_bDisconcert = true;
	}

	public void cleanNogifi() {
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_SHOW_SCREEN_PLAY);
		NotificationCenter.getInstance().removeObserver(this,
				SharePadMgr.SHAREPAD_STATECHANGE);
		// NotificationCenter.getInstance().removeObserver(this,
		// MeetingSession.NET_CONNECT_BREAK);
		// NotificationCenter.getInstance().removeObserver(this,
		// MeetingSession.NET_CONNECT_FAILED);
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_AUDIO_CHANGE);
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_USER_UNREAD_MSG);
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_CHAT_RECEIVE_TEXT);
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.REQUEST_CHAIRMAN);
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.UI_NOTIFY_PLAY_MOVIE);
		NotificationCenter.getInstance().removeObserver(this, WeiyiMeetingClient.SHOWPAGE);
		//xiaoyang add ɾ���ĵ�ҳ����Ϣ
	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) 
	{
		Log.e("emm", "test1");
		if (reqCode == TAKE_SHARE_PHOTO) {
			Log.e("emm", "resumeLocalVideo begin");
			Session.getInstance().resumeLocalVideo();
			Log.e("emm", "resumeLocalVideo end");
		}
		if ((reqCode == TAKE_SHARE_PHOTO || reqCode == GET_SHARE_FILE)) {

			if (resultCode != Activity.RESULT_OK)
			{
				Log.e("emm", "test2");
				return;
			}
			String strFile = "";
			if (reqCode == TAKE_SHARE_PHOTO) {
				strFile = m_strShareFilePath;							
			} else {
				Uri uri = data.getData();
				strFile = Utitlties.getPath(uri);
			}
			if(strFile != null && !strFile.isEmpty())
			{	
				uploadFile(strFile);
			}
			// startShareFile(strFile);
		} else if (reqCode == MEETING_MEMBER) {
			if (resultCode == 10) {
				m_vpMeeting.setCurrentItem(0, true);
			}
		}
	}

	private TextView tv_info;
	private ProgressBar pb_upload;
	private Button bt_cancel;
	private AlertDialog ad;

	public void uploadFile(final String strPath) {
		// fileformattip
		final String path = scaleAndSaveImage(strPath, 800, 800, 87, false);
		if (path == null) {
			Toast.makeText(
					m_FragmentContainer.m_ActParent,
					m_FragmentContainer.m_ActParent
					.getString(UZResourcesIDFinder.getResStringID("uploading_file_failed")),
					Toast.LENGTH_SHORT).show();
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(
				m_FragmentContainer.m_ActParent);
		LayoutInflater layoutInflater = (LayoutInflater) m_FragmentContainer.m_ActParent
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(UZResourcesIDFinder.getResLayoutID("upload_file_progress"), null);
		builder.setView(view);
		builder.setCancelable(false);

		tv_info = (TextView) view.findViewById(UZResourcesIDFinder.getResIdID("tv_info"));
		pb_upload = (ProgressBar) view.findViewById(UZResourcesIDFinder.getResIdID("pb_upload"));
		bt_cancel = (Button) view.findViewById(UZResourcesIDFinder.getResIdID("bt_cancel_upload"));
		pb_upload.setMax(100);
		pb_upload.setProgress(0);
		String string = String.format(m_FragmentContainer.m_ActParent
				.getString(UZResourcesIDFinder.getResStringID("uploading_file")), "0");
		tv_info.setText(string);
		ad = builder.show();
		ad.show();
		ad.setCanceledOnTouchOutside(false);

		// FormBodyPart[] parts = new FormBodyPart[10];
		String strUrl;
		if (SharePadMgr.getInstance().getWebImageDomain().startsWith("http://"))
			strUrl = SharePadMgr.getInstance().getWebImageDomain() + "/ClientAPI/"
					+ SharePadMgr.UPLOAD_IMAGE_INTERFACE;
		else
			strUrl = "http://" + SharePadMgr.getInstance().getWebImageDomain()
			+ "/ClientAPI/" + SharePadMgr.UPLOAD_IMAGE_INTERFACE;
		Log.e("emm", "strurl=" + strUrl);
		//		UploadFile uf = new UploadFile();
		//		uf.delegate = this;
		//		uf.UploadOperation(strUrl, Utitlties.getApplicationContext());
		//		uf.packageFile(path);
		//		uf.start();
		SharePadMgr.getInstance().upLoadMeetingFile(strUrl, path,
				WeiyiMeetingClient.getInstance().getM_strMeetingID(),
				Session.getInstance().getUserMgr().getSelfUser().getPeerID(),
				WeiyiMeetingClient.getInstance().getM_strUserName());
	}


	public String scaleAndSaveImage(String strPath, float maxWidth,
			float maxHeight, int quality, boolean cache) {
		String picLastName = strPath.substring(strPath.lastIndexOf(".") + 1)
				.toLowerCase();
		if (picLastName.equals("bmp") || picLastName.equals("jpeg")
				|| picLastName.equals("png") || picLastName.equals("jpg")) {
			Bitmap bitmap = null;
			try {
				InputStream in = new FileInputStream(strPath);
				int size = in.available();
				BitmapFactory.Options opts = new BitmapFactory.Options();
				if (size > 1024 * 1024)
					opts.inSampleSize = 2;
				try {
					bitmap = BitmapFactory.decodeStream(in, null, opts);
				} catch (OutOfMemoryError e) {
					//
					return null;
				}

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

			if (bitmap == null) {
				return null;
			}
			float photoW = bitmap.getWidth();
			float photoH = bitmap.getHeight();
			if (photoW == 0 || photoH == 0) {
				return null;
			}
			float scaleFactor = Math.max(photoW / maxWidth, photoH / maxHeight);
			Bitmap scaledBitmap = null;
			if(scaleFactor<=1){
				scaledBitmap = bitmap;
			}else{				
				scaledBitmap = Bitmap.createScaledBitmap(bitmap,
						(int) (photoW / scaleFactor), (int) (photoH / scaleFactor),
						true);
				bitmap.recycle();
			}


			String picUrl = strPath.substring(0, strPath.lastIndexOf("/"));
			String picName = "/_1_2"
					+ strPath.substring(strPath.lastIndexOf("/") + 1);

			File f = null;
			try {
				f = new File(Utitlties.getCacheDir(getActivity()
						.getApplicationContext()), picName);

			} catch (Exception e) {
				f = new File(picUrl, picName);
			}

			if (f.exists()) {
				f.delete();
			}
			try {
				FileOutputStream out1 = new FileOutputStream(f);
				if (picLastName.equalsIgnoreCase("jpg")
						|| picLastName.equalsIgnoreCase("jpeg")) {
					scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out1);
				} else if (picLastName.equalsIgnoreCase("png")) {
					scaledBitmap.compress(Bitmap.CompressFormat.PNG, 90, out1);
				}
				out1.flush();
				out1.close();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				return Utitlties.getCacheDir(getActivity()
						.getApplicationContext()) + picName;
			} catch (Exception e) {
				return picUrl + picName;
			}
		} else if (picLastName.equals("doc") || picLastName.equals("docx")
				|| picLastName.equals("xls") || picLastName.equals("xlsx")
				|| picLastName.equals("xlt") || picLastName.equals("xlsm")
				|| picLastName.equals("ppt") || picLastName.equals("pptx")
				|| picLastName.equals("pps") || picLastName.equals("pos")
				|| picLastName.equals("pdf") || picLastName.equals("txt")) {
			return strPath;
		} else {
			return null;
		}

	}


	private void saveHistory(String ID) {

		SharedPreferences sp = WeiyiMeetingClient.getApplicationContext()
				.getSharedPreferences("meeting_id", 0);

		String longhistory = sp.getString("history", "");

		String newString = longhistory;
		if (longhistory.contains(ID + ",")) {
			newString = longhistory.replace(ID + ",", "");
		}
		StringBuilder sb = new StringBuilder(newString);
		sb.insert(0, ID + ",");
		sp.edit().putString("history", sb.toString()).commit();

	}

	@Override
	public void OnPenClick(boolean bshow) {
		// TODO Auto-generated method stub
		if (!bshow) {
			this.HideLayouts();
			ShowTabpoints(true);
		}
		this.ShowTabpoints(bshow);
	}
	@Override
	public boolean onBackPressed() {
		// TODO Auto-generated method stub
		showExitDialog();
		return true;
	}

	@Override
	public void UploadingFileFinish(int nFileID, int pagenum, String filename,
			String swfpath) {

		Session.getInstance().resumeLocalVideo();
//		Session.getInstance().sendDocChange(nFileID,
//				false, filename, swfpath, 1);
		if (WeiyiMeetingClient.RequestHost_Allow == Session
				.getInstance().getUserMgr().getSelfUser()
				.getHostStatus()
				|| WeiyiMeetingClient.getInstance().getMyPID() == WeiyiMeetingClient
				.getInstance().getChairManID()) {// 同锟斤拷
			Session.getInstance().changeDoc(nFileID, 1);
			Session.getInstance().sendShowPage(nFileID,
					1);
		} else {
			Session.getInstance().changeDoc(nFileID, 1);
		}
		if (m_vpMeeting != null)
			m_vpMeeting.setCurrentItem(0);

		if(getActivity()!=null){
			spphoto = getActivity().getSharedPreferences("spphoto", 0);
			String filename1 = spphoto.getString("photoname", "");
			if(filename1 != null && !filename1.isEmpty())
			{
				spphoto.edit().putString("photoname", "").commit();
			}
		}	

	}

	@Override
	public void UploadingFileFailed(int operationcount) {

		Session.getInstance().resumeLocalVideo();
		if(operationcount>3)
		{
			Toast.makeText(
					m_FragmentContainer.m_ActParent,
					m_FragmentContainer.m_ActParent
					.getString(UZResourcesIDFinder.getResStringID("uploading_file_failed")),
					Toast.LENGTH_SHORT).show();
			return;
		}

	}

	@Override
	public void ChangedUploadProgress(int pro) {
		pb_upload.setProgress(pro);
		// Log.i("emm", "progress = " + i);
		String string = String.format(m_FragmentContainer.m_ActParent
				.getString(UZResourcesIDFinder.getResStringID("uploading_file")), "" + pro);
		tv_info.setText(string);
		if (pro >= 100) {
			ad.dismiss();
			bt_cancel.setEnabled(false);
		}

	}


	@Override
	public void DelmeetingFile(int id, int pageid, String filename,
			String fileurl) {
		// TODO Auto-generated method stub

	}
	
	 private int[] calculatePopWindowPos(final View anchorView, final View contentView) {
	        final int windowPos[] = new int[2];
	        final int anchorLoc[] = new int[2];
	        // ��ȡê��View����Ļ�ϵ����Ͻ�����λ��
	        anchorView.getLocationOnScreen(anchorLoc);
	        final int anchorHeight = anchorView.getHeight();
	        // ��ȡ��Ļ�ĸ߿�
	        final int screenHeight = getActivity().getWindowManager().getDefaultDisplay().getWidth();
	        final int screenWidth = getActivity().getWindowManager().getDefaultDisplay().getHeight();
	        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
	        // ����contentView�ĸ߿�
	        final int windowHeight = contentView.getMeasuredHeight();
	        final int windowWidth = contentView.getMeasuredWidth();
	        
            windowPos[0] = screenWidth - (windowWidth-windowWidth/5*2);
            windowPos[1] = anchorLoc[1] + anchorHeight;
	       
	        return windowPos;
	    }
}





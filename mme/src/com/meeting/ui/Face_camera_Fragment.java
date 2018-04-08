package com.meeting.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.broadcast.BroadCastViewPagerFragment;
import com.utils.BaseFragment;
import com.utils.BaseFragmentContainer;
import com.utils.Utitlties;
import com.utils.WeiyiMeetingClient;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.weiyicloud.whitepad.NotificationCenter;
import com.weiyicloud.whitepad.NotificationCenter.NotificationCenterDelegate;

import java.util.ArrayList;
import java.util.List;

import info.emm.meeting.MeetingUser;
import info.emm.meeting.MyWatch;
import info.emm.meeting.Session;

//import info.emm.messenger.MQLib;

@SuppressLint("ValidFragment")
public class Face_camera_Fragment extends BaseFragment implements NotificationCenterDelegate {
// LayoutInflater m_inflater;

	private info.emm.sdk.VideoView m_vCamearSelf;
	// xiaoyang add
	private VideoView m_vCamearSelf_bst;
	// xiaoyang add
	private LinearLayout layout;

	// private TextView m_tvSwitchCamera;
	private TextView m_tvMainCameraName;
	private TextView txt_no_video;
	private ImageView img_no_video;
	MeetingUser muself = null;

	// private boolean m_bIsbigCameraShowSelf = true;

	boolean m_blayoutsShow = false;
	boolean m_banimationing = false;
	boolean m_bLandScape = true;

	boolean _isFull = false;
	ArrayList<POSITION> _videoPositions = new ArrayList<POSITION>();

	private ImageView img_switch_cream;
	private ImageView img_switch_audio;

	private TextView tv_switch_audio;
	private SharedPreferences sp = null;

	// boolean m_bAutoVideoMode = true;

	private OnClickListener m_PageClickListener;

	public Face_camera_Fragment(OnClickListener ocl, BaseFragmentContainer con) {
		m_PageClickListener = ocl;
		m_FragmentContainer = con;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@SuppressWarnings("unused")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		muself = Session.getInstance().getUserMgr().getSelfUser();
		UZResourcesIDFinder.init(getActivity().getApplicationContext());
		if (fragmentView == null) {
			Log.e("emm", "face_camera_fragment oncreateview fragmentView is null");
			// m_inflater = inflater;
			fragmentView = inflater.inflate(UZResourcesIDFinder.getResLayoutID("face_camera_framgent"), null);
			m_vCamearSelf = (info.emm.sdk.VideoView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("camera_view_main"));
			// xiaoyang add
			m_vCamearSelf_bst = (VideoView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("camera_view_main_broadcast"));
			// xiaoyang add
			txt_no_video = (TextView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("txt_pic_video"));

			img_no_video = (ImageView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("img_no_video"));
			img_switch_cream = (ImageView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("img_switch_cream"));
			img_switch_cream.setAlpha(1f);
			img_switch_audio = (ImageView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("img_switch_audio"));
			img_switch_audio.setAlpha(1f);

			tv_switch_audio = (TextView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("switch_audio_tv"));

			tv_switch_audio.setText(getString(UZResourcesIDFinder.getResStringID("audio_up_remind")));
			img_switch_audio.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					boolean loud = WeiyiMeetingClient.getInstance().getLoudSpeaker();
					WeiyiMeetingClient.getInstance().setLoudSpeaker(!loud);
					if (!loud) {
						img_switch_audio.setImageResource(UZResourcesIDFinder.getResDrawableID("voice_out"));
						tv_switch_audio.setText(getString(UZResourcesIDFinder.getResStringID("audio_up_remind")));
					} else {
						img_switch_audio.setImageResource(UZResourcesIDFinder.getResDrawableID("voice_in"));
						tv_switch_audio.setText(getString(UZResourcesIDFinder.getResStringID("audio_down_remind")));
					}
				}
			});

			// if(MeetingSession.getInstance().hasMoreCamera()){
			// img_switch_cream.setVisibility(View.VISIBLE);
			// }else{
			// img_switch_cream.setVisibility(View.GONE);
			// }
			img_switch_cream.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					WeiyiMeetingClient.getInstance().switchCamera();
					SharedPreferences sp = getActivity().getSharedPreferences("state", 0);
					SharedPreferences.Editor editor = sp.edit();
					if (WeiyiMeetingClient.getInstance().isLiveMeeting()) {
						editor.putBoolean("m_bIsfrontCamera", BroadCastViewPagerFragment.m_bIsfrontCamera);
					} else {
						editor.putBoolean("m_bIsfrontCamera", ViewPagerFragment.m_bIsfrontCamera);

					}
					editor.commit(); // ViewPagerFragmentNew.m_bIsfrontCamera = !kkkk.m_bIsfrontCamera;
				}
			});

			txt_no_video.clearAnimation();
			img_no_video.clearAnimation();
			txt_no_video.setVisibility(View.INVISIBLE);
			img_no_video.setVisibility(View.INVISIBLE);
			if (Session.getInstance().isM_bInmeeting())
				showLayout();
			layout = (LinearLayout) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("ly_no_video"));
			layout.setOnClickListener(m_PageClickListener);

			m_tvMainCameraName = (TextView) fragmentView
					.findViewById(UZResourcesIDFinder.getResStringID("textView_big_camera_name"));
			m_vCamearSelf.setOnTouchListener(new OnTouchListener() {

				// @Override
				public boolean onTouch(View arg0, MotionEvent event) {
					int nAction = event.getAction();

					switch (nAction) {
						case MotionEvent.ACTION_DOWN: {

						}
						break;
						case MotionEvent.ACTION_UP: {
							if (event.getPointerCount() == 1) {

								if (m_vCamearSelf != null) {
									PointF ptA = new PointF(event.getX(0), event.getY(0));
									int nWidth = m_vCamearSelf.getWidth();
									int nHeight = m_vCamearSelf.getHeight();
									boolean found = false;

									for (int i = 1; i < _videoPositions.size(); ++i) {
										POSITION pos = _videoPositions.get(i);

										if (ptA.x > (nWidth * pos.left) && ptA.x < (nWidth * pos.right)
												&& ptA.y > (nHeight * pos.top) && ptA.y < (nHeight * pos.bottom)) {
											found = true;
											switchVideoPosition(i);
											break;
										}
									}

									if (!found) {
										if (m_PageClickListener != null)
											m_PageClickListener.onClick(null);
									}
								}
							}

						}
						break;
						case MotionEvent.ACTION_MOVE: {

						}
						break;
					}
					return true;
				}

			});

			showCameraName(true);
			// playbrocast();
		} else {
			Log.e("emm", "face_camera_fragment oncreateview fragmentView not is null");
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}

		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			m_bLandScape = false;
		} else {
			m_bLandScape = true;
		}

		if (Session.getInstance().isM_bInmeeting()) {
			Log.e("emm", "face camera already in meeting***************");
			if (WeiyiMeetingClient.getInstance().isLiveMeeting()) {
				if (!WeiyiMeetingClient.getInstance().isViewer()) {
					checkWatchSelf();
				} else {
					unWatchAll();
					// m_vCamearSelf.setVisibility(View.VISIBLE);
					// playbrocast();
				}
			} else {
				checkWatchSelf();
			}

			if (WeiyiMeetingClient.getInstance().isM_bAutoVideoMode() || Session.getInstance().isM_instMeeting()) {
				int count = Session.getInstance().getUserMgr().getCount();
				for (int i = 0; i < count; i++) {
					MeetingUser mu = Session.getInstance().getUserMgr().getUserFromIndex(i);
					if (mu != null && mu.ishasVideo()) {
						watchVideo(mu.getPeerID(), false, mu.getDefaultCameraIndex());
						watchVideo(mu.getPeerID(), true, mu.getDefaultCameraIndex());
					}
				}
			}

		}
		return fragmentView;
	}

	private void playbrocast(int status) {
		if (WeiyiMeetingClient.getInstance().isLiveMeeting()) {
			if (WeiyiMeetingClient.getInstance().isViewer()) {
				String mediaserver = WeiyiMeetingClient.getInstance().getLIVE_MEDIA_SERVER();
				;
				String mediaport = WeiyiMeetingClient.getInstance().getLIVE_MEDIA_PORT();
				String path = "rtmp://" + mediaserver + ":" + mediaport + "/live/"
						+ Session.getInstance().getMeetingId();
				if (Session.getInstance().getMeetingtype() == 12 || Session.getInstance().getMeetingtype() == 13) {
					if (status == 1) {
						m_vCamearSelf.setVisibility(View.VISIBLE);
						WeiyiMeetingClient.getInstance().playBroadCasting(path, m_vCamearSelf,
								WeiyiMeetingClient.getInstance().isM_scattype());
					} else if (status == 0) {
						m_vCamearSelf.setVisibility(View.INVISIBLE);
						txt_no_video.setText(getString(UZResourcesIDFinder.getResStringID("broadcast_over")));
						WeiyiMeetingClient.getInstance().unplayBroadCasting();
					}
				}
			}

		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		if (!Session.getInstance().isM_bInmeeting()) {
			this.unWatchAll();
		}
		Log.e("emm", "face_camera_fragment onDestroyView*****************");
	}

	@Override
	public void onStop() {
		NotificationCenter.getInstance().removeObserver(this);
		super.onStop();
	}

	@Override
	public void onStart() {
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.NET_CONNECT_ING);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.NET_CONNECT_SUCCESS);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.NET_CONNECT_FAILED);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.NET_CONNECT_BREAK);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.NET_CONNECT_ENABLE_PRESENCE);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.NET_CONNECT_USER_IN);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.NET_CONNECT_USER_OUT);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_USER_AUDIO_CHANGE);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_USER_WATCH_VIDEO);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_SELF_VEDIO_WISH_CHANGE);
		NotificationCenter.getInstance().addObserver(this, MeetingMemberFragment.ControlVideo);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.NET_CONNECT_USER_INLIST_COMPLETE);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_USER_VEDIO_CHANGE);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_USER_CHAIRMAN_CHANGE);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_USER_SYNC_WATCH_VIDEO);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_USER_SYNC_VIDEO_MODE_CHANGE);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.NET_CONNECT_FAILED);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.NET_CONNECT_BREAK);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.LIVECHANGE);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_USER_CAMERA_CHANGE);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.UI_NOTIFY_SWITCH_MAIN_VIDEO);
		NotificationCenter.getInstance().addObserver(this, WeiyiMeetingClient.START_BROADCAST);
		showCameraName(false);
		super.onStart();
	}

	@SuppressLint("NewApi")
	@SuppressWarnings("unused")
	@Override
	public void onResume() {
		super.onResume();

		// NotificationCenter.getInstance().addObserver(this,
		// MeetingSession.LIVE_SIGNAL_CONNECTED);

		if (Session.getInstance().isM_bInmeeting())
			doVideoLayout();
		Log.e("emm", "face_camera_fragment onresume*****************");
	}

	public void showLayout() {
		// showCameraName(BroadCastViewPagerFragment.isShow);
		if (txt_no_video == null)
			return;

		if (muself.getRole() == 2) {

			img_switch_cream.setVisibility(View.GONE);
		}

		if (muself.getRole() == 2 && WeiyiMeetingClient.getInstance().get_syncVideoList().size() == 0) {

			// txt_no_video.setVisibility(View.GONE);
			m_vCamearSelf.setVisibility(View.INVISIBLE);
			// txt_no_video.setVisibility(View.VISIBLE);
		} else {

			List<MyWatch> infoIds = new ArrayList<MyWatch>();
			infoIds.addAll(WeiyiMeetingClient.getInstance().getM_nWatchVideoIDs());
			if (infoIds.size() == 0) {
				m_vCamearSelf.setVisibility(View.INVISIBLE);

				img_no_video.setVisibility(View.VISIBLE);

				if (WeiyiMeetingClient.getInstance().get_syncVideoList().size() == 0
						&& WeiyiMeetingClient.getInstance().is_isAutoSyncVideo()) {
					// if(MeetingSession.getInstance().getChairManID() ==
					// MeetingSession.getInstance().getMyPID())
					//// txt_no_video.setVisibility(View.VISIBLE);
					// else
					// {
					//// txt_no_video.setVisibility(View.VISIBLE);
					// }
				} else {
					if (WeiyiMeetingClient.getInstance().isLiveMeeting()
							&& WeiyiMeetingClient.getInstance().isViewer()) {
					} else {
					}
					// txt_no_video.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	@Override
	public void didReceivedNotification(int id, Object... args) {
		switch (id) {
			case WeiyiMeetingClient.NET_CONNECT_SUCCESS: {

			}
			break;
			case WeiyiMeetingClient.UI_NOTIFY_USER_CHAIRMAN_CHANGE: {
				checkAutoVideoOut();
			}
			break;
			case WeiyiMeetingClient.NET_CONNECT_USER_INLIST_COMPLETE: {
				checkAutoVideoOut();
			}
			break;
			case MeetingMemberFragment.ControlVideo: {
				int nID = (Integer) args[0];
				boolean bWatch = (Boolean) args[1];
				int cameraid = (Integer) args[2];
				if (WeiyiMeetingClient.getInstance().isSyncVideo() && WeiyiMeetingClient.getInstance()
						.getMyPID() != WeiyiMeetingClient.getInstance().getChairManID()) {
					if (getActivity() != null) {
						Toast.makeText(getActivity(),
								getString(UZResourcesIDFinder.getResStringID("chairman_synchronous_video")),
								Toast.LENGTH_SHORT).show();
					}
				} else if (WeiyiMeetingClient.getInstance().isSyncVideo() && WeiyiMeetingClient.getInstance()
						.getMyPID() == WeiyiMeetingClient.getInstance().getChairManID()) {
					watchVideo(nID, bWatch, cameraid);
					// WeiyiMeetingClient.getInstance().syncUserVideo(nID, cameraid, bWatch);
				} else {
					watchVideo(nID, bWatch, cameraid);
				}

			}
			break;
			case WeiyiMeetingClient.UI_NOTIFY_USER_SYNC_VIDEO_MODE_CHANGE: {
				boolean mode = (Boolean) args[0];
				boolean auto = (Boolean) args[1];
				if (mode && auto)
					unWatchAll();
				else
					this.showLayout();
			}
			break;
			case WeiyiMeetingClient.NET_CONNECT_FAILED:
			case WeiyiMeetingClient.NET_CONNECT_BREAK: {
				unWatchAll();
				// qxm change
				txt_no_video.setText(getString(UZResourcesIDFinder.getResStringID("reconnect_server")));
				txt_no_video.setVisibility(View.GONE);
				img_no_video.setVisibility(View.GONE);
				break;
			}

			case WeiyiMeetingClient.UI_NOTIFY_USER_SYNC_WATCH_VIDEO: {
				int peerID = (Integer) args[0];
				int cameraid = (Integer) args[1];
				boolean open = (Boolean) args[2];
				watchVideo(peerID, open, cameraid);
			}
			break;

			case WeiyiMeetingClient.NET_CONNECT_ENABLE_PRESENCE: {

				// watchVideo(-1, false);
				Log.d("emn", "watchmyself video***************************");
				if (WeiyiMeetingClient.getInstance().isLiveMeeting()) {
					if (!WeiyiMeetingClient.getInstance().isViewer()) {
						checkWatchSelf();
					} else {
						unWatchAll();
						// m_vCamearSelf.setVisibility(View.VISIBLE);
						// playbrocast();
					}
				} else {
					if (WeiyiMeetingClient.getInstance().is_isSyncVideo()) {
						if (getActivity() != null) {
							Toast.makeText(getActivity(),
									getString(UZResourcesIDFinder.getResStringID("chairman_synchronous_video")),
									Toast.LENGTH_SHORT).show();
						}
					} else {
						checkWatchSelf();
					}
				}

			}
			break;
			case WeiyiMeetingClient.NET_CONNECT_USER_IN: {
				if (WeiyiMeetingClient.getInstance().isM_bAutoVideoMode()
						|| WeiyiMeetingClient.getInstance().isM_instMeeting()) {
					int peerid = (Integer) args[0];
					if (!WeiyiMeetingClient.getInstance().isSyncVideo()
							&& Session.getInstance().getUserMgr().getUser(peerid).ishasVideo()) {
						MeetingUser mu = Session.getInstance().getUserMgr().getUser(peerid);

						watchVideo(peerid, true, mu.getDefaultCameraIndex());
					}

				}
				boolean bInlist = (Boolean) args[1];
				if (!bInlist)
					checkAutoVideoOut();
			}
			break;
			case WeiyiMeetingClient.NET_CONNECT_USER_OUT: {
				int peerid = (Integer) args[0];
				boolean bWatchedleave = (Boolean) args[2];
				if (bWatchedleave) {
					doVideoLayout();// SESSION has automatically closed it. Just do
					// layout is ok.
				}

			}
			break;
			case WeiyiMeetingClient.UI_NOTIFY_USER_AUDIO_CHANGE: {
				checkAutoVideoOut();
			}
			break;
			case WeiyiMeetingClient.UI_NOTIFY_SELF_VEDIO_WISH_CHANGE: {
				// watchVideo(-1, false);
				checkWatchSelf();
			}
			break;
			case WeiyiMeetingClient.UI_NOTIFY_USER_WATCH_VIDEO: {
				int peerid = (Integer) args[0];
				boolean bWatch = (Boolean) args[1];
				int cameraid = (Integer) args[2];
				watchVideo(peerid, bWatch, cameraid);// peerid
				// WatchMainVideo(bWatch,peerid);
			}
			break;
			case WeiyiMeetingClient.UI_NOTIFY_USER_VEDIO_CHANGE: {
				int peerid = (Integer) args[0];
				boolean bWatched = (Boolean) args[1];
				MeetingUser mu = Session.getInstance().getUserMgr().getUser(peerid);
				if (mu != null && bWatched && !mu.ishasVideo()) {
					// MeetingSession.getInstance().m_bAutoVideoMode = true;
					doVideoLayout();// SESSION has automatically closed it. Just do
					// layout is ok.
				}

			}
			break;

			case WeiyiMeetingClient.LIVECHANGE:
				boolean isLive = (Boolean) args[0];
				MeetingUser mu = Session.getInstance().getUserMgr().getSelfUser();
				if (Session.getInstance().getMeetingtype() != 11) {
					watchVideo(0, isLive, mu.getDefaultCameraIndex());
				}

				break;
			case WeiyiMeetingClient.UI_NOTIFY_USER_CAMERA_CHANGE:
				Log.e("121231", "1");
				doVideoLayout();
				break;
			case WeiyiMeetingClient.UI_NOTIFY_SWITCH_MAIN_VIDEO:
				int peerid = (Integer) args[0];
				int videoid = (Integer) args[1];
				switchMainVideoPosition(peerid, videoid);
				break;
			case WeiyiMeetingClient.START_BROADCAST:
				int status = (Integer) args[0];
				playbrocast(status);
				break;
			// case MeetingSession.LIVE_SIGNAL_CONNECTED:
			// MQLib mqLib = (MQLib) args[0];
			// String topic = MeetingSession.getInstance().getMeetingId()+"/s" ;
			// String meetingName = MeetingSession.getInstance().getM_strMeetingName();
			// byte[] bytes = meetingName.getBytes();
			// int networkMsgID = mqLib.publish(
			// topic.toCharArray(), bytes.length, bytes,
			// 1, false);
			// if (networkMsgID > 0){
			// String mediaserver = MeetingSession.LIVE_MEDIA_SERVER;
			// String mediaport = MeetingSession.LIVE_MEDIA_PORT;
			// MeetingSession.getInstance().startBroadCasting("rtmp://"+mediaserver+":"+mediaport+"/live/"+MeetingSession.getInstance().getMeetingId());
			// }
			// break;
		}
	}

	public void showCameraName(boolean bShow) {
		if (m_tvMainCameraName != null)
			m_tvMainCameraName.setVisibility(bShow ? View.VISIBLE : View.GONE);
		if (img_switch_audio == null)
			return;
		if (!bShow) {
			if (WeiyiMeetingClient.getInstance().isLiveMeeting()) {
				if (WeiyiMeetingClient.getInstance().isViewer()) {
					img_switch_cream.setVisibility(View.GONE);
					img_switch_audio.setVisibility(View.VISIBLE);
				} else {
					if (WeiyiMeetingClient.getInstance().hasMoreCamera()
							&& Session.getInstance().getMeetingtype() != 11) {
						// 如果是观摩者进入会诊（2为观摩者），刚让摄像头切头图片消失
						if (muself.getRole() == 2) {
							img_switch_cream.setVisibility(View.GONE);
							WeiyiMeetingClient.getInstance().StopSpeaking();
						} else {
							img_switch_cream.setVisibility(View.VISIBLE);
						}
					} else {
						img_switch_cream.setVisibility(View.GONE);
					}
					img_switch_audio.setVisibility(View.GONE);
				}
			} else {
				if (WeiyiMeetingClient.getInstance().hasMoreCamera() && Session.getInstance().getMeetingtype() != 11) {
					// 如果是观摩者进入会诊（2为观摩者），刚让摄像头切头图片消失
					if (muself.getRole() == 2) {
						img_switch_cream.setVisibility(View.GONE);
						WeiyiMeetingClient.getInstance().StopSpeaking();
					} else {
						img_switch_cream.setVisibility(View.VISIBLE);
					}
				} else {
					img_switch_cream.setVisibility(View.GONE);
				}
				img_switch_audio.setVisibility(View.VISIBLE);
			}

			// img_photograph.setVisibility(View.GONE);
			// img_picture.setVisibility(View.GONE);
			// img_document.setVisibility(View.GONE);
			// tv_switch_audio.setVisibility(View.GONE);
		} else {
			img_switch_audio.setVisibility(View.GONE);
			img_switch_cream.setVisibility(View.GONE);
			// if(!ViewPagerFragmentNew.isMeeting){
			// img_photograph.setVisibility(View.VISIBLE);
			// img_picture.setVisibility(View.VISIBLE);
			// img_document.setVisibility(View.VISIBLE);
			// }
			// tv_switch_audio.setVisibility(View.VISIBLE);
		}
	}

	public void checkWatchSelf() {
		MeetingUser mu = Session.getInstance().getUserMgr().getSelfUser();
		boolean bWathmeWish = WeiyiMeetingClient.getInstance().getWatchMeWish();
		Log.e("emm", "bWathmeWish=" + bWathmeWish + " getWatch="
				+ Session.getInstance().getUserMgr().getSelfUser().getWatch());
		if (bWathmeWish && !Session.getInstance().getUserMgr().getSelfUser().getWatch()) {
			if (Session.getInstance().getMeetingtype() != 11) {
				watchVideo(0, true, mu.getDefaultCameraIndex());
			}
			Log.e("emm", "checkWatchSelf true");
		} else if (!bWathmeWish && Session.getInstance().getUserMgr().getSelfUser().getWatch()) {
			Log.e("emm", "checkWatchSelf false");
			watchVideo(0, false, mu.getDefaultCameraIndex());
		}
		// Log.e("emm","checkWatchSelf true");
		else if (!bWathmeWish && Session.getInstance().getUserMgr().getSelfUser().getWatch()) {
			Log.e("emm", "checkWatchSelf false");
			watchVideo(0, false, mu.getDefaultCameraIndex());
		}

	}

	public static int isNetworkOnline() {
		boolean status = false;
		try {
			if (WeiyiMeetingClient.getApplicationContext() == null)
				return 0;

			ConnectivityManager cm = (ConnectivityManager) WeiyiMeetingClient.getApplicationContext()
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
				return 1;
			} else {
				netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
					return 2;
				}
			}
		} catch (Exception e) {
			return 0;
		}
		return 0;
	}

	public void watchVideo(int nPlayID, boolean watch, int videoid) {
		Log.d("emm", "watchVideo " + nPlayID + " " + watch);
		if (WeiyiMeetingClient.getInstance().getM_nWatchVideoIDs().size() >= WeiyiMeetingClient.getInstance()
				.getMaxWatchVideoCount() && watch) {
			Toast.makeText(getActivity(), UZResourcesIDFinder.getResStringID("over_video_number"), Toast.LENGTH_SHORT)
					.show();
			return;
		}

		else {
			WeiyiMeetingClient.getInstance().PlayVideo(nPlayID, watch, m_vCamearSelf, 0, 0, 1, 1, 0, false,
					WeiyiMeetingClient.getInstance().isM_scattype(), videoid);
			showLayout();
			Log.e("121231", "2");
			doVideoLayout();
		}

	}

	int DiptoPX(int nDP) {
		final float scale = this.getActivity().getResources().getDisplayMetrics().density;
		return (int) (nDP * scale + 0.5f);
	}

	// void SwithBigandSmallCamera(){
	//
	// if(null !=
	// MeetingSession.getInstance().getUserMgr().getUser(MeetingSession.getInstance().getCurrentWatchID()))
	// {
	// MeetingSession.getInstance().m_bIsbigCameraShowSelf =
	// !MeetingSession.getInstance().m_bIsbigCameraShowSelf;
	//
	// }
	// else
	// {
	// MeetingSession.getInstance().m_bIsbigCameraShowSelf = true;
	// }
	//
	// watchVideo(-1);
	// }

	// void WatchMainVideo(boolean bWatch,int nPeerID){
	// MeetingSession.getInstance().m_bIsbigCameraShowSelf = false;
	// watchVideo(-1);
	// }
	boolean checkAutoVideoOut() {
		return false;
		/*
		 * if(!MeetingSession.getInstance().m_bAutoVideoMode ||
		 * MeetingSession.getInstance()._isSyncVideo) return false; int nSpeakerID = 0;
		 * int nChairManID = MeetingSession.getInstance().getChairManID(); MeetingUser
		 * muChairMan = MeetingSession.getInstance().getUserMgr().getUser(nChairManID);
		 * if(muChairMan!=null&&muChairMan.getAudioStatus() ==
		 * MeetingSession.RequestSpeak_Allow && muChairMan.ishasVideo()){ nSpeakerID =
		 * nChairManID; }else { for(int i = 0; i <
		 * MeetingSession.getInstance().mALSpeakerList.size();i++){ MeetingUser mufirst
		 * = MeetingSession.getInstance().getUserMgr().getUser(MeetingSession
		 * .getInstance().mALSpeakerList.get(i));
		 * if(mufirst!=null&&mufirst.ishasVideo()){ nSpeakerID = mufirst.getPeerID();
		 * break; } } if(nSpeakerID == 0){ if(muChairMan!=null&&
		 * muChairMan.ishasVideo()){ nSpeakerID = nChairManID; }else{ for(int i = 0; i <
		 * MeetingSession.getInstance().getUserMgr().getCount();i++){ MeetingUser
		 * mufirst = MeetingSession.getInstance().getUserMgr().getUserByIndex(i);
		 * if(mufirst!=null&&mufirst.ishasVideo()){ nSpeakerID = mufirst.getPeerID();
		 * break; } }
		 * 
		 * } } } if(nSpeakerID!=0&&nSpeakerID!=MeetingSession.getInstance().
		 * getCurrentWatchID()&&nSpeakerID != MeetingSession.getInstance().getMyPID()){
		 * checkVideoOut(nSpeakerID); return true; } return false;
		 */
	}

	public void unWatchAll() {
		List<MyWatch> infoIds = WeiyiMeetingClient.getInstance().getM_nWatchVideoIDs();
		for (int j = 0; j < infoIds.size(); j++) {
			Integer in = infoIds.get(j).getPeerid();
			Integer ca = infoIds.get(j).getCameraid();
			// MeetingUser mu = MeetingSession.getInstance().getUserMgr().getUser(in);
			// if(mu.getCameraCount()>0){
			for (int i = 0; i < infoIds.size(); i++) {
				WeiyiMeetingClient.getInstance().PlayVideo(in, false, m_vCamearSelf, (float) 0.0, (float) 0.0,
						(float) 0.0, (float) 0.0, 0, false, WeiyiMeetingClient.getInstance().isM_scattype(), ca);
			}
			// }

			// xiaoyang change
		}
		Log.e("121231", "3");
		this.showLayout();
		doVideoLayout();
	}

	class COORDINATE extends Object {
		public int r = 0;// rows
		public int c = 0;// columns
		public float w = 0;// width
		public float h = 0;// height
	}

	class POSITION extends Object {
		public float left = 0;
		public float top = 0;
		public float right = 0;
		public float bottom = 0;
		public boolean border = false;
		public int zorder = 0;

		POSITION(float l, float t, float r, float b, boolean bd, int z) {
			left = l;
			top = t;
			right = r;
			bottom = b;
			border = bd;
			zorder = z;
		}
		private boolean getBorder() {
			return border;
		}
	}

	public void setIsFullScreen(boolean isFull) {
		_isFull = isFull;
		doVideoLayout();
	}

	public void doVideoLayout() {

		if (muself == null || getActivity() == null)
			return;
		if (muself.getRole() == 2 && WeiyiMeetingClient.getInstance().get_syncVideoList().size() == 0
				&& !WeiyiMeetingClient.getInstance().isLiveMeeting())
			return;
		List<MyWatch> infoIds = new ArrayList<MyWatch>();
		infoIds.addAll(WeiyiMeetingClient.getInstance().getM_nWatchVideoIDs());

		int w = FaceMeeting_Activity.displaySize.x;
		int h = FaceMeeting_Activity.displaySize.y;
		if (w == 0 || h == 0) {
			w = m_vCamearSelf.getWidth();
			h = m_vCamearSelf.getHeight();
		}

		Log.e("emm", "doVideoLayout " + infoIds.size() + " " + w + ", " + h);

		if (!Utitlties.isPad(getActivity()) || _isFull) {
			/*
			 * if (infoIds.size() == 0) { if (MeetingSession.getInstance().getWatchMeWish())
			 * { img_no_video.setVisibility(View.GONE);
			 * txt_no_video.setVisibility(View.VISIBLE); } else {
			 * img_no_video.setVisibility(View.VISIBLE);
			 * txt_no_video.setVisibility(View.GONE); } }
			 */
			if (infoIds.size() > 0) {
				m_vCamearSelf.setVisibility(View.VISIBLE);

				ArrayList<POSITION> positions = new ArrayList<POSITION>();
				positions.add(new POSITION(0, 0, 1, 1, false, 0));
				int count = infoIds.size();
				if (count > 1) {
					float padding = 20;
					float absoluteWidth = Math.min(w, h) / 4;
					float vw = (float) (absoluteWidth / (float) w);
					float vh = (float) (absoluteWidth / (float) h);
					float paddingw = (float) (padding / (float) w);
					float paddingh = (float) (padding / (float) h);

					for (int i = 1; i < count; i++) {
						positions.add(new POSITION(1 - paddingw - vw * (count - i), 1 - paddingh - vh,
								1 - paddingw - vw * (count - i - 1), 1 - paddingh, true, 1));
					}
				}

				for (int j = 0; j < infoIds.size(); j++) {



					Integer in = infoIds.get(j).getPeerid();
					Integer ca = infoIds.get(j).getCameraid();
					POSITION pos = positions.get(j);


					if(pos.border){


					}else{

						m_vCamearSelf.setZOrderOnTop(false);
					}
					m_vCamearSelf.setZOrderOnTop(true);
					WeiyiMeetingClient.getInstance().PlayVideo(in, true, m_vCamearSelf, pos.left, pos.top, pos.right,
							pos.bottom, pos.zorder, pos.border, WeiyiMeetingClient.getInstance().isM_scattype(), ca);
				}

				_videoPositions = positions;



			}

		} else {
			/*
			 * if (infoIds.size() == 0) { m_vCamearSelf.setVisibility(View.INVISIBLE); if
			 * (MeetingSession.getInstance().getWatchMeWish()) {
			 * img_no_video.setVisibility(View.GONE);
			 * txt_no_video.setVisibility(View.VISIBLE); } else {
			 * img_no_video.setVisibility(View.VISIBLE);
			 * txt_no_video.setVisibility(View.GONE); } } else
			 */
			if (infoIds.size() > 0) {
				m_vCamearSelf.setVisibility(View.VISIBLE);
				m_vCamearSelf.setBackgroundColor(android.graphics.Color.BLUE);
				float ww = (float) w / (float) 5.0;// 20151118 cyj: Hardcoded 1/5 as the ratio. no good.
				float paddingw = (float) 10.0 / ww;
				float paddingh = (float) 10.0 / (float) h;
				float vh = (float) ((float) 3.0 * (ww - 20.0) / (float) (4.0 * h));
				float top = paddingh;
				float bottom = paddingh + vh;
				for (int i = 0; i < infoIds.size(); i++) {
					Integer in = infoIds.get(i).getPeerid();
					Integer ca = infoIds.get(i).getCameraid();
					WeiyiMeetingClient.getInstance().PlayVideo(in, true, m_vCamearSelf, paddingw, top, 1 - paddingw,
							bottom, 0, false, WeiyiMeetingClient.getInstance().isM_scattype(), ca);
					top += paddingh + vh;
					bottom += paddingh + vh;
				}
			}
		}
	}

	// must be called in main thread!
	public void switchVideoPosition(int pos) {
		ArrayList<MyWatch> infoIds = new ArrayList<MyWatch>();
		infoIds.addAll(WeiyiMeetingClient.getInstance().getM_nWatchVideoIDs());

		if (pos < 1 || pos >= infoIds.size())
			return;

		Integer vid = infoIds.get(pos).getPeerid();
		Integer ca = infoIds.get(pos).getCameraid();
		infoIds.remove(pos);
		infoIds.add(0, new MyWatch(vid, ca));

		WeiyiMeetingClient.getInstance().setM_nWatchVideoIDs(infoIds);
		if (WeiyiMeetingClient.getInstance().getMyPID() == WeiyiMeetingClient.getInstance().getChairManID()
				&& WeiyiMeetingClient.getInstance().is_isSyncVideo()) {
			WeiyiMeetingClient.getInstance().setfocusVideo(vid, ca);
		}
		doVideoLayout();
	}

	public void switchMainVideoPosition(int peerid, int videoid) {
		ArrayList<MyWatch> infoIds = new ArrayList<MyWatch>();
		infoIds.addAll(WeiyiMeetingClient.getInstance().getM_nWatchVideoIDs());
		for (int i = 0; i < infoIds.size(); i++) {
			if (peerid == infoIds.get(i).getPeerid() && videoid == infoIds.get(i).getCameraid()) {
				infoIds.remove(i);
			}
		}

		infoIds.add(0, new MyWatch(peerid, videoid));

		WeiyiMeetingClient.getInstance().setM_nWatchVideoIDs(infoIds);
		doVideoLayout();
	}

}

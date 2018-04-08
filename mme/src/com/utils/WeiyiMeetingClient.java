package com.utils;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.broadcast.BroadcastPlayBack_Activity;
import com.broadcast.Broadcast_Activity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.main.mme.view.FullVideoView;
import com.meeting.ui.FaceMeeting_Activity;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.weiyicloud.whitepad.ControlMode;
import com.weiyicloud.whitepad.FaceShareControl;
import com.weiyicloud.whitepad.GetMeetingFileCallBack;
import com.weiyicloud.whitepad.NotificationCenter;
import com.weiyicloud.whitepad.SharePadMgr;

import info.emm.meeting.ChatData;
import info.emm.meeting.MeetingUser;
import info.emm.meeting.MyWatch;
import info.emm.meeting.Session;
import info.emm.meeting.SessionInterface;
import info.emm.sdk.RtmpClientMgr;
import info.emm.sdk.VideoView;
import info.emm.sdk.joinMeetingCallBack;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


/**
 * @author Super
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
@SuppressLint("DefaultLocale")
public class WeiyiMeetingClient implements NotificationCenter.NotificationCenterDelegate,SessionInterface{
	private int agreeCount = 0;
	private int disagreeCount = 0;

	private boolean isEnterMeeting = false;
	private static boolean isbroadcast = false;
	private static String instMid ="";
	public static final String FROM_TITLE = "from_title_notify";
	private static Integer sync = 0;
	private static WeiyiMeetingClient mInstance;
	//listen event
	public static final int STOP_TIMER = 100000;
	public static final int FORCE_EXIT_MEETING = 100003;

	private static String m_nickName="";
	private static String m_protocol="";
	private static Activity m_activity =null;
	private static MeetingNotify m_notify;
	private static String m_protocolName="weiyi://";
	private static String m_inviteAddress="";
	private static boolean bRjoinMeeting=false;
	private static boolean bForceExitMeeting=false;
	private boolean isNeedShowExitDialog = true;
	public boolean isNeedShowExitDialog() {
		return isNeedShowExitDialog;
	}



	public void setNeedShowExitDialog(boolean isNeedShowExitDialog) {
		this.isNeedShowExitDialog = isNeedShowExitDialog;
	}

	//	String pwd;
	static String gbprotocol = "";
	AsyncHttpClient client = Session.getInstance().client;


	// final static public String DEFAULT_SERVER = "192.168.0.99";
	// final static public String DEFAULT_PORT = "443";

	final static public String ASSOCAITEUSERID = "associatedUserID";
	final static public String ASSOCAITEMSGID = "assocaitedMsgID";
	//	public int nLastShowPageUser = 0;

	final static public int NET_CONNECT_ING = 0; // 正在链接
	final static public int NET_CONNECT_SUCCESS = 1; // 链接成功
	final static public int NET_CONNECT_FAILED = 2; // 链接失败
	final static public int NET_CONNECT_BREAK = 3; // 链接中断
	final static public int NET_CONNECT_ENABLE_PRESENCE = 4; // 成功出席会议
	final static public int NET_CONNECT_USER_IN = 5; // 用户进入会议
	final static public int NET_CONNECT_USER_OUT = 6; // 用户离开会议
	final static public int NET_CONNECT_LEAVE = 7; // 当前用户主动退出会议
	final static public int NET_CONNECT_USER_INLIST_COMPLETE = 8;// 已读取完当前与会的名单
	final static public int NET_CONNECT_WARNING = 9; // 开启权限的提示

	final static public int UI_NOTIFY_USER_AUDIO_CHANGE = 10;// 会议中有用户音频状态有变化
	final static public int UI_NOTIFY_USER_CHAIRMAN_CHANGE = 11;// 主席变化
	final static public int UI_NOTIFY_USER_SYNC_VIDEO_MODE_CHANGE = 12;// 同步视频模式变化
	final static public int UI_NOTIFY_USER_SYNC_WATCH_VIDEO = 13;// 主席要求同步观看视频
	final static public int UI_NOTIFY_USER_SYNC_LOOKROOM = 14;
	final static public int UI_NOTIFY_USER_LAYOUT_CHANGE = 15;
	final static public int UI_NOTIFY_USER_HOST_CHANGE = 16;// 主讲变化
	// final static public int UI_NOTIFY_USER_SHARPS_CHANGE = 17;
	final static public int UI_NOTIFY_USER_WHITE_PAD = 18;// 有白板消息（p1 String
	// json 数据）
	final static public int UI_NOTIFY_USER_CHANGE_NAME = 19;// 有用户修改昵称
	final static public int UI_NOTIFY_USER_WATCH_VIDEO = 20;// 我查看或关闭别人的视频
	final static public int UI_NOTIFY_CHAT_RECEIVE_TEXT = 21;// 有聊天消息
	// final static public int UI_NOTIFY_WATCH_BUDDY_VIDEO = 22;
	// final static public int UI_NOTIFY_UNWATCH_BUDDY_VIDEO = 23;
	final static public int UI_NOTIFY_SHOW_SCREEN_PLAY = 24;// 有人分享屏幕
	final static public int UI_NOTIFY_USER_UNREAD_MSG = 25;// 有未读消息
	final static public int UI_NOTIFY_SELF_VEDIO_WISH_CHANGE = 26;// 自己的video状态有变化
	final static public int UI_NOTIFY_USER_VEDIO_CHANGE = 27;// 其他人的video状态有变化
	//	final static public int UI_NOTIFY_USER_WHITE_PAD_DOC_CHANGE = 28;// 有白板文档变化
	final static private int UI_NOTIFY_USER_PICTURE_TAKEN = 29;// 视频预览拍照完成
	final static private int UI_NOTIFY_USER_FOCUS_CHANGED = 30;// 焦点用户变化
	final static public int UI_NOTIFY_USER_SERVER_RECORDING = 31;// 服务器录制状态

	final static public int UI_NOTIFY_USER_KICKOUT = 32;// 自己被踢出会议
	final static public int UI_NOTIFY_USER_HOSTSTATUS = 33;// 数据控制权变化
	final static public int UI_NOTIFY_MEETING_MODECHANGE = 34;// 会场模式变化
	final static public int UI_NOTIFY_CALL_FUNCTION = 35;// 呼叫功能
	//		final static private int UI_NOTIFY_REMOTE_MESSAGE = 36;// 远程消息

	public static final int CHECK_MEETING = 37;// 选择会议
	public static final int REQUEST_CHAIRMAN = 38;// 申请成为主席

	public static final int GETMEETING_CONFIG = 40;// 获取会议的配置
	public static final int GETMEETING_DOC = 41;// 获取会议文档
	public static final int DELMEETING_DOC = 42;// 删除会议文档

	public static final int EXIT_MEETING = 43;// 退出会议
	public static final int PRESENCE_COMPLETE = 44;// 出席成功
	public static final int AUTO_EXIT_MEETING = 45;// 自动退出会议
	// 网页共享代码
	public static final int UI_NOTIFY_USER_START_WEBSHRAE = 46;// 开始网页共享
	public static final int UI_NOTIFY_USER_STOP_WEBSHRAE = 47;// 停止网页共享
	public static final int UI_NOTIFY_USER_SHOW_WEBPAGE = 48;// 显示网页的页数

	// 视频回调
	public static final int VIDEO_NOTIFY_CAMERA_DID_OPEN = 49;// 打开视频
	public static final int VIDEO_NOTIFY_CAMERA_WILL_CLOSE = 50;// 关闭视频

	final static public int UI_NOTIFY_CHAIRMAN_TAKE_BACK_HOST_RIGHT = 51;// 主席收回了所有的主讲权限
	final static public int UI_NOTIFY_CHAIRMAN_TAKE_BACK_FREE_SPEAK_RIGHT = 52;// 主席收回了所有人的的自由发言权限
	final static public int UI_NOTIFY_CHAIRMAN_TAKE_BACK_FREE_RECORD = 53;// 主席收回了所有人的的录制权限

	final static public int UI_NOTIFY_CHAIRMAN_ALLOW_FREE_HOST_RIGHT = 54;// 主席同意所有人的主讲权限
	final static public int UI_NOTIFY_CHAIRMAN_ALLOW_FREE_SPEAK_RIGHT = 55;// 主席同意所有人自由发言权限
	final static public int UI_NOTIFY_CHAIRMAN_ALLOW_FREE_RECORD = 56;// 主席同意所有人的的录制权限

	final static public int UI_NOTIFY_SHOW_TABPAGE = 57;// 显示标签的页数

	final static public int GETINVITEUSERSLIST = 60;//获取会议预约时的参会人员
	final static public int LIVECHANGE = 61;
	final static public int LEAVE_LIVE = 62;
	final static public int UI_NOTIFY_USER_CAMERA_CHANGE = 63;
	final static public int UI_NOTIFY_SWITCH_MAIN_VIDEO = 64;
	final static public int UI_NOTIFY_PLAY_MOVIE = 65;
	final static public int UI_NOTIFY_RECEIVE_SOCKET_MESSAGE = 66;
	final static public int LIVE_SIGNAL_CONNECTED = 67;
	final static public int DIRECT_MEETINGTYPE = 68;
	final static public int START_BROADCAST = 69;
	final static public int UI_NOTIFY_HANDSUP_START = 70;
	final static public int UI_NOTIFY_HANDSUP_STOP = 71;
	final static public int LIVE_WHITEPAD_JSON_BACK = 72;
	final static public int SHOWPAGE = 73;
	final static public int SCREENSHARE_CHANGE = 74;//屏幕共享缩放
	final static public int UI_NOTIFY_HANDSUP_ACK = 75;
	final static public int UI_NOTIFY_GET_ONLINE_NUM = 76;
	final static public int UI_NOTIFY_SIP_ACK_STATE = 77;
	final static public int UI_NOTIFY_SIP_FOCUS_CHANGED = 78;

	//	static public int SENDMSGTOALL_EXCEPT_ME = 0;
	//	static public int SENDMSGTOALL = 0xFFFFFFFF;
	//	static public int SENDMSGTOSERVER = 0xFFFFFFFE;

	public static int Kickout_ChairmanKickout = 0;
	public static int Kickout_Repeat = 1;

	//	static public int RequestSpeak_Disable = 0;// 申请自由发言失败
	//	static public int RequestSpeak_Allow = 1;// 成功
	//	static public int RequestSpeak_Pending = 2;// 正在申请

	static public int RequestHost_Disable = 0;// 申请主讲失败
	static public int RequestHost_Allow = 1;// 成功
	static public int RequestHost_Pending = 2;// 申请中

	//	static public int MeetingMode_Free = 0;// 自由模式
	//	static public int MeetingMode_ChairmanControl = 1;// 主控模式

	//	private ArrayList<Integer> mALSpeakerList = new ArrayList<Integer>();

	//	private ArrayList<Integer> mALPendingSpeakerList = new ArrayList<Integer>();
	private Set<Integer> mALWatchMe = new HashSet<Integer>();

	//	private List<ChatData> list = new ArrayList<ChatData>();
	//	private HashMap<Integer, List<ChatData>> MSG = new HashMap<Integer, List<ChatData>>();

	private ArrayList<JSONObject> _syncVideoList = new ArrayList<JSONObject>();
	private boolean _isSyncVideo = false;
	private boolean _isAutoSyncVideo = false;
	private boolean m_isLocked = false;
	// private int m_watchMeCount = 0;

	// private int m_ChairmanMode = 0;
	private int m_maxSpeakerCount = 9;
	private int m_nChairmanID = 0;

	private int m_hostID = 0;

	private ArrayList<Integer> mALPendingHostList = new ArrayList<Integer>();
	private ArrayList<Integer> mALHostList = new ArrayList<Integer>();

	//		private SharePadMgr m_thisSharePad = SharePadMgr.getInstance();
	//		private WhitePadInterface padInterface;
	//		private MeetingUserMgr getUserMgr() = new MeetingUserMgr();

	private boolean m_bWatchWish = true;
	//	private ArrayList<MyWatch> m_nWatchVideoIDs = new ArrayList<MyWatch>();
	//xiaoyang add
	//	private Map<Integer, ArrayList<Integer>> mapVideoIDs = new HashMap<Integer, ArrayList<Integer>>();

	//xiaoyang add



	private boolean m_bAutoVideoMode = false;
	private boolean m_bIsbigCameraShowSelf = true;
	private String sFileforphone = "";

	private int m_nScreenSharePeerID = 0;
	private int m_nWebSharePeerID = 0;
	//	private int m_nFocusUserPeerID = 0;
	//	private boolean m_bServerRecording = false;
	private boolean bHasFrontCamera = true;
	//	private int m_isAllowServerRecord = -1;
	//	private int m_isAutoServerRecord = -1;

	//xiaoyang add 记录影音播放id和状态
	private int moviePlayerId = 0;
	private boolean moviestate = false;
	//xiaoyang add 记录影音播放id和状态



	static String linkUrl = "";
	static String linkName = "";

	//		private boolean m_bInmeeting = false;
	private String m_strMeetingID;
	private int createrid;
	private String m_userIdfaction;
	//private int meetingtype;
	//	private String m_strMeetingName = "";
	private String m_strUserName;
	private String m_strMeetingCompanyID;
	private String m_strMeetingPassword;
	//	private boolean m_instMeeting;
	private boolean m_isCaller;
	private String m_pwd;
	private int m_chatid = 0;
	//	private int m_autoExitWeiyi = 0;


	//	private int m_sessionStatus = 0;// 0 idle, 1 connecting, 2 connected

	//	private int m_bSpeakFree = 1; // 自由发言
	//	private int m_bAllowRecord = 1;// 自由录制
	//	private int m_bControlFree = 1;// 自由主讲

	private int m_showInvitation = -1;// Invitation
	private int m_showWhiteBoard = -1;// WhiteBoard
	private int m_showChatList = -1;// ChatList
	private int m_showUserList = -1;// UserList
	private int m_showDocList = -1;// DocumentList
	private int m_showApplyChairman = -1;// ApplyChairman
	private int m_showApplyHost = -1;// ApplyHost
	private int m_autoOpenAV = -1;// Open audio and video automatically
	private int m_autoQuit = -1;// Quite meeting automatically
	private int m_showAudio = -1;//显示音频开关
	private int m_showVideo = -1;//显示视频开关


	/*
	 * chairmancontrol 会议属性 1111111001001111101000001000000000000000 1.音频 2.视频
	 * 3.白板 4.程序共享 5.邀请 6.录制 7.分享影音 8.会议到期自动退出(0:不退出 1:退出) 9.投票(未实现)
	 * 10.文件传输 11.高清功能(0:标清 1:高清) 12.问答(未实现) 13.隐藏主席(0:隐藏 1:显示) 14.隐藏主讲(0:隐藏
	 * 1:显示) 15.文本聊天 16.用户列表 17.文档列表 18.是否切图（缺省0：切图,1：不切图）不切图用于医疗系统，显示完整带黑边的视频
	 * 19.网页共享 20.自动进入会议(0:自动 1:不自动） 21.是否启用音视频设置向导(Flash)(0:不启用 1:启用) 22.sip电话
	 * 23.H323功能终端或MCU 24.自动开启音视频 25.公司会议是否可见 26.是否可以创建公司 27:是否隐藏视频窗口关闭按钮(Flash
	 * 0:显示 1：隐藏) 28:是否隐藏视频窗口用户名(Flash 0:显示 1：隐藏) 29:等分视频布局下列优先排列(Flash 0:横排列
	 * 1：竖排列), 30:多线程播放视频 0:多线程(缺省) 1：单线程（ANDRIOD需要传递手机型号给WEB API）31-40预留功能位
	 */
	private String m_chairmancontrol = "11111110010011111010000010000";
	/* 1.允许所有人自由发言 2.允许所有人获得主讲权限 3. 允许所有人录制 4.布局跟随 5.视频跟随 */
	private String m_chairmanfunc = "";
	private int m_bSupportSensor = 1;
	// 0 is rotation,1 is heng ping
	private int m_bSupportRotation = 0;
	// 0不隐藏，1是隐藏，缺省是不隐藏
	//	private int m_hideme = 0;

	/*
	 * private int m_meetingStartTime = 0; private int m_meetingEndTime = 0;
	 * private String detailsId; private String detailsName; private String
	 * detailsChairmanPwd; private String detailsConfuserPwd; private String
	 * detailsSidelineuserPwd;
	 */
	private boolean m_isLouder = true;

	//	private String m_uploadfilename="";

	//	private boolean m_isViewer = false;



	private static String webServer;
	private static String mid="";
	private static String pwd="";
	private static int thirdID = 0;
	private static int quitsoftware = 0;//(0,不退，1退出);
	VodMsgList m_shapeList = null;
	VodMsgList m_pageList = null;
	//xiaoyang add 获取流用户
	public String webFun_getStream = "";
	int videoForSipPeerId = -1;
	int videoForSipVideoId = 0;

	//	static public int SENDMSGTOALL_EXCEPT_ME = 0;
	//	static public int SENDMSGTOALL = 0xFFFFFFFF;
	//	static public int SENDMSGTOSERVER = 0xFFFFFFFE;


	//	enum DataTabIndex {
	//		DocumentShare, // 白板页 0
	//		ApplicationShare, // 桌面共享页
	//		WebPageShare, // 网页共享页
	//		CustomTab // 自定义页 海纳进入会议 传进来的网页tab 10
	//	};

	public int getAgreeCount() {
		return agreeCount;
	}



	public int getDisagreeCount() {
		return disagreeCount;
	}

	public void setChairmanFunc(String chairmanFunc) {
		Session.getInstance().setChairmanFunc(chairmanFunc);
	}



	/**
	 * 是否隐藏自己 0是不隐藏自己，1是隐藏自己
	 *
	 * @return
	 */
	public int getM_hideme() {
		return Session.getInstance().getM_hideme();
	}

	public void setM_hideme(int hideme) {
		Session.getInstance().setM_hideme(hideme);
	}

	private int m_currentCameraIndex = 0;
	private boolean m_isFrontCamera = true;

	public int getMoviePlayerId() {
		return moviePlayerId;
	}

	public boolean isMoviestate() {
		return moviestate;
	}

	/**
	 * 是否是自由发言
	 *
	 * @return
	 */
	public boolean isSpeakFree() {
		return Session.getInstance().isSpeakFree();
	}

	public void setM_speakFree(boolean bFree) {
		Session.getInstance().setM_speakFree(bFree);
	}

	/**
	 * 是否允许录制
	 *
	 * @return
	 */
	public boolean isAllowRecord() {
		return Session.getInstance().isAllowRecord();
	}

	public void setM_allowRecord(boolean bAllowRecrod) {
		Session.getInstance().setM_allowRecord(bAllowRecrod);
	}

	/**
	 * 是否是主控
	 *
	 * @return
	 */
	public boolean isControlFree() {
		return Session.getInstance().isControlFree();
	}

	public void setM_controlFree(boolean bFree) {
		Session.getInstance().setM_controlFree(bFree);
	}





	public int getM_bSupportRotation() {
		return m_bSupportRotation;
	}

	public void setM_bSupportRotation(int bSupportRotation) {
		this.m_bSupportRotation = bSupportRotation;
	}

	/**
	 * 传感器
	 *
	 * @return
	 */
	public int getM_bSupportSensor() {
		return m_bSupportSensor;
	}

	public void setM_bSupportSensor(int bSupportSensor) {
		this.m_bSupportSensor = bSupportSensor;
	}

	/**
	 * 自动退出远程平台
	 *
	 * @return
	 */
	public int getM_autoExitWeiyi() {
		return Session.getInstance().getM_autoExitWeiyi();
	}

	public void setM_autoExitWeiyi(int autoExitWeiyi) {
		Session.getInstance().setM_autoExitWeiyi(autoExitWeiyi);
	}

	/**
	 * 获取邀请地址
	 *
	 * @return
	 */
	public String getM_inviteAddress() {
		return m_inviteAddress;
	}

	public void setM_inviteAddress(String inviteAddress) {
		this.m_inviteAddress = inviteAddress;
	}
	public ArrayList<Integer> getmALSpeakerList() {
		return Session.getInstance().getmALSpeakerList();
	}

	public void setmALSpeakerList(ArrayList<Integer> mALSpeakerList) {
		Session.getInstance().setmALPendingSpeakerList(mALSpeakerList);
	}

	public ArrayList<Integer> getmALPendingSpeakerList() {
		return Session.getInstance().getmALPendingSpeakerList();
	}

	public void setmALPendingSpeakerList(
			ArrayList<Integer> mALPendingSpeakerList) {
		Session.getInstance().setmALPendingSpeakerList(mALPendingSpeakerList);
	}

	public Set<Integer> getmALWatchMe() {
		return mALWatchMe;
	}

	public void setmALWatchMe(Set<Integer> mALWatchMe) {
		this.mALWatchMe = mALWatchMe;
	}

	public HashMap<Integer, List<ChatData>> getMSG() {
		return Session.getInstance().getMSG();
	}
	//
	//	public void setMSG(HashMap<Integer, List<ChatData>> mSG) {
	//		MSG = mSG;
	//	}

	/**
	 * 同步视频的列表
	 *
	 * @return
	 */
	public ArrayList<JSONObject> get_syncVideoList() {
		return _syncVideoList;
	}

	public void set_syncVideoList(ArrayList<JSONObject> _syncVideoList) {
		this._syncVideoList = _syncVideoList;
	}

	/**
	 * 是否同步视频
	 *
	 * @return
	 */
	public boolean is_isSyncVideo() {
		return _isSyncVideo;
	}

	public void set_isSyncVideo(boolean _isSyncVideo) {
		this._isSyncVideo = _isSyncVideo;
	}

	/**
	 * 是否自动同步视频
	 *
	 * @return
	 */
	public boolean is_isAutoSyncVideo() {
		return _isAutoSyncVideo;
	}

	public void set_isAutoSyncVideo(boolean _isAutoSyncVideo) {
		this._isAutoSyncVideo = _isAutoSyncVideo;
	}

	/*
	 * public int getM_ChairmanMode() { return m_ChairmanMode; }
	 *
	 * public void setM_ChairmanMode(int m_ChairmanMode) { this.m_ChairmanMode =
	 * m_ChairmanMode; }
	 */
	/**
	 * 获取最多发言人的数量 9
	 *
	 * @return
	 */
	public int getM_maxSpeakerCount() {
		return m_maxSpeakerCount;
	}

	public void setM_maxSpeakerCount(int m_maxSpeakerCount) {
		this.m_maxSpeakerCount = m_maxSpeakerCount;
	}

	/**
	 * 主席的id
	 *
	 * @return
	 */
	public int getM_nChairmanID() {
		return m_nChairmanID;
	}

	public void setM_nChairmanID(int m_nChairmanID) {
		this.m_nChairmanID = m_nChairmanID;
//		Session.getInstance().setM_nChairmanID(m_nChairmanID);
	}

	/**
	 * 主讲的id
	 *
	 * @return
	 */
	public int getM_hostID() {
		return m_hostID;
	}

	public void setM_hostID(int m_hostID) {
		this.m_hostID = m_hostID;
	}

	public ArrayList<Integer> getmALPendingHostList() {
		return mALPendingHostList;
	}

	public void setmALPendingHostList(ArrayList<Integer> mALPendingHostList) {
		this.mALPendingHostList = mALPendingHostList;
	}

	public boolean isM_bWatchWish() {
		return m_bWatchWish;
	}

	public void setM_bWatchWish(boolean m_bWatchWish) {
		this.m_bWatchWish = m_bWatchWish;
	}

	public ArrayList<MyWatch> getM_nWatchVideoIDs() {

		return Session.getInstance().getM_nWatchVideoIDs();
	}

	public void setM_nWatchVideoIDs(ArrayList<MyWatch> m_nWatchVideoIDs) {
		Session.getInstance().setM_nWatchVideoIDs(m_nWatchVideoIDs);
	}

	public boolean isM_bAutoVideoMode() {
		if (m_autoOpenAV == -1) {
			if (m_chairmancontrol.length() > 23) {
				char c = m_chairmancontrol.charAt(23);
				if (c == '1')
					return true;
			}
		} else {
			return m_autoOpenAV == 1 ? true : false;
		}

		return false;
	}

	public boolean isM_bAutoQuit() {
		if (m_autoQuit == -1) {
			if (m_chairmancontrol.length() > 7) {
				char c = m_chairmancontrol.charAt(7);
				if (c == '1')
					return true;
			}
		} else {
			return m_autoQuit == 1 ? true : false;
		}

		return false;
	}

	/**
	 * 隐藏邀请
	 *
	 * @return
	 */
	public boolean isM_bShowInvite() {
		if (m_showInvitation == -1) {
			if (m_chairmancontrol.length() > 4) {
				char c = m_chairmancontrol.charAt(4);
				if (c == '1')
					return true;
			}
		} else {
			return m_showInvitation == 1 ? true : false;
		}
		return false;
	}
	/**
	 * 隐藏音频按钮
	 *
	 * @return
	 */
	public boolean isM_bShowAudio(){
		if (m_showAudio == -1) {
			if (m_chairmancontrol.length() > 2) {
				char c = m_chairmancontrol.charAt(0);
				if (c == '1')
					return true;
			}
		} else {
			return m_showAudio == 1 ? true : false;
		}

		return false;
	}

	/**
	 * 隐藏音频按钮
	 *
	 * @return
	 */
	public boolean isM_bShowVideo(){
		if (m_showVideo == -1) {
			if (m_chairmancontrol.length() > 2) {
				char c = m_chairmancontrol.charAt(0);
				if (c == '1')
					return true;
			}
		} else {
			return m_showVideo == 1 ? true : false;
		}

		return false;
	}

	/**
	 * 隐藏白板
	 *
	 * @return
	 */
	public boolean isM_bShowWhite() {
		if (m_showWhiteBoard == -1) {
			if (m_chairmancontrol.length() > 2) {
				char c = m_chairmancontrol.charAt(2);
				if (c == '1')
					return true;
			}
		} else {
			return m_showWhiteBoard == 1 ? true : false;
		}

		return false;
	}

	/**
	 * 隐藏文本聊天
	 *
	 * @return
	 */
	public boolean isM_bShowTextChat() {
		if (m_showChatList == -1) {
			if (m_chairmancontrol.length() > 14) {
				char c = m_chairmancontrol.charAt(14);
				if (c == '1')
					return true;
			}
		} else {
			return m_showChatList == 1 ? true : false;
		}

		return false;
	}

	/**
	 * 隐藏用户列表
	 *
	 * @return
	 */
	public boolean isM_bShowUserList() {
		if (m_showUserList == -1) {
			if (m_chairmancontrol.length() > 15) {
				char c = m_chairmancontrol.charAt(15);
				if (c == '1') {
					return true;
				}
			}
		} else {
			return m_showUserList == 1 ? true : false;
		}

		return false;
	}

	/**
	 * 隐藏文档列表
	 *
	 * @return
	 */
	public boolean isM_bShowDocList() {
		if (m_showDocList == -1) {
			if (m_chairmancontrol.length() > 16) {
				char c = m_chairmancontrol.charAt(16);
				if (c == '1') {
					return true;
				}
			}
		} else {
			return m_showDocList == 1 ? true : false;
		}

		return false;
	}

	/**
	 * 隐藏主席
	 *
	 * @return
	 */
	public boolean isM_bshowChairman() {
		if (m_showApplyChairman == -1) {
			if (m_chairmancontrol.length() > 12) {
				char c = m_chairmancontrol.charAt(12);
				if (c == '1') {
					return true;
				}
			}
		} else {
			return m_showApplyChairman == 1 ? true : false;
		}

		return false;
	}

	/**
	 * 隐藏主讲
	 *
	 * @return
	 */
	public boolean isM_bshowHost() {
		if (m_showApplyHost == -1) {
			if (m_chairmancontrol.length() > 13) {
				char c = m_chairmancontrol.charAt(13);
				if (c == '1') {
					return true;
				}
			}
		} else {
			return m_showApplyHost == 1 ? true : false;
		}

		return false;
	}

	//	/**
	//	 * 0:多线程播放视频 1:单线程播放视频
	//	 *
	//	 * @return
	//	 */
	//	public boolean isM_MultiThread() {
	//		if (m_chairmancontrol.length() > 29) {
	//			char c = m_chairmancontrol.charAt(29);
	//			if (c == '1') {
	//				return true;
	//			}
	//		}
	//		return false;
	//	}

	/**
	 * 是否切图 chairmancontrol 18位的 缺省0 切图，1是不切图，这个地方1是切，0是不切
	 *
	 * @return
	 */
	public int isM_scattype() {
		if (m_chairmancontrol.length() > 17) {
			char c = m_chairmancontrol.charAt(17);
			if (c == '1') {
				return 0;
			}else if(c == '0'){
				return 0;//不切视频头像完整
			}
		}
		return 1;
	}

	public boolean isM_bAutoRecord() {
		if (m_chairmancontrol.length() > 31) {
			char c = m_chairmancontrol.charAt(31);
			if (c == '1')
				return true;
		}
		return false;
	}

	/**
	 * 自动开启音视频
	 *
	 * @return
	 */
	public void setM_bAutoVideoMode(boolean m_bAutoVideoMode) {
		this.m_bAutoVideoMode = m_bAutoVideoMode;
	}

	public boolean isM_bIsbigCameraShowSelf() {
		return m_bIsbigCameraShowSelf;
	}

	public void setM_bIsbigCameraShowSelf(boolean m_bIsbigCameraShowSelf) {
		this.m_bIsbigCameraShowSelf = m_bIsbigCameraShowSelf;
	}

	public String getsFileforphone() {
		return sFileforphone;
	}

	public void setsFileforphone(String sFileforphone) {
		this.sFileforphone = sFileforphone;
	}

	public int getM_nScreenSharePeerID() {
		return m_nScreenSharePeerID;
	}

	public void setM_nScreenSharePeerID(int m_nScreenSharePeerID) {
		this.m_nScreenSharePeerID = m_nScreenSharePeerID;
	}

	public int getM_nFocusUserPeerID() {
		return Session.getInstance().getM_nFocusUserPeerID();
	}

	public void setM_nFocusUserPeerID(int m_nFocusUserPeerID) {
		Session.getInstance().setM_nFocusUserPeerID(m_nFocusUserPeerID);
	}

	public boolean isM_bServerRecording() {
		return Session.getInstance().isM_bServerRecording();
	}

	public void setM_bServerRecording(boolean m_bServerRecording) {
		Session.getInstance().setM_bServerRecording(m_bServerRecording);
	}

	public boolean isbHasFrontCamera() {
		return bHasFrontCamera;
	}

	public void setbHasFrontCamera(boolean bHasFrontCamera) {
		this.bHasFrontCamera = bHasFrontCamera;
	}

	//		public boolean isM_bInmeeting() {
	//			return m_bInmeeting;
	//		}
	//
	//		public void setM_bInmeeting(boolean m_bInmeeting) {
	//			this.m_bInmeeting = m_bInmeeting;
	//		}

	public String getM_strMeetingID() {
		return Session.getInstance().getM_strMeetingID();
	}

	public void setM_strMeetingID(String m_strMeetingID) {
		Session.getInstance().setM_strMeetingID(m_strMeetingID);
	}
	public int getCreaterid() {
		return createrid;
	}

	//	public void setCreaterid(int createrid) {
	//		this.createrid = createrid;
	//	}
	public String getM_userIdfaction() {
		return m_userIdfaction;
	}

	public void setM_userIdfaction(String m_userIdfaction) {
		this.m_userIdfaction = m_userIdfaction;
	}

	public void setM_strMeetingName(String meetingName) {
		Session.getInstance().setM_strMeetingName(meetingName);
	}

	public String getM_strMeetingName() {
		return Session.getInstance().getM_strMeetingName();
	}

	public String getM_pwd() {
		return m_pwd;
	}

	public void setM_pwd(String m_pwd) {
		this.m_pwd = m_pwd;
	}

	public String getM_strUserName() {
		return m_strUserName;
	}

	public void setM_strUserName(String m_strUserName) {
		this.m_strUserName = m_strUserName;
	}

	public boolean isM_isLocked() {
		return m_isLocked;
	}

	public void setM_isLocked(boolean m_isLocked) {
		this.m_isLocked = m_isLocked;
	}

	public String getM_strMeetingCompanyID() {
		return m_strMeetingCompanyID;
	}

	public void setM_strMeetingCompanyID(String m_strMeetingCompanyID) {
		this.m_strMeetingCompanyID = m_strMeetingCompanyID;
	}

	public String getM_strMeetingPassword() {
		return m_strMeetingPassword;
	}

	public void setM_strMeetingPassword(String m_strMeetingPassword) {
		this.m_strMeetingPassword = m_strMeetingPassword;
	}
	public boolean Init(final Context context, final String code,
						final String serial, final boolean filelog) {

		//			padInterface.setAppContext(context);
		//			return bHasFrontCamera = RtmpClientMgr.getInstance().init(this,
		//					context, code, serial, filelog);
		SharePadMgr.getInstance().setShareControl(Session.getInstance());
		Log.e("TAG", "sharePadMsg..........");
		Session.getInstance().registerWhiteBroad(SharePadMgr.getInstance());
		Session.getInstance().registerListener(WeiyiMeetingClient.getInstance());
		return bHasFrontCamera = Session.getInstance().Init(context, code, serial, filelog);
	}

	/**
	 * 调用会议中某人的某个与会者定义的方法，被调用者会收到RtmpClientMgrCbk.ClientFunc_Call回调
	 */
	@Override
	public void onCallClientFunction(String name, int peerID, Object params) {

		if (name.equals("ClientFunc_CustomFunc"))
		{
			JSONArray arr = (JSONArray) params;

			try {
				String func = arr.getString(0);
				if (func.equals("ClientFunc_HandsUp")) {

					NotificationCenter.getInstance().postNotificationName(
							UI_NOTIFY_HANDSUP_START, peerID);

				}else if(func.equals("ClientFunc_StopHands")){
					NotificationCenter.getInstance().postNotificationName(
							UI_NOTIFY_HANDSUP_STOP, peerID);
					disagreeCount = 0;
					agreeCount = 0;
				}else if(func.equals("ClientFunc_HandsUpACK")){
					int isargee = arr.getInt(1);
					if(isargee == 0){
						disagreeCount++;
					}else if(isargee == 1){
						agreeCount++;
					}
					NotificationCenter.getInstance().postNotificationName(UI_NOTIFY_HANDSUP_ACK, agreeCount,disagreeCount);
					Log.d("xiao", "ClientFunc_HandsUpACK_01");
				}else if (func.equals("ClientFunc_ChairmanKickOut")) {
					//					JSONArray arr = (JSONArray) params;
					try {
						if(arr.get(1) instanceof Double){
							double res = (Double) arr.get(1);
							ClientFunc_ChairmanKickOut((int) res);
						}else if(arr.get(1) instanceof Integer){
							int res = (Integer) arr.get(1);
							ClientFunc_ChairmanKickOut(res);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}

		}
		else {
			NotificationCenter.getInstance().postNotificationName(
					UI_NOTIFY_CALL_FUNCTION, peerID, name,
					params);
		}
		Log.i("ClientCall", name);
	}

	/**
	 * 被主席踢出会议
	 *
	 * @param res
	 */
	private void ClientFunc_ChairmanKickOut(int res) {
		NotificationCenter.getInstance().postNotificationName(
				UI_NOTIFY_USER_KICKOUT, res);
	}

	//	/**
	//	 * 文档改变
	//	 */
	//	private void ClientFunc_DocumentChange(Object params) {
	//		// todo..
	//		// m_thisSharePad.handReceivedNotification(
	//		// MeetingSession.UI_NOTIFY_USER_WHITE_PAD_DOC_CHANGE, params);
	//		NotificationCenter.getInstance().postNotificationName(
	//				UI_NOTIFY_USER_WHITE_PAD_DOC_CHANGE, params);
	//	}





	/**
	 * 提示开启权限
	 */
	@Override
	public void onWarning(int warning) {
		NotificationCenter.getInstance().postNotificationName(
				NET_CONNECT_WARNING, warning);
	}
	/**
	 * 通知
	 *
	 * @param name
	 * @param toID
	 * @param body
	 */
	public void Notify(String name, int toID, String body) {
		JSONObject parameters = new JSONObject();

		try {
			parameters.put("1", name);
			parameters.put("2", toID);
			parameters.put("3", body);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		RtmpClientMgr.getInstance().callClientFunction(toID,
		//				"ServerFunc_CallClientFunc", parameters);
		Session.getInstance().notify(toID,"ServerFunc_CallClientFunc",parameters);
	}
	/**
	 * 获取时间
	 *
	 * @return
	 */
	public String getTime() {
		String time = null;
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
		time = dateFormat.format(date);
		return time;

	}

	/**
	 * 检查用户
	 *
	 * @param userinfo
	 */
	private void checkHasUser(JSONObject userinfo) {
		try {
			userinfo.put("m_EnablePresence", true);
			userinfo.put("m_HideSelf", true);
			int uid = userinfo.getInt("m_MeetBuddyID");
			String nickName = "";
			if (userinfo.has("m_NickName"))
				nickName = userinfo.getString("m_NickName");
			MeetingUser user = Session.getInstance().getUserMgr().getMeetingUser(uid);
			int myrole = Session.getInstance().getUserMgr().getSelfUser().getRole();
			if (myrole == 2 && user != null && !nickName.isEmpty()
					&& user.getName() != nickName)
				user.setName(nickName);

			if (user != null || uid == 0)
				return;

			Session.getInstance().onUserIn(userinfo, false);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onRecTextMsg(int fromid,int type,String msg,JSONObject textFromat) {


		NotificationCenter.getInstance().postNotificationName(
				UI_NOTIFY_CHAT_RECEIVE_TEXT, fromid, msg,
				type, textFromat);
	}



	/**
	 * 远程消息
	 *
	 * @param msg
	 * @return
	 * @throws JSONException
	 */
	public void onRemotePubMsg(String msgName, int fromID,
							   int associatedUserID, String id, String associatedMsgID,
							   Object body) {
		try {
			FileLog.e("emm", msgName);
			//			int sendID = fromID;

			boolean bHandel = true;

			Log.e("Remote_PubMsg", msgName);
			if (msgName.equals("RequestSpeak")) {

				int nCount = Session.getInstance().getSpeakerCount();
				// boolean bFreeMode = isFreeMode();
				int chairManID = getChairManID();
				int maxSpeakCount = getMaxSpeakerCount();
				if (fromID == associatedUserID && associatedUserID != chairManID) {
					if (this.isSpeakFree() && nCount < maxSpeakCount) {
						addSpeaker(associatedUserID);
						Session.getInstance().ChangeAudioStatus(associatedUserID, Session.RequestSpeak_Allow);

					} else {
						Session.getInstance().getmALPendingSpeakerList().add(associatedUserID);
						Session.getInstance().addPendingSpeaker(associatedUserID);
						Session.getInstance().ChangeAudioStatus(associatedUserID, Session.RequestSpeak_Pending);
					}
				} else {
					addSpeaker(associatedUserID);
					if (nCount > maxSpeakCount) {
						int removeSpeakerID = delFirstSpeaker();
						Session.getInstance().ChangeAudioStatus(removeSpeakerID, Session.RequestSpeak_Disable);
						if (removeSpeakerID == getMyPID()) {
							String ids = "RequestSpeak" + getMyPID();
							Session.getInstance().DeleteMessage("RequestSpeak", Session.SENDMSGTOSERVER,
									getMyPID(), null, ids);
						}
					}
					Session.getInstance().ChangeAudioStatus(associatedUserID, Session.RequestSpeak_Allow);
				}

			}else if (msgName.equals("ChairmanChange")) {
				int oldID = m_nChairmanID;
				// xiaoyang change
				MeetingUser muold = Session.getInstance().getUserMgr().getUser(oldID);
				if (muold != null) {
					muold.setRole(0);
				}
				if (associatedUserID != 0) {
					m_nChairmanID = associatedUserID;
					setM_nChairmanID(m_nChairmanID);
					int myid = this.getMyPID();
					// xiaoyang change
					MeetingUser muNew = Session.getInstance().getUserMgr().getUser(m_nChairmanID);
					if (muNew != null) {
						muNew.setRole(1);
					}

					Session.getInstance().getUserMgr().reSort();
				}
				if (oldID == 0 && associatedUserID == getMyPID()) {
					if (!isSpeakFree()) {
						setSpeakerMode(false);
					}
					if (!isControlFree()) {
						setControlMode(false);
					}
					if (!isAllowRecord()) {
						setAllowRecord(false);
					}
					if (isSyncVideo()) {
						syncVideo(true, true);
					}
				}
				if(this.getMyPID()==this.getChairManID()){
					if(Session.getInstance().getPadInterface()!=null)
						Session.getInstance().getPadInterface().setControlMode(ControlMode.fullcontrol);
					if(Session.getInstance().isAllowServerRecord()&&Session.getInstance().isAutoServerRecord())
					{
						int fouceUserId = Session.getInstance().getFocusUser();
						if(Session.getInstance().getServerRecordingStatus()){
							if(fouceUserId == -1||Session.getInstance().getUserMgr().getUser(fouceUserId)==null){
								Session.getInstance().serverRecording(true);
								int myPeerid = Session.getInstance().getMyPID();
								Session.getInstance().setFocusUser(myPeerid,0);
							}
						}else{
							Session.getInstance().serverRecording(true);
							int myPeerid = Session.getInstance().getMyPID();
							Session.getInstance().setFocusUser(myPeerid,0);
						}
					}else if(Session.getInstance().isAllowServerRecord()){
						if(Session.getInstance().getServerRecordingStatus()){
							int fouceUserId = Session.getInstance().getFocusUser();
							if(fouceUserId == -1||Session.getInstance().getUserMgr().getUser(fouceUserId)==null){
								Session.getInstance().serverRecording(true);
								int myPeerid = Session.getInstance().getMyPID();
								Session.getInstance().setFocusUser(myPeerid,0);
							}
						}
					}
				}else{
					if(Session.getInstance().getPadInterface()!=null&&Session.getInstance().getUserMgr().getSelfUser().getHostStatus()!=RequestHost_Allow)
						Session.getInstance().getPadInterface().setControlMode(ControlMode.watch);
				}
				sendDefaultVideoToSip();
				NotificationCenter.getInstance().postNotificationName(UI_NOTIFY_USER_CHAIRMAN_CHANGE, oldID,m_nChairmanID);
			}else if (msgName.equals("StartAppShare")) {
				if(m_nScreenSharePeerID!=0){
					return;
				}
				m_nScreenSharePeerID = associatedUserID;
				NotificationCenter.getInstance().postNotificationName(UI_NOTIFY_SHOW_SCREEN_PLAY, true);
			} else if (msgName.equals("RequestControl")) {
				boolean force = false;
				if(body instanceof Double){
					double tmp = (Double) body;
					int tmp2 = (int) tmp;
					force = tmp2==0?false:true;
				}else if(body instanceof Integer){
					int tmp = (Integer) body;
					force = tmp==0?false:true;
				}else{
					force = (Boolean) body;
				}
				if (!force) {
					if (isControlFree()) {
						ControlStatusChange(associatedUserID, RequestHost_Allow,true);
					} else {
						ControlStatusChange(associatedUserID, RequestHost_Pending,true);
					}
				} else {
					// if (m_hostID != 0) {
					// ControlStatusChange(m_hostID, RequestHost_Disable);
					// if (m_hostID == getMyPID()) {
					// String id = "RequestControl" + getMyPID();
					// DeleteMessage("RequestControl", SENDMSGTOSERVER,
					// getMyPID(), null, id);
					// }
					// }
					ControlStatusChange(associatedUserID, RequestHost_Allow, true);
				}
			} /*
			 * else if (msgName.equals("ChairmanMode")) { setMode(); }
			 */else if (msgName.equals("SyncVideoMode")) {
				// boolean auto = msg.getBoolean("body");
				syncVideoModeChange(true, true);
			} else if (msgName.equals("SyncWatchVideo")) {
				JSONObject jsbody = (JSONObject) body;
				int peerID = 0;
				int videoID = 0;
				if(jsbody.get("userID") instanceof Double){
					double temp = jsbody.getDouble("userID");
					peerID = (int) temp;
				}else if(jsbody.get("userID") instanceof Integer){
					peerID = jsbody.getInt("userID");
				}

				if(jsbody.get("videoID") instanceof Double){
					double temp = jsbody.getDouble("videoID");
					videoID = (int) temp;
				}else if(jsbody.get("videoID") instanceof Integer){
					videoID = jsbody.getInt("videoID");
				}
				addSyncVideo(jsbody);
				syncWatchVideo(peerID, videoID, true);
			}else if(msgName.equals("MainVideoId")){
				Object obj = (Object)body;
				int videoid = 0;
				if(obj instanceof Double){
					double temp = (Double)obj;
					videoid = (int) temp;
				}else if(obj instanceof Integer){
					videoid = (Integer) obj;
				}
				int peerid = associatedUserID;
				NotificationCenter.getInstance().postNotificationName(UI_NOTIFY_SWITCH_MAIN_VIDEO, peerid,videoid);
			} else if (msgName.equals("StartWebShare")) {
				if(m_nWebSharePeerID!=0){
					return;
				}
				m_nWebSharePeerID = associatedUserID;
				NotificationCenter.getInstance().postNotificationName(UI_NOTIFY_USER_START_WEBSHRAE);
			}else if (msgName.equals("ShowWebPage")) {
				String url = (String) body;
				NotificationCenter.getInstance().postNotificationName(UI_NOTIFY_USER_SHOW_WEBPAGE, url);
			}else if (msgName.equals("SpeakMode")) {
				// 不可以自由发言
				setM_speakFree(false);
				NotificationCenter.getInstance().postNotificationName(UI_NOTIFY_CHAIRMAN_TAKE_BACK_FREE_SPEAK_RIGHT);
			} else if (msgName.equals("ControlMode")) {
				// 不可以自由申请为主讲人
				this.setM_controlFree(false);
				delAllHost();
				NotificationCenter.getInstance().postNotificationName(UI_NOTIFY_CHAIRMAN_TAKE_BACK_HOST_RIGHT);
			} else if (msgName.equals("AllowRecord")) {
				// 不可以自由录制
				this.setM_allowRecord(false);
				NotificationCenter.getInstance().postNotificationName(UI_NOTIFY_CHAIRMAN_TAKE_BACK_FREE_RECORD);
			} else if (msgName.equals("DataCurSel")) {
				Object obj = (Object)body;
				int cursel = 0;
				if(obj instanceof Double){
					double temp = (Double)obj;
					cursel = (int) temp;
				}else if(obj instanceof Integer){
					cursel = (Integer) obj;
				}
				NotificationCenter.getInstance().postNotificationName(UI_NOTIFY_SHOW_TABPAGE, cursel);
			} else if (msgName.equals("LockRoom")) {
				setM_isLocked(true);
				NotificationCenter.getInstance().postNotificationName(UI_NOTIFY_USER_SYNC_LOOKROOM);
			} else if(msgName.equals("StartMediaShare")){
				if(moviePlayerId!=0){
					return;
				}
				moviePlayerId = fromID;
				moviestate = true;
				NotificationCenter.getInstance().postNotificationName(UI_NOTIFY_PLAY_MOVIE);
			} else if(msgName.equals("broadcasttype")){
				JSONObject js = (JSONObject) body;
				int type= js.getInt("type");
				Session.getInstance().setMeetingtype(type);
				NotificationCenter.getInstance().postNotificationName(DIRECT_MEETINGTYPE);
			} else if(msgName.equals("startbroadcast")){
				JSONObject js = (JSONObject) body;
				int status = js.getInt("status");
				NotificationCenter.getInstance().postNotificationName(START_BROADCAST,status,fromID);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 同步视频模式改变
	 *
	 * @param mode
	 * @param auto
	 */
	public void syncVideoModeChange(boolean mode, boolean auto) {
		_isSyncVideo = mode;
		_isAutoSyncVideo = auto;
		_syncVideoList.clear();

		NotificationCenter.getInstance().postNotificationName(
				FaceShareControl.UI_NOTIFY_USER_SYNC_VIDEO_MODE_CHANGE, _isSyncVideo,
				_isAutoSyncVideo);
	}

	/**
	 * 是否同步视频
	 *
	 * @return
	 */
	public boolean isSyncVideo() {
		return _isSyncVideo;
	}

	/**
	 * 查看同步视频
	 *
	 * @param peerID
	 * @param videoID
	 * @param open
	 */
	private void syncWatchVideo(int peerID, int videoID, boolean open) {
		if (!_isSyncVideo)
			return;
		MeetingUser mu = null;
		if (peerID == getMyPID())
			mu = Session.getInstance().getUserMgr().getSelfUser();
		else
			mu = Session.getInstance().getUserMgr().getMeetingUser(peerID);
		if (mu == null)
			return;

		if (open) {
			if (peerID == getMyPID()) {
				// m_watchMeCount = getUserMgr().getCount();
				publishVideo();
			}
		}
		// else
		// {
		// if( peerID == getMyPID() )
		// {
		// m_watchMeCount = 0;
		// unpublishVideo();
		// }
		// }
		NotificationCenter.getInstance().postNotificationName(
				UI_NOTIFY_USER_SYNC_WATCH_VIDEO, peerID, videoID, open);
	}

	/**
	 * 添加同步的视频
	 *
	 * @param obj
	 */
	private void addSyncVideo(JSONObject obj) {
		_syncVideoList.add(obj);
	}

	/**
	 * 移除同步的视频
	 *
	 * @param obj
	 * @throws JSONException
	 */
	private void delSyncVideo(JSONObject obj) throws JSONException {
		int peerID = 0;
		int videoID = 0;
		if(obj.get("userID") instanceof Double){
			double temp = obj.getDouble("userID");
			peerID = (int) temp;
		}else if(obj.get("userID") instanceof Integer){
			peerID = obj.getInt("userID");
		}

		if(obj.get("videoID") instanceof Double){
			double temp = obj.getDouble("videoID");
			videoID = (int) temp;
		}else if(obj.get("videoID") instanceof Integer){
			videoID = obj.getInt("videoID");
		}

		for (int i = 0; i < _syncVideoList.size(); i++) {
			JSONObject temp = _syncVideoList.get(i);
			if (peerID == temp.getInt("userID")
					&& videoID == temp.getInt("videoID")) {
				_syncVideoList.remove(i);
				break;
			}
		}
	}

	public int getChairManID() {
		return m_nChairmanID;
	}

	/**
	 * 删除远程消息
	 *
	 * @param msg
	 * @return
	 * @throws JSONException
	 */
	public void onRemoteDelMsg(String msgName, int fromID,
							   int associatedUserID, String id, String associatedMsgID,Object body){





		Log.i("emm", "Remote_DelMsg_i msg=" + msgName);
		if (msgName.equals("RequestControl")) {
			ControlStatusChange(associatedUserID, RequestHost_Disable, true);
			// NewFreeHost();
		} /*
		 * else if (msgName.equals("ChairmanMode")) { setMode(MeetingMode_Free);
		 * }
		 */else if (msgName.equals("StartAppShare")) {
			NotificationCenter.getInstance().postNotificationName(
					UI_NOTIFY_SHOW_SCREEN_PLAY, false);
		}   else if (msgName.equals("SyncVideoMode")) {
			syncVideoModeChange(false, false);
		} else if (msgName.equals("SyncWatchVideo")) {
			try {
				JSONObject jsbody = (JSONObject) body;
				int peerID = 0;
				int videoID = 0;
				if(jsbody.get("userID") instanceof Double){
					double temp = jsbody.getDouble("userID");
					peerID = (int) temp;
				}else if(jsbody.get("userID") instanceof Integer){
					peerID = jsbody.getInt("userID");
				}

				if(jsbody.get("videoID") instanceof Double){
					double temp = jsbody.getDouble("videoID");
					videoID = (int) temp;
				}else if(jsbody.get("videoID") instanceof Integer){
					videoID = jsbody.getInt("videoID");
				}
				delSyncVideo(jsbody);
				syncWatchVideo(peerID, videoID, false);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (msgName.equals("StartWebShare")) {
			m_nWebSharePeerID = 0;
			NotificationCenter.getInstance().postNotificationName(
					UI_NOTIFY_USER_STOP_WEBSHRAE);
		} else if (msgName.equals("SpeakMode")) {
			// 可以自由发言
			setM_speakFree(true);
			Session.getInstance().NewFreeSpeak();
			// final static public int
			// UI_NOTIFY_CHAIRMAN_ALLOW_FREE_SPEAK_RIGHT=55;//主席同意所有人自由发言权限
			NotificationCenter.getInstance().postNotificationName(
					UI_NOTIFY_CHAIRMAN_ALLOW_FREE_SPEAK_RIGHT);

		} else if (msgName.equals("ControlMode")) {
			// 可以自由申请为主讲人
			this.setM_controlFree(true);
			NewFreeHost();
			// final static public int
			// UI_NOTIFY_CHAIRMAN_ALLOW_FREE_HOST_RIGHT=54;//主席同意所有人的主讲权限
			NotificationCenter.getInstance().postNotificationName(
					UI_NOTIFY_CHAIRMAN_ALLOW_FREE_HOST_RIGHT);
		} else if (msgName.equals("AllowRecord")) {
			// 可以自由录制
			this.setM_allowRecord(true);
			// final static public int
			// UI_NOTIFY_CHAIRMAN_ALLOW_FREE_RECORD=56;//主席同意所有人的的录制权限
			NotificationCenter.getInstance().postNotificationName(
					UI_NOTIFY_CHAIRMAN_ALLOW_FREE_RECORD);
		} else if (msgName.equals("LockRoom")) {
			setM_isLocked(false);
			NotificationCenter.getInstance().postNotificationName(
					UI_NOTIFY_USER_LAYOUT_CHANGE);
		}else if(msgName.equals("StartMediaShare")){
			moviePlayerId = associatedUserID;
			moviestate = false;
			NotificationCenter.getInstance().postNotificationName(UI_NOTIFY_PLAY_MOVIE);
		}else if(msgName.equals("startbroadcast")){
			NotificationCenter.getInstance().postNotificationName(START_BROADCAST,0);
		}else if(msgName.equals("RequestSpeak")){
			Session.getInstance().ChangeAudioStatus(associatedUserID, Session.RequestSpeak_Disable);
			delSpeaker(associatedUserID);
			Session.getInstance().delPendingSpeaker(associatedUserID);
			Session.getInstance().NewFreeSpeak();
		}
	}

	/**
	 * 主控模式发生变化
	 *
	 * @param UserID
	 * @param Status
	 * @param bNotify
	 */
	private void ControlStatusChange(int UserID, int Status, boolean bNotify) {
		// RequestHost_Disable = 0;RequestHost_Allow = 1;RequestHost_Pending =
		// 2;
		MeetingUser mu = null;
		if (UserID == getMyPID())
			mu = Session.getInstance().getUserMgr().getSelfUser();
		else
			mu = Session.getInstance().getUserMgr().getMeetingUser(UserID);
		if (mu == null)
			return;

		/*
		 * int oldStatus = mu.getHostStatus(); switch (oldStatus) { case 1:
		 * break; case 2: { delPendingHost(UserID); break; } }
		 */

		switch (Status) {
			case 0:
				delHost(UserID);
				delPendingHost(UserID);
				break;
			case 1:
				addHost(UserID);
				break;
			case 2:
				addPendingHost(UserID);
				break;
		}

		mu.setHostStatus(Status);
		Session.getInstance().getUserMgr().reSort();
		if (bNotify) {
			if(UserID == getMyPID()){
				if(Session.getInstance().getUserMgr().getSelfUser().getHostStatus()==RequestHost_Allow){
					if(Session.getInstance().getPadInterface()!=null)
						Session.getInstance().getPadInterface().setControlMode(ControlMode.fullcontrol);
				}else{
					if(Session.getInstance().getPadInterface()!=null)
						Session.getInstance().getPadInterface().setControlMode(ControlMode.watch);
				}
			}
			NotificationCenter.getInstance().postNotificationName(
					UI_NOTIFY_USER_HOSTSTATUS, UserID, Status);

			if (RequestHost_Allow == Status) {
				if (this.getM_hostID() != UserID) {
					NotificationCenter.getInstance().postNotificationName(
							UI_NOTIFY_USER_HOST_CHANGE, UserID, Status);
					this.setM_hostID(UserID);
				}
			}
		}
	}

	/**
	 * 添加正在申请主讲的人，使成为主讲
	 *
	 * @param peerID
	 */
	private void addPendingHost(int peerID) {
		if (!mALPendingHostList.contains(peerID)) {
			mALPendingHostList.add(peerID);
		}
	}

	/**
	 * 删除正在申请主讲的人，
	 *
	 * @param peerID
	 */
	private void delPendingHost(int peerID) {
		if (mALPendingHostList.contains(peerID)) {
			mALPendingHostList.remove(mALPendingHostList.indexOf(peerID));
		}
	}

	private void addHost(int peerID) {
		if (!mALHostList.contains(peerID)) {
			mALHostList.add(peerID);
		}
	}

	/**
	 * 删除主讲
	 *
	 * @param peerID
	 */
	private void delHost(int peerID) {
		if (mALHostList.contains(peerID)) {
			mALHostList.remove(mALHostList.indexOf(peerID));
		}
	}

	/**
	 * 删除所有的主讲
	 */
	private void delAllHost() {
		for (int i = 0; i < mALHostList.size(); i++) {
			int hostid = mALHostList.get(i);
			if (hostid != this.getChairManID()) {
				ControlStatusChange(hostid, RequestHost_Disable, false);
				if (hostid == getMyPID()) {
					MeetingUser self = Session.getInstance().getUserMgr().getMeetingUser(hostid);
					if (self != null&&self.getHostStatus() == RequestHost_Allow) {
						Session.getInstance().DeleteMessage("RequestControl", Session.SENDMSGTOALL_EXCEPT_ME,
								hostid, null, "RequestControl" + hostid);
					}
				}
			}
		}
		mALHostList.clear();
		MeetingUser mu = Session.getInstance().getUserMgr().getMeetingUser(this.getChairManID());
		if (mu != null) {
			if (mu.getHostStatus() == RequestHost_Allow)
				addHost(this.getChairManID());
		}
	}

	//	/**
	//	 * 改变音频的状态
	//	 *
	//	 * @param UserID
	//	 * @param Status
	//	 */
	//	private void ChangeAudioStatus(int UserID, int Status) {
	//		MeetingUser mu = null;
	//		if (UserID == getMyPID())
	//			mu = Session.getInstance().getUserMgr().getSelfUser();
	//		else
	//			mu = Session.getInstance().getUserMgr().getMeetingUser(UserID);
	//		if (mu == null)
	//			return;
	//
	//		int oldStatus = mu.getAudioStatus();
	//		mu.setAudioStatus(Status);
	//		// getUserMgr().reSort();
	//
	//		if (UserID == getMyPID()) {
	//			if (oldStatus != RequestSpeak_Allow && Status == RequestSpeak_Allow) {
	//				Session.getInstance().PublishAudio();// 发布自己的音频给会议中的其他人
	//			} else if (oldStatus == RequestSpeak_Allow
	//					&& Status != RequestSpeak_Allow) {
	//				Session.getInstance().UnPublishAudio();
	//			}
	//		} else {
	//			if (oldStatus != RequestSpeak_Allow && Status == RequestSpeak_Allow) {
	//				Session.getInstance().playAudio(UserID);
	//			} else if (oldStatus == RequestSpeak_Allow
	//					&& Status != RequestSpeak_Allow) {
	//				Session.getInstance().unplayAudio(UserID);
	//			}
	//		}
	//		NotificationCenter.getInstance().postNotificationName(
	//				UI_NOTIFY_USER_AUDIO_CHANGE, UserID, Status);
	//	}



	// //////////////////////////////////////////////////////////////////
	/**
	 * 添加自由发言
	 *
	 * @param peerID
	 */
	private void addSpeaker(int peerID) {
		Session.getInstance().addSpeaker(peerID);
	}

	/**
	 * 删除自由发言
	 *
	 * @param peerID
	 */
	private void delSpeaker(int peerID) {
		Session.getInstance().delSpeaker(peerID);
	}

	/**
	 * 删除第一个自由发言
	 *
	 * @return
	 */
	private int delFirstSpeaker() {
		return Session.getInstance().delFirstSpeaker();
	}

	//	/**
	//	 * 同意正在申请的人自由发言
	//	 *
	//	 * @param peerID
	//	 */
	//	private void addPendingSpeaker(int peerID) {
	//		Session.getInstance().addPendingSpeaker(peerID);
	//	}
	//
	//	/**
	//	 * 不同意正在申请的人自由发言
	//	 *
	//	 * @param peerID
	//	 */
	//	private void delPendingSpeaker(int peerID) {
	//
	//	}

	//	/**
	//	 * 自由发言的数量
	//	 *
	//	 * @return
	//	 */
	//	private int getSpeakerCount() {
	//		return mALSpeakerList.size();
	//	}

	/*
	 * public boolean isFreeMode() { if (m_ChairmanMode == MeetingMode_Free)
	 * return true; return false;
	 *
	 * }
	 */
	/**
	 * 主席离开
	 */
	private void chairManLeave() {

		// m_ChairmanMode = mode;
		// if (m_ChairmanMode == MeetingMode_Free) {
		if(m_chairmanfunc.charAt(0) == '1'){
			setM_speakFree(true);
		}else{
			setM_speakFree(false);
		}

		if (m_chairmanfunc.charAt(1) == '1')
			setM_controlFree(true);
		else
			setM_controlFree(false);

		if (m_chairmanfunc.charAt(2) == '1')
			setM_allowRecord(true);
		else
			setM_allowRecord(false);

		if (this.isSpeakFree())
			Session.getInstance().NewFreeSpeak();
		if (this.isControlFree())
			NewFreeHost();
		if (_isSyncVideo&&m_chairmanfunc.charAt(4) == '0')
			syncVideoModeChange(false, false);
		// } else if (m_ChairmanMode == MeetingMode_ChairmanControl) {
		// }
		// NotificationCenter.getInstance().postNotificationName(
		// UI_NOTIFY_MEETING_MODECHANGE, mode);
	}

	public void setMaxSpeakerCount(int count) {
		m_maxSpeakerCount = count;
	}

	/**
	 * 自由发言的最大数量
	 *
	 * @return
	 */
	private int getMaxSpeakerCount() {
		return m_maxSpeakerCount;
	}

	//	/**
	//	 * 自由发言模式
	//	 */
	//	private void NewFreeSpeak() {
	//		// if (m_ChairmanMode == MeetingMode_Free) {
	//		if (this.isSpeakFree()) {
	//			while (mALSpeakerList.size() < m_maxSpeakerCount
	//					&& mALPendingSpeakerList.size() > 0) {
	//				int newSpeakerID = mALPendingSpeakerList.get(0);
	//				delPendingSpeaker(newSpeakerID);
	//				addSpeaker(newSpeakerID);
	//				Session.getInstance().ChangeAudioStatus(newSpeakerID, RequestSpeak_Allow);
	//			}
	//		}
	//	}

	/**
	 * 主控模式
	 */
	private void NewFreeHost() {

		// if (m_ChairmanMode == MeetingMode_Free) {
		if (this.isControlFree()) {
			// if ((int) mALPendingHostList.size() > 0 && m_hostID == 0) {
			if ((int) mALPendingHostList.size() > 0) {
				int newDataOperID = mALPendingHostList.get(0);
				delPendingHost(newDataOperID);
				ControlStatusChange(newDataOperID, RequestHost_Allow, true);
			}
		}
	}

	/**
	 * 焦点用户发生变化
	 *
	 * @param peerID
	 */
	public void onFocusUserChange(int peerID,int videoId) {
		NotificationCenter.getInstance()
				.postNotificationName(
						UI_NOTIFY_USER_FOCUS_CHANGED,
						peerID,videoId);
	}

	/**
	 * 服务器录制
	 *
	 * @param start
	 */
	public void onServerRecording(boolean start) {
		NotificationCenter.getInstance().postNotificationName(
				UI_NOTIFY_USER_SERVER_RECORDING,
				start);
	}

	/**
	 * 获取我的id
	 *
	 * @return
	 */
	public int getMyPID() {
		return Session.getInstance().getUserMgr().getSelfUser().getPeerID();
	}

	public void clear() {
		//			list.clear();
		disagreeCount = 0;
		WeiyiMeetingClient.instMid = "";
		agreeCount = 0;
		Session.getInstance().getList().clear();
		setViewer(false);
		//		MSG.clear();
		m_bWatchWish = true;
		Session.getInstance().getUserMgr().clear();
		//			padInterface.Clear();
		//		mALSpeakerList.clear();
		//		mALPendingSpeakerList.clear();
		mALWatchMe.clear();
		//		m_nWatchVideoIDs.clear();
		//		mapVideoIDs.clear();
		m_nChairmanID = 0;
		//		m_bServerRecording = false;
		m_hostID = 0;
		mALPendingHostList.clear();
		// getUserMgr().mMeetingUserSelf = null;
		_syncVideoList.clear();
		_isSyncVideo = false;
		_isAutoSyncVideo = false;
		// m_ChairmanMode = 0;
		m_maxSpeakerCount = 9;
		m_nChairmanID = 0;
		m_hostID = 0;
		m_bAutoVideoMode = false;
		m_bIsbigCameraShowSelf = true;
		sFileforphone = "";
		m_nScreenSharePeerID = 0;
		//		m_nFocusUserPeerID = 0;
		bHasFrontCamera = true;
		m_isLocked = false;
		linkUrl = "";
		linkName = "";
		Session.getInstance().getUserMgr().getSelfUser().clear();
		Session.getInstance().getUserMgr().clear();
		//		mapVideoIDs.clear();
		//		m_autoExitWeiyi = 0;
		//		m_sessionStatus = 0;
		//		m_bSpeakFree = 1;
		//		m_bAllowRecord = 0;
		//		m_bControlFree = 1;

		m_chairmancontrol = "11111110010011111010000010000";
		m_bSupportSensor = 1;
		// 0 is rotation,1 is heng ping
		m_bSupportRotation = 0;
		//		m_hideme = 0;
		m_currentCameraIndex = 0;
		m_isFrontCamera = true;
		moviestate = false;
		moviePlayerId = 0;
		videoForSipPeerId = -1;
		videoForSipVideoId = 0;
		isNeedShowExitDialog = true;
		//			super.clear();
	}

	/**
	 * 播放某个与会者共享的屏幕
	 *
	 * @param sur
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @param zorder
	 */
	public void playScreen(VideoView sur, float left, float top, float right,
						   float bottom, int zorder) {
		if (m_nScreenSharePeerID != 0)
			Session.getInstance().playScreen(m_nScreenSharePeerID,sur, left, top, right, bottom, zorder);
		//			RtmpClientMgr.getInstance().playScreen(m_nScreenSharePeerID, sur,
		//					left, top, right, bottom, zorder);
	}

	public void unplayScreen() {
		if (m_nScreenSharePeerID != 0){
			Session.getInstance().unplayScreen(m_nScreenSharePeerID);
			m_nScreenSharePeerID = 0;
		}
		//			RtmpClientMgr.getInstance().unplayScreen(m_nScreenSharePeerID);
	}

	/**
	 * 发布自己的视频给会议中的其他人
	 */
	public void publishVideo() {
		//		RtmpClientMgr.getInstance().publishVideo();
		Session.getInstance().publishVideo();
	}

	public void unpublishVideo() {
		//		RtmpClientMgr.getInstance().unpublishVideo();
		Session.getInstance().unpublishVideo();
	}

	/**
	 * 屏幕旋转
	 *
	 * @param rotate
	 */
	public void setRotate(int rotate) {
		//		RtmpClientMgr.getInstance().setOrientation(rotate);
		Session.getInstance().setRotate(rotate);
	}

	/**
	 * 修改名字
	 *
	 * @param strName
	 */
//	public void changeMyName(String strName) {
//
//		Session.getInstance().changeMyName(strName);
//		NotificationCenter.getInstance().postNotificationName(
//				UI_NOTIFY_USER_CHANGE_NAME);
//
//	}
	/**
	 * 修改名字
	 *
	 * @param strName
	 */
	public void changeUserName(String strName,int peerid) {

		Session.getInstance().changeUserName(strName,peerid);// 设置本与会者属性
		NotificationCenter.getInstance().postNotificationName(
				UI_NOTIFY_USER_CHANGE_NAME);
//		NotificationCenter.getInstance().postNotificationName(
//				Session.UI_NOTIFY_USER_CHANGE_NAME);

	}

	/**
	 * 切换自己的摄像头，会影响自己观看自己的视频和发送给别人视频
	 *
	 * @param bFront
	 */
	public void switchCamera(boolean bFront) {
		Session.getInstance().switchCamera(bFront);
	}

	/**
	 * 选择摄像头
	 */
	public void switchCamera() {
		int count = getCamerCount();
		if (count <= 0)
			return;

		int tempIndex = m_currentCameraIndex + 1;

		if (tempIndex >= count) {
			tempIndex = 0;
		}

		m_currentCameraIndex = tempIndex;


		Session.getInstance().switchCamera(m_currentCameraIndex);
		//		RtmpClientMgr.getInstance().changeCameraByIndex(m_currentCameraIndex);

	}

	/**
	 * 视频截图（前提是已经在观看某个与会者的视频或屏幕共享）
	 *
	 * @param peerID
	 *            该与会者的peerID，不能为0，不能为自己的peerID
	 * @param isVideo
	 *            为true时表示视频截图，为false时表示屏幕共享截图
	 * @return
	 */
	public Bitmap cutPicture(final int peerID, final boolean isVideo) {

		//		return RtmpClientMgr.getInstance().cutPicture(peerID, isVideo);
		return Session.getInstance().cutPicture(peerID, isVideo);
	}

	/**
	 * 发送文本消息
	 *
	 * @param toID
	 * @param text
	 * @param textFormat
	 */
	public void sendTextMessage(int toID, String text, JSONObject textFormat) {
		Session.getInstance().sendTextMessage(toID, text, textFormat);
	}

	/**
	 * 自己的视频状态发生变化
	 *
	 * @param bCanWatch
	 */
	public void setWatchMeWish(boolean bCanWatch) {
		m_bWatchWish = bCanWatch;

		if ((mALWatchMe.size() > 0 || Session.getInstance().getFocusUser() == getMyPID())
				&& m_bWatchWish) {
			this.publishVideo();
		} else if (!m_bWatchWish) {
			this.unpublishVideo();
			mALWatchMe.clear();
		}

		//		RtmpClientMgr.getInstance().setClientProperty("m_HasVideo",
		//				m_bWatchWish ? 1 : 0, SENDMSGTOALL_EXCEPT_ME);
		Session.getInstance().setWatchMeWish(m_bWatchWish);
		NotificationCenter.getInstance().postNotificationName(
				UI_NOTIFY_SELF_VEDIO_WISH_CHANGE);
	}

	public boolean getWatchMeWish() {
		return m_bWatchWish;
	}

	/**
	 * 播放某个与会者的视频
	 *
	 * @param nPeerID
	 * @param bPlay
	 * @param m_vCamearSelf
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @param zorder
	 * @param border
	 * @param scaletype
	 */
	public void PlayVideo(int nPeerID, boolean bPlay, VideoView m_vCamearSelf,
						  float left, float top, float right, float bottom, int zorder,
						  boolean border, int scaletype,int videoid) {

		if (is_isSyncVideo() && getChairManID() == getMyPID()) {
			if (nPeerID == 0 || nPeerID == this.getMyPID()) {
				if(bPlay)
					publishVideo();
				else
					unpublishVideo();

				boolean find = false;
				ArrayList<MyWatch> currentWatchVideos = Session.getInstance().getM_nWatchVideoIDs();
				for (int i = 0; i < currentWatchVideos.size(); i++) {
					if(currentWatchVideos.get(i).getPeerid()==this.getMyPID()
							&& currentWatchVideos.get(i).getCameraid() == videoid){
						find = true;
					}
				}

				if(bPlay && !find)
					syncUserVideo(this.getMyPID(), videoid, bPlay);
				else if (!bPlay &&find)
					syncUserVideo(this.getMyPID(), videoid, bPlay);
			}else{
				Integer hisid = nPeerID;
				boolean find = false;
				ArrayList<MyWatch> currentWatchVideos = Session.getInstance().getM_nWatchVideoIDs();
				for (int i = 0; i < currentWatchVideos.size(); i++) {
					if(currentWatchVideos.get(i).getPeerid()==hisid
							&& currentWatchVideos.get(i).getCameraid() == videoid){
						find = true;
					}
				}

				if(bPlay && !find)
					syncUserVideo(hisid, videoid, bPlay);
				else if (!bPlay &&find)
					syncUserVideo(hisid, videoid, bPlay);
			}
		}

		Session.getInstance().PlayVideo(nPeerID, bPlay, m_vCamearSelf, left, top, right, bottom, zorder, border, scaletype, videoid);
	}

	/**
	 * 设置扬声器
	 *
	 * @param loud
	 */
	public void setLoudSpeaker(final boolean loud) {
		m_isLouder = loud;
		Session.getInstance().setLoudSpeaker(loud);
	}

	/**
	 * 扬声器
	 *
	 * @return
	 */
	public boolean getLoudSpeaker() {
		return m_isLouder;
	}

	/**
	 * 设置视频质量
	 *
	 * @param highquality
	 */
	public void setCameraQuality(final boolean highquality) {
		Session.getInstance().setCameraQuality(highquality);
	}

	public void sendBroadcastType(int type) {
		Session.getInstance().sendBroadcastType(type);
	}

	/**
	 * 焦点用户
	 *
	 * @param peerID
	 */
	public void setFocusUser(final int peerID,final int videoId) {
		Session.getInstance().setFocusUser(peerID,videoId);
	}

	public int getFocusUser() {
		return Session.getInstance().getFocusUser();
	}
	public int getFocusUserVideoId(){
		return Session.getInstance().getFocusUserVideoId();
	}
	public boolean hasVideoForSip(){
		return Session.getInstance().hasVideoForSip();
	}

	/**
	 * 录制
	 *
	 * @param start
	 */
	public void serverRecording(final boolean start) {
		Session.getInstance().serverRecording(start);
	}

	public boolean getServerRecordingStatus() {
		return Session.getInstance().getServerRecordingStatus();
	}

	/**
	 * 主席改变
	 *
	 * @param userID
	 */
	public void changeChairMan(int userID) {
		Session.getInstance().PublishMessage("ChairmanChange", Session.SENDMSGTOALL, userID);
	}

	/**
	 * 同步视频
	 *
	 * @param mode
	 * @param bAutoSyncVideo
	 */
	public void syncVideo(boolean mode, boolean bAutoSyncVideo) {
		syncVideoModeChange(mode, bAutoSyncVideo);

		if (mode) {
			Session.getInstance().PublishMessage("SyncVideoMode", Session.SENDMSGTOALL_EXCEPT_ME, 0,
					bAutoSyncVideo, "SyncVideoMode", "ChairmanChange");
		} else {
			Session.getInstance().DeleteMessage("SyncVideoMode", Session.SENDMSGTOALL_EXCEPT_ME, 0, null,
					"SyncVideoMode", "ChairmanChange");
		}
	}

	/**
	 * 锁定会议室
	 *
	 * @param bLock
	 */
	public void lockRoom(boolean bLock) {
		m_isLocked = bLock;
		if (bLock)
			Session.getInstance().PublishMessage("LockRoom", Session.SENDMSGTOALL, 0, null, "LockRoom",
					"ChairmanChange");
		else
			Session.getInstance().DeleteMessage("LockRoom", Session.SENDMSGTOALL, 0, null, "LockRoom",
					"ChairmanChange");
	}

	/**
	 * 同步用户视频
	 *
	 * @param userID
	 * @param videoID
	 * @param set
	 */
	public void syncUserVideo(int userID, int videoID, boolean set) {
		String buf = "SyncWatchVideo" + userID + videoID;

		JSONObject videoInfo = new JSONObject();

		try {

			videoInfo.put("userID", userID);
			videoInfo.put("videoID", videoID);

			if (set) {
				addSyncVideo(videoInfo);

				Session.getInstance().PublishMessage("SyncWatchVideo", Session.SENDMSGTOALL_EXCEPT_ME,
						userID, videoInfo, buf, "SyncVideoMode");
			} else {
				delSyncVideo(videoInfo);

				Session.getInstance().DeleteMessage("SyncWatchVideo", Session.SENDMSGTOALL_EXCEPT_ME, userID,
						videoInfo, buf, "SyncVideoMode");
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 申请自由发言
	 *
	 * @param userID
	 */
	public void requestSpeaking(int userID) {
		Session.getInstance().requestSpeaking(userID);
	}

	/**
	 * 取消自由发言
	 *
	 * @param userID
	 */
	public void cancelSpeaking(int userID) {
		JSONObject body = new JSONObject();
		try {

			body.put("force", false);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Session.getInstance().DeleteMessage("RequestSpeak", Session.SENDMSGTOALL, userID, body,
				"RequestSpeak" + userID);
	}

	/**
	 * 显示标签
	 *
	 * @param curSel
	 */
	public void requestShowTab(int curSel) {
		int mypid = this.getMyPID();
		if (Session.getInstance().getUserMgr().getSelfUser().isChairMan()
				|| Session.getInstance().getUserMgr().getSelfUser().getHostStatus() == RequestHost_Allow) {
			FileLog.e("emm", "requestShowTab");
			Session.getInstance().PublishMessage("DataCurSel", Session.SENDMSGTOALL_EXCEPT_ME, mypid, curSel,
					"", "");
		}
	}

	/**
	 * 申请主讲
	 *
	 * @param userID
	 */
	public void requestHost(int userID) {
		Log.e("emm", "requestHost************");
		boolean force = Session.getInstance().getUserMgr().getSelfUser().isChairMan();
		Session.getInstance().PublishMessage("RequestControl", Session.SENDMSGTOALL, userID, force,
				"RequestControl" + userID, "");

	}

	/**
	 * 取消主讲
	 *
	 * @param userID
	 */
	public void cancelHost(int userID) {
		JSONObject body = new JSONObject();
		try {
			Log.e("emm", "cancelhost***********");
			body.put("force", false);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Session.getInstance().DeleteMessage("RequestControl", Session.SENDMSGTOALL, userID, body,
				"RequestControl" + userID);
	}

	/**
	 * 设置自由发言模式
	 *
	 * @param bFree
	 */
	public void setSpeakerMode(boolean bFree) {
		Log.e("emm", "setSpeake mode************");
		int userID = this.getChairManID();
		if (!bFree)
			Session.getInstance().PublishMessage("SpeakMode", Session.SENDMSGTOALL, 0, bFree, "SpeakMode",
					"ChairmanChange");
		else {
			JSONObject body = new JSONObject();
			try {
				Log.e("emm", "cancelhost***********");
				body.put("free", false);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Session.getInstance().DeleteMessage("SpeakMode", Session.SENDMSGTOALL, 0, body, "SpeakMode",
					"ChairmanChange");
		}
	}

	/**
	 * 主控模式·······
	 *
	 * @param bFree
	 */
	public void setControlMode(boolean bFree) {
		Log.e("emm", "setControlMode ************");
		int userID = this.getChairManID();
		if (!bFree)
			Session.getInstance().PublishMessage("ControlMode", Session.SENDMSGTOALL, 0, bFree,
					"ControlMode", "ChairmanChange");
		else {
			JSONObject body = new JSONObject();
			try {
				Log.e("emm", "cancelhost***********");
				body.put("free", false);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Session.getInstance().DeleteMessage("ControlMode", Session.SENDMSGTOALL, 0, body, "ControlMode",
					"ChairmanChange");
		}
	}

	/**
	 * 允许录制
	 *
	 * @param bAllow
	 */
	public void setAllowRecord(boolean bAllow) {
		Log.e("emm", "setAllowRecord ************");
		int userID = this.getChairManID();
		if (!bAllow)
			Session.getInstance().PublishMessage("AllowRecord", Session.SENDMSGTOALL, 0, bAllow,
					"AllowRecord", "ChairmanChange");
		else {
			JSONObject body = new JSONObject();
			try {
				Log.e("emm", "cancelhost***********");
				body.put("free", false);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Session.getInstance().DeleteMessage("AllowRecord", Session.SENDMSGTOALL, 0, body, "AllowRecord",
					"ChairmanChange");
		}
	}

	/**
	 * 请出会议
	 *
	 * @param userID
	 */
	public void kickUser(int userID) {
		MeetingUser user = Session.getInstance().getUserMgr().getMeetingUser(userID);
		try {
			JSONArray arr = new JSONArray();
			arr.put(0, null);
			arr.put(1, "ClientFunc_CustomFunc");
			arr.put(2, userID);
			arr.put(3, "ClientFunc_ChairmanKickOut");
			arr.put(4, 0);
			//			RtmpClientMgr.getInstance().callServerFunction(
			//					"ServerFunc_CallClientFunc", arr);
			Session.getInstance().callServerFunctions("ServerFunc_CallClientFunc", arr);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isM_instMeeting() {
		return Session.getInstance().isM_instMeeting();
	}

	public void setM_instMeeting(boolean m_instMeeting) {
		Session.getInstance().setM_instMeeting(m_instMeeting);
	}

	public boolean isM_isCaller() {
		return m_isCaller;
	}

	public void setM_isCaller(boolean m_isCaller) {
		this.m_isCaller = m_isCaller;
	}

	/**
	 * 参加会议的邀请的地址
	 *
	 * @param meetingID
	 * @param pwd
	 * @return
	 */
	public String getInviteAddress(String meetingID, String pwd) {

		String strUrl = Session.getInstance().MEETING_PHP_SERVER + "/" + meetingID + "/"
				+ (pwd == null ? "" : pwd);

		return strUrl;
	}




	/**
	 * 申请主席
	 */
	public void requestChairman(final String serial, final String strPassword) {

		Utitlties.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				if (isM_instMeeting()) {
					/*
					 * Utitlties.RunOnUIThread(new Runnable() {
					 *
					 * @Override public void run() {
					 * NotificationCenter.getInstance() .postNotificationName(
					 * REQUEST_CHAIRMAN, 1); } });
					 */

					Session.getInstance().getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							NotificationCenter.getInstance()
									.postNotificationName(REQUEST_CHAIRMAN, 0);
						}
					});

					return;
				}
				String url = Session.getInstance().webFun_requestchairman;

				RequestParams params = new RequestParams();
				JSONObject jo = new JSONObject();
				try {
					jo.put("meetingid", serial);
					jo.put("pwd", strPassword);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				params.put("param", jo.toString());
				Log.e("emm", "param=" + params);
				Log.e("emm", "url=" + url);
				client.post(url, params, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String content) {
						try {
							// String strCommPassword = "";
							//change qxm
							JSONObject jsobj = new JSONObject(content);
							final int result = jsobj.optInt("result");//result=0  表示申请主席成功   -1：表示失败
							//final int nRet = Integer.valueOf(content);

							// final String pwd = strCommPassword;
							/*
							 * Utitlties.RunOnUIThread(new Runnable() {
							 *
							 * @Override public void run() {
							 * NotificationCenter.getInstance()
							 * .postNotificationName( REQUEST_CHAIRMAN, nRet); }
							 * });
							 */
							Session.getInstance().getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									NotificationCenter.getInstance()
											.postNotificationName(
													REQUEST_CHAIRMAN, result);
								}
							});

						} catch (Exception e) {
							e.printStackTrace();
							/*
							 * Utitlties.RunOnUIThread(new Runnable() {
							 *
							 * @Override public void run() { // TODO
							 * Auto-generated method stub
							 * NotificationCenter.getInstance()
							 * .postNotificationName( REQUEST_CHAIRMAN, -1); }
							 * });
							 */

							Session.getInstance().getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									NotificationCenter.getInstance()
											.postNotificationName(
													REQUEST_CHAIRMAN, -1);
								}
							});

						}
					}

					@Override
					public void onFailure(Throwable error, String content) {
						/*
						 * Utitlties.RunOnUIThread(new Runnable() {
						 *
						 * @Override public void run() { // TODO Auto-generated
						 * method stub
						 * NotificationCenter.getInstance().postNotificationName
						 * ( REQUEST_CHAIRMAN, -1); } });
						 */

						Session.getInstance().getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								NotificationCenter.getInstance()
										.postNotificationName(REQUEST_CHAIRMAN,
												-1);
							}
						});

					}
				});
			}
		});

	}

	/**
	 * 设置链接的地址
	 *
	 * @return
	 */
	public static void setLinkUrl(String _linkUrl) {
		linkUrl = _linkUrl;
	}

	/**
	 * 设置链接的名字
	 *
	 * @return
	 */
	public static void setLinkName(String _linkName) {
		linkName = _linkName;
	}

	/**
	 * 获取链接的地址
	 *
	 * @return
	 */
	public static String getLinkUrl() {
		return linkUrl;
	}

	/**
	 * 获取链接的名字
	 *
	 * @return
	 */
	public static String getLinkName() {
		return linkName;
	}

	/**
	 * 获取聊天的id
	 *
	 * @return
	 */
	public int getM_chatid() {
		return m_chatid;
	}

	/**
	 * 设置聊天的id
	 *
	 * @param m_chatid
	 */
	public void setM_chatid(int m_chatid) {
		this.m_chatid = m_chatid;
	}

	/**
	 * 摄像头的个数
	 *
	 * @return
	 */
	public int getCamerCount() {
		return getCameraInfo().size();
	}

	public List<Integer> getCameraInfo() {
		return Session.getInstance().getCameraInfo();
	}

	/**
	 * 摄像头是前置还是后置
	 *
	 * @param i
	 * @return
	 */
	public boolean hasMoreCamera() {
		if (getCameraInfo().size() > 1)
			return true;
		return false;
		/*
		 * boolean hasFront = false; boolean hasBack = false; List<Integer>
		 * list= RtmpClientMgr.getInstance().getCameraInfo(); for(int
		 * i=0;i<list.size();i++) { int d =list.get(i); if(d ==1 ){//有前置摄像头
		 * hasFront = true; }else if(d == 0){//有后置摄像头 hasBack = true; } }
		 * if(hasFront && hasBack)//前后摄像头都有 return true; return false;
		 */
	}

	/**
	 * 是否有前置摄像头
	 *
	 * @return
	 */
	public boolean hasFrontCamera() {
		List<Integer> list = Session.getInstance().getCameraInfo();
		for (int i = 0; i < list.size(); i++) {
			int d = list.get(i);
			if (d == 1)
				return true;
		}
		return false;
	}

	/**
	 * 是否有后置摄像头
	 *
	 * @return
	 */
	public boolean hasBackCamera() {
		List<Integer> list = Session.getInstance().getCameraInfo();
		for (int i = 0; i < list.size(); i++) {
			int d = list.get(i);
			if (d == 0)
				return true;
		}
		return false;
	}

	/**
	 * 最多可以查看视频的路数
	 *
	 * @return
	 */
	public int getMaxWatchVideoCount() {
		// 目前最多看四路视频
		return 4;
	}

	//	public void getInvitUsers(final String serial,final CheckMeetingCallBack callback){
	//		Utitlties.stageQueue.postRunnable(new Runnable() {
	//			@Override
	//			public void run() {
	//				String url = Session.getInstance().webFun_getInvitUsers;
	//
	//				RequestParams params = new RequestParams();
	//				params.put("serial", serial);
	//				Log.d("emm", "param=" + params.toString());
	//				Log.e("emm", "getInvitUsers url=" + url);
	//
	//				client.post(url, params, new AsyncHttpResponseHandler() {
	//					@Override
	//					public void onSuccess(String content) {
	//						try {
	//							JSONObject jsobj = new JSONObject(content);
	//							if(jsobj.optInt("result")==0){
	//								JSONArray jsousers = jsobj.optJSONArray("meetinguser");
	//								if(jsousers!=null){
	//
	//									for (int i = 0; i < jsousers.length(); i++) {
	//										JSONObject userinfo = jsousers.optJSONObject(i);
	//										MeetingUser mu = new MeetingUser();
	//										mu.setPeerID(-userinfo.getInt("receiveid"));
	//										if (userinfo.has("firstname"))
	//											mu.setName(userinfo.getString("firstname"));
	//
	//										if (userinfo.has("receiveid")) {
	//											mu.setThirdID(userinfo.getInt("receiveid"));
	//										}
	//										Session.getInstance().getUserMgr().addUser(mu);
	//										Session.getInstance().getUserMgr().addOffLineId(mu.getThirdID());
	//									}
	//								}
	//							}
	//
	//
	//						} catch (JSONException e) {
	//							// TODO Auto-generated catch block
	//							e.printStackTrace();
	//						}
	//					}
	//
	//					@Override
	//					public void onFailure(Throwable error, String content) {
	//
	//						Utitlties.RunOnUIThread(new Runnable() {
	//
	//							@Override
	//							public void run() {
	//								// TODO Auto-generated method stub
	//								// NotificationCenter.getInstance().postNotificationName(CHECK_MEETING,
	//								// -1);
	//								Log.e("emm", "checkmeeting complete falied***");
	//								callback.onError(-1);
	//							}
	//						});
	//					}
	//				});
	//			}
	//		});
	//	}
	//发送直播数据
	public void startBroadCasting(String url,int peerid){
		Session.getInstance().setRotate(4);
		Session.getInstance().startBroadCasting(url, peerid);
	}
	//停止发送直播数据
	public void stopBroadCasting(int peerid){
		Session.getInstance().stopBroadCasting(peerid);
		Session.getInstance().setRotate(0);
	}

	public void setfocusVideo(int peerid,int videoid){
		Integer body = videoid;
		Session.getInstance().PublishMessage("MainVideoId", Session.SENDMSGTOALL_EXCEPT_ME, peerid,body,"SyncVideoMode","");
	}
	/**
	 * 播放电影
	 */
	public void playMovie(int nPeerID, boolean bPlay, VideoView playView,
						  float left, float top, float right, float bottom, int zorder,
						  boolean border){
		if(!bPlay){
			moviePlayerId = 0;
		}
		Session.getInstance().playMovie(nPeerID, bPlay, playView, left, top, right, bottom, zorder, border);

	}

	public void playBroadCasting(String path,VideoView view,int scaletype){
		//		RtmpClientMgr.getInstance().playBroadCasting(path, view, scaletype);
		Session.getInstance().playBroadCasting(path, view, scaletype);
	}
	public void unplayBroadCasting(){
		//		RtmpClientMgr.getInstance().unplayBroadCasting();
		Session.getInstance().unplayBroadCasting();
	}
	public boolean isLiveMeeting(){
		return Session.getInstance().isLiveMeeting();
	}
	public boolean isViewer()
	{
		return Session.getInstance().isViewer();
	}
	public void setViewer(boolean isViewer)
	{
		Session.getInstance().setViewer(isViewer);
	}
	public void sendHandupACK(int toID, boolean res) {
		try {
			JSONArray arr = new JSONArray();
			arr.put(0, null);
			arr.put(1, "ClientFunc_CustomFunc");
			arr.put(2, toID);
			arr.put(3, "ClientFunc_HandsUpACK");
			arr.put(4, res?1:0);

			//			RtmpClientMgr.getInstance().callServerFunction(
			//					"ServerFunc_CallClientFunc", arr);
			Session.getInstance().callServerFunctions("ServerFunc_CallClientFunc", arr);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	public void sendHandup() {
		try {
			JSONArray arr = new JSONArray();
			arr.put(0, null);
			arr.put(1, "ClientFunc_CustomFunc");
			arr.put(2, Session.SENDMSGTOALL);
			arr.put(3, "ClientFunc_HandsUp");//从第3位开始封装成jsonarry从0开始算

			//			RtmpClientMgr.getInstance().callServerFunction(
			//					"ServerFunc_CallClientFunc", arr);
			Session.getInstance().callServerFunctions("ServerFunc_CallClientFunc", arr);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	public void sendHandupStop() {
		try {
			JSONArray arr = new JSONArray();
			arr.put(0, null);
			arr.put(1, "ClientFunc_CustomFunc");
			arr.put(2, Session.SENDMSGTOALL);
			arr.put(3, "ClientFunc_StopHands");

			//			RtmpClientMgr.getInstance().callServerFunction(
			//					"ServerFunc_CallClientFunc", arr);
			Session.getInstance().callServerFunctions("ServerFunc_CallClientFunc", arr);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void getJSON(String url)
	{

		client.get(url, null, new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(String content) {
				try {
					Log.d("emm", "getJSON_onSuccess");
					JSONObject jsonobj = new JSONObject(content);
					if(jsonobj!=null&&jsonobj.has("ShowPage")){
						JSONArray msgArray = jsonobj.getJSONArray("ShowPage");
						if(msgArray.length() > 0 && msgArray.getJSONObject(0).has("body"))
						{
							JSONObject msg = msgArray.getJSONObject(0).getJSONObject("body");
							if(msg.has("pageID") && msg.getInt("pageID") != 0)
							{
								boolean m_hasChapter = true;
								m_pageList = new VodMsgList(msgArray, false);

							}
						}
					}
					if(jsonobj!=null && jsonobj.has("sharpsChange"))
					{
						JSONArray msgArray1 = jsonobj.getJSONArray("sharpsChange");
						if(msgArray1.length() > 0 && msgArray1.getJSONObject(0).has("msg"))
						{
							JSONObject msg1 = msgArray1.getJSONObject(0).getJSONObject("msg");
							if(msg1.has("body") && !msg1.getString("body").isEmpty())
							{
								m_shapeList = new VodMsgList(msgArray1, true);
							}
						}
					}
					NotificationCenter.getInstance().postNotificationName(LIVE_WHITEPAD_JSON_BACK, m_pageList,m_shapeList);

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			@Override
			public void onFailure(Throwable error) {
				error.printStackTrace();
			}
		});
	}
	/**
	 * 进入会议之后调用，在onconnect之后回调
	 */
	@Override
	public void onEnablePresence(int peerid) {
		try {
			//			Session.getInstance().setM_bInmeeting(true);
			int myPID = peerid;
			//			Session.getInstance().getUserMgr().getSelfUser().setPeerID(myPID);
			if (Session.getInstance().getUserMgr().getSelfUser().getRole() == 1)
				this.changeChairMan(myPID);
			// 设置是否隐藏自己,0是不隐藏自己，1是隐藏自己
			Session.getInstance().getUserMgr().getSelfUser().setHide(this.getM_hideme());
			Session.getInstance().getUserMgr().getSelfUser().setUserImg(getChatHeadColor());
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean isInst = Session.getInstance().isM_instMeeting();
		boolean isAuto = isM_bAutoVideoMode();
		if (isInst || isAuto) {
			// 自动发言，在会议中的人会自动播放我的音频,不需要写playaudio

			// 自动播放视频，需要满足的条件是：主席在非同步视频状态下,在camera_fragment响应NET_CONNECT_USER_IN这个消息中，自动watchvideo
			// 自动发言在出席成功后，别人会自动播放的
			// 自动发言在出席成功后,别人会自动播放我的音频
			// 自由发言模式才自动播放别人
			if (this.isSpeakFree())
				StartSpeaking();
		}
//		Utitlties.RunOnUIThread(new Runnable() {
//
//			@Override
//			public void run() {
//				getStream();
//
//			}
//		});
		NotificationCenter.getInstance().postNotificationName(
				NET_CONNECT_ENABLE_PRESENCE,
				Session.getInstance().getUserMgr().getSelfUser().getPeerID(), getM_chatid());

	}

	@Override
	public void onDisConnect(int code) {
		if (code != 0) {
			clear();
			Session.getInstance().getUserMgr().getSelfUser().clear();
			NotificationCenter.getInstance().postNotificationName(
					NET_CONNECT_BREAK,code);
			isEnterMeeting = false;
		} else {
			NotificationCenter.getInstance().postNotificationName(
					NET_CONNECT_LEAVE);
			if (this.isM_instMeeting())
				NotificationCenter.getInstance().postNotificationName(
						EXIT_MEETING);
			if (getM_autoExitWeiyi() == 1) {
				Log.e("emm", "auto exit meeting***************");
				NotificationCenter.getInstance().postNotificationName(
						AUTO_EXIT_MEETING);
			}
			clear();
		}

	}

	@Override
	public void onConnect(int arg0, int quality) {
		if (arg0 == 0) {
			isEnterMeeting = true;
			NotificationCenter.getInstance().postNotificationName(NET_CONNECT_SUCCESS, arg0);
		} else {
			Session.getInstance().setM_bInmeeting(false);
			//			m_sessionStatus = 0;
			onConnect(Session.getInstance().getActivity(), arg0);
			NotificationCenter.getInstance().postNotificationName(
					NET_CONNECT_FAILED);
			clear();
		}
	}


	// 其他用户属性变化
	// peerID：对方的id
	// proerty：发生变化的属性
	@Override
	public void onUserPropertyChange(int peerID,JSONObject arg0) {
		Log.e("tag","arg0qita============" + arg0.toString());
		try {
			int nPeerID = peerID;
			MeetingUser mu = Session.getInstance().getUserMgr().getUser(nPeerID);
			if (arg0.has("m_NickName")) {

				String strNickName = arg0.getString("m_NickName");
				Log.e("tag","strNickName============" + strNickName);
				if (!strNickName.isEmpty()) {
					if (mu != null && !strNickName.equals(mu.getName())) {
						mu.setName(strNickName);
						NotificationCenter.getInstance().postNotificationName(
								UI_NOTIFY_USER_CHANGE_NAME,
								nPeerID);
					}
				}
			}
			if (arg0.has("m_HasVideo")) {
				boolean bHasVideo = true;

				Object obj = arg0.get("m_HasVideo");
				if (obj instanceof Boolean) {
					bHasVideo = ((Boolean) obj).booleanValue();
				} else {
					bHasVideo = ((Double) obj).intValue() != 0;
				}

				if (mu != null && mu.ishasVideo() != bHasVideo) {
					mu.sethasVideo(bHasVideo);
					boolean bWhoIWatchClosed = false;
					if (!bHasVideo && mu.getWatch()) {
						mu.setWatch(false);
						bWhoIWatchClosed = true;
					}
					NotificationCenter.getInstance().postNotificationName(
							UI_NOTIFY_USER_VEDIO_CHANGE,
							nPeerID, bWhoIWatchClosed);
				}
			}
			//xiaoyang add
			if(arg0.has("m_MutliCamera")&&mu!=null){

				JSONArray jsonCamreas = arg0.optJSONArray("m_MutliCamera");
				for (int i = 0; i < jsonCamreas.length(); i++) {
					JSONObject jscam = jsonCamreas.optJSONObject(i);
					int cIndex = jscam.optInt("m_CameraIndex");
					Log.e("tag","cIndexqita============" + cIndex);
					String cName = jscam.optString("m_CameraName");
					Log.e("tag","cNameqita============" + cName);
					boolean cEnable = jscam.optBoolean("m_CameraEnable");
					Log.e("tag","cEnable============" + cEnable);
					boolean cDefault = jscam.optBoolean("m_DefaultCamera");
					Log.e("tag","cDefault============" + cDefault);
					mu.addCamera(cIndex, cName, cEnable, cDefault);
				}
			}

			NotificationCenter.getInstance().postNotificationName(UI_NOTIFY_USER_CAMERA_CHANGE);
			//xiaoyang add
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i("ClientFunc_","ClientFunc_ClientProperty==========="  + arg0.toString());

	}

	@Override
	public void onUserOut(MeetingUser mu) {
		//		MeetingUser mu = Session.getInstance().getUserMgr().getMeetingUser(arg0);
		if (mu == null)
			return;
		if (mu.getPeerID() == m_hostID) {
			m_hostID = 0;
		}
		//xiaoyang add 主讲共享影音之后离开会议室
		if(mu.getPeerID()==getMoviePlayerId()){
			moviestate = false;
			NotificationCenter.getInstance().postNotificationName(UI_NOTIFY_PLAY_MOVIE);
		}

		//		JSONObject varParamRS = new JSONObject(), varParamRC = new JSONObject();
		try {
			//			varParamRS.put("associateUserID", arg0);
			//			varParamRS.put("name", "RequestSpeak");
			//			varParamRC.put("associateUserID", arg0);
			//			varParamRC.put("name", "RequestControl");
			onRemoteDelMsg("RequestSpeak", mu.getPeerID(), 0, "", "", null);
			onRemoteDelMsg("RequestControl", mu.getPeerID(), 0, "", "", null);

		} catch (Exception e) {
			e.printStackTrace();
		}
		Session.getInstance().ClientFunc_UnWatchBuddyVideo(mu.getPeerID());

		if (mu.getClientType() == 4||mu.getClientType() == 7) {
			Session.getInstance().unplayAudio(mu.getPeerID());
		}
		if (mu.getPeerID() == m_nChairmanID) {
			m_nChairmanID = 0;

			if (m_chairmanfunc.isEmpty())
				m_chairmanfunc = "11100";

			JSONObject varParamSM = new JSONObject();
			if (!isSpeakFree() && m_chairmanfunc.charAt(0) == '1') {
				try {
					varParamSM.put("name", "SpeakMode");
					onRemotePubMsg("SpeakMode", mu.getPeerID(), 0, "", "", null);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else if (isSpeakFree() && m_chairmanfunc.charAt(0) == '0') {
				try {
					varParamSM.put("name", "SpeakMode");
					onRemotePubMsg("SpeakMode", mu.getPeerID(), 0, "", "", null);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			JSONObject varParamCM = new JSONObject();
			if (!isControlFree() && m_chairmanfunc.charAt(1) == '1') {
				try {
					varParamCM.put("name", "ControlMode");
					onRemotePubMsg("ControlMode", mu.getPeerID(), 0, "", "", null);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else if (isControlFree() && m_chairmanfunc.charAt(1) == '0') {
				try {
					varParamCM.put("name", "ControlMode");
					onRemotePubMsg("ControlMode", mu.getPeerID(), 0, "", "", null);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			JSONObject varParamAR = new JSONObject();
			if (!isAllowRecord() && m_chairmanfunc.charAt(2) == '1') {
				try {
					varParamAR.put("name", "AllowRecord");
					onRemotePubMsg("AllowRecord", mu.getPeerID(), 0, "", "", null);
				} catch (JSONException e) {
					e.printStackTrace();
				}

			} else if (isAllowRecord() && m_chairmanfunc.charAt(2) == '0') {
				try {
					varParamAR.put("name", "AllowRecord");
					onRemotePubMsg("AllowRecord", mu.getPeerID(), 0, "", "", null);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			JSONObject varParamSV = new JSONObject();
			if (is_isSyncVideo()&&m_chairmanfunc.charAt(4) == '0') {
				try {
					varParamSV.put("name", "SyncVideoMode");
					onRemoteDelMsg("SyncVideoMode", mu.getPeerID(), 0, "", "", varParamSV);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else if (!is_isSyncVideo()&&m_chairmanfunc.charAt(4) == '1') {
				try {
					varParamSV.put("name", "SyncVideoMode");
					varParamSV.put("body", true);
					onRemotePubMsg("SyncVideoMode", mu.getPeerID(), 0, "", "", varParamSV);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			JSONObject varParamLR = new JSONObject();
			if (isM_isLocked()) {
				try {
					varParamLR.put("name", "LockRoom");
					onRemoteDelMsg("LockRoom", mu.getPeerID(), 0, "", "", null);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
//			else if (!isM_isLocked()) {
//				try {
//					onRemotePubMsg("LockRoom", mu.getPeerID(), 0, "", "", null);
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//			}

			// this.setM_allowRecord(true);
			// this.setM_controlFree(true);
			// this.setM_speakFree(true);
			// if (m_ChairmanMode == MeetingMode_ChairmanControl)
			// {
			// setMode();
			// }
			chairManLeave();
		}
		if (m_nScreenSharePeerID == mu.getPeerID()) {
			NotificationCenter.getInstance().postNotificationName(
					UI_NOTIFY_SHOW_SCREEN_PLAY, false);
		}
		if (m_nWebSharePeerID == mu.getPeerID()) {
			m_nWebSharePeerID = 0;
			NotificationCenter.getInstance().postNotificationName(
					UI_NOTIFY_USER_STOP_WEBSHRAE);
		}
		boolean bWatchedLeave = false;
		if (mu.getWatch()) {
			bWatchedLeave = true;
		}
		//		Session.getInstance().getUserMgr().delUser(arg0);
		if(Session.getInstance().getUserMgr().getSelfUser().isChairMan()&&mu.getPeerID()==Session.getInstance().getVideoPeerIdForSip()){
			sendDefaultVideoToSip();
		}
		NotificationCenter.getInstance().postNotificationName(
				NET_CONNECT_USER_OUT, mu.getPeerID(), mu.getName(),
				bWatchedLeave);

	}

	@Override
	public void onUserIn(int peerID,boolean binList) {
		MeetingUser mu = Session.getInstance().getUserMgr().getUser(peerID);
		if(mu == null){
			return;
		}
		mu.setUserImg(getChatHeadColor());
		MeetingUser muself = Session.getInstance().getUserMgr().getSelfUser();
		if (mu.getThirdID() != 0 && mu.getThirdID() == muself.getThirdID()&&binList) {
			JSONArray arr = new JSONArray();
			try {
				arr.put(0, null);
				arr.put(1, "ClientFunc_CustomFunc");
				arr.put(2, mu.getPeerID());
				arr.put(3, "ClientFunc_ChairmanKickOut");
				arr.put(4, 1);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}//xiaoyang 5月18修改互踢回所有端互踢

			Session.getInstance().callServerFunctions(
					"ServerFunc_CallClientFunc", arr);
		}
		if(mu.getRole() == 1){
			m_nChairmanID = mu.getPeerID();
		}
		if(!binList){
			sendDefaultVideoToSip();
			NotificationCenter.getInstance()
					.postNotificationName(NET_CONNECT_USER_IN,
							peerID, binList);
		}
		NotificationCenter.getInstance().postNotificationName(
				NET_CONNECT_USER_INLIST_COMPLETE);
		//		if(Session.getInstance().getUserMgr().getCount()<1){
		//			NotificationCenter.getInstance().postNotificationName(
		//					Session.PRESENCE_COMPLETE);
		//		}

	}

	private static HashMap<String,String> m_protocolParsedResult = new HashMap<String, String>();

	static public WeiyiMeetingClient getInstance()
	{
		synchronized (sync) {
			if (mInstance == null) {
				mInstance = new WeiyiMeetingClient();
			}
		}
		return mInstance;
	}
	public static void init(Context appcont, Handler apphandler){
		WeiyiClient.init(appcont, apphandler);
		Utitlties.init(appcont, apphandler);
		FileLog.init(appcont, apphandler);
		UZResourcesIDFinder.init(appcont.getApplicationContext());
		WeiyiMeetingClient.getInstance().Init(appcont, "weiyi20!%","A95F65A9FC8185F2", false);
		NotificationCenter.getInstance().removeObserver(WeiyiMeetingClient.getInstance());

		NotificationCenter.getInstance().addObserver(WeiyiMeetingClient.getInstance(), WeiyiMeetingClient.CHECK_MEETING);
		NotificationCenter.getInstance().addObserver(WeiyiMeetingClient.getInstance(), WeiyiMeetingClient.EXIT_MEETING);
		NotificationCenter.getInstance().addObserver(WeiyiMeetingClient.getInstance(),WeiyiMeetingClient.PRESENCE_COMPLETE);
		NotificationCenter.getInstance().addObserver(WeiyiMeetingClient.getInstance(),WeiyiMeetingClient.NET_CONNECT_USER_IN);
		NotificationCenter.getInstance().addObserver(WeiyiMeetingClient.getInstance(),WeiyiMeetingClient.NET_CONNECT_USER_OUT);
		headImg = new int[]{UZResourcesIDFinder.getResDrawableID("head_img1"),UZResourcesIDFinder.getResDrawableID("head_img2"),UZResourcesIDFinder.getResDrawableID("head_img3"),UZResourcesIDFinder.getResDrawableID("head_img4"),
				UZResourcesIDFinder.getResDrawableID("head_img5"),UZResourcesIDFinder.getResDrawableID("head_img6"),UZResourcesIDFinder.getResDrawableID("head_img7"),UZResourcesIDFinder.getResDrawableID("head_img8")};
	}
	public static void setWebPageProtocol(String protocolName)
	{
		m_protocolName = protocolName+"://";
	}

	public static void setInviteAddress(String inviteAddress)
	{
		m_inviteAddress = inviteAddress;
	}
	public static Context getApplicationContext()
	{
		return WeiyiClient.getApplicationContext();
	}
	public static Handler getApplicationHandler()
	{
		return WeiyiClient.getApplicationHandler();
	}
	private static void startMeeting(final Activity activiy,final String httpServer,final String mid,final String name,final String pwd,final boolean isInstMeeting,int thirduid,int chatid,int bAutoExitWeiyi,int bHasSensor,int bScreenRotation,int hideme,int meetingtype,String clientIdf,String title,int createid,int userid)
	{
		Intent intents = new Intent();
		intents.setClass(activiy,FaceMeeting_Activity.class);
		intents.putExtra("httpserver", httpServer);
		intents.putExtra("meetingid", mid);
		intents.putExtra("username", name);
		intents.putExtra("isInstMeeting", isInstMeeting);
		intents.putExtra("password", pwd);

		intents.putExtra("thirduid",thirduid);
		intents.putExtra("chatid",chatid);
		intents.putExtra("bAutoExitWeiyi",bAutoExitWeiyi);
		intents.putExtra("bSupportSensor",bHasSensor);
		intents.putExtra("bSupportRotation",bScreenRotation);
		intents.putExtra("inviteAddress",m_inviteAddress);
		intents.putExtra("hideme",hideme);
		intents.putExtra("meetingtype", meetingtype);
		intents.putExtra("clientidentIfication", clientIdf);
		intents.putExtra("title", title);
		intents.putExtra("createrid", createid);
		intents.putExtra("userid", userid);


		Log.e("emm", "auto exit meeting 1***************"+bAutoExitWeiyi);
		if(bAutoExitWeiyi==1)
		{
			Log.e("emm", "auto exit meeting 2***************"+bAutoExitWeiyi);
			NotificationCenter.getInstance().addObserver(mInstance, AUTO_EXIT_MEETING);
		}

		Log.e("emm", "httpserver="+httpServer+" meetingid="+mid+" username="+name);
		activiy.startActivity(intents);
	}

	private static void startBroadcast(final Activity activiy,final String httpServer,final String mid,final String name,final String pwd,final boolean isInstMeeting,int thirduid,int chatid,int bAutoExitWeiyi,int bHasSensor,int bScreenRotation,int hideme,int meetingtype,String clientIdf,String title,int createid,int userid)
	{
		Intent intents = new Intent();
		intents.setClass(activiy,Broadcast_Activity.class);
		intents.putExtra("httpserver", httpServer);
		intents.putExtra("meetingid", mid);
		intents.putExtra("username", name);
		intents.putExtra("isInstMeeting", isInstMeeting);
		intents.putExtra("password", pwd);

		intents.putExtra("thirduid",thirduid);
		intents.putExtra("chatid",chatid);
		intents.putExtra("bAutoExitWeiyi",bAutoExitWeiyi);
		intents.putExtra("bSupportSensor",bHasSensor);
		intents.putExtra("bSupportRotation",bScreenRotation);
		intents.putExtra("inviteAddress",m_inviteAddress);
		intents.putExtra("hideme",hideme);
		intents.putExtra("meetingtype", meetingtype);
		intents.putExtra("clientidentIfication", clientIdf);
		intents.putExtra("title", title);
		intents.putExtra("createrid", createid);
		intents.putExtra("userid", userid);

		Log.e("emm", "auto exit meeting 1***************"+bAutoExitWeiyi);
		if(bAutoExitWeiyi==1)
		{
			Log.e("emm", "auto exit meeting 2***************"+bAutoExitWeiyi);
			NotificationCenter.getInstance().addObserver(mInstance, AUTO_EXIT_MEETING);
		}

		Log.e("emm", "httpserver="+httpServer+" meetingid="+mid+" username="+name);
		activiy.startActivity(intents);
	}
	public static void joinMeeting(final Activity activity,final String protocolUrl,MeetingNotify notify)
	{
		//weiyi://start?ip=weiyicloud.com&port=80&meetingid=xxxxx
		//xxx&meetingtype=xxx&title="test"
		isbroadcast = false;
		Session.getInstance().setActivity(activity);
		innerEnterMeeting(activity,protocolUrl,"","",notify);
	}




	public static void joinBroadcast(final Activity activiy,final String url)
	{
		isbroadcast = true;
		Session.getInstance().setActivity(activiy);
		innerEnterMeeting(activiy, url, "", "",null);
	}

	public static void joinMeetingByUrl(final Activity activity,final Intent intent,final String name)
	{
		//		isbroadcast = false;
		m_nickName = name;
		Session.getInstance().setActivity(activity);
		String url = "";
		if(intent.getData() != null)
		{
			Log.e("emm", "joinmeetingbyurl on webpage*******************");
			Uri uri = intent.getData();
			if (uri != null) {
				url = uri.toString();
				Log.e("emm", "joinmeetingbyurl by url="+url);
				String inUrl = new String(url);
				reJoinMeeting(activity,inUrl,name,false);
				return;
			}
		} else {
			Log.i("rebuild", "4");
			Log.e("emm", "joinmeetingbyurl on taskbar*******************");
			Bundle bd = intent.getExtras();
			if (bd != null) {
				if (bd.containsKey(WeiyiMeetingClient.FROM_TITLE)) {
					url = bd.getString(WeiyiMeetingClient.FROM_TITLE);
					reJoinMeeting(activity,url,name,true);
					return;
				}
			}
		}
		Log.e("emm", "joinmeetingbyurl on desktop 1*******************");
		if(isInMeeting())
		{
			Log.e("emm", "joinmeetingbyurl on desktop in meeting*******************");
			restoreMeeting(activity);
		}
	}
	public static boolean isInMeeting()
	{
		return Session.getInstance().isM_bInmeeting();
	}
	public static void forceExitMeeting()
	{
		bForceExitMeeting = true;
		NotificationCenter.getInstance().addObserver(WeiyiMeetingClient.getInstance(),NET_CONNECT_LEAVE);
		Session.getInstance().LeaveMeeting();
		m_protocolParsedResult.clear();
		m_notify=null;

	}
	public void exitMeeting()
	{
		if(!isEnterMeeting){
			NotificationCenter.getInstance().postNotificationName(NET_CONNECT_LEAVE);
		}
		if(m_notify!=null)
		{
			ArrayList<Integer> uids = Session.getInstance().getUserMgr().getThirdUids();
			//int bAutoExitWeiyi = MeetingSession.getInstance().getM_autoExitWeiyi();
			m_notify.onExitMeeting(getM_chatid(),uids,getM_strMeetingID());
			m_notify=null;
		}
		NotificationCenter.getInstance().addObserver(WeiyiMeetingClient.getInstance(),NET_CONNECT_LEAVE);
		Session.getInstance().LeaveMeeting();
		m_protocolParsedResult.clear();
		m_notify=null;
		clear();
	}

	public void setMeetingNotifyIntent(Intent iMeetingNotifyIntent) {
		WeiyiClient.getInstance().setMeetingNotifyIntent(iMeetingNotifyIntent);
	}
	public static void joinInstMeeting(final Activity activiy,final String httpServer,final String instMid,final String name,final int thirduid,final int chatid,final MeetingNotify notify,final PersistentCookieStore cookie)
	{
		//		if(isNum(instMid))
		//		{

		isbroadcast = false;
		WeiyiMeetingClient.getInstance().setM_chatid(chatid);

		WeiyiMeetingClient.instMid = instMid;
		mid = instMid;
		m_nickName = name;
		webServer = httpServer;
		m_notify = notify;
		thirdID = thirduid;
		Session.getInstance().setActivity(activiy);
		String strport = "";
		String strip = "";
		if(cookie != null){

			if(httpServer.contains(":")){
				if(httpServer.lastIndexOf(":") == 4){
					strip = httpServer.substring(httpServer.indexOf("/")+2);
					strport = "80";
				}else{
					strport = httpServer.substring(httpServer.lastIndexOf(":")+1);
					strip = httpServer.substring(httpServer.indexOf("/")+2, httpServer.lastIndexOf(":"));
				}
			}
		}else{
			strport = httpServer.substring(httpServer.lastIndexOf(":")+1);
			strip = httpServer.substring(httpServer.indexOf("/")+1, httpServer.lastIndexOf(":"));
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("cookie", cookie);
		map.put("isWeiYiVirsion", true);
		Session.getInstance().joinmeeting(strip, Integer.parseInt(strport), name, instMid, "", thirduid, 0,map);
		//			startMeeting(activiy,httpServer,instMid,name,"",true,thirduid,chatid,0,1,0,0,0,"","",0,0);
		//		}
		//		MeetingSession.getInstance().joinmeeting(httpServer, webserverport, name, meetingid, meetingpwd, thirduid, usertype);

	}

	public static void restoreMeeting(final Activity activiy)
	{
		String httpserver = Session.getInstance().getWebHttpServerAddress();
		String mid = WeiyiMeetingClient.getInstance().getM_strMeetingID();
		String name = WeiyiMeetingClient.getInstance().getM_strUserName();
		String pwd = WeiyiMeetingClient.getInstance().getM_pwd();
		boolean isInstMeeting = WeiyiMeetingClient.getInstance().isM_instMeeting();
		int chatid = WeiyiMeetingClient.getInstance().getM_chatid();
		int thriduid = Session.getInstance().getUserMgr().getSelfUser().getThirdID();
		int bAutoExitWeiyi = WeiyiMeetingClient.getInstance().getM_autoExitWeiyi();
		int bSupportSensor = WeiyiMeetingClient.getInstance().getM_bSupportSensor();
		int bSupportRotation = WeiyiMeetingClient.getInstance().getM_bSupportRotation();
		int bHideme = WeiyiMeetingClient.getInstance().getM_hideme();
		int meetingtype = Session.getInstance().getMeetingtype();
		if(Session.getInstance().isLiveMeeting()&&Session.getInstance().isViewer()){
			startBroadcast(activiy,httpserver,mid,name,pwd,isInstMeeting,thriduid,chatid,bAutoExitWeiyi,bSupportSensor,bSupportRotation,bHideme,meetingtype,"","",0,0);
		}else{
			startMeeting(activiy,httpserver,mid,name,pwd,isInstMeeting,thriduid,chatid,bAutoExitWeiyi,bSupportSensor,bSupportRotation,bHideme,meetingtype,"","",0,0);
		}
	}

	private static void reJoinMeeting(final Activity activity,String protocolin,final String name,final boolean from_taskbar )
	{
		if(from_taskbar)
		{
			Log.e("emm", protocolin);
			if (isInMeeting())
			{
				restoreMeeting(activity);
				Log.e("emm", "restoreMeeting="+protocolin);
			}
			//			else{
			//				enterMeeting(activity,protocol,name);
			//				Log.e("emm", "enterMeeting="+protocol);
			//			}
		}
		else
		{
			Log.e("emm", "click url join  meeting");
			if (isInMeeting())
			{
				Log.e("emm", "compare meetingid");
				String currentMid = WeiyiMeetingClient.getInstance().getM_strMeetingID();
				String mid = getMidByUrl(gbprotocol);
				if(currentMid.compareTo(mid) == 0)
				{
					Log.e("emm", "has same meetingid");
					restoreMeeting(activity);
					return;
				}
				final String finprotocol = gbprotocol;
				Log.e("emm", "already in meeting and tip decided to enter diff meeting room");
				AlertDialog.Builder build = new AlertDialog.Builder(activity);
				build.setTitle(getApplicationContext().getString(UZResourcesIDFinder.getResStringID("app_name")));
				build.setMessage(getApplicationContext().getString(UZResourcesIDFinder.getResStringID("already_in_meeting")));
				build.setPositiveButton(
						getApplicationContext().getString(UZResourcesIDFinder.getResStringID("back_to_meeting")),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0,
												int arg1) {
								Log.e("emm", "decided to enter preview meeting room");
								restoreMeeting(activity);
							}

						});
				build.setNegativeButton(getApplicationContext().getString(UZResourcesIDFinder.getResStringID("quit_meeting")),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0,
												int arg1) {
								NotificationCenter.getInstance().addObserver(mInstance, NET_CONNECT_LEAVE);
								m_activity = activity;
								m_protocol = finprotocol;
								m_nickName = name;
								Log.e("emm", "decided to enter diff meeting room,first exit priview meeting room");
								bRjoinMeeting = true;
								WeiyiMeetingClient.getInstance().exitMeeting();
							}
						});
				build.setOnCancelListener(new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface arg0) {

					}
				});
				AlertDialog dlg = build.show();
				dlg.setCanceledOnTouchOutside(false);
			} else {

				Log.e("emm", "no meeting and enter meeting room and protocol="+protocolin+"name="+name);
				enterMeeting(activity,protocolin,name);
				return;
			}
		}
	}

	private static String getMidByUrl(String protocol)
	{
		String strProtocol = protocol;
		String mid="";
		String pwd="";
		String httpServer="";
		if (strProtocol.isEmpty())
			return "";

		Uri url = Uri.parse(strProtocol);
		if (url != null) {
			// weiyi://192.168.0.121:81/123456789/4321
			if (strProtocol.startsWith(m_protocolName))
			{
				httpServer = strProtocol.replace(m_protocolName, "");
				if(httpServer.startsWith("start?"))
				{
					mid = url.getQueryParameter("meetingid");
				}
				else
				{
					httpServer = httpServer.substring(0,httpServer.indexOf("/"));
					String UrlPath = url.getPath();
					String[] ss = UrlPath.split("/");
					if (ss.length >= 2) {
						mid = ss[1];
					}
					if (ss.length >= 3) {
						pwd = ss[2];
					}
					if (ss.length > 9) {
						try {
							WeiyiMeetingClient.setLinkUrl(URLDecoder.decode(ss[9], "UTF-8"));
							WeiyiMeetingClient.setLinkName(ss[8]);
						} catch (Exception e) {
						}
					}
				}

				Log.e("emm", "httpUri==" + httpServer);
			}


		}
		return mid;
	}
	private static int m_ThirdUserID = -1;
	private static int m_isInstMeeting = -1;
	private static int m_isQuitSoftware = -1;
	private static int m_isEnableProximitySensor = -1;
	private static int m_screenRotation = -1;
	private static int m_isHideMe = -1;
	private static int m_userType = -1;
	private static String m_chairmanControl = "";

	/**
	 * 设置会议名称
	 * @param meetingName
	 */
	public static void setMeetingName(String meetingName)
	{
		WeiyiMeetingClient.getInstance().setM_strMeetingName(meetingName);
	}

	/**
	 * 设置用户类型，默认为0。只有会议没有密码时此属性才会生效，否则以服务器获取的用户类型为准
	 * <p>通过此接口设置的属性优先级高于调用enterMeeting时传入协议的优先级
	 * @param userType  0：普通用户；1：主席；2：直播用户
	 */
	public static void setUserType(int userType)
	{
		m_userType = userType;
	}

	/**
	 * 设置用户ID
	 * <p>通过此接口设置的属性优先级高于调用enterMeeting时传入协议的优先级
	 * <p><b>注：<i>请在调用enterMeeting接口前调用此接口</i></b>
	 * @param thirdUserID
	 */
	public static void setThirdUserID(int thirdUserID)
	{
		m_ThirdUserID = thirdUserID;
	}

	/**
	 * 设置会议是否是即时会议，默认为false
	 * <p>通过此接口设置的属性优先级高于调用enterMeeting时传入协议的优先级
	 * <p><b>注：<i>请在调用enterMeeting接口前调用此接口</i></b>
	 * @param isInstMeeting  true：即时会议；false：非即时会议
	 */
	public static void setIsInstMeeting(boolean isInstMeeting)
	{
		m_isInstMeeting = (isInstMeeting==true?1:0);
	}


	/**
	 * 设置退出会议后是否退出软件，默认为false
	 * <p>通过此接口设置的属性优先级高于调用enterMeeting时传入协议的优先级
	 * <p><b>注：<i>请在调用enterMeeting接口前调用此接口</i></b>
	 * @param isQuitSoftware  true：退出软件；false：不退出软件
	 */
	public static void setIsQuitSoftware(boolean isQuitSoftware)
	{
		m_isQuitSoftware = (isQuitSoftware==true?1:0);
	}

	/**
	 * 设置是否禁用近距离传感器，启用近距离传感器后，当遮挡传感器时屏幕熄灭，不遮挡时屏幕亮起，默认为false
	 * <p>通过此接口设置的属性优先级高于调用enterMeeting时传入协议的优先级
	 * <p><b>注：<i>请在调用enterMeeting接口前调用此接口</i></b>
	 * @param isEnableProximitySensor  true：禁用近距离传感器；false：不禁用近距离传感器
	 */
	public static void setIsDisableProximitySensor(boolean isDisableProximitySensor)
	{
		m_isEnableProximitySensor = (isDisableProximitySensor==true?0:1);
	}

	/**
	 * 设置禁用屏幕旋转，默认为false
	 * <p>通过此接口设置的属性优先级高于调用enterMeeting时传入协议的优先级
	 * <p><b>注：<i>请在调用enterMeeting接口前调用此接口</i></b>
	 * @param screenRotaticon  false：不禁用；true：禁用
	 */
	public static void setIsDisableScreenRotation(boolean isDisableScreenRotaticon)
	{
		m_screenRotation = (isDisableScreenRotaticon==true?1:0);
	}

	/**
	 * 设置是否隐身，隐身用户进入会议后不会出现在其他参会用户的用户列表中，默认为false
	 * <p>通过此接口设置的属性优先级高于调用enterMeeting时传入协议的优先级
	 * <p><b>注：<i>请在调用enterMeeting接口前调用此接口</i></b>
	 * @param isHideMe  true：隐身；false：不隐身
	 */
	public static void setIsHideMe(boolean isHideMe)
	{
		m_isHideMe = (isHideMe==true?1:0);
	}
	//
	//	/**
	//	 * 设置会议相关属性，默认值从服务器读取
	//	 * <p>通过此接口设置的属性优先级高于调用enterMeeting时传入协议的优先级
	//	 * <p><b>注：<i>请在调用enterMeeting接口前调用此接口</i></b>
	//	 * @param chairmanControl  会议属性
	//	 */
	//	public static void setChairmanControl(String chairmanControl)
	//	{
	//		m_chairmanControl = chairmanControl;
	//	}

	/**
	 * 设置主席功能
	 * @param chairmanFunc  主席功能
	 */
	//	public static void setChairmanFunc(String chairmanFunc)
	//	{
	//		m_chairmanFunc = chairmanFunc;
	//	}

	//	public static void setSpeakFree(boolean isSpeakFree)
	//	{
	//		MeetingSession.getInstance().setM_speakFree(isSpeakFree);
	//	}
	//
	//	public static void setRecordFree(boolean isRecordFree)
	//	{
	//		MeetingSession.getInstance().setM_allowRecord(isRecordFree);
	//	}
	//
	//	public static void setControlFree(boolean isControlFree)
	//	{
	//		MeetingSession.getInstance().setM_controlFree(isControlFree);
	//	}

	/**
	 * 设置是否显示“邀请”按钮，默认值从服务器读取
	 * <p>通过此接口设置的属性优先级高于从服务器读取的信息的优先级
	 * @param bShow  true：显示；false：不显示
	 */
	public void setShowInvitation(boolean bShow)
	{
		m_showInvitation = (bShow == true ? 1 : 0);
	}

	/**
	 * 设置是否显示白板，默认值从服务器读取
	 * <p>通过此接口设置的属性优先级高于从服务器读取的信息的优先级
	 * <p><b>注：<i>请在调用enterMeeting接口前调用此接口</i></b>
	 * @param bShow  true：显示；false：不显示
	 */
	public void setShowWhiteBoard(boolean bShow)
	{
		m_showWhiteBoard = (bShow == true ? 1 : 0);
	}

	/**
	 * 设置是否显示“消息”按钮及聊天页面，默认值从服务器读取
	 * <p>通过此接口设置的属性优先级高于从服务器读取的信息的优先级
	 * <p><b>注：<i>请在调用enterMeeting接口前调用此接口</i></b>
	 * @param bShow  true：显示；false：不显示
	 */
	public void setShowChatList(boolean bShow)
	{
		m_showChatList = (bShow == true ? 1 : 0);
	}

	/**
	 * 设置是否显示“参与者”按钮及参与者页面，默认值从服务器读取
	 * <p>通过此接口设置的属性优先级高于从服务器读取的信息的优先级
	 * <p><b>注：<i>请在调用enterMeeting接口前调用此接口</i></b>
	 * @param bShow  true：显示；false：不显示
	 */
	public void setShowUserList(boolean bShow)
	{
		m_showUserList = (bShow == true ? 1 : 0);
	}

	/**
	 * 设置是否显示“资料”按钮及文档列表页面，默认值从服务器读取
	 * <p>通过此接口设置的属性优先级高于从服务器读取的信息的优先级
	 * <p><b>注：<i>请在调用enterMeeting接口前调用此接口</i></b>
	 * @param bShow  true：显示；false：不显示
	 */
	public void setShowDocList(boolean bShow)
	{
		m_showDocList = (bShow == true ? 1 : 0);
	}

	/**
	 * 设置是否显示“申请主席”按钮，默认值从服务器读取
	 * <p>通过此接口设置的属性优先级高于从服务器读取的信息的优先级
	 * <p><b>注：<i>请在调用enterMeeting接口前调用此接口</i></b>
	 * @param bShow  true：显示；false：不显示
	 */
	public void setShowApplyChairman(boolean bShow)
	{
		m_showApplyChairman = (bShow == true ? 1 : 0);
	}

	/**
	 * 设置是否显示“申请主讲”按钮，默认值从服务器读取
	 * <p>通过此接口设置的属性优先级高于从服务器读取的信息的优先级
	 * <p><b>注：<i>请在调用enterMeeting接口前调用此接口</i></b>
	 * @param bShow  true：显示；false：不显示
	 */
	public void setShowApplyHost(boolean bShow)
	{
		m_showApplyHost = (bShow == true ? 1 : 0);
	}

	/**
	 * 设置用户(非自己)进入会议后是否打开音视频，默认值从服务器读取
	 * <p>通过此接口设置的属性优先级高于从服务器读取的信息的优先级
	 * <p><b>注：<i>请在调用enterMeeting接口前调用此接口</i></b>
	 * @param bAuto  true：自动打开；false：不自动打开
	 */
	public void setAutoOpenAV(boolean bAuto)
	{
		m_autoOpenAV = (bAuto == true ? 1 : 0);
	}

	/**
	 * 设置到会议结束时间后是否自动退出会议，默认值从服务器读取
	 * <p>通过此接口设置的属性优先级高于从服务器读取的信息的优先级
	 * <p><b>注：<i>请在调用enterMeeting接口前调用此接口</i></b>
	 * @param bAuto  true：退出；false：不退出
	 */
	public void setAutoQuit(boolean bAuto)
	{
		m_autoQuit = (bAuto == true ? 1 : 0);
	}

	private static HashMap<String, String> parseProtocol(String protocol)
	{
//		protocol = Uri.decode(protocol);
		Log.d("xiao", "protocol = "+protocol);
		String protocolBody = "";
		String orderedPortocol = "";
		String unOrderedProtocol = "";
		if (protocol.startsWith("weiyi://"))
		{
			protocolBody = protocol.replace("weiyi://", "");
			if(protocolBody.isEmpty())
			{
				Log.d("emm", "protocol = null");
				return null;
			}
			else
			{
				if(protocolBody.startsWith("start?")||protocolBody.startsWith("start/?"))
				{
					unOrderedProtocol = protocolBody.replace("start?", "");
					unOrderedProtocol = unOrderedProtocol.replace("start/?", "");
					String[] kv_unsplit = unOrderedProtocol.split("&");
					for(int i=0;i<kv_unsplit.length;i++)
					{
						String[] kv_unsplittemp = kv_unsplit[i].split("=");
						try {
							m_protocolParsedResult.put(kv_unsplittemp[0].toLowerCase(), URLDecoder.decode(kv_unsplittemp[1], "UTF-8"));
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				else
				{

					orderedPortocol = Uri.decode(protocolBody);//xiaoyang change "/"分割的需要decode
					String[] paramValue = orderedPortocol.split("/");
					//					weiyi://ip:port/meetingid/pwd/nickname/usertype/thirdID/auth/ts/linkname/linkurl/isInstMeeting(默认0为普通会议  1为即时会议)
					//						/quitsoftware(0,不退，1退出)/disableProximitySensor(0不禁用，1禁用，缺省0）/disableScreenRotation(0不禁用横竖屏切换，1是只能横屏，缺省0)/hideme(0,不隐身，1是隐身)
					//						/account/accountpwd/isAutoEntermeeting/chairmancontrol
					if(paramValue.length >= 1)
					{
						m_protocolParsedResult.put("ip".toLowerCase(), paramValue[0]);
					}
					if(paramValue.length >= 2)
					{
						m_protocolParsedResult.put("meetingid".toLowerCase(), paramValue[1]);
					}
					if(paramValue.length >= 3)
					{
						m_protocolParsedResult.put("pwd".toLowerCase(), paramValue[2]);
					}
					if(paramValue.length >= 4)
					{
						m_protocolParsedResult.put("nickname".toLowerCase(), paramValue[3]);
					}
					if(paramValue.length >= 5)
					{
						m_protocolParsedResult.put("usertype".toLowerCase(), paramValue[4].length()==0?"0":paramValue[4]);

					}
					if(paramValue.length >= 6)
					{
						m_protocolParsedResult.put("thirdID".toLowerCase(), paramValue[5].length()==0?"0":paramValue[5]);
					}
					if(paramValue.length >= 7)
					{
						m_protocolParsedResult.put("auth".toLowerCase(), paramValue[6]);
					}
					if(paramValue.length >= 8)
					{
						m_protocolParsedResult.put("ts".toLowerCase(), paramValue[7]);
					}
					if(paramValue.length >= 9)
					{
						m_protocolParsedResult.put("linkname".toLowerCase(), paramValue[8]);
					}
					if(paramValue.length >= 10)
					{
						m_protocolParsedResult.put("linkurl".toLowerCase(), paramValue[9]);
					}
					if(paramValue.length >= 11)
					{
						m_protocolParsedResult.put("isInstMeeting".toLowerCase(), paramValue[10].length()==0?"0":paramValue[10]);
					}
					if(paramValue.length >= 12)
					{
						m_protocolParsedResult.put("quitsoftware".toLowerCase(), paramValue[11].length()==0?"0":paramValue[11]);
					}
					if(paramValue.length >= 13)
					{
						m_protocolParsedResult.put("enableProximitySensor".toLowerCase(), paramValue[12].length()==0?"1":paramValue[12]);
					}
					if(paramValue.length >= 14)
					{
						m_protocolParsedResult.put("disableScreenRotation".toLowerCase(), paramValue[13].length()==0?"0":paramValue[13]);
					}
					if(paramValue.length >= 15)
					{
						m_protocolParsedResult.put("hideme".toLowerCase(), paramValue[14].length()==0?"0":paramValue[14]);
					}
					if(paramValue.length >= 16)
					{
						m_protocolParsedResult.put("account".toLowerCase(), paramValue[15]);
					}
					if(paramValue.length >= 17)
					{
						m_protocolParsedResult.put("accountpwd".toLowerCase(), paramValue[16]);
					}
					if(paramValue.length >= 18)
					{
						m_protocolParsedResult.put("isAutoEntermeeting".toLowerCase(), paramValue[17].length()==0?"1":paramValue[17]);
					}
					if(paramValue.length >= 19)
					{
						m_protocolParsedResult.put("chairmancontrol".toLowerCase(), paramValue[18]);
					}
					//					if(paramValue.length >= 20)
					//					{
					//						m_protocolParsedResult.put("chairmanfunc", paramValue[19]);
					//					}

				}
			}
		}
		else if (protocol.startsWith("http://"))
		{
			Log.d("emm", "protocol start with http://");
			return null;
		}
		return m_protocolParsedResult;
	}

	public static void clearMeetingProtocol()
	{
		m_protocolParsedResult.clear();
	}

	@SuppressLint("DefaultLocale")
	private static void enterMeeting(final Activity activity,String protocol,final String name)
	{

		innerEnterMeeting(activity,protocol,name,"",null);
	}

	public static void errorTipDialog(final Activity activity,int errorTipID) {
		if(activity==null || getApplicationContext()==null||activity.isFinishing()||activity.isDestroyed())
			return;
		Utitlties.HideProgressDialog(activity);
		try {
			AlertDialog.Builder build = new AlertDialog.Builder(activity);
			build.setTitle(getApplicationContext().getString(UZResourcesIDFinder.getResStringID("link_tip")));
			build.setMessage(getApplicationContext().getString(errorTipID));
			build.setPositiveButton(getApplicationContext().getString(UZResourcesIDFinder.getResStringID("OK")), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					Utitlties.HideProgressDialog(activity);
					arg0.dismiss();
				}

			});
			build.show();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static boolean isNum(String str) {
		return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
	}

	public Intent getMeetingNotifyIntent() {
		return WeiyiClient.getInstance().getMeetingNotifyIntent();
	}


	public String getM_httpServer() {
		return WeiyiClient.getInstance().getM_httpServer();
	}
	public void setM_httpServer(String httpServer) {
		WeiyiClient.getInstance().setM_httpServer(httpServer);
		Session.getInstance().setWebHttpServerAddress(httpServer);
	}
	@Override
	public void didReceivedNotification(int id, Object... args)
	{
		if(id == CHECK_MEETING)
			WeiyiMeetingNotificationCenter.getInstance().postNotificationName(CHECK_MEETING,args);
			//else if(id == MeetingSession.STOP_TIMER)
			//WeiyiMeetingNotificationCenter.getInstance().postNotificationName(STOP_TIMER,args);
		else if(id == NET_CONNECT_LEAVE)
		{
			//WeiyiMeetingNotificationCenter.getInstance().postNotificationName(NET_CONNECT_LEAVE,args);

			NotificationCenter.getInstance().removeObserver(mInstance, NET_CONNECT_LEAVE);
			Log.e("emm", "receive net_connnect_leave message and enter new meeting room");
			if(bRjoinMeeting)
			{
				enterMeeting(m_activity,m_protocol,m_nickName);
				bRjoinMeeting=false;
			}
			else if(bForceExitMeeting)
			{
				//发通知，目的，当你在IM登录状态后，而且在会议中，有同样IM账号登录，你需要退出会议
				WeiyiMeetingNotificationCenter.getInstance().postNotificationName(FORCE_EXIT_MEETING);
				bForceExitMeeting = false;
			}
		}
		else if(id==PRESENCE_COMPLETE)
		{
			ArrayList<Integer> uids = Session.getInstance().getUserMgr().getThirdUids();
			if(m_notify!=null)
			{
				m_notify.onPresentComplete(uids,getM_chatid(),getM_strMeetingID());
			}
		}
		else if(id==NET_CONNECT_USER_IN)
		{
			int peerid = (Integer) args[0];
			MeetingUser user =Session.getInstance().getUserMgr().getUser(peerid);
			if(user!=null)
			{
				if(m_notify!=null)
					m_notify.onUserIn(user.getThirdID(),getM_chatid());
			}

		}
		else if(id==NET_CONNECT_USER_OUT)
		{
			int peerid = (Integer) args[0];
			//MeetingUser user = MeetingSession.getInstance().getUserMgr().getUser(peerid);
			//if(user!=null)
			//{
			if(m_notify!=null)
			{
				ArrayList<Integer> uids = Session.getInstance().getUserMgr().getThirdUids();
				m_notify.onUserOut(uids,getM_chatid());
			}
			//}
		}
		else if(id==EXIT_MEETING)
		{
			if(m_notify!=null)
			{
				ArrayList<Integer> uids = Session.getInstance().getUserMgr().getThirdUids();
				//int bAutoExitWeiyi = MeetingSession.getInstance().getM_autoExitWeiyi();
				m_notify.onExitMeeting(getM_chatid(),uids,getM_strMeetingID());
				m_notify=null;
			}
		}
		else if(id==AUTO_EXIT_MEETING)
		{
			Log.e("emm", "auto exit meeting 3***************");
			NotificationCenter.getInstance().removeObserver(mInstance, AUTO_EXIT_MEETING);
			WeiyiMeetingNotificationCenter.getInstance().postNotificationName(AUTO_EXIT_MEETING);
		}
	}
	private static void inputMeetingPassward(final Activity activity,int nTipID, final String protocol, final String name) {
		Utitlties.HideProgressDialog(activity);
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		LayoutInflater layoutInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(UZResourcesIDFinder.getResLayoutID("meeting_password"), null);
		final EditText etpsd = (EditText) view.findViewById(UZResourcesIDFinder.getResIdID("et_psd"));

		builder.setPositiveButton(getApplicationContext().getString(UZResourcesIDFinder.getResStringID("OK")), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
				innerEnterMeeting(activity,protocol,name,etpsd.getText().toString(),null);
			}

		});
		builder.setNegativeButton(getApplicationContext().getString(UZResourcesIDFinder.getResStringID("cancel")),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
						Utitlties.HideProgressDialog(activity);
					}
				});
		AlertDialog adlg = builder.create();
		adlg.setView(view);
		adlg.setTitle(getApplicationContext().getString(nTipID));
		adlg.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface arg0) {
				com.weiyicloud.whitepad.Utitlties.showKeyboard(etpsd);
			}
		});
		adlg.show();
		adlg.setCanceledOnTouchOutside(false);
	}

	private static void onConnect(Activity activity,int nRet)
	{
		Log.e("emm", "check meeting failed and result="+nRet);
		if(activity==null){
			return;
		}
		if (nRet == 4008) {
			if(isbroadcast)
				inputMeetingPassward(activity, UZResourcesIDFinder.getResStringID("checkmeeting_error_5003"),m_protocol,m_nickName);
			else
				inputMeetingPassward(activity, UZResourcesIDFinder.getResStringID("checkmeeting_error_4008"),m_protocol,m_nickName);
		} else if (nRet == 4110) {
			if(isbroadcast)
				inputMeetingPassward(activity, UZResourcesIDFinder.getResStringID("checkmeeting_error_5004"),m_protocol,m_nickName);
			else
				inputMeetingPassward(activity, UZResourcesIDFinder.getResStringID("checkmeeting_error_4110"),m_protocol,m_nickName);
		} else if (nRet == 4007) {
			if(isbroadcast)
				errorTipDialog(activity,UZResourcesIDFinder.getResStringID("checkmeeting_error_5002"));
			else
				errorTipDialog(activity,UZResourcesIDFinder.getResStringID("checkmeeting_error_4007"));
		} else if (nRet == 3001) {
			errorTipDialog(activity,UZResourcesIDFinder.getResStringID("checkmeeting_error_3001"));
		} else if (nRet == 3002) {
			errorTipDialog(activity,UZResourcesIDFinder.getResStringID("checkmeeting_error_3002"));
		} else if (nRet == 3003) {
			if(isbroadcast)
				errorTipDialog(activity,UZResourcesIDFinder.getResStringID("checkmeeting_error_5001"));
			else
				errorTipDialog(activity,UZResourcesIDFinder.getResStringID("checkmeeting_error_3003"));
		} else if (nRet == 4109) {
			errorTipDialog(activity,UZResourcesIDFinder.getResStringID("checkmeeting_error_4109"));
		} else if (nRet == 4103) {
			errorTipDialog(activity,UZResourcesIDFinder.getResStringID("checkmeeting_error_4103"));
		} else {
			errorTipDialog(activity,UZResourcesIDFinder.getResStringID("WaitingForNetwork"));
		}

	}
	private static void innerEnterMeeting(final Activity activity,final String protocol,final String name,final String midPWD,final MeetingNotify notify)
	{
		Log.d("xiao", "inner_protocol="+protocol);
		String strProtocol = protocol;
		m_protocol = protocol;
		String httpServer="";
		int hideme = 0;
		String nickname = "";
		int usertype = 0;
		String linkname = "";
		String linkurl = "";
		int isInstMeeting = 0;//(默认0为普通会议  1为即时会议)

		int enableProximitySensor = 1;//(0不启用，1启用，缺省启用）
		int disableScreenRotation = 0;//横竖屏切换(0不禁用，1禁用，缺省不禁用)
		String account = "";
		String accountpwd = "";
		int isAutoEntermeeting = 0;
		String chairmancontrol = "";
		String chairmanfunc = "";
		HashMap<String, String> result;
		int meetingtype = 0;
		String clientIdf = "";
		String title = "";
		int createrId = 0;
		int userid = 0;
		int port = 80;


		if (strProtocol.isEmpty())
			return;
		Log.e("emm", "enterMeeting strProtocol="+strProtocol);

		result = parseProtocol(strProtocol);
		if(result.isEmpty()){
			return;
		}
		httpServer = result.get("ip".toLowerCase()) == null?"":result.get("ip".toLowerCase());
		if(!httpServer.contains(":"))
		{
			if(result.get("port".toLowerCase()) != null)
			{
				String sport = result.get("port".toLowerCase());
				port = Integer.parseInt(sport);
			}
		}
		else
		{
			if(httpServer.contains("http://")){
				httpServer = httpServer.replace("http://", "");
			}
			String sport = httpServer.substring(httpServer.indexOf(":")+1);
			port = Integer.parseInt(sport);
			httpServer = httpServer.substring(0, httpServer.indexOf(":"));
		}

		mid = result.get("meetingid".toLowerCase()) == null?"":result.get("meetingid".toLowerCase());
		pwd = result.get("pwd".toLowerCase()) == null?"":result.get("pwd".toLowerCase());
		if(result.get("meetingtype")!=null&&!result.get("meetingtype").isEmpty()){
			meetingtype = Integer.parseInt(result.get("meetingtype"));
		}
		if(result.get("clientidentification")!=null&&!result.get("clientidentification").isEmpty()){
			clientIdf = result.get("clientidentification");
		}
		if(result.get("title")!=null&&!result.get("title").isEmpty()){
			title = result.get("title");
		}
		if(result.get("createrid")!=null&&!result.get("createrid").isEmpty()){
			createrId = Integer.parseInt(result.get("createrid"));
		}
		if(result.get("userid")!=null&&!result.get("userid").isEmpty()){
			userid = Integer.parseInt(result.get("userid"));
		}
		if(!midPWD.isEmpty())
			pwd = midPWD;
		nickname = result.get("nickname".toLowerCase()) == null?"":result.get("nickname".toLowerCase());


		if(m_userType == -1)
		{
			if(result.get("usertype".toLowerCase()) == null||result.get("usertype".toLowerCase())==""){
				usertype = 0;
			}else{
				usertype = Integer.parseInt(result.get("usertype".toLowerCase()));
			}
			//			因为会出现为“”的情况所以要多一个判断不能按照原来的三元运算符去写，xiaoyang
			//			usertype = result.get("usertype".toLowerCase()) == null?0:Integer.parseInt(result.get("usertype".toLowerCase()));
			setUserType(usertype);
		}
		else
		{
			setUserType(m_userType);
		}

		if(m_ThirdUserID == -1)
		{
			if(result.get("thirdID".toLowerCase()) == null||result.get("thirdID".toLowerCase())==""){
				thirdID = 0;
			}else{
				thirdID = Integer.parseInt(result.get("thirdID".toLowerCase()));
			}
			//			因为会出现为“”的情况所以要多一个判断不能按照原来的三元运算符去写，xiaoyang
			//			thirdID = result.get("thirdID".toLowerCase()) == null?0:Integer.parseInt(result.get("thirdID".toLowerCase()));
		}
		else
		{
			thirdID = m_ThirdUserID;
		}

		linkname = result.get("linkname".toLowerCase()) == null?"":result.get("linkname".toLowerCase());
		linkurl = result.get("linkurl".toLowerCase()) == null?"":result.get("linkurl".toLowerCase());
		Log.d("xiao", "linkurl = "+linkurl);
		if(linkurl != "")
		{
			try {
				setLinkUrl(URLDecoder.decode(linkurl, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setLinkName(linkname);
		}

		if(m_isInstMeeting == -1)
		{
			isInstMeeting = result.get("isInstMeeting".toLowerCase()) == null?0:Integer.parseInt(result.get("isInstMeeting".toLowerCase()));
		}
		else
		{
			isInstMeeting = m_isInstMeeting;
		}

		if(m_isQuitSoftware == -1)
		{
			quitsoftware = result.get("quitsoftware".toLowerCase()) == null?0:Integer.parseInt(result.get("quitsoftware".toLowerCase()));
		}
		else
		{
			quitsoftware = m_isQuitSoftware;
		}

		if(m_isEnableProximitySensor == -1)
		{
			String unOrderProEnableStatus = result.get("disableProximitySensor".toLowerCase());
			String orderProEnableStatus = result.get("enableProximitySensor".toLowerCase());
			if(unOrderProEnableStatus == null)
			{
				if(orderProEnableStatus == null)
				{
					enableProximitySensor = 1;
				}
				else
				{
					int orderStatus = Integer.parseInt(orderProEnableStatus);
					enableProximitySensor = orderStatus;
				}
			}
			else
			{
				int unOrderStatus = Integer.parseInt(unOrderProEnableStatus);
				if(unOrderStatus == 0)
				{
					enableProximitySensor = 1;
				}
				else if(unOrderStatus == 1)
				{
					enableProximitySensor = 0;
				}
			}
		}
		else
		{
			enableProximitySensor = m_isEnableProximitySensor;
		}

		if(m_screenRotation == -1)
		{
			disableScreenRotation = result.get("disableScreenRotation".toLowerCase()) == null?0:Integer.parseInt(result.get("disableScreenRotation".toLowerCase()));
		}
		else
		{
			disableScreenRotation = m_screenRotation;
		}

		if(m_isHideMe == -1)
		{
			hideme = result.get("hideme".toLowerCase()) == null?0:Integer.parseInt(result.get("hideme".toLowerCase()));
		}
		else
		{
			hideme = m_isHideMe;
		}

		account = result.get("account".toLowerCase()) == null?"":result.get("account".toLowerCase());
		accountpwd = result.get("accountpwd".toLowerCase()) == null?"":result.get("accountpwd".toLowerCase());
		isAutoEntermeeting = result.get("isAutoEntermeeting".toLowerCase()) == null?1:Integer.parseInt(result.get("isAutoEntermeeting".toLowerCase()));
		if(m_chairmanControl == "")
		{
			chairmancontrol = result.get("chairmancontrol".toLowerCase()) == null?"":result.get("chairmancontrol".toLowerCase());
		}
		else
		{
			Session.getInstance().setChairmanControl(m_chairmanControl);
		}

		webServer = httpServer+":"+port;
		//		final int exitWeiyi = quitsoftware;
		//		final int hasSensor = enableProximitySensor;
		//		final int isScreenRotation = disableScreenRotation;
		//		final int ishiedme = hideme;
		final int thirduid = thirdID;
		//		final int m_meetingType = meetingtype;
		//		final String strclientIdf = clientIdf;
		//		final String strtitle = title;
		//		final int fincreaterid = createrId;
		//		final int finuserid = userid;
		if(nickname!=null&&!nickname.isEmpty()){
			m_nickName = nickname;
		}
		if(isInstMeeting==1)
		{
			joinInstMeeting(activity, httpServer+":"+port, mid, nickname, thirduid, 0, notify, null);
		}
		else
		{
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("isWeiYiVirsion", true);
			map.put("noenter", true);
			if(port == 0){
				port = 80;
			}
			Session.getInstance().joinmeeting(httpServer, port, nickname, mid, pwd, thirduid, usertype,map);
		}
		//		startMeeting(activity,webServer,mid,name,pwd,false,thirduid,0,exitWeiyi,hasSensor,isScreenRotation,ishiedme,m_meetingType,strclientIdf,strtitle,fincreaterid,finuserid);
	}

	public String getLIVE_MEDIA_PORT(){
		return Session.getInstance().getLIVE_MEDIA_PORT();
	}
	public String getLIVE_MEDIA_SERVER(){
		return Session.getInstance().getLIVE_MEDIA_SERVER();
	}

	public void DoMsg(int tm)
	{
		try {
			if(m_pageList!=null)
			{
				JSONObject ret = m_pageList.OnTS(tm);
				if(ret!=null)
				{
					for (int i = 0; i < ret.getJSONArray("msgs").length(); i++) {
						JSONObject msg = ret.getJSONArray("msgs").getJSONObject(i);
						if(!msg.has("body")){
							continue;
						}
						final JSONObject obj = new JSONObject();
						obj.put("name", "ShowPage");
						obj.put("body", msg.get("body"));
						Utitlties.RunOnUIThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								Session.getInstance().whitePadChange(obj, null, null);
							}
						});
					}
				}
			}

			if(m_shapeList != null)
			{
				JSONObject ret1 = m_shapeList.OnTS(tm);
				if(ret1!=null)
				{
					for (int i = 0; i < ret1.getJSONArray("msgs").length(); i++) {
						final JSONObject msg1 = ret1.getJSONArray("msgs").getJSONObject(i);
						if(!msg1.has("add") || !msg1.has("msg"))
							continue;
						if(msg1.has("msg")){
							JSONObject msg = msg1.getJSONObject("msg");
							String strbody = msg.getString("body");
							final byte[] shapedata = Base64.decode(strbody, Base64.DEFAULT);
							Utitlties.RunOnUIThread(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									try {
										Session.getInstance().whitePadChange(msg1.getJSONObject("msg"), shapedata, msg1.getInt("add")==1);
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							});
						}
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}



	@Override
	public void onCameraWillClose(Camera cam) {
		NotificationCenter.getInstance().postNotificationName(
				VIDEO_NOTIFY_CAMERA_WILL_CLOSE, cam);

	}



	@Override
	public void onPhotoTaken(boolean success, byte[] data) {
		NotificationCenter.getInstance().postNotificationName(
				UI_NOTIFY_USER_PICTURE_TAKEN, success, data);

	}



	@Override
	public void onCameraDidOpen(Camera cam, boolean isFront, int index) {
		NotificationCenter.getInstance().postNotificationName(
				VIDEO_NOTIFY_CAMERA_DID_OPEN, cam, isFront,
				index);

	}



	@Override
	public void ChangeAudioStatus(int UserID, int Status) {
		NotificationCenter.getInstance().postNotificationName(
				UI_NOTIFY_USER_AUDIO_CHANGE, UserID, Status);

	}



	@Override
	public void showpage() {
		NotificationCenter.getInstance().postNotificationName(SHOWPAGE);


	}



	@Override
	public void onPresentComplete() {
		isEnterMeeting = true;
		Utitlties.RunOnUIThread(new Runnable() {

			@Override
			public void run() {
				getStream();

			}
		});
		if(Session.getInstance().getUserMgr().getCount()==0){
			NotificationCenter.getInstance().postNotificationName(
					PRESENCE_COMPLETE);
		}
	}



	@Override
	public void onVideoSizeChanged(int peerID, int videoIdx, int width,int height) {
		if(videoIdx == 5000)
			NotificationCenter.getInstance().postNotificationName(SCREENSHARE_CHANGE, width,height);
	}

	@Override
	public void onWhitePadPageCount(int count) {

	}

	public void setRole(int mRole){
		Session.getInstance().getUserMgr().getSelfUser().setRole(mRole);
	}

	public static void joinBroadCastPlayback(Activity activity,int meetingid,int meetingtype,String strHttpUrl,String pichttpUrl){
		Intent intent = new Intent(activity, BroadcastPlayBack_Activity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("meetingtype", meetingtype);
		bundle.putString("httpurl", strHttpUrl);
		bundle.putString("meetingid",meetingid+"");
		bundle.putString("pichttpUrl", pichttpUrl);
		intent.putExtras(bundle);
		activity.startActivity(intent);
	}

	/**
	 * 开始自由发言
	 */
	public void StartSpeaking() {
		String userID = "RequestSpeak" + getMyPID();
		boolean force = Session.getInstance().getUserMgr().getSelfUser().isChairMan();
		// remote_pubmsg_i
		Session.getInstance().PublishMessage("RequestSpeak", Session.SENDMSGTOALL, getMyPID(), force, userID,
				"");
		// PublishMessage("RequestSpeak", SENDMSGTOALL, getMyPID(), null, id,
		// "");

	}
	/**
	 * 取消自由发言
	 */
	public void StopSpeaking() {
		// remote_delmsg_i
		String id = "RequestSpeak" + getMyPID();
		Session.getInstance().DeleteMessage("RequestSpeak", Session.SENDMSGTOALL, getMyPID(), null, id, "");
	}

	/**
	 * 设置自由发言模式
	 *
	 * @param bFree
	 */

	public void entermeeting(final String nickname, final String meeting_id, final int user_id,
							 final boolean serverMix, final int user_type,final String headurl){
		Session.getInstance().enterMeeting(nickname, meeting_id, user_id, serverMix, user_type,headurl);

	}



	@Override
	public void onGotMeetingProperty(JSONObject arg0) {
		Utitlties.HideProgressDialog(Session.getInstance().getActivity());
		m_chairmancontrol = Session.getInstance().getChairmanControl();
		m_chairmanfunc = Session.getInstance().getChairmanFunc();
//		meetingName = Session.getInstance().getM_strMeetingName();
		if(isbroadcast){
			if(isLiveMeeting()){
				if(isViewer()){
					startBroadcast(Session.getInstance().getActivity(), webServer, mid, m_nickName, m_pwd, false, thirdID, 0, 0, 1, 0, 0, 0, "", "", 0, 0);
				}else{
					startMeeting(Session.getInstance().getActivity(),
							webServer,mid,m_nickName,pwd,false,thirdID,0,quitsoftware,1,0,0,0,"","",0,0);
				}
			}else{
				exitMeeting();
				errorTipDialog(Session.getInstance().getActivity(),UZResourcesIDFinder.getResStringID("IsMeeting"));
			}
		}else{
			if(!isLiveMeeting()){
				if(instMid!=null&&!instMid.isEmpty()){
					startMeeting(Session.getInstance().getActivity(),
							webServer,Session.getInstance().getM_strMeetingID(),m_nickName,pwd,true,thirdID,getM_chatid(),quitsoftware,1,0,0,0,"","",0,0);
				}else{
					startMeeting(Session.getInstance().getActivity(),
							webServer,mid,m_nickName,pwd,false,thirdID,0,quitsoftware,1,0,0,0,"","",0,0);
				}
			}else{
				exitMeeting();
				errorTipDialog(Session.getInstance().getActivity(),UZResourcesIDFinder.getResStringID("IsLiveMeeting"));
			}
		}

	}
	/**
	 * 是否允许服务器录制
	 * @return
	 */
	public boolean isAllowServerRecord() {
		return Session.getInstance().isAllowServerRecord();
	}
	/**
	 * 是否自动服务器录制
	 * @return
	 */
	public boolean isAutoServerRecord() {
		return Session.getInstance().isAutoServerRecord();
	}
	/***
	 * xiaoyang
	 * 是否是sip会议
	 */
	public boolean isSipMeeting(){
		return Session.getInstance().isSipMeeting();
	}

	/**
	 * 获取会议配置之后再去,获取会议文件。
	 */
	public void getMeetingFile_UI(final int meetingid,final GetMeetingFileCallBack callback) {
		Utitlties.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				String url = Session.getInstance().webFun_getconfig;
				Log.e("emm", "getmeetingconfig url=" + url);

				// client.setTimeout(5);
				client.post(url, null, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String content) {
						Log.e("emm",
								"getmeetingconfig success**********************");
						try {
							final String res = content;
							parserMeetingConfig(res,meetingid,callback);

						} catch (Exception e) {
							e.printStackTrace();

						}
					}

					@Override
					public void onFailure(Throwable error, String content) {


					}
				});
			}
		});

	}

	/**
	 * 解析会议配置(地址、ip、名字、端口号)
	 *
	 * @param strRet
	 * @return
	 */
	boolean parserMeetingConfig(String strRet,int meetingid,GetMeetingFileCallBack callback) {

		boolean bsuccess = false;

		try {
			JSONObject jsRoot = new JSONObject(strRet);
			String strDocServer = jsRoot.optString("DocConvertServerAddr");
			//xiaoyang add
			String MEETING_DOC_ADDR = "";
			if (strDocServer != null && !strDocServer.isEmpty())
				MEETING_DOC_ADDR = strDocServer;
			else
				MEETING_DOC_ADDR = Session.getInstance().getWebHttpServerAddress();
			Session.getInstance().getMeetingFile(meetingid,MEETING_DOC_ADDR, callback);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return bsuccess;
	}

	/**
	 * 获取直播在线人数
	 */
	public void getLiveOnLineNum()
	{
		final String url = Session.getInstance().webFun_getOnlineNum;
		RequestParams params = new RequestParams();
		String meetingid = Session.getInstance().getM_strMeetingID();
		params.put("serial", meetingid);
		Log.d("emm", params.toString());
		client.post(url, params, new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(String content) {
				try {
					JSONObject obj = new JSONObject(content);
					int result = obj.getInt("result");
					if(result == 0){
						JSONArray onlinenums = obj.optJSONArray("onlinenum");
						JSONObject onlinenum = onlinenums.optJSONObject(0);
						final int num = onlinenum.optInt("num");
						Session.getInstance().getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								NotificationCenter.getInstance().postNotificationName(UI_NOTIFY_GET_ONLINE_NUM, num);
								Log.d("emm", "onlinenum="+num);
							}
						});
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			@Override
			public void onFailure(Throwable error) {
				error.printStackTrace();
			}
		});
	}

	//	public String getBackMeetingUrl(){
	//		String httpurl = "";
	//		String port = "";
	//		if(webServer.contains(":")){
	//			String[] temp = webServer.split(":");
	//			httpurl = temp[0];
	//			port = temp[1];
	//		}
	//		String backUrl = "weiyi://start?ip="+httpurl+"&port="+port+"&meetingid="+getM_strMeetingID()+"&meetingtype="+Session.getInstance().getMeetingtype()+"&userid="+m_ThirdUserID+"&nickname="+m_nickName;
	//		Log.d("xiao", "backUrl = "+backUrl);
	//		return backUrl;
	//	}
	static int[] headImg = new int[8];

	private int getChatHeadColor(){
		int len = headImg.length;//获取数组长度给变量len
		Random random = new Random();//创建随机对象
		int arrIdx = random.nextInt(len-1);//随机数组索引，nextInt(len-1)表示随机整数[0,(len-1)]之间的值
		int num = headImg[arrIdx];//获取数组值
		return num;
	}

	/***
	 * call sip
	 */
	public void CallSipPhone(String terminalNum,String nickname,int action){

		Session.getInstance().callSipPhone(terminalNum, nickname, action);
	}



	@Override
	public void onCallSipACK(int mark, int state) {
		NotificationCenter.getInstance().postNotificationName(UI_NOTIFY_SIP_ACK_STATE, state);

	}
	public int getCallSipState() {
		return Session.getInstance().getCallSipState();
	}
	public void setVideoForSip(int peerid,int videoid){
		Session.getInstance().setVideoForSip(peerid, videoid);
	}



	@Override
	public void onFocusSipChange(int peerID, int videoId) {
		NotificationCenter.getInstance()
				.postNotificationName(
						UI_NOTIFY_SIP_FOCUS_CHANGED,
						peerID,videoId);

	}

	//xiaoyang 判断是否有sip和我是不是主席用户在并且给sip用户指定默认视频
	public void sendDefaultVideoToSip(){
		MeetingUser muSelf = Session.getInstance().getUserMgr().getSelfUser();
		if(muSelf.isChairMan()){
			boolean hasSipUser = false;
			Session.getInstance().getUserMgr().reSort();
			for (int i = 0; i < Session.getInstance().getUserMgr().getCountNoHideUser(); i++) {
				MeetingUser mu = Session.getInstance().getUserMgr().getUserFromIndex(i);
				if(mu.getClientType() == 7){
					hasSipUser = true;
				}
			}
			if(hasSipUser){
				if(hasVideoForSip()){
					int videoForSipPeerid = Session.getInstance().getVideoPeerIdForSip();
					int videoIdForSip = Session.getInstance().getVideoIdForSip();
					setVideoForSip(videoForSipPeerid, videoIdForSip);
				}else{
					if(videoForSipPeerId!=-1){
						setVideoForSip(videoForSipPeerId, videoForSipVideoId);
					}
					MeetingUser muself = Session.getInstance().getUserMgr().getSelfUser();
					if(muself!=null&&muSelf.ishasVideo()&&muself.getClientType()<4&&muself.getClientType()>0){
						setVideoForSip(muself.getPeerID(), muself.getDefaultCameraIndex());
						return;
					}
					Session.getInstance().getUserMgr().reSortUserHasVideo(1);
					if(Session.getInstance().getUserMgr().usersHasVideo.size()!=0){
						MeetingUser muHasVideo = Session.getInstance().getUserMgr().usersHasVideo.get(0);
						setVideoForSip(muHasVideo.getPeerID(), muHasVideo.getDefaultCameraIndex());
					}
				}
			}
		}
	}
	public void setVideoIdAndPeerIdForSip(int videoForSipPeerId,int videoForSipVideoId){
		this.videoForSipPeerId = videoForSipPeerId;
		this.videoForSipVideoId = videoForSipVideoId;
	};
	public boolean hasTempVideoForSip(){
		return videoForSipPeerId != -1;
	}
	public int getVideoPeerIdForSip(){
		return Session.getInstance().getVideoPeerIdForSip();
	}

	public int getVideoIdForSip(){
		return Session.getInstance().getVideoIdForSip();

	}
	public void getStream(){
		RequestParams params = new RequestParams();
		params.put("serial", Session.getInstance().getM_strMeetingID());
		webFun_getStream = Session.getInstance().MEETING_PHP_SERVER+Session.getInstance().webFunBase+"getstream";
//		webFun_getStream += "/serial/";
//		webFun_getStream += Session.getInstance().getM_strMeetingID();
		Log.d("xiao", "getStreamAddress = "+webFun_getStream);
		Log.d("xiao", "getStreamParams = "+params.toString());
//		AsyncHttpClient client1 = new AsyncHttpClient();
		client.post(webFun_getStream, params, new AsyncHttpResponseHandler() {
			//		client1.get(webFun_getStream, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(final String content) {
				Utitlties.RunOnUIThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {

							JSONObject jsb = new JSONObject(content);
							int result = jsb.optInt("result");
							if(result == 0){
								JSONArray jsarray = jsb.optJSONArray("stream");
								for (int i = 0; i < jsarray.length(); i++) {
									JSONObject jss = jsarray.optJSONObject(i);
									MeetingUser muliu = new MeetingUser();
									muliu.setPeerID(jss.optInt("streamid"));
									muliu.setName(jss.optString("streamname"));
									muliu.setClientType(21);
									muliu.sethasVideo(true);
									Session.getInstance().getUserMgr().addUser(muliu);
									NotificationCenter.getInstance()
											.postNotificationName(NET_CONNECT_USER_IN,
													muliu.getPeerID(), false);
								}
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

			}

			@Override
			public void onFailure(Throwable error, String content) {
				Log.d("emm", "onFailure");
			}
			@Override
			public void onFinish() {
				super.onFinish();
				Log.d("xiao", "onFinish");
			}
		});
	}
	public boolean isHighquality(){
		return Session.getInstance().isHighquality();
	}



	@Override
	public void onWatchStopped(int peerID, int videoIdx) {
		// TODO Auto-generated method stub

	}
}



package info.emm.meeting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.hardware.Camera;
import android.hardware.Camera.FaceDetectionListener;

import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import info.emm.sdk.EmmLog;
import info.emm.sdk.RtmpClientMgr;
import info.emm.sdk.RtmpClientMgrCbk;
import info.emm.sdk.VideoView;
import info.emm.sdk.joinMeetingCallBack;

import info.emm.utils.Utitlties;

import org.msgpack.MessagePack;
import org.msgpack.type.Value;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.squareup.okhttp.internal.Util;
import com.weiyicloud.whitepad.DocInterface;
import com.weiyicloud.whitepad.FaceShareControl;
import com.weiyicloud.whitepad.ControlMode;
import com.weiyicloud.whitepad.NotificationCenter;
import com.weiyicloud.whitepad.GetMeetingFileCallBack;
//import com.weiyicloud.whitepad.NotificationCenter;
//import com.weiyicloud.whitepad.PaintPad;
//import com.weiyicloud.whitepad.ShareDoc;
//import com.weiyicloud.whitepad.SharePadMgr;
import com.weiyicloud.whitepad.TL_PadAction;
import com.weiyicloud.whitepad.WhitePadInterface;

@SuppressLint("UseValueOf")
public class Session implements RtmpClientMgrCbk, FaceShareControl {

    // final static public String DEFAULT_SERVER = "192.168.0.99";
    // final static public String DEFAULT_PORT = "443";

    final static public String ASSOCAITEUSERID = "associatedUserID";
    final static public String ASSOCAITEMSGID = "assocaitedMsgID";
    public int nLastShowPageUser = 0;


    static public int SENDMSGTOALL_EXCEPT_ME = 0;
    static public int SENDMSGTOALL = 0xFFFFFFFF;
    static public int SENDMSGTOSERVER = 0xFFFFFFFE;

//	public static int Kickout_ChairmanKickout = 0;
//	public static int Kickout_Repeat = 1;

    static public int RequestSpeak_Disable = 0;// �������ɷ���ʧ��
    static public int RequestSpeak_Allow = 1;// �ɹ�
    static public int RequestSpeak_Pending = 2;// ��������

//	static public int RequestHost_Disable = 0;// ��������ʧ��
//	static public int RequestHost_Allow = 1;// �ɹ�
//	static public int RequestHost_Pending = 2;// ������

    static public int MeetingMode_Free = 0;// ����ģʽ
    static public int MeetingMode_ChairmanControl = 1;// ����ģʽ


    private RtmpClientMgr mRtmpMgr;//

    private ArrayList<Integer> mALSpeakerList = new ArrayList<Integer>();

    private ArrayList<Integer> mALPendingSpeakerList = new ArrayList<Integer>();
//	private Set<Integer> mALWatchMe = new HashSet<Integer>();

    private List<ChatData> list = new ArrayList<ChatData>();
    private HashMap<Integer, List<ChatData>> MSG = new HashMap<Integer, List<ChatData>>();

//	private ArrayList<JSONObject> _syncVideoList = new ArrayList<JSONObject>();
//	private boolean _isSyncVideo = false;
//	private boolean _isAutoSyncVideo = false;
//	private boolean m_isLocked = false;

    private int m_maxSpeakerCount = 9;
//	private int m_nChairmanID = 0;

//	private int m_hostID = 0;

    private ArrayList<Integer> mALPendingHostList = new ArrayList<Integer>();
    private ArrayList<Integer> mALHostList = new ArrayList<Integer>();

    static private Session mInstance = null;
    private MeetingUserMgr m_thisUserMgr = new MeetingUserMgr();

    //	private boolean m_bWatchWish = true;
    private ArrayList<MyWatch> m_nWatchVideoIDs = new ArrayList<MyWatch>();
    private static volatile Handler applicationHandler = null;


//	private boolean m_bAutoVideoMode = false;
//	private boolean m_bIsbigCameraShowSelf = true;
//	private String sFileforphone = "";

    //	private int m_nScreenSharePeerID = 0;
//	private int m_nWebSharePeerID = 0;
    private int m_nFocusUserPeerID = -1;
    private int m_nFocusVideoID = 0;
    private int videoForSipPeerId = -1;
    private int videoForSipVideoId = 0;
    private boolean m_bServerRecording = false;
//	private boolean bHasFrontCamera = true;

    //xiaoyang add ��¼Ӱ������id��״̬
//	private int moviePlayerId = 0;
//	private boolean moviestate = false;
    //xiaoyang add ��¼Ӱ������id��״̬


//	static String linkUrl = "";
//	static String linkName = "";

    private boolean m_bInmeeting = false;
    //	private String m_strMeetingID;
//	private int createrid;
//	private String m_userIdfaction;
    //private int meetingtype;
    private String m_strMeetingName = "";
    //	private String m_strUserName;
//	private String m_strMeetingCompanyID;
//	private String m_strMeetingPassword;
    private static String sync = "";
    private boolean m_instMeeting;
    //	private boolean m_isCaller;
//	private String m_pwd;
//	private int m_chatid = 0;
    private int m_autoExitWeiyi = 0;
    private Meeting myMeeting = new Meeting();

    public String MEETING_PHP_SERVER = "";
    public static String MEETING_DOC_ADDR = "";
    public static String MEDIA_SERVER_IP = "";
    public static String MEDIA_SERVER_INST_NAME = "";
    public static String MEDIA_SERVER_PORT = "443";
    public static String LIVE_MEDIA_SERVER = "";
    public static String LIVE_MEDIA_PORT = "";
    public static String SIGNAL_SERVER = "";
    public static String SIGNAL_SERVER_PORT = "";


    public static AsyncHttpClient client = new AsyncHttpClient();

    public String webFunBase = "/ClientAPI/";

    public String webFun_getconfig = "";

    public String webFun_checkmeeting = "";

    public String webFun_getmeeting = "";

    public String webFun_requestchairman = "";

    public String webFun_delmeetingfile = "";

    public String webFun_getmeetingfile = "";

    public String webFun_getInvitUsers = "";
    public String webFun_getOnlineNum = "";


    private Activity m_activity;

    private String m_inviteAddress = "";

    private int m_sessionStatus = 0;// 0 idle, 1 connecting, 2 connected

    private int m_bSpeakFree = 1; // ���ɷ���
    private int m_bAllowRecord = 1;// ����¼��
    private int m_bControlFree = 1;// ��������

//	private int m_showInvitation = -1;// Invitation
//	private int m_showWhiteBoard = -1;// WhiteBoard
//	private int m_showChatList = -1;// ChatList
//	private int m_showUserList = -1;// UserList
//	private int m_showDocList = -1;// DocumentList
//	private int m_showApplyChairman = -1;// ApplyChairman
//	private int m_showApplyHost = -1;// ApplyHost
//	private int m_autoOpenAV = -1;// Open audio and video automatically
//	private int m_autoQuit = -1;// Quite meeting automatically

    //	private int m_userType = -1;// ��ʵ�˱���Ӧ�ñ�Ϊcheckmeeting�����Ĳ����������޸ĺ�����������ദ��Ҫ�޸ģ��ʶ���ɱ�����
//	private int m_showVideo = -1;
//	private int m_showAudio = -1;
    /*
	 * chairmancontrol �������� 1111111001001111101000001000000000000000 1.��Ƶ 2.��Ƶ
	 * 3.�װ� 4.������ 5.���� 6.¼�� 7.����Ӱ�� 8.���鵽���Զ��˳�(0:���˳� 1:�˳�) 9.ͶƱ(δʵ��)
	 * 10.�ļ����� 11.���幦��(0:���� 1:����) 12.�ʴ�(δʵ��) 13.������ϯ(0:���� 1:��ʾ) 14.��������(0:����
	 * 1:��ʾ) 15.�ı����� 16.�û��б� 17.�ĵ��б� 18.�Ƿ���ͼ��ȱʡ0����ͼ,1������ͼ������ͼ����ҽ��ϵͳ����ʾ�������ڱߵ���Ƶ
	 * 19.��ҳ���� 20.�Զ��������(0:�Զ� 1:���Զ��� 21.�Ƿ���������Ƶ������(Flash)(0:������ 1:����) 22.sip�绰
	 * 23.H323�����ն˻�MCU 24.�Զ���������Ƶ 25.��˾�����Ƿ�ɼ� 26.�Ƿ���Դ�����˾ 27:�Ƿ�������Ƶ���ڹرհ�ť(Flash
	 * 0:��ʾ 1������) 28:�Ƿ�������Ƶ�����û���(Flash 0:��ʾ 1������) 29:�ȷ���Ƶ����������������(Flash 0:������
	 * 1��������), 30:���̲߳�����Ƶ 0:���߳�(ȱʡ) 1�����̣߳�ANDRIOD��Ҫ�����ֻ��ͺŸ�WEB API��31-40Ԥ������λ
	 */
    private String m_chairmancontrol = "11111110010011111010000010000";
    /* 1.�������������ɷ��� 2.���������˻������Ȩ�� 3. ����������¼�� 4.���ָ��� 5.��Ƶ���� */
    private String m_chairmanfunc = "";
    //	private int m_bSupportSensor = 1;
    // 0 is rotation,1 is heng ping
//	private int m_bSupportRotation = 0;
    // 0�����أ�1�����أ�ȱʡ�ǲ�����
    private int m_hideme = 0;

	/*
	 * private int m_meetingStartTime = 0; private int m_meetingEndTime = 0;
	 * private String detailsId; private String detailsName; private String
	 * detailsChairmanPwd; private String detailsConfuserPwd; private String
	 * detailsSidelineuserPwd;
	 */
//	private boolean m_isLouder = true;

//	private String m_uploadfilename="";

    private boolean m_isViewer = false;
    private WhitePadInterface padInterface;
    private SessionInterface sessionInterface;
    private boolean isWeiYiVirsion = false;
    private boolean noenter = false;
    private int m_isAllowServerRecord = -1;
    private int m_isAutoServerRecord = -1;
    private int m_isSipMeeting = -1;
    //	VodMsgList m_shapeList = null;
//	VodMsgList m_pageList = null;
    //xiaoyang add ��Ӻ���sip��״̬state
    private int callSipState = -1;

    public int getCallSipState() {
        return callSipState;
    }

    public static Handler getApplicationHandler() {
        return applicationHandler;
    }

    public void registerListener(SessionInterface sessionInterface) {
        this.sessionInterface = sessionInterface;
    }

    public WhitePadInterface getPadInterface() {
        return padInterface;
    }

    public void registerWhiteBroad(WhitePadInterface padInterface) {
        this.padInterface = padInterface;
    }


    enum DataTabIndex {
        DocumentShare, // �װ�ҳ 0
        ApplicationShare, // ���湲��ҳ
        WebPageShare, // ��ҳ����ҳ
        CustomTab // �Զ���ҳ ���ɽ������ ����������ҳtab 10
    }

    ;

    public void setChairmanControl(String chairmanControl) {
        m_chairmancontrol = chairmanControl;
    }

    public String getChairmanControl() {
        return m_chairmancontrol;
    }

    public void setChairmanFunc(String chairmanFunc) {
        m_chairmanfunc = chairmanFunc;
    }

    public String getChairmanFunc() {
        return m_chairmanfunc;
    }

//	public void setUserType(int userType) {
//		this.m_userType = userType;
//	}

    /**
     * �Ƿ������Լ� 0�ǲ������Լ���1�������Լ�
     *
     * @return
     */
    public int getM_hideme() {
        return m_hideme;
    }

    public void setM_hideme(int hideme) {
        this.m_hideme = hideme;
    }

    private int m_currentCameraIndex = 0;
    private boolean m_isFrontCamera = true;

//	public int getMoviePlayerId() {
//		return moviePlayerId;
//	}

//	public boolean isMoviestate() {
//		return moviestate;
//	}

    /**
     * �Ƿ������ɷ���
     *
     * @return
     */
    public boolean isSpeakFree() {
        return m_bSpeakFree == 1 ? true : false;
    }

    public void setM_speakFree(boolean bFree) {
        m_bSpeakFree = (bFree == true ? 1 : 0);
    }

    /**
     * �Ƿ�����¼��
     *
     * @return
     */
    public boolean isAllowRecord() {
        return m_bAllowRecord == 1 ? true : false;
    }

    public void setM_allowRecord(boolean bAllowRecrod) {
        m_bAllowRecord = (bAllowRecrod == true ? 1 : 0);
    }

    /**
     * �Ƿ�������
     *
     * @return
     */
    public boolean isControlFree() {
        return m_bControlFree == 1 ? true : false;
    }

    public void setM_controlFree(boolean bFree) {
        m_bControlFree = (bFree == true ? 1 : 0);
    }

//	public void setShowInvitation(boolean bShow) {
//		m_showInvitation = (bShow == true ? 1 : 0);
//	}

//	public void setShowWhiteBoard(boolean bShow) {
//		m_showWhiteBoard = (bShow == true ? 1 : 0);
//	}

//	public void setShowChatList(boolean bShow) {
//		m_showChatList = (bShow == true ? 1 : 0);
//	}

//	public void setShowUserList(boolean bShow) {
//		m_showUserList = (bShow == true ? 1 : 0);
//	}
//
//	public void setShowDocList(boolean bShow) {
//		m_showDocList = (bShow == true ? 1 : 0);
//	}
//
//	public void setShowApplyChairman(boolean bShow) {
//		m_showApplyChairman = (bShow == true ? 1 : 0);
//	}
//
//	public void setShowApplyHost(boolean bShow) {
//		m_showApplyHost = (bShow == true ? 1 : 0);
//	}
//
//	public void setAutoOpenAV(boolean bAuto) {
//		m_autoOpenAV = (bAuto == true ? 1 : 0);
//	}
//
//	public void setAutoQuit(boolean bAuto) {
//		m_autoQuit = (bAuto == true ? 1 : 0);
//	}

//	public void setShowVideo(boolean bAuto){
//		m_showVideo = (bAuto == true ? 1 : 0);
//	}
//	public void setShowAudio(boolean bAuto){
//		m_showAudio = (bAuto == true ? 1 : 0);
//	}

//	public int getM_bSupportRotation() {
//		return m_bSupportRotation;
//	}
//
//	public void setM_bSupportRotation(int bSupportRotation) {
//		this.m_bSupportRotation = bSupportRotation;
//	}

//	/**
//	 * ������
//	 *
//	 * @return
//	 */
//	public int getM_bSupportSensor() {
//		return m_bSupportSensor;
//	}
//
//	public void setM_bSupportSensor(int bSupportSensor) {
//		this.m_bSupportSensor = bSupportSensor;
//	}

    /**
     * �Զ��˳�Զ��ƽ̨
     *
     * @return
     */
    public int getM_autoExitWeiyi() {
        return m_autoExitWeiyi;
    }

    public void setM_autoExitWeiyi(int autoExitWeiyi) {
        this.m_autoExitWeiyi = autoExitWeiyi;
    }

    /**
     * ��ȡ�����ַ
     *
     * @return
     */
    public String getM_inviteAddress() {
        return m_inviteAddress;
    }

    public void setM_inviteAddress(String inviteAddress) {
        this.m_inviteAddress = inviteAddress;
    }

    public void setActivity(Activity activity) {
        m_activity = activity;
    }

    public Activity getActivity() {
        return m_activity;
    }

    /**
     * ���÷������ĵ�ַ
     *
     * @param httpServerAddress
     */
    public void setWebHttpServerAddress(String httpServerAddress) {
        Log.e("emm", "meetingmgr setWebHttpServerAddress=" + httpServerAddress);
        if (httpServerAddress.startsWith("http://"))
            MEETING_PHP_SERVER = httpServerAddress;
        else
            MEETING_PHP_SERVER = "http:" + "/" + "/" + httpServerAddress;

        webFun_checkmeeting = MEETING_PHP_SERVER + webFunBase + "checkmeeting";

        webFun_getInvitUsers = MEETING_PHP_SERVER + webFunBase + "getmeetinguser";

        webFun_getconfig = MEETING_PHP_SERVER + webFunBase + "getconfig";

        Log.e("emm", "setWebHttpServerAddress webconfig=" + webFun_getconfig);

        webFun_getmeeting = MEETING_PHP_SERVER + webFunBase + "getmeeting";

        webFun_requestchairman = MEETING_PHP_SERVER + webFunBase
                + "requestchairmannew";

        webFun_delmeetingfile = MEETING_PHP_SERVER + webFunBase
                + "delmeetingfile";

        webFun_getmeetingfile = MEETING_PHP_SERVER + webFunBase
                + "getmeetingfile";

        webFun_getOnlineNum = MEETING_PHP_SERVER + webFunBase
                + "getonlinenum";

    }

    public String getWebHttpServerAddress() {
        return MEETING_PHP_SERVER;
    }

    static public Session getInstance() {
        synchronized (sync) {
            if (mInstance == null) {
                mInstance = new Session();
            }
            return mInstance;
        }
    }

    public RtmpClientMgr getmRtmpMgr() {
        return mRtmpMgr;
    }

    public void setmRtmpMgr(RtmpClientMgr mRtmpMgr) {
        this.mRtmpMgr = mRtmpMgr;
    }

    public ArrayList<Integer> getmALSpeakerList() {
        return mALSpeakerList;
    }

    public void setmALSpeakerList(ArrayList<Integer> mALSpeakerList) {
        this.mALSpeakerList = mALSpeakerList;
    }

    public ArrayList<Integer> getmALPendingSpeakerList() {
        return mALPendingSpeakerList;
    }

    public void setmALPendingSpeakerList(
            ArrayList<Integer> mALPendingSpeakerList) {
        this.mALPendingSpeakerList = mALPendingSpeakerList;
    }

//	public Set<Integer> getmALWatchMe() {
//		return mALWatchMe;
//	}
//
//	public void setmALWatchMe(Set<Integer> mALWatchMe) {
//		this.mALWatchMe = mALWatchMe;
//	}

    /**
     * ��ȡ����ļ���
     *
     * @return
     */
    public List<ChatData> getList() {
        return list;
    }

    public void setList(List<ChatData> list) {
        this.list = list;
    }

    public HashMap<Integer, List<ChatData>> getMSG() {
        return MSG;
    }

    public void setMSG(HashMap<Integer, List<ChatData>> mSG) {
        MSG = mSG;
    }

//	/**
//	 * ͬ����Ƶ���б�
//	 *
//	 * @return
//	 */
//	public ArrayList<JSONObject> get_syncVideoList() {
//		return _syncVideoList;
//	}
//
//	public void set_syncVideoList(ArrayList<JSONObject> _syncVideoList) {
//		this._syncVideoList = _syncVideoList;
//	}

//	/**
//	 * �Ƿ�ͬ����Ƶ
//	 *
//	 * @return
//	 */
//	public boolean is_isSyncVideo() {
//		return _isSyncVideo;
//	}
//
//	public void set_isSyncVideo(boolean _isSyncVideo) {
//		this._isSyncVideo = _isSyncVideo;
//	}

//	/**
//	 * �Ƿ��Զ�ͬ����Ƶ
//	 *
//	 * @return
//	 */
//	public boolean is_isAutoSyncVideo() {
//		return _isAutoSyncVideo;
//	}
//
//	public void set_isAutoSyncVideo(boolean _isAutoSyncVideo) {
//		this._isAutoSyncVideo = _isAutoSyncVideo;
//	}

	/*
	 * public int getM_ChairmanMode() { return m_ChairmanMode; }
	 *
	 * public void setM_ChairmanMode(int m_ChairmanMode) { this.m_ChairmanMode =
	 * m_ChairmanMode; }
	 */

    /**
     * ��ȡ��෢���˵����� 9
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
     * ��ϯ��id
     *
     * @return
     */
//	public int getM_nChairmanID() {
//		return m_nChairmanID;
//	}
//
//	public void setM_nChairmanID(int m_nChairmanID) {
//		this.m_nChairmanID = m_nChairmanID;
//	}

//	/**
//	 * ������id
//	 *
//	 * @return
//	 */
//	public int getM_hostID() {
//		return m_hostID;
//	}
//
//	public void setM_hostID(int m_hostID) {
//		this.m_hostID = m_hostID;
//	}
    public ArrayList<Integer> getmALPendingHostList() {
        return mALPendingHostList;
    }

    public void setmALPendingHostList(ArrayList<Integer> mALPendingHostList) {
        this.mALPendingHostList = mALPendingHostList;
    }


    public MeetingUserMgr getM_thisUserMgr() {
        return m_thisUserMgr;
    }

    public void setM_thisUserMgr(MeetingUserMgr m_thisUserMgr) {
        this.m_thisUserMgr = m_thisUserMgr;
    }

//	public boolean isM_bWatchWish() {
//		return m_bWatchWish;
//	}
//
//	public void setM_bWatchWish(boolean m_bWatchWish) {
//		this.m_bWatchWish = m_bWatchWish;
//	}

    public ArrayList<MyWatch> getM_nWatchVideoIDs() {
        ArrayList<MyWatch> t = new ArrayList<MyWatch>();
        t.addAll(m_nWatchVideoIDs);
        return t;
    }

    public ArrayList<MyWatch> getWatchVideoIDs() {
        return m_nWatchVideoIDs;
    }

    public void setM_nWatchVideoIDs(ArrayList<MyWatch> m_nWatchVideoIDs) {
        this.m_nWatchVideoIDs = m_nWatchVideoIDs;
    }


//	public boolean isM_bAutoVideoMode() {
//		if (m_autoOpenAV == -1) {
//			if (m_chairmancontrol.length() > 23) {
//				char c = m_chairmancontrol.charAt(23);
//				if (c == '1')
//					return true;
//			}
//		} else {
//			return m_autoOpenAV == 1 ? true : false;
//		}
//
//		return false;
//	}
//
//	public boolean isM_bAutoQuit() {
//		if (m_autoQuit == -1) {
//			if (m_chairmancontrol.length() > 7) {
//				char c = m_chairmancontrol.charAt(7);
//				if (c == '1')
//					return true;
//			}
//		} else {
//			return m_autoQuit == 1 ? true : false;
//		}
//
//		return false;
//	}

    /**
     * @return start time of meeting . The value is the number of milliseconds
     * since Jan. 1, 1970, midnight GMT.
     **/
    public int getMeetingStartTime() {
        if (myMeeting != null)
            return myMeeting.getStartTime();
        return 0;
    }

    /**
     * @return end time of meeting . The value is the number of milliseconds
     * since Jan. 1, 1970, midnight GMT. If the value is 0, means it's a
     * long-term meeting.
     **/
    public int getMeetingEndTime() {
        if (myMeeting != null)
            return myMeeting.getEndTime();
        return 0;
    }

    public String getMeetingName() {
        if (myMeeting != null)
            return myMeeting.getMeetingTopic();
        return null;
    }

    public String getMeetingId() {
        if (myMeeting != null)
            return myMeeting.getMeetingSerialid();
        return null;
    }

    public String getChairmanPwd() {
        if (myMeeting != null)
            return myMeeting.getChairmanpwd();
        return null;
    }

    public String getConfuserPwd() {
        if (myMeeting != null)
            return myMeeting.getConfuserpwd();
        return null;
    }

    public String getSidelineuserPwd() {
        if (myMeeting != null)
            return myMeeting.getSidelineuserpwd();
        return null;
    }

    public int getMeetingType() {
        if (myMeeting != null)
            return myMeeting.getMeetingType();
        return 0;
    }

//	/**
//	 * ��������
//	 *
//	 * @return
//	 */
//	public boolean isM_bShowInvite() {
//		if (m_showInvitation == -1) {
//			if (m_chairmancontrol.length() > 4) {
//				char c = m_chairmancontrol.charAt(4);
//				if (c == '1')
//					return true;
//			}
//		} else {
//			return m_showInvitation == 1 ? true : false;
//		}
//		return false;
//	}

//	/**
//	 * ���ذװ�
//	 *
//	 * @return
//	 */
//	public boolean isM_bShowWhite() {
//		if (m_showWhiteBoard == -1) {
//			if (m_chairmancontrol.length() > 2) {
//				char c = m_chairmancontrol.charAt(2);
//				if (c == '1')
//					return true;
//			}
//		} else {
//			return m_showWhiteBoard == 1 ? true : false;
//		}
//
//		return false;
//	}

//	/**
//	 * �����ı�����
//	 *
//	 * @return
//	 */
//	public boolean isM_bShowTextChat() {
//		if (m_showChatList == -1) {
//			if (m_chairmancontrol.length() > 14) {
//				char c = m_chairmancontrol.charAt(14);
//				if (c == '1')
//					return true;
//			}
//		} else {
//			return m_showChatList == 1 ? true : false;
//		}
//
//		return false;
//	}

//	/**
//	 * �����û��б�
//	 *
//	 * @return
//	 */
//	public boolean isM_bShowUserList() {
//		if (m_showUserList == -1) {
//			if (m_chairmancontrol.length() > 15) {
//				char c = m_chairmancontrol.charAt(15);
//				if (c == '1') {
//					return true;
//				}
//			}
//		} else {
//			return m_showUserList == 1 ? true : false;
//		}
//
//		return false;
//	}
//
//	/**
//	 * �����ĵ��б�
//	 *
//	 * @return
//	 */
//	public boolean isM_bShowDocList() {
//		if (m_showDocList == -1) {
//			if (m_chairmancontrol.length() > 16) {
//				char c = m_chairmancontrol.charAt(16);
//				if (c == '1') {
//					return true;
//				}
//			}
//		} else {
//			return m_showDocList == 1 ? true : false;
//		}
//
//		return false;
//	}
//
//	/**
//	 * ������ϯ
//	 *
//	 * @return
//	 */
//	public boolean isM_bshowChairman() {
//		if (m_showApplyChairman == -1) {
//			if (m_chairmancontrol.length() > 12) {
//				char c = m_chairmancontrol.charAt(12);
//				if (c == '1') {
//					return true;
//				}
//			}
//		} else {
//			return m_showApplyChairman == 1 ? true : false;
//		}
//
//		return false;
//	}
//
//	/**
//	 * ��������
//	 *
//	 * @return
//	 */
//	public boolean isM_bshowHost() {
//		if (m_showApplyHost == -1) {
//			if (m_chairmancontrol.length() > 13) {
//				char c = m_chairmancontrol.charAt(13);
//				if (c == '1') {
//					return true;
//				}
//			}
//		} else {
//			return m_showApplyHost == 1 ? true : false;
//		}
//
//		return false;
//	}

//	/**
//	 * 0:���̲߳�����Ƶ 1:���̲߳�����Ƶ
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
//
//	/**
//	 * �Ƿ���ͼ chairmancontrol 18λ�� ȱʡ0 ��ͼ��1�ǲ���ͼ������ط�1���У�0�ǲ���
//	 *
//	 * @return
//	 */
//	public int isM_scattype() {
//		if (m_chairmancontrol.length() > 17) {
//			char c = m_chairmancontrol.charAt(17);
//			if (c == '1') {
//				return 0;
//			}else if(c == '0'){
//				return 1;
//			}
//		}
//		return 1;
//	}
//	/**
//	 * ������Ƶ
//	 *
//	 * @return
//	 */
//	public boolean isM_bShowVideo() {
//		if (m_showVideo == -1) {
//			if (m_chairmancontrol.length() > 1) {
//				char c = m_chairmancontrol.charAt(1);
//				if (c == '1')
//					return true;
//			}
//		} else {
//			return m_showVideo == 1 ? true : false;
//		}
//
//		return false;
//	}
//	/**
//	 * ������Ƶ
//	 *
//	 * @return
//	 */
//	public boolean isM_bShowAudio() {
//		if (m_showVideo == -1) {
//			if (m_chairmancontrol.length() > 1) {
//				char c = m_chairmancontrol.charAt(1);
//				if (c == '1')
//					return true;
//			}
//		} else {
//			return m_showVideo == 1 ? true : false;
//		}
//
//		return false;
//	}

//	public boolean isM_bAutoRecord() {
//		if (m_chairmancontrol.length() > 31) {
//			char c = m_chairmancontrol.charAt(31);
//			if (c == '1')
//				return true;
//		}
//		return false;
//	}

//	/**
//	 * �Զ���������Ƶ
//	 *
//	 * @return
//	 */
//	public void setM_bAutoVideoMode(boolean m_bAutoVideoMode) {
//		this.m_bAutoVideoMode = m_bAutoVideoMode;
//	}

//	public boolean isM_bIsbigCameraShowSelf() {
//		return m_bIsbigCameraShowSelf;
//	}
//
//	public void setM_bIsbigCameraShowSelf(boolean m_bIsbigCameraShowSelf) {
//		this.m_bIsbigCameraShowSelf = m_bIsbigCameraShowSelf;
//	}

//	public String getsFileforphone() {
//		return sFileforphone;
//	}
//
//	public void setsFileforphone(String sFileforphone) {
//		this.sFileforphone = sFileforphone;
//	}

//	public int getM_nScreenSharePeerID() {
//		return m_nScreenSharePeerID;
//	}
//
//	public void setM_nScreenSharePeerID(int m_nScreenSharePeerID) {
//		this.m_nScreenSharePeerID = m_nScreenSharePeerID;
//	}

    public int getM_nFocusUserPeerID() {
        return m_nFocusUserPeerID;
    }

    public void setM_nFocusUserPeerID(int m_nFocusUserPeerID) {
        this.m_nFocusUserPeerID = m_nFocusUserPeerID;
    }

    public boolean isM_bServerRecording() {
        return m_bServerRecording;
    }

    public void setM_bServerRecording(boolean m_bServerRecording) {
        this.m_bServerRecording = m_bServerRecording;
    }

//	public boolean isbHasFrontCamera() {
//		return bHasFrontCamera;
//	}
//
//	public void setbHasFrontCamera(boolean bHasFrontCamera) {
//		this.bHasFrontCamera = bHasFrontCamera;
//	}

    public boolean isM_bInmeeting() {
        return m_bInmeeting;
    }

    public void setM_bInmeeting(boolean m_bInmeeting) {
        this.m_bInmeeting = m_bInmeeting;
    }

    public String getM_strMeetingID() {
        return myMeeting.getMeetingSerialid();
    }

    public void setM_strMeetingID(String m_strMeetingID) {
        myMeeting.setMeetingSerialid(m_strMeetingID);
    }
//	public int getCreaterid() {
//		return createrid;
//	}
//
//	public void setCreaterid(int createrid) {
//		this.createrid = createrid;
//	}
//	public String getM_userIdfaction() {
//		return m_userIdfaction;
//	}
//
//	public void setM_userIdfaction(String m_userIdfaction) {
//		this.m_userIdfaction = m_userIdfaction;
//	}

    public int getMeetingtype() {
        if (myMeeting != null)
            return myMeeting.getMeetingType();
        return 0;
    }

    public void setMeetingtype(int meetingtype) {
        if (myMeeting != null)
            myMeeting.setMeetingType(meetingtype);
    }

    public void setM_strMeetingName(String meetingName) {
        this.m_strMeetingName = meetingName;
    }

    public String getM_strMeetingName() {
        return m_strMeetingName;
    }

//	public String getM_pwd() {
//		return m_pwd;
//	}
//
//	public void setM_pwd(String m_pwd) {
//		this.m_pwd = m_pwd;
//	}

//	public String getM_strUserName() {
//		return m_strUserName;
//	}
//
//	public void setM_strUserName(String m_strUserName) {
//		this.m_strUserName = m_strUserName;
//	}

//	public boolean isM_isLocked() {
//		return m_isLocked;
//	}
//
//	public void setM_isLocked(boolean m_isLocked) {
//		this.m_isLocked = m_isLocked;
//	}

	/*
	 * public String getM_strServerIP() { return m_strServerIP; }
	 *
	 *
	 * public void setM_strServerIP(String m_strServerIP) { this.m_strServerIP =
	 * m_strServerIP; }
	 *
	 *
	 * public String getM_strServerPort() { return m_strServerPort; }
	 *
	 *
	 * public void setM_strServerPort(String m_strServerPort) {
	 * this.m_strServerPort = m_strServerPort; }
	 */

//	public String getM_strMeetingCompanyID() {
//		return m_strMeetingCompanyID;
//	}
//
//	public void setM_strMeetingCompanyID(String m_strMeetingCompanyID) {
//		this.m_strMeetingCompanyID = m_strMeetingCompanyID;
//	}

//	public String getM_strMeetingPassword() {
//		return m_strMeetingPassword;
//	}
//
//	public void setM_strMeetingPassword(String m_strMeetingPassword) {
//		this.m_strMeetingPassword = m_strMeetingPassword;
//	}

    /**
     * ��ʼ��
     *
     * @param context
     * @param code
     * @param serial
     * @param filelog
     * @return
     */
    public boolean Init(final Context context, final String code,
                        final String serial, final boolean filelog) {
        applicationHandler = new Handler(context.getMainLooper());
//		WeiyiClient.init(context, apphandler);
        if (padInterface != null) {
            padInterface.setAppContext(context);
            padInterface.setClient(client);
        }
        return RtmpClientMgr.getInstance().init(this,
                context, code, serial, filelog);
    }

    // ////////////////////////////////////////////

    /**
     * ���û�����ĳ�˵�ĳ������߶���ķ������������߻��յ�RtmpClientMgrCbk.ClientFunc_Call�ص�
     */
    @Override
    public void ClientFunc_Call(String name, int peerID, Object params) {
        if (name.equals("ClientFunc_WatchBuddyVideo")) {
            ClientFunc_WatchBuddyVideo(peerID);
        } else if (name.equals("ClientFunc_UnWatchBuddyVideo")) {
            ClientFunc_UnWatchBuddyVideo(peerID);
        } else if (name.equals("ClientFunc_DocumentChange")) {
            JSONArray jsons = (JSONArray) params;
            for (int i = 0; i < jsons.length(); i++) {//���ﲻ֪��Ϊʲô�ص���jsonarray��������ѭ��һ��
                JSONObject js = jsons.optJSONObject(i);
                boolean isdel = js.optBoolean("isdel");
                int docId = js.optInt("fileid");
                int pagecount = js.optInt("pagecount");
                String fileName = js.optString("filename");
                String fileUrl = js.optString("fileurl");
                if (padInterface != null) {
                    padInterface.whitePadDocChange(isdel, docId, pagecount, fileName, fileUrl, false);
                }
            }
        } else if (name.equals("ClientFunc_ReceiveText")) {
            JSONArray arr = (JSONArray) params;
            if (arr.length() < 5)
                return;

            try {
                Object format = arr.get(4);
                ClientFunc_ReceiveText(peerID, arr.getString(0),
                        arr.getString(2), arr.getInt(3),
                        (format == JSONObject.NULL || format == null) ? null
                                : (JSONObject) format);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
        } else if (name.equals("ClientFunc_DocsAll")
                || name.equals("ClientFunc_AddDoc")
                || name.equals("ClientFunc_DelDoc")) {
            //cyj 20161029

            int count = peerID;//Here the first param does not mean peerID, but count.
            if (padInterface != null)
                padInterface.setWBPageCount(count);
            if (sessionInterface != null) {
                sessionInterface.onWhitePadPageCount(count);
            }

        } else if (name.equals("ClientFunc_RING")) {
            onCallSipACK(0, 1);
        } else if (name.equals("ClientFunc_ACTIVE")) {
            onCallSipACK(0, 2);
        } else if (name.equals("ClientFunc_HANGUP")) {
            onCallSipACK(0, 3);
        }
        sessionInterface.onCallClientFunction(name, peerID, params);
        Log.i("ClientCall", name);
    }

//	/**
//	 * ����ϯ�߳�����
//	 *
//	 * @param res
//	 */
//	private void ClientFunc_ChairmanKickOut(int res) {
//		NotificationCenter.getInstance().postNotificationName(
//				UI_NOTIFY_USER_KICKOUT, res);
//	}

//	/**
//	 * �ĵ��ı�
//	 */
//	private void ClientFunc_DocumentChange(Object params) {
//		// todo..
//		// m_thisSharePad.handReceivedNotification(
//		// MeetingSession.UI_NOTIFY_USER_WHITE_PAD_DOC_CHANGE, params);
//		NotificationCenter.getInstance().postNotificationName(
//				Session.UI_NOTIFY_USER_WHITE_PAD_DOC_CHANGE, params);
//	}

    /**
     * �������User���Է����仯
     */
    @Override
    public void ClientFunc_ClientProperty(JSONObject arg0) {
        try {
            int nPeerID = arg0.getInt("id");
            MeetingUser mu = Session.getInstance().getUserMgr().getUser(nPeerID);
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
                        //xiaoyang change ���ݷ����仯
                        for (int i = 0; i < m_nWatchVideoIDs.size(); i++) {
                            if (m_nWatchVideoIDs.get(i).getPeerid() == nPeerID) {
                                Session.getInstance().unplayVideo(nPeerID, m_nWatchVideoIDs.get(i).getCameraid());
                                m_nWatchVideoIDs.remove(i);
                            }
                        }


                        //						mapVideoIDs.remove(Integer.valueOf(nPeerID));
                        //xiaoyang change
                        bWhoIWatchClosed = true;
                    }
//					NotificationCenter.getInstance().postNotificationName(
//							Session.UI_NOTIFY_USER_VEDIO_CHANGE,
//							nPeerID, bWhoIWatchClosed);
                }
            }
            //xiaoyang add
            if (arg0.has("m_MutliCamera")) {
                JSONArray jsonCamreas = arg0.optJSONArray("m_MutliCamera");
                for (int i = 0; i < jsonCamreas.length(); i++) {
                    JSONObject jscam = jsonCamreas.optJSONObject(i);
                    int cIndex = jscam.optInt("m_CameraIndex");
                    String cName = jscam.optString("m_CameraName");
                    boolean cEnable = jscam.optBoolean("m_CameraEnable");
                    boolean cDefault = jscam.optBoolean("m_DefaultCamera");
                    mu.addCamera(cIndex, cName, cEnable, cDefault);
                }
            }
            for (int i = 0; i < m_nWatchVideoIDs.size(); i++) {
                for (Integer in : mu.getMapCamera().keySet()) {
                    if (m_nWatchVideoIDs.get(i).getPeerid() == nPeerID
                            && m_nWatchVideoIDs.get(i).getCameraid() == mu.getMapCamera().get(in).getVideoIndex()
                            && !mu.getMapCamera().get(in).isEnable()) {
                        Session.getInstance().unplayVideo(nPeerID, m_nWatchVideoIDs.get(i).getCameraid());
                        Session.getInstance().unwatchOtherVideo(nPeerID, m_nWatchVideoIDs.get(i).getCameraid());
                        m_nWatchVideoIDs.remove(i);
                    }
                }
            }
            sessionInterface.onUserPropertyChange(nPeerID, arg0);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.i("ClientFunc_Clie", arg0.toString());

    }

    /**
     * �������֮����ã���onconnect֮��ص�
     */
    @Override
    public void ClientFunc_EnablePresence(JSONObject arg0) {
        try {
            Session.getInstance().setM_bInmeeting(true);
            int myPID = arg0.getInt("m_MeetBuddyID");
            getUserMgr().getSelfUser().setPeerID(myPID);
            sessionInterface.onEnablePresence(myPID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * ��������߽������
     */
    @Override
    public void ClientFunc_UserIn(JSONObject arg0) {
        onUserIn(arg0, false);

    }

    /**
     * ����һ������߽������
     */
    @Override
    public void ClientFunc_UserInList(JSONArray arg0) {
        MeetingUser mu = m_thisUserMgr.getSelfUser();
        if (mu == null)
            return;
        for (int i = 0; i < arg0.length(); i++) {
            JSONObject var = null;
            try {
                var = arg0.getJSONObject(i);
                int buddyID = 0;
                int m_ClientType = 0;
                if (var != null && var.has("m_BuddyID")) {
                    buddyID = var.getInt("m_BuddyID");
                }
                if (var != null && var.has("m_ClientType")) {
                    m_ClientType = var.optInt("m_ClientType");
                }
//				if (buddyID != 0 && buddyID == mu.getThirdID()) {
//					JSONArray arr = new JSONArray();
//					arr.put(0, null);
//					arr.put(1, "ClientFunc_ChairmanKickOut");
//					arr.put(2, var.getInt("m_MeetBuddyID"));//xiaoyang 5��18�޸Ļ��߻����ж˻���
//					arr.put(3, 1);
//
//					RtmpClientMgr.getInstance().callServerFunction(
//							"ServerFunc_CallClientFunc", arr);
//				}

            } catch (JSONException e) {
                e.printStackTrace();
            }
            onUserIn(var, true);
        }

        if (this.isM_instMeeting()) {
            getUserMgr().reSort();

        }
    }

    @Override
    public void ClientFunc_UserOut(int arg0) {
        MeetingUser mu = Session.getInstance().getUserMgr().getMeetingUser(arg0);

        boolean bWatchedLeave = false;
        if (mu == null) {
            return;
        }
        if (mu.getWatch()) {
            //			m_nWatchVideoIDs.remove(Integer.valueOf(mu.getPeerID()));
            //xiaoyang change
            ArrayList<MyWatch> tempremove = new ArrayList<MyWatch>();
            for (int i = 0; i < m_nWatchVideoIDs.size(); i++) {
                if (m_nWatchVideoIDs.get(i).getPeerid() == Integer.valueOf(mu.getPeerID())) {
                    Session.getInstance().unplayVideo(m_nWatchVideoIDs.get(i).getPeerid(), m_nWatchVideoIDs.get(i).getCameraid());
                    tempremove.add(m_nWatchVideoIDs.get(i));
//					m_nWatchVideoIDs.remove(i);
                }
            }
            for (int i = 0; i < tempremove.size(); i++) {
                m_nWatchVideoIDs.remove(tempremove.get(i));
            }
            tempremove.clear();


            //			mapVideoIDs.remove(Integer.valueOf(mu.getPeerID()));
            //xiaoyang change
            bWatchedLeave = true;
        }
        try {
            MeetingUser muclon = (MeetingUser) mu.clone();
            Session.getInstance().getUserMgr().delUser(arg0);
            sessionInterface.onUserOut(muclon);
        } catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * ���ӷ�����
     */
    @Override
    public void onConnect(int arg0, int quality) {
        // TODO Auto-generated method stub
        if (arg0 == 0) {
            m_sessionStatus = 2;
            JSONObject parameters = new JSONObject();
            // RtmpClientMgr.getInstance().ClientFunc_Call("ServerFunc_EnablePresence",
            // parameters);
//			if(padInterface!=null){
//				padInterface.setWebImageDomain(RtmpClientMgr.getInstance().MEETING_DOC_ADDR);
//				padInterface.getMeetingFile(Integer.valueOf(myMeeting.getMeetingSerialid()), webFun_getmeetingfile);
//			}
        } else {
            m_bInmeeting = false;
            m_sessionStatus = 0;


            clear();
        }
        sessionInterface.onConnect(arg0, quality);

    }

    /**
     * ����ʧ��
     */
    @Override
    public void onDisconnect(int arg0) {

        m_bInmeeting = false;
        m_sessionStatus = 0;

        if (arg0 != 0) {
            // clear();
            this.getUserMgr().getSelfUser().clear();

        } else {
            //			NotificationCenter.getInstance().postNotificationName(
            //					Session.NET_CONNECT_LEAVE);
//			if (this.isM_instMeeting())
//				NotificationCenter.getInstance().postNotificationName(
//						Session.EXIT_MEETING);
//			if (getM_autoExitWeiyi() == 1) {
//				Log.e("emm", "auto exit meeting***************");
//				NotificationCenter.getInstance().postNotificationName(
//						Session.AUTO_EXIT_MEETING);
//			}
//			clear();
        }
        sessionInterface.onDisConnect(arg0);
        // NotificationCenter.getInstance().removeAllObservers();
        // m_thisUserMgr.mMeetingUserSelf = null;
    }

    @Override
    public void Remote_Msg(boolean arg0, JSONObject arg1, byte[] arg2) {
        boolean Handle = false;

        try {
            if (arg0)
                Handle = Remote_PubMsg_i(arg1);
            else
                Handle = Remote_DelMsg_i(arg1);
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        //Log.i("Remote_MsgRes", arg1.toString());

        if (Handle)
            return;

        whitePadChange(arg1, arg2, arg0);

    }

    /**
     * ��������ͷ
     */
    @Override
    public void onCameraDidOpen(Camera cam, boolean isFront, int index) {
        m_currentCameraIndex = index;
//		NotificationCenter.getInstance().postNotificationName(
//				Session.VIDEO_NOTIFY_CAMERA_DID_OPEN, cam, isFront,
//				index);
        if (sessionInterface != null) {
            sessionInterface.onCameraDidOpen(cam, isFront, index);
        }
    }

    /**
     * �ر�����ͷ
     */
    @Override
    public void onCameraWillClose(Camera cam) {
//		NotificationCenter.getInstance().postNotificationName(
//				Session.VIDEO_NOTIFY_CAMERA_WILL_CLOSE, cam);
        if (sessionInterface != null) {
            sessionInterface.onCameraWillClose(cam);
        }
    }

    /**
     * �������
     */
    @Override
    public void onPhotoTaken(boolean success, byte[] data) {
//		NotificationCenter.getInstance().postNotificationName(
//				Session.UI_NOTIFY_USER_PICTURE_TAKEN, success, data);
        if (sessionInterface != null) {
            sessionInterface.onPhotoTaken(success, data);
        }
    }

    /**
     * ��ʾ����Ȩ��
     */
    @Override
    public void onWarning(int warning) {
        if (sessionInterface != null) {
            sessionInterface.onWarning(warning);
        }
    }

    @Override
    public void onPresentComplete() {
        if (sessionInterface != null) {
            sessionInterface.onPresentComplete();
        }
    }

    // /////////////////////////////////////////////
    public void PublishMessage(String name, int toID, int assocaiteUserID) {
        PublishMessage(name, toID, assocaiteUserID, null, "", "");
    }

    public void PublishMessage(String name, int toID, int assocaiteUserID,
                               JSONObject body) {
        PublishMessage(name, toID, assocaiteUserID, body, "", "");
    }

    public void PublishMessage(String name, int toID, int assocaiteUserID,
                               JSONObject body, String id) {
        PublishMessage(name, toID, assocaiteUserID, body, id, "");
    }

    /**
     * ��Ϣ����
     *
     * @param name
     * @param toID
     * @param assocaiteUserID
     * @param body
     * @param id
     * @param associatedMsgID
     */
    public void PublishMessage(String name, int toID, int assocaiteUserID,
                               Object body, String id, String associatedMsgID) {
        JSONObject params = new JSONObject();
        try {
            params.put("senderID", this.getMyPID());
            params.put("toID", toID);
            params.put("name", name);
            if (body != null)
                params.put("body", body);
            if (assocaiteUserID != 0)
                params.put(ASSOCAITEUSERID, assocaiteUserID);
            if (!id.isEmpty())
                params.put("id", id);
            else
                params.put("id", name);
            if (!associatedMsgID.isEmpty())
                params.put("associatedMsgID", associatedMsgID);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        RtmpClientMgr.getInstance().remoteMsg(true, params, new byte[]{});

    }

    /**
     * ͨ�������ƴ�����Ϣ��������
     *
     * @param name
     * @param toID
     * @param assocaiteUserID
     * @param body
     * @param id
     * @param associatedMsgID
     */
    public void PublishMessageBinaryBody(String name, int toID,
                                         int assocaiteUserID, byte[] body, String id, String associatedMsgID) {
        JSONObject params = new JSONObject();
        try {
            params.put("senderID", this.getMyPID());
            params.put("toID", toID);
            params.put("name", name);
            if (assocaiteUserID != 0)
                params.put(ASSOCAITEUSERID, assocaiteUserID);
            if (!id.isEmpty())
                params.put("id", id);
            else
                params.put("id", name);
            if (!associatedMsgID.isEmpty())
                params.put("associatedMsgID", associatedMsgID);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // {"id":"RequestSpeak138700","senderID":138700,"assocaiteUserID":138700,"name":"RequestSpeak","toID":-1}

        RtmpClientMgr.getInstance().remoteMsg(true, params, body);

    }

    public void DeleteMessage(String name, int toID, int assocaiteUserID) {
        DeleteMessage(name, toID, assocaiteUserID, null, "", "");
    }

    public void DeleteMessage(String name, int toID, int assocaiteUserID,
                              JSONObject body) {
        DeleteMessage(name, toID, assocaiteUserID, body, "", "");
    }

    public void DeleteMessage(String name, int toID, int assocaiteUserID,
                              JSONObject body, String id) {
        DeleteMessage(name, toID, assocaiteUserID, body, id, "");
    }

    /**
     * ɾ����Ϣ
     *
     * @param name
     * @param toID
     * @param assocaiteUserID
     * @param body
     * @param id
     * @param associatedMsgID
     */
    public void DeleteMessage(String name, int toID, int assocaiteUserID,
                              JSONObject body, String id, String associatedMsgID) {
        JSONObject params = new JSONObject();
        try {
            params.put("senderID", this.getMyPID());
            params.put("toID", toID);
            params.put("name", name);
            if (body != null)
                params.put("body", body);
            if (assocaiteUserID != 0)
                params.put(ASSOCAITEUSERID, assocaiteUserID);
            if (!id.isEmpty())
                params.put("id", id);
            else
                params.put("id", name);
            if (!associatedMsgID.isEmpty())
                params.put("associatedMsgID", associatedMsgID);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        RtmpClientMgr.getInstance().remoteMsg(false, params, new byte[]{});

    }

    /**
     * ֪ͨ
     *
     * @param name
     * @param toID
     * @param body
     */
    //	public void Notify(String name, int toID, String body) {
    //		JSONObject parameters = new JSONObject();
    //
    //		try {
    //			parameters.put("1", name);
    //			parameters.put("2", toID);
    //			parameters.put("3", body);
    //		} catch (JSONException e) {
    //			// TODO Auto-generated catch block
    //			e.printStackTrace();
    //		}
    //
    //		RtmpClientMgr.getInstance().callClientFunction(toID,
    //				"ServerFunc_CallClientFunc", parameters);
    //	}

    /**
     * ��ȡʱ��
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
     * ����û�
     *
     * @param userinfo
     */
    private void checkHasUser(JSONObject userinfo) {
        try {
            userinfo.put("m_EnablePresence", true);
            userinfo.put("m_HideSelf", true);
            int uid = userinfo.optInt("m_MeetBuddyID");
            String nickName = "";
            if (userinfo.has("m_NickName"))
                nickName = userinfo.optString("m_NickName");
            MeetingUser user = m_thisUserMgr.getMeetingUser(uid);
            int myrole = m_thisUserMgr.getSelfUser().getRole();
            if (myrole == 2 && user != null && !nickName.isEmpty()
                    && user.getName() != nickName)
                user.setName(nickName);

            if (user != null || uid == 0)
                return;

            onUserIn(userinfo, false);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * �ı���Ϣ
     *
     * @param fromID
     * @param text
     * @param userName
     * @param msgType
     * @param format
     */
    private void ClientFunc_ReceiveText(int fromID, String text,
                                        String userName, int msgType, JSONObject format) {
        String nameText;

        MeetingUser user = Session.getInstance().getUserMgr().getUser(fromID);
        if (user != null) {
            //				userName = user.getName();
            user.setUnreadMsg(user.getUnreadMsg() + 1);
        } else {
            try {
                JSONObject obj = new JSONObject();
                obj.put("m_MeetBuddyID", fromID);
                obj.put("m_NickName", userName);
                checkHasUser(obj);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        ChatData data = new ChatData();

        data.setPersonal(msgType != 0);
        data.setType(ChatData.Type.receive);
        data.setContent(text);
        if (user != null && user.getName() != null && !user.getName().isEmpty()) {
            data.setName(user.getName());
        } else {
            data.setName(userName);
        }
        data.setTime(getTime());
        data.setnFromID(fromID);
        list.add(data);
        MSG.put(fromID, list);
        if (sessionInterface != null) {
            sessionInterface.onRecTextMsg(fromID, msgType, text, format);
        }
    }

//	/**
//	 * ��ʼ���ɷ���
//	 */
//	public void StartSpeaking() {
//		String userID = "RequestSpeak" + getMyPID();
//		boolean force = m_thisUserMgr.getSelfUser().isChairMan();
//		// remote_pubmsg_i
//		PublishMessage("RequestSpeak", SENDMSGTOALL, getMyPID(), force, userID,
//				"");
//		// PublishMessage("RequestSpeak", SENDMSGTOALL, getMyPID(), null, id,
//		// "");
//
//	}

    /**
     * ȡ�����ɷ���
     */
    public void StopSpeaking() {
        // remote_delmsg_i
        String id = "RequestSpeak" + getMyPID();
        DeleteMessage("RequestSpeak", SENDMSGTOALL, getMyPID(), null, id, "");
    }

    /**
     * �鿴������Ƶ
     *
     * @param userID
     */
    public void watchOtherVideo(int userID, int videoid) {
        MeetingUser mu = m_thisUserMgr.getMeetingUser(userID);
        if (mu == null)
            return;

        if (getMyPID() != userID) {
            JSONArray arr = new JSONArray();
            try {

                arr.put(0, null);
                arr.put(1, "ClientFunc_WatchBuddyVideo");
                arr.put(2, userID);
                arr.put(3, videoid);

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            RtmpClientMgr.getInstance().callServerFunction(
                    "ServerFunc_CallClientFunc", arr);
        }
    }

    /**
     * ����������Ƶ
     *
     * @param userID
     */
    public void unwatchOtherVideo(int userID, int videoID) {
        MeetingUser mu = m_thisUserMgr.getMeetingUser(userID);
        if (mu == null)
            return;
        if (getMyPID() != userID) {
            JSONArray arr = new JSONArray();
            try {

                arr.put(0, null);
                arr.put(1, "ClientFunc_UnWatchBuddyVideo");
                arr.put(2, userID);
                arr.put(3, videoID);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            RtmpClientMgr.getInstance().callServerFunction(
                    "ServerFunc_CallClientFunc", arr);
        }
    }


    // ////////////////////////////////////////////////////////////

    /**
     * �û��������
     *
     * @param userinfo
     * @param binList
     */
    public void onUserIn(JSONObject userinfo, boolean binList) {
        MeetingUser mu = null;
        try {

            if (!userinfo.optBoolean("m_EnablePresence"))
                return;
            mu = new MeetingUser();

            if (userinfo.has("m_ClientType"))
                mu.setClientType(userinfo.optInt("m_ClientType"));
            mu.setPeerID(userinfo.optInt("m_MeetBuddyID"));
            //�����������sip�û���m_telNumber����
            if (userinfo.has("m_telNumber")) {
                mu.setM_telNumber(userinfo.optString("m_telNumber"));
            }
            if (userinfo.has("m_NickName"))
                mu.setName(userinfo.optString("m_NickName"));
            if (userinfo.has("m_UserType")) {
                mu.setRole(userinfo.optInt("m_UserType"));
                if (mu.getRole() == 1) {
//					m_nChairmanID = mu.getPeerID();
                } else if (mu.getRole() == 2) {

                }
            }
            if (userinfo.has("m_BuddyID")) {
                mu.setThirdID(userinfo.optInt("m_BuddyID"));
            }
            if (userinfo.has("m_HasVideo"))
                mu.sethasVideo(userinfo.optBoolean("m_HasVideo")
                        && mu.getRole() != 2);
            if (userinfo.has("m_HasAudio"))
                mu.sethasAudio(userinfo.optBoolean("m_HasAudio"));


            //xiaoyang test
            //			mu.addCamera(1, "1", true, false);
            //			mu.addCamera(2, "2", false, false);
            //			mu.addCamera(3, "3", true, true);
            //xiaoyang test
            // chen ji todo..,connectserver��ʱ����Ҫ�����Ƿ������Լ��Ĳ���
            if (userinfo.has("m_HideSelf")) {
                boolean hideme = userinfo.optBoolean("m_HideSelf");
                if (hideme)
                    mu.setHide(1);
                else
                    mu.setHide(0);
            }

            //xiaoyang add
            if (userinfo.has("m_MutliCamera")) {
                JSONArray jsonCamreas = userinfo.optJSONArray("m_MutliCamera");
                for (int i = 0; i < jsonCamreas.length(); i++) {
                    JSONObject jscam = jsonCamreas.optJSONObject(i);
                    int cIndex = jscam.optInt("m_CameraIndex");
                    String cName = jscam.optString("m_CameraName");
                    boolean cEnable = jscam.optBoolean("m_CameraEnable");
                    boolean cDefault = jscam.optBoolean("m_DefaultCamera");
                    mu.addCamera(cIndex, cName, cEnable, cDefault);
                }
            }
            //xiaoyang add


            // ����ǵ绰�����飬�Զ�������Ƶ
            if (mu != null && (mu.getClientType() == 4 || mu.getClientType() == 7)) {
                mu.setAudioStatus(RequestSpeak_Allow);
                playAudio(mu.getPeerID());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mu != null) {
            Session.getInstance().getUserMgr().addUser(mu);
            sessionInterface.onUserIn(mu.getPeerID(), binList);
        }
    }

    /**
     * Զ����Ϣ�б�
     *
     * @param response
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void Remote_MsgList(JSONArray response) {

        ArrayList<JSONObject> msgList = new ArrayList<JSONObject>();
        for (int i = 0; i < response.length(); i++) {
            JSONObject var = null;
            try {
                var = response.getJSONObject(i);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            msgList.add(var);
        }
        Collections.sort(msgList, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                int nCom = 0;
                try {
                    nCom = ((JSONObject) o1).getInt("seq")
                            - ((JSONObject) o2).getInt("seq");
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return nCom;
            }
        });
        try {
            ArrayList<JSONObject> msgListLeft = new ArrayList<JSONObject>();
            for (int i = 0; i < msgList.size(); i++) {
                JSONObject msg = msgList.get(i);

                String strName = msg.getString("name");

                if (strName.equals("RequestSpeak")) {
                    if (!msg.getBoolean("body")) {
                        continue;
                    }
                    Remote_PubMsg_i(msg);
                }
            }

            for (int i = 0; i < msgListLeft.size(); i++) {
                JSONObject msg = msgListLeft.get(i);
                Remote_PubMsg_i(msg);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void Remote_PubMsg(JSONObject response) throws JSONException {
        Remote_PubMsg_i(response);
    }

    public void Remote_DelMsg(JSONObject response) throws JSONException {
        Remote_DelMsg_i(response);
    }

    /**
     * �鿴���˵���Ƶ
     *
     * @param nSenderid
     */
    public void ClientFunc_WatchBuddyVideo(int nSenderid) {

        // mALWatchMe.add(Integer.valueOf(nSenderid));
        // if(mALWatchMe.size()>0&&m_bWatchWish)
        // this.publishVideo();
        publishVideo();
        // m_watchMeCount++;
    }

    /**
     * ���鿴���˵���Ƶ
     *
     * @param nSenderid
     */
    public void ClientFunc_UnWatchBuddyVideo(int nSenderid) {

        // mALWatchMe.remove(Integer.valueOf(nSenderid));
        // if(mALWatchMe.size()==0)
        // this.unpublishVideo();
        // m_watchMeCount--;
        // if (m_watchMeCount == 0)
        // unpublishVideo();
    }

    // /////////////////////////////////////////////////////////////////////

    // private void publishMessage(String name,int toID,int
    // associatedUserID,JSONObject body,String id,String associatedMsgID){
    // JSONObject msg =
    // createMessage(name,toID,associatedUserID,body,id,associatedMsgID);
    //
    // RtmpClientMgr.getInstance().ClientFunc_Call("Remote_PubMsg",msg);
    // }
    // private void deleteMessage(String name,int toID,int
    // associatedUserID,JSONObject body,String id,String associatedMsgID){
    // JSONObject msg =
    // createMessage(name,toID,associatedUserID,body,id,associatedMsgID);
    //
    // RtmpClientMgr.getInstance().ClientFunc_Call("Remote_DelMsg",msg);
    // }
    private JSONObject createMessage(String name, int toID,
                                     int associatedUserID, JSONObject body, String id,
                                     String associatedMsgID) {
        JSONObject msg = new JSONObject();

        try {
            msg.put("senderID", getMyPID());
            msg.put("toID", toID);
            msg.put("name", name);

            if (associatedUserID != 0)
                msg.put(ASSOCAITEUSERID, associatedUserID);
            if (body != null)
                msg.put("body", body);
            if (id.length() != 0)
                msg.put("id", id);
            else
                msg.put("id", name);
            if (associatedMsgID.length() != 0)
                msg.put("associateMsgID", associatedMsgID);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return msg;

    }

    /**
     * Զ����Ϣ
     *
     * @param msg
     * @return
     * @throws JSONException
     */
    private boolean Remote_PubMsg_i(JSONObject msg) throws JSONException {
        Object body = msg.opt("body");
        String msgName = msg.optString("name");
        String msgid = msg.optString("id");
        String associatedMsgID = msg.optString("associatedMsgID");
        EmmLog.d("emm", msgName);
        int sendID = 0;

        if (msg.has("senderID")) {
            sendID = msg.getInt("senderID");
        }
        int associatedUserID = 0;
        if (msg.has(ASSOCAITEUSERID)) {
            associatedUserID = msg.getInt(ASSOCAITEUSERID);
            JSONObject obj = new JSONObject();
            obj.put("m_MeetBuddyID", associatedUserID);
            checkHasUser(obj);
        }

        boolean bHandel = true;

        Log.e("Remote_PubMsg", msgName);
        if (msgName.equals("RequestSpeak")) {
            if (isWeiYiVirsion) {
                bHandel = false;
            } else {

                int nCount = getSpeakerCount();
                int maxSpeakCount = getMaxSpeakerCount();
                if (nCount < maxSpeakCount) {
                    addSpeaker(associatedUserID);
                    ChangeAudioStatus(associatedUserID, RequestSpeak_Allow);

                } else {
                    mALPendingSpeakerList.add(associatedUserID);
                    addPendingSpeaker(associatedUserID);
                    ChangeAudioStatus(associatedUserID,
                            RequestSpeak_Pending);
                }
                NewFreeSpeak();
            }
        } else if (msgName.equals("StartRecording")) {
            onServerRecording(true);
        } else if (msgName.equals("LiveUserChange")) {
            JSONObject js = (JSONObject) body;
            int videoid = 0;
            if (js.get("videoID") instanceof Double) {
                double temp = js.getDouble("videoID");
                videoid = (int) temp;
            } else if (js.get("videoID") instanceof Integer) {
                videoid = js.getInt("videoID");
            }
            onFocusUserChange(associatedUserID, videoid);
        } else if (msgName.equals("VideoForSip")) {
            JSONObject js = (JSONObject) body;
            int videoid = 0;
            if (js.get("videoID") instanceof Double) {
                double temp = js.getDouble("videoID");
                videoid = (int) temp;
            } else if (js.get("videoID") instanceof Integer) {
                videoid = js.getInt("videoID");
            }
            onVideoForSipChange(associatedUserID, videoid);
        } else {
            bHandel = false;
        }
        if (!bHandel) {
            sessionInterface.onRemotePubMsg(msgName, sendID, associatedUserID, msgid, associatedMsgID, body);
        }
        return bHandel;
    }

//	/**
//	 * ͬ����Ƶģʽ�ı�
//	 *
//	 * @param mode
//	 * @param auto
//	 */
//	private void syncVideoModeChange(boolean mode, boolean auto) {
//		_isSyncVideo = mode;
//		_isAutoSyncVideo = auto;
//		_syncVideoList.clear();
//
//		NotificationCenter.getInstance().postNotificationName(
//				FaceShareControl.UI_NOTIFY_USER_SYNC_VIDEO_MODE_CHANGE, _isSyncVideo,
//				_isAutoSyncVideo);
//	}

//	/**
//	 * �Ƿ�ͬ����Ƶ
//	 *
//	 * @return
//	 */
//	public boolean isSyncVideo() {
//		return _isSyncVideo;
//	}

//	/**
//	 * �鿴ͬ����Ƶ
//	 *
//	 * @param peerID
//	 * @param videoID
//	 * @param open
//	 */
//	private void syncWatchVideo(int peerID, int videoID, boolean open) {
//		if (!_isSyncVideo)
//			return;
//		MeetingUser mu = null;
//		if (peerID == getMyPID())
//			mu = m_thisUserMgr.getSelfUser();
//		else
//			mu = m_thisUserMgr.getMeetingUser(peerID);
//		if (mu == null)
//			return;
//
//		if (open) {
//			if (peerID == getMyPID()) {
//				// m_watchMeCount = m_thisUserMgr.getCount();
//				publishVideo();
//			}
//		}
//		// else
//		// {
//		// if( peerID == getMyPID() )
//		// {
//		// m_watchMeCount = 0;
//		// unpublishVideo();
//		// }
//		// }
//		NotificationCenter.getInstance().postNotificationName(
//				UI_NOTIFY_USER_SYNC_WATCH_VIDEO, peerID, videoID, open);
//	}

//	/**
//	 * ���ͬ������Ƶ
//	 *
//	 * @param obj
//	 */
//	private void addSyncVideo(JSONObject obj) {
//		_syncVideoList.add(obj);
//	}
//
//	/**
//	 * �Ƴ�ͬ������Ƶ
//	 *
//	 * @param obj
//	 * @throws JSONException
//	 */
//	private void delSyncVideo(JSONObject obj) throws JSONException {
//		int peerID = obj.getInt("userID");
//		int videoID = obj.getInt("videoID");
//
//		for (int i = 0; i < _syncVideoList.size(); i++) {
//			JSONObject temp = _syncVideoList.get(i);
//			if (peerID == temp.getInt("userID")
//					&& videoID == temp.getInt("videoID")) {
//				_syncVideoList.remove(i);
//				break;
//			}
//		}
//	}

//	public int getChairManID() {
//		return m_nChairmanID;
//	}

    /**
     * ɾ��Զ����Ϣ
     *
     * @param msg
     * @return
     * @throws JSONException
     */
    private boolean Remote_DelMsg_i(JSONObject msg) throws JSONException {
        Object body = msg.get("body");
        String msgName = msg.getString("name");
        int senderid = msg.optInt("senderID");
        String id = msg.optString("id");
        String associatedMsgID = msg.optString("associatedMsgID");
        int associatedUserID = 0;
        if (msg.has(ASSOCAITEUSERID))
            associatedUserID = msg.getInt(ASSOCAITEUSERID);

        boolean bHandel = true;

        Log.i("emm", "Remote_DelMsg_i msg=" + msgName);
        if (msgName.equals("RequestSpeak")) {
            if (isWeiYiVirsion) {
                bHandel = false;
            } else {
                ChangeAudioStatus(associatedUserID, RequestSpeak_Disable);
                delSpeaker(associatedUserID);
                delPendingSpeaker(associatedUserID);
//				NewFreeSpeak();
            }
        } else if (msgName.equals("StartRecording")) {
            onServerRecording(false);
        } else if (msgName.equals("LiveUserChange")) {
            onFocusUserChange(0, 0);
        } else {
            bHandel = false;
        }
        sessionInterface.onRemoteDelMsg(msgName, senderid, associatedUserID, id, associatedMsgID, body);
        return bHandel;
    }

//	/**
//	 * ����ģʽ�����仯
//	 *
//	 * @param UserID
//	 * @param Status
//	 * @param bNotify
//	 */
//	private void ControlStatusChange(int UserID, int Status, boolean bNotify) {
//		// RequestHost_Disable = 0;RequestHost_Allow = 1;RequestHost_Pending =
//		// 2;
//		MeetingUser mu = null;
//		if (UserID == getMyPID())
//			mu = m_thisUserMgr.getSelfUser();
//		else
//			mu = m_thisUserMgr.getMeetingUser(UserID);
//		if (mu == null)
//			return;
//
//		/*
//		 * int oldStatus = mu.getHostStatus(); switch (oldStatus) { case 1:
//		 * break; case 2: { delPendingHost(UserID); break; } }
//		 */
//
//		switch (Status) {
//		case 0:
//			delHost(UserID);
//			delPendingHost(UserID);
//			break;
//		case 1:
//			addHost(UserID);
//			break;
//		case 2:
//			addPendingHost(UserID);
//			break;
//		}
//
//		mu.setHostStatus(Status);
//		m_thisUserMgr.reSort();
//		if (bNotify) {
//			if(UserID == getMyPID()&&m_thisUserMgr.getSelfUser().getHostStatus()==RequestHost_Allow){
//				if(padInterface!=null)
//					padInterface.setControlMode(ControlMode.fullcontrol);
//			}else{
//				if(padInterface!=null)
//					padInterface.setControlMode(ControlMode.watch);
//			}
//			NotificationCenter.getInstance().postNotificationName(
//					UI_NOTIFY_USER_HOSTSTATUS, UserID, Status);
//
//			if (RequestHost_Allow == Status) {
//				if (this.getM_hostID() != UserID) {
//					NotificationCenter.getInstance().postNotificationName(
//							UI_NOTIFY_USER_HOST_CHANGE, UserID, Status);
//					this.setM_hostID(UserID);
//				}
//			}
//		}
//	}

    /**
     * ������������������ˣ�ʹ��Ϊ����
     *
     * @param peerID
     */
    private void addPendingHost(int peerID) {
        if (!mALPendingHostList.contains(peerID)) {
            mALPendingHostList.add(peerID);
        }
    }

    /**
     * ɾ�����������������ˣ�
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
     * ɾ������
     *
     * @param peerID
     */
    private void delHost(int peerID) {
        if (mALHostList.contains(peerID)) {
            mALHostList.remove(mALHostList.indexOf(peerID));
        }
    }

//	/**
//	 * ɾ�����е�����
//	 */
//	private void delAllHost() {
//		for (int i = 0; i < mALHostList.size(); i++) {
//			int hostid = mALHostList.get(i);
//			if (hostid != this.getChairManID()) {
//				ControlStatusChange(hostid, RequestHost_Disable, false);
//				if (hostid == getMyPID()) {
//					MeetingUser self = this.getUserMgr().getMeetingUser(hostid);
//					if (self.getHostStatus() == RequestHost_Allow) {
//						DeleteMessage("RequestControl", SENDMSGTOALL_EXCEPT_ME,
//								hostid, null, "RequestControl" + hostid);
//					}
//				}
//			}
//		}
//		mALHostList.clear();
//		MeetingUser mu = this.getUserMgr().getMeetingUser(this.getChairManID());
//		if (mu != null) {
//			if (mu.getHostStatus() == RequestHost_Allow)
//				addHost(this.getChairManID());
//		}
//	}

    /**
     * �ı���Ƶ��״̬
     *
     * @param UserID
     * @param Status
     */
    public void ChangeAudioStatus(int UserID, int Status) {
        MeetingUser mu = null;
        if (UserID == getMyPID())
            mu = getUserMgr().getSelfUser();
        else
            mu = getUserMgr().getMeetingUser(UserID);
        if (mu == null)
            return;

        int oldStatus = mu.getAudioStatus();
        mu.setAudioStatus(Status);
        // getUserMgr().reSort();

        if (UserID == getMyPID()) {
            if (oldStatus != RequestSpeak_Allow && Status == RequestSpeak_Allow) {
                RtmpClientMgr.getInstance().publishAudio();// �����Լ�����Ƶ�������е�������
            } else if (oldStatus == RequestSpeak_Allow
                    && Status != RequestSpeak_Allow) {
                RtmpClientMgr.getInstance().unpublishAudio();
            }
        } else {
            if (oldStatus != RequestSpeak_Allow && Status == RequestSpeak_Allow) {
                RtmpClientMgr.getInstance().playAudio(UserID);
            } else if (oldStatus == RequestSpeak_Allow
                    && Status != RequestSpeak_Allow) {
                RtmpClientMgr.getInstance().unplayAudio(UserID);
            }
        }
//		NotificationCenter.getInstance().postNotificationName(
//				Session.UI_NOTIFY_USER_AUDIO_CHANGE, UserID, Status);
        if (sessionInterface != null) {
            sessionInterface.ChangeAudioStatus(UserID, oldStatus);
        }
    }


    // //////////////////////////////////////////////////////////////////

    /**
     * ������ɷ���
     *
     * @param peerID
     */
    public void addSpeaker(int peerID) {
        if (!mALSpeakerList.contains(peerID)) {
            mALSpeakerList.add(peerID);
        }
    }

    /**
     * ɾ�����ɷ���
     *
     * @param peerID
     */
    public void delSpeaker(int peerID) {
        if (mALSpeakerList.contains(peerID)) {
            mALSpeakerList.remove(mALSpeakerList.indexOf(peerID));
        }
    }

    /**
     * ɾ����һ�����ɷ���
     *
     * @return
     */
    public int delFirstSpeaker() {
        int nPeer = 0;
        if (mALSpeakerList.size() > 0) {
            nPeer = mALSpeakerList.get(0);
            mALSpeakerList.remove(0);
        }
        return nPeer;
    }

    /**
     * ͬ����������������ɷ���
     *
     * @param peerID
     */
    public void addPendingSpeaker(int peerID) {
        if (!mALPendingSpeakerList.contains(peerID)) {
            mALPendingSpeakerList.add(peerID);
        }
    }

    /**
     * ��ͬ����������������ɷ���
     *
     * @param peerID
     */
    public void delPendingSpeaker(int peerID) {
        if (mALPendingSpeakerList.contains(peerID)) {
            mALPendingSpeakerList.remove(mALPendingSpeakerList.indexOf(peerID));
        }
    }

    /**
     * ���ɷ��Ե�����
     *
     * @return
     */
    public int getSpeakerCount() {
        return mALSpeakerList.size();
    }

	/*
	 * public boolean isFreeMode() { if (m_ChairmanMode == MeetingMode_Free)
	 * return true; return false;
	 *
	 * }
	 */

    /**
     * ��ϯ�뿪
     * //
     */
//	private void chairManLeave() {
//
//		// m_ChairmanMode = mode;
//		// if (m_ChairmanMode == MeetingMode_Free) {
//		if (this.isSpeakFree())
//			NewFreeSpeak();
//		if (this.isControlFree())
//			NewFreeHost();
//		// if (_isSyncVideo)
//		// syncVideoModeChange(false, false);
//		// } else if (m_ChairmanMode == MeetingMode_ChairmanControl) {
//		// }
//		// NotificationCenter.getInstance().postNotificationName(
//		// UI_NOTIFY_MEETING_MODECHANGE, mode);
//	}
    public void setMaxSpeakerCount(int count) {
        m_maxSpeakerCount = count;
    }

    /**
     * ���ɷ��Ե��������
     *
     * @return
     */
    public int getMaxSpeakerCount() {
        return m_maxSpeakerCount;
    }

    /**
     * ���ɷ���ģʽ
     */
    public void NewFreeSpeak() {
        // if (m_ChairmanMode == MeetingMode_Free) {
        if (this.isSpeakFree()) {
            while (mALSpeakerList.size() < m_maxSpeakerCount
                    && mALPendingSpeakerList.size() > 0) {
                int newSpeakerID = mALPendingSpeakerList.get(0);
                delPendingSpeaker(newSpeakerID);
                addSpeaker(newSpeakerID);
                ChangeAudioStatus(newSpeakerID, RequestSpeak_Allow);
            }
        }
    }

//	/**
//	 * ����ģʽ
//	 */
//	private void NewFreeHost() {
//
//		// if (m_ChairmanMode == MeetingMode_Free) {
//		if (this.isControlFree()) {
//			// if ((int) mALPendingHostList.size() > 0 && m_hostID == 0) {
//			if ((int) mALPendingHostList.size() > 0) {
//				int newDataOperID = mALPendingHostList.get(0);
//				delPendingHost(newDataOperID);
//				ControlStatusChange(newDataOperID, RequestHost_Allow, true);
//			}
//		}
//	}

    /**
     * �����û������仯
     *
     * @param peerID
     */
    private void onFocusUserChange(int peerID, int videoID) {
        if (m_nFocusUserPeerID == peerID && m_nFocusVideoID == videoID)
            return;

        // use peerID "0" to present "all user"
        // when peerID "0" is watching me, that means I need to publishVideo to
        // all
        if (peerID == getMyPID()) {
            ClientFunc_WatchBuddyVideo(0);
        } else if (m_nFocusUserPeerID == getMyPID()) {
            ClientFunc_UnWatchBuddyVideo(0);
        }
        m_nFocusUserPeerID = peerID;
        m_nFocusVideoID = videoID;
        m_thisUserMgr.reSort();
        if (sessionInterface != null) {
            sessionInterface.onFocusUserChange(peerID, videoID);
        }
    }

    /**
     * ������¼��
     *
     * @param start
     */
    private void onServerRecording(boolean start) {
        m_bServerRecording = start;
        if (sessionInterface != null) {
            sessionInterface.onServerRecording(m_bServerRecording);
        }
    }

    /**
     * ��ȡ�ҵ�id
     *
     * @return
     */
    public int getMyPID() {
        return m_thisUserMgr.getSelfUser().getPeerID();
    }

    /**
     * ���
     */
    public void clear() {
        m_isViewer = false;
        list.clear();
        MSG.clear();
        m_thisUserMgr.clear();
        if (padInterface != null)
            padInterface.Clear();
        mALSpeakerList.clear();
        mALPendingSpeakerList.clear();
        m_nWatchVideoIDs.clear();
        m_nFocusUserPeerID = -1;
        m_bServerRecording = false;
        videoForSipPeerId = -1;
        videoForSipVideoId = 0;
        mALPendingHostList.clear();
        m_maxSpeakerCount = 9;
        m_thisUserMgr.getSelfUser().clear();
        m_thisUserMgr.clear();
//		m_autoExitWeiyi = 0;
        m_sessionStatus = 0;
        m_bSpeakFree = 1;
        m_bAllowRecord = 0;
        m_bControlFree = 1;

        m_chairmancontrol = "11111110010011111010000010000";
        m_chairmanfunc = "";
        m_hideme = 0;
        m_currentCameraIndex = 0;
        m_isFrontCamera = true;
        callSipState = -1;

    }

    /**
     * �˳�����
     */
    public void LeaveMeeting() {
        m_bInmeeting = false;
        if (padInterface != null)
            padInterface.Clear();
        clear();
        RtmpClientMgr.getInstance().exitMeeting();

    }

    /**
     * ��Ļ��ת
     *
     * @param rotate
     */
    public void setRotate(int rotate) {
        RtmpClientMgr.getInstance().setOrientation(rotate);
    }


    /**
     * ������Ƶ
     *
     * @param nUserPeerID
     * @param sur
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param zorder
     * @param border
     * @param scaletype   chairmancontrol 18λ��0 ��ͼ��1�ǲ���ͼ������ط�1���У�0�ǲ���
     */
    public void playVideo(int nUserPeerID, VideoView sur, float left,
                          float top, float right, float bottom, int zorder, boolean border,
                          int scaletype, int videoid) {

        RtmpClientMgr.getInstance().playVideo(nUserPeerID, sur, left, top,
                right, bottom, zorder, border, scaletype, videoid);
    }

    public void unplayVideo(int nUserPeerID, int videoid) {
        RtmpClientMgr.getInstance().unplayVideo(nUserPeerID, videoid);
    }

    /**
     * ����ĳ������߹������Ļ
     *
     * @param sur
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param zorder
     */
    public void playScreen(int m_nScreenSharePeerID, VideoView sur, float left, float top, float right,
                           float bottom, int zorder) {
        //		if (m_nScreenSharePeerID != 0)
        RtmpClientMgr.getInstance().playScreen(m_nScreenSharePeerID, sur,
                left, top, right, bottom, zorder);
    }

    public void unplayScreen(int m_nScreenSharePeerID) {
        //		if (m_nScreenSharePeerID != 0)
        RtmpClientMgr.getInstance().unplayScreen(m_nScreenSharePeerID);
    }

    /**
     * �����Լ�����Ƶ�������е�������
     */
    public void publishVideo() {
        RtmpClientMgr.getInstance().publishVideo();
    }

    public void unpublishVideo() {
        RtmpClientMgr.getInstance().unpublishVideo();
    }

    /**
     * ����ĳ������ߵ���Ƶ��ǰ���ǶԷ��Ѿ������Լ�����Ƶ��
     *
     * @param npeerid
     */
    public void playAudio(int npeerid) {
        RtmpClientMgr.getInstance().playAudio(npeerid);
    }

    /**
     * ֹͣ����ĳ������ߵ���Ƶ
     *
     * @param npeerid
     */
    public void unplayAudio(int npeerid) {
        RtmpClientMgr.getInstance().unplayAudio(npeerid);
    }

//	/**
//	 * �޸�����
//	 *
//	 * @param strName
//	 */
//	public void changeMyName(String strName) {
//
//		RtmpClientMgr.getInstance().setClientProperty("m_NickName", strName, 0);// ���ñ����������
//		m_thisUserMgr.getSelfUser().setName(strName);
////		NotificationCenter.getInstance().postNotificationName(
////				Session.UI_NOTIFY_USER_CHANGE_NAME);
//
//	}

    /**
     * �޸�����
     *
     * @param strName
     */
    public void changeUserName(String strName, int peerid) {

        RtmpClientMgr.getInstance().setClientProperty("m_NickName", strName, peerid);// ���ñ����������
        m_thisUserMgr.getUser(peerid).setName(strName);

    }

    /**
     * �л��Լ�������ͷ����Ӱ���Լ��ۿ��Լ�����Ƶ�ͷ��͸�������Ƶ
     *
     * @param bFront
     */
    public void switchCamera(boolean bFront) {
        RtmpClientMgr.getInstance().changeCamera(bFront);
    }

    /**
     * ѡ������ͷ
     */
    public void switchCamera(int m_currentCameraIndex) {
        int count = getCamerCount();
        if (count <= 0)
            return;

        int tempIndex = m_currentCameraIndex + 1;

        if (tempIndex >= count) {
            tempIndex = 0;
        }

        m_currentCameraIndex = tempIndex;
        RtmpClientMgr.getInstance().changeCameraByIndex(m_currentCameraIndex);

    }

    /**
     * ��Ƶ��ͼ��ǰ�����Ѿ��ڹۿ�ĳ������ߵ���Ƶ����Ļ����
     *
     * @param peerID  ������ߵ�peerID������Ϊ0������Ϊ�Լ���peerID
     * @param isVideo Ϊtrueʱ��ʾ��Ƶ��ͼ��Ϊfalseʱ��ʾ��Ļ�����ͼ
     * @return
     */
    public Bitmap cutPicture(final int peerID, boolean isVideo) {

        return RtmpClientMgr.getInstance().cutPicture(peerID, isVideo);
    }

    /**
     * �����ı���Ϣ
     *
     * @param toID
     * @param text
     * @param textFormat
     */
    public void sendTextMessage(int toID, String text, JSONObject textFormat) {
        try {
            JSONArray arr = new JSONArray();
            arr.put(0, null);
            arr.put(1, "ClientFunc_ReceiveText");
            arr.put(2, toID);
            arr.put(3, true);
            arr.put(4, text);
            arr.put(5, m_thisUserMgr.getSelfUser().getPeerID());
            arr.put(6, m_thisUserMgr.getSelfUser().getName());
            arr.put(7, toID == 0 ? 0 : 2);
            arr.put(8, textFormat);

            RtmpClientMgr.getInstance().callServerFunction(
                    "ServerFunc_CallClientFunc2", arr);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * �Լ�����Ƶ״̬�����仯
     *
     * @param bCanWatch
     */

    public void setWatchMeWish(boolean bCanWatch) {
        Session.getInstance().getUserMgr().getSelfUser().sethasVideo(bCanWatch);
        RtmpClientMgr.getInstance().setClientProperty("m_HasVideo",
                bCanWatch ? 1 : 0, Session.SENDMSGTOALL_EXCEPT_ME);
    }

//	public boolean getWatchMeWish() {
//		return m_bWatchWish;
//	}

    /**
     * ����ĳ������ߵ���Ƶ
     *
     * @param nPeerID
     * @param bPlay
     * @param playView
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param zorder
     * @param border
     * @param scaletype
     */
    public void PlayVideo(int nPeerID, boolean bPlay, VideoView playView,
                          float left, float top, float right, float bottom, int zorder,
                          boolean border, int scaletype, int videoid) {

        if (m_thisUserMgr == null)
            return;

        if (nPeerID == 0 || nPeerID == this.getMyPID()) {// nPeerID = 0 �����Լ�
            MeetingUser mu = m_thisUserMgr.getSelfUser();
            mu.setWatch(bPlay);
            Integer myid = this.getMyPID();

            if (bPlay) {

                if (m_nWatchVideoIDs.size() > 0) {
                    boolean needadd = true;
                    for (int i = 0; i < m_nWatchVideoIDs.size(); i++) {
                        if (m_nWatchVideoIDs.get(i).getPeerid() == myid
                                && m_nWatchVideoIDs.get(i).getCameraid() == videoid) {
                            needadd = false;
                        }
                    }
                    if (needadd) {
                        m_nWatchVideoIDs.add(0, new MyWatch(myid, videoid));
                    }
                } else {
                    m_nWatchVideoIDs.add(new MyWatch(myid, videoid));

                }


                playVideo(0, playView, left, top, right, bottom, zorder,
                        border, scaletype, videoid);
            } else {

                for (int i = 0; i < m_nWatchVideoIDs.size(); i++) {
                    if (m_nWatchVideoIDs.get(i).getPeerid() == myid
                            && m_nWatchVideoIDs.get(i).getCameraid() == videoid) {
                        m_nWatchVideoIDs.remove(i);
                    }
                }
                //				mapVideoIDs.remove(myid);
                unplayVideo(0, videoid);
            }
        } else {
            Integer hisid = nPeerID;
            MeetingUser mu = m_thisUserMgr.getUser(hisid);
            if (mu != null) {
                mu.setWatch(bPlay);
            }

            if (bPlay) {


                if (m_nWatchVideoIDs.size() > 0) {
                    boolean has = true;
                    for (int i = 0; i < m_nWatchVideoIDs.size(); i++) {
                        if (m_nWatchVideoIDs.get(i).getPeerid() == hisid && m_nWatchVideoIDs.get(i).getCameraid() == videoid) {
                            has = false;
                        }
                    }
                    if (has) {
                        m_nWatchVideoIDs.add(0, new MyWatch(hisid, videoid));

                        watchOtherVideo(hisid, videoid);
                    }
                } else {
                    m_nWatchVideoIDs.add(new MyWatch(hisid, videoid));

                    watchOtherVideo(hisid, videoid);
                }


                playVideo(hisid, playView, left, top, right, bottom, zorder,
                        border, scaletype, videoid);
            } else {
                for (int i = 0; i < m_nWatchVideoIDs.size(); i++) {
                    if (m_nWatchVideoIDs.get(i).getPeerid() == hisid && m_nWatchVideoIDs.get(i).getCameraid() == videoid) {
                        m_nWatchVideoIDs.remove(i);
                        //						mapVideoIDs.remove(hisid);
                    }
                }

                unplayVideo(hisid, videoid);
            }
        }
    }

    /**
     * ����������
     *
     * @param loud
     */
    public void setLoudSpeaker(boolean loud) {
        RtmpClientMgr.getInstance().setLoudSpeaker(loud);
    }

//	/**
//	 * ������
//	 *
//	 * @return
//	 */
//	public boolean getLoudSpeaker() {
//		return m_isLouder;
//	}

    /**
     * ������Ƶ����
     *
     * @param highquality
     */
    public void setCameraQuality(boolean highquality) {
        RtmpClientMgr.getInstance().setCameraQuality(highquality);
    }

    /**
     * �ĵ������仯
     *
     * @param nfileID
     * @param bDel
     * @param strFielname
     * @param strFileURL
     * @param nPageCount
     */
    public void sendDocChange(int nfileID, boolean bDel, String strFielname,
                              String strFileURL, int nPageCount) {

        JSONArray arr = new JSONArray();
        JSONObject body = new JSONObject();
        try {
            body.put("isdel", bDel);
            body.put("fileid", nfileID);
            body.put("filename", strFielname);
            body.put("fileurl", strFileURL);
            body.put("pagecount", nPageCount);
            arr.put(0, null);
            arr.put(1, "ClientFunc_DocumentChange");
            arr.put(2, SENDMSGTOALL_EXCEPT_ME);
            arr.put(3, body);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RtmpClientMgr.getInstance().callServerFunction(
                "ServerFunc_CallClientFunc", arr);

    }

    /**
     * ������ʾ��ҳ��
     *
     * @param fileid �ļ���id
     * @param page   ҳ��
     */
    public void sendShowPage(int fileid, int page) {
        JSONObject body = new JSONObject();
        try {
            body.put("fileID", fileid);
            body.put("pageID", page);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("emm", fileid + "");
        Log.e("emm", page + "");

        PublishMessage("ShowPage", SENDMSGTOALL_EXCEPT_ME, 0, body, "ShowPage");
    }

    public void sendBroadcastType(int type) {
        JSONObject body = new JSONObject();
        try {
            body.put("type", type);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        PublishMessage("broadcasttype", SENDMSGTOALL_EXCEPT_ME, 0, body, "broadcasttype");
    }

    /**
     * �������ű�����Ƶ
     */
    public void resumeLocalVideo() {
        RtmpClientMgr.getInstance().resumeLocalVideo();
    }

    /**
     * ��ͣ������Ƶ
     */
    public void pauseLocalVideo() {
        RtmpClientMgr.getInstance().pauseLocalVideo();
    }

    /**
     * ��ȡ��ǰ����ͷ�ķֱ���
     *
     * @return
     */
    public List<Camera.Size> getCurrentCameraResolutions() {
        return RtmpClientMgr.getInstance().getCurrentCameraResolutions();
    }

    /**
     * ����
     *
     * @param width
     * @param height
     */
    public void takePhoto(final int width, final int height) {
        RtmpClientMgr.getInstance().takePhoto(width, height);
    }

    /**
     * �����û�
     *
     * @param peerID
     */
    public void setFocusUser(final int peerID, final int videoId) {
        if (m_nFocusUserPeerID == peerID && m_nFocusVideoID == videoId)
            return;

        JSONObject params = new JSONObject();
        try {
            params.put("userID", peerID);
            params.put("videoID", videoId);
//			params.put("userInfo", mu == null ? null : mu.getJsonObject());

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        onFocusUserChange(peerID, videoId);
        if (peerID != 0) {
            PublishMessage("LiveUserChange", SENDMSGTOALL_EXCEPT_ME, peerID, params, "", "StartRecording");

        } else {

            DeleteMessage("LiveUserChange", SENDMSGTOALL_EXCEPT_ME, peerID, params, "", "StartRecording");
        }
    }

    public int getFocusUser() {
        return m_nFocusUserPeerID;
    }

    public int getFocusUserVideoId() {
        return m_nFocusVideoID;
    }

    public boolean hasVideoForSip() {
        return videoForSipPeerId != -1 && getUserMgr().getUser(videoForSipPeerId) != null;
    }

    /**
     * ¼��
     *
     * @param start
     */
    public void serverRecording(final boolean start) {
//			if(!isAllowServerRecord()){
//				return;
//			}
        if (m_bServerRecording == start)
            return;

        JSONObject params = new JSONObject();
        JSONArray arr = new JSONArray();
        try {
            params.put("senderID", this.getMyPID());
            params.put("toID", SENDMSGTOALL_EXCEPT_ME);
            params.put("name", "StartRecording");
            params.put("body", start ? 1 : 0);
            params.put("id", "StartRecording");

            arr.put(0, null);
            arr.put(1, 0);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        onServerRecording(start);
        RtmpClientMgr.getInstance().remoteMsg(start, params, new byte[]{});
        RtmpClientMgr.getInstance().callServerFunction(
                start ? "Remote_StartRecord" : "Remote_StopRecord", arr);
    }


    public MeetingUserMgr getUserMgr() {
        return m_thisUserMgr;
    }

    /**
     * ���û�����ĳ�˵�ĳ������߶���ķ������������߻��յ�RtmpClientMgrCbk.ClientFunc_Call�ص�
     * <p>
     * ��ϯ�����仯������������Ҫ���浽�������ϣ�������publishmessage()������
     * ��AҪ�鿴B����Ƶ������Ҫ���浽�������ϣ���callClientFunction()
     *
     * @param toWhom
     * @param functionName
     * @param parameters
     */
    public void callClientFunction(final int toWhom, final String functionName,
                                   final Object parameters) {
        RtmpClientMgr.getInstance().callClientFunction(toWhom, functionName,
                parameters);
    }

    /**
     * ��ϯ�ı�
     *
     * @param userID
     */
    public void changeChairMan(int userID) {
        PublishMessage("ChairmanChange", SENDMSGTOALL, userID);
    }


    /**
     * �������ɷ���
     *
     * @param userID
     */
    public void requestSpeaking(int userID) {
        JSONObject body = new JSONObject();
        try {

            PublishMessage("RequestSpeak", SENDMSGTOALL, userID, body,
                    "RequestSpeak" + userID, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ȡ�����ɷ���
     *
     * @param userID
     */
    public void cancelSpeaking(int userID) {
        JSONObject body = new JSONObject();
        try {

            body.put("simple", true);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DeleteMessage("RequestSpeak", SENDMSGTOALL, userID, body,
                "RequestSpeak" + userID);
    }

    /**
     * ��ʾ��ǩ
     *
     * @param curSel
     */
    public void requestShowTab(int curSel) {
        int mypid = this.getMyPID();
        if (m_thisUserMgr.getSelfUser().isChairMan()
                || m_thisUserMgr.getSelfUser().getHostStatus() == RequestHost_Allow) {
            EmmLog.d("emm", "requestShowTab");
            PublishMessage("DataCurSel", SENDMSGTOALL_EXCEPT_ME, mypid, curSel,
                    "", "");
        }
    }

    /**
     * ��������
     *
     * @param userID
     */
    public void requestHost(int userID) {
        Log.e("emm", "requestHost************");
        boolean force = m_thisUserMgr.getSelfUser().isChairMan();
        PublishMessage("RequestControl", SENDMSGTOALL, userID, force,
                "RequestControl" + userID, "");

    }

    /**
     * ȡ������
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
        DeleteMessage("RequestControl", SENDMSGTOALL, userID, body,
                "RequestControl" + userID);
    }


    /**
     * �������
     *
     * @param userID
     */
    public void kickUser(int userID) {
        MeetingUser user = m_thisUserMgr.getMeetingUser(userID);
        try {
            JSONArray arr = new JSONArray();
            arr.put(0, null);
            arr.put(1, "ClientFunc_ChairmanKickOut");
            arr.put(2, userID);
            arr.put(3, 0);
            RtmpClientMgr.getInstance().callServerFunction(
                    "ServerFunc_CallClientFunc", arr);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * ����ļ�
     *
     * @param fileid
     * @param filename
     * @param pagenum
     * @param swfpath
     */
    public void addMeetingFile(int fileid, String filename, int pagenum,
                               String swfpath) {
        //		ShareDoc sdc = new ShareDoc();
        //		sdc.fileName = filename;
        //		sdc.pageCount = pagenum;
        //		sdc.fileUrl = swfpath;
        //		sdc.docID = fileid;
        if (padInterface != null)
            padInterface.whitePadDocChange(true, fileid, pagenum, filename, swfpath, true);
    }

    /**
     * �ı��ĵ�
     *
     * @param seldocid
     * @param selPage
     */
    public void changeDoc(int seldocid, int selPage) {
        if (padInterface != null)
            padInterface.whiteshowPage(seldocid, selPage, true);
    }

    public boolean isM_instMeeting() {
        return m_instMeeting;
    }

    public void setM_instMeeting(boolean m_instMeeting) {
        this.m_instMeeting = m_instMeeting;
    }

//	public boolean isM_isCaller() {
//		return m_isCaller;
//	}
//
//	public void setM_isCaller(boolean m_isCaller) {
//		this.m_isCaller = m_isCaller;
//	}

    /**
     * �μӻ��������ĵ�ַ
     *
     * @param meetingID
     * @param pwd
     * @return
     */
    public String getInviteAddress(String meetingID, String pwd) {

        String strUrl = MEETING_PHP_SERVER + "/" + meetingID + "/"
                + (pwd == null ? "" : pwd);

        return strUrl;
    }


    //	/**
    //	 * ������������(��ַ��ip�����֡��˿ں�)
    //	 *
    //	 * @param strRet
    //	 * @return
    //	 */
    //	boolean parserMeetingConfig(String strRet) {
    //
    //		boolean bsuccess = false;
    //
    //		try {
    //			JSONObject jsRoot = new JSONObject(strRet);
    //			String strDocServer = jsRoot.optString("DocConvertServerAddr");
    //			String strMediaServer = jsRoot.optString("MediaServerIP");
    //			String strInstName = jsRoot.optString("MediaServerInstName");
    //			String strMediaServerPort = jsRoot.optString("MediaServerPort");
    //			//xiaoyang add
    //			String strLiveMediaServer = jsRoot.optString("livemediaserver");
    //			String strLiveMediaPort = jsRoot.optString("livemediaport");
    //			String strSignalServer = jsRoot.optString("signalserver");
    //			String strSignalServerPort = jsRoot.optString("signalserverport");
    //			//xiaoyang add
    //			if (strDocServer != null && !strDocServer.isEmpty())
    //				MEETING_DOC_ADDR = strDocServer;
    //			else
    //				MEETING_DOC_ADDR = getWebHttpServerAddress();
    //
    //			if (strMediaServer != null && !strMediaServer.isEmpty())
    //				MEDIA_SERVER_IP = strMediaServer;
    //			else {
    //				String webAddr = getWebHttpServerAddress();
    //				String tempid = "";
    //				if (webAddr.startsWith("http://")) {
    //					tempid = webAddr.toString().substring(7);
    //				} else {
    //					tempid = webAddr.toString();
    //				}
    //				if (tempid.contains(":")) {
    //					tempid = tempid.substring(0, tempid.indexOf(":"));
    //				}
    //				MEDIA_SERVER_IP = tempid;
    //			}
    //
    //			if (strInstName != null && !strInstName.isEmpty())
    //				MEDIA_SERVER_INST_NAME = strInstName;
    //			if (strMediaServerPort != null && !strMediaServerPort.isEmpty()) {
    //				MEDIA_SERVER_PORT = strMediaServerPort.trim();
    //			}
    //			//xiaoyang add
    //			if(strLiveMediaServer != null&&!strLiveMediaServer.isEmpty()){
    //				LIVE_MEDIA_SERVER = strLiveMediaServer;
    //			}else {
    //				String webAddr = getWebHttpServerAddress();
    //				String tempid = "";
    //				if (webAddr.startsWith("http://")) {
    //					tempid = webAddr.toString().substring(7);
    //				} else {
    //					tempid = webAddr.toString();
    //				}
    //				if (tempid.contains(":")) {
    //					tempid = tempid.substring(0, tempid.indexOf(":"));
    //				}
    //				LIVE_MEDIA_SERVER = tempid;
    //			}
    //			if(strLiveMediaPort != null&&!strLiveMediaPort.isEmpty()){
    //				LIVE_MEDIA_PORT = strLiveMediaPort;
    //			}
    //			if(strSignalServer != null&&!strSignalServer.isEmpty()){
    //				SIGNAL_SERVER = strSignalServer;
    //			}else {
    //				String webAddr = getWebHttpServerAddress();
    //				String tempid = "";
    //				if (webAddr.startsWith("http://")) {
    //					tempid = webAddr.toString().substring(7);
    //				} else {
    //					tempid = webAddr.toString();
    //				}
    //				if (tempid.contains(":")) {
    //					tempid = tempid.substring(0, tempid.indexOf(":"));
    //				}
    //				SIGNAL_SERVER = tempid;
    //			}
    //			if(strSignalServerPort != null&&!strSignalServerPort.isEmpty()){
    //				SIGNAL_SERVER_PORT = strSignalServerPort;
    //			}
    //			//xiaoyang add
    //			bsuccess = true;
    //			Log.e("emm", "strDocServer=" + MEETING_DOC_ADDR);
    //			Log.e("emm", "strMediaServer=" + MEDIA_SERVER_IP);
    //			Log.e("emm", "strInstName=" + strInstName);
    //			Log.e("emm", "strMediaServerPort=" + MEDIA_SERVER_PORT);
    //			Log.e("emm", "getWebHttpServerAddress=" + getWebHttpServerAddress());
    //		} catch (JSONException e) {
    //			e.printStackTrace();
    //		}
    //		return bsuccess;
    //	}


    /**
     * ��ȡ�����ļ�
     *
     * @param nMeetingID
     */
    public void getMeetingFile(final int nMeetingID, String doc_addr, GetMeetingFileCallBack callback) {
        if (padInterface != null) {
            padInterface.setWebImageDomain(doc_addr);
            padInterface.getMeetingFile(nMeetingID, webFun_getmeetingfile, callback);
        }
    }

    /**
     * ɾ�������ļ�
     *
     * @param nMeetingID
     * @param docid
     */
    public void delMeetingFile(final int nMeetingID, final int docid) {
        if (padInterface != null)
            padInterface.delMeetingFile(nMeetingID, docid, webFun_delmeetingfile);
    }


    /**
     * ����ϵͳ��Ϣ(��ʾ��Ϣ)
     *
     * @param fromid
     * @param userName
     * @param msg
     */
    public void sendSystemMsg(int fromid, String userName, String msg) {
        MeetingUser mu = this.getUserMgr().getUser(fromid);
        if (mu != null) {
            ClientFunc_ReceiveText(0, msg, userName, 0, null);
        }
    }

    /**
     * ����ͷ�ĸ���
     *
     * @return
     */
    public int getCamerCount() {
        return getCameraInfo().size();
    }

    public List<Integer> getCameraInfo() {
        return RtmpClientMgr.getInstance().getCameraInfo();
    }

    /**
     * ����ͷ��ǰ�û��Ǻ���
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
		 * i=0;i<list.size();i++) { int d =list.get(i); if(d ==1 ){//��ǰ������ͷ
		 * hasFront = true; }else if(d == 0){//�к�������ͷ hasBack = true; } }
		 * if(hasFront && hasBack)//ǰ������ͷ���� return true; return false;
		 */
    }

    /**
     * �Ƿ���ǰ������ͷ
     *
     * @return
     */
    public boolean hasFrontCamera() {
        List<Integer> list = RtmpClientMgr.getInstance().getCameraInfo();
        for (int i = 0; i < list.size(); i++) {
            int d = list.get(i);
            if (d == 1)
                return true;
        }
        return false;
    }

    /**
     * �Ƿ��к�������ͷ
     *
     * @return
     */
    public boolean hasBackCamera() {
        List<Integer> list = RtmpClientMgr.getInstance().getCameraInfo();
        for (int i = 0; i < list.size(); i++) {
            int d = list.get(i);
            if (d == 0)
                return true;
        }
        return false;
    }

    /**
     * �����Բ鿴��Ƶ��·��
     *
     * @return
     */
    public int getMaxWatchVideoCount() {
        // Ŀǰ��࿴��·��Ƶ
        return 4;
    }

    /**
     * ��Ӱװ��ϻ��ʵĲ���
     *
     * @param nActs
     * @param data
     */
    public void addSharps(int nActs, Object data) {
        TL_PadAction action = (TL_PadAction) data;
        if (action != null) {
            MessagePack msgPack = new MessagePack();// �����ƴ�������
            ByteArrayOutputStream byteOutPutSream = new ByteArrayOutputStream();
            org.msgpack.packer.Packer packer = msgPack
                    .createPacker(byteOutPutSream);
            try {
                packer.writeArrayBegin(5);
                packer.write(1);
                packer.write(nActs);
                packer.write(action.nDocID);
                packer.write(action.nPageID);

                if (action.nActionMode != TL_PadAction.factoryType.ft_markerPen)
                    packer.writeArrayBegin(7);
                else
                    packer.writeArrayBegin(8);

                packer.write(action.sID);
                packer.write(action.nActionMode.ordinal());
                PointF ptFreal = action.alActionPoint.get(0);
                packer.write(ptFreal.x * 100);
                packer.write(ptFreal.y * 100);
                ptFreal = action.alActionPoint
                        .get(action.alActionPoint.size() - 1);
                packer.write(ptFreal.x * 100);
                packer.write(ptFreal.y * 100);
                HashMap<String, Object> properData = new HashMap<String, Object>();
                properData.put("width", action.nPenWidth);

                int a = action.nPenColor >> 24 & 0xff;
                int color = (action.nPenColor << 8 & 0xffffff00) | a;

                properData.put("color", color);

                properData.put("fill", action.bIsFill);
                if (action.nActionMode == TL_PadAction.factoryType.ft_Text)
                    properData.put("text", action.sText);

                // packer.writeMapBegin(properData.size());
                packer.write(properData);
                // packer.writeMapEnd();

                if (action.nActionMode == TL_PadAction.factoryType.ft_markerPen) {
                    int arrayLen = (action.alActionPoint.size());
                    packer.writeArrayBegin(arrayLen);
                    for (int i = 0; i < action.alActionPoint.size(); i++) {
                        HashMap<String, Object> pt = new HashMap<String, Object>();
                        ptFreal = action.alActionPoint.get(i);
                        pt.put("x", ptFreal.x * 100);
                        pt.put("y", ptFreal.y * 100);

                        packer.write(pt);
                    }
                    packer.writeArrayEnd();
                }

                packer.writeArrayEnd();

                packer.writeArrayEnd();
                byte[] bytes = byteOutPutSream.toByteArray();
                String StrBytes = "";
                MessagePack mp = new MessagePack();
                try {
                    if (bytes != null && bytes.length > 0) {
                        Value va = mp.read(bytes);
                        StrBytes = va.toString();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String msgID = "sharpsChange-" + action.nDocID + "-"
                        + action.sID;
                PublishMessageBinaryBody("sharpsChange",
                        SENDMSGTOALL_EXCEPT_ME, 0, bytes, msgID, "");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ��ӡ��ƶ���ɾ���װ��ϻ��ʵĲ���
     */
    @Override
    public void SendActions(int nActs, Object data) {
        if (nActs == WhitePadInterface.ADD_ACTION) {
            addSharps(2, data);
        } else if (nActs == WhitePadInterface.MODIFY_ACTION) {
            if (data instanceof ArrayList) {
                ArrayList<TL_PadAction> actions = (ArrayList<TL_PadAction>) data;
                if (actions != null) {
                    for (int i = 0; i < actions.size(); i++) {
                        TL_PadAction action = actions.get(i);
                        if (action != null)
                            addSharps(nActs, action);
                    }
                }
            }
        } else if (nActs == WhitePadInterface.DELETE_ACTION) {
            // sharpRemove=3
            TL_PadAction action = (TL_PadAction) data;
            if (action != null) {
                String msgID = "sharpsChange-" + action.nDocID + "-"
                        + action.sID;
                DeleteMessage("sharpsChange", SENDMSGTOALL_EXCEPT_ME, 0, null,
                        msgID);
            }
        }
    }

    //	public void getInvitUsers(final String serial,final CheckMeetingCallBack callback){
//		Utitlties.stageQueue.postRunnable(new Runnable() {
//			@Override
//			public void run() {
//				String url = webFun_getInvitUsers;
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
//										Session.this.getUserMgr().addUser(mu);
//										Session.this.getUserMgr().addOffLineId(mu.getThirdID());
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
    //����ֱ������
    public void startBroadCasting(String url, int peerid) {
        JSONObject body = new JSONObject();
        try {
            body.put("subject", m_strMeetingName);
            body.put("status", 1);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PublishMessage("startbroadcast", SENDMSGTOALL_EXCEPT_ME, peerid, body, "startbroadcast");
        if (this.getMeetingtype() == 11) {
            RtmpClientMgr.getInstance().startBroadCasting(url, 0);
        } else {
            RtmpClientMgr.getInstance().startBroadCasting(url, 1);
        }
    }

    //ֹͣ����ֱ������
    public void stopBroadCasting(int peerid) {

        DeleteMessage("startbroadcast", SENDMSGTOALL_EXCEPT_ME, peerid);
        RtmpClientMgr.getInstance().stopBroadCasting();
    }

    public void setfocusVideo(int peerid, int videoid) {
        Integer body = videoid;
        Session.getInstance().PublishMessage("MainVideoId", Session.SENDMSGTOALL_EXCEPT_ME, peerid, body, "SyncVideoMode", "");
    }

    /**
     * ���ŵ�Ӱ
     */
    public void playMovie(int nPeerID, boolean bPlay, VideoView playView,
                          float left, float top, float right, float bottom, int zorder,
                          boolean border) {
        if (bPlay) {
            playVideo(nPeerID, playView, left, top, right, bottom, zorder, border, 0, 6000);
        } else {
            unplayVideo(nPeerID, 6000);
        }
    }

    //	@Override
    //	public boolean isChairAllow() {
    //		if(MeetingSession.RequestHost_Allow == 	MeetingSession.getInstance().getUserMgr().getSelfUser().getHostStatus()
    //				|| MeetingSession.getInstance().getMyPID()==MeetingSession.getInstance().getChairManID()){
    //			return true;
    //		}else{
    //			return false;
    //		}
    //	}
    //
    //	@Override
    //	public boolean isChairman() {
    //		if(MeetingSession.getInstance().getChairManID()==MeetingSession.getInstance().getMyPID()){
    //			return true;
    //		}else{
    //			return false;
    //		}
    //	}
    //
    //	@Override
    //	public boolean isMyAllow(int userID) {
    //		if(userID == MeetingSession.getInstance().getMyPID()){
    //			MeetingUser user = MeetingSession.getInstance().getUserMgr().getSelfUser();
    //			if(user.getHostStatus()==MeetingSession.RequestHost_Allow){
    //				return true;
    //			}
    //		}
    //		return false;
    //	}
    public void whitePadDocChange(JSONObject js) {
        try {
            boolean isdel = js.getBoolean("isdel");
            int docID = js.getInt("fileid");
            String fileName = js.getString("filename");
            String fileUrl = js.getString("fileurl");
            int pageCount = js.getInt("pagecount");
            if (padInterface != null)
                padInterface.whitePadDocChange(isdel, docID, pageCount, fileName, fileUrl, false);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void whitePadChange(JSONObject js, byte[] shapedata, Boolean bAdd) {
        try {
            String name = js.optString("name");
            if (name.equals("sharpsChange")) {
                if (padInterface != null)
                    padInterface.whiteshapesChange(shapedata, bAdd);
            } else if (name.equals("ShowPage")) {
                showPage(js);
            } else if (name.equals("WBBackColor")) {
                JSONObject jsbody;
                try {
                    //ͬ�����ڻ����е�ID
                    int senderid = js.optInt("senderID");
                    jsbody = js.getJSONObject("body");
                    if (jsbody != null) {
                        long color = jsbody.optLong("color");

                        if (padInterface != null)
                            padInterface.setWBBackColor((int) color);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void showPage(JSONObject js) {
        JSONObject jsbody;
        try {
            //ͬ�����ڻ����е�ID
            int senderid = js.optInt("senderID");
            jsbody = js.getJSONObject("body");
            if (jsbody != null) {
                int nFileID = jsbody.getInt("fileID");
                int nPageID = jsbody.getInt("pageID");
                nLastShowPageUser = senderid;
                if (padInterface != null)
                    padInterface.whiteshowPage(nFileID, nPageID, false);
//				NotificationCenter.getInstance().postNotificationName(SHOWPAGE);
                if (sessionInterface != null) {
                    sessionInterface.showpage();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //	public void loadFileComplete(String res){
    //		parserMeetingFiles(res);
    //		if(padInterface!=null)
    //			padInterface.whiteshowPage(0,1);
    //	}

    //	private boolean parserMeetingFiles(String strRes) {
    //		try {
    //			JSONObject jsbody = new JSONObject(strRes);
    //			int nRet = jsbody.getInt("result");
    //			if(nRet == 0){
    //				JSONArray jsa =jsbody.getJSONArray("meetingfile");
    //				for(int i = 0 ;i< jsa.length() ;i++){
    //					JSONObject jsmeeting =  jsa.getJSONObject(i);
    //
    //					int nConvert = jsmeeting.getInt("isconvert");
    //					if(nConvert == 1){
    //						ShareDoc sdc = new ShareDoc();
    //						String fileName = jsmeeting.getString("filename");
    //						int pageCount = jsmeeting.getInt("pagenum");
    //						String fileUrl = jsmeeting.getString("swfpath");
    //						int docID = jsmeeting.getInt("fileid");
    //						if(padInterface!=null)
    //							padInterface.whitePadDocChange(true, docID, pageCount, fileName, fileUrl);
    //					}
    //				}
    //			}
    //		} catch (JSONException e) {
    //			// TODO Auto-generated catch block
    //			e.printStackTrace();
    //		}
    //		return true;
    //	}
    public void clearWhitePad() {
        if (padInterface != null)
            padInterface.clearWhitePad();
    }

    public void playBroadCasting(String path, VideoView view, int scaletype) {
        RtmpClientMgr.getInstance().playBroadCasting(path, view, scaletype);
    }

    public void unplayBroadCasting() {
        RtmpClientMgr.getInstance().unplayBroadCasting();
    }

    public boolean isLiveMeeting() {
        if (getMeetingtype() == 11 || getMeetingtype() == 12 || getMeetingtype() == 13 || getMeetingtype() == 14) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isViewer() {
        if (!isLiveMeeting()) {
            return false;
        }
        if (m_isViewer) {
            return true;
        } else {
            if (getUserMgr().getSelfUser().getThirdID() == myMeeting.getCreateID() && getUserMgr().getSelfUser().getThirdID() != 0 && myMeeting.getCreateID() != 0) {
                return false;
            } else {
                return true;
            }
        }
    }

    public void setViewer(boolean isViewer) {
        m_isViewer = isViewer;
    }

    public void sendHandupACK(int toID, boolean res) {
        try {
            JSONArray arr = new JSONArray();
            arr.put(0, null);
            arr.put(1, "ClientFunc_HandsUpACK");
            arr.put(2, toID);
            arr.put(3, res);

            RtmpClientMgr.getInstance().callServerFunction(
                    "ServerFunc_CallClientFunc", arr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //	public void getJSON(String url)
//	{
//
//		client.post(url, null, new AsyncHttpResponseHandler(){
//			@Override
//			public void onSuccess(String content) {
//				try {
//					JSONObject jsonobj = new JSONObject(content);
//					if(jsonobj!=null&&jsonobj.has("ShowPage")){
//						JSONArray msgArray = jsonobj.getJSONArray("ShowPage");
//						if(msgArray.length() > 0 && msgArray.getJSONObject(0).has("body"))
//						{
//							JSONObject msg = msgArray.getJSONObject(0).getJSONObject("body");
//							if(msg.has("pageID") && msg.getInt("pageID") != 0)
//							{
//								boolean m_hasChapter = true;
//								m_pageList = new VodMsgList(msgArray, false);
//
//							}
//						}
//					}
//					if(jsonobj!=null && jsonobj.has("sharpsChange"))
//					{
//						JSONArray msgArray1 = jsonobj.getJSONArray("sharpsChange");
//						if(msgArray1.length() > 0 && msgArray1.getJSONObject(0).has("msg"))
//						{
//							JSONObject msg1 = msgArray1.getJSONObject(0).getJSONObject("msg");
//							if(msg1.has("body") && !msg1.getString("body").isEmpty())
//							{
//								m_shapeList = new VodMsgList(msgArray1, true);
//							}
//						}
//					}
////					NotificationCenter.getInstance().postNotificationName(LIVE_WHITEPAD_JSON_BACK, m_pageList,m_shapeList);
//
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//
//			}
//			@Override
//			public void onFailure(Throwable error) {
//				error.printStackTrace();
//			}
//		});
//	}
//	public void DoMsg(int tm)
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
    //xiaoyang add ����c++
    public void PublishAudio() {
        RtmpClientMgr.getInstance().publishAudio();
    }

    public void UnPublishAudio() {
        RtmpClientMgr.getInstance().unpublishAudio();
    }

    public void SetClientProperty(String key, Object value, int toWhom) {
        RtmpClientMgr.getInstance().setClientProperty(key, value, toWhom);
    }

    public void joinmeeting(String webserverip, int webserverport, final String name, final String meetingid, String meetingpwd, final int thirduid, final int usertype, Map<String, Object> paramMap) {
        m_thisUserMgr.getSelfUser().setName(name);
        m_thisUserMgr.getSelfUser().setMmeID(thirduid);
        m_thisUserMgr.getSelfUser().setThirdID(thirduid);
//		setM_strMeetingID(meetingid);
        joinMeetingCallBack callback = new joinMeetingCallBack() {

            @Override
            public void onError(int arg0) {
                // TODO Auto-generated method stub
                sessionInterface.onConnect(arg0, 0);
            }

            @Override
            public void onGotMeetingProperty(final JSONObject arg0) {
                //xiao

                try {
                    JSONObject meeting = arg0.getJSONObject("meeting");
                    String strCommPassword = "";
                    String mid = "";
                    final int meetingrole = arg0.optInt("meetingrole");
                    getUserMgr().getSelfUser().setRole(
                            meetingrole);
                    if (!meeting.isNull("confuserpwd")) {
                        strCommPassword = meeting
                                .getString("confuserpwd");
                    }
                    if (!meeting.isNull("serial")) {
                        mid = meeting.getString("serial");
                        myMeeting.setMeetingSerialid(mid);
                    }
                    if (!meeting.isNull("starttime")) {
                        myMeeting.setStartTime(meeting
                                .getInt("starttime"));
                    }
                    if (!meeting.isNull("endtime"))
                        myMeeting.setEndTime(meeting
                                .getInt("endtime"));

                    /** �������� */
                    if (!meeting.isNull("meetingname")) {
                        myMeeting.setMeetingTopic(meeting
                                .getString("meetingname"));
                        setM_strMeetingName(meeting
                                .getString("meetingname"));
                    }
                    if (!meeting.isNull("chairmanpwd"))
                        myMeeting.setChairmanpwd(meeting
                                .getString("chairmanpwd"));
                    if (!meeting.isNull("confuserpwd"))
                        myMeeting.setConfuserpwd(meeting
                                .getString("confuserpwd"));
                    if (!meeting.isNull("sidelineuserpwd"))
                        myMeeting.setSidelineuserpwd(meeting
                                .getString("sidelineuserpwd"));
                    if (!meeting.isNull("serial"))
                        myMeeting.setMeetingSerialid(meeting
                                .getString("serial"));
                    if (!meeting.isNull("meetingtype"))
                        myMeeting.setMeetingType(meeting
                                .getInt("meetingtype"));

                    myMeeting.setCreateID(meeting.optInt("userid"));
                    if (!meeting.isNull("videotype")) {
                        myMeeting.setVideoType(meeting.getInt("videotype"));
                    }


					/*
					 * chairmancontrol �������� 1111111001001111101000001000000000000000 1.��Ƶ 2.��Ƶ
					 * 3.�װ� 4.������ 5.���� 6.¼�� 7.����Ӱ��(δʵ��) 8.���鵽���Զ��˳�(0:���˳� 1:�˳�) 9.ͶƱ(δʵ��)
					 * 10.�ļ����� 11.���幦��(0:���� 1:����) 12.�ʴ�(δʵ��) 13.������ϯ(0:���� 1:��ʾ) 14.��������(0:����
					 * 1:��ʾ) 15.�ı����� 16.�û��б� 17.�ĵ��б� 18.�Ƿ���ͼ��ȱʡ0����ͼ,1������ͼ������ͼ����ҽ��ϵͳ����ʾ�������ڱߵ���Ƶ
					 * 19.��ҳ���� 20.�Զ��������(0:�Զ� 1:���Զ��� 21.�Ƿ���������Ƶ������(Flash)(0:������ 1:����) 22.sip�绰
					 * 23.H323�����ն˻�MCU 24.�Զ���������Ƶ 25.��˾�����Ƿ�ɼ� 26.�Ƿ���Դ�����˾ 27:�Ƿ�������Ƶ���ڹرհ�ť(Flash
					 * 0:��ʾ 1������) 28:�Ƿ�������Ƶ�����û���(Flash 0:��ʾ 1������) 29:�ȷ���Ƶ����������������(Flash 0:������
					 * 1��������), 30:���̲߳�����Ƶ 0:���߳�(ȱʡ) 1�����̣߳�ANDRIOD��Ҫ�����ֻ��ͺŸ�WEB API��31-40Ԥ������λ
					 */
                    if (!meeting.isNull("chairmancontrol")) {
                        // if(m_chairmancontrol
                        // ==
                        // "")
                        // {
                        m_chairmancontrol = meeting
                                .getString("chairmancontrol");
                        // }
                    }
                    if (!meeting.isNull("chairmanfunc")) {
                        if (m_chairmanfunc == "") {
                            m_chairmanfunc = meeting
                                    .getString("chairmanfunc");
                        }

                        if (!m_chairmanfunc.isEmpty()) {
                            // ����ȱʡֵ����PHP����
							/*
							 * chairmanfunc ��ϯ���� 1.�������������ɷ���
							 * 2.���������˻������Ȩ�� 3. ����������¼�� 4.���ָ���
							 * 5.��Ƶ����
							 */
                            char c = m_chairmanfunc.charAt(0);

                            if (c == '1')
                                setM_speakFree(true);
                            else
                                setM_speakFree(false);

                            c = m_chairmanfunc.charAt(1);
                            if (c == '1')
                                setM_controlFree(true);
                            else
                                setM_controlFree(false);

                            c = m_chairmanfunc.charAt(2);
                            if (c == '1')
                                setM_allowRecord(true);
                            else
                                setM_allowRecord(false);

                            c = m_chairmanfunc.charAt(4);

                            // thread except must sync ui thread
                            // xueqiang change
                            final char c1 = c;
                            Utitlties.RunOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (c1 == '1') {
                                        if (sessionInterface != null)
                                            sessionInterface.syncVideoModeChange(true,
                                                    true);
                                    } else {
                                        if (sessionInterface != null)
                                            sessionInterface.syncVideoModeChange(false,
                                                    false);
                                    }
                                }
                            });
                        }
                    } else {
                        m_chairmanfunc = "111000";
                        setM_speakFree(true);
                        setM_controlFree(true);
                        setM_allowRecord(true);
                    }
                    Utitlties.RunOnUIThread(new Runnable() {

                        @Override
                        public void run() {

                            if (sessionInterface != null) {
                                if (!isWeiYiVirsion && !noenter) {
                                    enterMeeting(name, meetingid, thirduid, false, usertype, null);
                                }
                                sessionInterface.onGotMeetingProperty(arg0);
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                //xiao
            }
        };
//		Map<String, Object> paramMap = new HashMap<String, Object>();
//		paramMap.put("cookie", cookie);
        if (paramMap != null && paramMap.get("isWeiYiVirsion") != null) {
            isWeiYiVirsion = (Boolean) paramMap.get("isWeiYiVirsion");
        } else {
            isWeiYiVirsion = false;
        }
        if (paramMap != null && paramMap.get("noenter") != null) {
            noenter = (Boolean) paramMap.get("noenter");
        } else {
            noenter = false;
        }
        RtmpClientMgr.getInstance().joinMeeting(webserverip, webserverport, name, meetingid, meetingpwd, thirduid, usertype, paramMap, null, callback);
    }

    //qxm add
    public void callServerFunctions(String functionName, Object parameters) {
        RtmpClientMgr.getInstance().callServerFunction(functionName, parameters);
    }

    //	public void startBroadCastings(String url,int type){
//		RtmpClientMgr.getInstance().startBroadCasting(url,type);
//	}
    public void notify(int toID, String functionName, Object parameters) {
        RtmpClientMgr.getInstance().callClientFunction(toID,
                functionName, parameters);
    }

    @Override
    public void onVideoSizeChanged(int arg0, int arg1, int arg2, int arg3) {
        if (sessionInterface != null) {
            sessionInterface.onVideoSizeChanged(arg0, arg1, arg2, arg3);
        }

    }

    public String getLIVE_MEDIA_SERVER() {
        return RtmpClientMgr.getInstance().LIVE_MEDIA_IP;
    }

    public String getLIVE_MEDIA_PORT() {
        return RtmpClientMgr.getInstance().LIVE_MEDIA_PORT;
    }


    public void enterMeeting(final String nickname, final String meeting_id, final int user_id,
                             final boolean serverMix, final int user_type, final String headurl) {
        if (padInterface != null) {
            padInterface.setWebImageDomain(RtmpClientMgr.getInstance().MEETING_DOC_ADDR);
            padInterface.getMeetingFile(Integer.valueOf(myMeeting.getMeetingSerialid()), webFun_getmeetingfile, new GetMeetingFileCallBack() {

                @Override
                public void GetmeetingFile(int code) {
                    if (m_sessionStatus != 0)
                        return;

                    m_sessionStatus = 1;


                    // clear();
                    m_thisUserMgr.getSelfUser().setName(nickname);
                    m_thisUserMgr.getSelfUser().setMmeID(user_id);
                    m_thisUserMgr.getSelfUser().setThirdID(user_id);
                    // m_watchMeCount = 0;
                    // chenji todo..��Ҫ�����������������������ʾ�û��б�������ִ�����hideme����ʾ��������Լ�����ʾ
                    // ���һ�����������true��ʾ���̲߳�����Ƶ�������Ƕ��̲߳�����Ƶ
                    String ip = "";
                    int port = 80;

                    if (isLiveMeeting()) {
                        ip = RtmpClientMgr.LIVE_SIGNAL_IP;
                        port = Integer.parseInt(RtmpClientMgr.LIVE_SIGNAL_PORT);
                    } else {
                        ip = RtmpClientMgr.MEDIA_SERVER_IP;
                        port = Integer.parseInt(RtmpClientMgr.MEDIA_SERVER_PORT);
                    }

                    JSONObject user_properties = null;
                    if (headurl != null && !headurl.isEmpty()) {
                        user_properties = new JSONObject();
                        try {
                            user_properties.put("m_headurl", headurl);
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    RtmpClientMgr.getInstance().enterMeeting(ip, port, getUserMgr().getSelfUser().getName(),
                            getM_strMeetingID(), getUserMgr().getSelfUser().getThirdID(), serverMix, user_type, getM_hideme() == 1,
                            isM_MultiThread(), user_properties);

                }
            });
        }


    }

    /**
     * 0:���̲߳�����Ƶ 1:���̲߳�����Ƶ
     *
     * @return
     */
    public boolean isM_MultiThread() {
        if (m_chairmancontrol.length() > 29) {
            char c = m_chairmancontrol.charAt(29);
            if (c == '1') {
                return true;
            }
        }
        return false;
    }


    public boolean getServerRecordingStatus() {
        return m_bServerRecording;
    }

    /**
     * �Ƿ����������¼��
     *
     * @return
     */
    public boolean isAllowServerRecord() {
        if (m_isAllowServerRecord == -1) {
            if (m_chairmancontrol.length() > 30) {
                char c = m_chairmancontrol.charAt(30);
                if (c == '1')
                    return true;
            }
        } else {
            return m_isAllowServerRecord == 1 ? true : false;
        }

        return false;
    }

    /**
     * �Ƿ��Զ�������¼��
     *
     * @return
     */
    public boolean isAutoServerRecord() {
        if (m_isAutoServerRecord == -1) {
            if (m_chairmancontrol.length() > 31) {
                char c = m_chairmancontrol.charAt(31);
                if (c == '1') {
                    return true;
                }
            }
        } else {
            return m_isAutoServerRecord == 1 ? true : false;
        }

        return false;
    }

    /***
     * xiaoyang
     * �Ƿ���sip����
     */
    public boolean isSipMeeting() {
        if (m_isSipMeeting == -1) {
            if (m_chairmancontrol.length() > 21) {
                char c = m_chairmancontrol.charAt(21);
                if (c == '1') {
                    return true;
                }
            }
        } else {
            return m_isSipMeeting == 1 ? true : false;
        }

        return false;
    }

    /***
     * call sip
     */
    public void callSipPhone(String terminalNum, String nickname, int action) {
        JSONArray array = new JSONArray();
        array.put(null);
        array.put(terminalNum);
        array.put(nickname);
        RtmpClientMgr.getInstance().callServerFunction((action == 0 ? "ServerFunc_MakeCall" : "ServerFunc_HangUp"), array);
    }

    private void onCallSipACK(int mark, int state) {
        callSipState = state;
        if (sessionInterface != null) {
            sessionInterface.onCallSipACK(mark, state);
        }
    }

    public void setVideoForSip(int peerid, int videoid) {
        JSONObject object = new JSONObject();
        try {
            object.put("userID", peerid);
            object.put("videoID", videoid);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        onVideoForSipChange(peerid, videoid);
        if (peerid != 0)
            PublishMessage("VideoForSip", SENDMSGTOALL_EXCEPT_ME, peerid, object);
        else
            DeleteMessage("VideoForSip", SENDMSGTOALL_EXCEPT_ME, peerid, object);
    }

    private void onVideoForSipChange(int peerid, int videoid) {
//		if (videoForSipPeerId == peerid&&videoForSipVideoId == videoid)
//			return;
        if (peerid == getMyPID()) {
            ClientFunc_WatchBuddyVideo(0);
        } else if (videoForSipPeerId == getMyPID()) {
            ClientFunc_UnWatchBuddyVideo(0);
        }
        videoForSipPeerId = peerid;
        videoForSipVideoId = videoid;
        m_thisUserMgr.reSort();
        if (sessionInterface != null) {
            sessionInterface.onFocusSipChange(peerid, videoid);
        }
    }

    public int getVideoPeerIdForSip() {
        return videoForSipPeerId;
    }

    public int getVideoIdForSip() {
        return videoForSipVideoId;
    }
//	public void setVideoIdAndPeerIdForSip(int videoForSipPeerId,int videoForSipVideoId){
//		this.videoForSipPeerId = videoForSipPeerId;
//		this.videoForSipVideoId = videoForSipVideoId;
//	}


    //cyj 20161029
    public void sendAddWBPage() {

        JSONArray arr = new JSONArray();
        try {
            arr.put(0, null);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        RtmpClientMgr.getInstance().callServerFunction(
                "ServerFunc_WBAddDoc", arr);
    }

    public void sendDelWBPage() {

        JSONArray arr = new JSONArray();
        try {
            arr.put(0, null);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        RtmpClientMgr.getInstance().callServerFunction(
                "ServerFunc_WBDelDoc", arr);
    }

    public void sendWBBackColor(int color) {
        JSONObject body = new JSONObject();
        try {
            body.put("color", (long) color);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        PublishMessage("WBBackColor", SENDMSGTOALL_EXCEPT_ME, 0, body, "WBBackColor");
    }

    public boolean isHighquality() {
        return myMeeting.getVideoType() == 1;
    }

    @Override
    public void onWatchStopped(int arg0, int arg1) {
        if (sessionInterface != null) {
            sessionInterface.onWatchStopped(arg0, arg1);
        }

    }
}

/*
\ * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.messenger;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONObject;



import com.utils.MeetingNotify;
import com.utils.WeiyiMeeting;

import info.emm.LocalData.Config;
import info.emm.LocalData.DataAdapter;
import info.emm.LocalData.DateUnit;
import info.emm.im.meeting.MeetingMgr;
import info.emm.messenger.ContactsController.Contact;
import info.emm.messenger.LocaleController.LocaleInfo;
import info.emm.messenger.TLRPC.TL_DirectPlayBackList;
import info.emm.messenger.TLRPC.TL_error;
import info.emm.messenger.TLRPC.User;
import info.emm.objects.MessageObject;
import info.emm.objects.PhotoObject;
import info.emm.services.UEngine;
import info.emm.ui.AlertActivity;
import info.emm.ui.ApplicationLoader;
import info.emm.ui.CharacterParser;
import info.emm.ui.LaunchActivity;
import info.emm.ui.PhoneActivity;
import info.emm.ui.Views.SpecialCalendar;
import info.emm.utils.ConstantValues;
import info.emm.utils.StringUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.TreeSet;

import com.utils.WeiyiMeetingNotificationCenter;


/**
 * @ClassName: MessagesController
 * 
 * @Description: TODO
 * 
 * @Author: He,Zhen hezhen@yunboxin.com
 * 
 * @Date: 2014-11-11
 * 
 */
public class MessagesController implements
NotificationCenter.NotificationCenterDelegate,WeiyiMeetingNotificationCenter.NotificationCenterDelegate,MeetingNotify {


	public ConcurrentHashMap<Integer, TLRPC.Chat> chats = new ConcurrentHashMap<Integer, TLRPC.Chat>(
			100, 1.0f, 2);
	public ConcurrentHashMap<Integer, TLRPC.EncryptedChat> encryptedChats = new ConcurrentHashMap<Integer, TLRPC.EncryptedChat>(
			10, 1.0f, 2);
	// 锟斤拷锟斤拷锟矫伙拷锟斤拷息,通锟斤拷userid锟芥储锟矫伙拷MAP
	public ConcurrentHashMap<Integer, TLRPC.User> users = new ConcurrentHashMap<Integer, TLRPC.User>(
			100, 1.0f, 2);
	// 锟斤拷锟斤拷锟矫伙拷锟斤拷息,通锟斤拷identifier锟芥储锟矫伙拷MAP
	public ConcurrentHashMap<String, TLRPC.User> usersSDK = new ConcurrentHashMap<String, TLRPC.User>(
			100, 1.0f, 2);
	// jenf
	public ArrayList<TLRPC.User> searchUsers = new ArrayList<TLRPC.User>();
	public ConcurrentHashMap<Integer, TLRPC.User> selectedUsers = new ConcurrentHashMap<Integer, TLRPC.User>(
			100, 1.0f, 2);

	public HashMap<Integer, TLRPC.User> ignoreUsers = new HashMap<Integer, TLRPC.User>();

	// xueqiang add
	// 锟斤拷司id锟斤拷应锟斤拷司锟斤拷息
	public ConcurrentHashMap<Integer, TLRPC.TL_Company> companys = new ConcurrentHashMap<Integer, TLRPC.TL_Company>(
			100, 1.0f, 2);
	// 锟斤拷锟斤拷id锟斤拷应锟斤拷锟斤拷锟斤拷息
	public ConcurrentHashMap<Integer, TLRPC.TL_DepartMent> departments = new ConcurrentHashMap<Integer, TLRPC.TL_DepartMent>(
			100, 1.0f, 2);
	// 锟斤拷锟斤拷id锟斤拷应锟斤拷锟斤拷锟矫伙拷锟斤拷息
	public ConcurrentHashMap<Integer, HashSet<TLRPC.TL_UserCompany>> departidToUsers = new ConcurrentHashMap<Integer, HashSet<TLRPC.TL_UserCompany>>(
			100, 1.0f, 2);

	public ArrayList<TLRPC.TL_ChannalInfo> localChnInfos = new ArrayList<TLRPC.TL_ChannalInfo>();
	public ArrayList<TLRPC.TL_ChannalInfo> remoteChnInfos = new ArrayList<TLRPC.TL_ChannalInfo>();
	// wangxm add
	// public HashMap<Integer, TLRPC.User> addNewMemIgnoreUsers;

	public ConcurrentHashMap<Integer, TLRPC.TL_PendingCompanyInfo> pendingCompanys = new ConcurrentHashMap<Integer, TLRPC.TL_PendingCompanyInfo>(
			100, 1.0f, 2);
	public ArrayList<TLRPC.TL_PendingCompanyInfo> invitedCompanys = new ArrayList<TLRPC.TL_PendingCompanyInfo>();
	// public ArrayList<TLRPC.TL_UserCompany> userCompanys = new
	// ArrayList<TLRPC.TL_UserCompany>();
	// 锟截硷拷锟斤拷为userid+" "+companyid锟斤拷锟斤拷锟斤拷锟斤拷锟接凤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷usercompany锟斤拷息锟斤拷锟斤拷锟皆猴拷锟斤拷锟阶分辨，user锟侥诧拷锟脚变化锟斤拷息
	public ConcurrentHashMap<String, TLRPC.TL_UserCompany> userCompanysMap = new ConcurrentHashMap<String, TLRPC.TL_UserCompany>(
			100, 1.0f, 2);
	// 锟芥储锟揭绑定碉拷锟绞猴拷
	// public ArrayList<String> accounts = new ArrayList<String>();
	/**
	 * wangxm 锟斤拷锟斤拷锟叫憋拷锟斤拷锟斤拷
	 */
	public ConcurrentHashMap<Integer, TLRPC.TL_MeetingInfo> meetings = new ConcurrentHashMap<Integer, TLRPC.TL_MeetingInfo>(
			100, 1.0f, 2);
	public ArrayList<TLRPC.TL_MeetingInfo> meetingList = new ArrayList<TLRPC.TL_MeetingInfo>();
	public ArrayList<TLRPC.TL_MeetingInfo> broadcastMeetingList = new ArrayList<TLRPC.TL_MeetingInfo>();
	public ConcurrentHashMap<String, TLRPC.TL_DirectPlayBackList> directImgMap = new ConcurrentHashMap<String,TLRPC.TL_DirectPlayBackList>(100, 1.0f, 2);
	public ConcurrentHashMap<Integer, ArrayList<TLRPC.TL_DirectPlayBackList>> directMap = new ConcurrentHashMap<Integer, ArrayList<TL_DirectPlayBackList>>();
	/**
	 * @Fields meeting2List : 锟斤拷锟斤拷锟叫憋拷
	 */
	public ConcurrentHashMap<String, ArrayList<TLRPC.TL_MeetingInfo>> everyDayMeeting = new ConcurrentHashMap<String, ArrayList<TLRPC.TL_MeetingInfo>>();

	public ConcurrentHashMap<String, TLRPC.TL_PSTNMeeting> meeting2Map = new ConcurrentHashMap<String, TLRPC.TL_PSTNMeeting>();
	public ArrayList<TLRPC.TL_PSTNMeeting> meeting2List = new ArrayList<TLRPC.TL_PSTNMeeting>();
	// 锟芥储锟斤拷锟斤拷锟街伙拷锟斤拷系锟斤拷
	public ConcurrentHashMap<Integer, Contact> contactsMap;
	public ConcurrentHashMap<Integer, Contact> contactsMapNew = new ConcurrentHashMap<Integer, Contact>();
	// 锟斤拷锟皆诧拷锟揭憋拷锟斤拷锟斤拷系锟斤拷锟斤拷锟斤拷锟斤拷业锟斤拷锟较碉拷司锟斤拷锟绞撅拷只锟斤拷牛锟斤拷锟斤拷锟斤拷锟绞�
	public ConcurrentHashMap<String, Contact> contactsMD5Map = new ConcurrentHashMap<String, Contact>(
			100, 1.0f, 2);
	public ArrayList<DataAdapter> contactsArray = new ArrayList<DataAdapter>();
	/**
	 * 默锟斤拷锟斤拷锟斤拷锟斤拷 锟斤拷锟斤拷状态- 2
	 */
	public int connect_state = 2;
	/**
	 * 锟斤拷锟斤拷锟斤拷史锟斤拷 全锟斤拷锟叫憋拷 ArrayList
	 */
	public ArrayList<TLRPC.TL_dialog> dialogs = new ArrayList<TLRPC.TL_dialog>();
	public ArrayList<TLRPC.TL_dialog> dialogsServerOnly = new ArrayList<TLRPC.TL_dialog>();
	public ConcurrentHashMap<Long, TLRPC.TL_dialog> dialogs_dict = new ConcurrentHashMap<Long, TLRPC.TL_dialog>(
			100, 1.0f, 2);
	public SparseArray<MessageObject> dialogMessage = new SparseArray<MessageObject>();
	public ConcurrentHashMap<Long, ArrayList<PrintingUser>> printingUsers = new ConcurrentHashMap<Long, ArrayList<PrintingUser>>(
			100, 1.0f, 2);
	public HashMap<Long, CharSequence> printingStrings = new HashMap<Long, CharSequence>();

	private HashMap<String, ArrayList<DelayedMessage>> delayedMessages = new HashMap<String, ArrayList<DelayedMessage>>();
	// xueqiang add for netmsg map
	private HashMap<Integer, Integer> msgMap = new HashMap<Integer, Integer>();
	public SortedSet<String> set;
	/**
	 * 准锟斤拷锟斤拷锟斤拷锟竭碉拷锟斤拷锟捷讹拷锟襟集猴拷
	 */
	public SparseArray<MessageObject> sendingMessages = new SparseArray<MessageObject>();
	public SparseArray<TLRPC.User> hidenAddToContacts = new SparseArray<TLRPC.User>();
	private SparseArray<TLRPC.EncryptedChat> acceptingChats = new SparseArray<TLRPC.EncryptedChat>();
	private ArrayList<TLRPC.Updates> updatesQueue = new ArrayList<TLRPC.Updates>();
	private ArrayList<Long> pendingEncMessagesToDelete = new ArrayList<Long>();
	private long updatesStartWaitTime = 0;
	public ArrayList<TLRPC.Update> delayedEncryptedChatUpdates = new ArrayList<TLRPC.Update>();
	private boolean startingSecretChat = false;

	private boolean gettingNewDeleteTask = false;
	private int currentDeletingTaskTime = 0;
	private Long currentDeletingTask = null;
	private ArrayList<Integer> currentDeletingTaskMids = null;

	public int totalDialogsCount = 0;
	public boolean loadingDialogs = false;
	public boolean loadingContacts = true;
	public boolean dataloaded = false;
	public boolean dialogsEndReached = false;
	public boolean gettingDifference = false;
	public boolean gettingDifferenceAgain = false;
	public boolean updatingState = false;
	public boolean firstGettingTask = false;
	public boolean registeringForPush = false;
	private long lastSoundPlay = 0;
	private long lastStatusUpdateTime = 0;
	private long statusRequest = 0;
	private int statusSettingState = 0;
	private boolean offlineSent = false;
	private String uploadingAvatar = null;

	private long lastVibrator = 0;

	public static boolean localInfoLoaded = false;

	public static SecureRandom random = new SecureRandom();
	public boolean enableJoined = true;
	public int fontSize = Utilities.dp(16);
	public long scheduleContactsReload = 0;

	public boolean earphone = false;
	/**
	 * 锟斤拷锟斤拷锟斤拷锟斤拷应锟缴癸拷
	 */
	public final static int SERVER_RESPONSE_SUCCESS = 10000;

	public MessageObject currentPushMessage;

	// jenf for language
	public LocaleInfo currentLocaleInfo = null;
	// wangxm add for meet alert handler
	public Handler meetAlertHandler;

	// sam for multi device
	public int connectResult = 0;
	// wangxm add for sort
	private Comparator<DataAdapter> compare = new PinYinSort();

	private Timer serviceTimer;

	// private ArrayList<Activity> activityBackTast = new ArrayList<Activity>();

	// private Map<Integer,TLRPC.TL_Affiche> tlAfficheList = new
	// HashMap<Integer,TLRPC.TL_Affiche>();

	// private Map<Integer, Map<Integer,TLRPC.TL_Affiche>> tlAfficheList = new
	// HashMap<Integer, Map<Integer,TLRPC.TL_Affiche>>();

	private Map<Integer, List<Integer>> afficheNum4CompanyMap = new HashMap<Integer, List<Integer>>(); // int
	// companyid
	// int
	// bbsid

	private Map<Integer, TLRPC.TL_Affiche> tlAfficheMap = new HashMap<Integer, TLRPC.TL_Affiche>(); // key
	// bbsid,value
	// 未锟斤拷锟斤拷锟斤拷
	// 锟斤拷时锟斤拷锟斤拷


	public ConcurrentHashMap<Integer, ArrayList<Integer>> gidToUserList = new ConcurrentHashMap<Integer, ArrayList<Integer>>(100, 1.0f, 2);
	public ConcurrentHashMap<Integer, String> gidToMid = new ConcurrentHashMap<Integer, String>(100, 1.0f, 2);
	public ConcurrentHashMap<String, ArrayList<TLRPC.TL_MeetingInfo>> meetingMap = new ConcurrentHashMap<String, ArrayList<TLRPC.TL_MeetingInfo>>();
	public TreeSet<String> groupLists = new TreeSet<String>();

	private class UserActionUpdates extends TLRPC.Updates {

	}

	static {
		try {
			File URANDOM_FILE = new File("/dev/urandom");
			FileInputStream sUrandomIn = new FileInputStream(URANDOM_FILE);
			byte[] buffer = new byte[1024];
			sUrandomIn.read(buffer);
			sUrandomIn.close();
			random.setSeed(buffer);
		} catch (Exception e) {
			FileLog.e("emm", e);
		}
	}

	public static final int MESSAGE_SEND_STATE_SENDING = 1;
	public static final int MESSAGE_SEND_STATE_SENT = 0;
	public static final int MESSAGE_SEND_STATE_SEND_ERROR = 2;

	public static final int UPDATE_MASK_NAME = 1;
	public static final int UPDATE_MASK_AVATAR = 2;
	public static final int UPDATE_MASK_STATUS = 4;
	public static final int UPDATE_MASK_CHAT_AVATAR = 8;
	public static final int UPDATE_MASK_CHAT_NAME = 16;
	public static final int UPDATE_MASK_CHAT_MEMBERS = 32;
	public static final int UPDATE_MASK_USER_PRINT = 64;
	public static final int UPDATE_MASK_USER_PHONE = 128;
	public static final int UPDATE_MASK_READ_DIALOG_MESSAGE = 256;
	public static final int UPDATE_MASK_ALL = UPDATE_MASK_AVATAR
			| UPDATE_MASK_STATUS | UPDATE_MASK_NAME | UPDATE_MASK_CHAT_AVATAR
			| UPDATE_MASK_CHAT_NAME | UPDATE_MASK_CHAT_MEMBERS
			| UPDATE_MASK_USER_PRINT | UPDATE_MASK_USER_PHONE
			| UPDATE_MASK_READ_DIALOG_MESSAGE;

	public long openned_dialog_id;

	private final static Integer sync = 1;

	private boolean bSartPersonalContactService = false;

	public static class PrintingUser {
		public long lastTime;
		public int userId;
	}

	/*
	 * public ArrayList<Activity> getBackActivity() { if (activityBackTast ==
	 * null) { activityBackTast = new ArrayList<Activity>(); } return
	 * activityBackTast; }
	 */
	private class DelayedMessage {
		public TLRPC.TL_messages_sendMedia sendRequest;
		public TLRPC.TL_decryptedMessage sendEncryptedRequest;
		public int type;
		public TLRPC.FileLocation location;
		public TLRPC.TL_video videoLocation;
		public TLRPC.TL_audio audioLocation;
		public TLRPC.TL_document documentLocation;
		public MessageObject obj;
		public TLRPC.EncryptedChat encryptedChat;
	}

	private class MyContentObserver extends ContentObserver {

		public MyContentObserver() {
			super(null);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			Utilities.stageQueue.postRunnable(new Runnable() {
				@Override
				public void run() {
					MessagesController.getInstance().scheduleContactsReload = System.currentTimeMillis() + 2000;					
				}
			});
		}

		@Override
		public boolean deliverSelfNotifications() {
			return false;
		}
	}

	public static final int didReceivedNewMessages = 1;
	public static final int updateInterfaces = 3;
	public static final int dialogsNeedReload = 4;
	public static final int closeChats = 5;
	public static final int messagesDeleted = 6;
	public static final int messagesReaded = 7;
	public static final int messagesDidLoaded = 8;

	public static final int messageReceivedByAck = 9;
	public static final int messageReceivedByServer = 10;
	public static final int messageSendError = 11;

	public static final int reloadSearchResults = 12;

	public static final int contactsDidLoaded = 13;

	public static final int chatDidCreated = 15;
	public static final int chatDidFailCreate = 16;

	public static final int chatInfoDidLoaded = 17;

	public static final int mediaDidLoaded = 18;
	public static final int mediaCountDidLoaded = 20;

	public static final int encryptedChatUpdated = 21;
	public static final int messagesReadedEncrypted = 22;
	public static final int encryptedChatCreated = 23;

	public static final int userPhotosLoaded = 24;

	public static final int removeAllMessagesFromDialog = 25;
	// 锟斤拷锟斤拷锟斤拷氐锟斤拷锟较拷锟斤拷澹瑆angxm begin
	public static final int meeting_create_failed = 27;
	public static final int meeting_list_delete = 28;
	public static final int meeting_list_update = 29;
	public static final int meeting_list_updateUI = 30;


	// 锟斤拷锟斤拷锟斤拷锟斤拷锟节伙拷锟斤拷锟叫碉拷锟斤拷息,inMeetingActivity锟斤拷应锟斤拷锟斤拷息锟斤拷锟斤拷rtmpclientmgr锟斤拷锟斤拷锟斤拷些锟斤拷息
	public static final int meeting_userin = 32;
	public static final int meeting_userout = 33;
	public static final int meeting_enablePresence = 34;
	public static final int meeting_connected = 35;
	public static final int meeting_disconnected = 36;
	public static final int meeting_speaking_status_change = 39;
	public static final int meeting_userin_nobody = 59;

	public static final int unread_message_update = 37;
	public static final int play_audio_completed = 38;

	// wangxm add
	public static final int meet_member_operator_error = 40;
	public static final int meet_infos_needreload = 41;
	public static final int company_create_failed = 42;
	public static final int company_create_success = 43;
	public static final int company_name_changed = 44;

	public static final int company_delete = 46;

	public static final int pending_company_loaded = 47;
	public static final int pending_company_added = 48;

	public static final int bind_account_success = 49;
	public static final int bind_account_failed = 50;
	public static final int unbind_account_success = 51;
	public static final int unbind_account_failed = 52;
	public static final int getcode_failed = 53;
	public static final int getcode_success = 54;
	public static final int checkcode_failed = 55;
	public static final int checkcode_success = 56;
	// 锟矫伙拷锟斤拷应锟斤拷锟斤拷锟叫凤拷锟斤拷锟铰硷拷
	public static final int meeting_call_response = 57;
	public static final int meeting_notice_bar = 58;

	// wangxm add 锟斤拷拥锟斤拷锟斤拷锟斤拷锟斤拷时锟斤拷锟阶筹拷一锟斤拷锟斤拷息
	// public static final int ProgramInitialized=38;

	public static final int retransmit_new_chat = 59;

	public static final int create_group_final = 60;

	public static final int meeting_call_end = 61; // 锟斤拷锟斤拷锟斤拷氐锟斤拷锟较拷锟斤拷澹瑆angxm end

	public static final int other_have_call = 62; // 通锟斤拷锟皆凤拷锟斤拷锟斤拷锟斤拷

	public static final int newforumaffice = 63; // 锟铰癸拷锟斤拷锟斤拷息

	public static final int refreshforumaffice = 64; // 锟铰癸拷锟斤拷锟斤拷息

	public static final int forumpublishsuccess = 65; // 锟斤拷坛锟斤拷锟酵成癸拷
	public static final int renamesuccess = 66; // 锟斤拷坛锟斤拷锟酵成癸拷
	public static final int renamefailed = 67; // 锟斤拷坛锟斤拷锟酵成癸拷

	public static final int alermDidLoaded = 68; //
	public static final int company_username_changed = 69; // 锟斤拷司锟斤拷员锟斤拷锟街凤拷锟斤拷锟剿变化

	public static final int PSTNControl_Notify = 70; // PSTN通知
	public static final int PSTNControl_Notify_error = 71; // PSTN通知

	public static final int EnterMeeting_Complete = 72; // PSTN通知

	//锟斤拷刷锟铰伙拷锟斤拷锟绞憋拷蚍祷锟�
	public static final int getall_meeting = 73;
	public static final int directplayback_notify = 74;
	public static final int directplayback_finsh_notify = 75;
	public static final int direct_list_delete = 76;

	private int msgCount = 0;
	private ArrayList<Integer> msgUserList = new ArrayList<Integer>();
	private ArrayList<Integer> msgGroupList = new ArrayList<Integer>();

	public void releaseNotificationData() {
		msgCount = 0;
		if (msgUserList != null) {
			msgUserList.clear();
		}
		if (msgGroupList != null) {
			msgGroupList.clear();
		}

	}

	private static volatile MessagesController Instance = null;

	public static MessagesController getInstance() {
		MessagesController localInstance = Instance;
		if (localInstance == null) {
			synchronized (MessagesController.class) {
				localInstance = Instance;
				if (localInstance == null) {
					Instance = localInstance = new MessagesController();
				}
			}
		}
		return localInstance;
	}

	public MessagesController() {
		MessagesStorage storage = MessagesStorage.getInstance();
		NotificationCenter.getInstance().addObserver(this,
				FileLoader.FileDidUpload);
		NotificationCenter.getInstance().addObserver(this,
				FileLoader.FileDidFailUpload);
		NotificationCenter.getInstance().addObserver(this, 10);

		//xueqiang todo..

		addSupportUser();
		SharedPreferences preferences = ApplicationLoader.applicationContext
				.getSharedPreferences("Notifications_"
						+ UserConfig.clientUserId, Activity.MODE_PRIVATE);
		enableJoined = preferences.getBoolean("EnableContactJoined", true);
		preferences = ApplicationLoader.applicationContext
				.getSharedPreferences("mainconfig_" + UserConfig.clientUserId,
						Activity.MODE_PRIVATE);
		fontSize = preferences.getInt("fons_size", 16);
		earphone = preferences.getBoolean("earphone", false);

		Utilities.globalQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				ApplicationLoader.applicationContext.getContentResolver()
				.registerContentObserver(
						ContactsContract.Contacts.CONTENT_URI, true,
						new MyContentObserver());
			}
		});
	}

	public void addSupportUser() {
		// todo..锟斤拷时去锟斤拷锟斤拷锟斤拷锟斤拷锟街э拷锟斤拷没锟�
		/*
		 * TLRPC.TL_userForeign user = new TLRPC.TL_userForeign(); user.phone =
		 * "333"; user.id = 333000; user.first_name = "Emm"; user.last_name =
		 * ""; user.status = null; user.photo = new
		 * TLRPC.TL_userProfilePhotoEmpty(); TLRPC.TL_userStatusEmpty userStatus
		 * = new TLRPC.TL_userStatusEmpty(); userStatus.expires =1; user.status
		 * = userStatus; users.put(user.id, user);
		 */
	}

	public static TLRPC.InputUser getInputUser(TLRPC.User user) {
		if (user == null) {
			return null;
		}
		TLRPC.InputUser inputUser = null;
		if (user.id == UserConfig.clientUserId) {
			inputUser = new TLRPC.TL_inputUserSelf();
			inputUser.user_id = user.id;// xueqiang add the line
		} else if (user instanceof TLRPC.TL_userForeign
				|| user instanceof TLRPC.TL_userRequest) {
			inputUser = new TLRPC.TL_inputUserForeign();
			inputUser.user_id = user.id;
			// inputUser.access_hash = user.access_hash;
		} else {
			inputUser = new TLRPC.TL_inputUserContact();
			inputUser.user_id = user.id;
		}
		return inputUser;
	}

	@Override
	public void didReceivedNotification(int id, Object... args) {
		if (id == FileLoader.FileDidUpload) 
		{
			if(args.length>3)
			{
				fileDidUploaded((String) args[0], (TLRPC.InputFile) args[1],
						(TLRPC.InputEncryptedFile) args[2], (String) args[3]);
			}
		} else if (id == FileLoader.FileDidFailUpload) {
			fileDidFailedUpload((String) args[0], (Boolean) args[1]);
		} else if (id == messageReceivedByServer) {
			Integer msgId = (Integer) args[0];
			MessageObject obj = dialogMessage.get(msgId);
			if (obj != null) {
				Integer newMsgId = (Integer) args[1];
				dialogMessage.remove(msgId);
				dialogMessage.put(newMsgId, obj);
				obj.messageOwner.id = newMsgId;
				obj.messageOwner.send_state = MessagesController.MESSAGE_SEND_STATE_SENT;

				long uid;
				if (obj.messageOwner.to_id.chat_id != 0) {
					uid = -obj.messageOwner.to_id.chat_id;
				} else {
					if (obj.messageOwner.to_id.user_id == UserConfig.clientUserId) {
						obj.messageOwner.to_id.user_id = obj.messageOwner.from_id;
					}
					uid = obj.messageOwner.to_id.user_id;
				}

				TLRPC.TL_dialog dialog = dialogs_dict.get(uid);
				if (dialog != null) {
					if (dialog.top_message == msgId) {
						dialog.top_message = newMsgId;
					}
				}

				NotificationCenter.getInstance().postNotificationName(
						dialogsNeedReload);
			}
		}		
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		NotificationCenter.getInstance().removeObserver(this,
				FileLoader.FileDidUpload);
		NotificationCenter.getInstance().removeObserver(this,
				FileLoader.FileDidFailUpload);
		NotificationCenter.getInstance().removeObserver(this,
				messageReceivedByServer);
	}

	public void cleanUp() {
		ContactsController.getInstance().cleanup();
		MediaController.getInstance().cleanup();
		// tlAfficheList.clear();
		tlAfficheMap.clear();
		afficheNum4CompanyMap.clear();
		dialogs_dict.clear();
		dialogs.clear();
		dialogsServerOnly.clear();
		acceptingChats.clear();
		users.clear();
		chats.clear();
		sendingMessages.clear();
		delayedMessages.clear();
		dialogMessage.clear();
		printingUsers.clear();
		printingStrings.clear();
		totalDialogsCount = 0;
		hidenAddToContacts.clear();
		updatesQueue.clear();
		pendingEncMessagesToDelete.clear();
		delayedEncryptedChatUpdates.clear();

		updatesStartWaitTime = 0;
		currentDeletingTaskTime = 0;
		scheduleContactsReload = 0;
		currentDeletingTaskMids = null;
		gettingNewDeleteTask = false;
		currentDeletingTask = null;
		loadingDialogs = false;
		loadingContacts = true;
		dataloaded = false;
		dialogsEndReached = false;
		gettingDifference = false;
		gettingDifferenceAgain = false;
		firstGettingTask = false;
		updatingState = false;
		lastStatusUpdateTime = 0;
		offlineSent = false;
		registeringForPush = false;
		uploadingAvatar = null;
		startingSecretChat = false;
		statusRequest = 0;
		statusSettingState = 0;
		addSupportUser();
		// 注锟斤拷锟斤拷锟铰碉拷录要锟斤拷锟铰匡拷始
		ConnectionsManager.getInstance().setVersion(-1);
		// sam
		companys.clear();
		departments.clear();
		departidToUsers.clear();
		meetings.clear();
		meetingList.clear();
		broadcastMeetingList.clear();

		msgMap.clear();

		localChnInfos.clear();
		remoteChnInfos.clear();
		// 锟斤拷锟斤拷锟斤拷通讯录锟斤拷同锟斤拷TIMER
		if (serviceTimer != null) {
			serviceTimer.cancel();
			serviceTimer = null;
		}
		ConnectionsManager.getInstance().stopUpdateTimer();
		// accounts.clear();
		userCompanysMap.clear();
		pendingCompanys.clear();
		invitedCompanys.clear();

		meeting2Map.clear();
		meeting2List.clear();
		bSartPersonalContactService  =false;

		if(contactsMap!=null)
			contactsMap.clear();
		contactsMapNew.clear();
		// 锟斤拷锟皆诧拷锟揭憋拷锟斤拷锟斤拷系锟斤拷锟斤拷锟斤拷锟斤拷业锟斤拷锟较碉拷司锟斤拷锟绞撅拷只锟斤拷牛锟斤拷锟斤拷锟斤拷锟绞�
		contactsMD5Map.clear();
		usersSDK.clear();
		directImgMap.clear();
		directMap.clear();
		MeetingMgr.getInstance().clearUp();
	}

	public void didAddedNewTask(final int minDate) {
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				if (currentDeletingTask == null && !gettingNewDeleteTask
						|| currentDeletingTaskTime != 0
						&& minDate < currentDeletingTaskTime) {
					getNewDeleteTask(null);
				}
			}
		});
	}

	public void getNewDeleteTask(final Long oldTask) {
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				gettingNewDeleteTask = true;
				MessagesStorage.getInstance().getNewTask(oldTask);
			}
		});
	}

	private void checkDeletingTask() {
		int currentServerTime = ConnectionsManager.getInstance()
				.getCurrentTime();

		if (currentDeletingTask != null && currentDeletingTaskTime != 0
				&& currentDeletingTaskTime <= currentServerTime) {
			currentDeletingTaskTime = 0;
			Utilities.RunOnUIThread(new Runnable() {
				@Override
				public void run() {
					// xueqiang change
					deleteMessages(currentDeletingTaskMids, null, null, true);

					Utilities.stageQueue.postRunnable(new Runnable() {
						@Override
						public void run() {
							getNewDeleteTask(currentDeletingTask);
							currentDeletingTaskTime = 0;
							currentDeletingTask = null;
						}
					});
				}
			});
		}
	}

	public void processLoadedDeleteTask(final Long taskId, final int taskTime,
			final ArrayList<Integer> messages) {
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				gettingNewDeleteTask = false;
				if (taskId != null) {
					currentDeletingTaskTime = taskTime;
					currentDeletingTask = taskId;
					currentDeletingTaskMids = messages;

					checkDeletingTask();
				} else {
					currentDeletingTaskTime = 0;
					currentDeletingTask = null;
					currentDeletingTaskMids = null;
				}
			}
		});
	}

	public void deleteAllAppAccounts() {
		// sam
		// try {
		// AccountManager am =
		// AccountManager.get(ApplicationLoader.applicationContext);
		// Account[] accounts =
		// am.getAccountsByType("info.emm.messenger.account");
		// for (Account c : accounts) {
		// am.removeAccount(c, null, null);
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	public void loadUserPhotos(final int uid, final int offset,
			final int count, final long max_id, final boolean fromCache,
			final int classGuid) {
		if (fromCache) {
			MessagesStorage.getInstance().getUserPhotos(uid, offset, count,
					max_id, classGuid);
		} else {
			TLRPC.User user = users.get(uid);
			if (user == null) {
				return;
			}
			TLRPC.TL_photos_getUserPhotos req = new TLRPC.TL_photos_getUserPhotos();
			req.limit = count;
			req.offset = offset;
			req.max_id = (int) max_id;
			req.user_id = getInputUser(user);
			long reqId = ConnectionsManager.getInstance().performRpc(req,
					new RPCRequest.RPCRequestDelegate() {
				@Override
				public void run(TLObject response, TLRPC.TL_error error) {
					if (error == null) {
						TLRPC.photos_Photos res = (TLRPC.photos_Photos) response;
						processLoadedUserPhotos(res, uid, offset,
								count, max_id, fromCache, classGuid);
					}
				}
			}, null, true, RPCRequest.RPCRequestClassGeneric);
			// xueqiang delete
			// ConnectionsManager.getInstance().bindRequestToGuid(reqId,
			// classGuid);
		}
	}

	public void processLoadedUserPhotos(final TLRPC.photos_Photos res,
			final int uid, final int offset, final int count,
			final long max_id, final boolean fromCache, final int classGuid) {
		if (!fromCache) {
			MessagesStorage.getInstance().putUserPhotos(uid, res);
		} else if (res == null || res.photos.isEmpty()) {
			loadUserPhotos(uid, offset, count, max_id, false, classGuid);
			return;
		}
		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				NotificationCenter.getInstance().postNotificationName(
						userPhotosLoaded, uid, offset, count, fromCache,
						classGuid, res.photos);
			}
		});
	}

	public void processLoadedMedia(final TLRPC.messages_Messages res,
			final long uid, int offset, int count, int max_id,
			final boolean fromCache, final int classGuid) {
		int lower_part = (int) uid;
		if (fromCache && res.messages.isEmpty() && lower_part != 0) {
			loadMedia(uid, offset, count, max_id, false, classGuid);
		} else {
			final HashMap<Integer, TLRPC.User> usersLocal = new HashMap<Integer, TLRPC.User>();
			for (TLRPC.User u : res.users) {
				usersLocal.put(u.id, u);
			}
			final ArrayList<MessageObject> objects = new ArrayList<MessageObject>();
			for (TLRPC.Message message : res.messages) {
				objects.add(new MessageObject(message, usersLocal));
			}

			Utilities.RunOnUIThread(new Runnable() {
				@Override
				public void run() {
					int totalCount;
					if (res instanceof TLRPC.TL_messages_messagesSlice) {
						totalCount = res.count;
					} else {
						totalCount = res.messages.size();
					}
					for (TLRPC.User user : res.users) {
						users.putIfAbsent(user.id, user);
						// add by xueqiang
						usersSDK.putIfAbsent(user.identification, user);

					}
					for (TLRPC.Chat chat : res.chats) {
						chats.putIfAbsent(chat.id, chat);
					}
					NotificationCenter.getInstance().postNotificationName(
							mediaDidLoaded, uid, totalCount, objects,
							fromCache, classGuid);
				}
			});
		}
	}

	public void loadMedia(final long uid, final int offset, final int count,
			final int max_id, final boolean fromCache, final int classGuid) {
		int lower_part = (int) uid;
		if (fromCache || lower_part == 0) {
			MessagesStorage.getInstance().loadMedia(uid, offset, count, max_id,
					classGuid);
		} else {
			TLRPC.TL_messages_search req = new TLRPC.TL_messages_search();
			req.offset = offset;
			req.limit = count;
			req.max_id = max_id;
			req.filter = new TLRPC.TL_inputMessagesFilterPhotoVideo();
			req.q = "";
			if (uid < 0) {
				req.peer = new TLRPC.TL_inputPeerChat();
				req.peer.chat_id = -lower_part;
			} else {
				TLRPC.User user = users.get(lower_part);
				if (user instanceof TLRPC.TL_userForeign
						|| user instanceof TLRPC.TL_userRequest) {
					req.peer = new TLRPC.TL_inputPeerForeign();
					// req.peer.access_hash = user.access_hash;
				} else {
					req.peer = new TLRPC.TL_inputPeerContact();
				}
				req.peer.user_id = lower_part;
			}
			long reqId = ConnectionsManager.getInstance().performRpc(req,
					new RPCRequest.RPCRequestDelegate() {
				@Override
				public void run(TLObject response, TLRPC.TL_error error) {
					if (error == null) {
						final TLRPC.messages_Messages res = (TLRPC.messages_Messages) response;
						processLoadedMedia(res, uid, offset, count,
								max_id, false, classGuid);
					}
				}
			}, null, true, RPCRequest.RPCRequestClassGeneric);
			// xueqiang delete
			// ConnectionsManager.getInstance().bindRequestToGuid(reqId,
			// classGuid);
		}
	}

	public void processLoadedMediaCount(final int count, final long uid,
			final int classGuid, final boolean fromCache) {
		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				int lower_part = (int) uid;
				if (fromCache && count == -1 && lower_part != 0) {
					getMediaCount(uid, classGuid, false);
				} else {
					// sam
					// if (!fromCache) {
					// MessagesStorage.getInstance().putMediaCount(uid, count);
					// }
					if (fromCache && count == -1) {
						NotificationCenter.getInstance().postNotificationName(
								mediaCountDidLoaded, uid, 0, fromCache);
					} else {
						NotificationCenter.getInstance().postNotificationName(
								mediaCountDidLoaded, uid, count, fromCache);
					}
				}
			}
		});
	}

	public void getMediaCount(final long uid, final int classGuid,
			boolean fromCache) {
		int lower_part = (int) uid;
		if (fromCache || lower_part == 0) {
			MessagesStorage.getInstance().getMediaCount(uid, classGuid);
		} else {
			TLRPC.TL_messages_search req = new TLRPC.TL_messages_search();
			req.offset = 0;
			req.limit = 1;
			req.max_id = 0;
			req.filter = new TLRPC.TL_inputMessagesFilterPhotoVideo();
			req.q = "";
			if (uid < 0) {
				req.peer = new TLRPC.TL_inputPeerChat();
				req.peer.chat_id = -lower_part;
			} else {
				TLRPC.User user = users.get(lower_part);
				if (user instanceof TLRPC.TL_userForeign
						|| user instanceof TLRPC.TL_userRequest) {
					req.peer = new TLRPC.TL_inputPeerForeign();
					// req.peer.access_hash = user.access_hash;
				} else {
					req.peer = new TLRPC.TL_inputPeerContact();
				}
				req.peer.user_id = lower_part;
			}
			long reqId = ConnectionsManager.getInstance().performRpc(req,
					new RPCRequest.RPCRequestDelegate() {
				@Override
				public void run(TLObject response, TLRPC.TL_error error) {
					if (error == null) {
						final TLRPC.messages_Messages res = (TLRPC.messages_Messages) response;
						if (res instanceof TLRPC.TL_messages_messagesSlice) {
							processLoadedMediaCount(res.count, uid,
									classGuid, false);
						} else {
							processLoadedMediaCount(
									res.messages.size(), uid,
									classGuid, false);
						}
					}
				}
			}, null, true, RPCRequest.RPCRequestClassGeneric);
			// xueqiang delete
			// ConnectionsManager.getInstance().bindRequestToGuid(reqId,
			// classGuid);
		}
	}

	public void uploadAndApplyUserAvatar(TLRPC.PhotoSize bigPhoto) {
		if (bigPhoto != null) {
			uploadingAvatar = Utilities.getCacheDir() + "/"
					+ bigPhoto.location.volume_id + "_"
					+ bigPhoto.location.local_id + ".jpg";
			FileLoader.getInstance().uploadFile(uploadingAvatar, null, null);
		}
	}

	public void deleteMessages(ArrayList<Integer> messages,
			ArrayList<Long> randoms, TLRPC.EncryptedChat encryptedChat,
			boolean bDeleteFile) {
		for (Integer id : messages) {

			MessageObject obj = dialogMessage.get(id);
			if (obj != null) {
				obj.deleted = true;
			}
		}
		MessagesStorage.getInstance().markMessagesAsDeleted(messages, true);
		MessagesStorage.getInstance().updateDialogsWithDeletedMessages(
				messages, true);
		NotificationCenter.getInstance().postNotificationName(messagesDeleted,
				messages, bDeleteFile);

		if (randoms != null && encryptedChat != null && !randoms.isEmpty()) {
			sendMessagesDeleteMessage(randoms, encryptedChat);
		}
		// ArrayList<Integer> toSend = new ArrayList<Integer>();
		// for (Integer mid : messages) {
		// if (mid > 0) {
		// toSend.add(mid);
		// }
		// }
		// if (toSend.isEmpty()) {
		// return;
		// }
		// TLRPC.TL_messages_deleteMessages req = new
		// TLRPC.TL_messages_deleteMessages();
		// req.id = messages;
		// ConnectionsManager.getInstance().performRpc(req, new
		// RPCRequest.RPCRequestDelegate() {
		// @Override
		// public void run(TLObject response, TLRPC.TL_error error) {
		//
		// }
		// }, null, true, RPCRequest.RPCRequestClassGeneric);
	}

	public void deleteDialog(final long did, int offset,
			final boolean onlyHistory) {
		TLRPC.TL_dialog dialog = dialogs_dict.get(did);
		if (dialog != null) {
			int lower_part = (int) did;

			if (offset == 0) {
				if (!onlyHistory) {
					dialogs.remove(dialog);
					dialogsServerOnly.remove(dialog);
					dialogs_dict.remove(did);
					totalDialogsCount--;
				}
				dialogMessage.remove(dialog.top_message);
				// 锟斤拷要锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷曰锟斤拷锟斤拷锟斤拷锟斤拷募锟斤拷锟较低筹拷锟缴撅拷锟斤拷锟斤拷锟斤拷锟斤拷知锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷曰锟斤拷锟斤拷锟斤拷锟斤拷兀锟斤拷锟揭拷锟窖拷锟斤拷菘锟�,然锟斤拷删锟斤拷
				// xueqiang change
				MessagesStorage.getInstance().deleteDialog(did, onlyHistory);
				NotificationCenter.getInstance().postNotificationName(
						removeAllMessagesFromDialog, did);
				NotificationCenter.getInstance().postNotificationName(
						dialogsNeedReload);
			}

			if (lower_part != 0) {
				TLRPC.TL_messages_deleteHistory req = new TLRPC.TL_messages_deleteHistory();
				req.offset = offset;
				if (did < 0) {
					req.peer = new TLRPC.TL_inputPeerChat();
					req.peer.chat_id = -lower_part;
					// 通知锟斤拷锟斤拷锟斤拷锟斤拷锟剿筹拷锟斤拷锟斤拷
					TLRPC.Chat chat = chats.get(-lower_part);
					// 锟斤拷锟斤拷锟诫开状态锟劫凤拷锟斤拷锟斤拷锟斤拷锟较拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
					if (chat != null) {
						if (!chat.left && !onlyHistory)
							deleteUserFromChat(-lower_part,
									UserConfig.currentUser, null);
						if (!onlyHistory)
							chats.remove(-lower_part);

					}

				} else {
					TLRPC.User user = users.get(lower_part);
					if (user instanceof TLRPC.TL_userForeign
							|| user instanceof TLRPC.TL_userRequest) {
						req.peer = new TLRPC.TL_inputPeerForeign();
						// req.peer.access_hash = user.access_hash;
					} else {
						req.peer = new TLRPC.TL_inputPeerContact();
					}
					req.peer.user_id = lower_part;
				}

			} else {
				int encId = (int) (did >> 32);
				if (onlyHistory) {
					TLRPC.EncryptedChat encryptedChat = encryptedChats
							.get(encId);
					sendClearHistoryMessage(encryptedChat);
				} else {
					declineSecretChat(encId);
				}
			}
		}
	}

	public void loadChatInfo(final int chat_id) {
		MessagesStorage.getInstance().loadChatInfo(chat_id);
	}

	// 锟斤拷锟斤拷锟侥碉拷时锟斤拷锟斤拷茫锟絏UEQIANG TODO..
	public void processChatInfo(final int chat_id,
			final TLRPC.ChatParticipants info,
			final ArrayList<TLRPC.User> usersArr, final boolean fromCache) {

		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				NotificationCenter.getInstance().postNotificationName(
						chatInfoDidLoaded, chat_id, info);
			}
		});
	}

	public void updatePrintingStrings() {
		final HashMap<Long, CharSequence> newPrintingStrings = new HashMap<Long, CharSequence>();

		ArrayList<Long> keys = new ArrayList<Long>(printingUsers.keySet());
		for (Long key : keys) {
			if (key > 0) {
				newPrintingStrings.put(key,
						LocaleController.getString("Typing", R.string.Typing));
			} else {
				ArrayList<PrintingUser> arr = printingUsers.get(key);
				int count = 0;
				String label = "";
				for (PrintingUser pu : arr) {
					TLRPC.User user = users.get(pu.userId);
					if (user != null) {
						if (label.length() != 0) {
							label += ", ";
						}
						String nameString = Utilities.formatName(user);

						label += nameString;
						count++;
					}
					if (count == 2) {
						break;
					}
				}
				if (label.length() != 0) {
					if (count > 1) {
						if (arr.size() > 2) {
							newPrintingStrings.put(key, Html.fromHtml(String
									.format("%s %s %s", label, String.format(
											LocaleController.getString(
													"AndMoreTyping",
													R.string.AndMoreTyping),
													arr.size() - 2), LocaleController
													.getString("AreTyping",
															R.string.AreTyping))));
						} else {
							newPrintingStrings.put(key, Html.fromHtml(String
									.format("%s %s", label, LocaleController
											.getString("AreTyping",
													R.string.AreTyping))));
						}
					} else {
						newPrintingStrings.put(key, Html.fromHtml(String
								.format("%s %s", label, LocaleController
										.getString("IsTyping",
												R.string.IsTyping))));
					}
				}
			}
		}

		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				printingStrings = newPrintingStrings;
			}
		});
	}

	public void sendTyping(long dialog_id, int classGuid) {
		if (dialog_id == 0) {
			return;
		}
		int lower_part = (int) dialog_id;
		if (lower_part != 0) {
			TLRPC.TL_messages_setTyping req = new TLRPC.TL_messages_setTyping();
			if (lower_part < 0) {
				req.peer = new TLRPC.TL_inputPeerChat();
				req.peer.chat_id = -lower_part;
			} else {
				TLRPC.User user = users.get(lower_part);
				if (user != null) {
					if (user instanceof TLRPC.TL_userForeign
							|| user instanceof TLRPC.TL_userRequest) {
						req.peer = new TLRPC.TL_inputPeerForeign();
						req.peer.user_id = user.id;
						// req.peer.access_hash = user.access_hash;
					} else {
						req.peer = new TLRPC.TL_inputPeerContact();
						req.peer.user_id = user.id;
					}
				} else {
					return;
				}
			}
			req.typing = true;
			long reqId = ConnectionsManager.getInstance().performRpc(req,
					new RPCRequest.RPCRequestDelegate() {
				@Override
				public void run(TLObject response, TLRPC.TL_error error) {

				}
			}, null, true, RPCRequest.RPCRequestClassGeneric);
			// xueqiang delete
			// ConnectionsManager.getInstance().bindRequestToGuid(reqId,
			// classGuid);
		} else {
			int encId = (int) (dialog_id >> 32);
			TLRPC.EncryptedChat chat = encryptedChats.get(encId);
			if (chat.auth_key != null && chat.auth_key.length > 1
					&& chat instanceof TLRPC.TL_encryptedChat) {
				TLRPC.TL_messages_setEncryptedTyping req = new TLRPC.TL_messages_setEncryptedTyping();
				req.peer = new TLRPC.TL_inputEncryptedChat();
				req.peer.chat_id = chat.id;
				req.peer.access_hash = chat.access_hash;
				req.typing = true;
				long reqId = ConnectionsManager.getInstance().performRpc(req,
						new RPCRequest.RPCRequestDelegate() {
					@Override
					public void run(TLObject response,
							TLRPC.TL_error error) {

					}
				}, null, true, RPCRequest.RPCRequestClassGeneric);
				// xueqiang delete
				// ConnectionsManager.getInstance().bindRequestToGuid(reqId,
				// classGuid);
			}
		}
	}

	public void loadMessages(final long dialog_id, final int offset,
			final int count, final int max_id, boolean fromCache, int midDate,
			final int classGuid, boolean from_unread, boolean forward) {
		int lower_part = (int) dialog_id;
		if (fromCache || lower_part == 0) {
			MessagesStorage.getInstance().getMessages(dialog_id, offset, count,
					max_id, midDate, classGuid, from_unread, forward);
		} else {
			// sam
			// TLRPC.TL_messages_getHistory req = new
			// TLRPC.TL_messages_getHistory();
			// if (lower_part < 0) {
			// req.peer = new TLRPC.TL_inputPeerChat();
			// req.peer.chat_id = -lower_part;
			// } else {
			// TLRPC.User user = users.get(lower_part);
			// if (user instanceof TLRPC.TL_userForeign || user instanceof
			// TLRPC.TL_userRequest) {
			// req.peer = new TLRPC.TL_inputPeerForeign();
			// req.peer.user_id = user.id;
			// req.peer.access_hash = user.access_hash;
			// } else {
			// req.peer = new TLRPC.TL_inputPeerContact();
			// req.peer.user_id = user.id;
			// }
			// }
			// req.offset = offset;
			// req.limit = count;
			// req.max_id = max_id;
			// long reqId = ConnectionsManager.getInstance().performRpc(req, new
			// RPCRequest.RPCRequestDelegate() {
			// @Override
			// public void run(TLObject response, TLRPC.TL_error error) {
			// if (error == null) {
			// final TLRPC.messages_Messages res = (TLRPC.messages_Messages)
			// response;
			// processLoadedMessages(res, dialog_id, offset, count, max_id,
			// false, classGuid, 0, 0, 0, 0, false);
			// }
			// }
			// }, null, true, RPCRequest.RPCRequestClassGeneric);
			// ConnectionsManager.getInstance().bindRequestToGuid(reqId,
			// classGuid);
			final TLRPC.messages_Messages res = new TLRPC.messages_Messages();
			processLoadedMessages(res, dialog_id, offset, count, max_id, false,
					classGuid, 0, 0, 0, 0, false);
		}
	}

	// sam changes a lot for null res members
	public void processLoadedMessages(
			final TLRPC.messages_Messages messagesRes, final long dialog_id,
			final int offset, final int count, final int max_id,
			final boolean isCache, final int classGuid, final int first_unread,
			final int last_unread, final int unread_count, final int last_date,
			final boolean isForward) {
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				int lower_id = (int) dialog_id;
				if (!isCache) {
					MessagesStorage.getInstance().putMessages(messagesRes,
							dialog_id);
				}
				if (lower_id != 0
						&& isCache
						&& (messagesRes.messages == null || messagesRes.messages
						.isEmpty()) && !isForward) {
					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() {
							loadMessages(dialog_id, offset, count, max_id,
									false, 0, classGuid, false, false);
						}
					});
					return;
				}
				final HashMap<Integer, TLRPC.User> usersLocal = new HashMap<Integer, TLRPC.User>();
				if (messagesRes.users != null) {
					for (TLRPC.User u : messagesRes.users) {
						usersLocal.put(u.id, u);
					}
				}
				// 锟斤拷锟斤拷锟捷匡拷锟斤拷锟斤拷锟斤拷锟较拷锟斤拷锟斤拷氐锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
				final ArrayList<MessageObject> objects = new ArrayList<MessageObject>();
				if (messagesRes.messages != null) {
					for (TLRPC.Message message : messagesRes.messages) {
						message.dialog_id = dialog_id;
						objects.add(new MessageObject(message, usersLocal));
					}
				}
				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						if (messagesRes.users != null) {
							for (TLRPC.User u : messagesRes.users) {
								if (isCache) {
									if (u.id == UserConfig.clientUserId
											|| u.id / 1000 == 333) {
										users.put(u.id, u);
										// add by xueqiang
										usersSDK.put(u.identification, u);
									} else {
										users.putIfAbsent(u.id, u);
										// add by xueqiang
										usersSDK.putIfAbsent(u.identification,
												u);
									}
								} else {
									users.put(u.id, u);
									// add by xueqiang
									usersSDK.put(u.identification, u);
									if (u.id == UserConfig.clientUserId) {
										u.sessionid = UserConfig.currentUser.sessionid;
										UserConfig.currentUser = u;
									}
								}
							}
						}
						if (messagesRes.chats != null) {
							for (TLRPC.Chat c : messagesRes.chats) {
								if (isCache) {
									chats.putIfAbsent(c.id, c);
								} else {
									chats.put(c.id, c);
								}
							}
						}
						NotificationCenter.getInstance().postNotificationName(
								messagesDidLoaded, dialog_id, offset, count,
								objects, isCache, first_unread, last_unread,
								unread_count, last_date, isForward);
					}
				});
			}
		});
	}

	public void loadDialogs(final int offset, final int serverOffset,
			final int count, boolean fromCache) {
		// 锟斤拷锟斤拷锟斤拷锟斤拷曰锟斤拷斜锟�
		if (loadingDialogs) {
			return;
		}
		loadingDialogs = true;

		if (fromCache) {
			MessagesStorage.getInstance().getDialogs(offset, serverOffset,
					count);
		} else {
			final TLRPC.messages_Dialogs dialogsRes = new TLRPC.messages_Dialogs();
			processLoadedDialogs(dialogsRes, null, offset, serverOffset, count,
					false, false);
		}
	}

	public void processDialogsUpdateRead(
			final HashMap<Long, Integer> dialogsToUpdate) {
		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				for (HashMap.Entry<Long, Integer> entry : dialogsToUpdate
						.entrySet()) {
					TLRPC.TL_dialog currentDialog = dialogs_dict.get(entry
							.getKey());
					if (currentDialog != null) {
						currentDialog.unread_count = entry.getValue();
					}
				}

				NotificationCenter.getInstance().postNotificationName(
						dialogsNeedReload);
			}
		});
	}

	public void processDialogsUpdate(final TLRPC.messages_Dialogs dialogsRes,
			ArrayList<TLRPC.EncryptedChat> encChats) {
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				final HashMap<Long, TLRPC.TL_dialog> new_dialogs_dict = new HashMap<Long, TLRPC.TL_dialog>();
				final HashMap<Integer, MessageObject> new_dialogMessage = new HashMap<Integer, MessageObject>();
				final HashMap<Integer, TLRPC.User> usersLocal = new HashMap<Integer, TLRPC.User>();

				for (TLRPC.User u : dialogsRes.users) {
					usersLocal.put(u.id, u);
				}

				for (TLRPC.Message m : dialogsRes.messages) {
					new_dialogMessage.put(m.id,
							new MessageObject(m, usersLocal));
				}
				for (TLRPC.TL_dialog d : dialogsRes.dialogs) {
					if (d.last_message_date == 0) {
						MessageObject mess = new_dialogMessage
								.get(d.top_message);
						if (mess != null) {
							d.last_message_date = mess.messageOwner.date;
						}
					}
					if (d.id == 0) {
						if (d.peer instanceof TLRPC.TL_peerUser) {
							d.id = d.peer.user_id;
						} else if (d.peer instanceof TLRPC.TL_peerChat) {
							d.id = -d.peer.chat_id;
						}
					}
					new_dialogs_dict.put(d.id, d);
				}

				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						for (TLRPC.User u : dialogsRes.users) {
							users.putIfAbsent(u.id, u);
							// add by xueqiang
							usersSDK.putIfAbsent(u.identification, u);
						}
						for (TLRPC.Chat c : dialogsRes.chats) {
							chats.putIfAbsent(c.id, c);
						}

						for (HashMap.Entry<Long, TLRPC.TL_dialog> pair : new_dialogs_dict
								.entrySet()) {
							long key = pair.getKey();
							TLRPC.TL_dialog value = pair.getValue();
							TLRPC.TL_dialog currentDialog = dialogs_dict
									.get(key);
							if (currentDialog == null) {
								dialogs_dict.put(key, value);
								dialogMessage.put(value.top_message,
										new_dialogMessage
										.get(value.top_message));
							} else {
								currentDialog.unread_count = value.unread_count;
								MessageObject oldMsg = dialogMessage
										.get(currentDialog.top_message);
								if (oldMsg == null
										|| currentDialog.top_message > 0) {
									if (oldMsg != null
											&& oldMsg.deleted
											|| value.top_message > currentDialog.top_message) {
										dialogs_dict.put(key, value);
										if (oldMsg != null) {
											dialogMessage
											.remove(oldMsg.messageOwner.id);
										}
										dialogMessage
										.put(value.top_message,
												new_dialogMessage
												.get(value.top_message));
									}
								} else {
									MessageObject newMsg = new_dialogMessage
											.get(value.top_message);
									if (oldMsg.deleted
											|| newMsg == null
											|| newMsg.messageOwner.date > oldMsg.messageOwner.date) {
										dialogs_dict.put(key, value);
										dialogMessage
										.remove(oldMsg.messageOwner.id);
										dialogMessage
										.put(value.top_message,
												new_dialogMessage
												.get(value.top_message));
									}
								}
							}
						}

						dialogs.clear();
						dialogsServerOnly.clear();
						dialogs.addAll(dialogs_dict.values());

						sortDialogs();

						for (TLRPC.TL_dialog d : dialogs) {
							if ((int) d.id != 0) {
								dialogsServerOnly.add(d);
							}
						}

						NotificationCenter.getInstance().postNotificationName(
								dialogsNeedReload);
					}
				});
			}
		});
	}

	public void processLoadedDialogs(final TLRPC.messages_Dialogs dialogsRes,
			final ArrayList<TLRPC.EncryptedChat> encChats, final int offset,
			final int serverOffset, final int count, final boolean isCache,
			final boolean resetEnd) {
		// FileLog.d("emm", "processLoadedDialogs 1");
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				// FileLog.d("emm", "processLoadedDialogs 2");
				if (isCache
						&& (dialogsRes.dialogs == null || dialogsRes.dialogs
						.size() == 0)) { // sam 锟斤拷锟斤拷dialogs为锟秸碉拷锟叫讹拷
					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() {
							for (TLRPC.User u : dialogsRes.users) {
								if (isCache) {
									if (u.id == UserConfig.clientUserId
											|| u.id / 1000 == 333) {
										users.put(u.id, u);
										// add by xueqiang
										usersSDK.put(u.identification, u);
									} else {
										users.putIfAbsent(u.id, u);
										// add by xueqiang
										usersSDK.putIfAbsent(u.identification,
												u);
									}
								} else {
									users.put(u.id, u);
									// add by xueqiang
									usersSDK.put(u.identification, u);
									if (u.id == UserConfig.clientUserId) {
										u.sessionid = UserConfig.currentUser.sessionid;
										UserConfig.currentUser = u;
									}
								}
							}

							loadingDialogs = false;
							if (resetEnd) {
								dialogsEndReached = false;
								NotificationCenter
								.getInstance()
								.postNotificationName(dialogsNeedReload);
							}
							loadDialogs(offset, serverOffset, count, false);

						}
					});
					// FileLog.d("emm", "processLoadedDialogs 3");
					return;
				}
				final HashMap<Long, TLRPC.TL_dialog> new_dialogs_dict = new HashMap<Long, TLRPC.TL_dialog>();
				final HashMap<Integer, MessageObject> new_dialogMessage = new HashMap<Integer, MessageObject>();
				final HashMap<Integer, TLRPC.User> usersLocal = new HashMap<Integer, TLRPC.User>();
				int new_totalDialogsCount;

				if (!isCache) {
					MessagesStorage.getInstance().putDialogs(dialogsRes);
				}

				if (dialogsRes instanceof TLRPC.TL_messages_dialogsSlice) {
					TLRPC.TL_messages_dialogsSlice slice = (TLRPC.TL_messages_dialogsSlice) dialogsRes;
					new_totalDialogsCount = slice.count;
				} else {
					new_totalDialogsCount = dialogsRes.dialogs.size();
				}

				for (TLRPC.User u : dialogsRes.users) {
					usersLocal.put(u.id, u);
				}

				for (TLRPC.Message m : dialogsRes.messages) {
					new_dialogMessage.put(m.id,
							new MessageObject(m, usersLocal));
				}
				for (TLRPC.TL_dialog d : dialogsRes.dialogs) {
					if (d.last_message_date == 0) {
						MessageObject mess = new_dialogMessage
								.get(d.top_message);
						if (mess != null) {
							d.last_message_date = mess.messageOwner.date;
						}
					}
					if (d.id == 0) {
						if (d.peer instanceof TLRPC.TL_peerUser) {
							d.id = d.peer.user_id;
						} else if (d.peer instanceof TLRPC.TL_peerChat) {
							d.id = -d.peer.chat_id;
						}
					}
					new_dialogs_dict.put(d.id, d);
				}

				// FileLog.d("emm", "processLoadedDialogs 4");
				final int arg1 = new_totalDialogsCount;
				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						// FileLog.d("emm", "processLoadedDialogs 5");
						for (TLRPC.User u : dialogsRes.users) {
							if (isCache) {
								if (u.id == UserConfig.clientUserId
										|| u.id / 1000 == 333) {
									users.put(u.id, u);
									// add by xueqiang
									usersSDK.put(u.identification, u);
								} else {
									users.putIfAbsent(u.id, u);
									// add by xueqiang
									usersSDK.putIfAbsent(u.identification, u);
								}
							} else {
								users.put(u.id, u);
								// add by xueqiang
								usersSDK.put(u.identification, u);
								if (u.id == UserConfig.clientUserId) {
									u.sessionid = UserConfig.currentUser.sessionid;
									UserConfig.currentUser = u;
								}
							}
						}
						for (TLRPC.Chat c : dialogsRes.chats) {
							if (isCache) {
								chats.putIfAbsent(c.id, c);
							} else {
								chats.put(c.id, c);
							}
						}
						if (encChats != null) {
							for (TLRPC.EncryptedChat encryptedChat : encChats) {
								encryptedChats.put(encryptedChat.id,
										encryptedChat);
							}
						}

						totalDialogsCount = arg1;

						for (HashMap.Entry<Long, TLRPC.TL_dialog> pair : new_dialogs_dict
								.entrySet()) {
							long key = pair.getKey();
							TLRPC.TL_dialog value = pair.getValue();
							TLRPC.TL_dialog currentDialog = dialogs_dict
									.get(key);
							if (currentDialog == null) {
								dialogs_dict.put(key, value);
								dialogMessage.put(value.top_message,
										new_dialogMessage
										.get(value.top_message));
							} else {
								MessageObject oldMsg = dialogMessage
										.get(value.top_message);
								if (oldMsg == null
										|| currentDialog.top_message > 0) {
									if (oldMsg != null
											&& oldMsg.deleted
											|| value.top_message > currentDialog.top_message) {
										if (oldMsg != null) {
											dialogMessage
											.remove(oldMsg.messageOwner.id);
										}
										dialogs_dict.put(key, value);
										dialogMessage
										.put(value.top_message,
												new_dialogMessage
												.get(value.top_message));
									}
								} else {
									MessageObject newMsg = new_dialogMessage
											.get(value.top_message);
									if (oldMsg.deleted
											|| newMsg == null
											|| newMsg.messageOwner.date > oldMsg.messageOwner.date) {
										dialogMessage
										.remove(oldMsg.messageOwner.id);
										dialogs_dict.put(key, value);
										dialogMessage
										.put(value.top_message,
												new_dialogMessage
												.get(value.top_message));
									}
								}
							}
						}

						dialogs.clear();
						dialogsServerOnly.clear();
						dialogs.addAll(dialogs_dict.values());

						sortDialogs();

						for (TLRPC.TL_dialog d : dialogs) {
							if ((int) d.id != 0) {
								dialogsServerOnly.add(d);
							}
						}

						dialogsEndReached = (dialogsRes.dialogs.size() == 0 || dialogsRes.dialogs
								.size() != count) && !isCache;
						// xueqiang锟斤拷锟斤拷锟斤拷锟叫讹拷
						// if(dialogs.size()>0)
						{
							loadingDialogs = false;
							NotificationCenter.getInstance()
							.postNotificationName(dialogsNeedReload);
						}
						// FileLog.d("emm", "processLoadedDialogs 6");
					}
				});
				// FileLog.d("emm", "processLoadedDialogs 7");
			}
		});
	}

	public TLRPC.TL_photo generatePhotoSizes(String path, Uri imageUri) {
		long time = System.currentTimeMillis();
		Bitmap bitmap = FileLoader.loadBitmap(path, imageUri, 800, 800);
		ArrayList<TLRPC.PhotoSize> sizes = new ArrayList<TLRPC.PhotoSize>();
		TLRPC.PhotoSize size = FileLoader.scaleAndSaveImage(bitmap, 90, 90, 55,
				true);
		if (size != null) {
			size.type = "s";
			sizes.add(size);
		}
		size = FileLoader.scaleAndSaveImage(bitmap, 320, 320, 87, false);
		if (size != null) {
			size.type = "m";
			sizes.add(size);
		}
		size = FileLoader.scaleAndSaveImage(bitmap, 800, 800, 87, false);
		if (size != null) {
			size.type = "x";
			sizes.add(size);
		}
		if (Build.VERSION.SDK_INT < 11) {
			if (bitmap != null) {
				bitmap.recycle();
			}
		}
		if (sizes.isEmpty()) {

			return null;
		} else {
			UserConfig.saveConfig(false);
			TLRPC.TL_photo photo = new TLRPC.TL_photo();
			photo.user_id = UserConfig.clientUserId;
			photo.date = ConnectionsManager.getInstance().getCurrentTime();
			photo.sizes = sizes;
			photo.caption = "";
			photo.geo = new TLRPC.TL_geoPointEmpty();
			return photo;
		}
	}

	public void markDialogAsRead(final long dialog_id, final int max_id,
			final int max_positive_id, final int offset, final int max_date,
			final boolean was) {
		int lower_part = (int) dialog_id;
		if (lower_part != 0) {
			if (max_id == 0 && offset == 0) {
				return;
			}
			TLRPC.TL_messages_readHistory req = new TLRPC.TL_messages_readHistory();
			if (lower_part < 0) {
				req.peer = new TLRPC.TL_inputPeerChat();
				req.peer.chat_id = -lower_part;
			} else {
				TLRPC.User user = users.get(lower_part);
				if (user instanceof TLRPC.TL_userForeign
						|| user instanceof TLRPC.TL_userRequest) {
					req.peer = new TLRPC.TL_inputPeerForeign();
					req.peer.user_id = user.id;
					// req.peer.access_hash = user.access_hash;
				} else {
					req.peer = new TLRPC.TL_inputPeerContact();
					req.peer.user_id = user.id;
				}
			}
			req.max_id = max_positive_id;
			req.offset = offset;
			if (offset == 0) {
				// 锟斤拷锟斤拷锟斤拷浣拷锟较拷锟斤拷锟窖讹拷状态
				MessagesStorage.getInstance().processPendingRead(dialog_id,
						max_positive_id, max_date, false);
			}

			if (offset == 0) {
				TLRPC.TL_dialog dialog = dialogs_dict.get(dialog_id);
				if (dialog != null) {
					// 锟斤拷锟斤拷锟斤拷息锟斤拷锟斤拷锟绞憋拷颍锟斤拷锟斤拷DB锟斤拷putmessages,然锟斤拷氐锟斤拷锟斤拷锟斤拷锟斤拷dialogsUnreadCountIncr锟斤拷锟斤拷锟斤拷unread_count,锟斤拷锟斤拷锟届步锟斤拷锟斤拷锟斤拷
					// 锟杰匡拷锟斤拷锟斤拷锟斤拷拇锟斤拷锟街达拷锟斤拷辏珼B锟脚回碉拷dialogsUnreadCountIncr锟斤拷锟斤拷锟斤拷未锟斤拷锟斤拷息锟斤拷锟斤拷锟斤拷锟酵诧拷锟斤拷锟斤拷
					dialog.unread_count = 0;
					NotificationCenter.getInstance().postNotificationName(
							dialogsNeedReload);
				}
			}

		} else {
			if (max_date == 0) {
				return;
			}
			int encId = (int) (dialog_id >> 32);
			TLRPC.EncryptedChat chat = encryptedChats.get(encId);
			if (chat.auth_key != null && chat.auth_key.length > 1
					&& chat instanceof TLRPC.TL_encryptedChat) {
				TLRPC.TL_messages_readEncryptedHistory req = new TLRPC.TL_messages_readEncryptedHistory();
				req.peer = new TLRPC.TL_inputEncryptedChat();
				req.peer.chat_id = chat.id;
				req.peer.access_hash = chat.access_hash;
				req.max_date = max_date;

				ConnectionsManager.getInstance().performRpc(req,
						new RPCRequest.RPCRequestDelegate() {
					@Override
					public void run(TLObject response,
							TLRPC.TL_error error) {
						// MessagesStorage.getInstance().processPendingRead(dialog_id,
						// max_id, max_date, true);
					}
				}, null, true, RPCRequest.RPCRequestClassGeneric);
			}
			MessagesStorage.getInstance().processPendingRead(dialog_id, max_id,
					max_date, false);

			TLRPC.TL_dialog dialog = dialogs_dict.get(dialog_id);
			if (dialog != null) {
				dialog.unread_count = 0;
				FileLog.d("emm", "****dialog id=" + dialog.id
						+ " unread count= " + dialog.unread_count);
				NotificationCenter.getInstance().postNotificationName(
						dialogsNeedReload);
			}

			if (chat.ttl > 0 && was) {
				int serverTime = Math.max(ConnectionsManager.getInstance()
						.getCurrentTime(), max_date);
				MessagesStorage.getInstance().createTaskForDate(chat.id,
						serverTime, serverTime, 0);
			}
		}
	}

	public void cancelSendingMessage(MessageObject object) {
		String keyToRemvoe = null;
		boolean enc = false;
		for (HashMap.Entry<String, ArrayList<DelayedMessage>> entry : delayedMessages
				.entrySet()) {
			ArrayList<DelayedMessage> messages = entry.getValue();
			for (int a = 0; a < messages.size(); a++) {
				DelayedMessage message = messages.get(a);
				if (message.obj.messageOwner.id == object.messageOwner.id) {
					messages.remove(a);
					if (messages.size() == 0) {
						keyToRemvoe = entry.getKey();
						if (message.sendEncryptedRequest != null) {
							enc = true;
						}
					}
					break;
				}
			}
		}
		if (keyToRemvoe != null) {
			FileLoader.getInstance().cancelUploadFile(keyToRemvoe, enc);
		}
		ArrayList<Integer> messages = new ArrayList<Integer>();
		messages.add(object.messageOwner.id);
		// xueqiang change
		deleteMessages(messages, null, null, true);
	}

	private long getNextRandomId() {
		long val = 0;
		while (val == 0) {
			val = random.nextLong();
		}
		return val;
	}

	public void sendMessage(TLRPC.User user, long peer) {
		sendMessage(null, 0, 0, null, null, null, null, user, null, null, null,
				peer);
	}

	public void sendMessage(MessageObject message, long peer) {
		sendMessage(null, 0, 0, null, null, message, null, null, null, null,
				null, peer);
	}

	public void sendMessage(TLRPC.TL_document document, long peer) {
		sendMessage(null, 0, 0, null, null, null, null, null, document, null,
				null, peer);
	}

	public void sendMessage(String message, long peer) {
		sendMessage(message, 0, 0, null, null, null, null, null, null, null,
				null, peer);
	}

	public void sendMessage(TLRPC.FileLocation location, long peer) {
		sendMessage(null, 0, 0, null, null, null, location, null, null, null,
				null, peer);
	}

	public void sendMessage(double lat, double lon, long peer) {
		sendMessage(null, lat, lon, null, null, null, null, null, null, null,
				null, peer);
	}

	public void sendMessage(TLRPC.TL_photo photo, long peer) {
		sendMessage(null, 0, 0, photo, null, null, null, null, null, null,
				null, peer);
	}

	public void sendMessage(TLRPC.TL_video video, long peer) {
		sendMessage(null, 0, 0, null, video, null, null, null, null, null,
				null, peer);
	}

	public void sendMessage(TLRPC.TL_audio audio, long peer) {
		sendMessage(null, 0, 0, null, null, null, null, null, null, audio,
				null, peer);
	}

	// xueqiang add for alert msg
	public void sendMessage(TLRPC.TL_alertMedia alert, long peer) {
		sendMessage(null, 0, 0, null, null, null, null, null, null, null,
				alert, peer);
	}

	private void processPendingEncMessages() {
		if (pendingEncMessagesToDelete.isEmpty()) {
			return;
		}
		ArrayList<Long> arr = new ArrayList<Long>(pendingEncMessagesToDelete);
		MessagesStorage.getInstance().markMessagesAsDeletedByRandoms(arr);
		pendingEncMessagesToDelete.clear();
	}

	private void sendMessagesDeleteMessage(ArrayList<Long> random_ids,
			TLRPC.EncryptedChat encryptedChat) {
		TLRPC.TL_decryptedMessageService reqSend = new TLRPC.TL_decryptedMessageService();
		reqSend.random_id = getNextRandomId();
		reqSend.random_bytes = new byte[Math.max(1,
				(int) Math.ceil(random.nextDouble() * 16))];
		random.nextBytes(reqSend.random_bytes);
		reqSend.action = new TLRPC.TL_decryptedMessageActionDeleteMessages();
		reqSend.action.random_ids = random_ids;
		performSendEncryptedRequest(reqSend, null, encryptedChat, null);
	}

	private void sendClearHistoryMessage(TLRPC.EncryptedChat encryptedChat) {
		TLRPC.TL_decryptedMessageService reqSend = new TLRPC.TL_decryptedMessageService();
		reqSend.random_id = getNextRandomId();
		reqSend.random_bytes = new byte[Math.max(1,
				(int) Math.ceil(random.nextDouble() * 16))];
		random.nextBytes(reqSend.random_bytes);
		reqSend.action = new TLRPC.TL_decryptedMessageActionFlushHistory();
		performSendEncryptedRequest(reqSend, null, encryptedChat, null);
	}

	public void sendTTLMessage(TLRPC.EncryptedChat encryptedChat) {
		TLRPC.TL_messageService newMsg = new TLRPC.TL_messageService();

		newMsg.action = new TLRPC.TL_messageActionTTLChange();
		newMsg.action.ttl = encryptedChat.ttl;
		newMsg.local_id = newMsg.id = UserConfig.getNewMessageId();
		newMsg.from_id = UserConfig.clientUserId;
		newMsg.unread = true;
		newMsg.dialog_id = ((long) encryptedChat.id) << 32;
		newMsg.to_id = new TLRPC.TL_peerUser();
		if (encryptedChat.participant_id == UserConfig.clientUserId) {
			newMsg.to_id.user_id = encryptedChat.admin_id;
		} else {
			newMsg.to_id.user_id = encryptedChat.participant_id;
		}
		newMsg.out = true;
		newMsg.date = ConnectionsManager.getInstance().getCurrentTime();
		newMsg.random_id = getNextRandomId();
		UserConfig.saveConfig(false);
		final MessageObject newMsgObj = new MessageObject(newMsg, users);
		newMsgObj.messageOwner.send_state = MESSAGE_SEND_STATE_SENDING;

		final ArrayList<MessageObject> objArr = new ArrayList<MessageObject>();
		objArr.add(newMsgObj);
		ArrayList<TLRPC.Message> arr = new ArrayList<TLRPC.Message>();
		arr.add(newMsg);
		MessagesStorage.getInstance().putMessages(arr, false, true);
		updateInterfaceWithMessages(newMsg.dialog_id, objArr);

		NotificationCenter.getInstance()
		.postNotificationName(dialogsNeedReload);

		sendingMessages.put(newMsg.id, newMsgObj);

		TLRPC.TL_decryptedMessageService reqSend = new TLRPC.TL_decryptedMessageService();
		reqSend.random_id = newMsg.random_id;
		reqSend.random_bytes = new byte[Math.max(1,
				(int) Math.ceil(random.nextDouble() * 16))];
		random.nextBytes(reqSend.random_bytes);
		reqSend.action = new TLRPC.TL_decryptedMessageActionSetMessageTTL();
		reqSend.action.ttl_seconds = encryptedChat.ttl;
		performSendEncryptedRequest(reqSend, newMsgObj, encryptedChat, null);
	}

	private void sendMessage(String message, double lat, double lon,
			TLRPC.TL_photo photo, TLRPC.TL_video video, MessageObject msgObj,
			TLRPC.FileLocation location, TLRPC.User user,
			TLRPC.TL_document document, TLRPC.TL_audio audio,
			TLRPC.TL_alertMedia alert, long peer) {
		TLRPC.Message newMsg = null;
		int type = -1;
		if (msgObj != null) {
			if (msgObj.messageOwner.media instanceof TLRPC.TL_messageMediaAudio) {
				// 锟斤拷锟斤拷一锟斤拷锟斤拷频锟斤拷锟斤拷锟皆拷锟斤拷锟阶拷锟斤拷锟缴撅拷锟斤拷耍锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
				TLRPC.TL_audio srcAudio = (TLRPC.TL_audio) msgObj.messageOwner.media.audio;
				srcAudio.path = msgObj.messageOwner.attachPath = Utilities
						.getSystemDir().getAbsolutePath()
						+ "/"
						+ MessageObject.getAttachFileName(srcAudio);
				try {
					File srcFile = new File(srcAudio.path);

					TLRPC.Audio destAudio = new TLRPC.TL_audio();
					destAudio.id = UserConfig.getSeq();
					destAudio.dc_id = UserConfig.clientUserId;

					String newAudioFile = Utilities.getSystemDir()
							.getAbsolutePath()
							+ "/"
							+ MessageObject.getAttachFileName(destAudio);

					File destFile = new File(newAudioFile);
					Utilities.copyFile(srcFile, destFile);

					audio = new TLRPC.TL_audio();
					audio.date = srcAudio.date;
					audio.duration = srcAudio.duration;
					audio.access_hash = srcAudio.access_hash;
					audio.user_id = srcAudio.user_id;
					audio.size = srcAudio.size;
					audio.id = destAudio.id;
					audio.dc_id = destAudio.dc_id;
					audio.path = destAudio.path = newAudioFile;

				} catch (IOException e) {

				}
			} else if (msgObj.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto) {
				photo = (TLRPC.TL_photo) msgObj.messageOwner.media.photo;
			} else if (msgObj.messageOwner.media instanceof TLRPC.TL_messageMediaGeo) {
				lat = msgObj.messageOwner.media.geo.lat;
				lon = msgObj.messageOwner.media.geo._long;
			} else if (msgObj.messageOwner.media instanceof TLRPC.TL_messageMediaDocument) {
				document = (TLRPC.TL_document) msgObj.messageOwner.media.document;
				if (!msgObj.messageOwner.attachPath.isEmpty()) {
					document.path = msgObj.messageOwner.attachPath;
					File destFile = new File(document.path);
					document.size = (int) destFile.length();
				}
			} else {
				message = msgObj.messageText.toString();
			}
		}
		if (message != null) {
			// 锟斤拷锟斤拷锟侥憋拷
			newMsg = new TLRPC.TL_message();
			newMsg.media = new TLRPC.TL_messageMediaEmpty();
			type = 0;   
			newMsg.message = message; 
		} else if (lat != 0 && lon != 0) {//锟斤拷纬锟斤拷
			newMsg = new TLRPC.TL_message();
			newMsg.media = new TLRPC.TL_messageMediaGeo();
			newMsg.media.geo = new TLRPC.TL_geoPoint();
			newMsg.media.geo.lat = lat;
			newMsg.media.geo._long = lon;
			newMsg.message = "";
			type = 1;
		} else if (photo != null) {
			newMsg = new TLRPC.TL_message();
			newMsg.media = new TLRPC.TL_messageMediaPhoto();
			newMsg.media.photo = photo;
			type = 2;
			newMsg.message = "";
			TLRPC.FileLocation location1 = photo.sizes
					.get(photo.sizes.size() - 1).location;
			// 锟斤拷锟斤拷图片锟斤拷锟侥硷拷锟斤拷
			newMsg.attachPath = Utilities.getCacheDir() + "/"
					+ location1.volume_id + "_" + location1.local_id + ".jpg";
		} else if (video != null) {
			newMsg = new TLRPC.TL_message();
			newMsg.media = new TLRPC.TL_messageMediaVideo();
			newMsg.media.video = video;
			type = 3;
			newMsg.message = "";
			newMsg.attachPath = video.path;
		} else if (location != null) {

		} else if (user != null) {
			newMsg = new TLRPC.TL_message();
			newMsg.media = new TLRPC.TL_messageMediaContact();
			newMsg.media.phone_number = user.phone;
			newMsg.media.first_name = user.first_name;
			newMsg.media.last_name = user.last_name;
			newMsg.media.user_id = user.id;
			newMsg.message = "";
			type = 6;
		} else if (document != null) {
			newMsg = new TLRPC.TL_message();
			newMsg.media = new TLRPC.TL_messageMediaDocument();
			newMsg.media.document = document;
			type = 7;
			newMsg.message = "-1";
			if (document.path == null) {
				String docPath = Utilities.getCacheDir().getAbsolutePath()
						+ "/" + MessageObject.getAttachFileName(document);
				document.path = docPath;
				File destFile = new File(docPath);
				document.size = (int) destFile.length();
			}
			newMsg.attachPath = document.path;
		} else if (audio != null) {
			newMsg = new TLRPC.TL_message();
			newMsg.media = new TLRPC.TL_messageMediaAudio();
			newMsg.media.audio = audio;
			type = 8;
			newMsg.message = "";
			newMsg.attachPath = audio.path;
		} else if (alert != null) {
			// xueqiang add for alert msg
			newMsg = new TLRPC.TL_message();
			newMsg.media = new TLRPC.TL_messageMediaAlert();
			newMsg.media.alert = alert;
			type = 9;
			newMsg.message = alert.msg;
		}
		if (newMsg == null) {
			return;
		}
		// 锟斤拷锟斤拷一锟斤拷锟斤拷锟斤拷id,通锟斤拷锟角革拷锟斤拷锟斤拷锟斤拷锟绞憋拷锟揭诧拷锟芥储锟斤拷chat UI锟斤拷一锟捷ｏ拷
		// 锟斤拷锟斤拷锟酵成癸拷锟襟，斤拷锟结将锟斤拷锟斤拷锟较⑼ㄖ狢HATACTIVITY锟斤拷CHATACTIVITY锟斤拷锟斤拷锟斤拷锟较拷锟街疚拷锟斤拷统晒锟�
		newMsg.local_id = newMsg.id = UserConfig.getNewMessageId();
		newMsg.from_id = UserConfig.clientUserId;
		newMsg.unread = true;
		// 锟皆伙拷id,锟斤拷锟斤拷锟�1锟斤拷1锟斤拷锟斤拷锟角对凤拷ID锟斤拷锟斤拷锟斤拷嵌锟斤拷耍锟斤拷锟斤拷锟�-chat_id
		newMsg.dialog_id = peer;

		int lower_id = (int) peer;
		TLRPC.EncryptedChat encryptedChat = null;
		TLRPC.InputPeer sendToPeer = null;
		if (lower_id != 0) {
			if (lower_id < 0) {
				// 锟斤拷示为锟斤拷锟斤拷锟斤拷
				newMsg.to_id = new TLRPC.TL_peerChat();
				newMsg.to_id.chat_id = -lower_id;
				sendToPeer = new TLRPC.TL_inputPeerChat();
				sendToPeer.chat_id = -lower_id;
			} else {
				// 锟斤拷示一锟斤拷一锟斤拷锟斤拷
				newMsg.to_id = new TLRPC.TL_peerUser();
				newMsg.to_id.user_id = lower_id;
				TLRPC.User sendToUser = users.get(lower_id);
				if (sendToUser != null) {
					if (sendToUser instanceof TLRPC.TL_userForeign
							|| sendToUser instanceof TLRPC.TL_userRequest) {
						sendToPeer = new TLRPC.TL_inputPeerForeign();
						sendToPeer.user_id = sendToUser.id;
						// sendToPeer.access_hash = sendToUser.access_hash;
					} else {
						sendToPeer = new TLRPC.TL_inputPeerContact();
						sendToPeer.user_id = sendToUser.id;
					}
				}
			}
		} else {
			encryptedChat = encryptedChats.get((int) (peer >> 32));
			newMsg.to_id = new TLRPC.TL_peerUser();
			if (encryptedChat.participant_id == UserConfig.clientUserId) {
				newMsg.to_id.user_id = encryptedChat.admin_id;
			} else {
				newMsg.to_id.user_id = encryptedChat.participant_id;
			}
			newMsg.ttl = encryptedChat.ttl;
		}
		newMsg.out = true;
		newMsg.date = ConnectionsManager.getInstance().getCurrentTime();
		newMsg.random_id = getNextRandomId();
		UserConfig.saveConfig(false);

		// 锟斤拷锟届函锟斤拷锟斤拷锟斤拷锟斤拷messageobject锟斤拷息锟斤拷锟斤拷
		final MessageObject newMsgObj = new MessageObject(newMsg, null);

		newMsgObj.messageOwner.send_state = MESSAGE_SEND_STATE_SENDING;

		final ArrayList<MessageObject> objArr = new ArrayList<MessageObject>();
		objArr.add(newMsgObj);
		ArrayList<TLRPC.Message> arr = new ArrayList<TLRPC.Message>();
		arr.add(newMsg);
		MessagesStorage.getInstance().putMessages(arr, false, true);
		// 锟斤拷锟斤拷锟斤拷didReceivedNewMessages锟斤拷息锟斤拷UI锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
		updateInterfaceWithMessages(peer, objArr);
		// 锟斤拷锟斤拷锟斤拷didReceivedNewMessages锟斤拷息锟斤拷UI,锟斤拷锟斤拷锟斤拷锟斤拷锟叫憋拷
		NotificationCenter.getInstance()
		.postNotificationName(dialogsNeedReload);

		sendingMessages.put(newMsg.id, newMsgObj);

		if (type == 0) {
			if (encryptedChat == null) {
				// 锟斤拷锟斤拷锟侥憋拷锟斤拷息
				TLRPC.TL_messages_sendMessage reqSend = new TLRPC.TL_messages_sendMessage();
				reqSend.message = message;
				reqSend.peer = sendToPeer;
				reqSend.random_id = newMsg.random_id;
				performSendMessageRequest(reqSend, newMsgObj);
			} else {
				TLRPC.TL_decryptedMessage reqSend = new TLRPC.TL_decryptedMessage();
				reqSend.random_id = newMsg.random_id;
				reqSend.random_bytes = new byte[Math.max(1,
						(int) Math.ceil(random.nextDouble() * 16))];
				random.nextBytes(reqSend.random_bytes);
				reqSend.message = message;
				reqSend.media = new TLRPC.TL_decryptedMessageMediaEmpty();
				performSendEncryptedRequest(reqSend, newMsgObj, encryptedChat,
						null);
			}
		} else if (type >= 1 && type <= 3 || type >= 5 && type <= 9) {
			if (encryptedChat == null) {
				TLRPC.TL_messages_sendMedia reqSend = new TLRPC.TL_messages_sendMedia();
				reqSend.peer = sendToPeer;
				reqSend.random_id = newMsg.random_id;
				if (type == 1) {
					// 位锟斤拷
					reqSend.media = new TLRPC.TL_inputMediaGeoPoint();
					reqSend.media.geo_point = new TLRPC.TL_inputGeoPoint();
					reqSend.media.geo_point.lat = lat;
					reqSend.media.geo_point._long = lon;
					performSendMessageRequest(reqSend, newMsgObj);
				} else if (type == 2) {
					reqSend.media = new TLRPC.TL_inputMediaUploadedPhoto();
					// xueqiang repair add
					reqSend.media.w = photo.sizes.get(0).w;
					reqSend.media.h = photo.sizes.get(0).h;
					reqSend.media.w1 = photo.sizes.get(photo.sizes.size() - 1).w;
					reqSend.media.h1 = photo.sizes.get(photo.sizes.size() - 1).h;
					reqSend.media.file_name = newMsg.attachPath;// 锟斤拷要锟斤拷锟叫匡拷锟斤拷锟角凤拷为锟斤拷一锟斤拷小图锟斤拷锟侥硷拷锟斤拷址
					reqSend.media.bytes = newMsg.media.photo.sizes.get(0).bytes;// 锟斤拷锟斤拷图锟斤拷锟斤拷
					reqSend.media.size = photo.sizes
							.get(photo.sizes.size() - 1).size;
					// xueqiang repair end
					DelayedMessage delayedMessage = new DelayedMessage();
					delayedMessage.sendRequest = reqSend;
					delayedMessage.type = 0;
					delayedMessage.obj = newMsgObj;
					delayedMessage.location = photo.sizes.get(photo.sizes
							.size() - 1).location;
					// 实锟斤拷锟斤拷通锟斤拷HTTP锟斤拷锟斤拷图片,xueqiang注锟斤拷
					performSendDelayedMessage(delayedMessage);
				} else if (type == 3) {
					reqSend.media = new TLRPC.TL_inputMediaUploadedThumbVideo();
					reqSend.media.duration = video.duration;
					reqSend.media.w = video.w;
					reqSend.media.h = video.h;
					DelayedMessage delayedMessage = new DelayedMessage();
					delayedMessage.sendRequest = reqSend;
					delayedMessage.type = 1;
					delayedMessage.obj = newMsgObj;
					delayedMessage.location = video.thumb.location;
					delayedMessage.videoLocation = video;
					performSendDelayedMessage(delayedMessage);
				} else if (type == 5) {
					reqSend.media = new TLRPC.TL_inputMediaPhoto();
					TLRPC.TL_inputPhoto ph = new TLRPC.TL_inputPhoto();
					ph.id = msgObj.messageOwner.action.photo.id;
					ph.access_hash = msgObj.messageOwner.action.photo.access_hash;
					((TLRPC.TL_inputMediaPhoto) reqSend.media).id = ph;
					performSendMessageRequest(reqSend, newMsgObj);
				} else if (type == 6) {
					reqSend.media = new TLRPC.TL_inputMediaContact();
					reqSend.media.phone_number = user.phone;
					reqSend.media.first_name = user.first_name;
					reqSend.media.last_name = user.last_name;
					performSendMessageRequest(reqSend, newMsgObj);
				} else if (type == 7) {
					reqSend.media = new TLRPC.TL_inputMediaUploadedDocument();
					reqSend.media.mime_type = document.mime_type;
					reqSend.media.file_name = document.file_name;
					// 锟斤拷锟斤拷一锟斤拷锟侥硷拷锟斤拷小
					reqSend.media.size = document.size;
					DelayedMessage delayedMessage = new DelayedMessage();
					delayedMessage.sendRequest = reqSend;
					delayedMessage.type = 2;
					delayedMessage.obj = newMsgObj;
					delayedMessage.documentLocation = document;
					delayedMessage.location = document.thumb.location;
					performSendDelayedMessage(delayedMessage);
				} else if (type == 8) {
					reqSend.media = new TLRPC.TL_inputMediaUploadedAudio();
					reqSend.media.duration = audio.duration;
					reqSend.media.file_name = audio.path;
					performSendMessageRequest(reqSend, newMsgObj);
				} else if (type == 9) {
					// xueqiang add for alert msg
					reqSend.media = new TLRPC.TL_inputMediaAlert();
					reqSend.media.alert = alert;
					performSendMessageRequest(reqSend, newMsgObj);
				}
			} else {
				TLRPC.TL_decryptedMessage reqSend = new TLRPC.TL_decryptedMessage();
				reqSend.random_id = newMsg.random_id;
				reqSend.random_bytes = new byte[Math.max(1,
						(int) Math.ceil(random.nextDouble() * 16))];
				random.nextBytes(reqSend.random_bytes);
				reqSend.message = "";
				if (type == 1) {
					reqSend.media = new TLRPC.TL_decryptedMessageMediaGeoPoint();
					reqSend.media.lat = lat;
					reqSend.media._long = lon;
					performSendEncryptedRequest(reqSend, newMsgObj,
							encryptedChat, null);
				} else if (type == 2) {
					reqSend.media = new TLRPC.TL_decryptedMessageMediaPhoto();
					reqSend.media.iv = new byte[32];
					reqSend.media.key = new byte[32];
					random.nextBytes(reqSend.media.iv);
					random.nextBytes(reqSend.media.key);
					TLRPC.PhotoSize small = photo.sizes.get(0);
					TLRPC.PhotoSize big = photo.sizes
							.get(photo.sizes.size() - 1);
					reqSend.media.thumb = small.bytes;
					reqSend.media.thumb_h = small.h;
					reqSend.media.thumb_w = small.w;
					reqSend.media.w = big.w;
					reqSend.media.h = big.h;
					reqSend.media.size = big.size;

					DelayedMessage delayedMessage = new DelayedMessage();
					delayedMessage.sendEncryptedRequest = reqSend;
					delayedMessage.type = 0;
					delayedMessage.obj = newMsgObj;
					delayedMessage.encryptedChat = encryptedChat;
					delayedMessage.location = photo.sizes.get(photo.sizes
							.size() - 1).location;
					performSendDelayedMessage(delayedMessage);
				} else if (type == 3) {
					reqSend.media = new TLRPC.TL_decryptedMessageMediaVideo();
					reqSend.media.iv = new byte[32];
					reqSend.media.key = new byte[32];
					random.nextBytes(reqSend.media.iv);
					random.nextBytes(reqSend.media.key);
					reqSend.media.duration = video.duration;
					reqSend.media.size = video.size;
					reqSend.media.w = video.w;
					reqSend.media.h = video.h;
					reqSend.media.thumb = video.thumb.bytes;
					reqSend.media.thumb_h = video.thumb.h;
					reqSend.media.thumb_w = video.thumb.w;

					DelayedMessage delayedMessage = new DelayedMessage();
					delayedMessage.sendEncryptedRequest = reqSend;
					delayedMessage.type = 1;
					delayedMessage.obj = newMsgObj;
					delayedMessage.encryptedChat = encryptedChat;
					delayedMessage.videoLocation = video;
					performSendDelayedMessage(delayedMessage);
				} else if (type == 5) {

				} else if (type == 6) {
					reqSend.media = new TLRPC.TL_decryptedMessageMediaContact();
					reqSend.media.phone_number = user.phone;
					reqSend.media.first_name = user.first_name;
					reqSend.media.last_name = user.last_name;
					reqSend.media.user_id = user.id;
					performSendEncryptedRequest(reqSend, newMsgObj,
							encryptedChat, null);
				} else if (type == 7) {
					reqSend.media = new TLRPC.TL_decryptedMessageMediaDocument();
					reqSend.media.iv = new byte[32];
					reqSend.media.key = new byte[32];
					random.nextBytes(reqSend.media.iv);
					random.nextBytes(reqSend.media.key);
					reqSend.media.size = document.size;
					if (!(document.thumb instanceof TLRPC.TL_photoSizeEmpty)) {
						reqSend.media.thumb = document.thumb.bytes;
						reqSend.media.thumb_h = document.thumb.h;
						reqSend.media.thumb_w = document.thumb.w;
					} else {
						reqSend.media.thumb = new byte[0];
						reqSend.media.thumb_h = 0;
						reqSend.media.thumb_w = 0;
					}
					reqSend.media.file_name = document.file_name;
					reqSend.media.mime_type = document.mime_type;

					DelayedMessage delayedMessage = new DelayedMessage();
					delayedMessage.sendEncryptedRequest = reqSend;
					delayedMessage.type = 2;
					delayedMessage.obj = newMsgObj;
					delayedMessage.encryptedChat = encryptedChat;
					delayedMessage.documentLocation = document;
					performSendDelayedMessage(delayedMessage);
				} else if (type == 8) {
					reqSend.media = new TLRPC.TL_decryptedMessageMediaAudio();
					reqSend.media.iv = new byte[32];
					reqSend.media.key = new byte[32];
					random.nextBytes(reqSend.media.iv);
					random.nextBytes(reqSend.media.key);
					reqSend.media.duration = audio.duration;
					reqSend.media.size = audio.size;

					DelayedMessage delayedMessage = new DelayedMessage();
					delayedMessage.sendEncryptedRequest = reqSend;
					delayedMessage.type = 3;
					delayedMessage.obj = newMsgObj;
					delayedMessage.encryptedChat = encryptedChat;
					delayedMessage.audioLocation = audio;
					performSendDelayedMessage(delayedMessage);
				}
			}
		} else if (type == 4) {
			/*
			 * TLRPC.TL_messages_forwardMessage reqSend = new
			 * TLRPC.TL_messages_forwardMessage(); reqSend.peer = sendToPeer;
			 * reqSend.random_id = newMsg.random_id; if (msgObj.messageOwner.id
			 * >= 0) { reqSend.id = msgObj.messageOwner.id; } else { reqSend.id
			 * = msgObj.messageOwner.fwd_msg_id; }
			 * performSendMessageRequest(reqSend, newMsgObj);
			 */

			if (newMsg.media instanceof TLRPC.TL_messageMediaAudio) {
				TLRPC.TL_messages_sendMedia reqSend = new TLRPC.TL_messages_sendMedia();
				reqSend.peer = sendToPeer;
				reqSend.random_id = newMsg.random_id;
				reqSend.media = new TLRPC.TL_inputMediaUploadedAudio();
				reqSend.media.duration = newMsg.media.audio.duration;
				reqSend.media.file_name = newMsg.attachPath;
				reqSend.media.file_name = newMsg.media.audio.path;
				performSendMessageRequest(reqSend, newMsgObj);
			} else if (newMsg.media instanceof TLRPC.TL_messageMediaPhoto) {
				TLRPC.TL_messages_sendMedia reqSend = new TLRPC.TL_messages_sendMedia();
				reqSend.peer = sendToPeer;
				reqSend.random_id = newMsg.random_id;
				reqSend.media = new TLRPC.TL_inputMediaUploadedPhoto();
				// xueqiang repair add
				reqSend.media.w = newMsg.media.photo.sizes.get(0).w;
				reqSend.media.h = newMsg.media.photo.sizes.get(0).h;
				reqSend.media.w1 = newMsg.media.photo.sizes
						.get(newMsg.media.photo.sizes.size() - 1).w;
				reqSend.media.h1 = newMsg.media.photo.sizes
						.get(newMsg.media.photo.sizes.size() - 1).h;
				reqSend.media.file_name = newMsg.attachPath;// 锟斤拷要锟斤拷锟叫匡拷锟斤拷锟角凤拷为锟斤拷一锟斤拷小图锟斤拷锟侥硷拷锟斤拷址
				reqSend.media.size = newMsg.media.photo.sizes
						.get(newMsg.media.photo.sizes.size() - 1).size;
				reqSend.media.bytes = newMsg.media.photo.sizes.get(0).bytes;// 锟斤拷锟斤拷图锟斤拷锟斤拷
				// xueqiang repair end
				DelayedMessage delayedMessage = new DelayedMessage();
				delayedMessage.sendRequest = reqSend;
				delayedMessage.type = 0;
				delayedMessage.obj = newMsgObj;
				delayedMessage.location = newMsg.media.photo.sizes
						.get(newMsg.media.photo.sizes.size() - 1).location;
				performSendDelayedMessage(delayedMessage);
			} else if (newMsg.media instanceof TLRPC.TL_messageMediaGeo) {
				// 位锟斤拷
				TLRPC.TL_messages_sendMedia reqSend = new TLRPC.TL_messages_sendMedia();
				reqSend.peer = sendToPeer;
				reqSend.random_id = newMsg.random_id;
				reqSend.media = new TLRPC.TL_inputMediaGeoPoint();
				reqSend.media.geo_point = new TLRPC.TL_inputGeoPoint();
				reqSend.media.geo_point.lat = newMsg.media.geo.lat;
				reqSend.media.geo_point._long = newMsg.media.geo._long;
				performSendMessageRequest(reqSend, newMsgObj);
			} else {
				TLRPC.TL_messages_sendMessage reqSend = new TLRPC.TL_messages_sendMessage();
				reqSend.message = newMsg.message;
				reqSend.peer = sendToPeer;
				reqSend.random_id = newMsg.random_id;
				performSendMessageRequest(reqSend, newMsgObj);
			}
		}
	}

	private void processSentMessage(TLRPC.Message newMsg,
			TLRPC.Message sentMessage, TLRPC.EncryptedFile file,
			TLRPC.DecryptedMessage decryptedMessage) {
		if (sentMessage != null) {
			if (sentMessage.media instanceof TLRPC.TL_messageMediaPhoto
					&& sentMessage.media.photo != null
					&& newMsg.media instanceof TLRPC.TL_messageMediaPhoto
					&& newMsg.media.photo != null) {
				for (TLRPC.PhotoSize size : sentMessage.media.photo.sizes) {
					if (size instanceof TLRPC.TL_photoSizeEmpty) {
						continue;
					}
					for (TLRPC.PhotoSize size2 : newMsg.media.photo.sizes) {
						if (size.type.equals(size2.type)) {
							String fileName = size2.location.volume_id + "_"
									+ size2.location.local_id;
							String fileName2 = size.location.volume_id + "_"
									+ size.location.local_id;
							if (fileName.equals(fileName2)) {
								break;
							}
							File cacheFile = new File(Utilities.getCacheDir(),
									fileName + ".jpg");
							File cacheFile2 = new File(Utilities.getCacheDir(),
									fileName2 + ".jpg");
							cacheFile.renameTo(cacheFile2);
							FileLoader.getInstance().replaceImageInCache(
									fileName, fileName2);
							size2.location = size.location;
							break;
						}
					}
				}
				sentMessage.message = newMsg.message;
				sentMessage.attachPath = newMsg.attachPath;
			} else if (sentMessage.media instanceof TLRPC.TL_messageMediaVideo
					&& sentMessage.media.video != null
					&& newMsg.media instanceof TLRPC.TL_messageMediaVideo
					&& newMsg.media.video != null) {
				TLRPC.PhotoSize size2 = newMsg.media.video.thumb;
				TLRPC.PhotoSize size = sentMessage.media.video.thumb;
				if (size2.location != null && size.location != null
						&& !(size instanceof TLRPC.TL_photoSizeEmpty)
						&& !(size2 instanceof TLRPC.TL_photoSizeEmpty)) {
					String fileName = size2.location.volume_id + "_"
							+ size2.location.local_id;
					String fileName2 = size.location.volume_id + "_"
							+ size.location.local_id;
					if (!fileName.equals(fileName2)) {
						File cacheFile = new File(Utilities.getCacheDir(),
								fileName + ".jpg");
						File cacheFile2 = new File(Utilities.getCacheDir(),
								fileName2 + ".jpg");
						boolean result = cacheFile.renameTo(cacheFile2);
						FileLoader.getInstance().replaceImageInCache(fileName,
								fileName2);
						size2.location = size.location;
					}
				}
				sentMessage.message = newMsg.message;
				sentMessage.attachPath = newMsg.attachPath;
			} else if (sentMessage.media instanceof TLRPC.TL_messageMediaDocument
					&& sentMessage.media.document != null
					&& newMsg.media instanceof TLRPC.TL_messageMediaDocument
					&& newMsg.media.document != null) {
				TLRPC.PhotoSize size2 = newMsg.media.document.thumb;
				TLRPC.PhotoSize size = sentMessage.media.document.thumb;
				if (size2.location != null && size.location != null
						&& !(size instanceof TLRPC.TL_photoSizeEmpty)
						&& !(size2 instanceof TLRPC.TL_photoSizeEmpty)) {
					String fileName = size2.location.volume_id + "_"
							+ size2.location.local_id;
					String fileName2 = size.location.volume_id + "_"
							+ size.location.local_id;
					if (!fileName.equals(fileName2)) {
						File cacheFile = new File(Utilities.getCacheDir(),
								fileName + ".jpg");
						File cacheFile2 = new File(Utilities.getCacheDir(),
								fileName2 + ".jpg");
						boolean result = cacheFile.renameTo(cacheFile2);
						FileLoader.getInstance().replaceImageInCache(fileName,
								fileName2);
						size2.location = size.location;
					}
				}
				sentMessage.message = newMsg.message;
				sentMessage.attachPath = newMsg.attachPath;
			} else if (sentMessage.media instanceof TLRPC.TL_messageMediaAudio
					&& sentMessage.media.audio != null
					&& newMsg.media instanceof TLRPC.TL_messageMediaAudio
					&& newMsg.media.audio != null) {
				sentMessage.message = newMsg.message;
				sentMessage.attachPath = newMsg.attachPath;

				sentMessage.media.audio.id = newMsg.media.audio.id;
				sentMessage.media.audio.dc_id = newMsg.media.audio.dc_id;
				/*
				 * String fileName = newMsg.media.audio.dc_id + "_" +
				 * newMsg.media.audio.id + ".m4a"; String fileName2 =
				 * sentMessage.media.audio.dc_id + "_" +
				 * sentMessage.media.audio.id + ".m4a"; if
				 * (!fileName.equals(fileName2)) { File cacheFile = new
				 * File(Utilities.getCacheDir(), fileName); File cacheFile2 =
				 * new File(Utilities.getCacheDir(), fileName2);
				 * cacheFile.renameTo(cacheFile2); newMsg.media.audio.dc_id =
				 * sentMessage.media.audio.dc_id; newMsg.media.audio.id =
				 * sentMessage.media.audio.id; }
				 */
			}
		} else if (file != null) {
			if (newMsg.media instanceof TLRPC.TL_messageMediaPhoto
					&& newMsg.media.photo != null) {
				TLRPC.PhotoSize size = newMsg.media.photo.sizes
						.get(newMsg.media.photo.sizes.size() - 1);
				String fileName = size.location.volume_id + "_"
						+ size.location.local_id;
				size.location = new TLRPC.TL_fileEncryptedLocation();
				size.location.key = decryptedMessage.media.key;
				size.location.iv = decryptedMessage.media.iv;
				size.location.dc_id = file.dc_id;
				size.location.volume_id = file.id;
				size.location.secret = file.access_hash;
				size.location.local_id = file.key_fingerprint;
				String fileName2 = size.location.volume_id + "_"
						+ size.location.local_id;
				File cacheFile = new File(Utilities.getCacheDir(), fileName
						+ ".jpg");
				File cacheFile2 = new File(Utilities.getCacheDir(), fileName2
						+ ".jpg");
				boolean result = cacheFile.renameTo(cacheFile2);
				FileLoader.getInstance().replaceImageInCache(fileName,
						fileName2);
				ArrayList<TLRPC.Message> arr = new ArrayList<TLRPC.Message>();
				arr.add(newMsg);
				MessagesStorage.getInstance().putMessages(arr, false, true);
			} else if (newMsg.media instanceof TLRPC.TL_messageMediaVideo
					&& newMsg.media.video != null) {
				TLRPC.Video video = newMsg.media.video;
				newMsg.media.video = new TLRPC.TL_videoEncrypted();
				newMsg.media.video.duration = video.duration;
				newMsg.media.video.thumb = video.thumb;
				newMsg.media.video.id = video.id;
				newMsg.media.video.dc_id = file.dc_id;
				newMsg.media.video.w = video.w;
				newMsg.media.video.h = video.h;
				newMsg.media.video.date = video.date;
				newMsg.media.video.caption = "";
				newMsg.media.video.user_id = video.user_id;
				newMsg.media.video.size = file.size;
				newMsg.media.video.id = file.id;
				newMsg.media.video.access_hash = file.access_hash;
				newMsg.media.video.key = decryptedMessage.media.key;
				newMsg.media.video.iv = decryptedMessage.media.iv;
				newMsg.media.video.path = video.path;
				ArrayList<TLRPC.Message> arr = new ArrayList<TLRPC.Message>();
				arr.add(newMsg);
				MessagesStorage.getInstance().putMessages(arr, false, true);
			} else if (newMsg.media instanceof TLRPC.TL_messageMediaDocument
					&& newMsg.media.document != null) {
				TLRPC.Document document = newMsg.media.document;
				newMsg.media.document = new TLRPC.TL_documentEncrypted();
				newMsg.media.document.id = file.id;
				newMsg.media.document.access_hash = file.access_hash;
				newMsg.media.document.user_id = document.user_id;
				newMsg.media.document.date = document.date;
				newMsg.media.document.file_name = document.file_name;
				newMsg.media.document.mime_type = document.mime_type;
				newMsg.media.document.size = file.size;
				newMsg.media.document.key = decryptedMessage.media.key;
				newMsg.media.document.iv = decryptedMessage.media.iv;
				newMsg.media.document.path = document.path;
				newMsg.media.document.thumb = document.thumb;
				newMsg.media.document.dc_id = file.dc_id;
				ArrayList<TLRPC.Message> arr = new ArrayList<TLRPC.Message>();
				arr.add(newMsg);
				MessagesStorage.getInstance().putMessages(arr, false, true);
			} else if (newMsg.media instanceof TLRPC.TL_messageMediaAudio
					&& newMsg.media.audio != null) {
				TLRPC.Audio audio = newMsg.media.audio;
				newMsg.media.audio = new TLRPC.TL_audioEncrypted();
				newMsg.media.audio.id = file.id;
				newMsg.media.audio.access_hash = file.access_hash;
				newMsg.media.audio.user_id = audio.user_id;
				newMsg.media.audio.date = audio.date;
				newMsg.media.audio.duration = audio.duration;
				newMsg.media.audio.size = file.size;
				newMsg.media.audio.dc_id = file.dc_id;
				newMsg.media.audio.key = decryptedMessage.media.key;
				newMsg.media.audio.iv = decryptedMessage.media.iv;
				newMsg.media.audio.path = audio.path;

				String fileName = audio.dc_id + "_" + audio.id + ".m4a";
				String fileName2 = newMsg.media.audio.dc_id + "_"
						+ newMsg.media.audio.id + ".m4a";
				if (!fileName.equals(fileName2)) {
					File cacheFile = new File(Utilities.getCacheDir(), fileName);
					File cacheFile2 = new File(Utilities.getCacheDir(),
							fileName2);
					cacheFile.renameTo(cacheFile2);
				}

				ArrayList<TLRPC.Message> arr = new ArrayList<TLRPC.Message>();
				arr.add(newMsg);
				MessagesStorage.getInstance().putMessages(arr, false, true);
			}
		}
	}

	private void performSendEncryptedRequest(final TLRPC.DecryptedMessage req,
			final MessageObject newMsgObj, final TLRPC.EncryptedChat chat,
			final TLRPC.InputEncryptedFile encryptedFile) {
		if (req == null || chat.auth_key == null
				|| chat instanceof TLRPC.TL_encryptedChatRequested
				|| chat instanceof TLRPC.TL_encryptedChatWaiting) {
			return;
		}
		// TLRPC.decryptedMessageLayer messageLayer = new
		// TLRPC.decryptedMessageLayer();
		// messageLayer.layer = 8;
		// messageLayer.message = req;
		SerializedData data = new SerializedData();
		req.serializeToStream(data);

		SerializedData toEncrypt = new SerializedData();
		toEncrypt.writeInt32(data.length());
		toEncrypt.writeRaw(data.toByteArray());

		byte[] innerData = toEncrypt.toByteArray();

		byte[] messageKeyFull = Utilities.computeSHA1(innerData);
		byte[] messageKey = new byte[16];
		System.arraycopy(messageKeyFull, messageKeyFull.length - 16,
				messageKey, 0, 16);

		MessageKeyData keyData = Utilities.generateMessageKeyData(
				chat.auth_key, messageKey, false);

		SerializedData dataForEncryption = new SerializedData();
		dataForEncryption.writeRaw(innerData);
		byte[] b = new byte[1];
		while (dataForEncryption.length() % 16 != 0) {
			MessagesController.random.nextBytes(b);
			dataForEncryption.writeByte(b[0]);
		}

		byte[] encryptedData = Utilities.aesIgeEncryption(
				dataForEncryption.toByteArray(), keyData.aesKey, keyData.aesIv,
				true, false, 0);

		data = new SerializedData();
		data.writeInt64(chat.key_fingerprint);
		data.writeRaw(messageKey);
		data.writeRaw(encryptedData);

		TLObject reqToSend;

		if (encryptedFile == null) {
			TLRPC.TL_messages_sendEncrypted req2 = new TLRPC.TL_messages_sendEncrypted();
			req2.data = data.toByteArray();
			req2.random_id = req.random_id;
			req2.peer = new TLRPC.TL_inputEncryptedChat();
			req2.peer.chat_id = chat.id;
			req2.peer.access_hash = chat.access_hash;
			reqToSend = req2;
		} else {
			TLRPC.TL_messages_sendEncryptedFile req2 = new TLRPC.TL_messages_sendEncryptedFile();
			req2.data = data.toByteArray();
			req2.random_id = req.random_id;
			req2.peer = new TLRPC.TL_inputEncryptedChat();
			req2.peer.chat_id = chat.id;
			req2.peer.access_hash = chat.access_hash;
			req2.file = encryptedFile;
			reqToSend = req2;
		}
		ConnectionsManager.getInstance().performRpc(
				reqToSend,
				new RPCRequest.RPCRequestDelegate() {
					@Override
					public void run(TLObject response, TLRPC.TL_error error) {
						if (newMsgObj != null) {
							if (error == null) {
								TLRPC.messages_SentEncryptedMessage res = (TLRPC.messages_SentEncryptedMessage) response;
								newMsgObj.messageOwner.date = res.date;
								if (res.file instanceof TLRPC.TL_encryptedFile) {
									processSentMessage(newMsgObj.messageOwner,
											null, res.file, req);
								}

								MessagesStorage
								.getInstance()
								.updateMessageStateAndId(
										newMsgObj.messageOwner.random_id,
										newMsgObj.messageOwner.id,
										newMsgObj.messageOwner.id,
										res.date, true);
								MessagesStorage.getInstance().storageQueue
								.postRunnable(new Runnable() {
									@Override
									public void run() {
										Utilities
										.RunOnUIThread(new Runnable() {
											@Override
											public void run() {
												newMsgObj.messageOwner.send_state = MESSAGE_SEND_STATE_SENT;
												NotificationCenter
												.getInstance()
												.postNotificationName(
														messageReceivedByServer,
														newMsgObj.messageOwner.id,
														newMsgObj.messageOwner.id,
														newMsgObj);
												sendingMessages
												.remove(newMsgObj.messageOwner.id);
											}
										});
									}
								});
							} else {
								Utilities.RunOnUIThread(new Runnable() {
									@Override
									public void run() {
										sendingMessages
										.remove(newMsgObj.messageOwner.id);
										newMsgObj.messageOwner.send_state = MESSAGE_SEND_STATE_SEND_ERROR;
										NotificationCenter
										.getInstance()
										.postNotificationName(
												messageSendError,
												newMsgObj.messageOwner.id);
									}
								});
							}
						}
					}
				},
				null,
				true,
				RPCRequest.RPCRequestClassGeneric
				| RPCRequest.RPCRequestClassCanCompress);
	}

	private void performSendMessageRequest(TLObject req,
			final MessageObject newMsgObj) {
		ConnectionsManager.getInstance().performRpc(
				req,
				new RPCRequest.RPCRequestDelegate() {
					@Override
					public void run(TLObject response, TLRPC.TL_error error) {
						if (error == null) {
							final int oldId = newMsgObj.messageOwner.id;
							ArrayList<TLRPC.Message> sentMessages = new ArrayList<TLRPC.Message>();

							if (response instanceof TLRPC.TL_messages_sentMessage) {
								TLRPC.TL_messages_sentMessage res = (TLRPC.TL_messages_sentMessage) response;
								newMsgObj.messageOwner.id = res.id;
							} else if (response instanceof TLRPC.messages_StatedMessage) {
								TLRPC.messages_StatedMessage res = (TLRPC.messages_StatedMessage) response;
								if (res.message.media instanceof TLRPC.TL_messageMediaAudio) {
									res.message.media.audio.id = newMsgObj.messageOwner.media.audio.id;
									res.message.media.audio.dc_id = newMsgObj.messageOwner.media.audio.dc_id;
								} else if (res.message.media instanceof TLRPC.TL_messageMediaPhoto)
									res.message.media.photo.sizes = newMsgObj.messageOwner.media.photo.sizes;
								else if (res.message.media instanceof TLRPC.TL_messageMediaDocument) {
									// xueqiang add for filepath
									res.message.attachPath = newMsgObj.messageOwner.attachPath;
								} else if (res.message.media instanceof TLRPC.TL_messageMediaAlert) {
									// xueqiang add for alert,save to db
									TLRPC.AlertMedia alertMedia = res.message.media.alert;
									MessagesStorage.getInstance().putAlert(
											alertMedia, false);
								}
								sentMessages.add(res.message);
								newMsgObj.messageOwner.id = res.message.id;
								// end
								processSentMessage(newMsgObj.messageOwner,
										res.message, null, null);

							} else if (response instanceof TLRPC.messages_StatedMessages) {
								TLRPC.messages_StatedMessages res = (TLRPC.messages_StatedMessages) response;
								if (!res.messages.isEmpty()) {
									TLRPC.Message message = res.messages.get(0);
									newMsgObj.messageOwner.id = message.id;
									sentMessages.add(message);
									processSentMessage(newMsgObj.messageOwner,
											message, null, null);
								}
							}

							MessagesStorage.getInstance()
							.updateMessageStateAndId(
									newMsgObj.messageOwner.random_id,
									oldId, newMsgObj.messageOwner.id,
									0, true);
							if (!sentMessages.isEmpty()) {
								MessagesStorage.getInstance().putMessages(
										sentMessages, true, true);
							}
							MessagesStorage.getInstance().storageQueue
							.postRunnable(new Runnable() {
								@Override
								public void run() {
									Utilities
									.RunOnUIThread(new Runnable() {
										@Override
										public void run() {
											newMsgObj.messageOwner.send_state = MESSAGE_SEND_STATE_SENT;
											// 锟斤拷锟斤拷息锟斤拷锟酵革拷锟斤拷锟斤拷去锟斤拷示
											NotificationCenter
											.getInstance()
											.postNotificationName(
													messageReceivedByServer,
													oldId,
													newMsgObj.messageOwner.id,
													newMsgObj);
											sendingMessages
											.remove(oldId);
											// sam
											UEngine.getInstance()
											.getSoundService()
											.playMidSound(
													0);
										}
									});
								}
							});
						} else {
							Utilities.RunOnUIThread(new Runnable() {
								@Override
								public void run() {
									sendingMessages
									.remove(newMsgObj.messageOwner.id);
									newMsgObj.messageOwner.send_state = MESSAGE_SEND_STATE_SEND_ERROR;
									NotificationCenter.getInstance()
									.postNotificationName(
											messageSendError,
											newMsgObj.messageOwner.id);
								}
							});
						}
					}
				},
				null,
				(req instanceof TLRPC.TL_messages_forwardMessages ? null
						: new RPCRequest.RPCQuickAckDelegate() {
					@Override
					public void quickAck() {
						final int msg_id = newMsgObj.messageOwner.id;
						Utilities.RunOnUIThread(new Runnable() {
							@Override
							public void run() {
								newMsgObj.messageOwner.send_state = MESSAGE_SEND_STATE_SENT;
								NotificationCenter.getInstance()
								.postNotificationName(
										messageReceivedByAck,
										msg_id);
							}
						});
					}
				}),
				true,
				RPCRequest.RPCRequestClassGeneric
				| RPCRequest.RPCRequestClassFailOnServerErrors
				| RPCRequest.RPCRequestClassCanCompress,
				ConnectionsManager.DEFAULT_DATACENTER_ID);
	}

	private void putToDelayedMessages(String location, DelayedMessage message) {
		ArrayList<DelayedMessage> arrayList = delayedMessages.get(location);
		if (arrayList == null) {
			arrayList = new ArrayList<DelayedMessage>();
			delayedMessages.put(location, arrayList);
		}
		arrayList.add(message);
	}

	private void performSendDelayedMessage(final DelayedMessage message) {
		if (message.type == 0) {
			// 锟斤拷锟斤拷图片通锟斤拷http,xueqiang注锟斤拷
			String location = Utilities.getCacheDir() + "/"
					+ message.location.volume_id + "_"
					+ message.location.local_id + ".jpg";
			putToDelayedMessages(location, message);
			if (message.sendRequest != null) {
				FileLoader.getInstance().uploadFile(location, null, null);
			} else {
				FileLoader.getInstance().uploadFile(location,
						message.sendEncryptedRequest.media.key,
						message.sendEncryptedRequest.media.iv);
			}
		} else if (message.type == 1) {
			if (message.sendRequest != null) {
				if (message.sendRequest.media.thumb == null) {
					String location = Utilities.getCacheDir() + "/"
							+ message.location.volume_id + "_"
							+ message.location.local_id + ".jpg";
					putToDelayedMessages(location, message);
					FileLoader.getInstance().uploadFile(location, null, null);
				} else {
					String location = message.videoLocation.path;
					if (location == null) {
						location = Utilities.getCacheDir() + "/"
								+ message.videoLocation.id + ".mp4";
					}
					putToDelayedMessages(location, message);
					FileLoader.getInstance().uploadFile(location, null, null);
				}
			} else {
				String location = message.videoLocation.path;
				if (location == null) {
					location = Utilities.getCacheDir() + "/"
							+ message.videoLocation.id + ".mp4";
				}
				putToDelayedMessages(location, message);
				FileLoader.getInstance().uploadFile(location,
						message.sendEncryptedRequest.media.key,
						message.sendEncryptedRequest.media.iv);
			}
		} else if (message.type == 2) {
			if (message.sendRequest != null
					&& message.sendRequest.media.thumb == null
					&& message.location != null) {
				String location = Utilities.getCacheDir() + "/"
						+ message.location.volume_id + "_"
						+ message.location.local_id + ".jpg";
				putToDelayedMessages(location, message);
				FileLoader.getInstance().uploadFile(location, null, null);
			} else {
				String location = message.documentLocation.path;
				putToDelayedMessages(location, message);
				if (message.sendRequest != null) {
					// xueqiang change为锟剿达拷锟斤拷锟侥硷拷锟斤拷锟矫革拷锟斤拷锟侥硷拷锟斤拷追
					FileLoader.getInstance().randomfile = 0;
					FileLoader.getInstance().uploadFile(location, null, null);
				} else {
					FileLoader.getInstance().uploadFile(location,
							message.sendEncryptedRequest.media.key,
							message.sendEncryptedRequest.media.iv);
				}
			}
		} else if (message.type == 3) {
			String location = message.audioLocation.path;
			putToDelayedMessages(location, message);
			if (message.sendRequest != null) {
				FileLoader.getInstance().uploadFile(location, null, null);
			} else {
				FileLoader.getInstance().uploadFile(location,
						message.sendEncryptedRequest.media.key,
						message.sendEncryptedRequest.media.iv);
			}
		}
	}

	public void fileDidFailedUpload(final String location, final boolean enc) {
		if (uploadingAvatar != null && uploadingAvatar.equals(location)) {
			uploadingAvatar = null;
		} else {
			Utilities.RunOnUIThread(new Runnable() {
				@Override
				public void run() {
					ArrayList<DelayedMessage> arr = delayedMessages
							.get(location);
					if (arr != null) {
						for (int a = 0; a < arr.size(); a++) {
							DelayedMessage obj = arr.get(a);
							if (enc && obj.sendEncryptedRequest != null || !enc
									&& obj.sendRequest != null) {
								obj.obj.messageOwner.send_state = MESSAGE_SEND_STATE_SEND_ERROR;
								sendingMessages.remove(obj.obj.messageOwner.id);
								arr.remove(a);
								a--;
								NotificationCenter.getInstance()
								.postNotificationName(messageSendError,
										obj.obj.messageOwner.id);
							}
						}
						if (arr.isEmpty()) {
							delayedMessages.remove(location);
						}
					}
				}
			});
		}
	}

	public void fileDidUploaded(final String location,
			final TLRPC.InputFile file,
			final TLRPC.InputEncryptedFile encryptedFile, final String url) {
		// 锟较达拷图片锟斤拷珊锟斤拷锟铰硷拷锟斤拷xueqiang注锟斤拷
		if (uploadingAvatar != null && uploadingAvatar.equals(location)) {
			TLRPC.TL_photos_uploadProfilePhoto req = new TLRPC.TL_photos_uploadProfilePhoto();
			req.caption = "";
			req.crop = new TLRPC.TL_inputPhotoCropAuto();
			req.file = file;
			req.geo_point = new TLRPC.TL_inputGeoPointEmpty();
			ConnectionsManager.getInstance().performRpc(req,
					new RPCRequest.RPCRequestDelegate() {
				@Override
				public void run(TLObject response, TLRPC.TL_error error) {
					if (error == null) {
						TLRPC.User user = users
								.get(UserConfig.clientUserId);
						if (user == null) {
							user = UserConfig.currentUser;
							users.put(user.id, user);
							// add by xueqiang
							usersSDK.put(user.identification, user);
						} else {
							user.sessionid = UserConfig.currentUser.sessionid;
							UserConfig.currentUser = user;
						}
						if (user == null) {
							return;
						}
						TLRPC.TL_photos_photo photo = (TLRPC.TL_photos_photo) response;
						ArrayList<TLRPC.PhotoSize> sizes = photo.photo.sizes;
						TLRPC.PhotoSize smallSize = PhotoObject
								.getClosestPhotoSizeWithSize(sizes,
										100, 100);
						TLRPC.PhotoSize bigSize = PhotoObject
								.getClosestPhotoSizeWithSize(sizes,
										1000, 1000);
						user.photo = new TLRPC.TL_userProfilePhoto();
						user.photo.photo_id = photo.photo.id;
						if (smallSize != null) {
							user.photo.photo_small = smallSize.location;
						}
						if (bigSize != null) {
							user.photo.photo_big = bigSize.location;
						} else if (smallSize != null) {
							user.photo.photo_small = smallSize.location;
						}
						MessagesStorage.getInstance().clearUserPhotos(
								user.id);
						ArrayList<TLRPC.User> users = new ArrayList<TLRPC.User>();
						users.add(user);
						MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
						Utilities.RunOnUIThread(new Runnable() {
							@Override
							public void run() {
								NotificationCenter.getInstance()
								.postNotificationName(
										updateInterfaces,
										UPDATE_MASK_AVATAR);
								UserConfig.saveConfig(true);
							}
						});
					}
				}
			}, null, true, RPCRequest.RPCRequestClassGeneric);
		} else {
			Utilities.RunOnUIThread(new Runnable() {
				@Override
				public void run() {
					ArrayList<DelayedMessage> arr = delayedMessages
							.get(location);
					if (arr != null) {
						for (int a = 0; a < arr.size(); a++) {
							DelayedMessage message = arr.get(a);
							if (file != null && message.sendRequest != null) {
								if (message.type == 0) {
									// 锟斤拷锟斤拷图片锟斤拷珊锟斤拷锟斤拷锟斤拷铮拷锟斤拷锟斤拷缃�100x100锟斤拷图通锟斤拷bytes[]锟斤拷式锟斤拷锟斤拷去
									// xueqiang 注锟斤拷
									message.sendRequest.media.file = file;
									// add by xueqiang
									message.sendRequest.media.url = url;
									// end
									int len = message.obj.messageOwner.media.photo.sizes
											.size();
									// 锟侥硷拷没锟斤拷small图锟斤拷只锟斤拷锟斤拷透锟斤拷锟酵凤拷锟斤拷锟�
									for (int i = 1; i < len; i++) {
										message.obj.messageOwner.media.photo.sizes
										.get(i).location.http_path_img = url;
									}
									// xueqiang 注锟斤拷
									performSendMessageRequest(
											message.sendRequest, message.obj);
								} else if (message.type == 1) {
									if (message.sendRequest.media.thumb == null) {
										message.sendRequest.media.thumb = file;
										performSendDelayedMessage(message);
									} else {
										message.sendRequest.media.file = file;
										performSendMessageRequest(
												message.sendRequest,
												message.obj);
									}
								} else if (message.type == 2) {
									if (message.sendRequest.media.thumb == null
											&& message.location != null) {
										message.sendRequest.media.thumb = file;
										performSendDelayedMessage(message);
									} else {
										message.sendRequest.media.file = file;
										// xueqiang add next line
										message.sendRequest.media.url = url;
										performSendMessageRequest(
												message.sendRequest,
												message.obj);
									}
								} else if (message.type == 3) {
									message.sendRequest.media.file = file;
									performSendMessageRequest(
											message.sendRequest, message.obj);
								}
								arr.remove(a);
								a--;
							} else if (encryptedFile != null
									&& message.sendEncryptedRequest != null) {
								performSendEncryptedRequest(
										message.sendEncryptedRequest,
										message.obj, message.encryptedChat,
										encryptedFile);
								arr.remove(a);
								a--;
							}
						}
						if (arr.isEmpty()) {
							delayedMessages.remove(location);
						}
					}
				}
			});
		}
	}

	// muzf 锟斤拷锟斤拷群锟斤拷
	public long createChat(String title,
			final ArrayList<Integer> selectedContacts,
			final TLRPC.InputFile uploadedAvatar, final TLRPC.PhotoSize small,
			final TLRPC.PhotoSize big) {
		TLRPC.TL_messages_createChat req = new TLRPC.TL_messages_createChat();
		req.title = title;
		if (uploadedAvatar != null)
			req.groupico = uploadedAvatar.http_path_img;// 锟斤拷头锟斤拷锟街�
		for (Integer uid : selectedContacts) {
			TLRPC.User user = users.get(uid);
			if (user == null) {
				continue;
			}
			// req.companyID = user.companyid;
			req.users.add(getInputUser(user));
		}
		return ConnectionsManager.getInstance().performRpc(req,
				new RPCRequest.RPCRequestDelegate() {
			@Override
			public void run(TLObject response, TLRPC.TL_error error) {
				if (error != null) {
					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() {
							NotificationCenter.getInstance()
							.postNotificationName(
									chatDidFailCreate);
						}
					});
					return;
				}
				final TLRPC.messages_StatedMessage res = (TLRPC.messages_StatedMessage) response;

				MessagesStorage.getInstance().putUsersAndChats(
						res.users, res.chats, true, true);

				// add by xueqiang
				// begin,目锟侥斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟较拷娲拷锟斤拷锟斤拷锟絚hatactivity锟斤拷锟斤拷锟斤拷processchatinfo锟斤拷使锟斤拷锟斤拷锟斤拷锟斤拷锟�
				int chat_id = res.chats.get(0).id;
				TLRPC.TL_chatParticipants participants = new TLRPC.TL_chatParticipants();
				participants.admin_id = UserConfig.clientUserId;
				participants.chat_id = chat_id;
				participants.version = 0;
				// 锟斤拷锟斤拷participants and save db through updateChatInfo
				for (int i = 0; i < selectedContacts.size(); i++) {
					int userid = selectedContacts.get(i);
					if (userid == 0)
						continue;

					TLRPC.TL_chatParticipant participant = new TLRPC.TL_chatParticipant();
					participant.date = ConnectionsManager.getInstance()
							.getCurrentTime();
					participant.inviter_id = UserConfig.clientUserId;
					participant.user_id = userid;
					participant.nick_name = "";
					participant.status = 0;
					participants.participants.add(participant);
				}
				MessagesStorage.getInstance().updateChatInfo(chat_id,
						participants, false);

				// add by xueqiang end

				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						for (TLRPC.User user : res.users) {
							users.put(user.id, user);
							// add by xueqiang
							usersSDK.put(user.identification, user);
							if (user.id == UserConfig.clientUserId) {
								user.sessionid = UserConfig.currentUser.sessionid;
								UserConfig.currentUser = user;
							}
						}
						for (TLRPC.Chat chat : res.chats) {
							chats.put(chat.id, chat);
						}

						final ArrayList<MessageObject> messagesObj = new ArrayList<MessageObject>();
						messagesObj.add(new MessageObject(res.message,
								users));
						TLRPC.Chat chat = res.chats.get(0);
						updateInterfaceWithMessages(-chat.id,
								messagesObj);
						NotificationCenter.getInstance()
						.postNotificationName(chatDidCreated,
								chat.id);

						NotificationCenter
						.getInstance()
						.postNotificationName(dialogsNeedReload);
						if (uploadedAvatar != null) {
							changeChatAvatar(chat.id, uploadedAvatar,
									small, big);
						}
					}
				});

				final ArrayList<TLRPC.Message> messages = new ArrayList<TLRPC.Message>();
				messages.add(res.message);
				MessagesStorage.getInstance().putMessages(messages,
						true, true);
			}
		});
	}

	// muzf 锟斤拷锟斤拷没锟斤拷锟饺猴拷锟�
	public void addUserToChat(int chat_id, final TLRPC.User user,
			final TLRPC.ChatParticipants info) {
		if (user == null) {
			return;
		}

		TLRPC.TL_messages_addChatUser req = new TLRPC.TL_messages_addChatUser();
		req.chat_id = chat_id;
		req.fwd_limit = 50;
		req.user_id = getInputUser(user);

		ConnectionsManager.getInstance().performRpc(req,
				new RPCRequest.RPCRequestDelegate() {
			@Override
			public void run(TLObject response, TLRPC.TL_error error) {
				if (error != null) {
					return;
				}

				final TLRPC.messages_StatedMessage res = (TLRPC.messages_StatedMessage) response;
				MessagesStorage.getInstance().putUsersAndChats(
						res.users, res.chats, true, true);

				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						for (TLRPC.User user : res.users) {
							users.put(user.id, user);
							// add by xueqiang
							usersSDK.put(user.identification, user);
							if (user.id == UserConfig.clientUserId) {
								user.sessionid = UserConfig.currentUser.sessionid;
								UserConfig.currentUser = user;
							}
						}
						for (TLRPC.Chat chat : res.chats) {
							chats.put(chat.id, chat);
						}
						final ArrayList<MessageObject> messagesObj = new ArrayList<MessageObject>();
						messagesObj.add(new MessageObject(res.message,
								users));
						TLRPC.Chat chat = res.chats.get(0);
						chats.put(chat.id, chat);
						updateInterfaceWithMessages(-chat.id,
								messagesObj);
						NotificationCenter.getInstance()
						.postNotificationName(updateInterfaces,
								UPDATE_MASK_CHAT_MEMBERS);

						NotificationCenter
						.getInstance()
						.postNotificationName(dialogsNeedReload);
						// 锟芥储锟铰碉拷锟斤拷锟皆憋拷锟紻B锟斤拷
						if (info != null) {
							for (TLRPC.TL_chatParticipant p : info.participants) {
								if (p.user_id == user.id) {
									return;
								}
							}
							TLRPC.TL_chatParticipant newPart = new TLRPC.TL_chatParticipant();
							newPart.user_id = user.id;
							newPart.inviter_id = UserConfig.clientUserId;
							newPart.date = ConnectionsManager
									.getInstance().getCurrentTime();
							newPart.nick_name = "";
							newPart.status = 0;
							info.participants.add(0, newPart);
							MessagesStorage.getInstance()
							.updateChatInfo(info.chat_id, info,
									true);
							NotificationCenter.getInstance()
							.postNotificationName(
									chatInfoDidLoaded,
									info.chat_id, info);
						}
					}
				});

				final ArrayList<TLRPC.Message> messages = new ArrayList<TLRPC.Message>();
				messages.add(res.message);
				MessagesStorage.getInstance().putMessages(messages,
						true, true);
			}
		});

	}

	// muzf 锟斤拷群锟斤拷锟斤拷删锟斤拷锟矫伙拷 删锟斤拷锟斤拷
	public void deleteUserFromChat(int chat_id, final TLRPC.User user,
			final TLRPC.ChatParticipants info) {
		if (user == null) {
			return;
		}

		TLRPC.TL_messages_deleteChatUser req = new TLRPC.TL_messages_deleteChatUser();
		req.chat_id = chat_id;
		req.user_id = getInputUser(user);
		ConnectionsManager.getInstance().performRpc(req,
				new RPCRequest.RPCRequestDelegate() {
			@Override
			public void run(TLObject response, TLRPC.TL_error error) {
				if (error != null) {
					return;
				}
				// 锟皆硷拷锟斤拷锟皆硷拷锟斤拷锟斤拷锟斤拷删锟斤拷
				if (user.id == UserConfig.clientUserId) {
					TLRPC.User TEMP = user;
					TEMP = MessagesController.getInstance().users
							.get(user.id);
					return;
				}
				final TLRPC.messages_StatedMessage res = (TLRPC.messages_StatedMessage) response;
				MessagesStorage.getInstance().putUsersAndChats(
						res.users, res.chats, true, true);

				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						for (TLRPC.User user : res.users) {
							users.put(user.id, user);
							// add by xueqiang
							usersSDK.put(user.identification, user);
							if (user.id == UserConfig.clientUserId) {
								user.sessionid = UserConfig.currentUser.sessionid;
								UserConfig.currentUser = user;
							}
						}
						for (TLRPC.Chat chat : res.chats) {
							chats.put(chat.id, chat);
						}
						if (user.id != UserConfig.clientUserId) {
							final ArrayList<MessageObject> messagesObj = new ArrayList<MessageObject>();
							messagesObj.add(new MessageObject(
									res.message, users));
							TLRPC.Chat chat = res.chats.get(0);
							chats.put(chat.id, chat);
							updateInterfaceWithMessages(-chat.id,
									messagesObj);
							NotificationCenter.getInstance()
							.postNotificationName(
									updateInterfaces,
									UPDATE_MASK_CHAT_MEMBERS);

							NotificationCenter.getInstance()
							.postNotificationName(
									dialogsNeedReload);
						}
						boolean changed = false;
						if (info != null) {
							for (int a = 0; a < info.participants
									.size(); a++) {
								TLRPC.TL_chatParticipant p = info.participants
										.get(a);
								if (p.user_id == user.id) {
									info.participants.remove(a);
									changed = true;
									break;
								}
							}
							if (changed) {
								MessagesStorage.getInstance()
								.updateChatInfo(info.chat_id,
										info, true);
								NotificationCenter.getInstance()
								.postNotificationName(
										chatInfoDidLoaded,
										info.chat_id, info);
							}
						}
					}
				});

				if (user.id != UserConfig.clientUserId) {
					final ArrayList<TLRPC.Message> messages = new ArrayList<TLRPC.Message>();
					messages.add(res.message);
					MessagesStorage.getInstance().putMessages(messages,
							true, true);
				}
			}
		});

	}

	// muzf 锟睫革拷群锟斤拷锟斤拷锟斤拷
	public void changeChatTitle(int chat_id, String title) {

		TLRPC.TL_messages_editChatTitle req = new TLRPC.TL_messages_editChatTitle();
		req.chat_id = chat_id;
		req.title = title;
		ConnectionsManager.getInstance().performRpc(req,
				new RPCRequest.RPCRequestDelegate() {
			@Override
			public void run(TLObject response, TLRPC.TL_error error) {
				if (error != null) {
					return;
				}
				final TLRPC.messages_StatedMessage res = (TLRPC.messages_StatedMessage) response;
				MessagesStorage.getInstance().putUsersAndChats(
						res.users, res.chats, true, true);

				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						for (TLRPC.User user : res.users) {
							users.put(user.id, user);
							// add by xueqiang
							usersSDK.put(user.identification, user);
							if (user.id == UserConfig.clientUserId) {
								user.sessionid = UserConfig.currentUser.sessionid;
								UserConfig.currentUser = user;
							}
						}
						for (TLRPC.Chat chat : res.chats) {
							chats.put(chat.id, chat);
						}
						final ArrayList<MessageObject> messagesObj = new ArrayList<MessageObject>();
						messagesObj.add(new MessageObject(res.message,
								users));
						TLRPC.Chat chat = res.chats.get(0);
						chats.put(chat.id, chat);
						chat.hasTitle = 0;
						updateInterfaceWithMessages(-chat.id,
								messagesObj);

						NotificationCenter
						.getInstance()
						.postNotificationName(dialogsNeedReload);
						NotificationCenter.getInstance()
						.postNotificationName(updateInterfaces,
								UPDATE_MASK_CHAT_NAME);
					}
				});

				final ArrayList<TLRPC.Message> messages = new ArrayList<TLRPC.Message>();
				messages.add(res.message);
				MessagesStorage.getInstance().putMessages(messages,
						true, true);
				if (MessagesStorage.lastSeqValue + 1 == res.seq) {
					MessagesStorage.lastSeqValue = res.seq;
					MessagesStorage.lastPtsValue = res.pts;
					MessagesStorage.getInstance().saveDiffParams(
							MessagesStorage.lastSeqValue,
							MessagesStorage.lastPtsValue,
							MessagesStorage.lastDateValue,
							MessagesStorage.lastQtsValue);
				} else if (MessagesStorage.lastSeqValue != res.seq) {

					if (gettingDifference
							|| updatesStartWaitTime == 0
							|| updatesStartWaitTime != 0
							&& updatesStartWaitTime + 1500 > System
							.currentTimeMillis()) {
						if (updatesStartWaitTime == 0) {
							updatesStartWaitTime = System
									.currentTimeMillis();
						}

						UserActionUpdates updates = new UserActionUpdates();
						updates.seq = res.seq;
						updatesQueue.add(updates);
					} else {
						getDifference();
					}
				}
			}
		});

	}

	// muzf 锟睫革拷群锟斤拷头锟斤拷 锟斤拷锟杰诧拷使锟斤拷
	public void changeChatAvatar(int chat_id,
			final TLRPC.InputFile uploadedAvatar, final TLRPC.PhotoSize small,
			final TLRPC.PhotoSize big) {
		// uploadedAvatar.http_path_img锟斤拷锟斤拷锟斤拷拇锟酵硷拷锟斤拷url锟斤拷址
		TLRPC.TL_messages_editChatPhoto req2 = new TLRPC.TL_messages_editChatPhoto();
		req2.chat_id = chat_id;
		if (uploadedAvatar != null) {
			req2.photo = new TLRPC.TL_inputChatUploadedPhoto();
			req2.photo.file = uploadedAvatar;
			req2.photo.crop = new TLRPC.TL_inputPhotoCropAuto();
		} else {
			req2.photo = new TLRPC.TL_inputChatPhotoEmpty();
		}
		ConnectionsManager.getInstance().performRpc(req2,
				new RPCRequest.RPCRequestDelegate() {
			@Override
			public void run(TLObject response, TLRPC.TL_error error) {
				if (error != null) {
					return;
				}
				final TLRPC.messages_StatedMessage res = (TLRPC.messages_StatedMessage) response;

				// xueqiang add for show group avatar begin
				if (uploadedAvatar != null) {
					TLRPC.TL_chatPhoto photo = new TLRPC.TL_chatPhoto();
					photo.photo_big = small.location;
					photo.photo_small = big.location;
					res.chats.get(0).photo = photo;

					res.message.action.photo.user_id = UserConfig.clientUserId;
					res.message.action.photo.date = ConnectionsManager
							.getInstance().getCurrentTime();
					res.message.action.photo.caption = "";
					res.message.action.photo.geo = new TLRPC.TL_geoPointEmpty();

					small.type = "s";
					big.type = "x";
					res.message.action.photo.sizes.add(small);
					res.message.action.photo.sizes.add(big);
				} else {
					res.chats.get(0).photo = new TLRPC.TL_chatPhotoEmpty();
					// res.message.action.photo.user_id =
					// UserConfig.clientUserId;
					// res.message.action.photo.date =
					// ConnectionsManager.getInstance().getCurrentTime();
					// res.message.action.photo.caption = "";
					// res.message.action.photo.geo = new
					// TLRPC.TL_geoPointEmpty();
					// res.message.action.photo.sizes.add(new
					// TLRPC.TL_photoSizeEmpty());
					// res.message.action.photo.sizes.add(new
					// TLRPC.TL_photoSizeEmpty());

				}
				// xueqiang add for show group avatar end

				MessagesStorage.getInstance().putUsersAndChats(
						res.users, res.chats, true, true);

				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						for (TLRPC.User user : res.users) {
							users.put(user.id, user);
							// add by xueqiang
							usersSDK.put(user.identification, user);
							if (user.id == UserConfig.clientUserId) {
								user.sessionid = UserConfig.currentUser.sessionid;
								UserConfig.currentUser = user;
							}
						}
						for (TLRPC.Chat chat : res.chats) {
							chats.put(chat.id, chat);
						}
						final ArrayList<MessageObject> messagesObj = new ArrayList<MessageObject>();
						messagesObj.add(new MessageObject(res.message,
								users));
						TLRPC.Chat chat = res.chats.get(0);
						chats.put(chat.id, chat);
						updateInterfaceWithMessages(-chat.id,
								messagesObj);
						NotificationCenter
						.getInstance()
						.postNotificationName(dialogsNeedReload);
						NotificationCenter.getInstance()
						.postNotificationName(updateInterfaces,
								UPDATE_MASK_CHAT_AVATAR);
					}
				});

				final ArrayList<TLRPC.Message> messages = new ArrayList<TLRPC.Message>();
				messages.add(res.message);
				MessagesStorage.getInstance().putMessages(messages,
						true, true);
			}
		});

	}

	public void unregistedPush() {
		if (UserConfig.registeredForPush && UserConfig.pushString.length() == 0) {
			TLRPC.TL_account_unregisterDevice req = new TLRPC.TL_account_unregisterDevice();
			req.token = UserConfig.pushString;
			req.token_type = 2;
			ConnectionsManager.getInstance().performRpc(req,
					new RPCRequest.RPCRequestDelegate() {
				@Override
				public void run(TLObject response, TLRPC.TL_error error) {

				}
			}, null, true, RPCRequest.RPCRequestClassGeneric);
		}

		// sam
		MessagesController.getInstance().cleanUp();
		MessagesStorage.getInstance().cleanUp();
		TLRPC.TL_auth_logOut req2 = new TLRPC.TL_auth_logOut();
		ConnectionsManager.getInstance().performRpc(req2, null, null, false,
				RPCRequest.RPCRequestClassGeneric);
		// TLRPC.TL_auth_logOut req2 = new TLRPC.TL_auth_logOut();
		// ConnectionsManager.getInstance().performRpc(req2, new
		// RPCRequest.RPCRequestDelegate() {
		// @Override
		// public void run(TLObject response, TLRPC.TL_error error) {
		//
		// }
		// }, null, true, RPCRequest.RPCRequestClassGeneric);
	}

	public void registerForPush(final String regid) {
		// sam 取锟斤拷锟斤拷锟斤拷注锟斤拷
		// if (regid == null || regid.length() == 0 || registeringForPush ||
		// UserConfig.clientUserId == 0) {
		// return;
		// }
		// if (UserConfig.registeredForPush &&
		// regid.equals(UserConfig.pushString)) {
		// return;
		// }
		// registeringForPush = true;
		// TLRPC.TL_account_registerDevice req = new
		// TLRPC.TL_account_registerDevice();
		// req.token_type = 2;
		// req.token = regid;
		// req.app_sandbox = false;
		// try {
		// req.lang_code = Locale.getDefault().getCountry();
		// req.device_model = Build.MANUFACTURER + Build.MODEL;
		// if (req.device_model == null) {
		// req.device_model = "Android unknown";
		// }
		// req.system_version = "SDK " + Build.VERSION.SDK_INT;
		// PackageInfo pInfo =
		// ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(),
		// 0);
		// req.app_version = pInfo.versionName;
		// if (req.app_version == null) {
		// req.app_version = "App version unknown";
		// }
		//
		// } catch (Exception e) {
		// FileLog.e("emm", e);
		// req.lang_code = "en";
		// req.device_model = "Android unknown";
		// req.system_version = "SDK " + Build.VERSION.SDK_INT;
		// req.app_version = "App version unknown";
		// }
		//
		// if (req.lang_code == null || req.lang_code.length() == 0) {
		// req.lang_code = "en";
		// }
		// if (req.device_model == null || req.device_model.length() == 0) {
		// req.device_model = "Android unknown";
		// }
		// if (req.app_version == null || req.app_version.length() == 0) {
		// req.app_version = "App version unknown";
		// }
		// if (req.system_version == null || req.system_version.length() == 0) {
		// req.system_version = "SDK Unknown";
		// }
		//
		// if (req.app_version != null) {
		// ConnectionsManager.getInstance().performRpc(req, new
		// RPCRequest.RPCRequestDelegate() {
		// @Override
		// public void run(TLObject response, TLRPC.TL_error error) {
		// if (error == null) {
		// FileLog.e("emm", "registered for push");
		// UserConfig.registeredForPush = true;
		// UserConfig.pushString = regid;
		// UserConfig.saveConfig(false);
		// }
		// Utilities.RunOnUIThread(new Runnable() {
		// @Override
		// public void run() {
		// registeringForPush = false;
		// }
		// });
		// }
		// }, null, true, RPCRequest.RPCRequestClassGeneric);
		// }
	}

	public void loadCurrentState() {
		if (updatingState) {
			return;
		}
		updatingState = true;
		TLRPC.TL_updates_getState req = new TLRPC.TL_updates_getState();
		ConnectionsManager.getInstance().performRpc(req,
				new RPCRequest.RPCRequestDelegate() {
			@Override
			public void run(TLObject response, TLRPC.TL_error error) {
				updatingState = false;
				if (error == null) {
					TLRPC.TL_updates_state res = (TLRPC.TL_updates_state) response;
					MessagesStorage.lastDateValue = res.date;
					MessagesStorage.lastPtsValue = res.pts;
					MessagesStorage.lastSeqValue = res.seq;
					MessagesStorage.lastQtsValue = res.qts;
					MessagesStorage.getInstance().saveDiffParams(
							MessagesStorage.lastSeqValue,
							MessagesStorage.lastPtsValue,
							MessagesStorage.lastDateValue,
							MessagesStorage.lastQtsValue);
				} else {
					if (error.code != 401) {
						loadCurrentState();
					}
				}
			}
		}, null, true, RPCRequest.RPCRequestClassGeneric);
	}

	private int getUpdateSeq(TLRPC.Updates updates) {
		if (updates instanceof TLRPC.TL_updatesCombined) {
			return updates.seq_start;
		} else {
			return updates.seq;
		}
	}

	private void processUpdatesQueue(boolean getDifference) {
		if (!updatesQueue.isEmpty()) {
			Collections.sort(updatesQueue, new Comparator<TLRPC.Updates>() {
				@Override
				public int compare(TLRPC.Updates updates, TLRPC.Updates updates2) {
					int seq1 = getUpdateSeq(updates);
					int seq2 = getUpdateSeq(updates2);
					if (seq1 == seq2) {
						return 0;
					} else if (seq1 > seq2) {
						return 1;
					}
					return -1;
				}
			});
			boolean anyProceed = false;
			for (int a = 0; a < updatesQueue.size(); a++) {
				TLRPC.Updates updates = updatesQueue.get(a);
				int seq = getUpdateSeq(updates);
				if (MessagesStorage.lastSeqValue + 1 == seq
						|| MessagesStorage.lastSeqValue == seq) {
					processUpdates(updates, true);
					anyProceed = true;
					updatesQueue.remove(a);
					a--;
				} else if (MessagesStorage.lastSeqValue < seq) {
					if (updatesStartWaitTime != 0
							&& (anyProceed || updatesStartWaitTime + 1500 > System
									.currentTimeMillis())) {
						if (anyProceed) {
							updatesStartWaitTime = System.currentTimeMillis();
						}
						return;
					} else {
						updatesStartWaitTime = 0;
						updatesQueue.clear();
						getDifference();
						return;
					}
				} else {
					updatesQueue.remove(a);
					a--;
				}
			}
			updatesQueue.clear();
			updatesStartWaitTime = 0;
			if (getDifference) {
				final int stateCopy = ConnectionsManager.getInstance().connectionState;
				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						NotificationCenter.getInstance().postNotificationName(
								703, stateCopy);
					}
				});
			}
		} else {
			if (getDifference) {
				final int stateCopy = ConnectionsManager.getInstance().connectionState;
				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						NotificationCenter.getInstance().postNotificationName(
								703, stateCopy);
					}
				});
			} else {
				updatesStartWaitTime = 0;
			}
		}
	}

	public void getDifference() {
		registerForPush(UserConfig.pushString);
		if (MessagesStorage.lastDateValue == 0) {
			loadCurrentState();
			return;
		}
		if (gettingDifference) {
			return;
		}
		if (!firstGettingTask) {
			getNewDeleteTask(null);
			firstGettingTask = true;
		}
		gettingDifference = true;
		TLRPC.TL_updates_getDifference req = new TLRPC.TL_updates_getDifference();
		req.pts = MessagesStorage.lastPtsValue;
		req.date = MessagesStorage.lastDateValue;
		req.qts = MessagesStorage.lastQtsValue;
		if (ConnectionsManager.getInstance().connectionState == 0) {
			ConnectionsManager.getInstance().connectionState = 3;
			final int stateCopy = ConnectionsManager.getInstance().connectionState;
			Utilities.RunOnUIThread(new Runnable() {
				@Override
				public void run() {
					NotificationCenter.getInstance().postNotificationName(703,
							stateCopy);
				}
			});
		}
		ConnectionsManager.getInstance().performRpc(req,
				new RPCRequest.RPCRequestDelegate() {
			@Override
			public void run(TLObject response, TLRPC.TL_error error) {
				gettingDifferenceAgain = false;
				if (error == null) {
					final TLRPC.updates_Difference res = (TLRPC.updates_Difference) response;
					gettingDifferenceAgain = res instanceof TLRPC.TL_updates_differenceSlice;

					final HashMap<Integer, TLRPC.User> usersDict = new HashMap<Integer, TLRPC.User>();
					for (TLRPC.User user : res.users) {
						usersDict.put(user.id, user);
					}

					final ArrayList<Integer> readMessages = new ArrayList<Integer>();
					final ArrayList<TLRPC.TL_updateMessageID> msgUpdates = new ArrayList<TLRPC.TL_updateMessageID>();
					if (!res.other_updates.isEmpty()) {
						for (int a = 0; a < res.other_updates.size(); a++) {
							TLRPC.Update upd = res.other_updates.get(a);
							if (upd instanceof TLRPC.TL_updateMessageID) {
								msgUpdates
								.add((TLRPC.TL_updateMessageID) upd);
								res.other_updates.remove(a);
								a--;
							} else if (upd instanceof TLRPC.TL_updateReadMessages) {
								readMessages.addAll(upd.messages);
							}
						}
					}

					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() {
							for (TLRPC.User user : res.users) {
								users.put(user.id, user);
								// add by xueqiang
								usersSDK.put(user.identification, user);
								if (user.id == UserConfig.clientUserId) {
									user.sessionid = UserConfig.currentUser.sessionid;
									UserConfig.currentUser = user;
								}
							}
							for (TLRPC.Chat chat : res.chats) {
								chats.put(chat.id, chat);
							}

							if (currentPushMessage != null
									&& readMessages
									.contains(currentPushMessage.messageOwner.id)) {
								NotificationManager mNotificationManager = (NotificationManager) ApplicationLoader.applicationContext
										.getSystemService(Context.NOTIFICATION_SERVICE);
								mNotificationManager.cancel(1);
								currentPushMessage = null;
							}
						}
					});

					MessagesStorage.getInstance().storageQueue
					.postRunnable(new Runnable() {
						@Override
						public void run() {
							if (!msgUpdates.isEmpty()) {
								final HashMap<Integer, Integer> corrected = new HashMap<Integer, Integer>();
								for (TLRPC.TL_updateMessageID update : msgUpdates) {
									Integer oldId = MessagesStorage
											.getInstance()
											.updateMessageStateAndId(
													update.random_id,
													null,
													update.id,
													0, false);
									if (oldId != null) {
										corrected.put(oldId,
												update.id);
									}
								}

								if (!corrected.isEmpty()) {
									Utilities
									.RunOnUIThread(new Runnable() {
										@Override
										public void run() {
											for (HashMap.Entry<Integer, Integer> entry : corrected
													.entrySet()) {
												Integer oldId = entry
														.getKey();
												sendingMessages
												.remove(oldId);
												Integer newId = entry
														.getValue();
												NotificationCenter
												.getInstance()
												.postNotificationName(
														messageReceivedByServer,
														oldId,
														newId,
														null);
											}
										}
									});
								}
							}

							Utilities.stageQueue
							.postRunnable(new Runnable() {
								@Override
								public void run() {
									if (!res.new_messages
											.isEmpty()
											|| !res.new_encrypted_messages
											.isEmpty()) {
										final HashMap<Long, ArrayList<MessageObject>> messages = new HashMap<Long, ArrayList<MessageObject>>();
										for (TLRPC.EncryptedMessage encryptedMessage : res.new_encrypted_messages) {
											TLRPC.Message message = decryptMessage(encryptedMessage);
											if (message != null) {
												res.new_messages
												.add(message);
											}
										}

										MessageObject lastMessage = null;
										for (TLRPC.Message message : res.new_messages) {
											MessageObject obj = new MessageObject(
													message,
													usersDict);

											long dialog_id = obj.messageOwner.dialog_id;
											if (dialog_id == 0) {
												if (obj.messageOwner.to_id.chat_id != 0) {
													dialog_id = -obj.messageOwner.to_id.chat_id;
												} else {
													dialog_id = obj.messageOwner.to_id.user_id;
												}
											}

											if (!(res instanceof TLRPC.TL_updates_differenceSlice)) {
												if ((dialog_id != openned_dialog_id || ApplicationLoader.lastPauseTime != 0)
														&& !obj.messageOwner.out
														&& obj.messageOwner.unread
														&& (lastMessage == null || lastMessage.messageOwner.date < obj.messageOwner.date)) {
													if (!readMessages
															.contains(obj.messageOwner.id)) {
														lastMessage = obj;
													}
												}
											}
											long uid;
											if (message.dialog_id != 0) {
												uid = message.dialog_id;
											} else {
												if (message.to_id.chat_id != 0) {
													uid = -message.to_id.chat_id;
												} else {
													if (message.to_id.user_id == UserConfig.clientUserId) {
														message.to_id.user_id = message.from_id;
													}
													uid = message.to_id.user_id;
												}
											}
											ArrayList<MessageObject> arr = messages
													.get(uid);
											if (arr == null) {
												arr = new ArrayList<MessageObject>();
												messages.put(
														uid,
														arr);
											}
											arr.add(obj);
										}

										processPendingEncMessages();

										final MessageObject object = lastMessage;
										Utilities
										.RunOnUIThread(new Runnable() {
											@Override
											public void run() {
												for (HashMap.Entry<Long, ArrayList<MessageObject>> pair : messages
														.entrySet()) {
													Long key = pair
															.getKey();
													ArrayList<MessageObject> value = pair
															.getValue();
													updateInterfaceWithMessages(
															key,
															value);
												}

												NotificationCenter
												.getInstance()
												.postNotificationName(
														dialogsNeedReload);
												if (object != null) {
													showInAppNotification(object);
												}
											}
										});
										MessagesStorage
										.getInstance().storageQueue
										.postRunnable(new Runnable() {
											@Override
											public void run() {
												MessagesStorage
												.getInstance()
												.startTransaction(
														false);
												MessagesStorage
												.getInstance()
												.putMessages(
														res.new_messages,
														false,
														false);
												MessagesStorage
												.getInstance()
												.putUsersAndChats(
														res.users,
														res.chats,
														false,
														false);
												MessagesStorage
												.getInstance()
												.commitTransaction(
														false);
											}
										});
									}

									if (res != null
											&& !res.other_updates
											.isEmpty()) {
										processUpdateArray(
												res.other_updates,
												res.users,
												res.chats);
									}

									gettingDifference = false;
									if (res instanceof TLRPC.TL_updates_difference) {
										MessagesStorage.lastSeqValue = res.state.seq;
										MessagesStorage.lastDateValue = res.state.date;
										MessagesStorage.lastPtsValue = res.state.pts;
										MessagesStorage.lastQtsValue = res.state.qts;
										ConnectionsManager
										.getInstance().connectionState = 0;
										processUpdatesQueue(true);
									} else if (res instanceof TLRPC.TL_updates_differenceSlice) {
										MessagesStorage.lastSeqValue = res.intermediate_state.seq;
										MessagesStorage.lastDateValue = res.intermediate_state.date;
										MessagesStorage.lastPtsValue = res.intermediate_state.pts;
										MessagesStorage.lastQtsValue = res.intermediate_state.qts;
										gettingDifferenceAgain = true;
										getDifference();
									} else if (res instanceof TLRPC.TL_updates_differenceEmpty) {
										MessagesStorage.lastSeqValue = res.seq;
										MessagesStorage.lastDateValue = res.date;
										ConnectionsManager
										.getInstance().connectionState = 0;
										processUpdatesQueue(true);
									}
									MessagesStorage
									.getInstance()
									.saveDiffParams(
											MessagesStorage.lastSeqValue,
											MessagesStorage.lastPtsValue,
											MessagesStorage.lastDateValue,
											MessagesStorage.lastQtsValue);
								}
							});
						}
					});
				} else {
					gettingDifference = false;
					getDifference();
				}
			}
		}, null, true, RPCRequest.RPCRequestClassGeneric);
	}

	public void processUpdates(final TLRPC.Updates updates, boolean fromQueue) {
		boolean needGetDiff = false;
		boolean needReceivedQueue = false;
		boolean addedToQueue = false;
		if (updates instanceof TLRPC.TL_updateShort) {
			ArrayList<TLRPC.Update> arr = new ArrayList<TLRPC.Update>();
			arr.add(updates.update);
			processUpdateArray(arr, null, null);
		} else if (updates instanceof TLRPC.TL_updateShortChatMessage) {
			boolean missingData = chats.get(updates.chat_id) == null
					|| users.get(updates.from_id) == null;
			// sam
			// if (MessagesStorage.lastSeqValue + 1 == updates.seq &&
			// !missingData) {
			if (!missingData) {
				TLRPC.TL_message message = new TLRPC.TL_message();
				message.from_id = updates.from_id;
				message.id = updates.id;
				message.to_id = new TLRPC.TL_peerChat();
				message.to_id.chat_id = updates.chat_id;
				message.message = updates.message;
				message.date = updates.date;
				message.unread = true;
				message.media = new TLRPC.TL_messageMediaEmpty();
				MessagesStorage.lastSeqValue = updates.seq;
				MessagesStorage.lastPtsValue = updates.pts;
				final MessageObject obj = new MessageObject(message, null);
				final ArrayList<MessageObject> objArr = new ArrayList<MessageObject>();
				objArr.add(obj);
				ArrayList<TLRPC.Message> arr = new ArrayList<TLRPC.Message>();
				arr.add(message);
				final boolean printUpdate = updatePrintingUsersWithNewMessages(
						-updates.chat_id, objArr);
				if (printUpdate) {
					updatePrintingStrings();
				}
				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						if (printUpdate) {
							NotificationCenter.getInstance()
							.postNotificationName(updateInterfaces,
									UPDATE_MASK_USER_PRINT);
						}
						if (obj.messageOwner.from_id != UserConfig.clientUserId) {
							long dialog_id;
							if (obj.messageOwner.to_id.chat_id != 0) {
								dialog_id = -obj.messageOwner.to_id.chat_id;
							} else {
								dialog_id = obj.messageOwner.to_id.user_id;
							}
							if (dialog_id != openned_dialog_id
									|| ApplicationLoader.lastPauseTime != 0
									|| !ApplicationLoader.isScreenOn) {
								showInAppNotification(obj);
							}
						}
						updateInterfaceWithMessages(-updates.chat_id, objArr);
						NotificationCenter.getInstance().postNotificationName(
								dialogsNeedReload);
					}
				});
				MessagesStorage.getInstance().putMessages(arr, false, true);
				MessagesStorage.getInstance().putUsersAndChats(updates.users,
						updates.chats, true, true);
			}

		} else if (updates instanceof TLRPC.TL_updateShortMessage) {
			boolean missingData = users.get(updates.from_id) == null;
			if (!missingData)
			{
				MessagesStorage.getInstance().putUsersAndChats(updates.users,
						updates.chats, true, true);

				TLRPC.TL_message message = new TLRPC.TL_message();
				message.from_id = updates.from_id;
				message.id = updates.id;
				message.to_id = new TLRPC.TL_peerUser();
				message.to_id.user_id = updates.from_id;
				message.message = updates.message;
				message.date = updates.date;
				message.unread = true;
				message.media = new TLRPC.TL_messageMediaEmpty();
				MessagesStorage.lastSeqValue = updates.seq;
				MessagesStorage.lastPtsValue = updates.pts;
				MessagesStorage.lastDateValue = updates.date;
				final MessageObject obj = new MessageObject(message, null);
				final ArrayList<MessageObject> objArr = new ArrayList<MessageObject>();
				objArr.add(obj);
				ArrayList<TLRPC.Message> arr = new ArrayList<TLRPC.Message>();
				arr.add(message);
				final boolean printUpdate = updatePrintingUsersWithNewMessages(
						updates.from_id, objArr);
				if (printUpdate) {
					updatePrintingStrings();
				}
				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						if (printUpdate) {
							NotificationCenter.getInstance()
							.postNotificationName(updateInterfaces,
									UPDATE_MASK_USER_PRINT);
						}
						if (obj.messageOwner.from_id != UserConfig.clientUserId) {
							long dialog_id;
							if (obj.messageOwner.to_id.chat_id != 0) {
								dialog_id = -obj.messageOwner.to_id.chat_id;
							} else {
								dialog_id = obj.messageOwner.to_id.user_id;
							}
							if (dialog_id != openned_dialog_id
									|| ApplicationLoader.lastPauseTime != 0
									|| !ApplicationLoader.isScreenOn) {
								showInAppNotification(obj);
							}
						}
						updateInterfaceWithMessages(updates.from_id, objArr);
						NotificationCenter.getInstance().postNotificationName(
								dialogsNeedReload);
					}
				});
				MessagesStorage.getInstance().putMessages(arr, false, true);
			}

		} else if (updates instanceof TLRPC.TL_updatesCombined) {
			if (MessagesStorage.lastSeqValue + 1 == updates.seq_start
					|| MessagesStorage.lastSeqValue == updates.seq_start) {
				MessagesStorage.getInstance().putUsersAndChats(updates.users,
						updates.chats, true, true);
				int lastPtsValue = MessagesStorage.lastPtsValue;
				int lastQtsValue = MessagesStorage.lastQtsValue;
				if (!processUpdateArray(updates.updates, updates.users,
						updates.chats)) {
					MessagesStorage.lastPtsValue = lastPtsValue;
					MessagesStorage.lastQtsValue = lastQtsValue;
					needGetDiff = true;
				} else {
					MessagesStorage.lastDateValue = updates.date;
					MessagesStorage.lastSeqValue = updates.seq;
					if (MessagesStorage.lastQtsValue != lastQtsValue) {
						needReceivedQueue = true;
					}
				}
			} else {
				if (gettingDifference
						|| updatesStartWaitTime == 0
						|| updatesStartWaitTime != 0
						&& updatesStartWaitTime + 1500 > System
						.currentTimeMillis()) {
					if (updatesStartWaitTime == 0) {
						updatesStartWaitTime = System.currentTimeMillis();
					}
					updatesQueue.add(updates);
					addedToQueue = true;
				} else {
					needGetDiff = true;
				}
			}
		} else if (updates instanceof TLRPC.TL_updates) {
			MessagesStorage.getInstance().putUsersAndChats(updates.users,
					updates.chats, true, true);
			// MessagesStorage.getInstance().putUsersAndChats(null,
			// updates.chats, true, true);
			// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷dialogneedsreload锟斤拷息锟斤拷锟铰斤拷锟芥反应锟斤拷锟斤拷锟斤拷原锟斤拷锟角斤拷锟斤拷锟斤拷锟斤拷锟斤拷息压锟诫到UITHREAD去执锟叫碉拷锟铰斤拷锟斤拷thread
			// 锟斤拷锟斤拷锟斤拷锟斤拷未锟斤拷锟斤拷锟斤拷只锟斤拷锟斤拷一锟斤拷锟斤拷息统一锟斤拷锟铰ｏ拷锟斤拷锟斤拷锟斤拷兀锟�
			processUpdateArray(updates.updates, updates.users, updates.chats);
		} else if (updates instanceof TLRPC.TL_updatesTooLong) {
			needGetDiff = true;
		} else if (updates instanceof UserActionUpdates) {
			MessagesStorage.lastSeqValue = updates.seq;
		}
		if (needGetDiff && !fromQueue) {
			getDifference();
		} else if (!fromQueue && !updatesQueue.isEmpty()) {
			processUpdatesQueue(false);
		}
		if (needReceivedQueue) {
			TLRPC.TL_messages_receivedQueue req = new TLRPC.TL_messages_receivedQueue();
			req.max_qts = MessagesStorage.lastQtsValue;
			ConnectionsManager.getInstance().performRpc(req,
					new RPCRequest.RPCRequestDelegate() {
				@Override
				public void run(TLObject response, TLRPC.TL_error error) {

				}
			}, null, true, RPCRequest.RPCRequestClassGeneric);
		}
		MessagesStorage.getInstance().saveDiffParams(
				MessagesStorage.lastSeqValue, MessagesStorage.lastPtsValue,
				MessagesStorage.lastDateValue, MessagesStorage.lastQtsValue);
	}

	public boolean processUpdateArray(ArrayList<TLRPC.Update> updates,
			final ArrayList<TLRPC.User> usersArr,
			final ArrayList<TLRPC.Chat> chatsArr) {
		if (updates.isEmpty()) {
			return true;
		}
		long currentTime = System.currentTimeMillis();

		final HashMap<Long, ArrayList<MessageObject>> messages = new HashMap<Long, ArrayList<MessageObject>>();
		final ArrayList<TLRPC.Message> messagesArr = new ArrayList<TLRPC.Message>();
		final ArrayList<Integer> markAsReadMessages = new ArrayList<Integer>();
		final HashMap<Integer, Integer> markAsReadEncrypted = new HashMap<Integer, Integer>();
		final ArrayList<Integer> deletedMessages = new ArrayList<Integer>();
		final ArrayList<Long> printChanges = new ArrayList<Long>();
		final ArrayList<TLRPC.ChatParticipants> chatInfoToUpdate = new ArrayList<TLRPC.ChatParticipants>();
		final ArrayList<TLRPC.Update> updatesOnMainThread = new ArrayList<TLRPC.Update>();
		final ArrayList<TLRPC.TL_updateEncryptedMessagesRead> tasks = new ArrayList<TLRPC.TL_updateEncryptedMessagesRead>();
		final ArrayList<Integer> contactsIds = new ArrayList<Integer>();
		MessageObject lastMessage = null;

		boolean checkForUsers = true;
		ConcurrentHashMap<Integer, TLRPC.User> usersDict;
		ConcurrentHashMap<Integer, TLRPC.Chat> chatsDict;
		if (usersArr != null) {
			usersDict = new ConcurrentHashMap<Integer, TLRPC.User>();
			for (TLRPC.User user : usersArr) {
				if (user != null)
					usersDict.put(user.id, user);
				else
					FileLog.d("emm", "user is null");
			}
		} else {
			checkForUsers = false;
			usersDict = users;
		}
		if (chatsArr != null) {
			chatsDict = new ConcurrentHashMap<Integer, TLRPC.Chat>();
			for (TLRPC.Chat chat : chatsArr) {
				chatsDict.put(chat.id, chat);
			}
		} else {
			checkForUsers = false;
			chatsDict = chats;
		}

		if (usersArr != null || chatsArr != null) {
			Utilities.RunOnUIThread(new Runnable() {
				@Override
				public void run() {
					if (usersArr != null) {
						for (TLRPC.User user : usersArr) {
							if (user != null) {
								users.put(user.id, user);
								// add by xueqiang
								usersSDK.put(user.identification, user);
								if (user.id == UserConfig.clientUserId) {
									user.sessionid = UserConfig.currentUser.sessionid;
									UserConfig.currentUser = user;
								}
							} else
								FileLog.d("emm", "user is null");
						}
					}
					if (chatsArr != null) {
						for (TLRPC.Chat chat : chatsArr) {
							chats.put(chat.id, chat);
						}
					}
				}
			});
		}

		int interfaceUpdateMask = 0;

		for (TLRPC.Update update : updates) {
			if (update instanceof TLRPC.TL_updateNewMessage) {
				TLRPC.TL_updateNewMessage upd = (TLRPC.TL_updateNewMessage) update;
				if (checkForUsers) {
					if (usersDict.get(upd.message.from_id) == null
							&& users.get(upd.message.from_id) == null
							|| upd.message.to_id.chat_id != 0
							&& chatsDict.get(upd.message.to_id.chat_id) == null
							&& chats.get(upd.message.to_id.chat_id) == null) {
						// return false;
						continue;
					}
				}
				messagesArr.add(upd.message);
				MessageObject obj = new MessageObject(upd.message, usersDict);
				if (obj.type == 11) {// 锟斤拷头锟斤拷锟斤拷锟斤拷锟�
					interfaceUpdateMask |= UPDATE_MASK_CHAT_AVATAR;
				} else if (obj.type == 10) {
					interfaceUpdateMask |= UPDATE_MASK_CHAT_NAME;
				} else if (obj.type == 21) {

					// xueqiang add for alert,save to db
					TLRPC.AlertMedia alertMedia = obj.messageOwner.media.alert;
					MessagesStorage.getInstance().putAlert(alertMedia, true);

				}
				long uid;
				if (upd.message.to_id.chat_id != 0) {
					uid = -upd.message.to_id.chat_id;
				} else {
					if (upd.message.to_id.user_id == UserConfig.clientUserId) {
						upd.message.to_id.user_id = upd.message.from_id;
					}
					uid = upd.message.to_id.user_id;
				}
				ArrayList<MessageObject> arr = messages.get(uid);
				if (arr == null) {
					arr = new ArrayList<MessageObject>();
					messages.put(uid, arr);
				}
				arr.add(obj);
				MessagesStorage.lastPtsValue = update.pts;
				if (upd.message.from_id != UserConfig.clientUserId
						&& upd.message.to_id != null) {
					if (uid != openned_dialog_id
							|| ApplicationLoader.lastPauseTime != 0) {
						if (upd.message.unread)// xueqiang add
							// 锟斤拷锟斤拷锟矫伙拷锟斤拷录锟斤拷锟斤拷锟斤拷姹撅拷锟轿�-1锟斤拷锟斤拷锟斤拷要锟斤拷锟街伙拷锟斤拷锟芥弹锟斤拷锟斤拷示锟斤拷息锟斤拷,锟斤拷锟斤拷锟絬nread=true锟斤拷锟斤拷锟斤拷要锟斤拷示锟斤拷
							lastMessage = obj;
					}
				}
			} else if (update instanceof TLRPC.TL_updateMessageID) {
				// can't be here
			} else if (update instanceof TLRPC.TL_updateReadMessages) {
				markAsReadMessages.addAll(update.messages);
				MessagesStorage.lastPtsValue = update.pts;
			} else if (update instanceof TLRPC.TL_updateDeleteMessages) {
				deletedMessages.addAll(update.messages);
				MessagesStorage.lastPtsValue = update.pts;
			} else if (update instanceof TLRPC.TL_updateRestoreMessages) {
				MessagesStorage.lastPtsValue = update.pts;
			} else if (update instanceof TLRPC.TL_updateUserTyping
					|| update instanceof TLRPC.TL_updateChatUserTyping) {
				if (update.user_id != UserConfig.clientUserId) {
					long uid = -update.chat_id;
					if (uid == 0) {
						uid = update.user_id;
					}
					ArrayList<PrintingUser> arr = printingUsers.get(uid);
					if (arr == null) {
						arr = new ArrayList<PrintingUser>();
						printingUsers.put(uid, arr);
					}
					boolean exist = false;
					for (PrintingUser u : arr) {
						if (u.userId == update.user_id) {
							exist = true;
							u.lastTime = currentTime;
							break;
						}
					}
					if (!exist) {
						PrintingUser newUser = new PrintingUser();
						newUser.userId = update.user_id;
						newUser.lastTime = currentTime;
						arr.add(newUser);
						if (!printChanges.contains(uid)) {
							printChanges.add(uid);
						}
					}
				}
			} else if (update instanceof TLRPC.TL_updateChatParticipants) {
				interfaceUpdateMask |= UPDATE_MASK_CHAT_MEMBERS;
				chatInfoToUpdate.add(update.participants);
			} else if (update instanceof TLRPC.TL_updateUserStatus) {
				interfaceUpdateMask |= UPDATE_MASK_STATUS;
				updatesOnMainThread.add(update);
			} else if (update instanceof TLRPC.TL_updateUserName) {
				interfaceUpdateMask |= UPDATE_MASK_NAME;
				updatesOnMainThread.add(update);
			} else if (update instanceof TLRPC.TL_updateUserPhoto) {
				interfaceUpdateMask |= UPDATE_MASK_AVATAR;
				MessagesStorage.getInstance().clearUserPhotos(update.user_id);
				/*
				 * if (!(update.photo instanceof
				 * TLRPC.TL_userProfilePhotoEmpty)) { DEPRECATED if
				 * (usersDict.containsKey(update.user_id)) {
				 * TLRPC.TL_messageService newMessage = new
				 * TLRPC.TL_messageService(); newMessage.action = new
				 * TLRPC.TL_messageActionUserUpdatedPhoto();
				 * newMessage.action.newUserPhoto = update.photo;
				 * newMessage.local_id = newMessage.id =
				 * UserConfig.getNewMessageId(); UserConfig.saveConfig(false);
				 * newMessage.unread = true; newMessage.date = update.date;
				 * newMessage.from_id = update.user_id; newMessage.to_id = new
				 * TLRPC.TL_peerUser(); newMessage.to_id.user_id =
				 * UserConfig.clientUserId; newMessage.out = false;
				 * newMessage.dialog_id = update.user_id;
				 * 
				 * messagesArr.add(newMessage); MessageObject obj = new
				 * MessageObject(newMessage, usersDict);
				 * ArrayList<MessageObject> arr =
				 * messages.get(newMessage.dialog_id); if (arr == null) { arr =
				 * new ArrayList<MessageObject>();
				 * messages.put(newMessage.dialog_id, arr); } arr.add(obj); if
				 * (newMessage.from_id != UserConfig.clientUserId &&
				 * newMessage.to_id != null) { if (newMessage.dialog_id !=
				 * openned_dialog_id || ApplicationLoader.lastPauseTime != 0) {
				 * lastMessage = obj; } } } }
				 */

				updatesOnMainThread.add(update);
			} else if (update instanceof TLRPC.TL_updateContactRegistered) {
				if (enableJoined && usersDict.containsKey(update.user_id)) {
					TLRPC.TL_messageService newMessage = new TLRPC.TL_messageService();
					newMessage.action = new TLRPC.TL_messageActionUserJoined();
					newMessage.local_id = newMessage.id = UserConfig
							.getNewMessageId();
					UserConfig.saveConfig(false);
					newMessage.unread = true;
					newMessage.date = update.date;
					newMessage.from_id = update.user_id;
					newMessage.to_id = new TLRPC.TL_peerUser();
					newMessage.to_id.user_id = UserConfig.clientUserId;
					newMessage.out = false;
					newMessage.dialog_id = update.user_id;

					messagesArr.add(newMessage);
					MessageObject obj = new MessageObject(newMessage, usersDict);
					ArrayList<MessageObject> arr = messages
							.get(newMessage.dialog_id);
					if (arr == null) {
						arr = new ArrayList<MessageObject>();
						messages.put(newMessage.dialog_id, arr);
					}
					arr.add(obj);
					if (newMessage.from_id != UserConfig.clientUserId
							&& newMessage.to_id != null) {
						if (newMessage.dialog_id != openned_dialog_id
								|| ApplicationLoader.lastPauseTime != 0) {
							lastMessage = obj;
						}
					}
				}
				// if (!contactsIds.contains(update.user_id)) {
				// contactsIds.add(update.user_id);
				// }
			} else if (update instanceof TLRPC.TL_updateContactLink) {
				if (update.my_link instanceof TLRPC.TL_contacts_myLinkContact
						|| update.my_link instanceof TLRPC.TL_contacts_myLinkRequested
						&& update.my_link.contact) {
					int idx = contactsIds.indexOf(-update.user_id);
					if (idx != -1) {
						contactsIds.remove(idx);
					}
					if (!contactsIds.contains(update.user_id)) {
						contactsIds.add(update.user_id);
					}
				} else {
					int idx = contactsIds.indexOf(update.user_id);
					if (idx != -1) {
						contactsIds.remove(idx);
					}
					if (!contactsIds.contains(update.user_id)) {
						contactsIds.add(-update.user_id);
					}
				}
			} else if (update instanceof TLRPC.TL_updateActivation) {
				// DEPRECATED
			} else if (update instanceof TLRPC.TL_updateNewAuthorization) {
				TLRPC.TL_messageService newMessage = new TLRPC.TL_messageService();
				newMessage.action = new TLRPC.TL_messageActionLoginUnknownLocation();
				newMessage.action.title = update.device;
				newMessage.action.address = update.location;
				newMessage.local_id = newMessage.id = UserConfig
						.getNewMessageId();
				UserConfig.saveConfig(false);
				newMessage.unread = true;
				newMessage.date = update.date;
				newMessage.from_id = 333000;
				newMessage.to_id = new TLRPC.TL_peerUser();
				newMessage.to_id.user_id = UserConfig.clientUserId;
				newMessage.out = false;
				newMessage.dialog_id = 333000;

				messagesArr.add(newMessage);
				MessageObject obj = new MessageObject(newMessage, usersDict);
				ArrayList<MessageObject> arr = messages
						.get(newMessage.dialog_id);
				if (arr == null) {
					arr = new ArrayList<MessageObject>();
					messages.put(newMessage.dialog_id, arr);
				}
				arr.add(obj);
				if (newMessage.from_id != UserConfig.clientUserId
						&& newMessage.to_id != null) {
					if (newMessage.dialog_id != openned_dialog_id
							|| ApplicationLoader.lastPauseTime != 0) {
						lastMessage = obj;
					}
				}
			} else if (update instanceof TLRPC.TL_updateNewGeoChatMessage) {
				// DEPRECATED
			} else if (update instanceof TLRPC.TL_updateNewEncryptedMessage) {
				MessagesStorage.lastQtsValue = update.qts;
				TLRPC.Message message = decryptMessage(((TLRPC.TL_updateNewEncryptedMessage) update).message);
				if (message != null) {
					int cid = ((TLRPC.TL_updateNewEncryptedMessage) update).message.chat_id;
					messagesArr.add(message);
					MessageObject obj = new MessageObject(message, usersDict);
					long uid = ((long) cid) << 32;
					ArrayList<MessageObject> arr = messages.get(uid);
					if (arr == null) {
						arr = new ArrayList<MessageObject>();
						messages.put(uid, arr);
					}
					arr.add(obj);
					if (message.from_id != UserConfig.clientUserId
							&& message.to_id != null) {
						if (uid != openned_dialog_id
								|| ApplicationLoader.lastPauseTime != 0) {
							lastMessage = obj;
						}
					}
				}
			} else if (update instanceof TLRPC.TL_updateEncryptedChatTyping) {
				long uid = ((long) update.chat_id) << 32;
				ArrayList<PrintingUser> arr = printingUsers.get(uid);
				if (arr == null) {
					arr = new ArrayList<PrintingUser>();
					printingUsers.put(uid, arr);
				}
				boolean exist = false;
				for (PrintingUser u : arr) {
					if (u.userId == update.user_id) {
						exist = true;
						u.lastTime = currentTime;
						break;
					}
				}
				if (!exist) {
					PrintingUser newUser = new PrintingUser();
					newUser.userId = update.user_id;
					newUser.lastTime = currentTime;
					arr.add(newUser);
					if (!printChanges.contains(uid)) {
						printChanges.add(uid);
					}
				}
			} else if (update instanceof TLRPC.TL_updateEncryptedMessagesRead) {
				markAsReadEncrypted.put(update.chat_id,
						Math.max(update.max_date, update.date));
				tasks.add((TLRPC.TL_updateEncryptedMessagesRead) update);
			} else if (update instanceof TLRPC.TL_updateChatParticipantAdd) {
				MessagesStorage.getInstance().updateChatInfo(update.chat_id,
						update.user_id, false, update.inviter_id,
						update.version);
			} else if (update instanceof TLRPC.TL_updateChatParticipantDelete) {
				MessagesStorage.getInstance().updateChatInfo(update.chat_id,
						update.user_id, true, 0, update.version);
			} else if (update instanceof TLRPC.TL_updateDcOptions) {
				ConnectionsManager.getInstance().updateDcSettings(0);
			} else if (update instanceof TLRPC.TL_updateEncryption) {
				final TLRPC.EncryptedChat newChat = update.chat;
				long dialog_id = ((long) newChat.id) << 32;
				TLRPC.EncryptedChat existingChat = encryptedChats
						.get(newChat.id);
				if (existingChat == null) {
					Semaphore semaphore = new Semaphore(0);
					ArrayList<TLObject> result = new ArrayList<TLObject>();
					MessagesStorage.getInstance().getEncryptedChat(newChat.id,
							semaphore, result);
					try {
						semaphore.acquire();
					} catch (Exception e) {
						FileLog.e("emm", e);
					}
					if (result.size() == 2) {
						existingChat = (TLRPC.EncryptedChat) result.get(0);
						TLRPC.User user = (TLRPC.User) result.get(1);
						users.putIfAbsent(user.id, user);
						// add by xueqiang
						usersSDK.putIfAbsent(user.identification, user);
					}
				}

				if (newChat instanceof TLRPC.TL_encryptedChatRequested
						&& existingChat == null) {
					int user_id = newChat.participant_id;
					if (user_id == UserConfig.clientUserId) {
						user_id = newChat.admin_id;
					}
					TLRPC.User user = users.get(user_id);
					if (user == null) {
						user = usersDict.get(user_id);
					}
					newChat.user_id = user_id;
					final TLRPC.TL_dialog dialog = new TLRPC.TL_dialog();
					dialog.id = dialog_id;
					dialog.unread_count = 0;
					dialog.top_message = 0;
					dialog.last_message_date = update.date;

					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() {
							dialogs_dict.put(dialog.id, dialog);
							dialogs.add(dialog);
							dialogsServerOnly.clear();
							encryptedChats.put(newChat.id, newChat);

							sortDialogs();
							// Collections.sort(dialogs, new
							// Comparator<TLRPC.TL_dialog>() {
							// @Override
							// public int compare(TLRPC.TL_dialog tl_dialog,
							// TLRPC.TL_dialog tl_dialog2) {
							// if (tl_dialog.last_message_date ==
							// tl_dialog2.last_message_date) {
							// return 0;
							// } else if (tl_dialog.last_message_date <
							// tl_dialog2.last_message_date) {
							// return 1;
							// } else {
							// return -1;
							// }
							// }
							// });
							for (TLRPC.TL_dialog d : dialogs) {
								if ((int) d.id != 0) {
									dialogsServerOnly.add(d);
								}
							}
							NotificationCenter.getInstance()
							.postNotificationName(dialogsNeedReload);
						}
					});
					MessagesStorage.getInstance().putEncryptedChat(newChat,
							user, dialog);
					acceptSecretChat(newChat);
				} else if (newChat instanceof TLRPC.TL_encryptedChat) {
					if (existingChat != null
							&& existingChat instanceof TLRPC.TL_encryptedChatWaiting
							&& (existingChat.auth_key == null || existingChat.auth_key.length == 1)) {
						newChat.a_or_b = existingChat.a_or_b;
						newChat.user_id = existingChat.user_id;
						processAcceptedSecretChat(newChat);
					} else if (existingChat == null && startingSecretChat) {
						delayedEncryptedChatUpdates.add(update);
					}
				} else {
					final TLRPC.EncryptedChat exist = existingChat;
					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() {
							if (exist != null) {
								newChat.user_id = exist.user_id;
								newChat.auth_key = exist.auth_key;
								newChat.ttl = exist.ttl;
								encryptedChats.put(newChat.id, newChat);
							}
							MessagesStorage.getInstance().updateEncryptedChat(
									newChat);
							NotificationCenter.getInstance()
							.postNotificationName(encryptedChatUpdated,
									newChat);
						}
					});
				}
			}
		}
		if (!messages.isEmpty()) {
			for (HashMap.Entry<Long, ArrayList<MessageObject>> pair : messages
					.entrySet()) {
				Long key = pair.getKey();
				ArrayList<MessageObject> value = pair.getValue();
				boolean printChanged = updatePrintingUsersWithNewMessages(key,
						value);
				if (printChanged && !printChanges.contains(key)) {
					printChanges.add(key);
				}
			}
		}

		if (!printChanges.isEmpty()) {
			updatePrintingStrings();
		}

		final MessageObject lastMessageArg = lastMessage;
		final int interfaceUpdateMaskFinal = interfaceUpdateMask;

		processPendingEncMessages();

		if (!contactsIds.isEmpty()) {
			ContactsController.getInstance().processContactsUpdates(
					contactsIds, usersDict);
		}

		if (!messagesArr.isEmpty()) {
			// save message to db,xueqiang tag
			MessagesStorage.getInstance().putMessages(messagesArr, true, true);
		}

		if (!messages.isEmpty() || !markAsReadMessages.isEmpty()
				|| !deletedMessages.isEmpty() || !printChanges.isEmpty()
				|| !chatInfoToUpdate.isEmpty()
				|| !updatesOnMainThread.isEmpty()
				|| !markAsReadEncrypted.isEmpty() || !contactsIds.isEmpty()) {
			Utilities.RunOnUIThread(new Runnable() {
				@Override
				public void run() {
					int updateMask = interfaceUpdateMaskFinal;

					boolean avatarsUpdate = false;
					if (!updatesOnMainThread.isEmpty()) {
						ArrayList<TLRPC.User> dbUsers = new ArrayList<TLRPC.User>();
						ArrayList<TLRPC.User> dbUsersStatus = new ArrayList<TLRPC.User>();
						for (TLRPC.Update update : updatesOnMainThread) {
							TLRPC.User toDbUser = new TLRPC.User();
							toDbUser.id = update.user_id;
							TLRPC.User currentUser = users.get(update.user_id);
							if (update instanceof TLRPC.TL_updateUserStatus) {
								if (currentUser != null) {
									currentUser.id = update.user_id;
									currentUser.status = update.status;
								}
								toDbUser.status = update.status;
								dbUsersStatus.add(toDbUser);
							} else if (update instanceof TLRPC.TL_updateUserName) {
								if (currentUser != null) {
									currentUser.first_name = update.first_name;
									currentUser.last_name = update.last_name;
								}
								toDbUser.first_name = update.first_name;
								toDbUser.last_name = update.last_name;
								dbUsers.add(toDbUser);
							} else if (update instanceof TLRPC.TL_updateUserPhoto) {
								if (currentUser != null) {
									currentUser.photo = update.photo;
								}
								avatarsUpdate = true;
								toDbUser.photo = update.photo;
								dbUsers.add(toDbUser);
							}
						}
						MessagesStorage.getInstance().updateUsers(
								dbUsersStatus, true, true, true);
						MessagesStorage.getInstance().updateUsers(dbUsers,
								false, true, true);
					}

					if (!messages.isEmpty()) {
						for (HashMap.Entry<Long, ArrayList<MessageObject>> entry : messages
								.entrySet()) {
							Long key = entry.getKey();
							ArrayList<MessageObject> value = entry.getValue();
							updateInterfaceWithMessages(key, value);
						}
						// NotificationCenter.getInstance().postNotificationName(dialogsNeedReload);
					}
					if (!markAsReadMessages.isEmpty()) {
						for (Integer id : markAsReadMessages) {
							MessageObject obj = dialogMessage.get(id);
							if (obj != null) {
								obj.messageOwner.unread = false;
								updateMask |= UPDATE_MASK_READ_DIALOG_MESSAGE;
							}
						}

						if (currentPushMessage != null
								&& markAsReadMessages
								.contains(currentPushMessage.messageOwner.id)) {
							NotificationManager mNotificationManager = (NotificationManager) ApplicationLoader.applicationContext
									.getSystemService(Context.NOTIFICATION_SERVICE);
							mNotificationManager.cancel(1);
							currentPushMessage = null;
						}
					}
					if (!markAsReadEncrypted.isEmpty()) {
						for (HashMap.Entry<Integer, Integer> entry : markAsReadEncrypted
								.entrySet()) {
							NotificationCenter.getInstance()
							.postNotificationName(
									messagesReadedEncrypted,
									entry.getKey(), entry.getValue());
							long dialog_id = (long) (entry.getKey()) << 32;
							TLRPC.TL_dialog dialog = dialogs_dict
									.get(dialog_id);
							if (dialog != null) {
								MessageObject message = dialogMessage
										.get(dialog.top_message);
								if (message != null
										&& message.messageOwner.date <= entry
										.getValue()) {
									message.messageOwner.unread = false;
									updateMask |= UPDATE_MASK_READ_DIALOG_MESSAGE;
								}
							}
						}
					}
					if (!deletedMessages.isEmpty()) {
						NotificationCenter.getInstance().postNotificationName(
								messagesDeleted, deletedMessages);
						for (Integer id : deletedMessages) {
							MessageObject obj = dialogMessage.get(id);
							if (obj != null) {
								obj.deleted = true;
							}
						}
					}
					if (!printChanges.isEmpty()) {
						updateMask |= UPDATE_MASK_USER_PRINT;
					}
					if (!contactsIds.isEmpty()) {
						updateMask |= UPDATE_MASK_NAME;
						updateMask |= UPDATE_MASK_USER_PHONE;
					}
					if (!chatInfoToUpdate.isEmpty()) {
						for (TLRPC.ChatParticipants info : chatInfoToUpdate) {
							MessagesStorage.getInstance().updateChatInfo(
									info.chat_id, info, false);
							NotificationCenter.getInstance()
							.postNotificationName(chatInfoDidLoaded,
									info.chat_id, info);
						}
					}
					if (updateMask != 0) {
						NotificationCenter.getInstance().postNotificationName(
								updateInterfaces, updateMask);
					}
					// 锟斤拷锟斤拷锟斤拷没锟斤拷锟斤拷锟斤拷时锟斤拷锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷锟叫斤拷锟斤拷息
					if (lastMessageArg != null) {
						showInAppNotification(lastMessageArg);
					}
				}
			});
		}

		if (!markAsReadMessages.isEmpty() || !markAsReadEncrypted.isEmpty()) {
			MessagesStorage.getInstance().storageQueue
			.postRunnable(new Runnable() {
				@Override
				public void run() {
					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() {
							if (!markAsReadMessages.isEmpty()) {
								NotificationCenter.getInstance()
								.postNotificationName(
										messagesReaded,
										markAsReadMessages);
							}
						}
					});
				}
			});
		}

		if (!markAsReadMessages.isEmpty() || !markAsReadEncrypted.isEmpty()) {
			if (!markAsReadMessages.isEmpty()) {
				MessagesStorage.getInstance().updateDialogsWithReadedMessages(
						markAsReadMessages, true);
			}
			MessagesStorage.getInstance().markMessagesAsRead(
					markAsReadMessages, markAsReadEncrypted, true);
		}
		if (!deletedMessages.isEmpty()) {
			MessagesStorage.getInstance().markMessagesAsDeleted(
					deletedMessages, true);
		}
		if (!deletedMessages.isEmpty()) {
			MessagesStorage.getInstance().updateDialogsWithDeletedMessages(
					deletedMessages, true);
		}
		if (!tasks.isEmpty()) {
			for (TLRPC.TL_updateEncryptedMessagesRead update : tasks) {
				MessagesStorage.getInstance().createTaskForDate(update.chat_id,
						update.max_date, update.date, 1);
			}
		}

		return true;
	}

	private boolean updatePrintingUsersWithNewMessages(long uid,
			ArrayList<MessageObject> messages) {
		if (uid > 0) {
			ArrayList<PrintingUser> arr = printingUsers.get(uid);
			if (arr != null) {
				printingUsers.remove(uid);
				return true;
			}
		} else if (uid < 0) {
			ArrayList<Integer> messagesUsers = new ArrayList<Integer>();
			for (MessageObject message : messages) {
				if (!messagesUsers.contains(message.messageOwner.from_id)) {
					messagesUsers.add(message.messageOwner.from_id);
				}
			}

			ArrayList<PrintingUser> arr = printingUsers.get(uid);
			boolean changed = false;
			if (arr != null) {
				for (int a = 0; a < arr.size(); a++) {
					PrintingUser user = arr.get(a);
					if (messagesUsers.contains(user.userId)) {
						arr.remove(a);
						a--;
						if (arr.isEmpty()) {
							printingUsers.remove(uid);
						}
						changed = true;
					}
				}
			}
			if (changed) {
				return true;
			}
		}
		return false;
	}

	private void playNotificationSound() {
		if (lastSoundPlay > System.currentTimeMillis() - 1800) {
			return;
		}
		try {
			lastSoundPlay = System.currentTimeMillis();

			UEngine.getInstance().getSoundService().playMidSound(2);
		} catch (Exception e) {
			FileLog.e("emm", e);
		}
	}

	private void showInAppNotification(MessageObject messageObject) 
	{

		if (!UserConfig.clientActivated) {
			return;
		}
		if (ApplicationLoader.lastPauseTime != 0) {
			ApplicationLoader.lastPauseTime = System.currentTimeMillis();
			FileLog.e("emm", "reset sleep timeout by recieved message");
		}
		if (messageObject == null) {
			return;
		}
		SharedPreferences preferences = ApplicationLoader.applicationContext
				.getSharedPreferences("Notifications_"
						+ UserConfig.clientUserId, Context.MODE_PRIVATE);
		boolean globalEnabled = preferences.getBoolean("EnableAll", true);
		if (!globalEnabled) {
			return;
		}

		if (ApplicationLoader.lastPauseTime == 0
				&& ApplicationLoader.isScreenOn) {
			boolean inAppSounds = preferences.getBoolean("EnableInAppSounds",
					true);
			boolean inAppVibrate = preferences.getBoolean("EnableInAppVibrate",
					true);
			boolean inAppPreview = preferences.getBoolean("EnableInAppPreview",
					true);

			if (inAppSounds || inAppVibrate || inAppPreview) {
				long dialog_id = messageObject.messageOwner.dialog_id;
				int user_id = messageObject.messageOwner.from_id;
				int chat_id = 0;
				if (dialog_id == 0) {
					if (messageObject.messageOwner.to_id.chat_id != 0) {
						dialog_id = -messageObject.messageOwner.to_id.chat_id;
						chat_id = messageObject.messageOwner.to_id.chat_id;
					} else if (messageObject.messageOwner.to_id.user_id != 0) {
						if (messageObject.messageOwner.to_id.user_id == UserConfig.clientUserId) {
							dialog_id = messageObject.messageOwner.from_id;
						} else {
							dialog_id = messageObject.messageOwner.to_id.user_id;
						}
					}
				} else {
					TLRPC.EncryptedChat chat = encryptedChats
							.get((int) (dialog_id >> 32));
					if (chat == null) {
						return;
					}
				}
				if (dialog_id == 0) {
					return;
				}
				TLRPC.User user = users.get(user_id);
				if (user == null) {
					return;
				}
				TLRPC.Chat chat;
				if (chat_id != 0) {
					chat = chats.get(chat_id);
					if (chat == null) {
						return;
					}
				}
				String key = "notify_" + dialog_id;
				boolean value = preferences.getBoolean(key, true);
				if (!value) {
					return;
				}

				if (inAppPreview) {
					NotificationCenter.getInstance().postNotificationName(701,
							messageObject);
				}
				if (inAppVibrate) {
					if (lastVibrator < System.currentTimeMillis() - 1800) {
						lastVibrator = System.currentTimeMillis();
						Vibrator v = (Vibrator) ApplicationLoader.applicationContext
								.getSystemService(Context.VIBRATOR_SERVICE);
						v.vibrate(100);
					}
				}
				if (inAppSounds) {
					playNotificationSound();
				}
			}
		} else {
			long dialog_id = messageObject.messageOwner.dialog_id;
			int chat_id = messageObject.messageOwner.to_id.chat_id;
			int user_id = messageObject.messageOwner.to_id.user_id;
			if (user_id != 0 && user_id == UserConfig.clientUserId) {
				user_id = messageObject.messageOwner.from_id;
			}
			if (dialog_id == 0) {
				if (chat_id != 0) {
					dialog_id = -chat_id;
				} else if (user_id != 0) {
					dialog_id = user_id;
				}
			}

			if (dialog_id != 0) {
				String key = "notify_" + dialog_id;
				boolean value = preferences.getBoolean(key, true);
				if (!value) {
					return;
				}
			}

			boolean groupEnabled = preferences.getBoolean("EnableGroup", true);
			if (chat_id != 0 && !globalEnabled) {
				return;
			}
			TLRPC.FileLocation photoPath = null;

			boolean globalVibrate = preferences.getBoolean("EnableVibrateAll",
					true);
			boolean groupVibrate = preferences.getBoolean("EnableVibrateGroup",
					true);
			boolean groupPreview = preferences.getBoolean("EnablePreviewGroup",
					true);
			boolean userPreview = preferences.getBoolean("EnablePreviewAll",
					true);

			String defaultPath = null;
			Uri defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI;
			if (defaultUri != null) {
				defaultPath = defaultUri.getPath();
			}

			String globalSound = preferences.getString("GlobalSoundPath",
					defaultPath);
			String chatSound = preferences.getString("GroupSoundPath",
					defaultPath);
			String userSoundPath = null;
			String chatSoundPath = null;

			NotificationManager mNotificationManager = (NotificationManager) ApplicationLoader.applicationContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			Intent intent = new Intent(ApplicationLoader.applicationContext,
					LaunchActivity.class);
			String msg = null;
			String titleString = StringUtil.getStringFromRes(R.string.AppName);
			if (msgUserList == null) {
				msgUserList = new ArrayList<Integer>();
			}
			if (msgGroupList == null) {
				msgGroupList = new ArrayList<Integer>();
			}
			if ((int) dialog_id != 0) {
				if (chat_id != 0) {
					intent.putExtra("chatId", chat_id);
					if (!msgGroupList.contains(chat_id)) {
						msgGroupList.add(chat_id);
					}
				}
				if (user_id != 0) {
					intent.putExtra("userId", user_id);
					if (!msgUserList.contains(user_id)) {
						msgUserList.add(user_id);
					}
					if (messageObject.messageOwner.is_from_other) {
						intent.putExtra("meetId", messageObject.messageOwner.id);
					}
				}

				if (chat_id == 0 && user_id != 0) {

					TLRPC.User u = users.get(user_id);
					if (u == null) {
						return;
					}

					String nameString = Utilities.formatName(u);

					if (u.photo != null && u.photo.photo_small != null
							&& u.photo.photo_small.volume_id != 0
							&& u.photo.photo_small.local_id != 0) {
						photoPath = u.photo.photo_small;
					}

					if (userPreview) {
						if (messageObject.messageOwner instanceof TLRPC.TL_messageService) {
							if (messageObject.messageOwner.action instanceof TLRPC.TL_messageActionUserJoined) {
								msg = LocaleController.formatString(
										"NotificationContactJoined",
										R.string.NotificationContactJoined,
										nameString);
							} else if (messageObject.messageOwner.action instanceof TLRPC.TL_messageActionUserUpdatedPhoto) {
								msg = LocaleController.formatString(
										"NotificationContactNewPhoto",
										R.string.NotificationContactNewPhoto,
										nameString);
							} else if (messageObject.messageOwner.action instanceof TLRPC.TL_messageActionLoginUnknownLocation) {
								String date = String
										.format("%s %s %s",
												LocaleController.formatterYear
												.format(((long) messageObject.messageOwner.date) * 1000),
												LocaleController.getString(
														"OtherAt",
														R.string.OtherAt),
														LocaleController.formatterDay
														.format(((long) messageObject.messageOwner.date) * 1000));
								msg = LocaleController
										.formatString(
												"NotificationUnrecognizedDevice",
												R.string.NotificationUnrecognizedDevice,
												UserConfig.currentUser.first_name,
												date,
												messageObject.messageOwner.action.title,
												messageObject.messageOwner.action.address);
							}
						} else {
							if (messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaEmpty) {
								if (messageObject.messageOwner.message != null
										&& messageObject.messageOwner.message
										.length() != 0) {
									msg = LocaleController.formatString(
											"NotificationMessageText",
											R.string.NotificationMessageText,
											nameString,
											messageObject.messageOwner.message);
								} else {
									msg = LocaleController.formatString(
											"NotificationMessageNoText",
											R.string.NotificationMessageNoText,
											nameString);
								}
							} else if (messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto) {
								msg = LocaleController.formatString(
										"NotificationMessagePhoto",
										R.string.NotificationMessagePhoto,
										nameString);
							} else if (messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaVideo) {
								msg = LocaleController.formatString(
										"NotificationMessageVideo",
										R.string.NotificationMessageVideo,
										nameString);
							} else if (messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaContact) {
								msg = LocaleController.formatString(
										"NotificationMessageContact",
										R.string.NotificationMessageContact,
										nameString);
							} else if (messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGeo) {
								msg = LocaleController.formatString(
										"NotificationMessageMap",
										R.string.NotificationMessageMap,
										nameString);
							} else if (messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaDocument) {
								msg = LocaleController.formatString(
										"NotificationMessageDocument",
										R.string.NotificationMessageDocument,
										nameString);
							} else if (messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaAudio) {
								msg = LocaleController.formatString(
										"NotificationMessageAudio",
										R.string.NotificationMessageAudio,
										nameString);
							} else if (messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaAlert) {
								msg = LocaleController.formatString(
										"NotificationMessageAlert",
										R.string.NotificationMessageAlert,
										nameString);
							}
							titleString = nameString;
						}
					} else {
						msg = LocaleController.formatString(
								"NotificationMessageNoText",
								R.string.NotificationMessageNoText, nameString);
					}
				} else if (chat_id != 0 && user_id == 0) {
					TLRPC.Chat chat = chats.get(chat_id);
					if (chat == null) {
						return;
					}
					TLRPC.User u = users
							.get(messageObject.messageOwner.from_id);
					if (u == null) {
						return;
					}

					if (u.photo != null && u.photo.photo_small != null
							&& u.photo.photo_small.volume_id != 0
							&& u.photo.photo_small.local_id != 0) {
						photoPath = u.photo.photo_small;
					}

					String chatTitle = "";
					if (chat.hasTitle == 0)
						chatTitle = chat.title;

					String nameString = Utilities.formatName(u);

					if (groupPreview) {
						if (messageObject.messageOwner instanceof TLRPC.TL_messageService) {
							if (messageObject.messageOwner.action instanceof TLRPC.TL_messageActionChatAddUser) {
								if (messageObject.messageOwner.action.user_id == UserConfig.clientUserId) {
									msg = LocaleController
											.formatString(
													"NotificationInvitedToGroup",
													R.string.NotificationInvitedToGroup,
													nameString, chatTitle);
								} else {
									TLRPC.User u2 = users
											.get(messageObject.messageOwner.action.user_id);
									if (u2 == null) {
										return;
									}
									String nameString1 = StringUtil
											.isEmpty(u2.nickname) ? Utilities
													.formatName(u2.first_name,
															u2.last_name) : u2.nickname;
													msg = LocaleController
															.formatString(
																	"NotificationGroupAddMember",
																	R.string.NotificationGroupAddMember,
																	nameString1, chatTitle,
																	Utilities.formatName(
																			u2.first_name,
																			u2.last_name));
								}
							}
							/*
							 * else if (messageObject.messageOwner.action
							 * instanceof TLRPC.TL_messageActionChatEditTitle) {
							 * msg = LocaleController.formatString(
							 * "NotificationEditedGroupName",
							 * R.string.NotificationEditedGroupName, nameString,
							 * messageObject.messageOwner.action.title); } else
							 * if (messageObject.messageOwner.action instanceof
							 * TLRPC.TL_messageActionChatEditPhoto ||
							 * messageObject.messageOwner.action instanceof
							 * TLRPC.TL_messageActionChatDeletePhoto) { msg =
							 * LocaleController
							 * .formatString("NotificationEditedGroupPhoto",
							 * R.string.NotificationEditedGroupPhoto,
							 * nameString, chat.title); }
							 */

							else if (messageObject.messageOwner.action instanceof TLRPC.TL_messageActionChatDeleteUser) {
								if (messageObject.messageOwner.action.user_id == UserConfig.clientUserId) {
									msg = LocaleController.formatString(
											"NotificationGroupKickYou",
											R.string.NotificationGroupKickYou,
											nameString, chatTitle);
								} else if (messageObject.messageOwner.action.user_id == u.id) {
									msg = LocaleController
											.formatString(
													"NotificationGroupLeftMember",
													R.string.NotificationGroupLeftMember,
													nameString, chatTitle);
								} else {
									TLRPC.User u2 = users
											.get(messageObject.messageOwner.action.user_id);
									if (u2 == null) {
										return;
									}
									String nameString1 = StringUtil
											.isEmpty(u2.nickname) ? Utilities
													.formatName(u2.first_name,
															u2.last_name) : u2.nickname;
													msg = LocaleController
															.formatString(
																	"NotificationGroupKickMember",
																	R.string.NotificationGroupKickMember,
																	nameString1, chatTitle,
																	Utilities.formatName(
																			u2.first_name,
																			u2.last_name));
								}
							}
						} else {
							if (messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaEmpty) {
								if (messageObject.messageOwner.message != null
										&& messageObject.messageOwner.message
										.length() != 0) {
									if (StringUtil.isEmpty(chatTitle)) {
										msg = LocaleController
												.formatString(
														"NotificationMessageText",
														R.string.NotificationMessageText,
														nameString,
														messageObject.messageOwner.message);
									} else {
										msg = LocaleController
												.formatString(
														"NotificationMessageGroupText",
														R.string.NotificationMessageGroupText,
														nameString,
														chatTitle,
														messageObject.messageOwner.message);
									}

								} else {
									msg = LocaleController
											.formatString(
													"NotificationMessageGroupNoText",
													R.string.NotificationMessageGroupNoText,
													nameString, chatTitle);
								}
							} else if (messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto) {
								msg = LocaleController.formatString(
										"NotificationMessageGroupPhoto",
										R.string.NotificationMessageGroupPhoto,
										nameString, chatTitle);
							} else if (messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaVideo) {
								msg = LocaleController.formatString(
										"NotificationMessageGroupVideo",
										R.string.NotificationMessageGroupVideo,
										nameString, chatTitle);
							} else if (messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaContact) {
								msg = LocaleController
										.formatString(
												"NotificationMessageGroupContact",
												R.string.NotificationMessageGroupContact,
												nameString, chatTitle);
							} else if (messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGeo) {
								msg = LocaleController.formatString(
										"NotificationMessageGroupMap",
										R.string.NotificationMessageGroupMap,
										nameString, chatTitle);
							} else if (messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaDocument) {
								msg = LocaleController
										.formatString(
												"NotificationMessageGroupDocument",
												R.string.NotificationMessageGroupDocument,
												nameString, chatTitle);
							} else if (messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaAudio) {
								msg = LocaleController.formatString(
										"NotificationMessageGroupAudio",
										R.string.NotificationMessageGroupAudio,
										nameString, chatTitle);
							} else if (messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaAlert) {
								msg = LocaleController.formatString(
										"NotificationMessageAlert",
										R.string.NotificationMessageAlert,
										nameString);
							}

						}
						titleString = chat.title;
					} else {
						msg = LocaleController.formatString(
								"NotificationMessageGroupNoText",
								R.string.NotificationMessageGroupNoText,
								nameString, chatTitle);
					}
				}
				msgCount++;
			} else {
				msg = LocaleController.getString("YouHaveNewMessage",
						R.string.YouHaveNewMessage);
				int enc_id = (int) (dialog_id >> 32);
				intent.putExtra("encId", enc_id);
			}
			if (msg == null) {
				return;
			}

			boolean needVibrate = false;

			if (user_id != 0) {
				userSoundPath = preferences.getString("sound_path_" + user_id,
						null);
				needVibrate = globalVibrate;
			}
			if (chat_id != 0) {
				chatSoundPath = preferences.getString("sound_chat_path_"
						+ chat_id, null);
				needVibrate = groupVibrate;
			}

			String choosenSoundPath = null;

			if (user_id != 0) {
				if (userSoundPath != null) {
					choosenSoundPath = userSoundPath;
				} else if (globalSound != null) {
					choosenSoundPath = globalSound;
				}
			} else if (chat_id != 0) {
				if (chatSoundPath != null) {
					choosenSoundPath = chatSoundPath;
				} else if (chatSound != null) {
					choosenSoundPath = chatSound;
				}
			} else {
				choosenSoundPath = globalSound;
			}

			intent.setAction("info.emm.openchat" + Math.random()
					+ Integer.MAX_VALUE);
			intent.setFlags(32768);
			PendingIntent contentIntent = PendingIntent.getActivity(
					ApplicationLoader.applicationContext, 0, intent,
					PendingIntent.FLAG_ONE_SHOT);

			int usersize = msgUserList.size() + msgGroupList.size();
			String contentText = msg;
			if (usersize > 1) { // 锟斤拷锟斤拷没锟斤拷锟较sgCount 锟较讹拷>1
				titleString = StringUtil.getStringFromRes(R.string.AppName);
				contentText = String
						.format(StringUtil
								.getStringFromRes(R.string.message_tips_more),
								usersize, msgCount);
			} else if (msgCount > 1) { // 一锟斤拷锟矫伙拷
				titleString += "("
						+ String.format(StringUtil
								.getStringFromRes(R.string.message_tips), ""
										+ msgCount) + ")";
			}
			if (StringUtil.isEmpty(titleString)) {
				titleString = StringUtil.getStringFromRes(R.string.AppName);
			}
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					ApplicationLoader.applicationContext)
			.setContentTitle(titleString)
			.setSmallIcon(R.drawable.nc_start)
			.setStyle(
					new NotificationCompat.BigTextStyle()
					.bigText(contentText))
					.setContentText(contentText).setAutoCancel(true)
					.setTicker(msg);

			try {
				mBuilder.setLargeIcon(BitmapFactory.decodeResource(
						ApplicationLoader.applicationContext.getResources(),
						R.drawable.ic_launcher));
			} catch (Exception e) {
				e.printStackTrace();
				FileLog.e("emm", e);
			}

			if (photoPath != null) {
				Bitmap img = FileLoader.getInstance().getImageFromMemory(
						photoPath, null, null, "50_50", false);
				if (img != null) {
					mBuilder.setLargeIcon(img);
				}
			}

			if (needVibrate) {
				mBuilder.setVibrate(new long[] { 0, 100, 0, 100 });
			}
			if (choosenSoundPath != null && !choosenSoundPath.equals("NoSound")) {
				if (choosenSoundPath.equals(defaultPath)) {
					mBuilder.setSound(defaultUri);
				} else {
					mBuilder.setSound(Uri.parse(choosenSoundPath));
				}
			}

			currentPushMessage = null;
			mBuilder.setContentIntent(contentIntent);
			mNotificationManager.cancel(1);
			Notification notification = mBuilder.build();
			notification.ledARGB = 0xff00ff00;
			notification.ledOnMS = 1000;
			notification.ledOffMS = 1000;
			notification.flags = Notification.FLAG_SHOW_LIGHTS;
			//			notification.flags |= Notification.FLAG_ONGOING_EVENT;

			try {
				Field field = notification.getClass().getDeclaredField("extraNotification");
				Object extraNotification = field.get(notification);
				Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
				method.invoke(extraNotification, msgCount);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				mNotificationManager.notify(1, notification);
				if (preferences.getBoolean("EnablePebbleNotifications", false)) {
					sendAlertToPebble(msg);
				}
				currentPushMessage = messageObject;
			} catch (Exception e) {
				FileLog.e("emm", e);
			}
		}
	}

	public void showCallNotification() {
		int notifId = 12;
		// int drawableId = R.drawable.voip_voicechat;
		String tickerText = "";
		int resid = 0;
		/*switch (IMRtmpClientMgr.getInstance().getCurrentCallStatus()) {
		case Calling:
			resid = R.string.call_notification_calling;
			break;
		case Called:
			resid = R.string.call_notification_called;
			break;
		case InCall:
			resid = R.string.call_notification_incall;
			break;
		case None:
			return;
		default:
			break;
		}*/
		//if (resid != 0) {
		tickerText = StringUtil.getStringFromRes(R.string.call_notification_called);
		//}


		Intent intent = new Intent(ApplicationLoader.getContext(),
				PhoneActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		Bundle bundle = new Bundle();
		bundle.putString("meetingId", IMRtmpClientMgr.getInstance().getMeetingID());
		bundle.putInt("chatId", IMRtmpClientMgr.getInstance().getChatId());
		bundle.putInt("userId", IMRtmpClientMgr.getInstance().getPeerID());
		bundle.putInt("callType", 0);
		intent.putExtras(bundle);

		PendingIntent contentIntent = PendingIntent.getActivity(
				ApplicationLoader.getContext(), notifId,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				ApplicationLoader.applicationContext)
		.setContentTitle(
				StringUtil.getStringFromRes(R.string.voicechattip))
				.setSmallIcon(R.drawable.nc_start)
				.setStyle(
						new NotificationCompat.BigTextStyle()
						.bigText(tickerText))
						.setContentText(tickerText).setAutoCancel(true)
						.setContentIntent(contentIntent).setTicker(tickerText);

		Notification notification = mBuilder.build();

		NotificationManager mNotifManager = (NotificationManager) ApplicationLoader
				.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		mNotifManager.notify(notifId, notification);
	}

	public void cancelCallNotif() {
		int notifId = 12;
		NotificationManager mNotifManager = (NotificationManager) ApplicationLoader
				.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		mNotifManager.cancel(notifId);
	}

	public void sendAlertToPebble(String message) {
		try {
			final Intent i = new Intent(
					"com.getpebble.action.SEND_NOTIFICATION");

			final HashMap<String, String> data = new HashMap<String, String>();
			data.put("title",
					LocaleController.getString("AppName", R.string.AppName));
			data.put("body", message);
			final JSONObject jsonData = new JSONObject(data);
			final String notificationData = new JSONArray().put(jsonData)
					.toString();

			i.putExtra("messageType", "PEBBLE_ALERT");
			i.putExtra("sender",
					LocaleController.formatString("AppName", R.string.AppName));
			i.putExtra("notificationData", notificationData);

			ApplicationLoader.applicationContext.sendBroadcast(i);
		} catch (Exception e) {
			FileLog.e("emm", e);
		}
	}

	public void dialogsUnreadCountIncrNew(final HashMap<Long, Integer> values) {

		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				for (HashMap.Entry<Long, Integer> entry : values.entrySet()) {
					TLRPC.TL_dialog dialog = dialogs_dict.get(entry.getKey());
					if (dialog != null) {
						dialog.unread_count = entry.getValue();
						NotificationCenter.getInstance().postNotificationName(
								dialogsNeedReload);
					}
				}

			}
		});
	}

	public void dialogsUnreadCountIncr(final HashMap<Long, Integer> values) {

		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				for (HashMap.Entry<Long, Integer> entry : values.entrySet()) {
					TLRPC.TL_dialog dialog = dialogs_dict.get(entry.getKey());
					if (dialog != null) {
						dialog.unread_count += entry.getValue();
						NotificationCenter.getInstance().postNotificationName(
								dialogsNeedReload);
					}
				}

			}
		});
	}

	public void updateInterfaceWithMessages(long uid,
			ArrayList<MessageObject> messages) {
		MessageObject lastMessage = null;
		TLRPC.TL_dialog dialog = dialogs_dict.get(uid);

		boolean isEncryptedChat = ((int) uid) == 0;

		NotificationCenter.getInstance().postNotificationName(
				didReceivedNewMessages, uid, messages);

		for (MessageObject message : messages) {
			if (lastMessage == null
					|| (!isEncryptedChat
							&& message.messageOwner.id > lastMessage.messageOwner.id || isEncryptedChat
							&& message.messageOwner.id < lastMessage.messageOwner.id)
							|| message.messageOwner.date > lastMessage.messageOwner.date) {
				lastMessage = message;
			}
		}

		boolean changed = false;

		if (dialog == null) {
			dialog = new TLRPC.TL_dialog();
			dialog.id = uid;
			dialog.unread_count = 0;
			dialog.top_message = lastMessage.messageOwner.id;
			dialog.last_message_date = lastMessage.messageOwner.date;
			dialogs_dict.put(uid, dialog);
			dialogs.add(dialog);
			dialogMessage.put(lastMessage.messageOwner.id, lastMessage);
			changed = true;
		} else {
			if (dialog.top_message > 0 && lastMessage.messageOwner.id > 0
					&& lastMessage.messageOwner.id > dialog.top_message
					|| dialog.top_message < 0
					&& lastMessage.messageOwner.id < 0
					&& lastMessage.messageOwner.id < dialog.top_message
					|| dialog.last_message_date < lastMessage.messageOwner.date) {
				dialogMessage.remove(dialog.top_message);
				dialog.top_message = lastMessage.messageOwner.id;
				dialog.last_message_date = lastMessage.messageOwner.date;
				dialogMessage.put(lastMessage.messageOwner.id, lastMessage);
				changed = true;
			}
		}

		if (changed) {
			dialogsServerOnly.clear();
			// Collections.sort(dialogs, new Comparator<TLRPC.TL_dialog>() {
			// @Override
			// public int compare(TLRPC.TL_dialog tl_dialog, TLRPC.TL_dialog
			// tl_dialog2) {
			// if (tl_dialog.last_message_date == tl_dialog2.last_message_date)
			// {
			// return 0;
			// } else if (tl_dialog.last_message_date <
			// tl_dialog2.last_message_date) {
			// return 1;
			// } else {
			// return -1;
			// }
			// }
			// });
			sortDialogs();
			for (TLRPC.TL_dialog d : dialogs) {
				if ((int) d.id != 0) {
					dialogsServerOnly.add(d);
				}
			}
		}
	}

	public void sortDialogs() {
		Collections.sort(dialogs, new Comparator<TLRPC.TL_dialog>() {
			@Override
			public int compare(TLRPC.TL_dialog tl_dialog,
					TLRPC.TL_dialog tl_dialog2) {
				if (tl_dialog2.upDate > tl_dialog.upDate) {
					return 1;
				} else if (tl_dialog2.upDate < tl_dialog.upDate) {
					return -1;
				} else {
					if (tl_dialog.last_message_date == tl_dialog2.last_message_date) {
						return 0;
					} else if (tl_dialog.last_message_date < tl_dialog2.last_message_date) {
						return 1;
					} else {
						return -1;
					}
				}
			}
		});
	}

	public TLRPC.Message decryptMessage(TLRPC.EncryptedMessage message) {
		TLRPC.EncryptedChat chat = encryptedChats.get(message.chat_id);
		if (chat == null) {
			Semaphore semaphore = new Semaphore(0);
			ArrayList<TLObject> result = new ArrayList<TLObject>();
			MessagesStorage.getInstance().getEncryptedChat(message.chat_id,
					semaphore, result);
			try {
				semaphore.acquire();
			} catch (Exception e) {
				FileLog.e("emm", e);
			}
			if (result.size() == 2) {
				chat = (TLRPC.EncryptedChat) result.get(0);
				TLRPC.User user = (TLRPC.User) result.get(1);
				encryptedChats.put(chat.id, chat);
				users.putIfAbsent(user.id, user);
				// add by xueqiang
				usersSDK.putIfAbsent(user.identification, user);
			}
		}
		if (chat == null) {
			return null;
		}
		SerializedData is = new SerializedData(message.bytes);
		long fingerprint = is.readInt64();
		if (chat.key_fingerprint == fingerprint) {
			byte[] messageKey = is.readData(16);
			MessageKeyData keyData = Utilities.generateMessageKeyData(
					chat.auth_key, messageKey, false);

			byte[] messageData = is.readData(message.bytes.length - 24);
			messageData = Utilities.aesIgeEncryption(messageData,
					keyData.aesKey, keyData.aesIv, false, false, 0);

			is = new SerializedData(messageData);
			int len = is.readInt32();
			TLObject object = TLClassStore.Instance().TLdeserialize(is,
					is.readInt32());
			if (object != null) {

				int from_id = chat.admin_id;
				if (from_id == UserConfig.clientUserId) {
					from_id = chat.participant_id;
				}

				if (object instanceof TLRPC.TL_decryptedMessage) {
					TLRPC.TL_decryptedMessage decryptedMessage = (TLRPC.TL_decryptedMessage) object;
					TLRPC.TL_message newMessage = new TLRPC.TL_message();
					newMessage.message = decryptedMessage.message;
					newMessage.date = message.date;
					newMessage.local_id = newMessage.id = UserConfig
							.getNewMessageId();
					UserConfig.saveConfig(false);
					newMessage.from_id = from_id;
					newMessage.to_id = new TLRPC.TL_peerUser();
					newMessage.random_id = message.random_id;
					newMessage.to_id.user_id = UserConfig.clientUserId;
					newMessage.out = false;
					newMessage.unread = true;
					newMessage.dialog_id = ((long) chat.id) << 32;
					newMessage.ttl = chat.ttl;
					if (decryptedMessage.media instanceof TLRPC.TL_decryptedMessageMediaEmpty) {
						newMessage.media = new TLRPC.TL_messageMediaEmpty();
					} else if (decryptedMessage.media instanceof TLRPC.TL_decryptedMessageMediaContact) {
						newMessage.media = new TLRPC.TL_messageMediaContact();
						newMessage.media.last_name = decryptedMessage.media.last_name;
						newMessage.media.first_name = decryptedMessage.media.first_name;
						newMessage.media.phone_number = decryptedMessage.media.phone_number;
						newMessage.media.user_id = decryptedMessage.media.user_id;
					} else if (decryptedMessage.media instanceof TLRPC.TL_decryptedMessageMediaGeoPoint) {
						newMessage.media = new TLRPC.TL_messageMediaGeo();
						newMessage.media.geo = new TLRPC.TL_geoPoint();
						newMessage.media.geo.lat = decryptedMessage.media.lat;
						newMessage.media.geo._long = decryptedMessage.media._long;
					} else if (decryptedMessage.media instanceof TLRPC.TL_decryptedMessageMediaPhoto) {
						if (decryptedMessage.media.key == null
								|| decryptedMessage.media.key.length != 32
								|| decryptedMessage.media.iv == null
								|| decryptedMessage.media.iv.length != 32) {
							return null;
						}
						newMessage.media = new TLRPC.TL_messageMediaPhoto();
						newMessage.media.photo = new TLRPC.TL_photo();
						newMessage.media.photo.user_id = newMessage.from_id;
						newMessage.media.photo.date = newMessage.date;
						newMessage.media.photo.caption = "";
						newMessage.media.photo.geo = new TLRPC.TL_geoPointEmpty();
						if (decryptedMessage.media.thumb.length != 0
								&& decryptedMessage.media.thumb.length <= 5000
								&& decryptedMessage.media.thumb_w < 100
								&& decryptedMessage.media.thumb_h < 100) {
							TLRPC.TL_photoCachedSize small = new TLRPC.TL_photoCachedSize();
							small.w = decryptedMessage.media.thumb_w;
							small.h = decryptedMessage.media.thumb_h;
							small.bytes = decryptedMessage.media.thumb;
							small.type = "s";
							small.location = new TLRPC.TL_fileLocationUnavailable();
							newMessage.media.photo.sizes.add(small);
						}

						TLRPC.TL_photoSize big = new TLRPC.TL_photoSize();
						big.w = decryptedMessage.media.w;
						big.h = decryptedMessage.media.h;
						big.type = "x";
						big.size = message.file.size;
						big.location = new TLRPC.TL_fileEncryptedLocation();
						big.location.key = decryptedMessage.media.key;
						big.location.iv = decryptedMessage.media.iv;
						big.location.dc_id = message.file.dc_id;
						big.location.volume_id = message.file.id;
						big.location.secret = message.file.access_hash;
						big.location.local_id = message.file.key_fingerprint;
						newMessage.media.photo.sizes.add(big);
					} else if (decryptedMessage.media instanceof TLRPC.TL_decryptedMessageMediaVideo) {
						if (decryptedMessage.media.key == null
								|| decryptedMessage.media.key.length != 32
								|| decryptedMessage.media.iv == null
								|| decryptedMessage.media.iv.length != 32) {
							return null;
						}
						newMessage.media = new TLRPC.TL_messageMediaVideo();
						newMessage.media.video = new TLRPC.TL_videoEncrypted();
						if (decryptedMessage.media.thumb.length != 0
								&& decryptedMessage.media.thumb.length <= 5000
								&& decryptedMessage.media.thumb_w < 100
								&& decryptedMessage.media.thumb_h < 100) {
							newMessage.media.video.thumb = new TLRPC.TL_photoCachedSize();
							newMessage.media.video.thumb.bytes = decryptedMessage.media.thumb;
							newMessage.media.video.thumb.w = decryptedMessage.media.thumb_w;
							newMessage.media.video.thumb.h = decryptedMessage.media.thumb_h;
							newMessage.media.video.thumb.type = "s";
							newMessage.media.video.thumb.location = new TLRPC.TL_fileLocationUnavailable();
						} else {
							newMessage.media.video.thumb = new TLRPC.TL_photoSizeEmpty();
							newMessage.media.video.thumb.type = "s";
						}
						newMessage.media.video.duration = decryptedMessage.media.duration;
						newMessage.media.video.dc_id = message.file.dc_id;
						newMessage.media.video.w = decryptedMessage.media.w;
						newMessage.media.video.h = decryptedMessage.media.h;
						newMessage.media.video.date = message.date;
						newMessage.media.video.caption = "";
						newMessage.media.video.user_id = from_id;
						newMessage.media.video.size = message.file.size;
						newMessage.media.video.id = message.file.id;
						newMessage.media.video.access_hash = message.file.access_hash;
						newMessage.media.video.key = decryptedMessage.media.key;
						newMessage.media.video.iv = decryptedMessage.media.iv;
					} else if (decryptedMessage.media instanceof TLRPC.TL_decryptedMessageMediaDocument) {
						if (decryptedMessage.media.key == null
								|| decryptedMessage.media.key.length != 32
								|| decryptedMessage.media.iv == null
								|| decryptedMessage.media.iv.length != 32) {
							return null;
						}
						newMessage.media = new TLRPC.TL_messageMediaDocument();
						newMessage.media.document = new TLRPC.TL_documentEncrypted();
						newMessage.media.document.id = message.file.id;
						newMessage.media.document.access_hash = message.file.access_hash;
						newMessage.media.document.user_id = decryptedMessage.media.user_id;
						newMessage.media.document.date = message.date;
						newMessage.media.document.file_name = decryptedMessage.media.file_name;
						newMessage.media.document.mime_type = decryptedMessage.media.mime_type;
						newMessage.media.document.size = message.file.size;
						newMessage.media.document.key = decryptedMessage.media.key;
						newMessage.media.document.iv = decryptedMessage.media.iv;
						if (decryptedMessage.media.thumb.length != 0
								&& decryptedMessage.media.thumb.length <= 5000
								&& decryptedMessage.media.thumb_w < 100
								&& decryptedMessage.media.thumb_h < 100) {
							newMessage.media.document.thumb = new TLRPC.TL_photoCachedSize();
							newMessage.media.document.thumb.bytes = decryptedMessage.media.thumb;
							newMessage.media.document.thumb.w = decryptedMessage.media.thumb_w;
							newMessage.media.document.thumb.h = decryptedMessage.media.thumb_h;
							newMessage.media.document.thumb.type = "s";
							newMessage.media.document.thumb.location = new TLRPC.TL_fileLocationUnavailable();
						} else {
							newMessage.media.document.thumb = new TLRPC.TL_photoSizeEmpty();
							newMessage.media.document.thumb.type = "s";
						}
						newMessage.media.document.dc_id = message.file.dc_id;
					} else if (decryptedMessage.media instanceof TLRPC.TL_decryptedMessageMediaAudio) {
						if (decryptedMessage.media.key == null
								|| decryptedMessage.media.key.length != 32
								|| decryptedMessage.media.iv == null
								|| decryptedMessage.media.iv.length != 32) {
							return null;
						}
						newMessage.media = new TLRPC.TL_messageMediaAudio();
						newMessage.media.audio = new TLRPC.TL_audioEncrypted();
						newMessage.media.audio.id = message.file.id;
						newMessage.media.audio.access_hash = message.file.access_hash;
						newMessage.media.audio.user_id = decryptedMessage.media.user_id;
						newMessage.media.audio.date = message.date;
						newMessage.media.audio.size = message.file.size;
						newMessage.media.audio.key = decryptedMessage.media.key;
						newMessage.media.audio.iv = decryptedMessage.media.iv;
						newMessage.media.audio.dc_id = message.file.dc_id;
						newMessage.media.audio.duration = decryptedMessage.media.duration;
					} else {
						return null;
					}
					return newMessage;
				} else if (object instanceof TLRPC.TL_decryptedMessageService) {
					TLRPC.TL_decryptedMessageService serviceMessage = (TLRPC.TL_decryptedMessageService) object;
					if (serviceMessage.action instanceof TLRPC.TL_decryptedMessageActionSetMessageTTL) {
						TLRPC.TL_messageService newMessage = new TLRPC.TL_messageService();
						newMessage.action = new TLRPC.TL_messageActionTTLChange();
						newMessage.action.ttl = chat.ttl = serviceMessage.action.ttl_seconds;
						newMessage.local_id = newMessage.id = UserConfig
								.getNewMessageId();
						UserConfig.saveConfig(false);
						newMessage.unread = true;
						newMessage.date = message.date;
						newMessage.from_id = from_id;
						newMessage.to_id = new TLRPC.TL_peerUser();
						newMessage.to_id.user_id = UserConfig.clientUserId;
						newMessage.out = false;
						newMessage.dialog_id = ((long) chat.id) << 32;
						MessagesStorage.getInstance().updateEncryptedChatTTL(
								chat);
						return newMessage;
					} else if (serviceMessage.action instanceof TLRPC.TL_decryptedMessageActionFlushHistory) {
						final long did = ((long) chat.id) << 32;
						Utilities.RunOnUIThread(new Runnable() {
							@Override
							public void run() {
								TLRPC.TL_dialog dialog = dialogs_dict.get(did);
								if (dialog != null) {
									dialogMessage.remove(dialog.top_message);
								}
								MessagesStorage.getInstance().deleteDialog(did,
										true);
								NotificationCenter.getInstance()
								.postNotificationName(
										removeAllMessagesFromDialog,
										did);
								NotificationCenter
								.getInstance()
								.postNotificationName(dialogsNeedReload);
							}
						});
						return null;
					} else if (serviceMessage.action instanceof TLRPC.TL_decryptedMessageActionDeleteMessages) {
						if (!serviceMessage.action.random_ids.isEmpty()) {
							pendingEncMessagesToDelete
							.addAll(serviceMessage.action.random_ids);
						}
						return null;
					}
				} else {
					FileLog.e("emm", "unkown message " + object);
				}
			} else {
				FileLog.e("emm", "unkown TLObject");
			}
		} else {
			FileLog.e("emm", "fingerprint mismatch");
		}
		return null;
	}

	public void processAcceptedSecretChat(
			final TLRPC.EncryptedChat encryptedChat) {
		BigInteger p = new BigInteger(1, MessagesStorage.secretPBytes);
		BigInteger i_authKey = new BigInteger(1, encryptedChat.g_a_or_b);

		if (!Utilities.isGoodGaAndGb(i_authKey, p)) {
			declineSecretChat(encryptedChat.id);
			return;
		}

		i_authKey = i_authKey
				.modPow(new BigInteger(1, encryptedChat.a_or_b), p);

		byte[] authKey = i_authKey.toByteArray();
		if (authKey.length > 256) {
			byte[] correctedAuth = new byte[256];
			System.arraycopy(authKey, authKey.length - 256, correctedAuth, 0,
					256);
			authKey = correctedAuth;
		} else if (authKey.length < 256) {
			byte[] correctedAuth = new byte[256];
			System.arraycopy(authKey, 0, correctedAuth, 256 - authKey.length,
					authKey.length);
			for (int a = 0; a < 256 - authKey.length; a++) {
				authKey[a] = 0;
			}
			authKey = correctedAuth;
		}
		byte[] authKeyHash = Utilities.computeSHA1(authKey);
		byte[] authKeyId = new byte[8];
		System.arraycopy(authKeyHash, authKeyHash.length - 8, authKeyId, 0, 8);
		long fingerprint = Utilities.bytesToLong(authKeyId);
		if (encryptedChat.key_fingerprint == fingerprint) {
			encryptedChat.auth_key = authKey;
			MessagesStorage.getInstance().updateEncryptedChat(encryptedChat);
			Utilities.RunOnUIThread(new Runnable() {
				@Override
				public void run() {
					encryptedChats.put(encryptedChat.id, encryptedChat);
					NotificationCenter.getInstance().postNotificationName(
							encryptedChatUpdated, encryptedChat);
				}
			});
		} else {
			final TLRPC.TL_encryptedChatDiscarded newChat = new TLRPC.TL_encryptedChatDiscarded();
			newChat.id = encryptedChat.id;
			newChat.user_id = encryptedChat.user_id;
			newChat.auth_key = encryptedChat.auth_key;
			MessagesStorage.getInstance().updateEncryptedChat(newChat);
			Utilities.RunOnUIThread(new Runnable() {
				@Override
				public void run() {
					encryptedChats.put(newChat.id, newChat);
					NotificationCenter.getInstance().postNotificationName(
							encryptedChatUpdated, newChat);
				}
			});
			declineSecretChat(encryptedChat.id);
		}
	}

	public void declineSecretChat(int chat_id) {
		TLRPC.TL_messages_discardEncryption req = new TLRPC.TL_messages_discardEncryption();
		req.chat_id = chat_id;
		ConnectionsManager.getInstance().performRpc(req,
				new RPCRequest.RPCRequestDelegate() {
			@Override
			public void run(TLObject response, TLRPC.TL_error error) {

			}
		}, null, true, RPCRequest.RPCRequestClassGeneric);
	}

	public void acceptSecretChat(final TLRPC.EncryptedChat encryptedChat) {
		if (acceptingChats.get(encryptedChat.id) != null) {
			return;
		}
		acceptingChats.put(encryptedChat.id, encryptedChat);
		TLRPC.TL_messages_getDhConfig req = new TLRPC.TL_messages_getDhConfig();
		req.random_length = 256;
		req.version = MessagesStorage.lastSecretVersion;
		ConnectionsManager.getInstance().performRpc(req,
				new RPCRequest.RPCRequestDelegate() {
			@Override
			public void run(TLObject response, TLRPC.TL_error error) {
				if (error == null) {
					TLRPC.messages_DhConfig res = (TLRPC.messages_DhConfig) response;
					if (response instanceof TLRPC.TL_messages_dhConfig) {
						if (!Utilities.isGoodPrime(res.p, res.g)) {
							acceptingChats.remove(encryptedChat.id);
							declineSecretChat(encryptedChat.id);
							return;
						}

						MessagesStorage.secretPBytes = res.p;
						MessagesStorage.secretG = res.g;
						MessagesStorage.lastSecretVersion = res.version;
						MessagesStorage.getInstance().saveSecretParams(
								MessagesStorage.lastSecretVersion,
								MessagesStorage.secretG,
								MessagesStorage.secretPBytes);
					}
					byte[] salt = new byte[256];
					for (int a = 0; a < 256; a++) {
						salt[a] = (byte) ((byte) (random.nextDouble() * 256) ^ res.random[a]);
					}
					encryptedChat.a_or_b = salt;
					BigInteger p = new BigInteger(1,
							MessagesStorage.secretPBytes);
					BigInteger g_b = BigInteger
							.valueOf(MessagesStorage.secretG);
					g_b = g_b.modPow(new BigInteger(1, salt), p);
					BigInteger g_a = new BigInteger(1,
							encryptedChat.g_a);

					if (!Utilities.isGoodGaAndGb(g_a, p)) {
						acceptingChats.remove(encryptedChat.id);
						declineSecretChat(encryptedChat.id);
						return;
					}

					byte[] g_b_bytes = g_b.toByteArray();
					if (g_b_bytes.length > 256) {
						byte[] correctedAuth = new byte[256];
						System.arraycopy(g_b_bytes, 1, correctedAuth,
								0, 256);
						g_b_bytes = correctedAuth;
					}

					g_a = g_a.modPow(new BigInteger(1, salt), p);

					byte[] authKey = g_a.toByteArray();
					if (authKey.length > 256) {
						byte[] correctedAuth = new byte[256];
						System.arraycopy(authKey, authKey.length - 256,
								correctedAuth, 0, 256);
						authKey = correctedAuth;
					} else if (authKey.length < 256) {
						byte[] correctedAuth = new byte[256];
						System.arraycopy(authKey, 0, correctedAuth,
								256 - authKey.length, authKey.length);
						for (int a = 0; a < 256 - authKey.length; a++) {
							authKey[a] = 0;
						}
						authKey = correctedAuth;
					}
					byte[] authKeyHash = Utilities.computeSHA1(authKey);
					byte[] authKeyId = new byte[8];
					System.arraycopy(authKeyHash,
							authKeyHash.length - 8, authKeyId, 0, 8);
					encryptedChat.auth_key = authKey;

					TLRPC.TL_messages_acceptEncryption req2 = new TLRPC.TL_messages_acceptEncryption();
					req2.g_b = g_b_bytes;
					req2.peer = new TLRPC.TL_inputEncryptedChat();
					req2.peer.chat_id = encryptedChat.id;
					req2.peer.access_hash = encryptedChat.access_hash;
					req2.key_fingerprint = Utilities
							.bytesToLong(authKeyId);
					ConnectionsManager.getInstance().performRpc(
							req2,
							new RPCRequest.RPCRequestDelegate() {
								@Override
								public void run(TLObject response,
										TLRPC.TL_error error) {
									acceptingChats
									.remove(encryptedChat.id);
									if (error == null) {
										final TLRPC.EncryptedChat newChat = (TLRPC.EncryptedChat) response;
										newChat.auth_key = encryptedChat.auth_key;
										newChat.user_id = encryptedChat.user_id;
										MessagesStorage.getInstance()
										.updateEncryptedChat(
												newChat);
										Utilities
										.RunOnUIThread(new Runnable() {
											@Override
											public void run() {
												encryptedChats
												.put(newChat.id,
														newChat);
												NotificationCenter
												.getInstance()
												.postNotificationName(
														encryptedChatUpdated,
														newChat);
											}
										});
									}
								}
							}, null, true,
							RPCRequest.RPCRequestClassGeneric);
				} else {
					acceptingChats.remove(encryptedChat.id);
				}
			}
		}, null, true, RPCRequest.RPCRequestClassGeneric);
	}

	public void startSecretChat(final Context context, final TLRPC.User user) {
		if (user == null) {
			return;
		}
		/*
		 * startingSecretChat = true; final ProgressDialog progressDialog = new
		 * ProgressDialog(context);
		 * progressDialog.setMessage(LocaleController.getString("Loading",
		 * R.string.Loading)); progressDialog.setCanceledOnTouchOutside(false);
		 * progressDialog.setCancelable(false); TLRPC.TL_messages_getDhConfig
		 * req = new TLRPC.TL_messages_getDhConfig(); req.random_length = 256;
		 * req.version = MessagesStorage.lastSecretVersion; final long reqId =
		 * ConnectionsManager.getInstance().performRpc(req, new
		 * RPCRequest.RPCRequestDelegate() {
		 * 
		 * @Override public void run(TLObject response, TLRPC.TL_error error) {
		 * if (error == null) { TLRPC.messages_DhConfig res =
		 * (TLRPC.messages_DhConfig) response; if (response instanceof
		 * TLRPC.TL_messages_dhConfig) { if (!Utilities.isGoodPrime(res.p,
		 * res.g)) { Utilities.RunOnUIThread(new Runnable() {
		 * 
		 * @Override public void run() { try { if (!((ActionBarActivity)
		 * context).isFinishing()) { progressDialog.dismiss(); } } catch
		 * (Exception e) { FileLog.e("emm", e); } } }); return; }
		 * MessagesStorage.secretPBytes = res.p; MessagesStorage.secretG =
		 * res.g; MessagesStorage.lastSecretVersion = res.version;
		 * MessagesStorage
		 * .getInstance().saveSecretParams(MessagesStorage.lastSecretVersion,
		 * MessagesStorage.secretG, MessagesStorage.secretPBytes); } final
		 * byte[] salt = new byte[256]; for (int a = 0; a < 256; a++) { salt[a]
		 * = (byte) ((byte) (random.nextDouble() * 256) ^ res.random[a]); }
		 * 
		 * BigInteger i_g_a = BigInteger.valueOf(MessagesStorage.secretG); i_g_a
		 * = i_g_a.modPow(new BigInteger(1, salt), new BigInteger(1,
		 * MessagesStorage.secretPBytes)); byte[] g_a = i_g_a.toByteArray(); if
		 * (g_a.length > 256) { byte[] correctedAuth = new byte[256];
		 * System.arraycopy(g_a, 1, correctedAuth, 0, 256); g_a = correctedAuth;
		 * }
		 * 
		 * TLRPC.TL_messages_requestEncryption req2 = new
		 * TLRPC.TL_messages_requestEncryption(); req2.g_a = g_a; req2.user_id =
		 * getInputUser(user); req2.random_id = random.nextInt();
		 * ConnectionsManager.getInstance().performRpc(req2, new
		 * RPCRequest.RPCRequestDelegate() {
		 * 
		 * @Override public void run(final TLObject response, TLRPC.TL_error
		 * error) { if (error == null) { Utilities.RunOnUIThread(new Runnable()
		 * {
		 * 
		 * @Override public void run() { startingSecretChat = false; if
		 * (!((ActionBarActivity) context).isFinishing()) { try {
		 * progressDialog.dismiss(); } catch (Exception e) { FileLog.e("emm",
		 * e); } } TLRPC.EncryptedChat chat = (TLRPC.EncryptedChat) response;
		 * chat.user_id = chat.participant_id; encryptedChats.put(chat.id,
		 * chat); chat.a_or_b = salt; TLRPC.TL_dialog dialog = new
		 * TLRPC.TL_dialog(); dialog.id = ((long) chat.id) << 32;
		 * dialog.unread_count = 0; dialog.top_message = 0;
		 * dialog.last_message_date =
		 * ConnectionsManager.getInstance().getCurrentTime();
		 * dialogs_dict.put(dialog.id, dialog); dialogs.add(dialog);
		 * dialogsServerOnly.clear(); Collections.sort(dialogs, new
		 * Comparator<TLRPC.TL_dialog>() {
		 * 
		 * @Override public int compare(TLRPC.TL_dialog tl_dialog,
		 * TLRPC.TL_dialog tl_dialog2) { if (tl_dialog.last_message_date ==
		 * tl_dialog2.last_message_date) { return 0; } else if
		 * (tl_dialog.last_message_date < tl_dialog2.last_message_date) { return
		 * 1; } else { return -1; } } }); for (TLRPC.TL_dialog d : dialogs) { if
		 * ((int) d.id != 0) { dialogsServerOnly.add(d); } }
		 * NotificationCenter.getInstance
		 * ().postNotificationName(dialogsNeedReload);
		 * MessagesStorage.getInstance().putEncryptedChat(chat, user, dialog);
		 * NotificationCenter
		 * .getInstance().postNotificationName(encryptedChatCreated, chat);
		 * Utilities.stageQueue.postRunnable(new Runnable() {
		 * 
		 * @Override public void run() { if
		 * (!delayedEncryptedChatUpdates.isEmpty()) {
		 * processUpdateArray(delayedEncryptedChatUpdates, null, null);
		 * delayedEncryptedChatUpdates.clear(); } } }); } }); } else {
		 * delayedEncryptedChatUpdates.clear(); Utilities.RunOnUIThread(new
		 * Runnable() {
		 * 
		 * @Override public void run() { if (!((ActionBarActivity)
		 * context).isFinishing()) { startingSecretChat = false; try {
		 * progressDialog.dismiss(); } catch (Exception e) { FileLog.e("emm",
		 * e); } AlertDialog.Builder builder = new AlertDialog.Builder(context);
		 * builder.setTitle(LocaleController.getString("AppName",
		 * R.string.AppName)); builder.setMessage(LocaleController.formatString(
		 * "CreateEncryptedChatOutdatedError",
		 * R.string.CreateEncryptedChatOutdatedError, user.first_name,
		 * user.first_name));
		 * builder.setPositiveButton(LocaleController.getString("OK",
		 * R.string.OK), null); builder.show().setCanceledOnTouchOutside(true);
		 * } } }); } } }, null, true, RPCRequest.RPCRequestClassGeneric |
		 * RPCRequest.RPCRequestClassFailOnServerErrors); } else {
		 * delayedEncryptedChatUpdates.clear(); Utilities.RunOnUIThread(new
		 * Runnable() {
		 * 
		 * @Override public void run() { startingSecretChat = false; if
		 * (!((ActionBarActivity) context).isFinishing()) { try {
		 * progressDialog.dismiss(); } catch (Exception e) { FileLog.e("emm",
		 * e); } } } }); } } }, null, true, RPCRequest.RPCRequestClassGeneric |
		 * RPCRequest.RPCRequestClassFailOnServerErrors);
		 */
	}

	// xueqiang add for version manage
	public void processLoadVersion(final int version) {
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				ConnectionsManager.getInstance().setVersion(version);
				// 锟斤拷锟斤拷锟斤拷锟斤拷乇锟斤拷锟斤拷锟较拷锟斤拷锟较碉拷耍锟斤拷榧帮拷锟斤拷锟斤拷锟较�
				loadData();
				// 锟斤拷锟斤拷没锟斤拷锟铰斤拷幕锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷娲拷锟絊ERVER锟斤拷锟斤拷锟斤拷锟绞号ｏ拷锟斤拷锟斤拷锟斤拷要SAVE TO DB
				// 锟斤拷锟斤拷锟斤拷远锟斤拷锟铰斤拷锟斤拷颖锟斤拷锟紻B锟斤拷锟斤拷锟绞猴拷锟斤拷息
				// 锟斤拷时注锟斤拷锟斤拷锟斤拷锟绞猴拷
				/*
				 * for(int i=0;i<accounts.size();i++) { String sAccount =
				 * accounts.get(i); if(sAccount.compareTo("")!=0) {
				 * FileLog.e("emm", "save account to db="+sAccount);
				 * MessagesStorage.getInstance().putAccount(sAccount); } }
				 */
			}
		});
	}

	public void LoadAddress(ArrayList<DataAdapter> arrData, boolean bShowAll) {
		int count = userCompanysMap.size();
		int size = companys.size();
		if (count == 0 || size == 0)
			return;
		if (size == 1) {
			// 锟斤拷锟截革拷锟斤拷锟斤拷锟铰的诧拷锟脚硷拷锟矫伙拷
			for (ConcurrentHashMap.Entry<Integer, TLRPC.TL_Company> entry : companys
					.entrySet()) {
				TLRPC.TL_Company company = entry.getValue();
				LoadAddress(company.id, company.rootdeptid, arrData, bShowAll);
			}

		} else {
			// 锟斤拷示锟洁公司锟斤拷同时锟斤拷示锟斤拷司锟叫碉拷锟窖撅拷同锟斤拷锟斤拷没锟�
			LoadCompanyData(arrData);
			// loadUsers(arrData,true);
		}
	}

	public boolean hasChild(int deptid) {
		for (ConcurrentHashMap.Entry<Integer, TLRPC.TL_DepartMent> entry : this.departments
				.entrySet()) {
			TLRPC.TL_DepartMent dept = entry.getValue();

			if (dept.deptParentID == deptid) {
				return true;
			}
		}
		int size = departidToUsers.size();
		HashSet<TLRPC.TL_UserCompany> userCompanys = departidToUsers
				.get(deptid);
		if (userCompanys != null) {
			Iterator<TLRPC.TL_UserCompany> it = userCompanys.iterator();
			while (it.hasNext()) {
				TLRPC.TL_UserCompany userCompany = it.next();
				if (userCompany != null)
					return true;
			}
		}
		return false;
	}

	/**
	 * @Title: LoadAddress
	 * 
	 * @Description: 锟斤拷司锟斤拷锟斤拷锟斤拷员锟叫憋拷
	 * 
	 * @param companyid
	 *            锟斤拷司ID
	 * @param deptid
	 *            锟斤拷锟斤拷ID
	 * @param arrData
	 *            锟斤拷锟斤拷锟斤拷员锟叫憋拷
	 */
	public void LoadAddress(int companyid, int deptid,
			ArrayList<DataAdapter> arrData, boolean bShowAll) {
	    
	    System.out.println("arrData--------11--"+arrData);
		if (companyid == 0) {
			LoadAddress(arrData, bShowAll);
			return;
		}
		TLRPC.TL_Company company = companys.get(companyid);
		if (company == null)
			return;

		// 目前锟斤拷锟捷癸拷锟斤拷锟斤拷depid锟斤拷示锟矫伙拷锟斤拷锟斤拷某锟斤拷锟斤拷锟脚ｏ拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷碌锟斤拷硬锟斤拷偶锟斤拷锟斤拷锟斤拷锟斤拷锟铰碉拷锟斤拷员锟斤拷息
		for (ConcurrentHashMap.Entry<Integer, TLRPC.TL_DepartMent> entry : this.departments
				.entrySet()) {
			TLRPC.TL_DepartMent dept = entry.getValue();

			if (dept.deptParentID == deptid) {
				DataAdapter da = new DataAdapter();
				da.dataID = dept.id;
				da.parentDeptID = dept.deptParentID;
				da.companyID = dept.companyID;
				da.dataName = dept.name;
				da.dataICO = "";
				da.isCompany = false;
				da.version = dept.version;
				da.haveChild = true;
				da.isUser = false;
				arrData.add(da);
			}
		}
		/*
		 * boolean bShow = false; TLRPC.TL_Company company =
		 * companys.get(companyid); if(company!=null &&
		 * company.createuserid==UserConfig.clientUserId) bShow = true;
		 */
		// 锟斤拷锟斤拷羌锟斤拷馗锟斤拷锟斤拷牛锟斤拷图锟斤拷锟斤拷锟斤拷锟斤拷朔锟斤拷锟街伙拷锟斤拷夭锟斤拷锟斤拷碌锟斤拷锟皆�
		// shenfeng锟斤拷锟斤拷
		if (company != null && company.rootdeptid == deptid) {
			loadCompanyUser(companyid, arrData, bShowAll);
			return;
		}

		HashSet<TLRPC.TL_UserCompany> userCompanys = departidToUsers
				.get(deptid);
		if (userCompanys != null) {
			Iterator<TLRPC.TL_UserCompany> it = userCompanys.iterator();
			while (it.hasNext()) {
				TLRPC.TL_UserCompany userCompany = it.next();
				String s = Utilities.formatName(userCompany.first_name,
						userCompany.last_name);
				if (userCompany.ucstate != 0 && !bShowAll) {
					continue;
				}
				DataAdapter da = new DataAdapter();
				da.dataID = userCompany.userID;
				// user锟斤拷锟皆斤拷锟斤拷锟斤拷锟斤拷锟斤拷companyid锟斤拷userid,锟斤拷锟斤拷使锟斤拷锟斤拷锟斤拷锟�
				da.parentDeptID = userCompany.deptID;
				da.companyID = userCompany.companyID;
				da.dataName = Utilities.formatName(userCompany.first_name,
						userCompany.last_name);
				TLRPC.User user = users.get(userCompany.userID);
				if (user != null) {
					da.dataICO = user.userico;
					da.dataName = StringUtil.getCompanyUserRemark(user,
							da.dataName);
				}
				da.isCompany = false;
				da.haveChild = false;
				da.isUser = true;
				
				 
				String pinyin = CharacterParser.getInstance().getSelling(
						da.dataName);
				String sortString = "";
				if (pinyin != null && pinyin.length() > 0) {
					sortString = pinyin.substring(0, 1).toUpperCase();
				}
				if (sortString.matches("[A-Z]")) {
					da.sortLetters = sortString;
				} else {
					da.sortLetters = "#";
				}
				arrData.add(da);
			}

			// wangxm add 锟斤拷锟斤拷
			
			
			Collections.sort(arrData, compare);
		}
	}

	public void loadCompanyUser(int companyID, ArrayList<DataAdapter> arrData,
			boolean bShowAll) {
		 System.out.println("arrData--------66--"+arrData);
		for (ConcurrentHashMap.Entry<Integer, HashSet<TLRPC.TL_UserCompany>> entry : departidToUsers
				.entrySet()) {
			HashSet<TLRPC.TL_UserCompany> depts = entry.getValue();
			Iterator<TLRPC.TL_UserCompany> it = depts.iterator();
			while (it.hasNext()) {
				TLRPC.TL_UserCompany userCompany = it.next();
				if (userCompany.companyID == companyID) {
					// 未锟斤拷装锟斤拷锟矫伙拷锟斤拷锟斤拷示,锟节达拷锟斤拷锟斤拷锟绞憋拷锟斤拷锟酵ㄑ堵凤拷锟揭拷锟绞�
					if (!bShowAll)
						if (getUserState(userCompany.userID) == 0) {
							// 锟斤拷userCompany锟叫碉拷ucstate锟斤拷0锟斤拷锟斤拷锟斤拷锟皆ｏ拷应锟斤拷锟斤拷3锟斤拷锟斤拷示未锟斤拷装锟脚讹拷,锟斤拷示锟斤拷user锟叫碉拷状态锟斤拷未锟斤拷锟斤拷锟叫断筹拷锟斤拷锟斤拷锟斤拷锟斤拷确锟斤拷
							// lihy todo..
							continue;
						}

					DataAdapter da = new DataAdapter();
					da.dataID = userCompany.userID;
					// user锟斤拷锟皆斤拷锟斤拷锟斤拷锟斤拷锟斤拷companyid锟斤拷userid,锟斤拷锟斤拷使锟斤拷锟斤拷锟斤拷锟�
					da.parentDeptID = userCompany.deptID;
					da.companyID = userCompany.companyID;
					da.dataName = Utilities.formatName(userCompany.first_name,
							userCompany.last_name);
					TLRPC.User user = users.get(userCompany.userID);
					if (user != null) {
						da.dataICO = user.userico;
						da.dataName = StringUtil.getCompanyUserRemark(user,
								da.dataName);
					}
					da.isCompany = false;
					da.haveChild = false;
					da.isUser = true;
					
					/*//中文比较 
					Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);    
					List<DataAdapter> list = Arrays.asList(da);  
				        Collections.sort(list, com); 
				        for(DataAdapter i:list){    
				        	 System.out.println("Dataadapter 循环时间"+i);
				        }
//					        }
*///				        SourceDateListTwo = filledDataTwo(list) ;  
//					    SourceDateList = filledData(SourceDateListTwo) ;
				        
					String pinyin = CharacterParser.getInstance().getSelling(
							da.dataName);
					String sortString = "";
					if (!StringUtil.isEmpty(pinyin)) {
						sortString = pinyin.substring(0, 1).toUpperCase();
					}

					if (sortString.matches("[A-Z]")) {
						da.sortLetters = sortString;
					} else {
						da.sortLetters = "#";
					}
					arrData.add(da);
				}
			}
			// wangxm add 锟斤拷锟斤拷
			Collections.sort(arrData, compare);
		}
	}

	public void LoadDepts(int companyid, ArrayList<DataAdapter> arrData) {
		// 目前锟斤拷锟捷癸拷锟斤拷锟斤拷depid锟斤拷示锟矫伙拷锟斤拷锟斤拷某锟斤拷锟斤拷锟脚ｏ拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷碌锟斤拷硬锟斤拷偶锟斤拷锟斤拷锟斤拷锟斤拷锟铰碉拷锟斤拷员锟斤拷息
		for (ConcurrentHashMap.Entry<Integer, TLRPC.TL_DepartMent> entry : this.departments
				.entrySet()) {
			TLRPC.TL_DepartMent dept = entry.getValue();
			DataAdapter da = new DataAdapter();
			da.dataID = dept.id;
			da.parentDeptID = dept.deptParentID;
			da.companyID = dept.companyID;
			da.dataName = dept.name;
			da.dataICO = "";
			da.isCompany = false;
			da.version = dept.version;
			da.haveChild = true;
			da.isUser = false;
			arrData.add(da);

		}
	}

	public void loadUsers(ArrayList<DataAdapter> arrData, boolean bIncludeMe) {
		 System.out.println("arrData--------77--"+arrData);
		for (ConcurrentHashMap.Entry<Integer, TLRPC.User> entry : users
				.entrySet()) {
			TLRPC.User user = entry.getValue();
			// 锟斤拷示锟斤拷锟斤拷没锟斤拷锟矫伙拷邪锟阶帮拷锟酵拷锟斤拷锟诫公司,EMAIL锟斤拷也要锟斤拷锟斤拷锟斤拷ucstate锟斤拷状态
			if (!queryUserState(user.id))
				continue;
			if (UserConfig.clientUserId == user.id && !bIncludeMe)
				continue;
			DataAdapter da = new DataAdapter();
			da.dataID = user.id;
			// user锟斤拷锟皆斤拷锟斤拷锟斤拷锟斤拷锟斤拷companyid锟斤拷userid,锟斤拷锟斤拷使锟斤拷锟斤拷锟斤拷锟�
			da.parentDeptID = 0;
			da.companyID = 0;

			da.dataName = Utilities.formatName(user);

			da.dataICO = user.userico;
			da.isCompany = false;
			da.haveChild = false;
			da.isUser = true;
			String pinyin = CharacterParser.getInstance().getSelling(
					da.dataName);
			String sortString = pinyin.substring(0, 1).toUpperCase();
			if (sortString.matches("[A-Z]")) {
				da.sortLetters = sortString;
			} else {
				da.sortLetters = "#";
			}
			arrData.add(da);
		}
		// wangxm add 锟斤拷锟斤拷
		Collections.sort(arrData, compare);
	}

	// 锟斤拷锟斤拷锟斤拷锟街伙拷通讯路锟斤拷锟窖撅拷锟斤拷装锟斤拷微锟斤拷锟斤拷没锟�
	public void loadMyContacts(ArrayList<DataAdapter> arrData) 
	{
		System.out.println("arrData--------22--"+arrData);
//		Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);    
//	    Collections.sort(arrData, com);   
		for (ConcurrentHashMap.Entry<Integer, TLRPC.User> entry : users
				.entrySet()) {
			TLRPC.User user = entry.getValue();
			DataAdapter da = new DataAdapter();
			if(user.id == UserConfig.clientUserId){
				continue;
			}
			//锟斤拷锟斤拷锟斤拷陌锟斤拷锟斤拷
			if(user.serverid == -1)
				continue;
			da.dataID = user.id;
			// user锟斤拷锟皆斤拷锟斤拷锟斤拷锟斤拷锟斤拷companyid锟斤拷userid,锟斤拷锟斤拷使锟斤拷锟斤拷锟斤拷锟�
			da.parentDeptID = 0;
			da.companyID = 0;
			da.dataName = Utilities.formatName(user);
			da.dataICO = user.userico;
			da.isCompany = false;
			da.haveChild = false;
			da.isUser = true;
			String pinyin = CharacterParser.getInstance().getSelling(
					da.dataName);
			String sortString = pinyin.substring(0, 1).toUpperCase();
			if (sortString.matches("[A-Z]")) {
				da.sortLetters = sortString;
			} else {
				da.sortLetters = "#";
			}
			arrData.add(da);
		}
		// wangxm add 锟斤拷锟斤拷
		Collections.sort(arrData, compare);
	}

	public void LoadCompanyData(ArrayList<DataAdapter> arrData) {
		 System.out.println("arrData--------88--"+arrData);
		for (ConcurrentHashMap.Entry<Integer, TLRPC.TL_Company> entry : companys
				.entrySet()) {
			// entry.getKey();
			TLRPC.TL_Company company = entry.getValue();
			DataAdapter da = new DataAdapter();
			da.dataID = company.id;
			da.parentDeptID = company.rootdeptid;
			da.companyID = company.id;
			da.dataName = company.name;
			da.dataICO = company.ico;
			da.isCompany = true;
			da.version = company.version;
			da.haveChild = true;// haveChild(da.dataID,da.isCompany);
			da.isUser = false;
			arrData.add(da);
		}

	}

	public ArrayList<Integer> getCompany() {
		ArrayList<Integer> temp = new ArrayList<Integer>();
		for (ConcurrentHashMap.Entry<Integer, TLRPC.TL_Company> entry : companys
				.entrySet()) {
			temp.add(entry.getKey());
		}
		return temp;
	}

	public String GetCompanyName(int id) {
		if (companys.get(id) != null)
			return companys.get(id).name;
		return "";
	}

	public String GetDepartName(int id) {
		TLRPC.TL_DepartMent dept = departments.get(id);
		if (dept != null)
			return dept.name;
		return "";
	}

	public String GetCompanyName() {
		ArrayList<Integer> temp = new ArrayList<Integer>();
		for (ConcurrentHashMap.Entry<Integer, TLRPC.TL_Company> entry : companys
				.entrySet()) {
			TLRPC.TL_Company company = entry.getValue();
			return company.name;
		}
		return "";
	}

	public void processLoadChnInfo(final ArrayList<TLRPC.TL_ChannalInfo> infos,
			final int from) {
		Utilities.RunOnUIThread(new Runnable() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				if (from == 1) {
					// 锟饺加憋拷锟截碉拷锟斤拷息
					localChnInfos = (ArrayList<TLRPC.TL_ChannalInfo>) infos
							.clone();
					localInfoLoaded = true;
					// 锟斤拷证锟斤拷锟斤拷一锟斤拷getUpdate锟斤拷取锟斤拷员锟斤拷息锟斤拷锟斤拷锟斤拷息锟斤拷锟斤拷锟斤拷锟斤拷息锟斤拷锟斤拷channinfo
					Utilities.stageQueue.postRunnable(new Runnable() {
						@Override
						public void run() 
						{
							//锟斤拷锟斤拷注锟斤拷晒锟斤拷螅锟紻B锟斤拷然锟斤拷LOAD锟斤拷锟斤拷锟斤拷锟捷ｏ拷然锟斤拷锟斤拷锟斤拷锟较伙拷取锟斤拷锟斤拷
							//ConnectionsManager.getInstance().getUpdate();
							//ConnectionsManager.getInstance().gettime();
							//getupdate after startnetwork
							//xueqiang change
							ConnectionsManager.getInstance().StartNetWorkService();
							ConnectionsManager.getInstance().updateLocations();
						}
					});
				} else if (from == 0) {
					// 锟斤拷锟斤拷getupdate,锟斤拷取锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷息锟斤拷然锟斤拷捅锟斤拷乇冉锟�
					remoteChnInfos = (ArrayList<TLRPC.TL_ChannalInfo>) infos
							.clone();
				}
				if (remoteChnInfos.size() >= 0 && localChnInfos.size() > 0
						&& from == 0) {
					// 锟斤拷锟截碉拷应锟矫达拷远锟教碉拷小
					for (TLRPC.TL_ChannalInfo localInfo : localChnInfos) {
						boolean bFound = false;
						int type = localInfo.type;
						int chnid = localInfo.channelid;
						for (TLRPC.TL_ChannalInfo remoteInfo : remoteChnInfos) {
							if (chnid == remoteInfo.channelid
									&& type == remoteInfo.type) {
								bFound = true;
								continue;
							}
						}
						if (!bFound) {
							// 锟斤拷锟斤拷锟截斤拷锟斤拷息锟斤拷锟竭ｏ拷锟斤拷锟节存及DB锟斤拷删锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷司锟斤拷息锟斤拷锟斤拷锟斤拷息锟斤拷锟斤拷锟斤拷锟斤拷息
							// type:1 锟斤拷示锟斤拷司锟斤拷2锟斤拷示锟介，3锟斤拷示锟斤拷锟斤拷
							// chanid锟街憋拷锟斤拷锟剿緄d,锟斤拷id,锟酵伙拷锟斤拷id
							if (type == 1) {
								// 锟斤拷锟饺达拷锟节达拷锟絛b锟斤拷删锟斤拷锟斤拷司

								companys.remove(chnid);
								MessagesStorage.getInstance().deleteCompany(
										chnid);

								ArrayList<Integer> deptList = new ArrayList<Integer>();
								for (ConcurrentHashMap.Entry<Integer, TLRPC.TL_DepartMent> entry : departments
										.entrySet()) {
									TLRPC.TL_DepartMent dept = entry.getValue();
									if (dept.companyID == chnid) {
										HashSet<TLRPC.TL_UserCompany> deptUsers = departidToUsers
												.get(dept.id);
										if (deptUsers != null) {
											Iterator<TLRPC.TL_UserCompany> it = deptUsers
													.iterator();
											while (it.hasNext()) {
												TLRPC.TL_UserCompany userCompany = it
														.next();
												MessagesStorage
												.getInstance()
												.deleteUserCompany(
														userCompany.userID,
														userCompany.companyID);
											}
										}
										// 删锟斤拷锟斤拷司锟侥诧拷锟脚达拷锟节达拷锟紻B锟斤拷
										departidToUsers.remove(dept.id);
										MessagesStorage.getInstance()
										.deleteDepartment(dept.id);
										// 锟斤拷锟斤拷要删锟斤拷锟侥癸拷司锟斤拷应锟侥诧拷锟斤拷id
										deptList.add(dept.id);
									}
								}
								for (Integer deptid : deptList) {
									departments.remove(deptid);
								}
								ArrayList<String> userCompanyList = new ArrayList<String>();
								for (ConcurrentHashMap.Entry<String, TLRPC.TL_UserCompany> userCompanyEntry : userCompanysMap
										.entrySet()) {
									TLRPC.TL_UserCompany userCompany = userCompanyEntry
											.getValue();
									if (userCompany != null
											&& userCompany.companyID == chnid) {
										userCompanyList.add(userCompanyEntry
												.getKey());
									}
								}
								for (int h = 0; h < userCompanyList.size(); h++) {
									userCompanysMap.remove(userCompanyList
											.get(h));
								}
								// NotificationCenter.getInstance().postNotificationName(MessagesController.contactsDidLoaded);
							} else if (type == 2) {
								// 锟斤拷锟皆硷拷锟斤拷锟斤拷锟斤拷锟睫筹拷锟斤拷锟斤拷锟斤拷锟节凤拷锟斤拷锟斤拷息锟剿ｏ拷锟斤拷锟斤拷锟皆匡拷锟斤拷锟斤拷前锟斤拷锟斤拷息
								TLRPC.Chat chat = chats.get(chnid);
								if (chat != null) {
									if (chat.innerChat) {
										deleteDialog(-chnid, 0, false);
									} else {
										chat.participants_count -= 1;
										chat.left = true;
										TLRPC.TL_updateNewMessage update = new TLRPC.TL_updateNewMessage();

										// 锟斤拷锟届创锟斤拷锟斤拷锟斤拷锟较�
										TLRPC.TL_messageService message = new TLRPC.TL_messageService();

										TLRPC.TL_peerChat peer = new TLRPC.TL_peerChat();
										peer.chat_id = chnid;
										message.id = UserConfig.getSeq();// 锟斤拷证id唯一
										message.from_id = UserConfig.clientUserId;
										message.to_id = peer;
										message.out = false;
										message.unread = true;

										// 只锟叫达拷锟斤拷锟斤拷锟绞憋拷锟侥憋拷锟斤拷锟绞憋拷锟�,xueqiang todo..
										message.date = ConnectionsManager
												.getInstance().getCurrentTime();
										TLRPC.TL_updates updates = new TLRPC.TL_updates();
										TLRPC.MessageAction action = null;
										// 删锟斤拷锟斤拷锟矫伙拷
										action = new TLRPC.TL_messageActionChatDeleteUser();
										// 锟斤拷锟斤拷锟斤拷锟矫伙拷删锟斤拷锟斤拷锟斤拷息
										TLRPC.Update delUpdate = new TLRPC.TL_updateChatParticipantDelete();
										delUpdate.chat_id = chnid;
										delUpdate.user_id = UserConfig.clientUserId;
										updates.updates.add(delUpdate);
										action.user_id = delUpdate.user_id;
										updates.chats.add(chat);

										action.title = chat.title;
										message.action = action;
										message.from_id = UserConfig.clientUserId;

										update.message = message;
										updates.updates.add(update);
										processUpdates(updates, false);
									}
									// MessagesStorage.getInstance().updateChatInfo(chat.id,
									// UserConfig.clientUserId, true, 0, 0);

								}
							} else if (type == 3) {
								// 锟斤拷锟斤拷锟斤拷锟斤拷锟较⒁拷颖锟斤拷锟缴撅拷锟斤拷锟� //qxm change
								meetings.remove(chnid);
								TLRPC.TL_MeetingInfo info = new TLRPC.TL_MeetingInfo();
								if(info.meetingType == 11 || info.meetingType == 12 || info.meetingType ==13 || info.meetingType ==14){
									for (TLRPC.TL_MeetingInfo mInfo : broadcastMeetingList) {
										if (mInfo.mid == chnid) {
											broadcastMeetingList.remove(mInfo);
											break;
										}
									}
									MessagesStorage.getInstance().deleteMeeting(chnid);
									sortDirects();
								}else{
									for (TLRPC.TL_MeetingInfo mInfo : meetingList) {
										if (mInfo.mid == chnid) {
											meetingList.remove(mInfo);
											break;
										}
									}
									MessagesStorage.getInstance().deleteMeeting(chnid);
									sortMeetings();
								}
								NotificationCenter.getInstance()
								.postNotificationName(
										meeting_list_update, chnid);
								scheduleNextAlert(chnid, false);

							}
						}
					}
				}
				if (from == 0) {
					MessagesStorage.getInstance().clearChaninfo();
					MessagesStorage.getInstance().updateChaninfo(infos);
					localChnInfos = (ArrayList<TLRPC.TL_ChannalInfo>) infos
							.clone();
				}
			}
		});

	}

	private void deleteMeetingFromLocal(int mid,ArrayList<TLRPC.TL_MeetingInfo> ml) 
	{
		if(meetings.get(mid)==null)
			return;
		MessagesStorage.getInstance().deleteMeeting(mid);
		meetings.remove(mid);
		int meetingType = 0;
		for (int k = 0; k < ml.size(); k++) {
			if (ml.get(k).mid == mid) {
				meetingType = ml.get(k).meetingType;
				ml.remove(k);
				break;
			}
		}
		//qxm change 
		if(meetingType == 11 || meetingType == 12 || meetingType == 13 || meetingType == 14){
			sortDirects();
			NotificationCenter.getInstance().postNotificationName(direct_list_delete, mid);
		}else{
			sortMeetings();
			MessagesController.getInstance().scheduleNextAlert(mid, false);
			NotificationCenter.getInstance().postNotificationName(meeting_list_delete, mid);
		}
	}

	private void innerProcessLoadMeetings(TLRPC.TL_MeetingInfo info,final int from,ArrayList<TLRPC.TL_MeetingInfo> ml)
	{
		int time = info.endTime;
		int currentTime = ConnectionsManager.getInstance().getCurrentTime();
		Date date = new Date(time);
		Date date2 = new Date(currentTime);
		SimpleDateFormat formatTime = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String StartTime = formatTime.format(date);
		String currentTimes  = formatTime.format(date2);

		//todo..
		if(info.meetingType == 3 || info.meetingType == 4 || info.meetingType == 5 || info.meetingType == 6){//锟斤拷锟斤拷
			if (from == 0)// 锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷幕锟斤拷锟斤拷锟较�
			{
				// 锟斤拷始锟斤拷锟斤拷锟斤拷时锟斤拷锟斤拷锟斤拷锟斤拷锟截碉拷锟斤拷息锟斤拷锟斤拷一锟街匡拷锟斤拷锟斤拷锟斤拷锟剿伙拷执锟斤拷锟睫革拷锟剿伙拷锟斤拷锟斤拷息锟斤拷也锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
				TLRPC.User host = users.get(info.createid);
				//if (host == null || !info.participants.contains(UserConfig.clientUserId))
				if (info.state==1)
				{
					// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷肟拷耍锟斤拷锟揭撅拷锟斤拷锟斤拷锟斤拷锟介，锟斤拷锟斤拷锟侥筹拷锟斤拷锟斤拷锟斤拷锟剿诧拷锟斤拷锟剿ｏ拷应锟斤拷锟节伙拷锟斤拷锟叫斤拷锟斤拷锟斤拷锟缴撅拷锟�
					deleteMeetingFromLocal(info.mid,ml);
				}
				else 
				{
					if (meetings.get(info.mid) != null) {
						for (int j = 0; j < ml.size(); j++) {
							if (ml.get(j).mid == info.mid) {
								ml.remove(j);
								break;
							}
						}
						meetings.put(info.mid, info);
						ml.add(info);
						MessagesStorage.getInstance().updateMeeting(info);
						//NotificationCenter.getInstance().postNotificationName(meeting_list_update,info.mid);
						sortMeetings();
						sortDirects();
						MessagesController.getInstance()
						.scheduleNextAlert(info.mid, true);
					} else {
						meetings.put(info.mid, info);
						ml.add(info);
						MessagesStorage.getInstance().updateMeeting(info);
						Log.e("emm", "processLoadMeetings********************");
						//NotificationCenter.getInstance().postNotificationName(meeting_list_update,info.mid);
						sortMeetings();
						sortDirects();
						MessagesController.getInstance().scheduleNextAlert(info.mid, true);
					}
				}
			}
			else 
			{
				//锟斤拷锟皆憋拷锟斤拷DB
				if (meetings.get(info.mid) == null) 
				{	
					meetings.put(info.mid, info);
					ml.add(info);
					MessagesStorage.getInstance().updateMeeting(info);								
					sortMeetings();
					sortDirects();
					MessagesController.getInstance().scheduleNextAlert(info.mid, true);
				}
			}
		}else{
			if(info.endTime < ConnectionsManager.getInstance().getCurrentTime() && info.endTime != 0){//锟斤拷锟剿碉拷锟斤拷锟节的伙拷锟斤拷  
				ml.remove(info);
			}else{
				if (from == 0)// 锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷幕锟斤拷锟斤拷锟较�
				{
					// 锟斤拷始锟斤拷锟斤拷锟斤拷时锟斤拷锟斤拷锟斤拷锟斤拷锟截碉拷锟斤拷息锟斤拷锟斤拷一锟街匡拷锟斤拷锟斤拷锟斤拷锟剿伙拷执锟斤拷锟睫革拷锟剿伙拷锟斤拷锟斤拷息锟斤拷也锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
					TLRPC.User host = users.get(info.createid);
					//if (host == null || !info.participants.contains(UserConfig.clientUserId))
					if (info.state==1)
					{
						// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷肟拷耍锟斤拷锟揭撅拷锟斤拷锟斤拷锟斤拷锟介，锟斤拷锟斤拷锟侥筹拷锟斤拷锟斤拷锟斤拷锟剿诧拷锟斤拷锟剿ｏ拷应锟斤拷锟节伙拷锟斤拷锟叫斤拷锟斤拷锟斤拷锟缴撅拷锟�
						deleteMeetingFromLocal(info.mid,ml);
					}
					else 
					{
						if (meetings.get(info.mid) != null) {
							for (int j = 0; j < ml.size(); j++) {
								if (ml.get(j).mid == info.mid) {
									ml.remove(j);
									break;
								}
							}
							meetings.put(info.mid, info);
							ml.add(info);
							MessagesStorage.getInstance().updateMeeting(info);
							//NotificationCenter.getInstance().postNotificationName(meeting_list_update,info.mid);
							sortMeetings();
							sortDirects();
							MessagesController.getInstance()
							.scheduleNextAlert(info.mid, true);
						} else {
							meetings.put(info.mid, info);
							ml.add(info);
							MessagesStorage.getInstance().updateMeeting(info);
							Log.e("emm", "processLoadMeetings********************");
							//NotificationCenter.getInstance().postNotificationName(meeting_list_update,info.mid);
							sortMeetings();
							sortDirects();
							MessagesController.getInstance().scheduleNextAlert(info.mid, true);
						}
					}
				}
				else 
				{
					//锟斤拷锟皆憋拷锟斤拷DB
					if (meetings.get(info.mid) == null) 
					{	
						meetings.put(info.mid, info);
						ml.add(info);
						MessagesStorage.getInstance().updateMeeting(info);								
						sortMeetings();
						sortDirects();
						MessagesController.getInstance().scheduleNextAlert(info.mid, true);
					}
				}
			}
		}
	}
	// 锟斤拷取锟斤拷锟斤拷锟叫憋拷颖锟斤拷锟紻B锟叫硷拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷匣锟饺★拷碌锟斤拷斜锟�
	public void processLoadMeetings(
			final ArrayList<TLRPC.TL_MeetingInfo> infos, final int from) {
		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() 
			{
				if(infos!=null){
					for (int i = 0; i < infos.size(); i++) 
					{
						TLRPC.TL_MeetingInfo info = infos.get(i);
						if(info.meetingType==11|| info.meetingType==13 || info.meetingType==12)//直锟斤拷
							innerProcessLoadMeetings(info,from,broadcastMeetingList);
						else if(info.meetingType!=14)
							innerProcessLoadMeetings(info,from,meetingList);
						if(info.mid == 729034583){
							Log.e("emm", "info"+info.mid);
						}
					}
				}
				NotificationCenter.getInstance().postNotificationName(getall_meeting);
			}
		});
	}

	private TLRPC.TL_MeetingInfo getMyDirectMeeting(){ 
		TLRPC.TL_MeetingInfo myDirectMeeting = null;
		for (int i = 0; i < broadcastMeetingList.size(); i++) {
			if(MessagesController.getInstance().broadcastMeetingList.get(i).createid==UserConfig.clientUserId){
				myDirectMeeting = broadcastMeetingList.get(i);
			}
		}
		return myDirectMeeting;
	}
	/**
	 * 锟斤拷锟斤拷锟叫憋拷锟斤拷锟斤拷锟�
	 */
	private void sortMeetings() {
		// 锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷失锟杰ｏ拷锟斤拷锟斤拷锟斤拷
		/*
		 * Utilities.RunOnUIThread(new Runnable() {
		 * 
		 * @Override public void run() {
		 * //锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷锟叫ｏ拷锟斤拷要锟斤拷starttime锟斤拷锟斤拷int值锟斤拷锟斤拷锟秸匡拷始时锟斤拷锟斤拷锟叫ｏ拷同时锟斤拷锟斤拷锟斤拷薷锟斤拷锟揭拷锟饺⊥ㄖ�
		 * //锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷息锟斤拷锟斤拷锟剿ｏ拷通讯协锟斤拷锟角凤拷锟叫点复锟接ｏ拷锟斤拷要锟侥斤拷锟铰ｏ拷太锟介烦锟剿ｏ拷一锟斤拷锟斤拷一锟斤拷锟侥ｏ拷 //xueqiang todo..
		 * Collections.sort(meetingList, new Comparator<TLRPC.TL_MeetingInfo>(){
		 * 
		 * @Override public int compare(TLRPC.TL_MeetingInfo tl_dialog,
		 * TLRPC.TL_MeetingInfo tl_dialog2) { if (tl_dialog.starttime ==
		 * tl_dialog2.starttime) { return 0; } else if
		 * (tl_dialog.starttime<tl_dialog2.starttime) { return 1; } else {
		 * return -1; } } }); } });
		 */
		// 锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷锟叫ｏ拷锟斤拷要锟斤拷starttime锟斤拷锟斤拷int值锟斤拷锟斤拷锟秸匡拷始时锟斤拷锟斤拷锟叫ｏ拷同时锟斤拷锟斤拷锟斤拷薷锟斤拷锟揭拷锟饺⊥ㄖ�
		// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷息锟斤拷锟斤拷锟剿ｏ拷通讯协锟斤拷锟角凤拷锟叫点复锟接ｏ拷锟斤拷要锟侥斤拷锟铰ｏ拷太锟介烦锟剿ｏ拷一锟斤拷锟斤拷一锟斤拷锟侥ｏ拷
		// xueqiang todo..
		Collections.sort(meetingList, new Comparator<TLRPC.TL_MeetingInfo>() {
			@Override
			public int compare(TLRPC.TL_MeetingInfo tl_dialog,
					TLRPC.TL_MeetingInfo tl_dialog2) {
				if (tl_dialog.startTime == tl_dialog2.startTime) {
					return 0;
				} else if (tl_dialog.startTime < tl_dialog2.startTime) {
					return 1;
				} else {
					return -1;
				}
			}
		});
	}
	private void sortDirects() {
		Collections.sort(broadcastMeetingList, new Comparator<TLRPC.TL_MeetingInfo>() {
			@Override
			public int compare(TLRPC.TL_MeetingInfo tl_dialog,
					TLRPC.TL_MeetingInfo tl_dialog2) {
				if (tl_dialog.startTime == tl_dialog2.startTime) {
					return 0;
				} else if (tl_dialog.startTime < tl_dialog2.startTime) {
					return 1;
				} else {
					return -1;
				}
			}
		});
	}

	/**
	 * 锟斤拷锟介创锟斤拷锟缴癸拷之锟斤拷锟饺★拷锟斤拷锟斤拷锟斤拷息
	 * @param req
	 */
	public void CreateMeeting(final TLRPC.TL_MeetingInfo req) {
		ConnectionsManager.getInstance().CreateMeeting((TLObject) req,
				new RPCRequest.RPCRequestDelegate() {
			@Override
			public void run(TLObject response, TLRPC.TL_error error) {
				if (error != null) {
					if (error.code == 1 || error.code == -1) {
						// 锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟节诧拷锟斤拷锟矫ｏ拷锟斤拷锟皆猴拷锟斤拷锟斤拷
						Utilities.RunOnUIThread(new Runnable() { 
							@Override
							public void run() {
								NotificationCenter.getInstance()
								.postNotificationName(
										meeting_create_failed);
							}
						});
					}
				} else {
					TLRPC.TL_UpdateMeetingResult result = (TLRPC.TL_UpdateMeetingResult) response;
					final int mid = result.mid;
					if (mid < 0) {
						// 锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷失锟杰ｏ拷锟斤拷锟斤拷锟斤拷
						Utilities.RunOnUIThread(new Runnable() {
							@Override
							public void run() {
								NotificationCenter.getInstance()
								.postNotificationName(
										meeting_create_failed);
							}
						});

					} else {
						Utilities.RunOnUIThread(new Runnable() {
							@Override
							public void run() 
							{	
								final TLRPC.TL_MeetingInfo info = new TLRPC.TL_MeetingInfo();
								info.mid = mid;										
								info.createid = req.createid;
								info.startTime = req.startTime;
								info.endTime = req.endTime;
								info.topic = req.topic;
								info.chairmanpwd = req.chairmanpwd;
								info.confuserpwd = req.confuserpwd;
								info.sidelineuserpwd = req.sidelineuserpwd;
								//qxm add
								info.meetingType =req.meetingType;
								info.ispublicMeeting = req.ispublicMeeting;
								info.beginTime = req.beginTime;
								info.duration = req.duration;
								info.meetingsubject = req.meetingsubject;
								Iterator<Integer> it = req.participants
										.iterator();
								while (it.hasNext())
								{
									int  peerid= it.next();
									info.participants.add(peerid);
									//锟斤拷锟斤拷 %s 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷    锟斤拷锟斤拷锟斤拷某锟皆憋拷锟斤拷锟较�(锟轿加伙拷锟斤拷牡锟街�)
									String strShare = String.format("%s/%d/%s",Config.getWebHttp(), mid,"");			
									String strFinal = String.format(ApplicationLoader.getContext().getString(R.string.share_string),strShare);								
									sendMessage(strFinal,peerid);
								}
								info.resParticipants
								.add(UserConfig.clientUserId);
								if(info.meetingType == 11 || info.meetingType == 12 || info.meetingType == 13||info.meetingType==14){
									meetings.put(info.mid, info);
									broadcastMeetingList.add(info);
									MessagesStorage.getInstance().updateMeeting(info);
									sortDirects();
								}else{
									//									meetingList.add(info);
									//									sortMeetings();

									if (meetings.get(info.mid) != null) {
										for (int j = 0; j < meetingList.size(); j++) {
											if (meetingList.get(j).mid == info.mid) {
												meetingList.remove(j);
												break;
											}
										}
										meetings.put(info.mid, info);
										meetingList.add(info);
										sortMeetings();
										sortDirects();
										MessagesController.getInstance().scheduleNextAlert(info.mid, true);
									} else {
										meetings.put(info.mid, info);
										meetingList.add(info);
										MessagesStorage.getInstance().updateMeeting(info);
										sortMeetings();
									}
								}

								if(info.meetingType != 11 && info.meetingType != 12 && info.meetingType != 13&&info.meetingType!=14){									
									MessagesController.getInstance().scheduleNextAlert(info.mid,true);
								}
								NotificationCenter.getInstance().postNotificationName(meeting_list_update,mid);
							}
						});
					}    
				}
			}
		});
	}
	public void DeleteMeeting(final int mid,final int meetingType) {
		ConnectionsManager.getInstance().deleteMeeting(mid,
				new RPCRequest.RPCRequestDelegate() {
			@SuppressLint("DefaultLocale")
			@Override
			public void run(TLObject response, TLRPC.TL_error error) {
				if (error != null) {
					if (error.code == -1) {
						// 锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟节诧拷锟斤拷锟矫ｏ拷锟斤拷锟皆猴拷锟斤拷锟斤拷
						Utilities.RunOnUIThread(new Runnable() {
							@Override
							public void run() {//qxm change
								if(meetingType == 11 || meetingType == 12 || meetingType == 13 || meetingType == 14)
									NotificationCenter.getInstance().postNotificationName(direct_list_delete, -1);
								else	
									NotificationCenter.getInstance().postNotificationName(meeting_list_delete, -1);
							}
						});

					} else if (error.code == 1) {
						// 锟斤拷示锟斤拷锟斤拷锟斤拷锟捷匡拷失锟杰ｏ拷锟斤拷锟皆猴拷锟斤拷锟斤拷
						// 锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟节诧拷锟斤拷锟矫ｏ拷锟斤拷锟皆猴拷锟斤拷锟斤拷
						Utilities.RunOnUIThread(new Runnable() {
							@Override
							public void run() {//qxm change
								if(meetingType == 11 || meetingType == 12 || meetingType == 13 || meetingType == 14)
									NotificationCenter.getInstance().postNotificationName(direct_list_delete, -1);
								else	
									NotificationCenter.getInstance().postNotificationName(meeting_list_delete, -1);
							}
						});

					}
				} else {
					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() {
							// NotificationCenter.getInstance().postNotificationName(meeting_list_updateUI);
							MessagesController.getInstance().scheduleNextAlert(mid, false);
							if(meetingType == 11 || meetingType == 12 || meetingType == 13 || meetingType == 14){
								broadcastMeetingList.remove(meetings.get(mid));
								meetings.remove(mid);
								MessagesStorage.getInstance().deleteMeeting(mid);
								sortDirects();
								NotificationCenter.getInstance().postNotificationName(direct_list_delete, mid);
							}else{
//								String yyyyMMddE = DateUnit.getMMDDDate(meetings.get(mid).startTime);
//								ArrayList<TL_MeetingInfo> list = meetingMap.get(yyyyMMddE);
//								for(int i = 0;i<list.size();i++){
//									if(list.get(i).mid == mid){
//										list.remove(i);
//									}
//								}
								meetingList.remove(meetings.get(mid));
								meetings.remove(mid);
								MessagesStorage.getInstance().deleteMeeting(mid);
								sortMeetings();
								NotificationCenter.getInstance().postNotificationName(meeting_list_delete, mid);
							}
						}
					});
				}
			}
		});
	}

	public void updateMeeting(final TLRPC.TL_MeetingInfo req) {
		ConnectionsManager.getInstance().updateMeeting(req,
				new RPCRequest.RPCRequestDelegate() {
			@Override
			public void run(TLObject response, TLRPC.TL_error error) {
				if (error != null) {
					if (error.code == -1) {
						// 锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟节诧拷锟斤拷锟矫ｏ拷锟斤拷锟皆猴拷锟斤拷锟斤拷
						Utilities.RunOnUIThread(new Runnable() {
							@Override
							public void run() {
								NotificationCenter
								.getInstance()
								.postNotificationName(
										meeting_list_update, -1);
							}
						});

					} else if (error.code == 1) {
						// 锟斤拷示锟斤拷锟斤拷锟斤拷锟捷匡拷失锟杰ｏ拷锟斤拷锟皆猴拷锟斤拷锟斤拷
						// 锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟节诧拷锟斤拷锟矫ｏ拷锟斤拷锟皆猴拷锟斤拷锟斤拷
						Utilities.RunOnUIThread(new Runnable() {
							@Override
							public void run() {
								NotificationCenter.getInstance()
								.postNotificationName(
										meeting_list_update, 1);
							}
						});

					}
				} else {
					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() {
							MessagesStorage.getInstance()
							.updateMeeting(req);
							meetings.put(req.mid, req);//qxm change

							TLRPC.TL_MeetingInfo mInfo = new TLRPC.TL_MeetingInfo();
							if(mInfo.meetingType == 11 || mInfo.meetingType == 12 || mInfo.meetingType == 13 ||  mInfo.meetingType == 14){
								for (TLRPC.TL_MeetingInfo info : broadcastMeetingList) {
									if (info.mid == req.mid) {
										broadcastMeetingList.remove(info);
										break;
									}
								}
								broadcastMeetingList.add(req);
								sortDirects();
							}else{
								for (TLRPC.TL_MeetingInfo info : meetingList) {
									if (info.mid == req.mid) {
										meetingList.remove(info);
										break;
									}
								}
								meetingList.add(req);
								sortMeetings();
							}
							MessagesController.getInstance()
							.scheduleNextAlert(req.mid, true);
							NotificationCenter
							.getInstance()
							.postNotificationName(
									meeting_list_update, req.mid);
						}
					});

				}
			}
		});
	}

	public void acceptMeeting(final TLRPC.TL_MeetingInfo req, final int accetpid) {
		ConnectionsManager.getInstance().acceptMeeting(req, accetpid,
				new RPCRequest.RPCRequestDelegate() {
			@Override
			public void run(TLObject response, TLRPC.TL_error error) {
				if (error != null) {
					if (error.code == -1) {
						// 锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟节诧拷锟斤拷锟矫ｏ拷锟斤拷锟皆猴拷锟斤拷锟斤拷
						Utilities.RunOnUIThread(new Runnable() {
							@Override
							public void run() {
								// NotificationCenter.getInstance().postNotificationName(meeting_list_update,-1);
							}
						});
					} else if (error.code == 1) {
						// 锟斤拷示锟斤拷锟斤拷锟斤拷锟捷匡拷失锟杰ｏ拷锟斤拷锟皆猴拷锟斤拷锟斤拷
						// 锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟节诧拷锟斤拷锟矫ｏ拷锟斤拷锟皆猴拷锟斤拷锟斤拷
						Utilities.RunOnUIThread(new Runnable() {
							@Override
							public void run() {
								// NotificationCenter.getInstance().postNotificationName(meeting_list_update,1);
							}
						});
					}
				} else {
					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() {
							req.resParticipants.add(accetpid);
							MessagesStorage.getInstance()
							.updateMeeting(req);
							sortMeetings();
							NotificationCenter
							.getInstance()
							.postNotificationName(
									MessagesController.unread_message_update);
						}
					});

				}
			}
		});
	}

	public void ChangeMeeting(final TLRPC.TL_MeetingChange mc) {
		ConnectionsManager.getInstance().changeMeeting(mc,
				new RPCRequest.RPCRequestDelegate() {
			@Override
			public void run(TLObject response, TLRPC.TL_error error) {
				if (error != null) {
					if (error.code == -1) {
						// 锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟节诧拷锟斤拷锟矫ｏ拷锟斤拷锟皆猴拷锟斤拷锟斤拷
						Utilities.RunOnUIThread(new Runnable() {
							@Override
							public void run() {
								NotificationCenter
								.getInstance()
								.postNotificationName(
										meet_member_operator_error,
										-1);
							}
						});

					} else if (error.code == 1) {
						// 锟斤拷示锟斤拷锟斤拷锟斤拷锟捷匡拷失锟杰ｏ拷锟斤拷锟皆猴拷锟斤拷锟斤拷
						// 锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟节诧拷锟斤拷锟矫ｏ拷锟斤拷锟皆猴拷锟斤拷锟斤拷
						Utilities.RunOnUIThread(new Runnable() {
							@Override
							public void run() {
								NotificationCenter
								.getInstance()
								.postNotificationName(
										meet_member_operator_error,
										1);
							}
						});

					}
				} else {
					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() {
							for (int i = 0; i < mc.users.size(); i++) {
								int mid = mc.mid;
								int act = mc.action;
								int uid = mc.users.get(i);
								if (uid == UserConfig.clientUserId) {
									// 锟斤拷锟斤拷约锟缴撅拷锟斤拷嘶锟斤拷椋拷锟斤拷锟斤拷植锟斤拷锟斤拷锟斤拷锟斤拷耍锟斤拷锟斤拷锟轿达拷锟斤拷
									meetingList.remove(meetings.get(mid));
									meetings.remove(mid);
									MessagesStorage.getInstance()
									.deleteMeeting(mid);
									sortMeetings();
									NotificationCenter
									.getInstance()
									.postNotificationName(
											meeting_list_updateUI);
								} else {
									// 只锟叫伙拷锟斤拷锟斤拷锟斤拷锟剿匡拷锟斤拷锟斤拷锟接憋拷锟剿伙拷删锟斤拷锟斤拷锟斤拷
									TLRPC.TL_MeetingInfo info = meetings
											.get(mid);

									if (info != null) {
										if (act == 5)// 锟斤拷示锟斤拷锟接伙拷锟斤拷锟斤拷员
										{
											info.participants.add(uid);
										} else if (act == 6)// 锟斤拷示删锟斤拷锟斤拷锟斤拷锟斤拷员
										{
											info.participants
											.remove(uid);
											info.resParticipants
											.remove(uid);
										}
										sortMeetings();
										NotificationCenter
										.getInstance()
										.postNotificationName(
												meet_infos_needreload,
												mid);
									}
								}
							}
							int mid = mc.mid;
							TLRPC.TL_MeetingInfo info = meetings
									.get(mid);
							if (info != null)
								MessagesStorage.getInstance()
								.updateMeeting(info);
							NotificationCenter
							.getInstance()
							.postNotificationName(
									MessagesController.unread_message_update);
							// 锟斤拷锟斤拷锟斤拷瞬渭锟皆ぴ硷拷锟斤拷锟�
							MessagesController.getInstance()
							.meetingInvite(mid, mc.users);
						}
					});
				}
			}
		});
	}

	public TLRPC.TL_MeetingInfo getMeetingInfo(int mid) {
		return meetings.get(mid);
	}

	public String getUserNameById(int userid) {
		TLRPC.User user = users.get(userid);
		if (user != null) {
			String nameString = Utilities.formatName(user);
			return nameString;
		}
		return "";
	}

	// jenf
	public String getUserAvatarPathById(int userid) {
		String photoPath = "";
		TLRPC.User user = users.get(userid);
		if (null != user && null != user.photo
				&& null != user.photo.photo_small) {
			photoPath = Utilities.getCacheDir() + "/"
					+ user.photo.photo_small.volume_id + "_"
					+ user.photo.photo_small.local_id + ".jpg";
		}
		return photoPath;
	}

	// jenf
	public int getMessagesUnreadCount() {
		int count = 0;
		for (int i = 0; i < dialogs.size(); i++) {
			TLRPC.TL_dialog dl = dialogs.get(i);
			count += dl.unread_count;
		}
		return count;
	}

	/**
	 * @return
	 * @Discription 锟斤拷锟斤拷未锟斤拷通锟斤拷锟斤拷
	 */
	public int getForumsUnreadCount() {
		int count = 0;
		for (Map.Entry<Integer, List<Integer>> entry : getForumAffiche4CompanyNumMap()
				.entrySet()) {
			count += entry.getValue().size();
		}
		return count;
	}

	/**
	 * @return
	 * @Discription 锟斤拷司锟斤拷 锟斤拷锟斤拷 锟斤拷锟斤拷
	 */
	public Map<Integer, List<Integer>> getForumAffiche4CompanyNumMap() {
		if (afficheNum4CompanyMap == null) {
			afficheNum4CompanyMap = new HashMap<Integer, List<Integer>>();
		}
		return afficheNum4CompanyMap;
	}

	/**
	 * @return
	 * @Discription 锟斤拷司锟斤拷 锟斤拷锟斤拷实锟斤拷
	 */
	public Map<Integer, TLRPC.TL_Affiche> getForumAffiche4OneCompanyMap() {
		if (tlAfficheMap == null) {
			tlAfficheMap = new HashMap<Integer, TLRPC.TL_Affiche>();
		}
		return tlAfficheMap;
	}

	public void addForumAfficheNum4CompanyMap(int companyId, int bbsid) {
		// int companyId = affiche.companyId;
		// int bbsid = affiche.bbsId;
		if (getForumAffiche4CompanyNumMap().containsKey(companyId)) {
			List<Integer> bbsidList = getForumAffiche4CompanyNumMap().get(
					companyId);
			if (!bbsidList.contains(bbsid)) {
				bbsidList.add(bbsid);
			}
			return;
		}
		List<Integer> bbsidList = new ArrayList<Integer>();
		bbsidList.add(bbsid);
		getForumAffiche4CompanyNumMap().put(companyId, bbsidList);
	}

	public void addForumAffiche4CompanyMap(TLRPC.TL_Affiche tlAffiche) {
		int bbsid = tlAffiche.bbsid;
		if (!getForumAffiche4OneCompanyMap().containsKey(bbsid)) {
			getForumAffiche4OneCompanyMap().put(bbsid, tlAffiche);
		}

	}

	public void removeFormAfficheNum(int companyId) {
		if (getForumAffiche4CompanyNumMap().containsKey(companyId)) {
			getForumAffiche4CompanyNumMap().remove(companyId);
		}
	}

	// public void addForumAffiche(TLRPC.TL_Affiche tlAffiche) {
	// int companyid = tlAffiche.companyid;
	// int bbsid = tlAffiche.bbsid;
	// if (getForumAfficheList().containsKey(companyid)) {
	// Map<Integer, TLRPC.TL_Affiche> tMap=
	// getForumAfficheList().get(companyid);
	// if (tMap.containsKey(bbsid)) {
	// return;
	// }else {
	// tMap.put(bbsid, tlAffiche);
	// }
	// }else {
	// Map<Integer, TLRPC.TL_Affiche> tMap = new HashMap<Integer,
	// TLRPC.TL_Affiche>();
	// tMap.put(bbsid, tlAffiche);
	// getForumAfficheList().put(companyid, tMap);
	// }
	// }
	public int getMeetingsUnreadCount() {
		int count = 0;
		for (int i = 0; i < meetingList.size(); i++) {
			boolean bFound = meetingList.get(i).resParticipants
					.contains(UserConfig.clientUserId);
			if (!bFound) {
				count++;
			}
		}
		return count;
	}

	public int getInviteCompanyUnreadCount() {
		int count = 0;
		for (int i = 0; i < invitedCompanys.size(); i++) {
			TLRPC.TL_PendingCompanyInfo info = invitedCompanys.get(i);
			if (info.unread) {
				count++;
			}

		}
		return count;
	}

	public void readInviteCompany() {
		for (int i = 0; i < invitedCompanys.size(); i++) {
			TLRPC.TL_PendingCompanyInfo info = invitedCompanys.get(i);
			info.unread = false;
			MessagesStorage.getInstance().putPendingCompany(info, false, true);
		}
	}

	public void loadData() {
		// 锟斤拷锟斤拷锟斤拷系锟剿ｏ拷锟斤拷锟斤拷锟斤拷息锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷息
		if (dataloaded)
			return;
		dataloaded = true;

		// 锟斤拷要锟斤拷锟斤拷锟铰憋拷锟剿猴拷锟斤拷锟揭憋拷锟侥拷锟斤拷说锟斤拷锟斤拷锟斤拷原锟斤拷锟斤拷loaddialog没执锟叫碉拷锟铰碉拷
		loadDialogs(0, 0, 100, true);
		// 锟斤拷锟叫的硷拷锟截讹拷应锟斤拷锟斤拷每锟斤拷锟斤拷同锟斤拷UI锟叫ｏ拷锟斤拷要锟斤拷一锟斤拷锟截凤拷,锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟叫э拷锟�
		if (UserConfig.isPersonalVersion) {
			// 锟斤拷锟斤拷锟饺★拷锟斤拷锟斤拷只锟斤拷锟较碉拷耍锟斤拷锟斤拷颖锟斤拷锟酵ㄑ堵硷拷锟饺�
			loadingContacts = true;
			MessagesStorage.getInstance().loadUsersAndPhoneBookFromDB();
		} else {
			// 锟斤拷取companys,departments,users锟斤拷息
			MessagesStorage.getInstance().loadCompanyInfoFromDB();
			// 锟斤拷取锟矫伙拷锟斤拷锟节的癸拷司锟斤拷息,一锟斤拷锟矫伙拷锟斤拷锟斤拷锟斤拷锟节讹拷锟斤拷锟剿�,//锟斤拷取锟矫伙拷锟斤拷锟斤拷司锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷息
			MessagesStorage.getInstance().loadCompanyUser();			
			//MessagesStorage.getInstance().getMeetingList2();
		}
		// 锟斤拷取锟斤拷锟截伙拷锟斤拷锟叫憋拷螅锟斤拷锟絞etupdate锟斤拷锟斤拷锟斤拷锟斤拷同锟斤拷锟斤拷息
		MessagesStorage.getInstance().getMeetingList();
		// getchannel锟斤拷去getupdate,锟斤拷锟斤拷锟斤拷锟捷讹拷准锟斤拷锟斤拷锟剿猴拷
		MessagesStorage.getInstance().getChaninfo();
		//锟接憋拷锟截伙拷取直锟斤拷锟叫憋拷  qxm add

		MessagesStorage.getInstance().getDirectImg();
		loadAlertInfo("");
	}

	public void processEnterPriseContacts(
			final ArrayList<TLRPC.TL_contact> contactsArr,
			final ArrayList<TLRPC.User> usersArr,
			final ArrayList<TLRPC.TL_Company> companys,
			final ArrayList<TLRPC.TL_DepartMent> departments, final int from) {
		// 锟斤拷锟矫伙拷锟斤拷转锟斤拷为pinyin,然锟斤拷娲拷锟紻B锟斤拷锟皆猴拷锟斤拷锟斤拷锟斤拷锟斤拷
		// from: 0 - from server, 1 - from db, 2 - from imported contacts
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				for (TLRPC.User user : usersArr) {
					TLRPC.User oldUser = MessagesController.getInstance().users
							.get(user.id);
					if (oldUser != null) {
						// String newName =
						// Utilities.formatName(user.first_name,
						// user.last_name);
						// StringUtil.getCompanyUserRemark(oldUser,newName);
						user.nickname = oldUser.nickname;
					}
					users.put(user.id, user);
					// add by xueqiang
					usersSDK.put(user.identification, user);
					// FileLog.e("emm", user.first_name+user.last_name);
					if (user.id == UserConfig.clientUserId) {
						user.sessionid = UserConfig.currentUser.sessionid;
						UserConfig.currentUser = user;
					}
					if (!MessagesController.getInstance().searchUsers
							.contains(user))
						MessagesController.getInstance().searchUsers.add(user);

				}// for end
				processLoadCompany(companys, departments, usersArr, from);
			}
		});
	}

	// xueqiang add for add company,department,usercompany
	public void processLoadCompany(final ArrayList<TLRPC.TL_Company> companys,
			final ArrayList<TLRPC.TL_DepartMent> departments,
			final ArrayList<TLRPC.User> users, final int from) {
		// from: 0 - from server, 1 - from db, 2 - from imported contacts

		/*
		 * Utilities.stageQueue.postRunnable(new Runnable() {
		 * 
		 * @Override public void run() { if( from == 1)
		 * ConnectionsManager.getInstance().StartNetWorkService(); } });
		 */

		//	if (from == 1)
		//		ConnectionsManager.getInstance().StartNetWorkService();
		// from: 0 - from server, 1 - from db, 2 - from imported contacts

		if (from == 0)// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
			MessagesStorage.getInstance().putUsersAndChats(users, null, true,true);
		// companystate 0,1锟斤拷锟斤拷锟矫ｏ拷<2锟斤拷锟斤拷锟斤拷锟斤拷使锟斤拷,锟角凤拷应锟斤拷锟斤拷锟斤拷司锟斤拷息锟斤拷锟斤拷DB锟斤拷
		ArrayList<TLRPC.TL_Company> tempCompanys = new ArrayList<TLRPC.TL_Company>();
		for (int i = 0; i < companys.size(); i++) {
			final TLRPC.TL_Company company = companys.get(i);

			MessagesController.getInstance().companys.put(company.id, company);

			if (from == 0) {
				// 0 - from server
				if (company.status >= 2)// 锟斤拷示锟斤拷司锟斤拷锟斤拷使锟矫ｏ拷锟矫伙拷锟街伙拷锟斤拷锟斤拷示锟斤拷业通讯锟斤拷锟斤拷
				{
					MessagesStorage.getInstance().deleteCompany(company.id);
					MessagesController.getInstance().companys
					.remove(company.id);
				} else {
					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() {
							NotificationCenter
							.getInstance()
							.postNotificationName(
									MessagesController.company_name_changed,
									company.id);
						}
					});
					tempCompanys.add(company);
				}
			}
		}
		if (from == 0) {
			// 只锟斤拷锟矫伙拷锟缴撅拷锟斤拷墓锟剿�
			MessagesStorage.getInstance().putCompany(tempCompanys, true, true);
		}

		ArrayList<TLRPC.TL_DepartMent> tempDepts = new ArrayList<TLRPC.TL_DepartMent>();
		for (int i = 0; i < departments.size(); i++) {
			TLRPC.TL_DepartMent dept = departments.get(i);
			// status为1锟斤拷示删锟斤拷,为0锟斤拷示锟斤拷锟斤拷
			if (dept.status == 1) {
				// clear db data锟斤拷锟节达拷锟斤拷锟捷结构
				MessagesController.getInstance().departments.remove(dept.id);
				MessagesController.getInstance().departidToUsers
				.remove(dept.id);
				// delete deptment from db
				MessagesStorage.getInstance().deleteDepartment(dept.id);
				continue;
			} else {
				MessagesController.getInstance().departments.put(dept.id, dept);
				tempDepts.add(dept);
			}
		}

		if (from == 0) {
			// 只锟斤拷锟矫伙拷锟缴撅拷锟斤拷墓锟剿�
			MessagesStorage.getInstance().putDepartment(tempDepts, true, true);
		}

		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				if (companys.size() > 0) {
					loadingContacts = false;
					NotificationCenter.getInstance().postNotificationName(
							MessagesController.contactsDidLoaded);
				}
			}
		});
		FileLog.d("emm", "processLoadCompany");
	}

	public void processLoadedUserCompany(
			final ArrayList<TLRPC.TL_UserCompany> userCompany, final int from) {
		// Utilities.stageQueue.postRunnable(new Runnable() {
		// @Override
		// public void run()
		// {

		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				if (userCompany != null) {
					for (int i = 0; i < userCompany.size(); i++) {
						TLRPC.TL_UserCompany info = userCompany.get(i);
						processUserCompanyInfo(info, from);
					}
				}
				if (from == 0) {
					loadingContacts = false;
					loadingDialogs = false;
					NotificationCenter.getInstance().postNotificationName(
							MessagesController.dialogsNeedReload);
					if (userCompany != null && userCompany.size() != 0)
						MessagesStorage.getInstance().putUserCompany(
								userCompany, true, true);
					NotificationCenter.getInstance().postNotificationName(
							MessagesController.contactsDidLoaded);
					ConnectionsManager.getInstance().getMeetingStatus("0",0);
				}
			}
		});
		// }
		// });
	}

	/**
	 * @param userid
	 * @return
	 * @Discription 锟斤拷取锟矫伙拷锟斤拷司锟斤拷锟斤拷
	 */
	public List<String> getCompanyEmails4User(int userid) {
		List<String> emailList = new ArrayList<String>();
		for (HashMap.Entry<Integer, TLRPC.TL_Company> tl_company : companys
				.entrySet()) {
			String key = getKey(userid, tl_company.getValue().id);
			if (StringUtil.isEmpty(key) || userCompanysMap == null) {
				continue;
			}
			TLRPC.TL_UserCompany userCompany = userCompanysMap.get(key);
			if (userCompany == null || StringUtil.isEmpty(userCompany.email)) {
				continue;
			}
			if (!emailList.contains(userCompany.email)) {
				emailList.add(userCompany.email);
			}
		}
		return emailList;
	}

	public String getCompanyEmail4User(int userid, int companyid) {
		String key = getKey(userid, companyid);
		if (StringUtil.isEmpty(key) || userCompanysMap == null) {
			return null;
		}
		TLRPC.TL_UserCompany userCompany = userCompanysMap.get(key);
		String email = null;
		if (userCompany != null) {
			email = userCompany.email;
		}
		return email;
	}

	public void processUserCompanyInfo(TLRPC.TL_UserCompany info, int from) {
		// 锟斤拷锟斤拷UserCompany锟斤拷息,锟饺查看锟角凤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
		// 0:锟斤拷锟斤拷,1:锟斤拷删锟斤拷锟斤拷锟斤拷 2锟窖帮拷装未同锟斤拷 3 未锟斤拷装,锟斤拷锟斤拷侄锟斤拷没锟斤拷锟斤拷锟斤拷锟斤拷只锟斤拷没锟斤拷锟阶刺�

		int user_oldDepartid = 0;
		if (info == null)
			return;
		if (info.ucstate == 1) {

			// 锟斤拷示锟接癸拷司删锟斤拷锟斤拷一锟斤拷锟矫伙拷
			HashSet<TLRPC.TL_UserCompany> userCompany = departidToUsers
					.get(info.deptID);
			if (userCompany != null) {
				userCompany.remove(info);
				if (userCompany.isEmpty())
					departidToUsers.remove(info.deptID);
				// 锟斤拷示某锟斤拷锟剿筹拷锟斤拷锟斤拷锟斤拷锟街�
				String key = getKey(info.userID, info.companyID);
				userCompanysMap.remove(key);
				MessagesStorage.getInstance().deleteUserCompany(info.userID,
						info.companyID);
			}
		} else {
			String key = getKey(info.userID, info.companyID);
			TLRPC.TL_UserCompany company = userCompanysMap.get(key);
			if (company != null) {
				company.ucstate = info.ucstate;
				company.userRoleID = info.userRoleID;
				// 锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
				if (from == 0) {

					// 锟斤拷示user锟斤拷锟斤拷锟斤拷司锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷,锟斤拷要删锟斤拷原锟斤拷锟侥诧拷锟脚碉拷锟矫伙拷锟侥癸拷系锟斤拷锟斤拷锟铰的癸拷系
					boolean bChanged = false;
					if (company.deptID != info.deptID) {
						HashSet<TLRPC.TL_UserCompany> userCompanys = departidToUsers
								.get(company.deptID);
						if (userCompanys != null) {
							Iterator<TLRPC.TL_UserCompany> it = userCompanys
									.iterator();
							while (it.hasNext()) {
								TLRPC.TL_UserCompany userCompany = it.next();
								if (userCompany != null
										&& userCompany.companyID == info.companyID
										&& userCompany.userID == info.userID) {
									user_oldDepartid = company.deptID;
									userCompanys.remove(userCompany);
									break;
								}
							}
						}
					}
				}
			}
			userCompanysMap.put(key, info);
			// 锟斤拷锟缴癸拷系userid锟酵诧拷锟脚硷拷锟斤拷司之锟斤拷墓锟较�
			createDeptToUserMap(info);
		}

	}

	public void createDeptToUserMap(TLRPC.TL_UserCompany info) {
		// 锟矫伙拷锟斤拷息锟角凤拷锟斤拷锟斤拷约锟� 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷约锟揭惨拷诺锟斤拷锟斤拷锟斤拷锟捷结构锟斤拷
		// xueqiang todo..
		HashSet<TLRPC.TL_UserCompany> userCompany = departidToUsers
				.get(info.deptID);
		if (userCompany == null) {
			userCompany = new HashSet<TLRPC.TL_UserCompany>();
			departidToUsers.put(info.deptID, userCompany);
		}

		if (!userCompany.add(info)) {
			userCompany.remove(info);
			userCompany.add(info);
		}

	}

	public String getKey(int userid, int companyid) {
		return userid + "-" + companyid;
	}

	public void showMeetInAppView(TLRPC.TL_MeetingInfo updates) {
		if (updates != null) {
			TLRPC.TL_message message = new TLRPC.TL_message();
			message.from_id = updates.createid;// 锟斤拷锟斤拷锟斤拷ID
			message.id = updates.mid;// 锟斤拷锟斤拷ID
			message.to_id = new TLRPC.TL_peerUser();//
			message.to_id.user_id = UserConfig.clientUserId;
			String messages = LocaleController.getString("InvokeJoinMeet",
					R.string.InvokeJoinMeet);
			message.message = messages;
			message.date = updates.startTime;
			message.media = new TLRPC.TL_messageMediaEmpty();
			message.is_from_other = true;
			final MessageObject obj = new MessageObject(message, null);

			Utilities.RunOnUIThread(new Runnable() {
				@Override
				public void run() {
					showInAppNotification(obj);
				}
			});
		}
	}

	/**
	 * 锟斤拷锟斤拷锟斤拷锟斤拷
	 * @param mid
	 * @param isRegister
	 */
	public void scheduleNextAlert(int mid, boolean isRegister) {
		if (!Utilities.isSupportOS()) {
			return;
		}
		TLRPC.TL_MeetingInfo info = MessagesController.getInstance().meetings.get(mid);
		if(info==null)
			return;
		Intent it = new Intent(ApplicationLoader.applicationContext,
				AlertActivity.class);
		// it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		AlarmManager aMgr = (AlarmManager) ApplicationLoader.applicationContext
				.getSystemService(android.content.Context.ALARM_SERVICE);
		it.putExtra("meetingId", String.valueOf(mid));		
		it.putExtra("pwd", info.confuserpwd);
		it.putExtra("type", 0);
		it.putExtra("userId", info.createid);
		PendingIntent pendingIntent = PendingIntent.getActivity(
				ApplicationLoader.applicationContext, mid, it,
				PendingIntent.FLAG_NO_CREATE);
		if (pendingIntent != null) {
			aMgr.cancel(pendingIntent);
		}

		if (isRegister) {
			if (info != null && info.startTime > ConnectionsManager.getInstance().getCurrentTime()) {
				PendingIntent pendingIntentNew = PendingIntent.getActivity(
						ApplicationLoader.applicationContext, mid, it,
						PendingIntent.FLAG_UPDATE_CURRENT);
				Calendar wakeUpTime = Calendar.getInstance();
				long triggerTime = ((long) (info.startTime)) * 1000;
				wakeUpTime.setTimeInMillis(triggerTime);
				aMgr.set(AlarmManager.RTC_WAKEUP, wakeUpTime.getTimeInMillis(),
						pendingIntentNew);
			}
		}
	}

	public void scheduleAlert(final TLRPC.TL_alertMedia alert,
			boolean isRegister) {
		if (!Utilities.isSupportOS()) {
			return;
		}
		Intent it = new Intent(ApplicationLoader.applicationContext,
				AlertActivity.class);
		AlarmManager aMgr = (AlarmManager) ApplicationLoader.applicationContext
				.getSystemService(android.content.Context.ALARM_SERVICE);
		it.putExtra("type", 2);
		it.putExtra("message", alert.msg);
		PendingIntent pendingIntent = PendingIntent.getActivity(
				ApplicationLoader.applicationContext, alert.id, it,
				PendingIntent.FLAG_NO_CREATE);
		if (pendingIntent != null) {
			aMgr.cancel(pendingIntent);
		}

		if (isRegister) {
			if (alert.date > ConnectionsManager.getInstance().getCurrentTime()) {
				PendingIntent pendingIntentNew = PendingIntent.getActivity(
						ApplicationLoader.applicationContext, alert.id, it,
						PendingIntent.FLAG_UPDATE_CURRENT);
				Calendar wakeUpTime = Calendar.getInstance();
				long triggerTime = ((long) (alert.date)) * 1000;
				wakeUpTime.setTimeInMillis(triggerTime);
				aMgr.set(AlarmManager.RTC_WAKEUP, wakeUpTime.getTimeInMillis(),
						pendingIntentNew);
			}
		}
	}

	/**
	 * @Title: processMeetingInvite
	 * 
	 * @Description: 锟斤拷锟杰伙拷锟斤拷锟斤拷锟斤拷
	 * 
	 * @param mid
	 *            锟斤拷锟斤拷锟斤拷ID
	 * @param from_id
	 *            锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷ID
	 */
	public void processMeetingInvite(final int mid, final int from_id) {
		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				// 锟斤拷示锟叫伙拷锟斤拷锟斤拷锟诫到锟斤拷
				if (!Utilities.isSupportOS()) {
					// 锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷渭踊锟斤拷椋拷锟斤拷锟侥壳帮拷锟较低筹拷锟街э拷只锟斤拷锟�
					return;
				}
				// 锟斤拷锟斤拷锟斤拷锟斤拷锟揭的伙拷锟介，锟斤拷锟斤拷锟揭憋拷锟斤拷没锟斤拷锟斤拷锟斤拷息呀锟斤拷锟斤拷未锟斤拷锟斤拷锟绞碉拷锟斤拷锟斤拷锟斤拷只锟斤拷要知锟斤拷锟斤拷锟斤拷锟街�
				// TLRPC.TL_MeetingInfo info =
				// MessagesController.getInstance().meetings.get(mid);
				Intent it = new Intent(ApplicationLoader.applicationContext,
						AlertActivity.class);
				// it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				AlarmManager aMgr = (AlarmManager) ApplicationLoader.applicationContext
						.getSystemService(android.content.Context.ALARM_SERVICE);
				it.putExtra("meetingId", mid + "");
				it.putExtra("fromid", from_id);
				it.putExtra("type", 0);// 0锟斤拷示预约锟斤拷1锟斤拷示锟斤拷时锟斤拷锟斤拷
				PendingIntent pendingIntent = PendingIntent.getActivity(
						ApplicationLoader.applicationContext, mid, it,
						PendingIntent.FLAG_NO_CREATE);
				if (pendingIntent != null) {
					aMgr.cancel(pendingIntent);
				}

				// if (info!=null)
				// {
				// 锟斤拷锟斤拷alertActivity锟斤拷锟斤拷
				PendingIntent pendingIntentNew = PendingIntent.getActivity(
						ApplicationLoader.applicationContext, mid, it,
						PendingIntent.FLAG_UPDATE_CURRENT);
				aMgr.set(AlarmManager.RTC_WAKEUP, 200, pendingIntentNew);
				// }
			}
		});
	}

	/**
	 * @Title: processMeetingInvite
	 * 
	 * @Description: 锟斤拷锟杰伙拷锟斤拷锟斤拷锟斤拷
	 * 
	 * @param mid
	 *            锟斤拷锟斤拷锟斤拷ID
	 * @param from_id
	 *            锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷ID
	 */
	public void processMeetingCall(final String mid, final int from_id,
			final int gid) {
		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				// 锟斤拷示锟叫伙拷锟斤拷锟斤拷锟诫到锟斤拷
				if (!Utilities.isSupportOS()) {
					// 锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷渭踊锟斤拷椋拷锟斤拷锟侥壳帮拷锟较低筹拷锟街э拷只锟斤拷锟�
					return;
				}
				// 锟斤拷锟斤拷锟斤拷锟斤拷锟揭的伙拷锟介，锟斤拷锟斤拷锟揭憋拷锟斤拷没锟斤拷锟斤拷锟斤拷息呀锟斤拷锟斤拷未锟斤拷锟斤拷锟绞碉拷锟斤拷锟斤拷锟斤拷只锟斤拷要知锟斤拷锟斤拷锟斤拷锟街�
				if(users.get(from_id)!=null)
				{
					ArrayList<User> usersarray = new ArrayList<User>();			 
					usersarray.add(users.get(from_id));
					MessagesStorage.getInstance().putUsersAndChats(usersarray,null, true, true);
				}
				//hasCall锟斤拷示锟斤拷锟斤拷锟斤拷锟节猴拷锟斤拷锟揭ｏ拷锟揭伙拷锟斤拷锟斤拷未锟斤拷锟斤拷状态锟斤拷锟斤拷锟绞憋拷锟斤拷斜锟斤拷撕锟斤拷锟斤拷一卮锟矫β�
				//锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷诨锟斤拷锟斤拷谢锟� 未锟斤拷锟斤拷锟斤拷状态锟斤拷只锟斤拷 1锟斤拷1锟斤拷时锟斤拷锟斤拷锟斤拷锟斤拷趾锟斤拷锟斤拷遥锟斤拷遣锟接︼拷没锟矫β碉拷锟�
				//锟斤拷锟斤拷应锟矫帮拷取锟斤拷锟脚斤拷去锟斤拷锟斤拷锟斤拷锟叫讹拷锟剿筹拷锟斤拷锟斤拷锟绞憋拷颍锟斤拷锟斤拷蟹锟斤拷锟矫伙拷卮锟接︼拷梅锟斤拷锟饺★拷锟斤拷锟斤拷锟�
				if (WeiyiMeeting.isInMeeting() || IMRtmpClientMgr.getInstance().hasCall() ) 
				{
					//锟斤拷示1锟斤拷1锟斤拷锟角革拷锟斤拷锟街猴拷锟斤拷锟揭碉拷时锟斤拷直锟接凤拷锟截ｏ拷锟斤拷为锟揭伙拷锟节伙拷锟斤拷锟轿达拷锟阶刺拷锟�
					if(gid==0 && IMRtmpClientMgr.getInstance().getPeerID()==from_id)
						return;
					//锟斤拷锟剿猴拷锟斤拷锟揭ｏ拷锟斤拷锟斤拷锟节伙拷锟斤拷锟叫ｏ拷锟斤拷要锟斤拷示锟斤拷息
					// 锟杰撅拷锟斤拷锟斤拷锟斤拷锟�,//0=锟斤拷锟叫ｏ拷1=取锟斤拷锟斤拷锟叫ｏ拷2=锟杰撅拷锟斤拷3=忙锟斤拷4=锟剿筹拷
					ArrayList<Integer> users = new ArrayList<Integer>();
					users.add(from_id);
					MessagesController.getInstance().meetingCall(mid, gid,
							users, 3);
					String temp = LocaleController.getString("youhavecall",
							R.string.youhavecall);
					if (gid != 0)
						MessagesController.getInstance().sendSystemMsg(0, gid,
								temp, true);
					else
						MessagesController.getInstance().sendSystemMsg(from_id,
								0, temp, true);
					return;
				}


				IMRtmpClientMgr.getInstance().setReceiveCall(true);
				Intent it = new Intent(ApplicationLoader.applicationContext,AlertActivity.class);
				// it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				AlarmManager aMgr = (AlarmManager) ApplicationLoader.applicationContext.getSystemService(android.content.Context.ALARM_SERVICE);
				it.putExtra("meetingId", mid);
				it.putExtra("userId", from_id);
				it.putExtra("chatId", gid);//锟斤拷锟絞id为0锟斤拷锟斤拷示为1锟斤拷1锟斤拷锟斤拷
				it.putExtra("type", 1);// 0锟斤拷示预约锟斤拷1锟斤拷示锟斤拷时锟斤拷锟斤拷
				//锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷时
				if(gid==0)
				{
					IMRtmpClientMgr.getInstance().setPeerID(from_id);					
				}
				else
				{
					IMRtmpClientMgr.getInstance().setChatId(gid);

				}
				IMRtmpClientMgr.getInstance().setMeetingID(mid);

				if (gid != 0) {
					PendingIntent pendingIntent = PendingIntent.getActivity(
							ApplicationLoader.applicationContext, gid, it,
							PendingIntent.FLAG_NO_CREATE);
					if (pendingIntent != null) {
						aMgr.cancel(pendingIntent);
					}
				} else {
					PendingIntent pendingIntent = PendingIntent.getActivity(
							ApplicationLoader.applicationContext, from_id, it,
							PendingIntent.FLAG_NO_CREATE);
					if (pendingIntent != null) {
						aMgr.cancel(pendingIntent);
					}
				}

				//实锟绞碉拷锟斤拷锟斤拷phoneAcitivy锟斤拷锟斤拷
				if (gid != 0) {
					// 锟斤拷锟斤拷alertActivity锟斤拷锟斤拷
					PendingIntent pendingIntentNew = PendingIntent.getActivity(
							ApplicationLoader.applicationContext, gid, it,
							PendingIntent.FLAG_UPDATE_CURRENT);
					aMgr.set(AlarmManager.RTC_WAKEUP, 200, pendingIntentNew);
				} else {
					// 锟斤拷锟斤拷alertActivity锟斤拷锟斤拷
					PendingIntent pendingIntentNew = PendingIntent.getActivity(
							ApplicationLoader.applicationContext, from_id, it,
							PendingIntent.FLAG_UPDATE_CURRENT);
					aMgr.set(AlarmManager.RTC_WAKEUP, 200, pendingIntentNew);
				}

				String temp = LocaleController.getString("callingpeer",R.string.callingpeer);
				TLRPC.User user = users.get(from_id);
				String invitename= Utilities.formatName(user.first_name,user.last_name);
				temp = invitename+temp;

				if (gid != 0)
					MessagesController.getInstance().sendSystemMsg(0, gid,
							temp, true);
				else
					MessagesController.getInstance().sendSystemMsg(from_id,
							0, temp, true);

				//NotificationCenter.getInstance().postNotificationName(MessagesController.meeting_notice_bar);
			}
		});
	}

	public void processMeetingStatus(final int gid,
			final ArrayList<Integer> userList,final String mid) {
		// 锟斤拷示某锟斤拷锟斤拷幕锟斤拷锟阶刺�
		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				IMRtmpClientMgr.getInstance().setStatus(gid, userList,mid);
			}
		});
	}

	public void processMeetingCallResponse(final String mid, final int from_id,
			final int gid, final int signal) {

		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				// signal 0=锟斤拷锟叫ｏ拷1=取锟斤拷锟斤拷锟叫ｏ拷2=锟杰撅拷锟斤拷3=忙锟斤拷4=锟剿筹拷
				Intent it = new Intent(ApplicationLoader.applicationContext,
						AlertActivity.class);
				AlarmManager aMgr = (AlarmManager) ApplicationLoader.applicationContext
						.getSystemService(android.content.Context.ALARM_SERVICE);
				it.putExtra("meetingId", mid);
				it.putExtra("userId", from_id);
				it.putExtra("chatId", gid);
				if (signal == 1) {
					if (gid != 0) {
						PendingIntent pendingIntent = PendingIntent
								.getActivity(
										ApplicationLoader.applicationContext,
										gid, it, PendingIntent.FLAG_NO_CREATE);
						if (pendingIntent != null) {
							aMgr.cancel(pendingIntent);
						}
					} else {
						PendingIntent pendingIntent = PendingIntent
								.getActivity(
										ApplicationLoader.applicationContext,
										from_id, it,
										PendingIntent.FLAG_NO_CREATE);
						if (pendingIntent != null) {
							aMgr.cancel(pendingIntent);
						}
					}
					NotificationCenter.getInstance().postNotificationName(MessagesController.meeting_call_response,signal);

				} else if (signal == 2 || signal == 3 || signal == 4) {
					// signal 0=锟斤拷锟叫ｏ拷1=取锟斤拷锟斤拷锟叫ｏ拷2=锟杰撅拷锟斤拷3=忙锟斤拷4=锟剿筹拷
					// 通知锟斤拷锟斤拷锟斤拷示锟斤拷息,锟皆凤拷锟杰撅拷锟斤拷忙碌锟斤拷锟斤拷息

					if(gid==0)
					{		
						WeiyiMeeting.getInstance().exitMeeting();
						String temp="";
						if(signal == 3)
						{
							temp = LocaleController.getString("peerbusy",R.string.peerbusy);
							//IMRtmpClientMgr.getInstance().dial(ToneGenerator.TONE_SUP_BUSY);
						}
						else if(signal==2)
						{
							temp = LocaleController.getString("peerrefusecall",R.string.peerrefusecall);
							//IMRtmpClientMgr.getInstance().dial(ToneGenerator.TONE_SUP_CONGESTION);
						}
						IMRtmpClientMgr.getInstance().stopdial();
						IMRtmpClientMgr.getInstance().stopTimer();
						MessagesController.getInstance().sendSystemMsg(from_id,gid, temp, true);
						NotificationCenter.getInstance().postNotificationName(MessagesController.meeting_call_response,signal);
					}

				}
			}
		});
	}

	public void meetingInvite(int mid, ArrayList<Integer> users) {
		ConnectionsManager.getInstance().meetingInvite(mid, users);
	}





	public boolean findIgnoreUser(String phone) {
		if (ignoreUsers != null && !ignoreUsers.isEmpty()) {
			for (HashMap.Entry<Integer, TLRPC.User> entryUsers : ignoreUsers
					.entrySet()) {
				TLRPC.User user = entryUsers.getValue();
				if (user.phone.equals(phone))
					return true;
			}
		}
		return false;
	}

	public void getContact(ArrayList<DataAdapter> arrData) {
		synchronized (sync) {
			if (contactsArray.size() > 0) {
				int count = contactsArray.size();
				for (int i = 0; i < count; i++)
					arrData.add(contactsArray.get(i));
				return;
			}
		}
	}

	// 锟斤拷锟截憋拷锟斤拷锟街伙拷锟斤拷锟斤拷系锟斤拷
	public void loadContact() {
		synchronized (sync) {
			// 锟斤拷锟斤拷锟斤拷只锟斤拷屎锟斤拷锟揭拷锟斤拷锟斤拷锟酵ㄑ堵硷拷谐锟斤拷锟�
			// if( !EmmUtil.isPhone(UserConfig.account) )
			// return;
			if (contactsMap == null)
				return;
			for (HashMap.Entry<String, ContactsController.Contact> entry : contactsMD5Map
					.entrySet()) {
				Contact ct = entry.getValue();
				if (ct != null) {
					DataAdapter da = new DataAdapter();
					if (ignoreUsers != null) {
						String usePhone = ct.phone;
						usePhone = usePhone;
						if (findIgnoreUser(usePhone))
							continue;
					}
					da.dataID = ct.id;// 锟斤拷锟角憋拷锟截电话锟斤拷锟斤拷锟街�
					da.parentDeptID = 0;
					da.companyID = 0;
					da.dataName = Utilities.formatName(ct.first_name,
							ct.last_name);
					da.dataName = da.dataName;
					da.dataInfo = ct.phone;
					// da.dataICO = user.userico;
					da.isCompany = false;
					da.haveChild = false;
					da.isUser = true;
					if (!da.dataName.equals("")) {
						String pinyin = CharacterParser.getInstance()
								.getSelling(da.dataName);
						String sortString = pinyin.substring(0, 1)
								.toUpperCase();
						if (sortString.matches("[A-Z]")) {
							da.sortLetters = sortString;
						} else {
							da.sortLetters = "#";
						}
						contactsArray.add(da);
					}
				}
			}
			// wangxm add 锟斤拷锟斤拷
			Collections.sort(contactsArray, compare);
			Utilities.RunOnUIThread(new Runnable() {
				@Override
				public void run() {
					loadingContacts = false;
					NotificationCenter.getInstance().postNotificationName(
							MessagesController.contactsDidLoaded);
				}
			});
		}
	}

	public void clearPersonalContacts() {
		synchronized (sync) {
			//contactsMD5Map.clear();
			//contactsMapNew.clear();
			//contactsArray.clear();
		}
	}

	private ArrayList<String> getNewContacts(ConcurrentHashMap<Integer, Contact> myContacts)
	{
		final ArrayList<String> contacts = new ArrayList<String>();
		if (!myContacts.isEmpty()) 
		{		
			for (HashMap.Entry<Integer, ContactsController.Contact> entry : myContacts.entrySet()) 
			{
				Contact ct = entry.getValue();
				for (int k = 0; k < ct.shortPhones.size(); k++) {
					// phones锟叫帮拷锟斤拷锟界话锟侥癸拷锟揭猴拷锟斤拷锟�,锟斤拷锟斤拷+锟斤拷00,shortPhones锟叫帮拷锟斤拷锟斤拷锟角诧拷锟斤拷+锟脚的ｏ拷锟斤拷锟斤拷示锟斤拷时锟斤拷锟斤拷锟斤拷锟斤拷穑坎锟斤拷锟斤拷锟斤拷锟�
					String usePhone = ct.shortPhones.get(k);
					// 锟斤拷锟斤拷锟矫伙拷锟界话锟斤拷锟斤拷锟斤拷锟斤拷示锟矫ｏ拷锟斤拷锟斤拷锟绞撅拷兀锟斤拷锟绞撅拷没锟斤拷锟�+锟界话锟斤拷锟斤拷
					Contact newContact = createNewContact(ct,usePhone);
					//						FileLog.d("emm", "get contact phone:"+usePhone);
					// 锟斤拷锟斤拷缁帮拷锟斤拷氩伙拷锟�+锟斤拷00锟斤拷锟揭达拷锟诫，锟斤拷默锟斤拷为注锟斤拷锟矫伙拷锟侥癸拷锟揭达拷锟斤拷								
					String phoneMD5 = Utilities.MD5(usePhone);
					contactsMD5Map.put(phoneMD5, newContact);
					contactsMapNew.put(newContact.id, newContact);										
					contacts.add(phoneMD5);
				}
			}
		}
		return contacts;
	}
	// 锟斤拷DB锟叫硷拷锟截憋拷锟斤拷锟街伙拷通讯录,锟斤拷锟矫伙拷锟斤拷锟斤拷锟街伙拷SIM锟斤拷锟斤拷锟斤拷
	public void loadPhoneBookFromDBCompleted(
			final ConcurrentHashMap<Integer, ContactsController.Contact> contactHashMap,
			final ArrayList<TLRPC.User> dbUsers) {
		Utilities.globalQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < dbUsers.size(); i++) 
				{
					TLRPC.User user = dbUsers.get(i);

					users.put(user.id, user);
					usersSDK.put(user.identification, user);					
					if (!MessagesController.getInstance().searchUsers.contains(user))
						MessagesController.getInstance().searchUsers.add(user);
				}
				ConnectionsManager.getInstance().StartNetWorkService();	

				ConcurrentHashMap<Integer, Contact> localContactsMap = ContactsController.getInstance().readContactsFromPhoneBook();
				if (contactHashMap.isEmpty()) {
					// 锟斤拷锟斤拷DB没锟斤拷锟斤拷锟斤拷只锟絊IM锟斤拷锟斤拷取锟街伙拷锟斤拷系锟斤拷
					contactsMap = localContactsMap;
					// save db and uploadcontacts to phpserver
					if(!contactsMap.isEmpty())
					{
						ArrayList<String> contacts = getNewContacts(contactsMap);
						MessagesStorage.getInstance().updatePhoneBook(contactsMap);
						uploadContacts(contacts);
						Log.e("tag","contacts======" + contacts);
						return;
					}

				}
				else
				{	
					ConcurrentHashMap<Integer, Contact> newContacts = new ConcurrentHashMap<Integer, Contact>();
					contactsMap = contactHashMap;
					//db锟斤拷通讯录锟斤拷要锟饺斤拷一锟铰ｏ拷锟角凤拷锟斤拷锟铰碉拷锟斤拷系锟斤拷,锟斤拷锟铰碉拷锟斤拷系锟斤拷锟斤拷锟较达拷锟斤拷锟斤拷锟斤拷锟斤拷
					for (HashMap.Entry<Integer, ContactsController.Contact> entry : localContactsMap.entrySet()) 
					{
						Integer id = entry.getKey();
						if(contactHashMap.get(id)==null)
						{
							ContactsController.Contact con = entry.getValue();								
							contactsMap.put(id, con);
							newContacts.put(id, con);								
						}							
					}
					if(!newContacts.isEmpty())
					{
						ArrayList<String> contacts = getNewContacts(newContacts);
						//save db
						MessagesStorage.getInstance().updatePhoneBook(contactsMap);
						//锟斤拷锟斤拷锟斤拷系锟斤拷锟斤拷息锟斤拷锟斤拷锟斤拷锟斤拷
						uploadContacts(contacts);
						return;
					}
				}


				Utilities.RunOnUIThread(new Runnable() 
				{
					@Override
					public void run() 
					{
						FileLog.e("emm", "you needn't uploadContacts,direct connnect im server");
						loadingContacts = false;
						NotificationCenter
						.getInstance()
						.postNotificationName(
								MessagesController.contactsDidLoaded);
						startPersonalContactService();
					}
				});

			}
		});
	}

	private void startPersonalContactService() {
		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				if (!bSartPersonalContactService) {
					// 锟斤拷锟斤拷要锟较达拷锟斤拷直锟斤拷锟斤拷锟斤拷锟斤拷锟街凤拷锟斤拷
					bSartPersonalContactService = true;
					//ConnectionsManager.getInstance().getUpdate();
					//ConnectionsManager.getInstance().gettime();
					// 锟斤拷锟斤拷timer锟斤拷锟接憋拷锟斤拷锟斤拷系锟剿变化锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷uploadContacts锟斤拷锟竭凤拷锟斤拷锟斤拷锟揭的猴拷锟窖凤拷锟斤拷锟戒化锟斤拷
					startTimerForMonitorPhoneBook();
					FileLog.e("emm", "start service and connect server");
					//ConnectionsManager.getInstance().StartNetWorkService();
					ConnectionsManager.getInstance().getMeetingStatus("0",0);
				}
			}
		});
	}

	public void uploadContacts(ArrayList<String> contacts) {
		Log.e("tag","contacts======" + contacts);
		ConnectionsManager.getInstance().uploadContacts(contacts,
				new RPCRequest.RPCRequestDelegate() {
			@Override
			public void run(TLObject response, TLRPC.TL_error error) {
				if (error != null) {
					if (error.code == 1 || error.code == -1) {
						// 锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟节诧拷锟斤拷锟矫ｏ拷锟斤拷锟皆猴拷锟斤拷锟斤拷
						Utilities.RunOnUIThread(new Runnable() {
							@Override
							public void run() {
								// notity ui,锟窖撅拷锟斤拷装微锟斤拷锟斤拷没锟阶硷拷锟斤拷锟斤拷耍锟斤拷锟斤拷锟絬sers锟叫ｏ拷通知UI锟斤拷示锟斤拷锟斤拷
								loadingContacts = false;
								NotificationCenter.getInstance().postNotificationName(
										MessagesController.contactsDidLoaded);	
							}
						});
					}
				} else {
					final TLObject resResult = response;
					processPersonalContacts((TLRPC.TL_InstallSoftwareContacts) resResult);
					FileLog.e("emm", "uploadContacts startPersonalContactService");
					startPersonalContactService();
				}
			}
		});
	}

	public void processPersonalContact(HashMap<String, Integer> md5PhoneMap) {
		for (HashMap.Entry<String, Integer> entry : md5PhoneMap.entrySet()) {
			String md5Phone = entry.getKey();
			int userid = entry.getValue();
			// 通锟斤拷PHONE锟斤拷MD5值锟斤拷取锟斤拷锟斤拷应锟斤拷锟斤拷系锟斤拷
			Contact ct = contactsMD5Map.get(md5Phone);
			if (ct != null) {
				TLRPC.User user = users.get(userid);
				// user锟斤拷锟街伙拷锟角斤拷锟揭憋拷锟斤拷锟斤拷系锟剿碉拷锟斤拷锟斤拷
				//user.first_name = ct.first_name;
				//user.last_name = ct.last_name;
				user.phone = ct.phone;
				// 锟斤拷锟斤拷锟斤拷系锟剿猴拷user
				ct.users.add(user);
			}
		}
	}

	public void processPersonalContacts(
			final TLRPC.TL_InstallSoftwareContacts resResult) {
		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				if (resResult != null) {
					TLRPC.TL_InstallSoftwareContacts res = (TLRPC.TL_InstallSoftwareContacts) resResult;
					for (int i = 0; i < res.users.size(); i++) 
					{
						//锟斤拷锟斤拷锟絬ser锟斤拷锟角憋拷锟斤拷通讯录锟斤拷USER锟斤拷锟斤拷锟斤拷一锟斤拷USER锟斤拷锟角碉拷锟斤拷锟剿达拷锟斤拷锟斤拷锟绞憋拷蚍祷氐锟侥帮拷锟経SER锟斤拷锟角诧拷锟斤拷锟斤拷示锟斤拷通讯锟斤拷锟叫碉拷
						TLRPC.User user = res.users.get(i);

						users.put(user.id, user);
						// add by xueqiang
						usersSDK.put(user.identification, user);

						if (!MessagesController.getInstance().searchUsers.contains(user))
							MessagesController.getInstance().searchUsers.add(user);
					}
					// change name为锟揭憋拷锟斤拷锟斤拷锟斤拷系锟剿碉拷锟斤拷锟斤拷
					processPersonalContact(res.md5PhoneMap);
					MessagesStorage.getInstance().putUsersAndChats(res.users,
							null, false, true);
				}
				// notity ui,锟窖撅拷锟斤拷装微锟斤拷锟斤拷没锟阶硷拷锟斤拷锟斤拷耍锟斤拷锟斤拷锟絬sers锟叫ｏ拷通知UI锟斤拷示锟斤拷锟斤拷
				loadingContacts = false;
				NotificationCenter.getInstance().postNotificationName(
						MessagesController.contactsDidLoaded);


			}
		});
	}

	public void ControlCompany(final TLRPC.TL_CompanyInfo req) {
		ConnectionsManager.getInstance().ControlCompany((TLObject) req,
				new RPCRequest.RPCRequestDelegate() {
			@Override
			public void run(TLObject response, TLRPC.TL_error error) {
				if (error != null) {
					if (error.code == 1 || error.code == -1
							|| error.code == 2) {
						// 锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟节诧拷锟斤拷锟矫ｏ拷锟斤拷锟皆猴拷锟斤拷锟斤拷
						final int errorCode = error.code;
						Utilities.RunOnUIThread(new Runnable() {
							@Override
							public void run() {
								NotificationCenter.getInstance()
								.postNotificationName(
										company_create_failed,
										errorCode);
							}
						});

					}
				} else {
					final TLObject res = response;
					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() {
							// 锟斤拷示锟斤拷锟斤拷锟斤拷司锟缴癸拷,通锟斤拷getupdate锟斤拷锟截癸拷司锟斤拷息锟斤拷锟斤拷锟斤拷DB锟斤拷锟斤拷锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷
							// act:1锟斤拷示锟斤拷锟斤拷锟斤拷司 2:锟睫改癸拷司 3:删锟斤拷锟斤拷司 4:锟斤拷锟接癸拷司锟斤拷员
							// 5:删锟斤拷锟斤拷司锟斤拷员
							if (req.act == 1) {
								TLRPC.TL_CompanyShortInfo info = (TLRPC.TL_CompanyShortInfo) res;
								if (info != null)
									NotificationCenter
									.getInstance()
									.postNotificationName(
											company_create_success,
											info.companyid);
								else
									NotificationCenter
									.getInstance()
									.postNotificationName(
											company_create_success,
											0);
								NotificationCenter
								.getInstance()
								.postNotificationName(
										MessagesController.contactsDidLoaded);
							} else if (req.act == 2) {
								TLRPC.TL_Company company = companys
										.get(req.companyid);
								company.name = req.name;
								if (req.clearico) {
									company.photo = new TLRPC.TL_chatPhotoEmpty();
								}
								MessagesController.getInstance().companys
								.put(company.id, company);
								ArrayList<TLRPC.TL_Company> companys = new ArrayList<TLRPC.TL_Company>();
								companys.add(company);
								MessagesStorage.getInstance()
								.putCompany(companys, false,
										true);
								NotificationCenter
								.getInstance()
								.postNotificationName(
										MessagesController.company_name_changed,
										req.companyid);
								NotificationCenter.getInstance()
								.postNotificationName(
										contactsDidLoaded); // hz
							} else if (req.act == 3) {
								// 锟斤拷散锟斤拷司锟斤拷说锟斤拷锟斤拷锟角癸拷司锟侥达拷锟斤拷锟斤拷
								// 删锟斤拷锟斤拷司锟斤拷息
								companys.remove(req.companyid);
								MessagesStorage.getInstance()
								.deleteCompany(req.companyid);
								// 删锟斤拷锟斤拷锟斤拷锟斤拷息锟斤拷锟斤拷锟街伙拷锟揭伙拷锟斤拷锟斤拷锟斤拷牛锟斤拷锟矫伙拷胁锟斤拷锟斤拷锟较�
								ArrayList<Integer> delDepts = new ArrayList<Integer>();
								for (ConcurrentHashMap.Entry<Integer, TLRPC.TL_DepartMent> entry : departments
										.entrySet()) {
									TLRPC.TL_DepartMent dept = entry
											.getValue();
									if (dept.companyID == req.companyid) {
										delDepts.add(entry.getKey());
									}
								}
								for (int j = 0; j < delDepts.size(); j++) {
									int deptid = delDepts.get(j);
									departments.remove(deptid);
									departidToUsers.remove(deptid);
									MessagesStorage.getInstance()
									.deleteDepartment(deptid);
								}

								// 删锟斤拷userid,deptid,companyid锟斤拷系,锟斤拷锟斤拷usercompany锟斤拷,删锟斤拷锟斤拷锟斤拷锟斤拷锟剿撅拷锟斤拷锟斤拷锟斤拷锟截的诧拷锟脚猴拷锟斤拷
								delDepts.clear();
								for (ConcurrentHashMap.Entry<Integer, HashSet<TLRPC.TL_UserCompany>> entry : departidToUsers
										.entrySet()) {
									HashSet<TLRPC.TL_UserCompany> depts = entry
											.getValue();
									Iterator<TLRPC.TL_UserCompany> it = depts
											.iterator();
									while (it.hasNext()) {
										TLRPC.TL_UserCompany userCompany = it
												.next();
										if (userCompany.companyID == req.companyid) {
											// 锟斤拷锟斤拷锟斤拷锟斤拷锟剿撅拷锟斤拷锟斤拷泄锟较碉拷锟缴撅拷锟�
											MessagesStorage
											.getInstance()
											.deleteUserCompany(
													userCompany.userID,
													userCompany.companyID);
											delDepts.add(userCompany.deptID);
											String key = getKey(
													userCompany.userID,
													userCompany.companyID);
											userCompanysMap.remove(key);
										}
									}
								}
								for (int j = 0; j < delDepts.size(); j++) {
									int deptid = delDepts.get(j);
									departidToUsers.remove(deptid);
								}
								NotificationCenter
								.getInstance()
								.postNotificationName(
										MessagesController.company_delete,
										req.companyid);
								NotificationCenter
								.getInstance()
								.postNotificationName(
										MessagesController.contactsDidLoaded);
							} else if (req.act == 4) {
								// 锟斤拷锟接筹拷员锟斤拷锟斤拷processUserCompany锟斤拷锟斤拷锟斤拷息锟斤拷UI
							} else if (req.act == 5) {
								// 5:删锟斤拷锟斤拷司锟斤拷员,锟斤拷锟缴撅拷锟斤拷锟斤拷锟斤拷约锟斤拷锟斤拷锟斤拷示锟剿筹拷锟斤拷司

								int size = req.delusers.size();
								for (int i = 0; i < size; i++) {
									TLRPC.User user = req.delusers
											.get(i);
									if (user.id == UserConfig.clientUserId) {
										// 删锟斤拷锟斤拷司锟斤拷息
										companys.remove(req.companyid);
										MessagesStorage.getInstance()
										.deleteCompany(
												req.companyid);
										// 删锟斤拷锟斤拷锟斤拷锟斤拷息锟斤拷锟斤拷锟街伙拷锟揭伙拷锟斤拷锟斤拷锟斤拷牛锟斤拷锟矫伙拷胁锟斤拷锟斤拷锟较�
										ArrayList<Integer> delDepts = new ArrayList<Integer>();
										for (ConcurrentHashMap.Entry<Integer, TLRPC.TL_DepartMent> entry : departments
												.entrySet()) {
											TLRPC.TL_DepartMent dept = entry
													.getValue();
											if (dept.companyID == req.companyid) {
												delDepts.add(entry
														.getKey());
											}
										}
										for (int j = 0; j < delDepts
												.size(); j++) {
											int deptid = delDepts
													.get(j);
											departments.remove(deptid);
											departidToUsers
											.remove(deptid);
											MessagesStorage
											.getInstance()
											.deleteDepartment(
													deptid);
										}

										// 删锟斤拷userid,deptid,companyid锟斤拷系,锟斤拷锟斤拷usercompany锟斤拷,删锟斤拷锟斤拷锟斤拷锟斤拷锟剿撅拷锟斤拷锟斤拷锟斤拷锟截的诧拷锟脚猴拷锟斤拷
										delDepts.clear();
										for (ConcurrentHashMap.Entry<Integer, HashSet<TLRPC.TL_UserCompany>> entry : departidToUsers
												.entrySet()) {
											HashSet<TLRPC.TL_UserCompany> depts = entry
													.getValue();
											Iterator<TLRPC.TL_UserCompany> it = depts
													.iterator();
											while (it.hasNext()) {
												TLRPC.TL_UserCompany userCompany = it
														.next();
												if (userCompany.companyID == req.companyid) {
													// 锟斤拷锟斤拷锟斤拷锟斤拷锟剿撅拷锟斤拷锟斤拷泄锟较碉拷锟缴撅拷锟�
													MessagesStorage
													.getInstance()
													.deleteUserCompany(
															userCompany.userID,
															userCompany.companyID);
													delDepts.add(userCompany.deptID);
													String key = getKey(
															userCompany.userID,
															userCompany.companyID);
													userCompanysMap
													.remove(key);
												}
											}
										}
										for (int j = 0; j < delDepts
												.size(); j++) {
											int deptid = delDepts
													.get(j);
											departidToUsers
											.remove(deptid);
										}
										NotificationCenter
										.getInstance()
										.postNotificationName(
												MessagesController.company_delete,
												req.companyid);
									} else {
										// 锟斤拷锟斤拷锟缴撅拷锟斤拷锟斤拷耍锟斤拷锟斤拷锟揭伙拷锟斤拷锟斤拷锟絀D
										HashSet<TLRPC.TL_UserCompany> depts = departidToUsers
												.get(user.deptid);
										if (depts != null) {
											Iterator<TLRPC.TL_UserCompany> it = depts
													.iterator();
											while (it.hasNext()) {
												TLRPC.TL_UserCompany userCompany = it
														.next();
												if (userCompany.companyID == req.companyid
														&& user.id == userCompany.userID) {
													userCompanysMap
													.remove(getKey(
															user.id,
															req.companyid));
													depts.remove(userCompany);
													MessagesStorage
													.getInstance()
													.deleteUserCompany(
															user.id,
															req.companyid);
													break;
												}
											}
										}
									}
								}
								NotificationCenter
								.getInstance()
								.postNotificationName(
										MessagesController.contactsDidLoaded);
							} else if (req.act == 6) {
								// 锟斤拷示同锟斤拷锟斤拷牍�,通知mainAddress锟斤拷锟斤拷示锟斤拷司锟斤拷息锟斤拷锟斤拷司锟斤拷锟斤拷锟斤拷员
								TLRPC.User user = users
										.get(UserConfig.clientUserId);
								setUserState(req.companyid,
										UserConfig.clientUserId);
								TLRPC.TL_PendingCompanyInfo pendingCompanyInfo = pendingCompanys
										.get(req.companyid);
								if (pendingCompanyInfo != null) {
									pendingCompanyInfo.bAccept = true;
									MessagesStorage.getInstance()
									.putPendingCompany(
											pendingCompanyInfo,
											false, true);
								}
								// 锟斤拷时锟斤拷删锟斤拷锟斤拷只锟斤拷锟矫憋拷志锟斤拷锟矫伙拷锟斤拷锟斤拷锟角凤拷锟斤拷锟斤拷删锟斤拷
								// DeleteInviteCompany(req.companyid);
								NotificationCenter
								.getInstance()
								.postNotificationName(
										MessagesController.pending_company_added,
										req.companyid);
							} else if (req.act == 8) {
								for (int i = 0; i < req.users.size(); i++) {
									NotificationCenter
									.getInstance()
									.postNotificationName(
											MessagesController.company_username_changed,
											req.companyid,
											req.users.get(i));
								}
							}
						}
					});

				}
			}
		});
	}

	public void DeleteInviteCompany(int companyid) {
		// 锟斤拷锟斤拷删锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
		MessagesStorage.getInstance().deletePendingCompany(companyid);
		pendingCompanys.remove(companyid);
		for (int i = 0; i < invitedCompanys.size(); i++) {
			TLRPC.TL_PendingCompanyInfo info = invitedCompanys.get(i);
			if (info != null && info.id == companyid) {
				String inviteName = info.inviteName;
				String name = info.name;
				invitedCompanys.remove(i);
				break;
			}
		}
		NotificationCenter.getInstance().postNotificationName(
				MessagesController.pending_company_added, companyid);
	}

	private void setUserState(int companyid, int userid) {
		String key = getKey(userid, companyid);
		TLRPC.TL_UserCompany company = userCompanysMap.get(key);
		if (company != null) {
			company.ucstate = 0;
		}
	}

	/**
	 * @Title: getUserState
	 * 
	 * @Description: TODO
	 * 
	 * @param companyid
	 * @param userid
	 * @return 0 未注锟斤拷 1锟窖撅拷注锟斤拷
	 */
	public int getUserState(int userid) {

		int size = users.size();
		TLRPC.User user = users.get(userid);
		// status 为 0锟斤拷示未锟斤拷锟筋，为1锟斤拷示锟斤拷锟斤拷锟斤拷锟轿�2锟斤拷示锟窖撅拷删锟斤拷
		if (user != null) {
			// FileLog.e("emm",
			// "userid="+userid+" status="+user.status.expires);
			return user.status.expires;// 锟洁当锟斤拷没锟斤拷装锟斤拷锟斤拷司状态锟斤拷3
		}
		// FileLog.e("emm", "get userid="+userid);
		/*
		 * String key = getKey(userid,companyid); TLRPC.TL_UserCompany company =
		 * userCompanysMap.get(key); if(company!=null) { //0:锟斤拷锟斤拷,1:锟斤拷删锟斤拷锟斤拷锟斤拷 2锟窖帮拷装未同锟斤拷
		 * 3 未锟斤拷装,锟斤拷锟斤拷侄锟斤拷没锟斤拷锟斤拷锟斤拷锟斤拷只锟斤拷没锟斤拷锟阶刺� int state = company.ucstate; return state; }
		 */
		FileLog.e("emm", "getUserState userstate is null");
		return 0;
	}

	private boolean queryUserState(int userid) {
		// 锟斤拷锟斤拷锟斤拷锟斤拷没锟斤拷锟阶刺拷锟斤拷锟斤拷泄锟剿撅拷锟阶刺拷锟斤拷欠锟斤拷锟絬cstate=0锟斤拷状态
		TLRPC.User user = users.get(userid);
		if (user != null) {
			for (ConcurrentHashMap.Entry<Integer, TLRPC.TL_Company> entry : companys
					.entrySet()) {
				int companyid = entry.getKey();
				String key = getKey(userid, companyid);
				TLRPC.TL_UserCompany userCompany = userCompanysMap.get(key);
				if (userCompany != null) {
					if (userCompany.ucstate == 0) {
						return true;
					}
					return false;
				}
			}
		}
		return false;
	}

	public int getUserRole(int companyid, int userid) {
		String key = getKey(userid, companyid);
		TLRPC.TL_UserCompany company = userCompanysMap.get(key);
		if (company != null) {
			int state = company.userRoleID;
			return state;
		}
		return 2;
	}

	public String getCompanyUserName(int companyid, int userid) {
		String key = getKey(userid, companyid);
		TLRPC.TL_UserCompany company = userCompanysMap.get(key);
		if (company != null) {
			return Utilities.formatName(company.first_name, company.last_name);
		}
		return "";
	}

	public String getUserShowName(int companyid, int userid) {
		TLRPC.User user = users.get(userid);
		String nameString = null;
		if (user != null) {
			nameString = user.nickname;
			if (!StringUtil.isEmpty(nameString)) {
				return nameString;
			}
			nameString = getCompanyUserName(companyid, userid);
			if (!StringUtil.isEmpty(nameString)) {
				return nameString;
			}
			return Utilities.formatName(user.first_name, user.last_name);
		}
		return "";
	}


	public void startTimerForMonitorPhoneBook() {
		if (serviceTimer != null)
			return;

		serviceTimer = new Timer();
		serviceTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					long currentTime = System.currentTimeMillis();
					if (UserConfig.clientUserId != 0) {
						if (scheduleContactsReload != 0
								&& currentTime > scheduleContactsReload) {
							scheduleContactsReload = 0;
							ConcurrentHashMap<Integer, Contact> contactHashMap = ContactsController
									.getInstance().getContactsCopy(contactsMap);
							ConcurrentHashMap<Integer, Contact> newContactsMap = ContactsController
									.getInstance().readContactsFromPhoneBook();

							if (!contactHashMap.isEmpty()) {
								final ArrayList<String> contacts = new ArrayList<String>();
								final ConcurrentHashMap<Integer, Contact> newContacts = new ConcurrentHashMap<Integer, Contact>();

								for (HashMap.Entry<Integer, Contact> pair : newContactsMap.entrySet()) 
								{
									//说锟斤拷通讯路锟斤拷锟斤拷锟剿ｏ拷锟斤拷锟斤拷锟斤拷通讯路锟斤拷锟节达拷冉希锟斤拷锟轿伙拷锟斤拷锟斤拷锟斤拷锟斤拷卸锟斤拷锟界话锟斤拷时锟斤拷ID锟斤拷一锟斤拷锟侥ｏ拷锟斤拷PHONE锟斤拷同
									//锟斤拷锟斤拷要锟斤拷锟秸电话锟饺较讹拷锟斤拷锟斤拷id
									Integer id = pair.getKey();
									Contact ct = pair.getValue();
									for (int k = 0; k < ct.shortPhones.size(); k++) 
									{
										// phones锟叫帮拷锟斤拷锟界话锟侥癸拷锟揭猴拷锟斤拷锟�,锟斤拷锟斤拷+锟斤拷00,shortPhones锟叫帮拷锟斤拷锟斤拷锟角诧拷锟斤拷+锟脚的ｏ拷锟斤拷锟斤拷示锟斤拷时锟斤拷锟斤拷锟斤拷锟斤拷穑坎锟斤拷锟斤拷锟斤拷锟�
										String usePhone = ct.shortPhones.get(k);
										String phoneMD5 = Utilities.MD5(usePhone);
										Contact existing = contactsMD5Map.get(phoneMD5);										
										if (existing == null) 
										{
											contactsMap.put(id, ct);
											newContacts.put(id, ct);
											Contact newContact = createNewContact(ct, usePhone);
											contactsMD5Map.put(phoneMD5,newContact);
											contactsMapNew.put(newContact.id,newContact);
											// 锟斤拷锟斤拷锟绞达拷锟斤拷+锟斤拷锟揭达拷锟斤拷+锟街伙拷锟界话锟斤拷锟斤拷锟組D5值
											contacts.add(phoneMD5);						
											FileLog.d("emm", "add new contact"+usePhone);
										}
									}
								}
								if (contacts.size() > 0) 
								{	
									uploadContacts(contacts);	
									Log.e("tag","contacts======" + contacts);
									MessagesStorage.getInstance().updatePhoneBook(newContacts);
								}		

							}
						}
					}
				} catch (Exception e) {
					FileLog.e("emm", e);
				}

			}
		}, 60*1000, 60*1000);
	}

	private Contact createNewContact(Contact ct, String usePhone) {
		Contact newContact = new Contact();
		newContact.first_name = ct.first_name;
		newContact.last_name = ct.last_name;
		// contact id锟斤拷锟角革拷锟斤拷
		newContact.id = (int) ConnectionsManager.getInstance()
				.generateContactId();

		newContact.phone = usePhone;
		return newContact;
	}

	public void processLoadedPendingCompany(
			final ArrayList<TLRPC.TL_PendingCompanyInfo> companys,
			final int from) {
		for (int i = 0; i < companys.size(); i++) {
			TLRPC.TL_PendingCompanyInfo info = companys.get(i);
			if (from == 0)// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟捷诧拷锟斤拷要锟芥储锟斤拷锟斤拷锟斤拷db
				MessagesStorage.getInstance().putPendingCompany(info, false,
						true);
			pendingCompanys.put(info.id, info);
			if (!invitedCompanys.contains(info))
				invitedCompanys.add(info);
		}
		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				// 锟斤拷锟斤拷应锟斤拷锟斤拷应锟斤拷锟斤拷锟较拷锟斤拷锟饺★拷锟斤拷锟斤拷牍撅拷斜锟�,锟斤拷锟捷结构为userCompanys
				NotificationCenter.getInstance().postNotificationName(
						MessagesController.pending_company_loaded);
			}
		});
	}

	public boolean isShowPhoneNumber(int userid) {
		// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷欠锟斤拷锟斤拷业锟紼MAIL锟侥癸拷司锟叫ｏ拷锟斤拷锟角凤拷锟斤拷锟揭碉拷锟斤拷系锟斤拷锟叫憋拷锟斤拷,锟斤拷锟斤拷欠锟斤拷锟絫rue,锟斤拷锟津返伙拷false
		// 锟斤拷锟斤拷锟斤拷要锟斤拷锟接达拷锟斤拷锟斤拷ID锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟角凤拷使锟矫碉拷EMAIL锟斤拷锟斤拷EMAIL锟洁当锟节达拷锟斤拷锟斤拷司锟斤拷
		return true;
	}

	public boolean canCreateCompany() {
		int userid = UserConfig.clientUserId;
		TLRPC.User user = users.get(userid);
		if (user != null) {
			int count = 0;
			for (ConcurrentHashMap.Entry<Integer, TLRPC.TL_Company> entry : companys
					.entrySet()) {
				int companyid = entry.getKey();
				String key = getKey(userid, companyid);
				TLRPC.TL_UserCompany userCompany = userCompanysMap.get(key);
				if (userCompany != null) {
					// 0锟角筹拷锟斤拷锟斤拷锟斤拷1锟斤拷系统锟斤拷锟斤拷员锟斤拷2锟斤拷锟斤拷通锟矫伙拷
					if (userCompany.userRoleID == 0) {
						count++;
					}
				}
			}
			if (count < ConstantValues.CREATE_COMPANY_MAX)
				return true;
		}
		return false;
	}

	public boolean isShowPhoneNumber(int userid, String usePhone) {
		// 锟节革拷锟斤拷锟斤拷息页锟角凤拷锟斤拷示锟界话锟斤拷锟斤拷,锟斤拷要锟斤拷锟斤拷锟斤拷锟斤拷欠锟斤拷锟斤拷锟揭低ㄑ堵硷拷屑锟斤拷欠锟轿拷业锟斤拷锟较碉拷锟�
		// createmode
		return true;
	}

	/**
	 * @Title: bindAccount
	 * 
	 * @Description: TODO
	 * 
	 * @param account
	 * @param bindType
	 *            0:锟斤拷示锟斤拷 1:锟斤拷示锟斤拷锟�
	 * @param codeValue
	 *            锟斤拷证锟斤拷 锟斤拷锟绞憋拷纱锟�""
	 * @param force
	 *            0:锟斤拷示希锟斤拷锟襟定ｏ拷1锟斤拷示强锟狡绑定ｏ拷原锟斤拷锟斤拷锟斤拷锟斤拷锟绞癸拷锟斤拷只锟斤拷锟斤拷锟阶拷幔拷锟绞癸拷锟紼MAIL注锟结，锟斤拷使锟斤拷锟街伙拷锟斤拷锟斤拷锟紼MAIL锟斤拷
	 *            EMAIL锟斤拷锟绞猴拷要删锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷锟矫伙拷
	 *            锟襟定凤拷锟斤拷值为1锟斤拷锟斤拷示锟斤拷锟襟定碉拷锟绞猴拷锟窖撅拷锟斤拷锟节ｏ拷锟角凤拷强锟狡绑定ｏ拷锟斤拷锟角匡拷瓢蠖ǎ锟絝orce锟斤拷要锟斤拷锟斤拷1锟斤拷锟街�
	 */
	public void bindAccount(final String account, final int bindType,
			final String codeValue, final int force) {
		/*
		 * ConnectionsManager.getInstance().bindAccount(UserConfig.clientUserId,
		 * account, bindType,codeValue,force,new RPCRequest.RPCRequestDelegate()
		 * {
		 * 
		 * @Override public void run(TLObject response, TLRPC.TL_error error) {
		 * if (error != null) { if( error.code == 1 || error.code == -1) {
		 * //锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟节诧拷锟斤拷锟矫ｏ拷锟斤拷锟皆猴拷锟斤拷锟斤拷 Utilities.RunOnUIThread(new Runnable() {
		 * 
		 * @Override public void run() { //hz 锟斤拷UI锟叫讹拷锟斤拷锟斤拷锟较拷锟斤拷写锟斤拷锟� if(bindType==0)
		 * NotificationCenter
		 * .getInstance().postNotificationName(bind_account_failed); else
		 * NotificationCenter
		 * .getInstance().postNotificationName(unbind_account_failed); } });
		 * 
		 * } } else { TLRPC.TL_BindResult bindResult =
		 * (TLRPC.TL_BindResult)response; final int result = bindResult.result;
		 * Utilities.RunOnUIThread(new Runnable() {
		 * 
		 * @Override public void run() {
		 * //saveaccount//loadaccount//deleteaccount if(bindType==0) { if(result
		 * == 0 ) { if(EmmUtil.isPhone(account)) { if(accounts.size()>0) {
		 * String oldAccount = accounts.get(0); if(EmmUtil.isPhone(oldAccount))
		 * { accounts.remove(0);
		 * MessagesStorage.getInstance().deleteAccount(oldAccount); } } }
		 * MessagesStorage.getInstance().putAccount(account);
		 * if(!accounts.contains(account)) { if(EmmUtil.isPhone(account)) {
		 * String s = accounts.get(0); if(s.compareTo("")==0)
		 * accounts.remove(0); accounts.add(0, account); } else {
		 * accounts.add(account); } }
		 * ConnectionsManager.getInstance().getUpdate(); FileLog.e("emm",
		 * "bind account="+account); //锟界话锟斤拷锟斤拷 if(EmmUtil.isPhone(account)) {
		 * UserConfig.phone = account; TLRPC.User user =
		 * users.get(UserConfig.clientUserId); if(user!=null) { user.phone =
		 * account; } ArrayList<TLRPC.User> users = new ArrayList<TLRPC.User>();
		 * users.add(user);
		 * MessagesStorage.getInstance().putUsersAndChats(users, null, false,
		 * true); } } //hz 锟斤拷UI锟叫讹拷锟斤拷锟斤拷锟较拷锟斤拷写锟斤拷锟�
		 * NotificationCenter.getInstance().postNotificationName
		 * (MessagesController.bind_account_success,result); } else { for(int
		 * i=0;i<accounts.size();i++) { String cc = accounts.get(i);
		 * FileLog.e("emm", "unbind account="+account+"cc="+cc);
		 * if(cc.compareTo(account)==0) { accounts.remove(i); break; } }
		 * if(EmmUtil.isPhone(account)) { accounts.add(0, ""); }
		 * 
		 * MessagesStorage.getInstance().deleteAccount(account); //hz
		 * 锟斤拷UI锟叫讹拷锟斤拷锟斤拷锟较拷锟斤拷写锟斤拷锟�
		 * NotificationCenter.getInstance().postNotificationName(MessagesController
		 * .unbind_account_success,account,bindType);
		 * ConnectionsManager.getInstance().getUpdate(); } for( int
		 * i=0;i<accounts.size();i++) { FileLog.e("emm",
		 * " processLoadedAccounts account="+accounts.get(i)); } } });
		 * 
		 * } } });
		 */
	}

	public void processLoadedAccounts(final ArrayList<String> loadAccounts,
			final int from) {
		/*
		 * Utilities.RunOnUIThread(new Runnable() {
		 * 
		 * @Override public void run() { for( int i=0;i<loadAccounts.size();i++)
		 * { String sAccount = loadAccounts.get(i); if(from==1) {
		 * FileLog.e("emm", "load accounts from db="+sAccount); }
		 * if(!accounts.contains(sAccount)) { if(EmmUtil.isPhone(sAccount)) {
		 * accounts.add(0,sAccount); } else accounts.add(sAccount); } }
		 * if(!accounts.isEmpty()) { String s = accounts.get(0);
		 * if(!EmmUtil.isPhone(s)) { if(EmmUtil.isPhone(UserConfig.account))
		 * accounts.add(0, UserConfig.getFullName(UserConfig.account)); else
		 * accounts.add(0, ""); } } else {
		 * if(EmmUtil.isPhone(UserConfig.account)) accounts.add(0,
		 * UserConfig.getFullName(UserConfig.account)); else { accounts.add(0,
		 * ""); accounts.add(UserConfig.account); } } for( int
		 * i=0;i<accounts.size();i++) { FileLog.e("emm",
		 * " processLoadedAccounts account="+accounts.get(i)); }
		 * 
		 * } });
		 */
	}

	public void getCode(final String account) {
		// 锟斤拷锟绞猴拷锟斤拷要锟斤拷锟斤拷锟饺★拷锟街わ拷锟�,锟斤拷锟斤拷锟斤拷锟斤拷为2
		ConnectionsManager.getInstance().GetIDCode(account, 2,
				new RPCRequest.RPCRequestDelegate() {
			@Override
			public void run(final TLObject response,
					final TL_error error) {
				if (error != null) {
					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() {
							// hz 锟斤拷UI锟叫讹拷锟斤拷锟斤拷锟较拷锟斤拷写锟斤拷锟�
							NotificationCenter.getInstance()
							.postNotificationName(
									getcode_failed);
						}
					});
					return;
				}

				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						// hz 锟斤拷UI锟叫讹拷锟斤拷锟斤拷锟较拷锟斤拷写锟斤拷锟�
						NotificationCenter.getInstance()
						.postNotificationName(getcode_success);
					}
				});

			}
		});
	}

	public void checkCode(final String account, final String codeValue) {

		ConnectionsManager.getInstance().CheckIDCode(account, codeValue,
				new RPCRequest.RPCRequestDelegate() {
			@Override
			public void run(TLObject response,
					final TLRPC.TL_error error) {
				if (error != null) {
					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() {
							// hz 锟斤拷UI锟叫讹拷锟斤拷锟斤拷锟较拷锟斤拷写锟斤拷锟�
							NotificationCenter.getInstance()
							.postNotificationName(
									checkcode_failed);
						}
					});
					return;
				}
				// 1:校锟斤拷锟斤拷证锟斤拷晒锟�,//说锟斤拷锟斤拷锟斤拷锟矫伙拷锟斤拷锟斤拷锟斤拷一页锟斤拷写锟斤拷锟诫，锟斤拷firstname and lastname
				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						// hz 锟斤拷UI锟叫讹拷锟斤拷锟斤拷锟较拷锟斤拷写锟斤拷锟�
						NotificationCenter
						.getInstance()
						.postNotificationName(checkcode_success);
					}
				});
			}
		});
	}

	public void sendSystemMsg(int uid, int gid, String msg, boolean bSave) {
		if (uid == 0 && gid == 0)
			return;
		TLRPC.TL_messageService message = new TLRPC.TL_messageService();
		TLRPC.TL_peerChat peer = new TLRPC.TL_peerChat();
		peer.user_id = uid;
		peer.chat_id = gid;
		message.id = UserConfig.getSeq();// 锟斤拷证id唯一
		message.from_id = UserConfig.clientUserId;
		message.to_id = peer;
		message.out = false;
		message.unread = true;
		message.date = ConnectionsManager.getInstance().getCurrentTime();
		message.send_state = MessagesController.MESSAGE_SEND_STATE_SENT;
		message.message = msg;

		// TL_messageMediaUnsupported
		TLRPC.TL_messages_call action = new TLRPC.TL_messages_call();
		action.title = msg;
		message.action = action;

		final ArrayList<MessageObject> messagesObj = new ArrayList<MessageObject>();
		messagesObj.add(new MessageObject(message, users));
		if (gid != 0)
			updateInterfaceWithMessages(-gid, messagesObj);
		else
			updateInterfaceWithMessages(uid, messagesObj);

		NotificationCenter.getInstance()
		.postNotificationName(dialogsNeedReload);
		if (bSave) {
			final ArrayList<TLRPC.Message> messages = new ArrayList<TLRPC.Message>();
			messages.add(message);
			MessagesStorage.getInstance().putMessages(messages, true, true);
		}
	}

	public void sendUnsupportMsg(int uid, int gid) {
		if (uid == 0 && gid == 0)
			return;
		TLRPC.TL_messageService message = new TLRPC.TL_messageService();
		TLRPC.TL_peerChat peer = new TLRPC.TL_peerChat();
		peer.user_id = uid;
		peer.chat_id = gid;
		message.id = UserConfig.getSeq();// 锟斤拷证id唯一
		message.from_id = UserConfig.clientUserId;
		message.to_id = peer;
		message.out = false;
		message.unread = true;
		message.date = ConnectionsManager.getInstance().getCurrentTime();
		message.send_state = MessagesController.MESSAGE_SEND_STATE_SENT;

		String messageText = LocaleController.getString("UnsuppotedMedia",
				R.string.UnsuppotedMedia);
		message.message = messageText;

		// TL_messageMediaUnsupported
		TLRPC.TL_messages_call action = new TLRPC.TL_messages_call();
		action.title = messageText;
		message.action = action;

		message.media = new TLRPC.TL_messageMediaUnsupported();

		final ArrayList<MessageObject> messagesObj = new ArrayList<MessageObject>();
		messagesObj.add(new MessageObject(message, users));
		if (gid != 0)
			updateInterfaceWithMessages(-gid, messagesObj);
		else
			updateInterfaceWithMessages(uid, messagesObj);

		final ArrayList<TLRPC.Message> messages = new ArrayList<TLRPC.Message>();
		messages.add(message);
		MessagesStorage.getInstance().putMessages(messages, true, true);

		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				NotificationCenter.getInstance().postNotificationName(
						dialogsNeedReload);
			}
		});

	}

	/**
	 * @param bbsid
	 * @param companyid
	 * @param date
	 * @param settop
	 * @param bbstitle
	 * @param _abstract
	 * @Discription 锟斤拷锟斤拷锟酵癸拷锟斤拷锟侥癸拷锟斤拷锟斤拷息
	 */
	public void processBBSAffiche(int bbsid, int companyid, int date,
			int settop, String bbstitle, String _abstract) {
		TLRPC.TL_Affiche tlAffiche = new TLRPC.TL_Affiche();
		tlAffiche.bbsid = bbsid;
		tlAffiche.companyid = companyid;
		tlAffiche.date = date;
		tlAffiche.settop = settop;
		tlAffiche.bbstitle = bbstitle;
		tlAffiche._abstract = _abstract;
		MessagesStorage.getInstance().addBBSAfficheToDB(tlAffiche);

	}

	private Handler mHandler;

	public Handler getHandler() {
		if (mHandler == null) {
			mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case 1:
						// NotificationCenter.getInstance().postNotificationName(MessagesController.forumafficeupdate);
						//						NotificationCenter.getInstance().postNotificationName(MessagesController.unread_message_update);
						break;
					default:
						break;
					}
				}
			};
		}
		return mHandler;
	}

	public int setRamarkName(final int userid, final int renameid,
			final String name) {
		TLRPC.TL_SetRamarkName req = new TLRPC.TL_SetRamarkName();
		req.userid = userid;
		req.renameid = renameid;
		req.name = name;

		return ConnectionsManager.getInstance().performRpc(req,
				new RPCRequest.RPCRequestDelegate() {
			@Override
			public void run(TLObject response, TLRPC.TL_error error) {
				if (error != null) {
					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() {
							// 锟斤拷锟斤拷通知UI锟斤拷锟斤拷锟斤拷锟斤拷失锟斤拷
							NotificationCenter.getInstance()
							.postNotificationName(renamefailed);
						}
					});
					return;
				}

				// add by xueqiang end

				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						// 锟斤拷锟斤拷通知UI锟斤拷锟斤拷锟斤拷锟斤拷锟缴癸拷
						ArrayList<TLRPC.User> usersList = new ArrayList<TLRPC.User>();
						TLRPC.User user = users.get(renameid);
						if (user != null) {
							// 锟斤拷锟斤拷潜锟阶拷锟�
							user.nickname = name;
							usersList.add(user);
							MessagesStorage.getInstance()
							.putUsersAndChats(usersList, null,
									false, true);
						}
						NotificationCenter.getInstance()
						.postNotificationName(renamesuccess,
								renameid, name);
					}
				});
			}
		});
	}

	public void loadAlertInfo(final String guid) {
		if (guid.isEmpty())
			MessagesStorage.getInstance().getAlertList();
		else
			MessagesStorage.getInstance().getAlert(guid);
	}

	// 锟斤拷锟斤拷锟侥碉拷时锟斤拷锟斤拷茫锟絏UEQIANG TODO..
	public void processAlertInfo(final String guid,
			final ArrayList<TLRPC.TL_alertMedia> info) {
		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				if (guid.isEmpty()) {
					// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷要锟斤拷锟铰凤拷锟斤拷intent
					for (int i = 0; i < info.size(); i++)
						scheduleAlert(info.get(i), true);
				} else
					NotificationCenter.getInstance().postNotificationName(
							alermDidLoaded, guid, info);
			}
		});
	}

	public void addStrangerUser(TLRPC.User user) 
	{
		//陌锟斤拷锟剿诧拷锟杰放碉拷通讯路锟斤拷锟斤拷锟斤拷一锟斤拷锟斤拷志,锟斤拷锟斤拷为锟斤拷通讯录锟斤拷锟剿碉拷时锟斤拷锟斤拷锟斤拷锟斤拷为0,然锟斤拷娲拷锟斤拷锟�
		user.serverid = -1;
		ArrayList<TLRPC.User> userArr = new ArrayList<TLRPC.User>();
		userArr.add(user);
		MessagesStorage.getInstance().putUsersAndChats(userArr, null, true,true);

		users.put(user.id, user);
		usersSDK.put(user.identification, user);
	}

	public boolean CheckCompanyID(int nCompanyID) {

		for (ConcurrentHashMap.Entry<Integer, TLRPC.TL_DepartMent> entry : departments
				.entrySet()) {
			TLRPC.TL_DepartMent dept = entry.getValue();
			if (dept.companyID == nCompanyID) {
				if (dept.id > 10000) {
					return true;
				}
			}
		}
		return false;
	}

	// /////////////////////////////////////////////////////
	// PSTN 锟斤拷锟斤拷锟斤拷锟� addby 锟斤拷锟睫憋拷
	// ////////////////////////////////////////////////////
	public void ControlPSTNMeeting(final int nAction,
			final TLRPC.TL_PSTNMeeting PSTNm) {

		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				ConnectionsManager.getInstance().ControlPSTNmeeting(nAction,
						PSTNm, new RPCRequest.RPCRequestDelegate() {

					@Override
					public void run(TLObject response, TL_error error) {

						final TLRPC.TL_PSTNResponse retresponse = (TLRPC.TL_PSTNResponse) response;
						if (error != null) {
							Utilities.RunOnUIThread(new Runnable() {
								@Override
								public void run() {
									NotificationCenter
									.getInstance()
									.postNotificationName(
											MessagesController.PSTNControl_Notify_error,
											nAction,
											retresponse);
								}
							});
						} else if (retresponse != null) {
							Utilities.RunOnUIThread(new Runnable() {
								@Override
								public void run() {
									if (nAction == 1) {
										PSTNm.conferenceId = retresponse.newconferenceId;
										if (!StringUtil
												.isEmpty(PSTNm.conferenceId)) {
											if (meeting2Map != null) {
												meeting2Map
												.put(PSTNm.conferenceId,
														PSTNm);
												meeting2List.add(0,
														PSTNm);
											}
											MessagesStorage
											.getInstance()
											.updateMeeting2(
													PSTNm);
										}
									} else if (nAction == 5) { // 锟斤拷取状态

									} else if (nAction == 4) {// 锟斤拷锟狡筹拷员

									}
									NotificationCenter
									.getInstance()
									.postNotificationName(
											MessagesController.PSTNControl_Notify,
											retresponse);
								}
							});
						}
					}
				});
			}
		});
	}
	public void meetingCall(String mid, int gid, ArrayList<Integer> users,int action) {
		if(action == 0 )
		{			
			if( users.size() > 0)
			{
				//锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
				int peerid = users.get(0);
				IMRtmpClientMgr.getInstance().setPeerID(peerid);
				IMRtmpClientMgr.getInstance().setChatId(gid);

			}

			IMRtmpClientMgr.getInstance().startTimer(true);
		}
		ConnectionsManager.getInstance().meetingCall(mid, gid, users, action);
	}

	@Override
	public void onPresentComplete(ArrayList<Integer> thirduids,int chatid,String mid) {
		// TODO Auto-generated method stub
		//锟斤拷锟斤拷1锟斤拷1锟斤拷锟斤拷锟介，锟斤拷锟斤拷要锟斤拷锟斤拷锟斤拷锟叫硷拷锟斤拷锟斤拷锟斤拷
		NotificationCenter.getInstance().postNotificationName(MessagesController.EnterMeeting_Complete);
		if(thirduids.size()==0)
		{	
			//this.meetingCall(mid, gid, users, action);
			IMRtmpClientMgr.getInstance().dial(ToneGenerator.TONE_SUP_RINGTONE);
			//锟斤拷示锟斤拷锟斤拷锟�
			String temp = LocaleController.getString("callingpeer",R.string.callingpeer);
			ArrayList<Integer> users = new ArrayList<Integer>();
			if(chatid<0)
			{
				MessagesController.getInstance().sendSystemMsg(0,-chatid, temp, true);
				meetingCall(mid,-chatid, users, 0);
			}
			else
			{
				users.add(chatid);
				MessagesController.getInstance().sendSystemMsg(chatid,0, temp, true);
				meetingCall(mid,0, users, 0);
			}


		}
	}

	@Override
	public void onUserIn(int thirdID,int chatid) {
		// TODO Auto-generated method stub
		IMRtmpClientMgr.getInstance().stopTimer();
		IMRtmpClientMgr.getInstance().stopdial();
	}

	@Override
	public void onUserOut(ArrayList<Integer> thirduids,int chatid) {
		// TODO Auto-generated method stub
		if(chatid<0)
		{
			//String temp = LocaleController.getString("voiceend",R.string.voiceend);
			//MessagesController.getInstance().sendSystemMsg(0,-chatid, temp, true);

		}
		else
		{
			//锟斤拷锟斤拷锟斤拷锟诫开锟剿ｏ拷锟斤拷锟斤拷要锟皆讹拷锟剿筹拷锟斤拷锟斤拷
			if(thirduids.size()==0)
			{
				IMRtmpClientMgr.getInstance().setReceiveCall(false);
				WeiyiMeeting.getInstance().exitMeeting();
				String temp = LocaleController.getString("voiceend",R.string.voiceend);
				MessagesController.getInstance().sendSystemMsg(chatid,0, temp, true);
			}
		}
	}
	@Override
	public void onExitMeeting(int chatid,ArrayList<Integer> thirduids,String mid) {
		IMRtmpClientMgr.getInstance().stopTimer();
		IMRtmpClientMgr.getInstance().stopdial();		
		if(chatid>0)
		{
			IMRtmpClientMgr.getInstance().setReceiveCall(false);
			//锟斤拷示1锟斤拷1锟斤拷锟斤拷
			if(thirduids.size()==0)
			{
				//锟斤拷锟斤拷取锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
				// 锟斤拷锟叫筹拷时,锟斤拷锟斤拷取锟斤拷锟斤拷锟斤拷
				ArrayList<Integer> users = new ArrayList<Integer>();
				users.add(chatid);
				meetingCall(mid,0, users, 1);
			}
			else
			{
				String temp = LocaleController.getString("voiceend",R.string.voiceend);
				MessagesController.getInstance().sendSystemMsg(chatid,0, temp, true);
			}
		}		
	}
	private int getCompanyID()
	{
		for (ConcurrentHashMap.Entry<Integer, TLRPC.TL_Company> entry : companys.entrySet()) {
			return entry.getKey();
		}

		return 0;
	}

	public TLRPC.TL_UserCompany getCompanyUser(int userid)
	{
		String key = getKey(userid, getCompanyID());
		TLRPC.TL_UserCompany company = userCompanysMap.get(key);
		return company;
	}

	public int getSize(int mid)
	{
		ArrayList<TLRPC.TL_DirectPlayBackList> list = MessagesController.getInstance().directMap.get(mid);
		int size = list.size();	
		return size;
	}

	public void processBroadCastImg(TLRPC.TL_DirectPlayBackList info,final int from/*0:network,1:local*/){	
		//锟斤拷锟侥硷拷锟角凤拷锟斤拷诘锟斤拷卸锟�
		if(directMap.get(info.mId)==null){
			ArrayList<TLRPC.TL_DirectPlayBackList> list = new ArrayList<TLRPC.TL_DirectPlayBackList>();
			list.add(info);
			directMap.put(info.mId,list);
		}else{
			List<TLRPC.TL_DirectPlayBackList> imgList = directMap.get(info.mId);
			boolean ishave = false;
			for (int i = 0; i < imgList.size(); i++) {
				if(imgList.get(i).livevideoid==info.livevideoid){
					ishave = true;
				}
			}
			if(!ishave){				
				directMap.get(info.mId).add(info);
			}
		}
		if(from==0)
		{	Log.e("TAG", "MessagesStorage...");
		MessagesStorage.getInstance().processBroadCastImg(info);
		}
	}

	//直锟斤拷锟截凤拷  qxm add
	public void processPlayBack(final ArrayList<TL_DirectPlayBackList> infos ,final int from){

		Utilities.RunOnUIThread(new Runnable() {

			@Override
			public void run() {
				if(infos != null){

					for(int i = 0; i < infos.size();i++)
					{	
						TLRPC.TL_DirectPlayBackList info = infos.get(i);
						processBroadCastImg(info,from);
					}
					NotificationCenter.getInstance().postNotificationName(directplayback_notify);
				}				
			}
		});
	}
	@SuppressLint("SimpleDateFormat")
	public void GroupAndChildList(int year_c,int month_c, String currrentGroupDate){
		SpecialCalendar sc = new SpecialCalendar();
		Date nextDay = getDateNext(currrentGroupDate, 42);
		if(meetingList != null){
			if(meetingMap.size()>0){
				meetingMap.clear();
			}
			for(int j = 0;j<meetingList.size();j++){

				ArrayList<TLRPC.TL_MeetingInfo> childList;
				TLRPC.TL_MeetingInfo mt = meetingList.get(j);
				if(mt != null){
					int meetingType =mt.meetingType;
					int endTime = mt.endTime;
					String MMDD = DateUnit.getMMDDDate(mt.startTime);

					//					Date dateBegin = null;
					//					SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
					Date dateBegin = DateUnit.StringToDate(MMDD);
					//					try{
					//						dateBegin=formater.parse(MMDD);
					//					}catch(Exception e){
					//						e.printStackTrace();
					//					}
					Calendar ca=Calendar.getInstance(); 

					if(meetingType == 0){//锟斤拷通锟斤拷锟斤拷
						if(endTime == 0){//锟斤拷锟节伙拷锟斤拷
							while(dateBegin.compareTo(nextDay)<=0){  
								String yyyyMMddE = DateUnit.getStringyyyyMMdd(dateBegin); 
								boolean hasKeys = meetingMap.containsKey(yyyyMMddE);
								if(!hasKeys){
									childList = new ArrayList<TLRPC.TL_MeetingInfo>();
									childList.add(mt);
									meetingMap.put(yyyyMMddE,childList);
								}else{
									childList = meetingMap.get(yyyyMMddE);
									if(!childList.contains(mt.mid))
										childList.add(mt);
								}
								ca.setTime(dateBegin);   
								ca.add(ca.DATE,1);//锟斤拷dateBegin锟斤拷锟斤拷1锟斤拷然锟斤拷锟斤拷锟铰革拷值锟斤拷date1   
								dateBegin=ca.getTime();  
							}
						}else{
							boolean hasKeys = meetingMap.containsKey(MMDD);
							if(!hasKeys){
								childList = new ArrayList<TLRPC.TL_MeetingInfo>();
								childList.add(mt);
								meetingMap.put(MMDD,childList);
							}else{
								childList = meetingMap.get(MMDD);
								childList.add(mt);
							}
						}
					}else if(meetingType == 3){//每锟斤拷
						while(dateBegin.compareTo(nextDay)<=0){  
							String yyyyMMddE = DateUnit.getStringyyyyMMdd(dateBegin); 
							boolean hasKeys = meetingMap.containsKey(yyyyMMddE);
							if(!hasKeys){
								childList = new ArrayList<TLRPC.TL_MeetingInfo>();
								childList.add(mt);
								meetingMap.put(yyyyMMddE,childList);
							}else{
								childList = meetingMap.get(yyyyMMddE);
								childList.add(mt);
							}
							ca.setTime(dateBegin);   
							ca.add(ca.DATE,1);//锟斤拷dateBegin锟斤拷锟斤拷1锟斤拷然锟斤拷锟斤拷锟铰革拷值锟斤拷date1   
							dateBegin=ca.getTime();  
						}
					}else if(meetingType == 4){//每锟斤拷  
						while(dateBegin.compareTo(nextDay)<=0){  
							String yyyyMMddE = DateUnit.getStringyyyyMMdd(dateBegin);  
							Long startTime = DateUnit.getLongSort(MMDD);
							Long checkTime = DateUnit.getLongSort(yyyyMMddE);
							boolean hasKeys = meetingMap.containsKey(yyyyMMddE);
							if(startTime<=checkTime){
								if(!hasKeys){
									childList = new ArrayList<TLRPC.TL_MeetingInfo>();
									childList.add(mt);
									meetingMap.put(yyyyMMddE,childList);
								}else{
									childList = meetingMap.get(yyyyMMddE);
									childList.add(mt);
								}
							}
							ca.setTime(dateBegin);   
							ca.add(ca.DATE,7);//锟斤拷dateBegin锟斤拷锟斤拷1锟斤拷然锟斤拷锟斤拷锟铰革拷值锟斤拷date1   
							dateBegin=ca.getTime();  
						}
					}else if(meetingType == 5){
						while(dateBegin.compareTo(nextDay)<=0){  
							String yyyyMMddE = DateUnit.getStringyyyyMMdd(dateBegin);  
							Long startTime = DateUnit.getLongSort(MMDD);
							Long checkTime = DateUnit.getLongSort(yyyyMMddE);
							boolean hasKeys = meetingMap.containsKey(yyyyMMddE);
							if(startTime<=checkTime){
								if(!hasKeys){
									childList = new ArrayList<TLRPC.TL_MeetingInfo>();
									childList.add(mt);
									meetingMap.put(yyyyMMddE,childList);
								}else{
									childList = meetingMap.get(yyyyMMddE);
									childList.add(mt);
								}
							}
							ca.setTime(dateBegin);   
							ca.add(ca.DATE,14);//锟斤拷dateBegin锟斤拷锟斤拷1锟斤拷然锟斤拷锟斤拷锟铰革拷值锟斤拷date1   
							dateBegin=ca.getTime();  
						}
					}else if(meetingType == 6){//每锟斤拷
						String day = mt.beginTime.substring(0,2);//锟斤拷锟斤拷:锟斤拷始时锟斤拷 
						int intDay = Integer.parseInt(day);
						for(int i = month_c;i<=month_c+2;i++){
							int months;
							int year ;
							String strMonth;
							if(i<=12){
								months = i;
								year = year_c;
								if(i<10)
									strMonth = "0"+i;
								else
									strMonth = i+"";
							}else{
								months = 1;
								year = year_c+1;
								strMonth = "0"+1;
							}
							int dayNum = sc.getDaysOfMonth(sc.isLeapYear(year), months);
							if(intDay<=dayNum){
								StringBuffer buffer = new StringBuffer();
								buffer.append(year).append("-").append(strMonth).append("-").append(day);
								String checkTime = buffer.toString();
								Long startTime = DateUnit.getLongSort(MMDD);
								Long checkTimes = DateUnit.getLongSort(checkTime);
								if(startTime<=checkTimes){
									boolean hasKeys = meetingMap.containsKey(checkTime);
									if(!hasKeys){
										childList = new ArrayList<TLRPC.TL_MeetingInfo>();
										childList.add(mt);
										meetingMap.put(checkTime,childList);
									}else{
										childList = meetingMap.get(checkTime);
										childList.add(mt);
									}
								}
							}

						}	

						//							String yyyyMMddE = DateUnit.getStringyyyyMMdd(dateBegin);  
						//							String E = yyyyMMddE.substring(8, 10);//2016-04-12 锟斤拷锟斤拷
						//							Long startTime = DateUnit.getLongSort(MMDD);
						//							Long checkTime = DateUnit.getLongSort(yyyyMMddE);
						//							boolean hasKeys = meetingMap.containsKey(yyyyMMddE);
						//							if(E.equals(day) && startTime<=checkTime){
						//								if(!hasKeys){
						//									childList = new ArrayList<TLRPC.TL_MeetingInfo>();
						//									childList.add(mt);
						//									meetingMap.put(yyyyMMddE,childList);
						//								}else{
						//									childList = meetingMap.get(yyyyMMddE);
						//									childList.add(mt);
						//								}
						//							}
						//							ca.setTime(dateBegin);   
						//							ca.add(ca.DATE,1);//锟斤拷dateBegin锟斤拷锟斤拷1锟斤拷然锟斤拷锟斤拷锟铰革拷值锟斤拷date1   
						//							dateBegin=ca.getTime();  
						//						}
					}
				}
			}
		}
		Set keySet = meetingMap.keySet();
		for (Object keyName : keySet) {
			groupLists.add(keyName.toString());
		}
		set = groupLists.subSet(currrentGroupDate,true, DateUnit.getStringyyyyMMdd(nextDay),true);
	}
	/**
	 * 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
	 * @param currentTime
	 * @param day
	 * @return
	 */
	private Date getDateNext(String currentTime, int day){
		Date d;
		Calendar now = null;
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
		try {
			d = formater.parse(currentTime);
			now = Calendar.getInstance();    
			now.setTime(d);    
			now.set(Calendar.DATE, now.get(Calendar.DATE) + day);    

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return  now.getTime();
	}

	public String getListDateByPos(int pos){
		if(pos >= set.size())
			return null;
		String t = (String)set.toArray()[pos];
		return t;
	}
}

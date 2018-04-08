/*
mess\ * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.messenger;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;
import android.webkit.MimeTypeMap;
import info.emm.LocalData.*;
//import info.emm.messenger.MQListener;
import info.emm.messenger.TLRPC.TL_MeetingInfo;
import info.emm.messenger.TLRPC.TL_chat;
import info.emm.messenger.TLRPC.TL_error;
import info.emm.objects.MessageObject;
import info.emm.ui.ApplicationLoader;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.msgpack.MessagePack;//only for test

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.msgpack.type.ValueType;
import org.msgpack.unpacker.Unpacker;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

public class ConnectionsManager implements MQListener {
	public static boolean DEBUG_VERSION = true;
	public static int APP_ID = 2458;
	public static String APP_HASH = "5bce48dc7d331e62c955669eb7233217";
	public static String HOCKEY_APP_HASH = "dedae71020c1c014120ef0153cb8457c";
	public static String GCM_SENDER_ID = "760348033672";
	public static final boolean enableAudio = DEBUG_VERSION;
	public volatile int connectionState = 2;
	public static final int DEFAULT_DATACENTER_ID = Integer.MAX_VALUE;
	public static final int DC_UPDATE_TIME = 60 * 60;

	// xiaoyang
	public static final int LOCATION = 123456789;
	// xiaoyang
	public int currentDatacenterId;
	public int movingToDatacenterId;
	private long lastOutgoingMessageId = 0;
	private int lastContactId = 0;
	public int timeDifference = 0;
	public int currentPingTime = 0;
	public int lastConnectStatus = 2; // jenf for connect status

	public static final boolean isDebugSession = false;
	private static volatile ConnectionsManager Instance = null;
	// add by xueqiang
	// private ArrayList<String> m_serverList= new ArrayList<String>();
	private HashMap<Integer, RPCRequest> msgMap = new HashMap<Integer, RPCRequest>();
	private int mqttVersion = 1;// 目前锟斤拷1锟斤拷锟皆猴拷锟斤拷锟斤拷使锟斤拷锟金渐硷拷1锟侥凤拷式

	public static AsyncHttpClient client = new AsyncHttpClient();
	// 锟斤拷锟斤拷锟斤拷锟斤拷息锟斤拷锟斤，锟斤拷锟斤拷锟截碉拷锟斤拷没锟叫碉拷时锟斤拷锟斤拷时锟斤拷锟斤拷锟斤拷息锟斤拷去锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟较拷锟饺★拷锟斤拷锟斤拷锟揭伙拷锟斤拷锟斤拷锟劫达拷锟斤拷锟斤拷锟斤拷锟斤拷械锟斤拷锟较�
	private ArrayList<TLRPC.Updates> updatesQueue = new ArrayList<TLRPC.Updates>();

	public ConcurrentHashMap<Integer, TLRPC.TL_MeetingCall> meetingcallMap = new ConcurrentHashMap<Integer, TLRPC.TL_MeetingCall>(
			100, 1.0f, 2);
	private int lastVersion = -1;
	private int versionNUM = -1;
	private RequestHandle request = null;

	// sam
	public boolean connected = false;
	public MQService.MQtoolsBinder mqBinder;
	Timer serviceTimer = new Timer();

	public static ConnectionsManager getInstance() {
		ConnectionsManager localInstance = Instance;
		if (localInstance == null) {
			synchronized (ConnectionsManager.class) {
				localInstance = Instance;
				if (localInstance == null) {
					Instance = localInstance = new ConnectionsManager();
					Instance.startUpdateTimer();
				}
			}
		}
		return localInstance;
	}

	// public void SetServiceBinder(MQTools.MQtoolsBinder binder)
	// {
	// mqBinder = binder;
	// mqBinder.setOnListenMessage(this);
	// }
	// public void ConnectServer()
	// {
	// mqBinder.connectServ();
	// }
	// public void disConnect()
	// {
	// mqBinder.disconnectServ();
	// }
	public void StartNetWorkService() {
		// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
		// MQService.getInstance().start(ApplicationLoader.applicationContext);
		// MQService.getInstance().connectServ();

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// 锟斤拷锟斤拷锟斤拷息锟斤拷锟斤拷MQToolsService
				Intent svc = new Intent(ApplicationLoader.applicationContext,
						MQService.class);
				ApplicationLoader.applicationContext.startService(svc);
			}
		}, 1);
	}

	public void disconnectServ() {
		if (mqBinder != null && connected == true) {
			mqBinder.disconnectServ();
			connected = false;
		}
	}

	public void buildPhoto(TLRPC.User user) {
		// 锟斤拷示锟斤拷锟矫伙拷图锟斤拷母锟斤拷模锟斤拷锟斤拷锟斤拷芨牡锟街伙拷锟斤拷锟斤拷郑锟接︼拷煤锟皆拷锟斤拷谋冉希锟斤拷欠锟斤拷锟揭拷锟斤拷锟酵计拷锟斤拷锟斤拷嗟憋拷锟矫匡拷味锟斤拷锟斤拷兀锟斤拷锟斤拷遣锟斤拷缘锟�
		if (user.userico != "null" && user.userico.compareTo("") != 0) {
			user.photo = new TLRPC.TL_userProfilePhoto();
			user.photo.photo_id = UserConfig.getSeq();

			TLRPC.FileLocation small_location = new TLRPC.TL_fileLocation();
			small_location.volume_id = UserConfig.getSeq();
			small_location.local_id = UserConfig.clientUserId;// local_id+volume_id锟斤拷锟斤拷锟斤拷一锟斤拷锟斤拷锟斤拷锟侥硷拷锟斤拷锟斤拷锟节讹拷锟绞猴拷锟铰诧拷锟斤拷锟截革拷锟斤拷锟斤拷锟斤拷要锟矫憋拷锟斤拷ID锟斤拷锟斤拷VOLUME_id锟斤拷使锟截革拷也没锟斤拷系;
			small_location.secret = 0;
			small_location.dc_id = 5;
			small_location.key = null;
			small_location.iv = null;
			String smallUrl = Config.getWebHttp() + user.userico + "_small";
			small_location.http_path_img = smallUrl;

			user.photo.photo_small = small_location;

			TLRPC.FileLocation big_location = new TLRPC.TL_fileLocation();
			big_location.volume_id = UserConfig.getSeq();
			big_location.local_id = UserConfig.clientUserId;
			big_location.secret = 0;
			big_location.dc_id = 5;
			big_location.key = null;
			big_location.iv = null;
			big_location.http_path_img = Config.getWebHttp() + user.userico;
			user.photo.photo_big = big_location;
		} else
			user.photo = new TLRPC.TL_userProfilePhotoEmpty();
	}

	public TLRPC.ChatPhoto buildChatPhoto(String chatico) {
		if (chatico != "null" && chatico.compareTo("") != 0) {
			TLRPC.FileLocation small_location = new TLRPC.TL_fileLocation();
			small_location.volume_id = UserConfig.getSeq();
			small_location.local_id = UserConfig.clientUserId;
			small_location.secret = 0;
			small_location.dc_id = 5;
			small_location.key = null;
			small_location.iv = null;
			String smallUrl = Config.getWebHttp() + chatico + "_small";

			small_location.http_path_img = smallUrl;

			TLRPC.FileLocation big_location = new TLRPC.TL_fileLocation();
			big_location.volume_id = UserConfig.getSeq();
			big_location.local_id = UserConfig.clientUserId;
			big_location.secret = 0;
			big_location.dc_id = 5;
			big_location.key = null;
			big_location.iv = null;
			big_location.http_path_img = Config.getWebHttp() + chatico;

			TLRPC.TL_chatPhoto chatphoto = new TLRPC.TL_chatPhoto();
			chatphoto.photo_big = big_location;
			chatphoto.photo_small = small_location;
			return chatphoto;
		}
		return new TLRPC.TL_chatPhotoEmpty();
	}

	private void createGroupName(TLRPC.TL_chat newChat,
			ArrayList<Integer> groupusers) {
		// 锟斤拷锟斤拷锟斤拷锟斤拷为锟秸ｏ拷使锟矫诧拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟缴憋拷锟斤拷
		if (newChat.title == null || newChat.title.isEmpty()) {
			int count = groupusers.size();
			if (count > 5)
				count = 5;
			for (int k = 0; k < count; k++) {
				TLRPC.User user = MessagesController.getInstance().users
						.get(groupusers.get(k));
				String nameString = Utilities.formatName(user);

				if (newChat.title == null || newChat.title.compareTo("") == 0) {
					newChat.title = nameString;
				} else {
					newChat.title = newChat.title + "," + nameString;
				}
			}
		}
	}

	public void sendGroupMessage(int type, int fromid,
			ArrayList<Integer> groupusers, TLRPC.TL_chat newChat,
			int invitedid, int groupcreatetime, TLRPC.TL_updates updates) {
		// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟�,锟斤拷锟斤拷锟斤拷锟轿拷盏锟�

		int chatid = newChat.id;

		// TLRPC.TL_updates updates = new TLRPC.TL_updates();

		TLRPC.TL_updateNewMessage update = new TLRPC.TL_updateNewMessage();

		// 锟斤拷锟届创锟斤拷锟斤拷锟斤拷锟较�
		TLRPC.TL_messageService message = new TLRPC.TL_messageService();

		TLRPC.TL_peerChat peer = new TLRPC.TL_peerChat();
		peer.chat_id = chatid;
		message.id = UserConfig.getSeq();// 锟斤拷证id唯一
		message.from_id = fromid;
		message.to_id = peer;
		message.out = false;
		if (lastVersion == -1)
			message.unread = false;
		else
			message.unread = true;
		// 只锟叫达拷锟斤拷锟斤拷锟绞憋拷锟侥憋拷锟斤拷锟绞憋拷锟�,xueqiang todo..
		message.date = groupcreatetime;

		TLRPC.MessageAction action = null;
		if (type == 0) {
			// 删锟斤拷锟斤拷锟矫伙拷
			action = new TLRPC.TL_messageActionChatDeleteUser();

			for (int i = 0; i < groupusers.size(); i++) {
				// 锟斤拷锟斤拷锟斤拷锟矫伙拷删锟斤拷锟斤拷锟斤拷息
				TLRPC.Update delUpdate = new TLRPC.TL_updateChatParticipantDelete();
				delUpdate.chat_id = chatid;
				delUpdate.user_id = groupusers.get(i);
				updates.updates.add(delUpdate);
				action.user_id = delUpdate.user_id;
			}
		}
		if (type == 1) {
			action = new TLRPC.TL_messageActionChatAddUser();
			for (int i = 0; i < groupusers.size(); i++) {
				// 锟斤拷锟斤拷锟斤拷锟矫伙拷锟斤拷锟接碉拷锟斤拷息
				TLRPC.Update addUpdate = new TLRPC.TL_updateChatParticipantAdd();
				addUpdate.inviter_id = invitedid;
				addUpdate.chat_id = chatid;
				addUpdate.user_id = groupusers.get(i);
				updates.updates.add(addUpdate);
				action.user_id = addUpdate.user_id;
			}
		}
		if (type == 2) {
			// 锟睫革拷锟斤拷锟斤拷锟斤拷锟�
			action = new TLRPC.TL_messageActionChatEditTitle();
			// 锟斤拷示锟矫伙拷锟斤拷锟斤拷锟斤拷锟斤拷谋锟斤拷锟�
			newChat.hasTitle = 0;
			action.user_id = fromid;
		}

		if (type == 3) {
			// 锟斤拷锟斤拷锟斤拷一锟斤拷锟斤拷锟斤拷
			action = new TLRPC.TL_messageActionChatCreate();
			for (int i = 0; i < groupusers.size(); i++) {
				action.users.add(groupusers.get(i));
				updates.users.add(MessagesController.getInstance().users
						.get(groupusers.get(i)));
			}
			action.user_id = fromid;
			createGroupName(newChat, groupusers);
		}
		if (type == 4) {
			// 锟睫革拷锟斤拷锟斤拷头锟斤拷
			if (newChat.chatico != null && !newChat.chatico.isEmpty()) {
				action = new TLRPC.TL_messageActionChatEditPhoto();
				TLRPC.TL_photo photo = new TLRPC.TL_photo();

				action.photo = photo;
				action.user_id = fromid;

				photo.user_id = fromid;
				photo.geo = new TLRPC.TL_geoPointEmpty();
				photo.id = UserConfig.getSeq();// 唯一锟酵匡拷锟斤拷
				photo.caption = "";
				photo.user_id = fromid;
				photo.date = getCurrentTime();

				TLRPC.FileLocation small_location = new TLRPC.TL_fileLocation();
				small_location.volume_id = UserConfig.getSeq();
				small_location.local_id = UserConfig.clientUserId;
				small_location.secret = 0;
				small_location.dc_id = 5;
				small_location.key = null;
				small_location.iv = null;
				String smallUrl = Config.getWebHttp() + newChat.chatico
						+ "_small";
				// FileLog.d("emm", "smallurl="+smallUrl);
				small_location.http_path_img = smallUrl;

				TLRPC.PhotoSize smallSize = new TLRPC.TL_photoSize();
				smallSize.type = "s";
				smallSize.location = small_location;
				smallSize.w = 160;
				smallSize.h = 160;
				smallSize.size = 0;
				smallSize.bytes = null;
				photo.sizes.add(smallSize);

				TLRPC.FileLocation big_location = new TLRPC.TL_fileLocation();
				big_location.volume_id = UserConfig.getSeq();
				big_location.local_id = UserConfig.clientUserId;
				big_location.secret = 0;
				big_location.dc_id = 5;
				big_location.key = null;
				big_location.iv = null;
				big_location.http_path_img = Config.getWebHttp()
						+ newChat.chatico;

				TLRPC.PhotoSize bigSize = new TLRPC.TL_photoSize();
				bigSize.type = "x";
				bigSize.location = big_location;
				bigSize.w = 800;
				bigSize.h = 800;
				bigSize.size = 0;
				bigSize.bytes = null;
				photo.sizes.add(bigSize);

				TLRPC.TL_chatPhoto chatphoto = new TLRPC.TL_chatPhoto();
				chatphoto.photo_big = big_location;
				chatphoto.photo_small = small_location;
				newChat.photo = chatphoto;
			} else // 删锟斤拷锟斤拷锟斤拷头锟斤拷
			{
				action = new TLRPC.TL_messageActionChatDeletePhoto();
				action.photo = new TLRPC.TL_photoEmpty();
				action.user_id = fromid;
				// action.photo.user_id =fromid;
				// action.photo.geo = new TLRPC.TL_geoPointEmpty();
				// action.photo.id=UserConfig.getSeq();//唯一锟酵匡拷锟斤拷
				// action.photo.caption = "";
				// action.photo.user_id=fromid;
				// action.photo.date = getCurrentTime();
				newChat.photo = new TLRPC.TL_chatPhotoEmpty();
			}
		}

		action.title = newChat.title;
		message.action = action;
		message.from_id = fromid;
		TLRPC.TL_peerChat to_id = new TLRPC.TL_peerChat();
		message.to_id = to_id;
		to_id.chat_id = chatid;
		update.message = message;
		// updates.chats.add(newChat);
		updates.updates.add(update);

	}

	private void parseGetGroupResult(final JSONObject jo) {
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				parseGetGroupResultInner(jo);
			}
		});
	}

	private int parseGetGroupResultInner(JSONObject jo) {
		try {
			if (jo.has("result") && jo.getInt("result") != 0)
				return -1;

			TLRPC.TL_updates updates = new TLRPC.TL_updates();

			if (jo.has("group") && !jo.isNull("group")) {
				JSONArray group = (JSONArray) jo.getJSONArray("group");
				int glen = group.length();
				for (int i = 0; i < glen; i++) {
					int type = 0;// 删锟斤拷为0锟斤拷锟斤拷锟斤拷为1锟斤拷锟睫革拷为2(锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟酵凤拷锟斤拷薷模锟斤拷锟斤拷锟斤拷锟轿�3
					JSONObject jGroup = group.getJSONObject(i);
					int groupid = jGroup.optInt("groupid", 0);
					int inviteid = jGroup.optInt("createuserid", 0);
					// groupname锟斤拷锟斤拷锟斤拷null,也锟斤拷锟斤拷锟斤拷要通锟斤拷锟斤拷某锟皆憋拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
					String groupname = jGroup.getString("groupname");
					int companyid = jGroup.optInt("companyid", 0);
					int updatetitleid = jGroup.optInt("updatetitleid", 0);
					int updatephotoid = jGroup.optInt("updatephotoid", 0);
					String groupico = "";
					if (jGroup.has("groupico") && !jGroup.isNull("groupico"))
						groupico = jGroup.getString("groupico");

					int grouptype = jGroup.optInt("grouptype", 0);

					// xueqiang change for create group time
					int groupcreatetime = jGroup.optInt("createtime",
							getCurrentTime());

					ArrayList<Integer> addgroupusers = new ArrayList<Integer>();
					addgroupusers.clear();

					ArrayList<Integer> removegroupusers = new ArrayList<Integer>();
					removegroupusers.clear();

					TLRPC.TL_chat oldChat = (TL_chat) MessagesController
							.getInstance().chats.get(groupid);
					if (oldChat == null) {
						TLRPC.TL_chat newChat = new TLRPC.TL_chat();
						newChat.id = groupid;
						newChat.title = groupname;
						if (groupname.isEmpty()
								|| groupname.compareTo("null") == 0)
							newChat.hasTitle = -1;
						else
							newChat.hasTitle = 0;
						newChat.chatico = groupico;// 锟斤拷锟酵凤拷锟接︼拷锟斤拷锟斤拷锟�
						newChat.photo = new TLRPC.TL_chatPhotoEmpty();// 原锟斤拷锟斤拷PHOTO锟斤拷锟铰的凤拷式锟斤拷锟斤拷chatico锟斤拷锟斤拷锟斤拷锟絬rl
						newChat.left = false;

						newChat.checked_in = false;
						if (grouptype == 1)
							newChat.innerChat = true;
						else
							newChat.innerChat = false;

						// 锟斤拷示锟斤拷锟铰达拷锟斤拷锟斤拷锟介，模锟斤拷锟斤拷息TLRPC.TL_updateChatParticipants
						TLRPC.TL_updateChatParticipants updateParts = new TLRPC.TL_updateChatParticipants();
						TLRPC.TL_chatParticipants parts = new TLRPC.TL_chatParticipants();
						updateParts.participants = parts;
						parts.admin_id = inviteid;
						parts.chat_id = groupid;

						// TLRPC.TL_updates updates = new TLRPC.TL_updates();

						// 锟斤拷示锟斤拷锟叫碉拷锟矫伙拷锟斤拷锟斤拷锟剿变化
						int whoInviteME = 0;
						JSONArray users = (JSONArray) jGroup
								.getJSONArray("groupuser");
						for (int j = 0; j < users.length(); j++) {
							JSONObject jUser = users.getJSONObject(j);
							TLRPC.TL_chatParticipant participant = new TLRPC.TL_chatParticipant();
							participant.date = getCurrentTime();
							participant.inviter_id = inviteid;
							participant.user_id = jUser.optInt("userid", 0);
							participant.nick_name = "";
							participant.status = jUser.optInt("ugstate", 0);// 0锟斤拷锟斤拷锟斤拷,1锟斤拷删锟斤拷
							// 某锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟揭ｏ拷锟揭匡拷锟皆诧拷fromid,锟斤拷fromid锟斤拷锟斤拷锟揭碉拷
							if (UserConfig.clientUserId == participant.user_id)
								whoInviteME = jUser.optInt("fromid", 0);

							TLRPC.User user = MessagesController.getInstance().users
									.get(participant.user_id);
							if (user == null) {
								user = buildUser(jUser);
								MessagesController.getInstance()
								.addStrangerUser(user);
							}
							if (user != null) {
								parts.participants.add(participant);
								addgroupusers.add(participant.user_id);
								// andriod锟斤拷锟斤拷锟斤拷锟斤拷锟矫伙拷锟斤拷为锟秸的ｏ拷锟斤拷IOS锟角对的ｏ拷TODO..
								updates.users.add(user);
							} else
								FileLog.d("create group failed",
										"no find group member");
						}
						// 锟斤拷锟斤拷锟斤拷锟剿斤拷锟秸碉拷锟斤拷锟斤拷锟斤拷锟斤拷锟较拷锟绞憋拷锟斤拷锟揭拷确锟斤拷锟揭伙拷锟絋L_updateChatParticipants锟斤拷息

						updates.updates.add(updateParts);
						updates.chats.add(newChat);
						// MessagesController.getInstance().processUpdates(updates,
						// false);
						newChat.participants_count = addgroupusers.size();
						// 锟斤拷锟酵达拷锟斤拷锟斤拷锟斤拷锟较�
						sendGroupMessage(3, whoInviteME, addgroupusers,
								newChat, inviteid, groupcreatetime, updates);

						// 锟斤拷锟斤拷息锟斤拷锟叫伙拷取锟角凤拷锟斤拷没锟叫达拷锟斤拷锟斤拷锟斤拷锟斤拷息,锟斤拷为锟街伙拷锟斤拷锟斤拷锟斤拷锟斤拷锟截伙拷锟斤拷,锟斤拷锟斤拷锟剿凤拷锟斤拷锟斤拷息锟斤拷锟杰碉拷锟铰ｏ拷锟揭憋拷锟斤拷锟窖撅拷没锟斤拷锟斤拷锟较拷锟�
						// 锟斤拷锟斤拷锟饺斤拷锟斤拷息锟芥储锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷取锟斤拷锟斤拷锟较拷锟斤拷锟斤拷陆锟斤拷锟斤拷锟斤拷锟斤拷椋拷俅锟斤拷锟斤拷锟斤拷锟斤拷息
						synchronized (this) {
							for (int k = 0, len = updatesQueue.size(); k < len; k++) {
								TLRPC.Updates delayupdates = updatesQueue
										.get(k);
								if (delayupdates.chat_id == groupid) {
									MessagesController
									.getInstance()
									.processUpdates(
											(TLRPC.Updates) delayupdates,
											false);
									updatesQueue.remove(k);// 锟斤拷态删锟斤拷一锟斤拷元锟斤拷
									--len;// 锟斤拷锟斤拷一锟斤拷
								}
							}
						}

						// 锟斤拷锟斤拷锟斤拷锟酵凤拷锟�
						if (!newChat.chatico.isEmpty())
							sendGroupMessage(4, updatephotoid, addgroupusers,
									newChat, inviteid, getCurrentTime(),
									updates);

					} else {
						updates.chats.add(oldChat);
						if (jGroup.isNull("groupuser")) {
							if (!groupname.isEmpty()
									&& groupname.compareTo("null") != 0
									&& !oldChat.title.equals(groupname)) {
								// 锟斤拷谋锟斤拷锟侥憋拷锟斤拷
								oldChat.title = groupname;
								sendGroupMessage(2, updatetitleid,
										addgroupusers, oldChat, inviteid,
										getCurrentTime(), updates);
							}
							if (!oldChat.chatico.equals(groupico)) {
								// 锟斤拷锟酵凤拷锟侥憋拷锟斤拷
								// 锟斤拷锟斤拷锟斤拷头锟斤拷
								oldChat.chatico = groupico;
								sendGroupMessage(4, updatephotoid,
										addgroupusers, oldChat, inviteid,
										getCurrentTime(), updates);
							}
						}
						// 锟斤拷示锟斤拷锟叫碉拷锟矫伙拷锟斤拷锟斤拷锟剿变化
						else if (jGroup.has("groupuser")) {
							JSONArray users = (JSONArray) jGroup
									.getJSONArray("groupuser");

							for (int j = 0; j < users.length(); j++) {
								JSONObject jUser = users.getJSONObject(j);

								int user_id = jUser.optInt("userid", 0);

								TLRPC.User user = MessagesController
										.getInstance().users.get(user_id);
								if (user == null) {
									user = buildUser(jUser);
									MessagesController.getInstance()
									.addStrangerUser(user);
								}

								int state = jUser.optInt("ugstate", 0);// 0锟斤拷锟斤拷锟斤拷,1锟斤拷删锟斤拷
								int fromid = jUser.optInt("fromid", 0);// 0锟斤拷锟斤拷锟斤拷,1锟斤拷删锟斤拷
								if (state == 1) {
									if (user_id == UserConfig.clientUserId)
										oldChat.left = true;// 锟揭憋拷锟竭筹拷锟斤拷锟斤拷
									removegroupusers.add(user_id);
									oldChat.participants_count -= removegroupusers
											.size();
									sendGroupMessage(0, fromid,
											removegroupusers, oldChat,
											inviteid, getCurrentTime(), updates);
									removegroupusers.clear();
								} else {
									if (user_id == UserConfig.clientUserId)
										oldChat.left = false;// 锟揭憋拷锟斤拷拥锟斤拷锟�
									addgroupusers.add(user_id);
									oldChat.participants_count += addgroupusers
											.size();
									sendGroupMessage(1, fromid, addgroupusers,
											oldChat, inviteid,
											getCurrentTime(), updates);
									addgroupusers.clear();
								}
							}
						}
					}
				}
				// 统一一锟轿达拷锟斤拷锟斤拷锟斤拷锟较拷锟斤拷锟斤拷锟叫碊B锟斤拷
				MessagesController.getInstance().processUpdates(updates, false);
				// 锟介发锟斤拷锟剿变化
				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						NotificationCenter.getInstance().postNotificationName(
								MessagesController.dialogsNeedReload);
					}
				});

			}
			return 0;

		} catch (JSONException e) {
			// System.out.println("Jsons parse error !");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	private void GetGroup(final int groupID, final int fromid) {
		if (client == null)
			return;

		String strUrl = Config.webFun_getusergroup + "/groupid/" + groupID;

		client.get(strUrl, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				try {
					JSONObject jo = new JSONObject(response);

					if (jo.has("result") && jo.getInt("result") == -2) {
						// 实锟斤拷锟斤拷注锟斤拷锟剿ｏ拷锟斤拷示session锟斤拷锟斤拷锟斤拷,lhy todo,锟斤拷锟叫猴拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷result
						reportNetworkStatus(100);
						return;
					}

					if (jo.has("result") && jo.getInt("result") == 0) {
						parseGetSingleGroupResult(jo);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(Throwable error, String content) {
				error.printStackTrace();
			}
		});
	}

	private int parseGetSingleGroupResult(JSONObject jo) {
		try {
			if (jo.has("result") && jo.getInt("result") != 0)
				return -1;

			// TLRPC.TL_updates updates = new TLRPC.TL_updates();

			if (jo.has("group") && !jo.isNull("group")) {
				JSONArray group = (JSONArray) jo.getJSONArray("group");
				int glen = group.length();
				for (int i = 0; i < glen; i++) {
					int type = 0;// 删锟斤拷为0锟斤拷锟斤拷锟斤拷为1锟斤拷锟睫革拷为2(锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟酵凤拷锟斤拷薷模锟斤拷锟斤拷锟斤拷锟轿�3
					JSONObject jGroup = group.getJSONObject(i);
					int groupid = jGroup.optInt("groupid", 0);
					int inviteid = jGroup.optInt("createuserid", 0);
					// groupname锟斤拷锟斤拷锟斤拷null,也锟斤拷锟斤拷锟斤拷要通锟斤拷锟斤拷某锟皆憋拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
					String groupname = jGroup.getString("groupname");
					int companyid = jGroup.optInt("companyid", 0);
					int updatetitleid = jGroup.optInt("updatetitleid", 0);
					int updatephotoid = jGroup.optInt("updatephotoid", 0);
					String groupico = "";
					if (jGroup.has("groupico") && !jGroup.isNull("groupico"))
						groupico = jGroup.getString("groupico");

					int grouptype = jGroup.optInt("grouptype", 0);

					// xueqiang change for create group time
					int groupcreatetime = jGroup.optInt("createtime",
							getCurrentTime());

					ArrayList<Integer> addgroupusers = new ArrayList<Integer>();
					addgroupusers.clear();

					ArrayList<Integer> removegroupusers = new ArrayList<Integer>();
					removegroupusers.clear();

					TLRPC.TL_chat oldChat = (TL_chat) MessagesController
							.getInstance().chats.get(groupid);
					if (oldChat == null) {
						TLRPC.TL_chat newChat = new TLRPC.TL_chat();
						newChat.id = groupid;
						newChat.title = groupname;
						if (groupname.isEmpty()
								|| groupname.compareTo("null") == 0)
							newChat.hasTitle = -1;
						else
							newChat.hasTitle = 0;
						newChat.chatico = groupico;// 锟斤拷锟酵凤拷锟接︼拷锟斤拷锟斤拷锟�
						newChat.photo = new TLRPC.TL_chatPhotoEmpty();// 原锟斤拷锟斤拷PHOTO锟斤拷锟铰的凤拷式锟斤拷锟斤拷chatico锟斤拷锟斤拷锟斤拷锟絬rl
						newChat.left = false;

						newChat.checked_in = false;
						if (grouptype == 1)
							newChat.innerChat = true;
						else
							newChat.innerChat = false;

						// 锟斤拷示锟斤拷锟铰达拷锟斤拷锟斤拷锟介，模锟斤拷锟斤拷息TLRPC.TL_updateChatParticipants
						TLRPC.TL_updateChatParticipants updateParts = new TLRPC.TL_updateChatParticipants();
						TLRPC.TL_chatParticipants parts = new TLRPC.TL_chatParticipants();
						updateParts.participants = parts;
						parts.admin_id = inviteid;
						parts.chat_id = groupid;

						TLRPC.TL_updates updates = new TLRPC.TL_updates();

						// 锟斤拷示锟斤拷锟叫碉拷锟矫伙拷锟斤拷锟斤拷锟剿变化
						int whoInviteME = 0;
						JSONArray users = (JSONArray) jo
								.getJSONArray("groupuser");
						for (int j = 0; j < users.length(); j++) {
							JSONObject jUser = users.getJSONObject(j);
							TLRPC.TL_chatParticipant participant = new TLRPC.TL_chatParticipant();
							participant.date = getCurrentTime();
							participant.inviter_id = inviteid;
							participant.user_id = jUser.optInt("userid", 0);
							participant.nick_name = "";
							participant.status = jUser.optInt("ugstate", 0);// 0锟斤拷锟斤拷锟斤拷,1锟斤拷删锟斤拷
							// 某锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟揭ｏ拷锟揭匡拷锟皆诧拷fromid,锟斤拷fromid锟斤拷锟斤拷锟揭碉拷
							if (UserConfig.clientUserId == participant.user_id)
								whoInviteME = jUser.optInt("fromid", 0);

							TLRPC.User user = MessagesController.getInstance().users
									.get(participant.user_id);
							if (user == null) {
								user = buildUser(jUser);
								MessagesController.getInstance()
								.addStrangerUser(user);
							}
							if (user != null) {
								parts.participants.add(participant);
								addgroupusers.add(participant.user_id);
								// andriod锟斤拷锟斤拷锟斤拷锟斤拷锟矫伙拷锟斤拷为锟秸的ｏ拷锟斤拷IOS锟角对的ｏ拷TODO..
								updates.users.add(user);
							} else
								FileLog.d("create group failed",
										"no find group member");
						}
						// 锟斤拷锟斤拷锟斤拷锟剿斤拷锟秸碉拷锟斤拷锟斤拷锟斤拷锟斤拷锟较拷锟绞憋拷锟斤拷锟揭拷确锟斤拷锟揭伙拷锟絋L_updateChatParticipants锟斤拷息

						updates.updates.add(updateParts);
						updates.chats.add(newChat);
						// MessagesController.getInstance().processUpdates(updates,
						// false);
						newChat.participants_count = addgroupusers.size();
						// 锟斤拷锟酵达拷锟斤拷锟斤拷锟斤拷锟较�
						sendGroupMessage(3, whoInviteME, addgroupusers,
								newChat, inviteid, groupcreatetime, updates);

						// 锟斤拷锟斤拷锟斤拷锟酵凤拷锟�
						if (!newChat.chatico.isEmpty())
							sendGroupMessage(4, updatephotoid, addgroupusers,
									newChat, inviteid, getCurrentTime(),
									updates);
						// 统一一锟轿达拷锟斤拷锟斤拷锟斤拷锟较拷锟斤拷锟斤拷锟叫碊B锟斤拷
						MessagesController.getInstance().processUpdates(
								updates, false);

						// 锟斤拷锟斤拷息锟斤拷锟叫伙拷取锟角凤拷锟斤拷没锟叫达拷锟斤拷锟斤拷锟斤拷锟斤拷息,锟斤拷为锟街伙拷锟斤拷锟斤拷锟斤拷锟斤拷锟截伙拷锟斤拷,锟斤拷锟斤拷锟剿凤拷锟斤拷锟斤拷息锟斤拷锟杰碉拷锟铰ｏ拷锟揭憋拷锟斤拷锟窖撅拷没锟斤拷锟斤拷锟较拷锟�
						// 锟斤拷锟斤拷锟饺斤拷锟斤拷息锟芥储锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷取锟斤拷锟斤拷锟较拷锟斤拷锟斤拷陆锟斤拷锟斤拷锟斤拷锟斤拷椋拷俅锟斤拷锟斤拷锟斤拷锟斤拷息
						synchronized (this) {
							for (int k = 0, len = updatesQueue.size(); k < len; k++) {
								TLRPC.Updates delayupdates = updatesQueue
										.get(k);
								if (delayupdates.chat_id == groupid) {
									MessagesController
									.getInstance()
									.processUpdates(
											(TLRPC.Updates) delayupdates,
											false);
									updatesQueue.remove(k);// 锟斤拷态删锟斤拷一锟斤拷元锟斤拷
									--len;// 锟斤拷锟斤拷一锟斤拷
								}
							}
						}

					}
				}

				// 锟介发锟斤拷锟剿变化
				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						NotificationCenter.getInstance().postNotificationName(
								MessagesController.dialogsNeedReload);
					}
				});

			}
			return 0;

		} catch (JSONException e) {
			// System.out.println("Jsons parse error !");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	private void GetMeeting(final int mid, final int fromid) {
		if (client == null)
			return;

		String strUrl = Config.webFun_getmeeting + "/meetingid/" + mid;

		client.get(strUrl, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				final String res = response;
				Utilities.stageQueue.postRunnable(new Runnable() {
					@Override
					public void run() {
						try {
							JSONObject jo = new JSONObject(res);
							if (jo.has("result") && jo.getInt("result") == -2) {
								// 实锟斤拷锟斤拷注锟斤拷锟剿ｏ拷锟斤拷示session锟斤拷锟斤拷锟斤拷,lhy todo,锟斤拷锟叫猴拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷result
								reportNetworkStatus(100);
								return;
							}

							if (jo.has("result")) {
								if (jo.getInt("result") == 0) {
									parseMeetingBook(jo);
									MessagesController.getInstance()
									.processMeetingInvite(mid, fromid);
									return;
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
				error.printStackTrace();
			}
		});
	}

	public void getUser(final int userid) {
		if (client == null)
			return;

		String strUrl = Config.webFun_getuser + "/userid/" + userid;

		client.get(strUrl, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				final String res = response;
				Utilities.stageQueue.postRunnable(new Runnable() {
					@Override
					public void run() {
						try {
							JSONObject jo = new JSONObject(res);
							if (jo.has("result") && jo.getInt("result") == -2) {
								// 实锟斤拷锟斤拷注锟斤拷锟剿ｏ拷锟斤拷示session锟斤拷锟斤拷锟斤拷,lhy todo,锟斤拷锟叫猴拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷result
								reportNetworkStatus(100);
								return;
							}
							if (jo.has("result") && jo.getInt("result") == -1) {
								// 锟斤拷锟斤拷锟斤拷锟�
								return;
							}

							if (jo.has("result")) {
								if (jo.getInt("result") == 0) {
									TLRPC.User user = parseUserResult(jo);
									// 锟斤拷时注锟斤拷锟斤拷,锟斤拷锟斤拷陌锟斤拷锟斤拷
									MessagesController.getInstance().users.put(
											user.id, user);
									// 锟斤拷锟斤拷息锟斤拷锟叫伙拷取锟角凤拷锟斤拷没锟叫达拷锟斤拷锟斤拷锟斤拷锟斤拷息,锟斤拷为锟街伙拷锟斤拷锟斤拷锟斤拷锟斤拷锟截伙拷锟斤拷,锟斤拷锟斤拷锟剿凤拷锟斤拷锟斤拷息锟斤拷锟杰碉拷锟铰ｏ拷锟揭憋拷锟斤拷锟窖撅拷没锟斤拷锟斤拷锟较拷锟�
									// 锟斤拷锟斤拷锟饺斤拷锟斤拷息锟芥储锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷取锟斤拷锟斤拷锟较拷锟斤拷锟斤拷陆锟斤拷锟斤拷锟斤拷锟斤拷椋拷俅锟斤拷锟斤拷锟斤拷锟斤拷息
									synchronized (this) {
										for (int k = 0, len = updatesQueue
												.size(); k < len; k++) {
											TLRPC.Updates delayupdates = updatesQueue
													.get(k);
											if (delayupdates.from_id == user.id) {
												delayupdates.users.add(user);
												MessagesController
												.getInstance()
												.processUpdates(
														(TLRPC.Updates) delayupdates,
														false);
												updatesQueue.remove(k);// 锟斤拷态删锟斤拷一锟斤拷元锟斤拷
												--len;// 锟斤拷锟斤拷一锟斤拷
											}
										}
									}

									TLRPC.TL_MeetingCall mc = meetingcallMap
											.get(user.id);
									if (mc != null) {
										int type = mc.type;
										int version = mc.version;
										int from_id = mc.from_id;
										int callTime = mc.callTime;
										String mid = mc.mid;
										int signal = mc.signal;
										int func = mc.func;
										int gid = mc.gid;

										if (signal == 5) {
											// 锟斤拷取锟斤拷锟斤拷锟斤拷员锟叫憋拷
											getMeetingStatus(mid, gid);
										} else {
											if (getCurrentTime() - callTime <= 60
													&& signal == 0)
												MessagesController
												.getInstance()
												.processMeetingCall(
														mid, from_id,
														gid);
											else
												MessagesController
												.getInstance()
												.processMeetingCallResponse(
														mid, from_id,
														gid, signal);
										}
									}

									return;
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
				error.printStackTrace();
			}
		});
	}

	public void onMessageArrival(String topic, final byte[] payload) {
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				// byte[] payloadCopy = new byte[payload.length];
				// System.arraycopy(src, srcPos, dest, destPos, length);
				// System.arraycopy(payload, 0, payloadCopy, 0, payload.length);
				MessagePack msgPack = new MessagePack();
				ByteArrayInputStream in = new ByteArrayInputStream(payload);
				Unpacker unpacker = msgPack.createUnpacker(in);

				try {
					// 锟斤拷锟斤拷锟叫伙拷锟斤拷锟斤拷
					unpacker.readArrayBegin();
					int type = unpacker.readInt();// 锟斤拷锟斤拷锟斤拷锟筋还锟斤拷锟斤拷锟斤拷锟斤拷息
					int version = unpacker.readInt();// 锟芥本锟斤拷锟捷匡拷锟斤拷
					// String identifier = unpacker.readString();
					// int from_id = getIdByIdentifier(identifier);
					int from_id = unpacker.readInt();// 锟斤拷锟斤拷锟斤拷id
					// 锟斤拷锟斤拷锟斤拷锟斤拷锟�,锟斤拷锟节匡拷锟斤拷锟斤拷锟斤拷锟斤拷ctrtype

					// 锟斤拷锟斤拷锟阶刺拷锟斤拷锟斤拷谋锟酵伙拷锟斤拷盏锟斤拷锟斤拷锟斤拷息
					if (type == MessagePackFormat.CtrlType) {
						int ctrltype = unpacker.readInt();
						// 锟斤拷锟斤拷锟斤拷息
						if (ctrltype == MessagePackFormat.GROUP_UPDATE)// 锟斤拷示锟斤拷锟斤拷锟�
						{
							getUpdate();
							updateLocations();
						} else if (ctrltype == MessagePackFormat.MEETING_UPDATE) {
							int mid = 0;
							if(unpacker.getNextType()==ValueType.RAW){
								String strmid = unpacker.readString();
								mid = Integer.valueOf(strmid);
							}else{								
								mid = unpacker.readInt();
							}
							int action = unpacker.readInt();
							// 锟斤拷锟斤拷锟斤拷0=删锟斤拷锟斤拷1=锟睫改ｏ拷2=锟斤拷锟斤拷锟斤拷3=锟斤拷锟斤拷锟剿ｏ拷4=锟斤拷员锟剿筹拷锟斤拷5=锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
							if (action == 5) {
								// notifyui,锟斤拷锟斤拷一锟斤拷dialog,锟斤拷锟斤拷锟矫伙拷锟轿加伙拷锟斤拷
								// if(
								// MessagesController.getInstance().meetings.get(mid)!=null)
								// {
								int callTime = unpacker.readInt();
								if (getCurrentTime() - callTime <= 60)
									MessagesController.getInstance()
									.processMeetingInvite(mid, from_id);
								// }
								// else
								// GetMeeting(mid,from_id);
							} else {
								getUpdate();
								updateLocations();
							}
						} else if (ctrltype == MessagePackFormat.MEETING_CALLING)// 锟斤拷示锟斤拷锟斤拷母锟斤拷锟斤拷锟斤拷锟�
						{
							/*
							 * [ uint8 type = 1 //control锟斤拷锟斤拷1.1锟斤拷 uint16 version,
							 * //协锟斤拷姹撅拷锟斤拷锟�1锟斤拷始 int fromid, //锟斤拷锟斤拷锟斤拷
							 * 
							 * uint8 ctrtype = 3 //calling int time, //锟斤拷锟斤拷时锟斤拷
							 * string mid,
							 * //锟斤拷锟斤拷牛锟斤拷址锟斤拷锟斤拷锟斤拷煞锟斤拷锟斤拷锟斤拷锟斤拷刹锟斤拷锟街と拷锟轿ㄒ�,gGID,u小锟斤拷锟斤拷前-锟斤拷锟斤拷诤锟� int
							 * signal, //0=锟斤拷锟叫ｏ拷1=取锟斤拷锟斤拷锟叫ｏ拷2=锟杰撅拷锟斤拷3=忙锟斤拷4=锟剿筹拷,5锟斤拷示锟斤拷锟斤拷状态 int
							 * func, //锟斤拷锟杰诧拷锟斤拷锟斤拷锟斤拷位锟斤拷1=锟斤拷锟斤拷锟斤拷2=锟斤拷频锟斤拷锟斤拷锟斤拷锟斤拷频锟斤拷锟斤拷锟斤拷为锟斤拷1|2 = 3锟斤拷 int
							 * gid //群锟斤拷id锟斤拷锟斤拷目锟斤拷为锟斤拷锟斤拷时锟斤拷锟斤拷锟斤拷锟�0 ]
							 */

							int callTime = unpacker.readInt();
							String mid = unpacker.readString();
							int signal = unpacker.readInt();
							int func = unpacker.readInt();
							int gid = unpacker.readInt();
							unpacker.readArrayEnd();

							boolean missingData = MessagesController
									.getInstance().users.get(from_id) == null;
							if (missingData) {
								TLRPC.TL_MeetingCall mc = new TLRPC.TL_MeetingCall();
								mc.from_id = from_id;
								mc.callTime = callTime;
								mc.mid = mid;
								mc.signal = signal;
								mc.type = type;
								mc.func = func;
								mc.gid = gid;
								mc.version = version;
								meetingcallMap.put(from_id, mc);
								getUser(from_id);
							} else {
								if (signal == 5) {
									// 锟斤拷取锟斤拷锟斤拷锟斤拷员锟叫憋拷
									getMeetingStatus(mid, gid);
								} else {
									if (getCurrentTime() - callTime <= 60
											&& signal == 0)
										MessagesController.getInstance()
										.processMeetingCall(mid,
												from_id, gid);
									else
										MessagesController.getInstance()
										.processMeetingCallResponse(
												mid, from_id, gid,
												signal);
								}
							}
						}
						return;
					}

					int chattype = unpacker.readInt();
					// change by xueqiang
					// String groupid = unpacker.readString();
					int gid = unpacker.readInt();
					// int gid = Integer.parseInt(groupid);

					byte[] contents = unpacker.readByteArray();
					int date = unpacker.readInt();

					if (version > 2) {
						// MessagesController.getInstance().sendUnsupportMsg(from_id,
						// gid);
						String sMsg = LocaleController.getString(
								"UnsuppotedMedia", R.string.UnsuppotedMedia);
						ProcessTextMsg(from_id, gid, sMsg, date);
						unpacker.readArrayEnd();
						return;
					}

					switch (chattype) {
					case MessagePackFormat.PLAINCHATTYPE:// 锟斤拷通锟斤拷锟斤拷
					{
						// 锟侥憋拷锟斤拷息
						unpacker.readArrayEnd();
						String sMsg = new String(contents, "UTF-8");
						ProcessTextMsg(from_id, gid, sMsg, date);
						break;
					}
					case MessagePackFormat.VOICECHATTYPE: {
						// 锟斤拷锟斤拷
						unpacker.readArrayBegin();
						int duration = unpacker.readInt();
						byte[] audio_data = unpacker.readByteArray();
						unpacker.readArrayEnd();
						unpacker.readArrayEnd();
						ProcessAudio(from_id, gid, audio_data, duration, date);
						break;
					}
					case MessagePackFormat.LOCATION: {
						// 锟斤拷图
						unpacker.readArrayBegin();
						double lat_location = unpacker.readDouble();
						double long_location = unpacker.readDouble();
						unpacker.readArrayEnd();
						unpacker.readArrayEnd();
						ProcessMapInfo(from_id, gid, date, lat_location,
								long_location);
						break;
					}
					case MessagePackFormat.IMAGETYPE: {
						// 图片
						int imageArryLen = unpacker.readArrayBegin();
						int w = unpacker.readInt();// 锟斤拷锟斤拷图锟斤拷锟�
						int h = unpacker.readInt();// 锟斤拷锟斤拷图锟竭讹拷
						byte[] image_data = unpacker.readByteArray();// 锟斤拷锟斤拷图
						// rawdata
						int w1 = unpacker.readInt();// 图片1锟斤拷锟�
						int h1 = unpacker.readInt();// 图片1锟竭讹拷
						byte[] url = unpacker.readByteArray();// 图片1锟斤拷址
						String sUrl = new String(url, "UTF-8");
						int size = unpacker.readInt();// 图片1锟竭讹拷
						unpacker.readArrayEnd();
						unpacker.readArrayEnd();
						ProcessImage(from_id, gid, date, w, h, image_data, w1,
								h1, sUrl, size);
						break;
					}
					case MessagePackFormat.FILE: {
						// 图片
						unpacker.readArrayBegin();
						byte[] filename = unpacker.readByteArray();// 锟侥硷拷锟斤拷址
						String sFilename = new String(filename, "UTF-8");
						byte[] fileaddress = unpacker.readByteArray();// 锟侥硷拷锟斤拷址
						String sFileaddress = new String(fileaddress, "UTF-8");
						int fileSize = unpacker.readInt();// 锟侥碉拷锟竭达拷
						unpacker.readArrayEnd();
						unpacker.readArrayEnd();
						ProcessFile(from_id, gid, date, sFileaddress,
								sFilename, fileSize);
						break;
					}
					case MessagePackFormat.ALERT: {
						// alert
						unpacker.readArrayBegin();
						byte[] msg = unpacker.readByteArray();
						String sMsg = new String(msg, "UTF-8");

						byte[] guid = unpacker.readByteArray();
						String sGuid = new String(guid, "UTF-8");

						int alertTime = unpacker.readInt();
						int status = unpacker.readInt();
						// int _status= unpacker.readInt();
						unpacker.readArrayEnd();
						unpacker.readArrayEnd();
						ProcessAlert(from_id, gid, date, sMsg, sGuid,
								alertTime, status);
						break;
					}
					default:
						break;
					}
					// 锟斤拷锟斤拷息锟斤拷锟斤拷
					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() {
							// NotificationCenter.getInstance().postNotificationName(MessagesController.dialogsNeedReload);
						}
					});

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void onConnect(final int rc) {
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				reportNetworkStatus(rc);
				if(rc==0)
				{
					//锟斤拷锟斤拷锟斤拷锟接成癸拷锟斤拷锟斤拷getupdate锟斤拷锟教诧拷锟角对碉拷 锟斤拷锟斤拷要
					ConnectionsManager.getInstance().getUpdate();
					ConnectionsManager.getInstance().gettime();
				}
			}
		});
	}

	public void reportNetworkStatus(int rc) {
		// sam
		if (rc == 0) {
			connectionState = 0;
			// FileLog.d("emm", "1 reportNetworkStatus:" + connectionState +
			// " rc:" + rc);
		} else {
			connectionState = 10 + rc; // 使锟斤拷10+rc锟斤拷示锟斤拷锟斤拷失锟杰的达拷锟诫，锟斤拷锟斤拷锟斤拷原锟斤拷状态锟斤拷突
			// FileLog.d("emm", "2 reportNetworkStatus:" + connectionState +
			// " rc:" + rc);
		}
		final int stateCopy = connectionState;
		// FileLog.d("emm", "3 reportNetworkStatus:" + connectionState + " rc:"
		// + rc + " stateCopy:" + stateCopy);
		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				if (stateCopy == 0)
					FileLog.e("emm", "connected success");
				else
					FileLog.e("emm", "connection failed");

				NotificationCenter.getInstance().postNotificationName(703,
						stateCopy);
				if (stateCopy < 10) {
					FileLog.e("emm", "connection success");
				} else if (stateCopy == 110 || stateCopy == 15) // web session
					// expired || mq
					// session
					// expired
				{
					FileLog.e("emm", "session expired:" + stateCopy);
					disconnectServ();
					ApplicationLoader.mqStarted = false;

					AccountManager accountManager = AccountManager
							.get(ApplicationLoader.applicationContext);
					if (accountManager != null) {
						Account[] accounts = accountManager
								.getAccountsByType("info.emm.weiyicloud.account");
						Account myAccount = null;
						String account = "";
						String domain = "";
						if (UserConfig.isPublic) {
							if (UserConfig.isPersonalVersion)
								account = UserConfig.account;
							else {
								account = UserConfig.pubcomaccount;
								domain = UserConfig.domain;
							}
						} else
							account = UserConfig.priaccount;

						for (Account acc : accounts) {
							if (acc.name.equalsIgnoreCase(account)) {
								myAccount = acc;
								break;
							}
						}
						if (myAccount != null) {
							String pwd = accountManager.getPassword(myAccount);
							CheckLogin(domain, account, pwd,
									new RPCRequest.RPCRequestDelegate() {
								@Override
								public void run(
										final TLObject response,
										final TL_error error) {
									if (error != null) {
										Utilities
										.RunOnUIThread(new Runnable() {
											@Override
											public void run() {
												int result = error.code;
												if (result == 1) {// 锟绞猴拷未锟斤拷锟斤拷
												} else if (result == 2) {// 锟绞猴拷锟窖撅拷锟斤拷锟斤拷
												} else if (result == 3) {// 锟斤拷锟斤拷锟矫伙拷锟斤拷息锟借备失锟斤拷
												} else if (result == 4) {// 锟斤拷锟斤拷锟斤拷锟�
												} else if (result == 5) {// 锟绞号达拷锟斤拷
												} else if (result == -1) {
												}
												NotificationCenter
												.getInstance()
												.postNotificationName(
														1234);
												MessagesController
												.getInstance()
												.unregistedPush();
												// xueqiang
												// 锟斤拷锟斤拷锟剿憋拷锟截伙拷锟斤拷锟斤拷募锟�
												UserConfig
												.logout();
											}
										});
										return;
									}

									Utilities
									.RunOnUIThread(new Runnable() {
										@Override
										public void run() {
											final TLRPC.TL_auth_authorization res = (TLRPC.TL_auth_authorization) response;
											UserConfig.currentUser = res.user;
											UserConfig.clientActivated = true;
											UserConfig.clientUserId = res.user.id;
											UserConfig
											.saveConfig(true);

											FileLog.d(
													"emm",
													"check login result:"
															+ UserConfig.clientUserId
															+ " sid:"
															+ UserConfig.currentUser.sessionid);
											StartNetWorkService();
										}
									});
								}
							});
						} else {
							// xueqiang 锟斤拷锟斤拷锟剿憋拷锟截伙拷锟斤拷锟斤拷募锟�
							UserConfig.logout();
							MessagesController.getInstance().unregistedPush();
							NotificationCenter.getInstance()
							.postNotificationName(1234);
						}
					} else {
						UserConfig.logout();
						MessagesController.getInstance().unregistedPush();
						NotificationCenter.getInstance().postNotificationName(
								1234);
					}
				} else {
					// 锟斤拷锟斤拷锟斤拷停止
					// IMRtmpClientMgr.getInstance().localLeaveMeeting();
					// todo..
					NotificationCenter.getInstance().postNotificationName(1234);
					MessagesController.getInstance().unregistedPush();
					// xueqiang 锟斤拷锟斤拷锟剿憋拷锟截伙拷锟斤拷锟斤拷募锟�
					UserConfig.logout();
				}
			}
		});
	}

	public void onDisConnect(int rc) {
		/*Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				if (isNetworkOnline()) {
					connectionState = 2;
				} else {
					connectionState = 1;
				}
				final int stateCopy = connectionState;
				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						FileLog.d("emm ", "already disconnected");
						NotificationCenter.getInstance().postNotificationName(
								703, stateCopy);
					}
				});
			}
		});*/
	}

	public void onLog(int level, String content) {
		FileLog.d("emm", content);
	}

	public void onPublishACK(int msgID) {
		final int networkMsgID = msgID;
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				// FileLog.e("emm", "msg map size: " + msgMap.size() +
				// " on pub ack: " + networkMsgID);
				if (msgMap.containsKey(networkMsgID)) {
					RPCRequest request = msgMap.get(networkMsgID);
					msgMap.remove(networkMsgID);
					if (request.rawRequest instanceof TLRPC.TL_messages_sendMessage) {
						TLRPC.TL_messages_sentMessage res = new TLRPC.TL_messages_sentMessage();
						res.id = UserConfig.getSeq();// 锟斤拷证id唯一
						res.date = getCurrentTime();
						res.pts = res.id;
						res.seq = res.id;// (int)++nextCallToken;
						request.completionBlock.run(res, null);
					} else if (request.rawRequest instanceof TLRPC.TL_messages_sendMedia) {
						TLRPC.TL_messages_sendMedia sendMsgRequest = (TLRPC.TL_messages_sendMedia) request.rawRequest;

						if (sendMsgRequest.media instanceof TLRPC.TL_inputMediaUploadedAudio) {
							// FileLog.d("emm", "msg map size: " + msgMap.size()
							// + " on pub ack 1: " + networkMsgID);
							TLRPC.messages_StatedMessage res = new TLRPC.messages_StatedMessage();
							TLRPC.TL_message msg = new TLRPC.TL_message();
							res.message = msg;
							TLRPC.TL_messageMediaAudio media = new TLRPC.TL_messageMediaAudio();
							res.message.media = media;
							TLRPC.TL_audio audio = new TLRPC.TL_audio();
							res.message.media.audio = audio;
							res.message.out = true;
							res.message.send_state = MessagesController.MESSAGE_SEND_STATE_SENT;
							res.message.unread = true;
							audio.id = UserConfig.getSeq();// 唯一锟酵匡拷锟斤拷
							audio.dc_id = 5;
							audio.duration = sendMsgRequest.media.duration;
							audio.size = 0;// 锟斤拷为锟斤拷锟斤拷锟斤拷锟斤拷应锟矫诧拷锟斤拷要锟接凤拷锟酵端达拷锟捷癸拷锟斤拷
							audio.user_id = UserConfig.clientUserId;
							audio.date = getCurrentTime();
							res.message.id = UserConfig.getSeq();// 锟斤拷证id唯一
							res.message.date = audio.date;
							res.message.from_id = UserConfig.clientUserId;
							if (sendMsgRequest.peer.chat_id == 0) // sam
							{
								TLRPC.TL_peerUser to_id = new TLRPC.TL_peerUser();
								res.message.to_id = to_id;
								res.message.to_id.user_id = sendMsgRequest.peer.user_id;
							} else {
								TLRPC.TL_peerChat to_id = new TLRPC.TL_peerChat();
								res.message.to_id = to_id;
								res.message.to_id.chat_id = sendMsgRequest.peer.chat_id;// 锟斤拷id
							}
							res.pts = res.message.id;// MessagesStorage.lastSeqValue+1;
							res.seq = res.message.id;// MessagesStorage.lastSeqValue
							// + 1;
							request.completionBlock.run(res, null);
						} else if (sendMsgRequest.media instanceof TLRPC.TL_inputMediaUploadedPhoto) {
							TLRPC.messages_StatedMessage res = new TLRPC.messages_StatedMessage();
							TLRPC.TL_message msg = new TLRPC.TL_message();
							res.message = msg;
							TLRPC.TL_messageMediaPhoto media = new TLRPC.TL_messageMediaPhoto();
							res.message.media = media;
							TLRPC.TL_photo photo = new TLRPC.TL_photo();
							res.message.media.photo = photo;
							res.message.out = true;
							res.message.send_state = MessagesController.MESSAGE_SEND_STATE_SENT;
							res.message.unread = true;
							res.message.message = "";
							photo.id = UserConfig.getSeq();// 唯一锟酵匡拷锟斤拷
							photo.user_id = UserConfig.clientUserId;
							photo.date = getCurrentTime();
							photo.caption = "";
							photo.geo = new TLRPC.TL_geoPointEmpty();
							// 锟斤拷原始锟斤拷锟斤拷锟斤拷锟斤拷锟叫伙拷取sizes,锟节凤拷锟斤拷锟斤拷锟斤拷锟叫撅拷锟斤拷要锟斤拷锟斤拷锟斤拷锟�
							// public ArrayList<PhotoSize> sizes = new
							// ArrayList<PhotoSize>();
							// photo.sizes = ;
							res.message.id = UserConfig.getSeq();// 锟斤拷证id唯一
							res.message.date = photo.date;
							res.message.from_id = UserConfig.clientUserId;
							if (sendMsgRequest.peer.chat_id == 0) // sam
							{
								TLRPC.TL_peerUser to_id = new TLRPC.TL_peerUser();
								res.message.to_id = to_id;
								res.message.to_id.user_id = sendMsgRequest.peer.user_id;
							} else {
								TLRPC.TL_peerChat to_id = new TLRPC.TL_peerChat();
								res.message.to_id = to_id;
								res.message.to_id.chat_id = sendMsgRequest.peer.chat_id;// 锟斤拷id
							}
							res.pts = res.message.id;// MessagesStorage.lastSeqValue+1;
							res.seq = res.message.id;// MessagesStorage.lastSeqValue
							// + 1;
							request.completionBlock.run(res, null);
						} else if (sendMsgRequest.media instanceof TLRPC.TL_inputMediaGeoPoint) {
							TLRPC.TL_inputMediaGeoPoint geoInputMedia = (TLRPC.TL_inputMediaGeoPoint) sendMsgRequest.media;
							TLRPC.messages_StatedMessage res = new TLRPC.messages_StatedMessage();
							TLRPC.TL_message msg = new TLRPC.TL_message();
							res.message = msg;
							TLRPC.TL_messageMediaGeo geoMedia = new TLRPC.TL_messageMediaGeo();
							res.message.media = geoMedia;
							TLRPC.TL_geoPoint geoPoint = new TLRPC.TL_geoPoint();
							geoPoint.lat = geoInputMedia.geo_point.lat;
							geoPoint._long = geoInputMedia.geo_point._long;
							res.message.media.geo = geoPoint;
							res.message.out = true;
							res.message.send_state = MessagesController.MESSAGE_SEND_STATE_SENT;
							res.message.unread = true;
							res.message.message = "";
							res.message.date = getCurrentTime();
							res.message.id = UserConfig.getSeq();// 锟斤拷证id唯一
							res.message.from_id = UserConfig.clientUserId;
							if (sendMsgRequest.peer.chat_id == 0) // sam
							{
								TLRPC.TL_peerUser to_id = new TLRPC.TL_peerUser();
								res.message.to_id = to_id;
								res.message.to_id.user_id = sendMsgRequest.peer.user_id;
							} else {
								TLRPC.TL_peerChat to_id = new TLRPC.TL_peerChat();
								res.message.to_id = to_id;
								res.message.to_id.chat_id = sendMsgRequest.peer.chat_id;// 锟斤拷id
							}
							res.pts = res.message.id;// MessagesStorage.lastSeqValue+1;
							res.seq = res.message.id;// MessagesStorage.lastSeqValue
							// + 1;
							request.completionBlock.run(res, null);
						} else if (sendMsgRequest.media instanceof TLRPC.TL_inputMediaUploadedDocument) {
							TLRPC.messages_StatedMessage res = new TLRPC.messages_StatedMessage();
							TLRPC.TL_message msg = new TLRPC.TL_message();
							res.message = msg;
							TLRPC.TL_messageMediaDocument media = new TLRPC.TL_messageMediaDocument();
							res.message.media = media;
							res.message.media.photo = null;
							TLRPC.TL_document document = new TLRPC.TL_document();
							document.dc_id = UserConfig.clientUserId;
							document.id = UserConfig.getSeq();// 锟斤拷证id唯一
							document.date = getCurrentTime();
							String filename = sendMsgRequest.media.file_name;
							document.file_name = sendMsgRequest.media.file_name;

							String ext = "";
							int idx = filename.lastIndexOf(".");
							if (idx != -1) {
								ext = filename.substring(idx);
							}

							if (ext.length() != 0) {
								MimeTypeMap myMime = MimeTypeMap.getSingleton();
								String mimeType = myMime
										.getMimeTypeFromExtension(ext
												.toLowerCase());
								if (mimeType != null) {
									document.mime_type = mimeType;
								} else {
									document.mime_type = "application/octet-stream";
								}
							} else {
								document.mime_type = "application/octet-stream";
							}

							document.thumb = new TLRPC.TL_photoSizeEmpty();
							document.thumb.type = "s";

							res.message.media.document = document;
							res.message.out = true;
							res.message.send_state = MessagesController.MESSAGE_SEND_STATE_SENT;
							res.message.unread = true;
							res.message.message = "";
							res.message.id = UserConfig.getSeq();// 锟斤拷证id唯一
							res.message.date = document.date;
							res.message.from_id = UserConfig.clientUserId;
							if (sendMsgRequest.peer.chat_id == 0) // sam
							{
								TLRPC.TL_peerUser to_id = new TLRPC.TL_peerUser();
								res.message.to_id = to_id;
								res.message.to_id.user_id = sendMsgRequest.peer.user_id;
							} else {
								TLRPC.TL_peerChat to_id = new TLRPC.TL_peerChat();
								res.message.to_id = to_id;
								res.message.to_id.chat_id = sendMsgRequest.peer.chat_id;// 锟斤拷id
							}
							res.pts = res.message.id;// MessagesStorage.lastSeqValue+1;
							res.seq = res.message.id;// MessagesStorage.lastSeqValue
							// + 1;
							request.completionBlock.run(res, null);
						}
						if (sendMsgRequest.media instanceof TLRPC.TL_inputMediaAlert) {
							// 锟斤拷要锟斤拷锟斤拷锟角凤拷锟斤拷确
							TLRPC.messages_StatedMessage res = new TLRPC.messages_StatedMessage();
							TLRPC.TL_message msg = new TLRPC.TL_message();
							res.message = msg;
							TLRPC.TL_messageMediaAlert media = new TLRPC.TL_messageMediaAlert();
							res.message.media = media;
							// TLRPC.TL_alertMedia alertMsg = new
							// TLRPC.TL_alertMedia();
							// res.message.media.alert = alertMsg;
							res.message.media.alert = sendMsgRequest.media.alert; // hz
							res.message.message = res.message.media.alert.msg;
							res.message.out = true;
							res.message.send_state = MessagesController.MESSAGE_SEND_STATE_SENT;
							res.message.unread = true;
							res.message.id = UserConfig.getSeq();// 锟斤拷证id唯一
							res.message.date = getCurrentTime();
							res.message.from_id = UserConfig.clientUserId;
							if (sendMsgRequest.peer.chat_id == 0) // sam
							{
								TLRPC.TL_peerUser to_id = new TLRPC.TL_peerUser();
								res.message.to_id = to_id;
								res.message.to_id.user_id = sendMsgRequest.peer.user_id;
							} else {
								TLRPC.TL_peerChat to_id = new TLRPC.TL_peerChat();
								res.message.to_id = to_id;
								res.message.to_id.chat_id = sendMsgRequest.peer.chat_id;// 锟斤拷id
							}
							res.pts = res.message.id;
							res.seq = res.message.id;
							request.completionBlock.run(res, null);
						}
					}
				}
			}
		});
	}

	public ConnectionsManager() {
		if (ApplicationLoader.myCookieStore != null) {
			FileLog.e("emm", "set http cookie************");
			client.setCookieStore(ApplicationLoader.myCookieStore);
		} else
			FileLog.e("emm",
					"no set http cookie,because null ApplicationLoader.myCookieStore");

		loadSession();
		if (!isNetworkOnline()) {
			connectionState = 1;
		}
	}

	public void resumeNetworkMaybe() {
		/*
		 * Utilities.stageQueue.postRunnable(new Runnable() {
		 * 
		 * @Override public void run() { if (paused) {
		 * ApplicationLoader.lastPauseTime = System.currentTimeMillis();
		 * nextWakeUpTimeout = 60000; nextSleepTimeout = 30000; FileLog.e("emm",
		 * "wakeup network in background by recieved push"); } else if
		 * (ApplicationLoader.lastPauseTime != 0) {
		 * ApplicationLoader.lastPauseTime = System.currentTimeMillis();
		 * FileLog.e("emm", "reset sleep timeout by recieved push"); } } });
		 */
	}

	public void applicationMovedToForeground() {
		/*
		 * Utilities.stageQueue.postRunnable(new Runnable() {
		 * 
		 * @Override public void run() { if (paused) { nextSleepTimeout = 60000;
		 * nextWakeUpTimeout = 60000; FileLog.e("emm",
		 * "reset timers by application moved to foreground"); } } });
		 */
	}

	// void setTimeDifference(int diff) {
	// // boolean store = Math.abs(diff - timeDifference) > 25;
	// timeDifference = diff;
	// // if (store)
	// {
	// saveSession();
	// }
	// }

	private void loadSession() {
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				SharedPreferences preferences = ApplicationLoader.applicationContext
						.getSharedPreferences("globalconfig",
								Context.MODE_PRIVATE);
				// currentDatacenterId =
				// preferences.getInt("currentDatacenterId", 0);
				timeDifference = preferences.getInt("timeDifference", 0);
				// lastDcUpdateTime = preferences.getInt("lastDcUpdateTime", 0);
			}
		});
	}

	private void saveSession() {
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				try {
					SharedPreferences preferences = ApplicationLoader.applicationContext
							.getSharedPreferences("globalconfig",
									Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = preferences.edit();
					// editor.putInt("currentDatacenterId",
					// currentDatacenterId);
					editor.putInt("timeDifference", timeDifference);
					// editor.putInt("lastDcUpdateTime", lastDcUpdateTime);
					editor.commit();
				} catch (Exception e) {
					FileLog.e("tmessages", e);
				}
			}
		});
	}

	long getNewSessionId() {
		long newSessionId = MessagesController.random.nextLong();
		return isDebugSession ? (0xabcd000000000000L | (newSessionId & 0x0000ffffffffffffL))
				: newSessionId;
	}

	public long generateMessageId() {
		long messageId = (long) ((((double) System.currentTimeMillis() + ((double) timeDifference) * 1000) * 4294967296.0) / 1000.0);
		if (messageId <= lastOutgoingMessageId) {
			messageId = lastOutgoingMessageId + 1;
		}
		while (messageId % 4 != 0) {
			messageId++;
		}
		lastOutgoingMessageId = messageId;
		return messageId;
	}

	public int generateContactId() {
		int messageId = (int) ((((double) System.currentTimeMillis() + ((double) timeDifference) * 1000)) / 1000.0);
		if (messageId <= lastContactId) {
			messageId = lastContactId + 1;
		}
		lastContactId = messageId;
		return -messageId;
	}

	public long getTimeFromMsgId(long messageId) {
		return (long) (messageId / 4294967296.0 * 1000);
	}

	public void updateDcSettings(int rc) {

	}

	// 专锟斤拷锟斤拷锟斤拷PHP锟斤拷锟戒开始,web锟斤拷锟矫凤拷式

	private boolean GetChatUserObject(RequestParams params,
			final TLObject rawRequest, int act) {
		boolean bResult = true;
		try {
			int chat_id = 0;
			int user_id = 0;
			String title;
			JSONObject jo = new JSONObject();
			if (act == 0) {
				TLRPC.TL_messages_deleteChatUser req = (TLRPC.TL_messages_deleteChatUser) rawRequest;
				chat_id = req.chat_id;
				user_id = req.user_id.user_id;
				jo.put("act", act);
				jo.put("groupid", chat_id);
				jo.put("fromid", UserConfig.clientUserId);
				JSONArray jsonArr = new JSONArray();
				jsonArr.put(user_id);
				jo.put("userarr", jsonArr);

			} else if (act == 1) {
				TLRPC.TL_messages_addChatUser req = (TLRPC.TL_messages_addChatUser) rawRequest;
				chat_id = req.chat_id;
				user_id = req.user_id.user_id;
				jo.put("act", act);
				jo.put("groupid", chat_id);
				jo.put("fromid", UserConfig.clientUserId);
				JSONArray jsonArr = new JSONArray();
				jsonArr.put(user_id);
				jo.put("userarr", jsonArr);
			} else if (act == 2) {
				if (rawRequest instanceof TLRPC.TL_messages_editChatTitle) {
					TLRPC.TL_messages_editChatTitle req = (TLRPC.TL_messages_editChatTitle) rawRequest;
					title = req.title;
					chat_id = req.chat_id;
					jo.put("groupname", title);
				}
				if (rawRequest instanceof TLRPC.TL_messages_editChatPhoto) {
					TLRPC.TL_messages_editChatPhoto req = (TLRPC.TL_messages_editChatPhoto) rawRequest;
					chat_id = req.chat_id;
					if (req.photo instanceof TLRPC.TL_inputChatPhotoEmpty)
						jo.put("groupico", "");
					else
						jo.put("groupico", req.photo.file.http_path_img);

				}

				jo.put("act", act);
				jo.put("groupid", chat_id);
				jo.put("fromid", UserConfig.clientUserId);
			}
			jo.put("version", versionNUM);
			// for usercompany
			jo.put("versiontype", 1);
			params.put("param", jo.toString());
		} catch (Exception e) {
			bResult = false;
			e.printStackTrace();
		}

		return bResult;
	}

	private int parseGroupResult(JSONObject jo, final TLObject rawRequest,
			TLRPC.TL_chat newChat, int act) {
		int result = -1;
		try {
			int chat_id = 0;
			int user_id = 0;
			// if(jo.has("result") && jo.getInt("result")!=0)
			// return -1;

			if (act == 0) {
				TLRPC.TL_messages_deleteChatUser req = (TLRPC.TL_messages_deleteChatUser) rawRequest;
				chat_id = req.chat_id;
				user_id = req.user_id.user_id;
				newChat = (TLRPC.TL_chat) MessagesController.getInstance().chats
						.get(chat_id);
				if (newChat != null)
					newChat.participants_count--;
				// newChat.photo = new TLRPC.TL_chatPhotoEmpty();
			} else if (act == 1) {
				TLRPC.TL_messages_addChatUser req = (TLRPC.TL_messages_addChatUser) rawRequest;
				chat_id = req.chat_id;
				user_id = req.user_id.user_id;
				newChat = (TLRPC.TL_chat) MessagesController.getInstance().chats
						.get(chat_id);
				newChat.participants_count++;
				// newChat.photo = new TLRPC.TL_chatPhotoEmpty();
			} else if (act == 2) {
				if (rawRequest instanceof TLRPC.TL_messages_editChatTitle) {
					TLRPC.TL_messages_editChatTitle req = (TLRPC.TL_messages_editChatTitle) rawRequest;
					chat_id = req.chat_id;
					newChat.title = req.title;
					// newChat.photo = new TLRPC.TL_chatPhotoEmpty();
				} else {
					TLRPC.TL_messages_editChatPhoto req = (TLRPC.TL_messages_editChatPhoto) rawRequest;
					chat_id = req.chat_id;
					// newChat.photo = new TLRPC.TL_chatPhotoEmpty();
				}
			}
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	private int parseCreateGroupResult(JSONObject jo,
			final TLRPC.TL_messages_createChat req, TLRPC.TL_chat newChat) {
		int result = -1;
		try {
			if (jo.has("groupid")) {
				int groupid = jo.getInt("groupid");
				newChat.id = groupid;
				// xueqiang todo
				// newChat.companyid = req.companyID;
				newChat.title = req.title;
				newChat.chatico = "";// 锟斤拷前为缺省锟斤拷锟斤拷锟斤拷锟斤拷锟酵凤拷锟斤拷锟�,todo..
				newChat.version = jo.getInt("version");
				newChat.photo = new TLRPC.TL_chatPhotoEmpty();// 原锟斤拷锟斤拷PHOTO锟斤拷锟铰的凤拷式锟斤拷锟斤拷chatico锟斤拷锟斤拷锟斤拷锟絬rl
				newChat.left = false;
				newChat.participants_count = req.users.size();
				newChat.checked_in = false;
				newChat.hasTitle = -1;// 锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷没锟叫憋拷锟斤拷,-1锟斤拷示没锟叫ｏ拷0锟斤拷示锟叫憋拷锟斤拷

				ArrayList<Integer> groupusers = new ArrayList<Integer>();
				for (int i = 0; i < req.users.size(); i++) {
					TLRPC.InputUser intputUser = req.users.get(i);
					if (intputUser != null)
						groupusers.add(intputUser.user_id);
				}
				createGroupName(newChat, groupusers);
				return groupid;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public void processCreateChat(final TLRPC.TL_messages_createChat req,
			final RPCRequest.RPCRequestDelegate completionBlock,
			final TLRPC.TL_chat chat) {
		if (client != null) {
			try {
				RequestParams params = new RequestParams();
				JSONObject jo = new JSONObject();
				jo.put("act", 3);
				jo.put("groupname", req.title);
				jo.put("host", UserConfig.clientUserId);
				// xueqiang todo
				// jo.put("companyid", req.companyID);
				jo.put("fromid", UserConfig.clientUserId);
				jo.put("groupico", req.groupico);// 锟斤拷锟斤拷锟斤拷锟绞憋拷锟酵匡拷锟杰革拷锟斤拷锟斤拷锟酵凤拷锟斤拷锟斤拷锟斤拷锟酵凤拷锟斤拷URL锟斤拷址
				jo.put("version", versionNUM);// 锟斤拷锟斤拷锟斤拷锟绞憋拷锟酵匡拷锟杰革拷锟斤拷锟斤拷锟酵凤拷锟斤拷锟斤拷锟斤拷锟酵凤拷锟斤拷URL锟斤拷址

				JSONArray jsonArr = new JSONArray();
				for (int i = 0; i < req.users.size(); i++) {
					jsonArr.put(req.users.get(i).user_id);
				}
				jo.put("userarr", jsonArr);
				// for usercompany
				jo.put("versiontype", 1);
				params.put("param", jo.toString());

				client.post(Config.webFun_controlusergroup, params,
						new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						final String res = response;
						Utilities.stageQueue
						.postRunnable(new Runnable() {
							@Override
							public void run() {
								try {
									JSONTokener jsonParser = new JSONTokener(
											res);
									JSONObject ret = (JSONObject) jsonParser
											.nextValue();

									if (ret.has("result")
											&& ret.getInt("result") == -2) {
										// 实锟斤拷锟斤拷注锟斤拷锟剿ｏ拷锟斤拷示session锟斤拷锟斤拷锟斤拷,lhy
										// todo,锟斤拷锟叫猴拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷result
										reportNetworkStatus(100);
										return;
									}

									parseUpdateInfo(ret, true);
									// result>0为锟斤拷id,锟斤拷锟斤拷锟斤拷锟绞э拷锟斤拷锟�
									int result = parseCreateGroupResult(
											ret, req, chat);

									if (result < 0) {
										// 锟斤拷锟届创锟斤拷锟斤拷失锟杰碉拷锟斤拷息
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = 1;
										completionBlock.run(
												null,
												networkerror);
									} else {
										// 锟斤拷锟斤拷一锟斤拷锟斤拷应锟斤拷息锟斤拷锟斤拷锟斤拷锟斤拷
										TLRPC.messages_StatedMessage res = new TLRPC.messages_StatedMessage();
										// 锟斤拷锟斤拷chats
										final ArrayList<TLRPC.Chat> chats = new ArrayList<TLRPC.Chat>();
										chats.clear();
										chats.add(chat);
										// 锟斤拷锟斤拷锟斤拷息
										TLRPC.TL_messageService message = new TLRPC.TL_messageService();
										TLRPC.TL_peerChat peer = new TLRPC.TL_peerChat();
										peer.chat_id = chat.id;
										message.id = UserConfig
												.getSeq();// 锟斤拷证id唯一
										message.from_id = UserConfig.clientUserId;
										message.to_id = peer;
										message.out = true;
										message.unread = true;
										message.date = getCurrentTime();

										TLRPC.TL_messageActionChatCreate action = new TLRPC.TL_messageActionChatCreate();
										action.title = chat.title;

										// 锟斤拷锟铰憋拷锟斤拷锟斤拷锟捷结构锟斤拷锟节存储锟斤拷锟皆�
										// 锟斤拷锟斤拷users
										for (int i = 0; i < req.users
												.size(); i++) {
											int userid = req.users
													.get(i).user_id;
											if (userid == 0)
												continue;
											action.users
											.add(userid);
											TLRPC.User contactUser = MessagesController
													.getInstance().users
													.get(userid);
											res.users
											.add(contactUser);
										}
										message.action = action;
										res.chats = chats;
										res.message = message;

										completionBlock.run(
												res, null);
									}

								} catch (Exception e) {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
									completionBlock.run(null,
											networkerror);
								}
							}
						});
					}

					@Override
					public void onFailure(Throwable error,
							String content) {
						Utilities.stageQueue
						.postRunnable(new Runnable() {
							@Override
							public void run() {
								TLRPC.TL_error networkerror = new TLRPC.TL_error();
								networkerror.code = -1;
								completionBlock.run(null,
										networkerror);
							}
						});
					}
				});
			} catch (Exception e) {
				TLRPC.TL_error networkerror = new TLRPC.TL_error();
				networkerror.code = -1;
				completionBlock.run(null, networkerror);
			}
		}
	}

	public void processChatUser(final TLObject rawRequest,
			final RPCRequest.RPCRequestDelegate completionBlock,
			final TLRPC.TL_chat chat, final int act,
			final TLRPC.MessageAction action) {
		if (client != null) {
			try {
				RequestParams params = new RequestParams();

				if (!GetChatUserObject(params, rawRequest, act)) {
					// 锟斤拷锟届创锟斤拷锟斤拷失锟杰碉拷锟斤拷息
					TLRPC.TL_error networkerror = new TLRPC.TL_error();
					networkerror.code = 1;
					completionBlock.run(null, networkerror);
					return;
				}

				client.post(Config.webFun_controlusergroup, params,
						new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						final String res = response;
						Utilities.stageQueue
						.postRunnable(new Runnable() {
							@Override
							public void run() {
								try {
									JSONTokener jsonParser = new JSONTokener(
											res);
									JSONObject ret = (JSONObject) jsonParser
											.nextValue();
									if (ret.has("result")
											&& ret.getInt("result") == -2) {
										// 实锟斤拷锟斤拷注锟斤拷锟剿ｏ拷锟斤拷示session锟斤拷锟斤拷锟斤拷,lhy
										// todo,锟斤拷锟叫猴拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷result
										reportNetworkStatus(100);
										return;
									}
									if (ret.has("result")
											&& ret.getInt("result") == -1) {
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = 1;
										completionBlock.run(
												null,
												networkerror);
									} else {
										parseUpdateInfo(ret,
												true);

										int result = parseGroupResult(
												ret,
												rawRequest,
												chat, act);
										if (result > 0) {
											// if(act!=0)
											{
												// 锟斤拷锟斤拷一锟斤拷锟斤拷应锟斤拷息锟斤拷锟斤拷锟斤拷锟斤拷
												TLRPC.messages_StatedMessage res = new TLRPC.messages_StatedMessage();
												// 锟斤拷锟斤拷chats
												final ArrayList<TLRPC.Chat> chats = new ArrayList<TLRPC.Chat>();
												chats.clear();
												chats.add(chat);
												// 锟斤拷锟斤拷锟斤拷息
												TLRPC.TL_messageService message = new TLRPC.TL_messageService();
												TLRPC.TL_peerChat peer = new TLRPC.TL_peerChat();
												peer.chat_id = chat.id;
												message.id = UserConfig
														.getSeq();// 锟斤拷证id唯一
												message.from_id = UserConfig.clientUserId;
												message.to_id = peer;
												message.out = true;
												message.unread = true;
												message.date = getCurrentTime();

												action.title = chat.title;
												message.action = action;
												res.chats = chats;
												res.message = message;
												completionBlock
												.run(res,
														null);
											}
											/*
											 * else {
											 * TLRPC.TL_error
											 * networkerror =
											 * new
											 * TLRPC.TL_error();
											 * networkerror.code
											 * = 1;
											 * completionBlock
											 * .run(null,
											 * networkerror); }
											 */
										} else {
											TLRPC.TL_error networkerror = new TLRPC.TL_error();
											networkerror.code = 1;
											completionBlock
											.run(null,
													networkerror);
										}
									}

								} catch (Exception e) {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
									completionBlock.run(null,
											networkerror);
								}
							}
						});
					}

					@Override
					public void onFailure(Throwable error,
							String content) {
						Utilities.stageQueue
						.postRunnable(new Runnable() {
							@Override
							public void run() {
								TLRPC.TL_error networkerror = new TLRPC.TL_error();
								networkerror.code = -1;
								completionBlock.run(null,
										networkerror);
							}
						});
					}
				});
			} catch (Exception e) {
				TLRPC.TL_error networkerror = new TLRPC.TL_error();
				networkerror.code = -1;
				completionBlock.run(null, networkerror);
			}
		}
	}

	public void processUserPhoto(final TLObject rawRequest,
			final RPCRequest.RPCRequestDelegate completionBlock) {
		if (client != null) {
			try {
				TLRPC.User user = MessagesController.getInstance().users
						.get(UserConfig.clientUserId);
				String firstname = user.first_name;
				String lastname = user.last_name;
				if (rawRequest instanceof TLRPC.TL_account_updateProfile) {
					TLRPC.TL_account_updateProfile req = (TLRPC.TL_account_updateProfile) rawRequest;
					firstname = req.first_name;
					lastname = req.last_name;
				}

				RequestParams params = new RequestParams();
				params.put("userid", UserConfig.clientUserId + "");
				params.put("userico", "");

				params.put("lastname", lastname);
				params.put("firstname", firstname);
				params.put("version", versionNUM + "");

				// for usercompany
				params.put("versiontype", 1 + "");

				client.post(Config.webFun_updateuserinfo, params,
						new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						final String res = response;
						Utilities.stageQueue
						.postRunnable(new Runnable() {
							@Override
							public void run() {
								try {
									JSONTokener jsonParser = new JSONTokener(
											res);
									JSONObject ret = (JSONObject) jsonParser
											.nextValue();
									if (ret.has("result")
											&& ret.getInt("result") == -2) {
										// 实锟斤拷锟斤拷注锟斤拷锟剿ｏ拷锟斤拷示session锟斤拷锟斤拷锟斤拷,lhy
										// todo,锟斤拷锟叫猴拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷result
										reportNetworkStatus(100);
										return;
									}
									parseUpdateInfo(ret, true);

									int result = 0;
									if (ret != null
											&& ret.has("result")) {
										result = ret
												.getInt("result");
									}

									if (result >= 0) {
										if (rawRequest instanceof TLRPC.TL_account_updateProfile) {
											TLRPC.TL_account_updateProfile temp = (TLRPC.TL_account_updateProfile) rawRequest;
											TLRPC.TL_account_updateProfile res = new TLRPC.TL_account_updateProfile();
											res.first_name = temp.first_name;
											res.last_name = temp.last_name;
											completionBlock
											.run(res,
													null);
										} else
											completionBlock
											.run(null,
													null);
									} else {
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = 1;
										completionBlock.run(
												null,
												networkerror);
									}

								} catch (Exception e) {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
									completionBlock.run(null,
											networkerror);
								}
							}
						});
					}

					@Override
					public void onFailure(Throwable error,
							String content) {
						Utilities.stageQueue
						.postRunnable(new Runnable() {
							@Override
							public void run() {
								TLRPC.TL_error networkerror = new TLRPC.TL_error();
								networkerror.code = -1;
								completionBlock.run(null,
										networkerror);
							}
						});
					}
				});
			} catch (Exception e) {
				TLRPC.TL_error networkerror = new TLRPC.TL_error();
				networkerror.code = -1;
				completionBlock.run(null, networkerror);
			}
		}
	}

	public void processRemarkName(final TLObject rawRequest,
			final RPCRequest.RPCRequestDelegate completionBlock) {
		if (client != null) {
			try {
				TLRPC.TL_SetRamarkName req = (TLRPC.TL_SetRamarkName) rawRequest;
				RequestParams params = new RequestParams();
				params.put("userid", req.userid + "");
				params.put("renameid", req.renameid + "");
				params.put("name", req.name);
				client.post(Config.webFun_SETREMARKNAME, params,
						new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						final String res = response;
						Utilities.stageQueue
						.postRunnable(new Runnable() {
							@Override
							public void run() {
								try {
									JSONTokener jsonParser = new JSONTokener(
											res);
									JSONObject ret = (JSONObject) jsonParser
											.nextValue();
									if (ret.has("result")
											&& ret.getInt("result") == -2) {
										// 实锟斤拷锟斤拷注锟斤拷锟剿ｏ拷锟斤拷示session锟斤拷锟斤拷锟斤拷,lhy
										// todo,锟斤拷锟叫猴拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷result
										reportNetworkStatus(100);
										return;
									}

									int result = 0;
									if (ret != null
											&& ret.has("result")) {
										result = ret
												.getInt("result");
									}

									if (result == 0) {
										completionBlock.run(
												null, null);
									} else {
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = -1;
										completionBlock.run(
												null,
												networkerror);
									}

								} catch (Exception e) {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
									completionBlock.run(null,
											networkerror);
								}
							}
						});
					}

					@Override
					public void onFailure(Throwable error,
							String content) {
						Utilities.stageQueue
						.postRunnable(new Runnable() {
							@Override
							public void run() {
								TLRPC.TL_error networkerror = new TLRPC.TL_error();
								networkerror.code = -1;
								completionBlock.run(null,
										networkerror);
							}
						});
					}
				});
			} catch (Exception e) {
				TLRPC.TL_error networkerror = new TLRPC.TL_error();
				networkerror.code = -1;
				completionBlock.run(null, networkerror);
			}
		}
	}

	public void getMeetingStatus(final String mid, final int gid) {
		if (client != null) {
			try {
				RequestParams params = new RequestParams();
				params.put("meetingid", mid + "");
				client.post(Config.webFun_GETMEETINGSTATUS, params,
						new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						final String res = response;
						Utilities.stageQueue
						.postRunnable(new Runnable() {
							@Override
							public void run() {
								try {
									JSONTokener jsonParser = new JSONTokener(
											res);
									JSONObject ret = (JSONObject) jsonParser
											.nextValue();
									if (ret.has("result")
											&& ret.getInt("result") == -2) {
										// 实锟斤拷锟斤拷注锟斤拷锟剿ｏ拷锟斤拷示session锟斤拷锟斤拷锟斤拷,lhy
										// todo,锟斤拷锟叫猴拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷result
										reportNetworkStatus(100);
										return;
									}

									int result = 0;
									if (ret != null
											&& ret.has("result")) {
										result = ret
												.getInt("result");
									}

									if (result == 0) {
										// completionBlock.run(null,
										// null);
										JSONArray jsonArr = ret
												.getJSONArray("meeting");
										int arrLen = jsonArr
												.length();
										for (int j = 0; j < arrLen; ++j) {
											// 锟斤拷司锟斤拷一锟斤拷array
											// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷司锟斤拷锟捷硷拷录
											JSONObject obj = jsonArr
													.getJSONObject(j);
											int gid = obj
													.getInt("groupid");
											String mid = obj
													.getInt("meetingid")
													+ "";
											ArrayList<Integer> userList = new ArrayList<Integer>();
											JSONArray jsonUsers = obj
													.getJSONArray("users");
											for (int k = 0; k < jsonUsers
													.length(); ++k) {
												int userid = jsonUsers
														.getInt(k);
												userList.add(userid);
											}
											MessagesController
											.getInstance()
											.processMeetingStatus(
													gid,
													userList,
													mid);
										}
									} else {
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = -1;
										// completionBlock.run(null,
										// networkerror);
									}

								} catch (Exception e) {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
									// completionBlock.run(null,
									// networkerror);
								}
							}
						});
					}

					@Override
					public void onFailure(Throwable error,
							String content) {
						Utilities.stageQueue
						.postRunnable(new Runnable() {
							@Override
							public void run() {
								TLRPC.TL_error networkerror = new TLRPC.TL_error();
								networkerror.code = -1;
								// completionBlock.run(null,
								// networkerror);
							}
						});
					}
				});
			} catch (Exception e) {
				TLRPC.TL_error networkerror = new TLRPC.TL_error();
				networkerror.code = -1;
				// completionBlock.run(null, networkerror);
			}
		}
	}

	public int performRpc(final TLObject rawRequest,
			final RPCRequest.RPCRequestDelegate completionBlock) {
		if (!isNetworkOnline()) {
			TLRPC.TL_error networkerror = new TLRPC.TL_error();
			networkerror.code = -1;
			completionBlock.run(null, networkerror);
			return 0;
		}
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				if (rawRequest instanceof TLRPC.TL_messages_createChat) {
					TLRPC.TL_messages_createChat req = (TLRPC.TL_messages_createChat) rawRequest;
					TLRPC.TL_chat newChat = new TLRPC.TL_chat();
					processCreateChat(req, completionBlock, newChat);
				} else if (rawRequest instanceof TLRPC.TL_messages_addChatUser
						|| rawRequest instanceof TLRPC.TL_messages_deleteChatUser
						|| rawRequest instanceof TLRPC.TL_messages_editChatTitle
						|| rawRequest instanceof TLRPC.TL_messages_editChatPhoto) {
					int chat_id = 0;
					String title = "";
					int user_id = 0;
					int act = -1;
					TLRPC.MessageAction action = null;

					if (rawRequest instanceof TLRPC.TL_messages_deleteChatUser) {
						TLRPC.TL_messages_deleteChatUser req = (TLRPC.TL_messages_deleteChatUser) rawRequest;
						chat_id = req.chat_id;
						user_id = req.user_id.user_id;
						action = new TLRPC.TL_messageActionChatDeleteUser();
						action.user_id = user_id;
						act = 0;
					} else if (rawRequest instanceof TLRPC.TL_messages_addChatUser) {
						TLRPC.TL_messages_addChatUser req = (TLRPC.TL_messages_addChatUser) rawRequest;
						user_id = req.user_id.user_id;
						chat_id = req.chat_id;
						action = new TLRPC.TL_messageActionChatAddUser();
						action.user_id = user_id;
						act = 1;
					}

					else if (rawRequest instanceof TLRPC.TL_messages_editChatTitle) {
						TLRPC.TL_messages_editChatTitle req = (TLRPC.TL_messages_editChatTitle) rawRequest;
						chat_id = req.chat_id;
						title = req.title;
						action = new TLRPC.TL_messageActionChatEditTitle();
						act = 2;
					} else if (rawRequest instanceof TLRPC.TL_messages_editChatPhoto) {
						TLRPC.TL_messages_editChatPhoto req = (TLRPC.TL_messages_editChatPhoto) rawRequest;
						if (req.photo.getClass() == TLRPC.TL_inputChatPhotoEmpty.class) // Sam
							// if
							// this
							// is
							// a
							// empty,
							// shoud
							// be
							// a
							// delete
							// action.
						{
							action = new TLRPC.TL_messageActionChatDeletePhoto();
						} else {
							action = new TLRPC.TL_messageActionChatEditPhoto();
							action.photo = new TLRPC.TL_photo();
						}
						chat_id = req.chat_id;
						act = 2;
					}
					// 锟斤拷锟斤拷锟斤拷锟窖撅拷锟斤拷锟斤拷chat锟斤拷息锟较讹拷也锟斤拷锟节ｏ拷锟斤拷锟斤拷锟斤拷锟角达拷锟斤拷锟�
					TLRPC.TL_chat newChat = (TL_chat) MessagesController
							.getInstance().chats.get(chat_id);
					processChatUser(rawRequest, completionBlock, newChat, act,
							action);
				} else if (rawRequest instanceof TLRPC.TL_photos_updateProfilePhoto) {
					processUserPhoto(rawRequest, completionBlock);
				} else if (rawRequest instanceof TLRPC.TL_account_updateProfile) {
					processUserPhoto(rawRequest, completionBlock);
				} else if (rawRequest instanceof TLRPC.TL_SetRamarkName) {
					processRemarkName(rawRequest, completionBlock);
				}
			}
		});
		return 0;

	}

	// 专锟斤拷锟斤拷锟斤拷PHP锟斤拷锟斤拷锟斤拷锟�

	// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷通讯锟侥ｏ拷RPC锟斤拷式
	public long performRpc(final TLObject rpc,
			final RPCRequest.RPCRequestDelegate completionBlock,
			final RPCRequest.RPCProgressDelegate progressBlock,
			boolean requiresCompletion, int requestClass) {
		return performRpc(rpc, completionBlock, progressBlock,
				requiresCompletion, requestClass, DEFAULT_DATACENTER_ID);
	}

	public long performRpc(final TLObject rpc,
			final RPCRequest.RPCRequestDelegate completionBlock,
			final RPCRequest.RPCProgressDelegate progressBlock,
			boolean requiresCompletion, int requestClass, int datacenterId) {
		return performRpc(rpc, completionBlock, progressBlock, null,
				requiresCompletion, requestClass, datacenterId);
	}

	private String getIdentifierByID(int userid) {
		if (userid == 0)
			return "";
		TLRPC.User user = MessagesController.getInstance().users.get(userid);
		if (user != null) {
			return user.identification;
		}
		return "";
	}

	private int getIdByIdentifier(String identifier) {
		TLRPC.User user = MessagesController.getInstance().usersSDK
				.get(identifier);
		if (user != null) {
			return user.id;
		}
		return 0;
	}

	// 执锟斤拷远锟教癸拷锟教碉拷锟矫ｏ拷实锟斤拷锟较撅拷锟角凤拷锟斤拷锟斤拷
	public static volatile long nextCallToken = 0;

	public long performRpc(final TLObject rawRequest,
			final RPCRequest.RPCRequestDelegate completionBlock,
			final RPCRequest.RPCProgressDelegate progressBlock,
			final RPCRequest.RPCQuickAckDelegate quickAckBlock,
			final boolean requiresCompletion, final int requestClass,
			final int datacenterId) {

		// final long requestToken = nextCallToken++;
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				if (rawRequest == null)
					return;

				RPCRequest request = new RPCRequest();
				request.rawRequest = rawRequest;
				request.completionBlock = completionBlock;

				if (rawRequest instanceof TLRPC.TL_messages_sendMessage) {
					TLRPC.TL_messages_sendMessage sendMsgRequest = (TLRPC.TL_messages_sendMessage) rawRequest;
					String message = sendMsgRequest.message;
					MessagePack msgPack = new MessagePack();
					ByteArrayOutputStream byteOutPutSream = new ByteArrayOutputStream();
					org.msgpack.packer.Packer packer = msgPack
							.createPacker(byteOutPutSream);

					String fromid = getIdentifierByID(UserConfig.clientUserId);
					String toid = getIdentifierByID(sendMsgRequest.peer.user_id);
					String groupid = sendMsgRequest.peer.chat_id + "";

					try {
						if (message != null) // 锟侥憋拷锟斤拷息
						{
							packer.writeArrayBegin(7);
							packer.write(MessagePackFormat.CHATTYPE);
							packer.write(MessagePackFormat.VERSION);
							// 锟斤拷锟斤拷identifier锟斤拷锟叫凤拷锟斤拷锟竭ｏ拷锟斤拷锟斤拷锟斤拷也一锟斤拷
							packer.write(UserConfig.clientUserId);//nickname
							packer.write(MessagePackFormat.PLAINCHATTYPE);
							packer.write(sendMsgRequest.peer.chat_id);//userclientid
							packer.write(message.getBytes("UTF-8"));
							packer.write(getCurrentTime());
							packer.writeArrayEnd();
							byte[] bytes = byteOutPutSream.toByteArray();
							String topic;
							if (sendMsgRequest.peer.chat_id == 0)
								topic = "u/m/" + toid + "/" + fromid;
							else
								topic = "g/m/" + groupid + "/" + fromid;
							if (mqBinder != null) {
								int networkMsgID = mqBinder.publishMessage(
										bytes, topic);
								if (networkMsgID > 0)
									msgMap.put(networkMsgID, request);
								else
									FileLog.d("publish_msg_failed",
											"publish_msg_failed");
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if (rawRequest instanceof TLRPC.TL_messages_sendMedia) {
					try {
						TLRPC.TL_messages_sendMedia sendMsgRequest = (TLRPC.TL_messages_sendMedia) rawRequest;

						String fromid = getIdentifierByID(UserConfig.clientUserId);
						String toid = getIdentifierByID(sendMsgRequest.peer.user_id);
						String groupid = sendMsgRequest.peer.chat_id + "";

						if (sendMsgRequest.media instanceof TLRPC.TL_inputMediaUploadedAudio) {
							MessagePack msgPack = new MessagePack();
							ByteArrayOutputStream byteOutPutSream = new ByteArrayOutputStream();
							org.msgpack.packer.Packer packer = msgPack
									.createPacker(byteOutPutSream);
							packer.writeArrayBegin(8);
							packer.write(MessagePackFormat.CHATTYPE);
							packer.write(MessagePackFormat.VERSION);
							packer.write(UserConfig.clientUserId);
							packer.write(MessagePackFormat.VOICECHATTYPE);
							packer.write(sendMsgRequest.peer.chat_id);
							packer.write(MessagePackFormat.TO.getBytes("UTF-8"));
							packer.write(getCurrentTime());
							packer.writeArrayBegin(2);
							packer.write(sendMsgRequest.media.duration);
							packer.write(FileUtil
									.readFileBytesPath(sendMsgRequest.media.file_name));
							packer.writeArrayEnd();
							packer.writeArrayEnd();
							byte[] bytes = byteOutPutSream.toByteArray();

							String topic;
							if (sendMsgRequest.peer.chat_id == 0)
								topic = "u/m/" + toid + "/" + fromid;
							else
								topic = "g/m/" + groupid + "/" + fromid;
							if (mqBinder != null) {
								int networkMsgID = mqBinder.publishMessage(
										bytes, topic);
								if (networkMsgID > 0)
									msgMap.put(networkMsgID, request);
								else
									FileLog.d("publish_msg_failed",
											"publish_msg_failed");
							}
						} else if (sendMsgRequest.media instanceof TLRPC.TL_inputMediaUploadedPhoto) {
							TLRPC.TL_inputMediaUploadedPhoto photoMedia = (TLRPC.TL_inputMediaUploadedPhoto) sendMsgRequest.media;
							MessagePack msgPack = new MessagePack();
							ByteArrayOutputStream byteOutPutSream = new ByteArrayOutputStream();
							org.msgpack.packer.Packer packer = msgPack
									.createPacker(byteOutPutSream);
							packer.writeArrayBegin(8);
							packer.write(MessagePackFormat.CHATTYPE);
							packer.write(MessagePackFormat.VERSION);
							packer.write(UserConfig.clientUserId);
							packer.write(MessagePackFormat.IMAGETYPE);
							packer.write(sendMsgRequest.peer.chat_id);
							packer.write(MessagePackFormat.TO.getBytes("UTF-8"));
							packer.write(getCurrentTime());
							packer.writeArrayBegin(7);
							packer.write(photoMedia.w);// 锟斤拷锟斤拷图锟斤拷锟�
							packer.write(photoMedia.h);// 锟斤拷锟斤拷图锟竭讹拷
							// packer.write(FileUtil.readFileBytesPath(photoMedia.file_name));//锟斤拷锟斤拷图
							// rawdata
							packer.write(photoMedia.bytes);
							packer.write(photoMedia.w1);// 图片1锟斤拷锟�
							packer.write(photoMedia.h1);// 图片1锟竭讹拷
							packer.write(photoMedia.url.getBytes("UTF-8"));// 图片1锟斤拷址*/
							packer.write(photoMedia.size);
							packer.writeArrayEnd();
							packer.writeArrayEnd();

							byte[] bytes = byteOutPutSream.toByteArray();
							String topic;
							if (sendMsgRequest.peer.chat_id == 0)
								topic = "u/m/" + toid + "/" + fromid;
							else
								topic = "g/m/" + groupid + "/" + fromid;
							if (mqBinder != null) {
								int networkMsgID = mqBinder.publishMessage(
										bytes, topic);
								if (networkMsgID > 0)
									msgMap.put(networkMsgID, request);
								else
									FileLog.d("publish_msg_failed",
											"publish_msg_failed");
							}
						} else if (sendMsgRequest.media instanceof TLRPC.TL_inputMediaGeoPoint) {
							TLRPC.TL_inputMediaGeoPoint geoMedia = (TLRPC.TL_inputMediaGeoPoint) sendMsgRequest.media;
							MessagePack msgPack = new MessagePack();
							ByteArrayOutputStream byteOutPutSream = new ByteArrayOutputStream();
							org.msgpack.packer.Packer packer = msgPack
									.createPacker(byteOutPutSream);
							packer.writeArrayBegin(8);
							packer.write(MessagePackFormat.CHATTYPE);
							packer.write(MessagePackFormat.VERSION);
							packer.write(UserConfig.clientUserId);
							packer.write(MessagePackFormat.LOCATION);
							packer.write(sendMsgRequest.peer.chat_id);
							packer.write(MessagePackFormat.TO.getBytes("UTF-8"));
							packer.write(getCurrentTime());
							packer.writeArrayBegin(2);
							packer.write(geoMedia.geo_point.lat); // 纬锟斤拷
							packer.write(geoMedia.geo_point._long); // 锟斤拷锟斤拷
							packer.writeArrayEnd();
							packer.writeArrayEnd();
							byte[] bytes = byteOutPutSream.toByteArray();
							String topic;
							if (sendMsgRequest.peer.chat_id == 0)
								topic = "u/m/" + toid + "/" + fromid;
							else
								topic = "g/m/" + groupid + "/" + fromid;
							if (mqBinder != null) {
								int networkMsgID = mqBinder.publishMessage(
										bytes, topic);
								if (networkMsgID > 0)
									msgMap.put(networkMsgID, request);
								else
									FileLog.d("publish_msg_failed",
											"publish_msg_failed");
							}
						} else if (sendMsgRequest.media instanceof TLRPC.TL_inputMediaUploadedDocument) {
							TLRPC.TL_inputMediaUploadedDocument fileMedia = (TLRPC.TL_inputMediaUploadedDocument) sendMsgRequest.media;
							MessagePack msgPack = new MessagePack();
							ByteArrayOutputStream byteOutPutSream = new ByteArrayOutputStream();
							org.msgpack.packer.Packer packer = msgPack
									.createPacker(byteOutPutSream);
							packer.writeArrayBegin(8);
							packer.write(MessagePackFormat.CHATTYPE);
							packer.write(MessagePackFormat.VERSION);
							packer.write(UserConfig.clientUserId);
							packer.write(MessagePackFormat.FILE);
							packer.write(sendMsgRequest.peer.chat_id);
							packer.write(MessagePackFormat.TO.getBytes("UTF-8"));
							packer.write(getCurrentTime());
							packer.writeArrayBegin(3);
							packer.write(fileMedia.file_name.getBytes("UTF-8"));
							packer.write(fileMedia.url.getBytes("UTF-8"));
							packer.write(fileMedia.size);
							packer.writeArrayEnd();
							packer.writeArrayEnd();

							byte[] bytes = byteOutPutSream.toByteArray();
							String topic;
							if (sendMsgRequest.peer.chat_id == 0)
								topic = "u/m/" + toid + "/" + fromid;
							else
								topic = "g/m/" + groupid + "/" + fromid;
							if (mqBinder != null) {
								int networkMsgID = mqBinder.publishMessage(
										bytes, topic);
								if (networkMsgID > 0)
									msgMap.put(networkMsgID, request);
								else
									FileLog.d("publish_msg_failed",
											"publish_msg_failed");
							}
						} else if (sendMsgRequest.media instanceof TLRPC.TL_inputMediaAlert) {
							TLRPC.TL_inputMediaAlert alertMedia = (TLRPC.TL_inputMediaAlert) sendMsgRequest.media;
							MessagePack msgPack = new MessagePack();
							ByteArrayOutputStream byteOutPutSream = new ByteArrayOutputStream();
							org.msgpack.packer.Packer packer = msgPack
									.createPacker(byteOutPutSream);
							packer.writeArrayBegin(8);
							packer.write(MessagePackFormat.CHATTYPE);
							packer.write(MessagePackFormat.VERSION + 1);
							packer.write(UserConfig.clientUserId);
							packer.write(MessagePackFormat.ALERT);// 锟斤拷锟窖癸拷锟斤拷

							if (sendMsgRequest.peer == null) {
								sendMsgRequest.peer = new TLRPC.InputPeer();
							}

							packer.write(sendMsgRequest.peer.chat_id);
							packer.write(MessagePackFormat.TO.getBytes("UTF-8"));
							packer.write(getCurrentTime());
							packer.writeArrayBegin(4);
							packer.write(alertMedia.alert.msg.getBytes("UTF-8"));
							packer.write(alertMedia.alert.guid
									.getBytes("UTF-8"));
							packer.write(alertMedia.alert.date);
							packer.write(alertMedia.alert.status);
							packer.writeArrayEnd();
							packer.writeArrayEnd();
							byte[] bytes = byteOutPutSream.toByteArray();
							String topic;

							if (sendMsgRequest.peer.chat_id == 0)
								topic = "u/m/" + toid + "/" + fromid;
							else
								topic = "g/m/" + groupid + "/" + fromid;

							if (mqBinder != null) {
								int networkMsgID = mqBinder.publishMessage(
										bytes, topic);
								if (networkMsgID > 0)
									msgMap.put(networkMsgID, request);
								else
									FileLog.d("publish_msg_failed",
											"publish_msg_failed");
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if (rawRequest instanceof TLRPC.TL_auth_logOut) {
					// 锟斤拷锟斤拷锟剿伙拷锟斤拷锟斤拷锟剿筹拷
					disconnectServ();
					ApplicationLoader.mqStarted = false;
				} else {
					int a = 1;
					Log.d("dfdf", a + "");
				}
			}
		});
		return 0;
	}

	public void cancelRpc(final long token, final boolean notifyServer) {

	}

	public void meetingInvite(final int mid, final ArrayList<Integer> users) {
		/*
		 * uint8 type = control, //锟斤拷锟酵ｏ拷锟斤拷1.1锟斤拷 uint16 version, //协锟斤拷姹撅拷锟斤拷锟�1锟斤拷始 int
		 * fromid, //锟斤拷锟斤拷锟斤拷
		 * 
		 * uint8 ctrtype = meeting, int mid, //锟斤拷锟斤拷id int action,
		 * //锟斤拷锟斤拷锟斤拷0=删锟斤拷锟斤拷1=锟睫改ｏ拷2=锟斤拷锟斤拷锟斤拷3=锟斤拷锟斤拷锟剿ｏ拷4=锟斤拷员锟剿筹拷锟斤拷5=锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷 array dsts
		 * //锟剿讹拷锟斤拷锟侥憋拷锟斤拷锟斤拷锟斤拷id锟叫憋拷锟斤拷锟斤拷锟斤拷为3锟斤拷4锟斤拷5时锟斤拷要锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟� [ ],
		 */

		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				MessagePack msgPack = new MessagePack();
				ByteArrayOutputStream byteOutPutSream = new ByteArrayOutputStream();
				org.msgpack.packer.Packer packer = msgPack
						.createPacker(byteOutPutSream);

				try {
					packer.writeArrayBegin(7);
					packer.write(1);
					packer.write(MessagePackFormat.VERSION);
					packer.write(UserConfig.clientUserId);// 锟斤拷锟斤拷锟斤拷ID锟斤拷锟斤拷锟斤拷锟斤拷一锟斤拷锟斤拷锟斤拷锟斤拷没锟斤拷锟�
					packer.write(MessagePackFormat.MEETING_UPDATE);
					packer.write(mid);
					packer.write(5);// 锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟叫ｏ拷锟皆凤拷应锟矫碉拷锟斤拷一锟斤拷锟斤拷示锟斤拷锟角凤拷锟斤拷锟斤拷锟斤拷
					packer.write(getCurrentTime());// 锟斤拷锟叫匡拷始时锟戒，锟斤拷锟斤拷锟斤拷辗锟斤拷盏锟斤拷锟较拷锟斤拷丫锟斤拷锟斤拷税锟斤拷小时锟酵诧拷锟斤拷锟斤拷锟斤拷锟斤拷
					packer.writeArrayEnd();
					byte[] bytes = byteOutPutSream.toByteArray();

					int size = users.size();
					for (int i = 0; i < size; i++) {
						int userid = users.get(i);
						packer.write(userid);
						String topic = "u/m/" + userid + "/"
								+ UserConfig.clientUserId;
						if (mqBinder != null)
							mqBinder.publishMessage(bytes, topic);
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void meetingCall(final String mid, final int gid,
			final ArrayList<Integer> users, final int action) {

		/*
		 * [ uint8 type = 1 //control锟斤拷锟斤拷1.1锟斤拷 uint16 version, //协锟斤拷姹撅拷锟斤拷锟�1锟斤拷始 int
		 * fromid, //锟斤拷锟斤拷锟斤拷
		 * 
		 * uint8 ctrtype = 3 //calling int time, //锟斤拷锟斤拷时锟斤拷 string mid,
		 * //锟斤拷锟斤拷牛锟斤拷址锟斤拷锟斤拷锟斤拷煞锟斤拷锟斤拷锟斤拷锟斤拷刹锟斤拷锟街と拷锟轿ㄒ�,gGID,u小锟斤拷锟斤拷前-锟斤拷锟斤拷诤锟� int signal,
		 * //0=锟斤拷锟叫ｏ拷1=取锟斤拷锟斤拷锟叫ｏ拷2=锟杰撅拷锟斤拷3=忙锟斤拷4=锟剿筹拷 int func,
		 * //锟斤拷锟杰诧拷锟斤拷锟斤拷锟斤拷位锟斤拷1=锟斤拷锟斤拷锟斤拷2=锟斤拷频锟斤拷锟斤拷锟斤拷锟斤拷频锟斤拷锟斤拷锟斤拷为锟斤拷1|2 = 3锟斤拷 int gid //群锟斤拷id锟斤拷锟斤拷目锟斤拷为锟斤拷锟斤拷时锟斤拷锟斤拷锟斤拷锟�0 ]
		 */

		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				MessagePack msgPack = new MessagePack();
				ByteArrayOutputStream byteOutPutSream = new ByteArrayOutputStream();
				org.msgpack.packer.Packer packer = msgPack
						.createPacker(byteOutPutSream);

				try {
					packer.writeArrayBegin(9);
					packer.write(1);
					packer.write(MessagePackFormat.VERSION);
					packer.write(UserConfig.clientUserId);// 锟斤拷锟斤拷锟斤拷ID锟斤拷锟斤拷锟斤拷锟斤拷一锟斤拷锟斤拷锟斤拷锟斤拷没锟斤拷锟�
					packer.write(MessagePackFormat.MEETING_CALLING);
					packer.write(getCurrentTime());// 锟斤拷锟叫匡拷始时锟戒，锟斤拷锟斤拷锟斤拷辗锟斤拷盏锟斤拷锟较拷锟斤拷丫锟斤拷锟斤拷税锟斤拷小时锟酵诧拷锟斤拷锟斤拷锟斤拷锟斤拷
					packer.write(mid);
					packer.write(action);// 0=锟斤拷锟叫ｏ拷1=取锟斤拷锟斤拷锟叫ｏ拷2=锟杰撅拷锟斤拷3=忙锟斤拷4=锟剿筹拷
					packer.write(1);// 锟斤拷锟杰诧拷锟斤拷锟斤拷锟斤拷位锟斤拷1=锟斤拷锟斤拷锟斤拷2=锟斤拷频锟斤拷锟斤拷锟斤拷锟斤拷频锟斤拷锟斤拷锟斤拷为锟斤拷1|2 = 3锟斤拷
					packer.write(gid);
					packer.writeArrayEnd();
					byte[] bytes = byteOutPutSream.toByteArray();
					String topic = "";
					String fromid = getIdentifierByID(UserConfig.clientUserId);
					if (gid == 0) {
						int size = users.size();
						for (int i = 0; i < size; i++) {
							int userid = users.get(i);

							String toid = getIdentifierByID(userid);

							topic = "u/c/" + toid + "/" + fromid;
							if (mqBinder != null)
								mqBinder.publishMessage(bytes, topic);
						}
					} else {
						topic = "g/c/" + gid + "/" + fromid;
						if (mqBinder != null)
							mqBinder.publishMessage(bytes, topic);
					}

					/*
					 * int size = users.size(); for(int i=0;i<size;i++) { int
					 * userid = users.get(i); packer.write( userid ); String
					 * topic = "u/m/" + userid + "/" + UserConfig.clientUserId;
					 * mqBinder.publishMessage(bytes, topic); }
					 */

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static boolean isNetworkOnline() {
		boolean status = false;
		try {
			ConnectivityManager cm = (ConnectivityManager) ApplicationLoader.applicationContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (netInfo != null
					&& netInfo.getState() == NetworkInfo.State.CONNECTED) {
				status = true;
			} else {
				netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if (netInfo != null
						&& netInfo.getState() == NetworkInfo.State.CONNECTED) {
					status = true;
				}
			}
		} catch (Exception e) {
			FileLog.e("emm", e);
			return false;
		}
		return status;
	}

	public int getCurrentTime() {
		return (int) ((long) System.currentTimeMillis() / (long) 1000)
				+ timeDifference;
	}

	public int dateStrToInt(String s) {
		return (int) (DateUnit.strToDateLong(s).getTime() / (long) 1000)
				+ timeDifference;
	}

	public int dateStrToInt1(String s) {
		return (int) (DateUnit.strToDateLong1(s).getTime() / (long) 1000);
	}

	public void switchBackend() {
		/*
		 * Utilities.stageQueue.postRunnable(new Runnable() {
		 * 
		 * @Override public void run() { if (isTestBackend == 0) { isTestBackend
		 * = 1; } else { isTestBackend = 0; } datacenters.clear();
		 * fillDatacenters(); saveSession();
		 * Utilities.stageQueue.postRunnable(new Runnable() {
		 * 
		 * @Override public void run() { UserConfig.clearConfig();
		 * System.exit(0); } }); } });
		 */
	}

	public void ProcessTextMsg(int from_id, int gid, String msg, int date) {
		// 模锟斤拷锟斤拷息
		if (gid == 0) {
			TLRPC.TL_updateShortMessage updates = new TLRPC.TL_updateShortMessage();
			updates.id = UserConfig.getSeq();// 锟斤拷证id唯一;
			updates.from_id = from_id;
			if (gid == 0)
				updates.chat_id = from_id;
			else
				updates.chat_id = gid;
			updates.message = msg;
			updates.date = date;
			updates.pts = updates.id;
			updates.seq = updates.id;
			boolean missingData = MessagesController.getInstance().users
					.get(from_id) == null;
			if (missingData) {
				synchronized (this) {
					updatesQueue.add(updates);
				}
				getUser(from_id);

			} else
				MessagesController.getInstance().processUpdates(
						(TLRPC.Updates) updates, false);

		} else {
			// 锟斤拷锟秸碉拷锟斤拷息锟襟，憋拷锟斤拷锟街伙拷锟斤拷录锟窖撅拷删锟斤拷锟斤拷没锟斤拷锟斤拷锟斤拷锟斤拷耍锟接︼拷玫锟斤拷锟斤拷锟斤拷锟斤拷匣锟饺★拷锟斤拷锟较拷锟斤拷锟斤拷陆锟斤拷锟斤拷椋拷俜锟斤拷锟斤拷锟较拷锟絤essagecontroller
			TLRPC.TL_updateShortChatMessage updates = new TLRPC.TL_updateShortChatMessage();
			updates.id = UserConfig.getSeq();// 锟斤拷证id唯一;
			updates.from_id = from_id;
			updates.chat_id = gid;
			updates.message = msg;
			updates.date = date;
			updates.pts = updates.id;
			updates.seq = updates.id;
			TLRPC.TL_chat oldChat = (TL_chat) MessagesController.getInstance().chats
					.get(gid);
			if (oldChat != null)
				MessagesController.getInstance().processUpdates(
						(TLRPC.Updates) updates, false);
			else {
				synchronized (this) {
					updatesQueue.add(updates);
				}
				// 锟斤拷证锟斤拷锟斤拷锟较拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
				GetGroup(gid, from_id);
			}
		}
	}

	private void ProcessMapInfo(int from_id, int gid, int date,
			double lat_location, double long_location) {
		// 模锟斤拷锟斤拷息 todo..没锟斤拷锟斤拷锟截ｏ拷锟斤拷锟斤拷应锟矫伙拷锟斤拷google play,锟节癸拷锟斤拷没锟斤拷锟斤拷锟斤拷
		TLRPC.TL_updates updates = new TLRPC.TL_updates();
		updates.date = date;
		updates.from_id = from_id;
		if (gid == 0)
			updates.chat_id = from_id;
		else
			updates.chat_id = gid;
		updates.from_id = from_id;
		updates.id = UserConfig.getSeq();// 锟斤拷证id唯一;
		updates.seq = MessagesStorage.lastSeqValue + 1;
		TLRPC.TL_updateNewMessage update = new TLRPC.TL_updateNewMessage();
		TLRPC.TL_message message = new TLRPC.TL_message();
		update.message = message;
		update.date = date;
		message.from_id = from_id;
		message.date = date;
		message.unread = true;
		message.out = false;
		message.id = updates.id;// 锟斤拷证id唯一;

		if (gid == 0) // sam
		{
			TLRPC.TL_peerUser to_id = new TLRPC.TL_peerUser();
			message.to_id = to_id;
			message.to_id.user_id = UserConfig.clientUserId;
		} else {
			TLRPC.TL_peerChat to_id = new TLRPC.TL_peerChat();
			message.to_id = to_id;
			message.to_id.chat_id = gid;// 锟斤拷id
		}
		TLRPC.TL_messageMediaGeo geoInputMedia = new TLRPC.TL_messageMediaGeo();
		message.media = geoInputMedia;
		message.message = "";
		TLRPC.TL_geoPoint geoPoint = new TLRPC.TL_geoPoint();
		message.media.geo = geoPoint;
		geoPoint.lat = lat_location;
		geoPoint._long = long_location;
		update.pts = updates.id;
		updates.updates.add(update);
		MessagesController.getInstance().processUpdates(updates, false);
		if (gid != 0) {
			TLRPC.TL_chat oldChat = (TL_chat) MessagesController.getInstance().chats
					.get(gid);
			if (oldChat != null)
				MessagesController.getInstance().processUpdates(
						(TLRPC.Updates) updates, false);
			else {
				// 锟斤拷证锟斤拷锟斤拷锟较拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
				synchronized (this) {
					updatesQueue.add(updates);
				}
				GetGroup(gid, from_id);
			}
		} else {
			boolean missingData = MessagesController.getInstance().users
					.get(from_id) == null;
			if (missingData) {
				synchronized (this) {
					updatesQueue.add(updates);
				}
				getUser(from_id);
			} else
				MessagesController.getInstance().processUpdates(
						(TLRPC.Updates) updates, false);
		}
	}

	private void ProcessAudio(int from_id, int gid, byte[] audio_data,
			int duration, int date) {
		// 模锟斤拷锟斤拷息
		TLRPC.TL_updates updates = new TLRPC.TL_updates();
		updates.date = date;
		updates.from_id = from_id;
		if (gid == 0)
			updates.chat_id = from_id;
		else
			updates.chat_id = gid;
		updates.from_id = from_id;
		updates.id = UserConfig.getSeq();// 锟斤拷证id唯一;
		updates.seq = MessagesStorage.lastSeqValue + 1;
		TLRPC.TL_updateNewMessage update = new TLRPC.TL_updateNewMessage();
		TLRPC.TL_message message = new TLRPC.TL_message();
		update.message = message;
		update.date = date;
		message.from_id = from_id;
		message.date = date;
		message.unread = true;
		message.out = false;
		message.id = updates.id;// 锟斤拷证id唯一;
		if (gid == 0) // sam
		{
			TLRPC.TL_peerUser to_id = new TLRPC.TL_peerUser();
			message.to_id = to_id;
			message.to_id.user_id = UserConfig.clientUserId;
		} else {
			TLRPC.TL_peerChat to_id = new TLRPC.TL_peerChat();
			message.to_id = to_id;
			message.to_id.chat_id = gid;// 锟斤拷id
		}
		TLRPC.TL_messageMediaAudio media = new TLRPC.TL_messageMediaAudio();
		message.media = media;
		TLRPC.TL_audio audio = new TLRPC.TL_audio();
		message.media.audio = audio;
		message.message = "";

		// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟侥硷拷锟斤拷锟斤拷锟斤拷锟斤拷锟截革拷锟斤拷
		audio.id = UserConfig.getSeq();
		audio.dc_id = UserConfig.clientUserId;

		audio.duration = duration;
		audio.size = 0;// 锟斤拷为锟斤拷锟斤拷锟斤拷锟斤拷应锟矫诧拷锟斤拷要锟接凤拷锟酵端达拷锟捷癸拷锟斤拷
		audio.user_id = from_id;
		audio.date = date;
		audio.path = Utilities.getSystemDir().getAbsolutePath()
				+ MessageObject.getAttachFileName(audio);
		update.pts = updates.id;// 1;

		message.attachPath = audio.path;
		updates.updates.add(update);
		try {
			// MessageObject.getAttachFileName(audio) = audio.dc_id + "_" +
			// audio.id + ".m4a";
			// 锟斤拷锟街会不锟斤拷锟截革拷呀?
			int ret = FileUtil.BytesWriteToFile(Utilities.getSystemDir()
					.getAbsolutePath(), MessageObject.getAttachFileName(audio),
					audio_data);
			// if( ret ==0 )
			// FileLog.e("emm", "write file success");
			// else
			// FileLog.e("emm", "write file failed");

		} catch (IOException e) {
			FileLog.e("emm", "write file exception");
		}
		if (gid != 0) {
			TLRPC.TL_chat oldChat = (TL_chat) MessagesController.getInstance().chats
					.get(gid);
			if (oldChat != null)
				MessagesController.getInstance().processUpdates(
						(TLRPC.Updates) updates, false);
			else {
				// 锟斤拷证锟斤拷锟斤拷锟较拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
				synchronized (this) {
					updatesQueue.add(updates);
				}
				GetGroup(gid, from_id);
			}
		} else {
			boolean missingData = MessagesController.getInstance().users
					.get(from_id) == null;
			if (missingData) {
				synchronized (this) {
					updatesQueue.add(updates);
				}

				getUser(from_id);
			} else
				MessagesController.getInstance().processUpdates(
						(TLRPC.Updates) updates, false);
		}
	}

	public void ProcessImage(int from_id, int gid, int date, int w, int h,
			byte[] image_data, int w1, int h1, String sUrl, int size) {
		// 模锟斤拷锟斤拷息
		TLRPC.TL_updates updates = new TLRPC.TL_updates();
		updates.date = date;
		updates.from_id = from_id;
		if (gid == 0)
			updates.chat_id = from_id;
		else
			updates.chat_id = gid;
		updates.from_id = from_id;
		updates.id = UserConfig.getSeq();// 锟斤拷证id唯一;
		updates.seq = MessagesStorage.lastSeqValue + 1;
		TLRPC.TL_updateNewMessage update = new TLRPC.TL_updateNewMessage();
		TLRPC.TL_message message = new TLRPC.TL_message();
		update.message = message;
		update.date = date;
		message.from_id = from_id;
		message.date = date;
		message.unread = true;
		message.out = false;
		message.id = updates.id;// 锟斤拷证id唯一;
		if (gid == 0) // sam
		{
			TLRPC.TL_peerUser to_id = new TLRPC.TL_peerUser();
			message.to_id = to_id;
			message.to_id.user_id = UserConfig.clientUserId;
		} else {
			TLRPC.TL_peerChat to_id = new TLRPC.TL_peerChat();
			message.to_id = to_id;
			message.to_id.chat_id = gid;// 锟斤拷id
		}
		TLRPC.TL_messageMediaPhoto media = new TLRPC.TL_messageMediaPhoto();
		message.media = media;
		message.message = "";
		TLRPC.TL_photo photo = new TLRPC.TL_photo();
		message.media.photo = photo;
		photo.id = UserConfig.getSeq();// 唯一锟酵匡拷锟斤拷
		photo.date = date;
		photo.geo = new TLRPC.TL_geoPointEmpty();
		photo.user_id = from_id;
		photo.caption = "";

		TLRPC.FileLocation small_location = new TLRPC.TL_fileLocation();
		small_location.volume_id = UserConfig.getSeq();
		small_location.local_id = UserConfig.clientUserId;
		small_location.secret = 0;
		small_location.dc_id = 5;
		small_location.key = null;
		small_location.iv = null;
		small_location.http_path_img = "";

		TLRPC.PhotoSize smallSize = new TLRPC.TL_photoCachedSize();
		smallSize.type = "s";
		smallSize.location = small_location;
		smallSize.w = w;
		smallSize.h = h;
		smallSize.size = 0;
		smallSize.bytes = image_data;
		photo.sizes.add(smallSize);

		TLRPC.FileLocation big_location = new TLRPC.TL_fileLocation();
		big_location.volume_id = UserConfig.getSeq();
		big_location.local_id = UserConfig.clientUserId;
		big_location.secret = 0;
		big_location.dc_id = 5;
		big_location.key = null;
		big_location.iv = null;
		// int index = sUrl.indexOf('/');
		// String subUrl = sUrl.substring(index+1);
		big_location.http_path_img = Config.getWebHttp() + sUrl;

		TLRPC.TL_photoSize bigSize = new TLRPC.TL_photoSize();
		bigSize.type = "x";
		bigSize.location = big_location;
		bigSize.w = w1;
		bigSize.h = h1;
		bigSize.size = size;// 锟斤拷要一锟斤拷值锟斤拷锟斤拷位锟饺★拷锟斤拷锟揭拷锟斤拷投烁锟斤拷锟斤拷锟�
		bigSize.bytes = null;
		photo.sizes.add(bigSize);

		update.pts = updates.id;// 锟斤拷证id唯一;
		updates.updates.add(update);

		TLRPC.TL_chat oldChat = (TL_chat) MessagesController.getInstance().chats
				.get(gid);
		if (gid != 0) {
			if (oldChat != null)
				MessagesController.getInstance().processUpdates(
						(TLRPC.Updates) updates, false);
			else {
				// 锟斤拷证锟斤拷锟斤拷锟较拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
				synchronized (this) {
					updatesQueue.add(updates);
				}
				GetGroup(gid, from_id);
			}
		} else {
			boolean missingData = MessagesController.getInstance().users
					.get(from_id) == null;
			if (missingData) {
				synchronized (this) {
					updatesQueue.add(updates);
				}
				getUser(from_id);
			} else
				MessagesController.getInstance().processUpdates(
						(TLRPC.Updates) updates, false);
		}
	}

	public void ProcessFile(int from_id, int gid, int date, String sUrl,
			String filename, int fileSize) {
		// 模锟斤拷锟斤拷息
		TLRPC.TL_updates updates = new TLRPC.TL_updates();
		updates.date = date;
		updates.from_id = from_id;
		if (gid == 0)
			updates.chat_id = from_id;
		else
			updates.chat_id = gid;
		updates.from_id = from_id;
		updates.id = UserConfig.getSeq();// 锟斤拷证id唯一;
		updates.seq = MessagesStorage.lastSeqValue + 1;
		TLRPC.TL_updateNewMessage update = new TLRPC.TL_updateNewMessage();
		TLRPC.TL_message message = new TLRPC.TL_message();
		update.message = message;
		update.date = date;
		message.from_id = from_id;
		message.date = date;
		message.unread = true;
		message.out = false;
		message.id = updates.id;// 锟斤拷证id唯一;
		message.message = "";
		if (gid == 0) // sam
		{
			TLRPC.TL_peerUser to_id = new TLRPC.TL_peerUser();
			message.to_id = to_id;
			message.to_id.user_id = UserConfig.clientUserId;
		} else {
			TLRPC.TL_peerChat to_id = new TLRPC.TL_peerChat();
			message.to_id = to_id;
			message.to_id.chat_id = gid;// 锟斤拷id
		}

		TLRPC.TL_messageMediaDocument media = new TLRPC.TL_messageMediaDocument();
		message.media = media;
		message.media.photo = null;

		String ext = "";
		int idx = filename.lastIndexOf(".");
		if (idx != -1) {
			ext = filename.substring(idx);
		}
		TLRPC.TL_document document = new TLRPC.TL_document();
		document.thumb = new TLRPC.TL_photoSizeEmpty();
		document.thumb.type = "s";
		document.id = UserConfig.getSeq();// 锟斤拷证id唯一;
		document.date = getCurrentTime();
		document.file_name = filename;
		document.size = fileSize;
		document.dc_id = UserConfig.clientUserId;
		document.user_id = from_id;
		document.size = fileSize;

		if (ext.length() != 0) {
			MimeTypeMap myMime = MimeTypeMap.getSingleton();
			String mimeType = myMime
					.getMimeTypeFromExtension(ext.toLowerCase());
			if (mimeType != null) {
				document.mime_type = mimeType;
			} else {
				document.mime_type = "application/octet-stream";
			}
		} else {
			document.mime_type = "application/octet-stream";
		}
		document.http_path = Config.getWebHttp() + sUrl;
		message.media.document = document;

		update.pts = updates.id;// 锟斤拷证id唯一;
		updates.updates.add(update);

		TLRPC.TL_chat oldChat = (TL_chat) MessagesController.getInstance().chats
				.get(gid);
		if (gid != 0) {
			if (oldChat != null)
				MessagesController.getInstance().processUpdates(
						(TLRPC.Updates) updates, false);
			else {
				// 锟斤拷证锟斤拷锟斤拷锟较拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
				synchronized (this) {
					updatesQueue.add(updates);
				}
				GetGroup(gid, from_id);
			}
		} else {
			boolean missingData = MessagesController.getInstance().users
					.get(from_id) == null;
			if (missingData) {
				synchronized (this) {
					updatesQueue.add(updates);
				}
				getUser(from_id);
			} else
				MessagesController.getInstance().processUpdates(
						(TLRPC.Updates) updates, false);
		}
	}

	public void ProcessAlert(int from_id, int gid, int date, String sMsg,
			String guid, int alertTime, int status) {
		// 模锟斤拷锟斤拷息 todo..没锟斤拷锟斤拷锟截ｏ拷锟斤拷锟斤拷应锟矫伙拷锟斤拷google play,锟节癸拷锟斤拷没锟斤拷锟斤拷锟斤拷
		TLRPC.TL_updates updates = new TLRPC.TL_updates();
		updates.date = date;
		updates.from_id = from_id;
		if (gid == 0)
			updates.chat_id = from_id;
		else
			updates.chat_id = gid;
		updates.from_id = from_id;
		updates.id = UserConfig.getSeq();// 锟斤拷证id唯一;
		updates.seq = MessagesStorage.lastSeqValue + 1;
		TLRPC.TL_updateNewMessage update = new TLRPC.TL_updateNewMessage();
		TLRPC.TL_message message = new TLRPC.TL_message();
		update.message = message;
		update.date = date;
		message.from_id = from_id;
		message.date = date;
		message.unread = true;
		message.out = false;
		message.id = updates.id;// 锟斤拷证id唯一;
		if (gid == 0) // sam
		{
			TLRPC.TL_peerUser to_id = new TLRPC.TL_peerUser();
			message.to_id = to_id;
			message.to_id.user_id = UserConfig.clientUserId;
		} else {
			TLRPC.TL_peerChat to_id = new TLRPC.TL_peerChat();
			message.to_id = to_id;
			message.to_id.chat_id = gid;// 锟斤拷id
		}
		TLRPC.TL_messageMediaAlert geoInputMedia = new TLRPC.TL_messageMediaAlert();
		message.media = geoInputMedia;
		message.message = sMsg;
		// 只锟叫达拷锟斤拷锟斤拷时锟斤拷锟斤拷锟揭拷锟斤拷锟絘lert.id锟斤拷锟斤拷锟斤拷删锟斤拷锟斤拷实锟斤拷锟斤拷同一锟斤拷id,锟斤拷锟斤拷锟侥伙拷锟斤拷要锟斤拷询DB
		// 锟斤拷知锟斤拷锟筋开始锟斤拷alert锟斤拷应锟斤拷id值锟斤拷锟斤拷锟斤拷scheduleAlert锟斤拷示锟矫伙拷
		TLRPC.TL_alertMedia alertClass = new TLRPC.TL_alertMedia();
		message.media.alert = alertClass;
		alertClass.msg = sMsg;
		alertClass.guid = guid;
		alertClass.date = alertTime;
		alertClass.status = status;
		alertClass.lastModifyTime = getCurrentTime();
		if (status == 0)// 0锟斤拷示锟斤拷锟斤拷锟斤拷锟窖ｏ拷1锟斤拷示锟斤拷锟斤拷锟斤拷锟窖ｏ拷2锟斤拷示删锟斤拷锟斤拷锟斤拷
			alertClass.id = UserConfig.getAlertId();
		update.pts = updates.id;
		updates.updates.add(update);

		if (gid != 0) {
			TLRPC.TL_chat oldChat = (TL_chat) MessagesController.getInstance().chats
					.get(gid);
			if (oldChat != null)
				MessagesController.getInstance().processUpdates(
						(TLRPC.Updates) updates, false);
			else {
				// 锟斤拷证锟斤拷锟斤拷锟较拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
				synchronized (this) {
					updatesQueue.add(updates);
				}

				GetGroup(gid, from_id);
			}
		} else {
			boolean missingData = MessagesController.getInstance().users
					.get(from_id) == null;
			if (missingData) {
				synchronized (this) {
					updatesQueue.add(updates);
				}
				getUser(from_id);
			} else
				MessagesController.getInstance().processUpdates(
						(TLRPC.Updates) updates, false);
		}
	}

	public void applyDcPushUpdate(final int dc, final String ip_address,
			final int port) {
	}

	public TLObject getRequestWithMessageId(long msgId) {
		/*
		 * for (RPCRequest request : runningRequests) { if (msgId ==
		 * request.runningMessageId) { return request.rawRequest; } }
		 */
		return null;
	}

	private TLRPC.User parseUserResult(JSONObject jo) throws JSONException {
		// 锟斤拷锟斤拷锟绞号碉拷录锟斤拷锟斤拷锟斤拷锟斤拷锟街伙拷锟斤拷锟截伙拷锟芥，锟脚伙拷锟斤拷锟铰斤拷锟斤拷锟绞伙拷锟斤拷息
		// 锟斤拷锟斤拷头锟斤拷筒锟斤拷锟斤拷耍锟斤拷没锟斤拷约锟斤拷锟斤拷锟斤拷锟斤拷茫锟斤拷锟斤拷锟斤拷锟絞etcompany锟斤拷时锟斤拷锟斤拷锟斤拷锟斤拷禄指锟斤拷锟斤拷原锟斤拷锟斤拷锟矫碉拷头锟斤拷
		TLRPC.User user = new TLRPC.TL_userContact();
		user.id = jo.getInt("userid");
		user.first_name = jo.getString("firstname");
		user.last_name = jo.getString("lastname").equals("null")?"":jo.getString("lastname");
		user.phone = jo.getString("mobile");
		user.email = jo.getString("email");
		user.sessionid = jo.optString("sessionid");
		// user.nickname = jo.getString("nickname");
		user.gender = jo.getInt("gender");
		user.userico = jo.getString("userico");
		// user.serverid = jo.optInt("serverid");
		// user.sortlevel = jo.optInt("sortlevel");
		user.description = jo.getString("description");

		// user.version = Config.DefVersion;
		user.photo = new TLRPC.TL_userProfilePhotoEmpty();
		buildPhoto(user);
		user.inactive = false;
		TLRPC.TL_userStatusEmpty userStatus = new TLRPC.TL_userStatusEmpty();
		userStatus.expires = jo.optInt("state", 0);
		user.status = userStatus;
		// 通讯锟斤拷时锟斤拷使锟斤拷锟斤拷锟斤拷锟绞讹拷锟斤拷锟绞癸拷锟絬serid锟剿ｏ拷锟斤拷锟斤拷PHP锟斤拷锟斤拷锟斤拷锟斤拷使锟斤拷userid
		user.identification = jo.getString("identification");  //931012958

		// IM锟斤拷锟斤拷锟斤拷锟斤拷ip锟酵端匡拷
		String imserverip = jo.optString("imserverip");
		int imserverport = jo.optInt("imserverport");
		user.productmodel = jo.optInt("productmodel");

		if (imserverip != null && !imserverip.isEmpty()) {
			Config.MESSAGEHOST = imserverip;
		} else {
			Config.MESSAGEHOST = Config.getWebHttp();
		}

		Config.MESSAGEPORT = imserverport;
		if (!UserConfig.isPublic) {
			if (imserverip == null || imserverip.isEmpty()
					|| "null".equals(imserverip)) {
				UserConfig.privateMESSAGEHOST = UserConfig.privateWebHttp;
			} else {
				UserConfig.privateMESSAGEHOST = imserverip.trim();
			}
			UserConfig.privateMESSAGEPORT = imserverport;

			UserConfig.saveConfig(true);   
		}

		FileLog.e("emm", "parse user result session id=" + user.sessionid);

		/*
		 * if( jo.has("accounts") ) { //锟斤拷取锟揭绑定碉拷锟绞猴拷 ArrayList<String> accounts =
		 * new ArrayList<String>(); JSONArray jsonArrAccounts =
		 * jo.getJSONArray("accounts"); for(int j = 0; j <
		 * jsonArrAccounts.length(); ++j) { // 锟斤拷司锟斤拷一锟斤拷array 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷司锟斤拷锟捷硷拷录 String
		 * account = jsonArrAccounts.getString(j); // FileLog.e("emm",
		 * "get account from server="+account); accounts.add(account); }
		 * MessagesController.getInstance().processLoadedAccounts(accounts, 0);
		 * }
		 */
		return user;
	}

	private TLRPC.User buildUser(JSONObject jo) throws JSONException {
		TLRPC.User user = new TLRPC.TL_userContact();
		user.id = jo.getInt("userid");
		user.first_name = jo.optString("firstname");
		user.last_name = jo.optString("lastname");
		user.phone = jo.optString("mobile");
		user.email = jo.optString("email");
		// user.sessionid = jo.getString("sessionid");
		user.nickname = jo.optString("nickname");
		user.gender = jo.optInt("gender");
		user.userico = jo.optString("userico");
		// user.serverid = jo.optInt("serverid");
		// user.sortlevel = jo.optInt("sortlevel");
		user.description = jo.optString("description");
		user.identification = jo.getString("identification");
		// user.version = Config.DefVersion;
		user.photo = new TLRPC.TL_userProfilePhotoEmpty();
		buildPhoto(user);
		user.inactive = false;
		TLRPC.TL_userStatusEmpty userStatus = new TLRPC.TL_userStatusEmpty();
		userStatus.expires = jo.optInt("state", 0);
		user.status = userStatus;

		return user;
	}

	public void parseJoinCompanyInfo(JSONObject json) {
		try {
			TLRPC.contacts_Contacts res = new TLRPC.contacts_Contacts();
			if (json.has("result") && json.getInt("result") != 0)
				return;

			if (json.has("joincompany") && !json.isNull("joincompany")) {
				JSONArray jsonArrCompany = json.getJSONArray("joincompany");
				for (int j = 0; j < jsonArrCompany.length(); ++j) {
					// 锟斤拷司锟斤拷一锟斤拷array 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷司锟斤拷锟捷硷拷录
					JSONObject jsonObj = (JSONObject) jsonArrCompany
							.getJSONObject(j);

					TLRPC.TL_PendingCompanyInfo company = new TLRPC.TL_PendingCompanyInfo();
					company.id = jsonObj.getInt("companyid");
					String fullname = jsonObj.optString("companyfullname");
					if (fullname == null || fullname.equals(""))
						company.name = jsonObj.optString("companyname");
					else
						company.name = fullname;
					String first_name = jsonObj.optString("firstname");
					String last_name = jsonObj.optString("lastname");

					company.inviteName = Utilities.formatName(first_name,
							last_name);
					res.pendingCompanys.add(company);
					// FileLog.e("emm",company.inviteName + " " +company.name);
				}
				MessagesController.getInstance().processLoadedPendingCompany(
						res.pendingCompanys, 0);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void parseUserCompanyInfo(JSONObject json) {
		try {
			TLRPC.contacts_Contacts res = new TLRPC.contacts_Contacts();
			if (json.has("result") && json.getInt("result") != 0) {
				MessagesController.getInstance().processLoadedUserCompany(
						res.userCompanys, 0);
				return;
			}
			if (json.has("companyuser") && !json.isNull("companyuser")) {
				JSONArray jsonArrCompany = json.getJSONArray("companyuser");
				for (int j = 0; j < jsonArrCompany.length(); ++j) {
					// 锟斤拷司锟斤拷一锟斤拷array 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷司锟斤拷锟捷硷拷录
					JSONObject jsonObj = (JSONObject) jsonArrCompany
							.getJSONObject(j);

					TLRPC.TL_UserCompany company = new TLRPC.TL_UserCompany();
					company.companyID = jsonObj.optInt("companyid", 0);
					if (company.companyID == 0)
						continue;
					company.userID = jsonObj.getInt("userid");
					company.deptID = jsonObj.optInt("deptid", 0);
					if (company.deptID == 0)
						continue;
					// 0:锟斤拷锟斤拷,1:锟斤拷删锟斤拷锟斤拷锟斤拷 2锟窖帮拷装未同锟斤拷 3 未锟斤拷装,锟斤拷锟斤拷侄锟斤拷没锟斤拷锟斤拷锟斤拷锟斤拷只锟斤拷没锟斤拷锟阶刺�
					company.ucstate = jsonObj.getInt("ucstate");

					company.first_name = jsonObj.optString("firstname");
					company.last_name = jsonObj.optString("lastname");

					FileLog.e("emm", Utilities.formatName(company.first_name,
							company.last_name));

					// 锟矫伙拷锟斤拷色ID,锟斤拷锟轿�0锟角达拷锟斤拷锟竭ｏ拷1锟角癸拷锟斤拷员锟斤拷2锟斤拷锟斤拷通锟矫伙拷
					company.userRoleID = jsonObj.getInt("userroleid");

					company.email = jsonObj.optString("email");
					company.mobile = jsonObj.optString("mobile");

					company.locationid = jsonObj.optInt("locationid");// xiaoyang
					// 锟斤拷锟斤拷锟接碉拷锟街讹拷
					company.productline = jsonObj.optString("productline");// xiaoyang
					// 锟斤拷锟斤拷锟接碉拷锟街讹拷
					company.usertitle = jsonObj.optString("usertitle");// xiaoyang
					// 锟斤拷锟斤拷锟接碉拷锟街讹拷

					res.userCompanys.add(company);

					if (company.ucstate == 1) {
						for (TLRPC.User user : MessagesController.getInstance().searchUsers) {
							if (user.id == company.userID) {
								MessagesController.getInstance().searchUsers
								.remove(user);
								break;
							}
						}
					}

				}

			}
			MessagesController.getInstance().processLoadedUserCompany(
					res.userCompanys, 0);

		} catch (JSONException e) {
			MessagesController.getInstance().processLoadedUserCompany(null, 0);
			e.printStackTrace();
		} catch (Exception e) {
			MessagesController.getInstance().processLoadedUserCompany(null, 0);
			e.printStackTrace();
		}
	}

	// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷莸锟絁son
	public boolean parseEnterpriseBook(JSONObject json) {
		try {
			TLRPC.contacts_Contacts res = new TLRPC.contacts_Contacts();
			boolean bHasChange = false;
			// JSONObject json = new JSONObject(strResult);
			if (json.has("result") && json.getInt("result") != 0)
				return false;

			if (json.has("company") && !json.isNull("company")) {
				JSONArray jsonArrCompany = json.getJSONArray("company");
				for (int j = 0; j < jsonArrCompany.length(); ++j) {
					// 锟斤拷司锟斤拷一锟斤拷array 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷司锟斤拷锟捷硷拷录
					JSONObject jsonObj = (JSONObject) jsonArrCompany
							.getJSONObject(j);

					TLRPC.TL_Company company = new TLRPC.TL_Company();
					company.id = jsonObj.getInt("companyid");
					String fullname = jsonObj.getString("companyfullname");
					if (fullname == null || fullname.equals(""))
						company.name = jsonObj.getString("companyname");
					else
						company.name = fullname;
					company.ico = jsonObj.getString("ico");
					company.functionItem = jsonObj.getString("functionitem");
					company.version = jsonObj.getInt("version");
					// company.status>=2锟斤拷示锟斤拷效锟侥癸拷司
					company.status = jsonObj.getInt("companystate");
					company.rootdeptid = jsonObj.getInt("rootdeptid");
					company.photo = buildChatPhoto(company.ico);
					// 锟斤拷锟斤拷锟接碉拷锟斤拷锟斤拷
					company.createmode = jsonObj.optInt("createmode");
					company.createuserid = jsonObj.optInt("createuserid");
					company.totalnum = jsonObj.optInt("totalnum");
					company.balance = jsonObj.optInt("balance");
					company.productmodel = jsonObj.optInt("productmodel");
					res.companys.add(company);
					// FileLog.e("emm", company.name+" " + company.id);
					bHasChange = true;
				}


			}

			// 锟斤拷取锟斤拷锟斤拷
			if (json.has("department") && !json.isNull("department")) {
				JSONArray jsonArrDept = json.getJSONArray("department");
				for (int j = 0; j < jsonArrDept.length(); ++j) {
					// 锟斤拷锟斤拷锟斤拷一锟斤拷array 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟捷硷拷录
					JSONObject jo = (JSONObject) jsonArrDept.getJSONObject(j);

					TLRPC.TL_DepartMent dept = new TLRPC.TL_DepartMent();
					dept.id = jo.getInt("deptid");
					dept.name = jo.getString("deptname");
					dept.companyID = jo.getInt("companyid");
					dept.deptParentID = jo.getInt("deptparentid");
					dept.deptLevel = jo.optInt("deptlevel", 0);
					dept.sortLevel = jo.optInt("sortlevel");
					dept.version = jo.getInt("version");
					// status 为 0锟斤拷示未锟斤拷锟筋，为1锟斤拷示锟斤拷锟斤拷锟斤拷锟轿�2锟斤拷示锟窖撅拷删锟斤拷
					dept.status = jo.optInt("deptstate", 0);
					dept.totalnum = jo.optInt("totalnum", 0);
					res.departments.add(dept);
					bHasChange = true;
				}
			}

			// 锟斤拷取锟矫伙拷
			if (json.has("user") && !json.isNull("user")) {
				// 应锟矫帮拷锟斤拷锟斤拷锟皆硷拷
				JSONArray jsonArrUser = json.getJSONArray("user");
				ArrayList<TLRPC.User> userArr = new ArrayList<TLRPC.User>();
				for (int j = 0; j < jsonArrUser.length(); ++j) {// 锟矫伙拷锟斤拷一锟斤拷array
					// 锟斤拷锟斤拷锟斤拷锟斤拷锟矫伙拷锟斤拷锟捷硷拷录
					JSONObject jo = (JSONObject) jsonArrUser.getJSONObject(j);
					String a = jo.toString();
					TLRPC.TL_userContact user = new TLRPC.TL_userContact();
					user.id = jo.getInt("userid");
					user.phone = jo.getString("mobile");
					user.email = jo.getString("email");
					// user.nickname = jo.getString("nickname");
					user.first_name = jo.getString("firstname");
					user.last_name = jo.getString("lastname");
					user.gender = jo.getInt("gender");
					user.userico = jo.getString("userico");
					user.serverid = jo.optInt("serverid");
					user.sortlevel = jo.optInt("sortlevel");
					user.description = jo.getString("description");

					buildPhoto(user);

					// status 为 0锟斤拷示未锟斤拷锟筋，为1锟斤拷示锟斤拷锟斤拷锟斤拷锟轿�2锟斤拷示锟窖撅拷删锟斤拷
					// 锟斤拷锟斤拷锟接碉拷锟斤拷业锟矫伙拷直锟接放碉拷DB锟斤拷锟节达拷锟叫既匡拷
					TLRPC.TL_userStatusEmpty userStatus = new TLRPC.TL_userStatusEmpty();
					userStatus.expires = jo.optInt("state", 0);
					user.status = userStatus;
					user.version = jo.optInt("version");
					user.companyid = jo.optInt("companyid");
					user.deptid = jo.optInt("deptid");
					// 0锟角筹拷锟斤拷锟斤拷锟斤拷1锟斤拷系统锟斤拷锟斤拷员锟斤拷2锟斤拷锟斤拷通锟矫伙拷
					user.userRoleID = jo.optInt("userroleid");
					user.identification = jo.getString("identification");

					// 0:锟斤拷锟斤拷,1:锟斤拷删锟斤拷锟斤拷锟斤拷 2锟窖帮拷装未同锟斤拷 3 未锟斤拷装,锟斤拷锟斤拷侄锟斤拷没锟斤拷锟斤拷锟斤拷锟斤拷只锟斤拷没锟斤拷锟阶刺�
					// 锟斤拷锟街伙拷锟矫伙拷注锟斤拷螅锟斤拷锟斤拷锟剿撅拷锟绞憋拷锟斤拷锟斤拷锟斤拷锟剿ｏ拷锟斤拷锟斤拷锟斤拷锟解几锟斤拷状态锟斤拷锟斤拷然也锟斤拷要锟斤拷UI锟斤拷锟斤拷锟街筹拷锟斤拷
					res.users.add(user);

					// FileLog.e("emm",
					// "username="+user.first_name+user.last_name+" userid="+user.id
					// + " userstate="+user.status.expires);
					bHasChange = true;
				}
			}

			if (bHasChange) {
				// 锟斤拷锟皆凤拷锟斤拷锟斤拷锟侥癸拷司锟斤拷锟斤拷锟脚猴拷锟斤拷员锟斤拷息
				ArrayList<TLRPC.TL_contact> contactsArr = new ArrayList<TLRPC.TL_contact>();
				MessagesController.getInstance().processEnterPriseContacts(
						contactsArr, res.users, res.companys, res.departments,
						0);
				return true;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean parseRemarkUser(JSONObject json) {
		// 锟斤拷取remarkuser
		try {
			if (json.has("remarkuser") && !json.isNull("remarkuser")) {
				JSONArray jsonArrDept = json.getJSONArray("remarkuser");
				for (int j = 0; j < jsonArrDept.length(); ++j) {
					// 锟斤拷锟斤拷锟斤拷一锟斤拷array 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟捷硷拷录
					JSONObject jo = (JSONObject) jsonArrDept.getJSONObject(j);
					// int userid = jo.getInt("userid");
					int renameid = jo.getInt("renameid");
					String name = jo.getString("name");
					TLRPC.User user = MessagesController.getInstance().users
							.get(renameid);
					if (user != null) {
						user.nickname = name;
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	private void sendLoginRequest(final String httpUrl,
			final RequestParams params,
			final RPCRequest.RPCRequestDelegate completionBlock, final int act) {
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				if (client == null)
					return;

				FileLog.d("emm", "post url:" + httpUrl);
				client.setTimeout(60000);
				client.post(httpUrl, params, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						FileLog.d("emm", "response:" + response);
						final String res = response;
						Utilities.stageQueue.postRunnable(new Runnable() {
							@Override
							public void run() {
								try {
									Log.d("emm", "params=" + params);
									JSONTokener jsonParser = new JSONTokener(
											res);
									JSONObject ret = (JSONObject) jsonParser
											.nextValue();
									if (ret.has("result")
											&& ret.getInt("result") == -2) {
										// 实锟斤拷锟斤拷注锟斤拷锟剿ｏ拷锟斤拷示session锟斤拷锟斤拷锟斤拷,lhy
										// todo,锟斤拷锟叫猴拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷result
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = -2;
										completionBlock.run(null, networkerror);
										reportNetworkStatus(100);
										return;
									}
									int bRet = 0;
									if (ret.has("result"))
										bRet = ret.getInt("result");

									if (act == 1)// 锟斤拷示getdicode
									{
										if (bRet == 0) {
											// TLRPC.TL_auth_authorization auth
											// = new
											// TLRPC.TL_auth_authorization();
											// auth.expires = bRet;
											TLRPC.TL_auth_authorization auth = new TLRPC.TL_auth_authorization();
											completionBlock.run(auth, null);
										} else {
											TLRPC.TL_error networkerror = new TLRPC.TL_error();
											networkerror.code = bRet;
											completionBlock.run(null,
													networkerror);
										}
									}
									if (act == 2)// CheckIDCode
									{
										if (bRet == 0) {
											TLRPC.TL_auth_authorization auth = new TLRPC.TL_auth_authorization();
											completionBlock.run(auth, null);
										} else {
											TLRPC.TL_error networkerror = new TLRPC.TL_error();
											networkerror.code = bRet;
											completionBlock.run(null,
													networkerror);
										}
									}
									if (act == 3)// Register
									{
										if (bRet == 0) {
											TLRPC.TL_auth_authorization auth = new TLRPC.TL_auth_authorization();
											completionBlock.run(auth, null);
										} else {
											TLRPC.TL_error networkerror = new TLRPC.TL_error();
											networkerror.code = bRet;
											completionBlock.run(null,
													networkerror);
										}
									}
									if (act == 4)// CheckUserPwd
									{
										if (bRet == 0) {
											TLRPC.User user = null;
											if (ret.has("userid")
													&& ret.has("firstname")) {
												user = parseUserResult(ret);
												FileLog.d("emm","login result:"+ user.id+ " sid:"+ user.sessionid);
											}
											TLRPC.TL_auth_authorization auth = new TLRPC.TL_auth_authorization();
											auth.user = user;
											completionBlock.run(auth, null);
										} else {
											TLRPC.TL_error networkerror = new TLRPC.TL_error();
											networkerror.code = bRet;
											completionBlock.run(null,
													networkerror);
										}
									}
									if (act == 5)// ResetPassword
									{
										if (bRet == 0) {
											TLRPC.TL_auth_authorization auth = new TLRPC.TL_auth_authorization();
											completionBlock.run(auth, null);
										} else {
											TLRPC.TL_error networkerror = new TLRPC.TL_error();
											networkerror.code = bRet;
											completionBlock.run(null,
													networkerror);
										}
									}

								} catch (Exception e) {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -2;
									completionBlock.run(null, networkerror);
								}
							}
						});
					}

					@Override
					public void onFailure(Throwable error, String content) {
						Utilities.stageQueue.postRunnable(new Runnable() {
							@Override
							public void run() {
								TLRPC.TL_error networkerror = new TLRPC.TL_error();
								networkerror.code = -2;
								completionBlock.run(null, networkerror);
							}
						});
					}
				});
			}
		});
	}

	/**
	 * 锟斤拷取锟斤拷证锟斤拷
	 * 
	 * @param userID
	 *            锟街伙拷锟脚伙拷锟斤拷email
	 * @return
	 */
	// 锟斤拷锟斤拷锟斤拷锟诫发锟斤拷1锟斤拷锟斤拷锟斤拷锟斤拷0
	public void GetIDCode(final String email, final int type,
			final RPCRequest.RPCRequestDelegate completionBlock) {
		String tempAccount = email;
		UserConfig.account = email;
		/*
		 * if(type!=2) { tempAccount = UserConfig.getFullName(email);
		 * UserConfig.account = email; UserConfig.saveConfig(false); }
		 */

		RequestParams params = new RequestParams();
		params.put("account", tempAccount);
		params.put("flag", "" + type);
		sendLoginRequest(Config.webFun_sendverificationcode, params,
				completionBlock, 1);
	}

	public void CheckIDCode(final String account, final String IDCode,
			final RPCRequest.RPCRequestDelegate completionBlock) {
		String tempAccount = account;
		RequestParams params = new RequestParams();
		params.put("account", account);
		params.put("verif", IDCode);
		sendLoginRequest(Config.webFun_checkverificationcode, params,
				completionBlock, 2);
	}

	public void Register(final String account, final String password,
			final String firstname, final String lastname,
			final String hasCode,
			final RPCRequest.RPCRequestDelegate completionBlock) {
		String tempAccount = account;
		String encPwd = Utilities.MD5(password);
		String encAccount = Utilities.MD5(tempAccount);
		String newPwd = Utilities.MD5(encPwd + encAccount);

		RequestParams params = new RequestParams();
		params.put("account", tempAccount);
		params.put("userpwd", newPwd);
		params.put("lastname", lastname);
		params.put("firstname", firstname);
		params.put("verif", hasCode);
		if (UserConfig.isPersonalVersion)
			params.put("mobile", tempAccount);

		sendLoginRequest(Config.webFun_registeredusers, params,
				completionBlock, 3);
	}

	public void CheckLogin(final String domain, final String account,
			final String password,
			final RPCRequest.RPCRequestDelegate completionBlock) {
		String tempAccount = account;
		String deviceID = Utilities.getDeviceID();
		String encPwd = Utilities.MD5(password);
		String encAccount = Utilities.MD5(tempAccount);
		String newPwd = Utilities.MD5(encPwd + encAccount);

		FileLog.e("emm", "CheckLogin:" + tempAccount);

		RequestParams params = new RequestParams();
		params.put("account", tempAccount);
		params.put("userpwd", newPwd);
		params.put("deviceno", deviceID);
		params.put("devicetype", "0");

		// if(UserConfig.isPersonalVersion)
		// {
		// if( UserConfig.isPublic )
		// params.put("mobile", tempAccount);
		// else
		// params.put("email", tempAccount);
		// }
		if (UserConfig.isPublic) {
			if (UserConfig.isPersonalVersion) {
				params.put("mobile", tempAccount);
			} else {
				if (domain != null && !domain.isEmpty()) {
					params.put("domain", domain);
				}
			}
		}

		sendLoginRequest(Config.webFun_checkuserpwd, params, completionBlock, 4);

	}

	public void CheckLogin(final String token,
			final RPCRequest.RPCRequestDelegate completionBlock) {
		String deviceID = Utilities.getDeviceID();
		RequestParams params = new RequestParams();
		params.put("geauthcode", token);
		params.put("deviceno", deviceID);
		params.put("devicetype", "0");
		params.put("geversion", "1");
		sendLoginRequest(Config.webFun_checkuserpwd, params, completionBlock, 4);
	}

	public void ResetPassword(final String account, final String pwd,
			final String IDCode,
			final RPCRequest.RPCRequestDelegate completionBlock) {
		// resetpassword //锟斤拷锟斤拷锟斤拷锟斤拷
		// account //锟矫伙拷锟绞猴拷
		// userpwd //锟矫伙拷锟斤拷锟斤拷
		// verif //锟斤拷证锟斤拷
		String tempAccount = account;
		String encPwd = Utilities.MD5(pwd);
		// String encAccount = Utilities.MD5(tempAccount);
		// String newPwd = Utilities.MD5(encPwd+encAccount);

		RequestParams params = new RequestParams();
		params.put("account", tempAccount);
		params.put("userpwd", encPwd);
		params.put("verif", IDCode);
		params.put("versiontype", "1");
		sendLoginRequest(Config.webFun_resetpassword, params, completionBlock,
				5);
	}

	private int parseUpdateMeetingResult(JSONObject jo,
			TLRPC.TL_UpdateMeetingResult res) {
		int result = -1;
		try {
			if (!jo.has("result"))
				return result;
			if (jo.has("serial")) {
				int mid = jo.getInt("serial");
				res.mid = mid;
				res.version = jo.getInt("version");
				return mid;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean parseChannalInfo(JSONObject json) {
		try {
			if (!json.has("channel"))
				return false;

			ArrayList<TLRPC.TL_ChannalInfo> infos = new ArrayList<TLRPC.TL_ChannalInfo>();
			JSONArray jsonArray = json.optJSONArray("channel");
			if (jsonArray == null) {
				MessagesController.getInstance().processLoadChnInfo(infos, 0);
				return false;
			}

			for (int i = 0; i < jsonArray.length(); ++i) {
				JSONObject jsonMeeting = (JSONObject) jsonArray
						.getJSONObject(i);
				TLRPC.TL_ChannalInfo meeting = new TLRPC.TL_ChannalInfo();
				meeting.channelid = jsonMeeting.getInt("channelid");
				meeting.type = jsonMeeting.getInt("type");
				// FileLog.e("emm",
				// "chanid="+meeting.channelid+" ***** type="+meeting.type);
				infos.add(meeting);
			}

			MessagesController.getInstance().processLoadChnInfo(infos, 0);

			return true;

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷莸锟絁son
	public boolean parseMeetingBook(JSONObject json) {
		try {
			// 锟斤拷锟斤拷锟斤拷锟斤拷薷模锟斤拷锟缴撅拷锟斤拷锟斤拷锟较拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟较�,锟斤拷锟斤拷没锟斤拷meeting key锟斤拷只锟斤拷meetinguser key,锟斤拷锟揭拷锟斤拷锟�
			ArrayList<TL_MeetingInfo> infos = new ArrayList<TL_MeetingInfo>();
			ConcurrentHashMap<Integer, TL_MeetingInfo> midMap = new ConcurrentHashMap<Integer, TL_MeetingInfo>(
					100, 1.0f, 2);
			if (json.has("result") && json.getInt("result") != 0) {
				return false;
			}

			JSONArray jsonArray = json.optJSONArray("meeting");
			if (jsonArray != null) {
				for (int i = 0; i < jsonArray.length(); ++i) {
					JSONObject jsonMeeting = (JSONObject) jsonArray
							.getJSONObject(i);
					TLRPC.TL_MeetingInfo meeting = new TLRPC.TL_MeetingInfo();

					meeting.mid = jsonMeeting.getInt("serial");


					meeting.topic = jsonMeeting.optString("meetingname");
					meeting.createid = jsonMeeting.optInt("userid");
					meeting.startTime = jsonMeeting.optInt("starttime");
					meeting.endTime = jsonMeeting.optInt("endtime");
					meeting.chairmanpwd = jsonMeeting.optString("chairmanpwd");					
					meeting.confuserpwd = jsonMeeting.optString("confuserpwd");
					if(meeting.confuserpwd.compareTo("null")==0)
						meeting.confuserpwd = "";
					meeting.sidelineuserpwd = jsonMeeting.optString("sidelineuserpwd");
					if (meeting.sidelineuserpwd.compareTo("null") == 0)
						meeting.sidelineuserpwd = "";
					meeting.autoopenav = jsonMeeting.optInt("autoopenav", 0);// 为0锟斤拷示锟斤拷锟斤拷锟斤拷为1锟斤拷示锟皆讹拷锟斤拷
					meeting.state = jsonMeeting.optInt("meetingstate", 0);// 0:锟斤拷锟斤拷
					//qxm  add
					meeting.beginTime = jsonMeeting.optString("begintime");
					meeting.ispublicMeeting =jsonMeeting.optInt("visiblemeeting");
					meeting.duration = jsonMeeting.optString("duration");
					meeting.meetingType = jsonMeeting.optInt("meetingtype");
					meeting.meetingsubject = jsonMeeting.optString("meetingsubject");

					// 1:删锟斤拷
					infos.add(meeting);
					// 锟斤拷锟斤拷映锟戒，锟斤拷锟矫伙拷锟斤拷拥锟絤eetinginfo锟斤拷使锟斤拷
					midMap.put(meeting.mid, meeting);
					String midmString = midMap.toString();

					// wangxm add for notifactionView
					/*
					 * if (meeting.createrid != UserConfig.clientUserId &&
					 * this.lastVersion!=-1) {
					 * MessagesController.getInstance().showMeetInAppView
					 * (meeting); }
					 */
				}
			}

			JSONArray joUsers = json.optJSONArray("meetinguser");
			if(joUsers!=null)
			{ 
				for(int j = 0; j < joUsers.length(); ++j) 
				{
					JSONObject jo = joUsers.getJSONObject(j);
					int mid = jo.getInt("serial"); 
					int sendid = jo.getInt("sendid"); 
					int	receiveid = jo.getInt("receiveid"); 
					int sendflag = jo.optInt("sendflag"); //锟角凤拷锟街�,1锟斤拷示锟斤拷执锟斤拷0锟斤拷示没锟斤拷 int
					int	receiptflag = jo.optInt("receiptflag",0); 
					int state = jo.optInt("state",0);

					TL_MeetingInfo info = midMap.get(mid);
					if(info==null)
						continue;
					String map = midMap.toString();
					if( info == null) {
						//说锟斤拷没锟斤拷锟斤拷meeting 锟斤拷息,只锟斤拷锟斤拷锟斤拷meetinguser锟斤拷息 info =
						MessagesController.getInstance().meetings.get(mid);
						if(info != null)
							infos.add(info); 
					}

					info.participants.add(sendid); 
					info.participants.add(receiveid);


					if(receiptflag==1) { //锟斤拷示锟斤拷锟斤拷私锟斤拷盏锟斤拷嘶锟斤拷锟斤拷锟较拷锟斤拷丫锟斤拷锟接︼拷锟�
						info.resParticipants.add(receiveid); 
					} 
					if( state == 1) {
						//0:锟斤拷锟斤拷,1:锟斤拷删锟斤拷,锟斤拷锟斤拷锟缴撅拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷约锟斤拷锟斤拷锟接︼拷媒锟斤拷锟斤拷鼗锟斤拷锟斤拷锟斤拷锟斤拷
						info.participants.remove(receiveid);
						info.resParticipants.remove(receiveid); 
					}
				}
			}
			MessagesController.getInstance().processLoadMeetings(infos, 0);

			// 锟斤拷锟斤拷锟斤拷锟斤拷薷模锟斤拷锟缴撅拷锟斤拷锟斤拷锟较拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟较�,锟斤拷锟斤拷没锟斤拷meeting key锟斤拷只锟斤拷meetinguser key,锟斤拷锟揭拷锟斤拷锟�
			ArrayList<TLRPC.TL_DirectPlayBackList> mInfosPlay = new ArrayList<TLRPC.TL_DirectPlayBackList>();
			ConcurrentHashMap<Integer,TLRPC.TL_DirectPlayBackList> mMap = new ConcurrentHashMap<Integer, TLRPC.TL_DirectPlayBackList>(100, 1.0f, 2);
			if (json.has("result") && json.getInt("result") != 0) {
				return false;
			}

			JSONArray jsonArrayBack = json.optJSONArray("livevideolist");
			Log.e("TAG", "parsemeetingBook"+jsonArrayBack);
			if (jsonArrayBack != null) {
				for (int i = 0; i < jsonArrayBack.length(); ++i) {
					JSONObject jsonPlayBack = (JSONObject) jsonArrayBack.getJSONObject(i);
					TLRPC.TL_DirectPlayBackList playback = new TLRPC.TL_DirectPlayBackList();
					playback.mId = jsonPlayBack.getInt("serial");
					playback.createuserid = jsonPlayBack.optInt("createuserid");
					playback.title = jsonPlayBack.optString("livevideotitle");
					playback.httpUrl = jsonPlayBack.optString("livevideopath");
					playback.startTime = jsonPlayBack.optInt("starttime");
					playback.duration = jsonPlayBack.optString("duration");
					playback.livevideopwd = jsonPlayBack.optString("livevideopwd");					
					playback.livevideoico = jsonPlayBack.optString("livevideoico");
					playback.livevideoid = jsonPlayBack.optInt("livevideoid");
					playback.newStartTime = jsonPlayBack.optString("newstarttime");

					// 1:删锟斤拷
					mInfosPlay.add(playback);
					// 锟斤拷锟斤拷映锟戒，锟斤拷锟矫伙拷锟斤拷拥锟絤eetinginfo锟斤拷使锟斤拷
					mMap.put(playback.mId, playback);
				}
			}
			MessagesController.getInstance().processPlayBack(mInfosPlay, 0);


			return true;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void CreateMeeting(final TLObject req,
			final RPCRequest.RPCRequestDelegate completionBlock) {
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				if (!isNetworkOnline()) {
					TLRPC.TL_error networkerror = new TLRPC.TL_error();
					networkerror.code = -1;
					completionBlock.run(null, networkerror);
					return;
				}
				if (client == null)
					return;
				try {
					RequestParams params = new RequestParams();
					// 锟斤拷锟斤拷锟斤拷锟斤拷
					// 锟斤拷取锟斤拷锟斤拷锟斤拷息
					TLRPC.TL_MeetingInfo info = (TLRPC.TL_MeetingInfo) req;

					JSONObject jo = new JSONObject();
					jo.put("act", 1);
					jo.put("meetingname", info.topic);
					jo.put("meetinginfo", info.topic);
					jo.put("userid", UserConfig.clientUserId);
					jo.put("starttime", info.startTime);
					jo.put("endtime", info.endTime);
					jo.put("chairmanpwd", info.chairmanpwd);
					if(info.confuserpwd != null && !info.confuserpwd.equals("") || info.confuserpwd.length()>0)
						jo.put("passwordrequired",1);	
					if(info.confuserpwd != null && !info.confuserpwd.equals("") || info.confuserpwd.length()>0)
						jo.put("passwordrequired",1);
					jo.put("confuserpwd", info.confuserpwd);
					jo.put("sidelineuserpwd",info.sidelineuserpwd);
					jo.put("version", versionNUM);
					//qxm add
					jo.put("meetingtype", info.meetingType);
					jo.put("duration", info.duration);
					jo.put("begintime", info.beginTime);
					jo.put("visiblemeeting", info.ispublicMeeting);
					jo.put("meetingsubject", info.meetingsubject);
					JSONArray jsonArr = new JSONArray();
					Iterator<Integer> it = info.participants.iterator();
					while (it.hasNext()) {
						jsonArr.put(it.next());
					}
					jo.put("meetinguser", jsonArr);
					params.put("param", jo.toString());

					client.post(Config.webFun_controlmeeting, params,
							new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String response) {
							final String res = response;
							Utilities.stageQueue
							.postRunnable(new Runnable() {
								@Override
								public void run() {
									try {
										JSONTokener jsonParser = new JSONTokener(res);
										JSONObject ret = (JSONObject) jsonParser.nextValue();

										if (ret.has("result") && ret.getInt("result") == -2) {
											// 实锟斤拷锟斤拷注锟斤拷锟剿ｏ拷锟斤拷示session锟斤拷锟斤拷锟斤拷,lhy
											// todo,锟斤拷锟叫猴拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷result
											reportNetworkStatus(100);
											return;
										}
										parseUpdateInfo(ret,
												false);

										TLRPC.TL_UpdateMeetingResult res = new TLRPC.TL_UpdateMeetingResult();
										int result = parseUpdateMeetingResult(
												ret, res);
										if (result > 0) {
											// 通知锟斤拷锟斤拷锟竭ｏ拷锟斤拷锟铰伙拷锟斤拷晒锟�
											completionBlock
											.run(res,
													null);
										} else {
											TLRPC.TL_error error = new TLRPC.TL_error();
											error.code = 1;
											completionBlock
											.run(null,error);
										}

									} catch (Exception e) {
										e.printStackTrace();
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = -1;
										completionBlock.run(
												null,
												networkerror);
									}
								}
							});
						}

						@Override
						public void onFailure(Throwable error,
								String content) {
							Utilities.stageQueue
							.postRunnable(new Runnable() {
								@Override
								public void run() {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
									completionBlock.run(null,
											networkerror);
								}
							});
						}
					});
				} catch (Exception e) {
					TLRPC.TL_error networkerror = new TLRPC.TL_error();
					networkerror.code = -1;
					completionBlock.run(null, networkerror);
				}
			}
		});
	}

	public void deleteMeeting(final int mid,
			final RPCRequest.RPCRequestDelegate completionBlock) {
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				if (!isNetworkOnline()) {
					TLRPC.TL_error networkerror = new TLRPC.TL_error();
					networkerror.code = -1;
					completionBlock.run(null, networkerror);
					return;
				}

				if (client == null)
					return;

				try {
					RequestParams params = new RequestParams();
					JSONObject jo = new JSONObject();
					// 删锟斤拷锟斤拷锟斤拷
					jo.put("act", 3);
					jo.put("serial", mid);
					jo.put("userid", UserConfig.clientUserId);
					jo.put("version", versionNUM);

					params.put("param", jo.toString());
					client.post(Config.webFun_controlmeeting, params,
							new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String response) {
							final String res = response;
							Utilities.stageQueue
							.postRunnable(new Runnable() {
								@Override
								public void run() {
									try {
										JSONTokener jsonParser = new JSONTokener(
												res);
										JSONObject ret = (JSONObject) jsonParser
												.nextValue();
										if (ret.has("result")
												&& ret.getInt("result") == -2) {
											// 实锟斤拷锟斤拷注锟斤拷锟剿ｏ拷锟斤拷示session锟斤拷锟斤拷锟斤拷,lhy
											// todo,锟斤拷锟叫猴拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷result
											reportNetworkStatus(100);
											return;
										}
										parseUpdateInfo(ret,
												false);

										TLRPC.TL_UpdateMeetingResult res = new TLRPC.TL_UpdateMeetingResult();
										int result = parseUpdateMeetingResult(
												ret, res);
										if (result > 0) {
											// 通知锟斤拷锟斤拷锟竭ｏ拷锟斤拷锟铰伙拷锟斤拷晒锟�
											completionBlock
											.run(null,
													null);
										} else {
											TLRPC.TL_error error = new TLRPC.TL_error();
											error.code = 1;
											completionBlock
											.run(null,
													error);
										}

									} catch (Exception e) {
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = -1;
										completionBlock.run(
												null,
												networkerror);
										return;
									}
								}
							});
						}

						@Override
						public void onFailure(Throwable error,
								String content) {
							Utilities.stageQueue
							.postRunnable(new Runnable() {
								@Override
								public void run() {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
									completionBlock.run(null,
											networkerror);
								}
							});
						}
					});
				} catch (Exception e) {
					TLRPC.TL_error networkerror = new TLRPC.TL_error();
					networkerror.code = -1;
					completionBlock.run(null, networkerror);
					return;
				}
			}
		});
	}

	public void updateMeeting(final TLObject req,
			final RPCRequest.RPCRequestDelegate completionBlock) {
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				if (!isNetworkOnline()) {
					TLRPC.TL_error networkerror = new TLRPC.TL_error();
					networkerror.code = -1;
					completionBlock.run(null, networkerror);
					return;
				}

				if (client == null)
					return;

				try {
					RequestParams params = new RequestParams();
					JSONObject jo = new JSONObject();
					TLRPC.TL_MeetingInfo info = (TLRPC.TL_MeetingInfo) req;
					jo.put("act", 2);
					jo.put("serial", info.mid);
					jo.put("userid", UserConfig.clientUserId);
					// 目前只锟斤拷锟斤拷锟睫改匡拷始时锟斤拷突锟斤拷锟斤拷锟斤拷锟�
					jo.put("starttime", info.startTime);
					jo.put("meetingname", info.topic);
					jo.put("version", versionNUM);
					params.put("param", jo.toString());
					// for usercompany
					jo.put("versiontype", 1);

					client.post(Config.webFun_controlmeeting, params,
							new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String response) {
							final String res = response;
							Utilities.stageQueue
							.postRunnable(new Runnable() {
								@Override
								public void run() {
									try {
										JSONTokener jsonParser = new JSONTokener(
												res);
										JSONObject ret = (JSONObject) jsonParser
												.nextValue();
										if (ret.has("result")
												&& ret.getInt("result") == -2) {
											// 实锟斤拷锟斤拷注锟斤拷锟剿ｏ拷锟斤拷示session锟斤拷锟斤拷锟斤拷,lhy
											// todo,锟斤拷锟叫猴拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷result
											reportNetworkStatus(100);
											return;
										}
										parseUpdateInfo(ret,
												false);

										TLRPC.TL_UpdateMeetingResult res = new TLRPC.TL_UpdateMeetingResult();
										int result = parseUpdateMeetingResult(
												ret, res);
										if (result > 0) {
											// 通知锟斤拷锟斤拷锟竭ｏ拷锟斤拷锟铰伙拷锟斤拷晒锟�
											completionBlock
											.run(null,
													null);
										} else {
											TLRPC.TL_error error = new TLRPC.TL_error();
											error.code = 1;
											completionBlock
											.run(null,
													error);
										}

									} catch (Exception e) {
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = -1;
										completionBlock.run(
												null,
												networkerror);
										return;
									}
								}
							});
						}

						@Override
						public void onFailure(Throwable error,
								String content) {
							Utilities.stageQueue
							.postRunnable(new Runnable() {
								@Override
								public void run() {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
									completionBlock.run(null,
											networkerror);
								}
							});
						}
					});
				} catch (Exception e) {
					TLRPC.TL_error networkerror = new TLRPC.TL_error();
					networkerror.code = -1;
					completionBlock.run(null, networkerror);
					return;
				}
			}
		});
	}

	public void acceptMeeting(final TLObject req, final int accetpid,
			final RPCRequest.RPCRequestDelegate completionBlock) {
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				if (!isNetworkOnline()) {
					return;
				}

				if (client == null)
					return;

				try {
					RequestParams params = new RequestParams();
					TLRPC.TL_MeetingInfo info = (TLRPC.TL_MeetingInfo) req;
					JSONObject jo = new JSONObject();
					jo.put("act", 4);
					jo.put("serial", info.mid);
					jo.put("userid", accetpid);
					jo.put("version", versionNUM);
					// for usercompany
					jo.put("versiontype", 1);

					params.put("param", jo.toString());

					client.post(Config.webFun_controlmeeting, params,
							new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String response) {
							final String res = response;
							Utilities.stageQueue
							.postRunnable(new Runnable() {
								@Override
								public void run() {
									try {
										JSONTokener jsonParser = new JSONTokener(
												res);
										JSONObject ret = (JSONObject) jsonParser
												.nextValue();
										if (ret.has("result")
												&& ret.getInt("result") == -2) {
											// 实锟斤拷锟斤拷注锟斤拷锟剿ｏ拷锟斤拷示session锟斤拷锟斤拷锟斤拷,lhy
											// todo,锟斤拷锟叫猴拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷result
											reportNetworkStatus(100);
											return;
										}
										parseUpdateInfo(ret,
												false);

										TLRPC.TL_UpdateMeetingResult res = new TLRPC.TL_UpdateMeetingResult();
										int result = parseUpdateMeetingResult(
												ret, res);
										if (result > 0) {
											// 通知锟斤拷锟斤拷锟竭ｏ拷锟斤拷锟铰伙拷锟斤拷晒锟�
											completionBlock
											.run(null,
													null);
										} else {
											TLRPC.TL_error error = new TLRPC.TL_error();
											error.code = 1;
											completionBlock
											.run(null,
													error);
										}

									} catch (Exception e) {
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = -1;
										completionBlock.run(
												null,
												networkerror);
										return;
									}
								}
							});
						}

						@Override
						public void onFailure(Throwable error,
								String content) {
							Utilities.stageQueue
							.postRunnable(new Runnable() {
								@Override
								public void run() {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
									completionBlock.run(null,
											networkerror);
								}
							});
						}
					});
				} catch (Exception e) {
					TLRPC.TL_error networkerror = new TLRPC.TL_error();
					networkerror.code = -1;
					completionBlock.run(null, networkerror);
					return;
				}
			}
		});
	}

	public void changeMeeting(final TLObject req,
			final RPCRequest.RPCRequestDelegate completionBlock) {
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				if (!isNetworkOnline()) {
					TLRPC.TL_error networkerror = new TLRPC.TL_error();
					networkerror.code = -1;
					completionBlock.run(null, networkerror);
					return;
				}

				if (client == null)
					return;

				try {
					TLRPC.TL_MeetingChange mc = (TLRPC.TL_MeetingChange) req;
					RequestParams params = new RequestParams();
					JSONObject jo = new JSONObject();
					// 删锟斤拷锟斤拷锟斤拷
					jo.put("act", mc.action);
					jo.put("serial", mc.mid);
					jo.put("userid", UserConfig.clientUserId);
					if (mc.action == 6)
						jo.put("version", versionNUM);
					JSONArray meetinguser = new JSONArray();
					for (int i = 0; i < mc.users.size(); i++)
						meetinguser.put(mc.users.get(i));
					jo.put("meetinguser", meetinguser);
					// for usercompany
					jo.put("versiontype", 1);

					params.put("param", jo.toString());
					client.post(Config.webFun_controlmeeting, params,
							new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String response) {
							final String res = response;
							Utilities.stageQueue
							.postRunnable(new Runnable() {
								@Override
								public void run() {
									try {
										JSONTokener jsonParser = new JSONTokener(
												res);
										JSONObject ret = (JSONObject) jsonParser
												.nextValue();
										if (ret.has("result")
												&& ret.getInt("result") == -2) {
											// 实锟斤拷锟斤拷注锟斤拷锟剿ｏ拷锟斤拷示session锟斤拷锟斤拷锟斤拷,lhy
											// todo,锟斤拷锟叫猴拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷result
											reportNetworkStatus(100);
											return;
										}
										parseUpdateInfo(ret,
												false);

										TLRPC.TL_UpdateMeetingResult res = new TLRPC.TL_UpdateMeetingResult();
										int result = parseUpdateMeetingResult(
												ret, res);
										if (result > 0) {
											// 通知锟斤拷锟斤拷锟竭ｏ拷锟斤拷锟铰伙拷锟斤拷晒锟�
											completionBlock
											.run(null,
													null);
										} else {
											TLRPC.TL_error error = new TLRPC.TL_error();
											error.code = 1;
											completionBlock
											.run(null,
													error);
										}

									} catch (Exception e) {
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = -1;
										completionBlock.run(
												null,
												networkerror);
										return;
									}
								}
							});
						}

						@Override
						public void onFailure(Throwable error,
								String content) {
							Utilities.stageQueue
							.postRunnable(new Runnable() {
								@Override
								public void run() {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
									completionBlock.run(null,
											networkerror);
								}
							});
						}
					});
				} catch (Exception e) {
					TLRPC.TL_error networkerror = new TLRPC.TL_error();
					networkerror.code = -1;
					completionBlock.run(null, networkerror);
					return;
				}
			}
		});
	}

	private void parseUpdateInfo(final JSONObject jo, final boolean active) {
		// Utilities.stageQueue.postRunnable(new Runnable() {
		// @Override
		// public void run()
		// {
		try {
			if (jo.has("result")) {
				if (jo.getInt("result") == 1) {
					parseChannalInfo(jo);
				}
			}
			if (jo.has("result") && jo.getInt("result") == 0) {
				FileLog.d("emm", "parse update start");
				if (!UserConfig.isPersonalVersion)
					parseEnterpriseBook(jo);
				else {
					TLRPC.TL_InstallSoftwareContacts res = new TLRPC.TL_InstallSoftwareContacts();
					parseContacts(jo, res);
					MessagesController.getInstance().processPersonalContacts(
							res);
				}
 				parseMeetingBook(jo);

				//only test   qxm add
				//				ArrayList<TLRPC.TL_DirectPlayBackList> infos = new ArrayList<TLRPC.TL_DirectPlayBackList>();
				//				TLRPC.TL_DirectPlayBackList info = new TLRPC.TL_DirectPlayBackList();
				//				info.httpUrl = "http://192.168.0.99/Public/img/logo.jpg";
				//				infos.add(info);
				//				MessagesController.getInstance().processPlayBack(infos, 0);
				// parseJoinCompanyInfo(jo);
				if (!active) {
					// 锟介被锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
					parseGetGroupResultInner(jo);
				} else {
					// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷原锟斤拷锟斤拷锟斤拷锟斤拷
				}
				parseChannalInfo(jo);
				if (!UserConfig.isPersonalVersion) {
					parseRemarkUser(jo);
					parseUserCompanyInfo(jo);// 锟斤拷证顺锟斤拷
				}
				FileLog.d("emm", "parse update end");
			} else {
				if (!UserConfig.isPersonalVersion)
					MessagesController.getInstance().processPersonalContacts(
							null);
				else
					MessagesController.getInstance().processLoadedUserCompany(
							null, 0);

				MessagesController.getInstance().processLoadMeetings(null, 0);
			}

			// 锟斤拷锟斤拷息锟斤拷锟叫伙拷取锟角凤拷锟斤拷没锟叫达拷锟斤拷锟斤拷锟斤拷锟斤拷息,锟斤拷为锟街伙拷锟斤拷锟斤拷锟斤拷锟斤拷锟截伙拷锟斤拷,锟斤拷锟斤拷锟剿凤拷锟斤拷锟斤拷息锟斤拷锟杰碉拷锟铰ｏ拷锟揭憋拷锟斤拷锟窖撅拷没锟斤拷锟斤拷锟较拷锟�
			// 锟斤拷锟斤拷锟饺斤拷锟斤拷息锟芥储锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷取锟斤拷锟斤拷锟较拷锟斤拷锟斤拷陆锟斤拷锟斤拷锟斤拷锟斤拷椋拷俅锟斤拷锟斤拷锟斤拷锟斤拷息
			synchronized (this) {
				for (int k = 0, len = updatesQueue.size(); k < len; k++) {
					TLRPC.Updates delayupdates = updatesQueue.get(k);
					MessagesController.getInstance().processUpdates(
							(TLRPC.Updates) delayupdates, false);
					updatesQueue.remove(k);// 锟斤拷态删锟斤拷一锟斤拷元锟斤拷
					--len;// 锟斤拷锟斤拷一锟斤拷
				}
			}
			if (jo.has("version")) {
				// 锟芥储锟芥本
				int version = jo.getInt("version");
				MessagesStorage.getInstance().setVersion(version);
				setVersion(version);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// }
		// });
	}


	public class AsyncHandler extends AsyncHttpResponseHandler {

		public RequestHandle innerRequest = null;

		@Override
		public void onSuccess(String response) {
			// FileLog.d("emm", response);
			final String s = response;
			Log.i("emm", "response=="+response);
			// FileLog.e("emm", s);
			Utilities.stageQueue.postRunnable(new Runnable() {
				@Override
				public void run() {
					if (innerRequest != request)
						return;
					request = null;
					try {
						JSONTokener jsonParser = new JSONTokener(s);
						Log.i("TAG", s);
						JSONObject ret = (JSONObject) jsonParser.nextValue();
						if (ret.has("result")) {
							if (ret.getInt("result") == -2)// ret.getInt("result")==-2
							{
								// 实锟斤拷锟斤拷注锟斤拷锟剿ｏ拷锟斤拷示session锟斤拷锟斤拷锟斤拷,lhy todo,锟斤拷锟叫猴拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷result
								FileLog.e("emm",
										"web session expired*******************");
								reportNetworkStatus(100);
								return;
							}
						}
						parseUpdateInfo(ret, false);

					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
				}
			});

		}

		@Override
		public void onFailure(Throwable error, String content) {
			MessagesController.getInstance().processLoadedUserCompany(null, 0);
			error.printStackTrace();
		}
	}

	public void getUpdate() {
		if (!isNetworkOnline()) {
			if (UserConfig.isPersonalVersion)
				MessagesController.getInstance().processPersonalContacts(null);
			else
				MessagesController.getInstance().processLoadedUserCompany(null,
						0);
			// 为锟斤拷锟揭的伙拷锟斤拷刷锟斤拷锟叫憋拷锟斤拷示锟斤拷锟斤拷
			Utilities.RunOnUIThread(new Runnable() {
				@Override
				public void run() {
					NotificationCenter.getInstance().postNotificationName(
							MessagesController.getall_meeting);
				}
			});

			return;
		}

		if (client != null) {
			try {
				if (UserConfig.bFirstUseNewVersion)
					lastVersion = versionNUM;
				else {
					UserConfig.bFirstUseNewVersion = true;
					UserConfig.saveConfig(false);
					versionNUM = -1;
				}
				String httpUrl = Config.webFun_getUpdate + "/userid/"
						+ UserConfig.clientUserId + "/version/" + versionNUM
						+ "/versiontype/1";
				FileLog.e("emm", "connectionMgr getupdate version:"
						+ versionNUM + httpUrl);

				if (request != null)
					request.cancel(true);

				AsyncHandler handler = new AsyncHandler();
				request = client.get(httpUrl, handler);
				handler.innerRequest = request;

				// updateLocations();

			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}

	public void setVersion(int version) {
		versionNUM = version;
	}

	public class getTimeAsyncHandler extends AsyncHttpResponseHandler {
		@Override
		public void onSuccess(String response) {
			final String s = response;
			Utilities.stageQueue.postRunnable(new Runnable() {
				@Override
				public void run() {
					try {
						JSONTokener jsonParser = new JSONTokener(s);
						JSONObject jo = (JSONObject) jsonParser.nextValue();
						if (!jo.has("result"))
							return;
						if (jo.getInt("result") == -2) {
							// 实锟斤拷锟斤拷注锟斤拷锟剿ｏ拷锟斤拷示session锟斤拷锟斤拷锟斤拷,lhy todo,锟斤拷锟叫猴拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷result
							reportNetworkStatus(100);
							return;
						}
						if (!jo.has("time"))
							return;

						// int pingTime = (int)(System.currentTimeMillis() /
						// 1000) - startTime;
						// if (Math.abs(pingTime) < 20) {
						// currentPingTime = (pingTime + currentPingTime) / 2;
						long timeMessage = jo.getLong("time");
						long currentTime = System.currentTimeMillis() / 1000;
						// timeDifference = (int)((timeMessage - currentTime) -
						// currentPingTime / 2.0);
						timeDifference = (int) (timeMessage - currentTime);
						saveSession();
						// }
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

		}

		@Override
		public void onFailure(Throwable error, String content) {
			error.printStackTrace();
		}
	}

	public void gettime() {
		if (!isNetworkOnline()) {
			return;
		}

		try {
			getTimeAsyncHandler handler = new getTimeAsyncHandler();
			client.get(Config.webFun_gettime, handler);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void uploadContacts(final ArrayList<String> contacts,
			final RPCRequest.RPCRequestDelegate completionBlock) {
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				if (!isNetworkOnline()) {
					TLRPC.TL_error networkerror = new TLRPC.TL_error();
					networkerror.code = -1;
					completionBlock.run(null, networkerror);
					return;
				}

				if (client == null)
					return;

				try {
					RequestParams params = new RequestParams();
					JSONObject jo = new JSONObject();
					jo.put("userid", UserConfig.clientUserId);
					jo.put("version", versionNUM);
					// jo.put("version", -1);
					JSONArray contactsJSON = new JSONArray();
					for (int i = 0; i < contacts.size(); i++)
						contactsJSON.put(contacts.get(i));
					jo.put("mobilearr", contactsJSON);
					String sParam = jo.toString();
					params.put("param", sParam);
					client.post(Config.webFun_getusercontacts, params,
							new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String response) {
							final String http_res = response;
							Log.d("emm", "contacts=========00===" + response);
							Utilities.stageQueue
							.postRunnable(new Runnable() {
								@Override
								public void run() {
									try {
										JSONTokener jsonParser = new JSONTokener(
												http_res);
										JSONObject ret = (JSONObject) jsonParser
												.nextValue();
										if (ret.has("result")
												&& ret.getInt("result") == -2) {
											// 实锟斤拷锟斤拷注锟斤拷锟剿ｏ拷锟斤拷示session锟斤拷锟斤拷锟斤拷,lhy
											// todo,锟斤拷锟叫猴拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷result
											reportNetworkStatus(100);
											return;
										}

										TLRPC.TL_InstallSoftwareContacts res = new TLRPC.TL_InstallSoftwareContacts();
										int result = parseContacts(
												ret, res);
										if (result == 0) {
											// 通知锟斤拷锟斤拷锟竭ｏ拷锟斤拷锟铰伙拷锟斤拷晒锟�
											completionBlock
											.run(res,
													null);
										} else if (result <= 1) {
											TLRPC.TL_error error = new TLRPC.TL_error();
											error.code = 1;
											completionBlock
											.run(null,
													error);
										}

									} catch (Exception e) {
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = -1;
										completionBlock.run(
												null,
												networkerror);
										return;
									}
								}
							});
						}

						@Override
						public void onFailure(Throwable error,
								String content) {
							Utilities.stageQueue
							.postRunnable(new Runnable() {
								@Override
								public void run() {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
									completionBlock.run(null,
											networkerror);
								}
							});
						}
					});
				} catch (Exception e) {
					TLRPC.TL_error networkerror = new TLRPC.TL_error();
					networkerror.code = -1;
					completionBlock.run(null, networkerror);
					return;
				}
			}
		});
	}

	public int parseContacts(JSONObject json,
			TLRPC.TL_InstallSoftwareContacts res) {
		int result = -1;
		try {
			if (!json.has("result"))
				return result;
			result = json.getInt("result");
			if (result != 0)
				return result;
			if (!json.has("usercontact"))
				return result;

			JSONArray jsonArrUser = json.optJSONArray("usercontact");
			ArrayList<TLRPC.User> userArr = new ArrayList<TLRPC.User>();
			for (int j = 0; j < jsonArrUser.length(); ++j) {
				// 锟矫伙拷锟斤拷一锟斤拷array 锟斤拷锟斤拷锟斤拷锟斤拷锟矫伙拷锟斤拷锟捷硷拷录
				JSONObject jo = (JSONObject) jsonArrUser.getJSONObject(j);
				TLRPC.TL_userContact user = new TLRPC.TL_userContact();
				user.id = jo.getInt("userid");
				user.phone = jo.getString("mobile");
				user.email = jo.getString("email");
				user.nickname = jo.getString("nickname");
				user.first_name = jo.getString("firstname");
				user.last_name = jo.getString("lastname");
				user.gender = jo.getInt("gender");
				user.userico = jo.getString("userico");
				user.serverid = 0;
				// user.serverid = jo.optInt("serverid");
				// user.sortlevel = jo.optInt("sortlevel");
				user.description = jo.getString("description");
				// add by xueqiang
				user.identification = jo.getString("identification");
				buildPhoto(user);
				// status 为 0锟斤拷示未锟斤拷锟筋，为1锟斤拷示锟斤拷锟斤拷锟斤拷锟轿�2锟斤拷示锟窖撅拷删锟斤拷
				// 锟斤拷锟斤拷锟接碉拷锟斤拷业锟矫伙拷直锟接放碉拷DB锟斤拷锟节达拷锟叫既匡拷
				TLRPC.TL_userStatusEmpty userStatus = new TLRPC.TL_userStatusEmpty();
				userStatus.expires = jo.optInt("state", 1);
				user.status = userStatus;
				// FileLog.e("emm", "parsecontacts="+user.status.expires);
				// user.version = jo.getInt("version");
				// user.companyid = jo.getInt("companyid");
				// user.deptid = jo.getInt("deptid");
				// user.userRoleID = jo.getInt("userroleid");
				res.users.add(user);
				// mobile锟斤拷MD5锟侥硷拷锟杰达拷
				String mobile = jo.getString("md5mobile");
				res.md5PhoneMap.put(mobile, user.id);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public void ControlCompany(final TLObject req,
			final RPCRequest.RPCRequestDelegate completionBlock) {
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				if (!isNetworkOnline()) {
					TLRPC.TL_error networkerror = new TLRPC.TL_error();
					networkerror.code = -1;
					completionBlock.run(null, networkerror);
					return;
				}
				if (client == null)
					return;
				try {
					RequestParams params = new RequestParams();
					// 锟斤拷锟斤拷锟斤拷锟斤拷
					// 锟斤拷取锟斤拷锟斤拷锟斤拷息
					// act:1锟斤拷示锟斤拷锟斤拷锟斤拷司 2:锟睫改癸拷司 3:删锟斤拷锟斤拷司 4:锟斤拷锟接癸拷司锟斤拷员 5:删锟斤拷锟斤拷司锟斤拷员
					TLRPC.TL_CompanyInfo info = (TLRPC.TL_CompanyInfo) req;

					JSONObject jo = new JSONObject();

					final int act = info.act;
					jo.put("act", act);
					if (act == 1) {
						jo.put("companyname", info.name);
						jo.put("userid", info.createrid);
						jo.put("version", versionNUM);
						JSONArray jsonArr = new JSONArray();
						for (int i = 0; i < info.users.size(); i++) {
							TLRPC.User user = info.users.get(i);
							JSONObject jUser = new JSONObject();
							jUser.put("userid", user.id);
							jUser.put("mobile", user.phone);
							jUser.put("firstname", user.first_name);
							jUser.put("lastname", "");
							jsonArr.put(jUser);
						}
						jo.put("userarr", jsonArr);
					} else if (act == 2) {
						jo.put("companyid", info.companyid);
						jo.put("companyname", info.name);
						jo.put("userid", info.createrid);
						jo.put("version", versionNUM);

						if (info.clearico) {
							jo.put("companyico", "");
						}
					} else if (act == 3) {
						jo.put("companyid", info.companyid);
						jo.put("companyname", info.name);
						jo.put("userid", info.createrid);
						jo.put("version", versionNUM);
					} else if (act == 4) {
						jo.put("companyid", info.companyid);
						jo.put("userid", info.createrid);
						jo.put("version", versionNUM);
						JSONArray jsonArr = new JSONArray();
						int size = info.addusers.size();
						for (int i = 0; i < size; i++) {
							TLRPC.User user = info.addusers.get(i);
							JSONObject jUser = new JSONObject();
							jUser.put("userid", user.id);
							jUser.put("mobile", user.phone); // user.first_name
							// 锟斤拷 说锟斤拷锟斤拷锟街讹拷锟斤拷拥某锟皆�
							// componeyUserName
							// 锟斤拷为锟斤拷锟斤拷锟斤拷锟斤拷
							jUser.put("firstname", user.first_name);
							jUser.put("lastname", "");
							jsonArr.put(jUser);
						}
						jo.put("userarr", jsonArr);
					} else if (act == 5) {
						jo.put("companyid", info.companyid);
						// if( info.createrid != UserConfig.clientUserId )
						// FileLog.e("emm", "delete company user is not me");
						jo.put("userid", info.createrid);
						jo.put("version", versionNUM);
						JSONArray jsonArr = new JSONArray();
						int size = info.delusers.size();
						for (int i = 0; i < size; i++) {
							TLRPC.User user = info.delusers.get(i);
							JSONObject jUser = new JSONObject();
							jUser.put("userid", user.id);
							jsonArr.put(jUser);
						}
						jo.put("userarr", jsonArr);
					} else if (act == 6 || act == 7) {
						// 锟斤拷示同锟斤拷锟斤拷牍撅拷锟斤拷遣锟酵拷锟斤拷锟诫公司,6锟斤拷示同锟解，7锟斤拷示锟杰撅拷
						jo.put("companyid", info.companyid);
						jo.put("userid", info.createrid);
						jo.put("version", versionNUM);
					} else if (act == 8) {
						jo.put("companyid", info.companyid);
						jo.put("userid", UserConfig.clientUserId);
						int size = info.users.size();
						for (int i = 0; i < size; i++) {
							TLRPC.User user = info.users.get(i);
							jo.put("repairid", user.id);
							jo.put("firstname", user.first_name);
							jo.put("lastname", user.last_name);
						}

						jo.put("version", versionNUM);
					}
					// for usercompany
					jo.put("versiontype", 1);
					String s = jo.toString();
					params.put("param", jo.toString());
					FileLog.e("emm", jo.toString());

					client.post(Config.webFun_controlcompany, params,
							new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String response) {
							final String res = response;
							Utilities.stageQueue
							.postRunnable(new Runnable() {
								@Override
								public void run() {
									try {
										JSONTokener jsonParser = new JSONTokener(
												res);
										JSONObject ret = (JSONObject) jsonParser
												.nextValue();
										int result = -1;
										if (ret.has("result")) {
											result = ret
													.getInt("result");
										}

										if (result == -2) {
											// 实锟斤拷锟斤拷注锟斤拷锟剿ｏ拷锟斤拷示session锟斤拷锟斤拷锟斤拷,lhy
											// todo,锟斤拷锟叫猴拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷result
											reportNetworkStatus(100);
											return;
										}
										parseUpdateInfo(ret,
												false);

										if (result >= 0
												&& result < 2) {
											// 通知锟斤拷锟斤拷锟竭ｏ拷锟斤拷锟铰伙拷锟斤拷晒锟�
											if (act == 1
													&& ret.has("companyid")) {
												int companyid = ret
														.getInt("companyid");
												TLRPC.TL_CompanyShortInfo info = new TLRPC.TL_CompanyShortInfo();
												info.companyid = companyid;
												completionBlock
												.run(info,
														null);
											} else
												completionBlock
												.run(null,
														null);

										} else {
											TLRPC.TL_error error = new TLRPC.TL_error();
											error.code = result;
											completionBlock
											.run(null,
													error);
										}

									} catch (Exception e) {
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = -1;
										completionBlock.run(
												null,
												networkerror);
									}
								}
							});
						}

						@Override
						public void onFailure(Throwable error,
								String content) {
							Utilities.stageQueue
							.postRunnable(new Runnable() {
								@Override
								public void run() {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
									completionBlock.run(null,
											networkerror);
								}
							});
						}
					});
				} catch (Exception e) {
					TLRPC.TL_error networkerror = new TLRPC.TL_error();
					networkerror.code = -1;
					completionBlock.run(null, networkerror);
				}
			}
		});
	}

	public void bindAccount(final int userid, final String account,
			final int bindType, final String code, final int bForce,
			final RPCRequest.RPCRequestDelegate completionBlock) {
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				if (!isNetworkOnline()) {
					TLRPC.TL_error networkerror = new TLRPC.TL_error();
					networkerror.code = -1;
					completionBlock.run(null, networkerror);
					return;
				}

				if (client == null)
					return;

				try {
					RequestParams params = new RequestParams();
					params.put("userid", UserConfig.clientUserId + "");
					params.put("account", account);
					params.put("bindtype", bindType + "");
					params.put("verif", code);
					params.put("force", bForce + "");
					// 锟斤拷锟斤拷锟斤拷屎锟揭惨拷锟斤拷锟揭伙拷锟�
					AccountManager accountManager = AccountManager
							.get(ApplicationLoader.applicationContext);
					if (accountManager != null) {
						Account[] accounts = accountManager
								.getAccountsByType("info.emm.weiyicloud.account");
						Account myAccount = null;
						for (Account acc : accounts) {
							if (acc.name.equalsIgnoreCase(UserConfig.account)) {
								myAccount = acc;
								break;
							}
						}
						if (myAccount != null) {
							String pwd = accountManager.getPassword(myAccount);
							String tempAccount = account;

							String encPwd = Utilities.MD5(pwd);
							String encAccount = Utilities.MD5(tempAccount);

							String newPwd = Utilities.MD5(encPwd + encAccount);
							params.put("userpwd", newPwd);
						}
					}

					client.post(Config.webFun_bindaccount, params,
							new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String response) {
							final String http_res = response;
							Utilities.stageQueue
							.postRunnable(new Runnable() {
								@Override
								public void run() {
									try {
										JSONTokener jsonParser = new JSONTokener(
												http_res);
										JSONObject ret = (JSONObject) jsonParser
												.nextValue();
										if (ret.has("result")
												&& ret.getInt("result") == -2) {
											// 实锟斤拷锟斤拷注锟斤拷锟剿ｏ拷锟斤拷示session锟斤拷锟斤拷锟斤拷,lhy
											// todo,锟斤拷锟叫猴拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷result
											reportNetworkStatus(100);
											return;
										}
										int result = 0;
										if (ret.has("result"))
											result = ret
											.getInt("result");
										if (result >= 0) {
											// 通知锟斤拷锟斤拷锟竭ｏ拷锟襟定成癸拷
											// 锟斤拷锟斤拷锟斤拷蠖ǖ锟斤拷屎锟斤拷丫锟斤拷锟斤拷锟�,锟斤拷要锟斤拷示UI锟斤拷锟角凤拷锟斤拷锟斤拷蠖ǎ锟斤拷锟斤拷锟斤拷原锟斤拷锟绞号碉拷锟斤拷息锟斤拷锟结丢失
											TLRPC.TL_BindResult bindResult = new TLRPC.TL_BindResult();
											bindResult.result = result;
											completionBlock
											.run(bindResult,
													null);
										} else if (result < 0) {
											TLRPC.TL_error error = new TLRPC.TL_error();
											error.code = 1;
											completionBlock
											.run(null,
													error);
										}

									} catch (Exception e) {
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = -1;
										completionBlock.run(
												null,
												networkerror);
										return;
									}
								}
							});
						}

						@Override
						public void onFailure(Throwable error,
								String content) {
							Utilities.stageQueue
							.postRunnable(new Runnable() {
								@Override
								public void run() {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
									completionBlock.run(null,
											networkerror);
								}
							});
						}
					});
				} catch (Exception e) {
					TLRPC.TL_error networkerror = new TLRPC.TL_error();
					networkerror.code = -1;
					completionBlock.run(null, networkerror);
					return;
				}
			}
		});
	}

	public void startUpdateTimer() {
		serviceTimer = new Timer();
		serviceTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						getUpdate();
						updateLocations();
					}
				});
			}
		}, 3600 * 1000, 3600 * 1000);
	}

	public void stopUpdateTimer() {
		serviceTimer.cancel();
	}

	// ///////////add by 锟斤拷锟睫憋拷
	// PSTN锟界话锟斤拷锟斤拷
	public void ControlPSTNmeeting(final int nAction,
			final TLRPC.TL_PSTNMeeting PSTNm,
			final RPCRequest.RPCRequestDelegate completionBlock) {
		if (client == null)
			return;

		String key_act = "act";
		String key_meetTile = "meetTitle";
		String key_meetCall = "meetCall";
		String key_meetuserarr = "meetuserarr";
		String key_meetType = "meetType";
		String key_meetTime = "meetTime";
		String key_meetSmsFlag = "meetSmsFlag";
		String key_meetRecFlag = "meetRecFlag";
		String key_autoCallFlag = "autoCallFlag";
		String key_shutupFlag = "shutupFlag";
		String key_conferenceId = "conferenceId";
		String key_commandNo = "commandNo";
		String key_phoneId = "phoneId";
		if (nAction == 1) {
			try {
				RequestParams params = new RequestParams();
				JSONObject jo = new JSONObject();
				jo.put(key_act, nAction);
				jo.put("userid", UserConfig.clientUserId);
				jo.put(key_meetTile, PSTNm.meettitle);
				jo.put(key_meetCall, PSTNm.meetcall);
				JSONArray jsonArray = new JSONArray();
				for (int i = 0; i < PSTNm.allUserId.size(); i++) {
					TLRPC.User user = MessagesController.getInstance().users
							.get(PSTNm.allUserId.get(i));
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("userid", user.id);
					jsonObject.put("mobile", user.phone);
					jsonObject.put("name", user.last_name + user.first_name);
					jsonArray.put(jsonObject);
				}
				jo.put(key_meetuserarr, jsonArray);
				jo.put(key_meetType, PSTNm.meettype);
				jo.put(key_meetTime, PSTNm.meettime);
				jo.put(key_meetSmsFlag, PSTNm.meetsmsflag);
				jo.put(key_meetRecFlag, PSTNm.meetrecflag);
				jo.put(key_autoCallFlag, PSTNm.Autocallnum);
				jo.put(key_shutupFlag, PSTNm.shutupflag);
				params.put("param", jo.toString());
				client.post(Config.webFun_PSTN_CONTROL, params,
						new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						final String res = response;
						// System.out.println(res);
						Utilities.stageQueue
						.postRunnable(new Runnable() {
							@Override
							public void run() {
								try {
									TLRPC.TL_PSTNResponse response = new TLRPC.TL_PSTNResponse();
									response.nAction = nAction;
									response.PSTNm = PSTNm;
									JSONTokener jsonParser = new JSONTokener(
											res);
									JSONObject ret = (JSONObject) jsonParser
											.nextValue();

									if (ret.has("result")) {
										response.nResult = ret
												.getInt("result");
										if (response.nResult == 0) {
											response.newconferenceId = ret
													.getString("conferenceId");
										}
										completionBlock.run(
												response, null);
									} else {
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = -1;
										completionBlock.run(
												null,
												networkerror);
									}
									return;
								} catch (Exception e) {
									e.printStackTrace();
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
									completionBlock.run(null,
											networkerror);
								}
							}
						});
					}

					public void onFailure(Throwable error,
							String content) {
						TLRPC.TL_error networkerror = new TLRPC.TL_error();
						networkerror.code = -1;
						completionBlock.run(null, networkerror);
					}
				});
			} catch (Exception e) {
				TLRPC.TL_error networkerror = new TLRPC.TL_error();
				networkerror.code = -1;
				completionBlock.run(null, networkerror);
			}
		} else if (nAction == 2) {
			try {
				RequestParams params = new RequestParams();
				JSONObject jo = new JSONObject();
				jo.put(key_act, nAction);
				jo.put(key_meetTile, PSTNm.meettitle);
				jo.put(key_meetCall, PSTNm.meetcall);
				JSONArray jsonArray = new JSONArray();
				for (int i = 0; i < PSTNm.allUserId.size(); i++) {
					TLRPC.User user = MessagesController.getInstance().users
							.get(PSTNm.allUserId.get(i));
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("userid", user.id);
					jsonObject.put("mobile", user.phone);
					jsonObject.put("name", user.last_name + user.first_name);
					jsonArray.put(jsonObject);
				}
				jo.put(key_meetuserarr, jsonArray);
				jo.put(key_meetType, PSTNm.meettype);
				jo.put(key_meetTime, PSTNm.meettime);
				jo.put(key_meetSmsFlag, PSTNm.meetsmsflag);
				jo.put(key_meetRecFlag, PSTNm.meetrecflag);
				jo.put(key_autoCallFlag, PSTNm.Autocallnum);
				jo.put(key_shutupFlag, PSTNm.shutupflag);
				jo.put(key_conferenceId, PSTNm.conferenceId);
				params.put("param", jo.toString());

				client.post(Config.webFun_PSTN_CONTROL, params,
						new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						final String res = response;
						Utilities.stageQueue
						.postRunnable(new Runnable() {
							@Override
							public void run() {
								try {
									TLRPC.TL_PSTNResponse response = new TLRPC.TL_PSTNResponse();
									response.nAction = nAction;
									response.PSTNm = PSTNm;
									JSONTokener jsonParser = new JSONTokener(
											res);
									JSONObject ret = (JSONObject) jsonParser
											.nextValue();

									if (ret.has("result")) {
										response.nResult = ret
												.getInt("result");
										completionBlock.run(
												response, null);
									} else {
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = -1;
										completionBlock.run(
												null,
												networkerror);
									}
									return;
								} catch (Exception e) {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
									completionBlock.run(null,
											networkerror);
								}
							}
						});
					}

					public void onFailure(Throwable error,
							String content) {
						TLRPC.TL_error networkerror = new TLRPC.TL_error();
						networkerror.code = -1;
						completionBlock.run(null, networkerror);
					}
				});
			} catch (Exception e) {
				TLRPC.TL_error networkerror = new TLRPC.TL_error();
				networkerror.code = -1;
				completionBlock.run(null, networkerror);
			}
		} else if (nAction == 3) {
			try {
				RequestParams params = new RequestParams();
				JSONObject jo = new JSONObject();
				jo.put(key_act, nAction);
				jo.put(key_conferenceId, PSTNm.conferenceId);
				params.put("param", jo.toString());

				client.post(Config.webFun_PSTN_CONTROL, params,
						new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						final String res = response;
						Utilities.stageQueue
						.postRunnable(new Runnable() {
							@Override
							public void run() {
								try {
									TLRPC.TL_PSTNResponse response = new TLRPC.TL_PSTNResponse();
									response.nAction = nAction;
									response.PSTNm = PSTNm;
									JSONTokener jsonParser = new JSONTokener(
											res);
									JSONObject ret = (JSONObject) jsonParser
											.nextValue();

									if (ret.has("result")) {
										response.nResult = ret
												.getInt("result");
										completionBlock.run(
												response, null);
									} else {
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = -1;
										completionBlock.run(
												null,
												networkerror);
									}
									return;
								} catch (Exception e) {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
									completionBlock.run(null,
											networkerror);
								}
							}
						});
					}

					public void onFailure(Throwable error,
							String content) {
						TLRPC.TL_error networkerror = new TLRPC.TL_error();
						networkerror.code = -1;
						completionBlock.run(null, networkerror);
					}
				});
			} catch (Exception e) {
				TLRPC.TL_error networkerror = new TLRPC.TL_error();
				networkerror.code = -1;
				completionBlock.run(null, networkerror);
			}
		} else if (nAction == 4) {
			try {
				RequestParams params = new RequestParams();
				JSONObject jo = new JSONObject();
				jo.put(key_act, nAction);
				jo.put(key_conferenceId, PSTNm.conferenceId);
				jo.put(key_commandNo, PSTNm.lastControlCmd);
				jo.put(key_phoneId, PSTNm.lastControlString);
				params.put("param", jo.toString());
				client.post(Config.webFun_PSTN_CONTROL, params,
						new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						final String res = response;
						Utilities.stageQueue
						.postRunnable(new Runnable() {
							@Override
							public void run() {
								try {
									TLRPC.TL_PSTNResponse response = new TLRPC.TL_PSTNResponse();
									response.nAction = nAction;
									response.PSTNm = PSTNm;
									JSONTokener jsonParser = new JSONTokener(
											res);
									JSONObject ret = (JSONObject) jsonParser
											.nextValue();
									if (ret.has("result")) {
										response.nResult = ret
												.getInt("result");
										completionBlock.run(
												response, null);
									} else {
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = -1;
										completionBlock.run(
												null,
												networkerror);
									}
									return;
								} catch (Exception e) {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
									completionBlock.run(null,
											networkerror);
								}
							}
						});
					}

					public void onFailure(Throwable error,
							String content) {
						TLRPC.TL_error networkerror = new TLRPC.TL_error();
						networkerror.code = -1;
						completionBlock.run(null, networkerror);
					}
				});
			} catch (Exception e) {
				TLRPC.TL_error networkerror = new TLRPC.TL_error();
				networkerror.code = -1;
				completionBlock.run(null, networkerror);
			}
		} else if (nAction == 5) {
			try {
				RequestParams params = new RequestParams();
				JSONObject jo = new JSONObject();
				jo.put(key_act, nAction);
				jo.put(key_conferenceId, PSTNm.conferenceId);
				params.put("param", jo.toString());

				client.post(Config.webFun_PSTN_CONTROL, params,
						new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						final String res = response;
						Utilities.stageQueue
						.postRunnable(new Runnable() {
							@Override
							public void run() {
								try {
									TLRPC.TL_PSTNResponse response = new TLRPC.TL_PSTNResponse();
									response.nAction = nAction;
									response.PSTNm = PSTNm;
									JSONTokener jsonParser = new JSONTokener(
											res);
									JSONObject ret = (JSONObject) jsonParser
											.nextValue();
									if (ret.has("result")) {
										response.nResult = ret
												.getInt("result");
										ArrayList<TLRPC.TL_PSTNUserStatus> statuslist = new ArrayList<TLRPC.TL_PSTNUserStatus>();
										ParserPSTNUserStatus(
												ret, statuslist);
										response.statuslist = statuslist;
										completionBlock.run(
												response, null);
									} else {
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = -1;
										completionBlock.run(
												null,
												networkerror);
									}
									return;
								} catch (Exception e) {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
									completionBlock.run(null,
											networkerror);
								}
							}
						});
					}

					public void onFailure(Throwable error,
							String content) {
						TLRPC.TL_error networkerror = new TLRPC.TL_error();
						networkerror.code = -1;
						completionBlock.run(null, networkerror);
					}
				});
			} catch (Exception e) {
				TLRPC.TL_error networkerror = new TLRPC.TL_error();
				networkerror.code = -1;
				completionBlock.run(null, networkerror);
			}
		} else if (nAction == 6) {
			try {
				RequestParams params = new RequestParams();
				JSONObject jo = new JSONObject();
				jo.put(key_act, nAction);
				jo.put(key_conferenceId, PSTNm.conferenceId);
				params.put("param", jo.toString());

				client.post(Config.webFun_PSTN_CONTROL, params,
						new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						final String res = response;
						Utilities.stageQueue
						.postRunnable(new Runnable() {
							@Override
							public void run() {
								try {
									TLRPC.TL_PSTNResponse response = new TLRPC.TL_PSTNResponse();
									response.nAction = nAction;
									response.PSTNm = PSTNm;
									JSONTokener jsonParser = new JSONTokener(
											res);
									JSONObject ret = (JSONObject) jsonParser
											.nextValue();
									// System.out.println(""+res);
									if (ret.has("result")) {
										response.nResult = ret
												.getInt("result");
										ArrayList<TLRPC.TL_PSTNUserStatus> statuslist = new ArrayList<TLRPC.TL_PSTNUserStatus>();
										ParserPSTNUserStatus(
												ret, statuslist);
										response.statuslist = statuslist;
										completionBlock.run(
												response, null);
									} else {
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = -1;
										completionBlock.run(
												null,
												networkerror);
									}
									return;
								} catch (Exception e) {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
									completionBlock.run(null,
											networkerror);
								}
							}
						});
					}

					public void onFailure(Throwable error,
							String content) {
						TLRPC.TL_error networkerror = new TLRPC.TL_error();
						networkerror.code = -1;
						completionBlock.run(null, networkerror);
					}
				});
			} catch (Exception e) {
				TLRPC.TL_error networkerror = new TLRPC.TL_error();
				networkerror.code = -1;
				completionBlock.run(null, networkerror);
			}
		} else if (nAction == 7) {
			try {
				RequestParams params = new RequestParams();
				JSONObject jo = new JSONObject();
				jo.put(key_act, nAction);
				// jo.put("conferenceId", PSTNm.conferenceId);
				params.put("param", jo.toString());

				client.post(Config.webFun_PSTN_CONTROL, params,
						new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						final String res = response;
						Utilities.stageQueue
						.postRunnable(new Runnable() {
							@Override
							public void run() {
								try {
									TLRPC.TL_PSTNResponse response = new TLRPC.TL_PSTNResponse();
									response.nAction = nAction;
									response.PSTNm = PSTNm;
									JSONTokener jsonParser = new JSONTokener(
											res);
									JSONObject ret = (JSONObject) jsonParser
											.nextValue();

									if (ret.has("result")) {
										response.nResult = ret
												.getInt("result");
										ArrayList<TLRPC.TL_PSTNMeeting> meetinglist = new ArrayList<TLRPC.TL_PSTNMeeting>();
										ParserPSTNMeetingList(
												ret,
												meetinglist);
										response.meetinglist = meetinglist;
										completionBlock.run(
												response, null);
									} else {
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = -1;
										completionBlock.run(
												null,
												networkerror);
									}
									return;
								} catch (Exception e) {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
									completionBlock.run(null,
											networkerror);
								}
							}
						});
					}

					public void onFailure(Throwable error,
							String content) {
						TLRPC.TL_error networkerror = new TLRPC.TL_error();
						networkerror.code = -1;
						completionBlock.run(null, networkerror);
					}
				});
			} catch (Exception e) {
				TLRPC.TL_error networkerror = new TLRPC.TL_error();
				networkerror.code = -1;
				completionBlock.run(null, networkerror);
			}
		} else if (nAction == 8 || nAction == 9) {
			try {
				RequestParams params = new RequestParams();
				JSONObject jo = new JSONObject();
				jo.put(key_act, nAction);
				jo.put(key_conferenceId, PSTNm.conferenceId);
				params.put("param", jo.toString());

				client.post(Config.webFun_PSTN_CONTROL, params,
						new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						final String res = response;
						// System.out.println(res);
						Utilities.stageQueue
						.postRunnable(new Runnable() {
							@Override
							public void run() {
								try {
									TLRPC.TL_PSTNResponse response = new TLRPC.TL_PSTNResponse();
									response.nAction = nAction;
									response.PSTNm = PSTNm;
									JSONTokener jsonParser = new JSONTokener(
											res);
									JSONObject ret = (JSONObject) jsonParser
											.nextValue();
									if (ret.has("result")) {
										response.nResult = ret
												.getInt("result");
										ArrayList<TLRPC.TL_PSTNMeeting> meetinglist = new ArrayList<TLRPC.TL_PSTNMeeting>();
										ParserPSTNMeetingList(
												ret,
												meetinglist);
										response.meetinglist = meetinglist;
										completionBlock.run(
												response, null);
									} else {
										TLRPC.TL_error networkerror = new TLRPC.TL_error();
										networkerror.code = -1;
										completionBlock.run(
												null,
												networkerror);
									}
									return;
								} catch (Exception e) {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
									completionBlock.run(null,
											networkerror);
								}
							}
						});
					}

					public void onFailure(Throwable error,
							String content) {
						TLRPC.TL_error networkerror = new TLRPC.TL_error();
						networkerror.code = -1;
						completionBlock.run(null, networkerror);
					}
				});
			} catch (Exception e) {
				TLRPC.TL_error networkerror = new TLRPC.TL_error();
				networkerror.code = -1;
				completionBlock.run(null, networkerror);
			}
		}
	}

	void ParserPSTNUserStatus(JSONObject jsonRet,
			ArrayList<TLRPC.TL_PSTNUserStatus> statuslist) { // 锟斤拷时锟斤拷锟斤拷锟斤拷锟斤拷
		try {
			if (jsonRet.has("resultdata")) {
				JSONArray userStatusarray = jsonRet.getJSONArray("resultdata");
				if (userStatusarray != null) {
					for (int i = 0; i < userStatusarray.length(); i++) {
						JSONObject jsonUser = userStatusarray.getJSONObject(i);
						if (jsonUser != null) {
							TLRPC.TL_PSTNUserStatus pstnUserStatus = new TLRPC.TL_PSTNUserStatus();
							pstnUserStatus.phone = jsonUser.getString("phone");
							pstnUserStatus.phoneaddress = jsonUser
									.getString("phoneaddress");
							pstnUserStatus.phonetype = jsonUser
									.getString("phonetype");
							pstnUserStatus.callstate = jsonUser
									.getString("callstate");
							pstnUserStatus.speakstate = jsonUser
									.getString("speakstate");
							pstnUserStatus.speakrequest = jsonUser
									.getString("speakrequest");
							pstnUserStatus.activetalker = jsonUser
									.getString("activetalker");

							statuslist.add(pstnUserStatus);
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	void ParserPSTNMeetingList(JSONObject jsonRet,
			ArrayList<TLRPC.TL_PSTNMeeting> meetinglist) {
		try {
			if (jsonRet.has("resultdata")) {
				JSONArray meetingarray = jsonRet.optJSONArray("resultdata");
				if (meetingarray != null) {
					for (int i = 0; i < meetingarray.length(); i++) {
						JSONObject jsonMeeting = meetingarray.getJSONObject(i);
						if (jsonMeeting != null) {

							String conferenceId = jsonMeeting
									.getString("conferenceid");
							TLRPC.TL_PSTNMeeting PSTNmeeting;
							if (MessagesController.getInstance().meeting2Map
									.containsKey(conferenceId)) {
								PSTNmeeting = MessagesController.getInstance().meeting2Map
										.get(conferenceId);
							} else {
								PSTNmeeting = new TLRPC.TL_PSTNMeeting();
								PSTNmeeting.conferenceId = conferenceId;
							}
							PSTNmeeting.meettitle = jsonMeeting
									.getString("meetTitle");
							PSTNmeeting.meetcall = jsonMeeting
									.getString("meetCall");
							PSTNmeeting.starttime = jsonMeeting
									.getString("starttime");
							PSTNmeeting.endtime = jsonMeeting
									.getString("endtime");
							PSTNmeeting.meetingfee = jsonMeeting
									.getString("meetingfee");
							meetinglist.add(PSTNmeeting);
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void updateLocations() {
		Utilities.stageQueue.postRunnable(new Runnable() {

			@Override
			public void run() {
				client.post(Config.webFun_GET_LOCATION,
						new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						final String res = response;
						Utilities.stageQueue
						.postRunnable(new Runnable() {
							@Override
							public void run() {
								try {
									JSONTokener jsonParser = new JSONTokener(
											res);
									JSONObject ret = (JSONObject) jsonParser
											.nextValue();
									if (ret.has("result")
											&& ret.getInt("result") == 0) {
										// 实锟斤拷锟斤拷注锟斤拷锟剿ｏ拷锟斤拷示session锟斤拷锟斤拷锟斤拷,lhy
										// todo,锟斤拷锟叫猴拷锟斤拷锟斤拷锟斤拷要锟斤拷锟斤拷result

										Map<Integer, String> locations = new HashMap<Integer, String>();

										JSONArray array = (JSONArray) ret
												.optJSONArray("location");
										for (int i = 0; i < array
												.length(); i++) {
											JSONObject jsoLoc = array
													.getJSONObject(i);
											locations.put(
													jsoLoc.optInt("locationid"),
													jsoLoc.optString("locationname"));
										}
										MessagesStorage
										.getInstance()
										.insertLocation(
												locations);

									}

								} catch (Exception e) {
									TLRPC.TL_error networkerror = new TLRPC.TL_error();
									networkerror.code = -1;
								}
							}
						});
					}

					@Override
					public void onFailure(Throwable error,
							String content) {
						Utilities.stageQueue
						.postRunnable(new Runnable() {
							@Override
							public void run() {
								TLRPC.TL_error networkerror = new TLRPC.TL_error();
								networkerror.code = -1;
							}
						});
					}
				});
			}
		});
	}
}

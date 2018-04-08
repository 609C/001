/*
 * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import info.emm.LocalData.Config;
import info.emm.ui.ApplicationLoader;
import info.emm.utils.Utilities;

public class UserConfig {
	public static TLRPC.User currentUser;
	//public static TLRPC.TL_userSelf self;
	/**
	 * 客户ID
	 */
	public static int clientUserId = 0;
	public static boolean clientActivated = false;
	public static boolean registeredForPush = false;
	public static String pushString = "";
	public static int lastSendMessageId = -210000;
	public static int lastLocalId = -210000;
	public static int lastUserId = -10000;//手机本地联系人开始ID，然后逐渐减少，临时代表userid,等某个联系人安装了软件，就变成了正数了
	public static String contactsHash = "";
	public static String importHash = "";
	private final static Integer sync = 1;
	public static boolean saveIncomingPhotos = false;
	public static int contactsVersion = 1;
	public static boolean firstTimeInstall = false;
	//xueqiang repair begin
	public static int lastAlertId = 1;
	public static int lastContactId = -10000;
	public static String account;
	public static String pubcomaccount;
	public static String priaccount;
	public static String email;
	public static String phone;
	public static String coutryCode;
	public static String domain;
	public static String privateWebHttp;
	public static int privatePort;
	//	public static String publishWebServer;
//	public static int publishPort;
	public static String privateMESSAGEHOST;
	public static int privateMESSAGEPORT;
	static public String strProtocolPath = null;
	//当用户第一次使用新版本，版本号码为了传-1，以后传增量
	static public boolean bFirstUseNewVersion = false;

	public static int currentSequenceNumber = 20000;
	public static int lastSequenceNumber = 20000;
	//是个人版还是企业版，个人版需要显示手机通讯录，企业版需要显示企业通讯录
	public static boolean isPersonalVersion = true;
	public static boolean isPublic = true;
	public static String meetingNickName="";
	public static String meetingID="";
	public static String meetingPwd;

	public static String versionNum;
	public static boolean bShowUpdateInfo = true;
	public static int currentProductModel;

	public static int getNewMessageId() {
		int id;
		synchronized (sync) {
			id = lastSendMessageId;
			lastSendMessageId--;
		}
		return id;
	}

	public static int getAlertId() {
		int id;
		synchronized (sync) {
			lastAlertId++;
			id = lastAlertId;
		}
		UserConfig.saveConfig(false);
		return id;
	}
	public static int getContactId() {
		int id;
		synchronized (sync) {
			lastContactId++;
			if(lastContactId==0)
				lastContactId = -10000;
			id = lastContactId;
		}
		UserConfig.saveConfig(false);
		return id;
	}

	public static int getSeq() {
		int seq;
		boolean needSave = false;
		synchronized (sync) {
			currentSequenceNumber++;
			if(currentSequenceNumber > lastSequenceNumber)
			{
				lastSequenceNumber += 1000;
				needSave = true;
			}
			seq = currentSequenceNumber;
		}
		if(needSave)
			UserConfig.saveConfig(false);
		return seq;
	}
	public static boolean readConfig()
	{

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(ApplicationLoader.applicationContext.getResources().getAssets().open("config.txt")));
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] args = line.split(";");
				Log.e("emm",args[0]+":"+args[1]+":"+args[2]+":"+args[3]);
				isPublic = false;
				privateWebHttp = args[0].trim();
				privatePort = Integer.parseInt(args[1].trim());
				if(privatePort==80)
					privatePort = 0;
				if(!privateWebHttp.isEmpty())
				{
					if(privatePort!=0){
						Config.setWebHttp(privateWebHttp+":"+privatePort);
					}else{
						Config.setWebHttp(privateWebHttp);
					}
				}
				meetingID = args[2].trim();
				meetingNickName = args[3].trim();
				if(args.length>4)
				{
					meetingPwd = args[4].trim();
				}
				return true;
			}
		} catch (Exception e) {
			FileLog.e("emm", e);
		}
		return false;
	}
	public static void saveConfig(boolean withFile) {
		saveConfig(withFile, null);
	}

	public static void saveConfig(boolean withFile, File oldFile) {
		synchronized (sync) {
			try {
				SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("userconfing", Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean("registeredForPush", registeredForPush);
				editor.putBoolean("isPersonalVersion", isPersonalVersion);
				editor.putString("pushString", pushString);
				editor.putInt("lastSendMessageId", lastSendMessageId);
				editor.putInt("lastLocalId", lastLocalId);
				editor.putInt("lastUserId", lastUserId);//xueqiang change for phoneuser
				editor.putInt("lastContactId", lastContactId);//xueqiang change for phoneuser
				editor.putString("contactsHash", contactsHash);
				editor.putString("importHash", importHash);
				editor.putBoolean("saveIncomingPhotos", saveIncomingPhotos);
				editor.putInt("contactsVersion", contactsVersion);
				editor.putBoolean("clientActivated", clientActivated);
				editor.putBoolean("firstTimeInstall", firstTimeInstall);
				editor.putString("account", account);
				editor.putString("coutryCode", coutryCode);
				editor.putString("domain", domain);
				editor.putString("webhttp", privateWebHttp);
				editor.putBoolean("isPublic", isPublic);
				editor.putInt("privatePort", privatePort);
				editor.putString("pubcomaccount", pubcomaccount);
				editor.putString("priaccount", priaccount);
				editor.putString("privateMESSAGEHOST", privateMESSAGEHOST);
				editor.putInt("privateMESSAGEPORT", privateMESSAGEPORT);
				editor.putString("meetingNickName", meetingNickName);
				editor.putBoolean("bFirstUseNewVersion", bFirstUseNewVersion);
				editor.putBoolean(versionNum, bShowUpdateInfo);


				if( Utilities.isPhone(account) )
					editor.putString("phone", account);
				else
					editor.putString("email", account);

				if(isPublic){
					Config.setWebHttp(Config.publicWebHttp);
				}else{
					if(!privateMESSAGEHOST.isEmpty()){
						Config.setMessageHostAndPort(privateMESSAGEHOST, privateMESSAGEPORT);
					}
					if(!privateWebHttp.isEmpty()){
						if(privatePort!=0){
							Config.setWebHttp(privateWebHttp+":"+privatePort);
						}else{
							Config.setWebHttp(privateWebHttp);
						}
					}
				}
				if (currentUser != null)
				{
					if (withFile)
					{
						SerializedData data = new SerializedData();
						currentUser.serializeToStream(data);
						clientUserId = currentUser.id;
						currentProductModel = currentUser.productmodel;
						//                        clientActivated = true;
						String userString = Base64.encodeToString(data.toByteArray(), Base64.DEFAULT);
						FileLog.e("emm", "userconfig sessionid="+currentUser.sessionid);
						editor.putString("user", userString);
					}
				}
				/*else
                {
                    editor.remove("user");
                }*/
				editor.putInt("lastAlertId", lastAlertId);//xueqiang add one line
				editor.putInt("lastSequenceNumber", lastSequenceNumber);//sam

				editor.commit();
				if (oldFile != null) {
					oldFile.delete();
				}


			} catch (Exception e) {
				FileLog.e("emm", e);
			}
		}
	}

	public static void loadConfig() {
		synchronized (sync) {

			SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("userconfing", Context.MODE_PRIVATE);
			registeredForPush = preferences.getBoolean("registeredForPush", false);
			isPersonalVersion = preferences.getBoolean("isPersonalVersion", true);
			pushString = preferences.getString("pushString", "");
			lastSendMessageId = preferences.getInt("lastSendMessageId", -210000);
			lastLocalId = preferences.getInt("lastLocalId", -210000);
			lastContactId= preferences.getInt("lastContactId", -5000);
			contactsHash = preferences.getString("contactsHash", "");
			importHash = preferences.getString("importHash", "");
			saveIncomingPhotos = preferences.getBoolean("saveIncomingPhotos", false);
			contactsVersion = preferences.getInt("contactsVersion", 0);
			lastAlertId = preferences.getInt("lastAlertId", 1);//xueqiang change
			lastSequenceNumber = preferences.getInt("lastSequenceNumber", 20000);//sam
			currentSequenceNumber = lastSequenceNumber;
			String user = preferences.getString("user", null);
			clientActivated = preferences.getBoolean("clientActivated",false );
			firstTimeInstall = preferences.getBoolean("firstTimeInstall",false );
			account = preferences.getString("account", "");
			email = preferences.getString("email", "");
			phone = preferences.getString("phone", "");
			domain = preferences.getString("domain", "");
			privateWebHttp = preferences.getString("webhttp", "");
			pubcomaccount = preferences.getString("pubcomaccount", "");
			priaccount = preferences.getString("priaccount", "");

			//国家代码
			coutryCode= preferences.getString("coutryCode", "86");
			lastUserId = preferences.getInt("lastUserId", -10000);//xueqiang change for phoneuser
			isPublic =  ApplicationLoader.isprivate() ? false : preferences.getBoolean("isPublic", true);
			privatePort = preferences.getInt("privatePort", 0);
			privateMESSAGEHOST = preferences.getString("privateMESSAGEHOST", "");
			privateMESSAGEPORT = preferences.getInt("privateMESSAGEPORT", 0);
			meetingNickName = preferences.getString("meetingNickName", "");
			bFirstUseNewVersion = preferences.getBoolean("bFirstUseNewVersion",false );

			bShowUpdateInfo = preferences.getBoolean(versionNum,true );

			if(isPublic)
			{
				Config.setWebHttp(Config.publicWebHttp);
				Config.setMessageHostAndPort("app.weiyicloud.com", 2443);
			}else{
				if(!privateMESSAGEHOST.isEmpty()){
					Config.setMessageHostAndPort(privateMESSAGEHOST, privateMESSAGEPORT);
				}
				if(!privateWebHttp.isEmpty()){
					if(privatePort!=0){
						Config.setWebHttp(privateWebHttp+":"+privatePort);
					}else{
						Config.setWebHttp(privateWebHttp);
					}
				}
			}
			if (user != null)
			{
				byte[] userBytes = Base64.decode(user, Base64.DEFAULT);
				if (userBytes != null) {
					SerializedData data = new SerializedData(userBytes);
					currentUser = (TLRPC.TL_userContact)TLClassStore.Instance().TLdeserialize(data, data.readInt32());
					clientUserId = currentUser.id;
					currentProductModel = currentUser.productmodel;
					//clientActivated = true;
					FileLog.e("emm", "userconfig loadConfig sessionid="+currentUser.sessionid);
				}
			}
			if (currentUser == null) {
				clientActivated = false;
				clientUserId = 0;
				currentProductModel = 0;
			}

			Log.e("emm", "public====="+isPublic);
		}
	}

	public static void clearConfig() {
		clientUserId = 0;
		clientActivated = false;
		currentUser = null;
		registeredForPush = false;
		contactsHash = "";
		importHash = "";
		currentProductModel = 0;
		/*lastLocalId = -210000;
        lastSendMessageId = -210000;
        contactsVersion = 1;
        saveIncomingPhotos = false;
        lastSequenceNumber = 10000;
        lastUserId = -10000;
        lastAlertId = 1;
        lastContactId = -10000;*/
		saveConfig(true);
		MessagesController.getInstance().deleteAllAppAccounts();
	}
	public static void logout()
	{
		clientUserId = 0;
		clientActivated = false;
		if(currentUser!=null)
		{
			currentUser.id=0;
		}
		saveConfig(true);
	}
	public static String getFullName(String usePhone)
	{
		String tempAccount = usePhone;
		if(Utilities.isPhone(usePhone))
		{
			//sam 增加一个保护性判断，如果不是以+开始的字符串，才加国家码，否则认为是一个包含国家码的号码
			if( !tempAccount.startsWith("+"))
			{
				//将所有的电话都转换成带+号+国家代码+电话号码
				if(tempAccount.startsWith("00"))
				{
					tempAccount = tempAccount.substring(2);
					tempAccount  = "+" + tempAccount;
				}
				else
					tempAccount = "+"+UserConfig.coutryCode+tempAccount;
			}
			//把头三位去掉，只保留手机号，国外以后统一使用EMail,在中国使用手机号
			tempAccount = tempAccount.substring(3);
		}
		return tempAccount;
	}
	public static String getPhoneNoCode(String usePhone) {
		String tempAccount = usePhone;
		if(Utilities.isPhone(usePhone))
		{
			if( tempAccount.startsWith("+") )
			{
				tempAccount = tempAccount.substring(3);
			}
		}
		return tempAccount;
	}

	public static String getNickName()
	{
		if(UserConfig.currentUser!=null)
			return UserConfig.currentUser.first_name;
		else if(!meetingNickName.isEmpty())
			return meetingNickName;
		return android.os.Build.MANUFACTURER;
	}
	public static String getJoinMeetingName()
	{
		if(!meetingNickName.isEmpty())
			return meetingNickName;
		else if(UserConfig.currentUser!=null)
			return UserConfig.currentUser.first_name;
		return android.os.Build.MANUFACTURER;
	}
}

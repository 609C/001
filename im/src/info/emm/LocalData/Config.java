package info.emm.LocalData;




import com.utils.WeiyiMeeting;

import info.emm.im.meeting.MeetingMgr;
import info.emm.messenger.UserConfig;


public class Config {
	public void Config(){}    


	public static String publicWebHttp = "http://139.199.184.75:80";
//	    public static String publicWebHttp = "http://192.168.0.121"; 
	public static String MESSAGEHOST = "app.weiyicloud.com";
	public static int MESSAGEPORT = 2443;//消息服务器的端口



	public static String webFunBase = "/ClientAPI/";

	public static String webFun_checkuserpwd			= publicWebHttp + webFunBase + "checkuserpwd";
	public static String webFun_registeredusers		= publicWebHttp + webFunBase + "registeredusers";
	public static String webFun_sendverificationcode	= publicWebHttp + webFunBase + "sendverificationcode";
	public static String webFun_checkverificationcode	= publicWebHttp + webFunBase + "checkverificationcode";
	public static String webFun_logout				= publicWebHttp + webFunBase + "logout";
	public static String webFun_getcompanyuser		= publicWebHttp + webFunBase + "getcompanyuser";
	public static String webFun_getdepartmentuser		= publicWebHttp + webFunBase + "getdepartmentuser";
	public static String webfun_uploadfile			= publicWebHttp + webFunBase + "uploadfile";
	public static String webFun_controlusergroup		= publicWebHttp + webFunBase + "controlusergroup";
	public static String webFun_getusergroup			= publicWebHttp + webFunBase + "getusergroup";
	public static String webFun_updateuserinfo		= publicWebHttp + webFunBase + "updateuserinfo";
	public static String webFun_getmeeting			= publicWebHttp + webFunBase + "getmeeting";
	public static String webFun_getuser				= publicWebHttp + webFunBase + "getuser";
	public static String webFun_resetpassword			= publicWebHttp + webFunBase + "resetpassword";
	public static String webFun_controlmeeting		= publicWebHttp + webFunBase + "controlmeeting";
	public static String webFun_getUpdate				= publicWebHttp + webFunBase + "getupdate";
	public static String webFun_getusercontacts	= publicWebHttp + webFunBase + "getusercontacts";
	public static String webFun_controlcompany		= publicWebHttp + webFunBase + "controlcompany";
	public static String webFun_gettime				= publicWebHttp + webFunBase + "gettime";
	public static String webFun_bindaccount		= publicWebHttp + webFunBase + "bindaccount";
	//bbs
	public static String webFun_POST_LIST 			= publicWebHttp + webFunBase + "getcategorybbs";
	public static String webFun_POST_DETAIL 			= publicWebHttp + webFunBase + "getbbsdetail";
	public static String webFun_POST_PUB 				= publicWebHttp + webFunBase + "publishnewbbs";
	public static String webFun_COMMENT_LIST 			= publicWebHttp + webFunBase + "getbbsreply";
	public static String webFun_COMMENT_PUB 			= publicWebHttp + webFunBase + "replybbs";
	public static String webFun_UPLOAD_FILE			= publicWebHttp + webFunBase + "uploadfile";
	public static String webFun_GET_CATEGORY 			= publicWebHttp + webFunBase + "getcategory";
	public static String webFun_GET_COMPANYBBS 		= publicWebHttp + webFunBase + "getcompanybbs";
	public static String webFun_COMMENT_REPLY 		= publicWebHttp + webFunBase + "replybbs";
	public static String webFun_USER_FEED_BACK 		= publicWebHttp + webFunBase + "userfeedback";
	public static String webFun_SETREMARKNAME 		= publicWebHttp + webFunBase + "setremarkname";
	public static String webFun_GETMEETINGSTATUS 		= publicWebHttp + webFunBase + "getmeetingstatus";


	public static String webFun_PSTN_CONTROL		= publicWebHttp + webFunBase + "ControlPSTNMeeting";
	public static String webFun_GET_LOCATION        = publicWebHttp + webFunBase + "getlocation";

	public static String webFun_Param = "param";

	public static int RootParentDeptID = 0;
	public static String CompanyID = "CompanyID";
	public static String DeptID = "DeptID";
	public static String DeptName = "DeptName";
	public static String ParentDeptID = "ParentDeptID";
	public static String FromAddress = "FromAddress";
	public static String FromAddressMeeting = "meeting_create";
	public static String FromAddressGroup = "group_create";
	public static String Account = "Account";
	public static String Password = "Password";
	public static String Version = "Version";
	public static String UserName = "userName";
	public static String IsGroup = "isGroup";
	public static String UserID = "UserID";
	public static String ServerID ="ServerID";
	public static String AddGroupUser = "AddGroupUser";
	public static String GroupCompanyID = "GroupCompanyID";
	public static String NullString = "null";
	public static String UserChat = "u";
	public static String GroupChat = "g";
	public static String DontChangeChatIco = "UBI-DontChangeChatIco-UBI";
	public static String DontChangeChatTitle = "UBI-DontChangeChatTitle-UBI";
	public static String MessagesFrament = "MessagesFrament";
	public static String ContactsFrament = "ContactsFrament";
	public static String DiscussFrament = "DiscussFrament";
	public static String MeetingFrament = "MeetingFrament";
	public static String SettingFrament = "SettingFrament";
	public static String ContactsActivity = "MainAddress";
	public static String ChatsActivity = "MessagesActivity";
	public static String MeetingActivity = "MeetingActivity";
	public static String DiscussActivity = "ForumMainePage";
	public static void setWebHttp(String httpServer)
	{

		if(!httpServer.startsWith("http://")){
			httpServer = "http://"+httpServer;
		}
		MeetingMgr.getInstance().setWebHttpServerAddress(httpServer);
		WeiyiMeeting.getInstance().setM_httpServer(httpServer);
		//MeetingMgr.set
		webFun_checkuserpwd			= httpServer + webFunBase + "checkuserpwd";
		webFun_registeredusers		= httpServer + webFunBase + "registeredusers";
		webFun_sendverificationcode	= httpServer + webFunBase + "sendverificationcode";
		webFun_checkverificationcode	= httpServer + webFunBase + "checkverificationcode";
		webFun_logout				= httpServer + webFunBase + "logout";
		webFun_getcompanyuser		= httpServer + webFunBase + "getcompanyuser";
		webFun_getdepartmentuser		= httpServer + webFunBase + "getdepartmentuser";
		webfun_uploadfile			= httpServer + webFunBase + "uploadfile";
		webFun_controlusergroup		= httpServer + webFunBase + "controlusergroup";
		webFun_getusergroup			= httpServer + webFunBase + "getusergroup";
		webFun_updateuserinfo		= httpServer + webFunBase + "updateuserinfo";
		webFun_getmeeting			= httpServer + webFunBase + "getmeeting";
		webFun_getuser				= httpServer + webFunBase + "getuser";
		webFun_resetpassword			= httpServer + webFunBase + "resetpassword";
		webFun_controlmeeting		= httpServer + webFunBase + "controlmeeting";
		webFun_getUpdate				= httpServer + webFunBase + "getupdate";
		webFun_getusercontacts	= httpServer + webFunBase + "getusercontacts";
		webFun_controlcompany		= httpServer + webFunBase + "controlcompany";
		webFun_gettime				= httpServer + webFunBase + "gettime";
		webFun_bindaccount		= httpServer + webFunBase + "bindaccount";
		//bbs
		webFun_POST_LIST 			= httpServer + webFunBase + "getcategorybbs";
		webFun_POST_DETAIL 			= httpServer + webFunBase + "getbbsdetail";
		webFun_POST_PUB 				= httpServer + webFunBase + "publishnewbbs";
		webFun_COMMENT_LIST 			= httpServer + webFunBase + "getbbsreply";
		webFun_COMMENT_PUB 			= httpServer + webFunBase + "replybbs";
		webFun_UPLOAD_FILE			= httpServer + webFunBase + "uploadfile";
		webFun_GET_CATEGORY 			= httpServer + webFunBase + "getcategory";
		webFun_GET_COMPANYBBS 		= httpServer + webFunBase + "getcompanybbs";
		webFun_COMMENT_REPLY 		= httpServer + webFunBase + "replybbs";
		webFun_USER_FEED_BACK 		= httpServer + webFunBase + "userfeedback";
		webFun_SETREMARKNAME 		= httpServer + webFunBase + "setremarkname";
		webFun_GETMEETINGSTATUS 		= httpServer + webFunBase + "getmeetingstatus";


		webFun_PSTN_CONTROL		= httpServer + webFunBase + "ControlPSTNMeeting";
		webFun_GET_LOCATION        = httpServer + webFunBase + "getlocation";
	}
	public static String getWebHttp(){
		if(UserConfig.isPublic){
			return publicWebHttp;
		}
		else
		{
			String privateWebHttp = UserConfig.privateWebHttp;
			int privatePort = UserConfig.privatePort;
			String httpserver="";
			if(!privateWebHttp.isEmpty()){ 
				if(privatePort!=0){
					httpserver = privateWebHttp+":"+privatePort;
				}else{                		
					httpserver = privateWebHttp;
				}
			}

			if(!httpserver.startsWith("http://"))    			
				httpserver =  "http://"+httpserver;
			return httpserver;
		}
	}
	public static void setMessageHostAndPort(String messageHost,int messagePort){
		MESSAGEHOST = messageHost;
		MESSAGEPORT = messagePort;
	}
}


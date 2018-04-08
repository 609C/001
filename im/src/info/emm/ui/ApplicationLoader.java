/*
 * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.view.ViewConfiguration;
import android.view.WindowManager;


//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GooglePlayServicesUtil;
//import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.loopj.android.http.PersistentCookieStore;

import info.emm.LocalData.Config;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.MessagesStorage;
import info.emm.messenger.NativeLoader;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.TLRPC;
import info.emm.messenger.NotificationCenter.NotificationCenterDelegate;
import info.emm.messenger.ScreenReceiver;
import info.emm.messenger.UserConfig;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.ToolUtil;
import info.emm.utils.UiUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.utils.WeiyiMeeting;


public class ApplicationLoader extends Application implements NotificationCenterDelegate {
	//private GoogleCloudMessaging gcm;    
	private String regid;
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";    
	public static long lastPauseTime;
	public static Bitmap cachedWallpaper = null;

	public static volatile Context applicationContext = null;
	public static volatile Handler applicationHandler = null;
	private static volatile boolean applicationInited = false;
	public static volatile boolean isScreenOn = false;
	public static volatile boolean mqStarted = false;

	public static ArrayList<BaseFragment> fragmentsStack = new ArrayList<BaseFragment>();
	public static ArrayList<BaseFragment> infragmentsStack = new ArrayList<BaseFragment>();
	public static ArrayList<BaseFragment> fragmentList = new ArrayList<BaseFragment>();

	public static PersistentCookieStore myCookieStore = null;

	private static ApplicationLoader sInstance;

	public static final int edition = 0;//�汾��Ŀǰ0Ϊ��׼��1ΪGE��

	public ApplicationLoader() {
		sInstance = this;
	}
	public static ApplicationLoader getInstance() {
		return sInstance;
	}
	public static Context getContext() {
		return getInstance();
	}

	public static void postInitApplication() 
	{	
		String versionName= ToolUtil.getAppVersionName(applicationContext);// 
		UserConfig.versionNum = versionName;
		UserConfig.loadConfig();
		LoadLocalConfig();

		LocaleController.getInstance().GetConfigLanguage();

		FileLog.e("emm", "postInitApplication************");
		if (applicationInited) 
		{
			FileLog.e("emm", "application inited************");
			return;
		}  
		applicationInited = true;
		myCookieStore = new PersistentCookieStore(applicationContext);


		if(UserConfig.clientActivated)
		{
			MessagesStorage.getInstance().cleanUp();//xueqiang change
			MessagesStorage.getInstance().openDatabase();
		}
		//ConnectionsManager.getInstance().StartNetWorkService();
		NativeLoader.initNativeLibs(applicationContext);


		try {
			LocaleController.getInstance();
		} catch (Exception e) {
			e.printStackTrace();      
		}

		try {
			final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			final BroadcastReceiver mReceiver = new ScreenReceiver();
			applicationContext.registerReceiver(mReceiver, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}     

		try {
			PowerManager pm = (PowerManager)ApplicationLoader.applicationContext.getSystemService(Context.POWER_SERVICE);
			isScreenOn = pm.isScreenOn();
			FileLog.d("emm", "screen state = " + isScreenOn);
		} catch (Exception e) {
			FileLog.e("emm", e);
		}

		//UserConfig.loadConfig();
		if (UserConfig.currentUser != null) {

			MessagesController.getInstance().users.put(UserConfig.clientUserId, UserConfig.currentUser);
			//ConnectionsManager.getInstance().applyCountryPortNumber(UserConfig.currentUser.phone);          
		}

		ApplicationLoader app = (ApplicationLoader)ApplicationLoader.applicationContext;
		//        app.initPlayServices();
	}

	@Override
	public void onCreate() 
	{	
		super.onCreate();

		lastPauseTime = System.currentTimeMillis();
		applicationContext = getApplicationContext();
		applicationHandler = new Handler(applicationContext.getMainLooper());
		FileLog.e("emm", "application oncreate************");
		WeiyiMeeting.init(applicationContext, applicationHandler);  
		NotificationCenter.getInstance().addObserver(this, MessagesController.unread_message_update);

		java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
		java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");

		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if(menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		initImageLoader(getApplicationContext());

		getWindowInfo();

		UserConfig.loadConfig();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		try {
			LocaleController.getInstance().onDeviceConfigurationChange(newConfig);
			Utilities.checkDisplaySize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getWindowInfo() {
		WindowManager windowManager = (WindowManager) getApplicationContext()
				.getSystemService("window");
		DisplayMetrics metrics = new DisplayMetrics();
		windowManager.getDefaultDisplay().getMetrics(metrics);
		//		ToolUtil.showLog("density = "+metrics.density+",densityDpi = "+metrics.densityDpi);
		UiUtil.ScreenWidth = metrics.widthPixels;
		UiUtil.ScreenHeight = metrics.heightPixels;
	}
	public static void resetLastPauseTime() {
		lastPauseTime = 0;
		//ConnectionsManager.getInstance().applicationMovedToForeground();
	}

	private void initPlayServices() {
		//        if (checkPlayServices()) {
		//            gcm = GoogleCloudMessaging.getInstance(this);
		//            regid = getRegistrationId();
		//
		//            if (regid.length() == 0) {
		//                registerInBackground();
		//            } else {
		//                sendRegistrationIdToBackend(false);
		//            }
		//        } else {
		//            FileLog.d("emm", "No valid Google Play Services APK found.");
		//        }
	}

	private boolean checkPlayServices() {
		return false;
		//        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		//        return resultCode == ConnectionResult.SUCCESS;
		/*if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("emm", "This device is not supported.");
            }
            return false;
        }
        return true;*/
	}

	private String getRegistrationId() {
		final SharedPreferences prefs = getGCMPreferences(applicationContext);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.length() == 0) {
			FileLog.d("emm", "Registration not found.");
			return "";
		}
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion();
		if (registeredVersion != currentVersion) {
			FileLog.d("emm", "App version changed.");
			return "";
		}
		return registrationId;
	}

	private SharedPreferences getGCMPreferences(Context context) {
		return getSharedPreferences(ApplicationLoader.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	public static int getAppVersion() {
		try {
			PackageInfo packageInfo = applicationContext.getPackageManager().getPackageInfo(applicationContext.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	//    private void registerInBackground() {
	//        AsyncTask<String, String, Boolean> task = new AsyncTask<String, String, Boolean>() {
	//            @Override
	//            protected Boolean doInBackground(String... objects) {
	//                if (gcm == null) {
	//                    gcm = GoogleCloudMessaging.getInstance(applicationContext);
	//                }
	//                int count = 0;
	//                while (count < 1000) {
	//                    try {
	//                        count++;
	//                        regid = gcm.register(BuildVars.GCM_SENDER_ID);
	//                        sendRegistrationIdToBackend(true);
	//                        storeRegistrationId(applicationContext, regid);
	//                        return true;
	//                    } catch (Exception e) {
	//                        FileLog.e("emm", e);
	//                    }
	//                    try {
	//                        if (count % 20 == 0) {
	//                            Thread.sleep(60000 * 30);
	//                        } else {
	//                            Thread.sleep(5000);
	//                        }
	//                    } catch (InterruptedException e) {
	//                        FileLog.e("emm", e);
	//                    }
	//                }
	//                return false;
	//            }
	//        }.execute(null, null, null);
	//    }

	private void sendRegistrationIdToBackend(final boolean isNew) {
		Utilities.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				UserConfig.pushString = regid;
				UserConfig.registeredForPush = !isNew;
				UserConfig.saveConfig(false);
				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						MessagesController.getInstance().registerForPush(regid);
					}
				});
			}
		});
	}

	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion();
		FileLog.e("emm", "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}


	public static void LoadLocalConfig()
	{   
		boolean changed = false;
		SharedPreferences preferences = applicationContext.getSharedPreferences("Notifications_" + UserConfig.clientUserId, MODE_PRIVATE);
		int v = preferences.getInt("v", 0);
		if (v != 1) {
			SharedPreferences preferences2 = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig_" + UserConfig.clientUserId, Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = preferences2.edit();
			if (preferences.contains("view_animations")) {
				editor.putBoolean("view_animations", preferences.getBoolean("view_animations", false));
			}
			if (preferences.contains("selectedBackground")) {
				editor.putInt("selectedBackground", preferences.getInt("selectedBackground", 1000001));
			}
			if (preferences.contains("selectedColor")) {
				editor.putInt("selectedColor", preferences.getInt("selectedColor", 0));
			}
			if (preferences.contains("fons_size")) {
				editor.putInt("fons_size", preferences.getInt("fons_size", 16));
			}
			editor.commit();
			editor = preferences.edit();
			editor.putInt("v", 1);
			editor.remove("view_animations");
			editor.remove("selectedBackground");
			editor.remove("selectedColor");
			editor.remove("fons_size");
			editor.commit();
		}
	}
	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you may tune some of them,
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
		.threadPriority(Thread.NORM_PRIORITY - 2)
		.denyCacheImageMultipleSizesInMemory()
		.diskCacheFileNameGenerator(new Md5FileNameGenerator())
		.tasksProcessingOrder(QueueProcessingType.LIFO)
		.writeDebugLogs() // Remove for release app
		.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}


	@Override
	public void didReceivedNotification(int id, Object... args) {
		if (id == MessagesController.unread_message_update){
			Utilities.setBadgeCount(applicationContext,MessagesController.getInstance().getMessagesUnreadCount());
		}		
	}


	public boolean isInMeeting()
	{
		return WeiyiMeeting.isInMeeting();
	}
	public void forceExitMeeting()
	{
		WeiyiMeeting.forceExitMeeting();
	}
	public void joinInstMeeting(final Activity activity,final String mid,final int chatid)
	{
		
		WeiyiMeeting.joinInstMeeting(activity,Config.getWebHttp(),mid,UserConfig.getNickName(),UserConfig.clientUserId,chatid,MessagesController.getInstance(),myCookieStore);

	}
	public void joinMeetingbyUrl(final Activity activity,Intent intent,final String name)
	{
		WeiyiMeeting.setThirdUserID(UserConfig.clientUserId==0?-1:UserConfig.clientUserId);
		WeiyiMeeting.joinMeetingByUrl(activity, intent, name);
	}
	
	
	public void joinMeeting(final Activity activity,final String mid,final String pwd){
		TLRPC.TL_MeetingInfo info = null;
		for(int i = 0 ;i<MessagesController.getInstance().meetingList.size();i++){
			if(mid.equals(MessagesController.getInstance().meetingList.get(i).mid+"")){
				info = MessagesController.getInstance().meetingList.get(i);
			}
		}			
		String Url = null;
		if(UserConfig.isPublic){
			String ss = Config.publicWebHttp;
			String publicHttp = ss.substring(7);
			Url = "weiyi://start?ip="+publicHttp+"&port="+80+"&meetingid="+info.mid+"&meetingtype="+info.meetingType+"&title="+info.topic+"&clientidentification="+UserConfig.currentUser.identification+"&createrid="+info.createid+"&userid="+UserConfig.clientUserId;
		}else{
			if(UserConfig.privatePort == 80){
				String privateHttp = UserConfig.privateWebHttp;
				Url = "weiyi://start?ip="+privateHttp+"&port="+80+"&meetingid="+info.mid+"&meetingtype="+info.meetingType+"&title="+info.topic+"&clientidentification="+UserConfig.currentUser.identification+"&createrid="+info.createid+"&userid="+UserConfig.clientUserId;
			}else{
				String privateHttp = UserConfig.privateWebHttp;
				int privatePort = UserConfig.privatePort;
				Url = "weiyi://start?ip="+privateHttp+"&port="+privatePort+"&meetingid="+info.mid+"&meetingtype="+info.meetingType+"&title="+info.topic+"&clientidentification="+UserConfig.currentUser.identification+"&createrid="+info.createid+"&userid="+UserConfig.clientUserId;
			}
		}
		WeiyiMeeting.getInstance().joinMeeting(activity,Url,null);
	}

	
	
	
	/**	public void joinMeeting(final Activity activity,final String mid,final String pwd)
	{
		//public static void joinMeeting(final Activity activiy,final String httpServer,final String domain,final String nickName,final String account,final int userid,final String userPwd,final String mid,final String meetingPwd,final MeetingCallBack callback)
		                 //joinMeeting( activiy,String httpServer,final String serverport,final String mid,final String nickName,final String pwd,final int thirduid,final int usertype,final MeetingCallBack callback)

//		@Override
//			public void onSuccess(String mid, String pwd, int role,int meetingtype) {
//				Utitlties.HideProgressDialog(activity);
//				if(meetingtype == 11||meetingtype == 12||meetingtype == 13||meetingtype == 14){
//					errorTipDialog(activity,R.string.IsLiveMeeting);
//				}
//			}
//
//			@Override
//			public void onError(int nRet) 
//			{
//				Utitlties.HideProgressDialog(activity);
//				if (nRet == 4008) {
//					inputMeetingPassward(activity,R.string.checkmeeting_error_4008,mid);
//				} else if (nRet == 4110) {
//					inputMeetingPassward(activity,R.string.checkmeeting_error_4110,mid);
//				} else if (nRet == 4007) {
//					errorTipDialog(activity,R.string.checkmeeting_error_4007);
//				} else if (nRet == 3001) {
//					errorTipDialog(activity,R.string.checkmeeting_error_3001);
//				} else if (nRet == 3002) {
//					errorTipDialog(activity,R.string.checkmeeting_error_3002);
//				} else if (nRet == 3003) {
//					errorTipDialog(activity,R.string.checkmeeting_error_3003);
//				} else if (nRet == 4109) {
//					errorTipDialog(activity,R.string.checkmeeting_error_4109);
//				} else if (nRet == 4103) {
//					errorTipDialog(activity,R.string.checkmeeting_error_4103);
//				} else {
//					errorTipDialog(activity,R.string.WaitingForNetwork);
//				}						
//			}});
	}*/

	/**public void joinBroadcast(final Activity activity,final String mid,final String pwd)
	{
		//public static void joinMeeting(final Activity activiy,final String httpServer,final String domain,final String nickName,final String account,final int userid,final String userPwd,final String mid,final String meetingPwd,final MeetingCallBack callback)
			@Override
			public void onSuccess(String mid, String pwd, int role,int meetingtype) {
				Utitlties.HideProgressDialog(activity);
				if(meetingtype != 11&&meetingtype != 12&&meetingtype != 13&&meetingtype != 14){
					errorTipDialog(activity,R.string.IsMeeting);
				}
			}

			@Override
			public void onError(int nRet) 
			{
				Utitlties.HideProgressDialog(activity);
				if (nRet == 4008) {
					inputMeetingPassward(activity,R.string.checkmeeting_error_5003,mid);
				} else if (nRet == 4110) {
					inputMeetingPassward(activity,R.string.checkmeeting_error_5004,mid);
				} else if (nRet == 4007) {
					errorTipDialog(activity,R.string.checkmeeting_error_5002);
				} else if (nRet == 3001) {
					errorTipDialog(activity,R.string.checkmeeting_error_3001);
				} else if (nRet == 3002) {
					errorTipDialog(activity,R.string.checkmeeting_error_3002);
				} else if (nRet == 3003) {
					errorTipDialog(activity,R.string.checkmeeting_error_5001);
				} else if (nRet == 4109) {
					errorTipDialog(activity,R.string.checkmeeting_error_4109);
				} else if (nRet == 4103) {
					errorTipDialog(activity,R.string.checkmeeting_error_4103);
				} else {
					errorTipDialog(activity,R.string.WaitingForNetwork);
				}						
			}});
	}*/

/**	public void inputMeetingPassward(final Activity activity,int nTipID,final String mid) {
		//Context  cont = this.getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		LayoutInflater layoutInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(R.layout.meeting_password, null);
		final EditText etpsd = (EditText) view.findViewById(R.id.et_psd);

		builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
				etpsd.getText().toString();
				//OnClickJoinToMeeting();
				//return etpsd.getText().toString();
				joinMeeting(activity,mid,etpsd.getText().toString());
			}

		});
		builder.setNegativeButton(getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();						
			}
		});
		AlertDialog adlg = builder.create();
		adlg.setView(view);
		adlg.setTitle(getString(nTipID));
		adlg.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface arg0) {
				Utilities.showKeyboard(etpsd);
			}
		});
		adlg.show();
		adlg.setCanceledOnTouchOutside(false);
	}*/

	public void errorTipDialog(final Activity activity,int errorTipID) {
		AlertDialog.Builder build = new AlertDialog.Builder(activity);
		build.setTitle(getString(R.string.link_tip));
		build.setMessage(getString(errorTipID));
		build.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
			}

		});
		build.show();
	}

	public static boolean isprivate(){//��ȡ�汾�ţ����Ƶİ汾������90000
		
		if(edition > 0)
			return true;
		
		try {
			PackageInfo packageInfo = applicationContext.getPackageManager().getPackageInfo(applicationContext.getPackageName(), 0);
			return (packageInfo.versionCode > 90000);
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}    
	}

	public static boolean isoem(){

		if(edition > 0)
			return true;

		try {
			PackageInfo packageInfo = applicationContext.getPackageManager().getPackageInfo(applicationContext.getPackageName(), 0);
			return (packageInfo.versionCode > 80000);
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}    
	}
}

/*
 * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.ui;

import java.net.URLDecoder;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;
import net.hockeyapp.android.Strings;
import net.hockeyapp.android.UpdateManager;
import net.hockeyapp.android.UpdateManagerListener;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.util.TypedValue;
import android.view.OrientationEventListener;
import android.widget.FrameLayout;

import com.utils.WeiyiMeeting;
import com.utils.WeiyiMeetingNotificationCenter;

import info.emm.messenger.BuildVars;
import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.MessagesStorage;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.messenger.RPCRequest.RPCRequestDelegate;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.UiUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;


@SuppressLint("ResourceAsColor") 
public class IntroActivity extends BaseActionBarActivity implements
WeiyiMeetingNotificationCenter.NotificationCenterDelegate  {
	private FrameLayout fra_contenter;
	IntroFragment introFragment = new IntroFragment();
	IntroFragmentForGE forGE = new IntroFragmentForGE();
	private AlertDialog builder = null;
	private Handler handler = new Handler(Looper.getMainLooper());


	@SuppressWarnings("unused")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("emm", "intro");
		ApplicationLoader.postInitApplication();

		WeiyiMeeting.getInstance().setMeetingNotifyIntent(
				new Intent(ApplicationLoader.applicationContext, IntroActivity.class));
		WeiyiMeetingNotificationCenter.getInstance().addObserver(this,WeiyiMeeting.AUTO_EXIT_MEETING);
		if(ApplicationLoader.edition == 1){
			UserConfig.isPublic = false;
			UserConfig.privateWebHttp = "ge.weiyicloud.cn";//"ge.weiyicloud.cn";
			UserConfig.isPersonalVersion = false;
			UserConfig.saveConfig(false);
			
//			Config.publicWebHttp = "192.168.0.5";
		}
		else if(ApplicationLoader.edition == 2){
			UserConfig.isPublic = false;
			UserConfig.privateWebHttp = "221.0.94.155";//"ge.weiyicloud.cn";
			UserConfig.privatePort = 8080;
			UserConfig.isPersonalVersion = false;
			UserConfig.saveConfig(false);
		}
		UiUtil.ActionHeight = getActionBarHeight();
		setContentView(R.layout.application_layout);
		fra_contenter = (FrameLayout) findViewById(R.id.container);
		
		Bundle bundle = getIntent().getBundleExtra("data");
		

//		if(ApplicationLoader.infragmentsStack.isEmpty()){
//			if(ApplicationLoader.edition == 0){				
//				presentFragment(introFragment, "", false);
//			}else if(ApplicationLoader.edition == 1){
//				presentFragment(forGE, "", false);
//			}
//		}else{
//			FragmentManager fm = getSupportFragmentManager();
//			FragmentTransaction fTrans = fm.beginTransaction();
//			fTrans.replace(R.id.container, ApplicationLoader.infragmentsStack.get(0));
//			fTrans.commit();
//		}
		if(bundle!=null){
			int relogin = bundle.getInt("relogin");
			if(relogin==1){
				PubPerLoginFragment perLoginFragment = new PubPerLoginFragment();
				presentFragment(perLoginFragment, "", false);
				return;
			}
		}
//		checkForUpdates();
		checkForCrashes();
//		createShortcut();
		if(MessagesController.getInstance().connectResult == 14)
		{
			//sam
			//����ʧ�ܣ�Ӧ����ʾ״̬, Ŀǰֻ�б���һ���豸��������һ�����
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			//	        builder.setMessage("You have logged in from another device.");
			builder.setMessage(LocaleController.getString("NotificationAnotherDevice", R.string.NotificationAnotherDevice));
			builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
			builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
			builder.show().setCanceledOnTouchOutside(true);
		}
		handleIntentemm(getIntent(), false, savedInstanceState != null);

		//		boolean b = UserConfig.readConfig();
		//		if(b)
		//		{
		//			ApplicationLoader.getInstance().joinMeeting(this, UserConfig.meetingID, UserConfig.meetingPwd);
		//		}
		m_Odetector = new MyOrientationDetector(this);

	}
	private MyOrientationDetector m_Odetector;


	public class MyOrientationDetector extends OrientationEventListener {
		public MyOrientationDetector(Context context) {
			super(context);
		}

		@Override
		public void onOrientationChanged(int orientation) {
			//			if (orientation > 350 || orientation < 10) { 
			//				reSetRotation();
			//			} else if (orientation > 80 && orientation < 100) { 
			//				reSetRotation();
			//			} else if (orientation > 170 && orientation < 190) { 
			//				reSetRotation();
			//			} else if (orientation > 260 && orientation < 280) { 
			//				reSetRotation();
			//			} else {
			//				return;
			//			}
		}
	} 
	@Override
	protected void onPause() {
		super.onPause();
		m_Odetector.disable();
	}
	@Override
	protected void onStart() {
//		if(getIntent().getStringExtra("username")!=null
//				&&!getIntent().getStringExtra("username").isEmpty()
//				&&getIntent().getStringExtra("pwd")!=null
//				&&!getIntent().getStringExtra("pwd").isEmpty()){
//			Log.d("xiao", "username and pwd is not empty");
//			String username = getIntent().getStringExtra("username");
//			String pwd = getIntent().getStringExtra("pwd");
//			outAppLogin(username, pwd);
//		}
		super.onStart();
		if(ApplicationLoader.infragmentsStack.isEmpty()){
			if(ApplicationLoader.edition == 1){
				presentFragment(forGE, "", false);
			}
			else
			{
				presentFragment(introFragment, "", false);
			}
		}else{
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction fTrans = fm.beginTransaction();
			fTrans.replace(R.id.container, ApplicationLoader.infragmentsStack.get(ApplicationLoader.infragmentsStack.size() - 1));
			fTrans.commit();
		}
	}
	private boolean joinMeeting()
	{
		try {
			ActivityInfo activityInfo = this.getPackageManager().getActivityInfo(
					new ComponentName(this, IntroActivity.class), PackageManager.GET_META_DATA);
			Bundle bundle1 = activityInfo.metaData;//����һ��Bundle����.
			if(bundle1==null)
				return false;
			//��bundle��ȡ,<meta-data>�趨��ֵ.
			String url = bundle1.getString("meeting.url");
			String name = bundle1.getString("meeting.name");
			if(!url.isEmpty())
			{
				if(!WeiyiMeeting.isInMeeting())
				{
					Intent intent = new Intent();				
					Uri uri = Uri.parse(url);
					intent.setData(uri);
					ApplicationLoader.getInstance().joinMeetingbyUrl(this, intent, name);
				}
				else
				{
					WeiyiMeeting.restoreMeeting(this);
				}
				return true;
			}
			return false;

		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}



	private void checkForCrashes() {
		CrashManagerListener listener = new CrashManagerListener() {

			public String getStringForResource(int resourceID) {
				switch (resourceID) {
				case Strings.CRASH_DIALOG_MESSAGE_ID:
					return getResources().getString(
							R.string.crash_dialog_message);
				case Strings.CRASH_DIALOG_NEGATIVE_BUTTON_ID:
					return getResources().getString(
							R.string.crash_dialog_negative_button);
				case Strings.CRASH_DIALOG_POSITIVE_BUTTON_ID:
					return getResources().getString(
							R.string.crash_dialog_positive_button);
				case Strings.CRASH_DIALOG_TITLE_ID:
					return getResources()
							.getString(R.string.crash_dialog_title);
				default:
					return null;
				}
			}

			@Override
			public boolean shouldAutoUploadCrashes() {
				// TODO Auto-generated method stub
				return true;
			}

		};

		CrashManager.register(this, "http://u.weiyicloud.com/",
				BuildVars.HOCKEY_APP_HASH, listener);


	}

	@Override
	public void onBackPressed() {
		FileLog.d("emm", "Launch onBackPressed");

		if(ApplicationLoader.infragmentsStack.size()<2)
		{    		
			//��С����������˳�����
			Log.e("emm", "��С��");
			ApplicationLoader.infragmentsStack.clear();
			Intent intent = new Intent();
			intent.setAction("android.intent.action.MAIN");
			intent.addCategory("android.intent.category.HOME");
			startActivity(intent);
		}
		else 
		{
			BaseFragment lastFragment = ApplicationLoader.infragmentsStack.get(ApplicationLoader.infragmentsStack.size() - 1);
			if (lastFragment.onBackPressed()) 
			{
				FileLog.d("emm", "close fragment");
				lastFragment.finishFragment();
				// FragmentManager fm = getSupportFragmentManager();
				//FragmentTransaction fTrans = fm.beginTransaction();
				//fTrans.replace(R.id.container, ApplicationLoader.infragmentsStack.get(0));
				//fTrans.commit();
			}
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		Utilities.HideProgressDialog(this);
	}


	private void checkForUpdates() {
		// if (BuildVars.DEBUG_VERSION)
		{
			UpdateManagerListener listener = new UpdateManagerListener() {
				public String getStringForResource(int resourceID) {
					switch (resourceID) {
					case Strings.UPDATE_MANDATORY_TOAST_ID:
						return getResources().getString(
								R.string.update_mandatory_toast);
					case Strings.UPDATE_DIALOG_TITLE_ID:
						return getResources().getString(
								R.string.update_dialog_title);
					case Strings.UPDATE_DIALOG_MESSAGE_ID:
						return getResources().getString(
								R.string.update_dialog_message);
					case Strings.UPDATE_DIALOG_NEGATIVE_BUTTON_ID:
						return getResources().getString(
								R.string.update_dialog_negative_button);
					case Strings.UPDATE_DIALOG_POSITIVE_BUTTON_ID:
						return getResources().getString(
								R.string.update_dialog_positive_button);
					case Strings.DOWNLOAD_FAILED_DIALOG_TITLE_ID:
						return getResources().getString(
								R.string.download_failed_dialog_title);
					case Strings.DOWNLOAD_FAILED_DIALOG_MESSAGE_ID:
						return getResources().getString(
								R.string.download_failed_dialog_message);
					case Strings.DOWNLOAD_FAILED_DIALOG_NEGATIVE_BUTTON_ID:
						return getResources()
								.getString(
										R.string.download_failed_dialog_negative_button);
					case Strings.DOWNLOAD_FAILED_DIALOG_POSITIVE_BUTTON_ID:
						return getResources()
								.getString(
										R.string.download_failed_dialog_positive_button);
					case Strings.UPDATE_VIEW_UPDATE_BUTTON_ID:
						return getResources().getString(
								R.string.update_view_update_button);
					case Strings.UPDATE_VIEW_SOURCE_FAILED_ID:
						return getResources().getString(
								R.string.update_view_source_failed);

					default:
						return null;
					}
				}
			};
			
			// UpdateManager.register(this,
			// "http://192.168.0.99:8080/update/php/public/",
			// BuildVars.HOCKEY_APP_HASH, listener);
//			UpdateManager.register(this, "http://u.weiyicloud.com/",
//					BuildVars.HOCKEY_APP_HASH, listener);
			if(ApplicationLoader.edition==1){
				UpdateManager.register(this, "http://u.weiyicloud.com/", BuildVars.HOCKEY_APP_HASH_GE, listener);
			}else if(ApplicationLoader.edition==3){
				if (UserConfig.privateWebHttp != null && !UserConfig.privateWebHttp.isEmpty())
				{
					
					String url = UserConfig.privateWebHttp + ":82/";
					if(!url.toLowerCase().startsWith("http://"))
						url = "http://" + url;
					
					UpdateManager.register(this, url, BuildVars.HOCKEY_APP_HASH, listener);
				}
			}else{				
				UpdateManager.register(this, "http://u.weiyicloud.com/", BuildVars.HOCKEY_APP_HASH, listener);
			}
		}
	}

	public void showActionBar() {
		//prepareForHideShowActionBar();
		getSupportActionBar().show();
	}
	public void updateActionBar() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar == null) {
			return;
		}
		BaseFragment currentFragment = null;
		if (!ApplicationLoader.infragmentsStack.isEmpty()) {
			currentFragment = ApplicationLoader.infragmentsStack.get(ApplicationLoader.infragmentsStack.size() - 1);
		}

		boolean canApplyLoading = true;// jenf for connect status
		if (currentFragment != null ) 
		{
			currentFragment.applySelfActionBar();
			//canApplyLoading = false;
		}

	}
	public int getActionBarHeight() {
		int actionBarHeight = 0;
		TypedValue tv = new TypedValue();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv,
					true))
				actionBarHeight = TypedValue.complexToDimensionPixelSize(
						tv.data, getResources().getDisplayMetrics());
		} else {
			actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
					getResources().getDisplayMetrics());
		}
		return actionBarHeight;
	}
	public void presentFragment(BaseFragment fragment, boolean removeLast) {
		presentFragment(fragment, "", removeLast, false);
	}

	public void presentFragment(BaseFragment fragment, String tag, boolean bySwipe) {
		presentFragment(fragment, tag, false, bySwipe);
	}

	public void presentFragment(BaseFragment fragment, String tag, boolean removeLast, boolean bySwipe) {
		if (getCurrentFocus() != null) {
			Utilities.hideKeyboard(getCurrentFocus());
		}

		FileLog.d("emm", "LaunchActivity presentFragment:" + fragment.toString() + " " + tag.toString());

		if (!fragment.onFragmentCreate()) {
			return;
		}
		BaseFragment current = null;
		if (!ApplicationLoader.infragmentsStack.isEmpty()) {
			current = ApplicationLoader.infragmentsStack.get(ApplicationLoader.infragmentsStack.size() - 1);
		}
		if (current != null) {
			current.willBeHidden();
		}
		FileLog.d("emm", "LaunchActivity presentFragment 1");
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction fTrans = fm.beginTransaction();
		if (removeLast && current != null) {
			ApplicationLoader.infragmentsStack.remove(ApplicationLoader.infragmentsStack.size() - 1);
			current.onFragmentDestroy();
		}
		FileLog.d("emm", "LaunchActivity presentFragment 2");
		SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig_" + UserConfig.clientUserId, Activity.MODE_PRIVATE);
		boolean animations = preferences.getBoolean("view_animations", true);
		if (animations) {
			if (bySwipe) {
				fTrans.setCustomAnimations(R.anim.slide_left, R.anim.no_anim);
			} else {
				fTrans.setCustomAnimations(R.anim.scale_in, R.anim.no_anim);
			}
		}
		try {
			fTrans.replace(R.id.container, fragment, tag);
			fTrans.commitAllowingStateLoss();
		} catch (Exception e) {
			FileLog.e("emm", e);
		}
		FileLog.d("emm", "LaunchActivity presentFragment 3");
		ApplicationLoader.infragmentsStack.add(fragment);
	}

	public void removeFromStack(BaseFragment fragment) 
	{
		if(fragment instanceof mainFramgMent)
			FileLog.d("emm", "shouldn't work here");
		ApplicationLoader.infragmentsStack.remove(fragment);
		fragment.onFragmentDestroy();
	}

	public void finishFragment(boolean bySwipe) 
	{
		if (getCurrentFocus() != null) {
			Utilities.hideKeyboard(getCurrentFocus());
		}	      

		if(ApplicationLoader.infragmentsStack.isEmpty())
			return;

		BaseFragment fragment = ApplicationLoader.infragmentsStack.get(ApplicationLoader.infragmentsStack.size() - 1);

		fragment.onFragmentDestroy();
		BaseFragment prev = ApplicationLoader.infragmentsStack.get(ApplicationLoader.infragmentsStack.size() - 2);
		if(prev!=null)
		{
			if(!isFinishing()){
				FragmentManager fm = getSupportFragmentManager();
				FragmentTransaction fTrans = fm.beginTransaction();
				SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig_" + UserConfig.clientUserId, Activity.MODE_PRIVATE);
				boolean animations = preferences.getBoolean("view_animations", true);
				if (animations) {
					if (bySwipe) {
						fTrans.setCustomAnimations(R.anim.no_anim_show, R.anim.slide_right_away);
					} else {
						fTrans.setCustomAnimations(R.anim.no_anim_show, R.anim.scale_out);
					}
				}
				
				fTrans.replace(R.id.container, prev, prev.getTag());
				//FileLog.e("emm", "finishFragment 1**************");
				
				fTrans.remove(fragment);	        
				fTrans.commitAllowingStateLoss();
				ApplicationLoader.infragmentsStack.remove(ApplicationLoader.infragmentsStack.size() - 1);        
				fm.executePendingTransactions();
			}
		}

	}

	@Override
	protected void onResume() {
		supportInvalidateOptionsMenu();	    
		WeiyiMeeting.getInstance().setMeetingNotifyIntent(
				new Intent(ApplicationLoader.applicationContext, IntroActivity.class));
		super.onResume();
		m_Odetector.enable();
	}
	//	    @Override
	//	    protected void onStop() {
	//	    	ApplicationLoader.infragmentsStack.clear();
	//	    	super.onStop();
	//	    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if(ApplicationLoader.infragmentsStack.isEmpty()){			
			presentFragment(introFragment, "", false);
		}else{
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction fTrans = fm.beginTransaction();
			fTrans.replace(R.id.container, ApplicationLoader.infragmentsStack.get(0));
			fTrans.commit();
		}
		handleIntentemm(intent, true, false);
	}
	String account="";
	String account_pwd="";
	private boolean parseUrl(String protocol)
	{
		String strProtocol = protocol;
		String mid="";
		String pwd="";
		String httpServer="";
		String httpPort="";
		int bAutoExitWeiyi = 0;
		int bHasSensor = 0;
		int bScreenRotation= 0;
		int hideme = 0;

		int bAutoEntermeeting = 1;
		if (strProtocol.isEmpty())
			return false;


		Log.e("emm", "enterMeeting strProtocol="+strProtocol);
		Uri url = Uri.parse(strProtocol);
		if (url != null) {
			// weiyi://192.168.0.121:81/123456789/4321
			if (strProtocol.startsWith("weiyi://")) 
			{
				httpServer = strProtocol.replace("weiyi://", "");
				if(httpServer.isEmpty())
					return false;
				if(httpServer.startsWith("start?")||httpServer.startsWith("start/?"))
				{
//					httpServer = Uri.decode(httpServer);
					String tempHttpPort = "";
					String tempProtocol = httpServer.replace("start?", ""); 
					tempProtocol = tempProtocol.replace("start/?", ""); 
					String[] kv_unsplit = tempProtocol.split("&");
					for(int i=0;i<kv_unsplit.length;i++)
					{
						if(kv_unsplit[i].split("=")[0].toLowerCase().equalsIgnoreCase("ip"))
						{
							httpServer = kv_unsplit[i].split("=")[1];
							if(httpServer.contains(":"))
							{
								int index = httpServer.indexOf(":");
								if(index>0)
								{
									tempHttpPort = httpServer.substring(index+1);
									httpServer = httpServer.substring(0, index);
								}
							}
						}
						
						if(kv_unsplit[i].split("=")[0].toLowerCase().equalsIgnoreCase("port"))
						{
							if(kv_unsplit[i].split("=")[1] != "")
							{
								httpPort = kv_unsplit[i].split("=")[1];
							}
						}
						
						if(kv_unsplit[i].split("=")[0].toLowerCase().equalsIgnoreCase("account"))
						{
							account = kv_unsplit[i].split("=")[1];
						}
						
						if(kv_unsplit[i].split("=")[0].toLowerCase().equalsIgnoreCase("isAutoEntermeeting"))
						{
							bAutoEntermeeting = Integer.parseInt(kv_unsplit[i].split("=")[1]);
						}
						
						if(kv_unsplit[i].split("=")[0].toLowerCase().equalsIgnoreCase("linkurl"))
						{
							try {
								WeiyiMeeting.setLinkUrl(URLDecoder.decode(kv_unsplit[i].split("=")[1], "UTF-8"));
							} catch (Exception e) {
							}
						}
						
						if(kv_unsplit[i].split("=")[0].toLowerCase().equalsIgnoreCase("linkname"))
						{
							try {
								WeiyiMeeting.setLinkName(kv_unsplit[i].split("=")[1]);
							} catch (Exception e) {
							}
						}
					}
					if(httpPort.isEmpty() && tempHttpPort.isEmpty())
					{
						httpPort = "80";
					}
					else if(!tempHttpPort.isEmpty())
					{
						httpPort = tempHttpPort;
					}
				}
				else
				{
					httpServer = httpServer.substring(0,httpServer.indexOf("/"));
					
					if(!httpServer.isEmpty())
					{
						int index  = httpServer.indexOf(":");
						if(index==-1)
							httpPort = "80";
						else	
						{	
							httpPort = httpServer.substring(index+1);
							httpServer = httpServer.substring(0, index);
						}
					}
					
					String UrlPath = url.getPath();
					String[] ss = UrlPath.split("/");
					
					if (ss.length > 9) {
						try {
							WeiyiMeeting.setLinkUrl(URLDecoder.decode(ss[9], "UTF-8"));
							WeiyiMeeting.setLinkName(ss[8]);
						} catch (Exception e) {
						}
					}
					
					if(ss.length>16)
					{		
						try {
							account=ss[16];
						} catch (Exception e) {
						}								
					}
					
					if(ss.length>18)
					{		
						try {
							//auto entermeeting
							bAutoEntermeeting  = Integer.parseInt(ss[18]);
						} catch (Exception e) {
						}								
					}
				}
						

				Log.e("emm", "httpUri==" + httpServer);
			}//end---if(strProtocol.startsWith("weiyi://"))
			
		}//end---if(url != null)
		if(bAutoEntermeeting == 1)//ȱʡ��1 ���Զ����������˺�û��
			return true;

		UserConfig.isPublic = false;
		UserConfig.privateWebHttp=httpServer;

		try {
			//auto entermeeting
			UserConfig.privatePort=Integer.parseInt(httpPort);
		} catch (Exception e) {
		}		

		UserConfig.priaccount = account;
		UserConfig.saveConfig(false);

		onNextAction();
		return false;
	}



	public void ShowAlertDialog(final Activity activity, final String message) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!IntroActivity.this.isFinishing()) {
					AlertDialog.Builder builder = new AlertDialog.Builder(activity);
					builder.setTitle(LocaleController.getString("AppName", R.string.app_name));
					builder.setMessage(message);
					builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
					builder.show().setCanceledOnTouchOutside(true);
				}
			}
		});
	}

	public void onNextAction() {
		needShowProgress();
		ConnectionsManager.getInstance().CheckLogin("", account, account_pwd,new RPCRequestDelegate() {

			@Override
			public void run(final info.emm.messenger.TLObject response,
					final info.emm.messenger.TLRPC.TL_error error) {
				needHideProgress();
				if (error != null) 
				{

					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() 
						{
							int result = error.code;
							if( result == 1 )
							{
								//�ʺ�δ����

								ShowAlertDialog(IntroActivity.this,ApplicationLoader.applicationContext.getString(R.string.AccountNoActication));

							}
							else if( result == 2)
							{
								//�ʺ��Ѿ�����

								ShowAlertDialog(IntroActivity.this,ApplicationLoader.applicationContext.getString(R.string.AccountFreeze));

							}
							else if( result == 3)
							{
								//�����û���Ϣ�豸ʧ��

								ShowAlertDialog(IntroActivity.this,ApplicationLoader.applicationContext.getString(R.string.DeviceUpdateFaild));

							}
							else if( result == 5)
							{
								//�������

								ShowAlertDialog(IntroActivity.this,ApplicationLoader.applicationContext.getString(R.string.PasswordError));

							}
							else if( result == 6)
							{
								//�ʺŴ���

								ShowAlertDialog(IntroActivity.this,ApplicationLoader.applicationContext.getString(R.string.AccountError));

							}         
							else if( result == -2)
							{

								ShowAlertDialog(IntroActivity.this,ApplicationLoader.applicationContext.getString(R.string.check_httpserver));

							}
						}
					});
					return;
				}

				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						//��¼�ɹ�
						//�����ݿ���˼��ʲô��xueqiang ask
						final TLRPC.TL_auth_authorization res = (TLRPC.TL_auth_authorization)response;       				 
						UserConfig.clearConfig();
						MessagesStorage.getInstance().cleanUp();
						//MessagesController.getInstance().cleanUp();
						UserConfig.currentUser = res.user;
						UserConfig.clientActivated = true;
						UserConfig.clientUserId = res.user.id;
						UserConfig.isPersonalVersion = false;
						UserConfig.priaccount = account;
						UserConfig.saveConfig(true);
						MessagesStorage.getInstance().openDatabase();
						MessagesController.getInstance().users.put(res.user.id, res.user);

						FileLog.d("emm", "check login result:" + UserConfig.clientUserId + " sid:" + UserConfig.currentUser.sessionid);

						//sam
						try {
							AccountManager accountManager = AccountManager.get(ApplicationLoader.applicationContext);
							Account myAccount  = new Account(account, "info.emm.weiyicloud.account");
							accountManager.addAccountExplicitly(myAccount, account_pwd, null);
						} catch (Exception e) {
							e.printStackTrace();
							FileLog.e("emm", e);
						}

						//ApplicationLoader.infragmentsStack.remove(ApplicationLoader.infragmentsStack.size()-1);
						needFinishActivity();
					}
				});
			}
		});

	}
	public void needFinishActivity() {
		Intent intent2 = new Intent(this, LaunchActivity.class);
		startActivity(intent2);
		Utilities.HideProgressDialog(this);
		finish();
	}
	public void needShowProgress() {
		Utilities.ShowProgressDialog(this, getResources().getString(R.string.Loading));
	}
	public void needHideProgress() {
		Utilities.HideProgressDialog(this);
	}

	private boolean handleIntentemm(Intent intent, boolean isNew,
			boolean restore) {
		boolean bfrom_title = false;
		if (intent == null) {
			Log.i("rebuild", "intent null");
		} else if (intent.getAction() != null) {
			Log.i("rebuild", intent.getAction());
		}
		if(intent!=null)
		{
			Log.e("emm", "intent not null");
		}
		if(restore)
		{
			Log.e("emm", "restore is true");
		}
		boolean isExtraApp = joinMeeting();
		if(!isExtraApp)
		{
			if (intent != null && !restore) {
				Log.e("emm", "joinMeetingbyUrl");
				Uri uri = intent.getData();
				if (uri != null) {		
					String url = uri.toString();
					if(parseUrl(url)){
						ApplicationLoader.getInstance().joinMeetingbyUrl(this, intent, UserConfig.getNickName());
					}
				}				
				else
				{
					ApplicationLoader.getInstance().joinMeetingbyUrl(this, intent, UserConfig.getNickName());
				}
			}
		}
		return false;
	}


	@Override
	public void didReceivedNotification(int id, Object... args) {
		if(id ==WeiyiMeeting.AUTO_EXIT_MEETING)
		{
			Log.e("emm", "lanchAcitivity auto exit meeting**********************");
			onBackPressed();
		}
	}
	public void createShortCutInternal() {
		
		if(ApplicationLoader.edition == 3)
			return;
		
		String Tips = ApplicationLoader.applicationContext
				.getString(R.string.Tips);
		String msg = LocaleController.getString("IsCreateShortCut",
				R.string.IsCreateShortCut);
		String ok = ApplicationLoader.applicationContext.getString(R.string.OK);
		String Cancel = ApplicationLoader.applicationContext
				.getString(R.string.Cancel);
		builder = new AlertDialog.Builder(this)
		.setTitle(Tips)
		.setMessage(msg)
		.setPositiveButton(ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (!hasShortCut(IntroActivity.this)) {
					// wangxm todo,����һ����ʾ����һ���Ƿ񴴽������ݷ�ʽ������
					// ������ݷ�ʽ��Intent
					Intent shortcutIntent = new Intent(
							"com.android.launcher.action.INSTALL_SHORTCUT");
					// �������ظ�����
					shortcutIntent.putExtra("duplicate", false);
					// ��ݷ�ʽ����
					// shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
					// getString(R.string.shortcut_name));
					String msg = LocaleController.getString("AppName",
							R.string.AppName);
					shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
							msg);
					// ���ͼƬ
					Parcelable icon = Intent.ShortcutIconResource
							.fromContext(getApplicationContext(),
									R.drawable.ic_start);
					shortcutIntent.putExtra(
							Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
					// ������ͼƬ�����еĳ��������
					shortcutIntent.putExtra(
							Intent.EXTRA_SHORTCUT_INTENT, new Intent(
									getApplicationContext(),
									LaunchActivity.class));
					sendBroadcast(shortcutIntent);
				}
			}
		})
		.setNegativeButton(Cancel,
				new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog,
					int which) {

			}
		}).show();
	}

	public boolean hasShortCut(Context context) {
		String url = "";
		if (android.os.Build.VERSION.SDK_INT < 8) {
			url = "content://com.android.launcher.settings/favorites?notify=true";
		} else {
			url = "content://com.android.launcher2.settings/favorites?notify=true";
		}
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(Uri.parse(url), null, "title=?",
				new String[] { context.getString(R.string.AppName) }, null);

		if (cursor != null && cursor.moveToFirst()) {
			cursor.close();
			return true;
		}
		return false;
	}

	private void createShortcut() {
		if (!UserConfig.firstTimeInstall) {
			UserConfig.firstTimeInstall = true;
			UserConfig.saveConfig(false);
			createShortCutInternal();
		}
	}

	/***
	 * �ⲿapp��¼
	 * @param userName
	 * @param pwd
	 */
	private void outAppLogin(final String  userName,final String pwd){
		needShowProgress();
		ConnectionsManager.getInstance().CheckLogin("", userName, pwd,new RPCRequestDelegate() {

			@Override
			public void run(final info.emm.messenger.TLObject response,
					final info.emm.messenger.TLRPC.TL_error error) {

				needHideProgress();
				if (error != null) 
				{

					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() 
						{
							int result = error.code;
							if( result == 1 )
							{
								//�ʺ�δ����

								ShowAlertDialog(IntroActivity.this,ApplicationLoader.applicationContext.getString(R.string.AccountNoActication));

							}
							else if( result == 2)
							{
								//�ʺ��Ѿ�����

								ShowAlertDialog(IntroActivity.this,ApplicationLoader.applicationContext.getString(R.string.AccountFreeze));

							}
							else if( result == 3)
							{
								//�����û���Ϣ�豸ʧ��

								ShowAlertDialog(IntroActivity.this,ApplicationLoader.applicationContext.getString(R.string.DeviceUpdateFaild));

							}
							else if( result == 5)
							{
								//�������

								ShowAlertDialog(IntroActivity.this,ApplicationLoader.applicationContext.getString(R.string.PasswordError));

							}
							else if( result == 6)
							{
								//�ʺŴ���

								ShowAlertDialog(IntroActivity.this,ApplicationLoader.applicationContext.getString(R.string.AccountError));

							}         
							else if( result == -2)
							{

								ShowAlertDialog(IntroActivity.this,ApplicationLoader.applicationContext.getString(R.string.check_httpserver));

							}
						}
					});
					return;
				}

				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						//��¼�ɹ�
						//�����ݿ���˼��ʲô��xueqiang ask
						final TLRPC.TL_auth_authorization res = (TLRPC.TL_auth_authorization)response;       				 
						UserConfig.clearConfig();
						MessagesStorage.getInstance().cleanUp();
						//MessagesController.getInstance().cleanUp();
						UserConfig.currentUser = res.user;
						UserConfig.clientActivated = true;
						UserConfig.clientUserId = res.user.id;
						UserConfig.isPersonalVersion = false;
						UserConfig.priaccount = userName;
						UserConfig.saveConfig(true);
						MessagesStorage.getInstance().openDatabase();
						MessagesController.getInstance().users.put(res.user.id, res.user);

						FileLog.d("emm", "check login result:" + UserConfig.clientUserId + " sid:" + UserConfig.currentUser.sessionid);

						//sam
						try {
							AccountManager accountManager = AccountManager.get(ApplicationLoader.applicationContext);
							Account myAccount  = new Account(userName, "info.emm.weiyicloud.account");
							accountManager.addAccountExplicitly(myAccount, pwd, null);
						} catch (Exception e) {
							e.printStackTrace();
							FileLog.e("emm", e);
						}

						ApplicationLoader.infragmentsStack.remove(ApplicationLoader.infragmentsStack.size()-1);
						needFinishActivity();

					}
				});





			}
		});
	}

}

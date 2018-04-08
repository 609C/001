/*
\ * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.ui;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;
import net.hockeyapp.android.Strings;
import net.hockeyapp.android.UpdateManager;
import net.hockeyapp.android.UpdateManagerListener;
import info.emm.LocalData.Config;
import info.emm.PhoneFormat.PhoneFormat;
import info.emm.im.directsending.DirectSending_Fragment;
import info.emm.im.meeting.MainFragment_Meeting;
import info.emm.messenger.BuildVars;
import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MediaController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;


import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.objects.MessageObject;
import info.emm.services.UEngine;
import info.emm.ui.Views.BaseFragment;
import info.emm.ui.Views.NotificationView;
import info.emm.utils.ConstantValues;
import info.emm.utils.UiUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.ArrayList;


//import com.google.android.gms.internal.bu;





import com.utils.WeiyiMeeting;
import com.utils.WeiyiMeetingNotificationCenter;

public class LaunchActivity extends BaseActionBarActivity implements
NotificationCenter.NotificationCenterDelegate,
MessagesActivity.MessagesActivityDelegate,WeiyiMeetingNotificationCenter.NotificationCenterDelegate {
	private boolean finished = false;
	private NotificationView notificationView;
	private Uri photoPath = null;
	private String videoPath = null;
	private String sendingText = null;
	private String documentPath = null;
	private Uri[] imagesPathArray = null;
	private String[] documentsPathArray = null;
	private ArrayList<TLRPC.User> contactsToSend = null;
	private int currentConnectionState;
	private View statusView;
	private View backStatusButton;
	private View statusBackground;
	private TextView statusText;
	private View containerView;
	private BaseFragment presentFragment = null;
	private Handler handler = new Handler(Looper.getMainLooper());
	public boolean topAudioCall = false;
	public boolean isRetransmit = false;
	private BroadcastsHandler headsetPlugHandler = null;
	private AlertDialog builder = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.e("emm", "launchactivity oncreate******************");

		//�˴�Ӧ��ʹ��saveinstance�ķ�ʽ���������յȶ��������⣬���ڲ��ָܻ������⣬��һ��fragmentchildmanager��
		super.onCreate(null);
		UiUtil.ActionHeight = getActionBarHeight();
		if (savedInstanceState != null)
			FileLog.e(
					"emm",
					"LaunchActivity onCreate"
							+ ((savedInstanceState == null) ? ""
									: savedInstanceState.toString())
									+ this.toString());
		else
			FileLog.d("emm",
					"LaunchActivity onCreate: savedInstanceState is null");


		ApplicationLoader.postInitApplication();
		this.setTheme(R.style.Theme_EMM);
		getWindow().setBackgroundDrawableResource(R.drawable.transparent);
		getWindow().setFormat(PixelFormat.RGB_565);		   

		// �˳������Ƿ��ߵ�¼���̣���¼�����������������Ĵ�����
		if (!UserConfig.clientActivated) {//����
			WeiyiMeeting.getInstance().setMeetingNotifyIntent(
					new Intent(ApplicationLoader.applicationContext, IntroActivity.class));
			Intent intent = getIntent();
			if (intent != null
					&& intent.getAction() != null
					&& (Intent.ACTION_SEND.equals(intent.getAction()) || intent
							.getAction().equals(Intent.ACTION_SEND_MULTIPLE))) {
				finish();
				return;
			}
			intent.setClass(this, IntroActivity.class);
			startActivity(intent);
			finish();
			return;
		}else{
			WeiyiMeeting.getInstance().setMeetingNotifyIntent(
					new Intent(ApplicationLoader.applicationContext, LaunchActivity.class));
		}

		int resourceId = getResources().getIdentifier("status_bar_height",
				"dimen", "android");
		if (resourceId > 0) {
			Utilities.statusBarHeight = getResources().getDimensionPixelSize(
					resourceId);
		}      

		NotificationCenter.getInstance().postNotificationName(702, this);
		currentConnectionState = ConnectionsManager.getInstance().connectionState;
		for (BaseFragment fragment : ApplicationLoader.fragmentsStack) {
			if (fragment.fragmentView != null) {
				ViewGroup parent = (ViewGroup) fragment.fragmentView 
						.getParent();
				if (parent != null) {
					parent.removeView(fragment.fragmentView);
				}
				fragment.fragmentView = null;
			}
			fragment.parentActivity = this;
			// FileLog.e("emm", "LaunchActivity onCreate: reset parent " );
		}
		setContentView(R.layout.application_layout);
		NotificationCenter.getInstance().addObserver(this, 1234);
		NotificationCenter.getInstance().addObserver(this, 658);
		NotificationCenter.getInstance().addObserver(this, 701);
		NotificationCenter.getInstance().addObserver(this, 702);
		NotificationCenter.getInstance().addObserver(this, 703);
		NotificationCenter.getInstance().addObserver(this,GalleryImageViewer.needShowAllMedia);
		NotificationCenter.getInstance().addObserver(this,MessagesController.create_group_final);


		WeiyiMeetingNotificationCenter.getInstance().addObserver(this,WeiyiMeeting.AUTO_EXIT_MEETING);

		statusView =LayoutInflater.from(this).inflate(
				R.layout.updating_state_layout, null);

		statusBackground = statusView.findViewById(R.id.back_button_background);
		backStatusButton = statusView.findViewById(R.id.back_button);
		containerView = findViewById(R.id.container);
		statusText = (TextView) statusView.findViewById(R.id.status_text);
		int	productmodel = UserConfig.currentProductModel;
		statusBackground.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (ApplicationLoader.fragmentsStack.size() > 1) {  //
					onBackPressed();
				}
			}
		});

		if (ApplicationLoader.fragmentList.isEmpty()) {
			MainAddress mainAddress = new MainAddress();
			mainAddress.onFragmentCreate();

			MessagesActivity messages = new MessagesActivity();
			messages.onFragmentCreate();

			MainFragment_Meeting meeting = new MainFragment_Meeting();
			meeting.onFragmentCreate();
			DirectSending_Fragment directSending = new DirectSending_Fragment();
			directSending.onFragmentCreate();//qxm yincang

			if(productmodel == 0){
				ApplicationLoader.fragmentList.add(meeting);
				ApplicationLoader.fragmentList.add(mainAddress);
				ApplicationLoader.fragmentList.add(messages);
				Log.e("mme", "product 0");
			}else{
				ApplicationLoader.fragmentList.add(meeting);
				ApplicationLoader.fragmentList.add(directSending);
				ApplicationLoader.fragmentList.add(mainAddress);
				ApplicationLoader.fragmentList.add(messages);
				Log.e("mme", "product else ");
			}
			FileLog.d("emm", "add three fragment");  
		}
		if (ApplicationLoader.fragmentsStack.isEmpty()) {
			mainFramgMent fragment = new mainFramgMent();		
			presentFragment = fragment;
			fragment.onFragmentCreate();
			ApplicationLoader.fragmentsStack.add(fragment);
		}		

		headsetPlugHandler = new BroadcastsHandler();
		this.registerReceiver(headsetPlugHandler, new IntentFilter(Intent.ACTION_HEADSET_PLUG));

		handleIntent(getIntent(), false, savedInstanceState != null);
		if(savedInstanceState!=null)
		{
			Log.e("emm", "saveinstance has value="+savedInstanceState.toString());
		}
		if(getIntent()!=null)
		{	
			Log.e("emm", "saveinstance" + getIntent().getAction());			
		}
//		checkForUpdates();

		Log.i("rebuild", "handleIntentemm begin");

		//handleIntentemm(getIntent(), false, savedInstanceState != null);

		Log.i("rebuild", "handleIntentemm end");


		// UEngine.getInstance().getSoundService().startRingMusic(RingType.RING_CALL_OUT);
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		// xiaoyang

		super.onStart();
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

	// @SuppressWarnings("unchecked")
	// private void prepareForHideShowActionBar() {
	// try {
	// Class firstClass = getSupportActionBar().getClass();
	// Class aClass = firstClass.getSuperclass();
	// if (aClass == android.support.v7.app.ActionBar.class) {
	// Method method =
	// firstClass.getDeclaredMethod("setShowHideAnimationEnabled",
	// boolean.class);
	// method.invoke(getSupportActionBar(), false);
	// } else {
	// Field field = aClass.getDeclaredField("mActionBar");
	// field.setAccessible(true);
	// Method method =
	// field.get(getSupportActionBar()).getClass().getDeclaredMethod("setShowHideAnimationEnabled",
	// boolean.class);
	// method.invoke(field.get(getSupportActionBar()), false);
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	public void showActionBar() {
		// prepareForHideShowActionBar();
		getSupportActionBar().show();
	}

	public void hideActionBar() {
		// prepareForHideShowActionBar();
		getSupportActionBar().hide();
	}

	private void handleIntent(Intent intent, boolean isNew, boolean restore) {
		boolean pushOpened = false;
		Integer push_user_id = 0;
		Integer push_chat_id = 0;
		Integer push_enc_id = 0;
		Integer push_meet_id = 0;// wangxm add for meetcreate alert
		Integer open_settings = 0;
		photoPath = null;
		videoPath = null;
		sendingText = null;
		documentPath = null;
		imagesPathArray = null;
		documentsPathArray = null;
		if(intent==null)
		{
			Log.e("emm", "intent is null***********************");
		}

		MessagesController.getInstance().releaseNotificationData();
		if (intent != null && intent.getAction() != null && !restore) {
			if (Intent.ACTION_SEND.equals(intent.getAction())) {
				boolean error = false;
				String type = intent.getType();
				if (type != null && type.equals("text/plain")) {
					String text = intent.getStringExtra(Intent.EXTRA_TEXT);
					String subject = intent
							.getStringExtra(Intent.EXTRA_SUBJECT);

					if (text != null && text.length() != 0) {
						if ((text.startsWith("http://") || text
								.startsWith("https://"))
								&& subject != null
								&& subject.length() != 0) {
							text = subject + "\n" + text;
						}
						sendingText = text;
					} else {
						error = true;
					}
				} else if (type != null
						&& type.equals(ContactsContract.Contacts.CONTENT_VCARD_TYPE)) {
					try {
						Uri uri = (Uri) intent.getExtras().get(
								Intent.EXTRA_STREAM);
						if (uri != null) {
							ContentResolver cr = getContentResolver();
							InputStream stream = cr.openInputStream(uri);

							String name = null;
							String nameEncoding = null;
							String nameCharset = null;
							ArrayList<String> phones = new ArrayList<String>();
							BufferedReader bufferedReader = new BufferedReader(
									new InputStreamReader(stream, "UTF-8"));
							String line = null;
							while ((line = bufferedReader.readLine()) != null) {
								String[] args = line.split(":");
								if (args.length != 2) {
									continue;
								}
								if (args[0].startsWith("FN")) {
									String[] params = args[0].split(";");
									for (String param : params) {
										String[] args2 = param.split("=");
										if (args2.length != 2) {
											continue;
										}
										if (args2[0].equals("CHARSET")) {
											nameCharset = args2[1];
										} else if (args2[0].equals("ENCODING")) {
											nameEncoding = args2[1];
										}
									}
									name = args[1];
									if (nameEncoding != null
											&& nameEncoding
											.equalsIgnoreCase("QUOTED-PRINTABLE")) {
										while (name.endsWith("=")
												&& nameEncoding != null) {
											name = name.substring(0,
													name.length() - 1);
											line = bufferedReader.readLine();
											if (line == null) {
												break;
											}
											name += line;
										}
										byte[] bytes = Utilities
												.decodeQuotedPrintable(name
														.getBytes());
										if (bytes != null && bytes.length != 0) {
											String decodedName = new String(
													bytes, nameCharset);
											if (decodedName != null) {
												name = decodedName;
											}
										}
									}
								} else if (args[0].startsWith("TEL")) {
									String phone = PhoneFormat
											.stripExceptNumbers(args[1], true);
									if (phone.length() > 0) {
										phones.add(phone);
									}
								}
							}
							if (name != null && !phones.isEmpty()) {
								contactsToSend = new ArrayList<TLRPC.User>();
								for (String phone : phones) {
									TLRPC.User user = new TLRPC.TL_userContact();
									user.phone = phone;
									user.first_name = name;
									user.last_name = "";
									user.id = 0;
									contactsToSend.add(user);
								}
							}
						} else {
							error = true;
						}
					} catch (Exception e) {
						FileLog.e("emm", e);
						error = true;
					}
				} else {
					Parcelable parcelable = intent
							.getParcelableExtra(Intent.EXTRA_STREAM);
					if (parcelable == null) {
						return;
					}
					String path = null;
					if (!(parcelable instanceof Uri)) {
						parcelable = Uri.parse(parcelable.toString());
					}
					if (parcelable != null && type != null
							&& type.startsWith("image/")) {
						photoPath = (Uri) parcelable;
					} else {
						path = Utilities.getPath((Uri) parcelable);
						if (path != null) {
							if (path.startsWith("file:")) {
								path = path.replace("file://", "");
							}
							if (type != null && type.startsWith("video/")) {
								videoPath = path;
							} else {
								documentPath = path;
							}
						} else {
							error = true;
						}
					}
					if (error) {
						Toast.makeText(this, "Unsupported content",
								Toast.LENGTH_SHORT).show();
					}
				}
			} else if (intent.getAction().equals(Intent.ACTION_SEND_MULTIPLE)) {
				boolean error = false;
				try {
					ArrayList<Parcelable> uris = intent
							.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
					String type = intent.getType();
					if (uris != null) {
						if (type != null && type.startsWith("image/")) {
							Uri[] uris2 = new Uri[uris.size()];
							for (int i = 0; i < uris2.length; i++) {
								Parcelable parcelable = uris.get(i);
								if (!(parcelable instanceof Uri)) {
									parcelable = Uri.parse(parcelable
											.toString());
								}
								uris2[i] = (Uri) parcelable;
							}
							imagesPathArray = uris2;
						} else {
							String[] uris2 = new String[uris.size()];
							for (int i = 0; i < uris2.length; i++) {
								Parcelable parcelable = uris.get(i);
								if (!(parcelable instanceof Uri)) {
									parcelable = Uri.parse(parcelable
											.toString());
								}
								String path = Utilities
										.getPath((Uri) parcelable);
								if (path != null) {
									if (path.startsWith("file:")) {
										path = path.replace("file://", "");
									}
									uris2[i] = path;
								}
							}
							documentsPathArray = uris2;
						}
					} else {
						error = true;
					}
				} catch (Exception e) {
					FileLog.e("emm", e);
					error = true;
				}
				if (error) {
					Toast.makeText(this, "Unsupported content",
							Toast.LENGTH_SHORT).show();
				}
			} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
				try {
					if (intent != null) {
						if (intent.getData() != null) {
							Cursor cursor = getContentResolver().query(
									intent.getData(), null, null, null, null);
							if (cursor != null) {
								if (cursor.moveToFirst()) {
									int userId = cursor.getInt(cursor
											.getColumnIndex("DATA4"));
									NotificationCenter
									.getInstance()
									.postNotificationName(
											MessagesController.closeChats);
									push_user_id = userId;
								}
								cursor.close();
							}
						}
					}
				} catch (Exception e) {
					FileLog.e("emm", e);
				}
			} else if (intent.getAction().equals(
					"info.emm.messenger.OPEN_ACCOUNT")) {
				open_settings = 1;
			}
		}

		if (getIntent().getAction() != null
				&& getIntent().getAction().startsWith("info.emm.openchat")
				&& (getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0
				&& !restore) {
			int chatId = getIntent().getIntExtra("chatId", 0);
			int userId = getIntent().getIntExtra("userId", 0);
			int encId = getIntent().getIntExtra("encId", 0);
			int meetId = getIntent().getIntExtra("meetId", 0);
			if (userId != 0 && meetId != 0) {
				TLRPC.User user = MessagesController.getInstance().users
						.get(userId);
				if (user != null) {
					push_user_id = userId;
				}
				TLRPC.TL_MeetingInfo info = MessagesController.getInstance().meetings
						.get(meetId);
				if (info != null) {
					push_meet_id = meetId;
				}
			} else if (chatId != 0) {
				TLRPC.Chat chat = MessagesController.getInstance().chats
						.get(chatId);
				if (chat != null) {
					NotificationCenter.getInstance().postNotificationName(
							MessagesController.closeChats);
					push_chat_id = chatId;
				}
			} else if (userId != 0) {
				TLRPC.User user = MessagesController.getInstance().users
						.get(userId);
				if (user != null) {
					NotificationCenter.getInstance().postNotificationName(
							MessagesController.closeChats);
					push_user_id = userId;
				}
			} else if (encId != 0) {
				TLRPC.EncryptedChat chat = MessagesController.getInstance().encryptedChats
						.get(encId);
				if (chat != null) {
					NotificationCenter.getInstance().postNotificationName(
							MessagesController.closeChats);
					push_enc_id = encId;
				}
			}
		}

		if (push_user_id != 0) {
			if (push_user_id == UserConfig.clientUserId) {
				open_settings = 1;
			} else {
				ChatActivity fragment = new ChatActivity();
				Bundle bundle = new Bundle();
				bundle.putInt("user_id", push_user_id);
				fragment.setArguments(bundle);
				if (fragment.onFragmentCreate()) {
					pushOpened = true;
					ApplicationLoader.fragmentsStack.add(fragment);
					getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.container, fragment,
							"chat" + Math.random())
							.commitAllowingStateLoss();
				}
			}
		} else if (push_chat_id != 0) {
			ChatActivity fragment = new ChatActivity();
			Bundle bundle = new Bundle();
			bundle.putInt("chat_id", push_chat_id);
			fragment.setArguments(bundle);
			if (fragment.onFragmentCreate()) {
				pushOpened = true;
				ApplicationLoader.fragmentsStack.add(fragment);
				getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.container, fragment,
						"chat" + Math.random())
						.commitAllowingStateLoss();
			}
		} else if (push_enc_id != 0) {
			ChatActivity fragment = new ChatActivity();
			Bundle bundle = new Bundle();
			bundle.putInt("enc_id", push_enc_id);
			fragment.setArguments(bundle);
			if (fragment.onFragmentCreate()) {
				pushOpened = true;
				ApplicationLoader.fragmentsStack.add(fragment);
				getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.container, fragment,
						"chat" + Math.random())
						.commitAllowingStateLoss();
			}
		}
		if (videoPath != null || photoPath != null || sendingText != null
				|| documentPath != null || documentsPathArray != null
				|| imagesPathArray != null || contactsToSend != null) {
			FileLog.d("emm", "new MessagesActivity");
			MessagesActivity fragment = new MessagesActivity();
			fragment.selectAlertString = R.string.ForwardMessagesTo;
			fragment.selectAlertStringDesc = "ForwardMessagesTo";
			fragment.animationType = 1;
			Bundle args = new Bundle();
			args.putBoolean("onlySelect", true);
			fragment.setArguments(args);
			fragment.delegate = this;
			ApplicationLoader.fragmentsStack.add(fragment);
			fragment.onFragmentCreate();
			getSupportFragmentManager().beginTransaction()
			.replace(R.id.container, fragment, fragment.getTag())
			.commitAllowingStateLoss();
			pushOpened = true;
		}
		if (open_settings != 0) {
			SettingsActivity fragment = new SettingsActivity();
			ApplicationLoader.fragmentsStack.add(fragment);
			fragment.onFragmentCreate();
			getSupportFragmentManager().beginTransaction()
			.replace(R.id.container, fragment, "settings")
			.commitAllowingStateLoss();
			pushOpened = true;
		}
		if (!pushOpened && !isNew) {
			BaseFragment fragment = ApplicationLoader.fragmentsStack
					.get(ApplicationLoader.fragmentsStack.size() - 1);
			getSupportFragmentManager().beginTransaction()
			.replace(R.id.container, fragment, fragment.getTag())
			.commitAllowingStateLoss();
		}

		if (intent.getAction() != null
				&& intent.getAction().equals("info.emm.joinmeeting")) {
			// ��ʾ���������Ҳμӻ���
			String meetId = intent.getStringExtra("meetingId");
			int fromid = intent.getIntExtra("userId", 0);
			int gid = intent.getIntExtra("chatId", 0);
			int type = intent.getIntExtra("type", 0);
			String pwd = intent.getStringExtra("pwd");
			if (currentConnectionState != 0) 
			{
				String alertMsg = ApplicationLoader.applicationContext
						.getString(R.string.WaitingForNetwork);
				Utilities.showToast(LaunchActivity.this, alertMsg);
			} 
			else 
			{				
				if (type == 0) 
				{				
					ApplicationLoader.getInstance().joinMeeting(this, meetId, pwd);
				}
				else 
				{						
					// ��ʾ���յ�һ������,��������ĳ��Ҳ��������Ⱥ��,xueqianag todo..
					Intent mIntent = new Intent(this, PhoneActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("meetingId", meetId);
					bundle.putInt("userId", fromid);
					bundle.putInt("chatId", gid);
					bundle.putInt("callType", 0);
					mIntent.putExtras(bundle);
					startActivity(mIntent);
				}				
			}
			pushOpened = true;
		}
		/*if(getIntent().getAction()==null && pushOpened==false)
		{
			Log.e("emm","handle meeting*******************");
			handleIntentemm(intent, true, false);
		}*/
		if(pushOpened==false)
		{
			Log.e("emm","handle meeting*******************");
			handleIntentemm(intent, true, false);
		}
		if(pushOpened)
			Log.e("emm","pushOpened=true*******************");
		if(getIntent().getAction()==null)		
			Log.e("emm","getIntent().getAction()==null*******************");
		else
		{	
			Log.e("emm","getIntent().getAction()="+getIntent().getAction());
		}
		//handleIntentemm(intent, true, false);
		getIntent().setAction(null);

	}
	/**
	 * ���ӽ������
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		FileLog.e("emm", "lanuchActivity onNewIntent");
		handleIntent(intent, true, false);
		//handleIntentemm(intent, true, false);
		Log.e("emm", "launchActivity onNewIntent***********************");
	}

	private void resumePhone() {
		/*if (IMRtmpClientMgr.getInstance().inMeeting
				&& IMRtmpClientMgr.getInstance().phoneIsTop) {
			Intent intent2 = new Intent(LaunchActivity.this,
					PhoneActivity.class);
			intent2.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			Bundle bundle = new Bundle();
			bundle.putString("meetingId", IMRtmpClientMgr.getInstance()
					.getMeetingId());
			bundle.putInt("chatId", IMRtmpClientMgr.getInstance().getChatId());
			bundle.putInt("userId", IMRtmpClientMgr.getInstance().getUserId());
			intent2.putExtras(bundle);
			startActivity(intent2);
		}*/
	}

	@Override
	public void didSelectDialog(MessagesActivity messageFragment, long dialog_id) {
		if (dialog_id != 0) {
			int lower_part = (int) dialog_id;

			ChatActivity fragment = new ChatActivity();

			Bundle bundle = new Bundle();
			if (lower_part != 0) {
				if (lower_part > 0) {
					NotificationCenter.getInstance().postNotificationName(
							MessagesController.closeChats);
					bundle.putInt("user_id", lower_part);
					fragment.setArguments(bundle);
					fragment.scrollToTopOnResume = true;
					presentFragment(fragment, "chat" + Math.random(), true,
							false);
				} else if (lower_part < 0) {
					NotificationCenter.getInstance().postNotificationName(
							MessagesController.closeChats);
					bundle.putInt("chat_id", -lower_part);
					fragment.setArguments(bundle);
					fragment.scrollToTopOnResume = true;
					presentFragment(fragment, "chat" + Math.random(), true,
							false);
				}
			} else {
				NotificationCenter.getInstance().postNotificationName(
						MessagesController.closeChats);
				int chat_id = (int) (dialog_id >> 32);
				bundle.putInt("enc_id", chat_id);
				fragment.setArguments(bundle);
				fragment.scrollToTopOnResume = true;
				presentFragment(fragment, "chat" + Math.random(), true, false);
			}
			if (photoPath != null) {
				fragment.processSendingPhoto(null, photoPath);
			} else if (videoPath != null) {
				fragment.processSendingVideo(videoPath);
			} else if (sendingText != null) {
				fragment.processSendingText(sendingText);
			} else if (documentPath != null) {
				fragment.processSendingDocument(documentPath);
			} else if (imagesPathArray != null) {
				for (Uri path : imagesPathArray) {
					fragment.processSendingPhoto(null, path);
				}
			} else if (documentsPathArray != null) {
				for (String path : documentsPathArray) {
					fragment.processSendingDocument(path);
				}
			} else if (contactsToSend != null && !contactsToSend.isEmpty()) {
				for (TLRPC.User user : contactsToSend) {
					MessagesController.getInstance().sendMessage(user,
							dialog_id);
				}
			}
			photoPath = null;
			videoPath = null;
			sendingText = null;
			documentPath = null;
			imagesPathArray = null;
			documentsPathArray = null;
			contactsToSend = null;
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
			}
			else if(ApplicationLoader.edition==3){
				if (UserConfig.privateWebHttp != null && !UserConfig.privateWebHttp.isEmpty())
				{
					String url = UserConfig.privateWebHttp + ":82/";
					if(!url.toLowerCase().startsWith("http://"))
						url = "http://" + url;
					
					UpdateManager.register(this, url, BuildVars.HOCKEY_APP_HASH, listener);
				}
			}
			else{				
				UpdateManager.register(this, "http://u.weiyicloud.com/", BuildVars.HOCKEY_APP_HASH, listener);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		ApplicationLoader.lastPauseTime = System.currentTimeMillis();
		if (notificationView != null) {
			notificationView.hide(false);
		}
		View focusView = getCurrentFocus();
		if (focusView instanceof EditText) {
			focusView.clearFocus();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		processOnFinish();
		if (headsetPlugHandler != null)
			this.unregisterReceiver(headsetPlugHandler);
		Log.e("emm", "lanuchactivity destroy***********************************************");
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!UserConfig.clientActivated) {
			WeiyiMeeting.getInstance().setMeetingNotifyIntent(
					new Intent(ApplicationLoader.applicationContext, IntroActivity.class));
		}else{
			WeiyiMeeting.getInstance().setMeetingNotifyIntent(
					new Intent(ApplicationLoader.applicationContext, LaunchActivity.class));
		}

		if (notificationView == null && getLayoutInflater() != null) {
			notificationView = (NotificationView) getLayoutInflater().inflate(
					R.layout.notification_layout, null);
		}
		fixLayout();


		MessagesController.getInstance().releaseNotificationData();
		ApplicationLoader.resetLastPauseTime();
		supportInvalidateOptionsMenu();
		updateActionBar();
		try {
			NotificationManager mNotificationManager = (NotificationManager) this
					.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(1);
			MessagesController.getInstance().currentPushMessage = null;
		} catch (Exception e) {
			FileLog.e("emm", e);
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.e("emm", "onrestart********************");
		resumePhone();
	}

	private void processOnFinish() {
		if (finished) {
			return;
		}
		finished = true;
		NotificationCenter.getInstance().removeObserver(this, 1234);
		NotificationCenter.getInstance().removeObserver(this, 658);
		NotificationCenter.getInstance().removeObserver(this, 701);
		NotificationCenter.getInstance().removeObserver(this, 702);
		NotificationCenter.getInstance().removeObserver(this, 703);
		NotificationCenter.getInstance().removeObserver(this,
				GalleryImageViewer.needShowAllMedia);
		NotificationCenter.getInstance().removeObserver(this,
				MessagesController.create_group_final);

		if (notificationView != null) {
			notificationView.hide(false);
			notificationView.destroy();
			notificationView = null;
		}
		//xiaoyang���activitydestroyʱdialogҲfinish
		if(builder != null){
			builder.dismiss();
		}

		FileLog.d("emm", "LaunchActivity processOnFinish");
		Log.i("emm", "LaunchActivity processOnFinish");
	}

	@Override
	public void onConfigurationChanged(
			android.content.res.Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Utilities.checkDisplaySize();
		fixLayout();
	}

	@SuppressLint("NewApi")
	private void fixLayout() {
		if (containerView != null) {
			ViewTreeObserver obs = containerView.getViewTreeObserver();
			obs.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
					int rotation = manager.getDefaultDisplay().getRotation();

					int height;
					int currentActionBarHeight = getSupportActionBar()
							.getHeight();
					if (currentActionBarHeight != Utilities.dp(48)
							&& currentActionBarHeight != Utilities.dp(40)) {
						height = currentActionBarHeight;
					} else {
						height = Utilities.dp(48);
						if (rotation == Surface.ROTATION_270
								|| rotation == Surface.ROTATION_90) {
							height = Utilities.dp(40);
						}
					}

					if (notificationView != null) {
						notificationView.applyOrientationPaddings(
								rotation == Surface.ROTATION_270
								|| rotation == Surface.ROTATION_90,
								height);
					}

					if (Build.VERSION.SDK_INT < 16) {
						containerView.getViewTreeObserver()
						.removeGlobalOnLayoutListener(this);
					} else {
						containerView.getViewTreeObserver()
						.removeOnGlobalLayoutListener(this);
					}
				}
			});
		}
	}

	private void createGroupFinal() {
		final int chatid = (Integer) NotificationCenter.getInstance()
				.getFromMemCache(3);
		final ArrayList<Integer> selectedContacts = (ArrayList<Integer>) NotificationCenter
				.getInstance().getFromMemCache(2);
		if (selectedContacts == null) {
			return;
		}
		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				if (isRetransmit) {
					NotificationCenter.getInstance().postNotificationName(
							MessagesController.retransmit_new_chat, chatid,
							true);
					isRetransmit = false;
					if (MainAddress.delegateSelUsers != null) {
						MainAddress.delegateSelUsers.onFinish();
					}
					return;
				}
				TLRPC.Chat currentChat = MessagesController.getInstance().chats
						.get(chatid);
				if (currentChat != null) {
					currentChat.hasTitle = -1; // �״δ������������� Ϊ��
				}
				if (topAudioCall) {
					NotificationCenter.getInstance().addToMemCache(5,
							selectedContacts);
					Intent intent = new Intent(LaunchActivity.this,
							PhoneActivity.class);
					Bundle bundle = new Bundle();
					// int id = (Integer)args[0];
					String mid = "g" + chatid;
					bundle.putString("meetingId", mid);
					bundle.putInt("chatId", chatid);
					bundle.putInt("callType", 1);
					bundle.putInt("type", 1);
					bundle.putBoolean("topCreateGroupCall", true);
					intent.putExtras(bundle);
					startActivity(intent);
					finishFragment(false);
					// selectedContacts.clear();
					// selectedContacts = null;
				} else {
					ChatActivity fragment = new ChatActivity();
					Bundle bundle = new Bundle();
					bundle.putInt("chat_id", chatid);
					fragment.setArguments(bundle);
					presentFragment(fragment, "chat" + Math.random(), false,
							false);
				}
				if (MainAddress.delegateSelUsers != null) {
					MainAddress.delegateSelUsers.onFinish();
				}
				// finishFragment();

			}
		});

	}

	@Override
	@SuppressWarnings("unchecked")
	public void didReceivedNotification(int id, Object... args) {
		if (id == MessagesController.create_group_final) {
			createGroupFinal();
		}
		else if (id == 1234) 
		{
			NotificationCenter.getInstance().removeObserver(this, 1234);
			if(ApplicationLoader.getInstance().isInMeeting())
			{	
				Log.e("emm", "1234 exitmeeting ******************");
				WeiyiMeetingNotificationCenter.getInstance().addObserver(this, WeiyiMeeting.FORCE_EXIT_MEETING);				
				ApplicationLoader.getInstance().forceExitMeeting();				
			}
			else
			{
				Log.e("emm", "1234 else  ******************");
				for (BaseFragment fragment : ApplicationLoader.fragmentsStack) {
					fragment.onFragmentDestroy();
				}
				ApplicationLoader.fragmentsStack.clear();
				FileLog.d("emm", "launchactivity clear fragmentsStack");
				Intent intent2 = new Intent(this, IntroActivity.class);
				startActivity(intent2);
				processOnFinish();
				finish();
			}
		} else if (id == GalleryImageViewer.needShowAllMedia) {
			long dialog_id = (Long) args[0];
			MediaActivity fragment = new MediaActivity();
			Bundle bundle = new Bundle();
			if (dialog_id != 0) {
				bundle.putLong("dialog_id", dialog_id);
				fragment.setArguments(bundle);
				presentFragment(fragment, "media_" + dialog_id, false);
			}
		} else if (id == 658) {
			Integer push_user_id = (Integer) NotificationCenter.getInstance()
					.getFromMemCache("push_user_id", 0);
			Integer push_chat_id = (Integer) NotificationCenter.getInstance()
					.getFromMemCache("push_chat_id", 0);
			Integer push_enc_id = (Integer) NotificationCenter.getInstance()
					.getFromMemCache("push_enc_id", 0);
			Integer push_meet_id = (Integer) NotificationCenter.getInstance()
					.getFromMemCache("push_meet_id", 0);// wangxm add for
			// meetalert

			if (push_meet_id != 0 && push_user_id != 0) {

			} else if (push_user_id != 0) {
				NotificationCenter.getInstance().postNotificationName(
						MessagesController.closeChats);
				ChatActivity fragment = new ChatActivity();
				Bundle bundle = new Bundle();
				bundle.putInt("user_id", push_user_id);
				fragment.setArguments(bundle);
				if (fragment.onFragmentCreate()) {
					if (ApplicationLoader.fragmentsStack.size() > 0) {
						BaseFragment lastFragment = ApplicationLoader.fragmentsStack
								.get(ApplicationLoader.fragmentsStack.size() - 1);
						lastFragment.willBeHidden();
					}
					ApplicationLoader.fragmentsStack.add(fragment);
					getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.container, fragment,
							"chat" + Math.random())
							.commitAllowingStateLoss();
				}
			} else if (push_chat_id != 0) {
				NotificationCenter.getInstance().postNotificationName(
						MessagesController.closeChats);
				ChatActivity fragment = new ChatActivity();
				Bundle bundle = new Bundle();
				bundle.putInt("chat_id", push_chat_id);
				fragment.setArguments(bundle);
				if (fragment.onFragmentCreate()) {
					if (ApplicationLoader.fragmentsStack.size() > 0) {
						BaseFragment lastFragment = ApplicationLoader.fragmentsStack
								.get(ApplicationLoader.fragmentsStack.size() - 1);
						lastFragment.willBeHidden();
					}
					ApplicationLoader.fragmentsStack.add(fragment);
					getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.container, fragment,
							"chat" + Math.random())
							.commitAllowingStateLoss();
				}
			} else if (push_enc_id != 0) {
				NotificationCenter.getInstance().postNotificationName(
						MessagesController.closeChats);
				ChatActivity fragment = new ChatActivity();
				Bundle bundle = new Bundle();
				bundle.putInt("enc_id", push_enc_id);
				fragment.setArguments(bundle);
				if (fragment.onFragmentCreate()) {
					if (ApplicationLoader.fragmentsStack.size() > 0) {
						BaseFragment lastFragment = ApplicationLoader.fragmentsStack
								.get(ApplicationLoader.fragmentsStack.size() - 1);
						lastFragment.willBeHidden();
					}
					ApplicationLoader.fragmentsStack.add(fragment);
					getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.container, fragment,
							"chat" + Math.random())
							.commitAllowingStateLoss();
				}
			}
		} else if (id == 701) {
			if (notificationView != null) {
				MessageObject message = (MessageObject) args[0];
				notificationView.show(message);
			}
		} else if (id == 702) {
			if (args[0] != this) {
				processOnFinish();
			}
		} else if (id == 703) {//state 0����¼��¼  14��ͬһ�˺ŵ�¼������
			Log.e("emm", "703 currentConnectionState ******************");
			int state = (Integer) args[0];
			currentConnectionState = state;
			FileLog.d("emm", "connect status:" + currentConnectionState);
			// sam <10Ϊ��������״̬�� >10Ϊ����ʧ�ܣ�����ֵΪ����ʧ�ܴ���
			MessagesController.getInstance().connectResult = currentConnectionState;
			if (currentConnectionState < 10)
				updateActionBar();
			if(currentConnectionState == 0)
				checkForCrashes();
		} else if(id ==WeiyiMeeting.AUTO_EXIT_MEETING)   
		{
			Log.e("emm", "lanchAcitivity auto exit meeting**********************");
			onBackPressed();
		}
		else if(id ==WeiyiMeeting.FORCE_EXIT_MEETING)   
		{
			Log.e("emm", "FORCE_EXIT_MEETING  ******************");
			WeiyiMeetingNotificationCenter.getInstance().removeObserver(this,WeiyiMeeting.FORCE_EXIT_MEETING);
			for (BaseFragment fragment : ApplicationLoader.fragmentsStack) {
				fragment.onFragmentDestroy();
			}
			ApplicationLoader.fragmentsStack.clear();
			FileLog.d("emm", "launchactivity clear fragmentsStack");
			Intent intent2 = new Intent(this, IntroActivity.class);
			startActivity(intent2);
			processOnFinish();
			finish();
		}
	}

	public void fixBackButton() {
		if (android.os.Build.VERSION.SDK_INT == 19) {
			try {
				Class firstClass = getSupportActionBar().getClass();
				Class aClass = firstClass.getSuperclass();
				if (aClass == android.support.v7.app.ActionBar.class) {

				} else {
					Field field = aClass.getDeclaredField("mActionBar");
					field.setAccessible(true);
					android.app.ActionBar bar = (android.app.ActionBar) field
							.get(getSupportActionBar());

					field = bar.getClass().getDeclaredField("mActionView");
					field.setAccessible(true);
					View v = (View) field.get(bar);
					aClass = v.getClass();

					field = aClass.getDeclaredField("mHomeLayout");
					field.setAccessible(true);
					v = (View) field.get(v);
					v.setVisibility(View.VISIBLE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void updateActionBar() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar == null) {
			return;
		}
		BaseFragment currentFragment = null;
		if (!ApplicationLoader.fragmentsStack.isEmpty()) {
			currentFragment = ApplicationLoader.fragmentsStack
					.get(ApplicationLoader.fragmentsStack.size() - 1);
		}

		boolean canApplyLoading = true;// jenf for connect status
		if (currentFragment != null) {
			currentFragment.applySelfActionBar();
			// canApplyLoading = false;
		}
		/*
		 * if (currentFragment != null && (currentConnectionState == 0 ||
		 * !currentFragment.canApplyUpdateStatus() || statusView == null)) {
		 * currentFragment.applySelfActionBar(); //canApplyLoading = false; }
		 */
		// jenf for connect status
		// if (canApplyLoading && currentFragment != null) {
		// currentFragment.applyConnectStatus(currentConnectionState);
		/*
		 * if (statusView != null) { statusView.setVisibility(View.VISIBLE);
		 * actionBar.setDisplayShowTitleEnabled(false);
		 * actionBar.setDisplayShowHomeEnabled
		 * (ConstantValues.ActionBarShowLogo);
		 * actionBar.setDisplayHomeAsUpEnabled(false);
		 * actionBar.setDisplayUseLogoEnabled(false);
		 * actionBar.setDisplayShowCustomEnabled(true);
		 * actionBar.setSubtitle(null);
		 * 
		 * if (ApplicationLoader.fragmentsStack.size() > 1) {
		 * backStatusButton.setVisibility(View.VISIBLE);
		 * statusBackground.setEnabled(true); } else {
		 * backStatusButton.setVisibility(View.GONE);
		 * statusBackground.setEnabled(false); } //״̬�ı�����ʾ����״̬ if
		 * (currentConnectionState == 1) {
		 * statusText.setText(getString(R.string.WaitingForNetwork)); } else if
		 * (currentConnectionState == 2) {
		 * statusText.setText(getString(R.string.Connecting)); } else if
		 * (currentConnectionState == 3) {
		 * statusText.setText(getString(R.string.Updating)); } if
		 * (actionBar.getCustomView() != statusView) {
		 * actionBar.setCustomView(statusView); }
		 * 
		 * try { if (statusView.getLayoutParams() instanceof
		 * android.support.v7.app.ActionBar.LayoutParams) {
		 * android.support.v7.app.ActionBar.LayoutParams statusParams =
		 * (android.
		 * support.v7.app.ActionBar.LayoutParams)statusView.getLayoutParams();
		 * statusText.measure(View.MeasureSpec.makeMeasureSpec(800,
		 * View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(100,
		 * View.MeasureSpec.AT_MOST)); statusParams.width =
		 * (statusText.getMeasuredWidth() + Utilities.dp(54)); if
		 * (statusParams.height == 0) { statusParams.height =
		 * actionBar.getHeight(); } if (statusParams.width <= 0) {
		 * statusParams.width = Utilities.dp(100); } statusParams.topMargin = 0;
		 * statusParams.leftMargin = 0;
		 * statusView.setLayoutParams(statusParams); } else if
		 * (statusView.getLayoutParams() instanceof
		 * android.app.ActionBar.LayoutParams) {
		 * android.app.ActionBar.LayoutParams statusParams =
		 * (android.app.ActionBar.LayoutParams)statusView.getLayoutParams();
		 * statusText.measure(View.MeasureSpec.makeMeasureSpec(800,
		 * View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(100,
		 * View.MeasureSpec.AT_MOST)); statusParams.width =
		 * (statusText.getMeasuredWidth() + Utilities.dp(54)); if
		 * (statusParams.height == 0) { statusParams.height =
		 * actionBar.getHeight(); } if (statusParams.width <= 0) {
		 * statusParams.width = Utilities.dp(100); } statusParams.topMargin = 0;
		 * statusParams.leftMargin = 0;
		 * statusView.setLayoutParams(statusParams); } } catch (Exception e) {
		 * e.printStackTrace(); } }
		 */
		// }
	}

	public void presentFragment(BaseFragment fragment, boolean removeLast) {
		presentFragment(fragment, "", removeLast, false);
	}

	public void presentFragment(BaseFragment fragment, String tag,
			boolean bySwipe) {
		presentFragment(fragment, tag, false, bySwipe);
	}

	public void presentFragment(BaseFragment fragment, String tag,
			boolean removeLast, boolean bySwipe) {
		if (getCurrentFocus() != null) {
			Utilities.hideKeyboard(getCurrentFocus());
		}

		FileLog.d("emm",
				"LaunchActivity presentFragment:" + fragment.toString() + " "
						+ tag.toString());

		if (!fragment.onFragmentCreate()) {
			return;
		}
		BaseFragment current = null;
		if (!ApplicationLoader.fragmentsStack.isEmpty()) {
			current = ApplicationLoader.fragmentsStack
					.get(ApplicationLoader.fragmentsStack.size() - 1);
		}
		if (current != null) {
			current.willBeHidden();
		}
		FileLog.d("emm", "LaunchActivity presentFragment 1");
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction fTrans = fm.beginTransaction();
		if (removeLast && current != null) {
			ApplicationLoader.fragmentsStack
			.remove(ApplicationLoader.fragmentsStack.size() - 1);
			current.onFragmentDestroy();
		}
		FileLog.d("emm", "LaunchActivity presentFragment 2");
		SharedPreferences preferences = ApplicationLoader.applicationContext
				.getSharedPreferences("mainconfig_" + UserConfig.clientUserId,
						Activity.MODE_PRIVATE);
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
		ApplicationLoader.fragmentsStack.add(fragment);
	}

	public void removeFromStack(BaseFragment fragment) {
		if (fragment instanceof mainFramgMent)
			FileLog.d("emm", "shouldn't work here");
		ApplicationLoader.fragmentsStack.remove(fragment);
		fragment.onFragmentDestroy();
	}

	public void finishFragment(boolean bySwipe) {
		if (getCurrentFocus() != null) {
			Utilities.hideKeyboard(getCurrentFocus());
		}
		if (ApplicationLoader.fragmentsStack.size() < 2) {
			/*
			 * for (BaseFragment fragment : ApplicationLoader.fragmentsStack) {
			 * fragment.onFragmentDestroy(); }
			 * ApplicationLoader.fragmentsStack.clear();
			 * 
			 * MainAddress fragment = new MainAddress();
			 * fragment.onFragmentCreate();
			 * ApplicationLoader.fragmentsStack.add(fragment);
			 * getSupportFragmentManager
			 * ().beginTransaction().replace(R.id.container, fragment,
			 * "contacts").commitAllowingStateLoss();
			 */
			FileLog.e("emm", "shouldn't be here");
			return;
		}
		FileLog.e("emm", "finishFragment**************");
		BaseFragment fragment = ApplicationLoader.fragmentsStack
				.get(ApplicationLoader.fragmentsStack.size() - 1);
		if (fragment instanceof ChatActivity) {
			FileLog.e("emm", "clear chatactivity");
		}
		fragment.onFragmentDestroy();
		BaseFragment prev = ApplicationLoader.fragmentsStack
				.get(ApplicationLoader.fragmentsStack.size() - 2);
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction fTrans = fm.beginTransaction();
		SharedPreferences preferences = ApplicationLoader.applicationContext
				.getSharedPreferences("mainconfig_" + UserConfig.clientUserId,
						Activity.MODE_PRIVATE);
		boolean animations = preferences.getBoolean("view_animations", true);
		if (animations) {
			if (bySwipe) {
				fTrans.setCustomAnimations(R.anim.no_anim_show,
						R.anim.slide_right_away);
			} else {
				fTrans.setCustomAnimations(R.anim.no_anim_show,
						R.anim.scale_out);
			}
		}

		fTrans.replace(R.id.container, prev, prev.getTag());
		FileLog.e("emm", "finishFragment 1**************");
		fTrans.commitAllowingStateLoss();
		ApplicationLoader.fragmentsStack
		.remove(ApplicationLoader.fragmentsStack.size() - 1);
		fm.executePendingTransactions();

		// if(prev instanceof mainFramgMent)
		// {
		// FileLog.e("emm", "show mainFramgMent" );
		// ((mainFramgMent) prev).refreshView();
		// }
	}

	// sam
	// private void onBackPressedDelay() {
	// handler.postDelayed(new Runnable() {
	// @Override
	// public void run() {
	// if (ApplicationLoader.fragmentsStack.size() == 1)
	// {
	// BaseFragment lastFragment = ApplicationLoader.fragmentsStack.get(0);
	// mainFramgMent f = (mainFramgMent)lastFragment;
	// if( f!=null && f.changeData() )
	// {
	// return;
	// }
	// //��С����������˳�����
	// Intent intent = new Intent();
	// intent.setAction("android.intent.action.MAIN");
	// intent.addCategory("android.intent.category.HOME");
	// startActivity(intent);
	// /*for (BaseFragment fragment : ApplicationLoader.fragmentsStack) {
	// fragment.onFragmentDestroy();
	// }
	// ApplicationLoader.fragmentsStack.clear();
	// processOnFinish();
	// finish();*/
	// return;
	// }
	//
	// if (!ApplicationLoader.fragmentsStack.isEmpty()) {
	// BaseFragment lastFragment =
	// ApplicationLoader.fragmentsStack.get(ApplicationLoader.fragmentsStack.size()
	// - 1);
	// if (lastFragment.onBackPressed())
	// {
	// FileLog.d("emm", "close fragment");
	// finishFragment(false);
	// }
	// }
	// }
	// }, 10);
	// }

	@Override
	public void onBackPressed() {
		// sam
		// onBackPressedDelay();

		/*
		 * if (ApplicationLoader.fragmentsStack.size() == 1) { BaseFragment
		 * lastFragment = ApplicationLoader.fragmentsStack.get(0); mainFramgMent
		 * f = (mainFramgMent)lastFragment; if( f!=null && f.changeData() ) {
		 * return; } //��С����������˳����� Intent intent = new Intent();
		 * intent.setAction("android.intent.action.MAIN");
		 * intent.addCategory("android.intent.category.HOME");
		 * startActivity(intent);
		 * 
		 * return; }
		 * 
		 * if (!ApplicationLoader.fragmentsStack.isEmpty()) { BaseFragment
		 * lastFragment =
		 * ApplicationLoader.fragmentsStack.get(ApplicationLoader.
		 * fragmentsStack.size() - 1); if (lastFragment.onBackPressed()) {
		 * FileLog.d("emm", "close fragment"); finishFragment(false); } }
		 */
		FileLog.d("emm", "Launch onBackPressed");
		if (ApplicationLoader.fragmentsStack.size() == 1) {
			BaseFragment lastFragment = ApplicationLoader.fragmentsStack.get(0);
			mainFramgMent f = (mainFramgMent) lastFragment;
			if (f != null && f.changeData()) {
				return;
			}
			// ��С����������˳�����
			Intent intent = new Intent();
			intent.setAction("android.intent.action.MAIN");
			intent.addCategory("android.intent.category.HOME");
			startActivity(intent);
			/*
			 * for (BaseFragment fragment : ApplicationLoader.fragmentsStack) {
			 * fragment.onFragmentDestroy(); }
			 * ApplicationLoader.fragmentsStack.clear(); processOnFinish();
			 * finish();
			 */
			return;
		}

		if (!ApplicationLoader.fragmentsStack.isEmpty()) {
			BaseFragment lastFragment = ApplicationLoader.fragmentsStack
					.get(ApplicationLoader.fragmentsStack.size() - 1);
			if (lastFragment.onBackPressed()) {
				FileLog.d("emm", "close fragment");
				finishFragment(false);
			}
		}
		Utilities.HideProgressDialog(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {// ���̵İ�ť�¼�
			if (!ApplicationLoader.fragmentsStack.isEmpty()) {
				BaseFragment lastFragment = ApplicationLoader.fragmentsStack
						.get(ApplicationLoader.fragmentsStack.size() - 1);
				lastFragment.onMenuClick();
			}
		}
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP
				|| keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			UEngine.getInstance().getSoundService().setRingVolume();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		try {
			super.onSaveInstanceState(outState);
			if (!ApplicationLoader.fragmentsStack.isEmpty()) {
				BaseFragment lastFragment = ApplicationLoader.fragmentsStack
						.get(ApplicationLoader.fragmentsStack.size() - 1);
				Bundle args = lastFragment.getArguments();
				if (lastFragment instanceof ChatActivity && args != null) {
					outState.putBundle("args", args);
					outState.putString("fragment", "chat");
				} else if (lastFragment instanceof SettingsActivity) {
					outState.putString("fragment", "settings");
				} else if (lastFragment instanceof CreateCompanyActivity
						&& args != null) {
					outState.putBundle("args", args);
					outState.putString("fragment", "group");
				} else if (lastFragment instanceof SettingsWallpapersActivity) {
					outState.putString("fragment", "wallpapers");
				}
				// only for test
				// lastFragment.saveSelfArgs(outState);
			}
		} catch (Exception e) {
			FileLog.e("tmessages", e);
		}
	}

	// jenf
	// public void SelTabBackground(TextView tvSel, ImageView imSel, TextView[]
	// tvs, ImageView[] ims)
	// {
	// ColorStateList cslBlue = (ColorStateList)
	// getResources().getColorStateList(R.color.blue);
	// tvSel.setTextColor(cslBlue);
	// for (int i = 0; i < tvs.length; i++)
	// {
	// ColorStateList cslBlack = (ColorStateList)
	// getResources().getColorStateList(R.color.black);
	// tvs[i].setTextColor(cslBlack);
	// }
	//
	// switch (imSel.getId())
	// {
	// case R.id.main_tab_contacts_img:
	// imSel.setBackgroundResource(R.drawable.tabiconcontacts_highlighted);
	// break;
	// case R.id.main_tab_chats_img:
	// imSel.setBackgroundResource(R.drawable.tabiconmessages_highlighted);
	// break;
	// case R.id.main_tab_discuss_img:
	// imSel.setBackgroundResource(R.drawable.tabicondiscuss_highlighted);
	// break;
	// case R.id.main_tab_meeting_img:
	// imSel.setBackgroundResource(R.drawable.tabiconmeeting_highlighted);
	// break;
	// default:
	// break;
	// }
	// for (int j = 0; j < ims.length; j++)
	// {
	// switch (ims[j].getId())
	// {
	// case R.id.main_tab_contacts_img:
	// ims[j].setBackgroundResource(R.drawable.tabiconcontacts);
	// break;
	// case R.id.main_tab_chats_img:
	// ims[j].setBackgroundResource(R.drawable.tabiconmessages);
	// break;
	// case R.id.main_tab_discuss_img:
	// ims[j].setBackgroundResource(R.drawable.tabicondiscuss);
	// break;
	// case R.id.main_tab_meeting_img:
	// ims[j].setBackgroundResource(R.drawable.tabiconmeeting);
	// break;
	// default:
	// break;
	// }
	//
	// }
	//
	// }

	// public void SelActivity(TextView tvSel, ImageView imSel, TextView[] tvs,
	// ImageView[] ims)
	// {
	// // set bg
	// SelTabBackground(tvSel, imSel, tvs, ims);
	//
	// // sel activity
	// if (R.id.main_tab_contacts_text == tvSel.getId())
	// {
	// SelContactsActivity();
	// }
	// else if (R.id.main_tab_chats_text == tvSel.getId())
	// {
	// SelChatsActivity();
	// }
	// else if (R.id.main_tab_discuss_text == tvSel.getId())
	// {
	// SelDiscussActivity();
	// }
	// else if (R.id.main_tab_meeting_text == tvSel.getId())
	// {
	// SelMeetingActivity();
	// }
	// }
	//
	// public void SelContactsActivity()
	// {
	// ((mainFramgMent)presentFragment).setTabIndex(0);
	// }
	//
	// public void SelChatsActivity()
	// {
	// ((mainFramgMent)presentFragment).setTabIndex(1);
	// }
	//
	// public void SelDiscussActivity()
	// {
	// ((mainFramgMent)presentFragment).setTabIndex(2);
	// }
	//
	// public void SelMeetingActivity()
	// {
	// ((mainFramgMent)presentFragment).setTabIndex(3);
	// }
	//
	public void createNewDiscuz() {
		// ForumSelectDialog selectDlg = new ForumSelectDialog(this);
	}

	public void SelSettingsActivity() {
		presentFragment(new SettingsActivity(), "settings", false);
	}

	/**
	 * @Title: createNewgroup
	 * 
	 * @Description: ����Ⱥ��
	 * 
	 */
	public void createNewgroup(Integer... objects) {
		Intent intent = new Intent(this, CreateNewGroupActivity.class);
		Bundle bundle = new Bundle();
		topAudioCall = false;
		if (objects != null) {
			if (objects.length > 0 && objects[0] != -1) {
				bundle.putInt("default_user_id", objects[0]);
			}
			if (objects.length > 1 && objects[1] != -1) {
				topAudioCall = true;
				// ��ҳ������Ƶ����
			}
		}
		MessagesController.getInstance().selectedUsers.clear();
		MessagesController.getInstance().ignoreUsers.clear();
		// MessagesController.getInstance().
		// ����������Լ�ȱʡ��ӵ�����
		TLRPC.User user = MessagesController.getInstance().users
				.get(UserConfig.clientUserId);
		if (user != null) {
			MessagesController.getInstance().selectedUsers.put(user.id, user);
		}

		bundle.putBoolean("topAudioCall", topAudioCall);
		intent.putExtras(bundle);
		startActivityForResult(intent, 0);

	}

	public void createNewChat() {
		isRetransmit = true;
		Intent intent = new Intent(this, CreateNewGroupActivity.class);
		Bundle bundle = new Bundle();
		bundle.putBoolean("isRetransmit", isRetransmit);
		intent.putExtras(bundle);
		startActivityForResult(intent, 0);
	}

	/**
	 * @Title: createNewMeeting
	 * 
	 * @Description: ��������
	 * 
	 */
	public void createNewMeeting() {
		Intent intent = new Intent(this, CreateNewGroupActivity.class);
		Bundle bundle = new Bundle();
		MessagesController.getInstance().selectedUsers.clear();
		MessagesController.getInstance().ignoreUsers.clear();
		// MessagesController.getInstance().
		// ����������Լ�ȱʡ��ӵ�����
		TLRPC.User user = MessagesController.getInstance().users
				.get(UserConfig.clientUserId);
		if (user != null) {
			MessagesController.getInstance().selectedUsers.put(user.id, user);
		}
		// bundle.putInt("faceType",
		// CreateNewGroupActivity.FaceType.CREATE_MEETING.ordinal());
		bundle.putBoolean("isCreateMeeting", true);
		intent.putExtras(bundle);
		startActivityForResult(intent, 1);
	}

	/**
	 * @Title: CreateCompany
	 * 
	 * @Description: ������֯
	 * 
	 */
	public void CreateCompany() {
		// ��һ��activity�����Դӱ���ͨѶ¼ѡ����ϵ�ˣ�Ȼ����ͨ���ֻ������Ͷ��Ÿ����������
		// ��Ҫ������ζ�ȡ�ֻ�����ͨѶ¼����η��Ͷ���
		boolean bCreate = MessagesController.getInstance().canCreateCompany();
		if (bCreate) {
			BaseFragment fragment = new CreateCompanyActivity();
			Bundle bundle = new Bundle();
			bundle.putBoolean("isCreateCompany", true);
			fragment.setArguments(bundle);
			presentFragment(fragment, "group_craate_final", false);
			return;
		}
		String sTip = String.format(LocaleController.getString(
				"createCompanyCount", R.string.createCompanyCount),
				ConstantValues.CREATE_COMPANY_MAX);
		Utilities.showToast(LaunchActivity.this, sTip);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
			return;
		}

		if (requestCode == 0) {
			// group

		} 
		else if (requestCode == 1) 
		{
			//invoke createmeeting for php server
			//wait for return value and send invitation to participants
			//receiver ���壬���ܻ�ܾ�������飬���Ҫ�ο�ԭ���������ĺ��й��̣����ܾͽ�����飬������ھܾ��������
			//�Ƿ����ʾ�б����߿�����������,��ʵ���Ƿ���������Ϣ,���ϻ����ַ

			// meeting
			/*BaseFragment fragment = new Meeting2AddActivity();
			Bundle bundle = new Bundle();
			fragment.setArguments(bundle);
			presentFragment(fragment, "meeting_add", false);*/
		} else if (requestCode == 2) {
			// company
			BaseFragment fragment = new CreateCompanyActivity();
			Bundle bundle = new Bundle();
			bundle.putInt(Config.CompanyID, -1);
			bundle.putBoolean("isCreateCompany", true);
			fragment.setArguments(bundle);
			presentFragment(fragment, "group_craate_final", false);
		}
	}

	public Fragment getFragment(int pos) {
		// ��ȡMessagesActivity,��viewpager��
		/*
		 * MessagesActivity fragment =
		 * (MessagesActivity)((LaunchActivity)parentActivity).getFragment(1);
		 * if(fragment!=null) { fragment.selectAlertString =
		 * R.string.ForwardMessagesTo; fragment.selectAlertStringDesc =
		 * "ForwardMessagesTo"; fragment.animationType = 1;
		 * fragment.serverOnly=true; fragment.onlySelect=true; fragment.delegate
		 * = this; }
		 */
		if (presentFragment instanceof mainFramgMent) {
			mainFramgMent m = (mainFramgMent) presentFragment;
			return m.getFragment(pos);
		}
		return null;
	}

	//	private void createShortcut() {
	//		if (!UserConfig.firstTimeInstall) {
	//			UserConfig.firstTimeInstall = true;
	//			UserConfig.saveConfig(false);
	//			createShortCutInternal();
	//		}
	//	}

	//	public boolean hasShortCut(Context context) {
	//		String url = "";
	//		if (android.os.Build.VERSION.SDK_INT < 8) {
	//			url = "content://com.android.launcher.settings/favorites?notify=true";
	//		} else {
	//			url = "content://com.android.launcher2.settings/favorites?notify=true";
	//		}
	//		ContentResolver resolver = context.getContentResolver();
	//		Cursor cursor = resolver.query(Uri.parse(url), null, "title=?",
	//				new String[] { context.getString(R.string.AppName) }, null);
	//
	//		if (cursor != null && cursor.moveToFirst()) {
	//			cursor.close();
	//			return true;
	//		}
	//		return false;
	//	}

	//	public void createShortCutInternal() {
	//		String Tips = ApplicationLoader.applicationContext
	//				.getString(R.string.Tips);
	//		String msg = LocaleController.getString("IsCreateShortCut",
	//				R.string.IsCreateShortCut);
	//		String ok = ApplicationLoader.applicationContext.getString(R.string.OK);
	//		String Cancel = ApplicationLoader.applicationContext
	//				.getString(R.string.Cancel);
	//		builder = new AlertDialog.Builder(this)
	//				.setTitle(Tips)
	//				.setMessage(msg)
	//				.setPositiveButton(ok, new DialogInterface.OnClickListener() {
	//
	//					@Override
	//					public void onClick(DialogInterface dialog, int which) {
	//						if (!hasShortCut(LaunchActivity.this)) {
	//							// wangxm todo,����һ����ʾ����һ���Ƿ񴴽������ݷ�ʽ������
	//							// ������ݷ�ʽ��Intent
	//							Intent shortcutIntent = new Intent(
	//									"com.android.launcher.action.INSTALL_SHORTCUT");
	//							// �������ظ�����
	//							shortcutIntent.putExtra("duplicate", false);
	//							// ��ݷ�ʽ����
	//							// shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
	//							// getString(R.string.shortcut_name));
	//							String msg = LocaleController.getString("AppName",
	//									R.string.AppName);
	//							shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
	//									msg);
	//							// ���ͼƬ
	//							Parcelable icon = Intent.ShortcutIconResource
	//									.fromContext(getApplicationContext(),
	//											R.drawable.ic_start);
	//							shortcutIntent.putExtra(
	//									Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
	//							// ������ͼƬ�����еĳ��������
	//							shortcutIntent.putExtra(
	//									Intent.EXTRA_SHORTCUT_INTENT, new Intent(
	//											getApplicationContext(),
	//											LaunchActivity.class));
	//							sendBroadcast(shortcutIntent);
	//						}
	//					}
	//				})
	//				.setNegativeButton(Cancel,
	//						new DialogInterface.OnClickListener() {
	//
	//							@Override
	//							public void onClick(DialogInterface dialog,
	//									int which) {
	//
	//							}
	//						}).show();
	//	}

	public class BroadcastsHandler extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equalsIgnoreCase(Intent.ACTION_HEADSET_PLUG)) {
				// state - 0 for unplugged, 1 for plugged.
				// name - Headset type, human readable string
				// microphone - 1 if headset has a microphone, 0 otherwise
				String data = intent.getDataString();
				Bundle extraData = intent.getExtras();

				int st = intent.getIntExtra("state", -1);
				String nm = intent.getStringExtra("name");
				int mic = intent.getIntExtra("microphone", 0);
				if (st == 0)
					MediaController.getInstance().SetEarPhoneState(false);
				else
					MediaController.getInstance().SetEarPhoneState(true);
			}
		}
	}

	@SuppressWarnings("unused")
	private boolean handleIntentemm(Intent intent, boolean isNew,
			boolean restore) {
		Log.d("emm", intent.toString());
		boolean bfrom_title = false;
		if (intent == null) {
			Log.i("rebuild", "intent null");
		} else if (intent.getAction() != null) {
			Log.i("rebuild", intent.getAction());
		}
		if (intent != null && !restore) 
		{
			Log.e("emm", "joinmeetingbyurl*******************");
			//			UserConfig.logout();
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

			//			return true;
		}
		return false;
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
								tempHttpPort = httpServer.substring(index+1);
								httpServer = httpServer.substring(0, index);
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
		UserConfig.logout();
		if(UserConfig.privateWebHttp == httpServer && UserConfig.privatePort == Integer.parseInt(httpPort) && UserConfig.priaccount == account){

		}else{
			Intent intent1 = new Intent(this, IntroActivity.class);
			startActivity(intent1);
			finish();
		}
		return false;
	}
}
/*
 * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import info.emm.yuanchengcloudb.R;
import info.emm.PhoneFormat.PhoneFormat;
import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.MessagesStorage;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.RPCRequest;
import info.emm.messenger.TLObject;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.messenger.TLRPC.TL_error;
import info.emm.ui.Views.SlideView;
import info.emm.utils.StringUtil;
import info.emm.utils.Utilities;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;




public class LoginActivitySmsView extends SlideView implements NotificationCenter.NotificationCenterDelegate {
	private String phoneHash;
	private String requestPhone;
	private String registered;
	private EditText codeField;
	private TextView confirmTextView;
	private TextView timeText;
	private Bundle currentParams;


	private Timer timeTimer;
	private final Integer timerSync = 1;
	private int time = 60000;
	private double lastCurrentTime;
	private boolean waitingForSms = false;
	//xueqiang add
	private String account;
	private int viewIndex=0;
	private boolean isRegister = false;
	private boolean resetPwd = false;
	private TextView wrongNumber;

	public LoginActivitySmsView(Context context) {
		super(context);
	}

	public LoginActivitySmsView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LoginActivitySmsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		confirmTextView = (TextView)findViewById(R.id.login_sms_confirm_text);
		codeField = (EditText)findViewById(R.id.login_sms_code_field);
		Utilities.showKeyboard(codeField);
		timeText = (TextView)findViewById(R.id.login_time_text);

		wrongNumber = (TextView) findViewById(R.id.wrong_number);
		wrongNumber.setText(LocaleController.getString("WrongNumber", R.string.WrongNumber));

		//�����������ʱ�����»�ȡ��֤��
		wrongNumber.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) 
			{
				//onBackPressed();
				//delegate.setPage(0, true, null, true);
				sendEmail(account,true);
				final Bundle params = new Bundle();
				params.putString("phoneOrEmailValue", account);
				params.putInt("viewIndex", 4);
				params.putInt("registered", 0);//0��ʾ���û���2��ʾע���û�
				params.putBoolean("resetPwd", true);				
				params.putInt("calltime", 120000);
				delegate.setPage(1, true, params, false);
			}
		});

		codeField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
				if (i == EditorInfo.IME_ACTION_NEXT) {
					if (delegate != null) {
						delegate.onNextAction();
					}
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public String getHeaderName() {
		return getResources().getString(R.string.AppName);
	}

	@Override
	public void setParams(Bundle params, boolean back) 
	{	
		//wangxm repair begin
		try {
			synchronized(timerSync) {
				if (timeTimer != null) {
					timeTimer.cancel();
					timeTimer = null;
				}
			}
		} catch (Exception e) {
			FileLog.e("emm", e);
		}

		account = params.getString("phoneOrEmailValue");
		resetPwd = params.getBoolean("resetPwd");
		if (wrongNumber != null) {
			wrongNumber.setVisibility(View.VISIBLE);
		}
		if( !Utilities.isPhone(account) )
		{	
			viewIndex=params.getInt("viewIndex", 0);
			int a = params.getInt("registered");


			if( a== 0)//��ʾ�����û���ע������
			{
				confirmTextView.setText(Html.fromHtml(String.format(ApplicationLoader.applicationContext.getResources().getString(R.string.SentEmailCode) + " <b>%s</b>", account)));
				codeField.setHint(LocaleController.getString("Code", R.string.Code));
				codeField.setInputType(InputType.TYPE_CLASS_NUMBER);
				codeField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

				isRegister = false;

				if(wrongNumber!=null)
					wrongNumber.setVisibility(View.GONE);
				timeText.setVisibility(View.VISIBLE);
			}	
			else //��2��ʾ�ߵ�¼����
			{
				confirmTextView.setText(Html.fromHtml(ApplicationLoader.applicationContext.getResources().getString(R.string.YourAlreadyRegidter)));
				codeField.setHint(LocaleController.getString("InputPwd", R.string.hint_password));
				codeField.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
				codeField.setTransformationMethod(PasswordTransformationMethod.getInstance());        		
				isRegister = true;
				timeText.setVisibility(View.GONE);
				return;
			}    		
		}
		else
		{	
			//wangxm repair end	
			codeField.setText("");
			Utilities.setWaitingForSms(true);
			NotificationCenter.getInstance().addObserver(this, 998);
			currentParams = params;
			waitingForSms = true;
			String phone = account;
			requestPhone = params.getString("phoneFormated");
			//phoneHash = params.getString("phoneHash");
			int a = params.getInt("registered");
			if(a==2)//��2��ʾ�ߵ�¼����,��ʾ�Ѿ�ע���,0��ʾ��һ��ע�ᣬ�����û�
			{
				isRegister = true;
				confirmTextView.setText(Html.fromHtml(ApplicationLoader.applicationContext.getResources().getString(R.string.YourAlreadyRegidter)));
				codeField.setHint(LocaleController.getString("InputPwd", R.string.hint_password));
				codeField.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
				codeField.setTransformationMethod(PasswordTransformationMethod.getInstance());
				timeText.setVisibility(View.GONE);
				return;
			}
			else
			{
				isRegister = false;
				if(wrongNumber!=null)
					wrongNumber.setVisibility(View.GONE);
				timeText.setVisibility(View.VISIBLE);
			}
			String number = PhoneFormat.getInstance().format(phone);
			confirmTextView.setText(Html.fromHtml(String.format(ApplicationLoader.applicationContext.getResources().getString(R.string.SentSmsCode) + " <b>%s</b>", number)));
			codeField.setHint(StringUtil.getStringFromRes( R.string.EnterCode));
			codeField.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
		}
		time = params.getInt("calltime");

		Utilities.showKeyboard(codeField);
		codeField.requestFocus();


		if( !Utilities.isPhone(account) )
			timeText.setText(String.format("%s 1:00", ApplicationLoader.applicationContext.getResources().getString(R.string.CallEmailText)));
		else
			timeText.setText(String.format("%s 1:00", ApplicationLoader.applicationContext.getResources().getString(R.string.CallText)));
		lastCurrentTime = System.currentTimeMillis();
		timeTimer = new Timer();
		timeTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				double currentTime = System.currentTimeMillis();
				double diff = currentTime - lastCurrentTime;
				time -= diff;
				lastCurrentTime = currentTime;
				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						if (time >= 1000) {
							int minutes = time / 1000 / 60;
							int seconds = time / 1000 - minutes * 60;

							if( !Utilities.isPhone(account) )
								timeText.setText(String.format("%s %d:%02d", ApplicationLoader.applicationContext.getResources().getString(R.string.CallEmailText), minutes, seconds));
							else
							{
								String s = String.format("%d:%02d", minutes, seconds);
								String s1 = LocaleController.formatString("CallText", R.string.CallText, s);
								timeText.setText(s1);
							}
						} 
						else 
						{
							//�ٴ�������email
							//                        	sendEmail(account,false);                        	
							if( !Utilities.isPhone(account) )
								timeText.setText(ApplicationLoader.applicationContext.getResources().getString(R.string.CallEmailText));
							else
							{
								timeText.setText(ApplicationLoader.applicationContext.getResources().getString(R.string.to_resend));
							}
							timeText.setTextColor(Color.BLUE);
							timeText.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									sendEmail(account,false);

								}
							});
							synchronized(timerSync) {
								if (timeTimer != null) {
									timeTimer.cancel();
									timeTimer = null;
								}
							}
						}
					}
				});
			}
		}, 0, 1000);    	
	}

	@Override
	public void onNextPressed() 
	{
		//wangxm repair begin
		if( !Utilities.isPhone(account) )
		{
			//��ʼ��֤
			if(isRegister)
				CheckLogin();
			else	
				checkCode();
			return;
		}
		else
		{
			waitingForSms = false;
			Utilities.setWaitingForSms(false);
			NotificationCenter.getInstance().removeObserver(this, 998);
			try {
				synchronized(timerSync) {
					if (timeTimer != null) {
						timeTimer.cancel();
						timeTimer = null;
					}
				}
			} catch (Exception e) {
				FileLog.e("emm", e);
			}

			if(isRegister)
				CheckLogin();
			else	
				checkCode();
		}
	}

	@Override
	public void onBackPressed() {
		try {
			synchronized(timerSync) {
				if (timeTimer != null) {
					timeTimer.cancel();
					timeTimer = null;
				}
			}
		} catch (Exception e) {
			FileLog.e("emm", e);
		}
		currentParams = null;
		Utilities.setWaitingForSms(false);
		NotificationCenter.getInstance().removeObserver(this, 998);
		waitingForSms = false;
	}

	@Override
	public void onDestroyActivity() {
		super.onDestroyActivity();
		Utilities.setWaitingForSms(false);
		NotificationCenter.getInstance().removeObserver(this, 998);
		try {
			synchronized(timerSync) {
				if (timeTimer != null) {
					timeTimer.cancel();
					timeTimer = null;
				}
			}
		} catch (Exception e) {
			FileLog.e("emm", e);
		}
		waitingForSms = false;
	}

	@Override
	public void onShow() {
		super.onShow();
		if (codeField != null) {
			codeField.requestFocus();
			codeField.setSelection(codeField.length());
		}
	}

	@Override
	public void didReceivedNotification(int id, final Object... args) {
		if (id == 998) {
			Utilities.RunOnUIThread(new Runnable() {
				@Override
				public void run() {
					if (!waitingForSms) {
						return;
					}
					if (codeField != null) {
						codeField.setText("" + args[0]);
						onNextPressed();
					}
				}
			});
		}
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		return new SavedState(superState, currentParams);
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		currentParams = savedState.params;
		if (currentParams != null) {
			setParams(currentParams,false);
		}
	}

	protected static class SavedState extends BaseSavedState {
		public Bundle params;

		private SavedState(Parcelable superState, Bundle p1) {
			super(superState);
			params = p1;
		}

		private SavedState(Parcel in) {
			super(in);
			params = in.readBundle();
		}

		@Override
		public void writeToParcel(Parcel destination, int flags) {
			super.writeToParcel(destination, flags);
			destination.writeBundle(params);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

	private void checkCode()
	{
		final String codeValue = codeField.getText().toString(); 
		if (codeValue.isEmpty()) 
		{
			delegate.needShowAlert(LocaleController.getString("InvalidCode", R.string.InvalidCode));
			return;
		}
		if (delegate != null) {
			delegate.needShowProgress();
		}

		ConnectionsManager.getInstance().CheckIDCode(account, codeValue ,new RPCRequest.RPCRequestDelegate() {
			@Override
			public void run(TLObject response, final TLRPC.TL_error error) 
			{
				if (delegate != null) {
					delegate.needHideProgress();
				}
				Log.d("emm", "codevalue="+codeValue);
				if (error != null) 
				{
					final TLRPC.TL_auth_authorization res = (TLRPC.TL_auth_authorization)response;
					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() 
						{
							int result = error.code;
							switch (result) {

							case -1:// У����֤��ʧ��
							if (delegate != null) {
								delegate.needShowAlert(ApplicationLoader.applicationContext.getString(R.string.InvalidCode));
							}	
							break;                			
							case -2:
								if (delegate != null) {
									delegate.needShowAlert(ApplicationLoader.applicationContext.getString(R.string.WaitingForNetwork));
								}
								break;
							default:                    				
								break;
							}

						}
					});
					return;
				}
				//1:У����֤��ɹ�,//˵�������û�������һҳ��д���룬��firstname and lastname
				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {
						Bundle params = new Bundle();
						params.putString("phoneOrEmailValue", account);
						params.putInt("dataType", viewIndex);
						params.putString("hashCode", codeValue);
						params.putBoolean("resetPwd", resetPwd);
						delegate.setPage(2, true, params, false);	
					}
				});
			}
		});
	}
	private void  CheckLogin()
	{
		final String pwd = codeField.getText().toString();
		if (pwd.isEmpty()) 
		{
			delegate.needShowAlert(LocaleController.getString("InvalidCode", R.string.InvalidPwd));
			return ;
		}

		if (delegate != null) {
			delegate.needShowProgress();
		}
		ConnectionsManager.getInstance().CheckLogin("",account, pwd, new RPCRequest.RPCRequestDelegate() 
		{			
			@Override
			public void run(final TLObject response, final TL_error error) 
			{	
				if (error != null) 
				{
					if (delegate != null) {
						delegate.needHideProgress();
					}
					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() 
						{
							int result = error.code;
							if( result == 1 )
							{
								//�ʺ�δ����
								if (delegate != null) {
									delegate.needShowAlert(ApplicationLoader.applicationContext.getString(R.string.AccountNoActication));
								}
							}
							else if( result == 2)
							{
								//�ʺ��Ѿ�����
								if (delegate != null) {
									delegate.needShowAlert(ApplicationLoader.applicationContext.getString(R.string.AccountFreeze));
								}
							}                        	
							else if( result == 3)
							{
								//�����û���Ϣ�豸ʧ��
								if (delegate != null) {
									delegate.needShowAlert(ApplicationLoader.applicationContext.getString(R.string.DeviceUpdateFaild));
								}
							}
							else if( result == 4)
							{
								//�������
								if (delegate != null) {
									delegate.needShowAlert(ApplicationLoader.applicationContext.getString(R.string.PasswordError));
								}
							}
							else if( result == 5)
							{
								//�ʺŴ���
								if (delegate != null) {
									delegate.needShowAlert(ApplicationLoader.applicationContext.getString(R.string.AccountError));
								}
							}                        
							else if (result==-1)
								if (delegate != null) {
									delegate.needShowAlert(ApplicationLoader.applicationContext.getString(R.string.WaitingForNetwork));
								}

						}
					});
					return;
				}

				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() 
					{
						//��¼�ɹ�,˵���Ѿ���ע���û��ˣ���ҳ��д��������                    	
						final TLRPC.TL_auth_authorization res = (TLRPC.TL_auth_authorization)response;       				 
						UserConfig.clearConfig();
						MessagesStorage.getInstance().cleanUp();
						//MessagesController.getInstance().cleanUp();
						UserConfig.currentUser = res.user;
						UserConfig.clientActivated = true;
						UserConfig.clientUserId = res.user.id;
						UserConfig.saveConfig(true);
						MessagesStorage.getInstance().openDatabase();                        
						ArrayList<TLRPC.User> users = new ArrayList<TLRPC.User>();
						users.add(res.user);
						MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
						MessagesController.getInstance().users.put(res.user.id, res.user);

						FileLog.d("emm", "check login result:" + UserConfig.clientUserId + " sid:" + UserConfig.currentUser.sessionid);

						//sam
						try {
							AccountManager accountManager = AccountManager.get(ApplicationLoader.applicationContext);
							Account myAccount  = new Account(account, "info.emm.weiyicloud.account");
							accountManager.addAccountExplicitly(myAccount, pwd, null);
						} catch (Exception e) {
							e.printStackTrace();
							FileLog.e("emm", e);
						}


						if (delegate != null) {
							delegate.needFinishActivity();
						}
					}
				});
			}	
		});    
	}
	private void sendEmail(final String email,final boolean forgetpwd)
	{	
		int type = 0; 
		if(forgetpwd)
			type = 1;
		ConnectionsManager.getInstance().GetIDCode(email,type, new RPCRequest.RPCRequestDelegate() 
		{			
			@Override
			public void run(final TLObject response, final TL_error error) 
			{
				if (error != null) 
				{
					Utilities.RunOnUIThread(new Runnable() {
						@Override
						public void run() 
						{                        	
							if (delegate != null) 
							{
								if (error.code==-2)
								{
									delegate.needShowAlert(ApplicationLoader.applicationContext.getString(R.string.WaitingForNetwork));
								}
								else if( error.code==-1)
								{
									String msg = LocaleController.getString("SendEmailFaildPleaseCheck", R.string.SendEmailFaildPleaseCheck);
									delegate.needShowAlert(msg);
								}
								else if( error.code==3)
								{
									String msg = LocaleController.getString("PleaseUseCompanyEmail", R.string.PleaseUseCompanyEmail);
									delegate.needShowAlert(msg);
								}
								else
								{
									delegate.needShowAlert(ApplicationLoader.applicationContext.getString(R.string.SysTemExpressionCatch));
								}
							}                        	
						}
					});      
					return;
				}

				Utilities.RunOnUIThread(new Runnable() {
					@Override
					public void run() {                    	
						if (delegate != null) 
						{
							/*final Bundle params = new Bundle();
    						TLRPC.TL_auth_authorization res = (TLRPC.TL_auth_authorization) response;
    						params.putString("phoneOrEmailValue", email);
    						params.putInt("viewIndex", 4);
    						params.putInt("registered", res.expires);//0��ʾ���û���2��ʾע���û�
    				        delegate.setPage(1, true, params, false);*/
						}
					}
				});


			}	
		});
	}


}

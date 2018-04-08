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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import info.emm.PhoneFormat.PhoneFormat;
import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.RPCRequest;
import info.emm.messenger.TLObject;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.messenger.TLRPC.TL_error;
import info.emm.ui.Views.SlideView;
import info.emm.utils.ToolUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class LoginActivityPhoneView extends SlideView implements AdapterView.OnItemSelectedListener {
	private EditText codeField;
	private EditText phoneField;
	private TextView countryButton;

	private int countryState = 0;
	//xiaoyang
	public boolean inState = false;



	private ArrayList<String> countriesArray = new ArrayList<String>();
	private HashMap<String, String> countriesMap = new HashMap<String, String>();
	private HashMap<String, String> codesMap = new HashMap<String, String>();
	private HashMap<String, String> languageMap = new HashMap<String, String>();

	private boolean ignoreSelection = false;
	private boolean ignoreOnTextChange = false;
	private boolean ignoreOnPhoneChange = false;



	public LoginActivityPhoneView(Context context) {
		super(context);
	}

	public LoginActivityPhoneView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LoginActivityPhoneView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();        




		countryButton = (TextView)findViewById(R.id.login_coutry_textview);
		countryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				ActionBarActivity activity = (ActionBarActivity)delegate;
				Intent intent = new Intent(activity, CountrySelectActivity.class);
				activity.startActivityForResult(intent, 1);
			}
		});

		codeField = (EditText)findViewById(R.id.login_county_code_field);
		if(codeField==null)
			return;
		codeField.clearFocus();
		codeField.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				if (ignoreOnTextChange) {
					ignoreOnTextChange = false;
					return;
				}
				ignoreOnTextChange = true;
				String text = PhoneFormat.stripExceptNumbers(codeField.getText().toString());
				codeField.setText(text);
				if (text.length() == 0) {
					countryButton.setText(LocaleController.getString("ChooseCountry", R.string.ChooseCountry));
					countryState = 1;
				} else {
					String country = codesMap.get(text);
					if (country != null) {
						int index = countriesArray.indexOf(country);
						if (index != -1) {
							ignoreSelection = true;
							countryButton.setText(countriesArray.get(index));

							updatePhoneField();
							countryState = 0;
						} else {
							countryButton.setText(LocaleController.getString("WrongCountry", R.string.WrongCountry));
							countryState = 2;
						}
					} else {
						countryButton.setText(LocaleController.getString("WrongCountry", R.string.WrongCountry));
						countryState = 2;
					}
					codeField.setSelection(codeField.getText().length());
				}
			}
		});
		codeField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
				if (i == EditorInfo.IME_ACTION_NEXT) {
					phoneField.requestFocus();
					return true;
				}
				return false;
			}
		});
		//�����EMAIL����Ҫ���ص�����BUTTON�͹��Ҵ����ֶ�
		if(!Utilities.isPhone)
		{
			countryButton.setVisibility(View.GONE);
			codeField.setVisibility(View.GONE);
			TextView tv  = (TextView)findViewById(R.id.login_confirm_text);
			tv.setVisibility(View.GONE);
			TextView tvLabel  = (TextView)findViewById(R.id.login_county_code_label);
			tvLabel.setVisibility(View.GONE);

		}
		phoneField = (EditText)findViewById(R.id.login_phone_field);
		phoneField.setHint(LocaleController.getString("YourPhone", R.string.YourPhone));
		phoneField.setInputType(InputType.TYPE_CLASS_NUMBER);
		//phoneField.setFocusable(true);
		//phoneField.setFocusableInTouchMode(true);
		//phoneField.requestFocus();

		if(Utilities.isPhone)
		{
			if( UserConfig.phone!=null && !UserConfig.phone.equals(""))
			{
				String sPhone=UserConfig.phone;
				//qxm add
				Activity activity = ((LoginActivity)(LoginActivityPhoneView.this.getContext()));
				Bundle bundle1 = activity.getIntent().getExtras();
				String strPhone ;
				if(bundle1 != null){
					strPhone=bundle1.getString("phoneNum");
					if(sPhone.equals(strPhone)){
						phoneField.setText(sPhone);
					}else{
						phoneField.setText(strPhone);
					}
				}

				//			
				//				if(sPhone.startsWith("+"))
				//				{
				//					int len = UserConfig.coutryCode.length()+1;					
				//					sPhone = UserConfig.phone.substring(len);
				//				}


			}
		}
		else
		{	
			phoneField.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
			phoneField.setHint(LocaleController.getString("Inputemial", R.string.Inputemial));
			if( UserConfig.email!=null && !UserConfig.email.equals(""))
				phoneField.setText(UserConfig.email);

		}
		if(Utilities.isPhone)
		{
			phoneField.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					if (ignoreOnPhoneChange) {
						return;
					}
					if (count == 1 && after == 0 && s.length() > 1) {
						String phoneChars = "0123456789";
						String str = s.toString();
						String substr = str.substring(start, start + 1);
						if (!phoneChars.contains(substr)) {
							ignoreOnPhoneChange = true;
							StringBuilder builder = new StringBuilder(str);
							int toDelete = 0;
							for (int a = start; a >= 0; a--) {
								substr = str.substring(a, a + 1);
								if(phoneChars.contains(substr)) {
									break;
								}
								toDelete++;
							}
							builder.delete(Math.max(0, start - toDelete), start + 1);
							str = builder.toString();
							if (PhoneFormat.strip(str).length() == 0) {
								phoneField.setText("");
							} else {
								phoneField.setText(str);
								updatePhoneField();
							}
							ignoreOnPhoneChange = false;
						}
					}
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					if (ignoreOnPhoneChange) {
						return;
					}
					updatePhoneField();
				}
			});

			if(!isInEditMode()) {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(getResources().getAssets().open("countries.txt")));
					String line;
					while ((line = reader.readLine()) != null) {
						String[] args = line.split(";");
						countriesArray.add(0, args[2]);
						countriesMap.put(args[2], args[0]);
						codesMap.put(args[0], args[2]);
						languageMap.put(args[1], args[2]);
					}
				} catch (Exception e) {
					FileLog.e("emm", e);
				}

				Collections.sort(countriesArray, new Comparator<String>() {
					@Override
					public int compare(String lhs, String rhs) {
						return lhs.compareTo(rhs);
					}
				});

				String country = null;

				try {
					TelephonyManager telephonyManager = (TelephonyManager)ApplicationLoader.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
					if (telephonyManager != null) {
						country = telephonyManager.getSimCountryIso().toUpperCase();
					}
				} catch (Exception e) {
					FileLog.e("emm", e);
				}

				if (country != null) {
					String countryName = languageMap.get(country);
					if (countryName != null) {
						int index = countriesArray.indexOf(countryName);
						if (index != -1) {
							codeField.setText(countriesMap.get(countryName));
							countryState = 0;
						}
					}
				}
				if (codeField.length() == 0) {
					countryButton.setText(LocaleController.getString("ChooseCountry", R.string.ChooseCountry));
					countryState = 1;
				}
			}
		}
		//        if (codeField.length() != 0) {
		Utilities.showKeyboard(phoneField);
		phoneField.requestFocus();
		//        } else {
		//            Utilities.showKeyboard(codeField);
		//            codeField.requestFocus();
		//        }
		phoneField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
				if (i == EditorInfo.IME_ACTION_NEXT) {
					delegate.onNextAction();
					return true;
				}
				return false;
			}
		});

		TextView changeTv = (TextView)findViewById(R.id.tv_change);
		changeTv.setText(Utilities.isPhone?R.string.TranslateToEmail:R.string.TranslateToPhone);
		changeTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				;
				Activity ac = ((LoginActivity)(LoginActivityPhoneView.this.getContext()));
				Intent intent = new Intent(ac,ac.getClass());
				Bundle mBundle = new Bundle();
				mBundle.putBoolean("phoneregister", !Utilities.isPhone);
				intent.putExtras(mBundle);
				ac.finish();
				ac.startActivity(intent);
			}
		});
	}

	public void selectCountry(String name) {
		int index = countriesArray.indexOf(name);
		if (index != -1) {
			ignoreOnTextChange = true;
			codeField.setText(countriesMap.get(name));
			countryButton.setText(name);
			countryState = 0;
		}
	}

	private void updatePhoneField() {
		ignoreOnPhoneChange = true;
		String codeText = codeField.getText().toString();
		//		String phone = PhoneFormat.getInstance().format("+" + codeText + phoneField.getText().toString()); 
		Activity activity = ((LoginActivity)(LoginActivityPhoneView.this.getContext()));
		Bundle bundle1 = activity.getIntent().getExtras();
		String strPhone = null ;
		if(bundle1 != null){
			strPhone=bundle1.getString("phoneNum");
		}
		String phone = PhoneFormat.getInstance().format("+" + codeText + strPhone);  
		int idx = phone.indexOf(" ");
		if (idx != -1) {
			String resultCode = PhoneFormat.stripExceptNumbers(phone.substring(0, idx));
			if (!codeText.equals(resultCode)) {
				phone = PhoneFormat.getInstance().format(phoneField.getText().toString()).trim();
				phoneField.setText(phone);
				int len = phoneField.length();
				phoneField.setSelection(phoneField.length());
			} else {
				phoneField.setText(phone.substring(idx).trim());
				int len = phoneField.length();
				phoneField.setSelection(phoneField.length());
			}
		} else {
			phoneField.setSelection(phoneField.length());
		}
		ignoreOnPhoneChange = false;
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
		if (ignoreSelection) {
			ignoreSelection = false;
			return;
		}
		ignoreOnTextChange = true;
		String str = countriesArray.get(i);
		codeField.setText(countriesMap.get(str));
		updatePhoneField();
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {

	}

	@Override
	public void onNextPressed() 
	{
		String account;
		if(Utilities.isPhone)
		{
			//	        if (countryState == 1) {
			//	            delegate.needShowAlert(LocaleController.getString("ChooseCountry", R.string.ChooseCountry));
			//	            return;
			//	        } else if (countryState == 2) {
			//	            delegate.needShowAlert(LocaleController.getString("WrongCountry", R.string.WrongCountry));
			//	            return;
			//	        }
			//codeField.length() == 0 ||
			if ( phoneField.length() == 0&&delegate!=null) {
				delegate.needShowAlert(LocaleController.getString("InvalidPhoneNumber", R.string.InvalidPhoneNumber));
				return;
			}
			//			//������Ҵ�����ʺ�
			//	        UserConfig.coutryCode =  codeField.getText().toString();
			//	        UserConfig.saveConfig(false);

			account = PhoneFormat.stripExceptNumbers("" /*codeField.getText()*/ + phoneField.getText());
		}
		else
		{
			account = phoneField.getText().toString();
			// jenf ���email�ĺϷ���
			if(!Utilities.CheckEmail(account) )
			{
				delegate.needShowAlert(LocaleController.getString("InvalidEmailAdrress", R.string.InvalidEmailAdrress));
				return;
			}

		}
		if(inState){
			sendEmail(account,true);
		}else{    		
			sendCall(account);
		}
	}

	@Override
	public void onShow() {
		super.onShow();
		if (phoneField != null) {
			phoneField.requestFocus();
			phoneField.setSelection(phoneField.length());
		}
	}

	@Override
	public String getHeaderName() 
	{
		if(Utilities.isPhone)
			return getResources().getString(R.string.YourPhone);
		return getResources().getString(R.string.YourEmail);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		return new SavedState(superState, phoneField.getText().toString(), codeField.getText().toString());
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		codeField.setText(savedState.code);
		phoneField.setText(savedState.phone);
	}

	protected static class SavedState extends BaseSavedState {
		public String phone;
		public String code;

		private SavedState(Parcelable superState, String text1, String text2) {
			super(superState);
			phone = text1;
			code = text2;
			if (phone == null) {
				phone = "";
			}
			if (code == null) {
				code = "";
			}
		}

		private SavedState(Parcel in) {
			super(in);
			phone = in.readString();
			code = in.readString();
		}

		@Override
		public void writeToParcel(Parcel destination, int flags) {
			super.writeToParcel(destination, flags);
			destination.writeString(phone);
			destination.writeString(code);
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
	private void sendCall(final String phone)
	{
		if (delegate != null) {
			delegate.needShowProgress();
		}
		ConnectionsManager.getInstance().GetIDCode(phone,0, new RPCRequest.RPCRequestDelegate() 
		{			
			@Override
			public void run(final TLObject response, final TL_error error) 
			{
				if (delegate != null) {
					delegate.needHideProgress();
				}
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
								}else if(error.code == 2){
									String msg = LocaleController.getString("UserExist", R.string.UserExist);
									delegate.needShowAlert(msg);
								}
								else
								{
									//qxm change  ����û�δע�ᣬ������������룬�����һ��֮����ʾδע�ᣬֱ�ӽ���ע��ҳ��
									//email��û�д���ֻ�������ֻ��ŵ�¼
									//delegate.needShowAlert(ApplicationLoader.applicationContext.getString(R.string.Systemnoregister));
									ActionBarActivity activity = (ActionBarActivity)delegate;
									Intent intent2 = new Intent(activity,LoginActivity.class);
									Bundle bundle = new Bundle();
									bundle.putBoolean("phoneregister",ToolUtil.isCNLanguage(activity));
									bundle.putBoolean("instate", false);
									Activity activity1 = ((LoginActivity)(LoginActivityPhoneView.this.getContext()));
									Bundle bundle1 = activity1.getIntent().getExtras();
									String strPhone = null ;
									if(bundle1 != null){
										strPhone=bundle1.getString("phoneNum");
									}
									bundle.putString("phoneNum", strPhone);
									intent2.putExtras(bundle);
									activity.startActivity(intent2);
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
							final Bundle params = new Bundle();
							TLRPC.TL_auth_authorization res = (TLRPC.TL_auth_authorization) response;    						
							params.putString("phoneOrEmailValue", phone);
							params.putInt("viewIndex", 4);
							params.putInt("registered", 0);//0��ʾ���û���2��ʾע���û�
							params.putString("phone", "+" + codeField.getText() + phoneField.getText());
							params.putString("phoneFormated", phone);
							params.putInt("calltime", 120000); 
							params.putBoolean("resetPwd", inState);
							delegate.setPage(1, true, params, false);
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
									//delegate.needShowAlert(ApplicationLoader.applicationContext.getString(R.string.Systemnoregister));
									//qxm change
									ActionBarActivity activity = (ActionBarActivity)delegate;
									AlertDialog.Builder builder = new AlertDialog.Builder(activity);
									builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
									builder.setMessage(ApplicationLoader.applicationContext.getString(R.string.Systemnoregister));
									builder.setPositiveButton(LocaleController.getString("OK", R.string.OK),new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface arg0, int arg1) {
											ActionBarActivity activity = (ActionBarActivity)delegate;
											Intent intent2 = new Intent(activity,LoginActivity.class);
											Bundle bundle = new Bundle();
											bundle.putBoolean("phoneregister",ToolUtil.isCNLanguage(activity));
											bundle.putBoolean("instate", false);
											Activity activity1 = ((LoginActivity)(LoginActivityPhoneView.this.getContext()));
											Bundle bundle1 = activity1.getIntent().getExtras();
											String strPhone = null ;
											if(bundle1 != null){
												strPhone=bundle1.getString("phoneNum");
											}
											bundle.putString("phoneNum", strPhone);
											intent2.putExtras(bundle);
											activity.startActivity(intent2);
										}
									} );
									builder.show().setCanceledOnTouchOutside(true);
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
							final Bundle params = new Bundle();
							TLRPC.TL_auth_authorization res = (TLRPC.TL_auth_authorization) response;    						
							params.putString("phoneOrEmailValue", email);
							params.putInt("viewIndex", 4);
							params.putInt("registered", 0);//0��ʾ���û���2��ʾע���û�
							params.putString("phone", "+" + codeField.getText() + phoneField.getText());
							params.putString("phoneFormated", email);
							params.putInt("calltime", 120000); 
							params.putBoolean("resetPwd", inState);
							delegate.setPage(1, true, params, false);
						}
					}
				});


			}	
		});
	}
}

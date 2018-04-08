/**
 * @Title        : PhoneLinkActivity.java
 *
 * @Package      : info.emm.ui
 *
 * @Copyright    : Copyright - All Rights Reserved.
 *
 * @Author       : He,Zhen hezhen@yunboxin.com
 *
 * @Date         : 2014-7-1
 *
 * @Version      : V1.00
 * 				 
 * 				  ���İ��ֻ�
 */
package info.emm.ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v7.app.ActionBar;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import info.emm.PhoneFormat.PhoneFormat;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.UserConfig;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.StringUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

public class ChangePhoneLinkActivity extends BaseFragment implements
NotificationCenter.NotificationCenterDelegate {
	private boolean isLinked = true; //true�Ѿ���
	
    private EditText codeField;
    private EditText phoneField;
    
    private TextView countryButton;
    
    private int countryState = 0;
    
    private ArrayList<String> countriesArray = new ArrayList<String>();
    private HashMap<String, String> countriesMap = new HashMap<String, String>();
    private HashMap<String, String> codesMap = new HashMap<String, String>();
    private HashMap<String, String> languageMap = new HashMap<String, String>();

    
    private boolean ignoreSelection = false;
    private boolean ignoreOnTextChange = false;
    private boolean ignoreOnPhoneChange = false;
    
    BroadcastReceiver bcr;
    
    private String phoneNum;
    private String countryCode;
	
	@Override
	public void didReceivedNotification(int id, Object... args) {
		Utilities.HideProgressDialog(getActivity());
		if(id == MessagesController.getcode_success){
//			  VerifyCodeActivity fragment = new VerifyCodeActivity();
//              Bundle bundle = new Bundle();
//              fragment.setArguments(bundle);
//              ((LaunchActivity)parentActivity).presentFragment(fragment, "", false);
        	VerifyCodeActivity fragment = new VerifyCodeActivity();
            Bundle bundle = new Bundle();
            bundle.putString("verifyAccount", phoneNum);
            bundle.putString("countryCode", countryCode);            
            fragment.setArguments(bundle);
            ((LaunchActivity)parentActivity).presentFragment(fragment, "", false);
		}else if(id == MessagesController.getcode_failed){
			Utilities.showToast(getActivity(), getResources().getString(R.string.GetCodeError));
		}
	}
	@Override
	public boolean onFragmentCreate() {
		super.onFragmentCreate();
		isLinked = !StringUtil.isEmpty(UserConfig.currentUser.phone);
		bcr = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String text = intent.getStringExtra("country");
				selectCountry(text);
			}
		};
		NotificationCenter.getInstance().addObserver(this, MessagesController.getcode_failed);
		NotificationCenter.getInstance().addObserver(this, MessagesController.getcode_success);
		return true;
	}
	@Override
	public void onFragmentDestroy() {
		super.onFragmentDestroy();
		NotificationCenter.getInstance().removeObserver(this, MessagesController.getcode_failed);
		NotificationCenter.getInstance().removeObserver(this, MessagesController.getcode_success);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (fragmentView == null) {
			fragmentView = inflater.inflate(R.layout.setting_input_phone,
					container, false);
			viewSet(fragmentView);
		}else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
		return fragmentView;
	}
	@Override
	public void onStart() {
		 IntentFilter filter = new IntentFilter();  
	     filter.addAction("getCountry");  
	     getActivity().registerReceiver(bcr, filter);
	     String name = CountrySelectActivity.selectCountry;
	     if(!StringUtil.isEmpty(name)){
	    	 selectCountry(name);
	    	 CountrySelectActivity.selectCountry = "";
	     }
		super.onStart();
	}
	@Override
	public void onStop() {
		getActivity().unregisterReceiver(bcr); 
		super.onStop();
	}
	 @Override
	    public void applySelfActionBar() {
	        if (parentActivity == null) {
	            return;
	        }
	        ActionBar actionBar =  super.applySelfActionBar(true);
	        actionBar.setTitle(R.string.VerifyPhoneNum);

	        TextView title = (TextView)parentActivity.findViewById(R.id.action_bar_title);
	        if (title == null) {
	            final int subtitleId = parentActivity.getResources().getIdentifier("action_bar_title", "id", "android");
	            title = (TextView)parentActivity.findViewById(subtitleId);
	        }
	        if (title != null) {
	            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
	            title.setCompoundDrawablePadding(0);
	        }
	    }
	 private void viewSet(View view) {
	        countryButton = (TextView)view.findViewById(R.id.login_coutry_textview);
	        countryButton.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View view) {
	            	Activity ac = getActivity();
	                Intent intent = new Intent(ac, CountrySelectActivity.class);
	                intent.putExtra("isChangePhone", true);
	                ac.startActivity(intent);
	            }
	        });

	        codeField = (EditText)view.findViewById(R.id.login_county_code_field);
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
//	        if(!EmmUtil.isPhone)
//	        {
//	        	countryButton.setVisibility(View.GONE);
//	        	codeField.setVisibility(View.GONE);
//	        	TextView tv  = (TextView)view.findViewById(R.id.login_confirm_text);
//	        	tv.setVisibility(View.GONE);
//	        	TextView tvLabel  = (TextView)view.findViewById(R.id.login_county_code_label);
//	        	tvLabel.setVisibility(View.GONE);
//	        	
//	        }
	        phoneField = (EditText)view.findViewById(R.id.login_phone_field);
	        
//	        if(EmmUtil.isPhone)
//	        {
////				if( UserConfig.phone!=null && !UserConfig.phone.equals(""))		
////					phoneField.setText(UserConfig.phone);			
//	        }
//	        else
//	        {	
//	        	phoneField.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
//	        	phoneField.setHint(LocaleController.getString("Inputemial", R.string.Inputemial));
//				if( UserConfig.email!=null && !UserConfig.email.equals(""))
//					phoneField.setText(UserConfig.email);
//				
//	        }
//	        if(EmmUtil.isPhone)
//	        {
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
//		        }
	        if (codeField.length() != 0) {
	            Utilities.showKeyboard(phoneField);
	            phoneField.requestFocus();
	        } else {
	            Utilities.showKeyboard(codeField);
	            codeField.requestFocus();
	        }
	        phoneField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
	            @Override
	            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
	                if (i == EditorInfo.IME_ACTION_NEXT) {
//	                    delegate.onNextAction();
	                    return true;
	                }
	                return false;
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
	        String phone = PhoneFormat.getInstance().format("+" + codeText + phoneField.getText().toString());        
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	       inflater.inflate(R.menu.group_profile_menu, menu);
	        SupportMenuItem doneItem = (SupportMenuItem)menu.findItem(R.id.block_user);
	        TextView doneTextView = (TextView)doneItem.getActionView().findViewById(R.id.done_button);
	        doneTextView.setText(R.string.Next);
	        doneTextView.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
	            	phoneNum = phoneField.getText().toString();
	            	if(!StringUtil.isEmpty(phoneNum))
	            	{ 
	            		//���� �˴����ж��Ƿ�Ϊ�ֻ���
	            		countryCode = codeField.getText().toString();
	            		phoneNum = "+" + codeField.getText().toString() + phoneNum;	            		
	            		Utilities.ShowProgressDialog(getActivity(), getResources().getString(R.string.Loading));
	            		MessagesController.getInstance().getCode(phoneNum);
					}
	            }
	        });
	}
	    @Override
	    public void onResume() {
	        super.onResume();
	        if (isFinish) {
	            return;
	        }
	        if (getActivity() == null) {
	            return;
	        }
	        ((LaunchActivity)parentActivity).showActionBar();
	        ((LaunchActivity)parentActivity).updateActionBar();
	        if(phoneField != null)
	        Utilities.showKeyboard(phoneField);
	    }
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        int itemId = item.getItemId();
	        switch (itemId) {
	            case android.R.id.home:
	                finishFragment();
	                break;
	        }
	        return true;
	    }
}

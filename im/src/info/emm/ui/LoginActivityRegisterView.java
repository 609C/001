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
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import info.emm.messenger.ConnectionsManager;

import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.MessagesStorage;
import info.emm.messenger.RPCRequest;
import info.emm.messenger.TLObject;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.messenger.TLRPC.TL_error;
import info.emm.ui.Views.SlideView;
import info.emm.utils.MaxLengthEdite;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;



public class LoginActivityRegisterView extends SlideView {
    private EditText firstNameField;
    private EditText lastNameField;
    private String requestPhone;
    private String phoneHash;
    private String phoneCode;
    private String account;
    private int viewIndex;
    private String hashCode;
    private Bundle currentParams;
    private boolean resetPwd=false;
   
    /**
     * �����������������Layoutֵ
     */
    private LinearLayout mPasswordLayout;
    /**
     * ���������
     */
    private EditText mInputPassword;
    /**
     * �ظ���������     
     */
    private EditText mReInputPassword;
    
    private TextView tipInfoTextView;
    
    private TextView wrongNumber;

    public LoginActivityRegisterView(Context context) {
        super(context);
    }

    public LoginActivityRegisterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoginActivityRegisterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        firstNameField = (EditText)findViewById(R.id.login_first_name_field);
        firstNameField.setHint(LocaleController.getString("FirstName", R.string.FirstName));
        firstNameField.addTextChangedListener(new MaxLengthEdite(12, firstNameField));
        lastNameField = (EditText)findViewById(R.id.login_last_name_field);
        lastNameField.setHint(LocaleController.getString("LastName", R.string.LastName));
        lastNameField.addTextChangedListener(new MaxLengthEdite(6, lastNameField));
        mPasswordLayout = (LinearLayout) findViewById(R.id.input_password_layout);
        mInputPassword = (EditText) findViewById(R.id.register_password_value);
        mReInputPassword = (EditText)findViewById(R.id.register_password_value_rewrite);
        //avatarImage = (BackupImageView)findViewById(R.id.settings_avatar_image);

        tipInfoTextView = (TextView)findViewById(R.id.login_register_info);
        
        
        tipInfoTextView.setText(LocaleController.getString("RegisterText", R.string.RegisterText));
        

        wrongNumber = (TextView) findViewById(R.id.changed_mind);
        wrongNumber.setText(LocaleController.getString("CancelRegistration", R.string.CancelRegistration));

        wrongNumber.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                delegate.setPage(0, true, null, true);
            }
        });

        /*firstNameField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_NEXT) {
                    lastNameField.requestFocus();
                    return true;
                }
                return false;
            }
        });*/
        
        firstNameField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_NEXT) {
                	mInputPassword.requestFocus();
                    return true;
                }
                return false;
            }
        });
        lastNameField.setVisibility(View.GONE);
    }

    public void resetAvatar() {

    }

    @Override
    public void onDestroyActivity() {
        super.onDestroyActivity();
    }

    @Override
    public void onBackPressed() {
        currentParams = null;
    }

    @Override
    public String getHeaderName() {
    	if(resetPwd)
    		return getResources().getString(R.string.RegisterTextResetPwd);
        return getResources().getString(R.string.YourName);
    }

    @Override
    public void onShow() {
        super.onShow();
        if (firstNameField != null) {
            firstNameField.requestFocus();
            firstNameField.setSelection(firstNameField.length());
        }
    }

    @Override
    public void setParams(Bundle params, boolean back) {
        if (params == null) 
        {
            return;
        }
        
        //xueqiang add
        account = params.getString("phoneOrEmailValue");
        viewIndex = params.getInt("dataType",0);
        resetPwd = params.getBoolean("resetPwd");        
        hashCode = params.getString("hashCode");    
        
        if(resetPwd)
        {
        	firstNameField.setVisibility(View.GONE);
        	lastNameField.setVisibility(View.GONE);
        	wrongNumber.setVisibility(View.GONE);
        	if(tipInfoTextView!=null)
        		tipInfoTextView.setText(LocaleController.getString("RegisterTextResetPwd", R.string.RegisterTextResetPwd));
        }
        
        //xueqiang end
        firstNameField.setText("");
        lastNameField.setText("");
        requestPhone = params.getString("phoneFormated");
        phoneHash = params.getString("phoneHash");
        phoneCode = params.getString("code");
        currentParams = params;
        resetAvatar();
    }

    @Override
    public void onNextPressed() 
    {
    	//xueqiang add
    	//if(!EmmUtil.isPhone(account))
        //{
    		if(resetPwd)
    			resetPwd();
    		else
    			Login();
        	return;
        //}
       
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, firstNameField.getText().toString(), lastNameField.getText().toString(), currentParams);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        currentParams = savedState.params;
        if (currentParams != null) {
            setParams(currentParams,false);
        }
        firstNameField.setText(savedState.firstName);
        lastNameField.setText(savedState.lastName);
    }

    protected static class SavedState extends BaseSavedState {
        public String firstName;
        public String lastName;
        public Bundle params;

        private SavedState(Parcelable superState, String text1, String text2, Bundle p1) {
            super(superState);
            firstName = text1;
            lastName = text2;
            if (firstName == null) {
                firstName = "";
            }
            if (lastName == null) {
                lastName = "";
            }
            params = p1;
        }

        private SavedState(Parcel in) {
            super(in);
            firstName = in.readString();
            lastName = in.readString();
            params = in.readBundle();
        }

        @Override
        public void writeToParcel(Parcel destination, int flags) {
            super.writeToParcel(destination, flags);
            destination.writeString(firstName);
            destination.writeString(lastName);
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
    
    private void Login()
    {
    	if(!CheckRegInfo())
		{
			return;
		}
	
		if (delegate != null)
            delegate.needShowProgress();
		
		
		final String pwd = mInputPassword.getText().toString().trim();
		
		String firstName = firstNameField.getText().toString().trim();
		String lastName = lastNameField.getText().toString().trim();
		
		ConnectionsManager.getInstance().Register(account,pwd,firstName,lastName,hashCode, new RPCRequest.RPCRequestDelegate() 
    	{			
			@Override
			public void run(TLObject response, final TL_error error) 
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
                        	int result = error.code;
                        	 //{"result":1}:���ʺ��ѱ�ע�ᡢ{"result":0}:ע��ɹ���{"result":-1}:ע��ʧ��;
                        	//wangxm repair û�����״̬�Ŀ�������
                        	if( result == 1 )
                        	{
                        		// �˺��ѱ�ע��
                        		delegate.needShowAlert(ApplicationLoader.applicationContext.getString(R.string.UserExist));
                        	}                        	
                        	if( result == 2 )
                        	{
                        		// �˺��ѱ�ע��
                        		delegate.needShowAlert(ApplicationLoader.applicationContext.getString(R.string.UserExist));
                        	}
                        	else if( result == -1)
                        	{
                        		// ע��ʧ��
                        		delegate.needShowAlert(ApplicationLoader.applicationContext.getString(R.string.RegisterFaild));
                        	}
                        	else if( result == -2)
                        	{
                        		// ע��ʧ��
                        		delegate.needShowAlert(ApplicationLoader.applicationContext.getString(R.string.WaitingForNetwork));
                        	}
                        	
                        }
                    });
                    return;
                }
                
                Utilities.RunOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                    	CheckLogin(pwd);
                    }
                });
			}	
      });    	
    }
    
    private void  CheckLogin(final String pwd)
    {
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
                        	else if( result == -2)
                        	{
                        		if (delegate != null) {
                        		delegate.needShowAlert(ApplicationLoader.applicationContext.getString(R.string.WaitingForNetwork));
                        		}
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
                        UserConfig.saveConfig(true);
                        MessagesStorage.getInstance().openDatabase();
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
    private void resetPwd()
    {		
		final String pwd = mInputPassword.getText().toString();		
		boolean bPasswordEmpty =  (mInputPassword.isEnabled() && mInputPassword.getText().toString().isEmpty());
    	boolean bRePasswordEmpty = (mReInputPassword.isEnabled() && mReInputPassword.getText().toString().isEmpty());
    	
    	String sPassword = mInputPassword.getText().toString();
    	String sRePassword = mReInputPassword.getText().toString();
    	String errorText;
    	if (bPasswordEmpty || bRePasswordEmpty ) 
		{   
    		errorText = LocaleController.getString("PasswordNoMatch", R.string.hint_password);
    		delegate.needShowAlert(errorText);
    		return;
		}
    	if(!sPassword.equals(sRePassword))
    	{
    		errorText = LocaleController.getString("PasswordNoMatch", R.string.PasswordNoMatch);
    		delegate.needShowAlert(errorText);
    		return;
    	}

    	if (delegate != null)
            delegate.needShowProgress();
    	
    	ConnectionsManager.getInstance().ResetPassword(account, pwd, hashCode, new RPCRequest.RPCRequestDelegate() 
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
                        	else if( result == -2)
                        	{
                        		if (delegate != null) {
                        		delegate.needShowAlert(ApplicationLoader.applicationContext.getString(R.string.WaitingForNetwork));
                        		}
                        	}
                        }
                    });
                    return;
                }
                
                Utilities.RunOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        //��¼�ɹ�
                    	CheckLogin(pwd);
                    }
                });
				
			}	
      });    
    }
    private boolean CheckRegInfo()
    {
    	boolean bRet = true;
    	
    	boolean bFirstNameEmpty = firstNameField.getText().toString().isEmpty();
    	boolean bLastNameEmpty = lastNameField.getText().toString().isEmpty(); 
    	boolean bPasswordEmpty =  (mInputPassword.isEnabled() && mInputPassword.getText().toString().isEmpty());
    	boolean bRePasswordEmpty = (mReInputPassword.isEnabled() && mReInputPassword.getText().toString().isEmpty());
    	
    	String sPassword = mInputPassword.getText().toString();
    	String sRePassword = mReInputPassword.getText().toString();
    	
    	String errorText = "";
    	if (bFirstNameEmpty || bPasswordEmpty || bRePasswordEmpty)
    	{
    		errorText = LocaleController.getString("PleaseEnter", R.string.PleaseEnter);
    		if (bFirstNameEmpty) 
    		{
				errorText += LocaleController.getString("onlyFirstName", R.string.onlyFirstName);
			}
    		
    		/*if (bLastNameEmpty) 
    		{   
    			if(bFirstNameEmpty && (bPasswordEmpty || bRePasswordEmpty))
    			{
    				errorText += ",";
    			}
    			else if (bFirstNameEmpty && !bPasswordEmpty && !bRePasswordEmpty) 
    			{
    				errorText += LocaleController.getString("And", R.string.And);
				}
				errorText += LocaleController.getString("onlyLastName", R.string.onlyLastName);
			}*/
    		
    		if (bPasswordEmpty || bRePasswordEmpty) 
    		{   
    			if(bFirstNameEmpty && bLastNameEmpty)
    			{
    				errorText += LocaleController.getString("And", R.string.And);
    			}
    			else if ((bFirstNameEmpty && !bLastNameEmpty )|| (!bFirstNameEmpty && bLastNameEmpty) )
    			{
    				errorText += LocaleController.getString("And", R.string.And);
				}
    			else if (!bFirstNameEmpty && !bLastNameEmpty) 
    			{
					
				}
    			else
    			{
    				errorText += ",";
				}
				errorText += LocaleController.getString("Password", R.string.Password);
			}
    		
			bRet = false;
		}
    	
    	if (!bPasswordEmpty && !bRePasswordEmpty) 
    	{
			if (!sPassword.equals(sRePassword)) 
			{
				bRet = false;
				errorText = LocaleController.getString("PasswordNoMatch", R.string.PasswordNoMatch);
			}
		}
    	
    	if (!bRet && !errorText.isEmpty()) 
    	{// tips
    		delegate.needShowAlert(errorText);
		}
    	return bRet;
    }
    
}

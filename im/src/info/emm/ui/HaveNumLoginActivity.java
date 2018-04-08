package info.emm.ui;
import info.emm.messenger.LocaleController;

import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
//xueqiang Ŀ��Ϊ�˻�ȡ�ֻ�ΨһID��ANDRIOD pad todo..


public class HaveNumLoginActivity extends ActionBarActivity{
	private TextView mTranslatePhoneLoginView;
	/**
	 * emial������ֵ
	 */
	private EditText email_value;
	/**
	 * ���������ֵ
	 */
	private EditText password_value;
	/**
	 * ��¼��ť
	 */
	private Button login_button;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub     
		super.onCreate(savedInstanceState);
		setContentView(R.layout.have_number_login);
		
		getSupportActionBar().setLogo(R.drawable.ab_icon_fixed2); 
        getSupportActionBar().show();
        
        getSupportActionBar().setTitle(getResources().getString(R.string.PhonenumorEmail));
        
        TextView create_new_account = (TextView) findViewById(R.id.create_new_account);
        email_value = (EditText)findViewById(R.id.email_phonenum_input);
        password_value = (EditText)findViewById(R.id.login_passworld);
        login_button = (Button)findViewById(R.id.main_login_btn);
        login_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// jenf ���������Ϣ�ĺϷ���
				if (!CheckLoginInfo()) {
					return;
				}	
				
				/*int login_flag = EmmUtil.CheckLogin(email_value.getText().toString(), password_value.getText().toString());
				int login_flag = 0;
				switch (login_flag) {
					case LoginResult.CHECK_ACCOUNT_NOT_ACTIVATING:
						ShowAlertDialog(HaveNumLoginActivity.this,ApplicationLoader.applicationContext.getString(R.string.AccountNoActication));
						break;
					case LoginResult.CHECK_ACCOUNT_FREEZE:
						ShowAlertDialog(HaveNumLoginActivity.this,ApplicationLoader.applicationContext.getString(R.string.AccountFreeze));
						break;
					case LoginResult.CHECK_ACCOUNT_DELETE:
						ShowAlertDialog(HaveNumLoginActivity.this,ApplicationLoader.applicationContext.getString(R.string.AccountDelete));
						break;
					case LoginResult.CHECK_ACCOUNT_UPDATE_DEVICE_FAILED:
						ShowAlertDialog(HaveNumLoginActivity.this,ApplicationLoader.applicationContext.getString(R.string.DeviceUpdateFaild));
						break;
					case LoginResult.CHECK_ACCOUNT_PWDERROR:
						ShowAlertDialog(HaveNumLoginActivity.this,ApplicationLoader.applicationContext.getString(R.string.PasswordError));
						break;
					case LoginResult.CHECK_ACCOUNT_ACCOUNTERROR:
						ShowAlertDialog(HaveNumLoginActivity.this,ApplicationLoader.applicationContext.getString(R.string.AccountError));
						break;					
					case CheckIDCodeResult.UserIdIsExist:
						//xueqiang todo..
						//MessagesStorage.getInstance().Login();
					  	
						// muzf to xueqiang �ֶ���¼ ����userid
						//�����������
				        ConnectionsManager.getInstance().StartNetWorkService();
						// jenf opendb
				        //MessagesStorage.getInstance().openDatabase();
				        
					  	Intent intent2 = new Intent(HaveNumLoginActivity.this, LaunchActivity.class);
				        startActivity(intent2);
				        finish();
						break;
					default:
						String errorText = ErrorResult.getErrorText(login_flag);
						ShowAlertDialog(HaveNumLoginActivity.this,errorText);
						break;
				}*/
			}
		});
        
        mTranslatePhoneLoginView = (TextView) findViewById(R.id.phonenum_login);
        
        mTranslatePhoneLoginView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Bundle mBundle = new Bundle();
				mBundle.putString("ORIGIN", "ORIGIN");
				Intent mIntent = new Intent(HaveNumLoginActivity.this, LoginActivity.class);
				mIntent.putExtras(mBundle);
				startActivity(mIntent);
			}
		});
        
        create_new_account.setOnClickListener(new createNewNumListenner());
	}
	
	public void ShowAlertDialog(final Activity activity, final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(HaveNumLoginActivity.this.getString(R.string.AppName));
                    builder.setMessage(message);
                    builder.setPositiveButton(ApplicationLoader.applicationContext.getString(R.string.OK), null);
                    builder.show().setCanceledOnTouchOutside(true);
                }
            }
        });
    }
	
	private class createNewNumListenner implements OnClickListener{
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
          Intent intent2 = new Intent(HaveNumLoginActivity.this, LoginActivity.class);
          startActivity(intent2);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}
	
	private boolean CheckLoginInfo()
	{
		boolean bAccountEmpty = email_value.getText().toString().isEmpty();
		boolean bPasswordEmpty = password_value.getText().toString().isEmpty();
		
		String sEmail = email_value.getText().toString();
		String errorText = "";
		boolean bRet = true;
		
		if (bAccountEmpty || bPasswordEmpty) 
		{
			bRet = false;
			errorText += LocaleController.getString("PleaseEnter", R.string.PleaseEnter);
			
			if (bAccountEmpty) 
			{
				errorText += LocaleController.getString("Account", R.string.Account);
			}
			
			if (bPasswordEmpty) 
			{
				if (bAccountEmpty) 
				{
					errorText += LocaleController.getString("And", R.string.And);
				}
				errorText += LocaleController.getString("Password", R.string.Password);
			}
		}
		else if (!bAccountEmpty && !Utilities.CheckEmail(sEmail)) 
		{
			bRet = false;
			if (sEmail.equals("2")) 
			{// jenf for test account "2"
				bRet = true;
			}
			
			errorText += LocaleController.getString("InvalidEmailAdrress", R.string.InvalidEmailAdrress);
		}
		
		if (!bRet && !errorText.isEmpty()) 
		{
			ShowAlertDialog(HaveNumLoginActivity.this, errorText);
		}
		return bRet;
	}
}

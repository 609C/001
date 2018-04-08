package info.emm.ui;






import info.emm.LocalData.Config;
import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.MessagesStorage;
import info.emm.messenger.RPCRequest.RPCRequestDelegate;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Fragment_Company_login extends BaseFragment {
	private EditText edt_domain;
	private EditText edt_account;
	private EditText edt_pwd;
	private Button txt_login;

	@Override
	public void onCreate(Bundle savedInstanceState) { 
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		fragmentView = inflater.inflate(R.layout.company_login, null);
		edt_domain = (EditText) fragmentView.findViewById(R.id.edt_domain);
		edt_account = (EditText) fragmentView.findViewById(R.id.edt_account);
		edt_pwd = (EditText) fragmentView.findViewById(R.id.edt_pwd);
		txt_login = (Button) fragmentView.findViewById(R.id.txt_login);
		edt_domain.setText(UserConfig.domain);
		edt_domain.requestFocus();
		edt_account.setText(UserConfig.pubcomaccount);
		txt_login.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				onNextAction();
			}
		});
		if(!UserConfig.isPublic){
			edt_domain.setVisibility(View.GONE);
		}
		UserConfig.isPersonalVersion = false;
		UserConfig.saveConfig(false);
		return fragmentView;
	}

	

	public void showDialog(String s) {
		AlertDialog.Builder builder = new Builder(getActivity());
		builder.setTitle(R.string.remind);
		builder.setMessage(s).setPositiveButton(R.string.sure, null).show()
				.setCanceledOnTouchOutside(true);
	}

	@Override
	public void applySelfActionBar() {
		ActionBar actionBar = super.applySelfActionBar(true);

		if (actionBar == null)
			return;

		actionBar.setTitle(getString(R.string.app_name));

		TextView title = (TextView) getActivity()
				.findViewById(R.id.action_bar_title);
		if (title == null) {
			final int subtitleId = getResources().getIdentifier(
					"action_bar_title", "id", "android");
			title = (TextView) getActivity()
					.findViewById(subtitleId);
		}
		if (title != null) {
			title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			title.setCompoundDrawablePadding(0);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		inflater.inflate(R.menu.group_create_menu, menu);
//		SupportMenuItem doneItem = (SupportMenuItem) menu
//				.findItem(R.id.done_menu_item);
//		TextView doneTextView = (TextView) doneItem.getActionView()
//				.findViewById(R.id.done_button);
//		doneTextView.setText(getResources().getString(R.string.done));
//		doneTextView.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				onNextAction();
//				if(UserConfig.clientUserId!=0){					
//					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//					imm.hideSoftInputFromWindow(
//							view.getWindowToken(), 0);
//				}
//			}
//		});
		super.onCreateOptionsMenu(menu, inflater);
	}

	  public void needShowProgress() {
	        Utilities.ShowProgressDialog(this.getActivity(), getResources().getString(R.string.Loading));
	    }

	    
	    public void needHideProgress() {
	        Utilities.HideProgressDialog(this.getActivity());
	    }
	    
	public void onNextAction() {
		final String strDomain = edt_domain.getText().toString().trim();
		final String strAccount = edt_account.getText().toString().trim();
		final String strPassword = edt_pwd.getText().toString().trim();
		Log.d("emm", "httpserver="+Config.getWebHttp());
		needShowProgress();
		ConnectionsManager.getInstance().CheckLogin(strDomain, strAccount, strPassword,new RPCRequestDelegate() {
			
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
                        		
                        		ShowAlertDialog(getActivity(),ApplicationLoader.applicationContext.getString(R.string.AccountNoActication));
    							
                        	}
                        	else if( result == 2)
                        	{
                        		
                        			ShowAlertDialog(getActivity(),ApplicationLoader.applicationContext.getString(R.string.AccountFreeze));
    							
                        	}
                        	else if( result == 3)
                        	{
                        		
                        		ShowAlertDialog(getActivity(),ApplicationLoader.applicationContext.getString(R.string.DeviceUpdateFaild));
    							
                        	}
                        	else if( result == 4)
                        	{
                        		
                        		ShowAlertDialog(getActivity(),ApplicationLoader.applicationContext.getString(R.string.PasswordError));
    							
                        	}
                        	else if( result == 5 || result==7)
                        	{
                        		
    							
                        		ShowAlertDialog(getActivity(),ApplicationLoader.applicationContext.getString(R.string.PasswordError));
                        	} else if(result == 6){
                        		ShowAlertDialog(getActivity(),ApplicationLoader.applicationContext.getString(R.string.AccountError));
                        		
                        	}        
                        	
                        	else if( result == -2)
                        	{
                        		
                        		ShowAlertDialog(getActivity(),ApplicationLoader.applicationContext.getString(R.string.WaitingForNetwork));
                        		
                        	}
                        }
                    });
                    return;
                }
                
                Utilities.RunOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        final TLRPC.TL_auth_authorization res = (TLRPC.TL_auth_authorization)response;       				 
                        UserConfig.clearConfig();
                        MessagesStorage.getInstance().cleanUp();
                        //MessagesController.getInstance().cleanUp();
                        UserConfig.currentUser = res.user;
                        UserConfig.clientActivated = true;
                        UserConfig.clientUserId = res.user.id;
                        UserConfig.domain = strDomain;
                        UserConfig.pubcomaccount = strAccount;
                        UserConfig.isPersonalVersion = false;
                        UserConfig.saveConfig(true);
                        MessagesStorage.getInstance().openDatabase();
                        MessagesController.getInstance().users.put(res.user.id, res.user);
                        
                        FileLog.d("emm", "check login result:" + UserConfig.clientUserId + " sid:" + UserConfig.currentUser.sessionid);
                        
                    	//sam
                        try {
                        	AccountManager accountManager = AccountManager.get(ApplicationLoader.applicationContext);
                            Account myAccount  = new Account(strAccount, "info.emm.weiyicloud.account");
                            accountManager.addAccountExplicitly(myAccount, strPassword, null);
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
	
	
	public void needFinishActivity() {
        Intent intent2 = new Intent(getActivity(), LaunchActivity.class);
        startActivity(intent2);
        //xueqiang change begin
        Utilities.HideProgressDialog(getActivity());
      //xueqiang change end
        getActivity().finish();
    }
	public void ShowAlertDialog(final Activity activity, final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!getActivity().isFinishing()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(LocaleController.getString("AppName", R.string.app_name));
                    builder.setMessage(message);
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                    builder.show().setCanceledOnTouchOutside(true);
                }
            }
        });
    }
	@Override
	 public boolean onOptionsItemSelected(MenuItem item) {
	        int itemId = item.getItemId();
	        switch (itemId) {
	            case android.R.id.home:
	            	
	            	((IntroActivity)getActivity()).onBackPressed();
	            	
	                break;
	        }
	        return true;
	 }
	@Override
	public void onResume() 
	{
		super.onResume();
		Utilities.showKeyboard(edt_domain);
		UserConfig.isPersonalVersion = false;
		UserConfig.saveConfig(false);
		((IntroActivity) parentActivity).showActionBar();
		((IntroActivity) parentActivity).updateActionBar();
		
	}
}

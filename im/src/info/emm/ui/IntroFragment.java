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
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog.Builder;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import info.emm.LocalData.Config;
import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.MessagesStorage;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.messenger.RPCRequest.RPCRequestDelegate;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

/**
 * ��ҳ
 * @author Administrator
 *
 */
@SuppressLint("ResourceAsColor") 
public class IntroFragment extends BaseFragment implements OnClickListener {
	private Button joinmeeting;
	private TextView tab_public;
	private TextView tab_personal;
	private Button btn_personal;
	private Button btn_company;
	private EditText edt_domain;
	private EditText edt_account;
	private EditText edt_pwd;
	private Button txt_login;
	private ImageView introl_iv;
	private RelativeLayout rl_image;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(fragmentView == null){
			fragmentView = inflater.inflate(R.layout.fragment_login_switch, null);
			rl_image = (RelativeLayout)fragmentView.findViewById(R.id.rl_image);
			introl_iv = (ImageView) fragmentView.findViewById(R.id.introl_iv);
			SoftKeyBoardListener.setListener(getActivity(), new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
	            @Override
	            public void keyBoardShow(int height) {
//	                Toast.makeText(getActivity(), "键盘显示 高度" + height, Toast.LENGTH_SHORT).show();
	            	 introl_iv.setBackground(null);
	            }

	            @Override
	            public void keyBoardHide(int height) {
//	                Toast.makeText(getActivity(), "键盘隐藏 高度" + height, Toast.LENGTH_SHORT).show();
	            	introl_iv.setBackgroundResource(R.drawable.intro_logo);
	            }
	        });
//			joinmeeting = (Button) fragmentView.findViewById(R.id.joinmeeting);
			tab_public = (TextView) fragmentView.findViewById(R.id.tab_public);
			tab_personal = (TextView) fragmentView.findViewById(R.id.tab_personal);
//			btn_personal = (Button) fragmentView.findViewById(R.id.btn_personal);
//			btn_company = (Button) fragmentView.findViewById(R.id.btn_company);
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

			if(!UserConfig.isPublic){
				tab_public.setBackgroundResource(R.color.white);
				tab_personal.setBackgroundResource(R.color.weiyi_btn_blue);
				tab_public.setTextColor(R.id.blank_header);
				tab_personal.setTextColor(R.color.white);
//				btn_personal.setText(R.string.Login);
//				btn_company.setText(R.string.set_httpserver);
//				btn_personal.setBackgroundResource(R.color.weiyi_btn_blue);
//				joinmeeting.setBackgroundResource(R.color.weiyi_btn_blue);
//				btn_personal.setClickable(true);
//				joinmeeting.setClickable(true);
				//Config.setWebHttp(UserConfig.privateWebHttp);
			}else{
				//Config.setWebHttp(Config.publicWebHttp);
			}

			tab_personal.setOnClickListener(this);
			tab_public.setOnClickListener(this);
//			btn_personal.setOnClickListener(this);
//			btn_company.setOnClickListener(this);


			/*joinmeeting.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					JointoMeeting_Fragment jointoMeeting_Fragment = new JointoMeeting_Fragment();
					Bundle bundle = new Bundle();
					bundle.putInt("type", 2);
					jointoMeeting_Fragment.setArguments(bundle);
					((IntroActivity)getActivity()).presentFragment(jointoMeeting_Fragment, "", false);
				}
			});*/
		}else{
			ViewGroup parent = (ViewGroup)fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		return fragmentView;
	}



	@Override
	public void onResume() {
		if(!UserConfig.isPublic){
			tab_public.setBackgroundResource(R.color.white);
			tab_personal.setBackgroundResource(R.color.weiyi_btn_blue);
			tab_public.setTextColor(R.id.blank_header);
			tab_personal.setTextColor(R.color.white);
//			btn_personal.setText(R.string.Login);
//			btn_company.setText(R.string.set_httpserver);
//			btn_personal.setBackgroundResource(R.color.weiyi_btn_blue);
//			joinmeeting.setBackgroundResource(R.color.weiyi_btn_blue);
//			btn_personal.setClickable(true);
//			joinmeeting.setClickable(true);
			//Config.setWebHttp(UserConfig.privateWebHttp);
		}else{
//			btn_personal.setText(R.string.str_personal);
//			btn_company.setText(R.string.str_company);
			//Config.setWebHttp(Config.publicWebHttp);
		}
//		joinmeeting.setText(R.string.joinmeetings);
		updateActionBarTitle_();
		
		if(ApplicationLoader.edition == 2)
		{
//			btn_personal.setVisibility(View.GONE);
//			btn_company.setVisibility(View.GONE);
		}
		else if(ApplicationLoader.edition == 3)
		{
//			joinmeeting.setVisibility(View.GONE);
//			btn_personal.setVisibility(View.GONE);
		}
		Utilities.showKeyboard(edt_domain);
		UserConfig.isPersonalVersion = false;
		UserConfig.saveConfig(false);
		((IntroActivity) parentActivity).showActionBar();
		((IntroActivity) parentActivity).updateActionBar();
		super.onResume();
	}

	public void updateActionBarTitle_() {
		if (parentActivity != null) {
			ActionBar actionBar = parentActivity.getSupportActionBar();
			actionBar.setTitle(R.string.AppName);	
		}
	}

	@SuppressLint("ResourceAsColor") @Override
	public void onClick(View arg0) {
		int i = arg0.getId();
		if (i == R.id.tab_public) {
			tab_public.setBackgroundResource(R.color.weiyi_btn_blue);
			tab_personal.setBackgroundResource(R.color.white);
			tab_public.setTextColor(R.color.white);
			tab_personal.setTextColor(R.id.blank_header);
//			btn_personal.setText(R.string.str_personal);
//			btn_company.setText(R.string.str_company);
//			btn_personal.setBackgroundResource(R.color.weiyi_btn_blue);
//			joinmeeting.setBackgroundResource(R.color.weiyi_btn_blue);
//			btn_personal.setClickable(true);
//			joinmeeting.setClickable(true);
			UserConfig.isPublic = true;
			UserConfig.saveConfig(false);
			if (UserConfig.isPublic) {
				Config.setWebHttp(Config.publicWebHttp);
			} else {
				Config.setWebHttp(UserConfig.privateWebHttp);
			}

		} else if (i == R.id.tab_personal) {
			tab_public.setBackgroundResource(R.color.white);
			tab_personal.setBackgroundResource(R.color.weiyi_btn_blue);
			tab_public.setTextColor(R.id.blank_header);
			tab_personal.setTextColor(R.color.white);
//			btn_personal.setText(R.string.Login);
//			btn_company.setText(R.string.set_httpserver);
			if (UserConfig.privateWebHttp != null && !UserConfig.privateWebHttp.isEmpty()) {
//				btn_personal.setBackgroundResource(R.color.weiyi_btn_blue);
//				joinmeeting.setBackgroundResource(R.color.weiyi_btn_blue);
//				btn_personal.setClickable(true);
//				joinmeeting.setClickable(true);
			} else {
//				btn_personal.setBackgroundResource(R.color.gray);
//				joinmeeting.setBackgroundResource(R.color.gray);
//				btn_personal.setClickable(false);
//				joinmeeting.setClickable(false);
			}
			UserConfig.isPublic = false;
			UserConfig.saveConfig(false);
			if (UserConfig.isPublic) {
				Config.setWebHttp(Config.publicWebHttp);
			} else {
				Config.setWebHttp(UserConfig.privateWebHttp);
			}

		/*case R.id.btn_personal:
			if(UserConfig.isPublic){
				PubPerLoginFragment pubPerLoginFragment = new PubPerLoginFragment();
				((IntroActivity)getActivity()).presentFragment(pubPerLoginFragment, "", false);
			}else{
				PriLoginFragment priLoginFragment = new PriLoginFragment();
				((IntroActivity)getActivity()).presentFragment(priLoginFragment, "", false);
			}
			break;*/
		/*case R.id.btn_company:
			if(UserConfig.isPublic){
				Fragment_Company_login company_login = new Fragment_Company_login();
				((IntroActivity)getActivity()).presentFragment(company_login, "", false);
			}else{
				SetHttpFragment personal_login = new SetHttpFragment();
				((IntroActivity)getActivity()).presentFragment(personal_login, "", false);
			}
			break;*/
		}
		//		if(UserConfig.isPublic){
		//			Config.setWebHttp(Config.publicWebHttp);
		//		}else{
		//			Config.setWebHttp(UserConfig.privateWebHttp);
		//		}
			

	}

	//��ʾ ����˽������¼����--��ҵ�û���
	/*@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.group_create_menu, menu);
		SupportMenuItem doneItem = (SupportMenuItem) menu
				.findItem(R.id.done_menu_item);
		TextView doneTextView = (TextView) doneItem.getActionView()
				.findViewById(R.id.done_button);
		//		doneTextView.setBackgroundResource(R.drawable.ic_ab_other);
		Drawable drawable= getResources().getDrawable(R.drawable.tittlebar_setting);  
		/// ��һ������Ҫ��,���򲻻���ʾ.  
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());  
		doneTextView.setCompoundDrawables(drawable,null,null,null);  
		//		doneTextView.setText(getResources().getString(R.string.done));
		doneTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showPopupWindow(view);
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(
						view.getWindowToken(), 0);
			}
		});
		super.onCreateOptionsMenu(menu, inflater);
	}*/
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(ApplicationLoader.isprivate() ? false : true);
	}

	private void showPopupWindow(View view) {

		// һ���Զ���Ĳ��֣���Ϊ��ʾ������
		View contentView = LayoutInflater.from(getActivity()).inflate(
				R.layout.radio_popup, null);

		final PopupWindow popupWindow = new PopupWindow(contentView,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		RadioGroup mRadioGroup1 = (RadioGroup) contentView.findViewById(R.id.radio_group);
		final RadioButton mRadiopublic = (RadioButton) contentView.findViewById(R.id.rad_public);
		final RadioButton mRadiopersonal = (RadioButton) contentView.findViewById(R.id.rad_personal);
		if(UserConfig.isPublic){
			mRadiopublic.setChecked(true);
		}else{
			mRadiopersonal.setChecked(true);
		}
		mRadioGroup1.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				if (arg1 == mRadiopublic.getId()) {
					Log.d("emm", "public");
					tab_public.setBackgroundResource(R.color.weiyi_btn_blue);
					tab_personal.setBackgroundResource(R.color.white);
					tab_public.setTextColor(R.color.white);
					tab_personal.setTextColor(R.id.blank_header);
//					btn_personal.setText(R.string.str_personal);
//					btn_company.setText(R.string.str_company);
//					btn_personal.setBackgroundResource(R.color.weiyi_btn_blue);
//					joinmeeting.setBackgroundResource(R.color.weiyi_btn_blue);
//					btn_personal.setClickable(true);
//					joinmeeting.setClickable(true);
					UserConfig.isPublic = true;
					UserConfig.saveConfig(false);
					popupWindow.dismiss();
				} else if (arg1 == mRadiopersonal.getId()) {
					Log.d("emm", "personal");
					tab_public.setBackgroundResource(R.color.white);
					tab_personal.setBackgroundResource(R.color.weiyi_btn_blue);
					tab_public.setTextColor(R.id.blank_header);
					tab_personal.setTextColor(R.color.white);
//					btn_personal.setText(R.string.Login);
//					btn_company.setText(R.string.set_httpserver);
					if(UserConfig.privateWebHttp!=null&&!UserConfig.privateWebHttp.isEmpty()){
//						btn_personal.setBackgroundResource(R.color.weiyi_btn_blue);
//						joinmeeting.setBackgroundResource(R.color.weiyi_btn_blue);
//						btn_personal.setClickable(true);
//						joinmeeting.setClickable(true);
					}else{
//						btn_personal.setBackgroundResource(R.color.gray);
//						joinmeeting.setBackgroundResource(R.color.gray);
//						btn_personal.setClickable(false);
//						joinmeeting.setClickable(false);
					}
					UserConfig.isPublic = false;
					UserConfig.saveConfig(false);
					popupWindow.dismiss();
				}

			}
		});
		popupWindow.setTouchable(true);


		// ���������PopupWindow�ı����������ǵ���ⲿ������Back�����޷�dismiss����
		// �Ҿ���������API��һ��bug
		popupWindow.setBackgroundDrawable(getResources().getDrawable(
				R.color.graywhite));

		// ���úò���֮����show
		popupWindow.showAsDropDown(view);

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


}

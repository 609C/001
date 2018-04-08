/**
 * @Title        : EmailLinkActivity.java
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
 */
package info.emm.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.StringUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

public class EmailLinkActivity extends BaseFragment implements
NotificationCenter.NotificationCenterDelegate {
	private boolean toLink;
	public static enum EmailType{
		AddEmail,
		UnlinkEmail
	}
	private EmailType emailType = EmailType.AddEmail;
	
	
	private EditText edtEmail;
	private String emailAddress;
	private String email;
	
	@Override
	public void didReceivedNotification(int id, Object... args) {
		Utilities.HideProgressDialog(parentActivity);
		if(id == MessagesController.getcode_success){
			  VerifyCodeActivity fragment = new VerifyCodeActivity();
              Bundle bundle = new Bundle();
              bundle.putString("verifyAccount", emailAddress);
              fragment.setArguments(bundle);
              ((LaunchActivity)parentActivity).presentFragment(fragment, "", false);
		}else if(id == MessagesController.getcode_failed){
			Utilities.showToast(parentActivity, LocaleController.getString("",R.string.GetCodeError));
		}
		else if(id == MessagesController.unbind_account_success){
			Utilities.showToast(parentActivity, LocaleController.getString("",R.string.unbindAccountSuccess));
		}
		else if(id == MessagesController.unbind_account_failed){
			Utilities.showToast(parentActivity, LocaleController.getString("",R.string.unbindAccountError));
		}
	}
	@Override
	public boolean onFragmentCreate() {
		super.onFragmentCreate();
		emailType = EmailType.values()[getArguments().getInt("emailType")];
		email = getArguments().getString("email");
		toLink = (emailType == EmailType.AddEmail);
		NotificationCenter.getInstance().addObserver(this, MessagesController.getcode_failed);
		NotificationCenter.getInstance().addObserver(this, MessagesController.getcode_success);
		NotificationCenter.getInstance().addObserver(this, MessagesController.unbind_account_success);
		NotificationCenter.getInstance().addObserver(this, MessagesController.unbind_account_failed);
		return true;
	}
	@Override
	public void onFragmentDestroy() {
		super.onFragmentDestroy();
		NotificationCenter.getInstance().removeObserver(this, MessagesController.getcode_failed);
		NotificationCenter.getInstance().removeObserver(this, MessagesController.getcode_success);
		NotificationCenter.getInstance().removeObserver(this, MessagesController.unbind_account_success);
		NotificationCenter.getInstance().removeObserver(this, MessagesController.unbind_account_failed);
		
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
			fragmentView = inflater.inflate(R.layout.email_modify,
					container, false);
			LinearLayout linkLayout = (LinearLayout)fragmentView.findViewById(R.id.linlayout_link);
			TextView unlinkTv = (TextView)fragmentView.findViewById(R.id.tv_unlink);
			edtEmail = (EditText)fragmentView.findViewById(R.id.et_email);
			linkLayout.setVisibility(toLink?View.VISIBLE:View.GONE);
			unlinkTv.setVisibility(toLink?View.GONE:View.VISIBLE);
//			edtEmail.requestFocus();
			if(toLink)
			{
				edtEmail.setHint(R.string.PleaseUseCompanyEmail);
				
				TextView confirm = (TextView)fragmentView.findViewById(R.id.tv_sure);
				TextView cancel = (TextView)fragmentView.findViewById(R.id.tv_cancel);
				confirm.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						emailAddress = edtEmail.getText().toString(); 
						 if(StringUtil.isEmail(emailAddress)){
							 Utilities.ShowProgressDialog(parentActivity, LocaleController.getString("",R.string.Loading));
							  MessagesController.getInstance().getCode(emailAddress);
						 }else{
							 Utilities.showToast(parentActivity, LocaleController.getString("",R.string.InvalidEmailAdrress));
							 return;
						 }
					}
				});
				cancel.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						 finishFragment();
					}
				});
			}
			else
			{
				edtEmail.setHint(email);
				edtEmail.setEnabled(false);
				unlinkTv.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						/*int size = MessagesController.getInstance().accounts.size();
						if ((size>2&&StringUtil.isEmpty(MessagesController.getInstance().accounts.get(0)))||
							(size>1&&!StringUtil.isEmpty(MessagesController.getInstance().accounts.get(0)))) {
							unLinkDialog();
						}else{
							new AlertDialog.Builder(parentActivity)
									.setTitle(R.string.Alert)
									.setMessage(R.string.unlink_notice)
									.setPositiveButton(R.string.OK,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													dialog.dismiss();
												}
											}).show();
						}*/  
					}
				});
			}
		}else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
		//Utilities.showKeyboard(edtEmail);
		return fragmentView;
	}
	
	 @Override
	    public void applySelfActionBar() {
	        if (parentActivity == null) {
	            return;
	        }
	        ActionBar actionBar =  super.applySelfActionBar(true);
	        actionBar.setTitle(R.string.modifyEmail);

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
	    @Override
	    public void onResume() {
	        super.onResume();
	        if (isFinish) {
	            return;
	        }
	        new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					if(edtEmail != null && toLink){
			        	Utilities.showKeyboard(edtEmail);
			        }
				}
			},100);
	        ((LaunchActivity)parentActivity).showActionBar();
	        ((LaunchActivity)parentActivity).updateActionBar();
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
	    private void unLinkDialog() {
	    	new AlertDialog.Builder(parentActivity)
			.setTitle(R.string.Alert)
			.setMessage(R.string.UnlinkEmail)
			.setPositiveButton(R.string.Unlink, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					 Utilities.ShowProgressDialog(parentActivity, LocaleController.getString("",R.string.Loading));
					 MessagesController.getInstance().bindAccount(email, 1, "",0);
					dialog.dismiss();
				}
			}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}

			}).show();
		}
}

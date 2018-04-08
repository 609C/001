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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.UserConfig;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.StringUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

public class VerifyCodeActivity extends BaseFragment implements
NotificationCenter.NotificationCenterDelegate {
	
    private EditText codeEdit;
	private String verifyAccount;
	private String codeText;
	private String countryCode;
	
	 private boolean waitingForSms = false;
	@Override
	public void didReceivedNotification(int id, final Object... args) {
		Utilities.HideProgressDialog(getActivity());
		if(id == MessagesController.bind_account_success){
//        	VerifyCodeActivity fragment = new VerifyCodeActivity();
//            Bundle bundle = new Bundle();
//            fragment.setArguments(bundle);
//            ((LaunchActivity)parentActivity).presentFragment(fragment, "", false);
			 waitingForSms = false;
			 Utilities.setWaitingForSms(false);
			if(((Integer) args[0]).intValue() == 2){ //��ʾ�˻�����
				new AlertDialog.Builder(parentActivity)
				.setTitle(R.string.Alert)
				.setMessage(R.string.HadBindNotice)
				.setPositiveButton(R.string.BindForce, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						MessagesController.getInstance().bindAccount(verifyAccount,0, codeText,1);
						dialog.dismiss();
					}
				}).setNegativeButton(R.string.cancel, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
				return;
			}
			Utilities.showToast(getActivity(), getResources().getString(R.string.Verified));
			LinkResultActivity fragment = new LinkResultActivity();
			Bundle bundle = new Bundle();
			bundle.putBoolean("isLink", true);
			fragment.setArguments(bundle);
			((LaunchActivity)parentActivity).presentFragment(fragment, "", false);
			
//			UserConfig.coutryCode=countryCode;
			UserConfig.saveConfig(false);
			
		}else if(id == MessagesController.bind_account_failed){
			Utilities.showToast(getActivity(), getResources().getString(R.string.VerifyCodeError));
			 waitingForSms = false;
			 Utilities.setWaitingForSms(false);
		} else if(id == 998){
			 waitingForSms = false;
			 Utilities.setWaitingForSms(false);
            Utilities.RunOnUIThread(new Runnable() {
                @Override
                public void run() {
                    //if (!waitingForSms) {
                    //    return;
                    //}
                    if (codeEdit != null) {
                    	codeEdit.setText("" + args[0]);
                    	nextStep();
                    }
                }
            });
        
		}
	}
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	@Override
	public boolean onFragmentCreate() {
		super.onFragmentCreate();
		NotificationCenter.getInstance().addObserver(this, MessagesController.bind_account_failed);
		NotificationCenter.getInstance().addObserver(this, MessagesController.bind_account_success);
		 NotificationCenter.getInstance().addObserver(this, 998);
		return true;
	}
	@Override
	public void onFragmentDestroy() {
		super.onFragmentDestroy();
		NotificationCenter.getInstance().removeObserver(this, MessagesController.bind_account_failed);
		NotificationCenter.getInstance().removeObserver(this, MessagesController.bind_account_success);
		NotificationCenter.getInstance().removeObserver(this, 998);
		 waitingForSms = false;
		 Utilities.setWaitingForSms(false);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		verifyAccount = getArguments().getString("verifyAccount");
		countryCode = getArguments().getString("countryCode");
		if (fragmentView == null) {
			fragmentView = inflater.inflate(R.layout.setting_input_verifycode,
					container, false);
			viewSet(fragmentView);
		}else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
		 waitingForSms = true;
		 Utilities.setWaitingForSms(true);
		return fragmentView;
	}
	 @Override
	    public void applySelfActionBar() {
	        if (parentActivity == null) {
	            return;
	        }
	        ActionBar actionBar =  super.applySelfActionBar(true);
	        actionBar.setTitle(R.string.VerifyCode);

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
		 TextView textview = (TextView)view.findViewById(R.id.tv_title); 
		 if(StringUtil.isEmail(verifyAccount)){
			 textview.setText(R.string.CallEmailText);
		 }else{
			 textview.setText(StringUtil.getStringFromRes(R.string.SentSmsCode)+verifyAccount);
		 }
		 
		 codeEdit = (EditText)view.findViewById(R.id.edit_code);
		 TextView nextBtn = (TextView)view.findViewById(R.id.tv_next);
		 nextBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				nextStep();
			}
		});
	 }
	 private void nextStep(){
		 if(codeEdit == null){
			 return;
		 }
		 codeText = codeEdit.getText().toString();
			if(StringUtil.isEmpty(codeText)){
				Utilities.showToast(getActivity(), getResources().getString(R.string.NotEmpty));
				return;
			}
			 Utilities.ShowProgressDialog(getActivity(), getResources().getString(R.string.Loading));
			 MessagesController.getInstance().bindAccount(verifyAccount,0, codeText,0);
	 }
//	 @Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//	       inflater.inflate(R.menu.group_profile_menu, menu);
//	        SupportMenuItem doneItem = (SupportMenuItem)menu.findItem(R.id.block_user);
//	        TextView doneTextView = (TextView)doneItem.getActionView().findViewById(R.id.done_button);
//	        doneTextView.setText(LocaleController.getString("AddMember", R.string.AddMember));
//	        doneTextView.setOnClickListener(new View.OnClickListener() {
//	            @Override
//	            public void onClick(View view) {
//	            }
//	        });
//	}
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
	        if(codeEdit != null){
	        	Utilities.showKeyboard(codeEdit);
	        }
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

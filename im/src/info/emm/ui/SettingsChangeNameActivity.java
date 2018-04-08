/*
 * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.ui;

import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.MessagesStorage;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.RPCRequest;
import info.emm.messenger.TLObject;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.ConstantValues;
import info.emm.utils.MaxLengthEdite;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsChangeNameActivity extends BaseFragment {
    private EditText firstNameField;
    private EditText lastNameField;
    private View headerLabelView;
    private View doneButton;
    private Button btn_cancel;
    private Button btn_done;

    public SettingsChangeNameActivity() {
        animationType = 1;
    }

    @Override
    public boolean canApplyUpdateStatus() {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFinish) {
            return;
        }
        if(parentActivity == null)
        	return;
//        ActionBar actionBar = parentActivity.getSupportActionBar();
//        actionBar.setDisplayShowCustomEnabled(true);
//        actionBar.setDisplayShowHomeEnabled(false);
//        actionBar.setDisplayShowTitleEnabled(false);
//        actionBar.setDisplayHomeAsUpEnabled(false);
//        actionBar.setSubtitle(null);
//
//        actionBar.setCustomView(R.layout.settings_do_action_layout);
//        Button cancelButton = (Button)actionBar.getCustomView().findViewById(R.id.cancel_button);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishFragment();
            }
        });
//        doneButton = actionBar.getCustomView().findViewById(R.id.done_button);
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstNameField.getText().length() != 0 )// && lastNameField.getText().length()!=0) 
                {
                    saveName();                    
                }
                else
                {
                	if(parentActivity != null)
                	{
	                	String alertMsg = ApplicationLoader.applicationContext.getString(R.string.fileinfirstnameandlastname);
	      				Utilities.showToast(parentActivity, alertMsg);
                	}
                }
            }
        });

//        cancelButton.setText(LocaleController.getString("Cancel", R.string.Cancel));
//        TextView textView = (TextView)doneButton.findViewById(R.id.done_button_text);
//        textView.setText(LocaleController.getString("Done", R.string.Done));

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig_" + UserConfig.clientUserId, Activity.MODE_PRIVATE);
        boolean animations = preferences.getBoolean("view_animations", true);
        if (!animations) {
            firstNameField.requestFocus();
            Utilities.showKeyboard(firstNameField);
        }
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (nextAnim != 0) {
            Animation anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);

            anim.setAnimationListener(new Animation.AnimationListener() {

                public void onAnimationStart(Animation animation) {
                    SettingsChangeNameActivity.this.onAnimationStart();
                }

                public void onAnimationRepeat(Animation animation) {

                }

                public void onAnimationEnd(Animation animation) {
                    SettingsChangeNameActivity.this.onAnimationEnd();
                    firstNameField.requestFocus();
                    Utilities.showKeyboard(firstNameField);
                }
            });

            return anim;
        } else {
            return super.onCreateAnimation(transit, enter, nextAnim);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.settings_change_name_layout, container, false);

            TLRPC.User user = MessagesController.getInstance().users.get(UserConfig.clientUserId);
            if (user == null) {
                user = UserConfig.currentUser;
            }

            firstNameField = (EditText)fragmentView.findViewById(R.id.first_name_field);
            firstNameField.setHint(LocaleController.getString("FirstName", R.string.FirstName));  
    
            firstNameField.addTextChangedListener(new MaxLengthEdite(ConstantValues.CREATE_NAMESIZE_MAX, firstNameField));
            firstNameField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i == EditorInfo.IME_ACTION_NEXT) {
                        lastNameField.requestFocus();
                        lastNameField.setSelection(lastNameField.length());
                        return true;
                    }
                    return false;
                }
            });
            lastNameField = (EditText)fragmentView.findViewById(R.id.last_name_field);
            lastNameField.setHint(LocaleController.getString("LastName", R.string.LastName));
            	lastNameField.addTextChangedListener(new MaxLengthEdite(6, lastNameField));
            lastNameField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i == EditorInfo.IME_ACTION_DONE) {
                        doneButton.performClick();
                        return true;
                    }
                    return false;
                }
            });

            if (user != null) {
                firstNameField.setText(user.last_name + user.first_name);
                firstNameField.setSelection(firstNameField.length());
//                lastNameField.setText(user.last_name);
            }

            lastNameField.setEnabled(false);
            lastNameField.setVisibility(View.GONE);
            
            TextView headerLabel = (TextView)fragmentView.findViewById(R.id.settings_section_text);
            headerLabel.setText(LocaleController.getString("YourFirstNameAndLastName", R.string.YourFirstNameAndLastName));
            
            btn_cancel = (Button) fragmentView.findViewById(R.id.btn_cancel);
            btn_done = (Button) fragmentView.findViewById(R.id.btn_done);
        } else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        return fragmentView;
    }

    private void saveName() {
        TLRPC.TL_account_updateProfile req = new TLRPC.TL_account_updateProfile();
        if (UserConfig.currentUser == null || lastNameField.getText() == null || firstNameField.getText() == null) {
            return;
        }
        req.first_name = firstNameField.getText().toString();
        req.last_name = lastNameField.getText().toString();
        
                
	
        
        ConnectionsManager.getInstance().performRpc(req, new RPCRequest.RPCRequestDelegate() {
            @Override
            public void run(TLObject response, TLRPC.TL_error error) 
            {	
        		if(error!=null)
        		{	
        			//��Ҫ��ʾ�������
        			Utilities.RunOnUIThread(new Runnable() {
                        @Override
                        public void run() 
                        {   
                        	if(parentActivity != null)
                        	{
	                        	String alertMsg = ApplicationLoader.applicationContext.getString(R.string.WaitingForNetwork);
	              				Utilities.showToast(parentActivity, alertMsg);
                        	}
              				finishFragment();
                        }
                    });      
        		}
        		else
        		{	
        			final TLObject ress = response;
        			Utilities.RunOnUIThread(new Runnable() 
        			{	
                        @Override
                        public void run() 
                        {
                        	 TLRPC.TL_account_updateProfile res = (TLRPC.TL_account_updateProfile)ress;  
                        	 UserConfig.currentUser.first_name = res.first_name;
                             UserConfig.currentUser.last_name = res.last_name;
                             UserConfig.currentUser.nickname = res.first_name;
                             TLRPC.User user = MessagesController.getInstance().users.get(UserConfig.clientUserId);
                             if (user != null) {
                                 user.first_name = res.first_name;
                                 user.last_name = res.last_name;
                                 user.nickname = res.first_name;// qxm add..�޸��ǳ�֮����ͨѶ¼�ı�
                             }
                            ArrayList<TLRPC.User> users = new ArrayList<TLRPC.User>();
                            users.add(user);
                            MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
                            UserConfig.saveConfig(true);        
                        	NotificationCenter.getInstance().postNotificationName(MessagesController.updateInterfaces, MessagesController.UPDATE_MASK_NAME);
                        	NotificationCenter.getInstance().postNotificationName(MessagesController.meeting_list_updateUI);
                        	finishFragment();
                        }
                    });
        		}
            }
        });
    }
    @Override
	public void applySelfActionBar() {
	ActionBar actionBar = super.applySelfActionBar(true);
		
		if(actionBar == null)return;
		
		actionBar.setTitle(getString(R.string.app_name));

		TextView title = (TextView) getActivity()
				.findViewById(R.id.action_bar_title);
		if (title == null) {
			final int subtitleId = getResources().getIdentifier(
					"action_bar_title", "id", "android");
			title = (TextView) getActivity().findViewById(subtitleId);
		}
		if (title != null) {
			title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			title.setCompoundDrawablePadding(0);
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
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
}

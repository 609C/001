/*
 * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.ui;

import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.ConstantValues;
import info.emm.utils.MaxLengthEdite;
import info.emm.utils.StringUtil;
import info.emm.utils.UiUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsAddRemarkActivity extends BaseFragment implements
NotificationCenter.NotificationCenterDelegate {
    private EditText firstNameField;
    private EditText lastNameField;
    private View headerLabelView;
    private View doneButton;
    private Button btn_cancel;
    private Button btn_done;
    
    private int user_id = 0;
    private String remarkString = "";

    public SettingsAddRemarkActivity() {
        animationType = 1;
    }
    
    @Override
    public boolean onFragmentCreate() {
    	super.onFragmentCreate();
    	user_id = getArguments().getInt("userid", 0);
    	remarkString = getArguments().getString("remark");
    	NotificationCenter.getInstance().addObserver(this,
				MessagesController.renamesuccess);
    	NotificationCenter.getInstance().addObserver(this,
				MessagesController.renamefailed);
    	return true;
    }
    @Override
    public void onFragmentDestroy() {
    	super.onFragmentDestroy();
    	NotificationCenter.getInstance().removeObserver(this,
				MessagesController.renamesuccess);
    	NotificationCenter.getInstance().removeObserver(this,
				MessagesController.renamefailed);
    	
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
//        actionBar.setDisplayShowHomeEnabled(ConstantValues.ActionBarShowLogo);
//        actionBar.setDisplayShowTitleEnabled(false);
//        actionBar.setDisplayHomeAsUpEnabled(false);
//        actionBar.setSubtitle(null);

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
            	String nameString = firstNameField.getText().toString().trim();
//                if (!StringUtil.isEmpty(nameString)) 
//                {
                    saveName(nameString);                    
//                }
//                else
//                {
//                	if(parentActivity != null)
//                	{
//	                	String alertMsg = ApplicationLoader.applicationContext.getString(R.string.fileinfirstnameandlastname);
//	      				Utilities.showToast(parentActivity, alertMsg);
//                	}
//                }
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
                    SettingsAddRemarkActivity.this.onAnimationStart();
                }

                public void onAnimationRepeat(Animation animation) {

                }

                public void onAnimationEnd(Animation animation) {
                    SettingsAddRemarkActivity.this.onAnimationEnd();
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
            firstNameField.addTextChangedListener(new MaxLengthEdite(ConstantValues.CREATE_NAMESIZE_MAX, firstNameField));
            
//            firstNameField.setHint();  
//            firstNameField.addTextChangedListener(new MaxLengthEdite(12, firstNameField));
            
//            firstNameField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//                @Override
//                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
//                    if (i == EditorInfo.IME_ACTION_NEXT) {
//                        lastNameField.requestFocus();
//                        lastNameField.setSelection(lastNameField.length());
//                        return true;
//                    }
//                    return false;
//                }
//            });
            lastNameField = (EditText)fragmentView.findViewById(R.id.last_name_field);
            lastNameField.setVisibility(View.GONE);
//            if (user != null) {
//                firstNameField.setText(user.first_name);
//                firstNameField.setSelection(firstNameField.length());
//            }

            TextView headerLabel = (TextView)fragmentView.findViewById(R.id.settings_section_text);
            headerLabel.setText(R.string.add_remark);
            
            if (!StringUtil.isEmpty(remarkString)) {
            	firstNameField.setText(remarkString);
            	 firstNameField.setSelection(firstNameField.length());
            	headerLabel.setText(R.string.modify_remark);
			}
            
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

    private void saveName(String textString) { 
    	MessagesController.getInstance().setRamarkName(UserConfig.clientUserId, user_id, textString);
    }

	@Override
	public void didReceivedNotification(int id, Object... args) {
		if (id == MessagesController.renamesuccess) {
			finishFragment();
		}else if (id == MessagesController.renamefailed) {
			UiUtil.showToast(getActivity(), 0, R.string.modify_remark_failed);
		}
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

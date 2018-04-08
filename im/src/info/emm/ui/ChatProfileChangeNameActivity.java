/*
 * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.ui;

import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.TLRPC;
import info.emm.messenger.TLRPC.TL_Company;
import info.emm.messenger.TLRPC.TL_CompanyInfo;
import info.emm.messenger.UserConfig;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.StringUtil;
import info.emm.utils.UiUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
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

public class ChatProfileChangeNameActivity extends BaseFragment {
	private EditText firstNameField;
	private View headerLabelView;
	private int chat_id;
	private View doneButton;
	private Button btn_cancel;
	private Button btn_done;
	/**
	 * @Fields type : 0 , 1 company
	 */
	private int chat_type = 0;
	TLRPC.Chat currentChat;

	public ChatProfileChangeNameActivity() {
		animationType = 1;
	}

	@Override
	public boolean onFragmentCreate() {
		super.onFragmentCreate();
		chat_id = getArguments().getInt("chat_id", 0);
		chat_type = getArguments().getInt("chat_type", 0);
		return true;
	}

	@Override
	public void onFragmentDestroy() {
		super.onFragmentDestroy();
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
			fragmentView = inflater.inflate(
					R.layout.chat_profile_change_name_layout, container, false);

			firstNameField = (EditText) fragmentView
					.findViewById(R.id.first_name_field);
			firstNameField.setHint(LocaleController.getString("GroupName",
					R.string.GroupName));
			firstNameField
					.setOnEditorActionListener(new TextView.OnEditorActionListener() {
						@Override
						public boolean onEditorAction(TextView textView, int i,
								KeyEvent keyEvent) {
							if (i == EditorInfo.IME_ACTION_DONE) {
								doneButton.performClick();
								return true;
							}
							return false;
						}
					});
			String titleName = "";
			if (chat_type == 1) {
				TL_Company currentCompany = MessagesController.getInstance().companys
						.get(chat_id);
				titleName = currentCompany.name;
			} else {
				currentChat = MessagesController.getInstance().chats
						.get(chat_id);
				if (currentChat.hasTitle == 0) {
					titleName = currentChat.title;
				}
			}
			if (StringUtil.isEmpty(titleName)) {
				firstNameField.setHint(R.string.group_un_name);
			} else {
				firstNameField.setText(titleName);
			}
			firstNameField.setSelection(firstNameField.length());

			TextView headerLabel = (TextView) fragmentView
					.findViewById(R.id.settings_section_text);
			headerLabel.setText(LocaleController.getString(
					"EnterGroupNameTitle",
					chat_type == 1 ? R.string.EnterCompanyNamePlaceholder
							: R.string.EnterGroupNameTitle));

			btn_cancel = (Button) fragmentView.findViewById(R.id.btn_cancel);
			btn_done = (Button) fragmentView.findViewById(R.id.btn_done);

			btn_cancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					finishFragment();
				}
			});
			btn_done.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (!StringUtil.isEmpty(firstNameField.getText().toString()
							.trim())) {
						saveName();
						finishFragment();
					} else {
						UiUtil.showToast(parentActivity, R.string.NotEmpty);
					}
				}
			});
		} else {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		return fragmentView;
	}

	@Override
	public boolean canApplyUpdateStatus() {
		return false;
	}

	@Override
	public void applySelfActionBar() {
		if (parentActivity == null) {
			return;
		}
		// ActionBar actionBar = parentActivity.getSupportActionBar();
		// actionBar.setDisplayShowCustomEnabled(true);
		// actionBar.setDisplayShowHomeEnabled(false);
		// actionBar.setDisplayShowTitleEnabled(false);
		// actionBar.setDisplayHomeAsUpEnabled(false);
		// actionBar.setCustomView(R.layout.settings_do_action_layout);
		// Button cancelButton =
		// (Button)actionBar.getCustomView().findViewById(R.id.cancel_button);
		// cancelButton.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View view) {
		// finishFragment();
		// }
		// });
		// doneButton =
		// actionBar.getCustomView().findViewById(R.id.done_button);
		// doneButton.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View view) {
		// if (!StringUtil.isEmpty(firstNameField.getText().toString().trim()))
		// {
		// saveName();
		// finishFragment();
		// }else {
		// UiUtil.showToast(parentActivity,R.string.NotEmpty);
		// }
		// }
		// });
		//
		// cancelButton.setText(LocaleController.getString("Cancel",
		// R.string.Cancel));
		// TextView textView =
		// (TextView)doneButton.findViewById(R.id.done_button_text);
		// textView.setText(LocaleController.getString("Done", R.string.Done));
	}

	@Override
	public void onResume() {
		super.onResume();
		if (getActivity() == null) {
			return;
		}
		((LaunchActivity) parentActivity).updateActionBar();

		SharedPreferences preferences = ApplicationLoader.applicationContext
				.getSharedPreferences("mainconfig_" + UserConfig.clientUserId,
						Activity.MODE_PRIVATE);
		boolean animations = preferences.getBoolean("view_animations", true);
		if (!animations) {
			firstNameField.requestFocus();
			Utilities.showKeyboard(firstNameField);
		}
	}

	@Override
	public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
		if (nextAnim != 0) {
			Animation anim = AnimationUtils.loadAnimation(getActivity(),
					nextAnim);

			anim.setAnimationListener(new Animation.AnimationListener() {

				public void onAnimationStart(Animation animation) {
					ChatProfileChangeNameActivity.this.onAnimationStart();
				}

				public void onAnimationRepeat(Animation animation) {

				}

				public void onAnimationEnd(Animation animation) {
					ChatProfileChangeNameActivity.this.onAnimationEnd();
					firstNameField.requestFocus();
					Utilities.showKeyboard(firstNameField);
				}
			});

			return anim;
		} else {
			return super.onCreateAnimation(transit, enter, nextAnim);
		}
	}

	private void saveName() {
		if (!showConnectStatus())
			return;
		String text = firstNameField.getText().toString();
		if (chat_type == 1) {
			TL_CompanyInfo company = new TL_CompanyInfo();
			company.act = 2;
			company.name = text;
			company.companyid = chat_id;
			company.createrid = UserConfig.clientUserId;
			MessagesController.getInstance().ControlCompany(company);
		} else {
			MessagesController.getInstance().changeChatTitle(chat_id, text);
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

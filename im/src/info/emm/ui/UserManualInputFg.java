package info.emm.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.TLRPC;
import info.emm.messenger.TLRPC.TL_CompanyInfo;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.StringUtil;
import info.emm.utils.UiUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

public class UserManualInputFg extends BaseFragment implements
		NotificationCenter.NotificationCenterDelegate {

	private EditText name_te, phone_te, phone_tv;
	private TextView addTextView;

	/**
	 * ������˾ʱ �޸���ϵ����Ϣ
	 */
	private int userId = -1;
	private TLRPC.User user;
	private int companyId = -1;
	
	/**
	 * �ֶ��������
	 * ������˾ʱ �޸���ϵ��
	 * �����Ĺ�˾�� �޸���ϵ��
	 */
	public static enum FaceType{
		ManualInput,ModifyUser,ModifyFromCompany
	}
	FaceType currentFaceType = FaceType.ManualInput;
	public UserManualInputFg() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		NotificationCenter.getInstance().addObserver(this,
				MessagesController.company_username_changed);
		NotificationCenter.getInstance().addObserver(this,
				MessagesController.company_create_failed);

		currentFaceType = FaceType.values()[getArguments().getInt("faceType")];
		userId = getArguments().getInt("user_id", -1);
		companyId = getArguments().getInt("company_id",-1);
		if (currentFaceType == FaceType.ModifyFromCompany) {
			user = MessagesController.getInstance().users.get(userId);
		} else {
			user = MessagesController.getInstance().selectedUsers.get(userId);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (fragmentView == null) {
			fragmentView = inflater.inflate(R.layout.add_manual_input_layout,
					container, false);
			name_te = (EditText) fragmentView.findViewById(R.id.name_te);
			phone_tv = (EditText) fragmentView.findViewById(R.id.phone_tv);
			phone_tv.setFocusableInTouchMode(false);
			phone_te = (EditText) fragmentView.findViewById(R.id.phone_te);
			addTextView = (TextView) fragmentView.findViewById(R.id.tv_add);
//			phone_tv.setText(UserConfig.coutryCode);
			phone_tv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(parentActivity,
							CountrySelectActivity.class);
					startActivityForResult(intent, 1);
				}
			});
			if (user != null) { //�޸���ϵ��
				
				
				
				if (currentFaceType == FaceType.ModifyUser) {
					String nameString = user.last_name + user.first_name;//MessagesController.getInstance().getCompanyUserName(companyId,userId);
					name_te.setText(nameString);
					name_te.setSelection(name_te.getText().length());
					 phone_te.setText(user.phoneNoCode);
					 phone_tv.setText(user.countyCode);
				}else if (currentFaceType == FaceType.ModifyFromCompany) {
					String nameString = MessagesController.getInstance().getCompanyUserName(companyId,userId);
					name_te.setText(nameString);
					name_te.setSelection(name_te.getText().length());
					fragmentView.findViewById(R.id.linlay_coutrycode)
					.setVisibility(View.GONE);
					phone_te.setHint(user.phone);
//					phone_te.setText(user.phone);
					phone_te.setEnabled(false);
					addTextView.setVisibility(View.VISIBLE);
					addTextView.setText(R.string.PHONE);
				}
			}

		} else {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}

		return fragmentView;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			phone_tv.setText(data.getStringExtra("countryCode"));

		}
		super.onActivityResult(requestCode, resultCode, data);
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
		((CreateNewGroupActivity) parentActivity).showActionBar();
		((CreateNewGroupActivity) parentActivity).updateActionBar();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		NotificationCenter.getInstance().removeObserver(this,
				MessagesController.company_username_changed);
		NotificationCenter.getInstance().removeObserver(this,
				MessagesController.company_create_failed);

	}

	@Override
	public void applySelfActionBar() {
		if (parentActivity == null) {
			return;
		}
		  ActionBar actionBar =  super.applySelfActionBar(true);
		  actionBar.setTitle((currentFaceType == FaceType.ManualInput) ? R.string.manual_input : R.string.EditContact
						);

		TextView title = (TextView) parentActivity
				.findViewById(R.id.action_bar_title);
		if (title == null) {
			final int subtitleId = parentActivity.getResources().getIdentifier(
					"action_bar_title", "id", "android");
			title = (TextView) parentActivity.findViewById(subtitleId);
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
			((CreateNewGroupActivity) parentActivity).finish();
			break;
		}
		return true;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		inflater.inflate(R.menu.group_profile_menu, menu);
		SupportMenuItem doneitem = (SupportMenuItem) menu
				.findItem(R.id.block_user);
		TextView doneView = (TextView) doneitem.getActionView().findViewById(
				R.id.done_button);
		doneView.setText(R.string.Done);
		doneView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String nameString = name_te.getText().toString();
				String phoneString = phone_te.getText().toString();
				String phoneCode = phone_tv.getText().toString();
				String error = null;
				if (StringUtil.isEmpty(nameString)) {
					error = StringUtil.getStringFromRes(R.string.name_isnot_empty);
				}else if ((currentFaceType != FaceType.ModifyFromCompany)&&!StringUtil.isMobileNO(phoneString)) {
					error = StringUtil.getStringFromRes(R.string.wrong_phone_num_notice);
				}
				if (!StringUtil.isEmpty(error)) {
					UiUtil.showToast(parentActivity, error);
					return;
				}
				if (currentFaceType == FaceType.ModifyUser) {
					user.first_name = nameString;
					user.countyCode = phoneCode;
					user.phoneNoCode = phoneString;
					user.phone = "+" + phoneCode + phoneString;// ����Ǵ��������
				} else if (currentFaceType == FaceType.ModifyFromCompany) {
					TL_CompanyInfo company = new TL_CompanyInfo();
					company.companyid = companyId;
					TLRPC.User mUser = new TLRPC.User();
					mUser.id = userId;
					mUser.first_name = nameString;
					mUser.last_name = ""; // ����
					mUser.phone = user.phone;

					company.act = 8;
					company.users.add(mUser);
					MessagesController.getInstance().ControlCompany(company);
					return;
				} else {
					ArrayList<Integer> result = new ArrayList<Integer>();
					TLRPC.User user = new TLRPC.TL_userContact();
					user.id = (int) ConnectionsManager.getInstance()
							.generateContactId();
					user.first_name = nameString;
					user.countyCode = phoneCode;
					user.phoneNoCode = phoneString;
					user.phone = "+" + phoneCode + phoneString;
					if (MessagesController.getInstance().findIgnoreUser(
							user.phone)) {
						Utilities.showToast(parentActivity, StringUtil
								.getStringFromRes(R.string.add_newmem_ishave));
						return;
					}
					MessagesController.getInstance().selectedUsers.put(user.id,
							user);
					result.add(user.id);
					NotificationCenter.getInstance().addToMemCache(2, result);
				}

				((CreateNewGroupActivity) parentActivity)
						.setResult(Activity.RESULT_OK);
				((CreateNewGroupActivity) parentActivity).finish();
			}
		});
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void didReceivedNotification(int id, Object... args) {
		if (id == MessagesController.company_username_changed) {
			user.first_name = name_te.getText().toString();
			user.last_name = ""; // ����
			user.phone = "+" + phone_tv.getText().toString() + phone_te.getText().toString(); // ����Ǵ��������
			((CreateNewGroupActivity) parentActivity)
					.setResult(Activity.RESULT_OK);
			((CreateNewGroupActivity) parentActivity).finish();
		} else if (id == MessagesController.company_create_failed) {
			String msg = LocaleController.getString("",
					R.string.modify_company_username_faild);
			Utilities.showToast(parentActivity, msg);
		}
	}
}

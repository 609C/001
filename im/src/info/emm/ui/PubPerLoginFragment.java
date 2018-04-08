package info.emm.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.MessagesStorage;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.messenger.RPCRequest.RPCRequestDelegate;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.ToolUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

/**
 * �����û�
 * @author Administrator
 *
 */
public class PubPerLoginFragment extends BaseFragment implements OnClickListener{

	private EditText edt_phone;
	private EditText edt_password;
	private TextView txt_forgot;
	private TextView txt_regiest;
	private Button txt_login;
	private String strAccount;
	private String strPassword;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(fragmentView==null){
			fragmentView = inflater.inflate(R.layout.pub_per_fragement, null);
			edt_phone = (EditText) fragmentView.findViewById(R.id.pub_per_phone);
			edt_password = (EditText) fragmentView.findViewById(R.id.pub_per_password);
			txt_forgot = (TextView) fragmentView.findViewById(R.id.pub_per_forgot);
			txt_regiest = (TextView) fragmentView.findViewById(R.id.pub_per_regiest);
			txt_login = (Button) fragmentView.findViewById(R.id.txt_login);
			// TODO Auto-generated method stub
			edt_phone.setText(UserConfig.account);
			txt_forgot.setOnClickListener(this);
			txt_regiest.setOnClickListener(this);
			txt_login.setOnClickListener(this);
		}else{
			ViewGroup parent = (ViewGroup)fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		UserConfig.isPersonalVersion = true;
		UserConfig.saveConfig(false);
		return fragmentView;
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
		//				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		//				imm.hideSoftInputFromWindow(
		//						view.getWindowToken(), 0);
		//			}
		//		});
		super.onCreateOptionsMenu(menu, inflater);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}




	public void needShowProgress() 
	{
		if(getActivity()!=null)
			Utilities.ShowProgressDialog(this.getActivity(), getResources().getString(R.string.Loading));
	}


	public void needHideProgress() 
	{
		if(getActivity()!=null)
			Utilities.HideProgressDialog(this.getActivity());
	}

	public void onNextAction() {


		needShowProgress();

		ConnectionsManager.getInstance().CheckLogin("", strAccount, strPassword,new RPCRequestDelegate() {

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
								//�ʺ�δ����

								ShowAlertDialog(getActivity(),ApplicationLoader.applicationContext.getString(R.string.AccountNoActication));

							}
							else if( result == 2)
							{
								//�ʺ��Ѿ�����

								ShowAlertDialog(getActivity(),ApplicationLoader.applicationContext.getString(R.string.AccountFreeze));

							}
							else if( result == 3)
							{
								//�����û���Ϣ�豸ʧ��

								ShowAlertDialog(getActivity(),ApplicationLoader.applicationContext.getString(R.string.DeviceUpdateFaild));

							}
							else if( result == 5)
							{
								//�������

								ShowAlertDialog(getActivity(),ApplicationLoader.applicationContext.getString(R.string.PasswordError));

							}
							else if( result == 6)
							{
								//�ʺŴ���

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
						//��¼�ɹ�
						//�����ݿ���˼��ʲô��xueqiang ask
						final TLRPC.TL_auth_authorization res = (TLRPC.TL_auth_authorization)response;       				 
						UserConfig.clearConfig();
						MessagesStorage.getInstance().cleanUp();
						//MessagesController.getInstance().cleanUp();
						UserConfig.currentUser = res.user;
						UserConfig.clientActivated = true;
						UserConfig.clientUserId = res.user.id;
						UserConfig.account = strAccount;
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
						//todo.. test qxm
						if(ApplicationLoader.infragmentsStack.size()>0)
						{
							ApplicationLoader.infragmentsStack.remove(ApplicationLoader.infragmentsStack.size()-1);
							needFinishActivity();
						}
					}
				});
			}
		});


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
	public void needFinishActivity() {
		Intent intent2 = new Intent(getActivity(), LaunchActivity.class);
		startActivity(intent2);
		//xueqiang change begin
		Utilities.HideProgressDialog(getActivity());
		//xueqiang change end
		getActivity().finish();
	}

	@Override                
	public void onClick(View arg0) {
		strAccount = edt_phone.getText().toString().trim();
		strPassword = edt_password.getText().toString().trim();
		Intent intent2 = new Intent(getActivity(),LoginActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("phoneNum", strAccount);
		int i = arg0.getId();
		if (i == R.id.pub_per_forgot) {
			bundle.putBoolean("phoneregister",
					ToolUtil.isCNLanguage(getActivity()));
			bundle.putBoolean("instate", true);
			intent2.putExtras(bundle);
			startActivity(intent2);

		} else if (i == R.id.pub_per_regiest) {
			bundle.putBoolean("phoneregister",
					ToolUtil.isCNLanguage(getActivity()));
			bundle.putBoolean("instate", false);
			intent2.putExtras(bundle);
			startActivity(intent2);

		} else if (i == R.id.txt_login) {
			onNextAction();
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(
					arg0.getWindowToken(), 0);

		}

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
		Utilities.showKeyboard(edt_phone);
		UserConfig.isPersonalVersion = true;
		UserConfig.saveConfig(false);
		((IntroActivity) parentActivity).showActionBar();
		((IntroActivity) parentActivity).updateActionBar();

	}
}

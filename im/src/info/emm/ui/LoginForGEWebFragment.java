package info.emm.ui;

import com.utils.Utitlties;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.FileLog;
import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.MessagesStorage;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.messenger.RPCRequest.RPCRequestDelegate;
import info.emm.messenger.TLObject;
import info.emm.messenger.TLRPC.TL_error;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

public class LoginForGEWebFragment extends BaseFragment {
	private WebView webLogin;
	public int num = 0;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (fragmentView == null) {
			fragmentView = inflater.inflate(R.layout.login_ge_web_fragment,
					null);
			webLogin = (WebView) fragmentView.findViewById(R.id.web_login);
			webLogin.getSettings().setJavaScriptEnabled(true);
			webLogin.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
			webLogin.clearHistory();
			webLogin.clearCache(true);
			CookieSyncManager.createInstance(getActivity());   
			CookieSyncManager.getInstance().startSync();   
			CookieManager.getInstance().removeSessionCookie();
			// ������Ҫ��ʾ����ҳ
			webLogin.setWebViewClient(new WebViewClient() {

				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					// TODO Auto-generated method stub
					// needFinishActivity();
					// Toast.makeText(getActivity(), url,
					// Toast.LENGTH_LONG).show();
					Log.e("emm", "url=" + url);
					if(url.contains("gesso.php?")){
						String temp = url.substring(url.indexOf("code="));
						String strtoken = temp.substring(temp.indexOf("=")+1);
						if(num == 1){
							return true;
						}
						num = 1;
						ConnectionsManager.getInstance().CheckLogin(strtoken,
								new RPCRequestDelegate() {

							@Override
							public void run(final TLObject response,
									final TL_error error) {
								// TODO Auto-generated method stub
								if (error != null) {

									Utilities.RunOnUIThread(new Runnable() {
										@Override
										public void run() {
											int result = error.code;
											if (result == 1) {
												// �ʺ�δ����

												ShowAlertDialog(
														getActivity(),
														ApplicationLoader.applicationContext
														.getString(R.string.AccountNoActication));

											} else if (result == 2) {
												// �ʺ��Ѿ�����

												ShowAlertDialog(
														getActivity(),
														ApplicationLoader.applicationContext
														.getString(R.string.AccountFreeze));

											} else if (result == 3) {
												// �����û���Ϣ�豸ʧ��

												ShowAlertDialog(
														getActivity(),
														ApplicationLoader.applicationContext
														.getString(R.string.DeviceUpdateFaild));

											} else if (result == 5) {
												// �������

												ShowAlertDialog(
														getActivity(),
														ApplicationLoader.applicationContext
														.getString(R.string.PasswordError));

											} else if (result == 6) {
												// �ʺŴ���

												ShowAlertDialog(
														getActivity(),
														ApplicationLoader.applicationContext
														.getString(R.string.AccountError));

											} else if (result == -2) {

												ShowAlertDialog(
														getActivity(),
														ApplicationLoader.applicationContext
														.getString(R.string.WaitingForNetwork));

											}
										}
									});
									return;
								}

								Utilities.RunOnUIThread(new Runnable() {
									@Override
									public void run() {
										// ��¼�ɹ�
										// �����ݿ���˼��ʲô��xueqiang ask
										final TLRPC.TL_auth_authorization res = (TLRPC.TL_auth_authorization) response;
										UserConfig.clearConfig();
										MessagesStorage.getInstance()
										.cleanUp();
										// MessagesController.getInstance().cleanUp();
										UserConfig.currentUser = res.user;
										UserConfig.clientActivated = true;
										UserConfig.clientUserId = res.user.id;
										UserConfig.account = res.user.phone;
										UserConfig.saveConfig(true);
										MessagesStorage.getInstance()
										.openDatabase();
										MessagesController.getInstance().users
										.put(res.user.id, res.user);

										FileLog.d(
												"emm",
												"check login result:"
														+ UserConfig.clientUserId
														+ " sid:"
														+ UserConfig.currentUser.sessionid);
										//											try {
										//					                        	AccountManager accountManager = AccountManager.get(ApplicationLoader.applicationContext);
										//					                            Account myAccount  = new Account(UserConfig.account, "info.emm.weiyicloud.account");
										//					                            accountManager.addAccountExplicitly(myAccount, UserConfig., null);
										//					                        } catch (Exception e) {
										//					                        	e.printStackTrace();
										//					                            FileLog.e("emm", e);
										//					                        }
										// todo.. test qxm
										if (ApplicationLoader.infragmentsStack
												.size() > 0) {
											ApplicationLoader.infragmentsStack
											.remove(ApplicationLoader.infragmentsStack
													.size() - 1);
											needFinishActivity();
										}
									}
								});
							}

						});

					}
					Log.e("emm", "shouldOverrideUrlLoading");
					return super.shouldOverrideUrlLoading(view, url);
				}

				// @Override
				// public void onPageFinished(WebView view, String url) {
				// CookieSyncManager.createInstance(getActivity());
				// CookieSyncManager.getInstance().startSync();
				// CookieManager.getInstance().removeSessionCookie();
				// super.onPageFinished(view, url);
				// }
				@Override
				public void onReceivedError(WebView view, int errorCode,
						String description, String failingUrl) {
					super.onReceivedError(view, errorCode, description, failingUrl);
					Utitlties.HideProgressDialog(getActivity());
				}
				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					Utitlties.HideProgressDialog(getActivity());
				}
				@Override
				public void onPageStarted(WebView view, String url,
						Bitmap favicon) {
					// TODO Auto-generated method stub
					super.onPageStarted(view, url, favicon);
//					Utitlties.ShowProgressDialog(getActivity(), getResources().getString(R.string.Loading));
				}

			});

			webLogin.loadUrl("https://fssfed.stage.ge.com/fss/as/authorization.oauth2?response_type=code&client_id=GEHC_CyberFE1_OIDC&redirect_uri=https%3a%2f%2fgesso.weiyicloud.com%2fgesso.php&scope=openid+profile+GEWorker_Allow_Policy_UAT");
//			webLogin.loadUrl("https://fssfed.stage.ge.com/fss/as/authorization.oauth2?response_type=code&client_id=GEHC_CyberFE1_OIDC&redirect_uri=weiyiim%3a%2f%2fgesso.weiyicloud.com%2fgesso.php&scope=openid+profile");

		} else {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		return fragmentView;
	}


	public void needFinishActivity() {
		if(getActivity()==null){
			return;
		}
		Intent intent2 = new Intent(getActivity(), LaunchActivity.class);
		startActivity(intent2);
		// xueqiang change begin
		Utilities.HideProgressDialog(getActivity());
		// xueqiang change end
		getActivity().finish();
	}

	public void ShowAlertDialog(final Activity activity, final String message) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!getActivity().isFinishing()) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							activity);
					builder.setTitle(LocaleController.getString("AppName",
							R.string.app_name));
					builder.setMessage(message);
					builder.setPositiveButton(
							LocaleController.getString("OK", R.string.OK), null);
					builder.show().setCanceledOnTouchOutside(true);
				}
			}
		});
	}
		@Override
		public void onStart() {
			super.onStart();
			Log.i("emm", "onStart.......");
			Utitlties.ShowProgressDialog(this.getActivity(), getResources().getString(R.string.Loading));
		}
	@Override
	public void onResume() {
		super.onResume();
		Log.i("emm", "onResume.......");
	}
}

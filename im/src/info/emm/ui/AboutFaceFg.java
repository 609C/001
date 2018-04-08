package info.emm.ui;

import net.hockeyapp.android.Strings;
import net.hockeyapp.android.UpdateManager;
import net.hockeyapp.android.UpdateManagerListener;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;
import info.emm.messenger.BuildVars;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.ConstantValues;
import info.emm.utils.StringUtil;
import info.emm.utils.ToolUtil;
import info.emm.utils.UiUtil;
import info.emm.yuanchengcloudb.R;

/**
 * @ClassName: AboutFaceFg
 *
 * @Description: ���ڽ���
 *
 */
public class AboutFaceFg extends BaseFragment implements OnClickListener{

	private TextView tvVision;
	private TextView tvWeb;
	private TextView tvUpdate;
	public AboutFaceFg() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
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
		((LaunchActivity) parentActivity).showActionBar();
		((LaunchActivity) parentActivity).updateActionBar();
	}

	private void initView() {
		if (fragmentView == null) {
			return;
		}
		tvVision = (TextView)fragmentView.findViewById(R.id.tv_vision);
		tvWeb = (TextView)fragmentView.findViewById(R.id.tv_web);
		tvUpdate = (TextView)fragmentView.findViewById(R.id.tv_update);
	}
	private void initOperate() {
		String textString = StringUtil.getStringFromRes(R.string.AppName)+
				ToolUtil.getAppVersionName(parentActivity);
		if(!StringUtil.isEmpty(textString))
			tvVision.setText(textString);

		tvWeb.setOnClickListener(this);
		tvUpdate.setOnClickListener(this);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (fragmentView == null) {
			fragmentView = inflater.inflate(R.layout.setting_about_layout, container, false);
		}else {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}

		initView();
		initOperate();
		return fragmentView;
	}

	@Override
	public boolean onFragmentCreate() {
		super.onFragmentCreate();
		return true;
	}

	@Override
	public void applySelfActionBar() {
		if (parentActivity == null) {
			return;
		}
		super.applySelfActionBar(true);
		ActionBar actionBar = parentActivity.getSupportActionBar();
		actionBar.setTitle(R.string.label_sub_about);

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
			finishFragment();
			break;
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.tv_web) {
			WebFaceFg webFaceFg = new WebFaceFg();
			Bundle bundle = new Bundle();
			bundle.putInt("titleName", R.string.official_website);
			bundle.putString("url", ConstantValues.OFFICAL_URL);
			webFaceFg.setArguments(bundle);
			((LaunchActivity) parentActivity).presentFragment(
					webFaceFg, "",
					false);

		} else if (id == R.id.tv_update) {
//			checkForUpdates();


		} else {
		}
	}
	private void checkForUpdates() {
		//	      if (BuildVars.DEBUG_VERSION) 
		{
			UpdateManagerListener listener = new UpdateManagerListener() {
				public String getStringForResource(int resourceID) {
					switch (resourceID) {
					case Strings.UPDATE_MANDATORY_TOAST_ID:
						return getResources().getString(R.string.update_mandatory_toast);
					case Strings.UPDATE_DIALOG_TITLE_ID:
						return getResources().getString(R.string.update_dialog_title);
					case Strings.UPDATE_DIALOG_MESSAGE_ID:
						return getResources().getString(R.string.update_dialog_message);
					case Strings.UPDATE_DIALOG_NEGATIVE_BUTTON_ID:
						return getResources().getString(R.string.update_dialog_negative_button);
					case Strings.UPDATE_DIALOG_POSITIVE_BUTTON_ID:
						return getResources().getString(R.string.update_dialog_positive_button);
					case Strings.DOWNLOAD_FAILED_DIALOG_TITLE_ID:
						return getResources().getString(R.string.download_failed_dialog_title);
					case Strings.DOWNLOAD_FAILED_DIALOG_MESSAGE_ID:
						return getResources().getString(R.string.download_failed_dialog_message);
					case Strings.DOWNLOAD_FAILED_DIALOG_NEGATIVE_BUTTON_ID:
						return getResources().getString(R.string.download_failed_dialog_negative_button);
					case Strings.DOWNLOAD_FAILED_DIALOG_POSITIVE_BUTTON_ID:
						return getResources().getString(R.string.download_failed_dialog_positive_button);
					case Strings.UPDATE_VIEW_UPDATE_BUTTON_ID:
						return getResources().getString(R.string.update_view_update_button);
					default:
						return null;
					}
				}

				@Override
				public void onNoUpdateAvailable() {
					UiUtil.showToast(parentActivity, 0,R.string.update_noupdate, 0.0f, 0.0f);
				}
			};

			//UpdateManager.register(this, "http://192.168.0.99:8080/update/php/public/", BuildVars.HOCKEY_APP_HASH, listener);
			if(ApplicationLoader.edition==1){
				UpdateManager.register(parentActivity, "http://u.weiyicloud.com/", BuildVars.HOCKEY_APP_HASH_GE, listener);
			}else{				
				UpdateManager.register(parentActivity, "http://u.weiyicloud.com/", BuildVars.HOCKEY_APP_HASH, listener);
			}
		}
	}
}

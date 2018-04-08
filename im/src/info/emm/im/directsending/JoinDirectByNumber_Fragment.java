package info.emm.im.directsending;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.utils.WeiyiMeeting;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import info.emm.LocalData.Config;
import info.emm.messenger.UserConfig;
import info.emm.ui.LaunchActivity;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

public class JoinDirectByNumber_Fragment extends BaseFragment implements
OnClickListener {
	private AutoCompleteTextView mEMeetingID;
	private EditText mEUsername;
	private Button mBtnJoinMeeting;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container,  Bundle savedInstanceState) {
		if (fragmentView == null) {
			fragmentView = inflater.inflate(
					R.layout.join_direct_by_number_fragment, null);

			mBtnJoinMeeting = (Button) fragmentView
					.findViewById(info.emm.yuanchengcloudb.R.id.button_join_dir);
			mEMeetingID = (AutoCompleteTextView) fragmentView
					.findViewById(R.id.edit_dir_number);
			mEUsername = (EditText) fragmentView
					.findViewById(R.id.editText_dir_username);
			final String reg = "^([a-z]|[A-Z]|[0-9]|[\u2E80-\u9FFF]){3,}|@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?|[wap.]{4}|[www.]{4}|[blog.]{5}|[bbs.]{4}|[.com]{4}|[.cn]{3}|[.net]{4}|[.org]{4}|[http://]{7}|[ftp://]{6}$";

			final Pattern pattern = Pattern.compile(reg);

			getHistory();

			mBtnJoinMeeting.setOnClickListener(this);

		} else {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}

		mEUsername.setText(UserConfig.getNickName());
		return fragmentView;
	}

	public void getHistory() {
		SharedPreferences sp = WeiyiMeeting.getApplicationContext()
				.getSharedPreferences("live_id", 0);

		String longhistory = sp.getString("history", "");

		final String[] histories = longhistory.split(",");

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				this.getActivity(),
				android.R.layout.simple_dropdown_item_1line, histories);

		if (histories.length > 10) {

			String[] newHistories = new String[10];

			System.arraycopy(histories, 0, newHistories, 0, 10);

			String strNewSave = "";
			for (int i = 0; i < 10; i++) {
				strNewSave += newHistories[i] + ",";
				sp.edit().putString("history", strNewSave).commit();
			}

			adapter = new ArrayAdapter<String>(this.getActivity(),
					android.R.layout.simple_dropdown_item_1line, newHistories);

		}

		mEMeetingID.setAdapter(adapter);

		mEMeetingID.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				AutoCompleteTextView view = (AutoCompleteTextView) arg0;
				if (!view.isPopupShowing()) {
					if (!histories[0].isEmpty()) {
						System.out.println("length=" + histories.length);
						view.showDropDown();
					}
				}
			}
		});

		mEMeetingID.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {

				AutoCompleteTextView view = (AutoCompleteTextView) v;
				if (hasFocus) {
					view.showDropDown();
				}
			}
		});
	}

	@SuppressWarnings("static-access")
	@Override
	public void onClick(View v) {
		int nId = v.getId();
		if (nId == R.id.button_join_dir) {
			final String strID = mEMeetingID.getText().toString();
			String strName = mEUsername.getText().toString();

			String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~��@#��%����&*��������+|{}������������������������]";
			Pattern p = Pattern.compile(regEx);
			Pattern emoji = Pattern
					.compile(
							"[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
							Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
			Matcher mt = emoji.matcher(strName);

			Matcher m = p.matcher(strName);
			if (strID.isEmpty()) {
				Toast.makeText(
						this.getActivity().getApplicationContext(),
						this.getResources().getString(
								R.string.direct_id_is_empty),
								Toast.LENGTH_SHORT).show();
			} else if (strName.isEmpty()) {
				Toast.makeText(
						this.getActivity().getApplicationContext(),
						this.getResources().getString(
								R.string.user_name_is_empty),
								Toast.LENGTH_SHORT).show();
			} else if (m.find()) {
				Toast.makeText(getActivity(),
						getString(R.string.nick_sp_alert), Toast.LENGTH_LONG)
						.show();
			} else if (mt.find()) {
				Toast.makeText(getActivity(), getString(R.string.nick_alert),
						Toast.LENGTH_LONG).show();
			} else {
				String Url = "";
				if(UserConfig.isPublic){
					String ss = Config.publicWebHttp;
					int index = ss.indexOf(6);
					String publicHttp = ss.substring(7);
					Url = "weiyi://start?ip="+publicHttp+"&port="+80+"&meetingid="+strID+"&thirdID="+UserConfig.clientUserId+"&nickname="+strName;
				}else{
					if(UserConfig.privatePort == 80){
						String privateHttp = UserConfig.privateWebHttp;
						Url = "weiyi://start?ip="+privateHttp+"&port="+80+"&meetingid="+strID+"&thirdID="+UserConfig.clientUserId+"&nickname="+strName;
					}else{
						String privateHttp = UserConfig.privateWebHttp;
						int privatePort = UserConfig.privatePort;
						Url = "weiyi://start?ip="+privateHttp+"&port="+privatePort+"&meetingid="+strID+"&thirdID="+UserConfig.clientUserId+"&nickname="+strName;
					}
				}
//				DirectPlayBackWeb_Fragment web_play_back = new DirectPlayBackWeb_Fragment();
//				((LaunchActivity) getActivity()).presentFragment(web_play_back,
//						"", false);
				WeiyiMeeting.getInstance().setViewer(true);
				Url+="&usertype="+2;
//				Url = "weiyi://start?ip=192.168.0.5&port=80&meetingid=601132014&thirdID=100001&nickname=qq";
				WeiyiMeeting.getInstance().joinBroadcast(getActivity(), Url);
			}
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
	public void applySelfActionBar() {
		if (parentActivity == null) {
			return;
		}
		ActionBar actionBar = super.applySelfActionBar(true);

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
	public void onResume() {
		super.onResume();
		Utilities.showKeyboard(mEMeetingID);
		((LaunchActivity) parentActivity).showActionBar();
		((LaunchActivity) parentActivity).updateActionBar();
	}
}

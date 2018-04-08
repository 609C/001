package info.emm.ui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.SyncHttpClient;

import info.emm.LocalData.Config;
import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.LocaleController;
import info.emm.messenger.UserConfig;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.HttpUtil;
import info.emm.utils.StringUtil;
import info.emm.utils.UiUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;


public class FeedbackActivity extends BaseFragment {

	
	
	private static SyncHttpClient client = new SyncHttpClient();
	private EditText feedback_content;
	private int user_id;
	Map<String, Object> params;
	private static Context ctx;
	private ProgressDialog mProgress;
	public FeedbackActivity() {

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
		
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		user_id = UserConfig.clientUserId;
		if (fragmentView == null) {
			fragmentView = inflater.inflate(R.layout.setting_suggests_layout, container, false);
		    feedback_content = (EditText) fragmentView.findViewById(R.id.feedback_content);
		    feedback_content.setFocusable(true);
		    feedback_content.requestFocus();
		    onFocusChange(feedback_content.isFocused());
		    feedback_content.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {

					v.getParent().requestDisallowInterceptTouchEvent(true);
					return false;
				}
			});
		}else {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}

		initView();
		return fragmentView;
	}

	
	private void onFocusChange(boolean focused) {
		// TODO Auto-generated method stub
		final boolean isFocus = focused;  
	    (new Handler()).postDelayed(new Runnable() {  
	    public void run() {  
	    InputMethodManager imm = (InputMethodManager)  
	    		feedback_content.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);  
	    if(isFocus)  
	    {  
	    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);  
	    }  
	    else  
	    {  
	    imm.hideSoftInputFromWindow(feedback_content.getWindowToken(),0);  
	    }  
	    }  
	    }, 100);
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
		  ActionBar actionBar =  super.applySelfActionBar(true);
		actionBar.setTitle(R.string.label_sub_feedback);

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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		 inflater.inflate(R.menu.group_profile_menu, menu);
	        SupportMenuItem doneItem = (SupportMenuItem)menu.findItem(R.id.block_user);
	        TextView doneTextView = (TextView)doneItem.getActionView().findViewById(R.id.done_button);
	        doneTextView.setText(LocaleController.getString("AddMember", R.string.Send));
	        doneTextView.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
						try {
							
							publishcontent();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NameNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
	            }

				
	        });

	}
	private void publishcontent() throws IOException, JSONException, NameNotFoundException
	{
		
		Utilities.hideKeyboard(feedback_content);
		String content = feedback_content.getText().toString();
		if (StringUtil.isEmpty(content)) {			
			UiUtil.showToast(feedback_content.getContext(), R.string.input_content);
			return;
		}else{
			mProgress = ProgressDialog.show(getActivity(), null,
					getString(R.string.submit), true, true);
		// ��ȡpackagemanager��ʵ��
        PackageManager packageManager = (ApplicationLoader.getContext()).getPackageManager();
        // getPackageName()���㵱ǰ��İ�����0�����ǻ�ȡ�汾��Ϣ
        PackageInfo packInfo = packageManager.getPackageInfo(ApplicationLoader.getContext().getPackageName(),0);
        String bundle_version =  "" + packInfo.versionCode;
        String bundle_identifier = packInfo.packageName;
        String bundle_short_version = packInfo.versionName;
        String os_version = android.os.Build.VERSION.RELEASE;
        int userid = UserConfig.clientUserId;
		String user_id = userid + "";
//		����豸������,Ħ��,���ǵȵ�
		String oem = Build.MANUFACTURER.toLowerCase(); 
		String model = android.os.Build.MODEL;
		
		// ��ǰʱ���
		SimpleDateFormat formatter = new SimpleDateFormat ("yyyy��MM��dd�� HH:mm:ss ");
		Date curDate = new Date(System.currentTimeMillis());//��ȡ��ǰʱ��
		String feedbackdate = formatter.format(curDate);

		pubfeedback(content,bundle_version,user_id,os_version,oem,model,feedbackdate,bundle_short_version,bundle_identifier);
		}
	}
	private void pubfeedback(String content,final String bundle_version,
			final String user_id,final String os_version,final String oem,final String model,
			final String feedbackdate,final String bundle_short_version,final String bundle_identifier) throws JSONException {
//		JSONObject jo = new JSONObject();
//		jo.put("text", content);
//		final RequestParams params = new RequestParams();
//		params.put("param", jo.toString());
//		new AsyncHttpClient().post(Config.webFun_USER_FEED_BACK, params, new AsyncHttpResponseHandler(){
//			@Override
//			public void onSuccess(String content) {
//				Log.e("TAG", content);
//			}
//			@Override
//			@Deprecated
//			public void onFailure(Throwable error, String content) {
//				// TODO Auto-generated method stub
//				super.onFailure(error, content);
//			}
//		});
		if (!ConnectionsManager.isNetworkOnline()) {
			if (mProgress != null)
				mProgress.dismiss();			
			UiUtil.showToast(feedback_content.getContext(), R.string.isNOTNET);
   			return;
		}
		new AsyncTask<String, Void, Integer>() {
			@Override
			protected Integer doInBackground(String... arg0) {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("text", arg0[0]));
				params.add(new BasicNameValuePair("bundle_version", bundle_version));
				params.add(new BasicNameValuePair("userid", user_id));
				params.add(new BasicNameValuePair("model", model));
				params.add(new BasicNameValuePair("os_version", os_version));
				params.add(new BasicNameValuePair("oem", oem));
				params.add(new BasicNameValuePair("feedbackdate", feedbackdate));
				params.add(new BasicNameValuePair("bundle_identifier", bundle_identifier));
				params.add(new BasicNameValuePair("bundle_short_version", bundle_short_version));
				Log.i("TAG",params.toString());
				JSONObject jo = HttpUtil.jsonPost(Config.webFun_USER_FEED_BACK, params);
				
				int result = -1;
				if(jo == null){
					result = -1;
				}else{
					result = jo.optInt("result");
					
				}
				return result;
				
			}
			protected void onPostExecute(Integer result) {
				if(result == -1){
					if (mProgress != null)
						mProgress.dismiss();
					UiUtil.showToast(feedback_content.getContext(), R.string.connect_unsuccess);					
				}else if(result == 0){
					if (mProgress != null)
						mProgress.dismiss();
					finishFragment();
					UiUtil.showToast(feedback_content.getContext(), R.string.connect_success);					
				}
			};
		}.execute(content);
		
	}
	} 
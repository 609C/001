package info.emm.ui;
import info.emm.LocalData.Config;
import info.emm.im.meeting.MainFragment_Meeting;
import info.emm.messenger.LocaleController;
import info.emm.messenger.UserConfig;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;

public class SetHttpFragment extends info.emm.ui.Views.BaseFragment   {
	private EditText edt_one;
	private EditText edt_two;
//	private TextView resethttp;
	private Button txt_done;
	MainFragment_Meeting main_fg = new MainFragment_Meeting();
	String webHttpServer;
	String port;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(fragmentView==null){
    		fragmentView = inflater.inflate(R.layout.personal_company_login, null);
    		edt_one = (EditText) fragmentView.findViewById(R.id.per_one);
    		edt_two = (EditText) fragmentView.findViewById(R.id.per_two);
    		txt_done = (Button) fragmentView.findViewById(R.id.txt_done);

    		edt_one.setHint(getActivity().getResources().getString(R.string.server_address));
    		edt_two.setHint(getActivity().getResources().getString(R.string.server_port));
    		edt_one.setInputType(InputType.TYPE_CLASS_TEXT);
    		edt_two.setInputType(InputType.TYPE_CLASS_NUMBER);
    		edt_one.setText(UserConfig.privateWebHttp);
    		txt_done.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					onNextAction();
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(
							arg0.getWindowToken(), 0);
					
				}
			});
    		if(UserConfig.privatePort!=0){    			
    			edt_two.setText(UserConfig.privatePort+"");
    		}else{
    			edt_two.setText(80+"");
    		}

    	}else{
    		ViewGroup parent = (ViewGroup)fragmentView.getParent();
    		if (parent != null) {
    			parent.removeView(fragmentView);
    		}
    	}

		
		return fragmentView;
	}

	public void showDialog(String s) {
		AlertDialog.Builder builder = new Builder(getActivity());
		builder.setTitle(R.string.remind);
		builder.setMessage(s).setPositiveButton(R.string.sure, null).show()
				.setCanceledOnTouchOutside(true);
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
	public void onStop() {
		edt_one.setText("");
		edt_two.setText("");
		super.onStop();
	}
	public void onNextAction() {
		
			webHttpServer = edt_one.getText().toString().trim();
			port = edt_two.getText().toString().trim();
			
			//ConnectionsManager.getInstance().setWebHttpServer(webHttpServer);	
			if(webHttpServer.isEmpty()){
				Toast.makeText(getActivity(), getString(R.string.enter_server_address), Toast.LENGTH_SHORT).show();
				return;
			}
//			if(!webHttpServer.startsWith("http://")){
//				webHttpServer = "http://"+webHttpServer;
//			}
			if(!port.isEmpty()){				
				Config.setWebHttp(webHttpServer+":"+port);
			}else{				
				Config.setWebHttp(webHttpServer);
			}
//			MeetingConnection.getInstance().setWebServerAddress(webHttpServer);
			UserConfig.isPublic = false;
			UserConfig.privateWebHttp = webHttpServer.trim();
			try {				
				UserConfig.privatePort = Integer.parseInt(port);
			} catch (Exception e) {
				UserConfig.privatePort = 80;
			}
			UserConfig.saveConfig(false);
			((IntroActivity)getActivity()).onBackPressed();
		
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
		edt_one.requestFocus();
		Utilities.showKeyboard(edt_one);
		((IntroActivity) parentActivity).showActionBar();
		((IntroActivity) parentActivity).updateActionBar();
		
	}

}

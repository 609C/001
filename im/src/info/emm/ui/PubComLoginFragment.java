package info.emm.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import info.emm.ui.Views.BaseFragment;
import info.emm.yuanchengcloudb.R;

public class PubComLoginFragment extends BaseFragment {

	private EditText edt_companyid;
	private EditText edt_phone;
	private EditText edt_password;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		if(fragmentView==null){
			fragmentView = inflater.inflate(R.layout.pub_com_fragment, null);
			edt_companyid = (EditText) fragmentView.findViewById(R.id.pub_com_companyid);
			edt_phone = (EditText) fragmentView.findViewById(R.id.pub_com_phone);
			edt_password = (EditText) fragmentView.findViewById(R.id.pub_com_password);
		}else{
			ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
		}
		
		return fragmentView;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	public void onNextAction() {
		//ִ����ɲ���
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
		
		((IntroActivity) parentActivity).showActionBar();
		((IntroActivity) parentActivity).updateActionBar();
		
	}
}

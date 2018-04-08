package info.emm.im.directsending;

import info.emm.messenger.LocaleController;
import info.emm.ui.LaunchActivity;
import info.emm.ui.Views.BaseFragment;
import info.emm.yuanchengcloudb.R;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * ֱ����ҳ�� 
 * @author qxm
 *
 */
public class DirectSending_Fragment extends BaseFragment implements OnClickListener{

	private RelativeLayout mMyDirectRl;
	private RelativeLayout mRangeDirectRl;
	private RelativeLayout mJoinDirectRl;
	private RelativeLayout mplayBackRl;
	private TextView mMyDirectListTv;
	private TextView mRangeDirectTv;
	private TextView mJoinDirectTv;
	private RelativeLayout mWatchLive;


	@Override  
	public void onActivityCreated(Bundle savedInstanceState)  
	{  
		super.onActivityCreated(savedInstanceState);  
	}  

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}
	@Override  
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)  
	{  
		if(fragmentView == null){
			fragmentView = inflater.inflate(R.layout.direct_sending__fragment, container,false); 
			mMyDirectRl = (RelativeLayout) fragmentView.findViewById(R.id.directsending_mydirect_list_rl);
			mRangeDirectRl =(RelativeLayout) fragmentView.findViewById(R.id.directsending_rangedirect_rl);
			mJoinDirectRl = (RelativeLayout) fragmentView.findViewById(R.id.directsending_joindirect_rl);
			mplayBackRl = (RelativeLayout) fragmentView.findViewById(R.id.directsending_playback_rl);
			mMyDirectListTv = (TextView) fragmentView.findViewById(R.id.directsending_mydirect_list_tv);
			mRangeDirectTv = (TextView) fragmentView.findViewById(R.id.directsending_rangedirect_tv);
			mJoinDirectTv = (TextView) fragmentView.findViewById(R.id.directsending_txt_join_direct);
			mWatchLive = (RelativeLayout) fragmentView.findViewById(R.id.directsending_watchdirect_rl);
			mMyDirectRl.setOnClickListener(this);
			mRangeDirectRl.setOnClickListener(this);
			mJoinDirectRl.setOnClickListener(this);
			mplayBackRl.setOnClickListener(this);
			mWatchLive.setOnClickListener(this);
		}else{
			ViewGroup parent = (ViewGroup)fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		return fragmentView;  
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
		refreshView();
	}
	@Override  
	public void applySelfActionBar(){

		ActionBar actionBar =  super.applySelfActionBar(true);
		if(actionBar == null)return;
		actionBar.setTitle(getString(R.string.app_name));
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false); 
		TextView title = (TextView)getActivity().findViewById(R.id.action_bar_title);
		if (title == null) {
			final int subtitleId = getActivity().getResources().getIdentifier("action_bar_title", "id", "android");
			title = (TextView)getActivity().findViewById(subtitleId);
		}
		if (title != null) {
			title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			title.setCompoundDrawablePadding(0);
		}
	}
	public void refreshView() 
	{
		if(mMyDirectListTv != null){
			mMyDirectListTv.setText(LocaleController.getString("mydirect_list", R.string.mydirect_list));
		}
		if(mRangeDirectTv != null){
			mRangeDirectTv.setText(LocaleController.getString("reservation_direct_sending", R.string.reservation_direct_sending));
		}
		if(mJoinDirectTv!=null){			
			mJoinDirectTv.setText(LocaleController.getString("joindirect", R.string.joindirect));
		}
	}

	@Override
	public void onClick(View arg0) {
		int nId = arg0.getId();
		if(nId == R.id.directsending_mydirect_list_rl){
			MyDirectList_Fragment m_fragMeetingList = new MyDirectList_Fragment();//�ҵ�ֱ��
			((LaunchActivity)getActivity()).presentFragment(m_fragMeetingList, "", false);

		}else if(nId ==  R.id.directsending_rangedirect_rl){
			RangeDirect_Fragment m_fragrm = new RangeDirect_Fragment();//ԤԼֱ��
			((LaunchActivity)getActivity()).presentFragment(m_fragrm, "", false);

		}else if(nId == R.id.directsending_joindirect_rl){//����ֱ��
			JoinDirect_Fragment m_fragjtm = new JoinDirect_Fragment();
			((LaunchActivity)getActivity()).presentFragment(m_fragjtm, "", false);
		}else if(nId == R.id.directsending_playback_rl){
			DirectPlayBack_Fragment back_Fragment = new DirectPlayBack_Fragment();
			((LaunchActivity)getActivity()).presentFragment(back_Fragment, "", false);
		}else if(nId == R.id.directsending_watchdirect_rl){
			JoinDirectByNumber_Fragment byNumber_Fragment = new JoinDirectByNumber_Fragment();
			((LaunchActivity)getActivity()).presentFragment(byNumber_Fragment, "", false);
		}
	}

}

package com.meeting.ui;



import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.utils.BaseFragment;
import com.utils.WeiyiMeetingClient;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.weiyicloud.whitepad.Face_Share_Fragment;
import com.weiyicloud.whitepad.Face_Share_Fragment.penClickListener;

import info.emm.meeting.Session;

@SuppressLint("ValidFragment")
public class PadMainFragment extends BaseFragment {
	public Face_camera_Fragment m_fragmentCamera;
	public Face_Share_Fragment m_fragment_share;
	public OnClickListener _onClickListener;
	private penClickListener m_penClickListener;
	private ImageView img_White_Full;
	private ImageView img_Video_Full;
	private FrameLayout fra_white;
	private FrameLayout fra_video;
	private boolean isWhiteFull = false;
	private boolean isVideoFull = false;
	private boolean _isvisable = true;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		UZResourcesIDFinder.init(getActivity().getApplicationContext());
		if (fragmentView == null) {
			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			fragmentView = inflater.inflate(UZResourcesIDFinder.getResLayoutID("pad_main_fragment"), null);
			m_fragmentCamera = new Face_camera_Fragment(_onClickListener,m_FragmentContainer);
			if(WeiyiMeetingClient.getInstance().isM_bShowWhite()){
				m_fragment_share = new Face_Share_Fragment(_onClickListener,Session.getInstance());
				m_fragment_share.setPenClickListener(m_penClickListener);
				m_fragment_share.setShareControl(Session.getInstance());
			}
			//������Ƶ
			//			if(MeetingSession.getInstance().isM_bShowVideo()){
			//				m_fragmentCamera = new Face_camera_Fragment(_onClickListener,m_FragmentContainer);
			//			}


			fra_white = (FrameLayout) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("fra_white"));
			fra_video = (FrameLayout) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("fra_video"));
			img_White_Full = (ImageView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("img_white_full"));
			img_Video_Full = (ImageView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("img_video_full"));
			img_White_Full.setAlpha(100);
			img_Video_Full.setAlpha(100);
			if(_isvisable){
				img_Video_Full.setVisibility(View.INVISIBLE);
				img_White_Full.setVisibility(View.INVISIBLE);
			}else{
				img_Video_Full.setVisibility(View.VISIBLE);
				img_White_Full.setVisibility(View.VISIBLE);
			}
			img_White_Full.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					//Toast.makeText(getActivity(), "dian jidao le", Toast.LENGTH_SHORT).show();
					LayoutParams lp = fra_white.getLayoutParams();
					if(isWhiteFull){
						lp.height = LayoutParams.WRAP_CONTENT;
						lp.width = LayoutParams.MATCH_PARENT;
						fra_white.setLayoutParams(lp);
						fra_video.setVisibility(View.VISIBLE);
					}else{
						lp.height = LayoutParams.MATCH_PARENT;
						lp.width = LayoutParams.MATCH_PARENT;
						fra_white.setLayoutParams(lp);
						fra_video.setVisibility(View.GONE);
					}
					isWhiteFull = !isWhiteFull;

				}
			});
			img_Video_Full.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					LayoutParams lp = fra_video.getLayoutParams();
					if(isVideoFull && WeiyiMeetingClient.getInstance().isM_bShowWhite()){
						lp.height = LayoutParams.WRAP_CONTENT;
						lp.width = LayoutParams.MATCH_PARENT;
						fra_video.setLayoutParams(lp);
						fra_white.setVisibility(View.VISIBLE);
					}else{
						lp.height = LayoutParams.MATCH_PARENT;
						lp.width = LayoutParams.MATCH_PARENT;
						fra_video.setLayoutParams(lp);
						fra_white.setVisibility(View.GONE);
					}
					isVideoFull = !isVideoFull;
					m_fragmentCamera.setIsFullScreen(isVideoFull);
				}
			});

			FragmentManager fm = getActivity().getSupportFragmentManager();
			FragmentTransaction fTrans = fm.beginTransaction();
			FragmentTransaction fTrans1 = fm.beginTransaction();
			if(m_fragment_share != null){
				fra_white.setVisibility(View.VISIBLE);
				fTrans.replace(UZResourcesIDFinder.getResIdID("white_container"), m_fragment_share);
				fTrans.commit();
			}else{
				fra_white.setVisibility(View.GONE);
			}
			fTrans1.replace(UZResourcesIDFinder.getResIdID("video_contaniner"), m_fragmentCamera);
			fTrans1.commit();

		} else {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}

		return fragmentView;
	}

	public PadMainFragment(OnClickListener onClickListener,penClickListener penClickListener) {
		this._onClickListener = onClickListener;
		m_penClickListener = penClickListener;
	}


	public void showCameraName(boolean bShow)
	{
		if(m_fragmentCamera != null)
			m_fragmentCamera.showCameraName(bShow);
	}
	public void setFullButtonVisable(boolean isvisable){
		if(getActivity()==null){
			return;
		}
		this._isvisable = isvisable;
		if(_isvisable){
			img_Video_Full.setVisibility(View.INVISIBLE);
			img_White_Full.setVisibility(View.INVISIBLE);
		}else{
			img_Video_Full.setVisibility(View.VISIBLE);
			img_White_Full.setVisibility(View.VISIBLE);
		}
	}
	public void showArrLayout()
	{
		if(m_fragment_share!=null)
			m_fragment_share.showArrLayout();
	}
	public void hideArrLayout()
	{
		if(m_fragment_share!=null)
			m_fragment_share.hideArrLayout();
	}
	@Override
	public void onResume() {

		super.onResume();		
	}
	public void showLayout()
	{
		if(m_fragmentCamera != null)
			m_fragmentCamera.showLayout();
	}
}

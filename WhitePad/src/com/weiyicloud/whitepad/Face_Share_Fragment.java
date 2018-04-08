package com.weiyicloud.whitepad;


import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.weiyicloud.whitepad.NotificationCenter.NotificationCenterDelegate;
import com.weiyicloud.whitepad.PaintPad.PaintPadClickListener;
import com.weiyicloud.whitepad.SharePadMgr.DataChangeListener;
import com.weiyicloud.whitepad.TL_PadAction.factoryType;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint({ "ValidFragment", "DefaultLocale" })
public class Face_Share_Fragment extends Fragment implements OnClickListener,TouchStopControl,DataChangeListener,NotificationCenterDelegate
{

	View fragmentView;
	LayoutInflater m_inflater;
	public PaintPad m_pp;
	public TextView m_tv_name;
	public ImageView img_left_arr;
	public ImageView img_right_arr;
	private LinearLayout shareSycnLl;
	boolean arrIsShow = false;
	Timer timer = null;
	public ArrayList<Integer> m_colorList = new ArrayList<Integer>();
	public ArrayList<Integer> m_penWidthList = new ArrayList<Integer>();

	AlertDialog dialog;


	private ImageView fsTotalBigIv;
	private ImageView fsTotalIv;
	private ImageView fsMarkerPenIv;
	private ImageView fsArrowLineIv;
	private ImageView fsLineIv;
	private ImageView fsRectangleIv;
	private ImageView fsEllipseIv;
	private ImageView fsTextIv;
	private ImageView fsColorIv;
	private ImageView fsClearIv;
	private LinearLayout fsbottomLl;
	private LinearLayout fsMarkerPenLL;
	private LinearLayout fsArrowLineLl;
	private LinearLayout fsLineLL;
	private LinearLayout fsRectangLl;
	private LinearLayout fsEllipseLl;
	private LinearLayout fsTextLl;
	private LinearLayout fsColorLl;
	private LinearLayout fsClearLL;
	//xiaoyang add
	private SharePadMgr m_sharepadmgr = SharePadMgr.getInstance();

	//xiaoyang add to test
	private CheckBox chk_pull;

	static int TAKE_SHARE_PHOTO = 21;
	static int GET_SHARE_FILE = 20;

	static String m_strShareFilePath = "";
	private ControlMode controlMode;
	private boolean isbroadcast = false;
	private boolean isshowArr = true;
	private boolean bZoom = true;
	private int padSizeMode = 0;



	public boolean isIsshowArr() {
		return isshowArr;
	}
	public void setIsshowArr(boolean isshowArr) {
		this.isshowArr = isshowArr;
	}
	public boolean isIsbroadcast() {
		return isbroadcast;
	}
	public void setIsbroadcast(boolean isbroadcast) {
		this.isbroadcast = isbroadcast;
	}
	public ControlMode getControlMode() {
		return controlMode;
	}
	public void setControlMode(ControlMode controlMode) {
		this.controlMode = controlMode;
	}
	boolean bDocFragmentFinish = false;
	//	private FaceShareControl syncInterface;
	//xiaoyang add
	private OnClickListener m_PageClickListener;


	private FaceShareControl shareControl;

	public void setShareControl(FaceShareControl shareControl) {
		this.shareControl = shareControl;
	}
	@SuppressLint("ValidFragment")
	public Face_Share_Fragment(OnClickListener ocl
			,FaceShareControl paintPadSyncInterface){
		m_PageClickListener = ocl;
		this.shareControl = paintPadSyncInterface;
	}

	public abstract static interface penClickListener{
		public abstract void OnPenClick(boolean bShowPoints);
	}

	penClickListener m_penClickListener=null;
	public void setPenClickListener(penClickListener listener)
	{
		m_penClickListener = listener;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		m_sharepadmgr.addOnDataChangeListener(this);
		UZResourcesIDFinder.init(getActivity().getApplicationContext());
		if (fragmentView == null) {
			m_inflater = inflater;
			fragmentView = inflater.inflate(UZResourcesIDFinder.getResLayoutID("face_share_fragment"), null);
			m_tv_name = (TextView)fragmentView.findViewById(UZResourcesIDFinder.getResIdID("PaintPad_name"));
			m_pp = (PaintPad)fragmentView.findViewById(UZResourcesIDFinder.getResIdID("PaintPad_1"));
			img_left_arr = (ImageView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("img_left_arr"));
			img_right_arr = (ImageView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("img_right_arr"));
			img_left_arr.setOnClickListener(this);
			img_right_arr.setOnClickListener(this);
			m_tv_name.setText(this.getString(UZResourcesIDFinder.getResStringID("share_pad")));
			shareSycnLl = (LinearLayout) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("share_sycn_ll"));
			shareSycnLl.setOnClickListener(this);



			fsTotalBigIv = (ImageView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID( "total_big_iv"));
			fsTotalBigIv.setOnClickListener(this);
			fsTotalIv = (ImageView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("total_iv"));
			fsTotalIv.setOnClickListener(this);
			fsMarkerPenIv = (ImageView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("markerpen_iv"));
			fsArrowLineIv = (ImageView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("arrowLine_iv"));
			fsLineIv = (ImageView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("line_iv"));
			fsRectangleIv = (ImageView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("rectangle_iv"));
			fsEllipseIv = (ImageView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("ellipse_iv"));
			fsTextIv = (ImageView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("text_iv"));
			fsColorIv = (ImageView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("color_iv"));
			fsClearIv = (ImageView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("clear_iv"));
			fsbottomLl = (LinearLayout) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("bottom_total_ll"));
			fsMarkerPenLL = (LinearLayout) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("markerpen_ll"));
			fsMarkerPenLL.setOnClickListener(this);
			fsArrowLineLl = (LinearLayout) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("arrowline_ll"));
			fsArrowLineLl.setOnClickListener(this);
			fsLineLL = (LinearLayout) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("line_ll"));
			fsLineLL.setOnClickListener(this);
			fsEllipseLl = (LinearLayout) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("ellipse_ll"));
			fsEllipseLl.setOnClickListener(this);
			fsRectangLl = (LinearLayout) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("rectangle_ll"));
			fsRectangLl.setOnClickListener(this);
			fsTextLl = (LinearLayout) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("text_ll"));
			fsTextLl.setOnClickListener(this);
			fsColorLl = (LinearLayout) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("color_ll"));
			fsColorLl.setOnClickListener(this);
			fsClearLL = (LinearLayout) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("clear_ll"));
			fsClearLL.setOnClickListener(this);

			//xiaoyang add to test
			chk_pull = (CheckBox) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("chk_select_pull"));
			chk_pull.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					m_pp.SetSelectMode(isChecked);
				}
			});
			m_pp.setPadMgr(m_sharepadmgr);
			m_pp.setSyncInterface(shareControl);
			m_pp.setSoundEffectsEnabled(false);
			m_pp.setClickable(true);
			m_pp.setOnPaintPadClickListener(new PaintPadClickListener(){
				@Override
				public void OnClick(View arg0) {
//					showArrLayout();
					if(m_PageClickListener!=null){
						Log.e("meeting", "single onclick***********");
						m_PageClickListener.onClick(arg0);
					}
				}

				@Override
				public void OnCleand() {
				}

				@Override
				public void OnReceived() {
				}

				@Override
				public void OnSelectColor(int penColor)
				{
					if(penColor == 0xffff0000){
						fsColorIv.setImageResource(UZResourcesIDFinder.getResDrawableID("round_color_red"));
					}else if(penColor == 0xff871f78){
						fsColorIv.setImageResource(UZResourcesIDFinder.getResDrawableID("round_color_gray"));
					}else if(penColor == 0xff00ff00){
						fsColorIv.setImageResource(UZResourcesIDFinder.getResDrawableID("round_color_green"));
					}else if(penColor == 0xffff9300){
						fsColorIv.setImageResource(UZResourcesIDFinder.getResDrawableID("round_color_orange"));
					}else if(penColor == 0xff2ab2f1){
						fsColorIv.setImageResource(UZResourcesIDFinder.getResDrawableID("round_color_blue"));
					}else if(penColor == 0xfffff100){
						fsColorIv.setImageResource(UZResourcesIDFinder.getResDrawableID("round_color_yellow"));
					}else if(penColor == 0xffff00bb){
						fsColorIv.setImageResource(UZResourcesIDFinder.getResDrawableID("round_color_peach"));
					}else if(penColor == 0xff000d90){
						fsColorIv.setImageResource(UZResourcesIDFinder.getResDrawableID("round_color_dark_blue"));
					}else{
						fsColorIv.setImageResource(UZResourcesIDFinder.getResDrawableID("round_color_blank"));
					}
				}
			});
			if(m_sharepadmgr.getControlMode()==ControlMode.fullcontrol){
				fsTotalBigIv.setVisibility(View.VISIBLE);
			}else{
				fsTotalBigIv.setVisibility(View.GONE);
			}
			if(isbroadcast){
				changeToolBarLayout();
			}
			m_pp.setZoomMode(bZoom);
			m_pp.setPadSize(padSizeMode);
		}else {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		return fragmentView;
	}
	@Override
	public void onStart() {
		NotificationCenter.getInstance().addObserver(this,FaceShareControl.UI_NOTIFY_USER_SYNC_VIDEO_MODE_CHANGE);
		NotificationCenter.getInstance().addObserver(this,FaceShareControl.UI_NOTIFY_MEETING_MODECHANGE);
		NotificationCenter.getInstance().addObserver(this,FaceShareControl.UI_NOTIFY_USER_CHAIRMAN_CHANGE);
		NotificationCenter.getInstance().addObserver(this,FaceShareControl.UI_NOTIFY_USER_HOSTSTATUS);
//		NotificationCenter.getInstance().addObserver(this,FaceShareControl.UI_NOTIFY_PLAY_MOVIE);
		NotificationCenter.getInstance().addObserver(this,FaceShareControl.DELMEETING_DOC);
		super.onStart();
	}
	@Override
	public void onStop() {
		NotificationCenter.getInstance().removeObserver(this,FaceShareControl.NET_CONNECT_BREAK);
		NotificationCenter.getInstance().removeObserver(this,FaceShareControl.NET_CONNECT_FAILED);
		NotificationCenter.getInstance().removeObserver(this,FaceShareControl.UI_NOTIFY_USER_SYNC_VIDEO_MODE_CHANGE);
		NotificationCenter.getInstance().removeObserver(this,FaceShareControl.UI_NOTIFY_MEETING_MODECHANGE);
		NotificationCenter.getInstance().removeObserver(this,FaceShareControl.UI_NOTIFY_USER_CHAIRMAN_CHANGE);
		NotificationCenter.getInstance().removeObserver(this,FaceShareControl.UI_NOTIFY_USER_HOSTSTATUS);
//		NotificationCenter.getInstance().removeObserver(this,FaceShareControl.UI_NOTIFY_PLAY_MOVIE);
		NotificationCenter.getInstance().removeObserver(this,FaceShareControl.DELMEETING_DOC);
		super.onStop();
	}
	public void showArrLayout(){

		Log.e("emm", "showArrLayout***********");
		arrIsShow = true;

		ShareDoc dc = m_sharepadmgr.getCurrentShareDoc();
		if(dc==null){
			return;
		}
		Log.e("emm", "dc.cuurentPage=="+dc.currentPage);
		img_left_arr.setVisibility(View.GONE);
		img_right_arr.setVisibility(View.GONE);
		if(!isshowArr){
			return;
		}
		if(dc.currentPage-1<=0){
			img_right_arr.setVisibility(View.VISIBLE);
			img_left_arr.setVisibility(View.GONE);
		}else if(dc.currentPage+1<=dc.pageCount&&dc.currentPage-1>=0){

			img_left_arr.setVisibility(View.VISIBLE);
			img_right_arr.setVisibility(View.VISIBLE);
		}else if(dc.currentPage+1>=dc.pageCount){
			img_left_arr.setVisibility(View.VISIBLE);
			img_right_arr.setVisibility(View.GONE);
		}
		if(dc.bIsBlank){

			img_left_arr.setVisibility(View.GONE);
			img_right_arr.setVisibility(View.GONE);
		}
		if(dc.pageCount==1){
			img_left_arr.setVisibility(View.GONE);
			img_right_arr.setVisibility(View.GONE);
		}
		if(timer!=null){
			timer.cancel();
			timer = null;
		}
		timer = new Timer(true);
		timer.schedule(new when(),3000);

	}
	public void hideArrLayout(){

		Log.e("emm", "hideArrLayout***********");
		arrIsShow = false;
		if(img_left_arr!=null)
			img_left_arr.setVisibility(View.GONE);
		if(img_right_arr!=null)
			img_right_arr.setVisibility(View.GONE);
		if(timer!=null){
			timer.cancel();
			timer = null;
		}
	}
	class when extends TimerTask{

		@Override
		public void run() {
			Utitlties.RunOnUIThread(new Runnable() {
				@Override
				public void run() {
					if (arrIsShow)
						hideArrLayout();
				}
			});
		}
	}
	@Override
	public void onDestroy(){
		super.onDestroy();

		m_sharepadmgr.removeOnDataChangeListener(this);


	}
	private int mClickNum = 0 ;
	@Override
	public void onClick(View v) {
		int nid =v.getId();
		ShareDoc dc = m_sharepadmgr.getCurrentShareDoc();

		if(nid == UZResourcesIDFinder.getResIdID("img_left_arr")){
			if(dc.currentPage-1>=0){
				if(m_sharepadmgr.getControlMode()==ControlMode.fullcontrol){//isChairAllow
					shareControl.sendShowPage(dc.docID, dc.currentPage-1);//sendShowPage
				}
				m_sharepadmgr.OnReceivePageChange(dc.docID, dc.currentPage-1);
			}
			showArrLayout();
		}else if(nid == UZResourcesIDFinder.getResIdID("img_right_arr")){
			if(dc.currentPage+1<=dc.pageCount){
				if(m_sharepadmgr.getControlMode()==ControlMode.fullcontrol){
					shareControl.sendShowPage(dc.docID, dc.currentPage+1);
				}
				m_sharepadmgr.OnReceivePageChange(dc.docID, dc.currentPage+1);
			}
			showArrLayout();

		}else if(nid ==  UZResourcesIDFinder.getResIdID("total_big_iv")){
			fsbottomLl.setVisibility(View.VISIBLE);
			fsTotalBigIv.setVisibility(View.GONE);
			if(m_pp!=null)
				m_pp.setZoomMode(false);
			if(m_penClickListener!=null)
				m_penClickListener.OnPenClick(false);
			mClickNum ++ ;
			if(mClickNum == 1){
				Toast.makeText(getActivity(), getString(UZResourcesIDFinder.getResStringID("reminder_message")), Toast.LENGTH_SHORT).show();
				fsMarkerPenIv.setImageResource(UZResourcesIDFinder.getResDrawableID("markpen_checked_img"));
				m_pp.SetAction(factoryType.ft_markerPen, true);
			}

		}else if(nid == UZResourcesIDFinder.getResIdID("total_iv")){
			fsbottomLl.setVisibility(View.GONE);
			fsTotalBigIv.setVisibility(View.VISIBLE);
			m_pp.setZoomMode(true);
			if(m_penClickListener!=null)
				m_penClickListener.OnPenClick(true);
		}else if(nid == UZResourcesIDFinder.getResIdID("arrowline_ll")){
			fsMarkerPenIv.setImageResource(UZResourcesIDFinder.getResDrawableID("markpen_img"));
			fsArrowLineIv.setImageResource(UZResourcesIDFinder.getResDrawableID("arrowline_checked_img"));
			fsLineIv.setImageResource(UZResourcesIDFinder.getResDrawableID("line_img"));
			fsRectangleIv.setImageResource(UZResourcesIDFinder.getResDrawableID("rectangle_img"));
			fsEllipseIv.setImageResource(UZResourcesIDFinder.getResDrawableID("ellipse_img"));
			fsTextIv.setImageResource(UZResourcesIDFinder.getResDrawableID("text_img"));
			fsClearIv.setImageResource(UZResourcesIDFinder.getResDrawableID("clear_img"));
			m_pp.SetAction(factoryType.ft_arrowLine, true);
		}else if(nid ==UZResourcesIDFinder.getResIdID("markerpen_ll")){
			fsMarkerPenIv.setImageResource(UZResourcesIDFinder.getResDrawableID("markpen_checked_img"));
			fsArrowLineIv.setImageResource(UZResourcesIDFinder.getResDrawableID("arrowline_img"));
			fsLineIv.setImageResource(UZResourcesIDFinder.getResDrawableID("line_img"));
			fsRectangleIv.setImageResource(UZResourcesIDFinder.getResDrawableID("rectangle_img"));
			fsEllipseIv.setImageResource(UZResourcesIDFinder.getResDrawableID("ellipse_img"));
			fsTextIv.setImageResource(UZResourcesIDFinder.getResDrawableID("text_img"));
			fsClearIv.setImageResource(UZResourcesIDFinder.getResDrawableID("clear_img"));
			m_pp.SetAction(factoryType.ft_markerPen, true);
		}else if(nid == UZResourcesIDFinder.getResIdID("line_ll")){
			fsMarkerPenIv.setImageResource(UZResourcesIDFinder.getResDrawableID("markpen_img"));
			fsArrowLineIv.setImageResource(UZResourcesIDFinder.getResDrawableID("arrowline_img"));
			fsLineIv.setImageResource(UZResourcesIDFinder.getResDrawableID("line_checked_img"));
			fsRectangleIv.setImageResource(UZResourcesIDFinder.getResDrawableID("rectangle_img"));
			fsEllipseIv.setImageResource(UZResourcesIDFinder.getResDrawableID("ellipse_img"));
			fsTextIv.setImageResource(UZResourcesIDFinder.getResDrawableID("text_img"));
			fsClearIv.setImageResource(UZResourcesIDFinder.getResDrawableID("clear_img"));
			m_pp.SetAction(factoryType.ft_line, true);
		}else if(nid == UZResourcesIDFinder.getResIdID("rectangle_ll")){
			fsMarkerPenIv.setImageResource(UZResourcesIDFinder.getResDrawableID("markpen_img"));
			fsArrowLineIv.setImageResource(UZResourcesIDFinder.getResDrawableID("arrowline_img"));
			fsLineIv.setImageResource(UZResourcesIDFinder.getResDrawableID("line_img"));
			fsRectangleIv.setImageResource(UZResourcesIDFinder.getResDrawableID("rectangle_checked_img"));
			fsEllipseIv.setImageResource(UZResourcesIDFinder.getResDrawableID("ellipse_img"));
			fsTextIv.setImageResource(UZResourcesIDFinder.getResDrawableID("text_img"));
			fsClearIv.setImageResource(UZResourcesIDFinder.getResDrawableID("clear_img"));
			m_pp.SetAction(factoryType.ft_Rectangle, true);
		}else if(nid == UZResourcesIDFinder.getResIdID("ellipse_ll")){
			fsMarkerPenIv.setImageResource(UZResourcesIDFinder.getResDrawableID("markpen_img"));
			fsArrowLineIv.setImageResource(UZResourcesIDFinder.getResDrawableID("arrowline_img"));
			fsLineIv.setImageResource(UZResourcesIDFinder.getResDrawableID("line_img"));
			fsRectangleIv.setImageResource(UZResourcesIDFinder.getResDrawableID("rectangle_img"));
			fsEllipseIv.setImageResource(UZResourcesIDFinder.getResDrawableID("ellipse_checked_img"));
			fsTextIv.setImageResource(UZResourcesIDFinder.getResDrawableID("text_img"));
			fsClearIv.setImageResource(UZResourcesIDFinder.getResDrawableID("clear_img"));
			m_pp.SetAction(factoryType.ft_Ellipse, true);
		}else if(nid == UZResourcesIDFinder.getResIdID("text_ll")){
			fsMarkerPenIv.setImageResource(UZResourcesIDFinder.getResDrawableID("markpen_img"));
			fsArrowLineIv.setImageResource(UZResourcesIDFinder.getResDrawableID("arrowline_img"));
			fsLineIv.setImageResource(UZResourcesIDFinder.getResDrawableID("line_img"));
			fsRectangleIv.setImageResource(UZResourcesIDFinder.getResDrawableID("rectangle_img"));
			fsEllipseIv.setImageResource(UZResourcesIDFinder.getResDrawableID("ellipse_img"));
			fsTextIv.setImageResource(UZResourcesIDFinder.getResDrawableID("text_checked_img"));
			fsClearIv.setImageResource(UZResourcesIDFinder.getResDrawableID("clear_img"));
			m_pp.SetAction(factoryType.ft_Text, true);
		}else if(nid == UZResourcesIDFinder.getResIdID("color_ll")){
			m_pp.getColor();
		}else if(nid == UZResourcesIDFinder.getResIdID("clear_ll")){
			fsClearIv.setImageResource(UZResourcesIDFinder.getResDrawableID("clear_img"));
			m_pp.Delete();
			//			TL_PadAction.factoryType mAction = null;
			//			m_pp.SetAction(mAction, true);
			//			mAction = null;
		}
	}

	public void SetBkPath(String strPath){
	}

	@Override
	public void onConfigurationChanged(Configuration config){
		if(m_pp!=null){
			m_pp.OnConfigChanged();
		}
		if(shareSycnLl!=null){
			if(isbroadcast){
				changeToolBarLayout();
			}
		}
	}

	@Override
	public boolean Needhandled(MotionEvent me) {

		if(m_pp!=null&&m_pp.needHandleMoveMent(me))
			return true;
		return false;
	}
	public void showName(boolean bShow){
		if(m_tv_name!=null)
			m_tv_name.setVisibility(bShow?View.VISIBLE:View.GONE);
//		if(img_photograph != null && img_picture != null && img_document != null){
//			if(bShow){
//				img_photograph.setVisibility(View.GONE);
//				img_picture.setVisibility(View.GONE);
//				img_document.setVisibility(View.GONE);
//			}else{
//				img_photograph.setVisibility(View.VISIBLE);
//				img_picture.setVisibility(View.VISIBLE);
//				img_document.setVisibility(View.VISIBLE);
//			}
//		}
	}

	@Override
	public void onChange() {
		ShareDoc sd = m_sharepadmgr.mCurrentShareDoc;
		if(sd == null){
		}else{
			if(sd.bIsBlank)sd.fileName = getString(UZResourcesIDFinder.getResStringID("share_pad"));

			if(sd.pageCount == 1){
				m_tv_name.setText(sd.fileName);
			}else{
				m_tv_name.setText(sd.fileName + "  " + sd.currentPage +"/" + sd.pageCount );
			}
		}
	}

	@Override
	public void didReceivedNotification(int id, Object... args) {
		switch (id) {
			case FaceShareControl.NET_CONNECT_BREAK:

				m_sharepadmgr
						.removeOnDataChangeListener(this);
				break;

			case FaceShareControl.NET_CONNECT_FAILED:

				m_sharepadmgr
						.removeOnDataChangeListener(this);
				break;

			case FaceShareControl.DELMEETING_DOC:

				int docid = (Integer) args[0];
				boolean isCurrentDoc = m_sharepadmgr.isCurrentDoc(docid);

				ShareDoc dc = m_sharepadmgr
						.getShareDocbyId(docid);
				if (dc == null)
					return;
				shareControl.sendDocChange(dc.docID, true,
						dc.fileName, dc.fileUrl, dc.pageCount);//sendDocChange

				if (isCurrentDoc) {
					dc = m_sharepadmgr
							.getNextDoc(docid);
					if (dc == null)
						return;
					m_sharepadmgr
							.OnReceivePageChange(dc.docID, dc.currentPage);
					shareControl.sendShowPage(dc.docID,
							dc.currentPage);

				} else
					m_sharepadmgr
							.OnReceiveDocChange(false, dc);
				break;

			case FaceShareControl.UI_NOTIFY_USER_CHAIRMAN_CHANGE:
				if(m_sharepadmgr.getControlMode()==ControlMode.fullcontrol)//isChairman
					fsTotalBigIv.setVisibility(View.VISIBLE);
				else
					fsTotalBigIv.setVisibility(View.GONE);
				break;
			case FaceShareControl.UI_NOTIFY_USER_HOSTSTATUS:
				int userID = (Integer)args[0];
				if(m_sharepadmgr.getControlMode()==ControlMode.fullcontrol)	{//isMyAllow未完成
					fsTotalBigIv.setVisibility(View.VISIBLE);
				}else{
					fsTotalBigIv.setVisibility(View.GONE);
				}


				break;
//		case FaceShareControl.UI_NOTIFY_PLAY_MOVIE:
//			int senderid = (Integer) args[0];
//			boolean isplay = (Boolean) args[1];
//			if(isplay){
//				m_pp.setVisibility(View.GONE);
//				movieView.setVisibility(View.VISIBLE);
//			}else{
//				m_pp.setVisibility(View.VISIBLE);
//				movieView.setVisibility(View.GONE);
//			}
//			MeetingSession.getInstance().playMovie(senderid, isplay, movieView, 0, 0, 1, 1, 0, false);
//			break;
		}
	}
	@Override
	public void onResume() {
		if (bDocFragmentFinish) {
			bDocFragmentFinish = false;
		}
		super.onResume();
	}
	public void changeControlMode(){
		if(fsTotalBigIv == null){
			return;
		}
		if(m_sharepadmgr.getControlMode()==ControlMode.fullcontrol)	{//isMyAllow未完成
			fsTotalBigIv.setVisibility(View.VISIBLE);
			fsbottomLl.setVisibility(View.GONE);
			if(m_pp!=null)
				m_pp.setZoomMode(true);
			if(m_penClickListener!=null)
				m_penClickListener.OnPenClick(true);

		}else{
			fsTotalBigIv.setVisibility(View.GONE);
			fsbottomLl.setVisibility(View.GONE);
			if(m_pp!=null)
				m_pp.setZoomMode(true);
			if(m_penClickListener!=null)
				m_penClickListener.OnPenClick(true);
		}
	}
	private void changeToolBarLayout(){
		if(shareSycnLl==null){
			return;
		}
		shareSycnLl.setOrientation(LinearLayout.VERTICAL);

		fsbottomLl.setOrientation(LinearLayout.VERTICAL);
		FrameLayout.LayoutParams paramsbom = (android.widget.FrameLayout.LayoutParams) fsbottomLl.getLayoutParams();
		paramsbom.gravity = Gravity.LEFT;
		paramsbom.width = 120;
		paramsbom.height = LayoutParams.MATCH_PARENT;
		paramsbom.bottomMargin = 100;
		fsbottomLl.setLayoutParams(paramsbom);

		FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) fsTotalBigIv.getLayoutParams();
		params.gravity = Gravity.TOP;
//		params.leftMargin = 120;
		fsTotalBigIv.setLayoutParams(params);
		fsTotalBigIv.setPadding(10, 10, 10, 10);
		LinearLayout.LayoutParams parbot = (android.widget.LinearLayout.LayoutParams) fsMarkerPenLL.getLayoutParams();
		parbot.gravity = Gravity.CENTER_HORIZONTAL;
		parbot.height = 0;
		parbot.width = LayoutParams.WRAP_CONTENT;
		fsMarkerPenLL.setLayoutParams(parbot);
		fsArrowLineLl.setLayoutParams(parbot);
		fsClearLL.setLayoutParams(parbot);
		fsColorLl.setLayoutParams(parbot);
		fsEllipseLl.setLayoutParams(parbot);
		fsLineLL.setLayoutParams(parbot);
		fsTotalIv.setLayoutParams(parbot);

		RelativeLayout.LayoutParams relP = (android.widget.RelativeLayout.LayoutParams) m_tv_name.getLayoutParams();
		relP.setMargins(20, 0, 0, 170);
		m_tv_name.setLayoutParams(relP);
	}

	public void setZoomMode(boolean bZoom){
		if(m_pp!=null){
			m_pp.setZoomMode(bZoom);
		}else{
			this.bZoom = bZoom;
		}
	}
	public void setPadSizeMode(int padSizeMode){
		if(m_pp!=null){
			m_pp.setPadSize(padSizeMode);
		}else{
			this.padSizeMode = padSizeMode;
		}
	}
}

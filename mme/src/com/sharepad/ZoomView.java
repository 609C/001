package com.sharepad;


import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;

import com.utils.Utitlties;
import com.utils.WeiyiMeetingClient;


public class ZoomView extends View implements OnClickListener {


	private boolean waitDouble = true;
	private static final int DOUBLE_CLICK_TIME = 350;
	private float dbZoomScale = (float)1.000000;

	private boolean bZoomMode = true;
	private boolean bZoomMove;

	private PointF mfZoomCheckPoint = new PointF();

	private float dbZoomDest;

	private PointF mfMovePoint = new PointF();
	private int		m_nOldWidth;
	private int		m_nOldHeight;
	private RectF   m_rcBK = new RectF();
	private RectF   m_rcBKOrigin;
	private RectF   m_rcScale = new RectF();
	private RectF   m_rcBKBeforMove = new RectF();
	private RectF  scaleRect = new RectF();//caleRect就是播放的屏幕图像相对于rcBk的相对位置

	private boolean bTouchMoved = false;

	private info.emm.sdk.VideoView m_vActionView = null;;

	//	Bitmap btForDraw = null;
	boolean bZoomed = false;
	boolean bMoved  = false;

	int nZoomLock = 30;
	private int screenW;//屏幕的宽
	private int screenH;//屏幕的高

	public ZoomView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ZoomView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOnClickListener(this);
		this.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				return onTouchET(arg1);
			}

		});
	}

	public ZoomView(Context context) {
		super(context);
	}

	public void SetActionView(View v){
		m_vActionView = (info.emm.sdk.VideoView)v;
	}

	@Override
	public void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		m_rcBK.left = l;
		m_rcBK.top = t;
		m_rcBK.right = r;
		m_rcBK.bottom = b;
		m_rcBKOrigin = new RectF(m_rcBK);
		WeiyiMeetingClient.getInstance().playScreen((info.emm.sdk.VideoView) m_vActionView, 0,  0,1, 1, 0);

	}

	public boolean onTouchET(MotionEvent event) {
		int nAction = event.getAction();
		boolean bhandle = true;
		switch(nAction){
			case MotionEvent.ACTION_DOWN:bhandle = OnTouchDown(event);break;//0
			case MotionEvent.ACTION_UP:bhandle = OnTouchUp(event);break;//1
			case MotionEvent.ACTION_MOVE:bhandle = OnTouchMove(event);break;//2
		}
		switch( nAction & MotionEvent.ACTION_MASK){  //261
			case MotionEvent.ACTION_POINTER_DOWN:OnMutiTouchDown(event);break;//5
			case MotionEvent.ACTION_POINTER_UP:OnMutiTouchUp(event);break;//6
		}
		String sOut = nAction + ":" +event.getX() + "-" +event.getY();
		Log.d("touch", sOut); //DEBUG

		if(bhandle)getParent().requestDisallowInterceptTouchEvent(true);

		return super.onTouchEvent(event);
	}
	public boolean OnTouchUp(MotionEvent event){


		if(bZoomMode){
			CheckZoom();

			if ( waitDouble == true )
			{
				waitDouble = false;
				Thread thread = new Thread() {
					@Override
					public void run() {
						try {
							sleep(DOUBLE_CLICK_TIME);
							if ( waitDouble == false ) {
								waitDouble = true;
								Utitlties.RunOnUIThread(new Runnable(){
									@Override
									public void run() {
										if(!bZoomed&&m_ppcl!=null&&equalRect(m_rcBKBeforMove,m_rcBK)){
											m_ppcl.OnClick();
										}
									}
								});
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				};
				thread.start();
			}
			else {
				waitDouble = true;
				if(!bZoomed&&m_ppcl!=null&&equalRect(m_rcBKBeforMove,m_rcBK)){
					doubleclick(new PointF(event.getX(),event.getY()));
				}
			}



			float l = m_rcBK.left/m_rcBKOrigin.width();
			float r = m_rcBK.right/m_rcBKOrigin.width();
			float t = m_rcBK.top/m_rcBKOrigin.height();
			float b = m_rcBK.bottom/m_rcBKOrigin.height();

			WeiyiMeetingClient.getInstance().playScreen( m_vActionView, l,  t,r, b, 0);
			return bTouchMoved;
		}
		return true;
	}
	public boolean equalRect(RectF rc1,RectF rc2){
		if(rc1.left == rc2.left&&
				rc1.top == rc2.top&&
				rc1.right == rc2.right&&
				rc1.bottom == rc2.bottom){
			return true;
		}
		return false;
	}
	public boolean OnTouchDown(MotionEvent event){ //坐标是基于当前控件的坐标
		if(bZoomMode){

			invalidPos();
			bZoomed = false;
			m_rcBKBeforMove.left = m_rcBK.left;// = new RectF(m_rcBK);
			m_rcBKBeforMove.top = m_rcBK.top;
			m_rcBKBeforMove.right = m_rcBK.right;
			m_rcBKBeforMove.bottom = m_rcBK.bottom;
			bTouchMoved = false;
			bZoomMove = true;
			mfMovePoint.x = event.getX();
			mfMovePoint.y = event.getY();

			int nWidth = this.getWidth();


			if(mfMovePoint.x<nWidth/2&&m_rcBK.left >= 0)return false;
			else if(mfMovePoint.x>nWidth/2&&m_rcBK.right  <=nWidth)return false;

			return true;
		}
		return true;
	}
	public void OnMutiTouchUp(MotionEvent event){
		if(bZoomMode){
			if(event.getPointerCount() == 1){
				mfMovePoint.x = event.getX(0);
				mfMovePoint.y = event.getY(0);
			}
			CheckZoom();
			return;
		}
	}
	public boolean OnMutiTouchMove(MotionEvent event){

		RectF rcOld = new RectF(m_rcBK);

		if(event.getPointerCount()<2){

			if(!bZoomMove)return false;

			PointF currentMovePoint = new PointF(event.getX(),event.getY());

			m_rcBK.offset(currentMovePoint.x - mfMovePoint.x, currentMovePoint.y - mfMovePoint.y);

			mfMovePoint = currentMovePoint;
		}
		else{
			bZoomMove = false;
			PointF ptA = new PointF(event.getX(0),event.getY(0));
			PointF ptB = new PointF(event.getX(1),event.getY(1));
			PointF currentZoomCheckPoint = new PointF((ptA.x+ptB.x)/2,(ptA.y+ptB.y)/2);
			float x = event.getX(0) - event.getX(1);
			float y = event.getY(0) - event.getY(1);
			float currentZoomDest = FloatMath.sqrt(x * x + y * y);

			if(dbZoomDest == 0)dbZoomDest = currentZoomDest;


			float ZoomScale = (currentZoomDest/dbZoomDest);

			// float nextZoomScale=dbZoomScale * ZoomScale;
			//dbZoomScale = nextZoomScale;

			if(m_rcBK.width()*ZoomScale / this.getWidth() >4.0){
				ZoomScale = (float) ((this.getWidth()*4.0)/m_rcBK.width());
			}

			m_rcBK.right *= ZoomScale;
			m_rcBK.top *= ZoomScale;
			m_rcBK.left *= ZoomScale;
			m_rcBK.bottom *= ZoomScale;

			dbZoomScale = m_rcBK.width()/this.getWidth();


			PointF ZoomCheckPoint = new PointF();
			ZoomCheckPoint.x = m_rcBK.width()*mfZoomCheckPoint.x + m_rcBK.left;
			ZoomCheckPoint.y = m_rcBK.height()*mfZoomCheckPoint.y + m_rcBK.top;

			dbZoomDest = currentZoomDest;

			m_rcBK.offset(currentZoomCheckPoint.x - ZoomCheckPoint.x, currentZoomCheckPoint.y - ZoomCheckPoint.y);

			bZoomed = true;
		}


		float W = m_rcBK.width();
		float H = m_rcBK.height();
		if(screenH==0||H==0){
			return false;
		}
		if(screenW/screenH > W/H)
		{
			H = screenH * W/screenW;
			scaleRect.left  = 0;
			scaleRect.right = (int)W;
			scaleRect.top =  (int)((m_rcBK.height() - H) / 2);
			scaleRect.bottom = scaleRect.top + (int)H;
		}
		else
		{
			W = screenW * H/screenH;
			scaleRect.left  = (int)((m_rcBK.width() - W) / 2);
			scaleRect.right = scaleRect.left + (int)W;
			scaleRect.top = 0;
			scaleRect.bottom = (int)H;
		}

		if(scaleRect.width()<this.getWidth()){
			m_rcBK.offsetTo((this.getWidth() - scaleRect.width())/2 - scaleRect.left, m_rcBK.top);
		}else if(m_rcBK.left + scaleRect.left > 0){
			m_rcBK.offsetTo(-scaleRect.left, m_rcBK.top);
		}else if(m_rcBK.right<this.getWidth() + scaleRect.left){
			m_rcBK.offset(this.getWidth() - m_rcBK.right + scaleRect.left, 0);
		}
		//m_rcBk代表view的大小
		//this.getHeight 手机屏幕高度
		//scaleRect>height()共享屏幕的高度

		if(scaleRect.height()<this.getHeight()){
			m_rcBK.offsetTo(m_rcBK.left,(this.getHeight() - scaleRect.height())/2 - scaleRect.top);
			Log.e("TAG", "1..............");
		}else if(m_rcBK.top + scaleRect.top > 0){
			m_rcBK.offsetTo(m_rcBK.left, -scaleRect.top);
			Log.e("TAG", "2..............");
		}else if(m_rcBK.bottom <this.getHeight() + scaleRect.top){
			m_rcBK.offset(0,this.getHeight() - m_rcBK.bottom  + scaleRect.top);
			Log.e("TAG", "3..............");
		}
		// mfZoomCheckPoint = currentZoomCheckPoint;
		Log.e("TAG", m_rcBK.toString());
		this.invalidPos();

		if(equalRect(rcOld,m_rcBK))return false;

		return true;
	}
	void CheckZoom(){
		if(dbZoomScale<1.0){
			m_rcBK.left = 0;
			m_rcBK.top = 0;
			m_rcBK.right = this.getWidth();
			m_rcBK.bottom = this.getHeight();

			dbZoomScale = (float) 1.0;
			invalidPos();
		}
	}

	public void OnMutiTouchDown(MotionEvent event){
		if(event.getPointerCount()<2)return;
		bZoomed = false;
		PointF ptA = new PointF(event.getX(0),event.getY(0));
		PointF ptB = new PointF(event.getX(1),event.getY(1));
		PointF ZoomCheckPoint = new PointF((ptA.x+ptB.x)/2,(ptA.y+ptB.y)/2);

		mfZoomCheckPoint.x = (ZoomCheckPoint.x-m_rcBK.left)/m_rcBK.width();
		mfZoomCheckPoint.y = (ZoomCheckPoint.y-m_rcBK.top)/m_rcBK.height();

		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);

		dbZoomDest = FloatMath.sqrt(x * x + y * y);

	}
	public boolean OnTouchMove(MotionEvent event){
		if(bZoomMode){
			bTouchMoved =  OnMutiTouchMove( event);
			return bTouchMoved;
		}
		return true;
	}

	public void invalidPos(){
		if(m_vActionView == null)return;


		float l = m_rcBK.left/m_rcBKOrigin.width();
		float r = m_rcBK.right/m_rcBKOrigin.width();
		float t = m_rcBK.top/m_rcBKOrigin.height();
		float b = m_rcBK.bottom/m_rcBKOrigin.height();

		WeiyiMeetingClient.getInstance().playScreen( m_vActionView, l,  t,r, b, 0);

		//	if(btForDraw== null){
		//btForDraw = MeetingSession.getInstance().cutPicture(MeetingSession.getInstance().m_nScreenSharePeerID,false);
		//	CheckBkImageSize();
		//	}
		//	invalidate();
	}
	public void CheckBkImageSize(){
		int m_nOldHeight = this.getHeight();
		int m_nOldWidth = this.getWidth();

		int nimageHeight = Math.min(m_nOldHeight,m_nOldWidth);
		int nimagew = nimageHeight;


		if(m_nOldHeight == 0 || nimageHeight == 0 || m_nOldWidth == 0 || nimagew == 0) return;

		double bkness = m_nOldWidth*1.0/m_nOldHeight;
		double imageness = nimagew*1.0/nimageHeight;

		double realness = 0;

		if(imageness>bkness){
			realness = nimagew*1.0/m_nOldWidth;

			double realHeight = nimageHeight*1.0/realness;
			m_rcScale.left = 0;
			m_rcScale.right = 1;
			m_rcScale.top = (float) (Math.abs(m_nOldHeight - realHeight)/2)/m_nOldHeight;
			m_rcScale.bottom = m_rcScale.top + (float) (realHeight/m_nOldHeight);
		}else{
			realness = nimageHeight*1.0/m_nOldHeight;
			double realWidth = nimagew*1.0/realness;
			m_rcScale.left =  (float) (Math.abs(m_nOldWidth - realWidth)/2)/m_nOldWidth;
			m_rcScale.right = m_rcScale.left +  (float)(realWidth/m_nOldWidth);
			m_rcScale.top = 0;
			m_rcScale.bottom = 1;//m_rcBK.top + realHeight;
		}
	}
	public abstract static interface ZoomViewClickListener{
		public abstract void OnClick();
	}

	public ZoomViewClickListener m_ppcl = null;

	public void setOnZoomViewClickListener(ZoomViewClickListener zoomViewClickListener){
		m_ppcl = zoomViewClickListener;
	}
	@Override
	public void onClick(View arg0) {

	}


	public void setWH(int widht,int height){
		screenW = widht;
		screenH = height;
	}

	private void doubleclick(PointF point) {

		float ZoomScale = 4;
		RectF rcBK = new RectF(m_rcBK);
		if(rcBK.width() / this.getWidth() >1.1){
			rcBK.right = m_rcBKOrigin.right;
			rcBK.top = m_rcBKOrigin.top;
			rcBK.left = m_rcBKOrigin.left ;
			rcBK.bottom = m_rcBKOrigin.bottom;
		}
		else{

			if(rcBK.width()*ZoomScale / this.getWidth() >4.0){
				ZoomScale = (float) ((this.getWidth()*4.0)/rcBK.width());
			}
			float fx = (point.x-rcBK.left)/rcBK.width();
			float fy= (point.y-rcBK.top)/rcBK.height();

			rcBK.right *= ZoomScale;
			rcBK.top *= ZoomScale;
			rcBK.left *= ZoomScale;
			rcBK.bottom *= ZoomScale;

			dbZoomScale = rcBK.width()/this.getWidth();


			PointF ZoomCheckPoint = new PointF();
			ZoomCheckPoint.x = rcBK.width()*fx + rcBK.left;
			ZoomCheckPoint.y = rcBK.height()*fy + rcBK.top;


			rcBK.offset(point.x - ZoomCheckPoint.x, point.y - ZoomCheckPoint.y);
		}

		if(rcBK.width()<this.getWidth()){
			rcBK.offsetTo((this.getWidth() - rcBK.width())/2, rcBK.top);
		}else if(rcBK.left>0){
			rcBK.offsetTo(0, rcBK.top);
		}else if(rcBK.right<this.getWidth()){
			rcBK.offset(this.getWidth() - rcBK.right, 0);
		}


		if(rcBK.height()<this.getHeight()){
			rcBK.offsetTo(rcBK.left,(this.getHeight() - rcBK.height())/2);
		}else if(rcBK.top>0){
			rcBK.offsetTo(rcBK.left,0);
		}else if(rcBK.bottom<this.getHeight()){
			rcBK.offset(0,this.getHeight() - rcBK.bottom);
		}
		ZoomTo(rcBK);
		bZoomed = true;
	}

	public void ZoomTo(final RectF rcbk){
		final RectF reNow = new RectF(m_rcBK);
		new Thread(new Runnable(){
			int nTime = 6;
			float nleft = (rcbk.left - reNow.left)/6;
			float nright = (rcbk.right - reNow.right)/6;
			float ntop= (rcbk.top - reNow.top)/6;
			float nbottom = (rcbk.bottom - reNow.bottom)/6;
			@Override
			public void run() {
				while(nTime >0){
					nTime--;
					RectF reNext = new RectF(m_rcBK);
					reNext.left+=nleft;
					reNext.right+=nright;
					reNext.top+=ntop;
					reNext.bottom+=nbottom;
					UIThreadZoomto( reNext);
					try{
						Thread.sleep(30);
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		}).start();;
	}
	public void UIThreadZoomto( RectF rcbk){
		m_rcBK.left = rcbk.left;
		m_rcBK.right = rcbk.right;
		m_rcBK.top = rcbk.top;
		m_rcBK.bottom = rcbk.bottom;


		float l = m_rcBK.left/m_rcBKOrigin.width();
		float r = m_rcBK.right/m_rcBKOrigin.width();
		float t = m_rcBK.top/m_rcBKOrigin.height();
		float b = m_rcBK.bottom/m_rcBKOrigin.height();

		WeiyiMeetingClient.getInstance().playScreen( m_vActionView, l,  t,r, b, 0);
	}
}

package com.weiyicloud.whitepad;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.weiyicloud.whitepad.SharePadMgr.DataChangeListener;
import com.weiyicloud.whitepad.TL_PadAction.factoryType;

import java.util.ArrayList;


@SuppressLint("NewApi")
public class PaintPad extends View  implements DataChangeListener{

	//	public interface PaintPadSyncInterface  {
//		void SendActions(int nActs,Object data);
//	}
	private boolean waitDouble = true;
	private static final int DOUBLE_CLICK_TIME = 350;
	private boolean m_bPadEditer = true;

	private TL_PadAction m_tl_CurrentPadAction = null;

	private TL_PadAction.factoryType 	m_nActionMode = null;

	private boolean  	m_bActionfill;

	private int 	m_nPenWidth = 10;

	private int 	m_nPenColor =  0xff2f4f4f;

	private PointF  m_ptSeletStart = new PointF();

	private PointF  m_ptSeletEnd = new PointF();

	private boolean  m_btSeleting;

	private boolean  m_btSeletmoving;

	private PointF  m_ptMovingStart = new PointF();

	private RectF   m_rcOriginBK = new RectF();

	private int		m_nOldWidth;
	private int		m_nOldHeight;

	private RectF   m_rcBK = new RectF();

	private RectF   m_orgRcBK = null;

	private RectF   m_rcBKBeforMove = new RectF();
	private int       m_nBitHashCode = 0;
	private static final int ActionBorder = 15;

	private FaceShareControl m_iSync;

	//private Bitmap mBkBitmap;

	private SharePadMgr m_thisPadMgr;
	///////////////////////////////////////

	private boolean bSelectMode=false;

	//	private boolean bDeleteMode=false;

	private boolean bTouchMoved = false;
	//////////////////////////////////
	private float dbZoomScale = (float)1.000000;

	private boolean bZoomMode = true;
	private boolean bZoomMove;

	private PointF mfZoomCheckPoint = new PointF();

	private float dbZoomDest;

	private PointF mfMovePoint = new PointF();


	boolean bZoomed = false;
	//////////////////////////////////
	private PointF m_longClickPoint = new PointF();;

	private boolean isMoved;
	private boolean isReleased;
	private int mCounter;
	private ColorAdapter adapter;

	private Runnable mLongPressRunnable  = new Runnable() {

		@Override
		public void run() {
			mCounter--;
			if(mCounter>0 || isReleased || isMoved) return;
			OnTouchDownLong();
		}
	};
	private static final int TOUCH_SLOP = 20;
	////////////////////////////////////

//	public static final int ADD_ACTION = 1;
//	public static final int MODIFY_ACTION = 2;
//	public static final int DELETE_ACTION = 3;

	private View m_theView;
	///////////////////////////////////////////

	PopupWindow popupWindow;
	int[] img ={UZResourcesIDFinder.getResDrawableID("round_color_red"),UZResourcesIDFinder.getResDrawableID("round_color_gray"),UZResourcesIDFinder.getResDrawableID("round_color_green"),UZResourcesIDFinder.getResDrawableID("round_color_orange"),
			UZResourcesIDFinder.getResDrawableID("round_color_red"),UZResourcesIDFinder.getResDrawableID("round_color_blue"),UZResourcesIDFinder.getResDrawableID("round_color_red"),UZResourcesIDFinder.getResDrawableID("round_color_yellow"),UZResourcesIDFinder.getResDrawableID("round_color_peach"),UZResourcesIDFinder.getResDrawableID("round_color_dark_blue")};

	//////////////////////////////////////
	public PaintPad(Context context , AttributeSet attrs) {
		super(context,attrs);
		m_theView = this;
	}
	/***
	 * 设置白板尺寸模式，1=4:3;2=16:9
	 */
	int padSizeMode;
	public void setPadSize(int padSizeMode){
		this.padSizeMode = padSizeMode;
	}

	public void CheckBkImageSize(){
		m_nOldHeight = this.getHeight();
		m_nOldWidth = this.getWidth();

		int nimageHeight = Math.min(m_nOldHeight,m_nOldWidth);
		int nimagew = nimageHeight;
		if(padSizeMode == 1){
			nimageHeight = nimageHeight/4*3;
		}else if(padSizeMode == 2){
			nimageHeight = nimageHeight/16*9;
		}

		Bitmap bt =getPadMgr().getCurrentImage();
		if(bt!=null){
			nimageHeight = bt.getHeight();
			nimagew = bt.getWidth();
		}

		if(m_nOldHeight == 0 || nimageHeight == 0 || m_nOldWidth == 0 || nimagew == 0) return;

		double bkness = m_nOldWidth*1.0/m_nOldHeight;
		double imageness = nimagew*1.0/nimageHeight;

		double realness = 0;

		if(imageness>bkness){
			realness = nimagew*1.0/m_nOldWidth;

			double realHeight = nimageHeight*1.0/realness;
			m_rcOriginBK.left = 0;
			m_rcOriginBK.right = m_nOldWidth;
			m_rcOriginBK.top = (float) (Math.abs(m_nOldHeight - realHeight)/2);
			m_rcOriginBK.bottom = (float) (m_rcOriginBK.top + realHeight);
		}else{
			realness = nimageHeight*1.0/m_nOldHeight;
			double realWidth = nimagew*1.0/realness;
			m_rcOriginBK.left =  (float) (Math.abs(m_nOldWidth - realWidth)/2);
			m_rcOriginBK.right = (float) (m_rcOriginBK.left + realWidth);
			m_rcOriginBK.top = 0;
			m_rcOriginBK.bottom = m_nOldHeight;//m_rcBK.top + realHeight;
		}
		m_rcBK = new RectF(m_rcOriginBK);
		dbZoomScale = (float)1.000000;
	}
	public void SetAction(TL_PadAction.factoryType nAction,boolean bisFIll){
		m_nActionMode = nAction;

		m_bActionfill = bisFIll;

		SetSelectMode(false);
		setZoomMode(false);
		//setDeleteMode(false);

	}

	//	public void setDeleteMode(boolean bDelete)
	//	{
	//		bDeleteMode = bDelete;
	//		//		if(bDelete){
	//		//			SetSelectMode(false);
	//		//			setZoomMode(false);
	//		//		}
	//	}
	public void SetSelectMode(boolean bSelect){
		bSelectMode = bSelect;

		if(bSelectMode){
			setZoomMode(false);
			//			setDeleteMode(false);
		}
		else{
			boolean bUnSelect = false;
			for(int i = 0; i < getPadMgr().m_alActions.size(); i++){
				TL_PadAction tl_pa = getPadMgr().m_alActions.get(i);
				calculateActionsRect(tl_pa);
				if(tl_pa!=null && tl_pa.bSelect){
					tl_pa.bSelect = false;
					bUnSelect = true;
				}
			}
			if(bUnSelect)this.invalidate();
		}
	}

	@Override
	public void onDraw(Canvas cvs){
		if( this.isInEditMode())return;
		int nHeight = this.getHeight();
		int nWidth = this.getWidth();
		Log.e("emm","h="+nHeight+"w="+nWidth);
		if(nHeight!=m_nOldHeight||nWidth!=m_nOldWidth) CheckBkImageSize();

		if(PaintBk(cvs))
			PaintActions(cvs);
	}

	boolean PaintBk(Canvas cvs){
		if(m_rcBK.isEmpty())CheckBkImageSize();
		Bitmap bt = getPadMgr().getCurrentImage();
		if(bt!=null&&!bt.isRecycled()){

			if(bt.hashCode()!=m_nBitHashCode){
				CheckBkImageSize();
				m_nBitHashCode = bt.hashCode();
			}
			Rect rcSrc = new Rect(0,0,bt.getWidth(),bt.getHeight());
			//Rect rcDest = new Rect(0,0,this.getWidth(),this.getHeight());
			cvs.drawBitmap(bt,rcSrc,m_rcBK,null);
			Log.e("emm", "PaintBK******************1");
			return true;
		}
		else
		{
			Log.e("emm", "PaintBK******************2");
			if(m_nBitHashCode!=0)
			{
				CheckBkImageSize();
				m_nBitHashCode = 0;
			}
			if(getPadMgr().mCurrentShareDoc!=null && getPadMgr().mCurrentShareDoc.bIsBlank)
			{
				Paint linePaint = new Paint();
				linePaint.setColor(Color.WHITE);
				cvs.drawRect(m_rcBK, linePaint);
				return true;
			}
			else
			{
				TextPaint textPaint = new TextPaint();
				textPaint.setColor(Color.WHITE);
				textPaint.setTextSize(24.0F);
				FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
				float baseline = ((m_rcBK.bottom + m_rcBK.top - fontMetrics.bottom - fontMetrics.top) / 2);
				textPaint.setTextAlign(TextPaint.Align.CENTER);
				cvs.drawText(this.getResources().getString(UZResourcesIDFinder.getResStringID("String_loading_pic")), m_rcBK.centerX(), baseline, textPaint);
				return false;
			}
		}
	}

	void PaintActions(Canvas cvs){
		for(int i = 0; i < getPadMgr().m_alActions.size(); i++){
			TL_PadAction tl_pa = getPadMgr().m_alActions.get(i);
			//Log.i("emm", "hh:"+tl_pa.CoverArea);
			if(getPadMgr().mCurrentShareDoc!=null){
				if(getPadMgr().mCurrentShareDoc.docID!=tl_pa.nDocID  || getPadMgr().mCurrentShareDoc.currentPage != tl_pa.nPageID){
					continue;
				}
			}else{
				return;
			}

			PaintPadAction(tl_pa, cvs);
			PaintSelected(tl_pa, cvs);
		}
		if(m_tl_CurrentPadAction!=null){

			PaintPadAction(m_tl_CurrentPadAction, cvs);
			PaintSelected(m_tl_CurrentPadAction, cvs);
		}
		if(m_btSeleting && bSelectMode)
		{
			Paint linePaint = new Paint();
			linePaint.setColor(Color.BLACK);
			linePaint.setStrokeWidth(2);
			linePaint.setAntiAlias(true);
			linePaint.setStyle(Paint.Style.STROKE);

			Path path = new Path();
			PathEffect effects = new DashPathEffect(new float[] { 5, 5, 5, 5 }, 1);


			RectF Pathrect = new RectF();
			Pathrect.left = Math.min(m_ptSeletStart.x,m_ptSeletEnd.x);
			Pathrect.top = Math.min(m_ptSeletStart.y,m_ptSeletEnd.y);
			Pathrect.right = Math.max(m_ptSeletStart.x,m_ptSeletEnd.x);
			Pathrect.bottom = Math.max(m_ptSeletStart.y,m_ptSeletEnd.y);

			path.addRect(Pathrect, Path.Direction.CCW);

			linePaint.setPathEffect(effects);
			cvs.drawPath(path, linePaint);
		}
	}

	void PaintPadAction(TL_PadAction tl_pa, Canvas cvs){
		switch(tl_pa.nActionMode){
			case ft_line:{

				Paint linePaint = new Paint();
				linePaint.setColor(tl_pa.nPenColor);
				linePaint.setStrokeWidth(tl_pa.nPenWidth*dbZoomScale);
				linePaint.setAntiAlias(true);
				PointF ptStart = null,ptstop = null;
				if(tl_pa.alActionPoint.size() == 2){
					ptStart = unRelativePoint(tl_pa.alActionPoint.get(0));
					ptstop = unRelativePoint(tl_pa.alActionPoint.get(1));
				}
				else if(tl_pa.alActionPoint.size() == 1){
					ptStart = unRelativePoint(tl_pa.alActionPoint.get(0));
					ptstop = unRelativePoint(tl_pa.ptSizingEnd);
				}

				if(ptStart == null || ptstop == null){
					break;
				}
				Log.e("emm", "x1="+ptStart.x+"y1="+ptStart.y+"x2="+ptstop.x+"y2="+ptstop.y);

				if(ptStart.y < m_rcBK.top ){
					ptStart.y = m_rcBK.top;
				}else if(ptStart.y > m_rcBK.bottom){
					ptStart.y = m_rcBK.bottom;
				}
				if(ptstop.y < m_rcBK.top){
					ptstop.y = m_rcBK.top;
				}
				if(ptstop.y >m_rcBK.bottom){
					ptstop.y = m_rcBK.bottom;
				}


				if(ptStart.x < m_rcBK.left ){
					ptStart.x = m_rcBK.left;
				}else if(ptStart.x > m_rcBK.right){
					ptStart.x = m_rcBK.right;
				}
				if(ptstop.x < m_rcBK.left){
					ptstop.x = m_rcBK.left;
				}
				if(ptstop.x >m_rcBK.right){
					ptstop.x = m_rcBK.right;
				}
				cvs.drawLine(ptStart.x, ptStart.y, ptstop.x, ptstop.y, linePaint);
			}break;
			case ft_markerPen:{

				int nSize = tl_pa.alActionPoint.size();
				if(nSize<=2){
					break;
				}

				Paint linePaint = new Paint();
				linePaint.setColor(tl_pa.nPenColor);
				linePaint.setStrokeWidth(tl_pa.nPenWidth*dbZoomScale);
				linePaint.setAntiAlias(true);
				linePaint.setStyle(Paint.Style.STROKE);
				//float points[] = new float[(nSize-1)*4];
				cvs.drawPath(getMarkPenPath(tl_pa), linePaint);
			}break;
			case ft_Ellipse:{
				Paint linePaint = new Paint();
				linePaint.setColor(tl_pa.nPenColor);
				linePaint.setStrokeWidth(tl_pa.nPenWidth*dbZoomScale);
				linePaint.setAntiAlias(true);
				if(!tl_pa.bIsFill)
					linePaint.setStyle(Paint.Style.STROKE);
				PointF ptStart = null,ptstop = null;
				if(tl_pa.alActionPoint.size() == 2){
					ptStart = unRelativePoint(tl_pa.alActionPoint.get(0));
					ptstop = unRelativePoint(tl_pa.alActionPoint.get(1));
				}
				else if(tl_pa.alActionPoint.size() == 1){
					ptStart = unRelativePoint(tl_pa.alActionPoint.get(0));
					ptstop = unRelativePoint(tl_pa.ptSizingEnd);
				}

				if(ptStart == null || ptstop == null){
					break;
				}
				if(ptStart.y < m_rcBK.top ){
					ptStart.y = m_rcBK.top;
				}else if(ptStart.y > m_rcBK.bottom){
					ptStart.y = m_rcBK.bottom;
				}
				if(ptstop.y < m_rcBK.top){
					ptstop.y = m_rcBK.top;
				}
				if(ptstop.y >m_rcBK.bottom){
					ptstop.y = m_rcBK.bottom;
				}

				if(ptStart.x < m_rcBK.left ){
					ptStart.x = m_rcBK.left;
				}else if(ptStart.x > m_rcBK.right){
					ptStart.x = m_rcBK.right;
				}
				if(ptstop.x < m_rcBK.left){
					ptstop.x = m_rcBK.left;
				}
				if(ptstop.x >m_rcBK.right){
					ptstop.x = m_rcBK.right;
				}

				RectF ovalrect = new RectF();
				ovalrect.left = Math.min(ptStart.x,ptstop.x);
				ovalrect.top = Math.min(ptStart.y,ptstop.y);
				ovalrect.right = Math.max(ptStart.x,ptstop.x);
				ovalrect.bottom = Math.max(ptStart.y,ptstop.y);
				cvs.drawOval(ovalrect, linePaint);
				if(tl_pa.bIsFill){
					linePaint.setStyle(Paint.Style.STROKE);
					cvs.drawOval(ovalrect, linePaint);
				}
			}break;
			case ft_Rectangle:{
				Paint linePaint = new Paint();
				linePaint.setColor(tl_pa.nPenColor);
				linePaint.setStrokeWidth(tl_pa.nPenWidth*dbZoomScale);
				linePaint.setAntiAlias(true);
				if(!tl_pa.bIsFill)
					linePaint.setStyle(Paint.Style.STROKE);//

				PointF ptStart = null,ptstop = null;
				if(tl_pa.alActionPoint.size() == 2){
					ptStart = unRelativePoint(tl_pa.alActionPoint.get(0));
					ptstop = unRelativePoint(tl_pa.alActionPoint.get(1));
				}
				else if(tl_pa.alActionPoint.size() == 1){
					ptStart = unRelativePoint(tl_pa.alActionPoint.get(0));
					ptstop =  unRelativePoint(tl_pa.ptSizingEnd);
				}

				if(ptStart == null || ptstop == null){
					break;
				}
				if(ptStart.y < m_rcBK.top ){
					ptStart.y = m_rcBK.top;
				}else if(ptStart.y > m_rcBK.bottom){
					ptStart.y = m_rcBK.bottom;
				}
				if(ptstop.y < m_rcBK.top){
					ptstop.y = m_rcBK.top;
				}
				if(ptstop.y >m_rcBK.bottom){
					ptstop.y = m_rcBK.bottom;
				}


				if(ptStart.x < m_rcBK.left ){
					ptStart.x = m_rcBK.left;
				}else if(ptStart.x > m_rcBK.right){
					ptStart.x = m_rcBK.right;
				}
				if(ptstop.x < m_rcBK.left){
					ptstop.x = m_rcBK.left;
				}
				if(ptstop.x >m_rcBK.right){
					ptstop.x = m_rcBK.right;
				}


				RectF ovalrect = new RectF();
				ovalrect.left = Math.min(ptStart.x,ptstop.x);
				ovalrect.top = Math.min(ptStart.y,ptstop.y);
				ovalrect.right = Math.max(ptStart.x,ptstop.x);
				ovalrect.bottom = Math.max(ptStart.y,ptstop.y);
				cvs.drawRect(ovalrect, linePaint);
				if(tl_pa.bIsFill){
					linePaint.setStyle(Paint.Style.STROKE);
					cvs.drawRect(ovalrect, linePaint);
				}
			}break;
			case ft_Text:{
				TextPaint textPaint = new TextPaint();
				textPaint.setColor(tl_pa.nPenColor);
				textPaint.setTextSize(tl_pa.nPenWidth*dbZoomScale);
				StaticLayout layout = new StaticLayout(tl_pa.sText,textPaint,cvs.getWidth(),Alignment.ALIGN_NORMAL,1.0F,0.0F,true); //闂備浇娉曢崰鎰板几婵犳艾绠柣鎴ｅГ閺呮悂鏌￠崒妯猴拷鏍拷姘炬嫹
				PointF ptStart = unRelativePoint(tl_pa.alActionPoint.get(0));
				cvs.save();
				if(ptStart == null){
					break;
				}
				if(ptStart.y < m_rcBK.top ){
					ptStart.y = m_rcBK.top;
				}
				if(ptStart.y > m_rcBK.bottom){
					ptStart.y = m_rcBK.bottom - tl_pa.nPenWidth*dbZoomScale-10;
				}


				if(ptStart.x < m_rcBK.left ){
					ptStart.x = m_rcBK.left;
				}
				if(ptStart.x >m_rcBK.right){
					ptStart.x = m_rcBK.right;
				}
				cvs.translate(ptStart. x, ptStart. y);
				Log.i("emm", ptStart.x +","+ptStart.y);
				layout.draw(cvs);
				cvs.restore();
			}break;
			case ft_arrowLine:{

				Paint linePaint = new Paint();
				linePaint.setColor(tl_pa.nPenColor);
				//linePaint.setStrokeWidth(tl_pa.nPenWidth);
				linePaint.setAntiAlias(true);
				PointF ptStart = null,ptstop = null;
				if(tl_pa.alActionPoint.size() == 2){
					ptStart = unRelativePoint(tl_pa.alActionPoint.get(0));
					ptstop = unRelativePoint(tl_pa.alActionPoint.get(1));
				}
				else if(tl_pa.alActionPoint.size() == 1){
					ptStart = unRelativePoint(tl_pa.alActionPoint.get(0));
					ptstop =  unRelativePoint(tl_pa.ptSizingEnd);
				}

				if(ptStart == null || ptstop == null){
					break;
				}

				if(ptStart.y < m_rcBK.top ){
					ptStart.y = m_rcBK.top;
				}else if(ptStart.y > m_rcBK.bottom){
					ptStart.y = m_rcBK.bottom;
				}
				if(ptstop.y < m_rcBK.top){
					ptstop.y = m_rcBK.top;
				}
				if(ptstop.y >m_rcBK.bottom){
					ptstop.y = m_rcBK.bottom;
				}

				if(ptStart.x < m_rcBK.left ){
					ptStart.x = m_rcBK.left;
				}else if(ptStart.x > m_rcBK.right){
					ptStart.x = m_rcBK.right;
				}
				if(ptstop.x < m_rcBK.left){
					ptstop.x = m_rcBK.left;
				}
				if(ptstop.x >m_rcBK.right){
					ptstop.x = m_rcBK.right;
				}


				Path path = new Path();
				getArrowPath(path,ptStart,ptstop);
				cvs.drawPath(path, linePaint);

			}break;
			default:
				break;
		}
	}
	void PaintSelected(TL_PadAction tl_pa, Canvas cvs){
		if(tl_pa.HotRegion != null&&tl_pa.bSelect && bSelectMode ){
			Paint linePaint = new Paint();
			if(m_btSeletmoving){
				linePaint.setStrokeWidth(2);
				linePaint.setColor(Color.RED);
			}
			else{
				linePaint.setStrokeWidth(1);
				linePaint.setColor(Color.BLACK);
			}
			linePaint.setAntiAlias(true);
			linePaint.setStyle(Paint.Style.STROKE);
			Path path = new Path();//tl_pa.HotRegion.getBoundaryPath();

			RectF recfpath = new RectF();
			recfpath.left = tl_pa.CoverArea.left;
			recfpath.top = tl_pa.CoverArea.top;
			recfpath.right = tl_pa.CoverArea.right;
			recfpath.bottom = tl_pa.CoverArea.bottom;

			path.addRect(recfpath, Path.Direction.CCW);


			PathEffect effects = new DashPathEffect(new float[] { 5, 5, 5, 5 }, 1);
			linePaint.setPathEffect(effects);

			cvs.drawPath(path, linePaint);

			cvs.save();

			int[] local = new int[2];

			getLocationInWindow(local);

			Region re = new Region(tl_pa.HotRegion);

			re.translate(local[0], local[1]);

			cvs.clipRegion(re);
			cvs.drawColor(Color.argb(128, 0, 0, 128));
			cvs.restore();
		}

	}

	public boolean isPadEditer() {
		return m_bPadEditer;
	}

	public void setPadEditer(boolean m_bPadEditer) {
		this.m_bPadEditer = m_bPadEditer;
	}



	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int nAction = event.getAction();
		boolean bhandle = true;
		switch(nAction)
		{

			//case MotionEvent.ACTION_CANCEL = 3;
			//OnTouchDown(event);OnTouchUp(event);OnTouchMove(event);
			case MotionEvent.ACTION_DOWN:bhandle = OnTouchDown(event);Log.e("emm", "onTouchEvent_04"+bhandle);break;//0
			case MotionEvent.ACTION_UP:bhandle = OnTouchUp(event);Log.e("emm", "onTouchEvent_05"+bhandle);break;//1
			case MotionEvent.ACTION_MOVE:
			{
				Log.e("emm", "onTouchEvent_06");
				bhandle = OnTouchMove(event);
				Log.e("emm", "onTouchEvent_07"+bhandle);
				if(bhandle){
					getParent().requestDisallowInterceptTouchEvent(true);
				}
				break;
			}
		}
		switch( nAction & MotionEvent.ACTION_MASK){  //261    MotionEvent.ACTION_MASK
			case MotionEvent.ACTION_POINTER_DOWN:OnMutiTouchDown(event);Log.e("emm", "onTouchEvent_08");break;//5
			case MotionEvent.ACTION_POINTER_UP:OnMutiTouchUp(event);Log.e("emm", "onTouchEvent_09");break;//6
		}
		String sOut = nAction + ":" +event.getX() + "-" +event.getY();

		Log.d("touch", sOut); //DEBUG
		Log.e("emm", "onTouchEvent_010");
		if(bhandle)
		{
			Log.e("emm", "onTouchEvent_011");
			getParent().requestDisallowInterceptTouchEvent(true);
			return true;
		}
		Log.e("emm", "onTouchEvent_012");
		return super.onTouchEvent(event);
	}

	void CheckZoom(){
		if(dbZoomScale<1.0){
			m_rcBK.left = m_rcOriginBK.left;
			m_rcBK.top = m_rcOriginBK.top;
			m_rcBK.right = m_rcOriginBK.right;
			m_rcBK.bottom = m_rcOriginBK.bottom;

			dbZoomScale = (float) 1.0;
			invalidate();
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
	public boolean OnMutiTouchMove(MotionEvent event){

		RectF rcOld = new RectF(m_rcBK);

		if(event.getPointerCount()<2){

			if(!bZoomMove){
				Log.e("emm", "OnMutiTouchMove_01");
				return false;
			};
			Log.e("emm", "OnMutiTouchMove_02");
			PointF currentMovePoint = new PointF(event.getX(),event.getY());

			m_rcBK.offset(currentMovePoint.x - mfMovePoint.x, currentMovePoint.y - mfMovePoint.y);

			mfMovePoint = currentMovePoint;
		}
		else{
			Log.e("emm", "OnMutiTouchMove_03");
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

			if(m_rcBK.width()*ZoomScale / m_rcOriginBK.width() >4.0){
				ZoomScale = (float) ((m_rcOriginBK.width()*4.0)/m_rcBK.width());
			}

			m_rcBK.right *= ZoomScale;
			m_rcBK.top *= ZoomScale;
			m_rcBK.left *= ZoomScale;
			m_rcBK.bottom *= ZoomScale;

			dbZoomScale = m_rcBK.width()/m_rcOriginBK.width();


			PointF ZoomCheckPoint = new PointF();
			ZoomCheckPoint.x = m_rcBK.width()*mfZoomCheckPoint.x + m_rcBK.left;
			ZoomCheckPoint.y = m_rcBK.height()*mfZoomCheckPoint.y + m_rcBK.top;

			dbZoomDest = currentZoomDest;

			m_rcBK.offset(currentZoomCheckPoint.x - ZoomCheckPoint.x, currentZoomCheckPoint.y - ZoomCheckPoint.y);
			bZoomed = true;
		}


		if(m_rcBK.width()<this.getWidth()){
			m_rcBK.offsetTo((this.getWidth() - m_rcBK.width())/2, m_rcBK.top);
		}else if(m_rcBK.left>0){
			m_rcBK.offsetTo(0, m_rcBK.top);
		}else if(m_rcBK.right<this.getWidth()){
			m_rcBK.offset(this.getWidth() - m_rcBK.right, 0);
		}

		if(m_rcBK.height()<this.getHeight()){
			m_rcBK.offsetTo(m_rcBK.left,(this.getHeight() - m_rcBK.height())/2);
		}else if(m_rcBK.top>0){
			m_rcBK.offsetTo(m_rcBK.left,0);
		}else if(m_rcBK.bottom<this.getHeight()){
			m_rcBK.offset(0,this.getHeight() - m_rcBK.bottom);
		}
		// mfZoomCheckPoint = currentZoomCheckPoint;
		this.invalidate();

		if(equalRect(rcOld,m_rcBK)){
			Log.e("emm", "OnMutiTouchMove_04");
			return false;
		}
		return false;
	}
	public boolean equalRect(RectF rc1,RectF rc2){
		if(rc1.left == rc2.left&&
				rc1.top == rc2.top&&
				rc1.right == rc2.right&&
				rc1.bottom == rc2.bottom){
			Log.e("emm", "equalRect_01");
			return true;
		}
		Log.e("emm", "equalRect_02");
		return false;
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

	public boolean OnTouchDown(MotionEvent event){
		if(bZoomMode){
			Log.e("emm", "OnTouchDown_01");
			bZoomed = false;
			m_rcBKBeforMove = new RectF(m_rcBK);
			bTouchMoved = false;
			bZoomMove = true;
			mfMovePoint.x = event.getX();
			mfMovePoint.y = event.getY();

			int nWidth = this.getWidth();


			if(mfMovePoint.x<nWidth/10&&m_rcBK.left >= 0){
				Log.e("emm", "OnTouchDown_01_false_01");
				return false;
			}
			else if(mfMovePoint.x>nWidth/10&&m_rcBK.right  <=nWidth){
				Log.e("emm", "OnTouchDown_01_false_02");
				return false;
			}
			Log.e("emm", "OnTouchDown_01_true");
			return true;
		}
		else if(bSelectMode){
			Log.e("emm", "OnTouchDown_02");
			boolean bTouchSelected = false;
			for(int i = 0; i< getPadMgr().m_alActions.size();i++){
				TL_PadAction tl_pa = getPadMgr().m_alActions.get( getPadMgr().m_alActions.size()-1-i);
				if(tl_pa.CoverArea.contains((int)event.getX(),(int)event.getY())){
					if(tl_pa.bSelect){
						bTouchSelected = true;
						//tl_pa.LinePath.transform(matrix);
						break;
					}
				}
			}
			m_ptMovingStart.x = event.getX();
			m_ptMovingStart.y = event.getY();
			m_longClickPoint.x = m_ptMovingStart.x;
			m_longClickPoint.y = m_ptMovingStart.y;
			mCounter++;
			isReleased = false;
			isMoved = false;
			postDelayed(mLongPressRunnable, 600);

			m_ptSeletStart.x = event.getX();
			m_ptSeletStart.y = event.getY();
			m_ptSeletEnd.x = event.getX();
			m_ptSeletEnd.y = event.getY();
			return true;
		}
		//		else if(bDeleteMode)
		//		{
		//			boolean bTouchSelected = false;
		//			for(int i = 0; i< getPadMgr().m_alActions.size();i++){
		//				TL_PadAction tl_pa = getPadMgr().m_alActions.get( getPadMgr().m_alActions.size()-1-i);
		//				if(tl_pa.CoverArea.contains((int)event.getX(),(int)event.getY())){
		//					if(tl_pa.bSelect){
		//						bTouchSelected = true;
		//						this.OnDelete(tl_pa);
		//						break;
		//					}
		//				}
		//			}
		//			return true;
		//		}
		if(m_nActionMode == null)return true;
		switch(m_nActionMode){
			case ft_markerPen:
			case ft_arrowLine:
			case ft_line:
			case ft_Rectangle:
			case ft_Ellipse:{
				if(m_tl_CurrentPadAction == null){
					m_tl_CurrentPadAction = new TL_PadAction();
					m_tl_CurrentPadAction.sID = m_tl_CurrentPadAction.hashCode()+"";
					m_tl_CurrentPadAction.nDocID = getPadMgr().mCurrentShareDoc.docID;
					m_tl_CurrentPadAction.nPageID = getPadMgr().mCurrentShareDoc.currentPage;
					m_tl_CurrentPadAction.nActionMode = m_nActionMode;
					m_tl_CurrentPadAction.nPenWidth = m_nPenWidth;
					m_tl_CurrentPadAction.nPenColor = m_nPenColor;
					m_tl_CurrentPadAction.bIsFill = m_bActionfill;
					m_tl_CurrentPadAction.alActionPoint.add(relativePoint(new PointF(event.getX(),event.getY())));
					Log.e("emm","touchdown x"+event.getX()+"y="+event.getY());
					if(m_nActionMode == TL_PadAction.factoryType.ft_markerPen){
						m_tl_CurrentPadAction.LinePath = new Path();
						m_tl_CurrentPadAction.LinePath.moveTo(event.getX(),event.getY());
					}
				}
			}break;
			case ft_Text:
			{
				insertText(event.getX(),event.getY());
			}break;
			default:
				break;
		}
		return true;
	}

	public boolean OnTouchUp(MotionEvent event){

		isReleased = true;

		if(bZoomMode){
			//mfMovePoint.x = event.getX();
			//mfMovePoint.y = event.getY();

			CheckZoom();
			if ( waitDouble == true )
			{
				Log.e("emm","OnTouchUp");
				waitDouble = false;
				Thread thread = new Thread() {
					@Override
					public void run() {
						try {
							sleep(DOUBLE_CLICK_TIME);
							if ( waitDouble == false ) {
								waitDouble = true;
								new Handler(getPadMgr().mAppContext.getMainLooper()).post(new Runnable(){
									@Override
									public void run() {
										if(!bZoomed&&equalRect(m_rcBKBeforMove,m_rcBK)){
											if(m_ppcl!=null)
												m_ppcl.OnClick(m_theView);
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
				Log.e("emm","doubleclick");
				waitDouble = true;
				if(!bZoomed&&equalRect(m_rcBKBeforMove,m_rcBK)){
					doubleclick(new PointF(event.getX(),event.getY()));
				}

			}

			return bTouchMoved;
		}
		else if(bSelectMode){
			if(m_btSeletmoving){
				float xoffset = event.getX() - m_ptMovingStart.x;
				float yoffset = event.getY() - m_ptMovingStart.y;
				ArrayList<TL_PadAction> al_movedActs = new ArrayList<TL_PadAction>();
				for(int i = 0; i< getPadMgr().m_alActions.size();i++){
					TL_PadAction tl_pa = getPadMgr().m_alActions.get( getPadMgr().m_alActions.size()-1-i);
					if(tl_pa.HotRegion != null && tl_pa.HotRegion.contains((int)event.getX(),(int)event.getY())){
						if(tl_pa.bSelect){
							tranlatePadAction(tl_pa,xoffset,yoffset);

							TL_PadAction tl_moved = new TL_PadAction();
							tl_moved.sID = tl_pa.sID;
							tl_moved.nActionMode =  tl_pa.nActionMode;
							for(int j = 0 ; j<tl_pa. alActionPoint.size();j++){
								PointF ptF = new PointF();
								PointF ptFreal = tl_pa. alActionPoint.get(j);
								ptF.x = ptFreal.x;
								ptF.y = ptFreal.y;
								tl_moved.alActionPoint.add(ptF);
							}
							UnRelativeAction(tl_moved);
							al_movedActs.add(tl_moved);
						}
					}
					else
					{
						//tl_pa.bSelect = false;
					}
				}
				m_btSeletmoving = false;

				if(m_iSync!= null) m_iSync.SendActions(WhitePadInterface.MODIFY_ACTION, al_movedActs);
			}
			else{
				m_ptSeletEnd.x = event.getX();
				m_ptSeletEnd.y = event.getY();
				Rect rcSelect = new Rect();
				rcSelect.left = (int)Math.min(m_ptSeletStart.x,m_ptSeletEnd.x);
				rcSelect.top = (int)Math.min(m_ptSeletStart.y,m_ptSeletEnd.y);
				rcSelect.right = (int)Math.max(m_ptSeletStart.x,m_ptSeletEnd.x);
				rcSelect.bottom = (int)Math.max(m_ptSeletStart.y,m_ptSeletEnd.y);

				boolean bsignleselect = false;
				for(int i = 0; i< getPadMgr().m_alActions.size();i++){
					TL_PadAction tl_pa = getPadMgr().m_alActions.get( getPadMgr().m_alActions.size()-1-i);

					if(rcSelect.width() <= 5 && rcSelect.height() <= 5){
						if(!bsignleselect&&tl_pa.HotRegion != null && tl_pa.HotRegion.contains(rcSelect.left,rcSelect.top)){

							tl_pa.bSelect = true;
							bsignleselect = true;
						}
						else
						{
							tl_pa.bSelect = false;
						}
					}
					else{
						Region TextRgn = new Region(tl_pa.HotRegion);

						if(tl_pa.HotRegion != null && TextRgn.op(rcSelect,Region.Op.INTERSECT)){

							tl_pa.bSelect = true;

						}
						else
						{
							tl_pa.bSelect = false;
						}
					}

				}
				m_btSeleting = false;
			}
			this.invalidate();
			return true;
		}else if(!bZoomMode){
			if(m_ppcl!=null)
				m_ppcl.OnClick(m_theView);
		}
		if(m_nActionMode == null)return true;
		switch(m_nActionMode){
			case ft_markerPen:
			case ft_arrowLine:
			case ft_line:
			case ft_Rectangle:
			case ft_Ellipse:{
				if(m_tl_CurrentPadAction != null){
					m_tl_CurrentPadAction.alActionPoint.add(relativePoint(new PointF(event.getX(), event.getY())));

					Log.e("emm","touchup x"+event.getX()+"y="+event.getY());

					GenerateNewActionID(m_tl_CurrentPadAction);
					calculateActionsRect(m_tl_CurrentPadAction);

					if(m_tl_CurrentPadAction.CoverArea == null || m_tl_CurrentPadAction.CoverArea.isEmpty()) {
						m_tl_CurrentPadAction = null;
						return true;
					}

					getPadMgr().m_alActions.add(m_tl_CurrentPadAction);


					TL_PadAction tl_pa = new  TL_PadAction();
					tl_pa.sID = tl_pa.sID;
					tl_pa.nActionMode =  tl_pa.nActionMode;
					tl_pa.alActionPoint = new  ArrayList<PointF>();
					for(int j = 0 ; j<m_tl_CurrentPadAction. alActionPoint.size();j++){
						PointF ptF = new PointF();
						PointF ptFreal = m_tl_CurrentPadAction. alActionPoint.get(j);
						ptF.x = ptFreal.x;
						ptF.y = ptFreal.y;
						tl_pa.alActionPoint.add(ptF);
					}

					if(m_iSync!= null) m_iSync.SendActions(WhitePadInterface.ADD_ACTION, m_tl_CurrentPadAction);

					Rect rcClip = m_tl_CurrentPadAction.CoverArea;
					m_tl_CurrentPadAction = null;
					if(rcClip!=null && rcClip.isEmpty())this.invalidate(rcClip);
					else
						this.invalidate();
				}
			}break;
			default:
				break;
		}
		return true;
	}

	public boolean OnTouchMove(MotionEvent event){
		if(bZoomMode){
			bTouchMoved =  OnMutiTouchMove( event);
			Log.e("emm", "OnTouchMove_01"+bTouchMoved);
			return bTouchMoved;
		}
		else if(bSelectMode){
			Log.e("emm", "OnTouchMove_02"+bTouchMoved);
			Rect rcClip = new Rect();
			if(m_btSeletmoving){
				float xoffset = event.getX() - m_ptMovingStart.x;
				float yoffset = event.getY() - m_ptMovingStart.y;
				for(int i = 0; i< getPadMgr().m_alActions.size();i++){
					TL_PadAction tl_pa = getPadMgr().m_alActions.get( getPadMgr().m_alActions.size()-1-i);

					if(tl_pa.bSelect){
						if(tl_pa.CoverArea!=null) rcClip.union(tl_pa.CoverArea);
						tranlatePadAction(tl_pa,xoffset,yoffset);
						if(tl_pa.CoverArea!=null) rcClip.union(tl_pa.CoverArea);
					}
					else
					{
						//tl_pa.bSelect = false;
					}
				}
				m_ptMovingStart.x = event.getX();
				m_ptMovingStart.y = event.getY();
			}
			else{
				if(!isMoved) ;
				if(Math.abs(m_longClickPoint.x-event.getX()) > TOUCH_SLOP
						|| Math.abs(m_longClickPoint.y-event.getY()) > TOUCH_SLOP) {
					isMoved = true;
				}
				if(!m_btSeletmoving)m_btSeleting = true;
				m_ptSeletEnd.x = event.getX();
				m_ptSeletEnd.y = event.getY();
				rcClip.union((int)m_ptSeletStart.x,(int)m_ptSeletStart.y,(int)m_ptSeletEnd.x,(int)m_ptSeletEnd.y);
			}
			rcClip.left-=1;
			rcClip.top-=1;
			rcClip.right+=1;
			rcClip.bottom+=1;

			if(rcClip!=null&&rcClip.isEmpty())this.invalidate(rcClip);
			else
				this.invalidate();

			return true;

		}
		if(m_nActionMode == null)return true;
		switch(m_nActionMode){
			case ft_arrowLine:
			case ft_line:
			case ft_Rectangle:
			case ft_Ellipse:{
				if(m_tl_CurrentPadAction != null){

					m_tl_CurrentPadAction.ptSizingEnd = relativePoint(new PointF(event.getX(),event.getY()));

					this.invalidate();
				}
			}break;
			case ft_markerPen:{
				if(m_tl_CurrentPadAction != null){
					m_tl_CurrentPadAction.alActionPoint.add(relativePoint(new PointF(event.getX(),event.getY())));
					//				m_tl_CurrentPadAction.LinePath.lineTo(event.getX(),event.getY());
					this.invalidate();
				}
			}break;
			default:
				break;

		}
		return true;
	}

	public void OnTouchDownLong(){

		Region coverhotUnion = new Region();
		for(int i = 0; i< getPadMgr().m_alActions.size();i++){
			TL_PadAction tl_pa = getPadMgr().m_alActions.get( getPadMgr().m_alActions.size()-1-i);

			if(tl_pa.bSelect){
				coverhotUnion.union(tl_pa.CoverArea);
			}
		}
		if(!coverhotUnion.isEmpty()){
			for(int i = 0; i< getPadMgr().m_alActions.size();i++){
				TL_PadAction tl_pa = getPadMgr().m_alActions.get( getPadMgr().m_alActions.size()-1-i);

				if(tl_pa.HotRegion != null && tl_pa.HotRegion.contains((int)m_longClickPoint.x,(int)m_longClickPoint.y)){
					coverhotUnion.union(tl_pa.CoverArea);
					tl_pa.bSelect = true;
					m_btSeletmoving = true;
					Vibrator vibrator = (Vibrator) this.getContext().getSystemService(Service.VIBRATOR_SERVICE);
					vibrator.vibrate(50);
				}
			}
		}
		else{
			if(m_longClickPoint!=null &&coverhotUnion.contains((int)m_longClickPoint.x,(int)m_longClickPoint.y) ){
				m_btSeletmoving = true;
				Vibrator vibrator = (Vibrator) this.getContext().getSystemService(Service.VIBRATOR_SERVICE);

				vibrator.vibrate(50);
			}
		}

		this.invalidate();
	}


	public int getPenWidth() {
		return m_nPenWidth;
	}


	public void setPenWidth(int nPenWidth) {
		this.m_nPenWidth = nPenWidth;
	}


	public int getPenColor() {
		return m_nPenColor;
	}


	public void setPenColor(int nPenColor) {
		this.m_nPenColor = nPenColor;
	}

	public void OnReceiveActions(int nACt,Object obj){
		switch(nACt){
			case WhitePadInterface.ADD_ACTION:{
				TL_PadAction tl_pa = (TL_PadAction)obj;
				RelativeAction(tl_pa);
				calculateActionsRect(tl_pa);
				getPadMgr().m_alActions.add(tl_pa);
				this.invalidate();
			}break;
			case WhitePadInterface.MODIFY_ACTION:{
				@SuppressWarnings("unchecked")
				ArrayList<TL_PadAction> al_realActs = (ArrayList<TL_PadAction>)obj;

				for(int i = 0 ; i < al_realActs.size() ;i++){

					TL_PadAction tl_paNew = al_realActs.get(i);

					for(int j = 0 ; j < getPadMgr().m_alActions.size(); j++){
						TL_PadAction tl_paOld = getPadMgr().m_alActions.get(j);
						if(tl_paOld.sID == tl_paNew.sID){
							RelativeAction(tl_paNew);
							calculateActionsRect(tl_paNew);
							tl_paOld = tl_paNew;
						}
					}
				}
				this.invalidate();

			}break;
			case WhitePadInterface.DELETE_ACTION:{
				@SuppressWarnings("unchecked")
				ArrayList<String> al_realActs = (ArrayList<String>)obj;

				for(int i = 0 ; i < al_realActs.size() ;i++){

					String deleteid = al_realActs.get(i);

					for(int j = 0 ; j < getPadMgr().m_alActions.size(); j++){
						TL_PadAction tl_paOld = getPadMgr().m_alActions.get(i);
						if(tl_paOld.sID == deleteid){
							getPadMgr().m_alActions.remove(j);
							break;
						}
					}
				}
				this.invalidate();
			}break;
		}
	}

	public void calculateActionsRect(TL_PadAction tl_pa){
		switch(tl_pa.nActionMode){
			case ft_line:{
				PointF ptStart = null,ptstop = null;
				if(tl_pa.alActionPoint.size() == 2){
					ptStart = unRelativePoint(tl_pa.alActionPoint.get(0));
					ptstop = unRelativePoint(tl_pa.alActionPoint.get(1));
				}
				else if(tl_pa.alActionPoint.size() == 1){
					ptStart = unRelativePoint(tl_pa.alActionPoint.get(0));
					ptstop = unRelativePoint(tl_pa.ptSizingEnd);
				}
				if(ptStart==null||ptstop==null)break;

				Path hotPath = new Path();
				getShotlineHotPath(hotPath,ptStart,ptstop);

				tl_pa.HotRegion = new Region();

				Rect rcBK = new Rect((int)m_rcBK.left,(int)m_rcBK.top,(int) m_rcBK.right,(int) m_rcBK.bottom);
				tl_pa.HotRegion.setPath(hotPath, new Region(rcBK ));

				tl_pa.CoverArea = tl_pa.HotRegion.getBounds();
			}break;
			case ft_markerPen:{
				int nSize = tl_pa.alActionPoint.size();
				if(nSize<=2){

					break;
				}
				tl_pa.HotRegion = new Region();
				Rect rcBK = new Rect((int)m_rcBK.left,(int)m_rcBK.top,(int) m_rcBK.right,(int) m_rcBK.bottom);
				for(int j = 0;j < nSize-1;j++){
					PointF ptStart = unRelativePoint(tl_pa.alActionPoint.get(j));
					PointF ptstop = unRelativePoint(tl_pa.alActionPoint.get(j+1));

					Path hotPath = new Path();
					getShotlineHotPath(hotPath,ptStart,ptstop);

					Region part = new Region();
					part.setPath(hotPath, new Region(rcBK ));

					tl_pa.HotRegion.op(part, Op.UNION);
				}
				tl_pa.CoverArea = tl_pa.HotRegion.getBounds();
			}break;
			case ft_Ellipse:{
				PointF ptStart = null,ptstop = null;
				if(tl_pa.alActionPoint.size() == 2){
					ptStart = unRelativePoint(tl_pa.alActionPoint.get(0));
					ptstop = unRelativePoint(tl_pa.alActionPoint.get(1));
				}
				else if(tl_pa.alActionPoint.size() == 1){
					ptStart = unRelativePoint(tl_pa.alActionPoint.get(0));
					ptstop = unRelativePoint(tl_pa.ptSizingEnd);
				}
				if(ptStart==null||ptstop==null)break;


				Path hotPathout = new Path();

				RectF ovalrectout = new RectF();

				ovalrectout.left = Math.min(ptStart.x,ptstop.x) - ActionBorder;
				ovalrectout.top = Math.min(ptStart.y,ptstop.y) - ActionBorder;
				ovalrectout.right = Math.max(ptStart.x,ptstop.x) + ActionBorder;
				ovalrectout.bottom = Math.max(ptStart.y,ptstop.y) + ActionBorder;

				hotPathout.addOval(ovalrectout, Path.Direction.CCW);

				tl_pa.HotRegion = new Region();

				Rect rcBK = new Rect((int)m_rcBK.left,(int)m_rcBK.top,(int) m_rcBK.right,(int) m_rcBK.bottom);
				tl_pa.HotRegion.setPath(hotPathout, new Region(rcBK ));
				//tl_pa.HotRegion.setPath(hotPathout,  new Region(new Rect(0,0,(int) m_rcBK.width(),(int) m_rcBK.height()) ));

				if(!tl_pa.bIsFill){
					if(Math.abs(ptStart.x-ptstop.x)>10&&Math.abs(ptStart.y-ptstop.y)>10){
						Path hotPathin = new Path();

						RectF ovalrectin = new RectF();

						ovalrectin.left = Math.min(ptStart.x,ptstop.x) + ActionBorder;
						ovalrectin.top = Math.min(ptStart.y,ptstop.y) + ActionBorder;
						ovalrectin.right = Math.max(ptStart.x,ptstop.x) - ActionBorder;
						ovalrectin.bottom = Math.max(ptStart.y,ptstop.y) - ActionBorder;

						hotPathin.addOval(ovalrectin, Path.Direction.CCW);
						Region part = new Region();
						//part.setPath(hotPathin, new Region(new Rect(0,0,this.getWidth(),this.getHeight()) ));
						//Rect rcBK = new Rect((int)m_rcBK.left,(int)m_rcBK.top,(int) m_rcBK.right,(int) m_rcBK.bottom);
						part.setPath(hotPathin, new Region(rcBK ));
						tl_pa.HotRegion.op(part, Op.DIFFERENCE);
					}
				}


				tl_pa.CoverArea = tl_pa.HotRegion.getBounds();
			}break;

			case ft_Rectangle:{
				PointF ptStart = null,ptstop = null;
				if(tl_pa.alActionPoint.size() == 2){
					ptStart = unRelativePoint(tl_pa.alActionPoint.get(0));
					ptstop = unRelativePoint(tl_pa.alActionPoint.get(1));
				}
				else if(tl_pa.alActionPoint.size() == 1){
					ptStart = unRelativePoint(tl_pa.alActionPoint.get(0));
					ptstop = unRelativePoint(tl_pa.ptSizingEnd);
				}
				if(ptStart==null||ptstop==null)break;

				Path hotPathout = new Path();

				RectF rectout = new RectF();

				rectout.left = Math.min(ptStart.x,ptstop.x) - ActionBorder;
				rectout.top = Math.min(ptStart.y,ptstop.y) - ActionBorder;
				rectout.right = Math.max(ptStart.x,ptstop.x) + ActionBorder;
				rectout.bottom = Math.max(ptStart.y,ptstop.y) + ActionBorder;

				hotPathout.addRect(rectout, Path.Direction.CCW);

				tl_pa.HotRegion = new Region();
				Rect rcBK = new Rect((int)m_rcBK.left,(int)m_rcBK.top,(int) m_rcBK.right,(int) m_rcBK.bottom);
				tl_pa.HotRegion.setPath(hotPathout, new Region(rcBK ));
				//tl_pa.HotRegion.setPath(hotPathout,  new Region(new Rect(0,0,(int) m_rcBK.width(),(int) m_rcBK.height()) ));

				if(!tl_pa.bIsFill){
					if(Math.abs(ptStart.x-ptstop.x)>10&&Math.abs(ptStart.y-ptstop.y)>10){
						Path hotPathin = new Path();

						RectF rectin = new RectF();

						rectin.left = Math.min(ptStart.x,ptstop.x) + ActionBorder;
						rectin.top = Math.min(ptStart.y,ptstop.y) + ActionBorder;
						rectin.right = Math.max(ptStart.x,ptstop.x) - ActionBorder;
						rectin.bottom = Math.max(ptStart.y,ptstop.y) - ActionBorder;

						hotPathin.addRect(rectin, Path.Direction.CCW);
						Region part = new Region();
						part.setPath(hotPathin,  new Region(rcBK));

						tl_pa.HotRegion.op(part, Op.DIFFERENCE);
					}
				}
				tl_pa.CoverArea = tl_pa.HotRegion.getBounds();
			}break;

			case ft_Text:{
				TextPaint textPaint = new TextPaint();
				textPaint.setColor(tl_pa.nPenColor);
				textPaint.setTextSize(20.0F*dbZoomScale);
				PointF ptStart = unRelativePoint(tl_pa.alActionPoint.get(0));

				int widthMax = 0;
				StaticLayout layout = new StaticLayout(tl_pa.sText,textPaint,(int) (m_rcBK.width() - ptStart.x) ,Alignment.ALIGN_NORMAL,1.0F,0.0F,false);

				for(int i = 0 ; i< layout.getLineCount();i++){//layout.getLineCount()
					int nWidth = (int) layout.getLineWidth(i);
					widthMax = Math.max(nWidth,widthMax);
				}
				Rect rcReg = new Rect();
				rcReg.left = (int) ptStart.x;
				rcReg.top = (int) ptStart.y;
				rcReg.right = rcReg.left + widthMax;
				rcReg.bottom = rcReg.top + layout.getHeight();
				tl_pa.HotRegion = new Region();
				tl_pa.HotRegion.set(rcReg);

				tl_pa.CoverArea = tl_pa.HotRegion.getBounds();
			}break;
			case ft_arrowLine:{
				PointF ptStart = null,ptstop = null;
				if(tl_pa.alActionPoint.size() == 2){
					ptStart = unRelativePoint(tl_pa.alActionPoint.get(0));
					ptstop = unRelativePoint(tl_pa.alActionPoint.get(1));
				}
				else if(tl_pa.alActionPoint.size() == 1){
					ptStart = unRelativePoint(tl_pa.alActionPoint.get(0));
					ptstop = unRelativePoint(tl_pa.ptSizingEnd);
				}
				if(ptStart==null||ptstop==null)break;

				Path hotPath = new Path();
				getShotlineHotPath(hotPath,ptStart,ptstop);

				tl_pa.HotRegion = new Region();
				Rect rcBK = new Rect((int)m_rcBK.left,(int)m_rcBK.top,(int) m_rcBK.right,(int) m_rcBK.bottom);
				tl_pa.HotRegion.setPath(hotPath, new Region(rcBK ));

				tl_pa.CoverArea = tl_pa.HotRegion.getBounds();
			}break;
			default:
				break;
		}
	}
	String strText;
	AlertDialog.Builder builder;

	public void insertText(final float x,final float y){

		m_orgRcBK = new RectF(m_rcBK);

		builder = new AlertDialog.Builder(this.getContext());
		LayoutInflater factory = LayoutInflater.from(this.getContext());
		final View textEntryView = factory.inflate(UZResourcesIDFinder.getResLayoutID("paintpadtextdialog"), null);
		builder.setTitle(this.getContext().getString(UZResourcesIDFinder.getResStringID("insert_text")));
		builder.setView(textEntryView);
		builder.setPositiveButton(this.getContext().getString(UZResourcesIDFinder.getResStringID("sure")), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String strText;
				EditText et = (EditText)textEntryView.findViewById(UZResourcesIDFinder.getResIdID("editText_name"));
				strText = et.getText().toString();
				if(strText.equals("") || strText == null){
					Toast.makeText(getContext(), UZResourcesIDFinder.getResStringID("entry_content"), Toast.LENGTH_LONG).show();
				}
				et.setTextSize(60);
				onInsertText(strText,x,y);
			}
		});
		builder.setNegativeButton(this.getContext().getString(UZResourcesIDFinder.getResStringID("cancel")), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		//		builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
		//
		//			@Override
		//			public void onDismiss(DialogInterface arg0) {
		//
		//			}
		//		});
		builder.create().show();
	}

	public void onInsertText(String strtext,float x,float y){
		//		TL_PadAction tl_PadAction = new TL_PadAction();
		m_tl_CurrentPadAction = new TL_PadAction();
		GenerateNewActionID(m_tl_CurrentPadAction)	;
		m_tl_CurrentPadAction.nActionMode = factoryType.ft_Text;
		m_tl_CurrentPadAction.nPenWidth = m_nPenWidth;
		m_tl_CurrentPadAction.nPenColor = m_nPenColor;
		m_tl_CurrentPadAction.bIsFill = m_bActionfill;
		m_tl_CurrentPadAction.alActionPoint = new ArrayList<PointF>();
		m_tl_CurrentPadAction.alActionPoint.add(relativePoint(new PointF(x,y)));
		m_tl_CurrentPadAction.sText = strtext;
		calculateActionsRect(m_tl_CurrentPadAction);
		getPadMgr().m_alActions.add(m_tl_CurrentPadAction);
		if(m_iSync!= null) m_iSync.SendActions(WhitePadInterface.ADD_ACTION, m_tl_CurrentPadAction);
		this.invalidate();
	}

	public void tranlatePadAction(TL_PadAction tl_pa,float x,float y){
		if(tl_pa.alActionPoint!=null){
			for(int i = 0 ; i < tl_pa.alActionPoint.size() ; i++){
				PointF ptf = unRelativePoint(tl_pa.alActionPoint.get(i));
				ptf.x += x;
				ptf.y += y;
				PointF rel = relativePoint(ptf);
				tl_pa.alActionPoint.get(i).x = rel.x;
				tl_pa.alActionPoint.get(i).y = rel.y;
			}
		}
		calculateActionsRect(tl_pa);
	}


	public void getShotlineHotPath( Path path, PointF aF,PointF bF){

		float hotWidth = ActionBorder ;

		PointF a,b;
		if(aF.x>=bF.x){
			a = bF;
			b = aF;
		}
		else{
			a = aF;
			b = bF;
		}

		float fLine = (float) Math.sqrt((a.x - b.x)*(a.x - b.x) + (a.y - b.y)*(a.y - b.y));
		float widthoff = 0;
		float heightoff = 0;
		if(fLine != 0){
			widthoff = hotWidth*Math.abs(a.y - b.y)/fLine;
			heightoff = hotWidth*Math.abs(a.x - b.x)/fLine;
		}

		PointF aX = new PointF(),aY = new PointF(),bX = new PointF(),bY = new PointF();

		if(a.y<=b.y){
			aX.x = a.x - widthoff;
			aY.x = a.x + widthoff;

			bX.x = b.x - widthoff;
			bY.x = b.x + widthoff;
		}
		else{
			aX.x = a.x + widthoff;
			aY.x = a.x - widthoff;

			bX.x = b.x + widthoff;
			bY.x = b.x - widthoff;
		}


		if(a.x<=b.x){
			aX.y = a.y + heightoff;
			aY.y = a.y - heightoff;

			bX.y = b.y + heightoff;
			bY.y = b.y - heightoff;
		}
		else{
			aX.y = a.y + heightoff;
			aY.y = a.y - heightoff;

			bX.y = b.y + heightoff;
			bY.y = b.y - heightoff;
		}
		path.moveTo(aX.x, aX.y);
		path.lineTo(aY.x, aY.y);
		path.lineTo(bY.x, bY.y);
		path.lineTo(bX.x, bX.y);
		path.close();
	}
	////////////////////////////////////////
	//
	//
	//////////////////////////////////////////////////////////////

	public void getArrowPath(Path path, PointF aF,PointF bF){
		final float normalarrowsize = 15*dbZoomScale;
		float fLine = (float) Math.sqrt((aF.x - bF.x)*(aF.x - bF.x) + (aF.y - bF.y)*(aF.y - bF.y));

		float realarrowSize = normalarrowsize;

		if(15*6>fLine) realarrowSize = (fLine/6);


		float widthoff = 0;
		float heightoff = 0;;
		if(fLine != 0){
			widthoff = (realarrowSize/2)*Math.abs(aF.y - bF.y)/(fLine-realarrowSize);
			heightoff =( realarrowSize/2)*Math.abs(aF.x - bF.x)/(fLine-realarrowSize);
		}
		else return;
		PointF ptSend = new PointF();
		PointF pthmF = new PointF();
		PointF ptmF = new PointF();
		PointF pthnF = new PointF();
		PointF ptnF = new PointF();

		ptSend.x = bF.x - (bF.x - aF.x)*(realarrowSize/fLine);
		ptSend.y = bF.y - (bF.y - aF.y)*(realarrowSize/fLine);

		if(aF.x<bF.x){
			if(aF.y<bF.y){
				ptmF.x = ptSend.x + widthoff;
				ptmF.y = ptSend.y - heightoff;
				ptnF.x = ptSend.x - widthoff;
				ptnF.y = ptSend.y + heightoff;
			}else{
				ptmF.x = ptSend.x - widthoff;
				ptmF.y = ptSend.y - heightoff;
				ptnF.x = ptSend.x + widthoff;
				ptnF.y = ptSend.y + heightoff;
			}
		}
		else{
			if(aF.y<bF.y){
				ptmF.x = ptSend.x + widthoff;
				ptmF.y = ptSend.y + heightoff;
				ptnF.x = ptSend.x - widthoff;
				ptnF.y = ptSend.y - heightoff;
			}else{
				ptmF.x = ptSend.x - widthoff;
				ptmF.y = ptSend.y + heightoff;
				ptnF.x = ptSend.x + widthoff;
				ptnF.y = ptSend.y - heightoff;
			}
		}
		pthmF.x = (ptmF.x +  ptSend.x)/2;
		pthmF.y = (ptmF.y +  ptSend.y)/2;
		pthnF.x = (ptnF.x +  ptSend.x)/2;
		pthnF.y = (ptnF.y +  ptSend.y)/2;

		path.moveTo(aF.x, aF.y);
		path.lineTo(pthmF.x, pthmF.y);
		path.lineTo(ptmF.x, ptmF.y);
		path.lineTo(bF.x, bF.y);
		path.lineTo(ptnF.x, ptnF.y);
		path.lineTo(pthnF.x, pthnF.y);
		path.close();

	}



	public void OnDelete(TL_PadAction tl_pa){
		if(bSelectMode){
			//ArrayList<String> alDeleteActs = new ArrayList<String>();

			//DeleteSelectedAction(alDeleteActs);
			DeleteSelectedAction(tl_pa);
			if(m_iSync != null) m_iSync.SendActions(WhitePadInterface.DELETE_ACTION, tl_pa);

			this.invalidate();
		}
	}

	private void DeleteSelectedAction(TL_PadAction tl_pa){

		for(int i = 0; i < getPadMgr().m_alActions.size(); i++){
			TL_PadAction temp_tl_pa = getPadMgr().m_alActions.get(i);
			if(temp_tl_pa.sID.compareTo(tl_pa.sID)==0)
			{
				getPadMgr().m_alActions.remove(i);
				break;
			}
		}
	}

	public void RelativeAction(TL_PadAction tl_pa){
		if(!tl_pa.bIsRelative){
			float fPadwidth = this.getWidth();
			float fPadheight = this.getHeight();

			for(int i = 0 ; i<tl_pa.alActionPoint.size() ;i++){
				PointF ptF = tl_pa.alActionPoint.get(i);
				ptF.x = ptF.x*fPadwidth;
				ptF.y = ptF.y*fPadheight;
			}

			tl_pa.bIsRelative = true;
		}
	}

	public void UnRelativeAction(TL_PadAction tl_pa){
		if(tl_pa.bIsRelative){
			float fPadwidth = this.getWidth();
			float fPadheight = this.getHeight();

			for(int i = 0 ; i<tl_pa.alActionPoint.size() ;i++){
				PointF ptF = tl_pa.alActionPoint.get(i);
				ptF.x = ptF.x/fPadwidth;
				ptF.y = ptF.y/fPadheight;
			}
			tl_pa.bIsRelative = false;
		}
	}

	public PointF relativePoint(PointF point)
	{
		PointF real = new PointF();
		if(this.m_orgRcBK!=null)
		{
			real.x = (point.x  - m_orgRcBK.left)/m_orgRcBK.width();
			real.y = (point.y  - m_orgRcBK.top)/m_orgRcBK.height();
			m_orgRcBK = null;
		}
		else
		{
			real.x = (point.x  - m_rcBK.left)/m_rcBK.width();
			real.y = (point.y  - m_rcBK.top)/m_rcBK.height();
		}
		return real;
	}

	public PointF unRelativePoint(PointF point){


		PointF real = new PointF();
		if(point!=null)
		{
			real.x = m_rcBK.left + m_rcBK.width()*point.x;//(0.0,168.0,480.0,648.0)
			real.y = m_rcBK.top + m_rcBK.height()*point.y;//(0,168)
		}
		return real;
	}


	public FaceShareControl getSyncInterface() {
		return m_iSync;
	}

	public void setSyncInterface(FaceShareControl iSync) {
		this.m_iSync = iSync;
	}
	public void GenerateNewActionID(TL_PadAction tl_pa)	{
		tl_pa.sID =  m_tl_CurrentPadAction.hashCode()+"";//System.currentTimeMillis() + tl_pa.hashCode();
		tl_pa.nDocID = getPadMgr().mCurrentShareDoc.docID;
		tl_pa.nPageID = getPadMgr().mCurrentShareDoc.currentPage;
	}

	public void setZoomMode(boolean bZoom){
		bZoomMode = bZoom;
		if(bZoomMode){
			SetSelectMode(false);
			//			setDeleteMode(false);
		}else{
			for(int i = 0; i < getPadMgr().m_alActions.size(); i++){
				TL_PadAction tl_pa = getPadMgr().m_alActions.get(i);
				calculateActionsRect(tl_pa);
			}
		}
	}

	public Path getMarkPenPath(TL_PadAction tl_pa){
		if(tl_pa.nActionMode == TL_PadAction.factoryType.ft_markerPen){
			Path lines = new Path();
			for(int i = 0 ; i < tl_pa.alActionPoint.size() ; i++){
				PointF ptf = unRelativePoint(tl_pa.alActionPoint.get(i));

				if(ptf.y < m_rcBK.top ){
					ptf.y = m_rcBK.top;
				}else if(ptf.y > m_rcBK.bottom){
					ptf.y = m_rcBK.bottom;
				}
				//xiaoyang add 修改白板横向可以画出白色区域的bug
				if(ptf.x < m_rcBK.left){
					ptf.x = m_rcBK.left;
				}else if(ptf.x > m_rcBK.right){
					ptf.x = m_rcBK.right;
				}
				if(i == 0){
					lines.moveTo(ptf.x, ptf.y);
				}else{
					lines.lineTo(ptf.x, ptf.y);
				}
			}
			return lines;
		}
		return null;
	}

	public boolean needHandleMoveMent(MotionEvent me){

		if(me!=null && MotionEvent.ACTION_MOVE == me.getAction()){
			if(!bZoomMode)return false;


			if(me.getPointerCount() == 1)
			{
				PointF currentMovePoint = new PointF(me.getX(),me.getY());

				RectF rcNewBK = new RectF(m_rcBK);
				rcNewBK.offset(currentMovePoint.x - mfMovePoint.x, currentMovePoint.y - mfMovePoint.y);

				if(rcNewBK.left>0)
					return false;
				else
					return true;
			}else if(me.getPointerCount() > 1){
				return true;
			}
		}
		return false;
	}

	public void OnConfigChanged(){
		Log.i("Screen", "Width"+this.getWidth());
		Log.i("Screen", "Height"+this.getHeight());
	}

	public abstract static interface PaintPadClickListener{
		public abstract void OnClick(View arg0);

		public abstract void OnCleand();

		public abstract void OnReceived();

		public abstract void OnSelectColor(int penColor);
	}

	public PaintPadClickListener m_ppcl = null;

	public void setOnPaintPadClickListener(PaintPadClickListener ppcl){
		m_ppcl = ppcl;
	}

	private void doubleclick(PointF point) {

		float ZoomScale =4;

		RectF rcBK = new RectF(m_rcBK);
		if(rcBK.width()/ m_rcOriginBK.width() >1.1){
			rcBK.right = m_rcOriginBK.right;
			rcBK.top = m_rcOriginBK.top;
			rcBK.left = m_rcOriginBK.left ;
			rcBK.bottom = m_rcOriginBK.bottom;
		}
		else{
			if(rcBK.width()*ZoomScale /m_rcOriginBK.width() >=4.0){
				ZoomScale = (float) ((m_rcOriginBK.width()*4.0)/rcBK.width());
			}
			float fx = (point.x-rcBK.left)/rcBK.width();
			float fy= (point.y-rcBK.top)/rcBK.height();

			rcBK.right *= ZoomScale;
			rcBK.top *= ZoomScale;
			rcBK.left *= ZoomScale;
			rcBK.bottom *= ZoomScale;



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

		}).start();
	}
	public void UIThreadZoomto( final RectF rcbk){
		m_rcBK.left = rcbk.left;
		m_rcBK.right = rcbk.right;
		m_rcBK.top = rcbk.top;
		m_rcBK.bottom = rcbk.bottom;

		dbZoomScale = m_rcBK.width()/m_rcOriginBK.width();
		new Handler(getPadMgr().mAppContext.getMainLooper()).post(new Runnable(){

			@Override
			public void run() {
				invalidate();
			}
		});
	}

	@Override
	public void onChange()
	{
		FileLog.e("emm", "onchange");
		this.invalidate();
	}
	@Override
	public void onAttachedToWindow(){
		getPadMgr().addOnDataChangeListener(this);
		super.onAttachedToWindow();
	}
	@Override
	public void onDetachedFromWindow(){
		getPadMgr().removeOnDataChangeListener(this);
		super.onDetachedFromWindow();
	}

	public SharePadMgr getPadMgr() {
		return m_thisPadMgr;
	}

	public void setPadMgr(SharePadMgr m_thisPadMgr) {
		this.m_thisPadMgr = m_thisPadMgr;
	}

	View bottomView;
	TextView penWidthTv;
	SeekBar seekBar;

	public void getColor(){
		View view = LayoutInflater.from(this.getContext()).inflate(UZResourcesIDFinder.getResLayoutID("layout_text"), null,false);
		popupWindow = new PopupWindow(view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.setOutsideTouchable(true);
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		popupWindow.showAtLocation(view, Gravity.BOTTOM, location[0],location[1]-popupWindow.getHeight());
		//		popupWindow.setFocusable(true);
		//		popupWindow.showAsDropDown(button1)
		GridView gridView =(GridView) view.findViewById(UZResourcesIDFinder.getResIdID("gridview"));

		seekBar = (SeekBar) view.findViewById(UZResourcesIDFinder.getResIdID("seekbar"));
		penWidthTv = (TextView) view.findViewById(UZResourcesIDFinder.getResIdID("line_width_tv"));
		seekBar.setOnSeekBarChangeListener(new seekBarListener());
		seekBar.setProgress(m_nPenWidth);
		adapter = new ColorAdapter(getContext());
		gridView.setAdapter(adapter);
		view.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if(popupWindow != null && popupWindow.isShowing()){
					popupWindow.dismiss();
					popupWindow = null;
				}
				return false;
			}
		});
	}

	class ColorAdapter extends BaseAdapter{
		private Context mContext;

		public ColorAdapter(Context mContext){
			this.mContext=mContext;
		}

		@Override
		public int getCount() {
			return img.length;
		}

		@Override
		public Object getItem(int arg0) {
			return img[arg0];
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if (convertView == null) {
				imageView = new ImageView(mContext);
				imageView.setLayoutParams(new GridView.LayoutParams(60, 60));
				imageView.setAdjustViewBounds(false);
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(8, 8, 8, 8);
			}else {
				imageView = (ImageView) convertView;
			}
			imageView.setImageResource(img[position]);
			imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if(position == 0){
						m_nPenColor = 0xffff0000;
					}else if(position == 1){
						m_nPenColor = 0xff871f78;
					}else if(position == 2){
						m_nPenColor = 0xff00ff00;
					}else if(position == 3){
						m_nPenColor = 0xffff9300;
					}else if(position == 4){
						m_nPenColor = 0xff2ab2f1;
					}else if(position == 5){
						m_nPenColor = 0xfffff100;
					}else if(position == 6){
						m_nPenColor = 0xffff00bb;
					}else if(position == 7){
						m_nPenColor = 0xff000d90;
					}else{
						m_nPenColor =  0xff2f4f4f;
					}
					if(popupWindow != null && popupWindow.isShowing()){
						popupWindow.dismiss();
						popupWindow = null;
					}
					if(m_ppcl!=null)
						m_ppcl.OnSelectColor(m_nPenColor);
				}
			});
			return imageView;
		}
	}

	class seekBarListener implements OnSeekBarChangeListener{

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			penWidthTv.setText(getResources().getString(UZResourcesIDFinder.getResStringID("line_width"))+"("+seekBar.getProgress()+")");
			m_nPenWidth = seekBar.getProgress();
			seekBar.setProgress(m_nPenWidth);
		}
		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			penWidthTv.setText(getResources().getString(UZResourcesIDFinder.getResStringID("line_width"))+"("+seekBar.getProgress()+")");
			m_nPenWidth = seekBar.getProgress();
			seekBar.setProgress(m_nPenWidth);
		}

		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			penWidthTv.setText(getResources().getString(UZResourcesIDFinder.getResStringID("line_width"))+"("+seekBar.getProgress()+")");
			if(seekBar.getProgress()==0)
				seekBar.setProgress(1);
			m_nPenWidth = seekBar.getProgress();
			seekBar.setProgress(m_nPenWidth);
		}
	}

	public void Delete(){
		ArrayList<TL_PadAction> listPadAction= getPadMgr().m_alActions;

		for(int i = 0; i < getPadMgr().m_alActions.size();i++){
			TL_PadAction temp_tl_pa = getPadMgr().m_alActions.get(i);
			if(getPadMgr().mCurrentShareDoc.docID==temp_tl_pa.nDocID &&
					getPadMgr().mCurrentShareDoc.currentPage == temp_tl_pa.nPageID){

				listPadAction.remove(i);
				i --;
				invalidate();
				if(m_iSync!= null)
					m_iSync.SendActions(WhitePadInterface.DELETE_ACTION, temp_tl_pa);
			}
		}
		//		if(gd.size() > 0){  
		//			for(TL_PadAction t : listPadAction){  
		//			}  
		//		} else{  
		//		}  
	}
}



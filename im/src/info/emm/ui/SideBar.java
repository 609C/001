package info.emm.ui;

import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class SideBar extends View {
	// 锟斤拷锟斤拷锟铰硷拷
	private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
	//"锟斤拷", "锟斤拷",
	public static String[] b = { "A", "B", "C", "D", "E", "F", "G", "H", "I",
			"J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
			"W", "X", "Y", "Z", "#" };
	private int choose = -1;
	private Paint paint = new Paint();

	private TextView mTextDialog;
	private boolean isRest = false;

	public void setTextView(TextView mTextDialog) {
		isRest = false;
		this.mTextDialog = mTextDialog;
	}


	public SideBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SideBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SideBar(Context context) {
		super(context);
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 锟斤拷取锟斤拷锟斤拷谋浔筹拷锟斤拷锟缴�.
		int height = getHeight();// 锟斤拷取锟斤拷应锟竭讹拷
		int width = getWidth();// 锟斤拷取锟斤拷应锟斤拷锟�
		int singleHeight = height / b.length;// 锟斤拷取每一锟斤拷锟斤拷母锟侥高讹拷

		for (int i = 0; i < b.length; i++) {
			paint.setColor(getResources().getColor(R.color.sidebar_color));//Color.rgb(33, 65, 98)
			paint.setTypeface(Typeface.DEFAULT);
			paint.setAntiAlias(true);
			paint.setTextSize(Utilities.dp(12));
			// 选锟叫碉拷状态
			if (i == choose) {
				paint.setColor(Color.parseColor("#3399ff"));
				paint.setFakeBoldText(true);
			}
			// x锟斤拷锟斤拷锟斤拷锟斤拷屑锟�-锟街凤拷锟斤拷锟斤拷鹊锟揭伙拷锟�.
			float xPos = width / 2 - paint.measureText(b[i]) / 2;
			float yPos = singleHeight * i + singleHeight;
			canvas.drawText(b[i], xPos, yPos, paint);
			paint.reset();// 锟斤拷锟矫伙拷锟斤拷
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final float y = event.getY();// 锟斤拷锟統锟斤拷锟斤拷
		final int oldChoose = choose;
		final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
		final int c = (int) (y / getHeight() * b.length);// 锟斤拷锟統锟斤拷锟斤拷锟斤拷占锟杰高度的憋拷锟斤拷*b锟斤拷锟斤拷某锟斤拷染偷锟斤拷诘锟斤拷b锟叫的革拷锟斤拷.
		switch (action) {
			case MotionEvent.ACTION_UP:
				setBackgroundDrawable(new ColorDrawable(0x00000000));
				choose = -1;//
				invalidate();
				if (mTextDialog != null) {
					mTextDialog.setVisibility(View.INVISIBLE);
				}
				break;
				
			case MotionEvent.ACTION_MOVE:
				 getParent().requestDisallowInterceptTouchEvent(true);
//				 choose = -1;//
//					invalidate();
//					if (mTextDialog != null) {
//						mTextDialog.setVisibility(View.INVISIBLE);
//					}
				break;
	
			default:
				if (!isRest) {
					setBackgroundResource(R.drawable.sidebar_background);
					if (oldChoose != c) {
						if (c >= 0 && c < b.length) {
							if (listener != null) {
								listener.onTouchingLetterChanged(b[c]);
							}
							if (mTextDialog != null) {
								mTextDialog.setText(b[c]);
								mTextDialog.setVisibility(View.VISIBLE);
							}
							choose = c;
							invalidate();
						}
					}
				}
				break;
		}
		return true;
	}

	public void setOnTouchingLetterChangedListener(OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
		this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
	}

	public interface OnTouchingLetterChangedListener {
		public void onTouchingLetterChanged(String s);
	}
	
	public void setReset(){
		isRest = true;
		setBackgroundDrawable(new ColorDrawable(0x00000000));
		choose = -1;
		invalidate();
		if (mTextDialog != null) {
			mTextDialog.setVisibility(View.INVISIBLE);
		}
	}
	
	public boolean getDiaLogStatus(){
		int status = mTextDialog.getVisibility();
		switch (status) {
			case View.GONE:
				return false;
			case View.INVISIBLE:
				return false;
			case View.VISIBLE:
				return true;
			default:
				return false;
		}
	}
	
	public boolean getBooleanRest(){
		return isRest;
	}
	
	public void setBooleanRest(boolean value){
		isRest = value;
	}
}
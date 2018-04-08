package com.meeting.ui;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.animation.RotateAnimation;

public class RefreshableView extends LinearLayout implements OnTouchListener {

	/**
	 *
	 */
	public static final int STATUS_PULL_TO_REFRESH = 0;

	/**
	 * 
	 */
	public static final int STATUS_RELEASE_TO_REFRESH = 1;

	/**
	 *
	 */
	public static final int STATUS_REFRESHING = 2;

	/**
	 * 
	 */
	public static final int STATUS_REFRESH_FINISHED = 3;

	/**
	 * 
	 */
	public static final int SCROLL_SPEED = -20;

	/**
	 *
	 */
	public static final long ONE_MINUTE = 60 * 1000;

	/**
	 * 
	 */
	public static final long ONE_HOUR = 60 * ONE_MINUTE;

	/**
	 * 
	 */
	public static final long ONE_DAY = 24 * ONE_HOUR;

	/**
	 * 
	 */
	public static final long ONE_MONTH = 30 * ONE_DAY;

	/**
	 * 
	 */
	public static final long ONE_YEAR = 12 * ONE_MONTH;

	/**
	 *
	 */
	private static final String UPDATED_AT = "updated_at";

	/**
	 * 
	 */
	private PullToRefreshListener mListener;

	/**
	 * 
	 */
	private SharedPreferences preferences;

	/**
	 * 
	 */
	private View header;

	/**
	 * 
	 */
	private ListView listView;

	/**
	 * 
	 */
	private ProgressBar progressBar;

	/**
	 * 
	 */
	private ImageView arrow;

	/**
	 * 
	 */
	private TextView description;

	/**
	 * 
	 */
	private TextView updateAt;

	/**
	 *
	 */
	private LinearLayout.LayoutParams headerLayoutParams;

	/**
	 * 
	 */
	private long lastUpdateTime;

	/**
	 * 
	 */
	private int mId = -1;

	/**
	 * 
	 */
	private int hideHeaderHeight;

	/**
	 * 
	 * 
	 */
	private int currentStatus = STATUS_REFRESH_FINISHED;;

	/**
	 * 
	 */
	private int lastStatus = currentStatus;

	/**
	 *
	 */
	private float yDown;

	/**
	 * 
	 */
	private int touchSlop;

	/**
	 *
	 */
	private boolean loadOnce;

	/**
	 *
	 */
	boolean ableToPull;

	/**
	 *
	 * 
	 * @param context
	 * @param attrs
	 */
	public RefreshableView(Context context, AttributeSet attrs) {
		super(context, attrs);
		UZResourcesIDFinder.init(context.getApplicationContext());
		
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		header = LayoutInflater.from(context).inflate(UZResourcesIDFinder.getResLayoutID("pull_to_refresh"), null, true);
		progressBar = (ProgressBar) header.findViewById(UZResourcesIDFinder.getResIdID("progress_bar"));
		arrow = (ImageView) header.findViewById(UZResourcesIDFinder.getResIdID("arrow"));
		description = (TextView) header.findViewById(UZResourcesIDFinder.getResIdID("description"));
		updateAt = (TextView) header.findViewById(UZResourcesIDFinder.getResIdID("updated_at"));
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

		refreshUpdatedAtValue();
		setOrientation(VERTICAL);
		addView(header, 0);
		//header.setVisibility(View.GONE);
	}

	/**
	 * 
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed && !loadOnce) {
			hideHeaderHeight = -header.getHeight();
			headerLayoutParams = (LayoutParams) header.getLayoutParams();
			headerLayoutParams.topMargin = hideHeaderHeight;
			header.setLayoutParams(headerLayoutParams);
			listView = (ListView) getChildAt(1);
			listView.setOnTouchListener(this);
			loadOnce = true;
			//finishRefreshing();

			new RefreshingTask().execute();
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		Log.i("TAG", "Refresh....dispatchTouchEvent");
		return super.dispatchTouchEvent(ev);
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		Log.i("TAG", "Refresh....onInterceptTouchEvent");
		return super.onInterceptTouchEvent(ev);
	}
	/**
	 * 
	 */
	public static boolean result = false,state = false;
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		setIsAbleToPull(event);
		if (ableToPull) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				result = false;
				state = false;
				Log.i("TAG", "Refresh....1");
				yDown = event.getRawY();
				break;
			case MotionEvent.ACTION_MOVE:
				if(!state){
					result = true;
					float yMove = event.getRawY();
					int distance = (int) (yMove - yDown);

					if (distance <= 0 && headerLayoutParams.topMargin <= hideHeaderHeight) {
						result = false;
						Log.i("TAG", "Refresh....2");
						return false;
					}
					if (distance < touchSlop) {
						result = false;
						Log.i("TAG", "Refresh....3");
						return false;
					}
					if (currentStatus != STATUS_REFRESHING) {
						if (headerLayoutParams.topMargin > 0) {
							currentStatus = STATUS_RELEASE_TO_REFRESH;
							Log.i("TAG", "Refresh....4");
						} else {
							currentStatus = STATUS_PULL_TO_REFRESH;
							Log.i("TAG", "Refresh....5");
						}

						headerLayoutParams.topMargin = (distance / 2) + hideHeaderHeight;
						header.setLayoutParams(headerLayoutParams);
						//	if(header.getVisibility()!=View.VISIBLE)
						//	header.setVisibility(View.VISIBLE);
					}
				}
				break;
			case MotionEvent.ACTION_UP:
			default:
				if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
					Log.i("TAG", "Refresh....6");
					new RefreshingTask().execute();
				} else if (currentStatus == STATUS_PULL_TO_REFRESH) {
					Log.i("TAG", "Refresh....7");
					new HideHeaderTask().execute();
				}
				break;
			}
			//
			if (currentStatus == STATUS_PULL_TO_REFRESH
					|| currentStatus == STATUS_RELEASE_TO_REFRESH) {
				updateHeaderView();
				Log.i("TAG", "Refresh....8");
				listView.setPressed(false);
				listView.setFocusable(false);
				listView.setFocusableInTouchMode(false);
				lastStatus = currentStatus;
				return true;
			}
		}
		Log.i("TAG", "Refresh....9");
		return false;
	}

	/**
	 * 
	 * 
	 * @param listener
	 *            
	 * @param id
	 *            
	 */
	public void setOnRefreshListener(PullToRefreshListener listener, int id) {
		mListener = listener;
		mId = id;
	}

	/**
	 * 
	 */
	public void finishRefreshing() {
		currentStatus = STATUS_REFRESH_FINISHED;
		preferences.edit().putLong(UPDATED_AT + mId, System.currentTimeMillis()).commit();
		new HideHeaderTask().execute();
	}
	public boolean isRefreshing(){
		return currentStatus == STATUS_REFRESHING;
	}
	public boolean isCatched(){
		return currentStatus != STATUS_REFRESH_FINISHED;
	}
	/**
	 * 
	 * 
	 * 
	 * @param event
	 */
	private void setIsAbleToPull(MotionEvent event) {
		View firstChild = listView.getChildAt(0);
		if (firstChild != null) {
			int firstVisiblePos = listView.getFirstVisiblePosition();
			if (firstVisiblePos == 0 && firstChild.getTop() == 0) {
				if (!ableToPull) {
					yDown = event.getRawY();
				}
				// 
				ableToPull = true;
			} else {
				if (headerLayoutParams.topMargin != hideHeaderHeight) {
					headerLayoutParams.topMargin = hideHeaderHeight;
					header.setLayoutParams(headerLayoutParams);
					//if(header.getVisibility()!=View.VISIBLE)
					//	header.setVisibility(View.VISIBLE);
				}
				ableToPull = false;
			}
		} else {
			// 
			ableToPull = true;
		}
	}

	/**
	 * ��������ͷ�е���Ϣ��
	 */
	private void updateHeaderView() {
		if (lastStatus != currentStatus) {
			if (currentStatus == STATUS_PULL_TO_REFRESH) {
				description.setText(getResources().getString(UZResourcesIDFinder.getResStringID("pull_to_refresh")));
				arrow.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				rotateArrow();
			} else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
				description.setText(getResources().getString(UZResourcesIDFinder.getResStringID("release_to_refresh")));
				arrow.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				rotateArrow();
			} else if (currentStatus == STATUS_REFRESHING) {
				description.setText(getResources().getString(UZResourcesIDFinder.getResStringID("refreshing")));
				progressBar.setVisibility(View.VISIBLE);
				arrow.clearAnimation();
				arrow.setVisibility(View.GONE);
			}
			refreshUpdatedAtValue();
		}
	}

	/**
	 * 
	 */
	private void rotateArrow() {
		float pivotX = arrow.getWidth() / 2f;
		float pivotY = arrow.getHeight() / 2f;
		float fromDegrees = 0f;
		float toDegrees = 0f;
		if (currentStatus == STATUS_PULL_TO_REFRESH) {
			fromDegrees = 180f;
			toDegrees = 360f;
		} else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
			fromDegrees = 0f;
			toDegrees = 180f;
		}
		RotateAnimation animation = new RotateAnimation(fromDegrees, toDegrees, pivotX, pivotY);
		animation.setDuration(100);
		animation.setFillAfter(true);
		arrow.startAnimation(animation);
	}

	/**
	 * 
	 */
	private void refreshUpdatedAtValue() {
		if( this.isInEditMode())return;
		lastUpdateTime = preferences.getLong(UPDATED_AT + mId, -1);
		long currentTime = System.currentTimeMillis();
		long timePassed = currentTime - lastUpdateTime;
		long timeIntoFormat;
		String updateAtValue;
		if (lastUpdateTime == -1) {
			updateAtValue = getResources().getString(UZResourcesIDFinder.getResStringID("not_updated_yet"));
		} else if (timePassed < 0) {
			updateAtValue = getResources().getString(UZResourcesIDFinder.getResStringID("time_error"));
		} else if (timePassed < ONE_MINUTE) {
			updateAtValue = getResources().getString(UZResourcesIDFinder.getResStringID("updated_just_now"));
		} else if (timePassed < ONE_HOUR) {
			timeIntoFormat = timePassed / ONE_MINUTE;
			String value = timeIntoFormat + getResources().getString(UZResourcesIDFinder.getResStringID("minute"));
			updateAtValue = String.format(getResources().getString(UZResourcesIDFinder.getResStringID("updated_at")), value);
		} else if (timePassed < ONE_DAY) {
			timeIntoFormat = timePassed / ONE_HOUR;
			String value = timeIntoFormat + getResources().getString(UZResourcesIDFinder.getResStringID("hour"));
			updateAtValue = String.format(getResources().getString(UZResourcesIDFinder.getResStringID("updated_at")), value);
		} else if (timePassed < ONE_MONTH) {
			timeIntoFormat = timePassed / ONE_DAY;
			String value = timeIntoFormat + getResources().getString(UZResourcesIDFinder.getResStringID("day"));
			updateAtValue = String.format(getResources().getString(UZResourcesIDFinder.getResStringID("updated_at")), value);
		} else if (timePassed < ONE_YEAR) {
			timeIntoFormat = timePassed / ONE_MONTH;
			String value = timeIntoFormat + getResources().getString(UZResourcesIDFinder.getResStringID("month"));
			updateAtValue = String.format(getResources().getString(UZResourcesIDFinder.getResStringID("updated_at")), value);
		} else {
			timeIntoFormat = timePassed / ONE_YEAR;
			String value = timeIntoFormat + getResources().getString(UZResourcesIDFinder.getResStringID("year"));
			updateAtValue = String.format(getResources().getString(UZResourcesIDFinder.getResStringID("updated_at")), value);
		}
		updateAt.setText(updateAtValue);
	}

	/**
	 * 
	 * 
	 * @author guolin
	 */
	class RefreshingTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			int topMargin = headerLayoutParams.topMargin;
			while (true) {
				topMargin = topMargin + SCROLL_SPEED;
				if (topMargin <= 0) {
					topMargin = 0;
					break;
				}
				publishProgress(topMargin);
				sleep(10);
			}
			currentStatus = STATUS_REFRESHING;
			publishProgress(0);
			if (mListener != null) {
				mListener.onRefresh();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... topMargin) {
			updateHeaderView();
			headerLayoutParams.topMargin = topMargin[0];
			header.setLayoutParams(headerLayoutParams);
			//if(header.getVisibility()!=View.VISIBLE)
			//	header.setVisibility(View.VISIBLE);
		}

	}

	/**
	 * 
	 * 
	 * @author guolin
	 */
	class HideHeaderTask extends AsyncTask<Void, Integer, Integer> {

		@Override
		protected Integer doInBackground(Void... params) 
		{

			if(headerLayoutParams==null)
				return 0;
			int topMargin = headerLayoutParams.topMargin;
			while (true) {
				topMargin = topMargin + SCROLL_SPEED;
				if (topMargin <= hideHeaderHeight) {
					topMargin = hideHeaderHeight;
					break;
				}
				publishProgress(topMargin);
				sleep(10);
			}
			return topMargin;
		}

		@Override
		protected void onProgressUpdate(Integer... topMargin) {
			headerLayoutParams.topMargin = topMargin[0];
			header.setLayoutParams(headerLayoutParams);
			//	if(header.getVisibility()!=View.VISIBLE)
			//		header.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(Integer topMargin) {
			headerLayoutParams.topMargin = topMargin;
			header.setLayoutParams(headerLayoutParams);
			currentStatus = STATUS_REFRESH_FINISHED;
			//	if(header.getVisibility()!=View.VISIBLE)
			//	header.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 
	 * 
	 * @param time
	 *            
	 */
	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * 
	 * @author guolin
	 */
	public interface PullToRefreshListener {

		/**
		 * 
		 */
		void onRefresh();

	}


}
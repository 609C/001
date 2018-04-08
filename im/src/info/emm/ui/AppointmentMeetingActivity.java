package info.emm.ui;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import info.emm.LocalData.DateUnit;
import info.emm.ui.Views.BaseFragment;
import info.emm.yuanchengcloudb.R;

/**
 * @ClassName: AppointmentMeetingActivity
 *
 * @Description: ԤԼ���� ���ʱ�����
 *
 * @Author: He,Zhen hezhen@yunboxin.com
 *
 * @Date: 2014-11-12
 *
 */
public class AppointmentMeetingActivity extends BaseFragment implements OnClickListener{

	private TextView tvTime;

	public AppointmentMeetingActivity() {

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
		((CreateNewGroupActivity) parentActivity).showActionBar();
		((CreateNewGroupActivity) parentActivity).updateActionBar();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (fragmentView == null) {
			fragmentView = inflater.inflate(R.layout.appintmentmeeting, container, false);
			tvTime = (TextView)fragmentView.findViewById(R.id.tv_timeshow);
			
			String currentTime = DateUnit.getStringDate(new Date());
			tvTime.setText(currentTime);
			
            tvTime.setOnClickListener(this);
            fragmentView.findViewById(R.id.tv_confirm).setOnClickListener(this);
		} else {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		return fragmentView;
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
		actionBar.setTitle("ԤԼ����");

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
			((CreateNewGroupActivity) parentActivity).finish();
			break;
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.tv_timeshow) {
			showTimeDialog();

		} else if (id == R.id.tv_confirm) {
			setDateOK();

		} else {
		}
		
	}
	private void showTimeDialog() {
		  AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
          View view = View.inflate(parentActivity, R.layout.date_time_dialog, null);
          final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
          final TimePicker timePicker = (android.widget.TimePicker) view.findViewById(R.id.time_picker);
          builder.setView(view);

          Calendar cal = Calendar.getInstance();
          cal.setTimeInMillis(System.currentTimeMillis());
          datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);

          timePicker.setIs24HourView(true);
          timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
          timePicker.setCurrentMinute(cal.get(Calendar.MINUTE));           
          
          String startTime = ApplicationLoader.applicationContext.getString(R.string.StartTimePicker);
          String ok = ApplicationLoader.applicationContext.getString(R.string.OK);
          String Cancel = ApplicationLoader.applicationContext.getString(R.string.Cancel);
//          if (v.getId() == R.id.selected_start_time) {
              builder.setTitle(startTime);
              builder.setPositiveButton(ok, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                      String timeString = getDateForm(
                    		  datePicker.getYear(),
                    		  datePicker.getMonth() + 1,
                              datePicker.getDayOfMonth(),
                              timePicker.getCurrentHour(),
                              timePicker.getCurrentMinute());
                      tvTime.setText(timeString);
                      dialog.cancel();
                  }
              });
              builder.setNegativeButton(Cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
//          }
          Dialog dialog = builder.create();
          dialog.show();	 

	}
	private void setDateOK() {
		String start_time = tvTime.getText().toString();
		int time = DateUnit.getTimeOfSeconde(start_time);
		Intent intent = new Intent();
		intent.putExtra("appintmentTime", time);
		parentActivity.setResult(Activity.RESULT_OK, intent);
		((CreateNewGroupActivity) parentActivity).finish();
	}
	/**
	 * @Title: getDateForm
	 *
	 * @Description: TODO
	 *
	 * @param time // 0 year 1 month 2day 3hour 4minute
	 * @return
	 */
	private String getDateForm(Integer...time) {
		 StringBuffer sb = new StringBuffer();
         sb.append(String.format("%d-%02d-%02d", 
        		 time[0], 
        		 time[1],
                 time[2]));
         sb.append(" ");
         sb.append(time[3])
         .append(":").append(time[4]).append(":").append("00");
         return sb.toString();
	}
}

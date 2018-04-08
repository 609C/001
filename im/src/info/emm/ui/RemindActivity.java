package info.emm.ui;


import java.lang.reflect.Field;
import java.util.Calendar;

import info.emm.LocalData.DateUnit;
import info.emm.messenger.ConnectionsManager;
import info.emm.messenger.MessagesController;
import info.emm.messenger.MessagesStorage;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.StringUtil;
import info.emm.utils.UiUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;


public class RemindActivity extends BaseFragment{

	private int user_id;
	private static Context ctx;
	private EditText remind_tv;
	private TextView time_title;
	private TextView time_remind;
	
	private TextView time_noticeTextView;
	
	private Button remindButton;
	
	private ImageView remindImageView;
	
	LinearLayout date_dialog;
	private View dialogLayout;
	private DatePicker datePicker;
	private TimePicker timePicker;
	private String s2;
	private String s1;
	
	private long dialog_id;
	
	private boolean guoqi;
	
	 TLRPC.TL_alertMedia alert;
	
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
		((LaunchActivity) parentActivity).showActionBar();
		((LaunchActivity) parentActivity).updateActionBar();
	}
	private void initData() {
//		_status = alert._status;
		remind_tv.setText(alert.msg);
		String tString = DateUnit.getMMddFormat1(alert.date);
		time_remind.setText(tString);
		
		String lastModifyTime = DateUnit.getMMddFormat1(alert.lastModifyTime);
		
		if (!(user_id == UserConfig.clientUserId)){
			time_title.setText(String.format(StringUtil.getStringFromRes(R.string.remind_you), tString));
			remind_layout.setEnabled(false);
			remind_tv.setEnabled(false);
			time_remind.setVisibility(View.GONE);
		}
		if (alert.status == 2) { //��ɾ��
			remind_tv.setEnabled(false);
			remindImageView.setVisibility(View.VISIBLE);
			remindImageView.setImageResource(R.drawable.remind_delet);
			remind_layout.setEnabled(false);
			time_noticeTextView.setText(String.format(StringUtil.getStringFromRes(R.string.remind_notice_delete), lastModifyTime));
			return;
		}
		guoqi = alert.date <= getCurrentTime();
		if (guoqi) {
			remind_tv.setEnabled(false);
			remind_layout.setEnabled(false);
			remindImageView.setVisibility(View.VISIBLE);
			remindImageView.setImageResource(R.drawable.remind_overdue);
			return;
		}
		if (alert.status == 1) {//����
			time_noticeTextView.setText(String.format(StringUtil.getStringFromRes(R.string.remind_notice_update), lastModifyTime));
		}
		remindButton.setVisibility(View.VISIBLE);
		if (user_id == UserConfig.clientUserId){
			remindButton.setText(R.string.remind_delete);
		}else {
			remindButton.setText(alert.status == 3?R.string.remind_me:R.string.remind_not_me);
		}
	}
	AlertDialog dialog;
	private LinearLayout remind_layout;
	private int idate;
	private void initView() {
//			long time=System.currentTimeMillis();  
//			final SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
//			final Date d1=new Date(time);  
//			final String t1=format.format(d1); 
				remindButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (user_id == UserConfig.clientUserId){   //ɾ������
							new AlertDialog.Builder(parentActivity).setTitle(R.string.remind_delete).setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
				            		alert.status = 2;
				            		MessagesController.getInstance().sendMessage(alert,dialog_id);
				            		MessagesStorage.getInstance().putAlert(alert,false);
				            		MessagesController.getInstance().scheduleAlert(alert, false);
				            		dialog.dismiss();
				            		finishFragment();
								}
							}).setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							}).create().show();
						}else {
							if (alert.status == 3) {
								alert.status = 0;
								MessagesController.getInstance().scheduleAlert(alert, true);
							}else {
								alert.status = 3;
								MessagesController.getInstance().scheduleAlert(alert, false);
							}
							
							remindButton.setText(alert.status == 3?R.string.remind_me:R.string.remind_not_me);
							MessagesStorage.getInstance().putAlert(alert,false);
						}
					}
				});
			
				String timeString = DateUnit.getMMddFormat1(alert.date);
				time_remind.setText(timeString);
				
			dialog = new AlertDialog.Builder(parentActivity).setTitle(R.string.remind_choose_time).setView(dialogLayout).setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						s1 = (datePicker.getYear()+"-"+(datePicker.getMonth()+1)+"-"+datePicker.getDayOfMonth()); 
						String dateString = s1+s2;
						time_remind.setText(dateString);
						alert.date = ConnectionsManager.getInstance().dateStrToInt1(dateString);
						if (alert.date <= (getCurrentTime()+60)) {
            				if (alert.date <= getCurrentTime()) {
            					UiUtil.showToast(parentActivity, R.string.remind_time_advance);
							}else{
            				UiUtil.showToast(parentActivity, R.string.remind_time_Too_close);
							}
					}
						dialog.dismiss();
					}
				}).setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				}).create();
			
			remind_layout.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					timePicker.setIs24HourView(true);
					timePicker.setCurrentHour(DateUnit.getCurrentDate().getHours()+1);
					timePicker.setCurrentMinute(0);
					int minute = timePicker.getCurrentMinute();
					s2 = "  "+(timePicker.getCurrentHour())+":"+(minute<10?"0"+minute:minute);
					  timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
							
							@Override
							public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
								s2 = ("  "+hourOfDay+":"+(minute<10?"0"+minute:minute));
								Log.e("TAG", s2);
							}
						});
					  dialog.show();
					
					
				}
			});
		}
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (fragmentView == null) {
			fragmentView = inflater.inflate(R.layout.remind, container, false);
			remind_tv = (EditText) fragmentView.findViewById(R.id.remind_tv);
			remind_layout = (LinearLayout) fragmentView.findViewById(R.id.remind_layout);
			time_noticeTextView = (TextView) fragmentView.findViewById(R.id.tv_notice);
			time_title = (TextView) fragmentView.findViewById(R.id.time_title);
			time_remind = (TextView) fragmentView.findViewById(R.id.time_remind);
			remindImageView = (ImageView)fragmentView.findViewById(R.id.iv_remind_ic);
			remindButton = (Button)fragmentView.findViewById(R.id.remind_delete_bt);
			dialogLayout = inflater.inflate(R.layout.remind_dialog, null);
			datePicker = (DatePicker) dialogLayout.findViewById(R.id.datePicker);
			timePicker = (TimePicker) dialogLayout.findViewById(R.id.timePicker);
			
			try {
			       Field f[] = datePicker.getClass().getDeclaredFields();
			       for (Field field : f) {
			           if (field.getName().equals("mYearPicker") ||field.getName().equals("mYearSpinner") ) {
			               field.setAccessible(true);
			               Object yearPicker = new Object();
			               yearPicker = field.get(datePicker);
			               ((View) yearPicker).setVisibility(View.GONE);
			           }
			       }
			   } 
			   catch (SecurityException e) {
			       Log.d("ERROR", e.getMessage());
			   } 
			   catch (IllegalArgumentException e) {
			       Log.d("ERROR", e.getMessage());
			   } 
			   catch (IllegalAccessException e) {
			       Log.d("ERROR", e.getMessage());
			   }
			
			 
		}else {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		initView();
		if (!StringUtil.isEmpty(alert.guid)) {
			initData();
		}
		return fragmentView;
	}
	
	@Override
	public boolean onFragmentCreate() {
		super.onFragmentCreate();
		alert = new TLRPC.TL_alertMedia();
		user_id = getArguments().getInt("userid");
		dialog_id = getArguments().getLong("dialogid",0l);
		
		alert.guid = getArguments().getString("guid");
		alert.status = getArguments().getInt("status",0);
		alert.msg = getArguments().getString("msg");
		
		alert.lastModifyTime = getArguments().getInt("lastModifyTime");
		alert.id = getArguments().getInt("id");
		
		int minute = Calendar.getInstance().get(Calendar.MINUTE);
		int second = Calendar.getInstance().get(Calendar.SECOND);
		int time = getCurrentTime() - minute *60 - second + 60 *60;
		idate = alert.date = getArguments().getInt("date",time);
		
		return true;
	}
	@Override
	public void onFragmentDestroy() {
		super.onFragmentDestroy();
	}

	@Override
	public void applySelfActionBar() {
		if (parentActivity == null) {
			return;
		}
		  ActionBar actionBar =  super.applySelfActionBar(true);
		actionBar.setTitle(R.string.Remind);

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
			finishFragment();
			break;
		}
		return true;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		
		final boolean b = ((alert.status == 0||alert.status == 1)&&user_id == UserConfig.clientUserId); //���û�����
		final boolean isCreate; 
		
		int tId ;
		if ((!StringUtil.isEmpty(alert.guid))) {
			if (!b||guoqi) {
				return;
			}
			tId = R.string.Done;
			isCreate = false;
		}else {
			tId = R.string.Create;
			isCreate = true;
		}
				
		 inflater.inflate(R.menu.group_profile_menu, menu);
	        SupportMenuItem doneItem = (SupportMenuItem)menu.findItem(R.id.block_user);
	        TextView doneTextView = (TextView)doneItem.getActionView().findViewById(R.id.done_button);
	        doneTextView.setText(tId);
	        doneTextView.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
	            	String textString = remind_tv.getText().toString().trim();
	            	if (!StringUtil.isEmpty(textString)) {
	            			if (alert.date <= (getCurrentTime()+60)) {
	            				if (alert.date <= getCurrentTime()) {
	            					UiUtil.showToast(parentActivity, R.string.remind_time_advance);
		            				return;
								}else{
	            				UiUtil.showToast(parentActivity, R.string.remind_time_Too_close);
	            				return;
								}
						}
	            		if (isCreate) {
	            			alert.guid = Utilities.getUUID();
	            			alert.status = 0;
	            			alert.id=UserConfig.getAlertId();
						}else if(alert.msg.toString().equals(textString)&&idate == alert.date){
							finishFragment();
							return;
						}else{
							alert.status = 1;							
						}  
	            		alert.msg = remind_tv.getText().toString().trim();
//	            		alert.date = ConnectionsManager.getInstance().dateStrToInt1(time_remind.getText().toString().trim());
//	            		alert.status = b ? 1 : 0;	            		
	            		alert.lastModifyTime = getCurrentTime();
	            		MessagesController.getInstance().sendMessage(alert, dialog_id);	
//	            		MessagesController.getInstance().setAlarmTime(alert);
	            		MessagesController.getInstance().scheduleAlert(alert, true);
	            		MessagesStorage.getInstance().putAlert(alert,false);
	            		finishFragment();
	            		
					}else{
						UiUtil.showToast(parentActivity, R.string.remind_content);
					}
	            	finishFragment();
	            }
	        });
	}
	 public int getCurrentTime() {
	        return ConnectionsManager.getInstance().getCurrentTime();
	    }
	} 
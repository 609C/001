package info.emm.ui;

import android.os.Bundle;

import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.TLRPC;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager.LayoutParams;

public class AlertActivity extends FragmentActivity {
	private String meetId;
	private boolean playing;
	private int fromid=0;
	private int gid=0;
	private int type=0;
	private String pwd ="";
	private String alertMsg;

	@SuppressLint("ValidFragment")
	public class AlertEvent extends DialogFragment
	{
		private MediaPlayer myMediaPlayer;
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// We need to turn on the screen 
			getActivity().getWindow().addFlags(LayoutParams.FLAG_TURN_SCREEN_ON |
					LayoutParams.FLAG_SHOW_WHEN_LOCKED |
					LayoutParams.FLAG_KEEP_SCREEN_ON |
					LayoutParams.FLAG_DISMISS_KEYGUARD);

			String Tips = ApplicationLoader.applicationContext.getString(R.string.Tips);
			String msg = alertMsg;
			if(type==0 || type==1)
			{	
				msg = LocaleController.formatString( "JoinMeetingAlertFromUnknown", R.string.JoinMeetingAlertFromUnknown );
				if(fromid!=0)
				{
					TLRPC.User user = MessagesController.getInstance().users.get(fromid);
					if(user!=null)
					{
						String fname = Utilities.formatName(user);
						msg = LocaleController.formatString( "JoinMeetingAlertFrom", R.string.JoinMeetingAlertFrom,fname );
					}
				}
			}			
			else if(type==2)
				Tips = ApplicationLoader.applicationContext.getString(R.string.alerttitle);				

			String ok = ApplicationLoader.applicationContext.getString(R.string.OK);
			String Cancel = ApplicationLoader.applicationContext.getString(R.string.Cancel);

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(Tips);
			builder.setMessage(msg);
			builder.setPositiveButton(ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					if(type!=2)
					{
						Intent intent = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.setAction("info.emm.joinmeeting");
						//����������������face_meeting_activity
						intent.putExtra("meetingId", meetId);
						intent.putExtra("pwd", pwd);
						intent.putExtra("userId", fromid);
						intent.putExtra("chatId", gid);		        	
						intent.putExtra("type", type);		        	
						ApplicationLoader.applicationContext.startActivity(intent); 			        
						getActivity().finish();
					}
				}
			});
			if(type!=2)
			{
				builder.setNegativeButton (Cancel, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						//�ܾ��������,//0=���У�1=ȡ�����У�2=�ܾ���3=æ��4=�˳�
						if(type==1)//0��ԤԼ���飬1�Ǽ�ʱ����
						{	
							ArrayList<Integer> users = new ArrayList<Integer>();
							users.add(fromid);
							MessagesController.getInstance().meetingCall(meetId,gid,users,2);
							getActivity().finish();					   
						}
					}                      
				});
			}
			AlertDialog ad = builder.create();
			//			ad.setCanceledOnTouchOutside(true);
			//			FileLog.e("emm", "onCreateDialog");
			return ad;
		}

		public void presentFragment(BaseFragment fragment)
		{
			fragment.onFragmentCreate();
		}

		@Override
		public void onPause() {
			super.onPause();
			if(myMediaPlayer != null && playing)
				myMediaPlayer.stop();
			//			FileLog.e("emm", "onPause");
		}

		@Override
		public void onResume() {
			super.onResume();
			//			FileLog.e("emm", "onResume");
			playSound(this.getActivity(), getAlarmUri());
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			if(myMediaPlayer != null)
				myMediaPlayer.stop();
			if(getActivity() != null)
				getActivity().finish();
		}

		private void playSound(Context context, Uri alert) {
			myMediaPlayer = new MediaPlayer();
			try {
				myMediaPlayer.setDataSource(context, alert);
				final AudioManager audioManager = (AudioManager) context
						.getSystemService(Context.AUDIO_SERVICE);
				if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
					myMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
					myMediaPlayer.prepare();
					myMediaPlayer.start();
					playing = true;
				}
			} catch (IOException e) {
				//		        System.out.println("OOPS");
				e.printStackTrace();
			}
		}

		private Uri getAlarmUri() 
		{
			if(type==2)
			{
				Uri  alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				if (alert == null) {
					alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
				}
				return alert; 
			}
			Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
			if (alert == null) 
			{
				alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				if (alert == null) {
					alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
				}
			}
			return alert;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent i = getIntent();
		if (i != null) 
		{
			type = i.getIntExtra("type", 0);
			if(type==0)
			{
				//��ʾԤԼ����
				meetId = i.getStringExtra("meetingId");						
				fromid = i.getIntExtra("userId", 0);			
				pwd = i.getStringExtra("pwd");

				AlertEvent ae = new AlertEvent();
				ae.show(getSupportFragmentManager(), "Alert");
				setContentView(R.layout.activity_alert);			
			}
			else if(type==1)
			{
				//��ʾ��ʱ����
				meetId = i.getStringExtra("meetingId");						
				fromid = i.getIntExtra("userId", 0);			
				gid = i.getIntExtra("chatId", 0);

				Intent intent = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setAction("info.emm.joinmeeting");
				//����������������phoneactivity
				intent.putExtra("meetingId", meetId);
				intent.putExtra("userId", fromid);
				intent.putExtra("chatId", gid);		        	
				intent.putExtra("type", type);		        	
				ApplicationLoader.applicationContext.startActivity(intent); 			        
				finish();
			}
			else if(type==2)
			{
				//��ʾ����
				alertMsg = i.getStringExtra("message");
				AlertEvent ae = new AlertEvent();
				ae.show(getSupportFragmentManager(), "Alert");
				setContentView(R.layout.activity_alert);		
			}
		}
	}
}

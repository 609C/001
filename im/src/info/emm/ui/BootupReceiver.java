package info.emm.ui;

//--------------------------------- IMPORTS ------------------------------------
import info.emm.messenger.FileLog;
import info.emm.messenger.MQService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;


public class BootupReceiver extends BroadcastReceiver{

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver
	 * #onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				//启动消息推送MQToolsService
				Intent svc = new Intent(ApplicationLoader.applicationContext, MQService.class);
				ApplicationLoader.applicationContext.startService(svc);
				FileLog.d("emm", "andriod service started");
			}
		}, 5000);
	}

}

package info.emm.messenger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

import java.util.Calendar;

import info.emm.LocalData.Config;
import info.emm.ui.ApplicationLoader;



public class MQService extends Service{

//	public native int init();// 初始化
//
//	public native int cleanup();
//
//	public native int mqnew(char[] clientID, boolean clean_session);
//
//	public native int mqloop(int timeout, int max_packets);// 线程循环始终
//
//	public native void mqdestroy();// 断开链接
//
//	public native int connect(char[] host, int port, int keepalive);// 链接
//
//	public native int disconnect();// 断开链接
//
//	public native int reconnect();// 重链
//
//	//Sam 这个接口的返回值比较特殊，为了使用方便，大于0的返回值表示mid，小于等于0的返回值表示失败
//	public native int publish(char[] topic, int payloadlen, byte[] payload,
//			int qos, boolean retain);// 发送消息
//
//	public native int subscribe(char[] sub, int qos);
//
//	public native int unsubscribe(char[] sub);
//
//	public native int setnp(char[] username, char[] pwd);

	private MyThread mMyThread;
	private MQtoolsBinder mBinder;
	private MQTools mqTools;
//
//	private MQListener mqListener;

	//sam
	private static PowerManager.WakeLock wakeLock = null;
	private static short keepAliveSeconds	 = 10 * 60;
	// receiver that wakes the Service up when it's time to ping the server
	private PingSender pingSender;
	public static final String MQTT_PING_ACTION = "info.emm.messenger.MQTools.PING";
//	private Context mqContext=null;

	//private ArrayList<ListenMessageArrival> mListenMessageArrival = new ArrayList<ListenMessageArrival>();
	/**
	 * 监听发送成功的数据
	 */
	//private ArrayList<LaunchActivity.ListenPublishSuccess> listen_publish = new ArrayList<LaunchActivity.ListenPublishSuccess>();
	@Override
	public IBinder onBind(Intent intent) {

		FileLog.d("emm", "***onBind");

		return mBinder;
	}

	/**
	 * 链接服务器
	 */
	@Override
	public void onCreate() {
		FileLog.d("emm", "mqtools onCreate");

		mBinder = new MQtoolsBinder();// 实例binder

		ApplicationLoader.postInitApplication();
		if( mqTools == null )
		{
			FileLog.d("emm", "***new MQTools");
			mqTools = new MQTools();
			mqTools.init();
		}

		super.onCreate();
	}

	public void createPingSender() {
		// creates the intents that are used to wake up the phone when it is
		//  time to ping the server
		if (pingSender == null)
		{
			pingSender = new PingSender();
			IntentFilter filter = new IntentFilter(MQTT_PING_ACTION);
			//mqContext.registerReceiver(pingSender, filter);
			ApplicationLoader.applicationContext.registerReceiver(pingSender, filter);
		}
	}

	public void deletePingSender() {
		if (pingSender != null)
		{
			ApplicationLoader.applicationContext.unregisterReceiver(pingSender);
			pingSender = null;
		}
	}

	ServiceConnection conn = new ServiceConnection()
	{
		@Override
		public void onServiceDisconnected(ComponentName name) {
			FileLog.d("emm", "***onServiceDisconnected");
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			FileLog.d("emm", "***onServiceConnected");
			if(ConnectionsManager.getInstance().mqBinder == null )
			{
				FileLog.d("emm", "***onServiceConnected *************************");
				ConnectionsManager.getInstance().mqBinder = (MQService.MQtoolsBinder) service;
//				ConnectionsManager.getInstance().SetServiceBinder(MQService.getInstance().mqBinder);
				ConnectionsManager.getInstance().mqBinder.setOnListenMessage(ConnectionsManager.getInstance());
				ConnectionsManager.getInstance().connected = true;
				connectServ();
			}
		}
	};

	/**
	 * 重复绑定时我们要做的逻辑
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		FileLog.d("emm", "***onStartCommand");
		super.onStartCommand(intent, flags, startId);


		if(!UserConfig.clientActivated)
			return START_STICKY;

		if(ApplicationLoader.mqStarted) return START_STICKY;
		ApplicationLoader.mqStarted = true;
		if(ConnectionsManager.getInstance().mqBinder != null )
		{
			connectServ();
			ConnectionsManager.getInstance().connected = true;
		}
		else
		{
			Intent mIntent = new Intent(ApplicationLoader.applicationContext, MQService.class);
			//mqContext.startService(mIntent);
			FileLog.d("emm", "***Start bindService");
			ApplicationLoader.applicationContext.bindService(mIntent, conn, Context.BIND_AUTO_CREATE);
		}


		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

//		ApplicationLoader.applicationContext.unbindService(conn);
//		ApplicationLoader.mqStarted = false;
	}

	class MyThread extends Thread {
		public void run() {
//			String clientID = "" + UserConfig.clientUserId;
//			//mqTools.init();
//			mqTools.mqnew(clientID.toCharArray(), true);
//			mqTools.connect(DicqConstant.MESSAGEHOST.toCharArray(), DicqConstant.MESSAGEPORT, 10);
			//MessagesController.getInstance().post_notifycenter_message.obtainMessage(8888, 0 , 0).sendToTarget();//确保线程启动了再绑定
			//mqTools.mqloop(-1, -1);
			FileLog.d("emm", "end thread");
		}
	}


	//sam
	/*
	* Used to implement a keep-alive protocol at this Service level - it sends
	*  a PING message to the server, then schedules another ping after an
	*  interval defined by keepAliveSeconds
	*/
	public class PingSender extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// Note that we don't need a wake lock for this method (even though
			//  it's important that the phone doesn't switch off while we're
			//  doing this).
			// According to the docs, "Alarm Manager holds a CPU wake lock as
			//  long as the alarm receiver's onReceive() method is executing.
			//  This guarantees that the phone will not sleep until you have
			//  finished handling the broadcast."
			// This is good enough for our needs.

			//FileLog.e("emm", "PingSender");

//			try {
//	            if (wakeLock == null) {
//	                PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
//	                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "lock");
//	            }
//	            if (!wakeLock.isHeld()) {
//	                wakeLock.acquire(5000);
//	            }
//	        } catch (Exception e) {
//	            try {
//	                if (wakeLock != null) {
//	                    wakeLock.release();
//	                }
//	            } catch (Exception e2) {
//	                FileLog.e("emm", e2);
//	            }
//	            FileLog.e("emm", e);
//	        }

			FileLog.d("emm", "PingSender onReceive 1");
			if(!ApplicationLoader.isScreenOn)
			{
				FileLog.d("emm", "PingSender onReceive 2");
				mqTools.mqloop(0, 0);
			}

			// start the next keep alive period
			//scheduleNextPing(context);
			//scheduleNextPing();
		}
	}

	/*
	 * Schedule the next time that you want the phone to wake up and ping the
	 *  message broker server
	 */
	//private void scheduleNextPing(Context context)
	private void scheduleNextPing()
	{
		FileLog.d("emm", "scheduleNextPing");
		// When the phone is off, the CPU may be stopped. This means that our
		//   code may stop running.
		// When connecting to the message broker, we specify a 'keep alive'
		//   period - a period after which, if the client has not contacted
		//   the server, even if just with a ping, the connection is considered
		//   broken.
		// To make sure the CPU is woken at least once during each keep alive
		//   period, we schedule a wake up to manually ping the server
		//   thereby keeping the long-running connection open
		// Normally when using this Java MQTT client library, this ping would be
		//   handled for us.
		// Note that this may be called multiple times before the next scheduled
		//   ping has fired. This is good - the previously scheduled one will be
		//   cancelled in favour of this one.
		// This means if something else happens during the keep alive period,
		//   (e.g. we receive an MQTT message), then we start a new keep alive
		//   period, postponing the next ping.

		PendingIntent pendingIntent = null;
//		if(mqContext != null)
//			pendingIntent = PendingIntent.getBroadcast(mqContext, 0, new Intent(MQTT_PING_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
//		else
//			pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(MQTT_PING_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);

		//pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(MQTT_PING_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
		pendingIntent = PendingIntent.getBroadcast(ApplicationLoader.applicationContext, 0, new Intent(MQTT_PING_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
		// in case it takes us a little while to do this, we try and do it
		//  shortly before the keep alive period expires
		// it means we're pinging slightly more frequently than necessary
		Calendar wakeUpTime = Calendar.getInstance();
		wakeUpTime.add(Calendar.SECOND, keepAliveSeconds-2);

		AlarmManager aMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
//		aMgr.set(AlarmManager.RTC_WAKEUP,
//				 wakeUpTime.getTimeInMillis(),
//				 pendingIntent);
		aMgr.setRepeating(AlarmManager.RTC_WAKEUP,
				wakeUpTime.getTimeInMillis(),
				keepAliveSeconds*1000, pendingIntent);
	}

	public void connectServ()
	{
		FileLog.d("emm", "***connectServ");
		createPingSender();
		//scheduleNextPing(mqTools.mqContext);
		scheduleNextPing();
		String clientID = "" + UserConfig.currentUser.identification;
		//mqTools.init();
		mqTools.mqnew(clientID.toCharArray(), true);

		FileLog.e("emm", "setnp:" + UserConfig.currentUser.id + " sid:" + UserConfig.currentUser.sessionid);

		if(!clientID.isEmpty() && !UserConfig.currentUser.sessionid.isEmpty())
			mqTools.setnp(clientID.toCharArray(), UserConfig.currentUser.sessionid.toCharArray());
		FileLog.e("emm", "connect host:" + Config.MESSAGEHOST + " " + Config.MESSAGEPORT);
		mqTools.connect(Config.MESSAGEHOST.toCharArray(), Config.MESSAGEPORT, keepAliveSeconds);
	}

	public class MQtoolsBinder extends Binder {
		/**
		 * 获得当前Service对象
		 *
		 * @return
		 */
		public MQService getMqToolsService() {
			return MQService.this;
		}

//		public void connectServ()
//		{
//			FileLog.d("emm", "***connectServ");
//			mqTools.createPingSender();
//			//scheduleNextPing(mqTools.mqContext);
//			scheduleNextPing();
//			String clientID = "" + UserConfig.clientUserId;
//			//mqTools.init();
//			mqTools.mqnew(clientID.toCharArray(), true);
//			if(!UserConfig.currentUser.email.isEmpty() && !UserConfig.currentUser.sessionid.isEmpty())
//				mqTools.setnp(UserConfig.currentUser.email.toCharArray(), UserConfig.currentUser.sessionid.toCharArray());
//			mqTools.connect(DicqConstant.MESSAGEHOST.toCharArray(), DicqConstant.MESSAGEPORT, keepAliveSeconds);
//		}

		public void disconnectServ() {
			FileLog.d("emm", "***disconnectServ 1");
			mqTools.disconnect();
			mqTools.mqdestroy();
			MQService.this.deletePingSender();
			FileLog.d("emm", "***disconnectServ 2");
		}

//		public void setContext(Context context) {
//			mqTools.mqContext = context;
//		}


//		public void cleanUp() {
//			mqTools.clear();
//		}

		/**
		 * 监听消息到达
		 * @param mListen
		 */
		public void setOnListenMessage(MQListener mListen)
		{
//			if( mqTools == null )
//			{
//				mqTools = new MQTools();
//				mqTools.init();
//			}
			mqTools.setMqListener(mListen);
//			mMyThread = new MyThread();
//			mMyThread.start();
		}
//		/**
//		 * 当Service执行Stop后调用这个方法让其清空注册的消息
//		 */
//		public void destoryCallBack(String teString){
//			FileLog.d("emm", "***destoryCallBack");
//			mqListener = null;
//		}

		public int publishMessage(byte[] msgbytes , String topic)
		{
			//非 0 失败
			int ret = mqTools.publish(topic.toCharArray(), msgbytes.length, msgbytes, 1, false);
			return ret;
		}
	}
//	public void clear()
//	{
//		cleanup();
//	}
//	public void onConnect(int result) // 连接状态回调
//	{
//		FileLog.d("emm", "***onConnect");
//		if(mqListener!=null)
//			mqListener.onConnect(result);
//	}

//	public void onDisConnect(int rc) // 断开状态回调
//	{
//		FileLog.d("emm", "***onDisConnect");
//		if(mqListener!=null)
//			mqListener.onDisConnect(rc);
//	}

//	public void onMessageArrival(String topic, byte[] payload, int payloadLen)
//	{
//		if(mqListener!=null)
//			mqListener.onMessageArrival(topic, payload);
//	}

//	public void onLog(int level, String content)
//	{
//		if(mqListener!=null)
//			mqListener.onLog(level, content);
//	}

	/**
	 * 发送消息成功的回调
	 * @param result 消息的ID
	 */
//	public void onPublish(int msgid)
//	{		
//		if(mqListener!=null)
//			mqListener.onPublishACK(msgid);		
//	}

}
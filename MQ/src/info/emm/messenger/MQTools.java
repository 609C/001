package info.emm.messenger;

import android.util.Log;

public class MQTools {

	static{
		System.loadLibrary("MQ");
	}
	private MQListener mqListener;
	public MQListener getMqListener() {
		return mqListener;
	}

	public void setMqListener(MQListener mqListener) {
		this.mqListener = mqListener;
	}

	public native int init();// 初始化

	public native int cleanup();

	public native int mqnew(char[] clientID, boolean clean_session);

	public native int mqloop(int timeout, int max_packets);// 线程循环始终

	public native void mqdestroy();// 断开链接

	public native int connect(char[] host, int port, int keepalive);// 链接

	public native int disconnect();// 断开链接

	public native int reconnect();// 重链

	//Sam 这个接口的返回值比较特殊，为了使用方便，大于0的返回值表示mid，小于等于0的返回值表示失败
	public native int publish(char[] topic, int payloadlen, byte[] payload,
							  int qos, boolean retain);// 发送消息

	public native int subscribe(char[] sub, int qos);

	public native int unsubscribe(char[] sub);

	public native int setnp(char[] username, char[] pwd);

	//	public MQTools(Context context) {
//		NativeLoader.initNativeLibs(context);
//	}
	public void onConnect(int result) // 连接状态回调
	{
		Log.d("emm", "***onConnect");
		if(mqListener!=null)
			mqListener.onConnect(result);
	}
	public void onDisConnect(int rc) // 断开状态回调
	{
		Log.d("emm", "***onDisConnect");
		if(mqListener!=null)
			mqListener.onDisConnect(rc);
	}
	/**
	 * 监听消息到达
	 * @param mListen
	 */
	public void onMessageArrival(String topic, byte[] payload, int payloadLen)
	{
		if(mqListener!=null)
			mqListener.onMessageArrival(topic, payload);
	}
	public void onLog(int level, String content)
	{
		if(mqListener!=null)
			mqListener.onLog(level, content);
	}

	/**
	 * 发送消息成功的回调
	 * @param result 消息的ID
	 */
	public void onPublish(int msgid)
	{
		if(mqListener!=null)
			mqListener.onPublishACK(msgid);
	}
	/**
	 * 当Service执行Stop后调用这个方法让其清空注册的消息
	 */
	public void destoryCallBack(String teString){
		Log.d("emm", "***destoryCallBack");
		mqListener = null;
	}
	
	
}
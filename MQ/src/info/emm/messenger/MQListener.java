package info.emm.messenger;
import android.content.Context;
public interface MQListener 
{
	public void onMessageArrival(String topic , byte[] payload);
	public void onConnect(int rc);
	public void onDisConnect(int rc);
	public void onPublishACK(int msgid);
	public void onLog(int level, String content);
}

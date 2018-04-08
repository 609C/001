package com.weiyicloud.whitepad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class NotificationCenter {
	final private HashMap<Integer, ArrayList<Object>> observers = new HashMap<Integer, ArrayList<Object>>();

	final private HashMap<String, Object> memCache = new HashMap<String, Object>();

	final private HashMap<Integer, Object> removeAfterBroadcast = new HashMap<Integer, Object>();
	final private HashMap<Integer, Object> addAfterBroadcast = new HashMap<Integer, Object>();
	final private ArrayList<Object> removeAfterBroadcastClass =new ArrayList<Object>();

	private boolean broadcasting = false;

	private static volatile NotificationCenter Instance = null;
	public static NotificationCenter getInstance() {
		NotificationCenter localInstance = Instance;
		if (localInstance == null) {
			synchronized (NotificationCenter.class) {
				localInstance = Instance;
				if (localInstance == null) {
					Instance = localInstance = new NotificationCenter();
				}
			}
		}
		return localInstance;
	}

	public interface NotificationCenterDelegate {  // 接受消息者必须实现此接口
		public abstract void didReceivedNotification(int id, Object... args);
	}

	public void addToMemCache(int id, Object object) {//添加到缓存
		addToMemCache(String.valueOf(id), object);
	}

	public void addToMemCache(String id, Object object) {
		memCache.put(id, object);
	}

	public Object getFromMemCache(int id) {
		return getFromMemCache(String.valueOf(id), null);
	}

	public Object getFromMemCache(String id, Object defaultValue) {
		Object obj = memCache.get(id);
		if (obj != null) {
			memCache.remove(id);
			return obj;
		}
		return defaultValue;
	}
	/**
	 * 发消息
	 * @param id
	 * @param args
	 */
	public void postNotificationName(int id, Object... args) {
		synchronized (observers) {
			broadcasting = true;
			ArrayList<Object> objects = observers.get(id);
			if (objects != null) {
				for (Object obj : objects)
				{
					((NotificationCenterDelegate)obj).didReceivedNotification(id, args);
				}
			}
			broadcasting = false;
			if (!removeAfterBroadcast.isEmpty()) {
				for (HashMap.Entry<Integer, Object> entry : removeAfterBroadcast.entrySet()) {
					removeObserver(entry.getValue(), entry.getKey());
				}
				removeAfterBroadcast.clear();
			}
			if (!addAfterBroadcast.isEmpty()) {
				for (HashMap.Entry<Integer, Object> entry : addAfterBroadcast.entrySet()) {
					addObserver(entry.getValue(), entry.getKey());
				}
				addAfterBroadcast.clear();
			}
			if (!removeAfterBroadcastClass.isEmpty()) {
				for (int i = 0 ; i< removeAfterBroadcastClass.size();i++) {
					removeObserver(removeAfterBroadcastClass.get(i));
				}
				removeAfterBroadcastClass.clear();
			}
		}
	}

	/**
	 * @Title: postNotificationName2
	 *
	 * @Description: 锟斤拷时锟接的ｏ拷锟睫改癸拷司锟斤拷息锟斤拷锟剿达拷锟斤拷锟侥第讹拷锟斤拷锟斤拷锟斤拷为锟斤拷锟矫讹拷潜锟斤拷锟斤拷锟斤拷锟斤拷锟接帮拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
	 *
	 * @param id
	 * @param args
	 */
	public void postNotificationName2(int id, Object... args) {
		synchronized (observers) {
			broadcasting = true;
			ArrayList<Object> objects = observers.get(id);
			if (objects != null) {
				for (Object obj : objects)
				{
					((NotificationCenterDelegate)obj).didReceivedNotification(id, args);
				}
			}
			broadcasting = false;
			if (!removeAfterBroadcast.isEmpty()) {
				for (HashMap.Entry<Integer, Object> entry : removeAfterBroadcast.entrySet()) {
					removeObserver(entry.getValue(), entry.getKey());
				}
				removeAfterBroadcast.clear();
			}
			if (!addAfterBroadcast.isEmpty()) {
				for (HashMap.Entry<Integer, Object> entry : addAfterBroadcast.entrySet()) {
					addObserver(entry.getValue(), entry.getKey());
				}
				addAfterBroadcast.clear();
			}
		}
	}
	/**
	 * 关注某条消息
	 * @param observer 消息接收者，必须实现NotificationCenterDelegate 接口
	 * @param id 消息id，表示要关注的消息类型
	 */
	public void addObserver(Object observer, int id) {
		synchronized (observers) {
			if (broadcasting) {
				addAfterBroadcast.put(id, observer);
				return;
			}

			if(removeAfterBroadcastClass.contains(observer))
				removeAfterBroadcastClass.remove(observer);

			ArrayList<Object> objects = observers.get(id);
			if (objects == null)
			{
				observers.put(id, (objects = new ArrayList<Object>()));
			}
			if (objects.contains(observer)) {
				return;
			}
			//if( id == MessagesController.contactsDidLoaded)
			//FileLog.d("emm", "postNotificationName addObserver");
			objects.add(observer);
		}
	}
	/**
	 * 停止关注各种消息
	 * @param observer要停止关注消息的接收者
	 */
	public void removeObserver(Object observer) {
		synchronized (observers) {
			if (broadcasting) {
				removeAfterBroadcastClass.add(observer);
				return;
			}
			Iterator<HashMap.Entry<Integer, ArrayList<Object>>>  iter = observers.entrySet().iterator();
			while (iter.hasNext()) {
				HashMap.Entry entry = (HashMap.Entry) iter.next();
				ArrayList<Object> objectsl = (ArrayList<Object>) entry.getValue();
				objectsl.remove(observer);
			}
		}
	}
	/**
	 *  停止关注某条消息
	 * @param observer消息接收者
	 * @param id  消息id，要停止关注的消息类型
	 */
	public void removeObserver(Object observer, int id) {
		synchronized (observers) {
			if (broadcasting) {
				removeAfterBroadcast.put(id, observer);
				return;
			}
			ArrayList<Object> objects = observers.get(id);
			if (objects != null) {
				objects.remove(observer);
				if (objects.size() == 0) {
					observers.remove(id);
				}
			}
		}
	}
	/**
	 *    移除所有消息关注者，通常在退出会议时调用
	 */
	public void removeAllObservers() {
		synchronized (observers) {
			observers.clear();
		}
	}
}
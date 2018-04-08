/*
 * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.messenger;



import android.annotation.SuppressLint;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressLint("UseSparseArrays")
public class NotificationCenter {

    final private HashMap<Integer, ArrayList<Object>> observers = new HashMap<Integer, ArrayList<Object>>();
    final private HashMap<String, Object> memCache = new HashMap<String, Object>();

    final private HashMap<Integer, Object> removeAfterBroadcast = new HashMap<Integer, Object>();
    final private HashMap<Integer, Object> addAfterBroadcast = new HashMap<Integer, Object>();

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

    public interface NotificationCenterDelegate {
        public abstract void didReceivedNotification(int id, Object... args);
    }

    public void addToMemCache(int id, Object object) {
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
        }
    }

    /**
     * @Title: postNotificationName2
     *
     * @Description: 临时加的，修改公司信息，此处传的第二个参数为引用而非变量。避免影响其它操作。
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

    public void addObserver(Object observer, int id) {
        synchronized (observers) {
            if (broadcasting) {
                addAfterBroadcast.put(id, observer);
                return;
            }
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

    public void removeObserver(Object observer, int id) {
        synchronized (observers) {
            if (broadcasting) {
                removeAfterBroadcast.put(id, observer);
                return;
            }
            ArrayList<Object> objects = observers.get(id);
            if (objects != null)
            {
                objects.remove(observer);
                if (objects.size() == 0) {
                    observers.remove(id);
                }
            }
        }
    }
}

/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.emm.messenger;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
//sam
//import javax.annotation.concurrent.GuardedBy;

/**
 * Manages the proximity sensor and notifies a listener when enabled.
 */
public class ProximitySensorManager {
    /**
     * Listener of the state of the proximity sensor.
     * <p>
     * This interface abstracts two possible states for the proximity sensor, near and far.
     * <p>
     * The actual meaning of these states depends on the actual sensor.
     */
    public interface Listener {
        /** Called when the proximity sensor transitions from the far to the near state. */
        public void onNear();
        /** Called when the proximity sensor transitions from the near to the far state. */
        public void onFar();
    }

    public static enum State {
        NEAR, FAR
    }

    private final ProximitySensorEventListener mProximitySensorListener;

    /**
     * The current state of the manager, i.e., whether it is currently tracking the state of the
     * sensor.
     */
    private boolean mManagerEnabled;

    /**
     * The listener to the state of the sensor.
     * <p>
     * Contains most of the logic concerning tracking of the sensor.
     * <p>
     * After creating an instance of this object, one should call {@link #register()} and
     * {@link #unregister()} to enable and disable the notifications.
     * <p>
     * Instead of calling unregister, one can call {@link #unregisterWhenFar()} to unregister the
     * listener the next time the sensor reaches the {@link State#FAR} state if currently in the
     * {@link State#NEAR} state.
     */
    private static class ProximitySensorEventListener implements SensorEventListener {
        private static final float FAR_THRESHOLD = 4.8f;//5.0f; hz

        private final SensorManager mSensorManager;
        private final Sensor mProximitySensor;
        private final float mMaxValue;
        private final Listener mListener;
        
        /**
         * The last state of the sensor.
         * <p>
         * Before registering and after unregistering we are always in the {@link State#FAR} state.
         */
        //sam
//        @GuardedBy("this") private State mLastState;
        private State mLastState;
        /**
         * If this flag is set to true, we are waiting to reach the {@link State#FAR} state and
         * should notify the listener and unregister when that happens.
         */
        //sam
//        @GuardedBy("this") private boolean mWaitingForFarState;
        private boolean mWaitingForFarState;

        public ProximitySensorEventListener(SensorManager sensorManager, Sensor proximitySensor,
                Listener listener) {
            mSensorManager = sensorManager;
            mProximitySensor = proximitySensor;
            mMaxValue = proximitySensor.getMaximumRange();
            mListener = listener;
            // Initialize at far state.
            mLastState = State.FAR;
            mWaitingForFarState = false;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            // Make sure we have a valid value.
            if (event.values == null) return;
            if (event.values.length == 0) return;
            float value = event.values[0];
            // Convert the sensor into a NEAR/FAR state.
            State state = getStateFromValue(value);
            synchronized (this) {
                // No change in state, do nothing.
                if (state == mLastState) return;
                // Keep track of the current state.
                mLastState = state;
                // If we are waiting to reach the far state and we are now in it, unregister.
                if (mWaitingForFarState && mLastState == State.FAR) {
                    unregisterWithoutNotification();
                }
            }
            // Notify the listener of the state change.
            switch (state) {
                case NEAR:
                    mListener.onNear();
                    break;

                case FAR:
                    mListener.onFar();
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Nothing to do here.
        }

        /** Returns the state of the sensor given its current value. */
        private State getStateFromValue(float value) {
            // Determine if the current value corresponds to the NEAR or FAR state.
            // Take case of the case where the proximity sensor is binary: if the current value is
            // equal to the maximum, we are always in the FAR state.
            return (value > FAR_THRESHOLD || value == mMaxValue) ? State.FAR : State.NEAR;
        }

        /**
         * Unregister the next time the sensor reaches the {@link State#FAR} state.
         */
        public synchronized void unregisterWhenFar() {
            if (mLastState == State.FAR) {
                // We are already in the far state, just unregister now.
                unregisterWithoutNotification();
            } else {
                mWaitingForFarState = true;
            }
        }

        /** Register the listener and call the listener as necessary. */
        public synchronized void register() {
            // It is okay to register multiple times.
            mSensorManager.registerListener(this, mProximitySensor, SensorManager.SENSOR_DELAY_UI);
            // We should no longer be waiting for the far state if we are registering again.
            mWaitingForFarState = false;
        }

        public void unregister() {
            State lastState;
            synchronized (this) {
                unregisterWithoutNotification();
                lastState = mLastState;
                // Always go back to the FAR state. That way, when we register again we will get a
                // transition when the sensor gets into the NEAR state.
                mLastState = State.FAR;
            }
            // Notify the listener if we changed the state to FAR while unregistering.
            if (lastState != State.FAR) {
                mListener.onFar();
            }
        }

        //sam
//        @GuardedBy("this")
        private void unregisterWithoutNotification() {
            mSensorManager.unregisterListener(this);
            mWaitingForFarState = false;
        }
    }
    
    //sam use hidden api
    private PowerManager powerManager;
    // The hidden api that uses a wake lock
    private WakeLock proximityWakeLock;
    private static Method powerLockReleaseIntMethod;

    public ProximitySensorManager(Context context, Listener listener, boolean use_hidden_api) {
    	powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
     // Try to detect the hidden api
        if (powerManager != null && use_hidden_api) {
            // Try to use powermanager proximity sensor
            try {
                boolean supportProximity = false;
                Field f = PowerManager.class.getDeclaredField("PROXIMITY_SCREEN_OFF_WAKE_LOCK");
                int proximityScreenOffWakeLock = (Integer) f.get(null);
                if(android.os.Build.VERSION.SDK_INT >= 17) {
                    // Changes of the private API on android 4.2
                    Method method = powerManager.getClass().getDeclaredMethod("isWakeLockLevelSupported", int.class);
                    supportProximity = (Boolean) method.invoke(powerManager, proximityScreenOffWakeLock);
                    FileLog.d("emm", "Use 4.2 detection way for proximity sensor detection. Result is " + supportProximity);
                }else {
                    Method method = powerManager.getClass().getDeclaredMethod("getSupportedWakeLockFlags");
                    int supportedFlags = (Integer) method.invoke(powerManager);
                    FileLog.d("emm", "Proxmity flags supported : " + supportedFlags);
                    supportProximity = ((supportedFlags & proximityScreenOffWakeLock) != 0x0);
                }
                if (supportProximity) {
                	FileLog.d("emm", "We can use native screen locker !!");
                    proximityWakeLock = powerManager.newWakeLock(proximityScreenOffWakeLock,
                            "com.csipsimple.CallProximity");
                    proximityWakeLock.setReferenceCounted(false);
                }
                
            } catch (Exception e) {
            	FileLog.d("emm", "Impossible to get power manager supported wake lock flags ");
            }
            if(powerLockReleaseIntMethod == null) {
                try {
                    powerLockReleaseIntMethod = proximityWakeLock.getClass().getDeclaredMethod(
                            "release", int.class);
                    
                }catch (Exception e) {
                	FileLog.d("emm", "Impossible to get power manager release with it");
                }
            }
        }
        
        // Try to detect a proximity sensor as fallback
        if(proximityWakeLock == null) {
            SensorManager sensorManager =
                    (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
	        Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
	        if (proximitySensor == null) {
	            // If there is no sensor, we should not do anything.
	            mProximitySensorListener = null;
	        } else {
	            mProximitySensorListener =
	                    new ProximitySensorEventListener(sensorManager, proximitySensor, listener);
	        }
        }
        else {
        	mProximitySensorListener = null;
        }
    }

    private boolean isProximityWakeHeld = false;
    /**
     * Enables the proximity manager.
     * <p>
     * The listener will start getting notifications of events.
     * <p>
     * This method is idempotent.
     */
    public void enable() {
    	
        if (proximityWakeLock != null && !isProximityWakeHeld) {
            proximityWakeLock.acquire();
            isProximityWakeHeld = true;
        }
        
        if (mProximitySensorListener != null && !mManagerEnabled) {
            mProximitySensorListener.register();
            mManagerEnabled = true;
        }
    }

    /**
     * Disables the proximity manager.
     * <p>
     * The listener will stop receiving notifications of events, possibly after receiving a last
     * {@link Listener#onFar()} callback.
     * <p>
     * If {@code waitForFarState} is true, if the sensor is not currently in the {@link State#FAR}
     * state, the listener will receive a {@link Listener#onFar()} callback the next time the sensor
     * actually reaches the {@link State#FAR} state.
     * <p>
     * If {@code waitForFarState} is false, the listener will receive a {@link Listener#onFar()}
     * callback immediately if the sensor is currently not in the {@link State#FAR} state.
     * <p>
     * This method is idempotent.
     */
    public void disable(boolean waitForFarState) {
    	
        if (proximityWakeLock != null && isProximityWakeHeld) {
            boolean usedNewRelease = false;
            if(powerLockReleaseIntMethod != null) {
                try {
                    powerLockReleaseIntMethod.invoke(proximityWakeLock, 0);
                    usedNewRelease = true;
                    //Log.d(THIS_FILE, "CALL NEW RELEASE WITH FLAG " + flag);
                } catch (Exception e) {
                    FileLog.e("emm", e);
                }
            }
            if(!usedNewRelease) {
                proximityWakeLock.release();
            }
            isProximityWakeHeld = false;
        }
        
        if (mProximitySensorListener != null && mManagerEnabled) {
            if (waitForFarState) {
                mProximitySensorListener.unregisterWhenFar();
            } else {
                mProximitySensorListener.unregister();
            }
            mManagerEnabled = false;
        }
    }
}

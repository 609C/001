package info.emm.services.impl;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.media.MediaPlayer;
import android.net.Uri;

import info.emm.services.ISoundService;
import info.emm.ui.ApplicationLoader;
import info.emm.ui.PhoneActivity;
import info.emm.yuanchengcloudb.R;

public class SoundService implements ISoundService {
	
	private MediaPlayer mRingPlayer;
	private AudioManager mAudioManager; 
	
	private Context mContext;
	
	/////////
	 private SoundPool soundPool;
	 private int sound_notification;
	 private int sound_sent;
	 private int sound_start_record;
	///////
	private RingType mRingType;
	private AudioManager audio;
	
	/*private OnAudioFocusChangeListener audioFocusChangeListener = new OnAudioFocusChangeListener() {
		
		@Override
		public void onAudioFocusChange(int focusChange) {
			switch (focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN:
				if(IMRtmpClientMgr.getInstance().wantPublishAudio){
					IMRtmpClientMgr.getInstance().publishAudio();
				}
				IMRtmpClientMgr.getInstance().playLocalAudio();
				IMRtmpClientMgr.getInstance().publishCallphone(1);
				break;
			case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
				break;
			case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
				break;
			case AudioManager.AUDIOFOCUS_LOSS:
				MediaController.getInstance().stopAudio();
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				MediaController.getInstance().stopAudio();
				if(IMRtmpClientMgr.getInstance().wantPublishAudio){
					IMRtmpClientMgr.getInstance().unPublishAudio();
				}
				IMRtmpClientMgr.getInstance().unplayLocalAudio();
				IMRtmpClientMgr.getInstance().publishCallphone(0);
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				break;
			default:
				break;
			}
		}
	};*/
	
	public SoundService() {
		this.mContext = ApplicationLoader.getContext();
		mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		initSoundPool();
	}
	private void initSoundPool() {
		soundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
        sound_notification = soundPool.load(ApplicationLoader.applicationContext, R.raw.notification, 1);
        sound_sent = soundPool.load(ApplicationLoader.applicationContext, R.raw.sent, 1);
        sound_start_record = soundPool.load(ApplicationLoader.applicationContext, R.raw.start_record, 1);
	}
	@Override
	public void startRingMusic(RingType mRingType) {
		this.mRingType = mRingType;
		stopRingMusic();
		createMediaPlayer();
		if (mRingPlayer != null) {
			synchronized (mRingPlayer) {
				mRingPlayer.start();
				setRingVolume();
			}
		}
	}
	private void createMediaPlayer() {
		try {
			AssetFileDescriptor afd = null;
			mRingPlayer = new MediaPlayer();
			int resId = R.raw.ga;
			switch (this.mRingType) {
			case RING_CALL_OUT:
				resId = R.raw.gc;
				mAudioManager.setMode(AudioManager.MODE_IN_CALL);
				mAudioManager.setSpeakerphoneOn(false);
				break;
			case RING_CALL_IN:
				resId = R.raw.ga;
				break;
			case RING_CALL_BUSY:
				resId = R.raw.ga;
				break;
			default:
				break;
			}
			afd = this.mContext.getResources().openRawResourceFd(resId);
			mRingPlayer.setAudioStreamType(AudioManager.STREAM_RING);
			mRingPlayer.setDataSource(afd.getFileDescriptor(),
					afd.getStartOffset(), afd.getLength());
			afd.close();
			mRingPlayer.setLooping(true);
			mRingPlayer.prepare();
		} catch (Exception e) {
		}

	}

	@Override
	public void stopRingMusic() {
		if (mRingPlayer != null) {
			synchronized (mRingPlayer) {
				mRingPlayer.stop();
				mAudioManager.setMode(AudioManager.MODE_NORMAL); 
			}
		}
	}
	public void setRingVolume() {
		if (mRingPlayer != null) {
			float maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
			float currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
			float volume = currentVolume/maxVolume;
			mRingPlayer.setVolume(volume, volume);
		}
	}
	@Override
	public void requestSoundFocus() {
		if (audio == null) {
			 audio = (AudioManager) ApplicationLoader.getContext().getSystemService(PhoneActivity.AUDIO_SERVICE);
		}
		/*if (audio != null) {
			audio.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);  
		 }*/
	}
	@Override
	public void abandonSoundFocus() {
		if (audio!=null) {
			//audio.abandonAudioFocus(audioFocusChangeListener);
		}
	}
	private Uri getAlarmUri() {
        Uri alert = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        if (alert == null) {
            alert = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (alert == null) {
                alert = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_ALARM);
            }
        }
        return alert;
    }
	@Override
	public void playSound() {
		stopSound();
			mRingPlayer = new MediaPlayer();
			try {
				mRingPlayer.setDataSource(ApplicationLoader.getContext(), getAlarmUri());
				mRingPlayer.setAudioStreamType(AudioManager.STREAM_RING);
				mRingPlayer.prepare();
		    } catch (IOException e) {
		    }
		synchronized (mRingPlayer) {
			mRingPlayer.start();
		}
	}
	@Override
	public void stopSound() {
		if (mRingPlayer != null) {
			synchronized (mRingPlayer) {
				mRingPlayer.stop();
			}
		}
	}
	@Override
	public void playMidSound(int id) {
		if (soundPool == null) {
			initSoundPool();
		}
		switch(id){
    	case 0:            	
    		soundPool.play(sound_sent, 1, 1, 1, 0, 1);
    		break;
    	case 1:
    		soundPool.play(sound_start_record, 1, 1, 1, 0, 1);
    		break;
    	case 2:
    		soundPool.play(sound_notification, 1, 1, 1, 0, 1);
    		break;
    	}
	}
}

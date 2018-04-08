package info.emm.services;

import info.emm.services.impl.SoundService;

public class UEngine {
	protected static UEngine sInstance;
	
	protected ISoundService mSoundService;
	
	public static UEngine getInstance(){
		if(sInstance == null){
			sInstance = new UEngine();
		}
		return sInstance;
	}
	public ISoundService getSoundService(){
		if(mSoundService == null){
			mSoundService = new SoundService();
		}
		return mSoundService;
	}
}

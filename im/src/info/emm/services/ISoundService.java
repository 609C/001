package info.emm.services;

public interface ISoundService {
	public enum RingType{
		RING_CALL_IN,RING_CALL_OUT,RING_CALL_BUSY
	}
	void startRingMusic(RingType style);
	void stopRingMusic();
	
	void setRingVolume();
	
	void requestSoundFocus();
	void abandonSoundFocus();
	
	void playSound();
	void stopSound();
	
	void playMidSound(int id);
}

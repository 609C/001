/*
 * This is the source code of Emm for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package info.emm.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;


import info.emm.messenger.FileLog;
import info.emm.messenger.FileLoader;

import info.emm.messenger.MediaController;
import info.emm.messenger.MessagesController;

import info.emm.messenger.TLRPC;
import info.emm.objects.MessageObject;
import info.emm.ui.Views.ProgressView;
import info.emm.ui.Views.SeekBar;
import info.emm.utils.UiUtil;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import java.io.File;

import com.utils.WeiyiMeeting;


public class ChatAudioCell extends ChatBaseCell implements SeekBar.SeekBarDelegate, MediaController.FileDownloadProgressListener {

    private static Drawable[][] statesDrawable = new Drawable[8][2];
    private static TextPaint timePaint;
//    private ImageReceiver avatarImage;
    private SeekBar seekBar;
    private ProgressView progressView;
    private int seekBarX;
    private int seekBarY;

    private int buttonState = 0;
    private int buttonX;
    private int buttonY;
    private int buttonPressed = 0;

    private int avatarPressed = 0;

    private StaticLayout timeLayout;
    private int timeX;
    private String lastTimeString = null;

    private int TAG;

    public TLRPC.User audioUser;
    private TLRPC.FileLocation currentPhoto;
    private int playPos=0;
    private static Drawable unReadAudioDrawable;//wangxm add for δ�����
    int unReadRecwidth = 0;
    private Context mContext;
    
    private boolean isPlaying = false;
    public ChatAudioCell(Context context, boolean isChat) 
    {	
        super(context, isChat);
        mContext = context;
        TAG = MediaController.getInstance().generateObserverTag();
        seekBar = new SeekBar(context);
        seekBar.delegate = this;
        progressView = new ProgressView();

        if (timePaint == null) {
            statesDrawable[0][0] = getResources().getDrawable(R.drawable.play1);
            statesDrawable[0][1] = getResources().getDrawable(R.drawable.play1_pressed);
            statesDrawable[1][0] = getResources().getDrawable(R.drawable.pause1);
            statesDrawable[1][1] = getResources().getDrawable(R.drawable.pause1_pressed);            
            //xueqiang change��TODO:�е�ʱ�������ظ�дDB���»���ʾ����ͬ������Ϣ����ʾ����BUTTON�Ǵ���ģ���ʱ���������п��ҵ����BUG
            statesDrawable[2][0] = getResources().getDrawable(R.drawable.audioload1);
            statesDrawable[2][1] = getResources().getDrawable(R.drawable.audioload1_pressed);
            statesDrawable[3][0] = getResources().getDrawable(R.drawable.audiocancel1);
            statesDrawable[3][1] = getResources().getDrawable(R.drawable.audiocancel1_pressed);

            statesDrawable[4][0] = getResources().getDrawable(R.drawable.play2);
            statesDrawable[4][1] = getResources().getDrawable(R.drawable.play2_pressed);
            statesDrawable[5][0] = getResources().getDrawable(R.drawable.pause2);
            statesDrawable[5][1] = getResources().getDrawable(R.drawable.pause2_pressed);
            
            
            
          //xueqiang change��TODO:�е�ʱ�������ظ�дDB���»���ʾ����ͬ������Ϣ����ʾ����BUTTON�Ǵ���ģ���ʱ���������п��ҵ����BUG
            statesDrawable[6][0] = getResources().getDrawable(R.drawable.audioload2);
            statesDrawable[6][1] = getResources().getDrawable(R.drawable.audioload2_pressed);
            statesDrawable[7][0] = getResources().getDrawable(R.drawable.audiocancel2);
            statesDrawable[7][1] = getResources().getDrawable(R.drawable.audiocancel2_pressed);

            timePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timePaint.setTextSize(Utilities.dp(12));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        boolean result = seekBar.onTouch(event.getAction(), event.getX() - seekBarX, event.getY() - seekBarY);
        if (result) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            invalidate();
        } else {
//            int side = Utilities.dp(36);
        	int side = Utilities.dp(32);
        	int width = Utilities.dp(28);
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (x >= buttonX-width && x <= buttonX + side + width && y >= buttonY-width && y <= buttonY + side + width) {
                	if (WeiyiMeeting.isInMeeting()) 
                	{ //hz
                		UiUtil.showToastForChat(mContext, R.string.toast_msg_have_a_call);
                		return true;
                	}
                	if (MessagesController.getInstance().earphone) {
                		UiUtil.showToast(mContext, R.drawable.function_receiver_btn,R.string.chatfooter_SpeakerOff_now);
					}
                    buttonPressed = 1;
                    invalidate();
                    result = true;
                }
            } else if (buttonPressed == 1) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    buttonPressed = 0;
                    playSoundEffect(SoundEffectConstants.CLICK);
                    didPressedButton();
                    invalidate();
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonPressed = 0;
                    invalidate();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!(x >= buttonX-width && x <= buttonX + side + width && y >= buttonY - width && y <= buttonY + side + width)) {
                        buttonPressed = 0;
                        invalidate();
                    }
                }
            } else if (avatarPressed == 1) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    avatarPressed = 0;
                    playSoundEffect(SoundEffectConstants.CLICK);
                    if (delegate != null) {
                        delegate.didPressedUserAvatar(this, audioUser);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    avatarPressed = 0;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    
                }
            }
            if (!result) {
                result = super.onTouchEvent(event);
            }
        }

        return result;
    }

    private void didPressedButton() {
        if (buttonState == 0) 
        {	
            boolean result = MediaController.getInstance().playAudio(currentMessageObject);
            if (result) {
                buttonState = 1;
                invalidate();
            }
        } else if (buttonState == 1) {
            boolean result = MediaController.getInstance().pauseAudio(currentMessageObject);
            if (result) {
                buttonState = 0;
                invalidate();
            }
        }else if (buttonState == 2) {
            FileLoader.getInstance().loadFile(null, null, null, currentMessageObject.messageOwner.media.audio);
            buttonState = 3;
            invalidate();
        } else if (buttonState == 3) {
            FileLoader.getInstance().cancelLoadFile(null, null, null, currentMessageObject.messageOwner.media.audio);
            buttonState = 2;
            invalidate();
        }
    }

    public void updateProgress() 
    {
        if (currentMessageObject == null) {
            return;
        }

        if (!seekBar.isDragging()) {
            seekBar.setProgress(currentMessageObject.audioProgress);
        }
        
        //xueqiang add for auto playaudio
    	/*if(currentMessageObject.messageOwner.isPlaying)
    	{	
	        buttonState = 1;
    	}*/

        int duration = 0;
        if (!MediaController.getInstance().isPlayingAudio(currentMessageObject)) {
            duration = currentMessageObject.messageOwner.media.audio.duration;
        } else {
            duration = currentMessageObject.audioProgressSec;
        }
        String timeString = String.format("%02d:%02d", duration / 60, duration % 60);
        if (lastTimeString == null || lastTimeString != null && !lastTimeString.equals(timeString)) {
            int timeWidth = (int)Math.ceil(timePaint.measureText(timeString));
            timeLayout = new StaticLayout(timeString, timePaint, timeWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        }
        invalidate();
    }

    public void downloadAudioIfNeed() {
        if (buttonState == 2) {
            FileLoader.getInstance().loadFile(null, null, null, currentMessageObject.messageOwner.media.audio);
            buttonState = 3;
            invalidate();
        }
    }

    public void updateButtonState() 
    {
    	//xueqiang change for �жϱ����Ƿ������Ƶ�ļ�
        String fileName = currentMessageObject.getFileName();
        File cacheFile = new File(Utilities.getSystemDir(), fileName);
        if (!cacheFile.exists())
        	FileLog.d("emm","audio file isn't exist");
        if (cacheFile.exists()) 
        {
            //MediaController.getInstance().removeLoadingFileObserver(this);
            boolean playing = MediaController.getInstance().isPlayingAudio(currentMessageObject);
            if (!playing || playing && MediaController.getInstance().isAudioPaused()) 
            {
            	//FileLog.e("emm", "updateButtonState buttonState=0");
                buttonState = 0;
            } 
            else 
            {
            	//FileLog.e("emm", "updateButtonState buttonState=1");
                buttonState = 1;
            }
            progressView.setProgress(0);
        } 
        else 
        {
        	FileLog.e("emm", "updateButtonState shouldn't work here");
            MediaController.getInstance().addLoadingFileObserver(fileName, this);
            if (!FileLoader.getInstance().isLoadingFile(fileName)) {
                buttonState = 2;
                progressView.setProgress(0);
            } else {
                buttonState = 3;
                Float progress = FileLoader.getInstance().fileProgresses.get(fileName);
                if (progress != null) {
                    progressView.setProgress(progress);
                } else {
                    progressView.setProgress(0);
                }
            }
        }
        updateProgress();
    }

    @Override
    public void onFailedDownload(String fileName) {
        updateButtonState();
    }

    @Override
    public void onSuccessDownload(String fileName) {
        updateButtonState();
    }

    @Override
    public void onProgressDownload(String fileName, float progress) {
        progressView.setProgress(progress);
        invalidate();
    }

    @Override
    public void onProgressUpload(String fileName, float progress, boolean isEncrypted) {

    }

    @Override
    public int getObserverTag() {
        return TAG;
    }

    @Override
    public void onSeekBarDrag(float progress) {
        if (currentMessageObject == null) {
            return;
        }
        currentMessageObject.audioProgress = progress;
        MediaController.getInstance().seekToProgress(currentMessageObject, progress ,buttonState == 1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, Utilities.dp(68));
        if (chat) {
            backgroundWidth = Math.min(width - Utilities.dp(102), Utilities.dp(300));
        } else {
            backgroundWidth = Math.min(width - Utilities.dp(50), Utilities.dp(300));
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
//        if (currentMessageObject.messageOwner.out) {
//        	avatarImage.imageX = layoutWidth - backgroundWidth + Utilities.dp(9);
//            seekBarX = layoutWidth - backgroundWidth + Utilities.dp(97);
//            buttonX = layoutWidth - backgroundWidth + Utilities.dp(67);
//            timeX = layoutWidth - backgroundWidth + Utilities.dp(71);
//        } else {
//            if (chat) {
//            	avatarImage.imageX = Utilities.dp(69);
//                seekBarX = Utilities.dp(158);
//                buttonX = Utilities.dp(128);
//                timeX = Utilities.dp(132);
//            } else {
//            	avatarImage.imageX = Utilities.dp(16);
//                seekBarX = Utilities.dp(106);
//                buttonX = Utilities.dp(76);
//                timeX = Utilities.dp(80);
//            }
//        }
//        avatarImage.imageY = Utilities.dp(9);
//        avatarImage.imageW = Utilities.dp(50);
//        avatarImage.imageH = Utilities.dp(50);
//
//        seekBar.width = backgroundWidth - Utilities.dp(112);
//        seekBar.height = Utilities.dp(30);
//        progressView.width = backgroundWidth - Utilities.dp(136);
//        progressView.height = Utilities.dp(30);
//        seekBarY = Utilities.dp(13);
//        buttonY = Utilities.dp(10);
        
        if (currentMessageObject.messageOwner.out) {
//        	buttonX = layoutWidth - backgroundWidth + Utilities.dp(9);
        	buttonX = layoutWidth - backgroundWidth + Utilities.dp(18);
        	seekBarX = layoutWidth - backgroundWidth + Utilities.dp(59);
            timeX = layoutWidth - backgroundWidth + Utilities.dp(64);
        } else {
            if (chat) {
                seekBarX = Utilities.dp(120);
//                buttonX = Utilities.dp(69);
                buttonX = Utilities.dp(78);
                timeX = Utilities.dp(125);
            } else {
                seekBarX = Utilities.dp(68);
//                buttonX = Utilities.dp(16);
                buttonX = Utilities.dp(25);
                timeX = Utilities.dp(73);
            }
        }
        
//        buttonY = Utilities.dp(9);
        buttonY = Utilities.dp(22);
        seekBar.width = backgroundWidth - Utilities.dp(98);
        seekBar.height = Utilities.dp(34);
        progressView.width = backgroundWidth - Utilities.dp(136) + Utilities.dp(14);
        progressView.height = Utilities.dp(30);
        seekBarY = Utilities.dp(18);
        updateProgress();
    }

    @Override
    protected boolean isUserDataChanged() {
        TLRPC.User newUser = MessagesController.getInstance().users.get(currentMessageObject.messageOwner.media.audio.user_id);
        TLRPC.FileLocation newPhoto = null;

        if (newUser != null && newUser.photo != null) {
            newPhoto = newUser.photo.photo_small;
        }

        return currentPhoto == null && newPhoto != null || currentPhoto != null && newPhoto == null || currentPhoto != null && newPhoto != null && (currentPhoto.local_id != newPhoto.local_id || currentPhoto.volume_id != newPhoto.volume_id) || super.isUserDataChanged();
    }

    @Override
    public void setMessageObject(MessageObject messageObject) {
        if (currentMessageObject != messageObject || isUserDataChanged()) {
        	int maxWidth;
        	if (chat) {
				maxWidth = Math.min(Utilities.displaySize.x - Utilities.dp(102) , Utilities.dp(300));
				drawName = true;
        	} else {
        		maxWidth = Math.min(Utilities.displaySize.x - Utilities.dp(60) , Utilities.dp(300));
        	}
        	backgroundWidth = maxWidth;
        	
            int uid = messageObject.messageOwner.media.audio.user_id;
            if (uid == 0) {
                uid = messageObject.messageOwner.from_id;
            }
            audioUser = MessagesController.getInstance().users.get(uid);
//            if (audioUser != null) {
//                if (audioUser.photo != null) {
//                    currentPhoto = audioUser.photo.photo_small;
//                }
//                avatarImage.setImage(currentPhoto, "50_50", getResources().getDrawable(Utilities.getUserAvatarForId(uid)));
//            } else {
//                avatarImage.setImage((TLRPC.FileLocation)null, "50_50", getResources().getDrawable(Utilities.getUserAvatarForId(uid)));
//            }

            if (messageObject.messageOwner.out) {
                seekBar.type = 0;
                progressView.setProgressColors(0xffb4e396, 0xff6ac453);
            } else {
                seekBar.type = 1;
                progressView.setProgressColors(0xffd9e2eb, 0xff86c5f8);
            }
            
            super.setMessageObject(messageObject);
        }
        updateButtonState();
    }

    @Override
    protected void onDraw(Canvas canvas) 
    {
        super.onDraw(canvas);

//        if (currentMessageObject == null) {
//            return;
//        }
//        avatarImage.draw(canvas, avatarImage.imageX, avatarImage.imageY, Utilities.dp(50), Utilities.dp(50));
//        canvas.save();
//        if (buttonState == 0 || buttonState == 1) {
//            canvas.translate(seekBarX, seekBarY);
//            seekBar.draw(canvas);
//        } else {
//            canvas.translate(seekBarX + Utilities.dp(12), seekBarY);
//            progressView.draw(canvas);
//        }
//        canvas.restore();
//
//        int state = buttonState;
//        if (!currentMessageObject.messageOwner.out) {
//            state += 4;
//            timePaint.setColor(0xffa1aab3);
//        } else {
//            timePaint.setColor(0xff70b15c);
//        }
//        Drawable buttonDrawable = statesDrawable[state][buttonPressed];
//        int side = Utilities.dp(36);
//        int x = (side - buttonDrawable.getIntrinsicWidth()) / 2;
//        int y = (side - buttonDrawable.getIntrinsicHeight()) / 2;
//        setDrawableBounds(buttonDrawable, x + buttonX, y + buttonY);
//        buttonDrawable.draw(canvas);
//
//        canvas.save();
//        canvas.translate(timeX, Utilities.dp(45));
//        timeLayout.draw(canvas);
//        canvas.restore();
//        
//        Drawable unReadAudioDrawable = getResources().getDrawable(R.drawable.rec);
//		int width = unReadAudioDrawable.getIntrinsicWidth();
//		if (chat && !currentMessageObject.messageOwner.out) {
//        	if (!currentMessageObject.messageOwner.isRead) 
//    		{	
//    			setDrawableBounds(unReadAudioDrawable, backgroundWidth + width + Utilities.dp(15), Utilities.dp(30));
//                unReadAudioDrawable.draw(canvas);
//    		}
//		} else if (!chat && !currentMessageObject.messageOwner.out) {
//			setDrawableBounds(unReadAudioDrawable, backgroundWidth - width - Utilities.dp(5), Utilities.dp(30));
//            unReadAudioDrawable.draw(canvas);
//		}
        
        
        if (currentMessageObject == null) {
            return;
        }
        canvas.save();
        if (buttonState == 0 || buttonState == 1) {
            canvas.translate(seekBarX, seekBarY);
            seekBar.draw(canvas);
        } else {
            canvas.translate(seekBarX + Utilities.dp(12), seekBarY);
            progressView.draw(canvas);
        }
        canvas.restore();

        int state = buttonState;
        if (!currentMessageObject.messageOwner.out) {
            state += 4;
            timePaint.setColor(0xffa1aab3);
        } else {
            timePaint.setColor(0xff70b15c);
        }
        Drawable buttonDrawable = statesDrawable[state][buttonPressed];
        int x = Utilities.dp(24);
        int y = Utilities.dp(24);
        buttonDrawable.setBounds(buttonX, buttonY,x + buttonX, y + buttonY);
        buttonDrawable.draw(canvas);

        canvas.save();
        canvas.translate(timeX, Utilities.dp(45));
        timeLayout.draw(canvas);
        canvas.restore();
        
        Drawable unReadAudioDrawable = getResources().getDrawable(R.drawable.rec);
		unReadRecwidth = unReadAudioDrawable.getIntrinsicWidth();
		if (chat && !currentMessageObject.messageOwner.out) {
        	if (!currentMessageObject.messageOwner.isRead) {	
    			setDrawableBounds(unReadAudioDrawable, backgroundWidth + unReadRecwidth + Utilities.dp(15), Utilities.dp(28));
                unReadAudioDrawable.draw(canvas);
    		}
		} else if (!chat && !currentMessageObject.messageOwner.out) {
			if (!currentMessageObject.messageOwner.isRead) {
				setDrawableBounds(unReadAudioDrawable, backgroundWidth - unReadRecwidth - Utilities.dp(15), Utilities.dp(28));
	            unReadAudioDrawable.draw(canvas);
    		}
    	}
    }

    @Override
    protected void finalize() throws Throwable {
        MediaController.getInstance().removeLoadingFileObserver(this);
        super.finalize();
    }
    public void sendTouchEvent()
    {
    	this.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 63, 41, 0));
    	this.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 63, 41, 0));
    }   
}

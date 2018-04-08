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
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;
import info.emm.messenger.FileLog;
import info.emm.messenger.TLRPC;
import info.emm.messenger.UserConfig;
import info.emm.objects.MessageObject;
import info.emm.ui.Views.ImageReceiver;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import java.lang.ref.WeakReference;

public class ChatReminderCell extends ChatBaseCell {

    private static TextPaint timePaint;
    private ImageReceiver avatarImage;
    private int textX, textY;
    private int totalHeight = 0;
    
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
    
    private int lastVisibleBlockNum = 0;
    private int firstVisibleBlockNum = 0;
    private int totalVisibleBlocksCount = 0;
    public MTouchEnevt mEvent;
    public boolean isInTouch;
    public ChatReminderCell(Context context, boolean isChat) 
    {	
        super(context, isChat);
        avatarImage = new ImageReceiver();
        avatarImage.parentView = new WeakReference<View>(this);
        if (timePaint == null) {
            timePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timePaint.setTextSize(Utilities.dp(12));
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (backGroudRect.isInTouch(x)) {
        	 if (event.getAction() == MotionEvent.ACTION_DOWN){
        		 isInTouch = true;
        	 }
         }
        return super.onTouchEvent(event);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int h = Math.max(Utilities.dp(68), totalHeight);
        setMeasuredDimension(width, h);
//        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), totalHeight);
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
        	avatarImage.imageX = layoutWidth - backgroundWidth + Utilities.dp(9);
        } else {
            if (chat) {
            	avatarImage.imageX = Utilities.dp(69);
            } else {
            	avatarImage.imageX = Utilities.dp(16);
            }
        }
        avatarImage.imageY = Utilities.dp(9);
        avatarImage.imageW = Utilities.dp(50);
        avatarImage.imageH = Utilities.dp(50);
        
        if (currentMessageObject.messageOwner.out) {
            textX = layoutWidth - backgroundWidth + Utilities.dp(10);
            textY = Utilities.dp(10) + namesOffset;
        } else {
            textX = Utilities.dp(19) + (chat ? Utilities.dp(52) : 0);
            textY = Utilities.dp(10) + namesOffset;
        }
        textX += Utilities.dp(53);
    }

//    @Override
//    protected boolean isUserDataChanged() {
//        TLRPC.User newUser = MessagesController.getInstance().users.get(currentMessageObject.messageOwner.media.audio.user_id);
//        TLRPC.FileLocation newPhoto = null;
//
//        if (newUser != null && newUser.photo != null) {
//            newPhoto = newUser.photo.photo_small;
//        }
//
//        return currentPhoto == null && newPhoto != null || currentPhoto != null && newPhoto == null || currentPhoto != null && newPhoto != null && (currentPhoto.local_id != newPhoto.local_id || currentPhoto.volume_id != newPhoto.volume_id) || super.isUserDataChanged();
//    }

    @Override
    public void setMessageObject(MessageObject messageObject) {
//        if (currentMessageObject != messageObject || isUserDataChanged()) {
//        	int maxWidth;
//        	if (chat) {
//				maxWidth = Math.min(Utilities.displaySize.x - Utilities.dp(102) , Utilities.dp(300));
//				drawName = true;
//        	} else {
//        		maxWidth = Math.min(Utilities.displaySize.x - Utilities.dp(60) , Utilities.dp(300));
//        	}
//        	backgroundWidth = maxWidth;
//            avatarImage.setImage((TLRPC.FileLocation)null, "50_50", getResources().getDrawable(R.drawable.remind_ic));
//            super.setMessageObject(messageObject);
//          
//            totalHeight = messageObject.textHeight + Utilities.dpf(19.5f) + namesOffset;
//        }
        
        if (currentMessageObject != messageObject || isUserDataChanged()) {
            if (currentMessageObject != messageObject) {
                firstVisibleBlockNum = 0;
                lastVisibleBlockNum = 0;
            }
            avatarImage.setImage((TLRPC.FileLocation)null, "50_50", getResources().getDrawable(R.drawable.remind_ic));
            int maxWidth;
            if (chat) {
                maxWidth = Utilities.displaySize.x - Utilities.dp(122);
                if (messageObject.messageOwner.from_id == UserConfig.clientUserId)
                	maxWidth -= Utilities.dp(15);
                else
                	maxWidth -= Utilities.dp(59);
                drawName = true;
            } else {
                maxWidth = Utilities.displaySize.x - Utilities.dp(80);
                maxWidth -= Utilities.dp(59);
            }

            backgroundWidth = maxWidth;

            super.setMessageObject(messageObject);

            backgroundWidth = messageObject.textWidth;
            totalHeight = messageObject.textHeight + Utilities.dpf(19.5f) + namesOffset;

            int maxChildWidth = Math.max(backgroundWidth, nameWidth);
            maxChildWidth = Math.max(maxChildWidth, forwardedNameWidth);

            int timeMore = timeWidth + Utilities.dp(6);
            if (messageObject.messageOwner.out) {
                timeMore += Utilities.dpf(20.5f);
            }

            if (maxWidth - messageObject.lastLineWidth < timeMore) {
                totalHeight += Utilities.dp(14);
                backgroundWidth = Math.max(maxChildWidth, messageObject.lastLineWidth) + Utilities.dp(29);
            } else {
                int diff = maxChildWidth - messageObject.lastLineWidth;
                if (diff >= 0 && diff <= timeMore) {
                    backgroundWidth = maxChildWidth + timeMore - diff + Utilities.dp(29);
                } else {
                    backgroundWidth = Math.max(maxChildWidth, messageObject.lastLineWidth + timeMore) + Utilities.dp(29);
                }
            }
        }
    }
    
    public void setVisiblePart(int position, int height) {
        if (currentMessageObject == null || currentMessageObject.textLayoutBlocks == null) {
            return;
        }
        int newFirst = -1, newLast = -1, newCount = 0;

        for (int a = Math.max(0, (position - textY) / currentMessageObject.blockHeight); a < currentMessageObject.textLayoutBlocks.size(); a++) {
            MessageObject.TextLayoutBlock block = currentMessageObject.textLayoutBlocks.get(a);
            float y = textY + block.textYOffset;
            if (intersect(y, y + currentMessageObject.blockHeight, position, position + height)) {
                if (newFirst == -1) {
                    newFirst = a;
                }
                newLast = a;
                newCount++;
            } else if (y > position) {
                break;
            }
        }

        if (lastVisibleBlockNum != newLast || firstVisibleBlockNum != newFirst || totalVisibleBlocksCount != newCount) {
            lastVisibleBlockNum = newLast;
            firstVisibleBlockNum = newFirst;
            totalVisibleBlocksCount = newCount;
            invalidate();
        }
    }
    
    private boolean intersect(float left1, float right1, float left2, float right2) {
        if (left1 <= left2) {
            return right1 >= left2;
        }
        return left1 <= right2;
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
        avatarImage.draw(canvas, avatarImage.imageX, avatarImage.imageY, Utilities.dp(50), Utilities.dp(50));
        canvas.save();
//        int state = buttonState;
//        if (!currentMessageObject.messageOwner.out) {
//            state += 4;
//            timePaint.setColor(0xffa1aab3);
//        } else {
//            timePaint.setColor(0xff70b15c);
//        }
        
        if (currentMessageObject == null || currentMessageObject.textLayoutBlocks == null || currentMessageObject.textLayoutBlocks.isEmpty() || firstVisibleBlockNum < 0) {
            return;
        }

        if (currentMessageObject.messageOwner.out) {
            textX = layoutWidth - backgroundWidth + Utilities.dp(10);
            textY = Utilities.dp(10) + namesOffset;
        } else {
            textX = Utilities.dp(19) + (chat ? Utilities.dp(52) : 0);
            textY = Utilities.dp(10) + namesOffset;
        }
        
        textX += Utilities.dp(53);

        for (int a = firstVisibleBlockNum; a <= lastVisibleBlockNum; a++) {
            if (a >= currentMessageObject.textLayoutBlocks.size()) {
                break;
            }
            MessageObject.TextLayoutBlock block = currentMessageObject.textLayoutBlocks.get(a);
            canvas.save();
            canvas.translate(textX - (int)Math.ceil(block.textXOffset), textY + block.textYOffset);
            try {
                block.textLayout.draw(canvas);
            } catch (Exception e) {
                FileLog.e("emm", e);
            }
            canvas.restore();
        }
        avatarImage.draw(canvas, avatarImage.imageX, avatarImage.imageY, Utilities.dp(50), Utilities.dp(50));
    }
    public interface MTouchEnevt{
    	public void listenerTouch();
    }
}

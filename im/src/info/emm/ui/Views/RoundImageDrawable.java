package info.emm.ui.Views;

import android.graphics.Bitmap;  
import android.graphics.Canvas;  
import android.graphics.ColorFilter;  
import android.graphics.Paint;  
import android.graphics.PixelFormat;  
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;  
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;  
  
public class RoundImageDrawable extends Drawable  
{  
  
    private Paint mPaint;  
    private Bitmap mBitmap;  
  
    private RectF rectF;
  
    
    public RoundImageDrawable(Bitmap bitmap)  
    {	
        mBitmap = bitmap;
        mPaint = new Paint();
    } 
    @Override  
    public void setBounds(int left, int top, int right, int bottom)  
    {  
       super.setBounds(left, top, right, bottom);  
       rectF = new RectF(left, top, right, bottom);
    }
  
    @Override  
    public void draw(Canvas canvas)  
    {  
        //canvas.drawRoundRect(rectF, 60, 60, mPaint);
    	Bitmap bitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        int w = (int)rectF.width();
        int h = (int)rectF.height();
        int radius = 0; 
        radius = (w < h ? w : h) / 2;  
        Bitmap roundBitmap = getCroppedRoundBitmap(bitmap, radius);        
    	canvas.drawBitmap(roundBitmap, rectF.left,rectF.top, null);
    }  
    public Bitmap getCroppedRoundBitmap(Bitmap bmp, int radius) { 
        Bitmap scaledSrcBmp; 
        int diameter = radius * 2; 
        // Ϊ�˷�ֹ��߲���ȣ����Բ��ͼƬ���Σ���˽�ȡ�������д����м�λ������������ͼƬ 
        int bmpWidth = bmp.getWidth(); 
        int bmpHeight = bmp.getHeight(); 
        int squareWidth = 0, squareHeight = 0; 
        int x = 0, y = 0; 
        Bitmap squareBitmap; 
        if (bmpHeight > bmpWidth) {// �ߴ��ڿ� 
            squareWidth = squareHeight = bmpWidth; 
            x = 0; 
            y = (bmpHeight - bmpWidth) / 2; 
            // ��ȡ������ͼƬ 
            squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth, squareHeight); 
        } else if (bmpHeight < bmpWidth) {// ����ڸ� 
            squareWidth = squareHeight = bmpHeight; 
            x = (bmpWidth - bmpHeight) / 2; 
            y = 0; 
            squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth,squareHeight); 
        } else { 
            squareBitmap = bmp; 
        } 
        if (squareBitmap.getWidth() != diameter || squareBitmap.getHeight() != diameter) { 
            scaledSrcBmp = Bitmap.createScaledBitmap(squareBitmap, diameter,diameter, true); 
        } else { 
            scaledSrcBmp = squareBitmap; 
        } 
        Bitmap output = Bitmap.createBitmap(scaledSrcBmp.getWidth(), 
                scaledSrcBmp.getHeight(),  
                Config.ARGB_8888); 
        Canvas canvas = new Canvas(output); 
   
        Paint paint = new Paint(); 
        Rect rect = new Rect(0, 0, scaledSrcBmp.getWidth(),scaledSrcBmp.getHeight()); 
   
        paint.setAntiAlias(true); 
        paint.setFilterBitmap(true); 
        paint.setDither(true); 
        canvas.drawARGB(0, 0, 0, 0); 
        canvas.drawCircle(scaledSrcBmp.getWidth() / 2, 
                scaledSrcBmp.getHeight() / 2,  
                scaledSrcBmp.getWidth() / 2, 
                paint); 
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN)); 
        canvas.drawBitmap(scaledSrcBmp, rect, rect, paint); 
        bmp = null; 
        squareBitmap = null; 
        scaledSrcBmp = null; 
        return output; 
    } 
    @Override  
    public int getIntrinsicWidth()  
    {  
        return mBitmap.getWidth();  
    }  
  
    @Override  
    public int getIntrinsicHeight()  
    {  
        return mBitmap.getHeight();  
    }  
  
    @Override  
    public void setAlpha(int alpha)  
    {  
        mPaint.setAlpha(alpha);  
    }  
  
    @Override  
    public void setColorFilter(ColorFilter cf)  
    {  
        mPaint.setColorFilter(cf);  
    }  
  
    @Override  
    public int getOpacity()  
    {  
        return PixelFormat.TRANSLUCENT;  
    }  
  
}  

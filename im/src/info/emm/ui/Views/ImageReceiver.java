/*
 * This is the source code of Emm for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.ui.Views;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import info.emm.messenger.FileLoader;
import info.emm.messenger.FileLog;
import info.emm.messenger.TLRPC;
import info.emm.utils.Utilities;

import java.lang.ref.WeakReference;

public class ImageReceiver {
	private TLRPC.FileLocation last_path = null;
	private String last_httpUrl = null;
	private String last_filter = null;
	private Drawable last_placeholder = null;
	private int last_size = 0;
	private String currentPath = null;
	private boolean isPlaceholder = false;
	private Drawable currentImage = null;
	public Integer TAG = null;
	public WeakReference<View> parentView = null;
	public int imageX = 0, imageY = 0, imageW = 0, imageH = 0;
	private float imgW = 0,imgH = 0;//图片真实高宽
	public void setImage(TLRPC.FileLocation path, String filter, Drawable placeholder) {
		setImage(path, null, filter, placeholder, 0);
	}

	public void setImage(TLRPC.FileLocation path, String filter, Drawable placeholder, int size) {
		setImage(path, null, filter, placeholder, size);
	}

	public void setImage(String path, String filter, Drawable placeholder) {
		setImage(null, path, filter, placeholder, 0);
	}

	public void setImage(TLRPC.FileLocation path, String httpUrl, String filter, Drawable placeholder, int size) {
		if ((path == null && httpUrl == null) || (path != null && !(path instanceof TLRPC.TL_fileLocation) && !(path instanceof TLRPC.TL_fileEncryptedLocation))) {
			recycleBitmap(null);
			currentPath = null;
			isPlaceholder = true;
			last_path = null;
			last_httpUrl = null;
			last_filter = null;
			last_placeholder = placeholder;
			last_size = 0;
			currentImage = null;
			FileLoader.getInstance().cancelLoadingForImageView(this);
			return;
		}
		String key;
		if (path != null) {
			key = Utilities.MD5(path.http_path_img);//path.volume_id + "_" + path.local_id;
		} else {
			key = Utilities.MD5(httpUrl);
		}
		if (filter != null) {
			key += "@" + filter;
		}
		Bitmap img;
		if (currentPath != null) {
			if (currentPath.equals(key)) {
				return;
			} else {
				img = FileLoader.getInstance().getImageFromMemory(path, httpUrl, this, filter, true);
				recycleBitmap(img);
			}
		} else {
			img = FileLoader.getInstance().getImageFromMemory(path, httpUrl, this, filter, true);
		}
		currentPath = key;
		last_path = path;
		last_httpUrl = httpUrl;
		last_filter = filter;
		last_placeholder = placeholder;
		last_size = size;
		if (img == null) {
			isPlaceholder = true;
			FileLoader.getInstance().loadImage(path, httpUrl, this, filter, true, size);
		} else {
			setImageBitmap(img, currentPath);
		}
	}

	public void setImageBitmap(Bitmap bitmap, String imgKey) {
		if (currentPath == null || !imgKey.equals(currentPath)) {
			return;
		}
		imgW = bitmap.getWidth();
		imgH = bitmap.getHeight();
		isPlaceholder = false;
		FileLoader.getInstance().incrementUseCount(currentPath);    
		currentImage = new BitmapDrawable(null, bitmap);                
		if (parentView.get() != null) {
			if (imageW != 0) {
				parentView.get().invalidate(imageX, imageY, imageX + imageW, imageY + imageH);
			} else {
				parentView.get().invalidate();
			}
		}
	}

	public void setImageBitmap(Bitmap bitmap) {
		currentPath = null;
		last_path = null;
		last_httpUrl = null;
		last_filter = null;
		last_placeholder = null;
		last_size = 0;
		FileLoader.getInstance().cancelLoadingForImageView(this);
		if (bitmap != null) {
			recycleBitmap(null);
			currentImage = new BitmapDrawable(null, bitmap);
		}
	}

	public void setImageBitmap(Drawable bitmap) {
		currentPath = null;
		last_path = null;
		last_httpUrl = null;
		last_filter = null;
		last_placeholder = null;
		last_size = 0;
		FileLoader.getInstance().cancelLoadingForImageView(this);
		if (bitmap != null) {
			recycleBitmap(null);
			currentImage = bitmap;
		}
	}

	public void clearImage() {
		recycleBitmap(null);
	}

	private void recycleBitmap(Bitmap newBitmap) {
		if (currentImage == null || isPlaceholder) {
			return;
		}
		if (currentImage instanceof BitmapDrawable) {
			Bitmap bitmap = ((BitmapDrawable)currentImage).getBitmap();
			if (bitmap != null && bitmap != newBitmap) {
				if (currentPath != null) {
					boolean canDelete = FileLoader.getInstance().decrementUseCount(currentPath);
					if (!FileLoader.getInstance().isInCache(currentPath)) {
						if (FileLoader.getInstance().runtimeHack != null) {
							FileLoader.getInstance().runtimeHack.trackAlloc(bitmap.getRowBytes() * bitmap.getHeight());
						}
						if (canDelete) {
							currentImage = null;
							if (Build.VERSION.SDK_INT < 11) {
								bitmap.recycle();
							}
						}
					} else {
						currentImage = null;
					}
					currentPath = null;
				}
			}
		}
	}

	public void draw(Canvas canvas, int x, int y, int w, int h) {
		try {
			if (currentImage != null) {//有头像
				float scale = imgH > 0?(imgW / imgH):1;
				int xx = x;
				int yy = y;
				int width = w;
				int height = h;
				if (scale > 1) {
					height = (int)(h*(imgW/w));
					yy = yy + (h - height)/2;
				}else {
					width = (int)(w*scale);
					xx = xx + (w - width)/2;
				}
				Bitmap bitmap = ((BitmapDrawable)currentImage).getBitmap();//drawable-->bitmap
				Bitmap b = bitmap.copy(Bitmap.Config.ARGB_8888, true);
				int radius  =(int) (width < height ? width : height) / 2;
				Bitmap c =getCroppedRoundBitmap(b, radius);
				canvas.drawBitmap(c, xx, yy, null);
				//				currentImage.setBounds(xx, yy, xx + width, yy + height);
				//				currentImage.draw(canvas);
			} 
			else if (last_placeholder != null) //没有头像
			{
				Bitmap bitmap = ((BitmapDrawable)last_placeholder).getBitmap();//drawable-->bitmap
				Bitmap b = bitmap.copy(Bitmap.Config.ARGB_8888, true);
				int radius  =(int) (w < h ? w : h) / 2;
				Bitmap c =getCroppedRoundBitmap(b, radius);
				canvas.drawBitmap(c, x, y, null);
				//				last_placeholder.setBounds(x, y, x + w, y + h);
				//				last_placeholder.draw(canvas);  
			}
		} catch (Exception e) {
			if (currentPath != null) {
				FileLoader.getInstance().removeImage(currentPath);
				currentPath = null;
			}
			setImage(last_path, last_httpUrl, last_filter, last_placeholder, last_size);
			FileLog.e("emm", e);
		}
	}
	/**
	 * 转成圆形图片
	 * @param bmp
	 * @param radius
	 * @return
	 */
	public Bitmap getCroppedRoundBitmap(Bitmap bmp, int radius) { 
		Bitmap scaledSrcBmp; 
		int diameter = radius * 2; 
		// 为了防止宽高不相等，造成圆形图片变形，因此截取长方形中处于中间位置最大的正方形图片 
		int bmpWidth = bmp.getWidth(); 
		int bmpHeight = bmp.getHeight(); 
		int squareWidth = 0, squareHeight = 0; 
		int x = 0, y = 0; 
		Bitmap squareBitmap; 
		if (bmpHeight > bmpWidth) {// 高大于宽 
			squareWidth = squareHeight = bmpWidth; 
			x = 0; 
			y = (bmpHeight - bmpWidth) / 2; 
			// 截取正方形图片 
			squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth, squareHeight); 
		} else if (bmpHeight < bmpWidth) {// 宽大于高 
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
		canvas.drawCircle(scaledSrcBmp.getWidth() / 2, scaledSrcBmp.getHeight() / 2,  scaledSrcBmp.getWidth() / 2, paint); 
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN)); //遮罩
		canvas.drawBitmap(scaledSrcBmp, rect, rect, paint); 
		bmp = null; 
		squareBitmap = null; 
		scaledSrcBmp = null; 
		return output; 
	} 
}

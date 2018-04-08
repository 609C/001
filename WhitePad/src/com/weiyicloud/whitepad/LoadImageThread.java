package com.weiyicloud.whitepad;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;

public class LoadImageThread extends Thread {

	public ShareDoc msdp;

	public int nPage;

	public Context appContext;

	public SharePadMgr m_PadMgr;

	static public int	LOAD_IMAGE = 100;

	public static byte[] getBytes(InputStream is) throws IOException {
		ByteArrayOutputStream outstream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024]; // ������װ
		int len = -1;
		while ((len = is.read(buffer)) != -1) {
			outstream.write(buffer, 0, len);
		}
		outstream.close();
		// �ر���һ��Ҫ�ǵá�
		return outstream.toByteArray();
	}

	@Override
	public void run() {	

		Bitmap bit = null;
		try {

			String httpurl = msdp.fileUrl.toString();
			int pos = httpurl.lastIndexOf('.');

			String strFinal = String.format("%s-%d%s",httpurl.substring(0, pos),nPage,httpurl.substring(pos));
			//String StrSaveFileDir = "";
			strFinal = strFinal.replace("http://http://", "http://");
			String StrSaveFile = "";

			URL url = new URL(strFinal);
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			InputStream is = urlConn.getInputStream();

			byte[] data = getBytes( is);

			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inPreferredConfig = Config.RGB_565;
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(data, 0,data.length, opts);
			int imageHeight = opts.outHeight;
			int imageWidth = opts.outWidth;

			int nScale = imageHeight * imageWidth/(1920*1080);

			if(nScale>1){
				opts.inSampleSize = nScale;
			}
			opts.inJustDecodeBounds = false;

			// btcurrentImage= BitmapFactory.decodeFile(strPath, opts);

			bit  = BitmapFactory.decodeByteArray(data, 0,data.length,opts);

			File dirFile = m_PadMgr.mAppContext.getExternalCacheDir(); 
			if(dirFile==null){  
				dirFile = m_PadMgr.mAppContext.getCacheDir();
			}
			if(dirFile==null){
				m_PadMgr.loadImage(msdp,nPage,bit);
				return;
			}
			String strSaveDir = dirFile.getPath() + "/" + "ShareImage";
			File dirFileShare = new File(strSaveDir);

			if(!dirFileShare.isDirectory()||!dirFileShare.exists()){
				dirFileShare.mkdir();
			}

			StrSaveFile = dirFileShare.getPath() + strFinal.substring(strFinal.lastIndexOf("/"));
			File myCaptureFile = new File(StrSaveFile);  	
			if(myCaptureFile.length()==0)
			{
				if(bit!=null){
					myCaptureFile.createNewFile();
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));  
					bit.compress(Bitmap.CompressFormat.JPEG, 80, bos);  
					bos.flush();  
					bos.close();  
					if(!msdp.docPageImages.containsKey(nPage))
						msdp.docPageImages.put(nPage, StrSaveFile);
				}
				is.close(); 
			}else{
				if(!msdp.docPageImages.containsKey(nPage))
					msdp.docPageImages.put(nPage, StrSaveFile);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		m_PadMgr.loadImage(msdp,nPage,bit);
	}
	public boolean SaveFile(String StrfilePath ,InputStream is){
		return false;
	}
}

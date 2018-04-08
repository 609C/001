package com.weiyicloud.whitepad;



import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestHandle;

import java.io.File;


@SuppressLint("DefaultLocale")
public class FileDownLoad {

	public FileDownLoadDelegate delegate;
	//public int state = 0;
	//public ShareDoc msdp;
	//private String httpUrl;
	//private File cacheFileFinal;	
	private static AsyncHttpClient client = new AsyncHttpClient();
	public static PersistentCookieStore myCookieStore = null;
	public static int externalCacheNotAvailableState = 0;
	private RequestHandle request = null;
	private static volatile FileDownLoad Instance = null;

	public static interface FileDownLoadDelegate {
		public abstract void didFinishLoadingFile(ShareDoc sdp,File file);
		public abstract void didFailedLoadingFile();
		public abstract void didChangedLoadProgress(float progress);
	}

	public static FileDownLoad getInstance() {
		FileDownLoad localInstance = Instance;
		if (localInstance == null) {
			synchronized (FileDownLoad.class) {
				localInstance = Instance;
				if (localInstance == null) {
					Instance = localInstance = new FileDownLoad();
				}
			}
		}
		return localInstance;
	}

	public void start(ShareDoc sdp,Context context)
	{
		/*if (state != 0) {
			return;
		}
		state = 1;*/

		if(myCookieStore != null)
			client.setCookieStore(myCookieStore);
		
		/*ShareDoc msdp=null;
		try {
			msdp = (ShareDoc) sdp.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}*/
		String httpUrlfinal;
		String httpurl = sdp.fileUrl.toString();
		int pos = httpurl.lastIndexOf('.');
		String strFinal = String.format("%s-%d%s",httpurl.substring(0, pos),sdp.currentPage,httpurl.substring(pos));
		httpUrlfinal = strFinal.replace("http://http://", "http://");

		String fileName = httpUrlfinal;
		fileName =fileName.substring(fileName.lastIndexOf("/")+1);
		FileLog.d("emm", "image file address="+httpUrlfinal);
		File cacheFileFinal = new File(getCacheDir(context),fileName);

		/**
		 * 清除缓存
		 */
		File dirFile = context.getExternalCacheDir();
		if(dirFile==null){
			dirFile = context.getCacheDir();
		}
		if(dirFile!=null){
			String strSaveDir = dirFile.getPath() + "/" + "cancleImage";
			File dirFileShare = new File(strSaveDir);
			if(dirFileShare.exists()){
				dirFileShare.delete();
			}
		}

		if (httpUrlfinal != null)
		{
			startDownloadHTTPRequest(cacheFileFinal,sdp,httpUrlfinal);
		}
	}

	public class AsyncHandler extends FileAsyncHttpResponseHandler {

		public AsyncHandler(File name)
		{
			super(name);
		}
		public RequestHandle innerRequest = null;
		public ShareDoc msdp=null;
		public void setShareDoc(ShareDoc sb)
		{
			try {
				msdp = (ShareDoc)sb.clone();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		@Override
		public void onSuccess(java.io.File file) {
			final File jpgFilePath = file;
			FileLog.d("emm", "msdp.currentPage="+msdp.currentPage);
			try {
				Utitlties.stageQueue.postRunnable(new Runnable() {
					@Override
					public void run() {
						try
						{
							FileLog.d("emm", "msdp.currentPage="+msdp.currentPage);
							FileLog.d("emm", "msdp.fileName="+msdp.fileName);
							delegate.didFinishLoadingFile(msdp,jpgFilePath);


//							onFinishLoadingFile();
						} catch (Exception e) {
							//delegate.didFailedLoadingFile(FileLoadOperation.this);
						}
					}
				});
			}
			catch (Exception e)
			{
				Utitlties.stageQueue.postRunnable(new Runnable() {
					@Override
					public void run()
					{
						//cleanup();
						delegate.didFailedLoadingFile();
					}
				});
			}

		}

		@Override
		public void onFailure(Throwable error, String content) {

			Utitlties.stageQueue.postRunnable(new Runnable() {
				@Override
				public void run()
				{
					//cleanup();
					delegate.didFailedLoadingFile();
				}
			});

			error.printStackTrace();
		}
		@Override
		public void onProgress(int bytesWritten, int totalSize)
		{
			final int totalBytesCount = totalSize;
			final int progress = bytesWritten;
			Utitlties.stageQueue.postRunnable(new Runnable() {
				@Override
				public void run() {
					if (totalBytesCount > 0) {
						delegate.didChangedLoadProgress(Math.min(1.0f, (float)(progress) / (float)totalBytesCount));
					}
				}
			});
		}
		@Override
		public void onFinish() {
			FileLog.d("emm", "onFinish"+msdp.fileUrl);
			super.onFinish();
		}

	}

	/**
	 * @param cacheFileFinal
	 * @param msdp
	 * @param httpUrl
	 */
	private void startDownloadHTTPRequest(File cacheFileFinal,ShareDoc msdp,String httpUrl) {
		//if (state != 1) {
		//return;
		//}

		if (request != null)
		{
			request.cancel(false);
			FileLog.d("emm", "2+"+request.toString()+httpUrl);
		}

		AsyncHandler handler = new AsyncHandler(cacheFileFinal);
		handler.setShareDoc(msdp);
		request = client.get(httpUrl, handler);
		FileLog.d("emm", "1+"+request.toString()+httpUrl);
		handler.innerRequest = request;
	}
	/**
	 * 缓存
	 * @param context
	 * @return
	 */
	public static File getCacheDir(Context context) {
		if (externalCacheNotAvailableState == 1 || externalCacheNotAvailableState == 0 && Environment.getExternalStorageState().startsWith(Environment.MEDIA_MOUNTED)) {
			externalCacheNotAvailableState = 1;
			return context.getExternalCacheDir();
		}
		externalCacheNotAvailableState = 2;
		return context.getCacheDir();
	}
}

package info.emm.utils;



import com.utils.FileLog;

import info.emm.messenger.MessagesStorage;
import info.emm.messenger.UserConfig;
import info.emm.ui.ApplicationLoader;

import java.io.File;

import com.loopj.android.http.AsyncHttpClient;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;


@SuppressLint("DefaultLocale")
public class FileDownLoad {

	public FileDownLoadDelegate delegate;
	public int state = 0;
	public String uri;// = "http://192.168.0.99/Public/img/bottom-logo.jpg";
	private String httpUrl;
	public File cacheFileFinal;	
	private AsyncHttpClient client = new AsyncHttpClient();
	//public static PersistentCookieStore myCookieStore = null;
	public static int externalCacheNotAvailableState = 0;

	public static interface FileDownLoadDelegate {
		public abstract void didFinishLoadingFile(String uri,File file);
		public abstract void didFailedLoadingFile();
		public abstract void didChangedLoadProgress(float progress);
	}

	public FileDownLoad(String uri,Context context){
		this.uri = uri;
		//		String httpurl = msdp.fileUrl.toString();
		int pos = uri.lastIndexOf('.');
		String strFinal = String.format("%s%s",uri.substring(0, pos),uri.substring(pos));		
		if(UserConfig.isPublic){

		}else{
			String privateHttp = UserConfig.privateWebHttp;
			if(strFinal.startsWith("http://")){
				httpUrl = privateHttp + strFinal;
			}else{
				httpUrl ="http://" + privateHttp + strFinal;
			}
		}


		String fileName = httpUrl;
		fileName =fileName.substring(fileName.lastIndexOf("hls/")+4).toString();
//		String dd = fileName.split("/")[0];
//		String ss =fileName.split("/")[1];
//		String sd =fileName.split("/")[2];
		String name = fileName.split("/")[0]+"_"+fileName.split("/")[1]+"_"+fileName.split("/")[2];
		cacheFileFinal = new File(getCacheDir(context),name);	

		/**
		 * «Â≥˝ª∫¥Ê
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
	}
	public void start() {
		/*if (state != 0) {
			return;
		}
		state = 1;*/

		//if(ApplicationLoader.myCookieStore != null)
			//client.setCookieStore(ApplicationLoader.myCookieStore);

		if (httpUrl != null) {
			startDownloadHTTPRequest();
		}
	}

	private void startDownloadHTTPRequest() {
		//if (state != 1) {
		//return;
		//}	  
		Log.e("TAG", "httpUrl="+httpUrl);
		client.get(httpUrl, new FileAsyncHttpResponseHandler(cacheFileFinal) {

			@Override
			public void onSuccess(java.io.File file) {
				try {
					Utitlties.stageQueue.postRunnable(new Runnable() {
						@Override
						public void run() {
							try 
							{					
								delegate.didFinishLoadingFile(uri,cacheFileFinal);
								Log.e("TAG", "success....."+httpUrl);
							} catch (Exception e) {
								e.printStackTrace();

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
							FileLog.e("emm", "Exception didFailedLoadingFile url="+httpUrl);
							Log.e("TAG", httpUrl);
							delegate.didFailedLoadingFile();
						}
					});
				}
			}

			@Override
			public void onFailure(Throwable e) {
				Utitlties.stageQueue.postRunnable(new Runnable() {
					@Override
					public void run() 
					{
						FileLog.e("emm", "onFailure didFailedLoadingFile url="+httpUrl);
						//cleanup();
						Log.e("TAG", "failed....."+httpUrl);
						delegate.didFailedLoadingFile();
					}
				});
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
							Log.e("TAG", "progress.....");
							delegate.didChangedLoadProgress(Math.min(1.0f, (float)(progress) / (float)totalBytesCount));
						}
					}
				});
			}
		});
		//		client.setTimeout(6 * 1000); 
	}
	/**
	 * ª∫¥Ê
	 * @param context
	 * @returnŒ“
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

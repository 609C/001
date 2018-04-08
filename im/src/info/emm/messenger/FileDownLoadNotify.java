package info.emm.messenger;

import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import info.emm.messenger.TLRPC.PhotoSize;
import info.emm.messenger.TLRPC.TL_DirectPlayBackList;
import info.emm.ui.ApplicationLoader;
import info.emm.utils.FileDownLoad;
import info.emm.utils.FileDownLoad.FileDownLoadDelegate;
import info.emm.utils.Utilities;
public class FileDownLoadNotify implements FileDownLoadDelegate{
	public FileDownLoadNotify(){

	}
	ArrayList<TLRPC.TL_DirectPlayBackList> directList = null;

	public void StartDownLoad(){
		ConcurrentHashMap<String, TLRPC.TL_DirectPlayBackList> imgMap= MessagesController.getInstance().directImgMap;
		for (ConcurrentHashMap.Entry<String, TLRPC.TL_DirectPlayBackList> entry : imgMap.entrySet()) {
			TLRPC.TL_DirectPlayBackList info =  entry.getValue();
			File file = new File(info.httpUrl);
			if(!file.exists())//qxm add change  表示下载的图片不存在的时候再去下载
			{
				FileDownLoad fileDownload = new FileDownLoad(info.livevideoico,ApplicationLoader.getContext());
				fileDownload.delegate = this;
				Log.e("TAG", "fileDownLoad"+info.livevideoico);
				fileDownload.start();

			}
		}
	}

	@Override
	public void didFinishLoadingFile(String httpurl, File file)
	{
		final String httpurlNew = httpurl;
		final File fileNew = file;
		Log.e("TAG", "didFinishLoadingFil...");
		Utilities.RunOnUIThread(new Runnable() {
			@Override
			public void run() {
				Uri networkUri=null;
				TLRPC.TL_photo photo = MessagesController.getInstance().generatePhotoSizes(fileNew.getAbsolutePath(), networkUri);
				ArrayList<PhotoSize> sizes = photo.sizes;
				for( int i=0;i<sizes.size();i++)
				{
					PhotoSize size = sizes.get(i);
					if(size.type == "m")
					{

						if( MessagesController.getInstance().directImgMap.entrySet() != null && !MessagesController.getInstance().directImgMap.entrySet().isEmpty()){
							for (Entry<String, TL_DirectPlayBackList> entry : MessagesController.getInstance().directImgMap.entrySet()) {
								TLRPC.TL_DirectPlayBackList directImg = entry.getValue();
								if(httpurlNew.equals(directImg.livevideoico)){
									TLRPC.TL_DirectPlayBackList info = new TLRPC.TL_DirectPlayBackList();
									String fileName = size.location.volume_id + "_" + size.location.local_id + ".jpg";
									info.mId = directImg.mId;
									info.duration = directImg.duration;
									info.startTime = directImg.startTime;
									info.title = directImg.title;
									info.livevideoico = httpurlNew;
									info.livevideopwd = directImg.livevideopwd;
									info.livevideoid = directImg.livevideoid;
									info.httpUrl = directImg.httpUrl;
									info.createuserid = directImg.createuserid;
									MessagesController.getInstance().processBroadCastImg(info,1);
								}
							}
						}
					}
				}
			}
		});
	}
	@Override
	public void didFailedLoadingFile() {
		//m_fileDownload.delegate = this;
		//m_fileDownload.start();
		//Toast.makeText(getActivity(), "失败", Toast.LENGTH_LONG).show();

	}
	@Override
	public void didChangedLoadProgress(float progress) {
		// TODO Auto-generated method stub
	}

}

package com.weiyicloud.whitepad;

import java.io.File;
import java.io.UnsupportedEncodingException;
//import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONObject;


import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;



public class UploadFile {
	public UpLoadFileDelegate delegate;
	public int state = 0;
	private String httpUrl;
	private String remoteFilename;
	private long currentFileId;
	private String uploadingFilePath;
	private String uploadserial;
	private String uploaduserid;
	private String uploadsender;
	
	RequestParams fileParams = null;
	private static AsyncHttpClient client = new AsyncHttpClient();
	public static PersistentCookieStore myCookieStore = null;
	public static volatile Context applicationContext = null;
	private int count = 0;
	
	
	public static interface UpLoadFileDelegate {
        public abstract void didFinishUploadingFile(UploadFile operation,String fileName, String result);
        public abstract void didFailedUploadingFile(UploadFile operation,int code);
        public abstract void didChangedUploadProgress(UploadFile operation, float progress);
    }
	
	public void UploadOperation(String url,Context appContext) 
    {
        httpUrl = url;
        applicationContext = appContext;
        myCookieStore = new PersistentCookieStore(applicationContext);
    }
	

	public void start() {
        if (state != 0) {
            return;
        }
        state = 1;
        
        //sam
    	if(myCookieStore != null)
    		client.setCookieStore(myCookieStore);
        
        if (httpUrl != null) {
        	startUploadHTTPRequest();
        }
    }

    public void cancel() {
      
    }
    
    private void startUploadHTTPRequest()
    {
        if (state != 1) {
            return;
        }
        
        if (client != null) {
            try {
        		client.post(httpUrl, fileParams, new AsyncHttpResponseHandler()
            	{
        			@Override
        			public void onSuccess(String response)
        			{
        				try {       
        					final String msg = response;
        					
        					
        					Utitlties.stageQueue.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                	try {
                                		JSONObject jsbody = new JSONObject(msg);
                						int nRet = jsbody.getInt("result");
                						if (nRet == 0) {
                							delegate.didFinishUploadingFile(UploadFile.this,remoteFilename,msg);
                						}
                						else
                						{
                							delegate.didFailedUploadingFile(UploadFile.this, nRet);
                						}
									} catch (Exception e) {
										// TODO: handle exception
									}
                                }
                        	});
        				} catch (Exception e) {  
        					
        					Utitlties.stageQueue.postRunnable(new Runnable() {
                                @Override
                                public void run() {
		                            delegate.didFailedUploadingFile(UploadFile.this,999);
                                }
        					});
                            return;
        				}
        			}

        			@Override
        			public void onFailure(Throwable error, String content)
        			{
        				Utitlties.stageQueue.postRunnable(new Runnable() {
                            @Override
                            public void run() {
		                        delegate.didFailedUploadingFile(UploadFile.this,999);
                            }
        				});
                        return;
        			}
        			
        			@Override
        			public void onProgress(int bytesWritten, int totalSize)
        			{
        				final int totalBytesCount = totalSize;
                    	final int progress = bytesWritten;
                    	Utitlties.stageQueue.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                            		delegate.didChangedUploadProgress(UploadFile.this, (float)progress / (float)totalBytesCount);
                            	
                            }
        				});
        			}
        		});
            } catch (Exception e) {
                FileLog.e("emm", e);
                delegate.didFailedUploadingFile(UploadFile.this,999);
                return;
            }
        }
    }
    public void packageFile(String path,String strserial,int peerid,String username)
    {
    	count++;
    	uploadingFilePath = path;
    	this.remoteFilename = path.substring(path.lastIndexOf("/") + 1);
		RequestParams params = new RequestParams();
		try {
			uploadserial = strserial;//MeetingSession.getInstance().getM_strMeetingID();
			uploaduserid = ""+peerid;//MeetingSession.getInstance().getUserMgr().getSelfUser().getPeerID();
			uploadsender = username;//MeetingSession.getInstance().getM_strUserName();
			String fileOldName = path.substring(path.lastIndexOf("/") + 1);
			String fileType = path.substring(path.lastIndexOf(".") + 1);
			
			File file = new File(path);
			
			params.put("filedata",file);
			params.put("serial",uploadserial);
			params.put("userid", uploaduserid);
			params.put("sender", uploadsender);
			params.put("conversion", "1");
			params.put("isconversiondone", "0");
			params.put("fileoldname", fileOldName);
			params.put("filename", path);
			params.put("filetype", fileType);
			params.put("alluser", "1");
			
			this.fileParams = params;
		} catch (Exception e) {
			e.printStackTrace();
		}            	
		
    }
	public String getPath()
	{
		return uploadingFilePath;
	}
	public String getserial() {
		return uploadserial;
	}


	public String getuserid() {
		return uploaduserid;
	}


	public String getsender() {
		return uploadsender;
	}
	public int getCount()
	{
		return count;
	}
}

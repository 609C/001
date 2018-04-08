package com.weiyicloud.whitepad;

public interface DocInterface {
	void UploadingFileFinish(int fileid,int pagenum,String filename,String swfpath);
	void UploadingFileFailed(int operationcount);
	void ChangedUploadProgress(int progress);
	void DelmeetingFile(int id,int pageid,String filename,String fileurl);
}

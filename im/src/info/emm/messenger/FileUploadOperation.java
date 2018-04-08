/*
 * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.messenger;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import info.emm.ui.ApplicationLoader;
import info.emm.utils.Utilities;

//sam

public class FileUploadOperation {
    private int uploadChunkSize = 1024 * 32;
    private String uploadingFilePath;
    public int state = 0;
    private byte[] readBuffer;
    public FileUploadOperationDelegate delegate;
    private long requestToken = 0;
    private int currentPartNum = 0;
    private long currentFileId;
    private boolean isLastPart = false;
    private long totalFileSize = 0;
    private int totalPartsCount = 0;
    private long currentUploaded = 0;
    private byte[] key;
    private byte[] iv;
    private int fingerprint;
    private boolean isBigFile = false;
    FileInputStream stream;
    MessageDigest mdEnc = null;

    //sam
    private String httpUrl;
    private String remoteFilename;
    private static AsyncHttpClient client = new AsyncHttpClient();
    private RequestHandle hReq;
    //为了图象转换，只在设置组的时候和个人头像的时候生效，在聊天室传输图象不起作用
    public int width = 100;
    public int height = 100;
    public int iscrop = 0;
    public int groupid = 0;
    public int userid=0;
    public int companyid=0;
    public int randomfile=1;


    public static interface FileUploadOperationDelegate {
        public abstract void didFinishUploadingFile(FileUploadOperation operation, TLRPC.InputFile inputFile, TLRPC.InputEncryptedFile inputEncryptedFile, String url);
        public abstract void didFailedUploadingFile(FileUploadOperation operation);
        public abstract void didChangedUploadProgress(FileUploadOperation operation, float progress);
    }

    public FileUploadOperation(String location, byte[] keyarr, byte[] ivarr) {
        uploadingFilePath = location;
        if (ivarr != null && keyarr != null) {
            iv = new byte[32];
            key = keyarr;
            System.arraycopy(ivarr, 0, iv, 0, 32);
            try {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
                byte[] arr = new byte[64];
                System.arraycopy(key, 0, arr, 0, 32);
                System.arraycopy(iv, 0, arr, 32, 32);
                byte[] digest = md.digest(arr);
                byte[] fingerprintBytes = new byte[4];
                for (int a = 0; a < 4; a++) {
                    fingerprintBytes[a] = (byte)(digest[a] ^ digest[a + 4]);
                }
                fingerprint = Utilities.bytesToInt(fingerprintBytes);
            } catch (Exception e) {
                FileLog.e("emm", e);
            }
        }
        currentFileId = MessagesController.random.nextLong();
        try {
            mdEnc = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            FileLog.e("emm", e);
        }
    }

    public FileUploadOperation(String location, String url, String filename)
    {
        httpUrl = url;
        remoteFilename = filename;
        uploadingFilePath = location;
        currentFileId = MessagesController.random.nextLong();
        try {
            mdEnc = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            FileLog.e("emm", e);
        }
    }

    public void start() {
        if (state != 0) {
            return;
        }
        state = 1;

        //sam
        if(ApplicationLoader.myCookieStore != null)
            client.setCookieStore(ApplicationLoader.myCookieStore);

        if (httpUrl != null) {
            startUploadHTTPRequest();
        } else {
            startUploadRequest();
        }
    }

    public void cancel() {
        if (state != 1) {
            return;
        }
        state = 2;

        //sam
        if (httpUrl != null) {
            if (hReq != null) {
                hReq.cancel(true);
            }
        }
        cleanup();

        if (requestToken != 0) {
            ConnectionsManager.getInstance().cancelRpc(requestToken, true);
        }
        delegate.didFailedUploadingFile(this);
    }

    //sam
    private void cleanup() {
    }


    private void startUploadHTTPRequest() {
        if (state != 1) {
            return;
        }

        if (client != null) {
            try {
                RequestParams params = new RequestParams();
                if(remoteFilename != null && remoteFilename.length() != 0)
                    params.put("filename", remoteFilename);
                params.put("filename", uploadingFilePath);
                params.put("filedata", new File(uploadingFilePath));
                //只有设置组头像和个人头像才起作用，在聊天中传输图象没关系，不需要裁减
                //randomfile  ==1 表示改名字    0 表示不改名字
                params.put("randomfile", randomfile+"");
                if( iscrop ==  1)
                {
                    params.put("width", width+"");
                    params.put("height", height+"");
                    params.put("iscrop", 1+"");
                    if( userid != 0)
                        params.put("userid", ""+userid+"");
                    if( groupid !=0)
                        params.put("groupid", groupid+"");
                    if( companyid !=0)
                        params.put("companyid", companyid+"");
                }
                params.put("fromid", UserConfig.clientUserId+"");

                hReq = client.post(httpUrl, params, new AsyncHttpResponseHandler()
                {
                    @Override
                    public void onSuccess(String response)
                    {
                        try {
                            JSONTokener jsonParser = new JSONTokener(response);
                            final JSONObject ret = (JSONObject) jsonParser.nextValue();
                            final TLRPC.InputFile result = new TLRPC.TL_inputFile();
                            result.md5_checksum = String.format(Locale.US, "%32s", new BigInteger(1, mdEnc.digest()).toString(16)).replace(' ', '0');
                            result.parts = currentPartNum;
                            result.id = currentFileId;
                            result.name = uploadingFilePath.substring(uploadingFilePath.lastIndexOf("/") + 1);
                            Utilities.stageQueue.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    delegate.didFinishUploadingFile(FileUploadOperation.this, result, null, ret.optString("result"));
                                }
                            });
                        } catch (Exception e) {
                            FileLog.e("emm", e);
                            Utilities.stageQueue.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    cleanup();
                                    delegate.didFailedUploadingFile(FileUploadOperation.this);
                                }
                            });
                            return;
                        }
                    }

                    @Override
                    public void onFailure(Throwable error, String content)
                    {
                        Utilities.stageQueue.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                cleanup();
                                delegate.didFailedUploadingFile(FileUploadOperation.this);
                            }
                        });
                        return;
                    }

                    @Override
                    public void onProgress(int bytesWritten, int totalSize)
                    {
                        final int totalBytesCount = totalSize;
                        final int progress = bytesWritten;
                        Utilities.stageQueue.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                delegate.didChangedUploadProgress(FileUploadOperation.this, (float)progress / (float)totalBytesCount);
                            }
                        });
                    }
                });
            } catch (Exception e) {
                FileLog.e("emm", e);
                cleanup();
                delegate.didFailedUploadingFile(this);
                return;
            }
        }
    }

    private void startUploadRequest() {
        if (state != 1) {
            return;
        }

        TLObject finalRequest;

        try {
            if (stream == null) {
                File cacheFile = new File(uploadingFilePath);
                stream = new FileInputStream(cacheFile);
                totalFileSize = cacheFile.length();
                if (totalFileSize > 10 * 1024 * 1024) {
                    FileLog.e("emm", "file is big!");
                    isBigFile = true;
                }

                uploadChunkSize = (int)Math.max(32, Math.ceil(totalFileSize / (1024.0f * 3000)));
                if (1024 % uploadChunkSize != 0) {
                    int chunkSize = 64;
                    while (uploadChunkSize > chunkSize) {
                        chunkSize *= 2;
                    }
                    uploadChunkSize = chunkSize;
                }

                uploadChunkSize *= 1024;
                totalPartsCount = (int)Math.ceil((float)totalFileSize / (float)uploadChunkSize);
                readBuffer = new byte[uploadChunkSize];
            }

            int readed = stream.read(readBuffer);
            int toAdd = 0;
            if (key != null && readed % 16 != 0) {
                toAdd += 16 - readed % 16;
            }
            byte[] sendBuffer = new byte[readed + toAdd];
            if (readed != uploadChunkSize) {
                isLastPart = true;
            }
            System.arraycopy(readBuffer, 0, sendBuffer, 0, readed);
            if (key != null) {
                sendBuffer = Utilities.aesIgeEncryption(sendBuffer, key, iv, true, true, 0);
            }
            mdEnc.update(sendBuffer, 0, readed + toAdd);
            if (isBigFile) {
                TLRPC.TL_upload_saveBigFilePart req = new TLRPC.TL_upload_saveBigFilePart();
                req.file_part = currentPartNum;
                req.file_id = currentFileId;
                req.file_total_parts = totalPartsCount;
                req.bytes = sendBuffer;
                finalRequest = req;
            } else {
                TLRPC.TL_upload_saveFilePart req = new TLRPC.TL_upload_saveFilePart();
                req.file_part = currentPartNum;
                req.file_id = currentFileId;
                req.bytes = sendBuffer;
                finalRequest = req;
            }
            currentUploaded += readed;
        } catch (Exception e) {
            FileLog.e("emm", e);
            delegate.didFailedUploadingFile(this);
            return;
        }
        requestToken = ConnectionsManager.getInstance().performRpc(finalRequest, new RPCRequest.RPCRequestDelegate() {
            @Override
            public void run(TLObject response, TLRPC.TL_error error) {
                requestToken = 0;
                if (error == null) {
                    if (response instanceof TLRPC.TL_boolTrue) {
                        currentPartNum++;
                        delegate.didChangedUploadProgress(FileUploadOperation.this, (float)currentUploaded / (float)totalFileSize);
                        if (isLastPart) {
                            state = 3;
                            if (key == null) {
                                TLRPC.InputFile result;
                                if (isBigFile) {
                                    result = new TLRPC.TL_inputFileBig();
                                } else {
                                    result = new TLRPC.TL_inputFile();
                                    result.md5_checksum = String.format(Locale.US, "%32s", new BigInteger(1, mdEnc.digest()).toString(16)).replace(' ', '0');
                                }
                                result.parts = currentPartNum;
                                result.id = currentFileId;
                                result.name = uploadingFilePath.substring(uploadingFilePath.lastIndexOf("/") + 1);
                                delegate.didFinishUploadingFile(FileUploadOperation.this, result, null, null);
                            } else {
                                TLRPC.InputEncryptedFile result;
                                if (isBigFile) {
                                    result = new TLRPC.TL_inputEncryptedFileBigUploaded();
                                } else {
                                    result = new TLRPC.TL_inputEncryptedFileUploaded();
                                    result.md5_checksum = String.format(Locale.US, "%32s", new BigInteger(1, mdEnc.digest()).toString(16)).replace(' ', '0');
                                }
                                result.parts = currentPartNum;
                                result.id = currentFileId;
                                result.key_fingerprint = fingerprint;
                                delegate.didFinishUploadingFile(FileUploadOperation.this, null, result, null);
                            }
                        } else {
                            startUploadRequest();
                        }
                    } else {
                        delegate.didFailedUploadingFile(FileUploadOperation.this);
                    }
                } else {
                    delegate.didFailedUploadingFile(FileUploadOperation.this);
                }
            }
        }, new RPCRequest.RPCProgressDelegate() {
            @Override
            public void progress(int length, int progress) {

            }
        }, null, true, RPCRequest.RPCRequestClassUploadMedia, ConnectionsManager.DEFAULT_DATACENTER_ID);
    }
}

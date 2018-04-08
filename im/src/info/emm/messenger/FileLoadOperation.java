/*
 * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.messenger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.util.Scanner;

import info.emm.ui.ApplicationLoader;
import info.emm.utils.Utilities;

public class FileLoadOperation {
	private int downloadChunkSize = 1024 * 32;

	public int datacenter_id;
	public TLRPC.InputFileLocation location;
	public volatile int state = 0;
	private int downloadedBytes;
	public int totalBytesCount;
	public FileLoadOperationDelegate delegate;
	public Bitmap image;
	public String filter;
	private byte[] key;
	private byte[] iv;
	private long requestToken = 0;

	private File cacheFileTemp;
	private File cacheFileFinal;
	private File cacheIvTemp;

	private String ext;
	private String httpUrl;
	private URLConnection httpConnection;
	public boolean needBitmapCreate = true;
	private InputStream httpConnectionStream;
	private RandomAccessFile fileOutputStream;
	RandomAccessFile fiv;

	//sam
	private AsyncHttpClient client = new AsyncHttpClient();

	public static interface FileLoadOperationDelegate {
		public abstract void didFinishLoadingFile(FileLoadOperation operation);
		public abstract void didFailedLoadingFile(FileLoadOperation operation);
		public abstract void didChangedLoadProgress(FileLoadOperation operation, float progress);
	}

	public FileLoadOperation(TLRPC.FileLocation fileLocation) {
		//    	FileLog.d("emm", "FileLoadOperation new: "+ " this:" + this);
		if (fileLocation instanceof TLRPC.TL_fileEncryptedLocation) {
			location = new TLRPC.TL_inputEncryptedFileLocation();
			location.id = fileLocation.volume_id;
			location.volume_id = fileLocation.volume_id;
			location.access_hash = fileLocation.secret;
			location.local_id = fileLocation.local_id;
			iv = new byte[32];
			System.arraycopy(fileLocation.iv, 0, iv, 0, iv.length);
			key = fileLocation.key;
			datacenter_id = fileLocation.dc_id;
		} else if (fileLocation instanceof TLRPC.TL_fileLocation) {
			location = new TLRPC.TL_inputFileLocation();
			location.volume_id = fileLocation.volume_id;
			location.secret = fileLocation.secret;
			location.local_id = fileLocation.local_id;
			datacenter_id = fileLocation.dc_id;
			location.http_path_img =  fileLocation.http_path_img;
			//            if(location.http_path_img.contains("http://api.yunboxin.com_small"))
			//            {
			//            	int i=0;
			//            	i=2;
			//            }
		}
	}

	public FileLoadOperation(TLRPC.Video videoLocation) {
		//    	FileLog.d("emm", "FileLoadOperation new: "+ " this:" + this);
		if (videoLocation instanceof TLRPC.TL_video) {
			location = new TLRPC.TL_inputVideoFileLocation();
			datacenter_id = videoLocation.dc_id;
			location.id = videoLocation.id;
			location.access_hash = videoLocation.access_hash;
		} else if (videoLocation instanceof TLRPC.TL_videoEncrypted) {
			location = new TLRPC.TL_inputEncryptedFileLocation();
			location.id = videoLocation.id;
			location.access_hash = videoLocation.access_hash;
			datacenter_id = videoLocation.dc_id;
			iv = new byte[32];
			System.arraycopy(videoLocation.iv, 0, iv, 0, iv.length);
			key = videoLocation.key;
		}
		ext = ".mp4";
	}

	public FileLoadOperation(TLRPC.Audio audioLocation) {
		//    	FileLog.d("emm", "FileLoadOperation new: "+ " this:" + this);
		if (audioLocation instanceof TLRPC.TL_audio) {
			location = new TLRPC.TL_inputAudioFileLocation();
			datacenter_id = audioLocation.dc_id;
			location.id = audioLocation.id;
			location.access_hash = audioLocation.access_hash;
		} else if (audioLocation instanceof TLRPC.TL_audioEncrypted) {
			location = new TLRPC.TL_inputEncryptedFileLocation();
			location.id = audioLocation.id;
			location.access_hash = audioLocation.access_hash;
			datacenter_id = audioLocation.dc_id;
			iv = new byte[32];
			System.arraycopy(audioLocation.iv, 0, iv, 0, iv.length);
			key = audioLocation.key;
		}
		ext = ".m4a";
	}

	public FileLoadOperation(TLRPC.Document documentLocation) {
		//    	FileLog.d("emm", "FileLoadOperation new: "+ " this:" + this);
		if (documentLocation instanceof TLRPC.TL_document) {
			location = new TLRPC.TL_inputDocumentFileLocation();
			datacenter_id = documentLocation.dc_id;
			location.id = documentLocation.id;
			location.access_hash = documentLocation.access_hash;
			//xueqiang change for download file
			location.http_path_img =  documentLocation.http_path;
		} else if (documentLocation instanceof TLRPC.TL_documentEncrypted) {
			location = new TLRPC.TL_inputEncryptedFileLocation();
			location.id = documentLocation.id;
			location.access_hash = documentLocation.access_hash;
			datacenter_id = documentLocation.dc_id;
			iv = new byte[32];
			System.arraycopy(documentLocation.iv, 0, iv, 0, iv.length);
			key = documentLocation.key;
		}
		ext = documentLocation.file_name;
		int idx = -1;
		if (ext == null || (idx = ext.lastIndexOf(".")) == -1) {
			ext = "";
		} else {
			ext = ext.substring(idx);
			if (ext.length() <= 1) {
				ext = "";
			}
		}
	}

	public FileLoadOperation(String url) {
		//    	FileLog.d("emm", "FileLoadOperation new: "+ " this:" + this);
		httpUrl = url;
	}

	public void start() {
		if (state != 0) {
			return;
		}
		state = 1;

		//sam
		if(ApplicationLoader.myCookieStore != null)
			client.setCookieStore(ApplicationLoader.myCookieStore);

		if (location == null && httpUrl == null) {
			Utilities.stageQueue.postRunnable(new Runnable() {
				@Override
				public void run() {
					delegate.didFailedLoadingFile(FileLoadOperation.this);
				}
			});
			return;
		}
		boolean ignoreCache = false;
		boolean onlyCache = false;
		boolean isLocalFile = false;
		String fileNameFinal = null;
		String fileNameTemp = null;
		String fileNameIv = null;
		if (httpUrl != null) {
			if (!httpUrl.startsWith("http")) {
				onlyCache = true;
				isLocalFile = true;
				fileNameFinal = httpUrl;
			} else {
				fileNameFinal = Utilities.MD5(httpUrl);
				fileNameTemp = fileNameFinal + "_temp.jpg";
				fileNameFinal += ".jpg";
			}
		} else if (location.volume_id != 0 && location.local_id != 0) {
			fileNameTemp = location.volume_id + "_" + location.local_id + "_temp.jpg";
			fileNameFinal = location.volume_id + "_" + location.local_id + ".jpg";
			if (key != null) {
				fileNameIv = location.volume_id + "_" + location.local_id + ".iv";
			}
			//if (datacenter_id == Integer.MIN_VALUE || location.volume_id == Integer.MIN_VALUE) {
			// onlyCache = true;
			//}
		}
		else
		{
			//这个地方生成了文档的名字
			ignoreCache = true;
			needBitmapCreate = false;
			fileNameTemp = datacenter_id + "_" + location.id + "_temp" + ext;
			fileNameFinal = datacenter_id + "_" + location.id + ext;
			if (key != null) {
				fileNameIv = datacenter_id + "_" + location.id + ".iv";
			}
		}

		boolean exist;
		if (isLocalFile) {
			cacheFileFinal = new File(fileNameFinal);
		} else {
			cacheFileFinal = new File(Utilities.getCacheDir(), fileNameFinal);
		}
		final boolean dontDelete = isLocalFile;
		//家载本地图片，无论发送端还是接收端，先查本地是否有这个文件，如果有从文件中拿出来显示
		if ((exist = cacheFileFinal.exists()) && !ignoreCache) {
			Utilities.cacheOutQueue.postRunnable(new Runnable() {
				@Override
				public void run() {
					try {
						int delay = 20;
						if (FileLoader.getInstance().runtimeHack != null) {
							delay = 60;
						}
						if (FileLoader.lastCacheOutTime != 0 && FileLoader.lastCacheOutTime > System.currentTimeMillis() - delay) {
							Thread.sleep(delay);
						}
						FileLoader.lastCacheOutTime = System.currentTimeMillis();
						if (state != 1) {
							return;
						}
						if (needBitmapCreate) {
							FileInputStream is = new FileInputStream(cacheFileFinal);
							//FileLog.e("emm", "FileLoadOperation="+cacheFileFinal.getAbsolutePath());
							BitmapFactory.Options opts = new BitmapFactory.Options();

							float w_filter = 0;
							float h_filter;
							if (filter != null) {
								String args[] = filter.split("_");
								w_filter = Float.parseFloat(args[0]) * Utilities.density;
								h_filter = Float.parseFloat(args[1]) * Utilities.density;

								opts.inJustDecodeBounds = true;
								BitmapFactory.decodeFile(cacheFileFinal.getAbsolutePath(), opts);
								float photoW = opts.outWidth;
								float photoH = opts.outHeight;
								float scaleFactor = Math.max(photoW / w_filter, photoH / h_filter);
								if (scaleFactor < 1) {
									scaleFactor = 1;
								}
								opts.inJustDecodeBounds = false;
								opts.inSampleSize = (int)scaleFactor;
							}

							if (filter == null) {
								opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
							} else {
								opts.inPreferredConfig = Bitmap.Config.RGB_565;
							}
							opts.inDither = false;
							image = BitmapFactory.decodeStream(is, null, opts);
							is.close();
							if (image == null) {
								if (!dontDelete && (cacheFileFinal.length() == 0 || filter == null)) {
									cacheFileFinal.delete();
								}
							} else {
								if (filter != null && image != null) {
									float bitmapW = image.getWidth();
									float bitmapH = image.getHeight();
									if (bitmapW != w_filter && bitmapW > w_filter) {
										float scaleFactor = bitmapW / w_filter;
										Bitmap scaledBitmap = Bitmap.createScaledBitmap(image, (int)w_filter, (int)(bitmapH / scaleFactor), true);
										if (image != scaledBitmap) {
											if (Build.VERSION.SDK_INT < 11) {
												image.recycle();
											}
											image = scaledBitmap;
										}
									}

								}
								if (FileLoader.getInstance().runtimeHack != null) {
									FileLoader.getInstance().runtimeHack.trackFree(image.getRowBytes() * image.getHeight());
								}
							}
						}
						Utilities.stageQueue.postRunnable(new Runnable() {
							@Override
							public void run() {
								if (image == null) {
									delegate.didFailedLoadingFile(FileLoadOperation.this);
								} else {
									delegate.didFinishLoadingFile(FileLoadOperation.this);
								}
							}
						});
					} catch (Exception e) {
						if (!dontDelete && cacheFileFinal.length() == 0) {
							cacheFileFinal.delete();
						}
						FileLog.e("emm", e);
					}
				}
			});
		} else {
			if (onlyCache) {
				cleanup();
				Utilities.stageQueue.postRunnable(new Runnable() {
					@Override
					public void run() {
						delegate.didFailedLoadingFile(FileLoadOperation.this);
					}
				});
				return;
			}
			//            cacheFileTemp = new File(Utilities.getCacheDir(), fileNameTemp);
			//            if (cacheFileTemp.exists()) {
			//                downloadedBytes = (int)cacheFileTemp.length();
			//                downloadedBytes = downloadedBytes / 1024 * 1024;
			//            }
			if (fileNameIv != null) {
				cacheIvTemp = new File(Utilities.getCacheDir(), fileNameIv);
				try {
					fiv = new RandomAccessFile(cacheIvTemp, "rws");
					long len = cacheIvTemp.length();
					if (len > 0 && len % 32 == 0) {
						fiv.read(iv, 0, 32);
					} else {
						downloadedBytes = 0;
					}
				} catch (Exception e) {
					FileLog.e("emm", e);
					downloadedBytes = 0;
				}
			}
			if (exist) {
				cacheFileFinal.delete();
			}
			//            try {
			//            	FileLog.d("emm", "FileLoadOperation new fileOutputStream: "+ " this:" + this);
			//                fileOutputStream = new RandomAccessFile(cacheFileTemp, "rws");
			//                if (downloadedBytes != 0) {
			//                    fileOutputStream.seek(downloadedBytes);
			//                }
			//            } catch (Exception e) {
			//                FileLog.e("emm", e);
			//            }
			//            if (fileOutputStream == null) {
			//                cleanup();
			//                Utilities.stageQueue.postRunnable(new Runnable() {
			//                    @Override
			//                    public void run() {
			//                        delegate.didFailedLoadingFile(FileLoadOperation.this);
			//                    }
			//                });
			//                return;
			//            }
			if (httpUrl != null) {
				startDownloadHTTPRequest();
			} else {
				startDownloadRequest();
			}
		}
	}

	public void cancel() {
		if (state != 1) {
			return;
		}
		state = 2;
		cleanup();
		if (httpUrl == null && requestToken != 0) {
			ConnectionsManager.getInstance().cancelRpc(requestToken, true);
		}
		delegate.didFailedLoadingFile(FileLoadOperation.this);
	}

	private void cleanup() {
		/*if (httpUrl != null) {
            try {
                if (httpConnectionStream != null) {
                    httpConnectionStream.close();
                }
                httpConnection = null;
                httpConnectionStream = null;
            } catch (Exception e) {
                FileLog.e("emm", e);
            }
        } else {*/
		try {
			if (fileOutputStream != null) {
				fileOutputStream.close();
				fileOutputStream = null;
			}
		} catch (Exception e) {
			FileLog.e("emm", e);
		}

		try {
			if (fiv != null) {
				fiv.close();
				fiv = null;
			}
		} catch (Exception e) {
			FileLog.e("emm", e);
		}
		//}
	}

	private void onFinishLoadingFile() throws Exception {
		if (state != 1) {
			return;
		}
		state = 3;
		cleanup();
		if (cacheIvTemp != null) {
			cacheIvTemp.delete();
		}
		//        final boolean renamed = cacheFileTemp.renameTo(cacheFileFinal);
		final boolean renamed = true; //sam

		if (needBitmapCreate) {
			Utilities.cacheOutQueue.postRunnable(new Runnable() {
				@Override
				public void run() {
					int delay = 20;
					if (FileLoader.getInstance().runtimeHack != null) {
						delay = 60;
					}
					if (FileLoader.lastCacheOutTime != 0 && FileLoader.lastCacheOutTime > System.currentTimeMillis() - delay) {
						try {
							Thread.sleep(delay);
						} catch (Exception e) {
							FileLog.e("emm", e);
						}
					}
					BitmapFactory.Options opts = new BitmapFactory.Options();

					float w_filter = 0;
					float h_filter;
					if (filter != null) {
						String args[] = filter.split("_");
						w_filter = Float.parseFloat(args[0]) * Utilities.density;
						h_filter = Float.parseFloat(args[1]) * Utilities.density;

						opts.inJustDecodeBounds = true;
						BitmapFactory.decodeFile(cacheFileFinal.getAbsolutePath(), opts);
						float photoW = opts.outWidth;
						float photoH = opts.outHeight;
						float scaleFactor = Math.max(photoW / w_filter, photoH / h_filter);
						if (scaleFactor < 1) {
							scaleFactor = 1;
						}
						opts.inJustDecodeBounds = false;
						opts.inSampleSize = (int) scaleFactor;
					}

					if (filter == null) {
						opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
					} else {
						opts.inPreferredConfig = Bitmap.Config.RGB_565;
					}

					opts.inDither = false;
					try {
						if (renamed) {
							image = BitmapFactory.decodeStream(new FileInputStream(cacheFileFinal), null, opts);
						} else {
							//                            try {
							//                                image = BitmapFactory.decodeStream(new FileInputStream(cacheFileTemp), null, opts);
							//                            } catch (Exception e) {
							//                                FileLog.e("emm", e);
							//                                image = BitmapFactory.decodeStream(new FileInputStream(cacheFileFinal), null, opts);
							//                            }
						}
						if (filter != null && image != null) {
							float bitmapW = image.getWidth();
							float bitmapH = image.getHeight();
							if (bitmapW != w_filter && bitmapW > w_filter) {
								float scaleFactor = bitmapW / w_filter;
								Bitmap scaledBitmap = Bitmap.createScaledBitmap(image, (int) w_filter, (int) (bitmapH / scaleFactor), true);
								if (image != scaledBitmap) {
									if (Build.VERSION.SDK_INT < 11) {
										image.recycle();
									}
									image = scaledBitmap;
								}
							}

						}
						if (image != null && FileLoader.getInstance().runtimeHack != null) {
							FileLoader.getInstance().runtimeHack.trackFree(image.getRowBytes() * image.getHeight());
						}
						if (image != null) {
							delegate.didFinishLoadingFile(FileLoadOperation.this);
						} else {
							delegate.didFailedLoadingFile(FileLoadOperation.this);
						}
					} catch (Exception e) {
						FileLog.e("emm", e);
						delegate.didFailedLoadingFile(FileLoadOperation.this);
					}
				}
			});
		} else {
			delegate.didFinishLoadingFile(FileLoadOperation.this);
		}
	}

	private void startDownloadHTTPRequest() {
		if (state != 1) {
			return;
		}
		//xueqiang change使用http方式下在图片
		//        AsyncHttpClient client = new AsyncHttpClient();   
		String[] allowedTypes = new String[] { ".*" };
		//FileLog.d("emm", " startDownloadHTTPRequest smallurl="+httpUrl + " this:" + this);
		//client.get(httpUrl, new BinaryHttpResponseHandler(allowedTypes) {
		//client.get(httpUrl, new FileAsyncHttpResponseHandler(cacheFileTemp) {
		httpUrl = httpUrl.replace("|","%124");
		if(httpUrl.contains("http://api.yunboxin.com_small"))
		{
			int i=0;
			i=2;
		}
		client.get(httpUrl, new FileAsyncHttpResponseHandler(cacheFileFinal) {
			@Override
			//public void onSuccess(byte[] imageData) {
			public void onSuccess(java.io.File file) {
				// Successfully got a response
				try {
					//            		FileLog.d("emm", "onsuccess httpUrl image size="+file.length() + " url:" + httpUrl + " this:" + FileLoadOperation.this);
					//            		fileOutputStream.write(imageData, 0, imageData.length);
					Utilities.stageQueue.postRunnable(new Runnable() {
						@Override
						public void run() {
							try
							{
								//                            	FileLog.d("emm", " onSuccess smallurl="+httpUrl + " this:" + FileLoadOperation.this);
								onFinishLoadingFile();
							} catch (Exception e) {
								delegate.didFailedLoadingFile(FileLoadOperation.this);
							}
						}
					});
				}
				catch (Exception e)
				{
					//            		e.printStackTrace();
					Utilities.stageQueue.postRunnable(new Runnable() {
						@Override
						public void run()
						{
							FileLog.d("emm", "didFailedLoadingFile smallurl 1="+httpUrl + " this:" + FileLoadOperation.this);
							cleanup();
							delegate.didFailedLoadingFile(FileLoadOperation.this);
						}
					});
				}
			}

			@Override
			public void onFailure(Throwable e) {
				Utilities.stageQueue.postRunnable(new Runnable() {
					@Override
					public void run()
					{
						FileLog.d("emm", "didFailedLoadingFile smallurl 2="+httpUrl + " this:" + FileLoadOperation.this);
						cleanup();
						delegate.didFailedLoadingFile(FileLoadOperation.this);
					}
				});
			}
			@Override
			public void onProgress(int bytesWritten, int totalSize)
			{
				final int totalBytesCount = totalSize;
				final int progress = bytesWritten;
				Utilities.stageQueue.postRunnable(new Runnable() {
					@Override
					public void run() {
						if (totalBytesCount > 0) {
							delegate.didChangedLoadProgress(FileLoadOperation.this,  Math.min(1.0f, (float)(progress) / (float)totalBytesCount));
						}
						//else if (totalBytesCount == -1) {
						//delegate.didChangedLoadProgress(FileLoadOperation.this,  Math.min(1.0f, (float)(progress) / (float)length));
						//}
					}
				});

			}
		});

		/*if (httpConnection == null) {
            try {
                URL downloadUrl = new URL(httpUrl);
                httpConnection = downloadUrl.openConnection();
                if( httpConnection!=null )
                {
	                httpConnection.setConnectTimeout(5000);
	                httpConnection.setReadTimeout(90000);
	                httpConnection.connect();
	                httpConnectionStream = httpConnection.getInputStream();
                }
            } catch (Exception e) {
                FileLog.e("emm", e);
                cleanup();
                Utilities.stageQueue.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        delegate.didFailedLoadingFile(FileLoadOperation.this);
                    }
                });
                return;
            }
        }

        try {
            byte[] data = new byte[1024 * 2];
            int readed = httpConnectionStream.read(data);
            if (readed > 0) {
                fileOutputStream.write(data, 0, readed);
                Utilities.imageLoadQueue.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        startDownloadHTTPRequest();
                    }
                });
            } else if (readed == -1) {
                cleanup();
                Utilities.stageQueue.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            onFinishLoadingFile();
                        } catch (Exception e) {
                            delegate.didFailedLoadingFile(FileLoadOperation.this);
                        }
                    }
                });
            } else {
                cleanup();
                Utilities.stageQueue.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        delegate.didFailedLoadingFile(FileLoadOperation.this);
                    }
                });
            }
        } catch (Exception e) {
            cleanup();
            FileLog.e("emm", e);
            Utilities.stageQueue.postRunnable(new Runnable() {
                @Override
                public void run() {
                    delegate.didFailedLoadingFile(FileLoadOperation.this);
                }
            });
        }*/
	}

	private void startDownloadRequest()
	{
		if( location.http_path_img!=null && !location.http_path_img.equals(""))
		{
			httpUrl=location.http_path_img;
			//FileLog.e("emm", "down file from"+httpUrl);
			startDownloadHTTPRequest();
			return;
		}
		if (state != 1) {
			return;
		}
		TLRPC.TL_upload_getFile req = new TLRPC.TL_upload_getFile();
		req.location = location;
		//if (totalBytesCount == -1) {
		//    req.offset = 0;
		//    req.limit = 0;
		//} else {
		req.offset = downloadedBytes;
		req.limit = downloadChunkSize;
		//}
		requestToken = ConnectionsManager.getInstance().performRpc(req, new RPCRequest.RPCRequestDelegate() {
			@Override
			public void run(TLObject response, TLRPC.TL_error error) {
				requestToken = 0;
				if (error == null) {
					TLRPC.TL_upload_file res = (TLRPC.TL_upload_file)response;
					try {
						if (res.bytes.limit() == 0) {
							onFinishLoadingFile();
							return;
						}
						if (key != null) {
							Utilities.aesIgeEncryption2(res.bytes.buffer, key, iv, false, true, res.bytes.limit());
						}
						if (fileOutputStream != null) {
							FileChannel channel = fileOutputStream.getChannel();
							channel.write(res.bytes.buffer);
						}
						if (fiv != null) {
							fiv.seek(0);
							fiv.write(iv);
						}
						downloadedBytes += res.bytes.limit();
						if (totalBytesCount > 0) {
							delegate.didChangedLoadProgress(FileLoadOperation.this,  Math.min(1.0f, (float)downloadedBytes / (float)totalBytesCount));
						}
						if (downloadedBytes % downloadChunkSize == 0 || totalBytesCount > 0 && totalBytesCount != downloadedBytes) {
							startDownloadRequest();
						} else {
							onFinishLoadingFile();
						}
					} catch (Exception e) {
						cleanup();
						delegate.didFailedLoadingFile(FileLoadOperation.this);
						FileLog.e("emm", e);
					}
				} else {
					if (error.text.contains("FILE_MIGRATE_")) {
						String errorMsg = error.text.replace("FILE_MIGRATE_", "");
						Scanner scanner = new Scanner(errorMsg);
						scanner.useDelimiter("");
						Integer val;
						try {
							val = scanner.nextInt();
						} catch (Exception e) {
							val = null;
						}
						if (val == null) {
							cleanup();
							delegate.didFailedLoadingFile(FileLoadOperation.this);
						} else {
							datacenter_id = val;
							startDownloadRequest();
						}
					} else if (error.text.contains("OFFSET_INVALID")) {
						if (downloadedBytes % downloadChunkSize == 0) {
							try {
								onFinishLoadingFile();
							} catch (Exception e) {
								FileLog.e("emm", e);
								cleanup();
								delegate.didFailedLoadingFile(FileLoadOperation.this);
							}
						} else {
							cleanup();
							delegate.didFailedLoadingFile(FileLoadOperation.this);
						}
					} else {
						cleanup();
						delegate.didFailedLoadingFile(FileLoadOperation.this);
					}
				}
			}
		}, new RPCRequest.RPCProgressDelegate() {
			@Override
			public void progress(int length, int progress) {
				if (totalBytesCount > 0) {
					delegate.didChangedLoadProgress(FileLoadOperation.this,  Math.min(1.0f, (float)(downloadedBytes + progress) / (float)totalBytesCount));
				} else if (totalBytesCount == -1) {
					delegate.didChangedLoadProgress(FileLoadOperation.this,  Math.min(1.0f, (float)(progress) / (float)length));
				}
			}
		}, null, true, RPCRequest.RPCRequestClassDownloadMedia, datacenter_id);
	}
}

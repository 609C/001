package com.weiyicloud.whitepad;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.msgpack.MessagePack;
import org.msgpack.type.Value;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.weiyicloud.whitepad.Face_Share_Fragment.penClickListener;
import com.weiyicloud.whitepad.UploadFile.UpLoadFileDelegate;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class SharePadMgr implements FileDownLoad.FileDownLoadDelegate,
		WhitePadInterface, UpLoadFileDelegate {

	public interface DataChangeListener {
		public abstract void onChange();
	};

	public static final int GETMEETING_DOC = 41;// 获取会诊文档
	public static final int DELMEETING_DOC = 42;// 删除会诊文档

	private static SharePadMgr mInstance;
	private static String sync = "";
	public ArrayList<TL_PadAction> m_alActions = new ArrayList<TL_PadAction>();

	private ConcurrentHashMap<Integer, ShareDoc> mMapDoc = new ConcurrentHashMap<Integer, ShareDoc>();
	private ArrayList<ShareDoc> mAryDoc = new ArrayList<ShareDoc>();
	public Context mAppContext;
	public ShareDoc mBlankShareDoc = new ShareDoc();
	public ShareDoc mCurrentShareDoc = mBlankShareDoc;
	public ArrayList<DataChangeListener> mDataChangeList = new ArrayList<DataChangeListener>();
	// public int ncurrentDocid = 0;
	// public int ncurrentPageid = 0;
	// public int nLastShowPageUser = 0;
	public String strCurrentImagePath = "";

	public String strWebImageDomain = "";
	public Bitmap btcurrentImage = null;
	public int btDoc = 0;
	public int btpage = -1;

	final public static int SHAREPAD_STATECHANGE = 1500;
	final public static int SHAREPAD_DOCCHANGE = 1501;
	final public static int SHAREPAD_PAGECHANGE = 1502;
	final public static int SHAREPAD_ACTIONCHANGE = 1503;
	final public static int SHAREPAD_LOAD_FILE_COMPLETE = 1504;
	public static final int UPLOAD_IMAGE_COMPLETE = 1505;
	public static final int UPLOAD_IMAGE_PROCESSING = 1506;
	public static final String UPLOAD_IMAGE_INTERFACE = "uploaddocument";
	static public int LOAD_IMAGE = 100;

	// xiaoyang add
	private ControlMode controlMode;
	private  Context pad_context;
	private AsyncHttpClient client;
	private DocInterface docInterface;
	private FaceShareControl shareControl;
	Face_Share_Fragment m_fragment_share;

	public void setShareControl(FaceShareControl shareControl) {
		this.shareControl = shareControl;
	}

	public void setDocInterface(DocInterface docInterface) {
		this.docInterface = docInterface;
	}

	//cyj 20161029
	public void setWBPageCount(int count) {
		Log.e("emm", "setWBPageCount " + count);
	}

	public void setWBBackColor(int color) {
		Log.e("emm", "setWBBackColor " + color);
	}

	public Context getPad_context() {
		return pad_context;
	}

	public void setPad_context(Context pad_context) {
		this.pad_context = pad_context;
	}

	public AsyncHttpClient getClient() {
		return client;
	}

	public void setClient(AsyncHttpClient client) {
		this.client = client;
	}

	public ControlMode getControlMode() {
		return controlMode;
	}

	public void setControlMode(ControlMode controlMode) {
		this.controlMode = controlMode;
		if(m_fragment_share!=null){
			m_fragment_share.changeControlMode();
		}
	}

	// xiaoyang add
	private SharePadMgr() {
		mBlankShareDoc.bIsBlank = true;
		mBlankShareDoc.docID = 0;
		mBlankShareDoc.currentPage = 1;
		mBlankShareDoc.pageCount = 1;
		mBlankShareDoc.fileName = "WhiteBoard";
		mMapDoc.put(mBlankShareDoc.docID, mBlankShareDoc);
		mAryDoc.add(mBlankShareDoc);
	}

	static public SharePadMgr getInstance() {
		synchronized (sync) {
			if (mInstance == null) {
				mInstance = new SharePadMgr();
			}
			return mInstance;
		}
	}

	/**
	 * setWebImageDomain
	 *
	 * @param strDomian
	 */
	public void setWebImageDomain(String strDomian) {
		strWebImageDomain = strDomian.toString();
	}

	public String getWebImageDomain() {
		return strWebImageDomain;
	}

	/**
	 * setAppContext
	 *
	 * @param cnt
	 *            app
	 */
	public void setAppContext(Context cnt) {
		mAppContext = cnt;
		Handler handler = new Handler(cnt.getMainLooper());
		Utitlties.init(cnt, handler);
		FileLog.init(cnt, handler);
		File dirFile = mAppContext.getExternalCacheDir();
		if (dirFile == null) {
			dirFile = mAppContext.getCacheDir();
		}
		if (dirFile != null) {
			String strSaveDir = dirFile.getPath() + "/" + "ShareImage";
			File dirFileShare = new File(strSaveDir);
			if (dirFileShare.exists()) {
				dirFileShare.delete();
			}
		}
	}

	/**
	 * setAppContext DataChangeListener
	 *
	 * @param cls
	 */
	public void addOnDataChangeListener(DataChangeListener cls) {
		if (!mDataChangeList.contains(cls)) {
			mDataChangeList.add(cls);
		}
	}

	public void removeOnDataChangeListener(DataChangeListener cls) {
		if (mDataChangeList.contains(cls)) {
			mDataChangeList.remove(cls);
		}
	}

	/**
	 *
	 * @param sd
	 * @param nPage
	 * @param bit
	 */
	public void loadImage(ShareDoc sd, int nPage, Bitmap bit) {
		final ShareDoc m_sd = sd;
		final int m_nPage = nPage;
		final Bitmap m_bit = bit;
		new Handler(mAppContext.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				OnImageLoad(m_sd, m_nPage, m_bit);
			}

		});
	}

	/**
	 * 接受文档变化，给客户
	 *
	 * @param bReceive
	 * @param sdc
	 */
	public void receiveDocChange(boolean bReceive, ShareDoc sdc) {
		OnReceiveDocChange(bReceive, sdc);
	}

	/**
	 * 接受文档页数变化，给客户。
	 *
	 * @param nDoc
	 * @param nPage
	 */
	public void receivePageChange(int nDoc, int nPage) {
		OnReceivePageChange(nDoc, nPage);
	}

	/**
	 * 改变白板图形，给客户
	 *
	 * @param fileid
	 * @param shapeid
	 * @param bAdd
	 * @param shapedata
	 */
	public void shapesChange(byte[] shapedata, Boolean bAdd) {
		String StrBytes = new String();
		MessagePack mp = new MessagePack();
		try {
			if (shapedata != null && shapedata.length > 0) {
				Value va = mp.read(shapedata);
				StrBytes = va.toString();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		TL_PadAction tl_pa = new TL_PadAction();

		try {
			JSONArray jsData = new JSONArray((String) StrBytes);

			if (jsData != null) {
				jsData.optInt(0);
				int nType = jsData.optInt(1);
				int DocID = jsData.optInt(2);
				int nPage = jsData.optInt(3);
				tl_pa.nPageID = nPage;
				tl_pa.nDocID = DocID;
				JSONArray ActionDate = jsData.optJSONArray(4);
				if (ActionDate != null) {
					tl_pa.sID = ActionDate.optString(0);
					tl_pa.nActionMode = TL_PadAction.factoryType
							.valueOf(ActionDate.optInt(1));
					PointF pt1 = new PointF();
					PointF pt2 = new PointF();
					pt1.x = (float) ActionDate.optDouble(2) / 100;
					pt1.y = (float) ActionDate.optDouble(3) / 100;
					pt2.x = (float) ActionDate.optDouble(4) / 100;
					pt2.y = (float) ActionDate.optDouble(5) / 100;
					tl_pa.alActionPoint.add(pt1);
					tl_pa.alActionPoint.add(pt2);
					JSONObject jsOpt = ActionDate.getJSONObject(6);
					if (jsOpt != null) {
						tl_pa.nPenWidth = jsOpt.getInt("width");
						int nColor = jsOpt.getInt("color");

						int r = (nColor >> 24) & 0xFF;
						int g = (nColor >> 16) & 0xFF;
						int b = (nColor >> 8) & 0xFF;
						int a = (nColor) & 0xFF;

						tl_pa.nPenColor = Color.argb(a, r, g, b);

						if (jsOpt.has("fill")){
							Object temp = jsOpt.get("fill");
							if(temp instanceof Integer){
								int inttemp = (Integer) temp;
								tl_pa.bIsFill = inttemp == 0?false:true;
							}else{
								tl_pa.bIsFill = (Boolean)temp;
							}


//							tl_pa.bIsFill = jsOpt.getBoolean("fill");
						}
						if (jsOpt.has("text"))
							tl_pa.sText = jsOpt.getString("text");

					}
					if (tl_pa.nActionMode == TL_PadAction.factoryType.ft_markerPen) {
						JSONArray jspts = ActionDate.getJSONArray(7);
						if (jspts != null) {
							tl_pa.alActionPoint.clear();
							for (int i = 0; i < jspts.length(); i++) {
								JSONObject jspt = jspts.getJSONObject(i);
								if (jspt != null) {
									PointF pt = new PointF();
									pt.x = (float) jspt.getDouble("x") / 100;
									pt.y = (float) jspt.getDouble("y") / 100;
									tl_pa.alActionPoint.add(pt);
								}
							}
						}
					}
				}

				if (nType == 4) {
					OnReceiveActionsChange(
							TL_PadAction.wbEventType.et_sharpChange, tl_pa);
				} else if (bAdd) {
					OnReceiveActionsChange(
							TL_PadAction.wbEventType.et_sharpAdd, tl_pa);
				} else {
					OnReceiveActionsChange(
							TL_PadAction.wbEventType.et_sharpRemove, tl_pa);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void clearWhitePad() {
		m_alActions.clear();
		receivePageChange(0, 1);
	}

	/**
	 * handReceivedNotification
	 *
	 *
	 * @param id
	 * @param args
	 */
	// public void handReceivedNotification(int id, Object... args) {
	// if(SharePadMgr.SHAREPAD_LOAD_FILE_COMPLETE == id){
	// final String strRes= (String) args[0];
	// //Log.e("emm", "strres=="+strRes);
	// //Log.e("emm", "mAppContext=="+mAppContext);
	// parserMeetingFiles(strRes);
	// OnReceivePageChange(0,1);
	//
	// }else if(LOAD_IMAGE == id){
	// final ShareDoc sd = (ShareDoc) args[0];
	// final int npage = (Integer) args[1];
	// final Bitmap BitMap = (Bitmap) args[2];
	//
	// new Handler(mAppContext.getMainLooper()).post(new Runnable(){
	// @Override
	// public void run() {
	// OnImageLoad(sd, npage, BitMap);
	// }
	//
	// });
	// }
	// else if( 28== id){ //MeetingSession.UI_NOTIFY_USER_WHITE_PAD_DOC_CHANGE
	// JSONObject js = (JSONObject)args[0];
	// try {
	// ShareDoc sdc = new ShareDoc();
	// boolean isdel = js.getBoolean("isdel");
	// sdc.docID = js.getInt("fileid");
	// if(!isdel){
	// sdc.fileName = js.getString("filename");
	// sdc.fileUrl =strWebImageDomain + js.getString("fileurl");
	// sdc.pageCount = js.getInt("pagecount");
	// }
	// OnReceiveDocChange(!isdel, sdc);
	// } catch (JSONException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// else if(18 == id){ //MeetingSession.UI_NOTIFY_USER_WHITE_PAD
	//
	// if (name.equals("ShowPage")){
	// JSONObject jsbody;
	// try
	// {
	// //同步者在会诊中的ID
	// senderid = js.getInt("senderID");
	// jsbody = js.getJSONObject("body");
	// if(jsbody!=null)
	// {
	// int nFileID = jsbody.getInt("fileID");
	// int nPageID = jsbody.getInt("pageID");
	// nLastShowPageUser = senderid;
	// OnReceivePageChange(nFileID, nPageID);
	//
	// }
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// }else if (name.equals("sharpsChange")){
	//
	//
	// }
	// }
	// }

	/**
	 * OnReceiveActionsChange
	 *
	 *
	 */
	public void OnReceiveActionsChange(TL_PadAction.wbEventType nAction,
									   TL_PadAction tl_pa) {
		if (nAction == TL_PadAction.wbEventType.et_sharpAdd) {
			boolean bHas = false;
			for (int i = 0; i < m_alActions.size(); i++) {
				TL_PadAction pa = m_alActions.get(i);
				if (pa.sID.equals(tl_pa.sID)) {
					m_alActions.remove(i);
					m_alActions.add(i, tl_pa);
					bHas = true;
					break;
				}
			}
			if (!bHas)
				m_alActions.add(tl_pa);
		} else if (nAction == TL_PadAction.wbEventType.et_sharpRemove) {
			for (int i = 0; i < m_alActions.size(); i++) {
				TL_PadAction pa = m_alActions.get(i);
				if (pa.sID.equals(tl_pa.sID)) {
					m_alActions.remove(pa);
					break;
				}
			}
		} else if (nAction == TL_PadAction.wbEventType.et_sharpChange) {
			for (int i = 0; i < m_alActions.size(); i++) {
				TL_PadAction pa = m_alActions.get(i);
				if (pa.sID.equals(tl_pa.sID)) {
					m_alActions.remove(i);
					m_alActions.add(i, tl_pa);
					break;
				}
			}
		}
		for (int i = 0; i < mDataChangeList.size(); i++) {
			mDataChangeList.get(i).onChange();
		}
	}

	/**
	 *
	 */
	public ShareDoc getCurrentShareDoc() {
		return mCurrentShareDoc;
	}

	/**
	 * OnReceivePageChagen
	 *
	 * @return
	 */
	public ShareDoc OnReceivePageChange(int nDoc, int nPage) {

		// String strDocid = "docsChange-"+nDoc;
		ShareDoc sd = mMapDoc.get(nDoc);
		if (sd != null && nDoc != 0) {
			if (isMediaFile(sd.fileName))
				return mCurrentShareDoc;
		}
		if (sd != null) {
			boolean bChanged = false;
			if (mCurrentShareDoc != sd) {
				bChanged = true;
				mCurrentShareDoc = sd;
			}
			if (sd.currentPage != nPage) {
				bChanged = true;
				sd.currentPage = nPage;
			}
			FileLog.d("emm", "nDoc=" + nDoc);
			FileLog.d("emm", "currentPage=" + sd.currentPage);
			FileLog.d("emm", "nPage=" + nPage);
			if (bChanged)
				changeDocImage();
		} else if (nDoc == 0) {
			mCurrentShareDoc = mBlankShareDoc;
			mBlankShareDoc.currentPage = nPage;
			if (mBlankShareDoc.currentPage >= mBlankShareDoc.pageCount) {
				mBlankShareDoc.pageCount = mBlankShareDoc.currentPage;
			}
			changeDocImage();
		}
		for (int i = 0; i < mDataChangeList.size(); i++) {
			mDataChangeList.get(i).onChange();
		}
		return mCurrentShareDoc;
	}

	private boolean isMediaFile(String fileName) {
		int index = fileName.lastIndexOf(".");
		if (index == -1)
			return false;
		String tempstr = fileName.substring(fileName.lastIndexOf("."))
				.toLowerCase();
		if (tempstr.equals(".swf") || tempstr.equals(".flv")
				|| tempstr.equals(".mp4")) {
			return true;
		}
		return false;
	}

	/**
	 * OnReceiveDocChange
	 */
	public void OnReceiveDocChange(boolean bReceive, ShareDoc sdc) {
		if (sdc != null) {
			if (bReceive) {
				if (mMapDoc.get(sdc.docID) == null) {
					mMapDoc.put(sdc.docID, sdc);
					if (isMediaFile(sdc.fileName)) {
						mAryDoc.remove(sdc);
					} else {
						mAryDoc.add(sdc);
					}
				}
			} else {
				if (mMapDoc.get(sdc.docID) != null) {
					ShareDoc sc = mMapDoc.get(sdc.docID);

					for (Map.Entry<Integer, String> e : sc.docPageImages
							.entrySet()) {
						String strFIle = e.getValue();
						File imageFile = new File(strFIle);
						if (imageFile.exists()) {
							imageFile.delete();
						}
					}
					mMapDoc.remove(sdc.docID);
					mAryDoc.remove(sc);
				}
			}
		} else {
		}
		for (int i = 0; i < mDataChangeList.size(); i++) {
			mDataChangeList.get(i).onChange();
		}
	}

	/**
	 *
	 * @param sdp
	 *            ,load image from webserver,if failed,I should download again
	 */
	public void LoadIamgeFromWebSite(ShareDoc sdp) {
		FileLog.d("emm", "sdp.fileUrl=" + sdp.fileUrl);
		FileDownLoad.getInstance().delegate = this;
		FileDownLoad.getInstance().start(sdp, mAppContext);
		// FileDownLoad f = new FileDownLoad(sdp,mAppContext);
		// f.start();
	}

	public void changeDocImage() {
		if (mCurrentShareDoc != null && !mCurrentShareDoc.bIsBlank) {
			if (getCurrentImage() == null) {
				LoadIamgeFromWebSite(mCurrentShareDoc);
			}

		} else {
		}
	}

	public void OnImageLoad(ShareDoc sd, int nPage, Bitmap bit) {
		if (mCurrentShareDoc != null && mCurrentShareDoc.docID == sd.docID) {
			// FileLog.d("emm", "msdp.docID="+msdp.docID);
			if (mCurrentShareDoc.currentPage == nPage) {

				btcurrentImage = bit;
				btDoc = mCurrentShareDoc.docID;
				btpage = mCurrentShareDoc.currentPage;
			} else if (bit != null) {
				bit.recycle();
				System.gc();
			}
		}
		for (int i = 0; i < mDataChangeList.size(); i++) {
			mDataChangeList.get(i).onChange();
		}
	}

	public Bitmap getCurrentImage() {
		if (mCurrentShareDoc != null) {
			if (mCurrentShareDoc.bIsBlank)
				return null;

			if (mCurrentShareDoc.docID == btDoc
					&& mCurrentShareDoc.currentPage == this.btpage) {
				return btcurrentImage;
			} else {

				String strPath = mCurrentShareDoc.docPageImages
						.get(mCurrentShareDoc.currentPage);
				if (strPath == null)
					return null;

				BitmapFactory.Options opts = new BitmapFactory.Options();

				BitmapFactory.decodeFile(strPath, opts);

				int imageHeight = opts.outHeight;
				int imageWidth = opts.outWidth;

				int nScale = imageHeight * imageWidth / (1920 * 1080);

				if (nScale > 1) {
					opts.inSampleSize = nScale;
				}

				opts.inPreferredConfig = Bitmap.Config.RGB_565;

				opts.inJustDecodeBounds = false;

				if (btcurrentImage != null) {
					btcurrentImage.recycle();
					System.gc();
					btcurrentImage = null;
				}

				btcurrentImage = BitmapFactory.decodeFile(strPath, opts);

				if (btcurrentImage != null) {
					this.btDoc = mCurrentShareDoc.docID;
					this.btpage = mCurrentShareDoc.currentPage;
				}
				return btcurrentImage;
			}
		}
		return null;
	}

	public boolean isHasThings() {
		return !mMapDoc.isEmpty() || !m_alActions.isEmpty();
	}

	public boolean isEmpty() {
		return m_alActions.isEmpty();
	}

	public void Clear() {
		for (Map.Entry<Integer, ShareDoc> eBig : mMapDoc.entrySet()) {
			ShareDoc sd = eBig.getValue();
			for (Map.Entry<Integer, String> e : sd.docPageImages.entrySet()) {
				String strFIle = e.getValue();
				File imageFile = new File(strFIle);
				if (imageFile.exists()) {
					imageFile.delete();
				}
			}
		}
		mMapDoc.clear();
		mAryDoc.clear();
		m_alActions.clear();
		mBlankShareDoc.currentPage = 1;
		mBlankShareDoc.pageCount = 1;
		mCurrentShareDoc = null;

		mMapDoc.put(mBlankShareDoc.docID, mBlankShareDoc);
		mAryDoc.add(mBlankShareDoc);
		setControlMode(ControlMode.watch);
	}

	public ShareDoc getShareDocbyIndex(int index) {
		return mAryDoc.get(index);
	}

	public int getShareDocCount() {
		return mAryDoc.size();
	}

	public ShareDoc getShareDocbyId(int docid) {
		return mMapDoc.get(docid);
	}

	public boolean isCurrentDoc(int docid) {
		if (docid == this.mCurrentShareDoc.docID)
			return true;
		return false;
	}

	private int getIndexByDocid(int docid) {
		for (int i = 0; i < mAryDoc.size(); i++) {
			ShareDoc dc = mAryDoc.get(i);
			if (dc.docID == docid) {
				return i;
			}
		}
		return -1;
	}

	public ShareDoc getNextDoc(int docid) {
		int removeIndex = getIndexByDocid(docid);
		ShareDoc sc = mMapDoc.get(docid);
		mAryDoc.remove(sc);
		mMapDoc.remove(docid);

		int size = mAryDoc.size();
		if (size == removeIndex) {

			if (mAryDoc.size() > 0)
				return mAryDoc.get(mAryDoc.size() - 1);
		} else {

			return mAryDoc.get(removeIndex);
		}
		return mBlankShareDoc;
	}

	@Override
	public void didFinishLoadingFile(ShareDoc msdp, File cacheFileFinal) {
		FileLog.d("emm", "msdp.docID=" + msdp.docID);
		FileLog.d("emm", "msdp.currentPage=" + msdp.currentPage);
		FileLog.d("emm", "mCurrentShareDoc.docID=" + mCurrentShareDoc.docID);
		FileLog.d("emm", "mCurrentShareDoc.currentPage="
				+ mCurrentShareDoc.currentPage);

		int nPage = msdp.currentPage;
		if (!msdp.docPageImages.containsKey(nPage)) {
			String path = cacheFileFinal.getAbsolutePath();
			Log.e("emm", path);
			// xueqiang change
			ShareDoc sd = mMapDoc.get(msdp.docID);
			sd.docPageImages.put(nPage, path);

			msdp.docPageImages.put(nPage, path);
		}

		BitmapFactory.Options opts = new BitmapFactory.Options();

		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(cacheFileFinal.getAbsolutePath(), opts);
		float photoW = opts.outWidth;
		float photoH = opts.outHeight;
		int nScale = (int) photoH * (int) photoW / (1920 * 1080);
		if (nScale > 1) {
			opts.inSampleSize = nScale;
		}

		// float scaleFactor = Math.max(photoW / w_filter, photoH / h_filter);
		// if (scaleFactor < 1) {
		// scaleFactor = 1;
		// }
		opts.inJustDecodeBounds = false;
		opts.inPreferredConfig = Bitmap.Config.RGB_565;

		Bitmap image = null;
		opts.inDither = false;
		try {
			image = BitmapFactory.decodeStream(new FileInputStream(
					cacheFileFinal), null, opts);
		} catch (Exception e) {
			FileLog.e("emm", e);
		}

		loadImage(msdp, nPage, image);
	}

	@Override
	public void didFailedLoadingFile() {
		// down load again,while download failed
		FileLog.d("emm", "load file again");
		changeDocImage();
	}

	@Override
	public void didChangedLoadProgress(float progress) {

	}

	@Override
	public void whitePadDocChange(boolean isdel, int docId, int pagecount,
								  String fileName, String fileUrl,boolean islocal) {
		try {
			ShareDoc sdc = new ShareDoc();
			sdc.docID = docId;
			sdc.fileName = fileName;
			sdc.fileUrl = getWebImageDomain() + fileUrl;
			sdc.pageCount = pagecount;
			receiveDocChange(!isdel, sdc);
			if(shareControl!=null&&islocal){
				shareControl.sendDocChange(docId, isdel, fileName, fileUrl, pagecount);
				whiteshowPage(docId, sdc.currentPage, islocal);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void whiteshapesChange(byte[] shapedata, boolean bAdd) {
		shapesChange(shapedata, bAdd);

	}

	@Override
	public void whiteshowPage(int fileId, int pageId,boolean islocal) {
		receivePageChange(fileId, pageId);
		if(controlMode == ControlMode.fullcontrol&&shareControl!=null&&islocal){
			shareControl.sendShowPage(fileId, pageId);
		}

	}

	/**
	 * 获取会诊文件
	 *
	 * @param nMeetingID
	 */
	public void getMeetingFile(final int nMeetingID, final String getfileurl,final GetMeetingFileCallBack callback) {

		Utitlties.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				String url = getfileurl;
				Log.d("emm", "webFun_getmeetingfileurl=" + url);
				RequestParams params = new RequestParams();
				params.put("serial", nMeetingID + "");

				client.post(url, params, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String content) {
						try {
							final String res = content;
							/*
							 * Utitlties.RunOnUIThread(new Runnable() {
							 *
							 * @Override public void run() {
							 * MeetingSession.getInstance
							 * ().getSharePadMgr().handReceivedNotification
							 * (SharePadMgr.SHAREPAD_LOAD_FILE_COMPLETE,res);
							 * NotificationCenter.getInstance()
							 * .postNotificationName( GETMEETING_DOC, 0); } });
							 */

							Utitlties.RunOnUIThread(new Runnable() {
								@Override
								public void run() {
									loadFileComplete(res);
//									NotificationCenter.getInstance()
//											.postNotificationName(
//													GETMEETING_DOC, 0);
									if(callback!=null){
										callback.GetmeetingFile(0);
									}
								}
							});

						} catch (Exception e) {
							e.printStackTrace();
							/*
							 * Utitlties.RunOnUIThread(new Runnable() {
							 *
							 * @Override public void run() { // TODO
							 * Auto-generated method stub
							 * NotificationCenter.getInstance()
							 * .postNotificationName( GETMEETING_DOC, -1); } });
							 */

							Utitlties.RunOnUIThread(new Runnable() {
								@Override
								public void run() {
//									NotificationCenter.getInstance()
//											.postNotificationName(
//													GETMEETING_DOC, -1);
									if(callback!=null){
										callback.GetmeetingFile(-1);
									}
								}
							});
						}
					}

					@Override
					public void onFailure(Throwable error, String content) {
						/*
						 * Utitlties.RunOnUIThread(new Runnable() {
						 *
						 * @Override public void run() { // TODO Auto-generated
						 * method stub NotificationCenter.getInstance()
						 * .postNotificationName( GETMEETING_DOC, -1); } });
						 */
						Utitlties.RunOnUIThread(new Runnable() {
							@Override
							public void run() {
								if(callback!=null){
									callback.GetmeetingFile(-1);
								}
							}
						});
					}
				});
			}
		});
	}

	public void loadFileComplete(String res) {
		parserMeetingFiles(res);
		if(getCurrentShareDoc()!=null){
			whiteshowPage(getCurrentShareDoc().docID, getCurrentShareDoc().currentPage,false);
		}else{
			whiteshowPage(0, 1,false);
		}
	}

	private boolean parserMeetingFiles(String strRes) {
		try {
			JSONObject jsbody = new JSONObject(strRes);
			int nRet = jsbody.getInt("result");
			if (nRet == 0) {
				JSONArray jsa = jsbody.getJSONArray("meetingfile");
				for (int i = 0; i < jsa.length(); i++) {
					JSONObject jsmeeting = jsa.getJSONObject(i);

					int nConvert = jsmeeting.getInt("isconvert");
					if (nConvert == 1) {
						ShareDoc sdc = new ShareDoc();
						String fileName = jsmeeting.getString("filename");
						int pageCount = jsmeeting.getInt("pagenum");
						String fileUrl = jsmeeting.getString("swfpath");
						int docID = jsmeeting.getInt("fileid");

						whitePadDocChange(false, docID, pageCount, fileName,
								fileUrl,false);
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 删除会诊文件
	 *
	 * @param nMeetingID
	 * @param docid
	 */
	public void delMeetingFile(final int nMeetingID, final int docid,
							   final String delfileurl) {
		Utitlties.stageQueue.postRunnable(new Runnable() {
			@Override
			public void run() {
				String url = delfileurl+"/ClientAPI/"+"delmeetingfile";

				RequestParams params = new RequestParams();
				params.put("serial", nMeetingID + "");
				params.put("fileid", docid + "");

				client.post(url, params, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String content) {
						try {
							final String res = content;
							/*
							 * Utitlties.RunOnUIThread(new Runnable() {
							 * 
							 * @Override public void run() {
							 * NotificationCenter.getInstance()
							 * .postNotificationName( DELMEETING_DOC, docid); }
							 * });
							 */

							Utitlties.RunOnUIThread(new Runnable() {
								@Override
								public void run() {
									boolean isCurrentDoc =isCurrentDoc(docid);
									//
									ShareDoc dc = getShareDocbyId(docid);
									if (dc == null)
										return;
//									whitePadDocChange(true,dc.docID,
//											dc.pageCount,dc.fileName, dc.fileUrl);

									if (isCurrentDoc) {
										ShareDoc dcnext = getNextDoc(docid);
										if (dc == null)
											return;
										whitePadDocChange(true, dc.docID, dc.currentPage, dc.fileName, dc.fileUrl, true);
//										OnReceivePageChange(dc.docID, dc.currentPage);
//										Session.getInstance().sendShowPage(dc.docID,
//												dc.currentPage);
										whiteshowPage(dcnext.docID, dcnext.currentPage, true);

									} else{
										whitePadDocChange(true, dc.docID, dc.currentPage, dc.fileName, dc.fileUrl, true);
										whiteshowPage(getCurrentShareDoc().docID, getCurrentShareDoc().currentPage, true);
									}
									if(docInterface!=null){
										docInterface.DelmeetingFile(dc.docID,dc.currentPage,dc.fileName,dc.fileUrl);
									}
								}
							});

						} catch (Exception e) {
							e.printStackTrace();
							/*
							 * Utitlties.RunOnUIThread(new Runnable() {
							 * 
							 * @Override public void run() { // TODO
							 * Auto-generated method stub
							 * NotificationCenter.getInstance()
							 * .postNotificationName( DELMEETING_DOC, -1); } });
							 */

							Utitlties.RunOnUIThread(new Runnable() {
								@Override
								public void run() {
//									NotificationCenter.getInstance()
//											.postNotificationName(
//													DELMEETING_DOC, -1);
									if(docInterface!=null){
										docInterface.DelmeetingFile(-1,0,null,null);
									}
								}
							});

						}
					}

					@Override
					public void onFailure(Throwable error, String content) {
						/*
						 * Utitlties.RunOnUIThread(new Runnable() {
						 * 
						 * @Override public void run() { // TODO Auto-generated
						 * method stub NotificationCenter.getInstance()
						 * .postNotificationName( DELMEETING_DOC, -1); } });
						 */

						Utitlties.RunOnUIThread(new Runnable() {
							@Override
							public void run() {
//								NotificationCenter.getInstance()
//										.postNotificationName(DELMEETING_DOC,
//												-1);
								if(docInterface!=null){
									docInterface.DelmeetingFile(-1,0,null,null);
								}
							}
						});

					}
				});
			}
		});
	}

	public void upLoadMeetingFile(String strUrl, String path, String strserial,
								  int peerid, String username) {
		UploadFile uf = new UploadFile();
		uf.delegate = this;
		uf.UploadOperation(strUrl, Utitlties.getApplicationContext());
		uf.packageFile(path, strserial, peerid, username);
		uf.start();
	}

	@Override
	public void didFinishUploadingFile(UploadFile operation, String fileName,
									   String result) {

		final String fName = fileName;
		final String ret = result;
		// notifydatachange,show currentdoc
		Utitlties.RunOnUIThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// if(delegate!=null)
				// delegate.didFinish();
				// m_FragmentContainer.removeFromStack(DocFragment.this);
				try {
					JSONObject jsbody = new JSONObject(ret);
					int nRet = jsbody.getInt("result");
					if (nRet == 0) {
						int nFileID = jsbody.getInt("fileid");
						int pagenum = jsbody.getInt("pagenum");
						String filename = fName;
						String swfpath = jsbody.getString("swfpath");
						// String swfpath =
						// MeetingMgr.getInstance().getWebHttpServerAddress()
						// + jsbody.getString("swfpath");
						whitePadDocChange(false, nFileID, pagenum, filename,
								swfpath,true);
						// MeetingSession.getInstance().addMeetingFile(nFileID,
						// filename, pagenum,swfpath);
						// notify others
//						NotificationCenter.getInstance().postNotificationName(
//								UPLOADFILE_FINISH, nFileID, pagenum, filename,
//								swfpath);
						if(docInterface!=null){
							docInterface.UploadingFileFinish(nFileID, pagenum, filename, swfpath);
						}

					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void didFailedUploadingFile(UploadFile operation, int code) {
		// TODO Auto-generated method stub
//		NotificationCenter.getInstance().postNotificationName(UPLOADFILE_FAILED,
//				operation.getCount());
		if(docInterface!=null){
			docInterface.UploadingFileFailed(operation.getCount());
		}

		final UploadFile uf = operation;
		Utitlties.RunOnUIThread(new Runnable() {

			@Override
			public void run() {
				uf.packageFile(uf.getPath(), uf.getserial(),
						Integer.valueOf(uf.getuserid()), uf.getsender());
				uf.start();
			}
		});

	}

	@Override
	public void didChangedUploadProgress(UploadFile operation, float progress) {
		// TODO Auto-generated method stub
		final int pro = (int) (progress * 100);
		Utitlties.RunOnUIThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
//				NotificationCenter.getInstance()
//						.postNotificationName(UPLOADFILE_PRO, pro);
				if(docInterface!=null){
					docInterface.ChangedUploadProgress(pro);
				}

			}
		});
	}

	public Fragment createWhitePadFragment(OnClickListener m_PageClickListener,FaceShareControl shareControl,penClickListener clickListener,boolean isbro,boolean bZoom,int padSizeMode){
//		if(m_fragment_share == null){			
		m_fragment_share = new Face_Share_Fragment(m_PageClickListener, shareControl);
		m_fragment_share.setPenClickListener(clickListener);
		m_fragment_share.setIsbroadcast(isbro);
		setZoomMode(bZoom);
		m_fragment_share.setPadSizeMode(padSizeMode);
//		}
		return m_fragment_share;
	}
	public void setZoomMode(boolean bZoom){
		if(m_fragment_share!=null){
			m_fragment_share.setZoomMode(bZoom);
		}
	}
}

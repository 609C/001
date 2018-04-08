package com.meeting.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.main.mme.view.DocumentSelectActivity;
import com.main.mme.view.DocumentSelectActivity.DocumentSelectActivityDelegate;
import com.utils.BaseFragment;
import com.utils.Utitlties;
import com.utils.WeiyiMeetingClient;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.weiyicloud.whitepad.NotificationCenter;
import com.weiyicloud.whitepad.NotificationCenter.NotificationCenterDelegate;
import com.weiyicloud.whitepad.ShareDoc;
import com.weiyicloud.whitepad.SharePadMgr;
import com.weiyicloud.whitepad.SharePadMgr.DataChangeListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import info.emm.meeting.Session;

@SuppressLint("DefaultLocale")
public class DocFragment extends BaseFragment implements OnClickListener,
DataChangeListener, NotificationCenterDelegate,
DocumentSelectActivityDelegate {

	public static abstract interface DocFragmentDelegate {
		public void didFinish();
	}

	LinearLayout lin_back;
	TextView txt_meeting_host;
	TextView txt_upload;
	//	private PopupWindow pop_share_doc;
	static String m_strShareFilePath = "";
	//	static int TAKE_SHARE_PHOTO = 21;
	//	static int GET_SHARE_FILE = 20; 
	private String m_strMeetingID;
	private String m_strUserName;
	private RelativeLayout relativeLayout1;
	private ListView doc_list;
	AlertDialog dialog;
	public DocFragmentDelegate delegate;
	//	private boolean isSycn;
	@SuppressLint("DefaultLocale")
	private BaseAdapter docListAdapter = new BaseAdapter() {

		@SuppressLint("DefaultLocale")
		@Override
		public View getView(int arg0, View convertView, ViewGroup arg2) {
			final ShareDoc dc = SharePadMgr.getInstance()
					.getShareDocbyIndex(arg0);
			// Log.e("emm","getview docid="+dc.docID);
			ViewHolder holder = null;
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			if (convertView == null) {
				holder = new ViewHolder();  
				convertView = inflater.inflate(UZResourcesIDFinder.getResLayoutID("doc_list"), null);
				holder.imgDoc = (ImageView) convertView
						.findViewById(UZResourcesIDFinder.getResIdID("img_doc"));
				holder.txtDoc = (TextView) convertView
						.findViewById(UZResourcesIDFinder.getResIdID("txt_doc"));
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (SharePadMgr.getInstance().getCurrentShareDoc()!=null&&dc.docID == SharePadMgr.getInstance()
					.getCurrentShareDoc().docID) {
				convertView.setBackgroundResource(UZResourcesIDFinder.getResColorID("button_color"));
			} else {
				convertView.setBackgroundResource(UZResourcesIDFinder.getResColorID("white"));
			}

			if (dc.docID == 0) {
				holder.imgDoc.setImageResource(UZResourcesIDFinder.getResDrawableID("doc_icon_01"));
			} else {
				String tempstr = dc.fileName.substring(
						dc.fileName.lastIndexOf(".")).toLowerCase();
				// Log.e("emm", "tempstr=" + tempstr);
				if (tempstr.equals(".doc") || tempstr.equals(".docx")) {

					holder.imgDoc.setImageResource(UZResourcesIDFinder.getResDrawableID("doc_icon_02"));

				} else if (tempstr.equals(".xls") || tempstr.equals(".xlsx")
						|| tempstr.equals(".xlt") || tempstr.equals(".xlsm")) {

					holder.imgDoc.setImageResource(UZResourcesIDFinder.getResDrawableID("doc_icon_03"));

				} else if (tempstr.equals(".ppt") || tempstr.equals(".pptx")
						|| tempstr.equals(".pps") || tempstr.equals(".pos")) {

					holder.imgDoc.setImageResource(UZResourcesIDFinder.getResDrawableID("doc_icon_04"));

				} else if (tempstr.equals(".pdf")) {

					holder.imgDoc.setImageResource(UZResourcesIDFinder.getResDrawableID("doc_icon_05"));

				} else if (tempstr.equals(".txt")) {

					holder.imgDoc.setImageResource(UZResourcesIDFinder.getResDrawableID("doc_icon_06"));

				} else if (tempstr.equals(".bmp") || tempstr.equals(".gif")
						|| tempstr.equals(".jpeg") || tempstr.equals(".pcx")
						|| tempstr.equals(".psd") || tempstr.equals(".tiff")
						|| tempstr.equals(".png") || tempstr.equals(".svg")
						|| tempstr.equals(".jpg")) {

					holder.imgDoc.setImageResource(UZResourcesIDFinder.getResDrawableID("doc_icon_07"));
				}
			}

			holder.txtDoc.setText(dc.fileName);

			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					//					if (MeetingSession.getInstance().getChairManID() == MeetingSession
					//							.getInstance().getMyPID()) {

					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivity());
					LayoutInflater layoutInflater = (LayoutInflater) getActivity()
							.getSystemService(
									Context.LAYOUT_INFLATER_SERVICE);
					View view = layoutInflater.inflate(
							UZResourcesIDFinder.getResLayoutID("popup_doc_control"), null);
					builder.setView(view);
					// builder.create();
					dialog = builder.show();

					dialog.setCanceledOnTouchOutside(true);

					TextView txt_open = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("txt_open_doc"));
					TextView txt_del = (TextView) view
							.findViewById(UZResourcesIDFinder.getResIdID("txt_del_doc"));

					if(dc.docID==0)
						txt_del.setVisibility(View.GONE);
					txt_open.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							if(WeiyiMeetingClient.RequestHost_Allow == Session.getInstance().getUserMgr().getSelfUser().getHostStatus()
									|| WeiyiMeetingClient.getInstance().getMyPID()==WeiyiMeetingClient.getInstance().getChairManID()){
								Session.getInstance().changeDoc(dc.docID, dc.currentPage);
								Session.getInstance().sendShowPage(dc.docID, dc.currentPage);
							}else{
								Session.getInstance().changeDoc(dc.docID, dc.currentPage);
							}

							if(delegate!=null)
								delegate.didFinish();
							m_FragmentContainer.removeFromStack(DocFragment.this);

							dialog.dismiss();
						}
					});
					txt_del.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							if(WeiyiMeetingClient.RequestHost_Allow == Session.getInstance().getUserMgr().getSelfUser().getHostStatus()
									|| WeiyiMeetingClient.getInstance().getMyPID()==WeiyiMeetingClient.getInstance().getChairManID()){
								// monitor MeetingMgr.DELMEETING_DOC
								NotificationCenter.getInstance().addObserver(DocFragment.this,Session.DELMEETING_DOC);
								int mid = Integer.parseInt(WeiyiMeetingClient
										.getInstance().getM_strMeetingID());								
								Session.getInstance().delMeetingFile(
										mid, dc.docID);
							}else{
								Toast.makeText(getActivity(), getString(UZResourcesIDFinder.getResStringID("delete_doc_remind")), Toast.LENGTH_SHORT).show();
							}
							dialog.dismiss();

						}
					});
				}
			});
			return convertView;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return SharePadMgr.getInstance()
					.getShareDocCount();
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Bundle bundle = getArguments();
		m_strMeetingID = bundle.getString("m_strMeetingID");
		m_strUserName = bundle.getString("m_strUserName");
		//		SharedPreferences sp = getActivity().getSharedPreferences("userInfo", getActivity().MODE_PRIVATE);
		//		isSycn = sp.getBoolean("isSycn", false);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_BREAK);
		NotificationCenter.getInstance().addObserver(this,
				WeiyiMeetingClient.NET_CONNECT_FAILED);
		UZResourcesIDFinder.init(getActivity().getApplicationContext());
		if (fragmentView == null) {
			fragmentView = inflater.inflate(UZResourcesIDFinder.getResLayoutID("doc_fragment"), null);
			lin_back = (LinearLayout) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("back"));
			txt_meeting_host = (TextView) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("Meeting_Host"));
			txt_upload = (TextView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("txt_upload"));
			relativeLayout1 = (RelativeLayout) fragmentView
					.findViewById(UZResourcesIDFinder.getResIdID("relativeLayout1"));
			doc_list = (ListView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("doc_list"));
			lin_back.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					m_FragmentContainer.removeFromStack(DocFragment.this);
				}
			});
			txt_meeting_host.setText(UZResourcesIDFinder.getResStringID("str_doc_list"));
			txt_upload.setOnClickListener(this);
			doc_list.setAdapter(docListAdapter);

			txt_upload.setVisibility(View.GONE);
			SharePadMgr.getInstance()
			.addOnDataChangeListener(this);
		} else {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		//		initpop(inflater);
		return fragmentView;
	}


	@Override
	public void onClick(View arg0) {
		int nid = arg0.getId();
		if (nid == UZResourcesIDFinder.getResIdID("txt_upload")) {
			DocumentSelectActivity selectActivity = new DocumentSelectActivity();
			selectActivity.delegate = this;
			m_FragmentContainer.PushFragment(selectActivity);

		}
	}

	//	@Override
	//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	//		if (requestCode == TAKE_SHARE_PHOTO) {
	//			Log.e("emm", "resumeLocalVideo begin");
	//			MeetingSession.getInstance().resumeLocalVideo();
	//			Log.e("emm", "resumeLocalVideo end");
	//		}
	//		if ((requestCode == TAKE_SHARE_PHOTO || requestCode == GET_SHARE_FILE)) {
	//
	//			if (resultCode != Activity.RESULT_OK)
	//				return;
	//		
	//			String strFile = "";
	//			if (requestCode == TAKE_SHARE_PHOTO) {
	//				strFile = m_strShareFilePath;			
	//			} else {
	//				Uri uri = data.getData();
	//				strFile = Utitlties.getPath(uri);
	//			}
	//
	//			Log.e("emm","docFrame onActivityResult uploadfile begin***************");
	//			
	//			uploadFile(strFile);
	//
	//			Log.e("emm",
	//					"docFrame onActivityResult uploadfile end***************");
	//
	//			super.onActivityResult(requestCode, resultCode, data);
	//		}
	//	}


	@Override
	public void onResume() {
		super.onResume();
		Log.e("emm", "docFrame onresume********************");
	}

	public String scaleAndSaveImage(String strPath, float maxWidth,
			float maxHeight, int quality, boolean cache) {
		String picLastName = strPath.substring(strPath.lastIndexOf(".") + 1).toLowerCase();
		if (picLastName.equals("bmp") 
				|| picLastName.equals("jpeg")  
				|| picLastName.equals("png") 
				|| picLastName.equals("jpg")) {
			Bitmap bitmap = null;
			try {
				InputStream in = new FileInputStream(strPath);
				BitmapFactory.Options opts = new BitmapFactory.Options();
				try {
					bitmap = BitmapFactory.decodeStream(in, null, opts);
				} catch (OutOfMemoryError e) {
					//
					return null;
				}

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

			if (bitmap == null) {
				return null;
			}
			float photoW = bitmap.getWidth();
			float photoH = bitmap.getHeight();
			if (photoW == 0 || photoH == 0) {
				return null;
			}
			float scaleFactor = Math.max(photoW / maxWidth, photoH / maxHeight);

			Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,
					(int) (photoW / scaleFactor), (int) (photoH / scaleFactor),
					true);
			String picUrl = strPath.substring(0, strPath.lastIndexOf("/"));
			String picName = "/_1_2"
					+ strPath.substring(strPath.lastIndexOf("/") + 1);

			File f = null;
			try {
				f = new File(Utitlties.getCacheDir(getActivity()
						.getApplicationContext()), picName);

			} catch (Exception e) {
				f = new File(picUrl, picName);
			}


			if (f.exists()) {
				f.delete();
			}
			try {
				FileOutputStream out1 = new FileOutputStream(f);
				if (picLastName.equalsIgnoreCase("jpg")
						|| picLastName.equalsIgnoreCase("jpeg")) {
					scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out1);
				} else if (picLastName.equalsIgnoreCase("png")) {
					scaledBitmap.compress(Bitmap.CompressFormat.PNG, 90, out1);
				}
				out1.flush();
				out1.close();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				return Utitlties.getCacheDir(getActivity().getApplicationContext())
						+ picName;
			} catch (Exception e) {
				return picUrl + picName;
			}
		}else if (picLastName.equals("doc") || picLastName.equals("docx")||picLastName.equals("xls") || picLastName.equals("xlsx")
				|| picLastName.equals("xlt") || picLastName.equals("xlsm")||picLastName.equals("ppt") || picLastName.equals("pptx")
				|| picLastName.equals("pps") || picLastName.equals("pos")||picLastName.equals("pdf")||picLastName.equals("txt")) {
			return strPath;
		} else {
			return null;
		}

	}

	@Override
	public boolean onBackPressed() {
		m_FragmentContainer.removeFromStack(this);
		return true;
	}

	class ViewHolder {
		ImageView imgDoc;
		TextView txtDoc;
	}

	@Override
	public void onChange() {
		docListAdapter.notifyDataSetChanged();
	}

	@Override
	public void didReceivedNotification(int arg0, Object... arg1) {
		switch (arg0) {
		case WeiyiMeetingClient.NET_CONNECT_BREAK:

			SharePadMgr.getInstance()
			.removeOnDataChangeListener(this);
			break;

		case WeiyiMeetingClient.NET_CONNECT_FAILED:

			SharePadMgr.getInstance()
			.removeOnDataChangeListener(this);
			break;

		case Session.DELMEETING_DOC:

			// adapter notifydatachange,
			// selNextDoc or before doc,or whitedoc

			int docid = (Integer) arg1[0];
			boolean isCurrentDoc = SharePadMgr.getInstance().isCurrentDoc(docid);

			ShareDoc dc = SharePadMgr.getInstance()
					.getShareDocbyId(docid);
			if (dc == null)
				return;
			Session.getInstance().sendDocChange(dc.docID, true,
					dc.fileName, dc.fileUrl, dc.pageCount);

			if (isCurrentDoc) {
				dc = SharePadMgr.getInstance()
						.getNextDoc(docid);
				if (dc == null)
					return;
				Session.getInstance()
				.changeDoc(dc.docID, dc.currentPage);
				Session.getInstance().sendShowPage(dc.docID,
						dc.currentPage);

			} else
				SharePadMgr.getInstance()
				.OnReceiveDocChange(false, dc);

			// show next or show before or show blank
			// notifyDataChange
			break;

		}
	}

	@Override
	public void onDestroy() {
		SharePadMgr.getInstance()
		.removeOnDataChangeListener(this);
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.NET_CONNECT_BREAK);
		NotificationCenter.getInstance().removeObserver(this,
				WeiyiMeetingClient.NET_CONNECT_FAILED);

		super.onDestroy();
	}

	@Override
	public void didSelectFile(DocumentSelectActivity activity, String path,
			String name, String ext, long size) {
		//uploadFile(path);
		//m_FragmentContainer.removeFromStack(this);
	}
}

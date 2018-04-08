package com.meeting.ui;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.utils.BaseFragment;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

@SuppressLint("ValidFragment")
public class WebViewFragment extends BaseFragment {

//	private TextView txt_linkname;
	private WebView web_linkurl;
	private OnClickListener m_PageClickListener;
	private String m_url="";
	
	@SuppressLint("ValidFragment")
	public WebViewFragment(OnClickListener listener) {
		m_PageClickListener = listener;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		UZResourcesIDFinder.init(getActivity().getApplicationContext());
		if (fragmentView == null) {
			fragmentView = inflater.inflate(UZResourcesIDFinder.getResLayoutID("webview_fragment"), null);
			web_linkurl = (WebView) fragmentView.findViewById(UZResourcesIDFinder.getResIdID("web_linkurl"));
//			txt_linkname.setText(MeetingMgr.getInstance().getLinkName());
			//String url = "";
			/*if (MeetingSession.getLinkUrl().startsWith("http://")) {
				url = MeetingSession.getLinkUrl();
			} else {
				url = "http://" + MeetingSession.getLinkUrl();
			}*/
			
			//Log.d("emm", "linkUrl:"+url);
			
			web_linkurl.getSettings().setJavaScriptEnabled(true);
			web_linkurl.getSettings().setDatabaseEnabled(true);
			web_linkurl.getSettings().setDomStorageEnabled(true);
			web_linkurl.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
			web_linkurl.setWebChromeClient(new WebChromeClient());
			if(this.getActivity() != null && this.getActivity().getApplicationContext() != null)
			{
				Log.d("emm", "got context");
				String appCachPath = this.getActivity().getApplicationContext().getCacheDir().getAbsolutePath();
				web_linkurl.getSettings().setAppCachePath(appCachPath);
			}
			web_linkurl.getSettings().setAllowFileAccess(true);
			web_linkurl.getSettings().setBlockNetworkImage(false);
			web_linkurl.getSettings().setBlockNetworkLoads(false);
			web_linkurl.setDrawingCacheEnabled(true);
			web_linkurl.getSettings().setEnableSmoothTransition(true);
			web_linkurl.getSettings().setPluginState(PluginState.ON);
			web_linkurl.getSettings().setRenderPriority(RenderPriority.HIGH);
			web_linkurl.getSettings().setUseWideViewPort(true);
			web_linkurl.getSettings().setLoadWithOverviewMode(true);
			web_linkurl.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
//			web_linkurl.addJavascriptInterface(Invoker, "WebViewInvoker");

//			wv.setWebViewClient(new WebViewClient() {
//				@Override
//				public boolean shouldOverrideUrlLoading(WebView view, String url) {
//					loadurl(view, url);
//					return false;
//				}
//			});
			
			web_linkurl.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url)
				{
					view.loadUrl(url);
					return false;
				}
			});
//			web_linkurl.setOnKeyListener(new View.OnKeyListener() {
//				@Override
//				public boolean onKey(View v, int keyCode, KeyEvent event) {
//					if (event.getAction() == KeyEvent.ACTION_DOWN) {
//						if (keyCode == KeyEvent.KEYCODE_BACK
//								&& web_linkurl.canGoBack()) {
//							web_linkurl.goBack(); // 閸氬酣锟斤拷
//							return true; // 瀹告彃顦╅悶锟�
//						}
//					}
//					return false;
//				}
//			});
			
			Log.d("emm", "linkUrl js enable:"+web_linkurl.getSettings().getJavaScriptEnabled());
			if(!m_url.isEmpty())
				web_linkurl.loadUrl(m_url);
//			web_linkurl.loadUrl("javascript:toastr.error('asdfaaa')");
//			txt_linkname.setOnClickListener(m_PageClickListener);
//			web_linkurl.setOnClickListener(m_PageClickListener);
		} else {
			Log.d("emm", "got fragmentView");
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null) {
				parent.removeView(fragmentView);
			}
		}
		return fragmentView;
	}
	public void loadUrl(String url)
	{
		this.m_url = url;
		if(web_linkurl!=null)
			web_linkurl.loadUrl(url);
	}
	@Override
	public void onResume() {
		super.onResume();
	}
}

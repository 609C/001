package info.emm.ui;

import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import info.emm.ui.Views.BaseFragment;
import info.emm.utils.StringUtil;
import info.emm.utils.UiUtil;
import info.emm.utils.Utitlties;
import info.emm.yuanchengcloudb.R;

public class WebFaceFg extends BaseFragment implements OnClickListener {

    private WebView webView;
    ImageView rightImageView;
    ImageView leftImageView;
    private ProgressDialog progDailog;

    private int titleNameId;
    private String url;
    private long timeout = 7000;
    Timer timer;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (progDailog != null && webView != null
                            && webView.getProgress() < 100) {
                        webView.stopLoading();
                        progDailog.dismiss();
                        UiUtil.showToast(webView.getContext(),
                                R.string.web_unsuccess);
                    }
                    break;

                default:
                    break;
            }
        }

        ;
    };

    public WebFaceFg() {

    }

    private void webViewSet() {
        WebSettings settings = webView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFinish) {
            return;
        }
        if (getActivity() == null) {
            return;
        }
        ((LaunchActivity) parentActivity).showActionBar();
        ((LaunchActivity) parentActivity).updateActionBar();
    }

    private void initView() {
        if (fragmentView == null) {
            return;
        }
        if (StringUtil.isEmpty(url)) {
            return;
        }
        progDailog = ProgressDialog.show(parentActivity, "Loading",
                "Please wait...", true);
        progDailog.setCancelable(false);

        webView = (WebView) fragmentView.findViewById(R.id.webview);
        webViewSet();
        leftImageView = (ImageView) fragmentView.findViewById(R.id.iv_left);
        leftImageView.setEnabled(webView.canGoBack());
        leftImageView.setOnClickListener(this);
        rightImageView = (ImageView) fragmentView.findViewById(R.id.iv_right);
        rightImageView.setEnabled(webView.canGoForward());
        rightImageView.setOnClickListener(this);
        fragmentView.findViewById(R.id.iv_refresh).setOnClickListener(this);
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // TODO Auto-generated method stub
                super.onPageStarted(view, url, favicon);
                progDailog.show();
                timer = new Timer();
                TimerTask task = new TimerTask() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Utitlties.RunOnUIThread(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                if (webView.getProgress() < 100) {
                                    Log.e("TAG", "++++++++++++++++++++");
                                    Message msg = new Message();
                                    msg.what = 1;
                                    mHandler.sendMessage(msg);
                                    timer.cancel();
                                    timer.purge();
                                }
                            }
                        });

                    }
                };
                timer.schedule(task, timeout);
            }

            // @Override
            // public boolean shouldOverrideUrlLoading(WebView view, String url)
            // {
            // progDailog.show();
            // view.loadUrl(url);
            // return true;
            // }

            @Override
            public void onPageFinished(WebView view, final String url) {
                Log.e("TAG", "---------------" + webView.getProgress());
                timer.cancel();
                timer.purge();
                progDailog.dismiss();
                leftImageView.setEnabled(webView.canGoBack());
                rightImageView.setEnabled(webView.canGoForward());
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Toast.makeText(getActivity(), "��Ǹ����ʱ�޷�����", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.webface, container, false);
        } else {
            ViewGroup parent = (ViewGroup) fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        initView();
        return fragmentView;
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        titleNameId = getArguments().getInt("titleName");
        url = getArguments().getString("url");
        return true;
    }

    @Override
    public void applySelfActionBar() {
        if (parentActivity == null) {
            return;
        }
        ActionBar actionBar = super.applySelfActionBar(true);
        actionBar.setTitle(titleNameId);

        TextView title = (TextView) parentActivity
                .findViewById(R.id.action_bar_title);
        if (title == null) {
            final int subtitleId = parentActivity.getResources().getIdentifier(
                    "action_bar_title", "id", "android");
            title = (TextView) parentActivity.findViewById(subtitleId);
        }
        if (title != null) {
            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            title.setCompoundDrawablePadding(0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finishFragment();
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_left) {
            webView.goBack();

        } else if (id == R.id.iv_right) {
            webView.goForward();

        } else if (id == R.id.iv_refresh) {
            webView.reload();

        } else {
        }
    }
}

package info.emm.utils;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class HttpUtil {
	private final static int OUTTIME = 15000;
	private  static String TAG = HttpUtil.class.getName();
	public static JSONObject jsonPost(String url, List<NameValuePair> params) {
		if (params != null) {
			HttpClient client = new DefaultHttpClient();
			HttpPost httpRequest = new HttpPost(url);
			HttpResponse httpResponse;
			httpRequest.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, OUTTIME);//ÇëÇó³¢ÊÔ
			httpRequest.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, OUTTIME);//Á¬½Ó³¢ÊÔ
			try {
				httpRequest.setEntity(new UrlEncodedFormEntity(params,
						HTTP.UTF_8));
				httpRequest.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");

				httpResponse = client.execute(httpRequest);
				Log.e(TAG, "link code = "+httpResponse.getStatusLine().getStatusCode());
				if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String str = EntityUtils.toString(httpResponse.getEntity(),
							"UTF8");
					return new JSONObject(str);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (client != null)
					client.getConnectionManager().shutdown();
			}
		}
		return null;
	}
	public static JSONArray jsonArrPost(String url, List<NameValuePair> params) {
		if (params != null) {
			HttpClient client = new DefaultHttpClient();
			HttpPost httpRequest = new HttpPost(url);
			HttpResponse httpResponse;
			httpRequest.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, OUTTIME);//ÇëÇó³¢ÊÔ
			httpRequest.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, OUTTIME);//Á¬½Ó³¢ÊÔ
			try {
				httpRequest.setEntity(new UrlEncodedFormEntity(params,
						HTTP.UTF_8));
				httpRequest.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
				httpResponse = client.execute(httpRequest);
//				ToolsUtil.showLog("link code = "
//						+ httpResponse.getStatusLine().getStatusCode());
				Log.e(TAG, "link code = "+httpResponse.getStatusLine().getStatusCode());
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					String str = EntityUtils.toString(httpResponse.getEntity(),
							"UTF8");
					return new JSONArray(str);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				if (client != null)
					client.getConnectionManager().shutdown();
			}
		}
		return null;
	}

}

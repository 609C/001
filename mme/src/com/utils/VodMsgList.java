package com.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VodMsgList
{
	protected JSONArray m_MsgArray;// 消息列表
	protected int m_curPageMsgPos = -1;
	protected int m_curPageStartTime = -1;
	protected int m_curPageStopTime = -1;
	protected boolean m_sequential = false;


	// list 消息列表，每个元素的结构是[body='', ts='']
	// sequential 是否严格按顺序执行,比如白板画笔消息就是
	public VodMsgList(JSONArray list, boolean sequential)
	{
		m_MsgArray = list;
//		m_MsgArray.sort(compareFunction);
		bubble_sort(m_MsgArray);
		m_sequential = sequential;
	}
	public void reSet(){
		m_curPageMsgPos = -1;
		m_curPageStartTime = -1;
		m_curPageStopTime = -1;
	}

	private void bubble_sort(JSONArray jsa)
	{
		for (int i = 0; i < jsa.length(); i++)
		{
			for (int j = i; j < jsa.length(); j++)
			{
				try {
					int ts_i = ((JSONObject)jsa.get(i)).getInt("ts");
					int ts_j = ((JSONObject)jsa.get(j)).getInt("ts");
					if (ts_i > ts_j)
					{
						JSONObject temp = jsa.getJSONObject(i);
						jsa.put(i, jsa.getJSONObject(j));
						jsa.put(j, temp);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	public JSONArray GetMsgArray()
	{
		return m_MsgArray;
	}

	public JSONObject OnTS(int tm) throws JSONException
	{
		if(m_curPageStartTime >= 0 && tm >= m_curPageStartTime && (tm < m_curPageStopTime || m_curPageStopTime == -1))
			return null;

		int pos = FindPageMsgByTime(tm, (m_curPageStopTime >= 0 && tm >= m_curPageStopTime) ? (m_curPageMsgPos + 1) : 1);
		if(pos<0 || pos>=m_MsgArray.length())
		{
			m_curPageMsgPos = -1;
			m_curPageStartTime = -1;
			m_curPageStopTime = -1;
			return null;
		}

		int multiMsgStartPos = pos;
		boolean needClear = false;
		if(m_sequential)
		{
			if(pos < m_curPageMsgPos)
			{
				// 清空，从头开始
				needClear = true;
				multiMsgStartPos = 0;
			}
			else if(pos > m_curPageMsgPos)
			{
				// 从当前位置的下一条开始，执行到目标位置
				multiMsgStartPos = m_curPageMsgPos + 1;
			}
			else
			{
				// 相等？闹哪样？
				return null;
			}
		}

		m_curPageMsgPos = pos;
		m_curPageStartTime = ((JSONObject)m_MsgArray.get(pos)).getInt("ts");
		m_curPageStopTime = (pos == m_MsgArray.length() - 1) ? -1 : ((JSONObject)m_MsgArray.get(pos+1)).getInt("ts");

		JSONObject ret = new JSONObject();
		ret.put("pos", pos);
		ret.put("clear", needClear);
		ret.put("msgs", new JSONArray());

		for(int i= multiMsgStartPos; i <= pos; ++i)
		{
			((JSONArray)ret.get("msgs")).put(m_MsgArray.get(i));
		}

		return ret;
	}

	private int compareFunction(JSONObject object1, JSONObject object2)
	{
		int ts1 = 0;
		int ts2 = 0;
		try {
			ts1 = object1.getInt("ts");
			ts2 = object2.getInt("ts");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(ts1 < ts2)
			return -1;
		if(ts1 == ts2)
			return 0;
		if(ts1 > ts2)
			return 1;

		return 0;
	}

	private int FindPageMsgByTime(int tm, int indexFrom)
	{
		if (m_MsgArray.length() == 0)
			return -1;
		int i = Math.max(1, indexFrom);
		for( ;i<m_MsgArray.length(); ++i)
		{
			try {
				if(((JSONObject)m_MsgArray.get(i)).getInt("ts") > tm)
				{
					// 找到第一个时间在当前时间之后的msg
					break;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			if(i > 0 && ((JSONObject)m_MsgArray.get(i-1)).getInt("ts") <= tm)
			{
				return i-1;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return -1;
	}
}
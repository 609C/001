package info.emm.utils;


import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

public class MaxLengthEdite implements TextWatcher{
	int maxlen;
	EditText edit;
	
	public MaxLengthEdite(int maxlen,EditText edit){
		this.maxlen = maxlen;
		this.edit = edit;
	}

	@Override
	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub
		 Log.i("TAG",edit.getText().toString());
		 Log.i("TAG",edit.getText().toString().length()+"");
		 String str = edit.getText().toString();
		 byte[] by = str.getBytes();
		 Log.e("TAG", by.length+"");
		 if (maxlen==edit.getText().toString().length()&&maxlen==6) {
				
			}
			if (maxlen==by.length&&maxlen==ConstantValues.CREATE_NAMESIZE_MAX) {
//				String sTip = String.format(LocaleController.getString("createNameSize", R.string.MaxNamenum), ConstantValues.CREATE_NAMESIZE_MAX);
//				UIHelper.ToastMessage(edit.getContext(), sTip);
				
			}
			if (maxlen==by.length&&maxlen==ConstantValues.CREATE_FROUMTITLE_MAX) {
//				String sTip = String.format(LocaleController.getString("forumTitleSize", R.string.MaxFroumTitlenum), ConstantValues.CREATE_NAMESIZE_MAX);
//				UIHelper.ToastMessage(edit.getContext(), sTip);
			}
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		Editable editable = edit.getText();
		byte[] bt = editable.toString().getBytes();
		int len = bt.length;
		
		if(len > maxlen)
		{
			int selEndIndex = Selection.getSelectionEnd(editable);
			String str = editable.toString();
			//��ȡ���ַ���
			String newStr = subStringByByte(str, maxlen);
			edit.setText(newStr);
			editable = edit.getText();
			
			//���ַ����ĳ���
			int newLen = editable.length();
			//�ɹ��λ�ó����ַ�������
			if(selEndIndex > newLen)
			{
				selEndIndex = editable.length();
			}
			//�����¹�����ڵ�λ��
			Selection.setSelection(editable, selEndIndex);
				}
	    }
	private static String subStringByByte(String str, int len) {
		String result = null;
		if (str != null) {
			byte[] a = str.getBytes();
			if (a.length <= len) {
				result = str;
			} else if (len > 0) {
				result = new String(a, 0, len);
				int length = result.length();
				if (str.charAt(length - 1) != result.charAt(length - 1)) {
					if (length < 2) {
						result = null;
					} else {
						result = result.substring(0, length - 1);
					}
				}
			}
		}
		return result;
	}

	}


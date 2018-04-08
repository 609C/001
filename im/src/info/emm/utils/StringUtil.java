/**
 * @Title        : StringUtil.java
 *
 * @Package      : info.emm.ui.utils
 *
 * @Copyright    : Copyright - All Rights Reserved.
 *
 * @Author       : He,Zhen hezhen@yunboxin.com
 *
 * @Date         : 2014-6-26
 *
 * @Version      : V1.00
 */
package info.emm.utils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

import info.emm.messenger.TLRPC;
import info.emm.ui.ApplicationLoader;
import info.emm.utils.HanziToPinyin.Token;

public class StringUtil 
{
	private final static Pattern emailer = Pattern
			.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0 || str.equals("null");
	}

	/**
	 * 从字符串中筛选出阿拉伯数字
	 **/
	public static String getNumFrom(String para) {
		String str = para.trim();
		StringBuilder numStr = new StringBuilder();
		if (str != null && !" ".equals(str)) {

			for (int i = 0, j = str.length(); i < j; i++) {
				if (str.charAt(i) >= 48 && str.charAt(i) <= 57) {
					numStr.append(str.charAt(i));
				}
			}
		}
		return numStr.toString();
	}

	public static String getStringFromRes(int resid) {
		return ApplicationLoader.getContext().getString(resid);
	}
	
	public static boolean isNum(String str) {
		return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
	}
	/**
	 * 验证手机格式
	 */
	public static boolean isMobileNO(String mobiles) {
		String telRegex = "[1][3578]\\d{9}";//"[1]"代表第1位为数字1，"[3578]"代表第二位可以为3、5、7\8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
		if (isEmpty(mobiles)) return false;
		else return mobiles.matches(telRegex);
    }
	 /**
	 * @param input
	 * @return
	 * @Description 获取汉字拼音
	 */
	public static String getPinYin(String input) {  
	        ArrayList<Token> tokens = HanziToPinyin.getInstance().get(input);  
	        StringBuilder sb = new StringBuilder();  
	        if (tokens != null && tokens.size() > 0) {  
	            for (Token token : tokens) {  
	                if (Token.PINYIN == token.type) {  
	                    sb.append(token.target);  
	                } else {  
	                    sb.append(token.source);  
	                }  
	            }  
	        }  
	        return sb.toString().toLowerCase();  
	    }  
	/**
	 * @param source
	 * @return
	 * 
	 * @Description 获取一段汉字简拼
	 */
	public static String getFirstPinYin(String source) {
		if (!Arrays.asList(Collator.getAvailableLocales()).contains(
				Locale.CHINA)) {
			return source;
		}
		ArrayList<Token> tokens = HanziToPinyin.getInstance().get(source);
		if (tokens == null || tokens.size() == 0) {
			return source;
		}
		StringBuffer result = new StringBuffer();
		for (Token token : tokens) {
			if (token.type == Token.PINYIN) {
				result.append(token.target.charAt(0));
			} else {
				result.append("#");
			}
		}
		return result.toString();
	}
	public static String getUserName(TLRPC.User user) {
		String nameString = user.nickname;
		if (StringUtil.isEmpty(nameString)) {
			nameString = Utilities.formatName(user.first_name, user.last_name);
		}
		return nameString;
	}
	public static String getCompanyUserRemark(TLRPC.User user,String companyUserName) 
	{	
		if(user==null)
			return "";
	    if(!user.nickname.isEmpty() && user.nickname!=null && user.nickname.compareTo("null")!=0)
	    		return user.nickname;
	    return companyUserName;
	}
	/**
	 * @param nameStrings 0 nickname 1 firstName 2 lastName
	 * @return
	 * @Discription
	 */
	public static String getRemark(String...nameStrings) {
		String nameString = StringUtil.isEmpty(nameStrings[0])?Utilities.formatName(nameStrings[1], nameStrings[2]):nameStrings[0];
		return	 nameString;
	}
	public static boolean isEmail(String email) {
		if (email == null || email.trim().length() == 0)
			return false;
		return emailer.matcher(email).matches();
	}

}

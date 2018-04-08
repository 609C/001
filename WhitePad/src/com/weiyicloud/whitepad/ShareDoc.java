package com.weiyicloud.whitepad;


import java.util.concurrent.ConcurrentHashMap;
/**
 * 
 * @author Luis
 *
 */
public class ShareDoc implements Cloneable{
		
	public int docID;     			
	public String fileName; 	
	public String fileUrl;			
	public int	  pageCount;		
	public int   currentPage = 1;
	public boolean bIsBlank = false;
	public ConcurrentHashMap<Integer,String> docPageImages = new ConcurrentHashMap<Integer,String>();
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}
}


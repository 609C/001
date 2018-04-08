package com.weiyicloud.whitepad;


import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;

import java.util.ArrayList;

/**
 * 
 * @author Luis
 *
 */
public class TL_PadAction {

	public enum wbEventType{
		et_none,
		et_docAdd,
		et_docRemove,
		et_sharpAdd,
		et_sharpRemove,
		et_sharpChange;
	}

	public enum factoryType
	{
		ft_markerPen,//标记
		ft_arrowLine,//箭头
		ft_line,     //线
		ft_Rectangle,//矩形
		ft_Ellipse, //椭圆
		ft_Text;  //文本

		public static factoryType valueOf(int optInt) {
			// TODO Auto-generated method stub
			switch(optInt){
				case 0:return ft_markerPen;
				case 1:return ft_arrowLine;
				case 2:return ft_line;
				case 3:return ft_Rectangle;
				case 4:return ft_Ellipse;
				case 5:return ft_Text;
			}
			return null;
		}

	}

	public String sID;

	public int nDocID;

	public int nPageID;

	public factoryType nActionMode;

	public int nPenWidth;

	public int nPenColor;

	/**alActionPoint
	 *
	 *
	 */
	public ArrayList<PointF> alActionPoint = new ArrayList<PointF>();

	public String sText = "";

	public boolean bIsFill;

	public boolean bIsRelative;

	/////////////////////////////////////////////////////
	public PointF ptSizingEnd;

	public Rect CoverArea;

	public Region HotRegion;//区域(由一个或多个Rect组成)

	public boolean bSelect;

	public Path LinePath;  // nActionMode = 1
	///////////////////////////////////////////////////////////////////
}

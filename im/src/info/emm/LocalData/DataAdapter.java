package info.emm.LocalData;

public class DataAdapter {
	public DataAdapter(){}

	public int dataID;    // 保存ID（userid companyid departmentid）
	public int companyID; // 保存对于的companyid
	public int parentDeptID; // 保存parent dept id
	public int  version;// 版本号
	public String dataName;  // 保存name(usename companyname deptname)
	public String dataInfo;  //用户备用信息（phoneNum等）
	public String dataICO;   // 保存icom(userico companyico deptico)
	public boolean haveChild; // 保存是否存在子部门或用户，即是否需要显示箭头图标
	public boolean isCompany; // 是否为公司信息
	public boolean isUser; // 是否为用户信息
	public String sortLetters;//排序[a-z]
	public String pinyinName;
	//	public int other; //备用(显示公司后未读公告等)
	@Override
	public String toString() {
		return "DataAdapter [dataID=" + dataID + ", companyID=" + companyID + ", parentDeptID=" + parentDeptID
				+ ", version=" + version + ", dataName=" + dataName + ", dataInfo=" + dataInfo + ", dataICO=" + dataICO
				+ ", haveChild=" + haveChild + ", isCompany=" + isCompany + ", isUser=" + isUser + ", sortLetters="
				+ sortLetters + "]";
	}
	
}


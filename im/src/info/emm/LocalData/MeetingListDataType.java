package info.emm.LocalData;

/**
 * @author wangxm
 * 存放会议列表的数据类型
 */
public class MeetingListDataType {
	public MeetingListDataType(){}
	/**
	 * 对应的  周几  超出本周的写   月日
	 */
	public String meeting_time_format;
	/**
	 * 会议图标
	 */
	public String meeting_icon;
	/**
	 * 会议名称
	 */
	public String meeting_name;
	/**
	 * 会议开始时间
	 */
	public int meeting_start_time;
	/**
	 * 会议结束时间
	 */
	public int meeting_end_time;
	/**
	 * 会议主席主持人
	 */
	public String meeting_host_name;
	/**
	 * 创建者ID
	 */
	public int created_id;
	/**
	 * 会议ID
	 */
	public int meeting_id;
	/**
	 * 创建时间
	 */
	public int create_time;
}

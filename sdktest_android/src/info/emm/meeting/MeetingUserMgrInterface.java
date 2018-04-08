package info.emm.meeting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;

public interface MeetingUserMgrInterface {
	/**
	 * Function  getSelfUser
	 *
	 * @param
	 * @return MeetingUser
	 */
	// 获取本用户的用户对象
	public MeetingUser getSelfUser();

	/**
	 * Function  getCount
	 *
	 * @param
	 * @return int
	 */
	// 获取用户总数（不含隐身用户）
	public int getCount();

	// 获取用户总数（包含隐身用户）
	public int getCountNoHideUser();
	/**
	 * Function  getUser
	 *
	 * @param int peerID
	 * @return MeetingUser
	 */
	public MeetingUser getUser(int peerid);
	/**
	 * Function  getUserByMmeId
	 *
	 * @param int mmeID
	 * @return MeetingUser
	 */
	public MeetingUser getUserByMmeId(int mmeid);

	// 根据用户ID获取用户对象
	public MeetingUser getMeetingUser(int id);

	/**
	 * Function  getMeetingUser
	 *
	 * @param int index
	 * @return MeetingUser
	 */
	// 根据用户序号获取用户对象，用于遍历用户列表（包含隐身用户）
	public MeetingUser getUserByIndex(int index);

	// 根据用户序号获取用户对象，用于遍历用户列表（不含隐身用户）
	public MeetingUser getUserFromIndex(int index);
	public void delUser(int id);
	public void clear();
	/**
	 * 鎺掑簭
	 */
	public void reSort();
	public ArrayList<Integer> getThirdUids();
}

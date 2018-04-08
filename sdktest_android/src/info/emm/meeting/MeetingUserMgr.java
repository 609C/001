package info.emm.meeting;




import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MeetingUserMgr implements MeetingUserMgrInterface {

	public ConcurrentHashMap<Integer, MeetingUser> mapUsers = new ConcurrentHashMap<Integer, MeetingUser>(100, 1.0f, 2);
	//安排会议时邀请的user列表，xiaoyang add
	List<Integer> offlineids = new ArrayList<Integer>();
	//安排会议时邀请的user列表，xiaoyang add
	List<Integer> infoIds = new ArrayList<Integer>(mapUsers.keySet());
	public ArrayList<MeetingUser> usersHasVideo = new ArrayList<MeetingUser>();

	static MeetingUserMgr mInstance = null;

	private MeetingUser mMeetingUserSelf;

	/**
	 * Function  getSelfUser
	 *
	 * @param
	 * @return MeetingUser
	 */
	public MeetingUser getSelfUser()
	{
		synchronized (this) {
			if(mMeetingUserSelf == null)
				mMeetingUserSelf = new MeetingUser();
			return mMeetingUserSelf;
		}
	}
	/**
	 * Function  getCount
	 *
	 * @param
	 * @return int
	 */
	public int getCount()
	{
		int size = mapUsers.size();
		return size;
	}


	public int getCountNoHideUser()
	{
		int size = mapUsers.size();
		for (ConcurrentHashMap.Entry<Integer, MeetingUser> entry : mapUsers.entrySet()) {
			MeetingUser mu = mapUsers.get(entry.getKey());
			if (mu != null) {
				if(mu.isHideme())
					size--;
			}
		}
		return size;
	}
	/**
	 * Function  getUser
	 *
	 * @param int peerID
	 * @return MeetingUser
	 */
	public MeetingUser getUser(int peerid){
		MeetingUser mu = mapUsers.get(peerid);
		if(mMeetingUserSelf!=null &&mMeetingUserSelf.getPeerID() == peerid){
			mu = mMeetingUserSelf;
		}
		return mu;
	}
	/**
	 * Function  getUserByMmeId
	 *
	 * @param int mmeID
	 * @return MeetingUser
	 */
	public MeetingUser getUserByMmeId(int mmeid){
		int i = 0;
		for(Integer key : mapUsers.keySet()){
			if(mapUsers.get(key).getMmeID() == mmeid)
				return mapUsers.get(key);
			i++;
		}
		if(mMeetingUserSelf!=null &&mMeetingUserSelf.getMmeID() == mmeid){
			return mMeetingUserSelf;
		}
		return null;
	}

	public MeetingUser getMeetingUser(int id)
	{
		if(mMeetingUserSelf != null && id == this.mMeetingUserSelf.getPeerID())
			return mMeetingUserSelf;
		MeetingUser mu = mapUsers.get(id);
		return mu;
	}
	/**
	 * Function  getMeetingUser
	 *
	 * @param int index
	 * @return MeetingUser
	 */
	public MeetingUser getUserByIndex(int index){
		int i = 0;
		for(Integer key : infoIds){
			if(i == index)
			{
				MeetingUser mu = mapUsers.get(key);
				if(mu!=null)
					return mu;
			}
			i++;
		}

		return null;
	}

	public MeetingUser getUserFromIndex(int index){

		int i = 0;
		for(Integer key : infoIds){
			if(i == index)
			{
				MeetingUser mu = mapUsers.get(key);
				if(mu!=null)
				{
					if(!mu.isHideme())
						return mu;
					else
						index++;
				}
			}
			i++;
		}

		return null;
	}



	public void addUser(MeetingUser user)
	{
		if(user==null)
			return;
		if(mapUsers.get(user.getPeerID()) == null){

			mapUsers.put(user.getPeerID(), user);
		};
		infoIds = new ArrayList<Integer>(mapUsers.keySet());
	}
	public void delUser(int id){
		mapUsers.remove(id);
		infoIds = new ArrayList<Integer>(mapUsers.keySet());
	}
	public void clear(){
		mapUsers.clear();
		usersHasVideo.clear();
		reSort();
	}
	/**
	 * 鎺掑簭
	 */
	public void reSort(){
		infoIds = new ArrayList<Integer>(mapUsers.keySet());

		Collections.sort(infoIds, new Comparator<Integer>() {
			public int compare(Integer o1,Integer o2) {

				MeetingUser mu1 = getUser(o1);
				MeetingUser mu2 = getUser(o2);
				if(mu1==null || mu2 == null)return 0;
				int L1 = mu1.getUserLevel();
				int L2 = mu2.getUserLevel();
				if(L1==L2)
					return mu1.getName().compareTo(mu2.getName());
				return mu1.getUserLevel() - mu2.getUserLevel();
			}
		});
	}
	public ArrayList<Integer> getThirdUids()
	{
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i=0;i<getCount();i++)
		{
			MeetingUser user = getUserByIndex(i);
			if(user!=null)
				list.add(user.getThirdID());
		}
		return list;
	}
	public void addOffLineId(int id){
		offlineids.add(id);
	}
	public void reSortUserHasVideo(int doWhat){//用来判断是sip还是录制0录制，1sip
		usersHasVideo.clear();
		for (int i = 0; i < infoIds.size(); i++) {
			MeetingUser mu = getUserFromIndex(i);
			if(mu!=null){
				if(doWhat == 0){
					if(mu.ishasVideo()){
						usersHasVideo.add(mu);
					}
				}else if(doWhat == 1){
					if(mu.ishasVideo()&&mu.getClientType()<4&&mu.getClientType()>0){
						usersHasVideo.add(mu);
					}
				}
			}

		}
	}


}

package info.emm.meeting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class MeetingUser implements MeetingUserInterface,Cloneable{

	private int mClientType;//0:flash,1:PC,2:IOS,3:andriod,4:tel,5:h323	6:html5 7:sip
	private int mID;
	private int mThirdID;
	private int mMmeID;
	private boolean mIsChairMan;
	private int mRole;//0 for common user, 1 for chairman, 2 for sideuser
	private boolean mhasVideo;
	private boolean mVideoStatus;
	private boolean mhasAudio;
	private int mAudioStatus;	
	private boolean mbPrivateChat;
	private String mName="";
	private int UnreadMsg;
	private String headurl="";

	public String getHeadurl() {
		return headurl;
	}
	public void setHeadurl(String headurl) {
		this.headurl = headurl;
	}
	//mHostStatusȡֵ��Χ:RequestHost_Disable = 0;RequestHost_Allow = 1;RequestHost_Pending = 2;
	private int mHostStatus;
	private boolean mWatch =false;
	//0�������Լ���1�����Լ�
	private int mHideme = 0; 
	//xiaoyang add
	private boolean mIsOnLine;
	
	private ArrayList<Camera> m_cameraList = new ArrayList<MeetingUser.Camera>();
	private Map<Integer, Camera> mapCamera = new HashMap<Integer, MeetingUser.Camera>();
	private int userImg = 0;//qxm add
	private String m_telNumber;
	public String getM_telNumber() {
		return m_telNumber;
	}
	public void setM_telNumber(String m_telNumber) {
		this.m_telNumber = m_telNumber;
	}
	public Map<Integer, Camera> getMapCamera() {
		return mapCamera;
	}
	public void setMapCamera(Map<Integer, Camera> mapCamera) {
		this.mapCamera = mapCamera;
	}
	public boolean ismIsOnLine() {
		return mIsOnLine;
	}
	public void setmIsOnLine(boolean mIsOnLine) {
		this.mIsOnLine = mIsOnLine;
	}
	public MeetingUser()
	{
		mThirdID = 0;
		mClientType = 3;
		mID = 0;
		mIsChairMan= false;
		mRole = 0;
		mhasVideo = true;
		mVideoStatus = false;
		mhasAudio = true;
		mAudioStatus = 0;
		mbPrivateChat = false;
		UnreadMsg = 0;
		mHostStatus = 0;
		mIsOnLine = true;
		userImg = 0;
	}
	public void clear(){
		mThirdID = 0;
		mClientType = 3;
		mID = 0;
		mIsChairMan= false;
		mRole = 0;
		mhasVideo = true;
		mVideoStatus = false;
		mhasAudio = true;
		mAudioStatus = 0;
		mbPrivateChat = false;
		UnreadMsg = 0;
		mHostStatus = 0;
		mWatch =false;
		userImg = 0;
		m_cameraList.clear();
	}

	public int getClientType() {
		return mClientType;
	}
	public void setClientType(int mClientType) {
		this.mClientType = mClientType;
	}
	public int getPeerID() {
		return mID;
	}
	public void setPeerID(int mID) {
		this.mID = mID;
	}
	public void setThirdID(int mID){
		this.mThirdID = mID;
	}
	public int getThirdID()
	{
		return this.mThirdID;
	}
	public int getMmeID() {
		return mMmeID;
	}
	public void setMmeID(int mmeID) {
		this.mMmeID = mmeID;
	}
	public boolean isChairMan() {
//		return this.mID == Session.getInstance().getM_nChairmanID();
		return this.getRole() == 1;
	}
	//	public void setIsChairMan(boolean mIsChairMan) {
	//		this.mID == = mIsChairMan;
	//	}
	public int getRole() {
		return mRole;
	}
	public void setRole(int mRole) {
		this.mRole = mRole;
	}
	public boolean ishasVideo() {
		return mhasVideo;
	}
	public void sethasVideo(boolean mhasVideo) {
		this.mhasVideo = mhasVideo;
	}
	public boolean isVideoStatus() {
		return mVideoStatus;
	}
	public void setVideoStatus(boolean mVideoStatus) {
		this.mVideoStatus = mVideoStatus;
	}
	public boolean ishasAudio() {
		return mhasAudio;
	}
	public void sethasAudio(boolean mhasAudio) {
		this.mhasAudio = mhasAudio;
	}
	public int getAudioStatus() {
		return mAudioStatus;
	}
	public void setAudioStatus(int mAudioStatus) {
		this.mAudioStatus = mAudioStatus;
	}
	public void setHostStatus(int status){
		mHostStatus = status;
	}
	public int getHostStatus(){
		return mHostStatus;
	}
	public boolean isPrivateChat() {
		return mbPrivateChat;
	}
	public void setPrivateChat(boolean mbPrivateChat) {
		this.mbPrivateChat = mbPrivateChat;
	}
	public String getName() {
		return mName;
	}
	public void setName(String mName) {
		this.mName = mName;
	}

	public int getUnreadMsg() {
		return UnreadMsg;
	}

	public void setUnreadMsg(int unreadMsg) {
		UnreadMsg = unreadMsg;
	}

	//qxm add
	public int getUserImg() {
		return userImg;
	}
	public void setUserImg(int userImg) {
		this.userImg = userImg;
	}

	public JSONObject getJsonObject() {

		JSONObject userinfo = new JSONObject();
		try {
			userinfo.put("m_ClientType", getClientType());		
			userinfo.put("m_MeetBuddyID", getPeerID());
			userinfo.put("m_NickName", getName());
			userinfo.put("m_UserType", getRole());
			userinfo.put("m_HasVideo", ishasAudio());
			userinfo.put("m_HasVideo", ishasVideo());

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		return userinfo;
	}
	public int getUserLevel(){
		if(isChairMan())return 0;

		if(getHostStatus() == Session.RequestHost_Allow)return 1;

		if(getAudioStatus() == Session.RequestSpeak_Allow)return 2;

		if(getRole() != 2) return 3;

		return 4;
	}
	public void setWatch(boolean bWatch)
	{
		mWatch = bWatch;
	}
	public boolean getWatch()
	{
		return mWatch;
	}
	public void setHide(int hideme)
	{
		mHideme = hideme;
	}
	public boolean isHideme()
	{
		if(mHideme == 1)
			return true;
		return false;
	}
	//xiaoyang add
	public void addCamera(int index,String name,boolean bEnable,boolean bDefault){

		Camera ca = new Camera();
		ca.videoIndex = index;
		ca.videoName = name;
		ca.isEnable = bEnable;
		ca.bDefault = bDefault;
		mapCamera.put(index, ca);
		m_cameraList.clear();
		for (Integer in : mapCamera.keySet()) {
			if(mapCamera.get(in).isEnable&&!mapCamera.get(in).bDefault){				
				m_cameraList.add(mapCamera.get(in));
			}
		}

	}
	public void clearCamera(){
		m_cameraList.clear();
	}
	public int getCameraCount(){
		return m_cameraList.size();
	}
	public int getDefaultCameraIndex(){
		if(mapCamera.size()==0)
			return 0;
		for (Integer in : mapCamera.keySet()) 
		{
			Camera camera = mapCamera.get(in);
			if(camera.bDefault)
				return camera.videoIndex;
		}	
		return 0;
	}
	public boolean isCameraEnable(int index){
		//		for(int i=0;i<getCameraCount();i++)
		//		{
		Camera camera = m_cameraList.get(index);
		//			if(camera.videoIndex==index)
		return camera.isEnable;
		//		}	
		//		return false;
	}
	public String getCameraNameByIndex(int index){
		//		for(int i=0;i<getCameraCount();i++)
		//		{
		Camera camera = m_cameraList.get(index);
		//			if(camera.videoIndex==index)
		return camera.videoName;
		//		}	
		//		return "";
	}
	public int getCameraIndexByIndex(int index){
		//		for(int i=0;i<getCameraCount();i++)
		//		{
		Camera camera = m_cameraList.get(index);
		//			if(camera.videoIndex==index)
		return camera.videoIndex;
		//		}	
		//		return "";
	}
	public class Camera implements Cloneable{
		int videoIndex;
		String videoName;
		boolean isEnable;
		boolean bDefault ;
		public int getVideoIndex() {
			return videoIndex;
		}
		public void setVideoIndex(int videoIndex) {
			this.videoIndex = videoIndex;
		}
		public String getVideoName() {
			return videoName;
		}
		public void setVideoName(String videoName) {
			this.videoName = videoName;
		}
		public boolean isEnable() {
			return isEnable;
		}
		public void setEnable(boolean isEnable) {
			this.isEnable = isEnable;
		}
		public boolean isbDefault() {
			return bDefault;
		}
		public void setbDefault(boolean bDefault) {
			this.bDefault = bDefault;
		}
		@Override
		protected Object clone() throws CloneNotSupportedException {
			// TODO Auto-generated method stub
			return super.clone();
		}
	}
	@Override
	protected Object clone() throws CloneNotSupportedException{
		MeetingUser mu = null;
		mu = (MeetingUser) super.clone();
		ArrayList<Camera> tc = new ArrayList<MeetingUser.Camera>();
		Map<Integer, Camera> mtc = new HashMap<Integer, MeetingUser.Camera>();
		for (int i = 0; i < m_cameraList.size(); i++) {
			Camera ca = (Camera) m_cameraList.get(i).clone();
			tc.add(ca);
		}
		mu.m_cameraList = tc;
		for (Integer caid : mapCamera.keySet()) {
			Camera ca = (Camera) mapCamera.get(caid).clone();
			mtc.put(caid, ca);
		}
		mu.mapCamera = mtc;
		return mu;
	}
	
}

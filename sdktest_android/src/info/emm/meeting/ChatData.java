package info.emm.meeting;


public class ChatData{

	String name;
	String content;
	String time;
	String User_id;
	int User_img;
	boolean bispersonal = false;
	int ntoID;
	int nFromID;
	public int getnFromID() {
		return nFromID;
	}
	public void setnFromID(int nFromID) {
		this.nFromID = nFromID;
	}
	String sToName;
	
	//��Ϣ����
	private Type type;
	
	public enum Type{
		send,receive;
	}
	
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public int getUser_img() {
		return User_img;
	}
	public void setUser_img(int user_img) {
		User_img = user_img;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getUser_id() {
		return User_id;
	}
	public void setUser_id(String user_id) {
		User_id = user_id;
	}
	
	public void setPersonal(boolean bp){
		bispersonal = bp;
	}
	public boolean isPersonal(){
		return bispersonal;	
	}
	public void setToID(int nToid){
		ntoID = nToid;
	}
	public int getToID(){
		return ntoID;
	}
	public void setToName(String sTo){
		sToName = sTo;
	}
	public String getToName(){
		return sToName;
	}
}

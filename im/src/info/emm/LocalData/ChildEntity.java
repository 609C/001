package info.emm.LocalData;

public class ChildEntity {
	public int id;
	public String time;
	public String topic;
//	public String date;

	public ChildEntity(){

	}

	public ChildEntity(int id, String time, String topic,String date) {
		super();
		this.id = id;
		this.time = time;
		this.topic = topic;
//		this.date = date;
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
//	public String getDate(){
//		return date;
//	}
//	public void setDate(String date){
//		this.date = date;
//	}


}

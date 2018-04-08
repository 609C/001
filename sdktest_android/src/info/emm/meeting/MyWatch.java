package info.emm.meeting;

public class MyWatch {

	/**
	 * @param args
	 */
	private int peerid;
	private int cameraid;
	public int getPeerid() {
		return peerid;
	}
	public void setPeerid(int peerid) {
		this.peerid = peerid;
	}
	public int getCameraid() {
		return cameraid;
	}
	public void setCameraid(int cameraid) {
		this.cameraid = cameraid;
	}
	
	public MyWatch(int peerid,int cameraid){
		this.peerid = peerid;
		this.cameraid = cameraid;
	}
}

package info.emm.objects;

public class MRect {
	public int x;
	public int y;
	public int w;
	public int h;
	public MRect() {
		
	}
	public boolean isInTouch(float x,float y){
		if (x > this.x && x < (this.x + this.w) &&
			y > this.y && y < (this.y + this.h)){
				return true;
		}
		return false;
	}
	public void setMRect(int x,int y,int w,int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	public boolean isInTouch(float x){
		if (x > this.x && x < (this.x + this.w)){
				return true;
		}
		return false;
	}
}

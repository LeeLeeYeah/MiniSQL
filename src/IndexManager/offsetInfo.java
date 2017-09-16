package IndexManager;

import java.util.Vector;

public class offsetInfo {
	public Vector<Integer> offsetInfile;
	public Vector<Integer> offsetInBlock;
	public int length;
	public offsetInfo(){
		offsetInfile = new Vector<Integer>();
		offsetInBlock = new Vector<Integer>();
		length=0;
	}
}

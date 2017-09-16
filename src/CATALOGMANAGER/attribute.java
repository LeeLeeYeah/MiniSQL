package CATALOGMANAGER;


public class attribute{
	String attriName;	//字段名称
	String type;		//字段类型int float char  boolean
	int length;			//字段长度
	boolean isUnique;	
	//构造函数
	public attribute(String attriName,String type,int length,boolean isU){
		this.attriName=attriName;
		this.type=type;
		this.length=length;
		this.isUnique=isU;
	}
}

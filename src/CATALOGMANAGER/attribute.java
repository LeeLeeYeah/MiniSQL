package CATALOGMANAGER;


public class attribute{
	String attriName;	//�ֶ�����
	String type;		//�ֶ�����int float char  boolean
	int length;			//�ֶγ���
	boolean isUnique;	
	//���캯��
	public attribute(String attriName,String type,int length,boolean isU){
		this.attriName=attriName;
		this.type=type;
		this.length=length;
		this.isUnique=isU;
	}
}

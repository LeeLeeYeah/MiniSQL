package CATALOGMANAGER;

import java.util.Vector;

public class table{
	String tableName;			//����
	String primaryKey;			//������
	Vector<attribute>attributes;//��vector��ʽ����ֶ�
	Vector<index> indexes;		//��vector��ʽ��ű��ϵ�����	
	int indexNum;				//��������
	int attriNum;				//�ֶ�����
	int tupleNum;				//��¼����
	int tupleLength;			//������¼���ֽ���
	//�������ʱ�Ĺ��췽��
	public table(String tableName,Vector<attribute> attributes,String primaryKey){
		this.tableName=tableName;
		this.primaryKey=primaryKey;
		this.indexes=new Vector<index>();				
		this.indexNum=0;		
		this.attributes=attributes;
		this.attriNum=attributes.size();
		this.tupleNum=0;
		//������tupleLength
		for(int i=0;i<attributes.size();i++){
			if(attributes.get(i).attriName.equals(primaryKey))
				attributes.get(i).isUnique=true;
			this.tupleLength+=attributes.get(i).length;
		}
	}
	//��ȡ�ļ��б����Ϣ�Ĺ��췽��
	public table(String tableName, Vector<attribute> attributes, Vector<index> indexes, String primaryKey,int tupleNum) {//initial table
		this.tableName=tableName;
		this.primaryKey=primaryKey;
		this.attributes=attributes;
		this.indexes=indexes;
		this.attriNum=attributes.size();
		this.indexNum=indexes.size();	
		this.tupleNum=tupleNum;
		for(int i=0;i<attributes.size();i++){
			this.tupleLength+=attributes.get(i).length;
		}
	}
	
}
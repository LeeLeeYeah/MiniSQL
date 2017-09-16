package CATALOGMANAGER;

import java.util.Vector;

public class table{
	String tableName;			//表名
	String primaryKey;			//主键名
	Vector<attribute>attributes;//以vector方式存放字段
	Vector<index> indexes;		//以vector方式存放表上的索引	
	int indexNum;				//索引数量
	int attriNum;				//字段数量
	int tupleNum;				//记录条数
	int tupleLength;			//单条记录总字节数
	//创建表格时的构造方法
	public table(String tableName,Vector<attribute> attributes,String primaryKey){
		this.tableName=tableName;
		this.primaryKey=primaryKey;
		this.indexes=new Vector<index>();				
		this.indexNum=0;		
		this.attributes=attributes;
		this.attriNum=attributes.size();
		this.tupleNum=0;
		//计算总tupleLength
		for(int i=0;i<attributes.size();i++){
			if(attributes.get(i).attriName.equals(primaryKey))
				attributes.get(i).isUnique=true;
			this.tupleLength+=attributes.get(i).length;
		}
	}
	//读取文件中表格信息的构造方法
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
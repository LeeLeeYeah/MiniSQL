package CATALOGMANAGER;
/*Catalog Manager负责管理数据库的所有模式信息，包括：
1.	数据库中所有表的定义信息，包括表的名称、表中字段（列）数、主键、定义在该表上的索引。
2.	表中每个字段的定义信息，包括字段类型、是否唯一等。
3.	数据库中所有索引的定义，包括所属表、索引建立在那个字段上等。
Catalog Manager还必需提供访问及操作上述信息的接口，供Interpreter和API模块使用。
*/
import java.io.*;
import java.util.*;
/*实现的功能： 
 *在程序运行之前把catalog信息读入内存 
 *在程序结束之前把catalog信息读入硬盘
 */
public class CatalogManager {
	private static Hashtable<String,table> tables=new Hashtable<String, table>() ;
	private static Hashtable<String,index> indexes=new Hashtable<String, index>();
	private static String tableFilename="table catalog";
	private static String indexFilename="index catalog";
	
	//从文件中读取Catalog信息进内存中
	public static void InitialCatalog() throws IOException {
		InitialTableCatalog();
		InitialIndexCatalog();
	}
	private static void InitialIndexCatalog() throws IOException  {
		// TODO Auto-generated method stub
		File file=new File(indexFilename);
		if(!file.exists()) return;
		FileInputStream fis = new FileInputStream(file);
		DataInputStream dis = new DataInputStream(fis);	
		String tmpIndexName,tmpTableName,tmpAttriName;	
		int tmpIndexBlockNum,tmpRootNum;	
		while(dis.available()>0) {
			tmpIndexName=dis.readUTF();
			tmpTableName=dis.readUTF();
			tmpAttriName=dis.readUTF();
			tmpIndexBlockNum=dis.readInt();
			tmpRootNum = dis.readInt();
			indexes.put(tmpIndexName, new index(tmpIndexName,tmpTableName,tmpAttriName,tmpIndexBlockNum,tmpRootNum));				
		}		
		dis.close();		
			
	}
	private static void InitialTableCatalog() throws IOException {
		// TODO Auto-generated method stub
		File file=new File(tableFilename);
		if(!file.exists()) return;
		FileInputStream fis = new FileInputStream(file);
		DataInputStream dis = new DataInputStream(fis);	
		String tmpTableName,tmpPriKey;	
		int tmpIndexNum,tmpAttriNum,tmpTupleNum;
		
		while(dis.available()>0) {
			Vector<attribute> tmpAttributes=new Vector<attribute>();
			Vector<index> tmpIndexes=new Vector<index> ();
			tmpTableName=dis.readUTF();
			tmpPriKey=dis.readUTF();
			tmpTupleNum=dis.readInt();//dos.writeInt(tmpTable.tupleNum);
			tmpIndexNum=dis.readInt();
			for(int i=0;i<tmpIndexNum;i++){
				String tmpIndexName,tmpAttriName;
				tmpIndexName=dis.readUTF();
				tmpAttriName=dis.readUTF();
				tmpIndexes.addElement(new index(tmpIndexName,tmpTableName,tmpAttriName));
			}
			tmpAttriNum=dis.readInt();
			for(int i=0;i<tmpAttriNum;i++){
				String tmpAttriName,tmpType;
				int tmpLength;boolean tmpIsU;
				tmpAttriName=dis.readUTF();
				tmpType=dis.readUTF();
				tmpLength=dis.readInt();
				tmpIsU=dis.readBoolean();
				tmpAttributes.addElement(new attribute(tmpAttriName,tmpType,tmpLength,tmpIsU));
			}
			tables.put(tmpTableName, new table(tmpTableName,tmpAttributes,tmpIndexes,tmpPriKey,tmpTupleNum));

		}		
		dis.close();
	}
	//将内存中的Catalog信息写入文件
	public static void storeCatalog() throws IOException{
		storeTableCatalog();
		storeIndexCatalog();
	}
	private static void storeIndexCatalog() throws IOException {
		// TODO Auto-generated method stub		
		
		File file=new File(indexFilename);
		if(file.exists())file.delete();
		FileOutputStream fos = new FileOutputStream(file);
		DataOutputStream dos = new DataOutputStream(fos);	
		index tmpIndex;
		Enumeration<index> en = indexes.elements();
		while(en.hasMoreElements()) {
			tmpIndex=en.nextElement();	
			dos.writeUTF(tmpIndex.indexName);
			dos.writeUTF(tmpIndex.tableName);
			dos.writeUTF(tmpIndex.attriName);
			dos.writeInt(tmpIndex.blockNum);
			dos.writeInt(tmpIndex.rootNum);
		}
		//将流中剩下的内容写入
		dos.close();				
	}
	private static void storeTableCatalog() throws IOException {
		// TODO Auto-generated method stub
		File file=new File(tableFilename);
		//if(file.exists())file.d;
		FileOutputStream fos = new FileOutputStream(file);
		DataOutputStream dos = new DataOutputStream(fos);	
		table tmpTable;
		Enumeration<table> en = tables.elements();
        while(en.hasMoreElements()) {
        	tmpTable=en.nextElement();
        	dos.writeUTF(tmpTable.tableName);
        	dos.writeUTF(tmpTable.primaryKey);
        	dos.writeInt(tmpTable.tupleNum);
        	dos.writeInt(tmpTable.indexNum);
        	for(int i=0;i<tmpTable.indexNum;i++){
        		index tmpIndex=tmpTable.indexes.get(i);
        		dos.writeUTF(tmpIndex.indexName);
        		dos.writeUTF(tmpIndex.attriName);
           	}
        	dos.writeInt(tmpTable.attriNum);
        	for(int i=0;i<tmpTable.attriNum;i++){
        		attribute tmpAttri=tmpTable.attributes.get(i);
        		dos.writeUTF(tmpAttri.attriName);
        		dos.writeUTF(tmpAttri.type);
        		dos.writeInt(tmpAttri.length);
        		dos.writeBoolean(tmpAttri.isUnique);
        	}
        }
		dos.close();
	}
	//显示Catalog信息
	public static void showCatalog(){
		showTableCatalog();
		System.out.println();
		showIndexCatalog();
	}
	public static void showIndexCatalog() {
		// TODO Auto-generated method stub
		index tmpIndex;
		Enumeration<index> en = indexes.elements();
		int cnt=1;
		System.out.println("There are "+indexes.size()+" indexes in the database: ");
        System.out.println("\tIndex name\tTable name\tAttribute name:");
		while(en.hasMoreElements()) {
			tmpIndex=en.nextElement();			
			System.out.println(cnt+++"\t"+tmpIndex.indexName+"\t\t"+tmpIndex.tableName+"\t\t"+tmpIndex.attriName);
		}
	}
	public static void showTableCatalog() {
		// TODO Auto-generated method stub
		table tmpTable;
		index tmpIndex;
		attribute tmpAttribute;
		Enumeration<table> en = tables.elements();
		int cnt=1;
		System.out.println("There are "+tables.size()+" tables in the database: ");
        while(en.hasMoreElements()) {
           tmpTable=en.nextElement();
           System.out.println("\nTable "+cnt++);
           System.out.println("Table name: "+tmpTable.tableName);
           System.out.println("Number of Columns: "+tmpTable.attriNum);
           System.out.println("Primary key: "+tmpTable.primaryKey);
           System.out.println("Number of tuples: "+tmpTable.tupleNum);
           System.out.println("Index keys: "+tmpTable.indexNum);
           System.out.println("\tIndex name\tTable name\tAttribute name:");
           for(int i=0;i<tmpTable.indexNum;i++){
        	   tmpIndex=tmpTable.indexes.get(i);
        	   System.out.println("\t"+tmpIndex.indexName+"\t"+tmpIndex.tableName+"\t\t"+tmpIndex.attriName);
           }
           System.out.println("Attributes: "+tmpTable.attriNum);
           System.out.println("\tAttribute name\tType\tlength\tisUnique");
           for(int i=0;i<tmpTable.attriNum;i++){
        	   tmpAttribute=tmpTable.attributes.get(i);
        	   System.out.println("\t"+tmpAttribute.attriName+"\t\t"+tmpAttribute.type+"\t"+tmpAttribute.length+"\t"+tmpAttribute.isUnique);
           }
        }      		
	}
	public static table getTable(String tableName){
		return tables.get(tableName);
	}
	public static index getIndex(String indexName){
		return indexes.get(indexName);
	}	
	public static String getPrimaryKey(String tableName) {
		return getTable(tableName).primaryKey;
	}
	public static int getTupleLength(String tableName){
		return getTable(tableName).tupleLength;
	}
	public static int getTableAttriNum(String tableName){
		return getTable(tableName).attriNum;
	}
	public static int getTupleNum(String tableName){
		return getTable(tableName).tupleNum;
	}
	public static boolean isPrimaryKey(String tableName,String attriName){
		if(isTableExist(tableName)){
			table tmpTable=getTable(tableName);
			if(tmpTable.primaryKey.equals(attriName))return true;
			else return false;
		}
		else{
			System.out.println("The table "+tableName+" doesn't exist");
			return false;
		}
	}
	public static boolean inUniqueKey(String tableName,String attriName){
		if(isTableExist(tableName)){
			table tmpTable=getTable(tableName);
			int i;
			for(i=0;i<tmpTable.attributes.size();i++){
				attribute tmpAttribute=tmpTable.attributes.get(i);
				if(tmpAttribute.attriName.equals(attriName)){
					return tmpAttribute.isUnique;
				}
			}
			if(i>=tmpTable.attributes.size()){
				System.out.println("The attribute "+attriName+" doesn't exist");
				return false;
			}
		}
		System.out.println("The table "+tableName+" doesn't exist");
		return false;
		
	}
	public static boolean isIndexKey(String tableName,String attriName){
		if(isTableExist(tableName)){
			table tmpTable=getTable(tableName);
			if(isAttributeExist(tableName,attriName)){
				for(int i=0;i<tmpTable.indexes.size();i++){
					if(tmpTable.indexes.get(i).attriName.equals(attriName))
						return true;
				}
				//System.out.println(" The attribute "+attriName+" is not an index key");留给interpreter
			}
			else{
				System.out.println("The attribute "+attriName+" doesn't exist");
			}
		}
		else
			System.out.println("The table "+tableName+" doesn't exist");
		return false;	
	}
	public static boolean isTableExist(String tableName){
		return tables.containsKey(tableName);
			}
	public static boolean isIndexExist(String indexName){
		return indexes.containsKey(indexName);
	}
	public static boolean isAttributeExist(String tableName,String attriName){
		table tmpTable=getTable(tableName);
		for(int i=0;i<tmpTable.attributes.size();i++){
			if(tmpTable.attributes.get(i).attriName.equals(attriName))
				return true;
		}
		return false;
	}
	public static String getIndexName(String tableName,String attriName){
		if(isTableExist(tableName)){
			table tmpTable=getTable(tableName);
			if(isAttributeExist(tableName,attriName)){
				for(int i=0;i<tmpTable.indexes.size();i++){
					if(tmpTable.indexes.get(i).attriName.equals(attriName))
						return tmpTable.indexes.get(i).indexName;
				}
			}
			else{
				System.out.println("The attribute "+attriName+" doesn't exist");
			}
		}
		else
			System.out.println("The table "+tableName+" doesn't exist");
		return null;	
	}
	public static String getAttriName(String tableName,int i){//用于insert 针对第i个属性
		return tables.get(tableName).attributes.get(i).attriName;
	}
	public static int getAttriOffest(String tableName,String attriName){
		table tmpTable=tables.get(tableName);
		attribute tmpAttri;
		for(int i=0;i<tmpTable.attributes.size();i++){
			tmpAttri=tmpTable.attributes.get(i);
			if(tmpAttri.attriName.equals(attriName))
				return i;
		}
		System.out.println("Error: The attribute "+attriName+" doesn't exist");
		return -1;
	}
	public static String getType(String tableName,String attriName){//用于where
		table tmpTable=tables.get(tableName);
		attribute tmpAttri;
		for(int i=0;i<tmpTable.attributes.size();i++){
			tmpAttri=tmpTable.attributes.get(i);
			if(tmpAttri.attriName.equals(attriName))
				return tmpAttri.type;
		}
		System.out.println("Error: The attribute "+attriName+" doesn't exist");
		return null;
	}
	public static int getLength(String tableName,String attriName){//用于where
		table tmpTable=tables.get(tableName);
		attribute tmpAttri;
		for(int i=0;i<tmpTable.attributes.size();i++){
			tmpAttri=tmpTable.attributes.get(i);
			if(tmpAttri.attriName.equals(attriName))
				return tmpAttri.length;
		}
		System.out.println("Error: The attribute "+attriName+" doesn't exist");
		return -1;
	}
	public static String getType(String tableName,int i){//用于insert 针对第i个属性
		table tmpTable=tables.get(tableName);
		//System.out.println(tmpTable.attributes.get(i).type+tmpTable.attributes.get(i).attriName);
		return tmpTable.attributes.get(i).type;
	}
	public static int getLength(String tableName,int i){//用于insert 针对第i个属性
		table tmpTable=tables.get(tableName);
		return tmpTable.attributes.get(i).length;
	}


	public static void addTupleNum(String tableName){
		tables.get(tableName).tupleNum++;
	}
	public static void deleteTupleNum(String tableName,int num){
		tables.get(tableName).tupleNum-=num;
	}
	public static boolean updateIndexTable(String indexName,index indexinfo){
		indexes.replace(indexName, indexinfo);
		return true;
	}
	public static boolean isAttributeExist(Vector<attribute> attributes, String attriName) {
		for(int i=0;i<attributes.size();i++){
			if(attributes.get(i).attriName.equals(attriName))
				return true;
		}
		return false;
	} 
	/*
	 * 创建表
	 * @param newTable
	 * @return
	 */
	public static boolean createTable(table newTable){		
		try{
			tables.put(newTable.tableName, newTable);
			//indexes.put(newTable.indexes.firstElement().indexName, newTable.indexes.firstElement());
			return true;		
		}
		catch(NullPointerException e){
			e.printStackTrace();
			return false;
		}
		
	}
	/*
	 * 删除表
	 * @param tableName
	 * @return
	 */
	public static boolean dropTable(String tableName){
		try{
			table tmpTable=tables.get(tableName);
			for(int i=0;i<tmpTable.indexes.size();i++){ //删除该表中对应的索引
				indexes.remove(tmpTable.indexes.get(i).indexName);
			}			
			tables.remove(tableName);
			return true;
		}
		catch(NullPointerException e){
			System.out.println("Error: drop null table. "+e.getMessage());
			return false;
		}
	}
	/*
	 * 创建索引
	 * @param newIndex
	 * @return
	 */
	public static boolean createIndex(index newIndex){
		try{
		table tmpTable=getTable(newIndex.tableName);
		//更新tableCatalog
		tmpTable.indexes.addElement(newIndex);
		tmpTable.indexNum=tmpTable.indexes.size();
		//更新indexCatalog
		indexes.put(newIndex.indexName, newIndex);
		return true;
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	/*
	 * 删除索引
	 * @param indexName
	 * @return
	 */
	public static boolean dropIndex(String indexName){
		//判断index是否已存在
		try{
			index tmpIndex=getIndex(indexName);
			table tmpTable=getTable(tmpIndex.tableName);				
			tmpTable.indexes.remove(tmpIndex) ;
			tmpTable.indexNum=tmpTable.indexes.size();
			indexes.remove(indexName);
			return true;
		}		
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
	}


}



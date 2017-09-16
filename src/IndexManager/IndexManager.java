package IndexManager;


import java.io.*;
import java.util.Vector;
import BUFFERMANAGER.*;
import CATALOGMANAGER.*;
import RECORDMANAGER.*;

public class IndexManager{
	 
//	public static BufferManager  buf;
//		
//	IndexManager(BufferManager buffer){
//		buf=buffer;
//	}
		
	//找出需要插入的索引值
	/*private static byte[] getColumnValue(table  tableinfor,index  indexinfor, byte[] row){
		
		int s_pos = 0, f_pos = 0;	
		for(int i= 0; i <= indexinfor.column; i++){ 
			s_pos = f_pos;
			//f_pos += tableinfor.attributes.get(i).length; //找出记录中第indexinfo。column列的长度为该列属性长度的字符串
			//f_pos+=tableinfor.attrlist[i].length;
			f_pos+=CatalogManager.getLength(indexinfor.tableName, i);
		}
		byte[] colValue=new byte[f_pos-s_pos];
		for(int j=0;j<f_pos-s_pos;j++){//返回该子字符串，即为需要插入的索引字符串
			colValue[j]=row[s_pos+j];
		}
		return colValue;
	}*/
	
	//创建索引
	public static boolean createIndex(index indexInfo){ //需要API提供表和索引信息结构

    	indexInfo.PickInfo();
        	BPlusTree thisTree=new BPlusTree(indexInfo/*,buf*/); //创建一棵新树
        	//开始正式建立索引
        	//这里需要建立一个table的文件比较蛋疼！！！
        	//---------------------------------------------------------------------------------------------------------------
        	//---------------------------------------------------------------------------------------------------------------
        	//---------------------------------------------------------------------------------------------------------------
        	//---------------------------------------------------------------------------------------------------------------
        	String tableName=indexInfo.tableName;
        	try{   	
        		int tinb = Block.BLOCKSIZE
    				/ (4 + CatalogManager.getTupleLength(tableName));
        		int offset=1,count=1,blockOffset=0;
        		String type = CatalogManager.getType(indexInfo.tableName, indexInfo.attriName);
        		byte[] bkey = null;
        		while(count<=CatalogManager.getTupleNum(tableName)){
            		tuple k = RecordManager.getTuple(tableName,offset);
            		if(offset%tinb==0)
            			blockOffset++;
            		if(k==null) continue;
            		count++;
            		String key = k.units.elementAt(CatalogManager.getAttriOffest(tableName, indexInfo.attriName));
            		if (type.equals("int")) {
            			bkey = StringInttoByte(key);
            		} else if (type.equals("float")) {
            			bkey = StringFloattoByte(key);
            		} else if (type.equals("char")) {
            			bkey = key.getBytes();
            		}
            		thisTree.insert(bkey,blockOffset,offset);
            		offset++;
        		}
        		/*for(int blockOffset=0; blockOffset<=CatalogManager.getTupleNum(tableName);blockOffset++){
        			for(int offset=1; offset<=tinb ;offset++){
                		thisTree.insert(key, blockOffset, offset);
        			}
        		}*/
        	}catch(NullPointerException e){
        		//System.err.println("must not be null for key.");
        		return false;
        	}
        	catch(Exception e){
        		//System.err.println("the index has not been created.");
        		return false;
        	}
        	
        	//indexInfo.rootNum=thisTree.myRootBlock.blockOffset;
        	//CatalogManager.setIndexRoot(indexInfo.indexName, thisTree.myRootBlock.blockoffset);
        	
        	//System.out.println("创建索引成功！");
        	return true;
	}
	
	//删除索引，即删除索引文件
	public static boolean dropIndex(String filename ){
		filename+=".index";
		File file = new File(filename);
		
		try{
			if(file.exists())
				if(file.delete())   {
					System.out.println("索引文件已删除");
					return true;}
			else
				//System.out.println("文件"+filename+"没有找到");
				return false;
        }catch(Exception   e){
            System.out.println(e.getMessage());
            //System.out.println("删除索引失败！");
            return false;
        }
			
		//buf.setInvalid(filename);  //将buf中所有与此索引相关的缓冲块都置为无效
		
		//System.out.println("删除索引成功！");
		return true;
	}
	
	//等值查找
	public static Integer searchEqual(index indexInfo, byte[] key) throws Exception{
    	indexInfo.PickInfo();
		offsetInfo off=new offsetInfo();
		try{
			//Index inx=CatalogManager.getIndex(indexInfo.indexName);
			BPlusTree thisTree=new BPlusTree(indexInfo,indexInfo.rootNum); //创建树访问结构（但不是新树）
			off=thisTree.searchKey(key);  //找到位置信息体，返回给API
			if(off==null) return null;
			else{
				return new Integer(off.offsetInfile.elementAt(0));
			}
		}catch(NullPointerException e){
			System.err.println();
			return null;
		}
	}
	
	public static Vector<Integer> searchRange(index indexInfo,String startkey, String endkey) throws Exception{
		String type = CatalogManager.getType(indexInfo.tableName, indexInfo.attriName);
		byte[] skey = null;
		byte[] ekey = null;
		if (type.equals("int")) {
			skey = StringInttoByte(startkey);
			ekey = StringInttoByte(endkey);
		} else if (type.equals("float")) {
			skey = StringFloattoByte(startkey);
			ekey = StringFloattoByte(endkey);
		} else if (type.equals("char")) {
			skey = startkey.getBytes();
			ekey = endkey.getBytes();
		}
    	indexInfo.PickInfo();
		offsetInfo off=new offsetInfo();
		Vector<Integer> res = new Vector<Integer>();
		try{
			//Index inx=CatalogManager.getIndex(indexInfo.indexName);
			BPlusTree thisTree=new BPlusTree(indexInfo,indexInfo.rootNum); //创建树访问结构（但不是新树）
			off=thisTree.searchKey(skey,ekey);  //找到位置信息体，返回给API
			if(off==null) return null;
			else{
				for(int i=0;i<off.length;i++){
					res.add((off.offsetInBlock.elementAt(i)));
				}
				return res;
			}
		}catch(NullPointerException e){
			System.err.println();
			return null;
		}
	}
	
	//插入新索引值，已有索引则更新位置信息
	static public void insertKey(index indexInfo,String key,int blockOffset,int offset) throws Exception{
    	indexInfo.PickInfo();
		String type = CatalogManager.getType(indexInfo.tableName, indexInfo.attriName);
		byte[] bkey = null;
		try{
			if (type.equals("int")) {
    			bkey = StringInttoByte(key);
    		} else if (type.equals("float")) {
    			bkey = StringFloattoByte(key);
    		} else if (type.equals("char")) {
    			bkey = key.getBytes();
    		}
			//Index inx=CatalogManager.getIndex(indexInfo.indexName);
			BPlusTree thisTree=new BPlusTree(indexInfo,indexInfo.rootNum);//创建树访问结构（但不是新树）
			thisTree.insert(bkey, blockOffset, offset);	//插入
			//indexInfo.rootNum=thisTree.myRootBlock.blockOffset;//设置根块
			//CatalogManager.setIndexRoot(indexInfo.indexName, thisTree.myRootBlock.blockoffset);
		}catch(NullPointerException e){
			System.err.println();
		}
		
	}
	
	//删除索引值，没有该索引则什么也不做
	static public void deleteKey(index indexInfo,String deleteKey) throws Exception{
    	indexInfo.PickInfo();
		String type = CatalogManager.getType(indexInfo.tableName, indexInfo.attriName);
		byte[] bkey = null;
		try{
			if (type.equals("int")) {
    			bkey = StringInttoByte(deleteKey);
    		} else if (type.equals("float")) {
    			bkey = StringFloattoByte(deleteKey);
    		} else if (type.equals("char")) {
    			bkey = deleteKey.getBytes();
    		}
			//Index inx=CatalogManager.getIndex(indexInfo.indexName);
			BPlusTree thisTree=new BPlusTree(indexInfo,indexInfo.rootNum);//创建树访问结构（但不是新树）
			thisTree.delete(bkey);	//删除
			//indexInfo.rootNum=thisTree.myRootBlock.blockOffset;//设置根块
			//CatalogManager.setIndexRoot(indexInfo.indexName, thisTree.myRootBlock.blockoffset);
		}catch(NullPointerException e){
			System.err.println();
		}
		
	}

	public static byte[] StringInttoByte(String num) {
		Integer j = new Integer(num);
		int i = j;
		ByteArrayOutputStream boutput = new ByteArrayOutputStream();
		DataOutputStream doutput = new DataOutputStream(boutput);
		byte[] data = new byte[4];
		try {
			doutput.writeInt(i);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] temp = boutput.toByteArray();
		data[0] = temp[0];
		data[1] = temp[1];
		data[2] = temp[2];
		data[3] = temp[3];
		return data;
	}
	
	public static byte[] StringFloattoByte(String num){
		Float j = new Float(num);
		float i = j;
		ByteArrayOutputStream boutput = new ByteArrayOutputStream();
		DataOutputStream doutput = new DataOutputStream(boutput);
		byte[] data = new byte[4];
		try {
			doutput.writeFloat(i);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] temp = boutput.toByteArray();
		data[0] = temp[0];
		data[1] = temp[1];
		data[2] = temp[2];
		data[3] = temp[3];
		return data;
	}
}
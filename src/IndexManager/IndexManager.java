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
		
	//�ҳ���Ҫ���������ֵ
	/*private static byte[] getColumnValue(table  tableinfor,index  indexinfor, byte[] row){
		
		int s_pos = 0, f_pos = 0;	
		for(int i= 0; i <= indexinfor.column; i++){ 
			s_pos = f_pos;
			//f_pos += tableinfor.attributes.get(i).length; //�ҳ���¼�е�indexinfo��column�еĳ���Ϊ�������Գ��ȵ��ַ���
			//f_pos+=tableinfor.attrlist[i].length;
			f_pos+=CatalogManager.getLength(indexinfor.tableName, i);
		}
		byte[] colValue=new byte[f_pos-s_pos];
		for(int j=0;j<f_pos-s_pos;j++){//���ظ����ַ�������Ϊ��Ҫ����������ַ���
			colValue[j]=row[s_pos+j];
		}
		return colValue;
	}*/
	
	//��������
	public static boolean createIndex(index indexInfo){ //��ҪAPI�ṩ���������Ϣ�ṹ

    	indexInfo.PickInfo();
        	BPlusTree thisTree=new BPlusTree(indexInfo/*,buf*/); //����һ������
        	//��ʼ��ʽ��������
        	//������Ҫ����һ��table���ļ��Ƚϵ��ۣ�����
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
        	
        	//System.out.println("���������ɹ���");
        	return true;
	}
	
	//ɾ����������ɾ�������ļ�
	public static boolean dropIndex(String filename ){
		filename+=".index";
		File file = new File(filename);
		
		try{
			if(file.exists())
				if(file.delete())   {
					System.out.println("�����ļ���ɾ��");
					return true;}
			else
				//System.out.println("�ļ�"+filename+"û���ҵ�");
				return false;
        }catch(Exception   e){
            System.out.println(e.getMessage());
            //System.out.println("ɾ������ʧ�ܣ�");
            return false;
        }
			
		//buf.setInvalid(filename);  //��buf���������������صĻ���鶼��Ϊ��Ч
		
		//System.out.println("ɾ�������ɹ���");
		return true;
	}
	
	//��ֵ����
	public static Integer searchEqual(index indexInfo, byte[] key) throws Exception{
    	indexInfo.PickInfo();
		offsetInfo off=new offsetInfo();
		try{
			//Index inx=CatalogManager.getIndex(indexInfo.indexName);
			BPlusTree thisTree=new BPlusTree(indexInfo,indexInfo.rootNum); //���������ʽṹ��������������
			off=thisTree.searchKey(key);  //�ҵ�λ����Ϣ�壬���ظ�API
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
			BPlusTree thisTree=new BPlusTree(indexInfo,indexInfo.rootNum); //���������ʽṹ��������������
			off=thisTree.searchKey(skey,ekey);  //�ҵ�λ����Ϣ�壬���ظ�API
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
	
	//����������ֵ���������������λ����Ϣ
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
			BPlusTree thisTree=new BPlusTree(indexInfo,indexInfo.rootNum);//���������ʽṹ��������������
			thisTree.insert(bkey, blockOffset, offset);	//����
			//indexInfo.rootNum=thisTree.myRootBlock.blockOffset;//���ø���
			//CatalogManager.setIndexRoot(indexInfo.indexName, thisTree.myRootBlock.blockoffset);
		}catch(NullPointerException e){
			System.err.println();
		}
		
	}
	
	//ɾ������ֵ��û�и�������ʲôҲ����
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
			BPlusTree thisTree=new BPlusTree(indexInfo,indexInfo.rootNum);//���������ʽṹ��������������
			thisTree.delete(bkey);	//ɾ��
			//indexInfo.rootNum=thisTree.myRootBlock.blockOffset;//���ø���
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
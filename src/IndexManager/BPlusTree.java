package IndexManager;


import BUFFERMANAGER.*;
import CATALOGMANAGER.*;
import FILEMANAGER.*;

public class BPlusTree {
	private static final int POINTERLENGTH = 4;//��ָ�붨��Ϊ4���ַ���������ڵ����ļ�����4���ֽھͿ��Ա����
	private static final double BLOCKSIZE = 4096.0;
	private int MIN_CHILDREN_FOR_INTERNAL;  //�м�ڵ����С·�����
    private int MAX_CHILDREN_FOR_INTERNAL;  //�м�ڵ�����·�����
    private int MIN_FOR_LEAF;  //Ҷ�ӽڵ����С������
    private int MAX_FOR_LEAF;  //Ҷ�ӽڵ�����������
    
    public String filename;
	public Block myRootBlock;  //����
	public index myindexInfo;  //������Ϣ�壬���ⲿ���룬�ɸ���
	
	//���캯��1
	public BPlusTree(index indexInfo){
		//�½������ļ�
		try{	
			 filename=indexInfo.indexName+".index";
			 FileManager.creatFile(filename);
		}catch(Exception e){
			 //System.err.println("create index failed !");
	    }
		
		//������������С����ֲ���
		int columnLength=indexInfo.columnLength; 
		//4k��С�Ŀ�Ҫһ���ֽڷֱ���Ҷ�ӽڵ㻹���м�ڵ㣬�ĸ��ֽڼ�¼��������Ŀ��4���ֽ�˵�����ڵ�Ŀ��
		//����һ��POINTERLENGTH���ȵ�ָ��ָ����һ���ֵܽڵ�
		//��ÿ������������8���ֽڵ�ָ�루ǰ�ĸ��ֽڱ�ʾ��¼�ڱ��ļ�����һ�飬���ĸ��ֽڱ�ʾ����tuple��ƫ��������myindexInfo.columnLength���ȵļ�ֵ
		MAX_FOR_LEAF=(int)Math.floor((BLOCKSIZE-1/*Ҷ�ӱ��*/-4/*��ֵ��*/-POINTERLENGTH/*���׿��*/-POINTERLENGTH/*��һ��Ҷ�ӿ�Ŀ��*/)/(8+columnLength));
		MIN_FOR_LEAF=(int)Math.ceil(1.0 * MAX_FOR_LEAF/ 2);	
		MAX_CHILDREN_FOR_INTERNAL=MAX_FOR_LEAF; 
		MIN_CHILDREN_FOR_INTERNAL=(int)Math.ceil(1.0 *(MAX_CHILDREN_FOR_INTERNAL)/ 2);
		
		indexInfo.rootNum = 0;
		////CatalogManager.setindexRoot(indexInfo.indexName, 0);  //��������������Catalog�ļ��е�blockƫ����  
		myindexInfo=indexInfo;
		////CatalogManager.addindexBlockNum(indexInfo.indexName);
		myindexInfo.blockNum++;//Ϊ��ʼ�ĸ������µĿռ�

		new LeafNode(myRootBlock=BufferManager.getBlock(filename,0)); //�����������ļ��ĵ�һ�飬����LeafNode���װ��
	}
	
	//���칹�캯��2
	public BPlusTree(index indexInfo,int rootBlockNum){
		int columnLength=indexInfo.columnLength; 
		MAX_FOR_LEAF=(int)Math.floor((BLOCKSIZE-1/*Ҷ�ӱ��*/-4/*��ֵ��*/-POINTERLENGTH/*���׿��*/-POINTERLENGTH/*��һ��Ҷ�ӿ�Ŀ��*/)/(8+columnLength));
		MIN_FOR_LEAF=(int)Math.ceil(1.0 * MAX_FOR_LEAF/ 2);	
		MAX_CHILDREN_FOR_INTERNAL=MAX_FOR_LEAF; 
		MIN_CHILDREN_FOR_INTERNAL=(int)Math.ceil(1.0 *(MAX_CHILDREN_FOR_INTERNAL)/ 2);
		
		myindexInfo=indexInfo;	
		filename = myindexInfo.indexName+".index";
		new LeafNode(myRootBlock=BufferManager.getBlock(filename,rootBlockNum),true); //ע���Ƕ����п�������¿�
	}
	
	//#################����Ϊ��λ�Ĳ���
	public void insert(byte[] originalkey,int blockOffset, int offset){
		if (originalkey == null)    throw new NullPointerException();  

		Node rootNode;
		//���ݿ����Ϣ���Բ�ͬ�����Ͱ�װ��
		if(myRootBlock.readData()[0]=='I'){ 
			rootNode=new InternalNode(myRootBlock,true);
		}
		else{
			rootNode=new LeafNode(myRootBlock,true);
		}
		
		byte[] key=new byte[myindexInfo.columnLength];
		
		int j=0;
		for(;j<originalkey.length;j++){
			key[j]=originalkey[j];
		}
		
	    for(;j<myindexInfo.columnLength;j++){
			key[j]='&';
		}
		
		Block newBlock=rootNode.insert(key, blockOffset, offset); //�ڵ�Ĳ������
    
		if(newBlock!=null){ //�����з��أ�˵�����鱻������
			myRootBlock=newBlock;
		}
		
		myindexInfo.rootNum = myRootBlock.blockoffset;
		////CatalogManager.setindexRoot(myindexInfo.indexName, myRootBlock.blockoffset);
	}
	
	//����Ϊ��λ�ĵ�ֵ����
	public offsetInfo searchKey(byte[] originalkey){
		Node rootNode;
		if(myRootBlock.readData()[0]=='I'){ 
			rootNode=new InternalNode(myRootBlock,true);
		}
		else{
			rootNode=new LeafNode(myRootBlock,true);
		}
		
		byte[] key=new byte[myindexInfo.columnLength];
		
		int j=0;
		for(;j<originalkey.length;j++){
			key[j]=originalkey[j];
		}
		
	    for(;j<myindexInfo.columnLength;j++){
			key[j]='&';
		}
		
		
		return rootNode.searchKey(key); //�Ӹ��ڵ㿪ʼ����
	}
	
	//����Ϊ��λ�ķ�Χ����
	public offsetInfo searchKey(byte[] originalkey,byte[] endkey){
		Node rootNode;
		if(myRootBlock.readData()[0]=='I'){ 
			rootNode=new InternalNode(myRootBlock,true);
		}
		else{
			rootNode=new LeafNode(myRootBlock,true);
		}
		
		byte[] skey=new byte[myindexInfo.columnLength];
		byte[] ekey=new byte[myindexInfo.columnLength];
		
		int j=0;
		for(;j<originalkey.length;j++){
			skey[j]=originalkey[j];
			ekey[j]=endkey[j];
		}
		
	    for(;j<myindexInfo.columnLength;j++){
			skey[j]='&';
			ekey[j]='&';
		}
		
		
		return rootNode.searchKey(skey,ekey); //�Ӹ��ڵ㿪ʼ����
	}
	
	//################����Ϊ��λ��������ɾ��
	public void delete(byte[] originalkey){
		if (originalkey == null)   
			throw new NullPointerException();  
		Node rootNode;
		if(myRootBlock.readData()[0]=='I'){ 
			rootNode=new InternalNode(myRootBlock,true);
		}
		else{
			rootNode=new LeafNode(myRootBlock,true);
		}
	
		byte[] key=new byte[myindexInfo.columnLength];
		
		int j=0;
		for(;j<originalkey.length;j++){
			key[j]=originalkey[j];
		}
		
	    for(;j<myindexInfo.columnLength;j++){
			key[j]='&';
		}
		
		Block newBlock=rootNode.delete(key);
    
		if(newBlock!=null){ //�����з��أ�˵�����鱻������
			myRootBlock=newBlock;
		}

		myindexInfo.rootNum = myRootBlock.blockoffset;
		////CatalogManager.setindexRoot(myindexInfo.indexName, myRootBlock.blockoffset);
	}
	
	//������Node,Ϊ�м�ڵ�InternalNode��Ҷ�ӽڵ�LeafNode����
	abstract class Node {
		Block block;
		
		Node createNode(Block blk){
			block=blk;
			return this;
		}

		abstract Block insert(byte[] inserKey,int blockOffset, int offset);
		abstract Block delete(byte[] deleteKey);
		abstract offsetInfo searchKey(byte[] Key);
		abstract offsetInfo searchKey(byte[] skey, byte[] ekey);
    }
	
	public int compareTo(byte[] buffer1,byte[] buffer2) {
		
		for (int i = 0, j = 0; i < buffer1.length && j < buffer2.length; i++, j++) {
			int a = (buffer1[i] & 0xff);
			int b = (buffer2[j] & 0xff);
			if (a != b) {
				return a - b;
			}
		}
		return buffer1.length - buffer2.length;
	}
	
	//#�м�ڵ���
	class InternalNode extends Node{
		
		InternalNode(Block blk){		
			block=blk; //���м���װ
	    	
	    	block.readData()[0]='I';  //��ʶΪ�м��
			block.writeInt(1,0);//���ڹ���0��keyֵ
			int i=5;
			byte[] a = new byte[9];
	    	for(;i<9;i++)
	    		a[i]='$';  //˵��û�и�����
	    	block.writeData(5,a,9);
		}
		
		InternalNode(Block blk,boolean t){//
			block=blk; //���м���װ
		}
		
		//���м�ڵ�Ϊ��λ�Ĳ���
		Block insert(byte[] insertKey,int blockOffset, int offset){
			int keyNum=block.readInt(1); //��ȡ·����
					
			int i=0;
			for(;i<keyNum;i++){
				int pos=9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH);
				if(compareTo(insertKey, block.getBytes(pos,myindexInfo.columnLength)) < 0) break; //�ҵ��˷�֧λ��
			}
			
			//��ȡ��֧�ӿ�ı��
			int nextBlockNum=block.readInt(9+i*(myindexInfo.columnLength+POINTERLENGTH));
			Block nextBlock=BufferManager.getBlock(filename, nextBlockNum); //������ӿ������
			
			//������ӿ���нڵ��װ
			Node nextNode;
			if(nextBlock.readData()[0]=='I') nextNode=new InternalNode(nextBlock,true);  
			else nextNode=new LeafNode(nextBlock,true);
			
			return nextNode.insert(insertKey, blockOffset, offset); //�����м�ڵ�ĵݹ����
		}
		
		//�����������ʱ��Ҫ���м�ڵ���з�֧��branchKeyΪҪ�²����·�꣬leftChildΪ��·������ӽڵ㣨Ҳ�����Ѿ����ڵĽڵ㣩��rightChildΪ��·������ӽڵ�
		Block branchInsert(byte[] branchKey,Node leftChild,Node rightChild){
			int keyNum = block.readInt(1);//��ȡ·����
			
			if(keyNum==0){ //ȫ�½ڵ�
				keyNum++;
				block.writeInt(1, keyNum);
				block.writeData(9+POINTERLENGTH, branchKey,branchKey.length); 
				block.writeInt(9, leftChild.block.blockoffset);
				block.writeInt(9+POINTERLENGTH+branchKey.length, rightChild.block.blockoffset);
				
				return this.block; //���ýڵ�鷵����Ϊ�¸���
			}
			
			if(++keyNum>MAX_CHILDREN_FOR_INTERNAL){  //���ѽڵ�
				boolean half=false; //��Ϊ���ܴ������ڴ�ռ䣬ֻ���������鷳�ķ����������ӿ����Щ·���ָ��
				//����һ���¿鲢��װ
				int newBlockOffset=myindexInfo.blockNum;
				////CatalogManager.addindexBlockNum(myindexInfo.indexName);
				myindexInfo.blockNum++;
				//FileManager.creatFile(filename);
				Block newBlock=BufferManager.getBlock(filename, newBlockOffset);
				InternalNode newNode=new InternalNode(newBlock);
				
				//����֪���²���·��ʹ���������ޣ�Ҳ��������·��ΪMAX+1����������Ϊԭ���Ŀ鱣��MIN��·�꣬�����¿��Ŀ���MAX+1-MIN��·��
				block.writeInt(1, MIN_CHILDREN_FOR_INTERNAL);
				newBlock.writeInt(1, MAX_CHILDREN_FOR_INTERNAL+1-MIN_CHILDREN_FOR_INTERNAL);
				
				for(int i=0;i<MIN_CHILDREN_FOR_INTERNAL;i++){ //������·�������ԭ���Ŀ�Ҳ������MIN֮��
					
					int pos=9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH);
					if(compareTo(branchKey,block.getBytes(pos,myindexInfo.columnLength))< 0){	//�ҵ���·������λ��	
						System.arraycopy(block.data,  //�ѵ�MIN_CHILDREN_FOR_INTERNA����ʼ�ļ�¼copy����block
								9+(MIN_CHILDREN_FOR_INTERNAL)*(myindexInfo.columnLength+POINTERLENGTH), 
								newBlock.data, 
								9, 
								POINTERLENGTH+(MAX_CHILDREN_FOR_INTERNAL-MIN_CHILDREN_FOR_INTERNAL)*(myindexInfo.columnLength+POINTERLENGTH));	
						System.arraycopy(block.data,  //���²���������λ��
								9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH), 
								block.data, 
								9+POINTERLENGTH+(i+1)*(myindexInfo.columnLength+POINTERLENGTH),
								//ע�⻹Ҫ��ԭ������һ��·�걣����������������Ҫ��Ϊ������²���·�꣩
								(MIN_CHILDREN_FOR_INTERNAL-1-i)*(myindexInfo.columnLength+POINTERLENGTH)+myindexInfo.columnLength);	
			
						//�����²�����Ϣ
						block.writeInternalKey(9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH),branchKey,rightChild.block.blockoffset);				
						
						half=true;
						break;
					}
				}
				if(!half){ //��·��������¿��Ŀ�Ҳ��������λ�ó�����MIN
					System.arraycopy(block.data,  //�ѵ�MIN_CHILDREN_FOR_INTERNA+1����ʼ�ļ�¼copy����block
							9+(MIN_CHILDREN_FOR_INTERNAL+1)*(myindexInfo.columnLength+POINTERLENGTH), 
							newBlock.data, 
							9, 
							POINTERLENGTH+(MAX_CHILDREN_FOR_INTERNAL-MIN_CHILDREN_FOR_INTERNAL-1)*(myindexInfo.columnLength+POINTERLENGTH));
					for(int i=0;i<MAX_CHILDREN_FOR_INTERNAL-MIN_CHILDREN_FOR_INTERNAL-1;i++){
						int pos=9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH);
						
						if(compareTo(branchKey,newBlock.getBytes(pos,myindexInfo.columnLength)) < 0){
							System.arraycopy(newBlock.data,  //���²���������λ��
									9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH),
									newBlock.data, 
									9+POINTERLENGTH+(i+1)*(myindexInfo.columnLength+POINTERLENGTH),
									(MAX_CHILDREN_FOR_INTERNAL-MIN_CHILDREN_FOR_INTERNAL-1-i)*(myindexInfo.columnLength+POINTERLENGTH));								
							
							//�����²�����Ϣ
							newBlock.writeInternalKey(9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH),branchKey,rightChild.block.blockoffset);				
							break;							
						}	
					}
				}
				
				//�ҳ�ԭ�����¿�֮���·�꣬�ṩ�����ڵ���������
				byte[] spiltKey=block.getBytes(9+POINTERLENGTH+(MIN_CHILDREN_FOR_INTERNAL)*(myindexInfo.columnLength+POINTERLENGTH),
						myindexInfo.columnLength);
				
				//�����¿���ӿ�ĸ���
				for(int j=0;j<=newBlock.readInt(1);j++){
					int childBlockNum=newBlock.readInt(9+j*(myindexInfo.columnLength+POINTERLENGTH));
					BufferManager.getBlock(filename, childBlockNum).writeInt(5, newBlockOffset);					
				}	
				
				int parentBlockNum;
				Block ParentBlock;
				InternalNode ParentNode;
				if(block.readData()[5]=='$'){  //û�и��ڵ㣬�򴴽����ڵ�
					//�����¿鲢��װ
					parentBlockNum=myindexInfo.blockNum;
					//FileManager.creatFile(filename);
					ParentBlock=BufferManager.getBlock(filename, parentBlockNum);
					//myindexInfo.blockNum++;
					////CatalogManager.addindexBlockNum(myindexInfo.indexName);
					myindexInfo.blockNum++;
					
					//���ø�����Ϣ
					block.writeInt(5, parentBlockNum);
					newBlock.writeInt(5,parentBlockNum);
					
					ParentNode=new InternalNode(ParentBlock);
				}
				else{
					parentBlockNum=block.readInt(5);				
					newBlock.writeInt(5, parentBlockNum); //�¿�ĸ���Ҳ���Ǿɿ�ĸ���	
					ParentBlock=BufferManager.getBlock(filename, parentBlockNum);	
					ParentNode=new InternalNode(ParentBlock,true);
				}
				
				//�������׿鲢Ϊ���װ��Ȼ���ٵݹ����
						
				return  ParentNode.branchInsert(spiltKey, this, newNode);//((InternalNode)createNode(ParentBlock)).branchInsert(spiltKey, this, newNode);
			}
			
			else{  //����Ҫ���ѽڵ�ʱ
				int i;
				for(i=0;i<keyNum-1;i++){
					int pos=9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH);
					if(compareTo(branchKey,block.getBytes(pos,myindexInfo.columnLength)) < 0){ //�ҵ������λ��
						System.arraycopy(block.data,
										9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH), 
										block.data, 
										9+POINTERLENGTH+(i+1)*(myindexInfo.columnLength+POINTERLENGTH), 
										(keyNum-1-i)*(myindexInfo.columnLength+POINTERLENGTH));
						
						block.writeInternalKey(9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH),branchKey,rightChild.block.blockoffset);									
						block.writeInt(1,keyNum);
						
						return null;
					}					
				}
				if(i==keyNum-1){				
						block.writeInternalKey(9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH),branchKey,rightChild.block.blockoffset);									
						block.writeInt(1,keyNum);
						
						return null;							
				}
			}
						
			return null;
		}
		
		//���м�ڵ�Ϊ��λ�Ĳ���
		offsetInfo searchKey(byte[] key){
			int keyNum=block.readInt(1);
			int i=0;
			for(;i<keyNum;i++){
				int pos=9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH);
				
				if(compareTo(key,block.getBytes(pos,myindexInfo.columnLength)) < 0) break;
			}
			int nextBlockNum=block.readInt(9+i*(myindexInfo.columnLength+POINTERLENGTH));
			Block nextBlock=BufferManager.getBlock(filename, nextBlockNum);
			//���ݿ������а�װ
			Node nextNode;
			if(nextBlock.readData()[0]=='I') nextNode=new InternalNode(nextBlock,true); 
			else nextNode=new LeafNode(nextBlock,true);
			
			return nextNode.searchKey(key); //�ݹ����
		}
		
		//���м�ڵ�Ϊ��λ�ķ�Χ����
		offsetInfo searchKey(byte[] skey,byte[] ekey){
			int keyNum=block.readInt(1);
			int i=0;
			for(;i<keyNum;i++){
				int pos=9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH);
				
				if(compareTo(skey,block.getBytes(pos,myindexInfo.columnLength)) < 0) break;
			}
			int nextBlockNum=block.readInt(9+i*(myindexInfo.columnLength+POINTERLENGTH));
			Block nextBlock=BufferManager.getBlock(filename, nextBlockNum);
			//���ݿ������а�װ
			Node nextNode;
			if(nextBlock.readData()[0]=='I') nextNode=new InternalNode(nextBlock,true); 
			else nextNode=new LeafNode(nextBlock,true);
			
			return nextNode.searchKey(skey,ekey); //�ݹ����
		}

		

		//���м�ڵ�Ϊ��λ��ɾ��
		Block delete(byte[] deleteKey){
			int keyNum=block.readInt(1);
			int i=0;
			for(;i<keyNum;i++){
				int pos=9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH);
				if(compareTo(deleteKey,block.getBytes(pos,myindexInfo.columnLength)) < 0) break;
			}
			int nextBlockNum=block.readInt(9+i*(myindexInfo.columnLength+POINTERLENGTH));
			Block nextBlock=BufferManager.getBlock(filename, nextBlockNum);
			Node nextNode;
			if(nextBlock.readData()[0]=='I') nextNode=new InternalNode(nextBlock,true); 
			else nextNode=new LeafNode(nextBlock,true);
			
			return nextNode.delete(deleteKey); //�ݹ�ɾ��
		}
	
		//ɾ�������в����Ľڵ�ϲ���this���after���Լ�����֮���unionKey
		Block union(byte[] unionKey,Block afterBlock){
			int keyNum = block.readInt(1);
			int afterkeyNum= afterBlock.readInt(1);
			
			//��after���е���Ϣ������this��ĺ��棬ע����һ��λ�ø�unionKey
			System.arraycopy(afterBlock.data,
					9,
					block.data,
					9+(keyNum+1)*(myindexInfo.columnLength+POINTERLENGTH),
					POINTERLENGTH+afterkeyNum*(myindexInfo.columnLength+POINTERLENGTH));
			
			//����unionKey
			block.writeData(9+keyNum*(myindexInfo.columnLength+POINTERLENGTH)+POINTERLENGTH, unionKey,unionKey.length);
			
			//���¼���·���С��ԭ·����+after·����+unionKey��
			keyNum=keyNum+afterkeyNum+1;		
			block.writeInt(1, keyNum);
			
			//�ҵ�����
			int parentBlockNum=block.readInt(5);
			Block parentBlock=BufferManager.getBlock(filename, parentBlockNum); 
			
			//��Block����after��
		//	afterBlock.isvalid=false;
			myindexInfo.blockNum--;
			
			//�ڸ�����ɾ��after��
			return (new InternalNode(parentBlock,true)).delete(afterBlock);
			
		}
		
		//ɾ�������в������ֵܿ��������ţ�this���after���Լ�����֮���internalKey,���ص�changeKey��Ϊ�˸��¸�����������ָ���м�ļ�ֵ
		byte[] rearrangeAfter(Block siblingBlock,byte[] InternalKey){ //�ֵܽڵ������
			int siblingKeyNum=siblingBlock.readInt(1);
			int keyNum = block.readInt(1);
			
			//Ҫת�Ƶ�һ��ָ������
			int blockOffset=siblingBlock.readInt(9);
			//��internalKey���ֵܿ�ĵ�һ��ָ�븴�Ƶ�this���β����·������1
			block.writeInternalKey(9+POINTERLENGTH+keyNum*(myindexInfo.columnLength+POINTERLENGTH), InternalKey, blockOffset);
			keyNum++;
			block.writeInt(1, keyNum);
			
			//�ֵܿ��·������1����ȡ�ֵܿ�ĵ�һ����ֵ��Ϊ���¸���ļ�ֵ���ٽ��ֵܿ�������Ϣ��ǰŲһ��ָ���·��ľ���
			siblingKeyNum--;
			siblingBlock.writeInt(1, siblingKeyNum);
			byte[] changeKey=siblingBlock.getBytes(9+POINTERLENGTH, myindexInfo.columnLength);
			System.arraycopy(siblingBlock.data, 9+POINTERLENGTH+myindexInfo.columnLength, siblingBlock.data, 9, POINTERLENGTH+siblingKeyNum*(POINTERLENGTH+myindexInfo.columnLength));
					
			return changeKey;
			
		}

		byte[] rearrangeBefore(Block siblingBlock,byte[] internalKey){ //�ֵܽڵ�����ǰ
			int siblingKeyNum=siblingBlock.readInt(1);
			int keyNum = block.readInt(1);
			
			siblingKeyNum--;
			siblingBlock.writeInt(1, siblingKeyNum);
			
			//�ֵܿ�����һ��·����Ϊ����ĸ���·�꣬���һ��ָ�뽫ת�Ƹ�this��
			byte[] changeKey=siblingBlock.getBytes(9+POINTERLENGTH+siblingKeyNum*(POINTERLENGTH+myindexInfo.columnLength), myindexInfo.columnLength);		
			int blockOffset=siblingBlock.readInt(9+(siblingKeyNum+1)*(POINTERLENGTH+myindexInfo.columnLength));
			
			//����ָ���·���ó�λ��
			System.arraycopy(block.data, 9, block.data, 9+POINTERLENGTH+myindexInfo.columnLength, POINTERLENGTH+keyNum*(POINTERLENGTH+myindexInfo.columnLength));
			block.writeInt(9, blockOffset); //������ֵܿ�Ų��������ָ��
			block.writeData(9+POINTERLENGTH, internalKey, internalKey.length); //����internalKey
			keyNum++;
			block.writeInt(1, keyNum);
					
			return changeKey;
		}

		//�޸�posBlockNum��ź����·��
		public void exchange(byte[] changeKey,int posBlockNum){
			int keyNum = block.readInt(1);
			
			int i=0;
			for(;i<keyNum;i++){
				int pos=9+i*(myindexInfo.columnLength+POINTERLENGTH);
				int blockNum=block.readInt(pos);
				if(blockNum==posBlockNum) break;
			}
			
			if(i<keyNum) block.writeData(9+i*(myindexInfo.columnLength+POINTERLENGTH)+POINTERLENGTH, changeKey, changeKey.length);
		}
		
		//���м�ڵ���ɾ��һ���ӿ���Ϣ���Լ���ǰ�������·�꣩
		Block	delete(Block blk){
			int keyNum = block.readInt(1);
			
			for(int i=0;i<=keyNum;i++){
				int pos=9+i*(myindexInfo.columnLength+POINTERLENGTH);
				int ptr=block.readInt(pos);
				if(ptr==blk.blockoffset){ //����ҵ����ӿ���
					System.arraycopy(block.data, //��������ź�ǰ���·�궼�Ƴ�
							9+POINTERLENGTH+(i-1)*(myindexInfo.columnLength+POINTERLENGTH), 
							block.data, 
							9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH), 
							(keyNum-i)*(myindexInfo.columnLength+POINTERLENGTH));
					keyNum--;
					block.writeInt(1, keyNum);
			
					if(keyNum >=MIN_CHILDREN_FOR_INTERNAL) return null; //? //�Ƴ���ֱ�ӽ���
			
					if(block.readData()[5]=='$'){  //������'R'�ȽϺ�? //û�и��ڵ�ʱ
						
						if(keyNum==0){	//û��·�ֻ꣬��һ���ӿ���ʱ���������ӿ���Ϊ���飬��this��ɾ��
							//block.isvalid=false;
							myindexInfo.blockNum--;
							return BufferManager.getBlock(filename, block.readInt(9));
						}
							
						return null;
					}
			
					//�ҵ����׿�
					int parentBlockNum=block.readInt(5);
					Block parentBlock=BufferManager.getBlock(filename, parentBlockNum);
					int parentKeyNum=parentBlock.readInt(1);
					
					int sibling;
					Block siblingBlock;
					int j=0;
					//���Һ����ֵܿ�
					for(;j<parentKeyNum;j++){
						int ppos=9+j*(myindexInfo.columnLength+POINTERLENGTH);
						if(block.blockoffset==parentBlock.readInt(ppos)){ 
							//���������ֵܿ�
							sibling=parentBlock.readInt(ppos+POINTERLENGTH+myindexInfo.columnLength);
							siblingBlock=BufferManager.getBlock(filename, sibling);
								
							byte[] unionKey=parentBlock.getBytes(ppos+POINTERLENGTH, myindexInfo.columnLength);
							
							//�ܹ��ϲ�
							if((siblingBlock.readInt(1)+keyNum)<=MAX_CHILDREN_FOR_INTERNAL){				
								return this.union(unionKey,siblingBlock);
							}
							
							//�����ڵ�ԭ������MIN_FOR_LEAF��deleteһ���Ժ�����û���ǣ����ʱ����漰�����ڵ�
							//�������û�д���Ҫ����Ļ����漰��4���ֵܿ飬��Ϊthis����MIN-1��·�꣬�ֵܿ���MIN��·�꣬���ֵܿ�Ųһ��·����Ȼ�ǰ׷�������
							//��������ȴҲ��һ���ܹ��ϲ������Ժ��鷳
							if(siblingBlock.readInt(1)==MIN_CHILDREN_FOR_INTERNAL) return null;
							
							//���ܺϲ���ͨ�����ֵܿ�ת��һ��·��������B+��·����С����
							(new InternalNode(parentBlock,true)).exchange(rearrangeAfter(siblingBlock,unionKey),block.blockoffset);//blockOffset��bufferManager�����ƺ�
							return null;
					
						}				
					}
					
					//�Ҳ������飬ֻ����ǰ���ֵܿ�
					sibling=parentBlock.readInt(9+(parentKeyNum-1)*(myindexInfo.columnLength+POINTERLENGTH));
					siblingBlock=BufferManager.getBlock(filename, sibling);		
								
					byte[] unionKey=parentBlock.getBytes(9+(parentKeyNum-1)*(myindexInfo.columnLength+POINTERLENGTH)+POINTERLENGTH, myindexInfo.columnLength);
					
					//�ܹ��ϲ�
					if((siblingBlock.readInt(1)+keyNum)<=MAX_CHILDREN_FOR_INTERNAL){		
						return (new InternalNode(siblingBlock,true)).union(unionKey,block);
					}
						
					//û�п��ǵ����
					if(siblingBlock.readInt(1)==MIN_CHILDREN_FOR_INTERNAL) return null;
					
					//���ŵ����
					(new InternalNode(parentBlock,true)).exchange(rearrangeBefore(siblingBlock,unionKey),sibling);
					return null;
				}
	
			}		
			return null;
		}
				
	}
	//Ҷ�ӽڵ���
	class LeafNode extends Node{
				
		LeafNode(Block blk){
			block=blk;
			
	    	block.data[0]='L';  //��ʶΪҶ�ӿ�
	    	int i=5;
			block.writeInt(1, 0);//���ڹ���0��keyֵ//���ڹ���0��keyֵ
	    	for(;i<9;i++)
	    		block.data[i]='$';  //û�и���
	    	for(;i<13;i++)
	    		block.data[i]='&';  //���һ��Ҷ�ӿ����һ��Ҷ�ӿ��ţ��൱��null
	    	block.writeData();
		}
		
		LeafNode(Block blk,boolean t){
			block=blk;	
		}

		//��Ҷ�ӽڵ�Ϊ��λ�������Ĳ���
		Block insert(byte[] insertKey,int blockOffset, int offset){//��ֵ ��� ����ƫ��
			int keyNum = block.readInt(1);
			
			if(++keyNum>MAX_FOR_LEAF){  //���ѽڵ�
				boolean half=false;
				//FileManager.creatFile(filename);
				Block newBlock=BufferManager.getBlock(filename, myindexInfo.blockNum);
				////CatalogManager.addindexBlockNum(myindexInfo.indexName);
				myindexInfo.blockNum++;
				LeafNode newNode=new LeafNode(newBlock);
				
				for(int i=0;i<MIN_FOR_LEAF-1;i++){ //����ԭ��
					int pos=17+i*(myindexInfo.columnLength+8);
					if(compareTo( insertKey,block.getBytes(pos,myindexInfo.columnLength))< 0){					
						System.arraycopy(block.data,  //�ѵ�MIN_FOR_LEAF-1����ʼ�ļ�¼copy����block
								9+(MIN_FOR_LEAF-1)*(myindexInfo.columnLength+8), 
								newBlock.data, 
								9, 
								POINTERLENGTH+(MAX_FOR_LEAF-MIN_FOR_LEAF+1)*(myindexInfo.columnLength+8));	
						System.arraycopy(block.data,  //���²���������λ��
								9+i*(myindexInfo.columnLength+8), 
								block.data, 
								9+(i+1)*(myindexInfo.columnLength+8),
								POINTERLENGTH+(MIN_FOR_LEAF-1-i)*(myindexInfo.columnLength+8));	
						
						//����������
						block.setKeydata(9+i*(myindexInfo.columnLength+8),insertKey,blockOffset,offset);				
											
						half=true;
						break;
					}
				}				
				if(!half){ //�����¿�
					System.arraycopy(block.data,  //�ѵ�MIN_FOR_LEAF����ʼ�ļ�¼copy����block
							9+(MIN_FOR_LEAF)*(myindexInfo.columnLength+8), 
							newBlock.data, 
							9, 
							POINTERLENGTH+(MAX_FOR_LEAF-MIN_FOR_LEAF)*(myindexInfo.columnLength+8));
					int i=0;
					for(;i<MAX_FOR_LEAF-MIN_FOR_LEAF;i++){
						int pos=17+i*(myindexInfo.columnLength+8);
						if(compareTo(insertKey,newBlock.getBytes(pos,myindexInfo.columnLength)) < 0){
							System.arraycopy(newBlock.data,  //���²���������λ��
									9+i*(myindexInfo.columnLength+8), 
									newBlock.data, 
									9+(i+1)*(myindexInfo.columnLength+8), 
									POINTERLENGTH+(MAX_FOR_LEAF-MIN_FOR_LEAF-i)*(myindexInfo.columnLength+8));								
							
							//����������
							newBlock.setKeydata(9+i*(myindexInfo.columnLength+8),insertKey,blockOffset,offset);
							break;
						}	
					}
					if(i==MAX_FOR_LEAF-MIN_FOR_LEAF){
						System.arraycopy(newBlock.data,  //���²���������λ��
								9+i*(myindexInfo.columnLength+8), 
								newBlock.data, 
								9+(i+1)*(myindexInfo.columnLength+8), 
								POINTERLENGTH+(MAX_FOR_LEAF-MIN_FOR_LEAF-i)*(myindexInfo.columnLength+8));								
						
						//����������
						newBlock.setKeydata(9+i*(myindexInfo.columnLength+8),insertKey,blockOffset,offset);
					}
				}
				
				block.writeInt(1,MIN_FOR_LEAF);
			    newBlock.writeInt(1,MAX_FOR_LEAF+1-MIN_FOR_LEAF);
			    
			    //ԭ����������¿�Ŀ�ţ�����������Ҷ�ӽڵ��һ������
			    block.writeInt(9+MIN_FOR_LEAF*(myindexInfo.columnLength+8), newBlock.blockoffset);
				
			    int parentBlockNum;
			    Block ParentBlock;
			    InternalNode ParentNode;
				if(block.readData()[5]=='$'){  //û�и��ڵ㣬�򴴽����ڵ�
					parentBlockNum=myindexInfo.blockNum;
					ParentBlock=BufferManager.getBlock(filename, parentBlockNum);
				
					////CatalogManager.addindexBlockNum(myindexInfo.indexName);
					myindexInfo.blockNum++;
		
					block.writeInt(5, parentBlockNum);
					newBlock.writeInt(5, parentBlockNum );
					ParentNode=new InternalNode(ParentBlock);
				}
				else{
					parentBlockNum=block.readInt(5);				
					newBlock.writeInt(5, parentBlockNum); //�½ڵ�ĸ���Ҳ���Ǿɽڵ�ĸ���
					ParentBlock=BufferManager.getBlock(filename, parentBlockNum);
					ParentNode=new InternalNode(ParentBlock,true);
				}
			
				//��branchKey(�¿�ĵ�һ��������ֵ)�ύ���ø�����ѳ�����������
				byte[] branchKey=newBlock.getBytes(17, myindexInfo.columnLength);
				
				return  ParentNode.branchInsert(branchKey, this, newNode);
			}
			
			else{  //����Ҫ���ѽڵ�ʱ
				if(keyNum-1==0){
					System.arraycopy(block.data,
							9, 
							block.data, 
							9+(myindexInfo.columnLength+8), 
							POINTERLENGTH);
			
					block.setKeydata(9,insertKey,blockOffset,offset);						
					block.writeInt(1, keyNum);
			
					return null;
				}
				int i; 
				for(i=0;i<keyNum;i++){
					int pos=17+i*(myindexInfo.columnLength+8);
					
					if(compareTo(insertKey,block.getBytes(pos,myindexInfo.columnLength))==0){ //���м�ֵ�����
						block.setKeydata(9+i*(myindexInfo.columnLength+8),insertKey,blockOffset,offset);
						return null;
					}
					
					if(compareTo(insertKey,block.getBytes(pos,myindexInfo.columnLength)) < 0){ //�ҵ������λ��
						System.arraycopy(block.data,
										9+i*(myindexInfo.columnLength+8), 
										block.data, 
										9+(i+1)*(myindexInfo.columnLength+8), 
										POINTERLENGTH+(keyNum-1-i)*(myindexInfo.columnLength+8));
						
						block.setKeydata(9+i*(myindexInfo.columnLength+8),insertKey,blockOffset,offset);						
						block.writeInt(1, keyNum);
						
						return null;
					}					
				}
				if(i==keyNum){
					System.arraycopy(block.data,
							9+(i-1)*(myindexInfo.columnLength+8), 
							block.data, 
							9+i*(myindexInfo.columnLength+8), 
							POINTERLENGTH);
			
					block.setKeydata(9+(i-1)*(myindexInfo.columnLength+8),insertKey,blockOffset,offset);						
					block.writeInt(1, keyNum);
			
					return null;
				}
			}
		    return null;		
		}
		
		//��Ҷ�ӽڵ�Ϊ��λ�Ĳ�������
		offsetInfo searchKey(byte[] originalkey){
			int keyNum=block.readInt(1); 
			if(keyNum==0) return null; //�տ��򷵻�null
		
			byte[] key=new byte[myindexInfo.columnLength];
			
			int i=0;
			for(;i<originalkey.length;i++){
				key[i]=originalkey[i];
			}
			
		    for(;i<myindexInfo.columnLength;i++){
				key[i]='&';
			}
			
			//���ֲ���
			int start=0;
			int end=keyNum-1;
			int middle=0;

			while (start <= end) {  

				middle = (start + end) / 2;
								
                byte[] middleKey = block.getBytes(17+middle*(myindexInfo.columnLength+8), myindexInfo.columnLength);  
                if (compareTo(key,middleKey) == 0){  
                    break;  
                }  
                  
                if (compareTo(key,middleKey) < 0) {  
                    end = middle-1;  
                } else {  
                    start = middle+1;  
                }  
                
            }  
              			
			int pos=9+middle*(myindexInfo.columnLength+8);
			byte[] middleKey = block.getBytes(8+pos, myindexInfo.columnLength); 
			
			//���ҵ���λ���ϵı��ļ�ƫ����Ϣ����off�У����ظ��ϼ�����
            offsetInfo off=new offsetInfo();
            
            off.offsetInfile.add(block.readInt(pos));
            off.offsetInBlock.add(block.readInt(pos+4));
            off.length=1;
					
            return compareTo(middleKey,key) == 0 ? off : null;   //�ٴ�ȷ����û���ҵ����������ֵ��û���򷵻�null
		}
		
		//��Ҷ�ӽڵ�Ϊ��λ�ķ�Χ��������
		offsetInfo searchKey(byte[] originalkey, byte[] endkey){
			int keyNum=block.readInt(1); 
			if(keyNum==0) return null; //�տ��򷵻�null
		
			byte[] key=new byte[myindexInfo.columnLength];
			byte[] ekey=new byte[myindexInfo.columnLength];
			
			int i=0;
			for(;i<originalkey.length;i++){
				key[i]=originalkey[i];
				ekey[i]=endkey[i];
			}
			
		    for(;i<myindexInfo.columnLength;i++){
				key[i]='&';
				ekey[i]='&';
			}
			
			//���ֲ���
			int start=0;
			int end=keyNum-1;
			int middle=0;

			while (start <= end) {  

				middle = (start + end) / 2;
								
                byte[] middleKey = block.getBytes(17+middle*(myindexInfo.columnLength+8), myindexInfo.columnLength);  
                if (compareTo(key,middleKey) == 0){  
                    break;  
                }  
                  
                if (compareTo(key,middleKey) < 0) {  
                    end = middle-1;  
                } else {  
                    start = middle+1;  
                }  
                
            }  
              			
			int pos=9+middle*(myindexInfo.columnLength+8);
			byte[] middleKey = block.getBytes(8+pos, myindexInfo.columnLength); 
					
            if(compareTo(middleKey,key) != 0) return null;   //�ٴ�ȷ����û���ҵ����������ֵ��û���򷵻�null
            else{
    			//���ҵ���λ���ϵı��ļ�ƫ����Ϣ����off�У����ظ��ϼ�����
                offsetInfo off=new offsetInfo();
                while(compareTo(middleKey,ekey)<=0){
                    off.offsetInfile.add(block.readInt(pos));
                    off.offsetInBlock.add(block.readInt(pos+4));
                    middle+=1;
                    off.length++;
                    if(middle>=keyNum){
                    	//��ȡβָ�벢��ȡ�µĿ����
                    	if(block.readString(9+keyNum*(8+myindexInfo.columnLength)).equals("&&&&")){
                    		break;
                    	}
                    	block = BufferManager.getBlock(filename,
                    			block.readInt(9+keyNum*(8+myindexInfo.columnLength)));
                    	keyNum=block.readInt(1); 
                    	middle=0;
                    	pos=9;
                    	middleKey = block.getBytes(8+pos, myindexInfo.columnLength);
                    }
                    else{
                    	pos=9+middle*(myindexInfo.columnLength+8);
                    	middleKey = block.getBytes(8+pos, myindexInfo.columnLength); 
                    }
                }
                //off.length = off.offsetInBlock.capacity();
                return off;
            }
		}

		

		//��Ҷ�ӽڵ�Ϊ��λ�Ŀ�ϲ�
		Block union(Block afterBlock){
			int keyNum = block.readInt(1);
			int afterkeyNum= afterBlock.readInt(1);
			
			//��after������ݸ��Ƶ�this��ĺ���
			System.arraycopy(afterBlock.data,9,block.data,9+keyNum*(myindexInfo.columnLength+8),POINTERLENGTH+afterkeyNum*(myindexInfo.columnLength+8));
			
			//������������
			keyNum+=afterkeyNum;		
			block.writeInt(1, keyNum);
						
			//��bufferManager����after��
			//afterBlock.isvalid=false;
			myindexInfo.blockNum--;
			
			//�ڸ��ڵ���ɾ�������������after�����Ϣ(��ż���ǰ���·��)
			int parentBlockNum=block.readInt(5);
			Block parentBlock=BufferManager.getBlock(filename, parentBlockNum); 
			
			return (new InternalNode(parentBlock,true)).delete(afterBlock);
			
		}
		
		//���ֵܽڵ�Ų��һ��������������
		byte[] rearrangeAfter(Block siblingBlock){ //�ֵܽڵ������
			int siblingKeyNum=siblingBlock.readInt(1);
			int keyNum = block.readInt(1);
			
			//ҪŲ������������Ϣ
			int blockOffset=siblingBlock.readInt(9);
			int offset=siblingBlock.readInt(13);
			byte[] Key=siblingBlock.getBytes(17, myindexInfo.columnLength);
			
			//�����ֵܿ������
			siblingKeyNum--;
			siblingBlock.writeInt(1, siblingKeyNum);
			System.arraycopy(siblingBlock.data, 9+8+myindexInfo.columnLength, siblingBlock.data, 9, POINTERLENGTH+siblingKeyNum*(8+myindexInfo.columnLength));
			
			//this����ֵܿ�֮�����·��
			byte[] changeKey=siblingBlock.getBytes(17, myindexInfo.columnLength);
			
			//���Ų��������
			block.setKeydata(9+keyNum*(myindexInfo.columnLength+8), Key, blockOffset, offset);
			keyNum++;
			block.writeInt(1, keyNum);
			//ע�ⲻҪ©��������Ϣ(��һ��ı��)
			block.writeInt(9+keyNum*(myindexInfo.columnLength+8), siblingBlock.blockoffset);
			
			return changeKey;
			
		}
		
		byte[] rearrangeBefore(Block siblingBlock){  //�ֵܽڵ�����ǰ
			int siblingKeyNum=siblingBlock.readInt(1);
			int keyNum = block.readInt(1);
			
			siblingKeyNum--;
			siblingBlock.writeInt(1, siblingKeyNum);
			
			//ҪŲ����������Ϣ
			int blockOffset=siblingBlock.readInt(9+siblingKeyNum*(myindexInfo.columnLength+8));
			int offset=siblingBlock.readInt(13+siblingKeyNum*(myindexInfo.columnLength+8));
			byte[] Key=siblingBlock.getBytes(17+siblingKeyNum*(myindexInfo.columnLength+8), myindexInfo.columnLength);
			
			//�����������Ϣ
			siblingBlock.writeInt(9+siblingKeyNum*(myindexInfo.columnLength+8), block.blockoffset);
			
			//Ų��������λ��
			System.arraycopy(block.data, 9, block.data, 9+8+myindexInfo.columnLength, POINTERLENGTH+keyNum*(8+myindexInfo.columnLength));
			block.setKeydata(9, Key, blockOffset, offset); //����������
			keyNum++;
			block.writeInt(1, keyNum);
			
			//��Ҫ����ĸ��³ɵ���·��
			byte[] changeKey=block.getBytes(17, myindexInfo.columnLength);
			
			return changeKey;
		}
		
		//��Ҷ�ӽڵ�Ϊ��λ������ɾ��
		Block delete(byte[] deleteKey){
			
			int keyNum = block.readInt(1);
			
			for(int i=0;i<keyNum;i++){
				int pos=17+i*(myindexInfo.columnLength+8);
				
				if(compareTo(deleteKey,block.getBytes(pos,myindexInfo.columnLength))<0){ //û�ҵ�������ֵ
					//System.out.println("û�и�������ֵ");
					return null;
				}
				
				if(compareTo(deleteKey,block.getBytes(pos,myindexInfo.columnLength)) == 0){ //�ҵ���Ӧ�ļ�ֵ					
					
					System.arraycopy(block.data, //�Ƴ���������
									9+(i+1)*(myindexInfo.columnLength+8), 
									block.data, 
									9+i*(myindexInfo.columnLength+8), 
									POINTERLENGTH+(keyNum-1-i)*(myindexInfo.columnLength+8));
					keyNum--;
					block.writeInt(1, keyNum);
					
					if(keyNum >=MIN_FOR_LEAF) return null; //��Ȼ��������Ҫ��
					
					if(block.readData()[5]=='$') return null;  //û�и��飬����Ϊ��
					
					boolean lastFlag=false;
					if(block.readData()[9+keyNum*(myindexInfo.columnLength+8)]=='&') lastFlag=true; //Ҷ�ӿ���������һ��
					
					int sibling=block.readInt(9+keyNum*(myindexInfo.columnLength+8)); 
					Block siblingBlock=BufferManager.getBlock(filename, sibling);
					int parentBlockNum=block.readInt(5);
					
					if(lastFlag || siblingBlock==null || siblingBlock.readInt(5)!=parentBlockNum/*��Ȼ�к����鵫����ͬһ�����׵��ֵܿ�*/){  //û���ҵ������ֵܽڵ�
						//ͨ��������ǰ���ֵܿ�
						Block parentBlock=BufferManager.getBlock(filename, parentBlockNum);
						int j=0;
						int parentKeyNum=parentBlock.readInt(1);
						for(;j<parentKeyNum;j++){
							int ppos=9+POINTERLENGTH+j*(myindexInfo.columnLength+POINTERLENGTH);
							if(compareTo(deleteKey,parentBlock.getBytes(ppos, myindexInfo.columnLength))<0){
								sibling=parentBlock.readInt(ppos-2*POINTERLENGTH-myindexInfo.columnLength);
								siblingBlock=BufferManager.getBlock(filename, sibling);
								break;
							}
						}
						
						//���Ժϲ�
						if((siblingBlock.readInt(1)+keyNum)<=MAX_FOR_LEAF){
							return (new LeafNode(siblingBlock,true)).union(block);
						}
									
						//�����ڵ�ԭ������MIN_FOR_LEAF��deleteһ���Ժ�����û���ǣ����ʱ����漰�����ڵ�
						if(siblingBlock.readInt(1)==MIN_FOR_LEAF) return null;
							
						//����
						(new InternalNode(parentBlock,true)).exchange(rearrangeBefore(siblingBlock),sibling);
						return null;
					}
			
					//�ϲ�
					if((siblingBlock.readInt(1)+keyNum)<=MAX_FOR_LEAF){
						return this.union(siblingBlock);
					}
					
					//û���ǵ����
					if(siblingBlock.readInt(1)==MIN_FOR_LEAF) return null;
					
					//����
					Block parentBlock=BufferManager.getBlock(filename, parentBlockNum);
					(new InternalNode(parentBlock,true)).exchange(rearrangeAfter(siblingBlock),block.blockoffset);//blockOffset��bufferManager�����ƺ�
					return null;
				}
			}
			
			return null;
		}
	}
}

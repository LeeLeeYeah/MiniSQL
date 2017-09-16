package IndexManager;


import BUFFERMANAGER.*;
import CATALOGMANAGER.*;
import FILEMANAGER.*;

public class BPlusTree {
	private static final int POINTERLENGTH = 4;//设指针定长为4个字符，即假设节点在文件中用4个字节就可以标记了
	private static final double BLOCKSIZE = 4096.0;
	private int MIN_CHILDREN_FOR_INTERNAL;  //中间节点的最小路标个数
    private int MAX_CHILDREN_FOR_INTERNAL;  //中间节点的最大路标个数
    private int MIN_FOR_LEAF;  //叶子节点的最小索引数
    private int MAX_FOR_LEAF;  //叶子节点的最大索引数
    
    public String filename;
	public Block myRootBlock;  //根块
	public index myindexInfo;  //索引信息体，由外部传入，可更新
	
	//构造函数1
	public BPlusTree(index indexInfo){
		//新建索引文件
		try{	
			 filename=indexInfo.indexName+".index";
			 FileManager.creatFile(filename);
		}catch(Exception e){
			 //System.err.println("create index failed !");
	    }
		
		//根据索引键大小计算分叉数
		int columnLength=indexInfo.columnLength; 
		//4k大小的块要一个字节分辨是叶子节点还是中间节点，四个字节记录索引键数目，4个字节说明父节点的块号
		//还有一个POINTERLENGTH长度的指针指向下一个兄弟节点
		//而每个索引键包括8个字节的指针（前四个字节表示记录在表文件的那一块，后四个字节表示整个tuple的偏移量）和myindexInfo.columnLength长度的键值
		MAX_FOR_LEAF=(int)Math.floor((BLOCKSIZE-1/*叶子标记*/-4/*键值数*/-POINTERLENGTH/*父亲块号*/-POINTERLENGTH/*下一块叶子块的块号*/)/(8+columnLength));
		MIN_FOR_LEAF=(int)Math.ceil(1.0 * MAX_FOR_LEAF/ 2);	
		MAX_CHILDREN_FOR_INTERNAL=MAX_FOR_LEAF; 
		MIN_CHILDREN_FOR_INTERNAL=(int)Math.ceil(1.0 *(MAX_CHILDREN_FOR_INTERNAL)/ 2);
		
		indexInfo.rootNum = 0;
		////CatalogManager.setindexRoot(indexInfo.indexName, 0);  //设置索引根块在Catalog文件中的block偏移量  
		myindexInfo=indexInfo;
		////CatalogManager.addindexBlockNum(indexInfo.indexName);
		myindexInfo.blockNum++;//为初始的根申请新的空间

		new LeafNode(myRootBlock=BufferManager.getBlock(filename,0)); //创建该索引文件的第一块，并用LeafNode类包装它
	}
	
	//构造构造函数2
	public BPlusTree(index indexInfo,int rootBlockNum){
		int columnLength=indexInfo.columnLength; 
		MAX_FOR_LEAF=(int)Math.floor((BLOCKSIZE-1/*叶子标记*/-4/*键值数*/-POINTERLENGTH/*父亲块号*/-POINTERLENGTH/*下一块叶子块的块号*/)/(8+columnLength));
		MIN_FOR_LEAF=(int)Math.ceil(1.0 * MAX_FOR_LEAF/ 2);	
		MAX_CHILDREN_FOR_INTERNAL=MAX_FOR_LEAF; 
		MIN_CHILDREN_FOR_INTERNAL=(int)Math.ceil(1.0 *(MAX_CHILDREN_FOR_INTERNAL)/ 2);
		
		myindexInfo=indexInfo;	
		filename = myindexInfo.indexName+".index";
		new LeafNode(myRootBlock=BufferManager.getBlock(filename,rootBlockNum),true); //注意是读已有块而创建新块
	}
	
	//#################以树为单位的插入
	public void insert(byte[] originalkey,int blockOffset, int offset){
		if (originalkey == null)    throw new NullPointerException();  

		Node rootNode;
		//根据块的信息，以不同的类型包装块
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
		
		Block newBlock=rootNode.insert(key, blockOffset, offset); //节点的插入操作
    
		if(newBlock!=null){ //假如有返回，说明根块被更新了
			myRootBlock=newBlock;
		}
		
		myindexInfo.rootNum = myRootBlock.blockoffset;
		////CatalogManager.setindexRoot(myindexInfo.indexName, myRootBlock.blockoffset);
	}
	
	//以树为单位的等值查找
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
		
		
		return rootNode.searchKey(key); //从根节点开始查找
	}
	
	//以树为单位的范围查找
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
		
		
		return rootNode.searchKey(skey,ekey); //从根节点开始查找
	}
	
	//################以树为单位的索引键删除
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
    
		if(newBlock!=null){ //假如有返回，说明根块被更新了
			myRootBlock=newBlock;
		}

		myindexInfo.rootNum = myRootBlock.blockoffset;
		////CatalogManager.setindexRoot(myindexInfo.indexName, myRootBlock.blockoffset);
	}
	
	//抽象类Node,为中间节点InternalNode和叶子节点LeafNode服务
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
	
	//#中间节点类
	class InternalNode extends Node{
		
		InternalNode(Block blk){		
			block=blk; //将中间块包装
	    	
	    	block.readData()[0]='I';  //标识为中间块
			block.writeInt(1,0);//现在共有0个key值
			int i=5;
			byte[] a = new byte[9];
	    	for(;i<9;i++)
	    		a[i]='$';  //说明没有父块标号
	    	block.writeData(5,a,9);
		}
		
		InternalNode(Block blk,boolean t){//
			block=blk; //将中间块包装
		}
		
		//以中间节点为单位的插入
		Block insert(byte[] insertKey,int blockOffset, int offset){
			int keyNum=block.readInt(1); //获取路标数
					
			int i=0;
			for(;i<keyNum;i++){
				int pos=9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH);
				if(compareTo(insertKey, block.getBytes(pos,myindexInfo.columnLength)) < 0) break; //找到了分支位置
			}
			
			//获取分支子块的标号
			int nextBlockNum=block.readInt(9+i*(myindexInfo.columnLength+POINTERLENGTH));
			Block nextBlock=BufferManager.getBlock(filename, nextBlockNum); //将这个子块读进来
			
			//将这个子块进行节点包装
			Node nextNode;
			if(nextBlock.readData()[0]=='I') nextNode=new InternalNode(nextBlock,true);  
			else nextNode=new LeafNode(nextBlock,true);
			
			return nextNode.insert(insertKey, blockOffset, offset); //进入中间节点的递归查找
		}
		
		//插入过程中有时需要对中间节点进行分支，branchKey为要新插入的路标，leftChild为新路标的左子节点（也就是已经存在的节点），rightChild为新路标的右子节点
		Block branchInsert(byte[] branchKey,Node leftChild,Node rightChild){
			int keyNum = block.readInt(1);//获取路标数
			
			if(keyNum==0){ //全新节点
				keyNum++;
				block.writeInt(1, keyNum);
				block.writeData(9+POINTERLENGTH, branchKey,branchKey.length); 
				block.writeInt(9, leftChild.block.blockoffset);
				block.writeInt(9+POINTERLENGTH+branchKey.length, rightChild.block.blockoffset);
				
				return this.block; //将该节点块返回作为新根块
			}
			
			if(++keyNum>MAX_CHILDREN_FOR_INTERNAL){  //分裂节点
				boolean half=false; //因为不能创建新内存空间，只能用这种麻烦的方法来分配子块的那些路标和指针
				//创建一个新块并包装
				int newBlockOffset=myindexInfo.blockNum;
				////CatalogManager.addindexBlockNum(myindexInfo.indexName);
				myindexInfo.blockNum++;
				//FileManager.creatFile(filename);
				Block newBlock=BufferManager.getBlock(filename, newBlockOffset);
				InternalNode newNode=new InternalNode(newBlock);
				
				//我们知道新插入路标使超过了上限，也就是现有路标为MAX+1，我们总是为原来的块保留MIN个路标，这样新开的块有MAX+1-MIN个路标
				block.writeInt(1, MIN_CHILDREN_FOR_INTERNAL);
				newBlock.writeInt(1, MAX_CHILDREN_FOR_INTERNAL+1-MIN_CHILDREN_FOR_INTERNAL);
				
				for(int i=0;i<MIN_CHILDREN_FOR_INTERNAL;i++){ //假如新路标需插在原来的块也就是在MIN之内
					
					int pos=9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH);
					if(compareTo(branchKey,block.getBytes(pos,myindexInfo.columnLength))< 0){	//找到了路标插入的位置	
						System.arraycopy(block.data,  //把第MIN_CHILDREN_FOR_INTERNA条开始的记录copy到新block
								9+(MIN_CHILDREN_FOR_INTERNAL)*(myindexInfo.columnLength+POINTERLENGTH), 
								newBlock.data, 
								9, 
								POINTERLENGTH+(MAX_CHILDREN_FOR_INTERNAL-MIN_CHILDREN_FOR_INTERNAL)*(myindexInfo.columnLength+POINTERLENGTH));	
						System.arraycopy(block.data,  //给新插入条留出位置
								9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH), 
								block.data, 
								9+POINTERLENGTH+(i+1)*(myindexInfo.columnLength+POINTERLENGTH),
								//注意还要把原块的最后一个路标保留（不用于自身，但要作为父块的新插入路标）
								(MIN_CHILDREN_FOR_INTERNAL-1-i)*(myindexInfo.columnLength+POINTERLENGTH)+myindexInfo.columnLength);	
			
						//设置新插入信息
						block.writeInternalKey(9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH),branchKey,rightChild.block.blockoffset);				
						
						half=true;
						break;
					}
				}
				if(!half){ //新路标需插在新开的块也就是它的位置超出了MIN
					System.arraycopy(block.data,  //把第MIN_CHILDREN_FOR_INTERNA+1条开始的记录copy到新block
							9+(MIN_CHILDREN_FOR_INTERNAL+1)*(myindexInfo.columnLength+POINTERLENGTH), 
							newBlock.data, 
							9, 
							POINTERLENGTH+(MAX_CHILDREN_FOR_INTERNAL-MIN_CHILDREN_FOR_INTERNAL-1)*(myindexInfo.columnLength+POINTERLENGTH));
					for(int i=0;i<MAX_CHILDREN_FOR_INTERNAL-MIN_CHILDREN_FOR_INTERNAL-1;i++){
						int pos=9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH);
						
						if(compareTo(branchKey,newBlock.getBytes(pos,myindexInfo.columnLength)) < 0){
							System.arraycopy(newBlock.data,  //给新插入条留出位置
									9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH),
									newBlock.data, 
									9+POINTERLENGTH+(i+1)*(myindexInfo.columnLength+POINTERLENGTH),
									(MAX_CHILDREN_FOR_INTERNAL-MIN_CHILDREN_FOR_INTERNAL-1-i)*(myindexInfo.columnLength+POINTERLENGTH));								
							
							//设置新插入信息
							newBlock.writeInternalKey(9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH),branchKey,rightChild.block.blockoffset);				
							break;							
						}	
					}
				}
				
				//找出原块与新块之间的路标，提供给父节点做分裂用
				byte[] spiltKey=block.getBytes(9+POINTERLENGTH+(MIN_CHILDREN_FOR_INTERNAL)*(myindexInfo.columnLength+POINTERLENGTH),
						myindexInfo.columnLength);
				
				//更新新块的子块的父亲
				for(int j=0;j<=newBlock.readInt(1);j++){
					int childBlockNum=newBlock.readInt(9+j*(myindexInfo.columnLength+POINTERLENGTH));
					BufferManager.getBlock(filename, childBlockNum).writeInt(5, newBlockOffset);					
				}	
				
				int parentBlockNum;
				Block ParentBlock;
				InternalNode ParentNode;
				if(block.readData()[5]=='$'){  //没有父节点，则创建父节点
					//创建新块并包装
					parentBlockNum=myindexInfo.blockNum;
					//FileManager.creatFile(filename);
					ParentBlock=BufferManager.getBlock(filename, parentBlockNum);
					//myindexInfo.blockNum++;
					////CatalogManager.addindexBlockNum(myindexInfo.indexName);
					myindexInfo.blockNum++;
					
					//设置父块信息
					block.writeInt(5, parentBlockNum);
					newBlock.writeInt(5,parentBlockNum);
					
					ParentNode=new InternalNode(ParentBlock);
				}
				else{
					parentBlockNum=block.readInt(5);				
					newBlock.writeInt(5, parentBlockNum); //新块的父亲也就是旧块的父亲	
					ParentBlock=BufferManager.getBlock(filename, parentBlockNum);	
					ParentNode=new InternalNode(ParentBlock,true);
				}
				
				//读出父亲块并为其包装，然后再递归分裂
						
				return  ParentNode.branchInsert(spiltKey, this, newNode);//((InternalNode)createNode(ParentBlock)).branchInsert(spiltKey, this, newNode);
			}
			
			else{  //不需要分裂节点时
				int i;
				for(i=0;i<keyNum-1;i++){
					int pos=9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH);
					if(compareTo(branchKey,block.getBytes(pos,myindexInfo.columnLength)) < 0){ //找到插入的位置
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
		
		//以中间节点为单位的查找
		offsetInfo searchKey(byte[] key){
			int keyNum=block.readInt(1);
			int i=0;
			for(;i<keyNum;i++){
				int pos=9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH);
				
				if(compareTo(key,block.getBytes(pos,myindexInfo.columnLength)) < 0) break;
			}
			int nextBlockNum=block.readInt(9+i*(myindexInfo.columnLength+POINTERLENGTH));
			Block nextBlock=BufferManager.getBlock(filename, nextBlockNum);
			//根据块类别进行包装
			Node nextNode;
			if(nextBlock.readData()[0]=='I') nextNode=new InternalNode(nextBlock,true); 
			else nextNode=new LeafNode(nextBlock,true);
			
			return nextNode.searchKey(key); //递归查找
		}
		
		//以中间节点为单位的范围查找
		offsetInfo searchKey(byte[] skey,byte[] ekey){
			int keyNum=block.readInt(1);
			int i=0;
			for(;i<keyNum;i++){
				int pos=9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH);
				
				if(compareTo(skey,block.getBytes(pos,myindexInfo.columnLength)) < 0) break;
			}
			int nextBlockNum=block.readInt(9+i*(myindexInfo.columnLength+POINTERLENGTH));
			Block nextBlock=BufferManager.getBlock(filename, nextBlockNum);
			//根据块类别进行包装
			Node nextNode;
			if(nextBlock.readData()[0]=='I') nextNode=new InternalNode(nextBlock,true); 
			else nextNode=new LeafNode(nextBlock,true);
			
			return nextNode.searchKey(skey,ekey); //递归查找
		}

		

		//以中间节点为单位的删除
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
			
			return nextNode.delete(deleteKey); //递归删除
		}
	
		//删除过程中产生的节点合并，this块和after块以及它们之间的unionKey
		Block union(byte[] unionKey,Block afterBlock){
			int keyNum = block.readInt(1);
			int afterkeyNum= afterBlock.readInt(1);
			
			//将after块中的信息拷贝到this块的后面，注意留一个位置给unionKey
			System.arraycopy(afterBlock.data,
					9,
					block.data,
					9+(keyNum+1)*(myindexInfo.columnLength+POINTERLENGTH),
					POINTERLENGTH+afterkeyNum*(myindexInfo.columnLength+POINTERLENGTH));
			
			//填入unionKey
			block.writeData(9+keyNum*(myindexInfo.columnLength+POINTERLENGTH)+POINTERLENGTH, unionKey,unionKey.length);
			
			//重新计算路标大小（原路标数+after路标数+unionKey）
			keyNum=keyNum+afterkeyNum+1;		
			block.writeInt(1, keyNum);
			
			//找到父块
			int parentBlockNum=block.readInt(5);
			Block parentBlock=BufferManager.getBlock(filename, parentBlockNum); 
			
			//请Block销毁after块
		//	afterBlock.isvalid=false;
			myindexInfo.blockNum--;
			
			//在父块中删除after块
			return (new InternalNode(parentBlock,true)).delete(afterBlock);
			
		}
		
		//删除过程中产生的兄弟块内容重排，this块和after块以及它们之间的internalKey,返回的changeKey是为了更新父块中它们两指针中间的键值
		byte[] rearrangeAfter(Block siblingBlock,byte[] InternalKey){ //兄弟节点在其后
			int siblingKeyNum=siblingBlock.readInt(1);
			int keyNum = block.readInt(1);
			
			//要转移的一条指针内容
			int blockOffset=siblingBlock.readInt(9);
			//将internalKey和兄弟块的第一条指针复制到this块的尾部，路标数加1
			block.writeInternalKey(9+POINTERLENGTH+keyNum*(myindexInfo.columnLength+POINTERLENGTH), InternalKey, blockOffset);
			keyNum++;
			block.writeInt(1, keyNum);
			
			//兄弟块的路标数减1，获取兄弟块的第一条键值作为更新父块的键值，再将兄弟块后面的信息向前挪一条指针和路标的距离
			siblingKeyNum--;
			siblingBlock.writeInt(1, siblingKeyNum);
			byte[] changeKey=siblingBlock.getBytes(9+POINTERLENGTH, myindexInfo.columnLength);
			System.arraycopy(siblingBlock.data, 9+POINTERLENGTH+myindexInfo.columnLength, siblingBlock.data, 9, POINTERLENGTH+siblingKeyNum*(POINTERLENGTH+myindexInfo.columnLength));
					
			return changeKey;
			
		}

		byte[] rearrangeBefore(Block siblingBlock,byte[] internalKey){ //兄弟节点在其前
			int siblingKeyNum=siblingBlock.readInt(1);
			int keyNum = block.readInt(1);
			
			siblingKeyNum--;
			siblingBlock.writeInt(1, siblingKeyNum);
			
			//兄弟块的最后一条路标作为父块的更新路标，最后一条指针将转移给this块
			byte[] changeKey=siblingBlock.getBytes(9+POINTERLENGTH+siblingKeyNum*(POINTERLENGTH+myindexInfo.columnLength), myindexInfo.columnLength);		
			int blockOffset=siblingBlock.readInt(9+(siblingKeyNum+1)*(POINTERLENGTH+myindexInfo.columnLength));
			
			//给新指针和路标让出位置
			System.arraycopy(block.data, 9, block.data, 9+POINTERLENGTH+myindexInfo.columnLength, POINTERLENGTH+keyNum*(POINTERLENGTH+myindexInfo.columnLength));
			block.writeInt(9, blockOffset); //插入从兄弟块挪来的这条指针
			block.writeData(9+POINTERLENGTH, internalKey, internalKey.length); //插入internalKey
			keyNum++;
			block.writeInt(1, keyNum);
					
			return changeKey;
		}

		//修改posBlockNum标号后面的路标
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
		
		//在中间节点中删除一个子块信息（以及他前面的那条路标）
		Block	delete(Block blk){
			int keyNum = block.readInt(1);
			
			for(int i=0;i<=keyNum;i++){
				int pos=9+i*(myindexInfo.columnLength+POINTERLENGTH);
				int ptr=block.readInt(pos);
				if(ptr==blk.blockoffset){ //如果找到了子块标号
					System.arraycopy(block.data, //把这条标号和前面的路标都移除
							9+POINTERLENGTH+(i-1)*(myindexInfo.columnLength+POINTERLENGTH), 
							block.data, 
							9+POINTERLENGTH+i*(myindexInfo.columnLength+POINTERLENGTH), 
							(keyNum-i)*(myindexInfo.columnLength+POINTERLENGTH));
					keyNum--;
					block.writeInt(1, keyNum);
			
					if(keyNum >=MIN_CHILDREN_FOR_INTERNAL) return null; //? //移除后直接结束
			
					if(block.readData()[5]=='$'){  //还是用'R'比较好? //没有父节点时
						
						if(keyNum==0){	//没有路标，只有一个子块标号时，把它的子块作为根块，把this块删除
							//block.isvalid=false;
							myindexInfo.blockNum--;
							return BufferManager.getBlock(filename, block.readInt(9));
						}
							
						return null;
					}
			
					//找到父亲块
					int parentBlockNum=block.readInt(5);
					Block parentBlock=BufferManager.getBlock(filename, parentBlockNum);
					int parentKeyNum=parentBlock.readInt(1);
					
					int sibling;
					Block siblingBlock;
					int j=0;
					//查找后续兄弟块
					for(;j<parentKeyNum;j++){
						int ppos=9+j*(myindexInfo.columnLength+POINTERLENGTH);
						if(block.blockoffset==parentBlock.readInt(ppos)){ 
							//读到后续兄弟块
							sibling=parentBlock.readInt(ppos+POINTERLENGTH+myindexInfo.columnLength);
							siblingBlock=BufferManager.getBlock(filename, sibling);
								
							byte[] unionKey=parentBlock.getBytes(ppos+POINTERLENGTH, myindexInfo.columnLength);
							
							//能够合并
							if((siblingBlock.readInt(1)+keyNum)<=MAX_CHILDREN_FOR_INTERNAL){				
								return this.union(unionKey,siblingBlock);
							}
							
							//两个节点原来都是MIN_FOR_LEAF，delete一个以后的情况没考虑，这个时候会涉及三个节点
							//这种情况没有处理，要处理的话将涉及到4个兄弟块，因为this块有MIN-1个路标，兄弟块有MIN个路标，向兄弟块挪一个路标显然是白费力气，
							//但是它们却也不一定能够合并，所以很麻烦
							if(siblingBlock.readInt(1)==MIN_CHILDREN_FOR_INTERNAL) return null;
							
							//不能合并，通过从兄弟块转移一个路标来满足B+树路标最小限制
							(new InternalNode(parentBlock,true)).exchange(rearrangeAfter(siblingBlock,unionKey),block.blockoffset);//blockOffset请bufferManager务必设计好
							return null;
					
						}				
					}
					
					//找不后续块，只能找前续兄弟块
					sibling=parentBlock.readInt(9+(parentKeyNum-1)*(myindexInfo.columnLength+POINTERLENGTH));
					siblingBlock=BufferManager.getBlock(filename, sibling);		
								
					byte[] unionKey=parentBlock.getBytes(9+(parentKeyNum-1)*(myindexInfo.columnLength+POINTERLENGTH)+POINTERLENGTH, myindexInfo.columnLength);
					
					//能够合并
					if((siblingBlock.readInt(1)+keyNum)<=MAX_CHILDREN_FOR_INTERNAL){		
						return (new InternalNode(siblingBlock,true)).union(unionKey,block);
					}
						
					//没有考虑的情况
					if(siblingBlock.readInt(1)==MIN_CHILDREN_FOR_INTERNAL) return null;
					
					//重排的情况
					(new InternalNode(parentBlock,true)).exchange(rearrangeBefore(siblingBlock,unionKey),sibling);
					return null;
				}
	
			}		
			return null;
		}
				
	}
	//叶子节点类
	class LeafNode extends Node{
				
		LeafNode(Block blk){
			block=blk;
			
	    	block.data[0]='L';  //标识为叶子块
	    	int i=5;
			block.writeInt(1, 0);//现在共有0个key值//现在共有0个key值
	    	for(;i<9;i++)
	    		block.data[i]='$';  //没有父块
	    	for(;i<13;i++)
	    		block.data[i]='&';  //最后一块叶子块的下一块叶子块标号，相当于null
	    	block.writeData();
		}
		
		LeafNode(Block blk,boolean t){
			block=blk;	
		}

		//以叶子节点为单位的索引的插入
		Block insert(byte[] insertKey,int blockOffset, int offset){//键值 块号 块中偏移
			int keyNum = block.readInt(1);
			
			if(++keyNum>MAX_FOR_LEAF){  //分裂节点
				boolean half=false;
				//FileManager.creatFile(filename);
				Block newBlock=BufferManager.getBlock(filename, myindexInfo.blockNum);
				////CatalogManager.addindexBlockNum(myindexInfo.indexName);
				myindexInfo.blockNum++;
				LeafNode newNode=new LeafNode(newBlock);
				
				for(int i=0;i<MIN_FOR_LEAF-1;i++){ //插入原块
					int pos=17+i*(myindexInfo.columnLength+8);
					if(compareTo( insertKey,block.getBytes(pos,myindexInfo.columnLength))< 0){					
						System.arraycopy(block.data,  //把第MIN_FOR_LEAF-1条开始的记录copy到新block
								9+(MIN_FOR_LEAF-1)*(myindexInfo.columnLength+8), 
								newBlock.data, 
								9, 
								POINTERLENGTH+(MAX_FOR_LEAF-MIN_FOR_LEAF+1)*(myindexInfo.columnLength+8));	
						System.arraycopy(block.data,  //给新插入条留出位置
								9+i*(myindexInfo.columnLength+8), 
								block.data, 
								9+(i+1)*(myindexInfo.columnLength+8),
								POINTERLENGTH+(MIN_FOR_LEAF-1-i)*(myindexInfo.columnLength+8));	
						
						//插入新内容
						block.setKeydata(9+i*(myindexInfo.columnLength+8),insertKey,blockOffset,offset);				
											
						half=true;
						break;
					}
				}				
				if(!half){ //插入新块
					System.arraycopy(block.data,  //把第MIN_FOR_LEAF条开始的记录copy到新block
							9+(MIN_FOR_LEAF)*(myindexInfo.columnLength+8), 
							newBlock.data, 
							9, 
							POINTERLENGTH+(MAX_FOR_LEAF-MIN_FOR_LEAF)*(myindexInfo.columnLength+8));
					int i=0;
					for(;i<MAX_FOR_LEAF-MIN_FOR_LEAF;i++){
						int pos=17+i*(myindexInfo.columnLength+8);
						if(compareTo(insertKey,newBlock.getBytes(pos,myindexInfo.columnLength)) < 0){
							System.arraycopy(newBlock.data,  //给新插入条留出位置
									9+i*(myindexInfo.columnLength+8), 
									newBlock.data, 
									9+(i+1)*(myindexInfo.columnLength+8), 
									POINTERLENGTH+(MAX_FOR_LEAF-MIN_FOR_LEAF-i)*(myindexInfo.columnLength+8));								
							
							//插入新内容
							newBlock.setKeydata(9+i*(myindexInfo.columnLength+8),insertKey,blockOffset,offset);
							break;
						}	
					}
					if(i==MAX_FOR_LEAF-MIN_FOR_LEAF){
						System.arraycopy(newBlock.data,  //给新插入条留出位置
								9+i*(myindexInfo.columnLength+8), 
								newBlock.data, 
								9+(i+1)*(myindexInfo.columnLength+8), 
								POINTERLENGTH+(MAX_FOR_LEAF-MIN_FOR_LEAF-i)*(myindexInfo.columnLength+8));								
						
						//插入新内容
						newBlock.setKeydata(9+i*(myindexInfo.columnLength+8),insertKey,blockOffset,offset);
					}
				}
				
				block.writeInt(1,MIN_FOR_LEAF);
			    newBlock.writeInt(1,MAX_FOR_LEAF+1-MIN_FOR_LEAF);
			    
			    //原块最后添上新块的块号，以做成所有叶子节点的一串链表
			    block.writeInt(9+MIN_FOR_LEAF*(myindexInfo.columnLength+8), newBlock.blockoffset);
				
			    int parentBlockNum;
			    Block ParentBlock;
			    InternalNode ParentNode;
				if(block.readData()[5]=='$'){  //没有父节点，则创建父节点
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
					newBlock.writeInt(5, parentBlockNum); //新节点的父亲也就是旧节点的父亲
					ParentBlock=BufferManager.getBlock(filename, parentBlockNum);
					ParentNode=new InternalNode(ParentBlock,true);
				}
			
				//将branchKey(新块的第一个索引键值)提交，让父块分裂出它们两个块
				byte[] branchKey=newBlock.getBytes(17, myindexInfo.columnLength);
				
				return  ParentNode.branchInsert(branchKey, this, newNode);
			}
			
			else{  //不需要分裂节点时
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
					
					if(compareTo(insertKey,block.getBytes(pos,myindexInfo.columnLength))==0){ //已有键值则更新
						block.setKeydata(9+i*(myindexInfo.columnLength+8),insertKey,blockOffset,offset);
						return null;
					}
					
					if(compareTo(insertKey,block.getBytes(pos,myindexInfo.columnLength)) < 0){ //找到插入的位置
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
		
		//以叶子节点为单位的查找索引
		offsetInfo searchKey(byte[] originalkey){
			int keyNum=block.readInt(1); 
			if(keyNum==0) return null; //空块则返回null
		
			byte[] key=new byte[myindexInfo.columnLength];
			
			int i=0;
			for(;i<originalkey.length;i++){
				key[i]=originalkey[i];
			}
			
		    for(;i<myindexInfo.columnLength;i++){
				key[i]='&';
			}
			
			//二分查找
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
			
			//将找到的位置上的表文件偏移信息存入off中，返回给上级调用
            offsetInfo off=new offsetInfo();
            
            off.offsetInfile.add(block.readInt(pos));
            off.offsetInBlock.add(block.readInt(pos+4));
            off.length=1;
					
            return compareTo(middleKey,key) == 0 ? off : null;   //再次确认有没有找到这个索引键值，没有则返回null
		}
		
		//以叶子节点为单位的范围查找索引
		offsetInfo searchKey(byte[] originalkey, byte[] endkey){
			int keyNum=block.readInt(1); 
			if(keyNum==0) return null; //空块则返回null
		
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
			
			//二分查找
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
					
            if(compareTo(middleKey,key) != 0) return null;   //再次确认有没有找到这个索引键值，没有则返回null
            else{
    			//将找到的位置上的表文件偏移信息存入off中，返回给上级调用
                offsetInfo off=new offsetInfo();
                while(compareTo(middleKey,ekey)<=0){
                    off.offsetInfile.add(block.readInt(pos));
                    off.offsetInBlock.add(block.readInt(pos+4));
                    middle+=1;
                    off.length++;
                    if(middle>=keyNum){
                    	//获取尾指针并读取新的块进来
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

		

		//以叶子节点为单位的块合并
		Block union(Block afterBlock){
			int keyNum = block.readInt(1);
			int afterkeyNum= afterBlock.readInt(1);
			
			//将after块的内容复制到this块的后面
			System.arraycopy(afterBlock.data,9,block.data,9+keyNum*(myindexInfo.columnLength+8),POINTERLENGTH+afterkeyNum*(myindexInfo.columnLength+8));
			
			//更新索引数量
			keyNum+=afterkeyNum;		
			block.writeInt(1, keyNum);
						
			//请bufferManager销毁after块
			//afterBlock.isvalid=false;
			myindexInfo.blockNum--;
			
			//在父节点中删除这个被废弃的after块的信息(标号及它前面的路标)
			int parentBlockNum=block.readInt(5);
			Block parentBlock=BufferManager.getBlock(filename, parentBlockNum); 
			
			return (new InternalNode(parentBlock,true)).delete(afterBlock);
			
		}
		
		//从兄弟节点挪来一条索引进行重排
		byte[] rearrangeAfter(Block siblingBlock){ //兄弟节点在其后
			int siblingKeyNum=siblingBlock.readInt(1);
			int keyNum = block.readInt(1);
			
			//要挪过来的索引信息
			int blockOffset=siblingBlock.readInt(9);
			int offset=siblingBlock.readInt(13);
			byte[] Key=siblingBlock.getBytes(17, myindexInfo.columnLength);
			
			//更新兄弟块的内容
			siblingKeyNum--;
			siblingBlock.writeInt(1, siblingKeyNum);
			System.arraycopy(siblingBlock.data, 9+8+myindexInfo.columnLength, siblingBlock.data, 9, POINTERLENGTH+siblingKeyNum*(8+myindexInfo.columnLength));
			
			//this块和兄弟块之间的新路标
			byte[] changeKey=siblingBlock.getBytes(17, myindexInfo.columnLength);
			
			//添加挪来的索引
			block.setKeydata(9+keyNum*(myindexInfo.columnLength+8), Key, blockOffset, offset);
			keyNum++;
			block.writeInt(1, keyNum);
			//注意不要漏了链表信息(下一块的标号)
			block.writeInt(9+keyNum*(myindexInfo.columnLength+8), siblingBlock.blockoffset);
			
			return changeKey;
			
		}
		
		byte[] rearrangeBefore(Block siblingBlock){  //兄弟节点在其前
			int siblingKeyNum=siblingBlock.readInt(1);
			int keyNum = block.readInt(1);
			
			siblingKeyNum--;
			siblingBlock.writeInt(1, siblingKeyNum);
			
			//要挪来的索引信息
			int blockOffset=siblingBlock.readInt(9+siblingKeyNum*(myindexInfo.columnLength+8));
			int offset=siblingBlock.readInt(13+siblingKeyNum*(myindexInfo.columnLength+8));
			byte[] Key=siblingBlock.getBytes(17+siblingKeyNum*(myindexInfo.columnLength+8), myindexInfo.columnLength);
			
			//保存好链表信息
			siblingBlock.writeInt(9+siblingKeyNum*(myindexInfo.columnLength+8), block.blockoffset);
			
			//挪出新索引位置
			System.arraycopy(block.data, 9, block.data, 9+8+myindexInfo.columnLength, POINTERLENGTH+keyNum*(8+myindexInfo.columnLength));
			block.setKeydata(9, Key, blockOffset, offset); //插入新索引
			keyNum++;
			block.writeInt(1, keyNum);
			
			//需要父块的更新成的新路标
			byte[] changeKey=block.getBytes(17, myindexInfo.columnLength);
			
			return changeKey;
		}
		
		//以叶子节点为单位的索引删除
		Block delete(byte[] deleteKey){
			
			int keyNum = block.readInt(1);
			
			for(int i=0;i<keyNum;i++){
				int pos=17+i*(myindexInfo.columnLength+8);
				
				if(compareTo(deleteKey,block.getBytes(pos,myindexInfo.columnLength))<0){ //没找到索引键值
					//System.out.println("没有该索引键值");
					return null;
				}
				
				if(compareTo(deleteKey,block.getBytes(pos,myindexInfo.columnLength)) == 0){ //找到对应的键值					
					
					System.arraycopy(block.data, //移除这条索引
									9+(i+1)*(myindexInfo.columnLength+8), 
									block.data, 
									9+i*(myindexInfo.columnLength+8), 
									POINTERLENGTH+(keyNum-1-i)*(myindexInfo.columnLength+8));
					keyNum--;
					block.writeInt(1, keyNum);
					
					if(keyNum >=MIN_FOR_LEAF) return null; //仍然满足数量要求
					
					if(block.readData()[5]=='$') return null;  //没有父块，本身为根
					
					boolean lastFlag=false;
					if(block.readData()[9+keyNum*(myindexInfo.columnLength+8)]=='&') lastFlag=true; //叶子块链表的最后一块
					
					int sibling=block.readInt(9+keyNum*(myindexInfo.columnLength+8)); 
					Block siblingBlock=BufferManager.getBlock(filename, sibling);
					int parentBlockNum=block.readInt(5);
					
					if(lastFlag || siblingBlock==null || siblingBlock.readInt(5)!=parentBlockNum/*虽然有后续块但不是同一个父亲的兄弟块*/){  //没有找到后续兄弟节点
						//通过父块找前续兄弟块
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
						
						//可以合并
						if((siblingBlock.readInt(1)+keyNum)<=MAX_FOR_LEAF){
							return (new LeafNode(siblingBlock,true)).union(block);
						}
									
						//两个节点原来都是MIN_FOR_LEAF，delete一个以后的情况没考虑，这个时候会涉及三个节点
						if(siblingBlock.readInt(1)==MIN_FOR_LEAF) return null;
							
						//重排
						(new InternalNode(parentBlock,true)).exchange(rearrangeBefore(siblingBlock),sibling);
						return null;
					}
			
					//合并
					if((siblingBlock.readInt(1)+keyNum)<=MAX_FOR_LEAF){
						return this.union(siblingBlock);
					}
					
					//没考虑的情况
					if(siblingBlock.readInt(1)==MIN_FOR_LEAF) return null;
					
					//重排
					Block parentBlock=BufferManager.getBlock(filename, parentBlockNum);
					(new InternalNode(parentBlock,true)).exchange(rearrangeAfter(siblingBlock),block.blockoffset);//blockOffset请bufferManager务必设计好
					return null;
				}
			}
			
			return null;
		}
	}
}

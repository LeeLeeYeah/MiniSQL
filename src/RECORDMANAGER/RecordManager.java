//
//                       _oo0oo_
//                      o8888888o
//                      88" . "88
//                      (| -_- |)
//                      0\  =  /0
//                    ___/`—'\___
//                  .' \\|     |// '.
//                 / \\|||  :  |||// \
//                / _||||| -:- |||||- \
//               |   | \\\  -  /// |   |
//               | \_|  ''\—/''  |_/ |
//               \  .-\__  '-'  ___/-. /
//             ___'. .'  /—.—\  `. .'___
//          ."" '<  `.___\_<|>_/___.' >' "".
//         | | :  `- \`.;`\ _ /`;.`/ - ` : | |
//         \  \ `_.   \_ __\ /__ _/   .-` /  /
//     =====`-.____`.___ \_____/___.-`___.-'=====
//                       `=—='
//
//
//     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//                      好人一生平安
package RECORDMANAGER;

import java.util.Comparator;
import java.util.Vector;

import BUFFERMANAGER.*;
import CATALOGMANAGER.*;
import FILEMANAGER.FileManager;
import IndexManager.*;


//功能描述：用来组织表中的记录在每个块如何存储
public class RecordManager {
	static final char EMPTY = 0;

	
//	功能描述：给定表名和多个tupleOffsets所构成的vector，返回对应多个tuple所构成的vector。如果给定tupleOffset处数据为空或数据已被删除，则对应结果中为null。
//	实现原理：先根据tupleOffset和每个记录的长度，计算出对应记录在文件对应的块号blockoffset以及块内字节偏移byteoffset，从对应块中读出数据，再根据每个attribute的类型进行转换，逐个存入tuple中。
	public static Vector<tuple> getTuple(String tablename,
			Vector<Integer> tupleOffsets) {
		final int tinb = Block.BLOCKSIZE
				/ (SIZEINT + CatalogManager.getTupleLength(tablename));// 记录一个block里有几个tuple。在每个tuple前面放一个int以代替指针。
		Vector<tuple> res = new Vector<tuple>(0);
		for (int ii = 0; ii < tupleOffsets.size(); ii++) {
			int blockoffset = tupleOffsets.elementAt(ii) / tinb;
			Block block = BufferManager.getBlock(tablename, blockoffset);
			int byteoffset = (SIZEINT + CatalogManager
					.getTupleLength(tablename))
					* (tupleOffsets.elementAt(ii) % tinb);
			if (block.readInt(byteoffset) >= 0)
				continue;
			byteoffset += 4;
			tuple T = new tuple();
			// 读出一个tuple
			for (int i = 0; i < CatalogManager.getTableAttriNum(tablename); i++) {
				if (CatalogManager.getType(tablename, i).equals("int")) {
					T.units.add(i, String.valueOf(block.readInt(byteoffset)));
					byteoffset += 4;
				} else if (CatalogManager.getType(tablename, i).equals("float")) {
					T.units.add(i, String.valueOf(block.readFloat(byteoffset)));
					byteoffset += 4;
				} else if (CatalogManager.getType(tablename, i).equals("char")) {
					T.units.add(
							i,
							block.readString(byteoffset,
									CatalogManager.getLength(tablename, i)));
					byteoffset += CatalogManager.getLength(tablename, i);
				}
			}
			res.add(T);
		}
		return res;
	}

//	功能描述：给定表名和多个tupleOffsets所构成的vector，返回对应多个tuple所构成的vector。如果给定tupleOffset处数据为空或数据已被删除，则对应结果中为null。
//	实现原理：先根据tupleOffset和每个记录的长度，计算出对应记录在文件对应的块号blockoffset以及块内字节偏移byteoffset，从对应块中读出数据，再根据每个attribute的类型进行转换，逐个存入tuple中。
	public static tuple getTuple(String tablename, int tupleOffset) {
		final int tinb = Block.BLOCKSIZE
				/ (SIZEINT + CatalogManager.getTupleLength(tablename));// 记录一个block里有几个tuple。在每个tuple前面放一个int以代替指针。
		int blockoffset = tupleOffset / tinb;
		Block block = BufferManager.getBlock(tablename, blockoffset);
		int byteoffset = (SIZEINT + CatalogManager.getTupleLength(tablename))
				* (tupleOffset % tinb);
		if (block.readInt(byteoffset) >= 0)
			return null;
		byteoffset += 4;
		tuple T = new tuple();
		// 读出一个tuple
		for (int i = 0; i < CatalogManager.getTableAttriNum(tablename); i++) {
			if (CatalogManager.getType(tablename, i).equals("int")) {
				T.units.add(i, String.valueOf(block.readInt(byteoffset)));
				byteoffset += 4;
			} else if (CatalogManager.getType(tablename, i).equals("float")) {
				T.units.add(i, String.valueOf(block.readFloat(byteoffset)));
				byteoffset += 4;
			} else if (CatalogManager.getType(tablename, i).equals("char")) {
				T.units.add(
						i,
						block.readString(byteoffset,
								CatalogManager.getLength(tablename, i)));
				byteoffset += CatalogManager.getLength(tablename, i);
			}
		}
		return T;
	}

//	功能描述：给定表名，创建表文件，初始化表头。
//	实现原理：调用FileManager创建文件。调用bufferManager在表头指针处写0。
	public static boolean createTable(String tableName) {
		if (FileManager.findFile(tableName) == true)
			return false;
		// 建立表头
		FileManager.creatFile(tableName);
		Block block = BufferManager.getBlock(tableName, 0);
		block.writeInt(0, 0);
		return true;
	}

//	功能描述：给定表名，删除表文件。
//	实现原理：调用FileManager删除文件。调用bufferManager把该表的脏数据清除掉。
	public static boolean dropTable(String tableName) {
		if (FileManager.findFile(tableName) == false)
			return false;
		BufferManager.dropblocks(tableName);
		FileManager.dropFile(tableName);
		return true;
	}

//	功能描述：给定表名，及一个tuple，插入表中，并返回所插入位置的tupleOffset。
//	实现原理：先从表头查询FreeList,如果有被删除后留下来的空位，则插入表头指向的空位，并让表头指向下一个空位。如果FreeList中没有空余，则通过Catalog Manager获得表当前的tuple数量，以此计算出表末尾的块号blockOffset及字节偏移byteOffset，插入对应位置。
	public static int insert(String tablename, tuple Tuple) {
		final int tinb = Block.BLOCKSIZE
				/ (SIZEINT + CatalogManager.getTupleLength(tablename));// 记录一个block里有几个tuple。在每个tuple前面放一个int以代替指针。

		// 从表头查询是否有被删除后留下的空位
		Block block1 = BufferManager.getBlock(tablename, 0);
		int tupleoffset = block1.readInt(0);
		Block block2 = null;
		if (tupleoffset > 0) {
			// 指向下一个空位置
			block2 = BufferManager.getBlock(tablename, tupleoffset / tinb);
			int nexttupleoffset = block2.readInt((CatalogManager
					.getTupleLength(tablename) + SIZEINT)
					* (tupleoffset % tinb));
			block1.writeInt(0, nexttupleoffset);
		} else {
			tupleoffset = 1 + CatalogManager.getTupleNum(tablename);
			block2 = BufferManager.getBlock(tablename, tupleoffset / tinb);
		}

		// 插入数据
		int byteoffset = (SIZEINT + CatalogManager.getTupleLength(tablename))
				* (tupleoffset % tinb);
		block2.writeInt(byteoffset, -1);// 用-1指示条记录有效
		byteoffset += 4;

		for (int i = 0; i < Tuple.units.size(); i++) {
			if (CatalogManager.getType(tablename, i).equals("int")) {
				block2.writeInt(byteoffset,
						Integer.parseInt(Tuple.units.elementAt(i)));
				byteoffset += 4;
			} else if (CatalogManager.getType(tablename, i).equals("float")) {
				block2.writeFloat(byteoffset,
						Float.parseFloat(Tuple.units.elementAt(i)));
				byteoffset += 4;
			} else if (CatalogManager.getType(tablename, i).equals("char")) {
				block2.writeString(byteoffset, Tuple.units.elementAt(i),
						CatalogManager.getLength(tablename, i));
				byteoffset += CatalogManager.getLength(tablename, i);
			}

		}
		return tupleoffset;
	}

	
//	功能描述：用于做select时的projection。给定表名，及要选出来属性名称，输入select函数返回的结果，返回经projection后的结果。
//	实现原理：根据要选出来属性名称，把tuple中没有被选择的属性值删去。
	public static Vector<tuple> project(Vector<tuple> res, String tablename,
			Vector<String> attriNames) {
		Vector<tuple> newres = new Vector<tuple>(0);
		for (int i = 0; i < res.size(); i++) {
			tuple T = new tuple();
			for (int j = 0; j < attriNames.size(); j++) {
				T.units.add(res.elementAt(i).units.elementAt(CatalogManager
						.getAttriOffest(tablename, attriNames.elementAt(j))));
			}
			newres.add(T);
		}
		return newres;
	}

//	功能描述：用于普通select语句。输入表名和判断条件，选出符合条件的记录存储在Vector<tuple>中并返回
//	实现原理：对表中的有效记录进行逐个遍历，读出记录至tuple T并使用condition.calc(tuple T)去判断每条记录是否符合条件，符合则放入Vector<tuple>中，遍历完后返回结果。
	public static Vector<tuple> select(String tablename, conditionNode condition) {
		final int tinb = Block.BLOCKSIZE
				/ (SIZEINT + CatalogManager.getTupleLength(tablename));// 记录一个block里有几个tuple。在每个tuple前面放一个int以代替指针。

		Vector<tuple> res = new Vector<tuple>(0);
		Block block = BufferManager.getBlock(tablename, 0);
		int blockoffset = 0;
		int tupleoffset = 1;
		int count = 0;
		while (count < CatalogManager.getTupleNum(tablename)) {
			if (blockoffset < tupleoffset / tinb) {
				blockoffset++;
				block = BufferManager.getBlock(tablename, blockoffset);
			}
			int byteoffset = (SIZEINT + CatalogManager
					.getTupleLength(tablename)) * (tupleoffset % tinb);
			if (block.readInt(byteoffset) >= 0) {
				tupleoffset++;
				continue;
			} else
				byteoffset += 4;
			tuple T = new tuple();
			// 读出一个tuple
			for (int i = 0; i < CatalogManager.getTableAttriNum(tablename); i++) {
				if (CatalogManager.getType(tablename, i).equals("int")) {
					T.units.add(i, String.valueOf(block.readInt(byteoffset)));
					byteoffset += 4;
				} else if (CatalogManager.getType(tablename, i).equals("float")) {
					T.units.add(i, String.valueOf(block.readFloat(byteoffset)));
					byteoffset += 4;
				} else if (CatalogManager.getType(tablename, i).equals("char")) {
					T.units.add(
							i,
							block.readString(byteoffset,
									CatalogManager.getLength(tablename, i)));
					byteoffset += CatalogManager.getLength(tablename, i);
				}
			}

			if (condition == null || condition.calc(tablename, T) == true)
				res.add(T);
			count++;
			tupleoffset++;
		}
		return res;
	}

//	功能描述：用于带有排序功能order的select语句。输入表名、判断条件、排序依据属性名、升序排序或降序排序，选出符合条件的记录存储在Vector<tuple>中，排序后返回
//	实现原理：对表中的有效记录进行逐个遍历，读出记录至tuple T并使用condition.calc(tuple T)去判断每条记录是否符合条件，符合则放入Vector<tuple>中，遍历完后，调用vector中的sort函数进行排序，返回结果。
	public static Vector<tuple> select(String tablename,
			conditionNode condition, String orderAttriName, boolean isInc) {
		Vector<tuple> res = select(tablename, condition);
		if (isInc)
			compareParaInc = true;
		else
			compareParaInc = false;
		comparePara = CatalogManager.getAttriOffest(tablename, orderAttriName);
		compareParaType = CatalogManager.getType(tablename, orderAttriName);
		res.sort(new MyCompare());
		return res;
	}

	static int comparePara;
	static boolean compareParaInc;
	static String compareParaType;

//	功能描述：用于带有排序功能order的select语句，通过指定要比较属性名称来进行比较。
//	实现原理：通过属性名称取出数值进行比较。
	static class MyCompare implements Comparator<tuple> // 实现Comparator，定义自己的比较方法
	{
		public int compare(tuple t1, tuple t2) {
			String num1 = t1.units.elementAt(comparePara);
			String num2 = t2.units.elementAt(comparePara);
			if (compareParaType.equals("int"))
				if (compareParaInc)// 不确定会不会写反了，等debug的时候看看@@@@@@@@@@@@@@@@
					return (Integer.parseInt(num1) - Integer.parseInt(num2));
				else
					return (Integer.parseInt(num2) - Integer.parseInt(num1));
			else if (compareParaType.equals("float"))
				if (compareParaInc)
					return (int) (Float.parseFloat(num1) - Float
							.parseFloat(num2));
				else
					return (int) (Float.parseFloat(num2) - Float
							.parseFloat(num1));
			else if (compareParaInc)// 不确定会不会写反了，等debug的时候看看@@@@@@@@@@@@@@@@
				return num1.compareTo(num2);
			else
				return num2.compareTo(num1);
		}
	}

//	功能描述：用于delete语句。输入表名和判断条件，删除符合条件的记录，返回被删除的记录数
//	实现原理：对表中的有效记录进行逐个遍历，读出记录至tuple T并使用condition.calc(tuple T)去判断每条记录是否符合条件，符合则把该记录的标记为标为已删除。
	public static int delete(String tablename, conditionNode condition) {
		final int tinb = Block.BLOCKSIZE
				/ (SIZEINT + CatalogManager.getTupleLength(tablename));// 记录一个block里有几个tuple。在每个tuple前面放一个int以代替指针。

		Block block1 = BufferManager.getBlock(tablename, 0);
		block1.fix();// 把第一个块fix在缓冲区
		Block block2 = BufferManager.getBlock(tablename, 0);
		int blockoffset = 0;
		int tupleoffset = 1;
		int count = 0;
		int numdeleted = 0;
		int tuplenum = CatalogManager.getTupleNum(tablename);
		while (count < tuplenum) {
			if (blockoffset < tupleoffset / tinb) {
				blockoffset++;
				block2 = BufferManager.getBlock(tablename, blockoffset);
			}
			int byteoffset = (SIZEINT + CatalogManager
					.getTupleLength(tablename)) * (tupleoffset % tinb);
			if (block2.readInt(byteoffset) >= 0) {
				tupleoffset++;
				continue;
			} else
				byteoffset += 4;
			tuple T = new tuple();
			int pointer = byteoffset - 4;
			// 读出一个tuple
			for (int i = 0; i < CatalogManager.getTableAttriNum(tablename); i++) {
				if (CatalogManager.getType(tablename, i).equals("int")) {
					T.units.add(i, String.valueOf(block2.readInt(byteoffset)));
					byteoffset += 4;
				} else if (CatalogManager.getType(tablename, i).equals("float")) {
					T.units.add(i, String.valueOf(block2.readFloat(byteoffset)));
					byteoffset += 4;
				} else if (CatalogManager.getType(tablename, i).equals("char")) {
					T.units.add(
							i,
							block2.readString(byteoffset,
									CatalogManager.getLength(tablename, i)));
					byteoffset += CatalogManager.getLength(tablename, i);
				}
			}

			if (condition == null || condition.calc(tablename, T) == true) {
				//######################################################################ZZZZZZZZZZRRRRRRRRRRRRRRRRRZZZZZZZZZZZZ
				try{
					for(int i = 0; i < CatalogManager.getTableAttriNum(tablename);i++){
						String indexname = CatalogManager.getIndexName(tablename, CatalogManager.getAttriName(tablename, i));
						if(indexname==null)
							continue;
						index tmpindex = CatalogManager.getIndex(indexname);
						if(indexname!=null){
							IndexManager.deleteKey(tmpindex, T.units.elementAt(i));
						}
						CatalogManager.updateIndexTable(indexname, tmpindex);
					}
				}catch(Exception e){
					System.err.println(e);
				}
				//######################################################################ZZZZZZZZZZRRRRRRRRRRRRRRRRRZZZZZZZZZZZZ
				int head = block1.readInt(0);
				block2.writeInt(pointer, head);
				block1.writeInt(0, tupleoffset);
				numdeleted++;
			}
			count++;
			tupleoffset++;
		}
		block1.unfix();
		return numdeleted;

	}

	
//	功能描述：用于select * join from A.a=B.b指令，指定两个表及其对应属性进行join，返回对应结果。
//	实现原理：用select函数获得两个表中的所有记录，用两重循环进行两两匹配，如果指定的属性值相等则加入结果中。
	public static Vector<tuple> join(String tableName1,
			String attributeName1, String tableName2, String attributeName2) {
		Vector<tuple> res1 = select(tableName1, null);
		Vector<tuple> res2 = select(tableName2, null);
		Vector<tuple> res = new Vector<tuple>(0);
		for (int i = 0; i < res1.size(); i++)
			for (int j = 0; j < res2.size(); j++) {
				if (res1.elementAt(i).units.elementAt(
						(CatalogManager.getAttriOffest(tableName1,
								attributeName1))).equals(
						res2.elementAt(j).units.elementAt((CatalogManager
								.getAttriOffest(tableName2, attributeName2))))) {
					// construct tuple and insert
					tuple T = new tuple();
					for (int k = 0; k < CatalogManager
							.getTableAttriNum(tableName1); k++) {
						T.units.addElement(res1.elementAt(i).units.elementAt(k));
					}
					for (int k = 0; k < CatalogManager
							.getTableAttriNum(tableName2); k++) {
						T.units.addElement(res2.elementAt(j).units.elementAt(k));
					}
					res.add(T);
				}
			}
		return res;

	}

	public static final int SIZEINT = 4;// 单位都是byte
	public static final int SIZEFLOAT = 4;
	public static final int SIZEBOOLEAN = 1;// 注意是1byte不是bit
	public static final int SIZECHAR = 2;
}

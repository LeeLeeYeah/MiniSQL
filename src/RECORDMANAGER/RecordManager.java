//
//                       _oo0oo_
//                      o8888888o
//                      88" . "88
//                      (| -_- |)
//                      0\  =  /0
//                    ___/`��'\___
//                  .' \\|     |// '.
//                 / \\|||  :  |||// \
//                / _||||| -:- |||||- \
//               |   | \\\  -  /// |   |
//               | \_|  ''\��/''  |_/ |
//               \  .-\__  '-'  ___/-. /
//             ___'. .'  /��.��\  `. .'___
//          ."" '<  `.___\_<|>_/___.' >' "".
//         | | :  `- \`.;`\ _ /`;.`/ - ` : | |
//         \  \ `_.   \_ __\ /__ _/   .-` /  /
//     =====`-.____`.___ \_____/___.-`___.-'=====
//                       `=��='
//
//
//     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//                      ����һ��ƽ��
package RECORDMANAGER;

import java.util.Comparator;
import java.util.Vector;

import BUFFERMANAGER.*;
import CATALOGMANAGER.*;
import FILEMANAGER.FileManager;
import IndexManager.*;


//����������������֯���еļ�¼��ÿ������δ洢
public class RecordManager {
	static final char EMPTY = 0;

	
//	�������������������Ͷ��tupleOffsets�����ɵ�vector�����ض�Ӧ���tuple�����ɵ�vector���������tupleOffset������Ϊ�ջ������ѱ�ɾ�������Ӧ�����Ϊnull��
//	ʵ��ԭ���ȸ���tupleOffset��ÿ����¼�ĳ��ȣ��������Ӧ��¼���ļ���Ӧ�Ŀ��blockoffset�Լ������ֽ�ƫ��byteoffset���Ӷ�Ӧ���ж������ݣ��ٸ���ÿ��attribute�����ͽ���ת�����������tuple�С�
	public static Vector<tuple> getTuple(String tablename,
			Vector<Integer> tupleOffsets) {
		final int tinb = Block.BLOCKSIZE
				/ (SIZEINT + CatalogManager.getTupleLength(tablename));// ��¼һ��block���м���tuple����ÿ��tupleǰ���һ��int�Դ���ָ�롣
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
			// ����һ��tuple
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

//	�������������������Ͷ��tupleOffsets�����ɵ�vector�����ض�Ӧ���tuple�����ɵ�vector���������tupleOffset������Ϊ�ջ������ѱ�ɾ�������Ӧ�����Ϊnull��
//	ʵ��ԭ���ȸ���tupleOffset��ÿ����¼�ĳ��ȣ��������Ӧ��¼���ļ���Ӧ�Ŀ��blockoffset�Լ������ֽ�ƫ��byteoffset���Ӷ�Ӧ���ж������ݣ��ٸ���ÿ��attribute�����ͽ���ת�����������tuple�С�
	public static tuple getTuple(String tablename, int tupleOffset) {
		final int tinb = Block.BLOCKSIZE
				/ (SIZEINT + CatalogManager.getTupleLength(tablename));// ��¼һ��block���м���tuple����ÿ��tupleǰ���һ��int�Դ���ָ�롣
		int blockoffset = tupleOffset / tinb;
		Block block = BufferManager.getBlock(tablename, blockoffset);
		int byteoffset = (SIZEINT + CatalogManager.getTupleLength(tablename))
				* (tupleOffset % tinb);
		if (block.readInt(byteoffset) >= 0)
			return null;
		byteoffset += 4;
		tuple T = new tuple();
		// ����һ��tuple
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

//	���������������������������ļ�����ʼ����ͷ��
//	ʵ��ԭ������FileManager�����ļ�������bufferManager�ڱ�ͷָ�봦д0��
	public static boolean createTable(String tableName) {
		if (FileManager.findFile(tableName) == true)
			return false;
		// ������ͷ
		FileManager.creatFile(tableName);
		Block block = BufferManager.getBlock(tableName, 0);
		block.writeInt(0, 0);
		return true;
	}

//	��������������������ɾ�����ļ���
//	ʵ��ԭ������FileManagerɾ���ļ�������bufferManager�Ѹñ���������������
	public static boolean dropTable(String tableName) {
		if (FileManager.findFile(tableName) == false)
			return false;
		BufferManager.dropblocks(tableName);
		FileManager.dropFile(tableName);
		return true;
	}

//	����������������������һ��tuple��������У�������������λ�õ�tupleOffset��
//	ʵ��ԭ���ȴӱ�ͷ��ѯFreeList,����б�ɾ�����������Ŀ�λ��������ͷָ��Ŀ�λ�����ñ�ͷָ����һ����λ�����FreeList��û�п��࣬��ͨ��Catalog Manager��ñ�ǰ��tuple�������Դ˼������ĩβ�Ŀ��blockOffset���ֽ�ƫ��byteOffset�������Ӧλ�á�
	public static int insert(String tablename, tuple Tuple) {
		final int tinb = Block.BLOCKSIZE
				/ (SIZEINT + CatalogManager.getTupleLength(tablename));// ��¼һ��block���м���tuple����ÿ��tupleǰ���һ��int�Դ���ָ�롣

		// �ӱ�ͷ��ѯ�Ƿ��б�ɾ�������µĿ�λ
		Block block1 = BufferManager.getBlock(tablename, 0);
		int tupleoffset = block1.readInt(0);
		Block block2 = null;
		if (tupleoffset > 0) {
			// ָ����һ����λ��
			block2 = BufferManager.getBlock(tablename, tupleoffset / tinb);
			int nexttupleoffset = block2.readInt((CatalogManager
					.getTupleLength(tablename) + SIZEINT)
					* (tupleoffset % tinb));
			block1.writeInt(0, nexttupleoffset);
		} else {
			tupleoffset = 1 + CatalogManager.getTupleNum(tablename);
			block2 = BufferManager.getBlock(tablename, tupleoffset / tinb);
		}

		// ��������
		int byteoffset = (SIZEINT + CatalogManager.getTupleLength(tablename))
				* (tupleoffset % tinb);
		block2.writeInt(byteoffset, -1);// ��-1ָʾ����¼��Ч
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

	
//	����������������selectʱ��projection��������������Ҫѡ�����������ƣ�����select�������صĽ�������ؾ�projection��Ľ����
//	ʵ��ԭ������Ҫѡ�����������ƣ���tuple��û�б�ѡ�������ֵɾȥ��
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

//	����������������ͨselect��䡣����������ж�������ѡ�����������ļ�¼�洢��Vector<tuple>�в�����
//	ʵ��ԭ���Ա��е���Ч��¼�������������������¼��tuple T��ʹ��condition.calc(tuple T)ȥ�ж�ÿ����¼�Ƿ�������������������Vector<tuple>�У�������󷵻ؽ����
	public static Vector<tuple> select(String tablename, conditionNode condition) {
		final int tinb = Block.BLOCKSIZE
				/ (SIZEINT + CatalogManager.getTupleLength(tablename));// ��¼һ��block���м���tuple����ÿ��tupleǰ���һ��int�Դ���ָ�롣

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
			// ����һ��tuple
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

//	�������������ڴ���������order��select��䡣����������ж��������������������������������������ѡ�����������ļ�¼�洢��Vector<tuple>�У�����󷵻�
//	ʵ��ԭ���Ա��е���Ч��¼�������������������¼��tuple T��ʹ��condition.calc(tuple T)ȥ�ж�ÿ����¼�Ƿ�������������������Vector<tuple>�У�������󣬵���vector�е�sort�����������򣬷��ؽ����
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

//	�������������ڴ���������order��select��䣬ͨ��ָ��Ҫ�Ƚ��������������бȽϡ�
//	ʵ��ԭ��ͨ����������ȡ����ֵ���бȽϡ�
	static class MyCompare implements Comparator<tuple> // ʵ��Comparator�������Լ��ıȽϷ���
	{
		public int compare(tuple t1, tuple t2) {
			String num1 = t1.units.elementAt(comparePara);
			String num2 = t2.units.elementAt(comparePara);
			if (compareParaType.equals("int"))
				if (compareParaInc)// ��ȷ���᲻��д���ˣ���debug��ʱ�򿴿�@@@@@@@@@@@@@@@@
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
			else if (compareParaInc)// ��ȷ���᲻��д���ˣ���debug��ʱ�򿴿�@@@@@@@@@@@@@@@@
				return num1.compareTo(num2);
			else
				return num2.compareTo(num1);
		}
	}

//	��������������delete��䡣����������ж�������ɾ�����������ļ�¼�����ر�ɾ���ļ�¼��
//	ʵ��ԭ���Ա��е���Ч��¼�������������������¼��tuple T��ʹ��condition.calc(tuple T)ȥ�ж�ÿ����¼�Ƿ����������������Ѹü�¼�ı��Ϊ��Ϊ��ɾ����
	public static int delete(String tablename, conditionNode condition) {
		final int tinb = Block.BLOCKSIZE
				/ (SIZEINT + CatalogManager.getTupleLength(tablename));// ��¼һ��block���м���tuple����ÿ��tupleǰ���һ��int�Դ���ָ�롣

		Block block1 = BufferManager.getBlock(tablename, 0);
		block1.fix();// �ѵ�һ����fix�ڻ�����
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
			// ����һ��tuple
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

	
//	��������������select * join from A.a=B.bָ�ָ�����������Ӧ���Խ���join�����ض�Ӧ�����
//	ʵ��ԭ����select��������������е����м�¼��������ѭ����������ƥ�䣬���ָ��������ֵ�����������С�
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

	public static final int SIZEINT = 4;// ��λ����byte
	public static final int SIZEFLOAT = 4;
	public static final int SIZEBOOLEAN = 1;// ע����1byte����bit
	public static final int SIZECHAR = 2;
}

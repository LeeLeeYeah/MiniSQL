import java.io.IOException;
import java.util.Vector;

import lexer.Comparison;
import BUFFERMANAGER.BufferManager;
import CATALOGMANAGER.*;
import IndexManager.IndexManager;
import RECORDMANAGER.*;

/*
 * ���ʣ�1select��delete���صĽ����record managerֱ�����������interpreter���  �����interpreter���  �����η���
 *      2insert ����unique���� ��δ����ݿ����ҳ��Ƿ�����ͬvalue������ ͨ��record manager?
 *      
 */
public class API {
	/**
	 * ��ʼ��API
	 * 
	 * @throws IOException
	 */


	public static void close() throws IOException {
		CatalogManager.storeCatalog();
		BufferManager.close();
	}
	/**
	 * �ر�API
	 * 
	 * @throws IOException
	 */
	public static void Initialize() throws IOException {
		BufferManager.initialize();
		CatalogManager.InitialCatalog();
	}

	public static void showCatalog() {
		CatalogManager.showCatalog();
	}

	public static void showTableCatalog() {
		CatalogManager.showTableCatalog();
	}

	public static void showIndexCatalog() {
		CatalogManager.showIndexCatalog();
	}

	/**
	 * ������
	 * 
	 * @param newTable
	 * @return
	 */
	public static boolean createTable(String tableName, table newTable) {
		if (RecordManager.createTable(tableName)
				&& CatalogManager.createTable(newTable)){
		index newIndex = new index(tableName+"_prikey",tableName, CatalogManager.getPrimaryKey(tableName));
		IndexManager.createIndex(newIndex);
		CatalogManager.createIndex(newIndex);
		return true;
		}
		else
			return false;
	}

	/**
	 * ɾ����
	 * 
	 * @param tableName
	 * @return
	 */
	public static boolean dropTable(String tableName) {
		for(int i=0;i<CatalogManager.getTableAttriNum(tableName);i++){
			String indexName = CatalogManager.getIndexName(tableName,CatalogManager.getAttriName(tableName, i));
			if(indexName!=null)
				IndexManager.dropIndex(indexName);
		}
		if (RecordManager.dropTable(tableName)
				&& CatalogManager.dropTable(tableName))
			;
		return true;
	}

	/**
	 * ��������
	 * 
	 * @param newIndex
	 * @return
	 */
	public static boolean createIndex(index newIndex) {
		boolean t = IndexManager.createIndex(newIndex);
		return t & CatalogManager.createIndex(newIndex);
	}

	/**
	 * ɾ������
	 * 
	 * @param indexName
	 * @return
	 */
	public static boolean dropIndex(String indexName) {
		boolean t = IndexManager.dropIndex(indexName);
		return t & CatalogManager.dropIndex(indexName);
	}


	/**
	 * �����¼
	 * 
	 * @param attributes
	 * @param attriName
	 * @return
	 */
	public static boolean insertTuples(String tableName, tuple theTuple) {

		int tupleoffset = RecordManager.insert(tableName, theTuple);
		//��ȡindex�б�
		int n = CatalogManager.getTableAttriNum(tableName);
		try{
			for(int i=0;i<n;i++){
				String attriName = CatalogManager.getAttriName(tableName, i);
				String indexName = CatalogManager.getIndexName(tableName,attriName);
				if(indexName==null)
					continue;
				index indexInfo = CatalogManager.getIndex(indexName);
				String key = theTuple.units.elementAt(CatalogManager.getAttriOffest(tableName, indexInfo.attriName));
				IndexManager.insertKey(indexInfo, key, 0, tupleoffset);
				CatalogManager.updateIndexTable(indexInfo.indexName, indexInfo);
			}
			CatalogManager.addTupleNum(tableName);
		}catch(Exception e){
			System.err.println(e);
		}
		return true;
	}

	/**
	 * ɾ����¼
	 * 
	 * @param tableName
	 * @param conditionNodes
	 * @return
	 */
	public static int deleteTuples(String tableName,
			conditionNode conditionNodes) {
		int deleteNum = RecordManager.delete(tableName, conditionNodes);
		CatalogManager.deleteTupleNum(tableName, deleteNum);
		return deleteNum;
	}

	/**
	 * ��ѯ
	 * 
	 * @param tableName
	 * @param conditionNodes
	 * @return
	 */
	public static Vector<tuple> selectTuples(String tableName,
			Vector<String> attriNames, conditionNode conditionNodes) {
		Vector<tuple> res = new Vector<tuple>(0);
		if ( conditionNodes!=null && conditionNodes.left == null && conditionNodes.right == null
				&& conditionNodes.op == Comparison.eq && CatalogManager.getIndexName(tableName,
						conditionNodes.attriName)!= null) {
				try {
					Vector<Integer> targets = IndexManager.searchRange(
							CatalogManager.getIndex(CatalogManager.getIndexName(tableName,
									conditionNodes.attriName)),
							conditionNodes.value,
							conditionNodes.value);
					if(targets != null){
						res = RecordManager.getTuple(tableName, targets);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
		} else
			res = RecordManager.select(tableName, conditionNodes);
		if (attriNames != null)
			return RecordManager.project(res, tableName, attriNames);
		else
			return res;
	}

	public static Vector<tuple> selectTuples(String tableName,
			Vector<String> attriNames, conditionNode conditionNodes,
			String orderAttri, boolean ins) {
		Vector<tuple> res = RecordManager.select(tableName, conditionNodes,
				orderAttri, ins);
		if (attriNames != null)
			return RecordManager.project(res, tableName, attriNames);
		else
			return res;
	}
	public static Vector<tuple> join(String tableName1,String attributeName1,String tableName2,String attributeName2){
		return RecordManager.join(tableName1, attributeName1, tableName2, attributeName2);
	}
}

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

import CATALOGMANAGER.CatalogManager;
import lexer.Comparison;

//�����������Զ������ṹ��¼������sql����е�where������䡣�ýṹ֧����������г���and, or�Լ����ţ�֧�ֱ��������볣�����бȽϣ�Ҳ֧��ͬһ������������ͬ����֮������ݽ��бȽϡ�����ʱ����һ��tuple,����true��ʾ��������,false��ʾ������������
//ʵ��ԭ����sql����������䰴��ִ�����ȼ���������������ȼ�Խ�ߵ����Խ�󡣼���ʱ�Ӹ��ڵ㿪ʼ�ݹ�ؼ���������
public class conditionNode {

	String tablename;//�����������ڷ�Ҷ�ڵ���Ϊ�ա���Ҷ�ڵ��б�ʾ���������漰�����������ĸ���
	public String attriName;//�����������ڷ�Ҷ�ڵ���Ϊ�ա���Ҷ�ڵ��б�ʾ���������漰�����Ե����֡�
	String tablename2;//�����������ڷ�Ҷ�ڵ���Ϊ�ա���Ҷ�ڵ��б�ʾ������Ҳ��漰�����������ĸ������������Ҳ�Ϊ����ʱ��ֵΪ�ա�
	String attriName2;//�����������ڷ�Ҷ�ڵ���Ϊ�ա���Ҷ�ڵ��б�ʾ������Ҳ��漰�����Ե����֡����������Ҳ�Ϊ����ʱ��ֵΪ�ա�
	public String conjunction;//������������Ҷ�ڵ���Ϊ�ա���ʾ�������֮����and����or���������ӡ�
	public Comparison op;//�����������ڷ�Ҷ�ڵ�Ϊ�ա���Ҷ�ڵ��б�ʾ�������
	public String value;//�����������ڷ�Ҷ�ڵ���Ϊ�ա���Ҷ�ڵ������������Ҳ�Ϊ����ʱ����¼�ڸ�ֵ�С����������Ҳ��漰�����������ݶ����ǳ���ʱ��ֵΪ�ա�		
	public conditionNode left;//�����
	public conditionNode right;//�Ҷ���
	boolean constantFlag;//��������������Ǻͳ����Ƚ���Ϊtrue������Ǻ���һ��attribute�Ƚ�����false
		
	
//	�������������ڰ�Ҷ�ڵ�תΪ���ַ�����
	public String toString(){
		return attriName+" "+op+" "+value;
	}
	
//	�������������ڹ��������볣���Ƚϵ�Ҷ�ڵ�
//	ʵ��ԭ������������и�ֵ
	public conditionNode(String attriName, Comparison op, String value,boolean constantFlag) {
		this.conjunction="";
		this.attriName = attriName;
		this.op = op;
		this.left=null;
		this.right=null;
		this.constantFlag=constantFlag;
		if(constantFlag){
			this.value = value;
		}
		else{
			this.attriName2=value;
		}
	}

	public conditionNode(String attriName, String op, String value) {//����unique key�ļ�����
		this.attriName = attriName;
		this.conjunction="";
		this.op = Comparison.parseCompar(op);		
		this.left=null;
		this.right=null;		
		this.constantFlag=true;
		this.value = value;

	}

//	�������������ڹ�������������֮��Ƚϵ�Ҷ�ڵ�
//	ʵ��ԭ������������и�ֵ
	public conditionNode(String conjunction) {
		this.attriName = "";
		this.op = null;
		this.value = "";
		this.conjunction=conjunction;
	}
	
//	�������������ڰ�һ�����ڵ����ӵ�����Ҷ�ڵ�
//	ʵ��ԭ������������и�ֵ
	public conditionNode linkChildNode( conditionNode l, conditionNode r) {
		this.left=l;
		this.right=r;
		return this;
	}
	
//	��������������һ��Tuple���ж�������¼�Ƿ��������������
//	ʵ��ԭ���Ӹ��ڵ㿪ʼ���ݹ�ؽ��м��㡣�����㷨������ġ�
	public boolean calc(String tablename, tuple T) {
		if (conjunction.equals("and"))
			return (left.calc(tablename, T) & right.calc(tablename, T));
		else if (conjunction.equals("or"))
			return (left.calc(tablename, T) | right.calc(tablename, T));
		else {// Ҷ�ڵ�
			if (op == Comparison.eq) {
				if (CatalogManager.getType(tablename, attriName).equals("int")) {
					int num1 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					int num2;
					if (constantFlag)
						num2 = Integer.parseInt(value);
					else 
						num2 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2))); 
					if (num1 != num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName)
						.equals("float")) {					
					float num1 = Float.parseFloat(T.units
							.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					float num2 ;
					if (constantFlag)
						num2 = Float.parseFloat(value);
					else 
						num2 = Float.parseFloat(T.units
								.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 != num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName).equals("char")) {
					String num1 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName));
					String num2;
					if (constantFlag)
						num2 = value;
					else
						num2 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2));
					if (!num1.equals(num2))
						return false;
				}
			} else if (op == Comparison.ne) {
				if (CatalogManager.getType(tablename, attriName).equals("int")) {
					int num1 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					int num2;
					if (constantFlag)
						num2 = Integer.parseInt(value);
					else 
						num2 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 == num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName)
						.equals("float")) {
					float num1 = Float.parseFloat(T.units
							.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					float num2 ;
					if (constantFlag)
						num2 = Float.parseFloat(value);
					else 
						num2 = Float.parseFloat(T.units
								.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 == num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName).equals("char")) {
					String num1 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName));
					String num2;
					if (constantFlag)
						num2 = value;
					else
						num2 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2));
					if (num1.equals(num2))
						return false;
				}
			} else if (op == Comparison.lt) {
				if (CatalogManager.getType(tablename, attriName).equals("int")) {
					int num1 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					int num2;
					if (constantFlag)
						num2 = Integer.parseInt(value);
					else 
						num2 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 >= num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName)
						.equals("float")) {
					float num1 = Float.parseFloat(T.units
							.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					float num2 ;
					if (constantFlag)
						num2 = Float.parseFloat(value);
					else 
						num2 = Float.parseFloat(T.units
								.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 >= num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName).equals("char")) {
					String num1 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName));
					String num2;
					if (constantFlag)
						num2 = value;
					else
						num2 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2));
					if (num1.compareTo(num2) >= 0)
						return false;
				}
			} else if (op == Comparison.le) {
				if (CatalogManager.getType(tablename, attriName).equals("int")) {
					int num1 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					int num2;
					if (constantFlag)
						num2 = Integer.parseInt(value);
					else 
						num2 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 > num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName)
						.equals("float")) {
					float num1 = Float.parseFloat(T.units
							.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					float num2 ;
					if (constantFlag)
						num2 = Float.parseFloat(value);
					else 
						num2 = Float.parseFloat(T.units
								.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 > num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName).equals("char")) {
					String num1 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName));
					String num2;
					if (constantFlag)
						num2 = value;
					else
						num2 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2));
					if (num1.compareTo(num2) > 0)
						return false;
				}
			} else if (op == Comparison.gt) {
				if (CatalogManager.getType(tablename, attriName).equals("int")) {
					int num1 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					int num2;
					if (constantFlag)
						num2 = Integer.parseInt(value);
					else 
						num2 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 <= num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName)
						.equals("float")) {
					float num1 = Float.parseFloat(T.units
							.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					float num2 ;
					if (constantFlag)
						num2 = Float.parseFloat(value);
					else 
						num2 = Float.parseFloat(T.units
								.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 <= num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName).equals("char")) {
					String num1 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName));
					String num2;
					if (constantFlag)
						num2 = value;
					else
						num2 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2));
					if (num1.compareTo(num2) <= 0)
						return false;
				}
			} else if (op == Comparison.ge) {
				if (CatalogManager.getType(tablename, attriName).equals("int")) {
					int num1 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					int num2;
					if (constantFlag)
						num2 = Integer.parseInt(value);
					else 
						num2 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 < num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName)
						.equals("float")) {
					float num1 = Float.parseFloat(T.units
							.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					float num2 ;
					if (constantFlag)
						num2 = Float.parseFloat(value);
					else 
						num2 = Float.parseFloat(T.units
								.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 < num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName).equals("char")) {
					String num1 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName));
					String num2;
					if (constantFlag)
						num2 = value;
					else
						num2 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2));
					if (num1.compareTo(num2) < 0)
						return false;
				}
			}
			return true;
		}

	}
	
}

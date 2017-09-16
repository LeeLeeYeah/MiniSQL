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

import CATALOGMANAGER.CatalogManager;
import lexer.Comparison;

//功能描述：以二叉树结构记录并计算sql语句中的where条件语句。该结构支持条件语句中出现and, or以及括号，支持表中数据与常数进行比较，也支持同一个表中两个不同属性之间的数据进行比较。计算时输入一条tuple,返回true表示符合条件,false表示不符合条件。
//实现原理：将sql语句的条件语句按照执行优先级存入二叉树，优先级越高的深度越大。计算时从根节点开始递归地计算出结果。
public class conditionNode {

	String tablename;//功能描述：在非叶节点中为空。在叶节点中表示运算符左侧涉及的属性属于哪个表。
	public String attriName;//功能描述：在非叶节点中为空。在叶节点中表示运算符左侧涉及的属性的名字。
	String tablename2;//功能描述：在非叶节点中为空。在叶节点中表示运算符右侧涉及的属性属于哪个表。如果运算符右侧为常数时该值为空。
	String attriName2;//功能描述：在非叶节点中为空。在叶节点中表示运算符右侧涉及的属性的名字。如果运算符右侧为常数时该值为空。
	public String conjunction;//功能描述：在叶节点中为空。表示多个条件之间用and还是or来进行连接。
	public Comparison op;//功能描述：在非叶节点为空。在叶节点中表示运算符。
	public String value;//功能描述：在非叶节点中为空。在叶节点中如果运算符右侧为常数时，记录在该值中。如果运算符右侧涉及的是属性数据而不是常数时该值为空。		
	public conditionNode left;//左儿子
	public conditionNode right;//右儿子
	boolean constantFlag;//功能描述：如果是和常数比较则为true，如果是和另一个attribute比较则置false
		
	
//	功能描述：用于把叶节点转为字字符串。
	public String toString(){
		return attriName+" "+op+" "+value;
	}
	
//	功能描述：用于构造属性与常量比较的叶节点
//	实现原理：根据输入进行赋值
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

	public conditionNode(String attriName, String op, String value) {//用于unique key的简单条件
		this.attriName = attriName;
		this.conjunction="";
		this.op = Comparison.parseCompar(op);		
		this.left=null;
		this.right=null;		
		this.constantFlag=true;
		this.value = value;

	}

//	功能描述：用于构造属性与属性之间比较的叶节点
//	实现原理：根据输入进行赋值
	public conditionNode(String conjunction) {
		this.attriName = "";
		this.op = null;
		this.value = "";
		this.conjunction=conjunction;
	}
	
//	功能描述：用于把一个父节点链接到两个叶节点
//	实现原理：根据输入进行赋值
	public conditionNode linkChildNode( conditionNode l, conditionNode r) {
		this.left=l;
		this.right=r;
		return this;
	}
	
//	功能描述：输入一个Tuple，判断这条记录是否满足这个条件。
//	实现原理：从根节点开始，递归地进行计算。具体算法详见后文。
	public boolean calc(String tablename, tuple T) {
		if (conjunction.equals("and"))
			return (left.calc(tablename, T) & right.calc(tablename, T));
		else if (conjunction.equals("or"))
			return (left.calc(tablename, T) | right.calc(tablename, T));
		else {// 叶节点
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

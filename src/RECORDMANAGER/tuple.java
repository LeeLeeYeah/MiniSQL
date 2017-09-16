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


import java.util.Vector;
//功能描述：用于存储Table中的一条记录
//实现原理：用一个String的Vector以字符串格式一个一个地存储每个属性对应的值。
public class tuple {
	public Vector<String> units;
	public tuple(Vector<String> units){
		this.units=units;
	}
	public tuple(){units = new Vector<String>();}
	public String getString(){
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<units.size();i++){
			sb.append("\t"+units.get(i));
		}
		return sb.toString();
	}
}

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
package FILEMANAGER;

import java.io.File;

public class FileManager {
	
//	功能描述：给定文件名，判断文件是否已存在
//	实现原理：调用File.exist函数
	public static boolean findFile(String filename) {
		File file = new File(filename);
		if (!file.exists())
			return false;
		return true;
	}

//	功能描述：给定文件名，创建文件
//	实现原理：调用File.createNewFile()
	public static void creatFile(String filename) {
		try {
			File myFile = new File(filename);
			// 判断文件是否存在，如果不存在则调用createNewFile()方法创建新目录，否则跳至异常处理代码
			if (!myFile.exists())
				myFile.createNewFile();
			else
				// 如果不存在则扔出异常
				throw new Exception("The new file already exists!");
		} catch (Exception ex) {
			System.out.println("无法创建新文件！");
			ex.printStackTrace();
		}
	}
	
//	功能描述：给定文件名，删除文件
//	实现原理：调用File.delete()
	public static void dropFile(String filename) {
		File f=new File(filename);
		if(f.exists())f.delete();		
	}
}

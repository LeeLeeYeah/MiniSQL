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
package FILEMANAGER;

import java.io.File;

public class FileManager {
	
//	���������������ļ������ж��ļ��Ƿ��Ѵ���
//	ʵ��ԭ������File.exist����
	public static boolean findFile(String filename) {
		File file = new File(filename);
		if (!file.exists())
			return false;
		return true;
	}

//	���������������ļ����������ļ�
//	ʵ��ԭ������File.createNewFile()
	public static void creatFile(String filename) {
		try {
			File myFile = new File(filename);
			// �ж��ļ��Ƿ���ڣ���������������createNewFile()����������Ŀ¼�����������쳣�������
			if (!myFile.exists())
				myFile.createNewFile();
			else
				// ������������ӳ��쳣
				throw new Exception("The new file already exists!");
		} catch (Exception ex) {
			System.out.println("�޷��������ļ���");
			ex.printStackTrace();
		}
	}
	
//	���������������ļ�����ɾ���ļ�
//	ʵ��ԭ������File.delete()
	public static void dropFile(String filename) {
		File f=new File(filename);
		if(f.exists())f.delete();		
	}
}

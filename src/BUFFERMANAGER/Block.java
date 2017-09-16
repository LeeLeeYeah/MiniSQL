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
package BUFFERMANAGER;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Block {
	public static final int BLOCKSIZE = 4096;
	static final boolean EMPTY = true;

	String filename="";//��¼�������ļ���
	public int blockoffset=0;//��¼�������������ļ��ĵڼ�����
	boolean dirty = false;//�Ƿ�������
	public boolean valid = false;//��Чλ
	boolean fixed = false;//�Ƿ�����
	boolean reference_bit = false;//����λ������LRU�㷨

	public byte[] data = new byte[BLOCKSIZE];//��������4KB��С

//	�ⲿ�ӿڣ�
	
//	�������������ڶ���4KB����
//	ʵ��ԭ�������ڲ���Աdata��������λ��1
	public byte[] readData() {
		reference_bit = true;
		return data;
	}

//	�������������ڽ�inputdata[]�е�����д�����
//	ʵ��ԭ��д�����ݺ󣬽�����λ��1��dirtyλ��1�Ա��Ϊ������
	public boolean writeData(int byteoffset, byte inputdata[], int size) {
		// ����ƫ�ƣ�����СΪsize������data[]д��block
		// �����Խ��
		if (byteoffset + size >= 4096)
			return false;
		for (int i = 0; i < size; i++)
			data[byteoffset + i] = inputdata[i];
		dirty = true;
		reference_bit = true;
		return true;
	}

//	����������������ֱ���޸�data֮�󷢳��������źż�����λ�ź�
//	ʵ��ԭ��д�����ݺ󣬽�����λ��1��dirtyλ��1�Ա��Ϊ������
	public boolean writeData() {
		dirty = true;
		reference_bit = true;
		return true;
	}

//	�����������ѿ������ڻ�����
//	ʵ��ԭ��fixλ��1������
	public void fix() {
		fixed = true;
	}

//	�����������ѿ�ӻ���������
//	ʵ��ԭ��fixλ��0�Խ���
	public void unfix() {
		fixed = false;
	}

//	�����������ӿ��е�ָ��λ�ö���һ������
//	ʵ��ԭ���������ݺ󣬽�����λ��1
	public int readInt(int offset) {
		byte[] temp = new byte[4];
		temp[0] = data[offset + 0];
		temp[1] = data[offset + 1];
		temp[2] = data[offset + 2];
		temp[3] = data[offset + 3];
		ByteArrayInputStream bintput = new ByteArrayInputStream(temp);
		DataInputStream dintput = new DataInputStream(bintput);
		int res = 0;
		try {
			res = dintput.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reference_bit = true;
		return res;		
	}

//	�����������ӿ��е�ָ��λ��д��һ������
//	ʵ��ԭ��д�����ݺ󣬽�����λ��1��dirtyλ��1�Ա��Ϊ������
	public void writeInt(int offset, int num) {
		ByteArrayOutputStream boutput = new ByteArrayOutputStream();
		DataOutputStream doutput = new DataOutputStream(boutput);
		try {
			doutput.writeInt(num);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] temp = boutput.toByteArray();
		data[offset] = temp[0];
		data[offset + 1] = temp[1];
		data[offset + 2] = temp[2];
		data[offset + 3] = temp[3];
		dirty = true;
		reference_bit = true;
	}

//	�����������ӿ��е�ָ��λ�ö���һ��float
//	ʵ��ԭ���������ݺ󣬽�����λ��1
	public float readFloat(int offset) {
		byte[] temp = new byte[4];
		temp[0] = data[offset + 0];
		temp[1] = data[offset + 1];
		temp[2] = data[offset + 2];
		temp[3] = data[offset + 3];
		ByteArrayInputStream bintput = new ByteArrayInputStream(temp);
		DataInputStream dintput = new DataInputStream(bintput);
		float res = 0;
		try {
			res = dintput.readFloat();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reference_bit = true;
		return res;
	}

//	�����������ӿ��е�ָ��λ��д��һ��float
//	ʵ��ԭ��д�����ݺ󣬽�����λ��1��dirtyλ��1�Ա��Ϊ������
	public void writeFloat(int offset, float num) {
		ByteArrayOutputStream boutput = new ByteArrayOutputStream();
		DataOutputStream doutput = new DataOutputStream(boutput);
		try {
			doutput.writeFloat(num);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] temp = boutput.toByteArray();
		data[offset] = temp[0];
		data[offset + 1] = temp[1];
		data[offset + 2] = temp[2];
		data[offset + 3] = temp[3];
		dirty = true;
		reference_bit = true;
	}
	
//	�����������ӿ��е�ָ��λ�ö���һ������Ϊlength��String
//	ʵ��ԭ���������ݺ󣬽�����λ��1
	public String readString(int offset, int length) {//��length��ָ��attribute�ĳ����Ǽ����ֽڡ�
		byte[] buf = new byte[length];
		for (int i = 0; i < length; i++)
			buf[i] = data[offset++];
		reference_bit = true;
		String res = new String(buf);
		res=res.replaceAll("&", "");
		return res;
	}
	
	
	public String readString(int offset) {
		byte[] buf = new byte[4];
		for (int i = 0; i < 4; i++)
			buf[i] = data[offset++];
		reference_bit = true;
		String res = new String(buf);
		return res;
	}
	
//	�����������ӿ��е�ָ��λ��д��һ������Ϊlength��String
//	ʵ��ԭ��д�����ݺ󣬽�����λ��1��dirtyλ��1�Ա��Ϊ������
	public void writeString(int offset, String num, int length) {//��length��ָ����string��0���������ֽڣ�
		byte[] buf = num.getBytes();
		int j;
		for (j = 0; j < buf.length; j++) {
			data[offset] = buf[j];
			offset += 1;
		}
		for (; j < length; j++){
			data[offset] = '&';
			offset += 1;
		}			
		dirty = true;
		reference_bit = true;
	}


	public int recordNum = 0;
	public Block next = null;
	public Block previous = null;
	//�м�ڵ��ã����÷ֲ�ļ�ֵ������������
	public  void writeInternalKey(int pos,byte[] key,int offset) {
		writeData(pos,key,key.length);
		writeInt(pos+key.length,offset);
		dirty=true;
	}

	public byte[] getBytes(int pos,int length){
		byte[] b = new byte[length];
		for(int i =0;i<length;i++){
			b[i]=data[pos+i];
		}
		return b;
	}

	public  void setInternalKey(int pos,byte[] key,int offset) {
		writeData(pos,key,key.length);
		writeInt(pos+key.length,offset);
		dirty=true;
	}
	public  void setKeydata(int pos,byte[] insertKey,int blockOffset,int offset) {
		writeInt(pos,blockOffset);
		writeInt(pos+4,offset);	
		writeData(pos+8,insertKey, insertKey.length);
		dirty=true;
	} 
}

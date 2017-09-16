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
package BUFFERMANAGER;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Block {
	public static final int BLOCKSIZE = 4096;
	static final boolean EMPTY = true;

	String filename="";//记录块所属文件名
	public int blockoffset=0;//记录这个块属于这个文件的第几个块
	boolean dirty = false;//是否脏数据
	public boolean valid = false;//有效位
	boolean fixed = false;//是否被锁定
	boolean reference_bit = false;//引用位，用于LRU算法

	public byte[] data = new byte[BLOCKSIZE];//数据区，4KB大小

//	外部接口：
	
//	功能描述：用于读出4KB数据
//	实现原理：返回内部成员data，将引用位置1
	public byte[] readData() {
		reference_bit = true;
		return data;
	}

//	功能描述：用于将inputdata[]中的数据写入块中
//	实现原理：写入数据后，将引用位置1，dirty位置1以标记为脏数据
	public boolean writeData(int byteoffset, byte inputdata[], int size) {
		// 给定偏移，将大小为size的数据data[]写入block
		// 请避免越界
		if (byteoffset + size >= 4096)
			return false;
		for (int i = 0; i < size; i++)
			data[byteoffset + i] = inputdata[i];
		dirty = true;
		reference_bit = true;
		return true;
	}

//	功能描述：用于在直接修改data之后发出脏数据信号及引用位信号
//	实现原理：写入数据后，将引用位置1，dirty位置1以标记为脏数据
	public boolean writeData() {
		dirty = true;
		reference_bit = true;
		return true;
	}

//	功能描述：把块锁定在缓冲区
//	实现原理：fix位置1以锁定
	public void fix() {
		fixed = true;
	}

//	功能描述：把块从缓冲区解锁
//	实现原理：fix位置0以解锁
	public void unfix() {
		fixed = false;
	}

//	功能描述：从块中的指定位置读出一个整数
//	实现原理：读出数据后，将引用位置1
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

//	功能描述：从块中的指定位置写入一个整数
//	实现原理：写入数据后，将引用位置1，dirty位置1以标记为脏数据
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

//	功能描述：从块中的指定位置读出一个float
//	实现原理：读出数据后，将引用位置1
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

//	功能描述：从块中的指定位置写入一个float
//	实现原理：写入数据后，将引用位置1，dirty位置1以标记为脏数据
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
	
//	功能描述：从块中的指定位置读出一个长度为length的String
//	实现原理：读出数据后，将引用位置1
	public String readString(int offset, int length) {//用length来指定attribute的长度是几个字节。
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
	
//	功能描述：从块中的指定位置写入一个长度为length的String
//	实现原理：写入数据后，将引用位置1，dirty位置1以标记为脏数据
	public void writeString(int offset, String num, int length) {//用length来指定把string补0补到几个字节，
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
	//中间节点用：设置分层的键值，包括了它在
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

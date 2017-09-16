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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class BufferManager {
	static final int NUMOFBLOCKS = 20;
	static final int NOTEXIST = -1;

	private static Block[] blocks = new Block[NUMOFBLOCKS];// 用一个block类数组代表buffer,总共占用80k的空间
	private static int pointer = 0;//用于实现时钟算法，用块在buffer中的下标代替指针
	
//	功能描述：初始化。在使用Buffer Manager前调用一次
//	实现原理：为buffer申请内存空间
	public static void initialize() {
		for (int i = 0; i < NUMOFBLOCKS; i++)
			blocks[i] = new Block();
	}

//	功能描述：关闭Buffer Manager,在退出程序之前调用
//	实现原理：把Buffer中的脏数据写回文件
	public static void close() {
		for (int i = 0; i < NUMOFBLOCKS; i++) {
			if (blocks[i].valid==true)
				writeToDisk(i);
		}
	}
	
	
	public static void dropblocks(String filename){
		for (int i = 0; i < NUMOFBLOCKS; i++)
			if (blocks[i].filename.equals(filename))
				blocks[i].valid=false;												
	}

//	功能描述：给定文件名和块编号，返回一个block
//	实现原理：调用findBlock搜索这个块是否在buffer中，是则返回这个block。否则，调用getFreeBlockNum得到buffer中可用的一个块下标，把文件中的数据读入这个块，并返回这个块。
	public static Block getBlock(String filename, int blockoffset) {
		// 指定文件名和第几个block，返回一个BLOCK
		int num = findBlock(filename, blockoffset);
		if (num != NOTEXIST)
			return blocks[num];
		else {
			num = getFreeBlockNum();
			File file = new File(filename);
			if (!file.exists()) {
				blocks[num].blockoffset = blockoffset;
				blocks[num].filename = filename;
				for (int i = 0; i < Block.BLOCKSIZE; i++)
					blocks[num].data[i] = 0;
				return blocks[num];
			}
			readFromDisk(filename, blockoffset, num);
			return blocks[num];
		}
	}

//	功能描述：给定文件名和块编号，返回一个block在buffer中的下标，如果不在buffer中则返回-1
//	实现原理：对buffer中的所有块进行遍历搜索。
	private static int findBlock(String filename, int blockoffset) {
		for (int i = 0; i < NUMOFBLOCKS; i++)
			if (blocks[i].valid)
				if(blocks[i].filename.equals(filename))
					if(blocks[i].blockoffset == blockoffset) {
				return i;
			}
		return NOTEXIST;
	}

//	功能描述：返回一个可被替换出去的block的下标。过程中使用时钟算法进行选择，并且跳过被锁定在buffer中的块。
//	实现原理：将pointer指向buffer中下一个块。如果且fixed为1，将pointer指向下一个块并进入下一个循环。否则，如果指向的块reference_bit为1，则把reference_bit位置1；如果指向的块reference_bit为0，则把这个块写回文件，并返回pointer的值。	
	private static int getFreeBlockNum() {
		/* 基于Clock算法（一个近似LRU的算法），并排除掉被锁定的block，进行替换 */
		/* 如果所有的block都被锁定在buffer中，此处将出现死循环 */
		do {
			pointer = (pointer + 1) % NUMOFBLOCKS;
			if (blocks[pointer].reference_bit == true
					&& blocks[pointer].fixed == false)
				blocks[pointer].reference_bit = false;
			else if (blocks[pointer].reference_bit == false) {
				writeToDisk(pointer);
				return pointer;
			}
		} while (true);
	}

//	功能描述：给定文件名，块偏移，把数据从文件读取到buffer中下标为num的块中，并对标记位进行初始化（有效位、reference_bit置1，dirty、fixed位置0）
//	实现原理：通过RandomAccessFile执行文件读取
	private static boolean readFromDisk(String filename, int blockoffset,
			int num) {
		File file = null;
		RandomAccessFile raf = null;
		blocks[num].filename = filename;
		blocks[num].blockoffset = blockoffset;
		blocks[num].valid = true;
		blocks[num].reference_bit = true;
		blocks[num].dirty = false;
		blocks[num].fixed = false;
		for (int i = 0; i < Block.BLOCKSIZE; i++)
			blocks[num].data[i] = 0;
		try {
			file = new File(filename);
			raf = new RandomAccessFile(file, "rw");

			if (raf.length() >= blocks[num].blockoffset * Block.BLOCKSIZE
					+ Block.BLOCKSIZE) {
				raf.seek(blockoffset * Block.BLOCKSIZE);
				raf.read(blocks[num].data, 0, Block.BLOCKSIZE);
			} else
				for (int j = 0; j < Block.BLOCKSIZE; j++)
					blocks[num].data[j] = 0;
			raf.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (raf != null) {
					raf.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

//	功能描述：把buffer中下标为num的块写回到文件中，并把块的有效位置0
//	实现原理：如果dirty位为0，则不执行写操作，否则执行写操作。
	private static void writeToDisk(int num) {
		if (blocks[num].dirty == false) {
			blocks[num].valid = false;
			return;
		} else {
			File file = null;
			RandomAccessFile raf = null;
			try {
				file = new File(blocks[num].filename);
				raf = new RandomAccessFile(file, "rw");
				// if file doesn't exists, then create it
				if (!file.exists()) {
					file.createNewFile();
				}
				raf.seek(blocks[num].blockoffset * Block.BLOCKSIZE);
				raf.write(blocks[num].data);
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (raf != null) {
						raf.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			blocks[num].valid = false;
		}
	}
}

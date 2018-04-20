package com.meiren.blockchain;

import com.meiren.blockchain.common.util.BlockChainFileUtils;
import com.meiren.blockchain.common.util.HashUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jijiecong   （这里替换为自己的名字）
 * @ClassName: CommonTest
 * @Description: ${todo}
 * @date 2018/3/29 9:46
 */
public class CommonTest {

	volatile boolean a = false;

	@Test
	public void test() throws InterruptedException {
		new Thread(){
			public void run(){
				for (int i =0 ;i<60 ;i++){
//					System.out.println(Thread.currentThread().getId() + "before: a=" + a);
					if(i == 50){
						a = true;
					}
					System.out.println(Thread.currentThread().getId() + "after: a=" + a);
				}

			}
		}.start();
		new Thread(){
			public void run(){
				while (!a){
				}
				System.out.println(Thread.currentThread().getId() + "++++++++++++++++: a=" + a);
			}
		}.start();
		Thread.sleep(1000);
	}

	private List<CommonTest> list = new ArrayList<>();
	@Test
	public  void test2() throws InterruptedException {
		for(int i = 0; i < 10000; i++){
			CommonTest commonTest = new CommonTest();
			list.add(commonTest);
			Thread.sleep(50);
		}
	}

	/**
	 *
	 * @methodName 快速排序
	 * @param a
	 * @param left
	 * @param right
	 */
	public static void quickSort(int [] a, int left, int right) {
		int i, j, t, base;
		if (left > right)
			return;
		base = a[left]; // temp中存的就是基准数
		i = left;       // 设置两个哨兵
		j = right;
		while (i != j) {
			// 顺序很重要，要先从右边开始找
			while (a[j] >= base && i < j)
				j--;
			// 再找右边的
			while (a[i] <= base && i < j)
				i++;
			// 交换两个数在数组中的位置
			if (i < j) {
				t = a[i];
				a[i] = a[j];
				a[j] = t;
			}
		}
		// 最终将基准数归位
		a[left] = a[i];
		a[i] = base;

		quickSort(a, left, i - 1);// 继续处理左边的，这里是一个递归的过程
		quickSort(a, i + 1, right);// 继续处理右边的 ，这里是一个递归的过程
	}

	@Test
	public void test3(){
		int [] a = new int[]{2,5,3,4,6,8,1,3,2,4,2,6,12};
		quickSort(a, 0, a.length-1);
		for(int i : a){
			System.out.println(i);
		}
	}

	@Test
	public void writeToDisk(){

	}

	@Test
	public void readFromDisk(){
		System.out.println(BlockChainFileUtils.getFileSize("D:/blk21.dat"));
	}

	@Test
	public void test4(){
		byte[] result = HashUtils.toBytesAsLittleEndian("0000000000000000000000000000000000000000000000000000000000000000");
		System.out.println("end");
	}
}

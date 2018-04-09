package com.meiren.blockchain.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author jijc
 * @ClassName: CollaborativeFiltering
 * @Description: 协同过滤算法
 * @date 2018/1/3 16:40
 */
public class CollaborativeFiltering {

	/**
	 * 计算用户相似度-皮尔逊相关系数
	 * 皮尔逊相关系数
	 * p(x,y) = (Σxy-Σx*Σy/n)/Math.sqrt((Σx^2-(Σx)^2/n)(Σy^2-(Σy)^2/n))
	 * */
	public static double getUserSimilarByPerson(Map<String, Integer> pm1, Map<String, Integer> pm2) {
		int n = 0;// 数量n
		int sxy = 0;// Σxy=x1*y1+x2*y2+....xn*yn
		int sx = 0;// Σx=x1+x2+....xn
		int sy = 0;// Σy=y1+y2+...yn
		int sx2 = 0;// Σx^2=(x1)^2+(x2)^2+....(xn)^2
		int sy2 = 0;// Σy^2=(y1)^2+(y2)^2+....(yn)^2
		for (Entry<String, Integer> pme : pm1.entrySet()) {
			String key = pme.getKey();
			Integer x = pme.getValue();
			Integer y = pm2.get(key);
			if (x != null && y != null) {
				n++;
				sxy += x * y;
				sx += x;
				sy += y;
				sx2 += Math.pow(x, 2);
				sy2 += Math.pow(y, 2);
			}
		}
		// p=(Σxy-Σx*Σy/n)/Math.sqrt((Σx^2-(Σx)^2/n)(Σy^2-(Σy)^2/n));
		double sd = sxy - sx * sy / n;
		double sm = Math.sqrt((sx2 - Math.pow(sx, 2) / n) * (sy2 - Math.pow(sy, 2) / n));
		return Math.abs(sm == 0 ? 1 : sd / sm);
	}

	/**
	 * 计算用户相似度-余弦相似度
	 *  Cosine 相似度
	 * T(x,y) = Σxi*yi / Math.sqrt(Σxi^2) * Math.sqrt(Σyi^2)
	 *
	 * */
	public static double getUserSimilarByCosine(Map<String, Integer> pm1, Map<String, Integer> pm2) {
		int sxiyi = 0;// Σxiyi=x1*y1+x2*y2+....xn*yn
		int sxi2 = 0;// Σxi2=(x1)2+(x2)2+....(xn)2
		int syi2 = 0;// Σyi2=(y1)2+(y2)2+....(yn)2
		for (Entry<String, Integer> pme : pm1.entrySet()) {
			String key = pme.getKey();
			Integer x = pme.getValue();
			Integer y = pm2.get(key);
			if (x != null && y != null) {
				sxiyi += x * y;
				sxi2 += Math.pow(x, 2);
				syi2 += Math.pow(y, 2);
			}
		}
		// T(x,y) = Σxi*yi / Math.sqrt(Σxi^2) * Math.sqrt(Σyi^2);
		double sd2 = sxiyi;
		double sm2 = Math.sqrt(sxi2 ) * Math.sqrt(syi2);
		return Math.abs(sm2 == 0 ? 1 : sd2 / sm2);
	}

	//get recommendation results
	/**
	 * 获取推荐列表
	 * @param simUserObjMap 用户的行为集合 Map<用户唯一id, Map<物品id, 用户是否对物品产生过行为>>
	 * @param simUserSimMap 用户于其它用户间的相似度集合 Map<其它用户唯一id, 和其它用户相似度>
	 * @param limitMax 推荐限制数量
	 * @param scoreMin 推荐限制最低分
	 * */
	public static List getRecommend(Map<String, Map<String, Integer>> simUserObjMap,
			Map<String, Double> simUserSimMap, int limitMax, double scoreMin) {
		Map<String, Double> objScoreMap = new HashMap<String, Double>();
		for (Map.Entry<String, Map<String, Integer>> simUserEn : simUserObjMap.entrySet()) {
			String user = simUserEn.getKey();
			double sim = simUserSimMap.get(user);
			for (Map.Entry<String, Integer> simObjEn : simUserEn.getValue().entrySet()) {
				double objScore = sim * simObjEn.getValue();
				String objName = simObjEn.getKey();
				if (objScoreMap.get(objName) == null) {
					objScoreMap.put(objName, objScore);
				} else {
					double totalScore = objScoreMap.get(objName);
					objScoreMap.put(objName, totalScore + objScore);
				}
			}
		}
		List<Entry<String, Double>> enList = new ArrayList<Entry<String, Double>>(objScoreMap.entrySet());
		//优化取排名前limitMax位，且推荐分大于scoreMin算法
		//冒泡排前limitMax位，其余不进行排序，复杂度：最小n, 最大n^2
		for(int i=0; i< limitMax && i< enList.size(); i++){
			for (int j = enList.size() - 1; j> i; j--){
				if(enList.get(j).getValue() > enList.get(j-1).getValue()){
					Entry<String, Double> temp = enList.get(j);
					enList.set(j, enList.get(j-1));
					enList.set(j-1, temp);
				}
			}
		}
//		Collections.sort(enList, new Comparator<Map.Entry<String, Double>>() {
//			public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
//				Double a = o1.getValue() - o2.getValue();
//				if (a == 0) {
//					return 0;
//				} else if (a > 0) {
//					return -1;
//				} else {
//					return 1;
//				}
//			}
//		});
		List<Entry<String, Double>> resultList = new ArrayList<Entry<String, Double>>();
		for (int i=0; i< limitMax && i< enList.size(); i++){
			Map.Entry<String, Double> entry = enList.get(i);
			if(entry.getValue() > scoreMin){
				resultList.add(entry);
			}
		}
		return resultList;
	}

	public static void main(String[] args) {
		Map<String, Map<String, Integer>> userPerfMap = new HashMap<String, Map<String, Integer>>();
		Map<String, Integer> pref1 = new HashMap<String, Integer>();
		pref1.put("A", 3);
		pref1.put("B", 4);
		pref1.put("C", 3);
		pref1.put("D", 5);
		pref1.put("E", 1);
		pref1.put("F", 4);
		userPerfMap.put("p1", pref1);
		Map<String, Integer> pref2 = new HashMap<String, Integer>();
		pref2.put("A", 2);
		pref2.put("B", 4);
		pref2.put("C", 4);
		pref2.put("D", 5);
		pref2.put("E", 3);
		pref2.put("F", 2);
		userPerfMap.put("p2", pref2);
		Map<String, Integer> pref3 = new HashMap<String, Integer>();
		pref3.put("A", 3);
		pref3.put("B", 5);
		pref3.put("C", 4);
		pref3.put("D", 5);
		pref3.put("E", 2);
		pref3.put("F", 1);
		userPerfMap.put("p3", pref3);
		Map<String, Integer> pref4 = new HashMap<String, Integer>();
		pref4.put("A", 2);
		pref4.put("B", 2);
		pref4.put("C", 3);
		pref4.put("D", 4);
		pref4.put("E", 3);
		pref4.put("F", 2);
		userPerfMap.put("p4", pref4);
		Map<String, Integer> pref5 = new HashMap<String, Integer>();
		pref5.put("A", 4);
		pref5.put("B", 4);
		pref5.put("C", 4);
		pref5.put("D", 5);
		pref5.put("E", 1);
		pref5.put("F", 0);
		userPerfMap.put("p5", pref5);

		Map<String, Double> simUserSimMap = new HashMap<String, Double>();
		System.out.println("皮尔逊相关系数:");

		for (Entry<String, Map<String, Integer>> userPerfEn : userPerfMap.entrySet()) {
			String userName = userPerfEn.getKey();
			if (!"p5".equals(userName)) {
				double sim = getUserSimilarByPerson(pref5, userPerfEn.getValue());
				System.out.println("    p5与" + userName + "之间的相关系数:" + sim);
				simUserSimMap.put(userName, sim);
			}
		}

		Map<String, Map<String, Integer>> simUserObjMap = new HashMap<String, Map<String, Integer>>();
		Map<String, Integer> pobjMap1 = new HashMap<String, Integer>();
		pobjMap1.put("玩命速递", 3);
		pobjMap1.put("环太平洋", 4);
		pobjMap1.put("变形金刚", 3);
		simUserObjMap.put("p1", pobjMap1);
		Map<String, Integer> pobjMap2 = new HashMap<String, Integer>();
		pobjMap2.put("玩命速递", 5);
		pobjMap2.put("环太平洋", 1);
		pobjMap2.put("变形金刚", 2);
		simUserObjMap.put("p2", pobjMap2);
		Map<String, Integer> pobjMap3 = new HashMap<String, Integer>();
		pobjMap3.put("玩命速递", 2);
		pobjMap3.put("环太平洋", 5);
		pobjMap3.put("变形金刚", 5);
		simUserObjMap.put("p3", pobjMap3);
		List<Entry<String, Double>> resultList = getRecommend(simUserObjMap, simUserSimMap, 4, 7);
		System.out.println("推荐结果:");
		for (Entry<String, Double> entry: resultList){
			System.out.println(entry.getKey()+"===推荐分："+entry.getValue());
		}
	}

}

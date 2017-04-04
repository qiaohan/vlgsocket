package com.fanlunge;

public class Test {
	public static void main(String[] a){
		Dao dao = new Dao("114.215.78.135","testvlg","qh","qiaohan");
		String passwd = dao.getLogInfo("qiaohan","passwd");
		System.out.println("p:"+passwd);
	}
}

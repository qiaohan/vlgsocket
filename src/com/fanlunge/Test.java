package com.fanlunge;
import java.util.ArrayList;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.ConnectionConfiguration;

public class Test {
	public static void main(String[] a){
		//Dao dao = new Dao("114.215.78.135","testvlg","qh","qiaohan");
		//String passwd = dao.getLogInfo("qiaohan","passwd");
		//System.out.println("p:"+passwd);
		//ConnectionConfiguration conconfig = new XMPPTCPConnectionConfiguration("www.fanlunge.com",5222)
		
		//XMPPTCPConnection connection = new XMPPTCPConnection("admin","admin","www.fanlunge.com:5222");
		//AccountManager amgr = AccountManager.getInstance(connection);
		try{
			//connection.connect();
			//amgr.createAccount("test2", "111111");
			ConnectionConfiguration config = new ConnectionConfiguration("www.fanlunge.com",5222);
			Connection connection = new XMPPConnection(config);
			connection.connect(); 
			AccountManager amgr = connection.getAccountManager();
			amgr.createAccount("test2", "111111");
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("finished");
	}
}

package com.fanlunge;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Dao {
	private static String driver="com.mysql.jdbc.Driver";
	private String db_url;
	private String db = "qh_test";
	private String user="qh";
	private String password="qiaohan";     
	private Connection connection=null;
	private ResultSet rs=null;     
	public Dao(String ip_str,String dbname, String u, String passwd){
	        try {
	        	db_url = "jdbc:mysql://"+ip_str+":3306/";
	        	db = dbname;
	        	user = u;
	        	password = passwd;
	            Class.forName(driver);
	            connect();
	        } catch (ClassNotFoundException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	}
	
	public void connect() {
	        // TODO Auto-generated method stub
	        try {
	             
	            connection=DriverManager.getConnection(db_url+db, user, password);
	             
	            if(connection!=null){
	                System.out.println("connection success");
	            }else{
	                System.out.println("connection failed");
	            }
	             
	        } catch (SQLException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	}
	     
	public void close() {
	        // TODO Auto-generated method stub
	         
	            try {
	                if(rs!=null){
	                rs.close();
	                }
	            } catch (SQLException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }finally{
	                        if(connection!=null){
	                            try {
	                                connection.close();
	                            } catch (SQLException e) {
	                                // TODO Auto-generated catch block
	                                e.printStackTrace();
	                            }
	                        }
	            }                 
	}
	public ArrayList<String> getTablesName(){
		try{
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery("show tables");
			ArrayList<String> reslist = new ArrayList<String>();
			while(rs.next()){
				String ss = rs.getString(1);
				//System.out.println(ss);
				reslist.add(ss);
			}
			return reslist;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	public String getLogInfo(String u, String field){
		try{
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery("select "+field+" from vlgUserLogInfo where phonenum = '"+u+"'");
			//if(rs.em)
			rs.next();
			String ss = rs.getString(1);
			return ss;
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";
	}
	public String getProperty(String u, String p){
		try{
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery("select "+p+" from vlgUserProperty where uid = '"+u+"'");
			//if(rs.em)
			rs.next();
			String ss = rs.getString(1);
			return ss;
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";
	}
}

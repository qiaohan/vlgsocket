package com.fanlunge;
import javax.websocket.Session;
import java.util.ArrayList;
import org.jivesoftware.smack.*;
public class User {
	static Dao xmppdao = new Dao("114.215.78.135","testvlg","qh","qiaohan");
	static Dao gamedao = new Dao("114.215.78.135","testvlg","qh","qiaohan");
	//private static Logger logger = LogManager.getLogger(GameSocket.class); 
	static public boolean Register(String uname, String upass){
		String gameu = gamedao.getLogInfo(uname, "pass");
		String xmppu = xmppdao.getLogInfo(uname, "pass");
		if(gameu.equals("")||xmppu.equals(""))
			return false;
		
		return true;
	}
	private Session sess;
	private String uid;
	private UserProperty property;
	private int status;
	public Room room;
	public User(String username){
		status = 1;
		uid = username;
		sess = null;
	}
	public void reset(){
		status = 1;
	}
	public void bindsession(Session s){
		sess = s;
	}
	public void unbindsession(){
		sess = null;
	}
	public boolean passmatch(){
		return true;
	}
	public int getleadership(){return 20;}
	public int getspeaksec(){return 5;}
	public int getStatus(){return status;}
	public void setSession(Session s){sess = s;}
	public Session getSession(){return sess;}
	public String getName(){return uid;}
	/*
	public boolean step(String comd){
		ArrayList<String> tablelist = gamedao.gettablesname();
		if(tablelist!=null)
		{
			for(String ss:tablelist)
				logger.error(ss);
			return true;
		}
		return false;
	}
	*/
	public boolean isplaying(){
		if(status==6||status==7||status==8)
			return true;
		else
			return false;
	}

	public boolean EnterRoom(Room r){
		if(status != 1)
			return false;
		if(r.addplayer(this)){
			status = 2;
			room = r;
			room.broadcast_players();
			return true;
		}
		else
			return false;
	}
	public boolean QuitRoom(){
		if(status != 2||room==null)
			return false;
		room.RemoveUser(this);
		room.broadcast_players();
		room = null;
		status = 1;
		return true;
	}
	public void ForceQuitRoom(){
		if(room==null)
			return;
		//room.RemoveUser(this);
		room.someonequit(this);
		room = null;
		status = 0;
	}
	public boolean shangmai(){
		if(status != 2 && status !=4)
			return false;
		if(room==null)
			return false;
		if(!room.shangmai(this))
			return false;
		if(status ==2) //not shangzuo
			status = 3;
		else 
			status = 5;
		room.broadcast_players();
		return true;
	}
	public boolean xiamai(){
		if(status != 3 && status != 5)
			return false;
		if(room==null)
			return false;
		if(status == 3) //not shangzuo
			status = 2;
		else 
			status = 4;
		if(!room.xiamai(this))
			return false;
		
		room.broadcast_players();
		return true;
	}
	public boolean speakover(){
		if(status!=3&&status!=5&&status!=7){
			return false;
		}
		if(room==null)
			return false;
		if(status == 3)
			status = 2;
		else if(status == 5)
			status = 4;
		else if(status == 7)
			status = 6;
		return true;
	}
	public boolean shangzuo(){
		if(status != 2 && status !=3)
			return false;
		if(room==null)
			return false;
		if(!room.shangzuo(this))
			return false;
		if(status ==2) //not speaking
			status = 4;
		else 
			status = 5;
		room.broadcast_players();
		return true;
	}
	public boolean xiazuo(){
		if(status != 4 && status != 5)
			return false;
		if(room==null)
			return false;
		if(!room.xiazuo(this))
			return false;
		if(status == 4) //not speaking
			status = 2;
		else 
			status = 3;
		room.broadcast_players();
		return true;
	}
	public void gamestart(){
		status = 6;
	}
	public boolean gameover(){
		if(status != 6 && status != 7 && status != 8)
		{
			//logger.error("player status:"+status);
			return false;
		}
		status = 2;
		return true;
	}
	public void addscore(int s){
		
	}
	public void killone(int n){
		if(room==null)
			return ;
		room.killone(this,n);
	}
	public void checkone(int n){
		if(room==null)
			return ;
		room.checkone(this, n);
	}
	public void voteone(int n){
		if(room==null)
			return ;
		room.voteone(this, n);
	}
	public void baofei(){
		if(room==null)
			return ;
		room.baofei(this);
	}
	public void skipspeak(){
		room.speakend(this);
	}
}

package com.fanlunge;

import org.apache.logging.log4j.Logger;
//import org.apache.logging.log4j.LogManager;
import java.util.LinkedList;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.util.Timer;  
import java.util.TimerTask; 
import java.lang.Thread;

/*
public class PaimaiTimerTask extends TimeTask{
	@Override
	public void run{
		
	}
}
*/
public class Room {
	private static int roomnum=0;
	private static Logger logger;// = LogManager.getLogger(Room.class);
	private int totalnum;
	private User speaker = null;
	private AtomicInteger shangzuonum = new AtomicInteger();
	//private ReadWriteLock rwl = new ReentrantReadWriteLock();//synchronize the users in room
	private Lock lock = new ReentrantLock();
	private AtomicInteger curnum = new AtomicInteger();
	private LinkedList<User> alluser = new LinkedList<User>();
	private LinkedList<User> shangmaiuser = new LinkedList<User>();
	private LinkedList<User> shangzuouser = new LinkedList<User>();
	private LinkedList<User> guanzhonguser = new LinkedList<User>();
	private HashMap<User,Integer> userlocation = new HashMap<User,Integer>();
	private HashMap<Integer,User> locationuser = new HashMap<Integer,User>();
	private HashMap<User,Integer> userrole = new HashMap<User,Integer>();
	private HashMap<Integer,Integer> GameTime = new HashMap<Integer,Integer>();
	private HashMap<User,Integer> killervote = new HashMap<User,Integer>();
	private HashMap<User,Integer> policemanvote = new HashMap<User,Integer>();
	private HashMap<User,Integer> allvote = new HashMap<User,Integer>();
	private HashMap<User,Boolean> killeracted = new HashMap<User,Boolean>();
	private HashMap<User,Boolean> policemanacted = new HashMap<User,Boolean>();
	private HashMap<User,Boolean> allacted = new HashMap<User,Boolean>();
	private HashMap<User,Boolean> useralive = new HashMap<User,Boolean>();
	private LinkedList<User> speakuserlist = null;
	private LinkedList<User> policemanlist = new LinkedList<User>();
	private LinkedList<User> killerlist = new LinkedList<User>();
	private LinkedList<Integer> pklist = null;
	private LinkedList<User> offlinelist = new LinkedList<User>();
	private boolean started;
	private User someonebaofei;
	private Timer maitimer = null;
	private Timer gametimer = null;
	private Timer discusstimer = null;
	private int maitime = 30;
	private int warntime = 5;
	private int gamestate = 0;
	private int gameoverstate = 0;
	private int checked = 0;
	private int killed = 0;
	private int num_yiyan = -1;
	//private TimerTask Paimaitask = 
	public Room(String type, int playernum){
		curnum.set(0);
		totalnum = playernum;
		shangzuonum.set(0);
		started = false;
		/*
		GameTime.put(0, 30);
		GameTime.put(1, 60);
		GameTime.put(2, 5);
		GameTime.put(4,30);
		GameTime.put(41,30);
		GameTime.put(42,30);
		GameTime.put(5,5);
		*/
		GameTime.put(0, 10);
		GameTime.put(1, 10);
		GameTime.put(2, 5);
		GameTime.put(3,10);
		GameTime.put(41,10);
		GameTime.put(42,10);
		GameTime.put(43,10);
		GameTime.put(44,10);
		GameTime.put(5,5);
		GameTime.put(6,5);
	}
	public int getCurNum(){
		return curnum.get();
	}
	public boolean shangzuo(User u){
		lock.lock();
		if(started)
		{
			System.out.println("game have started");
			lock.unlock();
			return false;
		}
		if(!alluser.contains(u)||started)
		{
			System.out.println("don't have the user:"+u.getName());
			lock.unlock();
			return false;
		}
		shangzuouser.add(u);
		guanzhonguser.remove(u);
		shangzuonum.getAndIncrement();
		checkgamestart();
		lock.unlock();
		return true;
	}
	public boolean xiazuo(User u){
		lock.lock();
		if(!alluser.contains(u))
		{
			System.out.println("don't have the user:"+u.getName());
			lock.unlock();
			return false;
		}
		if(started)
		{
			System.out.println("game have started");
			lock.unlock();
			return false;
		}
		shangzuouser.remove(u);
		guanzhonguser.add(u);
		shangzuonum.getAndDecrement();
		lock.unlock();
		return true;
	}
	public boolean shangmai(User u){
		lock.lock();
		if(started)
		{
			System.out.println("game have started");
			lock.unlock();
			return false;
		}
		if(!alluser.contains(u)||started)
		{
			System.out.println("don't have the user:"+u.getName());
			lock.unlock();
			return false;
		}
		if(shangmaiuser.isEmpty())
		{
			shangmaiuser.add(u);
			speaker = u;
			Startmaitimer();
		}else{
			shangmaiuser.add(u);
		}
		lock.unlock();
		return true;
	}
	public boolean xiamai(User u){
		lock.lock();
		if(started)
		{
			System.out.println("game have started");
			lock.unlock();
			return false;
		}
		if(!alluser.contains(u)||started)
		{
			System.out.println("don't have the user:"+u.getName());
			lock.unlock();
			return false;
		}
		if(shangmaiuser.contains(u))
			shangmaiuser.remove(u);
		if(speaker==u){
			try{
				maitimer.cancel();
				maitimer = null;
			}catch(Exception e){
				e.printStackTrace();
			}
			if(shangmaiuser.isEmpty()){
				speaker = null;
				broad_speaker();
				checkgamestart();
			}else{
				speaker = shangmaiuser.getFirst();
				Startmaitimer();	
			}
		}
		lock.unlock();
		return true;
	}
	
	public void RemoveUser(User u){
		lock.lock();
		if(!alluser.contains(u))
		{
			System.out.println("don't have the user:"+u.getName());
			lock.unlock();
			return;
		}
		try{
			alluser.remove(u);
			if(guanzhonguser.contains(u))
				guanzhonguser.remove(u);
			if(shangzuouser.contains(u)){
				shangzuouser.remove(u);
				shangzuonum.getAndDecrement();
			}
			if(shangmaiuser.contains(u)){
				shangmaiuser.remove(u);
			}
			int a = curnum.getAndDecrement();
			System.out.println("user num in room:"+a);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			lock.unlock();
		}
	}
	public void someonequit(User uu){
		//RemoveUser(uu);
		lock.lock();
		if(alluser.contains(uu))
			alluser.remove(uu);
		if(guanzhonguser.contains(uu))
			guanzhonguser.remove(uu);
		if(shangzuouser.contains(uu)){
			shangzuouser.remove(uu);
			shangzuonum.getAndDecrement();
			if(started){
				offlinelist.add(uu);
			}
		}
		if(shangmaiuser.contains(uu)){
			shangmaiuser.remove(uu);
		}
		int a = curnum.getAndDecrement();
		System.out.println("user disconnect! usr num in room:"+a);
		//boradcast
		String st = "someonequit "+userlocation.get(uu);
		try{
			for(User u:alluser){
				u.getSession().getBasicRemote().sendText(st);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			lock.unlock();
		}
	}
	public void someonereturn(User uu){
		lock.lock();
		if(offlinelist.contains(uu)){
			shangzuouser.add(uu);
			alluser.add(uu);
			shangzuonum.getAndIncrement();
			curnum.getAndIncrement();
			offlinelist.remove(uu);
		}
		//boradcast
		String st = "someonereturn "+userlocation.get(uu);
		try{
			for(User u:alluser){
				u.getSession().getBasicRemote().sendText(st);
			}
		}catch(Exception e){
				e.printStackTrace();
		}finally{
				lock.unlock();
		}
	}
	public boolean addplayer(User u)
	{
		lock.lock();
		if(curnum.get()<totalnum){
			alluser.add(u);
			guanzhonguser.add(u);
			curnum.getAndIncrement();
			lock.unlock();
			return true;
		}
		else{
			lock.unlock();
			return false;
		}
	}
	
	public void baofei(User u){
		lock.lock();
		System.out.println("user baofei:"+u.getName());
		if(userrole.get(u)!=1||(gamestate!=1&&gamestate!=2)){
			lock.unlock();
			return;
		}
		if(someonebaofei!=null){
			lock.unlock();
			return;
		}
		useralive.put(u, false);
		someonebaofei=u;
		if(1 == gamestate){
			//still in night
			if(null!=discusstimer)
				discusstimer.cancel();
			discusstimer = null;
			System.out.println("user baofei:"+u.getName());
			//StartGameTimer();
		}else if(2 == gamestate){
			//day time
			if(null!=discusstimer)
				discusstimer.cancel();
			discusstimer = null;
			broad_baofei(u);
			gamestate = 5;
			StartGameTimer();
		}
		lock.unlock();
	}
	public void speakend(User u){
		lock.lock();
		if(u!=speaker){
			return;
		}
		if(null!=discusstimer)
		discusstimer.cancel();
		discusstimer = null;
		StartDiscussTimer();
		lock.unlock();
	}
	public void killone(User killer, int targetnumber){
		lock.lock();
		User target = locationuser.get(targetnumber);
		if(gamestate == 1&&userrole.get(killer)==1&&!killeracted.get(killer)&&useralive.get(killer)&&useralive.get(target)){
			Integer v = killervote.get(target);
			v += killer.getleadership();
			killervote.put(target, v);
			killeracted.put(killer, true);
		}
		lock.unlock();
	}
	public void checkone(User policeman, int targetnumber){
		lock.lock();
		User target = locationuser.get(targetnumber);
		if(gamestate == 1&&userrole.get(policeman)==0&&!policemanacted.get(policeman)&&useralive.get(policeman)){
			Integer v = policemanvote.get(target);
			v += policeman.getleadership();
			policemanvote.put(target, v);
			policemanacted.put(policeman, true);
		}
		lock.unlock();
	}
	public void voteone(User user, int targetnumber){
		lock.lock();
		User target = locationuser.get(targetnumber);
		if((gamestate == 3||gamestate == 42||gamestate == 44)&&!allacted.get(user)&&useralive.get(user)&&useralive.get(target)){
			int v = allvote.get(target);
			v += 1;
			allvote.put(target, v);
			allacted.put(user, true);
		}
		lock.unlock();
	}
	public void broadcast_players(){
		lock.lock();
		boolean c;
		String st="roomplayer ";
		c = false;
		for(User u:shangmaiuser){
			st+=u.getName()+","+u.getStatus()+";";
			c = true;
		}
		if(c)
			st = st.substring(0, st.length()-1);
		st += "#";
		c = false;
		for(User u:shangzuouser){
			st+=u.getName()+","+u.getStatus()+";";
			c = true;
		}
		if(c)
			st = st.substring(0, st.length()-1);
		st += "#";
		c = false;
		for(User u:guanzhonguser){
			st+=u.getName()+","+u.getStatus()+";";
			c = true;
		}
		if(c)
			st = st.substring(0, st.length()-1);
		try{
			for(User u:alluser){
				u.getSession().getBasicRemote().sendText(st);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			lock.unlock();
		}
	}
	private int getpk(){
		Iterator iter;
		//statistic vote
		iter = allvote.entrySet().iterator();
		int vote=0;
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			User u = (User) entry.getKey();
			if(!pklist.contains(userlocation.get(u)))
				continue;
			int v = allvote.get(u);
			System.out.println("vote vote:"+u.getName()+'/'+v);
			if(v>vote){
				vote = v;
			}
		}
		
		iter = allvote.entrySet().iterator();
		LinkedList<User> killeds = new LinkedList<User>();
		LinkedList<Integer> pklist_t = new LinkedList<Integer>();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			User u = (User) entry.getKey();
			if(!pklist.contains(userlocation.get(u)))
				continue;
			int v = allvote.get(u);
			if(v==vote){
				killeds.add(u);
				pklist_t.add(userlocation.get(u));
			}			
		}
		pklist = pklist_t;
		if(killeds.size()==1){
			useralive.put(killeds.getFirst(), false);
			//System.out.println("voted user:"+killed.getName()+useralive.get(killed));
			return userlocation.get(killeds.getFirst());
		}else{
			return 0;
		}
	}
	private int getvoted(){
		Iterator iter;
		//statistic vote
		iter = allvote.entrySet().iterator();
		int vote=0;
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			User u = (User) entry.getKey();
			int v = allvote.get(u);
			System.out.println("vote vote:"+u.getName()+'/'+v);
			if(v>vote){
				vote = v;
			}
		}
		
		iter = allvote.entrySet().iterator();
		LinkedList<User> killeds = new LinkedList<User>();
		pklist = new LinkedList<Integer>();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			User u = (User) entry.getKey();
			int v = allvote.get(u);
			if(v==vote){
				killeds.add(u);
				pklist.add(userlocation.get(u));
			}			
		}
		if(killeds.size()==1){
			useralive.put(killeds.getFirst(), false);
			//System.out.println("voted user:"+killed.getName()+useralive.get(killed));
			return userlocation.get(killeds.getFirst());
		}else{
			return 0;
		}
	}
	private int getkilled() {
		Iterator iter;
		//statistic vote
		iter = killervote.entrySet().iterator();
		User killed=null;
		int vote=0;
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			User u = (User) entry.getKey();
			Integer v = killervote.get(u);
			System.out.println("kill vote:"+u.getName()+'/'+v);
			if(v>vote){
				killed = u;
				vote = v;
			}
			else if(v==vote){
				int x=(int)(Math.random()*100);
				if(x<50){
					killed = u;
					vote = v;
				}
				if(killed == null){
					killed = u;
				}
			}			
		}
		if(0 == vote)
			return 0;
		else{
			useralive.put(killed, false);
			return userlocation.get(killed);
		}
	}
	private int getchecked() {
		Iterator iter;
		//statistic vote
		iter = policemanvote.entrySet().iterator();
		User killed=null;
		int vote=0;
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			User u = (User) entry.getKey();
			Integer v = policemanvote.get(u);
			System.out.println("check vote:"+u.getName()+'/'+v);
			if(v>vote){
				killed = u;
				vote = v;
			}
			else if(v==vote){
				int x=(int)(Math.random()*100);
				if(x<50){
					killed = u;
					vote = v;
				}
				if(killed == null){
					killed = u;
				}
			}			
		}
		if(0 == vote)
			return 0;
		else
			return userlocation.get(killed);
	}
	private void broad_killed(int killed){
		try{
			for(User u:alluser){
				u.getSession().getBasicRemote().sendText("killed "+killed);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private void broad_voted(int killed){
		try{
			if(0==killed){
				String pkers="";
				for(int i=0;i<pklist.size();i++){
					if(0==i)
						pkers+=pklist.get(i).toString();
					else
						pkers+='_'+pklist.get(i).toString();
				}
				for(User u:alluser){
					u.getSession().getBasicRemote().sendText("voted 0 "+pkers);
				}
			}else{
				for(User u:alluser){
					u.getSession().getBasicRemote().sendText("voted "+killed);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private void broad_checked(int checked){
		try{
			User checkeduser = locationuser.get(checked);
			Iterator iter = userrole.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				User u = (User) entry.getKey();
				int role = userrole.get(u);
				if(role == 0)
					u.getSession().getBasicRemote().sendText("checked "+checked+" "+userrole.get(checkeduser));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private void broad_baofei(User baof){
		try{
			for(User u:alluser){
				u.getSession().getBasicRemote().sendText("someonebaofei "+userlocation.get(baof));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private void broad_speaker(){
		String st;
		if(speaker!=null){
			if(started)
				st = "speaker "+userlocation.get(speaker)+" "+speaker.getspeaksec();
			else
				st = "speaker "+speaker.getName()+" "+maitime;
		}
		else{
			st = "speaker #";
		}
		for(User u:alluser)
			try{
				u.getSession().getBasicRemote().sendText(st);
			}catch(Exception e){
				e.printStackTrace();
			}
	}
	private void broad_speaker_offline(){
		String st;
		if(speaker!=null){
			if(started)
				st = "speaker "+userlocation.get(speaker)+" -1";
			else
				st = "speaker "+speaker.getName()+" "+maitime;
		}
		else{
			st = "speaker #";
		}
		for(User u:alluser)
			try{
				u.getSession().getBasicRemote().sendText(st);
			}catch(Exception e){
				e.printStackTrace();
			}
	}
	private void stopkillandcheck(){
		Iterator iter;
		iter = killeracted.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			User u = (User) entry.getKey();
			killeracted.put(u, true);
		}
		iter = policemanacted.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			User u = (User) entry.getKey();
			policemanacted.put(u, true);
		}		
	}

	private void stopvote(){
		Iterator iter = allacted.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			User u = (User) entry.getKey();
			allacted.put(u, true);
		}
	}
	private void startoutsidepkvote(){
		Iterator iter;
		//clean vote
		iter = allvote.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			User u = (User) entry.getKey();
			allvote.put(u, 0);
		}
		//clean acted
		iter = allacted.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			User u = (User) entry.getKey();
			allacted.put(u, false);
		}
		//all vote
		String pkers="";
		for(int i=0;i<pklist.size();i++){
			if(0==i)
				pkers+=pklist.get(i).toString();
			else
				pkers+='_'+pklist.get(i).toString();
		}
		System.out.println(pkers+pklist);
		try{
			for(User u:shangzuouser){
				if(useralive.get(u))
					u.getSession().getBasicRemote().sendText("startvoteoutsidepk "+pkers);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private void startinsidepkvote(){
		Iterator iter;
		//clean vote
		iter = allvote.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			User u = (User) entry.getKey();
			allvote.put(u, 0);
		}
		//clean acted
		iter = allacted.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			User u = (User) entry.getKey();
			allacted.put(u, false);
		}
		//all vote
		String pkers="";
		for(int i=0;i<pklist.size();i++){
			if(0==i)
				pkers+=pklist.get(i).toString();
			else
				pkers+='_'+pklist.get(i).toString();
		}
		System.out.println(pkers+pklist);
		try{
			for(User u:shangzuouser){
				if(useralive.get(u))
					u.getSession().getBasicRemote().sendText("startvoteinsidepk "+pkers);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private void startvote(){
		Iterator iter;
		//clean vote
		iter = allvote.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			User u = (User) entry.getKey();
			allvote.put(u, 0);
		}
		//clean acted
		iter = allacted.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			User u = (User) entry.getKey();
			allacted.put(u, false);
		}
		//all vote
		try{
			for(User u:shangzuouser){
				if(useralive.get(u))
					u.getSession().getBasicRemote().sendText("startvote");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private void startkillandcheck(){
		Iterator iter;
		//clean vote
		iter = killervote.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			User u = (User) entry.getKey();
			killervote.put(u, 0);
		}
		iter = policemanvote.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			User u = (User) entry.getKey();
			policemanvote.put(u, 0);
		}
		//clean acted
		iter = killeracted.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			User u = (User) entry.getKey();
			killeracted.put(u, false);
		}
		iter = policemanacted.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			User u = (User) entry.getKey();
			policemanacted.put(u, false);
		}		
		//killer and policeman vote
		iter = userrole.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			User u = (User) entry.getKey();
			int role = userrole.get(u);
			try{
			if(0 == role)
				u.getSession().getBasicRemote().sendText("yan");
			else if(1 == role)
				u.getSession().getBasicRemote().sendText("sha");
			else
				u.getSession().getBasicRemote().sendText("shayan");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	private void StartGameTimer(){
		if(gametimer == null)
			gametimer = new Timer();
		System.out.println(gamestate);
		int delay = GameTime.get(gamestate);
		gametimer.schedule(new TimerTask() {
			public void run() {
				lock.lock();
				switch(gamestate){
					case 5:
						if(checkgameover())
						{
							lock.unlock();
							endGame();
							return;
						}
						gamestate = 6;
						new Timer().schedule(new TimerTask() {
							public void run() {
								lock.lock();
								StartGameTimer();
								lock.unlock();
							}
						}, 1000*(delay)); 
						lock.unlock();
						return;
					case 6:
						//speak yiyan
						
						//after yiyan
					case 0:
						//after 30s prepared, or activities one day, now the night coming
						gamestate = 1;
						someonebaofei = null;
						startkillandcheck();
						break;
					case 1:
						//daytime, process kill and check
						stopkillandcheck();
						killed = getkilled();
						checked = getchecked();
						System.out.println("killed:"+killed+' '+"checked:"+checked);
						if(checkgameover())
						{
							lock.unlock();
							endGame();
							return;
						}
						broad_killed(killed);
						if(checked!=0)
							broad_checked(checked);
						if(someonebaofei!=null){
							broad_baofei(someonebaofei);
							gamestate = 5;
							StartGameTimer();
							lock.unlock();
							return;
						}
						//speak in turn
						speakuserlist = new LinkedList<User>();
						if(killed!=0){
							int startone = killed;
							if(num_yiyan>0){
								num_yiyan--;
							}
							else{
								if(killed!=locationuser.size())
									startone += 1;
								else
									startone = 1;
							}
							for(int i = startone; i<=locationuser.size(); i++){
								if(useralive.get(locationuser.get(i)))
									speakuserlist.add(locationuser.get(i));
							}
							for(int i = 1; i<startone; i++){
								if(useralive.get(locationuser.get(i)))
									speakuserlist.add(locationuser.get(i));
							}
						}
						else{
							for(int i = 1; i<=locationuser.size(); i++){
								if(useralive.get(locationuser.get(i)))
									speakuserlist.add(locationuser.get(i));
							}
						}
						gamestate = 2;
						speaker = null;
						StartDiscussTimer();//do not launch GameTimer, instead, use DiscussTimer 
						lock.unlock();
						return;
					case 3:
						stopvote();
						int voted = getvoted();
						broad_voted(voted);
						if(0!=voted){
							System.out.println("voted:"+voted);
							gamestate = 5;
						}else{
							System.out.println("first time vote equal:"+pklist);
							//speak in turn
							speakuserlist = new LinkedList<User>();
							if(killed!=0){
								int startone = killed;
								if(num_yiyan>0){
									num_yiyan--;
								}
								else{
									if(killed!=locationuser.size())
										startone += 1;
									else
										startone = 1;
								}
								for(int i = startone; i<=locationuser.size(); i++){
									if(useralive.get(locationuser.get(i)))
										speakuserlist.add(locationuser.get(i));
								}
								for(int i = 1; i<startone; i++){
									if(useralive.get(locationuser.get(i)))
										speakuserlist.add(locationuser.get(i));
								}
							}
							else{
								for(int i = 1; i<=locationuser.size(); i++){
									if(pklist.contains(i)&&useralive.get(locationuser.get(i)))
										speakuserlist.add(locationuser.get(i));
								}
							}
							gamestate = 41;
							speaker = null;
							StartDiscussTimer();//do not launch GameTimer, instead, use DiscussTimer 
							lock.unlock();
							return;
						}
						break;
					case 42:
						stopvote();
						int pk1voted = getpk();
						broad_voted(pk1voted);
						if(0!=pk1voted){
							System.out.println("pk:"+pk1voted);
							gamestate = 5;
						}else{
							System.out.println("second time vote equal:"+pklist);
							//speak in turn
							speakuserlist = new LinkedList<User>();
							if(killed!=0){
								int startone = killed;
								if(num_yiyan>0){
									num_yiyan--;
								}
								else{
									if(killed!=locationuser.size())
										startone += 1;
									else
										startone = 1;
								}
								for(int i = startone; i<=locationuser.size(); i++){
									if(useralive.get(locationuser.get(i)))
										speakuserlist.add(locationuser.get(i));
								}
								for(int i = 1; i<startone; i++){
									if(useralive.get(locationuser.get(i)))
										speakuserlist.add(locationuser.get(i));
								}
							}
							else{
								for(int i = 1; i<=locationuser.size(); i++){
									if(!pklist.contains(i)&&useralive.get(locationuser.get(i)))
										speakuserlist.add(locationuser.get(i));
								}
							}
							gamestate = 43;
							speaker = null;
							StartDiscussTimer();//do not launch GameTimer, instead, use DiscussTimer 
							lock.unlock();
							return;
						}
						break;
					case 44:
						stopvote();
						int pk2voted = getpk();
						broad_voted(pk2voted);
						if(0!=pk2voted){
							System.out.println("pk:"+pk2voted);
							gamestate = 5;
						}else{
							System.out.println("pingju, 2nd time vote equal:"+pklist);
							gameoverstate = -1;
							lock.unlock();
							endGame();
							return;
						}
						break;
					default:
						break;
				}
				StartGameTimer();
				lock.unlock();
			}
		}, 1000*delay); 
	}
	private void StartDiscussTimer(){
		if(speakuserlist.isEmpty()){
			if(speaker != null){
				speaker.speakover();
		    	System.out.println("speaker end:"+speaker.getName());
		    	try{
					speaker.getSession().getBasicRemote().sendText("speakover");
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			speaker = null;
			System.out.println("start vote");
			if(2==gamestate){
				gamestate = 3;
				startvote();
			}else if(41==gamestate){
				gamestate = 42;
				startinsidepkvote();
			}else if(43==gamestate){
				gamestate = 44;
				startoutsidepkvote();
			}
			StartGameTimer();
			return;
		}
		if(discusstimer == null)
			discusstimer = new Timer();
		if(speaker != null){
			speaker.speakover();
	    	System.out.println("speaker end:"+speaker.getName());
	    	try{
				speaker.getSession().getBasicRemote().sendText("speakover");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		speaker = speakuserlist.removeFirst();
		System.out.println("speaker:"+speaker.getName());
		int delay;
		if(offlinelist.contains(speaker)){
			delay = warntime;
			broad_speaker_offline();
			speaker = null;
		}
		else{
			delay = warntime + speaker.getspeaksec();
			broad_speaker();
		}
		discusstimer.schedule(new TimerTask() {
			public void run() {
				lock.lock();
				StartDiscussTimer();
				lock.unlock();
			}
		}, 1000*delay); 
	}
	private void Startmaitimer(){
		if(maitimer == null)
			maitimer = new Timer();
		maitimer.schedule(new TimerTask() {  
	        public void run() {
	        	lock.lock();
	        	shangmaiuser.removeFirst();
	        	speaker.speakover();
	        	System.out.println("speaker end:"+speaker.getName());
	        	try{
	    			speaker.getSession().getBasicRemote().sendText("speakover");
	    		}catch(Exception e){
	    			e.printStackTrace();
	    		}
	        	if(shangmaiuser.isEmpty()){
	        		speaker = null;
	        		broad_speaker();
	        		checkgamestart();
	        		return;
	        	}
	        	User sp = shangmaiuser.getFirst();  
	            speaker = sp;
	            Startmaitimer();
	            lock.unlock();
	            broadcast_players();
	        }
	    }, 1000*maitime); 
		
		System.out.println("speaker start:"+speaker.getName());
        broad_speaker();
	}
	private void checkgamestart(){
		if(shangzuonum.get()>=3&&shangmaiuser.isEmpty())
		{
			started = true;
			initGame();
			gamestate=0;
			StartGameTimer();			
			String players="";
			for(int i=0; i<shangzuouser.size(); i++){
				if(0==i)
					players+=shangzuouser.get(i).getName();
				else
					players+='_'+shangzuouser.get(i).getName();
			}
			num_yiyan = policemanlist.size();
			for(User u:shangzuouser)
				try{
					u.gamestart();
					if(userrole.get(u)==2){
						u.getSession().getBasicRemote().sendText("gamestart "+policemanlist.size()+' '+players+' '+userrole.get(u)+' '+userlocation.get(u));
					}
					else if(userrole.get(u)==0){
						String partner="";
						for(int i=0;i<policemanlist.size();i++){
							if(policemanlist.get(i)!=u)
								partner+='_'+userlocation.get(policemanlist.get(i));
						}
						if(partner.length()!=0)
							partner = partner.substring(1, partner.length());
						u.getSession().getBasicRemote().sendText("gamestart "+policemanlist.size()+' '+players+' '+userrole.get(u)+' '+userlocation.get(u)+' '+partner);
					}
					else if(userrole.get(u)==1){
						String partner="";
						for(int i=0;i<killerlist.size();i++){
							if(killerlist.get(i)!=u)
								partner+='_'+userlocation.get(killerlist.get(i));
						}
						if(partner.length()!=0)
							partner = partner.substring(1, partner.length());
						u.getSession().getBasicRemote().sendText("gamestart "+policemanlist.size()+' '+players+' '+userrole.get(u)+' '+userlocation.get(u)+' '+partner);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
		}
	}
	private void initGame(){
		int gameplayernum = shangzuouser.size();
		ArrayList<Integer> loc = new ArrayList<Integer>();
		ArrayList<Integer> role = getroles(gameplayernum); 
		for(int i = 0; i < gameplayernum; i++)
			loc.add(i+1);
		Collections.shuffle(loc);
		Collections.shuffle(role);
		int i=0;
		for(User u:shangzuouser)
		{	
			u.gamestart();
			userlocation.put(u, loc.get(i));
			locationuser.put(loc.get(i), u);
			userrole.put(u,	role.get(i));
			if(0 == role.get(i)){
				policemanacted.put(u, false);
				policemanlist.push(u);
			}
			else if(1==role.get(i)){
				killeracted.put(u, false);
				killerlist.push(u);
			}
			allacted.put(u, false);
			useralive.put(u, true);
			policemanvote.put(u,0);
			killervote.put(u, 0);
			allvote.put(u, 0);
			i++;
		}
			
	}
	private void endGame(){
		lock.lock();
		//user score
		int jing,fei,min;
		switch(gameoverstate){
		case 1:
			jing = 20;
			fei = -15;
			min = 15;
			break;
		case 2:
			fei = 30;
			jing = -15;
			min = -10;
			break;
		case 3:
			fei = 60;
			jing = -25;
			min = -15;
			break;
		default:
			fei = 0;
			jing = 0;
			min = 0;
			break;
		}
		for(User u:shangzuouser){
			if(useralive.get(u)){
				switch(userrole.get(u)){
				case 0:
					u.addscore(jing);
					break;
				case 1:
					u.addscore(fei);
					break;
				case 2:
					u.addscore(min);
					break;
				default:
					break;
				}
			}
		}
		//broadcast
		for(User u:shangzuouser){
			int s=0;
			switch(userrole.get(u)){
				case 0:
					s = jing;
					break;
				case 1:
					s = fei;
					break;
				case 2:
					s=min;
					break;
				default:
					break;
			}
			try{
				u.getSession().getBasicRemote().sendText("gameover "+gameoverstate+" "+s);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		//room state clean
		started = false;
		num_yiyan = -1;
		for(User u:shangzuouser){
			if(u.gameover()){
				System.out.println(u.getName());
				guanzhonguser.add(u);
			}
			else
				System.out.println("wrong");
		}
		if(maitimer!=null)
			maitimer.cancel();
		maitimer = null;
		if(gametimer!=null)
			gametimer.cancel();
		gametimer = null;
		if(discusstimer!=null)
			discusstimer.cancel();
		discusstimer = null;
		//alluser = new LinkedList<User>();
		shangmaiuser = new LinkedList<User>();
		shangzuouser = new LinkedList<User>();
		userlocation = new HashMap<User,Integer>();
		locationuser = new HashMap<Integer,User>();
		userrole = new HashMap<User,Integer>();
		killervote = new HashMap<User,Integer>();
		policemanvote = new HashMap<User,Integer>();
		allvote = new HashMap<User,Integer>();
		killeracted = new HashMap<User,Boolean>();
		policemanacted = new HashMap<User,Boolean>();
		allacted = new HashMap<User,Boolean>();
		useralive = new HashMap<User,Boolean>();
		speakuserlist = null;
		maitimer = null;
		gametimer = null;
		discusstimer = null;
		policemanlist = new LinkedList<User>();
		killerlist = new LinkedList<User>();
		offlinelist = new LinkedList<User>();
		shangzuonum.set(0);
		lock.unlock();
		broadcast_players();
	}

	private ArrayList<Integer> getroles(int num){
		int jing=0,fei=0;
		ArrayList<Integer> role = new ArrayList<Integer>();
		if(num<=5){
			jing=1;
			fei=1;
		}
		else if(num>=6&&num<=10){
			jing=2;
			fei=2;
		}
		else if(num>=11&&num<=14){
			jing=3;
			fei=3;
		}
		else if(num>=15){
			jing=4;
			fei=4;
		}
		for(int i=0; i<num; i++)
		{
			if(i<jing)
				role.add(0);
			else if(i<jing+fei)
				role.add(1);
			else
				role.add(2);
		}
		return role;
	}
	private boolean checkgameover(){
		int jing = 0;
		int fei = 0;
		int min = 0;
		for(User u:shangzuouser){
			if(useralive.get(u)){
				switch(userrole.get(u)){
				case 0:
					jing++;
					break;
				case 1:
					fei++;
					break;
				case 2:
					min++;
					break;
				default:
					break;
				}
			}
		}
		if(0 == fei){
			gameoverstate = 1;
			return true;
		}
		else if(0 == min){
			gameoverstate = 3;
			return true;
		}
		else if(0 == jing){
			gameoverstate = 2;
			return true;
		}
		return false;
	}
}

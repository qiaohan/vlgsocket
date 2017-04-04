package com.fanlunge;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.OnClose;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@ServerEndpoint(value = "/game")
public class GameSocket {
	private static Logger logger = LogManager.getLogger(GameSocket.class);
	private static ReadWriteLock rwl = new ReentrantReadWriteLock();
	private static LinkedList<User> offlineusers = new LinkedList<User>();
	private static HashMap<Session,User> sess2user = new HashMap<Session,User>();
	private static HashMap<Integer,Room> putong=new HashMap<Integer,Room>(); // 50 putongfang in total
	static{
		for(int i = 0; i<50; i++){
			Room r = new Room("putong",20);
			putong.put(i, r);
		}
	}
	@OnOpen
    public void openConn(Session session) throws IOException {
		session.getBasicRemote().sendText("welcome to vlg"); // means open it
    	
    }
	private void broadcast_putong(){
		String putongfang="roomputong ";
		rwl.readLock().lock();
		for(int i=0;i<putong.size();i++){
			if(i==0)
				putongfang+=putong.get(i).getCurNum();
			else{
				putongfang+="_"+putong.get(i).getCurNum();
			}
		}		
		try{
			for(Session s:sess2user.keySet()){
				s.getBasicRemote().sendText(putongfang);
			}	
		}catch (IOException e) {
			e.printStackTrace();
		}finally{
			rwl.readLock().unlock();
		}
	}
	@OnMessage
	public void onTextMessage(Session session, String msg, boolean last) {
		try {
			if (session.isOpen()) {
				logger.info("received from client message = " + msg);
				
				/*
				if(!u.step(msg))
					System.out.println("error encountered!");
				for(Session s:allsessions)
					s.getBasicRemote().sendText(msg, last);
				*/
				String[] mess = msg.split(" ");
				User u=null;
				rwl.readLock().lock();
				if(!mess[0].equals("login"))
					u = sess2user.get(session);
				rwl.readLock().unlock();
				switch(mess[0]){
					case "login":
						User u1=null;
						for(User uu:offlineusers){
							if(mess[1].equals(uu.getName())){
								u1 = uu;
								u1.reset();
								offlineusers.remove(uu);
								break;
							}
						}
						if(u1 == null)
							u1= new User(mess[1]);
						u1.bindsession(session);
						sess2user.put(session, u1);
						System.out.println("user number connect: "+sess2user.size());
						if(u1.passmatch())
							session.getBasicRemote().sendText("login 0");
						else
							session.getBasicRemote().sendText("login 1");
					break;
					case "query":
						if(mess[1].equals("roomputong")){
							broadcast_putong();				
						}else if(mess[1].equals("roomjiazu")){
							String jiazufang="roomjiazu ";
							for(int i=0;i<15;i++){
								if(i==0)
									jiazufang+=putong.get(i).getCurNum();
								else{
									jiazufang+="_"+putong.get(i).getCurNum();
								}
							}
							session.getBasicRemote().sendText(jiazufang);
						}else if(mess[1].equals("key")){
							session.getBasicRemote().sendText("pubkey your shit pubkey");
						}
					break;
					case "action":
						if(mess[1].equals("enter")){
							if(mess[2].equals("putong")){
								if(u.EnterRoom(putong.get(Integer.valueOf(mess[3]))))
								{
									session.getBasicRemote().sendText("enter 0");
									broadcast_putong();
								}
								else{
									session.getBasicRemote().sendText("enter 1");
								}
							}
							else if(mess[2].equals("jiazu")){
								if(u.EnterRoom(putong.get(Integer.valueOf(mess[3])))){
									session.getBasicRemote().sendText("enter 0");
									
								}else{
									session.getBasicRemote().sendText("enter 1");
								}
							}
						}
						else if(mess[1].equals("quit")){
							u.QuitRoom();
							broadcast_putong();
						}
						else if(mess[1].equals("shangzuo")){
							if(u.shangzuo()){
								session.getBasicRemote().sendText("shangzuo 0");
							}
							else
								session.getBasicRemote().sendText("shangzuo 1");
						}
						else if(mess[1].equals("xiazuo")){
							if(u.xiazuo())
								session.getBasicRemote().sendText("xiazuo 0");
							else
								session.getBasicRemote().sendText("xiazuo 1");
						}
						else if(mess[1].equals("shangmai")){
							if(u.shangmai())
								session.getBasicRemote().sendText("shangmai 0");
							else
								session.getBasicRemote().sendText("shangmai 1");
						}
						else if(mess[1].equals("xiamai")){
							if(u.xiamai())
								session.getBasicRemote().sendText("xiamai 0");
							else
								session.getBasicRemote().sendText("xiamai 1");
						}
						else if(mess[1].equals("sha")){
							u.killone(Integer.valueOf(mess[2]));
						}
						else if(mess[1].equals("yan")){
							u.checkone(Integer.valueOf(mess[2]));
						}
						else if(mess[1].equals("vote")){
							u.voteone(Integer.valueOf(mess[2]));
						}
						else if(mess[1].equals("baofei")){
							u.baofei();
						}
						else if(mess[1].equals("speakend")){
							u.skipspeak();
						}
					break;
					default:
					break;
				}
			}
		} catch (IOException e) {
			try {
				session.close();
			} catch (IOException e1) {
			}
		}
	}
	
    @OnMessage
    public void echoBinaryMessage(Session session, ByteBuffer bb, boolean last) {
    	//System.out.println("send binary message...");
        try {
            if (session.isOpen()) {
            	//System.out.println("byte buffer lenghth : " + bb.array().length);
            	User u = sess2user.get(session);
                session.getBasicRemote().sendBinary(bb, last);
            }
        } catch (IOException e) {
            try {
                session.close();
            } catch (IOException e1) {
                // Ignore
            }
        }
    }
    @OnClose
    public void onclose(Session sess){
    	if(!sess2user.containsKey(sess))
    		return;
    	rwl.writeLock().lock();
    	try{
    		User u = sess2user.get(sess);
    		u.ForceQuitRoom();
    		if(u.isplaying()){
    			offlineusers.add(u);
    			u.unbindsession();
    		}
    		if(!sess2user.remove(sess, u))
    			System.out.println("cannot rm map");
    		broadcast_putong();
    		System.out.println("user number disconnect: "+sess2user.size());
    	}finally{
    		rwl.writeLock().unlock();
    	}
    }
}

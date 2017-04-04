package com.fanlunge;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.io.IOException;
import java.math.BigInteger;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
/**
 * Servlet implementation class RegisterServlet
 */
@WebServlet("/Register")
public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    static HashMap<String,String> user2pubkey = new HashMap<String,String>(); 
    static HashMap<String,RSAPrivateKey> user2prikey = new HashMap<String,RSAPrivateKey>(); 
    static ReadWriteLock rwl = new ReentrantReadWriteLock();
    static Dao dao = new Dao("114.215.78.135","testvlg","qh","qiaohan");
    static int cnt = 0;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
		response.addHeader("Access-Control-Allow-Origin","*");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		cnt++;
		System.out.println(cnt);
		String action = request.getParameter("action");
		String acct = request.getParameter("accounttype");
		String accnum = request.getParameter("account");
		String username = request.getParameter("user");
		if(action==null||acct==null||accnum==null||username==null){
			response.getWriter().append("baddata");
			response.addHeader("Access-Control-Allow-Origin","*");
			return;
		}
		if(acct.equals("uid")||acct.equals("phonenum")||acct.equals("email")){
			if(action.equals("getpubkey")){
				generateKeypair(accnum);
				rwl.readLock().lock();
				response.getWriter().append(user2pubkey.get(accnum));
				System.out.println("send pubkey for "+accnum+"key: "+user2pubkey.get(accnum));
				rwl.readLock().unlock();
			}else if(action.equals("query")){
				
			}else if(action.equals("register")){
				String pass_encry = request.getParameter("passwd");
				//if(match(accnum,pass_encry)){
				if(pass_encry!=null){
					System.out.println(pass_encry);
					rwl.readLock().lock();
					RSAPrivateKey priKey = user2prikey.get(accnum);
					rwl.readLock().unlock();
					if(priKey==null)
						System.out.println("prikey is nil");
					else
						System.out.println(priKey);
					try {
						String pass_de = RSAUtil.decryptByPrivateKey(pass_encry, priKey);
						response.getWriter().append(pass_de);
						System.out.println("pass:"+pass_de);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else{
					response.getWriter().append("badpassword");
				}
			}else{
				response.getWriter().append("badaction");
			}
		}
		else{
			response.getWriter().append("baduser");
		}
		
		
		response.addHeader("Access-Control-Allow-Origin","*");
	}
	private boolean match(String u, String pass_en){
		//解密后的明文
		RSAPrivateKey priKey = user2prikey.get(u);
		String pubkey = user2pubkey.get(u);
		System.out.println("recv passwd:"+pass_en);
		System.out.println("recv user:"+u);
		if(priKey==null)
		System.out.println("prikey is nil");
		if(pubkey==null)
		System.out.println("pubkey is nil");
		try {
			String pass_de = RSAUtil.decryptByPrivateKey(pass_en, priKey);
			System.out.println("pass:"+pass_de);
			String passwd = dao.getLogInfo(u,"passwd");
			if(pass_de.equals(passwd))
				return true;
			else
				return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	private void generateKeypair(String user){
		rwl.writeLock().lock();
		try{
			HashMap<String, Object> map = RSAUtil.getKeys();
			//生成公钥和私钥
			RSAPublicKey publicKey = (RSAPublicKey) map.get("public");
			RSAPrivateKey privateKey = (RSAPrivateKey) map.get("private");
			//模
			BigInteger mm = publicKey.getModulus();
			String modulus = mm.toString();
			//私钥指数
			String private_exponent = privateKey.getPrivateExponent().toString();
			//使用模和指数生成公钥和私钥
			RSAPrivateKey priKey = RSAUtil.getPrivateKey(modulus, private_exponent);
			try{
				user2prikey.put(user, priKey);
				user2pubkey.put(user, mm.toString(16));
				RSAPublicKey pubKey = RSAUtil.getPublicKey(modulus, mm.toString());
				String mi = RSAUtil.encryptByPublicKey("987654321", pubKey);
				System.out.println("mi: "+mi);
			}finally{
				rwl.writeLock().unlock();
			}
		}catch(Exception e){
				e.printStackTrace();
		}
	}
}

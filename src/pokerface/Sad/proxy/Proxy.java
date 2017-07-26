package pokerface.Sad.proxy;

import java.io.Serializable;

import javax.xml.ws.ServiceMode;



class Proxy implements Serializable{
	
	private static final long serialVersionUID = -5276635609254853263L;  
	String ip;	//ip地址
	String port;	//端口
	String isAnonymous;	//匿名度
	boolean isHttpsSupported = false;	//是否支持Https协议
	String location;	//位置
	boolean available = false;	//可用
	public Proxy() {}
	
	
	public Proxy(String ip, String port) {
		super();
		this.ip = ip;
		this.port = port;
	}


	@Override
	public String toString() {
		return "Proxy \nip = " + ip + "\nport = " + port + "\nanonymity = "
				+ isAnonymous + "\nisHttpsSupported = " + isHttpsSupported + "\nlocation = "
				+ location +"\navailable = "+available;
	}


	
}

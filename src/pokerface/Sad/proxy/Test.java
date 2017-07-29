package pokerface.Sad.proxy;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import pokerface.Sad.util.HttpUtil;

public class Test {
	static Logger logger = null;
	static{
		PropertyConfigurator.configure("log4j.properties");
		logger = Logger.getLogger("Test.class");
	}
	
	public static void main(String[] args) throws IOException{
//		ProxyGetter.getIP181();
		List<Proxy> proxyList = new LinkedList<Proxy>();
		int index = 0;
		while(true)
		{
			Proxy proxy = new Proxy();
			proxy.ip = "120.27.163.210";
			proxy.port = "3128";
			proxyList.add(proxy);
			System.out.println(++index);
		}
//		String html = HttpUtil.get("http://www.oschina.net/", proxy.ip, proxy.port);
//		String html = HttpUtil.get("https://github.com/pokerfaceSad/ProxyDB", proxy.ip, proxy.port);
//		System.out.println(html);
		
	}
}

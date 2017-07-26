package pokerface.Sad.proxy;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import pokerface.Sad.util.HttpUtil;

public class ProxyGetter {

	static Logger logger = null;
	static {
		PropertyConfigurator.configure("log4j.properties");
		logger = Logger.getLogger(ProxyGetter.class);
	}

	/**
	 * http://www.kuaidaili.com/ 从 快代理 抓取代理信息封装为 LinkedList<Proxy> 返回
	 * 
	 * @return
	 */
	public static LinkedList<Proxy> getKuaiDaiLi() {
		LinkedList<Proxy> proxyList = new LinkedList<Proxy>();
		for (int i = 1; i <= 10; i++) {
			logger.info("正在抓取快代理第" + i + "页");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.fatal("线程中断异常", e);
			}
			String html = null;
			try {
				html = HttpUtil.get("http://www.kuaidaili.com/free/inha/" + i
						+ "/", null, null);
				logger.info("Jsoup解析页面");
				Document doc = Jsoup.parse(html);
				Elements eles = doc.select("#list > table > tbody > tr");
				Proxy proxy = null;
				for (Element ele : eles) {
					proxy = new Proxy(ele.child(0).text(), ele.child(1).text());
					logger.debug("解析到"+proxy.ip+":"+proxy.port);
					proxyList.add(proxy);
				}
				logger.info("页面解析完成");
			} catch (IOException e) {
				logger.error("抓取快代理第" + i + "页失败",e);
			}
		}

		return proxyList;
	}

	/**
	 * http://www.66ip.cn/ 从 代理66 抓取代理信息封装为 LinkedList<Proxy> 返回
	 * 
	 * @return
	 */
	public static LinkedList<Proxy> getDaiLi66() {

		LinkedList<Proxy> proxyList = new LinkedList<Proxy>();

		for (int i = 1; i <= 10; i++) {
			logger.info("正在抓取代理66第" + i + "页");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.fatal("线程中断异常", e);
			}
			String html = null;
			try {
				html = HttpUtil.get("http://www.66ip.cn/" + i + ".html", null,null);
				
				Document doc = Jsoup.parse(html);
				Elements eles = doc
						.select("#main > div > div:nth-child(1) > table > tbody > tr");
				
				logger.info("Jsoup解析页面");
				int index = 0;
				Proxy proxy = null;
				for (Element ele : eles) {
					if (index != 0) {
						proxy = new Proxy();
						proxy.ip = ele.child(0).text();
						proxy.port = ele.child(1).text();
						logger.debug("解析到"+proxy.ip+":"+proxy.port);
						proxyList.add(proxy);
					}
					index++;
				}
			} catch (IOException e) {
				logger.error("抓取代理66第" + i + "页失败",e);
			}
			logger.info("页面解析完成");
		}

		return proxyList;
	}

	/**
	 * http://www.xicidaili.com/ 从 西刺抓取代理信息封装为 LinkedList<Proxy> 返回
	 * 
	 * @return
	 */
	public static List<Proxy> getFromXiCiDaiLi() {
		List<Proxy> proxyList = new LinkedList<Proxy>();
		for (int i = 1; i <= 10; i++) {
			logger.info("正在抓取西刺代理第" + i + "页");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				logger.error("线程中断异常", e1);
			}
			String html = null;
			try {
				html = HttpUtil.get("http://www.xicidaili.com/nn/" + i, null,null);
				
				Document doc = Jsoup.parse(html);
				Elements eles = doc.select("#ip_list > tbody > tr");
				
				logger.info("Jsoup解析页面");
				int index = 0;
				for (Element e : eles) {
					if (index != 0) {
						Proxy proxy = new Proxy();
						proxy.ip = e.child(1).text();
						proxy.port = e.child(2).text();
						logger.debug("解析到"+proxy.ip+":"+proxy.port);
						proxyList.add(proxy);
					}
					index++;
				}
				logger.info("页面解析完成");
			} catch (IOException e) {
				logger.error("抓取西刺代理IP第" + i + "页失败",e);
			}


		}

		return proxyList;
	}
	/**
	 * http://www.89ip.cn 从 89ip抓取代理信息封装为 LinkedList<Proxy> 返回
	 * 
	 * @return
	 */
	public static List<Proxy> get89IP() {
		List<Proxy> proxyList = new LinkedList<Proxy>();

		logger.info("正在抓取89代理");
		String html = null;
		try {
			
			html = HttpUtil.get("http://www.89ip.cn/tiqv.php?sxb=&tqsl=200&ports=&ktip=&xl=on&submit=%CC%E1++%C8%A1", null, null);
		
			Document doc = Jsoup.parse(html);
			Pattern p = Pattern.compile("([0-9\\.]+?):([0-9]+?)\\n   <br />");
			Matcher m = p.matcher(doc.toString());
			logger.info("正则解析页面");
			while(m.find())
			{
				Proxy proxy = new Proxy();
				proxy.ip = m.group(1);
				proxy.port = m.group(2);
				logger.debug("解析到"+proxy.ip+":"+proxy.port);
				proxyList.add(proxy);
			}
			logger.info("解析完成");
		
		} catch (IOException e) {
			logger.error("抓取89代理失败", e);
		}

		return proxyList;
	}
	
	/**
	 * http://www.ip181.com/ 从 ip181抓取代理信息封装为 LinkedList<Proxy> 返回
	 * 
	 * @return
	 */
	public static List<Proxy> getIP181() {
		List<Proxy> proxyList = new LinkedList<Proxy>();

		logger.info("正在抓去181代理");
		String html = null;
		try {
			
			html = HttpUtil.get("http://www.ip181.com/", null, null);
			
			logger.info("Jsoup解析页面");
			Document doc = Jsoup.parse(html);
			Elements eles = doc.select("body > div:nth-child(3) > div.panel.panel-info > div.panel-body > div > div:nth-child(2) > table > tbody > tr");
			int index = 0;
			for(Element e:eles)
			{
				if(index++ != 0)
				{
					Proxy proxy = new Proxy();
					proxy.ip = e.child(0).text();
					proxy.port = e.child(1).text();
					logger.debug("解析到"+proxy.ip+":"+proxy.port);
					proxyList.add(proxy);
				}
				
			}
			logger.info("解析完成");
		} catch (IOException e) {
			logger.error("抓取181代理失败", e);
		}

		return proxyList;
	}
	
}

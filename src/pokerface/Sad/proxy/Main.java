package pokerface.Sad.proxy;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class Main {
	
	static Logger logger = null;
	static {
		PropertyConfigurator.configure("log4j.properties");
		logger = Logger.getLogger(Main.class);
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws IOException, SQLException{
//		Thread t_checkExisted = new Thread(new CheckExisted());
//		t_checkExisted.setName("t_checkExisted");
//		t_checkExisted.start();
//		
//		Thread t_crawlKuaiDaiLiIntoDB = new Thread(new CrawlIntoDB("getKuaiDaiLi"));
//		t_crawlKuaiDaiLiIntoDB.setName("t_crawlKuaiDaiLiIntoDB");
//		
//		Thread t_crawlDaiLi66IntoDB = new Thread(new CrawlIntoDB("getDaiLi66"));
//		t_crawlDaiLi66IntoDB.setName("t_crawlDaiLi66IntoDB");
//		
//		Thread t_crawlXiCiDaiLiIntoDB = new Thread(new CrawlIntoDB("getFromXiCiDaiLi"));
//		t_crawlXiCiDaiLiIntoDB.setName("t_crawlXiCiDaiLiIntoDB");
//		
		Thread t_crawl89IPIntoDB = new Thread(new CrawlIntoDB("get89IP"));
		t_crawl89IPIntoDB.setName("t_crawl89IPIntoDB");
//		
//		Thread t_crawlIP181IntoDB = new Thread(new CrawlIntoDB("getIP181"));
//		t_crawlIP181IntoDB.setName("t_crawlIP181IntoDB");
//		
//		t_crawlKuaiDaiLiIntoDB.start();
//		t_crawlDaiLi66IntoDB.start();
//		t_crawlXiCiDaiLiIntoDB.start();
		t_crawl89IPIntoDB.start();
//		t_crawlIP181IntoDB.start();
	}
	
}
/**
 * 检查数据库中已有的Proxy，删除已失效的
 *
 */
class CheckExisted implements Runnable{
	static Logger logger = null;
	static {
		PropertyConfigurator.configure("log4j.properties");
		logger = Logger.getLogger(CheckExisted.class);
	}
	@Override
	public void run() {
		logger.info(Thread.currentThread().getName()+"线程启动");
		List<Proxy> dbProxyList = Util.readFromDB();
		logger.info("数据库读取完成");
		Util.markUseless(dbProxyList);
		logger.info("标记失效完成");
		Util.deleteUselessFromDB(dbProxyList);
		logger.info("删除失效完成");
		logger.info(Thread.currentThread().getName()+"线程终止");
	}
	
}
/**
 * 抓取Proxy检测后写入DB
 *
 */
class CrawlIntoDB implements Runnable{
	static Logger logger = null;
	static {
		PropertyConfigurator.configure("log4j.properties");
		logger = Logger.getLogger(CrawlIntoDB.class);
	}
	private String methodName;
	
	public CrawlIntoDB(String methodName)
	{
		this.methodName = methodName;
	}
	
	@Override
	public void run() {
		logger.info(Thread.currentThread().getName()+"线程启动");
		//反射调用Util类的抓取方法
		Class cls = null;
		List<Proxy> proxyList = null;
				
		try {
			System.out.println(methodName);
			cls = Class.forName("pokerface.Sad.proxy.ProxyGetter");
			proxyList = (List<Proxy>) cls.getDeclaredMethod(methodName).invoke(cls);
		} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			logger.error("工具类加载异常", e);
		}
		Util.markUseless(proxyList);
		Util.removeUseless(proxyList);
		if(Util.writeProxyListIntoDB(proxyList))
			logger.info("写入完成");
		
		logger.info(Thread.currentThread().getName()+"线程终止");
	}
}
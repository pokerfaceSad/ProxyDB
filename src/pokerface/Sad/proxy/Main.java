package pokerface.Sad.proxy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import pokerface.Sad.util.DBUtil;


public class Main {
	static AtomicInteger proxyNum = new AtomicInteger(-1);
	static Logger logger = null;
	static {
		PropertyConfigurator.configure("log4j.properties");
		logger = Logger.getLogger(Main.class);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args){
		
		Thread t_checkExisted = new Thread(new CheckExisted());
		t_checkExisted.setName("t_checkExisted");
		synchronized (Main.proxyNum) {
			
			t_checkExisted.start();
			Properties pro = null;
			int proxyNumStandard = 0;
			while(true)
			{
				try {
					Main.proxyNum.wait();
				} catch (InterruptedException e1) {
					logger.error("中断异常",e1);
				}
				try {
					pro = DBUtil.getProperties();
					proxyNumStandard = new Integer(pro.getProperty("proxyNumStandard"));
				} catch (IOException e) {
					logger.error("配置文件读取抓取标准异常，取默认值100",e);
					proxyNumStandard = 100;
				}
				//若代理数量小于标准则开始抓取
				if(Main.proxyNum.get() < proxyNumStandard)
				{
						
					logger.info("代理数量小于"+proxyNumStandard+",开始抓取");
					Thread t_crawlKuaiDaiLiIntoDB = new Thread(new CrawlIntoDB("getKuaiDaiLi"));
					t_crawlKuaiDaiLiIntoDB.setName("t_crawlKuaiDaiLiIntoDB");
					
					Thread t_crawlDaiLi66IntoDB = new Thread(new CrawlIntoDB("getDaiLi66"));
					t_crawlDaiLi66IntoDB.setName("t_crawlDaiLi66IntoDB");
					
					Thread t_crawlXiCiDaiLiIntoDB = new Thread(new CrawlIntoDB("getFromXiCiDaiLi"));
					t_crawlXiCiDaiLiIntoDB.setName("t_crawlXiCiDaiLiIntoDB");
					
					Thread t_crawl89IPIntoDB = new Thread(new CrawlIntoDB("get89IP"));
					t_crawl89IPIntoDB.setName("t_crawl89IPIntoDB");
					
					Thread t_crawlIP181IntoDB = new Thread(new CrawlIntoDB("getIP181"));
					t_crawlIP181IntoDB.setName("t_crawlIP181IntoDB");
					
					t_crawlKuaiDaiLiIntoDB.start();
					t_crawlDaiLi66IntoDB.start();
					t_crawlXiCiDaiLiIntoDB.start();
					t_crawl89IPIntoDB.start();
					t_crawlIP181IntoDB.start();
				
					//若有抓取线程未结束，则主线程阻塞
					while(t_crawlKuaiDaiLiIntoDB.isAlive() || t_crawlDaiLi66IntoDB.isAlive() || t_crawlXiCiDaiLiIntoDB.isAlive() || 
							t_crawl89IPIntoDB.isAlive() || t_crawlIP181IntoDB.isAlive());
					logger.info("所有线程抓取完成");
				}
			}
		}
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
		while(true)
		{
			synchronized (Main.proxyNum) {
					logger.info(Thread.currentThread().getName()+"线程启动");
					List<Proxy> dbProxyList = Util.readFromDB();
					logger.info("数据库读取完成");
					Util.markUseless(dbProxyList);
					logger.info("标记失效完成");
					Util.deleteUselessFromDB(dbProxyList);
					logger.info("删除失效完成");
					Main.proxyNum.set( dbProxyList.size());
					logger.info("更新代理池中代理数量为"+Main.proxyNum.get());
					Main.proxyNum.notify(); 
					logger.info("唤醒主线程");
				}
				
				logger.info(Thread.currentThread().getName()+"线程休眠");
				try {
					Thread.sleep(1000*60*30);	//休眠半小时
				} catch (InterruptedException e) {
					logger.error("中断异常",e);
				} 
		}
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
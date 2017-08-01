package pokerface.Sad.proxy;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import pokerface.Sad.util.DBUtil;
import pokerface.Sad.util.HttpUtil;

public class Util {

	static Logger logger = null;
	static {
		PropertyConfigurator.configure("log4j.properties");
		logger = Logger.getLogger(Util.class);
	}

	public static void proxyTest(Proxy proxy) throws ConnectException {

		// 获取本地ip
		String localIp = null;
		boolean flag = true;
		for (int i=0;i<20;i++) {
			try {
				localIp = getIP(analysisTestMsg(HttpUtil.get(
						"http://1212.ip138.com/ic.asp", null, null)));
				Thread.sleep(1000);
			} catch (Exception e) {
				if(i==19)
				{
					logger.error("获取本地IP失败，请检查网络.....", e);
					throw new ConnectException("网络连接异常");
				}
			}
		}

		String html = null;
		try {
			html = HttpUtil.get("http://1212.ip138.com/ic.asp", proxy.ip,
					proxy.port);
		} catch (IOException e) {
			// 若无法获取相应则代理不可用
			logger.info("Result: " + proxy.ip + ":" + proxy.port
					+ " 不可用	Reason: 无法获取响应");
			proxy.available = false;
			return;
		} catch (NumberFormatException e) {
			logger.info("Result: " + proxy.ip + ":" + proxy.port
					+ " 不可用	Reason: 格式错误", e);
		}
		if (html == null || html.isEmpty()) {
			// 若响应为空则代理不可用
			logger.info("Result: " + proxy.ip + ":" + proxy.port
					+ " 不可用	Reason: 响应为空");
			proxy.available = false;
			return;
		} else {
			String testResult = analysisTestMsg(html);
			if (getIP(testResult) == null) {
				// 若响应信息不正确则代理不可用
				logger.info("Result: " + proxy.ip + ":" + proxy.port
						+ " 不可用	Reason: 响应不正确");
				proxy.available = false;
				return;
			}
			// 比较本地ip与代理ip
			if (localIp.equals(getIP(testResult))) {
				proxy.isAnonymous = "透明";
				proxy.available = false; //滤除透明的代理
				return;
			} else {
				proxy.isAnonymous = "匿名";
				proxy.location = getLocation(testResult);
			}
			// 判断是否支持https
			try {
				String response = HttpUtil.get("https://www.zhihu.com/",
						proxy.ip, proxy.port);
				if (response.indexOf("知乎") != -1)
				{
					logger.info(proxy.ip + ":" + proxy.port + " 支持Https");
					proxy.isHttpsSupported = true;
				} else {
					logger.info(proxy.ip + ":" + proxy.port + " 不支持Https");
					proxy.isHttpsSupported = false;
				}
			} catch (IOException e) {
				logger.info(proxy.ip + ":" + proxy.port + " 不支持Https");
				proxy.isHttpsSupported = false;
			}

			proxy.available = true;
			logger.info("Result: " + proxy.ip + ":" + proxy.port + " 可用	");
		}
	}

	/**
	 * 解析出测试结果
	 * 
	 * @param html
	 * @return
	 */
	public static String analysisTestMsg(String html) {
		Document doc = Jsoup.parse(html);
		return doc.select("body > center").text();
	}

	/**
	 * 解析出IP
	 * 
	 * @param TestResult
	 * @return
	 */
	public static String getIP(String TestResult) {
		Pattern p = Pattern.compile(".+?\\[(.+?)\\].+?");
		Matcher m = p.matcher(TestResult);
		String ip = null;
		if (m.find()) {
			ip = m.group(1);
		}
		return ip;
	}

	/**
	 * 解析出location
	 * 
	 * @param TestResult
	 * @return
	 */
	public static String getLocation(String TestResult) {
		Pattern p = Pattern.compile(".+?来自：(.+)");
		Matcher m = p.matcher(TestResult);
		String location = null;
		if (m.find()) {
			location = m.group(1);
		}
		return location;
	}

	/**
	 * 将proxyList中无效的Proxy标记
	 * 
	 * @param proxyList
	 * @throws ConnectException 
	 */
	public static void markUseless(List<Proxy> proxyList) throws ConnectException {
		for (Proxy proxy : proxyList) {
			logger.info(proxy.ip + ":" + proxy.port + "开始检测");
			boolean httpsSupported = proxy.isHttpsSupported;
			Util.proxyTest(proxy);
			if (proxy.available == true && httpsSupported == proxy.isHttpsSupported)
				logger.info(proxy.ip + ":" + proxy.port + "检测完成，可用");
			else
				logger.info(proxy.ip + ":" + proxy.port + "检测完成，不可用");
		}
	}

	/**
	 * 对返回的proxyList逐个进行检验，并移除不可用项
	 * 
	 * @param proxyList
	 */
	public static void removeUseless(List<Proxy> proxyList) {

		Iterator<Proxy> it = proxyList.iterator();
		Proxy proxy = null;
		while (it.hasNext()) {

			proxy = it.next();
			if (proxy.available == false)
				it.remove();
		}
	}

	public static boolean writeProxyListIntoDB(List<Proxy> proxyList) {

		// 获取数据库连接
		Connection conn = null;
		PreparedStatement ps = null;
		boolean result = true;
		try {
			conn = DBUtil.getConn();
			ps = conn.prepareStatement(DBUtil.getProperties().getProperty(
					"sql_insert"));
			for (Proxy proxy : proxyList) {
				DBUtil.beginTransaction(conn);
				ps.setString(1, proxy.ip);
				ps.setString(2, proxy.port);
				ps.setString(3, proxy.isAnonymous);
				ps.setString(4, proxy.isHttpsSupported ? "true" : "false");
				ps.setString(5, proxy.location);
				try {
					ps.executeUpdate();
					logger.info(proxy.ip + ":" + proxy.port + " 写入成功");
				} catch (MySQLIntegrityConstraintViolationException e) { // 若已有重复则跳过
					logger.info(proxy.ip + ":" + proxy.port + " 已存在");
				}
				DBUtil.commitTransaction(conn);
			}
		} catch (IOException | SQLException e) {
			logger.fatal("写入数据库异常", e);
			result = false;
			DBUtil.rollback(conn);

		} finally {
			DBUtil.close(ps, conn, null);
		}
		return result;

	}

	/**
	 * 从数据库取出已有代理
	 * 
	 * @return
	 */
	public static List<Proxy> readFromDB() {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Proxy> proxyList = new LinkedList<Proxy>();
		try {
			conn = DBUtil.getConn();
			ps = conn.prepareStatement(DBUtil.getProperties().getProperty(
					"sql_select"));
			rs = ps.executeQuery();
			Proxy proxy = null;
			while (rs.next()) {
				proxy = new Proxy(rs.getString(1), rs.getString(2));
				proxyList.add(proxy);
			}
		} catch (IOException | SQLException e) {
			logger.error("读取数据库异常", e);
		}
		return proxyList;
	}

	/**
	 * 依照检验过的数据库中已存在的Proxy集合，将不可用的Proxy从数据库中移除
	 * 
	 * @param proxyList
	 */
	public static void deleteUselessFromDB(List<Proxy> proxyList) {

		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBUtil.getConn();
			ps = conn.prepareStatement(DBUtil.getProperties().getProperty(
					"sql_delete"));
			
			Iterator<Proxy> it = proxyList.iterator();
			Proxy proxy = null;
			while(it.hasNext())
			{
				proxy = it.next();
				if (proxy.available == false) {
					ps.setString(1, proxy.ip);
					ps.setString(2, proxy.port);
					if (ps.executeUpdate() == 1) {
						logger.info("成功移除失效Proxy " + proxy.ip + ":"
								+ proxy.port);
					}
					it.remove();
				}
			}
		} catch (IOException | SQLException e) {
			logger.error("连接数据库失败", e);
		} finally {
			DBUtil.close(ps, conn, null);
		}

	}
}

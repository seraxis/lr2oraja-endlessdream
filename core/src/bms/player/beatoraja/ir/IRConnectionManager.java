package bms.player.beatoraja.ir;

import bms.tool.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * IRConnectionの管理用クラス
 * 
 * @author exch
 */
public class IRConnectionManager {
	private static final Logger logger = LoggerFactory.getLogger(IRConnectionManager.class);
	
	/**
	 * 検出されたIRConnection
	 */
	private static Class<IRConnection>[] irconnections;


	/**
	 * 利用可能な全てのIRConnectionの名称を返す
	 * 
	 * @return IRConnectionの名称
	 */
	public static String[] getAllAvailableIRConnectionName() {
		Class<IRConnection>[] irclass = getAllAvailableIRConnection();
		String[] names = new String[irclass.length];
		for (int i = 0; i < names.length; i++) {
			try {
				names[i] = irclass[i].getField("NAME").get(null).toString();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return names;
	}

	/**
	 * 名称に対応したIRConnectionインスタンスを返す
	 * 
	 * @param name
	 *            IRCOnnectionの名称
	 * @return 対応するIRConnectionインスタンス。存在しない場合はnull
	 */
	public static IRConnection getIRConnection(String name) {
		Class<IRConnection> irclass = getIRConnectionClass(name);
		if(irclass != null) {
			try {
				return (IRConnection) irclass.getDeclaredConstructor().newInstance();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static Class<IRConnection> getIRConnectionClass(String name) {
		if (name == null || name.length() == 0) {
			return null;
		}
		Class<IRConnection>[] irclass = getAllAvailableIRConnection();
		for (int i = 0; i < irclass.length; i++) {
			try {
				if (name.equals(irclass[i].getField("NAME").get(null).toString())) {
					return irclass[i];
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private static Class<IRConnection>[] getAllAvailableIRConnection() {
		if(irconnections != null) {
			return irconnections;
		}
		List<Class<IRConnection>> classes = new ArrayList<Class<IRConnection>>();

		try {
			String customIRDirectory = System.getProperty("customIRDirectory");
			classes = customIRDirectory == null
					? fetchIRConnectionFromClassPath()
					: fetchIRConnectionFromCustomDirectory(customIRDirectory);
		} catch (Exception e) {
			logger.error("Failed to load ir connections: ", e);
		}

		irconnections = classes.toArray(new Class[classes.size()]);
		return irconnections;
	}

	/**
	 * Try to fetch possible ir connections from class path, this is the default behavior of beatoraja
	 */
	private static List<Class<IRConnection>> fetchIRConnectionFromClassPath() throws ClassNotFoundException, IOException {
		List<Class<IRConnection>> connections = new ArrayList<>();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Enumeration<URL> urlEnums = loader.getResources("bms/player/beatoraja/ir");
		while (urlEnums.hasMoreElements()) {
			URL candidate = urlEnums.nextElement();
			try {
				if (candidate.getProtocol().equals("jar")) {
					JarURLConnection connection = (JarURLConnection) candidate.openConnection();
					try (JarFile jarFile = connection.getJarFile()) {
						connections.addAll(fetchIRConnectionFromJarFile(loader, jarFile));
					} catch (Exception e) {
						logger.error("Failed to load ir connections from {}: {}", candidate, e.getMessage());
					}
				} else if (candidate.getProtocol().equals("file")) {
					// Below code is inherited from upstream, I don't know what it's used for
					File dir = new File(candidate.getPath());
					String[] list = dir.list();
					if (list != null) {
						for (String path : list) {
							if (path.endsWith(".class")) {
								Class<?> clazz = loader.loadClass("bms.player.beatoraja.ir." + path.substring(0, path.length() - 6));
								if (clazz != null && validateIRConnectionClass(clazz)) {
									connections.add((Class<IRConnection>) clazz);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error("Failed to load ir connection from url({}): {}", candidate, e.getMessage());
				throw e;
			}
		}
		return connections;
	}

	private static List<Class<IRConnection>> fetchIRConnectionFromCustomDirectory(String customDirectory) throws IOException, ClassNotFoundException {
		File irDir = new File(customDirectory);
		File[] rawJarFiles = irDir.listFiles((dir, name) -> name.endsWith(".jar"));
		if (rawJarFiles == null || rawJarFiles.length == 0) {
			return Collections.emptyList();
		}
		List<JarFile> jarFiles = new ArrayList<>();
		List<URL> urls = new ArrayList<>();
		for (File rawJarFile : rawJarFiles) {
			jarFiles.add(new JarFile(rawJarFile));
			urls.add(rawJarFile.toURI().toURL());
		}
		URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]));
		List<Class<IRConnection>> connections = new ArrayList<>();
		for (JarFile jarFile : jarFiles) {
			connections.addAll(fetchIRConnectionFromJarFile(loader, jarFile));
		}
		return connections;
	}

	private static List<Class<IRConnection>> fetchIRConnectionFromJarFile(ClassLoader loader, JarFile jarFile) throws ClassNotFoundException {
		List<Class<IRConnection>> ret = new ArrayList<>();
		Enumeration<JarEntry> jarEnum = jarFile.entries();
		while (jarEnum.hasMoreElements()) {
			JarEntry jarEntry = jarEnum.nextElement();
			String path = jarEntry.getName();
			if (path.startsWith("bms/player/beatoraja/ir/") && path.endsWith(".class")) {
				Class<?> candidate = loader.loadClass("bms.player.beatoraja.ir."
						+ path.substring(path.lastIndexOf("/") + 1, path.length() - 6));
				if (candidate != null && validateIRConnectionClass(candidate)) {
					ret.add((Class<IRConnection>) candidate);
				}
			}
		}
		return ret;
	}

	private static boolean validateIRConnectionClass(Class<?> clazz) {
		if (Arrays.stream(clazz.getInterfaces()).noneMatch(inf -> inf == IRConnection.class)) {
			return false;
		}
		return Arrays.stream(clazz.getFields()).anyMatch(f -> f.getName().equals("NAME"));
	}

	/**
	 * IRのホームURLを取得する
	 * @param name IR名
	 * @return IRのホームURL。存在しない場合はnull
	 */
	public static String getHomeURL(String name) {
		Class irclass = getIRConnectionClass(name);
		if(irclass != null) {
			try {
				Object result = irclass.getField("HOME").get(null);
				if(result != null) {
					return result.toString();
				}
			} catch (Throwable e) {
			}
		}
		return null;
	}

}

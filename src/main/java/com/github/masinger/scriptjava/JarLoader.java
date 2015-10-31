package com.github.masinger.scriptjava;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * A loader to load classes from a given JAR file.
 * @author Marcel Singer
 *
 */
public class JarLoader {

	/**
	 * Holds the jar file to load.
	 */
	private final JarFile jar;
	/**
	 * A map holding the JarEntrys for a class name.
	 */
	private final Map<String, JarEntry> classes = new HashMap<String, JarEntry>();
	/**
	 * The URLClass loader to use.
	 */
	private final URLClassLoader loader;
	/**
	 * Holds the loaded classes for a class name.
	 */
	private final Map<String, Class<?>> loaded = new HashMap<String, Class<?>>();
	/**
	 * Holds the jar file.
	 */
	private final File jarFile;

	
	/**
	 * Creates a new instance of {@link JarLoader}.
	 * @param jarFile The file of the jar to load.
	 * @throws IOException If an I/O error occurs.
	 */
	public JarLoader(ClassLoader parent, File jarFile) throws IOException {
		this.jarFile = jarFile;
		this.jar = new JarFile(jarFile);
		URL[] urls = { new URL("jar:file:" + jarFile.getAbsolutePath() + "!/") };
		loader = URLClassLoader.newInstance(urls, parent);
		load();
	}
	
	
	/**
	 * Returns the names of all classes that are available inside the JAR.
	 * @return The names of all available classes.
	 */
	public Iterable<String> getNames() {
		return classes.keySet();
	}
	
	/**
	 * Returns the names of all loaded classes.
	 * @return The names of all loaded classes.
	 */
	public Iterable<String> getLoadedNames(){
		return loaded.keySet();
	}

	/**
	 * Loads the names of all available classes.
	 */
	private void load() {
		Enumeration<JarEntry> entrys = jar.entries();
		JarEntry current;
		while (entrys.hasMoreElements()) {
			current = entrys.nextElement();
			if (current.getName().endsWith(".class") && !current.isDirectory()) {
				classes.put(
						current.getName()
								.substring(0, current.getName().length() - 6)
								.replace('/', '.'), current);
			}
		}
	}

	/**
	 * Loads the class with the given name from the jar.
	 * @param name The name of the class to load.
	 * @return The class with the given name.
	 * @throws ClassNotFoundException If there is no class for the given name.
	 */
	public Class<?> load(String name) throws ClassNotFoundException {
		if (!classes.containsKey(name))
			return null;
		if (loaded.containsKey(name))
			return loaded.get(name);

		Class<?> cl = loader.loadClass(name);
		if (check(name, cl)) {
			loaded.put(name, cl);
			return cl;
		}
		return null;
	}

	/**
	 * Checks if the given class should be loaded or skipped.
	 * @param name The name of the class to check.
	 * @param toCheck The class to check.
	 * @return {@code true}, if the given class should be loaded. {@code false}, if the given class should be skipped.
	 */
	protected boolean check(String name, Class<?> toCheck) {
		return true;
	}

	/**
	 * Loads all available classes and returns them.
	 * @return All available classes.
	 * @throws ClassNotFoundException If a class could not be found.
	 */
	public Collection<Class<?>> loadAll() throws ClassNotFoundException {
		for (String className : classes.keySet()) {
			load(className);
		}
		return getLoaded();
	}

	/**
	 * Loads all available classes and returns them. If an exception occurs the causing class is skipped.
	 * @param printStackTrace {@code true} if the stack trace of occurring exceptions should be printed to the standard error stream.
	 * @return All available classes.
	 */
	public Collection<Class<?>> loadAllIgnoreExceptions(boolean printStackTrace) {
		for (String className : classes.keySet()) {
			try {
				load(className);
			} catch (Throwable th) {
				if (printStackTrace)
					th.printStackTrace();
			}
		}
		return getLoaded();
	}

	/**
	 * Returns all currently loaded classes.
	 * @return All currently loaded classes.
	 */
	public Collection<Class<?>> getLoaded() {
		return loaded.values();
	}

	/**
	 * Checks if the class with the given name is currently loaded.
	 * @param name The name of the class.
	 * @return {@code true} if the class with the given name is already loaded.
	 */
	public boolean isLoaded(String name) {
		return loaded.containsKey(name);
	}

	/**
	 * Releases all currently loaded classes.
	 */
	public void unload() {
		loaded.clear();
	}
	
	public ClassLoader getLoader(){
		return loader;
	}

	/**
	 * Releases all currently loaded classes and names. It also closes the underlying class loader.
	 * @throws IOException If an I/O error occurs.
	 */
	public void release() throws IOException {
		unload();
		classes.clear();
		closeLoader();
	}

	/**
	 * Closes this and the underlying loader.
	 * @throws IOException If an I/O error occurs.
	 */
	public void closeLoader() throws IOException {
		loader.close();
		jar.close();
		
	}

	/**
	 * Returns the JAR-file.
	 * @return The JAR-file.
	 */
	public File getFile() {
		return jarFile;
	}

}
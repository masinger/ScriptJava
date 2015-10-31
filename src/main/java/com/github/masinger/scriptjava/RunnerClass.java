package com.github.masinger.scriptjava;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javassist.ClassPool;

public class RunnerClass {

	public static ExtendedClassPool CLASS_POOL = new ExtendedClassPool(ClassPool.getDefault());
	public static final Map<String, Allocation> allocations = new HashMap<String, Allocation>();
	public static final List<Method> STATIC_IMPORTS = new ArrayList<Method>();
	public static ClassLoader LOADER = RunnerClass.class.getClassLoader();
	private static final List<String> PACKAGES = new ArrayList<String>();
	public static final List<String> CACHE = new ArrayList<String>();

	public RunnerClass() {
	}

	public static void importPackage(String name) {
		if (PACKAGES.contains(name))
			return;
		PACKAGES.add(name);
		CLASS_POOL = new ExtendedClassPool(CLASS_POOL);
		for (String pkg : PACKAGES) {
			CLASS_POOL.importPackage(pkg);
		}
	}

	public static void allocNull(String name, Class<?> type) {
		allocations.put(name, new Allocation(type));
	}

	public static void alloc(String name, int v) {
		allocations.put(name, new Allocation(int.class));
		allocations.get(name).set(v);
	}

	public static void alloc(String name, long v) {
		allocations.put(name, new Allocation(long.class));
		allocations.get(name).set(v);
	}

	public static void alloc(String name, byte v) {
		allocations.put(name, new Allocation(byte.class));
		allocations.get(name).set(v);
	}

	public static void alloc(String name, boolean v) {
		allocations.put(name, new Allocation(boolean.class));
		allocations.get(name).set(v);
	}

	public static void alloc(String name, double v) {
		allocations.put(name, new Allocation(double.class));
		allocations.get(name).set(v);
	}

	public static void alloc(String name, float v) {
		allocations.put(name, new Allocation(float.class));
		allocations.get(name).set(v);
	}

	public static void alloc(String name, Object o) {
		allocations.put(name, new Allocation(o.getClass()));
		allocations.get(name).set(o);
	}

	public static void alloc(String name, Class<?> type, Object o) {
		if (o != null && !type.isAssignableFrom(o.getClass())) {
			System.err.println(
					String.format(" ERROR: %s is not assignable from %s", type.getName(), o.getClass().getName()));
			return;
		}
		allocations.put(name, new Allocation(type));
		allocations.get(name).set(o);
	}

	public static void ctype(String name, Class<?> newType) {
		if (!allocations.containsKey(name))
			allocNull(name, newType);
		else
			alloc(name, newType, allocations.get(name).get());
	}

	public static void dealloc(String name) {
		if (allocations.containsKey(name))
			allocations.remove(name);
		else
			System.err.println(" -> ERROR: No such variable!");
	}

	public static void clear() {
		for (int i = 0; i < 1000; i++)
			System.out.println();
	}

	public static void println(Object o) {
		System.out.println(o);
	}

	public static void println(int i) {
		System.out.println(i);
	}

	public static void println(boolean b) {
		System.out.println(b);
	}

	public static void println(double d) {
		System.out.println(d);
	}

	public static void importStatic(String className, int i) {
		Class<?> cl;
		try {
			cl = RunnerClass.class.getClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {
			System.err.println(String.format(" -> Class %s was not found...", className));
			return;
		}
		importStatic(cl, i);
	}

	public static void importMaven(String groupId, String artifact, String version) {
		String downloadUrl = String.format("http://central.maven.org/maven2/%s/%s/%s/%s-%s.jar",
				groupId.replace('.', '/'), artifact.replace('.', '/'), version, artifact, version);
		System.out.print(String.format(" -> Loading from %s...", downloadUrl));
		try {
			File local = File.createTempFile("ref_", ".jar");
			App.EXECUTION_FILES.add(local);
			URL url = new URL(downloadUrl);
			ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			FileOutputStream fos = new FileOutputStream(local);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.flush();
			fos.close();
			rbc.close();
			System.out.println(" Done!");
			System.out.print(" -> Importing classes...");
			CLASS_POOL.insertClassPath(local.getAbsolutePath());
			JarLoader jl = new JarLoader(LOADER, local);
			Collection<Class<?>> classes = jl.loadAll();
			LOADER = jl.getLoader();
			jl.closeLoader();
			local.deleteOnExit();
			System.out.println(" Done!");
			if (!classes.isEmpty()) {
				Class<?> cl = null;
				Class<?> current;
				Iterator<Class<?>> it = classes.iterator();
				while (it.hasNext()) {
					current = it.next();
					if (cl == null || current.getPackage().getName().length() < cl.getPackage().getName().length())
						cl = current;
				}
				if (cl != null) {
					System.out.print(String.format(" -> Try to import package '%s'...", cl.getPackage().getName()));
					importPackage(cl.getPackage().getName());
					System.out.println(" Done!");
				}
			}
		} catch (FileNotFoundException ex) {
			System.out.println();
			System.err.println(" ERROR: Dependency not found!");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void importMaven(String gradleIdentifier) {
		String[] args = gradleIdentifier.split(":");
		if (args.length != 3) {
			System.err.println(" ERROR: Invalid identifier!");
			return;
		}
		importMaven(args[0], args[1], args[2]);
	}

	public static void importStatic(Class<?> cl, int i) {
		List<Method> tmpMethods = new ArrayList<Method>();
		for (Method m : cl.getMethods()) {
			if (!Modifier.isStatic(m.getModifiers()))
				continue;
			tmpMethods.add(m);
			if (i == -1)
				System.out.println(String.format(" -> %d: %s", tmpMethods.size() - 1, getString(m)));
		}
		if (i != -1) {
			if (i < 0 || i >= tmpMethods.size()) {
				System.err.println("ERROR: Index is invalid!");
				return;
			}
			if (!STATIC_IMPORTS.contains(tmpMethods.get(i)))
				STATIC_IMPORTS.add(tmpMethods.get(i));
		}
	}

	public static void importStatic(Class<?> cl) {
		importStatic(cl, -1);
	}

	public static void importStatic(String className) {
		importStatic(className, -1);
	}

	public static void drop(int index) {
		if (index < 0 || index >= CACHE.size()) {
			System.err.println(" ERROR: The given index is invalid!");
			return;
		}
		CACHE.set(index, null);
	}

	public static void drop() {
		if (CACHE.size() == 0)
			return;
		drop(CACHE.size() - 1);
	}

	public static void writeScript(String file) throws IOException {
		File f = new File(file);
		if (!f.exists()) {
			File parent = f.getParentFile();
			if (parent != null && !parent.exists())
				parent.mkdirs();
			f.createNewFile();
		}
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f));
		for (String cmd : CACHE) {
			if (cmd == null)
				continue;
			writer.write(cmd + "\r\n");
		}
		writer.flush();
		writer.close();
	}

	public static void executeScript(String file, boolean showOutput) throws Exception {
		File f = new File(file);
		if (!f.exists()) {
			System.err.println(String.format(" ERROR: The script at '%s' does not exist!", f.getAbsolutePath()));
			return;
		}
		FileInputStream in = new FileInputStream(f);
		App.runCMDMode(in, true, showOutput);
		in.close();
	}

	public static void executeScript(String file) throws Exception {
		executeScript(file, false);
	}

	public static void exit(int code) throws IOException {
		App.onExit();
		System.exit(code);
	}

	public static void exit() throws IOException {
		exit(0);
	}

	public static void vars() {
		for (String fName : allocations.keySet()) {
			System.out.println(String.format("%s --> %s ==> %s", fName,
					App.getTypeString(allocations.get(fName).getType()), allocations.get(fName).get()));
		}
	}

	public static void methods(Object o) {
		if (o == null) {
			System.out.println("ERROR: Argument is null!");
			return;
		}
		Class<?> cl = o.getClass();
		Method[] methods = cl.getMethods();
		Arrays.sort(methods, new Comparator<Method>() {

			public int compare(Method o1, Method o2) {
				int res = o1.getName().compareTo(o2.getName());
				if (res != 0)
					return res;
				return ((Integer) o1.getParameterCount()).compareTo((Integer) o2.getParameterCount());
			}
		});
		for (Method m : methods) {
			System.out.println(String.format(" -> %s", getString(m)));
		}
	}

	private static String getString(Method m) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(".%s(", m.getName()));
		int i = 0;
		for (Parameter p : m.getParameters()) {
			if (i > 0)
				sb.append(", ");
			sb.append(String.format("%s %s", App.getTypeString(p.getType()), p.getName()));
			i++;
		}
		sb.append(")");
		if (m.getReturnType() != null && m.getReturnType() != Void.TYPE) {
			sb.append(String.format(" : %s", m.getReturnType().getName()));
		}
		return sb.toString();
	}

}

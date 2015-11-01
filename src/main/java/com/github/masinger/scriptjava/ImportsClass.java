package com.github.masinger.scriptjava;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ImportsClass extends SystemShortcuts {

	private static final List<String> PACKAGES = new ArrayList<String>();
	
	public static void importPackage(String name) {
		if (PACKAGES.contains(name))
			return;
		PACKAGES.add(name);
		CLASS_POOL = new ExtendedClassPool(CLASS_POOL);
		for (String pkg : PACKAGES) {
			CLASS_POOL.importPackage(pkg);
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

	public static void importStatic(Class<?> cl) {
		importStatic(cl, -1);
	}

	public static void importStatic(String className) {
		importStatic(className, -1);
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

	
}

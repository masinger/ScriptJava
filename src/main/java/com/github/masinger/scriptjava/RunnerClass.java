package com.github.masinger.scriptjava;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

public class RunnerClass extends ImportsClass {

	public RunnerClass() {
	}

	public static void clear() {
		for (int i = 0; i < 1000; i++)
			System.out.println();
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

}

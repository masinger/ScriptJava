package com.github.masinger.scriptjava;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;

public class App {

	public static final List<File> EXECUTION_FILES = new ArrayList<File>();

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			runCMDMode(System.in, false, true);
			onExit();
			return;
		}
		boolean doesOutput = showOutput(args);
		File f = new File(args[0]);
		if (!f.exists()) {
			System.err.println("The given script-file doesn't exist.");
			System.exit(-1);
		}
		if (doesOutput) {
			System.err.println(String.format("Script path: %s", f.getAbsolutePath()));
			System.out.println("Executing script...");
			System.out.println();
			System.out.println();
		}

		runCMDMode(new FileInputStream(f), true, doesOutput);
		onExit();

	}

	public static void onExit() throws IOException {
		RunnerClass.CLASS_POOL.close();
		for (File f : EXECUTION_FILES) {
			Files.delete(f.toPath());
		}
	}

	private static boolean showOutput(String[] args) {
		for (String arg : args) {
			if (arg.toLowerCase().equals("--showoutput"))
				return true;
		}
		return false;
	}

	public static String getTypeString(Class<?> cl) {
		if (cl.isArray())
			return String.format("%s[]", getTypeString(cl.getComponentType()));
		return cl.getName();
	}

	public static String getMethodDeclaration(Method m) {
		StringBuilder sb = new StringBuilder();
		sb.append("public static ");
		if (m.getReturnType() != null && m.getReturnType() != Void.class) {
			sb.append(getTypeString(m.getReturnType()));
		} else
			sb.append("void");

		sb.append(String.format(" %s(", m.getName()));
		int i = 0;
		for (Parameter p : m.getParameters()) {
			if (i > 0)
				sb.append(", ");
			sb.append(String.format("%s %s", getTypeString(p.getType()), p.getName()));
			i++;
		}
		sb.append(")");
		return sb.toString();
	}

	public static String getMethodCall(Method m) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (Parameter p : m.getParameters()) {
			if (i > 0)
				sb.append(", ");
			sb.append(p.getName());
			i++;
		}
		if (m.getReturnType() != null && m.getReturnType() != Void.TYPE) {
			return String.format("return %s.%s(%s);", getTypeString(m.getDeclaringClass()), m.getName(), sb.toString());
		} else
			return String.format("%s.%s(%s);", getTypeString(m.getDeclaringClass()), m.getName(), sb.toString());
	}

	private static int i = 0;
	private static int j = 0;

	public static void runCMDMode(InputStream in, boolean isFile, boolean showOutput) throws Exception {
		if (showOutput) {
			System.out.println("ScriptJava Console v0.1");
			System.out.println("");
		}
		Scanner scanner = new Scanner(in);
		String line = "";
		CtClass cl;
		CtClass runnerClass = RunnerClass.CLASS_POOL.get("com.github.masinger.scriptjava.RunnerClass");
		Class<?> systemClass;
		String tmp;
		boolean isBlockInput = false;
		if (showOutput)
			System.out.print(String.format(":%d > ", j));
		while (scanner.hasNextLine()) {
			tmp = scanner.nextLine();
			if (isBlockInput) {
				if (tmp.equals("#}")) {
					isBlockInput = false;
					tmp = line;
				} else {
					line = String.format("%s\r\n%s", line, tmp);
				}
			} else {
				if (tmp.equals("#{")) {
					isBlockInput = true;
					line = "";
				}
			}
			if (isBlockInput)
				continue;
			i++;
			line = tmp;
			if (!line.endsWith(";") && !line.isEmpty())
				line += ";";
			if (line.startsWith("#")) {
				line = line.substring(1);
			} else {
				j++;
				RunnerClass.CACHE.add(line);
			}
			if (isFile && showOutput)
				System.out.println(line);
			cl = RunnerClass.CLASS_POOL.makeClass(String.format("TempClass%d", i));
			try {
				cl.setSuperclass(runnerClass);
				for (String fieldName : RunnerClass.allocations.keySet()) {
					CtField field = CtField.make(String.format("public static %s %s;",
							getTypeString(RunnerClass.allocations.get(fieldName).getType()), fieldName), cl);
					cl.addField(field);
				}
				for (Method m : RunnerClass.STATIC_IMPORTS) {
					CtMethod ctMethod = CtMethod
							.make(String.format("%s{ %s }", getMethodDeclaration(m), getMethodCall(m)), cl);
					cl.addMethod(ctMethod);
				}
				cl.addMethod(CtMethod.make(String.format("public static void run(){ %s }", line), cl));
				systemClass = cl.toClass(RunnerClass.LOADER);
				for (String fieldName : RunnerClass.allocations.keySet()) {
					systemClass.getField(fieldName).set(null, RunnerClass.allocations.get(fieldName).get());
				}
				systemClass.getMethod("run").invoke(null);
				for (String fieldName : RunnerClass.allocations.keySet()) {
					try {
						RunnerClass.allocations.get(fieldName).set(systemClass.getField(fieldName).get(null));
					} catch (NoSuchFieldException ex) {

					}
				}
			} catch (CannotCompileException e) {
				System.err.println(e.getMessage());
				System.err.println();
				System.err.println();
			} catch (InvocationTargetException e) {
				Throwable th = e.getTargetException();
				th.printStackTrace();
				if (th instanceof IllegalAccessError) {
					System.err.println();
					System.err.println(
							" HELP: You might have accessed a class that is not available from your current scope.\n"
									+ "       This can occur if you directly allocate a (return-)value of a method or field.\n\n"
									+ "       Try to use alloc(name, type, value) where type is the return type of the called/accessed method/field.");
				}
				System.err.println();
				System.err.println();
			} catch (IllegalAccessError e) {
				e.printStackTrace();
				System.err.println();
				System.err.println(
						" HELP: You might have accessed a class that is not available from your current scope.\n"
								+ "       This can occur if you directly allocate a (return-)value of a method or field.\n\n"
								+ "       Try to use alloc(name, type, value) where type is the return type of the called/accessed method/field.");
			}
			if (showOutput)
				System.out.print(String.format(":%d > ", j));
		}
		scanner.close();
	}
}

package com.github.masinger.scriptjava;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.ClassPool;

public class BasicRunnerClass {

	public static ExtendedClassPool CLASS_POOL = new ExtendedClassPool(ClassPool.getDefault());
	public static final Map<String, Allocation> allocations = new HashMap<String, Allocation>();
	public static final List<Method> STATIC_IMPORTS = new ArrayList<Method>();
	public static final List<String> CACHE = new ArrayList<String>();
	public static ClassLoader LOADER = RunnerClass.class.getClassLoader();

	
	protected static String getString(Method m) {
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
	
	public static void vars() {
		for (String fName : allocations.keySet()) {
			System.out.println(String.format("%s --> %s ==> %s", fName,
					App.getTypeString(allocations.get(fName).getType()), allocations.get(fName).get()));
		}
	}
	
}

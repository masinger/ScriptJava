package com.github.masinger.scriptjava;

import java.io.IOException;

public class SystemShortcuts extends BasicRunnerClass {

	
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
	
	public static void println(float f){
		System.out.println(f);
	}
	
	public static void println(short s){
		System.out.println(s);
	}
	
	public static void println(byte b){
		System.out.println(b);
	}
	
	public static void exit(int code) throws IOException {
		App.onExit();
		System.exit(code);
	}

	public static void exit() throws IOException {
		exit(0);
	}
	
	
}

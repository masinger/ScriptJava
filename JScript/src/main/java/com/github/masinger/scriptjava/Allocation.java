package com.github.masinger.scriptjava;

public class Allocation {

	private Object value;
	
	private final Class<?> type;
	
	public Allocation(Class<?> type) {
		this.type = type;
	}
	
	public Object get(){ return value; }
	public void set(Object value){this.value = value; }

	public Class<?> getType(){
		return type;
	}
	
}

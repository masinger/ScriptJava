package com.github.masinger.scriptjava;

import java.util.ArrayList;
import java.util.List;

import javassist.ClassPath;
import javassist.ClassPool;
import javassist.NotFoundException;

public class ExtendedClassPool extends ClassPool {

	private final List<ClassPath> classPaths = new ArrayList<ClassPath>();
	
	public ExtendedClassPool() {
	}

	public ExtendedClassPool(boolean useDefaultPath) {
		super(useDefaultPath);
	}

	public ExtendedClassPool(ClassPool parent) {
		super(parent);
	}

	@Override
	public ClassPath appendClassPath(ClassPath cp) {
		ClassPath p = super.appendClassPath(cp);
		if(!classPaths.contains(p)) classPaths.add(p);
		return p;
	}

	@Override
	public ClassPath appendClassPath(String pathname) throws NotFoundException {
		ClassPath path = super.appendClassPath(pathname);
		if(!classPaths.contains(path)) classPaths.add(path);
		return path;
	}
	
	

	@Override
	public ClassPath insertClassPath(ClassPath cp) {
		ClassPath path = super.insertClassPath(cp);
		if(!classPaths.contains(path)) classPaths.add(path);
		return path;
	}

	@Override
	public ClassPath insertClassPath(String pathname) throws NotFoundException {
		ClassPath path = super.insertClassPath(pathname);
		if(!classPaths.contains(path)) classPaths.add(path);
		return path;
	}

	@Override
	public void removeClassPath(ClassPath cp) {
		if(classPaths.contains(cp)) classPaths.remove(cp);
		super.removeClassPath(cp);
	}
	
	public void close(){
		if(parent != null && parent instanceof ExtendedClassPool) ((ExtendedClassPool) parent).close();
		for(int i=classPaths.size()-1; i>=0; i--) removeClassPath(classPaths.get(i));
	}

	
	
	
	
}

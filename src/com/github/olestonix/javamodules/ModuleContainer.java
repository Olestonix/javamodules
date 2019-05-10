package com.github.olestonix.javamodules;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModuleContainer {

	private final File moduleFolder;
	private final Map<String, ModuleData> registered = new HashMap<>();

	public ModuleContainer(File folder) {
		this.moduleFolder = new File(folder + File.separator + "modules");
		moduleFolder.mkdirs();
	}

	public IModule getModuleById(String id) {
		if (!registered.containsKey(id))
			return null;
		
		return registered.get(id).getModule();
	}

	public ModuleData[] getRegisteredData() {
		Collection<ModuleData> mapResult = registered.values();
		ModuleData[] moduleDatas = new ModuleData[mapResult.size()];
		
		Iterator<ModuleData> iterator = mapResult.iterator();
		int index = 0;
		while (iterator.hasNext()) {
			moduleDatas[index] = iterator.next();
			index++;
		}
		
		return moduleDatas;
	}

	public IModule[] getModules() {
		Collection<ModuleData> mapResult = registered.values();
		IModule[] modules = new IModule[mapResult.size()];
		
		Iterator<ModuleData> iterator = mapResult.iterator();
		int index = 0;
		while (iterator.hasNext()) {
			modules[index] = iterator.next().getModule();
			index++;
		}
		
		return modules;
	}

	public IModule[] getModules(String nameOrVersion) {
		Collection<ModuleData> mapResult = registered.values();
		IModule[] modules = new IModule[mapResult.size()];
		
		Iterator<ModuleData> iterator = mapResult.iterator();
		int index = 0;
		while (iterator.hasNext()) {
			ModuleData data = iterator.next();
			if (nameOrVersion.equals(data.getName()) || nameOrVersion.equals(data.getVersion())) {
				modules[index] = data.getModule();
				index++;
			}
		}
		
		return modules;
	}

	public void register(String className) throws InstantiationException, IllegalAccessException {
		register(getClass(className));
	}

	public void register(Class<?> clazz) throws InstantiationException, IllegalAccessException {
		Module annotation = clazz.getDeclaredAnnotation(Module.class);
		
		if (!IModule.class.isAssignableFrom(clazz) || annotation == null)
			return;
		
		String id = annotation.id(), name = annotation.name(), version = annotation.version();
		
		IModule module = (IModule) clazz.newInstance();
		registered.put(id, new ModuleData(module, id, name, version));
		module.enable();
	}

	public void scanModules() {
		Arrays.stream(moduleFolder.listFiles((File dir, String name) -> name.endsWith(".jar"))).forEach(file -> 
		{
			try (JarFile jarFile = new JarFile(file)) {
				Enumeration<JarEntry> enumeration = jarFile.entries();
				URL[] urls = { new URL("jar:file:" + file.getAbsolutePath() + "!/") };
				URLClassLoader classLoader = URLClassLoader.newInstance(urls, IModule.class.getClassLoader());
				
				while (enumeration.hasMoreElements()) {
					JarEntry entry = enumeration.nextElement();
					if (entry.isDirectory() || !entry.getName().endsWith(".class"))
						continue;
					
					String className = entry.getName().substring(0, entry.getName().length() - ".class".length()).replace("/", ".");
					Class<?> clazz = classLoader.loadClass(className);
					register(clazz);
				}
			} catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		});
	}
	
	private Class<?> getClass(String className) {
		try {
			return Class.forName(className);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
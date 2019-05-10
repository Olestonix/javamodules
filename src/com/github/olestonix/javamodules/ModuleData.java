package com.github.olestonix.javamodules;

public class ModuleData {

	private final IModule module;
	private final String id, name, version;

	public ModuleData(IModule module, String id, String name, String version) {
		this.module = module;
		this.id = id;
		this.name = name;
		this.version = version;
	}

	public IModule getModule() {
		return module;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}
}
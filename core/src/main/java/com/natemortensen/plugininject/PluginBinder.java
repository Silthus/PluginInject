package com.natemortensen.plugininject;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.logging.Logger;

public class PluginBinder<T extends JavaPlugin> extends AbstractBinder
{
	private final T plugin;
	private final Class<T> pluginType;

	public PluginBinder(T plugin, Class<T> pluginType)
	{
		this.plugin = plugin;
		this.pluginType = pluginType;
	}

	protected void configure()
	{
		bind(plugin).to(pluginType).to(JavaPlugin.class).to(Plugin.class);
		bind(plugin.getLogger()).to(Logger.class);
	}

	public static PluginBinder<?> createFor(JavaPlugin plugin)
	{
		return doCreate(plugin.getClass(), plugin);
	}

	private static <T extends JavaPlugin> PluginBinder doCreate(Class<T> tClass, JavaPlugin plugin)
	{
		return new PluginBinder<>(tClass.cast(plugin), tClass);
	}
}

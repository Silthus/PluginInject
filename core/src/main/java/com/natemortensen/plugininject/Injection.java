package com.natemortensen.plugininject;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.ClasspathDescriptorFileFinder;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

import java.io.IOException;
import java.util.List;

public class Injection
{
	private static Injection instance;
	private final ServiceLocator serverLocator;
	private final List<Feature> features;

	public static Injection getInstance()
	{
		if (instance == null)
		{
			instance = new Injection();
		}
		return instance;
	}

	public static ServiceLocator loadPlugin(JavaPlugin plugin, List<Binder> binders)
	{
		return getInstance().loadLocator(plugin, binders);
	}

	public static void shutdownPlugin(JavaPlugin plugin, ServiceLocator locator)
	{
		getInstance().shutdownLocator(plugin, locator);
	}

	public Injection()
	{
		ServiceLocator systemLocator = ServiceLocatorUtilities.createAndPopulateServiceLocator("system");
		features = systemLocator.getAllServices(Feature.class);
		serverLocator = ServiceLocatorFactory.getInstance().create("server", systemLocator);
		ServiceLocatorUtilities.bind(serverLocator, new ServerBinder());
	}

	public ServiceLocator loadLocator(JavaPlugin plugin, List<Binder> binders)
	{
		final ServiceLocator serviceLocator = ServiceLocatorFactory.getInstance().create(plugin.getName(), serverLocator);
		Binder[] binderArray = binders.toArray(new Binder[binders.size()+1]);
		binderArray[binderArray.length-1] = PluginBinder.createFor(plugin);
		ServiceLocatorUtilities.bind(serviceLocator, binderArray);
		features.forEach(f -> f.preLoad(serviceLocator));
		DynamicConfigurationService dcs = serviceLocator.getService(DynamicConfigurationService.class);
		try
		{
			dcs.getPopulator().populate(new ClasspathDescriptorFileFinder(plugin.getClass().getClassLoader()));
		} catch (IOException e)
		{
			e.printStackTrace();
			plugin.getLogger().severe("Failed to load injection inhabitants file.");
			plugin.getLogger().severe("Disabling...");
			Bukkit.getPluginManager().disablePlugin(plugin);
			return null;
		}
		features.forEach(f -> f.postLoad(serviceLocator));
		serviceLocator.getAllServices(Module.class);
		return serviceLocator;
	}

	public void shutdownLocator(JavaPlugin plugin, ServiceLocator locator)
	{
		locator.shutdown();
	}
}

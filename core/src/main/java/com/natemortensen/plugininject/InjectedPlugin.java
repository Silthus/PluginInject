package com.natemortensen.plugininject;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.Binder;

import java.util.List;

/**
 * InjectedPlugin should be extended by plugins wishing to utilize automatic dependency injection.
 * Initialization and cleanup logic should be performed by annotating methods with {@link javax.annotation.PostConstruct}
 * and {@link javax.annotation.PreDestroy}. Injection is performed after modules have been instantiated. InjectedPlugin
 * implements Listener and is automatically registered.
 */
public class InjectedPlugin extends JavaPlugin implements Listener
{
	private ServiceLocator serviceLocator;
	@Override
	public final void onEnable()
	{
		serviceLocator = Injection.loadPlugin(this, getBinders());
		if (serviceLocator != null)
		{
			serviceLocator.inject(this);
			serviceLocator.postConstruct(this);
		}
	}

	/**
	 * Provides a list of binders to apply to the service locator for this plugin. All specified
	 * binders will be bound in the same config transaction as the internal ServerBinder, allowing
	 * for any built-in injection to be overridden by providing a binding of a higher rank.
	 * @return non-null but potentially empty list of Binders.
	 */
	public List<Binder> getBinders()
	{
		return ImmutableList.of();
	}

	@Override
	public final void onDisable()
	{
		if (serviceLocator != null)
		{
			serviceLocator.preDestroy(this);
			Injection.shutdownPlugin(this, serviceLocator);
			serviceLocator = null;
		}
	}

	/**
	 * Gets the ServiceLocator used by this plugin.  Returns null
	 * if this plugin is disabled.
	 * @return nullable ServiceLocator
	 */
	public final ServiceLocator getServices()
	{
		return serviceLocator;
	}
}

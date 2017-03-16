package com.natemortensen.plugininject.listener;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.InstanceLifecycleEvent;
import org.glassfish.hk2.api.InstanceLifecycleEventType;
import org.glassfish.hk2.api.InstanceLifecycleListener;

import javax.inject.Inject;

public class ListenerRegistration implements InstanceLifecycleListener
{
	private final JavaPlugin plugin;
	private final ListenerFilter filter;

	@Inject
	public ListenerRegistration(JavaPlugin plugin, ListenerFilter filter)
	{
		this.plugin = plugin;
		this.filter = filter;
	}

	public Filter getFilter()
	{
		return null;
	}

	public void lifecycleEvent(InstanceLifecycleEvent event)
	{
		Object potentialListener = event.getLifecycleObject();
		if (event.getEventType() == InstanceLifecycleEventType.POST_PRODUCTION)
		{
			if (potentialListener instanceof Listener && filter.shouldRegister(potentialListener))
			{
				plugin.getServer().getPluginManager().registerEvents((Listener) event.getLifecycleObject(), plugin);
			}
		}
	}
}

package com.natemortensen.plugininject.listener;

import org.glassfish.hk2.api.InstanceLifecycleListener;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

public class ListenerBinder extends AbstractBinder
{
	protected void configure()
	{
		bind(DefaultFilter.class).in(Singleton.class).to(ListenerFilter.class).ranked(-1);
		bind(ListenerRegistration.class).in(Singleton.class).to(InstanceLifecycleListener.class);
	}
}

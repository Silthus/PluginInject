package com.natemortensen.plugininject.listener;

import com.natemortensen.plugininject.Feature;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.annotations.Service;

@Service
public class ListenerFeature implements Feature
{
	@Override
	public void postLoad(ServiceLocator locator)
	{
		ServiceLocatorUtilities.bind(locator, new ListenerBinder());
	}
}

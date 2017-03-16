package com.natemortensen.plugininject;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Contract;

/**
 * An internal feature within PluginInject. Initialized prior to plugin loading,
 * so Features cannot be implemented by plugins.
 */
@Contract
public interface Feature
{
	/**
	 * Invoked prior to plugin configuration being loaded
	 * @param locator non-null plugin ServiceLocator
	 */
	default void preLoad(ServiceLocator locator) {}

	/**
	 * Invoked after plugin configuration has been loaded
	 * @param locator non-null plugin ServiceLocator
	 */
	default void postLoad(ServiceLocator locator) {}
}

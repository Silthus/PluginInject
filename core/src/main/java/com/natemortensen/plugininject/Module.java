package com.natemortensen.plugininject;

import org.jvnet.hk2.annotations.Contract;

/**
 * Marker interface for services that should be eagerly instantiated
 * once all services have been registered.  Modules serve as the entry
 * points of application logic, and can use {@link javax.annotation.PostConstruct}
 * to begin operation.
 */
@Contract
public interface Module
{
}

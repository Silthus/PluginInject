package com.natemortensen.plugininject.listener;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface ListenerFilter
{
	boolean shouldRegister(Object object);
}

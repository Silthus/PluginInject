package com.natemortensen.plugininject.listener;

public class DefaultFilter implements ListenerFilter
{
	public boolean shouldRegister(Object object)
	{
		return object.getClass().getAnnotation(DoNotRegister.class) == null;
	}
}

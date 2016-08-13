package com.github.SkySpiral7.Java.serialization;

import java.io.InputStream;

public class ObjectInputStream extends InputStream
{
	private final ObjectRegistry registry = new ObjectRegistry();

	@Override
	public int read(){return -1;}
	//@Override them all

	public boolean hasData(){return false;}  //can't call hasData(byte.class) because of overhead mismatch
	public boolean hasData(final Class<?> expectedClass){return false;}
	public int remainingBytes(){return 0;}

	public Object readObject(){return readObject(Object.class);}
	public <T> T readObject(final Class<T> expectedClass){return null;}
	public Object readSerializable(){return null;}  //unchecked/unsafe and difficult to implement
	public void readFieldsReflectively(final Object instance){}

	public ObjectRegistry getObjectRegistry(){return registry;}
}

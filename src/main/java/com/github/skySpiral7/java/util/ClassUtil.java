package com.github.skySpiral7.java.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public enum ClassUtil
{
   ;  //no instances

   /**
    * @see #getType(TypeReference)
    */
   public static abstract class TypeReference<T>
   {}

   /**
    * <p>This method is the easiest way to obtain a ParameterizedType since {@code List<String>.class} doesn't compile.</p>
    *
    * Example usage:<br/>
    * {@code ClassUtil.getType(new TypeReference<List<String>>(){});}<br/>
    * {@code ClassUtil.getType(new TypeReference<String>(){});}
    *
    * @param typeReference must be a subclass that doesn't use raw types.
    *
    * @return a class or ParameterizedType
    *
    * @throws IllegalArgumentException if {@code typeReference} used a raw type
    */
   public static <T> Type getType(final TypeReference<T> typeReference)
   {
      final Type genericSuperclass = typeReference.getClass().getGenericSuperclass();
      if (!(genericSuperclass instanceof ParameterizedType)) throw new IllegalArgumentException("Must pass in a typed subclass");
      final ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
      return parameterizedType.getActualTypeArguments()[0];
   }
}

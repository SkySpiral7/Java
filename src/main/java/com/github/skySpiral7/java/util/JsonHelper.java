package com.github.skySpiral7.java.util;

import java.util.Objects;

//TODO: some of my utils are final and some are abstract. make them all abstract
public final class JsonHelper
{
   private JsonHelper(){}

   public static String stringify(Object target)
   {
      if (target instanceof String) return stringify((String) target);
      return Objects.toString(target);  //target.toString is expected to put {} or [] around itself
   }

   public static String stringify(String target)
   {
      if (target == null) return "null";
      return "\"" + target + "\"";
   }

   public static String toStringHeader(Object target)
   {
      if (target == null) return "null";
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("{\"class\": \"");
      stringBuilder.append(target.getClass().getName());
      stringBuilder.append("\", \"hexHash\": \"");
      stringBuilder.append(Integer.toHexString(target.hashCode()));
      stringBuilder.append("\", \"data\": ");
      return stringBuilder.toString();
   }

}

package com.github.skySpiral7.java.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ClassUtil_UT
{
   @Test
   public void getType_ReturnsParameterizedType_GivenListOfString()
   {
      final Type actual = ClassUtil.getType(new ClassUtil.TypeReference<List<String>>(){});
      assertThat(actual, is(instanceOf(ParameterizedType.class)));
      assertEquals("java.util.List<java.lang.String>", actual.toString());
   }

   @Test
   public void getType_ReturnsClass_GivenString()
   {
      assertEquals(String.class, ClassUtil.getType(new ClassUtil.TypeReference<String>(){}));
   }

   @Test
   public void getType_Throws_GivenRawType()
   {
      try
      {
         ClassUtil.getType(new ClassUtil.TypeReference(){});
         fail("Didn't throw");
      }
      catch (final IllegalArgumentException actual)
      {
         assertEquals("Must pass in a typed subclass", actual.getMessage());
      }
   }

}

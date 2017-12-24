package com.github.skySpiral7.java.dataStructures;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Range_UT
{
   @Test
   public void createArray()
   {
      final Range<Integer> range = new Range<>(1, "..", 3);

      assertArrayEquals(new Integer[]{1, 2, 3}, range.createArray());
      assertArrayEquals(new Integer[]{1, 3}, range.createArray(2));

      assertArrayEquals(new int[]{1, 2, 3}, range.createArray(int[].class));
      assertArrayEquals(new byte[]{1, 2, 3}, range.createArray(byte[].class));
      assertArrayEquals(new long[]{1, 3}, range.createArray(long[].class, 2));
   }

   @Test
   public void createArray_otherTypes()
   {
      assertArrayEquals(new Double[]{1d, 1.75d, 2.5d}, new Range<>(1d, "..", 3d).createArray(Double[].class, 0.75d));
      final Range<BigInteger> bigIntegerRange = new Range<>(BigInteger.ZERO, "..>", BigInteger.valueOf(2));
      assertArrayEquals(new BigInteger[]{BigInteger.ZERO, BigInteger.ONE}, bigIntegerRange.createArray());
      assertArrayEquals(new int[0], new Range<>(1, "<..", 3).createArray(int[].class, 5));
   }

   @Test
   public void createList()
   {
      final Range<Integer> range = new Range<>(1, "..", 3);

      assertEquals(Arrays.asList(new Integer[]{1, 2, 3}), range.createList());
      assertEquals(Arrays.asList(new Integer[]{1, 3}), range.createList(2));

      assertEquals(Arrays.asList(new Integer[]{1, 2, 3}), range.createList(Integer.class));
      assertEquals(Arrays.asList(new Byte[]{1, 2, 3}), range.createList(Byte.class));
      assertEquals(Arrays.asList(new Long[]{1L, 3L}), range.createList(Long.class, 2));
   }

   @Test
   public void createStream()
   {
      final Range<Double> range = new Range<>(1.0, "..", Double.POSITIVE_INFINITY);

      final Iterator<Float> iterator = range.createStream(float.class).iterator();
      assertEquals(Float.valueOf(1f), iterator.next());
      assertEquals(Float.valueOf(2f), iterator.next());
      assertEquals(Float.valueOf(3f), iterator.next());
      assertEquals(Float.valueOf(4f), iterator.next());
      assertTrue(iterator.hasNext());
   }

   @Test
   public void contains()
   {
      final Range<Integer> range = new Range<>(Range.inclusive(0), Range.exclusive(10));

      assertTrue(range.contains(0));
      assertTrue(range.contains(1));
      assertFalse(range.contains(-1));
      assertFalse(range.contains(10));
      assertFalse(range.contains(11));
   }

   @Test
   public void test_toString()
   {
      assertEquals("0 ..> 10", new Range<>(Range.inclusive(0), Range.exclusive(10)).toString());
      assertEquals("0 <.. 10", new Range<>(Range.exclusive(0), Range.inclusive(10)).toString());
      assertEquals("-5 .. 5", new Range<>(-5, "  \t .. \n", 5).toString());
      assertEquals("0.0 <..> Infinity", new Range<>(0f, "<..>", Float.POSITIVE_INFINITY).toString());
   }

}

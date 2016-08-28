package com.github.SkySpiral7.Java.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import com.github.SkySpiral7.Java.util.BitWiseUtil;
import org.junit.Test;

import com.github.SkySpiral7.Java.util.FileIoUtil;

public class UT_ObjectStreamWriter
{
	@Test
	public void constructor_throws()
	{
		try
		{
			new ObjectStreamWriter(new File(".")).close();
		}
		catch (final IllegalArgumentException actual)
		{
			assertEquals("It is not possible to write to a directory", actual.getMessage());
		}
	}

	@Test
	public void constructor_clears() throws IOException
	{
		final File tempFile = File.createTempFile("UT_ObjectStreamWriter.TempFile.constructor_clears.", ".txt");
		tempFile.deleteOnExit();
		FileIoUtil.writeToFile(tempFile, "test");
		new ObjectStreamWriter(tempFile).close();
		assertEquals("", FileIoUtil.readTextFile(tempFile));
	}

	@Test
	public void writeObject_overHead() throws IOException
	{
		final File tempFile = File.createTempFile("UT_ObjectStreamWriter.TempFile.writeObject_overHead.", ".txt");
		tempFile.deleteOnExit();
		final ObjectStreamWriter testObject = new ObjectStreamWriter(tempFile);

		testObject.writeObject((byte) 0xab);
		testObject.close();
		//@formatter:off
		final byte[] expected = new byte[] {
				(byte)106, (byte)97, (byte)118, (byte)97, (byte)46,  //"java."
				(byte)108, (byte)97, (byte)110, (byte)103, (byte)46,  //"lang."
				(byte)66, (byte)121, (byte)116, (byte)101,  //"Byte"
				(byte)124,  //"|"
				(byte)0xab  //the data
		};
		//@formatter:on
		//don't use bytesToString since that assumes the header has correct encoding
		assertEquals(Arrays.toString(expected), Arrays.toString(FileIoUtil.readBinaryFile(tempFile)));
	}

	@Test
	public void writeObject_overHead_null() throws IOException
	{
		final File tempFile = File.createTempFile("UT_ObjectStreamWriter.TempFile.writeObject_overHead_null.", ".txt");
		tempFile.deleteOnExit();
		final ObjectStreamWriter testObject = new ObjectStreamWriter(tempFile);

		testObject.writeObject(null);
		testObject.close();
		final byte[] expected = new byte[] { (byte) '|' };
		//don't use bytesToString since that assumes the header has correct encoding
		assertEquals(Arrays.toString(expected), Arrays.toString(FileIoUtil.readBinaryFile(tempFile)));
	}

	@Test
	public void writeObject_byte() throws IOException
	{
		final File tempFile = File.createTempFile("UT_ObjectStreamWriter.TempFile.writeObject_byte.", ".txt");
		tempFile.deleteOnExit();
		final ObjectStreamWriter testObject = new ObjectStreamWriter(tempFile);
		final Byte data = (byte) 2;

		testObject.writeObject(data);
		testObject.close();
		final byte[] fileContents = FileIoUtil.readBinaryFile(tempFile);
		final String header = "java.lang.Byte|";
		assertEquals(header, bytesToString(fileContents, 1));
		assertEquals(2, fileContents[header.length()]);
	}

	@Test
	public void writeObject_short() throws IOException
	{
		final File tempFile = File.createTempFile("UT_ObjectStreamWriter.TempFile.writeObject_short.", ".txt");
		tempFile.deleteOnExit();
		final ObjectStreamWriter testObject = new ObjectStreamWriter(tempFile);
		final Short data = (short) 0xcafe;
		final byte[] expected = { (byte) 0xca, (byte) 0xfe };

		testObject.writeObject(data);
		testObject.close();
		final byte[] fileContents = FileIoUtil.readBinaryFile(tempFile);
		assertEquals("java.lang.Short|", bytesToString(fileContents, 2));
		assertEquals(Arrays.toString(expected), Arrays.toString(shortenBytes(fileContents, 2)));
	}

	@Test
	public void writeObject_int() throws IOException
	{
		final File tempFile = File.createTempFile("UT_ObjectStreamWriter.TempFile.writeObject_int.", ".txt");
		tempFile.deleteOnExit();
		final ObjectStreamWriter testObject = new ObjectStreamWriter(tempFile);
		final Integer data = 0xcafe_bead;
		final byte[] expected = { (byte) 0xca, (byte) 0xfe, (byte) 0xbe, (byte) 0xad };

		testObject.writeObject(data);
		testObject.close();
		final byte[] fileContents = FileIoUtil.readBinaryFile(tempFile);
		assertEquals("java.lang.Integer|", bytesToString(fileContents, 4));
		assertEquals(Arrays.toString(expected), Arrays.toString(shortenBytes(fileContents, 4)));
	}

	@Test
	public void writeObject_long() throws IOException
	{
		final File tempFile = File.createTempFile("UT_ObjectStreamWriter.TempFile.writeObject_long.", ".txt");
		tempFile.deleteOnExit();
		final ObjectStreamWriter testObject = new ObjectStreamWriter(tempFile);
		final Long data = 0xdead_beef__b100_d123L;
		final byte[] expected = { (byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef, (byte) 0xb1, (byte) 0x00,
				(byte) 0xd1, (byte) 0x23 };

		testObject.writeObject(data);
		testObject.close();
		final byte[] fileContents = FileIoUtil.readBinaryFile(tempFile);
		assertEquals("java.lang.Long|", bytesToString(fileContents, 8));
		assertEquals(Arrays.toString(expected), Arrays.toString(shortenBytes(fileContents, 8)));
	}

	@Test
	public void writeObject_float() throws IOException
	{
		final File tempFile = File.createTempFile("UT_ObjectStreamWriter.TempFile.writeObject_float.", ".txt");
		tempFile.deleteOnExit();
		final ObjectStreamWriter testObject = new ObjectStreamWriter(tempFile);
		final Float data = Float.intBitsToFloat(0xcafe_bead);
		final byte[] expected = { (byte) 0xca, (byte) 0xfe, (byte) 0xbe, (byte) 0xad };

		testObject.writeObject(data);
		testObject.close();
		final byte[] fileContents = FileIoUtil.readBinaryFile(tempFile);
		assertEquals("java.lang.Float|", bytesToString(fileContents, 4));
		assertEquals(Arrays.toString(expected), Arrays.toString(shortenBytes(fileContents, 4)));
	}

	@Test
	public void writeObject_double() throws IOException
	{
		final File tempFile = File.createTempFile("UT_ObjectStreamWriter.TempFile.writeObject_double.", ".txt");
		tempFile.deleteOnExit();
		final ObjectStreamWriter testObject = new ObjectStreamWriter(tempFile);
		final Double data = Double.longBitsToDouble(0xdead_beef__b100_d123L);
		final byte[] expected = { (byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef, (byte) 0xb1, (byte) 0x00,
				(byte) 0xd1, (byte) 0x23 };

		testObject.writeObject(data);
		testObject.close();
		final byte[] fileContents = FileIoUtil.readBinaryFile(tempFile);
		assertEquals("java.lang.Double|", bytesToString(fileContents, 8));
		assertEquals(Arrays.toString(expected), Arrays.toString(shortenBytes(fileContents, 8)));
	}

	@Test
	public void writeObject_boolean() throws IOException
	{
		final File tempFile = File.createTempFile("UT_ObjectStreamWriter.TempFile.writeObject_boolean.", ".txt");
		tempFile.deleteOnExit();
		final ObjectStreamWriter testObject = new ObjectStreamWriter(tempFile);

		testObject.writeObject(true);
		testObject.flush();
		byte[] fileContents = FileIoUtil.readBinaryFile(tempFile);
		assertEquals("java.lang.Boolean|", bytesToString(fileContents, 1));
		assertEquals("[1]", Arrays.toString(shortenBytes(fileContents, 1)));

		FileIoUtil.writeToFile(tempFile, "");

		testObject.writeObject(false);
		testObject.close();
		fileContents = FileIoUtil.readBinaryFile(tempFile);
		assertEquals("java.lang.Boolean|", bytesToString(fileContents, 1));
		assertEquals("[0]", Arrays.toString(shortenBytes(fileContents, 1)));
	}

	@Test
	public void writeObject_char() throws IOException
	{
		final File tempFile = File.createTempFile("UT_ObjectStreamWriter.TempFile.writeObject_char.", ".txt");
		tempFile.deleteOnExit();
		final ObjectStreamWriter testObject = new ObjectStreamWriter(tempFile);

		testObject.writeObject('f');
		testObject.flush();
		byte[] fileContents = FileIoUtil.readBinaryFile(tempFile);
		assertEquals("java.lang.Character|", bytesToString(fileContents, 2));
		assertEquals("[0, " + 0x66 + "]", Arrays.toString(shortenBytes(fileContents, 2)));

		FileIoUtil.writeToFile(tempFile, "");

		testObject.writeObject('\u221E');  //infinity sign is BMP non-private
		testObject.close();
		fileContents = FileIoUtil.readBinaryFile(tempFile);
		assertEquals("java.lang.Character|", bytesToString(fileContents, 2));
		assertEquals("[" + 0x22 + ", " + 0x1e + "]", Arrays.toString(shortenBytes(fileContents, 2)));
	}

	@Test
	public void writeObject_String() throws IOException
	{
		final File tempFile = File.createTempFile("UT_ObjectStreamWriter.TempFile.writeObject_String.", ".txt");
		tempFile.deleteOnExit();
		final ObjectStreamWriter testObject = new ObjectStreamWriter(tempFile);

		testObject.writeObject("f\u221E");  //infinity sign is BMP (3 UTF-8 bytes) non-private
		testObject.close();
		final byte[] expected = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04,  //UTF-8 length (int)
				(byte) 0x66, (byte) 0xe2, (byte) 0x88, (byte) 0x9e };
		final byte[] fileContents = FileIoUtil.readBinaryFile(tempFile);
		assertEquals("java.lang.String|", bytesToString(fileContents, expected.length));
		assertEquals(Arrays.toString(expected), Arrays.toString(shortenBytes(fileContents, expected.length)));
	}

	private static enum EnumByName implements StaticSerializableEnumByName
	{
		One, Two;
	}

	@Test
	public void writeObject_enumByName() throws IOException
	{
		final File tempFile = File.createTempFile("UT_ObjectStreamWriter.TempFile.writeObject_enumByName.", ".txt");
		tempFile.deleteOnExit();
		final ObjectStreamWriter testObject = new ObjectStreamWriter(tempFile);

		testObject.writeObject(EnumByName.One);
		testObject.close();
		final byte[] fileContents = FileIoUtil.readBinaryFile(tempFile);
		final String overhead = "com.github.SkySpiral7.Java.serialization.UT_ObjectStreamWriter$EnumByName|"
				+ "java.lang.String|";
		final byte[] data = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03,  //UTF-8 length (int)
				(byte) 79, (byte) 110, (byte) 101 };  //"One"
		assertEquals(overhead, bytesToString(fileContents, data.length));
		assertEquals(Arrays.toString(data), Arrays.toString(shortenBytes(fileContents, data.length)));
	}

	@Test
	public void writeObject_enumByName_notEnum() throws IOException
	{
		final File tempFile = File.createTempFile("UT_ObjectStreamWriter.TempFile.writeObject_enumByName_notEnum.", ".txt");
		tempFile.deleteOnExit();
		final ObjectStreamWriter testObject = new ObjectStreamWriter(tempFile);

		class NotEnum implements StaticSerializableEnumByName
		{}

		try
		{
			testObject.writeObject(new NotEnum());
		}
		catch (final ClassCastException actual)
		{
			assertEquals(
					"com.github.SkySpiral7.Java.serialization.UT_ObjectStreamWriter$1NotEnum cannot be cast to java.lang.Enum",
					actual.getMessage());
		}

		testObject.close();
	}

	private static enum EnumByOrdinal implements StaticSerializableEnumByOrdinal
	{
		One, Two, Three, Four;
	}

	@Test
	public void writeObject_enumByOrdinal() throws IOException
	{
		final File tempFile = File.createTempFile("UT_ObjectStreamWriter.TempFile.writeObject_enumByOrdinal.", ".txt");
		tempFile.deleteOnExit();
		final ObjectStreamWriter testObject = new ObjectStreamWriter(tempFile);

		testObject.writeObject(EnumByOrdinal.Four);
		testObject.close();
		final byte[] fileContents = FileIoUtil.readBinaryFile(tempFile);
		final String overhead = "com.github.SkySpiral7.Java.serialization.UT_ObjectStreamWriter$EnumByOrdinal|"
				+ "java.lang.Integer|";
		final byte[] data = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03 };
		assertEquals(overhead, bytesToString(fileContents, data.length));
		assertEquals(Arrays.toString(data), Arrays.toString(shortenBytes(fileContents, data.length)));
	}

	@Test
	public void writeObject_enumByOrdinal_notEnum() throws IOException
	{
		final File tempFile = File.createTempFile("UT_ObjectStreamWriter.TempFile.writeObject_enumByOrdinal_notEnum.", ".txt");
		tempFile.deleteOnExit();
		final ObjectStreamWriter testObject = new ObjectStreamWriter(tempFile);

		class NotEnum implements StaticSerializableEnumByOrdinal
		{}

		try
		{
			testObject.writeObject(new NotEnum());
		}
		catch (final ClassCastException actual)
		{
			assertEquals(
					"com.github.SkySpiral7.Java.serialization.UT_ObjectStreamWriter$2NotEnum cannot be cast to java.lang.Enum",
					actual.getMessage());
		}

		testObject.close();
	}

	@Test
	public void writeObject_custom() throws IOException
	{
		final class CustomLocal implements StaticSerializable
		{
			boolean wasCalled = false;

			//no reader doesn't matter

			@Override
			public void writeToStream(ObjectStreamWriter out)
			{
				wasCalled = true;
			}
		}

		final File tempFile = File.createTempFile("UT_ObjectStreamWriter.TempFile.writeObject_custom.", ".txt");
		tempFile.deleteOnExit();
		final ObjectStreamWriter testObject = new ObjectStreamWriter(tempFile);
		final CustomLocal data = new CustomLocal();

		testObject.writeObject(data);
		testObject.close();
		assertTrue(data.wasCalled);
	}

	@Test
	public void writeObject_throws() throws IOException
	{
		final File tempFile = File.createTempFile("UT_ObjectStreamWriter.TempFile.writeObject_throws.", ".txt");
		tempFile.deleteOnExit();
		final ObjectStreamWriter testObject = new ObjectStreamWriter(tempFile);

		try
		{
			testObject.writeObject(tempFile);
		}
		catch (final IllegalArgumentException actual)
		{
			assertEquals("Couldn't serialize object of class java.io.File", actual.getMessage());
		}

		testObject.close();
	}

	@Test
	public void writeObject_Serializable() throws IOException
	{
		final File tempFile = File.createTempFile("UT_ObjectStreamWriter.TempFile.writeObject_Serializable.", ".txt");
		tempFile.deleteOnExit();
		final ObjectStreamWriter testObject = new ObjectStreamWriter(tempFile);

		final BigInteger data = BigInteger.TEN;
		final byte[] javaData = ObjectStreamWriter.javaSerialize(data);

		testObject.writeObject(data);
		testObject.close();
		final byte[] fileContents = FileIoUtil.readBinaryFile(tempFile);
		assertEquals("java.math.BigInteger|", bytesToString(fileContents, (javaData.length+4)));

		final byte[] bytesOfSize = new byte[4];
		System.arraycopy(fileContents, "java.math.BigInteger|".length(), bytesOfSize, 0, 4);
		assertEquals(javaData.length, BitWiseUtil.bigEndianBytesToInteger(bytesOfSize));

		assertEquals(Arrays.toString(javaData), Arrays.toString(shortenBytes(fileContents, javaData.length)));
	}

	@Test
	public void writeFieldsReflectively() throws IOException
	{
		final class ReflectiveLocal implements StaticSerializable
		{
			private int field = 0xcafe_bead;

			//no reader doesn't matter

			@Override
			public void writeToStream(final ObjectStreamWriter writer)
			{
				writer.writeFieldsReflectively(this);
			}
		}

		final File tempFile = File.createTempFile("UT_ObjectStreamWriter.TempFile.writeFieldsReflectively.", ".txt");
		tempFile.deleteOnExit();
		final ObjectStreamWriter testObject = new ObjectStreamWriter(tempFile);
		final byte[] expected = { (byte) 0xca, (byte) 0xfe, (byte) 0xbe, (byte) 0xad };

		testObject.writeObject(new ReflectiveLocal());
		testObject.close();
		final byte[] fileContents = FileIoUtil.readBinaryFile(tempFile);
		assertEquals("com.github.SkySpiral7.Java.serialization.UT_ObjectStreamWriter$1ReflectiveLocal|java.lang.Integer|",
			bytesToString(fileContents, 4));
		assertEquals(Arrays.toString(expected), Arrays.toString(shortenBytes(fileContents, 4)));
	}

	private String bytesToString(final byte[] data, final int bytesToIgnore)
	{
		return new String(data, 0, (data.length - bytesToIgnore), StandardCharsets.UTF_8);
	}

	private byte[] shortenBytes(final byte[] data, final int bytesToKeep)
	{
		final byte[] smallerData = new byte[bytesToKeep];
		System.arraycopy(data, (data.length - bytesToKeep), smallerData, 0, bytesToKeep);
		return smallerData;
	}

}
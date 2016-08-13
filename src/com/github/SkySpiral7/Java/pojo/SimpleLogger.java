package com.github.SkySpiral7.Java.pojo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import com.github.SkySpiral7.Java.util.FileIoUtil;

/**
 * <p>This class is a very simple logger. It is useful for quick and dirty debugging.
 * For example if you need to debug a loop that will iterate 10,000 times but you
 * don't want to pollute the log file normally used then you can use this for a quick
 * temporary log. Or maybe in a loop if you want to track multiple things you can put
 * them each in their own log so you can see how they change over time without having
 * to parse which line belongs to which variable.</p>
 *
 * <p>This class is final to make it perform faster. This class should not gain functionality
 * and should not be a method parameter or class field. This is because this class is intended for quick debugging
 * and should not be used in production (or other environments like QA) for that see a real logger like Log4J.</p>
 */
public final class SimpleLogger {
    private final File log;

   /**
    * @param log the File that will be written to (even if it does not exist).
    * @throws IllegalArgumentException if the File is a directory (which can't be written to)
    */
   public SimpleLogger(File log)
   {
       if(log.isDirectory()) throw new IllegalArgumentException("It is not possible to log to a directory");
       this.log = log;
   }

   /**
    * This is a standard getter for the file that was used to construct this object.
    */
   public File getFile(){return log;}

   /**
    * Append to the log. Note that an end line is not added.
    * The log will be created if it does not exist.
    * 
    * @param text the text to be appended exactly as passed in
    */
   public void append(String text)
   {
       FileIoUtil.appendToFile(log, text);
   }

   /**
    * Append to the log. An end line is appended to the string.
    * The log will be created if it does not exist.
    * 
    * @param line the text to be appended followed by a system end line
    */
   public void appendLine(String line)
   {
       Objects.requireNonNull(line);
       this.append(line + System.lineSeparator());
   }

   /**
    * Clear the log. The file contents become empty.
    * The log will be created if it does not exist.
    */
   public void clear()
   {
   	FileIoUtil.writeToFile(log, "");
   }

   /**
    * Delete the log on disk. Note that it will be recreated if you call append or clear.
    * Nio Files.delete is used to delete the file because according to the Javadoc the error messages
    * are more specific than the ones for getFile().delete.
    * 
    * @see #clear()
    */
   public void delete()
   {
      try
      {
         Files.delete(Paths.get(log.getAbsolutePath()));
      }
       catch(IOException e){throw new RuntimeException(e);}
   }

}

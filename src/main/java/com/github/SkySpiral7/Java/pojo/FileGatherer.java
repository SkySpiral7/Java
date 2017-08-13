package com.github.SkySpiral7.Java.pojo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class FileGatherer
      //I tested this class by hand since a UT would require a file system
{
   private final Path rootFolder;
   private final Predicate<Path> exploreCriteria;
   private final int maxDepth;

   /*
    * @param rootFolder the root which will be searched along with all subfolders
    * @param gatherCriteria will compare using gatherCriteria.matcher(thisFile.getName()).find() this includes extension type
    * @param exploreCriteria will compare using folderCriteria.matcher(thisFile.getName()).find() to determine if the folder should be
    * explored
    * @param maxDepth pass -1 to have no maximum otherwise it only will go that number of folders down pass 0 for this folder only
    * @param maxFinds pass -1 to have no maximum
    * @return a list of files with matching names
    */
   /*
    * @param rootFolder        the root which will be searched along with all subfolders
    * @param gatherCriteria      if accept returns true then the file will be added to the search results
    * @param exploreCriteria if accept returns false then the folder will not be searched. Note that this is called on rootFolder
    * @param maxDepth          pass -1 to have no maximum otherwise it only will go that number of folders down pass 0 for this folder only
    * @param maxFinds          pass -1 to have no maximum
    */
   public FileGatherer(final Builder builder)
   {
      this.rootFolder = builder.getRootFolder();
      final int relativeMaxDepth = builder.getMaxDepth();
      this.maxDepth = (relativeMaxDepth == -1) ? relativeMaxDepth : (rootFolder.getNameCount() + 1);
      //+1 so that the rootFolder itself isn't counted
      if (maxDepth != -1)
      {
         this.exploreCriteria = builder.getExploreCriteria().and(path -> (maxDepth < path.getNameCount()));
      }
      else this.exploreCriteria = builder.getExploreCriteria();
   }

   /**
    * Also see Files.walk(Path) which does the same thing but might be more efficient.
    *
    * @see java.nio.file.Files#walk(Path, FileVisitOption...)
    */
   public static Stream<Path> search(final Path rootFolder)
   {
      return new Builder().rootFolder(rootFolder).build().search();
   }

   public static Stream<Path> searchForExtensions(final Path rootFolder, final String... extensions)
   {
      return FileGatherer.search(rootFolder).filter(Filters.acceptExtensions(extensions));
   }

   public Stream<Path> search()
   {
      //TODO: add a comparator for file order (will still be depth first)
      return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(new PathIterator(rootFolder, exploreCriteria), Spliterator.ORDERED | Spliterator.NONNULL),
            false);
   }

   private static class PathIterator implements Iterator<Path>
   {
      private final Deque<Path> remaining = new ArrayDeque<>();
      private final Predicate<Path> exploreCriteria;

      private PathIterator(final Path rootFolder, final Predicate<Path> exploreCriteria)
      {
         remaining.add(rootFolder);
         this.exploreCriteria = exploreCriteria;
      }

      @Override
      public boolean hasNext()
      {
         return !remaining.isEmpty();
      }

      @Override
      public Path next()
      {
         final Path thisPath = remaining.pollLast();
         try
         {
            if (Files.isDirectory(thisPath) && exploreCriteria.test(thisPath))
               remaining.addAll(Files.list(thisPath).collect(Collectors.toList()));
            return thisPath;
         }
         catch (final IOException ioException)
         {
            throw new UncheckedIOException("Could not open directory " + thisPath, ioException);
         }
      }
   }

   public static final class Filters
   {
      public static final Predicate<Path> ACCEPT_ALL = path -> true;
      public static final Predicate<Path> EXCLUDE_HIDDEN = path ->
      {
         try
         {
            return Files.isHidden(path);
         }
         catch (IOException ioException)
         {
            throw new UncheckedIOException(ioException);
         }
      };
      public static final Predicate<Path> EXCLUDE_DIRECTORIES = Files::isRegularFile;

      private Filters(){}

      public static Predicate<Path> acceptExtensions(final String... extensions)
      {
         //the name isn't required and it can't be seen
         final FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(null, extensions);
         //notice how extensionFilter is only created once and used for each call to accept
         return path -> (Files.isRegularFile(path) && extensionFilter.accept(path.toFile()));
      }

      public static Predicate<Path> acceptNamePattern(final Pattern pattern)
      {
         return path -> pattern.matcher(path.toFile().getName()).find();
      }
   }

   public static class Builder
   {
      private Path rootFolder;
      private Predicate<Path> exploreCriteria;
      private int maxDepth;

      public Builder()
      {
         //the default root is where this class's project (or executable jar) is located
         rootFolder = Paths.get(".");
         exploreCriteria = Filters.ACCEPT_ALL;
         maxDepth = -1;
      }

      public FileGatherer build()
      {
         return new FileGatherer(this);
      }

      public Builder unlimitedDepth()
      {
         this.maxDepth = -1;
         return this;
      }

      public Builder maxDepth(final int maxDepth)
      {
         if (maxDepth != -1 && maxDepth < 1) throw new IllegalArgumentException("Expected -1 or >= 1. Actual: " + maxDepth);
         this.maxDepth = maxDepth;
         return this;
      }

      public Builder rootFolder(final Path rootFolder)
      {
         Objects.requireNonNull(rootFolder);
         if (Files.notExists(rootFolder)) throw new IllegalArgumentException(rootFolder + " doesn't exist");
         if (Files.isRegularFile(rootFolder)) throw new IllegalArgumentException(rootFolder + " isn't a directory");
         this.rootFolder = rootFolder;
         return this;
      }

      public Builder exploreCriteria(final Predicate<Path> subFolderCriteria)
      {
         Objects.requireNonNull(subFolderCriteria);
         this.exploreCriteria = subFolderCriteria;
         return this;
      }

      //**************************************************************************
      //Rest of file is generated getters
      //**************************************************************************
      public Predicate<Path> getExploreCriteria()
      {
         return exploreCriteria;
      }

      public int getMaxDepth()
      {
         return maxDepth;
      }

      public Path getRootFolder()
      {
         return rootFolder;
      }
   }

   //**************************************************************************
   //Rest of file is generated getters
   //**************************************************************************
   public Predicate<Path> getExploreCriteria()
   {
      return exploreCriteria;
   }

   public int getMaxDepth()
   {
      return maxDepth;
   }

   public Path getRootFolder()
   {
      return rootFolder;
   }
}

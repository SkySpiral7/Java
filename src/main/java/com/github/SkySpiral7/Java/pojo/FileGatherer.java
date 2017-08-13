package com.github.SkySpiral7.Java.pojo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Comparator;
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
   private Path rootFolder;
   private Predicate<Path> exploreCriteria;
   private Comparator<Path> pathOrder;

   /*
    * @param rootFolder        the root which will be searched along with all subfolders
    * @param exploreCriteria if accept returns false then the folder will not be searched. Note that this is called on rootFolder
    */
   public FileGatherer()
   {
      //the default root is where this class's project (or executable jar) is located
      rootFolder = Paths.get(".");
      exploreCriteria = Filters.ACCEPT_ALL;
      pathOrder = Comparator.naturalOrder();
   }

   public Path getRootFolder()
   {
      return rootFolder;
   }

   public Predicate<Path> getExploreCriteria()
   {
      return exploreCriteria;
   }

   public Comparator<Path> getPathOrder()
   {
      return pathOrder;
   }

   public FileGatherer withRootFolder(final Path rootFolder)
   {
      Objects.requireNonNull(rootFolder);
      if (Files.notExists(rootFolder)) throw new IllegalArgumentException(rootFolder + " doesn't exist");
      if (Files.isRegularFile(rootFolder)) throw new IllegalArgumentException(rootFolder + " isn't a directory");
      this.rootFolder = rootFolder;
      return this;
   }

   /**
    * Will set rootFolder and create an exploreCriteria
    *
    * @param relativeMaxDepth 0 is the same as having exploreCriteria always reject
    */
   public FileGatherer withMaxDepth(final Path rootFolder, final int relativeMaxDepth)
   {
      this.withRootFolder(rootFolder);  //I don't think it needs to be normalized or absolute
      if (relativeMaxDepth < 0) throw new IllegalArgumentException("Impossible to explore with a relativeMaxDepth of " + relativeMaxDepth);
      final int absoluteMaxDepth = rootFolder.getNameCount() + relativeMaxDepth;
      exploreCriteria = path -> (absoluteMaxDepth >= path.getNameCount());
      return this;
   }

   public FileGatherer withExploreCriteria(final Predicate<Path> subFolderCriteria)
   {
      Objects.requireNonNull(subFolderCriteria);
      this.exploreCriteria = subFolderCriteria;
      return this;
   }

   public FileGatherer withPathOrder(final Comparator<Path> pathOrder)
   {
      Objects.requireNonNull(pathOrder);
      this.pathOrder = pathOrder;
      return this;
   }

   /**
    * Also see Files.walk(Path) which does the same thing and might be more efficient but throws IOException.
    *
    * @see java.nio.file.Files#walk(Path, FileVisitOption...)
    */
   public static Stream<Path> search(final Path rootFolder)
   {
      return new FileGatherer().withRootFolder(rootFolder).search();
   }

   public static Stream<Path> searchForExtensions(final Path rootFolder, final String... extensions)
   {
      return FileGatherer.search(rootFolder).filter(Filters.acceptExtensions(extensions));
   }

   public Stream<Path> search()
   {
      return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new PathIterator(rootFolder, exploreCriteria, pathOrder),
            //Although it is sorted don't set Spliterator.SORTED because the impl will incorrectly return null
            Spliterator.ORDERED | Spliterator.NONNULL), false);
   }

   private static class PathIterator implements Iterator<Path>
   {
      private final Deque<Path> remaining = new ArrayDeque<>(32);
      private final Predicate<Path> exploreCriteria;
      private final Comparator<Path> pathOrder;

      private PathIterator(final Path rootFolder, final Predicate<Path> exploreCriteria, final Comparator<Path> pathOrder)
      {
         remaining.add(rootFolder);
         this.exploreCriteria = exploreCriteria;
         //pathOrder is reversed because next() always uses the last element
         this.pathOrder = pathOrder.reversed();
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
               remaining.addAll(Files.list(thisPath).sorted(pathOrder).collect(Collectors.toList()));
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
}

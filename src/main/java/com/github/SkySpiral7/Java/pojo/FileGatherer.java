package com.github.SkySpiral7.Java.pojo;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class FileGatherer
{
   private final Path rootFolder;
   private final Predicate<Path> gatherCriteria, exploreCriteria;
   private final int maxDepth, maxFinds;

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
      this.gatherCriteria = builder.getGatherCriteria();
      final int relativeMaxDepth = builder.getMaxDepth();
      this.maxDepth = (relativeMaxDepth == -1) ? relativeMaxDepth : (rootFolder.getNameCount() + 1);
      //+1 so that the rootFolder itself isn't counted
      if (maxDepth != -1)
      {
         this.exploreCriteria = builder.getExploreCriteria().and(path -> (maxDepth < path.getNameCount()));
      }
      else this.exploreCriteria = builder.getExploreCriteria();
      this.maxFinds = builder.getMaxFinds();
   }

   /**
    * Also see Files.walk(Path) which (if passed no file visitors) does the same thing (but is more efficient).
    *
    * @see java.nio.file.Files#walk(Path, FileVisitOption...)
    */
   public static List<File> search(final Path rootFolder)
   {
      return FileGatherer.search(rootFolder, Filters.ACCEPT_ALL);
   }

   public static List<File> search(final Path rootFolder, final Predicate<Path> fileCriteria)
   {
      return new Builder().rootFolder(rootFolder).gatherCriteria(fileCriteria).build().search();
   }

   public static List<File> searchForFilesWithExtensions(final Path rootFolder, final String... extensions)
   {
      return FileGatherer.search(rootFolder, Filters.acceptExtensions(extensions));
   }

   public List<File> search()
   {
      final List<File> result = new ArrayList<>();
      final Deque<Path> remaining = new ArrayDeque<>();

      //TODO: make an iterator to return a Stream<Path>. this removes gatherCriteria and maxFinds
      //TODO: add a comparator for file order (will still be depth first)
      remaining.add(rootFolder);
      try
      {
         while (!remaining.isEmpty())
         {
            final Path thisPath = remaining.pollLast();
            if (Files.isDirectory(thisPath) && exploreCriteria.test(thisPath))
               remaining.addAll(Files.list(thisPath).collect(Collectors.toList()));
            if (gatherCriteria.test(thisPath)) result.add(thisPath.toFile());
            if (maxFinds == result.size()) break;
         }
      }
      catch (final IOException ioException)
      {
         throw new UncheckedIOException("Could not open a directory", ioException);
      }

      Collections.sort(result);  //so that the results will have some kind of order (in this case full path alphabetical ascending)
      return result;
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
      private Predicate<Path> gatherCriteria, exploreCriteria;
      private int maxDepth, maxFinds;

      public Builder()
      {
         //the default root is where this class's project (or executable jar) is located
         rootFolder = Paths.get(".").toAbsolutePath().normalize();
         exploreCriteria = Filters.ACCEPT_ALL;
         gatherCriteria = Filters.EXCLUDE_DIRECTORIES;
         maxFinds = maxDepth = -1;
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

      public Builder unlimitedFinds()
      {
         this.maxFinds = -1;
         return this;
      }

      public Builder maxFinds(final int maxFinds)
      {
         if (maxFinds != -1 && maxFinds < 1) throw new IllegalArgumentException("Expected -1 or >= 1. Actual: " + maxFinds);
         this.maxFinds = maxFinds;
         return this;
      }

      public Builder rootFolder(final Path rootFolder)
      {
         Objects.requireNonNull(rootFolder);
         if (Files.notExists(rootFolder)) throw new IllegalArgumentException(rootFolder + " doesn't exist");
         if (Files.isRegularFile(rootFolder)) throw new IllegalArgumentException(rootFolder + " isn't a directory");
         this.rootFolder = rootFolder.toAbsolutePath().normalize();
         return this;
      }

      public Builder exploreCriteria(final Predicate<Path> subFolderCriteria)
      {
         Objects.requireNonNull(subFolderCriteria);
         this.exploreCriteria = subFolderCriteria;
         return this;
      }

      public Builder gatherCriteria(final Predicate<Path> fileCriteria)
      {
         Objects.requireNonNull(fileCriteria);
         this.gatherCriteria = fileCriteria;
         return this;
      }

      //**************************************************************************
      //Rest of file is generated getters
      //**************************************************************************
      public Predicate<Path> getExploreCriteria()
      {
         return exploreCriteria;
      }

      public Predicate<Path> getGatherCriteria()
      {
         return gatherCriteria;
      }

      public int getMaxDepth()
      {
         return maxDepth;
      }

      public int getMaxFinds()
      {
         return maxFinds;
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

   public Predicate<Path> getGatherCriteria()
   {
      return gatherCriteria;
   }

   public int getMaxDepth()
   {
      return maxDepth;
   }

   public int getMaxFinds()
   {
      return maxFinds;
   }

   public Path getRootFolder()
   {
      return rootFolder;
   }
}

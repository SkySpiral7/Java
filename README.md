Java
====
This repository is a holder for stand alone files or very small projects.
This was once part of the Miscellaneous repo but was separated to make it easier
to have a Java project in Eclipse. Note that the project is currently using JRE 1.8 although it can easily be changed to 1.6.

This repo largely contains code that has no tests and is possibly half finished.
The code might have unfinished or buggy functions but every other function worked under my use conditions.
Although that's assuming I ran the code at all. A lot of them only need Javadoc and are otherwise done.


#Tests
As expected "src" contains the code and "tests" contain the tests.
The tests run off of junit-4.11 with hamcrest-all-1.3.
It doesn't make sense to test an interface except for default and static methods.
I do not make tests for the sake of line coverage and to have a UT for each class.
Instead I only make meaningful unit tests: ones that test functionality that have a chance of being wrong.
The definition of a bean requires that they do not have functionality.

The unit tests are in the same package as the file being tested (but are in the tests folder instead of src).
The tests are named "UT_File.java" where "File" is the name of the class being tested.

The UT_FileToStringAdapter.java requires a few files to run.
.gitignore exists for the sole purpose of excluding largeFile.txt because it is larger than 4GB.
If you would like to test using the large file, the UT includes a method to recreate it.


#Works in progress
1. FileToStringAdapter

#Finished but Untested
1. BasicSetTheory
2. ComparableSugar
3. DequeNode
4. DequeNodeIterator
5. DescendingListIterator
6. FileGatherer
7. IteratorExternal
8. JsonHelper
9. JumpingIteratorExternalRandomAccess
10. LinkedList
11. ListIteratorExternal
12. MapEntryExternal
13. SimpleLogger

#Finished and Tested
1. JumpingIterator although an interface the static and default methods are tested

#Finished without anything to test
1. Copyable (interface)
2. Comparison (enum without functionality)
3. JumpingIteratorDecoratorSequential (delegates without functionality)
4. ListIndexOutOfBoundsException (exception class)
5. ModCountList (interface)
6. ReadOnlyIterator (delegates without test-worthy functionality)
7. ReadOnlyListIterator (delegates without test-worthy functionality)

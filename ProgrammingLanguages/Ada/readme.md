#Ada Merge Sort Assignment
Professor Goldberg

Compile with: gnatmake progmain
Run with: ./progmain < input.txt

input.txt contains the input numbers to be merge sorted.

##Instructions
Your assignment is to implement an Ada program that, among other things, performs a concurrent merge sort.

As described below, you will be creating three files:  sort.ads, sort.adb, and progmain.adb.  Please compress these files into a zip file and submit it via our NYU Classes page.

The sections of the Lovelace Ada tutorial that you will find the most relevant are:

Lesson 2 - Basic Ada Structure (Packages)
Lesson 13 - Tasks and Protected Types
However, since you will need to declare types, implement procedures, etc., you should be sure to go through at least the following additional lessons in the Lovelace tutorial: 1, 2, 3, 4, 5, 6, 8, 9.

Also, be sure to review the sample Ada programs that I have provided under "Ada Resources" in the course page.

Assignment
Your Ada program will have two parts:

A package, Sort, for performing concurrent merge sort.
A main procedure, ProgMain.
The Sort Package
You should define a package called Sort whose specification appears in the file sort.ads and whose body appears in the file sort.adb.  The Sort package should export (i.e. declare in its specification) the following:

An integer constant SIZE, which you should define to be 40. This constant should be used throughout the program, rather than the actual number (so that if you change only this number, your program will still works).
A type (you can name it what you want) that defines the type of the array to be sorted, namely an array of integers whose values range between -500 and 500, inclusive. The size of the array should be SIZE.
A procedure MergeSort(A), where the parameter A is an array of the type you defined above. After the call to MergeSort, the elements of A should  be in sorted, increasing order.
Within the body of the Sort package, you will define the MergeSort procedure. This procedure should sort the array parameter A using a concurrent merge sort algorithm. You can use any merge sort algorithm you like (a description of several different merge sort algorithms is discussed in the Wikipedia page on merge sort). However, at any point in the program where sorting occurs on two or more portions of the array, the sorting of those portions must occur concurrently (the same Wikipedia page also discusses parallel merge sort which indicates one way to do this).

The ProgMain Procedure
In a separate file, progmain.adb, you should define the procedure ProgMain. It should declare an array of the type defined in the Sort package, read in values for the elements of the array, call MergeSort to sort the array, and compute the sum of the elements of the array. It should accomplish this using at least three tasks, as follows:

Reader: This task should read in SIZE integers from the terminal and write them into the array.
Sum: This task should compute the sum of the elements of the array.
Printer: This task should print the elements of the array and then print the sum computed by the Sum task.
The actual call to the MergeSort procedure should be from the body of the ProgMain procedure, not from within one of the above tasks. Of course, the MergeSort procedure shouldn't start running until all the numbers have been read into the array, and the printing of the array shouldn't happen until after the sorting is complete. Furthermore, the computing of the sum and the printing of the elements of the array after it has been sorted should happen concurrently. That is, the running of the above tasks should happen as concurrently as possible.

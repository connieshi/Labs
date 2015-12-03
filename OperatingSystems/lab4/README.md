#Lab4: Demand Paging

## Professor Allan Gottlieb
* Compile with: javac DemandPaging.java
* Run with: java DemandPaging M P S J N R 0

where...
* M, the machine size in words
* P, the page size in words
* S, the size of a process, i.e., the references are to virtual addresses 0..S-1
* J, the ‘‘job mix’’, which determines A, B, and C, as described below
* N, the number of references for each process
* R, the replacement algorithm: lifo, random, or lru

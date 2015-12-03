import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Connie Shi
 * Lab 4: Demand Paging, Professor Gottlieb
 * Operating Systems
 * 
 * Simulates demand paging using LRU, LIFO, and random eviction algorithms.
 */
public class DemandPaging {
  
  /**
   * Driver to simulate demand paging
   */
  static class Driver {
    List<Integer> randomNumbers;
    List<Frame> frames;
    List<Process> processes;
    int numberPointer = 0;
    int machineSize, pageSize, processSize, jobMix, nReferences;
    String replacement;

    @Override
    public String toString() {
      return "Driver [frames=" + frames
          + ", processes=" + processes + ", numberPointer=" + numberPointer
          + ", machineSize=" + machineSize + ", pageSize=" + pageSize
          + ", processSize=" + processSize + ", jobMix=" + jobMix
          + ", nReferences=" + nReferences + ", replacement=" + replacement + "]";
    }
  }

  /**
   * A frame in memory containing physical addresses.
   */
  static class Frame {
    int id;
    boolean valid;
    int processId;
    int pageId;
    int loadTime; // Time frame was loaded by process
    int timeUsed; // Time frame was last used

    Frame(int id) {
      this.id = id;
    }

    @Override
    public String toString() {
      return "Frame [id=" + id + ", valid=" + valid + ", processId="
          + processId + ", pageId=" + pageId + ", loadTime=" + loadTime
          + ", timeUsed=" + timeUsed + "]";
    }
  }

  /**
   * A process that reads pages to load into frames.
   */
  static class Process {
    int id;
    int cyclesLeft;
    int evictions;
    int faults;
    int residencyTime;
    
    // Save current reference and next reference
    int nextRef = -1;
    int oldRef = -1;
    
    // Values used by job mix
    double A;
    double B;
    double C;

    Process(int id, int cyclesLeft) {
      this.id = id;
      this.cyclesLeft = cyclesLeft;
      this.evictions = 0;
      this.residencyTime = 0;
    }

    @Override
    public String toString() {
      return "Process [id=" + id + ", cyclesLeft=" + cyclesLeft
          + ", evictions=" + evictions + ", faults=" + faults
          + ", residencyTime=" + residencyTime + ", nextRef=" + nextRef
          + ", oldRef=" + oldRef + ", A=" + A + ", B=" + B + ", C=" + C + "]";
    }
  }

  /**
   * @throws IOException
   * Reads the "random-numbers" and store in ArrayList
   */
  public static void readRandomNumbers(Driver driver) throws IOException {
    driver.randomNumbers = new ArrayList<Integer>();
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader("random-numbers"));
      String line;
      while ((line = br.readLine()) != null) {
        int n = Integer.parseInt(line);
        driver.randomNumbers.add(n);
      }
      br.close();
    } catch (IOException e) {
      System.out.println(e.getMessage());
      System.exit(1);
    }
  }

  /**
   * @param driver
   * Sets up frames list and process list with the right job mix
   */
  public static void setUp(Driver driver) {
    int nFrames = driver.machineSize / driver.pageSize;
    driver.numberPointer = 0;

    // Sets up frames
    driver.frames = new ArrayList<Frame>();
    for (int i = 0; i < nFrames; i++) {
      Frame f = new Frame(i);
      driver.frames.add(f);
    }

    // Sets up processes according to job mix
    driver.processes = new ArrayList<Process>();
    Process one, two, three, four;
    switch (driver.jobMix) {
      case 1:
        one = new Process(1, driver.nReferences);
        one.A = 1;
        driver.processes.add(one);
        break;
      case 2:
        one = new Process(1, driver.nReferences);
        two = new Process(2, driver.nReferences);
        three = new Process(3, driver.nReferences);
        four = new Process(4, driver.nReferences);
        one.A = 1;
        two.A = 1;
        three.A = 1;
        four.A = 1;
        driver.processes.add(one);
        driver.processes.add(two);
        driver.processes.add(three);
        driver.processes.add(four);
        break;
      case 3:
        one = new Process(1, driver.nReferences);
        two = new Process(2, driver.nReferences);
        three = new Process(3, driver.nReferences);
        four = new Process(4, driver.nReferences);
        driver.processes.add(one);
        driver.processes.add(two);
        driver.processes.add(three);
        driver.processes.add(four);
        break;
      case 4:
        one = new Process(1, driver.nReferences);
        two = new Process(2, driver.nReferences);
        three = new Process(3, driver.nReferences);
        four = new Process(4, driver.nReferences);
        one.A = .75;
        one.B = .25;
        two.A = .75;
        two.C = .25;
        three.A = .75;
        three.B = .125;
        three.C = .125;
        four.A = .5;
        four.B = .125;
        four.C = .125;
        driver.processes.add(one);
        driver.processes.add(two);
        driver.processes.add(three);
        driver.processes.add(four);
        break;
    }
  }

  /**
   * @param driver
   * @param p
   * @param nextRef
   * @return
   * Returns the first reference if nextRef is -1, otherwise
   * Calculate the next reference using formula given in the assignment instructions.
   */
  public static int getRef(Driver driver, Process p, int nextRef) {
    // nextRef == -1 denotes the first time a process is run
    if (nextRef == -1) {
      return firstRef(driver, p);
    }

    // Otherwise the reference is built off of oldRef
    int rand = driver.randomNumbers.get(driver.numberPointer++);
    System.out.printf("%d uses random number: %d\n", p.id, rand);
    
    double y = rand / (Integer.MAX_VALUE + 1d);
    if (y < p.A) {
      return (nextRef + 1);
    } else if (y < (p.A + p.B)) {
      return (nextRef - 5 + driver.processSize) % driver.processSize;
    } else if (y < (p.A + p.B + p.C)) {
      return nextRef + 4;
    } else {
      int rand2 = driver.randomNumbers.get(driver.numberPointer++);
      System.out.printf("%d uses random number: %d\n", p.id, rand2);
      return rand2 % driver.processSize;
    }    
  }

  /**
   * @param driver
   * @param p
   * @return
   * The first reference used by any process that's running for the first time.
   */
  public static int firstRef(Driver driver, Process p) {
    return (111 * p.id) % driver.processSize;
  }

  /**
   * @param id
   * @param word
   * @param pageNumber
   * @param time
   * @param s
   * Print out the debug message.
   */
  public static void printDebug(int id, int word, int pageNumber, int time, String s) {
    System.out.printf("%d references word %d (page %d) at time %d: %s\n",
        id, word, pageNumber, time, s);
  }

  /**
   * @param driver
   * Simulate demand paging using lru, lifo, or random algorithms.
   */
  public static void simulate(Driver driver) {
    int time = 1;
    int processPointer = 0;

    while (!allDone(driver)) {
      Process current = driver.processes.get(processPointer);

      // Run every process for a maximum of quantum = 3
      int quantum = 3;
      while (quantum-- != 0) {
        // If no cycles left, process is done
        if (current.cyclesLeft == 0) {
          break;
        }

        // Current reference is either the first reference formula
        // Or it's nextRef stored by the last run
        int ref;
        if (current.nextRef == -1) {
          ref = getRef(driver, current, current.nextRef) % driver.processSize;
        } else {
          ref = current.nextRef;
        }

        // Calculate page number that the reference is in, integer division rounds down
        int page = ref / driver.pageSize;
        
        // 3 cases can happen:
        //  1. Page is a hit
        //  2. Page not found and there's a free frame to load the new frame
        //  3. Page not found and there are  no free frames, so eviction is needed
        
        int hitFrameIndex, freeFrameIndex;
        if ((hitFrameIndex = hit(driver, current, page, time)) != -1) {
          printDebug(current.id, ref, page, time, "Hit in frame " + hitFrameIndex);  
        } else if ((freeFrameIndex = freeFrame(driver, current, page)) != -1) {
          printDebug(current.id, ref, page, time, "Fault, using free frame " 
              + freeFrameIndex + ".");
          useFreeFrame(driver, freeFrameIndex, current, page, time);
          current.faults++;
        } else {
          evict(driver, current, time, ref, page);
        }

        // Set up states for next cycle in while loop
        current.cyclesLeft--;
        current.oldRef = ref;
        current.nextRef = getRef(driver, current, current.oldRef) % driver.processSize;
        time++;
      }
      
      // Use the next process in the list or wrap around to 0 if at end of list
      processPointer = (processPointer + 1) % driver.processes.size();
    }
  }

  /**
   * @param driver
   * @param index
   * @param current
   * @param page
   * @param time
   * Fill a frame by setting up all necessary fields.
   */
  public static void useFreeFrame(Driver driver, int index, Process current, int page, int time) {
    Frame freeFrame = driver.frames.get(index);
    freeFrame.valid = true;
    freeFrame.loadTime = time;
    freeFrame.pageId = page;
    freeFrame.processId = current.id;
    freeFrame.timeUsed = time;
  }

  /**
   * @param driver
   * @param current
   * @param time
   * @param ref
   * @param page
   * Use lru, lifo, or random algorithm to evict a page and replace it with the current page
   */
  public static void evict(Driver driver, Process current, int time, int ref, int page) {
    int evictFrameIndex = 0; 
    
    // Use one eviction algorithm indicated by input
    switch (driver.replacement) {
      case "lru" : evictFrameIndex = getEvictFrameLRU(driver); break;
      case "random": evictFrameIndex = getEvictFrameRandom(driver, current); break;
      case "lifo": evictFrameIndex = getEvictFrameLIFO(driver); break;
    }
    
    // Get victim frame and process victim frame belongs to
    Frame evictFrame = driver.frames.get(evictFrameIndex);
    Process prev = driver.processes.get(evictFrame.processId - 1);
    printDebug(current.id, ref, page, time, "Fault, evicting page " + evictFrame.pageId 
        + " of " + prev.id + " from frame " + evictFrame.id + ".");

    // Set states for eviction
    evictFrame.valid = false;
    prev.evictions++;
    prev.residencyTime += (time - evictFrame.loadTime);
    current.faults++;

    // Replace victim with new frame from new process
    useFreeFrame(driver, evictFrameIndex, current, page, time);
  }

  /**
   * @param driver
   * @return
   * Returns the index position of the frame that was least recently used,
   * in other words, the frame that has the lowest timeUsed value.
   */
  public static int getEvictFrameLRU(Driver driver) {
    int lruFrame = 0;
    int lruTime = Integer.MAX_VALUE;
    for (int i = 0; i < driver.frames.size(); i++) {
      if (lruTime >= driver.frames.get(i).timeUsed) {
        lruFrame = i;
        lruTime = driver.frames.get(i).timeUsed;
      }
    }
    return lruFrame;
  }
  
  /**
   * @param driver
   * @param current
   * @return
   * Returns the index position of a random frame determined by random number % number of frames.
   */
  public static int getEvictFrameRandom(Driver driver, Process current) {
    int rand = driver.randomNumbers.get(driver.numberPointer++);
    System.out.printf("%d uses random number: %d\n", current.id, rand);
    return rand % driver.frames.size();
  }
  
  /**
   * @param driver
   * @return
   * Returns the index position of victim frame that was loaded last,
   * in other words, the frame that has the highest loadTime value.
   */
  public static int getEvictFrameLIFO(Driver driver) {
    int lifoFrame = 0;
    int lifoTime = 0;
    for (int i = 0; i < driver.frames.size(); i++) {
      if (lifoTime <= driver.frames.get(i).loadTime) {
        lifoFrame = i;
        lifoTime = driver.frames.get(i).loadTime;
      }
    }
    return lifoFrame;
  }

  /**
   * @param driver
   * @param p
   * @param pageNumber
   * @param time
   * @return
   * Returns the index position of the page hit or -1 if no hits.
   */
  public static int hit(Driver driver, Process p, int pageNumber, int time) {
    for (int i = 0; i < driver.frames.size(); i++) {
      Frame f = driver.frames.get(i);
      if (f.valid && f.processId == p.id && f.pageId == pageNumber) {
        f.timeUsed = time;
        return f.id;
      }
    }
    return -1;
  }

  /**
   * @param driver
   * @param p
   * @param pageNumber
   * @return
   * Returns index position of the highest id free frame or -1 if no free frames.
   */
  public static int freeFrame(Driver driver, Process p, int pageNumber) {
    for (int i = driver.frames.size() - 1; i >= 0; i--) {
      Frame f = driver.frames.get(i);
      if (!f.valid) {
        return f.id;
      }
    }
    return -1;
  }

  /**
   * @param driver
   * @return
   * Returns true if all processes have no cycles left to run.
   */
  public static boolean allDone(Driver driver) {
    for (int i = 0; i < driver.processes.size(); i++) {
      if (driver.processes.get(i).cyclesLeft != 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * @param driver
   * Prints for every process the number of faults and the average residence.
   * The average residence is calculated by residency time divided by number of evictions.
   */
  public static void printAnalysis(Driver driver) {
    int totalResidencyTime = 0;
    int totalEvictions = 0;
    int totalFaults = 0;

    System.out.println();
    for (int i = 0; i < driver.processes.size(); i++) {
      Process p = driver.processes.get(i);
      double averageResidency = (double) p.residencyTime / p.evictions;
      if (p.evictions == 0) {
        System.out.printf("Process %d had %d faults and the average residence is undefined.\n", 
            p.id, p.faults);
      } else {
        System.out.printf("Process %d had %d faults and %f average residency.\n", 
            p.id, p.faults, averageResidency);
      }
      
      // Calculate for total analysis
      totalResidencyTime += p.residencyTime;
      totalEvictions += p.evictions;
      totalFaults += p.faults;
    }

    // Print total analysis of all processes in the system
    System.out.println();
    if (totalEvictions == 0) {
      System.out.printf("The total number of faults is %d and the overall average residency "
          + "is undefined.\n", totalFaults);
    } else {
      double totalAverageResidency = (double) totalResidencyTime / totalEvictions;
      System.out.printf("The total number of faults is %d and the overall average residency "
          + "is %f.\n", totalFaults, totalAverageResidency);
    }
  }

  public static void main(String[]args) throws IOException {
    // Check that arguments are provided
    if (args.length < 6) {
      System.out.println("Not enough arguments, enter: java DemandPaging M P S J N R");
      System.exit(1);
    }

    // Read and store inputs
    Driver driver = new Driver();
    driver.machineSize = Integer.parseInt(args[0]);
    driver.pageSize = Integer.parseInt(args[1]);
    driver.processSize = Integer.parseInt(args[2]);
    driver.jobMix = Integer.parseInt(args[3]);
    driver.nReferences = Integer.parseInt(args[4]);
    driver.replacement = args[5];
    
    // Simulate demand paging only if replacement algorithm strings are valid
    if (driver.replacement.equals("lru") || driver.replacement.equals("random") 
        || driver.replacement.equals("lifo")) {
      readRandomNumbers(driver);
      setUp(driver);
      simulate(driver);
      printAnalysis(driver);
    } else {
      System.out.println("Incorrect replacement algorithm, must be lru, lifo, or random.");
      System.exit(1);
    }
  }
}

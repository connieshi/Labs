import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 * @author Connie Shi
 * Scheduler to perform one of the following scheduling policies to run {@link Process}es:
 * First Come First Served
 * Round Robin
 * Uniprogrammed 
 * Shorted Job First
 */
public class Scheduler {
  
  /**
   * Represents a single CPU object.
   */
  static class CPU {
    int cycle;
    int cpuUtilization;
    int ioUtilization;
    int nProcesses;

    public CPU(int nProcesses) {
      cycle = 0;
      cpuUtilization = 0;
      ioUtilization = 0;
      this.nProcesses = nProcesses;
    }
  }

  // Set quantum for round robin
  private static final int QUANTUM = 2;
  
  // List of random numbers to be used to calculate CPU and IO bursts
  private static ArrayList<Integer> randomNumbers = new ArrayList<Integer>();
  
  // Index to get a random number
  private static int randomIndex = 0;
  
  // List of processes to be run in their original states
  private static ArrayList<Process> processes = new ArrayList<Process>();
  
  // Flag for verbose output
  private static boolean verbose = false;
  
  // Original input of processes
  private static String originalInput;

  /**
   * First come first served policy runs until blocked or termination.
   * When a process is blocked, the next process with the earliest arrival time is scheduled to run.
   * Non-preemptive scheduling policy.
   */
  public static void firstComeFirstServed() {
    ArrayList<Process> fcfs = copyProcesses();
    LinkedList<Process> queue = new LinkedList<Process>();
    CPU cpu = new CPU(fcfs.size());

    // Print out initial states
    printInput(fcfs);
    printProcessStates(fcfs, cpu.cycle);

    // Run until all processes are terminated
    while (!allTerminated(fcfs)) {
      addToQueue(fcfs, queue, cpu.cycle);

      // If there is a ready job in queue, run it
      if (!queue.isEmpty()) {
        Process current = queue.removeFirst();
        current.setState(Process.State.running);
        
        // Set CPU and IO bursts according to random number
        current.setCpuBurstTime(getRandomNumber(current));
        current.setIoBurstTime(current.getCpuBurstTime() * current.getM());

        // Run for all of CPU burst or until termination
        while (current.getCpuBurstTime() > 0) {
          cpu.cycle++;
          cpu.cpuUtilization++;
          printProcessStates(fcfs, cpu.cycle);
          setOtherProcesses(fcfs, cpu);
          addToQueue(fcfs, queue, cpu.cycle);

          // Run the current process and return true if is terminated
          if (runIsFinished(current, cpu)) {
            break;
          }
        }
        
        // If CPU burst is done, set state to perform blocked IO operations in next cycle
        if (current.getState() == Process.State.running) {
          current.setState(Process.State.blocked);
        }
      } else {  // No process is ready to run (all blocked)
        runBlocked(cpu, fcfs);
        addToQueue(fcfs, queue, cpu.cycle);
      }
    }

    // Final print out of each process summary and overall summary data
    System.out.println("\nThe scheduling algorithm used was First Come First Served\n");
    printProcessSummary(fcfs);
    printCpuSummary(cpu, fcfs);
  }

  /**
   * Round robin scheduling policy runs until quantum has been reached, or until IO block, or
   * until termination. Preemptive policy.
   */
  public static void roundRobin() {
    ArrayList<Process> rr = copyProcesses();
    LinkedList<Process> queue = new LinkedList<Process>();
    CPU cpu = new CPU(rr.size());

    // Print out initial states
    printInput(rr);
    printProcessStates(rr, cpu.cycle);

    // While there are still processes that need to be executed
    while (!allTerminated(rr)) {
      addToQueue(rr, queue, cpu.cycle);

      // If there is a process that's ready to run
      if (!queue.isEmpty()) {
        Process current = queue.removeFirst();
        current.setState(Process.State.running);
        
        // Set CPU and IO burst times
        if (current.getCpuBurstTime() == 0) {
          current.setCpuBurstTime(getRandomNumber(current));
          current.setIoBurstTime(current.getCpuBurstTime() * current.getM());
        }
        
        // Temporary variable to store how many cycles have been used until quantum is reached
        int quantum = 0;

        // If both CPU burst and quantum hasn't been reached 
        while (current.getCpuBurstTime() > 0 && quantum < QUANTUM) {
          quantum++;
          cpu.cycle++;
          cpu.cpuUtilization++;
          printProcessStates(rr, cpu.cycle);
          setOtherProcesses(rr, cpu);
          
          // Run the current process and return true if is terminated
          if (runIsFinished(current, cpu)) {
            break;
          }
          
          if (quantum < QUANTUM) {
            addToQueue(rr, queue, cpu.cycle);
          }
        }
        
        if (current.getCpuBurstTime() == 0 && current.getState() == Process.State.running) {
          current.setState(Process.State.blocked);
        } else if (current.getCpuBurstTime() > 0 && current.getState() == Process.State.running) {
          current.setState(Process.State.ready);
        }
      } else { // All processes are blocked
        runBlocked(cpu, rr);
        addToQueue(rr, queue, cpu.cycle);
      }
    }

    // Print out summaries
    System.out.println("\nThe scheduling algorithm used was Round Robin\n");
    printProcessSummary(rr);
    printCpuSummary(cpu, rr);
  }

  /**
   * Runs one process at a time until completion. Does not switch processes when a process 
   * is blocked.
   */
  public static void uniprogrammed() {
    ArrayList<Process> uni = copyProcesses();
    LinkedList<Process> queue = new LinkedList<Process>();
    CPU cpu = new CPU(uni.size());

    // Print out initial states
    printInput(uni);
    printProcessStates(uni, cpu.cycle);

    // While not all processes have terminated
    while (!allTerminated(uni)) {
      addToQueue(uni, queue, cpu.cycle);
      Process current = queue.removeFirst();
      current.setState(Process.State.running);

      // Run the current process until it is terminated
      while (current.getState() != Process.State.terminated) {
        current.setCpuBurstTime(getRandomNumber(current));
        current.setIoBurstTime(current.getCpuBurstTime() * current.getM());

        // Run for all of cpu burst or until terminated
        while (current.getCpuBurstTime() > 0) {
          cpu.cycle++;
          cpu.cpuUtilization++;
          printProcessStates(uni, cpu.cycle);
          calculateWaitingTime(uni);
          addToQueue(uni, queue, cpu.cycle);
          if (runIsFinished(current, cpu)) {
            break;
          }
        }

        // Change state to blocked if it hasn't terminated
        if (current.getState() == Process.State.running) {
          current.setState(Process.State.blocked);
        }

        // Run for all of io burst if it hasn't terminated
        while (current.getC() > 0 && current.getIoBurstTime() > 0) {
          cpu.cycle++;
          cpu.ioUtilization++;
          printProcessStates(uni, cpu.cycle);
          calculateWaitingTime(uni);
          addToQueue(uni, queue, cpu.cycle);

          // Decrement io burst time
          if (current.getIoBurstTime() > 0) {
            current.decrementIoBurstTime();
            current.incrementTotalIOTime();
            if (current.getIoBurstTime() == 0) {
              current.setState(Process.State.running);
              break;
            }
          }
        }
      }
    }

    // Final print out of summaries
    System.out.println("\nThe scheduling algorithm used was Uniprocessing\n");
    printProcessSummary(uni);
    printCpuSummary(cpu, uni);
  }

  /**
   * Shortest job first picks the next job to run based on least CPU time left.
   * Non preemptive scheduling policies that runs until block or termination.
   */
  public static void shortestJobFirst() {
    ArrayList<Process> sjf = copyProcesses();
    CPU cpu = new CPU(sjf.size());

    // Print initial states
    printInput(sjf);
    printProcessStates(sjf, cpu.cycle);

    // While there is a process that still needs to be run
    while (!allTerminated(sjf)) {
      setReady(sjf, cpu);
      Process current = getShortestJob(sjf);
      
      // If all jobs are blocked
      if (current == null) {
        setReady(sjf, cpu);
        runBlocked(cpu, sjf);
        printProcessStates(sjf, cpu.cycle);
        continue;
      }
      
      // Otherwise, job can be run
      current.setState(Process.State.running);
      current.setCpuBurstTime(getRandomNumber(current));
      current.setIoBurstTime(current.getCpuBurstTime() * current.getM());

      // Run for cpu burst or until termination
      while (current.getCpuBurstTime() > 0) {
        cpu.cycle++;
        cpu.cpuUtilization++;
        printProcessStates(sjf, cpu.cycle);
        setOtherProcesses(sjf, cpu);
        setReady(sjf, cpu);

        // Run for one cycle and check if process has terminated
        if (runIsFinished(current, cpu)) {
          break;
        }
      }
      
      // If process hasn't terminated, set state to blocked
      if (current.getState() == Process.State.running) {
        current.setState(Process.State.blocked);
      }
    }

    // Print out summaries
    System.out.println("\nThe scheduling algorithm used was Shortest Job First\n");
    printProcessSummary(sjf);
    printCpuSummary(cpu, sjf);
  }
  
  /**
   * @param current
   * @param cpu
   * @return true if process has terminated
   * 
   * Runs a process for one cycle and returns true if process has terminated.
   */
  public static boolean runIsFinished(Process current, CPU cpu) {
    // If the process can be run
    if (current.getC() > 0) {
      current.decrementCpuBurstTime();
      current.decrementC();
      
      // Process has finished
      if (current.getC() == 0) {
        current.setState(Process.State.terminated);
        current.setFinishingTime(cpu.cycle);
        return true;
      } else {
        return false;
      }
    } else {
      return true;
    }
  }
  
  /**
   * @param cpu
   * @param list
   * 
   * Run for one cycle when all processes are blocked.
   */
  public static void runBlocked(CPU cpu, ArrayList<Process> list) {
    cpu.cycle++;
    printProcessStates(list, cpu.cycle);
    setOtherProcesses(list, cpu);
  }

  /**
   * @param list
   * @param cpu
   * 
   * When a process arrives, set the state to ready.
   */
  public static void setReady(ArrayList<Process> list, CPU cpu) {
    for (Process p : list) {
      if (p.getA() <= cpu.cycle && p.getState() == Process.State.unstarted) {
        p.setState(Process.State.ready);
      }
    }
  }
  
  /**
   * @param list
   * @return
   * 
   * Retrieve the shortest job that can be run based on CPU time left,
   * or return null if no process is ready.
   */
  public static Process getShortestJob(ArrayList<Process> list) {
    int shortestLeft = Integer.MAX_VALUE;
    Process process = null;
    
    for (Process p : list) {
      if (p.getState() == Process.State.ready && p.getC() < shortestLeft) {
        process = p;
        shortestLeft = p.getC();
      }
    }
    return process;
  }

  /**
   * @throws IOException
   * 
   * Reads the "random-numbers" and store in ArrayList
   */
  public static void readRandomNumbers() throws IOException {
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader("random-numbers"));
      String line;
      while ((line = br.readLine()) != null) {
        int n = Integer.parseInt(line);
        randomNumbers.add(n);
      }
      br.close();
    } catch (IOException e) {
      System.out.println(e.getMessage());
      System.exit(1);
    }
  }

  /**
   * @param args
   * 
   * Parses arguments of the program and process file.
   */
  public static void readInputFile(String[] args) {
    if (args.length == 1) {
      processFile(args[0]);
    } else if (args.length == 2) {
      if (args[0].equals("--verbose")) {
        verbose = true;
        processFile(args[1]);
      } else {
        System.out.println("Run with: java Scheduler [--verbose] InputFileName.txt");
        System.exit(1);
      }
    } else {
      System.out.println("Run with: java Scheduler [--verbose] InputFileName.txt");
      System.exit(1);
    }
  }

  /**
   * @param fileName
   * 
   * Process the file and create Process objects. Then sort by arrival time.
   */
  public static void processFile(String fileName) {
    try {
      BufferedReader br = new BufferedReader(new FileReader(fileName));
      String line = br.readLine().trim();
      
      // Replace parentheses with blank spaces for easier parsing
      line = line.replace('(', ' ');
      line = line.replace(')', ' ');
      String[] input = line.split(" +"); // One or more spaces
      
      // Number of processes and index position of input array
      int nProcesses = Integer.parseInt(input[0]);
      int index = 1;

      // Construct String for original input in order as presented in input file
      originalInput = "The original input was: " + nProcesses + " ";
      
      // Create Process objects and add to array
      while (nProcesses-- != 0) {
        Process process = new Process(
            Integer.parseInt(input[index++]),
            Integer.parseInt(input[index++]),
            Integer.parseInt(input[index++]),
            Integer.parseInt(input[index++]));
        processes.add(process);
        originalInput += process.toPrettyString() + " ";
      }

      // Sort processes by arrival time and set their rankings
      Collections.sort(processes);
      for (int i = 0; i < processes.size(); i++) {
        processes.get(i).setRank(i);
      }

      br.close();
    } catch (FileNotFoundException e) {
      System.out.println(e.getMessage());
      System.exit(1);
    } catch (IOException e) {
      System.out.println(e.getMessage());
      System.exit(1);
    }
  }

  /**
   * @param list
   * 
   * Calculate waiting time for all processes that's in the ready state.
   */
  public static void calculateWaitingTime(ArrayList<Process> list) {
    for (Process p : list) {
      if (p.getState() == Process.State.ready) {
        p.incrementWaitingTime();
      }
    }
  }

  /**
   * @param sortedInput
   * 
   * Print input of processes after it has been sorted by arrival time.
   */
  public static void printInput(ArrayList<Process> sortedInput) {
    System.out.println(originalInput);
    String sorted = "The (sorted) input is: " + processes.size() + " ";

    for (Process p : sortedInput) {
      sorted += p.toPrettyString() + " ";
    }

    System.out.println(sorted + "\n");
  }

  /**
   * @param fcfs
   * @param cpu
   * 
   * For all processes, if the state is blocked, decrement io burst time -- until io burst time 
   * reaches 0, then it is ready to be run.
   * If state is ready, increment waiting time.
   */
  public static void setOtherProcesses(ArrayList<Process> current, CPU cpu) {
    boolean hasBlocked = false; // If there is a process that's blocked
    
    for (Process process : current) {
      if (process.getState() == Process.State.blocked) {
        hasBlocked = true;
        process.decrementIoBurstTime();
        process.incrementTotalIOTime();
        
        // If IO is done, set state back to ready
        if (process.getIoBurstTime() == 0) {
          process.setState(Process.State.ready);
        }
      } else if (process.getState() == Process.State.ready) { 
        process.incrementWaitingTime();
      }
    }

    // If there is a process that's blocked, CPU is utilization IO
    if (hasBlocked) {
      cpu.ioUtilization++;
    }
  }

  /**
   * @param processes
   * @param cycle
   * 
   * Print the states and cycles left of every process in that state for verbose output.
   */
  public static void printProcessStates(ArrayList<Process> processes, int cycle) {
    // If verbose is false, do not print the process states
    if (!verbose) {
      return;
    }
    
    String output = "Before cycle \t " + cycle + ":\t";
    for (Process process : processes) {
      output += process.getState() + "\t";
      switch (process.getState()) {
        case blocked: output += "\t" + process.getIoBurstTime() + "\t";
        break;
        case ready: output += "\t" + "0" + "\t";
        break;
        case running: output += "\t" + process.getCpuBurstTime() + "\t";
        break;
        case terminated: output += "0" + "\t";
        break;
        case unstarted: output += "0" + "\t";
        break;
      }
    }
    System.out.println(output);
  }

  /**
   * @param currentList
   * 
   * Print summary of each process with:
   * Finishing time - the cycle where a process terminated
   * Turnaround time - the cycle time between arrival and termination
   * IO time - the total time a process spends blocked in IO
   * Waiting time - the total time that a process spends in ready state waiting to be picked up
   */
  public static void printProcessSummary(ArrayList<Process> currentList) {
    for (int i = 0; i < processes.size(); i++) {
      Process p = currentList.get(i);
      System.out.println("Process " + p.getRank() + ":");
      System.out.println("\t (A,B,C,M) = " + processes.get(i).toPrettyString());
      System.out.println("\t Finishing time: " + p.getFinishingTime());
      System.out.println("\t Turnaround time: " + (p.getFinishingTime() - p.getA()));
      System.out.println("\t I/O time: " + p.getTotalIOTime());
      System.out.println("\t Waiting time: " + p.getWaitingTime());
      System.out.println();
    }
  }

  /**
   * @param cpu
   * @param currentList
   * 
   * Prints the summary of the CPU overall
   */
  public static void printCpuSummary(CPU cpu, ArrayList<Process> currentList) {
    DecimalFormat df = new DecimalFormat("0.000000");
    int totalTurnaroundTime = 0;
    int totalWaitingTime = 0;

    // Calculate total turnaround time and total waiting time of all processes
    for (Process p : currentList) {
      totalTurnaroundTime += p.getFinishingTime() - p.getA();
      totalWaitingTime += p.getWaitingTime();
    }

    String result = "Summary Data: \n";
    
    // The cycle in which all processes terminated
    result += "\t Finishing Time: " + cpu.cycle + "\n";
    
    // The percentage of time at least one process is running on the CPU
    result += "\t CPU Utilization: " + df.format((double) cpu.cpuUtilization/cpu.cycle) + "\n";
    
    // The percentage of time at least one process is in blocked IO
    result += "\t I/O Utilization: " + df.format((double) cpu.ioUtilization/cpu.cycle) + "\n";
    
    // The number of processes ran over the number of cycles to complete each process * 100
    result += "\t Throughput: " + df.format((double) cpu.nProcesses/cpu.cycle * 100) 
        + " processes per hundred cycles \n";
    
    // The average turnaround time of all processes together
    result += "\t Average turnaround time: " + df.format((double) totalTurnaroundTime
        / (double) cpu.nProcesses) + "\n";
    
    // The average waiting time of all processes together
    result += "\t Average waiting time: " + df.format((double)totalWaitingTime 
        / (double) cpu.nProcesses) + "\n";
    
    System.out.println(result);
    System.out.println("*********************************************************************\n");
  }

  /**
   * @param process
   * @return
   * 
   * Generate the next random number by reading it from the list of numbers and % by B
   */
  public static int getRandomNumber(Process process) {
    int X = randomNumbers.get(randomIndex);
    randomIndex++;
    return 1 + (X % process.getB());
  }

  /**
   * @param list
   * @param queue
   * @param cycle
   * 
   * Add all process to queue that are not already there, that have arrive, and whose state is
   * either unstarted or ready. Note that this ensures lab 2 tie break rule because it adds 
   * processes in order from earliest arrival time to latest.
   */
  public static void addToQueue(ArrayList<Process> list, LinkedList<Process> queue, int cycle) {
    for (Process p : list) {
      if (!queue.contains(p) && p.getA() <= cycle 
          && (p.getState() == Process.State.ready || p.getState() == Process.State.unstarted)) {
        if (p.getState() == Process.State.unstarted) {
          p.setState(Process.State.ready);
        }
        queue.add(p);
      }
    }
  }

  /**
   * @param processes
   * @return true if all processes have terminated
   */
  public static boolean allTerminated(ArrayList<Process> processes) {
    for (Process p : processes) {
      if (!(p.getState() == Process.State.terminated)){
        return false;
      }
    }
    return true;
  }

  /**
   * @return an ArrayList<Process> that is the copy of the original processes input then sorted
   */
  public static ArrayList<Process> copyProcesses() {
    ArrayList<Process> newList = new ArrayList<Process>();
    for (Process process : processes) {
      Process p = new Process(
          process.getA(), 
          process.getB(), 
          process.getC(), 
          process.getM()
          );
      p.setRank(process.getRank());
      newList.add(p);
    }
    return newList;
  }

  /**
   * @param args
   * @throws IOException
   * 
   * Runs scheduling policies
   */
  public static void main(String[] args) throws IOException {
    readRandomNumbers();
    readInputFile(args);
    
    randomIndex = 0;
    firstComeFirstServed();
    
    randomIndex = 0;
    roundRobin();
    
    randomIndex = 0;
    uniprogrammed();
    
    randomIndex = 0;
    shortestJobFirst();
  }
}

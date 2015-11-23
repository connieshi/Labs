
/**
 * @author Connie Shi
 * Process object that will be scheduled to run with different scheduling policies.
 */
public class Process implements Comparable<Process> {
  /**
   * Describes the possible states of a process.
   */
  public enum State {
    unstarted, ready, blocked, running, terminated; 
  }
  
  private int rank;           // Rank of process after sorting
  private int A;              // Arrival time
  private int B;              // CPU burst modulo number
  private int C;              // Total CPU time needed
  private int M;              // IO time of burst = M * B
  private int cpuBurstTime;   // Current cpu burst cycle
  private int ioBurstTime;    // Current io burst cycle
  private int finishingTime;  // Cycle process terminated
  private int totalIOTime;    // Total time spent in IO
  private int waitingTime;    // Total time spent in ready
  private State state;        // State of process
  
  public Process(int A, int B, int C, int M) {
    this.A = A;
    this.B = B;
    this.C = C;
    this.M = M;
    this.state = State.unstarted;
  }
  
  /**
   * @return the rank
   */
  public int getRank() {
    return rank;
  }

  /**
   * @param rank the rank to set
   */
  public void setRank(int rank) {
    this.rank = rank;
  }

  /**
   * @return the a
   */
  public int getA() {
    return A;
  }

  /**
   * @param a the a to set
   */
  public void setA(int a) {
    A = a;
  }

  /**
   * @return the b
   */
  public int getB() {
    return B;
  }

  /**
   * @param b the b to set
   */
  public void setB(int b) {
    B = b;
  }

  /**
   * @return the c
   */
  public int getC() {
    return C;
  }

  /**
   * @param c the c to set
   */
  public void setC(int c) {
    C = c;
  }
  
  /**
   * @param c the c to decrement
   */
  public void decrementC() {
    C--;
  }

  /**
   * @return the m
   */
  public int getM() {
    return M;
  }

  /**
   * @param m the m to set
   */
  public void setM(int m) {
    M = m;
  }

  /**
   * @return the cpuBurstTime
   */
  public int getCpuBurstTime() {
    return cpuBurstTime;
  }

  /**
   * @param cpuBurstTime the cpuBurstTime to set
   */
  public void setCpuBurstTime(int cpuBurstTime) {
    this.cpuBurstTime = cpuBurstTime;
  }
  
  /**
   * @param cpuBurstTime the cpuBurstTime to decrement
   */
  public void decrementCpuBurstTime() {
    cpuBurstTime--;
  }

  /**
   * @return the ioBurstTime
   */
  public int getIoBurstTime() {
    return ioBurstTime;
  }

  /**
   * @param ioBurstTime the ioBurstTime to set
   */
  public void setIoBurstTime(int ioBurstTime) {
    this.ioBurstTime = ioBurstTime;
  }
  
  /**
   * @param ioBurstTime the ioBurstTime to decrement
   */
  public void decrementIoBurstTime() {
    ioBurstTime--;
  }

  /**
   * @return the finishingTime
   */
  public int getFinishingTime() {
    return finishingTime;
  }

  /**
   * @param finishingTime the finishingTime to set
   */
  public void setFinishingTime(int finishingTime) {
    this.finishingTime = finishingTime;
  }

  /**
   * @return the totalIOTime
   */
  public int getTotalIOTime() {
    return totalIOTime;
  }

  /**
   * @param totalIOTime the totalIOTime to set
   */
  public void incrementTotalIOTime() {
    totalIOTime++;
  }

  /**
   * @return the waitingTime
   */
  public int getWaitingTime() {
    return waitingTime;
  }

  /**
   * @param waitingTime the waitingTime to increment
   */
  public void incrementWaitingTime() {
    waitingTime++;
  }

  /**
   * @return the state
   */
  public State getState() {
    return state;
  }

  /**
   * @param state the state to set
   */
  public void setState(State state) {
    this.state = state;
  }

  /**
   * @return pretty string
   */
  public String toPrettyString() {
    return "(" + A + "," + B + "," + C + "," + M + ")";
  }
  
  @Override
  public String toString() {
    return "Process [rank=" + rank + ", A=" + A + ", B=" + B + ", C=" + C
        + ", M=" + M + ", cpuBurstTime=" + cpuBurstTime + ", ioBurstTime="
        + ioBurstTime + ", finishingTime=" + finishingTime + ", totalIOTime="
        + totalIOTime + ", state=" + state + "]";
  }

  @Override
  public int compareTo(Process o) {
    return A - o.A;
  }
}

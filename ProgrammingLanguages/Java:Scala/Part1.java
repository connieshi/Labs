import java.util.ArrayList;
import java.util.List;

/**
 * @author Connie Shi
 */
public class Part1 {
  public static void main(String[]args) {
    test();
  }

  /**
   * Add element z to sorted list L
   * @param L
   * @param z
   */
  static <T> void addToSortedList(SortedList<T> L, T z) {
    for (int i = 0; i < L.size(); i++) {
      if (((A) L.get(i)).compareTo((A) z) > 0) {
        L.add(i, z);
        return;
      }
    }
    L.add(L.size(), z);
  }

  static void test() {
    SortedList<A> c1 = new SortedList<A>();
    SortedList<A> c2 = new SortedList<A>();
    for (int i = 35; i >= 0; i-=5) {
      addToSortedList(c1, new A(i,i+1));
      addToSortedList(c2, new B(i+2,i+3,i+4));
    }

    System.out.print("c1: ");
    System.out.println(c1);

    System.out.print("c2: ");
    System.out.println(c2);

    switch (c1.compareTo(c2)) {
      case -1: 
        System.out.println("c1 < c2");
        break;
      case 0:
        System.out.println("c1 = c2");
        break;
      case 1:
        System.out.println("c1 > c2");
        break;
      default:
        System.out.println("Uh Oh");
        break;
    }

    Sorted<A> res = c1.merge(c2);
    System.out.print("Result: ");
    System.out.println(res);
  }
}

/**
 * Interface for sorted list
 * @param <E>
 */
interface Sorted<E> extends List<E> {
  public Sorted<E> merge(Sorted<E> unsorted);
}

/**
 * A generic sorted list
 * @param <E>
 */
@SuppressWarnings("serial")
class SortedList<E> extends ArrayList<E> implements Sorted<E>, Comparable<SortedList<E>> {

  @Override
  public int compareTo(SortedList<E> o) {
    if (this.size() == o.size()) {
      for (int i = 0; i < this.size(); i++) {
        if (((A) this.get(0)).compareTo((A) o.get(0)) != 0) {
          return ((A) this.get(0)).compareTo((A) o.get(0));
        }
      }
      return 0;
    } else if (this.size() > o.size()) {
      for (int i = 0; i < o.size(); i++) {
        if (((A) this.get(0)).compareTo((A) o.get(0)) != 0) {
          return ((A) this.get(0)).compareTo((A) o.get(0));
        }
      }
      return 1;
    } else {
      for (int i = 0; i < this.size(); i++) {
        if (((A) this.get(0)).compareTo((A) o.get(0)) != 0) {
          return ((A) this.get(0)).compareTo((A) o.get(0));
        }
      }
      return -1;
    }
  }

  @Override
  public Sorted<E> merge(Sorted<E> list) {
    Sorted<E> longer = (this.size() > list.size()) ? this : list;
    Sorted<E> shorter = (this.size() > list.size()) ? list : this;
    Sorted<E> mergedList = new SortedList<E>();
    int index1 = 0;
    int index2 = 0; 

    while (index1 < longer.size() && index2 < shorter.size()) {
      E smaller = (((A) longer.get(index1)).compareTo(((A) shorter.get(index2))) < 0)
          ? longer.get(index1++) : shorter.get(index2++);
          mergedList.add(smaller);
    }

    while (index1 < longer.size()) {
      mergedList.add(longer.get(index1++));
    }

    while (index2 < shorter.size()) {
      mergedList.add(shorter.get(index2++));
    }

    return mergedList;
  }

  @Override
  public boolean add(E e) {
    for (int i = 0; i < this.size(); i++) {
      if (((A) this.get(i)).compareTo((A) e) > 0) {
        this.add(i, e);
        return true;
      }
    }
    this.add(this.size(), e);
    return true;
  }
}

/**
 * Object A with x and ys
 */
class A implements Comparable<A> {
  Integer x;
  Integer y;

  public A(Integer x, Integer y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public int compareTo(A o) {
    int thisSum = this.x + this.y;
    int otherSum = o.x + o.y;

    if (o instanceof B) {
      otherSum += ((B) o).z;
    }

    if (thisSum == otherSum) {
      return 0;
    } else if (thisSum > otherSum) {
      return 1;
    } else {
      return -1;
    }
  }

  @Override
  public String toString() {
    return "A<" + x + "," + y + ">";
  }
}

/**
 * Object B extends A with x, y, and z
 */
class B extends A {
  Integer x, y, z;

  public B(Integer x, Integer y, Integer z) {
    super(x, y);
    this.x = x;
    this.y = y;
    this.z = z;
  }

  @Override
  public int compareTo(A o) {
    int thisSum = this.x + this.y + this.z;
    int otherSum = o.x + o.y;

    if (o instanceof B) {
      otherSum += ((B) o).z;
    }

    if (thisSum == otherSum) {
      return 0;
    } else if (thisSum > otherSum) {
      return 1;
    } else {
      return -1;
    }
  }

  @Override
  public String toString() {
    return "B<" + x + "," + y + "," + z + ">";
  }
}

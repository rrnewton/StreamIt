import streamit.library.*;
class Complex extends Structure {
  public float real;
  public float imag;
}
public class MergeSort extends StreamIt
{
  public static void main(String[] args) {
    MergeSort program = new MergeSort();
    program.run(args);
  }
  public void init() {
    int NUM_INPUTS = 64;
    int MULT = 4;
    add(new SortInput((NUM_INPUTS / MULT)));
    add(new Sorter(NUM_INPUTS));
    add(new IntPrinter());
  }
}
class Merger extends Filter
{
  int N;
  public void work() {
    int index1 = 0;
    int index2 = 1;
    while (((index1 < N) && (index2 < N))) {
      int val1 = input.peekInt(index1);
      int val2 = input.peekInt(index2);
      if ((val1 <= val2)) {
        output.pushInt(val1);
        index1 += 2;
      } else {
        output.pushInt(val2);
        index2 += 2;
      };
    };
    int leftover = ((index1 < N) ? index1 : index2);
    for (int i = leftover; (i < N); i += 2) {
      output.pushInt(input.peekInt(i));
    };
    for (int i = 0; (i < N); i++) {
      input.popInt();
    };
  }
  public Merger(int N) {
    super(N);
  }
  public void init(final int N) {
    this.N = N;
    input = new Channel(Integer.TYPE, N);
    output = new Channel(Integer.TYPE, N);
  }
}
class Sorter extends Pipeline
{
  public Sorter(int N) {
    super(N);
  }
  public void init(final int N) {
    if ((N > 2)) {
      add(new SplitJoin() {
              public void init() {
          setSplitter(ROUND_ROBIN(1));
          add(new Sorter((N / 2)));
          add(new Sorter((N / 2)));
          setJoiner(ROUND_ROBIN(1));
        }
}
);
    };
    add(new Merger(N));
  }
}
class SortInput extends Filter
{
  int N;
  public void work() {
    for (int i = 0; (i < N); i++) {
      output.pushInt((N - i));
    };
  }
  public SortInput(int N) {
    super(N);
  }
  public void init(final int N) {
    this.N = N;
    output = new Channel(Integer.TYPE, N);
  }
}
class IntPrinter extends Filter
{
  public void work() {
    System.out.println(input.popInt());
  }
  public void init() {
    input = new Channel(Integer.TYPE, 1);
  }
}

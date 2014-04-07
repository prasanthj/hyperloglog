package hyperloglog;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class Main {

  final static int COUNT = 100;
  final static int SEED = 100;

  public static void main(String[] args) {
    Random rand = new Random(SEED);
    long[] iterations = new long[6];
    for (int i = 0; i < iterations.length; i++) {
      iterations[i] = (long) (100 * Math.pow(10, i));
    }

    LinkedHashMap<Long, HLLResult> hllResults = new LinkedHashMap<Long, HLLResult>();
    for (int iter = 0; iter < iterations.length; iter++) {
      long start = System.currentTimeMillis();
      HyperLogLog hllo = new HyperLogLog();
      for (int i = 0; i < iterations[iter]; i++) {
        hllo.addLong(rand.nextLong());
      }
      long estCount = hllo.count();
      long end = System.currentTimeMillis();
      long time = end - start;
      HLLResult result = new HLLResult("HLL-Original", iterations[iter], estCount, time);
      hllResults.put(iterations[iter], result);
    }

    for (Map.Entry<Long, HLLResult> entry : hllResults.entrySet()) {
      System.out.println(entry.getValue());
    }
  }
}

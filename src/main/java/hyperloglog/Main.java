package hyperloglog;

import java.util.HashSet;
import java.util.Random;

public class Main {

  final static int COUNT = 100;
  final static int SEED = 100;

  public static void main(String[] args) {
    Random rand = new Random(SEED);
    long[] iterations = new long[5];
    for (int i = 0; i < iterations.length; i++) {
      iterations[i] = (long) (100 * Math.pow(10, i));
    }
    
    HashSet<Long> hashset = new HashSet<Long>();
    for (int iter = 0; iter < iterations.length; iter++) {
      HyperLogLogOriginal hllo = new HyperLogLogOriginal(16, 64);
      for (int i = 0; i < iterations[iter]; i++) {
        long num = rand.nextInt((int) iterations[iter]);
        hashset.add(num);
        hllo.addLong(num);
      }
      
      int actualCount = hashset.size();
      int hllCount = (int) hllo.count();
      System.out.println("Actual count : " + actualCount);
      float stderr = (1.0f - ((float) hllCount / (float) actualCount)) * 100.0f;
      System.out.println("HLL Original estimated count : " + hllo.count() + " with standard error: "
          + stderr + "%\n");
    }
  }
}

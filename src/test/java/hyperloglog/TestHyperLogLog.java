package hyperloglog;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

public class TestHyperLogLog {

  // 5% tolerance for estimated count
  private float tolerance = 5.0f;

  @Test
  public void testHLLAdd100() {
    Random rand = new Random();
    HyperLogLog hll = new HyperLogLog();
    int size = 100;
    for (int i = 0; i < size; i++) {
      hll.addLong(rand.nextLong());
    }
    double delta = tolerance * size / 100;
    HLLResult result = new HLLResult(size, hll.count());
    System.out.println("Relative error for " + size + " values: " + result);
    assertEquals((double) size, (double) hll.count(), delta);
  }

  @Test
  public void testHLLAdd1000() {
    Random rand = new Random();
    HyperLogLog hll = new HyperLogLog();
    int size = 1000;
    for (int i = 0; i < size; i++) {
      hll.addLong(rand.nextLong());
    }
    double delta = tolerance * size / 100;
    HLLResult result = new HLLResult(size, hll.count());
    System.out.println("Relative error for " + size + " values: " + result);
    assertEquals((double) size, (double) hll.count(), delta);
  }

  @Test
  public void testHLLAdd10000() {
    Random rand = new Random();
    HyperLogLog hll = new HyperLogLog();
    int size = 10000;
    for (int i = 0; i < size; i++) {
      hll.addLong(rand.nextLong());
    }
    double delta = tolerance * size / 100;
    HLLResult result = new HLLResult(size, hll.count());
    System.out.println("Relative error for " + size + " values: " + result);
    assertEquals((double) size, (double) hll.count(), delta);
  }

  @Test
  public void testHLLAdd100000() {
    Random rand = new Random();
    HyperLogLog hll = new HyperLogLog();
    int size = 100000;
    for (int i = 0; i < size; i++) {
      hll.addLong(rand.nextLong());
    }
    double delta = tolerance * size / 100;
    HLLResult result = new HLLResult(size, hll.count());
    System.out.println("Relative error for " + size + " values: " + result);
    assertEquals((double) size, (double) hll.count(), delta);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHLLMerge() {
    Random rand = new Random();
    HyperLogLog hll = new HyperLogLog();
    HyperLogLog hll2 = new HyperLogLog();
    HyperLogLog hll3 = new HyperLogLog();
    HyperLogLog hll4 = new HyperLogLog(16, 64);
    int size = 10000;
    for (int i = 0; i < size; i++) {
      hll.addLong(rand.nextLong());
      hll2.addLong(rand.nextLong());
      hll3.addLong(rand.nextLong());
    }
    double delta = tolerance * size / 100;
    HLLResult result = new HLLResult(size, hll.count());
    HLLResult result2 = new HLLResult(size, hll2.count());
    System.out.println("Relative error for " + size + " values: " + result);
    System.out.println("Relative error for " + size + " values: " + result2);
    assertEquals((double) size, (double) hll.count(), delta);
    assertEquals((double) size, (double) hll2.count(), delta);

    // merge
    hll.merge(hll2);
    result = new HLLResult(2 * size, hll.count());
    System.out.println("Relative error after merge for " + (2 * size) + " values: " + result);
    assertEquals((double) 2 * size, (double) hll.count(), delta);

    // merge should update registers and hence the count
    hll.merge(hll2);
    result = new HLLResult(2 * size, hll.count());
    System.out.println("Relative error after merge for " + (2 * size) + " values: " + result);
    assertEquals((double) 2 * size, (double) hll.count(), delta);

    // new merge
    hll.merge(hll3);
    result = new HLLResult(3 * size, hll.count());
    System.out.println("Relative error after merge for " + (3 * size) + " values: " + result);
    assertEquals((double) 3 * size, (double) hll.count(), delta);

    // invalid merge -- register set size doesn't match
    hll.merge(hll4);
  }

}

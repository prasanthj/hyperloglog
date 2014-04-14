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
    assertEquals((double) size, (double) hll.count(), delta);
  }

  @Test
  public void testHLLAdd1000000() {
    Random rand = new Random();
    HyperLogLog hll = new HyperLogLog();
    int size = 1000000;
    for (int i = 0; i < size; i++) {
      hll.addLong(rand.nextLong());
    }
    double delta = tolerance * size / 100;
    assertEquals((double) size, (double) hll.count(), delta);
  }

  @Test
  public void testHLLAdd20() {
    Random rand = new Random();
    HyperLogLog hll = new HyperLogLog();
    int size = 100000;
    int unique = 20;
    for (int i = 0; i < size; i++) {
      hll.addLong(rand.nextInt(unique));
    }
    double delta = tolerance * unique / 100;
    assertEquals((double) unique, (double) hll.count(), delta);
  }

  @Test
  public void testHLLAdd2000() {
    Random rand = new Random();
    HyperLogLog hll = new HyperLogLog();
    int size = 100000;
    int unique = 2000;
    for (int i = 0; i < size; i++) {
      hll.addLong(rand.nextInt(unique));
    }
    double delta = tolerance * unique / 100;
    assertEquals((double) unique, (double) hll.count(), delta);
  }

  @Test
  public void testHLLAdd20000() {
    Random rand = new Random();
    HyperLogLog hll = new HyperLogLog();
    int size = 100000;
    int unique = 20000;
    for (int i = 0; i < size; i++) {
      hll.addLong(rand.nextInt(unique));
    }
    double delta = tolerance * unique / 100;
    assertEquals((double) unique, (double) hll.count(), delta);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHLLMerge() {
    HyperLogLog hll = new HyperLogLog();
    HyperLogLog hll2 = new HyperLogLog();
    HyperLogLog hll3 = new HyperLogLog();
    HyperLogLog hll4 = new HyperLogLog(16, 64);
    int size = 10000;
    for (int i = 0; i < size; i++) {
      hll.addLong(i);
      hll2.addLong(10000 + i);
      hll3.addLong(30000 + i);
    }
    double delta = tolerance * size / 100;
    assertEquals((double) size, (double) hll.count(), delta);
    assertEquals((double) size, (double) hll2.count(), delta);

    // merge
    hll.merge(hll2);
    assertEquals((double) 2 * size, (double) hll.count(), delta);

    // merge should update registers and hence the count
    hll.merge(hll2);
    assertEquals((double) 2 * size, (double) hll.count(), delta);

    // new merge
    hll.merge(hll3);
    assertEquals((double) 3 * size, (double) hll.count(), delta);

    // invalid merge -- register set size doesn't match
    hll.merge(hll4);
  }

}

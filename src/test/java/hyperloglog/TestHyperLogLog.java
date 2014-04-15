package hyperloglog;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class TestHyperLogLog {

  // 5% tolerance for estimated count
  private float longRangeTolerance = 5.0f;
  private float shortRangeTolerance = 3.0f;

  private int size;

  public TestHyperLogLog(int n) {
    this.size = n;
  }

  @Parameters
  public static Collection<Object[]> data() {
    Object[][] data = new Object[][] { { 2 }, { 10 }, { 100 }, { 1000 }, { 10000 }, { 100000 },
        { 1000000 } };
    return Arrays.asList(data);
  }

  @Test
  public void testHLLAdd() {
    Random rand = new Random(size);
    HyperLogLog hll = new HyperLogLog();
    int size = 100;
    for (int i = 0; i < size; i++) {
      hll.addLong(rand.nextLong());
    }
    double threshold = size > 40000 ? longRangeTolerance : shortRangeTolerance;
    double delta = threshold * size / 100;
    assertEquals((double) size, (double) hll.count(), delta);
  }

  @Test
  public void testHLLAddHalfDistinct() {
    Random rand = new Random(size);
    HyperLogLog hll = new HyperLogLog();
    int unique = size/2;
    Set<Long> hashset = new HashSet<Long>();
    for (int i = 0; i < size; i++) {
      long val = rand.nextInt(unique);
      hashset.add(val);
      hll.addLong(val);
    }
    double threshold = size > 40000 ? longRangeTolerance : shortRangeTolerance;
    double delta = threshold * hashset.size() / 100;
    assertEquals((double) hashset.size(), (double) hll.count(), delta);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHLLMerge() {
    HyperLogLog hll = new HyperLogLog();
    HyperLogLog hll2 = new HyperLogLog();
    HyperLogLog hll3 = new HyperLogLog();
    HyperLogLog hll4 = new HyperLogLog(16, 64);
    for (int i = 0; i < size; i++) {
      hll.addLong(i);
      hll2.addLong(size + i);
      hll3.addLong(2 * size + i);
    }
    double threshold = size > 40000 ? longRangeTolerance : shortRangeTolerance;
    double delta = threshold * size / 100;
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

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
public class TestHLLNoBias {

  // 1% tolerance for long range bias and 0.5% for short range bias
  private float noBiaslongRangeTolerance = 1.0f;
  private float biasedlongRangeTolerance = 5.0f;
  private float shortRangeTolerance = 0.5f;

  private int size;

  public TestHLLNoBias(int n) {
    this.size = n;
  }

  @Parameters
  public static Collection<Object[]> data() {
    Object[][] data = new Object[][] { { 30000 }, { 41000 }, { 50000 }, { 60000 }, { 75000 },
        { 80000 }, { 81920 } };
    return Arrays.asList(data);
  }

  @Test
  public void testHLLAdd() {
    Random rand = new Random(size);
    HyperLogLog hll = HyperLogLog.builder().build();
    int size = 100;
    for (int i = 0; i < size; i++) {
      hll.addLong(rand.nextLong());
    }
    double threshold = size > 40000 ? noBiaslongRangeTolerance : shortRangeTolerance;
    double delta = threshold * size / 100;
    assertEquals((double) size, (double) hll.count(), delta);
  }

  @Test
  public void testHLLAddHalfDistinct() {
    Random rand = new Random(size);
    HyperLogLog hll = HyperLogLog.builder().build();
    int unique = size / 2;
    Set<Long> hashset = new HashSet<Long>();
    for (int i = 0; i < size; i++) {
      long val = rand.nextInt(unique);
      hashset.add(val);
      hll.addLong(val);
    }
    double threshold = size > 40000 ? noBiaslongRangeTolerance : shortRangeTolerance;
    double delta = threshold * hashset.size() / 100;
    assertEquals((double) hashset.size(), (double) hll.count(), delta);
  }

  @Test
  public void testHLLNoBiasDisabled() {
    Random rand = new Random(size);
    HyperLogLog hll = HyperLogLog.builder().enableNoBias(false).build();
    int size = 100;
    for (int i = 0; i < size; i++) {
      hll.addLong(rand.nextLong());
    }
    double threshold = size > 40000 ? biasedlongRangeTolerance : shortRangeTolerance;
    double delta = threshold * size / 100;
    assertEquals((double) size, (double) hll.count(), delta);
  }

  @Test
  public void testHLLNoBiasDisabledHalfDistinct() {
    Random rand = new Random(size);
    HyperLogLog hll = HyperLogLog.builder().enableNoBias(false).build();
    int unique = size / 2;
    Set<Long> hashset = new HashSet<Long>();
    for (int i = 0; i < size; i++) {
      long val = rand.nextInt(unique);
      hashset.add(val);
      hll.addLong(val);
    }
    double threshold = size > 40000 ? biasedlongRangeTolerance : shortRangeTolerance;
    double delta = threshold * hashset.size() / 100;
    assertEquals((double) hashset.size(), (double) hll.count(), delta);
  }

}

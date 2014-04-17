package hyperloglog;

import static org.junit.Assert.assertEquals;
import hyperloglog.HyperLogLog.EncodingType;

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
public class TestHyperLogLogDense {

  // 5% tolerance for long range bias and 1% for short range bias
  private float longRangeTolerance = 5.0f;
  private float shortRangeTolerance = 1.0f;

  private int size;

  public TestHyperLogLogDense(int n) {
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
    HyperLogLog hll = HyperLogLog.builder().setEncoding(EncodingType.DENSE).build();
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
    HyperLogLog hll = HyperLogLog.builder().setEncoding(EncodingType.DENSE).build();
    int unique = size / 2;
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

}

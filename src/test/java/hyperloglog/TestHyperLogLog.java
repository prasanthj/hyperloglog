package hyperloglog;

import static org.junit.Assert.assertEquals;
import hyperloglog.HyperLogLog.EncodingType;

import org.junit.Test;

public class TestHyperLogLog {
  // 5% tolerance for estimated count
  private float longRangeTolerance = 5.0f;
  private float shortRangeTolerance = 3.0f;

  @Test(expected = IllegalArgumentException.class)
  public void testHLLDenseMerge() {
    HyperLogLog hll = HyperLogLog.builder().setEncoding(EncodingType.DENSE).build();
    HyperLogLog hll2 = HyperLogLog.builder().setEncoding(EncodingType.DENSE).build();
    HyperLogLog hll3 = HyperLogLog.builder().setEncoding(EncodingType.DENSE).build();
    HyperLogLog hll4 = HyperLogLog.builder().setNumRegisterIndexBits(16).setNumHashBits(64)
        .setEncoding(EncodingType.DENSE).build();
    int size = 1000;
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
    assertEquals(EncodingType.DENSE, hll.getEncoding());

    // merge should update registers and hence the count
    hll.merge(hll2);
    assertEquals((double) 2 * size, (double) hll.count(), delta);
    assertEquals(EncodingType.DENSE, hll.getEncoding());

    // new merge
    hll.merge(hll3);
    assertEquals((double) 3 * size, (double) hll.count(), delta);
    assertEquals(EncodingType.DENSE, hll.getEncoding());

    // invalid merge -- register set size doesn't match
    hll.merge(hll4);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHLLSparseMerge() {
    HyperLogLog hll = HyperLogLog.builder().setEncoding(EncodingType.SPARSE).build();
    HyperLogLog hll2 = HyperLogLog.builder().setEncoding(EncodingType.SPARSE).build();
    HyperLogLog hll3 = HyperLogLog.builder().setEncoding(EncodingType.SPARSE).build();
    HyperLogLog hll4 = HyperLogLog.builder().setNumRegisterIndexBits(16).setNumHashBits(64)
        .setEncoding(EncodingType.SPARSE).build();
    int size = 500;
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
    assertEquals(EncodingType.SPARSE, hll.getEncoding());

    // merge should update registers and hence the count
    hll.merge(hll2);
    assertEquals((double) 2 * size, (double) hll.count(), delta);
    assertEquals(EncodingType.SPARSE, hll.getEncoding());

    // new merge
    hll.merge(hll3);
    assertEquals((double) 3 * size, (double) hll.count(), delta);
    assertEquals(EncodingType.SPARSE, hll.getEncoding());

    // invalid merge -- register set size doesn't match
    hll.merge(hll4);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHLLSparseDenseMerge() {
    HyperLogLog hll = HyperLogLog.builder().setEncoding(EncodingType.SPARSE).build();
    HyperLogLog hll2 = HyperLogLog.builder().setEncoding(EncodingType.SPARSE).build();
    HyperLogLog hll3 = HyperLogLog.builder().setEncoding(EncodingType.DENSE).build();
    HyperLogLog hll4 = HyperLogLog.builder().setNumRegisterIndexBits(16).setNumHashBits(64)
        .setEncoding(EncodingType.DENSE).build();
    int size = 1000;
    for (int i = 0; i < size; i++) {
      hll.addLong(i);
      hll2.addLong(size + i);
      hll3.addLong(2 * size + i);
    }
    double threshold = size > 40000 ? longRangeTolerance : shortRangeTolerance;
    double delta = threshold * size / 100;
    assertEquals((double) size, (double) hll.count(), delta);
    assertEquals((double) size, (double) hll2.count(), delta);

    // sparse-sparse merge
    hll.merge(hll2);
    assertEquals((double) 2 * size, (double) hll.count(), delta);
    assertEquals(EncodingType.SPARSE, hll.getEncoding());

    // merge should update registers and hence the count
    hll.merge(hll2);
    assertEquals((double) 2 * size, (double) hll.count(), delta);
    assertEquals(EncodingType.SPARSE, hll.getEncoding());

    // sparse-dense merge
    hll.merge(hll3);
    assertEquals((double) 3 * size, (double) hll.count(), delta);
    assertEquals(EncodingType.DENSE, hll.getEncoding());

    // invalid merge -- register set size doesn't match
    hll.merge(hll4);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHLLDenseSparseMerge() {
    HyperLogLog hll = HyperLogLog.builder().setEncoding(EncodingType.DENSE).build();
    HyperLogLog hll2 = HyperLogLog.builder().setEncoding(EncodingType.DENSE).build();
    HyperLogLog hll3 = HyperLogLog.builder().setEncoding(EncodingType.SPARSE).build();
    HyperLogLog hll4 = HyperLogLog.builder().setNumRegisterIndexBits(16).setNumHashBits(64)
        .setEncoding(EncodingType.SPARSE).build();
    int size = 1000;
    for (int i = 0; i < size; i++) {
      hll.addLong(i);
      hll2.addLong(size + i);
      hll3.addLong(2 * size + i);
    }
    double threshold = size > 40000 ? longRangeTolerance : shortRangeTolerance;
    double delta = threshold * size / 100;
    assertEquals((double) size, (double) hll.count(), delta);
    assertEquals((double) size, (double) hll2.count(), delta);

    // sparse-sparse merge
    hll.merge(hll2);
    assertEquals((double) 2 * size, (double) hll.count(), delta);
    assertEquals(EncodingType.DENSE, hll.getEncoding());

    // merge should update registers and hence the count
    hll.merge(hll2);
    assertEquals((double) 2 * size, (double) hll.count(), delta);
    assertEquals(EncodingType.DENSE, hll.getEncoding());

    // sparse-dense merge
    hll.merge(hll3);
    assertEquals((double) 3 * size, (double) hll.count(), delta);
    assertEquals(EncodingType.DENSE, hll.getEncoding());

    // invalid merge -- register set size doesn't match
    hll.merge(hll4);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHLLSparseOverflowMerge() {
    HyperLogLog hll = HyperLogLog.builder().setEncoding(EncodingType.SPARSE).build();
    HyperLogLog hll2 = HyperLogLog.builder().setEncoding(EncodingType.SPARSE).build();
    HyperLogLog hll3 = HyperLogLog.builder().setEncoding(EncodingType.SPARSE).build();
    HyperLogLog hll4 = HyperLogLog.builder().setNumRegisterIndexBits(16).setNumHashBits(64)
        .setEncoding(EncodingType.SPARSE).build();
    int size = 1000;
    for (int i = 0; i < size; i++) {
      hll.addLong(i);
      hll2.addLong(size + i);
      hll3.addLong(2 * size + i);
    }
    double threshold = size > 40000 ? longRangeTolerance : shortRangeTolerance;
    double delta = threshold * size / 100;
    assertEquals((double) size, (double) hll.count(), delta);
    assertEquals((double) size, (double) hll2.count(), delta);

    // sparse-sparse merge
    hll.merge(hll2);
    assertEquals((double) 2 * size, (double) hll.count(), delta);
    assertEquals(EncodingType.SPARSE, hll.getEncoding());

    // merge should update registers and hence the count
    hll.merge(hll2);
    assertEquals((double) 2 * size, (double) hll.count(), delta);
    assertEquals(EncodingType.SPARSE, hll.getEncoding());

    // sparse-sparse overload to dense
    hll.merge(hll3);
    assertEquals((double) 3 * size, (double) hll.count(), delta);
    assertEquals(EncodingType.DENSE, hll.getEncoding());

    // invalid merge -- register set size doesn't match
    hll.merge(hll4);
  }

  @Test
  public void testHLLSparseMoreRegisterBits() {
    HyperLogLog hll = HyperLogLog.builder().setEncoding(EncodingType.SPARSE).setNumRegisterIndexBits(16).build();
    int size = 1000;
    for (int i = 0; i < size; i++) {
      hll.addLong(i);
    }
    double threshold = size > 40000 ? longRangeTolerance : shortRangeTolerance;
    double delta = threshold * size / 100;
    assertEquals((double) size, (double) hll.count(), delta);
  }
}

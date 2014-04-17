package hyperloglog;

import static org.junit.Assert.assertEquals;
import hyperloglog.HyperLogLog.EncodingType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class TestHLLSerialization {

  private int size;
  private File testFile;
  private static final String pathPrefix = "src/test/resources/";
  private static final int SEED = 100;
  // 5% tolerance for long range bias and 1% for short range bias
  private float longRangeTolerance = 5.0f;
  private float shortRangeTolerance = 1.0f;

  public TestHLLSerialization(int n) {
    this.size = n;
    this.testFile = new File(pathPrefix + testCaseName.getMethodName() + "_" + size + ".hll");
  }

  @Parameters
  public static Collection<Object[]> data() {
    Object[][] data = new Object[][] { { 2 }, { 10 }, { 100 }, { 1000 }, { 2000 }, { 3000 },
        { 5000 }, { 6000 }, { 10000 }, { 100000 }, { 1000000 } };
    return Arrays.asList(data);
  }

  @After
  public void close() {
    if (testFile.exists()) {
      testFile.delete();
    }
  }

  @Rule
  public TestName testCaseName = new TestName();

  @Test
  public void testHLLSparseSerialization() throws IOException {
    HyperLogLog hll = HyperLogLog.builder().setEncoding(EncodingType.SPARSE).build();
    Random rand = new Random(SEED);
    for (int i = 0; i < size; i++) {
      hll.addLong(rand.nextLong());
    }
    FileOutputStream fos = new FileOutputStream(testFile);
    DataOutputStream out = new DataOutputStream(fos);
    HyperLogLogUtils.serializeHLL(out, hll);
    System.out.println(testCaseName.getMethodName() + "_" + size + " - serializedSize: "
        + out.size() + " bytes");

    FileInputStream fis = new FileInputStream(testFile);
    DataInputStream in = new DataInputStream(fis);
    HyperLogLog deserializedHLL = HyperLogLogUtils.deserializeHLL(in);
    assertEquals(hll, deserializedHLL);
    assertEquals(hll.toString(), deserializedHLL.toString());
    assertEquals(hll.toStringExtended(), deserializedHLL.toStringExtended());
    assertEquals(hll.hashCode(), deserializedHLL.hashCode());
    assertEquals(hll.count(), deserializedHLL.count());
  }

  @Test
  public void testHLLSparseSerializationHalfDistinct() throws IOException {
    HyperLogLog hll = HyperLogLog.builder().setEncoding(EncodingType.SPARSE).build();
    Random rand = new Random(SEED);
    Set<Integer> hashset = new HashSet<Integer>();
    for (int i = 0; i < size; i++) {
      int val = rand.nextInt(size / 2);
      hll.addLong(val);
      hashset.add(val);
    }
    FileOutputStream fos = new FileOutputStream(testFile);
    DataOutputStream out = new DataOutputStream(fos);
    HyperLogLogUtils.serializeHLL(out, hll);
    System.out.println(testCaseName.getMethodName() + "_" + size + " - serializedSize: "
        + out.size() + " bytes");

    double threshold = size > 40000 ? longRangeTolerance : shortRangeTolerance;
    double delta = threshold * hashset.size() / 100;
    FileInputStream fis = new FileInputStream(testFile);
    DataInputStream in = new DataInputStream(fis);
    HyperLogLog deserializedHLL = HyperLogLogUtils.deserializeHLL(in);
    assertEquals(hll, deserializedHLL);
    assertEquals(hll.toString(), deserializedHLL.toString());
    assertEquals(hll.toStringExtended(), deserializedHLL.toStringExtended());
    assertEquals(hll.hashCode(), deserializedHLL.hashCode());
    assertEquals(hll.count(), deserializedHLL.count());
    assertEquals(hashset.size(), hll.count(), delta);
    assertEquals(hashset.size(), deserializedHLL.count(), delta);
  }

  @Test
  public void testHLLSparseNoBitPacking() throws IOException {
    HyperLogLog hll = HyperLogLog.builder().setEncoding(EncodingType.SPARSE)
        .enableBitPacking(false).build();
    Random rand = new Random(SEED);
    for (int i = 0; i < size; i++) {
      hll.addLong(rand.nextLong());
    }
    FileOutputStream fos = new FileOutputStream(testFile);
    DataOutputStream out = new DataOutputStream(fos);
    HyperLogLogUtils.serializeHLL(out, hll);
    System.out.println(testCaseName.getMethodName() + "_" + size + " - serializedSize: "
        + out.size() + " bytes");

    FileInputStream fis = new FileInputStream(testFile);
    DataInputStream in = new DataInputStream(fis);
    HyperLogLog deserializedHLL = HyperLogLogUtils.deserializeHLL(in);
    assertEquals(hll, deserializedHLL);
    assertEquals(hll.toString(), deserializedHLL.toString());
    assertEquals(hll.toStringExtended(), deserializedHLL.toStringExtended());
    assertEquals(hll.hashCode(), deserializedHLL.hashCode());
    assertEquals(hll.count(), deserializedHLL.count());
  }

  @Test
  public void testHLLSparseNoBitPackingHalfDistinct() throws IOException {
    HyperLogLog hll = HyperLogLog.builder().setEncoding(EncodingType.SPARSE)
        .enableBitPacking(false).build();
    Random rand = new Random(SEED);
    Set<Integer> hashset = new HashSet<Integer>();
    for (int i = 0; i < size; i++) {
      int val = rand.nextInt(size / 2);
      hll.addLong(val);
      hashset.add(val);
    }
    FileOutputStream fos = new FileOutputStream(testFile);
    DataOutputStream out = new DataOutputStream(fos);
    HyperLogLogUtils.serializeHLL(out, hll);
    System.out.println(testCaseName.getMethodName() + "_" + size + " - serializedSize: "
        + out.size() + " bytes");

    double threshold = size > 40000 ? longRangeTolerance : shortRangeTolerance;
    double delta = threshold * hashset.size() / 100;
    FileInputStream fis = new FileInputStream(testFile);
    DataInputStream in = new DataInputStream(fis);
    HyperLogLog deserializedHLL = HyperLogLogUtils.deserializeHLL(in);
    assertEquals(hll, deserializedHLL);
    assertEquals(hll.toString(), deserializedHLL.toString());
    assertEquals(hll.toStringExtended(), deserializedHLL.toStringExtended());
    assertEquals(hll.hashCode(), deserializedHLL.hashCode());
    assertEquals(hll.count(), deserializedHLL.count());
    assertEquals(hashset.size(), hll.count(), delta);
    assertEquals(hashset.size(), deserializedHLL.count(), delta);
  }

  @Test
  public void testHLLDenseSerialization() throws IOException {
    HyperLogLog hll = HyperLogLog.builder().setEncoding(EncodingType.DENSE).build();
    Random rand = new Random(SEED);
    for (int i = 0; i < size; i++) {
      hll.addLong(rand.nextLong());
    }
    FileOutputStream fos = new FileOutputStream(testFile);
    DataOutputStream out = new DataOutputStream(fos);
    HyperLogLogUtils.serializeHLL(out, hll);
    System.out.println(testCaseName.getMethodName() + "_" + size + " - serializedSize: "
        + out.size() + " bytes");

    FileInputStream fis = new FileInputStream(testFile);
    DataInputStream in = new DataInputStream(fis);
    HyperLogLog deserializedHLL = HyperLogLogUtils.deserializeHLL(in);
    assertEquals(hll, deserializedHLL);
    assertEquals(hll.toString(), deserializedHLL.toString());
    assertEquals(hll.toStringExtended(), deserializedHLL.toStringExtended());
    assertEquals(hll.hashCode(), deserializedHLL.hashCode());
    assertEquals(hll.count(), deserializedHLL.count());
  }

  @Test
  public void testHLLDenseSerializationHalfDistinct() throws IOException {
    HyperLogLog hll = HyperLogLog.builder().setEncoding(EncodingType.DENSE).build();
    Random rand = new Random(SEED);
    Set<Integer> hashset = new HashSet<Integer>();
    for (int i = 0; i < size; i++) {
      int val = rand.nextInt(size / 2);
      hll.addLong(val);
      hashset.add(val);
    }
    FileOutputStream fos = new FileOutputStream(testFile);
    DataOutputStream out = new DataOutputStream(fos);
    HyperLogLogUtils.serializeHLL(out, hll);
    System.out.println(testCaseName.getMethodName() + "_" + size + " - serializedSize: "
        + out.size() + " bytes");

    double threshold = size > 40000 ? longRangeTolerance : shortRangeTolerance;
    double delta = threshold * hashset.size() / 100;
    FileInputStream fis = new FileInputStream(testFile);
    DataInputStream in = new DataInputStream(fis);
    HyperLogLog deserializedHLL = HyperLogLogUtils.deserializeHLL(in);
    assertEquals(hll, deserializedHLL);
    assertEquals(hll.toString(), deserializedHLL.toString());
    assertEquals(hll.toStringExtended(), deserializedHLL.toStringExtended());
    assertEquals(hll.hashCode(), deserializedHLL.hashCode());
    assertEquals(hll.count(), deserializedHLL.count());
    assertEquals(hashset.size(), hll.count(), delta);
    assertEquals(hashset.size(), deserializedHLL.count(), delta);
  }

  @Test
  public void testHLLDenseNoBitPacking() throws IOException {
    HyperLogLog hll = HyperLogLog.builder().setEncoding(EncodingType.DENSE).enableBitPacking(false)
        .build();
    Random rand = new Random(SEED);
    for (int i = 0; i < size; i++) {
      hll.addLong(rand.nextLong());
    }
    FileOutputStream fos = new FileOutputStream(testFile);
    DataOutputStream out = new DataOutputStream(fos);
    HyperLogLogUtils.serializeHLL(out, hll);
    System.out.println(testCaseName.getMethodName() + "_" + size + " - serializedSize: "
        + out.size() + " bytes");

    FileInputStream fis = new FileInputStream(testFile);
    DataInputStream in = new DataInputStream(fis);
    HyperLogLog deserializedHLL = HyperLogLogUtils.deserializeHLL(in);
    assertEquals(hll, deserializedHLL);
    assertEquals(hll.toString(), deserializedHLL.toString());
    assertEquals(hll.toStringExtended(), deserializedHLL.toStringExtended());
    assertEquals(hll.hashCode(), deserializedHLL.hashCode());
    assertEquals(hll.count(), deserializedHLL.count());
  }

  @Test
  public void testHLLDenseNoBitPackingHalfDistinct() throws IOException {
    HyperLogLog hll = HyperLogLog.builder().setEncoding(EncodingType.DENSE).enableBitPacking(false)
        .build();
    Random rand = new Random(SEED);
    Set<Integer> hashset = new HashSet<Integer>();
    for (int i = 0; i < size; i++) {
      int val = rand.nextInt(size / 2);
      hll.addLong(val);
      hashset.add(val);
    }
    FileOutputStream fos = new FileOutputStream(testFile);
    DataOutputStream out = new DataOutputStream(fos);
    HyperLogLogUtils.serializeHLL(out, hll);
    System.out.println(testCaseName.getMethodName() + "_" + size + " - serializedSize: "
        + out.size() + " bytes");

    double threshold = size > 40000 ? longRangeTolerance : shortRangeTolerance;
    double delta = threshold * hashset.size() / 100;
    FileInputStream fis = new FileInputStream(testFile);
    DataInputStream in = new DataInputStream(fis);
    HyperLogLog deserializedHLL = HyperLogLogUtils.deserializeHLL(in);
    assertEquals(hll, deserializedHLL);
    assertEquals(hll.toString(), deserializedHLL.toString());
    assertEquals(hll.toStringExtended(), deserializedHLL.toStringExtended());
    assertEquals(hll.hashCode(), deserializedHLL.hashCode());
    assertEquals(hll.count(), deserializedHLL.count());
    assertEquals(hashset.size(), hll.count(), delta);
    assertEquals(hashset.size(), deserializedHLL.count(), delta);
  }
}

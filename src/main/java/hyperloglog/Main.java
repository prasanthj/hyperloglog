package hyperloglog;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Lists;

public class Main {

  final static int COUNT = 800000;
  final static int SEED = 100;
  final static String fileName = "src/main/resources/hll.dat";

  static List<Long> getRandomData(int count) {
    List<Long> result = Lists.newArrayList();
    Random rand = new Random(SEED);
    for (int i = 0; i < count; i++) {
      //result.add((long) rand.nextInt(10000));
      result.add(rand.nextLong());
    }
    return result;
  }

  public static void main(String[] args) throws IOException {
    long start = System.currentTimeMillis();
    HyperLogLog hll = new HyperLogLog();
    List<Long> data = getRandomData(COUNT);
    Set<Long> uniqueData = new HashSet<Long>(data);
    File outFile = new File(fileName);
    FileOutputStream fos = new FileOutputStream(outFile);
    DataOutputStream out = new DataOutputStream(fos);

    for (Long val : data) {
      hll.addLong(val);
    }

    long estCount = hll.count();
    long end = System.currentTimeMillis();
    long time = end - start;
    long actualCount = uniqueData.size();
    float err = HyperLogLogUtils.getRelativeError(actualCount, estCount);
    System.out.println("HyperLogLog - " + " Actual count: " + actualCount + " Estimated count: "
        + estCount + " Relative Error: " + err + "% Execution time: " + time + " ms");
    System.out.println("Actual size: " + hll.getRegister().length + " bytes");

    HyperLogLogUtils.serializeHLL(out, hll);
    System.out.println("Serialized HLL size: " + out.size() + " bytes");
    out.flush();
    out.close();

    FileInputStream fis = new FileInputStream(outFile);
    DataInputStream in = new DataInputStream(fis);
    HyperLogLog deserializedHLL = HyperLogLogUtils.deserializeHLL(in);
    System.out.println("Original HLL: " + hll.toString());
    System.out.println("Deserialized HLL: " + deserializedHLL.toString());
    System.out.println("Is deserialized HLL same as original HLL? : "
        + hll.toStringExtended().equals(deserializedHLL.toStringExtended()));
    in.close();
  }
}

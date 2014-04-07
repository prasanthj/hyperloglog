package hyperloglog;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

public class Main {

  final static int COUNT = 1000000;
  final static int SEED = 100;

  static List<Long> getRandomData(int count) {
    List<Long> result = Lists.newArrayList();
    Random rand = new Random(SEED);
    for (int i = 0; i < count; i++) {
      result.add(rand.nextLong());
    }
    return result;
  }

  public static void main(String[] args) {
    long start = System.currentTimeMillis();
    HyperLogLog hll = new HyperLogLog();
    List<Long> data = getRandomData(COUNT);
    for (Long val : data) {
      hll.addLong(val);
    }
    long estCount = hll.count();
    long end = System.currentTimeMillis();
    long time = end - start;
    HLLResult result = new HLLResult("HyperLogLog", data.size(), estCount, time);
    System.out.println(result);
    System.out.println("Actual size: " + hll.getRegister().length + " bytes");
    
    String serialized = HyperLogLogUtils.serializeAsBase64String(hll.getRegister());
    System.out.println("Base64 size: " + serialized.length() + " bytes");
    
    byte[] bitPacked = null;
    try {
      bitPacked = HyperLogLogUtils.bitpackRegister(hll.getRegister());
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("Bitpacked size: " + bitPacked.length + " bytes");
  }
}

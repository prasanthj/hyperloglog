package hyperloglog;

import java.util.Random;

public class Main {

  final static int COUNT = 1000000;

  public static void main(String[] args) {
    Random rand = new Random();
    HyperLogLogOriginal hllo = new HyperLogLogOriginal(16, 128);
    for (int i = 0; i < COUNT; i++) {
      hllo.addLong(rand.nextLong());
    }
    System.out.println("Actual count : " + COUNT);
    float stderr = (1.0f - ((float) hllo.count() / (float) COUNT)) * 100.0f;
    System.out.println("HLL Original estimated count : " + hllo.count() + " with standard error: "
        + stderr + "%");
  }
}

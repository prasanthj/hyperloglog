package hyperloglog;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class HyperLogLogOriginal {

  // number of registers
  private int m;
  private float alpha;
  private byte[] register;
  private HashFunction hf;
  private HashCode hc;
  private int registerIdx;
  private int p;
  private long w;
  private int lr;
  private long numElems;
  private long shortRangeThreshold;
  private long cachedCount;
  private boolean countInvalidate;

  public HyperLogLogOriginal() {
    this(16, 64);
  }

  public HyperLogLogOriginal(int p, int numBitsHash) {
    this.p = p;
    this.m = 1 << p;
    this.shortRangeThreshold = (long) (2.5f * m);
    this.register = new byte[m];
    initializeAlpha(numBitsHash);
    this.hf = Hashing.goodFastHash(numBitsHash);
    this.numElems = 0;
    this.cachedCount = -1;
    this.countInvalidate = false;
  }

  // see paper for alpha initialization
  private void initializeAlpha(int numBitsHash) {
    if (numBitsHash <= 16) {
      alpha = 0.673f;
    } else if (numBitsHash <= 32) {
      alpha = 0.697f;
    } else if (numBitsHash <= 64) {
      alpha = 0.709f;
    } else {
      alpha = 0.7213f / (float) (1 + 1.079f / m);
    }
  }

  public void addLong(long val) {
    hc = hf.newHasher().putLong(val).hash();
    add();
  }

  private void add() {
    numElems++;
    long hashcode = hc.asLong();
    registerIdx = (int) (hashcode & (m - 1));
    w = hashcode >>> p;
    lr = findLongestRun(w);
    int currentVal = register[registerIdx];
    if (lr > currentVal) {
      register[registerIdx] = (byte) lr;
      countInvalidate = true;
    }
  }

  public long count() {
    if(countInvalidate || cachedCount < -1) {
      double sum = 0;
      long numZeros = 0;
      for (int i = 0; i < register.length; i++) {
        if (register[i] == 0) {
          numZeros++;
          sum += 1;
        } else {
          sum += Math.pow(2, -register[i]);
        }
      }
      cachedCount = (long) (alpha * (Math.pow(m, 2)) * (1d / (double) sum));

      // For short range correction use linear counting
      if (numElems <= shortRangeThreshold) {
        if (numZeros != 0) {
          cachedCount = linearCount(numZeros);
        }
      }
    }
    return cachedCount;
  }

  private long linearCount(long numZeros) {
    return (long) (Math.round(m * Math.log(m / ((double) numZeros))));
  }

  private int findLongestRun(long v) {
    int i = 1;
    while ((v & 1) == 0) {
      v = v >>> 1;
      i++;
    }
    return i;
  }
}

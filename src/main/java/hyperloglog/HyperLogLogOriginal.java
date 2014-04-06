package hyperloglog;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class HyperLogLogOriginal {

  // number of registers
  private int m;
  private float alpha;

  // number of bits to address registers
  private int p;

  // 8-bit registers.
  // TODO: This can further be space optimized using 6 bit registers as longest
  // run for long value (hashcode) can be maximum of 64.
  // Using p = 14,
  // space required for 8-bit registers = (2 ^ 14) * 8 = 16KB
  // space required for 8-bit registers = (2 ^ 14) * 6 = 12KB
  private byte[] register;

  // Good fast hash function suggested by Guava hashing for the specified bits
  // Default is MurmurHash3_128
  private HashFunction hf;
  private HashCode hc;

  // LSB p bits of hashcode
  private int registerIdx;

  // MSB (64 - p) bits of hashcode
  private long w;

  // longest run of zeroes
  private int lr;

  private long numElems;

  // counts are cached to avoid complex computation. If register value is updated
  // the count will be computed again.
  private long cachedCount;
  private boolean countInvalidate;

  public HyperLogLogOriginal() {
    this(14, 64);
  }

  public HyperLogLogOriginal(int p, int numBitsHash) {
    this.p = p;
    this.m = 1 << p;
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
    long hashcode = 0;
    if (hc.bits() < 64) {
      hashcode = hc.asInt();
    } else {
      hashcode = hc.asLong();
    }

    // LSB p bits
    registerIdx = (int) (hashcode & (m - 1));

    // MSB 64 - p bits
    w = hashcode >>> p;

    // longest run of zeroes
    lr = findLongestRun(w);

    // update register if the longest run exceeds the previous entry
    int currentVal = register[registerIdx];
    if (lr > currentVal) {
      register[registerIdx] = (byte) lr;
      countInvalidate = true;
    }
  }

  public long count() {

    // compute count only if the register values are updated else return the
    // cached count
    if (countInvalidate || cachedCount < -1) {
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

      // cardinality estimate from normalized bias corrected harmonic mean on
      // the registers
      cachedCount = (long) (alpha * (Math.pow(m, 2)) * (1d / (double) sum));
      int bits = hc.bits();
      long pow = (long) Math.pow(2, bits);

      // HLL algorithm shows stronger bias for values in (2.5 * m) range.
      // To compensate for this short range bias, linear counting is used for
      // values before this short range. The original paper also says similar
      // bias is seen for long range values due to hash collisions in range >1/30*(2^32)
      // We do not have to worry about this long range bias as the paper used
      // 32-bit hashing and we use 64-bit hashing as default. 2^64 values are too
      // high to observe long range bias.
      if (numElems <= 2.5 * m) {
        if (numZeros != 0) {
          cachedCount = linearCount(numZeros);
        }
      } else if (bits < 64 && numElems > (0.033333 * pow)) {

        // long range bias for 32-bit hashcodes
        if (numElems > (1 / 30) * pow) {
          cachedCount = (long) (-pow * Math.log(1.0 - (double) cachedCount / (double) pow));
        }
      }
      
      countInvalidate = false;
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

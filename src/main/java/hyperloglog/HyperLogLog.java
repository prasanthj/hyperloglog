package hyperloglog;

import java.nio.charset.Charset;
import java.util.Arrays;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class HyperLogLog {

  public static final int MIN_P_VALUE = 4;
  public static final int MAX_P_VALUE = 16;

  public static enum EncodingType {
    SPARSE, DENSE
  }

  // number of bits to address registers
  private int p;

  // number of registers - 2^p
  private int m;
  private float alpha;
  private int chosenHashBits;

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

  // counts are cached to avoid complex computation. If register value is updated
  // the count will be computed again.
  private long cachedCount;
  private boolean invalidateCount;

  private EncodingType encoding;

  /**
   * By default, HyperLogLog uses 14 LSB bits of hashcode as register index and a 64 bit
   * hashfunction (Good hash function for 64bit as suggested by Google Guava library is
   * MurmurHash3_128).
   */
  public HyperLogLog() {
    this(14, 128);
  }

  /**
   * Specify the number of bits in hashcode to be used as register index. Also specify the number
   * bits for hash function. The hash function is chosen by Guava library based on the specified
   * bits.
   * @param p
   *          - number of register bits in the range 4 to 16 (inclusive)
   * @param numBitsHash
   *          - bits for hash function
   */
  public HyperLogLog(int p, int numBitsHash) {
    if (p < MIN_P_VALUE || p > MAX_P_VALUE) {
      throw new IllegalArgumentException("p value should be between " + MIN_P_VALUE + " to "
          + MAX_P_VALUE);
    }
    this.p = p;
    this.m = 1 << p;
    this.register = new byte[m];

    // we won't need hash functions beyond 128 bits.. in fact 64 bits itself is
    // more than sufficient
    if (numBitsHash > 128) {
      numBitsHash = 128;
    }
    this.hf = Hashing.goodFastHash(numBitsHash);
    this.chosenHashBits = hf.bits();
    initializeAlpha();
    this.cachedCount = -1;
    this.invalidateCount = false;
    // TODO: Change it to SPARSE after adding support for sparse encoding
    this.encoding = EncodingType.DENSE;
  }

  // see paper for alpha initialization
  private void initializeAlpha() {
    if (chosenHashBits <= 16) {
      alpha = 0.673f;
    } else if (chosenHashBits <= 32) {
      alpha = 0.697f;
    } else if (chosenHashBits <= 64) {
      alpha = 0.709f;
    } else {
      alpha = 0.7213f / (float) (1 + 1.079f / m);
    }
  }

  public void addBoolean(boolean val) {
    hc = hf.newHasher().putBoolean(val).hash();
    add(getHashCode());
  }

  public void addByte(byte val) {
    hc = hf.newHasher().putByte(val).hash();
    add(getHashCode());
  }

  public void addBytes(byte[] val) {
    hc = hf.newHasher().putBytes(val).hash();
    add(getHashCode());
  }

  public void addBytes(byte[] val, int offset, int len) {
    hc = hf.newHasher().putBytes(val, offset, len).hash();
    add(getHashCode());
  }

  public void addShort(short val) {
    hc = hf.newHasher().putShort(val).hash();
    add(getHashCode());
  }

  public void addInt(int val) {
    hc = hf.newHasher().putInt(val).hash();
    add(getHashCode());
  }

  public void addLong(long val) {
    hc = hf.newHasher().putLong(val).hash();
    add(getHashCode());
  }

  public void addFloat(float val) {
    hc = hf.newHasher().putFloat(val).hash();
    add(getHashCode());
  }

  public void addDouble(double val) {
    hc = hf.newHasher().putDouble(val).hash();
    add(getHashCode());
  }

  public void addChar(char val) {
    hc = hf.newHasher().putChar(val).hash();
    add(getHashCode());
  }

  /**
   * Java's default charset will be used for strings.
   * @param val
   *          - input string
   */
  public void addString(String val) {
    hc = hf.newHasher().putString(val, Charset.defaultCharset()).hash();
    add(getHashCode());
  }

  public void addString(String val, Charset charset) {
    hc = hf.newHasher().putString(val, charset).hash();
    add(getHashCode());
  }

  private long getHashCode() {
    long hashcode = 0;
    if (chosenHashBits < 64) {
      hashcode = hc.asInt();
    } else {
      hashcode = hc.asLong();
    }
    return hashcode;
  }

  public void add(long hashcode) {
    // LSB p bits
    registerIdx = (int) (hashcode & (m - 1));

    // MSB 64 - p bits
    w = hashcode >>> p;

    // longest run of zeroes
    int lr = findLongestRun(w);

    // update register if the longest run exceeds the previous entry
    int currentVal = register[registerIdx];
    if (lr > currentVal) {
      register[registerIdx] = (byte) lr;
      invalidateCount = true;
    }
  }

  public long count() {

    // compute count only if the register values are updated else return the
    // cached count
    if (invalidateCount || cachedCount < 0) {
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
      long pow = (long) Math.pow(2, chosenHashBits);

      // HLL algorithm shows stronger bias for values in (2.5 * m) range.
      // To compensate for this short range bias, linear counting is used for
      // values before this short range. The original paper also says similar
      // bias is seen for long range values due to hash collisions in range >1/30*(2^32)
      // For the default case, we do not have to worry about this long range bias
      // as the paper used 32-bit hashing and we use 64-bit hashing as default.
      // 2^64 values are too high to observe long range bias.
      if (cachedCount <= 2.5 * m) {
        if (numZeros != 0) {
          cachedCount = linearCount(numZeros);
        }
      } else if (chosenHashBits < 64 && cachedCount > (0.033333 * pow)) {

        // long range bias for 32-bit hashcodes
        if (cachedCount > (1 / 30) * pow) {
          cachedCount = (long) (-pow * Math.log(1.0 - (double) cachedCount / (double) pow));
        }
      }

      invalidateCount = false;
    }
    return cachedCount;
  }

  public void setCount(long count) {
    this.cachedCount = count;
    this.invalidateCount = true;
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

  public double getStandardError() {
    return 1.04 / Math.sqrt(m);
  }

  public byte[] getRegister() {
    return register;
  }

  public void setRegister(byte[] register) {
    this.register = register;
  }

  public void merge(HyperLogLog hll) {
    byte[] inRegister = hll.getRegister();

    if (register.length != inRegister.length) {
      throw new IllegalArgumentException(
          "The size of register sets of HyperLogLogs to be merged does not match.");
    }

    for (int i = 0; i < inRegister.length; i++) {
      if (inRegister[i] > register[i]) {
        register[i] = inRegister[i];
      }
    }
    invalidateCount = true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Encoding: ");
    sb.append(encoding);
    sb.append(", p : ");
    sb.append(p);
    sb.append(", chosenHashBits: ");
    sb.append(chosenHashBits);
    sb.append(", estimatedCardinality: ");
    sb.append(count());
    return sb.toString();
  }

  public String toStringExtended() {
    return toString() + ", register: " + Arrays.toString(register);
  }

  public int getNumRegisterIndexBits() {
    return p;
  }

  public int getNumHashBits() {
    return chosenHashBits;
  }

  public EncodingType getEncoding() {
    return encoding;
  }

  public void setEncoding(EncodingType encoding) {
    this.encoding = encoding;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof HyperLogLog)) {
      return false;
    }

    HyperLogLog other = (HyperLogLog) obj;
    long count = count();
    long otherCount = other.count();
    return p == other.p && chosenHashBits == other.chosenHashBits
        && encoding.equals(other.encoding) && count == otherCount
        && Arrays.equals(register, other.register);
  }

  @Override
  public int hashCode() {
    int hashcode = 0;
    hashcode += 31 * p;
    hashcode += 31 * chosenHashBits;
    hashcode += encoding.hashCode();
    hashcode += 31 * count();
    hashcode += Arrays.hashCode(register);
    return hashcode;
  }
}

package hyperloglog;

import it.unimi.dsi.fastutil.ints.Int2ByteAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ByteSortedMap;

public class HLLSparseRegister {

  private Int2ByteSortedMap sparseMap;
  // 1/8th the size of sparse list
  private int[] tempList;
  private int tempListIdx;
  private final int p;
  private final int pPrime;
  private final int qPrime;
  private final int mask;
  private final int pPrimeMask;
  private final int qPrimeMask;

  public HLLSparseRegister(int p, int pp, int qp) {
    this.p = p;
    int size = 6 * (1 << p);
    this.sparseMap = new Int2ByteAVLTreeMap();
    this.tempList = new int[size / 32];
    this.tempListIdx = 0;
    this.pPrime = pp;
    this.qPrime = qp;
    this.mask = ((1 << pPrime) - 1) ^ ((1 << p) - 1);
    this.pPrimeMask = ((1 << pPrime) - 1);
    this.qPrimeMask = (1 << qPrime) - 1;
  }

  public boolean add(long hashcode) {
    boolean updated = false;
    if (tempListIdx < tempList.length) {
      int encodedHash = encodeHash(hashcode);
      tempList[tempListIdx++] = encodedHash;
      updated = true;
    } else {
      updated = merge();
      tempListIdx = 0;
    }

    return updated;
  }

  private boolean merge() {
    boolean updated = false;
    for (int i = 0; i < tempListIdx; i++) {
      int encodedHash = tempList[i];
      int key = encodedHash & pPrimeMask;
      byte value = (byte) (encodedHash >>> pPrime);
      byte nr = 0;
      if (encodedHash < 0) {
        nr = (byte) (value & qPrimeMask);
      } else {
        nr = (byte) (Integer.numberOfTrailingZeros(encodedHash >>> p) + 1);
      }
      if (sparseMap.containsKey(key) && encodedHash < 0) {
        byte containedVal = sparseMap.get(key);
        if (nr > containedVal) {
          sparseMap.put(key, nr);
          updated = true;
        }
      } else {
        sparseMap.put(key, nr);
        updated = true;
      }
    }
    return updated;
  }

  /**
   * <pre>
   * <b>Input:</b> 64 bit hashcode
   * 
   * |---------w-------------| |------------p'----------|
   * 10101101.......1010101010 10101010101 01010101010101
   *                                       |------p-----|
   *                                       
   * <b>Output:</b> 32 bit int
   * 
   * |b| |-q'-|  |------------p'----------|
   *  1  010101  01010101010 10101010101010
   *                         |------p-----|
   *                    
   * 
   * The default values of p', q' and b are 25, 6, 1 (total 32 bits) respectively.
   * This function will return an int encoded in the following format
   * 
   * p  - LSB p bits represent the register index
   * p' - LSB p' bits are used for increased accuracy in estimation
   * q' - q' bits after p' are left as such from the hashcode if b = 0 else
   *      q' bits encodes the longest trailing zero runs from in (w-p) input bits
   * b  - 0 if longest trailing zero run is contained within (p'-p) bits
   *      1 if longest trailing zero run is computeed from (w-p) input bits and
   *      its value is stored in q' bits
   * </pre>
   * @param hashcode
   * @return
   */
  public int encodeHash(long hashcode) {
    // x = p' - p
    int x = (int) (hashcode & mask);
    if (x == 0) {
      // more bits should be considered for finding q (longest zero runs)
      // set MSB to 1
      int ntr = Long.numberOfTrailingZeros(hashcode >> p) + 1;
      long newHashCode = hashcode & pPrimeMask;
      newHashCode |= ntr << pPrime;
      newHashCode |= 0x80000000;
      return (int) newHashCode;
    } else {
      // q is contained within p' - p
      // set MSB to 0
      return (int) (hashcode & 0x7FFFFFFF);
    }
  }

  public int getSize() {
    merge();
    return sparseMap.size();
  }

  public Int2ByteSortedMap getSparseMap() {
    return sparseMap;
  }

  public int getP() {
    return p;
  }

  public int getPPrime() {
    return pPrime;
  }
}

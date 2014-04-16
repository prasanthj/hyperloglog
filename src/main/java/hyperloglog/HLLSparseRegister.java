package hyperloglog;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;

public class HLLSparseRegister {

  private int[] sparseList;
  // 1/8th the size of sparse list
  private long[] tempList;
  private int sparseListIdx;
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
    this.sparseList = new int[size / 4];
    this.tempList = new long[size / 32];
    this.sparseListIdx = 0;
    this.tempListIdx = 0;
    this.pPrime = pp;
    this.qPrime = qp;
    this.mask = ((1 << pPrime) - 1) ^ ((1 << p) - 1);
    this.pPrimeMask = ((1 << pPrime) - 1);
    this.qPrimeMask = (1 << qPrime) - 1;
  }

  public int add(long hashcode) {
    if (sparseListIdx + tempListIdx > sparseList.length) {
      // no more insertion possible
      return -1;
    }
    if (tempListIdx < tempList.length) {
      int encodedHash = encodeHash(hashcode);
      tempList[tempListIdx++] = encodedHash;
    } else {
      tempList = sortTempList(tempList, tempListIdx);
      sparseList = merge(sparseList, sparseListIdx, tempList, tempListIdx);
      tempListIdx = 0;
    }

    return 0;
  }

  public int[] merge(int[] sl, int sIdx, long[] tl, int tIdx) {
    int[] scratch = new int[sl.length];
    int i = 0;
    if (sIdx == 0) {
      // copy from tempList directly to sparseList
      for (i = 0; i < tIdx; i++) {
        scratch[i] = encodeHash(tl[i]);
      }
      sparseListIdx = i;
    } else {

    }
    return scratch;
  }

  public long[] sortTempList(long[] tl, int tlIdx) {
    List<Long> outList = Lists.newArrayListWithCapacity(tlIdx);
    for (int i = 0; i < tlIdx; i++) {
      outList.add(tl[i]);
    }

    Collections.sort(outList, new Comparator<Long>() {

      public int compare(Long o1, Long o2) {
        long val1 = o1;
        long val2 = o2;
        int idx1 = (int) (val1 & pPrimeMask);
        int idx2 = (int) (val2 & pPrimeMask);
        if (idx1 > idx2) {
          return 1;
        } else if (idx2 > idx1) {
          return -1;
        } else {
          if (val1 < 0 && val2 < 0) {
            // both negative
            val1 = (val1 >>> pPrime) & qPrimeMask;
            val2 = (val2 >>> pPrime) & qPrimeMask;
          } else if (val1 < 0 && val2 >= 0) {
            // val1 is negative and it must have more trailing zeros
            return 1;
          } else if (val2 < 0 && val1 >= 0) {
            // val2 is negative and it must have more trailing zeros
            return -1;
          } else {
            // both positive
            val1 = Integer.numberOfTrailingZeros((int) (val1 >>> p));
            val2 = Integer.numberOfTrailingZeros((int) (val2 >>> p));
          }

          if (val1 > val2) {
            return 1;
          } else if (val2 > val1) {
            return -1;
          }
        }
        return 0;
      }

    });
    return Longs.toArray(outList);
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
    return sparseListIdx * 4;
  }
}

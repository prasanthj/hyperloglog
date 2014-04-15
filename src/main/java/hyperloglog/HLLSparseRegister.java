package hyperloglog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

public class HLLSparseRegister {

  private int[] sparseList;
  // 1/8th the size of sparse list
  private int[] tempList;
  private int sparseListIdx;
  private int tempListIdx;
  private final int p;
  private final int pPrime;
  private final int qPrime;
  private final int mask;
  private final int pPrimeMask;

  public HLLSparseRegister(int p, int pp, int qp) {
    this.p = p;
    int size = 6 * (1 << p);
    this.sparseList = new int[size / 4];
    this.tempList = new int[size / 32];
    this.sparseListIdx = 0;
    this.tempListIdx = 0;
    this.pPrime = pp;
    this.qPrime = qp;
    this.mask = ((1 << pPrime) - 1) ^ ((1 << p) - 1);
    this.pPrimeMask = ((1 << pPrime) - 1);
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
      tempList = sortTempList();
      merge();
      tempListIdx = 0;
    }

    return 0;
  }

  private void merge() {

  }

  private int[] sortTempList() {
    List<Integer> outList = Lists.newArrayList(tempListIdx);
    for (int t : tempList) {
      outList.add(t);
    }
    tempList = null;
    Collections.sort(outList, new Comparator<Integer>() {

      public int compare(Integer o1, Integer o2) {
        int idx1 = o1.intValue() >>> 7;
        int idx2 = o2.intValue() >>> 7;
        if (idx1 > idx2) {
          return 1;
        } else if (idx2 > idx1) {
          return -1;
        } else {
          int val1 = getZeroRuns(o1.intValue());
        }
        return 0;
      }

    });
    return Ints.toArray(outList);
  }

  private int getZeroRuns(int i) {
    int run = 0;
    if ((i & 0xfffffffe) == 0) {

    } else {
      int runMask = (1 << qPrime + (pPrime - p)) - 1;
      int runVal = (i >>> 1) & runMask;
      return Integer.numberOfTrailingZeros(runVal);
    }
    return run;
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
      int min = Integer.MIN_VALUE;
      int max = Integer.MAX_VALUE;
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

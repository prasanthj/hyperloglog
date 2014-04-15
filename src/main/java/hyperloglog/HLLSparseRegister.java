package hyperloglog;

import it.unimi.dsi.fastutil.ints.Int2ByteAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ByteSortedMap;

import java.util.Arrays;
import java.util.Comparator;

public class HLLSparseRegister {

  private int[] sparseList;
  // 1/8th the size of sparse list
  private int[] tempList;
  private int sparseListIdx;
  private int tempListIdx;
  private final int pPrime;
  private final int qPrime;
  private final int mask;
  private Int2ByteSortedMap sparseMap;

  public HLLSparseRegister(int p, int pp, int qp) {
    int size = 6 * (1 << p);
    this.sparseList = new int[size / 4];
    this.tempList = new int[size / 32];
    this.sparseListIdx = 0;
    this.tempListIdx = 0;
    this.pPrime = pp;
    this.qPrime = qp;
    this.mask = (1 << (pPrime - p)) - 1;
    this.sparseMap = new Int2ByteAVLTreeMap();
  }

  public int add(int hashcode) {
    if (sparseListIdx + tempListIdx > sparseList.length) {
      // no more insertion possible
      return -1;
    }
    if (tempListIdx < tempList.length) {
      tempList[tempListIdx++] = encodeHash(hashcode);
    } else {
      Arrays.sort(tempList);
      merge(sparseList, tempList);
      tempListIdx = 0;
    }

    return 0;
  }

  private void merge(int[] sl, int[] tl) {
    
  }

  private int encodeHash(int hashcode) {
    // x = p' - p
    int x = (hashcode >> (qPrime + 1)) & mask;
    if (x == 0) {
      // more bits should be considered for finding q (longest zero runs)
      // set LSB to 1
      return hashcode | 1;
    } else {
      // q is contained within p' - p
      // set LSB to 0
      return hashcode & 0xFFFFFFFE;
    }
  }
  
  public int getSize() {
    return sparseListIdx * 4;
  }
}

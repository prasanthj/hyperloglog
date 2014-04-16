package hyperloglog;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class TestSortTempList {

  @Test
  public void testSortTempList1() {
    HashFunction hf = Hashing.goodFastHash(64);
    HashCode hc = hf.newHasher().putLong(1).hash();
    long v1 = hc.asLong();
    hc = hf.newHasher().putLong(2).hash();
    long v2 = hc.asLong();
    hc = hf.newHasher().putLong(3).hash();
    long v3 = hc.asLong();
    hc = hf.newHasher().putLong(4).hash();
    long v4 = hc.asLong();
    long[] tempList = new long[] { v1, v2, v3, v4 };
    long[] expected = new long[] { v3, v4, v2, v1 };
    int[] merged = new int[] { 2, 3 };
    HLLSparseRegister hsr = new HLLSparseRegister(14, 25, 6);
    long[] got = hsr.sortTempList(tempList, tempList.length);
    assertArrayEquals(expected, got);
    int[] mergedGot = hsr.merge(new int[tempList.length], 0, tempList, tempList.length);
    assertArrayEquals(merged, mergedGot);
  }

  @Test
  public void testSortTempList2() {
    long[] tempList = new long[] { 2, 3 };
    long[] expected = new long[] { 2, 3 };
    HLLSparseRegister hsr = new HLLSparseRegister(14, 25, 6);
    long[] got = hsr.sortTempList(tempList, tempList.length);
    assertArrayEquals(expected, got);
  }

  @Test
  public void testSortTempList3() {
    long[] tempList = new long[] { 2, 2 };
    long[] expected = new long[] { 2, 2 };
    HLLSparseRegister hsr = new HLLSparseRegister(14, 25, 6);
    long[] got = hsr.sortTempList(tempList, tempList.length);
    assertArrayEquals(expected, got);
  }

  @Test
  public void testSortTempList4() {
    long[] tempList = new long[] { 3, 2, 25165826, 16777218 };
    long[] expected = new long[] { 2, 3, 16777218, 25165826 };
    HLLSparseRegister hsr = new HLLSparseRegister(14, 25, 6);
    long[] got = hsr.sortTempList(tempList, tempList.length);
    assertArrayEquals(expected, got);
  }

  @Test
  public void testSortTempList5() {
    long[] tempList = new long[] { 3, 2, 25165826, 16777218, -1845485568, -1879040000 };
    long[] expected = new long[] { 2, 3, -1879040000, -1845485568, 16777218, 25165826 };
    HLLSparseRegister hsr = new HLLSparseRegister(14, 25, 6);
    long[] got = hsr.sortTempList(tempList, tempList.length);
    assertArrayEquals(expected, got);
  }

  @Test
  public void testSortTempList6() {
    long[] tempList = new long[] { -1845485568, 8192 };
    long[] expected = new long[] { 8192, -1845485568 };
    HLLSparseRegister hsr = new HLLSparseRegister(14, 25, 6);
    long[] got = hsr.sortTempList(tempList, tempList.length);
    assertArrayEquals(expected, got);
  }

  @Test
  public void testSortTempList7() {
    long[] tempList = new long[] { -1845485568, 8192, -1879040000 };
    long[] expected = new long[] { 8192, -1879040000, -1845485568 };
    HLLSparseRegister hsr = new HLLSparseRegister(14, 25, 6);
    long[] got = hsr.sortTempList(tempList, tempList.length);
    assertArrayEquals(expected, got);
  }
}

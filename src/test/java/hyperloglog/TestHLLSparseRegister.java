package hyperloglog;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class TestHLLSparseRegister {

  private long input;
  private int expected;

  public TestHLLSparseRegister(long i, int e) {
    this.input = i;
    this.expected = e;
  }

  @Parameters
  public static Collection<Object[]> data() {
    Object[][] data = new Object[][] { { 11111111111L, 373692871 }, { 4314495982023L, -1711269433  },
        { 4314529536455L, -1744823865  }, { 4314563074503L, 268425671 },
        { 17257983908295L, -1644160569 }, { 536861127L, 536861127 }, { 536844743L, 536844743 }, 
        { 144115188075862471L, -671082041 }};
    return Arrays.asList(data);
  }

  @Test
  public void testEncodeHash() {
    HLLSparseRegister reg = new HLLSparseRegister(14, 25, 6);
    int got = reg.encodeHash(input);
    assertEquals(expected, got);
  }
}

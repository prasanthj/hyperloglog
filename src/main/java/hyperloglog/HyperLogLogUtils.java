package hyperloglog;

import hyperloglog.HyperLogLog.EncodingType;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class HyperLogLogUtils {

  public static final byte[] MAGIC = new byte[] { 'H', 'L', 'L' };

  static void bitpackHLLRegister(OutputStream out, byte[] register, int bitWidth)
      throws IOException {
    int bitsLeft = 8;
    byte current = 0;

    // write the blob
    for (byte value : register) {
      int bitsToWrite = bitWidth;
      while (bitsToWrite > bitsLeft) {
        // add the bits to the bottom of the current word
        current |= value >>> (bitsToWrite - bitsLeft);
        // subtract out the bits we just added
        bitsToWrite -= bitsLeft;
        // zero out the bits above bitsToWrite
        value &= (1 << bitsToWrite) - 1;
        out.write(current);
        current = 0;
        bitsLeft = 8;
      }
      bitsLeft -= bitsToWrite;
      current |= value << bitsLeft;
      if (bitsLeft == 0) {
        out.write(current);
        current = 0;
        bitsLeft = 8;
      }
    }

    out.flush();
  }

  /**
   * Unpack the bitpacked HyperLogLog register.
   * @param packedRegister
   *          - bit packed register
   * @return unpacked HLL register
   * @throws IOException
   */
  static byte[] unpackHLLRegister(InputStream in, int length, int bitSize) throws IOException {
    int mask = (1 << bitSize) - 1;
    int bitsLeft = 8;
    byte current = (byte) (0xff & in.read());

    byte[] output = new byte[length];
    for (int i = 0; i < output.length; i++) {
      byte result = 0;
      int bitsLeftToRead = bitSize;
      while (bitsLeftToRead > bitsLeft) {
        result <<= bitsLeft;
        result |= current & ((1 << bitsLeft) - 1);
        bitsLeftToRead -= bitsLeft;
        current = (byte) (0xff & in.read());
        bitsLeft = 8;
      }
      if (bitsLeftToRead > 0) {
        result <<= bitsLeftToRead;
        bitsLeft -= bitsLeftToRead;
        result |= (current >>> bitsLeft) & ((1 << bitsLeftToRead) - 1);
      }
      output[i] = (byte) (result & mask);
    }
    return output;
  }

  /**
   * HyperLogLog is serialized using the following format
   * 
   * <pre>
   * |-4 byte-|------varlong----|----(optional)---|----------|  
   * ---------------------------------------------------------
   * | header | estimated-count | register-length | register |
   * ---------------------------------------------------------
   * 
   * <b>4 byte header</b> is encoded like below
   * 3 bytes - HLL magic string to identify serialized stream
   * 4 bits  - p (number of bits to be used as register index)
   * 1 bit   - hash function (0 - MurmurHash3_32, 1 - MurmurHash3_128)
   * 3 bits  - encoding (000 - sparse, 001..110 - n bit packing, 111 - unused)
   * 
   * Followed by header are 3 fields that are required for reconstruction
   * of hyperloglog
   * Estimated count - variable length long to store last computed estimated count.
   *                   This is just for quick lookup without deserializing registers
   * Register length - number of entries in the register (required only for 
   *                   for sparse representation. For bit-packing, the register
   *                   length can be found from p)
   * </pre>
   * @param out
   *          - output stream to write to
   * @param hll
   *          - hyperloglog that needs to be serialized
   * @throws IOException
   */
  public static void serializeHLL(OutputStream out, HyperLogLog hll) throws IOException {
    // write header
    out.write(MAGIC);
    int fourthByte = 0;
    int p = hll.getNumRegisterIndexBits();
    fourthByte = (p & 0xff) << 4;
    int hb = hll.getNumHashBits();
    if (hb > 32) {
      fourthByte |= 1 << 3;
    }

    int bitWidth = 0;
    EncodingType enc = hll.getEncoding();
    if (enc.equals(EncodingType.SPARSE)) {

    } else if (enc.equals(EncodingType.DENSE)) {
      int lzr = getMax(hll.getRegister());
      bitWidth = getBitWidth(lzr);
      fourthByte |= (bitWidth & 7);
    } else {
      throw new IllegalArgumentException("Unknown encoding encountered.");
    }

    out.write(fourthByte);

    long estCount = hll.count();
    writeVulong(out, estCount);

    if (enc.equals(EncodingType.DENSE)) {
      byte[] register = hll.getRegister();
      bitpackHLLRegister(out, register, bitWidth);
    }
  }

  private static int getMax(byte[] register) {
    int max = 0;
    for (byte b : register) {
      if (b > max) {
        max = b;
      }
    }
    return max;
  }

  public static HyperLogLog deserializeHLL(InputStream in) throws IOException {
    checkMagicString(in);
    int fourthByte = in.read() & 0xff;
    int p = fourthByte >>> 4;
    int hb = (fourthByte >>> 3) & 1;
    if (hb == 0) {
      hb = 32;
    } else {
      hb = 128;
    }

    int enc = fourthByte & 7;
    EncodingType encoding = null;
    int bitSize = 0;
    if (enc == 0) {
      encoding = EncodingType.SPARSE;
    } else if (enc > 0 && enc < 7) {
      bitSize = enc;
      encoding = EncodingType.DENSE;
    } else {
      throw new IllegalArgumentException("Unknown encoding encountered.");
    }

    // estimated count
    long estCount = readVulong(in);

    byte[] register = null;
    if (encoding.equals(EncodingType.SPARSE)) {
      long numRegisterEntries = readVulong(in);
      // TODO: implement SPARSE deserialization
    } else {
      int m = 1 << p;
      register = unpackHLLRegister(in, m, bitSize);
    }

    HyperLogLog result = new HyperLogLog(p, hb);
    result.setEncoding(encoding);
    result.setRegister(register);
    result.setCount(estCount);

    return result;
  }

  public static long getEstimatedCountFromSerializedHLL(InputStream in) throws IOException {
    checkMagicString(in);
    in.read();
    return readVulong(in);
  }

  private static void checkMagicString(InputStream in) throws IOException {
    byte[] magic = new byte[3];
    magic[0] = (byte) in.read();
    magic[1] = (byte) in.read();
    magic[2] = (byte) in.read();

    if (!Arrays.equals(magic, MAGIC)) {
      throw new IllegalArgumentException("The input stream is not a HyperLogLog stream.");
    }
  }

  private static int getBitWidth(int lzr) {
    int count = 0;
    while (lzr != 0) {
      count++;
      lzr = (byte) (lzr >>> 1);
    }
    return count;
  }

  public static float getRelativeError(long actualCount, long estimatedCount) {
    float err = (1.0f - ((float) estimatedCount / (float) actualCount)) * 100.0f;
    return err;
  }

  static void writeVulong(OutputStream output, long value) throws IOException {
    while (true) {
      if ((value & ~0x7f) == 0) {
        output.write((byte) value);
        return;
      } else {
        output.write((byte) (0x80 | (value & 0x7f)));
        value >>>= 7;
      }
    }
  }

  static void writeVslong(OutputStream output, long value) throws IOException {
    writeVulong(output, (value << 1) ^ (value >> 63));
  }

  static long readVulong(InputStream in) throws IOException {
    long result = 0;
    long b;
    int offset = 0;
    do {
      b = in.read();
      if (b == -1) {
        throw new EOFException("Reading Vulong past EOF");
      }
      result |= (0x7f & b) << offset;
      offset += 7;
    } while (b >= 0x80);
    return result;
  }

  static long readVslong(InputStream in) throws IOException {
    long result = readVulong(in);
    return (result >>> 1) ^ -(result & 1);
  }
}

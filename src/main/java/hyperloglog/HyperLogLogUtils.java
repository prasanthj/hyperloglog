package hyperloglog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.google.common.io.BaseEncoding;

public class HyperLogLogUtils {

  /**
   * Bitpacks the HyperLogLog register with minimum fixed bit width possible. The bitpacked output
   * byte array is stored in this format
   * 
   * <pre>
   * |---- 4 bytes ---|--- (length * bit-width)/8 bytes ---|
   * -------------------------------------------------------
   * |length|bit-width|................blob................|
   * -------------------------------------------------------
   * </pre>
   * <ul>
   * <li><i>Length</i> - Max elements in HLL register can be 2^16. Hence 3 bytes are required to
   * store length.</li>
   * <li><i>Fixed bit width</i> - Stored in a byte</li>
   * <li><i>Blob</i> - fixed bit width * length</li>
   * @param register
   *          - HLL register
   * @return bit packed register in the above format
   * @throws IOException
   */
  public static byte[] bitpackHLLRegister(byte[] register) throws IOException {
    if (register == null) {
      return null;
    }

    int length = register.length;
    byte max = 0;
    for (byte b : register) {
      if (b > max) {
        max = b;
      }
    }

    int bitWidth = getBitWidth(max);
    int bitsLeft = 8;
    byte current = 0;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream w = new DataOutputStream(baos);

    // write the length in MSB 3 bytes of int
    int header = length << 8;

    // write bit-width to 3 MSB bits
    header |= bitWidth & 0xff;

    // write header
    w.writeInt(header);

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
        w.write(current);
        current = 0;
        bitsLeft = 8;
      }
      bitsLeft -= bitsToWrite;
      current |= value << bitsLeft;
      if (bitsLeft == 0) {
        w.write(current);
        current = 0;
        bitsLeft = 8;
      }
    }

    w.flush();
    return baos.toByteArray();
  }

  /**
   * Unpack the bitpacked HyperLogLog register.
   * @param packedRegister
   *          - bit packed register
   * @return unpacked HLL register
   * @throws IOException
   */
  public static byte[] unpackHLLRegister(byte[] packedRegister) throws IOException {
    if (packedRegister == null) {
      return null;
    }

    ByteArrayInputStream bais = new ByteArrayInputStream(packedRegister);
    DataInputStream dis = new DataInputStream(bais);

    // read the header and decode length and bit width
    int header = dis.readInt();
    int length = header >>> 8;
    int bitSize = header & 0xff;

    int mask = (1 << bitSize) - 1;
    int bitsLeft = 8;
    byte current = (byte) (0xff & dis.readByte());

    byte[] output = new byte[length];
    for (int i = 0; i < output.length; i++) {
      byte result = 0;
      int bitsLeftToRead = bitSize;
      while (bitsLeftToRead > bitsLeft) {
        result <<= bitsLeft;
        result |= current & ((1 << bitsLeft) - 1);
        bitsLeftToRead -= bitsLeft;
        current = (byte) (0xff & dis.readByte());
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

  private static int getBitWidth(byte value) {
    int count = 0;
    while (value != 0) {
      count++;
      value = (byte) (value >>> 1);
    }
    return count;
  }

  public static String serializeAsBase64String(byte[] register) {
    return BaseEncoding.base64().encode(register);
  }

  public static byte[] deserializeFromBase64String(String encodedStr) {
    return BaseEncoding.base64().decode(encodedStr);
  }
}

package hyperloglog;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.io.BaseEncoding;

public class HyperLogLogUtils {

  /**
   * Format of output byte array is
   * 
   * <pre>
   * ----------------------------------
   * |length|bit width|.....blob......|
   * ----------------------------------
   * </pre>
   * <ul>
   * <li><i>Length</i> - Number of elements stored as varint</li>
   * <li><i>3 bits</i> - fixed bit width</li>
   * <li><i>Blob</i> - fixed bit width * length</li>
   * @param register
   * @return
   * @throws IOException 
   */
  public static byte[] bitpackRegister(byte[] register) throws IOException {
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
    
    // write the length as varint
    writeVarInt(w, length);
    
    // write bit-width to 3 MSB bits
    current = (byte) (bitWidth << 5);
    bitsLeft = 5;
    
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

  static void writeVarInt(OutputStream output, int value) throws IOException {
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

  private static long readVarInt(InputStream in) throws IOException {
    int result = 0;
    int b;
    int offset = 0;
    do {
      b = in.read();
      result |= (0x7f & b) << offset;
      offset += 7;
    } while (b >= 0x80);
    return result;
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

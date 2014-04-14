package hyperloglog;

import java.util.Arrays;

public class HLLRegister {

  private byte[] register;
  private int maxRegisterValue;
  private int numZeroes;
  private double[] invPow2Register;

  public HLLRegister(int size) {
    this.register = new byte[size];
    this.invPow2Register = new double[size];
    Arrays.fill(invPow2Register, 1.0);
    this.maxRegisterValue = 0;
    this.numZeroes = size;
  }

  public boolean set(int idx, byte value) {
    boolean updated = false;
    if (idx < register.length && value > register[idx]) {

      // update max register value
      if (value > maxRegisterValue) {
        maxRegisterValue = value;
      }

      // update number of zeros
      if (register[idx] == 0 && value > 0) {
        numZeroes--;
      }

      register[idx] = value;
      invPow2Register[idx] = Math.pow(2, -value);
      updated = true;
    }
    return updated;
  }

  public int size() {
    return register.length;
  }

  public int getNumZeroes() {
    return numZeroes;
  }

  public void merge(HLLRegister hllRegister) {
    byte[] inRegister = hllRegister.getRegister();

    if (register.length != inRegister.length) {
      throw new IllegalArgumentException(
          "The size of register sets of HyperLogLogs to be merged does not match.");
    }

    for (int i = 0; i < inRegister.length; i++) {
      if (inRegister[i] > register[i]) {
        if (register[i] == 0) {
          numZeroes--;
        }
        register[i] = inRegister[i];
        invPow2Register[i] = Math.pow(2, -inRegister[i]);
      }
    }

    if (hllRegister.getMaxRegisterValue() > maxRegisterValue) {
      maxRegisterValue = hllRegister.getMaxRegisterValue();
    }
  }

  public byte[] getRegister() {
    return register;
  }

  public void setRegister(byte[] register) {
    this.register = register;
  }

  public int getMaxRegisterValue() {
    return maxRegisterValue;
  }

  public double getSumInversePow2() {
    double sum = 0;
    for (double d : invPow2Register) {
      sum += d;
    }
    return sum;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("HLLRegister - ");
    sb.append("numZeroes: ");
    sb.append(numZeroes);
    sb.append(" maxRegisterValue: ");
    sb.append(maxRegisterValue);
    sb.append(" register: ");
    sb.append(Arrays.toString(register));
    return sb.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof HLLRegister)) {
      return false;
    }
    HLLRegister other = (HLLRegister) obj;
    return numZeroes == other.numZeroes && maxRegisterValue == other.maxRegisterValue
        && Arrays.equals(register, other.register);
  }

  @Override
  public int hashCode() {
    int hashcode = 0;
    hashcode += 31 * numZeroes;
    hashcode += 31 * maxRegisterValue;
    hashcode += Arrays.hashCode(register);
    return hashcode;
  }
}

package hyperloglog;

public interface HLLRegister {

  public boolean add(long value);

  public boolean set(int idx, byte value);

  public void merge(HLLRegister reg);
}

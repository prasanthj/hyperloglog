package hyperloglog;

public class HLLResult {
  String algorithm;
  long actualCount;
  long hllCount;
  long execTime;
  float err;

  public HLLResult(String algorithm, long actualCount, long estCount, long execTime) {
    this.algorithm = algorithm;
    this.actualCount = actualCount;
    this.hllCount = estCount;
    this.execTime = execTime;
    this.err = getRelativeError();
  }

  public float getRelativeError() {
    float err = (1.0f - ((float) hllCount / (float) actualCount)) * 100.0f;
    return err;
  }

  @Override
  public String toString() {
    return "Algorithm: " + algorithm + ", Actual Count: " + actualCount + ", Estimated Count: "
        + hllCount + ", Relative Error: " + err + "%, Execution Time: " + execTime + " ms";
  }
}

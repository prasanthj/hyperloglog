package hyperloglog;

public class HLLResult {
  String algorithm;
  long actualCount;
  long hllCount;
  long execTime;
  float stdErr;

  public HLLResult(String algorithm, long actualCount, long estCount, long execTime) {
    this.algorithm = algorithm;
    this.actualCount = actualCount;
    this.hllCount = estCount;
    this.execTime = execTime;
    this.stdErr = getStandardError();
  }

  public float getStandardError() {
    float stderr = (1.0f - ((float) hllCount / (float) actualCount)) * 100.0f;
    return stderr;
  }

  @Override
  public String toString() {
    return "Algorithm: " + algorithm + ", Actual Count: " + actualCount + ", Estimated Count: "
        + hllCount + ", Standard Error: " + stdErr + "%, Execution Time: " + execTime + " ms";
  }
}

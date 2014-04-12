package graphs;

import java.util.ArrayList;
import java.util.List;

public class HLLGraphData {

  private String key;
  private List<float[]> values;

  public HLLGraphData(String name) {
    this.key = name;
    this.values = new ArrayList<float[]>();
  }

  public String getKey() {
    return key;
  }

  public void setKey(String name) {
    this.key = name;
  }

  public void addPoint(float x, float y) {
    float point[] = new float[] { x, y };
    values.add(point);
  }

  public List<float[]> getValues() {
    return values;
  }

  public void setValues(List<float[]> points) {
    this.values = points;
  }
}

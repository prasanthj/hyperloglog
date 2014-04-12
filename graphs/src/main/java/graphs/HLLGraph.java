package graphs;

import hyperloglog.HyperLogLog;
import hyperloglog.HyperLogLogUtils;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class HLLGraph {
  private HyperLogLog hll;
  private ObjectMapper jsonMapper;
  private static final String fileName = "src/main/resources/data/input.json";
  private File outFile;

  public HLLGraph() {
    hll = new HyperLogLog();
    outFile = new File(fileName);
    jsonMapper = new ObjectMapper();
  }

  private void generateBiasGraphData() {
    HLLGraphData hld = new HLLGraphData("Short-Range-Bias");
    Random rand = new Random(100);

    for (int i = 1; i <= 80000; i++) {
      hll.addLong(rand.nextLong());
      hld.addPoint(i, HyperLogLogUtils.getRelativeError(i, hll.count()));
    }
    try {
      jsonMapper.writeValue(outFile, hld);
    } catch (JsonGenerationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (JsonMappingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("Succefully written " + fileName + " json file.");
  }

  public static void main(String[] args) {
    HLLGraph hllg = new HLLGraph();
    hllg.generateBiasGraphData();
  }
}

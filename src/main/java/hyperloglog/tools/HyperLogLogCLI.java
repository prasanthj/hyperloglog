/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hyperloglog.tools;

import hyperloglog.HyperLogLog;
import hyperloglog.HyperLogLog.EncodingType;
import hyperloglog.HyperLogLogUtils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class HyperLogLogCLI {

  public static void main(String[] args) {
    Options options = new Options();
    addOptions(options);

    CommandLineParser parser = new BasicParser();
    CommandLine cli = null;
    long n = 0;
    long seed = 123;
    EncodingType enc = EncodingType.SPARSE;
    int p = 14;
    int hb = 64;
    boolean bitPack = true;
    boolean noBias = true;
    int unique = -1;
    String filePath = null;
    BufferedReader br = null;
    String outFile = null;
    String inFile = null;
    FileOutputStream fos = null;
    DataOutputStream out = null;
    FileInputStream fis = null;
    DataInputStream in = null;
    try {
      cli = parser.parse(options, args);

      if (!(cli.hasOption('n') || cli.hasOption('f') || cli.hasOption('d'))) {
        System.out.println("Example usage: hll -n 1000 " + "<OR> hll -f /tmp/input.txt "
            + "<OR> hll -d -i /tmp/out.hll");
        usage(options);
        return;
      }

      if (cli.hasOption('n')) {
        n = Long.parseLong(cli.getOptionValue('n'));
      }

      if (cli.hasOption('e')) {
        String value = cli.getOptionValue('e');
        if (value.equals(EncodingType.DENSE.name())) {
          enc = EncodingType.DENSE;
        }
      }

      if (cli.hasOption('p')) {
        p = Integer.parseInt(cli.getOptionValue('p'));
        if (p < 4 && p > 16) {
          System.out.println("Warning! Out-of-range value specified for p. Using to p=14.");
          p = 14;
        }
      }

      if (cli.hasOption('h')) {
        hb = Integer.parseInt(cli.getOptionValue('h'));
      }

      if (cli.hasOption('c')) {
        noBias = Boolean.parseBoolean(cli.getOptionValue('c'));
      }

      if (cli.hasOption('b')) {
        bitPack = Boolean.parseBoolean(cli.getOptionValue('b'));
      }

      if (cli.hasOption('f')) {
        filePath = cli.getOptionValue('f');
        br = new BufferedReader(new FileReader(new File(filePath)));
      }

      if (filePath != null && cli.hasOption('n')) {
        System.out.println("'-f' (input file) specified. Ignoring -n.");
      }

      if (cli.hasOption('s')) {
        if (cli.hasOption('o')) {
          outFile = cli.getOptionValue('o');
          fos = new FileOutputStream(new File(outFile));
          out = new DataOutputStream(fos);
        } else {
          System.err.println("Specify output file. Example usage: hll -s -o /tmp/out.hll");
          usage(options);
          return;
        }
      }

      if (cli.hasOption('d')) {
        if (cli.hasOption('i')) {
          inFile = cli.getOptionValue('i');
          fis = new FileInputStream(new File(inFile));
          in = new DataInputStream(fis);
        } else {
          System.err.println("Specify input file. Example usage: hll -d -i /tmp/in.hll");
          usage(options);
          return;
        }
      }

      // return after deserialization
      if (fis != null && in != null) {
        long start = System.currentTimeMillis();
        HyperLogLog deserializedHLL = HyperLogLogUtils.deserializeHLL(in);
        long end = System.currentTimeMillis();
        System.out.println(deserializedHLL.toString());
        System.out.println("Count after deserialization: " + deserializedHLL.count());
        System.out.println("Deserialization time: " + (end - start) + " ms");
        return;
      }

      // construct hll and serialize it if required
      HyperLogLog hll = HyperLogLog.builder().enableBitPacking(bitPack).enableNoBias(noBias)
          .setEncoding(enc).setNumHashBits(hb).setNumRegisterIndexBits(p).build();

      if (br != null) {
        Set<String> hashset = new HashSet<String>();
        String line;
        while ((line = br.readLine()) != null) {
          hll.addString(line);
          hashset.add(line);
        }
        n = hashset.size();
      } else {
        Random rand = new Random(seed);
        for (int i = 0; i < n; i++) {
          if (unique < 0) {
            hll.addLong(rand.nextLong());
          } else {
            int val = rand.nextInt(unique);
            hll.addLong(val);
          }
        }
      }

      long estCount = hll.count();
      System.out.println("Actual count: " + n);
      System.out.println(hll.toString());
      System.out.println("Relative error: " + HyperLogLogUtils.getRelativeError(n, estCount) + "%");
      if (fos != null && out != null) {
        long start = System.currentTimeMillis();
        HyperLogLogUtils.serializeHLL(out, hll);
        long end = System.currentTimeMillis();
        System.out.println("Serialized hyperloglog to " + outFile);
        System.out.println("Serialized size: " + out.size() + " bytes");
        System.out.println("Serialization time: " + (end - start) + " ms");
        out.close();
      }
    } catch (ParseException e) {
      System.err.println("Invalid parameter.");
      usage(options);
    } catch (NumberFormatException e) {
      System.err.println("Invalid type for parameter.");
      usage(options);
    } catch (FileNotFoundException e) {
      System.err.println("Specified file not found.");
      usage(options);
    } catch (IOException e) {
      System.err.println("Exception occured while reading file.");
      usage(options);
    }
  }

  private static void addOptions(Options options) {
    options.addOption("p", "num-register-bits", true, "number of bits from "
        + "hashcode used as register index between 4 and 16 (both inclusive). " + "default = 14");
    options.addOption("h", "num-hash-bits", true, "number of hashcode bits. " + "default = 64");
    options.addOption("e", "encoding", true, "specify encoding to use (SPARSE "
        + "or DENSE). default = SPARSE");
    options.addOption("b", "enable-bitpacking", true, "enable bit-packing of"
        + " registers. default = true");
    options.addOption("c", "no-bias", true, "use bias correction table "
        + "(no-bias algorithm). default = true");
    options.addOption("n", "num-random-values", true, "number of random values to generate");
    options.addOption("f", "file", true, "specify file to read input data");
    options.addOption("s", "serialize", false,
        "serialize hyperloglog to file. specify -o for output file");
    options.addOption("o", "output-file", true, "specify output file for serialization");
    options.addOption("d", "deserialize", false,
        "deserialize hyperloglog from file. specify -i for input file");
    options.addOption("i", "input-file", true, "specify input file for deserialization");
  }

  static void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("HyperLogLog", options);
  }
}

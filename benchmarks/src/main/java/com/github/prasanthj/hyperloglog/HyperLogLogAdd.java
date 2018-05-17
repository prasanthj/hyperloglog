/*
 * Copyright 2014 Prasanth Jayachandran
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.prasanthj.hyperloglog;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.profile.LinuxPerfAsmProfiler;
import org.openjdk.jmh.profile.LinuxPerfNormProfiler;
import org.openjdk.jmh.profile.LinuxPerfProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.github.prasanthj.hll.HyperLogLog;

@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class HyperLogLogAdd {

  private HyperLogLog hll = HyperLogLog
    .builder()
    .setNumRegisterIndexBits(10)
    .setEncoding(HyperLogLog.EncodingType.SPARSE)
    .build();
  private org.apache.hadoop.hive.common.ndv.hll.HyperLogLog hiveHll = org.apache.hadoop.hive.common.ndv.hll
    .HyperLogLog
    .builder()
    .setNumRegisterIndexBits(10)
    .setEncoding(org.apache.hadoop.hive.common.ndv.hll.HyperLogLog.EncodingType.SPARSE)
    .build();

  private static List<Long> hashcodes;

  static {
    hashcodes = new ArrayList<>();
    Random random = new Random(123);
    for (int i = 0; i < 100; i++) {
      hashcodes.add(random.nextLong());
    }
  }

  @Benchmark
  @OperationsPerInvocation(100)
  public void testHLLAdd() {
    for (long hashcode : hashcodes) {
      hll.add(hashcode);
    }
  }

  @Benchmark
  @OperationsPerInvocation(100)
  public void testHLLAddHive() {
    for (long hashcode : hashcodes) {
      hiveHll.add(hashcode);
    }
  }

  /*
   * ============================== HOW TO RUN THIS TEST: ====================================
   *
   * You can run this test:
   *
   * a) Via the command line:
   *    $ mvn clean install
   *    $ java -jar target/benchmarks.jar HyperLogLogAdd -prof perf     -f 1 (Linux)
   *    $ java -jar target/benchmarks.jar HyperLogLogAdd -prof perfnorm -f 3 (Linux)
   *    $ java -jar target/benchmarks.jar HyperLogLogAdd -prof perfasm  -f 1 (Linux)
   *    $ java -jar target/benchmarks.jar HyperLogLogAdd -prof perf -jvmArgsAppend "-XX:AllocatePrefetchStyle=2"
   */
  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
      .include(HyperLogLogAdd.class.getSimpleName())
      .addProfiler(LinuxPerfProfiler.class)
      .addProfiler(LinuxPerfNormProfiler.class)
      .addProfiler(LinuxPerfAsmProfiler.class)
      .build();

    new Runner(opt).run();
  }
}
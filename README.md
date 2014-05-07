HyperLogLog
===========

HyperLogLog is an amazing data structure for estimating the cardinality (with very high accuracy) of large data sets that uses very little memory. This implementation of HyperLogLog contains the original algorithm by [Flajolet et. al] as well hyperloglog++ algorithm by [Heule et. al]. Refer 'References' section for blog posts/paper to find out the inner workings of hyperloglog.


Features
--------
  - Built-in support for 32-bit and 64-bit hashcodes (Murmur3_32 and Murmur3_128 respectively)
  - API support for specifying hashcode directly (instead of using internal ones)
  - SPARSE and DENSE encoding support
  - Bit-packing of DENSE registers for better compression. Serialized hyperloglog size with bitpacking is ~10KB for millions of distinct items, ~12K for few billion distinct items. When bit-packing is disabled the serialized size is ~16KB.
  - Delta encoding and varints for SPARSE registers. Serialized hyperloglog size with sparse representation is from as low as 10s of bytes (boolean column) and above.
  - Bias correction using lookup table for better accuracy
  - Command line tool (hll)
  - Configurable options to enable/disable the above features

Installation
--------------

```sh
git clone https://github.com/prasanthj/hyperloglog.git hyperloglog
cd hyperloglog
mvn package -DskipTests
```

hll - Command Line Tool
-----------------------
After running ```mvn package -DskipTests```, run ```hll``` to display the usage options
```sh
Example usage: hll -n 1000 <OR> hll -f /tmp/input.txt <OR> hll -d -i /tmp/out.hll
usage: HyperLogLog
 -b,--enable-bitpacking <arg>   enable bit-packing of registers. default =
                                true
 -c,--no-bias <arg>             use bias correction table (no-bias
                                algorithm). default = true
 -d,--deserialize               deserialize hyperloglog from file. specify
                                -i for input file
 -e,--encoding <arg>            specify encoding to use (SPARSE or DENSE).
                                default = SPARSE
 -f,--file <arg>                specify file to read input data
 -h,--num-hash-bits <arg>       number of hashcode bits. default = 64
 -i,--input-file <arg>          specify input file for deserialization
 -n,--num-random-values <arg>   number of random values to generate
 -o,--output-file <arg>         specify output file for serialization
 -p,--num-register-bits <arg>   number of bits from hashcode used as
                                register index between 4 and 16 (both
                                inclusive). default = 14
 -s,--serialize                 serialize hyperloglog to file. specify -o
                                for output file
```

Examples
--------
Test with 'n' random numbers

```
#./hll -n 20000
Actual count: 20000
Encoding: DENSE, p : 14, chosenHashBits: 128, estimatedCardinality: 19993
Relative error: 0.034999847%
```

Test with input file
```
#./hll -f /etc/passwd
Actual count: 84
Encoding: SPARSE, p : 14, chosenHashBits: 128, estimatedCardinality: 84
Relative error: 0.0%
```

Test serialization
```
#./hll -n 100000000 -s -o /tmp/out.hll
Actual count: 100000000
Encoding: DENSE, p : 14, chosenHashBits: 128, estimatedCardinality: 100069607
Relative error: -0.069606304%
Serialized hyperloglog to /tmp/out.hll
Serialized size: 10248 bytes
Serialization time: 20 ms

./hll -f /etc/passwd -s -o /tmp/out.hll
Actual count: 84
Encoding: SPARSE, p : 14, chosenHashBits: 128, estimatedCardinality: 84
Relative error: 0.0%
Serialized hyperloglog to /tmp/out.hll
Serialized size: 337 bytes
Serialization time: 5 ms
```

Test deserialization
```
#./hll -d -i /tmp/passwd.hll
Encoding: SPARSE, p : 14, chosenHashBits: 128, estimatedCardinality: 84
Count after deserialization: 84
Deserialization time: 42 ms
```

Test disabling bit-packing of registers
```
#./hll -n 10000000 -b false -s -o /tmp/out.hll
Actual count: 10000000
Encoding: DENSE, p : 14, chosenHashBits: 128, estimatedCardinality: 10052011
Relative error: -0.52011013%
Serialized hyperloglog to /tmp/out.hll
Serialized size: 16392 bytes
Serialization time: 27 ms
```
Issues
------
Bug fixes or improvements are welcome! Please fork the project and send pull request on github. Or report issues here https://github.com/prasanthj/hyperloglog/issues


License
-------

Apache licensed.

References
----------
[1] http://research.neustar.biz/2012/10/25/sketch-of-the-day-hyperloglog-cornerstone-of-a-big-data-infrastructure/

[2] http://metamarkets.com/2012/fast-cheap-and-98-right-cardinality-estimation-for-big-data/

[3] http://research.neustar.biz/tag/flajolet-martin-sketch/

[4] http://research.neustar.biz/2013/01/24/hyperloglog-googles-take-on-engineering-hll/

[5] http://antirez.com/news/75


[Flajolet et. al]:http://algo.inria.fr/flajolet/Publications/FlFuGaMe07.pdf
[Heule et. al]:http://static.googleusercontent.com/media/research.google.com/en//pubs/archive/40671.pdf

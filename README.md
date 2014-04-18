<<<<<<< HEAD
hyperloglog
===========

HyperLogLog (original and hyperloglog++) algorithm implementation in java.
=======

 * There are quite some differences in this implementation that are not specified
 * in the above algorithms
 * 1) Bitpacking - This implementation caches the maximum value of the registers
 *                 which are then used for optimal bitpacking. For example: If the
 *                 max register value is 14 only 4 bits required for register values
 *                 as opposed to a full byte (or even 6 bits as per the paper)
 * 2) Caching    - There are many places where this implementation caches often
 *                 repeated or expensive computation like max register value,
 *                 inverse power of 2, cardinality estimate etc.
 * 3) HLL++ uses int[] for sparse list that is compressed and delta encoded. This
 *    implementation uses fastutils primitive sorted maps for lesser memory
 *    footprint and easy of implementation.
 * 4) Uses Guava library's fast hash functions (Murmur3_32 or Murmur3_128).
 *    There is a separate API for specifying hashcode directly if other external
 *    hash functions are used.
 *    
>>>>>>> code cleanup and added comments

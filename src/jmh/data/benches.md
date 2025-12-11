# JMH Benchmarks

## Platform threads

```
Benchmark                        (nbActors)  (nbMessages)   Mode  Cnt   Score    Error  Units
(...)
BenchmarkPlatformTest.benchmark      100000        100000  thrpt    4  37.962 ±  3.699  ops/s
BenchmarkPlatformTest.benchmark      100000        200000  thrpt    4  21.039 ±  2.693  ops/s
BenchmarkPlatformTest.benchmark      100000        300000  thrpt    4  13.556 ±  3.096  ops/s
BenchmarkPlatformTest.benchmark      100000        400000  thrpt    4   9.129 ±  2.165  ops/s
BenchmarkPlatformTest.benchmark      100000        500000  thrpt    4   7.879 ±  6.246  ops/s
BenchmarkPlatformTest.benchmark      100000        600000  thrpt    4   5.909 ±  1.286  ops/s
BenchmarkPlatformTest.benchmark      100000        700000  thrpt    4   5.384 ±  2.106  ops/s
BenchmarkPlatformTest.benchmark      100000        800000  thrpt    4   3.845 ±  3.607  ops/s
BenchmarkPlatformTest.benchmark      100000        900000  thrpt    4   3.751 ±  1.712  ops/s
BenchmarkPlatformTest.benchmark      100000       1000000  thrpt    4   3.628 ±  2.324  ops/s
```

## Virtual threads

```
Benchmark                        (nbActors)  (nbMessages)   Mode  Cnt   Score    Error  Units
(...)
BenchmarkVirtualTest.benchmark       100000        100000  thrpt    4  14.844 ±  5.944  ops/s
BenchmarkVirtualTest.benchmark       100000        200000  thrpt    4   7.908 ±  0.346  ops/s
BenchmarkVirtualTest.benchmark       100000        300000  thrpt    4   4.763 ±  1.030  ops/s
BenchmarkVirtualTest.benchmark       100000        400000  thrpt    4   4.066 ±  0.504  ops/s
BenchmarkVirtualTest.benchmark       100000        500000  thrpt    4   2.807 ±  0.240  ops/s
BenchmarkVirtualTest.benchmark       100000        600000  thrpt    4   2.485 ±  1.110  ops/s
BenchmarkVirtualTest.benchmark       100000        700000  thrpt    4   2.133 ±  0.538  ops/s
BenchmarkVirtualTest.benchmark       100000        800000  thrpt    4   2.009 ±  0.564  ops/s
BenchmarkVirtualTest.benchmark       100000        900000  thrpt    4   1.706 ±  0.530  ops/s
BenchmarkVirtualTest.benchmark       100000       1000000  thrpt    4   1.487 ±  0.730  ops/s
```

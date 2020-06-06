## ZIO Arrow benchmark results

`ZArrow` was benchmarked on the following setup:

## Hardware

* CPU: i7-6700HQ CPU @ 2.60GHz 
* Memory: DDR4 64GB
* SSD Drive 

## Software
* Ubuntu: 19.10.1 64Bit
* Kernel: 5.3.0-40-generic
* Java: OpenJDK 11.0.6
* Compiler: Scala 2.13.1
* JMH : 1.21
* G1 default garbage collector

## API Benchmark Results ( by method, the higher the better )
### note: CPU-Memory iteraction, memory bound
```bash
[info] Benchmark       Mode         Score (ops/s)
[info] asEffect        thrpt        39291338.197    
[info] choice          thrpt        82481642.103    
[info] compose         thrpt        76284520.477    
[info] endThen         thrpt        83402268.332    
[info] first           thrpt        68216801.935    
[info] id              thrpt       117017373.778    
[info] left            thrpt        98232953.255    
[info] lift            thrpt       219555267.469    
[info] merge           thrpt        25538627.153    
[info] right           thrpt        95448060.160    
[info] second          thrpt        66618998.459    
[info] split           thrpt        83030147.647    
[info] test            thrpt        18945081.040    
[info] zipWith         thrpt        83923062.648    
```

## SocketIO Benchmark Results (the higher the better)
### note: CPU-Memory-Disk iteraction, IO bound, CPU bound, balanced

### Test Description
A script creates a number of files, each holding a single random `Long` value from the specified range.<br>
Each worker gets a file name on input, reads a seed from there and computes a `factorial` for that seed<br>
The final result is the `sum` of all list values

This test emphasizes on real-world datacenter scenario, which is when an application is all memory, CPU and IO bound

### Test setup
* 10 Workers 
* Factorial seed range: 8..12
* Balanced approach with comparable IO and Memory load
* 2 CPU threads

```bash
[info] Benchmark       Mode        Score (ops/s)
[info] plainBench      thrpt       28813.775
[info] arrowBench      thrpt       28221.561
[info] zioBench        thrpt       27416.637
```

## Compute Benchmark Results (the higher the better)
### note: CPU-Memory iteraction, CPU/Memory bound

### Test Description
A list of random `Long` numbers from a specific range is generated. For each value a `factorial` is computed.<br>
We build a sequential computation, which creates a separate worker for each value, evaluates a result and stores its output in a list.<br>
The final result is the `sum` of all list values

This test emphasizes on long sequential computation chains, which may be a use case for a deep datacenter application pipeline.


### Test setup
* 500 Workers 
* Factorial seed range: 8..12
* Sequential computation: Each worker linearly gets a `Long` seed from a list, computes
* 2 CPU threads

```bash
[info] Benchmark       Mode        Score (ops/s)  
[info] plainBench      thrpt       61867.754
[info] zioBench        thrpt       9177.142
[info] arrowBench      thrpt       96310.372
```

We recognize a **10x+ improvement** for `ZIO Arrow` over `ZIO Monad` in this test. This can be explained in a drastical reduce of memory allocations and polymorphic dispatch events on `JVM`.

This emphasizes a poor performance of `Monad` computation due to excessive allocations and highlights an `Arrow` compositional semantics for the purpose of performance for the `JVM` ecosystem
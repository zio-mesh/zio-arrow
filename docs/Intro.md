# Welcome to ZIO Arrow 

## Introduction

### What is an Arrow ?
`Arrow[A,B]` is a data structure that efficiently models a notion of `Process` with an input channel of type `A` and an output channel of type `B`. <br>

The notation of "Process from A to B" is extremely generic, and indeed `Arrow` is a generalization for `Applicative`, `Monad`, `Stream` and many others

### What is ZIO Arrow?
`ZIO-Arrow` or `ZArrow` is a high-performance composition effect for ZIO Ecosystem. <br>
`ZArrow` is an implementation of Hughes[1,2] and Paterson[3] `Arrow` structure for ZIO Effects. 

`ZArrow` delivers three main capabilities:

* High Performance - `ZArrow` exploits `JVM` internals to dramatically decrease the number of allocations and dispatches, yielding an unprecedental runtime performance.

* Abstract interface - `Arrow` is a more abstract data type, than ZIO Monad. It's more abstract than ZIO Streams. In a nutshell, `ZArrow` allows function-like interface which can have both different input and different outputs. 

* Easy interop - `ZArrow` can both input and output `ZIO Monad` and `ZIO Stream`, simplifying application development with different ZIO Effect types

### ZIO Arrow API Vocalbuary
```bash
map   - map
>>=   - flatMap
<<<   - compose 
>>>   - andThen
<*>   - zipWith
***   - split 
&&&   - merge
|||   - choice 
lift  - lifts a function to ZArrow
liftM - lifts a `ZIO` effect to `ZArrow`
first - creates a tuple from input, applies a function to the first element
second- creates a tuple from input, applies a function to the second element
left  - returns a value as an `Either Left`
right - returns a value as an `Either Left`
fst   - returns the first element of the tuple
snd   - returns the second element of the tuple
unit  - Maps the output of this effectful function to `Unit`.
as    - map to constant
asEffect - yield an effect
```
For more information about API methods, consult online [References](References.md)

## Programming Examples
Available [here](examples/Basic.md)

## Benchmarks
Available [here](bench/Bench.md)

## References
Available [here](References.md)


# dinner-table

Solution to HW1 for CS541 Artificial Intelligence 

**Author:** Cesar Pinera

**Email:** cesar.pinera@gmail.com

## Language

This project has been written in Clojure, and uses Leiningen as the build system. 

## Requirements

- A running JVM 1.7 or newer. It may run on 1.6, but that is untested.
- [Leiningen](http://leiningen.org) 2.x. Please see the [Leiningen](http://leiningen.org) site for instructions on how to install it. 
- Linux or Mac OS X. Tested with the later. 

## Usage

### Full run

Run the generate.sh shell script (e.g. ./generate.sh). It will invoke the build system, which will download all dependencies as needed, including Clojure itself, and will produce the solution files for the homework in the solution subdirectory. 

### Single problem run

In order to find a solution for a single problem, use the run facility in leiningen:

  ````
lein run <input file> <output file> <seconds>
  
  ````
The program will run for the specified number of seconds (plus whatever compilation time and JVM start up time is needed). The solvers are time-limited, so even if the loading process takes time, it is not counted towards the solution time. 


## Solution

In the file src/dinner_table/solver.clj a number of algorithms have been implemented. There are comment sections that will allow you to test them if so desired. The algorithms implemented included:

- Simulated Anneling
- Greedy
- Greedy + Placing the people with the most negative accumulated preference score (i.e. the haters) in each of the table corners (a.k.a. Haters First)
- Random
- Random + Haters First
- Complete Search

The best solver so far has been Simulated Anneling for problems with over 10 people. Random does well in the smallest problem (10 people), but becomes increasingly bad as the number of people increases. In any case, two simulataneous threads are run for each problem, one with the simulated anneling implementation and the second one with random sitting, and the best one is chosen. Of course, this requires a multi-core CPU in order to stay within the time limits, which I have assumed to be a reasonable assumption to make. 

## Testing

The scoring function has been tested against the solution provided by Bart, and it correctly scores it to the expected value of 100.

Complete search correctly returns a 100 solution for the first problem. 

## Code repository

The source repository can be found in [Github](https://github.com/cesarpinera/dinner-table).

## License

This is free and unencumbered software released into the public domain.

Anyone is free to copy, modify, publish, use, compile, sell, or
distribute this software, either in source code form or as a compiled
binary, for any purpose, commercial or non-commercial, and by any
means.

In jurisdictions that recognize copyright laws, the author or authors
of this software dedicate any and all copyright interest in the
software to the public domain. We make this dedication for the benefit
of the public at large and to the detriment of our heirs and
successors. We intend this dedication to be an overt act of
relinquishment in perpetuity of all present and future rights to this
software under copyright law.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

For more information, please refer to <http://unlicense.org/>


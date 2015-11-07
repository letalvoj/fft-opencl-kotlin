# Equalizer writen in Kotlin using OpenCL

I always wanted to write something in CUDA or OpenCL.
Recently I came across the Aparapi from AMD, which allows you to write OpenCL code in pure Java (with some restrictions of course).
I could not resist and wrote Fast Fourier Transformation algorithm using the API.

To demonstrate the FFT I also wrote a small JavaFX app in Kotlin.
The app listens to your microphone and then shows you the frequency spectrum in real time.

## How to run it

**Run with VM params**: -Djava.library.path=./native  -Dcom.amd.aparapi.executionMode=%1

Otherwise the Aparapi will fall back to CPU mode!

## Performance comparison

I tested the performance of the implemented algorithm on my Macbook Pro with Intel Iris Pro graphics. The overhead of copying the data and executing the kernel seems to be in order of miliseconds.
On the following image you can see that with number of samples below 16k the CPU is faster, but with millions of samples the GPU is more than 10x faster.

<img src='./measurements/comparison.png'/>

You do not actually need to calculate FFTs with millions of samples.
To gain great performance improvements the FFTs can be simply calculated on GPU in batches.

## Licence MIT

Copyright (c) 2015 Vojtech Letal

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
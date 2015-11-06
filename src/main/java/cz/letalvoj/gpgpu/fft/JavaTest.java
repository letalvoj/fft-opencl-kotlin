package cz.letalvoj.gpgpu.fft;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;

import java.util.Arrays;


public class JavaTest {

    public static void main(String[] _args) {
        //TODO clean up the code and add JUnit test - just to be sure

        final int passes = 3;
        final int size = 8;

        float[] inputRe = new float[size];
        float[] inputIm = new float[size];
        float[] outputRe = new float[size];
        float[] outputIm = new float[size];

        for (int i = 0; i < size; i++) {
            inputRe[i] = i;
        }

        Kernel kernel = new FFTKernel(inputRe, inputIm, outputRe, outputIm, size);
        kernel.execute(Range.create(size), passes);

        System.out.println("inputRe" + Arrays.toString(inputRe));
        System.out.println("inputIm" + Arrays.toString(inputIm));
        System.out.println("outputRe" + Arrays.toString(outputRe));
        System.out.println("outputIm" + Arrays.toString(outputIm));


        kernel.dispose();
    }

}

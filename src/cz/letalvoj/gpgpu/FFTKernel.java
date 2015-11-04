package cz.letalvoj.gpgpu;

import com.amd.aparapi.Kernel;

/**
 * This is super non-optimal because of the lack of C-like low level structures in Java. On the other hand I really do
 * not care, because mostly wanned to implement something interesting using Java + OpenCL.
 */
public class FFTKernel extends Kernel {

    final float[] inputRe, inputIm, outputRe, outputIm;
    final float[] weightsIm, weightsRe;
    final int N;

    public FFTKernel(float[] inputRe, float[] inputIm, float[] outputRe, float[] outputIm, int N) {
        this.inputRe = inputRe;
        this.inputIm = inputIm;
        this.outputRe = outputRe;
        this.outputIm = outputIm;
        this.N = N;
        checkLengths();

        this.weightsIm = new float[N / 2];
        this.weightsRe = new float[N / 2];
        initWeights();
    }

    private void checkLengths() {
        if (Integer.highestOneBit(N) != N)
            throw new RuntimeException("N is not a power of 2: " + N);

        if (inputRe.length != N || inputIm.length != N || outputRe.length != N || outputIm.length != N)
            throw new RuntimeException("Expected all input arrays to have length " + N);
    }

    private void initWeights() {
        for (int i = 0; i < N / 2; i++) {
            double angle = -2 * i * Math.PI / N;

            weightsRe[i] = (float) Math.cos(angle);
            weightsIm[i] = (float) Math.sin(angle);
        }
    }

    @Override
    public void run() {
        int pass = getPassId();
        int gId = getPassId();

        if (pass == 0) {
            bitReversePermutation(gId);
        } else if (pass % 2 == 1) {
            fftIteration(pass - 1, gId, inputRe, inputIm, outputRe, outputIm);
        } else {
            fftIteration(pass - 1, gId, outputRe, outputIm, inputRe, inputIm);
        }
    }

    private void bitReversePermutation(int gId) {
        int shift = 1 + Integer.numberOfLeadingZeros(N);
        int newId = Integer.reverse(gId) >>> shift;

        inputRe[gId] = outputRe[newId];
        inputIm[gId] = outputIm[newId];
    }


    public void fftIteration(int pass, int gId, float[] inRe, float[] inIm, float[] outRe, float[] outIm) {
        int passBit = 1 << pass;
        int fftWidth = passBit * 2;

        int first = gId & (~passBit);
        int second = gId | passBit;
        int signum = (gId == first) ? 1 : -1;

        int butterflyNumber = pass == 0 ? 0 : gId % (fftWidth / 2);
        int weightIndex = (butterflyNumber * N) / fftWidth;

        outRe[gId] = inRe[second] * weightsRe[weightIndex] - inIm[second] * weightsIm[weightIndex];
        outIm[gId] = inRe[second] * weightsIm[weightIndex] + inIm[second] * weightsRe[weightIndex];

        outRe[gId] *= signum;
        outIm[gId] *= signum;

        outRe[gId] += inRe[first];
        outIm[gId] += inIm[first];
    }

}

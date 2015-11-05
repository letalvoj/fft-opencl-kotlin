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
        this.weightsIm = new float[N / 2];
        this.weightsRe = new float[N / 2];
        this.N = N;

        checkLengths();
        reverse(inputRe);
        reverse(inputIm);

        initWeights();
    }

    private void reverse(float[] x) {
        float tmp;
        for (int k = 0; k < N; k++) {
            int shift = 1 + Integer.numberOfLeadingZeros(N);
            int j = Integer.reverse(k) >>> shift;

            if (j > k) {
                tmp = x[j];
                x[j] = x[k];
                x[k] = tmp;
            }
        }
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
        int gId = getGlobalId();

        if (pass % 2 == 0) {
            fftIteration(pass, gId, inputRe, inputIm, outputRe, outputIm);
        } else {
            fftIteration(pass, gId, outputRe, outputIm, inputRe, inputIm);
        }
    }

    public void fftIteration(int pass, int gId, float[] inRe, float[] inIm, float[] outRe, float[] outIm) {
        int passBit = 1 << pass;
        int fftWidth = passBit * 2;

        int first = gId & (~passBit);
        int second = gId | passBit;
        int signum = (gId == first) ? 1 : -1;

        int butterflyNumber = pass == 0 ? 0 : gId % (fftWidth / 2);
        int weightIndex = (butterflyNumber * N) / fftWidth;

        int kthButterly = pass == 0 ? 0 : gId % (fftWidth / 2);
        float kthAngle = -2 * kthButterly * ((float) Math.PI) / fftWidth;

        //TODO use the lookup table
        float wRe = this.cos(kthAngle);
        float wIm = this.sin(kthAngle);

        outRe[gId] = inRe[second] * wRe - inIm[second] * wIm;
        outIm[gId] = inRe[second] * wIm + inIm[second] * wRe;

        outRe[gId] *= signum;
        outIm[gId] *= signum;

        outRe[gId] += inRe[first];
        outIm[gId] += inIm[first];
    }

}

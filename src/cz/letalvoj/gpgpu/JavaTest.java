package cz.letalvoj.gpgpu;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;


public class JavaTest {

    public static void main(String[] _args) {

        final int passes = 2;
        final int size = 16;

        final float[] a = new float[size];
        final float[] b = new float[size];

        Kernel kernel = new Kernel() {

            @Override
            public void run() {
                int passId = getPassId();
                int gid = getGlobalId();

                a[gid] = gid;

            }

        };

        kernel.execute(Range.create(size), passes);

        for (int i = 0; i < size; i++) {
            System.out.printf("%6.2f + %6.2f = %8.2f\n", a[i], b[i], 1.0);
        }

        kernel.dispose();
    }

}

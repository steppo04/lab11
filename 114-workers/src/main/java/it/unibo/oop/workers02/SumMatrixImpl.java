package it.unibo.oop.workers02;

import java.util.ArrayList;
import java.util.List;
/**
 * implementazione intefaccia SumMatrix.
 */
public class SumMatrixImpl implements SumMatrix {
private final int nthread;
    /**
     * 
     * @param nthread
     *            no. of thread performing the sum.
     */
    public SumMatrixImpl(final int nthread) {
        this.nthread = nthread;
    }

    private static class Worker extends Thread {
        private final int nelem;
        private final double[][] matrix;
        private final int startpos;
        private double res;

        /**
         * Build a new worker.
         * 
         * @param matrix
         *            the matrix to sum
         * @param startpos
         *            the initial position for this worker
         * @param nelem
         *            the no. of elems to sum up for this worker
         */
        Worker(final double[][] matrix, final int startpos, final int nelem) {     
            super();
            this.matrix = matrix;
            this.startpos = startpos;
            this.nelem = nelem;
        }

        @Override
        @SuppressWarnings("PMD.SystemPrintln")
        public void run() {
            System.out.println("Working from position " + startpos + " to position " + (startpos + nelem - 1));
                    for (int i = startpos; i < matrix.length && i < startpos + nelem; i++) {
                        for (final double elem : this.matrix[i]) {
                        this.res += elem;
                    }
                }
            }
        /**
         * Returns the result of summing up the integers within the list.
         * 
         * @return the sum of every element in the array
         */
        public double getResult() {
            return this.res;
        }

    }

@Override
    public double sum(final double[][] matrix) {
        final int size = matrix.length % nthread + matrix.length / nthread;
        /*
         * Build a list of workers
         */
        final List<Worker> workers = new ArrayList<>(nthread);
        for (int start = 0; start < matrix.length; start += size) {
            workers.add(new Worker(matrix, start, size));
        }
        /*
         * Start them
         */
        for (final Worker w: workers) {
            w.start();
        }
        /*
         * Wait for every one of them to finish. This operation is _way_ better done by
         * using barriers and latches, and the whole operation would be better done with
         * futures.
         */
        long sum = 0;
        for (final Worker w: workers) {
            try {
                w.join();
                sum += w.getResult();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        /*
         * Return the sum
         */
        return sum;
    }
}

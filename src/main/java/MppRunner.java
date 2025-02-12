import java.util.HashMap;
import java.util.Map;

public class MppRunner {
    private static final KiWiMap store = new KiWiMap();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting putMany() performance and correctness tests...");

        testPutPerformance();
        testPutManyPerformance();
        testPutManyCorrectness();
        testPutManyConcurrent();
        testPutManyConcurrentPerformance();
    }

    public static void testPutPerformance() {
        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            store.put(i, i);
        }
        long end = System.nanoTime();

        System.out.println("Single put() 1000 times: " + (end - start) / 1_000_000.0 + " ms");
    }

    public static void testPutManyPerformance() {
        Map<Integer, Integer> batch = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            batch.put(i, i);
        }

        long start = System.nanoTime();
        store.putMany(batch);
        long end = System.nanoTime();

        System.out.println("Batch putMany() time: " + (end - start) / 1_000_000.0 + " ms");
    }

    public static void testPutManyCorrectness() {
        Map<Integer, Integer> batch = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            batch.put(i, i);
        }

        store.putMany(batch);

        boolean allCorrect = true;
        for (int i = 0; i < 1000; i++) {
            if (i != store.get(i)) {
                System.out.println("\nKey " + i + " is missing or incorrect!");
                allCorrect = false;
                break;
            }
        }

        if (allCorrect) {
            System.out.println("\nAll inserted keys exist. There is no data loss.");
        }
    }

    public static void testPutManyConcurrent() throws InterruptedException {
        Map<Integer, Integer> batch1 = new HashMap<>();
        Map<Integer, Integer> batch2 = new HashMap<>();

        for (int i = 0; i < 500; i++) {
            batch1.put(i, i);
            batch2.put(i + 500, i + 500);
        }

        Thread t1 = new Thread(() -> store.putMany(batch1));
        Thread t2 = new Thread(() -> store.putMany(batch2));

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        boolean allCorrect = true;
        for (int i = 0; i < 1000; i++) {
            if (i != (store.get(i))) {
                System.out.println("\nConcurrent insert failed for key " + i);
                allCorrect = false;
                break;
            }
        }

        if (allCorrect) {
            System.out.println("Concurrent test passed.\n");
        }
    }

    public static void testPutManyConcurrentPerformance() throws InterruptedException {
        int[] threadCounts = {1, 2, 4, 8, 16, 32, 40};

        for (int numThreads : threadCounts) {
            Map<Integer, Integer> batch = new HashMap<>();
            for (int i = 0; i < 6000; i++) {
                batch.put(i, i);
            }

            Thread[] threads = new Thread[numThreads];
            long start = System.nanoTime();

            for (int t = 0; t < numThreads; t++) {
                threads[t] = new Thread(() -> store.putMany(batch));
                threads[t].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            long end = System.nanoTime();
            System.out.println("putMany() time with " + numThreads + " threads: " + (end - start) / 1_000_000.0 + " ms");
        }
    }
}

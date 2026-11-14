package Q6;

/**
 * Demonstrates the flaw when changing 'while' to 'if' in wait() conditions.
 * With multiple consumer threads, when cook produces 1 item and wakes all threads,
 * both consumers wake up. If using 'if', both proceed to decrement count and take,
 * causing underflow, corrupted data, or ArrayIndexOutOfBoundsException.
 */
public class TokenCounterIfBug {
    private final int[] buffer = new int[5];
    private int count = 0, in = 0, out = 0;

    public synchronized void put(int token) throws InterruptedException {
        // BUG: using 'if' instead of 'while'
        if (count == buffer.length) {
            wait();
        }
        buffer[in] = token;
        in = (in + 1) % buffer.length;
        count++;
        notifyAll();
    }

    public synchronized int take() throws InterruptedException {
        // BUG: using 'if' instead of 'while'
        if (count == 0) {
            wait();
        }
        int token = buffer[out];
        out = (out + 1) % buffer.length;
        count--;
        notifyAll();
        return token;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Testing TokenCounter with IF instead of WHILE ===");
        for (int trial = 1; trial <= 5; trial++) {
            final int currentTrial = trial;
            TokenCounterIfBug counter = new TokenCounterIfBug();
            int[] collected = new int[4];
            Thread[] cooks = new Thread[2];
            Thread[] students = new Thread[4];

            // 2 cooks produce 10 tokens each = 20 tokens
            for (int c = 0; c < 2; c++) {
                final int cookId = c;
                cooks[c] = new Thread(() -> {
                    try {
                        for (int t = 1; t <= 10; t++) counter.put(cookId * 100 + t);
                    } catch (InterruptedException ignored) {}
                });
            }

            // 4 students take 5 tokens each = 20 tokens
            for (int s = 0; s < 4; s++) {
                final int studentId = s;
                students[s] = new Thread(() -> {
                    try {
                        for (int k = 0; k < 5; k++) {
                            collected[studentId] += counter.take();
                        }
                    } catch (Exception e) {
                        System.err.printf("[Trial #%d Exception in Student %d]: %s%n", currentTrial, studentId, e.toString());
                    }
                });
            }

            for (Thread c : cooks) c.start();
            for (Thread s : students) s.start();

            for (Thread c : cooks) c.join(200);
            for (Thread s : students) s.join(200);

            int total = 0;
            for (int v : collected) total += v;
            System.out.printf("Trial #%d: Final buffer count = %d, Total tokens sum = %d%n",
                    currentTrial, counter.count, total);
        }
    }
}

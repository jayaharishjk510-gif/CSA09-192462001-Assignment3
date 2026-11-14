package Q5;

/**
 * Demonstrates the unsynchronized race condition over 10 runs.
 */
public class SeatBookingUnsyncTest {

    static class UnsyncSeatBooking {
        private final boolean[] booked;
        private int bookingsMade = 0;

        UnsyncSeatBooking(int seats) { booked = new boolean[seats]; }

        // WITHOUT synchronized keyword
        public boolean book(int seat) {
            if (!booked[seat]) {
                try { Thread.sleep(1); } catch (InterruptedException ignored) {}
                booked[seat] = true;
                bookingsMade++;
                return true;
            }
            return false;
        }

        public int getBookingsMade() { return bookingsMade; }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 10 Consecutive Runs WITHOUT Synchronization ===");
        for (int run = 1; run <= 10; run++) {
            UnsyncSeatBooking screen = new UnsyncSeatBooking(10);
            Thread[] users = new Thread[100];
            for (int i = 0; i < users.length; i++) {
                final int seat = i % 10;
                users[i] = new Thread(() -> screen.book(seat));
                users[i].start();
            }
            for (Thread t : users) t.join();
            System.out.printf("Run #%2d -> Seats Sold: %2d (Expected max: 10, Double bookings: %2d)%n",
                    run, screen.getBookingsMade(), screen.getBookingsMade() - 10);
        }
    }
}


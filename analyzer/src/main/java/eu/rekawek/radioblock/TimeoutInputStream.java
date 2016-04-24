package eu.rekawek.radioblock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class TimeoutInputStream extends InputStream {

    private enum State {
        IDLE, WAITING_FOR_BYTE, RECEIVED_RESULT, SHUTDOWN
    }

    private static final Logger LOG = LoggerFactory.getLogger(TimeoutInputStream.class);

    private final InputStream is;

    private final long timeoutMillis;

    private StateMachine<State> machine;

    private Either<Integer, IOException> result;

    public TimeoutInputStream(InputStream is, final int timeoutMillis) {
        this.is = is;
        this.timeoutMillis = timeoutMillis;
        this.machine = new StateMachine<State>(State.IDLE);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mainLoop();
                } catch (Exception e) {
                    LOG.error("Exception in the TimeoutInputStream loop", e);
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void mainLoop() throws InterruptedException, IOException {
        while (machine.waitUntil(State.WAITING_FOR_BYTE, State.SHUTDOWN) != State.SHUTDOWN) {
            try {
                result = Either.left(is.read());
            } catch (IOException e) {
                result = Either.<Integer, IOException>right(e);
            } finally {
                machine.setState(State.RECEIVED_RESULT);
            }
        }
    }

    @Override
    public int read() throws IOException {
        machine.setState(State.WAITING_FOR_BYTE);
        try {
            if (machine.waitUntil(State.RECEIVED_RESULT, timeoutMillis)) {
                if (result.isLeft()) {
                    return result.getLeft();
                } else {
                    machine.setState(State.SHUTDOWN);
                    throw result.getRight();
                }
            } else {
                machine.setState(State.SHUTDOWN);
                throw new IOException("Timeout: " + timeoutMillis);
            }
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
   }

    @Override
    public void close() throws IOException {
        is.close();
    }

    public static class Either<L, R> {

        private final L left;

        private final R right;

        private Either(L left, R right) {
            if (left != null && right != null) {
                throw new IllegalArgumentException();
            }
            if (left == null && right == null) {
                throw new IllegalArgumentException();
            }
            this.left = left;
            this.right = right;
        }

        public static <L, R> Either<L, R> left(L left) {
            return new Either(left, null);
        }

        public static <L, R> Either right(R right) {
            return new Either(null, right);
        }

        public boolean isLeft() {
            return left != null;
        }

        public boolean isRight() {
            return right != null;
        }

        public L getLeft() {
            return left;
        }

        public R getRight() {
            return right;
        }
    }

    public static class StateMachine<E extends Enum> {

        private volatile E state;

        public StateMachine(E initialState) {
            this.state = initialState;
        }

        public synchronized E setState(E newState) {
            E oldState = this.state;
            this.state = newState;
            notifyAll();
            return oldState;
        }

        public synchronized E waitUntil(E s1, E s2) throws InterruptedException {
            while (this.state != s1 && this.state != s2) {
                wait();
            }
            return this.state;
        }

        public synchronized boolean waitUntil(E state, long timeoutMillis) throws InterruptedException {
            long waitUntil = System.currentTimeMillis() + timeoutMillis;
            while (this.state != state) {
                long now = System.currentTimeMillis();
                if (now > waitUntil) {
                    return false;
                }
                wait(waitUntil - now);
            }
            return true;
        }
    }
}

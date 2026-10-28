package service;

public class TransactionBuffer {

    private final TransactionRequest[] buffer;
    private int count;

    public TransactionBuffer(int capacity) {
        this.buffer = new TransactionRequest[capacity];
    }

    public synchronized void produce(TransactionRequest request) throws InterruptedException {
        while (count == buffer.length) {
            wait();
        }
        buffer[count] = request;
        count++;
        notifyAll();
    }

    public synchronized TransactionRequest consume() throws InterruptedException {
        while (count == 0) {
            wait();
        }
        TransactionRequest request = buffer[0];
        for (int i = 1; i < count; i++) {
            buffer[i - 1] = buffer[i];
        }
        count--;
        buffer[count] = null;
        notifyAll();
        return request;
    }
}

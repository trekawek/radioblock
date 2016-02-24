package eu.rekawek.radioblock;

import java.util.Iterator;

public class WrappedArrayIterator implements Iterator<Short> {

    private final short[] buffer;

    private final int start;

    private int index;

    public WrappedArrayIterator(short[] buffer, int start) {
        this.buffer = buffer;
        this.start = start;
        this.index = start;
    }

    @Override
    public boolean hasNext() {
        return index != -1;
    }

    @Override
    public Short next() {
        short val = buffer[index];
        index++;
        if (index == buffer.length) {
            index = 0;
        }
        if (index == start) {
            index = -1;
        }
        return val;
    }
}
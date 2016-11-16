package eu.rekawek.analyzer.channel;

import eu.rekawek.analyzer.sample.Waveform;
import eu.rekawek.analyzer.sample.WaveformVector;

import java.util.Iterator;

public enum MultiplexingStrategy {
    LEFT {
        @Override
        public WaveformVector getWaveformVector(Iterator<Short> buffer, int channels, int size) {
            return new WaveformVector(Waveform.createFromSignedShort(new ChannelIterator(buffer, channels, 0), size));
        }
    }, RIGHT {
        @Override
        public WaveformVector getWaveformVector(Iterator<Short> buffer, int channels, int size) {
            return new WaveformVector(Waveform.createFromSignedShort(new ChannelIterator(buffer, channels, 1), size));
        }
    }, AVERAGE {
        @Override
        public WaveformVector getWaveformVector(Iterator<Short> buffer, int channels, int size) {
            return new WaveformVector(Waveform.createFromSignedShort(new CombinedChannelIterator(buffer, channels), size));
        }
    }, SEPARATE {
        @Override
        public WaveformVector getWaveformVector(Iterator<Short> buffer, int channels, int size) {
            return new WaveformVector(Waveform.createFromSignedShort(buffer, channels, size));
        }
    };

    public abstract WaveformVector getWaveformVector(Iterator<Short> buffer, int channels, int size);
}

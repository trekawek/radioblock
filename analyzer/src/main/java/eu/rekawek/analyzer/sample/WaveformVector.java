package eu.rekawek.analyzer.sample;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

public class WaveformVector {

    private Waveform[] waveforms;

    public WaveformVector(Waveform waveform) {
        this.waveforms = new Waveform[] {waveform};
    }

    public WaveformVector(Waveform[] waveforms) {
        this.waveforms = waveforms;
    }

    public WaveformVector doFft() {
        Arrays.stream(waveforms).forEach(Waveform::doFft);
        return this;
    }

    public WaveformVector doIfft() {
        Arrays.stream(waveforms).forEach(Waveform::doIfft);
        return this;
    }

    public Float[] getMaxReal(int range) {
        return Arrays.stream(waveforms).map(w -> w.getMaxReal(range)).collect(toList()).toArray(new Float[waveforms.length]);
    }

    public WaveformVector doConjAndMultiply(WaveformVector other) {
        checkArgument(this.waveforms.length == other.waveforms.length);
        for (int i = 0; i < waveforms.length; i++) {
            waveforms[i].doConjAndMultiply(other.waveforms[i]);
        }
        return this;
    }
}
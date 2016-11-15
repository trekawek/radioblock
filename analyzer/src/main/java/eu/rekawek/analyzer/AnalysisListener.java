package eu.rekawek.analyzer;

public interface AnalysisListener {

    default void analysisInProgress(String expectedId, int expectedJingleIndex, int[] levels) {
    }

    void gotJingle(String id, int expectedJingleIndex, int[] levels);

}

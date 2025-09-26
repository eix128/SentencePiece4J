package com.sentencepiece;

public class SegmentEnd {
    private final TokenType type;
    private final int id;
    private final float pathScoreSum;
    private final int pathSegmentCount;
    private final int segmentStart;

    public SegmentEnd(TokenType type, int id, float pathScoreSum, int pathSegmentCount, int segmentStart) {
        this.type = type;
        this.id = id;
        this.pathScoreSum = pathScoreSum;
        this.pathSegmentCount = pathSegmentCount;
        this.segmentStart = segmentStart;
    }

    public float score(Scoring scoring) {
        switch (scoring) {
            case FEWEST_SEGMENTS:
                return 1f / pathSegmentCount * 10_000_000 + pathScoreSum;
            case HIGHEST_SCORE:
                return pathScoreSum;
            default:
                throw new IllegalArgumentException("Unknown scoring " + scoring);
        }
    }

    public float scoreWith(Scoring scoring, float additionalSegmentScore) {
        switch (scoring) {
            case FEWEST_SEGMENTS:
                return 1f / (pathSegmentCount + 1) * 10_000_000 + (pathScoreSum + additionalSegmentScore);
            case HIGHEST_SCORE:
                return pathScoreSum + additionalSegmentScore;
            default:
                throw new IllegalArgumentException("Unknown scoring " + scoring);
        }
    }

    public int getId() {
        return id;
    }

    public float getPathScoreSum() {
        return pathScoreSum;
    }

    public int getPathSegmentCount() {
        return pathSegmentCount;
    }

    public int getSegmentStart() {
        return segmentStart;
    }
}
package com.sentencepiece;

import java.util.ArrayList;
import java.util.List;

public class ResultBuilderImpl implements SentencePieceAlgorithm.ResultBuilder {
    private final List<Integer> tokenIds = new ArrayList<>();

    @Override
    public void build(String input, SegmentEnd[] segmentEnds, boolean collapseUnknowns) {
        int i = segmentEnds.length - 1;
        while (i > 0) {
            SegmentEnd end = segmentEnds[i];
            tokenIds.add(0, end.getId());
            i = end.getSegmentStart();
        }
    }

    public List<Integer> getTokenIds() {
        return tokenIds;
    }
}
package com.sentencepiece;

public class SentencePieceAlgorithm {


    private final boolean collapseUnknowns;
    private final Scoring scoring;

    public SentencePieceAlgorithm(boolean collapseUnknowns, Scoring scoring) {
        this.collapseUnknowns = collapseUnknowns;
        this.scoring = scoring;
    }

    public void segment(String input, ResultBuilder resultBuilder, Model model) {
        SegmentEnd[] segmentEnds = new SegmentEnd[input.length() + 1];
        segmentEnds[0] = new SegmentEnd(TokenType.UNKNOWN, 0, 0, 0, 0);

        int start = 0;
        while (start < input.length()) {
            TrieNode node = model.getRoot();
            int pos = start;
            while (node != null && pos < input.length()) {
                int cp = input.codePointAt(pos);
                pos += Character.charCount(cp);
                node = node.child(cp);
                int length = pos - start;
                if (node != null && node.isToken() && node.getType() != TokenType.UNUSED) {
                    float score = (node.getType() == TokenType.USER_DEFINED)
                            ? (length * model.getMaxScore() - 0.1f)
                            : node.getScore();
                    addSegment(node.getType(), node.getId(), start, pos, score, segmentEnds);
                } else if (length == Character.charCount(cp)) {
                    addSegment(TokenType.UNKNOWN, model.getUnkId() >= 0 ? model.getUnkId() : 0,
                            start, start + length, model.getMinScore() - 10.0f, segmentEnds);
                }
            }
            start += Character.charCount(input.codePointAt(start));
        }
        resultBuilder.build(input, segmentEnds, collapseUnknowns);
    }

    private void addSegment(TokenType type, int id, int start, int end, float score, SegmentEnd[] segmentEnds) {
        if (segmentEnds[end] == null ||
                segmentEnds[start].scoreWith(scoring, score) > segmentEnds[end].score(scoring)) {

            segmentEnds[end] = new SegmentEnd(
                    type, id,
                    segmentEnds[start].getPathScoreSum() + score,
                    segmentEnds[start].getPathSegmentCount() + 1,
                    start
            );
        }
    }

    public interface ResultBuilder {
        void build(String input, SegmentEnd[] segmentEnds, boolean collapseUnknowns);
    }
}

package com.sentencepiece;


import sentencepiece.SentencepieceModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.sentencepiece.Piece.mapType;


/**
 * Wraps the SentencePiece protobuf model into a Java-friendly structure.
 */
public class Model {

    private final Map<String, Piece> vocabulary = new HashMap<>();
    private final TrieNode root = new TrieNode();
    private float maxScore = Float.NEGATIVE_INFINITY;
    private float minScore = Float.POSITIVE_INFINITY;

    private final List<Piece> piecesById = new ArrayList<>();
    // NEW: store <unk> id (or -1 if absent)
    private int unkId = -1;

    public Model() {
    }


    public static Model parseFrom(Path modelPath) throws IOException {
        byte[] bytes = Files.readAllBytes(modelPath);
        SentencepieceModel.ModelProto modelProto = SentencepieceModel.ModelProto.parseFrom(bytes);
        Model model = new Model();

        int id = 0;
        for (SentencepieceModel.ModelProto.SentencePiece sp : modelProto.getPiecesList()) {
            String token = sp.getPiece();
            float score = sp.getScore();
            // If your Piece has a type ctor, pass sp.getType() too; otherwise keep as is:
            model.addPiece(token, id++, score, mapType(sp.getType()));
        }

        // NEW: determine unkId from TrainerSpec or by name fallback
        if (modelProto.hasTrainerSpec() && modelProto.getTrainerSpec().hasUnkId()) {
            model.unkId = modelProto.getTrainerSpec().getUnkId();
        } else {
            // Fallback: find a piece literally named "<unk>"
            for (Piece p : model.piecesById) {
                if (p != null && "<unk>".equals(p.getToken())) {
                    model.unkId = p.getId();
                    break;
                }
            }
        }

        return model;
    }

    public void addPiece(String token, int id, float score, TokenType type) {
        Piece piece = new Piece(token, id, score, type);
        vocabulary.put(token, piece);

        // ensure O(1) id lookup
        while (piecesById.size() <= id) piecesById.add(null);
        piecesById.set(id, piece);

        maxScore = Math.max(maxScore, score);
        minScore = Math.min(minScore, score);
        insertIntoTrie(token, piece);
    }

    private void insertIntoTrie(String token, Piece piece) {
        TrieNode node = root;
        for (int i = 0; i < token.length(); ) {
            int cp = token.codePointAt(i);
            i += Character.charCount(cp);
            node = node.getOrCreate(cp);
        }
        // If Piece doesn't have getType(), use a default TokenType.TEXT here
        node.mark(piece.getId(), piece.getScore(), piece.getType());
    }

    public TrieNode getRoot() {
        return root;
    }

    public float getMaxScore() {
        return maxScore;
    }

    public float getMinScore() {
        return minScore;
    }

    public String getTokenById(int id) {
        for (Piece p : vocabulary.values()) {
            if (p.getId() == id) return p.getToken();
        }
        return "<unk>";
    }

    public int getUnkId() { return unkId; }

    public int getIdForToken(String token) {
        Piece piece = vocabulary.get(token);
        return (piece != null) ? piece.getId() : -1;
    }

    public String getTokenForId(int id) {
        return getTokenById(id);
    }

    public List<Integer> encode(String input) {
        // Naive space-based token matching for test (improve later with BPE segmenter)
        String trimmedInput = input.trim();
        String[] tokens = trimmedInput.split("\\s+");
        List<Integer> ids = new ArrayList<>();
        for (String t : tokens) {
            int id = getIdForToken(t);
            if (id != -1) {
                ids.add(id);
            } else {
                System.err.println("Unknown token: " + t);
            }
        }
        return ids;
    }



    public List<Integer> encodeNormalized(String rawInput, SentencePieceAlgorithm algorithm) {
        // Step 1: Unicode normalization
        String normalized = Normalizer.normalize(rawInput, Normalizer.Form.NFKC).toLowerCase();

        // Step 2: Collapse whitespace
        normalized = Pattern.compile("\\s+").matcher(normalized.trim()).replaceAll(" ");

        // Step 3: Add '▁' marker before each word
        StringBuilder sb = new StringBuilder();
        for (String word : normalized.split(" ")) {
            sb.append('▁').append(word);
        }

        String prepared = sb.toString();

        // Step 4: Segment using algorithm
        ResultBuilderImpl builder = new ResultBuilderImpl();
        algorithm.segment(prepared, builder, this);
        return builder.getTokenIds();
    }


    public String decodeSmart(List<Integer> ids) {
        StringBuilder sb = new StringBuilder();

        for (int id : ids) {
            String token = getTokenById(id);

            if (token.equals("<unk>")) {
                sb.append("�"); // or some fallback
                continue;
            }

            if (token.startsWith("▁")) {
                // Word boundary → insert a space if not at the very start
                if (sb.length() > 0) sb.append(' ');
                sb.append(token.substring(1));
            } else {
                // Continuation of the current word → append directly
                sb.append(token);
            }
        }

        return sb.toString().trim();
    }

    // Choose ONE consistently with training data
    private static final Pattern ZS = Pattern.compile("\\p{Z}+");

    // Strip Hebrew diacritics if model expects undiacritized text
    private static final Pattern HEBREW_DIACRITICS =
            Pattern.compile("[\\u0591-\\u05BD\\u05BF\\u05C1-\\u05C2\\u05C4-\\u05C5\\u05C7]");

    public String normalizeHebrew(String input, boolean stripDiacritics) {
        String s = Normalizer.normalize(input, Normalizer.Form.NFC);
        if (stripDiacritics) s = HEBREW_DIACRITICS.matcher(s).replaceAll("");
        s = ZS.matcher(s.trim()).replaceAll(" ");
        return s;
    }

    public String decode(List<Integer> ids) {
        return ids.stream()
                .map(this::getTokenForId)
                .collect(Collectors.joining(" "));
    }
}

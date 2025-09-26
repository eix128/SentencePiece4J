package com.sentencepiece;



import java.io.IOException;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SentencePieceProcessor {

    private final Model model;
    private final SentencePieceAlgorithm algorithm;
    private final boolean lowercase;

    // Model fields

    public SentencePieceProcessor(Path modelPath) throws IOException {
        this.model = Model.parseFrom(modelPath);
        this.algorithm = new SentencePieceAlgorithm(true, Scoring.HIGHEST_SCORE);
        this.lowercase = false;
    }


    public List<Integer> encode(String rawText) {
        String normalized = normalize(rawText);

        // Add SentencePiece boundary markers (▁)
        StringBuilder sb = new StringBuilder();
        for (String word : normalized.split(" ")) {
            sb.append('▁').append(word);
        }
        String prepared = sb.toString();

        ResultBuilderImpl builder = new ResultBuilderImpl();
        algorithm.segment(prepared, builder, model);
        return builder.getTokenIds();
    }

    public String decode(List<Integer> ids) {
        return ids.stream()
                .map(model::getTokenById)
                .map(t -> t.startsWith("▁") ? t.substring(1) : t)
                .collect(Collectors.joining(" "))
                .replaceAll(" +", " ")
                .trim();
    }

    public String decodeSmart(List<Integer> ids) {
        StringBuilder sb = new StringBuilder();

        for (int id : ids) {
            String token = model.getTokenById(id);
            if (token.startsWith("▁")) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(token.substring(1));
            } else {
                sb.append(token);
            }
        }

        return sb.toString().trim();
    }


    private static final Pattern WS = Pattern.compile("\\p{Z}+");
    private String normalize(String input) {
        String s = lowercase ? input.toLowerCase() : input;
        s = Normalizer.normalize(s, Normalizer.Form.NFKC);
        s = s.replaceAll("\\p{Cc}+", "");   // drop control chars
        s = WS.matcher(s.trim()).replaceAll(" ");
        return s;
    }
}

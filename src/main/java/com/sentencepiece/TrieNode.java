package com.sentencepiece;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class TrieNode {
    private final it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<TrieNode> children = new Int2ObjectOpenHashMap<>();
    private boolean isToken;
    private TokenType type = TokenType.TEXT;
    private int id = -1;
    private float score;


    public TrieNode getOrCreate(int codePoint) {
        TrieNode n = children.get(codePoint);
        if (n == null) { n = new TrieNode(); children.put(codePoint, n); }
        return n;
    }

    public TrieNode child(int codePoint) {
        return children.get(codePoint);
    }

    public void mark(int id, float score, TokenType type) {
        this.isToken = true; this.id = id; this.score = score; this.type = type;
    }

    public boolean isToken() {
        return isToken;
    }

    public TokenType getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public float getScore() {
        return score;
    }

}
package com.sentencepiece;


import sentencepiece.SentencepieceModel;

public class Piece {
    private final String token;
    private final int id;
    private final float score;
    private final TokenType type;   // NEW

    public Piece(String token, int id, float score, TokenType type) {
        this.token = token;
        this.id = id;
        this.score = score;
        this.type = type;
    }

    public String getToken() { return token; }
    public int getId() { return id; }
    public float getScore() { return score; }
    public TokenType getType() { return type; } // NEW

    public static TokenType mapType(SentencepieceModel.ModelProto.SentencePiece.Type t) {
        switch (t) {
            case NORMAL:       return TokenType.TEXT;
            case UNKNOWN:      return TokenType.UNKNOWN;
            case USER_DEFINED: return TokenType.USER_DEFINED;
            case CONTROL:      return TokenType.CONTROL;
            case BYTE:         return TokenType.BYTE;
            default:           return TokenType.TEXT;
        }
    }
}

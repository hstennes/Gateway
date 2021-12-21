package com.logic.components;

public interface BitWidthEntity {

    public static final int MAX_BITS = 16;

    public static final int MIN_BITS = 1;

    int getBitWidth();

    void changeBitWidth(int bitWidth);

}

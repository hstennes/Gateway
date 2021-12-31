package com.logic.components;

public interface BitWidthEntity {

    int MAX_BITS = 16;

    int MIN_BITS = 1;

    int getBitWidth();

    void changeBitWidth(int bitWidth);

    void validateBitWidth();
}

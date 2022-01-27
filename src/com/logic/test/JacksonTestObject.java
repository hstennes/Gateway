package com.logic.test;

import com.fasterxml.jackson.annotation.JsonInclude;

public class JacksonTestObject {

    public int otherThing = 19;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int[] poggers;

    public JacksonTestObject(int[] poggers){
        this.poggers = poggers;
    }

}

package com.ysoft.geecon.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PkceTest {

    @Test
    void validate() {
        assertTrue(Pkce.validate("S256", "W2ap_IuB0HJkMIkuxaXV2l8_Gx5mVmMStG_HQrAnQxA", "7AmAEXcl2Km9LQMwtUhif7GQ97HZy9RT72KZBwmxBRI"));
    }
}
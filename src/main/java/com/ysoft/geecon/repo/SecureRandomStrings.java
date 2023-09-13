package com.ysoft.geecon.repo;

import org.apache.commons.lang3.RandomStringUtils;

import java.security.SecureRandom;
import java.util.Random;

public class SecureRandomStrings {
    private static final Random RANDOM = new SecureRandom();
    public static String alphanumeric(int length) {
        return RandomStringUtils.random(length, 0, 0, true, true, null, RANDOM);
    }
}

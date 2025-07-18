package com.example.paymentservice.utils;

import org.apache.commons.lang.RandomStringUtils;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;

public class ShortIdGenerator implements IdentifierGenerator {
    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object obj) {
        return RandomStringUtils.randomAlphanumeric(8).toUpperCase();
    }
}

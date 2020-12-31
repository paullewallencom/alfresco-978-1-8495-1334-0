package com.bestmoney.test.foundationservice;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly = true)
public interface MyCmsService {

    public void readSomething();

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void writeSomething();

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void cleanUp();
}

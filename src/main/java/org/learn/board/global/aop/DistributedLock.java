package org.learn.board.global.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    // 락 고유 이름
    String key();

    // 락 대기 시간
    long waitTime() default 5;

    // 락 점유 시간
    long leaseTime() default 3;

    // 시간 단위
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}

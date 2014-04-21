package com.logicalpractice.diskbuffer.guava;

/**
 *
 */
public interface Deserializer<T> {
    T fromBytes(byte [] source, int offset, int length);
}

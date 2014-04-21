package com.logicalpractice.diskbuffer.guava;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 *
 */
public class JavaDeserializer<T> implements Deserializer<T> {

    @Override
    public T fromBytes(byte[] source, int offset, int length) {
        InputStream inputStream = new ByteArrayInputStream(source, offset, length);
        try {
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            return (T)ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

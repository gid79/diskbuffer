package com.logicalpractice.diskbuffer.guava;

import com.google.common.base.Preconditions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *
 */
public class JavaSerializer implements Serializer{

    @Override
    public byte[] serialize(Object value) {
        Preconditions.checkArgument(value instanceof Serializable, "'value' must be Serializable");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            oos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.logicalpractice.diskbuffer.core;

import java.nio.ByteBuffer;

/**
 *
 */
public interface BufferAllocator {

    ByteBuffer allocate(int size);

    void recycle(ByteBuffer buffer);
}

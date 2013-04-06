package com.logicalpractice.diskbuffer

import spock.lang.Specification

import java.nio.ByteBuffer

/**
 *
 */
class RecordBuffer2Specification extends Specification {

    File file = File.createTempFile("diskbuffer", "dat")

    def "reopen a file"(){
        setup:
        RecordBuffer buffer = RecordBuffer.newBuilder()
                                        .withPath(file.toPath())
                                        .build()
        300.times {buffer.append( ByteBuffer.wrap("Hello World ${it + 1}".bytes) )}
        buffer.close()

        when:
        RecordBuffer testObject = RecordBuffer.newBuilder().withPath(file.toPath()).build()

        then:
        testObject.end() == 300L

        cleanup:
        testObject.close()
        file.delete()
    }

}

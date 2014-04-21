package com.logicalpractice.diskbuffer.guava

import com.google.common.cache.CacheBuilder
import spock.lang.Specification

/**
 *
 */
class SnapshotCacheSpec extends Specification {

  def "should be able to write and read self"() {
    setup:
    def cache1 = new SnapshotCache.ManualCache(SnapshotCacheBuilder.from(CacheBuilder.newBuilder()))
    cache1.putAll([
        1:2,
        2:3,
        3:4,
        4:5
    ])

    def cache2 = new SnapshotCache.ManualCache(SnapshotCacheBuilder.from(CacheBuilder.newBuilder()))

    when:
    def baos = new ByteArrayOutputStream()
    cache1.writeTo(baos)

    cache2.readFrom(new ByteArrayInputStream(baos.toByteArray()))

    then:
    cache2.asMap() == [
        1:2,
        2:3,
        3:4,
        4:5
    ]
  }
}

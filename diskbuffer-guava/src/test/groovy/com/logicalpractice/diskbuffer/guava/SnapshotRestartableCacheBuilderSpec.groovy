package com.logicalpractice.diskbuffer.guava

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import spock.lang.Specification

/**
 *
 */
class SnapshotRestartableCacheBuilderSpec extends Specification {

  def "cache builder should return a cache impl"() {
    when:
    def result = SnapshotRestartableCacheBuilder.from(CacheBuilder.newBuilder()).build()

    then:
    result != null
    result.size() == 0L
    result.asMap() == [:]

    ! result.getIfPresent("banana")
  }

  def "cache builder should return a usable impl"() {
    setup:
    def cache = SnapshotRestartableCacheBuilder.from(CacheBuilder.newBuilder()).build()
    when:
    cache.putAll([
        1:2,
        2:3,
        3:4,
        4:5
    ])

    then:
    cache.size() == 4L
    cache.asMap() == [
        1:2,
        2:3,
        3:4,
        4:5
    ]

    cache.getIfPresent(1)
  }

  def "cache builder should return a usable loading cache"() {
    when:
    def result = SnapshotRestartableCacheBuilder
        .from(CacheBuilder.newBuilder())
        .build([load: { k -> "value :" + k}]  as CacheLoader)

    then:
    result != null
    result.size() == 0L
    result.asMap() == [:]

    ! result.getIfPresent("banana")

  }

  def "cache builder should return a usable loading cache 2"() {
    when:
    def cache = SnapshotRestartableCacheBuilder
        .from(CacheBuilder.newBuilder())
        .build([load: { k -> "value :" + k}]  as CacheLoader)

    then:
    cache.get(1) == "value :1"
    cache.get(2) == "value :2"
  }
}

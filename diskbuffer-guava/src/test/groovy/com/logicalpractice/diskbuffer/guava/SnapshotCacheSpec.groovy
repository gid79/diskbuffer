package com.logicalpractice.diskbuffer.guava

import com.google.common.cache.CacheBuilder
import spock.lang.Specification

/**
 *
 */
class SnapshotCacheSpec extends Specification {

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
}

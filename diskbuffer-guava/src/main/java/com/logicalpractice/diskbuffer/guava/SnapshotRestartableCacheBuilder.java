package com.logicalpractice.diskbuffer.guava;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.io.File;
import java.util.concurrent.ExecutorService;

/**
 *
 */
public class SnapshotRestartableCacheBuilder<K,V> {

    private final CacheBuilder<K, V> cacheBuilder;

    private String baseDirectory = System.getProperty("java.io.tmpdir");

    private String name = "cache.snapshot";

    private long snapshotAfterWrites = -1;

    private long snapshotAfterNanos = -1;

    private ExecutorService snapshotExecutor;

    protected SnapshotRestartableCacheBuilder(CacheBuilder<K, V> cacheBuilder) {
        this.cacheBuilder = cacheBuilder ;
    }

    public static <K,V> SnapshotRestartableCacheBuilder<K,V> from(CacheBuilder<K,V> cacheBuilder) {
        Preconditions.checkNotNull(cacheBuilder, "'cacheBuilder' instance is required");
        return new SnapshotRestartableCacheBuilder<>(cacheBuilder);
    }

    public Cache<K, V> build() {
      return new SnapshotCache.ManualCache<K, V>(this);
    }

    public CacheBuilder<K, V> getCacheBuilder() {
        return cacheBuilder;
    }

    public String baseDirectory() {
        return baseDirectory;
    }

    public String name() {
        return name;
    }

    public long snapshotAfterWrites() {
        return snapshotAfterWrites;
    }

    public long snapshotAfterNanos() {
        return snapshotAfterNanos;
    }

    public ExecutorService snapshotExecutor() {
        return snapshotExecutor;
    }
}

package com.logicalpractice.diskbuffer.guava;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class SnapshotRestartableCacheBuilder<K,V> {

    private final CacheBuilder<K, V> cacheBuilder;

    private String baseDirectory = System.getProperty("java.io.tmpdir");

    private String name = "cache.snapshot";

    private long snapshotAfterWrites = -1;

    private long snapshotAfterNanos = -1;

    private ExecutorService snapshotExecutor ;

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

    public LoadingCache<K, V> build(CacheLoader<K,V> loader) {
      return new SnapshotCache.LocalLoadingCache<K, V>(this, loader);
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
        if (snapshotExecutor == null) {
            return Executors.newSingleThreadExecutor(
                    new ThreadFactoryBuilder()
                            .setNameFormat("SNAPSHOT-%s")
                            .setDaemon(true)
                            .build()
            );
        }
        return snapshotExecutor;
    }
}

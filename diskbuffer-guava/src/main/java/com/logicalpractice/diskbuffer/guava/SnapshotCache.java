package com.logicalpractice.diskbuffer.guava;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.ForwardingCache;
import com.google.common.cache.ForwardingLoadingCache;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 *
 */
class SnapshotCache<K, V> implements LoadingCache<K,V> {

    private File directory;
    private String name;
    private ExecutorService executorService;

    private Cache<K,V> cache;
    private LoadingCache<K,V> loadingCache;

    SnapshotCache(
            SnapshotRestartableCacheBuilder<K, V> builder,
            @Nullable CacheLoader<K,V> cacheLoader
    ) {
        this.directory = new File(builder.baseDirectory());
        this.name = builder.name();
        this.executorService = builder.snapshotExecutor();

        if (directory.exists()) {
            if (!directory.isDirectory())
                throw new IllegalArgumentException("path:" + directory.getAbsolutePath() + " should be a directory");
        } else {
            boolean success = directory.mkdirs();
            if (!success) {
                throw new IllegalArgumentException("unable to create directory :" + directory.getAbsolutePath());
            }
        }

        if (cacheLoader == null) {
            this.cache = builder.getCacheBuilder().build();
            this.loadingCache = null;
        } else {
            // todo must wrap that cacheLoader
            this.loadingCache = builder.getCacheBuilder().build(cacheLoader);
            this.cache = loadingCache;
        }
        // todo must load snapshot from the disk
    }

    // Cache methods ...

    @Override
    @Nullable
    public V getIfPresent(Object key) {
        return cache.getIfPresent(key);
    }

    public V get(K key, Callable<? extends V> valueLoader) throws ExecutionException {
        return cache.get(key, valueLoader);
    }

    @Override
    public ImmutableMap<K, V> getAllPresent(Iterable<?> keys) {
        return cache.getAllPresent(keys);
    }

    public void put(K key, V value) {
        cache.put(key, value);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        cache.putAll(m);
    }

    @Override
    public void invalidate(Object key) {
        cache.invalidate(key);
    }

    @Override
    public void invalidateAll(Iterable<?> keys) {
        cache.invalidateAll(keys);
    }

    @Override
    public void invalidateAll() {
        cache.invalidateAll();
    }

    @Override
    public long size() {
        return cache.size();
    }

    @Override
    public CacheStats stats() {
        return cache.stats();
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        return cache.asMap();
    }

    @Override
    public void cleanUp() {
        cache.cleanUp();
    }

    // LoadingCache methods ...

    public V get(K key) throws ExecutionException {
        return loadingCache.get(key);
    }

    public V getUnchecked(K key) {
        return loadingCache.getUnchecked(key);
    }

    public ImmutableMap<K, V> getAll(Iterable<? extends K> keys) throws ExecutionException {
        return loadingCache.getAll(keys);
    }

    @Deprecated
    public V apply(K key) {
        return loadingCache.apply(key);
    }

    public void refresh(K key) {
        loadingCache.refresh(key);
    }

    static class ManualCache<K, V> extends ForwardingCache<K, V> {

        private final SnapshotCache<K, V> snapshotCache;

        ManualCache(SnapshotRestartableCacheBuilder<K, V> builder) {
            Preconditions.checkNotNull(builder, "'builder' is required");
            this.snapshotCache = new SnapshotCache<K,V>(builder, null);
        }

        @Override
        protected Cache<K, V> delegate() {
            return snapshotCache;
        }
    }

    static class LocalLoadingCache<K,V> extends  ForwardingLoadingCache<K,V> {
        private final SnapshotCache<K,V> snapshotCache;

        LocalLoadingCache(SnapshotRestartableCacheBuilder<K, V> builder, CacheLoader<K,V> loader) {
            Preconditions.checkNotNull(loader, "'loader' is required");
            this.snapshotCache = new SnapshotCache<>(builder, loader);
        }

        @Override
        protected LoadingCache<K, V> delegate() {
            return snapshotCache;
        }
    }
}

package com.logicalpractice.diskbuffer.guava;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.ForwardingCache;
import com.google.common.cache.ForwardingLoadingCache;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 *
 */
class SnapshotCache<K, V> implements LoadingCache<K, V> {

    private final File directory;
    private final String name;
    private final ExecutorService executorService;

    private final Cache<K, V> cache;
    private final LoadingCache<K, V> loadingCache;

    private final Serializer keySerializer;
    private final Serializer valueSerializer;
    private final Deserializer<K> keyDeserializer;
    private final Deserializer<V> valueDeserializer;

    SnapshotCache(
            SnapshotCacheBuilder<K, V> builder,
            @Nullable CacheLoader<K, V> cacheLoader
    ) {
        this.directory = new File(builder.baseDirectory());
        this.name = builder.name();
        this.executorService = builder.snapshotExecutor();

        this.keySerializer = builder.keySerializer();
        this.valueSerializer = builder.valueSerializer();

        this.keyDeserializer = builder.keyDeserializer();
        this.valueDeserializer = builder.valueDeserializer();

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

    private void writeTo(OutputStream outputStream) throws IOException {
        List [] snapshot = snapshot();
        List keys = snapshot[0];
        List values = snapshot[1];

        DataOutputStream dataOut = new DataOutputStream(outputStream);
        int length = keys.size();
        dataOut.writeInt(length);
        for (int i = 0; i < length; i ++) {
            byte [] key = keySerializer.serialize(keys.get(i));
            byte [] value = valueSerializer.serialize(values.get(i));
            dataOut.writeInt(key.length);
            dataOut.write(key);
            dataOut.writeInt(value.length);
            dataOut.write(value);
        }
    }

    private List [] snapshot() {
        // in theory this method may be implemented in the future using an exclusive lock
        // in order to ensure we have a consistent view of the backing cache
        Set<Map.Entry<K, V>> entries = cache.asMap().entrySet();
        List<Object> keys = new ArrayList<>(entries.size());
        List<Object> values = new ArrayList<>(entries.size());

        for (Map.Entry<K, V> entry : entries) {
            keys.add(entry.getKey());
            values.add(entry.getValue());
        }
        return new List[] {keys, values};
    }

    private void readFrom(InputStream inputStream) throws IOException {
        DataInputStream dataIn = new DataInputStream(inputStream);
        byte [] keyBuf = null, valueBuf = null;

        int length = dataIn.readInt();
        for (int i = 0; i < length; i++) {
            int keyLength = dataIn.readInt();
            keyBuf = ensureSize(keyBuf, keyLength);
            dataIn.readFully(keyBuf, 0, keyLength);

            int valueLength = dataIn.readInt();
            valueBuf = ensureSize(valueBuf, valueLength);
            dataIn.readFully(valueBuf, 0, valueLength);

            K key = keyDeserializer.fromBytes(keyBuf, 0, keyLength);
            V value = valueDeserializer.fromBytes(valueBuf, 0, valueLength);

            cache.put(key, value);
        }
    }

    private byte [] ensureSize(byte [] existing, int requiredSize){
        if (existing == null || existing.length < requiredSize) {
            return new byte[requiredSize * 2];
        }
        return existing;
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

        ManualCache(SnapshotCacheBuilder<K, V> builder) {
            Preconditions.checkNotNull(builder, "'builder' is required");
            this.snapshotCache = new SnapshotCache<K, V>(builder, null);
        }

        @Override
        protected Cache<K, V> delegate() {
            return snapshotCache;
        }

        void writeTo(OutputStream outputStream) throws IOException {
            snapshotCache.writeTo(outputStream);
        }

        void readFrom(InputStream inputStream) throws IOException {
            snapshotCache.readFrom(inputStream);
        }
    }

    static class LocalLoadingCache<K, V> extends ForwardingLoadingCache<K, V> {
        private final SnapshotCache<K, V> snapshotCache;

        LocalLoadingCache(SnapshotCacheBuilder<K, V> builder, CacheLoader<K, V> loader) {
            Preconditions.checkNotNull(loader, "'loader' is required");
            this.snapshotCache = new SnapshotCache<>(builder, loader);
        }

        @Override
        protected LoadingCache<K, V> delegate() {
            return snapshotCache;
        }
    }
}

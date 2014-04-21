package com.logicalpractice.diskbuffer.guava;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.ForwardingCache;
import com.google.common.cache.ForwardingLoadingCache;

import java.io.File;
import java.util.concurrent.ExecutorService;

/**
 *
 */
class SnapshotCache<K,V>  {

    private File directory;
    private String name ;
    private ExecutorService executorService;

    SnapshotCache(SnapshotRestartableCacheBuilder<K, V> builder) {

        directory = new File(builder.baseDirectory());
        if( directory.exists()) {
            if( !directory.isDirectory())
                throw new IllegalArgumentException("path:"+ directory.getAbsolutePath() + " should be a directory");
        }
        if( directory.exists())
    }

    static class ManualCache<K,V> extends ForwardingCache<K,V> {

        private final Cache<K,V> delegate;

        private final SnapshotCache<K,V> snapshotCache;

        ManualCache(SnapshotRestartableCacheBuilder<K,V> builder) {
            Preconditions.checkNotNull(builder, "'builder' is required");

            this.delegate = builder.getCacheBuilder().build();

            this.snapshotCache = new SnapshotCache<>(builder);
        }

        @Override
        protected Cache<K, V> delegate() {
            return delegate;
        }
    }

}

package com.penguineering.cleanuri.apigateway.model;

import io.micronaut.core.annotation.Introspected;
import net.jcip.annotations.Immutable;

import java.net.URI;
import java.util.*;

@Introspected
@Immutable
public class ReductionTask {

    public enum Meta {
      TITLE,
      PRICE
    };


    public static class Builder {
        public static Builder withURI(final URI uri) {
            return new Builder(uri);
        }

        private final URI uri;
        private Set<Meta> meta;

        private Builder(URI uri) {
            this.uri = uri;
            this.meta = null;
        }

        public Builder addMeta(Meta meta) {
            if (this.meta == null)
                this.meta = new HashSet<>();
            this.meta.add(meta);
            return this;
        }

        public ReductionTask instance() {
            ReductionTask instance = new ReductionTask(
                    uri,
                    meta
            );
            this.meta = null;
            return instance;
        }
    }

    private final URI uri;
    private final Set<Meta> meta;

    private ReductionTask(URI uri, Set<Meta> meta) {
        if (uri == null)
            throw new IllegalArgumentException("URI must not be null!");

        this.uri = uri;
        this.meta = meta != null ? meta : Collections.emptySet();
    }

    public URI getUri() {
        return uri;
    }

    public Set<Meta> getMeta() {
        return meta;
    }
}

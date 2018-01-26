package org.infinispan.creson.search;

import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.impl.operations.RetryOnFailureOperation;
import org.infinispan.client.hotrod.impl.protocol.Codec;
import org.infinispan.client.hotrod.impl.protocol.HeaderParams;
import org.infinispan.client.hotrod.impl.transport.Transport;
import org.infinispan.client.hotrod.impl.transport.TransportFactory;
import org.infinispan.creson.query.CresonRequest;
import org.infinispan.creson.query.CresonResponse;

import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class QueryOperation extends RetryOnFailureOperation<CresonResponse> {

    private final RemoteQuery remoteQuery;

    public QueryOperation(Codec codec, TransportFactory transportFactory, byte[] cacheName, AtomicInteger topologyId,
                          int flags, ClientIntelligence clientIntelligence, RemoteQuery remoteQuery) {
        super(codec, transportFactory, cacheName, topologyId, flags, clientIntelligence);
        this.remoteQuery = remoteQuery;
    }

    @Override
    protected Transport getTransport(int i, Set<SocketAddress> set) {
        return null;
    }

    @Override
    protected CresonResponse executeOperation(Transport transport) {
        HeaderParams params = writeHeader(transport, QUERY_REQUEST);
        CresonRequest request = new CresonRequest();
        request.setMaxResults(remoteQuery.maxResults);
        request.setQueryString(remoteQuery.jpqlString);
        request.setStartOffset(remoteQuery.startOffset);
        return null;
    }
}
/*
 * Copyright (c) 2012-2014 Spotify AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spotify.dns;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.net.HostAndPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;

import java.util.List;

/**
 * A DnsSrvResolver implementation that uses the dnsjava implementation from xbill.org:
 * http://www.xbill.org/dnsjava/
 */
class XBillDnsSrvResolver implements DnsSrvResolver {
  private static final Logger LOG = LoggerFactory.getLogger(XBillDnsSrvResolver.class);

  private final LookupFactory lookupFactory;

  XBillDnsSrvResolver(LookupFactory lookupFactory) {
    this.lookupFactory = Preconditions.checkNotNull(lookupFactory, "lookupFactory");
  }

  @Override
  public List<HostAndPort> resolve(final String fqdn) {
    return ImmutableList.copyOf(resolveWithTTL(fqdn).keys());
  }

  @Override
  public Multimap<HostAndPort, Long> resolveWithTTL(final String fqdn) {
    Lookup lookup = lookupFactory.forName(fqdn);
    Record[] queryResult = lookup.run();

    switch (lookup.getResult()) {
      case Lookup.SUCCESSFUL:
        return toHostAndPortsWithTTL(queryResult);
      case Lookup.HOST_NOT_FOUND:
        // fallthrough
      case Lookup.TYPE_NOT_FOUND:
        LOG.warn("No results returned for query '{}'; result from XBill: {} - {}",
            fqdn, lookup.getResult(), lookup.getErrorString());
        return ImmutableMultimap.of();
      default:
        throw new DnsException(
            String.format("Lookup of '%s' failed with code: %d - %s ",
                fqdn, lookup.getResult(), lookup.getErrorString())
        );
    }
  }

  private Multimap<HostAndPort, Long> toHostAndPortsWithTTL(Record[] queryResult) {
    ImmutableMultimap.Builder<HostAndPort, Long> builder = ImmutableMultimap.builder();

    if (queryResult != null) {
      for (Record record : queryResult) {
        if (record instanceof SRVRecord) {
          SRVRecord srvRecord = (SRVRecord) record;
          builder.put(HostAndPort.fromParts(srvRecord.getTarget().toString(), srvRecord.getPort()), srvRecord.getTTL());
        }
      }
    }

    return builder.build();
  }
}

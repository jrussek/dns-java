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

import com.google.common.collect.Multimap;
import com.google.common.net.HostAndPort;

import java.util.List;

/**
 * Contract for doing SRV lookups.
 */
public interface DnsSrvResolver {
  /**
   * Does a DNS SRV lookup for the supplied fully qualified domain name, and returns the
   * matching hosts and ports.
   *
   * @param fqdn a DNS name to query for
   * @return a possibly empty list of matching hosts and ports
   * @throws DnsException if there was an error doing the DNS lookup
   */
  List<HostAndPort> resolve(String fqdn);

  Multimap<HostAndPort, Long> resolveWithTTL(String fqdn);
}

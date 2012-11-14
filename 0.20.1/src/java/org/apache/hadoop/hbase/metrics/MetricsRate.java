/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hbase.metrics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.metrics.MetricsRecord;
import org.apache.hadoop.util.StringUtils;

/**
 * Publishes a rate based on a counter - you increment the counter each
 * time an event occurs (eg: an RPC call) and this publishes a rate.
 */
public class MetricsRate {
  private static final Log LOG = LogFactory.getLog("org.apache.hadoop.hbase.metrics");
  
  private String name;
  private int value;
  private float prevRate;
  private long ts;
  
  public MetricsRate(final String name) {
    this.name = name;
    this.value = 0;
    this.prevRate = 0;
    this.ts = System.currentTimeMillis();
  }
  
  public synchronized void inc(final int incr) {
    value += incr;
  }
  
  public synchronized void inc() {
    value++;
  }
  
  private synchronized void intervalHeartBeat() {
    long now = System.currentTimeMillis();
    long diff = (now-ts)/1000;
    if (diff == 0) diff = 1; // sigh this is crap.
    this.prevRate = (float)value / diff;
    this.value = 0;
    this.ts = now;
  }
  
  public synchronized void pushMetric(final MetricsRecord mr) {
    intervalHeartBeat();
    try {
      mr.setMetric(name, getPreviousIntervalValue());
    } catch (Exception e) {
      LOG.info("pushMetric failed for " + name + "\n" + 
          StringUtils.stringifyException(e));
    }
  }
  public synchronized float getPreviousIntervalValue() {
    return this.prevRate;
  }
}
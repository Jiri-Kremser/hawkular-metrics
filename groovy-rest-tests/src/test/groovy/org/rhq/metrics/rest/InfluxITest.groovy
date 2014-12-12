package org.rhq.metrics.rest

import org.joda.time.DateTime
import org.junit.Test

import static org.joda.time.DateTime.now
import static org.junit.Assert.assertEquals

/**
 * @author Thomas Segismont
 */
class InfluxITest extends RESTTest {

  static def tenantId = "influxtest"

  @Test
  void testInfluxDataOrderedAsc() {
    def timeseriesName = "influx.dataasc"
    def start = now().minus(2000)
    postData(timeseriesName, start)

    def influxQuery = 'select value from "' + timeseriesName + '" order asc'

    def response = rhqm.get(path: "influx/${tenantId}/series", query: [q: influxQuery])
    assertEquals(200, response.status)

    assertEquals(
        [
            [
                columns: ["time", "value"],
                name   : timeseriesName,
                points : [
                    [start.millis, 40.1],
                    [start.plus(1000).millis, 41.1],
                    [start.plus(2000).millis, 42.1]
                ]
            ]
        ],
        response.data
    )
  }

  @Test
  void testInfluxDataOrderedDescByDefault() {
    def timeseriesName = "influx.datadesc"
    def start = now().minus(2000)
    postData(timeseriesName, start)

    def influxQuery = 'select value from "' + timeseriesName + '"'

    def response = rhqm.get(path: "influx/${tenantId}/series", query: [q: influxQuery])
    assertEquals(200, response.status)

    assertEquals(
        [
            [
                columns: ["time", "value"],
                name   : timeseriesName,
                points : [
                    [start.plus(2000).millis, 42.1],
                    [start.plus(1000).millis, 41.1],
                    [start.millis, 40.1]
                ]
            ]
        ],
        response.data
    )
  }

  @Test
  void testInfluxAddGetOneMetric() {
    def timeseriesName = "influx.foo"
    def start = now().minus(2000)
    postData(timeseriesName, start)

    def influxQuery = 'select mean(value) from "' + timeseriesName + '" where time > now() - 30s group by time(30s) '

    def response = rhqm.get(path: "influx/${tenantId}/series", query: [q: influxQuery])
    assertEquals(200, response.status)

    assertEquals(
        [
            [
                columns: ["time", "mean"],
                name   : timeseriesName,
                points : [
                    [start.plus(2000).millis, 41.1]
                ]
            ]
        ],
        response.data
    )
  }

  @Test
  void testInfluxLimitClause() {
    def timeseriesName = "influx.limitclause"
    def start = now().minus(2000)
    postData(timeseriesName, start)

    def influxQuery = 'select value from "' + timeseriesName + '" limit 2 order asc '

    def response = rhqm.get(path: "influx/${tenantId}/series", query: [q: influxQuery])
    assertEquals(200, response.status)

    assertEquals(
        [
            [
                columns: ["time", "value"],
                name   : timeseriesName,
                points : [
                    [start.millis, 40.1],
                    [start.plus(1000).millis, 41.1]
                ]
            ]
        ],
        response.data
    )
  }

  @Test
  void testInfluxAddGetOneSillyMetric() {
    def timeseriesName = "influx.foo3"
    def start = now().minus(2000)
    postData(timeseriesName, start)

    def influxQuery = 'select mean(value) from "' + timeseriesName + '''" where time > '2013-08-12 23:32:01.232'
                        and time < '2013-08-13' group by time(30s) '''


    def response = rhqm.get(path: "influx/${tenantId}/series", query: [q: influxQuery])
    assertEquals(200, response.status)

    assertEquals(
        [
            [
                columns: ["time", "mean"],
                name   : timeseriesName,
                points : []
            ]
        ],
        response.data
    )
  }

  private static void postData(String timeseriesName, DateTime start) {
    def response = rhqm.post(path: "${tenantId}/metrics/numeric/data", body: [
        [
            name: timeseriesName,
            data: [
                [timestamp: start.millis, value: 40.1],
                [timestamp: start.plus(1000).millis, value: 41.1],
                [timestamp: start.plus(2000).millis, value: 42.1]
            ]
        ]
    ])
    assertEquals(200, response.status)
  }
}

package blah;

import java.util.concurrent.ExecutionException;

import com.clickhouse.client.ClickHouseClient;
import com.clickhouse.client.ClickHouseException;
import com.clickhouse.client.ClickHouseNode;
import com.clickhouse.client.ClickHouseNodes;
import com.clickhouse.client.ClickHouseProtocol;
import com.clickhouse.client.ClickHouseResponse;
import com.clickhouse.data.ClickHouseFormat;
import com.clickhouse.data.ClickHouseRecord;

import lombok.extern.slf4j.Slf4j;

/**
 * Uses clickhouse-specific java client.
 * https://clickhouse.com/docs/en/integrations/java#java-client
 * 
 * Recommended, and better ("most flexible and performance") than the jdbc/r2dbc
 * ones. Uses HTTP calls behind the scenes.
 * 
 * Derives from sample: https://github.com/ClickHouse/clickhouse-java
 * 
 * Uses the existing clickhouse 'playground' :
 * https://clickhouse.com/docs/en/getting-started/playground
 * 
 * Turns off compression, as that was giving errors complaining about magic #
 * (perhaps version difference in underlying compression lib)
 */

@Slf4j
public class App {
  public static void main(String[] args) {
    String table = "opensky";// existing table in clickhouse playground
    log.info("Connecting to clickhouse playground with table: {}", table);
    ClickHouseNode server = ClickHouseNodes.of("https://explorer@play.clickhouse.com:443?compress=0").getNodes().get(0);// lovely
    try {
      query(server, table);
    } catch (ClickHouseException e) {
      log.error("Error querying table", e);
    }
  }

  static int query(ClickHouseNode server, String table) throws ClickHouseException {
    try (ClickHouseClient client = ClickHouseClient.newInstance(ClickHouseProtocol.HTTP);
        ClickHouseResponse response = client.read(server)
            // they seem focused on this format, see details at
            // https://github.com/ClickHouse/clickhouse-java/issues/928
            .format(ClickHouseFormat.RowBinaryWithNamesAndTypes)
            .query("select * from " + table).execute().get()) {
      int count = 0;
      for (ClickHouseRecord record : response.records()) {
        log.info("response record # {} : {}", count, record);
        record.forEach(value -> log.info("value: {}", value));
        count++;
      }
      return count;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw ClickHouseException.forCancellation(e, server);
    } catch (ExecutionException e) {
      throw ClickHouseException.of(e, server);
    }
  }
}

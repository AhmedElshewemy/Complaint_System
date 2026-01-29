package com.complaint.complaint_consumer_service.health;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Component
public class KafkaHealthIndicator implements HealthIndicator {

    private final String bootstrapServers;

    public KafkaHealthIndicator(
        @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        this.bootstrapServers = bootstrapServers;
    }

    @Override
    public Health health() {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);

        try (AdminClient admin = AdminClient.create(props)) {

            DescribeClusterResult result = admin.describeCluster();

            String clusterId =
                result.clusterId().get(3, TimeUnit.SECONDS);

            int nodes =
                result.nodes().get(3, TimeUnit.SECONDS).size();

            return Health.up()
                    .withDetail("clusterId", clusterId)
                    .withDetail("brokerCount", nodes)
                    .build();

        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}

package co.com.yam.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsAsyncClient;

@Configuration
@Profile("aws")
public class AwsSnsConfig {

    @Bean
    public SnsAsyncClient snsAsyncClient() {
        return SnsAsyncClient.builder()
                .region(Region.of(System.getenv().getOrDefault("AWS_REGION", "us-east-1")))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}

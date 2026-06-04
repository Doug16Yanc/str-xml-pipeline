package tech.strxmlpipeline.infrastructure.config.aws

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import java.net.URI

@Configuration
class AwsS3Config(
    @Value("\${aws.s3.region}")            private val region: String,
    @Value("\${aws.s3.access-key-id}")     private val accessKeyId: String,
    @Value("\${aws.s3.secret-access-key}") private val secretAccessKey: String,
    @Value("\${aws.s3.endpoint:#{null}}")  private val endpoint: String?,
) {

    @Bean
    fun s3Client(): S3Client {
        val credentials = StaticCredentialsProvider.create(
            AwsBasicCredentials.create(accessKeyId, secretAccessKey)
        )

        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(credentials)
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(endpoint != null)
                    .build()
            )
            .apply {
                if (!endpoint.isNullOrBlank()) {
                    endpointOverride(URI.create(endpoint))
                }
            }
            .build()
    }
}
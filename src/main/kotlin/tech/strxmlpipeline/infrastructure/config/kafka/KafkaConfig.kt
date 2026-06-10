package tech.strxmlpipeline.infrastructure.config.kafka

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries
import org.springframework.scheduling.annotation.EnableScheduling
import tech.strxmlpipeline.infrastructure.exception.local.DuplicateSettlementReturnException
import java.time.Clock
import java.util.UUID

@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${kafka.topics.batch-emission.name:str.batch.emission}")      private val emissionTopic: String,
    @Value("\${kafka.topics.batch-emission.partitions:12}")                private val emissionPartitions: Int,
    @Value("\${kafka.topics.settlement-return.name:str.settlement.return}") private val returnTopic: String,
    @Value("\${kafka.topics.settlement-return.partitions:4}")              private val returnPartitions: Int,
    @Value("\${kafka.consumer.group-id:str-xml-pipeline}")                 private val groupId: String,
    private val clock: Clock
) {

    // ── Topics ────────────────────────────────────────────────────────────────

    /**
     * Emission topic — partition count matches the number of settlement windows
     * configured in the scheduler. Each window maps to a dedicated partition via
     * SettlementWindow.partitionKey, guaranteeing temporal ordering within a window
     * without distributed locks.
     */
    @Bean
    fun batchEmissionTopic(): NewTopic = TopicBuilder
        .name(emissionTopic)
        .partitions(emissionPartitions)
        .replicas(3)
        .config("retention.ms", "${7 * 24 * 60 * 60 * 1000}") // 7 days
        .config("min.insync.replicas", "2")
        .build()

    @Bean
    fun settlementReturnTopic(): NewTopic = TopicBuilder
        .name(returnTopic)
        .partitions(returnPartitions)
        .replicas(3)
        .config("retention.ms", "${30L * 24 * 60 * 60 * 1000}") // 30 days — audit retention
        .config("min.insync.replicas", "2")
        .build()

    // DLT topics — created automatically by @RetryableTopic but declared explicitly
    // so retention and replica config are enforced
    @Bean
    fun batchEmissionDlt(): NewTopic = TopicBuilder
        .name("$emissionTopic.DLT")
        .partitions(emissionPartitions)
        .replicas(3)
        .config("retention.ms", "${90L * 24 * 60 * 60 * 1000}") // 90 days
        .build()

    @Bean
    fun settlementReturnDlt(): NewTopic = TopicBuilder
        .name("$returnTopic.DLT")
        .partitions(returnPartitions)
        .replicas(3)
        .config("retention.ms", "${90L * 24 * 60 * 60 * 1000}")
        .build()

    // ── Producer ──────────────────────────────────────────────────────────────

    @Bean
    fun producerFactory(): ProducerFactory<String, String> =
        DefaultKafkaProducerFactory(
            mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG        to bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG     to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG   to StringSerializer::class.java,
                ProducerConfig.ACKS_CONFIG                     to "all",
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG       to true,
                ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION to 1,
                ProducerConfig.RETRIES_CONFIG                  to 3,
                ProducerConfig.LINGER_MS_CONFIG                to 5,
                ProducerConfig.BATCH_SIZE_CONFIG               to 65536,
            )
        )

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, String> =
        KafkaTemplate(producerFactory())

    // ── Consumer ──────────────────────────────────────────────────────────────

    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> =
        DefaultKafkaConsumerFactory(
            mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG        to bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG                 to groupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG   to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                // Manual ack — consumer only commits offset after successful processing
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG       to false,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG        to "earliest",
                ConsumerConfig.MAX_POLL_RECORDS_CONFIG         to 10,
                ConsumerConfig.ISOLATION_LEVEL_CONFIG          to "read_committed",
            )
        )

    /**
     * Exponential backoff: 3 retries — 1s → 2s → 4s — then routes to DLT.
     * DuplicateSettlementReturnException is not retryable — BACEN sent the same
     * return twice, no point retrying. Ack immediately and let the DLT record it.
     */
    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val backoff = ExponentialBackOffWithMaxRetries(3).apply {
            initialInterval = 1_000L
            multiplier      = 2.0
            maxInterval     = 10_000L
        }

        val errorHandler = DefaultErrorHandler(backoff).apply {
            // Non-retryable exceptions — go straight to DLT
            addNotRetryableExceptions(
                DuplicateSettlementReturnException::class.java,
                IllegalArgumentException::class.java,
                IllegalStateException::class.java,
            )
        }

        return ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            setConsumerFactory(this@KafkaConfig.consumerFactory())

            containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
            setCommonErrorHandler(errorHandler)
            setConcurrency(2)
        }
    }
}


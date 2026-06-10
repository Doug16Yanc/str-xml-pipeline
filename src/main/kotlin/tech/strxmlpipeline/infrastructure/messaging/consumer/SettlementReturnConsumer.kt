package tech.strxmlpipeline.infrastructure.messaging.consumer

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tech.strxmlpipeline.domain.enum.RejectionReason
import tech.strxmlpipeline.domain.enum.ReturnResult
import tech.strxmlpipeline.domain.model.SettlementReturn
import tech.strxmlpipeline.domain.port.`in`.ProcessSettlementReturnUseCase
import tech.strxmlpipeline.domain.port.out.XmlFilePort
import tech.strxmlpipeline.infrastructure.persistence.service.operational.RawReturnPersistenceServiceImpl
import tech.strxmlpipeline.infrastructure.exception.local.DuplicateSettlementReturnException
import tech.strxmlpipeline.infrastructure.persistence.service.operational.StrReturnXmlParserImpl
import java.util.UUID

@Component
class SettlementReturnConsumer(
    private val processReturnUseCase: ProcessSettlementReturnUseCase,
    private val rawReturnPersistence: RawReturnPersistenceServiceImpl,
    private val xmlParser: StrReturnXmlParserImpl,
    private val xmlFilePort: XmlFilePort
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @RetryableTopic(
        attempts = "4",
        backoff = Backoff(
            delay = 1_000L,
            multiplier = 2.0,
            maxDelay = 10_000L,
        ),
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
        exclude = [
            DuplicateSettlementReturnException::class,
            IllegalArgumentException::class,
        ],
        dltTopicSuffix = ".DLT",
    )
    @KafkaListener(
        topics           = ["\${kafka.topics.settlement-return.name:str.settlement.return}"],
        groupId          = "\${kafka.consumer.group-id:str-xml-pipeline}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    @Transactional
    fun consume(
        record: ConsumerRecord<String, String>,
        ack: Acknowledgment,
    ) {
        val rawXml = record.value()

        log.info(
            "Received settlement return — partition [{}] offset [{}] — {} bytes",
            record.partition(), record.offset(), rawXml.length,
        )


        val rawRecord = rawReturnPersistence.persist(
            rawPayload = rawXml,
            partition  = record.partition(),
            offset     = record.offset(),
        )

        val parsed = xmlParser.parse(rawXml)

        log.info(
            "Parsed return — batchId [{}] result [{}] messageCode [{}]",
            parsed.batchId, parsed.result, parsed.messageCode,
        )

        val batchId = UUID.fromString(parsed.batchId)

        rawReturnPersistence.linkToBatch(rawRecord.id, batchId)

        val xmlFile = requireNotNull(xmlFilePort.findByBatchId(batchId)) {
            "XmlFile not found for batch [$batchId] — cannot process return"
        }

        processReturnUseCase.process(
            SettlementReturn(
                batchId         = batchId,
                fileId          = xmlFile.id,
                result          = ReturnResult.valueOf(parsed.result),
                messageCode     = parsed.messageCode,
                description     = parsed.description ?: parsed.result,
                rejectionReason = parsed.rejectionCode?.let { RejectionReason.fromCode(it) },
            )
        )

        ack.acknowledge()

        log.info("Settlement return processed — batchId [{}]", parsed.batchId)
    }

    @DltHandler
    fun onDlt(
        record: ConsumerRecord<String, String>,
        @Header(KafkaHeaders.EXCEPTION_MESSAGE) exceptionMessage: String?,
    ) {
        log.error(
            "Settlement return reached DLT — offset [{}] partition [{}] — reason [{}]",
            record.offset(), record.partition(), exceptionMessage,
        )
    }
}
package tech.strxmlpipeline.infrastructure.exception.local

class UnauthorizedParticipantException(operatorIspb: String, originatorIspb: String) :
    RuntimeException(
        "Operator with ISPB [$operatorIspb] is not authorized " +
                "to act on behalf of originator ISPB [$originatorIspb]"
    )

package tech.strxmlpipeline.infrastructure.exception.local

class UnauthorizedParticipantException : RuntimeException {

    constructor(message: String) : super(message)

    constructor(operatorIspb: String, originatorIspb: String) : super(
        "Operator with ISPB [$operatorIspb] is not authorized to act on behalf of originator ISPB [$originatorIspb]"
    )
}
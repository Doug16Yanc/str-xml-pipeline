package tech.strxmlpipeline.infrastructure.security.util

const val PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{9,}$"
const val PASSWORD_MESSAGE =
    "Password must be at least 9 characters and contain uppercase, lowercase, digit, and special character"

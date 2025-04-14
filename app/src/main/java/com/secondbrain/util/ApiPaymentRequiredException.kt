package com.secondbrain.util

/**
 * Exception thrown when an API requires payment or additional credits
 */
class ApiPaymentRequiredException(message: String) : Exception(message)

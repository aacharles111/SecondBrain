package com.secondbrain.util

/**
 * Exception thrown when an API server is overloaded and cannot process the request
 */
class ApiServerOverloadException(message: String) : Exception(message)

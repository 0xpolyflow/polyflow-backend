package polyflow.exception

import org.springframework.http.HttpStatus

abstract class ServiceException(
    val errorCode: ErrorCode,
    val httpStatus: HttpStatus,
    override val message: String,
    override val cause: Throwable? = null
) : RuntimeException(message) {
    companion object {
        private const val serialVersionUID: Long = 8974557457024980481L
    }
}

class ResourceNotFoundException(message: String) : ServiceException(
    errorCode = ErrorCode.RESOURCE_NOT_FOUND,
    httpStatus = HttpStatus.NOT_FOUND,
    message = message
) {
    companion object {
        private const val serialVersionUID: Long = 8937915498141342807L
    }
}

class AccessForbiddenException(message: String) : ServiceException(
    errorCode = ErrorCode.ACCESS_FORBIDDEN,
    httpStatus = HttpStatus.FORBIDDEN,
    message = message
) {
    companion object {
        private const val serialVersionUID: Long = 6548344480966415539L
    }
}

class UserNotYetVerifiedException : ServiceException(
    errorCode = ErrorCode.USER_NOT_YET_VERIFIED,
    httpStatus = HttpStatus.FORBIDDEN,
    message = "User account has not yet been verified"
) {
    companion object {
        private const val serialVersionUID: Long = -1251019795312778357L
    }
}

class JwtTokenException(message: String) : ServiceException(
    errorCode = ErrorCode.BAD_AUTHENTICATION,
    httpStatus = HttpStatus.UNAUTHORIZED,
    message = message
) {
    companion object {
        private const val serialVersionUID: Long = -1691263375051175380L
    }
}

class BadAuthenticationException : ServiceException(
    errorCode = ErrorCode.BAD_AUTHENTICATION,
    httpStatus = HttpStatus.UNAUTHORIZED,
    message = "Authentication header is missing or has invalid format"
) {
    companion object {
        private const val serialVersionUID: Long = -787538305851627646L
    }
}

class NonExistentApiKeyException : ServiceException(
    errorCode = ErrorCode.NON_EXISTENT_API_KEY,
    httpStatus = HttpStatus.UNAUTHORIZED,
    message = "Non existent API key provided in request"
) {
    companion object {
        private const val serialVersionUID: Long = -176593491332037627L
    }
}

class NonExistentUserException : ServiceException(
    errorCode = ErrorCode.NON_EXISTENT_USER,
    httpStatus = HttpStatus.UNAUTHORIZED,
    message = "Non existent user"
) {
    companion object {
        private const val serialVersionUID: Long = 6232694989086178848L
    }
}

class UserAlreadyExistsException : ServiceException(
    errorCode = ErrorCode.USER_ALREADY_EXISTS,
    httpStatus = HttpStatus.BAD_REQUEST,
    message = "User already exists for the provided email address"
) {
    companion object {
        private const val serialVersionUID: Long = 7597674635139449827L
    }
}

class InvalidUserSecureTokenException : ServiceException(
    errorCode = ErrorCode.USER_SECURE_TOKEN_INVALID,
    httpStatus = HttpStatus.BAD_REQUEST,
    message = "Provided token is expired or does not exist"
) {
    companion object {
        private const val serialVersionUID: Long = 1387167781755092912L
    }
}

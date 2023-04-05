package polyflow.exception

import org.springframework.graphql.execution.ErrorType
import polyflow.util.annotation.Description

enum class ErrorCode(val graphQlErrorType: ErrorType) {

    @Description("Indicates that the requested resource cannot be found")
    RESOURCE_NOT_FOUND(ErrorType.NOT_FOUND),

    @Description("Access to the provided resource is not allowed for the current user")
    ACCESS_FORBIDDEN(ErrorType.FORBIDDEN),

    @Description("Provided authentication token has invalid format")
    BAD_AUTHENTICATION(ErrorType.FORBIDDEN),

    @Description("The project already has an API key generated and no more keys can be generated for that project")
    API_KEY_ALREADY_EXISTS(ErrorType.BAD_REQUEST),

    @Description("The provided API key does not exist and is therefore invalid")
    NON_EXISTENT_API_KEY(ErrorType.NOT_FOUND),

    @Description("Indicates that user with provided id does not exist")
    NON_EXISTENT_USER(ErrorType.NOT_FOUND),

    @Description("Indicates that user with provided email already exists")
    USER_ALREADY_EXISTS(ErrorType.BAD_REQUEST),

    @Description("Indicates that provided user secure token is invalid (i.e. expired or doesn't exist)")
    USER_SECURE_TOKEN_INVALID(ErrorType.BAD_REQUEST),

    @Description("Indicates that user account has not yet been verified")
    USER_NOT_YET_VERIFIED(ErrorType.FORBIDDEN)
}

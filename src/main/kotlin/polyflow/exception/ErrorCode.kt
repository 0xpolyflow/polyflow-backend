package polyflow.exception

import org.springframework.graphql.execution.ErrorType
import polyflow.util.annotation.Description

enum class ErrorCode(val graphQlErrorType: ErrorType) {

    @Description("Indicates that the requested resource cannot be found")
    RESOURCE_NOT_FOUND(ErrorType.NOT_FOUND),

    @Description("Access to the provided resource is not allowed for the current user")
    ACCESS_FORBIDDEN(ErrorType.FORBIDDEN),

    @Description("Indicates that one or more fields in the request body has an invalid value")
    INVALID_REQUEST_BODY(ErrorType.BAD_REQUEST),

    @Description("Provided authentication token has invalid format")
    BAD_AUTHENTICATION(ErrorType.FORBIDDEN),

    @Description("The provided API key does not exist and is therefore invalid")
    NON_EXISTENT_API_KEY(ErrorType.NOT_FOUND),

    @Description("Indicates that user with provided id does not exist")
    NON_EXISTENT_USER(ErrorType.NOT_FOUND),

    @Description("Indicates that user with provided email already exists")
    USER_ALREADY_EXISTS(ErrorType.BAD_REQUEST),

    @Description("Indicates that provided user secure token is invalid (i.e. expired or doesn't exist)")
    USER_SECURE_TOKEN_INVALID(ErrorType.BAD_REQUEST),

    @Description("Indicates that user account has not yet been verified")
    USER_NOT_YET_VERIFIED(ErrorType.FORBIDDEN),

    @Description("Indicates that Stripe session ID is not yet set; create Stripe session to resolve this")
    STRIPE_SESSION_ID_MISSING(ErrorType.BAD_REQUEST),

    @Description("Active subscription is required to access this resource")
    NO_ACTIVE_SUBSCRIPTION(ErrorType.FORBIDDEN),

    @Description("Usage limit for this account has been reached; switch to a higher tier to increase the limit")
    USAGE_LIMIT_EXCEEDED(ErrorType.FORBIDDEN),

    @Description("Specified alias already exists for the project")
    ALIAS_ALREADY_EXISTS(ErrorType.BAD_REQUEST),

    @Description(
        "Requested data cannot be retrieved from the blockchain because it is not well formed (i.e. non-existent" +
            " contract address, calling non-existent contract function etc.)"
    )
    BLOCKCHAIN_READ_ERROR(ErrorType.BAD_REQUEST),
}

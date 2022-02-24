package com.app.medialoader.tinyhttpd.response

/**
 * 响应状态
 *
 * @author vincanyang
 */
enum class HttpStatus(val code: Int, val desc: String) {
    OK(200, "OK"), PARTIAL_CONTENT(206, "Partial Content"), BAD_REQUEST(
        400,
        "Bad Request"
    ),
    UNAUTHORIZED(401, "Unauthorized"), FORBIDDEN(403, "Forbidden"), NOT_FOUND(
        404,
        "Not Found"
    ),
    TOO_MANY_REQUESTS(429, "Too Many Requests"), INTERNAL_ERROR(500, "Internal Server Error");

    override fun toString(): String {
        return "$code $desc"
    }
}
package io.github.opensmsrelay.core.common

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (String, Throwable?) -> Unit): Result<T> {
    if (this is Result.Error) action(message, cause)
    return this
}

fun <T> Result<T>.getOrNull(): T? = if (this is Result.Success) data else null

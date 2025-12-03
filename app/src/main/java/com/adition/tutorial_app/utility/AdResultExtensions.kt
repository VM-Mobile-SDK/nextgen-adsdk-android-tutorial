package com.adition.tutorial_app.utility

import com.adition.ad_sdk.api.entities.exception.AdResult

suspend fun <T, ActionResult> AdResult<T>.map(
    action: suspend (T) -> ActionResult
): AdResult<ActionResult> {
    return when (this) {
        is AdResult.Success -> AdResult.Success(action(this.result))
        is AdResult.Error -> AdResult.Error(this.error)
    }
}

suspend fun <T, ActionResult> AdResult<T>.flatMap(
    action: suspend (T) -> AdResult<ActionResult>
): AdResult<ActionResult> {
    return when (this) {
        is AdResult.Success -> action(this.result)
        is AdResult.Error -> AdResult.Error(this.error)
    }
}

suspend fun <T> AdResult<T>.onSuccess(
    action: suspend (T) -> Unit
) : AdResult<T> {
    return when (this) {
        is AdResult.Success -> {
            action(this.result)
            this
        }
        is AdResult.Error -> this
    }
}

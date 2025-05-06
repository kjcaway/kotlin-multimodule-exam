package me.courtboard.api.global


class CourtboardContext private constructor() {
    companion object {
        private val requestContext = ThreadLocal<RequestContext>()

        fun setContext(context: RequestContext) {
            requestContext.set(context)
        }

        fun getContext(): RequestContext {
            return requestContext.get() ?: throw IllegalStateException("illegal state")
        }

        fun clearContext() {
            requestContext.remove()
        }

        fun isLogin(): Boolean {
            if (requestContext.get() == null) {
                return false
            }
            return requestContext.get().memberId != Constants.GUEST_ID
        }
    }
}

data class RequestContext(
    val memberId: String,
    val role: String,
)
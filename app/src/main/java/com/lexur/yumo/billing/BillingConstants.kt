package com.lexur.yumo.billing

object BillingConstants {
    const val PREMIUM_PRODUCT_ID = "premium_character_creator"

    // Billing response codes
    const val BILLING_RESPONSE_RESULT_OK = 0
    const val BILLING_RESPONSE_RESULT_USER_CANCELED = 1
    const val BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE = 2
    const val BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3
    const val BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4
    const val BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5
    const val BILLING_RESPONSE_RESULT_ERROR = 6
    const val BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7
    const val BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8
}
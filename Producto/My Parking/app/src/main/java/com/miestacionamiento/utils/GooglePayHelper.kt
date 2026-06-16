package com.miestacionamiento.utils

import org.json.JSONArray
import org.json.JSONObject

object GooglePayHelper {

    private val allowedCardNetworks = JSONArray(listOf("MASTERCARD", "VISA"))
    private val allowedAuthMethods = JSONArray(listOf("PAN_ONLY", "CRYPTOGRAM_3DS"))

    val isReadyToPayRequest: JSONObject
        get() = JSONObject().apply {
            put("apiVersion", 2)
            put("apiVersionMinor", 0)
            put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod()))
        }

    fun createPaymentDataRequest(priceCLP: Double): String {
        val paymentDataRequestJson = JSONObject().apply {
            put("apiVersion", 2)
            put("apiVersionMinor", 0)
            put("allowedPaymentMethods", JSONArray().put(cardPaymentMethod()))
            put("transactionInfo", transactionInfo(priceCLP))
            put("merchantInfo", JSONObject().apply {
                put("merchantName", "MyParking")
            })
        }
        return paymentDataRequestJson.toString()
    }

    private fun baseCardPaymentMethod(): JSONObject = JSONObject().apply {
        put("type", "CARD")
        put("parameters", JSONObject().apply {
            put("allowedAuthMethods", allowedAuthMethods)
            put("allowedCardNetworks", allowedCardNetworks)
        })
    }

    private fun cardPaymentMethod(): JSONObject = baseCardPaymentMethod().apply {
        put("tokenizationSpecification", JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put("parameters", JSONObject().apply {
                put("gateway", "example")
                put("gatewayMerchantId", "exampleGatewayMerchantId")
            })
        })
    }

    private fun transactionInfo(priceCLP: Double): JSONObject = JSONObject().apply {
        put("totalPrice", String.format("%.0f", priceCLP))
        put("totalPriceStatus", "FINAL")
        put("currencyCode", "CLP")
        put("countryCode", "CL")
    }
}

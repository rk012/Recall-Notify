package io.github.rk012.recaller

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val client = HttpClient()

const val API_URL = "3c6d-108-16-235-152.ngrok.io"

@Serializable
data class Product(
    val name: String,
    val seller: String,
    val id: Long
)

@Serializable
data class SellerProduct(
    val product: Product,
    val token: String
)

@Serializable
data class ProductIdResponse(
    val id: String,
    val token: String
)

suspend fun getRecalls(ids: List<Long>): Map<Long, LocalDate> {
    val response = client.get {
        url {
            protocol = URLProtocol.HTTPS
            host = API_URL
            path("recalls")
            parameters["ids"] = Json.encodeToString(ids)
        }
    }

    return Json.decodeFromString<Map<String, String>>(response.body()).mapKeys { it.key.toLong() }.mapValues { LocalDate.parse(it.value) }
}

suspend fun getRecallId() = client.get {
    url {
        protocol = URLProtocol.HTTPS
        host = API_URL
        path("recall-auth")
    }
}.let { Json.decodeFromString<ProductIdResponse>(it.body()) }

suspend fun issueRecall(token: String) {
    client.post {
        url {
            protocol = URLProtocol.HTTPS
            host = API_URL
            path("recalls")
        }

        bearerAuth(token)
    }
}

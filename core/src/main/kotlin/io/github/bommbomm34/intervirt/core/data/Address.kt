package io.github.bommbomm34.intervirt.core.data

data class Address(
    val host: String,
    val port: Int
){
    companion object {
        val EXAMPLE = Address("example.com", 1234)
    }

    override fun toString() = "$host:$port"
}

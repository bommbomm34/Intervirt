package io.github.bommbomm34.intervirt.data

class VirtualHost(
    val serverName: String,
    val documentRoot: String
) {
    companion object {
        fun generateConfiguration(virtualHosts: List<VirtualHost>): String {
            val builder = StringBuilder()
            virtualHosts.forEach {
                builder.append(
                    """
                    <VirtualHost *:80>
                        ServerName ${it.serverName}
                        DocumentRoot ${it.documentRoot}
                    </VirtualHost>
    
                    """.trimIndent()
                )
            }
            return builder.toString()
        }
    }
}
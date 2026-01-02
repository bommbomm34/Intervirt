package io.github.bommbomm34.intervirt.data

object Screens {
    val SETUP = 0
    val HOME = 1
    val SETTINGS = 3
    val OS_INSTALLER = 5

    object IntervirtOS {
        val HOME = 0
        val MAILSERVER = 1
        val MAIL_CLIENT = 2
        val TERMINAL = 3
        val DNS_SERVER = 4
        val DNS_RESOLVER = 5
        val HTTP_SERVER = 6
        val SSH_SERVER = 7
        val BROWSER = 8
        val SSH_CLIENT = 9
    }

    object Setup {
        val VM_CONFIGURATION = 0
        val APP_CONFIGURATION = 1
        val INSTALLATION = 2
    }
}
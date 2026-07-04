package io.github.bommbomm34.intervirt.core.data.env

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
internal annotation class Env(val name: String)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
internal annotation class NoEnv

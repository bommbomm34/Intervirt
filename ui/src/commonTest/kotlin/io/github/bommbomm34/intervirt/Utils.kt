/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.recover
import io.github.bommbomm34.intervirt.core.data.Failure
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest

fun runIntervirtTest(block: suspend context(Raise<Failure>) () -> Unit) = runTest {
    recover(
        block = { block(this) },
        recover = {
            throw AssertionError("Expected successful result, but got $it instead")
        },
    )
}

inline infix fun <T> Iterable<T>.shouldMatchAny(predicate: (T) -> Boolean) {
    withClue("No element matched the predicate in the following iterable: $this") {
        this.any(predicate).shouldBeTrue()
    }
}

private val runningOnCi: Boolean by lazy {
    System.getenv("INTERVIRT_TEST_CI")?.toBoolean() ?: false
}

fun isRunningOnCi(): Boolean = runningOnCi

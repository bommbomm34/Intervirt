package io.github.bommbomm34.intervirt.core.util

import arrow.core.raise.Raise
import arrow.core.raise.recover
import io.github.bommbomm34.intervirt.core.data.Failure
import kotlinx.coroutines.test.runTest

fun runIntervirtTest(block: suspend context(Raise<Failure>) () -> Unit) = runTest {
    recover(
        block = { block(this) },
        recover = {
            throw AssertionError("Expected successful result, but got $it instead")
        }
    )
}

inline fun ignoreFailure(block: context(Raise<Failure>) () -> Unit) {
    recover(block = block, recover = {})
}



inline fun fails(block: context(Raise<Failure>) () -> Unit): Boolean = recover(
    block = {
        block(this)
        false
    },
    recover = { true }
)

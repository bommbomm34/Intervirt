package io.github.bommbomm34.intervirt.test

import arrow.core.raise.Raise
import arrow.core.raise.recover
import io.github.bommbomm34.intervirt.core.data.Failure
import kotlinx.coroutines.test.runTest





inline fun fails(block: context(Raise<Failure>) () -> Unit): Boolean = recover(
    block = {
        block(this)
        false
    },
    recover = { true }
)

/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data.agent

import arrow.core.Either
import io.github.bommbomm34.intervirt.core.data.Failure
import kotlinx.coroutines.flow.MutableSharedFlow

data class ActiveRequest(
    val body: RequestBody,
    val flow: MutableSharedFlow<Either<Failure, ResponseBody>> = MutableSharedFlow(),
)

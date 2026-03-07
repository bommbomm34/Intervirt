/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.MultipleAnimatedVisibility
import io.github.bommbomm34.intervirt.components.buttons.BackButton
import io.github.bommbomm34.intervirt.components.buttons.NextButton
import io.github.bommbomm34.intervirt.model.SetupViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun Setup() {
    val viewModel = koinViewModel<SetupViewModel>()
    AlignedBox(Alignment.TopCenter) {
        Text(
            text = "Intervirt Setup",
            fontSize = 40.sp,
        )
    }
    AlignedBox(Alignment.Center) {
        MultipleAnimatedVisibility(
            visible = viewModel.currentSetupScreenIndex,
            screens = viewModel.setupScreens,
        )
    }
    AlignedBox(Alignment.BottomStart) {
        BackButton(viewModel.currentSetupScreenIndex > 0) {
            viewModel.currentSetupScreenIndex--
        }
    }
    AlignedBox(Alignment.BottomCenter) {
        Text(
            text = (viewModel.currentSetupScreenIndex + 1).toString(),
            fontSize = 25.sp,
        )
    }
    AlignedBox(Alignment.BottomEnd) {
        NextButton(viewModel.currentSetupScreenIndex < viewModel.setupScreens.size - 1) {
            viewModel.currentSetupScreenIndex++
        }
    }
}
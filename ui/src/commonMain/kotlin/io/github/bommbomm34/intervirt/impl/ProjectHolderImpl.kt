package io.github.bommbomm34.intervirt.impl

import io.github.bommbomm34.intervirt.core.api.atomic.Holder
import io.github.bommbomm34.intervirt.core.data.Project
import io.github.bommbomm34.intervirt.data.AppState

class ProjectHolderImpl(private val appState: AppState) : Holder<Project> {
    override fun get(): Project {
        return appState.project.value
    }

    override fun set(new: Project) {
        appState.project.value = new
    }

    override fun compareAndSet(expected: Project, new: Project): Boolean {
        return appState.project.compareAndSet(expected, new)
    }
}

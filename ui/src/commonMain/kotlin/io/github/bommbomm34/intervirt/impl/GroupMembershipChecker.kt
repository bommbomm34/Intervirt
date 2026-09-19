/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.impl

import io.github.bommbomm34.intervirt.core.data.OS
import kotlin.io.path.Path
import kotlin.io.path.bufferedReader

object GroupMembershipChecker {
    private val CURRENT_USER = System.getProperty("user.name")
        ?: error("Expected non-null 'user.name' property, but got null")
    private val GROUP_FILE_PATH = Path("/etc/group")
    private val GROUPS = readGroups()


    fun checkGroupMembership(group: String): Boolean {
        require(OS.CURRENT == OS.LINUX) { "Checking group membership of '$group' only works on Linux!" }

        return group in GROUPS
    }

    private fun readGroups(): Sequence<String> {
        return sequence {
            GROUP_FILE_PATH.bufferedReader().useLines { lines ->
                for (line in lines) {
                    val parts = line.split(':')
                    // group_name:password:gid:members
                    val group = parts[0] // group_name
                    val members = parts[3].split(',') // members

                    if (CURRENT_USER in members) yield(group)
                }
            }
        }
    }
}

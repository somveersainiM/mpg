/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.paging

import androidx.paging.LoadState.Loading
import androidx.paging.LoadState.NotLoading
import androidx.paging.LoadType.APPEND
import androidx.paging.LoadType.PREPEND
import androidx.paging.LoadType.REFRESH
import androidx.paging.PageEvent.Drop
import assertk.assertThat
import assertk.assertions.*
import kotlin.test.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class FlattenedPageEventStorageTest {
    private val list = FlattenedPageEventStorage<String>()

    @Test
    fun empty() {
        assertThat(list.snapshot()).isEqualTo(Snapshot<String>())
        assertThat(list.getAsEvents()).isEmpty()
    }

    @Test
    fun refresh() {
        list.add(
            localRefresh(
                pages = listOf(
                    TransformablePage(data = listOf("a", "b", "c"))
                ),
                placeholdersBefore = 3,
                placeholdersAfter = 5,
            )
        )
        assertThat(list.snapshot()).isEqualTo(
            Snapshot(
                items = listOf("a", "b", "c"),
                placeholdersBefore = 3,
                placeholdersAfter = 5
            )
        )
    }

    @Test
    fun refresh_thenPrepend() {
        list.add(
            localRefresh(
                pages = listOf(
                    TransformablePage(data = listOf("a", "b", "c"))
                ),
                placeholdersBefore = 3,
                placeholdersAfter = 5,
            )
        )
        list.add(
            localPrepend(
                pages = listOf(
                    TransformablePage(data = listOf("x1")),
                    TransformablePage(data = listOf("x2"))
                ),
                placeholdersBefore = 1,
            )
        )
        assertThat(list.snapshot()).isEqualTo(
            Snapshot(
                items = listOf("x1", "x2", "a", "b", "c"),
                placeholdersBefore = 1,
                placeholdersAfter = 5
            )
        )
    }

    @Test
    fun refresh_thenAppend() {
        list.add(
            localRefresh(
                pages = listOf(
                    TransformablePage(data = listOf("a", "b", "c"))
                ),
                placeholdersBefore = 3,
                placeholdersAfter = 5,
            )
        )
        list.add(
            localAppend(
                pages = listOf(
                    TransformablePage(data = listOf("x1")),
                    TransformablePage(data = listOf("x2")),
                    TransformablePage(data = listOf("x3"))
                ),
                placeholdersAfter = 2,
            )
        )
        assertThat(list.snapshot()).isEqualTo(
            Snapshot(
                items = listOf("a", "b", "c", "x1", "x2", "x3"),
                placeholdersBefore = 3,
                placeholdersAfter = 2
            )
        )
    }

    @Test
    fun refresh_refreshAgain() {
        list.add(
            localRefresh(
                pages = listOf(
                    TransformablePage(data = listOf("a", "b", "c"))
                ),
                placeholdersBefore = 3,
                placeholdersAfter = 5,
            )
        )
        list.add(
            localRefresh(
                pages = listOf(
                    TransformablePage(data = listOf("x", "y"))
                ),
                placeholdersBefore = 2,
                placeholdersAfter = 4,
            )
        )
        assertThat(list.snapshot()).isEqualTo(
            Snapshot(
                items = listOf("x", "y"),
                placeholdersBefore = 2,
                placeholdersAfter = 4
            )
        )
    }

    @Test
    fun drop_fromStart() {
        list.add(
            localRefresh(
                pages = listOf(
                    TransformablePage(data = listOf("a", "b", "c")),
                    TransformablePage(data = listOf("d", "e"))
                ),
                placeholdersBefore = 3,
                placeholdersAfter = 5,
            )
        )
        assertThat(list.snapshot()).isEqualTo(
            Snapshot(
                items = listOf("a", "b", "c", "d", "e"),
                placeholdersBefore = 3,
                placeholdersAfter = 5
            )
        )
        list.add(
            Drop(
                loadType = PREPEND,
                minPageOffset = 0,
                maxPageOffset = 0,
                placeholdersRemaining = 6
            )
        )
        assertThat(list.snapshot()).isEqualTo(
            Snapshot(
                items = listOf("d", "e"),
                placeholdersBefore = 6,
                placeholdersAfter = 5
            )
        )
    }

    @Test
    fun drop_fromEnd() {
        list.add(
            localRefresh(
                pages = listOf(
                    TransformablePage(data = listOf("a", "b", "c")),
                    TransformablePage(data = listOf("d", "e"))
                ),
                placeholdersBefore = 3,
                placeholdersAfter = 5,
            )
        )
        assertThat(list.snapshot()).isEqualTo(
            Snapshot(
                items = listOf("a", "b", "c", "d", "e"),
                placeholdersBefore = 3,
                placeholdersAfter = 5
            )
        )
        list.add(
            Drop(
                loadType = APPEND,
                minPageOffset = 1,
                maxPageOffset = 1,
                placeholdersRemaining = 7
            )
        )
        assertThat(list.snapshot()).isEqualTo(
            Snapshot(
                items = listOf("a", "b", "c"),
                placeholdersBefore = 3,
                placeholdersAfter = 7
            )
        )
    }

    @Test
    fun stateInInsert() {
        val error = LoadState.Error(RuntimeException("?"))
        list.add(
            localRefresh(
                pages = listOf(
                    TransformablePage(data = listOf("a", "b", "c")),
                    TransformablePage(data = listOf("d", "e"))
                ),
                placeholdersBefore = 3,
                placeholdersAfter = 5,
                source = loadStates(prepend = Loading, append = error)
            )
        )
        assertThat(list.snapshot()).isEqualTo(
            Snapshot(
                items = listOf("a", "b", "c", "d", "e"),
                placeholdersBefore = 3,
                placeholdersAfter = 5,
                sourceLoadStates = loadStates(
                    refresh = NotLoading.Incomplete,
                    prepend = Loading,
                    append = error
                )
            )
        )
    }

    private fun <T : Any> FlattenedPageEventStorage<T>.snapshot(): Snapshot<T> {
        return this.getAsEvents().fold(Snapshot()) { snapshot, event ->
            when (event) {
                is PageEvent.Insert -> {
                    check(event.loadType == REFRESH) {
                        "should only send refresh event"
                    }
                    snapshot.copy(
                        items = snapshot.items + event.pages.flatMap { it.data },
                        placeholdersBefore = event.placeholdersBefore,
                        placeholdersAfter = event.placeholdersAfter,
                        sourceLoadStates = event.sourceLoadStates,
                        mediatorLoadStates = event.mediatorLoadStates,
                    )
                }
                is Drop -> {
                    throw IllegalStateException("shouldn't have any drops")
                }
                is PageEvent.LoadStateUpdate -> {
                    throw IllegalStateException("shouldn't have any state updates")
                }
            }
        }
    }

    data class Snapshot<T>(
        val items: List<T> = emptyList(),
        val sourceLoadStates: LoadStates = loadStates(),
        val mediatorLoadStates: LoadStates? = null,
        val placeholdersBefore: Int = 0,
        val placeholdersAfter: Int = 0
    )
}
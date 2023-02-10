/*
 * Copyright 2022 The Android Open Source Project
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

package androidx.credentials.exceptions

/**
 * During the create credential flow, this is thrown when some interruption occurs that may warrant
 * retrying or at least does not indicate a purposeful desire to close or tap away from credential
 * manager.
 *
 * @see CreateCredentialException
 */
class CreateCredentialInterruptedException @JvmOverloads constructor(
    errorMessage: CharSequence? = null
) : CreateCredentialException(TYPE_CREATE_CREDENTIAL_INTERRUPTED_EXCEPTION, errorMessage) {

    /** @hide */
    companion object {
        internal const val TYPE_CREATE_CREDENTIAL_INTERRUPTED_EXCEPTION =
            "android.credentials.CreateCredentialException.TYPE_INTERRUPTED"
    }
}
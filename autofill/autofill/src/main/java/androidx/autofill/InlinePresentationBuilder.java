/*
 * Copyright 2020 The Android Open Source Project
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

package androidx.autofill;

import android.annotation.SuppressLint;
import android.app.slice.Slice;
import android.app.slice.SliceSpec;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.service.autofill.AutofillService;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.util.Preconditions;

import java.util.Collections;

/**
 * Helper class to create {@link Slice} for rendering into inline suggestions via
 * {@link InlinePresentationRenderer}.
 *
 * <p>This builder is used by {@link AutofillService} providers to create slices representing
 * their inline suggestions UI.</p>
 *
 * TODO(b/147116534): Add documentation about UI templating.
 */
@SuppressLint("TopLevelBuilder")
@RequiresApi(api = Build.VERSION_CODES.Q) //TODO(b/147116534): Update to R.
public class InlinePresentationBuilder {

    private static final String TAG = "InlinePresentationBuilder";

    private static final String INLINE_PRESENTATION_SPEC_TYPE = "InlinePresentation";
    private static final int INLINE_PRESENTATION_SPEC_VERSION = 1;
    private static final String INLINE_PRESENTATION_SLICE_URI = "inline.slice";

    static final String HINT_INLINE_TITLE = "inline_title";
    static final String HINT_INLINE_SUBTITLE = "inline_subtitle";
    static final String HINT_INLINE_START_ICON = "inline_start_icon";
    static final String HINT_INLINE_END_ICON = "inline_end_icon";

    @Nullable private Icon mStartIcon;
    @Nullable private Icon mEndIcon;
    @NonNull private String mTitle;
    @Nullable private String mSubtitle;

    private boolean mDestroyed;

    /**
     * Initializes an {@link InlinePresentationBuilder} with title text.
     *
     * @param title String displayed as title of slice.
     */
    public InlinePresentationBuilder(@NonNull CharSequence title) {
        Preconditions.checkNotNull(title, "Title must not be null");
        mTitle = title.toString();
    }

    /**
     * Sets the subtitle of {@link Slice}.
     *
     * @param subtitle String displayed as subtitle of slice.
     */
    public @NonNull InlinePresentationBuilder setSubtitle(@NonNull CharSequence subtitle) {
        throwIfDestroyed();
        Preconditions.checkNotNull(subtitle, "Subtitle should not be null");
        mSubtitle = subtitle.toString();
        return this;
    }

    /**
     * Sets the start icon of {@link Slice}.
     *
     * @param startIcon {@link Icon} resource displayed at start of slice.
     */
    public @NonNull InlinePresentationBuilder setStartIcon(@NonNull Icon startIcon) {
        throwIfDestroyed();
        Preconditions.checkNotNull(startIcon, "StartIcon should not be null");
        mStartIcon = startIcon;
        return this;
    }

    /**
     * Sets the end icon of {@link Slice}.
     *
     * @param endIcon {@link Icon} resource displayed at end of slice.
     */
    public @NonNull InlinePresentationBuilder setEndIcon(@NonNull Icon endIcon) {
        throwIfDestroyed();
        Preconditions.checkNotNull(endIcon, "EndIcon should not be null");
        mEndIcon = endIcon;
        return this;
    }

    /**
     * Creates a new {@link Slice} instance.
     *
     * <p>You should not interact with this builder once this method is called.
     *
     * @throws IllegalStateException if the title was not set.
     *
     * @return The built slice.
     */
    public @NonNull Slice build() {
        throwIfDestroyed();
        mDestroyed = true;
        if (mTitle == null) {
            throw new IllegalStateException("The title must be set");
        }

        Slice.Builder builder = new Slice.Builder(Uri.parse(INLINE_PRESENTATION_SLICE_URI),
                new SliceSpec(INLINE_PRESENTATION_SPEC_TYPE, INLINE_PRESENTATION_SPEC_VERSION));
        if (mStartIcon != null) {
            builder.addIcon(mStartIcon, null, Collections.singletonList(HINT_INLINE_START_ICON));
        }
        builder.addText(mTitle, null, Collections.singletonList(HINT_INLINE_TITLE));
        if (mSubtitle != null) {
            builder.addText(mSubtitle, null, Collections.singletonList(HINT_INLINE_SUBTITLE));
        }
        if (mEndIcon != null) {
            builder.addIcon(mEndIcon, null, Collections.singletonList(HINT_INLINE_END_ICON));
        }
        return builder.build();
    }

    private void throwIfDestroyed() {
        if (mDestroyed) {
            throw new IllegalStateException("Already called #build()");
        }
    }
}

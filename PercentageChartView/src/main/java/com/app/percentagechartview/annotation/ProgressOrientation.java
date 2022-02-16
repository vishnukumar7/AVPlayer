/*
 * Copyright 2018 Rami Jemli
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

package com.app.percentagechartview.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

import static com.app.percentagechartview.renderer.BaseModeRenderer.INVALID_ORIENTATION;
import static com.app.percentagechartview.renderer.BaseModeRenderer.ORIENTATION_CLOCKWISE;
import static com.app.percentagechartview.renderer.BaseModeRenderer.ORIENTATION_COUNTERCLOCKWISE;

@Retention(RetentionPolicy.SOURCE)
@IntDef({INVALID_ORIENTATION, ORIENTATION_CLOCKWISE, ORIENTATION_COUNTERCLOCKWISE})
public @interface ProgressOrientation {
}

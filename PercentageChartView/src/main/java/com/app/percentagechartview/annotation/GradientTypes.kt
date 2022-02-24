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
package com.app.percentagechartview.annotation

import androidx.annotation.IntDef
import com.app.percentagechartview.renderer.BaseModeRenderer

@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
@IntDef(
    BaseModeRenderer.INVALID_GRADIENT,
    BaseModeRenderer.GRADIENT_LINEAR,
    BaseModeRenderer.GRADIENT_RADIAL,
    BaseModeRenderer.GRADIENT_SWEEP
)
annotation class GradientTypes 
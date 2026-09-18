/*
 * Copyright (C) 2026 Astrit Veliu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.astritveliu.showcaseoverlay.build

object Configuration {
  const val compileSdk = 37
  const val targetSdk = 37
  const val minSdk = 24
  const val minSdkDemo = 24

  const val majorVersion = 1
  const val minorVersion = 0
  const val patchVersion = 0
  const val versionName = "$majorVersion.$minorVersion.$patchVersion"
  const val versionCode = 1
  const val snapshotVersionName = "$majorVersion.$minorVersion.${patchVersion + 1}-SNAPSHOT"

  // Must match the GROUP declared in gradle.properties. For an io.github.*
  // namespace, Maven Central verifies ownership against this exact GitHub
  // account (case-sensitive), so keep it in sync with your username.
  const val artifactGroup = "io.github.astrit-veliu"
}

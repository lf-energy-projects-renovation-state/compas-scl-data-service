// SPDX-FileCopyrightText: 2021 Alliander N.V.
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.scl.data;

import io.quarkus.test.junit.NativeImageTest;
import org.lfenergy.compas.scl.data.rest.HealthCheckTest;

@NativeImageTest
class NativeHealthCheckIT extends HealthCheckTest {
    // Execute the same tests but in native mode.
}
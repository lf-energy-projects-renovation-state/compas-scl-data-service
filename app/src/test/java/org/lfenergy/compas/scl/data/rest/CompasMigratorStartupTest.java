// SPDX-FileCopyrightText: 2021 Alliander N.V.
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.scl.data.rest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.compas.scl.data.service.CompasMigratorService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompasMigratorStartupTest {
    @Mock
    private CompasMigratorService compasMigratorService;

    @InjectMocks
    private CompasMigratorStartup compasMigratorStartup;

    @Test
    void migrate_WhenCalled_ThenMigratorIsCalled() {
        when(compasMigratorService.migrate()).thenReturn(true);

        compasMigratorStartup.migrate();

        verify(compasMigratorService, times(1)).migrate();
    }
}
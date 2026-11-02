package com.mottainai.operacional.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class RoleAccessTest {
    @Test public void missingAndUnknownRolesDenyPrivilegedOperations() {
        for (String role : new String[]{null, "", " ", "unknown", "guest"}) {
            assertFalse(RoleHelper.canViewSuggestions(role));
            assertFalse(RoleHelper.canRegisterProduct(role));
            assertFalse(RoleHelper.canApprovePosRequests(role));
            assertFalse(RoleHelper.isOwner(role));
            assertFalse(RoleHelper.isKnownRole(role));
        }
    }

    @Test public void stockRolesHaveNoManagerPrivileges() {
        for (String role : new String[]{"ESTOQUISTA", "stock", "OPERATOR", "SUPERVISOR"}) {
            assertTrue(RoleHelper.isKnownRole(role));
            assertFalse(RoleHelper.canViewSuggestions(role));
            assertFalse(RoleHelper.canApprovePosRequests(role));
            assertFalse(RoleHelper.isOwner(role));
        }
    }

    @Test public void managersCanReviewButCannotConfigureOwnerArea() {
        for (String role : new String[]{"GERENTE", " manager "}) {
            assertTrue(RoleHelper.canViewSuggestions(role));
            assertTrue(RoleHelper.canApprovePosRequests(role));
            assertFalse(RoleHelper.isOwner(role));
        }
    }

    @Test public void ownersAndLegacyAliasesRemainCompatible() {
        for (String role : new String[]{"dono", "OWNER", "ADMIN", "ADMINISTRATOR"}) {
            assertTrue(RoleHelper.isOwner(role));
            assertTrue(RoleHelper.canViewSuggestions(role));
            assertTrue(RoleHelper.canApprovePosRequests(role));
        }
    }
}

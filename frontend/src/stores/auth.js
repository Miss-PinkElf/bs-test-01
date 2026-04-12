import { computed, ref } from "vue";
import { defineStore } from "pinia";
import { clearSession, getSession, saveSession } from "../utils/session";

const ROLE_ADMIN = "ADMIN";
const ROLE_WAREHOUSE_MANAGER = "WAREHOUSE_MANAGER";
const ROLE_VIEWER = "VIEWER";

export function normalizeRoleCodes(roleCodes) {
  return Array.isArray(roleCodes) ? roleCodes.filter(Boolean) : [];
}

export function resolvePrimaryRole(roleCodes) {
  if (roleCodes.includes(ROLE_ADMIN)) {
    return ROLE_ADMIN;
  }
  if (roleCodes.includes(ROLE_WAREHOUSE_MANAGER)) {
    return ROLE_WAREHOUSE_MANAGER;
  }
  return ROLE_VIEWER;
}

export function resolveDefaultRouteByRoleCodes(roleCodes) {
  return resolvePrimaryRole(normalizeRoleCodes(roleCodes)) === ROLE_WAREHOUSE_MANAGER
    ? "/environment"
    : "/dashboard";
}

export function canAccessAllowedRoles(roleCodes, allowedRoles = []) {
  if (!Array.isArray(allowedRoles) || allowedRoles.length === 0) {
    return true;
  }

  return normalizeRoleCodes(roleCodes).some((roleCode) => allowedRoles.includes(roleCode));
}

export const useAuthStore = defineStore("auth", () => {
  const user = ref(getSession());

  const isLoggedIn = computed(() => Boolean(user.value));
  const roleCodes = computed(() => normalizeRoleCodes(user.value?.roleCodes));
  const primaryRole = computed(() => resolvePrimaryRole(roleCodes.value));
  const isAdmin = computed(() => roleCodes.value.includes(ROLE_ADMIN));
  const isWarehouseManager = computed(() => roleCodes.value.includes(ROLE_WAREHOUSE_MANAGER));
  const isViewer = computed(() => roleCodes.value.includes(ROLE_VIEWER) && !isAdmin.value && !isWarehouseManager.value);
  const managedWarehouseId = computed(() => (isWarehouseManager.value ? user.value?.warehouseId ?? null : null));
  const defaultRoute = computed(() => resolveDefaultRouteByRoleCodes(roleCodes.value));

  function hydrate() {
    user.value = getSession();
  }

  function setUser(profile) {
    user.value = profile;
    saveSession(profile);
  }

  function logout() {
    user.value = null;
    clearSession();
  }

  function canAccessRoute(allowedRoles) {
    return canAccessAllowedRoles(roleCodes.value, allowedRoles);
  }

  return {
    user,
    isLoggedIn,
    roleCodes,
    primaryRole,
    isAdmin,
    isWarehouseManager,
    isViewer,
    managedWarehouseId,
    defaultRoute,
    hydrate,
    setUser,
    logout,
    canAccessRoute
  };
});

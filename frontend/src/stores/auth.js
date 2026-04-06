import { computed, ref } from "vue";
import { defineStore } from "pinia";
import { clearSession, getSession, saveSession } from "../utils/session";

export const useAuthStore = defineStore("auth", () => {
  const user = ref(getSession());

  const isLoggedIn = computed(() => Boolean(user.value));
  const roleCodes = computed(() => user.value?.roleCodes || []);
  const primaryRole = computed(() => roleCodes.value[0] || "VIEWER");

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

  return {
    user,
    isLoggedIn,
    roleCodes,
    primaryRole,
    hydrate,
    setUser,
    logout
  };
});

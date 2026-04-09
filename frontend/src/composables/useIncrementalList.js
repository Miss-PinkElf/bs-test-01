import { computed, proxyRefs, ref, unref, watch } from "vue";

function normalizeSource(source) {
  const raw = typeof source === "function" ? source() : unref(source);
  return Array.isArray(raw) ? raw : [];
}

export function useIncrementalList(source, options = {}) {
  const step = options.step ?? 5;
  const initialCount = options.initialCount ?? step;
  const visibleCount = ref(initialCount);

  const total = computed(() => normalizeSource(source).length);

  const visibleItems = computed(() => {
    return normalizeSource(source).slice(0, visibleCount.value);
  });

  const hasMore = computed(() => visibleCount.value < total.value);

  function resetVisibleCount() {
    visibleCount.value = initialCount;
  }

  function loadMore() {
    if (!hasMore.value) {
      return;
    }

    visibleCount.value = Math.min(visibleCount.value + step, total.value);
  }

  watch(
    () => normalizeSource(source),
    () => {
      resetVisibleCount();
    },
    { deep: true }
  );

  return proxyRefs({
    visibleCount,
    visibleItems,
    total,
    hasMore,
    resetVisibleCount,
    loadMore
  });
}

import { computed, ref, unref, watch } from "vue";

const DEFAULT_PAGE_SIZES = [5, 10, 20, 50];

function normalizeSource(source) {
  const raw = typeof source === "function" ? source() : unref(source);
  return Array.isArray(raw) ? raw : [];
}

function resolveMaxPage(total, pageSize) {
  return Math.max(1, Math.ceil(total / pageSize) || 1);
}

export function useClientPagination(source, options = {}) {
  const currentPage = ref(options.initialPage ?? 1);
  const pageSize = ref(options.initialPageSize ?? 10);
  const pageSizes = options.pageSizes ?? DEFAULT_PAGE_SIZES;

  const total = computed(() => normalizeSource(source).length);

  const pagedItems = computed(() => {
    const list = normalizeSource(source);
    const startIndex = (currentPage.value - 1) * pageSize.value;
    return list.slice(startIndex, startIndex + pageSize.value);
  });

  function clampCurrentPage() {
    const maxPage = resolveMaxPage(total.value, pageSize.value);

    if (currentPage.value > maxPage) {
      currentPage.value = maxPage;
    }
  }

  function handleCurrentChange(page) {
    currentPage.value = page;
  }

  function handleSizeChange(size) {
    pageSize.value = size;
    currentPage.value = 1;
  }

  function resetPagination() {
    currentPage.value = 1;
  }

  watch(total, clampCurrentPage);
  watch(pageSize, clampCurrentPage);

  return {
    currentPage,
    pageSize,
    pageSizes,
    total,
    pagedItems,
    handleCurrentChange,
    handleSizeChange,
    resetPagination
  };
}

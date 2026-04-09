/**
 * 列表模糊过滤：对若干文本字段做子串匹配（中英文按包含关系，ASCII 忽略大小写）。
 */
export function normalizeKeyword(raw) {
  if (raw == null) {
    return "";
  }

  return String(raw).trim();
}

export function filterRows(rows, keyword, pickTexts) {
  const k = normalizeKeyword(keyword);
  if (!k) {
    return rows;
  }

  const lower = k.toLowerCase();

  return rows.filter((row) => {
    const parts = pickTexts(row);
    return parts.some((text) => {
      if (text == null) {
        return false;
      }

      const s = String(text);
      return s.includes(k) || s.toLowerCase().includes(lower);
    });
  });
}

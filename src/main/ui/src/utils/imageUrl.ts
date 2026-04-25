/** Подготовка URL для <img src> (пробелы, протокол //). */
export function normalizeExternalImageUrl(raw: string | null | undefined): string | null {
  if (raw == null) return null;
  const t = raw.trim();
  if (!t) return null;
  if (t.startsWith('//')) return `https:${t}`;
  return t;
}

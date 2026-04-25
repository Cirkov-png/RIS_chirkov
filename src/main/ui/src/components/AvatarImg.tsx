import { useState } from 'react';
import { normalizeExternalImageUrl } from '../utils/imageUrl';

type Props = {
  src: string | null | undefined;
  alt?: string;
  className?: string;
};

/**
 * Аватар по внешней ссылке: без referrer часть CDN отдаёт картинку; при ошибке — заглушка.
 */
export function AvatarImg({ src, alt = '', className = '' }: Props) {
  const [broken, setBroken] = useState(false);
  const url = normalizeExternalImageUrl(src);
  if (!url || broken) {
    return (
      <div className={`flex items-center justify-center bg-white/10 text-2xl ${className}`} aria-hidden>
        👤
      </div>
    );
  }
  return (
    <img
      key={url}
      src={url}
      alt={alt}
      referrerPolicy="no-referrer"
      className={className}
      onError={() => setBroken(true)}
    />
  );
}

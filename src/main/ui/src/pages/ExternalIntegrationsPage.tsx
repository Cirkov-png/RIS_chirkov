import { useState } from 'react';
import { fetchMapGeocode, fetchWeather } from '../api/externalApi';
import type { MapGeocodeDto, WeatherDto } from '../types';
import { errorMessage } from '../utils/errorMessage';

/**
 * Прокси внешних API: погода (OpenWeatherMap), карта (Google Geocoding → встраиваемая карта).
 * Маршруты: /volunteer/external-services и /organizer/external-services.
 */
export function ExternalIntegrationsPage() {
  const [city, setCity] = useState('Минск');
  const [weather, setWeather] = useState<WeatherDto | null>(null);
  const [weatherErr, setWeatherErr] = useState<string | null>(null);
  const [weatherBusy, setWeatherBusy] = useState(false);

  const [address, setAddress] = useState('Минск');
  const [mapPoint, setMapPoint] = useState<MapGeocodeDto | null>(null);
  const [mapErr, setMapErr] = useState<string | null>(null);
  const [mapBusy, setMapBusy] = useState(false);

  async function loadWeather() {
    const c = city.trim();
    if (!c) return;
    setWeatherBusy(true);
    setWeatherErr(null);
    setWeather(null);
    try {
      const w = await fetchWeather(c);
      if (!w) {
        setWeatherErr('Данных нет (проверьте ключ OpenWeather на сервере или название города).');
      } else {
        setWeather(w);
      }
    } catch (e) {
      setWeatherErr(errorMessage(e));
    } finally {
      setWeatherBusy(false);
    }
  }

  async function loadMap() {
    const a = address.trim();
    if (!a) return;
    setMapBusy(true);
    setMapErr(null);
    setMapPoint(null);
    try {
      const g = await fetchMapGeocode(a);
      if (!g) {
        setMapErr(
          'Не удалось получить координаты. Задайте ключ Google: переменная GOOGLE_MAPS_API_KEY или app.google.maps-api-key в application.yml, и включите Geocoding API в Google Cloud Console.',
        );
      } else {
        setMapPoint(g);
      }
    } catch (e) {
      setMapErr(errorMessage(e));
    } finally {
      setMapBusy(false);
    }
  }

  const embedSrc =
    mapPoint != null
      ? `https://www.google.com/maps?q=${mapPoint.lat},${mapPoint.lng}&z=15&hl=ru&output=embed`
      : null;

  return (
    <div className="space-y-8 max-w-4xl">
      <p className="text-sm text-ink-light">
        Запросы идут через бэкенд: GET /api/external/weather и GET /api/external/maps/geocode. Ключи хранятся только на
        сервере.
      </p>

      <section className="bg-surface-card border border-white/10 rounded-2xl p-6 space-y-4">
        <h2 className="font-display text-xl font-semibold text-accent">Погода (OpenWeatherMap)</h2>
        <p className="text-sm text-ink-light">Удобно оценить условия перед выездом на задачу.</p>
        <div className="flex flex-col sm:flex-row gap-2">
          <input
            className="flex-1 rounded-lg bg-ink border border-white/10 px-3 py-2 text-sm"
            placeholder="Город"
            value={city}
            onChange={(e) => setCity(e.target.value)}
          />
          <button
            type="button"
            disabled={weatherBusy || !city.trim()}
            onClick={() => void loadWeather()}
            className="px-4 py-2 rounded-xl bg-accent text-ink font-semibold text-sm disabled:opacity-40"
          >
            {weatherBusy ? 'Запрос…' : 'Показать'}
          </button>
        </div>
        {weatherErr && (
          <div className="rounded-lg bg-coral/15 border border-coral/40 text-coral text-sm px-4 py-2">{weatherErr}</div>
        )}
        {weather && (
          <div className="rounded-xl border border-white/10 bg-ink/50 p-4 flex flex-wrap gap-4 items-center">
            {weather.icon ? (
              <img
                src={`https://openweathermap.org/img/wn/${weather.icon}@2x.png`}
                alt=""
                className="w-16 h-16"
                width={64}
                height={64}
              />
            ) : null}
            <div>
              <p className="font-semibold text-lg">
                {weather.city}: {Math.round(weather.temperatureCelsius)}°C
              </p>
              <p className="text-sm text-ink-light capitalize">{weather.description}</p>
              <p className="text-xs text-ink-light mt-1">
                Влажность {weather.humidity}% · ветер {weather.windSpeed} м/с
              </p>
            </div>
          </div>
        )}
      </section>

      <section className="bg-surface-card border border-white/10 rounded-2xl p-6 space-y-4">
        <h2 className="font-display text-xl font-semibold text-coral">Карта (Google)</h2>
        <p className="text-sm text-ink-light">
          Сервер вызывает Geocoding API и возвращает координаты; карта встроена через Google Maps (без отдельного
          JavaScript API ключа в браузере).
        </p>
        <div className="flex flex-col sm:flex-row gap-2">
          <input
            className="flex-1 rounded-lg bg-ink border border-white/10 px-3 py-2 text-sm"
            placeholder="Адрес или место, например: Минск, проспект Независимости 10"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
          />
          <button
            type="button"
            disabled={mapBusy || !address.trim()}
            onClick={() => void loadMap()}
            className="px-4 py-2 rounded-xl bg-surface-hover border border-white/15 font-semibold text-sm disabled:opacity-40"
          >
            {mapBusy ? 'Запрос…' : 'Показать на карте'}
          </button>
        </div>
        {mapErr && (
          <div className="rounded-lg bg-amber-500/15 border border-amber-500/40 text-amber-200 text-sm px-4 py-2">
            {mapErr}
          </div>
        )}
        {mapPoint && (
          <div className="space-y-2">
            <p className="text-sm text-stone-200">{mapPoint.formattedAddress}</p>
            <div className="rounded-xl overflow-hidden border border-white/10 aspect-video max-h-[420px]">
              {embedSrc ? (
                <iframe title="Карта" className="w-full h-full min-h-[280px]" src={embedSrc} loading="lazy" />
              ) : null}
            </div>
            <a
              href={`https://www.google.com/maps/search/?api=1&query=${mapPoint.lat},${mapPoint.lng}`}
              target="_blank"
              rel="noopener noreferrer"
              className="text-sm text-accent hover:underline"
            >
              Открыть в Google Maps
            </a>
          </div>
        )}
      </section>
    </div>
  );
}

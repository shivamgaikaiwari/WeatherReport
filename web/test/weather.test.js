// Run from the repo root: node --test web/test/*.test.js   (Node 20+, no dependencies)
import { test } from "node:test";
import assert from "node:assert/strict";
import { mapWeatherCode, toReport, MalformedWeatherDataError } from "../js/mapper.js";
import { getWeather, CityNotFoundError } from "../js/repository.js";
import { WeatherViewModel, EMPTY_QUERY_MESSAGE } from "../js/viewmodel.js";

const LOCATION = { name: "London", admin1: "England", country: "United Kingdom", latitude: 51.5, longitude: -0.12 };
const FORECAST = {
  current: { time: "2026-11-09T12:00", temperature_2m: 52.3, relative_humidity_2m: 81,
             apparent_temperature: 49.1, weather_code: 3, wind_speed_10m: 9.6 },
  daily: { time: ["2026-11-09", "2026-11-10"], weather_code: [3, 61],
           temperature_2m_max: [54, 51], temperature_2m_min: [44, 42] },
};

const fakeFetch = (routes) => async (url) => {
  const hit = Object.entries(routes).find(([prefix]) => url.startsWith(prefix));
  if (!hit) throw new TypeError("network down");
  const [status, body] = hit[1];
  return { ok: status < 400, status, json: async () => body };
};

test("weather codes map to descriptions, unknown codes fall back", () => {
  assert.deepEqual(mapWeatherCode(0), { code: 0, description: "Clear sky", icon: "☀️" });
  assert.equal(mapWeatherCode(53).description, "Drizzle");
  assert.equal(mapWeatherCode(99).description, "Thunderstorm with hail");
  assert.equal(mapWeatherCode(42).description, "Unknown");
});

test("toReport maps current conditions and daily forecast", () => {
  const r = toReport(LOCATION, FORECAST);
  assert.equal(r.cityName, "London");
  assert.equal(r.region, "England");
  assert.equal(r.current.temperature, 52.3);
  assert.equal(r.current.condition.description, "Overcast");
  assert.equal(r.daily.length, 2);
  assert.equal(r.daily[1].condition.description, "Light rain");
});

test("toReport trims daily arrays to the shortest length", () => {
  const f = structuredClone(FORECAST);
  f.daily.temperature_2m_min = [44];
  assert.equal(toReport(LOCATION, f).daily.length, 1);
});

test("toReport rejects a forecast with no current block", () => {
  assert.throws(() => toReport(LOCATION, { daily: FORECAST.daily }), MalformedWeatherDataError);
});

test("repository chains geocoding then forecast", async () => {
  const fetchImpl = fakeFetch({
    "https://geocoding-api": [200, { results: [LOCATION] }],
    "https://api.open-meteo.com": [200, FORECAST],
  });
  const r = await getWeather("London", { fetchImpl });
  assert.equal(r.cityName, "London");
});

test("repository reports an unknown city", async () => {
  const fetchImpl = fakeFetch({ "https://geocoding-api": [200, {}] });
  await assert.rejects(getWeather("Atlantis", { fetchImpl }), CityNotFoundError);
});

test("ViewModel: blank query is an error without calling the API", async () => {
  let called = false;
  const vm = new WeatherViewModel({ repository: async () => { called = true; } });
  await vm.search("   ");
  assert.deepEqual(vm.state, { kind: "error", message: EMPTY_QUERY_MESSAGE });
  assert.equal(called, false);
});

test("ViewModel: idle -> loading -> success", async () => {
  const seen = [];
  const vm = new WeatherViewModel({
    repository: async () => toReport(LOCATION, FORECAST),
    onChange: (s) => seen.push(s.kind),
  });
  assert.equal(vm.state.kind, "idle");
  await vm.search("London");
  assert.deepEqual(seen, ["loading", "success"]);
});

test("ViewModel: errors become readable messages", async () => {
  const vm = new WeatherViewModel({ repository: async () => { throw new CityNotFoundError("x"); } });
  await vm.search("Atlantis");
  assert.equal(vm.state.kind, "error");
  assert.match(vm.state.message, /Atlantis/);

  const vm2 = new WeatherViewModel({ repository: async () => { throw new TypeError("Failed to fetch"); } });
  await vm2.search("London");
  assert.match(vm2.state.message, /connection/);
});

test("ViewModel: a newer search replaces an older one", async () => {
  let resolveFirst;
  const repository = (city) => city === "Slow"
    ? new Promise((res) => { resolveFirst = () => res(toReport({ ...LOCATION, name: "Slow" }, FORECAST)); })
    : Promise.resolve(toReport(LOCATION, FORECAST));
  const vm = new WeatherViewModel({ repository });
  const first = vm.search("Slow");
  await vm.search("London");
  resolveFirst();
  await first;
  assert.equal(vm.state.report.cityName, "London");
});

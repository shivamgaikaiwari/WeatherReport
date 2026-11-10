// Mirrors WeatherViewModel.kt: holds the query and UI state, turns errors into messages.
// States: { kind: "idle" } | { kind: "loading" } | { kind: "success", report } | { kind: "error", message }
import { MalformedWeatherDataError } from "./mapper.js";
import { CityNotFoundError, HttpError, getWeather } from "./repository.js";

export const EMPTY_QUERY_MESSAGE = "Enter a city name to search.";
export const NETWORK_ERROR_MESSAGE = "Couldn't reach the weather service. Check your connection and try again.";

export function toUserMessage(err, city) {
  if (err instanceof CityNotFoundError) return `No city called “${city}” was found. Check the spelling or try a nearby larger city.`;
  if (err instanceof HttpError) return `The weather service returned an error (${err.status}). Try again in a moment.`;
  if (err instanceof MalformedWeatherDataError) return "The weather service sent incomplete data. Try again in a moment.";
  if (err instanceof TypeError) return NETWORK_ERROR_MESSAGE; // fetch network failure
  return err?.message || "Something went wrong. Try again.";
}

export class WeatherViewModel {
  constructor({ repository = getWeather, onChange = () => {} } = {}) {
    this.repository = repository;
    this.onChange = onChange;
    this.state = { kind: "idle" };
    this.units = "imperial";
    this.lastCity = "";
    this.controller = null;
  }

  set(state) {
    this.state = state;
    this.onChange(state);
  }

  async search(query) {
    const city = (query ?? "").trim();
    if (!city) return this.set({ kind: "error", message: EMPTY_QUERY_MESSAGE });

    this.controller?.abort(); // a newer search replaces an in-flight one
    const controller = (this.controller = new AbortController());
    this.lastCity = city;
    this.set({ kind: "loading" });
    try {
      const report = await this.repository(city, { units: this.units, signal: controller.signal });
      if (!controller.signal.aborted) this.set({ kind: "success", report, units: this.units });
    } catch (err) {
      if (err?.name === "AbortError" || controller.signal.aborted) return;
      this.set({ kind: "error", message: toUserMessage(err, city) });
    }
  }

  setUnits(units) {
    if (units === this.units) return;
    this.units = units;
    if (this.state.kind === "success") this.search(this.lastCity);
  }
}

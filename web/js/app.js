// View layer: renders ViewModel state into the page (the role WeatherScreen.kt plays on Android).
import { WeatherViewModel } from "./viewmodel.js";

const $ = (sel) => document.querySelector(sel);
const form = $("#search");
const input = $("#q");
const result = $("#result");
const root = document.documentElement;

const vm = new WeatherViewModel({ onChange: render });

// ---------- helpers ----------
const esc = (s) => String(s).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
const round = (n) => Math.round(n);
const toF = (t, units) => (units === "metric" ? t * 9 / 5 + 32 : t);

/** Where a temperature sits between cold slate (0%) and warm sand (100%): 15°F .. 95°F. */
function warmth(tempF) {
  const t = Math.min(1, Math.max(0, (tempF - 15) / 80));
  return `${Math.round(t * 100)}%`;
}

function dayLabel(iso, i) {
  if (i === 0) return "Today";
  const d = new Date(`${iso}T12:00:00`);
  return Number.isNaN(d.getTime()) ? iso : d.toLocaleDateString("en-US", { weekday: "short" });
}

// ---------- render ----------
function render(state) {
  result.dataset.state = state.kind;
  if (state.kind === "loading") {
    result.innerHTML = `<p class="status"><span class="spinner" aria-hidden="true"></span>Looking up the forecast…</p>`;
    return;
  }
  if (state.kind === "error") {
    result.innerHTML = `<p class="status error" role="alert">${esc(state.message)}</p>`;
    return;
  }
  if (state.kind !== "success") {
    result.innerHTML = "";
    return;
  }

  const { report, units } = state;
  const c = report.current;
  const deg = units === "metric" ? "°C" : "°F";
  const speed = units === "metric" ? "km/h" : "mph";
  root.style.setProperty("--t", warmth(toF(c.temperature, units)));

  const place = [report.region, report.country].filter(Boolean).join(", ");
  const lo = Math.min(...report.daily.map((d) => d.min));
  const hi = Math.max(...report.daily.map((d) => d.max));
  const span = Math.max(1, hi - lo);

  const rows = report.daily.map((d, i) => {
    const left = ((d.min - lo) / span) * 100;
    const width = Math.max(2, ((d.max - d.min) / span) * 100);
    const t1 = warmth(toF(d.min, units));
    const t2 = warmth(toF(d.max, units));
    return `<li>
      <span class="day">${dayLabel(d.date, i)}</span>
      <span class="icon" role="img" aria-label="${esc(d.condition.description)}">${d.condition.icon}</span>
      <span class="desc">${esc(d.condition.description)}</span>
      <span class="lo" aria-label="Low">${round(d.min)}°</span>
      <span class="track" aria-hidden="true"><span class="bar" style="left:${left}%;width:${width}%;--t1:${t1};--t2:${t2}"></span></span>
      <span class="hi" aria-label="High">${round(d.max)}°</span>
    </li>`;
  }).join("");

  result.innerHTML = `
    <article class="now">
      <div class="where">
        <h2>${esc(report.cityName)}</h2>
        ${place ? `<p>${esc(place)}</p>` : ""}
      </div>
      <div class="reading">
        <span class="big-icon" role="img" aria-label="${esc(c.condition.description)}">${c.condition.icon}</span>
        <span class="temp">${round(c.temperature)}<span class="unit">${deg}</span></span>
        <span class="cond">${esc(c.condition.description)}</span>
      </div>
      <dl class="stats">
        <div><dt>Feels like</dt><dd>${round(c.feelsLike)}${deg}</dd></div>
        <div><dt>Humidity</dt><dd>${c.humidityPercent}%</dd></div>
        <div><dt>Wind</dt><dd>${round(c.windSpeed)} ${speed}</dd></div>
      </dl>
      <div class="units" role="group" aria-label="Units">
        <button type="button" data-units="imperial" aria-pressed="${units === "imperial"}">°F</button>
        <button type="button" data-units="metric" aria-pressed="${units === "metric"}">°C</button>
      </div>
    </article>
    <section class="week" aria-label="7-day forecast">
      <h3>Next 7 days</h3>
      <ol>${rows}</ol>
    </section>`;
}

// ---------- events ----------
function search(city) {
  input.value = city;
  const url = new URL(location.href);
  url.searchParams.set("city", city);
  history.replaceState(null, "", url);
  vm.search(city);
}

form.addEventListener("submit", (e) => {
  e.preventDefault();
  input.blur();
  search(input.value);
});
$("#suggest").addEventListener("click", (e) => {
  const b = e.target.closest("button[data-city]");
  if (b) search(b.dataset.city);
});
result.addEventListener("click", (e) => {
  const b = e.target.closest("button[data-units]");
  if (b) vm.setUnits(b.dataset.units);
});

// Open with a real forecast so there's something to see immediately; ?city= links work too.
search(new URLSearchParams(location.search).get("city") || "London");

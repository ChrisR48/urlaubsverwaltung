// disabling date-fns#format is ok since we're formatting dates for api requests
// eslint-disable-next-line @urlaubsverwaltung/no-date-fns
import { isAfter, format, getYear } from "date-fns";
import formatNumber from "./format-number";
import sendGetDaysRequestForTurnOfTheYear from "./send-get-days-request-for-turn-of-the-year";
import { getJSON } from "./fetch";

export default async function sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, elementSelector) {
  const element = document.querySelector(elementSelector);
  element.innerHTML = "";

  if ((!startDate && !toDate) || isAfter(startDate, toDate)) {
    return;
  }

  const workDays = await getWorkdaysForDateRange(urlPrefix, dayLength, personId, startDate, toDate);

  let text;

  if (Number.isNaN(workDays)) {
    text = window.uv.i18n["application.applier.invalidPeriod"];
  } else if (workDays === "1.0") {
    text = formatNumber(workDays) + " " + window.uv.i18n["application.applier.day"];
  } else {
    text = formatNumber(workDays) + " " + window.uv.i18n["application.applier.days"];
  }

  element.innerHTML = text;
  let closest = element.closest("#days-count");
  if (closest) {
    closest.classList.remove("hidden");
  }

  if (getYear(startDate) !== getYear(toDate)) {
    element.innerHTML += '<span class="days-turn-of-the-year"></span>';
    await sendGetDaysRequestForTurnOfTheYear(
      urlPrefix,
      startDate,
      toDate,
      dayLength,
      personId,
      `${elementSelector} .days-turn-of-the-year`,
    );
  }
}

async function getWorkdaysForDateRange(urlPrefix, dayLength, personId, fromDate, toDate) {
  const startDate = format(fromDate, "yyyy-MM-dd");
  const endDate = format(toDate, "yyyy-MM-dd");
  let url = urlPrefix + "/persons/" + personId + "/workdays?from=" + startDate + "&to=" + endDate;
  if (dayLength) {
    url = url + "&length=" + dayLength;
  }

  const json = await getJSON(url);
  return json.workDays;
}

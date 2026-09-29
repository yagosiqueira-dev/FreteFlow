export interface Period {
  label: string;
  startDate: string;
  endDate: string;
}

function pad(n: number): string {
  return n.toString().padStart(2, "0");
}

function fmt(year: number, month: number, day: number): string {
  return `${year}-${pad(month)}-${pad(day)}`;
}

function fmtLabel(year: number, month: number, day: number): string {
  return `${pad(day)}/${pad(month)}/${year}`;
}

export function getRecentPeriods(count: number = 4): Period[] {
  const periods: Period[] = [];

  const today = new Date();

  let year = today.getFullYear();
  let month = today.getMonth() + 1;
  const day = today.getDate();

  // Descobre se estamos no período 11-25 ou 26-10 do mês atual
  let cursor: {
    year: number;
    month: number;
    isFirstHalf: boolean;
  };

  if (day >= 11 && day <= 25) {
    cursor = { year, month, isFirstHalf: true };
  } else if (day >= 26) {
    cursor = { year, month, isFirstHalf: false };
  } else {
    // Dias 1-10 pertencem ao período 26-10 que começou no mês anterior
    month -= 1;

    if (month === 0) {
      month = 12;
      year -= 1;
    }

    cursor = { year, month, isFirstHalf: false };
  }

  for (let i = 0; i < count; i++) {
    if (cursor.isFirstHalf) {
      periods.push({
        label: `${fmtLabel(cursor.year, cursor.month, 11)} → ${fmtLabel(
          cursor.year,
          cursor.month,
          25
        )}`,
        startDate: fmt(cursor.year, cursor.month, 11),
        endDate: fmt(cursor.year, cursor.month, 25),
      });

      cursor = {
        year: cursor.year,
        month: cursor.month,
        isFirstHalf: false,
      };

      cursor.month -= 1;

      if (cursor.month === 0) {
        cursor.month = 12;
        cursor.year -= 1;
      }
    } else {
      const endMonth = cursor.month + 1 > 12 ? 1 : cursor.month + 1;
      const endYear =
        cursor.month + 1 > 12 ? cursor.year + 1 : cursor.year;

      periods.push({
        label: `${fmtLabel(cursor.year, cursor.month, 26)} → ${fmtLabel(
          endYear,
          endMonth,
          10
        )}`,
        startDate: fmt(cursor.year, cursor.month, 26),
        endDate: fmt(endYear, endMonth, 10),
      });

      cursor = {
        ...cursor,
        isFirstHalf: true,
      };
    }
  }

  return periods;
}
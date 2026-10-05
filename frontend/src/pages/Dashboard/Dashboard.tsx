import { useMemo, useState } from "react";
import { useQuery, keepPreviousData } from "@tanstack/react-query";
import {
  TrendingUp,
  TrendingDown,
  DollarSign,
  Package,
  XCircle,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";
import { listFreights } from "../../api/freights";
import { listExpenses } from "../../api/expenses";
import type { Freight } from "../../types/freight";

function formatCurrency(value: number) {
  return value.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function formatCurrencyShort(value: number) {
  return value.toLocaleString("pt-BR", {
    style: "currency",
    currency: "BRL",
    maximumFractionDigits: 0,
  });
}

function pad(n: number) {
  return String(n).padStart(2, "0");
}

function toLocalDateTime(date: Date) {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

function toDateOnly(date: Date) {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

function monthKeyFromDateString(dateStr: string) {
  const [year, month] = dateStr.slice(0, 10).split("-");
  return `${year}-${month}`;
}

function monthLabel(key: string) {
  const [year, month] = key.split("-").map(Number);
  const d = new Date(year, month - 1, 1);
  return d.toLocaleDateString("pt-BR", { month: "short" }).replace(".", "");
}

export default function Dashboard() {
  const [viewedDate, setViewedDate] = useState(() => new Date());

  const monthStart = new Date(
    viewedDate.getFullYear(),
    viewedDate.getMonth(),
    1,
    0,
    0,
    0,
  );
  const monthEnd = new Date(
    viewedDate.getFullYear(),
    viewedDate.getMonth() + 1,
    0,
    23,
    59,
    59,
  );
  const rangeStart = new Date(
    viewedDate.getFullYear(),
    viewedDate.getMonth() - 5,
    1,
    0,
    0,
    0,
  );

  const monthStartDateTime = toLocalDateTime(monthStart);
  const monthEndDateTime = toLocalDateTime(monthEnd);
  const rangeStartDateTime = toLocalDateTime(rangeStart);

  const monthStartDate = toDateOnly(monthStart);
  const monthEndDate = toDateOnly(monthEnd);
  const rangeStartDate = toDateOnly(rangeStart);

  function goToPreviousMonth() {
    setViewedDate((d) => new Date(d.getFullYear(), d.getMonth() - 1, 1));
  }

  function goToNextMonth() {
    setViewedDate((d) => new Date(d.getFullYear(), d.getMonth() + 1, 1));
  }

  const { data: freightsData, isFetching: fetchingFreights } = useQuery({
    queryKey: ["dashboard-freights", rangeStartDateTime, monthEndDateTime],
    queryFn: () =>
      listFreights(0, {
        startDate: rangeStartDateTime,
        endDate: monthEndDateTime,
        size: 1000,
      }),
    placeholderData: keepPreviousData,
  });

  const { data: expensesData, isFetching: fetchingExpenses } = useQuery({
    queryKey: ["dashboard-expenses", rangeStartDate, monthEndDate],
    queryFn: () =>
      listExpenses(0, {
        startDate: rangeStartDate,
        endDate: monthEndDate,
        size: 1000,
      }),
    placeholderData: keepPreviousData,
  });

  const freights = freightsData?.content ?? [];
  const expenses = (expensesData?.content ?? []).filter((e) => e.enabled);
  const hasData = freightsData !== undefined && expensesData !== undefined;
  const isDimmed = (fetchingFreights || fetchingExpenses) && hasData;

  const currentMonthFreights = useMemo(
    () =>
      freights.filter(
        (f) =>
          f.freightDate >= monthStartDateTime &&
          f.freightDate <= monthEndDateTime,
      ),
    [freights, monthStartDateTime, monthEndDateTime],
  );
  const currentMonthExpenses = useMemo(
    () =>
      expenses.filter((e) => {
        const d = e.expenseDate.slice(0, 10);
        return d >= monthStartDate && d <= monthEndDate;
      }),
    [expenses, monthStartDate, monthEndDate],
  );

  const deliveredThisMonth = currentMonthFreights.filter(
    (f) => f.status === "DELIVERED",
  );

  const revenue = deliveredThisMonth.reduce(
    (sum, f) => sum + f.freightValue,
    0,
  );
  const expensesTotal = currentMonthExpenses.reduce(
    (sum, e) => sum + e.amount,
    0,
  );
  const netProfit = revenue - expensesTotal;

  const recentCanceled = useMemo(
    () =>
      [...freights]
        .filter((f) => f.status === "CANCELED")
        .sort((a, b) => b.freightDate.localeCompare(a.freightDate))
        .slice(0, 5),
    [freights],
  );

  const expensesByCategory = useMemo(() => {
    const map = new Map<string, number>();
    currentMonthExpenses.forEach((e) => {
      map.set(e.description, (map.get(e.description) ?? 0) + e.amount);
    });
    return [...map.entries()].sort((a, b) => b[1] - a[1]);
  }, [currentMonthExpenses]);

  const driverRanking = useMemo(() => {
    const map = new Map<string, number>();
    deliveredThisMonth.forEach((f) => {
      map.set(f.driverName, (map.get(f.driverName) ?? 0) + 1);
    });
    return [...map.entries()].sort((a, b) => b[1] - a[1]).slice(0, 5);
  }, [deliveredThisMonth]);

  const monthlyTrend = useMemo(() => {
    const revenueMap = new Map<string, number>();
    const expenseMap = new Map<string, number>();

    freights
      .filter((f) => f.status === "DELIVERED")
      .forEach((f) => {
        const key = monthKeyFromDateString(f.freightDate);
        revenueMap.set(key, (revenueMap.get(key) ?? 0) + f.freightValue);
      });

    expenses.forEach((e) => {
      const key = monthKeyFromDateString(e.expenseDate);
      expenseMap.set(key, (expenseMap.get(key) ?? 0) + e.amount);
    });

    const keys: string[] = [];
    for (let i = 5; i >= 0; i--) {
      const d = new Date(
        viewedDate.getFullYear(),
        viewedDate.getMonth() - i,
        1,
      );
      keys.push(`${d.getFullYear()}-${pad(d.getMonth() + 1)}`);
    }

    return keys.map((key) => ({
      key,
      label: monthLabel(key),
      revenue: revenueMap.get(key) ?? 0,
      expense: expenseMap.get(key) ?? 0,
    }));
  }, [freights, expenses, viewedDate]);

  const maxTrendValue = Math.max(
    1,
    ...monthlyTrend.flatMap((m) => [m.revenue, m.expense]),
  );

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-display font-semibold text-ink">
            Início
          </h1>
          <p className="text-ink/60">
            Resumo de{" "}
            {viewedDate.toLocaleDateString("pt-BR", {
              month: "long",
              year: "numeric",
            })}
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={goToPreviousMonth}
            className="p-2 border border-ink/20 rounded hover:bg-white"
            title="Mês anterior"
          >
            <ChevronLeft size={16} />
          </button>
          <span className="text-sm text-ink/70 w-32 text-center capitalize">
            {viewedDate.toLocaleDateString("pt-BR", {
              month: "long",
              year: "numeric",
            })}
          </span>
          <button
            onClick={goToNextMonth}
            className="p-2 border border-ink/20 rounded hover:bg-white"
            title="Próximo mês"
          >
            <ChevronRight size={16} />
          </button>
        </div>
      </div>

      <div
        className={`transition-opacity ${isDimmed ? "opacity-60" : "opacity-100"}`}
      >
        <div className="grid grid-cols-4 gap-4 mb-6">
          <div className="bg-white rounded p-5">
            <div className="flex items-center gap-2 text-ink/50 text-sm mb-2">
              <DollarSign size={16} />
              Faturamento do mês
            </div>
            <p className="text-2xl font-display font-semibold text-ink">
              {formatCurrency(revenue)}
            </p>
          </div>

          <div className="bg-white rounded p-5">
            <div className="flex items-center gap-2 text-ink/50 text-sm mb-2">
              <TrendingDown size={16} />
              Despesas do mês
            </div>
            <p className="text-2xl font-display font-semibold text-ink">
              {formatCurrency(expensesTotal)}
            </p>
          </div>

          <div
            className={`bg-white rounded p-5 border-l-4 ${netProfit >= 0 ? "border-highway-green" : "border-alert-red"}`}
          >
            <div className="flex items-center gap-2 text-ink/50 text-sm mb-2">
              <TrendingUp size={16} />
              Lucro líquido
            </div>
            <p
              className={`text-2xl font-display font-semibold ${netProfit >= 0 ? "text-highway-green" : "text-alert-red"}`}
            >
              {formatCurrency(netProfit)}
            </p>
          </div>

          <div className="bg-white rounded p-5">
            <div className="flex items-center gap-2 text-ink/50 text-sm mb-2">
              <Package size={16} />
              Fretes entregues
            </div>
            <p className="text-2xl font-display font-semibold text-ink">
              {deliveredThisMonth.length}
            </p>
          </div>
        </div>

        <div className="bg-white rounded p-6 mb-6">
          <h2 className="font-display font-semibold text-ink mb-4">
            Faturamento x Despesas (6 meses)
          </h2>
          <div className="grid grid-cols-6 gap-2 h-48 items-end">
            {monthlyTrend.map((m) => (
              <div
                key={m.key}
                className="flex flex-col items-center gap-2 h-full justify-end"
              >
                <div className="w-full flex items-end justify-center gap-1 h-36">
                  <div className="w-2/5 h-full flex flex-col items-center justify-end">
                    {m.revenue > 0 && (
                      <span className="shrink-0 mb-1 whitespace-nowrap text-[11px] font-semibold text-highway-green">
                        {formatCurrencyShort(m.revenue)}
                      </span>
                    )}
                    <div
                      className="shrink-0 w-full bg-highway-green rounded-t"
                      style={{ height: `${(m.revenue / maxTrendValue) * 85}%` }}
                      title={formatCurrency(m.revenue)}
                    />
                  </div>

                  <div className="w-2/5 h-full flex flex-col items-center justify-end">
                    {m.expense > 0 && (
                      <span className="shrink-0 mb-1 whitespace-nowrap text-[11px] font-semibold text-alert-red">
                        {formatCurrencyShort(m.expense)}
                      </span>
                    )}
                    <div
                      className="shrink-0 w-full bg-alert-red rounded-t"
                      style={{ height: `${(m.expense / maxTrendValue) * 85}%` }}
                      title={formatCurrency(m.expense)}
                    />
                  </div>
                </div>
                <span className="text-xs text-ink/50 capitalize">
                  {m.label}
                </span>
              </div>
            ))}
          </div>

          <div className="flex gap-4 mt-4 text-xs text-ink/60">
            <span className="flex items-center gap-1">
              <span className="w-2.5 h-2.5 rounded-sm bg-highway-green inline-block" />{" "}
              Faturamento
            </span>
            <span className="flex items-center gap-1">
              <span className="w-2.5 h-2.5 rounded-sm bg-alert-red inline-block" />{" "}
              Despesas
            </span>
          </div>
        </div>

        <div className="grid grid-cols-3 gap-4">
          <div className="bg-white rounded p-6">
            <h2 className="font-display font-semibold text-ink mb-4 flex items-center gap-2">
              <XCircle size={16} className="text-alert-red" />
              Cancelamentos recentes
            </h2>
            {recentCanceled.length === 0 ? (
              <p className="text-sm text-ink/50">
                Nenhum cancelamento recente.
              </p>
            ) : (
              <ul className="flex flex-col gap-3 text-sm">
                {recentCanceled.map((f: Freight) => (
                  <li
                    key={f.id}
                    className="flex justify-between border-b border-ink/5 pb-2 last:border-0"
                  >
                    <span>
                      {f.driverName} — {f.origin} → {f.destinations}
                    </span>
                    <span className="text-ink/50">
                      {new Date(f.freightDate).toLocaleDateString("pt-BR")}
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="bg-white rounded p-6">
            <h2 className="font-display font-semibold text-ink mb-4">
              Despesas por categoria
            </h2>
            {expensesByCategory.length === 0 ? (
              <p className="text-sm text-ink/50">Sem despesas no mês.</p>
            ) : (
              <ul className="flex flex-col gap-3 text-sm">
                {expensesByCategory.map(([category, total]) => (
                  <li
                    key={category}
                    className="flex items-center justify-between"
                  >
                    <span className="text-ink/80">{category}</span>
                    <span className="font-display text-ink">
                      {formatCurrency(total)}
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="bg-white rounded p-6">
            <h2 className="font-display font-semibold text-ink mb-4">
              Top motoristas do mês
            </h2>
            {driverRanking.length === 0 ? (
              <p className="text-sm text-ink/50">Sem fretes entregues ainda.</p>
            ) : (
              <ul className="flex flex-col gap-3 text-sm">
                {driverRanking.map(([name, count], i) => (
                  <li key={name} className="flex items-center justify-between">
                    <span className="text-ink/80">
                      {i + 1}. {name}
                    </span>
                    <span className="font-display text-route-orange font-medium">
                      {count}
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

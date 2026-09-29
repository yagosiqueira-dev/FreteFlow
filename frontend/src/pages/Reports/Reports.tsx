import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Download, Truck, User } from "lucide-react";
import { listDrivers } from "../../api/drivers";
import { listVehicles } from "../../api/vehicles";
import { getBiWeeklyReport, getVehicleProfitReport, exportVehicleProfitReport } from "../../api/reports";
import { getRecentPeriods } from "../../utils/periods";
import { formatCurrency, formatDate } from "../../utils/currency";

type Tab = "driver" | "vehicle";

export default function Reports() {
  const [tab, setTab] = useState<Tab>("driver");

  return (
    <div className="p-4 md:p-8 max-w-2xl mx-auto">
      <h1 className="text-2xl font-display font-semibold text-ink mb-4">
        Relatórios
      </h1>

      <div className="flex gap-2 mb-6 bg-white rounded p-1">
        <button
          onClick={() => setTab("driver")}
          className={`flex-1 flex items-center justify-center gap-2 py-2 rounded text-sm font-medium transition-colors ${
            tab === "driver" ? "bg-route-orange text-white" : "text-ink/60"
          }`}
        >
          <User size={16} />
          Por Motorista
        </button>
        <button
          onClick={() => setTab("vehicle")}
          className={`flex-1 flex items-center justify-center gap-2 py-2 rounded text-sm font-medium transition-colors ${
            tab === "vehicle" ? "bg-route-orange text-white" : "text-ink/60"
          }`}
        >
          <Truck size={16} />
          Por Caminhão
        </button>
      </div>

      {tab === "driver" ? <DriverReportTab /> : <VehicleReportTab />}
    </div>
  );
}

function DriverReportTab() {
  const [driverId, setDriverId] = useState("");
  const [period, setPeriod] = useState<{ start: string; end: string } | null>(null);
  const periods = getRecentPeriods();

  const { data: driversData } = useQuery({
    queryKey: ["drivers"],
    queryFn: listDrivers,
  });
  const activeDrivers = driversData?.content.filter((d) => d.enabled) ?? [];

  const { data: report, isLoading, isError } = useQuery({
    queryKey: ["bi-weekly-report", driverId, period],
    queryFn: () => getBiWeeklyReport(driverId, period!.start, period!.end),
    enabled: Boolean(driverId && period),
  });

  return (
    <div className="flex flex-col gap-4">
      <select
        value={driverId}
        onChange={(e) => setDriverId(e.target.value)}
        className="w-full border border-ink/20 px-3 py-3 rounded bg-white text-sm"
      >
        <option value="">Selecione o motorista</option>
        {activeDrivers.map((d) => (
          <option key={d.id} value={d.id}>{d.name}</option>
        ))}
      </select>

      <div className="grid grid-cols-2 gap-2">
        {periods.map((p) => (
          <button
            key={p.startDate}
            onClick={() => setPeriod({ start: p.startDate, end: p.endDate })}
            className={`px-3 py-2 rounded text-sm border ${
              period?.start === p.startDate
                ? "bg-route-orange text-white border-route-orange"
                : "bg-white text-ink/70 border-ink/20"
            }`}
          >
            {p.label}
          </button>
        ))}
      </div>

      {isLoading && <p className="text-ink/60 text-sm">Carregando...</p>}
      {isError && <p className="text-alert-red text-sm">Erro ao gerar relatório.</p>}

      {report && (
        <>
          <div className="bg-white rounded p-5 border-l-4 border-highway-green">
            <p className="text-xs text-ink/50 uppercase">Total no período</p>
            <p className="text-3xl font-display font-semibold text-highway-green">
              {formatCurrency(report.totalAmount)}
            </p>
            <p className="text-xs text-ink/50 mt-1">{report.freights.length} frete(s)</p>
          </div>

          <div className="flex flex-col gap-2">
            {report.freights.map((f, i) => (
              <div key={i} className="bg-white rounded p-4 flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-ink">{formatDate(f.date)}</p>
                  <p className="text-xs text-ink/60">{f.loadingLocation} → {f.fullRoute}</p>
                </div>
                <p className="font-display font-semibold text-ink">{formatCurrency(f.value)}</p>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}

function VehicleReportTab() {
  const [vehicleId, setVehicleId] = useState("");
  const [period, setPeriod] = useState<{ start: string; end: string } | null>(null);
  const [exporting, setExporting] = useState(false);
  const periods = getRecentPeriods();

  const { data: vehiclesData } = useQuery({
    queryKey: ["vehicles"],
    queryFn: listVehicles,
  });
  const activeVehicles = vehiclesData?.content.filter((v) => v.enabled) ?? [];

  const { data: report, isLoading, isError } = useQuery({
    queryKey: ["vehicle-profit-report", vehicleId, period],
    queryFn: () => getVehicleProfitReport(vehicleId, period!.start, period!.end),
    enabled: Boolean(vehicleId && period),
  });

  async function handleExport() {
    if (!vehicleId || !period) return;
    setExporting(true);
    try {
      await exportVehicleProfitReport(vehicleId, period.start, period.end);
    } finally {
      setExporting(false);
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <select
        value={vehicleId}
        onChange={(e) => setVehicleId(e.target.value)}
        className="w-full border border-ink/20 px-3 py-3 rounded bg-white text-sm"
      >
        <option value="">Selecione o veículo</option>
        {activeVehicles.map((v) => (
          <option key={v.id} value={v.id}>{v.licensePlate} — {v.model}</option>
        ))}
      </select>

      <div className="grid grid-cols-2 gap-2">
        {periods.map((p) => (
          <button
            key={p.startDate}
            onClick={() => setPeriod({ start: p.startDate, end: p.endDate })}
            className={`px-3 py-2 rounded text-sm border ${
              period?.start === p.startDate
                ? "bg-route-orange text-white border-route-orange"
                : "bg-white text-ink/70 border-ink/20"
            }`}
          >
            {p.label}
          </button>
        ))}
      </div>

      {isLoading && <p className="text-ink/60 text-sm">Carregando...</p>}
      {isError && <p className="text-alert-red text-sm">Erro ao gerar relatório.</p>}

      {report && (
        <>
          <div className="grid grid-cols-1 gap-3">
            <div className="bg-white rounded p-4">
              <p className="text-xs text-ink/50 uppercase">Total em fretes</p>
              <p className="text-xl font-display font-semibold text-ink">
                {formatCurrency(report.totalFreightValue)}
              </p>
            </div>
            <div className="bg-white rounded p-4">
              <p className="text-xs text-ink/50 uppercase">Total em despesas</p>
              <p className="text-xl font-display font-semibold text-alert-red">
                {formatCurrency(report.totalExpenses)}
              </p>
            </div>
            <div
              className={`bg-white rounded p-5 border-l-4 ${
                report.netProfit >= 0 ? "border-highway-green" : "border-alert-red"
              }`}
            >
              <p className="text-xs text-ink/50 uppercase">Lucro líquido</p>
              <p
                className={`text-3xl font-display font-semibold ${
                  report.netProfit >= 0 ? "text-highway-green" : "text-alert-red"
                }`}
              >
                {formatCurrency(report.netProfit)}
              </p>
            </div>
          </div>

          <button
            onClick={handleExport}
            disabled={exporting}
            className="flex items-center justify-center gap-2 py-3 bg-route-orange text-white rounded font-medium text-sm disabled:opacity-50"
          >
            <Download size={16} />
            {exporting ? "Gerando planilha..." : "Baixar relatório em Excel"}
          </button>
        </>
      )}
    </div>
  );
}
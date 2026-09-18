import { useState, useMemo } from "react";
import { useAuth } from "../../hooks/useAuth";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  Plus,
  ChevronLeft,
  ChevronRight,
  Play,
  CheckCircle2,
  XCircle,
} from "lucide-react";
import { AxiosError } from "axios";
import {
  listFreights,
  createFreight,
  updateFreightStatus,
} from "../../api/freights";
import { listDrivers } from "../../api/drivers";
import { listVehicles } from "../../api/vehicles";
import { listStores } from "../../api/stores";
import type { FreightRequest, FreightStatus } from "../../types/freight";

function formatCurrency(value: number) {
  return value.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function formatDate(value: string) {
  return new Date(value).toLocaleString("pt-BR", {
    dateStyle: "short",
    timeStyle: "short",
  });
}

const statusLabels: Record<FreightStatus, string> = {
  PENDING: "Pendente",
  IN_PROGRESS: "Em andamento",
  DELIVERED: "Entregue",
  CANCELED: "Cancelado",
};

const statusColors: Record<FreightStatus, string> = {
  PENDING: "text-ink/60",
  IN_PROGRESS: "text-route-orange",
  DELIVERED: "text-highway-green",
  CANCELED: "text-alert-red",
};

export default function Freights() {
  const [showForm, setShowForm] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [selectedStores, setSelectedStores] = useState<string[]>([]);
  const [page, setPage] = useState(0);
  const queryClient = useQueryClient();
  const { role } = useAuth();

  const { data, isLoading, isError } = useQuery({
    queryKey: ["freights", page],
    queryFn: () => listFreights(page),
  });

  const { data: driversData } = useQuery({
    queryKey: ["drivers-dropdown"],
    queryFn: listDrivers,
    enabled: showForm,
  });

  const { data: vehiclesData } = useQuery({
    queryKey: ["vehicles-dropdown"],
    queryFn: listVehicles,
    enabled: showForm,
  });

  const { data: storesData } = useQuery({
    queryKey: ["stores-dropdown"],
    queryFn: () => listStores(0, {}, 100),
    enabled: showForm,
  });

  const createMutation = useMutation({
    mutationFn: createFreight,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["freights"] });
      setShowForm(false);
      setFormError(null);
      setSelectedStores([]);
    },
    onError: () => {
      setFormError(
        "Não foi possível salvar o frete. Verifique os dados informados.",
      );
    },
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: FreightStatus }) =>
      updateFreightStatus(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["freights"] });
      setActionError(null);
    },
    onError: (err: AxiosError) => {
      if (err.response?.status === 403) {
        setActionError(
          "Você não tem permissão para alterar o status deste frete.",
        );
      } else {
        setActionError("Não foi possível alterar o status do frete.");
      }
    },
  });

  const groupedStores = useMemo(() => {
    if (!storesData) return {};
    const sorted = [...storesData.content]
      .filter((s) => s.enabled)
      .sort((a, b) => a.name.localeCompare(b.name));

    return sorted.reduce<Record<string, typeof sorted>>((acc, s) => {
      (acc[s.origin] ??= []).push(s);
      return acc;
    }, {});
  }, [storesData]);

  const selectedOrigin = storesData?.content.find((s) =>
    selectedStores.includes(s.id),
  )?.origin;

  function toggleStore(id: string) {
    setSelectedStores((prev) =>
      prev.includes(id) ? prev.filter((s) => s !== id) : [...prev, id],
    );
  }

  function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setFormError(null);

    if (selectedStores.length === 0) {
      setFormError("Selecione pelo menos uma loja.");
      return;
    }

    const formData = new FormData(e.currentTarget);
    const freightDate = formData.get("freightDate") as string;

    const payload: FreightRequest = {
      driverId: formData.get("driverId") as string,
      vehicleId: formData.get("vehicleId") as string,
      storeIds: selectedStores,
      freightDate: `${freightDate}:00`,
    };

    createMutation.mutate(payload);
  }

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-display font-semibold text-ink">Fretes</h1>
        <button
          onClick={() => {
            setShowForm(!showForm);
            setFormError(null);
          }}
          className="flex items-center gap-2 px-4 py-2 bg-route-orange text-white rounded font-medium text-sm"
        >
          <Plus size={16} />
          Novo frete
        </button>
      </div>

      {actionError && (
        <div className="mb-4 px-4 py-3 bg-alert-red/10 border border-alert-red/30 rounded text-alert-red text-sm">
          {actionError}
        </div>
      )}

      {showForm && (
        <form
          onSubmit={handleSubmit}
          className="bg-white p-6 rounded mb-6 grid grid-cols-2 gap-4 max-w-2xl"
        >
          <div>
            <label className="block text-xs text-ink/60 mb-1">Motorista</label>
            <select
              name="driverId"
              required
              className="w-full border border-ink/20 px-3 py-2 rounded text-sm"
            >
              <option value="">Selecione</option>
              {driversData?.content
                .filter((d) => d.enabled)
                .map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.name}
                  </option>
                ))}
            </select>
          </div>

          <div>
            <label className="block text-xs text-ink/60 mb-1">Veículo</label>
            <select
              name="vehicleId"
              required
              className="w-full border border-ink/20 px-3 py-2 rounded text-sm"
            >
              <option value="">Selecione</option>
              {vehiclesData?.content
                .filter((v) => v.enabled)
                .map((v) => (
                  <option key={v.id} value={v.id}>
                    {v.licensePlate} — {v.model}
                  </option>
                ))}
            </select>
          </div>

          <div className="col-span-2">
            <label className="block text-xs text-ink/60 mb-1">
              Data do frete
            </label>
            <input
              name="freightDate"
              type="datetime-local"
              required
              className="w-full border border-ink/20 px-3 py-2 rounded text-sm"
            />
          </div>

          <div className="col-span-2">
            <label className="block text-xs text-ink/60 mb-1">
              Lojas (rotas) — todas devem ter a mesma origem
            </label>
            <div className="border border-ink/20 rounded max-h-48 overflow-y-auto p-2">
              {Object.entries(groupedStores)
                .sort(([a], [b]) => a.localeCompare(b))
                .map(([origin, stores]) => (
                  <div key={origin} className="mb-2 last:mb-0">
                    <p className="text-sm font-display font-semibold text-ink bg-concrete border-l-2 border-route-orange px-2 py-1 mb-1 -mx-2">
                      {origin}
                    </p>
                    {stores.map((s) => {
                      const disabled =
                        !!selectedOrigin && selectedOrigin !== origin;
                      return (
                        <label
                          key={s.id}
                          className={`flex items-center gap-2 text-sm py-1 ${disabled ? "opacity-40" : ""}`}
                        >
                          <input
                            type="checkbox"
                            disabled={disabled}
                            checked={selectedStores.includes(s.id)}
                            onChange={() => toggleStore(s.id)}
                          />
                          {s.name} → {s.destination}
                        </label>
                      );
                    })}
                  </div>
                ))}
            </div>
          </div>

          {formError && (
            <p className="col-span-2 text-alert-red text-sm">{formError}</p>
          )}

          <button
            type="submit"
            disabled={createMutation.isPending}
            className="col-span-2 py-2 bg-highway-green text-white rounded font-medium disabled:opacity-50"
          >
            {createMutation.isPending ? "Salvando..." : "Salvar"}
          </button>
        </form>
      )}

      {isLoading && <p className="text-ink/60">Carregando...</p>}
      {isError && <p className="text-alert-red">Erro ao carregar fretes.</p>}

      {data && (
        <>
          <table className="w-full bg-white rounded overflow-hidden">
            <thead className="bg-ink/5 text-left text-sm text-ink/70">
              <tr>
                <th className="px-4 py-3">Data</th>
                <th className="px-4 py-3">Motorista</th>
                <th className="px-4 py-3">Veículo</th>
                <th className="px-4 py-3">Rota</th>
                <th className="px-4 py-3">Valor</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody className="text-sm">
              {data.content.map((freight) => (
                <tr key={freight.id} className="border-t border-ink/10">
                  <td className="px-4 py-3">
                    {formatDate(freight.freightDate)}
                  </td>
                  <td className="px-4 py-3 font-medium">
                    {freight.driverName}
                  </td>
                  <td className="px-4 py-3">{freight.vehiclePlate}</td>
                  <td className="px-4 py-3">
                    {freight.origin} → {freight.destinations}
                  </td>
                  <td className="px-4 py-3 font-display">
                    {formatCurrency(freight.freightValue)}
                  </td>
                  <td className="px-4 py-3">
                    <span className={statusColors[freight.status]}>
                      {statusLabels[freight.status]}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    {role === "ADMIN" ? (
                      <select
                        value={freight.status}
                        onChange={(e) =>
                          statusMutation.mutate({
                            id: freight.id,
                            status: e.target.value as FreightStatus,
                          })
                        }
                        className="text-xs border border-ink/20 rounded px-2 py-1"
                      >
                        {(
                          [
                            "PENDING",
                            "IN_PROGRESS",
                            "DELIVERED",
                            "CANCELED",
                          ] as FreightStatus[]
                        ).map((s) => (
                          <option key={s} value={s}>
                            {statusLabels[s]}
                          </option>
                        ))}
                      </select>
                    ) : (
                      <div className="flex gap-2">
                        {freight.status === "PENDING" && (
                          <>
                            <button
                              onClick={() =>
                                statusMutation.mutate({
                                  id: freight.id,
                                  status: "IN_PROGRESS",
                                })
                              }
                              className="text-ink/50 hover:text-route-orange"
                              title="Iniciar"
                            >
                              <Play size={16} />
                            </button>
                            <button
                              onClick={() =>
                                statusMutation.mutate({
                                  id: freight.id,
                                  status: "CANCELED",
                                })
                              }
                              className="text-ink/50 hover:text-alert-red"
                              title="Cancelar"
                            >
                              <XCircle size={16} />
                            </button>
                          </>
                        )}
                        {freight.status === "IN_PROGRESS" && (
                          <>
                            <button
                              onClick={() =>
                                statusMutation.mutate({
                                  id: freight.id,
                                  status: "DELIVERED",
                                })
                              }
                              className="text-ink/50 hover:text-highway-green"
                              title="Marcar como entregue"
                            >
                              <CheckCircle2 size={16} />
                            </button>
                            <button
                              onClick={() =>
                                statusMutation.mutate({
                                  id: freight.id,
                                  status: "CANCELED",
                                })
                              }
                              className="text-ink/50 hover:text-alert-red"
                              title="Cancelar"
                            >
                              <XCircle size={16} />
                            </button>
                          </>
                        )}
                        {freight.status === "DELIVERED" && (
                          <button
                            onClick={() =>
                              statusMutation.mutate({
                                id: freight.id,
                                status: "CANCELED",
                              })
                            }
                            className="text-ink/50 hover:text-alert-red"
                            title="Cancelar"
                          >
                            <XCircle size={16} />
                          </button>
                        )}
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="flex items-center justify-between mt-4 text-sm text-ink/70">
            <span>
              Página {data.number + 1} de {data.totalPages} (
              {data.totalElements} fretes)
            </span>
            <div className="flex gap-2">
              <button
                onClick={() => setPage((p) => p - 1)}
                disabled={data.number === 0}
                className="flex items-center gap-1 px-3 py-1.5 border border-ink/20 rounded disabled:opacity-40 disabled:cursor-not-allowed"
              >
                <ChevronLeft size={16} />
                Anterior
              </button>
              <button
                onClick={() => setPage((p) => p + 1)}
                disabled={data.number + 1 >= data.totalPages}
                className="flex items-center gap-1 px-3 py-1.5 border border-ink/20 rounded disabled:opacity-40 disabled:cursor-not-allowed"
              >
                Próxima
                <ChevronRight size={16} />
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}

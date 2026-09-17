import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Power, PowerOff } from "lucide-react";
import { AxiosError } from "axios";
import { listVehicles, createVehicle, deactivateVehicle, activateVehicle } from "../../api/vehicles";
import type { VehicleRequest } from "../../types/vehicle";

export default function Vehicles() {
  const [showForm, setShowForm] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const queryClient = useQueryClient();

  const { data, isLoading, isError } = useQuery({
    queryKey: ["vehicles"],
    queryFn: listVehicles,
  });

  const createMutation = useMutation({
    mutationFn: createVehicle,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["vehicles"] });
      setShowForm(false);
      setFormError(null);
    },
    onError: (err: AxiosError) => {
      if (err.response?.status === 403) {
        setFormError("Você não tem permissão para cadastrar veículos. Fale com um administrador.");
      } else {
        setFormError("Não foi possível salvar o veículo. Tente novamente.");
      }
    },
  });

  const deactivateMutation = useMutation({
    mutationFn: deactivateVehicle,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["vehicles"] });
      setActionError(null);
    },
    onError: (err: AxiosError) => {
      if (err.response?.status === 403) {
        setActionError("Você não tem permissão para desativar veículos. Fale com um administrador.");
      } else {
        setActionError("Não foi possível desativar o veículo.");
      }
    },
  });

  const activateMutation = useMutation({
    mutationFn: activateVehicle,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["vehicles"] });
      setActionError(null);
    },
    onError: (err: AxiosError) => {
      if (err.response?.status === 403) {
        setActionError("Você não tem permissão para reativar veículos. Fale com um administrador.");
      } else {
        setActionError("Não foi possível reativar o veículo.");
      }
    },
  });

  function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setFormError(null);
    const formData = new FormData(e.currentTarget);

    const payload: VehicleRequest = {
      licensePlate: formData.get("licensePlate") as string,
      type: formData.get("type") as string,
      model: formData.get("model") as string,
      year: Number(formData.get("year")),
    };

    createMutation.mutate(payload);
  }

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-display font-semibold text-ink">
          Veículos
        </h1>
        <button
          onClick={() => {
            setShowForm(!showForm);
            setFormError(null);
          }}
          className="flex items-center gap-2 px-4 py-2 bg-route-orange text-white rounded font-medium text-sm"
        >
          <Plus size={16} />
          Novo veículo
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
          className="bg-white p-6 rounded mb-6 grid grid-cols-2 gap-4 max-w-xl"
        >
          <input name="licensePlate" placeholder="Placa" required className="border border-ink/20 px-3 py-2 rounded" />
          <input name="type" placeholder="Tipo (ex: Carreta)" required className="border border-ink/20 px-3 py-2 rounded" />
          <input name="model" placeholder="Modelo" required className="border border-ink/20 px-3 py-2 rounded" />
          <input name="year" type="number" placeholder="Ano" required className="border border-ink/20 px-3 py-2 rounded" />

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
      {isError && <p className="text-alert-red">Erro ao carregar veículos.</p>}

      {data && (
        <table className="w-full bg-white rounded overflow-hidden">
          <thead className="bg-ink/5 text-left text-sm text-ink/70">
            <tr>
              <th className="px-4 py-3">Placa</th>
              <th className="px-4 py-3">Tipo</th>
              <th className="px-4 py-3">Modelo</th>
              <th className="px-4 py-3">Ano</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody className="text-sm">
            {data.content.map((vehicle) => (
              <tr key={vehicle.id} className="border-t border-ink/10">
                <td className="px-4 py-3 font-medium">{vehicle.licensePlate}</td>
                <td className="px-4 py-3">{vehicle.type}</td>
                <td className="px-4 py-3">{vehicle.model}</td>
                <td className="px-4 py-3">{vehicle.year}</td>
                <td className="px-4 py-3">
                  <span className={vehicle.enabled ? "text-highway-green" : "text-alert-red"}>
                    {vehicle.enabled ? "Ativo" : "Inativo"}
                  </span>
                </td>
                <td className="px-4 py-3">
                  {vehicle.enabled ? (
                    <button
                      onClick={() => deactivateMutation.mutate(vehicle.id)}
                      className="text-ink/50 hover:text-alert-red"
                      title="Desativar"
                    >
                      <Power size={16} />
                    </button>
                  ) : (
                    <button
                      onClick={() => activateMutation.mutate(vehicle.id)}
                      className="text-ink/50 hover:text-highway-green"
                      title="Reativar"
                    >
                      <PowerOff size={16} />
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
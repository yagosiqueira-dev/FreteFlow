import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Power, PowerOff, Pencil, ChevronLeft, ChevronRight } from "lucide-react";
import { AxiosError } from "axios";
import { listVehicles, createVehicle, deactivateVehicle, activateVehicle, updateVehicle } from "../../api/vehicles";
import type { VehicleRequest, Vehicle } from "../../types/vehicle";

export default function Vehicles() {
  const [showForm, setShowForm] = useState(false);
  const [editingVehicle, setEditingVehicle] = useState<Vehicle | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const queryClient = useQueryClient();

  const { data, isLoading, isError } = useQuery({
    queryKey: ["vehicles", page],
    queryFn: () => listVehicles(page), 
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
      } else if (err.response?.status === 409) {
        setFormError("Já existe um veículo cadastrado com esta placa.");
      } else {
        setFormError("Não foi possível salvar o veículo. Tente novamente.");
      }
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: VehicleRequest }) =>
      updateVehicle(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["vehicles"] });
      setEditingVehicle(null);
      setFormError(null);
    },
    onError: (err: AxiosError) => {
      if (err.response?.status === 403) {
        setFormError("Você não tem permissão para editar veículos. Fale com um administrador.");
      } else if (err.response?.status === 409) {
        setFormError("Já existe um veículo cadastrado com esta placa.");
      } else {
        setFormError("Não foi possível salvar as alterações.");
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

  function handleSubmitCreate(e: React.FormEvent<HTMLFormElement>) {
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

  function handleSubmitEdit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!editingVehicle) return;
    setFormError(null);

    const formData = new FormData(e.currentTarget);
    const payload: VehicleRequest = {
      licensePlate: formData.get("licensePlate") as string,
      type: formData.get("type") as string,
      model: formData.get("model") as string,
      year: Number(formData.get("year")),
    };

    updateMutation.mutate({ id: editingVehicle.id, data: payload });
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
            setEditingVehicle(null);
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

      {showForm && !editingVehicle && (
        <form
          onSubmit={handleSubmitCreate}
          className="bg-white p-6 rounded mb-6 grid grid-cols-2 gap-4 max-w-xl"
        >
          <div className="col-span-2 flex justify-between items-center mb-2">
            <h2 className="text-lg font-medium text-ink">Cadastrar Novo Veículo</h2>
            <button type="button" onClick={() => setShowForm(false)} className="text-ink/50 text-sm hover:underline">Cancelar</button>
          </div>
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

      {editingVehicle && (
        <form
          onSubmit={handleSubmitEdit}
          className="bg-white p-6 rounded mb-6 grid grid-cols-2 gap-4 max-w-xl"
        >
          <p className="col-span-2 text-sm text-ink/60">
            Editando Veículo:{" "}
            <span className="font-medium text-ink">{editingVehicle.licensePlate}</span>
          </p>
          <input
            name="licensePlate"
            placeholder="Placa"
            defaultValue={editingVehicle.licensePlate}
            required
            className="border border-ink/20 px-3 py-2 rounded"
          />
          <input
            name="type"
            placeholder="Tipo (ex: Carreta)"
            defaultValue={editingVehicle.type}
            required
            className="border border-ink/20 px-3 py-2 rounded"
          />
          <input
            name="model"
            placeholder="Modelo"
            defaultValue={editingVehicle.model}
            required
            className="border border-ink/20 px-3 py-2 rounded"
          />
          <input
            name="year"
            type="number"
            placeholder="Ano"
            defaultValue={editingVehicle.year}
            required
            className="border border-ink/20 px-3 py-2 rounded"
          />

          {formError && (
            <p className="col-span-2 text-alert-red text-sm">{formError}</p>
          )}

          <div className="col-span-2 flex gap-2 mt-2">
            <button
              type="button"
              onClick={() => {
                setEditingVehicle(null);
                setFormError(null);
              }}
              className="flex-1 py-2 border border-ink/20 rounded font-medium"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={updateMutation.isPending}
              className="flex-1 py-2 bg-highway-green text-white rounded font-medium disabled:opacity-50"
            >
              {updateMutation.isPending ? "Salvando..." : "Salvar Alterações"}
            </button>
          </div>
        </form>
      )}

      {isLoading && <p className="text-ink/60">Carregando...</p>}
      {isError && <p className="text-alert-red">Erro ao carregar veículos.</p>}

      {data && (
        <>
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
                    <div className="flex gap-2">
                      <button
                        onClick={() => {
                          setEditingVehicle(vehicle);
                          setShowForm(false);
                        }}
                        className="text-ink/50 hover:text-asphalt"
                        title="Editar"
                      >
                        <Pencil size={16} />
                      </button>

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
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {/* Paginação */}
          {data.totalPages !== undefined && (
            <div className="flex items-center justify-between mt-4 text-sm text-ink/70">
              <span>
                Página {data.number + 1} de {data.totalPages} (
                {data.totalElements} veículos)
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
          )}
        </>
      )}
    </div>
  );
}
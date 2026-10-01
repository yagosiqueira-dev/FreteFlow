import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Power, PowerOff, Pencil, ChevronLeft, ChevronRight } from "lucide-react";
import { AxiosError } from "axios";
import { listDrivers, createDriver, deactivateDriver, activateDriver, updateDriver } from "../../api/drivers";
import type { DriverRequest, Driver } from "../../types/driver";

export default function Drivers() {
  const [showForm, setShowForm] = useState(false);
  const [editingDriver, setEditingDriver] = useState<Driver | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [page, setPage] = useState(0); 
  const queryClient = useQueryClient();

  const { data, isLoading, isError } = useQuery({
    queryKey: ["drivers", page],
    queryFn: () => listDrivers(page), 
  });

  const createMutation = useMutation({
    mutationFn: createDriver,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["drivers"] });
      setShowForm(false);
      setFormError(null);
    },
    onError: (err: AxiosError) => {
      if (err.response?.status === 403) {
        setFormError("Você não tem permissão para cadastrar motoristas. Fale com um administrador.");
      } else if (err.response?.status === 409) {
          setFormError("Já existe um motorista com esse CPF.");
      } else {
        setFormError("Não foi possível salvar o motorista. Verifique os dados informados.");
      }
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: DriverRequest }) =>
      updateDriver(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["drivers"] });
      setEditingDriver(null);
      setFormError(null);
    },
    onError: (err: AxiosError) => {
      if (err.response?.status === 403) {
        setFormError("Você não tem permissão para editar motoristas. Fale com um administrador.");
      } else if (err.response?.status === 409) {
        setFormError("Já existe um motorista com esse CPF.");
      } else {
        setFormError("Não foi possível salvar as alterações.");
      }
    },
  });

  const deactivateMutation = useMutation({
    mutationFn: deactivateDriver,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["drivers"] });
      setActionError(null);
    },
    onError: (err: AxiosError) => {
      if (err.response?.status === 403) {
        setActionError("Você não tem permissão para desativar motoristas. Fale com um administrador.");
      } else {
        setActionError("Não foi possível desativar o motorista.");
      }
    },
  });

  const activateMutation = useMutation({
    mutationFn: activateDriver,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["drivers"] });
      setActionError(null);
    },
    onError: (err: AxiosError) => {
      if (err.response?.status === 403) {
        setActionError("Você não tem permissão para reativar motoristas. Fale com um administrador.");
      } else {
        setActionError("Não foi possível reativar o motorista.");
      }
    },
  });

  function handleSubmitCreate(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setFormError(null);
    const formData = new FormData(e.currentTarget);

    const payload: DriverRequest = {
      name: formData.get("name") as string,
      phone: formData.get("phone") as string,
      cpf: formData.get("cpf") as string,
    };

    createMutation.mutate(payload);
  }

  function handleSubmitEdit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!editingDriver) return;
    setFormError(null);

    const formData = new FormData(e.currentTarget);
    const payload: DriverRequest = {
      name: formData.get("name") as string,
      phone: formData.get("phone") as string,
      cpf: formData.get("cpf") as string,
    };

    updateMutation.mutate({ id: editingDriver.id, data: payload });
  }

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-display font-semibold text-ink">
          Motoristas
        </h1>
        <button
          onClick={() => {
            setShowForm(!showForm);
            setEditingDriver(null); 
            setFormError(null);
          }}
          className="flex items-center gap-2 px-4 py-2 bg-route-orange text-white rounded font-medium text-sm"
        >
          <Plus size={16} />
          Novo motorista
        </button>
      </div>

      {actionError && (
        <div className="mb-4 px-4 py-3 bg-alert-red/10 border border-alert-red/30 rounded text-alert-red text-sm">
          {actionError}
        </div>
      )}

      {showForm && !editingDriver && (
        <form
          onSubmit={handleSubmitCreate}
          className="bg-white p-6 rounded mb-6 grid grid-cols-2 gap-4 max-w-xl"
        >
          <div className="col-span-2 flex justify-between items-center mb-2">
            <h2 className="text-lg font-medium text-ink">Cadastrar Novo Motorista</h2>
            <button type="button" onClick={() => setShowForm(false)} className="text-ink/50 text-sm hover:underline">Cancelar</button>
          </div>
          <input name="name" placeholder="Nome completo" required className="col-span-2 border border-ink/20 px-3 py-2 rounded" />
          <input name="phone" placeholder="Telefone (ex: 11987654321)" required className="border border-ink/20 px-3 py-2 rounded" />
          <input name="cpf" placeholder="CPF (só números ou com pontuação)" required className="border border-ink/20 px-3 py-2 rounded" />

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

      {editingDriver && (
        <form
          onSubmit={handleSubmitEdit}
          className="bg-white p-6 rounded mb-6 grid grid-cols-2 gap-4 max-w-xl"
        >
          <p className="col-span-2 text-sm text-ink/60">
            Editando Motorista:{" "}
            <span className="font-medium text-ink">{editingDriver.name}</span>
          </p>
          <input
            name="name"
            placeholder="Nome completo"
            defaultValue={editingDriver.name}
            required
            className="col-span-2 border border-ink/20 px-3 py-2 rounded"
          />
          <input
            name="phone"
            placeholder="Telefone"
            defaultValue={editingDriver.phone}
            required
            className="border border-ink/20 px-3 py-2 rounded"
          />
          <input
            name="cpf"
            placeholder="CPF"
            defaultValue={editingDriver.cpf}
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
                setEditingDriver(null);
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
      {isError && <p className="text-alert-red">Erro ao carregar motoristas.</p>}

      {data && (
        <>
          <table className="w-full bg-white rounded overflow-hidden">
            <thead className="bg-ink/5 text-left text-sm text-ink/70">
              <tr>
                <th className="px-4 py-3">Nome</th>
                <th className="px-4 py-3">Telefone</th>
                <th className="px-4 py-3">CPF</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody className="text-sm">
              {data.content.map((driver) => (
                <tr key={driver.id} className="border-t border-ink/10">
                  <td className="px-4 py-3 font-medium">{driver.name}</td>
                  <td className="px-4 py-3">{driver.phone}</td>
                  <td className="px-4 py-3">{driver.cpf}</td>
                  <td className="px-4 py-3">
                    <span className={driver.enabled ? "text-highway-green" : "text-alert-red"}>
                      {driver.enabled ? "Ativo" : "Inativo"}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2">
                      <button
                        onClick={() => {
                          setEditingDriver(driver);
                          setShowForm(false); 
                        }}
                        className="text-ink/50 hover:text-asphalt"
                        title="Editar"
                      >
                        <Pencil size={16} />
                      </button>

                      {driver.enabled ? (
                        <button
                          onClick={() => deactivateMutation.mutate(driver.id)}
                          className="text-ink/50 hover:text-alert-red"
                          title="Desativar"
                        >
                          <Power size={16} />
                        </button>
                      ) : (
                        <button
                          onClick={() => activateMutation.mutate(driver.id)}
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

          {data.totalPages !== undefined && (
            <div className="flex items-center justify-between mt-4 text-sm text-ink/70">
              <span>
                Página {data.number + 1} de {data.totalPages} (
                {data.totalElements} motoristas)
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
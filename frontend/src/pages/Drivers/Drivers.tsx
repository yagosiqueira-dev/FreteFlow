import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Power, PowerOff } from "lucide-react";
import { AxiosError } from "axios";
import { listDrivers, createDriver, deactivateDriver, activateDriver } from "../../api/drivers";
import type { DriverRequest } from "../../types/driver";

export default function Drivers() {
  const [showForm, setShowForm] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const queryClient = useQueryClient();

  const { data, isLoading, isError } = useQuery({
    queryKey: ["drivers"],
    queryFn: listDrivers,
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
      } else {
        setFormError("Não foi possível salvar o motorista. Verifique o CPF informado.");
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

  function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
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

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-display font-semibold text-ink">
          Motoristas
        </h1>
        <button
          onClick={() => {
            setShowForm(!showForm);
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

      {showForm && (
        <form
          onSubmit={handleSubmit}
          className="bg-white p-6 rounded mb-6 grid grid-cols-2 gap-4 max-w-xl"
        >
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

      {isLoading && <p className="text-ink/60">Carregando...</p>}
      {isError && <p className="text-alert-red">Erro ao carregar motoristas.</p>}

      {data && (
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
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
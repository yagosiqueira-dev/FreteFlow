import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { AxiosError } from "axios";
import { listStores, createStore, deactivateStore, activateStore } from "../../api/stores";
import type { StoreRequest } from "../../types/store";
import { Plus, Power, PowerOff, ChevronLeft, ChevronRight } from "lucide-react";

function formatCurrency(value: number) {
  return value.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

export default function Stores() {
  const [showForm, setShowForm] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const queryClient = useQueryClient();

  const { data, isLoading, isError } = useQuery({
  queryKey: ["stores", page],
  queryFn: () => listStores(page),
});

  const createMutation = useMutation({
    mutationFn: createStore,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["stores"] });
      setShowForm(false);
      setFormError(null);
    },
    onError: (err: AxiosError) => {
      if (err.response?.status === 403) {
        setFormError("Você não tem permissão para cadastrar lojas. Fale com um administrador.");
      } else {
        setFormError("Não foi possível salvar a loja. Verifique os dados informados.");
      }
    },
  });

  const deactivateMutation = useMutation({
    mutationFn: deactivateStore,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["stores"] });
      setActionError(null);
    },
    onError: (err: AxiosError) => {
      if (err.response?.status === 403) {
        setActionError("Você não tem permissão para desativar lojas. Fale com um administrador.");
      } else {
        setActionError("Não foi possível desativar a loja.");
      }
    },
  });

  const activateMutation = useMutation({
    mutationFn: activateStore,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["stores"] });
      setActionError(null);
    },
    onError: (err: AxiosError) => {
      if (err.response?.status === 403) {
        setActionError("Você não tem permissão para reativar lojas. Fale com um administrador.");
      } else {
        setActionError("Não foi possível reativar a loja.");
      }
    },
  });

  function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setFormError(null);
    const formData = new FormData(e.currentTarget);

    const payload: StoreRequest = {
      name: formData.get("name") as string,
      origin: formData.get("origin") as string,
      destination: formData.get("destination") as string,
      defaultValue: Number(formData.get("defaultValue")),
    };

    createMutation.mutate(payload);
  }

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-display font-semibold text-ink">
          Lojas
        </h1>
        <button
          onClick={() => {
            setShowForm(!showForm);
            setFormError(null);
          }}
          className="flex items-center gap-2 px-4 py-2 bg-route-orange text-white rounded font-medium text-sm"
        >
          <Plus size={16} />
          Nova loja
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
          <input name="name" placeholder="Nome da loja" required className="col-span-2 border border-ink/20 px-3 py-2 rounded" />
          <input name="origin" placeholder="Origem" required className="border border-ink/20 px-3 py-2 rounded" />
          <input name="destination" placeholder="Destino" required className="border border-ink/20 px-3 py-2 rounded" />
          <input name="defaultValue" type="number" step="0.01" min="0.01" placeholder="Valor padrão do frete (R$)" required className="border border-ink/20 px-3 py-2 rounded" />

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
      {isError && <p className="text-alert-red">Erro ao carregar lojas.</p>}

        {data && (
  <>
    <table className="w-full bg-white rounded overflow-hidden">
      <thead className="bg-ink/5 text-left text-sm text-ink/70">
        <tr>
          <th className="px-4 py-3">Nome</th>
          <th className="px-4 py-3">Origem</th>
          <th className="px-4 py-3">Destino</th>
          <th className="px-4 py-3">Valor padrão</th>
          <th className="px-4 py-3">Status</th>
          <th className="px-4 py-3"></th>
        </tr>
      </thead>
      <tbody className="text-sm">
        {data.content.map((store) => (
          <tr key={store.id} className="border-t border-ink/10">
            <td className="px-4 py-3 font-medium">{store.name}</td>
            <td className="px-4 py-3">{store.origin}</td>
            <td className="px-4 py-3">{store.destination}</td>
            <td className="px-4 py-3 font-display">{formatCurrency(store.defaultValue)}</td>
            <td className="px-4 py-3">
              <span className={store.enabled ? "text-highway-green" : "text-alert-red"}>
                {store.enabled ? "Ativo" : "Inativo"}
              </span>
            </td>
            <td className="px-4 py-3">
              {store.enabled ? (
                <button
                  onClick={() => deactivateMutation.mutate(store.id)}
                  className="text-ink/50 hover:text-alert-red"
                  title="Desativar"
                >
                  <Power size={16} />
                </button>
              ) : (
                <button
                  onClick={() => activateMutation.mutate(store.id)}
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

    <div className="flex items-center justify-between mt-4 text-sm text-ink/70">
      <span>
        Página {data.number + 1} de {data.totalPages} ({data.totalElements} lojas)
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
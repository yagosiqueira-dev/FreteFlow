import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { 
  Plus, 
  Pencil, 
  ChevronLeft, 
  ChevronRight, 
  Receipt, 
  Search,
  XCircle,
  RotateCcw
} from "lucide-react";
import { AxiosError } from "axios";
import { listExpenses, createExpense, updateExpense, deactivateExpense, activateExpense } from "../../api/expenses";
import type { ExpenseFilters } from "../../api/expenses";
import { listVehicles } from "../../api/vehicles";
import type { ExpenseRequest, Expense } from "../../types/expense";

function formatCurrency(value: number) {
  return value.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function formatDate(value: string) {
  return new Date(value + "T00:00:00").toLocaleDateString("pt-BR");
}

const DESPESAS_CATEGORIAS = ["Diesel", "Pedágio", "Manutenção"];

export default function Expenses() {
  const [showForm, setShowForm] = useState(false);
  const [editingExpense, setEditingExpense] = useState<Expense | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  
  const [filters, setFilters] = useState<ExpenseFilters & { enabled?: boolean }>({});
  const queryClient = useQueryClient();

  const { data, isLoading, isError } = useQuery({
    queryKey: ["expenses", page, filters],
    queryFn: () => listExpenses(page, filters),
  });

  const { data: vehiclesData } = useQuery({
    queryKey: ["vehicles-dropdown"],
    queryFn: () => listVehicles(0), 
  });

  const createMutation = useMutation({
    mutationFn: createExpense,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["expenses"] });
      closeForm();
    },
    onError: () => setFormError("Não foi possível salvar a despesa. Verifique os dados."),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: ExpenseRequest }) =>
      updateExpense(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["expenses"] });
      closeForm();
    },
    onError: () => setFormError("Não foi possível salvar as alterações."),
  });

  const deactivateMutation = useMutation({
    mutationFn: deactivateExpense,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["expenses"] });
      setActionError(null);
    },
    onError: (err: AxiosError) => {
      if (err.response?.status === 403) {
        setActionError("Você não tem permissão para cancelar despesas.");
      } else {
        setActionError("Não foi possível cancelar a despesa.");
      }
    },
  });

  const activateMutation = useMutation({
    mutationFn: activateExpense,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["expenses"] });
      setActionError(null);
    },
    onError: (err: AxiosError) => {
      if (err.response?.status === 403) {
        setActionError("Você não tem permissão para reativar despesas.");
      } else {
        setActionError("Não foi possível reativar a despesa.");
      }
    },
  });

  function closeForm() {
    setShowForm(false);
    setEditingExpense(null);
    setFormError(null);
  }

  function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setFormError(null);
    const formData = new FormData(e.currentTarget);

    const payload: ExpenseRequest = {
      vehicleId: formData.get("vehicleId") as string,
      description: formData.get("description") as string,
      amount: Number(formData.get("amount")),
      expenseDate: formData.get("expenseDate") as string,
    };

    if (editingExpense) {
      updateMutation.mutate({ id: editingExpense.id, data: payload });
    } else {
      createMutation.mutate(payload);
    }
  }

  function handleFilterSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);

    const vehicleId = formData.get("filterVehicleId") as string;
    const description = formData.get("filterDescription") as string;
    const status = formData.get("filterStatus") as string;
    const startDate = formData.get("filterStartDate") as string;
    const endDate = formData.get("filterEndDate") as string;

    setFilters({
      vehicleId: vehicleId || undefined,
      description: description || undefined,
      enabled: status === "" ? undefined : status === "true",
      startDate: startDate || undefined,
      endDate: endDate || undefined,
    });
    setPage(0);
  }

  function handleClearFilters(e: React.MouseEvent<HTMLButtonElement>) {
    e.preventDefault();
    (e.currentTarget.closest("form") as HTMLFormElement)?.reset();
    setFilters({});
    setPage(0);
  }

  function getVehiclePlate(id: string) {
    if (!vehiclesData) return "Carregando...";
    const vehicle = vehiclesData.content.find((v) => v.id === id);
    return vehicle ? vehicle.licensePlate : "Veículo removido";
  }

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-display font-semibold text-ink">Despesas</h1>
        <button
          onClick={() => {
            setShowForm(!showForm);
            setEditingExpense(null);
            setFormError(null);
          }}
          className="flex items-center gap-2 px-4 py-2 bg-route-orange text-white rounded font-medium text-sm"
        >
          <Plus size={16} />
          Nova Despesa
        </button>
      </div>

      <form
        onSubmit={handleFilterSubmit}
        className="bg-white p-4 rounded mb-6 grid grid-cols-5 gap-3 items-end border border-ink/10"
      >
        <div>
          <label className="block text-xs text-ink/60 mb-1">Veículo</label>
          <select 
            name="filterVehicleId" 
            defaultValue=""
            className="w-full border border-ink/20 px-3 py-1.5 rounded text-sm bg-white"
          >
            <option value="">Todos</option>
            {vehiclesData?.content.map((v) => (
              <option key={v.id} value={v.id}>{v.licensePlate} - {v.model}</option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-xs text-ink/60 mb-1">Categoria</label>
          <select 
            name="filterDescription" 
            defaultValue="" 
            className="w-full border border-ink/20 px-3 py-1.5 rounded text-sm bg-white"
          >
            <option value="">Todas</option>
            {DESPESAS_CATEGORIAS.map((cat) => (
              <option key={cat} value={cat}>{cat}</option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-xs text-ink/60 mb-1">Status</label>
          <select 
            name="filterStatus" 
            defaultValue="" 
            className="w-full border border-ink/20 px-3 py-1.5 rounded text-sm bg-white"
          >
            <option value="">Todos</option>
            <option value="true">Ativo</option>
            <option value="false">Cancelado</option>
          </select>
        </div>

        <div>
          <label className="block text-xs text-ink/60 mb-1">Data Inicial</label>
          <input 
            name="filterStartDate" 
            type="date" 
            className="w-full border border-ink/20 px-3 py-1.5 rounded text-sm" 
          />
        </div>

        <div>
          <label className="block text-xs text-ink/60 mb-1">Data Final</label>
          <input 
            name="filterEndDate" 
            type="date" 
            className="w-full border border-ink/20 px-3 py-1.5 rounded text-sm" 
          />
        </div>

        <div className="col-span-5 flex gap-2 justify-end mt-2">
          <button
            onClick={handleClearFilters}
            type="button"
            className="px-4 py-1.5 border border-ink/20 rounded text-sm text-ink/70"
          >
            Limpar
          </button>
          <button
            type="submit"
            className="flex items-center gap-2 px-4 py-1.5 bg-asphalt text-white rounded text-sm font-medium"
          >
            <Search size={14} />
            Filtrar
          </button>
        </div>
      </form>

      {actionError && (
        <div className="mb-4 px-4 py-3 bg-alert-red/10 border border-alert-red/30 rounded text-alert-red text-sm">
          {actionError}
        </div>
      )}

      {(showForm || editingExpense) && (
        <form
          key={editingExpense ? editingExpense.id : "new"}
          onSubmit={handleSubmit}
          className="bg-white p-6 rounded mb-6 grid grid-cols-2 gap-4 max-w-xl"
        >
          <div className="col-span-2 mb-2">
            <h2 className="text-lg font-medium text-ink flex items-center gap-2">
              <Receipt size={20} className="text-ink/60" />
              {editingExpense ? "Editar Despesa" : "Lançar Nova Despesa"}
            </h2>
          </div>

          <div>
            <label className="block text-xs text-ink/60 mb-1">Veículo</label>
            <select
              name="vehicleId"
              defaultValue={editingExpense?.vehicleId || ""}
              required
              className="w-full border border-ink/20 px-3 py-2 rounded text-sm bg-white"
            >
              <option value="">Selecione um veículo</option>
              {vehiclesData?.content
                .filter((v) => v.enabled)
                .map((v) => (
                <option key={v.id} value={v.id}>{v.licensePlate} - {v.model}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs text-ink/60 mb-1">Categoria</label>
            <select
              name="description"
              defaultValue={editingExpense?.description || ""}
              required
              className="w-full border border-ink/20 px-3 py-2 rounded text-sm bg-white"
            >
              <option value="">Selecione a categoria</option>
              {DESPESAS_CATEGORIAS.map((cat) => (
                <option key={cat} value={cat}>{cat}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs text-ink/60 mb-1">Valor (R$)</label>
            <input
              name="amount"
              type="number"
              step="0.01"
              min="0.01"
              placeholder="0,00"
              defaultValue={editingExpense?.amount}
              required
              className="w-full border border-ink/20 px-3 py-2 rounded text-sm"
            />
          </div>

          <div>
            <label className="block text-xs text-ink/60 mb-1">Data da Despesa</label>
            <input
              name="expenseDate"
              type="date"
              defaultValue={editingExpense?.expenseDate}
              required
              className="w-full border border-ink/20 px-3 py-2 rounded text-sm"
            />
          </div>

          {formError && (
            <p className="col-span-2 text-alert-red text-sm">{formError}</p>
          )}

          <div className="col-span-2 flex gap-2 mt-4">
            <button
              type="button"
              onClick={closeForm}
              className="flex-1 py-2 border border-ink/20 rounded font-medium text-ink/70 hover:bg-ink/5"
            >
               Cancelar
            </button>
            <button
              type="submit"
              disabled={createMutation.isPending || updateMutation.isPending}
              className="flex-1 py-2 bg-highway-green text-white rounded font-medium disabled:opacity-50"
            >
              {createMutation.isPending || updateMutation.isPending ? "Salvando..." : "Salvar"}
            </button>
          </div>
        </form>
      )}

      {isLoading && <p className="text-ink/60">Carregando...</p>}
      {isError && <p className="text-alert-red">Erro ao carregar despesas.</p>}

      {data && (
        <>
          <table className="w-full bg-white rounded overflow-hidden">
            <thead className="bg-ink/5 text-left text-sm text-ink/70">
              <tr>
                <th className="px-4 py-3">Data</th>
                <th className="px-4 py-3">Veículo</th>
                <th className="px-4 py-3">Categoria</th>
                <th className="px-4 py-3">Valor</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody className="text-sm">
              {data.content.map((expense) => (
                <tr key={expense.id} className="border-t border-ink/10">
                  <td className="px-4 py-3">{formatDate(expense.expenseDate)}</td>
                  <td className="px-4 py-3 font-medium">{getVehiclePlate(expense.vehicleId)}</td>
                  <td className="px-4 py-3">{expense.description}</td>
                  <td className="px-4 py-3 text-alert-red font-medium">
                    - {formatCurrency(expense.amount)}
                  </td>
                  <td className="px-4 py-3">
                    <span className={expense.enabled ? "text-highway-green" : "text-alert-red"}>
                      {expense.enabled ? "Ativo" : "Cancelado"}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2">
                      <button
                        onClick={() => {
                          setEditingExpense(expense);
                          setShowForm(false);
                        }}
                        className="text-ink/50 hover:text-asphalt"
                        title="Editar"
                      >
                        <Pencil size={16} />
                      </button>

                      {expense.enabled ? (
                        <button
                          onClick={() => deactivateMutation.mutate(expense.id)}
                          className="text-ink/50 hover:text-alert-red"
                          title="Cancelar"
                        >
                          <XCircle size={16} />
                        </button>
                      ) : (
                        <button
                          onClick={() => activateMutation.mutate(expense.id)}
                          className="text-ink/50 hover:text-highway-green"
                          title="Desfazer cancelamento e Reativar"
                        >
                          <RotateCcw size={16} />
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
                Página {data.number + 1} de {data.totalPages} ({data.totalElements} registos)
              </span>
              <div className="flex gap-2">
                <button
                  onClick={() => setPage((p) => p - 1)}
                  disabled={data.number === 0}
                  className="flex items-center gap-1 px-3 py-1.5 border border-ink/20 rounded disabled:opacity-40 disabled:cursor-not-allowed"
                >
                  <ChevronLeft size={16} /> Anterior
                </button>
                <button
                  onClick={() => setPage((p) => p + 1)}
                  disabled={data.number + 1 >= data.totalPages}
                  className="flex items-center gap-1 px-3 py-1.5 border border-ink/20 rounded disabled:opacity-40 disabled:cursor-not-allowed"
                >
                  Próxima <ChevronRight size={16} />
                </button>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
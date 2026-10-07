import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { AxiosError } from "axios";
import type {
  User,
  UserRegistrationRequest,
  UserRequest,
} from "../../types/user";
import {
  Plus,
  Pencil,
  Power,
  PowerOff,
  ShieldCheck,
  ShieldOff,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";
import {
  listUsers,
  createUser,
  updateUser,
  deactivateUser,
  activateUser,
  promoteUser,
  demoteUser,
} from "../../api/users";

export default function Users() {
  const [page, setPage] = useState(0);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const queryClient = useQueryClient();

  const { data, isLoading, isError } = useQuery({
    queryKey: ["users", page],
    queryFn: () => listUsers(page),
  });

  const createMutation = useMutation({
    mutationFn: createUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
      setShowCreateForm(false);
      setFormError(null);
    },
    onError: (err: AxiosError) => {
      if (err.response?.status === 409) {
        setFormError("Já existe um usuário com esse email.");
      } else {
        setFormError("Não foi possível criar o usuário.");
      }
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: UserRequest }) =>
      updateUser(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
      setEditingUser(null);
      setFormError(null);
    },
    onError: (err: AxiosError) => {
      if (err.response?.status === 409) {
        setFormError("Já existe um usuário com esse email.");
      } else {
        setFormError("Não foi possível salvar as alterações.");
      }
    },
  });

  const deactivateMutation = useMutation({
    mutationFn: deactivateUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
      setActionError(null);
    },
    onError: (err: AxiosError) => {
      if (err.response?.status === 409) {
        setActionError("Você não pode desativar sua própria conta.");
      } else {
        setActionError("Não foi possível desativar este usuário.");
      }
    },
  });

  const activateMutation = useMutation({
    mutationFn: activateUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
      setActionError(null);
    },
    onError: () => setActionError("Não foi possível reativar este usuário."),
  });

  const promoteMutation = useMutation({
    mutationFn: promoteUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
      setActionError(null);
    },
    onError: () => setActionError("Não foi possível promover este usuário."),
  });

  const demoteMutation = useMutation({
    mutationFn: demoteUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
      setActionError(null);
    },
    onError: (err: AxiosError) => {
      if (err.response?.status === 409) {
        setActionError(
          "Você não pode remover seu próprio privilégio de administrador.",
        );
      } else {
        setActionError("Não foi possível despromover este usuário.");
      }
    },
  });

  function handleCreateSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setFormError(null);

    const formData = new FormData(e.currentTarget);
    const payload: UserRegistrationRequest = {
      name: formData.get("name") as string,
      email: formData.get("email") as string,
      password: formData.get("password") as string,
    };

    createMutation.mutate(payload);
  }

  function handleEditSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!editingUser) return;
    setFormError(null);

    const formData = new FormData(e.currentTarget);
    const payload: UserRequest = {
      name: formData.get("name") as string,
      email: formData.get("email") as string,
    };

    updateMutation.mutate({ id: editingUser.id, data: payload });
  }

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-display font-semibold text-ink">
          Usuários
        </h1>
        <button
          onClick={() => {
            setEditingUser(null);
            setShowCreateForm(true);
            setFormError(null);
          }}
          className="flex items-center gap-2 px-4 py-2 bg-route-orange text-white rounded font-medium text-sm"
        >
          <Plus size={16} />
          Novo usuário
        </button>
      </div>

      {actionError && (
        <div className="mb-4 px-4 py-3 bg-alert-red/10 border border-alert-red/30 rounded text-alert-red text-sm">
          {actionError}
        </div>
      )}

      {showCreateForm && (
        <form
          onSubmit={handleCreateSubmit}
          className="bg-white p-6 rounded mb-6 grid grid-cols-2 gap-4 max-w-xl"
        >
          <div className="col-span-2 flex justify-between items-center mb-2">
            <h2 className="text-lg font-medium text-ink">Cadastrar Novo Usuário</h2>
          </div>
          <div className="col-span-2">
            <label className="block text-xs text-ink/60 mb-1">Username</label>
            <input
              name="name"
              autoComplete="username"
              required
              className="w-full border border-ink/20 px-3 py-2 rounded"
            />
          </div>
          <div>
            <label className="block text-xs text-ink/60 mb-1">E-mail</label>
            <input
              name="email"
              type="email"
              autoComplete="email"
              required
              className="w-full border border-ink/20 px-3 py-2 rounded"
            />
          </div>
          <div>
            <label className="block text-xs text-ink/60 mb-1">Senha</label>
            <input
              name="password"
              type="password"
              autoComplete="new-password"
              required
              className="w-full border border-ink/20 px-3 py-2 rounded"
            />
          </div>

          {formError && (
            <p className="col-span-2 text-alert-red text-sm">{formError}</p>
          )}

          <div className="col-span-2 flex gap-2 mt-2">
            <button
              type="button"
              onClick={() => {
                setShowCreateForm(false);
                setFormError(null);
              }}
              className="flex-1 py-2 border border-ink/20 rounded font-medium"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={createMutation.isPending}
              className="flex-1 py-2 bg-highway-green text-white rounded font-medium disabled:opacity-50"
            >
              {createMutation.isPending ? "Criando..." : "Criar usuário"}
            </button>
          </div>
        </form>
      )}

      {editingUser && (
        <form
          onSubmit={handleEditSubmit}
          className="bg-white p-6 rounded mb-6 grid grid-cols-2 gap-4 max-w-xl"
        >
          <p className="col-span-2 text-sm text-ink/60">
            Editando:{" "}
            <span className="font-medium text-ink">{editingUser.email}</span>
          </p>
          <input
            name="name"
            placeholder="Nome completo"
            defaultValue={editingUser.name}
            required
            className="border border-ink/20 px-3 py-2 rounded"
          />
          <input
            name="email"
            type="email"
            placeholder="Email"
            defaultValue={editingUser.email}
            required
            className="border border-ink/20 px-3 py-2 rounded"
          />

          {formError && (
            <p className="col-span-2 text-alert-red text-sm">{formError}</p>
          )}

          <div className="col-span-2 flex gap-2">
            <button
              type="button"
              onClick={() => {
                setEditingUser(null);
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
              {updateMutation.isPending ? "Salvando..." : "Salvar"}
            </button>
          </div>
        </form>
      )}

      {isLoading && <p className="text-ink/60">Carregando...</p>}
      {isError && <p className="text-alert-red">Erro ao carregar usuários.</p>}

      {data && (
        <>
          <table className="w-full bg-white rounded overflow-hidden">
            <thead className="bg-ink/5 text-left text-sm text-ink/70">
              <tr>
                <th className="px-4 py-3">Nome</th>
                <th className="px-4 py-3">Email</th>
                <th className="px-4 py-3">Papel</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody className="text-sm">
              {data.content.map((user) => (
                <tr key={user.id} className="border-t border-ink/10">
                  <td className="px-4 py-3 font-medium">{user.name}</td>
                  <td className="px-4 py-3">{user.email}</td>
                  <td className="px-4 py-3">
                    <span
                      className={
                        user.role === "ADMIN"
                          ? "text-route-orange font-medium"
                          : "text-ink/70"
                      }
                    >
                      {user.role === "ADMIN" ? "Admin" : "Operador"}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={
                        user.enabled ? "text-highway-green" : "text-alert-red"
                      }
                    >
                      {user.enabled ? "Ativo" : "Inativo"}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2">
                      <button
                        onClick={() => {
                          setShowCreateForm(false);
                          setEditingUser(user);
                          setFormError(null);
                        }}
                        className="text-ink/50 hover:text-asphalt"
                        title="Editar"
                      >
                        <Pencil size={16} />
                      </button>
                     
                      {user.enabled ? (
                        <button
                          onClick={() => deactivateMutation.mutate(user.id)}
                          className="text-ink/50 hover:text-alert-red"
                          title="Desativar"
                        >
                          <Power size={16} />
                        </button>
                      ) : (
                        <button
                          onClick={() => activateMutation.mutate(user.id)}
                          className="text-ink/50 hover:text-highway-green"
                          title="Reativar"
                        >
                          <PowerOff size={16} />
                        </button>
                      )}
                      {user.role === "OPERATOR" ? (
                        <button
                          onClick={() => promoteMutation.mutate(user.id)}
                          className="text-ink/50 hover:text-route-orange"
                          title="Promover a Admin"
                        >
                          <ShieldCheck size={16} />
                        </button>
                      ) : (
                        <button
                          onClick={() => demoteMutation.mutate(user.id)}
                          className="text-ink/50 hover:text-alert-red"
                          title="Remover privilégio de Admin"
                        >
                          <ShieldOff size={16} />
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="flex items-center justify-between mt-4 text-sm text-ink/70">
            <span>
              Página {data.number + 1} de {data.totalPages} (
              {data.totalElements} usuários)
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

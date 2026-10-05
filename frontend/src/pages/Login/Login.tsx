import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { AxiosError } from "axios";
import { Eye, EyeOff, Loader2, Truck } from "lucide-react";
import api from "../../api/axios";
import { useAuth } from "../../hooks/useAuth";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const response = await api.post("/auth/login", { email, password });
      login(response.data.token);
      navigate("/dashboard");
    } catch (err) {
      const axiosErr = err as AxiosError<{ message?: string }>;
      if (axiosErr.response?.status === 429) {
        setError(
          axiosErr.response.data?.message ??
            "Muitas tentativas. Tente novamente mais tarde.",
        );
      } else {
        setError("Email ou senha inválidos");
      }
    } finally {
      setLoading(false);
    }
  }

  const inputClass =
    "w-full px-3 py-2.5 bg-white border border-ink/20 rounded text-ink placeholder:text-ink/30 focus:outline-none focus:border-route-orange focus:ring-2 focus:ring-route-orange/30";

  return (
    <div className="min-h-screen grid lg:grid-cols-2 bg-concrete">
      <aside className="hidden lg:flex flex-col justify-between bg-asphalt p-12 text-concrete">
        <div className="flex items-center gap-3">
          <Truck size={28} className="text-route-orange" />
          <span className="font-display text-2xl font-semibold">FreteFlow</span>
        </div>

        <div className="max-w-md">
          <h2 className="font-display text-4xl font-semibold leading-tight mb-4">
            Fretes, despesas e lucro da frota em um só lugar.
          </h2>
          <p className="text-concrete/60">
            Acompanhe cada entrega, controle os gastos de cada caminhão e veja
            o resultado do mês sem planilha.
          </p>
        </div>

        <div
          className="h-1 w-full"
          style={{
            backgroundImage:
              "repeating-linear-gradient(90deg, #E8752C 0 32px, transparent 32px 56px)",
          }}
        />
      </aside>

      <main className="flex items-center justify-center p-6">
        <form onSubmit={handleSubmit} className="w-full max-w-sm">
          <div className="flex items-center gap-2 mb-8 lg:hidden">
            <Truck size={24} className="text-route-orange" />
            <span className="font-display text-xl font-semibold text-ink">
              FreteFlow
            </span>
          </div>

          <h1 className="font-display text-3xl font-semibold text-ink mb-1">
            Entrar
          </h1>
          <p className="text-ink/60 mb-8">Use seu email e senha para acessar.</p>

          <label htmlFor="email" className="block text-sm font-medium text-ink mb-1">
            Email
          </label>
          <input
            id="email"
            type="email"
            autoComplete="username"
            placeholder="voce@email.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className={`${inputClass} mb-4`}
            required
          />

          <label htmlFor="password" className="block text-sm font-medium text-ink mb-1">
            Senha
          </label>
          <div className="relative mb-4">
            <input
              id="password"
              type={showPassword ? "text" : "password"}
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className={`${inputClass} pr-10`}
              required
            />
            <button
              type="button"
              onClick={() => setShowPassword((v) => !v)}
              aria-label={showPassword ? "Ocultar senha" : "Mostrar senha"}
              className="absolute inset-y-0 right-0 px-3 text-ink/50 hover:text-ink"
            >
              {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>

          {error && (
            <div
              role="alert"
              className="mb-4 border-l-4 border-alert-red bg-alert-red/10 px-3 py-2 text-sm text-alert-red"
            >
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full flex items-center justify-center gap-2 py-2.5 rounded bg-route-orange text-white font-medium hover:bg-route-orange/90 transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {loading && <Loader2 size={16} className="animate-spin" />}
            {loading ? "Entrando..." : "Entrar"}
          </button>

          <p className="mt-6 text-sm text-ink/50">
            Acesso restrito. Precisa de uma conta? Fale com o administrador.
          </p>
        </form>
      </main>
    </div>
  );
}
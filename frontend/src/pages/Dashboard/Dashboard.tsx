import { useAuth } from "../../hooks/useAuth";


export default function Dashboard() {
  return (
    <div className="p-8">
      <h1 className="text-2xl font-display font-semibold text-ink">
        Início
      </h1>
      <p className="text-ink/60 mt-2">Bem-vindo ao painel FreteFlow.</p>
    </div>
  );
}
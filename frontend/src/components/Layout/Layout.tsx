import { NavLink, Outlet } from "react-router-dom";
import { LayoutDashboard, Truck, Users, Store, Package, FileBarChart, LogOut } from "lucide-react";
import { useAuth } from "../../hooks/useAuth";

const navItems = [
  { to: "/dashboard", label: "Início", icon: LayoutDashboard },
  { to: "/veiculos", label: "Veículos", icon: Truck },
  { to: "/motoristas", label: "Motoristas", icon: Users },
  { to: "/lojas", label: "Lojas", icon: Store },
  { to: "/fretes", label: "Fretes", icon: Package },
  { to: "/relatorios", label: "Relatórios", icon: FileBarChart },
];

export default function Layout() {
  const { logout } = useAuth();

  return (
    <div className="min-h-screen flex">
      <aside className="w-64 bg-asphalt text-concrete flex flex-col shrink-0">
        <div className="px-6 py-5 border-b border-white/10">
          <span className="font-display text-xl font-semibold text-route-orange">
            FreteFlow
          </span>
        </div>

        <nav className="flex-1 px-3 py-4 flex flex-col gap-1">
          {navItems.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2 rounded text-sm font-medium transition-colors ${
                  isActive
                    ? "bg-route-orange text-white"
                    : "text-concrete/80 hover:bg-white/5"
                }`
              }
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>

        <button
          onClick={logout}
          className="flex items-center gap-3 px-3 py-3 mx-3 mb-4 text-sm text-concrete/70 hover:text-alert-red transition-colors"
        >
          <LogOut size={18} />
          Sair
        </button>
      </aside>

      <main className="flex-1 bg-concrete overflow-y-auto">
        <Outlet />
      </main>
    </div>
  );
}
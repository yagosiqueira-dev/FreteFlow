import { Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login/Login";
import Dashboard from "./pages/Dashboard/Dashboard";
import Layout from "./components/Layout/Layout";
import { PrivateRoute } from "./components/PrivateRoute";
import Vehicles from "./pages/Vehicles/Vehicles";
import Drivers from "./pages/Drivers/Drivers";
import Stores from "./pages/Stores/Stores";
import Freights from "./pages/Freights/Freights";
import Reports from "./pages/Reports/Reports";
import Users from "./pages/Users/Users";
import { AdminRoute } from "./components/AdminRoute";


function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<Login />} />

      <Route
        element={
          <PrivateRoute>
            <Layout />
          </PrivateRoute>
        }
      >
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/veiculos" element={<Vehicles />} />
        <Route path="/motoristas" element={<Drivers />} />
        <Route path="/lojas" element={<Stores />} />
        <Route path="/fretes" element={<Freights />} />
        <Route path="/relatorios" element={<Reports />} />
        <Route path="/usuarios" element={<AdminRoute><Users /></AdminRoute>} />
      </Route>
    </Routes>
  );
}

export default App;
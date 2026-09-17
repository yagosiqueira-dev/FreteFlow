import { Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login/Login";
import Dashboard from "./pages/Dashboard/Dashboard";
import Layout from "./components/Layout/Layout";
import { PrivateRoute } from "./components/PrivateRoute";
import Vehicles from "./pages/Vehicles/Vehicles";
import Drivers from "./pages/Drivers/Drivers";
import Stores from "./pages/Stores/Stores";


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
      </Route>
    </Routes>
  );
}

export default App;
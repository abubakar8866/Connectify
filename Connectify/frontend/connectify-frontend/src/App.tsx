import {
  Routes,
  Route,
  useLocation,
  useNavigate,
} from "react-router-dom";

import { useEffect } from "react";

import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

import ProtectedRoute from "./components/ProtectedRoute";

import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";
import ForgotPassword from "./pages/auth/ForgotPassword";
import ResetPassword from "./pages/auth/ResetPassword";
import Profile from "./pages/user/Profile";

import AuthInitializer from "./components/AuthInitializer";

import MainLayout from "./components/layout/MainLayout";

import PublicLayout from "./components/layout/PublicLayout";

import { useAppSelector } from "./hooks/reduxHooks";

function App() {
  return (
    <>
      <AuthInitializer />

      <ToastContainer
          position="top-right"
          autoClose={3000}
          hideProgressBar={false}
          newestOnTop
          closeOnClick
          pauseOnHover
          theme="dark"
      />

      <Routes>

        {/* PUBLIC LAYOUT (TopNavbar only) */}
        <Route element={<PublicLayout />}>

          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/reset-password" element={<ResetPassword />} />

        </Route>

        <Route element={<ProtectedRoute />}>
          <Route element={<MainLayout />}>

            <Route
              path="/"
              element={<h1 className="text-white">Home Page</h1>}
            />

            {/* PROFILE */}
            <Route
              path="/profile"
              element={<Profile />}
            />

          </Route>
        </Route>

      </Routes>
    </>
  );

}

export default App;


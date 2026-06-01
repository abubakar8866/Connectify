import { useState } from "react";
import { loginApi } from "../../api/authApi";
import { useAppDispatch } from "../../hooks/reduxHooks";
import { loginSuccess } from "../../features/auth/authSlice";
import { saveAuth } from "../../utils/authStorage";
import { useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { showSuccess, showError } from "../../utils/toast";
import {
  faEnvelope,
  faLock,
  faEye,
  faEyeSlash,
  faSpinner,
} from "@fortawesome/free-solid-svg-icons";

import {
  faGoogle,
  faGithub,
} from "@fortawesome/free-brands-svg-icons";

export default function Login() {
  const dispatch = useAppDispatch();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [emailTouched, setEmailTouched] =
    useState(false);

  const [passwordTouched, setPasswordTouched] =
    useState(false);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [emailError, setEmailError] = useState("");
  const [passwordError, setPasswordError] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  const navigate = useNavigate();

  const validateEmail = (value: string) => {
    setEmail(value);

    const regex =
      /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    if (!regex.test(value)) {
      setEmailError("Invalid email address");
    } else {
      setEmailError("");
    }
  };

  const validatePassword = (value: string) => {
    setPassword(value);

    const regex =
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@#$%^&+=!]).{8,}$/;

    if (!regex.test(value)) {
      setPasswordError(
        "Password must contain uppercase, lowercase, digit, special character and be at least 8 characters long"
      );
    } else {
      setPasswordError("");
    }
  };

  const handleReset = () => {
    setEmail("");
    setPassword("");
    setEmailError("");
    setPasswordError("");
    setEmailTouched(false);
    setPasswordTouched(false);
    setError("");
  };

  const handleLogin = async () => {
    setError("");

    if (!email) {
      setError("Email is required");
      return;
    }

    if (!password) {
      setError("Password is required");
      return;
    }

    if (emailError || passwordError) {
      setError("Please fix validation errors");
      return;
    }

    try {
      setLoading(true);

      const res = await loginApi({
        email,
        password,
      });

      dispatch(
        loginSuccess({
          user: res.user,
          accessToken: res.accessToken,
          refreshToken: res.refreshToken,
        })
      );

      saveAuth({
        user: res.user,
        accessToken: res.accessToken,
        refreshToken: res.refreshToken,
      });

     showSuccess("Login successful");

      navigate("/");
    } catch (err: any) {
      showError(
        err?.response?.data?.message ||
        "Something went wrong."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-gray-1000 flex items-center justify-center px-4">
      <div className="w-full my-6 max-w-md bg-gray-800 rounded-xl p-8 shadow-lg">

        {/* HEADER */}
        <div className="text-center mb-6">

          {/* App identity small logo */}
          <div className="w-14 h-14 mx-auto rounded-full bg-green-600 flex items-center justify-center text-white text-2xl font-bold mb-3">
            C
          </div>

          {/* Title */}
          <h1 className="text-2xl font-bold text-white">
            Welcome Back
          </h1>

          {/* Subtitle */}
          <p className="text-gray-400 text-sm mt-1">
            Login to continue to Connectify
          </p>

        </div>

        <div className="mb-4">
          <div className="relative">
            <FontAwesomeIcon
              icon={faEnvelope}
              className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
            />

            <input
              disabled={loading}
              type="email"
              placeholder="Email"
              value={email}
              onChange={(e) => validateEmail(e.target.value)}
              onBlur={() => setEmailTouched(true)}
              className="w-full pl-10 p-3 rounded bg-gray-700 text-white outline-none disabled:opacity-50"
            />
          </div>

          {emailTouched && emailError && (
            <p className="text-red-400 text-sm mt-1">
              {emailError}
            </p>
          )}
        </div>

        <div className="mb-2">
          <div className="relative">

            <FontAwesomeIcon
              icon={faLock}
              className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
            />

            <input
              disabled={loading}
              type={showPassword ? "text" : "password"}
              placeholder="Password"
              value={password}
              onChange={(e) => validatePassword(e.target.value)}
              onBlur={() => setPasswordTouched(true)}
              className="w-full pl-10 pr-12 p-3 rounded bg-gray-700 text-white outline-none"
            />

            <button
              type="button"
              disabled={loading}
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400"
            >
              <FontAwesomeIcon
                icon={showPassword ? faEyeSlash : faEye}
              />
            </button>

          </div>

          {passwordTouched && passwordError && (
            <p className="text-red-400 text-sm mt-1">
              {passwordError}
            </p>
          )}
        </div>

        <div className="text-right mb-5">
          <button
            disabled={loading}
            onClick={() => navigate("/forgot-password")}
            className="text-sm text-blue-400 hover:text-blue-300"
          >
            Forgot Password?
          </button>
        </div>

        <div className="flex gap-3">

          <button
            onClick={handleLogin}
            disabled={loading}
            className="w-1/2 bg-green-600 hover:bg-green-700 disabled:bg-gray-600 text-white py-3 rounded font-semibold flex items-center justify-center gap-2"
          >
            {loading ? (
              <>
                <FontAwesomeIcon icon={faSpinner} spin />
                Logging in...
              </>
            ) : (
              "Login"
            )}
          </button>

          <button
            type="button"
            disabled={loading}
            onClick={handleReset}
            className="w-1/2 bg-gray-600 hover:bg-gray-700 text-white py-3 rounded font-semibold"
          >
            Reset
          </button>

        </div>

        <div className="my-5 flex items-center">
          <div className="flex-1 border-t border-gray-600"></div>
          <span className="px-3 text-gray-400 text-sm">
            OR
          </span>
          <div className="flex-1 border-t border-gray-600"></div>
        </div>

        <div className="flex gap-3">

            <button
                disabled={loading}
                className="w-1/2 border border-gray-600 text-white py-3 rounded hover:bg-gray-700"
            >
                <FontAwesomeIcon icon={faGoogle} className="mr-1" />
                Google
            </button>

            <button
                disabled={loading}
                className="w-1/2 border border-gray-600 text-white py-3 rounded hover:bg-gray-700"
            >
                <FontAwesomeIcon icon={faGithub} className="mr-1" />
                GitHub
            </button>

        </div>

        <div className="mt-6 text-center text-gray-400">
          Don't have an account?
          <button
            disabled={loading}
            onClick={() => navigate("/register")}
            className="ml-2 text-green-400 hover:text-green-300"
          >
            Register
          </button>
        </div>
      </div>
    </div>
  );

}


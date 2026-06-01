import { useState } from "react";
import { registerApi } from "../../api/authApi";
import { useAppDispatch } from "../../hooks/reduxHooks";
import { loginSuccess } from "../../features/auth/authSlice";
import { saveAuth } from "../../utils/authStorage";
import { useNavigate } from "react-router-dom";
import { showSuccess, showError } from "../../utils/toast";

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faUser,
  faAt,
  faIdBadge,
  faLock,
  faEye,
  faEyeSlash,
  faSpinner,
} from "@fortawesome/free-solid-svg-icons";

export default function Register() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [uname, setUname] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [showPassword, setShowPassword] = useState(false);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // field errors
  const [nameError, setNameError] = useState("");
  const [unameError, setUnameError] = useState("");
  const [emailError, setEmailError] = useState("");
  const [passwordError, setPasswordError] = useState("");

  // touch state
  const [nameTouched, setNameTouched] = useState(false);
  const [unameTouched, setUnameTouched] = useState(false);
  const [emailTouched, setEmailTouched] = useState(false);
  const [passwordTouched, setPasswordTouched] = useState(false);

  // validations
  const validateName = (value: string) => {
    setName(value);
    setNameError(value.trim() ? "" : "Name is required");
  };

  const validateUname = (value: string) => {
    setUname(value);
    if (!value) setUnameError("Username is required");
    else if (value.length < 3 || value.length > 20)
      setUnameError("Username must be 3–20 characters");
    else setUnameError("");
  };

  const validateEmail = (value: string) => {
    setEmail(value);
    const regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    setEmailError(regex.test(value) ? "" : "Invalid email");
  };

  const validatePassword = (value: string) => {
    setPassword(value);
    const regex =
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@#$%^&+=!]).{8,}$/;

    setPasswordError(
      regex.test(value)
        ? ""
        : "Password must be strong (A-Z, a-z, number, special char)"
    );
  };

  const handleReset = () => {
    setName("");
    setUname("");
    setEmail("");
    setPassword("");

    setError("");

    setNameError("");
    setUnameError("");
    setEmailError("");
    setPasswordError("");

    setNameTouched(false);
    setUnameTouched(false);
    setEmailTouched(false);
    setPasswordTouched(false);
  };

  const handleRegister = async () => {
    setError("");

    if (!name || !uname || !email || !password) {
      setError("All fields are required");
      return;
    }

    if (nameError || unameError || emailError || passwordError) {
      setError("Fix validation errors");
      return;
    }

    try {
      setLoading(true);

      const res = await registerApi({
        name,
        uname,
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

      showSuccess("Account created successfully");

      navigate("/login");
    } catch (err: any) {;
      showError(
          err?.response?.data?.message || "Something went wrong"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-gray-1000 flex items-center justify-center px-4">

      <div className="w-full my-10 max-w-md bg-gray-800 rounded-xl p-8 shadow-lg">

        <div className="text-center mb-6">

          {/* App identity small logo */}
          <div className="w-14 h-14 mx-auto rounded-full bg-green-600 flex items-center justify-center text-white text-2xl font-bold mb-3">
            C
          </div>

          {/* Title */}
          <h1 className="text-2xl font-bold text-white">
            Create Account
          </h1>

          {/* Subtitle */}
          <p className="text-gray-400 text-sm mt-1">
            Join Connectify and start connecting with people
          </p>

        </div>

        {/* NAME */}
        <div className="mb-3">
          <div className="relative">
            <FontAwesomeIcon icon={faUser} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input
              className="w-full pl-10 p-3 rounded bg-gray-700 text-white"
              placeholder="Full Name"
              value={name}
              onChange={(e) => validateName(e.target.value)}
              onBlur={() => setNameTouched(true)}
              disabled={loading}
            />
          </div>
          {nameTouched && nameError && (
            <p className="text-red-400 text-sm">{nameError}</p>
          )}
        </div>

        {/* USERNAME */}
        <div className="mb-3">
          <div className="relative">
            <FontAwesomeIcon icon={faIdBadge} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input
              className="w-full pl-10 p-3 rounded bg-gray-700 text-white"
              placeholder="Username"
              value={uname}
              onChange={(e) => validateUname(e.target.value)}
              onBlur={() => setUnameTouched(true)}
              disabled={loading}
            />
          </div>
          {unameTouched && unameError && (
            <p className="text-red-400 text-sm">{unameError}</p>
          )}
        </div>

        {/* EMAIL */}
        <div className="mb-3">
          <div className="relative">
            <FontAwesomeIcon icon={faAt} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input
              className="w-full pl-10 p-3 rounded bg-gray-700 text-white"
              placeholder="Email"
              value={email}
              onChange={(e) => validateEmail(e.target.value)}
              onBlur={() => setEmailTouched(true)}
              disabled={loading}
            />
          </div>
          {emailTouched && emailError && (
            <p className="text-red-400 text-sm">{emailError}</p>
          )}
        </div>

        {/* PASSWORD */}
        <div className="mb-4">
          <div className="relative">
            <FontAwesomeIcon icon={faLock} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />

            <input
              className="w-full pl-10 pr-12 p-3 rounded bg-gray-700 text-white"
              type={showPassword ? "text" : "password"}
              placeholder="Password"
              value={password}
              onChange={(e) => validatePassword(e.target.value)}
              onBlur={() => setPasswordTouched(true)}
              disabled={loading}
            />

            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400"
            >
              <FontAwesomeIcon icon={showPassword ? faEyeSlash : faEye} />
            </button>
          </div>

          {passwordTouched && passwordError && (
            <p className="text-red-400 text-sm">{passwordError}</p>
          )}
        </div>

        {/* BUTTONS */}
        <div className="flex gap-3">

          <button
            onClick={handleRegister}
            disabled={loading}
            className="w-1/2 bg-green-600 hover:bg-green-700 text-white py-3 rounded font-semibold flex items-center justify-center gap-2"
          >
            {loading ? (
              <>
                <FontAwesomeIcon icon={faSpinner} spin />
                Creating...
              </>
            ) : (
              "Register"
            )}
          </button>

          <button
            onClick={handleReset}
            disabled={loading}
            className="w-1/2 bg-gray-600 hover:bg-gray-700 text-white py-3 rounded font-semibold"
          >
            Reset
          </button>

        </div>

        {/* LINK */}
        <p className="text-gray-400 text-center mt-5">
          Already have an account?
          <button
            className="text-green-400 ml-2"
            onClick={() => navigate("/login")}
            disabled={loading}
          >
            Login
          </button>
        </p>

      </div>
    </div>
  );

}


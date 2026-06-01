import { useState } from "react";
import { forgotPasswordApi } from "../../api/authApi";
import { useNavigate } from "react-router-dom";

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faEnvelope,
  faXmark,
  faSpinner,
} from "@fortawesome/free-solid-svg-icons";

import { showSuccess, showError } from "../../utils/toast";

export default function ForgotPassword() {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [emailTouched, setEmailTouched] = useState(false);

  const [emailError, setEmailError] = useState("");
  const [loading, setLoading] = useState(false);

  // EMAIL VALIDATION
  const validateEmail = (value: string) => {
    setEmail(value);

    const regex =
      /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    if (!regex.test(value.trim())) {
      setEmailError("Enter a valid email address");
    } else {
      setEmailError("");
    }
  };

  // RESET
  const handleReset = () => {
    setEmail("");
    setEmailError("");
    setEmailTouched(false);
  };

  // SUBMIT
  const handleSend = async () => {
    setEmailTouched(true);

    if (!email) {
      setEmailError("Email is required");
      return;
    }

    if (emailError) {
      showError("Please fix errors before sending");
      return;
    }

    try {
      setLoading(true);

      await forgotPasswordApi({ email });

      showSuccess("Reset link sent to your email 📩");

      handleReset();
    } catch (err: any) {
      showError(
        err?.response?.data?.message ||
          "Failed to send reset email"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-gray-900 flex items-center justify-center px-4 relative">

      {/* CLOSE BUTTON */}
      <button
        onClick={() => navigate("/login")}
        className="absolute top-5 right-5 text-white text-xl hover:text-red-400"
      >
        <FontAwesomeIcon icon={faXmark} />
      </button>

      <div className="w-full my-20 max-w-md bg-gray-800 p-8 rounded-xl shadow-lg">

        {/* TITLE */}
        <h1 className="text-2xl font-bold text-white text-center mb-2">
          Forgot Password
        </h1>

        <p className="text-gray-400 text-sm text-center mb-6">
          Enter your email to receive reset link
        </p>

        {/* EMAIL INPUT */}
        <div className="mb-4">
          <div className="relative">
            <FontAwesomeIcon
              icon={faEnvelope}
              className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
            />

            <input
              type="email"
              placeholder="Enter email"
              value={email}
              disabled={loading}
              onChange={(e) => validateEmail(e.target.value)}
              onBlur={() => setEmailTouched(true)}
              className="w-full pl-10 p-3 rounded bg-gray-700 text-white outline-none disabled:opacity-50"
            />
          </div>

          {/* ERROR BELOW FIELD */}
          {emailTouched && emailError && (
            <p className="text-red-400 text-sm mt-1">
              {emailError}
            </p>
          )}
        </div>

        {/* BUTTONS */}
        <div className="flex gap-3">

          <button
            onClick={handleSend}
            disabled={loading}
            className="w-1/2 bg-green-600 hover:bg-green-700 disabled:bg-gray-600 text-white py-3 rounded font-semibold flex items-center justify-center gap-2"
          >
            {loading ? (
              <>
                <FontAwesomeIcon icon={faSpinner} spin />
                Sending...
              </>
            ) : (
              "Send"
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

        {/* LOGIN LINK */}
        <div className="text-center mt-5">
          <button
            onClick={() => navigate("/login")}
            className="text-green-400 hover:text-green-300 text-sm"
            disabled={loading}
          >
            Know password? Login
          </button>
        </div>

      </div>
    </div>

  );

}


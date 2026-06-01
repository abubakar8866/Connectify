import { useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import { resetPasswordApi } from "../../api/authApi";
import { showSuccess, showError } from "../../utils/toast";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faLock, faEye, faEyeSlash, faSpinner } from "@fortawesome/free-solid-svg-icons";

export default function ResetPassword() {

  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const token = searchParams.get("token");

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [loading, setLoading] = useState(false);

  const handleReset = async () => {

    if (!token) {
      showError("Invalid or missing reset token");
      return;
    }

    if (!password || !confirmPassword) {
      showError("All fields are required");
      return;
    }

    if (password !== confirmPassword) {
      showError("Passwords do not match");
      return;
    }

    try {
      setLoading(true);

      await resetPasswordApi(token, { password });

      showSuccess("Password reset successful");

      navigate("/login");

    } catch (err: any) {
      showError(
        err?.response?.data?.message || "Reset failed"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-900 px-4">

      <div className="w-full max-w-md bg-gray-800 p-8 rounded-xl shadow-lg">

        {/* TITLE */}
        <h1 className="text-white text-2xl font-bold text-center mb-6">
          Reset Password
        </h1>

        {/* PASSWORD */}
        <div className="mb-4 relative">

          <FontAwesomeIcon
            icon={faLock}
            className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
          />

          <input
            type={showPassword ? "text" : "password"}
            placeholder="New Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={loading}
            className="w-full pl-10 pr-12 p-3 rounded bg-gray-700 text-white"
          />

          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400"
          >
            <FontAwesomeIcon icon={showPassword ? faEyeSlash : faEye} />
          </button>

        </div>

        {/* PASSWORD */}
                <div className="mb-4 relative">

                  <FontAwesomeIcon
                    icon={faLock}
                    className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
                  />

                  {/* CONFIRM PASSWORD */}
                  <input
                    type={showConfirmPassword ? "text" : "password"}
                    placeholder="Confirm Password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    disabled={loading}
                    className="w-full pl-10 pr-12 p-3 rounded bg-gray-700 text-white"
                  />

                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400"
                  >
                    <FontAwesomeIcon icon={showConfirmPassword ? faEyeSlash : faEye} />
                  </button>

                </div>

        <div className="flex gap-3">

          {/* RESET PASSWORD */}
          <button
            onClick={handleReset}
            disabled={loading}
            className="w-1/2 bg-green-600 hover:bg-green-700 text-white py-3 rounded font-semibold flex items-center justify-center gap-2"
          >
            {loading ? (
              <>
                <FontAwesomeIcon icon={faSpinner} spin />
                Resetting...
              </>
            ) : (
              "Reset Password"
            )}
          </button>

          {/* CLEAR FORM */}
          <button
            type="button"
            onClick={() => {
              setPassword("");
              setConfirmPassword("");
            }}
            disabled={loading}
            className="w-1/2 bg-gray-600 hover:bg-gray-700 text-white py-3 rounded font-semibold"
          >
            Reset Form
          </button>

        </div>

        {/* BACK TO LOGIN */}
        <p className="text-center text-gray-400 mt-4">
          Remember password?
          <button
            onClick={() => navigate("/login")}
            className="text-green-400 ml-2"
          >
            Login
          </button>
        </p>

      </div>
    </div>
  );

}


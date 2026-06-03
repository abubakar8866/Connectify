import { useEffect, useState } from "react";
import {
  getCurrentUserApi,
  updateProfileApi,
} from "../../api/userApi";
import { showError, showSuccess } from "../../utils/toast";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCamera, faSpinner } from "@fortawesome/free-solid-svg-icons";

export default function Profile() {
  const [user, setUser] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  const [file, setFile] = useState<File | null>(null);
  const [preview, setPreview] = useState<string>("");

  const [form, setForm] = useState({
    name: "",
    uname: "",
    bio: "",
    city: "",
    gender: "",
    languages: [] as string[],
    dateOfBirth: "",
  });

  // ================= LOAD USER =================
  useEffect(() => {
    const loadUser = async () => {
      try {
        const res = await getCurrentUserApi();
        setUser(res);

        setForm({
          name: res.name || "",
          uname: res.uname || "",
          bio: res.bio || "",
          city: res.city || "",
          gender: res.gender || "",
          languages: res.languages || [],
          dateOfBirth: res.dateOfBirth || "",
        });

        setPreview(res.profileImageUrl);
      } catch (err) {
        showError("Failed to load profile");
      }
    };

    loadUser();
  }, []);

  // ================= HANDLE IMAGE =================
  const handleImage = (e: React.ChangeEvent<HTMLInputElement>) => {
    const f = e.target.files?.[0];
    if (!f) return;

    setFile(f);
    setPreview(URL.createObjectURL(f));
  };

  // ================= HANDLE CHANGE =================
  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  // ================= SUBMIT =================
  const handleUpdate = async () => {
    try {
      setLoading(true);

      const updated = await updateProfileApi(
        user.id,
        form,
        file
      );

      setUser(updated);
      showSuccess("Profile updated successfully");
    } catch (err: any) {
      showError(
        err?.response?.data?.message ||
          "Update failed"
      );
    } finally {
      setLoading(false);
    }
  };

  if (!user) {
    return (
      <div className="text-white text-center mt-10">
        Loading profile...
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto bg-gray-900 text-white p-6 rounded-xl mt-10">

      {/* HEADER */}
      <h1 className="text-2xl font-bold mb-6">
        My Profile
      </h1>

      {/* PROFILE IMAGE */}
      <div className="flex items-center gap-6 mb-6">

        <div className="relative w-24 h-24">
          <img
            src={
              preview ||
              "https://via.placeholder.com/100"
            }
            className="w-24 h-24 rounded-full object-cover"
          />

          <label className="absolute bottom-0 right-0 bg-green-600 p-2 rounded-full cursor-pointer">
            <FontAwesomeIcon icon={faCamera} />
            <input
              type="file"
              hidden
              onChange={handleImage}
            />
          </label>
        </div>

        <div>
          <p className="text-xl font-semibold">
            {user.name}
          </p>
          <p className="text-gray-400">
            @{user.uname}
          </p>
          <p className="text-sm text-gray-400">
            Age: {user.age ?? "N/A"}
          </p>
        </div>
      </div>

      {/* FORM */}
      <div className="grid grid-cols-2 gap-4">

        <input
          name="name"
          value={form.name}
          onChange={handleChange}
          className="p-2 bg-gray-800 rounded"
          placeholder="Name"
        />

        <input
          name="uname"
          value={form.uname}
          onChange={handleChange}
          className="p-2 bg-gray-800 rounded"
          placeholder="Username"
        />

        <input
          name="city"
          value={form.city}
          onChange={handleChange}
          className="p-2 bg-gray-800 rounded"
          placeholder="City"
        />

        <input
          type="date"
          name="dateOfBirth"
          value={form.dateOfBirth}
          onChange={handleChange}
          className="p-2 bg-gray-800 rounded"
        />

        <textarea
          name="bio"
          value={form.bio}
          onChange={handleChange}
          className="p-2 bg-gray-800 rounded col-span-2"
          placeholder="Bio"
        />

      </div>

      {/* BUTTON */}
      <button
        onClick={handleUpdate}
        disabled={loading}
        className="mt-6 w-full bg-green-600 hover:bg-green-700 py-3 rounded flex items-center justify-center gap-2"
      >
        {loading ? (
          <>
            <FontAwesomeIcon icon={faSpinner} spin />
            Updating...
          </>
        ) : (
          "Update Profile"
        )}
      </button>

    </div>
  );
}


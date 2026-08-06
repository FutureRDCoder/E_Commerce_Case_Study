import { useNavigate } from "react-router-dom";
import { useMutation } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import toast from "react-hot-toast";

import { register as registerUser } from "../../../services/authService";
import { registerSchema } from "../schemas/registerSchema";
import useAuthStore from "../../../store/authStore";

function RegisterForm() {

  const navigate = useNavigate();

  const loginStore = useAuthStore(
    (state) => state.login
  );

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(registerSchema),
  });

  const registerMutation = useMutation({

    mutationFn: registerUser,

    onSuccess: (data) => {

      const { token, ...user } = data;

      loginStore(token, user);

      toast.success("Registration successful!");

      navigate("/");
    },

    onError: (error) => {

      toast.error(
        error.response?.data?.message ??
        "Registration failed."
      );
    },
  });

  const onSubmit = (formData) => {
    registerMutation.mutate(formData);
  };

  const inputClass = "input";

  return (

    <div className="card p-4 shadow-glow sm:p-8">

      <h1 className="mb-8 text-center text-2xl font-bold text-white sm:text-3xl">
        Create Account
      </h1>

      <form
        onSubmit={handleSubmit(onSubmit)}
        className="space-y-5"
      >

        {/* Name */}

        <div>

          <label className="mb-2 block font-medium text-slate-300">
            Full Name
          </label>

          <input
            type="text"
            {...register("name")}
            className={inputClass}
          />

          {errors.name && (
            <p className="mt-1 text-sm text-red-400">
              {errors.name.message}
            </p>
          )}

        </div>

        {/* Username */}

        <div>

          <label className="mb-2 block font-medium text-slate-300">
            Username
          </label>

          <input
            type="text"
            {...register("username")}
            className={inputClass}
          />

          {errors.username && (
            <p className="mt-1 text-sm text-red-400">
              {errors.username.message}
            </p>
          )}

        </div>

        {/* Email */}

        <div>

          <label className="mb-2 block font-medium text-slate-300">
            Email
          </label>

          <input
            type="email"
            {...register("email")}
            className={inputClass}
          />

          {errors.email && (
            <p className="mt-1 text-sm text-red-400">
              {errors.email.message}
            </p>
          )}

        </div>

        {/* Password */}

        <div>

          <label className="mb-2 block font-medium text-slate-300">
            Password
          </label>

          <input
            type="password"
            {...register("password")}
            className={inputClass}
          />

          {errors.password && (
            <p className="mt-1 text-sm text-red-400">
              {errors.password.message}
            </p>
          )}

        </div>

        {/* Confirm Password */}

        <div>

          <label className="mb-2 block font-medium text-slate-300">
            Confirm Password
          </label>

          <input
            type="password"
            {...register("confirmPassword")}
            className={inputClass}
          />

          {errors.confirmPassword && (
            <p className="mt-1 text-sm text-red-400">
              {errors.confirmPassword.message}
            </p>
          )}

        </div>

        {/* Tenant Slug */}

        <div>

          <label className="mb-2 block font-medium text-slate-300">
            Tenant Slug
            <span className="ml-1 text-slate-500">
              (Optional)
            </span>
          </label>

          <input
            type="text"
            placeholder="apple, samsung, nike..."
            {...register("tenantSlug")}
            className={inputClass}
          />

          {errors.tenantSlug && (
            <p className="mt-1 text-sm text-red-400">
              {errors.tenantSlug.message}
            </p>
          )}

          <p className="mt-2 text-sm text-slate-500">
            Leave empty to register as a customer.
            Enter a valid tenant slug if you are registering as a tenant administrator.
          </p>

        </div>

        {/* Submit Button */}

        <button
          type="submit"
          disabled={registerMutation.isPending}
          className="btn-primary w-full p-3"
        >
          {registerMutation.isPending
            ? "Creating Account..."
            : "Register"}
        </button>

      </form>

    </div>

  );
}

export default RegisterForm;

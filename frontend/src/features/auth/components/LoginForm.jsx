import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import { loginSchema } from "../schemas/loginSchema";

import { useNavigate } from "react-router-dom";
import { useMutation } from "@tanstack/react-query";
import toast from "react-hot-toast";

import { login } from "../../../services/authService";
import useAuthStore from "../../../store/authStore";

function LoginForm() {
    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm({
        resolver: zodResolver(loginSchema),
    });

    const navigate = useNavigate();
    const loginStore = useAuthStore((state) => state.login);

    const loginMutation = useMutation({
        mutationFn: login,

        onSuccess: (data) => {
            loginStore(data.token, data);

            toast.success("Login successful!");

            navigate("/");
        },

        onError: (error) => {
            toast.error(
                error.response?.data?.message ||
                "Login failed."
            );
        },
    });

    const onSubmit = (data) => {
        loginMutation.mutate(data);
    };

    return (
        <div className="card p-4 shadow-glow sm:p-8">

            <h1 className="mb-8 text-center text-2xl font-bold text-white sm:text-3xl">
                Login
            </h1>

            <form
                onSubmit={handleSubmit(onSubmit)}
                className="space-y-5"
            >

                <div>

                    <label className="mb-2 block font-medium text-slate-300">
                        Username
                    </label>

                    <input
                        type="text"
                        {...register("username")}
                        className="input"
                    />

                    {errors.username && (
                        <p className="mt-1 text-sm text-red-400">
                            {errors.username.message}
                        </p>
                    )}

                </div>

                <div>

                    <label className="mb-2 block font-medium text-slate-300">
                        Password
                    </label>

                    <input
                        type="password"
                        {...register("password")}
                        className="input"
                    />

                    {errors.password && (
                        <p className="mt-1 text-sm text-red-400">
                            {errors.password.message}
                        </p>
                    )}

                </div>

                <button
                    type="submit"
                    disabled={loginMutation.isPending}
                    className="btn-primary w-full p-3"
                >
                    {loginMutation.isPending
                        ? "Logging in..."
                        : "Login"}
                </button>

            </form>

        </div>
    );
}

export default LoginForm;

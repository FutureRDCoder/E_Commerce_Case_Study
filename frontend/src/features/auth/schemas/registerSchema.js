import { z } from "zod";

export const registerSchema = z
  .object({
    name: z
      .string()
      .trim()
      .min(2, "Name must be at least 2 characters."),

    username: z
      .string()
      .trim()
      .min(3, "Username must be at least 3 characters."),

    email: z
      .string()
      .trim()
      .email("Enter a valid email address."),

    password: z
      .string()
      .min(8, "Password must be at least 8 characters.")
      .regex(
        /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/,
        "Password must contain at least one uppercase letter, one lowercase letter, and one digit."
      ),

    confirmPassword: z
      .string(),

    tenantSlug: z
      .string()
      .trim()
      .optional(),
  })
  .refine(
    (data) => data.password === data.confirmPassword,
    {
      path: ["confirmPassword"],
      message: "Passwords do not match.",
    }
  );
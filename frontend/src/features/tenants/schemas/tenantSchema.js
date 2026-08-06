import { z } from "zod";

export const tenantSchema = z.object({

  name: z.string().trim().min(2).max(100),

  slug: z
    .string()
    .trim()
    .min(2)
    .max(50)
    .regex(
      /^[a-z0-9-]+$/,
      "Slug may contain only lowercase letters, numbers and hyphens."
    ),

  description: z.string().max(1000).optional().or(z.literal("")),

  logoUrl: z.string().url().optional().or(z.literal("")),

});

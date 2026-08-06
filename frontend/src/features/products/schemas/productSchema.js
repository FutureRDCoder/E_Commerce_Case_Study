import { z } from "zod";

export const productSchema = z.object({

  name: z.string().min(3),

  description: z.string().min(10),

  price: z.coerce.number().positive(),

  availableQuantity: z.coerce.number().min(0),

  category: z.string().min(2),

  imageUrl: z.string().url(),

});
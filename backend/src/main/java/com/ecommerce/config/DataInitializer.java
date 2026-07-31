package com.ecommerce.config;

import com.ecommerce.model.Product;
import com.ecommerce.model.Tenant;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.TenantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final ProductRepository productRepository;

    public DataInitializer(TenantRepository tenantRepository, ProductRepository productRepository) {
        this.tenantRepository = tenantRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (tenantRepository.count() > 0) {
            return;
        }

        // 1. Tenants (Brands)
        Tenant nike = tenantRepository.save(Tenant.builder()
                .name("Nike Store")
                .slug("nike")
                .description("Just Do It. Iconic performance footwear, athletic wear, and lifestyle gear.")
                .logoUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff")
                .build());

        Tenant adidas = tenantRepository.save(Tenant.builder()
                .name("Adidas Originals")
                .slug("adidas")
                .description("Impossible is Nothing. Premium sportswear, classic sneakers, and street style.")
                .logoUrl("https://images.unsplash.com/photo-1518002171953-a080ee817e1f")
                .build());

        Tenant apple = tenantRepository.save(Tenant.builder()
                .name("Apple Official")
                .slug("apple")
                .description("Think Different. Cutting-edge electronics, Mac, iPhone, and smart accessories.")
                .logoUrl("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9")
                .build());

        Tenant puma = tenantRepository.save(Tenant.builder()
                .name("Puma Lifestyle")
                .slug("puma")
                .description("Forever Faster. Sport-inspired performance sneakers and motor-sport fashion.")
                .logoUrl("https://images.unsplash.com/photo-1608231387042-66d1773070a5")
                .build());

        // 2. Products for Nike
        productRepository.save(Product.builder()
                .tenant(nike)
                .name("Nike Air Max 270")
                .description("Nike's biggest heel Air unit yet delivers super-soft cushioning for all-day comfort.")
                .price(149.99)
                .category("Footwear")
                .availableQuantity(45)
                .imageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&auto=format&fit=crop")
                .build());

        productRepository.save(Product.builder()
                .tenant(nike)
                .name("Nike Dunk Low Retro Panda")
                .description("Created for the hardwood but taken to the streets with clean monochrome leather overlays.")
                .price(119.99)
                .category("Footwear")
                .availableQuantity(20)
                .imageUrl("https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=600&auto=format&fit=crop")
                .build());

        productRepository.save(Product.builder()
                .tenant(nike)
                .name("Nike Pegasus 40 Running Shoe")
                .description("Responsive cushioning in the Pegasus provides an energized ride for daily road runs.")
                .price(129.99)
                .category("Footwear")
                .availableQuantity(35)
                .imageUrl("https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2?w=600&auto=format&fit=crop")
                .build());

        productRepository.save(Product.builder()
                .tenant(nike)
                .name("Nike Tech Fleece Full-Zip Hoodie")
                .description("Premium, lightweight fleece—smooth on both sides—gives you plenty of warmth without bulk.")
                .price(135.00)
                .category("Apparel")
                .availableQuantity(50)
                .imageUrl("https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=600&auto=format&fit=crop")
                .build());

        productRepository.save(Product.builder()
                .tenant(nike)
                .name("Nike Heritage Gym Duffel Bag")
                .description("Durable polyester duffel with spacious main compartment and zip pockets for training gear.")
                .price(45.00)
                .category("Accessories")
                .availableQuantity(60)
                .imageUrl("https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600&auto=format&fit=crop")
                .build());

        // 3. Products for Adidas
        productRepository.save(Product.builder()
                .tenant(adidas)
                .name("Adidas Ultraboost Light")
                .description("Experience epic energy with our lightest Ultraboost ever made with Light BOOST material.")
                .price(189.99)
                .category("Footwear")
                .availableQuantity(30)
                .imageUrl("https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2?w=600&auto=format&fit=crop")
                .build());

        productRepository.save(Product.builder()
                .tenant(adidas)
                .name("Adidas Samba OG Shoes")
                .description("Born on the pitch, the Samba is a timeless icon of street style crafted with smooth leather.")
                .price(100.00)
                .category("Footwear")
                .availableQuantity(15)
                .imageUrl("https://images.unsplash.com/photo-1518002171953-a080ee817e1f?w=600&auto=format&fit=crop")
                .build());

        productRepository.save(Product.builder()
                .tenant(adidas)
                .name("Adidas Adicolor Classics Track Top")
                .description("Retro 3-Stripes jacket made with recycled materials and classic stand-up collar.")
                .price(85.00)
                .category("Apparel")
                .availableQuantity(40)
                .imageUrl("https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=600&auto=format&fit=crop")
                .build());

        // 4. Products for Apple
        productRepository.save(Product.builder()
                .tenant(apple)
                .name("iPhone 15 Pro Max (256GB)")
                .description("Forged in titanium with aerospace-grade A17 Pro chip and customizable Action button.")
                .price(1199.00)
                .category("Electronics")
                .availableQuantity(15)
                .imageUrl("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600&auto=format&fit=crop")
                .build());

        productRepository.save(Product.builder()
                .tenant(apple)
                .name("MacBook Pro 16\" M3 Max")
                .description("Liquid Retina XDR display with up to 22 hours of battery life and extreme workstation power.")
                .price(2499.00)
                .category("Electronics")
                .availableQuantity(10)
                .imageUrl("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&auto=format&fit=crop")
                .build());

        productRepository.save(Product.builder()
                .tenant(apple)
                .name("AirPods Pro (2nd Gen) USB-C")
                .description("Up to 2x more Active Noise Cancellation with Adaptive Audio and Personalized Spatial Audio.")
                .price(249.00)
                .category("Electronics")
                .availableQuantity(80)
                .imageUrl("https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=600&auto=format&fit=crop")
                .build());

        // 5. Products for Puma
        productRepository.save(Product.builder()
                .tenant(puma)
                .name("Puma Suede Classic XXI")
                .description("The legendary suede sneaker that defined street culture since 1968 with rubber outsole.")
                .price(75.00)
                .category("Footwear")
                .availableQuantity(50)
                .imageUrl("https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=600&auto=format&fit=crop")
                .build());

        productRepository.save(Product.builder()
                .tenant(puma)
                .name("Puma RS-X Efekt Retro")
                .description("Chunky lifestyle sneakers with Running System heritage and bold color accents.")
                .price(110.00)
                .category("Footwear")
                .availableQuantity(25)
                .imageUrl("https://images.unsplash.com/photo-1600185365483-26d7a4cc7519?w=600&auto=format&fit=crop")
                .build());
    }
}

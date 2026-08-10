package com.ecommerce.config;

import com.ecommerce.model.Product;
import com.ecommerce.model.Tenant;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.TenantRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("dev")
@Transactional
public class DataInitializer implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final ProductRepository productRepository;

    public DataInitializer(
            TenantRepository tenantRepository,
            ProductRepository productRepository
    ) {
        this.tenantRepository = tenantRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {

        if (tenantRepository.count() > 0) {
            return;
        }

        seedSamsung();
        seedSony();
        seedIkea();
        seedUniqlo();
        seedLego();
        seedCanon();
        seedBose();
        seedNorthFace();
        seedApple();
        seedNike();
        seedAdidas();
        seedPuma();
        seedReebok();
        seedLevis();
    }

    // -----------------------------------------------------
    // Helper Methods
    // -----------------------------------------------------

    private Tenant createTenant(
            String name,
            String slug,
            String description,
            String logoUrl
    ) {
        return tenantRepository.save(
                Tenant.builder()
                        .name(name)
                        .slug(slug)
                        .description(description)
                        .logoUrl(logoUrl)
                        .build()
        );
    }

    private void createProduct(
            Tenant tenant,
            String name,
            String description,
            BigDecimal price,
            String category,
            int quantity,
            String imageUrl
    ) {
        productRepository.save(
                Product.builder()
                        .tenant(tenant)
                        .name(name)
                        .description(description)
                        .price(price)
                        .category(category)
                        .availableQuantity(quantity)
                        .imageUrl(imageUrl)
                        .build()
        );
    }

    // -----------------------------------------------------
    // Brand Seed Methods
    // -----------------------------------------------------

    private void seedSamsung() {

        Tenant samsung = createTenant(
                "Samsung Store",
                "samsung",
                "Discover Samsung's latest Galaxy smartphones, tablets, wearables, TVs, laptops, and smart home innovations.",
                "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=600&auto=format&fit=crop"
        );

        createProduct(
                samsung,
                "Galaxy S25 Ultra (512GB)",
                "Samsung's flagship smartphone featuring Galaxy AI, a high-resolution camera system, titanium frame, and an immersive Dynamic AMOLED display.",
                BigDecimal.valueOf(129999),
                "Smartphones",
                30,
                "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=600&auto=format&fit=crop"
        );

        createProduct(
                samsung,
                "Galaxy Z Fold7",
                "Premium foldable smartphone with a large immersive display, multitasking capabilities, and flagship performance.",
                BigDecimal.valueOf(164999),
                "Smartphones",
                20,
                "https://images.unsplash.com/photo-1610792516307-ea5acd9c3b00?w=600&auto=format&fit=crop"
        );

        createProduct(
                samsung,
                "Galaxy Tab S10 Ultra",
                "Ultra-large AMOLED tablet designed for creativity, productivity, entertainment, and professional workflows.",
                BigDecimal.valueOf(99999),
                "Tablets",
                25,
                "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=600&auto=format&fit=crop"
        );

        createProduct(
                samsung,
                "Galaxy Watch Ultra",
                "Premium smartwatch with advanced health tracking, GPS, rugged titanium design, and multi-day battery life.",
                BigDecimal.valueOf(54999),
                "Wearables",
                40,
                "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop"
        );

        createProduct(
                samsung,
                "Galaxy Buds3 Pro",
                "Wireless earbuds featuring intelligent noise cancellation, immersive Hi-Fi audio, and seamless Galaxy ecosystem integration.",
                BigDecimal.valueOf(24999),
                "Audio",
                75,
                "https://images.unsplash.com/photo-1606220945770-b5b6c2c55bf1?w=600&auto=format&fit=crop"
        );

        createProduct(
                samsung,
                "Odyssey OLED G9 Gaming Monitor",
                "49-inch ultra-wide OLED gaming monitor with an ultra-fast refresh rate, HDR support, and immersive curved display.",
                BigDecimal.valueOf(139999),
                "Monitors",
                12,
                "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=600&auto=format&fit=crop"
        );
    }

    private void seedSony() {

        Tenant sony = createTenant(
                "Sony Center",
                "sony",
                "Experience Sony's world-class entertainment ecosystem featuring PlayStation, Alpha cameras, BRAVIA TVs, premium audio, and Xperia smartphones.",
                "https://images.unsplash.com/photo-1518770660439-4636190af475?w=600&auto=format&fit=crop"
        );

        createProduct(
                sony,
                "PlayStation 5 Pro",
                "Sony's most powerful PlayStation console with advanced ray tracing, AI-powered upscaling, and breathtaking 4K gaming performance.",
                BigDecimal.valueOf(59990),
                "Gaming",
                25,
                "https://images.unsplash.com/photo-1606813907291-d86efa9b94db?w=600&auto=format&fit=crop"
        );

        createProduct(
                sony,
                "WH-1000XM6 Wireless Headphones",
                "Industry-leading wireless noise-canceling headphones with exceptional sound quality, adaptive audio, and all-day comfort.",
                BigDecimal.valueOf(34990),
                "Audio",
                60,
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop"
        );

        createProduct(
                sony,
                "Xperia 1 VII",
                "Sony's premium flagship smartphone featuring professional-grade photography, 4K OLED display, and Snapdragon flagship performance.",
                BigDecimal.valueOf(119990),
                "Smartphones",
                18,
                "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600&auto=format&fit=crop"
        );

        createProduct(
                sony,
                "Alpha A7 IV Mirrorless Camera",
                "33MP full-frame mirrorless camera delivering exceptional autofocus, 4K video recording, and professional image quality.",
                BigDecimal.valueOf(214990),
                "Cameras",
                12,
                "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=600&auto=format&fit=crop"
        );

        createProduct(
                sony,
                "BRAVIA XR OLED 65\" TV",
                "65-inch premium OLED television powered by Sony's Cognitive Processor XR for stunning visuals and immersive cinematic sound.",
                BigDecimal.valueOf(249990),
                "Televisions",
                10,
                "https://images.unsplash.com/photo-1593784991095-a205069470b6?w=600&auto=format&fit=crop"
        );

        createProduct(
                sony,
                "INZONE H9 Gaming Headset",
                "Premium wireless gaming headset featuring 360 Spatial Sound, active noise cancellation, and crystal-clear voice communication.",
                BigDecimal.valueOf(24990),
                "Gaming Accessories",
                35,
                "https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=600&auto=format&fit=crop"
        );
    }

    private void seedIkea() {

        Tenant ikea = createTenant(
                "IKEA Home",
                "ikea",
                "Discover beautifully designed Scandinavian furniture, home décor, storage solutions, and smart living essentials at affordable prices.",
                "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=600&auto=format&fit=crop"
        );

        createProduct(
                ikea,
                "MALM Queen Bed Frame",
                "Minimalist queen-size bed frame with a durable wood veneer finish, clean Scandinavian design, and under-bed storage compatibility.",
                BigDecimal.valueOf(32999),
                "Furniture",
                20,
                "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=600&auto=format&fit=crop"
        );

        createProduct(
                ikea,
                "BILLY Bookcase",
                "One of IKEA's most iconic bookcases featuring adjustable shelves and timeless Scandinavian styling for any living space.",
                BigDecimal.valueOf(11999),
                "Furniture",
                45,
                "https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?w=600&auto=format&fit=crop"
        );

        createProduct(
                ikea,
                "POÄNG Armchair",
                "Comfortable bentwood armchair with ergonomic support and soft cushioning, perfect for relaxing after a long day.",
                BigDecimal.valueOf(18999),
                "Furniture",
                30,
                "https://images.unsplash.com/photo-1501045661006-fcebe0257c3f?w=600&auto=format&fit=crop"
        );

        createProduct(
                ikea,
                "KALLAX Shelf Unit",
                "Versatile cube shelving system ideal for books, storage boxes, collectibles, and modern home organization.",
                BigDecimal.valueOf(12999),
                "Storage",
                40,
                "https://images.unsplash.com/photo-1484101403633-562f891dc89a?w=600&auto=format&fit=crop"
        );

        createProduct(
                ikea,
                "LACK Coffee Table",
                "Lightweight yet sturdy coffee table with a clean minimalist design that complements any contemporary living room.",
                BigDecimal.valueOf(4999),
                "Furniture",
                75,
                "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=600&auto=format&fit=crop"
        );

        createProduct(
                ikea,
                "HEMNES Study Desk",
                "Solid wood desk with spacious drawers and a timeless Scandinavian design, perfect for home offices and students.",
                BigDecimal.valueOf(24999),
                "Office Furniture",
                18,
                "https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?w=600&auto=format&fit=crop"
        );
    }

    private void seedUniqlo() {

        Tenant uniqlo = createTenant(
                "UNIQLO",
                "uniqlo",
                "LifeWear for everyone. Discover minimalist Japanese fashion designed for comfort, quality, and everyday living.",
                "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=600&auto=format&fit=crop"
        );

        createProduct(
                uniqlo,
                "AIRism Oversized Crew Neck T-Shirt",
                "Lightweight and breathable AIRism fabric provides superior comfort with moisture-wicking technology for everyday wear.",
                BigDecimal.valueOf(1990),
                "Clothing",
                120,
                "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600&auto=format&fit=crop"
        );

        createProduct(
                uniqlo,
                "Ultra Light Down Jacket",
                "Compact premium down jacket delivering exceptional warmth while remaining incredibly lightweight and easy to pack.",
                BigDecimal.valueOf(7990),
                "Outerwear",
                65,
                "https://images.unsplash.com/photo-1542272604-787c3835535d?w=600&auto=format&fit=crop"
        );

        createProduct(
                uniqlo,
                "Premium Linen Long Sleeve Shirt",
                "Made from 100% premium European linen, offering exceptional comfort and timeless style for every season.",
                BigDecimal.valueOf(3990),
                "Clothing",
                80,
                "https://images.unsplash.com/photo-1603252109303-2751441dd157?w=600&auto=format&fit=crop"
        );

        createProduct(
                uniqlo,
                "Selvedge Slim Fit Jeans",
                "Japanese-inspired premium selvedge denim crafted for durability, comfort, and everyday versatility.",
                BigDecimal.valueOf(4990),
                "Clothing",
                55,
                "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=600&auto=format&fit=crop"
        );

        createProduct(
                uniqlo,
                "Cargo Utility Jogger Pants",
                "Modern tapered cargo joggers featuring stretch fabric, multiple utility pockets, and all-day comfort.",
                BigDecimal.valueOf(3490),
                "Clothing",
                70,
                "https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=600&auto=format&fit=crop"
        );

        createProduct(
                uniqlo,
                "HEATTECH Crew Neck Long Sleeve",
                "Innovative HEATTECH technology generates and retains body heat while remaining soft, lightweight, and breathable.",
                BigDecimal.valueOf(1990),
                "Winter Wear",
                95,
                "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&auto=format&fit=crop"
        );
    }

    private void seedLego() {

        Tenant lego = createTenant(
                "LEGO Official",
                "lego",
                "Inspire builders of all ages with iconic LEGO sets featuring creativity, engineering, architecture, vehicles, and beloved franchises.",
                "https://images.unsplash.com/photo-1587654780291-39c9404d746b?w=600&auto=format&fit=crop"
        );

        createProduct(
                lego,
                "LEGO Star Wars Millennium Falcon",
                "An incredibly detailed collector's edition of the legendary Millennium Falcon featuring thousands of pieces and authentic Star Wars details.",
                BigDecimal.valueOf(84999),
                "Collector Sets",
                8,
                "https://images.unsplash.com/photo-1587654780291-39c9404d746b?w=600&auto=format&fit=crop"
        );

        createProduct(
                lego,
                "LEGO Technic Ferrari Daytona SP3",
                "A premium Technic supercar featuring advanced engineering, working gearbox, suspension, and authentic Ferrari styling.",
                BigDecimal.valueOf(38999),
                "Technic",
                15,
                "https://images.unsplash.com/photo-1511919884226-fd3cad34687c?w=600&auto=format&fit=crop"
        );

        createProduct(
                lego,
                "LEGO Architecture Tokyo Skyline",
                "Celebrate the iconic skyline of Tokyo with a beautifully designed architectural display model for home or office.",
                BigDecimal.valueOf(6999),
                "Architecture",
                35,
                "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=600&auto=format&fit=crop"
        );

        createProduct(
                lego,
                "LEGO Botanical Collection Orchid",
                "A relaxing building experience that creates a stunning orchid display requiring no watering or maintenance.",
                BigDecimal.valueOf(4999),
                "Botanical Collection",
                45,
                "https://images.unsplash.com/photo-1463320726281-696a485928c7?w=600&auto=format&fit=crop"
        );

        createProduct(
                lego,
                "LEGO Harry Potter Hogwarts Castle",
                "Build the magical Hogwarts Castle featuring iconic towers, classrooms, and beloved characters from the Wizarding World.",
                BigDecimal.valueOf(47999),
                "Collector Sets",
                12,
                "https://images.unsplash.com/photo-1518709268805-4e9042af2176?w=600&auto=format&fit=crop"
        );

        createProduct(
                lego,
                "LEGO Speed Champions McLaren Formula 1",
                "A highly detailed Formula 1 race car model designed for motorsport enthusiasts and LEGO collectors alike.",
                BigDecimal.valueOf(2999),
                "Vehicles",
                70,
                "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=600&auto=format&fit=crop"
        );
    }

    private void seedCanon() {

        Tenant canon = createTenant(
                "Canon Imaging",
                "canon",
                "Capture life's moments with Canon's world-class cameras, lenses, printers, and professional imaging solutions.",
                "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=600&auto=format&fit=crop"
        );

        createProduct(
                canon,
                "Canon EOS R6 Mark II",
                "24.2MP full-frame mirrorless camera featuring exceptional autofocus, 4K 60fps video recording, and outstanding low-light performance.",
                BigDecimal.valueOf(209995),
                "Cameras",
                15,
                "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=600&auto=format&fit=crop"
        );

        createProduct(
                canon,
                "Canon EOS R50",
                "Compact APS-C mirrorless camera designed for beginners, vloggers, and content creators with advanced autofocus.",
                BigDecimal.valueOf(76995),
                "Cameras",
                30,
                "https://images.unsplash.com/photo-1510127034890-ba27508e9f1c?w=600&auto=format&fit=crop"
        );

        createProduct(
                canon,
                "Canon RF 24-70mm f/2.8L IS USM Lens",
                "Professional L-series zoom lens delivering exceptional sharpness, image stabilization, and versatile focal lengths.",
                BigDecimal.valueOf(189995),
                "Camera Lenses",
                12,
                "https://images.unsplash.com/photo-1516724562728-afc824a36e84?w=600&auto=format&fit=crop"
        );

        createProduct(
                canon,
                "Canon PIXMA G3770 MegaTank Printer",
                "High-efficiency wireless all-in-one ink tank printer offering ultra-low printing costs for home and office use.",
                BigDecimal.valueOf(18999),
                "Printers",
                35,
                "https://images.unsplash.com/photo-1612815154858-60aa4c59eaa6?w=600&auto=format&fit=crop"
        );

        createProduct(
                canon,
                "Canon Speedlite EL-5 Flash",
                "Professional external flash unit with powerful illumination, fast recycling, and advanced wireless control.",
                BigDecimal.valueOf(32999),
                "Camera Accessories",
                22,
                "https://images.unsplash.com/photo-1512790182412-b19e6d62bc39?w=600&auto=format&fit=crop"
        );

        createProduct(
                canon,
                "Canon PowerShot V10",
                "Pocket-sized vlogging camera featuring a built-in stand, high-quality microphone, and ultra-portable design for creators.",
                BigDecimal.valueOf(35995),
                "Content Creation",
                28,
                "https://images.unsplash.com/photo-1495707902641-75cac588d2e9?w=600&auto=format&fit=crop"
        );
    }

    private void seedBose() {

        Tenant bose = createTenant(
                "Bose Audio",
                "bose",
                "Experience world-class sound with Bose premium headphones, speakers, soundbars, and home audio systems engineered for exceptional listening.",
                "https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=600&auto=format&fit=crop"
        );

        createProduct(
                bose,
                "Bose QuietComfort Ultra Headphones",
                "Premium wireless noise-cancelling headphones featuring immersive spatial audio, crystal-clear calls, and exceptional all-day comfort.",
                BigDecimal.valueOf(37999),
                "Headphones",
                40,
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop"
        );

        createProduct(
                bose,
                "Bose QuietComfort Ultra Earbuds",
                "Flagship true wireless earbuds delivering industry-leading active noise cancellation, immersive audio, and premium comfort.",
                BigDecimal.valueOf(25999),
                "Earbuds",
                65,
                "https://images.unsplash.com/photo-1606220945770-b5b6c2c55bf1?w=600&auto=format&fit=crop"
        );

        createProduct(
                bose,
                "Bose Smart Soundbar 900",
                "Dolby Atmos premium smart soundbar with immersive room-filling sound, built-in voice assistants, and elegant modern design.",
                BigDecimal.valueOf(89999),
                "Soundbars",
                20,
                "https://images.unsplash.com/photo-1545454675-3531b543be5d?w=600&auto=format&fit=crop"
        );

        createProduct(
                bose,
                "Bose SoundLink Flex Portable Speaker",
                "Water-resistant Bluetooth speaker delivering surprisingly deep, rich audio in a compact and durable portable design.",
                BigDecimal.valueOf(13999),
                "Portable Speakers",
                55,
                "https://images.unsplash.com/photo-1589003077984-894e133dabab?w=600&auto=format&fit=crop"
        );

        createProduct(
                bose,
                "Bose Portable Smart Speaker",
                "Premium portable smart speaker with Wi-Fi, Bluetooth, voice assistant support, and immersive 360-degree sound.",
                BigDecimal.valueOf(34999),
                "Smart Speakers",
                28,
                "https://images.unsplash.com/photo-1543512214-318c7553f230?w=600&auto=format&fit=crop"
        );

        createProduct(
                bose,
                "Bose Bass Module 700",
                "Powerful wireless subwoofer designed to deliver deep, cinematic bass for an unforgettable home theater experience.",
                BigDecimal.valueOf(72999),
                "Home Audio",
                18,
                "https://images.unsplash.com/photo-1558089687-f282ffcbc126?w=600&auto=format&fit=crop"
        );
    }

    private void seedNorthFace() {

        Tenant northFace = createTenant(
                "The North Face",
                "the-north-face",
                "Premium outdoor apparel, backpacks, footwear, and expedition gear engineered for exploration, adventure, and everyday performance.",
                "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=600&auto=format&fit=crop"
        );

        createProduct(
                northFace,
                "1996 Retro Nuptse Jacket",
                "The legendary insulated puffer jacket featuring 700-fill responsibly sourced down, exceptional warmth, and iconic outdoor styling.",
                BigDecimal.valueOf(28999),
                "Outerwear",
                45,
                "https://images.unsplash.com/photo-1542272604-787c3835535d?w=600&auto=format&fit=crop"
        );

        createProduct(
                northFace,
                "Borealis Backpack",
                "Versatile 28-liter backpack featuring the FlexVent suspension system, laptop compartment, and ergonomic comfort for travel and daily commuting.",
                BigDecimal.valueOf(10999),
                "Backpacks",
                70,
                "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600&auto=format&fit=crop"
        );

        createProduct(
                northFace,
                "Base Camp Duffel - Medium",
                "Extremely durable expedition duffel bag built with water-resistant materials and reinforced construction for adventure travel.",
                BigDecimal.valueOf(13999),
                "Travel Gear",
                35,
                "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=600&auto=format&fit=crop"
        );

        createProduct(
                northFace,
                "Summit Series Down Hoodie",
                "Lightweight alpine jacket designed for mountaineering with premium goose down insulation and exceptional packability.",
                BigDecimal.valueOf(34999),
                "Outerwear",
                20,
                "https://images.unsplash.com/photo-1520975954732-35dd22299614?w=600&auto=format&fit=crop"
        );

        createProduct(
                northFace,
                "Hedgehog FUTURELIGHT Hiking Shoes",
                "Waterproof hiking shoes engineered with breathable FUTURELIGHT technology, superior traction, and all-day trail comfort.",
                BigDecimal.valueOf(13499),
                "Footwear",
                40,
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&auto=format&fit=crop"
        );

        createProduct(
                northFace,
                "Apex Bionic Softshell Jacket",
                "Wind-resistant softshell jacket offering excellent mobility, weather protection, and everyday versatility for outdoor adventures.",
                BigDecimal.valueOf(15999),
                "Outerwear",
                55,
                "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&auto=format&fit=crop"
        );
    }

    private void seedApple() {

        Tenant apple = createTenant(
                "Apple Official",
                "apple",
                "Discover Apple's innovative ecosystem featuring iPhone, Mac, iPad, Apple Watch, AirPods, and premium accessories designed to work seamlessly together.",
                "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600&auto=format&fit=crop"
        );

        createProduct(
                apple,
                "iPhone 16 Pro Max (512GB)",
                "Apple's flagship smartphone featuring the A18 Pro chip, titanium design, Apple Intelligence, and an advanced Pro camera system.",
                BigDecimal.valueOf(159900),
                "Smartphones",
                25,
                "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600&auto=format&fit=crop"
        );

        createProduct(
                apple,
                "MacBook Pro 16-inch M4 Max",
                "Professional laptop powered by Apple's M4 Max chip, featuring a Liquid Retina XDR display and exceptional battery life for demanding workflows.",
                BigDecimal.valueOf(399900),
                "Laptops",
                12,
                "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&auto=format&fit=crop"
        );

        createProduct(
                apple,
                "iPad Pro 13-inch M4",
                "Ultra-thin professional tablet with the powerful M4 chip, Ultra Retina XDR display, and Apple Pencil Pro support.",
                BigDecimal.valueOf(129900),
                "Tablets",
                20,
                "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=600&auto=format&fit=crop"
        );

        createProduct(
                apple,
                "Apple Watch Ultra 2",
                "Rugged premium smartwatch featuring precision GPS, advanced health tracking, and outstanding battery life for athletes and adventurers.",
                BigDecimal.valueOf(89900),
                "Wearables",
                35,
                "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop"
        );

        createProduct(
                apple,
                "AirPods Pro (2nd Generation USB-C)",
                "Premium wireless earbuds featuring Adaptive Audio, Active Noise Cancellation, Personalized Spatial Audio, and MagSafe charging.",
                BigDecimal.valueOf(24900),
                "Audio",
                80,
                "https://images.unsplash.com/photo-1606220945770-b5b6c2c55bf1?w=600&auto=format&fit=crop"
        );

        createProduct(
                apple,
                "HomePod (2nd Generation)",
                "High-fidelity smart speaker delivering immersive room-filling sound, Siri integration, and seamless Apple ecosystem connectivity.",
                BigDecimal.valueOf(26900),
                "Smart Speakers",
                30,
                "https://images.unsplash.com/photo-1543512214-318c7553f230?w=600&auto=format&fit=crop"
        );
    }

    private void seedNike() {

        Tenant nike = createTenant(
                "Nike Store",
                "nike",
                "Just Do It. Discover Nike's latest innovations in running, basketball, training, sportswear, and lifestyle footwear engineered for athletes of every level.",
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&auto=format&fit=crop"
        );

        createProduct(
                nike,
                "Nike Air Max DN",
                "Next-generation Air Max sneakers featuring Dynamic Air cushioning for exceptional comfort and a bold modern design.",
                BigDecimal.valueOf(15995),
                "Footwear",
                45,
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&auto=format&fit=crop"
        );

        createProduct(
                nike,
                "Nike Air Force 1 '07",
                "The timeless basketball icon featuring premium leather construction, classic styling, and legendary everyday comfort.",
                BigDecimal.valueOf(10995),
                "Footwear",
                60,
                "https://images.unsplash.com/photo-1600185365483-26d7a4cc7519?w=600&auto=format&fit=crop"
        );

        createProduct(
                nike,
                "Nike Pegasus 41 Running Shoes",
                "Responsive daily running shoes featuring ReactX foam and dual Air Zoom units for smooth, energized performance.",
                BigDecimal.valueOf(12995),
                "Running",
                35,
                "https://images.unsplash.com/photo-1543508282-6319a3e2621f?w=600&auto=format&fit=crop"
        );

        createProduct(
                nike,
                "Nike Tech Fleece Full-Zip Hoodie",
                "Premium lightweight Tech Fleece hoodie delivering warmth, comfort, and a sleek athletic silhouette.",
                BigDecimal.valueOf(11999),
                "Apparel",
                55,
                "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=600&auto=format&fit=crop"
        );

        createProduct(
                nike,
                "Nike Heritage Duffel Bag",
                "Durable gym duffel featuring a spacious main compartment, multiple zip pockets, and an adjustable shoulder strap.",
                BigDecimal.valueOf(4995),
                "Accessories",
                75,
                "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600&auto=format&fit=crop"
        );

        createProduct(
                nike,
                "Nike Dri-FIT Challenger Running Shorts",
                "Lightweight moisture-wicking running shorts designed for maximum comfort and unrestricted movement during training.",
                BigDecimal.valueOf(3995),
                "Apparel",
                90,
                "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&auto=format&fit=crop"
        );
    }

    private void seedAdidas() {

        Tenant adidas = createTenant(
                "Adidas Originals",
                "adidas",
                "Impossible is Nothing. Explore Adidas' iconic footwear, performance sportswear, football gear, and timeless Originals collection.",
                "https://images.unsplash.com/photo-1518002171953-a080ee817e1f?w=600&auto=format&fit=crop"
        );

        createProduct(
                adidas,
                "Adidas Ultraboost 5",
                "Premium running shoes featuring Light BOOST cushioning for outstanding comfort, energy return, and everyday performance.",
                BigDecimal.valueOf(17999),
                "Running",
                35,
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&auto=format&fit=crop"
        );

        createProduct(
                adidas,
                "Adidas Samba OG",
                "The legendary Samba featuring premium leather construction, suede overlays, and timeless streetwear appeal.",
                BigDecimal.valueOf(10999),
                "Footwear",
                60,
                "https://images.unsplash.com/photo-1518002171953-a080ee817e1f?w=600&auto=format&fit=crop"
        );

        createProduct(
                adidas,
                "Adidas Gazelle Indoor",
                "A classic lifestyle sneaker with soft suede uppers, signature 3-Stripes branding, and retro-inspired design.",
                BigDecimal.valueOf(11999),
                "Footwear",
                50,
                "https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=600&auto=format&fit=crop"
        );

        createProduct(
                adidas,
                "Adicolor Classics Track Jacket",
                "Iconic full-zip track jacket featuring recycled materials, classic 3-Stripes styling, and everyday versatility.",
                BigDecimal.valueOf(7999),
                "Apparel",
                70,
                "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600&auto=format&fit=crop"
        );

        createProduct(
                adidas,
                "Tiro 24 Training Pants",
                "Professional football training pants featuring AEROREADY moisture management and a slim athletic fit.",
                BigDecimal.valueOf(5999),
                "Apparel",
                65,
                "https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=600&auto=format&fit=crop"
        );

        createProduct(
                adidas,
                "Adidas Defender 5 Medium Duffel Bag",
                "Durable duffel bag with a spacious main compartment, ventilated shoe pocket, and water-resistant base.",
                BigDecimal.valueOf(5499),
                "Accessories",
                85,
                "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600&auto=format&fit=crop"
        );
    }

    private void seedPuma() {

        Tenant puma = createTenant(
                "Puma Lifestyle",
                "puma",
                "Forever Faster. Explore Puma's performance footwear, motorsport-inspired apparel, lifestyle sneakers, and premium athletic gear.",
                "https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=600&auto=format&fit=crop"
        );

        createProduct(
                puma,
                "Puma Palermo Vintage",
                "Classic terrace-inspired sneakers featuring premium suede construction, retro styling, and exceptional everyday comfort.",
                BigDecimal.valueOf(8999),
                "Footwear",
                55,
                "https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=600&auto=format&fit=crop"
        );

        createProduct(
                puma,
                "Puma Suede XL",
                "A modern interpretation of the iconic Puma Suede with oversized proportions, premium materials, and streetwear appeal.",
                BigDecimal.valueOf(9999),
                "Footwear",
                45,
                "https://images.unsplash.com/photo-1600185365483-26d7a4cc7519?w=600&auto=format&fit=crop"
        );

        createProduct(
                puma,
                "Puma Deviate Nitro Elite 3",
                "Elite racing shoes engineered with NITRO™ Elite foam and a carbon fiber plate for maximum speed and energy return.",
                BigDecimal.valueOf(19999),
                "Running",
                28,
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&auto=format&fit=crop"
        );

        createProduct(
                puma,
                "Puma Essentials Logo Hoodie",
                "Soft fleece hoodie featuring Puma's iconic logo, ribbed cuffs, and a relaxed everyday fit.",
                BigDecimal.valueOf(5999),
                "Apparel",
                65,
                "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=600&auto=format&fit=crop"
        );

        createProduct(
                puma,
                "Puma TeamGOAL Backpack",
                "Durable everyday backpack with padded shoulder straps, laptop compartment, and multiple storage pockets.",
                BigDecimal.valueOf(3999),
                "Accessories",
                85,
                "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600&auto=format&fit=crop"
        );

        createProduct(
                puma,
                "Puma Train Favorite Woven Shorts",
                "Lightweight moisture-wicking training shorts designed for gym workouts, running, and everyday fitness.",
                BigDecimal.valueOf(3499),
                "Apparel",
                90,
                "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&auto=format&fit=crop"
        );
    }

    private void seedReebok() {

        Tenant reebok = createTenant(
                "Reebok Fitness",
                "reebok",
                "Discover Reebok's heritage of fitness innovation with premium training shoes, classic sneakers, activewear, and gym essentials.",
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&auto=format&fit=crop"
        );

        createProduct(
                reebok,
                "Reebok Nano X5",
                "Flagship cross-training shoes built for weightlifting, HIIT workouts, agility drills, and everyday fitness performance.",
                BigDecimal.valueOf(12999),
                "Training",
                40,
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&auto=format&fit=crop"
        );

        createProduct(
                reebok,
                "Reebok Club C 85",
                "Timeless leather sneakers featuring clean minimalist styling and all-day comfort for casual everyday wear.",
                BigDecimal.valueOf(7999),
                "Footwear",
                60,
                "https://images.unsplash.com/photo-1600185365483-26d7a4cc7519?w=600&auto=format&fit=crop"
        );

        createProduct(
                reebok,
                "Reebok Classic Leather",
                "The iconic Reebok sneaker crafted with soft premium leather, durable cushioning, and vintage-inspired style.",
                BigDecimal.valueOf(8999),
                "Footwear",
                55,
                "https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=600&auto=format&fit=crop"
        );

        createProduct(
                reebok,
                "Reebok FloatZig 1 Running Shoes",
                "Lightweight running shoes featuring FloatZig cushioning technology for smooth transitions and maximum comfort.",
                BigDecimal.valueOf(11999),
                "Running",
                35,
                "https://images.unsplash.com/photo-1543508282-6319a3e2621f?w=600&auto=format&fit=crop"
        );

        createProduct(
                reebok,
                "Reebok Identity Fleece Hoodie",
                "Comfortable fleece hoodie featuring a relaxed fit, soft brushed interior, and classic Reebok branding.",
                BigDecimal.valueOf(5499),
                "Apparel",
                70,
                "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=600&auto=format&fit=crop"
        );

        createProduct(
                reebok,
                "Reebok Active Core Duffel Bag",
                "Versatile gym duffel bag with multiple storage compartments, adjustable shoulder strap, and durable construction.",
                BigDecimal.valueOf(4999),
                "Accessories",
                80,
                "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600&auto=format&fit=crop"
        );
    }

    private void seedLevis() {

        Tenant levis = createTenant(
                "Levi's",
                "levis",
                "Since 1853. Discover the world's most iconic denim brand featuring premium jeans, jackets, shirts, and timeless everyday essentials.",
                "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=600&auto=format&fit=crop"
        );

        createProduct(
                levis,
                "Levi's 501 Original Fit Jeans",
                "The legendary straight-leg jeans that started it all, crafted from premium denim with a timeless fit that never goes out of style.",
                BigDecimal.valueOf(7999),
                "Jeans",
                80,
                "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=600&auto=format&fit=crop"
        );

        createProduct(
                levis,
                "Levi's 511 Slim Fit Jeans",
                "Modern slim-fit jeans offering the perfect balance of comfort, stretch, and contemporary everyday style.",
                BigDecimal.valueOf(6999),
                "Jeans",
                75,
                "https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=600&auto=format&fit=crop"
        );

        createProduct(
                levis,
                "Levi's Trucker Denim Jacket",
                "The original denim jacket featuring premium cotton construction, button-front closure, and iconic Levi's craftsmanship.",
                BigDecimal.valueOf(9999),
                "Outerwear",
                45,
                "https://images.unsplash.com/photo-1542272604-787c3835535d?w=600&auto=format&fit=crop"
        );

        createProduct(
                levis,
                "Levi's Sunset One Pocket Shirt",
                "Classic button-down shirt crafted from soft premium cotton, ideal for casual and smart-casual occasions.",
                BigDecimal.valueOf(5999),
                "Shirts",
                60,
                "https://images.unsplash.com/photo-1603252109303-2751441dd157?w=600&auto=format&fit=crop"
        );

        createProduct(
                levis,
                "Levi's Graphic Crewneck T-Shirt",
                "Soft cotton crewneck t-shirt featuring the iconic Levi's logo with a comfortable regular fit.",
                BigDecimal.valueOf(2999),
                "T-Shirts",
                120,
                "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600&auto=format&fit=crop"
        );

        createProduct(
                levis,
                "Levi's Reversible Leather Belt",
                "Premium genuine leather belt with a reversible buckle design suitable for both casual and formal outfits.",
                BigDecimal.valueOf(3499),
                "Accessories",
                90,
                "https://images.unsplash.com/photo-1624222247344-550fb60583dc?w=600&auto=format&fit=crop"
        );
    }
}
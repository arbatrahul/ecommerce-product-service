package org.example.product.service;

import org.example.product.entity.Category;
import org.example.product.repository.jpa.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(1) // Run before product initialization
public class CategoryInitializationService implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeCategories();
    }

    private void initializeCategories() {
        if (categoryRepository.count() == 0) {
            System.out.println("Initializing product categories...");

            // Electronics
            Category electronics = new Category("Electronics", "Electronic devices and gadgets");
            electronics.setDisplayOrder(1);
            electronics = categoryRepository.save(electronics);

            // Electronics subcategories
            Category smartphones = new Category("Smartphones", "Mobile phones and accessories");
            smartphones.setParentId(electronics.getId());
            smartphones.setDisplayOrder(1);
            categoryRepository.save(smartphones);

            Category laptops = new Category("Laptops", "Laptops and notebooks");
            laptops.setParentId(electronics.getId());
            laptops.setDisplayOrder(2);
            categoryRepository.save(laptops);

            Category tablets = new Category("Tablets", "Tablets and e-readers");
            tablets.setParentId(electronics.getId());
            tablets.setDisplayOrder(3);
            categoryRepository.save(tablets);

            Category accessories = new Category("Accessories", "Electronic accessories");
            accessories.setParentId(electronics.getId());
            accessories.setDisplayOrder(4);
            categoryRepository.save(accessories);

            // Clothing
            Category clothing = new Category("Clothing", "Fashion and apparel");
            clothing.setDisplayOrder(2);
            clothing = categoryRepository.save(clothing);

            // Clothing subcategories
            Category menClothing = new Category("Men's Clothing", "Men's fashion and apparel");
            menClothing.setParentId(clothing.getId());
            menClothing.setDisplayOrder(1);
            categoryRepository.save(menClothing);

            Category womenClothing = new Category("Women's Clothing", "Women's fashion and apparel");
            womenClothing.setParentId(clothing.getId());
            womenClothing.setDisplayOrder(2);
            categoryRepository.save(womenClothing);

            Category shoes = new Category("Shoes", "Footwear for all occasions");
            shoes.setParentId(clothing.getId());
            shoes.setDisplayOrder(3);
            categoryRepository.save(shoes);

            // Home & Garden
            Category homeGarden = new Category("Home & Garden", "Home improvement and garden supplies");
            homeGarden.setDisplayOrder(3);
            homeGarden = categoryRepository.save(homeGarden);

            // Home & Garden subcategories
            Category furniture = new Category("Furniture", "Home and office furniture");
            furniture.setParentId(homeGarden.getId());
            furniture.setDisplayOrder(1);
            categoryRepository.save(furniture);

            Category kitchenware = new Category("Kitchenware", "Kitchen appliances and utensils");
            kitchenware.setParentId(homeGarden.getId());
            kitchenware.setDisplayOrder(2);
            categoryRepository.save(kitchenware);

            Category garden = new Category("Garden", "Gardening tools and supplies");
            garden.setParentId(homeGarden.getId());
            garden.setDisplayOrder(3);
            categoryRepository.save(garden);

            // Sports & Outdoors
            Category sports = new Category("Sports & Outdoors", "Sports equipment and outdoor gear");
            sports.setDisplayOrder(4);
            sports = categoryRepository.save(sports);

            // Sports subcategories
            Category fitness = new Category("Fitness", "Fitness equipment and accessories");
            fitness.setParentId(sports.getId());
            fitness.setDisplayOrder(1);
            categoryRepository.save(fitness);

            Category outdoor = new Category("Outdoor Recreation", "Camping, hiking, and outdoor activities");
            outdoor.setParentId(sports.getId());
            outdoor.setDisplayOrder(2);
            categoryRepository.save(outdoor);

            Category teamSports = new Category("Team Sports", "Equipment for team sports");
            teamSports.setParentId(sports.getId());
            teamSports.setDisplayOrder(3);
            categoryRepository.save(teamSports);

            // Books & Media
            Category books = new Category("Books & Media", "Books, movies, music, and games");
            books.setDisplayOrder(5);
            books = categoryRepository.save(books);

            // Books subcategories
            Category booksCategory = new Category("Books", "Physical and digital books");
            booksCategory.setParentId(books.getId());
            booksCategory.setDisplayOrder(1);
            categoryRepository.save(booksCategory);

            Category movies = new Category("Movies & TV", "Movies, TV shows, and entertainment");
            movies.setParentId(books.getId());
            movies.setDisplayOrder(2);
            categoryRepository.save(movies);

            Category games = new Category("Games", "Video games and board games");
            games.setParentId(books.getId());
            games.setDisplayOrder(3);
            categoryRepository.save(games);

            // Health & Beauty
            Category health = new Category("Health & Beauty", "Health, beauty, and personal care products");
            health.setDisplayOrder(6);
            health = categoryRepository.save(health);

            // Health subcategories
            Category skincare = new Category("Skincare", "Skincare and beauty products");
            skincare.setParentId(health.getId());
            skincare.setDisplayOrder(1);
            categoryRepository.save(skincare);

            Category supplements = new Category("Health Supplements", "Vitamins and health supplements");
            supplements.setParentId(health.getId());
            supplements.setDisplayOrder(2);
            categoryRepository.save(supplements);

            Category personalCare = new Category("Personal Care", "Personal hygiene and care products");
            personalCare.setParentId(health.getId());
            personalCare.setDisplayOrder(3);
            categoryRepository.save(personalCare);

            System.out.println("Categories initialized successfully!");
            System.out.println("Created " + categoryRepository.count() + " categories");
        }
    }
}

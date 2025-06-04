package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.LocalDateTime;

/**
 * Головний клас додатку "Воєнторг"
 */
@SpringBootApplication
@EnableTransactionManagement
public class Server {
    public static void main(String[] args) {
        SpringApplication.run(Server.class, args);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

/**
 * Сутності додатку
 */
@Entity
@Table(name = "app_user")
class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String login;
    private String password;
    private String name;
    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role {
        MANAGER, CLIENT
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}

@Entity
class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(length = 2000)
    private String description;
    private Double price;
    private Integer quantity;
    private String imageUrl;
    private String category;

    // Конструктор за замовчуванням
    public Product() {}

    // Конструктор для зручності створення
    public Product(String name, String description, Double price, Integer quantity, String imageUrl, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}

@Entity
@Table(name = "app_order")
class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private AppUser user;

    private LocalDateTime orderDate;
    private Double totalAmount;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}

@Entity
class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private Product product;

    private Integer quantity;
    private Double price;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}

/**
 * Репозиторії для доступу до даних
 */
@Repository
interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByLogin(String login);
}

@Repository
interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    List<Product> findByNameContainingIgnoreCase(String keyword);
}

@Repository
interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(AppUser user);
}

/**
 * Сервіси для бізнес-логіки
 */
@Service
@Transactional
class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Реєстрація нового користувача
     */
    public AppUser register(String login, String password, String name, AppUser.Role role) {
        if (userRepository.findByLogin(login).isPresent()) {
            throw new RuntimeException("Користувач з таким логіном вже існує");
        }

        AppUser user = new AppUser();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setRole(role);

        return userRepository.save(user);
    }

    /**
     * Аутентифікація користувача
     */
    @Transactional(readOnly = true)
    public Optional<AppUser> authenticate(String login, String password) {
        Optional<AppUser> userOpt = userRepository.findByLogin(login);
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            return userOpt;
        }
        return Optional.empty();
    }
}

@Service
@Transactional
class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Отримання всіх товарів
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Отримання товарів за категорією
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    /**
     * Пошук товарів за ключовим словом
     */
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    /**
     * Отримання товару за ID
     */
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Створення нового товару
     */
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    /**
     * Оновлення існуючого товару
     */
    public Product updateProduct(Product product) {
        if (!productRepository.existsById(product.getId())) {
            throw new RuntimeException("Товар не знайдено");
        }
        return productRepository.save(product);
    }

    /**
     * Видалення товару
     */
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    /**
     * Отримання унікальних категорій товарів
     */
    @Transactional(readOnly = true)
    public Set<String> getAllCategories() {
        Set<String> categories = new HashSet<>();
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            if (product.getCategory() != null && !product.getCategory().isEmpty()) {
                categories.add(product.getCategory());
            }
        }
        return categories;
    }
}

@Service
@Transactional
class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    /**
     * Створення нового замовлення
     */
    public Order createOrder(AppUser user, Map<Long, Integer> cartItems) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());

        double totalAmount = 0.0;

        for (Map.Entry<Long, Integer> entry : cartItems.entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Товар не знайдено"));

            if (product.getQuantity() < entry.getValue()) {
                throw new RuntimeException("Недостатня кількість товару: " + product.getName());
            }

            // Оновлення кількості товару
            product.setQuantity(product.getQuantity() - entry.getValue());
            productRepository.save(product);

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(entry.getValue());
            item.setPrice(product.getPrice());

            order.getItems().add(item);
            totalAmount += product.getPrice() * entry.getValue();
        }

        order.setTotalAmount(totalAmount);
        return orderRepository.save(order);
    }

    /**
     * Отримання замовлень користувача
     */
    @Transactional(readOnly = true)
    public List<Order> getUserOrders(AppUser user) {
        return orderRepository.findByUser(user);
    }

    /**
     * Отримання всіх замовлень
     */
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
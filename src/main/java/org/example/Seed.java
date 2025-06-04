package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Random;

/**
 * Клас для початкового заповнення бази даних тестовими даними.
 * Виконується автоматично при запуску додатку.
 */
@Component
public class Seed implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        System.out.println("Запуск скрипта заповнення даних...");

        // Завжди заповнюємо базу, якщо вона пуста
        if (userRepository.count() == 0) {
            seedUsers();
        }

        if (productRepository.count() == 0) {
            seedProducts();
        }

        System.out.println("Заповнення бази даних завершено!");
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    // Список базових URL зображень для товарів
    private final String[] imageUrls = {
            "https://i.imgur.com/u2VQXvA.jpeg", // Тактичний рюкзак
            "https://i.imgur.com/1vqZ7k6.jpeg", // Бронежилет
            "https://i.imgur.com/tpDCBiE.jpeg", // Тактичний шолом
            "https://i.imgur.com/rXbGSL1.jpeg", // Тактичні окуляри
            "https://i.imgur.com/0CPu8WL.png",  // Тактичні рукавиці
            "https://i.imgur.com/3jZQ8xT.jpeg", // Тактичний костюм
            "https://i.imgur.com/GfFf5jU.jpeg", // Взуття тактичне
            "https://i.imgur.com/XJtY9pz.jpeg", // Плитоноска
            "https://i.imgur.com/FgZn0gn.jpeg", // Аптечка
            "https://i.imgur.com/rLZLQ1b.jpeg", // Розвантажувальний жилет
            "https://i.imgur.com/k5vqSBP.jpeg", // Військовий ліхтар
            "https://i.imgur.com/YVdB62P.jpeg", // Тактичний ремінь
            "https://i.imgur.com/3G8zxKc.jpeg", // Спальний мішок
            "https://i.imgur.com/1y5Gq3Q.jpeg", // Турнікет
            "https://i.imgur.com/8SSBiua.jpeg", // Військовий намет
            "https://i.imgur.com/lZGtMnO.jpeg", // Компас
            "https://i.imgur.com/K2KcPsi.jpeg", // Бінокль
            "https://i.imgur.com/SdUDdKm.jpeg", // Військова рація
            "https://i.imgur.com/4LGKXyT.jpeg", // Мультитул
            "https://i.imgur.com/oXrwNVP.jpeg"  // Захисні навушники
    };



    /**
     * Створює початкових користувачів (менеджерів та клієнтів)
     */
    private void seedUsers() {
        System.out.println("Створення початкових користувачів...");

        // Створення менеджерів
        createUser("manager1", "password", "Іван Петренко", AppUser.Role.MANAGER);
        createUser("manager2", "password", "Олена Коваленко", AppUser.Role.MANAGER);
        createUser("manager3", "password", "Максим Шевченко", AppUser.Role.MANAGER);

        // Створення клієнтів
        createUser("client1", "password", "Андрій Мельник", AppUser.Role.CLIENT);
        createUser("client2", "password", "Наталія Бондаренко", AppUser.Role.CLIENT);
        createUser("client3", "password", "Василь Ткаченко", AppUser.Role.CLIENT);

        System.out.println("Користувачів створено успішно!");
    }

    /**
     * Допоміжний метод для створення користувача
     */
    private void createUser(String login, String password, String name, AppUser.Role role) {
        AppUser user = new AppUser();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setRole(role);
        userRepository.save(user);
    }

    /**
     * Створює початкові товари
     */
    private void seedProducts() {
        System.out.println("Створення початкових товарів...");

        // Категорії військових товарів
        String[] categories = {
                "Уніформа", "Спорядження", "Взуття", "Захист", "Тактичне спорядження",
                "Комунікація", "Оптика", "Медичне", "Виживання", "Зброя та амуніція"
        };

        // Суфікси для назв
        String[] suffixes = {
                "Стандарт", "Про", "Тактикал", "Мілітарі", "Елітне",
                "Посилене", "Легке", "Універсальне", "Спеціальне", "Базове"
        };

        // Опис товарів за категоріями
        String[][] productTypes = {
                // Уніформа
                {
                        "Тактичний костюм", "Камуфляжна куртка", "Бойові штани", "Тактична сорочка", "Маскувальний жилет",
                        "Тактичний ремінь", "Балаклава", "Шапка", "Рукавиці тактичні", "Шарф-снуд"
                },
                // Спорядження
                {
                        "Тактичний рюкзак", "Плитоноска", "Розвантажувальний жилет", "Тактичний пояс", "Підсумок для магазинів",
                        "Кобура пістолетна", "Тактичні окуляри", "Підсумок для гранат", "Тактична аптечка", "Радіосумка"
                },
                // Взуття
                {
                        "Тактичні черевики", "Бойові чоботи", "Берці літні", "Берці зимові", "Кросівки тактичні",
                        "Черевики гірські", "Черевики десантні", "Чоботи зимові", "Взуття для спецпідрозділів", "Тапочки польові"
                },
                // Захист
                {
                        "Бронежилет", "Каска", "Захисні окуляри", "Наколінники", "Налокітники",
                        "Тактичні рукавиці", "Шоломофон", "Протиосколкові окуляри", "Балістичний щит", "Бронепластина"
                },
                // Тактичне спорядження
                {
                        "Тактичний ліхтар", "Мультитул", "Компас", "GPS-навігатор", "Тактична палиця",
                        "Трекер", "Карабін", "Військовий годинник", "Тактичний ніж", "Засоби маскування"
                },
                // Комунікація
                {
                        "Військова рація", "Захищений смартфон", "Навушники тактичні", "Антена", "Зарядний пристрій",
                        "Захисний чохол", "Підсилювач сигналу", "Тактичний мікрофон", "Портативна сонячна батарея", "Радіостанція"
                },
                // Оптика
                {
                        "Бінокль", "Прицільний пристрій", "Окуляри нічного бачення", "Тепловізор", "Монокуляр",
                        "Оптичний приціл", "Лазерний дальномір", "Коліматорний приціл", "Оптична трубка", "Оптичний збільшувач"
                },
                // Медичне
                {
                        "Індивідуальна аптечка", "Турнікет", "Бандаж", "Гемостатичний засіб", "Термоковдра",
                        "Шина", "Маска медична", "Перев'язувальний пакет", "Антисептик", "Медичні рукавички"
                },
                // Виживання
                {
                        "Спальний мішок", "Намет", "Сухпайок", "Фільтр для води", "Засоби розпалювання",
                        "Термос", "Казанок", "Мачете", "Складаний стілець", "Рибальський набір"
                },
                // Зброя та амуніція
                {
                        "Підсумок для магазинів", "Кобура", "Чохол для зброї", "Ремінь для зброї", "Збройове мастило",
                        "Набір для чищення зброї", "Тактичний ремінь", "Лазерний цілевказівник", "Розгрузка для магазинів", "Підствольний ліхтар"
                }
        };

        // Додаткові деталі для опису товарів
        String[] materials = {
                "поліестер", "нейлон", "натуральна шкіра", "поліамід", "кевлар",
                "полікарбонат", "титан", "кордура", "нержавіюча сталь", "алюміній"
        };

        String[] features = {
                "водонепроникний", "куленепробивний", "стійкий до пошкоджень", "легкий", "посилений",
                "розширений", "універсальний", "компактний", "модульний", "багатофункціональний"
        };

        String[] colors = {
                "олива", "койот", "чорний", "мультикам", "піксель",
                "хакі", "пустельний", "лісовий", "зимовий", "міський"
        };

        // Генерація 60 товарів
        for (int i = 0; i < 60; i++) {
            int categoryIndex = random.nextInt(categories.length);
            String category = categories[categoryIndex];

            String[] productsInCategory = productTypes[categoryIndex];
            String productType = productsInCategory[random.nextInt(productsInCategory.length)];

            String suffix = suffixes[random.nextInt(suffixes.length)];
            String name = productType + " " + suffix;

            double basePrice = 500 + random.nextInt(20000);
            double price = Math.round(basePrice / 100) * 100; // Округлення до сотень

            int quantity = 10 + random.nextInt(100);

            // Генерація детального опису
            String material = materials[random.nextInt(materials.length)];
            String feature = features[random.nextInt(features.length)];
            String color = colors[random.nextInt(colors.length)];

            StringBuilder descBuilder = new StringBuilder();
            descBuilder.append(name).append(" - високоякісний військовий товар для професійного використання. ");
            descBuilder.append("Категорія: ").append(category).append(". ");
            descBuilder.append("Виготовлений з матеріалу: ").append(material).append(". ");
            descBuilder.append("Характеристики: ").append(feature).append(". ");
            descBuilder.append("Колір: ").append(color).append(". ");
            descBuilder.append("Ідеально підходить для військових, правоохоронців та любителів активного відпочинку. ");
            descBuilder.append("Відповідає найвищим стандартам якості та надійності.");

            String description = descBuilder.toString();

            // Вибір зображення
            String imageUrl = imageUrls[random.nextInt(imageUrls.length)];

            createProduct(name, description, price, quantity, imageUrl, category);
        }

        System.out.println("Товари створено успішно!");
    }

    /**
     * Допоміжний метод для створення товару
     */
    private void createProduct(String name, String description, double price, int quantity, String imageUrl, String category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setImageUrl(imageUrl);
        product.setCategory(category);
        productRepository.save(product);
    }
}

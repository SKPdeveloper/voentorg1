package org.example;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Клієнтський інтерфейс для "Воєнторгу"
 * Використовує кольорову гаму:
 * - Онікс: #0F1F38 (темно-синій)
 * - Сіро-коричневий: #8E7970 (коричневий)
 * - Фейєрверк: #F55449 (червоний)
 * - Океанік: #1B4B5A (синьо-зелений)
 */
@PWA(name = "Воєнторг", shortName = "Воєнторг")
@Push
@CssImport("./styles/voentorg-theme.css")
@Route("")
public class Client extends VerticalLayout implements AppShellConfigurator {

    private static final String COLOR_ONYX = "#080706";
    private static final String COLOR_TAUPE = "#EFEFEF";
    private static final String COLOR_FIREWORK = "#D1B280";
    private static final String COLOR_OCEANIC = "#594D46";

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;

    private AppUser currentUser;
    private final Map<Long, Integer> cart = new HashMap<>();

    private VerticalLayout mainContent;
    private VerticalLayout catalogContent;
    private VerticalLayout cartContent;
    private VerticalLayout orderHistoryContent;
    private VerticalLayout productManagementContent;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("uk", "UA"));

    @Autowired
    public Client(UserService userService, ProductService productService, OrderService orderService) {
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;

        setSizeFull();
        setMargin(true);
        setSpacing(true);
        getStyle().set("background-color", COLOR_ONYX);

        // Ініціалізація головного вмісту
        mainContent = new VerticalLayout();
        mainContent.setSizeFull();
        mainContent.setSpacing(true);
        mainContent.setPadding(true);

        // Заголовок додатку
        H1 title = new H1("Воєнторг");
        title.getStyle().set("color", COLOR_FIREWORK);
        title.getStyle().set("text-align", "center");

        // Спочатку показуємо форму входу
        add(title, createLoginForm());
    }

    /**
     * Створює форму входу або реєстрації
     */
    private Component createLoginForm() {
        VerticalLayout form = new VerticalLayout();
        form.setWidth("400px");
        form.getStyle().set("margin", "auto");
        form.getStyle().set("background-color", COLOR_OCEANIC);
        form.getStyle().set("padding", "20px");
        form.getStyle().set("border-radius", "10px");

        H2 formTitle = new H2("Вхід");
        formTitle.getStyle().set("color", "white");
        formTitle.getStyle().set("text-align", "center");

        TextField login = new TextField("Логін");
        login.setWidth("100%");

        PasswordField password = new PasswordField("Пароль");
        password.setWidth("100%");

        Button loginButton = new Button("Увійти");
        loginButton.getStyle().set("background-color", COLOR_FIREWORK);
        loginButton.getStyle().set("color", "white");
        loginButton.setWidth("100%");

        loginButton.addClickListener(e -> {
            String enteredLogin = login.getValue();
            String enteredPassword = password.getValue();

            if (enteredLogin.isEmpty() || enteredPassword.isEmpty()) {
                Notification.show("Будь ласка, заповніть всі поля", 3000, Notification.Position.MIDDLE);
                return;
            }

            Optional<AppUser> userOpt = userService.authenticate(enteredLogin, enteredPassword);
            if (userOpt.isPresent()) {
                currentUser = userOpt.get();
                displayMainInterface();
            } else {
                Notification.show("Невірний логін або пароль", 3000, Notification.Position.MIDDLE);
            }
        });

        Button registerButton = new Button("Реєстрація");
        registerButton.getStyle().set("background-color", COLOR_TAUPE);
        registerButton.getStyle().set("color", "white");
        registerButton.setWidth("100%");

        registerButton.addClickListener(e -> {
            openRegistrationForm();
        });

        form.add(formTitle, login, password, loginButton, registerButton);
        return form;
    }

    /**
     * Відкриває діалог реєстрації нового користувача
     */
    private void openRegistrationForm() {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");

        VerticalLayout form = new VerticalLayout();
        form.setWidth("100%");
        form.setPadding(true);
        form.setSpacing(true);

        H2 title = new H2("Реєстрація нового користувача");
        title.getStyle().set("color", COLOR_FIREWORK);
        title.getStyle().set("text-align", "center");

        TextField login = new TextField("Логін");
        login.setWidth("100%");

        PasswordField password = new PasswordField("Пароль");
        password.setWidth("100%");

        TextField name = new TextField("Ім'я");
        name.setWidth("100%");

        ComboBox<AppUser.Role> role = new ComboBox<>("Роль");
        role.setItems(AppUser.Role.values());
        role.setWidth("100%");
        role.setItemLabelGenerator(r -> r == AppUser.Role.MANAGER ? "Менеджер" : "Клієнт");

        Button registerButton = new Button("Зареєструватися");
        registerButton.getStyle().set("background-color", COLOR_FIREWORK);
        registerButton.getStyle().set("color", "white");
        registerButton.setWidth("100%");

        registerButton.addClickListener(e -> {
            String enteredLogin = login.getValue();
            String enteredPassword = password.getValue();
            String enteredName = name.getValue();
            AppUser.Role selectedRole = role.getValue();

            if (enteredLogin.isEmpty() || enteredPassword.isEmpty() || enteredName.isEmpty() || selectedRole == null) {
                Notification.show("Будь ласка, заповніть всі поля", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {
                AppUser newUser = userService.register(enteredLogin, enteredPassword, enteredName, selectedRole);
                dialog.close();
                Notification.show("Реєстрація успішна! Тепер ви можете увійти", 3000, Notification.Position.MIDDLE);
            } catch (Exception ex) {
                Notification.show("Помилка реєстрації: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });

        Button cancelButton = new Button("Скасувати");
        cancelButton.addClickListener(e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(registerButton, cancelButton);
        buttons.setWidth("100%");

        form.add(title, login, password, name, role, buttons);
        dialog.add(form);
        dialog.open();
    }

    /**
     * Відображає головний інтерфейс після успішної авторизації
     */
    private void displayMainInterface() {
        removeAll();

        H1 title = new H1("Воєнторг");
        title.getStyle().set("color", COLOR_FIREWORK);
        title.getStyle().set("margin", "0");

        String roleText = currentUser.getRole() == AppUser.Role.MANAGER ? "Менеджер" : "Клієнт";
        Span greeting = new Span("Вітаємо, " + currentUser.getName() + " (" + roleText + ")");
        greeting.getStyle().set("color", "white");

        Button logoutButton = new Button("Вийти");
        logoutButton.getStyle().set("background-color", COLOR_FIREWORK);
        logoutButton.addClickListener(e -> {
            currentUser = null;
            cart.clear();
            removeAll();
            add(title, createLoginForm());
        });

        Span cartCounter = new Span(getCartCountText());
        cartCounter.getStyle().set("color", "white");

        HorizontalLayout topPanel = new HorizontalLayout(title, greeting, cartCounter, logoutButton);
        topPanel.setWidth("100%");
        topPanel.setAlignItems(FlexComponent.Alignment.CENTER);
        topPanel.expand(title);

        // Створення вкладок
        Tab catalogTab = new Tab("Каталог товарів");
        Tab cartTab = new Tab("Кошик");
        Tab orderHistoryTab = new Tab("Історія замовлень");

        Tabs tabs;
        if (currentUser.getRole() == AppUser.Role.MANAGER) {
            Tab productManagementTab = new Tab("Управління товарами");
            tabs = new Tabs(catalogTab, cartTab, orderHistoryTab, productManagementTab);
        } else {
            tabs = new Tabs(catalogTab, cartTab, orderHistoryTab);
        }

        tabs.setWidth("100%");
        tabs.getStyle().set("background-color", COLOR_OCEANIC);

        // Створення контейнерів для вмісту вкладок
        catalogContent = new VerticalLayout();
        catalogContent.setSizeFull();
        catalogContent.setPadding(true);
        catalogContent.setSpacing(true);
        catalogContent.getStyle().set("overflow", "auto");
        catalogContent.setVisible(true);
        populateCatalog();

        cartContent = new VerticalLayout();
        cartContent.setSizeFull();
        cartContent.setPadding(true);
        cartContent.setSpacing(true);
        cartContent.getStyle().set("overflow", "auto");
        cartContent.setVisible(false);
        updateCart();

        orderHistoryContent = new VerticalLayout();
        orderHistoryContent.setSizeFull();
        orderHistoryContent.setPadding(true);
        orderHistoryContent.setSpacing(true);
        orderHistoryContent.getStyle().set("overflow", "auto");
        orderHistoryContent.setVisible(false);
        populateOrderHistory();

        productManagementContent = new VerticalLayout();
        productManagementContent.setSizeFull();
        productManagementContent.setPadding(true);
        productManagementContent.setSpacing(true);
        productManagementContent.getStyle().set("overflow", "auto");
        productManagementContent.setVisible(false);

        if (currentUser.getRole() == AppUser.Role.MANAGER) {
            populateProductManagement();
            mainContent = new VerticalLayout(catalogContent, cartContent, orderHistoryContent, productManagementContent);
        } else {
            mainContent = new VerticalLayout(catalogContent, cartContent, orderHistoryContent);
        }

        mainContent.setSizeFull();
        mainContent.setPadding(false);
        mainContent.setSpacing(false);

        // Обробник зміни вкладок
        tabs.addSelectedChangeListener(event -> {
            catalogContent.setVisible(false);
            cartContent.setVisible(false);
            orderHistoryContent.setVisible(false);
            if (currentUser.getRole() == AppUser.Role.MANAGER) {
                productManagementContent.setVisible(false);
            }

            Tab selectedTab = tabs.getSelectedTab();
            if (selectedTab.equals(catalogTab)) {
                populateCatalog();
                catalogContent.setVisible(true);
            } else if (selectedTab.equals(cartTab)) {
                updateCart();
                cartContent.setVisible(true);
                cartCounter.setText(getCartCountText());
            } else if (selectedTab.equals(orderHistoryTab)) {
                populateOrderHistory();
                orderHistoryContent.setVisible(true);
            } else if (currentUser.getRole() == AppUser.Role.MANAGER) {
                populateProductManagement();
                productManagementContent.setVisible(true);
            }
        });

        add(topPanel, tabs, mainContent);
    }

    /**
     * Повертає текст з кількістю товарів у кошику
     */
    private String getCartCountText() {
        int totalItems = cart.values().stream().mapToInt(Integer::intValue).sum();
        return "Товарів у кошику: " + totalItems;
    }

    /**
     * Заповнює каталог товарів у вигляді карток
     */
    private void populateCatalog() {
        catalogContent.removeAll();

        H2 title = new H2("Каталог товарів");
        title.getStyle().set("color", COLOR_FIREWORK);
        title.getStyle().set("text-align", "center");

        // Отримання категорій товарів
        Set<String> categories = productService.getAllCategories();
        List<String> sortedCategories = new ArrayList<>(categories);
        Collections.sort(sortedCategories);

        // Додавання фільтрів
        ComboBox<String> categoryFilter = new ComboBox<>("Категорія");
        categoryFilter.setItems(sortedCategories);
        categoryFilter.setPlaceholder("Всі категорії");
        categoryFilter.setWidth("250px");

        TextField searchField = new TextField("Пошук");
        searchField.setPlaceholder("Введіть назву товару");
        searchField.setWidth("250px");

        Button searchButton = new Button("Знайти");
        searchButton.getStyle().set("background-color", COLOR_FIREWORK);
        searchButton.getStyle().set("color", "white");

        Button resetButton = new Button("Скинути фільтри");
        resetButton.getStyle().set("background-color", COLOR_TAUPE);
        resetButton.getStyle().set("color", "white");

        HorizontalLayout filterLayout = new HorizontalLayout(categoryFilter, searchField, searchButton, resetButton);
        filterLayout.setAlignItems(FlexComponent.Alignment.END);
        filterLayout.setPadding(true);
        filterLayout.setSpacing(true);

        // Контейнер для карток товарів
        Div cardsContainer = new Div();
        cardsContainer.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "20px")
                .set("justify-content", "center")
                .set("padding", "20px");

        // Функція для оновлення списку товарів
        Runnable updateProducts = () -> {
            cardsContainer.removeAll();

            List<Product> products;
            String selectedCategory = categoryFilter.getValue();
            String searchText = searchField.getValue();

            if (selectedCategory != null && !selectedCategory.isEmpty()) {
                products = productService.getProductsByCategory(selectedCategory);
                if (searchText != null && !searchText.isEmpty()) {
                    products = products.stream()
                            .filter(p -> p.getName().toLowerCase().contains(searchText.toLowerCase()))
                            .collect(Collectors.toList());
                }
            } else if (searchText != null && !searchText.isEmpty()) {
                products = productService.searchProducts(searchText);
            } else {
                products = productService.getAllProducts();
            }

            if (products.isEmpty()) {
                Span noProducts = new Span("Товари не знайдено");
                noProducts.getStyle().set("color", "white").set("font-size", "18px");
                cardsContainer.add(noProducts);
                return;

            }

            for (Product product : products) {
                // Створення картки товару
                Div card = new Div();
                card.getStyle()
                        .set("display", "flex")
                        .set("flex-direction", "column")
                        .set("width", "300px")
                        .set("height", "450px")
                        .set("border-radius", "10px")
                        .set("overflow", "hidden")
                        .set("box-shadow", "0 4px 8px rgba(0,0,0,0.2)")
                        .set("background-color", "white")
                        .set("transition", "transform 0.3s")
                        .set("cursor", "pointer");

                card.addClickListener(e -> {
                    showProductDetails(product);
                });

                // При наведенні миші
                card.getElement().addEventListener("mouseover", event ->
                        card.getStyle().set("transform", "scale(1.02)"));
                card.getElement().addEventListener("mouseout", event ->
                        card.getStyle().set("transform", "scale(1)"));

                // Зображення товару
                Div imageContainer = new Div();
                imageContainer.getStyle()
                        .set("height", "200px")
                        .set("width", "100%")
                        .set("overflow", "hidden");

                // Якщо є зображення, показуємо його
                if (product.getImageUrl() == null || product.getImageUrl().isEmpty()) {
                    imageContainer.getStyle().set("background-color", COLOR_OCEANIC)
                            .set("display", "flex")
                            .set("justify-content", "center")
                            .set("align-items", "center");
                    Span imagePlaceholder = new Span("Фото відсутнє");
                    imagePlaceholder.getStyle()
                            .set("color", "white")
                            .set("display", "flex")
                            .set("justify-content", "center")
                            .set("align-items", "center")
                            .set("height", "100%");
                    imageContainer.add(imagePlaceholder);
                }

                // Інформація про товар
                Div infoContainer = new Div();
                infoContainer.getStyle()
                        .set("padding", "15px")
                        .set("display", "flex")
                        .set("flex-direction", "column")
                        .set("flex-grow", "1")
                        .set("justify-content", "space-between");

                // Категорія товару
                Span category = new Span(product.getCategory() != null ? product.getCategory() : "");
                category.getStyle()
                        .set("color", COLOR_OCEANIC)
                        .set("font-size", "14px")
                        .set("margin-bottom", "5px");

                // Назва товару
                H3 productName = new H3(product.getName());
                productName.getStyle()
                        .set("margin", "0 0 10px 0")
                        .set("font-size", "18px")
                        .set("color", COLOR_ONYX)
                        .set("overflow", "hidden")
                        .set("text-overflow", "ellipsis")
                        .set("display", "-webkit-box")
                        .set("-webkit-line-clamp", "2")
                        .set("-webkit-box-orient", "vertical")
                        .set("height", "50px");

                // Опис товару (скорочений)
                String shortDescription = product.getDescription() != null ?
                        (product.getDescription().length() > 100 ?
                                product.getDescription().substring(0, 97) + "..." :
                                product.getDescription()) :
                        "";

                Paragraph description = new Paragraph(shortDescription);
                description.getStyle()
                        .set("margin", "0 0 10px 0")
                        .set("font-size", "14px")
                        .set("color", "#666")
                        .set("overflow", "hidden")
                        .set("text-overflow", "ellipsis")
                        .set("display", "-webkit-box")
                        .set("-webkit-line-clamp", "3")
                        .set("-webkit-box-orient", "vertical")
                        .set("height", "60px");

                // Ціна і кнопка додавання в кошик
                HorizontalLayout priceAndButton = new HorizontalLayout();
                priceAndButton.setWidthFull();
                priceAndButton.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
                priceAndButton.setAlignItems(FlexComponent.Alignment.CENTER);

                Span price = new Span(currencyFormat.format(product.getPrice()));
                price.getStyle()
                        .set("font-weight", "bold")
                        .set("font-size", "18px")
                        .set("color", COLOR_FIREWORK);

                Button addToCartButton = new Button("У кошик");
                addToCartButton.getStyle()
                        .set("background-color", COLOR_FIREWORK)
                        .set("color", "white")
                        .set("cursor", "pointer");

                // Якщо товар відсутній на складі
                if (product.getQuantity() <= 0) {
                    addToCartButton.setText("Немає в наявності");
                    addToCartButton.getStyle().set("background-color", "#999");
                    addToCartButton.setEnabled(false);
                } else {
                    addToCartButton.addClickListener(event -> {
                        // Додаємо обхідний шлях для запобігання поширенню події
                        event.getSource().getElement().executeJs("event.stopPropagation()");
                        addToCart(product);
                    });
                }

                // Кількість товару на складі
                Span quantity = new Span("В наявності: " + product.getQuantity());
                quantity.getStyle()
                        .set("font-size", "14px")
                        .set("color", "#666")
                        .set("margin-top", "5px");

                priceAndButton.add(price, addToCartButton);

                // Додавання всіх елементів до контейнера інформації
                infoContainer.add(category, productName, description, priceAndButton, quantity);

                // Додавання зображення та інформації до картки
                card.add(imageContainer, infoContainer);
                cardsContainer.add(card);
            }
        };

        // Додавання обробників подій для пошуку та фільтрації
        searchButton.addClickListener(e -> updateProducts.run());
        categoryFilter.addValueChangeListener(e -> updateProducts.run());
        resetButton.addClickListener(e -> {
            categoryFilter.clear();
            searchField.clear();
            updateProducts.run();
        });

        // Додавання до верстки
        catalogContent.add(title, filterLayout, cardsContainer);

        // Початкове заповнення товарами
        updateProducts.run();
    }

    /**
     * Відображає деталі товару
     */
    private void showProductDetails(Product product) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setHeight("600px");

        // Заголовок
        H2 title = new H2(product.getName());
        title.getStyle()
                .set("margin", "0")
                .set("color", COLOR_OCEANIC);

        // Закриття діалогу
        Button closeButton = new Button("×");
        closeButton.getStyle()
                .set("background", "none")
                .set("border", "none")
                .set("font-size", "24px")
                .set("cursor", "pointer")
                .set("color", COLOR_FIREWORK);
        closeButton.addClickListener(e -> dialog.close());

        // Заголовок діалогу
        HorizontalLayout header = new HorizontalLayout(title, closeButton);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        // Контент
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        content.setSizeFull();

        // Розділення на дві колонки: зображення та інформація
        HorizontalLayout mainContent = new HorizontalLayout();
        mainContent.setSizeFull();
        mainContent.setPadding(false);
        mainContent.setSpacing(true);

        // Колонка зображення
        Div imageColumn = new Div();
        imageColumn.setWidth("50%");
        imageColumn.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("overflow", "hidden")
                .set("background-color", "#f5f5f5");

        // Для контейнера зображення
        Div imageContainer = new Div();
        imageContainer.getStyle()
                .set("height", "200px")
                .set("width", "100%")
                .set("overflow", "hidden");

        // Якщо є зображення, показуємо його
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Image productImage = new Image(product.getImageUrl(), product.getName());
            productImage.getStyle()
                    .set("width", "100%")
                    .set("height", "100%")
                    .set("object-fit", "cover");
            imageContainer.add(productImage);
        } else {
            // Якщо немає зображення, показуємо заглушку
            imageContainer.getStyle()
                    .set("background-color", COLOR_OCEANIC)
                    .set("display", "flex")
                    .set("justify-content", "center")
                    .set("align-items", "center");
            Span imagePlaceholder = new Span("Фото відсутнє");
            imagePlaceholder.getStyle()
                    .set("color", "white")
                    .set("display", "flex")
                    .set("justify-content", "center")
                    .set("align-items", "center")
                    .set("height", "100%");
            imageContainer.add(imagePlaceholder);
        }

        // Колонка інформації
        VerticalLayout infoColumn = new VerticalLayout();
        infoColumn.setWidth("50%");
        infoColumn.setPadding(true);
        infoColumn.setSpacing(true);

        // Категорія
        Span category = new Span("Категорія: " + (product.getCategory() != null ? product.getCategory() : "Не вказана"));
        category.getStyle()
                .set("color", COLOR_OCEANIC)
                .set("font-style", "italic");

        // Ціна
        H3 price = new H3("Ціна: " + currencyFormat.format(product.getPrice()));
        price.getStyle().set("color", COLOR_FIREWORK).set("margin", "10px 0");

        // Наявність
        Span availability = new Span(product.getQuantity() > 0 ?
                "В наявності: " + product.getQuantity() + " шт." :
                "Немає в наявності");
        availability.getStyle().set("color", product.getQuantity() > 0 ? "green" : "red");

        // Опис
        Div descriptionContainer = new Div();
        descriptionContainer.setWidthFull();
        descriptionContainer.getStyle()
                .set("margin-top", "20px")
                .set("margin-bottom", "20px")
                .set("max-height", "200px")
                .set("overflow-y", "auto")
                .set("padding", "10px")
                .set("background-color", "#f9f9f9")
                .set("border-radius", "5px");

        Paragraph description = new Paragraph(product.getDescription() != null ? product.getDescription() : "Опис відсутній");
        descriptionContainer.add(description);

        // Кнопка "Додати до кошика"
        Button addToCartButton = new Button("Додати до кошика");
        addToCartButton.getStyle()
                .set("background-color", COLOR_FIREWORK)
                .set("color", "white")
                .set("width", "100%")
                .set("margin-top", "20px");

        // Якщо товар закінчився
        if (product.getQuantity() <= 0) {
            addToCartButton.setText("Немає в наявності");
            addToCartButton.setEnabled(false);
            addToCartButton.getStyle().set("background-color", "#999");
        } else {
            // Селектор кількості
            NumberField quantityField = new NumberField("Кількість");
            quantityField.setValue(1.0);
            quantityField.setMin(1);
            quantityField.setMax(product.getQuantity());
            quantityField.setStep(1);
            quantityField.setWidth("100%");

            addToCartButton.addClickListener(e -> {
                int quantity = quantityField.getValue().intValue();
                if (cart.containsKey(product.getId())) {
                    cart.put(product.getId(), cart.get(product.getId()) + quantity);
                } else {
                    cart.put(product.getId(), quantity);
                }
                Notification.show("Товар додано в кошик", 2000, Notification.Position.MIDDLE);
                dialog.close();
            });

            infoColumn.add(quantityField);
        }

        infoColumn.add(category, price, availability, descriptionContainer, addToCartButton);

        mainContent.add(imageColumn, infoColumn);
        content.add(mainContent);

        dialog.add(header, content);
        dialog.open();
    }

    /**
     * Швидке додавання товару в кошик
     */
    private void addToCart(Product product) {
        if (product.getQuantity() <= 0) {
            Notification.show("Товар відсутній на складі", 2000, Notification.Position.MIDDLE);
            return;
        }

        if (cart.containsKey(product.getId())) {
            cart.put(product.getId(), cart.get(product.getId()) + 1);
        } else {
            cart.put(product.getId(), 1);
        }

        Notification.show("Товар додано в кошик", 2000, Notification.Position.MIDDLE);
    }

    /**
     * Оновлює вміст кошика
     */
    private void updateCart() {
        cartContent.removeAll();

        H2 title = new H2("Кошик");
        title.getStyle().set("color", COLOR_FIREWORK);
        title.getStyle().set("text-align", "center");

        if (cart.isEmpty()) {
            // Порожній кошик
            VerticalLayout emptyCartLayout = new VerticalLayout();
            emptyCartLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            emptyCartLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
            emptyCartLayout.setWidthFull();
            emptyCartLayout.setHeightFull();

            H3 emptyMessage = new H3("Ваш кошик порожній");
            emptyMessage.getStyle().set("color", "white");

            Button continueShoppingButton = new Button("Перейти до каталогу");
            continueShoppingButton.getStyle()
                    .set("background-color", COLOR_FIREWORK)
                    .set("color", "white");

            continueShoppingButton.addClickListener(e -> {
                catalogContent.setVisible(true);
                cartContent.setVisible(false);
                populateCatalog();
            });

            emptyCartLayout.add(emptyMessage, continueShoppingButton);
            cartContent.add(title, emptyCartLayout);
            return;
        }

        // Наповнений кошик
        Div cartItemsContainer = new Div();
        cartItemsContainer.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "15px")
                .set("margin-bottom", "20px")
                .set("padding", "15px")
                .set("background-color", "rgba(255,255,255,0.1)")
                .set("border-radius", "8px");

        double totalSum = 0.0;

        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            Optional<Product> productOpt = productService.getProductById(productId);
            if (productOpt.isEmpty()) continue;

            Product product = productOpt.get();
            double itemTotal = product.getPrice() * quantity;
            totalSum += itemTotal;

            // Карточка товару в кошику
            HorizontalLayout cartItemCard = new HorizontalLayout();
            cartItemCard.setWidthFull();
            cartItemCard.setPadding(true);
            cartItemCard.setSpacing(true);
            cartItemCard.getStyle()
                    .set("background-color", "white")
                    .set("border-radius", "8px")
                    .set("padding", "10px");

            // Зображення товару
            Div imageContainer = new Div();
            imageContainer.setWidth("100px");
            imageContainer.setHeight("100px");
            imageContainer.getStyle()
                    .set("overflow", "hidden")
                    .set("border-radius", "4px");

            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Image productImage = new Image(product.getImageUrl(), product.getName());
                productImage.setWidth("100%");
                productImage.setHeight("100%");
                productImage.getStyle().set("object-fit", "cover");
                imageContainer.add(productImage);
            } else {
                imageContainer.getStyle()
                        .set("background-color", COLOR_OCEANIC)
                        .set("display", "flex")
                        .set("align-items", "center")
                        .set("justify-content", "center");
                Span noImage = new Span("Фото відсутнє");
                noImage.getStyle().set("color", "white");
                imageContainer.add(noImage);
            }

            // Інформація про товар
            VerticalLayout itemInfo = new VerticalLayout();
            itemInfo.setPadding(false);
            itemInfo.setSpacing(false);
            itemInfo.setFlexGrow(1, itemInfo);

            // Назва товару
            H3 productName = new H3(product.getName());
            productName.getStyle()
                    .set("margin", "0")
                    .set("font-size", "16px");

            // Ціна за одиницю
            Span unitPrice = new Span("Ціна: " + currencyFormat.format(product.getPrice()));

            // Загальна сума за позицію
            Span totalPrice = new Span("Сума: " + currencyFormat.format(itemTotal));
            totalPrice.getStyle().set("font-weight", "bold");

            itemInfo.add(productName, unitPrice, totalPrice);

            // Управління кількістю
            VerticalLayout quantityControl = new VerticalLayout();
            quantityControl.setPadding(false);
            quantityControl.setSpacing(false);
            quantityControl.setWidth("120px");
            quantityControl.setAlignItems(FlexComponent.Alignment.CENTER);

            // Кнопки +/-
            HorizontalLayout quantityButtons = new HorizontalLayout();

            Button decreaseButton = new Button("-");
            decreaseButton.getStyle()
                    .set("min-width", "32px")
                    .set("height", "32px")
                    .set("padding", "0")
                    .set("border-radius", "4px")
                    .set("background-color", COLOR_OCEANIC)
                    .set("color", "white");

            Span quantityDisplay = new Span(quantity.toString());
            quantityDisplay.getStyle()
                    .set("min-width", "32px")
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("justify-content", "center")
                    .set("font-weight", "bold");

            Button increaseButton = new Button("+");
            increaseButton.getStyle()
                    .set("min-width", "32px")
                    .set("height", "32px")
                    .set("padding", "0")
                    .set("border-radius", "4px")
                    .set("background-color", COLOR_OCEANIC)
                    .set("color", "white");

            decreaseButton.addClickListener(e -> {
                if (quantity > 1) {
                    cart.put(productId, quantity - 1);
                    updateCart();
                }
            });

            increaseButton.addClickListener(e -> {
                if (quantity < product.getQuantity()) {
                    cart.put(productId, quantity + 1);
                    updateCart();
                } else {
                    Notification.show("Досягнуто максимальну кількість товару",
                            2000, Notification.Position.MIDDLE);
                }
            });

            quantityButtons.add(decreaseButton, quantityDisplay, increaseButton);
            quantityButtons.setAlignItems(FlexComponent.Alignment.CENTER);

            // Кнопка видалення
            Button removeButton = new Button("Видалити");
            removeButton.getStyle()
                    .set("background-color", COLOR_FIREWORK)
                    .set("color", "white")
                    .set("margin-top", "5px");

            removeButton.addClickListener(e -> {
                cart.remove(productId);
                updateCart();
            });

            quantityControl.add(quantityButtons, removeButton);

            cartItemCard.add(imageContainer, itemInfo, quantityControl);
            cartItemsContainer.add(cartItemCard);
        }

        // Підсумок та кнопка оформлення замовлення
        Div cartSummary = new Div();
        cartSummary.getStyle()
                .set("padding", "15px")
                .set("background-color", "rgba(255,255,255,0.1)")
                .set("border-radius", "8px")
                .set("margin-bottom", "20px");

        // Загальна сума
        H3 totalSumHeading = new H3("Загальна сума: " + currencyFormat.format(totalSum));
        totalSumHeading.getStyle()
                .set("color", "white")
                .set("margin", "0 0 15px 0");

        // Кнопка оформлення замовлення
        Button checkoutButton = new Button("Оформити замовлення");
        checkoutButton.getStyle()
                .set("background-color", COLOR_FIREWORK)
                .set("color", "white")
                .set("font-size", "18px")
                .set("padding", "10px 20px")
                .set("width", "100%");

        checkoutButton.addClickListener(e -> {
            try {
                orderService.createOrder(currentUser, cart);
                cart.clear();
                Notification.show("Замовлення успішно створено!", 3000, Notification.Position.MIDDLE);
                updateCart();
            } catch (Exception ex) {
                Notification.show("Помилка при створенні замовлення: " + ex.getMessage(),
                        3000, Notification.Position.MIDDLE);
            }
        });

        cartSummary.add(totalSumHeading, checkoutButton);

        Button continueShopping = new Button("Продовжити покупки");
        continueShopping.getStyle()
                .set("background-color", COLOR_TAUPE)
                .set("color", "white")
                .set("width", "100%");

        continueShopping.addClickListener(e -> {
            catalogContent.setVisible(true);
            cartContent.setVisible(false);
            populateCatalog();
        });

        Div buttonContainer = new Div(continueShopping);
        buttonContainer.getStyle().set("margin-bottom", "20px");

        cartContent.add(title, cartItemsContainer, cartSummary, buttonContainer);
    }

    /**
     * Заповнює історію замовлень
     */
    private void populateOrderHistory() {
        orderHistoryContent.removeAll();

        H2 title = new H2("Історія замовлень");
        title.getStyle().set("color", COLOR_FIREWORK);
        title.getStyle().set("text-align", "center");

        List<Order> orders;
        if (currentUser.getRole() == AppUser.Role.MANAGER) {
            orders = orderService.getAllOrders();
        } else {
            orders = orderService.getUserOrders(currentUser);
        }

        if (orders.isEmpty()) {
            Div emptyOrdersMessage = new Div();
            emptyOrdersMessage.getStyle()
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("justify-content", "center")
                    .set("height", "200px")
                    .set("color", "white")
                    .set("font-size", "18px");
            emptyOrdersMessage.setText("У вас ще немає замовлень");

            orderHistoryContent.add(title, emptyOrdersMessage);
            return;
        }

        // Сортування замовлень за датою (нові спочатку)
        orders.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()));

        Div ordersContainer = new Div();
        ordersContainer.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "20px")
                .set("padding", "15px");

        for (Order order : orders) {
            // Картка замовлення
            Div orderCard = new Div();
            orderCard.getStyle()
                    .set("background-color", "white")
                    .set("border-radius", "8px")
                    .set("padding", "15px")
                    .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

            // Заголовок замовлення
            HorizontalLayout orderHeader = new HorizontalLayout();
            orderHeader.setWidthFull();
            orderHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

            // Номер і дата замовлення
            Div orderInfo = new Div();

            H3 orderNumber = new H3("Замовлення №" + order.getId());
            orderNumber.getStyle()
                    .set("margin", "0")
                    .set("color", COLOR_OCEANIC);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            Span orderDate = new Span("Дата: " + order.getOrderDate().format(formatter));

            orderInfo.add(orderNumber, orderDate);

            // Загальна сума
            Span orderTotal = new Span("Сума: " + currencyFormat.format(order.getTotalAmount()));
            orderTotal.getStyle()
                    .set("font-weight", "bold")
                    .set("font-size", "18px")
                    .set("color", COLOR_FIREWORK);

            orderHeader.add(orderInfo, orderTotal);

            // Інформація про клієнта (тільки для менеджерів)
            if (currentUser.getRole() == AppUser.Role.MANAGER) {
                Div customerInfo = new Div();
                customerInfo.getStyle()
                        .set("margin-top", "10px")
                        .set("padding", "10px")
                        .set("background-color", "rgba(27, 75, 90, 0.1)")
                        .set("border-radius", "4px");

                Span customerName = new Span("Клієнт: " + order.getUser().getName());
                Span customerLogin = new Span("Логін: " + order.getUser().getLogin());

                customerInfo.add(customerName, new Div(customerLogin));
                orderCard.add(orderHeader, customerInfo);
            } else {
                orderCard.add(orderHeader);
            }

            // Кнопка деталей
            Button detailsButton = new Button("Показати деталі");
            detailsButton.getStyle()
                    .set("background-color", COLOR_OCEANIC)
                    .set("color", "white")
                    .set("margin-top", "10px");

            Div orderDetails = new Div();
            orderDetails.setVisible(false);
            orderDetails.getStyle()
                    .set("margin-top", "15px")
                    .set("padding", "10px")
                    .set("background-color", "#f5f5f5")
                    .set("border-radius", "4px");

            // Позиції замовлення
            Grid<OrderItem> itemsGrid = new Grid<>();
            itemsGrid.addColumn(item -> item.getProduct().getName()).setHeader("Товар").setAutoWidth(true);
            itemsGrid.addColumn(OrderItem::getQuantity).setHeader("Кількість").setAutoWidth(true);
            itemsGrid.addColumn(item -> currencyFormat.format(item.getPrice())).setHeader("Ціна").setAutoWidth(true);
            itemsGrid.addColumn(item -> currencyFormat.format(item.getPrice() * item.getQuantity())).setHeader("Сума").setAutoWidth(true);

            itemsGrid.setItems(order.getItems());
            itemsGrid.setHeight("200px");

            orderDetails.add(itemsGrid);

            detailsButton.addClickListener(e -> {
                boolean isVisible = orderDetails.isVisible();
                orderDetails.setVisible(!isVisible);
                detailsButton.setText(isVisible ? "Показати деталі" : "Приховати деталі");
            });

            orderCard.add(detailsButton, orderDetails);
            ordersContainer.add(orderCard);
        }

        orderHistoryContent.add(title, ordersContainer);
    }

    /**
     * Заповнює сторінку управління товарами (тільки для менеджерів)
     */
    private void populateProductManagement() {
        productManagementContent.removeAll();

        H2 title = new H2("Управління товарами");
        title.getStyle().set("color", COLOR_FIREWORK);
        title.getStyle().set("text-align", "center");

        // Кнопка додавання нового товару
        Button addProductButton = new Button("Додати новий товар");
        addProductButton.getStyle()
                .set("background-color", COLOR_FIREWORK)
                .set("color", "white")
                .set("padding", "10px 20px")
                .set("margin-bottom", "20px");

        addProductButton.addClickListener(e -> openProductForm(null));

        // Таблиця товарів
        Grid<Product> grid = new Grid<>(Product.class, false);
        grid.addColumn(Product::getName).setHeader("Назва").setAutoWidth(true);
        grid.addColumn(Product::getCategory).setHeader("Категорія").setAutoWidth(true);
        grid.addColumn(p -> currencyFormat.format(p.getPrice())).setHeader("Ціна").setAutoWidth(true);
        grid.addColumn(Product::getQuantity).setHeader("Кількість").setAutoWidth(true);

        // Колонка з зображенням
        grid.addComponentColumn(product -> {
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Image image = new Image(product.getImageUrl(), product.getName());
                image.setHeight("50px");
                image.getStyle().set("object-fit", "cover");
                return image;
            } else {
                Div placeholder = new Div();
                placeholder.setText("Немає фото");
                placeholder.getStyle()
                        .set("background-color", COLOR_OCEANIC)
                        .set("color", "white")
                        .set("display", "flex")
                        .set("align-items", "center")
                        .set("justify-content", "center")
                        .set("height", "50px")
                        .set("width", "50px");
                return placeholder;
            }
        }).setHeader("Фото").setWidth("100px").setFlexGrow(0);

        // Колонка дій
        grid.addComponentColumn(product -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            Button editButton = new Button("Редагувати");
            editButton.getStyle()
                    .set("background-color", COLOR_OCEANIC)
                    .set("color", "white");

            Button deleteButton = new Button("Видалити");
            deleteButton.getStyle()
                    .set("background-color", COLOR_FIREWORK)
                    .set("color", "white");

            editButton.addClickListener(event -> openProductForm(product));

            deleteButton.addClickListener(event -> {
                Dialog confirmDialog = new Dialog();
                confirmDialog.setWidth("400px");

                VerticalLayout confirmContent = new VerticalLayout();
                confirmContent.setSpacing(true);
                confirmContent.setPadding(true);

                H3 confirmTitle = new H3("Підтвердження видалення");
                Paragraph confirmText = new Paragraph("Ви дійсно хочете видалити товар \"" + product.getName() + "\"?");

                HorizontalLayout confirmButtons = new HorizontalLayout();

                Button yesButton = new Button("Так, видалити");
                yesButton.getStyle()
                        .set("background-color", COLOR_FIREWORK)
                        .set("color", "white");

                Button noButton = new Button("Скасувати");

                yesButton.addClickListener(e -> {
                    try {
                        productService.deleteProduct(product.getId());
                        populateProductManagement();
                        Notification.show("Товар видалено", 2000, Notification.Position.MIDDLE);
                        confirmDialog.close();
                    } catch (Exception ex) {
                        Notification.show("Помилка видалення: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
                    }
                });

                noButton.addClickListener(e -> confirmDialog.close());

                confirmButtons.add(yesButton, noButton);
                confirmContent.add(confirmTitle, confirmText, confirmButtons);
                confirmDialog.add(confirmContent);

                confirmDialog.open();
            });

            actions.add(editButton, deleteButton);
            return actions;
        }).setHeader("Дії").setAutoWidth(true);

        List<Product> products = productService.getAllProducts();
        grid.setItems(products);

        productManagementContent.add(title, addProductButton, grid);
    }

    /**
     * Відкриває форму для створення/редагування товару
     */
    private void openProductForm(Product product) {
        boolean isEdit = product != null;

        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        VerticalLayout form = new VerticalLayout();
        form.setPadding(true);
        form.setSpacing(true);

        H3 title = new H3(isEdit ? "Редагувати товар" : "Додати новий товар");
        title.getStyle().set("color", COLOR_OCEANIC);

        // Поля форми
        TextField nameField = new TextField("Назва товару");
        nameField.setWidth("100%");
        nameField.setRequired(true);

        ComboBox<String> categoryField = new ComboBox<>("Категорія");
        categoryField.setWidth("100%");
        Set<String> categories = productService.getAllCategories();
        List<String> sortedCategories = new ArrayList<>(categories);
        Collections.sort(sortedCategories);
        categoryField.setItems(sortedCategories);
        categoryField.setAllowCustomValue(true);
        categoryField.setRequired(true);

        TextArea descriptionField = new TextArea("Опис");
        descriptionField.setWidth("100%");
        descriptionField.setHeight("150px");

        NumberField priceField = new NumberField("Ціна");
        priceField.setMin(0);
        priceField.setWidth("100%");
        priceField.setRequiredIndicatorVisible(true);

        NumberField quantityField = new NumberField("Кількість");
        quantityField.setMin(0);
        quantityField.setStep(1);
        quantityField.setWidth("100%");
        quantityField.setRequiredIndicatorVisible(true);

        TextField imageUrlField = new TextField("URL зображення");
        imageUrlField.setWidth("100%");
        imageUrlField.setPlaceholder("https://example.com/image.jpg");

        if (isEdit) {
            nameField.setValue(product.getName());
            categoryField.setValue(product.getCategory());
            descriptionField.setValue(product.getDescription() != null ? product.getDescription() : "");
            priceField.setValue(product.getPrice());
            quantityField.setValue(product.getQuantity().doubleValue());
            imageUrlField.setValue(product.getImageUrl() != null ? product.getImageUrl() : "");
        }

        // Попередній перегляд зображення
        Div imagePreview = new Div();
        imagePreview.setWidth("100%");
        imagePreview.setHeight("200px");
        imagePreview.getStyle()
                .set("border", "1px solid #ccc")
                .set("border-radius", "4px")
                .set("margin-top", "10px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("overflow", "hidden");

        Runnable updateImagePreview = () -> {
            imagePreview.removeAll();
            String url = imageUrlField.getValue();

            if (url != null && !url.isEmpty()) {
                Image image = new Image(url, "Попередній перегляд");
                image.setMaxHeight("100%");
                image.setMaxWidth("100%");
                image.getStyle().set("object-fit", "contain");

                // У разі помилки завантаження зображення
                image.getElement().addEventListener("error", event -> {
                    imagePreview.removeAll();
                    Span errorText = new Span("Помилка завантаження зображення");
                    errorText.getStyle().set("color", "red");
                    imagePreview.add(errorText);
                });

                imagePreview.add(image);
            } else {
                Span placeholderText = new Span("Попередній перегляд зображення");
                placeholderText.getStyle().set("color", "#999");
                imagePreview.add(placeholderText);
            }
        };

        imageUrlField.addValueChangeListener(e -> updateImagePreview.run());
        updateImagePreview.run();

        Button saveButton = new Button(isEdit ? "Зберегти зміни" : "Створити товар");
        saveButton.getStyle()
                .set("background-color", COLOR_FIREWORK)
                .set("color", "white")
                .set("width", "100%");

        saveButton.addClickListener(e -> {
            String name = nameField.getValue();
            String category = categoryField.getValue();
            String description = descriptionField.getValue();
            Double price = priceField.getValue();
            Integer quantity = quantityField.getValue() != null ? quantityField.getValue().intValue() : null;
            String imageUrl = imageUrlField.getValue();

            if (name.isEmpty() || category == null || category.isEmpty() || price == null || quantity == null) {
                Notification.show("Будь ласка, заповніть всі обов'язкові поля", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {
                Product productToSave = isEdit ? product : new Product();
                productToSave.setName(name);
                productToSave.setCategory(category);
                productToSave.setDescription(description);
                productToSave.setPrice(price);
                productToSave.setQuantity(quantity);
                productToSave.setImageUrl(imageUrl);

                if (isEdit) {
                    productService.updateProduct(productToSave);
                } else {
                    productService.createProduct(productToSave);
                }

                dialog.close();
                populateProductManagement();
                Notification.show(isEdit ? "Товар оновлено" : "Товар створено", 2000, Notification.Position.MIDDLE);
            } catch (Exception ex) {
                Notification.show("Помилка: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });

        Button cancelButton = new Button("Скасувати");
        cancelButton.addClickListener(event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        buttons.setWidth("100%");

        form.add(
                title,
                nameField,
                categoryField,
                descriptionField,
                priceField,
                quantityField,
                imageUrlField,
                new H3("Попередній перегляд зображення"),
                imagePreview,
                buttons
        );

        dialog.add(form);
        dialog.open();
    }
}
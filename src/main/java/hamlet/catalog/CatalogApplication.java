package hamlet.catalog;

import hamlet.catalog.entity.Category;
import hamlet.catalog.entity.Characteristic;
import hamlet.catalog.entity.Product;
import hamlet.catalog.entity.Value;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Scanner;


public class CatalogApplication {
    private static final EntityManagerFactory FACTORY =
            Persistence.createEntityManagerFactory("main");

    private static final Scanner IN = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.print("""
                Создание [1]
                Редактирование [2]
                Удаление [3]
                Выберите действие: \
                """);

        String actionNum = IN.nextLine();
        switch (actionNum) {
            case "1" -> create();
            case "2" -> update();
            case "3" -> delete();
            default -> System.out.println("Такого действия не сущесвует!");
        }
    }

    private static void create() {
        EntityManager manager = FACTORY.createEntityManager();
        try {
            manager.getTransaction().begin();
            TypedQuery<Category> query = manager.createQuery(
                    "select c from Category c order by c.name", Category.class
            );

            Product newProduct = new Product();
            List<Category> categories = query.getResultList();

            for (Category category : categories) {
                System.out.println(category.getName() + " [" + category.getId() + "]");
            }

            System.out.print("Выберите id категории, куда хотите добавить товар: ");
            String choiceId = IN.nextLine();
            while (true) {
                if (!choiceId.matches("\\d+")) {
                    System.out.print("Неверный формат! Введите еще раз: ");
                    choiceId = IN.nextLine();
                    continue;
                }
                TypedQuery<Long> categoryNumber = manager.createQuery(
                        "select count(c.id) from Category c where c.id = ?1", Long.class
                );
                categoryNumber.setParameter(1, Long.parseLong(choiceId));
                Long number = categoryNumber.getSingleResult();
                if (number > 0) {
                    break;
                }
                System.out.print("Такой категории не существует, введите еще раз: ");
                choiceId = IN.nextLine();
            }
            System.out.print("Введите название товара: ");
            String productName = IN.nextLine();
            System.out.print("Введите описание товара: ");
            String productDesc = IN.nextLine();
            System.out.print("Введите цену товара: ");
            String productPrice = IN.nextLine();
            while (!productPrice.matches("\\d+")) {
                System.out.println("Неверный формат!");
                System.out.print("Введите цену товара: ");
                productPrice = IN.nextLine();
            }

            Category category = manager.find(Category.class, Long.parseLong(choiceId));
            newProduct.setCategory(category);
            newProduct.setName(productName);
            newProduct.setDescription(productDesc);
            newProduct.setPrice(Integer.parseInt(productPrice));
            manager.persist(newProduct);

            List<Characteristic> characteristics = category.getCharacteristics();
            for (Characteristic characteristic : characteristics) {
                Value newValue = new Value();
                System.out.print(characteristic.getTitle() + ": ");
                String valueIn = IN.nextLine();
                newValue.setProduct(newProduct);
                newValue.setCharacteristic(characteristic);
                newValue.setValue(valueIn);
                manager.persist(newValue);
            }
            manager.getTransaction().commit();
        } catch (Exception e) {
            manager.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            manager.close();
        }
    }

    private static void update() {
        EntityManager manager = FACTORY.createEntityManager();
        try {
            manager.getTransaction().begin();
            System.out.print("Введите id товара, который хотите обновить: ");
            String productId = IN.nextLine();
            while (!productId.matches("\\d+")) {
                System.out.print("Неверный формат! Введите еще раз: ");
                productId = IN.nextLine();
            }
            Product product = manager.find(Product.class, Long.parseLong(productId));
            System.out.print("Введите имя товара: ");
            String newName = IN.nextLine();
            if (!newName.isEmpty()) {
                product.setName(newName);
            }
            System.out.print("Введите описание товара: ");
            String newDesc = IN.nextLine();
            if (!newDesc.isEmpty()) {
                product.setDescription(newDesc);
            }
            System.out.print("Введите цену товара: ");
            String newPrice = IN.nextLine();
            if (!newPrice.isEmpty()) {
                while (!newPrice.matches("\\d+")) {
                    System.out.print("Неверный формат! Введите еще раз: ");
                    newPrice = IN.nextLine();
                }
                product.setPrice(Integer.parseInt(newPrice));
            }

            List<Characteristic> characteristics = product.getCategory().getCharacteristics();
            for (Characteristic characteristic : characteristics) {
                System.out.print(characteristic.getTitle() + ": ");
                String valueIn = IN.nextLine();
                TypedQuery<Value> query = manager.createQuery(
                        "select v from Value v where v.product = ?1 and v.characteristic = ?2", Value.class
                );
                query.setParameter(1, product);
                query.setParameter(2, characteristic);
                query.setMaxResults(1);
                List<Value> valueList = query.getResultList();
                if (valueList.isEmpty()) {
                    Value value = new Value();
                    value.setValue(valueIn);
                    value.setProduct(product);
                    value.setCharacteristic(characteristic);
                    manager.persist(value);
                } else {
                    Value value = valueList.get(0);
                    if (!valueIn.isEmpty()) {
                        value.setValue(valueIn);
                    }
                }
            }
            manager.getTransaction().commit();
        } catch (Exception e) {
            manager.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            manager.close();
        }
    }

    private static void delete() {
        EntityManager manager = FACTORY.createEntityManager();
        try {
            manager.getTransaction().begin();
            System.out.print("Введите id товара, который хотите удалить: ");
            String productId = IN.nextLine();
            while (!productId.matches("\\d+")) {
                System.out.print("Неверный формат! Введите еще раз: ");
                productId = IN.nextLine();
            }
            Product product = manager.find(Product.class, Long.parseLong(productId));
            manager.remove(product);
            manager.getTransaction().commit();
        } catch (Exception e) {
            manager.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            manager.close();
        }
    }
}

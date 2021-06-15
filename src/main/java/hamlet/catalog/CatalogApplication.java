package hamlet.catalog;

import hamlet.catalog.entity.Category;
import hamlet.catalog.entity.Characteristic;
import hamlet.catalog.entity.Product;
import hamlet.catalog.entity.Value;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class CatalogApplication {
    //27.05.21
    //Lesson66
    private static final EntityManagerFactory FACTORY =
            Persistence.createEntityManagerFactory("main");

    private static final Scanner IN = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("""
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
            default -> System.out.println("Такого дейтввия не сущестует");
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

            System.out.println("Выберите id категории, куда хотите добавить товар: ");
            String choiceId = IN.nextLine();
            while (true) {
                if (!choiceId.matches("\\d+")) {
                    System.out.println("Неверный формат!");
                } else {
                    break;
                }
                System.out.println("Выберите id категории: ");
                choiceId = IN.nextLine();
            }
            System.out.println("Введите назвние товара: ");
            String productName = IN.nextLine();
            System.out.println("Введите описание товара: ");
            String productDesc = IN.nextLine();
            System.out.println("Введите цену товара: ");
            String productPrice = IN.nextLine();
            while (true) {
                if (!productPrice.matches("\\d+")) {
                    System.out.println("Неверный формат!");
                } else {
                    break;
                }
                System.out.println("Введите цену товара: ");
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
                System.out.println(characteristic.getTitle());
                System.out.println("Введите параметр: ");
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
            System.out.println("Введите id товара, который хотите обновить: ");
            String productId = IN.nextLine();
            Product product = manager.find(Product.class, Long.parseLong(productId));
            System.out.println("Введите имя товара: ");
            String newName = IN.nextLine();
            product.setName(newName);
            System.out.println("Введите описание товара: ");
            String newDesc = IN.nextLine();
            product.setDescription(newDesc);
            System.out.println("Введите цену товара: ");
            String newPrice = IN.nextLine();
            product.setPrice(Integer.parseInt(newPrice));

            List<Characteristic> characteristics = product.getCategory().getCharacteristics();
            for (Characteristic characteristic : characteristics) {
                System.out.println(characteristic.getTitle() + ": ");
                System.out.println("Обновите характеристику: ");
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
                    value.setValue(valueIn);
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
            //CODE
            manager.getTransaction().commit();
        } catch (Exception e) {
            manager.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            manager.close();
        }

    }
}

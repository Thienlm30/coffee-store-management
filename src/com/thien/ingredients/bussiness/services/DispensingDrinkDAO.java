package com.thien.ingredients.bussiness.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.thien.ingredients.bussiness.components.DataValidation;
import com.thien.ingredients.bussiness.components.IdGenerator;
import com.thien.ingredients.bussiness.model.Order;
import com.thien.ingredients.bussiness.model.OrderStatus;
import com.thien.ingredients.data.repository.OrderDAL;
import com.thien.ingredients.gui.utilities.DataInputter;


public class DispensingDrinkDAO implements Dispensable {

    public Map<String, Order> orderMap;
    private OrderDAL orderDAL;
    private String orderPathFile;
    private ManageIngredientDAO manageIngredientDAO;
    private ManageBeverageRecipeDAO manageBeverageRecipeDAO;

    public DispensingDrinkDAO(ManageIngredientDAO manageIngredientDAO, ManageBeverageRecipeDAO manageBeverageRecipeDAO, String orderPathFile) {
        this.manageIngredientDAO = manageIngredientDAO;
        this.manageBeverageRecipeDAO = manageBeverageRecipeDAO;
        

        OrderDAL orderDAL = new OrderDAL();
        List<Order> list = new ArrayList<>();
        if (orderDAL.loadFromFile(list, orderPathFile)) {
            this.orderMap = new TreeMap<>();
            for (Order o : list) {
                orderMap.put(o.getId(), o);
            }
            System.out.println("load thanh cong tu order constructor");
        } else {
            System.err.println("Error loading order from constructor");
        }
        
        this.orderDAL = new OrderDAL();
        this.orderPathFile = orderPathFile;
    }
    
    /** 
     * This method dispensing drinks that have been ordered
     * The order's status is preparing
     * @param prefixId is prefix of the order's id
     */
    @Override
    public void dispensingDrink(String prefixId) {
        IdGenerator idGenerator = new IdGenerator(orderMap, prefixId);
        String id = idGenerator.generateId();
        
        Map<String, Integer> drinkMap = dirnkCollection("D");
        
        orderMap.put(id, new Order(id, drinkMap));
        orderMap.get(id).setOrderStatus(OrderStatus.PREPARING);
    }
    
    /**
     * This method return the list of order drink and it's quantity 
     * @param prefixId is the prefix of the drink's id
     * @return a list contain drinks order
     */
    private Map<String, Integer> dirnkCollection(String prefixId) {
        Map <String, Integer> drinkMap = new HashMap<>();
        DataValidation dataValidation = new DataValidation();
        do {
            String beverageId = dataValidation.inputId(prefixId);
            int quantity;

            if (manageBeverageRecipeDAO.beverageRecipeMap.get(beverageId) == null ) 
                System.out.println("There no drink found");
            else {
                quantity = DataInputter.getInteger("Enter quantity: ", "Quantity must a number and cannot be less than one", 1);
                // check ingredient quantity
                if (!isEnoughIngredient(beverageId, quantity))
                    System.out.println("Ingredient not enough");
                else 
                    drinkMap.put(beverageId, quantity);
            }

        } while (DataInputter.getYN("Do you want to countinue order?"));
        return drinkMap;
    }
    
    private boolean isEnoughIngredient(String beverageId, int quantity) {
        Map<String, Integer> map = manageBeverageRecipeDAO.beverageRecipeMap.get(beverageId).getBeverageRecipeIngredients();
        for (String s : map.keySet()) {
            if (manageIngredientDAO.ingredientMap.get(s).getQuantity() < map.get(s) * quantity)
                return false;
        }
        return true;
    }

    @Override
    public void updateDispensingDrink(String orderId) {
        if (orderMap.get(orderId).getOrderStatus() == OrderStatus.PREPARING) {
            if (!DataInputter.getYN("Do you want to cancel this order?")) {
                manageBeverageRecipeDAO.showAll();
                System.out.println("==Your old order==");
                display(orderId);
                Map<String, Integer> drinkMap = dirnkCollection("D");
                orderMap.get(orderId).setOrderBeverageRecipe(drinkMap);
                System.out.println("");
                display(orderId);
            } else {
                orderMap.remove(orderId);
            }
        } else {
            System.out.println("Order have done and cannot edit");
        }        
    } 
    
    /**
     * This method show one order
     * @param id of order
     */
    private void display(String id) {
        System.out.println(" ------------------------------------------------------------------ ");
        System.out.println("|    ID    |                     Details                           |");
        System.out.println(" ------------------------------------------------------------------ ");
        System.out.println(orderMap.get(id).toString());
        System.out.println(" ------------------------------------------------------------------ ");
    }
    
    /**
     * This method show all order
     */
    public void showAll() {
        System.out.println(" ------------------------------------------------------------------------- ");
        System.out.println("|    ID    |                         Details                              |");
        System.out.println(" ------------------------------------------------------------------------- ");
        
        for (Order o : orderMap.values()) {
            System.out.println(o.toString());
        }
        
        System.out.println(" ------------------------------------------------------------------------- ");
    }
    
    /**
     * This method sends orders to the database
     * And set the status of the ingredient and beverage recipe
     * Ingredients and beverages that have been used will not be deleted
     */
    public void saveToFile() {
        try {
            // set status của beverage với ingredient
            orderDAL.saveToFile(converMapToList(), orderPathFile);
        } catch (Exception e) {
            System.out.println("Save order fail");
        }
    }
    
    private List<Order> converMapToList() {
        List<Order> list = new ArrayList<>();
        for (Order o : orderMap.values()) {
            list.add(o);
        }
        return list;
    }
}

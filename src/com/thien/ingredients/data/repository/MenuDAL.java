package com.thien.ingredients.data.repository;

import java.util.List;

import com.thien.ingredients.bussiness.model.MenuItem;
import com.thien.ingredients.data.FileManagement;

public class MenuDAL {

    private FileManagement fileManagement;

    public MenuDAL() {
        this.fileManagement = new FileManagement();
    }

    public boolean loadFromFile(List<MenuItem> menuItemList, String menuPathFile) {
        return fileManagement.loadListFromFile(menuItemList, menuPathFile);
    }
    
    public boolean saveToFile(List<MenuItem> menuItemList, String menuPathFile) {
        return fileManagement.saveListToFile(menuItemList, menuPathFile, "Save MenuItem to file successfully!");
    }
}

package main;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.naming.NamingException;
import main.dao.DAOMenu;
import main.dominio.Menu;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

/**
 *
 * @author JULIOS
 */
@Named(value = "mbMenu")
@SessionScoped
public class MbMenu implements Serializable {
    private MenuModel model;
    private ArrayList<Menu> menuItems;
    private DAOMenu dao;
    
    public MbMenu() {
        this.init();
    }
    
//    @PostConstruct
//    public void init() {
    private void init() {
        this.model = new DefaultMenuModel();
        this.obtenerMenu();
        
        DefaultSubMenu menu; int idMenu;
        DefaultSubMenu submenu; int idSubmenu;
        DefaultMenuItem item;
        int i=0; int n=menuItems.size();
        while(i < n) {
            idMenu=menuItems.get(i).getIdMenu();
            
            menu = new DefaultSubMenu();
            menu.setLabel(menuItems.get(i).getMenu());
            while(i < n && menuItems.get(i).getIdMenu()==idMenu) {
                idSubmenu=menuItems.get(i).getIdSubMenu();
                
                submenu = new DefaultSubMenu();
                if(idMenu==0) {
                    submenu.setLabel("");
                } else {
                    submenu.setLabel(menuItems.get(i).getSubMenu());
                }
                while(i < n && menuItems.get(i).getIdMenu()==idMenu && menuItems.get(i).getIdSubMenu()==idSubmenu) {
                    item = new DefaultMenuItem();  
                    item.setValue(menuItems.get(i).getModulo());
                    item.setAjax(false);
                    item.setOutcome(menuItems.get(i).getUrl());
                    if(menuItems.get(i).getIdSubMenu()==0) {
                        menu.getElements().add(item);
                    } else {
                        submenu.getElements().add(item);
                    }
                    i++;
                }
                if(idSubmenu!=0) {
                    menu.getElements().add(submenu);
                }
            }
            model.addElement(menu);
        }
    }
    
    private void obtenerMenu() {
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao=new DAOMenu();
            menuItems=this.dao.obtenermenu();
            this.dao.cargarUsuarioConfig();
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public MenuModel getModel() {
        return model;
    }

    public void setModel(MenuModel model) {
        this.model = model;
    }

    public ArrayList<Menu> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(ArrayList<Menu> menuItems) {
        this.menuItems = menuItems;
    }
}

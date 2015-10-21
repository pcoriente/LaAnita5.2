/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilerias;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

/**
 *
 * @author carlos.pat
 */
@Named(value = "habilita")
@SessionScoped
public class Habilita implements Serializable {

    private boolean disabled;
    private boolean disabledProd;

    public Habilita() {
        this.disabled = false;
        this.disabledProd = false;
    }

    public boolean isDisabledProd() {
        return disabledProd;
    }

    public void setDisabledProd(boolean disabledProd) {
        this.disabledProd = disabledProd;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

}

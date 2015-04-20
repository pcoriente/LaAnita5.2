package proveedores;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

/**
 *
 * @author jsolis
 */
@Named(value = "listas")
@SessionScoped
public class Listas implements Serializable {

    public Listas() {
    }
}

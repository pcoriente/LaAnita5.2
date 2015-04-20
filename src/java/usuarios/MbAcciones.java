package usuarios;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.event.ActionEvent;
import javax.naming.NamingException;
import usuarios.dao.DAOAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author JULIOS
 */
@Named(value = "mbAcciones")
@SessionScoped
public class MbAcciones implements Serializable {
    private int idModulo;
    private ArrayList<Accion> acciones;
    private DAOAcciones dao;
    
    public MbAcciones() {
    }
    
    public boolean validarAccion(String idComando) {
        boolean ok=false;
        for(Accion a: this.acciones) {
            if(a.getIdBoton().equals(idComando)) {
                ok=true;
                break;
            }
        }
        return ok;
    }
    
    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        try {
            this.idModulo=idModulo;
            this.dao=new DAOAcciones();
            this.acciones=this.dao.obtenerAcciones(idModulo);
        } catch (NamingException ex) {
            Logger.getLogger(MbAcciones.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(MbAcciones.class.getName()).log(Level.SEVERE, null, ex);
        }
        return this.acciones;
    }
    
    private ArrayList<Accion> obtenerAcciones() {
        try {
            this.dao=new DAOAcciones();
            this.acciones=this.dao.obtenerAcciones(this.idModulo);
        } catch (NamingException ex) {
            Logger.getLogger(MbAcciones.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(MbAcciones.class.getName()).log(Level.SEVERE, null, ex);
        }
        return this.acciones;
    }

//    public int getIdModulo() {
//        return idModulo;
//    }
//
//    public void setIdModulo(int idModulo) {
//        this.idModulo = idModulo;
//    }
//
    public ArrayList<Accion> getAcciones() {
        if(this.acciones==null) {
            this.obtenerAcciones();
        }
        return acciones;
    }

    public void setAcciones(ArrayList<Accion> acciones) {
        this.acciones = acciones;
    }
}

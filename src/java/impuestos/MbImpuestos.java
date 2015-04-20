package impuestos;

import impuestos.dao.DAOImpuestos;
import impuestos.dominio.Impuesto;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.naming.NamingException;

/**
 *
 * @author JULIOS
 */
@Named(value = "mbImpuestos")
@SessionScoped
public class MbImpuestos implements Serializable {

    private Impuesto impuesto;
    private ArrayList<Impuesto> impuestos;
    private String strModo;
    //private int modoEdicion;
    private DAOImpuestos dao;

    public MbImpuestos() {
        this.impuesto = new Impuesto(0, "", false, 1, false, false);
        this.impuestos = new ArrayList<Impuesto>();
        this.impuestos.add(this.impuesto);
        this.strModo = "1";
    }

    public void eliminar() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if (this.impuesto == null) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail("Seleccione un impuesto !!");
        } else {
            try {
                this.dao = new DAOImpuestos();
                this.dao.eliminar(this.impuesto.getIdImpuesto());
                this.impuestos = this.dao.obtenerImpuestos();
                this.impuesto = new Impuesto(0, "", false, 1, false, false);
                this.strModo = "1";
                ok = true;
            } catch (SQLException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getMessage());
            }
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    public void grabar() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if (this.impuesto == null) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail("Seleccione un impuesto !!");
        } else if (this.impuesto.getImpuesto().isEmpty()) {
            fMsg.setDetail("Se requiere la descripción del impuesto !!");
        } else if (!(strModo.equals("1") || strModo.equals("2"))) {
            fMsg.setDetail("Seleccione el modo de aplicación del impuesto !!");
        } else {
            try {
                this.impuesto.setModo(Integer.parseInt(strModo));

                this.dao = new DAOImpuestos();
                if (this.impuesto.getIdImpuesto() == 0) {
                    this.impuesto.setIdImpuesto(this.dao.agregar(this.impuesto));
                } else {
                    this.dao.modificar(this.impuesto);
                }
                this.impuestos = this.dao.obtenerImpuestos();
                ok = true;
            } catch (SQLException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getMessage());
            }
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    public void nuevo() {
        this.impuesto = new Impuesto(0, "", false, 1, false, false);
        this.strModo = "1";
    }

    public void actualizarBean() {
        this.impuesto.setModo(Integer.parseInt(this.strModo));
    }

    public void actualizarString() {
        Integer mode = this.impuesto.getModo();
        this.strModo = mode.toString();
    }
    
    public void cargarImpuestos() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "");
        try {
            this.dao = new DAOImpuestos();
            this.impuestos = this.dao.obtenerImpuestos();
            ok = true;
            } catch (SQLException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }
    
    public void mantenimientoImpuestos() {
        this.cargarImpuestos();
    }
    
    public Impuesto getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(Impuesto impuesto) {
        this.impuesto = impuesto;
    }

    public ArrayList<Impuesto> getImpuestos() {
        //if(this.impuestos==null) {
        //    this.cargarImpuestos();
        //}
        return impuestos;
    }

    public void setImpuestos(ArrayList<Impuesto> impuestos) {
        this.impuestos = impuestos;
    }
    /*
    public int getModoEdicion() {
        return modoEdicion;
    }

    public void setModoEdicion(int modoEdicion) {
        this.modoEdicion = modoEdicion;
    }
    * */

    public String getStrModo() {
        return strModo;
    }

    public void setStrModo(String strModo) {
        this.strModo = strModo;
    }
}

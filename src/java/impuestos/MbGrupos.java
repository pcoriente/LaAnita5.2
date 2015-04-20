package impuestos;

import impuestos.dao.DAOGrupos;
import impuestos.dominio.Impuesto;
import impuestos.dominio.ImpuestoGrupo;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;

/**
 *
 * @author JULIOS
 */
@Named(value = "mbGrupos")
@SessionScoped
public class MbGrupos implements Serializable {

    private ArrayList<SelectItem> listaGrupos;
    private ImpuestoGrupo grupo;
    private String strGrupo;
    private ArrayList<Impuesto> impuestosAgregados;
    private Impuesto impuestoAgregado;
    private ArrayList<Impuesto> impuestosDisponibles;
    private Impuesto impuestoDisponible;
    private ArrayList<Impuesto> impuestos;
    private Impuesto impuesto;
    private String strModo;
    private DAOGrupos dao;

    public MbGrupos() {
        this.grupo = new ImpuestoGrupo(0, "");
        this.nuevo();
    }
    
    public void eliminarImpuesto() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if (this.impuesto == null) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail("Seleccione un impuesto !!");
        } else {
            try {
                this.dao = new DAOGrupos();
                this.dao.eliminarImpuesto(this.impuesto.getIdImpuesto());
                this.impuestos = this.dao.obtenerImpuestos();
//                this.impuesto = new Impuesto(0, "", false, 1, false, false);
//                this.strModo = "1";
                this.nuevoImpuesto();
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

    public void grabarImpuesto() {
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

                this.dao = new DAOGrupos();
                if (this.impuesto.getIdImpuesto() == 0) {
                    this.impuesto.setIdImpuesto(this.dao.agregarImpuesto(this.impuesto));
                } else {
                    this.dao.modificarImpuesto(this.impuesto);
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
    
    private void nuevo() {
        this.nuevoImpuesto();
    }

    public void nuevoImpuesto() {
        this.impuesto = new Impuesto(0, "", false, 1, false, false);
        this.strModo = "1";
    }
    
    public void cargarImpuestos() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "");
        try {
            this.dao = new DAOGrupos();
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
    
    public void actualizarBean() {
        this.impuesto.setModo(Integer.parseInt(this.strModo));
    }
    
    public void actualizarString() {
        Integer mode = this.impuesto.getModo();
        this.strModo = mode.toString();
    }

    public void eliminarImpuestoGrupo() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
//        if (this.grupo == null || this.grupo.getIdGrupo() == 0) {
//            fMsg.setDetail("Se requiere un grupo !!");
//        } else 
            if (this.impuestoAgregado == null) {
            fMsg.setDetail("Debe seleccionar el impuesto a eliminar !!");
        } else {
                this.impuestosDisponibles.add(this.impuestoAgregado);
                this.impuestosAgregados.remove(this.impuestoAgregado);
                this.impuestoAgregado=null;
                ok=true;
//            try {
//                this.dao = new DAOGrupos();
//                this.impuestosAgregados = this.dao.eliminarImpuesto(this.grupo.getIdGrupo(), this.impuestoAgregado.getIdImpuesto());
//                this.impuestosDisponibles = this.dao.obtenerImpuestosDisponibles(this.grupo.getIdGrupo());
//                ok = true;
//            } catch (SQLException ex) {
//                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
//            } catch (NamingException ex) {
//                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//                fMsg.setDetail(ex.getMessage());
//            }
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    public void agregarImpuestoGrupo() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
//        if (this.grupo == null || this.grupo.getIdGrupo() == 0) {
//            fMsg.setDetail("Se requiere un grupo !!");
//        } else 
            if (this.impuestoDisponible == null) {
            fMsg.setDetail("No ha seleccionado un impuesto disponible !!");
        } else {
                this.impuestosAgregados.add(this.impuestoDisponible);
                this.impuestosDisponibles.remove(this.impuestoDisponible);
                this.impuestoDisponible=null;
                ok=true;
//            try {
//                this.dao = new DAOGrupos();
//                this.impuestosAgregados = this.dao.agregarImpuesto(this.grupo.getIdGrupo(), this.impuestoDisponible.getIdImpuesto());
//                this.impuestosDisponibles = this.dao.obtenerImpuestosDisponibles(this.grupo.getIdGrupo());
//                ok = true;
//            } catch (SQLException ex) {
//                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
//            } catch (NamingException ex) {
//                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//                fMsg.setDetail(ex.getMessage());
//            }
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    public ArrayList<Impuesto> obtenerImpuestosAgregados() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        ArrayList<Impuesto> imps = new ArrayList<Impuesto>();
        try {
//            if (this.grupo == null) {
//                fMsg.setDetail("Se requiere un grupo !!");
//            } else if (this.grupo.getIdGrupo() == 0) {
//                ok = true;
//            } else {
                this.dao = new DAOGrupos();
                imps = this.dao.obtenerImpuestosAgregados(this.grupo.getIdGrupo());
                ok = true;
//            }
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
        return imps;
    }

    public ArrayList<Impuesto> obtenerImpuestosDisponibles() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        ArrayList<Impuesto> imps = new ArrayList<Impuesto>();
        try {
//            if (this.grupo == null) {
//                fMsg.setDetail("Se requiere un grupo !!");
//            } else if (this.grupo.getIdGrupo() == 0) {
//                ok = true;
//            } else {
                this.dao = new DAOGrupos();
                imps = this.dao.obtenerImpuestosDisponibles(this.grupo.getIdGrupo());
                ok = true;
//            }
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
        return imps;
    }
    /*
    public boolean seleccionar() {
        boolean ok=false;
        return ok;
    }
    * */

    public boolean eliminarGrupo() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if (this.grupo == null || this.grupo.getIdGrupo() == 0) {
            fMsg.setDetail("Se requiere un grupo !!");
        } else {
            try {
                this.dao = new DAOGrupos();
                ok = this.dao.eliminarGrupo(this.grupo.getIdGrupo());
                if (ok) {
                    this.cargarGrupos();
                    this.grupo = new ImpuestoGrupo(0, "");
                } else {
                    fMsg.setDetail("El grupo no puede ser eliminado, actualmente está en uso !!");
                }
            } catch (SQLException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getMessage());
            }
        }
        if(!ok){
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okEliminarGrupo", ok);
        return ok;
    }

    public void grabarGrupo() {
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if (this.grupo.getGrupo().isEmpty()) {
            fMsg.setDetail("Se requiere el nombre un grupo !!");
        } else {
            try {
                this.dao = new DAOGrupos();
                if (this.grupo.getIdGrupo() == 0) {
                    this.grupo.setIdGrupo(this.dao.agregarGrupo(this.grupo, this.impuestosAgregados));
                } else {
                    this.dao.modificarGrupo(this.grupo, this.impuestosAgregados);
                }
                this.cargarGrupos();
                this.setImpuestosAgregados(this.obtenerImpuestosAgregados());
                this.setImpuestosDisponibles(this.obtenerImpuestosDisponibles());
                fMsg.setSeverity(FacesMessage.SEVERITY_INFO);
                fMsg.setDetail("El grupo se grabo correctamente !!!");
            } catch (SQLException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getMessage());
            }
        }
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
    }

    public void cargarGrupos() {
        this.listaGrupos = new ArrayList<SelectItem>();
        ImpuestoGrupo imp = new ImpuestoGrupo(0, "SELECCIONE");
        this.listaGrupos.add(new SelectItem(imp, imp.toString()));

        try {
            this.dao = new DAOGrupos();
            ArrayList<ImpuestoGrupo> lstGrupos = this.dao.obtenerGrupos();
            for (ImpuestoGrupo i : lstGrupos) {
                this.listaGrupos.add(new SelectItem(i, i.toString()));
            }
        } catch (SQLException ex) {
            Logger.getLogger(MbGrupos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NamingException ex) {
            Logger.getLogger(MbGrupos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ImpuestoGrupo getGrupo() {
        return grupo;
    }

    public void setGrupo(ImpuestoGrupo grupo) {
        this.grupo = grupo;
    }

    public Impuesto getImpuestoAgregado() {
        return impuestoAgregado;
    }

    public void setImpuestoAgregado(Impuesto impuestoAgregado) {
        this.impuestoAgregado = impuestoAgregado;
    }

    public ArrayList<Impuesto> getImpuestosAgregados() {
        return impuestosAgregados;
    }

    public void setImpuestosAgregados(ArrayList<Impuesto> impuestosAgregados) {
        this.impuestosAgregados = impuestosAgregados;
    }

    public Impuesto getImpuestoDisponible() {
        return impuestoDisponible;
    }

    public void setImpuestoDisponible(Impuesto impuestoDisponible) {
        this.impuestoDisponible = impuestoDisponible;
    }

    public ArrayList<Impuesto> getImpuestosDisponibles() {
        return impuestosDisponibles;
    }

    public void setImpuestosDisponibles(ArrayList<Impuesto> impuestosDisponibles) {
        this.impuestosDisponibles = impuestosDisponibles;
    }

    public String getStrGrupo() {
        return strGrupo;
    }

    public void setStrGrupo(String strGrupo) {
        this.strGrupo = strGrupo;
    }

    public ArrayList<SelectItem> getListaGrupos() {
        if (this.listaGrupos == null) {
            this.cargarGrupos();
        }
        return listaGrupos;
    }

    public void setListaGrupos(ArrayList<SelectItem> listaGrupos) {
        this.listaGrupos = listaGrupos;
    }

    public ArrayList<Impuesto> getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(ArrayList<Impuesto> impuestos) {
        this.impuestos = impuestos;
    }

    public Impuesto getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(Impuesto impuesto) {
        this.impuesto = impuesto;
    }

    public String getStrModo() {
        return strModo;
    }

    public void setStrModo(String strModo) {
        this.strModo = strModo;
    }
}

package unidadesMedida;

import Message.Mensajes;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author JULIOS
 */
@Named(value = "mbUnidadMedida")
@SessionScoped
public class MbUnidadMedida implements Serializable {

    private boolean modoEdicion;
    private UnidadMedida unidadMedidaSeleccionada;
    private UnidadMedida unidadMedida = new UnidadMedida();
    private ArrayList<UnidadMedida> unidadesMedida;
    private DAOUnidadesMedida dao;
    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;

    public MbUnidadMedida() {
        this.modoEdicion = false;
        this.mbAcciones = new MbAcciones();
    }

    public String grabar() {
        String destino = null;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if (this.unidadMedida.getUnidadMedida().equals("")) {
            fMsg.setDetail("Se requiere la descripcion de la unidad de medida !!");
        } else if (this.unidadMedida.getAbreviatura().equals("")) {
            fMsg.setDetail("Se requiere la abreviatura de la unidad de medida !!");
        } else {
            try {
                this.dao = new DAOUnidadesMedida();
                if (this.unidadMedida.getIdUnidadMedida() == 0) {
                    this.unidadMedida.setIdUnidadMedida(this.dao.agregar(this.unidadMedida));
                    this.unidadMedidaSeleccionada = this.unidadMedida;
                } else {
                    this.dao.modificar(this.unidadMedida);
                }
                this.modoEdicion = false;
                this.unidadMedidaSeleccionada = this.unidadMedida;
                this.unidadesMedida = null;
                destino = "unidadesMedida.xhtml";
            } catch (NamingException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getMessage());
            } catch (SQLException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
        if (destino == null) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        return destino;
    }

    public boolean validar() {
        boolean ok = false;
        if (this.unidadMedida.getUnidadMedida().equals("")) {
            Mensajes.mensajeAlert("Se requiere una unidad de medida");
        } else if (this.unidadMedida.getAbreviatura().equals("")) {
            Mensajes.mensajeAlert("Se requiere una abreviatura para esta unidad de medida");
        } else {
            ok = true;
        }
        return ok;
    }

    public void guardarUnidadMedida() {
        boolean ok = validar();
        if (ok == true) {
            try {
                this.dao = new DAOUnidadesMedida();
                if (this.unidadMedida.getIdUnidadMedida() == 0) {
                    this.unidadMedida.setIdUnidadMedida(this.dao.agregar(this.unidadMedida));
                    Mensajes.mensajeSucces("Exito! Nueva unidad de medida disponible");
                } else {
                    this.dao.modificar(this.unidadMedida);
                    Mensajes.mensajeSucces("Exito! Unidad de medida actualizada");
                    this.unidadMedidaSeleccionada = null;
                }
                this.modoEdicion = false;
//                this.unidadMedidaSeleccionada = this.unidadMedida;
                this.unidadesMedida = null;
            } catch (NamingException ex) {
                Message.Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    public String cancelar() {
        this.modoEdicion = false;
        return "unidadesMedida.xhtml";
    }

    public String terminar() {
        return "index.xhtml";
    }

    public String salir() {
        return "index.xhtml";
    }

    public String nuevaUnidadMedida() {
        this.modoEdicion = true;
        this.unidadMedida = new UnidadMedida(0, "", "");
        return "unidadesMedida.xhtml";
    }

    public String modificarUnidadMedida() {
        this.modoEdicion = true;
        this.copia();
        return "unidadesMedida.xhtml";
    }

    private void copia() {
        this.unidadMedida = new UnidadMedida(0, "", "");
        this.unidadMedida.setIdUnidadMedida(this.unidadMedidaSeleccionada.getIdUnidadMedida());
        this.unidadMedida.setUnidadMedida(this.unidadMedidaSeleccionada.getUnidadMedida());
        this.unidadMedida.setAbreviatura(this.unidadMedidaSeleccionada.getAbreviatura());
    }

    public void cargaUnidadesMedida() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao = new DAOUnidadesMedida();
            this.unidadesMedida = this.dao.obtenerUnidades();
            ok = true;
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    public void limpiarUnidadMedida() {
        unidadMedida = new UnidadMedida();
        unidadMedidaSeleccionada = null;
    }

    public void obtenerUnidadMedida() {
        unidadMedida = unidadMedidaSeleccionada;
    }

    public UnidadMedida getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(UnidadMedida unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public ArrayList<UnidadMedida> getUnidadesMedida() {
        if (this.unidadesMedida == null) {
            this.cargaUnidadesMedida();
        }
        return unidadesMedida;
    }

    public void setUnidadesMedida(ArrayList<UnidadMedida> unidadesMedida) {
        this.unidadesMedida = unidadesMedida;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(8);
        }
        return acciones;
    }

    public void setAcciones(ArrayList<Accion> acciones) {
        this.acciones = acciones;
    }

    public MbAcciones getMbAcciones() {
        return mbAcciones;
    }

    public void setMbAcciones(MbAcciones mbAcciones) {
        this.mbAcciones = mbAcciones;
    }

    public boolean isModoEdicion() {
        return modoEdicion;
    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
    }

    public UnidadMedida getUnidadMedidaSeleccionada() {
        return unidadMedidaSeleccionada;
    }

    public void setUnidadMedidaSeleccionada(UnidadMedida unidadMedidaSeleccionada) {
        this.unidadMedidaSeleccionada = unidadMedidaSeleccionada;
    }
}

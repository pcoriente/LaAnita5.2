package monedas;

import Message.Mensajes;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;

/**
 *
 * @author jesc
 */
@Named(value = "mbMonedas")
@SessionScoped
public class MbMonedas implements Serializable {

    private ArrayList<SelectItem> listaMonedas;
    private ArrayList<Moneda> lstMoneda;
    private ArrayList<Moneda> filterMonedas = null;
    private Moneda SeleccionMoneda = null;
    private Moneda moneda = new Moneda();
//    private Moneda monedas = new Moneda();
    private DAOMonedas dao;

    public MbMonedas() {
    }

    public Moneda obtenerMoneda(int idMoneda) {
        Moneda m = null;
        try {
            this.dao = new DAOMonedas();
            m = this.dao.obtenerMoneda(idMoneda);
            if (m == null) {
                Mensajes.mensajeAlert("No se encontro la moneda solicitada");
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        return m;
    }

    private ArrayList<SelectItem> obtenerListaMonedas() {
        listaMonedas = new ArrayList<>();
        try {
            Moneda m0 = new Moneda();
            m0.setIdMoneda(0);
            m0.setMoneda("Moneda: ");
            listaMonedas.add(new SelectItem(m0, m0.toString()));
            this.dao = new DAOMonedas();
            ArrayList<Moneda> monedas = dao.obtenerMonedas();
            for (Moneda e : monedas) {
                listaMonedas.add(new SelectItem(e, e.toString()));
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        return listaMonedas;
    }

    public boolean validar() {
        boolean ok = false;
        if (moneda.getMoneda().equals("")) {
            Mensajes.mensajeAlert("Se requiere el nombre de la moneda.");
        } else if (moneda.getCodigoIso().equals("")) {
            Mensajes.mensajeAlert("Se requiere una ISO par a la moneda.");
        } else if (moneda.getPrefijoUnidad().equals("")) {
            Mensajes.mensajeAlert("Se requiere una unidad.");
        } else if (moneda.getPrefijo().equals("")) {
            Mensajes.mensajeAlert("Se requiere un prefijo.");
        } else if (moneda.getSufijo().equals("")) {
            Mensajes.mensajeAlert("Se requiere un subfijo.");
        } else if (moneda.getSimbolo().equals("")) {
            Mensajes.mensajeAlert("Se requiere un simbolo.");
        } else {
            ok = true;
        }
        return ok;
    }

    public void limpiarMonedas() {
        moneda = new Moneda();
        SeleccionMoneda = null;
    }

    public void guardarMoneda() {
        boolean ok = validar();
        if (ok == true) {
            try {
                dao = new DAOMonedas();
                if (SeleccionMoneda.getIdMoneda() > 0) {
                    dao.actualizarMonedas(moneda);
                    Mensajes.mensajeSucces("Moneda Actualizada exitosamente");
                } else {
                    dao.guardarMonedas(moneda);
                    Mensajes.mensajeSucces("Nueva moneda disponible");
                }
                lstMoneda = null;
                SeleccionMoneda = null;
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }

        }
    }

    public Moneda obtenerMonedaCero() {
        return (Moneda) this.listaMonedas.get(0).getValue();
    }

    public ArrayList<SelectItem> getListaMonedas() throws NamingException {
        if (this.listaMonedas == null) {
            listaMonedas = this.obtenerListaMonedas();
        }
        return listaMonedas;
    }

    public void setListaMonedas(ArrayList<SelectItem> listaMonedas) {
        this.listaMonedas = listaMonedas;
    }

    public void obtenerInformacion() {
        moneda = SeleccionMoneda;
    }

    public ArrayList<Moneda> getLstMoneda() {
        if (lstMoneda == null) {
            lstMoneda = new ArrayList<>();
            try {
                this.dao = new DAOMonedas();
                lstMoneda = dao.obtenerMonedas();
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
        return lstMoneda;
    }

    public void setLstMoneda(ArrayList<Moneda> lstMoneda) {
        this.lstMoneda = lstMoneda;
    }

    public Moneda getMoneda() {
        return moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }

    public ArrayList<Moneda> getFilterMonedas() {
        return filterMonedas;
    }

    public void setFilterMonedas(ArrayList<Moneda> filterMonedas) {
        this.filterMonedas = filterMonedas;
    }

    public Moneda getSeleccionMoneda() {
        return SeleccionMoneda;
    }

    public void setSeleccionMoneda(Moneda SeleccionMoneda) {
        this.SeleccionMoneda = SeleccionMoneda;
    }
}

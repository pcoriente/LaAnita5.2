/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package formatos;

import Message.Mensajes;
import clientesListas.formatosDetalleDominio.ClienteListasDetalle;
import entradas.dao.DAOMovimientos;
import formatos.dao.DAOFormatos;
import formatos.dominio.ClienteFormato;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.Dependent;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.naming.NamingException;
import monedas.MbMonedas;
import org.primefaces.context.RequestContext;
import producto2.MbProductosBuscar;
import producto2.dominio.Producto;

/**
 *
 * @author Usuario
 */
@Named(value = "mbFormatos")
@Dependent
public class MbFormatos {
    @ManagedProperty(value = "#{mbBuscar}")
    private MbProductosBuscar mbBuscar;
    @ManagedProperty(value = "#{mbMonedas}")
    private MbMonedas mbMonedas;
    
    private ClienteFormato clientesFormatos = new ClienteFormato();
    private ClienteFormato cmbClientesFormatos = new ClienteFormato();
    private ClienteFormato cmbFormato = new ClienteFormato();
    private ClienteFormato formato = new ClienteFormato();
    private ClienteFormato formatoSeleccion;
    private ArrayList<SelectItem> lstFormatos = null;
    private ArrayList<ClienteFormato> listaFormatosFormatos = new ArrayList<ClienteFormato>();
    private ArrayList<ClienteFormato> lstFormato;
    
    private boolean actualizar = false;
    ArrayList<ClienteListasDetalle> lstFormatoDetalle = new ArrayList<ClienteListasDetalle>();
    private DAOMovimientos dao;

    /**
     * Creates a new instance of MbFormatos
     */
    public MbFormatos() {
        this.mbBuscar = new MbProductosBuscar();
        this.mbMonedas = new MbMonedas();
        
        this.formatoSeleccion = new ClienteFormato();
    }
    
    public void nuevoFormato() {
        this.formatoSeleccion=new ClienteFormato();
    }
    
    public void inicializar() {
        this.cargarArrayListListaFormatos(0);
        this.formatoSeleccion=new ClienteFormato();
    }

    public boolean validarFormatos() {
        boolean ok = false;
        if (clientesFormatos.getFormato().equals("")) {
            Mensajes.mensajeAlert("Formato Requerido");
        } else {
            ok = true;
        }
        return ok;
    }
    
    public ClienteFormato obtenerFormato(int idFormato) {
        ClienteFormato f=null;
        try {
            DAOFormatos dao1 = new DAOFormatos();
            f=dao1.obtenerClientesFormato(idFormato);
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
        }
        return f;
    }
    
    public void cargarFormatosCliente(int idCliente) {
        try {
            this.formatoSeleccion=new ClienteFormato();
            this.lstFormatos=new ArrayList<SelectItem>();
            DAOFormatos dao1 = new DAOFormatos();
            ClienteFormato cli = new ClienteFormato();
            cli.setIdFormato(0);
            cli.setFormato("Nuevo Formato");
            lstFormatos.add(new SelectItem(cli, cli.getFormato()));
            for (ClienteFormato clientes : dao1.dameFormatosCliente(idCliente)) {
                lstFormatos.add(new SelectItem(clientes, clientes.getFormato()));
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
            Logger.getLogger(MbFormatos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException e) {
            Mensajes.mensajeError(e.getMessage());
        }
    }

    public void cargarListaFormatos(int idGrupoCte) {
        try {
            lstFormatos = new ArrayList<SelectItem>();
            DAOFormatos dao1 = new DAOFormatos();
            ClienteFormato cli = new ClienteFormato();
            cli.setIdFormato(0);
            cli.setFormato("Nuevo Formato");
            lstFormatos.add(new SelectItem(cli, cli.getFormato()));
            for (ClienteFormato clientes : dao1.dameFormatos(idGrupoCte)) {
                lstFormatos.add(new SelectItem(clientes, clientes.getFormato()));
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
            Logger.getLogger(MbFormatos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException e) {
            Mensajes.mensajeError(e.getMessage());
        }
    }

    public void cargarArrayListListaFormatos(int idGrupoCliete) {
        try {
            DAOFormatos dao1 = new DAOFormatos();
            listaFormatosFormatos = dao1.dameFormatos(idGrupoCliete);
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
            Logger.getLogger(MbFormatos.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getMessage());
            Logger.getLogger(MbFormatos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<SelectItem> getLstFormatos() {
        if (lstFormatos == null) {
            cargarListaFormatos(0);
        }
        return lstFormatos;
    }

    public void setLstFormatos(ArrayList<SelectItem> lstFormatos) {
        this.lstFormatos = lstFormatos;
    }

    public ClienteFormato getClientesFormatos() {
        return clientesFormatos;
    }

    public void setClientesFormatos(ClienteFormato clientesFormatos) {
        this.clientesFormatos = clientesFormatos;
    }

    public ClienteFormato getCmbClientesFormatos() {
        return cmbClientesFormatos;
    }

    public void setCmbClientesFormatos(ClienteFormato cmbClientesFormatos) {
        this.cmbClientesFormatos = cmbClientesFormatos;
    }

    public boolean validar() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        fMsg.setSeverity(FacesMessage.SEVERITY_WARN);
        if (formato.getFormato().equals("")) {
            fMsg.setDetail("Se requiere un nombre del Formato");
        } else if (mbMonedas.getMoneda().getIdMoneda() == 0) {
            fMsg.setDetail("Seleccione un tipo de Moneda");
        } else {
            ok = true;
        }
        if (ok == false) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("ok", ok);
        return ok;
    }

    public void actualizar() {
        actualizar = true;
    }

    public String salir() {
        return "index.xhtml";
    }

    public void guardarFormato() {
        boolean ok = validar();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "");
        RequestContext context = RequestContext.getCurrentInstance();
        if (ok == true) {
            try {
                DAOFormatos dao1 = new DAOFormatos();
                if (actualizar == false) {
                    dao1.agregar(formato);
                    fMsg.setDetail("Nuevo Formato Disponible");
                } else {
                    dao1.actualizar(formato);
                    fMsg.setDetail("Formato Actualizado");
                }
                this.setLstFormato(null);
                this.limpiar();
            } catch (NamingException ex) {
                ok = false;
                fMsg.setDetail(ex.getMessage());
            } catch (SQLException ex) {
                ok = false;
                fMsg.setDetail(ex.getMessage());
            }
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
            context.addCallbackParam("ok", ok);
        }
    }

    public void limpiar() {
        mbMonedas.getMoneda().setIdMoneda(0);
        formato.setIdFormato(0);
        formato.setFormato("");
        actualizar = false;
        formatoSeleccion = null;
    }

    public void cargarDatos() {
        formato.setIdFormato(formatoSeleccion.getIdFormato());
        formato.setFormato(formatoSeleccion.getFormato());
    }

//    public ArrayList<SelectItem> getLstItems() {
//        if (lstItems == null) {
//            try {
//                DAOClientesLista dao = new DAOClientesLista();
//                ArrayList<ClientesFormatos> lstFormato = new ArrayList<ClientesFormatos>();
//                try {
//                    lstFormato = dao.dameListaFormatos();
//                } catch (SQLException ex) {
//                    Logger.getLogger(MbClientesListas.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                ClientesFormatos format = new ClientesFormatos();
//                format.setFormato("Nuevo Formato");
//                format.setIdFormato(0);
//                lstItems = new ArrayList<SelectItem>();
//                lstItems.add(new SelectItem(format, format.getFormato()));
//                for (ClientesFormatos formato : lstFormato) {
//                    SelectItem select = new SelectItem(formato, formato.getFormato());
//                    lstItems.add(select);
//                }
//            } catch (NamingException ex) {
//                Logger.getLogger(MbClientesListas.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return lstItems;
//    }
    public void construir() {
        for (Producto p : mbBuscar.getSeleccionados()) {
            ClienteListasDetalle fmto = new ClienteListasDetalle();
            fmto.setProducto(p);
            lstFormatoDetalle.add(fmto);
        }
    }

    public void buscar() {
        this.mbBuscar.buscarLista();
        if (this.mbBuscar.getProducto() != null) {
            this.actualizaProductoSeleccionado();
        }
    }

    public void actualizaProductoSeleccionado() {
    }
//
//    public void setLstItems(ArrayList<SelectItem> lstItems) {
//        this.lstItems = lstItems;
//    }

    public MbMonedas getMbMonedas() {
        return mbMonedas;
    }

    public void setMbMonedas(MbMonedas mbMonedas) {
        this.mbMonedas = mbMonedas;
    }

    public ArrayList<ClienteFormato> getLstFormato() {
        return lstFormato;
    }

    public void setLstFormato(ArrayList<ClienteFormato> lstFormato) {
        this.lstFormato = lstFormato;
    }

    public boolean isActualizar() {
        return actualizar;
    }

    public void setActualizar(boolean actualizar) {
        this.actualizar = actualizar;
    }

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }

    public ArrayList<ClienteListasDetalle> getLstFormatoDetalle() {
        return lstFormatoDetalle;
    }

    public void setLstFormatoDetalle(ArrayList<ClienteListasDetalle> lstFormatoDetalle) {
        this.lstFormatoDetalle = lstFormatoDetalle;
    }

    public ClienteFormato getFormato() {
        return formato;
    }

    public void setFormato(ClienteFormato formato) {
        this.formato = formato;
    }

    public ClienteFormato getCmbFormato() {
        return cmbFormato;
    }

    public void setCmbFormato(ClienteFormato cmbFormato) {
        this.cmbFormato = cmbFormato;
    }

    public ClienteFormato getFormatoSeleccion() {
        return formatoSeleccion;
    }

    public void setFormatoSeleccion(ClienteFormato formatoSeleccion) {
        this.formatoSeleccion = formatoSeleccion;
    }

    public DAOMovimientos getDao() {
        return dao;
    }

    public void setDao(DAOMovimientos dao) {
        this.dao = dao;
    }

    public ArrayList<ClienteFormato> getListaFormatosFormatos() {
        return listaFormatosFormatos;
    }

    public void setListaFormatosFormatos(ArrayList<ClienteFormato> listaFormatosFormatos) {
        this.listaFormatosFormatos = listaFormatosFormatos;
    }
    
    
    
}

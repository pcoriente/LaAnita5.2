package clientes;

import Message.Mensajes;
import clientes.dao.DAOClientes;
import clientes.dominio.Cliente;
import clientes.to.TOCliente;
import clientesBancos.MbClientesBancos;
import clientesBancos.dao.DAOClientesBancos;
import contribuyentes.Contribuyente;
import contribuyentes.DAOContribuyentes;
import contribuyentes.MbContribuyentes;
import direccion.MbDireccion;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.naming.NamingException;
import leyenda.dominio.ClienteBanco;
import mbMenuClientesGrupos.MbClientesGrupos;
import org.primefaces.context.RequestContext;

/**
 *
 * @author Usuario
 */
@Named(value = "mbClientes")
@SessionScoped
public class MbClientes implements Serializable {

    @ManagedProperty(value = "#{mbClientesGrupos}")
    private MbClientesGrupos mbClientesGrupos;
    @ManagedProperty(value = "#{mbContribuyente}")
    private MbContribuyentes mbContribuyente;
    @ManagedProperty(value = "#{mbDireccion}")
    private MbDireccion mbDireccion;
    @ManagedProperty(value = "#{mbClientesBancos}")
    private MbClientesBancos mbClientesBancos;
    @ManagedProperty(value = "#{mbTiendasFormatos}")
    private MbTiendasFormatos mbTiendasFormatos;

    private int indexClienteSeleccionado;
    private Cliente cliente, clienteSeleccionado;
    private ArrayList<Cliente> clientes, listaClientes;
    private ArrayList<Cliente>listaFiltros;

//    private int personaFisica = 0;
//    private boolean actualizarRfc = false;
    private ClienteBanco clienteBanco = new ClienteBanco();
//    private boolean direccionCliente = false;
//    private boolean nuevoContribuyente = false;
//    private boolean activarRfc = false;

    /**
     * Creates a new instance of MbClientes
     */
    public MbClientes() {
        this.mbClientesGrupos = new MbClientesGrupos();
        this.mbContribuyente = new MbContribuyentes();
        this.mbDireccion = new MbDireccion();
        this.mbClientesBancos = new MbClientesBancos();
        this.mbTiendasFormatos = new MbTiendasFormatos();

        this.cliente = new Cliente();
    }

    public List<String> completarClientes(String query) {
        List<String> lst = new ArrayList<String>();
        try {
            DAOContribuyentes dao = new DAOContribuyentes();
            for (Contribuyente con : dao.dameRfcContribuyente(query)) {
                lst.add(con.getRfc());
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
            Logger.getLogger(MbClientes.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getMessage());
            Logger.getLogger(MbClientes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lst;
    }

    public void cargarFormatos() {
        this.mbTiendasFormatos.cargarListaCombo(this.cliente.getGrupo().getIdGrupoCte());
    }

    public String salir() {
        this.cliente = null;
        this.clientes = null;
        return "index.xhtml";
    }

//    public void validarDireccion() {
//        if (this.mbDireccion.validarDireccion()) {
//            if (controlActualizar == 1) {
//                try {
//                    DAODireccion daoDireccion = new DAODireccion();
//                    daoDireccion.modificar(mbDireccion.getDireccion().getIdDireccion(), mbDireccion.getDireccion().getCalle(), mbDireccion.getDireccion().getNumeroExterior(), mbDireccion.getDireccion().getNumeroInterior(), mbDireccion.getDireccion().getReferencia(), mbDireccion.getDireccion().getPais().getIdPais(), mbDireccion.getDireccion().getCodigoPostal(), mbDireccion.getDireccion().getEstado(), mbDireccion.getDireccion().getMunicipio(), mbDireccion.getDireccion().getLocalidad(), mbDireccion.getDireccion().getColonia(), mbDireccion.getDireccion().getNumeroLocalizacion());
//                    if (direccionCliente == false) {
//                        cliente.getContribuyente().setDireccion(mbDireccion.getDireccion());
//                    } else {
//                        cliente.setDireccion(mbDireccion.getDireccion());
//                    }
//                    Mensajes.mensajeSucces("Exito!! Dirección actualizada correctamente");
//                } catch (NamingException ex) {
//                    Logger.getLogger(MbClientes.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (SQLException ex) {
//                    Logger.getLogger(MbClientes.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            } else if (direccionCliente == true) {
//                cliente.setDireccion(mbDireccion.getDireccion());
//            } else {
//                cliente.getContribuyente().setDireccion(mbDireccion.getDireccion());
//                mbContribuyente.getContribuyente().setDireccion(mbDireccion.getDireccion());
//            }
//        }
//    }
    public void altaDireccionClientes() {
//        direccionCliente = true;
        mbDireccion.setDireccion(cliente.getDireccion());
    }

    public void seleccionaCliente() {
        this.cliente = this.clienteSeleccionado;
        int idx = this.clientes.indexOf(this.cliente);
        if (idx != -1) {
            this.indexClienteSeleccionado = idx;
        }
        this.mbTiendasFormatos.cargarListaCombo(this.cliente.getGrupo().getIdGrupoCte());
    }

    public void obtenerInformacionRfc() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        try {
            DAOClientes dao = new DAOClientes();
            if (this.cliente.getIdCliente() == 0) {
                this.indexClienteSeleccionado = -1;
                this.listaClientes = new ArrayList<Cliente>();
                this.cliente.setContribuyente(this.mbContribuyente.obtenerContribuyenteRfc(this.cliente.getContribuyente().getRfc()));
                this.mbTiendasFormatos.cargarListaCombo(this.cliente.getGrupo().getIdGrupoCte());
            } else {
                this.listaClientes = new ArrayList<Cliente>();
                for (TOCliente to : dao.obtenerClientesRfc(this.cliente.getContribuyente().getRfc())) {
                    this.listaClientes.add(this.convertir(to));
                }
                if (this.listaClientes.isEmpty()) {
                    this.cliente = new Cliente();
                    this.cliente.setContribuyente(this.mbContribuyente.obtenerContribuyenteRfc(this.cliente.getContribuyente().getRfc()));
                    this.mbTiendasFormatos.cargarListaCombo(this.cliente.getGrupo().getIdGrupoCte());
                    Mensajes.mensajeAlert("No se encontraron clientes con el contribuyente especificado !!!");
                } else if (this.listaClientes.size() == 1) {
                    this.cliente = this.listaClientes.get(0);
                    int idx = this.clientes.indexOf(this.cliente);
                    if (idx != -1) {
                        this.indexClienteSeleccionado = idx;
                    }
                    this.mbTiendasFormatos.cargarListaCombo(this.cliente.getGrupo().getIdGrupoCte());
                } else {
                    ok = true;
                }
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        context.addCallbackParam("okAbrirListaClientes", ok);
    }

//    public void mttoDireccion() {
//        this.mbDireccion.mttoDireccion(this.cliente.getDireccion());
//    }
//
//    public void mttoDireccionContribuyente() {
////        mbDireccion.setDireccion(mbContribuyente.getContribuyente().getDireccion());
//        this.mbDireccion.mttoDireccion(this.mbContribuyente.getContribuyente().getDireccion());
////        this.mbDireccion.setRequerida(true);
//    }
//    public void cargarDatos() {
////        this.setControlActualizar(1);
//        this.setActualizarRfc(true);
//        cliente.setIdCliente(clienteSeleccionado.getIdCliente());
//        cliente.getContribuyente().setRfc(clienteSeleccionado.getContribuyente().getRfc());
//        cliente.getContribuyente().setDireccion(clienteSeleccionado.getContribuyente().getDireccion());
//        cliente.setDireccion(clienteSeleccionado.getDireccion());
//        cliente.setDiasCredito(clienteSeleccionado.getDiasCredito());
//        cliente.setLimiteCredito(clienteSeleccionado.getLimiteCredito());
//        cliente.setDescuentoComercial(clienteSeleccionado.getDescuentoComercial());
//        cliente.setDiasBloqueo(clienteSeleccionado.getDiasBloqueo());
//        
//        mbClientesGrupos.getCmbClientesGrupos().setIdGrupoCte(clienteSeleccionado.getGrupo().getIdGrupoCte());
//        mbClientesGrupos.getMbFormatos().setLstFormatos(null);
//        mbClientesGrupos.getMbFormatos().cargarListaFormatos(clienteSeleccionado.getGrupo().getIdGrupoCte());
//    }
//    public void cancelar() {
//        clienteSeleccionado = null;
////        this.controlActualizar = 0;
//        this.limpiar();
//        this.actualizarRfc = false;
////        this.actualizar = false;
//        cliente = new Cliente();
//        mbClientesGrupos.getCmbClientesGrupos().setIdGrupoCte(0);
//    }
//    public void obtenerBancosClientes() {
//        try {
//            mbClientesBancos.getMbBanco().obtenerBancos(cliente.getIdCliente());
//
//        } catch (SQLException ex) {
//            Mensajes.mensajeError(ex.getMessage());
//        }
//    }
    public void cargarDatosClientes(String rfc) {
        try {
            DAOClientes dao = new DAOClientes();
            Cliente cl = this.convertir(dao.dameInformacionCliente(rfc));
//            this.setControlActualizar(1);
//            this.setActualizarRfc(true);
            mbDireccion.setDireccion(cl.getContribuyente().getDireccion());
            cliente.getContribuyente().setDireccion(cl.getContribuyente().getDireccion());
            cliente.setDescuentoComercial(cl.getDescuentoComercial());
            cliente.setDiasBloqueo(cl.getDiasBloqueo());
            cliente.setDiasCredito(cl.getDiasCredito());
            cliente.setLimiteCredito(cl.getLimiteCredito());
            cliente.setIdCliente(cl.getIdCliente());
            mbContribuyente.getContribuyente().setContribuyente(cl.getContribuyente().getContribuyente());
            mbContribuyente.getContribuyente().setCurp(cl.getContribuyente().getCurp());
            mbContribuyente.getContribuyente().setRfc(cl.getContribuyente().getRfc());
            mbContribuyente.getContribuyente().setIdRfc(cl.getContribuyente().getIdRfc());
            mbContribuyente.getContribuyente().setIdContribuyente(cl.getContribuyente().getIdContribuyente());
//            mbClientesBancos.getMbBanco().obtenerBancos(cl.getCodigoCliente());
//            mbClientesBancos.cargarBancos(cl.getCodigoCliente());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void guardarCuentasBancarias() {
        FacesMessage fMsg = null;
        RequestContext context = RequestContext.getCurrentInstance();
        boolean ok = mbClientesBancos.validar();
        if (ok == true) {
            try {
                DAOClientesBancos daoClientesBancos = new DAOClientesBancos();
//                mbClientesBancos.getClientesBancos().setCodigoCliente(cliente.getCodigoCliente());
                if (clienteBanco.getBancoLeyenda().getIdBanco() == 0) {
                    daoClientesBancos.guardarClientesBancos(mbClientesBancos.getClientesBancos());
                    fMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "");
                    fMsg.setDetail("Exito!! Nueva Cuenta Disponible !!");
                } else {
                    mbClientesBancos.getClientesBancos().setIdClienteBanco(clienteBanco.getIdClienteBanco());
                    daoClientesBancos.actualizarClientesBancos(mbClientesBancos.getClientesBancos());
                    fMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "");
                    fMsg.setDetail("Exito!! Cuenta Actualizada !!");
                }
//                mbClientesBancos.cargarBancos(cliente.getCodigoCliente());
            } catch (SQLException ex) {
                ok = false;
                fMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", ex.getMessage());
            }
        }
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
        context.addCallbackParam("ok", ok);
    }

    private TOCliente convertir(Cliente cliente) {
        TOCliente to = new TOCliente();
        to.setIdCliente(cliente.getIdCliente());
        to.setIdGrupoCte(cliente.getGrupo().getIdGrupoCte());
//        to.setIdFormato(cliente.getFormato().getIdFormato());
        to.setIdContribuyente(cliente.getContribuyente().getIdContribuyente());
        to.setIdDireccion(cliente.getDireccion().getIdDireccion());
        to.setIdEsquema(cliente.getIdEsquema());
        to.setFechaAlta(cliente.getFechaAlta());
        to.setDiasCredito(cliente.getDiasCredito());
        to.setLimiteCredito(cliente.getLimiteCredito());
        to.setDesctoComercial(cliente.getDescuentoComercial());
        to.setDiasBloqueo(cliente.getDiasBloqueo());
        return to;
    }

//    private void limpiar() {
//        mbDireccion.setDireccion(new Direccion());
//        mbContribuyente.setContribuyente(new Contribuyente());
//        cliente.setDescuentoComercial((float) 0.00);
////      cliente.setDescuentoProntoPago(0.00);
//        cliente.setDiasBloqueo(0);
//        cliente.setDiasCredito(0);
//        cliente.setIdCliente(0);
//        cliente.setLimiteCredito((float) 0.00);
//        cliente.getContribuyente().setDireccion(new Direccion());
//    }
    public void grabarContribuyente() {
        if (this.mbDireccion.validarDireccion(this.mbContribuyente.getContribuyente().getDireccion())) {
            if (this.mbContribuyente.grabar()) {
                this.cliente.setContribuyente(this.mbContribuyente.getContribuyente());
            }
        }
    }

    public void mttoContribuyente() {
        this.mbContribuyente.mttoContribuyente(this.cliente.getContribuyente());
    }

//    public void validarContribuyente() {
//        boolean ok = mbContribuyente.valida();
//        if (ok == true) {
//            try {
//                DAOContribuyentes dao = new DAOContribuyentes();
//                if (actualizarRfc == false) {
//                    boolean paso = dao.verificarContribuyente(mbContribuyente.getContribuyente().getRfc());
//                    if (paso == false) {
//                        dao.guardarContribuyente(mbContribuyente.getContribuyente(), mbContribuyente.getContribuyente().getDireccion());
//                        Mensajes.mensajeSucces("Exito, nuevo contribuyente disponible");
//                    } else {
//                        Mensajes.mensajeAlert("Error, Este contribuyente ya esta dado de alta");
//                    }
//                } else {
//                    dao.actualizarContribuyente(mbContribuyente.getContribuyente());
//                }
//            } catch (NamingException ex) {
//                Mensajes.mensajeError(ex.getMessage());
//                Logger.getLogger(MbClientes.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (SQLException ex) {
//
//                Mensajes.mensajeError(ex.getMessage());
//            }
//        }
//    }
    public boolean validarClientes() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if (cliente.getFormato().getIdFormato() == 0) {
            fMsg.setDetail("Error!! Formato de Cliente Requerido");
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
            context.addCallbackParam("ok", ok);
        } else if (cliente.getContribuyente().getIdContribuyente() == 0) {
            fMsg.setDetail("Error!! Contribuyente del Cliente Requerido");
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
            context.addCallbackParam("ok", ok);
        } else if (cliente.getContribuyente().getIdRfc() == 0) {
            fMsg.setDetail("Error!! RFC del Contribuyente Requerido");
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
            context.addCallbackParam("ok", ok);
        } else if (cliente.getDiasCredito() == 0) {
            fMsg.setDetail("Error!! Dias de Credito Requerido");
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
            context.addCallbackParam("ok", ok);
        } else {
            ok = true;
        }
        return ok;
    }

    public void dameInformacion() {
        if (clienteBanco.getBancoLeyenda().getIdBanco() == 0) {
            mbClientesBancos.setClientesBancos(new ClienteBanco());
            mbClientesBancos.getMbBanco().getObjBanco().setIdBanco(0);
        } else {
            mbClientesBancos.setClientesBancos(this.clienteBanco);
            mbClientesBancos.getMbBanco().getObjBanco().setIdBanco(clienteBanco.getBancoLeyenda().getIdBanco());
        }
    }

    public void limpiarCampos() {
//        clientes.setIdBanco(0);
//        actualizar = true;
        mbClientesBancos.cargarBancos(clienteBanco.getCodigoCliente());
//        mbClientesBancos.getMbBanco().getObjBanco().setIdBanco(0);
    }

//    public void dameCuentasBancarias() {
//        mbClientesBancos.getMbBanco().cargarBancos(cliente.getIdCliente());
//    }
    public void cancelar() {
        if (this.indexClienteSeleccionado != -1) {
            this.cliente = this.clientes.get(this.indexClienteSeleccionado);
        } else {
            this.cliente = null;
        }
    }

    public void modificar() {
        this.indexClienteSeleccionado = this.clientes.indexOf(this.cliente);
        Cliente tmp = this.clientes.get(this.indexClienteSeleccionado);
        this.cliente = new Cliente();
        this.cliente.setIdCliente(tmp.getIdCliente());
        this.cliente.setGrupo(tmp.getGrupo());
        this.cliente.setFormato(tmp.getFormato());
        this.cliente.setContribuyente(tmp.getContribuyente());
        this.cliente.setDireccion(tmp.getDireccion());
        this.cliente.setIdEsquema(tmp.getIdEsquema());
        this.cliente.setFechaAlta(tmp.getFechaAlta());
        this.cliente.setDiasCredito(tmp.getDiasCredito());
        this.cliente.setLimiteCredito(tmp.getLimiteCredito());
        this.cliente.setDescuentoComercial(tmp.getDescuentoComercial());
        this.cliente.setDiasBloqueo(tmp.getDiasBloqueo());
        this.mbTiendasFormatos.cargarListaCombo(this.cliente.getGrupo().getIdGrupoCte());
        this.mbClientesBancos.cargarBancos(this.cliente.getIdCliente());
    }

    public void guardar() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        if (this.validarClientes()) {
            try {
                DAOClientes dao = new DAOClientes();
                if (this.cliente.getIdCliente() == 0) {
                    this.cliente.getDireccion().setIdDireccion(this.mbDireccion.agregar(this.cliente.getDireccion()));
                    this.cliente.setIdCliente(dao.agregar(this.convertir(this.cliente)));
                    this.mbClientesBancos.cargarBancos(this.cliente.getIdCliente());
                } else {
                    dao.modificar(this.convertir(this.cliente));
                }
                Mensajes.mensajeSucces("El cliente se grabo correctamente !!!");
                ok = true;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
        context.addCallbackParam("ok", ok);
    }

    public void nuevo() {
//        if(this.cliente==null) {
        this.indexClienteSeleccionado = -1;
//        } else {
//            this.indexClienteSeleccionado=clientes.indexOf(this.cliente);
//        }
        this.cliente = new Cliente();
        this.mbTiendasFormatos.cargarListaCombo(this.cliente.getGrupo().getIdGrupoCte());
    }

    private Cliente convertir(TOCliente to) {
        Cliente cte = new Cliente();
        cte.setIdCliente(to.getIdCliente());
        cte.getGrupo().setIdGrupoCte(to.getIdGrupoCte());
        cte.getGrupo().setGrupoCte(to.getGrupoCte());
        cte.setNombreEsquema(to.getEsquema());
//        cte.getFormato().setIdFormato(to.getIdFormato());
//        cte.getFormato().setFormato(to.getFormato());
        cte.getFormato().setIdGrupoCte(to.getIdGrupoCte());
        cte.getContribuyente().setIdContribuyente(to.getIdContribuyente());
        cte.getContribuyente().setContribuyente(to.getContribuyente());
        cte.getContribuyente().setIdRfc(to.getIdRfc());
        cte.getContribuyente().setRfc(to.getRfc());
        cte.getContribuyente().setCurp(to.getCurp());
        cte.getContribuyente().setDireccion(this.mbDireccion.obtener(to.getIdDireccionFiscal()));
        cte.setDireccion(this.mbDireccion.obtener(to.getIdDireccion()));
        cte.setFechaAlta(to.getFechaAlta());
        cte.setDiasCredito(to.getDiasCredito());
        cte.setLimiteCredito(to.getLimiteCredito());
        cte.setDescuentoComercial(to.getDesctoComercial());
        cte.setDiasBloqueo(to.getDiasBloqueo());
        return cte;
    }

    private void cargarClientes() {
        this.clientes = new ArrayList<Cliente>();
        try {
            DAOClientes daoCliente = new DAOClientes();
            for (TOCliente to : daoCliente.lstClientes()) {
                this.clientes.add(this.convertir(to));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public ArrayList<Cliente> getClientes() {
        if (this.clientes == null) {
            this.cargarClientes();
        }
        return clientes;
    }

    public void setClientes(ArrayList<Cliente> clientes) {
        this.clientes = clientes;
    }

//    public Cliente getClienteSeleccionado() {
//        return clienteSeleccionado;
//    }
//
//    public void setClienteSeleccionado(Cliente clienteSeleccionado) {
//        this.clienteSeleccionado = clienteSeleccionado;
//    }
    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public MbDireccion getMbDireccion() {
        return mbDireccion;
    }

    public void setMbDireccion(MbDireccion mbDireccion) {
        this.mbDireccion = mbDireccion;
    }

    public MbContribuyentes getMbContribuyente() {
        return mbContribuyente;
    }

    public void setMbContribuyente(MbContribuyentes mbContribuyente) {
        this.mbContribuyente = mbContribuyente;
    }

//    public int getPersonaFisica() {
//        return personaFisica;
//    }
//
//    public void setPersonaFisica(int personaFisica) {
//        this.personaFisica = personaFisica;
//    }
//    public boolean isActualizarRfc() {
//        return actualizarRfc;
//    }
//
//    public void setActualizarRfc(boolean actualizarRfc) {
//        this.actualizarRfc = actualizarRfc;
//    }
    public MbClientesBancos getMbClientesBancos() {
        return mbClientesBancos;
    }

    public void setMbClientesBancos(MbClientesBancos mbClientesBancos) {
        this.mbClientesBancos = mbClientesBancos;
    }

    public ClienteBanco getClienteBanco() {
        return clienteBanco;
    }

    public void setClienteBanco(ClienteBanco clienteBanco) {
        this.clienteBanco = clienteBanco;
    }

    public MbClientesGrupos getMbClientesGrupos() {
        return mbClientesGrupos;
    }

    public void setMbClientesGrupos(MbClientesGrupos mbClientesGrupos) {
        this.mbClientesGrupos = mbClientesGrupos;
    }

//    public boolean isDireccionCliente() {
//        return direccionCliente;
//    }
//
//    public void setDireccionCliente(boolean direccionCliente) {
//        this.direccionCliente = direccionCliente;
//    }
//    public boolean isNuevoContribuyente() {
//        return nuevoContribuyente;
//    }
//
//    public void setNuevoContribuyente(boolean nuevoContribuyente) {
//        this.nuevoContribuyente = nuevoContribuyente;
//    }
//    public boolean isActivarRfc() {
//        return activarRfc;
//    }
//
//    public void setActivarRfc(boolean activarRfc) {
//        this.activarRfc = activarRfc;
//    }
    public MbTiendasFormatos getMbTiendasFormatos() {
        return mbTiendasFormatos;
    }

    public void setMbTiendasFormatos(MbTiendasFormatos mbTiendasFormatos) {
        this.mbTiendasFormatos = mbTiendasFormatos;
    }

    public ArrayList<Cliente> getListaClientes() {
        return listaClientes;
    }

    public void setListaClientes(ArrayList<Cliente> listaClientes) {
        this.listaClientes = listaClientes;
    }

    public Cliente getClienteSeleccionado() {
        return clienteSeleccionado;
    }

    public void setClienteSeleccionado(Cliente clienteSeleccionado) {
        this.clienteSeleccionado = clienteSeleccionado;
    }

    public ArrayList<Cliente> getListaFiltros() {
        return listaFiltros;
    }

    public void setListaFiltros(ArrayList<Cliente> listaFiltros) {
        this.listaFiltros = listaFiltros;
    }
    
    
    
}

package agentes;

import Message.Mensajes;
import agentes.dao.DaoAgentes;
import agentes.dominio.Agente;
import agentes.dominio.EnumNivel;
import cedis.MbMiniCedis;
import cedis.dao.DAOMiniCedis;
import cedis.dominio.MiniCedis;
import contactos.MbContactos;
import contactos.dao.DAOContactos;
import contactos.dao.DAOTelefonos;
import contactos.dominio.Contacto;
import contactos.dominio.Telefono;
import contactos.dominio.TelefonoTipo;
import contribuyentes.Contribuyente;
import contribuyentes.DAOContribuyentes;
import contribuyentes.MbBuscarContribuyente;
import contribuyentes.MbContribuyentes;
import direccion.MbDireccion;
import direccion.dao.DAODireccion;
import direccion.dominio.Direccion;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;
import utilerias.Utilerias;

/**
 *
 * @author Anita
 */
@Named(value = "mbAgentes")
@SessionScoped
public class MbAgentes implements Serializable {

    @ManagedProperty(value = "#{mbCedis}")
    private MbMiniCedis mbCedis = new MbMiniCedis();
    @ManagedProperty(value = "#{mbBuscarContribuyente}")
    private MbBuscarContribuyente mbBuscarContribuyente = new MbBuscarContribuyente();
    @ManagedProperty(value = "#{mbContactos}")
    private MbContactos mbContactos = new MbContactos();
    @ManagedProperty(value = "#{mbAgente}")
    private MbContribuyentes mbContribuyente = new MbContribuyentes();
    @ManagedProperty(value = "#{mbDireccion}")
    private MbDireccion mbDireccion = new MbDireccion();
    private Agente seleccionListaAgente = null;
    private Agente agente = new Agente();
    private ArrayList<Agente> listaAgente;
    private ArrayList<SelectItem> listaAsentamientos = new ArrayList<SelectItem>();
    private ArrayList<SelectItem> listaMiniCedis = null;
    private ArrayList<SelectItem> lstAgente = null;
    private ArrayList<SelectItem> listaTelefonos = new ArrayList<SelectItem>();
    private ArrayList<SelectItem> lstNiveles = null;
    private ArrayList<SelectItem> lstSupervisor = null;
    private int personaFisica = 0;
    private DAOMiniCedis dao;
    private int flgDireccion = 0;
    boolean editarAsentamiento = false;
    private String lblCancelar = "";
    private String titleCancelar = "";
    private String lblnuevoAgente = "";
    private String lblNuevaDireccionAgente = "";
    private int actualizar = 0;
    private String colonia;
    private Agente cmbAgente = new Agente();
    int idContacto = 0;
    private boolean buscadorContribuyentes = false;
    private int valorEnum;
    private int valorSupervisor;

    public MbAgentes() {
        titleCancelar = "Cancelar Contacto";
        lblCancelar = "ui-icon-arrowreturnthick-1-w";
        lblnuevoAgente = "ui-icon-disk";
        lblNuevaDireccionAgente = "ui-icon-disk";
    }

    @PostConstruct
    public void inic() {

    }

//    public void cargarSuperVisor() {
//        if (valorEnum > 0 && agente.getMiniCedis().getIdCedis() > 0) {
//            try {
//                DaoAgentes dao = new DaoAgentes();
//                ArrayList<Agente> lst;
//                lst = new ArrayList<Agente>();
//                lst = dao.dameSupervisor(agente.getMiniCedis().getIdCedis(), valorEnum);
//                Agente a = new Agente();
//                a.setIdAgente(0);
//                a.setAgente("Seleccione un Supervisor");
//                try {
//                    lstSupervisor = new ArrayList<SelectItem>();
//                    lstSupervisor.add(new SelectItem(a.getIdAgente(), a.getAgente()));
//                } catch (Exception e) {
//                    System.out.println(e.getMessage());
//                }
//                for (Agente agente : lst) {
//                    lstSupervisor.add(new SelectItem(agente.getIdAgente(), agente.getAgente()));
//                }
//            } catch (SQLException ex) {
//                Mensajes.mensajeError(ex.getMessage());
//            }
//        }
//    }
    public void obtenerDireccion() {
        agente.setDireccionAgente(mbDireccion.obtener(seleccionListaAgente.getDireccionAgente().getIdDireccion()));
    }

    private void cargarTablaAgente() {
        try {
            DaoAgentes daoAgentes = new DaoAgentes();
            listaAgente = daoAgentes.listaAgentes();
        } catch (SQLException ex) {
            Mensajes.mensajeAlert(ex.getMessage());
        }
    }

    public ArrayList<Agente> getListaAgente() {
        if (listaAgente == null) {
            cargarTablaAgente();
        }
        return listaAgente;
    }

    public void buscar() {
        mbContribuyente.getContribuyente().setDireccion(new Direccion());
        if (mbContribuyente.getContribuyente().getRfc().trim().length() == 12 || mbContribuyente.getContribuyente().getRfc().trim().length() == 13) {
            mbBuscarContribuyente.setTipoBuscar("1");
            this.mbBuscarContribuyente.setStrBuscar(mbContribuyente.getContribuyente().getRfc().toUpperCase());
            try {
                Utilerias utilerias = new Utilerias();
                String error = utilerias.verificarRfc(mbContribuyente.getContribuyente().getRfc().toUpperCase());
                if (error.equals("")) {
                    mbContribuyente.setContribuyente(this.mbBuscarContribuyente.buscarRfc());
                    if (mbContribuyente.getContribuyente().getContribuyente() == "") {
                        this.buscadorContribuyentes = false;
                    } else {
                        DAODireccion daoDireccion = new DAODireccion();
                        agente.getContribuyente().setDireccion(daoDireccion.obtenerDireccion(mbBuscarContribuyente.getContribuyente().getDireccion().getIdDireccion()));
                        this.buscadorContribuyentes = true;
                        personaFisica = 0;
                    }
                } else {
                    Mensajes.mensajeAlert(error);
                }
            } catch (NamingException | SQLException ex) {
                Mensajes.mensajeError(ex.getMessage());
                Logger.getLogger(MbAgentes.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NullPointerException ex) {
                this.buscadorContribuyentes = false;
                mbContribuyente.setContribuyente(new Contribuyente());
            }
        } else {
            buscadorContribuyentes = false;
            Mensajes.mensajeAlert("Error! la longitud del rfc no es correcta");
            mbContribuyente.setContribuyente(new Contribuyente());
            agente.getContribuyente().setDireccion(new Direccion());
        }
    }

    public void agregarNuevoAgente() {
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = null;
        boolean ok = false;
        ok = validarAgente();
        if (ok == true) {
//            mbContribuyente.valida();
            try {
                listaAgente = null;
                DaoAgentes daoAgente = new DaoAgentes();
                if (actualizar == 0) {
                    boolean okExito = false;
                    agente.setNivel(valorEnum);
                    agente.setSuperior(valorSupervisor);
                    okExito = daoAgente.guardarAgentes(agente);
                    if (okExito == true) {
                        ok = true;
                        fMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "");
                        fMsg.setDetail("Exito!! Nuevo Agente Disponible");
                        FacesContext.getCurrentInstance().addMessage(null, fMsg);
                    }
                } else {
                    daoAgente.actualizarAgente(agente, mbContribuyente.getContribuyente());
                    this.setActualizar(0);
                    ok = true;
                    fMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "");
                    fMsg.setDetail("Exito!! Agente Actualizado");
                    FacesContext.getCurrentInstance().addMessage(null, fMsg);
                }
            } catch (SQLException ex) {
                ok = false;
                int errorCode = ex.getErrorCode();
                switch (errorCode) {
                    case 2601:
                        Mensajes.mensajeAlert("Este Contribuyente ya esta dado de alta. Implemente el buscador");
                        break;
                    default:
                        Mensajes.mensajeError(ex.getMessage());
                        break;
                }
            }
        }
        mbContactos = new MbContactos();
        seleccionListaAgente = null;
        context.addCallbackParam("ok", ok);
    }

    public boolean validarAgente() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = null;
        if (agente.getCodigo() == 0) {
            fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
            fMsg.setDetail("Se requiere un codigo del agente !!");
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
            context.addCallbackParam("okContribuyente", ok);
        } else if (agente.getAgente().equals("")) {
            fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
            fMsg.setDetail("Se requiere el Agente !!");
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
            context.addCallbackParam("okContribuyente", ok);
        } else if (valorEnum == 0) {
            fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
            fMsg.setDetail("Se requiere un nivel !!");
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
            context.addCallbackParam("okContribuyente", ok);
        } else if (agente.getMiniCedis().getIdCedis() == 0) {
            fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
            fMsg.setDetail("Se requiere un Cedis!!");
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
            context.addCallbackParam("okContribuyente", ok);
        } else {
            ok = true;
        }

        return ok;
    }

    public void dameStatusRfc() {
        int longitud = mbContribuyente.getContribuyente().getRfc().length();
        if (longitud == 13) {
            personaFisica = 1;
        } else {
            mbContribuyente.getContribuyente().setCurp("");
            personaFisica = 2;
        }
    }

    public void cargarTiposTelefonos() {
        mbContactos.getMbTelefonos().cargaTipos();
    }

    public void respaldoDireccionContribuyente() {
        this.flgDireccion = 1;
        mbDireccion.setDireccion(this.agente.getContribuyente().getDireccion());
        if (actualizar == 1) {
            this.mbDireccion.buscarAsentamientos();
            this.setColonia(agente.getContribuyente().getDireccion().getColonia());
            mbDireccion.setEditarAsentamiento(true);
        } else {
            ArrayList<SelectItem> lst = new ArrayList<SelectItem>();
            mbDireccion.setListaAsentamientos(lst);
        }
    }

    public void respaldoDireccionAgente() {
        this.flgDireccion = 2;
        mbDireccion.setDireccion(this.agente.getDireccionAgente());
        if (actualizar == 1) {
            this.mbDireccion.buscarAsentamientos();
            mbDireccion.setEditarAsentamiento(true);
            try {
                this.mbDireccion.getDireccion().setColonia(agente.getDireccionAgente().getColonia());
            } catch (Exception e) {
                System.err.println(e);
            }
        } else {
            ArrayList<SelectItem> lst = new ArrayList<SelectItem>();
            mbDireccion.setListaAsentamientos(lst);
        }
    }

    public void direccionContirbuyente() {
        mbDireccion.setDireccion(agente.getContribuyente().getDireccion());
//        mbContribuyente.getContribuyente().setDireccion(mbDireccion.getDireccion());
        flgDireccion = 1;
    }

    public void validarDireccion() throws SQLException {
        boolean paso = mbDireccion.validarDireccion();
        if (paso == true) {
            if (flgDireccion == 1) {
                agente.getContribuyente().setDireccion(mbDireccion.getDireccion());
            } else if (flgDireccion == 2) {
                agente.setDireccionAgente(mbDireccion.getDireccion());
            }
        }
        if (this.getActualizar() == 1 && flgDireccion == 2) {
//            try {
            DaoAgentes dao = new DaoAgentes();
            dao.actualizarDireccion(agente.getDireccionAgente(), agente.getIdAgente());
//                DAODireccion daoDireccion = new DAODireccion();
//                try {
//                    daoDireccion.modificar(mbDireccion.getDireccion().getIdDireccion(), mbDireccion.getDireccion().getCalle(), mbDireccion.getDireccion().getNumeroExterior(), mbDireccion.getDireccion().getNumeroInterior(), mbDireccion.getDireccion().getReferencia(), mbDireccion.getDireccion().getPais().getIdPais(), mbDireccion.getDireccion().getCodigoPostal(), mbDireccion.getDireccion().getEstado(), mbDireccion.getDireccion().getMunicipio(), mbDireccion.getDireccion().getLocalidad(), mbDireccion.getDireccion().getColonia(), mbDireccion.getDireccion().getNumeroLocalizacion());
//                } catch (SQLException ex) {
//                    Logger.getLogger(MbAgentes.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            } catch (NamingException ex) {
//                Logger.getLogger(MbAgentes.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }
        mbDireccion.setDireccion(new Direccion());

    }

    public void validarContacto() {
        boolean ok = false;

        ok = mbContactos.validarContactos();
        if (ok == true) {
            agente.setContacto(this.mbContactos.getContacto());
        }
    }

    public void dameValor() {
        if (agente.getContacto().getIdContacto() > 0) {
            this.setLblCancelar("ui-icon-trash");
            this.setTitleCancelar("Eliminar Contacto");
            this.mbContactos.getMbTelefonos().cargaTelefonos(agente.getContacto().getIdContacto());
        } else {
            this.setLblCancelar("ui-icon-arrowreturnthick-1-w");
            this.setTitleCancelar("Cancelar Contacto");
        }
    }

    public void cargarDatosActualizar() {
        try {
            this.setActualizar(1);
            ArrayList<Telefono> telefono = new ArrayList<Telefono>();
            this.agente.setAgente(seleccionListaAgente.getAgente());
            this.agente.getMiniCedis().setIdCedis(seleccionListaAgente.getMiniCedis().getIdCedis());
            this.agente.setIdAgente(seleccionListaAgente.getIdAgente());
            this.agente.getContribuyente().setRfc(seleccionListaAgente.getContribuyente().getRfc());
            this.agente.getContribuyente().setIdContribuyente(seleccionListaAgente.getContribuyente().getIdContribuyente());
            DAOTelefonos telefonos = new DAOTelefonos();
            obtenerDireccion();
            this.agente.getContacto().setCorreo(seleccionListaAgente.getContacto().getCorreo());
            this.agente.getContacto().setIdContacto(seleccionListaAgente.getContacto().getIdContacto());
            valorEnum = seleccionListaAgente.getNivel();
            if (agente.getContribuyente().getIdContribuyente() == 0) {
                mbContribuyente.getContribuyente().setRfc("");
            } else {
                DAOContribuyentes daoC = new DAOContribuyentes();
                mbContribuyente.setContribuyente(daoC.obtenerContribuyente(seleccionListaAgente.getContribuyente().getIdContribuyente()));
//                agente.setContribuyente();
            }
            valorSupervisor = seleccionListaAgente.getSuperior();
            try {
                telefono = telefonos.obtenerTelefonos(seleccionListaAgente.getContacto().getIdContacto());
                TelefonoTipo t0 = new TelefonoTipo(false);
                t0.setTipo("Nuevo Tipo");
                t0.setIdTipo(0);
                listaTelefonos = new ArrayList<SelectItem>();
                listaTelefonos.add(new SelectItem(t0, t0.toString()));
                for (Telefono t : telefono) {
                    listaTelefonos.add(new SelectItem(t, t.toString()));
                }
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getMessage());
            Logger.getLogger(MbAgentes.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.getMbContactos().cargaContactos(3, agente.getIdAgente());
        DAOContactos dao = null;
        try {
            dao = new DAOContactos();
            for (Contacto c : dao.obtenerContactos(3, agente.getIdAgente())) {
                idContacto = c.getIdContacto();
                break;
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void guardarContacto() {
        boolean ok = mbContactos.validarContactos();
        if (ok) {
            try {
                DAOContactos dao = null;
                try {
                    dao = new DAOContactos();
                } catch (NamingException ex) {
                    Mensajes.mensajeError(ex.getMessage());
                    Logger.getLogger(MbAgentes.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (mbContactos.getContacto().getIdContacto() == 0) {
                    dao.agregar(mbContactos.getContacto(), agente.getIdAgente(), 3);
                    Mensajes.mensajeSucces("Exito, nuevo contacto disponible");
                } else {
                    dao.modificar(mbContactos.getContacto());
                    Mensajes.mensajeSucces("Exito, contacto actualizado");
                }
                this.getMbContactos().cargaContactos(3, agente.getIdAgente());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getMessage());
                Logger.getLogger(MbAgentes.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void deseleccionar() {
        if (this.getActualizar() == 1) {
            this.setActualizar(0);

        }
        buscadorContribuyentes = false;
        seleccionListaAgente = null;
        mbContactos = new MbContactos();
        personaFisica = 0;
    }

    public String getLblCancelar() {
        return lblCancelar;
    }

    public void setLblCancelar(String lblCancelar) {
        this.lblCancelar = lblCancelar;
    }

    public void setListaAgente(ArrayList<Agente> listaAgente) {
        this.listaAgente = listaAgente;
    }

    public Agente getSeleccionListaAgente() {
        return seleccionListaAgente;
    }

    public void setSeleccionListaAgente(Agente seleccionListaAgente) {
        this.seleccionListaAgente = seleccionListaAgente;
    }

    public MbMiniCedis getMbCedis() {
        return mbCedis;
    }

    public void setMbCedis(MbMiniCedis mbCedis) {
        this.mbCedis = mbCedis;
    }

    public ArrayList<SelectItem> getListaAsentamientos() {
        return listaAsentamientos;
    }

    public void setListaAsentamientos(ArrayList<SelectItem> listaAsentamientos) {
        this.listaAsentamientos = listaAsentamientos;
    }

    public Agente getAgente() {
        return agente;
    }

    public void setAgente(Agente agente) {
        this.agente = agente;
    }

    private void obtenerListaMiniCedis() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        this.listaMiniCedis = new ArrayList<SelectItem>();
        try {
            MiniCedis p0 = new MiniCedis();
            p0.setIdCedis(0);
            p0.setCedis("Seleccione un CEDIS");
            SelectItem cero = new SelectItem(p0, p0.toString());
            listaMiniCedis.add(cero);
            this.dao = new DAOMiniCedis();
            for (MiniCedis m : this.dao.obtenerListaMiniCedisTodos()) {
                listaMiniCedis.add(new SelectItem(m, m.toString()));
            }
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

    public int getPersonaFisica() {
        return personaFisica;
    }

    public void setPersonaFisica(int personaFisica) {
        this.personaFisica = personaFisica;
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

    public ArrayList<SelectItem> getListaMiniCedis() {
        if (this.listaMiniCedis == null) {
            this.obtenerListaMiniCedis();
        }
        return listaMiniCedis;
    }

    public void cargarAgente() {
        this.setActualizar(0);
        limpiarCampos();
        this.getMbContactos().cargaContactos(2, 0);
        this.getMbContactos().getMbTelefonos().cargaTelefonos(0);
        this.agente.getContacto().setTelefonos(new ArrayList<Telefono>());
        this.cargaListaTelefonos();
        buscadorContribuyentes = false;
    }

    public String salir() {
        return "index.xhtml";
    }

    public void validarTelefonos() {
        boolean ok = mbContactos.getMbTelefonos().validarTelefonos();
        if (ok == true) {
            try {
                DAOTelefonos dao = new DAOTelefonos();
                if (mbContactos.getMbTelefonos().getTelefono().getIdTelefono() == 0) {
                    try {
                        dao.agregar(mbContactos.getMbTelefonos().getTelefono(), idContacto);
                        Mensajes.mensajeSucces("Exito, Nuevo telefono disponible");
                    } catch (SQLException ex) {
                        Mensajes.mensajeError(ex.getMessage());
                        Logger
                                .getLogger(MbAgentes.class
                                        .getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    dao.modificar(mbContactos.getMbTelefonos().getTelefono());
                    Mensajes.mensajeSucces("Exito, telefono modificado");
                }
                mbContactos.getMbTelefonos().cargaTelefonos(idContacto);
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    public void guardarTelefonoTipo() {
        mbContactos.getMbTelefonos().grabarTipo();
        mbContactos.getMbTelefonos().cargaTipos();
    }

    public void mantenimientoTelefonosTipo() {
        mbContactos.getMbTelefonos().setTipo(mbContactos.getMbTelefonos().getTelefono().getTipo());

    }

    public void cargaListaTelefonos() {
        TelefonoTipo t0 = new TelefonoTipo(false);
        t0.setTipo("Nuevo Tipo");
        listaTelefonos = new ArrayList<SelectItem>();
        listaTelefonos.add(new SelectItem(t0, t0.toString()));
        for (Telefono t : this.agente.getContacto().getTelefonos()) {
            listaTelefonos.add(new SelectItem(t, t.toString()));
        }
    }

    public void cargarTelefonos() {
        try {
            mbContactos.getCorreo().setIdContacto(0);
            this.getMbContactos().getMbTelefonos().cargaTelefonos(mbContactos.getContacto().getIdContacto());
            this.getMbContactos().obtenerCorreo(mbContactos.getContacto().getIdContacto());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
            Logger.getLogger(MbAgentes.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getMessage());
            Logger.getLogger(MbAgentes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void actualizarCorreos() {
        try {
            if (mbContactos.getCorreo().getCorreo().equals("")) {
                Mensajes.mensajeAlert("Error! correo requerido");
            } else {
                DAOContactos daoContactos = new DAOContactos();
                daoContactos.modificar(mbContactos.getCorreo());
                mbContactos.setListaCorreos(null);
                this.getMbContactos().obtenerCorreo(mbContactos.getContacto().getIdContacto());
                Mensajes.mensajeSucces("Exito! correo actualizado");
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
            Logger.getLogger(MbAgentes.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getMessage());
            Logger.getLogger(MbAgentes.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void eliminarTelefono() {
        try {
            DAOTelefonos daotelefonos = new DAOTelefonos();
            daotelefonos.eliminar(this.getMbContactos().getMbTelefonos().getTelefono().getIdTelefono());
            boolean ok = false;
            RequestContext context = RequestContext.getCurrentInstance();
            FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
            fMsg.setDetail("Telefono Eliminado");
            FacesContext.getCurrentInstance().addMessage(null, fMsg);

        } catch (NamingException ex) {
            Logger.getLogger(MbAgentes.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(MbAgentes.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void limpiarCampos() {
        mbContribuyente.getContribuyente().setRfc("");
        mbContribuyente.getContribuyente().setCurp("");
        mbContribuyente.getContribuyente().setContribuyente("");
        lstSupervisor = new ArrayList<SelectItem>();
        this.setValorEnum(0);
        this.agente.getMiniCedis().setIdCedis(0);
        this.agente.getTelefono().setIdTelefono(0);
        this.agente.setAgente("");
        this.agente.getContacto().setCorreo("");
        this.agente.setDireccionAgente(new Direccion());
        this.agente.getContribuyente().setDireccion(new Direccion());
    }

    public void limpiarContriuyente() {
        mbContribuyente.setContribuyente(new Contribuyente());
    }

    public void guardarContribuyente() {
        mbContribuyente.getContribuyente().setDireccion(agente.getContribuyente().getDireccion());
        boolean ok = mbContribuyente.valida();
        if (ok == true) {
//            if (agente.getContacto().getCorreo().equals("")) {
//                Mensajes.mensajeAlert("Se requiere un correo");
//            } else {
//                ok = utilerias.Utilerias.validarEmail(agente.getContacto().getCorreo());
//                if (ok == false) {
//                    Mensajes.mensajeAlert("Correo no valido");
//                }
//                if (ok == true) {
            if (seleccionListaAgente != null && seleccionListaAgente.getContribuyente().getIdContribuyente() == 0) {
                DaoAgentes dao = new DaoAgentes();
                try {
                    dao.agregarDireccionAgentesContribuyentes(mbContribuyente.getContribuyente().getDireccion(), seleccionListaAgente.getIdAgente(), mbContribuyente.getContribuyente());
                } catch (SQLException ex) {
                    Mensajes.mensajeError(ex.getMessage());
                }
            } else if (actualizar == 1) {
                try {
                    DAOContribuyentes dao = new DAOContribuyentes();
                    dao.actualizarContribuyente(mbContribuyente.getContribuyente());
                    Mensajes.mensajeSucces("Contribuyente Actualizado");
                } catch (NamingException ex) {
                    Logger.getLogger(MbAgentes.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SQLException ex) {
                    Mensajes.mensajeError(ex.getMessage());
                    Logger.getLogger(MbAgentes.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    DAOContribuyentes dao = new DAOContribuyentes();
                    boolean okVericiar = dao.verificarContribuyente(mbContribuyente.getContribuyente().getRfc());
                    if (okVericiar == true) {
                        Mensajes.mensajeAlert("Este Rfc ya esta asignado a un contribuyente");
                    } else {
                        Mensajes.mensajeSucces("Exito !! Datos de Contribuyente correctos");
                    }
                } catch (NamingException ex) {
                    Logger.getLogger(MbAgentes.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SQLException ex) {
                    System.err.println(ex.getMessage());
                    Mensajes.mensajeError(ex.getMessage());
                }
//                    }

//                }
            }
        }
    }

    public void dameContribuyente() {
        if (seleccionListaAgente != null && seleccionListaAgente.getContribuyente().getIdContribuyente() > 0) {
            try {
                DAOContribuyentes daoContribuyente = new DAOContribuyentes();
                mbContribuyente.setContribuyente(daoContribuyente.obtenerContribuyente(seleccionListaAgente.getContribuyente().getIdContribuyente()));
                DAODireccion dao = new DAODireccion();
                agente.getContribuyente().setDireccion(dao.obtenerDireccion(mbContribuyente.getContribuyente().getDireccion().getIdDireccion()));
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    public void dameTelefono() {
        try {
            if (agente.getTelefono().getIdTelefono() > 0) {
                this.getMbContactos().getMbTelefonos().setTelefono(agente.getTelefono());
                this.getMbContactos().getMbTelefonos().getTelefono().setIdTelefono(agente.getTelefono().getIdTelefono());
            } else {
                this.getMbContactos().getMbTelefonos().setTelefono(new Telefono());
            }
        } catch (NullPointerException e) {
            agente.getTelefono().setIdTelefono(0);
        }
    }

    public void setListaMiniCedis(ArrayList<SelectItem> listaMiniCedis) {
        this.listaMiniCedis = listaMiniCedis;
    }

    public DAOMiniCedis getDao() {
        return dao;
    }

    public void setDao(DAOMiniCedis dao) {
        this.dao = dao;
    }

    public boolean isEditarAsentamiento() {
        return editarAsentamiento;
    }

    public void setEditarAsentamiento(boolean editarAsentamiento) {
        this.editarAsentamiento = editarAsentamiento;
    }

    public MbContactos getMbContactos() {
        return mbContactos;
    }

    public void setMbContactos(MbContactos mbContactos) {
        this.mbContactos = mbContactos;
    }

    public String getTitleCancelar() {
        return titleCancelar;
    }

    public void setTitleCancelar(String titleCancelar) {
        this.titleCancelar = titleCancelar;
    }

    public String getLblnuevoAgente() {
        return lblnuevoAgente;
    }

    public void setLblnuevoAgente(String lblnuevoAgente) {
        this.lblnuevoAgente = lblnuevoAgente;
    }

    public String getLblNuevaDireccionAgente() {
        return lblNuevaDireccionAgente;
    }

    public void setLblNuevaDireccionAgente(String lblNuevaDireccionAgente) {
        this.lblNuevaDireccionAgente = lblNuevaDireccionAgente;
    }

    public int getActualizar() {
        return actualizar;
    }

    public void setActualizar(int actualizar) {
        this.actualizar = actualizar;
    }

    public ArrayList<SelectItem> getListaTelefonos() {
        return listaTelefonos;
    }

    public void setListaTelefonos(ArrayList<SelectItem> listaTelefonos) {
        this.listaTelefonos = listaTelefonos;
    }

    public String getColonia() {
        return colonia;
    }

    public void setColonia(String colonia) {
        this.colonia = colonia;
    }

    public ArrayList<SelectItem> getLstAgente() {
        if (lstAgente == null) {
            lstAgente = new ArrayList<SelectItem>();
            cargarTablaAgente();
            Agente agent = new Agente();
            agent.setIdAgente(0);
            agent.setAgente("Nuevo Agente");
            lstAgente.add(new SelectItem(agent, agent.getAgente()));
            for (Agente agentes : listaAgente) {
                lstAgente.add(new SelectItem(agentes, agentes.getAgente()));
            }
        }
        return lstAgente;
    }

    public void setLstAgente(ArrayList<SelectItem> lstAgente) {
        this.lstAgente = lstAgente;
    }

    public Agente getCmbAgente() {
        return cmbAgente;
    }

    public void setCmbAgente(Agente cmbAgente) {
        this.cmbAgente = cmbAgente;
    }

    public MbBuscarContribuyente getMbBuscarContribuyente() {
        return mbBuscarContribuyente;
    }

    public void setMbBuscarContribuyente(MbBuscarContribuyente mbBuscarContribuyente) {
        this.mbBuscarContribuyente = mbBuscarContribuyente;
    }

    public boolean isBuscadorContribuyentes() {
        return buscadorContribuyentes;
    }

    public void setBuscadorContribuyentes(boolean buscadorContribuyentes) {
        this.buscadorContribuyentes = buscadorContribuyentes;
    }

    public ArrayList<SelectItem> getLstNiveles() {

        if (lstNiveles == null) {
            lstNiveles = new ArrayList<SelectItem>();
            for (EnumNivel e : EnumNivel.values()) {
                lstNiveles.add(new SelectItem(e.getValorNivel(), e.toString()));
            }
        }
        return lstNiveles;
    }

    public void setLstNiveles(ArrayList<SelectItem> lstNiveles) {
        this.lstNiveles = lstNiveles;
    }

    public int getValorEnum() {
        return valorEnum;
    }

    public void setValorEnum(int valorEnum) {
        this.valorEnum = valorEnum;
    }

    public ArrayList<SelectItem> getLstSupervisor() {
        return lstSupervisor;
    }

    public void setLstSupervisor(ArrayList<SelectItem> lstSupervisor) {
        this.lstSupervisor = lstSupervisor;
    }

    public int getValorSupervisor() {
        return valorSupervisor;
    }

    public void setValorSupervisor(int valorSupervisor) {
        this.valorSupervisor = valorSupervisor;
    }
}

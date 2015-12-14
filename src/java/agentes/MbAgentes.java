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
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;
import utilerias.Dialogs;
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
    @ManagedProperty(value = "#{mbAgentes}")
    private MbContribuyentes mbContribuyente = new MbContribuyentes();
    @ManagedProperty(value = "#{mbDireccion}")
    private MbDireccion mbDireccion;
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
    private boolean contribuyenteExistente = false;

    public MbAgentes() {
        this.mbDireccion = new MbDireccion();
        this.mbContribuyente = new MbContribuyentes();
        titleCancelar = "Cancelar Contacto";
        lblCancelar = "ui-icon-arrowreturnthick-1-w";
        lblnuevoAgente = "ui-icon-disk";
        lblNuevaDireccionAgente = "ui-icon-disk";
    }

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

    public void buscarAsentamientos() {
//        mbDireccion.setDireccion(mbContribuyente.getContribuyente().getDireccion());
        mbDireccion.buscarAsentamientos();
    }

    public void buscar() {
        Contribuyente contribuyente;
        agente.getContacto().setCorreo("");
        String rfc = mbContribuyente.getContribuyente().getRfc().toUpperCase().trim();
        if (rfc.equals("")) {
            Mensajes.MensajeAlertP("Escriba primero el rfc del contribuyente");
        } else if (rfc.length() == 12 || rfc.length() == 13) {
            try {
                Utilerias utilerias = new Utilerias();
                String error = utilerias.verificarRfc(mbContribuyente.getContribuyente().getRfc().toUpperCase());
                if (error.equals("")) {
                    DAOContribuyentes dao1 = new DAOContribuyentes();
                    contribuyente = dao1.buscarContribuyente(rfc);
                    if (contribuyente != null) {
                        mbContribuyente.copia(contribuyente);
                        mbContribuyente.setContribuyente(contribuyente);
                        this.mbContribuyente.getContribuyente().getContribuyente();

                        if (actualizar == 1) {
                            contribuyenteExistente = false;
                        } else {
                            contribuyenteExistente = true;
                        }
                    } else {
                        mbDireccion.setDireccion(new Direccion());
                        agente.getContribuyente().setDireccion(mbContribuyente.getContribuyente().getDireccion());
                    }
                    Dialogs.abrirDialogo("dlgContribuyente");
                } else {
                    Mensajes.mensajeAlert(error);
                }
            } catch (NamingException | SQLException ex) {
                Mensajes.mensajeError(ex.getMessage());
                Logger.getLogger(MbAgentes.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NullPointerException ex) {
                mbContribuyente.setContribuyente(new Contribuyente());
            }
        } else {
            Mensajes.mensajeAlert("Error! la longitud del rfc no es correcta");
        }
    }

    public void buscarDireccion() {
        mbDireccion.getDireccion().setCodigoPostal(mbContribuyente.getContribuyente().getDireccion().getCodigoPostal());
        mbDireccion.buscarAsentamientos();

    }

    public void agregarNuevoAgente() {
        boolean ok = false;
        ok = validarAgente();
        Contribuyente c = null;
        if (ok == true) {
            DAOContribuyentes daoContribuyente;
            try {
                daoContribuyente = new DAOContribuyentes();
                c = daoContribuyente.buscarContribuyente(mbContribuyente.getContribuyente().getRfc().toUpperCase());
            } catch (NamingException ex) {
                Logger.getLogger(MbAgentes.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getMessage());
                Logger.getLogger(MbAgentes.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (c == null) {
                if (mbContribuyente.getContribuyente().getContribuyente().equals("")) {
                    Mensajes.MensajeAlertP("No existe este contribuyente darlo de alta primero");
                    mbContribuyente.getContribuyente().setDireccion(new Direccion());
                    Dialogs.abrirDialogo("dlgContribuyente");
                } else {
                    guardarAgente(ok);
                }
            } else {
                guardarAgente(ok);
            }

        }
    }

    public void guardarAgente(boolean ok) {
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = null;
        try {
            listaAgente = null;
            DaoAgentes daoAgente = new DaoAgentes();
            if (actualizar == 0) {
                boolean okExito = false;
                agente.setNivel(valorEnum);
//                agente.setSuperior(valorSupervisor);
                agente.setContribuyente(mbContribuyente.getContribuyente());
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
                    Mensajes.MensajeAlertP("Este codigo del agente ya esta asignado");
//                    Mensajes.mensajeAlert("Este Contribuyente ya esta dado de alta. Implemente el buscador");
                    break;
                default:
                    Mensajes.mensajeError(ex.getMessage());
                    break;
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
        if (mbContribuyente.getContribuyente().getRfc().equals("")) {
            Mensajes.MensajeAlertP("Se requiere un Rfc");
        } else if (agente.getCodigo() == 0) {
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
        } else if (agente.getDireccionAgente().getCalle().equals("")) {
            fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
            fMsg.setDetail("Se requiere un Direccion!!");
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
            ArrayList<SelectItem> lst = new ArrayList<>();
            mbDireccion.setListaAsentamientos(lst);
        }
    }

    public void respaldoDireccionAgente() {
        mbDireccion.setDireccion(new Direccion());
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
            ArrayList<SelectItem> lst = new ArrayList<>();
            mbDireccion.setListaAsentamientos(lst);
        }
    }

    public void direccionContirbuyente() {
        mbDireccion.setDireccion(mbContribuyente.getContribuyente().getDireccion());
//        mbDireccion.setDireccion(agente.getContribuyente().getDireccion());
        flgDireccion = 1;
    }

    public void validarDireccion() {
//        mbDireccion.setDireccion(new Direccion());
//        mbDireccion.setDireccion(mbContribuyente.getContribuyente().getDireccion());
        boolean paso = mbDireccion.validarDireccion();
        if (paso == true) {
            if (flgDireccion == 1) {
                agente.getContribuyente().setDireccion(mbDireccion.getDireccion());
            } else if (flgDireccion == 2) {
                agente.setDireccionAgente(mbDireccion.getDireccion());
            }
            mbDireccion.setDireccion(new Direccion());
        }
        if (this.getActualizar() == 1 && flgDireccion == 2) {
            DaoAgentes dao = new DaoAgentes();
            try {
                dao.actualizarDireccion(agente.getDireccionAgente(), agente.getIdAgente());
                Mensajes.mensajeSucces("Direccon del agente actualizada");
            } catch (SQLException ex) {
                Mensajes.MensajeErrorP(ex.getMessage());
                Logger.getLogger(MbAgentes.class.getName()).log(Level.SEVERE, null, ex);
            }
            mbDireccion.setDireccion(new Direccion());
        } else if (this.getActualizar() == 1 && flgDireccion == 1) {
            DaoAgentes dao = new DaoAgentes();
            try {
                //DAODireccion daoDireccion = new DAODireccion();
                dao.actualizarDireccion(agente.getContribuyente().getDireccion(), agente.getIdAgente());
                Mensajes.mensajeSucces("Direccion del contribuyente actualizada");
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getMessage());
                Logger.getLogger(MbAgentes.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

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
//            ArrayList<Telefono> telefono = new ArrayList<Telefono>();
            this.agente.setCodigo(seleccionListaAgente.getCodigo());
            this.agente.setAgente(seleccionListaAgente.getAgente());
            this.agente.getMiniCedis().setIdCedis(seleccionListaAgente.getMiniCedis().getIdCedis());
            this.agente.setIdAgente(seleccionListaAgente.getIdAgente());
            this.agente.getContribuyente().setRfc(seleccionListaAgente.getContribuyente().getRfc());
            this.agente.getContribuyente().setIdContribuyente(seleccionListaAgente.getContribuyente().getIdContribuyente());
//            DAOTelefonos telefonos = new DAOTelefonos();
            obtenerDireccion();
            this.agente.getContacto().setCorreo(seleccionListaAgente.getContacto().getCorreo());
            this.agente.getContacto().setIdContacto(seleccionListaAgente.getContacto().getIdContacto());
            valorEnum = seleccionListaAgente.getNivel();
            if (agente.getContribuyente().getIdContribuyente() == 0) {
                mbContribuyente.getContribuyente().setRfc("");
            } else {
                DAOContribuyentes daoC = new DAOContribuyentes();
                mbContribuyente.setContribuyente(daoC.obtenerContribuyente(seleccionListaAgente.getContribuyente().getIdContribuyente()));
            }
            valorSupervisor = seleccionListaAgente.getSuperior();
//            try {
//                telefono = telefonos.obtenerTelefonos(seleccionListaAgente.getContacto().getIdContacto());
//                TelefonoTipo t0 = new TelefonoTipo(false);
//                t0.setTipo("Nuevo Tipo");
//                t0.setIdTipo(0);
//                listaTelefonos = new ArrayList<SelectItem>();
//                listaTelefonos.add(new SelectItem(t0, t0.toString()));
//                for (Telefono t : telefono) {
//                    listaTelefonos.add(new SelectItem(t, t.toString()));
//                }
//            } catch (SQLException ex) {
//                Mensajes.mensajeError(ex.getMessage());
//            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getMessage());
            Logger
                    .getLogger(MbAgentes.class
                            .getName()).log(Level.SEVERE, null, ex);
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
                    Logger
                            .getLogger(MbAgentes.class
                                    .getName()).log(Level.SEVERE, null, ex);
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
                Logger
                        .getLogger(MbAgentes.class
                                .getName()).log(Level.SEVERE, null, ex);
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
//            mbContactos.getCorreo().setIdContacto(0);
            this.getMbContactos().getMbTelefonos().cargaTelefonos(mbContactos.getContacto().getIdContacto());
            this.getMbContactos().obtenerCorreo(mbContactos.getContacto().getIdContacto());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
            Logger
                    .getLogger(MbAgentes.class
                            .getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getMessage());
            Logger
                    .getLogger(MbAgentes.class
                            .getName()).log(Level.SEVERE, null, ex);
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
            Logger
                    .getLogger(MbAgentes.class
                            .getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getMessage());
            Logger
                    .getLogger(MbAgentes.class
                            .getName()).log(Level.SEVERE, null, ex);
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
        this.agente.setCodigo(0);
        this.agente.getContribuyente().setDireccion(new Direccion());
    }

    public void limpiarContriuyente() {
        mbContribuyente.setContribuyente(new Contribuyente());
    }

    public void guardarContribuyente() {
        mbContribuyente.getContribuyente().setDireccion(agente.getContribuyente().getDireccion());
        boolean ok = mbContribuyente.valida();
        if (mbContribuyente.getContribuyente().getDireccion().getCalle().equals("")) {
            Mensajes.mensajeError("Se requiere una direccion");
            ok = false;
        }

        if (ok == true) {
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
                    Logger.getLogger(MbAgentes.class
                            .getName()).log(Level.SEVERE, null, ex);
                } catch (SQLException ex) {
                    Mensajes.mensajeError(ex.getMessage());
                    Logger
                            .getLogger(MbAgentes.class
                                    .getName()).log(Level.SEVERE, null, ex);
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
                    Logger.getLogger(MbAgentes.class
                            .getName()).log(Level.SEVERE, null, ex);
                } catch (SQLException ex) {
                    System.err.println(ex.getMessage());
                    Mensajes.mensajeError(ex.getMessage());
                }
            }
        }
    }

    public void dameContribuyente() {
        try {
            DAOContribuyentes daoContribuyente = new DAOContribuyentes();
            daoContribuyente.obtenerContribuyenteRfc(mbContribuyente.getContribuyente().getRfc().toUpperCase());
            DAODireccion dao = new DAODireccion();
            agente.getContribuyente().setDireccion(dao.obtenerDireccion(mbContribuyente.getContribuyente().getDireccion().getIdDireccion()));

        } catch (NamingException | SQLException ex) {
            Logger.getLogger(MbAgentes.class
                    .getName()).log(Level.SEVERE, null, ex);
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

    public boolean isContribuyenteExistente() {
        return contribuyenteExistente;
    }

    public void setContribuyenteExistente(boolean contribuyenteExistente) {
        this.contribuyenteExistente = contribuyenteExistente;
    }

}

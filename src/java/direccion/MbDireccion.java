package direccion;

import Message.Mensajes;
import direccion.dao.DAOAsentamiento;
import direccion.dao.DAODirecciones;
import direccion.dao.DAOPais;
import direccion.dominio.Asentamiento;
import direccion.dominio.Direccion;
import direccion.dominio.Pais;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;

/**
 *
 * @author Julio
 */
@ManagedBean(name = "mbDireccion")
@SessionScoped
public class MbDireccion implements Serializable {

    private Direccion direccion = new Direccion();
    private Direccion respaldo = new Direccion();
    private ArrayList<SelectItem> listaPaises = null;
    private ArrayList<SelectItem> listaAsentamientos;
    private boolean editarAsentamiento;
    private Asentamiento selAsentamiento;
    private DAODirecciones dao;
    private String llama;
    private String iconSearch;
    private String iconSearchTitle;
    private String actualiza;

    public MbDireccion() {
        this.direccion = new Direccion();
        this.editarAsentamiento = true;
    }

    public void limpiarDireccion() {
        direccion = new Direccion();
    }

    public void actualizarDireccion() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        if (this.validarDireccion()) {
            this.actualizaDireccion();
            Mensajes.mensajeSucces("La dirección se grabó correctamente !!");
            ok = true;
        }
        context.addCallbackParam("okDireccion", ok);
    }

    public int agregar(Direccion d) {
        int idDireccion = 0;
        try {
            if (this.direccion.getIdDireccion() == 0) {
                this.dao = new DAODirecciones();
                idDireccion = this.dao.agregar(d);
            } else {
                Mensajes.mensajeError("La direccion no se puede agregar, ya tiene ** Id **");
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        return idDireccion;
    }

    public void grabar() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        if (this.valida(this.direccion)) {
            try {
                if (this.direccion.getIdDireccion() != 0) {
                    this.dao = new DAODirecciones();
                    this.dao.modificar(this.direccion);
                }
                this.actualizaDireccion();
                //Mensajes.mensajeSucces("La dirección se grabó correctamente !!");
                ok = true;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
        context.addCallbackParam("okDireccion", ok);
    }

    public boolean validarDireccion(Direccion d) {
        RequestContext context = RequestContext.getCurrentInstance();
        boolean ok = this.valida(d);
        context.addCallbackParam("okDireccion", ok);
        return ok;
    }

    private boolean valida(Direccion d) {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if (d.getCalle().isEmpty()) {
            fMsg.setDetail("Se requiere la calle !!");
        } else if (d.getNumeroExterior().isEmpty()) {
            fMsg.setDetail("Se requiere el número exterior !!");
//        } else if (d.getReferencia().isEmpty()) {
//            fMsg.setDetail("Se requiere la referencia !!");
        } else if (d.getPais().getIdPais() == 0) {
            fMsg.setDetail("Se requiere el pais !!");
        } else if (d.getCodigoPostal().isEmpty()) {
            fMsg.setDetail("Se requiere el códigoPostal");
        } else if (d.getCodigoPostal().length() < 5) {
            fMsg.setDetail("Verifique la longitud de su codigo postal");
        } else if (d.getLocalidad().equals("")) {
            fMsg.setDetail("Se requiere una localidad");
        } else if (d.getMunicipio().equals("")) {
            fMsg.setDetail("Se requiere un municipioi");
        } else if (d.getEstado().isEmpty()) {
            fMsg.setDetail("Se requiere el estado");
        } else if (d.getMunicipio().isEmpty()) {
            fMsg.setDetail("Se requiere el municipio");
        } else {
            ok = true;
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        return ok;
    }

    public boolean validarDireccion() {
        RequestContext context = RequestContext.getCurrentInstance();
        boolean ok = this.valida(this.direccion);
        context.addCallbackParam("okDireccion", ok);
        return ok;
    }

    public void eliminar(int idDireccion) {
        try {
            this.dao = new DAODirecciones();
            this.dao.eliminar(idDireccion);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public String salir() {
        this.listaPaises = null;
        return this.llama + ".mantenimiento";
    }

    public void mttoDireccion(String actualiza) {
        this.setEditarAsentamiento(true);
        this.iconSearch = "ui-icon-search";
        this.iconSearchTitle = "Buscar Colonias";
        this.actualiza = actualiza;
    }

    public void mttoDireccionDlg(Direccion direccion, String actualiza) {
        this.setEditarAsentamiento(true);
        this.iconSearch = "ui-icon-search";
        this.iconSearchTitle = "Buscar Colonias";
        this.actualiza = actualiza;
        this.copiaDireccion(direccion);
        this.respaldo = direccion;
    }

    public String mttoDireccionNavega(Direccion direccion, String llama) {
        this.mttoDireccionDlg(direccion, "");
        this.llama = llama;
        return "direccion.mantenimiento";
    }

    private void copiaDireccion(Direccion direccion) {
        this.direccion = new Direccion();
        this.direccion.setIdDireccion(direccion.getIdDireccion());
        this.direccion.setCalle(direccion.getCalle());
        this.direccion.setNumeroExterior(direccion.getNumeroExterior());
        this.direccion.setNumeroInterior(direccion.getNumeroInterior());
        this.direccion.setReferencia(direccion.getReferencia());
        this.direccion.getPais().setIdPais(direccion.getPais().getIdPais());
        this.direccion.getPais().setPais(direccion.getPais().getPais());
        this.direccion.setCodigoPostal(direccion.getCodigoPostal());
        this.direccion.setEstado(direccion.getEstado());
        this.direccion.setMunicipio(direccion.getMunicipio());
        this.direccion.setLocalidad(direccion.getLocalidad());
        this.direccion.setColonia(direccion.getColonia());
        this.direccion.setNumeroLocalizacion(direccion.getNumeroLocalizacion());
    }

    private void actualizaDireccion() {
        this.respaldo.setIdDireccion(this.direccion.getIdDireccion());
        this.respaldo.setCalle(this.direccion.getCalle());
        this.respaldo.setNumeroExterior(this.direccion.getNumeroExterior());
        this.respaldo.setNumeroInterior(this.direccion.getNumeroInterior());
        this.respaldo.setReferencia(this.direccion.getReferencia());
        this.respaldo.setPais(this.direccion.getPais());
        this.respaldo.getPais().setIdPais(this.direccion.getPais().getIdPais());
        this.respaldo.getPais().setPais(this.direccion.getPais().getPais());
        this.respaldo.setCodigoPostal(this.direccion.getCodigoPostal());
        this.respaldo.setEstado(this.direccion.getEstado());
        this.respaldo.setMunicipio(this.direccion.getMunicipio());
        this.respaldo.setLocalidad(this.direccion.getLocalidad());
        this.respaldo.setColonia(this.direccion.getColonia());
        this.respaldo.setNumeroLocalizacion(this.direccion.getNumeroLocalizacion());
    }

    public Direccion nuevaDireccion() {
        return new Direccion();
    }

    public Direccion obtener(int idDireccion) {
        Direccion dir = null;
        try {
            if (idDireccion == 0) {
                dir = new Direccion();
            } else {
                this.dao = new DAODirecciones();
                dir = this.dao.obtener(idDireccion);
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        return dir;
    }

    private ArrayList<SelectItem> obtenerAsentamientos(String codigoPostal) throws NamingException, SQLException {
        ArrayList<SelectItem> asentamientos = new ArrayList<>();
//        try {
        Asentamiento a0 = new Asentamiento();
//            a0.setCodAsentamiento("0");
//            a0.setCodigoPostal("");
//            a0.setTipo("");
//            a0.setAsentamiento("");
//            a0.setCodEstado("");
//            a0.setEstado("");
//            a0.setCodMunicipio("");
//            a0.setMunicipio("");
//            a0.setCiudad("");
//            SelectItem cero = new SelectItem(a0, "Seleccione un asentamiento");
        asentamientos.add(new SelectItem(a0, "Seleccione un asentamiento"));

        DAOAsentamiento daoAsenta = new DAOAsentamiento();
//            Asentamiento[] aAsentamientos = daoAsenta.obtenerAsentamientos(codigoPostal);
        for (Asentamiento a : daoAsenta.obtenerAsentamientos(codigoPostal)) {
            asentamientos.add(new SelectItem(a, a.getTipo() + " " + a.getAsentamiento()));
        }
//        } catch (NamingException ex) {
//            Mensajes.mensajeError(ex.getMessage());
//        } catch (SQLException ex) {
//            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
//        }
        return asentamientos;
    }

    public void buscarAsentamientos() {
        editarAsentamiento = true;
        System.out.println("--------------------------------");
        System.out.println("entro a buscar el asentamiento");
        System.out.println("--------------------------------");

        System.out.println(direccion.getCodigoPostal());
        if (this.editarAsentamiento) {
            String codigoPostal = this.direccion.getCodigoPostal();
            if (!codigoPostal.isEmpty() && this.direccion.getPais().getIdPais() == 1) {
                try {
                    this.listaAsentamientos = obtenerAsentamientos(codigoPostal);
                    this.editarAsentamiento = false;
                    this.iconSearch = "ui-icon-pencil";
                    this.iconSearchTitle = "Editar Colonia";
                } catch (SQLException ex) {
                    Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
                } catch (NamingException ex) {
                    Mensajes.mensajeError(ex.getMessage());
                }
            }
        } else {
            this.editarAsentamiento = true;
            this.iconSearch = "ui-icon-search";
            this.iconSearchTitle = "Buscar Colonias";
        }
        editarAsentamiento = false;
    }

    public List<SelectItem> getListaAsentamientos() {
        return listaAsentamientos;
    }

    public void setListaAsentamientos(ArrayList<SelectItem> listaAsentamientos) {
        this.listaAsentamientos = listaAsentamientos;
    }

    public void actualizaAsentamiento() {

        Asentamiento nuevo = this.direccion.getSelAsentamiento();
        String[] localidades = {"08", "15", "18", "20", "23", "24", "25", "26", "27", "28", "29", "32"};

        if (nuevo.getCodAsentamiento().equals("0")) {
            this.direccion.setEstado("");
            this.direccion.setMunicipio("");
            this.direccion.setLocalidad("");
            this.direccion.setColonia("");
        } else {
            this.direccion.setEstado(nuevo.getEstado());
            this.direccion.setMunicipio(nuevo.getMunicipio());
            if (nuevo.getCiudad().trim().isEmpty()) {
                boolean flag = false;
                for (String s : localidades) {
                    if (s.equals(nuevo.getcTipo())) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    this.direccion.setLocalidad(nuevo.toString());
                    this.direccion.setColonia("");
                } else {
                    this.direccion.setLocalidad("");
                    this.direccion.setColonia(nuevo.toString());
                }
            } else {
                this.direccion.setLocalidad(nuevo.getCiudad().trim());
                this.direccion.setColonia(nuevo.toString());
            }
        }
        this.editarAsentamiento = true;
        this.iconSearch = "ui-icon-search";
        this.iconSearchTitle = "Buscar Colonias";
    }

    public void actualizaAsentamiento2() {
        Asentamiento nuevo = this.getSelAsentamiento();
        String[] localidades = {"08", "15", "18", "20", "23", "24", "25", "26", "27", "28", "29", "32"};

        if (nuevo.getCodAsentamiento().equals("0")) {
            this.direccion.setEstado("");
            this.direccion.setMunicipio("");
            this.direccion.setLocalidad("");
            this.direccion.setColonia("");
        } else {
            this.direccion.setEstado(nuevo.getEstado());
            this.direccion.setMunicipio(nuevo.getMunicipio());
            if (nuevo.getCiudad().trim().isEmpty()) {
                boolean flag = false;
                for (String s : localidades) {
                    if (s.equals(nuevo.getcTipo())) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    this.direccion.setLocalidad(nuevo.toString());
                    this.direccion.setColonia("");
                } else {
                    this.direccion.setLocalidad("");
                    this.direccion.setColonia(nuevo.toString());
                }
            } else {
                this.direccion.setLocalidad(nuevo.getCiudad().trim());
                this.direccion.setColonia(nuevo.toString());
            }
        }
        this.editarAsentamiento = true;
    }

    public boolean isEditarAsentamiento() {
        return editarAsentamiento;
    }

    public void setEditarAsentamiento(boolean editarAsentamiento) {
        if (!editarAsentamiento && (this.listaAsentamientos == null || this.listaAsentamientos.isEmpty())) {
            FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "No hay asentamientos en la lista, proporcione un código postal y de click al botón BUSCAR");
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
            editarAsentamiento = true;
        }
        this.editarAsentamiento = editarAsentamiento;
    }

    public ArrayList<SelectItem> getListaPaises() {
        if (this.listaPaises == null) {
            this.obtenerPaises();
        }
        return this.listaPaises;
    }

    private void obtenerPaises() {
        this.listaPaises = new ArrayList<>();

        Pais p0 = new Pais();
        p0.setIdPais(0);
        p0.setPais("Seleccione un país");
        this.listaPaises.add(new SelectItem(p0, p0.getPais()));

        DAOPais daoPais;
        try {
            daoPais = new DAOPais();
            for (Pais p : daoPais.obtenerPaises()) {
                this.listaPaises.add(new SelectItem(p, p.getPais()));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void cancelarDireccion() {
//        this.direccion = new Direccion();
//        direccion.setMunicipio("0");
        editarAsentamiento = true;
//        selAsentamiento = new Asentamiento();
    }

    public void setListaPaises(ArrayList<SelectItem> listaPaises) {
        this.listaPaises = listaPaises;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }

    public Asentamiento getSelAsentamiento() {
        return selAsentamiento;
    }

    public void setSelAsentamiento(Asentamiento selAsentamiento) {
        this.selAsentamiento = selAsentamiento;
    }

    public String getIconSearch() {
        return iconSearch;
    }

    public void setIconSearch(String iconSearch) {
        this.iconSearch = iconSearch;
    }

    public String getIconSearchTitle() {
        return iconSearchTitle;
    }

    public void setIconSearchTitle(String iconSearchTitle) {
        this.iconSearchTitle = iconSearchTitle;
    }

    public String getActualiza() {
        return actualiza;
    }

    public void setActualiza(String actualiza) {
        this.actualiza = actualiza;
    }
}

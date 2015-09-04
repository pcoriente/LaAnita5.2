package empresas;

import direccion.MbDireccion;
import empresas.dao.DAOEmpresas;
import empresas.dominio.Empresa;
import empresas.to.TOEmpresa;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import utilerias.Utilerias;

@ManagedBean(name = "mbEmpresas")
@SessionScoped
public class MbEmpresas implements Serializable{

    private Empresa empresa;
    private ArrayList<Empresa> listaEmpresas;
    @ManagedProperty(value = "#{mbDireccion}")
    private MbDireccion mbDireccion;
    private DAOEmpresas dao;
    private Date date;
    private String comenta;
    private ArrayList<SelectItem> listaObtenerEmpresas;

    public MbEmpresas() {
        try {
            this.dao = new DAOEmpresas();
        } catch (NamingException e) {
            System.err.println(e);
        }
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public MbDireccion getMbDireccion() {
        return mbDireccion;
    }

    public void setMbDireccion(MbDireccion mbDireccion) {
        this.mbDireccion = mbDireccion;
    }

    public void setListaEmpresas(ArrayList<Empresa> listaEmpresas) {
        this.listaEmpresas = listaEmpresas;

    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getComenta() {
        return comenta;
    }

    public void setComenta(String comenta) {
        this.comenta = comenta;
    }

    public ArrayList<SelectItem> getListaObtenerEmpresas() throws NamingException {
        if (listaObtenerEmpresas == null) {
            try {
                listaObtenerEmpresas = obtenerListaEmpresas();
            } catch (SQLException ex) {
                Logger.getLogger(MbEmpresas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return listaObtenerEmpresas;
    }

    public void setListaObtenerEmpresas(ArrayList<SelectItem> listaObtenerEmpresas) {
        this.listaObtenerEmpresas = listaObtenerEmpresas;

    }

    //////////////////// M E T O D O S  ////////////
    public ArrayList<Empresa> getListaEmpresas() throws NamingException {
        try {
            if (listaEmpresas == null) {
                cargaEmpresas();
            }
        } catch (SQLException ex) {
            Logger.getLogger(MbEmpresas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listaEmpresas;
    }

    private void cargaEmpresas() throws SQLException, NamingException {
        listaEmpresas = new ArrayList<Empresa>();
        ArrayList<TOEmpresa> toLista = dao.obtenerEmpresa();

        for (TOEmpresa e : toLista) {
            listaEmpresas.add(convertir(e));
        }
    }

    private Empresa convertir(TOEmpresa to) {
        Empresa emp = new Empresa();
        emp.setIdEmpresa(to.getIdEmpresa());
        emp.setCodigoEmpresa(to.getCodigoEmpresa());
        emp.setEmpresa(to.getEmpresa());
        emp.setNombreComercial(to.getNombreComercial());
        emp.setRfc(to.getRfc());
        emp.setTelefono(to.getTelefono());
        emp.setFax(to.getFax());
        emp.setCorreo(to.geteMail());
        emp.setRepresentanteLegal(to.getRepresentanteLegal());
        emp.setDireccion(this.mbDireccion.obtener(to.getIdDireccion()));
        return emp;
    }

    public String mantenimiento(int idEmpresa) throws SQLException {
        String destino = "empresa.mantenimiento";

        System.out.println("Id de la Empresa: " + idEmpresa);
        try {
            if (idEmpresa == 0) {
                this.empresa = nuevoEmpresa();
            } else {
                TOEmpresa toEmpresa = this.dao.obtenerEmpresa(idEmpresa);
                if (toEmpresa == null) {
                    destino = null;
                } else {
                    this.empresa = convertir(toEmpresa);
                }
            }
        } catch (SQLException ex) {
            destino = null;
            Logger.getLogger(MbEmpresas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return destino;
    }

    private Empresa nuevoEmpresa() throws SQLException {
        Empresa e = new Empresa();
        int ultimo = this.dao.ultimoEmpresa();
        e.setIdEmpresa(0);
        e.setCodigoEmpresa(ultimo + 1);
        e.setEmpresa("");
        e.setNombreComercial("");
        e.setRfc("");
        e.setTelefono("");
        e.setFax("");
        e.setCorreo("");
        e.setRepresentanteLegal("");
        e.setDireccion(this.mbDireccion.nuevaDireccion());
        return e;
    }

    public void grabar() {
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        int codigo = this.empresa.getCodigoEmpresa();
        String strEmpresa = Utilerias.Acentos(this.empresa.getEmpresa());
        String nombreComercial = Utilerias.Acentos(this.empresa.getNombreComercial());
        String rfc = this.empresa.getRfc();
        String telefono = this.empresa.getTelefono();
        String fax = this.empresa.getFax();
        String correo = this.empresa.getCorreo();
        String representanteLegal = Utilerias.Acentos(this.empresa.getRepresentanteLegal());
        int idDireccion = this.empresa.getDireccion().getIdDireccion();

        if (strEmpresa.isEmpty()) {
            fMsg.setDetail("Se requiere el nombre de la EMPRESA ??????");
        } else if (nombreComercial.isEmpty()) {
            fMsg.setDetail("Se requiere el Nombre Comercial");
        } else if (rfc.isEmpty()) {
            fMsg.setDetail("Se requiere el RFC");
        } else if (telefono.isEmpty()) {
            fMsg.setDetail("Se requiere el número de telefono");
        } else if (fax.isEmpty()) {
            fMsg.setDetail("Se requiere el número de fax");
        } else if (correo.isEmpty()) {
            fMsg.setDetail("Se requiere el correo electrónico");
        } else if (representanteLegal.isEmpty()) {
            fMsg.setDetail("Se requiere el representante legal");
        } else if (idDireccion == 0) {
            fMsg.setDetail("Se requiere la DIRECCION de la Empresa");
        } else {
            try {
                int idEmpresa = this.empresa.getIdEmpresa();
                if (idEmpresa == 0) {
                    idEmpresa = this.dao.agregar(codigo, strEmpresa, nombreComercial, rfc, telefono, fax, correo, representanteLegal, idDireccion);
                } else {
                    this.dao.modificar(this.empresa.getIdEmpresa(), strEmpresa, nombreComercial, rfc, telefono, fax, correo, representanteLegal, idDireccion);
                }
                this.empresa = this.convertir(this.dao.obtenerEmpresa(idEmpresa));
                this.listaEmpresas = null;
                fMsg.setSeverity(FacesMessage.SEVERITY_INFO);
                fMsg.setDetail("La EMPRESA se grabó correctamente !!");
            } catch (SQLException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
                Logger.getLogger(MbEmpresas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
    }

    public String salir() {

        if (this.empresa.getIdEmpresa() == 0 && this.empresa.getDireccion().getIdDireccion() > 0) {
            mbDireccion.eliminar(this.empresa.getDireccion().getIdDireccion());
        }
        return "empresa.salir";
    }

    public String terminar() {
        return "menuEmpresa.terminar";
    }

    //DAVID
    public ArrayList<SelectItem> obtenerListaEmpresas() throws SQLException, NamingException {
        ArrayList<SelectItem> listaComboEmpresas = new ArrayList<>();

        try {
            Empresa e0 = new Empresa();
            e0.setIdEmpresa(0);
            e0.setCodigoEmpresa(0);
            e0.setNombreComercial("Seleccione Empresa");
            listaComboEmpresas.add(new SelectItem(e0, e0.toString()));

            ArrayList<Empresa> empresas = this.dao.obtenerComboEmpresa();
            for (Empresa e : empresas) {
                listaComboEmpresas.add(new SelectItem(e, e.getNombreComercial()));
            }
        } catch (SQLException e) {
            Logger.getLogger(MbEmpresas.class.getName()).log(Level.SEVERE, null, e);
        }
        return listaComboEmpresas;
    }

}
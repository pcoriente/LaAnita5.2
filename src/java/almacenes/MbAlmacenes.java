package almacenes;

import almacenes.dao.DAOAlmacenes;
import almacenes.dominio.Almacen;
import cedis.MbMiniCedis;
import cedis.dominio.MiniCedis;
import contactos.MbContactos;
import contactos.dominio.Contacto;
import contactos.dominio.Telefono;
import contactos.dominio.TelefonoTipo;
import direccion.MbDireccion;
import empresas.MbMiniEmpresas;
import empresas.dominio.MiniEmpresa;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jsolis
 */
@Named(value = "mbAlmacenes")
@SessionScoped
public class MbAlmacenes implements Serializable {
    private int idTipo=2;
    private boolean modoEdicion;
//    private String tipoDireccion;
//    private int idEmpresa;
//    private int idCedis;
    private Almacen almacen;
    private Almacen almacenSeleccionado;
    private ArrayList<Almacen> almacenes;
    private ArrayList<SelectItem> listaAlmacenes;
    private DAOAlmacenes dao;
    
    private Contacto contacto;  // Contacto Seleccionado del SelectOne
    private Telefono telefono;  // Telefono Seleccionado del SelectOne
    
    @ManagedProperty(value = "#{mbMiniCedis}")
    private MbMiniCedis mbCedis;
    @ManagedProperty(value = "#{mbMiniEmpresas}")
    private MbMiniEmpresas mbEmpresas;
    @ManagedProperty(value = "#{mbDireccion}")
    private MbDireccion mbDireccion;
    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value="#{mbContactos}")
    private MbContactos mbContactos;

    public MbAlmacenes() throws NamingException {
        this.modoEdicion = false;
        this.almacen = new Almacen(0, 0);

        this.mbCedis = new MbMiniCedis();
        this.mbEmpresas = new MbMiniEmpresas();
        this.mbDireccion = new MbDireccion();
        this.mbAcciones = new MbAcciones();
        this.mbContactos=new MbContactos();
    }
    
    public void obtenerListaAlmacenes() {
        boolean ok=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        this.listaAlmacenes=new ArrayList<SelectItem>();
        try {
            Almacen a0 = new Almacen();
            a0.setIdAlmacen(0);
            a0.setAlmacen("Seleccione un almacén");
            SelectItem cero = new SelectItem(a0, a0.toString());
            listaAlmacenes.add(cero);
            
            this.dao=new DAOAlmacenes();
            for (Almacen a : this.dao.obtenerAlmacenes(this.mbCedis.getCedis().getIdCedis(), this.mbEmpresas.getEmpresa().getIdEmpresa())) {
                listaAlmacenes.add(new SelectItem(a, a.toString()));
            }
            ok=true;
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
    
    public void eliminarTelefonoTipo() {
        if(this.mbContactos.getMbTelefonos().eliminarTipo(this.mbContactos.getMbTelefonos().getTipo().getIdTipo())) {
            this.mbContactos.getMbTelefonos().getTelefono().setTipo(new TelefonoTipo(this.mbContactos.getMbTelefonos().isCelular()));
            this.mbContactos.getMbTelefonos().cargaTipos();
        }
    }
    
    public void grabarTelefonoTipo() {
        if(this.mbContactos.getMbTelefonos().grabarTipo()) {
            this.mbContactos.getMbTelefonos().getTelefono().setTipo(this.mbContactos.getMbTelefonos().getTipo());
            this.mbContactos.getMbTelefonos().cargaTipos();
        }
    }
    
    public void mttoTelefonoTipo() {
        if(this.mbContactos.getMbTelefonos().getTelefono().getTipo().getIdTipo()==0) {
            this.mbContactos.getMbTelefonos().setTipo(new TelefonoTipo(this.mbContactos.getMbTelefonos().isCelular()));
        } else {
            this.mbContactos.getMbTelefonos().setTipo(this.mbContactos.getMbTelefonos().copiaTipo(this.mbContactos.getMbTelefonos().getTelefono().getTipo()));
        }
    }
    
    public void eliminarTelefono() {
        if(this.mbContactos.getMbTelefonos().eliminar(this.mbContactos.getMbTelefonos().getTelefono().getIdTelefono())) {
            this.telefono=new Telefono();
            this.mbContactos.getMbTelefonos().cargaTelefonos(this.contacto.getIdContacto());
        }
    }
    
    public void grabarTelefono() {
        if(this.mbContactos.getMbTelefonos().grabar(this.contacto.getIdContacto())) {
            this.telefono=this.mbContactos.getMbTelefonos().getTelefono();
            this.mbContactos.getMbTelefonos().cargaTelefonos(this.contacto.getIdContacto());
        }
    }
    
    public void mttoTelefono() {
        if(this.telefono.getIdTelefono()==0) {
            this.mbContactos.getMbTelefonos().setTelefono(new Telefono());
        } else {
            this.mbContactos.getMbTelefonos().setTelefono(this.mbContactos.getMbTelefonos().copia(this.telefono));
        }
        this.mbContactos.getMbTelefonos().setCelular(this.mbContactos.getMbTelefonos().getTelefono().getTipo().isCelular());
        this.mbContactos.getMbTelefonos().cargaTipos();
    }
    
    public void cargarTelefonos() {
        this.mbContactos.getMbTelefonos().cargaTelefonos(this.contacto.getIdContacto());
    }
    
    public void eliminarContacto() {
        if(this.mbContactos.eliminar(this.contacto.getIdContacto())) {
            this.contacto=new Contacto();
            this.mbContactos.cargaContactos(this.idTipo, this.almacen.getIdAlmacen());
        }
    }
    
    public void grabarContacto() {
        if(this.mbContactos.grabar(this.idTipo, this.almacen.getIdAlmacen())) {
            this.contacto=this.mbContactos.getContacto();
            this.mbContactos.cargaContactos(this.idTipo, this.almacen.getIdAlmacen());
        }
    }
    
    public void mttoContacto() {
        if(this.contacto.getIdContacto()==0) {
            this.mbContactos.setContacto(new Contacto());
        } else {
            this.mbContactos.setContacto(this.mbContactos.copia(this.contacto));
        }
    }

//    public void mttoDireccion(String tipoDireccion) {
//        this.tipoDireccion = tipoDireccion;
//        this.mbDireccion.mttoDireccion(this.almacen.getDireccion());
//    }

//    public void grabarDireccion() {
//        if (this.mbDireccion.grabar2()) {
//            if (this.tipoDireccion.equals("direccion")) {
//                this.almacen.setDireccion(this.mbDireccion.getDireccion());
//            }
//        }
//    }

    public void grabarAlmacen() {
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if(this.almacen.getAlmacen().equals("")) {
            fMsg.setDetail("Se requiere el nombre del almacén !!");
        } else if(this.almacen.getDireccion().getIdDireccion()==0) {
            fMsg.setDetail("Se requiere la dirección del almacén !!");
        } else {
            try {
                this.dao=new DAOAlmacenes();
                if (this.almacen.getIdAlmacen() == 0) {
                    this.almacen.setIdAlmacen(this.dao.agregar(almacen));
                } else {
                    this.dao.modificar(almacen);
                }
                fMsg.setSeverity(FacesMessage.SEVERITY_INFO);
                fMsg.setDetail("El almacén se grabó correctamente !!");
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

    public void modificarAlmacen() {
        this.modoEdicion = true;
        this.almacen=this.copia(this.almacenSeleccionado);
        this.almacen.setDireccion(this.mbDireccion.obtener(this.almacenSeleccionado.getDireccion().getIdDireccion()));
        this.contacto=new Contacto();
        this.mbContactos.cargaContactos(this.idTipo, this.almacen.getIdAlmacen());
        this.cargarTelefonos();
        //return "almacen.mantenimiento";
    }

    public void nuevoAlmacen() {
        this.modoEdicion = true;
        int idCedis = this.mbCedis.getCedis().getIdCedis();
        int idEmpresa = this.mbEmpresas.getEmpresa().getIdEmpresa();
        this.almacen = new Almacen(idCedis, idEmpresa);
        this.almacen.setDireccion(this.mbDireccion.nuevaDireccion());
        this.contacto=new Contacto();
        this.mbContactos.cargaContactos(this.idTipo, this.almacen.getIdAlmacen());
        this.cargarTelefonos();
        //return "almacen.mantenimiento";
    }

    public String salir() {
        this.modoEdicion = false;
        if (this.almacen.getIdAlmacen() == 0 && this.almacen.getDireccion().getIdDireccion() > 0) {
            mbDireccion.eliminar(this.almacen.getDireccion().getIdDireccion());
        }
        this.almacenes=null;
        this.almacen=null;
        this.almacenSeleccionado=null;
        return "almacenes.xhtml";
    }

//    public void cargaAlmacenes() {
//        if(this.mbCedis.getCedis().getIdCedis()!=0 && this.mbEmpresas.getEmpresa().getIdEmpresa()==0) {
//            this.mbAlmacenes.cargaAlmacenes();
//        }
//    }
    public String terminar() {
        //String outcome = "menuAlmacenes.terminar";
        this.modoEdicion = false;
        this.almacenes=null;
        this.almacenSeleccionado=null;
        this.acciones = null;
        this.mbCedis.setCedis(new MiniCedis());
        this.mbCedis.setListaMiniCedis(null);
        this.mbEmpresas.setEmpresa(new MiniEmpresa());
        this.mbEmpresas.setListaEmpresas(null);
        return "index.xhtml";
    }

    public boolean grabar() {
        boolean ok = true;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if (this.almacen.getAlmacen().isEmpty()) {
            fMsg.setDetail("Se requiere el nombre del almacén !!");
        } else if (this.almacen.getDireccion().getIdDireccion() == 0) {
            fMsg.setDetail("Se requiere la dirección del almacén !!");
        } else {
            try {
                this.dao = new DAOAlmacenes();
                if (this.almacen.getIdAlmacen() == 0) {
                    this.almacen.setIdAlmacen(this.dao.agregar(this.almacen));
                } else {
                    this.dao.modificar(this.almacen);
                }
                ok = false;
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
        return ok;
    }
    
    public void cargaAlmacenesCedis() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
//            if (this.mbCedis.getCedis().getIdCedis() != 0 && this.mbEmpresas.getEmpresa().getIdEmpresa() != 0) {
                this.dao = new DAOAlmacenes();
                this.almacenes = this.dao.obtenerAlmacenesCedis(this.mbCedis.getCedis().getIdCedis());
//            }
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

    public void cargaAlmacenes() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            if (this.mbCedis.getCedis().getIdCedis() != 0 && this.mbEmpresas.getEmpresa().getIdEmpresa() != 0) {
                this.dao = new DAOAlmacenes();
                this.almacenes = this.dao.obtenerAlmacenes(this.mbCedis.getCedis().getIdCedis(), this.mbEmpresas.getEmpresa().getIdEmpresa());
            }
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

    public Almacen copia(Almacen almacen) {
        Almacen alm = new Almacen(almacen.getIdCedis(), almacen.getIdEmpresa());
        alm.setAlmacen(almacen.getAlmacen());
        alm.setIdAlmacen(almacen.getIdAlmacen());
        alm.setDireccion(almacen.getDireccion());
        return alm;
    }

    public void modificar(Almacen almacen) {
    }
    /*
     public void nuevoAlmacen() {
     this.almacen = new Almacen(this.idCedis, this.idEmpresa);
     }
     */

    public Almacen getAlmacen() {
        return almacen;
    }

    public void setAlmacen(Almacen almacen) {
        this.almacen = almacen;
    }

    public ArrayList<Almacen> getAlmacenes() {
        if (this.almacenes == null) {
            this.cargaAlmacenes();
        }
        return almacenes;
    }

    public void setAlmacenes(ArrayList<Almacen> almacenes) {
        this.almacenes = almacenes;
    }
    /*
     public int getIdEmpresa() {
     return idEmpresa;
     }

     public void setIdEmpresa(int idEmpresa) {
     this.idEmpresa = idEmpresa;
     }

     public int getIdCedis() {
     return idCedis;
     }

     public void setIdCedis(int idCedis) {
     this.idCedis = idCedis;
     }
     * */

    public MbMiniCedis getMbCedis() {
        return mbCedis;
    }

    public void setMbCedis(MbMiniCedis mbCedis) {
        this.mbCedis = mbCedis;
    }

    public MbMiniEmpresas getMbEmpresas() {
        return mbEmpresas;
    }

    public void setMbEmpresas(MbMiniEmpresas mbEmpresas) {
        this.mbEmpresas = mbEmpresas;
    }

    public MbDireccion getMbDireccion() {
        return mbDireccion;
    }

    public void setMbDireccion(MbDireccion mbDireccion) {
        this.mbDireccion = mbDireccion;
    }

    public MbAcciones getMbAcciones() {
        return mbAcciones;
    }

    public void setMbAcciones(MbAcciones mbAcciones) {
        this.mbAcciones = mbAcciones;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(12);
        }
        return acciones;
    }

    public void setAcciones(ArrayList<Accion> acciones) {
        this.acciones = acciones;
    }

    public boolean isModoEdicion() {
        return modoEdicion;
    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
    }

//    public String getTipoDireccion() {
//        return tipoDireccion;
//    }
//
//    public void setTipoDireccion(String tipoDireccion) {
//        this.tipoDireccion = tipoDireccion;
//    }

    public Almacen getAlmacenSeleccionado() {
        return almacenSeleccionado;
    }

    public void setAlmacenSeleccionado(Almacen almacenSeleccionado) {
        this.almacenSeleccionado = almacenSeleccionado;
    }

    public MbContactos getMbContactos() {
        return mbContactos;
    }

    public void setMbContactos(MbContactos mbContactos) {
        this.mbContactos = mbContactos;
    }

    public Contacto getContacto() {
        return contacto;
    }

    public void setContacto(Contacto contacto) {
        this.contacto = contacto;
    }

    public Telefono getTelefono() {
        return telefono;
    }

    public void setTelefono(Telefono telefono) {
        this.telefono = telefono;
    }

    public ArrayList<SelectItem> getListaAlmacenes() {
        if(this.listaAlmacenes==null) {
            this.obtenerListaAlmacenes();
        }
        return listaAlmacenes;
    }

    public void setListaAlmacenes(ArrayList<SelectItem> listaAlmacenes) {
        this.listaAlmacenes = listaAlmacenes;
    }
}

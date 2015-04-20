package cedis;

import cedis.dao.DAOCedis;
import direccion.MbDireccion;
import cedis.dominio.Cedis;
import cedis.to.TOCedis;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import usuarios.MbAcciones;
import utilerias.Utilerias;

/**
 *
 * @author Julio
 */
@ManagedBean(name = "mbCedis")
@SessionScoped
public class MbCedis implements Serializable {
    private int idModulo=1;
    private Cedis cedis;
    @ManagedProperty(value="#{mbDireccion}")
    private MbDireccion mbDireccion;
    @ManagedProperty(value="#{mbAcciones}")
    private MbAcciones mbAcciones;
    private ArrayList<Cedis> listaCedis;
    private ArrayList<Cedis> cedisFiltrados;
    private DAOCedis dao;
    
    public MbCedis() {
        this.mbDireccion=new MbDireccion();
        this.mbAcciones=new MbAcciones();
//        this.mbAcciones.setIdModulo(1);
    }
    
    public String regresarSinAcceso() {
        //this.mbAcciones.setAcciones(null);
        return "menuCedis.terminar";
    }
    
    public String grabar() {
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        
        String destino=null;
        int codigo=this.cedis.getCodigo();
        String strCedis=Utilerias.Acentos(this.cedis.getCedis());
        String telefono=this.cedis.getTelefono();
        String fax=this.cedis.getFax();
        String correo=this.cedis.getCorreo();
        String representante=Utilerias.Acentos(this.cedis.getRepresentante());
        int idDireccion=this.cedis.getDireccion().getIdDireccion();
        
//        if(strCedis.isEmpty()) return destino;
//        else if(correo.isEmpty()) return destino;
//        else if(representante.isEmpty()) return destino;
//        else if(idDireccion == 0) return destino;
        if(idDireccion==0) {
            fMsg.setDetail("Se requiere una direccion");
        } else {
            try {
                this.dao=new DAOCedis();
                int idCedis=this.cedis.getIdCedis();
                if (idCedis == 0) {
                    idCedis=this.dao.agregar(codigo, strCedis, idDireccion, telefono, fax, correo, representante);
                } else {
                    this.dao.modificar(idCedis, strCedis, idDireccion, telefono, fax, correo, representante);
                }
                this.cedis=this.obtenerCedis(idCedis);
                this.listaCedis=null;
                destino="cedis.salir";
            } catch (NamingException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getMessage());
            } catch (SQLException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
        if (destino==null) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        return destino;
    }
    
    public String terminar() {
        this.listaCedis=null;
        return "menuCedis.terminar";
    }
    
    public String cancelar() {
        if (this.cedis.getIdCedis() == 0 && this.cedis.getDireccion().getIdDireccion() > 0) {
            mbDireccion.eliminar(this.cedis.getDireccion().getIdDireccion());
        }
//        this.cedisFiltrados=null;
        return "cedis.salir";
    }
    
    public String mantenimiento(int idCedis) {
        String destino=null;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        
        try {
            this.dao=new DAOCedis();
            if(idCedis == 0) {
                this.cedis=nuevoCedis();
            } else {
                TOCedis toCedis=this.dao.obtenerUnCedis(idCedis);
                if(toCedis != null) { 
                    this.cedis=convertir(toCedis);
                }
            }
            destino="cedis.mantenimiento";
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if (destino==null) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        return destino;
    }
    
    private Cedis nuevoCedis() {
        Cedis c=new Cedis();
        c.setIdCedis(0);
        c.setCedis("");
        c.setDireccion(this.mbDireccion.nuevaDireccion());
        c.setTelefono("");
        c.setFax("");
        c.setCorreo("");
        c.setRepresentante("");
        return c;
    }
    
    public ArrayList<Cedis> getListaCedis() {
        if(listaCedis == null) {
            cargaCedis();
        }
        return listaCedis;
    }
    
    private void cargaCedis() {
        boolean ok=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        
        try {
            listaCedis=new ArrayList<Cedis>();
            this.dao=new DAOCedis();
            ArrayList<TOCedis> toLista=dao.obtenerCedis();
            for(TOCedis c:toLista) {
                listaCedis.add(convertir(c));
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
    
    public Cedis obtenerCedis(int idCedis) throws SQLException {
        Cedis xCedis=convertir(this.dao.obtenerUnCedis(idCedis));
        return xCedis;
    }
    
    private Cedis convertir(TOCedis to) {
        Cedis c=new Cedis();
        c.setIdCedis(to.getIdCedis());
        c.setCodigo(to.getCodigo());
        c.setCedis(to.getCedis());
        c.setDireccion(this.mbDireccion.obtener(to.getIdDireccion()));
        c.setTelefono(to.getTelefono());
        c.setFax(to.getFax());
        c.setCorreo(to.getCorreo());
        c.setRepresentante(to.getRepresentante());
        return c;
    }

    public Cedis getCedis() {
        return cedis;
    }

    public void setCedis(Cedis cedis) {
        this.cedis = cedis;
    }

    public MbDireccion getMbDireccion() {
        return mbDireccion;
    }

    public void setMbDireccion(MbDireccion mbDireccion) {
        this.mbDireccion = mbDireccion;
    }

    public int getIdModulo() {
        return idModulo;
    }

    public MbAcciones getMbAcciones() {
        return mbAcciones;
    }

    public void setMbAcciones(MbAcciones mbAcciones) {
        this.mbAcciones = mbAcciones;
    }

    public ArrayList<Cedis> getCedisFiltrados() {
        return cedisFiltrados;
    }

    public void setCedisFiltrados(ArrayList<Cedis> cedisFiltrados) {
        this.cedisFiltrados = cedisFiltrados;
    }
}
package clasificaciones;

import clasificaciones.dao.DAOClasificacion;
import clasificaciones.dominio.Grupo;
import clasificaciones.dominio.SubGrupo;
import clasificaciones.to.TOSubGrupo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import utilerias.Utilerias;

/**
 *
 * @author Julio
 */
@ManagedBean(name = "mbClasificacion")
@SessionScoped
public class MbClasificacion {
    private int codigoGrupo;
    private SubGrupo subGrupo;
//    private boolean editarGrupo;
    private Grupo grupo;
    private List<SelectItem> listaGrupos;
    private ArrayList<SubGrupo> listaClasificaciones;
    private DAOClasificacion dao;
    
    public MbClasificacion() {
//        this.editarGrupo=false;
        try {
            this.dao=new DAOClasificacion();
        } catch (NamingException ex) {
            Logger.getLogger(MbClasificacion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void grabar() {
        FacesMessage fMsg=new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        
        if(this.subGrupo.getCodigoSubGrupo() == 0) fMsg.setDetail("Se requiere el código del subgrupo");
        if(this.subGrupo.getSubGrupo().trim().isEmpty()) fMsg.setDetail("Se requiere la descripción del subgrupo");
        else if(this.subGrupo.getGrupo().getIdGrupo() == 0) fMsg.setDetail("Se requiere elegir un grupo");
        else {
            try {
                String nombre=Utilerias.Acentos(this.subGrupo.getSubGrupo().trim());
                int idSubGrupo=this.subGrupo.getIdSubGrupo();
                if(idSubGrupo == 0) {
                    idSubGrupo=this.dao.agregar(this.subGrupo.getCodigoSubGrupo(), nombre, this.subGrupo.getGrupo().getIdGrupo());
                } else {
                    this.dao.modificar(this.subGrupo.getIdSubGrupo(), this.subGrupo.getCodigoSubGrupo(), nombre, this.subGrupo.getGrupo().getIdGrupo());
                }
                this.subGrupo=this.convertir(this.dao.obtenerSubGrupo(idSubGrupo));
                fMsg.setSeverity(FacesMessage.SEVERITY_INFO);
                fMsg.setDetail("El subgrupo se grabó correctamente");
            } catch (SQLException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getErrorCode()+" "+ex.getMessage());
            }
        }
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
    }
    
    public String cancelar() {
        try {
            cargaSubGrupos();
        } catch (SQLException ex) {
            Logger.getLogger(MbClasificacion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "clasificaciones.salir";
    }
    
    public String terminar() {
        return "menuClasificaciones.terminar";
    }
    
    public String editarGrupo() {
        int xCodigoGrupo=this.subGrupo.getGrupo().getCodigoGrupo();
        try {
            if(this.subGrupo.getGrupo().getIdGrupo() == 0) xCodigoGrupo=this.dao.obtenerUltimoCodigoGrupo()+1;
            this.grupo=new Grupo(this.subGrupo.getGrupo().getIdGrupo(), xCodigoGrupo, this.subGrupo.getGrupo().getGrupo());
            
        } catch (SQLException ex) {
            Logger.getLogger(MbClasificacion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "grupos.mantenimiento";
    }
    
//    public String grabarGrupo() {
//        String navegar=null;
//        if(this.grupo.getCodigoGrupo() == 0) return navegar;
//        else if(this.grupo.getGrupo().isEmpty()) return navegar;
//        else {
//            try {
//                if(this.grupo.getIdGrupo() == 0) {
//                    this.grupo.setIdGrupo(this.dao.agregarGrupo(this.grupo.getCodigoGrupo(), this.grupo.getGrupo()));
//                } else {
//                    this.dao.modificarGrupo(this.grupo.getIdGrupo(), this.grupo.getCodigoGrupo(), this.grupo.getGrupo());
//                }
//                this.subGrupo.setGrupo(this.grupo);
//                navegar="mantenimiento.clasificaciones";
//            } catch (SQLException ex) {
//                Logger.getLogger(MbClasificacion.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return navegar;
//    }
    
    public void grabarGrupo() {
        FacesMessage fMsg=new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        
        if(this.grupo.getCodigoGrupo() == 0) fMsg.setDetail("Se requiere el código del grupo");
        else if(this.grupo.getGrupo().isEmpty()) fMsg.setDetail("Se requiere la descripción del grupo");
        else {
            try {
                int idGrupo=this.grupo.getIdGrupo();
                if(idGrupo == 0) {
                    idGrupo=this.dao.agregarGrupo(this.grupo.getCodigoGrupo(), this.grupo.getGrupo());
                } else {
                    this.dao.modificarGrupo(this.grupo.getIdGrupo(), this.grupo.getCodigoGrupo(), this.grupo.getGrupo());
                }
                this.codigoGrupo=idGrupo;
                this.obtenerGrupo();
                this.subGrupo.setGrupo(this.grupo);
                fMsg.setSeverity(FacesMessage.SEVERITY_INFO);
                fMsg.setDetail("El grupo se grabó correctamente");
            } catch (SQLException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getErrorCode()+" "+ex.getMessage());
            }
        }
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
    }
    
    public String cancelarGrupo() {
       return "clasificaciones.mantenimiento";
    }

    public Grupo getGrupo() {
        return grupo;
    }
    
    public ArrayList<SubGrupo> getListaClasificaciones() {
        try {
            if(listaClasificaciones == null) cargaSubGrupos();
        } catch (SQLException ex) {
            Logger.getLogger(MbClasificacion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listaClasificaciones;
    }
    
    private void cargaSubGrupos() throws SQLException {
        listaClasificaciones=new ArrayList<SubGrupo>();
        ArrayList<TOSubGrupo> toLista=dao.obtenerSubGrupos();
        for(TOSubGrupo c:toLista) {
            listaClasificaciones.add(convertir(c));
        }
    }
    
    public void setListaCedis(ArrayList<SubGrupo> listaClasificaciones) {
        this.listaClasificaciones = listaClasificaciones;
    }
    
    public String mantenimiento(int idSubGrupo) {
        String destino="clasificaciones.mantenimiento";
        try {
            if(idSubGrupo == 0) {
                this.subGrupo=nuevoSubGrupo();
                int xCodigoSubGrupo=this.dao.obtenerUltimoCodigoSubGrupo()+1;
                this.subGrupo.setCodigoSubGrupo(xCodigoSubGrupo);
            } else {
                TOSubGrupo toSubGrupo=this.dao.obtenerSubGrupo(idSubGrupo);
                if(toSubGrupo == null) destino=null;
                else this.subGrupo=convertir(toSubGrupo);
            }
        } catch (SQLException ex) {
            destino=null;
            Logger.getLogger(MbClasificacion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return destino;
    }
    
    public void actualizarGrupo() {
        int xCodigoGrupo=this.subGrupo.getGrupo().getCodigoGrupo();
        try {
//            if(xCodigoGrupo == 0)
//                this.editarGrupo=true;
//            else {
                int xIdGrupo=this.dao.obtenerIdGrupo(xCodigoGrupo);
                if(xIdGrupo == 0) {
//                    this.editarGrupo=true;
                    this.subGrupo.setGrupo(new Grupo(0, xCodigoGrupo, ""));
                }
                else this.subGrupo.setGrupo(this.dao.obtenerGrupo(xIdGrupo));
//            }
        } catch (SQLException ex) {
            Logger.getLogger(MbClasificacion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void obtenerGrupo() {
        try {
            if(this.getCodigoGrupo() == 0) {
//                this.editarGrupo=true;
            } else {
                Grupo xGrupo=this.dao.obtenerGrupo(this.getCodigoGrupo());
                if(xGrupo != null) 
                    this.subGrupo.setGrupo(xGrupo);
            }
        } catch (SQLException ex) {
            Logger.getLogger(MbClasificacion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private SubGrupo nuevoSubGrupo() {
        SubGrupo subGpo=new SubGrupo();
        subGpo.setIdSubGrupo(0);
        subGpo.setCodigoSubGrupo(0);
        subGpo.setSubGrupo("");
        subGpo.setGrupo(new Grupo(0, 0, ""));
        return subGpo;
    }
    
    private SubGrupo convertir(TOSubGrupo toSubGrupo) throws SQLException {
        SubGrupo clas=new SubGrupo();
        clas.setIdSubGrupo(toSubGrupo.getIdSubGrupo());
        clas.setCodigoSubGrupo(toSubGrupo.getCodigoSubGrupo());
        clas.setSubGrupo(toSubGrupo.getSubGrupo());
        clas.setGrupo(dao.obtenerGrupo(toSubGrupo.getIdGrupo()));
        return clas;
    }

    public int getCodigoGrupo() {
        return codigoGrupo;
    }

    public void setCodigoGrupo(int codigoGrupo) {
        this.codigoGrupo = codigoGrupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }
    
//    public boolean isEditarGrupo() {
//        return editarGrupo;
//    }
//
//    public void setEditarGrupo(boolean editarGrupo) {
//        this.grupo=this.subGrupo.getGrupo();
//        this.editarGrupo = editarGrupo;
//    }
    
    public List<SelectItem> getListaGrupos() {
        if(this.listaGrupos == null) {
            try {
                this.listaGrupos=dao.obtenerGrupos();
            } catch (SQLException ex) {
                Logger.getLogger(MbClasificacion.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return listaGrupos;
    }

    public void setListaGrupos(List<SelectItem> listaGrupos) {
        this.listaGrupos = listaGrupos;
    }

    public SubGrupo getSubGrupo() {
        return subGrupo;
    }

    public void setSubGrupo(SubGrupo subGrupo) {
        this.subGrupo = subGrupo;
    }
}

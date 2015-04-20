package proveedores;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;
import proveedores.dao.DAOProveedoresProductosOfertas;
import proveedores.dominio.ProveedorProductoOferta;

/**
 *
 * @author jsolis
 */
@Named(value = "mbProveedorProductoOfertas")
@SessionScoped
public class MbProveedorProductoOfertas implements Serializable {
    private int idProveedor;
    private int idProducto;
    //private String strIniVigencia;
    //private String strFinVigencia;
    private ProveedorProductoOferta oferta;
    private ArrayList<ProveedorProductoOferta> ofertas;
    private DAOProveedoresProductosOfertas dao;
    
    public MbProveedorProductoOfertas() {
        this.oferta=new ProveedorProductoOferta();
    }
    
    public void cambioBase() {
        this.oferta.setPtjeOferta(0);
        this.oferta.setPrecioOferta(0);
        this.oferta.setSinCargo(1);
    }
    
    public void cambioPrecioOferta() {
        this.oferta.setPtjeOferta(0);
        this.oferta.setBase(0);
        this.oferta.setSinCargo(0);
    }
    
    public void cambioPtjeOferta() {
        this.oferta.setPrecioOferta(0);
        this.oferta.setBase(0);
        this.oferta.setSinCargo(0);
    }
    
    private void cargaOfertas() {
        boolean resultado=false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao=new DAOProveedoresProductosOfertas();
            this.ofertas=this.dao.obtenerOfertas(idProveedor, idProducto);
            resultado=true;
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        }
        if (!resultado) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okOferta", resultado);
    }
    
    public boolean eliminar() {
        boolean resultado = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao=new DAOProveedoresProductosOfertas();
            this.dao.eliminar(this.oferta, this.idProveedor, this.idProducto);
            resultado=true;
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if (!resultado) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okOferta", resultado);
        return resultado;
    }
    
    public boolean grabar() {
        boolean resultado = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            Date utilFecha;
            //java.sql.Date sqlFecha;
            
            if(this.oferta.getInicioVigencia()==null) {
                throw new Exception("Se requiere la fecha de inicio de vigencia");
            } else if(this.oferta.isNuevo()) {
                utilFecha=utilerias.Utilerias.addDays(this.oferta.getInicioVigencia(), 1);
                this.oferta.setInicioVigencia(utilFecha);
            }
            if(this.oferta.getFinVigencia()!=null) {
                utilFecha=utilerias.Utilerias.addDays(this.oferta.getFinVigencia(), 1);
                this.oferta.setFinVigencia(utilFecha);
                if(this.oferta.getInicioVigencia().after(this.oferta.getFinVigencia())) {
                    throw new Exception("La fecha final no puede ser anterior a la fecha inicial");
                }
            }
            if(this.oferta.getBase()>0) {
                if(this.oferta.getSinCargo() == 0 || this.oferta.getSinCargo() < this.oferta.getBase()) {
                    throw new Exception("La cantidad sin cargo no puede ser cero, ni mayor que la cantidad con cargo");
                }
            }
            this.dao=new DAOProveedoresProductosOfertas();
            if(this.oferta.isNuevo()) {
                for(ProveedorProductoOferta ofer: this.ofertas) {
                    if(ofer.getFinVigencia()==null) {
                        if(this.oferta.getFinVigencia()==null) {
                            throw new Exception("No se puede agregar una oferta sin fin de vigencia cuando ya existe una");
                        } else if(!ofer.getInicioVigencia().before(this.oferta.getInicioVigencia())) {
                            throw new Exception("El período de una oferta no puede iniciar dentro de otro ( sin fin de vigencia )");
                        } else if(!ofer.getFinVigencia().before(this.oferta.getFinVigencia())) {
                            throw new Exception("El período de una oferta no puede finalizar dentro de otro ( sin fin de vigencia )");
                        }
                    } else if(!(this.oferta.getInicioVigencia().before(ofer.getInicioVigencia()) || this.oferta.getInicioVigencia().after(ofer.getFinVigencia()))) {
                        throw new Exception("El período de una oferta no puede iniciar dentro de otro");
                    } else if(this.oferta.getFinVigencia()!=null) {
                        if(!(this.oferta.getFinVigencia().before(ofer.getInicioVigencia()) || this.oferta.getFinVigencia().after(ofer.getFinVigencia()))) {
                            throw new Exception("El período de una oferta no puede finalizar dentro de otro");
                        }
                    }
                }
                this.oferta.setNuevo(this.dao.agregar(this.oferta, this.idProveedor, this.idProducto));
            } else {
                for(ProveedorProductoOferta ofer: this.ofertas) {
                    if(ofer.getFinVigencia()==null) {
                        if(this.oferta.getFinVigencia()==null) {
                            throw new Exception("No pueden haber dos ofertas sin fin de vigencia");
                        } else if(!ofer.getFinVigencia().before(this.oferta.getFinVigencia())) {
                            throw new Exception("El período de una oferta no puede finalizar dentro de otro ( sin fin de vigencia )");
                        }
                    } else if(this.oferta.getFinVigencia()!=null) {
                        if(!(this.oferta.getFinVigencia().before(ofer.getInicioVigencia()) || this.oferta.getFinVigencia().after(ofer.getFinVigencia()))) {
                            throw new Exception("El período de una oferta no puede finalizar dentro de otro");
                        }
                    }
                }
                this.dao.modificar(this.oferta, this.idProveedor, this.idProducto);
            }
            resultado=true;
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (Exception ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_WARN);
            fMsg.setDetail(ex.getMessage());
        }   
        if (!resultado) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okOferta", resultado);
        return resultado;
    }
    
    public void copia(ProveedorProductoOferta oferta) {
        this.oferta=new ProveedorProductoOferta();
        this.oferta.setBase(oferta.getBase());
        this.oferta.setFechaLista(oferta.getFechaLista());
        this.oferta.setFinVigencia(oferta.getFinVigencia());
        this.oferta.setInicioVigencia(oferta.getInicioVigencia());
        this.oferta.setPtjeOferta(oferta.getPtjeOferta());
        this.oferta.setPrecioOferta(oferta.getPrecioOferta());
        this.oferta.setSinCargo(oferta.getSinCargo());
        this.oferta.setNuevo(oferta.isNuevo());
    }

    public ProveedorProductoOferta getOferta() {
        return oferta;
    }

    public void setOferta(ProveedorProductoOferta oferta) {
        this.oferta = oferta;
    }

    public ArrayList<ProveedorProductoOferta> getOfertas() {
        if(this.ofertas==null) {
            this.cargaOfertas();
        }
        return ofertas;
    }

    public void setOfertas(ArrayList<ProveedorProductoOferta> ofertas) {
        this.ofertas = ofertas;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }
    /*
    public String getStrIniVigencia() {
        return strIniVigencia;
    }

    public void setStrIniVigencia(String strIniVigencia) {
        this.strIniVigencia = strIniVigencia;
    }

    public String getStrFinVigencia() {
        return strFinVigencia;
    }

    public void setStrFinVigencia(String strFinVigencia) {
        this.strFinVigencia = strFinVigencia;
    }
    * */
}

package proveedores;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;
import proveedores.dao.DAOProveedoresProductosOfertas;
import proveedores.dao.DAOProveedoresProductosPrecios;
import proveedores.dominio.ProveedorProductoOferta;
import proveedores.dominio.ProveedorProductoPrecio;

/**
 *
 * @author jsolis
 */
@Named(value = "mbProveedorProductoPrecios")
@SessionScoped
public class MbProveedorProductoPrecios implements Serializable {
    private int idProveedor;
    private int idProducto;
    private ProveedorProductoPrecio precio;
    private ArrayList<ProveedorProductoPrecio> precios;
    private DAOProveedoresProductosPrecios dao;
    
    public MbProveedorProductoPrecios() {
        this.precio=new ProveedorProductoPrecio();
    }
    
    private void cargaPrecios() {
        boolean resultado=false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao=new DAOProveedoresProductosPrecios();
            this.precios=this.dao.obtenerPrecios(idProveedor, idProducto);
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
        context.addCallbackParam("okPrecio", resultado);
    }
    
    public boolean eliminar() {
        boolean resultado = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao=new DAOProveedoresProductosPrecios();
            this.dao.eliminar(this.precio, this.idProveedor, this.idProducto);
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
        context.addCallbackParam("okPrecio", resultado);
        return resultado;
    }
    
    public boolean grabar() {
        boolean resultado = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            Date utilFecha;
            
            if(this.precio.getInicioVigencia()==null) {
                throw new Exception("Se requiere la fecha de inicio de vigencia");
            } else if(this.precio.isNuevo()) {
                utilFecha=utilerias.Utilerias.addDays(this.precio.getInicioVigencia(), 1);
                this.precio.setInicioVigencia(utilFecha);
            }
            if(this.precio.getFinVigencia()!=null) {
                utilFecha=utilerias.Utilerias.addDays(this.precio.getFinVigencia(), 1);
                this.precio.setFinVigencia(utilFecha);
                if(this.precio.getInicioVigencia().after(this.precio.getFinVigencia())) {
                    throw new Exception("La fecha final no puede ser anterior a la fecha inicial");
                }
            }
            if(this.precio.getPrecioLista()==0) {
                throw new Exception("El precio de lista no puede ser cero");
            }
            this.dao=new DAOProveedoresProductosPrecios();
            if(this.precio.isNuevo()) {
                for(ProveedorProductoPrecio pre: this.precios) {
                    if(pre.getFinVigencia()==null) {
                        if(this.precio.getFinVigencia()==null) {
                            throw new Exception("No se puede agregar un cambio sin fin de vigencia cuando ya existe una");
                        } else if(!pre.getInicioVigencia().before(this.precio.getInicioVigencia())) {
                            throw new Exception("El período de un cambio no puede iniciar dentro de otro ( sin fin de vigencia )");
                        } else if(!pre.getFinVigencia().before(this.precio.getFinVigencia())) {
                            throw new Exception("El período de un cambio no puede finalizar dentro de otro ( sin fin de vigencia )");
                        }
                    } else if(!(this.precio.getInicioVigencia().before(pre.getInicioVigencia()) || this.precio.getInicioVigencia().after(pre.getFinVigencia()))) {
                        throw new Exception("El período de un cambio no puede iniciar dentro de otro");
                    } else if(this.precio.getFinVigencia()!=null) {
                        if(!(this.precio.getFinVigencia().before(pre.getInicioVigencia()) || this.precio.getFinVigencia().after(pre.getFinVigencia()))) {
                            throw new Exception("El período de un cambio no puede finalizar dentro de otro");
                        }
                    }
                }
                this.precio.setNuevo(this.dao.agregar(this.precio, this.idProveedor, this.idProducto));
            } else {
                for(ProveedorProductoPrecio pre: this.precios) {
                    if(pre.getFinVigencia()==null) {
                        if(this.precio.getFinVigencia()==null) {
                            throw new Exception("No pueden haber dos cambios sin fin de vigencia");
                        } else if(!pre.getFinVigencia().before(this.precio.getFinVigencia())) {
                            throw new Exception("El período de un cambio no puede finalizar dentro de otro ( sin fin de vigencia )");
                        }
                    } else if(this.precio.getFinVigencia()!=null) {
                        if(!(this.precio.getFinVigencia().before(pre.getInicioVigencia()) || this.precio.getFinVigencia().after(pre.getFinVigencia()))) {
                            throw new Exception("El período de un cambio no puede finalizar dentro de otro");
                        }
                    }
                }
                this.dao.modificar(this.precio, this.idProveedor, this.idProducto);
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
        context.addCallbackParam("okPrecio", resultado);
        return resultado;
    }
    
    public void copia(ProveedorProductoPrecio precio) {
        this.precio=new ProveedorProductoPrecio();
        this.precio.setPrecioLista(precio.getPrecioLista());
        this.precio.setDesctoComercial1(precio.getDesctoComercial1());
        this.precio.setDesctoComercial2(precio.getDesctoComercial2());
        this.precio.setDesctoConfidencial(precio.getDesctoConfidencial());
        this.precio.setFechaLista(precio.getFechaLista());
        this.precio.setInicioVigencia(precio.getInicioVigencia());
        this.precio.setFinVigencia(precio.getFinVigencia());
        this.precio.setNuevo(precio.isNuevo());
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

    public ProveedorProductoPrecio getPrecio() {
        return precio;
    }

    public void setPrecio(ProveedorProductoPrecio precio) {
        this.precio = precio;
    }

    public ArrayList<ProveedorProductoPrecio> getPrecios() {
        if(this.precios==null) {
            this.cargaPrecios();
        }
        return precios;
    }

    public void setPrecios(ArrayList<ProveedorProductoPrecio> precios) {
        this.precios = precios;
    }
}

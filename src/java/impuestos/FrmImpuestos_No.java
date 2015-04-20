package impuestos;

import impuestos.dominio.ImpuestoGrupo;
import impuestos.dominio.ImpuestoZona;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

@ManagedBean(name = "frmImpuestos_no")
@SessionScoped
public class FrmImpuestos_No implements Serializable {

    private ImpuestoZona zona;
    private ImpuestoGrupo grupo;
    //private ImpuestoDetalle detalle;
    
    @ManagedProperty(value = "#{mbZonas}")
    private MbZonas mbZonas;
    @ManagedProperty(value = "#{mbGrupos}")
    private MbGrupos mbGrupos;
    @ManagedProperty(value = "#{mbImpuestos}")
    private MbImpuestos mbImpuestos;
    @ManagedProperty(value = "#{mbDetalle}")
    private FrmImpuestos mbDetalle;
    //@ManagedProperty(value = "#{detalle}")
    //private ImpuestoDetalle detalle;
    
    public FrmImpuestos_No() {
            this.mbZonas = new MbZonas();
            this.mbGrupos = new MbGrupos();
            this.mbImpuestos = new MbImpuestos();
            this.mbDetalle = new FrmImpuestos();
    }
    
    public String salir() {
        String destino="index.xhtml";
        this.zona=new ImpuestoZona(0, "");
        this.mbZonas.setListaZonas(null);
        this.grupo=new ImpuestoGrupo(0, "");
        this.mbGrupos.setListaGrupos(null);
        this.mbDetalle.setDetalle(null);
        this.mbDetalle.setDetalles(null);
        this.mbDetalle.setPeriodo("1");
        this.mbDetalle.setSoloLectura(true);
        
        /*
        this.detalle.setZona(new ImpuestoZona(0, ""));
        this.mbZonas.setListaZonas(null);
        this.detalle.setGrupo(new ImpuestoGrupo(0, ""));
        this.mbGrupos.setListaGrupos(null);
        //this.mbDetalle.setDetalle(new ImpuestoDetalle());
        this.mbImpuestos.setImpuesto(new Impuesto(0, "", false, 1, false));
        this.mbDetalle.setDetalles(null);
        * */
        return destino;
    }
    
    public void seleccionarGrupo() {
        if(!this.grupo.equals(this.mbGrupos.getGrupo())) {
            this.grupo=this.mbGrupos.getGrupo();
            this.cambiarGrupo();
        }
    }
    
    public void eliminarGrupo() {
        if (this.mbGrupos.eliminarGrupo()) {
            this.grupo=this.mbGrupos.getGrupo();
            this.cambiarGrupo();
        }
    }
    /*
    public void grabarGrupo() {
        if (this.mbGrupos.grabar()) {
            this.grupo = this.mbGrupos.getGrupo();
        }
    }
    * */

    public void mttoGrupos() {
        ImpuestoGrupo g;
        if (this.grupo == null) {
            g = new ImpuestoGrupo(0, "");
        } else {
            g = new ImpuestoGrupo(this.grupo.getIdGrupo(), this.grupo.getGrupo());
        }
        this.mbGrupos.setGrupo(g);
        this.mbGrupos.setImpuestosAgregados(this.mbGrupos.obtenerImpuestosAgregados());
        this.mbGrupos.setImpuestosDisponibles(this.mbGrupos.obtenerImpuestosDisponibles());
    }
    
    public void eliminarZona() {
        if(this.mbZonas.eliminar()) {
            this.zona=this.mbZonas.getZona();
        }
    }
    
    public void grabarZona() {
        if (this.mbZonas.grabar()) {
            this.zona=this.mbZonas.getZona();
        }
    }
    
    public void mttoZonas() {
        ImpuestoZona z = new ImpuestoZona(this.zona.getIdZona(), this.zona.getZona());
        this.mbZonas.setZona(z);
    }
    
    public void cambiarZona() {
        this.mbDetalle.setPeriodo("2");
        this.cargarImpuestosDetalle();
        this.mbDetalle.setPeriodo("1");
        this.cambiarPeriodo();
    }

    public void cambiarGrupo() {
        this.mbDetalle.setPeriodo("2");
        this.cargarImpuestosDetalle();
        this.mbDetalle.setPeriodo("1");
        this.cambiarPeriodo();
    }

    public void cambiarPeriodo() {
        this.mbDetalle.cambioDePeriodo();
        this.cargarImpuestosDetalle();
        this.mbDetalle.setSoloLectura(true);
        this.mbDetalle.setDetalle(null);
        //this.mbDetalle.setDetalle(new ImpuestoDetalle());
    }

    private void cargarImpuestosDetalle() {
//        int idZona = 0;
//        if (this.zona != null) {
//            idZona = this.zona.getIdZona();
//        }
//        int idGrupo = 0;
//        if (this.grupo != null) {
//            idGrupo = this.grupo.getIdGrupo();
//        }
//        this.mbDetalle.cargarDetalles(idZona, idGrupo);
    }
    /*
    public ImpuestoDetalle getDetalle() {
        return detalle;
    }

    public void setDetalle(ImpuestoDetalle detalle) {
        this.detalle = detalle;
    }
    */
    public ImpuestoGrupo getGrupo() {
        return grupo;
    }

    public void setGrupo(ImpuestoGrupo grupo) {
        this.grupo = grupo;
    }
    
    public ImpuestoZona getZona() {
        return zona;
    }

    public void setZona(ImpuestoZona zona) {
        this.zona = zona;
    }
    
    public MbZonas getMbZonas() {
        return mbZonas;
    }

    public void setMbZonas(MbZonas mbZonas) {
        this.mbZonas = mbZonas;
    }

    public MbGrupos getMbGrupos() {
        return mbGrupos;
    }

    public void setMbGrupos(MbGrupos mbGrupos) {
        this.mbGrupos = mbGrupos;
    }
    
    public MbImpuestos getMbImpuestos() {
        return mbImpuestos;
    }

    public void setMbImpuestos(MbImpuestos mbImpuestos) {
        this.mbImpuestos = mbImpuestos;
    }

    public FrmImpuestos getMbDetalle() {
        return mbDetalle;
    }

    public void setMbDetalle(FrmImpuestos mbDetalle) {
        this.mbDetalle = mbDetalle;
    }
}

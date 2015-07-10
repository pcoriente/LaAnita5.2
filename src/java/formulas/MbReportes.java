package formulas;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.faces.bean.ManagedProperty;
import producto2.MbGrupo;
import producto2.MbTipo;

/**
 *
 * @author jesc
 */
@Named(value = "mbReportesFormulas")
@SessionScoped
public class MbReportes implements Serializable {
    @ManagedProperty(value = "#{mbTipo}")
    private MbTipo mbTipo;
    @ManagedProperty(value = "#{mbGrupo}")
    private MbGrupo mbGrupo;
    @ManagedProperty(value = "#{mbFormulas}")
    private MbFormulas mbFormulas;
    private String buscarPor;
    private boolean ok;

    public MbReportes() {
        this.mbTipo = new MbTipo();
        this.mbGrupo=new MbGrupo();
        this.mbFormulas = new MbFormulas();

        this.buscarPor = "BUSCAR";
    }
    
    public String terminar() {
        this.mbFormulas.getMbEmpresas().inicializar();
        this.mbTipo.inicializar();
        this.mbGrupo.inicializar();
        this.mbFormulas.getMbBuscar().inicializar();
        this.buscarPor="BUSCAR";
        return "index.xhtml";
    }
    
    public void cambioDeParametros() {
        this.ok=false;
        this.buscarPor = "BUSCAR";
    }
    
    public void cambioDeBuscar() {
        this.ok=false;
    }
    
    public void verVariables() {
        this.ok=false;
        int idEmpresa=0;
        int idTipo=0;
        int idGrupo=0;
        int idSubGrupo=0;
        String busca="";
        if(this.mbFormulas.getMbEmpresas().getEmpresa()!=null) {
            idEmpresa=this.mbFormulas.getMbEmpresas().getEmpresa().getIdEmpresa();
        }
        if(this.mbTipo.getTipo()!=null) {
            idTipo=this.mbTipo.getTipo().getIdTipo();
        }
        if(this.mbGrupo.getGrupo()!=null) {
            idGrupo=this.mbGrupo.getGrupo().getIdGrupo();
        }
        if(this.mbGrupo.getMbSubGrupo().getSubGrupo()!=null) {
            idSubGrupo=this.mbGrupo.getMbSubGrupo().getSubGrupo().getIdSubGrupo();
        }
        if(this.buscarPor!=null) {
            busca=this.buscarPor;
        }
    }
    
    public void generarFormulasPdf() {
//        this.verVariables();
        this.mbFormulas.generarPdf(this.buscarPor);
    }
    
    public void generarFormulasXls() {
//        this.verVariables();
        this.mbFormulas.generarXls(this.buscarPor);
    }
    
    public void generarFormulas() {
//        this.verVariables();
        this.ok=this.mbFormulas.generar(this.buscarPor, this.mbTipo.getTipo().getIdTipo(), this.mbGrupo.getGrupo().getIdGrupo(), this.mbGrupo.getMbSubGrupo().getSubGrupo().getIdSubGrupo());
    }

    public void limpiar() {
        this.cambioDeParametros();
        this.buscarPor = "BUSCAR";
        this.mbFormulas.getMbBuscar().setProducto(null);
    }
    
    public void cargaSubGrupos() {
        this.cambioDeParametros();
        this.mbGrupo.getMbSubGrupo().cargaListaSubGrupos(this.mbGrupo.getGrupo().getIdGrupo());
    }

    public void buscar() {
        this.mbFormulas.getMbBuscar().buscarLista();
    }

    public void configurarReporte() {
        this.cambioDeParametros();
        this.mbFormulas.getMbBuscar().inicializar();
        this.mbFormulas.getMbBuscar().setUpdate(":main:txtCod_pro :main:txtProducto :main:btnBuscar :main:btnNuevaBusqueda :main:btnAceptar :main:btnImprimirXls :main:btnImprimirPdf");
    }

    public String getBuscarPor() {
        return buscarPor;
    }

    public void setBuscarPor(String buscarPor) {
        this.buscarPor = buscarPor;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public MbTipo getMbTipo() {
        return mbTipo;
    }

    public void setMbTipo(MbTipo mbTipo) {
        this.mbTipo = mbTipo;
    }

    public MbGrupo getMbGrupo() {
        return mbGrupo;
    }

    public void setMbGrupo(MbGrupo mbGrupo) {
        this.mbGrupo = mbGrupo;
    }

    public MbFormulas getMbFormulas() {
        return mbFormulas;
    }

    public void setMbFormulas(MbFormulas mbFormulas) {
        this.mbFormulas = mbFormulas;
    }
}

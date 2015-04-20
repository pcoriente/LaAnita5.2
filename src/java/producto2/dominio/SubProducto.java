package producto2.dominio;

/**
 *
 * @author JULIOS
 */
public class SubProducto {
    private int idProducto;
    private int piezas;
    private Empaque empaque;
    private SubProducto subSubProducto;

    public SubProducto() {
        this.empaque=new Empaque();
//        this.subProducto=new SubProducto(0,0,new Empaque());
    }
    
    public SubProducto(int idProducto) {
        this.idProducto = idProducto;
        this.empaque = new Empaque();
    }

    public SubProducto(int idProducto, int piezas, Empaque empaque) {
        this.idProducto = idProducto;
        this.piezas = piezas;
        this.empaque = empaque;
    }

    @Override
    public String toString() {
        String str="";
        if(this.empaque.getIdEmpaque()!=1) {
            if(this.piezas > 1) {
                str+=Integer.toString(this.piezas)+"/";
            }
            str+=this.empaque.getAbreviatura().equals("")?this.empaque.getEmpaque():this.empaque.getAbreviatura();
        }
        str+=this.subSubProducto==null?"":(" "+this.subSubProducto.toString());
        return str;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.idProducto;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SubProducto other = (SubProducto) obj;
        if (this.idProducto != other.idProducto) {
            return false;
        }
        return true;
    }
    
    public int piezasTotales() {
        return this.piezas*(this.subSubProducto==null?1:this.subSubProducto.piezasTotales());
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getPiezas() {
        return piezas;
    }

    public void setPiezas(int piezas) {
        this.piezas = piezas;
    }

    public Empaque getEmpaque() {
        return empaque;
    }

    public void setEmpaque(Empaque empaque) {
        this.empaque = empaque;
    }

    public SubProducto getSubSubProducto() {
        return subSubProducto;
    }

    public void setSubSubProducto(SubProducto subSubProducto) {
        this.subSubProducto = subSubProducto;
    }
}
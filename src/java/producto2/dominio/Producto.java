package producto2.dominio;

/**
 *
 * @author jesc
 */
public class Producto {

    private int idProducto;
    private String cod_pro;
    private Upc upc;
    private Articulo articulo;
    private int piezas;
    private Empaque empaque;
    private SubProducto subProducto;
    private String dun14;
    private double peso;
    private double volumen;

    public Producto() {
        this.cod_pro = "";
        this.upc = new Upc();
        this.articulo = new Articulo();
        this.empaque = new Empaque();
        this.subProducto = new SubProducto();
        this.dun14 = "";
    }

    public Producto(Articulo articulo, Upc upc) {
        this.cod_pro = "";
        this.upc = upc;
        this.articulo = articulo;
        this.empaque = new Empaque();
        this.subProducto = new SubProducto();
        this.dun14 = "";
    }

//    public String toString() {
//        String str=this.articulo.toString();
////        if(this.piezas>1) {
////            str+=" "+(this.empaque.getAbreviatura().equals("")?this.empaque.toString():this.empaque.getAbreviatura())+" c/"+Integer.toString(this.piezas);
////            if(this.subProducto.getIdProducto()!=0) {
////                str+=" ("+this.subProducto.getEmpaque().getAbreviatura()+" x "+Integer.toString(this.subProducto.getPiezas())+")";
////            }
////        }
//        if(this.empaque.getIdEmpaque()!=1) {
//            str+=" "+(this.empaque.getAbreviatura().equals("")?this.empaque.toString():this.empaque.getAbreviatura());
//            if(this.piezas!=1) {
//                str+=" c/"+Integer.toString(this.piezas);
//            }
////            if(this.subProducto.getIdProducto()!=0 && this.subProducto.getEmpaque().getIdEmpaque()!=1) {
////                str+=" ("+(this.subProducto.getEmpaque().getAbreviatura().equals("")?this.subProducto.getEmpaque():this.subProducto.getEmpaque().getAbreviatura());
////                if(this.piezas!=1) {
////                    str+=" x "+Integer.toString(this.subProducto.getPiezas());
////                }
////                str+=")";
////            }
//            if(this.subProducto==null) {
//                str+="";
//            } else {
////                if(this.subProducto.getIdProducto()!=0) {
//                str+=" "+this.subProducto.toString();
//            }
//        }
//        return str;
//    }
    @Override
    public String toString() {
        String str;
        try {
            str = this.articulo.toString();
            if (this.empaque.getIdEmpaque() != 1) {
                if (this.subProducto == null) {
                    str += "";
                } else {
                    str += " " + this.subProducto.toString();
                }
                str += " ";
                if (this.piezas != 1) {
                    str += Integer.toString(this.piezas) + "/";
                }
                str += (this.empaque.getAbreviatura().equals("") ? this.empaque.toString() : this.empaque.getAbreviatura());

            }
            if (str == null) {
                str = "-------------------" + Integer.toString(idProducto) + "-------------------";
            }
        } catch (NullPointerException e) {
            str = "-------------------" + Integer.toString(idProducto) + "-------------------";
        }

        return str;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + this.idProducto;
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
        final Producto other = (Producto) obj;
        if (this.idProducto != other.idProducto) {
            return false;
        }
        return true;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getCod_pro() {
        return cod_pro;
    }

    public void setCod_pro(String cod_pro) {
        this.cod_pro = cod_pro;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
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

    public SubProducto getSubProducto() {
        return subProducto;
    }

    public void setSubProducto(SubProducto subProducto) {
        this.subProducto = subProducto;
    }

    public String getDun14() {
        return dun14;
    }

    public void setDun14(String dun14) {
        this.dun14 = dun14;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public double getVolumen() {
        return volumen;
    }

    public void setVolumen(double volumen) {
        this.volumen = volumen;
    }

    public Upc getUpc() {
        return upc;
    }

    public void setUpc(Upc upc) {
        this.upc = upc;
    }
}

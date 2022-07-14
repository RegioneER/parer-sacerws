package it.eng.parer.util.ejb.help.dto;

import java.io.Serializable;

public class AplVGetValParamDto implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5996789178422769294L;
    private String dsValoreParamApplic;
    private String tiAppart;

    public AplVGetValParamDto() {
        super();
        // TODO Auto-generated constructor stub
    }

    public AplVGetValParamDto(String dsValoreParamApplic, String tiAppart) {
        super();
        this.dsValoreParamApplic = dsValoreParamApplic;
        this.tiAppart = tiAppart;
    }

    public AplVGetValParamDto(String dsValoreParamApplic) {
        super();
        this.dsValoreParamApplic = dsValoreParamApplic;
    }

    public String getDsValoreParamApplic() {
        return dsValoreParamApplic;
    }

    public void setDsValoreParamApplic(String dsValoreParamApplic) {
        this.dsValoreParamApplic = dsValoreParamApplic;
    }

    public String getTiAppart() {
        return tiAppart;
    }

    public void setTiAppart(String tiAppart) {
        this.tiAppart = tiAppart;
    }
}

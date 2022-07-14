package it.eng.parer.ws.versamento.dto;

/**
 *
 * @author Fioravanti_F
 */
public class DatoSpecifico implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2432324299565034042L;
    private long idDatoSpec;
    private String chiave = null;
    private String valore = null;

    /**
     * @return the idDatoSpec
     */
    public long getIdDatoSpec() {
        return idDatoSpec;
    }

    /**
     * @param idDatoSpec
     *            the idDatoSpec to set
     */
    public void setIdDatoSpec(long idDatoSpec) {
        this.idDatoSpec = idDatoSpec;
    }

    /**
     * @return the chiave
     */
    public String getChiave() {
        return chiave;
    }

    /**
     * @param chiave
     *            the chiave to set
     */
    public void setChiave(String chiave) {
        this.chiave = chiave;
    }

    /**
     * @return the valore
     */
    public String getValore() {
        return valore;
    }

    /**
     * @param valore
     *            the valore to set
     */
    public void setValore(String valore) {
        this.valore = valore;
    }
}

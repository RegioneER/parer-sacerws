/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.ws.versamentoMM.ejb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;

import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.versamento.dto.ComponenteVers;
import it.eng.parer.ws.versamento.dto.FileBinario;
import it.eng.parer.ws.versamento.dto.RispostaWS;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.dto.VoceDiErrore;
import it.eng.parer.ws.versamentoMM.dto.ComponenteMM;
import it.eng.parer.ws.versamentoMM.dto.VersamentoMMExt;

/**
 *
 * @author Fioravanti_F
 */
@Stateless(mappedName = "AllineaFileComponenti")
@LocalBean
public class AllineaFileComponenti {

    public void verificaCoerenza(String pathFileTemporanei, VersamentoMMExt versamento, RispostaWS rispostaWs,
            SyncFakeSessn sessioneFinta) throws IOException {
        ComponenteVers tmpComponenteVers;
        for (ComponenteMM componenteMM : versamento.getStrutturaIndiceMM().getComponenti().values()) {
            tmpComponenteVers = versamento.getStrutturaComponenti().getFileAttesi().get(componenteMM.getId());
            boolean erroriComp = false;

            if (tmpComponenteVers == null) {
                erroriComp = true;
                // Componente Multimedia <ID> {0}: Non \u00e8 stato trovato un componente di versamento corrispondente
                versamento.listErrAddErrorUd(componenteMM.getMyComponenteMM().getID(), MessaggiWSBundle.MM_COMP_006_001,
                        componenteMM.getMyComponenteMM().getID());
                // inutile proseguire: se i due xml sono incoerenti a questo livello tutti gli altri test sono privi di
                // senso.
            } else {
                if (tmpComponenteVers.getTipoSupporto() != ComponenteVers.TipiSupporto.FILE) {
                    erroriComp = true;
                    // Componente Multimedia <ID> {0}: Il componente di versamento deve essere di tipo FILE
                    versamento.listErrAddErrorUd(componenteMM.getMyComponenteMM().getID(),
                            MessaggiWSBundle.MM_COMP_007_001, componenteMM.getMyComponenteMM().getID());
                }
                if (componenteMM.isForzaFirmeFormati() && tmpComponenteVers.getMySottoComponente() != null) {
                    erroriComp = true;
                    // Componente Multimedia <ID> {0}: La forzatura di firme o formati \u00e8 possibile solo sui
                    // componenti
                    versamento.listErrAddErrorUd(componenteMM.getMyComponenteMM().getID(),
                            MessaggiWSBundle.MM_COMP_008_001, componenteMM.getMyComponenteMM().getID());
                }
                if (componenteMM.isForzaFirmeFormati()
                        && tmpComponenteVers.getMyComponente().getSottoComponenti() != null
                        && tmpComponenteVers.getMyComponente().getSottoComponenti().getSottoComponente().size() > 0) {
                    erroriComp = true;
                    // Componente Multimedia <ID> {0}: La forzatura di firme o formati \u00e8 possibile solo sui
                    // componenti privi di sottocomponenti
                    versamento.listErrAddErrorUd(componenteMM.getMyComponenteMM().getID(),
                            MessaggiWSBundle.MM_COMP_009_001, componenteMM.getMyComponenteMM().getID());
                }
            }
            //
            if (!erroriComp) {
                tmpComponenteVers.setRifComponenteMM(componenteMM);
                componenteMM.setRifComponenteVers(tmpComponenteVers);
                if (componenteMM.isForzaHash()) {
                    tmpComponenteVers.setHashForzato(true);
                    // utile in seguito, per sapere che questo hash non è stato calcolato da SACER
                    tmpComponenteVers.setHashCalcolato(componenteMM.getHashForzato());
                }
            }
        }
        // verifica se ci sono componenti dichiarati nel SIP che non hanno un omologo nel PISIP
        for (ComponenteVers componenteVers : versamento.getStrutturaComponenti().getFileAttesi().values()) {
            if (componenteVers.getTipoSupporto() == ComponenteVers.TipiSupporto.FILE
                    && componenteVers.getRifComponenteMM() == null) {
                // Componente di versamento <ID> {0}: Non \u00e8 stato trovato un componente Multimedia corrispondente
                versamento.listErrAddErrorUd(componenteVers.getId(), MessaggiWSBundle.MM_COMP_010_001,
                        componenteVers.getId());
            }
        }
        //
        VoceDiErrore tmpVdE = versamento.calcolaErrorePrincipale();
        if (tmpVdE != null) {
            rispostaWs.setSeverity(tmpVdE.getSeverity());
            if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.NEGATIVO) {
                rispostaWs.setEsitoWsError(tmpVdE.getErrorCode(), tmpVdE.getErrorMessage());
            } else if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.WARNING) {
                rispostaWs.setEsitoWsWarning(tmpVdE.getErrorCode(), tmpVdE.getErrorMessage());
            }
        }

        /*
         *
         */
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            String tmpPrefissoPath = versamento.getPrefissoPathPerApp();
            String tmpFilePath;
            File tmpFile;
            FileBinario tmpFileBinario;
            File tmpDirectoryPerZip = null;
            if (versamento.isContainerZip()) {

                // LS: 20/06/2016, Ho bisogno che il contenuto la cartella creata
                // da glassfish sia accessibile da tomcat. Di default
                // Files.createTempDirectory crea una cartella con i seguenti
                // permessi: rwx------
                // I file ivi contenuti, invece, hanno dei permessi corretti
                Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxr-xr-x");
                FileAttribute<Set<PosixFilePermission>> attribute = PosixFilePermissions.asFileAttribute(perms);

                tmpDirectoryPerZip = Files.createTempDirectory(FileSystems.getDefault().getPath(pathFileTemporanei),
                        "tempZip_", attribute).toFile();
                versamento.setPathLocaleContenutoZip(tmpDirectoryPerZip.getCanonicalPath());
            }
            for (ComponenteVers componente : versamento.getStrutturaComponenti().getFileAttesi().values()) {
                if (componente.getTipoSupporto() == ComponenteVers.TipiSupporto.FILE) {
                    if (versamento.isContainerZip()) {
                        tmpFile = this.estraFileDaZip(versamento.getPathContainerZip(),
                                componente.getRifComponenteMM().getMyComponenteMM().getURNFile(), tmpDirectoryPerZip);
                    } else {
                        tmpFilePath = tmpPrefissoPath
                                + componente.getRifComponenteMM().getMyComponenteMM().getURNFile();
                        tmpFile = new File(tmpFilePath);
                    }

                    tmpFileBinario = new FileBinario();
                    tmpFileBinario.setId(componente.getRifComponenteMM().getMyComponenteMM().getID());
                    tmpFileBinario.setFileSuDisco(tmpFile);
                    tmpFileBinario.setDimensione(tmpFile.length());
                    componente.setRifFileBinario(tmpFileBinario);
                    componente.setDatiLetti(true);
                    // imposta la dimensione file nell'XML di risposta associato al componente trovato
                    // NOTA BENE: solo uno tra componente e sottocomponente è valorizzato,
                    // perciò il doppio IF che segue comporta la scrittura della dimensione su un solo elemento
                    if (componente.getRifComponenteResp() != null) {
                        componente.getRifComponenteResp().setDimensioneFile(BigInteger.valueOf(tmpFile.length()));
                    }
                    if (componente.getRifSottoComponenteResp() != null) {
                        componente.getRifSottoComponenteResp().setDimensioneFile(BigInteger.valueOf(tmpFile.length()));
                    }

                    // incrementa la dimensione in byte del totale dei file da memorizzare
                    versamento.getStrutturaComponenti().setTotalSizeInBytes(
                            versamento.getStrutturaComponenti().getTotalSizeInBytes() + tmpFile.length());

                    // aggiungo il file binario alla sessione finta
                    sessioneFinta.getFileBinari().add(tmpFileBinario);
                }
            }
        }
    }

    private File estraFileDaZip(String zipPathName, String fileInZip, File pathContenutoZip) throws IOException {
        File tmpRetFile = null;
        byte[] bufffer = new byte[512 * 1024]; // 1/2 megabyte di buffer
        ZipArchiveEntry tmpZipArchiveEntry;
        ZipFile tmpZipFile = null;
        FileOutputStream tmpFileOutStream = null;
        InputStream tmpInputStream = null;
        try {
            tmpZipFile = new ZipFile(zipPathName);
            tmpZipArchiveEntry = tmpZipFile.getEntry(fileInZip);
            if (tmpZipArchiveEntry != null) {
                tmpRetFile = File.createTempFile("out_", ".tmp", pathContenutoZip);
                tmpFileOutStream = new FileOutputStream(tmpRetFile);
                tmpInputStream = tmpZipFile.getInputStream(tmpZipArchiveEntry);
                int numBytes;
                while ((numBytes = tmpInputStream.read(bufffer, 0, bufffer.length)) != -1) {
                    tmpFileOutStream.write(bufffer, 0, numBytes);
                }
            }
        } finally {
            IOUtils.closeQuietly(tmpFileOutStream);
            IOUtils.closeQuietly(tmpInputStream);
            ZipFile.closeQuietly(tmpZipFile);
        }

        return tmpRetFile;
    }
}

/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna <p/> This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version. <p/> This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. <p/> You should
 * have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <https://www.gnu.org/licenses/>.
 */

package it.eng;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.File;
import java.util.List;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.MessaggiWSBundle;

public class ArquillianTestUtils {

    public static void saveArchiveTo(Archive<WebArchive> testArchive, String path) {
        testArchive.as(ZipExporter.class).exportTo(new File(path), true);
    }

    public static JavaArchive createSacerLogJavaArchive() {
        return ShrinkWrap.create(JavaArchive.class, "sacerlog.jar")
                .addPackages(true, "it.eng.parer.sacerlog")
                .addAsResource(ArquillianTestUtils.class.getClassLoader()
                        .getResource("ejb-jar-sacerlog.xml"), "META-INF/ejb-jar.xml")
                .addAsResource(
                        ArquillianTestUtils.class.getClassLoader()
                                .getResource("WSRequestIndiceSIPFascicolo_1.0.xsd"),
                        "/it/eng/parer/ws/xml/versfascicolo/WSRequestIndiceSIPFascicolo_1.0.xsd");
    }

    public static JavaArchive createPaginatorJavaArchive() {
        return ShrinkWrap.create(JavaArchive.class, "paginator.jar")
                .addPackages(true, "it.eng.paginator", "it.eng.spagoLite")
                .addAsResource(ArquillianTestUtils.class.getClassLoader()
                        .getResource("ejb-jar-paginator.xml"), "META-INF/ejb-jar.xml");
    }

    public static JavaArchive createSacerWSJavaArchive(List<String> packages, Class<?>... classes) {
        JavaArchive sacerWsJavaArchive = ShrinkWrap.create(JavaArchive.class, "sacerWSEjb.jar");
        sacerWsJavaArchive
                .addPackages(true, "it.eng.parer.entity", "it.eng.parer.grantedEntity",
                        "it.eng.parer.view_entity", "it.eng.parer.util.ejb.help.dto",
                        "it.eng.parer.ws.versamentoUpd.dto", "it.eng.parer.ws.dto",
                        "it.eng.parer.ws.versamento.dto", "org.apache.commons.lang3",
                        "it.eng.parer.jboss.timer.common", "it.eng.parer.crypto.model.exceptions",
                        "it.eng.parer.eidas.model.exception", "org.apache.commons.text",
                        "com.fasterxml.uuid")
                .addPackages(false, "it.eng.parer.sacerlog.entity",
                        "it.eng.parer.sacerlog.view_entity", "it.eng.parer.exception",
                        "it.eng.parer.ws.utils", "it.eng.parer.ws.versamentoUpd.utils",
                        "it.eng.sequences.hibernate")
                .addAsResource(
                        ArquillianTestUtils.class.getClassLoader().getResource("persistence.xml"),
                        "META-INF/persistence.xml")
                .addAsResource(
                        ArquillianTestUtils.class.getClassLoader().getResource("ejb-jar.xml"),
                        "META-INF/ejb-jar.xml")
                .addClass("it.eng.ArquillianTestUtils")
                .addClass(it.eng.parer.util.ejb.help.ConfigurationHelper.class)
                .addClass(it.eng.spagoCore.util.UUIDMdcLogUtil.class)
                .addClass("org.springframework.http.client.ClientHttpRequestInterceptor")
                .addClass("org.springframework.retry.RetryCallback")
                .addClass("org.springframework.http.HttpRequest")
                .addClass("org.springframework.http.HttpMessage");
        for (Class<?> c : classes) {
            sacerWsJavaArchive.addClass(c);
        }
        packages.parallelStream().forEach(sacerWsJavaArchive::addPackage);
        return sacerWsJavaArchive;
    }

    public static EnterpriseArchive createEnterpriseArchive(String archiveName,
            JavaArchive... modules) {
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, archiveName + ".ear")
                .addAsResource(EmptyAsset.INSTANCE, "beans.xml");
        for (JavaArchive m : modules) {
            ear.addAsModule(m);
        }
        return ear;
    }

    public static boolean exceptionMessageContains(Exception e, String... messages) {
        for (String m : messages) {
            final String message = e.getMessage() != null ? e.getMessage()
                    : e.getClass().getSimpleName();
            if (message.contains(m)) {
                return true;
            }
        }
        return false;
    }

    public static void assertNoErr(RispostaControlli rispostaControlli) {
        assertNotEquals(MessaggiWSBundle.ERR_666, rispostaControlli.getCodErr());
        assertNotEquals(MessaggiWSBundle.ERR_666P, rispostaControlli.getCodErr());
        assertNotEquals(MessaggiWSBundle.ERR_666N, rispostaControlli.getCodErr());
    }

}
